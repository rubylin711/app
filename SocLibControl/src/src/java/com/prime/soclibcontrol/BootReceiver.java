package com.prime.soclibcontrol;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "PrimeBroadcastReceiver" ;
    private boolean mInit = false;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, intent.getAction());
        switch (intent.getAction())
        {
            case Intent.ACTION_BOOT_COMPLETED:
            {
                
                Log.d(TAG, "onReceive: ACTION_BOOT_COMPLETED");
                Intent i = new Intent(Intent.ACTION_RUN);
                i.setClass(context, MonitorService.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(i);
            }break;
        }
    }
}
