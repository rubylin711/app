package com.prime.aosp.media.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

@RequiresApi(api = Build.VERSION_CODES.R)
public class BackgroundReceiver extends BroadcastReceiver {
    
    String TAG = BackgroundReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //Log.d(TAG, "action = " + action);
        switch (action) {
            //case Launcher.ACTION_PLAYER_INIT:
            case Intent.ACTION_MEDIA_MOUNTED:
                Log.d(TAG, "ACTION_MEDIA_MOUNTED");
                LocalBroadcastManager.getInstance(context).sendBroadcast(getPlayIntent(context));
                break;
            case Intent.ACTION_MEDIA_UNMOUNTED:
                Log.d(TAG, "ACTION_MEDIA_UNMOUNTED");
                LocalBroadcastManager.getInstance(context).sendBroadcast(getStopIntent());
                break;
            /*case Intent.ACTION_MEDIA_EJECT:
                Log.d(TAG, "ACTION_MEDIA_EJECT");
                LocalBroadcastManager.getInstance(context).sendBroadcast(getStopIntent());
                break;*/
        }
    }

    private Intent getPlayIntent(Context context) {
        Log.d(TAG, "getPlayIntent: ");
        MediaHelper mediaHelper = null;
        String[] playArray = null;

        // init play list
        mediaHelper = new MediaHelper(context);
        playArray = mediaHelper.getPlayArray();

        // init intent
        Intent playIntent = new Intent(Launcher.ACTION_MEDIA_PLAY);
        playIntent.putExtra(Launcher.EXTRA_PLAY_LIST, playArray);
        return playIntent;
    }

    private Intent getStopIntent() {
        Log.d(TAG, "getStopIntent: ");
        Intent stopIntent = new Intent(Launcher.ACTION_MEDIA_STOP);
        return stopIntent;
    }
}