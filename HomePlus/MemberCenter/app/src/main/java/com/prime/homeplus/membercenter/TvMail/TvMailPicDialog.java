package com.prime.homeplus.membercenter.TvMail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.membercenter.R;
import com.prime.homeplus.membercenter.enity.Data;
import com.prime.homeplus.membercenter.enity.RequestData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.WINDOW_SERVICE;

public class TvMailPicDialog {

    public static TvMailPicDialog INSTANCE;
    private final String TAG = TvMailPicDialog.class.getSimpleName();
    private TvMailDbHelper tvMailDbHelper;
    private Context context;
    private View relayout;
    private Button btnOK, btnCancel;
    private ImageView ivContent;
    private TextView tvTitle;
    private Data data;
    private TvMailPicDialogReceiver mTvMailPicDialogReceiver;
    private WindowManager windowManager;

    private static String status_topic_prefix = "api/message/postMessageStatus";

    public static TvMailPicDialog getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TvMailPicDialog.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TvMailPicDialog(context);
                }
            }
        }
        return INSTANCE;
    }

    public TvMailPicDialog(Context context) {
        this.context = context.getApplicationContext();
        initView();
    }


    private void initView() {
        windowManager = (WindowManager) context.getApplicationContext().getSystemService(WINDOW_SERVICE);
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

        relayout = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.dialog_tvmail_onlypic, null);
        btnOK = (Button) relayout.findViewById(R.id.btnOK);
        btnCancel = (Button) relayout.findViewById(R.id.btnCancel);
        ivContent = (ImageView) relayout.findViewById(R.id.ivContent);
        tvTitle = (TextView) relayout.findViewById(R.id.tvTitle);

        if (tvMailDbHelper == null) {
            tvMailDbHelper = new TvMailDbHelper(this.context);
        }

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openIntent();
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


    public void showTvMailPicDialog(Data tvmailData, boolean showTitle) {
        data = tvmailData;
        if (relayout == null || data == null) {
            return;
        }

        if (relayout.getVisibility() == View.GONE) {
            relayout.setVisibility(View.VISIBLE);
        }

        if (data.getContent().getImageUrl() != null && !data.getContent().getImageUrl().equals("")) {
            Log.d(TAG, "getImageUrl: " + data.getContent().getImageUrl());
            Glide.with(context).load(data.getContent().getImageUrl()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivContent);
            ivContent.setVisibility(View.VISIBLE);
            Log.d(TAG, "ivContent: set VISIBLE");
        }


        String strButtonOk;
        if (data.getContent().getIntent().getContentUri().equals("")
                && data.getContent().getIntent().getPackageName().equals("")) {

            strButtonOk = context.getResources().getString(R.string.tvmail_close);
            btnOK.setText(strButtonOk);
            btnCancel.setVisibility(View.GONE);
        } else {
            strButtonOk = context.getResources().getString(R.string.tvmail_open);
            btnOK.setText(strButtonOk);
            btnCancel.setVisibility(View.VISIBLE);
        }

        mTvMailPicDialogReceiver = new TvMailPicDialogReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        intentFilter.addAction("closeDialog");
        context.getApplicationContext().registerReceiver(mTvMailPicDialogReceiver, intentFilter);

        if (showTitle) {
            tvTitle.setText(data.getTitle());
        } else {
            tvTitle.setText(context.getResources().getString(R.string.msg_notice));
        }

        btnOK.requestFocus();
        postMessageStatus(1, 0);
    }

    private void postMessageStatus(int isRead, int isClick) {
        tvMailDbHelper.updateIsRead(String.valueOf(data.getLauMessagePublishId()));
        String tvmailDomain = GposInfo.getTvMailUrlPrefix(context);//Settings.System.getString(context.getContentResolver(),TvMailService.PROP_TVMAIL_URL_PREFIX);
        String token = GposInfo.getLauncherToken(context);//Settings.System.getString(context.getContentResolver(), "cns_launcher_token");

        if (TextUtils.isEmpty(token)) {
            return;
        }

        if (TextUtils.isEmpty(tvmailDomain) || tvmailDomain.equals("")) {
            GposInfo.setTvMailUrlPrefix(context, TvMailService.DEFAULT_TVMAIL_URL);//Settings.System.putString(context.getContentResolver(),TvMailService.PROP_TVMAIL_URL_PREFIX,TvMailService.DEFAULT_TVMAIL_URL);
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
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"), requestData.getRequestBody());

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


    private void openIntent() {
        com.prime.homeplus.membercenter.enity.Intent dataIntent = data.getContent().getIntent();

        if (dataIntent.getContentUri().equals("")
                && dataIntent.getPackageName().equals("")) {
            Log.d(TAG, "intent data is null, close TvMail Dialog.");
            closeDialog();
            return;
        }

        showReminderDialog();
    }

    private void checkKeyDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            closeDialog();
        }
    }

    private void closeDialog() {
        Log.d(TAG, "CloseDialog.");
        if (mTvMailPicDialogReceiver != null) {
            context.getApplicationContext().unregisterReceiver(mTvMailPicDialogReceiver);
            mTvMailPicDialogReceiver = null;
        }
        windowManager.removeView(relayout);
    }

    private void showReminderDialog() {
        try {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                Handler mainThread = new Handler(Looper.getMainLooper());
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        ReminderDialog reminderDialog = new ReminderDialog(context);
                        reminderDialog.showReminderDialog(data);
                        reminderDialog = null;
                    }
                });
            } else {
                ReminderDialog reminderDialog = new ReminderDialog(context);
                reminderDialog.showReminderDialog(data);
                reminderDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public class TvMailPicDialogReceiver extends BroadcastReceiver {
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_GOOGLE_ASSISTANT = "assist";

        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(intentAction)) {
                closeDialog();
            } else if (intent.getAction().equals("closeDialog")) {
                closeDialog();
            }else {
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
