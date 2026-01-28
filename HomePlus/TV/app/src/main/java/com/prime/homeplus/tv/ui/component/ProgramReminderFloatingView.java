package com.prime.homeplus.tv.ui.component;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.ui.activity.MainActivity;

public class ProgramReminderFloatingView {
    private static final String TAG = "ProgramReminderFloatingView";
    private final Context context;
    private final WindowManager windowManager;
    private View floatingView;
    private CountDownTimer countDownTimer;
    private final int COUNTDOWN_TIME_MS = 60 * 1000;

    private TextView tvReminderChannelNumber, tvReminderChannelName,
            tvReminderProgramName, tvReminderConfirmSecond;
    private Button btnReminderWatch, btnReminderCancel;

    public ProgramReminderFloatingView(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void show(String channelNumber, String channelName, String programName) {
        if (floatingView != null) return;

        LayoutInflater inflater = LayoutInflater.from(context);
        floatingView = inflater.inflate(R.layout.floating_reminder, null);

        int layoutFlag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;

        tvReminderChannelNumber = floatingView.findViewById(R.id.tvReminderChannelNumber);
        tvReminderChannelName = floatingView.findViewById(R.id.tvReminderChannelName);
        tvReminderProgramName = floatingView.findViewById(R.id.tvReminderProgramName);
        tvReminderConfirmSecond = floatingView.findViewById(R.id.tvReminderConfirmSecond);
        btnReminderWatch = floatingView.findViewById(R.id.btnReminderWatch);
        btnReminderCancel = floatingView.findViewById(R.id.btnReminderCancel);

        tvReminderChannelNumber.setText(channelNumber);
        tvReminderChannelName.setText(channelName);
        tvReminderProgramName.setText(programName);

        btnReminderWatch.setOnClickListener(v -> {
            Log.d(TAG, "User chose to watch program");
            remove();
            try {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("CHANNEL_DISPLAY_NUMBER", channelNumber);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to start MainActivity, error:" + e.toString());
            }
        });

        btnReminderWatch.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                remove();
                return true;
            }
            return false;
        });

        btnReminderCancel.setOnClickListener(v -> {
            Log.d(TAG, "User cancelled reminder");
            remove();
        });

        btnReminderCancel.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                remove();
                return true;
            }
            return false;
        });

        try {
            windowManager.addView(floatingView, params);
            btnReminderWatch.requestFocus();
        } catch (Exception e) {
            Log.e(TAG, "Failed to add floating view", e);
        }

        startCountdown(COUNTDOWN_TIME_MS);
    }

    public void remove() {
        cancelCountdown();
        if (floatingView != null) {
            try {
                windowManager.removeView(floatingView);
                floatingView = null;
            } catch (Exception e) {
                Log.e(TAG, "Failed to remove floating view", e);
            }
        }
    }

    private void startCountdown(long millisInFuture) {
        cancelCountdown();

        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                if (tvReminderConfirmSecond != null) {
                    tvReminderConfirmSecond.setText(String.valueOf(secondsLeft));
                }
            }

            public void onFinish() {
                if (btnReminderWatch != null) {
                    btnReminderWatch.performClick();
                }
            }
        }.start();
    }

    private void cancelCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}

