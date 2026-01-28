package com.prime.otaupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootUpReceiver extends BroadcastReceiver {
    public final String TAG = getClass().getSimpleName();

    // reboot count
    public static final String REBOOT_TIMES = "Locale.Helper.Selected.REBOOT_TIMES";
    public static int reboot_counter = 0 ;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: action = " + action);
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.d(TAG, "onReceive: ACTION_BOOT_COMPLETED");

            reboot_counter = getPersistData(context) ;
            setPersistData(context, reboot_counter+1);

            // start service
            Intent i = new Intent(context, BootUpService.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(i);
        }
    }

    private static int getPersistData(Context context) {
        int defValue = 0 ;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(REBOOT_TIMES, defValue);
    }

    private static void setPersistData(Context context, int reboot_times) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(REBOOT_TIMES, reboot_times);
        editor.apply();
    }
}
