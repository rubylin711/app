package com.prime.dtv.service.Test;

import android.util.Log;

import com.prime.dtv.Interface.PesiDtvFrameworkInterface;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.utils.TVTunerParams;

import java.util.Timer;
import java.util.TimerTask;

public class FrontendCmdTest {
    private static final String TAG = "FrontendCmdTest";
    private final PesiDtvFrameworkInterface mPesiDtvFrameworkInterface;
    private final Timer mTimer;

    public FrontendCmdTest(PesiDtvFrameworkInterface pesiDtvFrameworkInterface) {
        mPesiDtvFrameworkInterface = pesiDtvFrameworkInterface;

        mTimer = new Timer();
    }

    public void startTest() {
        TVTunerParams tunerParams = TVTunerParams.CreateTunerParamDVBC(0, 0, 0, 482000, 5200, TpInfo.Cable.QAM_256);
        Log.d(TAG, "startTest: tunerLock = " + mPesiDtvFrameworkInterface.tunerLock(tunerParams));
        Log.d(TAG, "startTest: getTunerNum = " + mPesiDtvFrameworkInterface.getTunerNum());
        Log.d(TAG, "startTest: getTunerType = " + mPesiDtvFrameworkInterface.getTunerType());

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "run: \n");
                Log.d(TAG, "run: getSignalStrength = " + mPesiDtvFrameworkInterface.getSignalStrength(0));
                Log.d(TAG, "run: getSignalQuality = " + mPesiDtvFrameworkInterface.getSignalQuality(0));
                Log.d(TAG, "run: getSignalSNR = " + mPesiDtvFrameworkInterface.getSignalSNR(0));
                Log.d(TAG, "run: getSignalBER = " + mPesiDtvFrameworkInterface.getSignalBER(0));
                Log.d(TAG, "run: getTunerStatus = " + mPesiDtvFrameworkInterface.getTunerStatus(0));
            }
        }, 0, 3000);
    }

    public void stopTest() {
        mTimer.cancel();
    }
}
