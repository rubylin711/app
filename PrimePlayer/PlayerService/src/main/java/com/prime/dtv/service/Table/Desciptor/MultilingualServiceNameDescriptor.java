package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.service.Util.DVBString;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;
public class MultilingualServiceNameDescriptor extends DescBase{
    private static final String TAG = "MultilingualServiceNameDescriptor";

    public List<MultilingualServiceName> mMultilingualServiceNameList = new ArrayList<MultilingualServiceName>();

    public MultilingualServiceNameDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }
    @Override
    public void Parsing(byte[] data, int lens) {
        int t=0;
        String service_provider_name=null;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        while (t<Length) {
            //final int languageCode=getInt(data, t+2,3,MASK_24BITS);
            DVBString languageCode = new DVBString(data, t+2, 3);
            //Log.d(TAG, "languageCode "+languageCode);
            int service_provider_name_length = getInt(data, t+2+3, 1, MASK_8BITS);
            //Log.d(TAG, "service_provider_name_length"+service_provider_name_length);
            if(service_provider_name_length>0) {
                DVBString dvbString = new DVBString(data, t + 2 + 4,service_provider_name_length);
                service_provider_name = dvbString.toString(Pvcfg.getDefaultCharset());
            }
            int service_name_length = getInt(data, t+2+4+service_provider_name_length, 1, MASK_8BITS);
            //Log.d(TAG, "service_name_length"+service_name_length);
            DVBString dvbString1 = new DVBString(data,t+2+4+service_provider_name_length);
            String service_name = dvbString1.toString(Pvcfg.getDefaultCharset());
            //Log.d(TAG, "service_name"+service_name);
            final MultilingualServiceName s = new MultilingualServiceName(languageCode.toString(), service_provider_name,service_name);
            mMultilingualServiceNameList.add(s);
            t+=5+service_provider_name_length+service_name_length;
        }

    }

    public List<MultilingualServiceName> getServiceNameList() {
        return mMultilingualServiceNameList;
    }

    public class MultilingualServiceName {
        public String ISO639LanguageCode;
        public String ServiceProviderName;
        public String ServiceName;

        public MultilingualServiceName(String languageCode, String service_provider_name2, String service_name2) {
            ISO639LanguageCode = languageCode;
            ServiceProviderName = service_provider_name2;
            ServiceName = service_name2;
        }
        public String getService_provider_name() {
            return ServiceProviderName;
        }

        public String getService_name() {
            return ServiceName;
        }
    }
}
