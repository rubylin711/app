package com.prime.dtv.service.Table;

import static com.prime.dtv.service.Table.Desciptor.Descriptor.*;
import static com.prime.dtv.service.Util.Utils.*;

import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;


import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.service.Table.Desciptor.*;
import com.prime.dtv.service.Table.Desciptor.DescBase;
import com.prime.dtv.service.Table.Desciptor.Descriptor;

public class BatData extends TableData {
    private static final String TAG = "BatData";
    private int mBouquetId;
    private int mVersion;
    private int mBouquetNameDescriptorsLength;
    private int mBouquetDescriptorsLength;
    private int mExtLCNDescriptorsLength;
    private int mTransportStreamLoopLength;
    private List<BatData.BouquetNameStream> mBouquetNameStreamList = new ArrayList<BatData.BouquetNameStream>();
    private List<BatData.ExtLCNStream> mExtLCNStreamList = new ArrayList<BatData.ExtLCNStream>();
    private List<BatData.TransportStream> mTransportStreamList = new ArrayList<BatData.TransportStream>();

    public int getVersion() {
        return mVersion;
    }

    public int getBouquetId() {
        return mBouquetId;
    }
    public List<BatData.ExtLCNStream> getExtLCNStreamList() {
        return mExtLCNStreamList;
    }
    public class BouquetNameStream {//bouquet_name_descriptor
        //private int mVersion;
        //private int mBouquetId;
        private Descriptor mDescriptor = new Descriptor();

        public String getBouquetName() {
            List<DescBase> descBase = mDescriptor.getDescriptorList(BOUQUET_NAME_DESC);
            for (int i = 0; i < descBase.size(); i++) {
                BouquetNameDescriptor bouquetNameDescriptor = (BouquetNameDescriptor) descBase.get(i);
                return bouquetNameDescriptor.BouquetName;
            }
            return null;
        }

        public Descriptor getDescriptor() {
            return mDescriptor;
        }
    }
    public class ExtLCNStream {
        private Descriptor mDescriptor = new Descriptor();

        public int getExtLCNDescriptorSize() {
            List<DescBase> descBase_list = mDescriptor.getDescriptorList(Descriptor.CNS_EXT_LOGICAL_CHANNEL_DESC);
            return descBase_list.size();
        }

        // 支援多個 ExtLCN Descriptor
        public List<CNSExtLogicalChannelNumberDescriptor.ExtLogicalChannelNumber> getExtLCNList() {
            List<CNSExtLogicalChannelNumberDescriptor.ExtLogicalChannelNumber> all = new ArrayList<>();
            List<DescBase> descBase_list = mDescriptor.getDescriptorList(Descriptor.CNS_EXT_LOGICAL_CHANNEL_DESC);

            for (DescBase base : descBase_list) {
                if (base instanceof CNSExtLogicalChannelNumberDescriptor) {
                    CNSExtLogicalChannelNumberDescriptor desc = (CNSExtLogicalChannelNumberDescriptor) base;
                    all.addAll(desc.mExtLogicalChannelNumberList);
                }
            }
            return all;
        }

        public Descriptor getDescriptor() {
            return mDescriptor;
        }
    }

    public List<CNSExtLogicalChannelNumberDescriptor.ExtLogicalChannelNumber> getAllExtLCNList() {
        List<CNSExtLogicalChannelNumberDescriptor.ExtLogicalChannelNumber> ExtLCNList = new ArrayList<>();
        for (ExtLCNStream stream : mExtLCNStreamList) {
            ExtLCNList.addAll(stream.getExtLCNList());
        }
        return ExtLCNList;
    }
    public class TransportStream {

        private int mTransportStreamId;
        private int mOriginalNetworkId;
        private Descriptor mDescriptor = new Descriptor();

        public int getTransportStreamId() {
            return mTransportStreamId;
        }

        public int getOriginalNetworkId() {
            return mOriginalNetworkId;
        }

        public int getServiceListDescriptorSize() {
            List<DescBase> descBase_list=mDescriptor.getDescriptorList(SERVICE_LIST_DESC);
            return descBase_list.size();
        }

        public List<ServiceListDescriptor.Service> getServiceList() {

            List<DescBase> descBase_list=mDescriptor.getDescriptorList(SERVICE_LIST_DESC);
            ServiceListDescriptor tempDescriptor= (ServiceListDescriptor) descBase_list.get(0);
            return tempDescriptor.mServiceList;
        }

        public Descriptor getDescriptor() {
            return mDescriptor;
        }

        public List<DescBase> getDescriptorList() {
            return mDescriptor.getDescList();
        }
    }

    public int getBouquetNameStreamListSize() {
        return mBouquetNameStreamList.size();
    }

    public List<BatData.BouquetNameStream> getBouquetNametreamList() {
        return mBouquetNameStreamList;
    }
    public int getExtLCNStreamListSize() {
        return mExtLCNStreamList.size();
    }

    public int getTransportStreamListSize() {
        return mTransportStreamList.size();
    }

    public List<BatData.TransportStream> getTransportStreamList() {
        return mTransportStreamList;
    }

    private void buildBouquetNameStreamList(final byte[] data, final int offset, final int length) {
        int t=0, desc_length;
        final BatData.BouquetNameStream bouquetNameStream_data = new BatData.BouquetNameStream();
        //Log.d(TAG,"buildBouquetNameStreamList = " + mBouquetDescriptorsLength);
        //Log.d(TAG,"length = " + length);
        while(t<length){
            desc_length = toUnsignedInt(data[offset+t+1]);
            //Log.d(TAG,"desc_length = " + desc_length + " Tag = " + toUnsignedInt(data[offset+t]));
            byte[] tempData = new byte[desc_length+2];
            System.arraycopy(data, offset+t, tempData, 0, desc_length + 2);
            //Log.d(TAG," Tag = " + toUnsignedInt(tempData[0]));
            //Log.d(TAG,"desc_length = " + toUnsignedInt(tempData[1]));
            bouquetNameStream_data.mDescriptor.ParsingDescriptor(tempData, desc_length + 2);
            t+=(desc_length+2);
        }
        mBouquetNameStreamList.add(bouquetNameStream_data);
    }
    private void buildExtLCNStreamList(final byte[] data, final int offset, final int length) {
        int t=0, desc_length;
        final BatData.ExtLCNStream extLCNStream_data = new BatData.ExtLCNStream();
        //Log.d(TAG,"buildExtLCNStreamList = " + mExtLCNDescriptorsLength);
        //Log.d(TAG,"length = " + length);
        while(t<length){
            desc_length = toUnsignedInt(data[offset+t+1]);
            //Log.d(TAG,"desc_length = " + desc_length + " Tag = " + toUnsignedInt(data[offset+t]));
            byte[] tempData = new byte[desc_length+2];
            System.arraycopy(data, offset+t, tempData, 0, desc_length + 2);
            //Log.d(TAG," Tag = " + toUnsignedInt(tempData[0]));
            //Log.d(TAG,"desc_length = " + toUnsignedInt(tempData[1]));
            extLCNStream_data.mDescriptor.ParsingDescriptor(tempData, desc_length + 2);
            t+=(desc_length+2);
        }
        mExtLCNStreamList.add(extLCNStream_data);
    }
    private void buildTransportStreamList(final byte[] data, final int offset, final int length) {
        int t=0, transportStreamLoopLen,j,desc_length;
        Log.d(TAG,"buildTransportStreamList length = " + length);
        while(t<length){
            final BatData.TransportStream transportStream_data = new BatData.TransportStream();

            transportStream_data.mTransportStreamId = getInt(data, offset+t, 2, MASK_16BITS);
            transportStream_data.mOriginalNetworkId = getInt(data, offset+t+2, 2, MASK_16BITS);
            Log.d(TAG,"TransportStreamId = " + transportStream_data.mTransportStreamId);
            Log.d(TAG,"OriginalNetworkId = " + transportStream_data.mOriginalNetworkId);
            desc_length = toUnsignedInt(data[offset+t+4]);
            transportStreamLoopLen=getInt(data, offset+t+4, 2, MASK_12BITS);
            Log.d(TAG,"TransportStreamLoopLen = " + transportStreamLoopLen);
            for(j=0; j<transportStreamLoopLen; ) {
                desc_length = toUnsignedInt(data[offset+t+6+j+1]);
                byte[] tempData = new byte[desc_length+2];
                System.arraycopy(data, offset+t+6+j, tempData, 0, desc_length + 2);
                if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DIGITURK)) {
                    if (toUnsignedInt(tempData[0]) == LOGICAL_CHANNEL_DESC)
                        transportStream_data.mDescriptor.DigiturkParsingDescriptor(tempData, desc_length + 2);
                    else
                        transportStream_data.mDescriptor.ParsingDescriptor(tempData, desc_length + 2);
                }
                else if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS))
                {
                    if (toUnsignedInt(tempData[0]) == CNS_LOGICAL_CHANNEL_DESC)
                        transportStream_data.mDescriptor.CNSParsingDescriptor(tempData, desc_length + 2, transportStream_data.mTransportStreamId);
                    else
                        transportStream_data.mDescriptor.ParsingDescriptor(tempData, desc_length + 2);
                }
                else {
                    transportStream_data.mDescriptor.ParsingDescriptor(tempData, desc_length + 2);
                }
                j+=(desc_length+2);
                Log.d(TAG,"j = " + j);
            }
            t+=6+transportStreamLoopLen;
            Log.d(TAG,"t = " + t);
            mTransportStreamList.add(transportStream_data);
        }
    }

    public void parsing(byte[] data, int lens) {
        //Log.d(TAG,"parsing()");
        try {
            mBouquetId = getInt(data, 3, 2, MASK_16BITS);
            //mVersion=getInt(data, 5, 1, MASK_5BITS);
            mVersion = (toUnsignedInt(data[5]) & 0x3e) >> 1;
            mBouquetDescriptorsLength = getInt(data, 8, 2, MASK_12BITS);
            Log.d(TAG, "mBouquetDescriptorsLength = " + mBouquetDescriptorsLength);
            if (mBouquetDescriptorsLength > data.length) {
                Log.e(TAG, "The data is wrong!!!!!!!!!!!!!!!!");
                return;
            }
            mBouquetNameDescriptorsLength = getInt(data, 11, 1, MASK_8BITS);

            if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS))
            {
                if(CNS_EXT_LOGICAL_CHANNEL_DESC == getInt(data, 12+mBouquetNameDescriptorsLength, 1, MASK_8BITS)) {
                    mExtLCNDescriptorsLength = getInt(data, 12 + mBouquetNameDescriptorsLength + 1, 1, MASK_8BITS);
                    if (mExtLCNDescriptorsLength > data.length) {
                        Log.e(TAG, "The data is wrong!!!!!!!!!!!!!!!!");
                        return;
                    }
                    Log.d(TAG, "mExtLCNDescriptorsLength = " + mExtLCNDescriptorsLength);
                }
                else
                    mExtLCNDescriptorsLength = 0;
            }
            mTransportStreamLoopLength = getInt(data, 10 + mBouquetDescriptorsLength, 2, MASK_12BITS);
            Log.d(TAG, "mTransportStreamLoopLength = " + mTransportStreamLoopLength);
            if (mTransportStreamLoopLength > data.length) {
                Log.e(TAG, "The data is wrong!!!!!!!!!!!!!!!!");
                return;
            }
            //Log.d(TAG,"mBouquetDescriptorsLength = " + mBouquetDescriptorsLength);
            //Log.d(TAG,"mTransportStreamLoopLength = " + mTransportStreamLoopLength);
            buildBouquetNameStreamList(data, 8 + 2, mBouquetNameDescriptorsLength);
            if(mExtLCNDescriptorsLength>0)
                buildExtLCNStreamList(data, 12 + mBouquetNameDescriptorsLength, mExtLCNDescriptorsLength);
            buildTransportStreamList(data, 12 + mBouquetDescriptorsLength, mTransportStreamLoopLength);
        } catch (Exception e) {
            Log.e(TAG, "e = "+e);
            e.printStackTrace();
        }
    }
}
