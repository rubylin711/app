package com.prime.homeplus.settings;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Locale;

public class DebugOverlayService extends Service {
    private String TAG = "DebugOverlayService";

    private WindowManager mWindowManager;
    private View mOverlayView;
    private TextView mTextView;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mRunning = false;
    private long mLastIdle = 0;
    private long mLastTotal = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createOverlayView();
        startForegroundNotification();
        startUpdateLoop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 可以根據 intent extra 決定 show/hide, 這裡先簡單 always show
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRunning = false;
        if (mOverlayView != null) {
            mWindowManager.removeView(mOverlayView);
            mOverlayView = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createOverlayView() {
        if (mOverlayView != null) return;

        // 這邊你也可以用 inflate 自訂 layout
        mTextView = new TextView(this);
        mTextView.setText("CPU: --%\nMEM: --GB\nFREE:  --GB");
        mTextView.setTextSize(12);
        mTextView.setPadding(8, 4, 8, 4);

        // 背景小方塊
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0x88000000);          // 半透明黑
        bg.setCornerRadius(8);
        mTextView.setBackground(bg);
        mTextView.setTextColor(Color.WHITE);

        mOverlayView = mTextView;

        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        // 右下角
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.x = 20;  // 距右邊 20px
        params.y = 20;  // 距下方 20px

        mWindowManager.addView(mOverlayView, params);
    }

    @SuppressLint("ForegroundServiceType")
    private void startForegroundNotification() {
        // 做個簡單的前景通知，避免被系統殺掉
        String channelId = "debug_overlay_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Debug Overlay",
                    NotificationManager.IMPORTANCE_MIN
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Debug Overlay")
                .setContentText("顯示 CPU / Memory 使用率")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .build();

        startForeground(1, notification);
    }

    private void startUpdateLoop() {
        mRunning = true;
        mHandler.post(updateRunnable);
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mRunning) return;

            float cpu = readCpuUsagePercent();
            //float mem = readMemUsagePercent();
            float mem = readMemTotal();
            float free = readMemFree();

            String text = String.format(Locale.US,
                    "CPU:  %.2f%%\nMEM: %.3fGB\nFREE: %.3fGB", cpu, mem, free);
            mTextView.setText(text);

            mHandler.postDelayed(this, 1000); // 每 1 秒更新
        }
    };

    private float readMemUsagePercent() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        long total = mi.totalMem;
        long avail = mi.availMem;
        long used = total - avail;

        if (total == 0) return 0f;
        return used * 100f / total;
    }

    private float readMemTotal() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //Log.d(TAG, "readMemTotal: " + mi.totalMem);
        return mi.totalMem/1024f/1024f/1024f;
    }

    private float readMemFree() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //Log.d(TAG, "readMemFree: " + mi.availMem);
        return mi.availMem/1024f/1024f/1024f;
    }

    private float readCpuUsagePercent() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"))) {
            String line = reader.readLine(); // 第一行 "cpu  ..."
            if (line == null || !line.startsWith("cpu ")) {
                return 0f;
            }
            String[] toks = line.split("\\s+");
            long user = Long.parseLong(toks[1]);
            long nice = Long.parseLong(toks[2]);
            long system = Long.parseLong(toks[3]);
            long idle = Long.parseLong(toks[4]);
            long iowait = Long.parseLong(toks[5]);
            long irq = Long.parseLong(toks[6]);
            long softirq = Long.parseLong(toks[7]);

            long idleAll = idle + iowait;
            long nonIdle = user + nice + system + irq + softirq;
            long total = idleAll + nonIdle;

            long totalDiff = total - mLastTotal;
            long idleDiff = idleAll - mLastIdle;

            mLastTotal = total;
            mLastIdle = idleAll;

            if (totalDiff == 0) return 0f;

            return (totalDiff - idleDiff) * 100f / totalDiff;
        } catch (Exception e) {
            return 0f;
        }
    }
}
