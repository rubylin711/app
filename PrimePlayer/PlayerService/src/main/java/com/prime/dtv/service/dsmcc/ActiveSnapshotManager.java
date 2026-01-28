package com.prime.dtv.service.dsmcc;

import android.util.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public final class ActiveSnapshotManager {
    private static final String TAG = "ActiveSnapshotMgr";

    private final File baseDir;               // /data/vendor/dtvdata/<SERVICE>
    private final File sessionsDir;           // /data/vendor/dtvdata/<SERVICE>/sessions
    private final File tombstoneDir;          // /data/vendor/dtvdata/<SERVICE>/tombstone
    private final File activePtr;             // /data/vendor/dtvdata/<SERVICE>/active.txt
    private final ReentrantLock lock = new ReentrantLock();

    // tombstone metadata：filename 相對於 baseDir 的相對路徑, 到期時間（毫秒）
    private final File tombstoneIndex;

    public ActiveSnapshotManager(String basePath) {
        this.baseDir = new File(basePath);
        this.sessionsDir = new File(baseDir, "sessions");
        this.tombstoneDir = new File(baseDir, "tombstone");
        this.activePtr = new File(baseDir, "active.txt");
        this.tombstoneIndex = new File(tombstoneDir, "index.txt");

        // 目錄確保存在（權限交給 sepolicy / init.rc，這裡只做 mkdirs）
        if (!sessionsDir.exists()) sessionsDir.mkdirs();
        if (!tombstoneDir.exists()) tombstoneDir.mkdirs();
    }

    /** 建立/取得本次 session 根目錄（例如 txid=0x8F830000 → sessions/8f830000） */
    public File ensureSessionRoot(long txid) {
        String hex = String.format("%08x", txid);
        File root = new File(sessionsDir, hex);
        if (!root.exists()) root.mkdirs();
        return root;
    }

    /** 原子發佈新 session：active.txt.tmp → rename 覆蓋 active.txt */
    public void publish(long txid) {
        String rel = "sessions/" + String.format("%08x", txid);
        lock.lock();
        try {
            atomicWriteString(activePtr, rel + "\n");
            Log.i(TAG, "published: " + rel);
        } finally {
            lock.unlock();
        }
    }

    /** 取目前 active 的 session 目錄（可能為 null） */
    public File resolveActiveRoot() {
        lock.lock();
        try {
            String rel = readTrim(activePtr);
            if (rel == null || rel.isEmpty()) return null;
            File f = new File(baseDir, rel);
            return f.isDirectory() ? f : null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 以「新快照 live set」做 GC：
     * - 掃描上一版 active（或指定的 previousRoot）下所有檔案
     * - 不在 liveSet 的搬到 tombstone/，並記錄到期時間
     */
    public void gcPreviousSnapshot(File previousRoot, Set<String> liveSetRelPaths, long ttlMillis) {
        if (previousRoot == null || !previousRoot.isDirectory()) return;
        lock.lock();
        try {
            List<File> stale = listAllFiles(previousRoot);
            long expireAt = System.currentTimeMillis() + ttlMillis;

            for (File f : stale) {
                String rel = relativize(baseDir, f);
                if (rel == null || rel.startsWith("tombstone")) continue;
                if (liveSetRelPaths.contains(rel)) continue; // 還在 live set → 保留

                File dst = new File(tombstoneDir, rel.replace('/', '_')); // 簡單處理檔名衝突
                dst.getParentFile().mkdirs();
                if (f.renameTo(dst)) {
                    appendTombstone(rel, expireAt);
                } else {
                    // 退而求其次：複製→刪除，避免 rename 被 policy 擋（非原子但可用）
                    copyFile(f, dst);
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                    appendTombstone(rel, expireAt);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /** 刪除到期的 tombstone 檔案（在 service 生命週期或定期調用） */
    public void cleanupExpiredTombstones() {
        lock.lock();
        try {
            Map<String, Long> idx = readTombstoneIndex();
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<String, Long>> it = idx.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> e = it.next();
                if (e.getValue() != null && now >= e.getValue()) {
                    File f = new File(tombstoneDir, e.getKey().replace('/', '_'));
                    if (f.exists() && !f.delete()) {
                        Log.w(TAG, "tombstone delete failed: " + f);
                        continue;
                    }
                    it.remove();
                }
            }
            writeTombstoneIndex(idx);
        } finally {
            lock.unlock();
        }
    }

    // ---------- 小工具 ----------

    private static void atomicWriteString(File target, String content) {
        File tmp = new File(target.getParentFile(), target.getName() + ".tmp");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tmp, false);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            try { fos.getFD().sync(); } catch (Throwable ignore) {}

            if (!tmp.renameTo(target)) {
                if (target.exists() && !target.delete()) {
                    Log.w(TAG, "cannot delete old pointer: " + target);
                }
                if (!tmp.renameTo(target)) {
                    // fallback：直接覆寫
                    try (FileOutputStream direct = new FileOutputStream(target, false)) {
                        direct.write(content.getBytes(StandardCharsets.UTF_8));
                        direct.flush();
                        try { direct.getFD().sync(); } catch (Throwable ignore) {}
                    }
                    //noinspection ResultOfMethodCallIgnored
                    tmp.delete();
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "atomic write failed: " + target + " : " + t);
        } finally {
            try { if (fos != null) fos.close(); } catch (Exception ignore) {}
        }
    }

    private static String readTrim(File f) {
        try {
            byte[] b = java.nio.file.Files.readAllBytes(f.toPath());
            return new String(b, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            return null;
        }
    }

    private static List<File> listAllFiles(File root) {
        List<File> out = new ArrayList<>();
        Deque<File> dq = new ArrayDeque<>();
        dq.add(root);
        while (!dq.isEmpty()) {
            File d = dq.removeFirst();
            File[] arr = d.listFiles();
            if (arr == null) continue;
            for (File x : arr) {
                if (x.isDirectory()) dq.add(x);
                else out.add(x);
            }
        }
        return out;
    }

    private static String relativize(File base, File f) {
        try {
            String b = base.getCanonicalPath();
            String p = f.getCanonicalPath();
            if (!p.startsWith(b)) return null;
            String rel = p.substring(b.length());
            if (rel.startsWith(File.separator)) rel = rel.substring(1);
            return rel.replace(File.separatorChar, '/');
        } catch (IOException e) {
            return null;
        }
    }

    private void appendTombstone(String rel, long expireAt) {
        try (FileWriter fw = new FileWriter(tombstoneIndex, true)) {
            fw.write(rel + "|" + expireAt + "\n");
        } catch (IOException e) {
            Log.w(TAG, "append tombstone failed: " + e.getMessage());
        }
    }

    private Map<String, Long> readTombstoneIndex() {
        Map<String, Long> m = new LinkedHashMap<>();
        if (!tombstoneIndex.exists()) return m;
        try (BufferedReader br = new BufferedReader(new FileReader(tombstoneIndex))) {
            String line;
            while ((line = br.readLine()) != null) {
                int i = line.lastIndexOf('|');
                if (i <= 0) continue;
                String rel = line.substring(0, i);
                long ts;
                try { ts = Long.parseLong(line.substring(i + 1)); }
                catch (NumberFormatException e) { continue; }
                m.put(rel, ts);
            }
        } catch (IOException e) {
            Log.w(TAG, "read tombstone index failed: " + e.getMessage());
        }
        return m;
    }

    private void writeTombstoneIndex(Map<String, Long> idx) {
        try (FileWriter fw = new FileWriter(tombstoneIndex, false)) {
            for (Map.Entry<String, Long> e : idx.entrySet()) {
                fw.write(e.getKey() + "|" + e.getValue() + "\n");
            }
        } catch (IOException e) {
            Log.w(TAG, "write tombstone index failed: " + e.getMessage());
        }
    }

    /** 小工具：把一個檔複製到另一個檔（權限/Context 交給 sepolicy） */
    private static void copyFile(File src, File dst) {
        dst.getParentFile().mkdirs();
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst, false)) {
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            out.flush();
            try { ((FileOutputStream) out).getFD().sync(); } catch (Throwable ignore) {}
        } catch (IOException e) {
            Log.e(TAG, "copyFile failed: " + e);
        }
    }
}
