package com.prime.homeplus.settings.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;

public class OTADialog extends Dialog {
    private static final String TAG = "OTADialog";

    int COUNTDOWN = 1, NORMAL = 2;
    int g_show_status = NORMAL;
    String g_new_version;

    public OTADialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_update);
    }

    @Override
    protected void onStart() {
        super.onStart();

        TextView textView_L3_1 = findViewById(R.id.tvUpgradeStatus_L3_1);
        TextView textView_L3_2 = findViewById(R.id.tvUpgradeStatus_L3_2);
        TextView textView_L4_1 = findViewById(R.id.tvUpgradeStatus_L4_1);
        TextView textView_L4_2 = findViewById(R.id.tvUpgradeStatus_L4_2);
        TextView textView_L5_1 = findViewById(R.id.tvUpgradeStatus_L5_1);
        TextView textView_L5_2 = findViewById(R.id.tvUpgradeStatus_L5_2);
        Button btnConfirm = findViewById(R.id.btnConfirm);
        Button btnCancel = findViewById(R.id.btnCancel);

        if(g_show_status == COUNTDOWN) {
            textView_L3_1.setText(getContext().getString(R.string.upgrade_status_download_finish));
            textView_L3_1.setVisibility(View.VISIBLE);
            textView_L3_2.setVisibility(View.GONE);

            textView_L4_1.setText(getContext().getString(R.string.upgrade_status_reboot));
            textView_L4_1.setVisibility(View.VISIBLE);
            textView_L4_2.setText(getContext().getString(R.string.upgrade_status_second, 15));
            textView_L4_2.setVisibility(View.VISIBLE);

            textView_L5_1.setVisibility(View.GONE);
            textView_L5_2.setVisibility(View.GONE);

            btnConfirm.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            timer.start();
        }
        else if (g_show_status == NORMAL) {
            textView_L3_1.setText(getContext().getString(R.string.upgrade_status_is_upgrade));
            textView_L3_1.setVisibility(View.VISIBLE);
            textView_L3_2.setVisibility(View.GONE);

            textView_L4_1.setText(getContext().getString(R.string.upgrade_status_new_version_for_upgrade));
            textView_L4_1.setVisibility(View.VISIBLE);
            textView_L4_2.setVisibility(View.GONE);

            textView_L5_1.setText(getContext().getString(R.string.upgrade_status_new_version));
            textView_L5_1.setVisibility(View.VISIBLE);
            textView_L5_2.setText(g_new_version);
            textView_L5_2.setVisibility(View.VISIBLE);

            btnConfirm.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);

            btnConfirm.setOnClickListener((view) -> {
                Log.d(TAG, "onStart: start ota update");
                PrimeUtils.start_ota_update();
                dismiss();
            });
            btnCancel.setOnClickListener((view) -> {
                Log.d(TAG, "onStart: cancel ota update");
                dismiss();
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            on_click_back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void on_click_back() {
        dismiss();
    }

    public void show_count_down_dialog() {
        g_show_status = COUNTDOWN;
        show();
    }

    public void show_update_dialog(String newVersion) {
        g_show_status = NORMAL;
        g_new_version = newVersion;
        show();
    }

    CountDownTimer timer = new CountDownTimer(15000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            TextView textView_L4_2 = findViewById(R.id.tvUpgradeStatus_L4_2);
            textView_L4_2.setText(getContext().getString(R.string.upgrade_status_second,
                    millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            PrimeUtils.start_ota_update();
            dismiss();
        }
    };
}
