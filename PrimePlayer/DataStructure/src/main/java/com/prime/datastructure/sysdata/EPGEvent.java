package com.prime.datastructure.sysdata;

import com.google.gson.Gson;

/**
 * Created by gary_hsu on 2017/11/13.
 */

public class EPGEvent {
    public static final String TAG = "EPGEvent";
    public static final int EPG_TYPE_PRESENT = 0;
    public static final int EPG_TYPE_FOLLOW = 1;
    public static final int EPG_TYPE_SCHEDULE = 2;
    private int g_s_id;
    private int g_original_network_id;
    private int g_transport_stream_id;
    private int g_event_id;
    private int g_table_id;
    private int g_event_type; //present/follow/schedule
    private String g_event_name;
    private String g_event_name_lang_codec;
    private long g_start_time;
    private long g_end_time;
    private long g_duration;
    private int g_parental_rate;
    private String g_short_event;
    private String g_short_event_lang_codec;
    private String g_extended_event;
    private String g_extended_event_lang_codec;
    private byte[] g_series_key = new byte[SeriesInfo.Series.MAX_SERIES_KEY_LENGTH];
    private int g_episode_key;
    private int g_episode_status;
    private int g_episode_last;
    private long g_channel_id;

    public EPGEvent(){

    }
    public EPGEvent(EPGEvent event) {
        set_s_id(event.get_s_id());
        set_original_network_id(event.get_original_network_id());
        set_transport_stream_id(event.get_transport_stream_id());
        set_event_id(event.get_event_id());
        set_table_id(event.get_table_id());
        set_event_type(event.get_event_type());
        set_event_name(event.get_event_name());
        set_event_name_lang_codec(event.get_event_name_lang_codec());
        set_start_time(event.get_start_time());
        set_end_time(event.get_end_time());
        set_duration(event.get_duration());
        set_parental_rate(event.get_parental_rate());
        set_short_event(event.get_short_event());
        set_short_event_lang_codec(event.get_short_event_lang_codec());
        set_extended_event(event.get_extended_event());
        set_extended_event_lang_codec(event.get_extended_event_lang_codec());
        set_series_key(event.get_series_key());
        set_episode_key(event.get_episode_key());
        set_episode_status(event.get_episode_status());
        set_episode_last(event.get_episode_last());
    }

    public String to_string(){
        return "[Sid  " + g_s_id + "OriginalNetworkId : " + g_original_network_id + "TransportStreamId : " + g_transport_stream_id
                + "EventId : " + g_event_id + "TableId : "+ g_table_id + "EventType : " + g_event_type + "EventName : "+ g_event_name + "EventNameLangCodec : "+ g_event_name_lang_codec
                + "StartTime : " + g_start_time + "EndTime : "+ g_end_time + "Duration : "+ g_duration + "ParentalRate : "
                + g_parental_rate + "ShortEvent : " + g_short_event + "ShortEventLangCodec : " + g_short_event_lang_codec + "ShortEvent : " + g_short_event
                + "ExtendedEventLangCodec : "+ g_extended_event_lang_codec + "]";
    }

    public int get_s_id() {
        return g_s_id;
    }

    public void set_s_id(int sid) {
        g_s_id = sid;
    }

    public int get_original_network_id() {
        return g_original_network_id;
    }

    public void set_original_network_id(int originalNetworkId) {
        g_original_network_id = originalNetworkId;
    }

    public int get_transport_stream_id() {
        return g_transport_stream_id;
    }

    public void set_transport_stream_id(int transportStreamId) {
        g_transport_stream_id = transportStreamId;
    }

    public int get_event_id() {
        return g_event_id;
    }

    public void set_event_id(int eventId) {
        g_event_id = eventId;
    }

    public int get_table_id() {
        return g_table_id;
    }

    public void set_table_id(int tableId) {
        g_table_id = tableId;
    }

    public String get_event_name() {
        return g_event_name;
    }

    public void set_event_name(String eventName) {
        g_event_name = eventName;
    }

    public long get_start_time()
    {
        return g_start_time;
    }

    public long get_start_time(long offset) {
        return g_start_time + offset;
    }

    public void set_start_time(long value)
    {
        g_start_time = value;
    }

    public long get_end_time() {
        return g_end_time;
    }

    public long get_end_time(long offset) {
        return g_end_time + offset;
    }

    public void set_end_time(long value)
    {
        g_end_time = value;
    }

    public long get_duration() {
        return g_duration;
    }

    public void set_duration(long duration) {
        g_duration = duration;
    }

    public int get_parental_rate() {
        return g_parental_rate;
    }

    public void set_parental_rate(int parentalRate) {
        g_parental_rate = parentalRate;
    }

    public String get_short_event() {
        return g_short_event;
    }

    public void set_short_event(String shortEvent) {
        g_short_event = shortEvent;
    }

    public String get_extended_event() {
        return g_extended_event;
    }

    public void set_extended_event(String extendedEvent) {
        g_extended_event = extendedEvent;
    }

    public String get_event_name_lang_codec() {
        return g_event_name_lang_codec;
    }

    public void set_event_name_lang_codec(String eventNameLangCodec) {
        g_event_name_lang_codec = eventNameLangCodec;
    }

    public String get_short_event_lang_codec() {
        return g_short_event_lang_codec;
    }

    public void set_short_event_lang_codec(String shortEventLangCodec) {
        g_short_event_lang_codec = shortEventLangCodec;
    }

    public String get_extended_event_lang_codec() {
        return g_extended_event_lang_codec;
    }

    public void set_extended_event_lang_codec(String extendedEventLangCodec) {
        g_extended_event_lang_codec = extendedEventLangCodec;
    }

    public int get_event_type() {
        return g_event_type;
    }

    public void set_event_type(int eventType) {
        g_event_type = eventType;
    }

    public EPGEvent getEPGEventFromJsonString(String jsonString) {
        Gson gson = new Gson();
        EPGEvent epgEvent = gson.fromJson(jsonString, EPGEvent.class);
        return epgEvent;
    }

    public byte[] get_series_key() {
        return g_series_key;
    }

    public void set_series_key(byte[] g_series_key) {
        this.g_series_key = g_series_key;
    }

    public int get_episode_key() {
        return g_episode_key;
    }

    public void set_episode_key(int g_episode_key) {
        this.g_episode_key = g_episode_key;
    }

    public int get_episode_status() {
        return g_episode_status;
    }

    public void set_episode_status(int g_episode_status) {
        this.g_episode_status = g_episode_status;
    }

    public int get_episode_last() {
        return g_episode_last;
    }

    public void set_episode_last(int g_episode_last) {
        this.g_episode_last = g_episode_last;
    }

    public boolean is_series() {
        return get_series_key()[0] != 0;
    }

    public void set_channel_id(long channelId) {
        g_channel_id = channelId;
    }

    public long get_channel_id() {
        return g_channel_id;
    }
}
