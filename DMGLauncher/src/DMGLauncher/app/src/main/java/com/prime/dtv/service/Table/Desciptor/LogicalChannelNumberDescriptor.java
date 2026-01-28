package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.List;

public class LogicalChannelNumberDescriptor extends DescBase{
    private static final String TAG = "LogicalChannelNumberDescriptor";
    public List<LogicalChannelNumber> mLogicalChannelNumberList = new ArrayList<>();

    public LogicalChannelNumberDescriptor(byte[] data, int lens) {
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
            final int visible = getInt(data,t+4,1,0x80) >>7; // 1 bit
            // chNumber is 14 bits in Nordig specs V1
            final int chNumber=getInt(data, t+4,2,MASK_10BITS);
            final LogicalChannelNumber s = new LogicalChannelNumber(serviceId, visible, chNumber);
            mLogicalChannelNumberList.add(s);
            t+=4;
        }

    }

    public class LogicalChannelNumber {
        public int ServiceId;
        public int VisibleServiceFlag;
        public int LogicalChannelMumber;

        public LogicalChannelNumber(final int id, final int visibleService, final int type){
            ServiceId = id;
            VisibleServiceFlag = visibleService;
            LogicalChannelMumber = type;
        }
    }
}
