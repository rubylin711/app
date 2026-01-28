package com.prime.dtv.service.dsmcc.parse;

import android.util.Log;

/**
 * DSM-CC DownloadDataBlock (DDB)
 * 一個 Module 的一個 Block。通常出現在 table_id=0x3C（但仍以 messageId=0x1003 為準）。
 */
public class DdbMessage {
    private static final String TAG = "DDB";
    private static final boolean DEBUG = false; // 需要時設成 false 關掉

    private int downloadId;     // 由上層（DSI/DII）補入；DDB body 本身通常不帶
    private int transactionId;
    private int moduleId;
    private int moduleVersion;  // 多數台會帶，若無則為 0
    private int blockNumber;    // 注意：不同台可能 0-based 或 1-based
    private byte[] data;        // 單一 section 的 payload（需要上層 ModuleAssembler 組裝）

    // ===== Constructors =====
    public DdbMessage(int downloadId, int moduleId, int blockNumber, byte[] data) {
        this.downloadId = downloadId;
        this.moduleId = moduleId;
        this.blockNumber = blockNumber;
        this.data = data;
        this.moduleVersion = 0;
    }

    public DdbMessage(int downloadId, int moduleId, int moduleVersion, int blockNumber, byte[] data) {
        this.downloadId = downloadId;
        this.moduleId = moduleId;
        this.moduleVersion = moduleVersion;
        this.blockNumber = blockNumber;
        this.data = data;
    }

    // ===== Getters / Setters =====
    public int getDownloadId() { return downloadId; }
    public void setDownloadId(int downloadId) { this.downloadId = downloadId; }
    public int getTransactionId() { return transactionId; }
    public void setDTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getModuleId() { return moduleId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }

    public int getModuleVersion() { return moduleVersion; }
    public void setModuleVersion(int moduleVersion) { this.moduleVersion = moduleVersion; }

    public int getBlockNumber() { return blockNumber; }
    public void setBlockNumber(int blockNumber) { this.blockNumber = blockNumber; }

    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }

    @Override
    public String toString() {
        return "DDB{downloadId=" + downloadId +
                ", moduleId=0x" + Integer.toHexString(moduleId) +
                ", moduleVersion=" + moduleVersion +
                ", block=" + blockNumber +
                ", len=" + (data != null ? data.length : 0) + "}";
    }

    // =============================
    // Parser: 吃整個 section（從 table_id 開始，含 PSI header）
    // 以 messageId=0x1003 判斷 DDB，body 版型：
    //   moduleId(2) | moduleVersion(1) | reserved(1) | blockNumber(2) | blockData(...)
    // =============================
    public static DdbMessage parse(byte[] sec) {
        if (sec == null || sec.length < 20) {
            if (DEBUG) Log.w(TAG, "parse: buffer too short: " + (sec == null ? "null" : sec.length));
            return null;
        }

        final int tableId = sec[0] & 0xFF;
        if (tableId != 0x3B && tableId != 0x3C) {
            if (DEBUG) Log.d(TAG, String.format("parse: not DSM-CC (tableId=0x%02X)", tableId));
            return null;
        }

        // PSI: section_length（12 bits）
        int sectionLength = ((sec[1] & 0x0F) << 8) | (sec[2] & 0xFF);
        if (DEBUG) {
            int verByte = sec[5] & 0xFF;
            int version = (verByte >> 1) & 0x1F;
            int sectionNumber = sec[6] & 0xFF;
            int lastSectionNumber = sec[7] & 0xFF;
            Log.d(TAG, String.format("PSI: tid=0x%02X sec_len=%d ver=%d sec=%d/%d",
                    tableId, sectionLength, version, sectionNumber, lastSectionNumber));
        }

        // DSM-CC common header 從 offset=8 開始（private_section header 為 8 bytes）
        int hdr = 8;
        if (!rangeOk(sec, hdr, 12)) {
            if (DEBUG) Log.w(TAG, "parse: not enough bytes for DSM-CC header");
            return null;
        }

        int protocolDiscriminator = sec[hdr] & 0xFF;     // 常見 0x11
        int dsmccType            = sec[hdr + 1] & 0xFF;  // 常見 0x03
        int messageId     = u16(sec, hdr + 2);
        int downloadId        = u32(sec, hdr + 4);
        int adaptationLen = sec[hdr + 9] & 0xFF;
        int messageLen    = u16(sec, hdr + 10);

        if (DEBUG) {
            Log.d(TAG, String.format("DMS-CC hdr: pd=0x%02X type=0x%02X msgId=0x%04X tx=0x%08X adapt=%d msgLen=%d",
                    protocolDiscriminator, dsmccType, messageId, downloadId, adaptationLen, messageLen));
        }

        // 嚴格只收 DDB
        if (messageId != 0x1003) {
            if (DEBUG) Log.d(TAG, String.format("parse: not DDB (messageId=0x%04X)", messageId));
            return null;
        }

        int bodyOff = hdr + 12 + adaptationLen;
        if (bodyOff < 0 || bodyOff > sec.length) {
            if (DEBUG) Log.w(TAG, "parse: bad body offset " + bodyOff);
            return null;
        }

        // ---- DDB body ----
        // moduleId(2) | moduleVersion(1) | reserved(1) | blockNumber(2) | blockData(...)
        if (!rangeOk(sec, bodyOff, 2 + 1 + 1 + 2)) {
            if (DEBUG) Log.w(TAG, "parse: not enough for DDB fixed body");
            return null;
        }
        int p = bodyOff;

        int moduleId      = u16(sec, p); p += 2;
        int moduleVersion = sec[p++] & 0xFF;
        /* reserved */     p++;
        int blockNumber   = u16(sec, p); p += 2;

        int maxBodyEnd = Math.min(sec.length, bodyOff + messageLen);
        int dataLen = Math.max(0, maxBodyEnd - p);
        byte[] blockData = slice(sec, p, dataLen);

        if (DEBUG) {
            Log.d(TAG, String.format("chuck DDB body: mod=0x%04X ver=%d block=%d dataLen=%d (p=%d, maxEnd=%d)",
                    moduleId, moduleVersion, blockNumber, dataLen, p, maxBodyEnd));
        }

        DdbMessage d = new DdbMessage(downloadId, moduleId, moduleVersion, blockNumber, blockData);
        if (DEBUG) Log.d(TAG, "DDB parsed -> " + d);
        return d;
    }

    // ===== Helpers =====
    private static boolean rangeOk(byte[] b, int off, int len) {
        return b != null && off >= 0 && len >= 0 && off + len <= b.length;
    }
    private static int u16(byte[] b, int p) {
        return ((b[p] & 0xFF) << 8) | (b[p + 1] & 0xFF);
    }
    private static int u32(byte[] b, int p) {
        return ((b[p] & 0xFF) << 24)
             | ((b[p + 1] & 0xFF) << 16)
             | ((b[p + 2] & 0xFF) << 8)
             |  (b[p + 3] & 0xFF);
    }
    private static byte[] slice(byte[] b, int p, int n) {
        if (b == null || n <= 0 || p < 0 || p >= b.length) return new byte[0];
        int nn = Math.min(n, b.length - p);
        byte[] o = new byte[nn];
        System.arraycopy(b, p, o, 0, nn);
        return o;
    }
}
