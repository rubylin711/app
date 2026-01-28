package com.prime.dmg.launcher.Utils.JsonParser;

import java.util.List;

public class RecommendProgram {

    private int g_sequence;
    private String g_title;
    private String g_startTime;
    private String g_endTime;
    private String g_video_Id;
    private String g_playlistId;
    private String g_icon;
    private String g_appName;
    private String g_type;
    private String g_videoId;
    private List<Trigger> g_triggers;

    public int get_sequence() {
        return g_sequence;
    }

    public void set_sequence(int sequence) {
        g_sequence = sequence;
    }

    public String get_title() {
        return g_title;
    }

    public void set_title(String title) {
        g_title = title;
    }

    public String get_start_time() {
        return g_startTime;
    }

    public void set_start_time(String startTime) {
        g_startTime = startTime;
    }

    public String get_end_time() {
        return g_endTime;
    }

    public void set_end_time(String endTime) {
        g_endTime = endTime;
    }

    public String get_video_id() {
        return g_video_Id;
    }

    public void set_video_id(String video_id) {
        g_video_Id = video_id;
    }

    public String get_playlist_id() {
        return g_playlistId;
    }

    public void set_playlist_id(String playlistId) {
        g_playlistId = playlistId;
    }

    public String get_icon() {
        return g_icon;
    }

    public void set_icon(String icon) {
        g_icon = icon;
    }

    public String get_app_name() {
        return g_appName;
    }

    public void set_app_name(String appName) {
        g_appName = appName;
    }

    public String get_type() {
        return g_type;
    }

    public void set_type(String type) {
        g_type = type;
    }

    public String get_videoId() {
        return g_videoId;
    }

    public void set_videoId(String videoId) {
        g_videoId = videoId;
    }

    public Trigger get_trigger() {
        if (null == g_triggers || g_triggers.size() <= 0)
            return null;
        return g_triggers.get(0);
    }

    public List<Trigger> get_triggers() {
        return g_triggers;
    }

    public void set_triggers(List<Trigger> triggers) {
        g_triggers = triggers;
    }

    public static class Trigger {
        public String activityName;
        public String EXTRA_VIDEO_ID; // CP 使用 原則上只認 "activity_name":"CP", 其餘棄用
        public String triggerUri;
        public String packageName;
        public int min_version_code; // 棄用, 過去有碰過 3rd Party 會因版本而開啟方式不同
        public int max_version_code; // 棄用, 過去有碰過 3rd Party 會因版本而開啟方式不同
        public String intent_flags; // 棄用, 過去有碰過 3rd Party 會因版本而開啟方式不同, 所對應的開啟參數

        public String get_ActivityName() {
            return activityName;
        }

        public void set_ActivityName(String activityName) {
            this.activityName = activityName;
        }

        public String get_EXTRA_VIDEO_ID() {
            return EXTRA_VIDEO_ID;
        }

        public void set_EXTRA_VIDEO_ID(String EXTRA_VIDEO_ID) {
            this.EXTRA_VIDEO_ID = EXTRA_VIDEO_ID;
        }

        public String get_TriggerUri() {
            return triggerUri;
        }

        public void set_TriggerUri(String trigger_uri) {
            this.triggerUri = trigger_uri;
        }

        public String get_PackageName() {
            return packageName;
        }

        public void set_PackageName(String package_name) {
            this.packageName = package_name;
        }

        public int get_MinVersionCode() {
            return min_version_code;
        }

        public void set_MinVersionCode(int min_version_code) {
            this.min_version_code = min_version_code;
        }

        public int get_MaxVersionCode() {
            return max_version_code;
        }

        public void set_MaxVersionCode(int max_version_code) {
            this.max_version_code = max_version_code;
        }

        public String get_IntentFlags() {
            return intent_flags;
        }

        public void set_IntentFlags(String intent_flags) {
            this.intent_flags = intent_flags;
        }
    }
}
