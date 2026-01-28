package com.prime.homeplus.membercenter.data;

public class QueryCrmMyAccountOBJ {
    String mCrmId, mDeviceSNo3;
    public QueryCrmMyAccountOBJ(String crmId, String deviceSNo3) {
	mCrmId = crmId;
	mDeviceSNo3 = deviceSNo3;
    }

    public String getCrmId() {
	return mCrmId;
    }

    public String getDeviceSNo3() {
	return mDeviceSNo3;
    }
}

