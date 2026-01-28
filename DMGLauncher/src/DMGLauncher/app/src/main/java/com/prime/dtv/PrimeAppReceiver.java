package com.prime.dtv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class PrimeAppReceiver extends BroadcastReceiver {
    String TAG = getClass().getSimpleName();

    public interface Callback {
        void on_app_install(String pkgName);
        void on_app_uninstall(String pkgName);
    } Callback g_callback;
    private static Callback gSideMenuCallback;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (action != null && extras != null && intent.getData() != null) {
            int pkgUID = extras.getInt(Intent.EXTRA_UID);
            String pkg = intent.getData().getSchemeSpecificPart();

            if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                // APP 已安装
                Log.d(TAG, "App installed: pkgUID = " + pkgUID + ", pkg = " + pkg);
                if (g_callback != null)
                    g_callback.on_app_install(pkg);
                if (gSideMenuCallback != null)
                    gSideMenuCallback.on_app_install(pkg);
            }
            else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                // APP 已移除
                Log.d(TAG, "App uninstalled: pkgUID = " + pkgUID + ", pkg = " + pkg);
                if (g_callback != null)
                    g_callback.on_app_uninstall(pkg);
                if (gSideMenuCallback != null)
                    gSideMenuCallback.on_app_uninstall(pkg);
            }
            else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
                // APP 已更新
                Log.d(TAG, "App updated: pkgUID = " + pkgUID + ", pkg = " + pkg);
            }
        }
    }

    public void register_callback(Callback callback) {
        g_callback = callback;
    }

    public static void register_side_menu_callback(Callback callback) {
        gSideMenuCallback = callback;
    }

    public static void unregister_side_menu_callback() {
        gSideMenuCallback = null;
    }
}
