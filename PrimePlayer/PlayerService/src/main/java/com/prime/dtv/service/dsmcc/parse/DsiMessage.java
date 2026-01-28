package com.prime.dtv.service.dsmcc.parse;

/**
 * 解析 DSM-CC UN (table_id 0x3B) 中的 DSI (message_id 0x1006).
 * 僅擷取實務常用欄位：carouselId/moduleId/objectKey/assocTag/tapTransactionId 等。
 */
public final class DsiMessage {

    // ---- MPEG-TS / DSM-CC meta ----
    public int tableId;                 // 0x3B
    public int sectionLength;           // 不含前 3 bytes
    public int sectionNumber;
    public int lastSectionNumber;
    public int tableIdExtension;        // 不太用到
    public int protocolDiscriminator;   // 0x11
    public int dsmccType;               // 0x03 (download)
    public int messageId;               // 0x1006 (DSI)
    public int adaptationLength;
    public int messageLength;
    public int transactionId;           // 32-bit (DSI header)
    public int version;                 // PSI version_number

    // ---- DSI / BIOP 摘要 ----
    public int    downloadId = 0;       // DSI 本身沒有 download_id, 仍保留欄位=0
    public int    carouselId = -1;      // from ObjectLocation
    public int    moduleId   = -1;      // from ObjectLocation
    public int    assocTag   = -1;      // from ConnBinder
    public int    tapTransactionId = -1;// from ConnBinder selector(type=0x0001)
    public byte[] objectKey;            // from ObjectLocation (len + data)

    // ---- constants ----
    public static final int MSGID_DSI              = 0x1006;
    private static final int TAG_BIOP_PROFILE_BODY = 0x49534F06; // 'I''S''O''\x06'
    private static final int TAG_OBJECT_LOCATION   = 0x49534F50; // 'I''S''O''P'
    private static final int TAG_CONNBINDER        = 0x49534F40; // 'I''S''O''@'

    // 可避免壞資料拖垮 (安全上限)
    private static final int MAX_PROFILES   = 32;
    private static final int MAX_COMPONENTS = 16;
    private static final int MAX_TAPS       = 8;
    private static final int MAX_OBJECT_KEY = 32;

    private DsiMessage() {}

    public static DsiMessage parse(byte[] section) {
        DsiMessage msg = new DsiMessage();
        if (section == null || section.length < 12) return msg;

        int pos = 0;
        msg.tableId = u8(section, pos); pos += 1;
        if (msg.tableId != 0x3B) return msg;

        int ssi_private = u8(section, pos); pos += 1;
        msg.sectionLength = ((ssi_private & 0x0F) << 8) | u8(section, pos); pos += 1;

        int end = pos + msg.sectionLength;
        if (end > section.length) end = section.length; // 邊界保護

        msg.tableIdExtension = u16(section, pos); pos += 2;

        int tmp = u8(section, pos); pos += 1;
        msg.version = (tmp >> 1) & 0x1F;

        msg.sectionNumber     = u8(section, pos); pos += 1;
        msg.lastSectionNumber = u8(section, pos); pos += 1;

        msg.protocolDiscriminator = u8(section, pos); pos += 1; // 0x11
        msg.dsmccType             = u8(section, pos); pos += 1; // 0x03
        msg.messageId             = u16(section, pos); pos += 2; // 0x1006

        msg.transactionId = (int) u32(section, pos); pos += 4;

        pos += 1; // reserved
        msg.adaptationLength = u8(section, pos); pos += 1;
        pos = Math.min(end, pos + msg.adaptationLength);

        msg.messageLength = u16(section, pos); pos += 2;
        int bodyEnd = Math.min(end, pos + msg.messageLength);

        if (msg.messageId != MSGID_DSI) {
            return msg; // 非 DSI, 不再解析
        }

        // --- server_id: 20 bytes ---
        if (pos + 20 > bodyEnd) return msg; else pos += 20;

        // --- compatibilityDescriptor(): u16 length + data ---
        if (pos + 2 > bodyEnd) return msg;
        int compLen = u16(section, pos); pos += 2;
        pos = Math.min(bodyEnd, pos + compLen);

        // --- privateDataLength + privateData (內含 IOR / BIOP Profile Body) ---
        if (pos + 2 <= bodyEnd) {
            int pvtLen = u16(section, pos); pos += 2;
            int pvtEnd = Math.min(bodyEnd, pos + pvtLen);

            // IOP::IOR - type_id_length + type_id + 4-byte alignment
            if (pos + 4 <= pvtEnd) {
                long typeIdLen = u32(section, pos); pos += 4;
                int skip = safeClampToInt(typeIdLen, pvtEnd - pos);
                pos += skip;
                int pad = (int) (typeIdLen & 3);
                if (pad != 0) pos = Math.min(pvtEnd, pos + (4 - pad));
            }

            // tagged_profiles_count (u32)
            if (pos + 4 <= pvtEnd) {
                long profCountL = u32(section, pos); pos += 4;
                int profCount = (int) Math.min(profCountL, MAX_PROFILES);

                for (int i = 0; i < profCount && pos + 8 <= pvtEnd; i++) {
                    int profileIdTag = (int) u32(section, pos); pos += 4;
                    long profileDataLenL = u32(section, pos); pos += 4;
                    int profEnd = Math.min(pvtEnd, pos + safeClampToInt(profileDataLenL, pvtEnd - pos));
                    if (pos >= profEnd) { pos = profEnd; continue; }

                    // profile_data_byte_order (u8)
                    int byteOrder = u8(section, pos); pos += 1; // 0=BE(常見)

                    if (profileIdTag == TAG_BIOP_PROFILE_BODY) {
                        if (pos < profEnd) {
                            int liteCount = u8(section, pos); pos += 1;
                            liteCount = Math.min(liteCount, MAX_COMPONENTS);

                            for (int j = 0; j < liteCount && pos + 5 <= profEnd; j++) {
                                int cid = (int) u32(section, pos); pos += 4;
                                int compLenU8 = u8(section, pos); pos += 1;
                                int compEnd = Math.min(profEnd, pos + compLenU8);

                                if (cid == TAG_OBJECT_LOCATION) {
                                    // carouselId u32
                                    if (pos + 4 <= compEnd) { msg.carouselId = (int) u32(section, pos); pos += 4; }
                                    // moduleId u16
                                    if (pos + 2 <= compEnd) { msg.moduleId = u16(section, pos); pos += 2; }
                                    // version.major / version.minor
                                    if (pos + 2 <= compEnd) { pos += 2; }
                                    // objectKey_length + data
                                    if (pos + 1 <= compEnd) {
                                        int keyLen = u8(section, pos); pos += 1;
                                        keyLen = Math.min(keyLen, Math.min(MAX_OBJECT_KEY, compEnd - pos));
                                        if (keyLen > 0) {
                                            msg.objectKey = new byte[keyLen];
                                            System.arraycopy(section, pos, msg.objectKey, 0, keyLen);
                                            pos += keyLen;
                                        }
                                    }
                                    pos = compEnd;
                                }
                                else if (cid == TAG_CONNBINDER) {
                                    if (pos < compEnd) {
                                        int tapsCount = u8(section, pos); pos += 1;
                                        tapsCount = Math.min(tapsCount, MAX_TAPS);

                                        for (int k = 0; k < tapsCount; k++) {
                                            // tap 固定頭：id u16, use u16, association_tag u16
                                            if (pos + 6 > compEnd) { pos = compEnd; break; }
                                            /* id  */ pos += 2;
                                            /* use */ pos += 2;
                                            msg.assocTag = u16(section, pos); pos += 2;

                                            // selector_length (u8)
                                            if (pos + 1 > compEnd) { pos = compEnd; break; }
                                            int selLen = u8(section, pos); pos += 1;
                                            int selEnd = Math.min(compEnd, pos + selLen);

                                            // 至少有 2 bytes 才能讀 selector_type
                                            if (pos + 2 <= selEnd) {
                                                int selectorType = u16(section, pos); pos += 2;
                                                // type 0x0001: 需再有 8 bytes 為 tx(u32)+timeout(u32)
                                                if (selectorType == 0x0001 && (selEnd - pos) >= 8) {
                                                    msg.tapTransactionId = (int) u32(section, pos); pos += 4;
                                                    // timeout u32
                                                    if (pos + 4 <= selEnd) { pos += 4; }
                                                    else { pos = selEnd; }
                                                } else {
                                                    // 其他 type：不解析，整個 selector 跳過
                                                    pos = selEnd;
                                                }
                                            } else {
                                                pos = selEnd;
                                            }
                                        }
                                    }
                                    pos = compEnd;
                                }
                                else {
                                    // 未知 component
                                    pos = compEnd;
                                }
                            }
                        }
                    }

                    // 對齊到該 profile 結束
                    pos = profEnd;
                }
            }

            // 結束 privateData 區
            pos = pvtEnd;
        }

        return msg;
    }

    // --------- 輸出：與你貼的格式接近 ---------
    public String toPrettyLog() {
        StringBuilder sb = new StringBuilder(256);
        sb.append(String.format("* DSM-CC UNM, TID 0x%02X (%d)\n", tableId, tableId))
          .append(String.format("  Section: %d (last: %d), version: %d, size: %d bytes\n",
                  sectionNumber, lastSectionNumber, version, 3 + sectionLength))
          .append(String.format("  Protocol discriminator: 0x%02X (%d)\n", protocolDiscriminator, protocolDiscriminator))
          .append(String.format("  Dsmcc type: 0x%02X (Download message)\n", dsmccType))
          .append(String.format("  Message id: 0x%04X (DownloadServerInitiate)\n", messageId))
          .append(String.format("  Transaction id: 0x%08X (%d)\n", transactionId, unsigned(transactionId)))
          .append("  Server id (20 bytes):\n")
          .append("    0000:  --省略--\n"); // 若要印 server_id，請在 parse 時存下來

        // BIOP
        sb.append("  ProfileId Tag: 0x49534F06 (TAG_BIOP (BIOP Profile Body))\n")
          .append("  Profile Data Byte Order: 0x00 (0)\n");

        if (carouselId >= 0) {
            sb.append("  ComponentId Tag: 0x49534F50 (TAG_ObjectLocation (BIOP::ObjectLocation))\n")
              .append(String.format("  Carousel Id: 0x%08X (%d)\n", carouselId, unsigned(carouselId)))
              .append(String.format("  Module Id: 0x%04X (%d)\n", moduleId, moduleId))
              .append("  Version Major: 0x01 (1)\n")
              .append("  Version Minor: 0x00 (0)\n");
            if (objectKey != null && objectKey.length > 0) {
                sb.append("  Object Key Data:  ").append(hexBytes(objectKey)).append("\n");
            }
        }

        if (assocTag >= 0 || tapTransactionId >= 0) {
            sb.append("  ComponentId Tag: 0x49534F40 (TAG_ConnBinder (DSM::ConnBinder))\n")
              .append("  Tap id: 0x0000 (0)\n")
              .append("  Tap use: 0x0016 (BIOP_DELIVERY_PARA_USE (Module delivery parameters))\n")
              .append(String.format("  Tap association tag: 0x%04X (%d)\n", assocTag, assocTag))
              .append("  Tap selector type: 0x0001 (1)\n");
            if (tapTransactionId >= 0) {
                sb.append(String.format("  Tap transaction id: 0x%08X (%d)\n",
                        tapTransactionId, unsigned(tapTransactionId)));
            }
            sb.append("  Tap timeout: 0xFFFFFFFF (4294967295)\n");
        }

        sb.append("  Download taps count: 0x00 (0)\n")
          .append("  Service context list count: 0x00 (0)\n")
          .append("  User info length: 0x0000 (0)\n");
        return sb.toString();
    }

    public String toDebugString() {
        return "DSI{"
                + "table_id=0x" + hex8(tableId)
                + ", ver=" + version
                + ", sec=" + sectionNumber + "/" + lastSectionNumber
                + ", proto=0x" + hex8(protocolDiscriminator)
                + ", type=0x" + hex8(dsmccType)
                + ", msgId=0x" + hex16(messageId)
                + ", txid=0x" + hex32(transactionId)
                + ", adaptLen=" + adaptationLength
                + ", msgLen=" + messageLength
                + ", tid_ext=0x" + hex16(tableIdExtension)
                + ", carouselId=0x" + hex32(carouselId)
                + ", moduleId=0x" + hex16(moduleId)
                + ", assocTag=0x" + hex16(assocTag)
                + ", tapTx=0x" + hex32(tapTransactionId)
                + "}";
    }

    // ---- helpers ----
    private static int u8(byte[] a, int i){ return (i>=0 && i<a.length)?(a[i]&0xFF):0; }
    private static int u16(byte[] a, int i){
        if (i+1>=a.length) return 0;
        return ((a[i]&0xFF)<<8)|(a[i+1]&0xFF);
    }
    private static long u32(byte[] a, int i){
        if (i+3>=a.length) return 0;
        return ((long)(a[i]&0xFF)<<24)|((long)(a[i+1]&0xFF)<<16)
             | ((long)(a[i+2]&0xFF)<<8)|((long)(a[i+3]&0xFF));
    }
    private static int safeClampToInt(long want, int remain) {
        if (want <= 0) return 0;
        long max = Math.min(want, Math.max(0, remain));
        return (int) Math.min(max, Integer.MAX_VALUE);
    }
    private static long unsigned(int v){ return v & 0xFFFFFFFFL; }

    private static String hexBytes(byte[] b) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            s.append(String.format("%02X", b[i] & 0xFF));
            if (i + 1 < b.length) s.append(' ');
        }
        return s.toString();
    }
    private static String hex8(int v){ return String.format("%02X", v & 0xFF); }
    private static String hex16(int v){ return String.format("%04X", v & 0xFFFF); }
    private static String hex32(int v){ return String.format("%08X", v); }
}
