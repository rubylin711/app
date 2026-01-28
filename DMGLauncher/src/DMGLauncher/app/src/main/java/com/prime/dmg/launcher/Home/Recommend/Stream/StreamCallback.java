package com.prime.dmg.launcher.Home.Recommend.Stream;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;

import java.lang.ref.WeakReference;

public class StreamCallback implements Player.Listener {

    String TAG = getClass().getSimpleName();

    WeakReference<AppCompatActivity> g_ref;
    int g_bufferingCount = 0;
    long g_seekPos = 0;

    public StreamCallback(AppCompatActivity activity) {
        g_ref = new WeakReference<>(activity);
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        Player.Listener.super.onPlaybackStateChanged(playbackState);
        switch (playbackState) {
            case Player.STATE_IDLE:
                Log.d(TAG, "onPlaybackStateChanged: Player.STATE_IDLE");
                if (g_bufferingCount >= 10) {
                    Log.w(TAG, "onPlaybackStateChanged: play video again");
                    g_bufferingCount = 0;
                    get().player_seek(g_seekPos);
                }
                break;
            case Player.STATE_BUFFERING:
                Log.d(TAG, "onPlaybackStateChanged: Player.STATE_BUFFERING");
                if (++g_bufferingCount >= 10) {
                    Log.w(TAG, "onPlaybackStateChanged: stop player");
                    g_seekPos = get().getCurrentPosition();
                    get().player_stop();
                }
                break;
            case Player.STATE_READY:
                Log.d(TAG, "onPlaybackStateChanged: Player.STATE_READY");
                break;
            case Player.STATE_ENDED:
                Log.d(TAG, "onPlaybackStateChanged: Player.STATE_ENDED");
                get().finish();
                break;
        }
    }

    @Override
    public void onPlayerError(@NonNull PlaybackException error) {
        Player.Listener.super.onPlayerError(error);
        Log.w(TAG, "onPlayerError: error = " + error);
        error.printStackTrace();
    }

    public StreamActivity get() {
        return (StreamActivity) g_ref.get();
    }

}
