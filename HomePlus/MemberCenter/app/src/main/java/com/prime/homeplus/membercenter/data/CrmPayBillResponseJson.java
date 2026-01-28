package com.prime.homeplus.membercenter.data;

import com.google.gson.annotations.SerializedName;

public class CrmPayBillResponseJson {
	@SerializedName(value = "RetCode")
	private String mRetCode;

	@SerializedName(value = "RetMsg")
	private String mRetMsg;

	@SerializedName(value = "RetDate")
	private String mRetDate;

	@SerializedName(value = "TransId")
	private String mTransId;

	@SerializedName(value = "UCID")
	private String mUCID;

	@SerializedName(value = "RetData")
	private CrmPayBillRetData mRetData;

	public String getRetCode() {
		return mRetCode;
	}

	public String getRetMsg() {
		return mRetMsg;
	}

	public String getRetDate() {
		return mRetDate;
	}

	public String getTransId() {
		return mTransId;
	}

	public String getUCID() {
		return mUCID;
	}

	public CrmPayBillRetData getRetData() {
                return mRetData;
        }

	@Override
	public String toString() {
	    String out = "RetCode:" + mRetCode + ", RetMsg" + mRetMsg + ", RetDate:" + mRetDate + ", TransId:" + mTransId + ", UCID:" + mUCID;
	    out += ", RetData:[null]";

	    return out;
	}

	public class CrmPayBillRetData {
	}
}



