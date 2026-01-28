package com.prime.datastructure.sysdata;

public class MusicAdInfo {
    private static final String TAG = "MusicAdInfo";

    private String g_ad_url;
    private String g_end_date;
    private String g_md5_checksum;
    private String g_media_url;
    private int g_sequence;
    private String g_source_type;
    private String g_start_date;
    private String g_title;
    private String g_type;
    private String g_type_name;
    private String g_url;

    public String get_ad_url() {
        return g_ad_url;
    }

    public void set_ad_url(String adUrl) {
        g_ad_url = adUrl;
    }

    public String get_end_date() {
        return g_end_date;
    }

    public void set_end_date(String endDate) {
        g_end_date = endDate;
    }

    public String get_md5_checksum() {
        return g_md5_checksum;
    }

    public void set_md5_checksum(String md5Checksum) {
        g_md5_checksum = md5Checksum;
    }

    public String get_media_url() {
        return g_media_url;
    }

    public void set_media_url(String mediaUrl) {
        g_media_url = mediaUrl;
    }

    public int get_sequence() {
        return g_sequence;
    }

    public void set_sequence(int sequence) {
        g_sequence = sequence;
    }

    public String get_source_type() {
        return g_source_type;
    }

    public void set_source_type(String sourceType) {
        g_source_type = sourceType;
    }

    public String get_start_date() {
        return g_start_date;
    }

    public void set_start_date(String startDate) {
        g_start_date = startDate;
    }

    public String get_title() {
        return g_title;
    }

    public void set_title(String title) {
        g_title = title;
    }

    public String get_type() {
        return g_type;
    }

    public void set_type(String type) {
        g_type = type;
    }

    public String get_type_name() {
        return g_type_name;
    }

    public void set_type_name(String typeName) {
        g_type_name = typeName;
    }

    public String get_url() {
        return g_url;
    }

    public void set_url(String url) {
        g_url = url;
    }

    public String to_string() {
        return "adUrl = " + g_ad_url + " endDate = " + g_end_date + " md5Checksum = " + g_md5_checksum +
                " mediaUrl = " + g_media_url + " sequence = " + g_sequence + " sourceType = " + g_source_type +
                " startDate = " + g_start_date + " title = " + g_title + " type = " + g_type +
                " typeName = " + g_type_name + " url = " + g_url;
    }
}
