package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.frontend.OnTuneEventListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.prime.dtv.sysdata.DTVMessage;
import com.prime.dtv.Interface.BaseManager;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.Tuner.TunerInterface;
import com.prime.dtv.utils.TVTunerParams;

import java.util.concurrent.Executor;

public class FrontendCmdManager extends BaseManager {
    private static final String TAG = "FrontendCmdManager" ;
    private static final int MSG_TUNE = 1;

    private final TunerInterface mTunerInterface;

    public FrontendCmdManager(Context context, Handler handler) {
        super(context, TAG, handler, FrontendCmdManager.class);

        mTunerInterface = TunerInterface.getInstance(context);

        // set tune event listener to monitor lock status
/*        for(int i=0 ; i<TunerInterface.NUMBER_OF_TUNER ; i++){
            Tuner tuner = mTunerInterface.getTuner(i);
            if (tuner != null) {
                tuner.clearOnTuneEventListener();
                int tunerId = i;
                tuner.setOnTuneEventListener(getExecutor(), new OnTuneEventListener() {
                    @Override
                    public void onTuneEvent(int tuneEvent) {
                        switch (tuneEvent) {
                            case OnTuneEventListener.SIGNAL_LOCKED:
                                // arg1 = 1, lock
                                // arg2 = i, tunerId
                                sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_SIGNAL_STAUTS, 1, tunerId, null);
                                break;
                            case OnTuneEventListener.SIGNAL_NO_SIGNAL:
                            case OnTuneEventListener.SIGNAL_LOST_LOCK:
                                // arg1 = 0, unlock
                                // arg2 = i, tunerId
                                sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_SIGNAL_STAUTS, 0, tunerId, null);
                                break;
                            default:
                        }
                    }
                });
            }

        }*/
//        Tuner tuner = mTunerInterface.getTuner(0);
//        Log.d(TAG, "NEED TO CHECK TUNERID !!!!!!!!!!!!!!!!!!!!");
//        if (tuner != null) {
//            tuner.clearOnTuneEventListener();
//        tuner.setOnTuneEventListener(getExecutor(), getOnTuneEventListener());
//        }
    }

    /*
     tuner hw
      */

    // return 0 if send cmd success, -1 otherwise
    public int tunerLock(TVTunerParams tvTunerParams) {
        if (tvTunerParams == null) {
            Log.d(TAG, "tunerLock: null TVTunerParams");
            return -1;
        }

        Message msg = Message.obtain();
        msg.what = MSG_TUNE;
        msg.obj = tvTunerParams;
        DoCommand(msg);

//        return mTunerInterface.tune(tvTunerParams.getTunerId(), tvTunerParams) ? 0 : -1;
        return 0;
    }

    public int getTunerNum() {
        // TODO: find a way to get, temporally return fixed value
        return TunerInterface.NUMBER_OF_TUNER;
    }

    public int getTunerType() {
        return DataManager.TUNR_TYPE;
    }

    public int getSignalStrength(int nTunerID) {
        return mTunerInterface.getStrength(nTunerID);
    }

    public int getSignalQuality(int nTunerID) {
        return mTunerInterface.getQuality(nTunerID);
    }

    public int getSignalSNR(int nTunerID) {
        return mTunerInterface.getSnr(nTunerID);
    }

    public int getSignalBER(int nTunerID) {
        return mTunerInterface.getBer(nTunerID);
    }

    // seems to be tuner lock status
    // return 1 if lock, 0 otherwise
    public boolean getTunerStatus(int tuner_id) {
        //Log.d(TAG, "getTunerStatus tuner_id = "+tuner_id);
        if(mTunerInterface == null) {
            Log.d(TAG, "mTunerInterface: not init");
            return false;
        }
        return mTunerInterface.isLock(tuner_id);
    }

    // not implemented
    public int setFakeTuner(int openFlag) {
        Log.d(TAG, "setFakeTuner: not implemented");
        return 0;
    }

    // not implemented
    public int tunerSetAntenna5V(int tuner_id, int onOff) {
        Log.d(TAG, "tunerSetAntenna5V: not implemented");
        return 0;
    }

    // not implemented
    public int setDiSEqC10PortInfo(int nTuerID, int nPort, int n22KSwitch, int nPolarity) {
        Log.d(TAG, "setDiSEqC10PortInfo: not implemented");
        return 0;
    }

    // not implemented
    public int setDiSEqC12MoveMotor(int nTunerId, int direct, int step) {
        Log.d(TAG, "setDiSEqC12MoveMotor: not implemented");
        return 0;
    }

    // not implemented
    public int setDiSEqC12MoveMotorStop(int nTunerId) {
        Log.d(TAG, "setDiSEqC12MoveMotorStop: not implemented");
        return 0;
    }

    // not implemented
    public int resetDiSEqC12Position(int nTunerId) {
        Log.d(TAG, "resetDiSEqC12Position: not implemented");
        return 0;
    }

    // not implemented
    public int setDiSEqCLimitPos(int nTunerId, int limitType) {
        Log.d(TAG, "setDiSEqCLimitPos: not implemented");
        return 0;
    }

    @Override
    public void BaseHandleMessage(Message msg) {
        switch(msg.what) {
            case MSG_TUNE:
                Log.d(TAG, "BaseHandleMessage: msg tune");
                TVTunerParams tvTunerParams = (TVTunerParams) msg.obj;
                mTunerInterface.tune(tvTunerParams.getTunerId(), tvTunerParams);
                break;
            default:
                Log.d(TAG,"BaseHandleMessage: unknown msg what = " + msg.what);

        }
    }

    public void init_tuner(){
        mTunerInterface.init_tuner();
//        for(int i=0 ; i<TunerInterface.NUMBER_OF_TUNER ; i++){
//            Tuner tuner = mTunerInterface.getTuner(i);
//            if (tuner != null) {
//                tuner.clearOnTuneEventListener();
//                int tunerId = i;
//                tuner.setOnTuneEventListener(getExecutor(), new OnTuneEventListener() {
//                    @Override
//                    public void onTuneEvent(int tuneEvent) {
//                        switch (tuneEvent) {
//                            case OnTuneEventListener.SIGNAL_LOCKED:
//                                // arg1 = 1, lock
//                                // arg2 = i, tunerId
//                                sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_SIGNAL_STAUTS, 1, tunerId, null);
//                                mTunerInterface.SetTunerLock(tunerId, true);
//                                break;
//                            case OnTuneEventListener.SIGNAL_NO_SIGNAL:
//                            case OnTuneEventListener.SIGNAL_LOST_LOCK:
//                                // arg1 = 0, unlock
//                                // arg2 = i, tunerId
//                                sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_SIGNAL_STAUTS, 0, tunerId, null);
//                                mTunerInterface.SetTunerLock(tunerId, false);
//                                break;
//                            default:
//                        }
//                    }
//                });
//            }
//
//        }
    }

    private Executor getExecutor() {
        return Runnable::run;
    }
}
