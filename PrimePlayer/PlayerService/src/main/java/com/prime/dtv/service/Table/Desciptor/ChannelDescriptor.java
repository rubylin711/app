package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.MASK_16BITS;
import static com.prime.dtv.service.Util.Utils.getInt;
import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ChannelDescriptor extends DescBase{
    private static final String TAG = "ChannelDescriptor";

    public List<ChannelDesc> mChannelDesc = new ArrayList<ChannelDesc>();

    public ChannelDescriptor(byte[] data) {
        Parsing(data, data.length);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        //Log.d(TAG,"Tag = " + Tag + " Length = " + Length + " DataExist = " + DataExist);
        if(toUnsignedInt(data[0]) != Descriptor.CHANNEL_DESCRIPTOR)
        {
            Log.d(TAG,"unknow desciptor [" + toUnsignedInt(data[0]) + "]");
        }
        else {
            int serviceId,logicalChannelNumber,r;
            ChannelDesc channelDesc;
            Tag = toUnsignedInt(data[0]);
            Length = toUnsignedInt(data[1]);
            if(lens == Length+2 && Length>0)
                DataExist = true;
            r=0;
            while((Length-r) >= 4){
                serviceId = getInt(data, r+2, 2, MASK_16BITS);
                logicalChannelNumber = getInt(data, r+2+2, 2, MASK_16BITS);
                channelDesc = new ChannelDesc(serviceId,logicalChannelNumber);
                r=r+4;
                //Log.d(TAG,"serviceId = " + serviceId + " logicalChannelNumber = " + logicalChannelNumber);
                mChannelDesc.add(channelDesc);
            }
        }
    }

    public class ChannelDesc {
        public int ServiceId;
        public int LogicalChannelNumber;

        private ChannelDesc(int serviceId, int logicalChannelNumber) {
            super();
            this.ServiceId = serviceId;
            this.LogicalChannelNumber = logicalChannelNumber;
        }
    }

    public List<ChannelDesc> getChannelDesc(){
        return mChannelDesc;
    }
}
