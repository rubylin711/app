package com.prime.dtv.service.Tuner;

import android.media.tv.tuner.Descrambler;
import android.media.tv.tuner.Lnb;
import android.media.tv.tuner.LnbCallback;
import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.frontend.FrontendStatus;
import android.media.tv.tuner.frontend.OnTuneEventListener;
import android.os.SystemClock;
import android.util.Log;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.DTVMessage;
import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVTunerParams;
import com.prime.dtv.module.IrdetoModule;
import com.prime.dtv.service.datamanager.DataManager;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class TunerBase {
    private static final String TAG = "TunerBase";

    private int _Id;
    private static final long TIMEOUT_LOCK = 500; // ms
    private Tuner mTuner;
    private Tuner mShareTuner;
    private Lnb mLnb;
    private TpInfo mTpInfo;
    private Descrambler mDescrambler;
    private boolean isTunerLock = false;
    private boolean isTune = false;
    private Thread mMonitorLockThread;

    private boolean mCancelTuning = false;

    /** ---- 前端鎖定事件 ---- */
    public interface FrontendLockListener {
        void onFrontendLockChanged(boolean locked, int strength, int quality, int snr, int ber);
    }
    private FrontendLockListener mLockListener;
    public void setFrontendLockListener(FrontendLockListener l) { mLockListener = l; }

    // 去抖 + 節流（避免過度回報）
    private Boolean mLastReportedLock = null;
    private long mLastReportMs = 0L;
    private static final int MIN_REPORT_INTERVAL_MS = 1000;
    private static final int UNLOCK_CONFIRM_POLLS   = 2;
    private int mUnlockStreak = 0;

    /** 在 onTuneEvent 與輪詢處都會呼叫 */
    private void maybeNotifyLockChanged(boolean locked) {
        // unlock 去抖：連續 N 次 false 才算解鎖
        if (!locked) {
            if (++mUnlockStreak < UNLOCK_CONFIRM_POLLS) {
                return;
            }
        } else {
            mUnlockStreak = 0;
        }

        long now = SystemClock.uptimeMillis();
        if (mLastReportedLock != null && mLastReportedLock == locked &&
                (now - mLastReportMs) < MIN_REPORT_INTERVAL_MS) {
            return; // 同狀態且太頻繁，略過
        }

        mLastReportedLock = locked;
        mLastReportMs = now;
        //Log.d(TAG, "maybeNotifyLockChanged: locked=" + locked   );
        if (mLockListener != null) {
            mLockListener.onFrontendLockChanged(
                    locked, getStrength(), getQuality(), getSnr(), getBer());
        }
    }

    public TunerBase() {
        mTuner = null;
        mTpInfo = null;
        mShareTuner = null;
        mMonitorLockThread = null;
    }

    public TunerBase(Tuner tuner) {
        mTpInfo = null;
        update(tuner);
    }
    
    public Tuner getShareTuner(){
        return mShareTuner;
    }

    public void setShareTuner(Tuner tuner){
        mShareTuner = tuner;
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
        if(Pvcfg.getCAType() == Pvcfg.CA_TYPE.CA_IRDETO) {
            Log.d(TAG,"tuner init set descrambler");
            mDescrambler = tuner.openDescrambler();
            byte[] keyToken = new byte[1];
            keyToken[0] = (byte) IrdetoModule.IRDETO_KEY_TOKEN;

            mDescrambler.setKeyToken(keyToken);
            mDescrambler.addPid(Descrambler.PID_TYPE_T, 0, null);
            Log.d(TAG, "tuner init openDescrambler() keyToken = " + keyToken[0]);
        }
        LogUtils.i("Tuner ["+_Id+"]  set mTpInfo to null");
        mTpInfo = null;
        tuner.setOnTuneEventListener(getExecutor(), new OnTuneEventListener() {
            @Override
            public void onTuneEvent(int tuneEvent) {
                if(!isTune){
                    isTune = true;
                    mMonitorLockThread = new Thread(() -> {
                        while (isTune) {
                            try {
                                boolean lock = getLock();
                                set_lock_status(lock);
                                maybeNotifyLockChanged(lock); // ★ 輪詢同步回報
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                // 結束輪詢
                                break;
                            } catch (Exception e) {
                                Log.w(TAG, "MonitorLockThread err: ", e);
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
                        set_lock_status(true);
                        maybeNotifyLockChanged(true);  // ★ 事件回報
                        break;
                    case OnTuneEventListener.SIGNAL_NO_SIGNAL:
                    case OnTuneEventListener.SIGNAL_LOST_LOCK:
                        // arg1 = 0, unlock
                        // arg2 = i, tunerId
                        set_lock_status(false);
						maybeNotifyLockChanged(false); // ★ 事件回報
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

    private void set_dvbt_tuner_type(boolean lock) {
        int dvbt_type = mTpInfo.TerrTp.getDvbtType();
        String[] type_str = {"DVBT_AUTO","DVBT_T","DVBT_T2"};
//            Log.d(TAG,"set_dvbt_tuner_type freq = "+mTpInfo.TerrTp.getFreq()+ " type = "+type_str[dvbt_type]);
        if(lock == true) {
//                Log.d(TAG,"freq = "+mTpInfo.TerrTp.getFreq()+ " type = "+type_str[dvbt_type]);
            int PlpId = getCurPLPId();
//                Log.d(TAG,"PlpId = "+PlpId);
            if(PlpId == 0xff){
                mTpInfo.TerrTp.setDvbtType(TpInfo.Terr.DVBT_TYPE_T);
            }else{
                mTpInfo.TerrTp.setDvbtType(TpInfo.Terr.DVBT_TYPE_T2);
			}
            if(dvbt_type != mTpInfo.TerrTp.getDvbtType()) {
                DataManager dataManager = DataManager.getDataManager(null);
                if(dataManager != null) {
                    dataManager.updateTpInfo(mTpInfo);
                    dataManager.DataManagerSaveData(DataManager.SV_TP_INFO);
                }
            }
        }
    }

    private void set_lock_status(boolean lock) {
        if(mTpInfo != null && mTpInfo.getTunerType() == TpInfo.DVBT) {
            set_dvbt_tuner_type(lock);
        }
        isTunerLock = lock;
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

    public Lnb getLNB() {
        if ( mLnb == null ) {
            int THREAD_COUNT = 4;
            ExecutorService executor;
            executor = Executors.newFixedThreadPool(THREAD_COUNT);
            mLnb = mTuner.openLnb(executor,new LnbCallback() {
                @Override
                public void onEvent(int i) {
                    Log.d(TAG, "LnbCallback : onEvent " + i );
                }

                @Override
                public void onDiseqcMessage(byte[] bytes) {

                }
            });
        }

        return mLnb;
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
            //LogUtils.i("Tuner ["+_Id+"]  set mTpInfo to null");
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
        if(mShareTuner != null){
            mShareTuner.clearOnTuneEventListener();
            mShareTuner.clearResourceLostListener();
            mShareTuner.close();
            mShareTuner = null;
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
    protected abstract boolean tune(TpInfo tpInfo, SatInfo satInfo);

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

    public int getCurPLPId(){
        int PlpId = 0xff;
        FrontendStatus status = null;
        if(mTuner != null/* && checkTpInfo(tpInfo)*/) {
            try {
                status = mTuner.getFrontendStatus(new int[] {FrontendStatus.FRONTEND_STATUS_TYPE_PLP_ID});
//                if(status != null)
//                    Log.d(TAG,"status.getPlpId() = "+status.getPlpId());
                PlpId = status != null ? status.getPlpId() : 0xff;
            }
            catch (Exception e) {
                Log.w(TAG, "getCurPLPId: ", e);
            }
        }
        return PlpId;
    }
    
}
