package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

public class TerrestrialDeliverySystemDescriptor extends DescBase{
    private static final String TAG = "TerrestrialDeliverySystemDescriptor";
    public int CentreFrequency;
    public int bandwidth;
    public int FTTMode;
    public int Constellation;
    public int Hierarchy;
    public int CodeRateH;
    public int CodeRateL ;
    public int GuardInterval;
    public int OtherFrequencyFlag;

    public TerrestrialDeliverySystemDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        CentreFrequency = getInt(data, 2, 4, MASK_32BITS);
        switch(((toUnsignedInt(data[6]) & 0xe0 ) >> 5))
        {
            case 0://8M
                bandwidth = 8;
                break;
            case 1://7M
                bandwidth = 7;
                break;
            case 2://6M
                bandwidth = 6;
                break;
            case 3://5M
                bandwidth = 5;
                break;
        }
        Constellation = getInt(data, 7, 1, 0b1100_0000)>>>6;
        Hierarchy = getInt(data, 7, 1, 0b0011_1000)>>>3;
        CodeRateH = getInt(data, 7, 1, 0x07);
        CodeRateL = getInt(data, 8, 1, 0xe0)>>>5;
        GuardInterval = getInt(data, 8, 1, 0b0001_1000)>>>3;
        OtherFrequencyFlag = getInt(data, 8, 1, 0b0000_0001);
    }
}
