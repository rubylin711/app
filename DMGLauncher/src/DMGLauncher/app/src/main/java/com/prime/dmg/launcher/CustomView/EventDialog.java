package com.prime.dmg.launcher.CustomView;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.prime.dmg.launcher.BaseDialog;
import com.prime.dmg.launcher.R;
import com.prime.dtv.utils.TVMessage;

import java.lang.ref.WeakReference;

public class EventDialog extends BaseDialog {

    private static final String TAG = EventDialog.class.getSimpleName();

    private final WeakReference<AppCompatActivity> g_ref;
    private String g_strTitle;
    private String g_strConfirm;
    private String g_strCancel;
    private Runnable g_startAction;
    private Runnable g_confirmAction;
    private Runnable g_cancelAction;
    private Runnable g_dismissAction;
    private Runnable g_backAction;
    
    public EventDialog(@NonNull AppCompatActivity activity) {
        super(activity, R.style.Theme_DMGLauncher_DialogFullScreen);
        g_ref = new WeakReference<>(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.semi_transparent);
            getWindow().setWindowAnimations(R.style.Theme_DMGLauncher_DialogAnimation);
        }
        setContentView(R.layout.dialog_event);

        TextView txvTitle = findViewById(R.id.Title);
        txvTitle.setText(g_strTitle);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (g_startAction != null)
            g_startAction.run();

        TextView btnYes = findViewById(R.id.button_yes);
        btnYes.setText(g_strConfirm);
        btnYes.setOnClickListener(this::on_click_yes);
        btnYes.setOnFocusChangeListener(this::on_focus_yes);
        
        TextView btnNo = findViewById(R.id.button_no);
        btnNo.setText(g_strCancel);
        btnNo.setOnClickListener(this::on_click_no);
        btnNo.setOnFocusChangeListener(this::on_focus_no);
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            on_click_back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void on_click_yes(View view) {
        if (g_confirmAction != null) {
            g_confirmAction.run();
            g_confirmAction = null;
        }
        dismiss();
    }
    
    public void on_click_no(View view) {
        if (g_cancelAction != null) {
            g_cancelAction.run();
            g_cancelAction = null;
        }
        dismiss();
    }

    public void on_click_back() {
        if (g_backAction != null) {
            g_backAction.run();
            g_backAction = null;
        }
        dismiss();
    }

    public void on_focus_yes(View view, boolean hasFocus) {
        TextView btnYes = findViewById(R.id.button_yes);
        View focusView = findViewById(R.id.button_yes_focus);
        btnYes.setTextColor(ContextCompat.getColor(getContext(), hasFocus ? R.color.white : R.color.opacity_white));
        focusView.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
    }
    
    public void on_focus_no(View view, boolean hasFocus) {
        TextView btnNo = findViewById(R.id.button_no);
        View focusView = findViewById(R.id.button_no_focus);
        btnNo.setTextColor(ContextCompat.getColor(getContext(), hasFocus ? R.color.white : R.color.opacity_white));
        focusView.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
    }

    public void set_title_text(int title) {
        g_strTitle = get().getString(title);
    }

    public void set_title_text(String title) {
        g_strTitle = title;
    }

    public void set_confirm_text(int text) {
        g_strConfirm = get().getString(text);
    }

    public void set_confirm_text(String text) {
        g_strConfirm = text;
    }

    public void set_cancel_text(int text) {
        g_strCancel = get().getString(text);
    }

    public void set_cancel_text(String text) {
        g_strCancel = text;
    }

    public void set_start_action(Runnable action) {
        g_startAction = action;
    }

    public void set_confirm_action(Runnable action) {
        g_confirmAction = action;
    }

    public void set_cancel_action(Runnable action) {
        g_cancelAction = action;
    }

    public void set_dismiss_action(Runnable action) {
        g_dismissAction = action;
    }

    public void set_back_action(Runnable action) {
        g_backAction = action;
    }

    public void set_timeout_action(Runnable action, int timeout) {
        CountDownTimer timer = new CountDownTimer(timeout, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                EventDialog.this.dismiss();
                action.run();
            }
        };
        timer.start();
    }

    public AppCompatActivity get() {
        return g_ref.get();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (g_dismissAction != null) {
            g_dismissAction.run();
            g_dismissAction = null;
        }
    }
}