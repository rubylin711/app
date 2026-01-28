package com.prime.homeplus.settings.system;

import android.app.ActionBar;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.TVMessage;
import com.prime.datastructure.utils.TVTunerParams;
import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.PrimeApplication;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

import java.util.List;

public class SignalView extends ThirdLevelView {
    private String TAG = "HomePlus-SignalView";
    private SettingsRecyclerView settingsRecyclerView;

    private Handler mHandler;
    private Context mContext;
    private int mTunerId = 1, mStrength, mSNR;


    public SignalView(int i, Context context, SettingsRecyclerView settingsRecyclerView) {
        super(i, context, settingsRecyclerView);
        this.settingsRecyclerView = settingsRecyclerView;
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_signal;
    }

    private Button btnSignal;


    public void onFocus() {
        btnSignal.requestFocus();
    }

    @Override
    public void onViewCreated() {
        initPopWindow();
        initPopWindowAdv();

        btnSignal = (Button) findViewById(R.id.btnSignal);

        btnSignal.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        settingsRecyclerView.backToList();
                    }
                }
                return false;
            }
        });

        btnSignal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopWindow();
            }
        });
    }

    private View popupView, popupViewAdv;
    private static PopupWindow popupWindow;
    private Button btnAdvance;

    private EditText etFrequency, etSR;
    private Button btnSearch, btnCancel, btnQam;
    private TextView tvQamValue, tvStrengthValue, tvSnrValue, tvProcess;
    private ProgressBar pbSearchBar, pbStrengthBar, pbSnrBar;
    private int mQam = TpInfo.Cable.QAM_256;
    private int qam_value[] = {16,32,64,128,256};

    private boolean moveFocusNext(View currentView, int keyCode){
        if(currentView == etFrequency){
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
            {
                etSR.requestFocus();
                return true;
            }
        }
        if(currentView == etSR){
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                // 將焦點移動到符碼率 EditText
                etFrequency.requestFocus();
                return true; // 表示事件已處理
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                // 將焦點移動到符碼率 EditText
                btnQam.requestFocus();
                return true; // 表示事件已處理
            }
        }
        if(currentView == btnQam){
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
                mQam--;
                if(mQam < TpInfo.Cable.QAM_16)
                    mQam = TpInfo.Cable.QAM_256;
                tvQamValue.setText(Integer.toString(qam_value[mQam]));
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                mQam++;
                if(mQam >TpInfo.Cable.QAM_256)
                    mQam = TpInfo.Cable.QAM_16;
                tvQamValue.setText(Integer.toString(qam_value[mQam]));
                return true;
            }
        }
        return false;
    }

    private void set_key_listener(View v) {
        v.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    return moveFocusNext(v, keyCode);
                }
                return false;
            }
        });
    }

    private void set_OnClick_listenner(View v){
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.equals(btnSearch)) {
                    int freq = Integer.valueOf(etFrequency.getText().toString());
                    int symbol = Integer.valueOf(etSR.getText().toString());

                    PrimeUtils.tune_lock(mTunerId, freq, symbol, mQam);
                    update_tuner_status(false, mTunerId);
                } else if (v.equals(btnCancel)) {
                    popupWindow.dismiss();
                }
            }
        });
    }

    private void init_view(View v){
        etFrequency = v.findViewById(R.id.etFrequency);
        set_key_listener(etFrequency);

        etSR = v.findViewById(R.id.etSR);
        set_key_listener(etSR);
        btnQam = v.findViewById(R.id.btnQAM);
        set_key_listener(btnQam);
        tvQamValue = v.findViewById(R.id.tvQamValue);

        pbSearchBar = v.findViewById(R.id.pbProgress);
        tvProcess = v.findViewById(R.id.tvProgress);
        pbStrengthBar = v.findViewById(R.id.pbStrength);
        pbSnrBar = v.findViewById(R.id.pbSNR);
        tvStrengthValue = v.findViewById(R.id.tvStrength);
        tvSnrValue = v.findViewById(R.id.tvSNR);

        btnSearch = (Button) v.findViewById(R.id.btnSearch);
        set_OnClick_listenner(btnSearch);

        btnCancel = (Button) v.findViewById(R.id.btnCancel);
        set_OnClick_listenner(btnCancel);


        mStrength = 0;
        mSNR = 0;
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case TVMessage.TYPE_TUNER_LOCK_STATUS:{
                        boolean isAdv = (msg.arg1 == 1);
                        int tunerId = msg.arg2;
                        boolean isLock = PrimeUtils.tuner_get_lock(tunerId);
                        LogUtils.d( "isAdv = "+isAdv+" isLock = "+isLock);
                        if(isAdv){
                            update_adv_ui();
                        }else {
                            if (isLock == true) {
                                mStrength = PrimeUtils.tuner_get_strength(tunerId);
                                pbStrengthBar.setProgress(mStrength);
                                tvStrengthValue.setText(Integer.toString(mStrength));

                                mSNR = PrimeUtils.tuner_get_snr(tunerId);
                                pbSnrBar.setProgress(mSNR);
                                tvSnrValue.setText(Integer.toString(mSNR));
                                LogUtils.d("TYPE_TUNER_LOCK_STATUS Lock => mStrength :" + mStrength + " dBuV mSNR :" + mSNR + " dB");
                            } else {
                                LogUtils.d("TYPE_TUNER_LOCK_STATUS UnLock => mStrength :" + mStrength + " dBuV mSNR :" + mSNR + " dB");
                                reset_tuner_status();
                            }
                        }
                        update_tuner_status(isAdv, tunerId);
                    }break;
                }

            }
        };
        reset_tuner_status();
    }

    private void initPopWindow() {
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.homeplus_signal_detection, null);

        btnAdvance = (Button) popupView.findViewById(R.id.btnAdvance);
        btnAdvance.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                showPopWindowAdv();
            }
        });
        init_view(popupView);
    }

    private void showPopWindow() {
        popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
    }

    private EditText etAdvFreq0, etAdvFreq1, etAdvFreq2, etAdvFreq3, etAdvSR;
    private Button btnAdvSignal, btnAdvCancel, btnAdvQam;
    private TextView tvAdvQamValue, tvAdvStrengthValue0, tvAdvSnrValue0, tvAdvStrengthValue1,
                    tvAdvSnrValue1, tvAdvStrengthValue2, tvAdvSnrValue2, tvAdvStrengthValue3,
                    tvAdvSnrValue3, tvAdvBerValue0, tvAdvBerValue1, tvAdvBerValue2, tvAdvBerValue3;

    private boolean moveFocusNext_adv(View currentView, int keyCode) {
        if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
            if(currentView == etAdvFreq0) {
                etAdvFreq1.requestFocus();
                return true;
            }
            if(currentView == etAdvFreq1) {
                etAdvFreq2.requestFocus();
                return true;
            }
            if(currentView == etAdvFreq2) {
                etAdvFreq3.requestFocus();
                return true;
            }
            if(currentView == btnAdvQam){
                mQam++;
                if(mQam > TpInfo.Cable.QAM_256)
                    mQam = TpInfo.Cable.QAM_16;
                tvAdvQamValue.setText(Integer.toString(qam_value[mQam]));
                return true;
            }
        }
        if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
            if(currentView == etAdvFreq3) {
                etAdvFreq2.requestFocus();
                return true;
            }
            if(currentView == etAdvFreq2) {
                etAdvFreq1.requestFocus();
                return true;
            }
            if(currentView == etAdvFreq1) {
                etAdvFreq0.requestFocus();
                return true;
            }
            if(currentView == btnAdvQam){
                mQam--;
                if(mQam < TpInfo.Cable.QAM_16)
                    mQam = TpInfo.Cable.QAM_256;
                tvAdvQamValue.setText(Integer.toString(qam_value[mQam]));
                return true;
            }
        }
        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
            if(currentView == etAdvFreq3 || currentView == etAdvFreq2 || currentView == etAdvFreq1 || currentView == etAdvFreq0) {
                btnAdvSignal.requestFocus();
                return true;
            }
            if(currentView == etAdvSR){
                btnAdvQam.requestFocus();
                return true;
            }
        }
        if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
            if(currentView == btnAdvSignal){
                etAdvFreq0.requestFocus();
                return true;
            }
            if(currentView == btnAdvCancel){
                etAdvFreq3.requestFocus();
                return true;
            }
            if(currentView == etAdvSR){
                btnAdvSignal.requestFocus();
                return true;
            }
        }

        return false;
    }

    private void set_key_listener_adv(View v){
        v.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    return moveFocusNext_adv(v, keyCode);
                }
                return false;
            }
        });
    }

    private void set_OnClick_listener_adv(View v){
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v == btnAdvSignal){
                    //tune_lock(0);
                    int freq0 = Integer.valueOf(etAdvFreq0.getText().toString());
                    int freq1 = Integer.valueOf(etAdvFreq1.getText().toString());
                    int freq2 = Integer.valueOf(etAdvFreq2.getText().toString());
                    int freq3 = Integer.valueOf(etAdvFreq3.getText().toString());

                    int symbol = Integer.valueOf(etAdvSR.getText().toString());

                    PrimeUtils.tune_lock(0, freq0, symbol, mQam);
                    PrimeUtils.tune_lock(1, freq1, symbol, mQam);
                    PrimeUtils.tune_lock(2, freq2, symbol, mQam);
                    PrimeUtils.tune_lock(3, freq3, symbol, mQam);

                    update_tuner_status(true, 1);
                }else if(v == btnAdvCancel){
                    popupWindow.dismiss();
                }
            }
        });
    }

    private void init_view_adv(View v){
        //LogUtils.d(" ");
        etAdvFreq0 = v.findViewById(R.id.etFrequency0);
        etAdvFreq1 = v.findViewById(R.id.etFrequency1);
        etAdvFreq2 = v.findViewById(R.id.etFrequency2);
        etAdvFreq3 = v.findViewById(R.id.etFrequency3);
        set_key_listener_adv(etAdvFreq0);
        set_key_listener_adv(etAdvFreq1);
        set_key_listener_adv(etAdvFreq2);
        set_key_listener_adv(etAdvFreq3);

        btnAdvSignal = v.findViewById(R.id.btnSearch);
        set_OnClick_listener_adv(btnAdvSignal);
        btnAdvCancel = v.findViewById(R.id.btnCancel);
        set_OnClick_listener_adv(btnAdvCancel);

        etAdvSR = v.findViewById(R.id.etSR);
        set_key_listener_adv(etAdvSR);
        btnAdvQam = v.findViewById(R.id.btnQAM);
        set_key_listener_adv(btnAdvQam);
        tvAdvQamValue = v.findViewById(R.id.tvQamValue);

        tvAdvStrengthValue0 = v.findViewById(R.id.tvStrength0);
        tvAdvStrengthValue0.setText("0");
        tvAdvStrengthValue1 = v.findViewById(R.id.tvStrength1);
        tvAdvStrengthValue1.setText("0");
        tvAdvStrengthValue2 = v.findViewById(R.id.tvStrength2);
        tvAdvStrengthValue2.setText("0");
        tvAdvStrengthValue3 = v.findViewById(R.id.tvStrength3);
        tvAdvStrengthValue3.setText("0");

        tvAdvSnrValue0 = v.findViewById(R.id.tvSNR0);
        tvAdvSnrValue0.setText("0");
        tvAdvSnrValue1 = v.findViewById(R.id.tvSNR1);
        tvAdvSnrValue1.setText("0");
        tvAdvSnrValue2 = v.findViewById(R.id.tvSNR2);
        tvAdvSnrValue2.setText("0");
        tvAdvSnrValue3 = v.findViewById(R.id.tvSNR3);
        tvAdvSnrValue3.setText("0");

        tvAdvBerValue0 = v.findViewById(R.id.tvBer0);
        tvAdvBerValue0.setText("0");
        tvAdvBerValue1 = v.findViewById(R.id.tvBer1);
        tvAdvBerValue1.setText("0");
        tvAdvBerValue2 = v.findViewById(R.id.tvBer2);
        tvAdvBerValue2.setText("0");
        tvAdvBerValue3 = v.findViewById(R.id.tvBer3);
        tvAdvBerValue3.setText("0");
    }

    private void update_adv_ui(){
        if(PrimeUtils.tuner_get_lock(0)){
            tvAdvStrengthValue0.setText(Integer.toString(PrimeUtils.tuner_get_strength(0)));
            tvAdvBerValue0.setText(Double.toString(PrimeUtils.tuner_get_ber(0)));
            tvAdvSnrValue0.setText(Integer.toString(PrimeUtils.tuner_get_snr(0)));
        }else{
            tvAdvStrengthValue0.setText("0");
            tvAdvBerValue0.setText("0");
            tvAdvSnrValue0.setText("0");
        }
        if(PrimeUtils.tuner_get_lock(1)){
            tvAdvStrengthValue1.setText(Integer.toString(PrimeUtils.tuner_get_strength(1)));
            tvAdvBerValue1.setText(Double.toString(PrimeUtils.tuner_get_ber(1)));
            tvAdvSnrValue1.setText(Integer.toString(PrimeUtils.tuner_get_snr(1)));
        }else{
            tvAdvStrengthValue1.setText("0");
            tvAdvBerValue1.setText("0");
            tvAdvSnrValue1.setText("0");
        }
        if(PrimeUtils.tuner_get_lock(2)){
            tvAdvStrengthValue2.setText(Integer.toString(PrimeUtils.tuner_get_strength(2)));
            tvAdvBerValue2.setText(Double.toString(PrimeUtils.tuner_get_ber(2)));
            tvAdvSnrValue2.setText(Integer.toString(PrimeUtils.tuner_get_snr(2)));
        }else{
            tvAdvStrengthValue2.setText("0");
            tvAdvBerValue2.setText("0");
            tvAdvSnrValue2.setText("0");
        }
        if(PrimeUtils.tuner_get_lock(3)){
            tvAdvStrengthValue3.setText(Integer.toString(PrimeUtils.tuner_get_strength(3)));
            tvAdvBerValue3.setText(Double.toString(PrimeUtils.tuner_get_ber(3)));
            tvAdvSnrValue3.setText(Integer.toString(PrimeUtils.tuner_get_snr(3)));
        }else{
            tvAdvStrengthValue3.setText("0");
            tvAdvBerValue3.setText("0");
            tvAdvSnrValue3.setText("0");
        }
    }

    private void initPopWindowAdv() {
        popupViewAdv = LayoutInflater.from(getContext()).inflate(R.layout.homeplus_advanced_signal_detection, null);
        init_view_adv(popupViewAdv);
    }

    private void showPopWindowAdv() {
        popupWindow = new PopupWindow(popupViewAdv, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
        mHandler.removeCallbacksAndMessages(null);
    }

    void update_tuner_status(Boolean adv, int TunerId){
        if(popupWindow.isShowing()) {
            Message msg = new Message();
            msg.what = TVMessage.TYPE_TUNER_LOCK_STATUS;
            msg.arg1 = (adv)? 1:0;
            msg.arg2 = TunerId;

            mHandler.removeCallbacksAndMessages(null);
            mHandler.sendMessageDelayed(msg, 2000);
        }
    }

    private void reset_tuner_status(){
        mStrength = 0;//dBuV
        pbStrengthBar.setProgress(mStrength);
        tvStrengthValue.setText(Integer.toString(mStrength));

        mSNR = 0;//dB
        pbSnrBar.setProgress(mSNR);
        tvSnrValue.setText(Integer.toString(mSNR));
    }
}
