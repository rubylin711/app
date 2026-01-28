package com.prime.otaupdater;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.face.IFaceService;
import android.os.Build;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;


public class MountReceiver extends BroadcastReceiver {
    public static final String TAG = "MountReceiver" ;

    public static final String ACTION_VOLUME_STATE_CHANGED  = "android.os.storage.action.VOLUME_STATE_CHANGED";
    public static final String EXTRA_VOLUME_STATE           = "android.os.storage.extra.VOLUME_STATE";
    public static final int STATE_MOUNTED       = 2;
    public static final int STATE_EJECTING      = 5;
    public static final int STATE_BAD_REMOVAL   = 8;

    public static final String PATH_STORAGE             = "/storage/" ;
    public static final String PESI_FORCE_OTA           = "/Pesi_Force_Ota" ;
    public static final String PESI_OTA                 = "/Pesi_Ota" ;
    public static final String OTA_PAYLOAD_BIN          = "payload.bin" ;
    public static final String OTA_PAYLOAD_PROP         = "payload_properties.txt" ;
    public static final String OTA_METADATA             = "metadata" ;
    public static final String OTA_ZIP_FILE             = "ota_package.zip" ;
    public static final String RECOVERY_ZIP_FILE     = "recovery_package.zip" ;
    public static final String USER_SETUP_COMPLETE = "user_setup_complete";
    public static final String TV_USER_SETUP_COMPLETE = "tv_user_setup_complete";
    public static final Handler mHandler = new Handler();
    public StorageManager mStorageManager;
    public static String mSrcDir = null;

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceive(Context context, Intent intent) {
        int state = -1;
        mStorageManager = context.getSystemService(StorageManager.class);

        String action = intent.getAction();
        String path = intent.getData().getPath();

        Log.d(TAG, "onReceive: action = " + action + ", path = " + path);
        if (path.equals("/storage/emulated/0")) {
            Log.e(TAG, "onReceive: is sdcard = " + path);
            return;
        }

        if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            String otaPath = getOtaPath(path);
            mSrcDir = otaPath;
            broadcastStartUI(context, otaPath);
        }
        else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
            Log.d(TAG, "onReceive: mSrcDir = " + mSrcDir);
            broadcastStopOTA(context); //stopOTA(context);
        }

        /*if (ACTION_VOLUME_STATE_CHANGED.equals(intent.getAction())) {
            state = intent.getIntExtra(EXTRA_VOLUME_STATE, -1);

            Log.d(TAG, "action = " + intent.getAction() + " , state = " + state);
            if (STATE_MOUNTED == state) {
                String otaPath = getOtaPath();
                broadcastStartUI(context, otaPath);
            }
            else if (STATE_EJECTING == state || STATE_BAD_REMOVAL == state) {
                stopOTA(context);
            }
        }*/
    }

    /*public String getUuid() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            for (StorageVolume vol : mStorageManager.getStorageVolumes()) {
                String volName = vol.getMediaStoreVolumeName();
                String volUuid = vol.getUuid();

                if (volName == null || volUuid == null)
                    continue;
                if (volName.equals("external_primary"))
                    continue;

                //Log.d(TAG, "getUuid: volUuid = " + volUuid);
                return vol.getUuid();
            }
        }
        return null;
    }*/

    public String getOtaPath(String UsbPath) {
        //String UsbPath = PATH_STORAGE + getUuid();
        String ForceOtaPath = UsbPath + PESI_FORCE_OTA;
        String OtaPath = UsbPath + PESI_OTA;
        String path = null;

        Log.d(TAG, "getOtaPath: UsbPath      = " + UsbPath);
        Log.d(TAG, "getOtaPath: ForceOtaPath = " + ForceOtaPath);
        Log.d(TAG, "getOtaPath: OtaPath      = " + OtaPath);

        if (new File(OtaPath).exists())
            path = OtaPath;
        else
            Log.e(TAG, "getOtaPath: " + OtaPath + " not found");

        if (new File(ForceOtaPath).exists())
            path = ForceOtaPath;
        else
            Log.e(TAG, "getOtaPath: " + ForceOtaPath + " not found");

        return path;
    }

    public String getFilePath(String srcDir, String fileName) {
        String otaDataPath = null;
        File otaData = new File(srcDir, fileName);

        if (otaData.exists()) {
            otaDataPath = otaData.getAbsolutePath();
            //Log.d(TAG, "found " + fileName);
        }
        else
            Log.e(TAG, fileName + " not found");

        return otaDataPath;
    }

    public String getOtaZip(String srcDir) {
        //final String otaZipFilter = "(.*)ota(.*).zip";
        String otaZipFilter = SystemProperties.get(MainActivity.PROPERTY_OTA_UPDATE_NAME, "aosp_usb.zip").toLowerCase(); // OTA zip file 使用固定檔名
        File srcFolder = new File(srcDir);
        File[] files = srcFolder.listFiles();
        String retPath = null;

        if (files == null) {
            Log.e(TAG, "getOtaZip: Empty Pesi_Ota / Pesi_Force_Ota");
            return null;
        }

        for (File f : files) {
            if (f.getName().toLowerCase().matches(otaZipFilter)) {
                retPath = f.getPath();
                break;
            }
        }

        if (retPath == null) {
            Log.e(TAG, "getOtaZip: " + otaZipFilter + " not found");
        }
        return retPath;
    }

    public void broadcastStartUI(Context context, String otaPath) {
        //String PAYLOAD_BIN  = getFilePath(otaPath, OTA_PAYLOAD_BIN);
        //String PAYLOAD_PROP = getFilePath(otaPath, OTA_PAYLOAD_PROP);
        //String METADATA     = getFilePath(otaPath, OTA_METADATA);
        String ZIP_FILE_OTA         = getFilePath(otaPath, OTA_ZIP_FILE);
        String ZIP_FILE_RECOVERY    = getFilePath(otaPath, RECOVERY_ZIP_FILE);

        if (ZIP_FILE_OTA == null && ZIP_FILE_RECOVERY == null) {
            Log.e(TAG, "[Error] ZIP_FILE Path is null");
            return;
        }
        //Log.d(TAG, "broadcastStartUI: PAYLOAD_BIN   = " + PAYLOAD_BIN);
        //Log.d(TAG, "broadcastStartUI: PAYLOAD_PROP  = " + PAYLOAD_PROP);
        //Log.d(TAG, "broadcastStartUI: METADATA      = " + METADATA);
        Log.d(TAG, "broadcastStartUI: ZIP_FILE_OTA      = " + ZIP_FILE_OTA);
        Log.d(TAG, "broadcastStartUI: ZIP_FILE          = " + ZIP_FILE_RECOVERY);

        if (!setupCompleted(TAG, context)) {
            Log.e(TAG, "[Error] SetupWraith is not completed");
            return;
        }
        //if ((PAYLOAD_BIN == null) && (PAYLOAD_PROP == null) && (METADATA == null) && ZIP_FILE == null) {
        //    Log.e(TAG, "[Error] file not found");
        //    return;
        //}
        /*if (isRunningTask(context)) {
            Log.e(TAG, "[Error] already open OTA UI");
            return;
        }*/

        Intent i = new Intent(Intent.ACTION_RUN);
        i.setClass(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(MainActivity.EXTRA_OTA_PATH,      otaPath);
        //i.putExtra(MainActivity.EXTRA_PAYLOAD_BIN,   PAYLOAD_BIN);
        //i.putExtra(MainActivity.EXTRA_PAYLOAD_PROP,  PAYLOAD_PROP);
        //i.putExtra(MainActivity.EXTRA_METADATA,      METADATA);
        i.putExtra(MainActivity.EXTRA_ZIP_FILE_OTA,     ZIP_FILE_OTA);
        i.putExtra(MainActivity.EXTRA_ZIP_FILE_RECOVERY,ZIP_FILE_RECOVERY);
        context.startActivity(i);
        Log.d(TAG, "broadcastStartUI: startActivity");
    }

    public static boolean setupCompleted(String TAG, Context context) {
        ContentResolver r = context.getContentResolver();

        Log.d(TAG, "setupCompleted: USER_SETUP_COMPLETE    = " + Settings.Secure.getInt(r, USER_SETUP_COMPLETE, 0));
        Log.d(TAG, "setupCompleted: TV_USER_SETUP_COMPLETE = " + Settings.Secure.getInt(r, TV_USER_SETUP_COMPLETE, 0));

        return  ( (1 == Settings.Secure.getInt(r, USER_SETUP_COMPLETE, 0)) &&
                  (1 == Settings.Secure.getInt(r, TV_USER_SETUP_COMPLETE, 0)) );
    }

    /*public boolean isRunningTask(Context context) {
        ActivityManager actyManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = actyManager.getRunningTasks(Integer.MAX_VALUE);
        for (ActivityManager.RunningTaskInfo task : runningTasks)
        {
            String clsName = task.baseActivity.getClassName();

            Log.d(TAG, "isRunningTask: clsName   = " + clsName);
            if (clsName.equals( context.getPackageName() + ".MainActivity" ))
            {
                Log.d(TAG, "isRunningTask: equal pkg = " + context.getPackageName() + ".MainActivity");
                return true;
            }
        }
        return false;
    }*/

    /*public void stopOTA(Context context) {
        // stop Update Engine
        broadcastStopOTA(context);
        // close OTA activity
        //closeMainActivity(context);
    }*/

    public void broadcastStopOTA(Context context) {
        Log.d(TAG, "broadcastStopOTA: stop Update Engine");
        Intent i = new Intent(MainActivity.ABUPDATE_BROADCAST_STOP);
        i.setPackage(MainActivity.OTA_SERVICE_PACKAGE_NAME);
        //i.setFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        context.sendBroadcast(i);
    }

    /*public void broadcastStartOTA(Context context) {
        Log.d(TAG, "broadcastStartOTA: START , mSrcDir = " + mSrcDir);
        //Log.d(TAG, "broadcastStartOTA: START , property = " + payloadProp);
        Intent intent = new Intent(ABUPDATE_BROADCAST_START);
        intent.setPackage(OTA_SERVICE_PACKAGE_NAME);
        intent.putExtra(ABUPDATE_BROADCAST_UPDATE_BIN_URL,     mSrcDir + "/" + OTA_PAYLOAD_BIN);
        intent.putExtra(ABUPDATE_BROADCAST_UPDATE_PARAM_URL,   mSrcDir + "/" + OTA_PAYLOAD_PROP);
        intent.putExtra(ABUPDATE_BROADCAST_UPDATE_MODE,        ABUPDATE_USB_MODE);
        context.sendBroadcast(intent);
    }*/

    /*public void closeMainActivity(Context context) {
        Log.d(TAG, "close OTA activity");
        Intent i = new Intent(MainActivity.ACTION_CLOSE_OTA_UI);
        i.setPackage(context.getPackageName());
        //i.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        context.sendBroadcast(i);
    }*/
}
