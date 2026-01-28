package com.prime.tvinputframework;

import static com.prime.datastructure.utils.Utils.getPid;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceConnection;
import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.ServiceDefine.PrimeDtvInterface; // 若未用可移除
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.utils.TVMessage;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.ServiceInterface;

import java.util.List;

/**
 * 全域 Application：啟動時綁 PrimeDTV 服務、註冊回呼、初始化 EPG/Channel，
 * 並維護目前前台 Activity、ChannelChangeManager 等共用元件。
 */
public class PrimeTvInputFrameworkApplication extends Application
        implements PrimeDtvServiceInterface.onMessageListener,
        PrimeDtvServiceConnection.PrimeDtvServiceConnectionCallback {

    private static final String TAG = "PrimeTvInputFrameworkApplication";

    // ---- Singleton / Globals ----
    private static PrimeTvInputFrameworkApplication sInstance;

    // 可能在專案中其它處會用到；目前程式碼未直接使用它
    @Nullable
    private static PrimeDtv sPrimeDtv; // 可依需求移除
    @Nullable
    private static ServiceInterface sServiceInterface; // 可依需求移除

    // 與 PrimeDTV AIDL 封裝橋接
    @Nullable
    private static PrimeDtvServiceInterface sPrimeDtvService;

    // TIF inputId（由 PrimeTvInputService.onCreateSession 傳入後更新）
    @Nullable
    private static String sTvInputId = Pvcfg.getTvInputId();

    // 前台 Activity 追蹤（僅做 onMessage 投遞）
    @Nullable
    private static Activity sCurrentResumedActivity;

    // 與 Service 綁定的連線器
    @Nullable
    private PrimeDtvServiceConnection mPrimeDtvServiceConn;

    // 可選：讓外部註冊 Handler 與 ServiceInterface 溝通
    @Nullable
    private Handler mRegisteredHandler;

    // ------------------------------------------------------------
    // Application lifecycle
    // ------------------------------------------------------------
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        sInstance = this;

        // 取得單例 PrimeDtvServiceInterface 並註冊回呼
        sPrimeDtvService = PrimeDtvServiceInterface.getInstance(this);
        if (sPrimeDtvService == null) {
            Log.e(TAG, "PrimeDtvServiceInterface.getInstance() returned null!");
        } else {
            sPrimeDtvService.register_callbacks(this);
        }

        // 監聽 Activity 生命週期（用於 onMessage 投遞給 BaseActivity）
        registerLifecycleCallback();

        // 綁定 PrimeDTV 服務
        bindPrimeDtvService();
    }

    // ------------------------------------------------------------
    // Activity lifecycle tracking
    // ------------------------------------------------------------
    private void registerLifecycleCallback() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity a, @Nullable Bundle b) {
                Log.d(TAG, a.getLocalClassName() + " Created");
            }

            @Override
            public void onActivityStarted(@NonNull Activity a) {
                Log.d(TAG, a.getLocalClassName() + " Started");
            }

            @Override
            public void onActivityResumed(@NonNull Activity a) {
                Log.d(TAG, a.getLocalClassName() + " Resumed");
                sCurrentResumedActivity = a;
            }

            @Override
            public void onActivityPaused(@NonNull Activity a) {
                Log.d(TAG, a.getLocalClassName() + " Paused");
                if (sCurrentResumedActivity == a) {
                    sCurrentResumedActivity = null;
                }
            }

            @Override
            public void onActivityStopped(@NonNull Activity a) {
                Log.d(TAG, a.getLocalClassName() + " Stopped");
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity a, @NonNull Bundle outState) {
                Log.d(TAG, a.getLocalClassName() + " SaveInstanceState");
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity a) {
                Log.d(TAG, a.getLocalClassName() + " Destroyed");
                if (sCurrentResumedActivity == a) {
                    sCurrentResumedActivity = null;
                }
            }
        });
    }

    // ------------------------------------------------------------
    // Service binding
    // ------------------------------------------------------------
    private void bindPrimeDtvService() {
        int pid = getPid(this);
        mPrimeDtvServiceConn = new PrimeDtvServiceConnection(
                /* context */ this,
                /* wrapper */ get_prime_dtv_service(),
                /* appContext */ getInstance(),
                /* pkg */ PrimeDtvServiceConnection.PRIME_DTV_SERVICE_PKGNAME,
                /* action */ PrimeDtvServiceConnection.PRIME_DTV_SERVICE_ACTION,
                /* tag */ PrimeTvInputService.class.getSimpleName() + " PID: " + pid);
        bindService(mPrimeDtvServiceConn);
    }

    private void bindService(@NonNull PrimeDtvServiceConnection conn) {
        Log.d(TAG, "bindService pkg:" + conn.getPkg() + " action:" + conn.getAction());
        Intent intent = new Intent();
        intent.setPackage(conn.getPkg());
        intent.setAction(conn.getAction());
        try {
            // BIND_AUTO_CREATE：若服務未啟動則自動啟動
            boolean ok = getApplicationContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "bindService result=" + ok);
        } catch (Throwable t) {
            Log.e(TAG, "bindService exception", t);
        }
    }

    private void unbindPrimeDtvService() {
        if (mPrimeDtvServiceConn != null) {
            try {
                getApplicationContext().unbindService(mPrimeDtvServiceConn);
            } catch (Throwable t) {
                Log.w(TAG, "unbindPrimeDtvService ignored", t);
            } finally {
                mPrimeDtvServiceConn = null;
            }
        }
    }

    // ------------------------------------------------------------
    // PrimeDtvServiceConnection callbacks
    // ------------------------------------------------------------
    @Override
    public void onServiceConnected() {
        Log.d(TAG, "onServiceConnected");
        // 把目前 TIF inputId 同步給後端 Service（若已知）
        if (sPrimeDtvService != null)
            sPrimeDtvService.init_service();
    }

    @Override
    public void onServiceDisconnected() {
        Log.d(TAG, "onServiceDisconnected");
        // 服務掉線時可在此清理／標記狀態
    }

    // ------------------------------------------------------------
    // PrimeDtvServiceInterface.onMessageListener
    // ------------------------------------------------------------
    @Override
    public void onMessage(@NonNull TVMessage msg) {
        // 將訊息轉交給目前在前台且繼承 BaseActivity 的畫面

        final int flag = msg.getMsgFlag();

        if (flag == TVMessage.FLAG_SYSTEM && msg.getMsgType() == TVMessage.TYPE_SYSTEM_INIT) {
//            Log.d("dtvService", "TVMessage.TYPE_SYSTEM_INIT callback "+PrimeTvInputFrameworkApplication.class.getSimpleName());
            if (sPrimeDtvService != null && !sPrimeDtvService.isPrimeDtvServiceReady()) {
                try {
                    sPrimeDtvService.set_tv_input_id(sTvInputId);
                } catch (Throwable t) {
                    Log.w(TAG, "set_tv_input_id failed on connect", t);
                }
                sPrimeDtvService.check_lib_correct(com.prime.datastructure.BuildConfig.VERSION_NAME,
                        com.prime.dtv.BuildConfig.VERSION_NAME);
            }

            // 視設定決定是否自動初始化
            if (Pvcfg.isPrimeDTVServiceEnable() && sPrimeDtvService != null) {
                try {
                    // only init tuner when we have channels
                    List<ProgramInfo> programInfoList = sPrimeDtvService.get_program_info_list(
                            FavGroup.ALL_TV_TYPE,
                            MiscDefine.ProgramInfo.POS_ALL,
                            MiscDefine.ProgramInfo.NUM_ALL);
                    if (programInfoList != null && !programInfoList.isEmpty()) {
                        sPrimeDtvService.init_tuner();
                    }

                    // 建好頻道與 EPG
                    sPrimeDtvService.setup_epg_channel();
                    sPrimeDtvService.start_epg(0);
                } catch (Throwable t) {
                    Log.e(TAG, "service init flows failed", t);
                }
            }
            return;
        }
        if (flag == TVMessage.FLAG_AV) {
            // 直接交給目前 owner 的 Session 處理
            PrimeTvInputServiceSession.handleTvMessageFromPrime(msg);
            return;
        }

        if (flag == TVMessage.FLAG_PVR) {
            PrimeTvInputServiceRecordingSession.handleTvMessageFromPrime(msg);
            return;
        }

        // 其它非 AV：你原本的前景 Activity 分派
        Activity a = sCurrentResumedActivity;

        if (a instanceof BaseActivity) {
            Log.d(TAG, "chuck onMessage -> " + a.getClass().getSimpleName());
            ((BaseActivity) a).onMessage(msg);
        } else {
            Log.d(TAG, "onMessage dropped (no foreground BaseActivity)");
        }
    }

    // ------------------------------------------------------------
    // Public static accessors
    // ------------------------------------------------------------
    @MainThread
    public static PrimeTvInputFrameworkApplication getInstance() {
        return sInstance;
    }

    /** 取得 AIDL 封裝服務入口（可為 null，使用前請判斷） */
    @Nullable
    public static PrimeDtvServiceInterface get_prime_dtv_service() {
        return sPrimeDtvService;
    }

    /** TIF onCreateSession 時會回傳 inputId；在此保存並同步給 Service */
    public static void setTvInputId(@Nullable String id) {
        sTvInputId = id;
        PrimeDtvServiceInterface svc = sPrimeDtvService;
        if (svc != null && sTvInputId != null) {
            try {
                svc.set_tv_input_id(sTvInputId);
            } catch (Throwable t) {
                Log.w(TAG, "setTvInputId: sync to service failed", t);
            }
        }
    }

    @Nullable
    public static String getTvInputId() {
        return sTvInputId;
    }

    // ------------------------------------------------------------
    // Optional: handler registration passthrough
    // （若你實際使用的是 ServiceInterface，請視情況改成使用 sPrimeDtvService）
    // ------------------------------------------------------------
    public void registerHandler(@NonNull Handler handler) {
        Log.d(TAG, "registerHandler " + handler);
        mRegisteredHandler = handler;
        if (sServiceInterface != null) {
            try {
                sServiceInterface.registerHandler(handler);
            } catch (Throwable t) {
                Log.w(TAG, "registerHandler via ServiceInterface failed", t);
            }
        } else {
            // 若實際要透過 PrimeDtvServiceInterface 註冊，改成呼叫 sPrimeDtvService 對應 API
            Log.d(TAG, "ServiceInterface is null; skip registerHandler (ok if unused)");
        }
    }

    public void unregisterHandler(@NonNull Handler handler) {
        Log.d(TAG, "unregisterHandler " + handler);
        if (sServiceInterface != null) {
            try {
                sServiceInterface.unregisterHandler(handler);
            } catch (Throwable t) {
                Log.w(TAG, "unregisterHandler via ServiceInterface failed", t);
            }
        } else {
            Log.d(TAG, "ServiceInterface is null; skip unregisterHandler (ok if unused)");
        }
        if (mRegisteredHandler == handler) {
            mRegisteredHandler = null;
        }
    }
}
