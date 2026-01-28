package com.prime.dtv.service.dsmcc.parse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class IOR {
    final String typeId;     // 例如 "dir\0","fil\0"
    final byte[] objectKey;  // 從 BIOP::ObjectLocation 取出的 key（若找不到可能為 null）
    final int length;        // IOR 總長（已消耗多少）

    static IOR parse(ByteBuffer bb) {
        final int start = bb.position();
        bb.order(ByteOrder.BIG_ENDIAN);
        if (bb.remaining() < 4) return null;

        // --- type_id_length + type_id_byte[N1]
        int typeIdLen = bb.getInt(); // N1 (32, bytes)
        if (typeIdLen < 0 || typeIdLen > bb.remaining()) return null;

        byte[] typeIdBytes = new byte[typeIdLen];
        bb.get(typeIdBytes);

        // 取前 4 bytes 形成簡短 typeId（便於 dir/fil 判斷；NUL 可能在末端）
        String typeId = new String(typeIdBytes, 0, Math.min(4, typeIdLen),
                java.nio.charset.StandardCharsets.US_ASCII);

        // --- CDR 4-byte alignment after type_id
        int rem = typeIdLen % 4;
        if (rem != 0) {
            int pad = 4 - rem;
            if (bb.remaining() < pad) return null;
            bb.position(bb.position() + pad); // alignment_gap（常見 0xFF；容忍 0x00）
        }

        if (bb.remaining() < 4) return null;
        long profiles = bb.getInt() & 0xFFFFFFFFL; // taggedProfiles_count (32)

        byte[] key = null;
        LiteOptionsProfileBody liteOpt = null;
        for (long i = 0; i < profiles; i++) {
            if (bb.remaining() < 8) break;

            // 1) 讀 profileId_tag (32)
            int profileIdTag = bb.getInt();

            // 2) 讀 profile_data_length (32)
            long plen = bb.getInt() & 0xFFFFFFFFL;
            if (plen < 0 || plen > bb.remaining()) break;

            int pStart = bb.position();
            int pEnd   = (int) (pStart + plen);

            // 取這個 profile 的切片
            ByteBuffer w = bb.slice();
            w.limit((int) plen);
            w.order(ByteOrder.BIG_ENDIAN);

            if (profileIdTag == BIOPProfileBody.PROFILE_TAG_BIOP) {
                // 只在 tag = BIOP 時交給 BIOPProfileBody 解析
                BIOPProfileBody body = BIOPProfileBody.parse(w.duplicate());
                if (body != null && body.objectKey != null && body.objectKey.length > 0) {
                    key = body.objectKey;
                }

            } else if (profileIdTag == LiteOptionsProfileBody.PROFILE_TAG_LITEOPTIONS) {
                liteOpt = LiteOptionsProfileBody.parse(w.duplicate());
                // LITE_OPTIONS 不會提供 objectKey；主要提供 NSAP 與 pathName
            } else {
                // 其他 Profile（例如 LITE_OPTIONS 等）：
                // 可選擇直接略過，或用備援 TLV 掃描（若供應商把 ObjectLocation 放這裡）
                byte[] maybe = scanKeyFromTLV(w.duplicate());
                if (maybe != null && maybe.length > 0) key = maybe;
            }

            // 推進外層 bb 到這個 profile 結尾
            bb.position(pEnd);
        }

        return new IOR(typeId, key, bb.position() - start);
    }

    // === 備援：在非標準/簡化串流中，以 TLV 方式掃描 ISOP/ISO@ 取 key ===
    private static byte[] scanKeyFromTLV(ByteBuffer w) {
        while (w.remaining() >= 6) {
            int tag = w.getInt(); // 4-byte componentTag
            int len = ((w.get() & 0xFF) << 8) | (w.get() & 0xFF); // 16-bit length
            if (len < 0 || len > w.remaining()) break;

            byte[] val = new byte[len];
            w.get(val);

            // 'ISOP' or 'ISO@'
            if (tag == BIOPProfileBody.TAG_ISOP || tag == BIOPProfileBody.TAG_ISOAT) {
                byte[] k = extractKeyFromObjectLocation(val);
                if (k != null && k.length > 0) return k;
            }
        }
        return null;
    }

    // === 從 ObjectLocation component 取 objectKey（BE） ===
    private static byte[] extractKeyFromObjectLocation(byte[] val) {
        // 常見格式（BE）：
        // u32 carouselId, u16 moduleId, u8 version.major, u8 version.minor,
        // u8 objectKey_length, objectKey[Len]
        if (val == null || val.length < 7) return null;

        ByteBuffer b = ByteBuffer.wrap(val).order(ByteOrder.BIG_ENDIAN);
        if (b.remaining() < 7) return null;

        b.getInt();   // carouselId
        b.getShort(); // moduleId
        b.get();      // version.major（多為 0x01）
        if (!b.hasRemaining()) return null;

        // 有的串流還會放 version.minor 或 reserved；這裡寬鬆處理：
        int maybeMinor = b.get() & 0xFF;
        if (!b.hasRemaining()) return null;

        int keyLen = b.get() & 0xFF;
        if (keyLen > 0 && keyLen <= b.remaining() && keyLen <= 255) {
            byte[] key = new byte[keyLen];
            b.get(key);
            return key;
        }

        // 若前面將 minor 當成 reserved 造成位移，也嘗試回退一格再取
        b.position(b.position() - 1);
        if (!b.hasRemaining()) return null;
        keyLen = b.get() & 0xFF;
        if (keyLen > 0 && keyLen <= b.remaining() && keyLen <= 255) {
            byte[] key = new byte[keyLen];
            b.get(key);
            return key;
        }
        return null;
    }

    private IOR(String typeId, byte[] key, int length) {
        this.typeId = typeId;
        this.objectKey = key;
        this.length = length;
    }
}
