package com.prime.launcher.teletextservice.fullpageteletext;

public class FttxPacket27 {
    public int[] pageno;
    public int Controlbit;

    public FttxPacket27() {
        this.pageno = new int[5];
    }

    public void parse(int dataBytePos, String[] items) {
        // Packet Address
        int packetAddress = Integer.parseInt(items[dataBytePos - 1]);
        int magazineNo = (7 & packetAddress);
        // designation code
        int designationCode = Integer.parseInt(items[dataBytePos]);

        if (0 == designationCode) {
            for (int i = 0; i < 4; ++i) {  // first 4page link info(Byte7 to Byte30)
                int MagM2M3 = Integer.parseInt(items[dataBytePos + 6]);  // Subcode S4 + M2, M3
                int MagM1 = Integer.parseInt(items[dataBytePos + 4]);  // Subcode S2 + M1

                int bit4 = bitExtract(MagM2M3, 1, 4);
                int bit3 = bitExtract(MagM2M3, 1, 3);
                int bit4M1 = bitExtract(MagM1, 1, 4);
                String str = Integer.toString(bit4) + Integer.toString(bit3) + Integer.toString(
                        bit4M1);

                int MagazineBitsVal = Integer.parseInt(str, 2);
                int magCtrlBitsAppendVal = (magazineNo ^ MagazineBitsVal);
                String convertStr = Integer.toString(magCtrlBitsAppendVal);

                // Concatenate Magazine bits, pageTens & pageUnits
                String pageNumber =
                        convertStr + (items[dataBytePos + 2]) + (items[dataBytePos + 1]);

                pageno[i] = Integer.parseInt(pageNumber);
                dataBytePos = dataBytePos + 6;
            }
            dataBytePos = dataBytePos + 13;
            int ControlByte = Integer.parseInt(items[dataBytePos]);
            Controlbit = bitExtract(ControlByte, 1, 4);
        }
    }

    static int bitExtract(int number, int k, int pos) {
        return (((1 << k) - 1) & (number >> (pos - 1)));
    }
}