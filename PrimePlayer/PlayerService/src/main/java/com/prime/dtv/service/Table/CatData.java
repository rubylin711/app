package com.prime.dtv.service.Table;

import static com.prime.dtv.service.Table.Desciptor.Descriptor.*;
import static com.prime.dtv.service.Util.Utils.*;

import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;


import com.prime.dtv.service.Table.Desciptor.*;
import com.prime.dtv.service.Table.Desciptor.DescBase;
import com.prime.dtv.service.Table.Desciptor.Descriptor;

public class CatData extends TableData {
    private static final String TAG = "CatData";
    private static int mVersion=-1;
    private byte [] mRawData = null;
    private int mRawDataLens = 0;
    private List<CaEmmData> mCaEmmDataList = new ArrayList<CaEmmData>();
    private int mVersion_tmp;
    public int getVersion() {
        return mVersion;
    }
    public List<CaEmmData> getCaEmmDataList() {
        return mCaEmmDataList;
    }
    public int getNumOfCaEmmData() {
        return mCaEmmDataList.size();
    }
    public CaEmmData getCaEmmDataByIndex(int i) {
        if(i >= mCaEmmDataList.size())
            return null;
        else
            return mCaEmmDataList.get(i);
    }

    public class CaEmmData {
        private int mCaSystemId=-1;
        private int mEmmPid=-1;
        private Descriptor mDescriptor = new Descriptor();

        public int getCaSystemId() {
            return mCaSystemId;
        }

        public int getEmmPid() {
            return mEmmPid;
        }

        public byte[] getPrivateData() {
            List<DescBase> descBase = mDescriptor.getDescriptorList(CA_DESC);
            if(descBase.size() == 0){
                return null;
            }
            else {
                CADescriptor privateDescriptor = (CADescriptor) descBase.get(0);
                return privateDescriptor.getPrivateDataByte();
            }
        }

        public Descriptor getDescriptor() {
            return mDescriptor;
        }
    }

    /*
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
    */

    private int buildCASystemDataList(final byte[] data, final int offset, final int length) {
        int t=0, desc_length,desc_tag,parsing_len;

        CaEmmData caSystemData;
        parsing_len=0;

        while(t<length){
            desc_tag = toUnsignedInt(data[offset+t]);
            desc_length = toUnsignedInt(data[offset+t+1]);
            if(desc_tag == CA_DESC) {
                byte[] tempData = new byte[desc_length + 2];
                System.arraycopy(data, offset + t, tempData, 0, desc_length + 2);
                caSystemData=null;
                caSystemData = new CaEmmData();
                caSystemData.mDescriptor.ParsingDescriptor(tempData, desc_length + 2);
                parsing_len=parsing_len+desc_length+2;
                caSystemData.mCaSystemId=getInt(data, offset+t+2, 2, MASK_16BITS);
                caSystemData.mEmmPid=getInt(data, offset+t+4, 2, MASK_13BITS);
                mCaEmmDataList.add(caSystemData);
            }
            t+=(desc_length+2);
        }
        //for(t=0;t<mCaEmmDataList.size();t++) {
        //    LogUtils.d("Index = "+t);
        //    LogUtils.d("Ca System Id = "+mCaEmmDataList.get(t).getCaSystemId());
        //    LogUtils.d("Emm Pid = "+mCaEmmDataList.get(t).getEmmPid());
        //}
        return parsing_len;
    }

    public byte[] getRawData(){
        if(mRawData != null){
            return mRawData;
        }
        return null;
    }
    public int getRawDataLen(){
        return mRawDataLens;
    }

    public void parsing(byte[] data, int lens) {
        try {
            int SectionLength, TotalDescLen, CaDescLen;
            mVersion_tmp=(toUnsignedInt(data[5]) & 0x3e) >>1;
            //LogUtils.d("@@@### mVersion_tmp = "+mVersion_tmp+", mVersion = "+mVersion);
//            if(mVersion_tmp == mVersion)
//                return;
            if(mRawData == null) {
                mRawData = new byte[lens];
                mRawDataLens = lens;
                System.arraycopy(data, 0, mRawData, 0, lens);
            }
            else{
                byte [] raw_data_tmp= new byte[lens+mRawDataLens];
                System.arraycopy(mRawData, 0, raw_data_tmp, 0, mRawDataLens);
                System.arraycopy(data, 0, raw_data_tmp, mRawDataLens, lens);
                mRawDataLens = mRawDataLens + lens;
                mRawData = raw_data_tmp;
            }
            mVersion = (toUnsignedInt(data[5]) & 0x3e) >> 1;
            SectionLength = getInt(data, 1, 2, MASK_12BITS);
            TotalDescLen = SectionLength - 4 - 5;//TotalDescLen=SectionLength-(CRC+The remaining header after the section_length field)
            CaDescLen = buildCASystemDataList(data, 8, TotalDescLen);
        } catch (Exception e) {
            Log.e(TAG, "e = "+e);
            e.printStackTrace();
        }
    }
}
/*
CA_section() {
	table_id	                    8	uimsbf
	section_syntax_indicator	    1	bslbf
	'0'	                            1	bslbf
	reserved	                    2	bslbf
	section_length	                12	uimsbf
	reserved	                    18	bslbf
	version_number	                5	uimsbf
	current_next_indicator	        1	bslbf
	section_number	                8	uimsbf
	last_section_number	            8	uimsbf
	for (i=0; i<N;i++) {
		descriptor()
	}
	CRC_32	                        32	rpchof
}

CA_descriptor() {
	descriptor_tag	                8	uimsbf
	descriptor_length	            8	uimsbf
	CA_system_ID	                16	uimsbf
	reserved	                    3	bslbf
	CA_PID	                        13	uimsbf
	for ( i=0; i<N; i++) {
		private_data_byte	        8	uimsbf
	}
}
 */