package com.prime.homeplus.settings.sms.data;

public class OBJSwapMobilePhone {
    String mCrmId, mCrmWorkshortsno, mMobilePhone;

    public OBJSwapMobilePhone(String crmId, String crmWorkshortsno, String mobilephone) {
        mCrmId = crmId;
        mCrmWorkshortsno = crmWorkshortsno;
        mMobilePhone = mobilephone;
    }

    public String getCrmId() {
        return mCrmId;
    }

    public String getCrmWorkshortsno() {
        return mCrmWorkshortsno;
    }

    public String getMobilePhone() {
        return mMobilePhone;
    }
}

