package com.prime.datastructure.sysdata;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MusicAdScheduleInfo {
    private static final String TAG = "MusicAdScheduleInfo";

    private String g_end_date;
    private String g_start_date;
    private List<MusicAdInfo> g_music_ad_info_list;

    public String get_start_date() {
        return g_start_date;
    }

    public void set_start_date(String startDate) {
        g_start_date = startDate;
    }

    public String get_end_date() {
        return g_end_date;
    }

    public void set_end_date(String endDate) {
        g_end_date = endDate;
    }

    public List<MusicAdInfo> get_music_ad_info_list() {
        return g_music_ad_info_list;
    }

    public void set_music_ad_info_list(List<MusicAdInfo> musicAdInfoList) {
        g_music_ad_info_list = musicAdInfoList;
    }

    public String to_string() {
        return "g_end_date = " + g_end_date + " g_start_date = " + g_start_date + " g_music_ad_info_list = " + g_music_ad_info_list;
    }

    public static List<MusicAdInfo> get_music_ad_info_list_from_schedule(List<MusicAdScheduleInfo> musicAdScheduleInfoList) {
        for (MusicAdScheduleInfo musicAdScheduleInfo: musicAdScheduleInfoList) {
            if (is_current_in_dates(get_date_from_string(musicAdScheduleInfo.get_start_date()), get_date_from_string(musicAdScheduleInfo.get_end_date())))
                return musicAdScheduleInfo.get_music_ad_info_list();
        }
        return new ArrayList<>();
    }

    public static boolean is_current_in_dates(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            Log.e(TAG, "isNowInDates get null date!");
            return false;
        }
        Date date = new Date();
        return date.after(startDate) && date.before(endDate);
    }

    @SuppressLint("SimpleDateFormat")
    public static Date get_date_from_string(String dateString) {
        if (dateString == null || "".equals(dateString)) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
