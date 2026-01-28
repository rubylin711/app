package com.prime.dtv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PrimeHomeReceiver extends BroadcastReceiver {

    String TAG = getClass().getSimpleName();

    static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    public interface Callback {
        void on_press_home();
    } Callback g_callback;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null && action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (reason != null && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY))
                g_callback.on_press_home();
        }
    }

    public void register_callback(Callback callback) {
        g_callback = callback;
    }
}
