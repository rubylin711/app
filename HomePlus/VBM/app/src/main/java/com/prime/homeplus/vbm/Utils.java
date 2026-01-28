package com.prime.homeplus.vbm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.prime.datastructure.config.PropertyDefine;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    // 對齊 Benchmark Property.SO_CRMID / Property.VBM_URL
    public static String getSoId(boolean padWithZero){
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        return "20";
    }

    public static String getStbId() {
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        return "12345678";
    }
   /**
     * v1.29 Agent 9 欄位 Value_0 / Agent 10 可能會使用 Smartcard number。
     * HomePlus Benchmark 由 DTVKit CA 取得；Prime 端若沒有對外 API，可先回傳 N/A。
     *
     * 若 STB vendor 有提供 system property / content provider，請在此處補齊。
     */
    public static String getSmartCardNumber(Context context) {
        // Possible vendor properties (best-effort). If none, return N/A.
        return "N/A";
    }
    private static String getSystemProperty(String key, String def) {
        try {
            Class<?> sp = Class.forName("android.os.SystemProperties");
            Method get = sp.getMethod("get", String.class, String.class);
            return (String) get.invoke(null, key, def);
        } catch (Throwable t) {
            return def;
        }
    }
}
