package com.prime.dtv.service.Table;

import android.util.Log;

import com.prime.dtv.service.Table.Desciptor.DescBase;
import com.prime.dtv.service.Table.Desciptor.Descriptor;
import com.prime.dtv.service.Table.Desciptor.LocalTimeOffsetDescriptor;
import com.prime.dtv.service.Util.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TotData extends TableData {
    private static final String TAG = "TotData";
    private static final int MIN_TOT_LENGTH = 14;

    private int mTableId;
    private final byte[] mRawUtcTimeBytes;
    private LocalDateTime mLocalDateTime;
    private long mTimeMillis;
    private final Descriptor mDescriptorHelper;
    private final List<LocalTimeOffsetDescriptor> mLocalTimeOffsetDescriptorList;

    public TotData() {
        mRawUtcTimeBytes = new byte[5]; // 40 bits = 5 bytes
        mLocalDateTime = LocalDateTime.now(); // init time = now
        mDescriptorHelper = new Descriptor();
        mLocalTimeOffsetDescriptorList = new ArrayList<>();
    }

    @Override
    public void parsing(byte[] data, int lens) {
        try{
            // TOT lengths should > 14
            if (data.length != lens || lens < MIN_TOT_LENGTH) {
                Log.e(TAG, "parsing: data length error!");
                return;
            }

            if (data[0] != Table.TOT_TABLE_ID) {
                Log.e(TAG, "parsing: table id error!");
                return;
            }

            // table_id = first byte
            mTableId = Byte.toUnsignedInt(data[0]);
            Log.d(TAG, "parsing: table_id = " + mTableId);

            // copy raw UTC_time (4th to 8th byte = 5 bytes)
            System.arraycopy(data, 3, mRawUtcTimeBytes, 0, 5);
            Log.d(TAG, "parsing: raw utc time = " + Arrays.toString(mRawUtcTimeBytes));

            // get LocalDateTime from raw UTC_time
            mLocalDateTime = Utils.getLocalDateTimeFromRawBytes(mRawUtcTimeBytes);
            Log.d(TAG, "parsing: localDateTime = " + mLocalDateTime);

            // get time millis from raw UTC_time
            mTimeMillis = Utils.getTimeMillisFromRawBytes(mRawUtcTimeBytes);
            Log.d(TAG, "parsing: time millis = " + mTimeMillis);

            // descriptors
            // descriptors_loop_length = 12 bits of 9th and 10th byte
            int descriptorsLoopLength = Utils.getInt(data, 8, 2, Utils.MASK_12BITS);
            // copy all descriptor data to descriptorsLoopData
            byte[] descriptorsLoopData = new byte[descriptorsLoopLength];
            // descriptors start from 11th byte
            System.arraycopy(data, 10, descriptorsLoopData, 0, descriptorsLoopLength);
            // parse
            parseDescriptors(descriptorsLoopData);

            printAllDesc();
        } catch (Exception e) {
            Log.e(TAG, "e = "+e);
            e.printStackTrace();
        }
    }

    private void parseDescriptors(byte[] descriptorsLoopData) {
        int curPos = 0;
        while (curPos < descriptorsLoopData.length) {
            int descriptorLength = Byte.toUnsignedInt(descriptorsLoopData[curPos+1]);
            byte[] descriptorData = new byte[descriptorLength+2]; // +2 = descriptorTag byte + descriptorLength byte
            System.arraycopy(descriptorsLoopData, curPos, descriptorData, 0, descriptorLength+2);
            mDescriptorHelper.ParsingDescriptor(descriptorData, descriptorData.length);
            curPos = curPos + descriptorData.length;
        }

        // save parse result
        List<DescBase> descBaseList = mDescriptorHelper.getDescriptorList(Descriptor.LOCAL_TIME_OFFSET_DESC);
        for (DescBase descBase : descBaseList) {
            mLocalTimeOffsetDescriptorList.add((LocalTimeOffsetDescriptor) descBase);
        }
    }

    private void printAllDesc() {
        for (LocalTimeOffsetDescriptor descriptor : mLocalTimeOffsetDescriptorList) {
            for (LocalTimeOffsetDescriptor.LocalTimeOffset timeOffset : descriptor.getOffsetList()) {
                Log.d(TAG, "printAllDesc LocalTimeOffsetPolarity: " + timeOffset.LocalTimeOffsetPolarity);
                Log.d(TAG, "printAllDesc LocalTimeOffset: " + timeOffset.LocalTimeOffset);
                Log.d(TAG, "printAllDesc CountryCode: " + timeOffset.CountryCode);
                Log.d(TAG, "printAllDesc CountryRegionId: " + timeOffset.CountryRegionId);
                Log.d(TAG, "printAllDesc nextTimeOffset: " + timeOffset.nextTimeOffset);
                Log.d(TAG, "printAllDesc TimeOfChangeLsb: " + timeOffset.TimeOfChangeLsb);
                Log.d(TAG, "printAllDesc TimeOfChangeMsb: " + timeOffset.TimeOfChangeMsb);
            }
        }
    }

    public int getTableId() {
        return mTableId;
    }

    public byte[] getUtcTimeBytes() {
        return mRawUtcTimeBytes;
    }

    public LocalDateTime getUTCLocalDateTime() {
        return mLocalDateTime;
    }

    public long getTimeMillis() {
        return mTimeMillis;
    }

    public String getUtcFormattedTimeString() {
        return Utils.getUtcFormattedTimeString(mLocalDateTime);
    }

    public Date getDate() {
        return Utils.getDate(mLocalDateTime);
    }

    public List<LocalTimeOffsetDescriptor> getLocalTimeOffsetDescriptorList() {
        return mLocalTimeOffsetDescriptorList;
    }
}
