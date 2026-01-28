package com.prime.dmg.launcher.Utils;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import com.prime.dtv.PrimeUsbReceiver;
import com.prime.dtv.utils.LogUtils;

import java.io.File;
import java.util.HashMap;

public class UsbUtils {

    private static final String TAG = UsbUtils.class.getSimpleName();

    public static long get_usb_total_size(Context context, Intent intent, String[] usb_label) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        long totalSize = -1;
        long availableSize;

        for (StorageVolume volume : storageManager.getStorageVolumes()) {
            Uri volumeUri = Uri.fromFile(volume.getDirectory());
            Uri intentUri = intent.getData();

            if (!volumeUri.equals(intentUri))
                continue;
            if (null == volume.getDirectory())
                continue;

            LogUtils.d("volumeUri = "+volumeUri);
            LogUtils.d("Path = "+volume.getDirectory().getPath());
            usb_label[0] = volume.getDescription(context);

            File usbFile    = new File(volume.getDirectory().getPath());
            StatFs statFs   = new StatFs(usbFile.getPath());
            totalSize       = statFs.getBlockCountLong() * statFs.getBlockSizeLong() / (1024 * 1024);
            availableSize   = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong() / (1024 * 1024);
            Log.d(TAG, "on_usb_mount: Total Size: " + totalSize + " MB");
            Log.d(TAG, "on_usb_mount: Available Size: " + availableSize + " MB");
            break;
        }

        return totalSize;
    }

    public static boolean has_usb_disk(Context context) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        return !deviceList.isEmpty() && PrimeUsbReceiver.MOUNTED;
    }

    public static boolean has_usb_enough_space() {
        boolean enoughSpace = Utils.check_usb_size();
        Log.d(TAG, "has_usb_enough_space: " + (enoughSpace ? "has enough space" : "not enough space"));
        return enoughSpace;
    }
}
