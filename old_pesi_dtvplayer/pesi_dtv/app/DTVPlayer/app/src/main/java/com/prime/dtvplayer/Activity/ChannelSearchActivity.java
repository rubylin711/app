package com.prime.dtvplayer.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.TvInput.TvInputActivity;
import com.dolphin.dtv.HiDtvMediaPlayer;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.MiscDefine;
import com.prime.dtvplayer.Sysdata.PROGRAM_PLAY_STREAM_TYPE;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityHelpViewWoColorIcon;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.SelectBoxView;
import com.prime.dtvplayer.utils.TVMessage;
import com.prime.dtvplayer.utils.TVScanParams;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.RecordTSDialog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.text.SimpleDateFormat;

import java.util.Locale;
import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_9;

public class ChannelSearchActivity extends DTVActivity {
    private final String TAG = "ChannelSearchActivity";
    private ActivityHelpView helpView;
    private ActivityHelpViewWoColorIcon helpViewWoColorIcon;
    Spinner SpinnerFreq;
    Spinner SpinnerQam;
    Spinner SpinnerScanMode;
    Spinner SpinnerChannelType;
    EditText EditSymbolrate;
    Button ButtonSearch;
    ProgressBar ProgressStrength;
    ProgressBar ProgressQuality;
    TextView QualityValue;
    TextView StrengthValue;
    TextView BerValue;
    TextView TextFreq;
    TextView TextSymbol;
    TextView TextQam;
    TextView TextScanMode;
    TextView TextChannelType;
    SelectBoxView SelectQam;
    SelectBoxView SelectScanMode;
    SelectBoxView SelectChannelType;

    // Edwin 20181129 add search fail message
    MessageDialogView mSearchFail;

    int Frequency = 0, Symbolrate = 0, Qam = 0;
    int tuneFreq = 0, tuneSymbol = 0, tuneQam = 0;
    int tpID = 0;

    private List<Integer> SearchModeList = new ArrayList<>();
    int SearchMode = TVScanParams.SCAN_MODE_AUTO; // 0 : auto   1: manual
    int ScanMode = 0;
    int ChannelType = 0;
    int mParent=1; //0:EasyInstall, 1:MainMenu
    //int NitSearch = 0;  // 0 : off,  1 : on
    private List<TpInfo> tpList = new ArrayList<>();
    Handler  CheckSignalHandler=null;
    Handler  ChangeParamHandler=null;

    private String keystr = ""; // connie 20180803 add record ts

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_search);

        if(AvControlGetPlayStatus(ViewHistory.getPlayId()) == HiDtvMediaPlayer.EnPlayStatus.LIVEPLAY.getValue()) {
            AvControlPrePlayStop();
            AvControlPlayStop(MiscDefine.AvControl.AV_STOP_ALL);
        }

        Bundle bundle =this.getIntent().getExtras();
        if(bundle != null) {
            String parent = bundle.getString(getString(R.string.STR_EXTRAS_PARENT));
            if(parent != null) {
                if (parent.equals(getString(R.string.STR_EXTRAS_EASYINSTALL)))
            mParent = 0;
                else if (parent.equals(getString(R.string.STR_EXTRAS_MAINMENU)))
                    mParent = 1;
            }else
            mParent = 1;
        }else
            mParent = 1;

        ItemInit();
    }
    @Override
    public void onConnected() {
        super.onConnected();
        CheckSignalHandler = new Handler();
        ChangeParamHandler = new Handler();
        //CheckSignalHandler.post(CheckStatusRunnable);
        tuneFreq =0;//eric lin 20180612 fix ScanResult backto channel search, not to re-tune
        ChangeParamHandler.post(ParamChangeRunnable);
        Log.d(TAG, "ChangeParamHandler post runnable" );
    }
    @Override
    public void onDisconnected() {
        super.onDisconnected();
        //CheckSignalHandler.removeCallbacks(CheckStatusRunnable);
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
    protected void onDestroy() {
        super.onDestroy();

        int avPlayStatus = AvControlGetPlayStatus(ViewHistory.getPlayId());
        boolean isAvStop = avPlayStatus == HiDtvMediaPlayer.EnPlayStatus.IDLE.getValue()
                || avPlayStatus == HiDtvMediaPlayer.EnPlayStatus.STOP.getValue()
                || avPlayStatus == HiDtvMediaPlayer.EnPlayStatus.RELEASEPLAYRESOURCE.getValue();

        if (isAvStop && ViewHistory.getCurChannel() != null) {
            AvControlPlayByChannelId(ViewHistory.getPlayId(), ViewHistory.getCurChannel().getChannelId(), ViewHistory.getCurGroupType(), 1);
        }
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);
    }

    final Runnable CheckStatusRunnable = new Runnable() {
        public void run() {
            Log.d(TAG, "run:     Auto  CheckStatusRunnable !!!!");
            if(Frequency !=0 && Symbolrate != 0) {
                UpdateSignalLevel();
            }
            CheckSignalHandler.postDelayed(CheckStatusRunnable, 1000);
        }
    };

    final Runnable ParamChangeRunnable = new Runnable() {
        public void run() {
            int changeLock = 0;
            int freqIndex = SpinnerFreq.getSelectedItemPosition();

            if (tpList != null && !tpList.isEmpty()) {
                //TpInfo tpInfo = TpInfoGet(tpList.get(freqIndex).getTpId());
                if (tuneFreq != tpList.get(freqIndex).CableTp.getFreq()) // Freq  Change !
                {
                    Log.d(TAG, "TP  ID  CHANGE!!!!!!!!!!!  tuneFreq = " + tuneFreq);
                    tuneFreq = tpList.get(freqIndex).CableTp.getFreq();
                    tuneSymbol = Symbolrate;
                    tuneQam = Qam;
                    changeLock = 1;
                }
                if (Symbolrate != tuneSymbol)  // Symbol  Edit !
                {
                    tuneSymbol = Symbolrate;
                    changeLock = 1;
                }
                if (Qam != tuneQam) // Qam Edit !
                {
                    tuneQam = Qam;
                    changeLock = 1;
                }

                if (changeLock == 1) {
                    tpID = tpList.get(freqIndex).getTpId();
                    Log.d(TAG, "ParamChangeRunnable   : TPID = " + tpID);
                    Log.d(TAG, "ParamChangeRunnable   : Freq = " + tuneFreq);
                    Log.d(TAG, "ParamChangeRunnable   : Symbol = " + tuneSymbol);
                    Log.d(TAG, "ParamChangeRunnable   : Qam = " + tuneQam);
                    if (tuneFreq != 0 && tuneSymbol != 0)
                        TunerTuneDVBC(0, tpID, tuneFreq, tuneSymbol, tuneQam);
                }
                if (tuneFreq != 0 && tuneSymbol != 0) {
                    UpdateSignalLevel();
                }
            }

            ChangeParamHandler.postDelayed(ParamChangeRunnable, 1000);
        }
    };

    private void ItemInit() {
        ActivityTitleView titleView;
        int freq,position = 0;
        String str;
        String[] strFreqList;
        titleView = (ActivityTitleView) findViewById(R.id.TitleViewLayout);
            titleView.setTitleView(getString(R.string.STR_TITLE_EASY_CHANNEL_SEARCH));
            titleView.setTitleView(getString(R.string.STR_TITLE_CHANNEL_SEARCH));
        helpView = (ActivityHelpView) findViewById(R.id.HelpViewLayout);
        helpViewWoColorIcon = (ActivityHelpViewWoColorIcon) findViewById(R.id.HelpViewWoColorIconLayout);//for largest display size

        if(mParent == 0) {//Easy Install
            helpViewWoColorIcon.setVisibility(View.INVISIBLE);//for largest display size
//            helpView.resetHelp(1, R.drawable.help_red, getResources().getString(R.string.STR_PREVIOUS));
//            helpView.resetHelp(2, R.drawable.help_blue,getResources().getString(R.string.STR_NEXT));
            helpView.resetHelp(1, 0, null);
            helpView.resetHelp(2, 0, null);
            helpView.resetHelp(3, 0, null);
            helpView.resetHelp(4, 0, null);

            // Johnny 20181228 for mouse control -s
            helpView.setHelpIconClickListener(1, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                    Intent intent = new Intent();
                    intent.setClass(ChannelSearchActivity.this, FirstTimeInstallationActivity.class);
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

            helpView.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));
        }else if(mParent == 1) {//Main Menu
            helpView.setVisibility(View.INVISIBLE);//for largest display size
            helpViewWoColorIcon.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));
        }

        SpinnerFreq = (Spinner) findViewById(R.id.freqSPINNER);
        SpinnerQam = (Spinner) findViewById(R.id.qamSPINNER);
        SpinnerScanMode = (Spinner) findViewById(R.id.ScanModeSPINNER);
        SpinnerChannelType = (Spinner) findViewById(R.id.channelTypeSPINNER);
        EditSymbolrate = (EditText) findViewById(R.id.symbolEDV);
        ProgressStrength =  (ProgressBar) findViewById(R.id.strengthPROGBAR);
        ProgressQuality =  (ProgressBar) findViewById(R.id.qualityPROGBAR);
        QualityValue = (TextView) findViewById(R.id.qualityValueTXV);
        StrengthValue = (TextView) findViewById(R.id.strengthValueTXV);
        BerValue = (TextView) findViewById(R.id.berValueTXV);
        ButtonSearch = (Button) findViewById(R.id.searchBTN);
        if(mParent == 0) //Easy Install
            ButtonSearch.setVisibility(View.INVISIBLE);


        TextFreq = (TextView) findViewById(R.id.freqTXV);
        TextSymbol = (TextView) findViewById(R.id.symbolTXV);
        TextQam = (TextView) findViewById(R.id.qamTXV);

        TextScanMode = (TextView) findViewById(R.id.scanModeTXV);
        TextChannelType = (TextView) findViewById(R.id.channelTypeTXV);

        str = getString(R.string.STR_FREQUENCY)+" : ";
        TextFreq.setText(str);
        str=getString(R.string.STR_SYMBOLRATE)+" : ";
        TextSymbol.setText(str);
        str=getString(R.string.STR_QAM)+" : ";
        TextQam.setText(str);

        str=getString(R.string.STR_SCAN_MODE)+" : ";
        TextScanMode.setText(str);
        str=getString(R.string.STR_CHANNEL_TYPE)+" : ";
        TextChannelType.setText(str);

        SearchModeList.add(TVScanParams.SCAN_MODE_AUTO);
        SearchModeList.add(TVScanParams.SCAN_MODE_MANUAL);
        SearchModeList.add(TVScanParams.SCAN_MODE_NETWORK);

        tpList = TpInfoGetList(GetCurTunerType());

        if(tpList==null)
            tpList = new ArrayList<>();

        //Log.d(TAG, "size " + tpList.size() );
        strFreqList = new String [tpList.size()];
        for(int i = 0; i< tpList.size(); i++) {
            freq = (tpList.get(i)).CableTp.getFreq();
            strFreqList[i] = Integer.toString(freq / 1000); // Johnny 20190508 modify DVBC freq shown in UI from KHz to MHz
            Log.d(TAG, "tp id = " + tpList.get(i).getTpId() + "     freq = " + tpList.get(i).CableTp.getFreq() +
                                "    Symbol = " + tpList.get(i).CableTp.getSymbol() );
        }

        // Setting  Freq
        new SelectBoxView(this, SpinnerFreq, strFreqList);
        SpinnerFreq.setOnItemSelectedListener(FreqListener);
        if(ViewHistory.getCurChannel() != null && ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.DVB_TYPE) {
            ProgramInfo programInfo = ProgramInfoGetByChannelId(ViewHistory.getCurChannel().getChannelId());
            TpInfo cutTpInfo = TpInfoGet(programInfo.getTpId());
            // Change Lock !!!!
            for (int i = 0; i < tpList.size(); i++) {
                if (cutTpInfo.CableTp.getFreq() == tpList.get(i).CableTp.getFreq()) {
                    position = i;
                    break;
                }
            }
        }
        SpinnerFreq.setSelection(position);

        // Setting  Symborate
        hiddenSoftInputFromWindow(ChannelSearchActivity.this, EditSymbolrate);//eric lin 20180606 disable symbol rate's virtual keyboard
        EditSymbolrate.setFocusable(false);
        EditSymbolrate.setEnabled(false);   // Johnny 20181219 for mouse control
        EditSymbolrate.setOnKeyListener(new View.OnKeyListener(){//eric lin 20180606 disable symbol rate's virtual keyboard
            public boolean onKey(View v, int keyCode, KeyEvent event){
                boolean isActionDown = (event.getAction() == KeyEvent.ACTION_DOWN);
                EditText etSymbolRate = (EditText) v;
                if (isActionDown)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_0:
                            boolean isStartEqualEnd = EditSymbolrate.getSelectionStart() == EditSymbolrate.getSelectionEnd() ? true: false;
                            if(!isStartEqualEnd || EditSymbolrate.getText().toString().isEmpty())
                                return true;
                            break;
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                            if(EditSymbolrate.getText().length()==4)
                                SpinnerQam.requestFocus();
                            return true;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            SpinnerQam.requestFocus();
                            return true;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            SpinnerFreq.requestFocus();
                            return true;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                    deleteEditText(EditSymbolrate);
                    return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
        EditSymbolrate.setOnFocusChangeListener(symbol_rate_change_focus);

        // setting QAM
        String[] str_qam_list = getResources().getStringArray(R.array.STR_ARRAY_QAMLIST);
        SelectQam = new SelectBoxView(this, SpinnerQam, str_qam_list);
        SpinnerQam.setFocusable(false);
        SpinnerQam.setEnabled(false);   // Johnny 20181219 for mouse control
        SpinnerQam.setOnItemSelectedListener(QamListener);

        // Scan Mode
        String[] scanModeList = getResources().getStringArray(R.array.STR_ARRAY_SCAN_MODE);
        SelectScanMode = new SelectBoxView(this, SpinnerScanMode, scanModeList);

        // Channel Type
        String[] chTypeList = getResources().getStringArray(R.array.STR_ARRAY_CHANNEL_TYPE);
        SelectChannelType = new SelectBoxView(this, SpinnerChannelType, chTypeList);

        // search Button
        if(mParent == 1) {//Main Menu
            ButtonSearch.setText(getString(R.string.STR_AUTO_SEARCH));
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
        //Log.d(TAG, "onActivityResult: requestCode = " + requestCode+" resultCode = "+resultCode + " data = "+data);
        super.onActivityResult(requestCode, resultCode, data);
        if ( resultCode == ScanResultActivity.RESULT_SEARCH_FAIL )
        {
            Log.d(TAG, "onActivityResult: RESULT_SEARCH_FAIL");
            mSearchFail.show();
        }
        if(resultCode == 0) { //gary20200619 fix from view press back not to pesi lanucher after search channel then view
            Log.d(TAG, "onActivityResult: finish channel search activity");
            finish();
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
            Frequency = tpList.get(position).CableTp.getFreq();
            Symbolrate = tpList.get(position).CableTp.getSymbol();
            Qam = tpList.get(position).CableTp.getQam();
            tpID = tpList.get(position).getTpId();

            str = Integer.toString(Symbolrate);
            EditSymbolrate.setText(str);
            SelectQam.SetSelectedItemIndex(Qam);

            Log.d(TAG, "onItemSelected:    Frequency   = " + Frequency + "    Symbolrate = " + Symbolrate + "    Qam = " + Qam);
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private View.OnFocusChangeListener symbol_rate_change_focus = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                // Set Help                
                    helpView.setHelpInfoTextBySplit(getString(R.string.STR_BUTTON_HELP));
                    helpViewWoColorIcon.setHelpInfoTextBySplit(getString(R.string.STR_BUTTON_HELP));
                    EditSymbolrate.selectAll();
            }
            else {
                String NewStr = EditSymbolrate.getText().toString();
                int NewSymbol;
                if(NewStr.length() == 4) {
                    NewSymbol = Integer.parseInt(NewStr);
                    if(NewSymbol != Symbolrate) {
                        Symbolrate = NewSymbol;
                        Log.d(TAG, "onFocusChange:     symbol change ! new symbol  = " + Symbolrate);
                    }
                }
                else
                {
                    EditSymbolrate.setText(String.valueOf(Symbolrate));
                }
                    helpView.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));
                    helpViewWoColorIcon.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));

            }
        }
    };

    private AdapterView.OnItemSelectedListener QamListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            Qam = position;
            Log.d(TAG, "freq = " +Frequency+ "   symbol rate = " + Symbolrate+ "   Qam = " + Qam );
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private View.OnFocusChangeListener SearchFocusListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {

            if (hasFocus) {
                // Set Help                
                    helpViewWoColorIcon.setHelpInfoTextBySplit(getString(R.string.STR_SEARCH_BUTTON_HELPTEXT));
            }
            else
            {
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
        int Quality;
        int Strength;
        int lock;
        int barcolor ;
        String str;
        String ber;

        lock = TunerGetLockStatus(0);
        Quality = TunerGetQuality(0);
        Strength = TunerGetStrength(0);
        ber = TunerGetBER(0);

        //Log.d(TAG, "UpdateSignalLevel  Strengh = " + Strength + "Quality = " + Quality);
        str = Integer.toString(Strength)+" %";
        StrengthValue.setText(str);
        str = Integer.toString(Quality)+" %";
        QualityValue.setText(str);
//        BerValue.setText(ber);

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
        int freqIndex = SpinnerFreq.getSelectedItemPosition();
        if(Frequency == tpList.get(freqIndex).CableTp.getFreq()) // Freq  Change !
        {
            if(Symbolrate != tpList.get(freqIndex).CableTp.getSymbol())  // Symbol  Edit !
                tpList.get(freqIndex).CableTp.setSymbol(Symbolrate);
            if(Qam != tpList.get(freqIndex).CableTp.getQam()) // Qam Edit !
                tpList.get(freqIndex).CableTp.setQam(Qam);

            TpInfoUpdate(tpList.get(freqIndex));
            //TpInfoSaveList(tpList);  
        }

        ScanMode = SelectScanMode.GetSelectedItemIndex();
        ChannelType = SelectChannelType.GetSelectedItemIndex();
        Intent intent = new Intent();
        intent.setClass(ChannelSearchActivity.this, ScanResultActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt(getString(R.string.STR_EXTRAS_TPID), tpID);
        bundle.putInt(getString(R.string.STR_EXTRAS_SEARCHMODE), SearchMode); // 0 : manaul  1 : auto 2 : network 3 : all sat
        bundle.putInt(getString(R.string.STR_EXTRAS_SCANMODE), ScanMode); // 0 : All  1 : FTA  2 : $
        bundle.putInt(getString(R.string.STR_EXTRAS_CHANNELTYPE), ChannelType); // 0 : All  1 : TV  2 : Radio
        //bundle.putInt(getString(R.string.STR_EXTRA_NIT_SEARCH),NitSearch);

        intent.putExtras(bundle);
        startActivityForResult(intent, 0); // Edwin 20181210 add search fail message

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
                    intent.setClass(ChannelSearchActivity.this, FirstTimeInstallationActivity.class);
                    startActivity(intent);
                }break;
                default:
                    break;
            }
        }
        else if(mParent == 1) {//Main Menu
            switch (keyCode) {
                  //Scoty 20180809 add fake tuner command
//                case KeyEvent.KEYCODE_PROG_RED:
//                case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
//                    setFakeTuner(1);//0:normal 1:fake tuner
//                    break;
//                case KeyEvent.KEYCODE_PROG_GREEN:
//                case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
//                    setFakeTuner(0);//0:normal 1:fake tuner
//                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:{
                    if(ButtonSearch.hasFocus()) {
                        if(SearchModeList.indexOf(SearchMode) <= 0)
                            SearchMode = SearchModeList.get(SearchModeList.size()-1);
                        else
                            SearchMode = SearchModeList.get(SearchModeList.indexOf(SearchMode)-1);
                        changeSearchMode();
                        return true;
                    }
                }break;
                case KeyEvent.KEYCODE_DPAD_RIGHT: {
                    if(ButtonSearch.hasFocus()) {
                        Log.d(TAG, "str = " + ButtonSearch.getText());
                        if(SearchModeList.indexOf(SearchMode) >= (SearchModeList.size() -1))
                            SearchMode = SearchModeList.get(0);
                        else
                            SearchMode = SearchModeList.get(SearchModeList.indexOf(SearchMode)+1);
                        changeSearchMode();
                        return true;
                    }
                } break;
                case KeyEvent.KEYCODE_BACK:{
                }break;
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

    private boolean deleteEditText(EditText et) {//eric lin 20180606 disable symbol rate's virtual keyboard
        int position = et.getSelectionStart();
        Editable editable = et.getText();

        if (position > 0) {
            editable.delete(position - 1, position);
            return true;
        } else {
            return false; //delete fail
        }
    }

    private void hiddenSoftInputFromWindow(Activity activity, EditText editText) {//eric lin 20180606 disable symbol rate's virtual keyboard
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            editText.setInputType(InputType.TYPE_NULL);
        } else {
            activity.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus",
                        boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(editText, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        String strQam = SpinnerQam.getSelectedItem().toString();
        strQam = strQam.replaceAll(" ", "");
        FileName = SpinnerFreq.getSelectedItem().toString() + "_" + EditSymbolrate.getText()+"_"+ strQam + "_" + curTime + ".ts";
        fullName = GetRecordPath()+"/Records/"+FileName;
        RecordTSDialog recDurDialog = new RecordTSDialog(ChannelSearchActivity.this, this, tunerId, fullName );
        return ;
    }

    // Johnny 20181219 for mouse control
    private void changeSearchMode() {
        //NitSearch = 0;
        if(SearchMode == TVScanParams.SCAN_MODE_AUTO) {
            ButtonSearch.setText(R.string.STR_AUTO_SEARCH);
            SearchMode = TVScanParams.SCAN_MODE_AUTO;
            EditSymbolrate.setFocusable(false);
            EditSymbolrate.setEnabled(false);   // Johnny 20181219 for mouse control
            SpinnerQam.setFocusable(false);
            SpinnerQam.setEnabled(false);   // Johnny 20181219 for mouse control
        }
        else if(SearchMode == TVScanParams.SCAN_MODE_MANUAL){
            ButtonSearch.setText(R.string.STR_MANUAL_SEARCH);
            SearchMode = TVScanParams.SCAN_MODE_MANUAL ;
            EditSymbolrate.setFocusable(true);
            EditSymbolrate.setFocusableInTouchMode(true);   // Johnny 20181219 for mouse control
            EditSymbolrate.setEnabled(true);    // Johnny 20181219 for mouse control
            SpinnerQam.setFocusable(true);
            SpinnerQam.setFocusableInTouchMode(true);   // Johnny 20181219 for mouse control
            SpinnerQam.setEnabled(true);   // Johnny 20181219 for mouse control
        }
        else if(SearchMode == TVScanParams.SCAN_MODE_NETWORK) {
            ButtonSearch.setText(R.string.STR_DVBS_NETWORK_SEARCH);
            SearchMode = TVScanParams.SCAN_MODE_NETWORK;
            EditSymbolrate.setFocusable(false);
            EditSymbolrate.setEnabled(false);   // Johnny 20181219 for mouse control
            SpinnerQam.setFocusable(false);
            SpinnerQam.setEnabled(false);   // Johnny 20181219 for mouse control
            //NitSearch = 1;
        }
        else {
            ButtonSearch.setText(R.string.STR_MANUAL_SEARCH);
            SearchMode = TVScanParams.SCAN_MODE_MANUAL ;
            EditSymbolrate.setFocusable(true);
            EditSymbolrate.setFocusableInTouchMode(true);   // Johnny 20181219 for mouse control
            EditSymbolrate.setEnabled(true);    // Johnny 20181219 for mouse control
            SpinnerQam.setFocusable(true);
            SpinnerQam.setFocusableInTouchMode(true);   // Johnny 20181219 for mouse control
            SpinnerQam.setEnabled(true);   // Johnny 20181219 for mouse control
        }
    }
}
