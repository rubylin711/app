package com.prime.dtv.service.Tuner;

import android.content.Context;
import android.media.tv.TvInputService;
import android.media.tv.tuner.Tuner;
import android.os.SystemProperties;
import android.util.Log;

import com.prime.dtv.service.Table.Pat;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.utils.LogUtils;
import com.prime.dtv.utils.TVTunerParams;

import java.util.ArrayList;
import java.util.List;

public class TunerInterface {
    private static final String TAG = "TunerInterface";
    //private static final long TIMEOUT_LOCK = 2000; // ms
    public static final int TUNER_TYPE_LIVE = TvInputService.PRIORITY_HINT_USE_CASE_TYPE_LIVE;
    public static final int TUNER_TYPE_PLAYBACK = TvInputService.PRIORITY_HINT_USE_CASE_TYPE_PLAYBACK;
    public static final int NUMBER_OF_TUNER = 4;

    private List<TunerBase> mTunerBase;
    private static TunerInterface mTunerInterface = null;

    private static Context mContext;

    // private, use TunerInterface.getInstance() instead
    private TunerInterface(Context context, int tunerType) {
        // TODO: check useCase
        // use different useCase for scan, live play, ...?
        // https://source.android.com/devices/tv/tuner-framework#client-priority
        mContext = context;
        try {
            mTunerBase = new ArrayList<>();
            for(int i=0 ; i<NUMBER_OF_TUNER ; i++ ) {;
                TunerBase tuner_base;
                if (tunerType == TpInfo.DVBC) {

                    tuner_base = new TunerDVBC();
                    tuner_base.set_Id(i);
                    mTunerBase.add(tuner_base);

                }
                // TODO: DVBS, DVBT, ISDBT, ...
                else { // unsupported tuner type
                    Log.e(TAG, "TunerInterface: undefined tuner type = " + tunerType);
                }
            }

            // from rtk demo for rtk tunerhal
            SystemProperties.set("persist.sys.tunerhal.source.smp", "1"); // no video without this
            SystemProperties.set("persist.sys.tunerhal.source.cas.type", "WV");
        }
        catch (Exception e) {
            Log.e(TAG, "TunerInterface: got exception = " + e);
        }
        /*
        finally {
            if (mTunerBase == null) {
                mTunerBase = new TunerBase(mTuner) {
//                @Override
//                protected boolean checkTpInfo(TpInfo tpInfo) {
//                    return false;
//                }

                    @Override
                    protected boolean tune(int tunerId, TpInfo tpInfo) {
                        return false;
                    }

                    @Override
                    protected boolean tune(int tunerId, TVTunerParams tunerParams) {
                        return false;
                    }
                };
            }
        }
         */
    }

    public void init_tuner(){
        for(int i=0 ; i<NUMBER_OF_TUNER ; i++ ) {
            Tuner tuner = new Tuner(mContext, null, TvInputService.PRIORITY_HINT_USE_CASE_TYPE_LIVE);
            mTunerBase.get(i).update(tuner);
            requestDemusResource(i);
        }
    }

    public void SetTunerLock(int TunerId, boolean islock){
        mTunerBase.get(TunerId).setTunerLock(islock);
    }

    public static TunerInterface getInstance(Context context) {
        if(mTunerInterface == null) {
            mTunerInterface = new TunerInterface(context, DataManager.TUNR_TYPE);
        }
        return mTunerInterface;
    }

    public static TunerInterface getInstance(){
        return mTunerInterface;
    }

    public Tuner getTuner(int tunerId) {
  		check_tuner_available(tunerId);
        return mTunerBase.get(tunerId).getTuner();
    }

    // return true if tune success and locked before timeout
    public boolean tune_only(int tunerId, TpInfo tpInfo) {
        check_tuner_available(tunerId);
        return mTunerBase.get(tunerId).tune(tpInfo);
    }
    public boolean tune(int tunerId, TpInfo tpInfo) {
        LogUtils.d("tune and wait lock tunerId = "+tunerId);
        check_tuner_available(tunerId);
        return  mTunerBase.get(tunerId).tune(tpInfo) &&  waitLock(tunerId);
    }

    public boolean tune(int tunerId, TVTunerParams tunerParams) {
        LogUtils.d("tune and wait lock tunerId = "+tunerId);
        check_tuner_available(tunerId);
        return mTunerBase.get(tunerId).tune(tunerParams) && waitLock(tunerId);
    }

    public boolean cancelTune(int tunerId) {
        check_tuner_available(tunerId);
        return mTunerBase.get(tunerId).cancelTune();
    }

    public boolean isLock(int tunerId/*, TpInfo tpInfo*/) {
        //LogUtils.d("tunerId = "+tunerId);
        check_tuner_available(tunerId);
        return mTunerBase.get(tunerId).isLock();
    }

    public int getStrength(int tunerId/*, TpInfo tpInfo*/) {
        check_tuner_available(tunerId);
        return mTunerBase.get(tunerId).getStrength();
    }

    public int getQuality(int tunerId/*, TpInfo tpInfo*/) {
        check_tuner_available(tunerId);
        return mTunerBase.get(tunerId).getQuality();
    }

    public int getSnr(int tunerId/*, TpInfo tpInfo*/) {
        check_tuner_available(tunerId);
        return mTunerBase.get(tunerId).getSnr();
    }

    public int getBer(int tunerId/*, TpInfo tpInfo*/) {
        check_tuner_available(tunerId);
        return mTunerBase.get(tunerId).getBer();
    }

    public TpInfo getTpinfo(int tunerId){
        return mTunerBase.get(tunerId).getTpInfo();
    }
    public void setTpinfo(int tunerId, TpInfo tp){
        mTunerBase.get(tunerId).setTpInfo(tp);
    }
    public void close(int tunerId) {
        /*
        if(mTuner != null) {
            mTuner.close();
            mTuner = null;
        }*/
        Log.d(TAG,"tuner close tunerId = "+tunerId);
        mTunerBase.get(tunerId).close();
        mTunerBase.add(tunerId, null);
    }

    private boolean waitLock(int tunerId) {
        check_tuner_available(tunerId);
        return mTunerBase.get(tunerId).waitLock();
    }

    private void requestDemusResource(int tunerID){
        LogUtils.d("tunerID = "+tunerID);
        new Pat(tunerID);
    }

    private void check_tuner_available(int tunerId){
        if(mTunerBase.get(tunerId).getTuner() == null){
            LogUtils.d(" tunerId = "+tunerId);
            Tuner tuner = new Tuner(mContext, null, TvInputService.PRIORITY_HINT_USE_CASE_TYPE_LIVE);
            mTunerBase.get(tunerId).update(tuner);
            requestDemusResource(tunerId);
        }
    }

    public void requestTuner(int tunerId, int flag){
        LogUtils.d("tunerId = "+tunerId);

        TunerBase tunerBase = mTunerBase.get(tunerId);
        tunerBase.close();
        tunerBase.update(new Tuner(mContext, null, flag));
        if(flag == TunerInterface.TUNER_TYPE_LIVE)
            requestDemusResource(tunerId);
    }
}
