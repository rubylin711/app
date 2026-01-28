package com.prime.homeplus.settings.persional;

import android.app.ActionBar;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.prime.datastructure.sysdata.SystemInfo;
import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

public class PersonalUpdateView extends ThirdLevelView {
    private String TAG = "HomePlus-PersonalUpdateView";
    private SettingsRecyclerView settingsRecyclerView;
    private Context appContext;
    private TextView tvSoftwareVersion, tvMAC;

    public PersonalUpdateView(int i, Context context, SettingsRecyclerView settingsRecyclerView) {
        super(i, context, settingsRecyclerView);
        this.settingsRecyclerView = settingsRecyclerView;
        this.appContext = context;
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_update;
    }

    private Button btnUpdate;

    public void onFocus() {
        btnUpdate.requestFocus();
        SystemInfo systemInfo = PrimeUtils.get_system_info(getContext());
        tvMAC.setText(systemInfo.Ethernet_mac);
        tvSoftwareVersion.setText(systemInfo.SW_version);
    }

    @Override
    public void onViewCreated() {
        LogUtils.d(" ");
        initPopWindow();

        tvMAC = (TextView) findViewById(R.id.tvMAC);
        tvSoftwareVersion = (TextView) findViewById(R.id.tvSoftwareVersion);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);

        btnUpdate.setOnKeyListener(new OnKeyListener() {
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

        btnUpdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean hasVersion = true;
                popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
                currentState = hasVersion ? OtaState.VERSION_DETECT : OtaState.NO_VERSION;
                showState(currentState);
            }
        });

        tvMAC.setText("00:11:22:33:44:55");
        tvSoftwareVersion.setText("0.0.1");
    }

    @Override
    public void onViewResumed() {
        super.onViewResumed();
        SystemInfo systemInfo = PrimeUtils.get_system_info(getContext());
        if (systemInfo != null) {
            tvMAC.setText(systemInfo.Ethernet_mac);
            tvSoftwareVersion.setText(systemInfo.SW_version);
        } else {
            tvMAC.setText("00:11:22:33:44:55");
            tvSoftwareVersion.setText("0.0.1");
        }
    }

    private View popupView;
    private static PopupWindow popupWindow;
    private Button btnConfirm, btnCancel;
    private TextView textView_L3_1, textView_L3_2, textView_L4_1, textView_L4_2, textView_L5_1, textView_L5_2;
    private final static String FILE_VERSION = "0.0.1";
    private final static String NEW_FILE_VERSION = "0.0.2";
    private OtaState currentState = OtaState.VERSION_DETECT;

    private enum OtaState {
        NO_VERSION,
        VERSION_DETECT,
        DOWNLOADING,
        DOWNLOAD_FAILED,
        PROMPT_UPGRADE,
        FORCE_COUNTDOWN_TO_UPGRADE
    }

    private void initPopWindow() {
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update, null);
        popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT, true);

        textView_L3_1 = (TextView) popupView.findViewById(R.id.tvUpgradeStatus_L3_1);
        textView_L3_2 = (TextView) popupView.findViewById(R.id.tvUpgradeStatus_L3_2);
        textView_L4_1 = (TextView) popupView.findViewById(R.id.tvUpgradeStatus_L4_1);
        textView_L4_2 = (TextView) popupView.findViewById(R.id.tvUpgradeStatus_L4_2);
        textView_L5_1 = (TextView) popupView.findViewById(R.id.tvUpgradeStatus_L5_1);
        textView_L5_2 = (TextView) popupView.findViewById(R.id.tvUpgradeStatus_L5_2);

        btnConfirm = (Button) popupView.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentState == OtaState.VERSION_DETECT) {
                    boolean isCheckPassed = true;
                    if (isCheckPassed) {
                        showState(OtaState.DOWNLOADING);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                boolean isForceUpgrade = true;
                                if (isForceUpgrade) {
                                    showState(OtaState.FORCE_COUNTDOWN_TO_UPGRADE);
                                } else {
                                    showState(OtaState.PROMPT_UPGRADE);
                                }
                            }
                        }, 3 * 1000);
                    } else {
                        showState(OtaState.DOWNLOAD_FAILED);
                    }
                }
            }
        });

        btnCancel = (Button) popupView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void showState(OtaState state) {
        timer.cancel();
        currentState = state;

        switch (state) {
            case VERSION_DETECT:
                textView_L3_1.setText(getContext().getString(R.string.upgrade_status_is_upgrade));
                textView_L3_1.setVisibility(View.VISIBLE);
                textView_L3_2.setVisibility(View.GONE);

                textView_L4_1.setText(getContext().getString(R.string.upgrade_status_new_version_for_upgrade));
                textView_L4_1.setVisibility(View.VISIBLE);
                textView_L4_2.setVisibility(View.GONE);

                textView_L5_1.setText(getContext().getString(R.string.upgrade_status_new_version));
                textView_L5_1.setVisibility(View.VISIBLE);
                textView_L5_2.setText(NEW_FILE_VERSION);
                textView_L5_2.setVisibility(View.VISIBLE);

                btnConfirm.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
            case DOWNLOADING:
                textView_L3_1.setText(getContext().getString(R.string.upgrade_status_downloading));
                textView_L3_1.setVisibility(View.VISIBLE);
                textView_L3_2.setText("99 %");
                textView_L3_2.setVisibility(View.VISIBLE);

                textView_L4_1.setVisibility(View.GONE);
                textView_L4_2.setVisibility(View.GONE);

                textView_L5_1.setVisibility(View.GONE);
                textView_L5_2.setVisibility(View.GONE);

                btnConfirm.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                break;
            case PROMPT_UPGRADE:
                textView_L3_1.setText(getContext().getString(R.string.upgrade_status_upgrade_immediately));
                textView_L3_1.setVisibility(View.VISIBLE);
                textView_L3_2.setVisibility(View.GONE);

                textView_L4_1.setVisibility(View.GONE);
                textView_L4_2.setVisibility(View.GONE);

                textView_L5_1.setText(getContext().getString(R.string.upgrade_status_new_version));
                textView_L5_1.setVisibility(View.VISIBLE);
                textView_L5_2.setText(NEW_FILE_VERSION);
                textView_L5_2.setVisibility(View.VISIBLE);

                btnConfirm.setText(getContext().getString(R.string.upgrade_status_upgrade_confirm));
                btnConfirm.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
            case FORCE_COUNTDOWN_TO_UPGRADE:
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
                break;
            case NO_VERSION:
                textView_L3_1.setText(getContext().getString(R.string.upgrade_status_newest_version));
                textView_L3_1.setVisibility(View.VISIBLE);
                textView_L3_2.setVisibility(View.GONE);

                textView_L4_1.setText(getContext().getString(R.string.upgrade_status_current_version));
                textView_L4_1.setVisibility(View.VISIBLE);
                textView_L4_2.setText(FILE_VERSION);
                textView_L4_2.setVisibility(View.VISIBLE);

                textView_L5_1.setVisibility(View.GONE);
                textView_L5_2.setVisibility(View.GONE);

                btnConfirm.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.GONE);
                break;
            case DOWNLOAD_FAILED:
                boolean isNoFreeSpace = false;
                if (isNoFreeSpace) {
                    textView_L3_1.setText(getContext().getString(R.string.upgrade_status_no_free_space));
                } else {
                    textView_L3_1.setText(getContext().getString(R.string.upgrade_status_download_failed));
                }
                textView_L3_1.setVisibility(View.VISIBLE);
                textView_L3_2.setVisibility(View.GONE);

                textView_L4_1.setVisibility(View.GONE);
                textView_L4_2.setVisibility(View.GONE);

                textView_L5_1.setVisibility(View.GONE);
                textView_L5_2.setVisibility(View.GONE);

                btnConfirm.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                break;
        }
    }

    CountDownTimer timer = new CountDownTimer(15000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            textView_L4_2.setText(getContext().getString(R.string.upgrade_status_second,
                    millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        }
    };
}
