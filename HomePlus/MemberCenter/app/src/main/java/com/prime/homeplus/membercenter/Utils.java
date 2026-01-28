package com.prime.homeplus.membercenter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemProperties;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.GposInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    public static String SO_ID_NEW = "persist.sys.inspur.cns.soid";
    public static String getCurrentTime() {
        String time = new SimpleDateFormat("yyyy.MM.dd / HH:mm:ss").format(new Date());

        return time;
    }

    public static String getVodPointDueDate() {
        Calendar mCalendar = Calendar.getInstance();
        int daysInMonth = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        mCalendar.set(Calendar.DAY_OF_MONTH, daysInMonth);
        mCalendar.set(Calendar.HOUR_OF_DAY, 23);
        mCalendar.set(Calendar.MINUTE, 59);
        mCalendar.set(Calendar.SECOND, 59);
        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd / HH:mm:ss");
        String str = df.format(mCalendar.getTime());

        return str;
    }

    public static String getSoId(boolean padWithZero){
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
//        String soId = SystemProperties.get(SO_ID_NEW, "00");
        String soId = GposInfo.getSo(PrimeHomeplusMemberCenterApplication.getInstance());
        return soId;
    }

    public static String getCardSN() {
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        return Pvcfg.get_device_sn();
    }

    public static String getSN() {
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        return Pvcfg.get_device_sn();
    }

    public static boolean isAppInstalled(Context context, String str) {
        try {
            return context.getPackageManager().getApplicationInfo(str, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
