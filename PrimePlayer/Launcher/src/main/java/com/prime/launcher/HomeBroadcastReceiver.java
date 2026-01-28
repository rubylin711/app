package com.prime.launcher;

import static com.prime.launcher.HomeActivity.TYPE_HANDLE_ACS_DATA;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.launcher.ACSDatabase.ACSHelper;
import com.prime.datastructure.utils.LogUtils;

import java.lang.ref.WeakReference;

public class HomeBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "HomeBroadcastReceiver";

    private Handler g_HandlerThreadHandler = null,g_MainThreadHandler = null;
    public static final String DO_USER_OTA_CHECK = "com.prime.acsclient.update.do_user_ota_check";
    WeakReference<AppCompatActivity> g_ref;
    public HomeBroadcastReceiver(Handler handlerThreadHandler) {
        g_HandlerThreadHandler = handlerThreadHandler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (null == action)
            return;

        //intent.getBooleanExtra(HomeBackgroundService.BROADCAST_FROM_HOME_BACKGROUND_SERVICE,false);
        Log.d(TAG, "onReceive: [action] " + action);

        if(action.equals(DO_USER_OTA_CHECK)) {
            ACSHelper.do_acs_command(ACSHelper.MSG_OTA_UPDATE,0,0,0);
        }
        else {
            if(g_HandlerThreadHandler !=null) {
                Message msg = g_HandlerThreadHandler.obtainMessage();
                msg.what = TYPE_HANDLE_ACS_DATA;
                msg.obj = intent;
                g_HandlerThreadHandler.sendMessage(msg);
            }
        }
    }

    public static IntentFilter getHomeBroadcastFilter() {
        IntentFilter intentFilter = ACSHelper.getIntentFilter();
        intentFilter.addAction(DO_USER_OTA_CHECK);
        return intentFilter;
    }

    public static IntentFilter getHomeBroadcastFilterForHomeBackgroundService() {
        IntentFilter intentFilter = ACSHelper.getIntentFilter();
        return intentFilter;
    }
}
