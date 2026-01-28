package com.prime.homeplus.membercenter.data;

import com.google.gson.annotations.SerializedName;

public class QueryCrmMyAccountRetData2 {
	@SerializedName(value = "CrmId")
	private String mCrmId;

	@SerializedName(value = "DeviceSNo3")
	private String mDeviceSNo3;

	@SerializedName(value = "CrmCItemName")
	private String mCrmCItemName;

	@SerializedName(value = "CrmStartDate")
	private String mCrmStartDate;

	@SerializedName(value = "CrmStopDate")
	private String mCrmStopDate;

	@SerializedName(value = "CrmPeriod")
	private String mCrmPeriod;

	@SerializedName(value = "CrmRealDate")
	private String mCrmRealDate;

	@SerializedName(value = "CrmAmount")
	private String mCrmAmount;

	@SerializedName(value = "CrmContStartDate")
	private String mCrmContStartDate;

	@SerializedName(value = "CrmContStopDate")
	private String mCrmContStopDate;

	public String getCrmId() {
		return mCrmId;
	}

	public String getDeviceSNo3() {
		return mDeviceSNo3;
	}

	public String getCrmCItemName() {
		return mCrmCItemName;
	}

	public String getCrmStartDate() {
		return mCrmStartDate;
	}

	public String getCrmStopDate() {
		return mCrmStopDate;
	}

	public String getCrmPeriod() {
		return mCrmPeriod;
	}

	public String getCrmRealDate() {
		return mCrmRealDate;
	}

	public String getCrmAmount() {
		return mCrmAmount;
	}

	public String getCrmContStartDate() {
		return mCrmContStartDate;
	}

	public String getCrmContStopDate() {
		return mCrmContStopDate;
	}

	@Override
	public String toString() {
	    return "CrmId:" + mCrmId + ", DeviceSNo3:" + mDeviceSNo3 + ", CrmCItemName:" + mCrmCItemName +
				", CrmStartDate:" + mCrmStartDate + ", CrmStopDate:" + mCrmStopDate +
				", CrmPeriod:" + mCrmPeriod + ", CrmRealDate:" + mCrmRealDate + ", CrmAmount:" + mCrmAmount +
				", CrmContStartDate:" + mCrmContStartDate + ", CrmContStopDate:" + mCrmContStopDate;
	}
}
