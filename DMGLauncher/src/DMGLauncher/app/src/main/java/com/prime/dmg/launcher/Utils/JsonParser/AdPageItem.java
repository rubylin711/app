package com.prime.dmg.launcher.Utils.JsonParser;

public class AdPageItem {
    int g_pagination;
    int g_sequence;
    String g_title;
    String g_type;
    String g_sourceType;
    String g_source_type;
    String g_typeName;
    String g_poster_art_url;
    String g_description;
    String g_start_time;
    String g_service_id;
    String g_end_time;
    String g_channel_num;
    String g_channel_name;
    String g_videoId;
    String g_playListId;
    String g_url;
    String g_label;
    String g_pkgName;
    String g_package_params;
    String g_intentUri;
    String g_intentFlag;
    private boolean g_isBindFromTvContract;

    public int get_pagination() {
        return g_pagination;
    }

    public int get_sequence() {
        return g_sequence;
    }

    public String get_title() {
        return g_title;
    }

    public String get_type() {
        return g_type;
    }

    public String get_sourceType() {
        return g_sourceType;
    }

    public String get_source_type() {
        return g_source_type;
    }

    public String get_typeName() {
        return g_typeName;
    }

    public String get_poster_art_url() {
        return g_poster_art_url;
    }

    public String get_description() {
        return g_description;
    }

    public String get_start_time() {
        return g_start_time;
    }

    public String get_service_id() {
        return g_service_id;
    }

    public String get_end_time() {
        return g_end_time;
    }

    public String get_channel_num() {
        return g_channel_num;
    }

    public String get_channel_name() {
        return g_channel_name;
    }

    public String get_videoId() {
        return g_videoId;
    }

    public String get_playListId() {
        return g_playListId;
    }

    public String get_url() {
        return g_url;
    }

    public String get_package_name() {
        return g_pkgName;
    }

    public String get_package_params() {
        return g_package_params;
    }

    public String get_intent_uri() {
        return g_intentUri;
    }

    public String get_intent_flag() {
        return g_intentFlag;
    }

    public String get_label() {
        return g_label;
    }

    public boolean is_bind_from_tv_contract() {
        return g_isBindFromTvContract;
    }

    public void set_pagination(int pagination) {
        g_pagination = pagination;
    }

    public void set_sequence(int sequence) {
        g_sequence = sequence;
    }

    public void set_title(String title) {
        g_title = title;
        g_label = title;
    }

    public void set_type(String type) {
        g_type = type;
    }

    public void set_sourceType(String sourceType) {
        g_sourceType = sourceType;
    }

    public void set_source_type(String source_type) {
        g_source_type = source_type;
    }

    public void set_typeName(String typeName) {
        g_typeName = typeName;
    }

    public void set_poster_art_url(String poster_art_url) {
        g_poster_art_url = poster_art_url;
    }

    public void set_description(String description) {
        g_description = description;
    }

    public void set_start_time(String start_time) {
        g_start_time = start_time;
    }

    public void set_service_id(String service_id) {
        g_service_id = service_id;
    }

    public void set_end_time(String end_time) {
        g_end_time = end_time;
    }

    public void set_channel_num(String channel_num) {
        g_channel_num = channel_num;
    }

    public void set_channel_name(String channel_name) {
        g_channel_name = channel_name;
    }

    public void set_videoId(String videoId) {
        g_videoId = videoId;
    }

    public void set_playListId(String playListId) {
        g_playListId = playListId;
    }

    public void set_url(String url) {
        g_url = url;
    }

    public void set_package_name(String pkgName) {
        g_pkgName = pkgName;
    }

    public void set_package_params(String pkgParams) {
        g_package_params = pkgParams;
    }

    public void set_intent_uri(String intentUri) {
        g_intentUri = intentUri;
    }

    public void set_intent_flag(String intent_flag) {
        g_intentFlag = intent_flag;
    }

    public void set_label(String label) {
        g_label = label;
    }

    public void set_bind_from_tv_contract(boolean is_bind) {
        g_isBindFromTvContract = is_bind;
    }
}
