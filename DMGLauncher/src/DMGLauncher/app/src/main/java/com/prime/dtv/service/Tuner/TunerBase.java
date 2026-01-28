package com.prime.dtv.service.Tuner;

import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.frontend.FrontendStatus;
import android.media.tv.tuner.frontend.OnTuneEventListener;
import android.util.Log;

import com.prime.dtv.sysdata.DTVMessage;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.utils.LogUtils;
import com.prime.dtv.utils.TVTunerParams;

import java.util.concurrent.Executor;

public abstract class TunerBase {
    private static final String TAG = "TunerBase";

    private int _Id;
    private static final long TIMEOUT_LOCK = 2000; // ms
    private Tuner mTuner;
    private TpInfo mTpInfo;
    private boolean isTunerLock = false;
    private boolean isTune = false;
    private Thread mMonitorLockThread;

    private boolean mCancelTuning = false;
    public TunerBase() {
        mTuner = null;
        mTpInfo = null;
        mMonitorLockThread = null;
    }
    public TunerBase(Tuner tuner) {
        mTpInfo = null;
        update(tuner);
    }

    public void update(Tuner tuner){
        LogUtils.d("mTuner = "+mTuner+" new tuner = "+ tuner);
        if(mTuner != null){
            mTuner.clearOnTuneEventListener();
            mTuner.close();
        }
        mTuner = tuner;
        isTune = false;
        isTunerLock = false;
        LogUtils.i("Tuner ["+_Id+"]  set mTpInfo to null");
        mTpInfo = null;
        tuner.setOnTuneEventListener(getExecutor(), new OnTuneEventListener() {
            @Override
            public void onTuneEvent(int tuneEvent) {
                if(!isTune){
                    isTune = true;
                    mMonitorLockThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(isTune){
                                try {
                                    isTunerLock = getLock();
                                    //LogUtils.d("isTunerLock = "+isTunerLock);
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }, "MonitorLockThread");

                    mMonitorLockThread.start();
                }
                switch (tuneEvent) {
                    case OnTuneEventListener.SIGNAL_LOCKED:
                        // arg1 = 1, lock
                        // arg2 = i, tunerId
                        //LogUtils.e( "[DB_TUNER_LOCK] Tuner Lock");
                        isTunerLock = true;
                        break;
                    case OnTuneEventListener.SIGNAL_NO_SIGNAL:
                    case OnTuneEventListener.SIGNAL_LOST_LOCK:
                        // arg1 = 0, unlock
                        // arg2 = i, tunerId
                        isTunerLock = false;
                        //LogUtils.e( "[DB_TUNER_LOCK] Tuner UNLock");
                        break;
                    default:
                }
            }
        });
        mTuner.setResourceLostListener(getExecutor(), new Tuner.OnResourceLostListener() {
            @Override
            public void onResourceLost(Tuner tuner) {
                LogUtils.d("Tuner onResourceLost tuner = "+tuner);
                mTpInfo = null;
                LogUtils.i("Tuner ["+_Id+"]  set mTpInfo to null");
                mTuner = null;
                isTune = false;
                if(mMonitorLockThread != null){
                    try {
                        mMonitorLockThread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    mMonitorLockThread = null;
                }
            }
        });

    }
    private Executor getExecutor(){
        return Runnable::run;
    }
    protected void setTpInfo(TpInfo tpInfo) {
        mTpInfo = tpInfo;
        LogUtils.i("Tuner ["+_Id+"]  set mTpInfo to null");
    }

    protected TpInfo getTpInfo() {
        return mTpInfo;
    }

    protected boolean IsCancelTuning(){
        return mCancelTuning;
    }
    protected void setCancelTuning(boolean value){
        mCancelTuning = value;
    }
    protected Tuner getTuner() {
        return mTuner;
    }

    public boolean cancelTune() {
        int result = Tuner.RESULT_UNKNOWN_ERROR;

        if (mTuner != null) {
            //isTunerLock = false;
            setCancelTuning(true);
            result = mTuner.cancelTuning();
            mTpInfo = null;
            LogUtils.i("Tuner ["+_Id+"]  set mTpInfo to null");
        }

        return result == Tuner.RESULT_SUCCESS;
    }

    public void setTunerLock(boolean islock){
        isTunerLock = islock;
    }

    public void setIsTune(boolean tune){
        isTune = tune;
    }

    public boolean getLock(){
        boolean islock = false;
        FrontendStatus status = null;
        if(mTuner != null/* && checkTpInfo(tpInfo)*/) {
            try {
                status = mTuner.getFrontendStatus(new int[] {FrontendStatus.FRONTEND_STATUS_TYPE_DEMOD_LOCK});
                //mTuner.getFrontendStatus(new int[]{FrontendStatus.FRONTEND_STATUS_TYPE_RF_LOCK}).isRfLocked()
                islock = status != null && status.isDemodLocked();
            }
            catch (Exception e) {
                Log.w(TAG, "isLock: ", e);
            }
        }
        if(islock == false) {
            //mTpInfo = null;
            LogUtils.i("Tuner ["+_Id+"]  set mTpInfo to null");
        }
        //LogUtils.d("[DB_TUNER_LOCK] islock = "+islock);
        return islock;
    }

    public boolean isLock() {
        boolean islock = isTunerLock;
//        FrontendStatus status = null;
//        if(mTuner != null/* && checkTpInfo(tpInfo)*/) {
//            try {
//                status = mTuner.getFrontendStatus(new int[] {FrontendStatus.FRONTEND_STATUS_TYPE_DEMOD_LOCK});
//                islock = status != null && status.isDemodLocked();
//            }
//            catch (Exception e) {
//                Log.w(TAG, "isLock: ", e);
//            }
//        }
//        if(islock == false)
//            mTpInfo = null;
        return islock;
    }

    public int getStrength() {
        FrontendStatus status = null;
        if(mTuner != null/* && checkTpInfo(tpInfo)*/) {
            try {
                status = mTuner.getFrontendStatus(new int[] {FrontendStatus.FRONTEND_STATUS_TYPE_SIGNAL_STRENGTH});
            }
            catch (Exception e) {
                Log.w(TAG, "getStrength: ", e);
            }
        }

        // return 99 if null, mantis: 7258
        return status == null ? 99 : status.getSignalStrength();
    }

    public int getQuality() {
        FrontendStatus status = null;
        if(mTuner != null/* && checkTpInfo(tpInfo)*/) {
            try {
                status = mTuner.getFrontendStatus(new int[] {FrontendStatus.FRONTEND_STATUS_TYPE_SIGNAL_QUALITY});
            }
            catch (Exception e) {
                Log.w(TAG, "getQuality: ", e);
            }
        }

        // return 99 if null, mantis: 7258
        return status == null ? 99 : status.getSignalQuality();
    }

    public int getSnr() {
        FrontendStatus status = null;
        if(mTuner != null/* && checkTpInfo(tpInfo)*/) {
            try {
                status = mTuner.getFrontendStatus(new int[] {FrontendStatus.FRONTEND_STATUS_TYPE_SNR});
            }
            catch (Exception e) {
                Log.w(TAG, "getSnr: ", e);
            }
        }

        // return 0 if null
        return status == null ? 0 : status.getSnr();
    }

    public int getBer() {
        FrontendStatus status = null;
        if(mTuner != null/* && checkTpInfo(tpInfo)*/) {
            try {
                status = mTuner.getFrontendStatus(new int[] {FrontendStatus.FRONTEND_STATUS_TYPE_BER});
            }
            catch (Exception e) {
                Log.w(TAG, "getBer: ", e);
            }
        }

        // return 0 if null
        return status == null ? 0 : status.getBer();
    }
    public boolean close(){
        LogUtils.d("close tuner = " + mTuner);
        boolean value = false;
        cancelTune();
        if (mTuner != null) {
            mTuner.clearOnTuneEventListener();
            mTuner.clearResourceLostListener();
            mTuner.close();
        }

        mTuner = null;
        isTune = false;
        if(mMonitorLockThread != null){
            try {
                mMonitorLockThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            mMonitorLockThread = null;
        }
        return value;
    }
//    protected abstract boolean checkTpInfo(TpInfo tpInfo);
    protected abstract boolean tune(TpInfo tpInfo);
    protected abstract boolean tune(TVTunerParams tunerParams);

    public boolean waitLock() {
        //LogUtils.d("[DB_TUNER_LOCK] AvCmdMiddle start !! ");
        long timeoutNano = TIMEOUT_LOCK * 1000 * 1000; // nanoseconds
        long endTimeNano = System.nanoTime() + timeoutNano;
        while ((System.nanoTime() < endTimeNano) && !mCancelTuning) {
            if (isLock()) {
                //LogUtils.d("[DB_TUNER_LOCK] AvCmdMiddle get tuner lock !! ");
                return true;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LogUtils.d("AvCmdMiddle wait lock timeout ");
        return false;
    }

    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    public int get_Id() {
        return this._Id;
    }
}
