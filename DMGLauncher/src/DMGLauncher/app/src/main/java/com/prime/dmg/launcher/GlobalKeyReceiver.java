package com.prime.dmg.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.prime.android.SystemAPP.PrimeSystemApp;
import com.prime.dmg.launcher.ACSDatabase.ACSHelper;

public class GlobalKeyReceiver extends BroadcastReceiver {
    public final String TAG = getClass().getSimpleName();
    private static final String ACTION_GLOBAL_BUTTON = "com.prime.dmg.launcher.GLOBAL_BUTTON";

    public interface Callback {
        void on_press_global_key(int keyCode);
    }
    private static Callback g_Callback;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (action == null || extras == null)
            return;

        Log.d(TAG,"onReceive: global key action = " + action);
        if (ACTION_GLOBAL_BUTTON.equals(action)) {
            KeyEvent keyEvent = (KeyEvent) extras.get("android.intent.extra.KEY_EVENT");

            if (keyEvent == null || keyEvent.getAction() != KeyEvent.ACTION_UP)
                return;

            int keyCode = keyEvent.getKeyCode();
            Log.d(TAG, "onReceive: global key code = " + keyCode);
            BaseActivity.set_global_key(keyCode);
            if (g_Callback != null)
                g_Callback.on_press_global_key(keyCode);

            if (pa_lock_check(context))
                return;
            if (update_network_check(context))
                return;
            if (force_screen_check(context))
                return;
        }

        if (action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            HomeBackgroundService.startService(context);
        }
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            HomeBackgroundService.startService(context);
        }
    }

    public static void register_callback(Callback callback) {
        g_Callback = callback;
    }

    public static boolean pa_lock_check(Context context) {
        if (ACSHelper.get_PaLock(context) == 1) {
            ACSHelper.do_acs_command(ACSHelper.MSG_PA_LOCK, null, 0, 0);
            return true;
        }
//        else {
//            context.sendBroadcast(new Intent(ForcePaHintActivity.ACTION_CLOSE_FORCE_PA));
//            return false;
//        }
        return false;
    }

    public static boolean update_network_check(Context context) {
        if (ACSHelper.get_IpWhiteListStatus(context) || !ACSHelper.isIllegalNetwork(context))
            return false;
        ACSHelper.do_acs_command(ACSHelper.MSG_UPDATE_NETWORK_CHECK, null, 0, 0);
        return true;
    }

    public static boolean force_screen_check(Context context) {
        if (ACSHelper.get_ForceScreenStatus(context) && ACSHelper.get_force_screen_param(context).is_exist()) {
            ACSHelper.do_acs_command(ACSHelper.MSG_FORCE_SCREEN, null, 0, 0);
            return true;
        }
        return false;
    }
}
