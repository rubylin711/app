package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.Interface.BaseManager;
import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.service.Table.Eit;
import com.prime.dtv.service.Table.EitSchedulerDMG;
import com.prime.dtv.service.Table.Table;
import com.prime.dtv.service.Table.Tdt;
import com.prime.dtv.service.Tuner.TunerInterface;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.TpInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EpgCmdManager extends BaseManager {
    private static final String TAG = "EpgCmdManager";

    // schedule epg use last tuner, e.g. 4 tuners, tuner id = 0, 1, 2, 3, epg use 3
    public static int EPG_TUNER_ID = Pvcfg.PROJECT_HOME_TP_TUNER_ID;
    private static final int EPG_MAIN_FREQUENCY = Pvcfg.getSIHomeTPFrequency(); // main freq for schedule EPG
    private static final int MSG_START_SCHEDULE_EIT = 1;
    private static final int MSG_STOP_SCHEDULE_EIT = 2;
    private static final int NUM_OF_SCHEDULER_EIT = 20;

    private Eit mScheduleEit = null;
    private Eit mScheduleEit2 = null;
    private EitSchedulerDMG[] mEitSchedulerDMGs = new EitSchedulerDMG[NUM_OF_SCHEDULER_EIT];
    private List<Integer>[] mServiceIDs = new ArrayList[NUM_OF_SCHEDULER_EIT];
    private final TunerInterface mTunerInterface;
    private final DataManager mDataManager;
    private boolean mStopEitScheduler = false;
    private int mTimeoutMS = SystemProperties.getInt("persist.sys.prime.eit_timeout", 10000);
    private int mTpId;
    private boolean mReadyToRun;

    public EpgCmdManager(Context context, Handler handler) {
        super(context, TAG, handler, EpgCmdManager.class);
        if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)
                || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC)
                || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS))
            EPG_TUNER_ID = Pvcfg.PROJECT_HOME_TP_TUNER_ID;
        else
            EPG_TUNER_ID = 0;
        mTunerInterface = TunerInterface.getInstance(context);
        mDataManager = DataManager.getDataManager(context);
        for (int i = 0; i < NUM_OF_SCHEDULER_EIT; i++) {
            mServiceIDs[i] = new ArrayList<>();
        }
        mTpId = -1;
        mReadyToRun = false;
    }

    /*
     * EPG
     */
    public EPGEvent getPresentEvent(long channelId) {
        return null;
    }

    public EPGEvent getFollowEvent(long channelId) {
        return null;
    }

    public EPGEvent getEpgByEventID(long channelId, int eventId) {
        return null;
    }

    public List<EPGEvent> getEPGEvents(long channelId, Date startDate, Date endDate, int offset, int onetimeObtainedNumber, int addEmpty) {
        return null;
    }

    public String getShortDescription(long channelId, int eventId) {
        return null;
    }

    public String getDetailDescription(long channelId, int eventId) {
        return null;
    }

    public int setEvtLang(String firstLangCode, String secLangCode) {
        return 0;
    }

    public Date getDtvDate() {
        return new Date();
    }

    public void startScheduleEit(int tp_id, int tuner_id) {
        TpInfo tpInfo = null;
        Message msg = Message.obtain();
        if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)
                || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC)
                || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS)) {
//            tpInfo = mDataManager.getTpInfoByFreq(EPG_MAIN_FREQUENCY);
//            LogUtils.d("EPG_MAIN_FREQUENCY = " + EPG_MAIN_FREQUENCY);
//            LogUtils.d("tpInfo = " + tpInfo);
//            if (tpInfo != null) {
//                mTunerInterface.tune(EPG_TUNER_ID, tpInfo);
//                if (Pvcfg.getUpdateTdt())
//                    new Tdt(EPG_TUNER_ID);
//            }
        }
        if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG) && tpInfo != null) {
            msg.what = MSG_START_SCHEDULE_EIT;
            msg.arg1 = tpInfo.getTpId();
            msg.arg2 = tuner_id;
            DoCommandDelayed(msg, 300000);// 5 mi
        } else {
            tpInfo = mDataManager.getTpInfo(tp_id);
            LogUtils.d("tpInfo = " + tpInfo);
            msg.what = MSG_START_SCHEDULE_EIT;
            msg.arg1 = tp_id;
            msg.arg2 = tuner_id;
            tpInfo = mDataManager.getTpInfo(tp_id);
            if (tpInfo != null) {
                if (Pvcfg.getUpdateTdt())
                    new Tdt(tuner_id);
            }
            DoCommand(msg);
        }
    }

    private void startScheduleEitInternal(int tp_id, int tuner_id) throws InterruptedException {
        stopScheduleEitInternal();

        if (Pvcfg.isEnableEIT_Scheduler() == false) {
            return;
        }
        TpInfo tpInfo = mDataManager.getTpInfo(tp_id);
        Log.d(TAG, "startScheduleEitInternal tpInfo = " + tpInfo);
        if (tpInfo != null) {
            if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)) {
                int index = 0, i = 0, j = 0;
                List<Integer> serviceIds = new ArrayList<>();
                List<ProgramInfo> programInfoList = mDataManager.getProgramInfoList();
                for (ProgramInfo tmp : programInfoList) {
                    serviceIds.add(tmp.getServiceId());
                    mServiceIDs[i].add(tmp.getServiceId());
                    i++;
                    if (i == NUM_OF_SCHEDULER_EIT)
                        i = 0;
                }
                for (i = 0; i < NUM_OF_SCHEDULER_EIT; i++) {
                    LogUtils.d("mServiceIDs [" + i + "] = " + mServiceIDs[i].toString());
                    if (mServiceIDs[i].size() > 0) {
                        mEitSchedulerDMGs[i] = new EitSchedulerDMG(EPG_TUNER_ID,
                                Table.EIT_SCHEDULING_MIN_TABLE_ID,
                                mServiceIDs[i],
                                mTimeoutMS);
                        // Thread.sleep(250);
                    }
                }
            } else {
                SatInfo satInfo;
                satInfo = mDataManager.getSatInfo(tpInfo.getSatId());
                boolean isLock = mTunerInterface.tune(tuner_id, tpInfo, satInfo);
                Log.d(TAG, "startScheduleEitInternal isLock = " + isLock + " tuner_id = " + tuner_id +
                        " tpInfo = " + tpInfo.ToString());
                if (isLock) {
                    runEitSchedule(tuner_id, tpInfo);
                }
            }
        }
    }

    public void stopScheduleEit() {
        mStopEitScheduler = true;
/*        Message msg = Message.obtain();
        msg.what = MSG_STOP_SCHEDULE_EIT;
        DoCommand(msg);*/
    }

    private void stopScheduleEitInternal() {
        if (mScheduleEit != null) {
            mScheduleEit.abort();
            mScheduleEit.cleanup();
            mScheduleEit = null;
        }
        if (mScheduleEit2 != null) {
            mScheduleEit2.abort();
            mScheduleEit2.cleanup();
            mScheduleEit2 = null;
        }
        for(int i=0 ; i< NUM_OF_SCHEDULER_EIT ; i++){
            if(mEitSchedulerDMGs[i] != null){
                mEitSchedulerDMGs[i].abort();
                mEitSchedulerDMGs[i].cleanup();
                mEitSchedulerDMGs[i] = null;
            }
        }
    }

    @Override
    public void BaseHandleMessage(Message msg) {
        switch(msg.what) {
            case MSG_START_SCHEDULE_EIT: {
                try {
                    startScheduleEitInternal(msg.arg1,msg.arg2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } break;
            case MSG_STOP_SCHEDULE_EIT: {
                stopScheduleEitInternal();
            } break;
            default: {
                Log.d(TAG,"unknown msg what = " + msg.what);
            }
        }
    }

    public void set_tuner_ready() {
        mReadyToRun = true;
        TpInfo tpInfo = mDataManager.getTpInfoByFreq(EPG_MAIN_FREQUENCY);
        LogUtils.d("EPG_MAIN_FREQUENCY = " + EPG_MAIN_FREQUENCY);
        LogUtils.d("tpInfo = " + tpInfo);
        if (tpInfo != null) {
            mTunerInterface.tune(EPG_TUNER_ID, tpInfo);
            if (Pvcfg.getUpdateTdt())
                new Tdt(EPG_TUNER_ID);
        }
    }

    private void runEitSchedule(int tuner_id, TpInfo tpInfo) {
//        try {
            if (mScheduleEit == null) {
                mScheduleEit = new Eit(
                        tuner_id,
                        Table.EIT_SCHEDULING_MIN_TABLE_ID,
                        null, 1);
                // Thread.sleep(10000);
            }
            if (mScheduleEit2 == null) {
                mScheduleEit2 = new Eit(
                        tuner_id,
                        Table.EIT_SCHEDULING_MIN_TABLE_ID,
                        null, 0);
            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
}
