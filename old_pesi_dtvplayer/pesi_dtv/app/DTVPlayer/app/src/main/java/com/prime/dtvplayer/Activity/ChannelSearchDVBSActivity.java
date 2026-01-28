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
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.SureDialog;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.DVBSTpDialogView;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.RecordTSDialog;
import com.prime.dtvplayer.View.SelectBoxView;
import com.prime.dtvplayer.utils.TVMessage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_9;
import static com.dolphin.dtv.HiDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS;


public class ChannelSearchDVBSActivity extends DTVActivity implements DVBSTpDialogView.NoticeDialogListener {
    private final String TAG = getClass().getSimpleName();

    private ActivityTitleView titleView;
    private ActivityHelpView helpView;
    Spinner SpinnerSat;
    Spinner SpinnerTpList;
    Spinner SpinnerScanMode;
    Spinner SpinnerChannelType;

    Button ButtonSearch;
    ProgressBar ProgressStrength;
    ProgressBar ProgressQuality;
    TextView QualityValue;
    TextView StrengthValue;
    TextView BerValue;
    TextView TextSat;
    TextView TextTpList;
    TextView TextScanMode;
    TextView TextChannelType;

    TextView TextLnbType;
    TextView TextAntType;
    TextView TextTone22k;
    TextView TextDiSEqC;
    TextView TextLnbTypeValue;
    TextView TextAntTypeValue;
    TextView TextTone22kValue;
    TextView TextDiSEqCValue;

    SelectBoxView SelectSat;
    SelectBoxView SelectTpList;
    SelectBoxView SelectScanMode;
    SelectBoxView SelectChannelType;

    // Edwin 20181129 add search fail message
    MessageDialogView mSearchFail;

    int freq = 0, symbol = 0, polar = 0;    // Johnny 20180813 modify DVBS tune change rule
    int tuneFreq = -1, tuneSymbol = -1, tunePolar = -1; // init -1 for first tune   // Johnny 20180813 modify DVBS tune change rule
    int tpID = 0;
    int tuneTpID = -1;  // init -1 for first tune   // Johnny 20180813 modify DVBS tune change rule

    int ScanMode = 0;   // 0 : fta & $, 1 : fta, 2 : $
    int ChannelType = 0; // 0 : TV & Radio,  1 : TV, 2 : Radio
    int searchMode = 0; //
    int mParent=1; //0:EasyInstall, 1:MainMenu

    private List<TpInfo> tpList = null;
    private List<SatInfo> satList = null;
//    private List<AntInfo> antList = null;

    Handler  CheckSignalHandler;
    Handler  ChangeParamHandler;

    String[] searchButtonItems;
    String[] lnbTypeItems;
    String[] antTypeItems;
    String[] tone22kItems;
    String[] diseqcItems;

    private String keystr = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_search_dvbs);
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
//            Log.d(TAG, "run:     Auto  CheckStatusRunnable !!!!");
            UpdateSignalLevel();
            CheckSignalHandler.postDelayed(CheckStatusRunnable, 1000);
        }
    };
    final Runnable ParamChangeRunnable = new Runnable() {
        public void run() {
            boolean changeLock = false;

            if(tpID != tuneTpID || freq != tuneFreq || symbol != tuneSymbol || polar != tunePolar) // tune  Change !
            {
                tuneTpID = tpID;
                tuneFreq = freq;
                tuneSymbol = symbol;
                tunePolar = polar;
                changeLock = true;
            }

            if(changeLock)
            {
                TunerTuneDVBS(0, tuneTpID, tuneFreq, tuneSymbol, tunePolar);
                Log.d(TAG, "TUNE TP CHANGE!!!!!!!!!!!  tuneTpID = " + tuneTpID + " tuneFreq = " + tuneFreq + "  tuneSymbol = " + tuneSymbol + "  tunePolar = " + tunePolar);
            }

            ChangeParamHandler.postDelayed(ParamChangeRunnable, 1000);
        }
    };

    private void ItemInit() {
        String str;
        String[] strSatList;
        String[] strTpList;
        titleView = (ActivityTitleView) findViewById(R.id.TitleViewLayout);
        if (mParent == 0) { //Easy Install
            titleView.setTitleView(getString(R.string.STR_TITLE_EASY_CHANNEL_SEARCH));
        } else if (mParent == 1) {//Main Menu
            titleView.setTitleView(getString(R.string.STR_TITLE_CHANNEL_SEARCH));
        }

        helpView = (ActivityHelpView) findViewById(R.id.HelpViewLayout);

        if (mParent == 0) {//Easy Install
            helpView.setHelpInfoTextBySplit(null);
            helpView.resetHelp(1, R.drawable.help_red, getResources().getString(R.string.STR_PREVIOUS));
            helpView.resetHelp(2, R.drawable.help_blue, getResources().getString(R.string.STR_NEXT));
            helpView.resetHelp(3, 0, null);
            helpView.resetHelp(4, 0, null);

            // Johnny 20181228 for mouse control -s
            helpView.setHelpIconClickListener(1, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                    Intent intent = new Intent();
                    intent.setClass(ChannelSearchDVBSActivity.this, FirstTimeInstallationActivity.class);
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
        } else if (mParent == 1) {//Main Menu
            helpView.setHelpInfoTextBySplit(null);
            helpView.resetHelp(1, 0, null);
            helpView.resetHelp(2, 0, null);
            helpView.resetHelp(3, 0, null);
            helpView.resetHelp(4, 0, null);
        }

        helpView.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));

        SpinnerSat = (Spinner) findViewById(R.id.satSPINNER);
        SpinnerTpList = (Spinner) findViewById(R.id.tpListSPINNER);
        SpinnerScanMode = (Spinner) findViewById(R.id.scanModeSPINNER);
        SpinnerChannelType = (Spinner) findViewById(R.id.channelTypeSPINNER);

        ProgressStrength = (ProgressBar) findViewById(R.id.strengthPROGBAR);
        ProgressQuality = (ProgressBar) findViewById(R.id.qualityPROGBAR);
        QualityValue = (TextView) findViewById(R.id.qualityValueTXV);
        StrengthValue = (TextView) findViewById(R.id.strengthValueTXV);
        BerValue = (TextView) findViewById(R.id.berValueTXV);
        ButtonSearch = (Button) findViewById(R.id.searchBTN);

        TextSat = (TextView) findViewById(R.id.satTXV);
        TextTpList = (TextView) findViewById(R.id.tpListTXV);
        TextScanMode = (TextView) findViewById(R.id.scanModeTXV);
        TextChannelType = (TextView) findViewById(R.id.channelTypeTXV);

        TextLnbType = (TextView) findViewById(R.id.lnbTypeTXV);
        TextAntType = (TextView) findViewById(R.id.antTypeTXV);
        TextTone22k = (TextView) findViewById(R.id.tone22kTXV);
        TextDiSEqC = (TextView) findViewById(R.id.diseqcTXV);
        TextLnbTypeValue = (TextView) findViewById(R.id.lnbTypeValueTXV);
        TextAntTypeValue = (TextView) findViewById(R.id.antTypeValueTXV);
        TextTone22kValue = (TextView) findViewById(R.id.tone22kValueTXV);
        TextDiSEqCValue = (TextView) findViewById(R.id.diseqcValueTXV);

        str = getString(R.string.STR_DVBS_SATELLITE) + " : ";
        TextSat.setText(str);
        str = getString(R.string.STR_DVBS_TP_LIST) + " : ";
        TextTpList.setText(str);
        str = getString(R.string.STR_SCAN_MODE) + " : ";
        TextScanMode.setText(str);
        str = getString(R.string.STR_CHANNEL_TYPE) + " : ";
        TextChannelType.setText(str);

        str = getString(R.string.STR_DVBS_LNB_TYPE) + " : ";
        TextLnbType.setText(str);
        str = getString(R.string.STR_DVBS_ANT_TYPE) + " : ";
        TextAntType.setText(str);
        str = getString(R.string.STR_DVBS_22KHZ_TONE) + " : ";
        TextTone22k.setText(str);
        str = getString(R.string.STR_DVBS_DISEQC) + " : ";
        TextDiSEqC.setText(str);

        // get correct Sats for DVBS
        satList = GetSatListForDVBS();

        // Set strSatList for TpSatSpinner
        strSatList = GetStrSatListFromSatList();

        // Set strTpList for TpSpinner
        tpList = satList.isEmpty() ? null : TpInfoGetListBySatId(satList.get(0).getSatId());
        strTpList = GetStrTpListFromTpList();

        // Setting  Sat
        SelectSat = new SelectBoxView(this, SpinnerSat, strSatList);
        SpinnerSat.setOnItemSelectedListener(SatListener);
        for(int i = 0 ; i < satList.size() ; i++) {//Scoty 20180831 modify antenna rule by antenna type
            if(satList.get(i).Antenna.getDiseqcType() == SatInfo.DISEQC_TYPE_OFF) {
                SpinnerSat.setSelection(i);
                break;
            }
        }
        // Setting  TpList
        SelectTpList = new SelectBoxView(this, SpinnerTpList, strTpList);
        SpinnerTpList.setOnItemSelectedListener(TpListListener);
        SpinnerTpList.setOnFocusChangeListener(TpListFocusListener);

        // Setting ScanMode
        String[] scanModeList = getResources().getStringArray(R.array.STR_ARRAY_SCAN_MODE);
        SelectScanMode = new SelectBoxView(this, SpinnerScanMode, scanModeList);

        // Setting Channel Type
        String[] channelTypeList = getResources().getStringArray(R.array.STR_ARRAY_CHANNEL_TYPE);
        SelectChannelType = new SelectBoxView(this, SpinnerChannelType, channelTypeList);

        /*antList = AntInfoGetList();
        if(antList == null) {
            antList = new ArrayList<>();
        }*/

        // Setting LNBType/AntType/Tone22K/Diseqc Value
        lnbTypeItems = getResources().getStringArray(R.array.STR_DVBS_ARRAY_LNB_TYPE);
        antTypeItems = getResources().getStringArray(R.array.STR_DVBS_ARRAY_ANT_TYPE);
        tone22kItems = getResources().getStringArray(R.array.STR_DVBS_ARRAY_22KHZ_TONE);
        diseqcItems = getResources().getStringArray(R.array.STR_DVBS_ARRAY_DISEQC);

        // search Button
        if(mParent == 0) { //Easy Install
            ButtonSearch.setVisibility(View.INVISIBLE);
        }
        else
        {
            ButtonSearch.setOnClickListener(SearchClickListener);
            ButtonSearch.setOnLongClickListener(SearchLongClickListener);   // Johnny 20181219 for mouse control
            ButtonSearch.setOnFocusChangeListener(SearchFocusListener);
            searchButtonItems = getResources().getStringArray(R.array.STR_DVBS_ARRAY_SEARCH_BUTTON);
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

    private AdapterView.OnItemSelectedListener SatListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            String str;

            //Log.d(TAG, "position =  " + position);
            tpList = TpInfoGetListBySatId(satList.get(position).getSatId());

            String[] strTpList = GetStrTpListFromTpList();

            // Setting curTpId, freq, symbol, polar
            if ( tpList != null && !tpList.isEmpty() )
            {
                tpID = tpList.get(0).getTpId();
                freq = tpList.get(0).SatTp.getFreq();
                symbol = tpList.get(0).SatTp.getSymbol();
                polar = tpList.get(0).SatTp.getPolar();
            }

            // Setting  TpList
            SelectTpList.SetNewAdapterItems(strTpList);
            SpinnerTpList.setOnItemSelectedListener(TpListListener);
            SpinnerTpList.setOnFocusChangeListener(TpListFocusListener);

            // Setting LNBType/AntType/Tone22K/Diseqc Value
            int lnbType = satList.get(position).Antenna.getLnbType();
            str = lnbTypeItems[lnbType];
            TextLnbTypeValue.setText(str);

            int diseqcType = satList.get(position).Antenna.getDiseqcType();
            str = antTypeItems[diseqcType];
            TextAntTypeValue.setText(str);

            int tone22k = satList.get(position).Antenna.getTone22k();
            str = tone22kItems[tone22k];
            TextTone22kValue.setText(str);

            if (diseqcType == SatInfo.DISEQC_TYPE_1_0)
            {
                str = diseqcItems[satList.get(position).Antenna.getDiseqc()];
            }
            else    // OFF, Diseqc1.2
            {
                str = getString(R.string.STR_DVBS_DISEQC_NONE);
            }

            TextDiSEqCValue.setText(str);

            Log.d(TAG, "onItemSelected:    Sat   = " + satList.get(position).getSatName());
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };



    private AdapterView.OnItemSelectedListener TpListListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);

            // Setting curTpId, freq, symbol, polar
            if ( tpList != null && !tpList.isEmpty() )
            {
                tpID = tpList.get(position).getTpId();
                freq = tpList.get(position).SatTp.getFreq();
                symbol = tpList.get(position).SatTp.getSymbol();
                polar = tpList.get(position).SatTp.getPolar();
            }
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private View.OnFocusChangeListener TpListFocusListener = new SelectBoxView.SelectBoxtOnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            super.onFocusChange(v, hasFocus);
            if (hasFocus) {
                // Set Help Button
                helpView.resetHelp(1,R.drawable.help_red, getString(R.string.STR_DVBS_EDIT_TP));
                helpView.resetHelp(2,R.drawable.help_green, getString(R.string.STR_DVBS_DELETE_TP));
                helpView.resetHelp(3,R.drawable.help_blue, getString(R.string.STR_DVBS_ADD_TP));
                helpView.resetHelp(4,0, null);

                // Johnny 20181228 for mouse control -s
                helpView.setHelpIconClickListener(1, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowEditTpDialog();
                    }
                });
                helpView.setHelpIconClickListener(2, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowDeleteTpDialog();
                    }
                });
                helpView.setHelpIconClickListener(3, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAddTpDialog();
                    }
                });
                // Johnny 20181228 for mouse control -e
            }
            else
            {
                helpView.resetHelp(1,0,null);
                helpView.resetHelp(2,0,null);
                helpView.resetHelp(3,0,null);
                helpView.resetHelp(4,0,null);
            }
        }
    };

    private View.OnFocusChangeListener SearchFocusListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {

            if (hasFocus) {
                // Set Help
                helpView.setHelpInfoTextBySplit(getString(R.string.STR_SEARCH_BUTTON_HELPTEXT));
            }
            else
            {
                helpView.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));
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
            SearchBtnPressRight();  // long click = onkeydown:right
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

//        Log.d(TAG, "UpdateSignalLevel  Lock = " + lock + " Strengh = " + Strength + " Quality = " + Quality);
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

    private void SearchBtnPressRight()
    {
        if ( ++searchMode >= searchButtonItems.length )
        {
            searchMode = 0;
        }

        ButtonSearch.setText(searchButtonItems[searchMode]);
    }

    private void SearchBtnPressLeft()
    {
        if ( --searchMode < 0 )
        {
            searchMode = searchButtonItems.length-1;
        }

        ButtonSearch.setText(searchButtonItems[searchMode]);
    }

    private String[] GetStrSatListFromSatList()
    {
        if(satList == null) {
            satList = new ArrayList<>();
        }

        String[] strSatList = new String[satList.size()];
        for (int i = 0; i < satList.size(); i++) {
            String name = satList.get(i).getSatName();
            String angle = Float.toString(satList.get(i).getAngle());
            String angleEW = satList.get(i).getAngleEW() == SatInfo.ANGLE_E ? "E" : "W";

            strSatList[i] = name + " " + angle + angleEW;
            Log.d(TAG, "sat id = " + satList.get(i).getSatId() + "     name = " + name +
                    "    angle = " + angle + " angleEW = " + angleEW);
        }

        return strSatList;
    }

    private String[] GetStrTpListFromTpList()
    {
        if(tpList == null) {
            tpList = new ArrayList<>();
        }

        //Log.d(TAG, "size " + tpList.size() );
        String[] strTpList = new String [tpList.size()];
        for(int i = 0; i< tpList.size(); i++) {
            String freq = Integer.toString(tpList.get(i).SatTp.getFreq());
            String symbolRate = Integer.toString(tpList.get(i).SatTp.getSymbol());
            String polar;

            if ( tpList.get(i).SatTp.getPolar() == TpInfo.Sat.POLAR_H )
            {
                polar = " H ";
            }
            else
            {
                polar = " V ";
            }

            strTpList[i] = freq + polar + symbolRate;
            Log.d(TAG, "tp id = " + tpList.get(i).getTpId() + "     str = " + strTpList[i]);
        }

        return strTpList;
    }

    public void ShowEditTpDialog()
    {
        Log.d(TAG," EditTpDialog ");
        Bundle bundle = new Bundle();
        bundle.putInt(DVBSTpDialogView.KEY_TYPE, DVBSTpDialogView.TYPE_EDIT_TP);
        bundle.putInt(DVBSTpDialogView.KEY_FREQUENCY, freq);
        bundle.putInt(DVBSTpDialogView.KEY_SYMBOL_RATE, symbol);
        bundle.putInt(DVBSTpDialogView.KEY_POLAR, polar);

        DVBSTpDialogView tpDialog = new DVBSTpDialogView();
        tpDialog.setArguments(bundle);
        tpDialog.show(getFragmentManager(), "editTpDialog");
    }

    public void ShowDeleteTpDialog()   //Yes : delete ; No: cancel
    {
        Log.d(TAG," DeleteTpDialog ");
        if ( tpList.size() == 1 )
        {
            new MessageDialogView(ChannelSearchDVBSActivity.this,
                    getString(R.string.STR_CAN_NOT_DELETE_LAST_TP),
                    getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY)) {   // Johnny 20180813 modify msg dialog delay by xml
                public void dialogEnd() {
                }
            }.show();
            return;
        }

        new SureDialog(ChannelSearchDVBSActivity.this){
            public void onSetMessage(View v){
                ((TextView)v).setText(getString(R.string.STR_DO_YOU_WANT_TO_DELETE_TP));
            }
            public void onSetNegativeButton(){
            }
            public void onSetPositiveButton(){
                onDialogDeletePositiveClick();
            }
        };
    }

    public void ShowAddTpDialog()
    {
        Log.d(TAG," AddTpDialog ");
        if ( tpList.size() == SatInfo.MAX_TP_NUM_IN_ONE_SAT )
        {
            new MessageDialogView(ChannelSearchDVBSActivity.this,
                    getString(R.string.STR_TP_LIST_IS_FULL),
                    getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY)) {   // Johnny 20180813 modify msg dialog delay by xml
                public void dialogEnd() {
                }
            }.show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putInt(DVBSTpDialogView.KEY_TYPE, DVBSTpDialogView.TYPE_ADD_TP);

        DVBSTpDialogView tpDialog = new DVBSTpDialogView();
        tpDialog.setArguments(bundle);
        tpDialog.show(getFragmentManager(), "addTpDialog");
    }

    public void onDialogDeletePositiveClick()
    {
        // not in DVBSTpDialogView, its in SureDialog

        // Delete selected tp from DB/tpList
        int curTpListIndex = SelectTpList.GetSelectedItemIndex();

        TpInfoDelete(tpID);
        tpList.remove(curTpListIndex);

        // Update Tp SelectBoxView
        String[] strTpList = GetStrTpListFromTpList();
        SelectTpList.SetNewAdapterItems(strTpList);
        SpinnerTpList.setOnItemSelectedListener(TpListListener);
        SpinnerTpList.setOnFocusChangeListener(TpListFocusListener);

        if ( curTpListIndex == tpList.size() )
        {
            curTpListIndex--;
        }

        SelectTpList.SetSelectedItemIndex(curTpListIndex);
    }

    @Override
    public void onDialogEditPositiveClick(int freq, int symbol, int polar)
    {
        // Do when dvbsTpdialog click ok, type = edit

        // Save change to DB
        int curTpListIndex = SelectTpList.GetSelectedItemIndex();
        TpInfo tpInfo = tpList.get(curTpListIndex);
        tpInfo.SatTp.setFreq(freq);
        tpInfo.SatTp.setSymbol(symbol);
        tpInfo.SatTp.setPolar(polar);
        TpInfoUpdate(tpInfo);

        // Update Tp SelectBoxView
        String[] strTpList = GetStrTpListFromTpList();
        SelectTpList.SetNewAdapterItems(strTpList);
        SpinnerTpList.setOnItemSelectedListener(TpListListener);
        SpinnerTpList.setOnFocusChangeListener(TpListFocusListener);
        SelectTpList.SetSelectedItemIndex(curTpListIndex);
    }

    @Override
    public void onDialogAddPositiveClick(int freq, int symbol, int polar)
    {
        // Do when dvbsTpdialog click ok, type = add

        TpInfo sample = tpList.get(tpList.size()-1);    // use last as sample
        TpInfo tpInfo = new TpInfo(GetCurTunerType());
//        tpInfo.setNetwork_id(sample.getNetwork_id());
//        tpInfo.setTransport_id(sample.getTransport_id());
//        tpInfo.setOrignal_network_id(sample.getOrignal_network_id());
        tpInfo.setTuner_id(sample.getTuner_id());
        tpInfo.setSatId(sample.getSatId());
        tpInfo.SatTp.setFreq(freq);
        tpInfo.SatTp.setSymbol(symbol);
        tpInfo.SatTp.setPolar(polar);
//        tpInfo.SatTp.setOtherData();

        // Add change to DB/tpList
        if (TpInfoAdd(tpInfo) == CMD_RETURN_VALUE_SUCCESS)  // Johnny 20180813 show add tp fail msg
        {
            tpList.add(tpInfo);
            // Update Tp SelectBoxView
            String[] strTpList = GetStrTpListFromTpList();
            SelectTpList.SetNewAdapterItems(strTpList);
            SpinnerTpList.setOnItemSelectedListener(TpListListener);
            SpinnerTpList.setOnFocusChangeListener(TpListFocusListener);
            SelectTpList.SetSelectedItemIndex(tpList.size()-1);
        }
        else
        {
            new MessageDialogView(ChannelSearchDVBSActivity.this,
                    getString(R.string.STR_ADD_TP_FAIL),
                    getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY)) {
                public void dialogEnd() {
                }
            }.show();
        }
    }

    @Override
    public void onDialogNegativeClick()
    {
        // do when dvbsTpdialog click cancel
    }

    private void SearchStart()
    {
        ScanMode = SelectScanMode.GetSelectedItemIndex();
        ChannelType = SelectChannelType.GetSelectedItemIndex();

        Intent intent = new Intent();
        intent.setClass(ChannelSearchDVBSActivity.this, ScanResultActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt("tp_id", tpID);
        bundle.putInt("scan_mode", ScanMode);   // 0 : fta&$, 1 : fta, 2 : $
        bundle.putInt("channel_type", ChannelType); // 0 : TV&Radio, 1 : TV, 2 : Radio
        bundle.putInt("search_mode", searchMode);   // 0~4

        Log.d(TAG, "SearchStart: " + searchMode);
        intent.putExtras(bundle);
        startActivityForResult(intent, 0); // Edwin 20181210 add search fail message

        if ( TvInputActivity.isTvInputOpened() ) // Edwin 20181214 live tv cannot scan channel
        {
            finish();
        }
    }

    private List<SatInfo> GetSatListForDVBS()
    {
        List<SatInfo> satInfoList = new ArrayList<>();
        List<SatInfo> allSatList = SatInfoGetList(TpInfo.DVBS);

        if (allSatList == null)
        {
            return  satInfoList;
        }

        // find correct diseqcType
        int diseqcType = SatInfo.DISEQC_TYPE_OFF;
        for (int i = 0 ; i < allSatList.size() ; i++)
        {
            int tmpDiseqcType = allSatList.get(i).Antenna.getDiseqcType();
            if(tmpDiseqcType != SatInfo.DISEQC_TYPE_NONE) {//Scoty 20180831 modify antenna rule by antenna type
                diseqcType = tmpDiseqcType;
                break;
            }
        }

        // find sats that fulfill following condition
        // if off, find all sats
        // if 1.0, find diseqcUse == 1
        // if 1.2, find positionIndex != 0
        if (diseqcType == SatInfo.DISEQC_TYPE_OFF)
        {
            Log.d(TAG, "GetSatListForDVBS: DiseqcType = " + SatInfo.DISEQC_TYPE_OFF);
            satInfoList = allSatList;
        }
        else//1.0 or 1.2 //Scoty 20180831 modify antenna rule by antenna type
                {
            for (int i = 0 ; i < allSatList.size() ; i++)
            {
                SatInfo satInfo = allSatList.get(i);
                if(satInfo.Antenna.getDiseqcType() == diseqcType)//Scoty 20180831 modify antenna rule by antenna type
                {
                    satInfoList.add(satInfo);
                }
            }
        }


        return satInfoList;
    }

    // ========  OnKeyDown ===================
    @Override
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
                    intent.setClass(ChannelSearchDVBSActivity.this, FirstTimeInstallationActivity.class);
                    startActivity(intent);
                }break;
                default:
                    break;
            }
        }
        else if (mParent == 1) { //Main Menu
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT: {
                    if (ButtonSearch.hasFocus()) {
                        Log.d(TAG, "str = " + ButtonSearch.getText());
                        SearchBtnPressLeft();
                        return true;
                    }
                }
                break;
                case KeyEvent.KEYCODE_DPAD_RIGHT: {
                    if (ButtonSearch.hasFocus()) {
                        Log.d(TAG, "str = " + ButtonSearch.getText());
                        SearchBtnPressRight();
                        return true;
                    }
                }
                break;
                case KeyEvent.KEYCODE_PROG_RED:
                case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                {
                    if (SpinnerTpList.hasFocus()) {
                        ShowEditTpDialog();
                        return true;
                    }
                }
                break;
                case KeyEvent.KEYCODE_PROG_GREEN:
                case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
                {
                    if (SpinnerTpList.hasFocus()) {
                        ShowDeleteTpDialog();
                        return true;
                    }
                }
                break;
                case KeyEvent.KEYCODE_PROG_BLUE:
                case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                {
                    if (SpinnerTpList.hasFocus()) {
                        ShowAddTpDialog();
                        return true;
                    }
                }
                break;
            }
            if (keystr.length() >= 4) // connie 20180803 add record ts -s
                keystr = "";
            if(keyCode >= KEYCODE_0 && keyCode <= KEYCODE_9)
                keystr=keystr+Integer.toString(keyCode-KEYCODE_0);
            else
                keystr="";
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
        String PolarText = "";
        if(polar == 0)
            PolarText = "H";
        else
            PolarText = "V";

        FileName = freq + "_" + PolarText +"_"+ symbol + "_" + curTime + ".ts";
        fullName = GetRecordPath()+"/Records/"+FileName;
        RecordTSDialog recDurDialog = new RecordTSDialog(ChannelSearchDVBSActivity.this, this, tunerId, fullName );
        return ;
    }
}
