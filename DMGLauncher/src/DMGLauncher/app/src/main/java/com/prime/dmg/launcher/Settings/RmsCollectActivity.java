package com.prime.dmg.launcher.Settings;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
//import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.R;
import com.prime.dtv.utils.TVMessage;

public class RmsCollectActivity extends BaseActivity {
    private static final String TAG = "RmsCollectActivity";
    private static final long ONEDAY_SECOND = 86400;
    public static final long PHASE1_INTERVAL_DAY = 1;
    public static final long PHASE2_INTERVAL_DAY = 90;
    public static final String PROPERTY_RMSCLIENT_TRIGGET_TIME = "persist.vendor.rtk.rms_triggertime";
    private final int RESPOND_AGREE = 9696;
    private final int RESPOND_DISAGREE = 881;
    private Button g_cancel_button;
    private ImageView g_checkbox_view;
    private Button g_confirm_button;
    private LinearLayout g_item_layer;
    private TextView g_msg_text;
    private boolean g_rms_agree_state = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rms);
        update_rms_collect_time();
        //SharedPrefHelper.setShowRmsHint(this, true);
        init();
    }

    private void init() {
        this.g_checkbox_view = (ImageView) findViewById(R.id.lo_rms_imgv_checkbox_view);
        this.g_msg_text = (TextView) findViewById(R.id.lo_rms_textv_msg_text);
        this.g_item_layer = (LinearLayout) findViewById(R.id.lo_rms_lnrl_item_layer);
        this.g_confirm_button = (Button) findViewById(R.id.lo_rms_btn_confirm_button);
        this.g_cancel_button = (Button) findViewById(R.id.lo_rms_btn_cancel_button);
//        this.mRmsAgreeState = SharedPrefHelper.isNeedShowRmsHintOnBoot(this);
        this.g_msg_text.setText(R.string.settings_rms_hint_title_first);
        this.g_checkbox_view.setBackgroundResource(this.g_rms_agree_state ? R.mipmap.icon_checkbox_on : R.mipmap.icon_checkbox_off);
        LinearLayout linearLayout = this.g_item_layer;
        int i = View.VISIBLE;
        Log.d(TAG, "init: showCheckBox = " + getIntent().getBooleanExtra("showCheckBox", false));
        if (!getIntent().getBooleanExtra("showCheckBox", false)) {
            i = View.GONE;
        }
        linearLayout.setVisibility(i);
        this.g_item_layer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RmsCollectActivity rmsCollectActivity = RmsCollectActivity.this;
                //SharedPrefHelper.setNeedShowRmsHintOnBoot(rmsCollectActivity, !rmsCollectActivity.mRmsAgreeState);
                //RmsCollectActivity rmsCollectActivity2 = RmsCollectActivity.this;
                //rmsCollectActivity2.mRmsAgreeState = SharedPrefHelper.isNeedShowRmsHintOnBoot(rmsCollectActivity2);
                RmsCollectActivity.this.g_checkbox_view.setBackgroundResource(
                        RmsCollectActivity.this.g_rms_agree_state ? R.mipmap.icon_checkbox_off : R.mipmap.icon_checkbox_on);
                RmsCollectActivity.this.g_rms_agree_state = RmsCollectActivity.this.g_rms_agree_state ? false : true;
            }
        });
        this.g_confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RmsCollectActivity.this.agree_rms();
                RmsCollectActivity.this.finish();
            }
        });
        this.g_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RmsCollectActivity.this.cancel_rms();
                RmsCollectActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        cancel_rms();
        super.onBackPressed();
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    public void agree_rms() {
        set_rms_client(this, 2);
        setResult(RESPOND_AGREE);
    }

    public void cancel_rms() {
//        AcsHelper.setProviderData(this, "rms_client", "1");
        set_rms_client(this, 1);
        setResult(RESPOND_DISAGREE);
    }

    private void set_rms_client(Context context, int value) {
//        String valueOf = String.valueOf(value);
//        Intent intent = new Intent(AcsHelper.INTENT_RMS_COLLECT);
//        intent.putExtra("rmsstatus", valueOf);
//        intent.setPackage("com.vasott.acsclient");
//        if (Build.VERSION.SDK_INT < 19) {
//            sendBroadcast(intent);
//        } else {
//            context.sendBroadcastAsUser(intent, Process.myUserHandle());
//        }
    }

    private void update_rms_collect_time() {
        //SystemProperties.set(PROPERTY_RMSCLIENT_TRIGGET_TIME, Long.toString(System.currentTimeMillis() / 1000));
    }

    /*public void open_rms_collect_activity(Context context) {
        String providerData = AcsHelper.getProviderData(context, "rms_client");
        String providerData2 = AcsHelper.getProviderData(context, "rms_status");
        VasLog.m24i("rmsClient:" + providerData + ",rmsServer:" + providerData2);
        if (providerData2.contentEquals("1")) {
            return;
        }
        if (providerData2.contentEquals("0") && providerData.contentEquals("0")) {
            return;
        }
        if (providerData.contentEquals(ExifInterface.GPS_MEASUREMENT_2D) && checkRmsCollectTime(1L)) {
            VasLog.m24i("user does not respond sms over 24 hours, disable rms...");
            AcsHelper.setProviderData(context, "rms_client", "1");
            setRMSClient(context, 1);
        } else if (!SharedPrefHelper.isNeedShowRmsHintOnBoot(context)) {
        } else {
            if (!PvrErrorCodeHandler.isTbcLauncherOnTop()) {
                VasLog.m24i("do not show on other apps");
            } else if (PvrErrorCodeHandler.getClsNameOnTop().equals("com.vasott.tbc.hybrid.RmsCollectActivity")) {
                VasLog.m24i("already show rms activity!");
            } else {
                if (SharedPrefHelper.haveShowRmsHint(context)) {
                    if (checkRmsCollectTime(90L)) {
                        VasLog.m24i("user does not respond sms, show rms view again!");
                    } else {
                        VasLog.m24i("interval time is not enough of 90 days");
                        return;
                    }
                }
                VasLog.m24i("prepare show...");
                Intent intent = new Intent(context, RmsCollectActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("showCheckBox", true);
                intent.addFlags(RtkMediaPlayer.FATALERR_AUDIO);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        }
    }*/

    /*public boolean check_rms_collect_time(long day) {
        long parseLong = Long.parseLong(SystemProperties.get(PROPERTY_RMSCLIENT_TRIGGET_TIME, "0"));
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        boolean z = false;
        try {
            VasLog.m24i("currentTime:" + currentTimeMillis + ",triggerTime:" + parseLong);
            if (currentTimeMillis - parseLong > day * ONEDAY_SECOND) {
                z = true;
            }
        } catch (NumberFormatException unused) {
            VasLog.m26e("Parse long fail");
        }
        VasLog.m24i(">>> checkRmsCollectTime, state:" + z);
        return z;
    }*/
}
