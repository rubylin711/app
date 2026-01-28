package com.prime.dtv.service.Table;

import static com.prime.dtv.service.Table.Desciptor.Descriptor.*;
import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import com.prime.dtv.service.Table.Desciptor.*;
import com.prime.dtv.service.Table.Desciptor.DescBase;
import com.prime.dtv.service.Table.Desciptor.Descriptor;

import java.util.ArrayList;
import java.util.List;


public class NitData extends TableData {
    private static final String TAG = "NitData";
    private int mNetworkDescriptorsLength;
    private int mTransportStreamLoopLength;
    private List<NetworktStream> mnetworkstreamList = new ArrayList<NetworktStream>();
    private List<TransportStream> mtransportStreamList = new ArrayList<TransportStream>();

    public class NetworktStream {
        private int mVersion;
        private int mNetworkID;
        private Descriptor mDescriptor = new Descriptor();

        //public List<Descriptor> getDescriptorList() {
        //    return mdescriptorList;
        //}
        public int getVersion() { return mVersion; }
        public int getNetworkID() { return mNetworkID; }
        public Descriptor getDescriptor() {
            return mDescriptor;
        }

    }

    public class TransportStream {
        private int mTransportStreamID;
        private int mOriginalNetworkID;
        private int mTransportDescriptorsLength;
        private Descriptor mDescriptor = new Descriptor();

        public List<DescBase> getDescriptorList() {
            return mDescriptor.getDescList();
        }

        public int getTransportStreamID() {
            return mTransportStreamID;
        }
        public int getOriginalNetworkID() {
            return mOriginalNetworkID;
        }
        public int getTransportDescriptorsLength() {
            return mTransportDescriptorsLength;
        }
        public Descriptor getDescriptor() {
            return mDescriptor;
        }
        public int getdescTag(){

            List<DescBase> descBase_list = mDescriptor.getDescriptorList(CABLE_DELIVERY_SYSTEM_DESC);
            if(!descBase_list.isEmpty())
            {
                return CABLE_DELIVERY_SYSTEM_DESC;
            }
            descBase_list = mDescriptor.getDescriptorList(TERRESTRIAL_DELIVERY_SYSTEM_DESC);
            if(!descBase_list.isEmpty())
            {
                return TERRESTRIAL_DELIVERY_SYSTEM_DESC;
            }
            descBase_list = mDescriptor.getDescriptorList(SATELLITE_DELIVERY_SYSTEM_DESC);
            if(!descBase_list.isEmpty())
            {
                return SATELLITE_DELIVERY_SYSTEM_DESC;
            }
            descBase_list = mDescriptor.getDescriptorList(ISDBT_DELIVERY_SYSTEM_DESC);
            if(!descBase_list.isEmpty())
            {
                return ISDBT_DELIVERY_SYSTEM_DESC;
            }
            return 0;
        }

        public int getFreq(int desc_tag){
            int freq;

            List<DescBase> descBase_list = mDescriptor.getDescriptorList(desc_tag);
            if (!descBase_list.isEmpty()) {
                for (DescBase temp : descBase_list) {
                    switch(desc_tag){
                        case CABLE_DELIVERY_SYSTEM_DESC: {
                            CableDeliverySystemDescriptor tempDescriptor = (CableDeliverySystemDescriptor) temp;
                            freq = tempDescriptor.Frequency;
                        }
                        break;
                        case TERRESTRIAL_DELIVERY_SYSTEM_DESC: {
                            TerrestrialDeliverySystemDescriptor tempDescriptor = (TerrestrialDeliverySystemDescriptor) temp;
                            freq = tempDescriptor.CentreFrequency;
                        }
                        break;
                        case SATELLITE_DELIVERY_SYSTEM_DESC: {
                            SatelliteDeliverySystemDescriptor tempDescriptor = (SatelliteDeliverySystemDescriptor) temp;
                            freq = tempDescriptor.Frequency;
                        }
                        break;
                        default:
                            freq = 0;
                            break;
                    }
                    Log.d(TAG, "Frequency = " + freq);
                    return freq;
                }
            }
            Log.w(TAG, "not find desc_tag "+desc_tag);
            return 0;
        }

        public int getSymbolrate(int desc_tag){
            int symbol_rate;

            List<DescBase> descBase_list = mDescriptor.getDescriptorList(desc_tag);
            if (!descBase_list.isEmpty()) {
                for (DescBase temp : descBase_list) {
                    switch(desc_tag){
                        case CABLE_DELIVERY_SYSTEM_DESC: {
                            CableDeliverySystemDescriptor tempDescriptor = (CableDeliverySystemDescriptor) temp;
                            symbol_rate = tempDescriptor.SymbolRate;
                        }
                        break;
                        case SATELLITE_DELIVERY_SYSTEM_DESC: {
                            SatelliteDeliverySystemDescriptor tempDescriptor = (SatelliteDeliverySystemDescriptor) temp;
                            symbol_rate = tempDescriptor.SymbolRate;
                        }
                        break;
                        default:
                            Log.w(TAG, "desc_tag "+desc_tag);
                            symbol_rate = 0;
                            break;
                    }
                    Log.d(TAG, "symbol_rate = " + symbol_rate);
                    return symbol_rate;
                }
            }
            Log.w(TAG, "not find desc_tag "+desc_tag);
            return 0;
        }

        public int getQam(){
            List<DescBase> descBase_list = mDescriptor.getDescriptorList(CABLE_DELIVERY_SYSTEM_DESC);
            if (!descBase_list.isEmpty()) {
                for (DescBase temp : descBase_list) {
                    CableDeliverySystemDescriptor tempDescriptor = (CableDeliverySystemDescriptor) temp;
                    Log.d(TAG, "qam = " + tempDescriptor.Qam);
                    return tempDescriptor.Qam;
                }
            }
            Log.w(TAG, "not find cable desc, return 0");
            return 0;
        }

        public int getPolarization(){
            List<DescBase> descBase_list = mDescriptor.getDescriptorList(SATELLITE_DELIVERY_SYSTEM_DESC);
            if (!descBase_list.isEmpty()) {
                for (DescBase temp : descBase_list) {
                    SatelliteDeliverySystemDescriptor tempDescriptor = (SatelliteDeliverySystemDescriptor) temp;
                    Log.d(TAG, "polarization = " + tempDescriptor.Polarization);
                    return tempDescriptor.Polarization;
                }
            }
            Log.w(TAG, "not match find satellite desc, return 0");
            return 0;
        }

        public int getFECinner(){
            List<DescBase> descBase_list = mDescriptor.getDescriptorList(SATELLITE_DELIVERY_SYSTEM_DESC);
            if (!descBase_list.isEmpty()) {
                for (DescBase temp : descBase_list) {
                    SatelliteDeliverySystemDescriptor tempDescriptor = (SatelliteDeliverySystemDescriptor) temp;
                    Log.d(TAG, "fec_inner = " + tempDescriptor.FECInner);
                    return tempDescriptor.FECInner;
                }
            }
            Log.w(TAG, "not match find satellite desc, return 0");
            return 0;
        }

        public int getFFT_mode(){
            List<DescBase> descBase_list = mDescriptor.getDescriptorList(TERRESTRIAL_DELIVERY_SYSTEM_DESC);
            if (!descBase_list.isEmpty()) {
                for (DescBase temp : descBase_list) {
                    TerrestrialDeliverySystemDescriptor tempDescriptor = (TerrestrialDeliverySystemDescriptor) temp;
                    Log.d(TAG, "FFT_mode = " + tempDescriptor.FTTMode);
                    return tempDescriptor.FTTMode;
                }
            }
            Log.w(TAG, "not match find terrestrial desc, return 0");
            return 0;
        }

        public int getConstellation(){
            List<DescBase> descBase_list = mDescriptor.getDescriptorList(TERRESTRIAL_DELIVERY_SYSTEM_DESC);
            if (!descBase_list.isEmpty()) {
                for (DescBase temp : descBase_list) {
                    TerrestrialDeliverySystemDescriptor tempDescriptor = (TerrestrialDeliverySystemDescriptor) temp;
                    Log.d(TAG, "Constellation = " + tempDescriptor.Constellation);
                    return tempDescriptor.Constellation;
                }
            }
            Log.w(TAG, "not match find terrestrial desc, return 0");
            return 0;
        }

        public int getHierarchy(){
            List<DescBase> descBase_list = mDescriptor.getDescriptorList(TERRESTRIAL_DELIVERY_SYSTEM_DESC);
            if (!descBase_list.isEmpty()) {
                for (DescBase temp : descBase_list) {
                    TerrestrialDeliverySystemDescriptor tempDescriptor = (TerrestrialDeliverySystemDescriptor) temp;
                    Log.d(TAG, "Hierarchy = " + tempDescriptor.Hierarchy);
                    return tempDescriptor.Hierarchy;
                }
            }
            Log.w(TAG, "not match find terrestrial desc, return 0");
            return 0;
        }

        public int getGuardInterval(){
            List<DescBase> descBase_list = mDescriptor.getDescriptorList(TERRESTRIAL_DELIVERY_SYSTEM_DESC);
            if (!descBase_list.isEmpty()) {
                for (DescBase temp : descBase_list) {
                    TerrestrialDeliverySystemDescriptor tempDescriptor = (TerrestrialDeliverySystemDescriptor) temp;
                    Log.d(TAG, "GuardInterval = " + tempDescriptor.GuardInterval);
                    return tempDescriptor.GuardInterval;
                }
            }
            Log.w(TAG, "not match find terrestrial desc, return 0");
            return 0;
        }

        public int getCodeRateH(){
            List<DescBase> descBase_list = mDescriptor.getDescriptorList(TERRESTRIAL_DELIVERY_SYSTEM_DESC);
            if (!descBase_list.isEmpty()) {
                for (DescBase temp : descBase_list) {
                    TerrestrialDeliverySystemDescriptor tempDescriptor = (TerrestrialDeliverySystemDescriptor) temp;
                    Log.d(TAG, "CodeRateH = " + tempDescriptor.CodeRateH);
                    return tempDescriptor.CodeRateH;
                }
            }
            Log.w(TAG, "not match find terrestrial desc, return 0");
            return 0;
        }

        public int getISDBT_freqnum(){
            List<DescBase> descBase_list = mDescriptor.getDescriptorList(ISDBT_DELIVERY_SYSTEM_DESC);
            if (!descBase_list.isEmpty()) {
                for (DescBase temp : descBase_list) {
                    ISDBTDeliverySystemDescriptor tempDescriptor = (ISDBTDeliverySystemDescriptor) temp;
                    Log.d(TAG, "Freq_list_num = " + tempDescriptor.Freq_list_num);
                    return tempDescriptor.Freq_list_num;
                }
            }
            Log.w(TAG, "not match find isdbr desc, return 0");
            return 0;
        }

        public int getISDBT_freqency(int index){
            List<DescBase> descBase_list = mDescriptor.getDescriptorList(ISDBT_DELIVERY_SYSTEM_DESC);
            if (!descBase_list.isEmpty()) {
                for (DescBase temp : descBase_list) {
                    ISDBTDeliverySystemDescriptor tempDescriptor = (ISDBTDeliverySystemDescriptor) temp;
                    if(!tempDescriptor.mfreqList.isEmpty()) {
                        Log.d(TAG, "Freqency = " + tempDescriptor.mfreqList.get(index));
                        return tempDescriptor.mfreqList.get(index).ISDBT_Frequency;
                    }
                }
            }
            Log.w(TAG, "not match find isdbr desc, return 0");
            return 0;
        }

    }

    public List<NetworktStream> getNetworkStreamList() {
        return mnetworkstreamList;
    }
    public List<TransportStream> getTransportStreamList() {
        return mtransportStreamList;
    }
    public int getNumOfTransportStream() {
        return mtransportStreamList.size();
    }
    public TransportStream getTransportStream(final int streamID) {
        for(final TransportStream tStream:mtransportStreamList){
            if(tStream.mTransportStreamID ==streamID){
                return tStream;
            }
        }
        return null;
    }

    public int getLCN(int serviceID, int onID){
        for(final TransportStream tStream:mtransportStreamList)
        {
            List<DescBase> descBase_list = tStream.mDescriptor.getDescriptorList(LOGICAL_CHANNEL_DESC);
            if(!descBase_list.isEmpty())
            {
                for(DescBase temp : descBase_list)
                {
                    LogicalChannelNumberDescriptor tempDescriptor = (LogicalChannelNumberDescriptor) temp;
                    for (final LogicalChannelNumberDescriptor.LogicalChannelNumber lcn : tempDescriptor.mLogicalChannelNumberList) {
                        if (lcn.ServiceId == serviceID && tStream.mOriginalNetworkID == onID)
                            return lcn.LogicalChannelMumber;

                    }
                }
            }
        }
        return 0;
    }

    private void buildNetworkStreamList(final byte[] data, final int offset, final int length) {
        int t = 0, desc_length;
        final NetworktStream networktStream_data = new NetworktStream();

        networktStream_data.mVersion = (toUnsignedInt(data[offset + 5]) & 0x3e) >> 1;
        networktStream_data.mNetworkID = getInt(data, offset + 3, 2, MASK_16BITS);
        //Log.d(TAG, "mVersion="+networktStream_data.mVersion+" mNetworkID="+networktStream_data.mNetworkID);
        while (t < length) {
            desc_length = toUnsignedInt(data[offset + 10 + t + 1]);
            byte[] tempData = new byte[desc_length + 2];
            System.arraycopy(data, offset + 10 + t, tempData, 0, desc_length + 2);
            networktStream_data.mDescriptor.ParsingDescriptor(tempData, desc_length + 2);
            t += (desc_length + 2);
            //Log.d(TAG, "length="+length);
            //Log.d(TAG, "desc_length="+desc_length+" t= "+t);
        }
        mnetworkstreamList.add(networktStream_data);
    }

    private void buildTransportStreamList(final byte[] data, final int offset, final int programInfoLength) {
        int t = 0, j, desc_length;
        while (t < programInfoLength) {
            final TransportStream transportStream_data = new TransportStream();
            transportStream_data.mTransportStreamID = getInt(data, offset + t, 2, MASK_16BITS);
            transportStream_data.mOriginalNetworkID = getInt(data, offset + t + 2, 2, MASK_16BITS);
            transportStream_data.mTransportDescriptorsLength = getInt(data, offset + t + 4, 2, MASK_12BITS);
            //Log.d(TAG, "mTransportStreamID="+transportStream_data.mTransportStreamID+" mOriginalNetworkID="+transportStream_data.mOriginalNetworkID);
            //Log.d(TAG, "mTransportDescriptorsLength="+transportStream_data.mTransportDescriptorsLength);
            for (j = 0; j < transportStream_data.mTransportDescriptorsLength; ) {
                desc_length = toUnsignedInt(data[offset + t + 6 + j + 1]);
                byte[] tempData = new byte[desc_length + 2];
                System.arraycopy(data, offset + t + 6 + j, tempData, 0, desc_length + 2);
                transportStream_data.mDescriptor.ParsingDescriptor(tempData, desc_length + 2);
                j += (desc_length + 2);
            }
            t += 6 + transportStream_data.mTransportDescriptorsLength;
            mtransportStreamList.add(transportStream_data);
        }
    }

    @Override
    public void parsing(byte[] data, int lens) {
        try {
            mNetworkDescriptorsLength = getInt(data, 8, 2, MASK_12BITS);
            Log.d(TAG, "mNetworkDescriptorsLength = " + mNetworkDescriptorsLength);
            if (mNetworkDescriptorsLength > data.length) {
                Log.e(TAG, "The data is wrong!!!!!!!!!!!!!!!!");
                return;
            }
            mTransportStreamLoopLength = getInt(data, 10 + mNetworkDescriptorsLength, 2, MASK_12BITS);
            Log.d(TAG, "mTransportStreamLoopLength = " + mTransportStreamLoopLength);
            if (mTransportStreamLoopLength > data.length) {
                Log.e(TAG, "The data is wrong!!!!!!!!!!!!!!!!");
                return;
            }
            //sLog.d(TAG, "mNetworkDescriptorsLength="+mNetworkDescriptorsLength+" mTransportStreamLoopLength="+mTransportStreamLoopLength);

            buildNetworkStreamList(data, 0, mNetworkDescriptorsLength);
            buildTransportStreamList(data, 12 + mNetworkDescriptorsLength, mTransportStreamLoopLength);
        } catch (Exception e) {
            Log.e(TAG, "e = "+e);
            e.printStackTrace();
        }
    }
}
