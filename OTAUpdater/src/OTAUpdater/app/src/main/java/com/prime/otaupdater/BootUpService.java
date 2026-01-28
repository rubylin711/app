package com.prime.otaupdater;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class BootUpService extends Service {
    public static final String TAG = "BootUpService" ;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // logcat | grep -E 'MainActivity|MountReceiver|MainService'
        Log.d(TAG, "onCreate: register Mount Receiver");
        /*MountReceiver receiver = new MountReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.os.storage.action.VOLUME_STATE_CHANGED");
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        registerReceiver(receiver, intentFilter);*/
    }
}
