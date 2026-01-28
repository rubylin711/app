package com.prime.appDownloadManager.UpdateApp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Build;
import android.util.Log;

import com.prime.appDownloadManager.ServerCommunicate.BackendManagementService;
import com.prime.appDownloadManager.ServerCommunicate.InterfaceString;
import com.prime.appDownloadManager.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.KEY_NAME_INSTRUCTION_ID;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.INSTALL_APK.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.RESULT_STATUS.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.SET_RETURN_TARGET.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.UNINSTALL_APK.*;

public class UpdateAppProcess
{
    private static final String TAG = "UpdateAppProcess";
    private static final boolean DEBUG = true;
    private static final String PACKAGE_INSTALLED_UNINSTALLED_ACTION =
            "com.prime.android.apis.content.SESSION_API_PACKAGE_INSTALLED_UNINSTALLED";
    public static final String KEY_NAME_PKG_NAME = "PKG_NAME";
    private WeakReference<Context> mContextRef = null;
    private DownloadFile mDownloadFile = null;
    private static int pre_enabled_setting = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT ;
    private static boolean isPlayStoreCanDisalbe = true;
    private static final Object lock = new Object();

    public UpdateAppProcess(Context context)
    {
        mContextRef = new WeakReference<>(context);
        DownloadMsgHandler handler = new DownloadMsgHandler(context, Looper.getMainLooper());
        mDownloadFile = new DownloadFile(mContextRef.get(), handler);
    }

    public void deinit()
    {
        mDownloadFile.deinit();
    }
    public void startProcess(String pkgName, String downloadPath)
    {
        //download app form internet to local first
        if (DEBUG)
            Log.d(TAG, "package name : " + pkgName + " download path : " + downloadPath);
		if(downloadPath != null && downloadPath.length() !=0)
        	mDownloadFile.startDownload(pkgName, downloadPath);
    }

    private static class DownloadMsgHandler extends Handler
    {
        private WeakReference<Context> contextRef = null;

        public DownloadMsgHandler(Context context, Looper mainLooper)
        {
            super(mainLooper);
            contextRef = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg)
        {
            Log.d(TAG, "Receiver download msg Id : " + msg.what);
            switch (msg.what)
            {
                case DownloadFile.MSG_DOWNLOAD_COMPLETE:
                    String downloadFile = msg.getData().getString(KEY_NAME_DOWNLOAD_FILE_PATH);
                    String installPkgName = msg.getData().getString(KEY_NAME_INSTALL_PKGNAME);
                    String targetAction = msg.getData().getString(KEY_NAME_RETURN_TARGET_ACTION);
                    String targetPkg = msg.getData().getString(KEY_NAME_RETURN_TARGET_PKGNAME);
                    if (DEBUG)
                    {
                        Log.d(TAG, "handleMessage: download complete file : " + downloadFile);
                        Log.d(TAG, "handleMessage: installPkgName = " + installPkgName);
                        Log.d(TAG, "handleMessage: targetAction = " + targetAction);
                        Log.d(TAG, "handleMessage: targetPkg = " + targetPkg);
                    }
                    installApp(downloadFile, installPkgName);
                    break;
            }
        }

        private void installApp(String downloadFile,String installPkgName)
        {
            PackageInstaller.Session session = null;
            try
            {
                PackageInstaller pkgInstaller = contextRef.get().getPackageManager().getPackageInstaller();
                PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                        PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                int sessionId = pkgInstaller.createSession(params);
                session = pkgInstaller.openSession(sessionId);

                if (downloadFile == null)
                {
                    Log.d(TAG, "reallyInstallApp: download file is null");
                    return;
                }
                File appFile = new File(downloadFile);
                //if (checkIsAppFile(appFile))
                {
                    addApkToInstallSession(appFile, session);

                    // Commit the session (this will start the installation workflow).
                    session.commit(createStatusReceiver(
                            contextRef.get(),
                            installPkgName,
                            downloadFile,
                            InterfaceString.INSTRUCTION_LIST.UI_Management.INSTALL_APK.ID));
                }
            }
            catch (IOException e)
            {
                Log.e(TAG, "reallyInstallApp: " + Log.getStackTraceString(e));
                throw new RuntimeException("Couldn't install package", e);
            }
            catch (RuntimeException e)
            {
                Log.e(TAG, "reallyInstallApp: " + Log.getStackTraceString(e));
                if (session != null)
                    session.abandon();
                throw e;
            }
        }

        private void addApkToInstallSession(File appFile, PackageInstaller.Session session)
                throws IOException
        {
            //Log.d( TAG, "filepath = " + appFile.getPath());
            // It's recommended to pass the file size to openWrite(). Otherwise installation may fail
            // if the disk is almost full.
            try (OutputStream packageInSession = session.openWrite("package", 0, -1);
                 FileInputStream fis = new FileInputStream(appFile))
            {
                byte[] buffer = new byte[16384];//16KB
                int n;
                while ((n = fis.read(buffer)) >= 0)
                {
                    packageInSession.write(buffer, 0, n);
                }
            }
        }
    }

    public void uninstallApp(String pkgName)
    {
        PackageInstaller pkgInstaller = mContextRef.get().getPackageManager().getPackageInstaller();
        pkgInstaller.uninstall(
                pkgName,
                createStatusReceiver(
                        mContextRef.get(),
                        pkgName,
                        null,
                        InterfaceString.INSTRUCTION_LIST.UI_Management.UNINSTALL_APK.ID));
    }

    private  boolean checkIsAppFile(File appFile)
    {
        if (!appFile.exists())
            return false ;

        PackageInfo packageInfo = mContextRef.get().getPackageManager().getPackageArchiveInfo(appFile.getAbsolutePath(), 0);
        // you can be sure that it is a valid apk
        return packageInfo != null;
    }

    private static IntentSender createStatusReceiver(Context context, String pkgName, String downloadFile, int executeId)
    {
        // Create an install status receiver.
        //Intent intent = new Intent(mContext, com.prime.appDownloadManager.UpdateApp.UpdateAppProcess.class);
        Intent intent = new Intent(context, InstalledUninstalledReceiver.class);
        //給 PendingIntent 區分 intent 為不同的 intent (方法1)
        //intent.setData(Uri.parse("custom://"+System.currentTimeMillis()));
        intent.setAction(PACKAGE_INSTALLED_UNINSTALLED_ACTION);
        intent.putExtra(KEY_NAME_DOWNLOAD_FILE_PATH, downloadFile);
        intent.putExtra(KEY_NAME_EXECUTE_INSTRUCTION_ID, executeId);

        if (executeId == InterfaceString.INSTRUCTION_LIST.UI_Management.INSTALL_APK.ID)
        {
            intent.putExtra(KEY_NAME_INSTALL_PKGNAME, pkgName);
            ForceDisablelPlayStore(context, false);
        }
        else if (executeId == InterfaceString.INSTRUCTION_LIST.UI_Management.UNINSTALL_APK.ID)
            intent.putExtra(KEY_NAME_UNINSTALL_PKGNAME, pkgName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) System.currentTimeMillis(),//給 PendingIntent 區分 intent 為不同的 intent (方法2)
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        IntentSender statusReceiver = pendingIntent.getIntentSender();
        if (DEBUG)
        {
            Log.d(TAG, "createStatusReceiver: pkgName = " + pkgName);
            Log.d(TAG, "createStatusReceiver: filePath = " + downloadFile);
        }
        return statusReceiver;
    }

    // Note: this Activity must run in singleTop launchMode for it to be able to receive the intent
    // in onNewIntent().
    /*
    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (PACKAGE_INSTALLED_ACTION.equals(intent.getAction())) {
            int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
            String message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);

            switch (status) {
                case PackageInstaller.STATUS_PENDING_USER_ACTION:
                    // This test app isn't privileged, so the user has to confirm the install.
                    Intent confirmIntent = (Intent) extras.get(Intent.EXTRA_INTENT);
                    mContext.startActivity(confirmIntent);
                    break;

                case PackageInstaller.STATUS_SUCCESS:
                    Toast.makeText(mContext, "Install succeeded!", Toast.LENGTH_SHORT).show();
                    break;

                case PackageInstaller.STATUS_FAILURE:
                case PackageInstaller.STATUS_FAILURE_ABORTED:
                case PackageInstaller.STATUS_FAILURE_BLOCKED:
                case PackageInstaller.STATUS_FAILURE_CONFLICT:
                case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                case PackageInstaller.STATUS_FAILURE_INVALID:
                case PackageInstaller.STATUS_FAILURE_STORAGE:
                    Toast.makeText(mContext, "Install failed! " + status + ", " + message,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(mContext, "Unrecognized status received from installer: " + status,
                            Toast.LENGTH_SHORT).show();
            }
        }
    }
    */

    public static class InstalledUninstalledReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (DEBUG)
                Log.d(TAG, "InstalledUninstalledReceiver onReceive: action = " + action);
            if (PACKAGE_INSTALLED_UNINSTALLED_ACTION.equals(action))
            {
                Bundle extras = intent.getExtras();
                String message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);
                String pkgName = null;
                final String filePath = intent.getStringExtra(KEY_NAME_DOWNLOAD_FILE_PATH);
                int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
                int resultStatus = formatStatus(status);
                int executeId = intent.getIntExtra(KEY_NAME_EXECUTE_INSTRUCTION_ID, -1);

                if (executeId == InterfaceString.INSTRUCTION_LIST.UI_Management.INSTALL_APK.ID)
                {
                    pkgName = intent.getStringExtra(KEY_NAME_INSTALL_PKGNAME);
                    ForceDisablelPlayStore(context, true);
                }
                else if (executeId == InterfaceString.INSTRUCTION_LIST.UI_Management.UNINSTALL_APK.ID)
                    pkgName = intent.getStringExtra(KEY_NAME_UNINSTALL_PKGNAME);

                if (Utility.canSendBroadcast(context))
                {
                    Intent resultIntent = null;
                    resultIntent = new Intent(BackendManagementService.mTargetAction); //設定廣播 action 識別碼
                    resultIntent.setPackage(BackendManagementService.mTargetPkgName);
                    resultIntent.putExtra(
                            KEY_NAME_INSTRUCTION_ID,
                            InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.ID);
                    resultIntent.putExtra(
                            KEY_NAME_EXECUTE_INSTRUCTION_ID,
                            executeId);
                    resultIntent.putExtra(KEY_NAME_RESULT_DETAIL, message); //設定廣播夾帶參數
                    resultIntent.putExtra(KEY_NAME_RESULT_STATUS, resultStatus);

                    if (executeId == InterfaceString.INSTRUCTION_LIST.UI_Management.INSTALL_APK.ID)
                        resultIntent.putExtra(KEY_NAME_INSTALL_PKGNAME, pkgName);
                    else if (executeId == InterfaceString.INSTRUCTION_LIST.UI_Management.UNINSTALL_APK.ID)
                        resultIntent.putExtra(KEY_NAME_UNINSTALL_PKGNAME, pkgName);
                    else
                        Log.d(TAG, "onReceive: executeId is not defined, executeId = " + executeId);

                    context.sendBroadcast(resultIntent); //發送廣播訊息
                }


                if (filePath != null && !filePath.equals(""))
                {
                    AsyncTask.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Log.d(TAG, "InstalledUninstalledReceiver AsyncTask run: ");
                            File file = new File(filePath);
                            if (!file.delete())
                                Log.d(TAG, "InstalledUninstalledReceiver onReceive: Failed to delete, file path = " + filePath);
                        }
                    });
                }
                if (DEBUG)
                {
                    Log.d(TAG, "InstalledUninstalledReceiver onReceive: message = " + message);
                    Log.d(TAG, "InstalledUninstalledReceiver onReceive: pkgName = " + pkgName);
                    Log.d(TAG, "InstalledUninstalledReceiver onReceive: status = " + status);
                    Log.d(TAG, "InstalledUninstalledReceiver onReceive: result Status = " + resultStatus);
                    Log.d(TAG, "InstalledUninstalledReceiver onReceive: filePath = " + filePath);
                }
            }
        }

        private int formatStatus(int status)
        {
            if (status == STATUS_PENDING_STREAMING)
                return INSTALL_UNINSTALL_RESULT_STATUS_PENDING_STREAMING;
            else if (status == PackageInstaller.STATUS_PENDING_USER_ACTION)
                return INSTALL_UNINSTALL_RESULT_STATUS_PENDING_USER_ACTION;
            else if (status == PackageInstaller.STATUS_SUCCESS)
                return INSTALL_UNINSTALL_RESULT_STATUS_SUCCESS;
            else if (status == PackageInstaller.STATUS_FAILURE)
                return INSTALL_UNINSTALL_RESULT_STATUS_FAILURE;
            else if (status == PackageInstaller.STATUS_FAILURE_BLOCKED)
                return INSTALL_UNINSTALL_RESULT_STATUS_FAILURE_BLOCKED;
            else if (status == PackageInstaller.STATUS_FAILURE_ABORTED)
                return INSTALL_UNINSTALL_RESULT_STATUS_FAILURE_ABORTED;
            else if (status == PackageInstaller.STATUS_FAILURE_INVALID)
                return INSTALL_UNINSTALL_RESULT_STATUS_FAILURE_INVALID;
            else if (status == PackageInstaller.STATUS_FAILURE_CONFLICT)
                return INSTALL_UNINSTALL_RESULT_STATUS_FAILURE_CONFLICT;
            else if (status == PackageInstaller.STATUS_FAILURE_STORAGE)
                return INSTALL_UNINSTALL_RESULT_STATUS_FAILURE_STORAGE;
            else if (status == PackageInstaller.STATUS_FAILURE_INCOMPATIBLE)
                return INSTALL_UNINSTALL_RESULT_STATUS_FAILURE_INCOMPATIBLE;
            else
                return 0;
        }
    }

    private static synchronized void ForceDisablelPlayStore( Context context, boolean set_back_status )
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        {
            PackageManager packageManager = context.getPackageManager() ;
            try {
                synchronized (lock) {
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo("com.android.vending", 0);
                    if (set_back_status) {
                        if (!isPlayStoreCanDisalbe) {
                            packageManager.setApplicationEnabledSetting("com.android.vending", pre_enabled_setting, 0);
                            isPlayStoreCanDisalbe = true;
                        }
                    } else {
                        if (isPlayStoreCanDisalbe) {
                            pre_enabled_setting = packageManager.getApplicationEnabledSetting("com.android.vending");
                            packageManager.setApplicationEnabledSetting("com.android.vending", PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER, 0);
                            isPlayStoreCanDisalbe = false;
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
