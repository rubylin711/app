package com.prime.dtvservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.utils.UsbUtils;

public class UsbReceiver extends BroadcastReceiver {
    private static final String TAG = "UsbReceiver";

    private final PrimeDtv g_PrimeDtv = PrimeDtvServiceApplication.get_prime_dtv();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            Log.e(TAG, "onReceive: null action");
            return;
        }

        Log.d(TAG, "onReceive: " + action);
        switch (action) {
            case Intent.ACTION_MEDIA_MOUNTED -> mounted(intent);
            case Intent.ACTION_MEDIA_UNMOUNTED -> unmounted(intent);
            case Intent.ACTION_MEDIA_EJECT -> eject(intent);
            default -> Log.w(TAG, "onReceive: unknown action = " + action);
        }
    }

    private boolean mounted(Intent intent) {
        String usb_path = intent.getDataString();

        Log.d(TAG, "usb_path = "+usb_path);
        if (null == usb_path || usb_path.isEmpty() || usb_path.length() < 7)
            return false;

        String usb_mount_path = UsbUtils.get_mount_usb_path();
        Log.d(TAG, "mounted: mount_usb_path = " + usb_mount_path);
        if(usb_path.contains("emulated"))
            return false;
        if (usb_mount_path != null)
            return true;
        usb_path = usb_path.substring(7);
        UsbUtils.set_mount_usb_path(usb_path);
        g_PrimeDtv.pvr_init(usb_path);
        g_PrimeDtv.hdd_monitor_start(Pvcfg.getPvrHddLimitSize());

        return true;
    }

    private void unmounted(Intent intent) {
        Log.d(TAG, "unmounted: ");
        if (!check_usb_path(intent))
            return;

        UsbUtils.unmount_usb_path();
        g_PrimeDtv.hdd_monitor_stop();
        g_PrimeDtv.pvr_deinit();
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
        Log.i(TAG, "check_usb_path: " + UsbUtils.get_mount_usb_path());
        return usb_path.equals(UsbUtils.get_mount_usb_path());
    }
}
