package com.prime.homeplus.settings;

import android.content.Context;
import android.os.SystemProperties;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.GposInfo;

public class InfoUtils {

    public static String getSoId(Context context, boolean padWithZero){
        GposInfo gposInfo = PrimeUtils.get_gpos_info();
        if(gposInfo != null){
            return GposInfo.getSo(context);
        }
        LogUtils.e("Can't get Gpos !!!!!!!!!!!!!!!!!!!!!!!");
        return "20";
    }

    public static String getCardSN() {
        return Pvcfg.get_device_sn();
    }

    public static String getSN() {
        return Pvcfg.get_device_sn();
    }
}
