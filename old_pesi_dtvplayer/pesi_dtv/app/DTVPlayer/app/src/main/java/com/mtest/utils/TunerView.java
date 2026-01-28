package com.mtest.utils;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mtest.module.TunerModule;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.utils.TVScanParams;

import java.lang.ref.WeakReference;
import java.util.List;

public class TunerView {
    private final String TAG = getClass().getSimpleName();

    public static int STATUS_NONE = 0;
    public static int STATUS_FAIL = 1;
    public static int STATUS_PASS = 2;
    private static int STATUS_SEARCHING = 3;
    public static int STATUS_SELECT = 4;

    private static final int MAX_FREQ_LENGTH_DVBC = 4;
    private static final int MAX_FREQ_LENGTH_ISDBT = 6;
    private static final int MAX_FREQ_LENGTH_DVBS = 4;
    private static final int MAX_SYMBOL_LENGTH_DVBC = 4;
    private static final int MAX_SYMBOL_LENGTH_DVBS = 5;

    private static String[] QAM_STRING_ARRAY = {"16", "32", "64", "128", "256"};
    private static String[] BAND_STRING_ARRAY = {"6M", "7M", "8M"};

    private final WeakReference<Context> mContRef;

    private View mTunerView;
    private TextView mTvIndex;
//    private TextView mTvFreq;
    private EditText mEtFreq;
    private TextView mTvSymbol;
    private EditText mEtSymbol;
    private TextView mTvQam;
    private TextView mTvQamValue;
    private ImageView mIvQamUp;
    private ImageView mIvQamDown;
    private TextView mTvBand;
    private TextView mTvBandValue;
    private ImageView mIvBandUp;
    private ImageView mIvBandDown;
    private TextView mTvRF;
    private TextView mTvGeneration;

    private int mTunerType;
    private int mTunerIndex;
    private int mMaxFreqLength;
    private int mMaxSymbolLength;

    private DTVActivity mDTVActivity;
    private TpInfo mTpInfo;

    private View.OnFocusChangeListener mEtFreqFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            EditText editText = (EditText) view;

            if (hasFocus) {
                editText.setBackgroundResource(R.drawable.shape_rectangle_edit);
                editText.setText("");
                editText.addTextChangedListener(mEtFreqTextWatcher);
            } else {
                editText.setBackgroundResource(R.drawable.shape_rectangle_orange);
                editText.removeTextChangedListener(mEtFreqTextWatcher);

                if (TextUtils.isEmpty(editText.getText())) {    // return to init value if empty
                    if (mTunerType == TpInfo.DVBC) {
                        editText.setText(String.valueOf(mTpInfo.CableTp.getFreq() / 1000));
                    }
                    else if (mTunerType == TpInfo.ISDBT) {
                        editText.setText(String.valueOf(mTpInfo.TerrTp.getFreq()));
                    }
                    else if (mTunerType == TpInfo.DVBS) {
                        SatInfo satInfo = mDTVActivity.SatInfoGet(mTpInfo.getSatId());
                        editText.setText(String.valueOf(getInputFreqDVBS(mTpInfo.SatTp.getFreq(), satInfo)));
                    }
                }
            }
        }
    };

    private View.OnFocusChangeListener mEtSymbolFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            EditText editText = (EditText) view;

            if (hasFocus) {
                editText.setBackgroundResource(R.drawable.shape_rectangle_edit);
                editText.setText("");
                editText.addTextChangedListener(mEtSymbolTextWatcher);
            } else {
                editText.setBackgroundResource(R.drawable.shape_rectangle_orange);
                editText.removeTextChangedListener(mEtSymbolTextWatcher);

                if (TextUtils.isEmpty(editText.getText())) {    // return to init value if empty
                    if (mTunerType == TpInfo.DVBC) {
                        editText.setText(String.valueOf(mTpInfo.CableTp.getSymbol()));
                    }
                    else if (mTunerType == TpInfo.DVBS) {
                        editText.setText(String.valueOf(mTpInfo.SatTp.getSymbol()));
                    }
                }
            }
        }
    };

    private View.OnFocusChangeListener mTvQamValueFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            TextView textView = (TextView) view;

            if (hasFocus) {
                textView.setBackgroundResource(R.drawable.shape_rectangle_edit);
            } else {
                textView.setBackgroundResource(R.drawable.shape_rectangle_orange);
            }
        }
    };

    private View.OnFocusChangeListener mTvBandValueFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            TextView textView = (TextView) view;

            if (hasFocus) {
                textView.setBackgroundResource(R.drawable.shape_rectangle_edit);
            } else {
                textView.setBackgroundResource(R.drawable.shape_rectangle_orange);
            }
        }
    };

    private View.OnKeyListener mEtFreqKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keycode, KeyEvent keyEvent) {
            boolean isActionDown = keyEvent.getAction() == KeyEvent.ACTION_DOWN;
            EditText editText = (EditText) v;

            if (isActionDown) {
//                switch (keycode) {
//                    case KeyEvent.KEYCODE_DPAD_CENTER:
//                        setEditAreaFocusable(mTunerType, false);
//                        return true;
//                    case KeyEvent.KEYCODE_DPAD_UP:
//                        int selectionStart = editText.getSelectionStart();
//                        if (selectionStart > 0) {
//                            editText.getText().delete(selectionStart - 1, selectionStart);
//                        }
//                        return true;
//                    case KeyEvent.KEYCODE_DPAD_DOWN:    // Block dpad_down
//                        return true;
//                }
            }

            return false;
        }
    };

    private View.OnKeyListener mEtSymbolKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keycode, KeyEvent keyEvent) {
            boolean isActionDown = keyEvent.getAction() == KeyEvent.ACTION_DOWN;
            EditText editText = (EditText) v;

            if (isActionDown) {
//                switch (keycode) {
//                    case KeyEvent.KEYCODE_DPAD_CENTER:
//                        setEditAreaFocusable(mTunerType, false);
//                        return true;
//                    case KeyEvent.KEYCODE_DPAD_UP:
//                        int selectionStart = editText.getSelectionStart();
//                        if (selectionStart > 0) {
//                            editText.getText().delete(selectionStart - 1, selectionStart);
//                        }
//                        return true;
//                    case KeyEvent.KEYCODE_DPAD_DOWN:    // Block dpad_down
//                        return true;
//                }
            }

            return false;
        }
    };

    private View.OnKeyListener mTvQamValueKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keycode, KeyEvent keyEvent) {
            boolean isActionDown = keyEvent.getAction() == KeyEvent.ACTION_DOWN;
            TextView textView = (TextView) v;
            if (isActionDown) {
                int qam = mTpInfo.CableTp.getQam();
                switch (keycode) {
//                    case KeyEvent.KEYCODE_DPAD_CENTER:
//                        setEditAreaFocusable(mTunerType, false);
//                        return true;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (qam > 0) {
                            qam--;
                            textView.setText(QAM_STRING_ARRAY[qam]);
                            mTpInfo.CableTp.setQam(qam);
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (qam < QAM_STRING_ARRAY.length - 1) {
                            qam++;
                            textView.setText(QAM_STRING_ARRAY[qam]);
                            mTpInfo.CableTp.setQam(qam);
                        }
                        return true;
                }

                if (keycode >= KeyEvent.KEYCODE_0 && keycode <= KeyEvent.KEYCODE_9) {   // between 0~9
                    return true; // block 0~9
                }
            }

            return false;
        }
    };

    private View.OnKeyListener mTvBandValueKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keycode, KeyEvent keyEvent) {
            boolean isActionDown = keyEvent.getAction() == KeyEvent.ACTION_DOWN;
            TextView textView = (TextView) v;
            if (isActionDown) {
                int band = mTpInfo.TerrTp.getBand();
                switch (keycode) {
//                    case KeyEvent.KEYCODE_DPAD_CENTER:
//                        setEditAreaFocusable(mTunerType, false);
//                        return true;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (band > 0) {
                            band--;
                            textView.setText(BAND_STRING_ARRAY[band]);
                            mTpInfo.TerrTp.setBand(band);
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (band < BAND_STRING_ARRAY.length - 1) {
                            band++;
                            textView.setText(BAND_STRING_ARRAY[band]);
                            mTpInfo.TerrTp.setBand(band);
                        }
                        return true;
                }

                if (keycode >= KeyEvent.KEYCODE_0 && keycode <= KeyEvent.KEYCODE_9) {   // between 0~9
                    return true; // block 0~9
                }
            }

            return false;
        }
    };

    private TextWatcher mEtFreqTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == mMaxFreqLength) {
                if (mTunerType == TpInfo.DVBC) {
                    mEtSymbol.requestFocus();
                }
                else if (mTunerType == TpInfo.ISDBT) {
                    mTvBandValue.requestFocus();
                }
                else if (mTunerType == TpInfo.DVBS) {
                    mEtSymbol.requestFocus();
                }
            }
        }
    };

    private TextWatcher mEtSymbolTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == mMaxSymbolLength) {
                if (mTunerType == TpInfo.DVBC) {
                    mTvQamValue.requestFocus();
                }
                else if (mTunerType == TpInfo.DVBS) {
                    setEditAreaFocusable(mTunerType, false);
                }
            }
        }
    };

    public TunerView(View tunerView, int tunerIndex, int tunerType, Context context) {
        mContRef = new WeakReference<>(context);
        mDTVActivity = (DTVActivity)mContRef.get();

        mTunerView = tunerView;
        mTvIndex = (TextView) tunerView.findViewById(R.id.tv_tuner_index);
//        mTvFreq = (TextView) tunerView.findViewById(R.id.tv_tuner_freq);
        mEtFreq = (EditText) tunerView.findViewById(R.id.et_tuner_freq);
        mTvSymbol = (TextView) tunerView.findViewById(R.id.tv_tuner_symbol_rate);
        mEtSymbol = (EditText) tunerView.findViewById(R.id.et_tuner_symbol_rate);
        mTvQam = (TextView) tunerView.findViewById(R.id.tv_tuner_qam);
        mTvQamValue = (TextView) tunerView.findViewById(R.id.tv_tuner_qam_value);
        mIvQamUp = (ImageView) tunerView.findViewById(R.id.iv_tuner_qam_up);
        mIvQamDown = (ImageView) tunerView.findViewById(R.id.iv_tuner_qam_down);
        mTvBand = (TextView) tunerView.findViewById(R.id.tv_tuner_bandwidth);
        mTvBandValue = (TextView) tunerView.findViewById(R.id.tv_tuner_bandwidth_value);
        mIvBandUp = (ImageView) tunerView.findViewById(R.id.iv_tuner_bandwidth_up);
        mIvBandDown = (ImageView) tunerView.findViewById(R.id.iv_tuner_bandwidth_down);
        mTvRF = (TextView) tunerView.findViewById(R.id.tv_tuner_rf);
        mTvGeneration = (TextView) tunerView.findViewById(R.id.tv_tuner_generation);

        setTunerIndex(tunerIndex);
        setTunerType(tunerType);
        setTpInfo(tunerType);
        setTunerDefaultParams(mTpInfo);
    }

    private void setTunerIndex(int tunerIndex) {
        mTunerIndex = tunerIndex;
        mTvIndex.setText(String.valueOf(tunerIndex));
    }

    private void setTunerType(int tunerType) {
        mTunerType = tunerType;

        if (tunerType == TpInfo.DVBC) {
            mMaxFreqLength = MAX_FREQ_LENGTH_DVBC;
            mMaxSymbolLength = MAX_SYMBOL_LENGTH_DVBC;

            mTunerView.setVisibility(View.VISIBLE);
            mEtFreq.setFilters(new InputFilter[] { new InputFilter.LengthFilter(mMaxFreqLength) });  // freq max length = 4
            mTvSymbol.setVisibility(View.VISIBLE);
            mEtSymbol.setVisibility(View.VISIBLE);
            mEtSymbol.setFilters(new InputFilter[] { new InputFilter.LengthFilter(mMaxSymbolLength) });  // symbol max length = 4
            mTvQam.setVisibility(View.VISIBLE);
            mTvQamValue.setVisibility(View.VISIBLE);
            mIvQamUp.setVisibility(View.VISIBLE);
            mIvQamDown.setVisibility(View.VISIBLE);

            mEtFreq.setOnFocusChangeListener(mEtFreqFocusChangeListener);
            mEtFreq.setOnKeyListener(mEtFreqKeyListener);
            mEtSymbol.setOnFocusChangeListener(mEtSymbolFocusChangeListener);
            mEtSymbol.setOnKeyListener(mEtSymbolKeyListener);
            mTvQamValue.setOnFocusChangeListener(mTvQamValueFocusChangeListener);
            mTvQamValue.setOnKeyListener(mTvQamValueKeyListener);
        }
        else if (tunerType == TpInfo.ISDBT) {
            mMaxFreqLength = MAX_FREQ_LENGTH_ISDBT;

            mTunerView.setVisibility(View.VISIBLE);
            mEtFreq.setFilters(new InputFilter[] { new InputFilter.LengthFilter(mMaxFreqLength) });  // freq max length = 6
            mTvBand.setVisibility(View.VISIBLE);
            mTvBandValue.setVisibility(View.VISIBLE);
            mIvBandUp.setVisibility(View.VISIBLE);
            mIvBandDown.setVisibility(View.VISIBLE);

            mEtFreq.setOnFocusChangeListener(mEtFreqFocusChangeListener);
            mEtFreq.setOnKeyListener(mEtFreqKeyListener);
            mTvBandValue.setOnFocusChangeListener(mTvBandValueFocusChangeListener);
            mTvBandValue.setOnKeyListener(mTvBandValueKeyListener);
        }
        else if (tunerType == TpInfo.DVBS) {
            mMaxFreqLength = MAX_FREQ_LENGTH_DVBS;
            mMaxSymbolLength = MAX_SYMBOL_LENGTH_DVBS;

            mTunerView.setVisibility(View.VISIBLE);
            mEtFreq.setFilters(new InputFilter[] { new InputFilter.LengthFilter(mMaxFreqLength) });  // freq max length = 4
            mTvSymbol.setVisibility(View.VISIBLE);
            mEtSymbol.setVisibility(View.VISIBLE);
            mEtSymbol.setFilters(new InputFilter[] { new InputFilter.LengthFilter(mMaxSymbolLength) });  // symbol max length = 5
            mTvRF.setVisibility(View.VISIBLE);
            mTvGeneration.setVisibility(View.VISIBLE);

            mEtFreq.setOnFocusChangeListener(mEtFreqFocusChangeListener);
            mEtFreq.setOnKeyListener(mEtFreqKeyListener);
            mEtSymbol.setOnFocusChangeListener(mEtSymbolFocusChangeListener);
            mEtSymbol.setOnKeyListener(mEtSymbolKeyListener);
        }
    }

    private void setTpInfo(int tunerType) {
        List<SatInfo> satInfoList = mDTVActivity.SatInfoGetList(tunerType); // get sat list by tuner type
        if (satInfoList == null || satInfoList.isEmpty()) {
            Log.d(TAG, "No SatList!");
            Toast.makeText(mContRef.get(), "No SatList!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<TpInfo> tpInfoList = mDTVActivity.TpInfoGetListBySatId(satInfoList.get(0).getSatId()); // use first sat for search
        if (tpInfoList == null || tpInfoList.isEmpty()) {
            Log.d(TAG, "No TpList!");
            Toast.makeText(mContRef.get(), "No TpList!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mTunerIndex >= tpInfoList.size()) {
            Log.d(TAG, "Not Enough TpList!");
            Toast.makeText(mContRef.get(), "Not Enough TpList!", Toast.LENGTH_SHORT).show();
            return;
        }

        mTpInfo = tpInfoList.get(mTunerIndex);    // get tunerIndex's tp in tp list
    }

    private void setTunerDefaultParams(TpInfo tpInfo) {
        if (tpInfo == null) {
            return;
        }

        int tunerType = tpInfo.getTunerType();
        if (tunerType == TpInfo.DVBC) {
            mEtFreq.setText(String.valueOf(tpInfo.CableTp.getFreq() / 1000));
            mEtSymbol.setText(String.valueOf(tpInfo.CableTp.getSymbol()));
            mTvQamValue.setText(QAM_STRING_ARRAY[tpInfo.CableTp.getQam()]);
        }
        else if (tunerType == TpInfo.ISDBT) {
            mEtFreq.setText(String.valueOf(tpInfo.TerrTp.getFreq()));
            mTvBandValue.setText(BAND_STRING_ARRAY[tpInfo.TerrTp.getBand()]);
        }
        else if (tunerType == TpInfo.DVBS) {
            SatInfo satInfo = mDTVActivity.SatInfoGet(tpInfo.getSatId());
            int freq = getInputFreqDVBS(tpInfo.SatTp.getFreq(), satInfo);
            mEtFreq.setText(String.valueOf(freq));
            mEtSymbol.setText(String.valueOf(tpInfo.SatTp.getSymbol()));
        }
    }

    private void setEditAreaFocusable(int tunerType, boolean focusable) {
        if (focusable) {
            if (tunerType == TpInfo.DVBC) {
                mEtFreq.setFocusable(true);
                mEtSymbol.setFocusable(true);
                mTvQamValue.setFocusable(true);
            }
            else if (tunerType == TpInfo.ISDBT) {
                mEtFreq.setFocusable(true);
                mTvBandValue.setFocusable(true);
            }
            else if (tunerType == TpInfo.DVBS) {
                mEtFreq.setFocusable(true);
                mEtSymbol.setFocusable(true);
            }

            mEtFreq.requestFocus();  // focus to freq when start edit
        } else {
            if (tunerType == TpInfo.DVBC) {
                mEtFreq.setFocusable(false);
                mEtSymbol.setFocusable(false);
                mTvQamValue.setFocusable(false);
            }
            else if (tunerType == TpInfo.ISDBT) {
                mEtFreq.setFocusable(false);
                mTvBandValue.setFocusable(false);
            }
            else if (tunerType == TpInfo.DVBS) {
                mEtFreq.setFocusable(false);
                mEtSymbol.setFocusable(false);
            }
        }
    }

    // index of qam str array
    private int getQamFromString(String str) {
        int ret = 0;
        for (int i = 0; i < QAM_STRING_ARRAY.length; i++) {
            if (str.equals(QAM_STRING_ARRAY[i])) {
                ret = i;
                break;
            }
        }

        return ret;
    }

    // index of band str array
    private int getBandFromString(String str) {
        int ret = 0;
        for (int i = 0; i < BAND_STRING_ARRAY.length; i++) {
            if (str.equals(BAND_STRING_ARRAY[i])) {
                ret = i;
                break;
            }
        }

        return ret;
    }

    // convert from input to actual freq accepted for service ch search
    // work in lnb type = universal
    private int getActualFreqDVBS(int inputFreq, SatInfo satInfo) {
        if (satInfo == null) {
            Log.d(TAG, "getActualFreq: Null SatInfo!");
            return -1;
        }

        int lnb1 = satInfo.Antenna.getLnb1();
        int lnb2 = satInfo.Antenna.getLnb2();
        int actualFreq = inputFreq + lnb1;

        if (actualFreq >= 11700) {   // by Hisi code, type = universal
            actualFreq = inputFreq + lnb2;
        }

        return actualFreq;
    }

    // convert from service tpinfo freq to freq for display
    // work in lnb type = universal
    private int getInputFreqDVBS(int actualFreq, SatInfo satInfo) {
        if (satInfo == null) {
            Log.d(TAG, "getInputFreqDVBS: Null SatInfo!");
            return -1;
        }

        int lnb1 = satInfo.Antenna.getLnb1();
        int lnb2 = satInfo.Antenna.getLnb2();
        int inputFreq = actualFreq - lnb2;

        if (inputFreq < 1950) {
            inputFreq = actualFreq - lnb1;
        }

        return inputFreq;
    }

    private boolean checkTpInfoNotAvailable(TpInfo tpInfo, int tunerType) {
        return tpInfo == null
                ||
                ((tunerType == TpInfo.DVBC && tpInfo.CableTp == null)
                        || (tunerType == TpInfo.ISDBT && tpInfo.TerrTp == null)
                        || (tunerType == TpInfo.DVBS && tpInfo.SatTp == null));
    }

    public void setStatus(int status) {

        if (status == STATUS_NONE) {
            mTvIndex.setBackgroundResource(R.drawable.shape_rectangle_blue_bright);
        }
        else if (status == STATUS_FAIL) {
            mTvIndex.setBackgroundResource(R.drawable.shape_rectangle_fail);
        }
        else if (status == STATUS_PASS) {
            mTvIndex.setBackgroundResource(R.drawable.shape_rectangle_pass);
        }
        else if (status == STATUS_SEARCHING) {
            mTvIndex.setBackgroundResource(R.drawable.shape_rectangle_edit);
        }
        else if (status == STATUS_SELECT) {
            mTvIndex.setBackgroundResource(R.drawable.shape_rectangle_focus);
        }
    }

    public void startEditParams() {
        setEditAreaFocusable(mTunerType, true);
    }

    public void stopEditParams() {
        setEditAreaFocusable(mTunerType, false);
    }

    public boolean tunerSearch() {

        if (!updateTpInfo()) {
            return false;
        }

        TunerModule tunerModule = new TunerModule(mContRef.get());
        tunerModule.ScanParamsStartScan(
                mTunerIndex,                                // tunerID is tunerIndex for now
                mTpInfo.getTpId(),
                mTpInfo.getSatId(),
                TVScanParams.SCAN_MODE_MANUAL,
                TVScanParams.SEARCH_OPTION_TV_ONLY,     // scan only TV program
                TVScanParams.SEARCH_OPTION_FTA_ONLY,    // scan no ca program
                0,
                0
        );

        setStatus(TunerView.STATUS_SEARCHING);
        return true;
    }

    public boolean updateTpInfo() {
        if(checkTpInfoNotAvailable(mTpInfo, mTunerType)) {
            Toast.makeText(mContRef.get(), "TpInfo Not Available!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mTunerType == TpInfo.DVBC) {
            mTpInfo.CableTp.setFreq(Integer.parseInt(mEtFreq.getText().toString()) * 1000);
            mTpInfo.CableTp.setSymbol(Integer.parseInt(mEtSymbol.getText().toString()));
            mTpInfo.CableTp.setQam(getQamFromString(mTvQamValue.getText().toString()));
        }
        else if (mTunerType == TpInfo.ISDBT) {
            mTpInfo.TerrTp.setFreq(Integer.parseInt(mEtFreq.getText().toString()));
            mTpInfo.TerrTp.setBand(getBandFromString(mTvBandValue.getText().toString()));
        }
        else if (mTunerType == TpInfo.DVBS) {
            SatInfo satInfo = mDTVActivity.SatInfoGet(mTpInfo.getSatId());
            int actualFreqDVBS = getActualFreqDVBS(Integer.parseInt(mEtFreq.getText().toString()), satInfo);

            mTpInfo.SatTp.setFreq(actualFreqDVBS);
            mTpInfo.SatTp.setSymbol(Integer.parseInt(mEtSymbol.getText().toString()));
        }

        TunerModule tunerModule = new TunerModule(mContRef.get());
        tunerModule.TpInfoUpdate(mTpInfo);
        return true;
    }

    public void setFreq(int freq) {
        if (freq < 0) {
            return;
        }

        String strFreq = String.valueOf(freq);

        if (mTunerType == TpInfo.DVBC && strFreq.length() <= MAX_FREQ_LENGTH_DVBC) {
            mEtFreq.setText(strFreq);
        }
        else if (mTunerType == TpInfo.ISDBT && strFreq.length() <= MAX_FREQ_LENGTH_ISDBT) {
            mEtFreq.setText(strFreq);
        }
        else if (mTunerType == TpInfo.DVBS && strFreq.length() <= MAX_FREQ_LENGTH_DVBS) {
            mEtFreq.setText(strFreq);
        }
    }

    public void setSymbolRate(int symbol) {
        if (symbol < 0) {
            return;
        }

        String strSymbol = String.valueOf(symbol);

        if (mTunerType == TpInfo.DVBC && strSymbol.length() <= MAX_SYMBOL_LENGTH_DVBC) {
            mEtSymbol.setText(strSymbol);
        }
        else if (mTunerType == TpInfo.DVBS && strSymbol.length() <= MAX_SYMBOL_LENGTH_DVBS) {
            mEtSymbol.setText(strSymbol);
        }
    }

    public void setQam(int qam) {
        if (qam < 0) {
            return;
        }

        if (mTunerType == TpInfo.DVBC && qam <= QAM_STRING_ARRAY.length) {
            mTvQamValue.setText(QAM_STRING_ARRAY[qam]);
        }
    }

    public void setBandwidth(int bandwidth) {
        if (bandwidth < 0) {
            return;
        }

        if (mTunerType == TpInfo.ISDBT && bandwidth <= BAND_STRING_ARRAY.length) {
            mTvBandValue.setText(BAND_STRING_ARRAY[bandwidth]);
        }
    }

    public int getTunerType() {
        return mTunerType;
    }

    public int getTpID() {
        if(checkTpInfoNotAvailable(mTpInfo, mTunerType)) {
            Toast.makeText(mContRef.get(), "TpInfo Not Available!", Toast.LENGTH_SHORT).show();
            return -1;
        }

        return mTpInfo.getTpId();
    }
}
