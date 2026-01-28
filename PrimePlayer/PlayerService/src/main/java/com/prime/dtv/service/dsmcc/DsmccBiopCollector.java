package com.prime.dtv.service.dsmcc;

import android.os.Build;
import android.os.Process;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;

import com.prime.dtv.service.dsmcc.parse.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

/**
 * DSM-CC -> BIOP 解析 + 檔案落地（透過 ActiveSnapshotManager 管理快照 / 指標 / GC）
 *
 * 重點：
 * 1) 以 DSI.ConnBinder 的 tapTransactionId 為主，無則回退 header.transactionId。
 * 2) BiopSink 事件（onServiceGateway/onDirectory/onFile）序列化到同一條 HandlerThread，
 *    避免非 thread-safe 的 Map/Set 造成資料競爭。
 * 3) BiopReader.parse() 丟到 ThreadPoolExecutor（背景優先序），避免阻塞 DSM-CC Handler。
 * 4) 以「sinkHandler barrier」保證該模組的所有檔案已寫入後再通知 UI 可用。
 * 5) 索引用 hex 字串串接 parent chain，避免熱路徑 encode/decode。
 */
public class DsmccBiopCollector extends DsmccCollectorBase
        implements DownloadSessionManager.Listener, BiopSink {

    private static final String TAG = "DsmccBiopCollector";

    private final DownloadSessionManager mgr = new DownloadSessionManager(this);
    private final ActiveSnapshotManager snapshotMgr;

    private volatile int activeDownloadId = 0;

    // ---- config ----
    private final String serviceName;
    private final String baseDir;            // /data/vendor/dtvdata/<SERVICE>
    private final String mountRoot = "";     // 若要多一層前綴（例 "resources"），可設定
    private static final int PENDING_LIMIT = 512;
    private static final long TOMBSTONE_GRACE_MS = 10 * 60 * 1000; // 例：10 分鐘

    // ---- Module 完成/整輪完成的狀態回拋（可選，給 UI 用）----
    public interface ModuleStateListener {
        default void onModuleSaved(int downloadId, int moduleId, int moduleVersion) {}
        /** 整輪 modules 都已 save 到磁碟後回報。
         *  txid: 這輪的 transaction id（若無則 0）
         *  relPath: 例如 "sessions/82460002"
         *  absPath: 例如 "/data/vendor/dtvdata/<SERVICE>/sessions/82460002"
         */
        default void onCarouselAllSaved(int downloadId, long txid, String relPath, String absPath) {}
    }
    private volatile ModuleStateListener stateListener = null;
    public void setModuleStateListener(ModuleStateListener l) { this.stateListener = l; }

    // ---- Node 基本資訊：以 hex 字串當作 parent 鏈結，避免 encode/decode 開銷 ----
    static final class NodeInfo {
        final String parentHex;  // root = null
        final String name;        // child name under parent
        NodeInfo(String parentHex, String name) { this.parentHex = parentHex; this.name = name; }
    }

    // 已發佈模組 stamp（避免重複處理同內容）
    private static final class ModuleStamp {
        final int version;
        final int size;
        final int crc32;
        ModuleStamp(int version, int size, int crc32) {
            this.version = version; this.size = size; this.crc32 = crc32;
        }
    }
    // Key: (downloadId << 16) | moduleId
    private final ConcurrentHashMap<Long, ModuleStamp> published = new ConcurrentHashMap<>();

    // ---- Per-carousel in-memory indices（以 carouselId 區分）----
    private final Map<Integer, Map<String, NodeInfo>> keyIndexByCarousel = new HashMap<>();
    private final Map<Integer, Map<String, byte[]>> pendingFilesByCarousel = new HashMap<>();
    private final Map<Integer, Long>   lastDsiTxn = new HashMap<>();
    private final Map<Integer, Integer> lastDsiVer = new HashMap<>();
    private final Map<Integer, Integer> lastDiiVer = new HashMap<>();
    private final Map<Integer, Long>                 currentTxidByCarousel = new HashMap<>();
    private final Map<Integer, Long>                 publishedTxidByCarousel = new HashMap<>();
    private final Set<Integer>                       needsFullGc = new HashSet<>();

    // === BiopSink 事件序列化專用執行緒 ===
    private android.os.HandlerThread sinkThread;
    private android.os.Handler sinkHandler;

    private Map<String, NodeInfo> keys(int carouselId) {
        return keyIndexByCarousel.computeIfAbsent(carouselId, k -> new HashMap<>());
    }
    private Map<String, byte[]> pendings(int carouselId) {
        return pendingFilesByCarousel.computeIfAbsent(carouselId, k -> new HashMap<>());
    }

    // === 輪次追蹤（由 DII 建 expected；onModuleComplete + barrier 宣告 saved）===
    private static final class CarouselTracker {
        final Set<Integer> expected = java.util.Collections.synchronizedSet(new HashSet<>());
        final Set<Integer> saved    = java.util.Collections.synchronizedSet(new HashSet<>());
        final java.util.concurrent.atomic.AtomicInteger inflight = new java.util.concurrent.atomic.AtomicInteger(0);
        void setExpected(List<DiiMessage.ModuleInfo> modules) {
            expected.clear(); saved.clear();
            for (DiiMessage.ModuleInfo m : modules) expected.add(m.moduleId);
        }
        boolean allSaved() { return !expected.isEmpty() && saved.containsAll(expected); }
    }
    private final ConcurrentHashMap<Integer, CarouselTracker> trackers = new ConcurrentHashMap<>();

    // 背景解析 BIOP 的 thread pool：避免阻塞 DSM-CC 的 handler（降到背景優先序）
    private final ThreadPoolExecutor biopExecutor =
    new ThreadPoolExecutor(
        Math.max(2, Runtime.getRuntime().availableProcessors() / 2), // core
        Math.max(2, Runtime.getRuntime().availableProcessors()),     // max
        30, TimeUnit.SECONDS,                                        // keepAlive
        new ArrayBlockingQueue<>(32),                                 // 有界佇列
        new ThreadFactory() {
            @Override public Thread newThread(Runnable r) {
                return new Thread(() -> {
                    try { Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND); }
                    catch (Throwable ignore) {}
                    r.run();
                }, "BiopReaderPool");
            }
        },
        new ThreadPoolExecutor.DiscardOldestPolicy()                 // 滿了丟最舊
    );

    // === ctor ===
    public DsmccBiopCollector(int tunerId, int carouselPid, String serviceName) {
        super(tunerId, carouselPid, serviceName);
        this.serviceName = (serviceName != null ? serviceName : "UNKNOWN");
        this.baseDir = "/data/vendor/dtvdata/" + this.serviceName;
        this.snapshotMgr = new ActiveSnapshotManager(this.baseDir);
        Log.d(TAG, "Base dir = " + baseDir);

        // 建立序列化處理 BiopSink 的執行緒（背景優先序）
        sinkThread = new android.os.HandlerThread("BiopSink-Serial", Process.THREAD_PRIORITY_BACKGROUND);
        sinkThread.start();
        try { Process.setThreadPriority(sinkThread.getThreadId(), Process.THREAD_PRIORITY_BACKGROUND); }
        catch (Throwable ignore) {}
        sinkHandler = new android.os.Handler(sinkThread.getLooper());
    }

    public void cleanup() {
        if (sinkHandler != null) sinkHandler.removeCallbacksAndMessages(null);
        if (sinkThread != null) { sinkThread.quitSafely(); sinkThread = null; }
    
        biopExecutor.shutdown(); // 優雅關閉
        try {
            if (!biopExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                biopExecutor.shutdownNow(); // 超時再強硬關
            }
        } catch (InterruptedException e) {
            biopExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ===== DsmccCollectorBase callbacks =====
    @Override
    protected void onDsi(DsiMessage dsi) {
        int dl = (dsi.downloadId != 0) ? dsi.downloadId
                : (dsi.carouselId != -1 ? dsi.carouselId : activeDownloadId);
        if (dl != 0) activeDownloadId = dl;

        // ★ 優先使用 ConnBinder 的 tapTransactionId；若沒有則回退 header.transactionId
        long txn = ((long) (dsi.tapTransactionId != -1 ? dsi.tapTransactionId : dsi.transactionId)) & 0xFFFFFFFFL;
        int  ver = dsi.version;

        boolean changed = false;
        Long prevTxn = lastDsiTxn.get(dl);
        Integer prevVer = lastDsiVer.get(dl);

        if (prevTxn == null || (txn != 0 && !Objects.equals(prevTxn, txn))) {
            lastDsiTxn.put(dl, txn);
            changed = true;
        }
        if (prevVer == null || (ver >= 0 && !Objects.equals(prevVer, ver))) {
            lastDsiVer.put(dl, ver);
            changed = true;
        }

        if (changed && dl != 0) {
            // 新 session：建立 session root，重置 in-memory 狀態，標記需要 full GC（等快照後執行）
            currentTxidByCarousel.put(dl, txn);
            snapshotMgr.ensureSessionRoot(txn);
            resetCarouselState(dl);
            needsFullGc.add(dl);
            snapshotMgr.cleanupExpiredTombstones();
            Log.i(TAG, serviceName + " chuck DSI change: dl=0x" + hex8(dl)
                    + " txid=0x" + hex8(txn) + " ver=" + ver + " -> new session & pending full-GC");
        }

        if (dl != 0) {
            mgr.onDsi(dl);
        } else {
            Log.w(TAG, serviceName + " DSI has no valid downloadId; skip mgr.onDsi()");
        }
    }

    @Override
    protected void onDii(DiiMessage dii) {
        int dl = dii.getDownloadId();
        if (dl != 0) activeDownloadId = dl;

        int diiVer = dii.getVersionNumber();
        Integer prev = lastDiiVer.get(dl);

        if (prev == null || !prev.equals(diiVer)) {
            lastDiiVer.put(dl, diiVer);
            Log.i(TAG, serviceName + " DII ver -> dl=0x" + hex8(dl)
                    + " v=" + diiVer + " (modules=" + dii.getModules().size() + ")");
        }

        Log.d(TAG, "[chuck DII] " + dii.toDebugString());
        for (DiiMessage.ModuleInfo mi : dii.getModules()) {
            Log.d(TAG, "[chuck DII] mod=0x" + hex4(mi.moduleId)
                    + " size=" + mi.moduleSize
                    + " ver=" + mi.moduleVersion
                    + " block=" + mi.blockSize);
        }

        // 建 expected 清單（覆蓋即可；已 saved 會保留不影響）
        CarouselTracker t = trackers.computeIfAbsent(dl, k -> new CarouselTracker());
        t.setExpected(dii.getModules());

        // 通知底層收集
        mgr.onDii(dl, guessBlockSize(dii), dii.getModules());

        // 有新的 DII 就嘗試發佈（冪等）與差異 GC（真發佈在 snapshot 有 live set 後）
        maybePublishAndGcPost(dl, /*forceFull*/ false);
    }

    @Override
    protected void onDdb(DdbMessage ddb) {
        int dl = ddb.getDownloadId();
        if (dl == 0) {
            dl = activeDownloadId;
            ddb.setDownloadId(dl);
        }
        Log.d(TAG, "[chuck DDB] MId=0x" + hex4(ddb.getModuleId())
                + " Mver=" + ddb.getModuleVersion()
                + " Bnum=" + ddb.getBlockNumber()
                + " size=" + (ddb.getData() != null ? ddb.getData().length : 0));

        mgr.onDdb(dl, ddb.getModuleId(), ddb.getModuleVersion(), ddb.getBlockNumber(), ddb.getData());
    }

    private static long modKey(int downloadId, int moduleId) {
        return (((long) downloadId) << 16) | (moduleId & 0xFFFFL);
    }
    
    private static int crc32(byte[] data) {
        CRC32 c = new CRC32();
        c.update(data, 0, data.length);
        return (int) c.getValue();
    }

    private long currentTxidFor(int downloadId) {
        Long t = currentTxidByCarousel.get(downloadId);
        return (t != null) ? t : 0L;
    }
    private String sessionRelPath(long txid) {
        return (txid == 0) ? null : "sessions/" + hex8Lower(txid);
    }
    private String sessionAbsPath(long txid) {
        if (txid == 0) return null;
        File root = new File(baseDir, sessionRelPath(txid));
        return root.getAbsolutePath();
    }

    @Override
    public void onModuleComplete(int downloadId, int moduleId, int moduleVersion, byte[] payload) {
        Log.d(TAG, "chuck " + serviceName + " MODULE DONE: dl=0x" + hex8(downloadId)
                + " mod=0x" + hex4(moduleId)
                + " ver=" + moduleVersion
                + " size=" + (payload != null ? payload.length : 0));

        if (payload == null || payload.length == 0) return;

        final CarouselTracker tracker = trackers.computeIfAbsent(downloadId, k -> new CarouselTracker());
        tracker.inflight.incrementAndGet(); // 這個模組 parse 任務開始

        biopExecutor.execute(() -> {
            long start = SystemClock.elapsedRealtime();
            try {
                if (Build.VERSION.SDK_INT >= 18) Trace.beginSection("BiopReader.parse");
                new BiopReader(this).parse(downloadId, moduleId, payload);
            } catch (Throwable t) {
                Log.e(TAG, "BiopReader.parse failed dl=0x" + hex8(downloadId)
                        + " mod=0x" + hex4(moduleId), t);
            } finally {
                if (Build.VERSION.SDK_INT >= 18) Trace.endSection();

                // ★★★ barrier：排在所有 onFile() -> sinkHandler.saveImpl() 之後
                sinkHandler.post(() -> {
                    try {
                        tracker.saved.add(moduleId);

                        ModuleStateListener l = stateListener;
                        if (l != null) l.onModuleSaved(downloadId, moduleId, moduleVersion);

                        // 當 expected 全到齊，且這是最後一個在跑的 parse 任務，觸發整輪完成
                        if (tracker.allSaved() && tracker.inflight.get() == 1) {
                            // 這裡已在 sinkHandler 之後的序列化區段，磁碟寫入都完成了
                            long txid = currentTxidFor(downloadId);
                            String rel = sessionRelPath(txid);
                            String abs = sessionAbsPath(txid);

                            maybePublishAndGcPost(downloadId, /*forceFull*/ false);

                            ModuleStateListener l2 = stateListener;
                            if (l2 != null) l2.onCarouselAllSaved(downloadId, txid, rel, abs);
                            disableDsmcc3C();
                            Log.i(TAG, "chuck carousel 0x" + hex8(downloadId)
                                    + " all modules saved. tx=0x" + hex8(txid) + " path=" + rel);
                        }
                    } finally {
                        tracker.inflight.decrementAndGet();
                    }
                });

                long dt = SystemClock.elapsedRealtime() - start;
                double mb = payload.length / (1024.0 * 1024.0);
                double mbps = dt > 0 ? mb / (dt / 1000.0) : 0.0;
                Log.d(TAG, "chuck BiopReader.parse done: dl=0x" + hex8(downloadId)
                        + " mod=0x" + hex4(moduleId)
                        + " ver=" + moduleVersion
                        + " size=" + payload.length + "B"
                        + " cost=" + dt + "ms"
                        + " (" + String.valueOf(mb) + " MB, " + String.valueOf(mbps) + " MB/s)");
            }
        });

        // 這裡不是「檔案已落地」的信號；真正的準點在上面的 barrier 內
        maybePublishAndGcPost(downloadId, /*forceFull*/ false);
    }

    @Override
    public void onModuleProgress(int downloadId, int moduleId, int moduleVersion, int received, int total) {
        Log.d(TAG, serviceName + " progress dl=0x" + hex8(downloadId)
                + " mod=0x" + hex4(moduleId)
                + " v=" + moduleVersion + " " + received + "/" + total);
    }

    @Override
    public void onSessionReset(int downloadId, String reason) {
        Log.w(TAG, serviceName + " reset dl=0x" + hex8(downloadId) + ": " + reason);
        final int dlCopy = downloadId;
        sinkHandler.post(() -> {
            resetCarouselState(dlCopy);
            needsFullGc.add(dlCopy);
        });
    }

    private int guessModuleVersion(DdbMessage ddb) { return 0; }

    private int guessBlockSize(DiiMessage dii) {
        int fallback = 4096;
        if (dii.getModules().isEmpty()) return fallback;
        int bs = dii.getModules().get(0).blockSize;
        return bs > 0 ? bs : fallback;
    }

    // ===== BiopSink（序列化執行） =====
    @Override
    public void onServiceGateway(int carouselId, byte[] srgObjectKey, String mountHint) {
        // 輕量檢查 + 只做必要的拷貝；重活丟到 sinkHandler
        final int cid = carouselId;
        final byte[] keyCopy =
                (srgObjectKey != null && srgObjectKey.length > 0)
                        ? Arrays.copyOf(srgObjectKey, srgObjectKey.length)
                        : null;
        // 不需要 new String()；sanitize() 內會處理 null
        final String hintCopy = mountHint;
    
        sinkHandler.post(() -> onServiceGatewayImpl(cid, keyCopy, hintCopy));
    }
    // 單執行緒（sinkHandler）中的真正處理
    private void onServiceGatewayImpl(int carouselId, byte[] srgObjectKey, String mountHint) {
        // key 無效就放棄（避免 NPE 以及亂建索引）
        if (srgObjectKey == null || srgObjectKey.length == 0) {
            Log.w(TAG, "[SRG] ignore: null/empty objectKey (carousel=0x" + Integer.toHexString(carouselId) + ")");
            return;
        }
        final Map<String, NodeInfo> idx = keys(carouselId);

        // 以十六進位字串作為索引鍵；hex() 已保護 null/empty
        final String srgHex = hex(srgObjectKey);
        if (srgHex.isEmpty()) {
            Log.w(TAG, "[SRG] ignore: hex(objectKey) empty (carousel=0x" + Integer.toHexString(carouselId) + ")");
            return;
        }

        // 清理掛載提示字串（允許為空字串）
        final String cleanMount = sanitize(mountHint);

        // SRG 的 parentHex = null，表示根節點
        idx.put(srgHex, new NodeInfo(/*parentHex*/ null, /*name*/ cleanMount));

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "[SRG] carousel=0x" + Integer.toHexString(carouselId)
                    + " key=" + srgHex + " mount='" + cleanMount + "'");
        }

        // 可能讓快照達到可發布狀態（冪等）
        maybePublishAndGcImpl(carouselId, /*forceFull*/ false);
    }

    @Override
    public void onDirectory(int carouselId, byte[] directoryKey, Map<String, byte[]> nameToChildKey) {
        final int cid = carouselId;
        final byte[] dirCopy = (directoryKey != null) ? Arrays.copyOf(directoryKey, directoryKey.length) : null;
        final Map<String, byte[]> mapCopy = (nameToChildKey != null) ? new HashMap<>(nameToChildKey) : null;
        sinkHandler.post(() -> onDirectoryImpl(cid, dirCopy, mapCopy));
    }

    private void onDirectoryImpl(int carouselId, byte[] directoryKey, Map<String, byte[]> nameToChildKey) {
        final Map<String, NodeInfo> idx = keys(carouselId);
        final Map<String, byte[]> pend = pendings(carouselId);
        if (nameToChildKey == null || nameToChildKey.isEmpty()) return;
        
        final String dirHex = (directoryKey != null && directoryKey.length > 0) ? hex(directoryKey) : null;

        for (Map.Entry<String, byte[]> e : nameToChildKey.entrySet()) {
            final String childName = sanitize(e.getKey());
            final byte[] childKey  = e.getValue();
            if (childKey == null || childKey.length == 0) continue;

            final String childHex = hex(childKey);
            if (childHex.isEmpty()) continue;

            idx.put(childHex, new NodeInfo(dirHex, childName));

            // 若檔案先到，現在可以落地
            byte[] content = pend.remove(childHex);
            if (content != null) {
                String fullPath = tryBuildPathHex(carouselId, childHex);
                if (fullPath != null) saveImpl(carouselId, fullPath, content);
            }
        }

        maybePublishAndGcImpl(carouselId, /*forceFull*/ false);
    }

    @Override
    public void onFile(int carouselId, byte[] objectKey, byte[] content) {
        final int cid = carouselId;
        final byte[] keyCopy = (objectKey != null) ? Arrays.copyOf(objectKey, objectKey.length) : null;
        final byte[] dataCopy = (content != null) ? Arrays.copyOf(content, content.length) : null;
        sinkHandler.post(() -> onFileImpl(cid, keyCopy, dataCopy));
    }

    private void onFileImpl(int carouselId, byte[] objectKey, byte[] content) {
        if (objectKey == null || objectKey.length == 0) return;
        final Map<String, byte[]> pend = pendings(carouselId);

        final String keyHex = hex(objectKey);
        if (keyHex.isEmpty()) return;

        String fullPath = tryBuildPathHex(carouselId, keyHex);
        if (fullPath != null) {
            saveImpl(carouselId, fullPath, content);
            Log.d(TAG, "chuck [FILE] saved " + fullPath + " (" + (content != null ? content.length : 0) + "B)");
            maybePublishAndGcImpl(carouselId, /*forceFull*/ false);
        } else {
            if (pend.size() >= PENDING_LIMIT) {
                String any = pend.keySet().iterator().next();
                pend.remove(any);
            }
            pend.put(keyHex, content);
            Log.d(TAG, "chuck [FILE] pending key=" + keyHex + " size=" + (content != null ? content.length : 0));
        }
    }

    // ===== 發佈 + GC =====
    private void maybePublishAndGcPost(int carouselId, boolean forceFull) {
        final int cid = carouselId;
        sinkHandler.post(() -> maybePublishAndGcImpl(cid, forceFull));
    }

    private void maybePublishAndGcImpl(int carouselId, boolean forceFull) {
        Long txid = currentTxidByCarousel.get(carouselId);
        if (txid == null || txid == 0) return;

        Long publishedTx = publishedTxidByCarousel.get(carouselId);
        boolean needPublish = (publishedTx == null) || !publishedTx.equals(txid);

        // 計算 live set（相對於 baseDir），在 sessions/<txid>/<path> 之下
        Set<String> liveRelToBase = computeLiveSetRelToBase(carouselId, txid);
        if (liveRelToBase.isEmpty()) return; // 快照尚不完整

        File prevActive = snapshotMgr.resolveActiveRoot();

        if (needPublish) {
            snapshotMgr.publish(txid);
            publishedTxidByCarousel.put(carouselId, txid);
        }

        boolean doFull = forceFull || needsFullGc.remove(carouselId);

        snapshotMgr.gcPreviousSnapshot(prevActive, liveRelToBase, TOMBSTONE_GRACE_MS);
        snapshotMgr.cleanupExpiredTombstones();

        //Log.i(TAG, String.format("chuck publish+gc done (carousel=0x%X, txid=0x%08X, full=%s, live=%d)",
        //        carouselId, txid, doFull, liveRelToBase.size()));
    }

    // ★ 免去 hexToBytes：以 hex key 直接回溯 parentHex
    private Set<String> computeLiveSetRelToBase(int carouselId, long txid) {
        String txHex = hex8Lower(txid);
        String prefix = "sessions/" + txHex + "/";
   
        Set<String> live = new HashSet<>();
        Map<String, NodeInfo> idx = keyIndexByCarousel.get(carouselId);
        if (idx == null || idx.isEmpty()) return live;

        for (String hexKey : idx.keySet()) {
            String p = tryBuildPathHex(carouselId, hexKey);
            if (p != null && !p.isEmpty()) {
                live.add(prefix + p);
            }
        }
        return live;
    }

    // ===== path building helpers（只在 sinkHandler 執行緒被呼叫） =====
    private String tryBuildPathHex(int carouselId, String hexKey) {
        final Map<String, NodeInfo> idx = keys(carouselId);
        Deque<String> comps = new ArrayDeque<>();
        String curHex = hexKey;
        int guard = 0;

        while (curHex != null && guard++ < 1024) {
            NodeInfo ni = idx.get(curHex);
            if (ni == null) return null; // 某層還沒來
            String seg = sanitize(ni.name);
            if (!seg.isEmpty()) comps.addFirst(seg);
            curHex = ni.parentHex;
        }

        String built = String.join("/", comps);
        if (mountRoot != null && !mountRoot.isEmpty()) {
            return mountRoot + (built.isEmpty() ? "" : ("/" + built));
        } else {
            return built;
        }
    }

    private void resetCarouselState(int downloadId) {
        keyIndexByCarousel.remove(downloadId);
        pendingFilesByCarousel.remove(downloadId);
        publishedTxidByCarousel.remove(downloadId);
        // lastDiiVer 視需求保留；目前保留到下一輪
    }

    // ====== 寫檔（寫到目前 sessionRoot）— 只在 sinkHandler 執行緒呼叫 ======
    private void saveImpl(int carouselId, String relPath, byte[] data) {
        if (data == null || data.length == 0) {
            Log.w(TAG, "save: empty data for path=" + relPath);
            return;
        }
        if (relPath == null) relPath = "";

        Long txid = currentTxidByCarousel.get(carouselId);
        if (txid == null || txid == 0) {
            Log.w(TAG, "save: no current txid for carousel=0x" + Integer.toHexString(carouselId));
            return;
        }
        File sessionRoot = snapshotMgr.ensureSessionRoot(txid);

        // 路徑清理
        String[] parts = relPath.split("/");
        List<String> segs = new ArrayList<>();
        for (String p : parts) {
            if (p == null) continue;
            p = p.trim();
            if (p.isEmpty() || ".".equals(p) || "..".equals(p)) continue;
            int end = p.length();
            while (end > 0) {
                char c = p.charAt(end - 1);
                if (c == '\0' || Character.isISOControl(c)) end--;
                else break;
            }
            if (end > 0) segs.add(p.substring(0, end));
        }
        String cleanRel = String.join("/", segs);
        if (cleanRel.isEmpty()) cleanRel = "unnamed.bin";

        File out  = new File(sessionRoot, cleanRel);
        File dir = out.getParentFile();
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "save: mkdirs failed: " + dir);
            return;
        }

        // 原子寫：同目錄 tmp -> fsync -> rename；失敗回落 direct write
        File tmp = new File(dir != null ? dir : sessionRoot, out.getName() + ".tmp");
        FileOutputStream fos = null;
        boolean renamed = false;

        try {
            fos = new FileOutputStream(tmp, false);
            fos.write(data);
            fos.flush();
            try { fos.getFD().sync(); } catch (Throwable ignore) {}
            fos.close(); fos = null;

            if (tmp.renameTo(out)) {
                renamed = true;
            } else {
                if (out.exists() && !out.delete()) {
                    Log.w(TAG, "save: cannot delete old file before rename: " + out);
                }
                renamed = tmp.renameTo(out);
            }

            if (!renamed) {
                Log.w(TAG, "rename denied; fallback to direct write: " + out.getAbsolutePath());
                try (FileOutputStream direct = new FileOutputStream(out, false)) {
                    direct.write(data);
                    direct.flush();
                    try { direct.getFD().sync(); } catch (Throwable ignore) {}
                }
            }

            Log.d(TAG, "save OK: " + out.getAbsolutePath() + " (" + data.length + " bytes)");
        } catch (Throwable t) {
            Log.e(TAG, "save FAIL: " + out.getAbsolutePath() + " : " + t);
            try { if (fos != null) fos.close(); } catch (Exception ignore) {}
        } finally {
            if (tmp.exists() && !tmp.delete()) {
                Log.w(TAG, "save: tmp delete failed: " + tmp);
            }
        }
    }

    // ===== 小工具 =====
    private static String hex(byte[] b) {
        if (b == null || b.length == 0) return "";
        char[] out = new char[b.length * 2];
        final char[] HEX = "0123456789ABCDEF".toCharArray();
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xFF;
            out[i*2]   = HEX[v >>> 4];
            out[i*2+1] = HEX[v & 0x0F];
        }
        return new String(out);
            }

    private static String hex4(int v) {
        String s = Integer.toHexString(v & 0xFFFF).toUpperCase();
        int pad = 4 - s.length();
        if (pad > 0) s = "0000".substring(0, pad) + s;
        return s;
        }

    private static String hex8(int v) {
        String s = Integer.toHexString(v).toUpperCase();
        int pad = 8 - s.length();
        if (pad > 0) s = "00000000".substring(0, pad) + s;
        return s;
    }

    private static String hex8(long v) {
        String s = Long.toHexString(v).toUpperCase();
        int pad = 8 - s.length();
        if (pad > 0) s = "00000000".substring(0, pad) + s;
        return s;
    }

    public static String hex8Lower(long v) {
        String s = Long.toHexString(v);
        int pad = 8 - s.length();
        if (pad > 0) s = "00000000".substring(0, pad) + s;
        return s;
    }

    private static String sanitize(String s) {
        if (s == null) return "";
        int end = s.length();
        while (end > 0) {
            char c = s.charAt(end - 1);
            if (c == '\0' || Character.isISOControl(c)) end--;
            else break;
        }
        return s.substring(0, end).trim();
    }
}