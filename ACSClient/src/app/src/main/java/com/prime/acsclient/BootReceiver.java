package com.prime.acsclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prime.acsclient.common.CommonDefine;

public class BootReceiver extends BroadcastReceiver {
    private final String TAG = "ACS_BootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "onReceive : " + action );
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            Log.i(TAG, "Start ACSService");
            Intent serviceIntent = new Intent();
            serviceIntent.setClass(context, ACSService.class);
            serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            serviceIntent.putExtra(CommonDefine.IS_BOOT_COMPLETE, true);
            context.startService(serviceIntent);
        }
    }
}