package com.prime.homeplus.settings.system;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;
import com.prime.homeplus.settings.sms.SMSActivity;

public class ReportView extends ThirdLevelView {
    private String TAG = "HomePlus-ReportView";
    private SettingsRecyclerView settingsRecyclerView;


    public ReportView(int i, Context context, SettingsRecyclerView settingsRecyclerView) {
        super(i, context, settingsRecyclerView);
        this.settingsRecyclerView = settingsRecyclerView;
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_signal;
    }

    private Button btnSignal;
    private TextView tvSignal, tvSignalHint;


    public void onFocus() {
        btnSignal.requestFocus();
    }

    @Override
    public void onViewCreated() {
        initPopWindow();

        tvSignal = (TextView) findViewById(R.id.tvSignal);
        tvSignalHint = (TextView) findViewById(R.id.tvSignalHint);

        tvSignal.setText(getContext().getString(R.string.settings_report_smsreport));
        tvSignalHint.setText(getContext().getString(R.string.settings_report_description));

        btnSignal = (Button) findViewById(R.id.btnSignal);

        btnSignal.setText(getContext().getString(R.string.settings_report_smsreport));

        btnSignal.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        settingsRecyclerView.backToList();
                    }
                }
                return false;
            }
        });

        btnSignal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // UI shell
                //popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);

                // internal
                Intent intent = new Intent(getContext(), SMSActivity.class);
                getContext().startActivity(intent);

                // external
                //ActivityUtils.myStartActivity(getContext(), "com.rtk.partnerconfig/.SMSActivity", null);
            }
        });
    }

    private View popupView;
    private static PopupWindow popupWindow;

    private void initPopWindow() {
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report, null);
        popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
    }
}
