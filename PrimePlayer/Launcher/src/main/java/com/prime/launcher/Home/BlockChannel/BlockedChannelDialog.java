package com.prime.launcher.Home.BlockChannel;

import static com.prime.launcher.Settings.UnlockChannelActivity.LOCK_TYPE_ADULT;
import static com.prime.launcher.Settings.UnlockChannelActivity.LOCK_TYPE_CHANNEL;
import static com.prime.launcher.Settings.UnlockChannelActivity.LOCK_TYPE_PARENT;
import static com.prime.launcher.Settings.UnlockChannelActivity.LOCK_TYPE_TIME;
import static com.prime.launcher.Settings.UnlockChannelActivity.PASSWORD_STAR;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.launcher.Utils.Utils;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.R;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.sysdata.GposInfo;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class BlockedChannelDialog extends Dialog {

    String TAG = getClass().getSimpleName();

    WeakReference<AppCompatActivity> g_ref;

    private String g_current_input_pass = "";
    private TextView g_pass_text;
    private TextView g_enter_text;
    private TextView g_wrong_text;
    private TextView g_lock_title;
    private PrimeDtv g_dtv;
    private int g_max_password_number = 4;
    private String g_strLockTitle;

    Callback g_callback;

    public interface Callback {
        void on_unlock_program();
    }

    public BlockedChannelDialog(AppCompatActivity activity) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.color.trans);
            getWindow().setWindowAnimations(R.style.Theme_Launcher_DialogAnimation);
            getWindow().setDimAmount(0);
        }
        setContentView(R.layout.dialog_unlock_live_tv);
        g_dtv = get().g_dtv;
        g_pass_text = findViewById(R.id.blocked_channel_textv_password);
        g_enter_text = findViewById(R.id.blocked_channel_textv_password_enter_hint);
        g_wrong_text = findViewById(R.id.blocked_channel_textv_pass_fail);
        g_lock_title = findViewById(R.id.blocked_channel_type);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setLockTitle(get(), g_strLockTitle);
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    private void clean_password() {
        Log.d(TAG, "clean_password:");
        g_enter_text.setVisibility(View.VISIBLE);
        g_pass_text.setText("");
        g_current_input_pass = "";
        g_wrong_text.setVisibility(View.GONE);
        g_pass_text.requestFocus();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.e(TAG, "onKeyDown: [keyCode] " + keyCode + ", [current focus] " + getCurrentFocus());
        if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            clean_password();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            Log.d(TAG, "dismiss ");
            clean_password();
            this.dismiss();
        }

        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            g_current_input_pass += Utils.number_code_to_string(keyCode);
            g_pass_text.setText(g_pass_text.getText() + PASSWORD_STAR);
            g_wrong_text.setVisibility(View.GONE);
            g_enter_text.setVisibility(View.GONE);
        }

        if (KeyEvent.KEYCODE_CHANNEL_UP == keyCode || KeyEvent.KEYCODE_CHANNEL_DOWN == keyCode) {
            clean_password();
            dismiss();
            if(KeyEvent.KEYCODE_CHANNEL_UP == keyCode) {
                get().g_liveTvMgr.set_channel_up(KeyEvent.ACTION_DOWN);
                get().g_liveTvMgr.set_channel_up(KeyEvent.ACTION_UP);
            }
            if(KeyEvent.KEYCODE_CHANNEL_DOWN == keyCode) {
                get().g_liveTvMgr.set_channel_down(KeyEvent.ACTION_DOWN);
                get().g_liveTvMgr.set_channel_down(KeyEvent.ACTION_UP);
            }
            return true;
        }

        if (g_current_input_pass.length() == g_max_password_number)
            check_password();
        return super.onKeyDown(keyCode, event);
    }

    private String getLockTitle(Context context, int lockType) {
        if (lockType != LOCK_TYPE_ADULT) {
            if (lockType == LOCK_TYPE_TIME) {
                return context.getString(R.string.lock_time);
            }
            if (lockType == LOCK_TYPE_PARENT) {
                return context.getString(R.string.lock_parent);
            }
            if (lockType == LOCK_TYPE_CHANNEL) {
                return context.getString(R.string.lock_channel);
            }
            return "";
        }
        return context.getString(R.string.lock_adult);
    }

    public void setLockTitle(BlockedChannel blockedChannel) {
        int lockType = -1;

        if (blockedChannel.isAdultLock())
            lockType = LOCK_TYPE_ADULT;
        else if (blockedChannel.isTimeLock())
            lockType = LOCK_TYPE_TIME;
        else if (blockedChannel.isParentalLock())
            lockType = LOCK_TYPE_PARENT;
        else if (blockedChannel.isChannelLock())
            lockType = LOCK_TYPE_CHANNEL;
        else
            Log.e(TAG, "setLockTitle: unknown lock type");

        g_strLockTitle = getLockTitle(get(), lockType);
    }

    private void setLockTitle(Context context,String title) {
        g_lock_title.setText(title);
    }

    private void check_password() {
        Log.d(TAG, "check_password:");
        GposInfo gposInfo = g_dtv.gpos_info_get();
        String passwordFromGposInfo = String.format(Locale.US, "%04d", gposInfo.getPasswordValue());
        set_Result(g_current_input_pass.equals(passwordFromGposInfo));
    }

    private void set_Result(boolean result) {
        Log.d(TAG, "set_Result: result = " + result);
        if (result) {
            g_pass_text.setText("");
            g_current_input_pass = "";
            this.dismiss();
            g_callback.on_unlock_program();
        }
        else {
            g_wrong_text.setVisibility(View.VISIBLE);
            g_pass_text.setText("");
            g_current_input_pass = "";
            g_enter_text.setVisibility(View.VISIBLE);
        }
    }

    public void register_callback(Callback callback) {
        g_callback = callback;
    }
}