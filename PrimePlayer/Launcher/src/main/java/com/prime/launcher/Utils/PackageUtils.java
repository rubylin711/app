package com.prime.launcher.Utils;

import static com.prime.launcher.Utils.ActivityUtils.get_all_app;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.prime.launcher.Home.Recommend.Installer.InstallData;
import com.prime.launcher.Receiver.PrimeAppReceiver;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class PackageUtils {

    static String TAG = "PackageUtils";

    public static String get_package_name(File apkFile) {
        try {
            PackageParser packageParser = new PackageParser();
            PackageParser.Package pkg = packageParser.parsePackage(apkFile, 0);
            return pkg.packageName;
        } catch (PackageParser.PackageParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean is_installed(Context context, String pkgName) {
        for (ResolveInfo resolveInfo : get_all_app(context, pkgName)) {
            if (resolveInfo.activityInfo.packageName.equals(pkgName)) {
                Log.i(TAG, "is_installed: [package] " + pkgName + ", [installed] " + true);
                return true;
            }
        }
        Log.i(TAG, "is_installed: [package] " + pkgName + ", [installed] " + false);
        return false;
    }

    public static boolean is_update(Context context, InstallData installData) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(installData.getPkgName(), 0);
            Log.i(TAG, "is_update: installData [version code] " + installData.getVersionCode());
            Log.i(TAG, "is_update: packageInfo [version code] " + packageInfo.getLongVersionCode());
            return installData.getVersionCode() > packageInfo.getLongVersionCode();
        }
        catch (Exception e) {
            Log.i(TAG, "is_update: [can update] " + false + ", [exception] " + e);
        }
        return false;
    }

    public static void install_apk(Context context, File apkFile) {
        try {
            Log.i(TAG, "install_apk: [file path] " + apkFile.getAbsolutePath());
            PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.setAppPackageName(PackageUtils.get_package_name(apkFile));

            int sessionId = packageInstaller.createSession(params);
            PackageInstaller.Session session = packageInstaller.openSession(sessionId);

            try (OutputStream out = session.openWrite("app_install", 0, apkFile.length());
                 InputStream in = Files.newInputStream(apkFile.toPath())) {
                byte[] buffer = new byte[65536];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                session.fsync(out);
            }

            Intent intent = new Intent(context, PrimeAppReceiver.class);
            intent.setAction(PackageInstaller.ACTION_SESSION_UPDATED);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            session.commit(pendingIntent.getIntentSender());
        }
        catch (Exception e) {
            if (e.getCause() != null)
                e.getCause().printStackTrace();
            else
                e.printStackTrace();
        }
    }
}
