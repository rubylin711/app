package com.prime.homeplus.settings.system;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.prime.homeplus.settings.DebugOverlayService;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

public class DebugView extends ThirdLevelView {
    private String TAG = "HomePlus-DebugView";

    private static boolean mCPUMemoryInfoEnable = false;

    private SettingsRecyclerView settingsRecyclerView;


    public DebugView(int i, Context context, SettingsRecyclerView settingsRecyclerView) {
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

        tvSignal.setText(getContext().getString(R.string.settings_debug_configuration));
        tvSignalHint.setText(getContext().getString(R.string.settings_debug_description));

        btnSignal = (Button) findViewById(R.id.btnSignal);

        btnSignal.setText(getContext().getString(R.string.settings_debug_configuration));

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
                popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
                updateCPUMemory();
            }
        });
    }

    private View popupView;
    private static PopupWindow popupWindow;
    private RadioButton rbtn1, rbtn2;
    private Button btnDebug;
    private TextView tvSelected, tvDebug;
    private ImageView ivArrowLeft, ivArrowRight;
    private String[] debugLevelItems;

    private void initPopWindow() {
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_debug, null);
        popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        rbtn1 = (RadioButton) popupView.findViewById(R.id.rbtn1);
        rbtn2 = (RadioButton) popupView.findViewById(R.id.rbtn2);
        Button btnSave = (Button) popupView.findViewById(R.id.btnSave);
        Button btnCancel = (Button) popupView.findViewById(R.id.btnCancel);

        rbtn1.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        rbtn2.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        rbtn1.setOnClickListener((view) -> {
            rbtn1.setChecked(true);
            mCPUMemoryInfoEnable = true;
            rbtn2.setChecked(false);
        });
        rbtn2.setOnClickListener((view) -> {
            rbtn1.setChecked(false);
            mCPUMemoryInfoEnable = false;
            rbtn2.setChecked(true);
        });

        btnSave.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        btnDebug = (Button) popupView.findViewById(R.id.btnDebug);
        tvSelected = (TextView) popupView.findViewById(R.id.tvSelected);
        tvDebug = (TextView) popupView.findViewById(R.id.tvDebug);
        ivArrowLeft = (ImageView) popupView.findViewById(R.id.ivArrowLeft);
        ivArrowRight = (ImageView) popupView.findViewById(R.id.ivArrowRight);

        debugLevelItems = getResources().getStringArray(R.array.debug_level_list);

        btnDebug.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    tvSelected.setTypeface(null, Typeface.BOLD);
                    tvSelected.setTextSize(17);
                    tvSelected.setTextColor(getResources().getColor(R.color.white));
                    tvDebug.setTypeface(null, Typeface.BOLD);
                    tvDebug.setTextSize(17);
                    tvDebug.setTextColor(getResources().getColor(R.color.white));
                    ivArrowLeft.setImageResource(R.drawable.arrow_setting_spin_l_f);
                    ivArrowRight.setImageResource(R.drawable.arrow_setting_spin_r_f);
                } else {
                    tvSelected.setTypeface(null, Typeface.NORMAL);
                    tvSelected.setTextSize(16);
                    tvSelected.setTextColor(getResources().getColor(R.color.colorWhiteOpacity70));
                    tvDebug.setTypeface(null, Typeface.NORMAL);
                    tvDebug.setTextSize(16);
                    tvDebug.setTextColor(getResources().getColor(R.color.colorWhiteOpacity70));
                    ivArrowLeft.setImageResource(R.drawable.arrow_setting_spin_l_d);
                    ivArrowRight.setImageResource(R.drawable.arrow_setting_spin_r_d);
                }
            }
        });
        
        btnDebug.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        return true;
                    }
                }
                return false;
            }
        });

        btnDebug.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                // save debug level
            }
        });

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: mCPUMemoryInfoEnable = " + mCPUMemoryInfoEnable);
                Intent service = new Intent(getContext(), DebugOverlayService.class);
                if (mCPUMemoryInfoEnable) {
                    getContext().startForegroundService(service);
                }
                else {
                    getContext().stopService(service);
                }
                popupWindow.dismiss();
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }

    private void updateCPUMemory() {
        if (mCPUMemoryInfoEnable) {
            rbtn1.setChecked(true);
            rbtn2.setChecked(false);
        } else {
            rbtn1.setChecked(false);
            rbtn2.setChecked(true);
        }
    }
}
