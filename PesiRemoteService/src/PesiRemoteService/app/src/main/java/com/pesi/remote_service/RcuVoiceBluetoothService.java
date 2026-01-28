package com.pesi.remote_service;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

public class RcuVoiceBluetoothService extends Service
{
    private static final String TAG = "RcuVoiceBluetoothService";
    public static final int SERVER_RESPONSE = 500 ;
    public static final int SEND_MIC_OPEN = 600;
    public static final int SEND_MIC_CLOSE = 601;
    public static final int SEND_MIC_RECEIVE_AUDIO_DATA = 602;

    public static final String VOICE_ASSISTANT_PACKAGE_NAME = "com.pesi.voice_assist";

    public static final String ACTION_VOICE_ASSISTANT_START = "com.pesi.voice_assist.start";
    public static final String ACTION_VOICE_ASSISTANT_CLOSE = "com.pesi.voice_assist.close";
    public static final String ACTION_VOICE_ASSISTANT_MIC_CLOSE = "com.pesi.voice_assist.close.mic";

    public static final String ACTION_REMOTE_SERVICE_SERVER_RESPONSE = "com.pesi.remote_service.server.response";
    public static final String ACTION_REMOTE_SERVICE_SERVER_RESPONSE_TEXT = "com.pesi.remote_service.server.response.text";
    public static final String ACTION_REMOTE_SERVICE_SEND_MIC_OPEN = "com.pesi.remote_service.send.mic.open";
    public static final String ACTION_REMOTE_SERVICE_SEND_MIC_CLOSE = "com.pesi.remote_service.send.mic.close";
    public static final String ACTION_REMOTE_SERVICE_SEND_RECEIVE_AUDIO_DATA = "com.pesi.remote_service.send.receive.audio.data";

    public RcuVoiceBluetoothGATT mRcuVoiceBluetoothGATT = null ;

    private String[] PERMISSIONS_ARRAY = {
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_ADVERTISE,
            android.Manifest.permission.BLUETOOTH_PRIVILEGED
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case SERVER_RESPONSE:
                    Log.d( TAG, "SERVER_RESPONSE =" + msg.obj ) ;
                    sendBroadcastToVoiceAssistant(ACTION_REMOTE_SERVICE_SERVER_RESPONSE, (String)msg.obj);
                    break;
                case SEND_MIC_OPEN:
                    Log.d( TAG, "SEND_MIC_OPEN") ;
                    sendBroadcastToVoiceAssistant(ACTION_REMOTE_SERVICE_SEND_MIC_OPEN);
                    break;
                case SEND_MIC_CLOSE:
                    Log.d( TAG, "SEND_MIC_CLOSE") ;
                    sendBroadcastToVoiceAssistant(ACTION_REMOTE_SERVICE_SEND_MIC_CLOSE);
                    break;
                case SEND_MIC_RECEIVE_AUDIO_DATA:
                    Log.d( TAG, "SEND_MIC_RECEIVE_AUDIO_DATA") ;
                    sendBroadcastToVoiceAssistant(ACTION_REMOTE_SERVICE_SEND_RECEIVE_AUDIO_DATA);
                    break;
                default:
                    Log.d( TAG, "unkown case " + msg.what ) ;
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        Log.d( TAG, "onCreate" );
        /*int permission = ActivityCompat.checkSelfPermission(this, Arrays.toString(PERMISSIONS_ARRAY));
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            Log.d( TAG, "there is BLUETOOTH PERMISSIONS" );
            //ActivityCompat.requestPermissions((MainActivity)getBaseContext(), PERMISSIONS_ARRAY, 1);
        }
        else
            Log.d( TAG, "there is not BLUETOOTH PERMISSIONS " );*/

        registerVoiceAssistantReceiver();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mRcuVoiceBluetoothGATT = new RcuVoiceBluetoothGATT(this, mHandler);
        mRcuVoiceBluetoothGATT.connectBluetoothGatt();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d( TAG, "onDestroy" );
        if (mRcuVoiceBluetoothGATT != null) {
            mRcuVoiceBluetoothGATT.disconnectBluetoothGatt();
            mRcuVoiceBluetoothGATT = null ;
        }
        unregisterReceiver(mVoiceAssistantReceiver);
        super.onDestroy();
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerVoiceAssistantReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_VOICE_ASSISTANT_START);
        intentFilter.addAction(ACTION_VOICE_ASSISTANT_CLOSE);
        intentFilter.addAction(ACTION_VOICE_ASSISTANT_MIC_CLOSE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            registerReceiver(mVoiceAssistantReceiver, intentFilter, Context.RECEIVER_EXPORTED);
        else
            registerReceiver(mVoiceAssistantReceiver, intentFilter);
    }

    private final BroadcastReceiver mVoiceAssistantReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: action = " + action);

            if (ACTION_VOICE_ASSISTANT_START.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_VOICE_ASSISTANT_START");
                mRcuVoiceBluetoothGATT.setVoiceAssistantStartFlag(true);
            } else if (ACTION_VOICE_ASSISTANT_CLOSE.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_VOICE_ASSISTANT_CLOSE");
                mRcuVoiceBluetoothGATT.setVoiceAssistantStartFlag(false);
            } else if (ACTION_VOICE_ASSISTANT_MIC_CLOSE.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_VOICE_ASSISTANT_MIC_CLOSE");
                mRcuVoiceBluetoothGATT.sendMsg(RcuVoiceBluetoothGATT.ATVV_TX_COMMAND_SEND_MIC_CLOSE);
            }
        }
    };

    private void sendBroadcastToVoiceAssistant(String action, String text) {
        Intent intent = new Intent(action);
        intent.setPackage(VOICE_ASSISTANT_PACKAGE_NAME);
        intent.putExtra(ACTION_REMOTE_SERVICE_SERVER_RESPONSE_TEXT, text);
        sendBroadcast(intent, action);
    }

    private void sendBroadcastToVoiceAssistant(String action) {
        Intent intent = new Intent(action);
        intent.setPackage(VOICE_ASSISTANT_PACKAGE_NAME);
        sendBroadcast(intent, action);
    }
}
