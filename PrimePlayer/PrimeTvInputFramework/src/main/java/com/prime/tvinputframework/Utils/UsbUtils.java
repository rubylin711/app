package com.prime.tvinputframework.Utils;

import android.content.Context;
import android.net.Uri;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class UsbUtils {
    private static final String TAG = "Utils";
    private static String MOUNTED_USB_PATH = null;

    public static String get_mount_usb_path() {
        return MOUNTED_USB_PATH;
    }

    public static void unmount_usb_path() {
        MOUNTED_USB_PATH = null;
    }

    public static void set_mount_usb_path(String usbPath) {
        LogUtils.d("set_mount_usb_path: "+ usbPath);
        MOUNTED_USB_PATH = usbPath;
    }

    public static boolean check_usb_size() {
        return check_usb_size(MOUNTED_USB_PATH);
    }

    public static boolean check_usb_size(String usbMountPath) {
        //Log.d(TAG, "check_usb_size:");
        if (usbMountPath == null || usbMountPath.isEmpty()) {
            Log.d(TAG, "check_usb_size: 0");
            return false;
        }

        List<Long> usbSize = get_usb_space_info(usbMountPath);
        long totalSize = usbSize.get(0);
        long availableSize = usbSize.get(1);

        if (totalSize < 0) {
            Log.w(TAG, "check_usb_size: no usb disk");
            return false;
        }

        if (totalSize < Pvcfg.getPvrHddTotalSizeLimit()) {
            Log.w(TAG, "check_usb_size: total size less than " + Pvcfg.getPvrHddTotalSizeLimit()/1000+"g");
            return false;
        }

        if (availableSize < Pvcfg.getPvrHddLimitSize()) {
            Log.w(TAG, "check_usb_size: not enough available space");
            return false;
        }

        return true;
    }

    public static List<Long> get_usb_space_info() {
        return get_usb_space_info(MOUNTED_USB_PATH);
    }

    public static List<Long> get_usb_space_info(String usbMountPath) {
        long totalSize = -1;
        long availableSize = 0;

        if(usbMountPath != null) {
            StatFs statFs = new StatFs(Uri.parse(usbMountPath).getPath());
            totalSize = statFs.getBlockCountLong() * statFs.getBlockSizeLong() / (1024 * 1024);
            availableSize = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong() / (1024 * 1024);
            //Log.d(TAG, "check_usb_size: Total Size: " + totalSize + " MB");
            //Log.d(TAG, "check_usb_size: Available Size: " + availableSize + " MB");
        }
        else{
            Log.w(TAG, "check_usb_size: usbMountPath is null");
        }

        List<Long> usbSize = new ArrayList<>();
        usbSize.add(totalSize);
        usbSize.add(availableSize);
        return usbSize;
    }

    public static void detect_usb_storage(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        for (StorageVolume volume : storageManager.getStorageVolumes()) {

            if (null == volume.getDirectory())
                continue;
            if (volume.isPrimary())
                continue;
            if (!volume.isRemovable())
                continue;

            String usbPath = volume.getDirectory().getAbsolutePath();
            set_mount_usb_path(usbPath); // Store USB path for database
            Log.d(TAG, "detect_usb_storage: USB path detected = " + usbPath);
            break;
        }
    }
}
