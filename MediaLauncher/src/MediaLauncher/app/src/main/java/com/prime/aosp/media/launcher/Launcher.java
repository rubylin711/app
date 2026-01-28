package com.prime.aosp.media.launcher;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.R)
@SuppressLint({"SetTextI18n", "PrivateApi"})
public class Launcher extends AppCompatActivity {

    String TAG = Launcher.class.getSimpleName();

    public static final String FOLDER_AUTO_PLAY = "AUTO_PLAY";
    public static final String ACTION_PLAYER_INIT = "com.prime.aosp.media.launcher.action.PLAYER_INIT";
    public static final String ACTION_MEDIA_PLAY = "com.prime.aosp.media.launcher.action.MEDIA_PLAY";
    public static final String ACTION_MEDIA_STOP = "com.prime.aosp.media.launcher.action.MEDIA_STOP";
    public static final String EXTRA_PLAY_LIST = "com.prime.aosp.media.launcher.extra.PLAY_LIST";

    Intent m_serviceIntent;
    LauncherReceiver m_launcherReceiver;
    Handler m_handler;

    // Exo Player
    ExoPlayer m_player; //SimpleExoPlayer m_player;
    ExoPlayerCallback m_playerCallback;

    // view
    ImageView m_playerImageView;
    PlayerView m_playerView;

    // data
    List<Integer> m_keyList = new ArrayList<>();
    List<String> m_playList = new ArrayList<>();
    int m_playListIndex = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupService(true);
        setupReceiver(true);
        setupVersion(true);
        setupHandler(true);
        setupUserSetupComplete();

        playerInit();
        //playerInitList();
        //playerPlay();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        m_keyList.add(0, keyCode);

        //for (int KeyDown : m_keyList)
        //    Log.d(TAG, "onKeyDown: " + KeyDown);

        if (KeyEvent.KEYCODE_BACK == keyCode)
            return true;

        if (m_keyList.size() >= 4) {
            if (KeyEvent.KEYCODE_DPAD_UP    == m_keyList.get(3) &&
                KeyEvent.KEYCODE_DPAD_DOWN  == m_keyList.get(2) &&
                KeyEvent.KEYCODE_DPAD_LEFT  == m_keyList.get(1) &&
                KeyEvent.KEYCODE_DPAD_RIGHT == m_keyList.get(0)) {
                m_keyList = new ArrayList<>();
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerDestroy();
        setupReceiver(false);
        setupService(false);
    }

    public void playerInit() {
        Log.d(TAG, "playerInit: ");

        // for player
        m_player = new ExoPlayer.Builder(this).build(); // m_player = new SimpleExoPlayer.Builder(this).build();

        // for callback
        m_playerCallback = new ExoPlayerCallback(this);
        m_player.addListener(m_playerCallback);

        // for image
        m_playerImageView = findViewById(R.id.player_image_view);

        // for video
        m_playerView = findViewById(R.id.player_view);
        m_playerView.setPlayer(m_player);
        m_playerView.setUseController(false);
    }

    public void playerInitList() {
        MediaHelper mediaHelper = null;
        String[] playArray = null;

        // init play list
        mediaHelper = new MediaHelper(this);
        playArray = mediaHelper.getPlayArray();

        m_playList = Arrays.asList(playArray);
    }

    public void playerPrintList(List<String> playList) {
        int index = 0;

        for (String path : playList) {
            Log.d(TAG, "playerPrintList: [" + index + "] " + path);
            index++;
        }
    }

    public void playerPlay() {
        playerPlay(m_playList);
    }

    public void playerPlay(List<String> playList) {
        Log.d(TAG, "playerPlay: ");

        if (playList == null || playList.size() == 0) {
            //Toast.makeText(this, "Media file not found", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "playerPlay: Media file not found");
            setupVersion(true);
            return;
        }
        m_playList = playList;
        playerPrintList(playList);

        // index out of bound ?
        int i = m_playListIndex;
        if ((i < 0) || (i >= playList.size())) {
            //Toast.makeText(this, "index out of bound: " + i, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "playerPlay: index out of bound: " + i);
            i = m_playListIndex = 0;
        }

        // play
        MediaFile file = new MediaFile(playList.get(i));
        if (file.isVideo())         playerPlayVideo(file);
        else if (file.isImage())    playerPlayImage(file);
        else                        playerPlayUnknown();

        m_playListIndex++;
        setupVersion(false);
    }

    public void playerPlayVideo(MediaFile file) {
        Log.d(TAG, "playerPlayVideo: file = " + file.getName());
        //Toast.makeText(this, "play video", Toast.LENGTH_SHORT).show();

        m_playerImageView.setVisibility(View.GONE);
        m_playerView.setVisibility(View.VISIBLE);

        MediaItem mediaItem = MediaItem.fromUri(file.toString());
        m_player.setMediaItem(mediaItem);
        m_player.prepare();
        m_player.play();
    }
    
    public void playerPlayImage(MediaFile file) {
        Log.d(TAG, "playerPlayImage: file = " + file.getName());
        //Toast.makeText(this, "play image", Toast.LENGTH_SHORT).show();

        m_playerImageView.setVisibility(View.VISIBLE);
        m_playerView.setVisibility(View.GONE);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        m_playerImageView.setImageDrawable(drawable);


        setupHandler(false);
        m_handler.postDelayed(this::playerPlay, 30000);
    }

    public void playerPlayUnknown() {
        Toast.makeText(this, "unknown file type", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "playerPlayUnknown: unknown file type");
    }

    public void playerStop() {
        Log.d(TAG, "playerStop: ");
        //Toast.makeText(this, "stop player", Toast.LENGTH_SHORT).show();

        m_playerImageView.setVisibility(View.GONE);
        m_playerView.setVisibility(View.GONE);

        m_playListIndex = 0;
        m_player.stop();

        setupVersion(true);
        setupHandler(false);
    }

    public void playerDestroy() {
        Log.d(TAG, "playerDestroy: ");
        m_player.removeListener(m_playerCallback);
        m_player.release();
    }

    private void setupReceiver(boolean enable) {
        Log.d(TAG, "setupReceiver: enable = " + enable);
        if (enable) {
            m_launcherReceiver = new LauncherReceiver(this);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_MEDIA_PLAY);
            filter.addAction(ACTION_MEDIA_STOP);
            LocalBroadcastManager.getInstance(this).registerReceiver(m_launcherReceiver, filter);
            //registerReceiver(m_launcherReceiver, filter);
        }
        else {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(m_launcherReceiver);
            //unregisterReceiver(m_launcherReceiver);
        }
    }

    private void setupService(boolean enable) {
        Log.d(TAG, "setupService: enable = " + enable);
        if (enable) {
            m_serviceIntent = new Intent(this, BackgroundService.class);
            startService(m_serviceIntent);
        }
        else
            stopService(m_serviceIntent);
    }

    private void setupVersion(boolean enable) {
        Log.d(TAG, "setupVersion: enable = " + enable);
        TextView versionView = findViewById(R.id.version);
        String version = "unknown";

        try {
            Process process = Runtime.getRuntime().exec("getprop ro.firmwareVersion");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            version = "version: " + line;
            Log.d(TAG, "version: " + line);
        } catch (IOException e) {
            e.printStackTrace();
        }

        versionView.setText(version);
        versionView.setZ(100);

        if (enable)
            versionView.setVisibility(View.VISIBLE);
        else
            versionView.setVisibility(View.GONE);
    }

    private void setupHandler(boolean enable) {
        Log.d(TAG, "setupHandler: enable = " + enable);
        if (enable)
            m_handler = new Handler();
        else
            m_handler.removeCallbacksAndMessages(null);
    }

    /**
     * android:sharedUserId="android.uid.system"
     * android:sharedUserMaxSdkVersion="32"
     *
     * <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
     *
     * signingConfigs {
     *      release {
     *          storeFile file("sign.jks")
     *          storePassword 'pesi1234'
     *          keyAlias 'key0'
     *          keyPassword 'pesi1234'
     *      }
     *      debug {
     *          storeFile file("sign.jks")
     *          storePassword 'pesi1234'
     *          keyAlias 'key0'
     *          keyPassword 'pesi1234'
     *      }
     *  }
     */
    private void setupUserSetupComplete() {
        Settings.Global.putString(getContentResolver(), "device_provisioned", "1");
        Settings.Secure.putString(getContentResolver(), "user_setup_complete", "1");
        Settings.Secure.putString(getContentResolver(), "tv_user_setup_complete", "1");
    }
}