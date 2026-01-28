package com.prime.dmg.launcher.CustomView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.BaseDialog;
import com.prime.dmg.launcher.R;
import com.prime.dtv.utils.TVMessage;

import java.lang.ref.WeakReference;

public class MessageDialog extends BaseDialog {

    String TAG = MessageDialog.class.getSimpleName();

    WeakReference<AppCompatActivity> g_ref;
    int DIALOG = 0, PANEL = 1, EDIT_PANEL = 2;
    int g_show_status = DIALOG;
    boolean g_showDialog = true;
    boolean g_ShowCancel = false;
    String g_message;

    // button action
    Runnable g_confirmAction;
    Runnable g_dismissAction;

    public MessageDialog(@NonNull Context context) {
        super(context, R.style.Theme_DMGLauncher_DialogFullScreen);
        g_ref = new WeakReference<>((AppCompatActivity) context);
        g_message = get().getString(R.string.dialog_lang_none);
    }

    public MessageDialog(@NonNull AppCompatActivity activity) {
        super(activity, R.style.Theme_DMGLauncher_DialogFullScreen);
        g_ref = new WeakReference<>(activity);
        g_message = get().getString(R.string.dialog_lang_none);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.semi_transparent);
            getWindow().setWindowAnimations(R.style.Theme_DMGLauncher_DialogAnimation);
        }
        setContentView(R.layout.dialog_message);
    }

    @Override
    protected void onStart() {
        super.onStart();

        View messageDialog = findViewById(R.id.message_dialog);
        View messagePanel  = findViewById(R.id.message_panel);
        messageDialog.setVisibility(is_show_dialog() ? View.VISIBLE : View.GONE);
        messagePanel.setVisibility(is_show_dialog() ? View.GONE : View.VISIBLE);

        if (g_show_status == DIALOG) {
            TextView dialogMsg = findViewById(R.id.message_dialog_text);
            dialogMsg.setText(g_message);
        }
        else if (g_show_status == PANEL) {
            TextView txvPanelMsg = findViewById(R.id.message_panel_main_text);
            Button   btnConfirm  = findViewById(R.id.message_panel_button_confirm);
            Button   btnCancel   = findViewById(R.id.message_panel_button_cancel);
            txvPanelMsg.setText(g_message);
            btnCancel.setVisibility(g_ShowCancel ? View.VISIBLE : View.GONE);
            btnConfirm.setOnClickListener(this::on_click_confirm);
            btnCancel.setOnClickListener(this::on_click_cancel);
        }
        else if (g_show_status == EDIT_PANEL) {
            TextView txvPanelMsg = findViewById(R.id.message_panel_main_text);
            EditText editTextMsg = findViewById(R.id.message_panel_label_edit);
            Button   btnConfirm  = findViewById(R.id.message_panel_button_confirm);
            Button   btnCancel   = findViewById(R.id.message_panel_button_cancel);
            txvPanelMsg.setVisibility(View.GONE);
            editTextMsg.setVisibility(View.VISIBLE);
            editTextMsg.setText(g_message);
            btnCancel.setVisibility(g_ShowCancel ? View.VISIBLE : View.GONE);
            btnConfirm.setOnClickListener(this::on_click_confirm);
            btnCancel.setOnClickListener(this::on_click_cancel);
        }
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    @Override
    public void dismiss() {
        if (g_dismissAction != null) {
            g_dismissAction.run();
            g_dismissAction = null;
        }
        super.dismiss();
    }

    public void on_click_confirm(View v) {
        dismiss();
        if (g_confirmAction != null) {
            g_confirmAction.run();
            g_confirmAction = null;
        }
    }

    public void on_click_cancel(View v) {
        dismiss();
    }

    public void set_content_message(int redId) {
        g_message = get().getString(redId);
    }

    public void set_content_message(String msg) {
        g_message = msg;
    }

    public void set_cancel_visible(boolean visible) {
        g_ShowCancel = visible;
    }

    public void set_confirm_action(Runnable action) {
        g_confirmAction = action;
    }

    public void set_dismiss_action(Runnable action) {
        g_dismissAction = action;
    }

    public String get_input_text() {
        EditText editTextMsg = findViewById(R.id.message_panel_label_edit);
        return editTextMsg == null ? "" : editTextMsg.getText().toString();
    }

    public AppCompatActivity get() {
        return g_ref.get();
    }

    public boolean is_show_dialog() {
        return g_showDialog;
    }

    public boolean is_show_panel() {
        return !g_showDialog;
    }

    public void show_dialog() {
        show_dialog(g_message);
    }

    public void show_dialog(int resId) {
        show_dialog(getContext().getString(resId));
    }

    public void show_dialog(String msg) {
        g_message = msg;
        g_show_status = DIALOG;
        g_showDialog = true;
        show();
    }

    public void show_panel() {
        show_panel(g_message);
    }

    public void show_panel(int resId, boolean showCancel) {
        g_ShowCancel = showCancel;
        show_panel(resId);
    }

    public void show_panel(int resId) {
        show_panel(getContext().getString(resId));
    }

    public void show_panel(String msg, Runnable action) {
        g_confirmAction = action;
        show_panel(msg);
    }

    public void show_panel(String msg) {
        g_message = msg;
        g_showDialog = false;
        g_show_status = PANEL;
        show();
    }

    public void show_edit_panel(String msg) {
        g_message = msg;
        g_show_status = EDIT_PANEL;
        g_showDialog = false;
        g_ShowCancel = true;
        show();
    }
}
