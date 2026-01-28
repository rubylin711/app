package com.prime.homeplus.membercenter.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetVodPointResponseJson {
	@SerializedName(value = "result")
	private GetVodPointResult mResult;

	@SerializedName(value = "data")
	private List<GetVodPointData> mDataList;

	public GetVodPointResult getRetResult() {
                return mResult;
        }

	public List<GetVodPointData> getRetDataList() {
		return mDataList;
	}

	@Override
	public String toString() {
		String out = "";
		if (mResult != null) {
			out += "Result: {" + mResult.toString() +"}";
		} else {
			out += "Result: {null}";
		}

		if (mDataList != null && mDataList.size() > 0) {
			for (int i = 0; i < mDataList.size(); i++) {
				out += ", data:[" + mDataList.get(i).toString() + "]";
			}
		} else {
			out += ", data:[null]";
		}

		return out;
	}

	public class GetVodPointResult {
		@SerializedName(value = "code")
		private String mCode;

		@SerializedName(value = "desc")
		private String mDesc;

		@SerializedName(value = "uuid")
		private String mUuid;

		@SerializedName(value = "ts")
		private String mTs;

		public String getCode() {
			return mCode;
		}

		public String getDesc() {
			return mDesc;
		}

		public String getUuid() {
			return mUuid;
		}

		public String getTs() {
			return mTs;
		}

		@Override
		public String toString() {
			return "Code:" + mCode + ", Desc:" + mDesc + ", Uuid:" + mUuid + ", Ts:" + mTs;
		}
	}

	public class GetVodPointData {
		@SerializedName(value = "compcode")
		private String mCompcode;

		@SerializedName(value = "custid")
		private String mCustid;

		@SerializedName(value = "facisno")
		private String mFacisno;

		@SerializedName(value = "faciid")
		private String mFaciid;

		@SerializedName(value = "vodamount")
		private String mVodamount;

		@SerializedName(value = "giftpoint")
		private String mGiftpoint;

		@SerializedName(value = "usepoint")
		private String mUsepoint;

		@SerializedName(value = "surpluspoint")
		private String mSurpluspoint;

		@SerializedName(value = "RecvTill")
		private String mRecvTill;

		@SerializedName(value = "voddiscount")
		private String mVoddiscount;

		@SerializedName(value = "remark")
		private String mRemark;

		public String getCompcode() {
			return mCompcode;
		}

		public String getCustid() {
			return mCustid;
		}

		public String getFacisno() {
			return mFacisno;
		}

		public String getFaciid() {
			return mFaciid;
		}

		public String getVodamount() {
			return mVodamount;
		}

		public String getGiftpoint() {
			return mGiftpoint;
		}

		public String getUsepoint() {
			return mUsepoint;
		}

		public String getSurpluspoint() {
			return mSurpluspoint;
		}

		public String getRecvTill() {
			return mRecvTill;
		}

		public String getVoddiscount() {
			return mVoddiscount;
		}

		public String getRemark() {
			return mRemark;
		}

		@Override
		public String toString() {
			return "Compcode:" + mCompcode + ", Custid:" + mCustid + ", Facisno:" + mFacisno +
					", Faciid:" + mFaciid + ", Vodamount:" + mVodamount +
					", Giftpoint:" + mGiftpoint + ", Usepoint:" + mUsepoint +
					", Surpluspoint:" + mSurpluspoint + ", RecvTill:" + mRecvTill +
					", Voddiscount:" + mVoddiscount + ", Remark:" + mRemark;
		}
	}
}



