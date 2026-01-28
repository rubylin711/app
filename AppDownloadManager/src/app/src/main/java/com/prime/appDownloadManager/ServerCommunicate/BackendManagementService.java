package com.prime.appDownloadManager.ServerCommunicate;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.prime.appDownloadManager.WhitelistSharedPreferences;
import com.prime.appDownloadManager.UpdateApp.UpdateAppProcess;
import com.prime.appDownloadManager.Utility;

import java.util.ArrayList;
import java.util.List;

import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.INSTRUCTION_ID_DEFAULT_VALUE;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.KEY_NAME_INSTRUCTION_ID;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.DELETE_WHITELIST.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.INSTALL_APK.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.RESULT_DETAIL.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.RESULT_STATUS.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.SET_RETURN_TARGET.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.SET_WHITELIST.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.SET_WHITELIST_ONOFF.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.UNINSTALL_APK.*;


public class BackendManagementService extends Service
{
    private static final String TAG = "BackendManagementService";
    private static final boolean DEBUG = false;
    private static final String DATA_SCHEME_PACKAGE = "package";
    private static final int APP_DATA_SCHEME_TITLE_LENGTH = 8;
    public static String mTargetAction = null;
    public static String mTargetPkgName = null;
    private UpdateAppProcess mUpdateAppProcessHandle = null;
    private PackageManager mPkgManager = null;
    private WhitelistSharedPreferences mWhitelistPref = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    public void onCreate()
    {
        mUpdateAppProcessHandle = new UpdateAppProcess(this);
        mPkgManager = getPackageManager();
        mWhitelistPref = new WhitelistSharedPreferences(this);
        mTargetAction = mWhitelistPref.getTargetAction();
        mTargetPkgName = mWhitelistPref.getTargetPkgName();

        IntentFilter appsFilter = new IntentFilter();
        appsFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appsFilter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        appsFilter.addDataScheme(DATA_SCHEME_PACKAGE);
        registerReceiver(mAppsListener, appsFilter, Context.RECEIVER_EXPORTED);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(InterfaceString.BROADCAST_ACTION);
        registerReceiver(mServerBroadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED);

        boolean whitelistOnOff = mWhitelistPref.getWhitelistOnOff();
        if (DEBUG)
        {
            Log.d(TAG, "onCreate: whitelistOnOff = " + whitelistOnOff);
            Log.d(TAG, "onCreate: mWhiteList = " + mWhitelistPref.getWhitelist());
        }
        if (whitelistOnOff)
            checkAllInstalledApps();
        super.onCreate();
    }

    public void onDestroy()
    {
        unregisterReceiver(mServerBroadcastReceiver);
        unregisterReceiver(mAppsListener);
        mUpdateAppProcessHandle.deinit();
        super.onDestroy();
    }

    private final BroadcastReceiver mServerBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(InterfaceString.BROADCAST_ACTION))
            {
                int instructionId = intent.getIntExtra(KEY_NAME_INSTRUCTION_ID, INSTRUCTION_ID_DEFAULT_VALUE);
                Log.d(TAG, "ServerBroadcastReceiver Receiver Server Broadcast id : " + instructionId);
                switch (instructionId)
                {
                    case InterfaceString.INSTRUCTION_LIST.UI_Management.INSTALL_APK.ID:
                    {
                        installApk(intent);
                        break;
                    }
                    case InterfaceString.INSTRUCTION_LIST.UI_Management.UNINSTALL_APK.ID:
                    {
                        uninstallApk(context, intent);
                        break;
                    }
                    case InterfaceString.INSTRUCTION_LIST.UI_Management.SET_WHITELIST_ONOFF.ID:
                    {
                        setWhitelistOnOff(context, intent);
                        break;
                    }
                    case InterfaceString.INSTRUCTION_LIST.UI_Management.SET_WHITELIST.ID:
                    {
                        setWhitelist(context, intent);
                        break;
                    }
                    case InterfaceString.INSTRUCTION_LIST.UI_Management.DELETE_WHITELIST.ID:
                    {
                        deleteWhitelist(context, intent);
                        break;
                    }
                    case InterfaceString.INSTRUCTION_LIST.UI_Management.DELETE_ALL_WHITELIST.ID:
                    {
                        deleteAllWhitelist(context);
                        break;
                    }
                    case InterfaceString.INSTRUCTION_LIST.UI_Management.GET_WHITELIST.ID:
                    {
                        getWhitelist(context);
                        break;
                    }
                    case InterfaceString.INSTRUCTION_LIST.UI_Management.SET_RETURN_TARGET.ID:
                    {
                        setReturnTarget(context, intent);
                        break;
                    }
                    default:
                        Log.d(TAG, "ServerBroadcastReceiver onReceive: instruction_id is not defined action, instruction Id = " + instructionId);
                }
            }
        }

        private void installApk(Intent intent)
        {
            //adb shell am broadcast -a pesi.broadcast.action.device.manager --ei INSTRUCTION_ID 201 --esal INSTALL_PKGNAME testvlc1,testvlc2 --esal INSTALL_PKG_DOWNLOAD_PATH https://www.techspot.com/downloads/downloadnow/6881/?evp=c5b1c0007f9e5702dc710c80dc5c7ee7,http://dl.hdslb.com/mobile/latest/android_tv_yst/iBiliTV-master.apk?t=1615533696000tv.danmaku.bili
            List<String> installPkgNames = intent.getStringArrayListExtra(KEY_NAME_INSTALL_PKGNAME);
            List<String> installPkgDownloadPaths = intent.getStringArrayListExtra(KEY_NAME_INSTALL_PKG_DOWNLOAD_PATH);
            boolean isWhitelistOn = mWhitelistPref.getWhitelistOnOff();
            if (DEBUG)
            {
                Log.d(TAG, "ServerBroadcastReceiver onReceive: install = " + installPkgNames);
                Log.d(TAG, "ServerBroadcastReceiver onReceive: install path = " + installPkgDownloadPaths);
            }

            if (installPkgNames != null && installPkgNames.size() != 0)
            {
                for (int num = 0; num < installPkgNames.size(); num++)
                {
                    String installPkgName = installPkgNames.get(num);
                    if (installPkgName != null
                            && installPkgName.length() > 0)
                    {
                        if (isWhitelistOn && !isInstallableApk(installPkgName))
                        {
                            Log.d(TAG, "ServerBroadcastReceiver onReceive: is not Installable Apk, installPkgName = " + installPkgName);
                            continue;
                        }
                        mUpdateAppProcessHandle.startProcess(
                                installPkgName,
                                installPkgDownloadPaths.get(num));
                    }
                }
            }
        }

        private void uninstallApk(Context context, Intent intent)
        {
            //adb shell am broadcast -a pesi.broadcast.action.device.manager --ei INSTRUCTION_ID 202 --esal UNINSTALL_PKGNAME com.xiaodianshi.tv.yst
            List<String> uninstallPkgNames = intent.getStringArrayListExtra(KEY_NAME_UNINSTALL_PKGNAME);
            if (DEBUG)
                Log.d(TAG, "ServerBroadcastReceiver onReceive: uninstall = " + uninstallPkgNames);

            if (uninstallPkgNames != null && uninstallPkgNames.size() > 0)
            {
                for (int num = 0; num < uninstallPkgNames.size(); num++)
                {
                    String uninstallPkgName = uninstallPkgNames.get(num);
                    if (uninstallPkgName != null
                            && uninstallPkgName.length() > 0)
                    {
                        if (Utility.checkAppExist(context, uninstallPkgName))
                            mUpdateAppProcessHandle.uninstallApp(uninstallPkgName);
                        else
                        {
                            int resultStatus = INSTALL_UNINSTALL_RESULT_STATUS_FAILURE;
                            if (!Utility.canSendBroadcast(context))
                                return;
                            Intent resultIntent = new Intent(mTargetAction);
                            resultIntent.setPackage(mTargetPkgName);
                            resultIntent.putExtra(
                                    KEY_NAME_INSTRUCTION_ID,
                                    InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.ID);
                            resultIntent.putExtra(
                                    KEY_NAME_EXECUTE_INSTRUCTION_ID,
                                    InterfaceString.INSTRUCTION_LIST.UI_Management.UNINSTALL_APK.ID);
                            resultIntent.putExtra(KEY_NAME_UNINSTALL_PKGNAME, uninstallPkgName);
                            resultIntent.putExtra(KEY_NAME_WHITELIST_ONOFF, mWhitelistPref.getWhitelistOnOff());
                            resultIntent.putExtra(KEY_NAME_RESULT_STATUS, resultStatus);
                            resultIntent.putExtra(KEY_NAME_RESULT_DETAIL, UNINSTALL_RESULT_DETAIL_ERROR_APP_NOT_EXIST);
                            context.sendBroadcast(resultIntent);
                            Log.d(TAG, "ServerBroadcastReceiver uninstallApp: app not exist!!, pkg name = " + uninstallPkgName);
                        }
                    }
                }
            }
        }

        private void setWhitelistOnOff(Context context, Intent intent)
        {
            //adb shell am broadcast -a pesi.broadcast.action.device.manager --ei INSTRUCTION_ID 203 --ez WHITE_LIST_SWITCH true
            String resultDetail = null;
            boolean isWhitelistOn = intent.getBooleanExtra(KEY_NAME_WHITELIST_ONOFF, false);
            boolean isWhitelistOnOffFixSuccess = mWhitelistPref.setWhitelistOnOff(isWhitelistOn);
            int resultStatus;

            if (isWhitelistOnOffFixSuccess)
            {
                resultStatus = RESULT_STATUS_SUCCESS;
                if (isWhitelistOn)
                    checkAllInstalledApps();
            }
            else
            {
                resultStatus = RESULT_STATUS_FAILURE;
                resultDetail = RESULT_DETAIL_SAVE_FAILURE;
            }

            if (Utility.canSendBroadcast(context))
            {
                Intent resultIntent = new Intent(mTargetAction);
                resultIntent.setPackage(mTargetPkgName);
                resultIntent.putExtra(
                        KEY_NAME_INSTRUCTION_ID,
                        InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.ID);
                resultIntent.putExtra(
                        KEY_NAME_EXECUTE_INSTRUCTION_ID,
                        InterfaceString.INSTRUCTION_LIST.UI_Management.SET_WHITELIST_ONOFF.ID);
                resultIntent.putExtra(KEY_NAME_WHITELIST_ONOFF, mWhitelistPref.getWhitelistOnOff());
                resultIntent.putExtra(KEY_NAME_RESULT_STATUS, resultStatus);
                resultIntent.putExtra(KEY_NAME_RESULT_DETAIL, resultDetail);
                context.sendBroadcast(resultIntent);
            }
            Log.d(TAG, "ServerBroadcastReceiver onReceive: getWhitelistOnOff = " + mWhitelistPref.getWhitelistOnOff());
            Log.d(TAG, "ServerBroadcastReceiver onReceive: is whitelist OnOff fix success? " + isWhitelistOnOffFixSuccess);
        }

        private void setWhitelist(Context context, Intent intent)
        {
            //adb shell am broadcast -a pesi.broadcast.action.device.manager --ei INSTRUCTION_ID 204 --esal SET_WHITELIST com.csdroid.pkg,net.fetnet.fetvod.tv,com.iqiyi.i18n.tv,flar2.homebutton,com.google.android.exoplayer2.demo,com.actions.voicebletest.ota --ez APPEND true
            ArrayList<String> prefWhiteList = mWhitelistPref.getWhitelist();
            ArrayList<String> samePkgNameList = new ArrayList<>();
            ArrayList<String> whiteList = intent.getStringArrayListExtra(KEY_NAME_SET_WHITELIST);
            String resultDetail = null;
            boolean append = intent.getBooleanExtra(KEY_NAME_APPEND, false);
            boolean isSetWhiteListFixSuccess = false;
            int resultStatus = RESULT_STATUS_FAILURE;


            if (append && (whiteList == null || whiteList.size() == 0))
            {
                resultStatus = RESULT_STATUS_FAILURE;
                resultDetail = SET_WHITELIST_RESULT_DETAIL_APPEND_EMPTY;
            }

            if (append && whiteList != null && whiteList.size() > 0)
            {
                boolean isAppendSuccess = false;
                for (String pkgname : whiteList)
                {
                    if (!prefWhiteList.contains(pkgname))
                    {
                        prefWhiteList.add(pkgname);
                        if (!isAppendSuccess)
                        {
                            isAppendSuccess = true;
                            resultDetail = SET_WHITELIST_RESULT_DETAIL_APPEND_SUCCESS;
                        }
                    }
                    else
                    {
                        samePkgNameList.add(pkgname);
                        resultDetail = SET_WHITELIST_RESULT_DETAIL_APPEND_AUTO_MERGE;
                    }
                }
                isSetWhiteListFixSuccess = mWhitelistPref.setWhitelist(prefWhiteList);
            }
            else
            {
                if (whiteList == null)
                    whiteList = new ArrayList<>();
                isSetWhiteListFixSuccess = mWhitelistPref.setWhitelist(whiteList);
                resultDetail = SET_WHITELIST_RESULT_DETAIL_COVER_SUCCESS;
            }

            if (isSetWhiteListFixSuccess)
            {
                boolean isWhitelistOn = mWhitelistPref.getWhitelistOnOff();
                resultStatus = RESULT_STATUS_SUCCESS;
                if (isWhitelistOn)
                    checkAllInstalledApps();
            }
            else
            {
                resultStatus = RESULT_STATUS_FAILURE;
                resultDetail = RESULT_DETAIL_SAVE_FAILURE;
            }

            if (Utility.canSendBroadcast(context))
            {
                Intent resultIntent = new Intent(mTargetAction);
                resultIntent.setPackage(mTargetPkgName);
                resultIntent.putExtra(
                        KEY_NAME_INSTRUCTION_ID,
                        InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.ID);
                resultIntent.putExtra(
                        KEY_NAME_EXECUTE_INSTRUCTION_ID,
                        InterfaceString.INSTRUCTION_LIST.UI_Management.SET_WHITELIST.ID);
                resultIntent.putStringArrayListExtra(KEY_NAME_CURRENT_WHITELIST, mWhitelistPref.getWhitelist());
                resultIntent.putExtra(KEY_NAME_APPEND, append);
                resultIntent.putExtra(KEY_NAME_RESULT_STATUS, resultStatus);
                resultIntent.putExtra(KEY_NAME_RESULT_DETAIL, resultDetail);
                resultIntent.putStringArrayListExtra(KEY_NAME_APPEND_SAME_PKGNAME_LIST, samePkgNameList);
                context.sendBroadcast(resultIntent);
            }

            if (DEBUG)
            {
                Log.d(TAG, "ServerBroadcastReceiver onReceive: is set whitelist fix success? " + isSetWhiteListFixSuccess);
                Log.d(TAG, "ServerBroadcastReceiver onReceive: CURRENT_WHITELIST = " + mWhitelistPref.getWhitelist());
                Log.d(TAG, "ServerBroadcastReceiver onReceive: append = " + append);
            }
        }

        private void deleteWhitelist(Context context, Intent intent)
        {
            //adb shell am broadcast -a pesi.broadcast.action.device.manager --ei INSTRUCTION_ID 205 --esal DELETE_WHITELIST com.csdroid.pkg,net.fetnet.fetvod.tv,com.iqiyi.i18n.tv,flar2.homebutton,com.google.android.exoplayer2.demo,com.actions.voicebletest.ota
            ArrayList<String> prefWhitelist = mWhitelistPref.getWhitelist();
            ArrayList<String> pkgNameNotFoundList = new ArrayList<>();
            ArrayList<String> deletedWhitelist = new ArrayList<>();
            ArrayList<String> whiteList = intent.getStringArrayListExtra(KEY_NAME_DELETE_WHITELIST);
            int resultStatus = RESULT_STATUS_FAILURE;
            String resultDetail = null;
            if (whiteList == null || whiteList.size() == 0)
            {
                resultStatus = RESULT_STATUS_FAILURE;
                resultDetail = DELETE_WHITELIST_RESULT_DETAIL_DELETED_EMPTY;
            }
            else
            {
                for (String pkgName : whiteList)
                {
                    if (prefWhitelist.contains(pkgName))
                    {
                        prefWhitelist.remove(pkgName);
                        deletedWhitelist.add(pkgName);
                    }
                    else
                        pkgNameNotFoundList.add(pkgName);
                }
                if (pkgNameNotFoundList.size() > 0)
                {
                    resultStatus = RESULT_STATUS_FAILURE;
                    resultDetail = DELETE_WHITELIST_RESULT_DETAIL_PKGNAME_NOT_FOUND;
                }
                else
                    resultStatus = RESULT_STATUS_SUCCESS;
            }

            boolean isDeleteWhiteListFixSuccess = mWhitelistPref.setWhitelist(prefWhitelist);
            if (!isDeleteWhiteListFixSuccess)
            {
                resultStatus = RESULT_STATUS_FAILURE;
                resultDetail = RESULT_DETAIL_SAVE_FAILURE;
            }

            if (Utility.canSendBroadcast(context))
            {
                Intent resultIntent = new Intent(mTargetAction);
                resultIntent.setPackage(mTargetPkgName);
                resultIntent.putExtra(
                        KEY_NAME_INSTRUCTION_ID,
                        InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.ID);
                resultIntent.putExtra(
                        KEY_NAME_EXECUTE_INSTRUCTION_ID,
                        InterfaceString.INSTRUCTION_LIST.UI_Management.DELETE_WHITELIST.ID);
                resultIntent.putStringArrayListExtra(KEY_NAME_DELETED_WHITELIST, deletedWhitelist);
                resultIntent.putStringArrayListExtra(KEY_NAME_CURRENT_WHITELIST, mWhitelistPref.getWhitelist());
                resultIntent.putStringArrayListExtra(KEY_NAME_PKGNAMES_NOT_FOUND_LIST, pkgNameNotFoundList);
                resultIntent.putExtra(KEY_NAME_RESULT_STATUS, resultStatus);
                resultIntent.putExtra(KEY_NAME_RESULT_DETAIL, resultDetail);
                context.sendBroadcast(resultIntent);
            }
        }

        private void deleteAllWhitelist(Context context)
        {
            //adb shell am broadcast -a pesi.broadcast.action.device.manager --ei INSTRUCTION_ID 206

            ArrayList<String> deletedWhitelist = mWhitelistPref.getWhitelist();
            String resultDetail = null;
            boolean isDeleteAllWhiteListFixSuccess = mWhitelistPref.setWhitelist(new ArrayList<String>());
            int resultStatus;

            if (isDeleteAllWhiteListFixSuccess)
                resultStatus = RESULT_STATUS_SUCCESS;
            else
            {
                resultStatus = RESULT_STATUS_FAILURE;
                resultDetail = RESULT_DETAIL_SAVE_FAILURE;
            }

            if (Utility.canSendBroadcast(context))
            {
                Intent resultIntent = new Intent(mTargetAction);
                resultIntent.setPackage(mTargetPkgName);
                resultIntent.putExtra(
                        KEY_NAME_INSTRUCTION_ID,
                        InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.ID);
                resultIntent.putExtra(
                        KEY_NAME_EXECUTE_INSTRUCTION_ID,
                        InterfaceString.INSTRUCTION_LIST.UI_Management.DELETE_ALL_WHITELIST.ID);
                resultIntent.putStringArrayListExtra(KEY_NAME_DELETED_WHITELIST, deletedWhitelist);
                resultIntent.putExtra(KEY_NAME_RESULT_STATUS, resultStatus);
                resultIntent.putExtra(KEY_NAME_RESULT_DETAIL, resultDetail);
                context.sendBroadcast(resultIntent);
            }
        }

        private void getWhitelist(Context context)
        {
            //adb shell am broadcast -a pesi.broadcast.action.device.manager --ei INSTRUCTION_ID 207
            if (Utility.canSendBroadcast(context))
            {
                int resultStatus = RESULT_STATUS_FAILURE;
                String resultDetail = null;
                ArrayList<String> prefWhitelist = mWhitelistPref.getWhitelist();
                if (prefWhitelist.size() == 0)
                    resultDetail = GET_WHITELIST_RESULT_DETAIL_EMPTY_WHITELIST;
                resultStatus = RESULT_STATUS_SUCCESS;
                Intent resultIntent = new Intent(mTargetAction);
                resultIntent.setPackage(mTargetPkgName);
                resultIntent.putExtra(
                        KEY_NAME_INSTRUCTION_ID,
                        InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.ID);
                resultIntent.putExtra(
                        KEY_NAME_EXECUTE_INSTRUCTION_ID,
                        InterfaceString.INSTRUCTION_LIST.UI_Management.GET_WHITELIST.ID);
                resultIntent.putStringArrayListExtra(KEY_NAME_CURRENT_WHITELIST, prefWhitelist);
                resultIntent.putExtra(KEY_NAME_WHITELIST_ONOFF, mWhitelistPref.getWhitelistOnOff());
                resultIntent.putExtra(KEY_NAME_RESULT_STATUS, resultStatus);
                resultIntent.putExtra(KEY_NAME_RESULT_DETAIL, resultDetail);
                context.sendBroadcast(resultIntent);
            }
        }

        private void setReturnTarget(Context context, Intent intent)
        {
            //adb shell am broadcast -a pesi.broadcast.action.device.manager --ei INSTRUCTION_ID 208 -e RETURN_TARGET_ACTION pesi.broadcast.action.device.appDownloadManager -e RETURN_TARGET_PKGNAME com.prime.appDownloadManager
            String targetAction = intent.getStringExtra(KEY_NAME_RETURN_TARGET_ACTION);
            String targetPkgName = intent.getStringExtra(KEY_NAME_RETURN_TARGET_PKGNAME);
            boolean isSetReturnTargetSuccess = mWhitelistPref.setTarget(targetAction, targetPkgName);
            int resultStatus;
            String resultDetail = null;

            if (isSetReturnTargetSuccess)
            {
                resultStatus = RESULT_STATUS_SUCCESS;
                mTargetAction = targetAction;
                mTargetPkgName = targetPkgName;
            }
            else
            {
                resultStatus = RESULT_STATUS_FAILURE;
                resultDetail = RESULT_DETAIL_SAVE_FAILURE;
            }

            if (Utility.canSendBroadcast(context))
            {
                Intent resultIntent = new Intent(mTargetAction);
                resultIntent.setPackage(mTargetPkgName);
                resultIntent.putExtra(
                        KEY_NAME_INSTRUCTION_ID,
                        InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.ID);
                resultIntent.putExtra(
                        KEY_NAME_EXECUTE_INSTRUCTION_ID,
                        InterfaceString.INSTRUCTION_LIST.UI_Management.SET_RETURN_TARGET.ID);
                resultIntent.putExtra(KEY_NAME_RETURN_TARGET_ACTION, mTargetAction);
                resultIntent.putExtra(KEY_NAME_RETURN_TARGET_PKGNAME, mTargetPkgName);
                resultIntent.putExtra(KEY_NAME_RESULT_STATUS, resultStatus);
                resultIntent.putExtra(KEY_NAME_RESULT_DETAIL, resultDetail);
                context.sendBroadcast(resultIntent);
            }
        }
    };

    /*private String[] getWhiteList()
    {
        return new String[]{
                "com.csdroid.pkg",
                "net.fetnet.fetvod.tv",
                "com.iqiyi.i18n.tv",
                "flar2.homebutton",
                "com.google.android.exoplayer2.demo",
                "com.actions.voicebletest.ota"};
    }*/

    private boolean isInstallableApk(String pkgName)
    {
        List<ApplicationInfo> appInfos = mPkgManager.getInstalledApplications(0);
        boolean isWhitelistOn = mWhitelistPref.getWhitelistOnOff();
        for (int i = 0; i < appInfos.size(); i++)
        {
            if (pkgName.equals(appInfos.get(i).packageName))
            {
                ApplicationInfo appInfo = appInfos.get(i);
                if((appInfo.flags // It is a system app
                        & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP | ApplicationInfo.FLAG_SYSTEM)) > 0)
                {
                    if (DEBUG)
                        Log.d(TAG, "IsInstallableApk: It is a system app, pkgName = " + appInfo.packageName);
                    return true;
                }
                else // It is installed by the user
                {
                    if (DEBUG)
                        Log.d(TAG, "IsInstallableApk: It is installed by the user, pkgName = " + appInfo.packageName);
                    if (isWhitelistOn)
                        return isWhiteList(pkgName);
                    else
                        return true;
                }
            }
        }
        Log.d(TAG, "isInstallableApk: the package name is not found in app list. pkgName = " + pkgName);
        if (isWhitelistOn)
            return isWhiteList(pkgName);
        else
            return true;
    }

    private boolean isWhiteList(String pkgName)
    {
        ArrayList<String> whitelist = mWhitelistPref.getWhitelist();
        for (String whitePkgName : whitelist)
        {
            if (pkgName.equals(whitePkgName))
            {
                if (DEBUG)
                    Log.d(TAG, "IsWhiteList: pkgName is found in White List.");
                return true;
            }
        }
        if (DEBUG)
            Log.d(TAG, "IsWhiteList: pkgName not found in White List.");
        return false;
    }

    private final BroadcastReceiver mAppsListener = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            //intent.getDataString() = "package:com.prime.xxx"
            String pkgName = intent.getDataString().substring(APP_DATA_SCHEME_TITLE_LENGTH);
            Log.d(TAG, "AppsListener onReceive: action = " + action);
            if (action.equals(Intent.ACTION_PACKAGE_ADDED))
            {
                boolean isWhitelistOn = mWhitelistPref.getWhitelistOnOff();
                if (DEBUG)
                    Log.d(TAG, "AppsListener onReceive: ACTION_PACKAGE_ADDED, pkgName = " + pkgName);
                if (isWhitelistOn && !isInstallableApk(pkgName))
                {
                    mUpdateAppProcessHandle.uninstallApp(pkgName);
                }
            }
            else if (action.equals(Intent.ACTION_PACKAGE_FULLY_REMOVED))
            {
                if (DEBUG)
                    Log.d(TAG, "AppsListener onReceive: ACTION_PACKAGE_FULLY_REMOVED, pkgName = " + pkgName);

                // Toast.makeText(context, "ACTION_PACKAGE_FULLY_REMOVED, package name = " + pkgName, Toast.LENGTH_LONG).show();
            }
            else
            {
                if (DEBUG)
                    Log.d(TAG, "AppsListener onReceive: the action not define.");
            }
        }
    };

    private void checkAllInstalledApps()
    {
        List<ApplicationInfo> apps = getPackageManager().getInstalledApplications(0);
        String pkgName;
        for (ApplicationInfo app : apps)
        {
            pkgName = app.packageName;
            if (!isInstallableApk(pkgName))
            {
                Log.d(TAG, "checkAllInstalledApps: uninstallApp pkgName = " + pkgName);
                mUpdateAppProcessHandle.uninstallApp(pkgName);
            }
        }
    }
}
