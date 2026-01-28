package com.prime.sysdata;

import android.content.Context;

/**
 * Created by gary_hsu on 2017/11/14.
 */

public class BookInfo {
    private static final String TAG="BookInfo";
    public static final int MAX_NUM_OF_BOOKINFO = 20;

    private int BookId;
    private int ChannelId;
    private int GroupType;
    private String EventName;
    private int BookType; // 起機 , 錄影
    private int BookCycle; // 一次, 每天, 每周, 周末, 工作日
    private int Year;
    private int Month;
    private int Date;
    private int Week;
    private int StartTime; //單位 分
    private int Duration; //單位 分
    private int Enable;

    public String ToString(){
        return "[BookId  " + BookId + "ChannelId : " + ChannelId + "GroupType : " + GroupType + "EventName : " + EventName
                + "BookType : "+ BookType + "BookCycle : " + BookCycle + "Year : " + Year + "Month : " + Month
                + "Date : " + Date + "Week : " + Week + "StartTime : " + StartTime + "Duration : " + Duration
                + "Enable : " + Enable + "]";
    }

    public int getBookId() {
        return BookId;
    }

    public void setBookId(int bookId) {
        BookId = bookId;
    }

    public int getChannelId() {
        return ChannelId;
    }

    public void setChannelId(int channelId) {
        ChannelId = channelId;
    }

    public int getGroupType() {
        return GroupType;
    }

    public void setGroupType(int groupType) {
        GroupType = groupType;
    }

    public String getEventName() {
        return EventName;
    }

    public void setEventName(String eventName) {
        EventName = eventName;
    }

    public int getBookType() {
        return BookType;
    }

    public void setBookType(int bookType) {
        BookType = bookType;
    }

    public int getBookCycle() {
        return BookCycle;
    }

    public void setBookCycle(int bookCycle) {
        BookCycle = bookCycle;
    }

    public int getYear() {
        return Year;
    }

    public void setYear(int year) {
        Year = year;
    }

    public int getMonth() {
        return Month;
    }

    public void setMonth(int month) {
        Month = month;
    }

    public int getDate() {
        return Date;
    }

    public void setDate(int date) {
        Date = date;
    }

    public int getWeek() {
        return Week;
    }

    public void setWeek(int week) {
        Week = week;
    }

    public int getStartTime() {
        return StartTime;
    }

    public void setStartTime(int startTime) {
        StartTime = startTime;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int duration) {
        Duration = duration;
    }

    public int getEnable() {
        return Enable;
    }

    public void setEnable(int enable) {
        Enable = enable;
    }
}
