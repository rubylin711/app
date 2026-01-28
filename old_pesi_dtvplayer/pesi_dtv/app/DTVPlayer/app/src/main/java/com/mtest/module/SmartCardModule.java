package com.mtest.module;

import android.content.Context;
import android.util.Log;

import com.dolphin.dtv.HiDtvMediaPlayer;
import com.mtest.config.MtestConfig;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

import java.lang.ref.WeakReference;

public class SmartCardModule
{
    private static final String TAG = SmartCardModule.class.getSimpleName();
    private static final int CMD_SUCCESS = HiDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS;
    private static final int CMD_FAIL = HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    private static final int RESULT_PASS = MtestConfig.TEST_RESULT_PASS;
    private static final int RESULT_FAIL = MtestConfig.TEST_RESULT_FAIL;
    private final WeakReference<Context> mContRef;

    // smart card
    static final int STATUS_ATR_OK = 0;
    static final int STATUS_ATR_OK_CARD_OUT = 1;

    public SmartCardModule(Context context)
    {
        mContRef = new WeakReference<>(context);
    }

    public int getATRStatus(int status)
    {
        Log.d(TAG, "getATRStatus: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.MtestGetATRStatus(status);
    }

    public int checkSmartCard(int status)
    {
        Log.d(TAG, "checkSmartCard: ");
        int ret = 0;

        ret = getATRStatus(status);
        if (ret == STATUS_ATR_OK)
            return MtestConfig.TEST_RESULT_WAIT_CARD_OUT;
        else if (ret == STATUS_ATR_OK_CARD_OUT)
            return MtestConfig.TEST_RESULT_PASS;
        else
            return MtestConfig.TEST_RESULT_FAIL;
    }
}
