package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

public class AC3Descriptor extends DescBase {


    private  int component_type_flag;
    private  int bsid_flag;
    private  int mainid_flag;
    private  int asvc_flag;
    private  int reserved_flags;

    private int component_type;
    private int bsid;
    private int mainid;
    private int asvc;
    private byte[] additional_info;

    public AC3Descriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        component_type_flag = getInt(data, 2, 1, 0x80)>>7;
        bsid_flag = getInt(data, 2, 1, 0x40)>>6;
        mainid_flag = getInt(data, 2, 1, 0x20)>>5;
        asvc_flag = getInt(data, 2, 1, 0x10)>>4;
        reserved_flags = getInt(data, 2, 1, 0x0F);
        int t=3;
        if(component_type_flag!=0){
            component_type = getInt(data, t++, 1, MASK_8BITS);
        }
        if(bsid_flag!=0){
            bsid = getInt(data, t++, 1, MASK_8BITS);
        }
        if(mainid_flag!=0){
            mainid = getInt(data, t++, 1, MASK_8BITS);
        }
        if(asvc_flag!=0){
            asvc = getInt(data, t++, 1, MASK_8BITS);
        }
        if(t<Length){
            additional_info=getBytes(data, t, Length-t);
        }
    }


    public static String getComponentTypeString(final int type){
        final StringBuilder s = new StringBuilder();
        if((type&0x80)==0x80){
            s.append("Enhanced AC-3, ");
        }else{
            s.append("AC-3, ");
        }
        if((type&0x40)==0x40){
            s.append("stream is a full service, ");
        }else{
            s.append("stream is intended to be combined with another audio stream, ");
        }
        if((type&0x07)==0){
            s.append("Mono, ");
        }else if((type&0x07)==0x01){
            s.append("1+1 Mode, ");
        }else if((type&0x07)==0x02){
            s.append("2 channel (stereo), ");
        }else if((type&0x07)==0x03){
            s.append("2 channel Dolby Surround encoded (stereo), ");
        }else if((type&0x07)==0x04){
            s.append("Multichannel audio (> 2 channels), ");
        }else if((type&0x87)==0x85){
            s.append("Multichannel audio (> 5.1 channels), ");
        }else if((type&0x87)==0x86){
            s.append("Elementary stream contains multiple programmes carried in independent substreams, ");
        }
        if((type&0x78)==0x40){
            s.append("Complete Main");
        }else if((type&0x78)==0x08){
            s.append("Music and Effects");
        }else if((type&0x38)==0x10){
            s.append("Visually Impaired");
        }else if((type&0x38)==0x18){
            s.append("Hearing Impaired");
        }else if((type&0x78)==0x20){
            s.append("Dialogue");
        }else if((type&0x3f)==0x28){
            s.append("Commentary");
        }else if((type&0x7f)==0x70){
            s.append("Emergency");
        }else if((type&0x7f)==0x78){
            s.append("Voiceover");
        }else if((type&0x78)==0x78){
            s.append("Karaoke");
        }
        return s.toString();
    }
}