package com.prime.aosp.media.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.R)
public class LauncherReceiver extends BroadcastReceiver {

    String TAG = LauncherReceiver.class.getSimpleName();

    WeakReference<Launcher> m_ref;

    public LauncherReceiver(Launcher launcher) {
        m_ref = new WeakReference<>(launcher);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case Launcher.ACTION_MEDIA_PLAY:
                mediaPlay(intent);
                break;
            case Launcher.ACTION_MEDIA_STOP:
                mediaStop();
                break;
        }
    }

    public void mediaPlay(Intent intent) {
        Log.d(TAG, "ACTION_MEDIA_PLAY");
        List<String> playList = new ArrayList<>();
        String[] playArray = null;

        // get play list
        playArray = intent.getStringArrayExtra(Launcher.EXTRA_PLAY_LIST);
        if (playArray != null && playArray.length != 0)
            playList = Arrays.asList(playArray);

        // media play
        Launcher launcher = m_ref.get();
        launcher.m_playListIndex = 0;
        launcher.playerPlay(playList);
    }

    public void mediaStop() {
        // media stop
        Log.d(TAG, "ACTION_MEDIA_STOP");
        Launcher launcher = m_ref.get();
        launcher.playerStop();
    }
}
