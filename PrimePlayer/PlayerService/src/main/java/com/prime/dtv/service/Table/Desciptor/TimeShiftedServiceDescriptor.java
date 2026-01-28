package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

public class TimeShiftedServiceDescriptor extends DescBase{
    private static final String TAG = "TimeShiftedServiceDescriptor";
    public int ReferenceServiceId;

    public TimeShiftedServiceDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }
    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        ReferenceServiceId = getInt(data,2,2,MASK_16BITS);;

    }
}
