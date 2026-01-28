package com.prime.acsclient.common;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PublicIpFetcher {

    public interface IpFetchCallback {
        void onIpFetched(String service, String ip);
        void onError(Exception e);
    }

    public static void fetchWebIP(String web_url, final IpFetchCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String respose = null;
                Exception exception = null;
                try {
                    URL url = new URL(web_url);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setReadTimeout(3000);
                    urlConnection.setConnectTimeout(3000);
                    urlConnection.connect();

                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    respose = in.readLine();
                    in.close();
                    urlConnection.disconnect();
                } catch (Exception e) {
                    exception = e;
                }

                if (respose != null) {
                    callback.onIpFetched(web_url, respose);
                } else {
                    callback.onError(exception);
                }
            }
        }).start();
    }
}