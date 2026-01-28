package com.prime.homeplus.vbm;

import com.google.gson.annotations.SerializedName;

public class VBMUploadResponse {
	@SerializedName(value = "success")
	private boolean success;

	@SerializedName(value = "message")
	private String message;

	public boolean getSuccess() {
		return this.success;
	}

	public String getMessage() {
		return this.message;
	}
}
