package com.prime.dtv.service.dsmcc.parse;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

final class BIOPFileMessage {

    // === 對齊規格的欄位 ===
    final byte[] objectInfo_data_byte; // 直接保留原始 objectInfo
    final long   contentSize;          // 取 objectInfo 前 8 bytes（不足則 -1）
    final int    serviceContextList_count;
    final List<ServiceContext> serviceContextList;
    final long   messageBody_length;   // u32
    final long   content_length;       // u32
    final byte[] content_data_byte;    // 實際檔案內容（長度=min(content_length, remaining)）

    // ---- ServiceContext ----
    static final class ServiceContext {
        final long   context_id;          // u32
        final int    context_data_length; // u16
        final byte[] context_data_byte;
        ServiceContext(long id, int len, byte[] data) {
            this.context_id = id;
            this.context_data_length = len;
            this.context_data_byte = data;
        }
    }

    private BIOPFileMessage(byte[] objectInfo,
                            long contentSize,
                            int scCount, List<ServiceContext> scList,
                            long bodyLen, long contentLen, byte[] content)
    {
        this.objectInfo_data_byte     = objectInfo;
        this.contentSize              = contentSize;
        this.serviceContextList_count = scCount;
        this.serviceContextList       = scList;
        this.messageBody_length       = bodyLen;
        this.content_length           = contentLen;
        this.content_data_byte        = content;
    }

    // 主要解析入口：依賴 base.payloadStart 指向 objectInfo 之後的位置（BIOPMessage 先讀完固定頭）
    static BIOPFileMessage parse(BIOPMessage base, ByteBuffer bb) {
        // ---- 位置與端序 ----
        bb.position(base.payloadStart);
        bb.order(ByteOrder.BIG_ENDIAN);

        // ---- 從 objectInfo 抓 contentSize（若 >=8）----
        long contentSize = -1;
        if (base.objectInfo != null && base.objectInfo.length >= 8) {
            contentSize = u64_be(base.objectInfo, 0);
        }

        // ---- ServiceContextList（可選，容錯）----
        int serviceContextListCount = 0;
        List<ServiceContext> scList = new ArrayList<>();
        bb.mark();
        if (bb.hasRemaining()) {
            int tentativeCount = bb.get() & 0xFF;
            boolean ok = true;
            for (int i = 0; i < tentativeCount; i++) {
                if (bb.remaining() < 6) { ok = false; break; }
                long ctxId = bb.getInt() & 0xFFFFFFFFL;                 // context_id u32
                int  clen  = ((bb.get() & 0xFF) << 8) | (bb.get() & 0xFF); // u16
                if (clen < 0 || clen > bb.remaining()) { ok = false; break; }
                byte[] cdat = new byte[clen];
                bb.get(cdat);
                scList.add(new ServiceContext(ctxId, clen, cdat));
            }
            if (!ok) {
                // 回退：把前面的計數視為不是 ServiceContextList
                bb.reset();
                scList.clear();
            } else {
                serviceContextListCount = tentativeCount;
            }
        }

        // ---- messageBody_length(u32) + content_length(u32) ----
        long bodyLen = 0;
        long contentLen = 0;
        if (bb.remaining() >= 8) {
            bodyLen    = bb.getInt() & 0xFFFFFFFFL;
            contentLen = bb.getInt() & 0xFFFFFFFFL;
        } else {
            // 不足以讀 header，回傳空內容
            return new BIOPFileMessage(base.objectInfo, contentSize,
                    serviceContextListCount, scList, 0, 0, new byte[0]);
        }

        // ---- content_data_byte（實際檔案內容）----
        int take = (int)Math.min(contentLen, Math.max(0, bb.remaining()));
        byte[] content = new byte[take];
        bb.get(content);

        return new BIOPFileMessage(base.objectInfo, contentSize,
                serviceContextListCount, scList, bodyLen, contentLen, content);
    }

    // ---- 工具 ----
    private static long u64_be(byte[] a, int off) {
        // 以無號 64 位讀取；回傳 Java long（足以存檔案大小）
        return ((long)(a[off    ] & 0xFF) << 56) |
               ((long)(a[off + 1] & 0xFF) << 48) |
               ((long)(a[off + 2] & 0xFF) << 40) |
               ((long)(a[off + 3] & 0xFF) << 32) |
               ((long)(a[off + 4] & 0xFF) << 24) |
               ((long)(a[off + 5] & 0xFF) << 16) |
               ((long)(a[off + 6] & 0xFF) <<  8) |
               ((long)(a[off + 7] & 0xFF));
    }

    // 方便把檔案存到磁碟（可選）
    void saveTo(File root, String relativePath) throws Exception {
        File f = new File(root, relativePath);
        File p = f.getParentFile();
        if (p != null && !p.exists()) p.mkdirs();
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(content_data_byte);
        }
    }

    // 一些輔助 getter（可視需要擴充）
    boolean hasFullContent() { return content_length == content_data_byte.length; }
    long    advertisedSize() { return contentSize >= 0 ? contentSize : content_length; }
}
