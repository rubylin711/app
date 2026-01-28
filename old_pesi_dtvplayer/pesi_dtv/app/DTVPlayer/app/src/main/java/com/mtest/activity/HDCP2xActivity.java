package com.mtest.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.mtest.config.MtestConfig;
import com.mtest.module.HdcpModule;
import com.mtest.module.PesiSharedPreference;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

public class HDCP2xActivity extends DTVActivity {
    private static final String TAG = "HDCP2xActivity";
    private TextView mHDCPStatus;
    private Handler mHDCPHandler;
    private Runnable mHDCPRunnable;
    boolean mHDCPSuccess = false;
    private HdcpModule mHdcpModule;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hdcp2x);
        mHdcpModule = new HdcpModule(this);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        startHDCPRunnable();
    }

    @Override
    protected void onStop(){
        Log.d(TAG, "onStop");
        super.onStop();
        mHDCPHandler.removeCallbacks(mHDCPRunnable);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);
        if (mHDCPSuccess) {
            pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_hdcp2x), MtestConfig.TEST_RESULT_PASS);
        } else {
            pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_hdcp2x), MtestConfig.TEST_RESULT_FAIL);
        }

        pesiSharedPreference.save();
    }

    private void startHDCPRunnable(){
        mHDCPStatus = (TextView) findViewById(R.id.status);
        mHDCPHandler = new Handler();
        mHDCPRunnable = new Runnable() {
            public void run() {
                int ret = mHdcpModule.checkHdcp2x();
                Log.d(TAG, "run: checkHdcp2x ret = " + ret);
                if (ret == MtestConfig.TEST_RESULT_PASS) {
                    mHDCPStatus.setText(R.string.str_pass);
                    mHDCPStatus.setBackgroundResource(R.drawable.shape_rectangle_pass);
                    mHDCPSuccess = true;
                } else {
                    mHDCPStatus.setText(R.string.str_fail);
                    mHDCPStatus.setBackgroundResource(R.drawable.shape_rectangle_fail);
                    mHDCPSuccess = false;
                }

                mHDCPHandler.postDelayed(mHDCPRunnable, 2000);
            }
        };

        mHDCPHandler.post(mHDCPRunnable);
    }
}
