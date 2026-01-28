package com.prime.aosp.media.launcher;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.media3.common.Player;

import java.lang.ref.WeakReference;

@RequiresApi(api = Build.VERSION_CODES.R)
public class ExoPlayerCallback implements Player.Listener {

    String TAG = ExoPlayerCallback.class.getSimpleName();

    WeakReference<Launcher> m_ref;
    Launcher m_launcher;

    public ExoPlayerCallback(Launcher launcher) {
        m_ref = new WeakReference<>(launcher);
        m_launcher = m_ref.get();
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        Player.Listener.super.onPlaybackStateChanged(playbackState);
        switch (playbackState) {
            case Player.STATE_IDLE:
                Log.d(TAG, "onPlaybackStateChanged: Player.STATE_IDLE");
                break;
            case Player.STATE_BUFFERING:
                Log.d(TAG, "onPlaybackStateChanged: Player.STATE_BUFFERING");
                break;
            case Player.STATE_READY:
                Log.d(TAG, "onPlaybackStateChanged: Player.STATE_READY");
                break;
            case Player.STATE_ENDED:
                Log.d(TAG, "onPlaybackStateChanged: Player.STATE_ENDED");
                m_launcher.playerPlay();
                break;
        }
    }
}
