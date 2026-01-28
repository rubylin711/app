package com.prime.dtv.service.Table.Desciptor;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public abstract class DescBase {
    public static final int MAX_STRING_LENGTH_TABLES = 50;

    // descriptor_tag + descriptor_length
    // these 2 bytes apply to all descriptors
    protected static final int MIN_DESCRIPTOR_LENGTH = 2;

    public int Tag;
    public int Length;
    public boolean DataExist;
    public boolean DebugMsgEnable;
    public abstract void Parsing(byte[] data,int lens);
    public void LogPrint(String tag, String msg) {
        if(DebugMsgEnable) {
            Log.d(tag,msg);
        }
    }

    public int bcd_to_bin(int bcd)
    {
        return (((bcd & 0xf0000000) >> 28) * 10000000 +
                ((bcd & 0xf000000) >> 24) * 1000000 +
                ((bcd & 0xf00000) >> 20) * 100000 +
                ((bcd & 0xf0000) >> 16) * 10000 +
                ((bcd & 0xf000) >> 12) * 1000 +
                ((bcd & 0xf00) >> 8) * 100 +
                ((bcd & 0xf0) >> 4) * 10 +
                (bcd & 0xf));
    }

    public Date get_utc_time (int time_m, int time_l)
    {
        Calendar calendar = Calendar.getInstance();
        int	y, y1, m, m1, k, mjd;

// mjd is the 16-bit value from time_of_change_msb
        mjd = time_m & 0x000fffff;

// 轉換成年月日
        int j = mjd + 2400001 + 68569;
        int c = 4 * j / 146097;
        j = j - (146097 * c + 3) / 4;
        y1 = 4000 * (j + 1) / 1461001;
        j = j - 1461 * y1 / 4 + 31;
        m1 = 80 * j / 2447;
        int d = j - 2447 * m1 / 80;
        j = m1 / 11;
        m = m1 + 2 - 12 * j;
        y = 100 * (c - 49) + y1 + j;

// 時間（BCD）
        int hour = bcd_to_bin ((time_l & 0xff0000) >> 16);
        int minute = bcd_to_bin ((time_l & 0xff00) >> 8);
        int second = bcd_to_bin (time_l & 0xff);

// 設定時間
        calendar.set(Calendar.YEAR, y);
        calendar.set(Calendar.MONTH, m - 1); // Java 的月份是從 0 開始
        calendar.set(Calendar.DAY_OF_MONTH, d);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);

// 轉成 Date
        Date changeTime = calendar.getTime();

        return changeTime;
    }
}
