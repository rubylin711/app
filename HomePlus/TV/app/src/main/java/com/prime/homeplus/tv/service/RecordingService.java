package com.prime.homeplus.tv.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.prime.datastructure.CommuincateInterface.BookModule;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.data.ScheduledProgramData;
import com.prime.homeplus.tv.manager.RecordingManager;
import com.prime.homeplus.tv.utils.ScheduledProgramUtils;

public class RecordingService extends Service {
    private static final String TAG = "RecordingService";
    private static final String CHANNEL_ID = "recording_service";
    private static final String CHANNEL_NAME = "Recording";
    private static final int NOTIFICATION_ID = 1001;

    private RecordingManager mRecordingManager;
    private final RecordingManager.OnRecordingStateChangeListener mStateListener = id -> {
        Log.i(TAG, "RecordingManager reported stop for id: " + id);
        cancelRecordStopAlarm(id);

        ScheduledProgramUtils.scheduleNextScheduledProgram(this, id);

        checkAndStop();
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mRecordingManager = RecordingManager.getInstance(this);
        mRecordingManager.addOnRecordingStateChangeListener(mStateListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            checkAndStop();
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        Log.i(TAG, "onStartCommand: action = " + action);
        long bookId = intent.getLongExtra(BookInfo.BOOK_ID, -1);
        Log.d(TAG, "onStartCommand: bookId = " + bookId);

        if (BookModule.ACTION_START_RECORD.equals(action)) {
            startMyForeground();
            if (!handleStartRecording(bookId)) {
                checkAndStop();
            }
        } else if (BookModule.ACTION_STOP_RECORD.equals(action)) {
            handleStopRecording(bookId);
        } else if (BookModule.ACTION_STOP_ALL_RECORD.equals(action)) {
            mRecordingManager.stopAllRecordings();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mRecordingManager.removeOnRecordingStateChangeListener(mStateListener);
        super.onDestroy();
    }

    private boolean handleStartRecording(long bookId) {
        Log.d(TAG, "handleStartRecording: " + bookId);
        if (mRecordingManager.isRecording(bookId)) {
            Log.i(TAG, "handleStartRecording: already recording, skip id = " + bookId);
            return false;
        }

        ScheduledProgramData data = ScheduledProgramUtils.getScheduledProgram(this, bookId);
        if (data == null) {
            Log.e(TAG, "handleStartRecording: ScheduledProgram not found by id = " + bookId);
            return false;
        }

        mRecordingManager.startRecording(data);

        setRecordStopAlarm(data);

        return true;
    }

    private void handleStopRecording(long bookId) {
        Log.d(TAG, "handleStopRecording: bookId = " + bookId);
        mRecordingManager.stopRecording(bookId);
    }

    private void startMyForeground() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MIN);
        channel.setShowBadge(false);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.bg_pvr_title)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentTitle(null)
                .setContentText(null)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void checkAndStop() {
        if (mRecordingManager.getRecordingCount() == 0) {
            stopForeground(true);
            stopSelf();
        }
    }

    private void setRecordStopAlarm(ScheduledProgramData data) {
        if (data == null) {
            return;
        }

        Intent intent = new Intent(this, RecordingService.class);
        intent.setAction(BookModule.ACTION_STOP_RECORD);

        long bookId = data.getId();
        long endTime = data.getEndTimeUtcMillis();
        intent.putExtra(BookInfo.BOOK_ID, bookId);

        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                (int) bookId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    endTime,
                    pendingIntent
            );
            Log.d(TAG, "setRecordStopAlarm: stop alarm set for bookId " + bookId + " at " + endTime);
        }
    }

    private void cancelRecordStopAlarm(long bookId) {
        Intent intent = new Intent(this, RecordingService.class);
        intent.setAction(BookModule.ACTION_STOP_RECORD);
        PendingIntent pendingIntent = PendingIntent.getService(
                this, (int) bookId, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();

            Log.d(TAG, "cancelRecordStopAlarm: cancel stop alarm for bookId " + bookId);
        }
    }
}
