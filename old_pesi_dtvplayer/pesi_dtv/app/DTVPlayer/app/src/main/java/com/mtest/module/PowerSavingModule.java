package com.mtest.module;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.dolphin.dtv.HiDtvMediaPlayer;
import com.mtest.config.MtestConfig;
import com.prime.dtvplayer.Activity.DTVActivity;

import java.lang.ref.WeakReference;

public class PowerSavingModule
{
    private static final String TAG = PowerSavingModule.class.getSimpleName();
    private static final int CMD_SUCCESS = HiDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS;
    private static final int CMD_FAIL = HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    private static final int RESULT_PASS = MtestConfig.TEST_RESULT_PASS;
    private static final int RESULT_FAIL = MtestConfig.TEST_RESULT_FAIL;
    private final WeakReference<Context> mContRef;

    // shutdown
    static final String ACTION_REQUEST_SHUTDOWN = "com.android.internal.intent.action.REQUEST_SHUTDOWN";
    static final String EXTRA_KEY_CONFIRM = "android.intent.extra.KEY_CONFIRM";
    static final String EXTRA_USER_REQUESTED_SHUTDOWN = "android.intent.extra.USER_REQUESTED_SHUTDOWN";
    static final int GO_TO_SLEEP_REASON_APPLICATION = 0;

    // wakeup mode
    // used when power saving by service
    static final int PESI_WAKEUP_IR = 0;
    static final int PESI_WAKEUP_GPIO = 1;


    // wakeup flag
    // used when power saving by android
    public static final String POWER_SAVING_FLAG_STRING = "key_power_saving";//jackie mod
    static final String POWER_SAVING_FLAG_ENABLE = "enable";

    public PowerSavingModule(Context context)
    {
        mContRef = new WeakReference<>(context);
    }

    private static int getReturn(int ret)
    {
        return (ret == CMD_SUCCESS) ?
                RESULT_PASS :
                RESULT_FAIL ;
    }

    private static int getReturn(boolean success)
    {
        return success ?
                RESULT_PASS :
                RESULT_FAIL ;
    }

    private void shutdown()
    {
        Log.d(TAG, "shutdown: ");
        Intent shutdown = new Intent(ACTION_REQUEST_SHUTDOWN);
        shutdown.putExtra(EXTRA_KEY_CONFIRM, false);
        shutdown.putExtra(EXTRA_USER_REQUESTED_SHUTDOWN, true);
        shutdown.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContRef.get().startActivity(shutdown);
    }

//    /**
//     * Forces the device to go to sleep.
//     *
//     * Overrides all the wake locks that are held.
//     * This is what happens when the power key is pressed to turn off the screen.
//     *
//     * Requires the {android.Manifest.permission#DEVICE_POWER} permission.
//     * Requires signature permission.
//     *
//     * @param time The time when the request to go to sleep was issued, in the
//     * {@link SystemClock#uptimeMillis()} time base.  This timestamp is used to correctly
//     * order the go to sleep request with other power management functions.  It should be set
//     * to the timestamp of the input event that caused the request to go to sleep.
//     * @param reason The reason the device is going to sleep.
//     * @param flags Optional flags to apply when going to sleep.
//     * edwin 20201217 add IPowerManager function
//     */
//    public void goToSleep(long time, int reason, int flags) // edwin 20201217 add IPowerManager function
//    {
//        Log.d(TAG, "standby: goToSleep by IPowerManager");
//        IPowerManager iPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
//        try
//        {
//            iPowerManager.goToSleep(time, reason, flags);
//        }
//        catch (RemoteException e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Turn off the device.
//     *
//     * @param confirm If true, shows a shutdown confirmation dialog.
//     * @param reason code to pass to android_reboot() (e.g. "userrequested"), or null.
//     * @param wait If true, this call waits for the shutdown to complete and does not return.
//     * edwin 20201217 add IPowerManager function
//     */
//    public void turnOff(boolean confirm, String reason, boolean wait)
//    {
//        Log.d(TAG, "turnOff: by IPowerManager");
//        IPowerManager iPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
//        try
//        {
//            iPowerManager.shutdown(confirm, reason, wait);
//        }
//        catch (RemoteException e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    private int getWakeUpMode()
//    {
//        Log.d(TAG, "getWakeUpMode: ");
//        DTVActivity activity = (DTVActivity) mContRef.get();
//        return activity.MtestGetWakeUpMode();
//    }

    /**
     * the way to go power saving
     * may differ with different model
     */
    public void powerSave()
    {
        Log.d(TAG, "powerSave: ");

        // go power saving by service
        /*DTVActivity activity = (DTVActivity) mContRef.get();
        int ret = activity.MtestPowerSave();
        Log.d(TAG, "powerSave: MtestPowerSave ret = " + ret);*/

        // go power saving by android (save a flag and shutdown)
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(mContRef.get());
        pesiSharedPreference.putString(POWER_SAVING_FLAG_STRING, POWER_SAVING_FLAG_ENABLE);
        pesiSharedPreference.save();
        shutdown();
        //goToSleep(0, GO_TO_SLEEP_REASON_APPLICATION, 0);
        //turnOff(false, "0" /*SHUTDOWN_REASON_UNKNOWN*/, true);
    }

    /**
     * the way we check power saving
     * may differ with different model
     * @return {@link MtestConfig#TEST_RESULT_PASS} or {@link MtestConfig#TEST_RESULT_PASS}
     */
    public int checkPowerSaving()
    {
        Log.d(TAG, "checkPowerSaving: ");

        // check power saving by service -s
        /*int ret = getWakeUpMode();

        if (ret == PESI_WAKEUP_GPIO || ret == PESI_WAKEUP_IR) {
            return MtestConfig.TEST_RESULT_PASS;
        }*/
        // check power saving by service -e

        // check power saving by android (check the saved flag) -s
        // if power saving fails, it will still return PASS next time you reboot
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(mContRef.get());
        String flag = pesiSharedPreference.getString(POWER_SAVING_FLAG_STRING, "disable");
        if (flag.equals(POWER_SAVING_FLAG_ENABLE)) {
            // remove the flag to reset power saving test
            pesiSharedPreference.remove(POWER_SAVING_FLAG_STRING);
            pesiSharedPreference.save();

            return MtestConfig.TEST_RESULT_PASS;
        }
        // check power saving by android (check the saved flag) -e

        // return NONE
        // only show PASS or NONE in power saving test result
        return MtestConfig.TEST_RESULT_NONE;
    }
}
