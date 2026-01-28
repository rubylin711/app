package com.prime.launcher.Home.Recommend.Stream;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultAllocator;
import androidx.media3.ui.PlayerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.prime.launcher.BaseActivity;
import com.prime.launcher.R;
import com.prime.datastructure.utils.TVMessage;

public class StreamActivity extends BaseActivity {

    static String TAG;

    public static final String EXTRA_STREAM_URL = "EXTRA_STREAM_URL";

    StreamCallback g_streamCallback;
    ExoPlayer g_exoPlayer;
    String g_streamUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        TAG = getClass().getSimpleName();
        Intent intent = getIntent();
        g_streamUrl = intent.getStringExtra(EXTRA_STREAM_URL);
    }

    @Override
    protected void onStart() {
        super.onStart();
        player_init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        player_play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        player_reset();
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }

    /**
     * // player for insufficient memory
     * loadControl = new DefaultLoadControl.Builder()
     *      .setAllocator(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
     *      .setBufferDurationsMs(5000,10000,2000,2000)
     *      .setTargetBufferBytes(-1)
     *      .setPrioritizeTimeOverSizeThresholds(true)
     *      .build();
     * renderersFactory = new DefaultRenderersFactory(this)
     *      .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
     * player = new ExoPlayer.Builder(this, m_renderersFactory)
     *      .setLoadControl(m_loadControl)
     *      .build();
     * // speed
     * player.setPlaybackParameters(new PlaybackParameters(3));
     */
    public void player_init() {
        Log.d(TAG, "player_init: ");
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
           .setAllocator(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
           .setBufferDurationsMs(5000,30000,5000,5000)
           .setTargetBufferBytes(-1)
           .setPrioritizeTimeOverSizeThresholds(false)
           .build();

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
        trackSelector.setParameters(
            trackSelector.buildUponParameters().setMaxVideoBitrate(800000)
        );

        // for player
        g_exoPlayer = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl).build(); // player = new SimpleExoPlayer.Builder(this).build();
        g_exoPlayer.setVideoSurface(null);

        // for callback
        g_streamCallback = new StreamCallback(this);
        g_exoPlayer.addListener(g_streamCallback);

        // for video
        PlayerView playerView = findViewById(R.id.lo_player_view);
        playerView.setPlayer(g_exoPlayer);
        playerView.setUseController(false);
        //playerView.showController();
    }

    public long getCurrentPosition() {
        return g_exoPlayer.getCurrentPosition();
    }

    public void player_play() {
        Log.d(TAG, "player_play: " + g_streamUrl);
        MediaItem mediaItem = MediaItem.fromUri(g_streamUrl);
        g_exoPlayer.setMediaItem(mediaItem);
        g_exoPlayer.prepare();
        g_exoPlayer.play();
    }

    public void player_seek(long position) {
        Log.d(TAG, "player_seek: position " + position);
        MediaItem mediaItem = MediaItem.fromUri(g_streamUrl);
        g_exoPlayer.setMediaItem(mediaItem);
        g_exoPlayer.prepare();
        g_exoPlayer.play();
        g_exoPlayer.seekTo(position);
    }

    public void player_stop() {
        Log.d(TAG, "player_stop: ");
        g_exoPlayer.stop();
    }

    public void player_reset() {
        Log.d(TAG, "player_reset: ");
        g_exoPlayer.stop();
        g_exoPlayer.clearVideoSurface();
        g_exoPlayer.setVideoSurface(null);
        g_exoPlayer.removeListener(g_streamCallback);
        g_exoPlayer.release();
        g_exoPlayer = null;
        g_streamCallback = null;
    }
}