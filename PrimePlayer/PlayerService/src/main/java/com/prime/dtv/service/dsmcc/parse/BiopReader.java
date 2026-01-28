package com.prime.dtv.service.dsmcc.parse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

public final class BiopReader {

    private final BiopSink sink;
    private String mountPath = "";
    private int debug=0;

    public BiopReader(BiopSink sink) { this.sink = sink; }

    public void parse1(int downloadId, int moduleId, byte[] moduleBytes) {
        if (moduleBytes == null || moduleBytes.length < 12) return;

		// 在 DSM-CC Download（DII/DDB）情境下，downloadId 可視為 carouselId 的上下文
        final int carouselId = downloadId;
        ByteBuffer bb = ByteBuffer.wrap(moduleBytes);
        bb.order(ByteOrder.BIG_ENDIAN);

       
    }
    public void parse(int downloadId, int moduleId, byte[] moduleBytes) {
        if (moduleBytes == null || moduleBytes.length < 12) return;

		// 在 DSM-CC Download（DII/DDB）情境下，downloadId 可視為 carouselId 的上下文
        final int carouselId = downloadId;
        ByteBuffer bb = ByteBuffer.wrap(moduleBytes);
        bb.order(ByteOrder.BIG_ENDIAN);
        if (debug ==1)
            return;
        while (seekNextMagic(bb)) {
            final int msgStart = bb.position();

            BIOPMessage base = BIOPMessage.parse(bb);
            if (base == null) {
                // 無效 BIOP，往前一位繼續找，避免死循環
                bb.position(Math.min(bb.limit(), msgStart + 1));
                continue;
            }

            // 規格：DVB 要求 byte_order=0(大端)。若不是 0，仍以大端解析（多數實況仍大端）
            if (base.byteOrder != 0) {
                // 如果你要支援小端，可在這裡切換 bb.order(ByteOrder.LITTLE_ENDIAN) 並確保子解析配合
                bb.order(ByteOrder.BIG_ENDIAN);
            } else {
                bb.order(ByteOrder.BIG_ENDIAN);
            }

            final String kind3 = base.objectKind.length() >= 3 ? base.objectKind.substring(0, 3) : base.objectKind;
            
            switch (kind3) {
                case "srg":
                case "dir": {
                    BIOPDirectoryMessage dir = BIOPDirectoryMessage.parse(base, bb);

                    if ("srg".equals(kind3)) {
                        // 可選：用 SRG 的 objectInfo 嘗試當 mount 提示
                        String maybeMount = tryUtf8Name(base.objectInfo);
                        if (maybeMount != null && !maybeMount.isEmpty()) {
                            mountPath = maybeMount;
                        }
                        // 回報 SRG：帶上 carouselId 與 SRG 自己的 objectKey
                        sink.onServiceGateway(carouselId, base.objectKey, mountPath);
                    }

                    // 回報目錄：把「這個目錄自己的 objectKey」當 parent/directory key 傳回去
                    sink.onDirectory(carouselId, base.objectKey, dir.bindingsNameToKey);

                    advanceToEndOfMessage(bb, msgStart, base.messageSize);
                    break;
                }    

                case "fil": { // File
                    // 解析 File：建議 BIOPFileMessage.parse 會吃掉該 payload 區段
                    BIOPFileMessage file = BIOPFileMessage.parse(base, bb);

                    // 回報檔案內容，讓上層用 objectKey 去對應名稱與路徑
                    sink.onFile(carouselId, base.objectKey, file.content_data_byte);
                    // 統一跳到訊息尾，避免殘留字節影響下一輪
                    advanceToEndOfMessage(bb, msgStart, base.messageSize);
                  break;
                }

                default: {
                    // 其他型別暫不處理，直接跳過
                    advanceToEndOfMessage(bb, msgStart, base.messageSize);
                    break;
                }
            }
        }
    }
    // 尋找 'B''I''O''P'
    private static boolean seekNextMagic(ByteBuffer bb) {
        while (bb.remaining() >= 4) {
            int p = bb.position();
            if (bb.get(p) == 'B' && bb.get(p + 1) == 'I' && bb.get(p + 2) == 'O' && bb.get(p + 3) == 'P') {
                return true;
            }
            bb.position(p + 1);
        }
        return false;
    }

    // BIOP header 固定 12 bytes（magic4 + vmaj + vmin + byteOrder + msgType + msgSize(4)）
    private static int endOfMessageOffset(int msgStart, int messageSize) {
        int safeSize = Math.max(0, messageSize);
        long end = (long) msgStart + 12L + (long) safeSize;
        if (end > Integer.MAX_VALUE) end = Integer.MAX_VALUE;
        return (int) end;
    }

    private static void advanceToEndOfMessage(ByteBuffer bb, int msgStart, int messageSize) {
        int end = endOfMessageOffset(msgStart, messageSize);
        int pos = bb.position();
        if (pos < end) {
            bb.position(Math.min(bb.limit(), end));
        }
    }
  
    // 嘗試把 objectInfo 視為 [u16 nameLen][nameBytes(UTF-8)] 的簡單情境；失敗回 null
    private static String tryUtf8Name(byte[] info) {
        try {
            if (info == null || info.length < 2) return null;
            int n = ((info[0] & 0xFF) << 8) | (info[1] & 0xFF);
            if (n <= 0 || 2 + n > info.length) return null;
            return new String(info, 2, n, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Throwable t) {
            return null;
        }
    }
}
