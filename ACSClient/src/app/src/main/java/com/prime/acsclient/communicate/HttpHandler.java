package com.prime.acsclient.communicate;


import com.prime.acsclient.ACSService;
import com.prime.acsclient.acsdata.ACSServerJsonFenceName;
import com.prime.acsclient.acsdata.HttpContentValue;
import com.prime.acsclient.acsdata.ParseACSData;
import com.prime.acsclient.acsdata.ProcessACSData;
import com.prime.acsclient.common.CommonDefine;
import com.prime.acsclient.common.CommonFunctionUnit;
import com.prime.acsclient.common.DeviceControlUnit;


import android.app.AlarmManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HttpHandler extends Handler {
    private final static String TAG = "ACS_HttpHandler" ;
    private static HandlerThread g_http_HandlerThread = null;

    public HttpHandler(Looper looper) {
        super(looper);
    }

    public static HttpHandler createHandler() {
        Log.d( TAG, "createHandler") ;
        g_http_HandlerThread = new HandlerThread("http_HandlerThread");
        g_http_HandlerThread.start();
        return new HttpHandler(g_http_HandlerThread.getLooper());
    }

    @Override
    public void handleMessage(Message message)
    {
        int caseid = message.what ;
        switch ( caseid )
        {
            case CommonDefine.ACS_HTTP_PROVISION_CASEID:
            {
                String provision_url = CommonFunctionUnit.combinServerUrl(CommonDefine.ACS_HTTP_POST_PROVISION_TOKEN) ;
                Log.d( TAG, "ACS provision url : " + provision_url ) ;

                JSONObject response_json = (JSONObject) connect_to_http_server( CommonDefine.HTTP_REQUEST_METHOD_POST, provision_url, get_device_info_json() );
                if ( response_json != null )
                {
                    DeviceControlUnit.update_connect_acs_server_time(true);
                    {
                        ParseACSData.parse_provision_response(response_json);
                        ProcessACSData.process_all_data(ProcessACSData.HTTP_PROCESS_PROVISION_DATA);
                        ////////////////// send message to statr process mqtt //////////////////
                        ACSService.notify_acsservice(CommonDefine.ACS_MQTT_SERVER_CONNECT_CASEID, null, 0, 0);
                    }

                    {
                        /////////////////  get AD list /////////////////
                        Message ad_list_mag = new Message();
                        ad_list_mag.what = CommonDefine.ACS_HTTP_POST_AD_LIST_CASEID ;
                        sendMessageDelayed(ad_list_mag, CommonDefine.ONE_SECOND);
                    }

                    {
                        /////////////////  get week highlight list /////////////////
                        Message week_hightlight_msg = new Message();
                        week_hightlight_msg.what = CommonDefine.ACS_HTTP_POST_WEEK_HIGHLIGHT_CASEID ;
                        sendMessageDelayed(week_hightlight_msg, CommonDefine.ONE_SECOND);
                    }

                    {
                        /////////////////  get rank list /////////////////
                        Message rank_list_msg = new Message();
                        rank_list_msg.what = CommonDefine.ACS_HTTP_GET_RANK_LIST_CASEID ;
                        sendMessageDelayed(rank_list_msg, CommonDefine.ONE_SECOND) ;
                    }

                    {
                        /////////////////  get hot video /////////////////
                        Message hot_video_msg = new Message();
                        hot_video_msg.what = CommonDefine.ACS_HTTP_POST_HOT_VIDEO_CASEID ;
                        sendMessageDelayed(hot_video_msg, CommonDefine.ONE_SECOND) ;
                    }

                    {
                        /////////////////  get recommend packages /////////////////
                        Message recommend_packages_msg = new Message();
                        recommend_packages_msg.what = CommonDefine.ACS_HTTP_POST_RECOMMEND_PACKAGES_CASEID ;
                        sendMessageDelayed(recommend_packages_msg, CommonDefine.ONE_SECOND) ;
                    }

                    {
                        ////////////////// check launcher //////////////////
                        Message launcher_msg = new Message();
                        launcher_msg.what = CommonDefine.ACS_HTTP_LAUNCHERINFO_CASEID;
                        sendMessageDelayed(launcher_msg, 10 * CommonDefine.ONE_SECOND);

                        Message launcher_daily_msg = Message.obtain(launcher_msg);
                        launcher_daily_msg.arg1 = CommonDefine.IS_DATILY_CHECK;
                        sendMessageDelayed(launcher_daily_msg, HttpContentValue.ProvisionResponse.launcherinfo_check_daily_interval * CommonDefine.ONE_SECOND);
                    }

                    {
                        ////////////////// check app //////////////////
                        Message app_msg = new Message();
                        app_msg.what = CommonDefine.ACS_HTTP_APPINFO_CASEID;
                        sendMessageDelayed(app_msg, 40 * CommonDefine.ONE_SECOND);

                        Message app_daily_msg = Message.obtain(app_msg);
                        app_daily_msg.arg1 = CommonDefine.IS_DATILY_CHECK;
                        sendMessageDelayed(app_daily_msg, HttpContentValue.ProvisionResponse.appinfo_check_daily_interval * CommonDefine.ONE_SECOND);
                    }

                    {
                        ////////////////// check fw //////////////////
                        Message fw_msg = new Message();
                        fw_msg.what = CommonDefine.ACS_HTTP_FWINFO_CASEID;
                        sendMessageDelayed(fw_msg, 70 * CommonDefine.ONE_SECOND);

                        Message fw_daily_msg = Message.obtain(fw_msg);
                        fw_daily_msg.arg1 = CommonDefine.IS_DATILY_CHECK;
                        sendMessageDelayed(fw_daily_msg, HttpContentValue.ProvisionResponse.fwinfo_check_daily_interval * CommonDefine.ONE_SECOND);
                    }
                }
                else
                {
                    DeviceControlUnit.update_connect_acs_server_time(false);
                    sendEmptyMessageDelayed(CommonDefine.ACS_HTTP_PROVISION_CASEID, CommonDefine.ACS_HTTP_CONNECT_FAIL_RETRY_DURATION);
                }
            }break;
            case CommonDefine.ACS_HTTP_APPINFO_CASEID:
            {
                JSONObject appinfo_jsonobj = (JSONObject) connect_to_http_server( CommonDefine.HTTP_REQUEST_METHOD_POST, HttpContentValue.ProvisionResponse.http_appinfo_url, get_device_info_json() );
                if ( appinfo_jsonobj != null  )
                {
                    ParseACSData.parse_appinfo_response( appinfo_jsonobj ) ;
                    ProcessACSData.process_all_data(ProcessACSData.HTTP_PROCESS_APPINFO_DATA);
                }

                if ( message.arg1 == CommonDefine.IS_DATILY_CHECK )
                    sendMessageDelayed(Message.obtain(message), HttpContentValue.ProvisionResponse.appinfo_check_daily_interval*CommonDefine.ONE_SECOND);
                else
                    sendMessageDelayed(Message.obtain(message), HttpContentValue.ProvisionResponse.appinfo_check_interval*CommonDefine.ONE_SECOND);
            }break;
            case CommonDefine.ACS_HTTP_FWINFO_CASEID:
            {
                JSONObject fwinfo_jsonobj = (JSONObject) connect_to_http_server( CommonDefine.HTTP_REQUEST_METHOD_POST, HttpContentValue.ProvisionResponse.http_fwinfo_url, get_device_info_json() );
                if ( fwinfo_jsonobj != null )
                {
                    ParseACSData.parse_fwinfo_response( fwinfo_jsonobj ) ;
                    ProcessACSData.process_all_data(ProcessACSData.HTTP_PROCESS_FWINFO_DATA);
                }

                if ( message.arg1 == CommonDefine.IS_DATILY_CHECK )
                    sendMessageDelayed(Message.obtain(message), HttpContentValue.ProvisionResponse.fwinfo_check_daily_interval*CommonDefine.ONE_SECOND);
                else
                    sendMessageDelayed(Message.obtain(message), HttpContentValue.ProvisionResponse.fwinfo_check_interval*CommonDefine.ONE_SECOND);
            }break;
            case CommonDefine.ACS_HTTP_LAUNCHERINFO_CASEID:
            {
                JSONObject launcherinfo_jsonobj = (JSONObject) connect_to_http_server( CommonDefine.HTTP_REQUEST_METHOD_POST, HttpContentValue.ProvisionResponse.http_launchernfo_url, get_device_info_json() );
                if ( launcherinfo_jsonobj != null )
                {
                    ParseACSData.parse_launcherinfo_response(launcherinfo_jsonobj) ;
                    ProcessACSData.process_all_data(ProcessACSData.HTTP_PROCESS_LAUNCHERINFO_DATA);
                }

                if ( message.arg1 == CommonDefine.IS_DATILY_CHECK )
                    sendMessageDelayed(Message.obtain(message), HttpContentValue.ProvisionResponse.launcherinfo_check_daily_interval*CommonDefine.ONE_SECOND);
                else
                    sendMessageDelayed(Message.obtain(message), HttpContentValue.ProvisionResponse.launcherinfo_check_interval*CommonDefine.ONE_SECOND);
            }break;
            case CommonDefine.ACS_HTTP_POST_AD_LIST_CASEID:
            {
                String ad_url = CommonFunctionUnit.combinServerUrl(CommonDefine.ACS_HTTP_POST_AD_TOKEN) ;
                JSONObject ad_list_response = (JSONObject) connect_to_http_server( CommonDefine.HTTP_REQUEST_METHOD_POST, ad_url, get_device_info_json() );
                if ( ad_list_response != null )
                {
                    JSONArray ad_list_data = CommonFunctionUnit.get_json_jsonarray(ad_list_response, ACSServerJsonFenceName.AdList.AD_KEY, HttpContentValue.AdList.ad_list_rawdata) ;
                    ParseACSData.parse_ad_list_response(ad_list_data);
                    ProcessACSData.process_all_data(ProcessACSData.HTTP_PROCESS_AD_LIST_DATA);
                }
            }break;
            case CommonDefine.ACS_HTTP_POST_WEEK_HIGHLIGHT_CASEID:
            {
                String week_highlight_url = CommonFunctionUnit.combinServerUrl(CommonDefine.ACS_HTTP_POST_WEEK_HIGHLIGHT_TOKEN) ;
                JSONObject week_highlight_response = (JSONObject) connect_to_http_server( CommonDefine.HTTP_REQUEST_METHOD_POST, week_highlight_url, get_device_info_json() );
                if ( week_highlight_response != null )
                {
                    JSONArray week_hightlight_data = CommonFunctionUnit.get_json_jsonarray(week_highlight_response, ACSServerJsonFenceName.WeekHightLight.WEEK_HIGHLIGHT, HttpContentValue.WeekHightLight.week_highlight_rawdata) ;
                    ParseACSData.parse_week_highlight_response(week_hightlight_data);
                    ProcessACSData.process_all_data(ProcessACSData.HTTP_PROCESS_WEEK_HIGHLIGHT_DATA);
                }
            }break;
            case CommonDefine.ACS_HTTP_GET_RANK_LIST_CASEID:
            {
                String rank_url = CommonFunctionUnit.combinServerUrl(CommonDefine.ACS_HTTP_GET_RANKLIST_TOKEN) ;
                JSONArray rank_list_response = (JSONArray) connect_to_http_server( CommonDefine.HTTP_REQUEST_METHOD_GET, rank_url, get_device_info_json() );
                if ( rank_list_response != null )
                {
                    ParseACSData.parse_rank_list_response(rank_list_response);
                    ProcessACSData.process_all_data(ProcessACSData.HTTP_PROCESS_RANK_LIST_DATA);
                }
            }break;
            case CommonDefine.ACS_HTTP_CHECK_NETWORK_CONNECT_CASEID:
            {
                String check_network_url = CommonFunctionUnit.combinServerUrl(CommonDefine.ACS_HTTP_POST_CHECK_NETWORK_CONNECT_TOKEN) ;
                int check_interval = HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.network_sync_time * CommonDefine.ONE_SECOND;
                JSONObject check_network_response = (JSONObject) connect_to_http_server( CommonDefine.HTTP_REQUEST_METHOD_POST, check_network_url, get_device_info_json() );
                if ( check_network_response != null )
                {
                    //{"status":200,"message":"success","deviceOnlineStatus":1,"IllegalNetwork":0}
                    HttpContentValue.ProvisionResponse.is_illegal_network = CommonFunctionUnit.get_json_int(check_network_response, ACSServerJsonFenceName.ProvisionResponse.IS_ILLEGAL_NETWORK, HttpContentValue.ProvisionResponse.is_illegal_network);
                    CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.IS_ILLEGAL_NETWORK, HttpContentValue.ProvisionResponse.is_illegal_network, true);
                }

                sendMessageDelayed(Message.obtain(message), check_interval);
            }break;
            case CommonDefine.ACS_HTTP_CHECK_PA_ACS_SERVER_STATUS:
            {
                DeviceControlUnit.check_pa_day();
                sendMessageDelayed(Message.obtain(message), AlarmManager.INTERVAL_HOUR);
            }break;
            case CommonDefine.ACS_HTTP_POST_HOT_VIDEO_CASEID:
            {
                String hot_video_url = CommonFunctionUnit.combinServerUrl(CommonDefine.ACS_HTTP_POST_HOT_VIDEO_TOKEN) ;
                JSONArray hot_video_response = (JSONArray) connect_to_http_server( CommonDefine.HTTP_REQUEST_METHOD_POST, hot_video_url, get_device_info_json() );
                if ( hot_video_response != null )
                {
                    ParseACSData.parse_hot_video_response( hot_video_response ) ;
                    ProcessACSData.process_all_data(ProcessACSData.HTTP_PROCESS_HOT_VIDEO_DATA);
                }

                sendMessageDelayed(Message.obtain(message), AlarmManager.INTERVAL_DAY);
            }break;
            case CommonDefine.ACS_HTTP_POST_RECOMMEND_PACKAGES_CASEID:
            {
                String recommend_packages_url = CommonFunctionUnit.combinServerUrl(CommonDefine.ACS_HTTP_POST_RECOMMEND_PACKAGES_TOKEN) ;
                JSONObject recommend_packages_response = (JSONObject) connect_to_http_server( CommonDefine.HTTP_REQUEST_METHOD_POST, recommend_packages_url, get_device_info_json() );
                if ( recommend_packages_response != null )
                {
                    ParseACSData.parse_recommend_packages_response( recommend_packages_response ) ;
                    ProcessACSData.process_all_data(ProcessACSData.HTTP_PROCESS_RECOMMEND_PACKAGES);

                    String server = HttpContentValue.ProvisionResponse.storage_server ;
                    JSONArray recommend_packages_json = (JSONArray) connect_to_http_server( CommonDefine.HTTP_REQUEST_METHOD_GET, server + CommonDefine.ACS_HTTP_GET_RECOMMEND_PACKAGES_FILE_PATH, get_device_info_json() );
                    if ( recommend_packages_json != null )
                    {
                        ParseACSData.parse_recommend_packages_json_data( recommend_packages_json ) ;
                        ProcessACSData.process_all_data(ProcessACSData.HTTP_PROCESS_RECOMMEND_PACKAGES_JSON_DATA);
                    }
                }

                sendMessageDelayed(Message.obtain(message), AlarmManager.INTERVAL_DAY);
            }break;
            case CommonDefine.ACS_HTTP_UPLOAD_FILE_CASEID:
            {
                Bundle bundle = (Bundle) message.obj;
                String upload_url = bundle.getString(CommonDefine.HTTP_UPLOAD_URL_KEY) ;
                String file_path = bundle.getString(CommonDefine.HTTP_UPLOAD_FILE_PATH_KEY) ;
                new UploadFileTask().execute(upload_url, file_path);
            }break;
            case CommonDefine.ACS_HTTP_RENEW_DEVICE_COMMON_CASEID:
            {
                String provision_url = CommonFunctionUnit.combinServerUrl(CommonDefine.ACS_HTTP_POST_PROVISION_TOKEN) ;
                Log.d( TAG, "RENEW ACS provision url : " + provision_url ) ;
                JSONObject response_json = (JSONObject) connect_to_http_server( CommonDefine.HTTP_REQUEST_METHOD_POST, provision_url, get_device_info_json() );
                if ( response_json != null )
                {
                    ParseACSData.parse_common_device_info(response_json);
                    ProcessACSData.process_all_data(ProcessACSData.MQTT_DATA_COMMON_DEVICE_INFO);
                }
            }break;
        }
    }

    private JSONObject get_device_info_json()
    {
        JSONObject deviceInfo = new JSONObject();
        try {
            deviceInfo.put(ACSServerJsonFenceName.ProvisionDeviceInfo.ETH_MAC_ADDR, HttpContentValue.ProvisionDeviceInfo.eth_mac_address);
            deviceInfo.put(ACSServerJsonFenceName.ProvisionDeviceInfo.ANDROID_ID, HttpContentValue.ProvisionDeviceInfo.android_id);
            deviceInfo.put(ACSServerJsonFenceName.ProvisionDeviceInfo.FW_VERSION, HttpContentValue.ProvisionDeviceInfo.fw_version);
            deviceInfo.put(ACSServerJsonFenceName.ProvisionDeviceInfo.MODEL_NAME, HttpContentValue.ProvisionDeviceInfo.model_name);
            deviceInfo.put(ACSServerJsonFenceName.ProvisionDeviceInfo.SUB_MODEL, HttpContentValue.ProvisionDeviceInfo.sub_model);
            deviceInfo.put(ACSServerJsonFenceName.ProvisionDeviceInfo.ANDROID_VERSION, HttpContentValue.ProvisionDeviceInfo.android_version);
            deviceInfo.put(ACSServerJsonFenceName.ProvisionDeviceInfo.WIFI_MAC_ADDR, HttpContentValue.ProvisionDeviceInfo.wifi_mac);
            deviceInfo.put(ACSServerJsonFenceName.ProvisionDeviceInfo.SERIAL_NUMBER, HttpContentValue.ProvisionDeviceInfo.serial_number);
            deviceInfo.put(ACSServerJsonFenceName.ProvisionDeviceInfo.CHECK_NETWORK_CONNECT_ETH_MAC, HttpContentValue.ProvisionDeviceInfo.eth_mac_address);
//            CommonFunctionUnit.printClassFields(HttpContentValue.ProvisionDeviceInfo.class);
//            Log.d( TAG, "provision data = " + deviceInfo ) ;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return deviceInfo ;
    }
    private Object connect_to_http_server( String request_method, String server_url, JSONObject post_json )
    {
        JSONObject responseJson ;
        HttpsURLConnection provision_url_connect = null;
        try {
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " try to connect : " + server_url ) ;
            provision_url_connect = (HttpsURLConnection) new URL(server_url).openConnection();
            provision_url_connect.setConnectTimeout(CommonDefine.ACS_HTTP_CONNECT_TIMEOUT_DURATION);
            provision_url_connect.setReadTimeout(CommonDefine.ACS_HTTP_CONNECT_TIMEOUT_DURATION);
            provision_url_connect.setRequestMethod(request_method);
            provision_url_connect.setRequestProperty("Accept", "application/json");
            provision_url_connect.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            provision_url_connect.setUseCaches(false);
            provision_url_connect.setChunkedStreamingMode(0);
            if ( request_method.equals(CommonDefine.HTTP_REQUEST_METHOD_POST) )
            {
                provision_url_connect.setDoOutput(true);
                OutputStream outputStream = provision_url_connect.getOutputStream();
                outputStream.write(post_json.toString().getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();
            }
            provision_url_connect.connect();
            int responseCode = provision_url_connect.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(provision_url_connect.getInputStream(), "utf-8")))
                {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    Log.d( TAG, server_url + " Response: " + response.toString());
                    if ( response.toString().contains("{\"err\":\"Permission denied\"}") ) {
                        provision_url_connect.disconnect();
                        return null;
                    }

                    try {
                        responseJson = new JSONObject(response.toString());
                        provision_url_connect.disconnect();
                        return responseJson;
                    } catch (JSONException e) {
                        try {
                            JSONArray responseArray = new JSONArray(response.toString());
                            provision_url_connect.disconnect();
                            return responseArray;  // 視需求回傳 JSONArray
                        } catch (JSONException ex) {
                            Log.e(TAG, "Response parsing error", ex);
                            provision_url_connect.disconnect();
                            return null;
                        }
                    }
                }
            } else {
                Log.d( TAG, CommonFunctionUnit.getMethodName() + " connect to " + server_url + " error:" + responseCode ) ;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        provision_url_connect.disconnect();
        return null ;
    }

    public class UploadFileTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            int response_code = 0 ;
            String response = null;
            HttpURLConnection httpURLConnection = null ;
            try {
                String server_url = (String) params[0];
                String file_path = (String) params[1];
                Log.d( TAG, "UploadFileTask server_url " + server_url + " file_path " + file_path ) ;
                FileInputStream fileInputStream = new FileInputStream(new File(file_path));
                httpURLConnection = (HttpURLConnection) new URL(server_url).openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setChunkedStreamingMode(0);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                httpURLConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****");
                httpURLConnection.setRequestProperty("uploaded_file", file_path);
                DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                try {
                    dataOutputStream.writeBytes("--*****\r\n");
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + file_path + "\"\r\n");
                    dataOutputStream.writeBytes("\r\n");

                    int bytesAvailable = fileInputStream.available();
                    int maxBufferSize = CommonDefine.HTTP_FILE_SIZE_1MB;
                    byte[] buffer = new byte[Math.min(bytesAvailable, maxBufferSize)];
                    int bytesRead = fileInputStream.read(buffer, 0, buffer.length);
                    while (bytesRead > 0) {
                        dataOutputStream.write(buffer, 0, bytesRead);
                        bytesAvailable = fileInputStream.available();
                        buffer = new byte[Math.min(bytesAvailable, maxBufferSize)];
                        bytesRead = fileInputStream.read(buffer, 0, buffer.length);
                    }
                    dataOutputStream.writeBytes("\r\n");
                    dataOutputStream.writeBytes("--*****--\r\n");
                    fileInputStream.close();
                    dataOutputStream.flush();
                    dataOutputStream.close();

                    int responseCode = httpURLConnection.getResponseCode();
                    response_code = responseCode;

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"))) {
                            StringBuilder responseBuilder = new StringBuilder();
                            String responseLine = null;
                            while ((responseLine = br.readLine()) != null) {
                                responseBuilder.append(responseLine.trim());
                            }
                            response = responseBuilder.toString();
                            Log.d( TAG, server_url + " Response: " + responseBuilder.toString());
                        }
                    } else {
                        Log.d( TAG, "UploadFileTask connect to URL error:" + responseCode ) ;
                    }

                    //////////////////
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            httpURLConnection.disconnect();
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Handle the result (e.g., show a message)
        }
    }
}