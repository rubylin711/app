package com.prime.dtv.service.Table;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process; 

import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Demux.DemuxSectionCallback;
import com.prime.dtv.service.dsmcc.parse.DdbMessage;
import com.prime.dtv.service.dsmcc.parse.DiiMessage;
import com.prime.dtv.service.dsmcc.parse.DsiMessage;

import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DSM-CC（Demux + Table 風格）
 * - 3B: 仍由 Table 機制開啟 filter
 * - 3C: 改由 ThreeCFilterController 動態開關
 * - HandlerThread 解析，不在 callback 做重活
 */
public class DsmccTable extends Table {
    private static final String TAG = "DsmccTable";

    // 兩個 table_id
    public static final byte TID_3B = (byte) 0x3B; // DSI/DII/DDB 都在 0x3B
    public static final byte TID_3C = (byte) 0x3C; // 私有段（若台端有用）

    // 建議的 filter buffer（可依實況調整；DDB 流量大）
    private static final long BUF_3B = 256 * 1024;
    private static final long BUF_3C = 256 * 1024;

    private static final int WHAT_SECTION = 99;

    // DSM-CC carousel PID（從 PMT 取出）
    private final int mCarouselPid;
    private final int mTunerId;

    // ===== 狀態 =====
    private volatile int  currentDownloadId = -1;  // 以 DII 的 download_id 切輪次
    private volatile int currentTxId       = -1; // TAP/DII transaction_id (u32)
    private volatile int  currentDiiVer     = -1;
    private volatile int  dsiTransId        = -1;
    private volatile int  dsiVersion        = -1;
    
    
    // DII PSI 版本快取（每個 tx 對應上次看到的 PSI version）
    private final ConcurrentHashMap<Integer, Integer> lastDiiVersionByTx = new ConcurrentHashMap<>();
    // 已見過 DSI 的 Tx
    private final ConcurrentHashMap<Integer, Boolean> dsiSeenByTx = new ConcurrentHashMap<>();
    // tx → downloadId 對應
    private final ConcurrentHashMap<Integer, Integer> tx2dl = new ConcurrentHashMap<>();
    // (dl,mod,ver) 啟用 DDB 去重的位圖
    private final ConcurrentHashMap<Long, ModuleBitmap> ddbByMod = new ConcurrentHashMap<>();

    // ====== DII catalog: 記住每個 (dl,mod,ver) 的大小，供 DDB 聚合 ======
    private static final class DiiCatalog {
        final int downloadId, moduleId, moduleVersion, moduleSize, blockSize;
        DiiCatalog(int dl, int mid, int ver, int msize, int bsize) {
            this.downloadId = dl; this.moduleId = mid; this.moduleVersion = ver;
            this.moduleSize = msize; this.blockSize = bsize;
        }
    }
    private final ConcurrentHashMap<Long, DiiCatalog> diiCatalog = new ConcurrentHashMap<>();

    // ====== DDB 多 section 組裝 ======
    private static final long DDB_ASSEMBLY_TIMEOUT_MS = 3000;

    private static final class DdbAssembly {
        final int  dl, moduleId, moduleVersion, blockNo;
        final BitSet got = new BitSet();
        int last = -1;
        int expectedLen = -1;
        final java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream(8192);
        byte[] firstSection;
        long lastTs = System.currentTimeMillis();
        DdbAssembly( int dl, int mid, int ver, int bn) {
             this.dl = dl; this.moduleId = mid; this.moduleVersion = ver; this.blockNo = bn;
        }
        void touch() { lastTs = System.currentTimeMillis(); }
        boolean expired(long now) { return now - lastTs > DDB_ASSEMBLY_TIMEOUT_MS; }
        boolean bitmapComplete() { return last >= 0 && got.cardinality() == (last + 1); }
        boolean sizeComplete() { return expectedLen >= 0 && buf.size() >= expectedLen; }
    }
    private final ConcurrentHashMap<String, DdbAssembly> ddbAssemblies = new ConcurrentHashMap<>();

    /** 解析工作執行緒 */
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private final AtomicInteger pending = new AtomicInteger(0);
    private final AtomicLong seqGen = new AtomicLong(0);

    // 回拋介面
    public interface Listener {
        void onDsi(DsiMessage dsi);
        void onDii(DiiMessage dii);
        void onDdb(DdbMessage ddb);
        default void onPrivate3C(byte[] raw) {}
        default void onObservedOther(byte[] raw, int len, int tableId) {}
        default void onParseError(Throwable t) { LogUtils.e(TAG + " parse error: " + t.getMessage(), t); }
    }
    private final Listener mListener;

    // 3C 控制器 + 保存 3C 的 DemuxChannel 參數
    private final ThreeCFilterController threeC;
    private Demux.DemuxChannel mCh3C;
    // 3C idle 自動關閉時間（ms），想關閉就設 0
    private static final long THREE_C_IDLE_MS = 30_000;

    public DsmccTable(int tunerID, int carouselPid, Listener listener) {
        super(tunerID, TID_3B);
        this.mCarouselPid = carouselPid;
        this.mListener = listener;
        this.mTunerId = tunerID;

        threeC = new ThreeCFilterController(mTunerId, THREE_C_IDLE_MS);

        mHandlerThread = new HandlerThread(getClass().getName(), Process.THREAD_PRIORITY_DISPLAY);
        mHandlerThread.start();
        try {
            Process.setThreadPriority(mHandlerThread.getThreadId(), Process.THREAD_PRIORITY_DISPLAY);
        } catch (Throwable ignore) {}

        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override public void handleMessage(Message msg) {
                if (msg.what != WHAT_SECTION) return;

                byte[] sec = (byte[]) msg.obj;
                int len = msg.arg1;
                int seq = msg.arg2;
                if (sec == null || len <= 0) return;
                pending.decrementAndGet();

                final int tableId   = sec[0] & 0xFF;
                if (tableId != (TID_3B & 0xFF) && tableId != (TID_3C & 0xFF)) return;

                try {
                    final int version   = (sec[5] >> 1) & 0x1F;
                    final int sectionNo = sec[6] & 0xFF;
                    final int lastSecNo = sec[7] & 0xFF;
                    int msgId = ((sec[10] & 0xFF) << 8) | (sec[11] & 0xFF);
                    int transOrDl = (int)(((sec[12] & 0xFFL) << 24) | ((sec[13] & 0xFFL) << 16)
                            | ((sec[14] & 0xFFL) << 8)  |  (sec[15] & 0xFFL));

                    if (pending.get() > 50) {
                        LogUtils.d("DSMCC handle len=" + len + " seq=" + seq +
                                " tableId=" + tableId + " version=" + version +
                                " sectionNo=" + sectionNo + " msgId=" + msgId +
                                " trans/dl=" + transOrDl + " inQ=" + pending.get());
                    }

                    if (msgId == 0x1006) { // DSI
                        boolean changed = !(transOrDl == dsiTransId && version == dsiVersion);
                        DsiMessage dsi = DsiMessage.parse(sec);
                        if (dsi != null) {
                            final int tapTx = dsi.tapTransactionId;
                            dsiSeenByTx.put(tapTx, Boolean.TRUE);

                            if (currentTxId != tapTx) {
                                LogUtils.d(TAG + " DSI Tx change: " + currentTxId + " -> " + tapTx + " (reset dedup)");
                                ddbByMod.clear();
                                diiCatalog.clear();
                                ddbAssemblies.clear();
                                currentTxId = tapTx;
                                currentDiiVer = -1;
                            }

                            // ★ session 變更就先關 3C，等 DII 來再開
                            if (changed) {
                                threeC.disable();
                            }

                            mListener.onDsi(dsi);
                            dsiTransId = transOrDl;
                            dsiVersion = version;
                        }
                        return;
                    } else if (msgId == 0x1002) { // DII
                        final int tx = transOrDl;
                        final boolean seenDsiForTx = Boolean.TRUE.equals(dsiSeenByTx.get(tx));
                        if (!seenDsiForTx) return;
                        if (currentDiiVer != -1 && currentTxId != -1 &&
                                currentTxId == transOrDl && version == currentDiiVer)
                            return;

                        DiiMessage dii = DiiMessage.parse(sec);
                        if (dii == null) return;

                        final int dl = dii.getDownloadId();
                        tx2dl.put(tx, dl);

                        boolean diiVersionChanged = (currentDiiVer == -1) || (currentDiiVer != version);
                        boolean txChanged = (currentTxId != -1) && (currentTxId != tx);
                        if (txChanged || diiVersionChanged || currentDownloadId != dl) {
                            LogUtils.d(TAG + " DII switch to dl=" + dl +
                                    (txChanged ? " [tx]" : "") +
                                    (diiVersionChanged ? " [ver]" : "") +
                                    (currentDownloadId != dl ? " [dl]" : ""));
                            clearAllForDownloadSwitch(dl);
                            currentTxId = tx;
                            currentDiiVer = version;
                            dsiSeenByTx.put(tx, Boolean.TRUE);
                        }

                        for (DiiMessage.ModuleInfo mi : dii.getModules()) {
                            long mkey = modKey(dl, mi.moduleId, mi.moduleVersion);
                            ddbByMod.compute(mkey, (k, v) -> {
                                if (v == null) return new ModuleBitmap(mi.moduleId, mi.moduleVersion, dl);
                                if (v.moduleVersion != mi.moduleVersion) {
                                    v.blocks.clear();
                                    v.started = false;
                                    v.moduleVersion = mi.moduleVersion;
                                }
                                v.downloadId = dl;
                                return v;
                            });
                            int bsize = mi.blockSize > 0 ? mi.blockSize : dii.getGlobalBlockSize();
                            int msize = mi.moduleSize;
                            diiCatalog.put(mkey, new DiiCatalog(dl, mi.moduleId, mi.moduleVersion, msize, bsize));
                        }

                        currentDownloadId = dl;

                        // ★★★ DII 來了 -> 開 3C
                        if (mCh3C != null) {
                            try { threeC.enable(mCh3C); } catch (Exception e) {
                                LogUtils.e("enable 3C failed: " + e);
                            }
                        }

                        mListener.onDii(dii);
                        return;
                    } else if (msgId == 0x1003) { // DDB
                        final int dlFromHeader = transOrDl;
                        if (currentDownloadId != -1 && dlFromHeader != currentDownloadId) {
                            return;
                        }
                        DdbMessage ddb = DdbMessage.parse(sec);
                        if (ddb == null) return;

                        final int dl = ddb.getDownloadId();
                        final int moduleId = ddb.getModuleId();
                        final int mver = ddb.getModuleVersion();
                        final int blockNo = ddb.getBlockNumber();

                        byte[] ddbChunk = extractDdbPayload(sec);
                        if (ddbChunk == null) return;

                        String aKey = ddbKey(dl, moduleId, mver, blockNo);
                        DdbAssembly as = ddbAssemblies.computeIfAbsent(aKey,
                                k -> new DdbAssembly(dl, moduleId, mver, blockNo));
                        if (as.firstSection == null) {
                            as.firstSection = java.util.Arrays.copyOf(sec, sec.length);
                        }
                        if (as.last < 0) as.last = lastSecNo;
                        if (as.expectedLen < 0) as.expectedLen = expectedBlockLen(dl, moduleId, mver, blockNo);

                        if (!as.got.get(sectionNo)) {
                            try { as.buf.write(ddbChunk); } catch (Exception ignore) {}
                            as.got.set(sectionNo);
                            as.touch();
                        }

                        long nowTs = System.currentTimeMillis();
                        threeC.touch(); 
                        ddbAssemblies.entrySet().removeIf(e -> e.getValue().expired(nowTs));

                        boolean assembled = as.bitmapComplete() || as.sizeComplete();
                        if (!assembled) return;

                        byte[] merged = as.buf.toByteArray();
                        byte[] mergedSec = synthesizeSingleDdbSection(as.firstSection, merged);
                        ddbAssemblies.remove(aKey);

                        DdbMessage mergedDdb = DdbMessage.parse(mergedSec);
                        if (mergedDdb == null) return;

                        final long mkey = modKey(dl, moduleId, mver);
                        ModuleBitmap mb = ddbByMod.get(mkey);
                        if (mb == null) return;

                        final int blockNo2 = mergedDdb.getBlockNumber();
                        synchronized (mb) {
                            if (!mb.started && blockNo2 == 0) {
                                mb.blocks.clear();
                                mb.started = true;
                                mb.blocks.set(0);
                            } else if (mb.started) {
                                if (mb.blocks.get(blockNo2)) {
                                    return; // dup
                                }
                                mb.blocks.set(blockNo2);
                            }
                        }
                      
                        if (currentDownloadId != dl && dl >= 0) {
                            LogUtils.d(TAG + " DDB sees new dl: " + currentDownloadId + " -> " + dl);
                            clearAllForDownloadSwitch(dl);
                        }

                        mListener.onDdb(mergedDdb);
                        return;
                    }

                    // 0x3C 其他 payload
                    if (tableId == (TID_3C & 0xFF)) {
                        threeC.touch(); // ★ 有 3C 活動
                        mListener.onPrivate3C(sec);
                        return;
                    }
                            
                    // 保險
                        mListener.onObservedOther(sec, len, tableId);

                } catch (Throwable t) {
                    LogUtils.e(TAG + " parse error: " + t.getMessage(), t);
                    mListener.onParseError(t);
                } finally {
                    // 3C idle 自動關閉
                    threeC.maybeAutoClose();
                }
            }
        };

        // 啟動 Table 流程（與 EIT 一樣）
        this.run(this.getClass().getName());
    }

    private static long modKey(int downloadId, int moduleId, int moduleVersion) {
        return ((long)(downloadId & 0xFFFF) << 24) | ((long)(moduleId & 0xFFFF) << 8) | (long)(moduleVersion & 0xFF);
    }
    
    private void clearAllForDownloadSwitch(int newDownloadId) {
        ddbByMod.clear();
        diiCatalog.clear();
        ddbAssemblies.clear();
        currentDownloadId = newDownloadId;
        currentDiiVer = -1;
    }

    static final class ModuleBitmap {
        final int moduleId;
        int moduleVersion;
        int downloadId;
        final BitSet blocks = new BitSet();
        boolean started = false;
        ModuleBitmap(int mid, int mver, int dl) {
            this.moduleId = mid; this.moduleVersion = mver; this.downloadId = dl;
    }
    }

    @Override
    protected List<Demux.DemuxChannel> getDemuxChannels() {
        List<Demux.DemuxChannel> list = new ArrayList<>();

        // --- 3B：交給 Table 機制 ---
        {
            Demux.DemuxFilter f3B = new Demux.DemuxFilter();
            f3B.setFilterData(new byte[]{ TID_3B });
            f3B.setFilterMask(new byte[]{ (byte) 0xFF });

            Demux.DemuxChannel ch3B = new Demux.DemuxChannel();
            ch3B.setPid(mCarouselPid);
            ch3B.setTableId(TID_3B);
            ch3B.setCrcEnable(true);
            ch3B.setRepeat(true);
            ch3B.setFilter(f3B);
            ch3B.setFilterBufferSize(BUF_3B);
            ch3B.setTimeOut(0);
            ch3B.setDemuxSectionCallback(
                    new DemuxSectionCallback(getFilterCompleteCallback(), TID_3B));
            list.add(ch3B);
        }

        // --- 3C：不加入 list，只存起參數，等 DII 來時由 ThreeCFilterController 動態 open ---
        {
            Demux.DemuxFilter f3C = new Demux.DemuxFilter();
            f3C.setFilterData(new byte[]{ TID_3C });
            f3C.setFilterMask(new byte[]{ (byte) 0xFF });

            Demux.DemuxChannel ch3C = new Demux.DemuxChannel();
            ch3C.setPid(mCarouselPid);
            ch3C.setTableId(TID_3C);
            ch3C.setCrcEnable(true);
            ch3C.setRepeat(true);
            ch3C.setFilter(f3C);
            ch3C.setFilterBufferSize(BUF_3C);
            ch3C.setTimeOut(0);
            ch3C.setDemuxSectionCallback(
                    new DemuxSectionCallback(getFilterCompleteCallback(), TID_3C));

            this.mCh3C = ch3C; // 保存參數
        }

        return list;
    }

    @Override
    protected TableData parsing(byte[] data, int len) {
        byte[] copy = java.util.Arrays.copyOf(data, len);
        int seq = (int) (seqGen.incrementAndGet() & 0x7FFFFFFF);
        Message m = Message.obtain(mHandler, WHAT_SECTION, len, seq, copy);
        boolean ok = mHandler.sendMessage(m);
        if (ok) pending.incrementAndGet();
        return null;
    }

    @Override
    protected void finish_table() {
        // DSM-CC 長期監聽，不需特別收尾
    }

    public void cleanup() {
        // 先把 3C 關掉
        threeC.disable();

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
            mHandlerThread = null;
        }
        ddbByMod.clear();
        tx2dl.clear();
        dsiSeenByTx.clear();
        diiCatalog.clear();
        ddbAssemblies.clear();
        currentDownloadId = -1;
        currentTxId = -1;
        currentDiiVer = -1;
    }

    public static boolean isDsmccTid(byte tid) {
        return tid == TID_3B || tid == TID_3C;
    }

    // ====== DDB 輔助 ======

    private static String ddbKey(int dl, int mid, int ver, int bn){
        return  dl + ":" + mid + ":" + ver + ":" + bn;
    }

    private int expectedBlockLen(int dl, int mid, int ver, int blockNo) {
        long k = modKey(dl, mid, ver);
        DiiCatalog c = diiCatalog.get(k);
        if (c == null) return -1;
        int blockSize = c.blockSize;
        int moduleSize = c.moduleSize;
        if (blockSize <= 0 || moduleSize <= 0) return -1;
        long start = (long) blockNo * (long) blockSize;
        long remain = Math.max(0L, ((long) moduleSize) - start);
        return (int) Math.min(remain, (long) blockSize);
    }

    private static byte[] extractDdbPayload(byte[] sec) {
        int sectionLength = ((sec[1] & 0x0F) << 8) | (sec[2] & 0xFF);
        int payloadLen = sectionLength - 9;
        int payloadOff = 8;
        if (payloadLen <= 0 || payloadOff + payloadLen > sec.length) return null;

        int adaptLen = sec[payloadOff + 9] & 0xFF;
        int dsmccBodyOff = payloadOff + 12 + adaptLen;

        int ddbDataOff = dsmccBodyOff + 6; // moduleId2 + moduleVer1 + reserved1 + blockNo2
        int ddbDataLen = payloadOff + payloadLen - ddbDataOff;
        if (ddbDataOff < 0 || ddbDataLen < 0 || ddbDataOff + ddbDataLen > sec.length) return null;

        return java.util.Arrays.copyOfRange(sec, ddbDataOff, ddbDataOff + ddbDataLen);
    }

    private static byte[] synthesizeSingleDdbSection(byte[] firstSec, byte[] mergedBlock) {
        if (firstSec == null) return null;

        int sectionLength = ((firstSec[1] & 0x0F) << 8) | (firstSec[2] & 0xFF);
        int payloadLen = sectionLength - 9;
        int payloadOff = 8;

        int adaptLen = firstSec[payloadOff + 9] & 0xFF;
        int dsmccBodyOff = payloadOff + 12 + adaptLen;

        int ddbHdrOff = dsmccBodyOff;
        int ddbHdrLen = 6;

        int newPayloadLen = (12 + adaptLen) + ddbHdrLen + mergedBlock.length;
        int newSectionLen = newPayloadLen + 9;

        byte[] out = new byte[8 + newPayloadLen + 4];
        System.arraycopy(firstSec, 0, out, 0, 8);
        out[1] = (byte) ((out[1] & 0xF0) | ((newSectionLen >> 8) & 0x0F));
        out[2] = (byte) (newSectionLen & 0xFF);
        out[6] = 0;
        out[7] = 0;

        System.arraycopy(firstSec, payloadOff, out, payloadOff, (12 + adaptLen) + ddbHdrLen);
        System.arraycopy(mergedBlock, 0, out, payloadOff + (12 + adaptLen) + ddbHdrLen, mergedBlock.length);
        return out;
    }

    /** 對外提供關閉 0x3C 私有段（DDB）filter 的方法 */
    public void disableDsmcc3C() {
        threeC.disable();
    }

    /** 若之後需要也可手動開啟 3C */
    public void enableDsmcc3C() {
        if (mCh3C != null) {
            try { threeC.enable(mCh3C); } catch (Exception e) {
                LogUtils.e("enable 3C failed: " + e);
            }
        }
    }
}
   