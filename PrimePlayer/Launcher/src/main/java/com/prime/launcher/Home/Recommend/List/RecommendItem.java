package com.prime.launcher.Home.Recommend.List;

import static android.content.Intent.URI_INTENT_SCHEME;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.prime.launcher.Utils.JsonParser.RecommendProgram;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

public class RecommendItem {

    public String g_videoId;
    public String g_label;
    public String g_thumbnailUrl;
    public String g_poster;
    public String g_pkg;
    public String g_intentUri;
    public int g_thumbnailResId;
    public Intent g_intent;
    public Drawable g_banner;

    // Recommend Page Item
    private int g_sequence;
    private String g_title;
    private String g_startTime;
    private String g_endTime;
    private String g_video_Id;
    private String g_playlistId;
    private String g_icon;
    private String g_appName;
    private String g_type;
    private RecommendProgram.Trigger g_trigger;
    private List<RecommendProgram.Trigger> g_triggers;

    // Recommend Launcher
    private String g_packageName;
    private int g_iconPriority;
    private String g_activityName;
    private String g_productId;
    private HashMap<String, String> g_packageParams;
    private String g_description;
    private String g_fullText;
    private String g_appPath;
    private String g_appVersionCode;
    private String g_apkLabel;
    private List<String> g_screenCaptures;
    private int g_displayPosition;
    private boolean g_forceUpdate;

    // flag
    private boolean g_isBindFromTvContract;

    public RecommendItem() {

    }

    // Recommend Page Item
    public RecommendItem(int sequence, String title, String startTime, String endTime,
                         String videoId, String playlistId, String icon, String appName,
                         String type, String video_Id, RecommendProgram.Trigger trigger, List<RecommendProgram.Trigger> triggers) {
        g_sequence = sequence;
        g_title = title;
        g_startTime = startTime;
        g_endTime = endTime;
        g_videoId = videoId;
        g_playlistId = playlistId;
        g_icon = icon;
        g_appName = appName;
        g_type = type;
        g_video_Id = video_Id;
        g_trigger = trigger;
        g_triggers = triggers;
        g_label = g_title;
        g_poster = g_icon;
        g_thumbnailUrl = g_icon;
    }

    // Recommend Launcher Profile
    public RecommendItem(String appName, String packageName, String thumbnail, int iconPriority,
                         String activityName, String productId, HashMap<String, String> packageParams,
                         String description, String fullText, String appPath, String appVersionCode,
                         String apkLabel, List<String> screenCaptures, int displayPosition,
                         boolean forceUpdate) {
        g_appName = appName;
        g_packageName = packageName;
        g_thumbnailUrl = thumbnail;
        g_iconPriority = iconPriority;
        g_activityName = activityName;
        g_productId = productId;
        g_packageParams = packageParams;
        g_description = description;
        g_fullText = fullText;
        g_appPath = appPath;
        g_appVersionCode = appVersionCode;
        g_apkLabel = apkLabel;
        g_screenCaptures = screenCaptures;
        g_displayPosition = displayPosition;
        g_forceUpdate = forceUpdate;
        g_label = g_appName;
        g_poster = g_thumbnailUrl;
        g_thumbnailUrl = g_thumbnailUrl;
    }

    public RecommendItem(String ch_pkg, long pg_id, String pg_title, String pg_desc, String pg_poster, String pg_intentUri) {
        g_packageName   = ch_pkg;
        g_videoId       = String.valueOf(pg_id);
        g_label         = pg_title;
        g_description   = pg_desc;
        g_poster        = pg_poster;
        g_icon          = pg_poster;
        g_thumbnailUrl  = pg_poster;
        g_intentUri     = pg_intentUri;
    }

    public RecommendItem(String app_label, Drawable app_banner, String app_pkg, Intent app_intent) {
        g_label  = app_label;
        g_banner = app_banner;
        g_pkg    = app_pkg;
        g_intent = app_intent;
        g_appName = g_label;
        g_packageName = g_pkg;
    }

    public void set_sequence(int sequence) {
        g_sequence = sequence;
    }

    public void set_title(String title) {
        g_title = title;
        g_label = g_title;
    }

    public void set_start_time(String startTime) {
        g_startTime = startTime;
    }

    public void set_end_time(String endTime) {
        g_endTime = endTime;
    }

    public void set_video_id(String video_id) {
        g_video_Id = video_id;
    }

    public void set_playlist_id(String playlistId) {
        g_playlistId = playlistId;
    }

    public void set_icon(String icon) {
        g_icon = icon;
        g_poster = g_icon;
        g_thumbnailUrl = g_icon;
    }

    public void set_banner(Drawable banner) {
        g_banner = banner;
    }

    public void set_app_name(String appName) {
        g_appName = appName;
        g_label = g_appName;
    }

    public void set_type(String type) {
        g_type = type;
    }

    public void set_videoId(String videoId) {
        g_videoId = videoId;
    }

    public void set_trigger(RecommendProgram.Trigger trigger) {
        g_trigger = trigger;
    }

    public void set_triggers(List<RecommendProgram.Trigger> triggers) {
        g_triggers = triggers;
    }

    public void set_thumbnail(String thumbnail_url) {
        set_icon(thumbnail_url);
    }

    public void set_thumbnail_res_id(int thumbnail_res_id) {
        g_thumbnailResId = thumbnail_res_id;
    }

    public void set_package_name(String package_name) {
        g_packageName = package_name;
    }

    public void set_bind_from_tv_contract(boolean is_bind) {
        g_isBindFromTvContract = is_bind;
    }

    public void set_label(String label) {
        g_label = label;
    }

    public Intent get_intent() {
        return g_intent;
    }

    public Intent get_intent(String intent_uri) {
        try {
            return Intent.parseUri(intent_uri, URI_INTENT_SCHEME);
        } catch (URISyntaxException e) {
            e.printStackTrace(); //throw new RuntimeException(e);
        }
        return null;
    }

    public String get_intent_uri() {
        return g_intentUri;
    }

    public String get_label() {
        return g_label;
    }

    public int get_sequence() {
        return g_sequence;
    }

    public String get_title() {
        return g_title;
    }

    public String get_start_time() {
        return g_startTime;
    }

    public String get_end_time() {
        return g_endTime;
    }

    public String get_video_id() {
        return g_video_Id;
    }

    public String get_playlist_id() {
        return g_playlistId;
    }

    public String get_icon() {
        return g_icon;
    }

    public String get_poster() {
        return g_poster;
    }

    public String get_app_name() {
        return g_appName;
    }

    public String get_type() {
        return g_type;
    }

    public String get_videoId() {
        return g_videoId;
    }

    public RecommendProgram.Trigger get_trigger() {
        return g_trigger;
    }

    public List<RecommendProgram.Trigger> get_triggers() {
        return g_triggers;
    }

    public String get_package_name() {
        return g_packageName;
    }

    public Drawable get_thumbnail_drawable() {
        return g_banner;
    }

    public String get_thumbnail_url() {
        return g_thumbnailUrl;
    }

    public int get_thumbnail_res_id() {
        return g_thumbnailResId;
    }

    public int get_icon_priority() {
        return g_iconPriority;
    }

    public String get_activity_name() {
        return g_activityName;
    }

    public String get_product_id() {
        return g_productId;
    }

    public HashMap<String, String> get_package_params() {
        return g_packageParams;
    }

    public String get_description() {
        return g_description;
    }

    public String get_full_text() {
        return g_fullText;
    }

    public String get_app_path() {
        return g_appPath;
    }

    public String get_app_version_code() {
        return g_appVersionCode;
    }

    public String get_apk_label() {
        return g_apkLabel;
    }

    public List<String> get_screen_captures() {
        return g_screenCaptures;
    }

    public int get_display_position() {
        return g_displayPosition;
    }

    public boolean get_force_update() {
        return is_force_update();
    }

    public boolean is_force_update() {
        return g_forceUpdate;
    }

    public boolean is_bind_from_tv_contract() {
        return g_isBindFromTvContract;
    }
}
