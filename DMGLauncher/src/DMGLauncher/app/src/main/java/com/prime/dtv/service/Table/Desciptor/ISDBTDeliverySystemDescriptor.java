package com.prime.dtv.service.Table.Desciptor;

import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.List;

public class ISDBTDeliverySystemDescriptor extends DescBase{
    private static final String TAG = "ISDBTDeliverySystemDescriptor";
    public int AreaCode;
    public int GuardInterval;
    public int transmissionMode;
    public int Freq_list_num;
    public List<Frequency> mfreqList = new ArrayList<>();

    public ISDBTDeliverySystemDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        int i;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        AreaCode = (toUnsignedInt(data[2]) << 4) + ((toUnsignedInt(data[3]) & 0xF0) >> 4);
        GuardInterval = ((toUnsignedInt(data[3]) & 0x0C) >> 2);
        transmissionMode = (toUnsignedInt(data[3]) & 0x03);
        Freq_list_num = (Length -2)/2;
        for(i = 0; i < Freq_list_num; i+=2)//centaur 20160107 fix autoOTA error
        {
            final int frequency = (((toUnsignedInt(data[4+i*2]) << 8) + toUnsignedInt(data[4+i*2+1]))/7)*1000+143;
            final Frequency s = new Frequency(frequency);
            mfreqList.add(s);
        }


    }

    public class Frequency {
        public int ISDBT_Frequency;

        public Frequency(final int frequency) {
            super();
            ISDBT_Frequency = frequency;
        }
    }
}