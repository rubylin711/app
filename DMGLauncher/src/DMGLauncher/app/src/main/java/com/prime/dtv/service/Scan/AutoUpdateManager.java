package com.prime.dtv.service.Scan;

import android.content.Context;

import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class AutoUpdateManager {
    private static final String TAG = "AutoUpdateManager";
    private static PesiDtvFrameworkInterfaceCallback mCallback;

    private List<PmtUpdater> mPmtUpdateList = new ArrayList<>();
    private PmtUpdater[] pmtUpdaters = new PmtUpdater[3];
    private SIUpdater mSIUpdater;
    private EmmUpdater mEmmUpdater;
    private Context mContext;

    public AutoUpdateManager(Context context, PesiDtvFrameworkInterfaceCallback callback) {
        //super(context, TAG, handler, FrontendCmdManager.class);
        mCallback = callback;
        mContext = context;
    }

    private void stop_all(){
        for(int i=0 ; i<3 ; i++) {
            if (pmtUpdaters[i] != null) {
                PmtUpdater pmtUpdater = pmtUpdaters[i];
                LogUtils.d("pmtUpdater = " + pmtUpdater);
                LogUtils.d("Channel ID = " + pmtUpdater.getChannelId() + " isFcc = " + pmtUpdater.getIsFcc());
                pmtUpdaters[i].stop();
            }
        }
        if(mEmmUpdater != null)
            mEmmUpdater.stop();
        if(mSIUpdater != null)
            mSIUpdater.stop();
    }
    public void StopTableMonitor(long channelId, int tuner_id) {
        LogUtils.d("StopTableMonitor IN ==> channelId = " + channelId);
        if(channelId == -1){
            stop_all();
            return;
        }
        if(pmtUpdaters[tuner_id] != null){
            pmtUpdaters[tuner_id].stop();
        }
//        int size = mPmtUpdateList.size();
//        for (int i = 0; i < size; i++) {
//            PmtUpdater pmtUpdater = mPmtUpdateList.get(i);
//            if (pmtUpdater.getChannelId() == channelId || channelId == -1) {
//                pmtUpdater.setChannelId(0);
//                pmtUpdater.stop();
//                //mPmtUpdateList.remove(i);
//                //size--;
//            }
//
//        }
    }

    public void ShowRuningPmtUpdater(){
//        for (PmtUpdater pmtUpdater : mPmtUpdateList){
//            LogUtils.d("pmtUpdater = "+pmtUpdater);
//            LogUtils.d("Channel ID = "+pmtUpdater.getChannelId()+" isFcc = "+pmtUpdater.getIsFcc());
//        }
        for(int i =0 ; i <3 ; i++){
            if (pmtUpdaters[i] != null){
                PmtUpdater pmtUpdater = pmtUpdaters[i];
                LogUtils.d("pmtUpdater = "+pmtUpdater);
                LogUtils.d("Channel ID = "+pmtUpdater.getChannelId()+" isFcc = "+pmtUpdater.getIsFcc());
            }
        }
    }
    public void StartTableMonitor(long channelId, int isFcc, int tuner_id) {
        if(Pvcfg.isPmtupdateEnable() == false){
            return;
        }
        LogUtils.d("StartTableMonitor IN ==> channelId = " + channelId + " isFcc = " + isFcc+" tuner_id = "+tuner_id);
//        for (PmtUpdater pmtUpdater : mPmtUpdateList) {
//            if (pmtUpdater.getChannelId() == channelId) {
//                LogUtils.e("The PmtUpdate already running");
//                pmtUpdater.setIsFcc(isFcc);
//                pmtUpdater.check_si_emm_stop();
//                pmtUpdater.start();
//                ShowRuningPmtUpdater();
//                return;
//            }
//        }
        if(pmtUpdaters[tuner_id] == null) {
            pmtUpdaters[tuner_id] = new PmtUpdater(mContext, mCallback, channelId, isFcc);
        }
        else {
            pmtUpdaters[tuner_id].setChannelId(channelId);
            pmtUpdaters[tuner_id].setIsFcc(isFcc);
        }
        pmtUpdaters[tuner_id].setTunerId(tuner_id);
        pmtUpdaters[tuner_id].start();
        if(isFcc == 0){
            if(Pvcfg.isSiupdateEnable()) {
                if (mSIUpdater == null) {
                    mSIUpdater = new SIUpdater(mContext, mCallback, channelId);
                } else {
                    mSIUpdater.setChannelId(channelId);
                }
                mSIUpdater.setTunerId(tuner_id);
                mSIUpdater.start();
            }
            if(Pvcfg.isProcessEMM()) {
                if (mEmmUpdater == null) {
                    mEmmUpdater = new EmmUpdater(mContext, mCallback, channelId);
                } else {
                    mEmmUpdater.setChannelId(channelId);
                }
                mEmmUpdater.setTunerId(tuner_id);
                mEmmUpdater.start();
            }
        }
        //mPmtUpdateList.add(pmtUpdater);
        //ShowRuningPmtUpdater();
    }
}
