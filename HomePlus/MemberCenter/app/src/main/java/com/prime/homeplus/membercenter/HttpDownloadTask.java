package com.prime.homeplus.membercenter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class HttpDownloadTask extends AsyncTask<String, Void, Pair<String, String>> {
    private String TAG = "HomePlus-MemberActivity";
    private int timeout = 3 * 1000;
    Context context;

    public interface HttpDownloadResponse {
        void HttpDownloadResponse(Pair<String, String> result);
    }

    public void setTimeout(int timeout){
        this.timeout = timeout;
    }

    HttpDownloadResponse httpDownloadResponse;

    public HttpDownloadTask(Context context) {
        this.context = context;
        this.httpDownloadResponse = (HttpDownloadResponse) context;
    }

    private int dateIndex = 2;

    @Override
    protected Pair doInBackground(String... params) {
        Log.d(TAG, "doInBackground");
        String out = "";
        out = downloadHttp(params[0], params[1]);
        Log.d(TAG, "out = "+out);
        Pair<String, String> result = new Pair(params[1], out);
        return result;
    }

    @Override
    protected void onProgressUpdate(Void... a) {
        super.onProgressUpdate();
    }

    @Override
    protected void onPostExecute(Pair<String, String> result) {
        httpDownloadResponse.HttpDownloadResponse(result);
    }

    private String downloadHttp(String sUrl, String mode) {
        try {
            Log.d(TAG, sUrl);
            URL url = new URL(sUrl);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.setUseCaches(false);
            c.connect();

            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            try {
                int respCode = c.getResponseCode();
                if (respCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line + "\n");
                    }
                } else {
                    stringBuilder.append("Error  - " + respCode + " " + c.getResponseMessage());
                }
            } finally {
                c.disconnect();
            }

            return stringBuilder.toString();
        } catch (MalformedURLException e) {
            String tmp = "URLError: " + e;
            return tmp;
        } catch (IOException e) {
            String tmp = "IOError:" + e;
            return tmp;
        }
    }
}
