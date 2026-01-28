package com.prime.homeplus.settings.data;

import com.google.gson.annotations.SerializedName;

public class ActivationStatusAPIResponseJson {
	@SerializedName(value = "request-id")
	private String mRequestId;

	@SerializedName(value = "code")
	private String mCode;

	@SerializedName(value = "messages")
	private String[] mMessages;

	@SerializedName(value = "data")
	private ActivationStatusDataJson mData;

	public String getRequestId() {
		return mRequestId;
	}

	public String getCode() {
		return mCode;
	}

	public String[] getMessages() {
		return mMessages;
	}

	public ActivationStatusDataJson getData() {
		return mData;
	}

	@Override
	public String toString() {
	    String out = "mRequestId:" + mRequestId + ", mCode:" + mCode + ", mMessages:" + mMessages + ", mData:" + mData;

	    return out;
	}
}



