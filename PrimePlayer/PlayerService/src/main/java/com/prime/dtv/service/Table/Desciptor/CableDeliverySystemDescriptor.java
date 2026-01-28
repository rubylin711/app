package com.prime.dtv.service.Table.Desciptor;

import static java.lang.Byte.toUnsignedInt;

public class CableDeliverySystemDescriptor extends DescBase{
    private static final String TAG = "CableDeliverySystemDescriptor";
    public int Frequency;
    public int Qam;
    public int SymbolRate;

    public CableDeliverySystemDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        Frequency = (toUnsignedInt(data[2]) << 24) + (toUnsignedInt(data[3]) << 16) + (toUnsignedInt(data[4]) << 8) + toUnsignedInt(data[5]);
        Frequency = bcd_to_bin(Frequency)/10;
        Qam = toUnsignedInt(data[8]);
        SymbolRate = (toUnsignedInt(data[9]) << 20) + (toUnsignedInt(data[10]) << 12) +
                (toUnsignedInt(data[11])<<4) + ((toUnsignedInt(data[12]) & 0xf0) >> 4);
        SymbolRate = bcd_to_bin(SymbolRate)/10;
    }
}
