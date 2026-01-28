package com.prime.dtv.service.dsmcc.parse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class BIOPMessage {
    final int versionMajor;
    final int versionMinor;
    final int byteOrder;       // 0 = BE
    final int messageType;     // 0=dir/srg, 1=file(常見 fil 也是 type=0, 依 objectKind 判)
    final int messageSize;     // 不含前 12 bytes
    final int objectKeyLength; // u8 變體
    final byte[] objectKey;
    final int objectKindLength; // u32 應為 4
    final String objectKind;    // "srg\0","dir\0","fil\0"
    final int objectInfoLength; // u16
    final byte[] objectInfo;
    final int payloadStart;     // 指向「ServiceContextList 或 body」開頭的 ByteBuffer 絕對位置

    static BIOPMessage parse(ByteBuffer bb) {
        bb.order(ByteOrder.BIG_ENDIAN);
        if (bb.remaining() < 12) return null;
        // magic
        if (bb.get()!='B' || bb.get()!='I' || bb.get()!='O' || bb.get()!='P') return null;
        int maj  = bb.get() & 0xFF;
        int min  = bb.get() & 0xFF;
        int bo   = bb.get() & 0xFF;
        int type = bb.get() & 0xFF;
        int size = bb.getInt(); // 剩餘長度

        // SubHeader (u8 keyLen)
        if (bb.remaining() < 1) return null;
        int keyLen = bb.get() & 0xFF;
        if (bb.remaining() < keyLen + 4 + 2) return null;
        byte[] key = new byte[keyLen];
        bb.get(key);

        int kindLen = bb.getInt(); // 應為 4
        if (kindLen < 4 || bb.remaining() < kindLen) return null;
        byte[] kind = new byte[kindLen];
        bb.get(kind);
        String kindStr = new String(kind, 0, Math.min(4, kindLen), java.nio.charset.StandardCharsets.US_ASCII);

        int infoLen = ((bb.get() & 0xFF) << 8) | (bb.get() & 0xFF);
        if (bb.remaining() < infoLen) return null;
        byte[] info = new byte[infoLen];
        bb.get(info);

        BIOPMessage msg = new BIOPMessage(maj, min, bo, type, size, keyLen, key, kindLen, kindStr, infoLen, info, bb.position());
        return msg;
    }

    private BIOPMessage(int maj, int min, int bo, int type, int size, int keyLen, byte[] key,
                        int kindLen, String kindStr, int infoLen, byte[] info, int payloadStart) {
        this.versionMajor = maj;
        this.versionMinor = min;
        this.byteOrder = bo;
        this.messageType = type;
        this.messageSize = size;
        this.objectKeyLength = keyLen;
        this.objectKey = key;
        this.objectKindLength = kindLen;
        this.objectKind = kindStr;
        this.objectInfoLength = infoLen;
        this.objectInfo = info;
        this.payloadStart = payloadStart;
    }
}
