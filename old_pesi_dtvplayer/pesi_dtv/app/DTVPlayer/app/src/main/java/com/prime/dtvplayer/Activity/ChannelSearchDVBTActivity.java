package com.prime.dtvplayer.Activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.TvInput.TvInputActivity;
import com.dolphin.dtv.HiDtvMediaPlayer;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityHelpViewWoColorIcon;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.SelectBoxView;
import com.prime.dtvplayer.utils.TVMessage;

import java.util.ArrayList;
import java.util.List;

public class ChannelSearchDVBTActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    private ActivityTitleView titleView;
    private ActivityHelpView helpView;
    private ActivityHelpViewWoColorIcon helpViewWoColorIcon;
    Spinner SpinnerChannel;
    //Spinner SpinnerQam;
    Spinner SpinnerScanMode;
    Spinner SpinnerChannelType;
    Spinner SpinnerAntenna5V;
    //EditText EditFrequency;
    Button ButtonSearch;
    ProgressBar ProgressStrength;
    ProgressBar ProgressQuality;
    TextView QualityValue;
    TextView StrengthValue;
    TextView BerValue;
    TextView TextChannel;
    TextView TextFrequency;
    TextView TextFrequencyValue;
    //TextView TextQam;
    TextView TextScanMode;
    TextView TextChannelType;
    TextView TextAntenna5V;
    SelectBoxView SelectScanMode;
    SelectBoxView SelectChannelType;
    SelectBoxView SelectAntenna5V;

    // Edwin 20181129 add search fail message
    MessageDialogView mSearchFail;

    int Channel = 0, Frequency = 0, Band =0; //, Symbolrate = 0, Qam = 0;
    int tuneChannel =-1, tuneFreq = 0, tuneBand =0; //, tuneSymbol = 0, tuneQam = 0;
    int tpID = 0;

    int searchMode = 0; // 0 : auto   1: manual
    int ScanMode = 0;
    int ChannelType = 0;
    int Antenna5V=0;
    int mParent=1; //0:EasyInstall, 1:MainMenu

    private List<TpInfo> tpList = new ArrayList<>(); 

    Handler  CheckSignalHandler;
    Handler  ChangeParamHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_search_dvbt);
        if(AvControlGetPlayStatus(ViewHistory.getPlayId()) == HiDtvMediaPlayer.EnPlayStatus.LIVEPLAY.getValue()) {
            AvControlPlayStop(ViewHistory.getPlayId());
        }

        Bundle bundle = this.getIntent().getExtras();
        String parent = bundle.getString("parent");
        if(parent.equals("EasyInstall")) {
            mParent = 0;
        }
        else if(parent.equals("MainMenu")) {
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
            //Log.d(TAG, "run:     Auto  CheckStatusRunnable !!!!");
            UpdateSignalLevel();
            CheckSignalHandler.postDelayed(CheckStatusRunnable, 1000);
        }
    };
    final Runnable ParamChangeRunnable = new Runnable() {
        public void run() {
            int changeLock = 0;
            int channelIndex = SpinnerChannel.getSelectedItemPosition();

            //Log.d(TAG, "ParamChangeRunnable: channelIndex:"+channelIndex);//eric lin test
            TpInfo tpInfo = TpInfoGet(tpList.get(channelIndex).getTpId());
            if(tuneChannel != channelIndex) // Channel  Change !
            {
                Log.d(TAG, "Channel CHANGE!!!!!!!!!!!  tuneChannel="+tuneChannel+", channelIndex="+channelIndex);
                tuneFreq = tpList.get(channelIndex).TerrTp.getFreq();
                tuneBand =tpList.get(channelIndex).TerrTp.getBand();
                changeLock = 1;
                tuneChannel = channelIndex;
            }

            if(Frequency != tuneFreq)  // Symbol  Edit !
            {
                Log.d(TAG, "Frequency CHANGE!!!!!!!!!!!  tuneFreq="+tuneFreq+", Frequency="+Frequency);
                tuneFreq = Frequency;
                changeLock = 1;
            }

            if(changeLock == 1)
            {
                TunerTuneDVBT( 0, tpID, Frequency, Band);//eric lin need
                //Log.d(TAG, "param Save   : TPID = " + tpID);
                //Log.d(TAG, "param Save   : Freq = " + tpList.get(channelIndex).TerrTp.getFreq());
                //Log.d(TAG, "param Save   : bandwidth = " + tpList.get(channelIndex).TerrTp.getBand());
            }
            ChangeParamHandler.postDelayed(ParamChangeRunnable, 1000);
        }
    };

    private void ItemInit() {
        int channel=0,position = 0;
        String str;
        String[] strChannelList = null;
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
                    intent.setClass(ChannelSearchDVBTActivity.this, FirstTimeInstallationActivity.class);
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
            helpView.setVisibility(View.INVISIBLE); //for largest display size
            helpViewWoColorIcon.setHelpInfoTextBySplit(null); //for largest display size
        }

        
//        SpannableStringBuilder style = new SpannableStringBuilder(getString(R.string.STR_PARENT_HELP_INFO));
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 4, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 15, 17, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 28, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 50, 54, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if(mParent == 0) //Easy Install //for largest display size
        helpView.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));
        else if(mParent == 1) //for largest display size
            helpViewWoColorIcon.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));
        SpinnerChannel = (Spinner) findViewById(R.id.channelSPINNER);
        //SpinnerQam = (Spinner) findViewById(R.id.qamSPINNER);
        SpinnerScanMode = (Spinner) findViewById(R.id.ScanModeSPINNER);
        SpinnerChannelType = (Spinner) findViewById(R.id.channelTypeSPINNER);
        SpinnerAntenna5V = (Spinner) findViewById(R.id.antenna5VSPINNER);
        //EditFrequency = (EditText) findViewById(R.id.freqEDV);
        TextFrequencyValue = (TextView) findViewById(R.id.freqValueTXV);
        ProgressStrength =  (ProgressBar) findViewById(R.id.strengthPROGBAR);
        ProgressQuality =  (ProgressBar) findViewById(R.id.qualityPROGBAR);
        QualityValue = (TextView) findViewById(R.id.qualityValueTXV);
        StrengthValue = (TextView) findViewById(R.id.strengthValueTXV);
        BerValue = (TextView) findViewById(R.id.berValueTXV);
        ButtonSearch = (Button) findViewById(R.id.searchBTN);

        TextChannel = (TextView) findViewById(R.id.channelTXV);
        TextFrequency = (TextView) findViewById(R.id.freqTXV);
        //TextQam = (TextView) findViewById(R.id.qamTXV);
        TextScanMode = (TextView) findViewById(R.id.scanModeTXV);
        TextChannelType = (TextView) findViewById(R.id.channelTypeTXV);
        TextAntenna5V = (TextView) findViewById(R.id.antenna5VTXV);

        str = getString(R.string.STR_CHANNEL)+" : ";
        TextChannel.setText(str);
        str=getString(R.string.STR_FREQUENCY)+" : ";
        TextFrequency.setText(str);
        //str=getString(R.string.STR_QAM)+" : ";
        //TextQam.setText(str);
        str=getString(R.string.STR_SCAN_MODE)+" : ";
        TextScanMode.setText(str);
        str=getString(R.string.STR_CHANNEL_TYPE)+" : ";
        TextChannelType.setText(str);
        str="Antenna 5V"+" : ";
        TextAntenna5V.setText(str);

        if(tpList==null)
            tpList = new ArrayList<TpInfo>();

        tpList = TpInfoGetList(GetCurTunerType());

        Log.d(TAG, "size " + tpList.size() );
        strChannelList = new String [tpList.size()];
        for(int i = 0; i< tpList.size(); i++) {
            channel = (tpList.get(i)).TerrTp.getChannel();
            strChannelList[i] =Integer.toString(channel);
            Log.d(TAG, "tp id = " + tpList.get(i).getTpId() + "     freq = " + tpList.get(i).TerrTp.getFreq() +
                    "    Bandwidth = " + tpList.get(i).TerrTp.getBand() );
        }

        // Setting  Channel
        ArrayAdapter<String> adapter_channel = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, strChannelList);
        SelectBoxView ChannelList = new SelectBoxView(this, SpinnerChannel, strChannelList);
        SpinnerChannel.setOnItemSelectedListener(ChannelListener);

        if(ViewHistory.getCurChannel() != null) {
            ProgramInfo programInfo = ProgramInfoGetByChannelId(ViewHistory.getCurChannel().getChannelId());
            TpInfo cutTpInfo = TpInfoGet(programInfo.getTpId());
            // Change Lock !!!!
            if(cutTpInfo != null) {
            for (int i = 0; i < tpList.size(); i++) {
                if (cutTpInfo.TerrTp.getFreq() == tpList.get(i).TerrTp.getFreq()) {
                    position = i;
                    break;
                }
            }
        }
        }
        SpinnerChannel.setSelection(position);
        // Setting  Freq
        //EditFrequency.setFocusable(false);
        //EditFrequency.setOnFocusChangeListener(frequency_change_focus);

        // Scan Mode
        String[] scanModeList = getResources().getStringArray(R.array.STR_ARRAY_SCAN_MODE);
        ArrayAdapter<String> adapter_scanMode = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, scanModeList);
        SelectScanMode = new SelectBoxView(this, SpinnerScanMode, scanModeList);

        // Chaanel Type
        String[] chTypeList = getResources().getStringArray(R.array.STR_ARRAY_CHANNEL_TYPE);
        ArrayAdapter<String> adapter_chType = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, chTypeList);
        SelectChannelType = new SelectBoxView(this, SpinnerChannelType, chTypeList);

        // Antenna 5V
        String[] antenna5VList = getResources().getStringArray(R.array.STR_ARRAY_ANTENNA_5V);
        ArrayAdapter<String> adapter_antenna5V = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, antenna5VList);
        SelectAntenna5V = new SelectBoxView(this, SpinnerAntenna5V, antenna5VList);
        SpinnerAntenna5V.setOnItemSelectedListener(Antenna5VListener);

        // search Button
        if(mParent == 0) { //Easy Install
            ButtonSearch.setVisibility(View.INVISIBLE);
        }
        else
        {
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
        super.onActivityResult(requestCode, resultCode, data);
        if ( resultCode == ScanResultActivity.RESULT_SEARCH_FAIL )
        {
            Log.d(TAG, "onActivityResult: RESULT_SEARCH_FAIL");
            mSearchFail.show();
        }
    }

    private AdapterView.OnItemSelectedListener ChannelListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            String str;
            // Change Lock !!!!
            //Log.d(TAG, "position =  " + position);
            Log.d(TAG, "ChannelListener: position="+position);
            Channel = tpList.get(position).TerrTp.getChannel();
            Frequency = tpList.get(position).TerrTp.getFreq();
            Band = tpList.get(position).TerrTp.getBand();
            tpID = tpList.get(position).getTpId();

            str = Integer.toString(Frequency);
            TextFrequencyValue.setText(str);

            Log.d(TAG, "onItemSelected: Channel="+Channel+", tpId="+tpID+", freq="+Frequency+", Band="+Band);
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
            String str;
            GposInfo gpos = GposInfoGet();

            //Log.d(TAG, "position =  " + position);
            Log.d(TAG, "Antenna5VListener: position="+position);

            if(position == 0) {
                gpos.setLnbPower(0);
                TunerSetAntenna5V(0, 0);    // Johnny add 20180124
            }
            else if(position == 1) {
                gpos.setLnbPower(1);
                TunerSetAntenna5V(0, 1);    // Johnny add 20180124
            }
            GposInfoUpdate(gpos);

            Log.d(TAG, "onItemSelected: Antenna5V="+position);
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    /*
    private View.OnFocusChangeListener frequency_change_focus = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {

            if (hasFocus) {
                // Set Help
                SpannableStringBuilder style = new SpannableStringBuilder(getString(R.string.STR_BUTTON_HELP));
                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 4, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                helpView.setHelpInfoText(style,getString(R.string.STR_BUTTON_HELP));
            }
            else {
                String NewStr = EditFrequency.getText().toString();
                int NewFreq=0;
                Log.d(TAG, "frequency_change_focus: no hasFocus, NewStr"+NewStr);//eric lin test
                if(NewStr.length() >= 5 &&  NewStr.length() <= 6) {
                    NewFreq = Integer.parseInt(NewStr);
                    if(NewFreq != Frequency) {
                        Frequency = NewFreq;
                        Log.d(TAG, "onFocusChange:     frequency change ! new frequency  = " + Frequency);
                    }
                }
                else
                {
                    EditFrequency.setText(Integer.toString(Frequency));
                }

                SpannableStringBuilder style = new SpannableStringBuilder(getString(R.string.STR_PARENT_HELP_INFO));
                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 4, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 15, 17, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 28, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 50, 54, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                helpView.setHelpInfoText(style,getString(R.string.STR_PARENT_HELP_INFO));
            }
        }
    };
    */


    private View.OnFocusChangeListener SearchFocusListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {

            if (hasFocus) {
                // Set Help
                SpannableStringBuilder style = new SpannableStringBuilder(getString(R.string.STR_SEARCH_BUTTON_HELPTEXT));
                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 4, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 38, 40, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                style.setSpan(new ForegroundColorSpan(Color.YELLOW), 60, 64, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if(mParent == 0) //Easy Install //for largest display size
                helpView.setHelpInfoText(style,getString(R.string.STR_SEARCH_BUTTON_HELPTEXT));
                else if(mParent == 1) //for largest display size
                    helpViewWoColorIcon.setHelpInfoText(style,getString(R.string.STR_SEARCH_BUTTON_HELPTEXT));
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
                else if(mParent == 1) //for largest display size
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
        int channelIndex = SpinnerChannel.getSelectedItemPosition();
        if(Channel == tpList.get(channelIndex).TerrTp.getChannel()) // Freq  Change !
        {
            if(Frequency != tpList.get(channelIndex).TerrTp.getFreq())  // Frequency  Edit !
                tpList.get(channelIndex).TerrTp.setFreq(Frequency);
            TpInfoUpdateList(tpList);
        }

        ScanMode = SelectScanMode.GetSelectedItemIndex();
        ChannelType = SelectChannelType.GetSelectedItemIndex();
        Antenna5V = SelectAntenna5V.GetSelectedItemIndex(); //eric lin need check

        Intent intent = new Intent();
        intent.setClass(ChannelSearchDVBTActivity.this, ScanResultActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt("tp_id", tpID);
        bundle.putInt("search_mode", searchMode); // 0 : manaul  1 : auto
        bundle.putInt("scan_mode", ScanMode); // 0 : All  1 : FTA  2 : $
        bundle.putInt("channel_type", ChannelType); // 0 : All  1 : TV  2 : Radio
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
                    intent.setClass(ChannelSearchDVBTActivity.this, FirstTimeInstallationActivity.class);
                    startActivity(intent);
                }break;
                default:
                    break;
            }
        }
        else if (mParent == 1)
        {
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
        }

        return super.onKeyDown(keyCode, event);
    }

    // Johnny 20181219 for mouse control
    private void changeSearchMode() {
        if(searchMode == 0) {
            ButtonSearch.setText(R.string.STR_MANUAL_SEARCH);
            searchMode = 1 ;
            //EditSymbolrate.setFocusable(true);
            //SpinnerQam.setFocusable(true);
        }
        else {
            ButtonSearch.setText(R.string.STR_AUTO_SEARCH);
            searchMode = 0;
            //EditSymbolrate.setFocusable(false);
            //SpinnerQam.setFocusable(false);
        }
    }
}
