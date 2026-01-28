package com.prime.homeplus.membercenter;

import java.io.UnsupportedEncodingException;
import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.prime.homeplus.membercenter.data.CrmPayBillOBJ;
import com.prime.homeplus.membercenter.data.CrmPayBillResponseJson;
import com.prime.homeplus.membercenter.data.GetVodPointOBJ;
import com.prime.homeplus.membercenter.data.GetVodPointResponseJson;
import com.prime.homeplus.membercenter.data.QueryCrmMyAccountOBJ;
import com.prime.homeplus.membercenter.data.QueryCrmMyAccountResponseJson;
import com.prime.homeplus.membercenter.data.QueryCustInfoOBJ;
import com.prime.homeplus.membercenter.data.QueryPrepayAmountOBJ;
import com.prime.homeplus.membercenter.data.QueryCustInfoResponseJson;
import com.prime.homeplus.membercenter.data.QueryPrepayAmountResponseJson;

public class PostDataMemberCenter {
    private static String TAG = "HOMEPLUSTV_MEMBERCENTER-PostDataMemberCenter";
    private String member_center_url;
	private String member_center_vod_point_url;
    private RequestQueue mQueue;
    private QueryCrmMyAccountOBJ mQueryCrmMyAccountOBJ;
    private QueryPrepayAmountOBJ mQueryPrepayAmountOBJ;
    private QueryCustInfoOBJ mQueryCustInfoOBJ;
    private CrmPayBillOBJ mCrmPayBillOBJ;
	private GetVodPointOBJ mGetVodPointOBJ;
    private Handler mHandler = null;

    public PostDataMemberCenter(Context context, Handler handler) {
		mQueue = Volley.newRequestQueue(context);
		mHandler = handler;
    }

	public void sendQueryCustInfo(QueryCustInfoOBJ queryCustInfoOBJ) {
		mQueryCustInfoOBJ = queryCustInfoOBJ;
		send("QueryCustInfo");
	}

	public void sendQueryPrepayAmount(QueryPrepayAmountOBJ queryPrepayAmountOBJ) {
		mQueryPrepayAmountOBJ = queryPrepayAmountOBJ;
		send("QueryPrepayAmount");
	}

	public void sendCrmPayBill(CrmPayBillOBJ crmPayBillOBJ) {
		mCrmPayBillOBJ = crmPayBillOBJ;
		send("CrmPayBill");
	}

	public void sendQueryCrmMyAccount(QueryCrmMyAccountOBJ queryCrmMyAccountOBJ) {
		mQueryCrmMyAccountOBJ = queryCrmMyAccountOBJ;
		send("QueryCrmMyAccount");
	}

	public void sendGetVodPoint(GetVodPointOBJ getVodPointOBJ) {
		mGetVodPointOBJ = getVodPointOBJ;
		send("GetVodPoint");
	}

	public void send(final String api) {
		String mSO = Utils.getSoId(true);
		member_center_url = "http://so" + mSO + ".myaccount.totaltv.com.tw/CnsApi/Api/STB/";
		member_center_vod_point_url = "https://cnsapp.cns.net.tw/cnsweb/vod_point.php";

		Log.d(TAG, "MemberCenter url prefix: " + member_center_url);

		if (api.equals("QueryCustInfo")) {
			sendQueryCustInfo();
		} else if (api.equals("QueryPrepayAmount")) {
			sendQueryPrepayAmount();
		} else if (api.equals("CrmPayBill")) {
			sendCrmPayBill();
		} else if (api.equals("QueryCrmMyAccount")) {
			sendQueryCrmMyAccount();
		} else if (api.equals("GetVodPoint")) {
			sendGetVodPoint();
		}
	}

    public void sendQueryCustInfo() {
	    String mUrl = member_center_url + "QueryCustInfo" + "/";
	    Log.d(TAG, "MemberCenter url: " + mUrl);
	    GsonRequest<QueryCustInfoResponseJson> configGsonRequest =
		new GsonRequest<QueryCustInfoResponseJson>(Request.Method.POST, mUrl, QueryCustInfoResponseJson.class,  new Response.Listener<QueryCustInfoResponseJson>() {
		    @Override
		    public void onResponse(QueryCustInfoResponseJson response) {
				Log.d(TAG, "onResponse In");
				if(response != null)
					Log.d(TAG, "QueryCustInfo response: " + response.toString());

				Bundle bundle = new Bundle();
				bundle.putString("API", "QueryCustInfo");

				Message msg = new Message();
				msg.obj = response;
				msg.setData(bundle);
				mHandler.sendMessage(msg);
		    }
		},
		new Response.ErrorListener() {
		    @Override
		    public void onErrorResponse(VolleyError error) {
			Log.d(TAG, "Error: " + error.toString());
			Bundle bundle = new Bundle();
			bundle.putString("ACTION", "VolleyError");
			bundle.putString("ErrorMsg", error.toString());
			Message msg = new Message();
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		    }
		}
	    ) {
		@Override
		public byte[] getBody() throws AuthFailureError {
		    String postData = "";
			postData = postDataQueryCustInfo(mQueryCustInfoOBJ.getCrmId(), mQueryCustInfoOBJ.getDeviceSNo3(), mQueryCustInfoOBJ.getSmartCard());

			Log.d(TAG, "Body:" + postData);
		    try {
			return postData == null ? null :
			    postData.getBytes(getParamsEncoding());
		    } catch (UnsupportedEncodingException uee) {
				return null;
		    }
		}
	    };
	    mQueue.add(configGsonRequest);
    }

	public void sendQueryPrepayAmount() {
		String mUrl = member_center_url + "QueryPrepayAmount" + "/";
		Log.d(TAG, "MemberCenter url: " + mUrl);
		GsonRequest<QueryPrepayAmountResponseJson> configGsonRequest =
				new GsonRequest<QueryPrepayAmountResponseJson>(Request.Method.POST, mUrl, QueryPrepayAmountResponseJson.class,  new Response.Listener<QueryPrepayAmountResponseJson>() {
					@Override
					public void onResponse(QueryPrepayAmountResponseJson response) {
						Log.d(TAG, "onResponse In");
						Log.d(TAG, "QueryPrepayAmount response: " + response.toString());

						Bundle bundle = new Bundle();
						bundle.putString("API", "QueryPrepayAmount");

						Message msg = new Message();
						msg.obj = response;
						msg.setData(bundle);
						mHandler.sendMessage(msg);
					}
				},
						new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								Log.d(TAG, "Error: " + error.toString());
								Bundle bundle = new Bundle();
								bundle.putString("ACTION", "VolleyError");
								bundle.putString("ErrorMsg", error.toString());
								Message msg = new Message();
								msg.setData(bundle);
								mHandler.sendMessage(msg);
							}
						}
				) {
					@Override
					public byte[] getBody() throws AuthFailureError {
						String postData = "";
						postData = postDataQueryPrepayAmount(mQueryPrepayAmountOBJ.getCrmId(), mQueryPrepayAmountOBJ.getDeviceSNo3(), mQueryPrepayAmountOBJ.getCrmBaseDate(), mQueryPrepayAmountOBJ.getFlag());

						Log.d(TAG, "Body:" + postData);
						try {
							return postData == null ? null :
									postData.getBytes(getParamsEncoding());
						} catch (UnsupportedEncodingException uee) {
							return null;
						}
					}
				};
		mQueue.add(configGsonRequest);
	}

	public void sendCrmPayBill() {
		String mUrl = member_center_url + "CrmPayBill" + "/";
		Log.d(TAG, "MemberCenter url: " + mUrl);
		GsonRequest<CrmPayBillResponseJson> configGsonRequest =
				new GsonRequest<CrmPayBillResponseJson>(Request.Method.POST, mUrl, CrmPayBillResponseJson.class,  new Response.Listener<CrmPayBillResponseJson>() {
					@Override
					public void onResponse(CrmPayBillResponseJson response) {
						Log.d(TAG, "onResponse In");
						Log.d(TAG, "CrmPayBill response: " + response.toString());

						Bundle bundle = new Bundle();
						bundle.putString("API", "CrmPayBill");

						Message msg = new Message();
						msg.obj = response;
						msg.setData(bundle);
						mHandler.sendMessage(msg);
					}
				},
						new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								Log.d(TAG, "Error: " + error.toString());
								Bundle bundle = new Bundle();
								bundle.putString("ACTION", "VolleyError");
								bundle.putString("ErrorMsg", error.toString());
								Message msg = new Message();
								msg.setData(bundle);
								mHandler.sendMessage(msg);
							}
						}
				) {
					@Override
					public byte[] getBody() throws AuthFailureError {
						String postData = "";
						postData = postDataCrmPayBill(mCrmPayBillOBJ.getCrmId(), mCrmPayBillOBJ.getCreditNumber(), mCrmPayBillOBJ.getValidDate(), mCrmPayBillOBJ.getCrmPrepay(), mCrmPayBillOBJ.getCrmMediabillno(), mCrmPayBillOBJ.getDeviceSNo3());

						Log.d(TAG, "Body:" + postData);
						try {
							return postData == null ? null :
									postData.getBytes(getParamsEncoding());
						} catch (UnsupportedEncodingException uee) {
							return null;
						}
					}
				};
		mQueue.add(configGsonRequest);
	}

	public void sendQueryCrmMyAccount() {
		String mUrl = member_center_url + "QueryCrmMyAccount" + "/";
		Log.d(TAG, "MemberCenter url: " + mUrl);
		GsonRequest<QueryCrmMyAccountResponseJson> configGsonRequest =
				new GsonRequest<QueryCrmMyAccountResponseJson>(Request.Method.POST, mUrl, QueryCrmMyAccountResponseJson.class,  new Response.Listener<QueryCrmMyAccountResponseJson>() {
					@Override
					public void onResponse(QueryCrmMyAccountResponseJson response) {
						Log.d(TAG, "onResponse In");
						Log.d(TAG, "QueryCrmMyAccount response: " + response.toString());

						Bundle bundle = new Bundle();
						bundle.putString("API", "QueryCrmMyAccount");

						Message msg = new Message();
						msg.obj = response;
						msg.setData(bundle);
						mHandler.sendMessage(msg);
					}
				},
						new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								Log.d(TAG, "Error: " + error.toString());
								Bundle bundle = new Bundle();
								bundle.putString("ACTION", "VolleyError");
								bundle.putString("ErrorMsg", error.toString());
								Message msg = new Message();
								msg.setData(bundle);
								mHandler.sendMessage(msg);
							}
						}
				) {
					@Override
					public byte[] getBody() throws AuthFailureError {
						String postData = "";
						postData = postDataQueryCrmMyAccount(mQueryCrmMyAccountOBJ.getCrmId(), mQueryCrmMyAccountOBJ.getDeviceSNo3());

						Log.d(TAG, "Body:" + postData);
						try {
							return postData == null ? null :
									postData.getBytes(getParamsEncoding());
						} catch (UnsupportedEncodingException uee) {
							return null;
						}
					}
				};
		mQueue.add(configGsonRequest);
	}

	public void sendGetVodPoint() {
		String mUrl = member_center_vod_point_url + "?so=" + mGetVodPointOBJ.getSoId() + "&devicesno=" + mGetVodPointOBJ.getDevicesno();

		Log.d(TAG, "MemberCenter vod point url: " + mUrl);
		GsonRequest<GetVodPointResponseJson> configGsonRequest =
				new GsonRequest<GetVodPointResponseJson>(Request.Method.GET, mUrl, GetVodPointResponseJson.class,  new Response.Listener<GetVodPointResponseJson>() {
					@Override
					public void onResponse(GetVodPointResponseJson response) {
						Log.d(TAG, "onResponse In");
						if (response != null) {
							Log.d(TAG, "GetVodPoint response: " + response.toString());
						} else {
							Log.d(TAG, "GetVodPoint response: null");
						}

						Bundle bundle = new Bundle();
						bundle.putString("API", "GetVodPoint");
						bundle.putString("QUERY_TIME", Utils.getCurrentTime());
						bundle.putString("VOD_POINT_DUE_DATE", Utils.getVodPointDueDate());


						Message msg = new Message();
						msg.obj = response;
						msg.setData(bundle);
						mHandler.sendMessage(msg);
					}
				},
						new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								Log.d(TAG, "Error: " + error.toString());
								Bundle bundle = new Bundle();
								bundle.putString("ACTION", "VolleyError");
								bundle.putString("ErrorMsg", error.toString());
								Message msg = new Message();
								msg.setData(bundle);
								mHandler.sendMessage(msg);
							}
						}
				);
		mQueue.add(configGsonRequest);
	}

    private String postDataQueryCustInfo(String crmId, String deviceSNo3, String smartCard) {
		String postDataBody;

		postDataBody = "{\"CrmId\":\"" + crmId + "\",\"DeviceSNo3\":\"" + deviceSNo3 + "\",\"SmartCard\":\"" + smartCard + "\"}";

		return postDataBody;
    }

	private String postDataQueryPrepayAmount(String crmId, String deviceSNo3, String crmBaseDate, String flag) {
		String postDataBody;

		postDataBody = "{\"CrmId\":\"" + crmId + "\",\"DeviceSNo3\":\"" + deviceSNo3 + "\",\"CrmBaseDate\":\"" + crmBaseDate + "\",\"Flag\":\"" + flag + "\"}";

		return postDataBody;
	}

	private String postDataCrmPayBill(String crmId, String creditNumber, String validDate, String crmPrepay, String crmMediabillno, String deviceSNo3) {
		String postDataBody;

		postDataBody = "{\"CrmId\":\"" + crmId + "\",\"CreditNumber\":\"" + creditNumber + "\",\"ValidDate\":\"" + validDate + "\",\"CrmPrepay\":\"" + crmPrepay + "\",\"CrmMediabillno\":\"" + crmMediabillno + "\",\"DeviceSNo3\":\"" + deviceSNo3 + "\"}";

		return postDataBody;
	}

	private String postDataQueryCrmMyAccount(String crmId, String deviceSNo3) {
		String postDataBody;

		postDataBody = "{\"CrmId\":\"" + crmId + "\",\"DeviceSNo3\":\"" + deviceSNo3 + "\"}";

		return postDataBody;
	}
}
