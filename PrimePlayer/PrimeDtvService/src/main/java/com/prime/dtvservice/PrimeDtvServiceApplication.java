package com.prime.dtvservice;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.util.Log;

import com.prime.datastructure.CommuincateInterface.IPrimeDtvServiceCallback;
import com.prime.datastructure.ServiceDefine.PrimeDtvInterface;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVMessage;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.PrimeTimerReceiver;
import com.prime.dtv.ServiceInterface;
import com.prime.dtv.utils.UsbUtils;

public class PrimeDtvServiceApplication extends Application {
    public static String TAG = "PrimeDtvServiceApplication";
    private static PrimeDtvServiceApplication gInstance;
    private static PrimeDtv g_dtv = null;
    private static Handler gHandler;
    private PrimeDtvInterface.DTVCallback gCallback = null;
    private static android.os.RemoteCallbackList<IPrimeDtvServiceCallback> mRemoteCallbacks = new android.os.RemoteCallbackList<>();
    private IPrimeDtvServiceCallback mIPrimeDtvServiceCallback = null;
    private static ServiceInterface mServiceInterface = null;
    private static Activity g_CurrentResumeActivity = null;
    private static ChannelChangeManager mChannelChangeManager = null;
    private UsbReceiver mUsbReceiver = null;
    private PrimeTimerReceiver mTimerReceiver = null;

    private final static java.util.concurrent.CountDownLatch mInitLatch = new java.util.concurrent.CountDownLatch(1);
    private HandlerThread mHandlerThread = new HandlerThread("PrimeDtvServiceApplication HandlerThread");
    private static final Object mBroadcastLock = new Object();

    public static void waitForInit() {
        try {
            mInitLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.w(TAG, "Interrupted while waiting for init", e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        gInstance = this;
        mHandlerThread.start();
        gHandler = new Handler(mHandlerThread.getLooper());
        primeDtvInit();
        detectUsbStorage(this);
        register_lifecycle_callback();
    }

    public void primeDtvInit() {
        gHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Initializing ServiceInterface in background thread...");
                Log.d(TAG, "[PrimeDtvServiceAIDL] primeDtvInit 1111111111111");
                mServiceInterface = ServiceInterface.getServiceInterfaceInstance(new PrimeDtvInterface.DTVCallback() {
                    @Override
                    public void onMessage(TVMessage msg) {
                        Log.d(TAG,
                                "[PrimeDtvServiceAIDL] onMessage msg = " + msg.getMessage() + " type "
                                        + msg.getMsgType() + " flag "
                                        + msg.getMsgFlag());

                        synchronized (mBroadcastLock) {
                            int n = mRemoteCallbacks.beginBroadcast();
                            Log.d(TAG, "[PrimeDtvServiceAIDL] onMessage: broadcasting to " + n + " callbacks");
                            try {
                                for (int i = 0; i < n; i++) {
                                    IPrimeDtvServiceCallback callback = mRemoteCallbacks.getBroadcastItem(i);
                                    String connectedFrom = (String) mRemoteCallbacks.getBroadcastCookie(i);
                                    if (callback != null) {
                                        try {
                                            callback.onMessage(msg);
                                            Log.d(TAG, "[PrimeDtvServiceAIDL] Successfully sent message to: "
                                                    + connectedFrom);
                                        } catch (android.os.DeadObjectException e) {
                                            Log.w(TAG, "[PrimeDtvServiceAIDL] DeadObjectException for " + connectedFrom
                                                    + ". RemoteCallbackList will handle cleanup.");
                                        } catch (RemoteException e) {
                                            Log.e(TAG, "[PrimeDtvServiceAIDL] Failed to invoke connectedFrom for "
                                                    + connectedFrom, e);
                                        }
                                    }
                                }
                            } finally {
                                mRemoteCallbacks.finishBroadcast();
                            }
                        }
                    }
                }, PrimeDtvServiceApplication.this);
                Log.d(TAG, "[PrimeDtvServiceAIDL] mServiceInterface init done...");
                g_dtv = mServiceInterface.get_prime_dtv();
                mInitLatch.countDown();
                mChannelChangeManager = ChannelChangeManager.get_instance(PrimeDtvServiceApplication.this);
                Log.d(TAG, "[PrimeDtvServiceAIDL] ServiceInterface initialization completed.");
                if (!TextUtils.isEmpty(UsbUtils.get_mount_usb_path())) {
                    g_dtv.pvr_init(UsbUtils.get_mount_usb_path());
                    g_dtv.hdd_monitor_start(Pvcfg.getPvrHddLimitSize());
                }

                g_dtv.schedule_all_timers(); // schedule all bookInfo and set alarm
                registerReceivers();
            }
        });
    }

    public static void primeDtvInitDone() {
        gHandler.post(new Runnable() {
            @Override
            public void run() {
                waitForInit();
                synchronized (mBroadcastLock) {
                    int n = mRemoteCallbacks.beginBroadcast();
                    Log.d(TAG, "[PrimeDtvServiceAIDL] primeDtvInitDone: broadcasting to " + n + " callbacks");
                    try {
                        for (int i = 0; i < n; i++) {
                            IPrimeDtvServiceCallback callback = mRemoteCallbacks.getBroadcastItem(i);
                            String connectedFrom = (String) mRemoteCallbacks.getBroadcastCookie(i);
                            if (callback != null) {
                                try {
                                    TVMessage msg = new TVMessage(TVMessage.FLAG_SYSTEM, TVMessage.TYPE_SYSTEM_INIT);
                                    callback.onMessage(msg);
                                    Log.d(TAG, "[PrimeDtvServiceAIDL] Sent TYPE_SYSTEM_INIT to: " + connectedFrom);
                                } catch (android.os.DeadObjectException e) {
                                    Log.w(TAG, "[PrimeDtvServiceAIDL] DeadObjectException for " + connectedFrom
                                            + " in primeDtvInitDone.");
                                } catch (RemoteException e) {
                                    Log.e(TAG,
                                            "[PrimeDtvServiceAIDL] Failed to invoke connectedFrom for " + connectedFrom,
                                            e);
                                }
                            }
                        }
                    } finally {
                        mRemoteCallbacks.finishBroadcast();
                    }
                }
            }
        });
    }

    public static PrimeDtv get_prime_dtv() {
        return g_dtv;
    }

    public static PrimeDtvServiceApplication getInstance() {
        Log.d(TAG, "getInstance " + gInstance);
        return gInstance;
    }

    public ServiceInterface getServiceInterface() {
        return mServiceInterface;
    }

    public void registerHandler(Handler handler) {
        Log.d(TAG, "registerHandler " + handler);
        gHandler = handler;
    }

    public void unregisterHandler(Handler handler) {
        Log.d(TAG, "unregisterHandler " + handler);
        gHandler = null;
    }

    public void add_aidl_callback(IPrimeDtvServiceCallback callback, String caller) {
        Log.d(TAG, "[PrimeDtvServiceAIDL]  add_aidl_callback IPrimeDtvServiceCallback = " + callback
                + " connectedFrom = " + caller);
        // 使用 register 的第二個參數 cookie 來儲存 caller 資訊
        mRemoteCallbacks.register(callback, caller);
        mIPrimeDtvServiceCallback = callback;
    }

    public void remove_aidl_callback(IPrimeDtvServiceCallback callback) {
        Log.d(TAG, "[PrimeDtvServiceAIDL]  remove_aidl_callback IPrimeDtvServiceCallback = " + callback);
        mRemoteCallbacks.unregister(callback);
    }

    public void broadcastMessage(TVMessage msg) {
        if (msg == null) {
            return;
        }
        synchronized (mBroadcastLock) {
            int n = mRemoteCallbacks.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    IPrimeDtvServiceCallback callback = mRemoteCallbacks.getBroadcastItem(i);
                    String connectedFrom = (String) mRemoteCallbacks.getBroadcastCookie(i);
                    if (callback != null) {
                        try {
                            callback.onMessage(msg);
                        } catch (android.os.DeadObjectException e) {
                            Log.w(TAG, "DeadObjectException for " + connectedFrom + ". Removing callback.", e);
                            mRemoteCallbacks.unregister(callback);
                        } catch (RemoteException e) {
                            Log.e(TAG, "Failed to invoke connectedFrom for " + connectedFrom, e);
                        }
                    }
                }
            } finally {
                mRemoteCallbacks.finishBroadcast();
            }
        }
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
                g_CurrentResumeActivity = activity;
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

    public static ChannelChangeManager getChannelChangeManager() {
        return mChannelChangeManager;
    }

    public void registerReceivers() {
        Log.d(TAG, "registerReceivers: ");
        registerUsbReceiver();
        registerTimerReceiver();
    }

    private void registerUsbReceiver() {
        mUsbReceiver = new UsbReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        registerReceiver(mUsbReceiver, filter, RECEIVER_EXPORTED);
    }

    private void registerTimerReceiver() {
        mTimerReceiver = new PrimeTimerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BookInfo.ACTION_TIMER_RECORD);
        filter.addAction(BookInfo.ACTION_TIMER_POWER_ON);
        filter.addAction(BookInfo.ACTION_TIMER_CHANGE_CHANNEL);
        registerReceiver(mTimerReceiver, filter, RECEIVER_EXPORTED);
    }

    public void unregisterReceivers() {
        unregisterReceiver(mUsbReceiver);
        unregisterReceiver(mTimerReceiver);
    }

    private void detectUsbStorage(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        for (StorageVolume volume : storageManager.getStorageVolumes()) {

            if (null == volume.getDirectory())
                continue;
            if (volume.isPrimary())
                continue;
            if (!volume.isRemovable())
                continue;

            String usbPath = volume.getDirectory().getAbsolutePath();
            UsbUtils.set_mount_usb_path(usbPath); // Store USB path for database
            Log.d(TAG, "detectUsbStorage: USB path detected = " + usbPath);
            break;
        }
    }
}
