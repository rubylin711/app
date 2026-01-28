package com.prime.datastructure.sysdata;

/**
 * Created by gary_hsu on 2018/1/8.
 */

public class SimpleChannel {
    private long g_channel_id;
    private int g_group_type;
    private int g_channel_num;
    private String g_channel_name;
    private int g_user_lock;
    private int g_ca;
    private int g_channel_skip;//Scoty 20181109 modify for skip channel
    private int g_pvr_skip;
    private int g_tp_id;//Scoty 20180613 change get simplechannel list for PvrSkip rule
    private String g_url; //for VOD and Youtube Stream
    private int g_play_stream_type;//0:DVB;1:VOD;2:Youtube
    private EPGEvent g_present_epg_event;
    private EPGEvent g_follow_epg_event;
    private String g_short_event;
    private String g_detail_info;
    private int g_is_adult_channel;

    public long get_channel_id() {
        return g_channel_id;
    }

    public void set_channel_id(long channelId) {
        g_channel_id = channelId;
    }

    public int get_channel_num() {
        return g_channel_num;
    }

    public void set_channel_num(int g_channel_num) {
        this.g_channel_num = g_channel_num;
    }

    public String get_channel_name() {
        return g_channel_name;
    }

    public void set_channel_name(String channelName) {
        g_channel_name = channelName;
    }

    public int get_user_lock() {
        return g_user_lock;
    }

    public void set_user_lock(int userLock) {
        g_user_lock = userLock;
    }


    public int get_ca() {
        return g_ca;
    }

    public void set_ca(int Ca) {
        g_ca = Ca;
    }

    public int get_pvr_skip() {
        return g_pvr_skip;
    }

    public void set_pvr_skip(int PVRSkip) {
        this.g_pvr_skip = PVRSkip;
    }

    public void set_tp_id(int TpId)//Scoty 20180613 change get simplechannel list for PvrSkip rule
    {
        this.g_tp_id = TpId;
    }

    public int get_tp_id() {//Scoty 20180613 change get simplechannel list for PvrSkip rule
        return g_tp_id;
    }

    public int get_channel_skip() {//Scoty 20181109 modify for skip channel
        return g_channel_skip;
    }

    public void set_channel_skip(int channelSkip) {//Scoty 20181109 modify for skip channel
        g_channel_skip = channelSkip;
    }

    public String get_url() {
        return g_url;
    }

    public void set_url(String url) {
        g_url = url;
    }

    public int get_type() {
        return g_group_type;
    }

    public void set_type(int type) {
        this.g_group_type = type;
    }

    public int get_play_stream_type() {
        return g_play_stream_type;
    }

    public void set_play_stream_type(int playStreamType) {
        this.g_play_stream_type = playStreamType;
    }

    public EPGEvent get_present_epg_event() {
        return g_present_epg_event;
    }

    public void set_present_epg_event(EPGEvent presentepgEvent) {
        g_present_epg_event = presentepgEvent;
    }

    public EPGEvent get_follow_epg_event() {
        return g_follow_epg_event;
    }

    public void set_follow_epg_event(EPGEvent followepgEvent) {
        g_follow_epg_event = followepgEvent;
    }

    public String get_short_event() {
        return g_short_event;
    }

    public void set_short_event(String shortEvent) {
        g_short_event = shortEvent;
    }

    public String get_detail_info() {
        return g_detail_info;
    }

    public void set_detail_info(String detailInfo) {
        g_detail_info = detailInfo;
    }

    public void set_is_adult_channel(int isAdultChannel) {
        g_is_adult_channel = isAdultChannel;
    }

    public int is_adult_channel() {
        return g_is_adult_channel;
    }
}
