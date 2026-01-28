package com.prime.launcher.teletextservice.fullpageteletext;

public class FttxPacket1_25 {

    //  300706 says a packets X/Y is 45 bytes
    //  - with 2 bytes for clock run-in, 1 byte for framing code, etc ... (section 9.3.2)
    //  300472 says data_field_length is 44 (0x2C)
    //  - with 1 byte for [field_parity, line_offset], 1 byte for framing code, etc ...
    //    (section 4.3)
    //  300472 explains that framing code, magazine_and_packet_address, data_block are the
    //  43 bytes that follows clock run-in
    //  To conclude, we have here (aka after magazine_and_packet_address) 40 bytes to read
    private static final int DATA_BLOCK_SIZE = 40;

    public FttxPacket1_25() {
        //To access it from TeletextService.java
    }

    public void parse(int dataBytePos, String[] items, FttxPage page, int packetNumber,
            boolean subtitlePage) {
        FttxLine line = page.getLine(packetNumber);
        line.clear(subtitlePage);
        for (int i = 0; i < DATA_BLOCK_SIZE; ++i) {
            int code = Integer.parseInt(items[dataBytePos + i]);
            line.setCode(i, code, page.getCharset(), subtitlePage);
        }
        page.setUpdated(true);
    }
}
