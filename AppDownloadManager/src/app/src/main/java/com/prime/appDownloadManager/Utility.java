package com.prime.appDownloadManager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import static com.prime.appDownloadManager.ServerCommunicate.BackendManagementService.mTargetAction;
import static com.prime.appDownloadManager.ServerCommunicate.BackendManagementService.mTargetPkgName;

public class Utility
{
    private static final String TAG = "Utility";
    private static final boolean DEBUG = false;

    public static boolean checkAppExist(Context context, String pkgName)
    {
        PackageManager pkgManager = context.getPackageManager();
        final List<PackageInfo> pkgInfoList = pkgManager.getInstalledPackages(0);
        for (PackageInfo pkgInfo : pkgInfoList)
        {
            if (DEBUG)
                Log.d(TAG, "checkAppExist pkg name = " + pkgInfo.packageName);
            if (pkgInfo.packageName.equals(pkgName))
                return true;
        }

        return false ;
    }

    public static boolean canSendBroadcast(Context context)
    {
        boolean appIsExist = checkAppExist(context, mTargetPkgName);
        boolean canSend = mTargetAction != null
                && !mTargetAction.equals("")
                && mTargetPkgName != null
                && !mTargetPkgName.equals("")
                && appIsExist;
        if (DEBUG)
        {
            Log.d(TAG, "canSendBroadcast: canSend = " + canSend);
            Log.d(TAG, "canSendBroadcast: mTargetAction = " + mTargetAction);
            Log.d(TAG, "canSendBroadcast: mTargetPkgName = " + mTargetPkgName);
            Log.d(TAG, "canSendBroadcast: appIsExist = " + appIsExist);
        }

        if (canSend)
            return true;
        else if (!appIsExist && mTargetPkgName != null && !mTargetPkgName.equals(""))
        {
            // Toast.makeText(
            //         context,
            //         "target pkg name not found in all install apps." + "\n"
            //                 + "target pkg name = " + mTargetPkgName,
            //         Toast.LENGTH_LONG).show();
            return false;
        }
        else
        {
            // Toast.makeText(
            //         context,
            //         "target action or target pkg name not found.",
            //         Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
