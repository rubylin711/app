package com.mtest.module;

import android.content.Context;
import android.util.Log;

import com.dolphin.dtv.HiDtvMediaPlayer;
import com.mtest.config.MtestConfig;
import com.prime.dtvplayer.Activity.DTVActivity;

import java.lang.ref.WeakReference;

public class OtaModule
{
    private static final String TAG = OtaModule.class.getSimpleName();
    private static final int CMD_SUCCESS = HiDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS;
    private static final int CMD_FAIL = HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    private static final int RESULT_PASS = MtestConfig.TEST_RESULT_PASS;
    private static final int RESULT_FAIL = MtestConfig.TEST_RESULT_FAIL;
    private final WeakReference<Context> mContRef;

    public static final String KEY_ENABLE_OPT = "enable.opt";

    public OtaModule(Context context)
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

    public int enableOpt(boolean enable)
    {
        DTVActivity activity = (DTVActivity) mContRef.get();
        int ret = activity.MtestEnableOpt(enable);
        return getReturn(ret);
    }

    public int triggerOTASoftWare()
    {
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.UpdateMtestOTASoftWare();
    }
}
