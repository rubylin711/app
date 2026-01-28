package com.prime.datastructure.utils;

import android.text.TextUtils;

import com.prime.datastructure.sysdata.MusicAdInfo;
import com.prime.datastructure.sysdata.MusicAdScheduleInfo;
import com.prime.datastructure.sysdata.MusicInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {
    public static List<MusicAdScheduleInfo> parse_music_ad_schedule_info(String jsonString) {
        List<MusicAdScheduleInfo> musicAdScheduleInfoList = new ArrayList<>();

        if (TextUtils.isEmpty(jsonString))
            return musicAdScheduleInfoList;

        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                MusicAdScheduleInfo musicAdScheduleInfo = new MusicAdScheduleInfo();
                musicAdScheduleInfo.set_end_date(get_string(object, "EndDate"));
                musicAdScheduleInfo.set_start_date(get_string(object, "StartDate"));

                JSONArray playList = object.getJSONArray("PlayList");
                List<MusicAdInfo> musicAdInfoList = new ArrayList<>();
                for (int j = 0; j < playList.length(); j++) {
                    JSONObject itemObj = playList.getJSONObject(j);
                    MusicAdInfo musicAdInfo = new MusicAdInfo();
                    musicAdInfo.set_ad_url(get_string(itemObj, "adUrl"));
                    musicAdInfo.set_end_date(get_string(itemObj, "endDate"));
                    musicAdInfo.set_md5_checksum(get_string(itemObj, "md5_checksum"));
                    musicAdInfo.set_media_url(get_string(itemObj, "mediaUrl"));
                    musicAdInfo.set_sequence(get_int(itemObj, "sequence"));
                    musicAdInfo.set_source_type(get_string(itemObj, "sourceType"));
                    musicAdInfo.set_start_date(get_string(itemObj, "startDate"));
                    musicAdInfo.set_title(get_string(itemObj, "title"));
                    musicAdInfo.set_type(get_string(itemObj, "type"));
                    musicAdInfo.set_type_name(get_string(itemObj, "typeName"));
                    musicAdInfo.set_url(get_string(itemObj, "url"));
                    //Log.d(TAG, "parse_music_ad_schedule_info: musicAdInfo = " + musicAdInfo.to_string());
                    musicAdInfoList.add(musicAdInfo);
                }
                musicAdScheduleInfo.set_music_ad_info_list(musicAdInfoList);
                //Log.d(TAG, "parse_music_ad_schedule_info: to string = " + musicAdScheduleInfo.to_string());
                musicAdScheduleInfoList.add(musicAdScheduleInfo );
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return musicAdScheduleInfoList;
    }

    public static List<MusicInfo> parse_music_info(String jsonString) {
        List<MusicInfo> musicInfoList = new ArrayList<>();

        if (TextUtils.isEmpty(jsonString))
            return musicInfoList;

        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                MusicInfo musicInfo = new MusicInfo();
                musicInfo.set_category_name(get_string(object, "name"));
                musicInfo.set_url(get_string(object, "icon"));
                musicInfo.set_service_id_list(get_int_list(object, "servicelists"));
                //musicInfo.to_string();
                musicInfoList.add(musicInfo);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return musicInfoList;
    }

    public static JSONArray get_array(JSONObject obj, String name) {
        try {
            return obj.getJSONArray(name);
        }
        catch (JSONException e) {
            //Log.w(TAG, "get_object: exception, [name] " + name);
            return null;
        }
    }

    public static JSONObject get_object(JSONObject obj, String name) {
        try {
            return obj.getJSONObject(name);
        }
        catch (JSONException e) {
            //Log.w(TAG, "get_object: exception, [name] " + name);
            return null;
        }
    }

    public static int get_int(JSONObject obj, String name) {
        try {
            return obj.getInt(name);
        }
        catch (JSONException e) {
            //Log.w(TAG, "get_int: exception, [name] " + name);
            return -1;
        }
    }

    public static String get_string(JSONObject obj, String name) {
        try {
            return obj.getString(name);
        }
        catch (Exception e) {
            //Log.w(TAG, "get_string: exception, [name] " + name);
            return null;
        }
    }

    public static String get_string(JSONArray array) {
        try {
            if (null == array)
                return null;
            return array.getString(0);
        }
        catch (Exception e) {
            //Log.w(TAG, "get_string: exception, [array] " + array);
            return null;
        }
    }

    public static ArrayList<Integer> get_int_list(JSONObject obj, String name) {
        try {
            JSONArray jArray = obj.getJSONArray(name);
            ArrayList<Integer> arrayList = new ArrayList<>();
            for (int i = 0; i < jArray.length(); i++)
                arrayList.add(jArray.getInt(i));
            return arrayList;
        }
        catch (JSONException e) {
            //Log.w(TAG, "get_int_list: exception, [name] " + name);
            return null;
        }
    }
}
