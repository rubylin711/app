package com.prime.dtv.service.dsmcc.parse;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class BIOPName {

    static final class NameComponent {
        final int id_length;
        final byte[] id_data;
        final int kind_length;
        final byte[] kind_data;

        NameComponent(int idLen, byte[] id, int kindLen, byte[] kind) {
            this.id_length = idLen;
            this.id_data = id;
            this.kind_length = kindLen;
            this.kind_data = kind;
        }

        String idString()   { return new String(id_data, StandardCharsets.UTF_8); }
        String kindString() { return new String(kind_data, StandardCharsets.US_ASCII); }
    }

    private final int nameComponentsCount;
    private final List<NameComponent> components;
    private final int length; // consumed bytes

    BIOPName(int count, List<NameComponent> comps, int len) {
        this.nameComponentsCount = count;
        this.components = comps;
        this.length = len;
    }

    String getName() {
        if (components.isEmpty()) return "";
        return components.get(0).idString(); // DVB 常見 1 個 component
    }

    int getLength() { return length; }
    List<NameComponent> getComponents() { return components; }

    // 既有版本：byte[] + offset
    static BIOPName parse(byte[] data, int offset) {
        if (data == null || offset < 0 || offset >= data.length) return null;
        int p = offset;
        if (p + 1 > data.length) return null;

        int count = data[p++] & 0xFF;
        if (count < 0 || count > 16) return null;

        List<NameComponent> comps = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            if (p + 1 > data.length) return null;
            int idLen = data[p++] & 0xFF;
            if (idLen > 255 || p + idLen > data.length) return null;
            byte[] id = new byte[idLen];
            System.arraycopy(data, p, id, 0, idLen);
            p += idLen;

            if (p + 1 > data.length) return null;
            int kindLen = data[p++] & 0xFF;
            if (kindLen > 255 || p + kindLen > data.length) return null;
            byte[] kind = new byte[kindLen];
            System.arraycopy(data, p, kind, 0, kindLen);
            p += kindLen;

            comps.add(new NameComponent(idLen, id, kindLen, kind));
        }
        return new BIOPName(count, comps, p - offset);
    }

    // ★ 新增多載：直接從 ByteBuffer 讀並推進 position
    static BIOPName parse(ByteBuffer bb) {
        if (bb == null || bb.remaining() < 1) return null;
        int startPos = bb.position();

        int count = bb.get() & 0xFF;
        if (count < 0 || count > 16) { bb.position(startPos); return null; }

        List<NameComponent> comps = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            if (bb.remaining() < 1) { bb.position(startPos); return null; }
            int idLen = bb.get() & 0xFF;
            if (idLen > 255 || idLen > bb.remaining()) { bb.position(startPos); return null; }
            byte[] id = new byte[idLen];
            bb.get(id);

            if (bb.remaining() < 1) { bb.position(startPos); return null; }
            int kindLen = bb.get() & 0xFF;
            if (kindLen > 255 || kindLen > bb.remaining()) { bb.position(startPos); return null; }
            byte[] kind = new byte[kindLen];
            bb.get(kind);

            comps.add(new NameComponent(idLen, id, kindLen, kind));
        }
        int consumed = bb.position() - startPos;
        return new BIOPName(count, comps, consumed);
    }
}
