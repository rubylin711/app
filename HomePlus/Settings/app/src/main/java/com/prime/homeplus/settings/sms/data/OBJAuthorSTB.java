package com.prime.homeplus.settings.sms.data;

public class OBJAuthorSTB {
    String mCrmId, mDeviceSNo3, mCrmWorkOrder, mCrmWorker1, mMobilePhone, mIncludeHD, mCustId, mReturnMode, mHDSerialNo, mCmMode;

    public OBJAuthorSTB(String crmId, String deviceSNo3, String crmWorkOrder, String crmWorker1, String mobilePhone, String includeHD, String custId, String returnMode, String hdSerialNo, String cmMode) {
        mCrmId = crmId;
        mDeviceSNo3 = deviceSNo3;
        mCrmWorkOrder = crmWorkOrder;
        mCrmWorker1 = crmWorker1;
        mMobilePhone = mobilePhone;
        mIncludeHD = includeHD;
        mCustId = custId;
        mReturnMode = returnMode;
        mHDSerialNo = hdSerialNo;
        mCmMode = cmMode;
    }

    public String getCrmId() {
        return mCrmId;
    }

    public String getDeviceSNo3() {
        return mDeviceSNo3;
    }

    public String getCrmWorkOrder() {
        return mCrmWorkOrder;
    }

    public String getCrmWorker1() {
        return mCrmWorker1;
    }

    public String getMobilePhone() {
        return mMobilePhone;
    }

    public String getIncludeHD() {
        return mIncludeHD;
    }

    public String getCustId() {
        return mCustId;
    }

    public String getReturnMode() {
        return mReturnMode;
    }

    public String getHDSerialNo() {
        return mHDSerialNo;
    }

    public String getCmMode() {
        return mCmMode;
    }
}

