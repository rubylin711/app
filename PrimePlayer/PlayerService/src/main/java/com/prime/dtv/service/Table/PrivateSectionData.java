package com.prime.dtv.service.Table;

import static com.prime.dtv.service.Util.Utils.MASK_12BITS;
import static com.prime.dtv.service.Util.Utils.getInt;
import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PrivateSectionData extends TableData {
    private static final String TAG = "PrivateSectionData";

    public class PrivateData {
        public int mTableId=-1;
        public int mSectionLength=0;
        public byte[] mPrivateDataByte=null;
    }

    private List<PrivateData> mPrivateDataList = new ArrayList<PrivateData>();

    public int getNumOfPrivateData() {
        return mPrivateDataList.size();
    }
    public PrivateData getPrivateDataByIndex(int i) {
        if(i >= mPrivateDataList.size())
            return null;
        else
            return mPrivateDataList.get(i);
    }


    public void buildPrivateDataList(byte[] data, int len) {
        int sectionSyntaxIndicator,sectionLength,tableId;
        tableId = toUnsignedInt(data[0]);
        sectionSyntaxIndicator = (toUnsignedInt(data[1])) >> 7;
        sectionLength = getInt(data, 1, 2, MASK_12BITS);
        //Log.d(TAG, "tableId = "+tableId+" sectionSyntaxIndicator = "+sectionSyntaxIndicator+" sectionLength = "+sectionLength);
        if(sectionSyntaxIndicator == 1){
            Log.w(TAG, "Currently not implemented");
        }
        else{
            PrivateData privateData = new PrivateData();
            //byte[] mPrivateDataByte = new byte[lens];

            privateData.mTableId = tableId;
            privateData.mSectionLength=sectionLength;
            privateData.mPrivateDataByte = new byte[sectionLength];
            System.arraycopy(data, 3, privateData.mPrivateDataByte, 0, sectionLength);
            mPrivateDataList.add(privateData);
        }
    }

    @Override
    public void parsing(byte[] data, int lens) {
        //Log.d(TAG, "parsing()");
        try{
            buildPrivateDataList(data,lens);
        } catch (Exception e) {
            Log.e(TAG, "e = "+e);
            e.printStackTrace();
        }
    }
}
/*
private_section() {
    table_id                                                8 uimsbf
    section_syntax_indicator                                1 bslbf
    private_indicator                                       1 bslbf
    reserved                                                2 bslbf
    private_section_length                                  12 uimsbf
    if (section_syntax_indicator = = '0') {
        for (i = 0; i < N; i++) {
            private_data_byte                               8 bslbf
        }
    }
    else {
        table_id_extension                                  16 uimsbf
        reserved                                            2 bslbf
        version_number                                      5 uimsbf
        current_next_indicator                              1 bslbf
        section_number                                      8 uimsbf
        last_section_number                                 8 uimsbf
        for (i = 0; i < private_section_length-9; i++) {
            private_data_byte                               8 bslbf
        }
        CRC_32 32 rpchof
    }
}
 */