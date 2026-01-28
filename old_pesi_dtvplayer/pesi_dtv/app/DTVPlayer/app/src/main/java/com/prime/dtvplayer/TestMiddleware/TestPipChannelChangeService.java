package com.prime.dtvplayer.TestMiddleware;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class TestPipChannelChangeService extends Service {
    private final String TAG = getClass().getSimpleName();
    private Handler change = new Handler();
    public static final String DTV_MAIN_ACTIVITY = "com.prime.dtvplayer.TestMiddleware.TestPipActivity";
    public static final String PERMISSION_DTV_BROADCAST = "android.permission.DTV_BROADCAST";
    public static final String TEST_PIP_ALARM_REMIND = "com.prime.changepip.remind";
    final Runnable ChangePipRunnable = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            //Log.d(TAG, "mmm1234 Runnable => send msg to change channel");
            Intent ChangePipIntent = new Intent(TEST_PIP_ALARM_REMIND);
            sendBroadcast(ChangePipIntent, PERMISSION_DTV_BROADCAST);
            change.postDelayed(ChangePipRunnable, 1500);
        }
    };
    public TestPipChannelChangeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
    @Override
    public void onCreate()
    {
        //Log.d(TAG, "mmm1234 onCreate:AAABCD ");
        super.onCreate();
        change.postDelayed(ChangePipRunnable, 5000);
    }
    @Override
    public void onDestroy() {
        //Log.d(TAG, "mmm1234 onDestroy:AAABCD ");
        if(change!=null)
        {
            change.removeCallbacks(ChangePipRunnable);
            change = null;
        }
    }
}
