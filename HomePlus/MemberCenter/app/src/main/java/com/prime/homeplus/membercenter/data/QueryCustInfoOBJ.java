package com.prime.homeplus.membercenter.data;

public class QueryCustInfoOBJ {
    String mCrmId, mDeviceSNo3, mSmartCard;
    public QueryCustInfoOBJ(String crmId, String deviceSNo3, String smartCard) {
	mCrmId = crmId;
	mDeviceSNo3 = deviceSNo3;
	mSmartCard = smartCard;
    }

    public String getCrmId() {
	return mCrmId;
    }

    public String getDeviceSNo3() {
	return mDeviceSNo3;
    }

    public String getSmartCard() {
	return mSmartCard;
    }

}

