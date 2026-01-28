package com.prime.homeplus.settings.sms.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import android.util.Log;

public class ProductAPIResponseJson {
    @SerializedName(value = "code")
    private String mCode;

    @SerializedName(value = "message")
    private String mMessage;

    @SerializedName(value = "timecost")
    private String mTimecost;

    @SerializedName(value = "timestamp")
    private String mTimestamp;

    public String getCode() {
        return mCode;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getTimecost() {
        return mTimecost;
    }

    public String getTimestamp() {
        return mTimestamp;
    }

    @Override
    public String toString() {
        String out = "mCode:" + mCode + ", mMessage:" + mMessage + ", mTimecost:" + mTimecost + ", mTimestamp:" + mTimestamp;

        return out;
    }
}



