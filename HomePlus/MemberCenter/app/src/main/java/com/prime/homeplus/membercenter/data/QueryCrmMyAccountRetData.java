package com.prime.homeplus.membercenter.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QueryCrmMyAccountRetData {
	@SerializedName(value = "RemainCredit")
	private String mRemainCredit;

	@SerializedName(value = "DataRows")
	private List<QueryCrmMyAccountRetData2> mDataRowsList;

	public String getRemainCredit() {
		return mRemainCredit;
	}

	public List<QueryCrmMyAccountRetData2> getDataRowsList() {
		return mDataRowsList;
	}

	@Override
	public String toString() {
		String out = "RemainCredit:" + mRemainCredit;

		if (mDataRowsList != null && mDataRowsList.size() > 0) {
			for (int i = 0; i < mDataRowsList.size(); i++) {
				out += ", DataRows:[" + mDataRowsList.get(i).toString() + "]";
			}
		} else {
			out += ", DataRows:[null]";
		}

		return out;
	}
}
