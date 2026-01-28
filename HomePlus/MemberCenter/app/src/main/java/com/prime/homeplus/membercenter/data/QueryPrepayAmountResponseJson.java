package com.prime.homeplus.membercenter.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QueryPrepayAmountResponseJson {
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
	private QueryPrepayAmountRetData mRetData;

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

	public QueryPrepayAmountRetData getRetData() {
                return mRetData;
        }

	@Override
	public String toString() {
	    String out = "mRetCode:" + mRetCode + ", mRetMsg" + mRetMsg + ", mRetDate:" + mRetDate + ", mTransId:" + mTransId + ", mUCID:" + mUCID;

	    if (mRetData != null) {
			out += ", mRetData:[" + mRetData.toString() + "]";
	    } else {
			out += ", mRetData:[null]";
	    }

	    return out;
	}

	public class QueryPrepayAmountRetData {
		@SerializedName(value = "CrmId")
		private String mCrmId;

		@SerializedName(value = "CrmCustName")
		private String mCrmCustName;

		@SerializedName(value = "CrmTotalShouldAmount")
		private String mCrmTotalShouldAmount;

		@SerializedName(value = "DataRows")
		private List<QueryPrepayAmountRetData2> mDataRowsList;

		public String getCrmId() {
			return mCrmId;
		}

		public String getCrmCustName() {
			return mCrmCustName;
		}

		public String getCrmTotalShouldAmount() {
			return mCrmTotalShouldAmount;
		}

		public List<QueryPrepayAmountRetData2> getDataRowsList() {
			return mDataRowsList;
		}

		@Override
		public String toString() {
			String out = "CrmId:" + mCrmId + ", CrmCustName:" + mCrmCustName + ", CrmTotalShouldAmount:" + mCrmTotalShouldAmount;

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

	public class QueryPrepayAmountRetData2 {
		@SerializedName(value = "CrmCItemcode")
		private String mCrmCItemcode;

		@SerializedName(value = "CrmCItemName")
		private String mCrmCItemName;

		@SerializedName(value = "Crmitem")
		private String mCrmitem;

		@SerializedName(value = "CrmRealStartDate")
		private String mCrmRealStartDate;

		@SerializedName(value = "CrmRealStopDate")
		private String mCrmRealStopDate;

		@SerializedName(value = "CrmRealPeriod")
		private String mCrmRealPeriod;

		@SerializedName(value = "CrmShouldAmount")
		private String mCrmShouldAmount;

		@SerializedName(value = "CrmMediabillno")
		private String mCrmMediabillno;

		@SerializedName(value = "PayType")
		private String mPayType;

		@SerializedName(value = "DeviceSNo4")
		private String mDeviceSNo4;

		@SerializedName(value = "Default")
		private String mDefault;

		@SerializedName(value = "Must")
		private String mMust;

		@SerializedName(value = "Days")
		private String mDays;

		@SerializedName(value = "SalePointcode")
		private String mSalePointcode;

		@SerializedName(value = "SalePointName")
		private String mSalePointName;


		public String getCrmCItemcode() {
			return mCrmCItemcode;
		}

		public String getCrmCItemName() {
			return mCrmCItemName;
		}

		public String getCrmitem() {
			return mCrmitem;
		}

		public String getCrmRealStartDate() {
			return mCrmRealStartDate;
		}

		public String getCrmRealStopDate() {
			return mCrmRealStopDate;
		}

		public String getCrmRealPeriod() {
			return mCrmRealPeriod;
		}

		public String getCrmShouldAmount() {
			return mCrmShouldAmount;
		}

		public String getCrmMediabillno() {
			return mCrmMediabillno;
		}

		public String getPayType() {
			return mPayType;
		}

		public String getDeviceSNo4() {
			return mDeviceSNo4;
		}

		public String getDefault() {
			return mDefault;
		}

		public String getMust() {
			return mMust;
		}

		public String getDays() {
			return mDays;
		}

		public String getSalePointcode() {
			return mSalePointcode;
		}

		public String getSalePointName() {
			return mSalePointName;
		}

		@Override
		public String toString() {
			return "CrmCItemcode:" + mCrmCItemcode + ", CrmCItemName:" + mCrmCItemName + ", Crmitem:" + mCrmitem +
					", CrmRealStartDate:" + mCrmRealStartDate + ", CrmRealStopDate:" + mCrmRealStopDate +
					", CrmRealPeriod:" + mCrmRealPeriod + ", CrmShouldAmount:" + mCrmShouldAmount + ", CrmMediabillno:" + mCrmMediabillno +
					", PayType:" + mPayType + ", DeviceSNo4:" + mDeviceSNo4 + ", Default:" + mDefault +
					", Must:" + mMust + ", Days:" + mDays + ", SalePointcode:" + mSalePointcode +
					", SalePointName:" + mSalePointName;
		}
	}
}



