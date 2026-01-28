package com.prime.aosp.media.launcher

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.R)
class BackgroundService:Service() {

    private val TAG = javaClass.simpleName;
    private lateinit var m_backgroundReceiver: BackgroundReceiver

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind: ")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
        m_backgroundReceiver = BackgroundReceiver()
        val filter = IntentFilter()
        //filter.addAction(Launcher.ACTION_PLAYER_INIT)
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED)
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED)
        filter.addAction(Intent.ACTION_MEDIA_EJECT)
        filter.addDataScheme("file");
        registerReceiver(m_backgroundReceiver, filter);
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        unregisterReceiver(m_backgroundReceiver)
    }
}