package com.prime.homeplus.vbm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "HOMEPLUS_VBM";
    private static boolean sStarted = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        if (intent == null) return;

        String action = intent.getAction();
        Log.d(TAG, "onReceive action=" + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            if (sStarted) {
                Log.d(TAG, "Boot already handled, ignore.");
                return;
            }
            Log.d(TAG, "ACTION_BOOT_COMPLETED");
            sStarted = true;

            Intent serviceIntent = new Intent(context, MainService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
