package com.prime.dtvplayer.Activity;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.hisilicon.android.hidisplaymanager.DispFmt;
import com.hisilicon.android.hidisplaymanager.HiDisplayManager;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.SureDialog;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.ResolutionInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.SelectBoxView;

import java.util.ArrayList;

public class OutputSettingsActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    private ActivityTitleView title;
    private ActivityHelpView help;
    private Spinner tvRatio;
    private Spinner conversion;
    private Spinner conversion4X3;//eric lin 20180627 add 4:3 letter box UI
    private Spinner audoutput;
    private Spinner hdmiResolution;
    private Spinner autoStandby;//Scoty 20181129 add Auto Standby item
    private SelectBoxView selTvRatio;
    private SelectBoxView selConversion;
    private SelectBoxView selConversion4X3;//eric lin 20180627 add 4:3 letter box UI
    private SelectBoxView selAudioOutput;
    private SelectBoxView selHDMIResolution;
    private SelectBoxView selAutoStandby;//Scoty 20181129 add Auto Standby item
    String orgStr;
    int ratioCheck=0;//eric lin 20180627 add 4:3 letter box UI
    int conversionCheck=0;//eric lin 20180627 add 4:3 letter box UI
    int conversion4X3Check=0;//eric lin 20180627 add 4:3 letter box UI
    int mRatio=0;//eric lin 20180627 add 4:3 letter box UI
    int mConversion=0;//eric lin 20180627 add 4:3 letter box UI
    //eric lin 20180814 hdmi resolution,-start
    private CountDownTimer resetTimer = null;// Timer for createDialog.
    public static int FMT_DELAY_TIME = 10;// timeout in next 10 seconds.
    private HiDisplayManager display_manager = null ;
    private Spinner resolutionSpinner;
    private ArrayList<ResolutionInfo> mSupportResolution = null ;
    private String[] mStrSupportResolution = new String[100];;    
    private int oldFmt;
    private Rect rect;
    private int auto_adaptation = 0xFF ;
    private final String AUTO_STANDBY_KEY = "sleep_timeout";//Scoty 20181129 add Auto Standby item
    //eric lin 20180814 hdmi resolution,-end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.output_settings);

        InitTitleHelp();
        InitSelectBox();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();

        GposInfo gposInfo = GposInfoGet();
        selTvRatio.SetSelectedItemIndex(gposInfo.getScreen16x9());
        mRatio = gposInfo.getScreen16x9();//eric lin 20180627 add 4:3 letter box UI
        setConversionVisibility(selTvRatio.GetSelectedItemIndex());//eric lin 20180627 add 4:3 letter box UI
        selConversion.SetSelectedItemIndex(gposInfo.getConversion());
        selConversion4X3.SetSelectedItemIndex(gposInfo.getConversion());//eric lin 20180627 add 4:3 letter box UI
        mConversion = gposInfo.getConversion();//eric lin 20180627 add 4:3 letter box UI

        selAudioOutput.SetSelectedItemIndex(gposInfo.getDolbyMode());
        selAutoStandby.SetSelectedItemIndex(SetAutoStandbyTimeIndex(Settings.Secure.getInt(getContentResolver(), AUTO_STANDBY_KEY, 0)));//Scoty 20181129 add Auto Standby item
        Log.d(TAG, "onResume: "
                +"\n    conversion = "+GposInfoGet().getConversion()
                +"\n    selConversion = "+selConversion.GetSelectedItemIndex()
        );
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();

        GposInfo gposInfo = GposInfoGet();
        gposInfo.setScreen16x9(selTvRatio.GetSelectedItemIndex());
        if(selTvRatio.GetSelectedItemIndex() == 0) {//16x9 //eric lin 20180627 add 4:3 letter box UI,-start
        gposInfo.setConversion(selConversion.GetSelectedItemIndex());
        }else if(selTvRatio.GetSelectedItemIndex() == 1) {//4x3
            gposInfo.setConversion(selConversion4X3.GetSelectedItemIndex());
        }//eric lin 20180627 add 4:3 letter box UI,-end
        gposInfo.setDolbyMode(selAudioOutput.GetSelectedItemIndex());
        GposInfoUpdate(gposInfo);
        Log.d(TAG, "onPause: "
                +"\n    conversion = "+GposInfoGet().getConversion()
                +"\n    selConversion = "+selConversion.GetSelectedItemIndex()
                +"\n    selAudioOutput = "+selAudioOutput.GetSelectedItemIndex()
        );
    }


    private AdapterView.OnItemSelectedListener RatioListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()//eric lin 20180627 add 4:3 letter box UI
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            if(++ratioCheck > 1) {
                super.onItemSelected(adapterView, view, position, id);
                int ratioIndex = selTvRatio.GetSelectedItemIndex();
                setConversionVisibility(ratioIndex);
                if (ratioIndex == 0) {//16x9
                    selConversion.SetSelectedItemIndex(selConversion4X3.GetSelectedItemIndex());
                } else if (ratioIndex == 1) {//4x3
                    selConversion4X3.SetSelectedItemIndex(selConversion.GetSelectedItemIndex());
                }
                //Log.d(TAG, "onItemSelected:  RatioConversionListener 111 ratioPos=" + selTvRatio.GetSelectedItemIndex() + ", con=" + selConversion.GetSelectedItemIndex() + ", con4x3=" + selConversion4X3.GetSelectedItemIndex());
                setRatioConversion(selTvRatio.GetSelectedItemIndex(), selConversion.GetSelectedItemIndex());
            }
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private AdapterView.OnItemSelectedListener RatioConversionListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            if(++conversionCheck > 1) {//eric lin 20180627 add 4:3 letter box UI
                super.onItemSelected(adapterView, view, position, id);
                //Log.d(TAG, "onItemSelected:  RatioConversionListener 222 ratioPos=" + selTvRatio.GetSelectedItemIndex() + ", con=" + selConversion.GetSelectedItemIndex() + ", con4x3=" + selConversion4X3.GetSelectedItemIndex());
                setRatioConversion(selTvRatio.GetSelectedItemIndex(), selConversion.GetSelectedItemIndex());
            }

        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private AdapterView.OnItemSelectedListener Conversion4X3Listener =  new SelectBoxView.SelectBoxOnItemSelectedListener()//eric lin 20180627 add 4:3 letter box UI
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            if(++conversion4X3Check > 1) {
                super.onItemSelected(adapterView, view, position, id);
                //Log.d(TAG, "onItemSelected:  RatioConversionListener 333 ratioPos=" + selTvRatio.GetSelectedItemIndex() + ", con=" + selConversion.GetSelectedItemIndex() + ", con4x3=" + selConversion4X3.GetSelectedItemIndex());
                setRatioConversion(selTvRatio.GetSelectedItemIndex(), selConversion4X3.GetSelectedItemIndex());                
            }

        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private AdapterView.OnItemSelectedListener AudioOutputListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);

            AvControlAudioOutput(ViewHistory.getPlayId(), position);
            Log.d(TAG, "onItemSelected:  " + position);

        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    //Scoty 20181129 add Auto Standby item
    private AdapterView.OnItemSelectedListener AutoStandbyListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            AutoStandbyTimeTransfer(position);
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private void InitTitleHelp() {
        Log.d(TAG, "InitTitleHelp: ");
        title = (ActivityTitleView) findViewById(R.id.outputTitleLayout);
        help = (ActivityHelpView) findViewById(R.id.outputHelpLayout);
        title.setTitleView(getString(R.string.STR_OUTPUT_SETTINGS_TITLE));
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

        help.setHelpInfoTextBySplit(orgStr);
        help.resetHelp(1,0,null);
        help.resetHelp(2,0,null);
        help.resetHelp(3,0,null);
        help.resetHelp(4,0,null);
    }

    private void InitSelectBox() {
        Log.d(TAG, "InitSelectBox: ");
        Resources res = getResources();
        tvRatio = (Spinner) findViewById(R.id.tvRatio);
        conversion = (Spinner) findViewById(R.id.conversion);
        conversion4X3 = (Spinner) findViewById(R.id.conversion4X3);//eric lin 20180627 add 4:3 letter box UI
        audoutput = (Spinner) findViewById(R.id.audoutputSPINNER);
        //hdmiResolution = (Spinner) findViewById(R.id.hdmiResolution);
        autoStandby = (Spinner) findViewById(R.id.autoStandbySpinner);//Scoty 20181129 add Auto Standby item

        selTvRatio = new SelectBoxView(this,
                tvRatio, res.getStringArray(R.array.STR_TV_RATIO_OPTION));
        selConversion = new SelectBoxView(this,
                conversion, res.getStringArray(R.array.STR_CONVERSION_OPTION));
        selConversion4X3 = new SelectBoxView(this,
                conversion4X3, res.getStringArray(R.array.STR_CONVERSION4X3_OPTION));//eric lin 20180627 add 4:3 letter box UI
        selAudioOutput = new SelectBoxView(this,
                audoutput, res.getStringArray(R.array.STR_ARRAY_AUDIO_OUTPUT));
        selAutoStandby = new SelectBoxView(this,
                autoStandby, res.getStringArray(R.array.STR_AUTO_STANDBY_OPTION_ENTRIES));//Scoty 20181129 add Auto Standby item

        tvRatio.setOnItemSelectedListener(RatioListener);
        conversion.setOnItemSelectedListener(RatioConversionListener);
        conversion4X3.setOnItemSelectedListener(Conversion4X3Listener);//eric lin 20180627 add 4:3 letter box UI
        audoutput.setOnItemSelectedListener(AudioOutputListener);
        autoStandby.setOnItemSelectedListener(AutoStandbyListener);//Scoty 20181129 add Auto Standby item
        resolutionInit();//eric lin 20180814 hdmi resolution
    }

    private void setConversionVisibility(int ratio){//eric lin 20180627 add 4:3 letter box UI
        if(selTvRatio.GetSelectedItemIndex() == 0) {//16x9
            conversion.setVisibility(View.VISIBLE);
            conversion4X3.setVisibility(View.INVISIBLE);
        }
        else if(selTvRatio.GetSelectedItemIndex() == 1) {//4x3
            conversion.setVisibility(View.INVISIBLE);
            conversion4X3.setVisibility(View.VISIBLE);
        }
    }

    private void setRatioConversion(int ratio, int conversion){//eric lin 20180627 add 4:3 letter box UI
        int tmpRatio = GetRatioByIndex(ratio);
        if(ratio != mRatio || conversion != mConversion) {
            //Log.d(TAG, "RatioConversionListener ratio="+ratio+", mRatio="+mRatio+", conversion="+conversion+", mConversion="+mConversion);
            mRatio = ratio;
            mConversion = conversion;
            AvControlChangeRatioConversion(ViewHistory.getPlayId(), tmpRatio, conversion);
        }
    }

    //eric lin 20180814 hdmi resolution,-start
    private void resolutionInit(){
        resolutionSpinner = (Spinner) findViewById(R.id.resolutionSpinner);
        display_manager = new HiDisplayManager();
        getSupportResolutionList() ;
        ArrayAdapter<ResolutionInfo> adapter =
                new ArrayAdapter<ResolutionInfo>(this, android.R.layout.simple_spinner_dropdown_item,mSupportResolution);
        resolutionSpinner.setAdapter(adapter);


        String [] strSupportResolution = new String [mSupportResolution.size()];
        for(int i=0; i<mSupportResolution.size(); i++){
            strSupportResolution[i] = mSupportResolution.get(i).getResolution(); //new String(mSupportResolution.get(i).getResolution());
            //Log.d(TAG, "resolutionInit: i="+i+", mStrSupportResolution[i]="+mStrSupportResolution[i]);
        }

        selHDMIResolution = new SelectBoxView(this,
                resolutionSpinner, strSupportResolution);

        resetSpinnerListIndex() ;//set current resolution

        resolutionSpinner.setOnFocusChangeListener(new SelectBoxView.SelectBoxtOnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                super.onFocusChange(v, hasFocus);
                if (resolutionSpinner.isFocused())
                    help.setHelpInfoTextBySplit(getString(R.string.STR_HDMI_RESOLUTION_HELP_INFO));
                else
                    help.setHelpInfoTextBySplit(orgStr);
            }
        });

        //resolutionSpinner.setOnItemSelectedListener(ResolutionOnItemSelectListener);
        resolutionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ResolutionInfo selectResolution = mSupportResolution.get(position);//(resolutionInfo) parent.getSelectedItem();
                //Log.d( TAG, "Format: "+selectResolution.getFormat()+ ",  Resolution Name : " + selectResolution.getResolution() ) ;
                rect = display_manager.getGraphicOutRange();

                if ( selectResolution.getFormat() == auto_adaptation )
                {
                    display_manager.setOptimalFormatEnable(1); // enable auto set highest resoution
                    display_manager.saveParam();
                    oldFmt = display_manager.getFmt() ;
                    resetSpinnerListIndex() ;
                    return ;
                }

                if ( oldFmt != selectResolution.getFormat() ) { // to avoid cancel set fmt, dialog popup twice when setSelection back
                    display_manager.setFmt(selectResolution.getFormat());
                    createDialog();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    protected void createDialog() {
        final SureDialog sureDialog = new SureDialog(OutputSettingsActivity.this) {
            public void onSetMessage(View v) {
                ((TextView) v).setText(getString(R.string.STR_ARE_YOU_SURE_TO_SWITCH_IT));
            }
            public void onSetNegativeButton() {
                setBackToOldFormat();
                //dialog.dismiss();
                if (resetTimer != null) {
                    resetTimer.cancel();
                }

            }

            public void onSetPositiveButton() {
                //dialog.dismiss();
                display_manager.saveParam();
                oldFmt = display_manager.getFmt() ;
                display_manager.setOptimalFormatEnable(0); // disable auto set highest resoution
                if (resetTimer != null) {
                    resetTimer.cancel();
                }
            }
        };

        resetTimer = new CountDownTimer(1000*FMT_DELAY_TIME, 1000) {
            @Override
            public void onTick(long time) {
                Log.d( TAG, "onTick " + time ) ;
            }

            @Override
            public void onFinish() {
                Log.d( TAG, "CountDown finish!" ) ;
                setBackToOldFormat();
                sureDialog.dismissDialog();
            }
        }.start();


    }
    
    private void getSupportResolutionList()
    {
        oldFmt = display_manager.getFmt();
        Log.d( TAG, "oldFmt = " + oldFmt ) ;

        DispFmt capfmt = display_manager.getDisplayCapability();
        if ( mSupportResolution == null )
            mSupportResolution =  new ArrayList<>();
        int hdmiport = display_manager.getOutputEnable(0);
        Log.i(TAG,"hdmiport=="+hdmiport+"\n ");

        if(display_manager.getDisplayDeviceType() <= 1) //tv case: 0 is av, 1 is hdmi
        {
            mSupportResolution.clear();
            if(hdmiport == 1) //tv case
            {
                Log.i(TAG,"gethdmi cap success !");
                int i = 0;
                if(capfmt!=null)
                {
                    if(capfmt.ENC_FMT_1080P_60 == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080P_60,new String("1080P 60"))) ;
                    if(capfmt.ENC_FMT_1080P_50 == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080P_50,new String("1080P 50")));
                    if( capfmt.ENC_FMT_1080P_30 == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080P_30,new String("1080P 30")));
                    if( capfmt.ENC_FMT_1080P_25 == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080P_25,new String("1080P 25")));
                    if( capfmt.ENC_FMT_1080P_24 == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080P_24,new String("1080P 24")));
                    if( capfmt.ENC_FMT_1080i_60 == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080i_60,new String("1080i 60")));
                    if( capfmt.ENC_FMT_1080i_50 == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080i_50,new String("1080i 50")));
                    if( capfmt.ENC_FMT_720P_60 == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_720P_60,new String("720P 60")));
                    if(capfmt.ENC_FMT_720P_50 == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_720P_50,new String("720P 50")));
                    if( capfmt.ENC_FMT_576P_50 == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_576P_50,new String("576P 50")));
                    if( capfmt.ENC_FMT_480P_60 == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_480P_60,new String("480P 60")));
                    if( capfmt.ENC_FMT_PAL == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_PAL,new String("PAL")));
                    if( capfmt.ENC_FMT_NTSC == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_NTSC,new String("NTSC")));
                    if( capfmt.ENC_FMT_1080P_24_FRAME_PACKING == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080P_24_FRAME_PACKING,new String("1080P_24_FRAME_PACKING")));
                    if( capfmt.ENC_FMT_720P_60_FRAME_PACKING == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_720P_60_FRAME_PACKING,new String("720P_60_FRAME_PACKING")));
                    if( capfmt.ENC_FMT_720P_50_FRAME_PACKING == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_720P_50_FRAME_PACKING,new String("720P_50_FRAME_PACKING")));
                    if( capfmt.ENC_FMT_3840X2160_24             == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_3840X2160_24,new String("3840X2160_24")));
                    if( capfmt.ENC_FMT_3840X2160_25             == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_3840X2160_25,new String("3840X2160_25")));
                    if( capfmt.ENC_FMT_3840X2160_30             == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_3840X2160_30,new String("3840X2160_30")));
                    if( capfmt.ENC_FMT_3840X2160_50             == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_3840X2160_50,new String("3840X2160_50")));
                    if( capfmt.ENC_FMT_3840X2160_60             == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_3840X2160_60,new String("3840X2160_60")));
                    if( capfmt.ENC_FMT_4096X2160_24             == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_4096X2160_24,new String("4096X2160_24")));
                    if( capfmt.ENC_FMT_4096X2160_25             == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_4096X2160_25,new String("4096X2160_25")));
                    if( capfmt.ENC_FMT_4096X2160_30             == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_4096X2160_30,new String("4096X2160_30")));
                    if( capfmt.ENC_FMT_4096X2160_50             == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_4096X2160_50,new String("4096X2160_50")));
                    if( capfmt.ENC_FMT_4096X2160_60             == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_4096X2160_60,new String("4096X2160_60")));
                    if( capfmt.ENC_FMT_3840X2160_23_976         == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_3840X2160_23_976,new String("3840X2160_23_976")));
                    if( capfmt.ENC_FMT_3840X2160_29_97          == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_3840X2160_29_97,new String("3840X2160_29_97")));
                    if( capfmt.ENC_FMT_720P_59_94               == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_720P_59_94,new String("720P_59_94")));
                    if( capfmt.ENC_FMT_1080P_59_94              == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080P_59_94,new String("1080P_59_94")));
                    if( capfmt.ENC_FMT_1080P_29_97              == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080P_29_97,new String("1080P_29_97")));
                    if( capfmt.ENC_FMT_1080P_23_976             == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080P_23_976,new String("1080P_23_976")));
                    if( capfmt.ENC_FMT_1080i_59_94              == 1)
                        mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080i_59_94,new String("1080i_59_94")));
                    mSupportResolution.add(new ResolutionInfo(auto_adaptation, new String("Auto adaptation")));
                }
                else//hdmi enable case ,but hdmi cap get fail
                {
                    mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080P_60,new String("1080P 60")));
                    mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080P_50,new String("1080P 50")));
                    mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080i_60,new String("1080i 60")));
                    mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_1080i_50,new String("1080i 50")));
                    mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_720P_60,new String("720P 60")));
                    mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_720P_50,new String("720P 50")));
                    mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_576P_50,new String("576P 50")));
                    mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_480P_60,new String("480P 60")));
                    mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_NTSC,new String("NTSC")));
                    mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_PAL,new String("PAL")));
                    mSupportResolution.add(new ResolutionInfo(auto_adaptation, new String("Auto adaptation")));
                    Log.w(TAG,"hdmi enable case ,but hdmi cap get fail !");
                }
            }
            else//tv case, hdmi not enable, only  cvbs case
            {
                Log.w(TAG,"hdmi not enable or there isn't hdmi hotplug event case,only cvbs!");
                mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_PAL,new String("PAL")));
                mSupportResolution.add(new ResolutionInfo(HiDisplayManager.ENC_FMT_NTSC,new String("NTSC")));
            }

            for (final ResolutionInfo resolutionInfo : mSupportResolution)
            {
                Log.d( TAG, "support resolution[" + mSupportResolution.indexOf(resolutionInfo) + "] : " + resolutionInfo.getResolution() + " hw fmt index : " + resolutionInfo.getFormat()) ;
            }
        }
    }

    private void resetSpinnerListIndex()
    {
        for (final ResolutionInfo resolutionInfo : mSupportResolution)
        {
            if ( oldFmt == resolutionInfo.getFormat() )
            {
                resolutionSpinner.setSelection(mSupportResolution.indexOf(resolutionInfo));
                break ;
            }
        }
    }

    private void setBackToOldFormat() {
        display_manager.setFmt(oldFmt);
        resetSpinnerListIndex() ;
        if (null != rect) {
            display_manager.setGraphicOutRange(rect.left, rect.top, rect.right, rect.bottom);
        }
    }
    //eric lin 20180814 hdmi resolution,-end

    //Scoty 20181129 add Auto Standby item -s
    private int SetAutoStandbyTimeIndex(int time)
    {
        String[] timeList = getResources().getStringArray(R.array.STR_AUTO_STANDBY_OPTION_ENTRIES_VALUES);
        int index = 0;
        for(int i = 0 ; i < timeList.length ; i++)
        {
            if(Integer.valueOf(timeList[i]) == time)
            {
                index = i;
                break;
            }
        }
        return index;
    }

    private void AutoStandbyTimeTransfer(int position)
    {
        String[] timeList = getResources().getStringArray(R.array.STR_AUTO_STANDBY_OPTION_ENTRIES_VALUES);
        int standbyTime = Integer.valueOf(timeList[position]);
        Settings.Secure.putInt(getContentResolver(), AUTO_STANDBY_KEY, standbyTime);
    }
    //Scoty 20181129 add Auto Standby item -e
}
