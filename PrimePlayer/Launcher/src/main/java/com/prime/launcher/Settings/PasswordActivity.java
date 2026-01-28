package com.prime.launcher.Settings;

import static com.prime.launcher.Settings.UnlockChannelActivity.CHANNEL_LOCK_PASSWORD_LENGTH;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prime.launcher.BaseActivity;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.Utils.Utils;
import com.prime.launcher.R;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.utils.TVMessage;

import java.util.Locale;

public class PasswordActivity  extends BaseActivity implements PrimeDtv.LauncherCallback {
    private final static String TAG = "PasswordActivity";

    private TextView g_texv_old_password, g_texv_new_password, g_texv_new_password_again, g_texv_lock_done;
    private View g_view_old_password_line, g_view_new_password_line, g_view_new_password_again_line;
    private RelativeLayout g_blue_hint_key1, g_blue_hint_key2, g_blue_hint_key3;
    private StringBuffer g_old_password, g_new_password, g_new_password_again;
    private PrimeDtv g_prime_dtv;

    //toast ui
    private Toast g_toast;
    private TextView g_texv_toast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        g_prime_dtv.save_table(EnTableType.GPOS);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode >= KeyEvent.KEYCODE_0  && keyCode <= KeyEvent.KEYCODE_9) {
            handle_number_key(Utils.number_code_to_string(keyCode));
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            clear_text();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void init() {
        g_prime_dtv = HomeApplication.get_prime_dtv();

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, null);
        g_texv_toast = layout.findViewById(R.id.toast_message);
        g_toast = new Toast(getApplicationContext());
        g_toast.setDuration(Toast.LENGTH_SHORT);
        g_toast.setView(layout);

        g_texv_old_password = findViewById(R.id.lo_password_old);
        g_texv_new_password = findViewById(R.id.lo_password_new);
        g_texv_new_password_again = findViewById(R.id.lo_password_new_again);

        g_view_old_password_line = findViewById(R.id.lo_password_old_line);
        g_view_new_password_line = findViewById(R.id.lo_password_new_line);
        g_view_new_password_again_line = findViewById(R.id.lo_password_new_again_line);

        g_blue_hint_key1 = findViewById(R.id.lo_password_delete_frame1);
        g_blue_hint_key2 = findViewById(R.id.lo_password_delete_frame2);
        g_blue_hint_key3 = findViewById(R.id.lo_password_delete_frame3);

        g_texv_lock_done = findViewById(R.id.lo_password_lock_done);

        g_old_password = new StringBuffer();
        g_new_password = new StringBuffer();
        g_new_password_again = new StringBuffer();

        set_listener();
    }

    private void set_listener() {
        g_texv_old_password.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d(TAG, "texv_old_password: hasFocus");
                g_view_old_password_line.setBackgroundColor(getColor(R.color.pvr_red_color));
                g_blue_hint_key1.setVisibility(View.VISIBLE);
                g_blue_hint_key2.setVisibility(View.GONE);
                g_blue_hint_key3.setVisibility(View.GONE);
            }
            else g_view_old_password_line.setBackgroundColor(getColor(R.color.white));
        });

        g_texv_new_password.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d(TAG, "texv_new_password: hasFocus");
                g_view_new_password_line.setBackgroundColor(getColor(R.color.pvr_red_color));
                g_blue_hint_key1.setVisibility(View.GONE);
                g_blue_hint_key2.setVisibility(View.VISIBLE);
                g_blue_hint_key3.setVisibility(View.GONE);
            }
            else g_view_new_password_line.setBackgroundColor(getColor(R.color.white));
        });

        g_texv_new_password_again.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d(TAG, "texv_new_password_again: hasFocus");
                g_view_new_password_again_line.setBackgroundColor(getColor(R.color.pvr_red_color));
                g_blue_hint_key1.setVisibility(View.GONE);
                g_blue_hint_key2.setVisibility(View.GONE);
                g_blue_hint_key3.setVisibility(View.VISIBLE);
            }
            else g_view_new_password_again_line.setBackgroundColor(getColor(R.color.white));
        });

        g_texv_lock_done.setOnClickListener((v -> {
            check_pass_word();
        }));
    }

    @SuppressLint("SetTextI18n")
    private void handle_number_key(String valueOfNum) {
        //Log.d(TAG, "handle_number_key: valueOfNum = " + valueOfNum);
        if (g_texv_old_password.isFocused()) {
            if (g_old_password.length() < CHANNEL_LOCK_PASSWORD_LENGTH) {
                g_old_password.append(valueOfNum);
                g_texv_old_password.setText(g_texv_old_password.getText() + "* ");
            } else
                return;
            if (g_old_password.length() == CHANNEL_LOCK_PASSWORD_LENGTH)
                g_texv_new_password.requestFocus();
        } else if (g_texv_new_password.isFocused()) {
            if (g_new_password.length() < CHANNEL_LOCK_PASSWORD_LENGTH) {
                g_new_password.append(valueOfNum);
                g_texv_new_password.setText(g_texv_new_password.getText() + "* ");
            } else
                return;
            if (g_new_password.length() == CHANNEL_LOCK_PASSWORD_LENGTH)
                g_texv_new_password_again.requestFocus();
        } else if (g_texv_new_password_again.isFocused()) {
            if (g_new_password_again.length() < CHANNEL_LOCK_PASSWORD_LENGTH) {
                g_new_password_again.append(valueOfNum);
                g_texv_new_password_again.setText(g_texv_new_password_again.getText() + "* ");
            } else
                return;
            if (g_new_password_again.length() == CHANNEL_LOCK_PASSWORD_LENGTH)
                g_texv_lock_done.requestFocus();
        }
    }

    private void clear_text() {
        if (g_texv_old_password.isFocused()) {
            g_texv_old_password.setText("");
            g_old_password.delete(0, g_old_password.length());
        } else if (g_texv_new_password.isFocused()) {
            g_texv_new_password.setText("");
            g_new_password.delete(0, g_new_password.length());
        } else if (g_texv_new_password_again.isFocused()) {
            g_texv_new_password_again.setText("");
            g_new_password_again.delete(0, g_new_password_again.length());
        }
    }

    private void check_pass_word() {
        if (g_old_password.length() < CHANNEL_LOCK_PASSWORD_LENGTH
            || g_new_password.length() < CHANNEL_LOCK_PASSWORD_LENGTH
            || g_new_password_again.length() < CHANNEL_LOCK_PASSWORD_LENGTH) {
            g_texv_toast.setText(R.string.dmg_settings_password_subtitle);
            g_toast.show();
            return;
        }

        GposInfo gposInfo = g_prime_dtv.gpos_info_get();
        String oldPassword = String.format(Locale.US, "%04d", gposInfo.getPasswordValue());
        //Log.d(TAG, "check_pass_word: oldPassword = " + oldPassword);

        if (oldPassword.equals(g_old_password.toString())) {
            if (g_new_password.toString().equals(g_new_password_again.toString())) {
                Log.d(TAG, "check_pass_word: new password String to int = " + Integer.parseInt(g_new_password.toString()));
                gposInfo.setPasswordValue(Integer.parseInt(g_new_password.toString()));
                finish();
                return;
            }
            g_texv_toast.setText(R.string.dmg_settings_password_new_both_not_match);
            g_toast.show();
            return;
        }
        g_texv_toast.setText(R.string.dmg_settings_password_old_no_match);
        g_toast.show();
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);
        switch (tvMessage.getMsgType()) {
            case -1:
                break;
        }
    }
}
