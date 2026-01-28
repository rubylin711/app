package com.prime.homeplus.settings;

import static com.prime.datastructure.utils.Utils.getPid;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceConnection;
import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.utils.TVMessage;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.ServiceInterface;
import com.prime.homeplus.settings.dialog.OTADialog;

public class PrimeApplication extends Application implements PrimeDtvServiceInterface.onMessageListener, PrimeDtvServiceConnection.PrimeDtvServiceConnectionCallback {
    private static String TAG = "PrimeApplication";
    private static PrimeApplication gInstance;
    private static PrimeDtv g_dtv;
    private Handler gHandler;
   private static ServiceInterface gServiceInterface;
    private static Activity g_CurrentResumeActivity = null ;
    private static PrimeDtvServiceInterface gPrimeDtvServiceInterface = null;
    //private static PlayerControl gPlayerControl = null;
    private static String gTvInputId = "com.prime.tvinputframework/.PrimeTvInputService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "pppp app onCreate");
        register_lifecycle_callback();
        gInstance = this;
        gPrimeDtvServiceInterface = PrimeDtvServiceInterface.getInstance(this);
        gPrimeDtvServiceInterface.register_callbacks(this);
        //gPlayerControl = PlayerControl.getInstance(this);
        //gPlayerControl.initTvInputManager();
        int pid = getPid(this);
        PrimeDtvServiceConnection gPrimeDtvServiceConn = new PrimeDtvServiceConnection(this,
                gPrimeDtvServiceInterface,this,PrimeDtvServiceConnection.PRIME_DTV_SERVICE_PKGNAME,
                PrimeDtvServiceConnection.PRIME_DTV_SERVICE_ACTION, PrimeApplication.class.getSimpleName()
                +" PID: "+pid);
        bindService(gPrimeDtvServiceConn);
    }

    public static PrimeApplication getInstance() {
        Log.d(TAG, "getInstance " + gInstance);
        return gInstance;
    }

    public void register_lifecycle_callback() {
        Log.d(TAG,"[Ethan] register_lifecycle_callback");
        // 注册 Activity 生命周期回调
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.d(TAG, activity.getLocalClassName() + " Created");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + " Started");
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + " Resumed");
                g_CurrentResumeActivity = activity ;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + " Paused");
                g_CurrentResumeActivity = null;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + " Stopped");
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                Log.d(TAG, activity.getLocalClassName() + " SaveInstanceState");
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + " Destroyed");
            }
        });
    }

    public static PrimeDtvServiceInterface get_prime_dtv_service() {
        return gPrimeDtvServiceInterface;
    }
//    public static PrimeDtv get_prime_dtv() {
//        return g_dtv;
//    }

    public void registerHandler(Handler handler) {
        Log.d(TAG, "[Ethan] registerHandler " + handler);
        //gServiceInterface.registerHandler(handler);
        gHandler = handler ;
    }

    public void unregisterHandler(Handler handler) {
        Log.d(TAG,"unregisterHandler " + handler);
        //gServiceInterface.unregisterHandler(handler);
        gHandler = null ;
    }

    @Override
    public void onMessage(TVMessage tvmsg) {
        if(tvmsg != null) {
            int msgType = tvmsg.getMsgType();
            Log.d(TAG, "[Ethan] onMessage : MsgType = " + tvmsg.getMsgType());
            if (tvmsg.getMsgFlag() == TVMessage.FLAG_SYSTEM && tvmsg.getMsgType() == TVMessage.TYPE_SYSTEM_INIT) {
//                Log.d("dtvService", "TVMessage.TYPE_SYSTEM_INIT callback "+ PrimeApplication.class.getSimpleName());
                if(gPrimeDtvServiceInterface != null && !gPrimeDtvServiceInterface.isPrimeDtvServiceReady())
                    gPrimeDtvServiceInterface.check_lib_correct(com.prime.datastructure.BuildConfig.VERSION_NAME,
                            com.prime.dtv.BuildConfig.VERSION_NAME);
                return;
            }
            switch(msgType){
                case TVMessage.TYPE_SCAN_SCHEDULE:{
                    LogUtils.d("[Ethan] percent = "+tvmsg.getPercent());
                    Message msg = new Message();
                    msg.what = TVMessage.TYPE_SCAN_SCHEDULE;
                    msg.arg1 = tvmsg.getPercent();
                    //LogUtils.d("[Ethan] gHandler = "+gHandler);
                    if(gHandler != null)
                        gHandler.sendMessage(msg);
                }break;
                case TVMessage.TYPE_SCAN_END:{
                    LogUtils.d("[Ethan] tv = "+tvmsg.getTotalTVNumber()+" radio = "+tvmsg.getTotalRadioNumber());
                    Message msg = new Message();
                    msg.what = TVMessage.TYPE_SCAN_END;
                    msg.arg1 = tvmsg.getTotalTVNumber();
                    msg.arg2 = tvmsg.getTotalRadioNumber();
                    if(gHandler != null)
                        gHandler.sendMessage(msg);
                }break;
                case TVMessage.TYPE_OTA_SHOW_COUNT_DOWN_DIALOG:{
                    LogUtils.d("[tony] lastVersion = "+tvmsg.getOtaLastVersion());
                    showOTACountDownDialog(tvmsg.getOtaLastVersion());
                }
                case TVMessage.TYPE_OTA_SHOW_UPDATE_DIALOG:{
                    LogUtils.d("[tony] lastVersion = "+tvmsg.getOtaLastVersion());
                    showOTAUpdateDialog(tvmsg.getOtaLastVersion());
                }

            }
        }

    }


    public static void setTvInputId(String id) {
        gTvInputId = id;
//        if(gPrimeDtvServiceInterface != null && gTvInputId != null)
//            gPrimeDtvServiceInterface.set_tv_input_id(gTvInputId);
    }

    public static String getTvInputId() {
        return gTvInputId;
    }

    private void bindService(PrimeDtvServiceConnection serviceConnection) {
        Log.d(TAG, "bindService pkg:" + serviceConnection.getPkg() + " action:" + serviceConnection.getAction() );
        Intent intent = new Intent();
        intent.setPackage(serviceConnection.getPkg());
        intent.setAction(serviceConnection.getAction());
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

//    public static PlayerControl getPlayerControl() {
//        return gPlayerControl;
//    }

    @Override
    public void onServiceConnected() {
        Log.d(TAG,"[Ethan] onServiceConnected");
        if (gPrimeDtvServiceInterface != null)
            gPrimeDtvServiceInterface.init_service();
    }

    @Override
    public void onServiceDisconnected() {
        Log.d(TAG,"[Ethan] onServiceDisconnected");
    }

    private void showOTACountDownDialog(String otaLastVersion) {
        Log.d(TAG, "showCountDownDialog:");

        if (g_CurrentResumeActivity == null) {
            Log.e(TAG, "showCountDownDialog: g_CurrentResumeActivity == null");
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            OTADialog otaDialog = new OTADialog(g_CurrentResumeActivity);
            otaDialog.show_count_down_dialog();
        });
    }

    private void showOTAUpdateDialog(String otaLastVersion) {
        Log.d(TAG, "showCountDownDialog:");

        if (g_CurrentResumeActivity == null) {
            Log.e(TAG, "showCountDownDialog: g_CurrentResumeActivity == null");
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            OTADialog otaDialog = new OTADialog(g_CurrentResumeActivity);
            otaDialog.show_update_dialog(otaLastVersion);
        });
    }
}
