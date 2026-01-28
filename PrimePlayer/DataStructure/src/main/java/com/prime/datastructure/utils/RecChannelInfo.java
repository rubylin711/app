package com.prime.datastructure.utils;

import android.util.Log;

import androidx.annotation.NonNull;


import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.EPGEvent;

import java.util.Arrays;

public class RecChannelInfo {
    private final String TAG = this.getClass().getName();

    private long mChannleId;
    private int mEventId;
    private int mTunerId;
    private int mDuration;
    private String mChannelNum;
    private String mEventName;
    private boolean mIs4K;
    private long mStartTimeMs;
    private EPGEvent mEpgEvent;

    public RecChannelInfo(long ch_id, int event_id, int tuner_id, int duration, String chNum, String eventName, boolean is4K, EPGEvent epgEvent){
        mChannleId = ch_id;
        mEventId = event_id;
        mTunerId = tuner_id;
        mDuration = duration;
        mChannelNum = chNum;
        mEventName = eventName;
        mIs4K = is4K;
        mEpgEvent = epgEvent;
        mStartTimeMs = System.currentTimeMillis();
    }

    public boolean isSameChannel(long ch_id){
        if (mChannleId == ch_id)
            return true;
        else
            return false;
    }

    @NonNull
    public String toString(){
        String s = "mChannelId = " + mChannleId + ", mEventId = " + mEventId + ", mTunerId = " + mTunerId + ", mDuration = "+mDuration;
        return s;
    }

    public long getChannelId(){
        return mChannleId;
    }

    public int getEventId() {
        return mEventId;
    }

    public int getTunerId(){
        return mTunerId;
    }

    public int getDuration() {
        return mDuration;
    }

    public String getChannelNum() {
        return mChannelNum;
    }

    public String getEventName() {
        return mEventName;
    }

    public boolean is4K() {
        return mIs4K;
    }

    public EPGEvent getEpgEvent() {
        return mEpgEvent;
    }

    public long getEstimateEndTimeMs() {
        return mStartTimeMs + mDuration * 1000L;
    }

    public boolean is_same_series(BookInfo bookInfo) {
        if (bookInfo == null) {
            Log.e(TAG, "is_same_series: null book info");
            return false;
        }

        if (getEventId() < 0) { // 0 is a valid event id
            Log.e(TAG, "is_same_series: maybe is timer recording, incorrect event id = " + getEventId());
            return false;
        }

//        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
//        EPGEvent recEvent = primeDtv.get_epg_by_event_id(getChannelId(), getEventId());
//        String recSeriesKey = Arrays.toString(recEvent.get_series_key());
//        String seriesKey = Arrays.toString(bookInfo.getSeriesRecKey());
//
//        return  seriesKey.equals(recSeriesKey);
        return false;
    }

}
