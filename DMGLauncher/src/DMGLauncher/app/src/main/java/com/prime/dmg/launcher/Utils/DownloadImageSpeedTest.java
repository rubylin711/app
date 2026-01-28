package com.prime.dmg.launcher.Utils;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;

import com.prime.dmg.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.dmg.launcher.ACSDatabase.ACSHelper;
import com.prime.dmg.launcher.Settings.TBCSpeedTestActivity;
import com.prime.dmg.launcher.Settings.networkconnection.ConnectionClassManager;
import com.prime.dmg.launcher.Settings.networkconnection.DeviceBandwidthSampler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class DownloadImageSpeedTest {
    String TAG = getClass().getSimpleName();

    public static final String NETWORK_TEST_URL1 = "http://custom.tbcnet.net.tw/tbc_test_100m.zip";
    public static final String NETWORK_TEST_URL2 = "http://tpdb.speed2.hinet.net/test_100m.zip";

    private HandlerThread g_handlerThread = null;
    private Handler g_handler;
    private Runnable g_runnable;
    private AsyncTask g_measure_bw_task;
    private DeviceBandwidthSampler g_device_bandwidth_sampler;
    private ConnectionClassManager g_connection_class_manager;
//    private ArrayList<String> g_p1;
//    private ArrayList<String> g_p2;
    private int g_isp_num;
    private int g_time_in_sec = 0;
    private boolean is_running;
    private String g_url1;
    private String g_url2;
    private String g_url1_speed = "0 Mbps";
    private String g_url2_speed = "0 Mbps";
    private Context g_context;
    InputStream g_input;
    long g_start_time;

    public DownloadImageSpeedTest(Context context, Bundle bundle) {
//        g_handler = handler;
//        g_runnable = runnable;
        g_context = context;
        g_connection_class_manager = new ConnectionClassManager();
        g_device_bandwidth_sampler = new DeviceBandwidthSampler(g_connection_class_manager);
//        g_p1 = new ArrayList<>();
//        g_p2 = new ArrayList<>();
        if(bundle != null) {
            g_url1 = bundle.getString("url1",NETWORK_TEST_URL1);
            g_url2 = bundle.getString("url2",NETWORK_TEST_URL2);
        }
        else {
            g_url1 = NETWORK_TEST_URL1;
            g_url2 = NETWORK_TEST_URL2;
        }
        g_isp_num = 1;
        is_running = true;
        g_handlerThread = new HandlerThread("check network speed");
        g_handlerThread.start();
        g_handler = new Handler(g_handlerThread.getLooper());
        g_runnable = new Runnable() {
            @Override
            public void run() {
                long filesize = ((long) g_connection_class_manager.get_download_kbits_per_second()) * TBCSpeedTestActivity.PLAYBACK_STATE_COMPAT_ACTION_PLAY_FROM_MEDIA_ID;
                switch (g_isp_num) {
                    case 1:
                        String speed = TBCSpeedTestActivity.get_file_size1(filesize);
//                        Log.d("gary", "DownloadImageSpeedTest check speed speed = "+speed);
//                        g_p1.add(speed);
                        g_url1_speed = TBCSpeedTestActivity.get_file_size2(filesize);
//                        Log.d("gary", "DownloadImageSpeedTest check speed g_isp_num["+g_isp_num+"] g_p1["+ (g_p1.size()-1) + "] = "+g_url1_speed);
                        break;
                    case 2:
//                        g_p2.add(TBCSpeedTestActivity.get_file_size1(filesize));
                        g_url2_speed = TBCSpeedTestActivity.get_file_size2(filesize);
//                        Log.d("gary", "DownloadImageSpeedTest check speed g_isp_num["+g_isp_num+"] = "+g_url2_speed);
                        break;
                }
                g_handler.postDelayed(g_runnable, 200L);
                //TBCSpeedTestActivity.access_$308(TBCSpeedTestActivity.this);//access$xxx is timer's function
                int timeInSec = g_time_in_sec;
                g_time_in_sec = timeInSec + 1;

            }
        };
        g_measure_bw_task = new DownloadImage().execute(g_url1);
        Log.d(TAG, "DownloadImageSpeedTest init");
    }

    private class DownloadImage extends AsyncTask<String, Void, Void> {
        @Override
        protected void onCancelled() {
            Log.d(TAG, "DownloadImageSpeedTest DownloadImage onCancelled");
            g_device_bandwidth_sampler.stop_sampling();
            g_handler.removeCallbacks(g_runnable);
            if (!g_device_bandwidth_sampler.is_sampling()) {
            }
            g_device_bandwidth_sampler.destory();
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "DownloadImageSpeedTest onPreExecute");
            g_device_bandwidth_sampler.start_sampling();
            g_handler.postDelayed(g_runnable, 200L);
            g_start_time = System.currentTimeMillis() / 1000;
            long filesize = ((long) g_connection_class_manager.get_download_kbits_per_second()) * TBCSpeedTestActivity.PLAYBACK_STATE_COMPAT_ACTION_PLAY_FROM_MEDIA_ID;
            switch (g_isp_num) {
                case 1:
//                    g_p1.add(TBCSpeedTestActivity.get_file_size1(filesize));
                    return;
                case 2:
//                    g_p2.add(TBCSpeedTestActivity.get_file_size1(filesize));
                    return;
                default:
                    return;
            }
        }

        @Override
        public Void doInBackground(String... url) {
            Log.d(TAG, "DownloadImageSpeedTest doInBackground");
            String imageURL = url[0];
            try {
                URLConnection connection = new URL(imageURL).openConnection();
                connection.setUseCaches(false);
                connection.connect();
                g_input = connection.getInputStream();
                byte[] buffer = new byte[8192];
                while (g_input.read(buffer) != -1) {
                    if (g_time_in_sec >= 100) {
                        g_input.close();
                    }
                }
                g_input.close();
                Log.d(TAG, "DownloadImageSpeedTest doInBackground done");
                return null;
            } catch (IOException e) {
                Log.e(TAG, "Error while downloading image.\n" + e.toString());
                return null;
            }
        }

        @Override
        public void onPostExecute(Void v) {
            Log.d(TAG, "DownloadImageSpeedTest onPostExecute g_isp_num = "+g_isp_num);
            long filesize = ((long) g_connection_class_manager.get_download_kbits_per_second()) * TBCSpeedTestActivity.PLAYBACK_STATE_COMPAT_ACTION_PLAY_FROM_MEDIA_ID;
            switch (g_isp_num) {
                case 1:
//                    g_p1.add(TBCSpeedTestActivity.get_file_size1(filesize));
                    break;
                case 2:
//                    g_p2.add(TBCSpeedTestActivity.get_file_size1(filesize));
                    break;
            }
            g_device_bandwidth_sampler.stop_sampling();
            g_handler.removeCallbacks(g_runnable);
            if (!g_device_bandwidth_sampler.is_sampling()) {

            }
            //TBCSpeedTestActivity.access_$008(TBCSpeedTestActivity.this);//access$xxx is timer's function
            int ispNum = g_isp_num;
            g_isp_num = ispNum + 1;
            if (g_isp_num == 3) {
                print_result();
                /*try { //unknown sendBroadcast
                    JSONArray subJsonArray = new JSONArray();
                    JSONObject subJson = new JSONObject();
                    subJson.put("URL", g_url1);
                    subJson.put("Speed", g_avg_bandwidth.getText().toString());
                    subJson.put("Quality", g_textview.getText().toString());
                    subJsonArray.put(subJson);
                    JSONObject subJson2 = new JSONObject();
                    subJson2.put("URL", g_url2);
                    subJson2.put("Speed", g_avg_bandwidth2.getText().toString());
                    subJson2.put("Quality", g_textview2.getText().toString());
                    subJsonArray.put(subJson2);
                    Log.i("TBCSpeedTestActivity", "subJsonArray:" + subJsonArray.toString());
                    Intent it = new Intent("com.android.tv.settings.FINISH_SPEED_TEST");
                    it.putExtra("SpeedTest", subJsonArray.toString());
                    if (Build.VERSION.SDK_INT < 19) {
                        sendBroadcast(it);
                    } else {
                        sendBroadcastAsUser(it, UserHandle.getUserHandleForUid(-1));
                        //-3, send to the current user, but if this is calling from a user process then we will send to the caller's user instead of failing wiht a security exception
                        //Process.myUserHandle(), get current user handle

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                is_running = false;
                destory();
            } else {
                g_time_in_sec = 0;
                g_connection_class_manager.reset();
            }
            if (g_isp_num == 2) {
                if(g_url1_speed.equals("0 Mbps")) {
                    Log.d(TAG, "DownloadImageSpeedTest execute g_url2 = "+g_url2);
                    g_measure_bw_task = new DownloadImage().execute(g_url2);
                }
                else {
                    print_result();
                    is_running = false;
                    destory();
                }
            }
        }

        public void print_result() {
            Log.d(TAG, "finish speed test");
            Log.d(TAG, "=======================");
            Log.d(TAG, "Point 1:" + g_url1);
            Log.d(TAG, "Speed:" + ((Object) g_url1_speed));
//            Log.i("TBCSpeedTestActivity", "Quality:" + ((Object) g_textview.getText()));
            Log.d(TAG, "=======================");
            Log.d(TAG, "Point 2:" + g_url2);
            Log.d(TAG, "Speed:" + ((Object) g_url2_speed));
//            Log.i("TBCSpeedTestActivity", "Quality:" + ((Object) g_textview2.getText()));
            Log.d(TAG, "=======================");
        }
    }

    public void destory() {
        Log.d(TAG, "DownloadImageSpeedTest destory");
        if (g_measure_bw_task != null) {
            g_measure_bw_task.cancel(true);
        }
        if(g_handlerThread != null)
            g_handlerThread.quit();
        if(g_url1_speed.equals("0 Mbps"))
            ACSHelper.set_network_speed_test(g_context,g_url2_speed);
        else
            ACSHelper.set_network_speed_test(g_context,g_url1_speed);
    }

    public boolean is_not_running(){
        return is_running;
    }
}
