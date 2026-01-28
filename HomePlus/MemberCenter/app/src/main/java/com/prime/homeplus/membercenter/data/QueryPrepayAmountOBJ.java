package com.prime.homeplus.membercenter.data;

public class QueryPrepayAmountOBJ {
    String mCrmId, mDeviceSNo3, mCrmBaseDate, mFlag;
    public QueryPrepayAmountOBJ(String crmId, String deviceSNo3, String crmBaseDate, String flag) {
	mCrmId = crmId;
	mDeviceSNo3 = deviceSNo3;
	mCrmBaseDate = crmBaseDate;
	mFlag = flag;
    }

    public String getCrmId() {
	return mCrmId;
    }

    public String getDeviceSNo3() {
	return mDeviceSNo3;
    }

    public String getCrmBaseDate() {
	return mCrmBaseDate;
    }

    public String getFlag() {
	return mFlag;
    }
}

