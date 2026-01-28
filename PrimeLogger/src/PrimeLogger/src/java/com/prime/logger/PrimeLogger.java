package com.prime.logger;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.PowerManager;
import android.os.storage.VolumeInfo;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Handler;
import android.os.HwBinder;
import android.os.storage.StorageEventListener;
import android.os.FileUtils;
import android.os.RecoverySystem;
import android.os.SystemProperties;
import android.provider.DeviceConfig;
import android.provider.DeviceConfig.Properties;
import android.view.KeyEvent;
import android.widget.Toast;
import android.provider.Settings;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Calendar;
import java.util.List;
import java.util.logging.LogManager;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

// import vendor.prime.hardware.misc.V1_0.IMisc;

public class PrimeLogger extends Service
{
    public final String TAG = "PrimeLogger" ;
    private static final File RECOVERY_DIR = new File("/cache/recovery");
    private static final File LOG_FILE = new File(RECOVERY_DIR, "log");
    private static final int LOG_FILE_MAX_LENGTH = 64 * 1024;
    private static final long MAX_FILE_SIZE = 500L * 1024 * 1024; // 500MB
    public static final String PROPERTY_RUNNING = "com.prime.logger.running";
    public static final String PROPERTY_GREP = "persist.sys.prime.log.grep";
    public static final String LOGCAT_TMP = "/sdcard/logcatToTmp.log";
    public static final long LOGCAT_TIMEOUT = 30000;
    public static final int MAX_BYTE = 800*1024;
    public static final boolean DEBUG = true;
    public static boolean MOUNTED = false;

    // private IMisc hidl_IMisc = null ;
    public Context mContext = null ;
    public final Handler mHandler = new Handler();
    public static StorageManager mStorageManager;
    public static DropBoxManager mDropboxManager;
    public BroadcastReceiver mReceiver;
    public Thread mThread = null;
    public SharedPreferences mSharedPreference;
    public SharedPreferences.Editor mEditor;

    public String mUsbPath = null;
    public String mDateTime = null;
    public String mLastLog = null;
    public String mLastLogTime = null;
    public int mLogcatTimeout = 0;

    /*
    private void getHidlService()
    {
        try {
            hidl_IMisc = IMisc.getService(true);
        } catch (RemoteException e) {
            Log.i(TAG,"Can't find vendor.prime.hardware.misc.V1_0.IMisc");
        }
    }

    void linkDeathNotify()
    {
        try {
            hidl_IMisc.linkToDeath(recipient, 1481); // cookie
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    void unlinkDeatNotify()
    {
        try {
            hidl_IMisc.unlinkToDeath(recipient);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    final DeathRecipient recipient = new DeathRecipient() ;
    final class DeathRecipient implements HwBinder.DeathRecipient {
        @Override
        public void serviceDied(long cookie) {
            Log.d( TAG, "DeathRecipient cookie = " + cookie ) ;
            getHidlService() ;
            linkDeathNotify();
        }
    }
    */

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        mContext = this ;
        // getHidlService() ;
        // linkDeathNotify() ;

        // register receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(VolumeInfo.ACTION_VOLUME_STATE_CHANGED);
        mReceiver = ServiceBroadcastReceiver();
        registerReceiver(mReceiver, intentFilter);

        // get service
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        mDropboxManager = (DropBoxManager) getSystemService(Context.DROPBOX_SERVICE);

        // enable adb
        Settings.Global.putInt(getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
        Settings.Global.putInt(getContentResolver(), Settings.Global.ADB_ENABLED, 1);

        // register
        mStorageManager.registerListener(storageListener());

        // preference
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = mSharedPreference.edit();

        super.onCreate();
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        handleUnmount();
        // unlinkDeatNotify() ;
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public StorageEventListener storageListener()
    {
        return new StorageEventListener() {
            @Override
            public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
                int type = vol.getType();

                // TYPE_PUBLIC = 0
                // STATE_UNMOUNTED = 0
                // STATE_MOUNTED   = 2
                // STATE_EJECTING  = 5

                if (DEBUG)
                    Log.d(TAG, "onVolumeStateChanged: type = " + type + ", oldState = " + oldState + ", newState = " + newState);

                if ( type != VolumeInfo.TYPE_PUBLIC )
                    return;

                MOUNTED = (VolumeInfo.STATE_MOUNTED == newState);
            }
        };
    }

    public BroadcastReceiver ServiceBroadcastReceiver()
    {
        return new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction() ;
                int state = intent.getIntExtra(VolumeInfo.EXTRA_VOLUME_STATE, -1);

                if (DEBUG)
                {
                    Log.d(TAG, "onReceive: action = " + action);
                    Log.d(TAG, "onReceive: state  = " + state + ", UNMOUNTED(0), MOUNTED(2)");
                    // Log.d(TAG, "onReceive: STATE_MOUNTED   = " + VolumeInfo.STATE_MOUNTED);
                    // Log.d(TAG, "onReceive: STATE_UNMOUNTED = " + VolumeInfo.STATE_UNMOUNTED);
                }

                if (action == VolumeInfo.ACTION_VOLUME_STATE_CHANGED)
                {
                    switch(state)
                    {
                        case VolumeInfo.STATE_MOUNTED:
                            if (hasUsbDisk())
                                handleMount();
                            break;

                        case VolumeInfo.STATE_UNMOUNTED:
                            if (hasUsbDisk() == false)
                                handleUnmount();
                            break;
                    }
                }
            }
        };
    }

    private void handleMount()
    {
        String running = System.getProperty(PROPERTY_RUNNING);
        Log.d(TAG, "handleMount: running = " + running);

        if (running == "0" || running == null)
        {
            System.setProperty(PROPERTY_RUNNING, "1");
            mDateTime = getLogIndex() + "_" + getDateTime();
            mHandler.post(copyAllLog());
        }
    }

    private void handleUnmount()
    {
        Log.d(TAG, "handleUnmount: clear");
        mUsbPath = null;
        System.setProperty(PROPERTY_RUNNING, "0");
        mHandler.removeCallbacksAndMessages(null);
    }

    private boolean hasUsbDisk()
    {
        //String volumeId = intent.getStringExtra(VolumeInfo.EXTRA_VOLUME_ID);
        //StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        List<VolumeInfo> volumeInfos = mStorageManager.getVolumes();
        boolean hasUsbDisk = false;

        for (VolumeInfo info : volumeInfos)
        {
            if (DEBUG)
                Log.d(TAG, "hasUsbDisk: volume type = " + info.getType() + ", TYPE_PUBLIC(0)");

            if (info.getType() == VolumeInfo.TYPE_PUBLIC)
            {
                if (DEBUG)
                    Log.d(TAG, "hasUsbDisk: volume info = " + info.toString());
                hasUsbDisk = true;
            }
        }

        return hasUsbDisk;
    }

    public String getLogIndex() {
        int tmpIndex = 0;
        String logIndex = "";

        // length to 4
        tmpIndex = mSharedPreference.getInt("key_log_index", 0);
        logIndex = String.valueOf(tmpIndex);
        while (logIndex.length() < 4) {
            logIndex = "0" + logIndex;
        }

        // hold index
        mEditor.putInt("key_log_index", ++tmpIndex);
        if (mEditor.commit()) {
            try {
                Process process = Runtime.getRuntime().exec("/system/bin/sync");
                process.waitFor();
            }
            catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return logIndex;
    }

    // boot to write logcat tmp
    /*private void logcatToTmp()
    {
        Log.d(TAG, "logcatToTmp: LOGCAT_TMP = " + LOGCAT_TMP);
        try {
            Process process = Runtime.getRuntime().exec("logcat");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new FileWriter(LOGCAT_TMP, true));
            String line = "";
            //long timeMs = System.currentTimeMillis();

            // write log
            while ((line = bufferedReader.readLine()) != null)
            {
                line += "\n";
                writer.write(line);
                writer.flush();
            }

            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "logcatToTmp: finish");
    }*/

    // copy all logs
    private Runnable copyAllLog()
    {
        return new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "date & time = " + mDateTime);

            // user system
            String path_UserSystem = getTimePath(getUserSystemPath(), mDateTime);
            if (path_UserSystem != null)
            {
                // copy dropbox
                copyDropbox(path_UserSystem + "/dropbox.log");
                // copy tombstone
                exec("cp -rf /data/tombstones/ " + path_UserSystem);
            }

            // device info
            String path_DeviceInfo = getTimePath(getDeviceInfoPath(), mDateTime);
            if (path_DeviceInfo != null)
            {
                // property
                copyProperty(path_DeviceInfo + "/property.log");

                // recovery
                copyRecovery(path_DeviceInfo);
                //exec("cp -rf /cache/recovery " + path_DeviceInfo);
                //exec("/system/bin/toybox cp -rf /data/cache/recovery " + path_DeviceInfo);

                // network interface
                copyNetworkInterface(path_DeviceInfo + "/network_interface.log");

                // OTA key
                exec("cp /system/etc/security/otacerts.zip " + path_DeviceInfo);

                // ifconfig
                execToybox("ifconfig", path_DeviceInfo + "/ifconfig.log");

                // dumpsys
                execOutput("dumpsys", path_DeviceInfo + "/dumpsys.log");
            }

            // copy logcat
            Log.d(TAG, "Start to copy logcat");
            String path_logcat = getLogcatPath();
            if (path_logcat != null)
            {
                String logPath = path_logcat + "/" + mDateTime + "_logcat.log";
                new Thread(() -> copyLogcat(logPath)).start();
            }
            Log.d(TAG, "copyAllLog: DONE!");
        }};
    }

    // copy logcat v1
    /*private Runnable copyLogcat()
    {
        mLogcatTimeout = 0;

        return new Runnable() {
            @Override
            public void run()
            {
                String path_logcat = getLogcatPath();
                if (path_logcat != null)
                {
                    // logcat dump
                    path_logcat = path_logcat + "/" + mDateTime + "_logcat.log";
                    //exec("cp " + LOGCAT_TMP + " " + path_logcat);
                    exec("logcat -d -f " + path_logcat);
                    Log.d(TAG, "copyLogcat: logcat dump !!!");

                    // sync every 30 sec
                    if (mLogcatTimeout % 30 == 0)
                    {
                        exec("sync");
                        mLogcatTimeout = 0;
                        Log.d(TAG, "copyLogcat: sync");
                    }

                    if (DEBUG)
                        Log.d(TAG, "copyLogcat: timeout = " + mLogcatTimeout++);
                }
                mHandler.postDelayed(this, 1000);
            }
        };
    }*/

    // copy logcat v2
    private void copyLogcat(String logPath)
    {
        int fileIndex = 0;
        long timeMs = System.currentTimeMillis();
        long timeoutMs = 0;
        String line = "";
        int linesWritten = 0;
        Process process = null;
        BufferedReader bufferedReader = null;
        File currentLogFile = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter writer = null;
        boolean isRunning = true;

        logPath = logPath.replace("logcat.log", "logcat_" + fileIndex + ".log");
        Log.d(TAG, "copyLogcat: logPath = " + logPath);

        while (isRunning) {
            try {
                // reader
                String[] logcatCmd = new String[] {
                    "logcat",
                    "-v", "threadtime",   // 輸出格式，包含時間戳和線程信息
                    "*:V"                // 所有標籤的所有等級
                };
                process = Runtime.getRuntime().exec(logcatCmd);
                //process = Runtime.getRuntime().exec("logcat");
                //process = Runtime.getRuntime().exec("logcat -v time -b main -b system -b crash");
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                // writer
                currentLogFile = new File(logPath);
                fos = new FileOutputStream(currentLogFile, true);
                osw = new OutputStreamWriter(fos);
                writer = new BufferedWriter(osw);

                // write log
                while ((line = bufferedReader.readLine()) != null)
                {
                    String grepStr = SystemProperties.get(PROPERTY_GREP, "");
                    Pattern pattern = Pattern.compile(grepStr);

                    line += "\n";
                    if (grepStr.isEmpty() || pattern.matcher(line).find()) {
                        writer.write(line);
                        //Log.e(TAG, "copyLogcat: @@@@@ " + line);

                        // 每 100 行就強制寫入磁碟
                        if (++linesWritten % 100 == 0 || ((System.currentTimeMillis() - timeMs) > (5 * 1000))) {
                            Log.e(TAG, "copyLogcat: 每 100 行就強制寫入磁碟");
                            timeMs = System.currentTimeMillis();
                            writer.flush();
                        }
                    }
                    else
                        Log.e(TAG, "copyLogcat: not match");

                    // Check file size and roll over if needed
                    if (currentLogFile.length() >= MAX_FILE_SIZE) {
                        Log.e(TAG, "copyLogcat: Check file size and roll over");
                        writer.close();
                        logPath = logPath.replace("logcat_" + fileIndex + ".log", "logcat_" + (++fileIndex) + ".log");
                        Log.d(TAG, "copyLogcat: logPath = " + logPath);
                        currentLogFile = new File(logPath);
                        fos = new FileOutputStream(currentLogFile, true);
                        osw = new OutputStreamWriter(fos);
                        writer = new BufferedWriter(osw);
                        linesWritten = 0;
                    }

                    /* timeoutMs = System.currentTimeMillis() - timeMs;
                    if (timeoutMs >= LOGCAT_TIMEOUT)
                    {
                        Log.d(TAG, "copyLogcat: sync file, timeoutMs = " + timeoutMs);
                        timeMs = System.currentTimeMillis();
                        //exec("sync");
                        //exec("fsync " + logPath);
                        fos.flush();
                        fos.getFD().sync();
                    } */
                }
                Log.e(TAG, "copyLogcat: CLOSE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ");
                writer.close();
            }
            catch (Exception e) {
                Log.e(TAG, "copyLogcat: " + e.getMessage());
                e.printStackTrace();

                // 等待一段時間後重試
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            finally {
                // 確保資源釋放
                Log.e(TAG, "copyLogcat: 確保資源釋放");
                try {
                    if (writer != null) writer.close();
                    if (bufferedReader != null) bufferedReader.close();
                    if (process != null) process.destroy();
                } catch (IOException e) {
                    Log.e(TAG, "copyLogcat: Error closing resources: " + e.getMessage());
                }
            }
        }

        if (DEBUG)
            Log.d(TAG, "copyLogcat: done!");
    }

    /*private void copyLogcat_v3()
    {
        String line = "";
        long timeMs = 0;
        long timeoutMs = 0;

        // write to sdcard
        try {
            // reader
            Process process = Runtime.getRuntime().exec("logcat");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // writer
            FileOutputStream fos = new FileOutputStream(new File("/sdcard/logcat"));
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter writer = new BufferedWriter(osw);

            // write log
            while ((line = bufferedReader.readLine()) != null)
            {
                line += "\n";
                writer.write(line);
                writer.flush();
            }
            writer.close();
        }
        catch (Exception e) {
            Log.d(TAG, "copyLogcat: " + e);
            e.printStackTrace();
        }

        if (DEBUG)
            Log.d(TAG, "copyLogcat: done!");
    }*/

    private void copyDropbox(String logPath)
    {
        //DropBoxManager manager = (DropBoxManager) getSystemService(Context.DROPBOX_SERVICE);
        DropBoxManager.Entry entry = null;
        String dropboxPath = logPath;
        int entryTextLength = 0;
        long startTime = 0;
        long entryTime = 0;
        String entryTag = "";
        String entryText = "";
        String entryLog = "";

        try {
            FileWriter fileWriter = new FileWriter(dropboxPath, true);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            while ((entry = mDropboxManager.getNextEntry(null, startTime)) != null) {
                entryTime = entry.getTimeMillis();
                entryTag = entry.getTag();
                entryText = entry.getText(MAX_BYTE);

                if (entryText != null)
                    entryTextLength = entryText.length();

                entryLog = "========================================\n" +
                        getDateTime(entryTime) + " " +
                        entryTag + " (text, " + entryTextLength + " bytes)\n" +
                        entryText + "\n\n";
                // write entry
                writer.write(entryLog);
                writer.flush();
                //Log.d(TAG, "copyDropbox: entryTime = " + entryTime);
                //Log.d(TAG, "copyDropbox: entryTag = " + entryTag);
                //Log.d(TAG, "copyDropbox: entryLog = " + entryLog);

                if (entryTag.startsWith("SYSTEM_RECOVERY_LOG")) {
                    mLastLog = entryLog;
                    mLastLogTime = "" + entryTime;
                }

                startTime = entry.getTimeMillis();
                entry.close();
            }
        } catch (IOException exception) {
            Log.d(TAG, "copyDropbox: IOException");
            exception.printStackTrace();
        }

        if (DEBUG)
            Log.d(TAG, "copyDropbox: done!");
    }

    private void copyProperty(String logPath)
    {
        String line = "";

        try {
            // reader
            Process process = Runtime.getRuntime().exec("getprop");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // writer
            FileOutputStream fos = new FileOutputStream(new File(logPath));
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter writer = new BufferedWriter(osw);
            //BufferedWriter writer = new BufferedWriter(new FileWriter(logPath, true));

            // write log
            while ((line = bufferedReader.readLine()) != null)
            {
                line += "\n";
                writer.write(line);
                writer.flush();

                // sync
                fos.flush();
                fos.getFD().sync();
            }

            writer.close();
        }
        catch (IOException e) {
            Log.d(TAG, "copyProperty: e = " + e.toString());
        }

        if (DEBUG)
            Log.d(TAG, "copyProperty: done");
    }

    private void copyRecovery(String dstPath)
    {
        File recovery = new File(dstPath + "/recovery");

        if (DEBUG)
            Log.d(TAG, "copyRecovery: usb recovery = " + recovery.getAbsolutePath());

        copyDirectory(new File("/cache/recovery"), recovery);
        copyLastLog(recovery);
    }

    private void copyLastLog(File recovery)
    {
        if (mLastLog == null)
        {
            Log.d(TAG, "copyLastLog: there is no last_log");
            return;
        }

        try {
            // check recovery
            if (!recovery.exists())
                recovery.mkdirs();

            if (DEBUG)
                Log.d(TAG, "copyLastLog: recovery = " + recovery.getAbsolutePath());

            // writer last_log
            File last_log = new File(recovery, "last_log");
            FileOutputStream fos = new FileOutputStream(last_log);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter writer = new BufferedWriter(osw);

            if (DEBUG)
                Log.d(TAG, "copyLastLog: last_log = " + last_log.getAbsolutePath());

            writer.write(mLastLog); // write log
            writer.flush();
            fos.flush(); // sync
            fos.getFD().sync();
            writer.close();
        }
        catch (IOException e) {
            Log.d(TAG, "copyLastLog: " + e);
        }

        if (DEBUG)
            Log.d(TAG, "copyLastLog: END");
    }

    private String getLogcatPath()
    {
        String logcatPath = getUsbPath() + "/Pesi_Logcat";

        if (DEBUG)
            Log.d(TAG, "getLogcatPath: to find path = " + logcatPath);

        logcatPath = findPath(logcatPath);
        return logcatPath;
    }

    private String getUserSystemPath()
    {
        String userSystemPath = getUsbPath() + "/Pesi_UserSystem";

        if (DEBUG)
            Log.d(TAG, "getUserSystemPath: to find path = " + userSystemPath);

        userSystemPath = findPath(userSystemPath);
        return userSystemPath;
    }

    private String getDeviceInfoPath()
    {
        String devPath = getUsbPath() + "/Pesi_DeviceInfo";

        if (DEBUG)
            Log.d(TAG, "getDevPath: to find path = " + devPath);

        devPath = findPath(devPath);
        return devPath;
    }

    private String findPath(String path)
    {
        String line = null;

        try {
            String usbPath = getUsbPath();
            String cmd = "find " + usbPath + " -type d -ipath " + path;

            if (DEBUG)
                Log.d(TAG, "findPath: cmd = " + cmd);

            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            line = bufferedReader.readLine();

            if (DEBUG)
                Log.d(TAG, "findPath: found path = " + line);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return line;
    }

    private String getUsbPath()
    {
        if (mUsbPath == null)
        {
            List<StorageVolume> volumeList = mStorageManager.getStorageVolumes();

            // get storage
            for (StorageVolume volume : volumeList)
            {
                // get UUID
                if (volume.getUuid() != null)
                {
                    mUsbPath = "/storage/" + volume.getUuid();
                    Log.d(TAG, "getUsbPath: usb path = " + mUsbPath);
                    break;
                }
            }
        }

        return mUsbPath;
    }

    private String getTimePath(String logPath, String dateTime)
    {
        String timeDirPath = logPath + "/" + dateTime;
        File timeDir = null;

        if (DEBUG)
            Log.d(TAG, "getTimePath: mkdir " + timeDirPath);

        exec("mkdir " + timeDirPath);
        timeDir = new File(timeDirPath);

        if (DEBUG)
            Log.d(TAG, "getTimePath: time folder exists = " + timeDir.exists());

        if (timeDir.exists())
            return timeDirPath;
        else
            return null;
    }

    private static String getDateTime()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return formatter.format(calendar.getTime());
    }

    private static String getDateTime(long timeMs)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMs);
        return formatter.format(calendar.getTime());
    }

    private void exec(String cmd)
    {
        try {
            if (DEBUG)
                Log.d(TAG, "exec: cmd = " + cmd);
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
        }
        catch (Exception exception) {
            Log.e(TAG, "exec: exception = " + exception);
        }
    }

    private void execOutput(String cmd, String path)
    {
        if (DEBUG)
            Log.d(TAG, "execOutput: cmd = " + cmd + " , path = " + path);
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectOutput(new File(path));
            builder.redirectError(new File(path));
            Process p = builder.start();
        }
        catch (Exception exception) {
            Log.e(TAG, "execOutput: exception = " + exception);
        }
        exec("fsync " + path);
    }

    private void execToybox(String cmd, String path)
    {
        if (DEBUG)
            Log.d(TAG, "execToybox: cmd = " + cmd + " , path = " + path);
        try {
            ProcessBuilder builder = new ProcessBuilder("/system/bin/toybox", cmd);
            builder.redirectOutput(new File(path));
            builder.redirectError(new File(path));
            Process p = builder.start();
        }
        catch (Exception exception) {
            Log.e(TAG, "execToybox: exception = " + exception);
        }
        exec("fsync " + path);
    }

    public void copyDirectory(File sourceDir, File destDir)
    {
        try {
            if (!destDir.exists())
                destDir.mkdirs();

            if (!sourceDir.exists())
                Log.d(TAG, "copyDirectory: sourceDir does not exist");

            if (sourceDir.isFile() || destDir.isFile())
                Log.d(TAG, "copyDirectory: Either sourceDir or destDir is not a directory");

            copyDirectoryImpl(sourceDir, destDir);
        }
        catch(Exception e) {
            Log.d(TAG, "copyDirectory: " + e);
        }
    }

    private void copyDirectoryImpl(File sourceDir, File destDir)
    {
        try {
            File[] items = sourceDir.listFiles();

            if (DEBUG)
                Log.d(TAG, "copyDirectoryImpl: items: " + items);

            if (items != null && items.length > 0)
            {
                for (File anItem : items)
                {
                    if (DEBUG)
                        Log.d(TAG, "copyDirectoryImpl: anItem: " + anItem.getAbsolutePath());

                    if (anItem.isDirectory())
                    {
                        // create the directory in the destination
                        File newDir = new File(destDir, anItem.getName());
                        if (DEBUG)
                            Log.d(TAG, "copyDirectoryImpl: CREATED DIR: " + newDir.getAbsolutePath());
                        newDir.mkdir();

                        // copy the directory (recursive call)
                        copyDirectory(anItem, newDir);
                    }
                    else {
                        // copy the file
                        File destFile = new File(destDir, anItem.getName());
                        if (DEBUG)
                            Log.d(TAG, "copyDirectoryImpl: copy destFile = " + destFile.getAbsolutePath());
                        copySingleFile(anItem, destFile);

                    }
                }
            }
        }
        catch(Exception e) {
            Log.d(TAG, "copyDirectoryImpl: " + e);
        }
    }

    private void copySingleFile(File sourceFile, File destFile)
    {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;

        try {
            if (DEBUG)
                Log.d(TAG, "copySingleFile: COPY FILE: " + sourceFile.getAbsolutePath() + ", TO: " + destFile.getAbsolutePath());

            if (!destFile.exists())
                destFile.createNewFile();

            sourceChannel = new FileInputStream(sourceFile).getChannel();
            destChannel = new FileOutputStream(destFile).getChannel();
            sourceChannel.transferTo(0, sourceChannel.size(), destChannel);

            if (sourceChannel != null)
                sourceChannel.close();

            if (destChannel != null)
                destChannel.close();
        }
        catch(Exception e) {
            Log.d(TAG, "copySingleFile: " + e);
        }
    }

    /*
    public void copy(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                out.flush();
            }
            out.close();
            in.close();
        } catch(IOException e) {
            Log.d(TAG, "copy: " + e);
        }
    } */

    /*
    private void copyRecoveryLog(File sourceFile, File destFile)
    {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        String output;

        try {
            Log.d(TAG, "copyRecoveryLog: COPY FILE: " + sourceFile.getAbsolutePath() + ", TO: " + destFile.getAbsolutePath());
            hidl_IMisc.ReadFileToString( sourceFile.getAbsolutePath(), output ) ;
        }
        catch (RemoteException e) {
            Log.d( TAG, "copyRecoveryLog: vendor.prime.hardware.misc.V1_0.IMisc error!!!! " + e) ;
        }

        Log.d(TAG, "copyRecoveryLog: output = " + output);
    }*/

    private void copyNetworkInterface(String logPath)
    {
        StringBuilder LOG = new StringBuilder();
        List<NetworkInterface> interfaces;

        try {
            interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : interfaces) {
                interfaceName(LOG, ni);
                linkEncap(LOG, ni);
                //hwAddr(LOG, ni);
                //driver(LOG, ni);
                inetAddr(LOG, ni);
                inetAddr6(LOG, ni);
                upRunningMtu(LOG, ni);
                LOG.append("\n");
            }

            if (DEBUG)
                Log.d(TAG, "copyNetworkInterface: LOG \n" + LOG);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // writer
            FileOutputStream fos = new FileOutputStream(new File(logPath));
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter writer = new BufferedWriter(osw);

            // write log
            writer.write(LOG.toString());
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            Log.d(TAG, "copyNetworkInterface: " + e);
        }
    }

    public void interfaceName(StringBuilder LOG, NetworkInterface ni) {
        String name = ni.getDisplayName();
        int i = 0;

        LOG.append(name);
        while(i < (10 - (name.length()))) {
            i++;
            LOG.append(" ");
        }
    }

    public void linkEncap(StringBuilder LOG, NetworkInterface ni) throws Exception {
        if (ni.isLoopback())
            LOG.append("Link encap:Local Loopback  ");
        else if (ni.isPointToPoint())
            LOG.append("Point-to-Point Protocol  ");
        else
            LOG.append("Link encap:UNSPEC  ");
    }

    public void hwAddr(StringBuilder LOG, NetworkInterface ni) throws Exception {
        // https://developer.android.com/training/articles/user-data-ids#mac-11-plus
        byte[] hwAddress;

        if (!ni.isLoopback()) {
            hwAddress = ni.getHardwareAddress();
            LOG.append("HWaddr ")
                    .append(Arrays.toString(hwAddress))
                    .append("  ");
        }
    }

    public void driver(StringBuilder LOG, NetworkInterface ni) throws Exception {
            if (!ni.isLoopback())
                LOG.append("Driver ").append("null");
    }

    public void inetAddr(StringBuilder LOG, NetworkInterface ni) {
        String tmpAddr = null;

        for(InterfaceAddress ia : ni.getInterfaceAddresses()) {
            InetAddress inetAddress = ia.getAddress();
            if (inetAddress != null)
                tmpAddr = inetAddress.getHostAddress();
            if (tmpAddr != null) {
                if (tmpAddr.contains("%") || tmpAddr.contains("::")) {
                    //Log.d(TAG, "inetAddr: do nothing");;
                }
                else {
                    String inetAddr = tmpAddr;
                    String inetBcast = getBcast(ia.getBroadcast());
                    LOG.append("\n          ");
                    LOG.append("inet addr:").append(inetAddr);
                    if (!inetAddr.equals("127.0.0.1"))
                        LOG.append("  Bcast:").append(inetBcast);
                    //LOG.append("  Mask:null");
                }
            }
        }
    }

    public void inetAddr6(StringBuilder LOG, NetworkInterface ni) {
        String tmpAddr = null;

        for(InterfaceAddress ia : ni.getInterfaceAddresses()) {
            InetAddress inetAddress = ia.getAddress();
            if (inetAddress != null)
                tmpAddr = inetAddress.getHostAddress();
            if (tmpAddr != null) {
                if (tmpAddr.contains("%")) {
                    String inet6Addr = tmpAddr.replace(ni.getDisplayName(), "").replace("%", "/") + ia.getNetworkPrefixLength();
                    LOG.append("\n          ");
                    LOG.append("inet6 addr: ").append(inet6Addr);
                    //LOG.append("  Scope: ").append("Link");
                }
                else if (tmpAddr.contains("::")) {
                    String inet6Addr = tmpAddr + "/" + ia.getNetworkPrefixLength();
                    LOG.append("\n          ");
                    LOG.append("inet6 addr: ").append(inet6Addr);
                    //LOG.append("  Scope: ").append("Link");
                }
                else {
                    //Log.d(TAG, "inetAddr6: do nothing");
                }
            }
        }
    }

    public String getBcast(InetAddress bcastAddr) {
        String bcast = null;
        if (bcastAddr != null) {
            bcast = bcastAddr.getHostAddress();
        }
        return bcast;
    }

    public void upRunningMtu(StringBuilder LOG, NetworkInterface ni) throws Exception {
        LOG.append("\n          ");
        if (ni.isUp())
            LOG.append("UP RUNNING ");
        if (ni.isLoopback())
            LOG.append("LOOPBACK ");
        if (ni.supportsMulticast())
            LOG.append("MULTICAST ");
        LOG.append("MTU:").append(ni.getMTU());
    }
}
