package com.prime.homeplus.settings;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.prime.homeplus.settings.data.MD5;
import com.prime.homeplus.settings.data.ProductAPIResponseJson;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class PostDataProduct {
    private static String TAG = "HomePlus-PostDataProduct";
    private String DEFAULT_POST_BASE_URL = "https://cnsatv.totaltv.com.tw:8091/";
    private String product_url;
    private RequestQueue mQueue;
    private Handler mHandler = null;
    private String mGroupId = "";

    public static final String API_CHANGE_GROUP_SINGLE = "api/v3/product/changeGroup/single";

    public PostDataProduct(Context context, Handler handler) {
        mQueue = Volley.newRequestQueue(context);
        mHandler = handler;
    }

    public void sendChangeGroup(String groupId) {
        mGroupId = groupId;
        send(API_CHANGE_GROUP_SINGLE);
    }

    public void send(final String api) {
        Log.d(TAG, "send " + api);
        if (api.equals(API_CHANGE_GROUP_SINGLE)) {
            product_url = DEFAULT_POST_BASE_URL;
            String mUrl = product_url + api;
            Log.d(TAG, "Product url: " + mUrl);
            GsonRequest<ProductAPIResponseJson> configGsonRequest =
                    new GsonRequest<ProductAPIResponseJson>(Request.Method.POST, mUrl, ProductAPIResponseJson.class, new Response.Listener<ProductAPIResponseJson>() {
                        @Override
                        public void onResponse(ProductAPIResponseJson response) {
                            Log.d(TAG, "onResponse In");

                            String code = response.getCode();
                            String message = response.getMessage();
                            String timecost = response.getTimecost();
                            String timestamp = response.getTimestamp();
                            Log.d(TAG, "code: " + code);
                            Log.d(TAG, "message: " + message);
                            Log.d(TAG, "timecost: " + timecost);
                            Log.d(TAG, "timestamp: " + timestamp);

                            Bundle bundle = new Bundle();
                            bundle.putString("API", api);
                            bundle.putString("code", code);
                            bundle.putString("message", message);
                            bundle.putString("timecost", timecost);
                            bundle.putString("timestamp", timestamp);
                            Message msg = new Message();
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }
                    },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d(TAG, "Error: " + error.toString());
                                    error.printStackTrace();
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
                            if (api.equals(API_CHANGE_GROUP_SINGLE)) {
                                if (!mGroupId.equals("")) {
                                    postData = postChangeGroup(mGroupId);
                                } else {
                                    postData = postChangeGroup("product1");
                                }
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

    private String postChangeGroup(String groupId) {
        String postDataBody;
        String sn = android.os.Build.getSerial();
        String command = "00120";
        String timestamp = "" + System.currentTimeMillis();
        MD5 mMD5 = new MD5();
        String sign = mMD5.md5sumStr(command + "IEWGKQNM" + timestamp).toLowerCase();

        postDataBody = "{\"groupId\":\"" + groupId + "\",\"sn\":\"" + sn + "\",\"command\":\"" + command + "\",\"timestamp\":\"" + timestamp + "\",\"sign\":\"" + sign + "\"}";

        return postDataBody;
    }
}
