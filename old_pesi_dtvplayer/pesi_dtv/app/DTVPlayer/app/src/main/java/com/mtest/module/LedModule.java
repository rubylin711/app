package com.mtest.module;

import android.content.Context;
import android.util.Log;

import com.dolphin.dtv.HiDtvMediaPlayer;
import com.mtest.config.MtestConfig;
import com.prime.dtvplayer.Activity.DTVActivity;

import java.lang.ref.WeakReference;

public class LedModule
{
    private static final String TAG = LedModule.class.getSimpleName();
    private static final int CMD_SUCCESS = HiDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS;
    private static final int CMD_FAIL = HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    private static final int RESULT_PASS = MtestConfig.TEST_RESULT_PASS;
    private static final int RESULT_FAIL = MtestConfig.TEST_RESULT_FAIL;
    private final WeakReference<Context> mContRef;

    // vary by model
    // should sync with pesi service
    public static final int STATUS_ALL_OFF = 0;
    public static final int STATUS_GREEN = 1;
    public static final int STATUS_RED = 2;
    public static final int STATUS_ALL_ON = 3;

    public LedModule(Context context)
    {
        mContRef = new WeakReference<>(context);
    }

    private static int getReturn(int ret)
    {
        return (ret == CMD_SUCCESS) ?
                RESULT_PASS :
                RESULT_FAIL ;
    }

    private static int getReturn(boolean success)
    {
        return success ?
                RESULT_PASS :
                RESULT_FAIL ;
    }

    // use setOnOff(int status) instead
    public int setWhite(int enable)
    {
        Log.d(TAG, "setRed: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.MtestSetLedWhite(enable);
    }

    // use setOnOff(int status) instead
    public int setRed(int enable)
    {
        Log.d(TAG, "setRed: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.MtestSetLedRed(enable);
    }

    // use setOnOff(int status) instead
    public int setGreen(int enable)
    {
        Log.d(TAG, "setGreen: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.MtestSetLedGreen(enable);
    }

    // use setOnOff(int status) instead
    public int setOrange(int enable)
    {
        Log.d(TAG, "setOrange: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.MtestSetLedOrange(enable);
    }

    public int setOnOff(int status)
    {
        Log.d(TAG, "setOnOff: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.MtestSetLedOnOff(status);
    }
}
