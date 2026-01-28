package com.prime.dtvplayer.Sysdata;

/**
 * Created by gary_hsu on 2017/11/13.
 */

public class EPGEvent {
    public static final String TAG = "EPGEvent";
    public static final int EPG_TYPE_PRESENT = 0;
    public static final int EPG_TYPE_FOLLOW = 1;
    public static final int EPG_TYPE_SCHEDULE = 2;
    private int Sid;
    private int OriginalNetworkId;
    private int TransportStreamId;
    private int EventId;
    private int TableId;
    private int EventType; //present/follow/schedule
    private String EventName;
    private String EventNameLangCodec;
    private long startTime;
    private long endTime;
    private long Duration;
    private int ParentalRate;
    private String ShortEvent;
    private String ShortEventLangCodec;
    private String ExtendedEvent;
    private String ExtendedEventLangCodec;

    public String ToString(){
        return "[Sid  " + Sid + "OriginalNetworkId : " + OriginalNetworkId + "TransportStreamId : " + TransportStreamId
                + "EventId : " + EventId + "TableId : "+ TableId + "EventType : " + EventType + "EventName : "+ EventName + "EventNameLangCodec : "+ EventNameLangCodec
                + "StartTime : " + startTime + "EndTime : "+ endTime + "Duration : "+ Duration + "ParentalRate : "
                + ParentalRate + "ShortEvent : " + ShortEvent + "ShortEventLangCodec : " + ShortEventLangCodec + "ShortEvent : " + ShortEvent
                + "ExtendedEventLangCodec : "+ ExtendedEventLangCodec + "]";
    }

    public int getSid() {
        return Sid;
    }

    public void setSid(int sid) {
        Sid = sid;
    }

    public int getOriginalNetworkId() {
        return OriginalNetworkId;
    }

    public void setOriginalNetworkId(int originalNetworkId) {
        OriginalNetworkId = originalNetworkId;
    }

    public int getTransportStreamId() {
        return TransportStreamId;
    }

    public void setTransportStreamId(int transportStreamId) {
        TransportStreamId = transportStreamId;
    }

    public int getEventId() {
        return EventId;
    }

    public void setEventId(int eventId) {
        EventId = eventId;
    }

    public int getTableId() {
        return TableId;
    }

    public void setTableId(int tableId) {
        TableId = tableId;
    }

    public String getEventName() {
        return EventName;
    }

    public void setEventName(String eventName) {
        EventName = eventName;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long value)
    {
        startTime = value;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long value)
    {
        endTime = value;
    }

    public long getDuration() {
        return Duration;
    }

    public void setDuration(long duration) {
        Duration = duration;
    }

    public int getParentalRate() {
        return ParentalRate;
    }

    public void setParentalRate(int parentalRate) {
        ParentalRate = parentalRate;
    }

    public String getShortEvent() {
        return ShortEvent;
    }

    public void setShortEvent(String shortEvent) {
        ShortEvent = shortEvent;
    }

    public String getExtendedEvent() {
        return ExtendedEvent;
    }

    public void setExtendedEvent(String extendedEvent) {
        ExtendedEvent = extendedEvent;
    }

    public String getEventNameLangCodec() {
        return EventNameLangCodec;
    }

    public void setEventNameLangCodec(String eventNameLangCodec) {
        EventNameLangCodec = eventNameLangCodec;
    }

    public String getShortEventLangCodec() {
        return ShortEventLangCodec;
    }

    public void setShortEventLangCodec(String shortEventLangCodec) {
        ShortEventLangCodec = shortEventLangCodec;
    }

    public String getExtendedEventLangCodec() {
        return ExtendedEventLangCodec;
    }

    public void setExtendedEventLangCodec(String extendedEventLangCodec) {
        ExtendedEventLangCodec = extendedEventLangCodec;
    }

    public int getEventType() {
        return EventType;
    }

    public void setEventType(int eventType) {
        EventType = eventType;
    }
}
