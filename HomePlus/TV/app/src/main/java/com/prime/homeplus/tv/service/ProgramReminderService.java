package com.prime.homeplus.tv.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.ui.component.ProgramReminderFloatingView;

public class ProgramReminderService extends Service {

    private static final String TAG = "ProgramReminderService";
    private static final String CHANNEL_ID = "ReminderServiceChannel";
    private static final int NOTIFICATION_ID = 102;
    public static boolean isRunning = false;
    private ProgramReminderFloatingView floatingView;
    private ProgramReminderReceiver programReminderReceiver;
    public static final String ACTION_SHOW_REMINDER = "com.prime.homeplus.tv.ACTION_SHOW_REMINDER";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Created.");

        floatingView = new ProgramReminderFloatingView(this);

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // program reminder scheduler

        programReminderReceiver = new ProgramReminderReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SHOW_REMINDER);
        registerReceiver(programReminderReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // start foreground service
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Program Reminder service is running")
                .setContentText("Monitoring scheduled reminders...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        isRunning = true;
        startForeground(NOTIFICATION_ID, notification);
        Log.d(TAG, "Service started as Foreground.");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service Destroyed.");

        if (programReminderReceiver != null) {
            unregisterReceiver(programReminderReceiver);
            programReminderReceiver = null;
        }

        floatingView.remove();
        stopForeground(true);
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Program Reminder",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private class ProgramReminderReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "ProgramReminderReceiver onReceive action:" + intent.getAction());
            if (intent != null && ACTION_SHOW_REMINDER.equals(intent.getAction())) {
                String channelNumber = intent.getStringExtra("channelNumber");
                String channelName = intent.getStringExtra("channelName");
                String programName = intent.getStringExtra("programName");

                Log.d(TAG, "Received broadcast: " + channelNumber + ", " + channelName + ", " + programName);

                if (floatingView != null) {
                    floatingView.show(channelNumber, channelName, programName);
                }
            }
        }
    }
}
