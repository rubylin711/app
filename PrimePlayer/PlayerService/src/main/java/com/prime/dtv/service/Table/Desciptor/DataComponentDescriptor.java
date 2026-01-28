package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.MASK_16BITS;
import static com.prime.dtv.service.Util.Utils.MASK_2BITS;
import static com.prime.dtv.service.Util.Utils.MASK_4BITS;
import static com.prime.dtv.service.Util.Utils.MASK_8BITS;
import static com.prime.dtv.service.Util.Utils.getBytes;
import static com.prime.dtv.service.Util.Utils.getInt;
import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

public class DataComponentDescriptor extends DescBase {
    public static final String TAG = "DataComponentDescriptor";
    private int data_component_id;
    private int dmf;
    private int reserved;
    private int timing;

    public DataComponentDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        data_component_id = getInt(data, 2, 2, MASK_16BITS);
        if(Length > 2) {
            dmf = getInt(data, 4, 1, MASK_8BITS) >> 4;
            reserved = getInt(data, 4, 1, MASK_4BITS) >> 2;
            timing = getInt(data, 4, 1, MASK_2BITS);
        }
        Log.d(TAG,ToString());
    }

    public String ToString() {
        String str = "DataComponentDescriptor : \n"
                    + " Tag["+Tag+"]"
                    + " Length["+Length+"]"
                    + " data_component_id["+data_component_id+"]"
                    + " dmf["+dmf+"]"
                    + " reserved["+reserved+"]"
                    + " timing["+timing+"]";
        return str;
    }

    public int getData_component_id() {
        return data_component_id;
    }

    public int getDmf() {
        return dmf;
    }

    public int getTiming() {
        return timing;
    }
}
