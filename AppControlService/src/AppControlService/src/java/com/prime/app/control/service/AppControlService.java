package com.prime.app.control.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.Message;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppControlService extends Service
{
    public String TAG = getClass().getSimpleName();

    // OTA Service package name
    public static final String OTA_SERVICE_PACKAGE_NAME            = "com.prime.android.tv.otaservice";

    // broadcast to OTA Service
    public static final String ABUPDATE_BROADCAST_START            = "com.prime.android.tv.otaservice.abupdate.start";
    public static final String ABUPDATE_BROADCAST_REGISTER_CALLER  = "com.prime.android.tv.otaservice.abupdate.register.caller";

    // param of ABUPDATE_BROADCAST_START
	public static final String ABUPDATE_BROADCAST_UPDATE_BIN_URL   = "alticast.abupdate.url.bin";
    public static final String ABUPDATE_BROADCAST_UPDATE_PARAM_URL = "alticast.abupdate.url.txt";
    public static final String ABUPDATE_BROADCAST_UPDATE_MODE      = "alticast.abupdate.MODE";
    public static final int    ABUPDATE_INTERNET_MODE              = 0;

    // App Manager
    public static final String APP_MANAGER_PACKAGE_NAME            = "com.prime.appDownloadManager";
    public static final String APP_MANAGER_ACTION                  = "pesi.broadcast.action.device.manager";

    // property
    public static final String PROPERTY_CONFIG_URL  = "persist.sys.prime.app_control_config";

    // default value
    // public static final String DEFAULT_CONFIG_URL   = "http://10.1.4.180:8080/default.json";
    public static final int    DEFAULT_UPDATE_CYCLE = 30 * 1000;

    // debug
    public static final int    DEBUG_NORMAL         = 1;
    public static final int    DEBUG_VERBOSE        = 2;
    public static final int    DEBUG                = DEBUG_VERBOSE;

    // variable
    public Context          mContext            = null;
    public AppMgrReceiver   mAppManagerReceiver = null;
    public OtaReceiver      mOtaReceiver        = null;
    public List<String>     mUrlList            = new ArrayList<>();

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG.CHECK_TIMESTAMP: // 0
                    checkTimestamp();
                    break;
                case MSG.BROADCAST_OTA:
                    boolean timestampOK = (boolean) msg.obj;
                    broadcastOTA(timestampOK);
                    break;
                case MSG.APP_INSTALL:
                    broadcastAppInstall();
                    break;
                case MSG.APP_UNINSTALL:
                    broadcastAppUninstall();
                    break;
                case MSG.WHITELIST_ON_OFF:
                    broadcastWhitelistOnOff();
                    break;
                case MSG.WHITELIST_APPEND:
                    broadcastWhitelistAppend();
                    break;
                case MSG.WHITELIST_REMOVE:
                    broadcastWhitelistRemove();
                    break;
                case MSG.WHITELIST_REMOVE_ALL:
                    broadcastWhitelistRemoveAll();
                    break;
                case MSG.WHITELIST_REPLACE:
                    broadcastWhitelistReplace();
                    break;
                case MSG.WHITELIST_GET:
                    broadcastWhitelistGet();
                    break;
                case MSG.DOWNLOAD_CONFIG:
                    configDownload();
                    break;
                case MSG.DOWNLOAD_CONFIG_AGAIN:
                    configDownloadAgain();
                    break;
            }
        }
    };

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
        super.onCreate();
        mContext = getApplicationContext();

        initConfigURL();
        registerOtaReceiver();
        registerAppManagerReceiver();
        checkService();
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        unregisterReceiver(mOtaReceiver);
        unregisterReceiver(mAppManagerReceiver);
        mHandler.removeCallbacksAndMessages(null);
        mOtaReceiver = null;
        mAppManagerReceiver = null;
        mHandler = null;
        mContext = null;
    }

    public void initConfigURL() {
        SharedPreferences   sharedPreferences = getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);
        Set<String>         stringSet         = sharedPreferences.getStringSet("list_key", new HashSet<>());

        // select 1st URL
        Config.CONFIG_URL_LIST  = new ArrayList<>(stringSet);
        Config.CONFIG_URL       = (Config.CONFIG_URL_LIST.size() > 0) ? Config.CONFIG_URL_LIST.get(0) : null;

        // remove 1st URL
        mUrlList = new ArrayList<>(Config.CONFIG_URL_LIST);
        mUrlList.remove(Config.CONFIG_URL);

        Log.d(TAG, "initConfigURL: CONFIG_URL      : " + Config.CONFIG_URL);
        Log.d(TAG, "initConfigURL: CONFIG_URL_LIST : " + Config.CONFIG_URL_LIST);
        Log.d(TAG, "initConfigURL: mUrlList        : " + mUrlList);
    }

    public void checkService() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isServiceRunning()) {
                    Log.d(TAG, "checkService: is running service");
                    registerCaller();
                    configDownload();
                }
                else {
                    Log.d(TAG, "checkService: is not running service");
                    mHandler.postDelayed(this, 5000);
                }
            }
        }, 5000);
    }

    public boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean isAppManager = false;
        boolean isOtaService = false;

        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                String serviceClassName = service.service.getClassName();
                if (serviceClassName.equals("com.prime.appDownloadManager.ServerCommunicate.BackendManagementService"))
                    isAppManager = true;
                if (serviceClassName.equals("com.prime.android.tv.otaservice.DownloadService"))
                    isOtaService = true;
            }
        }
        Log.d(TAG, "isServiceRunning: " + (isAppManager && isOtaService));
        return isAppManager && isOtaService;
    }

    public void registerOtaReceiver() {
        Log.d(TAG, "registerOtaReceiver: ");
        mOtaReceiver = new OtaReceiver(mHandler);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OtaReceiver.ABUPDATE_BROADCAST_ERROR);
        intentFilter.addAction(OtaReceiver.ABUPDATE_BROADCAST_STATUS);
        intentFilter.addAction(OtaReceiver.ABUPDATE_BROADCAST_COMPLETE);
        intentFilter.addAction(OtaReceiver.ABUPDATE_BROADCAST_CALLER);
        registerReceiver(mOtaReceiver, intentFilter);
    }

    public void registerAppManagerReceiver() {
        Log.d(TAG, "registerAppManagerReceiver: ");
        mAppManagerReceiver = new AppMgrReceiver(mHandler);
        IntentFilter intentFilter = new IntentFilter(APP_MANAGER_ACTION);
        registerReceiver(mAppManagerReceiver, intentFilter);
    }

    public void registerCaller() {
        // OTA Service
        Log.d(TAG, "registerCaller: Register caller to OTA Service");
        Intent intent = new Intent(ABUPDATE_BROADCAST_REGISTER_CALLER);
        intent.setPackage(OTA_SERVICE_PACKAGE_NAME);
        intent.putExtra(OtaReceiver.ABUPDATE_BROADCAST_UPDATE_CALLER, getPackageName());
        sendBroadcastAsUser(intent, UserHandle.ALL);
        // App Manager
        Log.d(TAG, "registerCaller: Register caller to App Manager");
        Intent sendIntent = new Intent(APP_MANAGER_ACTION);
        sendIntent.setPackage(APP_MANAGER_PACKAGE_NAME);
        sendIntent.putExtra("INSTRUCTION_ID", AppMgrReceiver.SET_RETURN_TARGET);
        sendIntent.putExtra("RETURN_TARGET_ACTION", APP_MANAGER_ACTION);
        sendIntent.putExtra("RETURN_TARGET_PKGNAME", getPackageName());
        sendBroadcastAsUser(sendIntent, UserHandle.ALL);
    }

    public void configDownloadAgain() {
        mUrlList.remove(Config.CONFIG_URL);
        Config.CONFIG_URL = (mUrlList.size() > 0) ? mUrlList.get(0) : null;
        Config.UPDATE_CYCLE = 5000;

        Log.d(TAG, "configDownloadAgain: Config URL List: " + mUrlList);

        configDownload();
    }

    public void configDownload() {

        if (isEmpty(Config.CONFIG_URL))
            Config.CONFIG_URL = SystemProperties.get(PROPERTY_CONFIG_URL, null);

        Log.d(TAG, "configDownload: Config URL: " + Config.CONFIG_URL);
        Log.d(TAG, "configDownload: Delay     : " + Config.UPDATE_CYCLE + " Ms ................................................................");

        mHandler.postDelayed(() -> new Thread(() -> {
            try {
                StringBuilder jsonStr = new StringBuilder();
                String line = "";
                URL url = new URL(Config.CONFIG_URL);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));

                while ((line = bufferedReader.readLine()) != null) {
                   line += "\n";
                   jsonStr.append(line);
                }

                configUpdate(jsonStr.toString());
            }
            catch (Exception e) {
                Log.e(TAG, "configDownload: Error: " + e);
                // e.printStackTrace();
                // Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                // throw new RuntimeException(e);
                sendMessage(MSG.DOWNLOAD_CONFIG_AGAIN);
            }
        }).start(), Config.UPDATE_CYCLE);
    }

    public void configUpdate(Object object) {
        Log.d(TAG, "configUpdate: parse json string to params");
        JSONObject configuration, otaFile, appManager;
        JSONArray  installApps, uninstallApps;
        JSONObject whitelist;
        JSONArray  whitelistAppend, whitelistRemove, whitelistReplace, configUrlList;

        // Configuration Check
        try {
            configuration = new JSONObject((String) object);
        } catch (Exception e) {
            Log.e(TAG, "configUpdate: Error Configuration Check");
            sendMessage(MSG.DOWNLOAD_CONFIG_AGAIN);
            return;
        }
        // Configuration Update Cycle
        try {
            configuration       = new JSONObject((String) object);
            Config.UPDATE_CYCLE = configuration.getLong("Configuration Update Cycle") * 1000;
        }
        catch (JSONException e) {
            Log.e(TAG, "configUpdate: Error Configuration Update Cycle");
            Config.UPDATE_CYCLE = DEFAULT_UPDATE_CYCLE;
        }
        // Configuration URL
        try {
            // add config URL array
            configuration = new JSONObject((String) object);
            configUrlList = configuration.getJSONArray("Configuration Remote URL");
            for (int i = configUrlList.length()-1; i >= 0; i--) {
                String url = (String) configUrlList.get(i);
                if (Config.CONFIG_URL_LIST.contains(url))
                    Config.CONFIG_URL_LIST.remove(url);
                Config.CONFIG_URL_LIST.add(0, url);
            }
            mUrlList = new ArrayList<>(Config.CONFIG_URL_LIST);
            configUpdateURL();

            // set CONFIG_URL
            if (Config.CONFIG_URL_LIST.size() > 0)
                Config.CONFIG_URL = Config.CONFIG_URL_LIST.get(0);
        }
        catch (JSONException e) {
            Log.e(TAG, "configUpdate: Error Configuration Remote URL");
            // setupConfigURL();
        }
        // OTA File
        try {
            configuration   = new JSONObject((String) object);
            otaFile         = configuration.getJSONObject("OTA File");
            Config.OTA_PAYLOAD_BIN        = otaFile.getString("payload");
            Config.OTA_PAYLOAD_PROPERTIES = otaFile.getString("payload properties");
            Config.OTA_METADATA           = otaFile.getString("metadata");
        }
        catch (JSONException e) {
            Log.e(TAG, "configUpdate: Error OTA File");
            Config.OTA_PAYLOAD_BIN        = null;
            Config.OTA_PAYLOAD_PROPERTIES = null;
            Config.OTA_METADATA           = null;
        }
        // App Install
        try {
            configuration   = new JSONObject((String) object);
            appManager      = configuration.getJSONObject("App Manager");
            installApps     = appManager.getJSONArray("Install");
            Config.APP_INSTALL_PKG.clear();
            Config.APP_INSTALL_URL.clear();
            for (int i = 0; i < installApps.length(); i++) {
                JSONArray oneApp = (JSONArray) installApps.get(i);
                String pkgName   = (String) oneApp.get(0);
                String apkUrl    = (String) oneApp.get(1);
                if (!Config.APP_INSTALL_PKG.contains(pkgName)) {
                    Config.APP_INSTALL_PKG.add(pkgName);
                    Config.APP_INSTALL_URL.add(apkUrl);
                }
            }
        }
        catch (JSONException e) {
            Log.e(TAG, "configUpdate: Error App Install");
            Config.APP_INSTALL_PKG.clear();
            Config.APP_INSTALL_URL.clear();
        }
        // App Uninstall
        try {
            configuration   = new JSONObject((String) object);
            appManager      = configuration.getJSONObject("App Manager");
            uninstallApps   = appManager.getJSONArray("Uninstall");
            Config.APP_UNINSTALL_PKG.clear();
            for (int i = 0; i < uninstallApps.length(); i++) {
                String pkgName = (String) uninstallApps.get(i);
                if (!Config.APP_UNINSTALL_PKG.contains(pkgName))
                    Config.APP_UNINSTALL_PKG.add(pkgName);
            }
        }
        catch (JSONException e) {
            Log.e(TAG, "configUpdate: Error App Uninstall");
            Config.APP_UNINSTALL_PKG.clear();
        }
        // Whitelist ON/OFF
        try {
            configuration   = new JSONObject((String) object);
            appManager      = configuration.getJSONObject("App Manager");
            whitelist       = appManager.getJSONObject("Whitelist");
            Config.WHITELIST_ENABLE = whitelist.getBoolean("Enable");
        }
        catch (JSONException e) {
            Log.e(TAG, "configUpdate: Error Whitelist ON/OFF");
            Config.WHITELIST_ENABLE = false;
        }
        // Whitelist Append
        try {
            configuration   = new JSONObject((String) object);
            appManager      = configuration.getJSONObject("App Manager");
            whitelist       = appManager.getJSONObject("Whitelist");
            whitelistAppend = whitelist.getJSONArray("Append");
            Config.WHITELIST_APPEND.clear();
            for (int i = 0; i < whitelistAppend.length(); i++)
                Config.WHITELIST_APPEND.add((String) whitelistAppend.get(i));
        }
        catch (JSONException e) {
            Log.e(TAG, "configUpdate: Error Whitelist Append");
            Config.WHITELIST_APPEND.clear();
        }
        // Whitelist Replace
        try {
            configuration    = new JSONObject((String) object);
            appManager       = configuration.getJSONObject("App Manager");
            whitelist        = appManager.getJSONObject("Whitelist");
            whitelistReplace = whitelist.getJSONArray("Replace");
            Config.WHITELIST_REPLACE.clear();
            for (int i = 0; i < whitelistReplace.length(); i++)
                Config.WHITELIST_REPLACE.add((String) whitelistReplace.get(i));
        }
        catch (JSONException e) {
            Log.e(TAG, "configUpdate: Error Whitelist Replace");
            Config.WHITELIST_REPLACE.clear();
        }
        // Whitelist Remove
        try {
            configuration   = new JSONObject((String) object);
            appManager      = configuration.getJSONObject("App Manager");
            whitelist       = appManager.getJSONObject("Whitelist");
            whitelistRemove = whitelist.getJSONArray("Remove");
            Config.WHITELIST_REMOVE.clear();
            for (int i = 0; i < whitelistRemove.length(); i++)
                Config.WHITELIST_REMOVE.add((String) whitelistRemove.get(i));
        }
        catch (JSONException e) {
            Log.e(TAG, "configUpdate: Error Whitelist Remove");
            Config.WHITELIST_REMOVE.clear();
        }
        // Whitelist Remove All
        try {
            configuration   = new JSONObject((String) object);
            appManager      = configuration.getJSONObject("App Manager");
            whitelist       = appManager.getJSONObject("Whitelist");
            Config.WHITELIST_REMOVE_ALL = whitelist.getBoolean("RemoveAll");
        }
        catch (JSONException e) {
            Log.e(TAG, "configUpdate: Error Whitelist Remove All");
            Config.WHITELIST_REMOVE_ALL = false;
        }

        configLog();

        if (Config.OTA_PAYLOAD_BIN        != null &&
            Config.OTA_PAYLOAD_PROPERTIES != null &&
            Config.OTA_METADATA           != null) {
            Log.d(TAG, "configUpdate: found OTA file! Check timestamp!");
            sendMessage(MSG.CHECK_TIMESTAMP);
        }
        else {
            Log.e(TAG, "configUpdate: OTA file not found! Do not check timestamp!");
            sendMessage(MSG.WHITELIST_ON_OFF);
        }
    }

    public void configUpdateURL() {
        Log.d(TAG, "configUpdateURL: mUrlList: " + mUrlList);
        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> stringSet = new HashSet<>(mUrlList);
        editor.putStringSet("list_key", stringSet);
        editor.apply();
    }

    public void configLog() {

        if (DEBUG < DEBUG_VERBOSE)
            return;

        Log.d(TAG, "configLog: UPDATE_CYCLE         " + Config.UPDATE_CYCLE);
        Log.d(TAG, "configLog: CONFIG_URL           " + Config.CONFIG_URL);
        Log.d(TAG, "configLog: CONFIG_URL_LIST      " + Config.CONFIG_URL_LIST);
        Log.d(TAG, "configLog: PAYLOAD_BIN          " + Config.OTA_PAYLOAD_BIN);
        Log.d(TAG, "configLog: PAYLOAD_PROPERTIES   " + Config.OTA_PAYLOAD_PROPERTIES);
        Log.d(TAG, "configLog: METADATA             " + Config.OTA_METADATA);
        Log.d(TAG, "configLog: WHITELIST_ENABLE     " + Config.WHITELIST_ENABLE);
        Log.d(TAG, "configLog: WHITELIST_REMOVE_ALL " + Config.WHITELIST_REMOVE_ALL);
        for (int i = 0; i < Config.WHITELIST_APPEND.size(); i++)  Log.d(TAG, "configLog: WHITELIST_APPEND     " + Config.WHITELIST_APPEND.get(i));
        for (int i = 0; i < Config.WHITELIST_REMOVE.size(); i++)  Log.d(TAG, "configLog: WHITELIST_REMOVE     " + Config.WHITELIST_REMOVE.get(i));
        for (int i = 0; i < Config.WHITELIST_REPLACE.size(); i++) Log.d(TAG, "configLog: WHITELIST_REPLACE    " + Config.WHITELIST_REPLACE.get(i));
        for (int i = 0; i < Config.APP_INSTALL_PKG.size(); i++)   Log.d(TAG, "configLog: APP_INSTALL          " + Config.APP_INSTALL_PKG.get(i) + " " + Config.APP_INSTALL_URL.get(i));
        for (int i = 0; i < Config.APP_UNINSTALL_PKG.size(); i++) Log.d(TAG, "configLog: APP_UNINSTALL        " + Config.APP_UNINSTALL_PKG.get(i));
    }

    /* public void setupConfigURL() {
        String propertyURL = SystemProperties.get(PROPERTY_CONFIG_URL, null);
        Log.d(TAG, "setupConfigURL: Property URL: " + propertyURL);
        if (!isEmpty(propertyURL))
            Config.CONFIG_URL = propertyURL;
        else
            Config.CONFIG_URL = DEFAULT_CONFIG_URL;
        Log.d(TAG, "setupConfigURL: Config URL: " + Config.CONFIG_URL);
    } */

    public void checkTimestamp() {
        new Thread(() -> {
            long sysTime = Long.parseLong(SystemProperties.get("ro.build.date.utc", "-1"));
            long metaTime = 0;

            // read metadata
            String metaPath = Config.OTA_METADATA;
            String line     = null;
            if (DEBUG >= DEBUG_NORMAL)
                Log.d(TAG, "checkTimestamp: metadata    = " + metaPath);
            try {
                URL url = new URL(metaPath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("post-timestamp")) {
                        line = line.replace("post-timestamp=", "");
                        break;
                    }
                }
                reader.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            if (line == null)
                line = "0";

            // get metadata timestamp
            metaTime = Long.parseLong(line);

            boolean timestampOK = metaTime > sysTime;
            if (DEBUG >= DEBUG_NORMAL) {
                Log.d(TAG, "checkTimestamp: metaTime    = " + metaTime);
                Log.d(TAG, "checkTimestamp: sysTime     = " + sysTime);
                //Log.d(TAG, "checkTimestamp: metaTime > sysTime , result = " + (metaTime > sysTime));
                Log.d(TAG, "checkTimestamp: timestampOK = " + timestampOK);
            }
            sendMessage(MSG.BROADCAST_OTA, timestampOK);

        }).start();
    }

    public void broadcastOTA(boolean timestampOK) {

        Log.i(TAG, "broadcastOTA: timestampOK = " + timestampOK);
        String payloadBin   = Config.OTA_PAYLOAD_BIN;
        String payloadProp  = Config.OTA_PAYLOAD_PROPERTIES;
        int    TODO_MSG     = MSG.WHITELIST_ON_OFF;

        if (OtaReceiver.OTA_IN_PROGRESS) {
            Log.e(TAG, "broadcastOTA: OTA in progress");
            sendMessage(TODO_MSG);
            return;
        }

        if (!timestampOK) {
            Log.e(TAG, "broadcastOTA: timestamp FAIL!");
            sendMessage(TODO_MSG);
            return;
        }

        Log.d(TAG, "broadcastOTA: START , bin file = " + payloadBin);
        Log.d(TAG, "broadcastOTA: START , property = " + payloadProp);

        Intent intent = new Intent(ABUPDATE_BROADCAST_START);
        intent.setPackage(OTA_SERVICE_PACKAGE_NAME);
        intent.putExtra(ABUPDATE_BROADCAST_UPDATE_BIN_URL,     payloadBin);
        intent.putExtra(ABUPDATE_BROADCAST_UPDATE_PARAM_URL,   payloadProp);
        intent.putExtra(ABUPDATE_BROADCAST_UPDATE_MODE,        ABUPDATE_INTERNET_MODE);
        sendBroadcastAsUser(intent, UserHandle.ALL);
        sendMessage(TODO_MSG);
    }

    public void broadcastWhitelistOnOff() {
        Log.i(TAG, "broadcastWhitelistOnOff: WHITELIST_ENABLE = " + Config.WHITELIST_ENABLE);
        Intent OnOff = new Intent(APP_MANAGER_ACTION);
        OnOff.setPackage(APP_MANAGER_PACKAGE_NAME);
        OnOff.putExtra("INSTRUCTION_ID", AppMgrReceiver.SET_WHITELIST_ON_OFF);
        OnOff.putExtra("WHITELIST_ONOFF", Config.WHITELIST_ENABLE);
        sendBroadcastAsUser(OnOff, UserHandle.ALL);
    }

    public void broadcastWhitelistAppend() {
        if (Config.WHITELIST_APPEND.size() != 0) {
            Log.i(TAG, "broadcastWhitelistAppend: WHITELIST_APPEND = " + Config.WHITELIST_APPEND);
            Intent Append = new Intent(APP_MANAGER_ACTION);
            Append.setPackage(APP_MANAGER_PACKAGE_NAME);
            Append.putExtra("INSTRUCTION_ID", AppMgrReceiver.SET_WHITELIST);
            Append.putExtra("SET_WHITELIST", (ArrayList<String>) Config.WHITELIST_APPEND);
            Append.putExtra("APPEND", true);
            sendBroadcastAsUser(Append, UserHandle.ALL);
        }
        else {
            Log.e(TAG, "broadcastWhitelistAppend: empty WHITELIST_APPEND = " + Config.WHITELIST_APPEND);
            broadcastWhitelistReplace();
        }
    }

    public void broadcastWhitelistReplace() {
        if (Config.WHITELIST_REPLACE.size() != 0) {
            Log.i(TAG, "broadcastWhitelistReplace: WHITELIST_REPLACE = " + Config.WHITELIST_REPLACE);
            Intent Replace = new Intent(APP_MANAGER_ACTION);
            Replace.setPackage(APP_MANAGER_PACKAGE_NAME);
            Replace.putExtra("INSTRUCTION_ID", AppMgrReceiver.SET_WHITELIST);
            Replace.putExtra("SET_WHITELIST", (ArrayList<String>) Config.WHITELIST_REPLACE);
            Replace.putExtra("APPEND", false);
            sendBroadcastAsUser(Replace, UserHandle.ALL);
        }
        else {
            Log.e(TAG, "broadcastWhitelistReplace: empty WHITELIST_REPLACE = " + Config.WHITELIST_REPLACE);
            broadcastWhitelistRemove();
        }
    }

    public void broadcastWhitelistRemove() {
        if (Config.WHITELIST_REMOVE.size() != 0) {
            Log.i(TAG, "broadcastWhitelistRemove: WHITELIST_REMOVE = " + Config.WHITELIST_REMOVE);
            Intent Remove = new Intent(APP_MANAGER_ACTION);
            Remove.setPackage(APP_MANAGER_PACKAGE_NAME);
            Remove.putExtra("INSTRUCTION_ID", AppMgrReceiver.DELETE_WHITELIST);
            Remove.putExtra("DELETE_WHITELIST", (ArrayList<String>) Config.WHITELIST_REMOVE);
            sendBroadcastAsUser(Remove, UserHandle.ALL);
        }
        else {
            Log.e(TAG, "broadcastWhitelistRemove: empty WHITELIST_REMOVE = " + Config.WHITELIST_REMOVE);
            broadcastWhitelistRemoveAll();
        }
    }

    public void broadcastWhitelistRemoveAll() {
        Log.i(TAG, "broadcastWhitelistRemoveAll: WHITELIST_REMOVE_ALL = " + Config.WHITELIST_REMOVE_ALL);
        if (Config.WHITELIST_REMOVE_ALL) {
            Intent RemoveAll = new Intent(APP_MANAGER_ACTION);
            RemoveAll.setPackage(APP_MANAGER_PACKAGE_NAME);
            RemoveAll.putExtra("INSTRUCTION_ID", AppMgrReceiver.DELETE_ALL_WHITELIST);
            sendBroadcastAsUser(RemoveAll, UserHandle.ALL);
        }
        else
            broadcastAppInstall();
    }

    public void broadcastWhitelistGet() {
        Log.i(TAG, "broadcastWhitelistGet: ");
        Intent Get = new Intent(APP_MANAGER_ACTION);
        Get.setPackage(APP_MANAGER_PACKAGE_NAME);
        Get.putExtra("INSTRUCTION_ID", AppMgrReceiver.GET_WHITELIST);
        sendBroadcastAsUser(Get, UserHandle.ALL);
    }

    public void broadcastAppInstall() {

        ArrayList<String> installPkgList        = new ArrayList<>(Config.APP_INSTALL_PKG);
        ArrayList<String> removePkgList         = new ArrayList<>();
        ArrayList<String> removeUrlList         = new ArrayList<>();

        // remove install app according to the app in system
        for (String pkg : installPkgList) {
            if (isPackageExisted(pkg)) {
                int i = installPkgList.indexOf(pkg);
                removePkgList.add(Config.APP_INSTALL_PKG.get(i));
                removeUrlList.add(Config.APP_INSTALL_URL.get(i));
            }
        }
        for (String pkg : removePkgList)
            Config.APP_INSTALL_PKG.remove(pkg);
        for (String url : removeUrlList)
            Config.APP_INSTALL_URL.remove(url);

        // remove install app according to whitelist
        if (Config.WHITELIST_ENABLE) {
            removePkgList.clear();
            removeUrlList.clear();
            for (String installPackage : Config.APP_INSTALL_PKG) {
                if (!Config.WHITELIST.contains(installPackage))
                {
                    int i = Config.APP_INSTALL_PKG.indexOf(installPackage);
                    removePkgList.add(Config.APP_INSTALL_PKG.get(i));
                    removeUrlList.add(Config.APP_INSTALL_URL.get(i));
                }
            }
            for (String pkg : removePkgList)
                Config.APP_INSTALL_PKG.remove(pkg);
            for (String url : removeUrlList)
                Config.APP_INSTALL_URL.remove(url);
        }

        // send broadcast
        if (Config.APP_INSTALL_PKG.size() != 0) {

            Log.i(TAG, "broadcastAppInstall: APP_INSTALL_PKG = " + Config.APP_INSTALL_PKG);
            mAppManagerReceiver.resetInstallCount();
            Intent install = new Intent(APP_MANAGER_ACTION);
            install.setPackage(APP_MANAGER_PACKAGE_NAME);
            install.putExtra("INSTRUCTION_ID", AppMgrReceiver.INSTALL_PACKAGE);
            install.putExtra("INSTALL_PKGNAME", (ArrayList<String>) Config.APP_INSTALL_PKG);
            install.putExtra("INSTALL_PKG_DOWNLOAD_PATH", (ArrayList<String>) Config.APP_INSTALL_URL);
            sendBroadcastAsUser(install, UserHandle.ALL);
        }
        else {
            Log.e(TAG, "broadcastAppInstall: empty APP_INSTALL_PKG = " + Config.APP_INSTALL_PKG);
            broadcastAppUninstall();
        }
    }

    public void broadcastAppUninstall() {

        ArrayList<String> uninstallPkgList = new ArrayList<>(Config.APP_UNINSTALL_PKG);
        ArrayList<String> removePkgList = new ArrayList<>();

        // uninstall if package in system
        for (String pkg : uninstallPkgList) {
            if (!isPackageExisted(pkg)) {
                int i = uninstallPkgList.indexOf(pkg);
                removePkgList.add(Config.APP_UNINSTALL_PKG.get(i));
            }
        }
        for (String pkg : removePkgList)
            Config.APP_UNINSTALL_PKG.remove(pkg);

        if (Config.APP_UNINSTALL_PKG.size() != 0) {
            Log.i(TAG, "broadcastAppUninstall: APP_UNINSTALL_PKG = " + Config.APP_UNINSTALL_PKG);
            mAppManagerReceiver.resetUninstallCount();
            Intent uninstall = new Intent(APP_MANAGER_ACTION);
            uninstall.setPackage(APP_MANAGER_PACKAGE_NAME);
            uninstall.putExtra("INSTRUCTION_ID", AppMgrReceiver.UNINSTALL_PACKAGE);
            uninstall.putExtra("UNINSTALL_PKGNAME", (ArrayList<String>) Config.APP_UNINSTALL_PKG);
            sendBroadcastAsUser(uninstall, UserHandle.ALL);
        }
        else {
            Log.e(TAG, "broadcastAppUninstall: empty APP_UNINSTALL_PKG = " + Config.APP_UNINSTALL_PKG);
            configDownload();
        }
    }

    public boolean isPackageExisted(String packageName) {
        List<ApplicationInfo> packages;
        PackageManager pm;

        pm = getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(packageName))
                return true;
        }
        return false;
    }

    public boolean isEmpty(String value) {
        return (value == null || (value != null && value.length() == 0));
    }

    public void sendMessage(int what, Object obj) {
        Message msg = new Message();
        msg.what    = what;
        msg.obj     = obj;
        mHandler.sendMessage(msg);
    }

    public void sendMessage(int what) {
        Message msg = new Message();
        msg.what    = what;
        mHandler.sendMessage(msg);
    }
}