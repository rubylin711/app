package com.prime.homeplus.settings.sms.data;

import com.google.gson.annotations.SerializedName;

public class APIResponseJson {
    @SerializedName(value = "RetCode")
    private String mRetCode;

    @SerializedName(value = "RetMsg")
    private String mRetMsg;

    @SerializedName(value = "RetDate")
    private String mRetDate;

    @SerializedName(value = "TransId")
    private String mTransId;

    @SerializedName(value = "UCID")
    private String mUCID;

    @SerializedName(value = "RetData")
    private RetData mRetData;

    public String getRetCode() {
        return mRetCode;
    }

    public String getRetMsg() {
        return mRetMsg;
    }

    public String getRetDate() {
        return mRetDate;
    }

    public String getTransId() {
        return mTransId;
    }

    public String getUCID() {
        return mUCID;
    }

    public RetData getRetData() {
        return mRetData;
    }

    @Override
    public String toString() {
        String out = "mRetCode:" + mRetCode + ", mRetMsg" + mRetMsg + ", mRetDate:" + mRetDate + ", mTransId:" + mTransId + ", mUCID:" + mUCID;

        if (mRetData != null) {
            out += ", mRetData:[" + mRetData.toString() + "]";
        } else {
            out += ", mRetData:[null]";
        }

        return out;
    }
}



