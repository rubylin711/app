package com.prime.dmg.launcher.ACSDatabase;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class ACSForceSrceenParam {
    String TAG = getClass().getSimpleName();

    @SerializedName("image")
    private String mImage;
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

    public String getImage() {
        return this.mImage;
    }

    public void setImage(String image) {
        this.mImage = image;
    }

    public String toString() {
        return "Message:" + this.mMessage + "\nQrCode:" + this.mQrCode + "\nImage:" + this.mImage;
    }

    public ACSForceSrceenParam() {
        mMessage = "";
    }

    public static ACSForceSrceenParam parser_data(String json) {
        ACSForceSrceenParam param = new ACSForceSrceenParam();
        if(json != null || !json.isEmpty())
            param = (ACSForceSrceenParam) new Gson().fromJson(json, ACSForceSrceenParam.class);
        return param;
    }

    public boolean is_exist() {
        if(mMessage != null && !mMessage.isEmpty())
            return true;
        return false;
    }
}
