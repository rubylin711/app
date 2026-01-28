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

import com.prime.datastructure.config.Pvcfg;

import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.TVMessage;
import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.PrimeApplication;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

public class ChannelScanView extends ThirdLevelView {
    private String TAG = "HOMEPLUS_SETTINGS-ChannelScanView";

    private boolean mIsSearchAll = false;
    private Handler mHandler;
    private Context mContext;
    private int mTunerId = 0, mStrength, mSNR;


    public ChannelScanView(int i, Context context, SettingsRecyclerView secondDepthView) {
        super(i, context, secondDepthView);
        mContext = context;
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_channel_scan;
    }

    private Button btn1, btn2;

    public void onFocus(){
        btn1.requestFocus();
    }

    @Override
    public void onViewCreated() {
        initPopWindow();

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);

        btn1.setText(getContext().getString(R.string.settings_channel_scan_network));

        btn1.setOnKeyListener(new OnKeyListener() {
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

        btn1.setOnFocusChangeListener(OnFocusChangeListener);

        btn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsSearchAll = true;
                showPopWindow(mIsSearchAll);
            }
        });

        btn2.setText(getContext().getString(R.string.settings_channel_scan_manual));

        btn2.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

                    }
                }
                return false;
            }
        });

        btn2.setOnFocusChangeListener(OnFocusChangeListener);

        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsSearchAll = false;
                showPopWindow(mIsSearchAll);
            }
        });
    }


    private View popupView;
    private static PopupWindow popupWindow;
    private TextView tvTitle, tvBIDTitle, tvBID, tvProcess, tvSubTitle, tvStrengthValue, tvSnrValue, tvQam;
    private EditText etFrequency, etSymbol;
    private Button btnSearch, btnCancel, btnQAM;
    private ProgressBar pbSearchBar, pbStrengthBar, pbSnrBar;
    private int mQam = TpInfo.Cable.QAM_256;
    private int qam_value[] = {16,32,64,128,256};

    private void initPopWindow() {
        LogUtils.d("[Ethan] initPopWindow IN");
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.homeplus_channel_scan_all, null);
        popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        tvTitle = (TextView) popupView.findViewById(R.id.tvTitle);
        tvBIDTitle = (TextView) popupView.findViewById(R.id.tvBIDTitle);
        tvBID = (TextView) popupView.findViewById(R.id.tvBID);
        tvBID.setText(Integer.toString(Pvcfg.getBatId()));

        mStrength = 0;
        mSNR = 0;
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
                        String sub_title_text = mContext.getString(R.string.current_status_1)+" "+mContext.getString(R.string.tv)+" :"+msg.arg1+", "+mContext.getString(R.string.radio)+" :"+msg.arg2;
                        LogUtils.d("TYPE_SCAN_END =>"+sub_title_text);
                        tvSubTitle.setText(sub_title_text);
                        PrimeUtils.save_scan_result(getContext());
                    }break;
                    case TVMessage.TYPE_TUNER_LOCK_STATUS:{
                        boolean isLock = PrimeUtils.tuner_get_lock(mTunerId);
                        LogUtils.d( "isLock = "+isLock);
                        if(isLock == true) {
                            mStrength = PrimeUtils.tuner_get_strength(mTunerId);
                            pbStrengthBar.setProgress(mStrength);
                            tvStrengthValue.setText(Integer.toString(mStrength));

                            mSNR = PrimeUtils.tuner_get_snr(mTunerId);
                            pbSnrBar.setProgress(mSNR);
                            tvSnrValue.setText(Integer.toString(mSNR));
                            LogUtils.d("TYPE_TUNER_LOCK_STATUS Lock => mStrength :" + mStrength + " dBuV mSNR :" + mSNR + " dB");
                        }else{
                            LogUtils.d("TYPE_TUNER_LOCK_STATUS UnLock => mStrength :" + mStrength + " dBuV mSNR :" + mSNR + " dB");
                            reset_tuner_status();
                        }
                        update_tuner_status();
                    }break;
                }
            }
        };

        btnSearch = (Button) popupView.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int freq = Integer.valueOf(etFrequency.getText().toString());
                int sym = Integer.valueOf(etSymbol.getText().toString());
                LogUtils.d("[Ethan] Press Scan button freq = "+freq+" sym = "+sym);
                PrimeUtils.start_scan(freq, sym, TpInfo.Cable.BAND_6MHZ, mQam, mTunerId, !mIsSearchAll);
            }
        });

        btnCancel = (Button) popupView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.d("[Ethan] Press Scan button");
                popupWindow.dismiss();
            }
        });

        etFrequency = (EditText) popupView.findViewById(R.id.etFrequency);

        etFrequency.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    // 檢查是否按下了向下鍵
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        // 將焦點移動到符碼率 EditText
                        etSymbol.requestFocus();
                        return true; // 表示事件已處理
                    }
                }
                return false;
            }
        });
        etSymbol = (EditText) popupView.findViewById(R.id.etSR);
        etSymbol.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    // 檢查是否按下了向下鍵
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        // 將焦點移動到符碼率 EditText
                        etFrequency.requestFocus();
                        return true; // 表示事件已處理
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        // 將焦點移動到符碼率 EditText
                        btnQAM.requestFocus();
                        return true; // 表示事件已處理
                    }
                }
                return false;
            }
        });
        btnQAM = popupView.findViewById(R.id.btnQAM);
        tvQam = popupView.findViewById(R.id.tvQamValue);
        btnQAM.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    // 檢查是否按下了向下鍵
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        // 將焦點移動到符碼率 EditText
                        etSymbol.requestFocus();
                        return true; // 表示事件已處理
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
                        mQam--;
                        if(mQam < TpInfo.Cable.QAM_16)
                            mQam = TpInfo.Cable.QAM_256;
                        tvQam.setText(Integer.toString(qam_value[mQam]));
                        return true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                        mQam++;
                        if(mQam >TpInfo.Cable.QAM_256)
                            mQam = TpInfo.Cable.QAM_16;
                        tvQam.setText(Integer.toString(qam_value[mQam]));
                        return true;
                    }
                }
                return false;
            }
        });
        pbSearchBar = (ProgressBar)popupView.findViewById(R.id.pbProgress);
        tvProcess = (TextView)popupView.findViewById(R.id.tvProgress);
        tvSubTitle = popupView.findViewById(R.id.tvSubtitle);
        tvSubTitle.setText(getContext().getString(R.string.current_status));
        pbStrengthBar = popupView.findViewById(R.id.pbStrength);
        pbSnrBar = popupView.findViewById(R.id.pbSNR);
        tvStrengthValue = popupView.findViewById(R.id.tvStrength);
        tvSnrValue = popupView.findViewById(R.id.tvSNR);
        reset_tuner_status();
        LogUtils.d("[Ethan] initPopWindow OUT");
    }

    private void showPopWindow(boolean isScanAll) {
        if (isScanAll) {
            tvTitle.setText(R.string.title_netscan_1);
            tvBIDTitle.setVisibility(VISIBLE);
            tvBID.setVisibility(VISIBLE);
            popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
        } else {
            tvTitle.setText(R.string.title_netscan_0);
            tvBIDTitle.setVisibility(GONE);
            tvBID.setVisibility(GONE);
            popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
        }
        tvSubTitle.setText(getContext().getString(R.string.current_status));
        PrimeApplication.getInstance().registerHandler(mHandler);
        tune_lock();
    }

    private void tune_lock(){
        int freq, symbol, modulation;
        freq = Integer.valueOf(etFrequency.getText().toString());
        symbol = Integer.valueOf(etSymbol.getText().toString());

        PrimeUtils.tune_lock(mTunerId, freq, symbol, mQam);
        update_tuner_status();
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
        mStrength = 0;//dBuV
        pbStrengthBar.setProgress(mStrength);
        tvStrengthValue.setText(Integer.toString(mStrength));

        mSNR = 0;//dB
        pbSnrBar.setProgress(mSNR);
        tvSnrValue.setText(Integer.toString(mSNR));
    }
}
