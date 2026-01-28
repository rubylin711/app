package com.prime.launcher.Utils.JsonParser;

import static com.prime.datastructure.utils.JsonParser.get_array;
import static com.prime.datastructure.utils.JsonParser.get_int;
import static com.prime.datastructure.utils.JsonParser.get_int_list;
import static com.prime.datastructure.utils.JsonParser.get_object;
import static com.prime.datastructure.utils.JsonParser.get_string;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prime.datastructure.sysdata.MusicAdInfo;
import com.prime.datastructure.sysdata.MusicAdScheduleInfo;
import com.prime.datastructure.sysdata.MusicInfo;

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
}
