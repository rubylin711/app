package com.prime.dtv.service.Util;


import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;

import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.service.Player.CasSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class HttpURLConnectionUtil {
    private static final String TAG = "HttpURLConnectionUtil";
    private static final int MAX_RETRIES = 3;
    private static final long INIT_RETRY_DELAY_MS = 1000;

    public static String doGet(String httpUrl) {
        disableSSLCertificateVerify(); // temp disable ssl for alti lab license server
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder result = new StringBuilder();
        try {
            Log.d(TAG, "doGet: httpUrl = " + httpUrl);
            connection = getHttpUrlConnection(httpUrl);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(15000);

            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            connection.setRequestProperty("Content-Type", "application/json");

            connection.connect();
            Log.d(TAG, "doGet: connection.getResponseCode() = " + connection.getResponseCode());
            if (connection.getResponseCode() == 200) {

                is = connection.getInputStream();
                if (null != is) {
                    br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String temp = null;
                    while (null != (temp = br.readLine())) {
                        result.append(temp);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
        Log.d(TAG, "doGet: result.toString() = " + result);
        return result.toString();
    }


    public static String doPost(String httpUrl, String param) {
        StringBuilder result = new StringBuilder();

        HttpURLConnection connection = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        Log.d(TAG, "doPost: httpUrl = " + httpUrl);
        try {

            URL url = new URL(httpUrl);

            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");

            connection.setConnectTimeout(2000);

            connection.setReadTimeout(15000);

            connection.setDoOutput(true);
            connection.setDoInput(true);

            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");

            // if (null != param && param.equals("")) {
            if (null != param) {
                os = connection.getOutputStream();
                Log.d(TAG, "doPost () write param=");
                os.write(param.getBytes("UTF-8"));
                //os.write(param.getBytes());
            }

            Log.d(TAG, "doPost: connection.getResponseCode() = " + connection.getResponseCode());
            if (connection.getResponseCode() == 200) {

                is = connection.getInputStream();
                if (null != is) {
                    Log.d(TAG, "doPost: connection.getInputStream success ");
                    br = new BufferedReader(new InputStreamReader(is, "GBK")); //test...
                    //br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String temp = null;
                    while (null != (temp = br.readLine())) {
                        Log.d(TAG, "doPost: connection.getResponse temp =" + temp);
                        result.append(temp);
                        result.append("\r\n");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
        Log.d(TAG, "doPost: result.toString() = " + result.toString());
        return result.toString();
    }

    public static byte[] doPostByte(String httpUrl, byte[] param, Callback callback, boolean doRetry) {
        disableSSLCertificateVerify(); // temp disable ssl for alti lab license server
        HttpURLConnection connection = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        Log.d(TAG, "doPostByte: httpUrl=" + httpUrl);

        int retryAttempts = 0;
        int maxRetries = doRetry ? MAX_RETRIES : 0;
        long retryDelay = INIT_RETRY_DELAY_MS;

        byte[] data = new byte[2000];
        int offset = 0;
        while (retryAttempts <= maxRetries) {
            try {
                connection = getHttpUrlConnection(httpUrl);
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(15000);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("accept", "*/*");
                connection.setRequestProperty("connection", "Keep-Alive");
                connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
                connection.setRequestProperty("Content-Type", "application/octet-stream");

                // if (null != param && param.equals("")) {
                if (null != param) {
                    os = connection.getOutputStream();
                    Log.d(TAG, "doPostByte () write param=");
                    //os.write(param.getBytes("UTF-8"));
                    os.write(param);
                }

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "doPostByte: connection.getResponseCode() = " + responseCode);
                if (responseCode == 200) {
                    is = connection.getInputStream();
                    if (null != is) {
                        Log.d(TAG, "doPostByte: connection.getInputStream success ");
                        int readNum = -1;
                        int readLength = 2000 - offset;
                        while ((readNum = is.read(data, offset, readLength)) != -1) {

                            offset = offset + readNum;
                            readLength = 2000 - offset;
                            Log.d(TAG, "doPostByte: connection.getInputStream readNum = " + readNum + " offset = " + offset + " readLength =" + readLength);
                        }

                        Log.d(TAG, "doPostByte: connection.getInputStream offset = " + offset);
			/*
                    br = new BufferedReader(new InputStreamReader(is, "GBK")); //test...
			//br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String temp = null;
                    while (null != (temp = br.readLine())) {
			   log.d("demo connection.getResponse templength =" +temp.length());
                        result.append(temp);
                        result.append("\r\n");
                    }
                    */
                    }
                } else {
                    if (callback != null) {
                        callback.onHttpResponseError(responseCode);
                    }
                }

                break;
            } catch (SocketTimeoutException e) {
                Log.d(TAG, "doPostByte: SocketTimeoutException");
                retryAttempts++;
                if (retryAttempts <= maxRetries) {
                    try {
                        Log.d(TAG, "doPostByte: retry " + retryAttempts + " in " + retryDelay + " ms...");
                        Thread.sleep(retryDelay);
                        retryDelay *= 2;
                    } catch (InterruptedException ex) {
                        Log.e(TAG, "doPostByte: retry interrupted", ex);
                    }
                } else {
                    Log.w(TAG, "doPostByte: max retries reached");
                    if (callback != null) {
                        callback.onConnectTimeout(e.toString());
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "doPostByte: ", e);
                break;
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        //log.d("result.toString() length= " +result.toString().length());
        //return result.toString();
        byte[] dataRet = new byte[offset];
        System.arraycopy(data, 0, dataRet, 0, offset);
        return dataRet;
    }

    private static void disableSSLCertificateVerify() {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                        return myTrustedAnchors;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");

            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static HttpURLConnection getHttpUrlConnection(String httpUrl) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();

            // device id is serial number, ro.serialno
            // alti DB use lowercase (although sending uppercase is ok too after testing)
            //String device = SystemProperties.get("ro.boot.cstmsnno").toLowerCase();//Build.getSerial().toLowerCase();
            String device;
            if(Pvcfg.IsUseCasn()){
                device = SystemProperties.get("ro.boot.cstmsnno").toLowerCase();
            }
            else{
                device = Build.getSerial().toLowerCase();
            }
            String keyId = "6b00eaff-0ce5-4a6f-832f-1578c93ab49f";

            String key = "bf77fc74a618c750ff510963fc3f75412891f6d47ef957120ef1355271aa52a7";
            // TODO: try to avoid hardcoded key
            byte[] hexKeyBytes = hexStringToByteArray(key);
            /*{
                    (byte)0xbf, (byte)0x77, (byte)0xfc, (byte)0x74,
                    (byte)0xa6, (byte)0x18, (byte)0xc7, (byte)0x50,
                    (byte)0xff, (byte)0x51, (byte)0x09, (byte)0x63,
                    (byte)0xfc, (byte)0x37, (byte)0x75, (byte)0x41,
                    (byte)0x28, (byte)0x91, (byte)0xf6, (byte)0xd4,
                    (byte)0x7e, (byte)0xf9, (byte)0x57, (byte)0x12,
                    (byte)0x0e, (byte)0xf1, (byte)0x35, (byte)0x52,
                    (byte)0x71, (byte)0xaa, (byte)0x52, (byte)0xa7
            };*/

            long unixTime = System.currentTimeMillis() / 1000L; // unix timestamp
            String input = unixTime + device;
            Log.d(TAG, "getHttpUrlConnection: input = " + input);
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            hmacSHA256.init(new SecretKeySpec(hexKeyBytes, "HmacSHA256")); // key type = hex
            byte[] hash = hmacSHA256.doFinal(input.getBytes()); // input type = text
            String strHash = CasSession.bytesToHex(hash);
            Log.d(TAG, "getHttpUrlConnection: Device = " + device);
//            Log.d(TAG, "getHttpUrlConnection: KeyId = " + keyId);
            Log.d(TAG, "getHttpUrlConnection: Timestamp = " + unixTime);
//            Log.d(TAG, "getHttpUrlConnection: Hash = " + strHash);

            // alti custom http header
            connection.setRequestProperty("X-Fortress-Wvc-Device", device);
            connection.setRequestProperty("X-Fortress-Wvc-KeyId", keyId);
            connection.setRequestProperty("X-Fortress-Wvc-Timestamp", String.valueOf(unixTime));
            connection.setRequestProperty("X-Fortress-Wvc-Hash", strHash);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return connection;
    }

    public interface Callback {
        void onHttpResponseError(int responseCode);
        void onConnectTimeout(String msg);
    }
}
