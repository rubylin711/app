package com.prime.homeplus.tv.data.network;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.prime.homeplus.tv.PrimeHomeplusTvApplication;
import com.prime.homeplus.tv.utils.PrimeUtils;

public class VDC {
    private static final String TAG = "VDC";
    private String stbId = "";

    public VDC() {
        try {
            stbId = Build.getSerial();
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.toString());
        }
    }

    private void sendVDC(long serviceId, long timeStamp) {
        Log.d(TAG, "send to server:" + serviceId + ", timeStamp:" +  timeStamp);
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        String soId = PrimeUtils.getSoId(PrimeHomeplusTvApplication.getInstance());// "00"; //default value

        String sUrl = "http://so" + soId + ".vdc.totaltv.com.tw/vdc/v1.0/" + soId + "/" + stbId + "/" + serviceId + "/" + timeStamp;
        Log.d(TAG, "url: " + sUrl);

        // send out data
        new MyHttpDownloadTask().execute(sUrl);

    }

    class MyHttpDownloadTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setReadTimeout(6 * 1000);
                c.setConnectTimeout(6 * 1000);
                c.setUseCaches(false);
                c.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");
                c.connect();
                try {
                    int respCode = c.getResponseCode();
                    if (respCode == 200) {
                        String line;
                        StringBuilder stringBuilder = new StringBuilder();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        Log.d(TAG, "server return <" + stringBuilder.toString() + ">");
                    } else {
                        Log.d(TAG, "Error - " + respCode + " " + c.getResponseMessage());
                    }
                } finally {
                    c.disconnect();
                }
            } catch (Exception e) {
                Log.d(TAG, "Error: " + e.toString());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... a) {
            super.onProgressUpdate();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
