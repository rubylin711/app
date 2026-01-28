package com.prime.dtv.service.Table;

import static com.prime.dtv.service.Table.Desciptor.Descriptor.*;
import static com.prime.dtv.service.Util.Utils.*;

import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;


import com.prime.dtv.service.Table.Desciptor.*;
import com.prime.dtv.service.Table.Desciptor.DescBase;
import com.prime.dtv.service.Table.Desciptor.Descriptor;

public class BatData extends TableData {
    private static final String TAG = "BatData";
    private static int mBouquetId;
    private static int mVersion;
    private static int mBouquetNameDescriptorsLength;
    private static int mTransportStreamLoopLength;
    private List<BatData.BouquetNameStream> mBouquetNameStreamList = new ArrayList<BatData.BouquetNameStream>();
    private List<BatData.TransportStream> mTransportStreamList = new ArrayList<BatData.TransportStream>();

    public int getVersion() {
        return mVersion;
    }

    public int getBouquetId() {
        return mBouquetId;
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

    public int getTransportStreamListSize() {
        return mTransportStreamList.size();
    }

    public List<BatData.TransportStream> getTransportStreamList() {
        return mTransportStreamList;
    }

    private void buildBouquetNameStreamList(final byte[] data, final int offset, final int length) {
        int t=0, desc_length;
        final BatData.BouquetNameStream bouquetNameStream_data = new BatData.BouquetNameStream();
        //Log.d(TAG,"buildBouquetNameStreamList = " + mBouquetNameDescriptorsLength);
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

    private void buildTransportStreamList(final byte[] data, final int offset, final int length) {
        int t=0, transportStreamLoopLen,j,desc_length;
        while(t<length){
            final BatData.TransportStream transportStream_data = new BatData.TransportStream();

            transportStream_data.mTransportStreamId = getInt(data, offset+t, 2, MASK_16BITS);
            transportStream_data.mOriginalNetworkId = getInt(data, offset+t+2, 2, MASK_16BITS);
            //Log.d(TAG,"TransportStreamId = " + transportStream_data.mTransportStreamId);
            //Log.d(TAG,"OriginalNetworkId = " + transportStream_data.mOriginalNetworkId);
            //desc_length = toUnsignedInt(data[offset+t+4]);
            transportStreamLoopLen=getInt(data, offset+t+4, 2, MASK_12BITS);
            //Log.d(TAG,"TransportStreamLoopLen = " + transportStreamLoopLen);
            for(j=0; j<transportStreamLoopLen; ) {
                desc_length = toUnsignedInt(data[offset+t+6+j+1]);
                byte[] tempData = new byte[desc_length+2];
                System.arraycopy(data, offset+t+6+j, tempData, 0, desc_length + 2);
                transportStream_data.mDescriptor.ParsingDescriptor(tempData, desc_length + 2);
                j+=(desc_length+2);
            }
            t+=6+transportStreamLoopLen;
            mTransportStreamList.add(transportStream_data);
        }
    }

    public void parsing(byte[] data, int lens) {
        //Log.d(TAG,"parsing()");
        try {
            mBouquetId = getInt(data, 3, 2, MASK_16BITS);
            //mVersion=getInt(data, 5, 1, MASK_5BITS);
            mVersion = (toUnsignedInt(data[5]) & 0x3e) >> 1;
            mBouquetNameDescriptorsLength = getInt(data, 8, 2, MASK_12BITS);
            Log.d(TAG, "mBouquetNameDescriptorsLength = " + mBouquetNameDescriptorsLength);
            if (mBouquetNameDescriptorsLength > data.length) {
                Log.e(TAG, "The data is wrong!!!!!!!!!!!!!!!!");
                return;
            }
            mTransportStreamLoopLength = getInt(data, 10 + mBouquetNameDescriptorsLength, 2, MASK_12BITS);
            Log.d(TAG, "mTransportStreamLoopLength = " + mTransportStreamLoopLength);
            if (mTransportStreamLoopLength > data.length) {
                Log.e(TAG, "The data is wrong!!!!!!!!!!!!!!!!");
                return;
            }
            //Log.d(TAG,"mBouquetNameDescriptorsLength = " + mBouquetNameDescriptorsLength);
            //Log.d(TAG,"mTransportStreamLoopLength = " + mTransportStreamLoopLength);
            buildBouquetNameStreamList(data, 8 + 2, mBouquetNameDescriptorsLength);
            buildTransportStreamList(data, 12 + mBouquetNameDescriptorsLength, mTransportStreamLoopLength);
        } catch (Exception e) {
            Log.e(TAG, "e = "+e);
            e.printStackTrace();
        }
    }
}
