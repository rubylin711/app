package com.prime.homeplus.settings.data;

import com.google.gson.annotations.SerializedName;

public class ActivationStatusDataJson {
	@SerializedName(value = "CrmId")
	private String mCrmId;

	@SerializedName(value = "SmartCard")
	private String mSmartCard;

	@SerializedName(value = "DeviceSNo3")
	private String mDeviceSNo3;

	@SerializedName(value = "BID")
	private String mBid;

	@SerializedName(value = "ZIPCode")
	private String mZipCode;

	@SerializedName(value = "AreaCode")
	private String mAreaCode;

	@SerializedName(value = "CmMode")
	private String mCmMode;

	public String getCrmId() {
		return mCrmId;
	}

	public String getSmartCard() {
		return mSmartCard;
	}

	public String getDeviceSNo3() {
		return mDeviceSNo3;
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

	public String getCmMode() {
		return mCmMode;
	}

	@Override
	public String toString() {
	    String out = "mCrmId:" + mCrmId + ", mSmartCard:" + mSmartCard + ", mDeviceSNo3:" + mDeviceSNo3 + ", mBid:" + mBid +
				"mZipCode:" + mZipCode + ", mAreaCode:" + mAreaCode + ", mCmMode:" + mCmMode;

	    return out;
	}
}



