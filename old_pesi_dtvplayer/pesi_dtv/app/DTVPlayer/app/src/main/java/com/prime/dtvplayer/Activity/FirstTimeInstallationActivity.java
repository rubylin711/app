package com.prime.dtvplayer.Activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.prime.dtvplayer.MessageDialog;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.SelectBoxView;

public class FirstTimeInstallationActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    private ActivityTitleView title;
    private ActivityHelpView helpView;
    private Spinner SpinnerMenuLanguage;
    private Spinner SpinnerTvRatio;
    private Spinner SpinnerConversion;
    private Spinner SpinnerHdmiResolution;
    private SelectBoxView SelMenuLanguage;
    private SelectBoxView SelTvRatio;
    private SelectBoxView SelConversion;
    private SelectBoxView SelHdmiResolution;
    GposInfo mGpos;
    String orgStr;
    SpannableStringBuilder colorStr;
    private int mPreMenuLanguage=0;
    private int mPreTvRatio=0;
    private int mPreConversion=0;
    private int mPreHdmiResolution=0;
    private int mCurHdmiResolution=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_installation);

        InitTitleHelp();
        InitItems();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: keyCode = "+ keyCode);

        switch (keyCode) {
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
            {
                Log.d(TAG, "FirstTimeInstallationActivity: KeyEvent.KEYCODE_PROG_BLUE, CurTunerType="+GetCurTunerType());
                onProgBlueClicked();    // Johnny 20181228 for mouse control
            }break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setManuLanguageHelp()
    {
        orgStr = getString(R.string.STR_FIRST_TIME_INSTALLATION_HELP_INFO);

        // set COLOR to system settings message at bottom
//        colorStr = new SpannableStringBuilder(orgStr);
//        colorStr.setSpan(new ForegroundColorSpan(Color.YELLOW),
//                8, 8 + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        colorStr.setSpan(new ForegroundColorSpan(Color.YELLOW),
//                71, 71 + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        helpView.setHelpInfoTextBySplit(orgStr);
    }
    private void setHdmiResolutionHelp()
    {

    }
    private void setOtherHelp()
    {
        orgStr = getString(R.string.STR_OUTPUT_SETTINGS_HELP_INFO);

        // set COLOR to system settings message at bottom
//        colorStr = new SpannableStringBuilder(orgStr);
//        colorStr.setSpan(new ForegroundColorSpan(Color.YELLOW),
//                4, 4 + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        colorStr.setSpan(new ForegroundColorSpan(Color.YELLOW),
//                15, 15 + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        colorStr.setSpan(new ForegroundColorSpan(Color.YELLOW),
//                28, 28 + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        colorStr.setSpan(new ForegroundColorSpan(Color.YELLOW),
//                50, 50 + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        helpView.setHelpInfoTextBySplit( orgStr);
    }
    private void InitTitleHelp() {
        //Log.d(TAG, "InitTitleHelp: ");
        title = (ActivityTitleView) findViewById(R.id.easyInstallTitleLayout);
        title.setTitleView(getString(R.string.STR_FIRST_TIME_INSTALLATION_TITLE));
        helpView = (ActivityHelpView) findViewById(R.id.easyInstallHelpViewLayout);

        setManuLanguageHelp();

        helpView.resetHelp(1,R.drawable.help_blue,getResources().getString(R.string.STR_NEXT));
        helpView.resetHelp(2,0,null);
        helpView.resetHelp(3,0,null);
        helpView.resetHelp(4,0,null);

        helpView.setHelpIconClickListener(1, new View.OnClickListener() {   // Johnny 20181228 for mouse control
            @Override
            public void onClick(View view) {
                onProgBlueClicked();
            }
        });
    }



    private void InitItems() {
        //Log.d(TAG, "InitContent: ");

        //Spinners
        SpinnerMenuLanguage = (Spinner) findViewById(R.id.menuLanguageSPIN);
        SpinnerTvRatio = (Spinner) findViewById(R.id.tvRatioSPIN);
        SpinnerConversion = (Spinner) findViewById(R.id.conversionSPIN);
        SpinnerHdmiResolution = (Spinner) findViewById(R.id.hdmiResolutionSPIN);
        Resources res = getResources();
        SelMenuLanguage = new SelectBoxView(this, SpinnerMenuLanguage,
                res.getStringArray(R.array.STR_MENU_LANGUAGE_OPTION));
        SelTvRatio = new SelectBoxView(this, SpinnerTvRatio,
                res.getStringArray(R.array.STR_TV_RATIO_OPTION));
        SelConversion = new SelectBoxView(this, SpinnerConversion,
                res.getStringArray(R.array.STR_4X3CONVERSION_OPTION));
        SelHdmiResolution = new SelectBoxView(this, SpinnerHdmiResolution,
                res.getStringArray(R.array.STR_HDMI_RESOLUTION_OPTION));

        mGpos = GposInfoGet();
        Log.d(TAG, "InitItems: getOSDLanguage="+mGpos.getOSDLanguage());
        //Menu Language
        if(mGpos.getOSDLanguage().equals("eng")) {
            SpinnerMenuLanguage.setSelection(0);
            mPreMenuLanguage=0;
        }
        else if(mGpos.getOSDLanguage().equals("spa")) { //Spanish need fix
            SpinnerMenuLanguage.setSelection(1);
            mPreMenuLanguage=1;
        }else {
            SpinnerMenuLanguage.setSelection(0);
            mPreMenuLanguage=0;
        }
        SpinnerMenuLanguage.setOnFocusChangeListener(new SelectBoxView.SelectBoxtOnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                super.onFocusChange(v, hasFocus);
                if (SpinnerMenuLanguage.isFocused())
                    setManuLanguageHelp();//helpView.setHelpInfoText(null, getString(R.string.STR_FIRST_TIME_INSTALLATION_HELP_INFO));
                else
                    setOtherHelp();//helpView.setHelpInfoText(colorStr, orgStr);
            }
        });

        //TV Ratio
        Log.d(TAG, "InitItems: getScreen16x9="+mGpos.getScreen16x9());
        if (mGpos.getScreen16x9() == 0 ) {//4x3
            SpinnerTvRatio.setSelection(0);
            String[] conversion = getResources().getStringArray(R.array.STR_4X3CONVERSION_OPTION);
            SelConversion.SetNewAdapterItems(conversion);
            mPreTvRatio=0;
        }
        else if (mGpos.getScreen16x9() == 1 ) {//16x9
            SpinnerTvRatio.setSelection(1);
            String[] conversion = getResources().getStringArray(R.array.STR_CONVERSION_OPTION);
            SelConversion.SetNewAdapterItems(conversion);
            mPreTvRatio=0;
        }
        else{//set to 16x9
            SpinnerTvRatio.setSelection(1);
            String[] conversion = getResources().getStringArray(R.array.STR_CONVERSION_OPTION);
            SelConversion.SetNewAdapterItems(conversion);
            mGpos.setScreen16x9(1);
            mPreTvRatio=1;
        }

        //Conversion
        Log.d(TAG, "InitItems: getConversion="+mGpos.getConversion());
        if (mGpos.getConversion() == 0 ) {
            SpinnerConversion.setSelection(0);
            mPreConversion=0;
        }
        else if (mGpos.getConversion() == 1 ) {
            SpinnerConversion.setSelection(1);
            mPreConversion=1;
        }
        else if (mGpos.getConversion() == 2 ) {
            SpinnerConversion.setSelection(2);
            mPreConversion=2;
        }
        else{
            SpinnerConversion.setSelection(0);
            mGpos.setConversion(0);
            mPreConversion=0;
        }

        //Hdmi Resolution
        Log.d(TAG, "InitItems: getResolution="+mGpos.getResolution());
        if (mGpos.getResolution() == 0 ) {//480p
            SpinnerHdmiResolution.setSelection(0);
            mPreHdmiResolution=0;
        }
        else if (mGpos.getResolution() == 1 ) {//720p
            SpinnerHdmiResolution.setSelection(1);
            mPreHdmiResolution=1;
        }
        else if (mGpos.getResolution() == 2 ) {//1080i
            SpinnerHdmiResolution.setSelection(2);
            mPreHdmiResolution=2;
        }
        else if (mGpos.getResolution() == 3 ) {//1080p
            SpinnerHdmiResolution.setSelection(3);
            mPreHdmiResolution=3;
        }
        else{//set to 720p
            SpinnerHdmiResolution.setSelection(1);
            mGpos.setResolution(1);
            mPreHdmiResolution=1;
        }
        SpinnerHdmiResolution.setOnFocusChangeListener(new SelectBoxView.SelectBoxtOnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                super.onFocusChange(v, hasFocus);
                if (SpinnerHdmiResolution.isFocused())
                    helpView.setHelpInfoTextBySplit(getString(R.string.STR_HDMI_RESOLUTION_HELP_INFO));
                else
                    helpView.setHelpInfoTextBySplit(orgStr);
            }
        });

        //Menu Language's selectedListener
        SpinnerMenuLanguage.setOnItemSelectedListener(MenuLanguageListener);

        //Tv Ratio's selectedListener
        SpinnerTvRatio.setOnItemSelectedListener(TvRatioListener);

        //Conversion's selectedListener
        SpinnerConversion.setOnItemSelectedListener(ConversionListener);

        //Hdmi Resolution's selectedListener
        SpinnerHdmiResolution.setOnItemSelectedListener(HdmiResolutionListener);
    }

    private AdapterView.OnItemSelectedListener MenuLanguageListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            String str;
            //Log.d(TAG, "position =  " + position);
            if(mPreMenuLanguage != position) {
                if (position == 0) {//English
                    mGpos.setOSDLanguage("eng");
                } else if (position == 1) {//Spanish
                    mGpos.setOSDLanguage("spa");
                }
                Log.d(TAG, "onItemSelected:    position = " + position);
            }
            mPreMenuLanguage = position;

        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private AdapterView.OnItemSelectedListener TvRatioListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            String str;
            //Log.d(TAG, "position =  " + position);
            if(mPreTvRatio != position) {
                if (position == 0) {//4x3
                    mGpos.setScreen16x9(0);
                    String[] conversion = getResources().getStringArray(R.array.STR_4X3CONVERSION_OPTION);
                    SelConversion.SetNewAdapterItems(conversion);
                    SpinnerConversion.setOnItemSelectedListener(ConversionListener);
                } else if (position == 1) {////16x9
                    mGpos.setScreen16x9(1);
                    String[] conversion = getResources().getStringArray(R.array.STR_CONVERSION_OPTION);
                    SelConversion.SetNewAdapterItems(conversion);
                    SpinnerConversion.setOnItemSelectedListener(ConversionListener);
                }
                Log.d(TAG, "onItemSelected:    position = " + position);
            }
            mPreTvRatio = position;
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private AdapterView.OnItemSelectedListener ConversionListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            String str;
            //Log.d(TAG, "position =  " + position);
            if(mPreConversion != position) {
                if (position == 0) {
                    mGpos.setConversion(0);
                } else if (position == 1) {
                    mGpos.setConversion(1);
                } else if (position == 2) {
                    mGpos.setConversion(2);
                }
                Log.d(TAG, "onItemSelected:    position = " + position);
            }
            mPreConversion = position;
        }

        @Override
        public void onNothingSelected(AdapterView arg0) {
            Log.d(TAG, "onItemSelected:    onNothingSelected");
        }
    };

    private AdapterView.OnItemSelectedListener HdmiResolutionListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            String str;
            final int tmpPosition=0;
            //Log.d(TAG, "position =  " + position);
            mCurHdmiResolution = position;
            if(mPreHdmiResolution != position) {
                new MessageDialog(FirstTimeInstallationActivity.this, 10000) {
                    public void onSetMessage(View v) {
                        ((TextView) v).setText(getString(R.string.STR_PRESS_OK_IF_YOU_SEE_THIS_SCREEN));
                    }

                    public void onSetNegativeButton() {
                    }

                    public void onSetPositiveButton(int status) {
                        if (status == 1) {
                            //Log.d(TAG, "onSetPositiveButton:");
                            if (mCurHdmiResolution == 0) {//480p
                                mGpos.setResolution(0);
                            } else if (mCurHdmiResolution == 1) {//720p
                                mGpos.setResolution(1);
                            } else if (mCurHdmiResolution == 2) {//1080i
                                mGpos.setResolution(2);
                            } else if (mCurHdmiResolution == 3) {//1080p
                                mGpos.setResolution(3);
                            }
                            mPreHdmiResolution = mCurHdmiResolution;
                        }
                    }

                    public void dialogEnd(int status) {
                        if (status == 0) {
                            //Log.d(TAG, "dialogEnd: ");
                            SpinnerHdmiResolution.setSelection(mPreHdmiResolution);
                        }
                    }
                };
            }
            //Log.d(TAG, "DDD BK2: mPreHdmiResolution="+mPreHdmiResolution+", getResolution="+mGpos.getResolution());
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    // Johnny 20181228 for mouse control -s
    private void onProgBlueClicked() {
        Intent it = new Intent();
        if(GetCurTunerType() == TpInfo.DVBC) {
            it.setClass(FirstTimeInstallationActivity.this, ChannelSearchActivity.class);
        }
        else if(GetCurTunerType() == TpInfo.ISDBT) {
            it.setClass(FirstTimeInstallationActivity.this, ChannelSearchISDBTActivity.class);
        }
        else if(GetCurTunerType() == TpInfo.DVBT) {
            it.setClass(FirstTimeInstallationActivity.this, ChannelSearchDVBTActivity.class);
        }
        else if(GetCurTunerType() == TpInfo.DVBS) {
            it.setClass(FirstTimeInstallationActivity.this, ChannelSearchDVBSActivity.class);
        }
        finish();
        Bundle bundle = new Bundle();
        bundle.putString("parent", "EasyInstall");
        it.putExtras(bundle);
        startActivity(it);
    }
    // Johnny 20181228 for mouse control -e
}
