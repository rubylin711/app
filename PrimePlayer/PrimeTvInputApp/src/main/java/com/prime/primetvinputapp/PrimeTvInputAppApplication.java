package com.prime.primetvinputapp;

import static com.prime.datastructure.utils.Utils.getPid;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceConnection;
import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.utils.TVMessage;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.ServiceInterface;

public class PrimeTvInputAppApplication extends Application implements PrimeDtvServiceInterface.onMessageListener, PrimeDtvServiceConnection.PrimeDtvServiceConnectionCallback{
    private static String TAG = "PrimeTvInputFrameworkApplication";
    private static PrimeTvInputAppApplication gInstance;
    private static PrimeDtv g_dtv;
    private Handler gHandler;
    private static ServiceInterface gServiceInterface;
    private static Activity g_CurrentResumeActivity = null ;
    private static PrimeDtvServiceInterface gPrimeDtvServiceInterface = null;
    private static PlayerControl gPlayerControl = null;
    private static String gTvInputId = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "pppp app onCreate");
        gInstance = this;
        gPrimeDtvServiceInterface = PrimeDtvServiceInterface.getInstance(this);
        gPrimeDtvServiceInterface.register_callbacks(this);
        gPlayerControl = PlayerControl.getInstance(this);
        gPlayerControl.initTvInputManager();
        register_lifecycle_callback();
        int pid = getPid(this);
        PrimeDtvServiceConnection gPrimeDtvServiceConn = new PrimeDtvServiceConnection(this,
                gPrimeDtvServiceInterface,this,PrimeDtvServiceConnection.PRIME_DTV_SERVICE_PKGNAME,
                PrimeDtvServiceConnection.PRIME_DTV_SERVICE_ACTION,PrimeTvInputAppApplication.class.getSimpleName()
                +" PID: "+pid);
        bindService(gPrimeDtvServiceConn);
    }

    public static PrimeTvInputAppApplication getInstance() {
        Log.d(TAG, "getInstance " + gInstance);
        return gInstance;
    }

    public void register_lifecycle_callback() {
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
        Log.d(TAG, "registerHandler " + handler);
        gServiceInterface.registerHandler(handler);
        gHandler = handler ;
    }

    public void unregisterHandler(Handler handler) {
        Log.d(TAG, "unregisterHandler " + handler);
        gServiceInterface.unregisterHandler(handler);
        gHandler = null ;
    }

    public void onMessage(TVMessage msg) {
        Log.d("ggggg", "onMessage : g_CurrentResumeActivity = " + g_CurrentResumeActivity);
        if(g_CurrentResumeActivity != null)
            Log.d("ggggg", "onMessage : " + g_CurrentResumeActivity.getClass());
        if ( g_CurrentResumeActivity instanceof BaseActivity ) {
            Log.d("ggggg", "onMessage : " + g_CurrentResumeActivity.getClass());
            ((BaseActivity) g_CurrentResumeActivity).onMessage(msg);
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

    public static PlayerControl getPlayerControl() {
        return gPlayerControl;
    }

    @Override
    public void onServiceConnected() {
        if(gTvInputId != null)
            gPrimeDtvServiceInterface.set_tv_input_id(gTvInputId);
    }

    @Override
    public void onServiceDisconnected() {

    }
}