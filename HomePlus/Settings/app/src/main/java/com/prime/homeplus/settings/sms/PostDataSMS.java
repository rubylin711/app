package com.prime.homeplus.settings.sms;

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

import com.prime.homeplus.settings.sms.data.APIResponseJson;
import com.prime.homeplus.settings.sms.data.RetData;
import com.prime.homeplus.settings.sms.data.OBJQuerySNO;
import com.prime.homeplus.settings.sms.data.OBJSwapMobilePhone;
import com.prime.homeplus.settings.sms.data.OBJAuthorSTB;

public class PostDataSMS {
    private static String TAG = "HOMEPLUSTV_OTA-PostDataSMS";
    private String sms_url;
    private RequestQueue mQueue;
    private OBJQuerySNO mOBJQuerySNO;
    private OBJSwapMobilePhone mOBJSwapMobilePhone;
    private OBJAuthorSTB mOBJAuthorSTB;
    private Handler mHandler = null;

    public PostDataSMS(Context context, Handler handler) {
        mQueue = Volley.newRequestQueue(context);
        mHandler = handler;
    }

    public void sendQuerySNO(OBJQuerySNO objQuerySNO) {
        mOBJQuerySNO = objQuerySNO;
        send("QuerySNO");
    }

    public void sendSwapMobilePhone(OBJSwapMobilePhone objSwapMobilePhone) {
        mOBJSwapMobilePhone = objSwapMobilePhone;
        send("SwapMobilePhone");
    }

    public void sendAuthorSTB(OBJAuthorSTB objAuthorSTB) {
        mOBJAuthorSTB = objAuthorSTB;
        send("AuthorSTB");
    }

    public void send(final String api) {
        Log.d(TAG, "send " + api);
        if (api.equals("QuerySNO") || api.equals("SwapMobilePhone") || api.equals("AuthorSTB")) {
            String mSO = "20";
            if (api.equals("QuerySNO")) {
                mSO = mOBJQuerySNO.getCrmId();
            } else if (api.equals("SwapMobilePhone")) {
                mSO = mOBJSwapMobilePhone.getCrmId();
            } else if (api.equals("AuthorSTB")) {
                mSO = mOBJAuthorSTB.getCrmId();
            }
            sms_url = "http://so" + mSO + ".myaccount.totaltv.com.tw/CnsApi/Api/STB/";

            String mUrl = sms_url + api + "/";
            Log.d(TAG, "SMS url: " + mUrl);
            GsonRequest<APIResponseJson> configGsonRequest =
                    new GsonRequest<APIResponseJson>(Request.Method.POST, mUrl, APIResponseJson.class, new Response.Listener<APIResponseJson>() {
                        @Override
                        public void onResponse(APIResponseJson response) {
                            Log.d(TAG, "onResponse In");
                            String retCode = response.getRetCode();
                            String retMsg = response.getRetMsg();
                            String retDate = response.getRetDate();
                            String transId = response.getTransId();
                            String UCID = response.getUCID();
                            Log.d(TAG, "RetCode: " + retCode);
                            Log.d(TAG, "RetMsg:  " + retMsg);
                            Log.d(TAG, "RetDate: " + retDate);
                            Log.d(TAG, "TransId: " + transId);
                            Log.d(TAG, "UCID:    " + UCID);

                            RetData retData = response.getRetData();
                            String crmId = retData.getCrmId();
                            String crmWorkOrder = retData.getCrmWorkOrder();
                            String crminstallname = retData.getCrminstallname();
                            String crmBpname = retData.getCrmBpname();
                            String crmWorker1 = retData.getCrmWorker1();
                            String mobilephone = retData.getMobilephone();
                            String cmMode = retData.getCmMode();
                            String bid =  retData.getBid();
                            String zipCode =  retData.getZipCode();
                            String areaCode =  retData.getAreaCode();
                            Log.d(TAG, "RetData-CrmId:        " + crmId);
                            Log.d(TAG, "RetData-CrmWorkOrder: " + crmWorkOrder);
                            Log.d(TAG, "RetData-Crminstallname: " + crminstallname);
                            Log.d(TAG, "RetData-CrmBpname: " + crmBpname);
                            Log.d(TAG, "RetData-CrmWorker1: " + crmWorker1);
                            Log.d(TAG, "RetData-mobilephone: " + mobilephone);
                            Log.d(TAG, "RetData-CmMode: " + cmMode);
                            Log.d(TAG, "RetData-BID: " + bid);
                            Log.d(TAG, "RetData-ZIPCode: " + zipCode);
                            Log.d(TAG, "RetData-AreaCode: " + areaCode);

                            Bundle bundle = new Bundle();
                            bundle.putString("API", api);
                            bundle.putString("RetCode", retCode);
                            bundle.putString("RetMsg", retMsg);
                            bundle.putString("RetDate", retDate);
                            bundle.putString("TransId", transId);
                            bundle.putString("UCID", UCID);
                            bundle.putString("CrmId", crmId);
                            bundle.putString("CrmWorkOrder", crmWorkOrder);
                            bundle.putString("Crminstallname", crminstallname);
                            bundle.putString("CrmBpname", crmBpname);
                            bundle.putString("CrmWorker1", crmWorker1);
                            bundle.putString("mobilephone", mobilephone);
                            bundle.putString("CmMode", cmMode);
                            bundle.putString("BID", bid);
                            bundle.putString("ZIPCode", zipCode);
                            bundle.putString("AreaCode", areaCode);
                            Message msg = new Message();
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
                            if (api.equals("QuerySNO")) {
                                postData = postDataQuerySNO(mOBJQuerySNO.getCrmId(), mOBJQuerySNO.getCrmWorkshortsno());
                            } else if (api.equals("SwapMobilePhone")) {
                                postData = postDataSwapMobilePhone(mOBJSwapMobilePhone.getCrmId(), mOBJSwapMobilePhone.getCrmWorkshortsno(), mOBJSwapMobilePhone.getMobilePhone());
                            } else if (api.equals("AuthorSTB")) {
                                postData = postDataAuthorSTB(mOBJAuthorSTB.getCrmId(), mOBJAuthorSTB.getDeviceSNo3(), mOBJAuthorSTB.getCrmWorkOrder(), mOBJAuthorSTB.getCrmWorker1(), mOBJAuthorSTB.getMobilePhone(), mOBJAuthorSTB.getIncludeHD(), mOBJAuthorSTB.getCustId(), mOBJAuthorSTB.getReturnMode(), mOBJAuthorSTB.getHDSerialNo(), mOBJAuthorSTB.getCmMode());
                            }
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
        } else {
            Log.d(TAG, "Error: " + api + " not support !!");
        }
    }

    private String postDataQuerySNO(String crmId, String crmWorkshortsno) {
        String postDataBody;

        postDataBody = "{\"CrmId\":\"" + crmId + "\",\"CrmWorkshortsno\":\"" + crmWorkshortsno + "\"}";

        return postDataBody;
    }

    private String postDataSwapMobilePhone(String crmId, String crmWorkshortsno, String mobilephone) {
        String postDataBody;

        postDataBody = "{\"CrmId\":\"" + crmId + "\",\"CrmWorkshortsno\":\"" + crmWorkshortsno + "\",\"mobilephone\":\"" + mobilephone + "\"}";

        return postDataBody;
    }


    private String postDataAuthorSTB(String crmId, String deviceSNo3, String crmWorkOrder, String crmWorker1, String mobilePhone, String includeHD, String custId, String returnMode, String hdSerialNo, String cmMode) {
        String postDataBody;

        postDataBody = "{\"CrmId\":\"" + crmId + "\",\"DeviceSNo3\":\"" + deviceSNo3 + "\",\"CrmWorkOrder\":\"" + crmWorkOrder + "\",\"CrmWorker1\":\"" + crmWorker1 + "\" ,\"MobilePhone\":\"" + mobilePhone + "\",\"IncludeHD\":\"" + includeHD + "\" ,\"CustId\":\"" + custId + "\",\"ReturnMode\":\"" + returnMode + "\",\"HDSerialNo\":\"" + hdSerialNo + "\",\"CmMode\":\"" + cmMode + "\"}";

        return postDataBody;
    }

}
