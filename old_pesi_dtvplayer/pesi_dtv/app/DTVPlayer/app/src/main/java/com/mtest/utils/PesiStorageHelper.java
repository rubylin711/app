package com.mtest.utils;

import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * functions for @hide volumeInfo, @hide disInfo
 * there are no framework.jar libs in mtest anymore
 * find other solutions in the future
 */
public class PesiStorageHelper
{
    private static final String TAG = PesiStorageHelper.class.getSimpleName();
    private static final String STR_PORT_1 = "usb1";
    private static final String STR_PORT_2 = "usb3";
    private StorageManager mStorageManager;
    private Class<?> mStoreManagerClazz;
    private Class<?> mVolumeInfoClazz;
    private Class<?> mDiskInfoClazz;

    public PesiStorageHelper(StorageManager storageManager) {
        mStorageManager = storageManager;
        try {
            mStoreManagerClazz = Class.forName("android.os.storage.StorageManager");
            mVolumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            mDiskInfoClazz = Class.forName("android.os.storage.DiskInfo");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<StorageVolume> getStorageVolumes()
    {
        return mStorageManager.getStorageVolumes();
    }

    public List<Object> getVolumes() {
        List<Object> volumes = new ArrayList<>();

        try {
            Method getVolumesMethod = mStoreManagerClazz.getMethod("getVolumes");
            volumes = (List<Object>) getVolumesMethod.invoke(mStorageManager);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return volumes;
    }

    public String getFsType(Object volumeInfo) {
        String fsType = "";

        try {
            Field fsTypeField = mVolumeInfoClazz.getDeclaredField("fsType");
            fsType = (String)fsTypeField.get(volumeInfo);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return fsType;
    }

    public String getId(Object volumeInfo) {
        String id = "";

        try {
            Field idField = mVolumeInfoClazz.getDeclaredField("id");
            id = (String)idField.get(volumeInfo);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return id;
    }

    public String getPath(Object volumeInfo) {
        String path = "";

        try {
            Field pathField = mVolumeInfoClazz.getDeclaredField("path");
            path = (String)pathField.get(volumeInfo);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (path == null)
            path = "";

        return path;
    }

    public String getInternalPath(Object volumeInfo) {
        String internalPath = "";

        try {
            Field internalPathField = mVolumeInfoClazz.getDeclaredField("internalPath");
            internalPath = (String)internalPathField.get(volumeInfo);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (internalPath == null)
            internalPath = "";

        return internalPath;
    }

    public boolean isMountedReadable(Object volumeInfo) {
        boolean result = false;

        try {
            Method isMountedReadableMethod = mVolumeInfoClazz.getMethod("isMountedReadable");
            result = (boolean) isMountedReadableMethod.invoke(volumeInfo);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return result;
    }

    public Object getDisk(Object volumeInfo) {
        Object disk = null;

        try {
            Field diskField = mVolumeInfoClazz.getDeclaredField("disk");
            disk = diskField.get(volumeInfo);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return disk;
    }

    public boolean isUsb(Object volumeInfo)
    {
        Object diskInfo = getDisk(volumeInfo);
        boolean result = false;

        if (diskInfo != null)
        {
            try {
                Method isUsbMethod = mDiskInfoClazz.getMethod("isUsb");
                result = (boolean) isUsbMethod.invoke(diskInfo);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public boolean isSd(Object volumeInfo)
    {
        Object diskInfo = getDisk(volumeInfo);
        boolean result = false;

        if (diskInfo != null)
        {
            try {
                Method isSdMethod = mDiskInfoClazz.getMethod("isSd");
                result = (boolean) isSdMethod.invoke(diskInfo);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public boolean isPort1 (Object volumeInfo)
    {
        Object diskInfo = getDisk(volumeInfo);
        boolean result = false;

        if (diskInfo != null)
        {
            Log.d(TAG, "isPort1: "+diskInfo.toString());
            result = diskInfo.toString().contains(STR_PORT_1);
        }

        return result;
    }

    public boolean isPort2 (Object volumeInfo)
    {
        Object diskInfo = getDisk(volumeInfo);
        boolean result = false;

        if (diskInfo != null)
        {
            Log.d(TAG, "isPort2: "+diskInfo.toString());
            result = diskInfo.toString().contains(STR_PORT_2);
        }

        return result;
    }

    public int getUsbPortNum(Object volumeInfo) {
        // not exist in VolumeInfo
        // we modify android framework to get this before
        // need to find other solution
        return 0;
    }

    public String getDevType(Object volumeInfo) {
        // not exist in VolumeInfo
        // we modify android framework to get this before
        // need to find other solution
        return "";
    }

    public String getUsbPath(Object volumeInfo) {
        // not exist in VolumeInfo
        // we modify android framework to get this before
        // need to find other solution
        return "";
    }
}
