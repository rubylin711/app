package com.prime.homeplus.settings.persional;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;

import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.PrimeApplication;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsItemData;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

public class MiniGuideDisplayTimeView extends ThirdLevelView {
    private String TAG = "HomePlus-MiniGuideDisplayTimeView";

    private SettingsRecyclerView secondDepthView;
    private static final int DEFAULT_MINIEPG_DISPLAY_TIME_SEC = 5;

    public MiniGuideDisplayTimeView(int i, Context context, SettingsRecyclerView secondDepthView) {
        super(i, context, secondDepthView);

        this.secondDepthView = secondDepthView;
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_mini_guide;
    }

    private RadioButton rbtn1, rbtn2, rbtn3, rbtn4, rbtn5;

    public void onFocus() {
        getDisplayTime();
        //rbtn1.requestFocus();
    }

    private String[] displayTimeItems = {"3", "5", "10", "15", "20"};

    @Override
    public void onViewCreated() {
        rbtn1 = (RadioButton) findViewById(R.id.rbtn1);
        rbtn2 = (RadioButton) findViewById(R.id.rbtn2);
        rbtn3 = (RadioButton) findViewById(R.id.rbtn3);
        rbtn4 = (RadioButton) findViewById(R.id.rbtn4);
        rbtn5 = (RadioButton) findViewById(R.id.rbtn5);

        displayTimeItems = getResources().getStringArray(R.array.display_time_list);

        rbtn1.setOnFocusChangeListener(OnFocusChangeListener);
        rbtn2.setOnFocusChangeListener(OnFocusChangeListener);
        rbtn3.setOnFocusChangeListener(OnFocusChangeListener);
        rbtn4.setOnFocusChangeListener(OnFocusChangeListener);
        rbtn5.setOnFocusChangeListener(OnFocusChangeListener);

        rbtn1.setText(displayTimeItems[0] + getResources().getString(R.string.unit_of_display_time));
        rbtn2.setText(displayTimeItems[1] + getResources().getString(R.string.unit_of_display_time));
        rbtn3.setText(displayTimeItems[2] + getResources().getString(R.string.unit_of_display_time));
        rbtn4.setText(displayTimeItems[3] + getResources().getString(R.string.unit_of_display_time));
        rbtn5.setText(displayTimeItems[4] + getResources().getString(R.string.unit_of_display_time));

        rbtn1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
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

        rbtn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkSecButton(0);
                saveDisplayTime();
            }
        });

        rbtn2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

                    }
                }
                return false;
            }
        });

        rbtn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkSecButton(1);
                saveDisplayTime();
            }
        });

        rbtn3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

                    }
                }
                return false;
            }
        });

        rbtn3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkSecButton(2);
                saveDisplayTime();
            }
        });

        rbtn4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

                    }
                }
                return false;
            }
        });

        rbtn4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkSecButton(3);
                saveDisplayTime();
            }
        });

        rbtn5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

                    }
                }
                return false;
            }
        });

        rbtn5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkSecButton(4);
                saveDisplayTime();
            }
        });
    }

    private int displayTimeIndex = 0;

    private void getDisplayTime() {
        int bannaerTimeout = GposInfo.getBannerTimeout(getContext());
        String nowDisplayTime = String.valueOf(bannaerTimeout);
        LogUtils.d("bannaerTimeout = "+bannaerTimeout);

        if (nowDisplayTime.equals("") || nowDisplayTime.equals(displayTimeItems[0])) {
            displayTimeIndex = 0;
        } else if (nowDisplayTime.equals(displayTimeItems[1])) {
            displayTimeIndex = 1;
        } else if (nowDisplayTime.equals(displayTimeItems[2])) {
            displayTimeIndex = 2;
        } else if (nowDisplayTime.equals(displayTimeItems[3])) {
            displayTimeIndex = 3;
        } else if (nowDisplayTime.equals(displayTimeItems[4])) {
            displayTimeIndex = 4;
        }

        checkSecButton(displayTimeIndex);
    }

    private void checkSecButton(int index) {
        displayTimeIndex = index;
        switch (displayTimeIndex) {
            case 0:
                rbtn1.setChecked(true);
                rbtn2.setChecked(false);
                rbtn3.setChecked(false);
                rbtn4.setChecked(false);
                rbtn5.setChecked(false);

                rbtn1.requestFocus();
                break;
            case 1:
                rbtn1.setChecked(false);
                rbtn2.setChecked(true);
                rbtn3.setChecked(false);
                rbtn4.setChecked(false);
                rbtn5.setChecked(false);

                rbtn2.requestFocus();
                break;
            case 2:
                rbtn1.setChecked(false);
                rbtn2.setChecked(false);
                rbtn3.setChecked(true);
                rbtn4.setChecked(false);
                rbtn5.setChecked(false);

                rbtn3.requestFocus();
                break;
            case 3:
                rbtn1.setChecked(false);
                rbtn2.setChecked(false);
                rbtn3.setChecked(false);
                rbtn4.setChecked(true);
                rbtn5.setChecked(false);

                rbtn4.requestFocus();
                break;
            case 4:
                rbtn1.setChecked(false);
                rbtn2.setChecked(false);
                rbtn3.setChecked(false);
                rbtn4.setChecked(false);
                rbtn5.setChecked(true);

                rbtn5.requestFocus();
                break;
        }
    }

    private void saveDisplayTime() {
        int bannerTimeout = Integer.valueOf(displayTimeItems[displayTimeIndex]);
        GposInfo.setBannerTimeout(getContext(), bannerTimeout);
        //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_BANNER_TIMEOUT, bannerTimeout);

        SettingsItemData settingsItems = secondDepthView.settingsItems.get(secondDepthView.getViewIndex());
        settingsItems.setValue(displayTimeItems[displayTimeIndex] + getResources().getString(R.string.unit_of_display_time));

        secondDepthView.notifyDataSetChanged();

        rbtn1.setFocusable(false);
        rbtn2.setFocusable(false);
        rbtn3.setFocusable(false);
        rbtn4.setFocusable(false);
        rbtn5.setFocusable(false);

        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rbtn1.setFocusable(true);
                rbtn2.setFocusable(true);
                rbtn3.setFocusable(true);
                rbtn4.setFocusable(true);
                rbtn5.setFocusable(true);


                getDisplayTime();
            }
        },300);
    }
}
