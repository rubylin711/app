package com.prime.dmg.launcher.Utils.JsonParser;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prime.dtv.sysdata.MusicAdInfo;
import com.prime.dtv.sysdata.MusicAdScheduleInfo;
import com.prime.dtv.sysdata.MusicInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    static String TAG = "JsonParser";

    public static class LiTV {
        public static final boolean ENABLE_ANDROID_TV = false;
        public static final String PACKAGE_CABLE = "com.litv.cable.home";
        public static final String PACKAGE_ANDROID_TV = "com.js.litv.home";
    }

    public static List<RecommendPackage> parse_recommend_packages(String jsonString) {
        List<RecommendPackage> packageList = new ArrayList<>();
        List<RecommendContent> channelList;

        if (TextUtils.isEmpty(jsonString))
            return packageList;

        try {
            JSONArray allPackageArray = new JSONArray(jsonString);
            for (int i = 0; i < allPackageArray.length(); i++) {
                JSONObject aPackageObject = allPackageArray.getJSONObject(i);
                RecommendPackage aPackage = new RecommendPackage();
                // add Package ID, Package Name
                aPackage.set_package_id(get_string(aPackageObject, "PackageId"));
                aPackage.set_package_name(get_string(aPackageObject, "PackageName"));
                // add Channel List
                JSONArray channelArray = aPackageObject.getJSONArray("ChannelList");
                channelList = new ArrayList<>();
                for (int j = 0; j < channelArray.length(); j++) {
                    // add One Channel
                    JSONObject channelObject = channelArray.getJSONObject(j);
                    RecommendContent channel = new RecommendContent();
                    channel.set_service_id(get_string(channelObject, "ServiceId"));
                    channel.set_program_name(get_string(channelObject, "ProgramName"));
                    channel.set_program_poster(get_string(channelObject, "ProgramPoster"));
                    channelList.add(channel);
                }
                aPackage.set_channel_list(channelList);
                packageList.add(aPackage);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return packageList;
    }

    public static List<RecommendProgram> parse_popular_programs(String jsonString) {
        List<RecommendProgram> recommendProgramList = new ArrayList<>();

        if (TextUtils.isEmpty(jsonString))
            return recommendProgramList;

        jsonString = "{\"recommends\":" + jsonString + "}";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray recommendsArray = jsonObject.getJSONArray("recommends");

            for (int i = 0; i < recommendsArray.length(); i++) {
                JSONObject obj = recommendsArray.getJSONObject(i);

                RecommendProgram recommendProgram = new RecommendProgram();
                recommendProgram.set_sequence(get_int(obj, "sequence"));
                recommendProgram.set_title(get_string(obj, "title"));
                recommendProgram.set_start_time(get_string(obj, "start_time"));
                recommendProgram.set_end_time(get_string(obj, "end_time"));
                recommendProgram.set_video_id(get_string(obj, "video_id"));
                recommendProgram.set_playlist_id(get_string(obj, "playlist_id"));
                recommendProgram.set_icon(get_string(obj, "icon"));
                recommendProgram.set_app_name(get_string(obj, "app_name"));
                recommendProgram.set_type(get_string(obj, "type"));
                recommendProgram.set_videoId(get_string(obj, "videoId"));

                // Parsing triggers
                JSONArray triggersArray = obj.getJSONArray("triggers");
                List<RecommendProgram.Trigger> triggerList = new ArrayList<>();
                for (int j = 0; j < triggersArray.length(); j++) {
                    JSONObject triggerObj = (JSONObject) triggersArray.get(0);
                    JSONObject paramsObj = get_object(triggerObj, "package_params");
                    JSONArray flagArray = get_array(triggerObj, "intent_flags");

                    RecommendProgram.Trigger newTrigger = new RecommendProgram.Trigger();
                    newTrigger.set_ActivityName(get_string(triggerObj, "activity_name"));
                    newTrigger.set_TriggerUri(get_string(triggerObj, "trigger_uri"));
                    newTrigger.set_PackageName(get_string(triggerObj, "package_name"));
                    newTrigger.set_MinVersionCode(get_int(triggerObj, "min_version_code"));
                    newTrigger.set_MaxVersionCode(get_int(triggerObj, "max_version_code"));
                    if (paramsObj != null) newTrigger.set_EXTRA_VIDEO_ID(get_string(paramsObj, "EXTRA_VIDEO_ID"));
                    if (flagArray != null) newTrigger.set_IntentFlags(flagArray.getString(0));
                    triggerList.add(newTrigger);
                }
                recommendProgram.set_triggers(triggerList);;
                recommendProgramList.add(recommendProgram);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recommendProgramList;
    }

    public static List<AdPage> parse_ad_pages(String jsonString) {
        List<AdPage> adPageList = new ArrayList<>();

        if (TextUtils.isEmpty(jsonString))
            return adPageList;

        try {
            jsonString = "{\"tvRecommendations\":" + jsonString + "}";
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray pageArray = jsonObject.getJSONArray("tvRecommendations");

            for (int i = 0; i < pageArray.length(); i++) {
                JSONObject pageObj = pageArray.getJSONObject(i);

                AdPage adPage = new AdPage();
                adPage.setPage(pageObj.getString("page"));
                adPage.setType(pageObj.getString("type"));

                JSONArray itemArray = pageObj.getJSONArray("items");
                List<AdPageItem> itemList = new ArrayList<>();
                for (int j = 0; j < itemArray.length(); j++) {
                    JSONObject itemObj = itemArray.getJSONObject(j);

                    AdPageItem pageItem = new AdPageItem();
                    pageItem.set_pagination(get_int(itemObj, "pagination"));
                    pageItem.set_sequence(get_int(itemObj, "sequence"));
                    pageItem.set_title(get_string(itemObj, "title"));
                    pageItem.set_type(get_string(itemObj, "type"));
                    pageItem.set_typeName(get_string(itemObj, "typeName"));
                    pageItem.set_sourceType(get_string(itemObj, "sourceType"));
                    pageItem.set_source_type(get_string(itemObj, "source_type"));
                    pageItem.set_poster_art_url(get_string(itemObj, "poster_art_url"));
                    pageItem.set_description(get_string(itemObj, "description"));
                    pageItem.set_start_time(get_string(itemObj, "start_time"));
                    pageItem.set_service_id(get_string(itemObj, "service_id"));
                    pageItem.set_end_time(get_string(itemObj, "end_time"));
                    pageItem.set_channel_num(get_string(itemObj, "channel_num"));
                    pageItem.set_channel_name(get_string(itemObj, "channel_name"));
                    pageItem.set_videoId(get_string(itemObj, "videoId"));
                    pageItem.set_playListId(get_string(itemObj, "playListId"));
                    pageItem.set_url(get_string(itemObj, "url"));
                    pageItem.set_package_name(get_string(itemObj, "packageName"));
                    pageItem.set_intent_uri(get_string(itemObj, "uri"));
                    pageItem.set_intent_flag(get_string(get_array(itemObj, "intent_flags")));
                    pageItem.set_package_params(get_string(get_object(itemObj, "package_params"), "EXTRA_VIDEO_ID"));

                    itemList.add(pageItem);
                }
                adPage.setItems(itemList);

                adPageList.add(adPage);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return adPageList;
    }

    public static List<LauncherProfile> parse_recommend_apps(String jsonString) {
        List<LauncherProfile> launcherProfiles = new ArrayList<>();
        Gson gson = new Gson();
        Type type = new TypeToken<List<LauncherProfile>>(){}.getType();

        if (TextUtils.isEmpty(jsonString))
            return launcherProfiles;

        launcherProfiles = gson.fromJson(jsonString, type);

        if (LiTV.ENABLE_ANDROID_TV) {
            for (LauncherProfile profile : launcherProfiles) {
                String pkgName = profile.get_package_name();
                if (LiTV.PACKAGE_CABLE.equals(pkgName))
                    profile.set_package_name(LiTV.PACKAGE_ANDROID_TV);
            }
        }

        return launcherProfiles; //return gson.fromJson(jsonString, LauncherInfo.class);
    }

    public static List<RankInfo> parse_ranking_info(String jsonString) {
        List<RankInfo> rankInfos = new ArrayList<>();

        if (TextUtils.isEmpty(jsonString))
            return rankInfos;

        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                RankInfo rankInfo = new RankInfo();
                rankInfo.set_ranking(get_int(object, "ranking"));
                rankInfo.set_id(get_int(object, "id"));
                rankInfo.set_channel_id(get_int(object, "channel_id"));
                rankInfo.set_service_id(get_int(object, "service_id"));
                rankInfo.set_channel_name(get_string(object, "channel_name"));
                rankInfo.set_channel_poster(get_string(object, "channel_poster"));
                rankInfo.set_event_id(get_string(object, "event_id"));
                rankInfo.set_tv_name(get_string(object, "tv_name"));
                rankInfo.set_start_date(get_string(object, "start_date"));
                rankInfo.set_end_date(get_string(object, "end_date"));
                rankInfo.set_start_time(get_string(object, "start_time"));
                rankInfo.set_end_time(get_string(object, "end_time"));
                rankInfo.set_rating(get_string(object, "rating"));
                rankInfo.set_keep_record(get_int(object, "keepRecord"));
                rankInfo.set_last_updated(get_string(object, "lastUpdated"));
                rankInfo.set_box_count(get_string(object, "box_count"));
                rankInfo.set_description(get_string(object, "description"));
                rankInfo.set_time_interval(get_string(object, "Time_Interval"));
                rankInfo.set_total_sec(get_int(object, "TotalSec"));
                rankInfo.set_time_interval(get_string(object, "lastUpdatedUTC"));
                rankInfos.add(rankInfo);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return rankInfos;
    }

    public static List<AppPackage> parse_hot_video(String json) {
        List<AppPackage> appPackageList = new ArrayList<>();

        if (TextUtils.isEmpty(json))
            return appPackageList;

        Gson gson = new Gson();
        Type type = new TypeToken<List<AppPackage>>(){}.getType();
        appPackageList = gson.fromJson(json, type);

        if (LiTV.ENABLE_ANDROID_TV) {
            for (AppPackage appPackage : appPackageList) {
                String pkgName = appPackage.get_package_name();
                if (LiTV.PACKAGE_CABLE.equals(pkgName))
                    appPackage.set_package_name(LiTV.PACKAGE_ANDROID_TV);
            }
        }

        return appPackageList;
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

    private static JSONArray get_array(JSONObject obj, String name) {
        try {
            return obj.getJSONArray(name);
        }
        catch (JSONException e) {
            //Log.w(TAG, "get_object: exception, [name] " + name);
            return null;
        }
    }

    private static JSONObject get_object(JSONObject obj, String name) {
        try {
            return obj.getJSONObject(name);
        }
        catch (JSONException e) {
            //Log.w(TAG, "get_object: exception, [name] " + name);
            return null;
        }
    }

    private static int get_int(JSONObject obj, String name) {
        try {
            return obj.getInt(name);
        }
        catch (JSONException e) {
            //Log.w(TAG, "get_int: exception, [name] " + name);
            return -1;
        }
    }
    
    private static String get_string(JSONObject obj, String name) {
        try {
            return obj.getString(name);
        }
        catch (Exception e) {
            //Log.w(TAG, "get_string: exception, [name] " + name);
            return null;
        }
    }

    private static String get_string(JSONArray array) {
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

    private static ArrayList<Integer> get_int_list(JSONObject obj, String name) {
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
