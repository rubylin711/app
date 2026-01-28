package com.prime.dtv.service.dsmcc.parse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * BIOP Profile Body parser (Table 4.5)
 *
 * Layout:
 *   profile_data_byte_order : u8 (0x00 = big-endian)
 *   liteComponents_count    : u8 (N1)
 *   repeat N1 times:
 *     componentId_tag       : u32
 *     component_data_length : u8 (N)
 *     component_data_byte   : N bytes
 *
 * Supported components:
 *   'ISOP' (0x49534F50) -> BIOP::ObjectLocation  -> provides objectKey
 *   'ISO@' (0x49534F40) -> DSM::ConnBinder       -> provides TAP list
 */
public final class BIOPProfileBody {

    // Component tags
    /** Table 4.5：BIOP Profile 的 profileId_tag 值 */
    public static final int PROFILE_TAG_BIOP        = 0x49534F06; // BIOP
    public static final int PROFILE_TAG_LITEOPTIONS = 0x49534F05; // LITE_OPTIONS
    
    public static final int TAG_ISOP  = 0x49534F50; // 'ISOP' - ObjectLocation
    public static final int TAG_ISOAT = 0x49534F40; // 'ISO@' - ConnBinder

    /** 0x00 expected (big endian). Parser will only accept 0x00. */
    public final int byteOrder;
    /** component count (liteComponents_count) */
    public final int componentCount;

    /** ObjectLocation -> objectKey (may be null) */
    public final byte[] objectKey;

    /** ConnBinder -> TAP list (may be empty) */
    public final List<Tap> taps;

    private BIOPProfileBody(int byteOrder, int componentCount, byte[] objectKey, List<Tap> taps) {
        this.byteOrder = byteOrder;
        this.componentCount = componentCount;
        this.objectKey = objectKey;
        this.taps = taps;
    }

    /** Parsed TAP record from DSM::ConnBinder. */
    public static final class Tap {
        public final int id;
        public final int use;               // often 0x0016 (BIOP_DELIVERY_PARA_USE)
        public final int associationTag;
        public final int selectorType;      // often 0x01
        public final long transactionId;
        public final long timeout;

        public Tap(int id, int use, int associationTag, int selectorType, long transactionId, long timeout) {
            this.id = id;
            this.use = use;
            this.associationTag = associationTag;
            this.selectorType = selectorType;
            this.transactionId = transactionId;
            this.timeout = timeout;
        }

        @Override public String toString() {
            return "Tap{id=" + id + ", use=0x" + Integer.toHexString(use) +
                    ", atag=" + associationTag + ", selType=" + selectorType +
                    ", tx=0x" + Long.toHexString(transactionId) +
                    ", timeout=" + timeout + "}";
        }
    }

    /**
     * Parse a BIOPProfileBody from the given slice of ByteBuffer.
     * The buffer position will be advanced by the full size of the body (caller controls the limit).
     *
     * @param w ByteBuffer positioned at the start of profile body (limit set to profile length)
     * @return parsed BIOPProfileBody, or null on malformed data
     */
    public static BIOPProfileBody parse(ByteBuffer w) {
        w.order(ByteOrder.BIG_ENDIAN);

        if (w.remaining() < 2) return null;

        int byteOrder = w.get() & 0xFF;      // profile_data_byte_order
        int compCount = w.get() & 0xFF;      // liteComponents_count

        // Table 4.5 expects 0x00 (big endian). If not, be conservative and fail.
        if (byteOrder != 0x00) {
            // If you want to be permissive, you could continue; but spec says 0x00.
            return null;
        }

        byte[] objectKey = null;
        List<Tap> taps = new ArrayList<>();

        for (int n = 0; n < compCount; n++) {
            if (w.remaining() < 5) break;

            int ctag = w.getInt();           // componentId_tag (u32)
            int clen = w.get() & 0xFF;       // component_data_length (u8)
            if (clen > w.remaining()) break;

            // Slice the component payload
            ByteBuffer c = w.slice();
            c.limit(clen);
            c.order(ByteOrder.BIG_ENDIAN);

            if (ctag == TAG_ISOP) {
                // BIOP::ObjectLocation
                // carouselId u32 | moduleId u16 | version.major u8 | version.minor u8
                // objectKey_length u8 (N2) | objectKey[N2]
                if (c.remaining() >= 8) {
                    c.getInt();                // carouselId (available if needed)
                    c.getShort();              // moduleId
                    int vmaj = c.get() & 0xFF; // expect 0x01
                    int vmin = c.get() & 0xFF; // expect 0x00
                    if (c.hasRemaining()) {
                        int keyLen = c.get() & 0xFF;
                        if (keyLen > 0 && keyLen <= c.remaining()) {
                            objectKey = new byte[keyLen];
                            c.get(objectKey);
                        }
                    }
                }
            } else if (ctag == TAG_ISOAT) {
                // DSM::ConnBinder
                // taps_count u8 = N3
                // repeat N3 times TAP:
                //   id u16, use u16(0x0016), association_tag u16,
                //   selector_length u8(0x0A), selector_type u8(0x01),
                //   transactionId u32, timeout u32
                if (c.remaining() >= 1) {
                    int tapsCount = c.get() & 0xFF;
                    for (int t = 0; t < tapsCount; t++) {
                        if (c.remaining() < 14) break;

                        int id = c.getShort() & 0xFFFF;
                        int use = c.getShort() & 0xFFFF;
                        int assocTag = c.getShort() & 0xFFFF;
                        int selLen   = c.get() & 0xFF; // spec says 0x0A typical
                        int selType  = c.get() & 0xFF; // spec says 0x01 typical
                        long txn     = c.getInt() & 0xFFFFFFFFL;
                        long timeout = c.getInt() & 0xFFFFFFFFL;

                        // Some streams may carry extra selector bytes beyond (len - 1(type) - 4 - 4).
                        int consumedBySelector = 1 /*selType*/ + 4 /*txn*/ + 4 /*timeout*/;
                        int extra = selLen - consumedBySelector;
                        if (extra > 0 && extra <= c.remaining()) {
                            c.position(c.position() + extra); // swallow trailing selector_data
                        }

                        taps.add(new Tap(id, use, assocTag, selType, txn, timeout));
                    }
                }
            } else {
                // Unknown component: just skip
            }

            // Advance outer buffer to next component
            w.position(w.position() + clen);
        }

        return new BIOPProfileBody(byteOrder, compCount, objectKey, taps);
    }
}
