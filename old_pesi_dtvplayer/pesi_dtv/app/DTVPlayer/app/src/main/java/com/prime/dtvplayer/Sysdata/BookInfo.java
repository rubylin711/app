package com.prime.dtvplayer.Sysdata;


/**
 * Created by gary_hsu on 2017/11/14.
 */

public class BookInfo {
    private static final String TAG="BookInfo";
    public static final int MAX_NUM_OF_BOOKINFO = 20;
    public static final int BOOK_CYCLE_ONETIME = 0;
    public static final int BOOK_CYCLE_DAILY = 1;
    public static final int BOOK_CYCLE_WEEKLY = 2;
    public static final int BOOK_CYCLE_WEEKEND = 3;
    public static final int BOOK_CYCLE_WEEKDAYS = 4;
//    public static final int BOOK_CYCLE_MONTHLY = 5;

    public static final int BOOK_TYPE_RECORD = 0;
    public static final int BOOK_TYPE_TURNON = 1;

    private int BookId;
    private long ChannelId;
    private int GroupType;
    private String EventName;
    private int BookType; // 起機 , 錄影
    private int BookCycle; // 一次, 每天, 每周, 周末, 工作日
    private int Year;
    private int Month;
    private int Date;
    private int Week;   //0~6 Monday~Sunday
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

    public long getChannelId() {
        return ChannelId;
    }

    public void setChannelId(long channelId) {
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

    public void update(BookInfo bookInfo) {
        int i;
        if(this.BookId == bookInfo.getBookId()) {
            this.ChannelId = bookInfo.getChannelId();
            this.GroupType = bookInfo.getGroupType();
            this.EventName = bookInfo.getEventName();
            this.BookType = bookInfo.getBookType();
            this.BookCycle = bookInfo.getBookCycle();
            this.Year = bookInfo.getYear();
            this.Month = bookInfo.getMonth();
            this.Date = bookInfo.getDate();
            this.Week = bookInfo.getWeek();
            this.StartTime = bookInfo.getStartTime();
            this.Duration = bookInfo.getDuration();
            this.Enable = bookInfo.getEnable();
        }
    }
}
