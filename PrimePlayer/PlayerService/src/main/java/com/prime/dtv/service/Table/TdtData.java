package com.prime.dtv.service.Table;

import android.os.SystemClock;
import android.util.Log;

import com.prime.dtv.service.Util.Utils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

public class TdtData extends TableData {
    private static final String TAG = "TdtData";
    private static final int TDT_LENGTH = 8;

    private int mTableId;
    private final byte[] mRawUtcTimeBytes;
    private LocalDateTime mLocalDateTime;
    private long mTimeMillis;

    public TdtData() {
        mRawUtcTimeBytes = new byte[5]; // 40 bits = 5 bytes
        mLocalDateTime = LocalDateTime.now(); // init time = now
    }

    @Override
    public void parsing(byte[] data, int lens) {
        try {
            // TDT lengths should be 8
            if (data.length != lens || lens != TDT_LENGTH) {
                Log.e(TAG, "parsing: data length error!");
                return;
            }

            if (data[0] != Table.TDT_TABLE_ID) {
                Log.e(TAG, "parsing: table id error!");
                return;
            }

            // table_id = first byte
            mTableId = Byte.toUnsignedInt(data[0]);
            Log.d(TAG, "parsing: table_id = " + mTableId);
            // copy raw UTC_time (3rd to 8th byte = 5 bytes)
            System.arraycopy(data, 3, mRawUtcTimeBytes, 0, 5);
            Log.d(TAG, "parsing: raw utc time = " + Arrays.toString(mRawUtcTimeBytes));

            // get LocalDateTime from raw UTC_time
            mLocalDateTime = Utils.getLocalDateTimeFromRawBytes(mRawUtcTimeBytes);
            Log.d(TAG, "parsing: localDateTime = " + mLocalDateTime);

            // get time millis from raw UTC_time
            mTimeMillis = Utils.getTimeMillisFromRawBytes(mRawUtcTimeBytes);
            Log.d(TAG, "parsing: time millis = " + mTimeMillis);

            SystemClock.setCurrentTimeMillis(mTimeMillis+8*60*60*1000);
        } catch (Exception e) {
            Log.e(TAG, "e = "+e);
            e.printStackTrace();
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
}
