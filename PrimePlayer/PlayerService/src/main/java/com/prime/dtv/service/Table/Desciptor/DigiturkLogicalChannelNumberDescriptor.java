package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.MASK_16BITS;
import static com.prime.dtv.service.Util.Utils.MASK_8BITS;
import static com.prime.dtv.service.Util.Utils.getInt;
import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DigiturkLogicalChannelNumberDescriptor extends DescBase{
    private static final String TAG = "DigiturkLogicalChannelNumberDescriptor";
    public List<DigiturkLogicalChannelNumberDescriptor.LogicalChannelNumber> mLogicalChannelNumberList = new ArrayList<>();

    public DigiturkLogicalChannelNumberDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }
    int[] Reserved = new int[3];
    @Override
    public void Parsing(byte[] data, int lens) {
        int t=0;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
//        Log.d(TAG,"t = "+t+" Length = "+Length);
        Reserved[0] = getInt(data, 2,1,MASK_8BITS);
        Reserved[1] = getInt(data, 2+1,1,MASK_8BITS);
        Reserved[2] = getInt(data, 2+1+1,1,MASK_8BITS);
        while ((t+3)<Length) {
//            Log.d(TAG,"t = "+t);
            final int serviceId=getInt(data, 2+3+t,2,MASK_16BITS);
            final int chNumber=getInt(data, 2+3+t+2,2,MASK_16BITS);
            int tsId = 0;
            if(serviceId == getInt(data, 2+3+t+2+2,2,MASK_16BITS))
                tsId = getInt(data, 2+3+t+2+2+2,2,MASK_16BITS);
            final DigiturkLogicalChannelNumberDescriptor.LogicalChannelNumber s = new DigiturkLogicalChannelNumberDescriptor.LogicalChannelNumber(serviceId, tsId, chNumber);
            mLogicalChannelNumberList.add(s);
            t+=8;
        }
    }

    public class LogicalChannelNumber {
        public int ServiceId;
        public int TransportStreamID;
        public int LogicalChannelNumber;

        public LogicalChannelNumber(final int id, final int tsId, final int ChNum){
            ServiceId = id;
            TransportStreamID = tsId;
            LogicalChannelNumber = ChNum;
            Log.d(TAG,"service id = "+id+" Lcn = "+ ChNum + " TransportStreamID = "+tsId);
        }
    }
}
