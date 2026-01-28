package com.prime.app.control.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class OtaReceiver extends BroadcastReceiver {

    public final String TAG = getClass().getSimpleName();

    // broadcast from OTA Service
    public static final String ABUPDATE_BROADCAST_ERROR             = "com.prime.android.tv.otaservice.abupdate.error";//send error code and error msg
    public static final String ABUPDATE_BROADCAST_STATUS            = "com.prime.android.tv.otaservice.abupdate.status";//send error code and error msg
    public static final String ABUPDATE_BROADCAST_COMPLETE          = "com.prime.android.tv.otaservice.abupdate.complete";//send update complete
    public static final String ABUPDATE_BROADCAST_CALLER            = "com.prime.android.tv.otaservice.abupdate.caller";

    // param of ABUPDATE_BROADCAST_CALLER
    public static final String ABUPDATE_BROADCAST_UPDATE_CALLER     = "prime.abupdate.caller";

    // param of ABUPDATE_BROADCAST_ERROR
    public static final String ABUPDATE_BROADCAST_ERROR_CODE        = "prime.abupdate.errCode";
    public static final String ABUPDATE_BROADCAST_ERROR_MSG         = "prime.abupdate.errStr";

    // param of ABUPDATE_BROADCAST_STATUS
    public static final String ABUPDATE_BROADCAST_UPDATE_PROGRESS   = "prime.abupdate.progress";
    public static final String ABUPDATE_BROADCAST_STATUS_MESSAGE    = "prime.abupdate.statusMsg";

    public static boolean OTA_IN_PROGRESS = false;
    int     DEBUG_NORMAL    = AppControlService.DEBUG_NORMAL;
    int     DEBUG_VERBOSE   = AppControlService.DEBUG_VERBOSE;
    int     DEBUG           = AppControlService.DEBUG;
    int     mProgress       = 0;
    Handler mHandler        = null;

    public OtaReceiver(Handler handler) {
        Log.d(TAG, "Create " + TAG);
        mHandler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (DEBUG >= DEBUG_VERBOSE)
            Log.i(TAG, "onReceive: action = " + action);

        if (ABUPDATE_BROADCAST_CALLER.equals(action)) {
            String packageName = intent.getStringExtra(ABUPDATE_BROADCAST_UPDATE_CALLER);
            Log.i(TAG, "onReceive: [OTA service] caller package name = " + packageName);
            // Toast.makeText(context, "[OTA service] caller package name = " + packageName, Toast.LENGTH_LONG).show();
        }
        else
        if (ABUPDATE_BROADCAST_ERROR.equals(action)) {
            int errorCode = intent.getIntExtra(ABUPDATE_BROADCAST_ERROR_CODE, -1);
            String errorMsg = intent.getStringExtra(ABUPDATE_BROADCAST_ERROR_MSG);
            Log.i(TAG, "onReceive: [OTA service] errorCode = " + errorCode + " , errorMsg = " + errorMsg);
            // Toast.makeText(context, "[OTA service] errorCode = " + errorCode + " , errorMsg = " + errorMsg, Toast.LENGTH_LONG).show();
        }
        else
        if (ABUPDATE_BROADCAST_STATUS.equals(action)) {
            OTA_IN_PROGRESS = true;

            int progress = intent.getIntExtra(ABUPDATE_BROADCAST_UPDATE_PROGRESS, 0);
            String status = intent.getStringExtra(ABUPDATE_BROADCAST_STATUS_MESSAGE);

            Log.i(TAG, "onReceive: [OTA service] progress = " + progress + ", status = " + status);

            // if (mProgress != progress)
            //     Toast.makeText(context, "[OTA service] progress = " + progress + ", status = " + status, Toast.LENGTH_SHORT).show();
            mProgress = progress;
        }
        else
        if (ABUPDATE_BROADCAST_COMPLETE.equals(action)) {
            OTA_IN_PROGRESS = true;

            Log.i(TAG, "onReceive: [OTA service] Success (need reboot)");
            Toast.makeText(context, "[OTA service] Success (need reboot)", Toast.LENGTH_LONG).show();
        }
    } // onReceive

    public void sendMessage(int what) {
        Message msg = new Message();
        msg.what    = what;
        mHandler.sendMessage(msg);
    }
}
