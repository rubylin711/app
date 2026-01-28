package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;
public class ServiceListDescriptor extends DescBase{
    private static final String TAG = "ServiceListDescriptor";
    public final List<Service> mServiceList = new ArrayList<Service>();

    public ServiceListDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }
    @Override
    public void Parsing(byte[] data, int lens) {
        int t=0;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        while (t<Length) {
            final int serviceId=getInt(data, 2+t,2,MASK_16BITS);
            final int serviceType=getInt(data, 4+t,1,MASK_8BITS);
            final Service s = new Service( serviceId,serviceType);
            //Log.d(TAG,"Service Id = " +serviceId );
            //Log.d(TAG,"serviceType = " +serviceType );
            mServiceList.add(s);
            t+=3;
        }

    }

    public class Service {
        public int ServiceID;
        public int ServiceType;

        public Service(final int serviceid, final int servicetype) {
            super();
            ServiceID = serviceid;
            ServiceType = servicetype;
        }
    }
}
