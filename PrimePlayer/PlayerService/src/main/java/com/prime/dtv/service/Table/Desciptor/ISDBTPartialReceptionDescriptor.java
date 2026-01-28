package com.prime.dtv.service.Table.Desciptor;

import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.List;

public class ISDBTPartialReceptionDescriptor extends DescBase{
    private static final String TAG = "ISDBTPartialReceptionDescriptor";
    public List<ServiceID> msidList = new ArrayList<>();

    public ISDBTPartialReceptionDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        int i;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        for(i = 0; i < Length; i+=2)
        {
            final int sid = (toUnsignedInt(data[2+(i*2)]) << 8) + toUnsignedInt(data[3+(i*2)]);
            final ServiceID s = new ServiceID(sid);
            msidList.add(s);
        }
    }

    public class ServiceID {
        public int serviceID;
        public ServiceID(final int sid) {
            super();
            serviceID = sid;
        }
    }
}