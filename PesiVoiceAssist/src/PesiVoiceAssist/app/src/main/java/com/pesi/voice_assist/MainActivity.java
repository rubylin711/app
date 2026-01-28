package com.pesi.voice_assist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
    String TAG = "TestBluetoothGATT";

    public static final String REMOTE_SERVICE_PACKAGE_NAME = "com.pesi.remote_service";

    public static final String ACTION_VOICE_ASSISTANT_START = "com.pesi.voice_assist.start";
    public static final String ACTION_VOICE_ASSISTANT_CLOSE = "com.pesi.voice_assist.close";
    public static final String ACTION_VOICE_ASSISTANT_CLOSE_MIC = "com.pesi.voice_assist.close.mic";

    public static final String ACTION_REMOTE_SERVICE_SERVER_RESPONSE = "com.pesi.remote_service.server.response";
    public static final String ACTION_REMOTE_SERVICE_SERVER_RESPONSE_TEXT = "com.pesi.remote_service.server.response.text";
    public static final String ACTION_REMOTE_SERVICE_SEND_MIC_OPEN = "com.pesi.remote_service.send.mic.open";
    public static final String ACTION_REMOTE_SERVICE_SEND_MIC_CLOSE = "com.pesi.remote_service.send.mic.close";
    public static final String ACTION_REMOTE_SERVICE_SEND_RECEIVE_AUDIO_DATA = "com.pesi.remote_service.send.receive.audio.data";

    //UI
    private TextView mTextView = null ;
    private TextView mVoiceTextView = null;
    private ImageView mVoiceIconImageView = null;
    private Animation mMicAnimation = null;

    private boolean mAudioReceiving = false;

    //Tool
    private AudioManager mAudioManager = null;
    private PowerManager mPowerManager = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d( TAG, "onCreate" ) ;
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.text_view);

        initUI();
        initAnimation();
        initTool();

        registerRemoteServiceReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d( TAG, "onStart" );
        sendBroadcastToRemoteService(ACTION_VOICE_ASSISTANT_START);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d( TAG, "onStop" );
        sendBroadcastToRemoteService(ACTION_VOICE_ASSISTANT_CLOSE);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d( TAG, "onDestroy" );
        unregisterReceiver(mRemoteServiceReceiver);
        sendBroadcastToRemoteService(ACTION_VOICE_ASSISTANT_CLOSE);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d( TAG, "onKeyUp keyCode = " + keyCode ) ;

        if ( keyCode == KeyEvent.KEYCODE_BACK) {
            if (mAudioReceiving) {
                Log.d( TAG, "onKeyUp KeyEvent.KEYCODE_BACK mAudioReceiving") ;
                sendBroadcastToRemoteService(ACTION_VOICE_ASSISTANT_CLOSE_MIC);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void initUI() {
        mVoiceTextView = findViewById(R.id.voice_text_View);
        mVoiceIconImageView = findViewById(R.id.voice_icon_image_View);
    }

    @SuppressLint("ResourceType")
    private void initAnimation() {
        //讀入動畫設定
        mMicAnimation = AnimationUtils.loadAnimation(this, R.animator.anim_alpha);
    }

    private void startAnimation() {
        Log.d(TAG, "startAnimation");
        //開始動畫
        mVoiceIconImageView.startAnimation(mMicAnimation);
    }

    private void stopAnimation() {
        Log.d(TAG, "stopAnimation");
        mVoiceIconImageView.clearAnimation();
    }

    private void initTool() {
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    }

    private void increaseVolume() {
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);;
    }

    private void decreaseVolume() {
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);;
    }

    private void muteVolume() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0 /*volume:0*/, AudioManager.FLAG_SHOW_UI);
        //mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        //Log.d(TAG, "Mute = " + mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC));
    }

    private void standby() {
        //mPowerManager.goToSleep(SystemClock.uptimeMillis());
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerRemoteServiceReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_REMOTE_SERVICE_SERVER_RESPONSE);
        intentFilter.addAction(ACTION_REMOTE_SERVICE_SEND_MIC_OPEN);
        intentFilter.addAction(ACTION_REMOTE_SERVICE_SEND_MIC_CLOSE);
        intentFilter.addAction(ACTION_REMOTE_SERVICE_SEND_RECEIVE_AUDIO_DATA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            registerReceiver(mRemoteServiceReceiver, intentFilter, Context.RECEIVER_EXPORTED);
        else
            registerReceiver(mRemoteServiceReceiver, intentFilter);
    }

    private final BroadcastReceiver mRemoteServiceReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: action = " + action);

            if (ACTION_REMOTE_SERVICE_SERVER_RESPONSE.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_REMOTE_SERVICE_SERVER_RESPONSE");
                String serverResponseText = intent.getStringExtra(ACTION_REMOTE_SERVICE_SERVER_RESPONSE_TEXT);
                Log.d(TAG, "serverResponseText = " + serverResponseText);
                mVoiceTextView.setText(serverResponseText);
            } else if (ACTION_REMOTE_SERVICE_SEND_MIC_OPEN.equals(action) ) {
                Log.d(TAG, "onReceive: ACTION_REMOTE_SERVICE_SEND_MIC_OPEN");
                mVoiceTextView.setText("");
            } else if (ACTION_REMOTE_SERVICE_SEND_MIC_CLOSE.equals(action) ) {
                Log.d(TAG, "onReceive: ACTION_REMOTE_SERVICE_SEND_MIC_CLOSE");
                mAudioReceiving = false;
                stopAnimation();
            } else if (ACTION_REMOTE_SERVICE_SEND_RECEIVE_AUDIO_DATA.equals(action) ) {
                Log.d(TAG, "onReceive: ACTION_REMOTE_SERVICE_SEND_RECEIVE_AUDIO_DATA");
                mAudioReceiving = true;
                startAnimation();
            }
        }
    };

    private void sendBroadcastToRemoteService(String action) {
        Log.d(TAG, "sendBroadcastToRemoteService = " + action);
        Intent intent = new Intent(action);
        intent.setPackage(REMOTE_SERVICE_PACKAGE_NAME);
        //intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        sendBroadcast(intent, action);
    }
}