package com.prime.homeplus.membercenter.TvMail;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.membercenter.R;
import com.prime.homeplus.membercenter.enity.Data;
import com.prime.homeplus.membercenter.enity.RequestData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TvMailService extends Service {
    private String TAG = "TvMailService";

    public static final String PROP_TVMAIL_URL_PREFIX = "cns_tvmail_domain";
    public static final String PROP_TVMAIL_HEARTBEAT = "cns_tvmail_heartbeat";
    public static final String PROP_TVMAIL_HEARTBEAT_DEBUG = "cns_tvmail_debug_heartbeat";
    public static final int DEFAULT_TVMAIL_HEARTBEAT = 60 * 60 * 1000;
    public static final String DEFAULT_TVMAIL_URL = "https://cnsatv.totaltv.com.tw:9102/";

    private static String tvmail_topic_prefix = "api/message/subscribe";
    private static String heartbeat_topic_prefix = "api/message/changeMessageHeartbeatTime/";
    private int firstTimeHeartBeat = 60000;
    private int tvMailHeartBeat;
    private int debugHeartBeat;
    private TvMailServiceReceiver mTvMailServiceReceiver;
    private Handler handler = new Handler();
    private Timer timer = null;
    private static int isHighLevel = 0;
    private static String highLevelMailID = "";
    private TvMailDbHelper tvMailDbHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "TvMailService is onCreate.");

        if (Build.VERSION.SDK_INT >= 26) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startMyOwnForeground();
            } else {
                startForeground(3, new Notification());
            }
        }
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        if (mTvMailServiceReceiver != null) {
            getApplicationContext().unregisterReceiver(mTvMailServiceReceiver);
            mTvMailServiceReceiver = null;
        }
        Log.d(TAG, "TvMailService is onDestroy.");
    }

    public void init() {

        setTvMailTimer();

        if (tvMailDbHelper == null) {
            tvMailDbHelper = new TvMailDbHelper(this);
        }

        String checkDomain = GposInfo.getTvMailUrlPrefix(getApplicationContext());//Settings.System.getString(this.getContentResolver(), PROP_TVMAIL_URL_PREFIX);
        if (TextUtils.isEmpty(checkDomain) || checkDomain.equals("")) {
            GposInfo.setTvMailUrlPrefix(getApplicationContext(), DEFAULT_TVMAIL_URL);//Settings.System.putString(this.getContentResolver(), PROP_TVMAIL_URL_PREFIX, DEFAULT_TVMAIL_URL);
        }

        mTvMailServiceReceiver = new TvMailServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ResetTvMailTimer");
        getApplicationContext().registerReceiver(mTvMailServiceReceiver, intentFilter);
    }

    public void setTvMailTimer() {
        debugHeartBeat = GposInfo.getTvMailDebugHeartBeat(getApplicationContext());//Settings.System.getInt(getContentResolver(), PROP_TVMAIL_HEARTBEAT_DEBUG, 0);
        tvMailHeartBeat = GposInfo.getTvMailHeartBeat(getApplicationContext());//Settings.System.getInt(getContentResolver(), PROP_TVMAIL_HEARTBEAT, DEFAULT_TVMAIL_HEARTBEAT);

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (timer == null) {
            timer = new Timer();
        }

        if (debugHeartBeat == 0) {
            Log.d(TAG, "setTvMailTimer： " + tvMailHeartBeat + " 毫秒（" + (tvMailHeartBeat / 1000 / 60) + " 分鐘）");
            timer.scheduleAtFixedRate(new TimeDisplay(), firstTimeHeartBeat, tvMailHeartBeat);
        } else {
            Log.d(TAG, "setTvMailTimer： use debug Heartbeat.");
            Log.d(TAG, "setTvMailTimer debugHeartBeat: " + debugHeartBeat + " 毫秒（" + (debugHeartBeat / 1000 / 60)
                    + " 分鐘）");
            timer.scheduleAtFixedRate(new TimeDisplay(), firstTimeHeartBeat, debugHeartBeat);
        }

    }

    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.prime.homeplus.membercenter.nid2";
        String channelName = "HOMEPLUS_TVMAIL_SERVICE";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName,
                NotificationManager.IMPORTANCE_NONE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(chan);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentText(channelName)
                .setSmallIcon(androidx.core.R.drawable.notification_icon_background)
                .setWhen(System.currentTimeMillis())
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .build();
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(4, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(4, notification);
        }
    }

    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    connectTvMailServer();
                }
            });
        }
    }

    private void connectTvMailServer() {
        String tvmailDomain = GposInfo.getTvMailUrlPrefix(getApplicationContext());//Settings.System.getString(this.getContentResolver(), PROP_TVMAIL_URL_PREFIX);
        String token = GposInfo.getLauncherToken(getApplicationContext());//Settings.System.getString(this.getContentResolver(), "cns_launcher_token");

        if (TextUtils.isEmpty(token)) {
            return;
        }

        if (TextUtils.isEmpty(tvmailDomain) || tvmailDomain.equals("")) {
            GposInfo.setTvMailUrlPrefix(getApplicationContext(), DEFAULT_TVMAIL_URL);//Settings.System.putString(this.getContentResolver(), PROP_TVMAIL_URL_PREFIX, DEFAULT_TVMAIL_URL);
            tvmailDomain = DEFAULT_TVMAIL_URL;
        }

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();

        String command = "0000";
        String timestamp = System.currentTimeMillis() + "";

        JsonObject request_data = new JsonObject();
        request_data.addProperty("token", token);
        Log.d(TAG, "token: " + token);
        Log.d(TAG, "tvmailDomain: " + tvmailDomain);
        RequestData requestData = new RequestData(command, timestamp, request_data);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"),
                requestData.getRequestBody());

        String requestUrl = "";

        requestUrl = tvmailDomain + tvmail_topic_prefix;

        Log.d(TAG, "requestUrl: " + requestUrl);
        Log.d(TAG, requestData.getRequestBody());
        Request request = new Request.Builder()
                .url(requestUrl)
                .post(body)
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d(TAG, "result: " + result);
                if (!TextUtils.isEmpty(result)) {
                    handleTvMailMessage(result);
                } else {
                    Log.d(TAG, "result is null.");
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "Connect Fail Exception:" + e.toString());
            }
        });
    }

    private void handleTvMailMessage(String result) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(result);
            int code = obj.getInt("code");
            if (code == 999997) {
                Log.d(TAG, "message: Token 失效");
                return;
            } else if (code == 999998) {
                Log.d(TAG, "message: 消息不合法/參數錯誤");
                return;
            } else if (code == 0) {
                int messageHeartbeatTime = obj.getInt("messageHeartbeatTime");
                handleMessageHeartbeatTime(messageHeartbeatTime);

                isHighLevel = 0;
                JSONArray jsonArray = obj.getJSONArray("data");
                if (!TextUtils.isEmpty(jsonArray.toString()) && jsonArray.length() >= 1) {
                    try {
                        Gson gson = new Gson();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject dataObj = new JSONObject(jsonArray.get(i).toString());
                            Log.d(TAG, "dataObj.toString(): " + dataObj.toString());
                            Data data = gson.fromJson(dataObj.toString(), Data.class);
                            tvMailDbHelper.insertTvMailData(data);
                            if (data.getLevel() == 3) {
                                isHighLevel = 1;
                                highLevelMailID = Long.toString(data.getLauMessagePublishId());
                                TvMailManager.handleTvMailInfo(this, highLevelMailID, true);
                                highLevelMailID = "";
                            }
                        }
                        if (isHighLevel != 1) {
                            showTvMailNewDialog();
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "Error: " + e.getMessage());
                    }
                } else {
                    Log.d(TAG, "NO Data");
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }
    }

    private void handleMessageHeartbeatTime(int newHeartBeat) {
        if (debugHeartBeat != 0) {
            Log.d(TAG, "handleMessageHeartbeatTime: 使用debug心跳。");
            return;
        }
        if (newHeartBeat == 0) {
            Log.d(TAG, "messageHeartbeatTime = 0, return.");
            return;
        }
        Log.d(TAG, "messageHeartbeatTime： " + newHeartBeat + " 毫秒（" + (newHeartBeat / 1000 / 60) + " 分鐘）");
        Log.d(TAG, "Now Heartbeat Time： " + tvMailHeartBeat + " 毫秒（" + (tvMailHeartBeat / 1000 / 60) + " 分鐘）");
        if (newHeartBeat == tvMailHeartBeat) {
            Log.d(TAG, "與機上盒目前設定相同。");
        } else if (newHeartBeat <= 0) {
            Log.d(TAG, "messageHeartbeatTime <= 0");
        } else {
            Log.d(TAG, "TvMail HeartbeatTime修改為： " + newHeartBeat + "毫秒（" + (newHeartBeat / 1000 / 60) + "分鐘）");
            GposInfo.setTvMailHeartBeat(getApplicationContext(), newHeartBeat);//Settings.System.putInt(this.getContentResolver(), TvMailService.PROP_TVMAIL_HEARTBEAT, newHeartBeat);
            setTvMailTimer();
            changeMessageHeartbeatTime();
        }
    }

    private void changeMessageHeartbeatTime() {
        String tvmailDomain = GposInfo.getTvMailUrlPrefix(getApplicationContext());//Settings.System.getString(this.getContentResolver(), PROP_TVMAIL_URL_PREFIX);

        if (TextUtils.isEmpty(tvmailDomain) || tvmailDomain.equals("")) {
            GposInfo.setTvMailUrlPrefix(getApplicationContext(), DEFAULT_TVMAIL_URL);//Settings.System.putString(this.getContentResolver(), PROP_TVMAIL_URL_PREFIX, DEFAULT_TVMAIL_URL);
            tvmailDomain = DEFAULT_TVMAIL_URL;
        }

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
        String requestUrl = "";
        requestUrl = tvmailDomain + heartbeat_topic_prefix + tvMailHeartBeat;
        Log.d(TAG, "requestUrl: " + requestUrl);
        Request request = new Request.Builder()
                .url(requestUrl)
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d(TAG, "checkMessageHeartbeatTime, result: " + result);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "Connect Fail Exception:" + e.toString());
            }
        });
    }

    private void showTvMailNewDialog() {
        try {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                Handler mainThread = new Handler(Looper.getMainLooper());
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        TvMailNewDialog.getInstance(getApplicationContext()).showAlertDialog();
                    }
                });
            } else {
                TvMailNewDialog.getInstance(getApplicationContext()).showAlertDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class TvMailServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String intentAction = intent.getAction();
                Log.d(TAG, "TvMailServiceReceiver intentAction: " + intentAction);
                if (intentAction.equals("ResetTvMailTimer")) {
                    setTvMailTimer();
                }
            } catch (Exception e) {
                Log.d(TAG, "TvMailServiceReceiver Exception: " + e.getMessage());
            }
        }
    }

}
