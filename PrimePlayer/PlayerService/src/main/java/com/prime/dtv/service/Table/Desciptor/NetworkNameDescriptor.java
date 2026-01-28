package com.prime.dtv.service.Table.Desciptor;

import static java.lang.Byte.toUnsignedInt;

public class NetworkNameDescriptor extends DescBase{
    private static final String TAG = "NetworkNameDesciptor";

    public int[] NetworkName = new int[MAX_STRING_LENGTH_TABLES];

    public NetworkNameDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        int i=0;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        //NetworkName = new DVBString(data,2);
        for(i=0; (i<Length) && (i<(MAX_STRING_LENGTH_TABLES-1)); i++)
        {
            NetworkName[i] = toUnsignedInt(data[i+2]);
        }
        NetworkName[i] = '\0';
        //DVBString dvbString = new DVBString(data,2);
        //NetworkName = dvbString.toString();
    }
/*
    public String getNetworkNameAsString()
    {
        return NetworkName.toString();
    }

    @Override
    public String toString() {
        return super.toString() + "networkName="+NetworkName;
    }

 */
}
