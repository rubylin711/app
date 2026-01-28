package com.prime.dmg.launcher;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dtv.PrimeHomeReceiver;
import com.prime.dtv.utils.TVMessage;

import java.lang.ref.WeakReference;

public abstract class BaseDialog extends Dialog implements PrimeHomeReceiver.Callback, GlobalKeyReceiver.Callback {
    String TAG = BaseDialog.class.getSimpleName();
    PrimeHomeReceiver g_homeReceiver;
    WeakReference<AppCompatActivity> g_ref;

    public interface DialogLifecycleListener {
        void onDialogShow(Dialog dialog);
        void onDialogDismiss(Dialog dialog);
    }

    private static DialogLifecycleListener lifecycleListener;

    public BaseDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        g_ref = new WeakReference<>((AppCompatActivity) context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        register_home_receiver();
        GlobalKeyReceiver.register_callback(this);
    }

    @Override
    public void on_press_home() {
        Log.d(TAG, "on_press_home: close dialog");
        g_ref.get().unregisterReceiver(g_homeReceiver);
        dismiss();
    }

    @Override
    public void on_press_global_key(int keyCode) {
        Log.d(TAG, "on_press_global_key: keyCode = " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_TV:       // Live TV
            case KeyEvent.KEYCODE_GUIDE:    // EPG
            case KeyEvent.KEYCODE_BUTTON_3: // YouTube
            case KeyEvent.KEYCODE_BUTTON_4: // Netflix
            case KeyEvent.KEYCODE_BUTTON_5: // Hottest
            case KeyEvent.KEYCODE_BUTTON_6: // Prime Video
            case KeyEvent.KEYCODE_BUTTON_7: // LiTV
                dismiss();
                break;
        }
    }

    public void register_home_receiver() {
        if (g_homeReceiver == null)
            g_homeReceiver = new PrimeHomeReceiver();
        g_homeReceiver.register_callback(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            g_ref.get().registerReceiver(g_homeReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    public static void setDialogLifecycleListener(DialogLifecycleListener listener) {
        lifecycleListener = listener;
    }

    @Override
    public void show() {
        super.show();
        if (lifecycleListener != null) {
            lifecycleListener.onDialogShow(this);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (lifecycleListener != null) {
            lifecycleListener.onDialogDismiss(this);
        }
    }


    public abstract void onMessage(TVMessage msg) ;
}
