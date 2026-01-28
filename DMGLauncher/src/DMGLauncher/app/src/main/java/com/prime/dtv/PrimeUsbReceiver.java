package com.prime.dtv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.Utils.Utils;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.utils.TVMessage;

public class PrimeUsbReceiver extends BroadcastReceiver implements PrimeDtv.DTVCallback{

    String TAG = PrimeUsbReceiver.class.getSimpleName();

    public static final String ACTION_MEDIA_MOUNTED     = Intent.ACTION_MEDIA_MOUNTED;
    public static final String ACTION_MEDIA_UNMOUNTED   = Intent.ACTION_MEDIA_UNMOUNTED;
    public static final String ACTION_MEDIA_EJECT       = Intent.ACTION_MEDIA_EJECT;
    public static final String ACTION_MEDIA_REMOVED     = Intent.ACTION_MEDIA_REMOVED;
    public static final String ACTION_MEDIA_SHARED      = Intent.ACTION_MEDIA_SHARED;
    public static final String ACTION_MEDIA_BAD_REMOVAL = Intent.ACTION_MEDIA_BAD_REMOVAL;
    public static final String ACTION_MEDIA_CHECKING    = Intent.ACTION_MEDIA_CHECKING;
    public static final String ACTION_MEDIA_UNSHARED    = "android.intent.action.MEDIA_UNSHARED";
    public static final String ACTION_MEDIA_SCANNER_FINISHED    = Intent.ACTION_MEDIA_SCANNER_FINISHED;
    public static final String ACTION_MEDIA_SCANNER_STARTED     = Intent.ACTION_MEDIA_SCANNER_STARTED;
    public static final String ACTION_MEDIA_SCANNER_SCAN_FILE   = Intent.ACTION_MEDIA_SCANNER_SCAN_FILE;

    public static boolean MOUNTED = false;

    private final PrimeDtv g_PrimeDtv = HomeApplication.get_prime_dtv();

    @Override
    public void onMessage(TVMessage msg) {

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // check null action
        String action = intent.getAction();
        if (null == action)
            action = "NULL";

        Log.d(TAG, "onReceive: " + action);
        switch (action) {
            case ACTION_MEDIA_MOUNTED:
                if(mounted(intent) == false){
                    return;
                }
                break;
            case ACTION_MEDIA_UNMOUNTED:
                unmounted(intent);
                break;
            case ACTION_MEDIA_EJECT:
                eject(intent);
                break;
            case ACTION_MEDIA_REMOVED:
            case ACTION_MEDIA_SHARED:
            case ACTION_MEDIA_BAD_REMOVAL:
            case ACTION_MEDIA_CHECKING:
            case ACTION_MEDIA_UNSHARED:
            case ACTION_MEDIA_SCANNER_FINISHED:
            case ACTION_MEDIA_SCANNER_STARTED:
            case ACTION_MEDIA_SCANNER_SCAN_FILE:
                break;
            default:
                Log.e(TAG, "onReceive: unknown action = " + action);
                break;
        }
        HomeApplication.onMessage(context, intent);
    }

    private boolean mounted(Intent intent) {
        String usb_path = intent.getDataString();

        Log.d(TAG, "usb_path = "+usb_path);
        if (null == usb_path || usb_path.isEmpty() || usb_path.length() < 7)
            return false;

        String usb_mount_path = Utils.get_mount_usb_path();
        Log.d(TAG, "mounted: mount_usb_path = " + usb_mount_path);
        if(usb_path.contains("emulated"))
            return false;
        if (usb_mount_path != null)
            return true;
        usb_path = usb_path.substring(7);
        Utils.set_mount_usb_path(usb_path);
        g_PrimeDtv.hdd_monitor_start(Pvcfg.getPvrHddLimitSize());
        g_PrimeDtv.pvr_init(usb_path);
        MOUNTED = true;
        return true;
    }

    private void unmounted(Intent intent) {
        Log.d(TAG, "unmounted: ");
        if (!check_usb_path(intent))
            return;

        Utils.unmount_usb_path();
        g_PrimeDtv.hdd_monitor_stop();
        g_PrimeDtv.pvr_deinit();
        MOUNTED = false;
    }

    private void eject(Intent intent) {
        if (!check_usb_path(intent))
            return;

        g_PrimeDtv.pvr_deinit();
        g_PrimeDtv.hdd_monitor_stop();
    }

    private boolean check_usb_path(Intent intent) {
        String usb_path = intent.getDataString();

        if (null == usb_path || usb_path.isEmpty() || usb_path.length() < 7)
            return false;

        usb_path = usb_path.substring(7);
        Log.i(TAG, "check_usb_path: " + Utils.get_mount_usb_path());
        return usb_path.equals(Utils.get_mount_usb_path());
    }
}
