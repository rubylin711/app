package com.prime.dmg.launcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.prime.dmg.launcher.Hottest.HottestActivity;
import com.prime.dmg.launcher.Settings.DMGSettingsActivity;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.PrimeTimerReceiver;
import com.prime.dtv.PrimeUsbReceiver;
import com.prime.dtv.PrimeVolumeReceiver;
import com.prime.dtv.service.database.DVBDatabase;
import com.prime.dtv.utils.TVMessage;

@SuppressLint("InlinedApi")
public class HomeApplication extends Application {
    private static final String TAG = HomeApplication.class.getSimpleName() ;

    public static Activity top_activity;
    private static String gTopClsName;
    private static String gPreviousClassName;
    private static String g_CurrentPausedActivity = null ;
    private static Activity g_CurrentResumeActivity = null ;
    private static Dialog g_CurrentShowDialog = null ;
    private static boolean gIsSaveInstanceState = false;


    private PrimeTimerReceiver      g_timerReceiver;
    private PrimeUsbReceiver        g_usbReceiver;
    private PrimeVolumeReceiver     g_VolumeReceiver;
    private static PrimeDtv g_dtv;

    private static int activityCount = 0;

    // for default channel
    private static boolean gotoHottest = false;

    private BaseActivity g_lastBaseActivity;

    public static boolean isIsSaveInstanceState() {
        return gIsSaveInstanceState;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        g_dtv = new PrimeDtv(new PrimeDtv.DTVCallback() {
            @Override
            public void onMessage(TVMessage msg) {
//                Log.d(TAG, "onMessage msg = " + msg.getMessage() + " type " + msg.getMsgType() + " flag " + msg.getMsgFlag() ) ;
                if ( g_CurrentResumeActivity instanceof BaseActivity ) {
                    ((BaseActivity) g_CurrentResumeActivity).onMessage(msg);
                } else if (g_lastBaseActivity != null && is_hdd_space_msg(msg)) {
                    // send HDD msg(e.g. HDD_NO_SPACE) to last BaseActivity when
                    // 1. not in our app
                    // 2. in our activity which does not extends BaseActivity(e.g. DMGSettingActivity)
                    g_lastBaseActivity.onMessage(msg);
                }

                if ( g_CurrentShowDialog instanceof BaseDialog )
                    ((BaseDialog)g_CurrentShowDialog).onMessage(msg);
            }
        }, this);
        g_dtv.register_callbacks();

        register_receivers(this);

        register_lifecycle_callback();

        register_dialog_lifecycle_listener();

        g_dtv.backupDatabase(false);
        DVBDatabase.BackupWorker.scheduleDailyBackup(this);
    }

    public void register_dialog_lifecycle_listener(){
        BaseDialog.setDialogLifecycleListener(new BaseDialog.DialogLifecycleListener() {
            @Override
            public void onDialogShow(Dialog dialog) {
                g_CurrentShowDialog = dialog ;
            }

            @Override
            public void onDialogDismiss(Dialog dialog) {
                g_CurrentShowDialog = null ;
            }
        });
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
                gTopClsName = activity.getComponentName().getClassName();
                top_activity = activity;
                g_CurrentResumeActivity = activity ;
                activityCount++;
                gIsSaveInstanceState = false;

                if (activity instanceof BaseActivity) {
                    g_lastBaseActivity = (BaseActivity) activity;
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + " Paused");
                if (!activity.getClass().getName().equals(DMGSettingsActivity.class.getName())) {
                    g_CurrentResumeActivity = null;
                }
                gPreviousClassName = activity.getComponentName().getClassName();
                activityCount--;

                if (HottestActivity.class.getName().equals(gPreviousClassName))
                    set_goto_hottest(false);
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + " Stopped");
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                Log.d(TAG, activity.getLocalClassName() + " SaveInstanceState");
                gIsSaveInstanceState = true;
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + " Destroyed");
            }
        });
    }

    public void register_receivers(Context context) {
        Log.d(TAG, "register_receiver: ");
        register_usb_receiver();
        register_timer_receiver();
        register_volume_receiver(context);
    }

    public void unregister_receivers() {
        unregisterReceiver(g_usbReceiver);
        unregisterReceiver(g_VolumeReceiver);
        unregisterReceiver(g_timerReceiver);
    }

    private void register_timer_receiver() {
        g_timerReceiver = new PrimeTimerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PrimeTimerReceiver.ACTION_TIMER_RECORD);
        filter.addAction(PrimeTimerReceiver.ACTION_TIMER_POWER_ON);
        filter.addAction(PrimeTimerReceiver.ACTION_TIMER_CHANGE_CHANNEL);
        registerReceiver(g_timerReceiver, filter, RECEIVER_EXPORTED);
    }

    public void register_usb_receiver() {
        g_usbReceiver       = new PrimeUsbReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PrimeUsbReceiver.ACTION_MEDIA_MOUNTED);
        filter.addAction(PrimeUsbReceiver.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(PrimeUsbReceiver.ACTION_MEDIA_SCANNER_FINISHED);
        filter.addAction(PrimeUsbReceiver.ACTION_MEDIA_SCANNER_STARTED);
        filter.addAction(PrimeUsbReceiver.ACTION_MEDIA_SCANNER_SCAN_FILE);
        filter.addAction(PrimeUsbReceiver.ACTION_MEDIA_CHECKING);
        filter.addAction(PrimeUsbReceiver.ACTION_MEDIA_EJECT);
        filter.addAction(PrimeUsbReceiver.ACTION_MEDIA_REMOVED);
        filter.addAction(PrimeUsbReceiver.ACTION_MEDIA_SHARED);
        filter.addAction(PrimeUsbReceiver.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(PrimeUsbReceiver.ACTION_MEDIA_UNSHARED);
        filter.addDataScheme("file");
        registerReceiver(g_usbReceiver, filter, RECEIVER_EXPORTED);
    }

    public void register_volume_receiver(Context context) {
        g_VolumeReceiver    = new PrimeVolumeReceiver(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(PrimeVolumeReceiver.ACTION_VOLUME_CHANGED);
        filter.addAction(PrimeVolumeReceiver.ACTION_STREAM_MUTE_CHANGED);
        filter.addAction(PrimeVolumeReceiver.ACTION_KEYCODE_VOLUME_DOWN);
        filter.addAction(PrimeVolumeReceiver.ACTION_KEYCODE_VOLUME_MUTE);
        registerReceiver(g_VolumeReceiver, filter, RECEIVER_EXPORTED);
    }

    public static PrimeDtv get_prime_dtv() {
        return g_dtv;
    }

    public static String get_top_class_name() {
        return gTopClsName;
    }

    public static String get_previous_class_name() {
        return gPreviousClassName;
    }

    public static int get_activity_count() {
        return activityCount;
    }

    public static boolean is_goto_hottest() {
        return gotoHottest;
    }

    public static void set_goto_hottest(boolean status) {
        gotoHottest = status;
    }

    /** @noinspection EnhancedSwitchMigration, DataFlowIssue */
    public static void onMessage(Context context, Intent intent) {
        String action = intent.getAction();
        int what = 0;

        switch (action) {
            case PrimeVolumeReceiver.ACTION_VOLUME_CHANGED:
            case PrimeVolumeReceiver.ACTION_STREAM_MUTE_CHANGED:
            case PrimeVolumeReceiver.ACTION_KEYCODE_VOLUME_DOWN:
                what = HomeActivity.MSG_VOLUME_CHANGED;
                break;
        }

        if (top_activity instanceof BaseActivity baseActivity) {
            baseActivity.onBroadcastMessage(context, intent);
            BaseActivity.send_message(what);
        }
        else Log.i(TAG, "onMessage: top_activity != BaseActivity");
    }

    public static Activity get_current_activity() {
        return g_CurrentResumeActivity;
    }

    private boolean is_hdd_space_msg(TVMessage msg) {
        int type = msg == null ? 0 : msg.getMsgType();
        return type == TVMessage.TYPE_HDD_NO_SPACE
                || type == TVMessage.TYPE_PVR_REC_DISK_FULL;
    }
}

