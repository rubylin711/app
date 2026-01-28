package com.prime.homeplus.membercenter.TvMail;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.gson.JsonObject;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.membercenter.R;
import com.prime.homeplus.membercenter.Utils;
import com.prime.homeplus.membercenter.enity.Data;
import com.prime.homeplus.membercenter.enity.Extra;
import com.prime.homeplus.membercenter.enity.RequestData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.WINDOW_SERVICE;

public class ReminderDialog {
    public static ReminderDialog INSTANCE;
    private final String TAG = ReminderDialog.class.getSimpleName();
    private Context context;
    private View relayout;
    private Button btnOK, btnCancel;
    private ReminderDialogReceiver mReminderDialogReceiver;
    private Data data;
    private static String status_topic_prefix = "api/message/postMessageStatus";

    public static ReminderDialog getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ReminderDialog.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ReminderDialog(context);
                }
            }
        }
        return INSTANCE;
    }

    public ReminderDialog(Context context) {
        this.context = context.getApplicationContext();
        initView();
    }

    private void initView() {
        WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        relayout = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.dialog_reminder, null);
        btnOK = (Button) relayout.findViewById(R.id.btnOK);
        btnCancel = (Button) relayout.findViewById(R.id.btnCancel);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postMessageStatus(1, 1);
                openIntent();
                sendBroadcastMessage();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeDialog();
            }
        });

        btnOK.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    checkKeyDown(keyCode);
                }
                return false;
            }
        });

        btnCancel.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    checkKeyDown(keyCode);
                }
                return false;
            }
        });

        windowManager.addView(relayout, layoutParams);
    }

    public void showReminderDialog(Data inputData) {

        if (relayout == null) {
            return;
        }

        if (relayout.getVisibility() == View.GONE) {
            relayout.setVisibility(View.VISIBLE);
        }

        data = inputData;

        mReminderDialogReceiver = new ReminderDialogReceiver();
        IntentFilter dialogFilter = new IntentFilter();
        dialogFilter.addAction(Intent.ACTION_SCREEN_OFF);
        dialogFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.getApplicationContext().registerReceiver(mReminderDialogReceiver, dialogFilter);

        btnOK.requestFocus();

    }

    private void openIntent() {
        com.prime.homeplus.membercenter.enity.Intent dataIntent = data.getContent().getIntent();
        if (dataIntent == null || (TextUtils.isEmpty(dataIntent.getContentUri())
                && TextUtils.isEmpty(dataIntent.getPackageName())
                && TextUtils.isEmpty(dataIntent.getAction()))) {
            Log.d(TAG, "intent data is null, close Reminder Dialog.");
            closeDialog();
            return;
        }

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor

        try {
            Intent intent = new Intent();
            if (!TextUtils.isEmpty(dataIntent.getContentUri())) {
                String contentUri = dataIntent.getContentUri().replace("com.ott.launcher/com.ott.browser.MainActivity","com.prime.webbrowser/com.prime.webbrowser.MainActivity");
                intent = Intent.parseUri(contentUri, Intent.URI_INTENT_SCHEME);
//                Log.d(TAG,"contentUri = "+contentUri);
            }
            if (!TextUtils.isEmpty(dataIntent.getAction())) {
                intent.setAction(dataIntent.getAction());
//                Log.d(TAG,"dataIntent.getAction() = "+dataIntent.getAction());
            }
            if (!TextUtils.isEmpty(dataIntent.getPackageName())) {
                intent.setPackage(dataIntent.getPackageName());
//                Log.d(TAG,"dataIntent.getPackageName() = "+dataIntent.getPackageName());
            }

            // 處理 Extras
            List<Extra> extras = dataIntent.getExtras();
            if (extras != null && !extras.isEmpty()) {
                intent.putExtra("PERMIT", true);
                for (Extra extra : extras) {
//                    Log.d(TAG,"extra.getKey() = "+extra.getKey() + " extra.getValue() = "+extra.getValue());
                    if("transferPage".equals(extra.getKey()))
                        intent.putExtra("WEB_URL_KEY", extra.getValue());
                    else
                        intent.putExtra(extra.getKey(), extra.getValue());
                }
            }
//            Log.d(TAG,"WEB_URL_KEY = "+intent.getStringExtra("WEB_URL_KEY"));
//            Log.d(TAG,"PERMIT = "+intent.getBooleanExtra("PERMIT",false));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "openIntent error: " + e.getMessage());
        }

        closeDialog();
    }

    private void postMessageStatus(int isRead, int isClick) {
        String tvmailDomain = GposInfo.getTvMailUrlPrefix(context);//Settings.System.getString(context.getContentResolver(),
                //TvMailService.PROP_TVMAIL_URL_PREFIX);
        String token = GposInfo.getLauncherToken(context);//Settings.System.getString(context.getContentResolver(), "cns_launcher_token");

        if (TextUtils.isEmpty(token)) {
            return;
        }

        if (TextUtils.isEmpty(tvmailDomain) || tvmailDomain.equals("")) {
            GposInfo.setTvMailUrlPrefix(context, TvMailService.DEFAULT_TVMAIL_URL);//Settings.System.putString(context.getContentResolver(), TvMailService.PROP_TVMAIL_URL_PREFIX,
                    //TvMailService.DEFAULT_TVMAIL_URL);
            tvmailDomain = TvMailService.DEFAULT_TVMAIL_URL;
        }

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();

        String command = "0000";
        String timestamp = System.currentTimeMillis() + "";

        JsonObject request_data = new JsonObject();
        request_data.addProperty("token", token);
        request_data.addProperty("lauMessagePublishId", data.getLauMessagePublishId());
        request_data.addProperty("isRead", isRead);
        request_data.addProperty("isClick", isClick);
        request_data.addProperty("scheduleTime", data.getScheduleTime());
        Log.d(TAG, "token: " + token);
        Log.d(TAG, "tvmailDomain: " + tvmailDomain);
        RequestData requestData = new RequestData(command, timestamp, request_data);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"),
                requestData.getRequestBody());

        String requestUrl = "";

        requestUrl = tvmailDomain + status_topic_prefix;

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
                        }
                    } catch (JSONException e) {
                    }
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

    private void checkKeyDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            closeDialog();
        }
    }

    private void closeDialog() {
        Log.d(TAG, "CloseDialog.");
        if (mReminderDialogReceiver != null) {
            context.getApplicationContext().unregisterReceiver(mReminderDialogReceiver);
            mReminderDialogReceiver = null;
        }
        relayout.setVisibility(View.GONE);
    }

    public void sendBroadcastMessage() {
        Intent intent = new Intent();
        intent.setAction("closeDialog");
        context.sendBroadcast(intent);
    }

    public class ReminderDialogReceiver extends BroadcastReceiver {
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_GOOGLE_ASSISTANT = "assist";

        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(intentAction)) {
                closeDialog();
            } else {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                        closeDialog();
                    }
                }
            }
        }
    }

}
