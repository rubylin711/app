package com.prime.homeplus.membercenter.TvMail;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.prime.homeplus.membercenter.R;

import static android.content.Context.WINDOW_SERVICE;

public class TvMailNewDialog {
    public static TvMailNewDialog INSTANCE;
    private final String TAG = TvMailNewDialog.class.getSimpleName();
    private Context context;
    private View relayout;
    private Button button;

    public static TvMailNewDialog getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TvMailNewDialog.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TvMailNewDialog(context);
                }
            }
        }
        return INSTANCE;
    }

    public TvMailNewDialog(Context context) {
        this.context = context.getApplicationContext();
        initView();
    }

    final Handler handler = new Handler();
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            closeDialog();
        }
    };

    private void initView() {
        WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        relayout = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.dialog_tvmail_newmail, null);
        button = (Button) relayout.findViewById(R.id.button);

        windowManager.addView(relayout, layoutParams);
    }

    @SuppressLint("NewApi")
    public void showAlertDialog() {
        Log.d(TAG,"TvMailNewDialog SHOW.");
        if (relayout == null) {
            return;
        }
        if (relayout.getVisibility() == View.GONE) {
            relayout.setVisibility(View.VISIBLE);
        }
        handler.postDelayed(runnable, 180000);
        button.requestFocus();
        button.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        closeDialog();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        Log.d(TAG, "openMessageList");
                        closeDialog();
                    }

                }
                return false;
            }
        });
    }


    private void closeDialog() {
        Log.d(TAG, "closeTvMailNewDialog.");
        handler.removeCallbacks(runnable);
        relayout.setVisibility(View.GONE);
    }

}
