package com.prime.launcher.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.prime.launcher.HomeApplication;
import com.prime.datastructure.utils.LogUtils;

public class PrimeVolumeReceiver extends BroadcastReceiver {
    private static final String TAG = "PrimeVolumeReceiver";

    public static final String EXTRA_VOLUME_STREAM_TYPE     = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    public static final String ACTION_VOLUME_CHANGED        = "android.media.VOLUME_CHANGED_ACTION";
    public static final String ACTION_STREAM_MUTE_CHANGED   = "android.media.STREAM_MUTE_CHANGED_ACTION";
    public static final String ACTION_KEYCODE_VOLUME_DOWN   = "com.prime.launcher.KEYCODE_VOLUME_DOWN";
    public static final String ACTION_KEYCODE_VOLUME_MUTE   = "com.prime.launcher.KEYCODE_VOLUME_MUTE";

    public static final String KEY_VOLUME = "VOLUME";

    public static int g_volume = -1;

    public PrimeVolumeReceiver(Context context) {
        setVolumeDefaultParam(context, 21, 15);
        init_volume(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (null == action)
            return;

        if (action.endsWith(ACTION_KEYCODE_VOLUME_DOWN) || action.endsWith(ACTION_KEYCODE_VOLUME_MUTE)) {
            Log.d(TAG, "onReceive: " + action);
            //Log.d(TAG, "onReceive: g_volume = " + g_volume);
            if (g_volume == 0)
                HomeApplication.onMessage(context, intent);
            return;
        }

        int streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
        //boolean state = intent.getBooleanExtra("android.media.EXTRA_STREAM_VOLUME_MUTED", false);
        if (streamType != AudioManager.STREAM_MUSIC)
            return;
        Log.d(TAG, "onReceive: " + action);
        //Log.d(TAG, "onReceive: state: " + state);

        if (action.endsWith(ACTION_VOLUME_CHANGED) || action.endsWith(ACTION_STREAM_MUTE_CHANGED)) {
            setTunerAudioTrackVolume(context);
            HomeApplication.onMessage(context, intent);
        }

        if (action.endsWith(ACTION_VOLUME_CHANGED)) {
            g_volume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0);
        }
    }

    public void setTunerAudioTrackVolume(Context context) {
        AudioManager audioManager = context.getSystemService(AudioManager.class);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setParameters("tunerAudioTrackVolume=" + volume);
        Log.d(TAG,"STREAM_MUSIC volume = " + volume);

        new Thread(() -> {
            SharedPreferences preferences = context.getSharedPreferences(HomeApplication.class.getSimpleName(), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor;
            editor = preferences.edit();
            editor.putInt(KEY_VOLUME, volume);
            editor.apply();
        },"setTunerAudioTrackVolume").start();

    }

    public static void init_volume(Context context) {
        LogUtils.d(" ");
        new Thread(() -> {
            SharedPreferences preferences = context.getSharedPreferences(HomeApplication.class.getSimpleName(), Context.MODE_PRIVATE);
            int volume = preferences.getInt(KEY_VOLUME, 15);
            LogUtils.d( "init_volume: volume = " + volume);

            AudioManager audioManager = context.getSystemService(AudioManager.class);
            boolean isMuted = audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
            LogUtils.d( "init_volume: isMuted = " + isMuted);
            if (isMuted)
                set_mute(context, isMuted);
            else
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            audioManager.setParameters("tunerAudioTrackVolume=" + volume);
        },"init_volume").start();
    }

    public static void setVolumeDefaultParam(Context context, int max_index, int steps){
        AudioManager audioManager = context.getSystemService(AudioManager.class);
        LogUtils.d( "setVolumeDefaultParam max_index = "+max_index+" steps = "+steps);
        audioManager.setParameters("tunerAudioTrackVolumeMax=" + max_index);
        audioManager.setParameters("tunerAudioTrackVolumeLevel=" + steps);
    }

    public static void set_mute(Context context, boolean isMuted) {
        AudioManager audioManager = context.getSystemService(AudioManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    isMuted ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE,
                    0);
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, isMuted);
        }
    }
}
