package com.pesi.openqrcode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GlobalKeyOpenQRCodeReceiver extends BroadcastReceiver {
    private static final String TAG = "GlobalKeyOpenQRCodeReceiver";
    private static final String ACTION_GLOBAL_BUTTON = "android.intent.action.GLOBAL_BUTTON";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: action = " + action);
        if (ACTION_GLOBAL_BUTTON.equals(action)) {
            // 創建一個 Intent 物件，指定要啟動的 Activity 類
            Intent intentMain = new Intent(context, MainActivity.class);
            intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // 啟動 Activity
            context.startActivity(intentMain);
        }
    }
}
