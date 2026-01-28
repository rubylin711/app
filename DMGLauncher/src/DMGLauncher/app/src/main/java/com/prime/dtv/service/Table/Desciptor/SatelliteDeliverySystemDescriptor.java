package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

public class SatelliteDeliverySystemDescriptor extends DescBase{
    private static final String TAG = "SatelliteDeliverySystemDescriptor";
    public int Frequency;
    public int OrbitalPosition;
    public int WestEastFlag;
    public int Polarization;
    public int Modulation;
    public int SymbolRate;
    public int FECInner;

    public SatelliteDeliverySystemDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        Frequency = (toUnsignedInt(data[2]) << 24) + (toUnsignedInt(data[3]) << 16) + (toUnsignedInt(data[4]) << 8) + toUnsignedInt(data[5]);
        OrbitalPosition = getInt(data, 6, 2, MASK_16BITS);
        WestEastFlag= getInt(data, 8 , 1, 0x80)>>7;
        Polarization= getInt(data, 8 , 1, 0x60)>>5;
        Modulation= getInt(data, 8 , 1, MASK_5BITS);
        SymbolRate = (toUnsignedInt(data[9]) << 20) + (toUnsignedInt(data[10]) << 12) +
                (toUnsignedInt(data[11])<<4) + ((toUnsignedInt(data[12]) & 0xf0) >> 4);
        FECInner = getInt(data, 12, 1, MASK_4BITS);

    }
}
