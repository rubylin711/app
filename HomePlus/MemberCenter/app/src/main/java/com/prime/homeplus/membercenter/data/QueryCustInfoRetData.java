package com.prime.homeplus.membercenter.data;

import com.google.gson.annotations.SerializedName;

public class QueryCustInfoRetData {
	@SerializedName(value = "CrmId")
	private String mCrmId;

	@SerializedName(value = "CompanyNo")
	private String mCompanyNo;

	@SerializedName(value = "DeviceSNo3")
	private String mDeviceSNo3;

	@SerializedName(value = "CustId")
	private String mCustId;

	@SerializedName(value = "SubsId")
	private String mSubsId;

	@SerializedName(value = "CustStatus")
	private String mCustStatus;

	@SerializedName(value = "BPCode")
	private String mBPCode;

	public String getCrmId() {
		return mCrmId;
	}

	public String getCompanyNo() {
		return mCompanyNo;
	}

	public String getDeviceSNo3() {
		return mDeviceSNo3;
	}

	public String getCustId() {
		return mCustId;
	}

	public String getSubsId() {
		return mSubsId;
	}

	public String getCustStatus() {
		return mCustStatus;
	}

	public String getBPCode() {
		return mBPCode;
	}

	@Override
	public String toString() {
	    return "CrmId:" + mCrmId + ", CompanyNo:" + mCompanyNo + ", DeviceSNo3:" + mDeviceSNo3 + ", CustId:" + mCustId + ", SubsId:" + mSubsId + ", CustStatus:" + mCustStatus + ", BPCode:" + mBPCode;
	}
}
