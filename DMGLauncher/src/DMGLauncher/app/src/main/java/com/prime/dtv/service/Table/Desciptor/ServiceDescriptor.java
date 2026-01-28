package com.prime.dtv.service.Table.Desciptor;

import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Util.DVBString;

public class ServiceDescriptor extends DescBase{
    private static final String TAG = "ServiceDescriptor";
    public int ServiceType;
    public String ServiceProviderName;
    public String ServiceName;

    public ServiceDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {

        //Log.d(TAG,"Service Descriptor lens = " + lens);
        //Log.d(TAG,"Service Descriptor tag = " + toUnsignedInt(data[0]));
        if(toUnsignedInt(data[0]) != Descriptor.SERVICE_DESC)
        {
            Log.d(TAG,"unknow desciptor [" + toUnsignedInt(data[0]) + "]");
            ServiceType=-1;
            ServiceProviderName=null;
            ServiceName=null;
        }
        else {
            int descriptorLength, serviceProviderNameLength, serviceNameLength;
            Tag = toUnsignedInt(data[0]);
            Length = toUnsignedInt(data[1]);
            if(lens == Length+2 && Length>0)
                DataExist = true;
            descriptorLength = toUnsignedInt(data[1]);
            if (lens == (descriptorLength+2)) {
                ServiceType = toUnsignedInt(data[2]);
                //Log.d(TAG,"Service Descriptor descriptorLength = " + descriptorLength);
                //Log.d(TAG,"Service Descriptor ServiceType = " + ServiceType);
                serviceProviderNameLength = toUnsignedInt(data[3]);
                //Log.d(TAG,"Service Descriptor serviceProviderNameLength = " + serviceProviderNameLength);
                if (serviceProviderNameLength != 0) {
                    //byte[] serviceProviderName = new byte[serviceProviderNameLength];
                    //System.arraycopy(data, 4, serviceProviderName, 0, serviceProviderNameLength);
                    //ServiceProviderName=new String(serviceProviderName);
                    DVBString dvbString = new DVBString(data, 4, serviceProviderNameLength);
                    ServiceProviderName = dvbString.toString(Pvcfg.getDefaultCharset());
                    //Log.d(TAG,"Service Descriptor ServiceProviderName = " + ServiceProviderName);
                } else {
                    ServiceProviderName = null;
                    Log.d(TAG, "Service Provider Name Length is 0");
                }
                serviceNameLength = toUnsignedInt(data[serviceProviderNameLength + 4]);
                //Log.d(TAG,"Service Descriptor serviceNameLength = " + serviceNameLength);
                if (serviceNameLength != 0) {
                    //byte[] serviceName = new byte[serviceNameLength];
                    //System.arraycopy(data, 5 + serviceProviderNameLength, serviceName, 0, serviceNameLength);
                    //ServiceName = new String(serviceName);
                    DVBString dvbString = new DVBString(data, 5 + serviceProviderNameLength, serviceNameLength);
                    ServiceName = dvbString.toString(Pvcfg.getDefaultCharset());
                    //Log.d(TAG,"Service Descriptor ServiceName = " + ServiceName);
                } else {
                    ServiceName = null;
                    Log.d(TAG, "Service Name Length is 0");
                }
            }
        }
    }
}
