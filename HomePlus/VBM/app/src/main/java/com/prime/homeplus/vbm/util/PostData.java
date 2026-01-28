package com.prime.homeplus.vbm.util;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.List;

import android.content.Context;
import android.os.Build;
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
import com.prime.datastructure.config.PropertyDefaultValue;
import com.prime.datastructure.config.Pvcfg;
import com.prime.homeplus.vbm.Utils;
import com.prime.homeplus.vbm.VBMData;
import com.prime.homeplus.vbm.VBMUploadResponse;

public class PostData {
    private static String TAG = "HOMEPLUS_VBM-PostData";
    private String vbm_url;
    private RequestQueue mQueue;
    private Handler mHandler;

    public PostData(Context context, Handler handler) {
        mQueue = Volley.newRequestQueue(context);
        mHandler = handler;
    }

    public void send(String stbId, String soId, List<VBMData> vmbDataList, int listSize) {
        send(stbId, soId, vmbDataList, listSize, 0);
    }

    public void send(String stbId, String soId, List<VBMData> vmbDataList, int listSize, int dropCount) {
        Log.d(TAG, "post vbm data");
        if (listSize == 0) return;

        // URL override（對齊 Benchmark）
        String override = Pvcfg.get_Vbm_Url();
        vbm_url = (override != null && !override.isEmpty()) ? override : PropertyDefaultValue.VBM_URL;

        final int finalListSize = listSize;
        final int finalDropCount = dropCount;

        GsonRequest<VBMUploadResponse> simpleRequest =
                new GsonRequest<VBMUploadResponse>(Request.Method.POST, vbm_url, VBMUploadResponse.class,
                        new Response.Listener<VBMUploadResponse>() {
                            @Override
                            public void onResponse(VBMUploadResponse response) {
                                boolean success = response.getSuccess();
                                String message = response.getMessage();
                                Log.d(TAG, "success: " + success);
                                Log.d(TAG, "message: " + message);
                                if (success) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("message", message);
                                    Message msg = new Message();
                                    msg.what = 999;
                                    msg.setData(bundle);
                                    mHandler.sendMessage(msg);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "Error: " + error.toString());
                                mHandler.sendEmptyMessage(-1);
                            }
                        }
                ) {
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            String content = "";
                            for (int i = 0; i < finalListSize; i++) {
                                String data = vmbDataList.get(i).toUploadString();
                                if (!content.equals("")) content += "\n";
                                content += URLEncoder.encode(data, "utf-8");
                            }

                            // 末端 append Agent 99
                            if (finalDropCount > 0) {
                                VBMData drop = new VBMData(stbId, "99", "0",
                                        String.valueOf(System.currentTimeMillis()),
                                        String.valueOf(finalDropCount), "N/A");
                                if (!content.equals("")) content += "\n";
                                content += URLEncoder.encode(drop.toUploadString(), "utf-8");
                            }

                            String postData = "seriesNumber=" + stbId +
                                    "&SO=" + soId +
                                    "&time=" + String.valueOf(System.currentTimeMillis()) +
                                    "&content=" + content;

                            return postData.getBytes(getParamsEncoding());
                        } catch (UnsupportedEncodingException uee) {
                            return null;
                        }
                    }
                };

        mQueue.add(simpleRequest);
    }
}
