package com.prime.dmg.launcher.Utils.System;

import android.os.SystemProperties;

import com.prime.dtv.config.Pvcfg;

public class LiAdUtils {
    private static String LIAD_PACKAGE_NAME = "com.litv.interstital";
    private static String LIAD_CLASS_NAME = "com.litv.sdk.apk.bootup.ActivityIntentMode";

    private static String TBC_LIAD_API_KEY = "TBCATV-UFwK6APapdDQtlGr";
    private static String DMG_LIAD_API_KEY = "DMGATV-ZoHj8pSP70I96XTV";

    private static String TBC_LIAD_APP_BUNDLE = "com.tbcatv.home";
    private static String DMG_LIAD_APP_BUNDLE  = "com.dmgatv.home";

    private static boolean HAS_TOAST = true;
    private static boolean HAS_LOG = true;
    private static String LITV_ENV = "Staging";

    public static String get_api_key(){
        if(Pvcfg.getModuleType() == Pvcfg.MODULE_DMG)
            return DMG_LIAD_API_KEY;
        return TBC_LIAD_API_KEY;
    }

    public static String get_app_bundle(){
        if(Pvcfg.getModuleType() == Pvcfg.MODULE_DMG)
            return DMG_LIAD_APP_BUNDLE;
        return TBC_LIAD_APP_BUNDLE;
    }

    public static String get_packagename(){
        return LIAD_PACKAGE_NAME;
    }

    public static String get_classname(){
        return LIAD_CLASS_NAME;
    }

    public static boolean isHasToast() {
        return Pvcfg.isLiADDebug();
    }

    public static boolean isHasLog() {
        return HAS_LOG;
    }

    public static String getLitvEnv() {
        return LITV_ENV;
    }

    public static String getOperateSo(){
        String so = SystemProperties.get("persist.sys.prime.so", "DF");
        return so;
    }
}
