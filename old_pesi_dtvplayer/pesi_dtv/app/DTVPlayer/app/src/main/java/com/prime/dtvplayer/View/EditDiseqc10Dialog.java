package com.prime.dtvplayer.View;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;

import java.util.ArrayList;
import java.util.List;

import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_PROG_GREEN;
import static android.view.KeyEvent.KEYCODE_PROG_RED;
import static com.prime.dtvplayer.Sysdata.SatInfo.ANGLE_E;
import static com.prime.dtvplayer.Sysdata.SatInfo.ANGLE_W;
import static com.prime.dtvplayer.Sysdata.SatInfo.DISEQC_PORT_A;
import static com.prime.dtvplayer.Sysdata.SatInfo.DISEQC_PORT_B;
import static com.prime.dtvplayer.Sysdata.SatInfo.DISEQC_PORT_C;
import static com.prime.dtvplayer.Sysdata.SatInfo.DISEQC_PORT_D;
import static com.prime.dtvplayer.Sysdata.SatInfo.LNB_TYPE_LNBF;
import static com.prime.dtvplayer.Sysdata.SatInfo.LNB_TYPE_NORMAL;
import static com.prime.dtvplayer.Sysdata.SatInfo.LNB_TYPE_UNIVERSAL;
import static com.prime.dtvplayer.Sysdata.SatInfo.TONE_22K_AUTO;

// Created by edwin_weng on 2018/4/20.

public abstract class EditDiseqc10Dialog extends Dialog {
    private static final String TAG = "EditDiseqc10Dialog";
    private static final int DISEQC_OFF = -1;
    // constructor
    private Context cont;
    private int diseqcPort;
    private int curSat;
    private String[] strSatList;
    private String[] strTpList;
    private DTVActivity mDtv;
    private List<SatInfo> satList;
    private List<TpInfo> tpList;

    // parent view
    private ActivityHelpView help;

    // dialog view
    private Spinner spSatellite;
    private Spinner spLnbType;
    private Spinner sp22kTone;
    //private Spinner spLnbPower;
    private Spinner spTP;
    private EditText etFreqLow;
    private EditText etFreqHigh;
    private ProgressBar pbStrength;
    private ProgressBar pbQuality;
    private TextView strengthTxv;
    private TextView qualityTxv;

    // var
    private boolean isModified = false;
    private int orgSat;
    private int orgLnbType;
    private int orgLowFreq;
    private int orgHighFreq;
    private int orgTone22k;

    // listener
    private View.OnFocusChangeListener limitFreq65535;
    private View.OnClickListener stopKeyboard;
    private View.OnKeyListener inputRule;

    protected EditDiseqc10Dialog(@NonNull Context context,
                                 int diseqcPort,
                                 int curSat,
                                 boolean clickNONE,
                                 List<SatInfo> satList,
                                 ActivityHelpView help)
    {
        super(context, R.style.transparentDialog);
        Log.d(TAG, "EditDiseqc10Dialog: Constructor");

        if (getWindow() == null) {
            Log.d(TAG, "EditDiseqc10Dialog: getWindow() is null");
            return;
        }

        this.mDtv = ((DTVActivity) context);
        this.cont = context;
        this.diseqcPort = diseqcPort;
        this.satList = satList;
        this.strSatList = GetSatStringArray(this.satList);
        this.help = help;


        this.isModified = clickNONE;
        this.curSat = curSat;

        getWindow().getAttributes().gravity = Gravity.TOP;
        getWindow().getAttributes().verticalMargin = (float) 0.13;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_lnb_dialog);

        InitView();
        InitTitleHelp(diseqcPort, help);
        InitFreq();
        InitSpinner(curSat);
        InitProgressBar();

//        mDtv.FrontEndSetDiSEqC10PortInfo(diseqcPort, satList.get(curSat));  // Johnny 20180814 setDiseqc1.0 port when show diseqc10port dialog // service do this when tune
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        Log.d(TAG, "onKeyDown: ");

        switch (keyCode)
        {
            case KEYCODE_BACK:
                ShowConfirmDialog(diseqcPort,
                        spSatellite.getSelectedItemPosition(),
                        spLnbType.getSelectedItemPosition(),
                        Integer.valueOf(etFreqLow.getText().toString()),
                        Integer.valueOf(etFreqHigh.getText().toString()),
                        sp22kTone.getSelectedItemPosition());
                break;

            case KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                ShowEditSat();
                break;

            case KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
                ShowAddTp();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void ShowEditSat()
    {
        Log.d(TAG, "ShowEditSat: ");

        new DialogEditSat(getContext(), spSatellite.getSelectedItemPosition(), satList)
        {
            @Override
            public void OnClickUpdate(int satIndex, String satName, String newAngle, String angleWE, int anglePos)
            {
                Log.d(TAG, "OnClickSave: edit SAT");

                satList.get(satIndex).setSatName(satName); // set new sat name
                strSatList[satIndex] = satName + " " + newAngle + angleWE;
                satList.get(satIndex).setAngle(Float.valueOf(newAngle)); // set new angle
                satList.get(satIndex).setAngleEW((anglePos == ANGLE_E) ? ANGLE_E : ANGLE_W); // set new angle W E

                int orgPos = spSatellite.getSelectedItemPosition();
                new SelectBoxView(cont, spSatellite, strSatList);
                InitSpinnerSat();    // Johnny 20180515 set listener after new SelectBoxView()
                spSatellite.setSelection(orgPos);
                isModified = true;
            }
        }.show();
    }

    private void ShowAddTp() {

        Log.d(TAG, "ShowAddTp: ");

        DialogAddTp addTpDialog = new DialogAddTp(getContext())
        {
            @Override
            public void OnClickOK(int freq, int sym, int pol)
            {
                Log.d(TAG, "OnClickOK: add TP");

                int index = spSatellite.getSelectedItemPosition();
                int tpNum = satList.get(index).getTpNum();

                // add TP info
                TpInfo tpInfo = new TpInfo(TpInfo.DVBS);
                tpInfo.setSatId(satList.get(index).getSatId());
                tpInfo.setTuner_id(0);
                tpInfo.SatTp.setFreq(freq);
                tpInfo.SatTp.setSymbol(sym);
                tpInfo.SatTp.setPolar(pol);
                tpList.add(tpInfo);
                mDtv.TpInfoAdd(tpInfo); // Johnny 20180815 add tp to service here because TpinfoUpdateList() used in EditDiseqc10/12Dialog can't add tp

                // set Tp Num & Tps
                satList.get(index).setTpNum(tpNum+1);
                List<Integer> tps = new ArrayList<>();
                for (int i = 0; i < tpList.size(); i++) {
                    tps.add(tpList.get(i).getTpId());
                }
                satList.get(index).setTps(tps);
                strTpList = GetTpStringArray(tpList);

                new SelectBoxView(getContext(), spTP, strTpList);
                InitSpinnerTP();    // Johnny 20180515 set listener after new SelectBoxView()
                spTP.setSelection(tpList.size()-1);
                isModified = true;
            }
        };
        addTpDialog.show();
    }

    private void ShowConfirmDialog(final int diseqcPort, int curSat, int curLnbType, int curFreqLow, int curFreqHigh, int curTone22k)
    {
        Log.d(TAG, "ShowConfirmDialog: press back");

        if (diseqcPort == DISEQC_OFF)
        {
            help.resetHelp(1, R.drawable.help_blue, cont.getString(R.string.STR_DELETE));
            help.resetHelp(2, 0, null);
        }
        else
        {
            help.resetHelp(1, R.drawable.help_red, cont.getString(R.string.STR_DELETE_ALL));
            help.resetHelp(2, R.drawable.help_blue, cont.getString(R.string.STR_DELETE));
        }

        if (orgSat != curSat ||
                orgLnbType != curLnbType ||
                orgLowFreq != curFreqLow ||
                orgHighFreq != curFreqHigh ||
                orgTone22k != curTone22k)
        {
            isModified = true;
        }

        if ( ! isModified ) return;

        ConfirmDialog confirmDialog = new ConfirmDialog(getContext(), EditDiseqc10Dialog.this) {
            @Override
            void OnClickYes()
            {
                Log.d(TAG, "OnClickYes: change sat list");

                //ClickBackYes(dscType); // set new sat & angle & LNB
                int sat         = spSatellite.getSelectedItemPosition();
                int lnbType     = spLnbType.getSelectedItemPosition();
                int tone22k     = sp22kTone.getSelectedItemPosition();
                //int lnbPower    = spLnbPower.getSelectedItemPosition();
                int lowFreq     = Integer.valueOf(etFreqLow.getText().toString());
                int highFreq    = Integer.valueOf(etFreqHigh.getText().toString());

                UpdateSetting(diseqcPort, sat, lnbType, lowFreq, highFreq, tone22k);
            }
        };
        confirmDialog.show();
    }

    private void InitView()
    {
        Log.d(TAG, "InitView: ");

        spSatellite = (Spinner) findViewById(R.id.sat_spinner);
        spLnbType = (Spinner) findViewById(R.id.lnb_type_spinner);
        sp22kTone = (Spinner) findViewById(R.id.tone_spinner);
        //spLnbPower = (Spinner) findViewById(R.id.lnbPower_spinner);
        spTP = (Spinner) findViewById(R.id.transponder_spinner);
        etFreqLow = (EditText) findViewById(R.id.lnb_low_freq_edt);
        etFreqHigh = (EditText) findViewById(R.id.lnb_high_freq_edt);
        pbStrength = (ProgressBar) findViewById(R.id.strength_progressbar);
        pbQuality = (ProgressBar) findViewById(R.id.quality_progressbar);
        strengthTxv = (TextView) findViewById(R.id.strength_percent_txv);
        qualityTxv = (TextView) findViewById(R.id.quality_percent_txv);
    }

    private void InitTitleHelp(int diseqcPort, ActivityHelpView help)
    {
        Log.d(TAG, "InitTitle: ");

        // init title
        TextView txvTitle = (TextView) findViewById(R.id.lnb_edit_title);
        if (diseqcPort == DISEQC_OFF)           txvTitle.setText(cont.getString(R.string.STR_EDIT));
        else if (diseqcPort == DISEQC_PORT_A)   txvTitle.setText(cont.getString(R.string.STR_EDIT_LNB).concat(" 1"));
        else if (diseqcPort == DISEQC_PORT_B)   txvTitle.setText(cont.getString(R.string.STR_EDIT_LNB).concat(" 2"));
        else if (diseqcPort == DISEQC_PORT_C)   txvTitle.setText(cont.getString(R.string.STR_EDIT_LNB).concat(" 3"));
        else if (diseqcPort == DISEQC_PORT_D)   txvTitle.setText(cont.getString(R.string.STR_EDIT_LNB).concat(" 4"));

        help.resetHelp(1, R.drawable.help_red, cont.getString(R.string.STR_SAT_EDIT));
        help.resetHelp(2, R.drawable.help_green, cont.getString(R.string.STR_DVBS_ADD_TP));

        // Johnny 20181228 for mouse control -s
        help.setHelpIconClickListener(1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowEditSat();
            }
        });
        help.setHelpIconClickListener(2, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowAddTp();
            }
        });
        // Johnny 20181228 for mouse control -e
    }

    private void InitFreq()
    {
        Log.d(TAG, "InitFreq: ");

        // set listener
        Log.d(TAG, "InitFreq: set listener");
        InitFreqListener();

        etFreqLow.setOnFocusChangeListener(limitFreq65535);
        etFreqHigh.setOnFocusChangeListener(limitFreq65535);
        etFreqLow.setOnClickListener(stopKeyboard);
        etFreqHigh.setOnClickListener(stopKeyboard);
        etFreqLow.setOnKeyListener(inputRule);
        etFreqHigh.setOnKeyListener(inputRule);
    }

    private void InitFreqListener()
    {
        Log.d(TAG, "InitFreqListener: ");

        limitFreq65535 = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                EditText etFreq = (EditText) v;
                String strFreq;
                int freq;

                if ( hasFocus )
                {
                    Log.d(TAG, "onFocusChange: selectAll()");
                    etFreq.selectAll();
                }
                else
                {
                    Log.d(TAG, "onFocusChange: unFocusFreq");

                    strFreq = etFreq.getText().toString();
                    freq = Integer.valueOf(strFreq);

                    if (freq >= 65535)
                    {
                        etFreq.setText(String.valueOf(65535));
                    }
                    else if (freq <= 0)
                    {
                        etFreq.setText(String.valueOf(0));
                    }
                }
            }
        };

        stopKeyboard = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: stop Keyboard");
                v.requestFocus();
            }
        };

        inputRule = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ( event.getAction() == KeyEvent.ACTION_UP )
                    return false;

                Log.d(TAG, "onKey: OnKeyFreq, keyCode = "+keyCode);
                EditText etFreq = (EditText) v;
                boolean isSelectAll = etFreq.getSelectionStart() != etFreq.getSelectionEnd();

                switch (keyCode)
                {
                    case KEYCODE_DPAD_LEFT:
                        if ( ! isSelectAll )
                        {
                            FreqDelete(etFreq);
                        }
                        return true;

                    case KEYCODE_DPAD_CENTER:
                    case KEYCODE_DPAD_DOWN:
                        FreqMoveDown(etFreq);
                        return true;

                    case KEYCODE_DPAD_UP:
                        FreqMoveUp(etFreq);
                        return true;
                }

                return CheckInput(etFreq, keyCode);
            }
        };
    }

    private void InitSpinner(int curSat)
    {
        Log.d(TAG, "InitSpinner: ");

        int lnbType = satList.get(curSat).Antenna.getLnbType();
        //int lnbPower = mDtv.GposInfoGet().getLnbPower();
        int tone22k = (satList.get(curSat).Antenna.getTone22kUse() == 1)
                ? satList.get(curSat).Antenna.getTone22k()
                : TONE_22K_AUTO;

        tpList = mDtv.TpInfoGetListBySatId(satList.get(curSat).getSatId());
        strTpList = GetTpStringArray(tpList);

        // init Spinner
        new SelectBoxView(cont, spSatellite, strSatList);
        new SelectBoxView(cont, spLnbType, cont.getResources().getStringArray(R.array.STR_DVBS_ARRAY_LNB_TYPE));
        new SelectBoxView(cont, sp22kTone, cont.getResources().getStringArray(R.array.STR_DVBS_ARRAY_22KHZ_TONE));
        //new SelectBoxView(cont, spLnbPower, cont.getResources().getStringArray(R.array.STR_ARRAY_ONOFF));
        new SelectBoxView(cont, spTP, strTpList);

        InitSpinnerSat();       // Satellite
        InitSpinnerLNBType();   // LNB Type
        InitSpinner22kTone();   // 22K Tone
        //InitSpinnerLNBPower();  // LNB Power
        InitSpinnerTP();        // TP

        spSatellite.setSelection(curSat);
        spLnbType.setSelection(lnbType);
        //spLnbPower.setSelection(lnbPower);
        sp22kTone.setSelection(tone22k);

        orgSat      = spSatellite.getSelectedItemPosition();
        orgLnbType  = spLnbType.getSelectedItemPosition();
        orgTone22k  = sp22kTone.getSelectedItemPosition();
        orgLowFreq  = satList.get(curSat).Antenna.getLnb1();
        orgHighFreq = satList.get(curSat).Antenna.getLnb2();
        Log.d(TAG, "InitSpinner:" +
                " orgSat = " + orgSat +
                ", orgLnbType = " + orgLnbType +
                ", orgLowFreq = " + orgLowFreq +
                ", orgHighFreq = " + orgHighFreq +
                ", orgTone22k = " + orgTone22k
        );
    }

    private void InitSpinnerSat()
    {
        Log.d(TAG, "InitSpinnerSat: ");

        spSatellite.setOnItemSelectedListener(new SelectBoxView.SelectBoxOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //if (stopSelect) return;

                super.onItemSelected(parent, view, position, id);
                int lnbType = satList.get(position).Antenna.getLnbType();
                int freqLow = satList.get(position).Antenna.getLnb1();
                int freqHigh = satList.get(position).Antenna.getLnb2();
                int tone22kUse = satList.get(position).Antenna.getTone22kUse();
                int tone22k = satList.get(position).Antenna.getTone22k();

                Log.d(TAG, "onItemSelected: Spinner Sat, lnbType = "+lnbType+", freqLow = "+freqLow);

                spLnbType.setSelection(lnbType);
                sp22kTone.setSelection(tone22kUse == 1 ? tone22k : TONE_22K_AUTO);

                if (lnbType == LNB_TYPE_NORMAL)
                {
                    etFreqLow.setText(String.valueOf(freqLow));
                    etFreqHigh.setText(String.valueOf(freqHigh));
                }
                else if (lnbType == LNB_TYPE_UNIVERSAL)
                {
                    etFreqLow.setText(String.valueOf(9750));
                    etFreqHigh.setText(String.valueOf(10600));
                }
                else if (lnbType == LNB_TYPE_LNBF)
                {
                    etFreqLow.setText(String.valueOf(5150));
                    etFreqHigh.setText(String.valueOf(5750));
                }

                // change Transponder list
                tpList = mDtv.TpInfoGetListBySatId(satList.get(position).getSatId());
                strTpList = GetTpStringArray(tpList);
                new SelectBoxView(cont, spTP, strTpList);
                InitSpinnerTP();    // Johnny 20180814 add to fix listener not set after new SelectBoxView()

                // Tuner Tune
                TunerTune(spTP.getSelectedItemPosition(), tpList);
            }
        });
    }

    private void InitSpinnerLNBType()
    {
        Log.d(TAG, "InitSpinnerLNBType: ");

        spLnbType.setOnItemSelectedListener(new SelectBoxView.SelectBoxOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d(TAG, "onItemSelected: Spinner LNB Type, lnbType = "+position);

                super.onItemSelected(parent, view, position, id);

                switch (position)
                {
                    case LNB_TYPE_NORMAL:
                        etFreqLow.setFocusable(true);
                        etFreqHigh.setFocusable(true);
                        sp22kTone.setFocusable(true);   // Johnny 20180814 only LNB_TYPE_NORMAL can edit 22kTone
                        break;

                    case LNB_TYPE_UNIVERSAL:
                        SetHighLowFreq(false, 9750, 10600);
                        Set22kTone(false, SatInfo.TONE_22K_AUTO);   // Johnny 20180814 only LNB_TYPE_NORMAL can edit 22kTone
                        break;

                    case LNB_TYPE_LNBF:
                        SetHighLowFreq(false, 5150, 5750);
                        Set22kTone(false, SatInfo.TONE_22K_AUTO);   // Johnny 20180814 only LNB_TYPE_NORMAL can edit 22kTone
                        break;
                }

                // Tuner Tune
                TunerTune(spTP.getSelectedItemPosition(), tpList);
            }
        });
    }

    private void InitSpinner22kTone()
    {
        Log.d(TAG, "InitSpinner22kTone: ");

        sp22kTone.setOnItemSelectedListener(new SelectBoxView.SelectBoxOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d(TAG, "onItemSelected: 22KHz Tone");

                super.onItemSelected(parent, view, position, id);
//                etFreqLow.setFocusable(true); // Johnny 20180814 mark
//                etFreqHigh.setFocusable(true);
//                spLnbType.setSelection(LNB_TYPE_NORMAL);
//                satList.get(curSat).Antenna.setLnbType(LNB_TYPE_NORMAL);

                // Tuner Tune
                TunerTune(spTP.getSelectedItemPosition(), tpList);
            }
        });
    }

//    private void InitSpinnerLNBPower()
//    {
//        Log.d(TAG, "InitSpinnerLNBPower: ");
//
//        spLnbPower.setOnItemSelectedListener(new SelectBoxView.SelectBoxOnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//                Log.d(TAG, "onItemSelected: InitSpinnerLNBPower stopSelect = "+stopSelect);
//                if (stopSelect) return;
//                super.onItemSelected(parent, view, position, id);
//                isModified = true; // check if LNB Power & TP changed
//
//            }
//        });
//    }

    private void InitSpinnerTP()
    {
        Log.d(TAG, "InitSpinnerTP: ");

        spTP.setOnItemSelectedListener(new SelectBoxView.SelectBoxOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                super.onItemSelected(parent, view, position, id);
                Log.d(TAG, "onItemSelected: TP");

                // Tuner Tune
                TunerTune(spTP.getSelectedItemPosition(), tpList);
            }
        });
    }

    private void InitProgressBar()
    {
        Log.d(TAG, "InitProgressBar: ");

        final Handler handler = new Handler();
        Runnable updateProgressBar = new Runnable() {
            @Override
            public void run() {

                int strength, quality, lock, color;

                lock        = mDtv.TunerGetLockStatus(0);
                strength    = mDtv.TunerGetStrength(0);
                quality     = mDtv.TunerGetQuality(0);
                color       = (lock == 1) ? Color.GREEN : Color.RED;

                pbStrength.setProgressTintList(ColorStateList.valueOf(color));
                pbStrength.setProgress(strength);
                pbQuality.setProgressTintList(ColorStateList.valueOf(color));
                pbQuality.setProgress(quality);
                strengthTxv.setText(String.valueOf(strength).concat(" %"));
                qualityTxv.setText(String.valueOf(quality).concat(" %"));
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(updateProgressBar, 1000);
    }

    private void TunerTune(int tpPos, List<TpInfo> tpList)
    {
        Log.d(TAG, "TunerTune: ");

        int tpId = tpList.get(tpPos).getTpId();
        int frequency = tpList.get(tpPos).SatTp.getFreq();
        int symbolRate = tpList.get(tpPos).SatTp.getSymbol();
        int polar = tpList.get(tpPos).SatTp.getPolar();
        mDtv.TunerTuneDVBS(0, tpId, frequency, symbolRate, polar);
    }

    private String[] GetSatStringArray(List<SatInfo> satList)
    {
        if (satList == null)
        {
            satList = new ArrayList<>();
        }

        String[] strSatList = new String[satList.size()];

        Log.d(TAG, "GetSatStringArray: satList size = "+satList.size());

        for(int i = 0; i< satList.size(); i++)
        {
            String name = satList.get(i).getSatName();
            String angle = Float.toString(satList.get(i).getAngle());
            String angleWE = (satList.get(i).getAngleEW() == ANGLE_W) ? "W" : "E";

            strSatList[i] = (name + " " + angle + angleWE);
        }

        return strSatList;
    }

    private String[] GetTpStringArray(List<TpInfo> tpList)
    {
        Log.d(TAG, "GetTpStringArray: ");

        if(tpList == null) {
            tpList = new ArrayList<>();
        }

        String[] strTpList = new String [tpList.size()];
        for(int i = 0; i< tpList.size(); i++) {
            String freq = Integer.toString(tpList.get(i).SatTp.getFreq());
            String symbolRate = Integer.toString(tpList.get(i).SatTp.getSymbol());
            String polar = (tpList.get(i).SatTp.getPolar() == TpInfo.Sat.POLAR_H) ? " H " : " V ";

            strTpList[i] = freq + polar + symbolRate;
        }
        return strTpList;
    }

    private void SetHighLowFreq(boolean focus, int lowFreq, int highFreq)
    {
        Log.d(TAG, "SetHighLowFreq: ");


        etFreqLow.setText(String.valueOf(lowFreq));
        etFreqHigh.setText(String.valueOf(highFreq));
        etFreqLow.setFocusable(focus);
        etFreqHigh.setFocusable(focus);
        //lnbLowFreqImage.setVisibility(VISIBLE);
        //lnbHighFreqImage.setVisibility(VISIBLE);
    }

    private void Set22kTone(boolean focus, int index)
    {
        Log.d(TAG, "Set22kTone: ");

        sp22kTone.setSelection(index);
        sp22kTone.setFocusable(focus);
    }

    private void FreqDelete(EditText editBox)
    {
        Log.d(TAG, "FreqDelete: ");

        int length = editBox.length();
        editBox.setSelectAllOnFocus(false);
        editBox.getText().delete(length - 1, length);
    }

    private void FreqMoveDown(EditText editBox)
    {
        Log.d(TAG, "FreqMoveDown: ");

        String lowFreq = String.valueOf(satList.get(curSat).Antenna.getLnb1());
        String highFreq = String.valueOf(satList.get(curSat).Antenna.getLnb2());

        if (editBox == etFreqLow)
        {
            if (editBox.length() == 0)
            {
                editBox.setText(lowFreq);
            }

            etFreqHigh.requestFocus();
        }
        else if (editBox == etFreqHigh)
        {
            if (editBox.length() == 0)
            {
                editBox.setText(highFreq);
            }

            sp22kTone.requestFocus();
        }
    }

    private void FreqMoveUp(EditText editBox)
    {
        Log.d(TAG, "FreqMoveUp: ");

        String lowFreq = String.valueOf(satList.get(curSat).Antenna.getLnb1());
        String highFreq = String.valueOf(satList.get(curSat).Antenna.getLnb2());

        if (editBox == etFreqLow)
        {
            if (editBox.length() == 0) editBox.setText(lowFreq);

            spLnbType.requestFocus();
        }
        else if (editBox == etFreqHigh)
        {
            if (editBox.length() == 0) editBox.setText(highFreq);

            etFreqLow.requestFocus();
        }
    }

    private boolean CheckInput(EditText editBox, int keyCode)
    {
        boolean isSelectAll = editBox.getSelectionStart() != editBox.getSelectionEnd();

        Log.d(TAG, "CheckInput: keyCode = "+keyCode+
                ", editBox.length() = "+editBox.length()+
                ", isSelectAll = "+isSelectAll
        );

        if (keyCode == KEYCODE_0 && editBox.length() == 0)
        {
            return true;
        }

        if (keyCode == KEYCODE_0 && isSelectAll)
        {
            return true;
        }

        if ( (KEYCODE_0 <= keyCode) && (keyCode <= KEYCODE_9) )
        {
            if (editBox.length() != 5)
            {
                isModified = true;
            }
        }

        return false;
    }

    protected abstract void UpdateSetting(int diseqcPort,
                                          int selectedSat,
                                          int lnbType,
                                          int lowFreq,
                                          int highFreq,
                                          int tone22k);

    private void LogAll()
    {
        for(int i = 0; i< satList.size(); i++)
        {
            Log.d(TAG, " \nLogAll:\n SatName = "+satList.get(i).getSatName()+"\n"+
                    "        DiseqcType = "+satList.get(i).Antenna.getDiseqcType()+
                    //" DiseqcUse = "+satList.get(i).Antenna.getDiseqcUse()+
                    " Diseqc = "+satList.get(i).Antenna.getDiseqc()+
                    " PostionIndex = "+satList.get(i).getPostionIndex()+
                    " Lnb1 = "+satList.get(i).Antenna.getLnb1()+
                    " Lnb2 = "+satList.get(i).Antenna.getLnb2()
            );
        }
    }

    public abstract class ConfirmDialog extends Dialog {
        private final String TAG = "AntennaSetupActivity.ConfirmDialog";
        private TextView yes;
        private TextView no;
        private Dialog parentDialog;

        ConfirmDialog(@NonNull Context context, Dialog parent) {
            super(context, R.style.transparentDialog);
            this.parentDialog = parent;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.change_lnb_confirm_dialog);
            yes = (TextView) findViewById(R.id.message_yes_txv);
            no = (TextView) findViewById(R.id.message_no_txv);
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d(TAG, "onClick: YES");

                    OnClickYes();
                    dismiss();

                    ((Activity) cont).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parentDialog.dismiss();
                        }
                    });
                }
            });
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d(TAG, "onClick: NO");

                    dismiss();

                    ((Activity) cont).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parentDialog.dismiss();
                        }
                    });
                }
            });
        }

        abstract void OnClickYes();

    } // Confirm Dialog

}
