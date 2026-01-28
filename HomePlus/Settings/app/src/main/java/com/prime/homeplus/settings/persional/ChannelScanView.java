package com.prime.homeplus.settings.persional;

import android.app.ActionBar;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.constraintlayout.widget.ConstraintLayout;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.TVMessage;
import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.PrimeApplication;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;


public class ChannelScanView extends ThirdLevelView {
    private String TAG = "HomePlus-ChannelScanView";


    public ChannelScanView(int i, Context context, SettingsRecyclerView secondDepthView) {
        super(i, context, secondDepthView);
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_parental_control;
    }

    private TextView tvEnterPin, tvEnterPinHint;
    private EditText etParentalPin;
    private TextView tvPin1, tvPin2, tvPin3, tvPin4;

    private Handler mHandler;
    private int mTunerId = 0;

    public void onFocus(){
        etParentalPin.requestFocus();
    }

    @Override
    public void onViewCreated() {
        initPopWindow();

        tvEnterPin = (TextView) findViewById(R.id.tvEnterPin);
        tvEnterPinHint = (TextView) findViewById(R.id.tvEnterPinHint);

        tvPin1 = (TextView) findViewById(R.id.tvPin1);
        tvPin2 = (TextView) findViewById(R.id.tvPin2);
        tvPin3 = (TextView) findViewById(R.id.tvPin3);
        tvPin4 = (TextView) findViewById(R.id.tvPin4);

        etParentalPin = (EditText) findViewById(R.id.etParentalPin);

        etParentalPin.setInputType(InputType.TYPE_NULL);

        etParentalPin.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etParentalPin.getText().toString().equals("")) {
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Instrumentation inst = new Instrumentation();
                                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                                    } catch (Exception e) {
                                        Log.d(TAG, "" + e);
                                    }
                                }
                            }.start();
                            return true;
                        } else {
                            settingsRecyclerView.backToList();
                        }
                    }
                }
                return false;
            }
        });


        etParentalPin.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {

                } else {
                    etParentalPin.setText("");
                }
            }
        });

        etParentalPin.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {
                lightPassword(etParentalPin.getText().length());

                if (etParentalPin.getText().length() == 4) {
                    GposInfo gposInfo = PrimeApplication.get_prime_dtv_service().gpos_info_get();
                    LogUtils.d("getPasswordValue "+GposInfo.getPasswordValue(getContext()));
                    String pin = String.format("%04d", GposInfo.getPasswordValue(getContext()));
                    if (etParentalPin.getText().toString().equals(pin)) {
                        showPopWindow();
                        etParentalPin.setText("");
                    } else {
                        tvEnterPinHint.setText(getContext().getString(R.string.settings_system_pinwrong));
                        tvEnterPinHint.setTextColor(getResources().getColor(R.color.colorWarning));
                        etParentalPin.setText("");
                    }
                } else if (etParentalPin.getText().length() == 1) {
                    tvEnterPinHint.setText(getContext().getString(R.string.settings_system_pindel));
                    tvEnterPinHint.setTextColor(getResources().getColor(R.color.colorWhiteOpacity40));
                }
            }
        });

        mHandler = new Handler(Looper.getMainLooper()){

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                LogUtils.d("[Ethan] what :"+msg.what+" arg1 :"+msg.arg1);
                switch(msg.what){
                    case TVMessage.TYPE_SCAN_SCHEDULE:{
                        pbSearchBar.setProgress(msg.arg1);
                        tvProcess.setText(Integer.toString(msg.arg1));
                    }break;
                    case TVMessage.TYPE_SCAN_END:{
                        String sub_title_text = getContext().getString(R.string.current_status_1)+" "+getContext().getString(R.string.tv)+" :"+msg.arg1+", "+getContext().getString(R.string.radio)+" :"+msg.arg2;
                        LogUtils.d("TYPE_SCAN_END =>"+sub_title_text);
                        tvSubTitle.setText(sub_title_text);
                        PrimeUtils.save_scan_result(getContext());
                    }break;
                    case TVMessage.TYPE_TUNER_LOCK_STATUS:{
                        boolean isLock = PrimeUtils.tuner_get_lock(mTunerId);
                        LogUtils.d( "isLock = "+isLock);
                        if(isLock == true) {
                            int strength = PrimeUtils.tuner_get_strength(mTunerId);
                            pbStrengthBar.setProgress(strength);
                            tvStrengthValue.setText(Integer.toString(strength));

                            int snr = PrimeUtils.tuner_get_snr(mTunerId);
                            pbSnrBar.setProgress(snr);
                            tvSnrValue.setText(Integer.toString(snr));

                            Double ber = PrimeUtils.tuner_get_ber(mTunerId);
                            tvBer.setText(Double.toString(ber));
                            LogUtils.d("TYPE_TUNER_LOCK_STATUS Lock => mStrength :" + strength + " dBuV mSNR :" + snr + " dB Ber :"+ ber);

                        }else{
                            LogUtils.d("TYPE_TUNER_LOCK_STATUS UnLock ");
                            reset_tuner_status();
                        }
                        update_tuner_status();
                    }break;
                }
            }
        };
    }

    private void lightPassword(int size) {
        switch (size) {
            case 0:
                setPasswordOn(tvPin1, false);
                setPasswordOn(tvPin2, false);
                setPasswordOn(tvPin3, false);
                setPasswordOn(tvPin4, false);
                break;
            case 1:
                setPasswordOn(tvPin1, true);
                setPasswordOn(tvPin2, false);
                setPasswordOn(tvPin3, false);
                setPasswordOn(tvPin4, false);
                break;
            case 2:
                setPasswordOn(tvPin1, true);
                setPasswordOn(tvPin2, true);
                setPasswordOn(tvPin3, false);
                setPasswordOn(tvPin4, false);
                break;
            case 3:
                setPasswordOn(tvPin1, true);
                setPasswordOn(tvPin2, true);
                setPasswordOn(tvPin3, true);
                setPasswordOn(tvPin4, false);
                break;
            case 4:
                setPasswordOn(tvPin1, true);
                setPasswordOn(tvPin2, true);
                setPasswordOn(tvPin3, true);
                setPasswordOn(tvPin4, true);
                break;
        }
    }

    private void setPasswordOn(TextView tv, boolean on) {
        if (on) {
            tv.setBackground(getResources().getDrawable(R.drawable.password_on));
        } else {
            tv.setBackground(getResources().getDrawable(R.drawable.password_off));
        }
    }

    private View popupView;
    private static PopupWindow popupWindow;
    private Button btnSearch, btnCancel;
    private TextView tvBID, tvProcess, tvSubTitle, tvStrengthValue, tvSnrValue, tvBer;
    private ProgressBar pbSearchBar, pbStrengthBar, pbSnrBar;

    private void set_OnClick_listenner(View v){
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v == btnSearch){
                    PrimeUtils.start_scan(405000, 5217, TpInfo.Cable.BAND_6MHZ, TpInfo.Cable.QAM_256, mTunerId,false);
                }
                if(v == btnCancel)
                    popupWindow.dismiss();
            }
        });
    }

    private void init_view(View v){
        tvBID = v.findViewById(R.id.tvBID);
        tvBID.setText(Integer.toString(Pvcfg.getBatId()));

        btnSearch = v.findViewById(R.id.btnSearch);
        set_OnClick_listenner(btnSearch);
        btnCancel = v.findViewById(R.id.btnCancel);
        set_OnClick_listenner(btnCancel);

        tvSubTitle = v.findViewById(R.id.tvSubtitle);
        tvSubTitle.setText(getContext().getString(R.string.current_status));
        tvProcess = v.findViewById(R.id.tvProgress);
        tvProcess.setText("0");
        tvStrengthValue = v.findViewById(R.id.tvStrength);
        tvSnrValue = v.findViewById(R.id.tvSNR);
        tvBer = v.findViewById(R.id.tvBer);

        pbStrengthBar = v.findViewById(R.id.pbStrength);
        pbStrengthBar.setProgress(0);
        pbSearchBar = v.findViewById(R.id.pbProgress);
        pbSnrBar = v.findViewById(R.id.pbSNR);

        reset_tuner_status();
    }

    void update_tuner_status(){
        if(popupWindow.isShowing()) {
            Message msg = new Message();
            msg.what = TVMessage.TYPE_TUNER_LOCK_STATUS;

            mHandler.removeCallbacksAndMessages(null);
            mHandler.sendMessageDelayed(msg, 1000);
        }
    }

    private void reset_tuner_status(){
        pbStrengthBar.setProgress(0);
        tvStrengthValue.setText("0");

        pbSnrBar.setProgress(0);
        tvSnrValue.setText("0");

        tvBer.setText("0");
    }

    private void initPopWindow() {
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.homeplus_channel_scan_all, null);
        popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
    }

    private void showPopWindow() {
        TextView tvTitle = (TextView) popupView.findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.title_netscan_2);

        EditText etFrequency = (EditText) popupView.findViewById(R.id.etFrequency);
        etFrequency.setVisibility(View.GONE);

        TextView tvFrequency = (TextView) popupView.findViewById(R.id.tvFrequency);
        tvFrequency.setVisibility(View.VISIBLE);

        EditText etSR = (EditText) popupView.findViewById(R.id.etSR);
        etSR.setVisibility(View.GONE);

        TextView tvSR = (TextView) popupView.findViewById(R.id.tvSR);
        tvSR.setVisibility(View.VISIBLE);

        ConstraintLayout clQam = (ConstraintLayout) popupView.findViewById(R.id.clQam);
        clQam.setVisibility(View.GONE);

        TextView tvQamValue2 = (TextView) popupView.findViewById(R.id.tvQamValue2);
        tvQamValue2.setVisibility(View.VISIBLE);

        init_view(popupView);

        popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
        PrimeApplication.getInstance().registerHandler(mHandler);
        PrimeUtils.tune_lock(mTunerId, 405000, 5217, TpInfo.Cable.QAM_256);
        update_tuner_status();
    }
}
