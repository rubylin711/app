package com.prime.dtv.service.dsmcc.parse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Options (Lite Options) Profile Body parser (Table 4.7)
 *
 * Layout:
 *   profile_data_byte_order : u8 (0x00 = big endian)
 *   component_count         : u8 (N1)
 *   DSM::ServiceLocation component:
 *     componentId_tag       : u32 (0x49534F46, 'ISOF' = TAG_ServiceLocation)
 *     component_data_length : u32 (bytes, not bits!)
 *       serviceDomain_length: u8   (expect 0x14)
 *       serviceDomain_data  : 20B  (DVBcarouselNSAPAddress, see table 4.8)
 *       CosNaming::Name {
 *         nameComponents_count : u32 (N2)
 *         repeat N2:
 *           id_length   : u32 (N3),  id_data[N3]
 *           kind_length : u32 (N4),  kind_data[N4] (as type_id)
 *       }
 *       initialContext_length : u32 (N5), initialContext_data[N5]
 *   Remaining (N6 = N1-1): BIOP::LiteOptionComponent (tag u32, length u8, bytes[N7])
 */
public final class LiteOptionsProfileBody {

    // Profile tag in IOR::taggedProfile
    public static final int PROFILE_TAG_LITEOPTIONS = 0x49534F05; // 'ISO' 0x05

    // Component tags inside the profile body
    public static final int TAG_ServiceLocation = 0x49534F46; // 'ISOF'

    /** 0x00 expected (big endian). */
    public final int byteOrder;
    /** number of components (component_count) */
    public final int componentCount;

    /** DVB carousel NSAP address (20 bytes) if present. */
    public final byte[] nsapAddress; // may be null

    /** CosNaming pathName components (id/kind pairs). */
    public final List<NameComponent> pathName; // may be empty

    /** initialContext raw bytes (may be null). */
    public final byte[] initialContext;

    /** Any remaining LiteOptionComponent(s). */
    public final List<LiteOptionComponent> extraComponents;

    public static final class NameComponent {
        public final String id;
        public final String kind;
        public NameComponent(String id, String kind) { this.id = id; this.kind = kind; }
        @Override public String toString() { return id + "(" + kind + ")"; }
    }

    public static final class LiteOptionComponent {
        public final int tag;     // u32
        public final int length;  // u8
        public final byte[] data; // length bytes
        public LiteOptionComponent(int tag, int length, byte[] data) {
            this.tag = tag; this.length = length; this.data = data;
        }
    }

    private LiteOptionsProfileBody(
            int byteOrder, int componentCount,
            byte[] nsapAddress, List<NameComponent> pathName,
            byte[] initialContext, List<LiteOptionComponent> extraComponents) {
        this.byteOrder = byteOrder;
        this.componentCount = componentCount;
        this.nsapAddress = nsapAddress;
        this.pathName = pathName;
        this.initialContext = initialContext;
        this.extraComponents = extraComponents;
    }

    /** Parse Options (Lite Options) Profile Body. Buffer limit must equal the profile_data_length. */
    public static LiteOptionsProfileBody parse(ByteBuffer w) {
        w.order(ByteOrder.BIG_ENDIAN);
        if (w.remaining() < 2) return null;

        int byteOrder = w.get() & 0xFF;
        int compCount = w.get() & 0xFF;
        if (byteOrder != 0x00) return null; // spec expects big-endian

        byte[] nsap = null;
        List<NameComponent> path = new ArrayList<>();
        byte[] initCtx = null;
        List<LiteOptionComponent> extras = new ArrayList<>();

        // Expect first component to be DSM::ServiceLocation, but be robust
        for (int cidx = 0; cidx < compCount; cidx++) {
            if (w.remaining() < 8) break;

            int ctag = w.getInt();             // u32
            long clen = w.getInt() & 0xFFFFFFFFL; // u32 length (bytes)
            if (clen < 0 || clen > w.remaining()) break;

            ByteBuffer c = w.slice();
            c.order(ByteOrder.BIG_ENDIAN);
            c.limit((int) clen);

            if (ctag == TAG_ServiceLocation) {
                // --- DSM::ServiceLocation ---
                if (c.remaining() < 1) break;

                int domainLen = c.get() & 0xFF;  // expect 0x14
                if (domainLen > 0 && domainLen <= c.remaining()) {
                    byte[] dom = new byte[domainLen];
                    c.get(dom);
                    if (domainLen == 20) nsap = dom; // 20 bytes NSAP (DVBcarouselNSAPAddress)
                }

                // CosNaming::Name()
                if (c.remaining() >= 4) {
                    long n2 = c.getInt() & 0xFFFFFFFFL;
                    for (long i = 0; i < n2 && c.remaining() >= 4; i++) {
                        String id = "";
                        String kind = "";

                        if (c.remaining() < 4) break;
                        long n3 = c.getInt() & 0xFFFFFFFFL;
                        if (n3 >= 0 && n3 <= c.remaining()) {
                            byte[] idb = new byte[(int) n3];
                            c.get(idb);
                            id = new String(idb, java.nio.charset.StandardCharsets.US_ASCII);
                        } else break;

                        if (c.remaining() < 4) break;
                        long n4 = c.getInt() & 0xFFFFFFFFL;
                        if (n4 >= 0 && n4 <= c.remaining()) {
                            byte[] kb = new byte[(int) n4];
                            c.get(kb);
                            kind = new String(kb, java.nio.charset.StandardCharsets.US_ASCII);
                        } else break;

                        path.add(new NameComponent(id, kind));
                    }
                }

                // initialContext
                if (c.remaining() >= 4) {
                    long n5 = c.getInt() & 0xFFFFFFFFL;
                    if (n5 >= 0 && n5 <= c.remaining()) {
                        initCtx = new byte[(int) n5];
                        c.get(initCtx);
                    }
                }

                // Any tail bytes in ServiceLocation are ignored (shouldn't exist)
            } else {
                // --- BIOP::LiteOptionComponent (others) ---
                // spec: componentId_tag u32, component_data_length u8, component_data_byte[N7]
                // 但這裡在 Table 4.7 頭段已給的是 u32 長度；其它 N6 components的長度是 u8。
                // 由於我們在 Options body 層看到的是 u32 length（clen），
                // 故這裡把整塊 bytes 當作一個「額外 component」保留即可。
                byte[] data = new byte[(int) clen];
                c.get(data);
                extras.add(new LiteOptionComponent(ctag, (int) clen, data));
            }

            // advance outer buffer
            w.position(w.position() + (int) clen);
        }

        return new LiteOptionsProfileBody(byteOrder, compCount, nsap, path, initCtx, extras);
    }
}
