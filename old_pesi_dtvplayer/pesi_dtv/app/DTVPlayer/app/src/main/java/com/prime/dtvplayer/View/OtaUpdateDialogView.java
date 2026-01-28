package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.TpInfo;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by scoty on 2018/4/23.
 */
public abstract class OtaUpdateDialogView extends Dialog{
    private static final String TAG = "OtaUpdateDialogView";
    //Scoty 20180614 modify ota update dialog -s
    private TextView symbolTXV,symbolUnitTXV,qamTXV,qamUnitTXV,bandwithTXV;
    private EditText tpidEDV,freqEDV,symbolEDV;
    private Spinner qamSPINNER,bandwidthSPINNER;
    private Button cancelBTN,okBTN;
    private Context mContext = null;
    private SelectBoxView sel_qam,sel_bandwidth;
    private String[] str_qam_list,str_bandwidth_list;
    private int mTunerType;
    //Scoty 20180614 modify ota update dialog -e
    private String mTpId;
    private String mFrequency;
    private String mSymbolRate;

    private View.OnKeyListener tpIdOnKeyListener;
    private View.OnFocusChangeListener tpIdOnFocusChangeListener;

    private View.OnKeyListener freqOnKeyListener;
    private View.OnFocusChangeListener freqOnFocusChangeListener;
    private View.OnClickListener stopKeyboardOnClickListener;
    private View.OnKeyListener srOnKeyListener;
    private View.OnFocusChangeListener srOnFocusChangeListener;
    private List<TpInfo> tpList = new ArrayList<>();
    private DTVActivity mDtv;
    private int mMinFreq=0;
    private int mMaxFreq=0;

    private static final int DVBC_FREQUENCY_MAX_LENGTH = 3;
    private static final int DVBC_SYMBOLRATE_MAX_LENGTH = 4;
    private static final int DVBT_FREQUENCY_MAX_LENGTH = 3;

    private static final int DVBS_FREQUENCY_MAX_LENGTH = 5;
    private static final int DVBS_SYMBOLRATE_MAX_LENGTH = 5;


    protected OtaUpdateDialogView(Context context, int tunerType){
        super(context);
        mContext = context;
        mTunerType = tunerType;
        mDtv = (DTVActivity) context;
//        setCancelable(false);// disable click back button // Johnny 20181210 for keyboard control
        setCanceledOnTouchOutside(false);// disable click home button and other area

        show();//show dialog
        setContentView(R.layout.ota_update_dialog);
        Window window = getWindow();//get dialog widow size
        if (window == null) {
            Log.d(TAG, "OtaUpdateDialogView: window = null");
            return;
        }

        WindowManager.LayoutParams lp = window.getAttributes();//set dialog window size to lp
        lp.dimAmount=0.0f;

        window.setAttributes(lp);//set dialog parameter
        int width =lp.width;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);

        ota_dialog_init();
    }

    private void ota_dialog_init()
    {
        Window window = getWindow();
        //Scoty 20180614 modify ota update dialog -s
        tpidEDV = (EditText) window.findViewById(R.id.otatpidEDV);
        freqEDV = (EditText) window.findViewById(R.id.otafreqEDV);
        symbolTXV = (TextView) window.findViewById(R.id.otasymTXV);
        symbolEDV = (EditText) window.findViewById(R.id.otasymbolEDV);
        symbolUnitTXV = (TextView) window.findViewById(R.id.symbolUnitTXV);
        bandwithTXV = (TextView) window.findViewById(R.id.otabandwithTXV);
        qamTXV = (TextView) window.findViewById(R.id.otaqamTXV);
        qamUnitTXV = (TextView) window.findViewById(R.id.symbolUnitTXV);

        str_qam_list = mContext.getResources().getStringArray(R.array.STR_ARRAY_QAMLIST);
        qamSPINNER = (Spinner) window.findViewById(R.id.otaqamSPINNER);
        str_bandwidth_list = mContext.getResources().getStringArray(R.array.STR_ARRAY_OTA_BANDWIDTH);
        bandwidthSPINNER = (Spinner) window.findViewById(R.id.otabandwidthSPINNER);

        InitListener();
        tpidEDV.setOnClickListener(stopKeyboardOnClickListener);
        tpidEDV.setOnKeyListener(tpIdOnKeyListener);
        tpidEDV.setOnFocusChangeListener(tpIdOnFocusChangeListener);
        freqEDV.setOnClickListener(stopKeyboardOnClickListener);
        freqEDV.setOnKeyListener(freqOnKeyListener);
        freqEDV.setOnFocusChangeListener(freqOnFocusChangeListener);
        symbolEDV.setOnClickListener(stopKeyboardOnClickListener);
        symbolEDV.setOnKeyListener(srOnKeyListener);
        symbolEDV.setOnFocusChangeListener(srOnFocusChangeListener);

        if(mTunerType == TpInfo.DVBC)
        {
            freqEDV.setFilters(new InputFilter[] {//set max length
                    new InputFilter.LengthFilter(DVBC_FREQUENCY_MAX_LENGTH)
            });
            symbolEDV.setFilters(new InputFilter[] {//set max length
                    new InputFilter.LengthFilter(DVBC_SYMBOLRATE_MAX_LENGTH)
            });
            sel_qam = new SelectBoxView(mContext, qamSPINNER, str_qam_list);
            qamSPINNER.setSelection(0);

            symbolTXV.setVisibility(View.VISIBLE);
            symbolEDV.setVisibility(View.VISIBLE);
            symbolUnitTXV.setVisibility(View.VISIBLE);

            bandwithTXV.setVisibility(View.INVISIBLE);
            bandwidthSPINNER.setVisibility(View.INVISIBLE);

        }
        else if(mTunerType == TpInfo.ISDBT || mTunerType == TpInfo.DVBT)
        {
            freqEDV.setFilters(new InputFilter[] {//set max length
                    new InputFilter.LengthFilter(DVBT_FREQUENCY_MAX_LENGTH)
            });
            symbolTXV.setVisibility(View.INVISIBLE);
            symbolEDV.setVisibility(View.INVISIBLE);
            symbolUnitTXV.setVisibility(View.INVISIBLE);

            bandwithTXV.setVisibility(View.VISIBLE);
            bandwidthSPINNER.setVisibility(View.VISIBLE);

            sel_bandwidth = new SelectBoxView(mContext, bandwidthSPINNER, str_bandwidth_list);
            bandwidthSPINNER.setSelection(0);

            qamTXV.setVisibility(View.GONE);
            qamSPINNER.setVisibility(View.GONE);
        }
        else if(mTunerType == TpInfo.DVBS){
            Log.d(TAG, "ota_dialog_init: DVBS ");
            qamSPINNER.setVisibility(View.GONE);

        }
        tpList = mDtv.TpInfoGetList(mTunerType); 
        if(tpList==null)
            tpList = new ArrayList<>();
        else
        {
            if(mTunerType == TpInfo.DVBC) {
                mMinFreq = tpList.get(0).CableTp.getFreq();
                mMaxFreq = tpList.get(tpList.size()-1).CableTp.getFreq();
            }else if(mTunerType == TpInfo.DVBT || mTunerType == TpInfo.ISDBT) {
                mMinFreq = tpList.get(0).TerrTp.getFreq();
                mMaxFreq = tpList.get(tpList.size()-1).TerrTp.getFreq();
            }else if(mTunerType == TpInfo.DVBS) {
                mMinFreq = tpList.get(0).SatTp.getFreq();
                mMaxFreq = tpList.get(tpList.size() - 1).SatTp.getFreq();
            }
        }

        cancelBTN = (Button) window.findViewById(R.id.otadialogcancelBTN);
        cancelBTN.setOnClickListener(new onCancelclick());
        okBTN = (Button) window.findViewById(R.id.otadialogokBTN);
        okBTN.setOnClickListener(new onOkclick());
        //Scoty 20180614 modify ota update dialog -e

        //get default value
        mTpId = tpidEDV.getText().toString();
        mFrequency = freqEDV.getText().toString();
        if(mTunerType == TpInfo.DVBC || mTunerType == TpInfo.DVBS) {
            mSymbolRate = symbolEDV.getText().toString();
        }

    }

    private boolean checkFreq(int freq){
        Log.d(TAG, "checkFreq：　mMinFreq"+mMinFreq+", mMaxFreq="+mMaxFreq);
        int tmpFreq = freq*1000;
        if(tmpFreq < mMinFreq || tmpFreq > mMaxFreq)
            return false;
        else
            return true;
    }

    private void InitListener(){
        stopKeyboardOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: stop Keyboard");
                v.requestFocus();
            }
        };

        tpIdOnFocusChangeListener = new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    tpidEDV.selectAll();
                else{
                    if(tpidEDV.getText().toString().isEmpty())
                        tpidEDV.setText(mTpId);
                }
            }
        };

        tpIdOnKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean isKeyDown = (event.getAction() == KeyEvent.ACTION_DOWN);
                EditText etVar = (EditText) v;
                if (isKeyDown)
                {
                    if(etVar.getText().toString().isEmpty())
                        Log.d(TAG, "TTT onKey: isKeyDown-value=null");
                    else
                        Log.d(TAG, "TTT onKey: isKeyDown-value="+Integer.valueOf(etVar.getText().toString()));

                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_0:
                            boolean isStartEqualEnd = tpidEDV.getSelectionStart() == tpidEDV.getSelectionEnd() ? true: false;
                            if(!isStartEqualEnd || etVar.getText().toString().isEmpty())
                                return true;
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            return true;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            freqEDV.requestFocus();
                            return true;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            deleteEditText(tpidEDV);
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        };



        //freq
        freqOnFocusChangeListener = new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    freqEDV.selectAll();
                else{
                    boolean isFreqEmpty;
                    isFreqEmpty = freqEDV.getText().toString().isEmpty();
                    if( isFreqEmpty || (!isFreqEmpty && checkFreq(Integer.valueOf(freqEDV.getText().toString()))==false))
                        freqEDV.setText(mFrequency);
                }
            }
        };

        freqOnKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean isKeyDown = (event.getAction() == KeyEvent.ACTION_DOWN);
                EditText etVar = (EditText) v;
                if (isKeyDown)
                {
                    if(etVar.getText().toString().isEmpty())
                        Log.d(TAG, "TTT onKey: isKeyDown-value=null");
                    else
                        Log.d(TAG, "TTT onKey: isKeyDown-value="+Integer.valueOf(etVar.getText().toString()));

                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_0:
                            boolean isStartEqualEnd = freqEDV.getSelectionStart() == freqEDV.getSelectionEnd() ? true: false;
                            if(!isStartEqualEnd || etVar.getText().toString().isEmpty())
                                return true;
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            if(mTunerType == TpInfo.DVBC) {
                                symbolEDV.requestFocus();
                            }else if(mTunerType == TpInfo.DVBT || mTunerType == TpInfo.ISDBT) {
                                bandwidthSPINNER.requestFocus();
                            }
                            return true;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            tpidEDV.requestFocus();
                            return true;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            deleteEditText(freqEDV);
                            return true;
                    }
                }
                return false;
            }
        };

        //sr
        srOnFocusChangeListener = new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    symbolEDV.selectAll();
                else{
                    if(symbolEDV.getText().toString().isEmpty())
                        symbolEDV.setText(mSymbolRate);
                }
            }
        };

        srOnKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean isKeyDown = (event.getAction() == KeyEvent.ACTION_DOWN);
                EditText etVar = (EditText) v;
                if (isKeyDown)
                {
                    if(etVar.getText().toString().isEmpty())
                        Log.d(TAG, "TTT onKey: isKeyDown-value=null");
                    else
                        Log.d(TAG, "TTT onKey: isKeyDown-value="+Integer.valueOf(etVar.getText().toString()));

                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_0:
                            if(mTunerType == TpInfo.DVBC) {
                                boolean isStartEqualEnd = symbolEDV.getSelectionStart() == symbolEDV.getSelectionEnd() ? true: false;
                                if(!isStartEqualEnd || etVar.getText().toString().isEmpty())
                                    return true;
                            }
                            //Log.d(TAG, "getSelectionEnd="+symbolEDV.getSelectionEnd()+", start="+symbolEDV.getSelectionStart()+", isSelected="+symbolEDV.isSelected());
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            qamSPINNER.requestFocus();
                            return true;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            freqEDV.requestFocus();
                            return true;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            deleteEditText(symbolEDV);
                            return true;
                    }
                }
                return false;
            }
        };
    }

    class onOkclick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onOkclick : onclick  mTunerType = " + mTunerType + " C = " + TpInfo.DVBC);
            String tpid_string,freq_string,symbol_string,bandwidth_string;
            int tpid,freq,symbol,qam,bandwidth;

            if(mTunerType == TpInfo.DVBC) {
                Log.d(TAG, "OtaUpdateDialogView: DVBC = " + mTunerType);
                tpid_string = tpidEDV.getText().toString();
                freq_string = freqEDV.getText().toString();
                symbol_string = symbolEDV.getText().toString();

                tpid = Integer.valueOf(tpid_string);
                freq = Integer.valueOf(freq_string);
                symbol = Integer.valueOf(symbol_string);
                qam = sel_qam.GetSelectedItemIndex();

                onSetPositiveButton(tpid, freq, symbol, qam, 0);
            }
            else if(mTunerType == TpInfo.ISDBT || mTunerType == TpInfo.DVBT)
            {
                Log.d(TAG, "OtaUpdateDialogView: DVBT/DVBT2 = " + mTunerType);
                tpid_string = tpidEDV.getText().toString();
                freq_string = freqEDV.getText().toString();
                bandwidth_string = symbolEDV.getText().toString();

                tpid = Integer.valueOf(tpid_string);
                freq = Integer.valueOf(freq_string);
                bandwidth = Integer.valueOf(bandwidth_string);

                onSetPositiveButton(tpid, freq, 0, 0, bandwidth);
            }
            else if(mTunerType == TpInfo.DVBS)
            {
                Log.d(TAG, "OtaUpdateDialogView: DVBS = " + mTunerType);
                tpid_string = tpidEDV.getText().toString();
                freq_string = freqEDV.getText().toString();
                symbol_string = symbolEDV.getText().toString();

                tpid = Integer.valueOf(tpid_string);
                freq = Integer.valueOf(freq_string);
                symbol = Integer.valueOf(symbol_string);

                onSetPositiveButton(tpid, freq, symbol, 0, 0);
            }
            dismiss();
        }
    }

    class onCancelclick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onCancelclick : onclick");
            dismiss();
        }
    }

    abstract public void onSetPositiveButton(int tpid, int freq, int symbol, int qam, int bandwidth);

    private boolean deleteEditText(EditText et) {
        int position = et.getSelectionStart();
        Editable editable = et.getText();

        if (position > 0) {
            editable.delete(position - 1, position);
            return true;
        } else {
            return false; //delete fail
        }
    }
   
}
