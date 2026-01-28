package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

public class DataBroadcastDescriptor extends DescBase{
    private static final String TAG = "DataBroadcastDescriptor";
    public int DataBroadcastId;
    public int ComponentTag;

    public DataBroadcastDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }
    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        DataBroadcastId = getInt(data, 2, 2, MASK_16BITS);
        ComponentTag = getInt(data, 4, 1, MASK_8BITS);

    }

}
