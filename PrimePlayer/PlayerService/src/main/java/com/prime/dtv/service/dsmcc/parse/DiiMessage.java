package com.prime.dtv.service.dsmcc.parse;

import java.util.ArrayList;
import java.util.List;

public class DiiMessage {

    public static class ModuleInfo {
        public int moduleId;
        public int moduleSize;
        public int moduleVersion;
        public int blockSize;

        // 新增：從 taps 取得
        public int  assocTag = -1;          // u16
        public int tapTransactionId = -1;  // u32

        public ModuleInfo(int moduleId, int moduleSize, int moduleVersion, int blockSize) {
            this.moduleId = moduleId;
            this.moduleSize = moduleSize;
            this.moduleVersion = moduleVersion;
            this.blockSize = blockSize;
        }

        @Override
        public String toString() {
            return "ModuleInfo{id=" + moduleId +
                    ", size=" + moduleSize +
                    ", ver=" + moduleVersion +
                    ", block=" + blockSize +
                    ", assocTag=0x" + Integer.toHexString(assocTag & 0xFFFF) +
                    ", tapTxId=0x" + Integer.toHexString(tapTransactionId ) +
                    "}";
        }
    }

    // ---- existing fields ----
    private int downloadId;
    private int transactionId;
    private final List<ModuleInfo> modules = new ArrayList<>();

    // ---- added for logging / version tracking ----
    private int tableId = 0x3B;           // should be 0x3B
    private int sectionLength;
    private int versionNumber = -1;       // PSI version_number (0..31)
    private int sectionNumber;
    private int lastSectionNumber;
    private int messageId;                // DSM-CC message_id (DII=0x1002)
    private int globalBlockSize;          // parsed global block_size (for reference)

    public int getDownloadId() { return downloadId; }
    public void setDownloadId(int downloadId) { this.downloadId = downloadId; }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public List<ModuleInfo> getModules() { return modules; }
    public void addModule(ModuleInfo module) { this.modules.add(module); }

    public int getVersionNumber() { return versionNumber; }
    public int getGlobalBlockSize() { return globalBlockSize; }
    public int getMessageId() { return messageId; }
    public int getSectionNumber() { return sectionNumber; }
    public int getLastSectionNumber() { return lastSectionNumber; }

    @Override
    public String toString() {
        return "DII{downloadId=" + downloadId +
                ", transactionId=" + transactionId +
                ", ver=" + versionNumber +
                ", msgId=0x" + Integer.toHexString(messageId) +
                ", modules=" + modules + "}";
    }

    /** 更完整、適合除錯的輸出 */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DII{")
          .append("table_id=0x").append(hex8(tableId))
          .append(", sec_len=").append(sectionLength)
          .append(", ver=").append(versionNumber)
          .append(", sec=").append(sectionNumber).append("/").append(lastSectionNumber)
          .append(", msgId=0x").append(hex16(messageId))
          .append(", dl=0x").append(hex32(downloadId))
          .append(", txid=0x").append(hex32(transactionId))
          .append(", gBlock=").append(globalBlockSize)
          .append(", modules=").append(modules.size())
          .append("}");
        return sb.toString();
    }

    // =========================
    // DII 解析主程式（吃整個 section）
    // =========================
    public static DiiMessage parse(byte[] sec) {
        if (sec == null || sec.length < 12) return null;

        int off = 0;

        int tableId = sec[off] & 0xFF;
        if (tableId != 0x3B) {
            // 不是 DSM-CC 0x3B
            return null;
        }

        // PSI: section_length 12bits
        int sectionLength = ((sec[off + 1] & 0x0F) << 8) | (sec[off + 2] & 0xFF);

        // 先把 PSI header 的 version / sectionNumber / lastSectionNumber 拿到
        int tableIdExtension = u16(sec, off + 3);
        int verByte          = sec[off + 5] & 0xFF;
        int versionNumber    = (verByte >> 1) & 0x1F;
        int sectionNumber    = sec[off + 6] & 0xFF;
        int lastSectionNumber= sec[off + 7] & 0xFF;

        // payload 從 byte 8 開始，長度 = section_length - 9（扣掉 5 bytes PSI 尾 + 4 bytes CRC）
        int payloadOff = off + 8;
        int payloadLen = sectionLength - 9;
        if (!rangeOk(sec, payloadOff, payloadLen)) return null;

        // ---- DSM-CC message header（固定 12 bytes）----
        if (payloadLen < 12) return null;
        int pd              = sec[payloadOff]   & 0xFF; // 0x11
        int dsmccType       = sec[payloadOff+1] & 0xFF; // 0x03 (download)
        int messageId       = u16(sec, payloadOff+2);   // 0x1002 for DII
        int transactionIdU  = u32(sec, payloadOff+4);   // unsigned 32
        int adaptationLen   = sec[payloadOff+9] & 0xFF;
        int messageLen      = u16(sec, payloadOff+10);

        int bodyOff = payloadOff + 12 + adaptationLen;
        if (!rangeOk(sec, bodyOff, messageLen)) return null;

        // ---- DII body（精簡常用欄位）----
        int p = bodyOff;

        int downloadIdU      = u32(sec, p); p += 4;           // 32-bit unsigned
        int blockSizeU       = u16(sec, p); p += 2;           // global blockSize
        int windowSize       = sec[p++] & 0xFF;               // 可用但暫不回填
        int ackPeriod        = sec[p++] & 0xFF;

        // tCDownloadWindow(4) + tCDownloadScenario(4)
        if (!rangeOk(sec, p, 8)) return null;
        int tCDownloadWindow = u32(sec, p); p += 4;
        int tCDownloadScen   = u32(sec, p); p += 4;

        // compatibilityDescriptor()
        if (!rangeOk(sec, p, 2)) return null;
        int compLen = u16(sec, p); p += 2;
        if (!rangeOk(sec, p, compLen)) return null;
        p += compLen; // skip

        // number_of_modules
        if (!rangeOk(sec, p, 2)) return null;
        int numModules = u16(sec, p); p += 2;

        DiiMessage dii = new DiiMessage();
        dii.tableId         = tableId;
        dii.sectionLength   = sectionLength;
        dii.versionNumber   = versionNumber;
        dii.sectionNumber   = sectionNumber;
        dii.lastSectionNumber = lastSectionNumber;
        dii.messageId       = messageId;

        dii.setDownloadId(downloadIdU);
        dii.setTransactionId(transactionIdU);
        dii.globalBlockSize = blockSizeU;

        for (int i = 0; i < numModules; i++) {
            // moduleId(2) | moduleSize(4) | moduleVersion(1) | moduleInfoLength(1)
            if (!rangeOk(sec, p, 2+4+1+1)) return dii; // 容錯：回傳已解析的部分
            int moduleId      = u16(sec, p); p += 2;
            int moduleSizeU   = u32(sec, p); p += 4;
            int moduleVersion = sec[p++] & 0xFF;
            int infoLen       = sec[p++] & 0xFF;

            if (!rangeOk(sec, p, infoLen)) return dii;
            int infoEnd = p + infoLen;

            // 預設用全域 blockSize；如需可從 info 解析覆寫
            int moduleBlockSize = blockSizeU;

            // 先預設 taps 結果（若無 taps 仍留 -1）
            int  pickedAssocTag = -1;
            int pickedTapTxId  = -1;

            // module_timeout(u32) + block_timeout(u32) + min_block_time(u32)
            if (rangeOk(sec, p, 12)) {
                /* int moduleTimeout = */ u32(sec, p); p += 4;
                /* int blockTimeout  = */ u32(sec, p); p += 4;
                /* int minBlockTime  = */ u32(sec, p); p += 4;
            } else {
                p = infoEnd;
        }

            // taps_count(u8) + taps[...]（若還有空間）
            if (p < infoEnd) {
                if (!rangeOk(sec, p, 1)) { p = infoEnd; }
                else {
                    int tapsCount = sec[p++] & 0xFF;

                    // 先掃描所有 tap，優先挑 selector_type==0x0001 的那筆；若沒有，就挑第一筆
                    boolean picked = false;
                    int scanPtr = p;

                    for (int t = 0; t < tapsCount; t++) {
                        if (!rangeOk(sec, scanPtr, 16)) { scanPtr = infoEnd; break; }
                        int tapId      = u16(sec, scanPtr); scanPtr += 2;
                        int tapUse     = u16(sec, scanPtr); scanPtr += 2;
                        int assocTag   = u16(sec, scanPtr); scanPtr += 2;
                        int selector   = u16(sec, scanPtr); scanPtr += 2;
                        int tapTxId    = u32(sec, scanPtr); scanPtr += 4;
                        int tapTimeout = u32(sec, scanPtr); scanPtr += 4;

                        // 第一次先記（fallback）
                        if (!picked) {
                            pickedAssocTag = assocTag;
                            pickedTapTxId  = tapTxId ;
                        }
                        // 更佳匹配：selector_type == 0x0001
                        if (selector == 0x0001) {
                            pickedAssocTag = assocTag;
                            pickedTapTxId  = tapTxId ;
                            picked = true;
                        }
                    }

                    // 真正把 p 前進到掃描結束位置
                    p = scanPtr;
                }
            }

            // user_info_length(u8) + descriptors[user_info_length]
            if (p < infoEnd) {
                if (!rangeOk(sec, p, 1)) { p = infoEnd; }
                else {
                    int userInfoLen = sec[p++] & 0xFF;
                    if (rangeOk(sec, p, userInfoLen)) p += userInfoLen; else p = infoEnd;
                }
            }

            // 對齊 module_info 區塊結尾
            p = infoEnd;

            ModuleInfo mi = new ModuleInfo(moduleId, moduleSizeU, moduleVersion, moduleBlockSize);
            mi.assocTag = pickedAssocTag;
            mi.tapTransactionId = pickedTapTxId;

            dii.addModule(mi);
        }

        // private_data_length(u16) + private_data[...]（可選）
        if (rangeOk(sec, p, 2)) {
            int privateLen = u16(sec, p); p += 2;
            if (rangeOk(sec, p, privateLen)) {
                // byte[] privateData = slice(sec, p, privateLen);
                p += privateLen;
            }
        }

        return dii;
    }

    // ========== 可選：從 module_info 裡讀 block size（若有自定義 descriptor）==========
    @SuppressWarnings("unused")
    private static int maybeParseBlockSizeFromModuleInfo(byte[] b, int off, int len, int fallback) {
        int p = off;
        while (p + 2 <= off + len) {
            int tag  = b[p++] & 0xFF;
            int dlen = b[p++] & 0xFF;
            if (p + dlen > off + len) break;
            // 範例：自定義 tag=0x80，含 2-byte block size
            if (tag == 0x80 && dlen >= 2) {
                return u16(b, p);
            }
            p += dlen;
        }
        return fallback;
    }

    // ========== 工具 ==========
    private static boolean rangeOk(byte[] b, int off, int len) {
        return b != null && off >= 0 && len >= 0 && off + len <= b.length;
    }
    private static int u16(byte[] b, int p) {
        return ((b[p] & 0xFF) << 8) | (b[p+1] & 0xFF);
    }
    private static int u32(byte[] b, int p) {
        return ((b[p] & 0xFF) << 24)
             | ((b[p+1] & 0xFF) << 16)
             | ((b[p+2] & 0xFF) << 8)
             |  (b[p+3] & 0xFF);
    }
    @SuppressWarnings("unused")
    private static byte[] slice(byte[] b, int p, int n) {
        if (n <= 0) return new byte[0];
        byte[] o = new byte[n];
        System.arraycopy(b, p, o, 0, n);
        return o;
    }
    private static String hex8(int v){ return String.format("%02X", v & 0xFF); }
    private static String hex16(int v){ return String.format("%04X", v & 0xFFFF); }
    private static String hex32(int v){ return String.format("%08X", v); }
}
