package com.prime.homeplus.settings.sms.data;

import com.google.gson.annotations.SerializedName;

public class RetData {
    @SerializedName(value = "CrmId")
    private String mCrmId;

    @SerializedName(value = "CrmWorkOrder")
    private String mCrmWorkOrder;

    @SerializedName(value = "Crminstallname")
    private String mCrminstallname;

    @SerializedName(value = "CrmBpname")
    private String mCrmBpname;

    @SerializedName(value = "CrmWorker1")
    private String mCrmWorker1;

    @SerializedName(value = "mobilephone")
    private String mMobilephone;

    @SerializedName(value = "CmMode")
    private String mCmMode;

    @SerializedName(value = "BID")
    private String mBid;

    @SerializedName(value = "ZIPCode")
    private String mZipCode;

    @SerializedName(value = "AreaCode")
    private String mAreaCode;

    public String getCrmId() {
        return mCrmId;
    }

    public String getCrmWorkOrder() {
        return mCrmWorkOrder;
    }

    public String getCrminstallname() {
        return mCrminstallname;
    }

    public String getCrmBpname() {
        return mCrmBpname;
    }

    public String getCrmWorker1() {
        return mCrmWorker1;
    }

    public String getMobilephone() {
        return mMobilephone;
    }

    public String getCmMode() {
        return mCmMode;
    }

    public String getBid() {
        return mBid;
    }

    public String getZipCode() {
        return mZipCode;
    }

    public String getAreaCode() {
        return mAreaCode;
    }

    @Override
    public String toString() {
        return "mCrmId:" + mCrmId + ", mCrmWorkOrder:" + mCrmWorkOrder + ", mCrminstallname:" + mCrminstallname + ", mCrmBpname:" + mCrmBpname + ", mCrmWorker1:" + mCrmWorker1 + ", mMobilephone:" + mMobilephone + ", mCmMode:" + mCmMode + ", mBid:" + mBid + ", mZipCode:" + mZipCode + ", mAreaCode:" + mAreaCode;
    }
}
