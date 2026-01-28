package com.prime.dtv.service.Table.Desciptor;

import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Descriptor {
    private static final String TAG = "Descriptor";
    public static final int CA_DESC = 0x09;
    public static final int ISO639LANG_Desc = 0x0A;
    public static final int MAX_BITRATE_DESC = 0x0E;
    public static final int NETWORK_NAME_DESC = 0x40;
    public static final int SERVICE_LIST_DESC = 0x41;
    public static final int SATELLITE_DELIVERY_SYSTEM_DESC = 0x43;
    public static final int CABLE_DELIVERY_SYSTEM_DESC = 0x44;
    public static final int BOUQUET_NAME_DESC = 0x47;
    public static final int SERVICE_DESC = 0x48;
    public static final int LINKAGE_DESC = 0x4A;
    public static final int TIME_SHIFTED_SERVICE_DESC = 0x4C;
    public static final int SHORT_EVENT_DESC = 0x4D;
    public static final int EXTENDED_EVENT_DESC = 0x4E;
    public static final int TIME_SHIFTED_EVENT_DESC = 0x4F;
    public static final int COMPONENT_DESC = 0x50;
    public static final int STREAM_IDENTIFIER_DESC = 0x52;
    public static final int CA_IDENTIFIER_DESC = 0x53;
    public static final int CONTENT_DESC = 0x54;
    public static final int PARENTAL_RATING_DESC = 0x55;
    public static final int TELETEXT_DESC = 0x56;
    public static final int TELEPHONE_DESC = 0x57;
    public static final int LOCAL_TIME_OFFSET_DESC = 0x58;
    public static final int SUBTITLE_DESC = 0x59;
    public static final int TERRESTRIAL_DELIVERY_SYSTEM_DESC = 0x5A;
    public static final int MULTILINGUAL_SERVICE_NAME_DESC = 0x5D;
    public static final int MULTILINGUAL_COMPONENT_DESC = 0x5E;
    public static final int PRIVATE_DATA_SPECIFIER_DESC = 0x5F;
    public static final int SHORT_SMOOTHING_BUFFER_DESC = 0x61;
    public static final int FREQUENCY_LIST_DESC = 0x62;
    public static final int DATA_BROADCAST_DESC = 0x64;
    public static final int AC3_DESC = 0x6A;
    public static final int EnhancedAC3_DESC = 0x7A;
    public static final int EXTENSION_DESC = 0x7F;
    public static final int CHANNEL_DESCRIPTOR = 0x82; ////////////////////////// TBC(SDT)
    public static final int LOGICAL_CHANNEL_DESC = 0x83;
    public static final int CHANNEL_CATEGORY_DESCRIPTOR = 0x84; ////////////////////////// TBC(SDT)
    public static final int LOGICAL_CHANNEL2_DESC = 0x87;
    public static final int NETWORK_PRODUCT_LIST_DESCRIPTOR = 0x88; ////////////////////////// TBC(NIT)
    //public static final int HD_SIMULCAST_LCN_DESC = 0x88;
    public static final int CHANNEL_PRODUCT_LIST_DESCRIPTOR = 0x89; ////////////////////////// TBC(SDT)

    public static final int CNS_LOGICAL_CHANNEL_DESC = 0x93;
    public static final int CNS_EXT_LOGICAL_CHANNEL_DESC = 0x95;

    public static final int COPY_CONTROL_DESC = 0xA0;
    public static final int PRIVATE_DESC = 0xA1;
    public static final int NDS_EPG_INFO_DESC = 0xCC;
    public static final int CI_PROTECTION_DESC = 0xCE;
    public static final int SERIES_LINKING_DESCRIPTOR = 0xD0; ////////////////////////// TBC(EIT)
    public static final int TRUE_VISION_LCN_DESC = 0xE2;
    public static final int CA_ASSOCIATION_DESC = 0xEB;
    public static final int ISDBT_DELIVERY_SYSTEM_DESC = 0xFA;
    public static final int ISDBT_PARTIAL_RECEPTION_DESC = 0xFB;
    public static final int DATA_COMPONENT_DESC = 0xFD;
    public static final int EMERGENCY_INFORMATION_DESC = 0xFC;
    public static final int TS_INFORMATION_DESC = 0xCD;
    List<DescBase> mDescList = new ArrayList<>();

    public List<DescBase> getDescList() {
        return mDescList;
    }

    public boolean ParsingDescriptor(byte[] data,int lens) {
        int tag = toUnsignedInt(data[0]);
        DescBase descBase = null;
        //(TAG,"ParsingDescriptor tag [" + tag + "]");
        switch(tag) {
            case CA_DESC: {
                descBase = new CADescriptor(data,lens);
//                NetworkNameDescriptor descriptor = new NetworkNameDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case ISO639LANG_Desc: {
                descBase = new ISO639LanguageDescriptor(data,lens);
//                NetworkNameDescriptor descriptor = new NetworkNameDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case MAX_BITRATE_DESC:{

            }break;
            case NETWORK_NAME_DESC: {
                descBase = new NetworkNameDescriptor(data,lens);
//                NetworkNameDescriptor descriptor = new NetworkNameDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case SERVICE_LIST_DESC: {
                descBase = new ServiceListDescriptor(data,lens);
//                ServiceListDescriptor descriptor = new ServiceListDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case SATELLITE_DELIVERY_SYSTEM_DESC: {
                descBase = new SatelliteDeliverySystemDescriptor(data,lens);
//                SatelliteDeliverySystemDescriptor descriptor = new SatelliteDeliverySystemDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case CABLE_DELIVERY_SYSTEM_DESC: {
                descBase = new CableDeliverySystemDescriptor(data,lens);
//                CableDeliverySystemDescriptor descriptor = new CableDeliverySystemDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case TERRESTRIAL_DELIVERY_SYSTEM_DESC: {
                descBase = new TerrestrialDeliverySystemDescriptor(data,lens);
//                TerrestrialDeliverySystemDescriptor descriptor = new TerrestrialDeliverySystemDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case LOCAL_TIME_OFFSET_DESC: {
                descBase = new LocalTimeOffsetDescriptor(data,lens);
//                LocalTimeOffsetDescriptor descriptor = new LocalTimeOffsetDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case CNS_EXT_LOGICAL_CHANNEL_DESC:
            {
                descBase = new CNSExtLogicalChannelNumberDescriptor(data,lens);
            }break;
            case LOGICAL_CHANNEL_DESC: {
                descBase = new LogicalChannelNumberDescriptor(data,lens);
//                LogicalChannelNumberDescriptor descriptor = new LogicalChannelNumberDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case SERVICE_DESC: {
                //Log.d(TAG,"Descriptor lens = " + lens);
                descBase = new ServiceDescriptor(data,lens);
                //Log.d(TAG,"Descriptor ((ServiceDescriptor) descBase).ServiceType = " + ((ServiceDescriptor) descBase).ServiceType);
                if(((ServiceDescriptor) descBase).ServiceType != -1) {
                    //Log.d(TAG,"Descriptor mDescList.add(descBase)");
                    descBase.Tag=SERVICE_DESC;
                    mDescList.add(descBase);
                }
//                ServiceDescriptor descriptor = new ServiceDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case LINKAGE_DESC: {
                descBase = new LinkageDescriptor(data,lens);
//                LinkageDescriptor descriptor = new LinkageDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case FREQUENCY_LIST_DESC: {
                descBase = new FrequencyListDescriptor(data,lens);
//                FrequencyListDescriptor descriptor = new FrequencyListDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case LOGICAL_CHANNEL2_DESC: {
                descBase = new LogicalChannelNumber2Descriptor(data,lens);
//                LogicalChannelNumber2Descriptor descriptor = new LogicalChannelNumber2Descriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case DATA_BROADCAST_DESC: {
                descBase = new DataBroadcastDescriptor(data,lens);
//                DataBroadcastDescriptor descriptor = new DataBroadcastDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case TIME_SHIFTED_SERVICE_DESC: {
                descBase = new TimeShiftedServiceDescriptor(data,lens);
//                TimeShiftedServiceDescriptor descriptor = new TimeShiftedServiceDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case EXTENSION_DESC: {

                final int descriptor_tag_extension = toUnsignedInt(data[2]);
                switch(descriptor_tag_extension) {
                    case 0x04:
                        descBase = new T2DeliverySystemDescriptor(data, lens);
                        break;
                    default:
                        DVBExtensionDescriptor d = new DVBExtensionDescriptor(data, lens);
                        Log.w(TAG,"unimplemented DVBExtensionDescriptor:" +
                                d.getDescriptorTagString());
                        break;
                }

//                ExtensionDescriptor descriptor = new ExtensionDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case MULTILINGUAL_SERVICE_NAME_DESC: {
                descBase = new MultilingualServiceNameDescriptor(data,lens);
//                MultilingualServiceNameDescriptor descriptor = new MultilingualServiceNameDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case STREAM_IDENTIFIER_DESC:{

            }break;
            case CA_IDENTIFIER_DESC: {
                descBase = new CAIdentifierDescriptor(data,lens);
//                CAIdentifierDescriptor descriptor = new CAIdentifierDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case TELETEXT_DESC: {
                descBase = new TeletextDescriptor(data,lens);
//                CAIdentifierDescriptor descriptor = new CAIdentifierDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case SUBTITLE_DESC: {
                descBase = new SubtitlingDescriptor(data,lens);
//                CAIdentifierDescriptor descriptor = new CAIdentifierDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case PRIVATE_DATA_SPECIFIER_DESC: {
                descBase = new PrivateDataSpecifierDescriptor(data,lens);
//                PrivateDataSpecifierDescriptor descriptor = new PrivateDataSpecifierDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case AC3_DESC: {
                descBase = new AC3Descriptor(data,lens);
//                PrivateDataSpecifierDescriptor descriptor = new PrivateDataSpecifierDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case 0x79:{

            }break;
            case EnhancedAC3_DESC: {
                descBase = new EnhancedAC3Descriptor(data,lens);
//                PrivateDataSpecifierDescriptor descriptor = new PrivateDataSpecifierDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
//            case PRIVATE_DESC: {
//
//            }break;
//            case CI_PROTECTION_DESC: {
//
//            }break;
//            case TRUE_VISION_LCN_DESC: {
//
//            }break;
//            case CA_ASSOCIATION_DESC: {
//
//            }break;
//            case NDS_EPG_INFO_DESC: {
//
//            }break;
            case ISDBT_DELIVERY_SYSTEM_DESC: {
                descBase = new ISDBTDeliverySystemDescriptor(data,lens);
            }break;
            case ISDBT_PARTIAL_RECEPTION_DESC: {
                descBase = new ISDBTPartialReceptionDescriptor(data,lens);
            }break;
            case EMERGENCY_INFORMATION_DESC: {
                descBase = new EmergencyInformationDescriptor(data,lens);
                //mDescList.add(descriptor);
            }break;
            case TS_INFORMATION_DESC: {
                descBase = new TSInformationDescriptor(data,lens);
            }break;
            case SHORT_EVENT_DESC: {
                descBase = new ShortEventDescriptor(data);
            }break;
            case EXTENDED_EVENT_DESC: {
                descBase = new ExtendedEventDescriptor(data);
            }break;
            case TIME_SHIFTED_EVENT_DESC: {
                descBase = new ParentalRatingDescriptor(data);
            }break;
            case BOUQUET_NAME_DESC: {
                //Log.d(TAG,"ParsingDescriptor tag = BOUQUET_NAME_DESC");
                descBase = new BouquetNameDescriptor(data);
//                CAIdentifierDescriptor descriptor = new CAIdentifierDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case CHANNEL_DESCRIPTOR: {
                //Log.d(TAG,"ParsingDescriptor tag = CHANNEL_DESCRIPTOR");
                descBase = new ChannelDescriptor(data);
//                CAIdentifierDescriptor descriptor = new CAIdentifierDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case CHANNEL_CATEGORY_DESCRIPTOR: {
                //Log.d(TAG,"ParsingDescriptor tag = CHANNEL_CATEGORY_DESCRIPTOR");
                descBase = new ChannelCategoryDescriptor(data);
//                CAIdentifierDescriptor descriptor = new CAIdentifierDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
            case NETWORK_PRODUCT_LIST_DESCRIPTOR: {
                //Log.d(TAG,"ParsingDescriptor tag = NETWORK_PRODUCT_LIST_DESCRIPTOR");
                descBase = new NetworkProductListDescriptor(data);
//                CAIdentifierDescriptor descriptor = new CAIdentifierDescriptor(data,lens);
//                mDescList.add(descriptor);
            }break;
//            case CHANNEL_PRODUCT_LIST_DESCRIPTOR: {  ==>no implement
//                descBase = new ParentalRatingDescriptor(data);
//                CAIdentifierDescriptor descriptor = new CAIdentifierDescriptor(data,lens);
//                mDescList.add(descriptor);
//            }break;
//            case SERIES_LINKING_DESCRIPTOR: {  ==>eit
//                descBase = new ParentalRatingDescriptor(data);
//                CAIdentifierDescriptor descriptor = new CAIdentifierDescriptor(data,lens);
//                mDescList.add(descriptor);
//            }break;
            default: {
                Log.d(TAG,"unknown or unsupported descriptor [" + tag + "]");
            }break;
        }
        if(descBase != null && descBase.DataExist) {
            mDescList.add(descBase);
            return descBase.DataExist;
        }
        else {
            return false;
        }
    }

    public List<DescBase> getDescriptorList(int tag) {
        List<DescBase> descBaseList = new ArrayList<>();
        for(int i = 0; i < mDescList.size(); i++) {
            if(mDescList.get(i).Tag == tag) {
                descBaseList.add(mDescList.get(i));
            }
        }
        return descBaseList;
    }

    public void desc_sample() {
        int tag = SERVICE_LIST_DESC;
        List<DescBase> list = getDescriptorList(tag);
        for(DescBase temp : list) {
            ServiceListDescriptor desc = (ServiceListDescriptor) temp;
            for(ServiceListDescriptor.Service s : desc.mServiceList) {
                int sid = s.ServiceID ;
            }
        }
    }

    public boolean DigiturkParsingDescriptor(byte[] data,int lens) {
        int tag = toUnsignedInt(data[0]);
        DescBase descBase = null;
        switch(tag) {
            case LOGICAL_CHANNEL_DESC: {
                descBase = new DigiturkLogicalChannelNumberDescriptor(data, lens);
            }break;
            default: {
                Log.d(TAG,"unknown or unsupported DigiturkParsingDescriptor [" + tag + "]");
            }break;
        }
        if(descBase != null && descBase.DataExist) {
            mDescList.add(descBase);
            return descBase.DataExist;
        }
        else {
            return false;
        }
    }
    public boolean CNSParsingDescriptor(byte[] data,int lens, int transportStreamId) {
        int tag = toUnsignedInt(data[0]);
        DescBase descBase = null;
        switch(tag) {
            case CNS_LOGICAL_CHANNEL_DESC: {
                //Log.d(TAG,"CNS_LOGICAL_CHANNEL_DESC");
                descBase = new CNSLogicalChannelNumberDescriptor(data, lens, transportStreamId);
            }break;
            //case CNS_EXT_LOGICAL_CHANNEL_DESC: {
            //    Log.d(TAG,"CNS_EXT_LOGICAL_CHANNEL_DESC");
            //   descBase = new CNSExtLogicalChannelNumberDescriptor(data, lens);
            //}break;
            default: {
                Log.d(TAG,"unknown or unsupported CNSParsingDescriptor [" + tag + "]");
            }break;
        }
        if(descBase != null && descBase.DataExist) {
            mDescList.add(descBase);
            return descBase.DataExist;
        }
        else {
            return false;
        }
    }
}
