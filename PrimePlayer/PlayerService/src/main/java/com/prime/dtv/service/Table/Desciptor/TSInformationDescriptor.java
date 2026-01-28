package com.prime.dtv.service.Table.Desciptor;

import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.List;

public class TSInformationDescriptor extends DescBase{
    private static final String TAG = "TSInformationDescriptor";
    public int remote_control_key_id;

    public TSInformationDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        int i;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        if(Length>0)
            remote_control_key_id = toUnsignedInt(data[2]);
    }
}