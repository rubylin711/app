package com.mtest.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mtest.config.MtestConfig;
import com.mtest.module.PesiSharedPreference;
import com.mtest.module.SmartCardModule;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;

import java.util.Locale;

// out dated, only detect atr
public class SmartcardActivity extends DTVActivity {
    private static final String TAG = "SmartcardActivity";
    private Handler smart_card_handler;
    private Runnable smart_card_runnable;
    private TextView mTvSmartCard;
    private TextView mTvFailCount;

    // smart card module
    private SmartCardModule smartCardModule;
    private static final int STATUS_ATR_OK = 0;

    private int mFailCount;

    private boolean mAtrFlag = false;
    private Global_Variables mGlobalVars;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smartcard);

        mTvSmartCard = (TextView) findViewById(R.id.tv_item13_smartcard_1);
        mTvFailCount = (TextView) findViewById(R.id.tv_fail_count);

        mGlobalVars = (Global_Variables) getApplicationContext();
    }

    @Override
    protected void onStart() {
       // Log.d(TAG, "onStart");
        super.onStart();
        setup_smart_card_thread();
    }

    @Override
    protected void onStop(){
       // Log.d(TAG, "onStop");
        super.onStop();
        smart_card_handler.removeCallbacks(smart_card_runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        smart_card_handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(mAtrFlag)
                {
                    pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_smartcard), MtestConfig.TEST_RESULT_PASS);
                }
                else
                {
                    pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_smartcard), MtestConfig.TEST_RESULT_FAIL);
                }

                pesiSharedPreference.save();
                break;
            case KeyEvent.KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
                if (mGlobalVars.isStableTestEnabled()) {
                    Toast.makeText(this, "Stable Test Start!", Toast.LENGTH_SHORT).show();
                    mFailCount = 0;
                    mTvFailCount.setVisibility(View.VISIBLE);

                    // use normal runnable to run stable test, do nothing here
                }
                break;
            default:
                break;

        }

        return super.onKeyDown(keyCode, event);
    }

    private void setup_smart_card_thread()
    {
        Log.d(TAG, "setup_smart_card_thread");
        smartCardModule = new SmartCardModule(this);
        smart_card_handler = new Handler();
        smart_card_runnable = new Runnable() {
            public void run() {
                int result;
                if(mAtrFlag)
                    result = smartCardModule.checkSmartCard(1);//MtestGetATRStatus(1);
                else
                    result = smartCardModule.checkSmartCard(0);//MtestGetATRStatus(0);

                Log.d(TAG,"MtestGetATRStatus = " + result);

                if (result == MtestConfig.TEST_RESULT_WAIT_CARD_OUT)
                {
                    mTvSmartCard.setText(R.string.str_pass);
                    mTvSmartCard.setBackgroundResource(R.drawable.shape_rectangle_pass);
                    mAtrFlag = true;
                }
                else
                {
                    mTvSmartCard.setText(R.string.str_fail);
                    mTvSmartCard.setBackgroundResource(R.drawable.shape_rectangle_fail);
                    mAtrFlag = false;
                    mFailCount++;   // for stable test
                }

                mTvFailCount.setText(String.format(Locale.getDefault(), "Fail : %d", mFailCount));  // for stable test
                smart_card_handler.postDelayed(smart_card_runnable, 5000);
            }
        };
        smart_card_handler.post(smart_card_runnable);
    }
}
