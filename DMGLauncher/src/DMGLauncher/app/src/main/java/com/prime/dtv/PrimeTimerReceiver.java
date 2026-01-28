package com.prime.dtv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prime.dmg.launcher.HomeApplication;

public class PrimeTimerReceiver extends BroadcastReceiver {
    private static final String TAG = "PrimeTimerReceiver";
    public static final String ACTION_TIMER_RECORD          = "com.prime.dmg.launcher.timer_record";
    public static final String ACTION_TIMER_POWER_ON        = "com.prime.dmg.launcher.timer_power_on";
    public static final String ACTION_TIMER_CHANGE_CHANNEL  = "com.prime.dmg.launcher.timer_change_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null) {
            Log.d(TAG, "onReceive: " + action);
            HomeApplication.onMessage(context, intent);
        }
    }
}
