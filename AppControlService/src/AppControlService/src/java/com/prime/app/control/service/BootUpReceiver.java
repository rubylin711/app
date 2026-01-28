package com.prime.app.control.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;

public class BootUpReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName() ;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "onReceive: action = " + intent.getAction());
        switch (intent.getAction())
        {
            case Intent.ACTION_BOOT_COMPLETED:
            {
                Intent i = new Intent(Intent.ACTION_RUN);
                i.setClass(context, AppControlService.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(i);
            }break;
        }
    }
}
