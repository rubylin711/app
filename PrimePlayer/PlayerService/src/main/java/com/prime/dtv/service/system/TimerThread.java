package com.prime.dtv.service.system;

import android.content.Context;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;

import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.datastructure.sysdata.DTVMessage;

import java.util.Calendar;

public class TimerThread extends Thread{
    private static final String TAG = "TimerThread";
    private boolean DEBUG = SystemProperties.getBoolean("persist.sys.prime.debug.timer_thread",false);
    private Context mContext;
    private PesiDtvFrameworkInterfaceCallback mCallback;
    private DataManager mDataManager;
    private final int AD_CHECK_TIME = SystemProperties.getInt("persist.sys.prime.ad_check_count", 10800);// 3 hours
    public int mCheckADDuration;
    public int mRunningTime;

    public TimerThread(Context context, Handler handler){
        mContext = context;
        mCallback  = new PesiDtvFrameworkInterfaceCallback(handler);
        mDataManager = DataManager.getDataManager(mContext);
        mCheckADDuration = 0;
        mRunningTime = 0;
    }

    @Override
    public void run() {
        Log.d(TAG,"TimerThread start");
        while(true){
            Calendar calendar = Calendar.getInstance();
            handle_liad_timer(calendar);
            handl_wvcas_ref_casdata_time();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void ResetCheckADDuration(){
        mCheckADDuration = 0;
    }

    private boolean isDEBUG(){
        DEBUG = SystemProperties.getBoolean("persist.sys.prime.debug.timer_thread",false);
        return DEBUG;
    }

    private void handle_liad_timer(Calendar calendar){
        int hour = calendar.get(Calendar.HOUR_OF_DAY), start_checking_hour = Pvcfg.getCheckADTime() - 3, end_checking_hour = (Pvcfg.getCheckADTime() + 2)%24;
        int minute = calendar.get(Calendar.MINUTE);
        if(isDEBUG())
            LogUtils.d("TimerThread ["+hour+" : "+minute+"]"+" start_checking_hour = "+start_checking_hour+" endi_checking_hour = "+end_checking_hour);
        if(hour >= start_checking_hour && hour <= end_checking_hour){
            if(isDEBUG())
                LogUtils.d("mCheckADDuration = "+mCheckADDuration+" AD_CHECK_TIME = "+AD_CHECK_TIME);
            if(mCheckADDuration >= AD_CHECK_TIME) {
                mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_BOOK_RUN_AD, 0, 0, null);
                mCheckADDuration = -100;//Avoid Run AD again in the same day
            }

            mCheckADDuration ++;
        }else{
            mCheckADDuration = 0;
        }
    }

    private void handl_wvcas_ref_casdata_time(){
        if(mDataManager == null){
            mDataManager = DataManager.getDataManager(mContext);
            if(mDataManager == null){
                return;
            }
        }
        //LogUtils.d("getSTBRefCasDataTime = "+mDataManager.getGposInfo().getSTBRefCasDataTime());
        if(GposInfo.getSTBRefCasDataTime(mContext) == 0)
            return;
        if(mRunningTime >= (GposInfo.getSTBRefCasDataTime(mContext)*60)){
            LogUtils.d(" call  PESI_EVT_CA_WIDEVINE_REFRESH_CAS_DATA mRunningTime = "+mRunningTime+" getSTBRefCasDataTime = "+GposInfo.getSTBRefCasDataTime(mContext)*60);
            mRunningTime = 0;
            if(Pvcfg.isRunRefreshCasByTime())
                mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_CA_WIDEVINE_REFRESH_CAS_DATA, 0 , 0, GposInfo.getCurChannelId(mContext));
            return;
        }
        mRunningTime ++;
    }
}
