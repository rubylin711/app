package com.prime.homeplus.settings.sms.data;

public class OBJQuerySNO {
    String mCrmId, mCrmWorkshortsno;

    public OBJQuerySNO(String crmId, String crmWorkshortsno) {
        mCrmId = crmId;
        mCrmWorkshortsno = crmWorkshortsno;
    }

    public String getCrmId() {
        return mCrmId;
    }

    public String getCrmWorkshortsno() {
        return mCrmWorkshortsno;
    }
}

