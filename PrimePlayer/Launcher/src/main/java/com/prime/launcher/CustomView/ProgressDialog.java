package com.prime.launcher.CustomView;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.launcher.BaseDialog;
import com.prime.launcher.R;
import com.prime.datastructure.utils.TVMessage;

import java.lang.ref.WeakReference;

public class ProgressDialog extends BaseDialog {

    private static final String TAG = ProgressDialog.class.getSimpleName();

    private final WeakReference<AppCompatActivity> g_ref;
    private String g_title;
    private Runnable g_startAction;
    private Runnable g_dismissAction;

    public ProgressDialog(@NonNull Context context) {
        super(context, R.style.Theme_Launcher_DialogFullScreen);
        g_ref = new WeakReference<>((AppCompatActivity) context);
    }

    public ProgressDialog(@NonNull AppCompatActivity activity) {
        super(activity, R.style.Theme_Launcher_DialogFullScreen);
        g_ref = new WeakReference<>(activity);
    }

    public ProgressDialog(@NonNull AppCompatActivity activity, String title) {
        super(activity, R.style.Theme_Launcher_DialogFullScreen);
        g_ref = new WeakReference<>(activity);
        g_title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.semi_transparent);
            getWindow().setWindowAnimations(R.style.Theme_Launcher_DialogAnimation);
        }
        setContentView(R.layout.dialog_progress);

        TextView txvTitle = findViewById(R.id.dialog_title);
        txvTitle.setText(g_title);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (g_startAction != null) {
            g_startAction.run();
            g_startAction = null;
        }
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    public void set_title(String title) {
        g_title = title;
    }

    public void set_message(int resId) {
        set_message(get().getString(resId));
    }

    public void set_message(String message) {

    }

    public void set_start_action(Runnable action) {
        g_startAction = action;
    }

    public void set_dismiss_action(Runnable action) {
        g_dismissAction = action;
    }

    public AppCompatActivity get() {
        return g_ref.get();
    }

    @Override
    public void dismiss() {
        if (g_dismissAction != null) {
            g_dismissAction.run();
            g_dismissAction = null;
        }
        super.dismiss();
    }
}