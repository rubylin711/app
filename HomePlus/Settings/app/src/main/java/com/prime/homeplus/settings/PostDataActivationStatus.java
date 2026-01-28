package com.prime.homeplus.settings;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.prime.homeplus.settings.data.ActivationStatusAPIResponseJson;
import com.prime.homeplus.settings.data.ActivationStatusDataJson;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class PostDataActivationStatus {
    private static String TAG = "HomePlus-PostDataActivationStatus";
    private String DEFAULT_POST_BASE_URL = "https://stbgw.homeplus.net.tw/";
    private String activation_status_url;
    private RequestQueue mQueue;
    private Handler mHandler = null;
    private String mSoId = "00";
    private String mCardSn = "";

    public static final String API_ACTIVATION_STATUS = "api/stb/v1/activation_status";

    public PostDataActivationStatus(Context context, Handler handler) {
        mQueue = Volley.newRequestQueue(context);
        mHandler = handler;
    }

    public void sendActivationStatus(String soId, String cardSn) {
        mSoId = soId;
        mCardSn = cardSn;
        send(API_ACTIVATION_STATUS);
    }

    public void send(final String api) {
        Log.d(TAG, "send " + api);
        if (api.equals(API_ACTIVATION_STATUS)) {
            activation_status_url = DEFAULT_POST_BASE_URL;
            String mUrl = activation_status_url + api;
            Log.d(TAG, "Activation Status url: " + mUrl);
            final GsonRequest<ActivationStatusAPIResponseJson> configGsonRequest =
                    new GsonRequest<ActivationStatusAPIResponseJson>(Request.Method.POST, mUrl, ActivationStatusAPIResponseJson.class, new Response.Listener<ActivationStatusAPIResponseJson>() {
                        @Override
                        public void onResponse(ActivationStatusAPIResponseJson response) {
                            Log.d(TAG, "onResponse In");


                            String requestId = response.getRequestId();
                            String code = response.getCode();
                            String messages[] = response.getMessages();
                            Log.d(TAG, "requestId: " + requestId);
                            Log.d(TAG, "code: " + code);
                            Log.d(TAG, "messages: " + Arrays.toString(messages));

                            if (code.equals("0000")) {
                                ActivationStatusDataJson data = response.getData();
                                String crmId = data.getCrmId();
                                String smartCard = data.getSmartCard();
                                String deviceSNo3 = data.getDeviceSNo3();
                                String bid = data.getBid();
                                String zipCode = data.getZipCode();
                                String areaCode = data.getAreaCode();
                                String cmMode = data.getCmMode();
                                Log.d(TAG, "CrmId: " + crmId);
                                Log.d(TAG, "SmartCard: " + smartCard);
                                Log.d(TAG, "DeviceSNo3: " + deviceSNo3);
                                Log.d(TAG, "BID: " + bid);
                                Log.d(TAG, "ZIPCode: " + zipCode);
                                Log.d(TAG, "AreaCode: " + areaCode);
                                Log.d(TAG, "CmMode: " + cmMode);

                                Bundle bundle = new Bundle();
                                bundle.putString("API", api);
                                bundle.putString("requestId", requestId);
                                bundle.putString("code", code);
                                bundle.putStringArray("messages", messages);
                                bundle.putString("CrmId", crmId);
                                bundle.putString("SmartCard", smartCard);
                                bundle.putString("DeviceSNo3", deviceSNo3);
                                bundle.putString("BID", bid);
                                bundle.putString("ZIPCode", zipCode);
                                bundle.putString("AreaCode", areaCode);
                                bundle.putString("CmMode", cmMode);
                                Message msg = new Message();
                                msg.setData(bundle);
                                mHandler.sendMessage(msg);
                            } else {
                                Log.d(TAG, "Return code is not 0000.");
                            }
                        }
                    },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d(TAG, "Error: " + error.toString());
                                    //error.printStackTrace();

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
                            if (api.equals(API_ACTIVATION_STATUS)) {
                                postData = postActivationStatus();
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

    private String postActivationStatus() {
        String postDataBody;
        String sn = android.os.Build.getSerial();

        postDataBody = "{\"CrmId\":\"" + mSoId + "\",\"SmartCard\":\"" + mCardSn + "\",\"DeviceSNo3\":\"" + sn + "\"}";

        return postDataBody;
    }
}
