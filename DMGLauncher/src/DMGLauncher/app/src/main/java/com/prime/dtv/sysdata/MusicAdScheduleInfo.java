package com.prime.dtv.sysdata;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.prime.dmg.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.dmg.launcher.Utils.JsonParser.JsonParser;

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

    public static List<MusicAdScheduleInfo> get_music_ad_schedule_info_list(Context context) {
        String musicAdScheduleInfoList = ACSDataProviderHelper.get_acs_provider_data(context, "ad_profile");
        //String musicAdScheduleInfoList = "[\n" + "    {\n" + "        \"EndDate\": \"2022-01-30T16:00:00.000Z\",\n" + "        \"PlayList\": [\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\",\n" + "                \"endDate\": \"2022-01-30T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676876666059.jpg\",\n" + "                \"sequence\": 0,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2021-11-30T16:00:00.000Z\",\n" + "                \"title\": \"數位影音立即購QR Code\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\"\n" + "            },\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\",\n" + "                \"endDate\": \"2022-01-30T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676883894708.jpg\",\n" + "                \"sequence\": 1,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2021-11-30T16:00:00.000Z\",\n" + "                \"title\": \"線上影音加購超優惠\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\"\n" + "            }\n" + "        ],\n" + "        \"StartDate\": \"2021-11-30T16:00:00.000Z\"\n" + "    },\n" + "    {\n" + "        \"EndDate\": \"2022-03-30T16:00:00.000Z\",\n" + "        \"PlayList\": [\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\",\n" + "                \"endDate\": \"2022-03-30T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676876666059.jpg\",\n" + "                \"sequence\": 0,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2022-01-31T16:00:00.000Z\",\n" + "                \"title\": \"數位影音立即購QR Code\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\"\n" + "            },\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\",\n" + "                \"endDate\": \"2022-03-30T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676883894708.jpg\",\n" + "                \"sequence\": 1,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2022-01-31T16:00:00.000Z\",\n" + "                \"title\": \"線上影音加購超優惠\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\"\n" + "            }\n" + "        ],\n" + "        \"StartDate\": \"2022-01-31T16:00:00.000Z\"\n" + "    },\n" + "    {\n" + "        \"EndDate\": \"2022-06-29T16:00:00.000Z\",\n" + "        \"PlayList\": [\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\",\n" + "                \"endDate\": \"2022-06-29T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676876666059.jpg\",\n" + "                \"sequence\": 0,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2022-04-30T16:00:00.000Z\",\n" + "                \"title\": \"數位影音立即購QR Code\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\"\n" + "            },\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\",\n" + "                \"endDate\": \"2022-06-29T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676883894708.jpg\",\n" + "                \"sequence\": 1,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2022-04-30T16:00:00.000Z\",\n" + "                \"title\": \"線上影音加購超優惠\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\"\n" + "            }\n" + "        ],\n" + "        \"StartDate\": \"2022-04-30T16:00:00.000Z\"\n" + "    },\n" + "    {\n" + "        \"EndDate\": \"2022-09-29T16:00:00.000Z\",\n" + "        \"PlayList\": [\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\",\n" + "                \"endDate\": \"2022-09-29T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676876666059.jpg\",\n" + "                \"sequence\": 0,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2022-06-30T16:00:00.000Z\",\n" + "                \"title\": \"數位影音立即購QR Code\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\"\n" + "            },\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\",\n" + "                \"endDate\": \"2022-09-29T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676883894708.jpg\",\n" + "                \"sequence\": 1,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2022-06-30T16:00:00.000Z\",\n" + "                \"title\": \"線上影音加購超優惠\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\"\n" + "            }\n" + "        ],\n" + "        \"StartDate\": \"2022-06-30T16:00:00.000Z\"\n" + "    },\n" + "    {\n" + "        \"EndDate\": \"2024-10-30T16:00:00.000Z\",\n" + "        \"PlayList\": [\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\",\n" + "                \"endDate\": \"2024-10-30T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676876666059.jpg\",\n" + "                \"sequence\": 0,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2024-02-14T16:00:00.000Z\",\n" + "                \"title\": \"數位影音立即購QR Code\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\"\n" + "            },\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\",\n" + "                \"endDate\": \"2024-10-30T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676883894708.jpg\",\n" + "                \"sequence\": 1,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2024-02-14T16:00:00.000Z\",\n" + "                \"title\": \"線上影音加購超優惠\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\"\n" + "            }\n" + "        ],\n" + "        \"StartDate\": \"2024-02-14T16:00:00.000Z\"\n" + "    }\n" + "]";
        //Log.d(TAG, "get_current_category: musicAdScheduleInfoList = " + musicAdScheduleInfoList);

        if (musicAdScheduleInfoList == null)
            return new ArrayList<>();

        return JsonParser.parse_music_ad_schedule_info(musicAdScheduleInfoList);
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
