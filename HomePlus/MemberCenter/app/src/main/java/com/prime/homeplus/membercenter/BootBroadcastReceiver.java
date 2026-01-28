package com.prime.homeplus.membercenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


import com.prime.homeplus.membercenter.TvMail.TvMailService;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "HomePlus-MemberCenter";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i(TAG, "onReceive()::Intent.ACTION_BOOT_COMPLETED");
            Intent intent2 = new Intent(context, TvMailService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent2);
            } else {
                context.startService(intent2);
            }
        }
    }
}
