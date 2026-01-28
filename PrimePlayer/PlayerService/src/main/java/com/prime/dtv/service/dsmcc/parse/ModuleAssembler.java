package com.prime.dtv.service.dsmcc.parse;

import java.util.BitSet;

public class ModuleAssembler {
    public final int downloadId;
    public final int moduleId;
    public final int moduleVersion;
    public final int moduleSize;
    public final int blockSize;
    public final int totalBlocks;

    private final byte[] buffer;    // 完整模組的承載
    private final BitSet received;  // 收到哪些 block
    private int receivedCount = 0;
    private long lastUpdateMs = System.currentTimeMillis();

    public ModuleAssembler(int downloadId, int moduleId, int moduleVersion, int moduleSize, int blockSize) {
        this.downloadId = downloadId;
        this.moduleId = moduleId;
        this.moduleVersion = moduleVersion;
        this.moduleSize = moduleSize;
        this.blockSize = Math.max(1, blockSize);
        this.totalBlocks = (int) Math.ceil((double) moduleSize / this.blockSize);
        this.buffer = new byte[moduleSize];
        this.received = new BitSet(totalBlocks);
    }

    /** 回傳 true 表示這塊是新收到並寫入成功；false 表示重複或越界 */
    public synchronized boolean addBlock(int blockNumber, byte[] data) {
        int bn = Math.max(0, blockNumber); // 有些台從 0，有些從 1；外層可先標準化
        if (bn >= totalBlocks || data == null || data.length == 0) return false;

        if (received.get(bn)) return false;

        int offset = bn * blockSize;
        int copyLen = Math.min(data.length, moduleSize - offset);
        if (copyLen <= 0) return false;

        System.arraycopy(data, 0, buffer, offset, copyLen);
        received.set(bn);
        receivedCount++;
        lastUpdateMs = System.currentTimeMillis();
        return true;
    }

    public synchronized boolean isComplete() {
        return receivedCount >= totalBlocks;
    }

    public synchronized byte[] getAssembled() {
        if (!isComplete()) return null;
        return buffer.clone();
    }

    public synchronized int missingBlocks() {
        return totalBlocks - receivedCount;
    }

    public synchronized long getLastUpdateMs() {
        return lastUpdateMs;
    }
}
