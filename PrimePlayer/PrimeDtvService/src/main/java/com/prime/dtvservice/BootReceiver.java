package com.prime.dtvservice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.service.dsmcc.DsmccService;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        final String action = intent.getAction();
        Log.d(TAG, "onReceive action = " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {

            // ---- Start PrimeDtvService (Foreground Service) ----
            Intent serviceIntent = new Intent(context, PrimeDtvService.class);

            // isRunning() 這種狀態在被 kill / package replaced 後不一定可靠
            // 建議直接 start，讓 service 自己 idempotent
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Failed to start PrimeDtvService", t);
            }

           
        }
    }
}
