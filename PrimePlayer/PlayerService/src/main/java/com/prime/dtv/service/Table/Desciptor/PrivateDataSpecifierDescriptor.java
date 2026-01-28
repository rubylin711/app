package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

public class PrivateDataSpecifierDescriptor extends DescBase{
    private static final String TAG = "PrivateDataSpecifierDescriptor";

    public long PrivateDataSpecifier;

    public PrivateDataSpecifierDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }
    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        PrivateDataSpecifier = getLong(data,2,4,0xFFFFFFFF);

    }
}
