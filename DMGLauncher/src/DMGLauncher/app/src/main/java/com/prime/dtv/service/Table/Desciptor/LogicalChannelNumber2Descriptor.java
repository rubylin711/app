package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Util.DVBString;

import java.util.ArrayList;
import java.util.List;

public class LogicalChannelNumber2Descriptor extends DescBase{
    private static final String TAG = "LogicalChannelNumber2Descriptor";
    public List<LogicalChannelNumber2> mLogicalChannelNumber2List = new ArrayList<>();

    public LogicalChannelNumber2Descriptor(byte[] data, int lens) {
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
            final int channel_list_id=getInt(data, t+2,1,MASK_8BITS);
            DVBString dvbString = new DVBString(data,t+3);
            String channel_list_name = dvbString.toString(Pvcfg.getDefaultCharset());
            t+=2+getInt(data, t+3,1,MASK_8BITS);
            String country_code = getISO8859_1String(data, t+2, 3);
            t+=3;
            int service_loop_length=getInt(data, t+2,1,MASK_8BITS);
            t+=1;
            List<LogicalChannel> channelList = new ArrayList<>();
            int s=0;
            while (s<service_loop_length) {
                final int serviceId=getInt(data, t+2+s,2,MASK_16BITS);
                final int visible = getInt(data,t+4+s,1,0x80) >>7;
                final int reserved = getInt(data,t+4+s,1,0x7C) >>2; // 5 bits
                // chNumber is 10 bits in Nordig specs V2
                final int chNumber=getInt(data, t+4+s,2,MASK_10BITS);
                final LogicalChannel lc = new LogicalChannel(serviceId, visible, chNumber);
                channelList.add(lc);
                s+=4;
            }
            t+=s;
            LogicalChannelNumber2 chList = new LogicalChannelNumber2(channel_list_id, channel_list_name,country_code,service_loop_length,channelList);
            mLogicalChannelNumber2List.add(chList);
        }

    }

    public class LogicalChannelNumber2 {
        public int ChannelListId;
        public String ChannelListName;
        public final String CountryCode;
        public int ServDescLeng;
        public List<LogicalChannel> mLogicalChannelNumber2ServiceList = new ArrayList<LogicalChannel>();

        private LogicalChannelNumber2(int channel_list_id, String channel_list_name,String country_code,
                            int service_loop_length, List<LogicalChannel> logicalChannelList) {
            super();
            this.ChannelListId = channel_list_id;
            this.ChannelListName = channel_list_name;
            this.CountryCode = country_code;
            this.ServDescLeng = service_loop_length;
            this.mLogicalChannelNumber2ServiceList = logicalChannelList;
        }


    }
    public class LogicalChannel {
        public int ServiceId;
        public int VisibleServiceFlag;
        public int LogicalChannelMumber;

        public LogicalChannel(final int id, final int visibleService, final int type){
            ServiceId = id;
            VisibleServiceFlag = visibleService;
            LogicalChannelMumber = type;
        }
    }
}
