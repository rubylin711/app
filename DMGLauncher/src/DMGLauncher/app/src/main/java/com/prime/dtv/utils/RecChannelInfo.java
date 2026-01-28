package com.prime.dtv.utils;

import android.util.Log;

import androidx.annotation.NonNull;

public class RecChannelInfo {
    private final String TAG = this.getClass().getName();

    private long mChannleId;
    private int mEventId;
    private int mTunerId;
    private int mDuration;
    private String mChannelNum;
    private String mEventName;
    private boolean mIs4K;

    public RecChannelInfo(long ch_id, int event_id, int tuner_id, int duration, String chNum, String eventName, boolean is4K){
        mChannleId = ch_id;
        mEventId = event_id;
        mTunerId = tuner_id;
        mDuration = duration;
        mChannelNum = chNum;
        mEventName = eventName;
        mIs4K = is4K;
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
}
