package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

public class EnhancedAC3Descriptor extends DescBase {


    private int component_type_flag;
    private int bsid_flag;
    private int mainid_flag;
    private int asvc_flag;
    private int mixinfoexists;
    private int substream1_flag;
    private int substream2_flag;
    private int substream3_flag;

    private int component_type;
    private int bsid;
    private int mainid;
    private int asvc;
    private int substream1;
    private int substream2;
    private int substream3;
    private byte[] additional_info;

    public EnhancedAC3Descriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
    
        if(lens == Length+2 && Length>0)
            DataExist = true;
        else {
            DataExist = false;
            return;
        }
        component_type_flag = getInt(data, 2, 1, 0x80)>>7;
        bsid_flag = getInt(data, 2, 1, 0x40)>>6;
        mainid_flag = getInt(data, 2, 1, 0x20)>>5;
        asvc_flag = getInt(data, 2, 1, 0x10)>>4;
        mixinfoexists = getInt(data, 2, 1, 0x08)>>3;
        substream1_flag = getInt(data, 2, 1, 0x04)>>2;
        substream2_flag = getInt(data, 2, 1, 0x02)>>1;
        substream3_flag = getInt(data, 2, 1, 0x01);


        if(Length < (bsid_flag+component_type_flag+mainid_flag+asvc_flag+mixinfoexists+substream1_flag+substream2_flag+substream3_flag)+1) {
            DataExist = false;
        }
        else {
            int t = 3;
            if (component_type_flag != 0) {
                component_type = getInt(data, t++, 1, MASK_8BITS);
            }
            if (bsid_flag != 0) {
                bsid = getInt(data, t++, 1, MASK_8BITS);
            }
            if (mainid_flag != 0) {
                mainid = getInt(data, t++, 1, MASK_8BITS);
            }
            if (asvc_flag != 0) {
                asvc = getInt(data, t++, 1, MASK_8BITS);
            }
            if (substream1_flag != 0) {
                substream1 = getInt(data, t++, 1, MASK_8BITS);
            }
            if (substream2_flag != 0) {
                substream2 = getInt(data, t++, 1, MASK_8BITS);
            }
            if (substream3_flag != 0) {
                substream3 = getInt(data, t++, 1, MASK_8BITS);
            }
            if (t < Length) {
                additional_info = getBytes(data, t, Length - t);
            }
        }
    }

}