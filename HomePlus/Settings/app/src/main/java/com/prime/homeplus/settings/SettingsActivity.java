package com.prime.homeplus.settings;


import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;

import androidx.constraintlayout.widget.ConstraintLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.settings.persional.PersonalUpdateView;
import com.prime.homeplus.settings.persional.ChannelScanView;
import com.prime.homeplus.settings.persional.LanguageView;
import com.prime.homeplus.settings.persional.MiniGuideDisplayTimeView;
import com.prime.homeplus.settings.persional.ParentalControlView;
import com.prime.homeplus.settings.persional.PurchasePinView;
import com.prime.homeplus.settings.persional.RemoteControlView;
import com.prime.homeplus.settings.persional.STBDataReturnView;
import com.prime.homeplus.settings.persional.TutorialSetting;
import com.prime.homeplus.settings.persional.SubtitleView;
import com.prime.homeplus.settings.persional.SystemInfoView;
import com.prime.homeplus.settings.persional.VideoAndAudioView;
import com.prime.homeplus.settings.system.ActivateQRView;
import com.prime.homeplus.settings.system.CAView;
import com.prime.homeplus.settings.system.DebugView;
import com.prime.homeplus.settings.system.FormatDiskView;
import com.prime.homeplus.settings.system.ReportView;
import com.prime.homeplus.settings.system.NetworkView;
import com.prime.homeplus.settings.system.ResetToDefaultView;
import com.prime.homeplus.settings.system.SignalView;
import com.prime.homeplus.settings.system.SystemUpdateView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettingsActivity extends Activity {
    public static String TAG = "HomePlus-SettingsActivity";

    private float density;
    private GposInfo mGpos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        density = getResources().getDisplayMetrics().density;

        // cmMode 1: disable cable mode, cmMode 0: enable cable mode
        int cmMode = 1;
        updateCableModeFlag((cmMode == 0) ? true : false);

        initLayout();
        initSettingsAndroid();
        initSettingsPersonal();
        initSettingsSystem();

        GposInfo gposInfo = PrimeApplication.get_prime_dtv_service().gpos_info_get();
        //LogUtils.d("[Ethan] gposInfo = "+gposInfo);
        if(gposInfo != null){
            //LogUtils.d("[Ethan] getWorkerPasswordValue = "+gposInfo.getWorkerPasswordValue());
            systemPassword = String.format("%08d", GposInfo.getWorkerPasswordValue(this.getApplicationContext()));
        }
        //LogUtils.d("[Ethan] systemPassword = "+systemPassword);
    }

    private ListView lvSettings;
    private TextView tvDateTime, tvSetting;
    private ConstraintLayout clList, clListTitle;

    private int listIndex = 0;
    private boolean isLockOnSettingsSystem = false;

    private void initLayout() {
        tvDateTime = (TextView) findViewById(R.id.tvDateTime);
        tvSetting = (TextView) findViewById(R.id.tvSetting);

        clList = (ConstraintLayout) findViewById(R.id.clList);
        clListTitle = (ConstraintLayout) findViewById(R.id.clListTitle);

        lvSettings = (ListView) findViewById(R.id.lvSettings);
        lvSettings.setAdapter(getSettingsListAdapter());

        lvSettings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // lunch android setting
                        LaunchAndroidSettings();
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }

            }
        });

        lvSettings.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listIndex = position;
                showSettingsRight(position);

                for (int i = 0; i < lvSettings.getCount(); i++) {
                    View viewItem = lvSettings.getChildAt(i);
                    TextView tv_item_name = viewItem.findViewById(R.id.item_name);
                    if (position == i) {
                        tv_item_name.setTypeface(Typeface.DEFAULT_BOLD);
                    } else {
                        tv_item_name.setTypeface(Typeface.DEFAULT);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tvAnimatorSet = new AnimatorSet();

        lvSettings.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                listViewFocus(hasFocus);
            }

        });
    }

    private void LaunchAndroidSettings() {
        Intent launchIntent  = new Intent();
        launchIntent.setComponent(new ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings"));
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getBaseContext().startActivity(launchIntent);
    }

    private ConstraintLayout settingsAndroid, settingsSystem;
    private ConstraintLayout settingsPersonal;

    private void initSettingsAndroid() {
        settingsAndroid = (ConstraintLayout) findViewById(R.id.settingsAndroid);
    }

    private SettingsRecyclerView recyclerViewPersonal;
    private TextView tvPersonIndex, tvPersonalTotal;
    private LinearLayout llPersonalIndex;

    private void initSettingsPersonal() {
        int personal_view_index_count = 0;
        String personalString[] = getResources().getStringArray(R.array.settings_personal);
        String personalSubtitleString[] = getResources().getStringArray(R.array.settings_personal_subtitle);

        settingsPersonal = (ConstraintLayout) findViewById(R.id.settingsPersonal);

        tvPersonIndex = (TextView) findViewById(R.id.tvPersonalIndex);
        tvPersonalTotal = (TextView) findViewById(R.id.tvPersonalTotal);

        llPersonalIndex = (LinearLayout) findViewById(R.id.llPersonalIndex);
        llPersonalIndex.setVisibility(View.GONE);

        recyclerViewPersonal = (SettingsRecyclerView) findViewById(R.id.settingsRecyclerView);

        recyclerViewPersonal.setBackToListListener(new BackToListListener() {
            @Override
            public void onBack() {
                goToList(1);
            }
        });

        recyclerViewPersonal.setFocusItemListener(new FocusItemListener() {
            @Override
            public void onFocus(int index) {
                String stIndex = "";
                if (index < 10) {
                    stIndex = "0" + index;
                } else {
                    stIndex = "" + index;
                }
                tvPersonIndex.setText(stIndex);
            }
        });

        ArrayList arrayList = new ArrayList(personalString.length);

        arrayList.add(new SettingsItemData(personalString[0], personalSubtitleString[0],
                "", new PersonalUpdateView(personal_view_index_count++, this, recyclerViewPersonal)));
        arrayList.add(new SettingsItemData(personalString[1], personalSubtitleString[1],
                "", new ParentalControlView(personal_view_index_count++, this, recyclerViewPersonal)));
        arrayList.add(new SettingsItemData(personalString[2], personalSubtitleString[2],
                "", new PurchasePinView(personal_view_index_count++, this, recyclerViewPersonal)));

        arrayList.add(new SettingsItemData(personalString[3], personalSubtitleString[3],
                Locale.getDefault().getDisplayLanguage(), new LanguageView(personal_view_index_count++, this, recyclerViewPersonal)));
        arrayList.add(new SettingsItemData(personalString[4], personalSubtitleString[4],
                "", new VideoAndAudioView(personal_view_index_count++, this, recyclerViewPersonal)));

        String nowSubtitle = "";
        String subtitle;

        if (nowSubtitle.equals("")) {
            subtitle = getBaseContext().getString(R.string.settings_av_setting_lang_chinese);
        } else if (nowSubtitle.equals("chi")) {
            subtitle = getBaseContext().getString(R.string.settings_av_setting_lang_chinese);
        } else {
            subtitle = getBaseContext().getString(R.string.settings_av_setting_lang_english);
        }
        arrayList.add(new SettingsItemData(personalString[5], personalSubtitleString[5],
                subtitle, new SubtitleView(personal_view_index_count++, this, recyclerViewPersonal)));
        String nowDisplayTime = "5";
        arrayList.add(new SettingsItemData(personalString[6], personalSubtitleString[6],
                nowDisplayTime + getResources().getString(R.string.unit_of_display_time),
                new MiniGuideDisplayTimeView(personal_view_index_count++, this, recyclerViewPersonal)));
        arrayList.add(new SettingsItemData(personalString[7], personalSubtitleString[7],
                "", new ChannelScanView(personal_view_index_count++, this, recyclerViewPersonal)));
        arrayList.add(new SettingsItemData(personalString[8], personalSubtitleString[8],
                "", new SystemInfoView(personal_view_index_count++, this, recyclerViewPersonal)));

        if (!mCableMode) {
            String returnData = "true";
            if (returnData.equals("true")) {
                returnData = getResources().getString(R.string.open);
            } else {
                returnData = getResources().getString(R.string.close);
            }
            arrayList.add(new SettingsItemData(personalString[9], personalSubtitleString[9],
                    returnData, new STBDataReturnView(personal_view_index_count++, this, recyclerViewPersonal)));
        }

        arrayList.add(new SettingsItemData(personalString[10], personalSubtitleString[10],
                "", new RemoteControlView(personal_view_index_count++, this, recyclerViewPersonal)));

        String enableTutorial = getResources().getString(R.string.open);
        boolean showTutorial = true;
        if (showTutorial) {
            enableTutorial = getResources().getString(R.string.open);
        } else {
            enableTutorial = getResources().getString(R.string.close);
        }

        arrayList.add(new SettingsItemData(personalString[11], personalSubtitleString[11],
                enableTutorial, new TutorialSetting(personal_view_index_count++, this, recyclerViewPersonal)));

        tvPersonalTotal.setText("02");

        recyclerViewPersonal.addAllItem(arrayList);
    }

    private void initPersionArrayList(){
        GposInfo gposInfo = PrimeApplication.get_prime_dtv_service().gpos_info_get();
        int personal_view_index_count = 0;
        String personalString[] = getResources().getStringArray(R.array.settings_personal);
        String personalSubtitleString[] = getResources().getStringArray(R.array.settings_personal_subtitle);

        ArrayList arrayList = new ArrayList(personalString.length);

        arrayList.add(new SettingsItemData(personalString[0], personalSubtitleString[0],
                "", new PersonalUpdateView(personal_view_index_count++, this, recyclerViewPersonal)));
        arrayList.add(new SettingsItemData(personalString[1], personalSubtitleString[1],
                "", new ParentalControlView(personal_view_index_count++, this, recyclerViewPersonal)));
        arrayList.add(new SettingsItemData(personalString[2], personalSubtitleString[2],
                "", new PurchasePinView(personal_view_index_count++, this, recyclerViewPersonal)));

        arrayList.add(new SettingsItemData(personalString[3], personalSubtitleString[3],
                Locale.getDefault().getDisplayLanguage(), new LanguageView(personal_view_index_count++, this, recyclerViewPersonal)));
        arrayList.add(new SettingsItemData(personalString[4], personalSubtitleString[4],
                "", new VideoAndAudioView(personal_view_index_count++, this, recyclerViewPersonal)));

        String nowSubtitle = "";
        String subtitle;
        if(gposInfo != null){
            nowSubtitle = GposInfo.getSubtitleLanguageSelection(getApplicationContext(), 0);
        }

        if (nowSubtitle.equals("")) {
            subtitle = getBaseContext().getString(R.string.settings_av_setting_lang_chinese);
        } else if (nowSubtitle.equals("chi")) {
            subtitle = getBaseContext().getString(R.string.settings_av_setting_lang_chinese);
        } else {
            subtitle = getBaseContext().getString(R.string.settings_av_setting_lang_english);
        }
        arrayList.add(new SettingsItemData(personalString[5], personalSubtitleString[5],
                subtitle, new SubtitleView(personal_view_index_count++, this, recyclerViewPersonal)));
        String nowDisplayTime = "5";
        if(gposInfo != null){
            nowDisplayTime = Integer.toString(GposInfo.getBannerTimeout(getApplicationContext()));
        }
        arrayList.add(new SettingsItemData(personalString[6], personalSubtitleString[6],
                nowDisplayTime + getResources().getString(R.string.unit_of_display_time),
                new MiniGuideDisplayTimeView(personal_view_index_count++, this, recyclerViewPersonal)));
        arrayList.add(new SettingsItemData(personalString[7], personalSubtitleString[7],
                "", new ChannelScanView(personal_view_index_count++, this, recyclerViewPersonal)));
        arrayList.add(new SettingsItemData(personalString[8], personalSubtitleString[8],
                "", new SystemInfoView(personal_view_index_count++, this, recyclerViewPersonal)));

        if (!mCableMode) {
            String returnData = "true";
            if(gposInfo != null){
                returnData = (GposInfo.getSTBDataReturn(getApplicationContext())== 1)?"true":"false";
            }
            if (returnData.equals("true")) {
                returnData = getResources().getString(R.string.open);
            } else {
                returnData = getResources().getString(R.string.close);
            }
            arrayList.add(new SettingsItemData(personalString[9], personalSubtitleString[9],
                    returnData, new STBDataReturnView(personal_view_index_count++, this, recyclerViewPersonal)));
        }

        arrayList.add(new SettingsItemData(personalString[10], personalSubtitleString[10],
                "", new RemoteControlView(personal_view_index_count++, this, recyclerViewPersonal)));

        String enableTutorial = getResources().getString(R.string.open);
        boolean showTutorial = true;
        if(gposInfo != null){
            showTutorial = (GposInfo.getTutorialSetting(getApplicationContext())==1)?true:false;
        }
        if (showTutorial) {
            enableTutorial = getResources().getString(R.string.open);
        } else {
            enableTutorial = getResources().getString(R.string.close);
        }

        arrayList.add(new SettingsItemData(personalString[11], personalSubtitleString[11],
                enableTutorial, new TutorialSetting(personal_view_index_count++, this, recyclerViewPersonal)));

        tvPersonalTotal.setText("02");

        recyclerViewPersonal.addAllItem(arrayList);
    }

    public void goToList(int index) {
        lvSettings.setFocusable(true);

        llPersonalIndex.setVisibility(View.GONE);

        recyclerViewPersonal.closeAllItem();
        recyclerViewPersonal.notifyDataSetChanged();

        llSystemIndex.setVisibility(View.GONE);


        //recyclerViewSystem.closeAllItem();
        initPersionArrayList();
        recyclerViewSystem.notifyDataSetChanged();

        lvSettings.requestFocus();
        lvSettings.setSelection(index);
    }

    public void goToView(int viewIndex) {
        lvSettings.setFocusable(false);

        switch (viewIndex) {
            case 0:
                break;
            case 1:
                recyclerViewPersonal.toView();

                llPersonalIndex.setVisibility(View.VISIBLE);
                break;
            case 2:
                if (llSystemPassword.getVisibility() == View.VISIBLE) {
                    etSystemPassword.requestFocus();
                } else {
                    llSystemIndex.setVisibility(View.VISIBLE);

                    recyclerViewSystem.toView();
                }
                break;
        }

    }

    private EditText etSystemPassword;
    private TextView tvPin1, tvPin2, tvPin3, tvPin4, tvPin5, tvPin6, tvPin7, tvPin8;
    private TextView tvEnterPinHint;
    private SettingsRecyclerView recyclerViewSystem;
    private LinearLayout llSystemPassword;

    private TextView tvSystemIndex, tvSystemTotal;
    private LinearLayout llSystemIndex;

    private void initSettingsSystem() {
        settingsSystem = (ConstraintLayout) findViewById(R.id.settingsSystem);

        tvPin1 = (TextView) findViewById(R.id.tvPin1);
        tvPin2 = (TextView) findViewById(R.id.tvPin2);
        tvPin3 = (TextView) findViewById(R.id.tvPin3);
        tvPin4 = (TextView) findViewById(R.id.tvPin4);
        tvPin5 = (TextView) findViewById(R.id.tvPin5);
        tvPin6 = (TextView) findViewById(R.id.tvPin6);
        tvPin7 = (TextView) findViewById(R.id.tvPin7);
        tvPin8 = (TextView) findViewById(R.id.tvPin8);

        tvSystemIndex = (TextView) findViewById(R.id.tvSystemIndex);
        tvSystemTotal = (TextView) findViewById(R.id.tvSystemTotal);
        llSystemIndex = (LinearLayout) findViewById(R.id.llSystemIndex);
        llSystemIndex.setVisibility(View.GONE);

        recyclerViewSystem = (SettingsRecyclerView) findViewById(R.id.systemRecyclerView);
        recyclerViewSystem.setFocusable(false);

        recyclerViewSystem.setVisibility(View.GONE);

        recyclerViewSystem.setFocusItemListener(new FocusItemListener() {
            @Override
            public void onFocus(int index) {
                String stIndex = "";
                if (index < 10) {
                    stIndex = "0" + index;
                } else {
                    stIndex = "" + index;
                }
                tvSystemIndex.setText(stIndex);
            }
        });

        llSystemPassword = (LinearLayout) findViewById(R.id.llSystemPassword);

        tvEnterPinHint = (TextView) findViewById(R.id.tvEnterPinHint);


        etSystemPassword = (EditText) findViewById(R.id.etSystemPassword);

        etSystemPassword.setInputType(InputType.TYPE_NULL);

        etSystemPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        //settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        //settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etSystemPassword.getText().toString().equals("")) {
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Instrumentation inst = new Instrumentation();
                                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                                    } catch (Exception e) {
                                        Log.d(TAG, "" + e);
                                    }
                                }
                            }.start();
                            return true;
                        } else {
                            if (isLockOnSettingsSystem == true) {
                                Log.d(TAG, "isLockOnSettingsSystem");
                                return true;
                            }
                            goToList(2);
                        }
                    }
                }
                return false;
            }
        });

        recyclerViewSystem.setBackToListListener(new BackToListListener() {
            @Override
            public void onBack() {
                if (isLockOnSettingsSystem == true) {
                    Log.d(TAG, "isLockOnSettingsSystem");
                    return;
                }
                goToList(2);
            }
        });

        String systemString[] = getResources().getStringArray(R.array.settings_system);
        String systemSubtitleString[] = getResources().getStringArray(R.array.settings_system_subtitle);


        ArrayList arrayList = new ArrayList(systemString.length);

        arrayList.add(new SettingsItemData(systemString[0], systemSubtitleString[0],
                "", new SystemUpdateView(0, this, recyclerViewSystem)));
        arrayList.add(new SettingsItemData(systemString[1], systemSubtitleString[1],
                "", new com.prime.homeplus.settings.system.ChannelScanView(1, this, recyclerViewSystem)));
        arrayList.add(new SettingsItemData(systemString[2], systemSubtitleString[2],
                "", new SignalView(2, this, recyclerViewSystem)));
        arrayList.add(new SettingsItemData(systemString[3], systemSubtitleString[3],
                "", new NetworkView(3, this, recyclerViewSystem)));
        arrayList.add(new SettingsItemData(systemString[4], systemSubtitleString[4],
                "", new CAView(4, this, recyclerViewSystem)));
        arrayList.add(new SettingsItemData(systemString[5], systemSubtitleString[5],
                "", new ResetToDefaultView(5, this, recyclerViewSystem)));
        arrayList.add(new SettingsItemData(systemString[6], systemSubtitleString[6],
                "", new ReportView(6, this, recyclerViewSystem)));
        arrayList.add(new SettingsItemData(systemString[7], systemSubtitleString[7],
                "", new ActivateQRView(7, this, recyclerViewSystem)));
        arrayList.add(new SettingsItemData(systemString[8], systemSubtitleString[8],
                "", new DebugView(8, this, recyclerViewSystem)));
        arrayList.add(new SettingsItemData(systemString[9], systemSubtitleString[9],
                "", new FormatDiskView(9, this, recyclerViewSystem)));

        recyclerViewSystem.addAllItem(arrayList);

        tvSystemTotal.setText("02");

        etSystemPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {

                } else {
                    etSystemPassword.setText("");
                }
            }
        });

        etSystemPassword.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {
                lightPassword(etSystemPassword.getText().length());

                if (etSystemPassword.getText().length() == 8) {
                    if (etSystemPassword.getText().toString().equals(systemPassword)) {
                        recyclerViewSystem.setVisibility(View.VISIBLE);
                        recyclerViewSystem.setFocusable(true);
                        recyclerViewSystem.toView();
                        llSystemPassword.setVisibility(View.GONE);

                        llSystemIndex.setVisibility(View.VISIBLE);
                    } else {
                        tvEnterPinHint.setText(getString(R.string.settings_system_pinwrong));
                        tvEnterPinHint.setTextColor(getResources().getColor(R.color.colorWarning));
                        etSystemPassword.setText("");
                    }
                } else if (etSystemPassword.getText().length() == 1) {
                    tvEnterPinHint.setText(getString(R.string.settings_system_pindel));
                    tvEnterPinHint.setTextColor(getResources().getColor(R.color.colorWhiteOpacity40));
                }
            }
        });

        etSystemPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etSystemPassword.getText().toString().equals("")) {
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Instrumentation inst = new Instrumentation();
                                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                                    } catch (Exception e) {
                                        Log.d(TAG, "" + e);
                                    }
                                }
                            }.start();
                            return true;
                        } else {
                            if (isLockOnSettingsSystem == true) {
                                Log.d(TAG, "isLockOnSettingsSystem");
                                return true;
                            }
                            goToList(2);
                        }
                    }
                }
                return false;
            }
        });
    }

    private String systemPassword = "13197776";

    private void lightPassword(int size) {
        switch (size) {
            case 0:
                setPasswordOn(tvPin1, false);
                setPasswordOn(tvPin2, false);
                setPasswordOn(tvPin3, false);
                setPasswordOn(tvPin4, false);
                setPasswordOn(tvPin5, false);
                setPasswordOn(tvPin6, false);
                setPasswordOn(tvPin7, false);
                setPasswordOn(tvPin8, false);
                break;
            case 1:
                setPasswordOn(tvPin1, true);
                setPasswordOn(tvPin2, false);
                setPasswordOn(tvPin3, false);
                setPasswordOn(tvPin4, false);
                setPasswordOn(tvPin5, false);
                setPasswordOn(tvPin6, false);
                setPasswordOn(tvPin7, false);
                setPasswordOn(tvPin8, false);
                break;
            case 2:
                setPasswordOn(tvPin1, true);
                setPasswordOn(tvPin2, true);
                setPasswordOn(tvPin3, false);
                setPasswordOn(tvPin4, false);
                setPasswordOn(tvPin5, false);
                setPasswordOn(tvPin6, false);
                setPasswordOn(tvPin7, false);
                setPasswordOn(tvPin8, false);
                break;
            case 3:
                setPasswordOn(tvPin1, true);
                setPasswordOn(tvPin2, true);
                setPasswordOn(tvPin3, true);
                setPasswordOn(tvPin4, false);
                setPasswordOn(tvPin5, false);
                setPasswordOn(tvPin6, false);
                setPasswordOn(tvPin7, false);
                setPasswordOn(tvPin8, false);
                break;
            case 4:
                setPasswordOn(tvPin1, true);
                setPasswordOn(tvPin2, true);
                setPasswordOn(tvPin3, true);
                setPasswordOn(tvPin4, true);
                setPasswordOn(tvPin5, false);
                setPasswordOn(tvPin6, false);
                setPasswordOn(tvPin7, false);
                setPasswordOn(tvPin8, false);
                break;
            case 5:
                setPasswordOn(tvPin1, true);
                setPasswordOn(tvPin2, true);
                setPasswordOn(tvPin3, true);
                setPasswordOn(tvPin4, true);
                setPasswordOn(tvPin5, true);
                setPasswordOn(tvPin6, false);
                setPasswordOn(tvPin7, false);
                setPasswordOn(tvPin8, false);
                break;
            case 6:
                setPasswordOn(tvPin1, true);
                setPasswordOn(tvPin2, true);
                setPasswordOn(tvPin3, true);
                setPasswordOn(tvPin4, true);
                setPasswordOn(tvPin5, true);
                setPasswordOn(tvPin6, true);
                setPasswordOn(tvPin7, false);
                setPasswordOn(tvPin8, false);
                break;
            case 7:
                setPasswordOn(tvPin1, true);
                setPasswordOn(tvPin2, true);
                setPasswordOn(tvPin3, true);
                setPasswordOn(tvPin4, true);
                setPasswordOn(tvPin5, true);
                setPasswordOn(tvPin6, true);
                setPasswordOn(tvPin7, true);
                setPasswordOn(tvPin8, false);
                break;
            case 8:
                setPasswordOn(tvPin1, true);
                setPasswordOn(tvPin2, true);
                setPasswordOn(tvPin3, true);
                setPasswordOn(tvPin4, true);
                setPasswordOn(tvPin5, true);
                setPasswordOn(tvPin6, true);
                setPasswordOn(tvPin7, true);
                setPasswordOn(tvPin8, true);
                break;
        }
    }

    private void setPasswordOn(TextView tv, boolean on) {
        if (on) {
            tv.setBackground(getResources().getDrawable(R.drawable.password_on));
        } else {
            tv.setBackground(getResources().getDrawable(R.drawable.password_off));
        }
    }

    private void showSettingsRight(int index) {
        switch (index) {
            case 0:
                settingsAndroid.setVisibility(View.VISIBLE);
                settingsPersonal.setVisibility(View.INVISIBLE);
                settingsSystem.setVisibility(View.INVISIBLE);
                break;
            case 1:
                settingsAndroid.setVisibility(View.INVISIBLE);
                settingsPersonal.setVisibility(View.VISIBLE);
                settingsSystem.setVisibility(View.INVISIBLE);
                break;
            case 2:
                settingsAndroid.setVisibility(View.INVISIBLE);
                settingsPersonal.setVisibility(View.INVISIBLE);
                settingsSystem.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void listViewFocus(boolean isFocus) {


        if (isFocus) {
            clListTitle.setVisibility(View.VISIBLE);

            animateTvInput((int) (250 * density), 300);
        } else {
            clListTitle.setVisibility(View.INVISIBLE);

            animateTvInput((int) (55 * density), 300);

            goToView(listIndex);
        }

        setListSpace(isFocus);

        for (int i = 0; i < lvSettings.getCount(); i++) {
            View view = lvSettings.getChildAt(i);

            if (null != view) {
                TextView tvName = (TextView) view.findViewById(R.id.item_name);

                if (isFocus) {
                    tvName.setVisibility(View.VISIBLE);
                } else {
                    tvName.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private AnimatorSet tvAnimatorSet;

    private void animateTvInput(int toWidth, int duration) {
        ValueAnimator anim = ValueAnimator.ofInt(clList.getMeasuredWidth(), toWidth);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = clList.getLayoutParams();
                layoutParams.width = val;
                clList.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(duration);
        anim.start();
    }

    private void setListSpace(boolean isFocus) {
        ValueAnimator anim;

        if (isFocus) {
            anim = ValueAnimator.ofInt((int) (205 * density), (int) (250 * density));
        } else {
            anim = ValueAnimator.ofInt((int) (250 * density), (int) (205 * density));
        }

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = lvSettings.getLayoutParams();
                layoutParams.width = val;
                lvSettings.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(300);
        anim.start();
    }

    private Handler handler = new Handler();
    private Runnable updateTimer = new Runnable() {
        public void run() {
            String time = new SimpleDateFormat("MM.dd (E) HH:mm").format(new Date());
            tvDateTime.setText(time);

            handler.postDelayed(this, 10000);
        }
    };

    private SimpleAdapter getSettingsListAdapter() {
        return new Adapter(this, getSettingsLisData(), R.layout.settings_list_item,
                new String[]{"item_rating", "item_name", "item_date", "item_time"}, new int[]{R.id.item_icon, R.id.item_name});
    }

    private List<Map<String, Object>> getSettingsLisData() {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();

        map = new HashMap<String, Object>();
        map.put("item_rating", R.drawable.selector_icon_set_list_android);
        map.put("item_name", getString(R.string.settings_android));
        mList.add(map);
        map = new HashMap<String, Object>();
        map.put("item_rating", R.drawable.selector_icon_set_list_personal);
        map.put("item_name", getString(R.string.settings_personal));
        mList.add(map);
        map = new HashMap<String, Object>();
        map.put("item_rating", R.drawable.selector_icon_set_list_system);
        map.put("item_name", getString(R.string.settings_system));
        mList.add(map);

        return mList;
    }

    public class Adapter extends SimpleAdapter {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();

        public Adapter(Context context, List<? extends Map<String, Object>> data, int resource, String[] from, int[] to) {

            super(context, data, resource, from, to);
            mList = (List<Map<String, Object>>) data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            return view;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "keyCode " + keyCode);

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume() {
        String action;
        Log.d(TAG, "onResume");
        super.onResume();

        initPersionArrayList();

        handler.postDelayed(updateTimer, 500);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null && (action = bundle.getString("Action")) != null) {
            if (action.equals("FeeReminder")) {
                Log.d(TAG, "From FeeReminder");
                enterSettingsSystem();
                this.isLockOnSettingsSystem = true;
                return;
            } else {
                if (action.equals("TutorialSetting")) {
                    Log.d(TAG, "show TutorialSetting");
                    enterTutorialSetting();
                    return;
                }
                intent.removeExtra("Action");
            }
        }

        isLockOnSettingsSystem = false;

        int mItemIndex = recyclerViewPersonal.getViewIndex();
        recyclerViewPersonal.viewResumed(mItemIndex);
        mItemIndex = recyclerViewSystem.getViewIndex();
                                        recyclerViewSystem.viewResumed(mItemIndex);
    }


    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        int mItemIndex = recyclerViewPersonal.getViewIndex();
        recyclerViewPersonal.viewResumed(mItemIndex);
        mItemIndex = recyclerViewSystem.getViewIndex();
        recyclerViewSystem.viewPaused(mItemIndex);

        handler.removeCallbacks(updateTimer);
    }

    private boolean mCableMode = false;

    private void updateCableModeFlag(boolean flag) {
        mCableMode = flag;

        if (recyclerViewPersonal != null) {
            recyclerViewPersonal.setCableMode(flag);
        }

        if (recyclerViewSystem != null) {
            recyclerViewSystem.setCableMode(flag);
        }
    }

    private void enterTutorialSetting() {
        this.handler.postDelayed(new Runnable() { // from class: com.hwacom.dtv.settings.SettingsActivity.15
            @Override // java.lang.Runnable
            public void run() throws Resources.NotFoundException {
                SettingsActivity.this.lvSettings.setSelection(1);
                SettingsActivity.this.listIndex = 1;
                SettingsActivity.this.showSettingsRight(1);
                SettingsActivity.this.clListTitle.setVisibility(4);
                SettingsActivity.this.animateTvInput((int) (55.0f * SettingsActivity.this.density), 0);
                SettingsActivity.this.goToView(SettingsActivity.this.listIndex);
                ValueAnimator anim = ValueAnimator.ofInt((int) (250.0f * SettingsActivity.this.density), (int) (205.0f * SettingsActivity.this.density));
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.hwacom.dtv.settings.SettingsActivity.15.1
                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                        ViewGroup.LayoutParams layoutParams = SettingsActivity.this.lvSettings.getLayoutParams();
                        layoutParams.width = val;
                        SettingsActivity.this.lvSettings.setLayoutParams(layoutParams);
                    }
                });
                anim.setDuration(0L);
                anim.start();
                for (int i = 0; i < SettingsActivity.this.lvSettings.getCount(); i++) {
                    View view = SettingsActivity.this.lvSettings.getChildAt(i);
                    if (view != null) {
                        TextView tvName = (TextView) view.findViewById(R.id.item_name);
                        tvName.setVisibility(4);
                    }
                }
                String[] personalString = SettingsActivity.this.getResources().getStringArray(R.array.settings_personal);
                SettingsActivity.this.recyclerViewPersonal.focusItemByName(personalString[11]);
                int mItemIndex = SettingsActivity.this.recyclerViewPersonal.getViewIndex();
                SettingsActivity.this.recyclerViewPersonal.handleAction(mItemIndex, "DisableTutorial");
            }
        }, 0L);
    }

    private void enterSettingsSystem() {
        this.handler.postDelayed(new Runnable() { // from class: com.hwacom.dtv.settings.SettingsActivity.16
            @Override // java.lang.Runnable
            public void run() {
                SettingsActivity.this.lvSettings.setSelection(2);
                SettingsActivity.this.listIndex = 2;
                SettingsActivity.this.showSettingsRight(2);
                SettingsActivity.this.clListTitle.setVisibility(4);
                SettingsActivity.this.animateTvInput((int) (55.0f * SettingsActivity.this.density), 0);
                SettingsActivity.this.goToView(SettingsActivity.this.listIndex);
                ValueAnimator anim = ValueAnimator.ofInt((int) (250.0f * SettingsActivity.this.density), (int) (205.0f * SettingsActivity.this.density));
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.hwacom.dtv.settings.SettingsActivity.16.1
                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                        ViewGroup.LayoutParams layoutParams = SettingsActivity.this.lvSettings.getLayoutParams();
                        layoutParams.width = val;
                        SettingsActivity.this.lvSettings.setLayoutParams(layoutParams);
                    }
                });
                anim.setDuration(0L);
                anim.start();
                for (int i = 0; i < SettingsActivity.this.lvSettings.getCount(); i++) {
                    View view = SettingsActivity.this.lvSettings.getChildAt(i);
                    if (view != null) {
                        TextView tvName = (TextView) view.findViewById(R.id.item_name);
                        tvName.setVisibility(4);
                    }
                }
                if (SettingsActivity.this.llSystemPassword.getVisibility() == 0) {
                    SettingsActivity.this.etSystemPassword.requestFocus();
                } else {
                    SettingsActivity.this.llSystemIndex.setVisibility(0);
                    SettingsActivity.this.recyclerViewSystem.toView();
                }
            }
        }, 0L);
    }

}
