package com.prime.dmg.launcher.ACSDatabase;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class ACSSystemAlertParam {
    String TAG = getClass().getSimpleName();
    @SerializedName("message")
    private String mMessage;
    @SerializedName("qrcode")
    private String mQrCode;

    public String getQrCode() {
        return this.mQrCode;
    }

    public void setQrCode(String qrCode) {
        this.mQrCode = qrCode;
    }

    public String getMessage() {
        return this.mMessage;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public String toString() {
        return "Message:" + this.mMessage + "\nQrCode:" + this.mQrCode ;
    }

    public ACSSystemAlertParam() {
        mMessage = "";
    }

    public static ACSSystemAlertParam parser_data(String json) {
        ACSSystemAlertParam param = new ACSSystemAlertParam();
        if(json != null || !json.isEmpty())
            param = (ACSSystemAlertParam) new Gson().fromJson(json, ACSSystemAlertParam.class);
        return param;
    }

    public boolean is_exist() {
        if(mMessage != null && !mMessage.isEmpty())
            return true;
        return false;
    }
}
