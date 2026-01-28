package com.prime.homeplus.tv;

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
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVMessage;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.ServiceInterface;
import com.prime.homeplus.tv.ui.activity.MainActivity;
import com.prime.homeplus.tv.ui.activity.PvrPlayerActivity;

/**
 * 全域 Application：啟動時綁 PrimeDTV 服務、註冊回呼、初始化 EPG/Channel，
 * 並維護目前前台 Activity、ChannelChangeManager 等共用元件。
 */
public class PrimeHomeplusTvApplication extends Application
        implements PrimeDtvServiceInterface.onMessageListener,
        PrimeDtvServiceConnection.PrimeDtvServiceConnectionCallback {

    private static final String TAG = "PrimeHomeplusTvApplication";

    // ---- Singleton / Globals ----
    private static PrimeHomeplusTvApplication sInstance;

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
    // Service Connection Listeners
    // ------------------------------------------------------------
    public interface ServiceConnectionListener {
        void onServiceConnected();
        void onServiceDisconnected();
    }

    private final java.util.List<ServiceConnectionListener> mServiceListeners = new java.util.ArrayList<>();
    private boolean mServiceReadyNotified = false;

    public void addServiceConnectionListener(ServiceConnectionListener listener) {
        if (!mServiceListeners.contains(listener)) {
            mServiceListeners.add(listener);
        }
        if (sPrimeDtvService != null && sPrimeDtvService.isPrimeDtvServiceReady()) {
            listener.onServiceConnected();
        }
    }

    public void removeServiceConnectionListener(ServiceConnectionListener listener) {
        mServiceListeners.remove(listener);
    }

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
                /* tag */ MainActivity.class.getSimpleName() + " PID: " + pid);
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
        if (sPrimeDtvService != null)
            sPrimeDtvService.init_service();

        for (ServiceConnectionListener listener : mServiceListeners) {
            listener.onServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected() {
        Log.d(TAG, "onServiceDisconnected");
        // 服務掉線時可在此清理／標記狀態
        for (ServiceConnectionListener listener : mServiceListeners) {
            listener.onServiceDisconnected();
        }
    }

    // ------------------------------------------------------------
    // PrimeDtvServiceInterface.onMessageListener
    // ------------------------------------------------------------
    @Override
    public void onMessage(@NonNull TVMessage msg) {
        // 將訊息轉交給目前在前台且繼承 BaseActivity 的畫面

        final int flag = msg.getMsgFlag();
        if (flag == TVMessage.FLAG_SYSTEM && msg.getMsgType() == TVMessage.TYPE_SYSTEM_INIT) {
            // Log.d("dtvService", "TVMessage.TYPE_SYSTEM_INIT callback
            // "+PrimeHomeplusTvApplication.class.getSimpleName());
            // 把目前 TIF inputId 同步給後端 Service（若已知）
            if (sPrimeDtvService != null && !sPrimeDtvService.isPrimeDtvServiceReady()) {
                sPrimeDtvService.check_lib_correct(com.prime.datastructure.BuildConfig.VERSION_NAME,
                        com.prime.dtv.BuildConfig.VERSION_NAME);
            }
            if (!mServiceReadyNotified && sPrimeDtvService != null && sPrimeDtvService.isPrimeDtvServiceReady()) {
                mServiceReadyNotified = true;
                for (ServiceConnectionListener listener : mServiceListeners) {
                    listener.onServiceConnected();
                }
            }
            return;
        }

        // 其它非 AV：你原本的前景 Activity 分派
        Activity a = sCurrentResumedActivity;
        if (a instanceof MainActivity) {
            ((MainActivity) a).handleTvMessage(msg);
        } else if (a instanceof PvrPlayerActivity) {
            ((PvrPlayerActivity) a).handleTvMessage(msg);
        } else {
            Log.d(TAG, "onMessage dropped (no foreground MainActivity)");
        }
    }

    // ------------------------------------------------------------
    // Public static accessors
    // ------------------------------------------------------------
    @MainThread
    public static PrimeHomeplusTvApplication getInstance() {
        return sInstance;
    }

    /** 取得 AIDL 封裝服務入口（可為 null，使用前請判斷） */
    @Nullable
    public static PrimeDtvServiceInterface get_prime_dtv_service() {
        return sPrimeDtvService;
    }

    @Nullable
    public static String getTvInputId() {
        return sTvInputId;
    }
}
