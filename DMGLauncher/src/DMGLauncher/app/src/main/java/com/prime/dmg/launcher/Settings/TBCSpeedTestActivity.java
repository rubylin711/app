package com.prime.dmg.launcher.Settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dmg.launcher.ACSDatabase.ACSHelper;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Settings.networkconnection.ConnectionClassManager;
import com.prime.dmg.launcher.Settings.networkconnection.ConnectionQuality;
import com.prime.dmg.launcher.Settings.networkconnection.DeviceBandwidthSampler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;

public class TBCSpeedTestActivity extends Activity {
    private static final String TAG = "TBCSpeedTestActivity";
    public static final long PLAYBACK_STATE_COMPAT_ACTION_PLAY_FROM_MEDIA_ID = 1024;
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_VALUE = "value";
    //public static final Uri CONTENT_URI = Uri.parse("content://com.prime.dmg.launcher.Settings.DataProvider/provider_table1");
    public static final String KEY_NAME = "network_quality_defined";
    private static final String g_data = "DATA";
    private static final String g_permission = "permission";
    private View g_button;
    InputStream g_input;
    private int g_isp_num;
    private TextView g_avg_bandwidth;
    private TextView g_avg_bandwidth2;
    private ConnectionClassManager g_connection_class_manager;
    private TextView g_cur_avg_bandwidth;
    private View g_cur_running_bar;
    private TextView g_current_textview;
    private DeviceBandwidthSampler g_device_bandwidth_sampler;
    private AsyncTask g_measure_bw_task;
    private Button g_measure_cancel;
    private Button g_measure_start;
    private TextView g_network_status;
    private View g_running_bar;
    private View g_running_bar2;
    private TextView g_textview;
    private TextView g_textview2;
    private TextView g_textview_title;
    private TextView g_textview_title2;
    private SharedPreferences g_settings;
    long g_start_time;
    private int g_tries = 0;
    private ConnectionQuality g_connection_class = ConnectionQuality.UNKNOWN;
    Timer g_timer = new Timer(true);
    private Handler g_handler = new Handler();
    private int g_time_in_sec = 0;
    private int g_cur_network_type = 0;

    private ArrayList<String> g_f179_p1 = new ArrayList<>();

    private ArrayList<String> g_f180_p2 = new ArrayList<>();
    private String g_url1 = "BuildConfig.FLAVOR";
    private String g_url2 = "BuildConfig.FLAVOR";
    private int g_bw_level_1 = 1000;
    private int g_bw_level_2 = 6000;
    private int g_bw_level_3 = 25000;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ACSHelper.INTENT_NETWORK_QUALITY_DEFINED_UPDATE)) {
                get_acs_data(context);
                Log.d("gary","INTENT_NETWORK_QUALITY_DEFINED_UPDATE !!");
            }
        }
    };
    private Runnable g_runnable = new Runnable() {
        @Override
        public void run() {
            long filesize = ((long) TBCSpeedTestActivity.this.g_connection_class_manager.get_download_kbits_per_second()) * PLAYBACK_STATE_COMPAT_ACTION_PLAY_FROM_MEDIA_ID;
            TBCSpeedTestActivity.this.g_cur_avg_bandwidth.setText(TBCSpeedTestActivity.get_file_size(filesize));
            switch (TBCSpeedTestActivity.this.g_isp_num) {
                case 1:
                    TBCSpeedTestActivity.this.g_f179_p1.add(TBCSpeedTestActivity.get_file_size1(filesize));
                    break;
                case 2:
                    TBCSpeedTestActivity.this.g_f180_p2.add(TBCSpeedTestActivity.get_file_size1(filesize));
                    break;
            }
            TBCSpeedTestActivity.this.g_current_textview.setText(TBCSpeedTestActivity.this.get_bandwidth_level_str(filesize));
            TBCSpeedTestActivity.this.set_connection_class_color(TBCSpeedTestActivity.this.get_bandwidth_level_int(filesize));
            TBCSpeedTestActivity.this.g_handler.postDelayed(this, 200L);
            //TBCSpeedTestActivity.access_$308(TBCSpeedTestActivity.this);//access$xxx is timer's function
            int timeInSec = TBCSpeedTestActivity.this.g_time_in_sec;
            TBCSpeedTestActivity.this.g_time_in_sec = timeInSec + 1;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle datas = getIntent().getExtras();
        setContentView(R.layout.activity_speed_test);
        Log.i(TAG, "TBCSpeedTestActivity - onCreate");
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        this.g_connection_class_manager = ConnectionClassManager.getInstance();
        this.g_device_bandwidth_sampler = DeviceBandwidthSampler.getInstance();
        this.g_textview = (TextView) findViewById(R.id.isp_1_connection_class);
        this.g_textview.setText("    ");
        this.g_textview2 = (TextView) findViewById(R.id.isp_2_connection_class);
        this.g_textview2.setText("    ");
        this.g_network_status = (TextView) findViewById(R.id.network_status);
        this.g_avg_bandwidth = (TextView) findViewById(R.id.isp_1_bandwidth);
        this.g_avg_bandwidth.setText("0 Mbps");
        this.g_avg_bandwidth2 = (TextView) findViewById(R.id.isp_2_bandwidth);
        this.g_avg_bandwidth2.setText("0 Mbps");
        this.g_running_bar = findViewById(R.id.isp_1_runningBar);
        this.g_running_bar.setVisibility(View.INVISIBLE);
        this.g_running_bar2 = findViewById(R.id.isp_2_runningBar);
        this.g_running_bar2.setVisibility(View.INVISIBLE);
        this.g_measure_start = (Button) findViewById(R.id.test_btn);
        this.g_measure_cancel = (Button) findViewById(R.id.cancel_btn);
        this.g_cur_avg_bandwidth = this.g_avg_bandwidth;
        this.g_measure_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TBCSpeedTestActivity.TAG, "start speed test");
                TBCSpeedTestActivity.this.g_isp_num = 1;
                TBCSpeedTestActivity.this.set_current_object(TBCSpeedTestActivity.this.g_isp_num);
                TBCSpeedTestActivity.this.g_f179_p1.clear();
                TBCSpeedTestActivity.this.g_f180_p2.clear();
                TBCSpeedTestActivity.this.g_time_in_sec = 0;
                TBCSpeedTestActivity.this.g_current_textview.setText("    ");
                TBCSpeedTestActivity.this.g_current_textview.setTextColor(Color.parseColor("#ffffff"));
                TBCSpeedTestActivity.this.g_cur_avg_bandwidth.setText("0 Mbps");
                TBCSpeedTestActivity.this.g_connection_class_manager.reset();
                TBCSpeedTestActivity.this.g_measure_bw_task = new DownloadImage().execute(TBCSpeedTestActivity.this.g_url1);
                TBCSpeedTestActivity.this.g_button = v;
                TBCSpeedTestActivity.this.g_button.setEnabled(false);
                TBCSpeedTestActivity.this.g_button.setFocusable(false);
                TBCSpeedTestActivity.this.g_measure_cancel.requestFocus();
            }
        });
        this.g_measure_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TBCSpeedTestActivity.TAG, "cancel speed test");
                TBCSpeedTestActivity.this.onBackPressed();
            }
        });
        if (netInfo == null) {
            this.g_network_status.setText(getResources().getString(R.string.dmg_settings_signal_network_disconnected));
            this.g_measure_start.setEnabled(false);
            this.g_measure_start.setFocusable(false);
        } else {
            if (datas != null) {
                this.g_url1 = datas.getString("url1");
                this.g_url2 = datas.getString("url2");
            } else {
                this.g_url1 = "http://custom.tbcnet.net.tw/tbc_test_100m.zip";
                this.g_url2 = "http://tpdb.speed2.hinet.net/test_100m.zip";
            }
            if (netInfo.isAvailable()) {
                Log.i(TAG, "network available");
                if (cm.getNetworkInfo(9).getState() == NetworkInfo.State.CONNECTED) {
                    this.g_network_status.setText(getResources().getString(R.string.dmg_settings_signal_ethernet_connected));
                }
                if (cm.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
                    this.g_network_status.setText(getResources().getString(R.string.dmg_settings_signal_wifi_connected));
                }
                if (this.g_network_status.getText().toString().contentEquals("BuildConfig.FLAVOR")) {
                    this.g_network_status.setText(getResources().getString(R.string.dmg_settings_signal_network_disconnected));
                    this.g_measure_start.setEnabled(false);
                    this.g_measure_start.setFocusable(false);
                }
            } else {
                Log.i(TAG, "network not available");
                this.g_network_status.setText(getResources().getString(R.string.dmg_settings_signal_network_disconnected));
                this.g_measure_start.setEnabled(false);
                this.g_measure_start.setFocusable(false);
            }
        }
        get_acs_data(this);
//        get_provider_data(this, "networkQualityDefined");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ACSHelper.INTENT_NETWORK_QUALITY_DEFINED_UPDATE);
        registerReceiver(mBroadcastReceiver,intentFilter, RECEIVER_EXPORTED);
        if (this.g_measure_bw_task != null) {
            this.g_measure_bw_task.cancel(true);
        }
    }

    public String get_current_bandwidth_quality_to_cht(ConnectionQuality currentBandwidthQuality) {
        switch (currentBandwidthQuality.ordinal()) {
            case 0:
                String rtn_q = getResources().getString(R.string.dmg_settings_signal_poor);
                return rtn_q;
            case 1:
                String rtn_q2 = getResources().getString(R.string.dmg_settings_signal_moderate);
                return rtn_q2;
            case 2:
                String rtn_q3 = getResources().getString(R.string.dmg_settings_signal_good);
                return rtn_q3;
            case 3:
                String rtn_q4 = getResources().getString(R.string.dmg_settings_signal_excellent);
                return rtn_q4;
            default:
                return "BuildConfig.FLAVOR";
        }
    }

    protected void set_current_object(int ispNum) {
        Log.d(TAG, "set_current_object: g_isp_num is " + String.valueOf(ispNum));
        switch (ispNum) {
            case 1:
                this.g_current_textview = this.g_textview;
                this.g_cur_avg_bandwidth = this.g_avg_bandwidth;
                this.g_cur_running_bar = this.g_running_bar;
                this.g_textview2.setText("    ");
                this.g_avg_bandwidth2.setText("0 Mbps");
                return;
            case 2:
                this.g_current_textview = this.g_textview2;
                this.g_cur_avg_bandwidth = this.g_avg_bandwidth2;
                this.g_cur_running_bar = this.g_running_bar2;
                return;
            case 3:
                return;
            default:
                this.g_current_textview = this.g_textview;
                this.g_cur_avg_bandwidth = this.g_avg_bandwidth;
                this.g_cur_running_bar = this.g_running_bar;
                return;
        }
    }

    public void reset_all_view() {
        this.g_running_bar.setVisibility(View.INVISIBLE);
        this.g_textview.setText("    ");
        this.g_avg_bandwidth.setText("0 Mbps");
        this.g_running_bar2.setVisibility(View.INVISIBLE);
        this.g_textview2.setText("    ");
        this.g_avg_bandwidth2.setText("0 Mbps");
    }

    public String get_bandwidth_level_str(long filesize) {
        double value;
        if (filesize <= 0) {
            return getResources().getString(R.string.dmg_settings_signal_poor);
        }
        int digitGroups = (int) (Math.log10(filesize) / Math.log10(1024.0d));
        try {
            value = Double.parseDouble(new DecimalFormat("#,##0.#").format(filesize / Math.pow(1024.0d, digitGroups)));
        } catch (NumberFormatException e) {
            value = 1.0d;
            digitGroups = 2;
        }
        if (digitGroups == 2) {
            if (value * 1000.0d < this.g_bw_level_1) {
                return getResources().getString(R.string.dmg_settings_signal_poor);
            }
            if (value * 1000.0d < this.g_bw_level_2) {
                return getResources().getString(R.string.dmg_settings_signal_moderate);
            }
            if (1000.0d * value < this.g_bw_level_3) {
                return getResources().getString(R.string.dmg_settings_signal_good);
            }
            return getResources().getString(R.string.dmg_settings_signal_excellent);
        } else if (digitGroups == 1) {
            return getResources().getString(R.string.dmg_settings_signal_poor);
        } else {
            if (digitGroups == 0) {
                return getResources().getString(R.string.dmg_settings_signal_poor);
            }
            return getResources().getString(R.string.dmg_settings_signal_excellent);
        }
    }

    public int get_bandwidth_level_int(long filesize) {
        double value;
        if (filesize <= 0) {
            return 0;
        }
        int digitGroups = (int) (Math.log10(filesize) / Math.log10(1024.0d));
        try {
            value = Double.parseDouble(new DecimalFormat("#,##0.#").format(filesize / Math.pow(1024.0d, digitGroups)));
        } catch (NumberFormatException e) {
            digitGroups = 2;
            value = 1.0d;
        }
        if (digitGroups != 2) {
            return (digitGroups == 1 || digitGroups == 0) ? 0 : 3;
        } else if (value * 1000.0d < this.g_bw_level_1) {
            return 0;
        } else {
            if (value * 1000.0d < this.g_bw_level_2) {
                return 1;
            }
            return 1000.0d * value < ((double) this.g_bw_level_3) ? 2 : 3;
        }
    }

    public static String get_file_size(long size) {
        if (size <= 0) {
            return "0 Mbps";
        }
        String[] units = {"bps", "Kbps", "Mbps", "Gbps", "Tbps"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024.0d));
        try {
            Double.parseDouble(new DecimalFormat("#,##0.#").format(size / Math.pow(1024.0d, digitGroups)));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024.0d, digitGroups)) + " " + units[digitGroups];
        } catch (NumberFormatException e) {
            return "1 Mpbs";
        }
    }

    public static String get_file_size1(long size) {
        if (size <= 0) {
            return "0 Mbps";
        }
        return new DecimalFormat("#,##0.#").format((size / PLAYBACK_STATE_COMPAT_ACTION_PLAY_FROM_MEDIA_ID) / PLAYBACK_STATE_COMPAT_ACTION_PLAY_FROM_MEDIA_ID);
    }

    public static String get_file_size2(long size) {
        if (size <= 0) {
            return "0 Mbits/s";
        }
        String[] units = {"bits/s", "Kbits/s", "Mbits/s", "Gbits/s", "Tbits/s"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024.0d));
        try {
            Double.parseDouble(new DecimalFormat("#,##0.#").format(size / Math.pow(1024.0d, digitGroups)));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024.0d, digitGroups)) + " " + units[digitGroups];
        } catch (NumberFormatException e) {
            return "1 Mbits/s";
        }
    }

    public void set_connection_class_color(int mConnectionClassNum) {
        switch (mConnectionClassNum) {
            case 0:
                this.g_current_textview.setTextColor(Color.parseColor("#DC2020"));
                break;
            case 1:
                this.g_current_textview.setTextColor(Color.parseColor("#DD8700"));
                break;
            case 2:
                this.g_current_textview.setTextColor(Color.parseColor("#DADD00"));
                break;
            case 3:
                this.g_current_textview.setTextColor(Color.parseColor("#00D961"));
                break;
            default:
                this.g_current_textview.setTextColor(Color.parseColor("#ffffff"));
                break;
        }
        this.g_current_textview.setTypeface(null, Typeface.BOLD);
    }

    private boolean get_acs_data(Context context){
        String networkQualityDefined = ACSHelper.get_NetworkQualityDefined(context);
        if(networkQualityDefined != null) {
            try {
                JSONArray jsonArr = new JSONArray(networkQualityDefined);
                int level = 0;
                while (level < jsonArr.length()) {
                    JSONObject jsonObj = jsonArr.getJSONObject(level);
                    int level2 = jsonObj.getInt("lowest");
                    if (level == 0) {
                        this.g_bw_level_3 = level2 * 1000;
                    } else {
                        if (level == 1) {
                            this.g_bw_level_2 = level2 * 1000;
                        } else if (level == 2) {
                            this.g_bw_level_1 = level2 * 1000;
                        }
                    }
                    level++;
                }
                return false;
            } catch (JSONException e) {
//                throw new RuntimeException(e);
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    /*private boolean get_provider_data(Context context, String dataName) {
        boolean ret;
        boolean ret2 = false;
        String[] proj = {"name", "value"};
        int i = 1;
        int i2 = 0;
        String[] selectionArgs = {dataName};
        try {
            Cursor cursor = context.getContentResolver().query(CONTENT_URI, proj, "name=?", selectionArgs, null);
            if (cursor == null) {
                return false;
            }
            int cnt = cursor.getCount();
            if (cnt > 0) {
                while (cursor.moveToNext()) {
                    cursor.getString(i2);
                    String value = cursor.getString(i);
                    JSONArray jsonArr = new JSONArray(value);
                    int level = i2;
                    while (level < jsonArr.length()) {
                        JSONObject jsonObj = jsonArr.getJSONObject(level);
                        int level2 = jsonObj.getInt("lowest");
                        StringBuilder sb = new StringBuilder();
                        boolean ret3 = ret2;
                        try {
                            sb.append("i :");
                            sb.append(level);
                            sb.append(" level :");
                            sb.append(level2);
                            Log.i("TBCSpeedTestActivity", sb.toString());
                            if (level == 0) {
                                this.g_bw_level_3 = level2 * 1000;
                                i = 1;
                            } else {
                                i = 1;
                                if (level == 1) {
                                    this.g_bw_level_2 = level2 * 1000;
                                } else if (level == 2) {
                                    this.g_bw_level_1 = level2 * 1000;
                                }
                            }
                            level++;
                            ret2 = ret3;
                        } catch (IllegalArgumentException e) {
                            ret2 = ret3;
                            Log.i("TBCSpeedTestActivity", "IllegalArgumentException");
                            return ret2;
                        } catch (Exception e2) {
                            ret2 = ret3;
                            Log.i("TBCSpeedTestActivity", "Exception");
                            return ret2;
                        }
                    }
                    i2 = 0;
                }
                ret = true;
            } else {
                ret = false;
            }
            ret2 = ret;
            try {
                cursor.close();
                return ret2;
            } catch (IllegalArgumentException e3) {
                Log.i("TBCSpeedTestActivity", "IllegalArgumentException");
                return ret2;
            } catch (Exception e4) {
                Log.i("TBCSpeedTestActivity", "Exception");
                return ret2;
            }
        } catch (IllegalArgumentException e5) {
        } catch (Exception e6) {
        }
        return ret2;
    }*/

    private class DownloadImage extends AsyncTask<String, Void, Void> {
        private DownloadImage() {
        }

        @Override
        protected void onCancelled() {
            TBCSpeedTestActivity.this.g_device_bandwidth_sampler.stop_sampling();
            TBCSpeedTestActivity.this.g_handler.removeCallbacks(TBCSpeedTestActivity.this.g_runnable);
            if (!TBCSpeedTestActivity.this.g_device_bandwidth_sampler.is_sampling()) {
                TBCSpeedTestActivity.this.reset_all_view();
            }
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            TBCSpeedTestActivity.this.g_device_bandwidth_sampler.start_sampling();
            TBCSpeedTestActivity.this.g_handler.postDelayed(TBCSpeedTestActivity.this.g_runnable, 200L);
            TBCSpeedTestActivity.this.g_start_time = System.currentTimeMillis() / 1000;
            TBCSpeedTestActivity.this.g_cur_running_bar.setVisibility(View.VISIBLE);
            long filesize = ((long) TBCSpeedTestActivity.this.g_connection_class_manager.get_download_kbits_per_second()) * PLAYBACK_STATE_COMPAT_ACTION_PLAY_FROM_MEDIA_ID;
            switch (TBCSpeedTestActivity.this.g_isp_num) {
                case 1:
                    TBCSpeedTestActivity.this.g_f179_p1.add(TBCSpeedTestActivity.get_file_size1(filesize));
                    return;
                case 2:
                    TBCSpeedTestActivity.this.g_f180_p2.add(TBCSpeedTestActivity.get_file_size1(filesize));
                    return;
                default:
                    return;
            }
        }

        @Override
        public Void doInBackground(String... url) {
            String imageURL = url[0];
            try {
                URLConnection connection = new URL(imageURL).openConnection();
                connection.setUseCaches(false);
                connection.connect();
                TBCSpeedTestActivity.this.g_input = connection.getInputStream();
                byte[] buffer = new byte[8192];
                while (TBCSpeedTestActivity.this.g_input.read(buffer) != -1) {
                    if (TBCSpeedTestActivity.this.g_time_in_sec >= 100) {
                        TBCSpeedTestActivity.this.g_input.close();
                    }
                }
                TBCSpeedTestActivity.this.g_input.close();
                return null;
            } catch (IOException e) {
                Log.e(TBCSpeedTestActivity.TAG, "Error while downloading image.\n" + e.toString());
                return null;
            }
        }

        @Override
        public void onPostExecute(Void v) {
            long filesize = ((long) TBCSpeedTestActivity.this.g_connection_class_manager.get_download_kbits_per_second()) * PLAYBACK_STATE_COMPAT_ACTION_PLAY_FROM_MEDIA_ID;
            switch (TBCSpeedTestActivity.this.g_isp_num) {
                case 1:
                    TBCSpeedTestActivity.this.g_f179_p1.add(TBCSpeedTestActivity.get_file_size1(filesize));
                    break;
                case 2:
                    TBCSpeedTestActivity.this.g_f180_p2.add(TBCSpeedTestActivity.get_file_size1(filesize));
                    break;
            }
            TBCSpeedTestActivity.this.g_current_textview.setText(TBCSpeedTestActivity.this.get_bandwidth_level_str(filesize));
            TBCSpeedTestActivity.this.set_connection_class_color(TBCSpeedTestActivity.this.get_bandwidth_level_int(filesize));
            TBCSpeedTestActivity.this.g_device_bandwidth_sampler.stop_sampling();
            TBCSpeedTestActivity.this.g_handler.removeCallbacks(TBCSpeedTestActivity.this.g_runnable);
            if (!TBCSpeedTestActivity.this.g_device_bandwidth_sampler.is_sampling()) {
                TBCSpeedTestActivity.this.g_cur_running_bar.setVisibility(View.INVISIBLE);
                TBCSpeedTestActivity.this.g_cur_avg_bandwidth.setText(TBCSpeedTestActivity.get_file_size(filesize));
            }
            //TBCSpeedTestActivity.access_$008(TBCSpeedTestActivity.this);//access$xxx is timer's function
            int ispNum = TBCSpeedTestActivity.this.g_isp_num;
            TBCSpeedTestActivity.this.g_isp_num = ispNum + 1;
            if (TBCSpeedTestActivity.this.g_isp_num == 3) {
                TBCSpeedTestActivity.this.g_button.setEnabled(true);
                TBCSpeedTestActivity.this.g_button.setFocusable(true);
                Log.i("TBCSpeedTestActivity", "finish speed test");
                Log.i("TBCSpeedTestActivity", "=======================");
                Log.i("TBCSpeedTestActivity", "Point 1:" + TBCSpeedTestActivity.this.g_url1);
                Log.i("TBCSpeedTestActivity", "Speed:" + ((Object) TBCSpeedTestActivity.this.g_cur_avg_bandwidth.getText()));
                Log.i("TBCSpeedTestActivity", "Quality:" + ((Object) TBCSpeedTestActivity.this.g_textview.getText()));
                Log.i("TBCSpeedTestActivity", "=======================");
                Log.i("TBCSpeedTestActivity", "Point 2:" + TBCSpeedTestActivity.this.g_url2);
                Log.i("TBCSpeedTestActivity", "Speed:" + ((Object) TBCSpeedTestActivity.this.g_avg_bandwidth2.getText()));
                Log.i("TBCSpeedTestActivity", "Quality:" + ((Object) TBCSpeedTestActivity.this.g_textview2.getText()));
                Log.i("TBCSpeedTestActivity", "=======================");
                /*try { //unknown sendBroadcast
                    JSONArray subJsonArray = new JSONArray();
                    JSONObject subJson = new JSONObject();
                    subJson.put("URL", TBCSpeedTestActivity.this.g_url1);
                    subJson.put("Speed", TBCSpeedTestActivity.this.g_avg_bandwidth.getText().toString());
                    subJson.put("Quality", TBCSpeedTestActivity.this.g_textview.getText().toString());
                    subJsonArray.put(subJson);
                    JSONObject subJson2 = new JSONObject();
                    subJson2.put("URL", TBCSpeedTestActivity.this.g_url2);
                    subJson2.put("Speed", TBCSpeedTestActivity.this.g_avg_bandwidth2.getText().toString());
                    subJson2.put("Quality", TBCSpeedTestActivity.this.g_textview2.getText().toString());
                    subJsonArray.put(subJson2);
                    Log.i("TBCSpeedTestActivity", "subJsonArray:" + subJsonArray.toString());
                    Intent it = new Intent("com.android.tv.settings.FINISH_SPEED_TEST");
                    it.putExtra("SpeedTest", subJsonArray.toString());
                    if (Build.VERSION.SDK_INT < 19) {
                        TBCSpeedTestActivity.this.sendBroadcast(it);
                    } else {
                        TBCSpeedTestActivity.this.sendBroadcastAsUser(it, UserHandle.getUserHandleForUid(-1));
                        //-3, send to the current user, but if this is calling from a user process then we will send to the caller's user instead of failing wiht a security exception
                        //Process.myUserHandle(), get current user handle

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            } else {
                TBCSpeedTestActivity.this.set_current_object(TBCSpeedTestActivity.this.g_isp_num);
                TBCSpeedTestActivity.this.g_current_textview.setText(TBCSpeedTestActivity.this.get_current_bandwidth_quality_to_cht(TBCSpeedTestActivity.this.g_connection_class_manager.get_current_bandwidth_quality()));
                TBCSpeedTestActivity.this.g_time_in_sec = 0;
                TBCSpeedTestActivity.this.g_current_textview.setTextColor(Color.parseColor("#ffffff"));
                TBCSpeedTestActivity.this.g_cur_avg_bandwidth.setText("0 Mbps");
                TBCSpeedTestActivity.this.g_connection_class_manager.reset();
            }
            if (TBCSpeedTestActivity.this.g_isp_num == 2) {
                TBCSpeedTestActivity.this.g_running_bar.setVisibility(View.INVISIBLE);
                TBCSpeedTestActivity.this.g_measure_bw_task = new DownloadImage().execute(TBCSpeedTestActivity.this.g_url2);
            }
        }
    }
}
