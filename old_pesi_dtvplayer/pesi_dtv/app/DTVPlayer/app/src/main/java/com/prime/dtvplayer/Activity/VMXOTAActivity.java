package com.prime.dtvplayer.Activity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.SelectBoxView;

import java.util.ArrayList;
import java.util.List;

public class VMXOTAActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    private ProgressBar strengthBar ,qualityBar ;
    private TextView strengthValue,qualityValue ;
    private Spinner freqSpinner,bandwidthSpinner;
    private SelectBoxView sel_freq,sel_bandwidth;
    private EditText dsmccPidEdv,tableIdEdv;
    private Button starOtabtn;
    private Handler CheckSignalHandler=null,ChangeParamHandler=null;
    private List<TpInfo> tpList = new ArrayList<>();
    private int Frequency = 0, Bandwidth = 0;  // 0 : 6Mhz, 1 : 7Mhz, 2 : 8Mhz
    private int tuneFreq = 0, tuneBandwidth = 0;
    private int tpID = 0;

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
        if(CheckSignalHandler != null) {
            Log.d(TAG, "CheckSignalHandler remove Callback");
            CheckSignalHandler.removeCallbacks(CheckStatusRunnable);
            CheckSignalHandler = null;
        }
        if(ChangeParamHandler != null) {
            ChangeParamHandler.removeCallbacks(ParamChangeRunnable);
            ChangeParamHandler = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vmxota);

        InitTitle();
        InitHelp();
        InitItem();
    }

    private void InitTitle() {
        Log.d(TAG, "InitTitle: ");
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.TitleViewLayout);
        title.setTitleView(getString(R.string.STR_VMX_OTA_TITLE));
    }

    private void InitHelp()
    {
        Log.d(TAG, "InitHelp: ");
        ActivityHelpView helpView = (ActivityHelpView) findViewById(R.id.HelpViewLayout);
        helpView.resetHelp(1, 0, null);
        helpView.resetHelp(2, 0, null);
        helpView.resetHelp(3, 0, null);
        helpView.resetHelp(4, 0, null);
        helpView.setHelpInfoTextBySplit(null);
    }

    private void InitItem()
    {
        int freq = 0, position = 0;
        String str;
        String[] strFreqList = null;

        freqSpinner = (Spinner) findViewById(R.id.freqSPINNER);
        bandwidthSpinner = (Spinner) findViewById(R.id.bandwidthSPINNER);
        dsmccPidEdv = (EditText) findViewById(R.id.dsmccPidEDV);
        tableIdEdv = (EditText) findViewById(R.id.tableIdEDV);
        starOtabtn = (Button) findViewById(R.id.starOtaBTN);
        strengthBar = (ProgressBar)findViewById(R.id.strengthPROGBAR) ;
        qualityBar = (ProgressBar)findViewById(R.id.qualityPROGBAR) ;
        strengthValue = (TextView)findViewById(R.id.strengthValueTXV) ;
        qualityValue = (TextView)findViewById(R.id.qualityValueTXV) ;

        // Setting Frequency
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

        sel_freq = new SelectBoxView(this, freqSpinner, strFreqList);
        freqSpinner.setOnItemSelectedListener(FreqListener);
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
        freqSpinner.setSelection(position);

        // Setting  Bandwidth
        String[] strBandList = getResources().getStringArray(R.array.STR_ISDBT_ARRAY_BANDWIDTH);
        sel_bandwidth = new SelectBoxView(this, bandwidthSpinner, strBandList);
        bandwidthSpinner.setFocusable(false);
        bandwidthSpinner.setOnItemSelectedListener(BandwidthListener);

        dsmccPidEdv.setOnFocusChangeListener(dsmccPidfocusListener);
        dsmccPidEdv.setShowSoftInputOnFocus(false); // disable keyboard

        tableIdEdv.setOnFocusChangeListener(tableIdfocusListener);
        tableIdEdv.setShowSoftInputOnFocus(false); // disable keyboard

        starOtabtn.setOnClickListener(startOtaListener);
    }

    private View.OnFocusChangeListener dsmccPidfocusListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                dsmccPidEdv.selectAll();
            }
        }
    };

    private View.OnFocusChangeListener tableIdfocusListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                tableIdEdv.selectAll();
            }
        }
    };


    View.OnClickListener startOtaListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: startOtaListener");
            ArrayList<Integer> freqList = new ArrayList<>(),  bandwidthList = new ArrayList<>();
            int satId = TpInfoGet(tpID).getSatId();
            int dsmccPid = Integer.valueOf(dsmccPidEdv.getText().toString());
            freqList.add(Integer.valueOf(sel_freq.GetSelectedItem().toString()));
            bandwidthList.add(sel_bandwidth.GetSelectedItemIndex());
            VMXAutoOTA(0, 0, 0, 0, satId, dsmccPid, 1, freqList, bandwidthList);
        }
    };
    final Runnable CheckStatusRunnable = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            UpdateSignal(TunerGetLockStatus(0),TunerGetStrength(0),TunerGetQuality(0));
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

    private void UpdateSignal(int lock, int strength, int quality)
    {
        int barcolor;
        String value;

        if(lock == 1 )
            barcolor = Color.GREEN;
        else
            barcolor =Color.RED;
        strengthBar.setProgressTintList(ColorStateList.valueOf(barcolor));
        strengthBar.setProgress(strength);
        qualityBar.setProgressTintList(ColorStateList.valueOf(barcolor));
        qualityBar.setProgress(quality);

        value = Integer.toString(strength) + " %";
        strengthValue.setText(value);
        value = Integer.toString(quality)+ " %";
        qualityValue.setText(value);
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

            bandwidthSpinner.setSelection(Bandwidth);

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

}
