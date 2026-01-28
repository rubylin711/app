package com.prime.dtvplayer.Activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.TvInput.TvInputActivity;
import com.dolphin.dtv.HiDtvMediaPlayer;
import com.prime.dtvplayer.Config.Pvcfg;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityHelpViewWoColorIcon;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.RecordTSDialog;
import com.prime.dtvplayer.View.SelectBoxView;
import com.prime.dtvplayer.utils.TVMessage;
import com.prime.dtvplayer.utils.TVScanParams;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_9;

public class ChannelSearchISDBTActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private ActivityTitleView titleView;
    private ActivityHelpView helpView;
    private ActivityHelpViewWoColorIcon helpViewWoColorIcon;
    Spinner SpinnerFreq;
    Spinner SpinnerBandwidth;
    Spinner SpinnerScanMode;
    Spinner SpinnerNitSearch;
    Spinner SpinnerSegChannel;
    Spinner SpinnerAntenna5V;

    Button ButtonSearch;
    ProgressBar ProgressStrength;
    ProgressBar ProgressQuality;
    TextView QualityValue;
    TextView StrengthValue;
    TextView BerValue;
    TextView TextFreq;
    TextView TextBandwidth;
    TextView TextScanMode;
    TextView TextNitSearch;
    TextView TextSegChannel;
    TextView TextAntenna5V;

    SelectBoxView SelectFrequency;
    SelectBoxView SelectBandwidth;
    SelectBoxView SelectScanMode;
    SelectBoxView SelectNitSearch;
    SelectBoxView SelectSegChannel;
    SelectBoxView SelectAntenna5V;

    // Edwin 20181129 add search fail message
    MessageDialogView mSearchFail;

    int Frequency = 0, Bandwidth = 0;  // 0 : 6Mhz, 1 : 7Mhz, 2 : 8Mhz
    int tuneFreq = 0, tuneBandwidth = 0;
    int tpID = 0;

    int ScanMode = 0;   // 0 : fta&$, 1 : fta, 2 : $
    int SegChannel = 0; // 0 : off,  1 : on
    int NitSearch = 0;  // 0 : off,  1 : on
    int Antenna5V = 0;  // 0 : off,  1 : on
    int searchMode = 0; // 0 : auto   1: manual
    int mParent=1; //0:EasyInstall, 1:MainMenu

    private List<TpInfo> tpList = new ArrayList<>();

    Handler  CheckSignalHandler;
    Handler  ChangeParamHandler;

    private String keystr = ""; // connie 20180803 add record ts

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_search_isdbt);
        if(AvControlGetPlayStatus(ViewHistory.getPlayId()) == HiDtvMediaPlayer.EnPlayStatus.LIVEPLAY.getValue()) {
            AvControlPlayStop(ViewHistory.getPlayId());
        }

        Bundle bundle = this.getIntent().getExtras();
        String parent = bundle.getString("parent");
        if(parent.equals("EasyInstall")) {
            mParent = 0;
        }
        else if(parent.equals("MainMenu") || parent.equals("View")) {  // connie 20181116 for  Revisions-20170526 report
            mParent = 1;
        }

        ItemInit();
    }
    @Override
    public void onConnected() {
        super.onConnected();
        CheckSignalHandler = new Handler();
        ChangeParamHandler = new Handler();
        CheckSignalHandler.post(CheckStatusRunnable);
        ChangeParamHandler.post(ParamChangeRunnable);
        Log.d(TAG, "CheckSignalHandler post runnable" );
        Log.d(TAG, "ChangeParamHandler post runnable" );
    }
    @Override
    public void onDisconnected() {
        super.onDisconnected();
        CheckSignalHandler.removeCallbacks(CheckStatusRunnable);
        ChangeParamHandler.removeCallbacks(ParamChangeRunnable);
        Log.d(TAG, "CheckSignalHandler remove Callback" );
        Log.d(TAG, "ChangeParamHandler remove Callback" );
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);
    }

    final Runnable CheckStatusRunnable = new Runnable() {
        public void run() {
            Log.d(TAG, "run:     Auto  CheckStatusRunnable !!!!");
            UpdateSignalLevel();
            CheckSignalHandler.postDelayed(CheckStatusRunnable, 1000);
        }
    };
    final Runnable ParamChangeRunnable = new Runnable() {
        public void run() {
            int changeLock = 0;

            if(Frequency != tuneFreq) // Freq  Change !
            {
                Log.d(TAG, "TP  ID  CHANGE!!!!!!!!!!!  tuneFreq = " + tuneFreq);
                tuneFreq = Frequency;
                tuneBandwidth = Bandwidth;
                changeLock = 1;
            }

            if(Bandwidth != tuneBandwidth)  // Bandwidth  Edit !
            {
                tuneBandwidth = Bandwidth;
                changeLock = 1;
            }

            if(changeLock == 1)
            {
                TunerTuneISDBT(0, tpID, Frequency, Bandwidth );
                Log.d(TAG, "param Save   : TPID = " + tpID);
                Log.d(TAG, "param Save   : Freq = " + Frequency);
                Log.d(TAG, "param Save   : Bandwidth = " + Bandwidth);
            }

            ChangeParamHandler.postDelayed(ParamChangeRunnable, 1000);
        }
    };

    private void ItemInit() {
        int freq = 0, position = 0;
        String str;
        String[] strFreqList = null;
        titleView = (ActivityTitleView) findViewById(R.id.TitleViewLayout);
        if(mParent == 0) { //Easy Install
            titleView.setTitleView(getString(R.string.STR_TITLE_EASY_CHANNEL_SEARCH));
        }
        else if(mParent == 1) {//Main Menu
            titleView.setTitleView(getString(R.string.STR_TITLE_CHANNEL_SEARCH));
        }

        helpView = (ActivityHelpView) findViewById(R.id.HelpViewLayout);
        helpViewWoColorIcon = (ActivityHelpViewWoColorIcon) findViewById(R.id.HelpViewWoColorIconLayout);//for largest display size

        if(mParent == 0) {//Easy Install
            helpViewWoColorIcon.setVisibility(View.INVISIBLE);//for largest display size
            helpView.setHelpInfoTextBySplit(null);
            helpView.resetHelp(1, R.drawable.help_red, getResources().getString(R.string.STR_PREVIOUS));
            helpView.resetHelp(2, R.drawable.help_blue,getResources().getString(R.string.STR_NEXT));
            helpView.resetHelp(3, 0, null);
            helpView.resetHelp(4, 0, null);

            // Johnny 20181228 for mouse control -s
            helpView.setHelpIconClickListener(1, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                    Intent intent = new Intent();
                    intent.setClass(ChannelSearchISDBTActivity.this, FirstTimeInstallationActivity.class);
                    startActivity(intent);
                }
            });
            helpView.setHelpIconClickListener(2, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SearchStart();
                }
            });
            // Johnny 20181228 for mouse control -e
        }
        else if(mParent == 1) {//Main Menu
            helpView.setVisibility(View.INVISIBLE);//for largest display size
            helpViewWoColorIcon.setHelpInfoTextBySplit(null);//for largest display size
        }

//        SpannableStringBuilder style = new SpannableStringBuilder(getString(R.string.STR_PARENT_HELP_INFO));
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 4, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 15, 17, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 28, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 50, 54, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if(mParent == 0) //Easy Install //for largest display size
        helpView.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));
        else if(mParent == 1) //Main Menu //for largest display size
            helpViewWoColorIcon.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));


        SpinnerFreq = (Spinner) findViewById(R.id.freqSPINNER);
        SpinnerBandwidth = (Spinner) findViewById(R.id.bandwidthSPINNER);
        SpinnerScanMode = (Spinner) findViewById(R.id.scanModeSPINNER);
        SpinnerNitSearch = (Spinner) findViewById(R.id.NITSearchSPINNER);
        //SpinnerSegChannel = (Spinner) findViewById(R.id.segChannelSPINNER); // connie 20181116 for  Revisions-20170526 report
        SpinnerAntenna5V = (Spinner) findViewById(R.id.antenna5vSPINNER);

        ProgressStrength =  (ProgressBar) findViewById(R.id.strengthPROGBAR);
        ProgressQuality =  (ProgressBar) findViewById(R.id.qualityPROGBAR);
        QualityValue = (TextView) findViewById(R.id.qualityValueTXV);
        StrengthValue = (TextView) findViewById(R.id.strengthValueTXV);
        BerValue = (TextView) findViewById(R.id.berValueTXV);
        ButtonSearch = (Button) findViewById(R.id.searchBTN);

        TextFreq = (TextView) findViewById(R.id.freqTXV);
        TextBandwidth = (TextView) findViewById(R.id.bandwidthTXV);
        TextScanMode = (TextView) findViewById(R.id.scanModeTXV);

        TextNitSearch = (TextView) findViewById(R.id.NITSearchTXV);
        //TextSegChannel = (TextView) findViewById(R.id.segChannelTXV);  // connie 20181116 for  Revisions-20170526 report
        TextAntenna5V = (TextView) findViewById(R.id.antenna5vTXV);

        str = getString(R.string.STR_FREQUENCY)+" : ";
        TextFreq.setText(str);
        str = getString(R.string.STR_ISDBT_BANDWIDTH)+" : ";
        TextBandwidth.setText(str);
        str=getString(R.string.STR_SCAN_MODE)+" : ";
        TextScanMode.setText(str);
        str = getString(R.string.STR_ISDBT_NITSEARCH)+" : ";
        TextNitSearch.setText(str);
        //str = getString(R.string.STR_ISDBT_1SEGCHANNEL)+" : ";  // connie 20181116 for  Revisions-20170526 report
        //TextSegChannel.setText(str);
        str = getString(R.string.STR_ANTENNA_5V)+" : ";
        TextAntenna5V.setText(str);

        Log.d(TAG, "ChannelSearch ItemInit: CurTunerType = " + GetCurTunerType());
        tpList = TpInfoGetList(GetCurTunerType());

        if (tpList == null)
        {
            tpList = new ArrayList<>();
        }

        //Log.d(TAG, "size " + tpList.size() );
        strFreqList = new String [tpList.size()];
        for(int i = 0; i< tpList.size(); i++) {
            freq = tpList.get(i).TerrTp.getFreq();
            strFreqList[i] =Integer.toString(freq);
            Log.d(TAG, "tp id = " + tpList.get(i).getTpId() + "     freq = " + tpList.get(i).TerrTp.getFreq() +
                    "    Bandwidth = " + tpList.get(i).TerrTp.getBand() );
        }

        // Setting  Freq
        SelectFrequency = new SelectBoxView(this, SpinnerFreq, strFreqList);
        SpinnerFreq.setOnItemSelectedListener(FreqListener);
        if(ViewHistory.getCurChannel() != null) {
            ProgramInfo programInfo = ProgramInfoGetByChannelId(ViewHistory.getCurChannel().getChannelId());
            TpInfo cutTpInfo = TpInfoGet(programInfo.getTpId());
            // Change Lock !!!!
            for (int i = 0; i < tpList.size(); i++) {
                if (cutTpInfo.TerrTp.getFreq() == tpList.get(i).TerrTp.getFreq()) {
                    position = i;
                    break;
                }
            }
        }
        SpinnerFreq.setSelection(position);

        // Setting  Bandwidth
        String[] strBandList = getResources().getStringArray(R.array.STR_ISDBT_ARRAY_BANDWIDTH);
        SelectBandwidth = new SelectBoxView(this, SpinnerBandwidth, strBandList);
        SpinnerBandwidth.setFocusable(false);
        SpinnerBandwidth.setEnabled(false); // Johnny 20181219 for mouse control
        SpinnerBandwidth.setOnItemSelectedListener(BandwidthListener);

        // setting ScanMode
        String[] scanModeList = getResources().getStringArray(R.array.STR_ARRAY_SCAN_MODE);
        SelectScanMode = new SelectBoxView(this, SpinnerScanMode, scanModeList);

        // Seg Channel
        //String[] segChannelList = getResources().getStringArray(R.array.STR_ISDBT_ARRAY_1SEG_CHANNEL);  // connie 20181116 for  Revisions-20170526 report
        //SelectSegChannel = new SelectBoxView(this, SpinnerSegChannel, segChannelList);

        // NIT
        String[] nitList = getResources().getStringArray(R.array.STR_ISDBT_ARRAY_NIT_SEARCH);
        SelectNitSearch = new SelectBoxView(this, SpinnerNitSearch, nitList);
        SpinnerNitSearch.setVisibility(View.INVISIBLE);
        TextNitSearch.setVisibility(View.INVISIBLE);

        // Antenna 5V
        String[] antenna5VList = getResources().getStringArray(R.array.STR_ARRAY_ANTENNA_5V);
        SelectAntenna5V = new SelectBoxView(this, SpinnerAntenna5V, antenna5VList);
        SpinnerAntenna5V.setOnItemSelectedListener(Antenna5VListener);

        // search Button
        if(mParent == 0) { //Easy Install
            ButtonSearch.setVisibility(View.INVISIBLE);
        }
        else
        {
            ButtonSearch.setOnClickListener(SearchClickListener);
            ButtonSearch.setOnLongClickListener(SearchLongClickListener);   // Johnny 20181219 for mouse control
            ButtonSearch.setOnFocusChangeListener(SearchFocusListener);
        }

        // Edwin 20181129 add search fail message
        mSearchFail = new MessageDialogView(this, R.string.STR_SEARCH_FAIL, 3000) {
            @Override
            public void dialogEnd () {
                Log.d(TAG, "dialogEnd: Search Failed");
            }
        };
    }

    // Edwin 20181129 add search fail message
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( resultCode == ScanResultActivity.RESULT_SEARCH_FAIL )
        {
            Log.d(TAG, "onActivityResult: RESULT_SEARCH_FAIL");
            mSearchFail.show();
        }
    }

    private AdapterView.OnItemSelectedListener FreqListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            String str;
            // Change Lock !!!!
            //Log.d(TAG, "position =  " + position);
            Frequency = tpList.get(position).TerrTp.getFreq();
            Bandwidth = tpList.get(position).TerrTp.getBand();
            tpID = tpList.get(position).getTpId();

            SpinnerBandwidth.setSelection(Bandwidth);

            Log.d(TAG, "onItemSelected:    Frequency   = " + Frequency);
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };



    private AdapterView.OnItemSelectedListener BandwidthListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            Bandwidth = position;
            Log.d(TAG, "freq = " +Frequency+ "  bandwidth = " + Bandwidth );
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private AdapterView.OnItemSelectedListener Antenna5VListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            GposInfo gpos = GposInfoGet();

            //Log.d(TAG, "position =  " + position);
            Log.d(TAG, "Antenna5VListener: position="+position);

            if(position == 0) {
                gpos.setLnbPower(0);
                if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798) // connie 20181101 for 3798 5v on/off
                    MtestSetAntenna5V(0, GetCurTunerType(), 0);
                else
                    TunerSetAntenna5V(0, 0);
            }
            else if(position == 1) {
                gpos.setLnbPower(1);
                if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798) // connie 20181101 for 3798 5v on/off
                    MtestSetAntenna5V(0, GetCurTunerType(), 1);
                else
                    TunerSetAntenna5V(0, 1);
            }
            GposInfoUpdate(gpos);

            Log.d(TAG, "onItemSelected: Antenna5V="+position);
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private View.OnFocusChangeListener SearchFocusListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {

            if (hasFocus) {
                // Set Help
//                SpannableStringBuilder style = new SpannableStringBuilder(getString(R.string.STR_SEARCH_BUTTON_HELPTEXT));
//                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 4, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 38, 40, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 60, 64, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if(mParent == 0) //Easy Install //for largest display size
                helpView.setHelpInfoTextBySplit(getString(R.string.STR_SEARCH_BUTTON_HELPTEXT));
                else if(mParent == 1) //Main Menu //for largest display size
                    helpViewWoColorIcon.setHelpInfoTextBySplit(getString(R.string.STR_SEARCH_BUTTON_HELPTEXT));
            }
            else
            {
//                SpannableStringBuilder style = new SpannableStringBuilder(getString(R.string.STR_PARENT_HELP_INFO));
//                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 4, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 15, 17, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 28, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 50, 54, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if(mParent == 0) //Easy Install //for largest display size
                helpView.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));
                else if(mParent == 1) //Main Menu //for largest display size
                    helpViewWoColorIcon.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));
            }
        }
    };


    private View.OnClickListener SearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SearchStart();
        }
    };

    // Johnny 20181219 for mouse control
    private View.OnLongClickListener SearchLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            changeSearchMode();
            return true;
        }
    };

    private void UpdateSignalLevel()
    {
        int Quality=0;
        int Strength =0;
        int lock = 0;
        int barcolor ;
        String ber;
        String str;
        lock = TunerGetLockStatus(0);
        Quality = TunerGetQuality(0);
        Strength = TunerGetStrength(0);
        ber = TunerGetBER(0);

        //Log.d(TAG, "UpdateSignalLevel  Strengh = " + Strength + "Quality = " + Quality);
        str = Integer.toString(Strength)+" %";
        StrengthValue.setText(str);
        str = Integer.toString(Quality)+" %";
        QualityValue.setText(str);
        BerValue.setText(ber);

        if(lock == 1 )
            barcolor =Color.GREEN;
        else
            barcolor =Color.RED;
        ProgressStrength.setProgressTintList(ColorStateList.valueOf(barcolor));
        ProgressQuality.setProgressTintList(ColorStateList.valueOf(barcolor));

        ProgressStrength.setProgress(Strength);
        ProgressQuality.setProgress(Quality);
    }

    private void SearchStart()
    {
        int freqIndex = SelectFrequency.GetSelectedItemIndex();
        if(Frequency == tpList.get(freqIndex).TerrTp.getFreq()) // Freq  Change !
        {
            if(Bandwidth != tpList.get(freqIndex).TerrTp.getBand())  // Bandwidth  Edit !
            {
                tpList.get(freqIndex).TerrTp.setBand(Bandwidth);
            }

            TpInfoUpdateList(tpList);
        }

        Bandwidth = SelectBandwidth.GetSelectedItemIndex();
        ScanMode = SelectScanMode.GetSelectedItemIndex();
        NitSearch = SelectNitSearch.GetSelectedItemIndex();
        //SegChannel = SelectSegChannel.GetSelectedItemIndex();
        SegChannel = 0;  // connie 20181116 for  Revisions-20170526 report
        Antenna5V = SelectAntenna5V.GetSelectedItemIndex();

        Intent intent = new Intent();
        intent.setClass(ChannelSearchISDBTActivity.this, ScanResultActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt("tp_id", tpID);
        bundle.putInt("scan_mode", ScanMode);   // 0 : fta&$, 1 : fta, 2 : $
        bundle.putInt("nit_search", NitSearch); // 0 : off, 1 : on
        bundle.putInt("1seg_channel", SegChannel);  // 0 : off, 1 : on
        bundle.putInt("search_mode", searchMode);   // 0 : auto  1 : manual
        intent.putExtras(bundle);
        startActivityForResult(intent, 2);

        if ( TvInputActivity.isTvInputOpened() ) // Edwin 20181214 live tv cannot scan channel
        {
            finish();
        }
    }

    // ========  OnKeyDown ===================
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mParent == 0) {//Easy Install
            switch (keyCode) {
                case KeyEvent.KEYCODE_PROG_BLUE:
                case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                {
                    SearchStart();
                }break;
                case KeyEvent.KEYCODE_PROG_RED:
                case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                {
                    finish();
                    Intent intent = new Intent();
                    intent.setClass(ChannelSearchISDBTActivity.this, FirstTimeInstallationActivity.class);
                    startActivity(intent);
                }break;
                default:
                    break;
            }
        }
        else if(mParent == 1) { //Main Menu
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT: {
                    if(ButtonSearch.hasFocus()) {
                        Log.d(TAG, "str = " + ButtonSearch.getText());
                        changeSearchMode(); // Johnny 20181219 for mouse control, use function instead
                        return true;
                    }
                }
            }
            if (keystr.length() >= 4) // connie 20180803 add record ts -s
                keystr = "";
            if(keyCode >= KEYCODE_0 && keyCode <= KEYCODE_9)
                keystr=keystr+Integer.toString(keyCode-KEYCODE_0);
            else
                keystr="";
            Log.d(TAG, "onKeyDown: keystr = " + keystr);
            if(keystr.equals("3141") && (TunerGetLockStatus(0)==1))
                RecordTS();// connie 20180803 add record ts -e
        }

        return super.onKeyDown(keyCode, event);
    }

    private void RecordTS() // connie 20180803 add record ts
    {
        int tunerId = 0;
        String RecMountPath = GetRecordPath();
        if(RecMountPath == null)
            return ;

        if(RecMountPath.equals(getDefaultRecPath())) {
            new MessageDialogView(this,getString( R.string.STR_STORAGE_DEVICE_IS_NOT_AVAILABLE ),getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY ))
            {
                public void dialogEnd() {
                }
            }.show();
            return;
        }

        File file = new File(RecMountPath);
        if (!file.exists())
        {
            Log.d(TAG, "Record Path Not Exit ! path = " + GetRecordPath() );
            SetRecordPath(getDefaultRecPath()); // connie 20180525 get path by getDefaultRecPath()
            file = new File(getDefaultRecPath());
            if (!file.exists()) {
                Log.d(TAG, "Record Path Not Exit ! path = " + GetRecordPath() );
                return ;
            }
        }

        SimpleDateFormat CurTimeformatter = new SimpleDateFormat("MMM_dd_HH_mm", Locale.getDefault());
        String FileName, fullName;
        String curTime = CurTimeformatter.format(GetLocalTime().getTime());
        String bandwidth = SpinnerBandwidth.getSelectedItem().toString();
        FileName = SpinnerFreq.getSelectedItem().toString() + "_" +"_"+ bandwidth + "_" + curTime + ".ts";
        fullName = GetRecordPath()+"/Records/"+FileName;
        RecordTSDialog recDurDialog = new RecordTSDialog(ChannelSearchISDBTActivity.this, this, tunerId, fullName );
        return ;
    }

    // Johnny 20181219 for mouse control
    private void changeSearchMode() {
        if(searchMode == TVScanParams.SCAN_MODE_AUTO) {
            ButtonSearch.setText(R.string.STR_MANUAL_SEARCH);
            searchMode = TVScanParams.SCAN_MODE_MANUAL ;
//            SpinnerBandwidth.setFocusable(true);  // temporarily mark due to mantis : 0005099
//            SpinnerBandwidth.setEnabled(true);    // temporarily mark due to mantis : 0005099
//            SpinnerBandwidth.setFocusableInTouchMode(true);   // temporarily mark due to mantis : 0005099
            SpinnerNitSearch.setVisibility(View.VISIBLE);
            TextNitSearch.setVisibility(View.VISIBLE);
        }
        else {
            ButtonSearch.setText(R.string.STR_AUTO_SEARCH);
            searchMode = TVScanParams.SCAN_MODE_AUTO;
            SelectNitSearch.SetSelectedItemIndex(0);    // Set Nit to off when auto search
            SpinnerBandwidth.setFocusable(false);
            SpinnerBandwidth.setEnabled(false);
            SpinnerNitSearch.setVisibility(View.INVISIBLE);
            TextNitSearch.setVisibility(View.INVISIBLE);
        }
    }
}