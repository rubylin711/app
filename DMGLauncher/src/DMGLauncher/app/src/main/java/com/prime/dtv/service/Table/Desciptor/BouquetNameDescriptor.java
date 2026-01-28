package com.prime.dtv.service.Table.Desciptor;


import android.util.Log;

import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Util.DVBString;

import static java.lang.Byte.toUnsignedInt;

public class BouquetNameDescriptor extends DescBase{
    private static final String TAG = "BouquetNameDescriptor";
    public String BouquetName;

    public BouquetNameDescriptor(byte[] data) {
        Parsing(data, data.length);
    }


    @Override
    public void Parsing(byte[] data, int lens) {

        //Log.d(TAG,"Service Descriptor lens = " + lens);
        //Log.d(TAG,"Service Descriptor tag = " + toUnsignedInt(data[0]));
        if(toUnsignedInt(data[0]) != Descriptor.BOUQUET_NAME_DESC)
        {
            Log.d(TAG,"unknow desciptor [" + toUnsignedInt(data[0]) + "]");
            BouquetName=null;
        }
        else {
            int descriptorLength, serviceProviderNameLength, serviceNameLength;
            Tag = toUnsignedInt(data[0]);
            Length = toUnsignedInt(data[1]);
            if(lens == Length+2 && Length>0)
                DataExist = true;
            descriptorLength = toUnsignedInt(data[1]);
            if (lens == (descriptorLength+2)) {
                if (descriptorLength != 0) {
                    //byte[] serviceName = new byte[serviceNameLength];
                    //System.arraycopy(data, 5 + serviceProviderNameLength, serviceName, 0, serviceNameLength);
                    //ServiceName = new String(serviceName);
                    DVBString dvbString = new DVBString(data, 2 , descriptorLength);
                    BouquetName = dvbString.toString(Pvcfg.getDefaultCharset());
                    //Log.d(TAG,"BouquetName = " + BouquetName);
                }
                else {
                    BouquetName = null;
                    //Log.d(TAG, "Bouquet Name Length is 0");
                }
            }
        }
    }
}
