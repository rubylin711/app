package com.mtest.module;

import android.content.Context;
import android.util.Log;

import com.mtest.config.MtestConfig;
import com.prime.dtvplayer.Activity.DTVActivity;

public class HdcpModule {
    private static final String TAG = "HdcpModule";
    private static final int HDCP_STATUS_1X_PASS = 1;
    private static final int HDCP_STATUS_2X_PASS = 2;

    //private Context mContext;
    private DTVActivity mDtv;

    public HdcpModule(Context context) {
        //mContext = context;
        mDtv = (DTVActivity) context;
    }

    // must return one of the TEST_RESULT_XXXX in MtestConfig
    public int checkHdcp1x() {
        int ret = mDtv.MtestGetHDCPStatus();
        Log.d(TAG, "checkHdcp1x: ret = " + ret);
        if (mDtv.MtestGetHDCPStatus() == HDCP_STATUS_1X_PASS) {
            Log.d(TAG, "checkHdcp1x: pass");
            return MtestConfig.TEST_RESULT_PASS;
        } else {
            Log.d(TAG, "checkHdcp1x: fail");
            return MtestConfig.TEST_RESULT_FAIL;
        }
    }

    // must return one of the TEST_RESULT_XXXX in MtestConfig
    public int checkHdcp2x() {
        int ret = mDtv.MtestGetHDCPStatus();
        Log.d(TAG, "checkHdcp2x: ret = " + ret);
        if (ret == HDCP_STATUS_2X_PASS) {
            Log.d(TAG, "checkHdcp2x: pass");
            return MtestConfig.TEST_RESULT_PASS;
        } else {
            Log.d(TAG, "checkHdcp2x: fail");
            return MtestConfig.TEST_RESULT_FAIL;
        }
    }
}
