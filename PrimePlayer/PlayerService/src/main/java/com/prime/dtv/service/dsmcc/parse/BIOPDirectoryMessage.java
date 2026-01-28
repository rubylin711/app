package com.prime.dtv.service.dsmcc.parse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class BIOPDirectoryMessage {

    // === 規格對齊命名 ===
    final byte[] objectInfo_data_byte;     // from base.objectInfo
    final int serviceContextList_count;
    final List<ServiceContext> serviceContextList;
    final long messageBody_length;
    final int bindings_count;
    final List<Binding> bindings;          // 完整 binding 物件
    final Map<String, byte[]> bindingsNameToKey; // name → objectKey

    static final class ServiceContext {
        final long context_id;            // u32
        final int context_data_length;    // u16
        final byte[] context_data_byte;   // len bytes
        ServiceContext(long id, int len, byte[] data) {
            this.context_id = id; this.context_data_length = len; this.context_data_byte = data;
        }
    }

    static final class Binding {
        final BIOPName name;              // BIOP::Name()
        final int bindingType;            // 0x01 nobject, 0x02 ncontext
        final IOR ior;                    // IOP::IOR()
        final int objectInfo_length;      // u16
        final byte[] objectInfo_data_byte;// len bytes
        Binding(BIOPName n, int bt, IOR i, int oil, byte[] oid) {
            this.name = n; this.bindingType = bt; this.ior = i;
            this.objectInfo_length = oil; this.objectInfo_data_byte = oid;
        }
    }

    private BIOPDirectoryMessage(byte[] objectInfo,
                                 int scCount, List<ServiceContext> scList,
                                 long bodyLen, int bindCount, List<Binding> bindList,
                                 Map<String, byte[]> nameToKey)
    {
        this.objectInfo_data_byte   = objectInfo;
        this.serviceContextList_count = scCount;
        this.serviceContextList     = scList;
        this.messageBody_length     = bodyLen;
        this.bindings_count         = bindCount;
        this.bindings               = bindList;
        this.bindingsNameToKey      = nameToKey;
    }

    static BIOPDirectoryMessage parse(BIOPMessage base, ByteBuffer bb) {
        // 指到 payload 起點，按規格皆為 BE
        bb.position(base.payloadStart);
        bb.order(ByteOrder.BIG_ENDIAN);

        // ---- ServiceContextList（可選，容錯讀法）----
        int serviceContextListCount = 0;
        List<ServiceContext> scList = new ArrayList<>();
        bb.mark();
        if (bb.hasRemaining()) {
            int tentativeCount = bb.get() & 0xFF;
            boolean ok = true;
            for (int i = 0; i < tentativeCount; i++) {
                if (bb.remaining() < 6) { ok = false; break; }
                long ctxId = bb.getInt() & 0xFFFFFFFFL;               // context_id u32
                int  clen  = ((bb.get() & 0xFF) << 8) | (bb.get() & 0xFF); // context_data_length u16
                if (clen < 0 || clen > bb.remaining()) { ok = false; break; }
                byte[] cdat = new byte[clen];
                bb.get(cdat);                                         // context_data_byte[]
                scList.add(new ServiceContext(ctxId, clen, cdat));
            }
            if (!ok) {
                bb.reset();       // 視為沒有 ServiceContextList
                scList.clear();
            } else {
                serviceContextListCount = tentativeCount;
            }
        }

        // ---- messageBody_length(u32) + bindings_count(u16) ----
        if (bb.remaining() < 6) {
            return new BIOPDirectoryMessage(
                base.objectInfo, serviceContextListCount, scList,
                0, 0, new ArrayList<>(), new LinkedHashMap<>()
            );
        }
        long bodyLen  = bb.getInt() & 0xFFFFFFFFL;                  // messageBody_length
        int  bindCount = ((bb.get() & 0xFF) << 8) | (bb.get() & 0xFF); // bindings_count

        // ---- Bindings ----
        List<Binding> bindList = new ArrayList<>(Math.max(bindCount, 0));
        Map<String, byte[]> nameToKey = new LinkedHashMap<>();

        for (int i = 0; i < bindCount; i++) {
            BIOPName name = BIOPName.parse(bb);     // BIOP::Name()
            if (name == null || bb.remaining() < 1) break;

            int bindingType = bb.get() & 0xFF;      // 0x01 nobject, 0x02 ncontext

            IOR ior = IOR.parse(bb);                // IOP::IOR()
            if (ior == null || bb.remaining() < 2) break;

            int infoLen = ((bb.get() & 0xFF) << 8) | (bb.get() & 0xFF);  // objectInfo_length
            if (infoLen < 0 || infoLen > bb.remaining()) break;
            byte[] info = new byte[infoLen];
            bb.get(info);                            // objectInfo_data_byte[]

            bindList.add(new Binding(name, bindingType, ior, infoLen, info));

            if (ior.objectKey != null) {
                nameToKey.put(name.getName(), ior.objectKey);
            }
        }

        return new BIOPDirectoryMessage(
            base.objectInfo, serviceContextListCount, scList,
            bodyLen, bindCount, bindList, nameToKey
        );
    }

    // （可選）幾個 getter，讓外部好拿資料
    List<Binding> getBindings() { return bindings; }
    Map<String, byte[]> getBindingsNameToKey() { return bindingsNameToKey; }
    int getBindingsCount() { return bindings_count; }
}
