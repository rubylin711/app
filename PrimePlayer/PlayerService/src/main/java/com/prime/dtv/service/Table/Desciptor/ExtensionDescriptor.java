package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

public class ExtensionDescriptor extends DescBase{
    private static final String TAG = "ExtensionDescriptor";
    public int DescriptorTagExtension;
    public byte[] selector_byte;

    public ExtensionDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }
    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        DescriptorTagExtension = getInt(data, 2, 1, MASK_8BITS);
        selector_byte=getBytes(data, 3, Length-1);

    }
}
