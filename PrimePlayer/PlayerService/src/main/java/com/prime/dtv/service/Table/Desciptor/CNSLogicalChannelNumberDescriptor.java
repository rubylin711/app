package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.MASK_16BITS;
import static com.prime.dtv.service.Util.Utils.MASK_10BITS;
import static com.prime.dtv.service.Util.Utils.MASK_8BITS;
import static com.prime.dtv.service.Util.Utils.getInt;
import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CNSLogicalChannelNumberDescriptor extends DescBase{
    private static final String TAG = "CNSLogicalChannelNumberDescriptor";
    public List<CNSLogicalChannelNumberDescriptor.LogicalChannelNumber> mLogicalChannelNumberList = new ArrayList<>();

    public CNSLogicalChannelNumberDescriptor(byte[] data, int lens, int transportStreamId) {
        Parsing(data,lens);
    }
    @Override
    public void Parsing(byte[] data, int lens) {
        int t=0;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;

        while (t<Length) {
            final int serviceId=getInt(data, 2+t,2,MASK_16BITS);
            final int chNumber=getInt(data, t+4,2,MASK_16BITS);
            final LogicalChannelNumber s = new LogicalChannelNumber(serviceId, chNumber);
            mLogicalChannelNumberList.add(s);
            t+=4;
        }
    }

    public class LogicalChannelNumber {
        public int ServiceId;
        public int LogicalChannelNumber;

        public LogicalChannelNumber(final int id, final int ChNum){
            ServiceId = id;
            LogicalChannelNumber = ChNum;
        }
    }
}
