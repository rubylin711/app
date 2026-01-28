package com.prime.launcher.Settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.prime.launcher.HomeApplication;
import com.prime.launcher.Utils.Utils;
import com.prime.launcher.R;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.utils.TVMessage;

import java.util.Locale;

public class UnlockChannelActivity extends FragmentActivity implements PrimeDtv.LauncherCallback {
    private final static String TAG = "UnlockChannelActivity";

    public static final String ACTIVITY_UNLOCK_CHANNEL = "com.prime.launcher.Settings.UnlockChannelActivity";
    public static final String INTENT_KEY_LOCK_TYPE = "KEY_LOCK_TYPE";
    public static final String INTENT_KEY_PASS_RESULT = "KEY_PASS_RESULT";
    public static final String INTENT_KEY_PASS_TEXT = "KEY_PASS_TEXT";
    public static final String INTENT_KEY_CHANNEL_LOCK_COUNT = "KEY_CHANNEL_LOCK_COUNT";

    public static final String DEFAULT_WORKER_LOCK_PASSWORD = "13197776";
    public static final String DEFAULT_PIN_PASSWORD = "0000";

    public static final String PASSWORD_STAR = "*";
    public static final int DMG_SETTINGS_REQUEST_CODE = 1234;
    public static final int LOCK_TYPE_ADULT = 1;
    public static final int LOCK_TYPE_CHANNEL = 2;
    public static final int LOCK_TYPE_TIME = 3;
    public static final int LOCK_TYPE_PARENT = 4;
    public static final int LOCK_TYPE_WORKER = 5;
    public static final  int CHANNEL_LOCK_PASSWORD_LENGTH = 4;
    public static final  int ENGINEERING_LOCK_PASSWORD_LENGTH = 8;

    private PrimeDtv g_dtv;
    private int g_current_lock_type;
    private TextView g_enter_text;
    private TextView g_lock_title;
    private TextView g_pass_text;
    private TextView g_wrong_text;
    private String g_current_input_pass = "";
    private int g_max_password_number = 4;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_channel);

        g_dtv = HomeApplication.get_prime_dtv();
        g_pass_text = findViewById(R.id.lo_unlock_channel_textv_password);
        g_wrong_text = findViewById(R.id.lo_unlock_channel_textv_pass_fail);
        g_lock_title = findViewById(R.id.lo_unlock_channel_lock_type);
        g_enter_text = findViewById(R.id.lo_unlock_channel_textv_password_enter_hint);
        if (getIntent() != null) {
            int intExtra = getIntent().getIntExtra(INTENT_KEY_LOCK_TYPE, 0);
            g_current_lock_type = intExtra;
            g_lock_title.setText(getLockTitle(this, intExtra));
        }
        if (g_current_lock_type == LOCK_TYPE_WORKER) {
            g_max_password_number = ENGINEERING_LOCK_PASSWORD_LENGTH;
            g_enter_text.setText(getText(R.string.enter_pass_eight));
        }
    }

    public static String getLockTitle(Context context, int lockType) {
        if (lockType != LOCK_TYPE_ADULT) {
            if (lockType == LOCK_TYPE_TIME) {
                return context.getString(R.string.lock_time);
            }
            if (lockType == LOCK_TYPE_PARENT) {
                return context.getString(R.string.lock_parent);
            }
            if (lockType == LOCK_TYPE_WORKER) {
                return context.getString(R.string.lock_worker);
            }
            return context.getString(R.string.lock_channel);
        }
        return context.getString(R.string.lock_adult);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            clean_password();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
            if (remove_character())
                return true;

        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            g_current_input_pass += Utils.number_code_to_string(keyCode);
            g_pass_text.setText(g_pass_text.getText() + PASSWORD_STAR);
            g_enter_text.setVisibility(View.GONE);
        }

        if (g_current_input_pass.length() == g_max_password_number)
            check_password();
        return super.onKeyUp(keyCode, event);
    }

    private void check_password() {
        Log.d(TAG, "check_password:");
        GposInfo gposInfo = g_dtv.gpos_info_get();
        String passwordFromGposInfo;
        if (g_current_lock_type == LOCK_TYPE_WORKER) {
            passwordFromGposInfo = String.format(Locale.US, "%08d", gposInfo.getWorkerPasswordValue());
            set_Result(g_current_input_pass.equals(passwordFromGposInfo));
        } else {
            passwordFromGposInfo = String.format(Locale.US, "%04d", gposInfo.getPasswordValue());
            set_Result(g_current_input_pass.equals(passwordFromGposInfo));
        }
    }

    private void clean_password() {
        Log.d(TAG, "clean_password:");
        g_enter_text.setVisibility(View.VISIBLE);
        g_pass_text.setText("");
        g_current_input_pass = "";
        g_wrong_text.setVisibility(View.GONE);
        g_pass_text.requestFocus();
    }

    private void set_Result(boolean result) {
        Log.d(TAG, "set_Result: result = " + result);
        if (result) {
            Intent intent = new Intent();
            intent.putExtra(UnlockChannelActivity.INTENT_KEY_PASS_TEXT, g_current_input_pass);
            intent.putExtra(UnlockChannelActivity.INTENT_KEY_PASS_RESULT, true);
            intent.putExtra(UnlockChannelActivity.INTENT_KEY_LOCK_TYPE, g_current_lock_type);
            setResult(-1, intent);
            finish();
        }
        else {
            g_wrong_text.setVisibility(View.VISIBLE);
            g_pass_text.setText("");
            g_current_input_pass = "";
            g_enter_text.setVisibility(View.VISIBLE);
        }
    }

    private boolean remove_character() {
        Log.d(TAG, "remove_character:");
        if (g_pass_text.getText().length() > 0) {
            g_wrong_text.setVisibility(View.GONE);
            g_pass_text.requestFocus();
            g_pass_text.setText(g_pass_text.getText().subSequence(0, g_pass_text.getText().length() - 1));
            String str = g_current_input_pass;
            g_current_input_pass = str.substring(0, str.length() - 1);
            if (TextUtils.isEmpty(g_pass_text.getText())) {
                g_enter_text.setVisibility(View.VISIBLE);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        switch (tvMessage.getMsgType()) {
            case -1:
                break;
        }
    }
}
