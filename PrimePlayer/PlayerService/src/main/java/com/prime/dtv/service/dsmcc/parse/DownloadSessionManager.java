package com.prime.dtv.service.dsmcc.parse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadSessionManager {

    public interface Listener {
        void onModuleComplete(int downloadId, int moduleId, int moduleVersion, byte[] payload);
        default void onModuleProgress(int downloadId, int moduleId, int moduleVersion, int received, int total) {}
        default void onSessionReset(int downloadId, String reason) {}
    }

    private static final long STALE_MS = 60_000; // 逾時清理閾值，可調
    private final Listener listener;

    // key: downloadId -> (moduleKey -> assembler)
    private final Map<Integer, Map<String, ModuleAssembler>> sessions = new ConcurrentHashMap<>();
    // key 組合
    private static String key(int moduleId, int version) { return moduleId + ":" + version; }

    public DownloadSessionManager(Listener listener) {
        this.listener = listener;
    }

    /** DSI 到來時，可在這裡做 session 級初始化（選擇性） */
    public void onDsi(int downloadId) {
        sessions.computeIfAbsent(downloadId, k -> new ConcurrentHashMap<>());
    }

    /** DII 到來：建立/更新 modules 的 assembler */
    public void onDii(int downloadId, int blockSize, List<com.prime.dtv.service.dsmcc.parse.DiiMessage.ModuleInfo> modules) {
        Map<String, ModuleAssembler> map = sessions.computeIfAbsent(downloadId, k -> new ConcurrentHashMap<>());

        // 版本切換策略：同 moduleId 若 version 變動 → 移除舊的
        Set<String> keep = new HashSet<>();
        for (var m : modules) {
            String k = key(m.moduleId, m.moduleVersion);
            keep.add(k);
            map.compute(k, (kk, old) -> {
                if (old == null || old.moduleVersion != m.moduleVersion || old.moduleSize != m.moduleSize) {
                    return new ModuleAssembler(downloadId, m.moduleId, m.moduleVersion, m.moduleSize, m.blockSize > 0 ? m.blockSize : blockSize);
                }
                return old; // 沿用
            });
        }

        // 清掉不在 DII 列表裡的舊版本
        map.keySet().removeIf(k -> !keep.contains(k));
    }

    /** DDB 到來：餵進對應 assembler；完成則觸發 callback */
    public void onDdb(int downloadId, int moduleId, int moduleVersion, int blockNumber, byte[] data) {
        Map<String, ModuleAssembler> map = sessions.get(downloadId);
        if (map == null) return;

        // 注意：blockNumber 有的台 1-based，有的 0-based；如需可在外層轉為 0-based
        String k = key(moduleId, moduleVersion);
        ModuleAssembler a = map.get(k);
        if (a == null) return; // 還沒收到對應 DII

        if (a.addBlock(blockNumber, data)) {
            listener.onModuleProgress(downloadId, moduleId, moduleVersion, a.totalBlocks - a.missingBlocks(), a.totalBlocks);
            if (a.isComplete()) {
                byte[] payload = a.getAssembled();
                map.remove(k);
                listener.onModuleComplete(downloadId, moduleId, moduleVersion, payload);
            }
        }
    }

    /** 逾時清理（可定期在背景 thread 呼叫） */
    public void reapStale() {
        long now = System.currentTimeMillis();
        for (var e : sessions.entrySet()) {
            int downloadId = e.getKey();
            Map<String, ModuleAssembler> map = e.getValue();
            map.entrySet().removeIf(me -> {
                ModuleAssembler a = me.getValue();
                boolean stale = (now - a.getLastUpdateMs()) > STALE_MS;
                if (stale) {
                    listener.onSessionReset(downloadId, "stale module " + me.getKey());
                }
                return stale;
            });
        }
    }
}
