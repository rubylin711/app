package com.prime.dtvplayer.Activity;

/**
 * Created by jim_huang on 2018/7/3.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.TvInput.PesiTvInputService;
import com.prime.dtvplayer.R;

import static android.content.pm.PackageManager.FEATURE_LEANBACK;

public class PesiBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = "PesiBroadcastReceiver" ;
    private static final String START_DTVPLATER_BROADCAST_ACTION = "pesi.HomeKeyDectect.broadcast.action.startDTVPlayer";
    private String packageName = null;
    private static final String internalBroadcast = "pesi.broadcast.action.waitSetupComplete" ; // jim 2018/07/18 add for  broadcast 10 sec ANR rules
    private static boolean mHasStartAPK = false;    // Johnny 20190416 add for first boot after burn

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "onReceive: LIVE_TV_MODE = "+PesiTvInputService.LIVE_TV_MODE);
        Log.d(TAG, intent.getAction());
        switch (intent.getAction())
        {
            case Intent.ACTION_LOCKED_BOOT_COMPLETED:
                packageName = context.getPackageName();
//                bootCompleteStartAPK(context); // jim 2018/07/18 add for  broadcast 10 sec ANR rules
                break;
            case internalBroadcast: // jim 2018/07/18 add for  broadcast 10 sec ANR rules
            case Intent.ACTION_BOOT_COMPLETED:  // Johnny 20190416 add for first boot after burn
                if (!mHasStartAPK) {
                    packageName = context.getPackageName();
//                    bootCompleteStartAPK(context); // jim 2018/07/18 add for  broadcast 10 sec ANR rules
                }

                //setMaxVolume(context);
                break;
            case START_DTVPLATER_BROADCAST_ACTION:
                boolean dtvplayer_on_top = intent.getBooleanExtra("dtvplayer_on_top",true) ;
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

                Log.d(TAG, "top Activity PackageName = " + dtvplayer_on_top ) ;
                if ( pm.isInteractive() == true && dtvplayer_on_top == false )
                {
                    // Johnny 20181101 comment out
//                    Intent newActivityIntent = new Intent(context, ViewActivity.class);
//                    //Intent newActivityIntent = new Intent(context, MainActivity.class); //LoaderDTV need open
//                    newActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    context.startActivity(newActivityIntent);

                    // Johnny 20181101 get launch intent by AndroidManifest.xml
                    PackageManager packageManager = context.getPackageManager();
                    Intent newActivityIntent = packageManager.getLaunchIntentForPackage("com.prime.dtvplayer");
                    if (newActivityIntent != null) {
                        newActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(newActivityIntent);
                    }
                }
                // jim 2018/09/27 fix home key broadcast timming cause crash when switch third part app and DTVPlayer -s
                else if ( !PesiTvInputService.LIVE_TV_MODE // Edwin 20181102 fix home key cause Live Channel black screen
                        && pm.isInteractive() == true && dtvplayer_on_top == true )
                {
                    Intent intentDTVPlayer = new Intent();
                    intentDTVPlayer.setAction(context.getString(R.string.STR_INTERNAL_BROADCAST_HOMEKEY));
                    intentDTVPlayer.setPackage(context.getPackageName());
                    context.sendBroadcast(intentDTVPlayer);
                }
                // jim 2018/09/27 fix home key broadcast timming cause crash when switch third part app and DTVPlayer  -e
                break;
        }
    }

    // jim 2018/07/18 add determine is user first setup complete -s
    public static final String USER_SETUP_COMPLETE = "user_setup_complete";
    public static final String TV_USER_SETUP_COMPLETE = "tv_user_setup_complete";
    boolean isUserSetupComplete(Context context) {
        boolean isSetupComplete = Settings.Secure.getInt(context.getContentResolver(),USER_SETUP_COMPLETE, 0) != 0;
        if (context.getPackageManager().hasSystemFeature(FEATURE_LEANBACK)) {
            isSetupComplete &= isTvUserSetupComplete(context);
        }
        return isSetupComplete;
    }
    private boolean isTvUserSetupComplete(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), TV_USER_SETUP_COMPLETE, 0) != 0;
    }
    // jim 2018/07/18 add determine is user first setup complete -e

    // jim 2018/07/18 add for  broadcast 10 sec ANR rules -s
    private void bootCompleteStartAPK(final Context context )
    {
//        if ( isUserSetupComplete(context) == false ) //  5 sec send broadcast to myself to wait user setup complete
//        {
//            new CountDownTimer(1000*5,1000)
//            {
//                @Override
//                public void onFinish() {
//                    Intent intentDTVPlayer = new Intent();
//                    intentDTVPlayer.setAction(internalBroadcast);
//                    intentDTVPlayer.setPackage(packageName);
//                    Log.d( TAG, "packageName " + packageName ) ;
//                    context.sendBroadcast(intentDTVPlayer);
//                }
//
//                @Override
//                public void onTick(long millisUntilFinished) {
//                    Log.d( TAG, "countdown " + millisUntilFinished ) ;
//                }
//            }.start();
//        }
//        else
        {
            // Johnny 20181101 comment out
//            Intent newActivityIntent = new Intent(context, ViewActivity.class);
//            //Intent newActivityIntent = new Intent(context, MainActivity.class); //LoaderDTV need open
//            newActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(newActivityIntent);

            // Johnny 20181101 get launch intent by AndroidManifest.xml
            PackageManager packageManager = context.getPackageManager();
            Intent newActivityIntent = packageManager.getLaunchIntentForPackage("com.prime.dtvplayer");
            if (newActivityIntent != null) {
                newActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                newActivityIntent.putExtra("bootComplete",1);
                context.startActivity(newActivityIntent);
                mHasStartAPK = true;
            }
        }


    }
    // jim 2018/07/18 add for  broadcast 10 sec ANR rules -e

    private void setMaxVolume(Context context) {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_SHOW_UI);
        }
    }
}

