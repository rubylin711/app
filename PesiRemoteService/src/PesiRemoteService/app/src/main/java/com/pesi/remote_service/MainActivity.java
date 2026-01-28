package com.pesi.remote_service;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    String TAG = "TestBluetoothGATT";
    public RcuVoiceBluetoothGATT mRcuVoiceBluetoothGATT = null ;
    public static final int SERVER_RESPONSE = 500 ;
    private TextView mTextView = null ;
    private String[] PERMISSIONS_ARRAY = {
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_ADVERTISE,
            android.Manifest.permission.BLUETOOTH_PRIVILEGED
    };
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case SERVER_RESPONSE:
                    mTextView.setText((String) msg.obj);
                break;
                default:
                    Log.d( TAG, "unkown case " + msg.what ) ;
                break;
            }
        }
    };


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d( TAG, "onCreate" ) ;
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.text_view) ;
        int permission = ActivityCompat.checkSelfPermission(this, Arrays.toString(PERMISSIONS_ARRAY));
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(this, PERMISSIONS_ARRAY, 1);
//        }
//        else
        {
            mRcuVoiceBluetoothGATT = new RcuVoiceBluetoothGATT(this, mHandler);
            mRcuVoiceBluetoothGATT.connectBluetoothGatt();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRcuVoiceBluetoothGATT = new RcuVoiceBluetoothGATT(this, mHandler);
        mRcuVoiceBluetoothGATT.connectBluetoothGatt();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d( TAG, "onDestroy" ) ;
        if (mRcuVoiceBluetoothGATT != null) {
            mRcuVoiceBluetoothGATT.disconnectBluetoothGatt();
            mRcuVoiceBluetoothGATT = null ;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d( TAG, "onKeyDown keyCode = " + keyCode ) ;
        if ( keyCode == KeyEvent.KEYCODE_0)
        {
            mRcuVoiceBluetoothGATT.sendMsg(RcuVoiceBluetoothGATT.ATVV_TX_COMMAND_SEND_MIC_OPEN);
        }
        else if ( keyCode == KeyEvent.KEYCODE_1)
        {
            mRcuVoiceBluetoothGATT.sendMsg(RcuVoiceBluetoothGATT.ATVV_TX_COMMAND_SEND_MIC_CLOSE);
        }
        return super.onKeyUp(keyCode, event);
    }
}