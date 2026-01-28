package com.prime.launcher.Utils.JsonParser;

import java.util.HashMap;
import java.util.List;

public class AppPackage {
    public static final String TYPE_TBC = "TBC";
    public static final String TYPE_APP = "APP";
    public static final String SOURCE_TYPE_YOUTUBE  = "1001";
    public static final String SOURCE_TYPE_MYVIDEO  = "1002";
    public static final String SOURCE_TYPE_CATCHPLAY = "1003";
    public static final String SOURCE_TYPE_STREAM   = "1004";
    public static final String SOURCE_TYPE_BROWSER  = "1005";
    public static final String SOURCE_TYPE_POSTER   = "1006";
    public static final String SOURCE_TYPE_QRCODE   = "1007";
    public static final String SOURCE_TYPE_PROGRAM  = "1008";
    public static final String SOURCE_TYPE_APP      = "1009";
    public static final String SOURCE_TYPE_CP       = "1010";
    public static final String SOURCE_TYPE_CP_MULTI = "1011";
    private String packageName;
    private String label;
    private String type;
    private String iconUrl;
    private String plan_text;
    private String description;
    private String qrcode_content;
    private String qrcode_hint;
    private String btn_label;
    private String cp_code;
    private List<Content> content;

    public String get_package_name() {
        return packageName;
    }

    public void set_package_name(String packageName) {
        this.packageName = packageName;
    }

    public String get_label() {
        return label;
    }

    public void set_label(String label) {
        this.label = label;
    }

    public String get_type() {
        return type;
    }

    public void set_type(String type) {
        this.type = type;
    }

    public String get_icon_url() {
        return iconUrl;
    }

    public void set_icon_url(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String get_plan_text() {
        return plan_text;
    }

    public void set_plan_text(String plan_text) {
        this.plan_text = plan_text;
    }

    public String get_description() {
        return description;
    }

    public void set_description(String description) {
        this.description = description;
    }

    public String get_qrcode_content() {
        return qrcode_content;
    }

    public void set_qrcode_content(String qrcode_content) {
        this.qrcode_content = qrcode_content;
    }

    public String get_qrcode_hint() {
        return qrcode_hint;
    }

    public void set_qrcode_hint(String qrcode_hint) {
        this.qrcode_hint = qrcode_hint;
    }

    public String get_btn_label() {
        return btn_label;
    }

    public void set_btn_label(String btn_label) {
        this.btn_label = btn_label;
    }

    public String get_cp_code() {
        return cp_code;
    }

    public void set_cp_code(String cp_code) {
        this.cp_code = cp_code;
    }

    public List<Content> get_content() {
        return content;
    }

    public void set_content(List<Content> content) {
        this.content = content;
    }

    public static class Content {
        private int sequence;
        private String title;
        private String sourceType;
        private String type;
        private String typeName;
        private String description;
        private String url;
        private String poster;
        private String activityName;
        private String channelName;
        private int channelNum;
        private String startTime;
        private String endTime;
        private List<Integer> intentFlags;
        private String packageName;
        private HashMap<String, String> packageParams;
        private String playlistId;
        private String triggerUri;
        private String videoId;

        public int get_sequence() {
            return sequence;
        }

        public void set_sequence(int sequence) {
            this.sequence = sequence;
        }

        public String get_title() {
            return title;
        }

        public void set_title(String title) {
            this.title = title;
        }

        public String get_source_type() {
            return sourceType;
        }

        public void set_source_type(String sourceType) {
            this.sourceType = sourceType;
        }

        public String get_type_name() {
            return typeName;
        }

        public void set_type_name(String typeName) {
            this.typeName = typeName;
        }

        public String get_description() {
            return description;
        }

        public void set_description(String description) {
            this.description = description;
        }

        public String get_url() {
            return url;
        }

        public void set_url(String url) {
            this.url = url;
        }

        public String get_poster() {
            return poster;
        }

        public void set_poster(String poster) {
            this.poster = poster;
        }

        public String get_type() {
            return type;
        }

        public void set_type(String type) {
            this.type = type;
        }

        public String getActivityName() {
            return activityName;
        }

        public void setActivityName(String activityName) {
            this.activityName = activityName;
        }

        public String get_channel_name() {
            return channelName;
        }

        public void set_channel_name(String channelName) {
            this.channelName = channelName;
        }

        public int get_channel_num() {
            return channelNum;
        }

        public void set_channel_num(int channelNum) {
            this.channelNum = channelNum;
        }

        public String get_start_time() {
            return startTime;
        }

        public void set_start_time(String startTime) {
            this.startTime = startTime;
        }

        public String get_end_time() {
            return endTime;
        }

        public void set_end_time(String endTime) {
            this.endTime = endTime;
        }

        public List<Integer> get_intent_flags() {
            return intentFlags;
        }

        public void set_intent_flags(List<Integer> intentFlags) {
            this.intentFlags = intentFlags;
        }

        public String get_package_name() {
            return packageName;
        }

        public void set_package_name(String packageName) {
            this.packageName = packageName;
        }

        public HashMap<String, String> get_package_params() {
            return packageParams;
        }

        public void set_package_params(HashMap<String, String> packageParams) {
            this.packageParams = packageParams;
        }

        public String get_playlist_id() {
            return playlistId;
        }

        public void set_playlist_id(String playlistId) {
            this.playlistId = playlistId;
        }

        public String get_trigger_uri() {
            return triggerUri;
        }

        public void set_trigger_uri(String triggerUri) {
            this.triggerUri = triggerUri;
        }

        public String get_video_id() {
            return videoId;
        }

        public void set_video_id(String videoId) {
            this.videoId = videoId;
        }
    }
}
