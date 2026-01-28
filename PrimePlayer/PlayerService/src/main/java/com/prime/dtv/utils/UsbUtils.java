package com.prime.dtv.utils;

import static com.prime.dtv.service.Util.Utils.find_disk_by_id;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.StatFs;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.util.Log;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.PrimeDtv;

import java.io.File;
import java.lang.reflect.Method;
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
        //Log.d(TAG, "check_usb_size:");
        if (MOUNTED_USB_PATH == null || MOUNTED_USB_PATH.isEmpty()) {
            Log.d(TAG, "check_usb_size: 0");
            return false;
        }

        List<Long> usbSize = get_usb_space_info();
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
        long totalSize = -1;
        long availableSize = 0;

        if(MOUNTED_USB_PATH != null) {
            StatFs statFs = new StatFs(Uri.parse(MOUNTED_USB_PATH).getPath());
            totalSize = statFs.getBlockCountLong() * statFs.getBlockSizeLong() / (1024 * 1024);
            availableSize = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong() / (1024 * 1024);
            //Log.d(TAG, "check_usb_size: Total Size: " + totalSize + " MB");
            //Log.d(TAG, "check_usb_size: Available Size: " + availableSize + " MB");
        }
        else{
            Log.w(TAG, "check_usb_size: mStatFs is null ! MOUNTED_USB_PATH is "+MOUNTED_USB_PATH);
        }

        List<Long> usbSize = new ArrayList<>();
        usbSize.add(totalSize);
        usbSize.add(availableSize);
        return usbSize;
    }

    public static String get_hdd_sys_path(Context context){
        DiskInfo diskInfo = get_current_usb_disk_info(context);
        if(diskInfo != null){
            return diskInfo.sysPath;
        }
        return null;
    }

    public static String get_usb_serial(Context context, String diskId) {
        String usb_serial = "11111111113333333333333";
        DiskInfo diskInfo = find_disk_by_id(context, diskId);
        String sysPath = diskInfo.sysPath;
        Log.d(TAG, "get_usb_speed: " + sysPath);
        //PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
        return null;//primeDtv.get_usb_speed_by_disk_sys_path(sysPath);
    }

    public static DiskInfo get_current_usb_disk_info(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        String curMountPath = MOUNTED_USB_PATH;

        if (curMountPath == null) {
            Log.w(TAG, "get_current_usb_disk_info: MOUNTED_USB_PATH is null");
            return null;
        }

        try {
            @SuppressLint("DiscouragedPrivateApi")
            Method getVolumesMethod = StorageManager.class.getDeclaredMethod("getVolumes");
            List<VolumeInfo> volumes = (List<VolumeInfo>) getVolumesMethod.invoke(storageManager);

            if (volumes == null) {
                Log.w(TAG, "get_current_usb_disk_info: no volumes");
                return null;
            }

            // find current diskinfo by current mount path
            for (VolumeInfo volume : volumes) {
                File file = volume.getPath();
                if (file != null && file.getPath().equals(curMountPath)) {
                    DiskInfo disk = volume.getDisk();
                    if (disk != null && disk.isUsb() && disk.isAdoptable()) {
                        return disk;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "get_current_usb_disk_info: fail", e);
        }

        return null;
    }
}
