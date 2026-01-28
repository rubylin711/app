package com.prime.dmg.launcher.Utils.System;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.prime.dmg.launcher.ACSDatabase.ACSHelper;
import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.ActivityUtils;
import com.prime.dtv.utils.TVMessage;


public class IPAlertActivity extends BaseActivity {
    String TAG = getClass().getSimpleName();
    private static final String CATEGORY_LEANBACK_SETTINGS = "android.intent.category.LEANBACK_SETTINGS";

    private int mTaskId;
    private TextView mMessageText;
    private Handler mHandler = new Handler();
    // java.lang.Runnable
    private Runnable mReorderFrontRunnable = () -> {
        if (ActivityUtils.is_process_on_top(IPAlertActivity.this) || IPAlertActivity.this.mTaskId == -1) {
            return;
        }
        IPAlertActivity.showIpIllegalError(IPAlertActivity.this);
    };

    private BroadcastReceiver mIpReceiver = new BroadcastReceiver() { // from class: com.vasott.tbc.hybrid.IpAlertActivity.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"alert activity receiver broadcase intent = "+intent.getAction());
            if (intent == null || intent.getAction() == null) {
                return;
            }
            if (ACSHelper.get_IpWhiteListStatus(context)) {
                IPAlertActivity.this.finish();
            } else if (ACSHelper.isIllegalNetwork(context)) {
            } else {
                IPAlertActivity.this.finish();
            }
        }
    };

    public static boolean showIpIllegalError(Context context) {
        if (ACSHelper.get_IpWhiteListStatus(context) || !ACSHelper.isIllegalNetwork(context)) {
            return false;
        }
        Intent intent = new Intent(context, IPAlertActivity.class);
        context.startActivity(intent);
        return true;
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_alert);
        this.mTaskId = getTaskId();
        this.mMessageText = (TextView) findViewById(R.id.lo_alert_msg_text);
        String charSequence = getText(R.string.error_e508).toString();
        if (ACSHelper.get_ProjectId(this) == ACSHelper.PROJECT_DMG) {
            charSequence = charSequence.replace(getText(R.string.project_name_tbc).toString(), getText(R.string.project_name_dmg).toString());
        }
        this.mMessageText.setText(charSequence);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACSHelper.ILLEGAL_NETWORK);
        intentFilter.addAction(ACSHelper.IP_WHITELIST_STATUS);
        registerReceiver(this.mIpReceiver, intentFilter, RECEIVER_EXPORTED);
        Log.d(TAG,"alert activity onCreate this.mTaskId = "+this.mTaskId+" !!!!");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mHandler.removeCallbacks(this.mReorderFrontRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        this.mHandler.postDelayed(this.mReorderFrontRunnable, 200L);
    }

    @Override
    public void onDestroy() {
        this.mTaskId = -1;
        this.mHandler.removeCallbacks(this.mReorderFrontRunnable);
        unregisterReceiver(this.mIpReceiver);
        Log.d(TAG,"alert activity ondestroy !!!!");
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == 0 && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openWifiSetting();
//            finish();
            return true;
        }
        return true;
//        return super.onKeyDown(keyCode, event);
    }

    private void openWifiSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_WIFI_SETTINGS);
        startActivity(intent);
    }

//    private void openWifiSetting() {
//        PackageManager packageManager = getPackageManager();
//        List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(new Intent("android.settings.WIFI_SETTINGS").addCategory(CATEGORY_LEANBACK_SETTINGS), 0);
//        for (ResolveInfo resolveInfo : packageManager.queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory(CATEGORY_LEANBACK_SETTINGS), 64)) {
//            if (resolveInfo.activityInfo != null) {
//                Iterator<ResolveInfo> it = queryIntentActivities.iterator();
//                while (true) {
//                    if (it.hasNext()) {
//                        ResolveInfo next = it.next();
//                        if (next.activityInfo != null && TextUtils.equals(next.activityInfo.name, resolveInfo.activityInfo.name) && TextUtils.equals(next.activityInfo.packageName, resolveInfo.activityInfo.packageName)) {
//                            Intent makeMainActivity = Intent.makeMainActivity(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
////                            makeMainActivity.addFlags(268468224);
//                            startActivity(makeMainActivity);
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//    }

}
