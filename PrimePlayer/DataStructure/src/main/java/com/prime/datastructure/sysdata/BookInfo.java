package com.prime.datastructure.sysdata;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by gary_hsu on 2017/11/14.
 */

public class BookInfo implements Parcelable {
    public static final String TAG="BookInfo";

    public static final int ADVANCE_REMINDER_TIME = 0;// minute

    public static final int MAX_NUM_OF_BOOKINFO = 20;
    public static final int BOOK_CYCLE_ONETIME = 0;
    public static final int BOOK_CYCLE_DAILY = 1;
    public static final int BOOK_CYCLE_WEEKLY = 2;
    public static final int BOOK_CYCLE_WEEKEND = 3;
    public static final int BOOK_CYCLE_WEEKDAYS = 4;
    public static final int BOOK_CYCLE_MONTHLY = 5;
    public static final int BOOK_CYCLE_SERIES = 6;
    public static final int BOOK_CYCLE_SERIES_EMPTY = 7;
    public static final int BOOK_CYCLE_ONETIME_EMPTY = 8;
    public static final int BOOK_WEEK_DAY_SUNDAY = 0x1;
    public static final int BOOK_WEEK_DAY_MONDAY = 0x2;
    public static final int BOOK_WEEK_DAY_TUESDAY = 0x4;
    public static final int BOOK_WEEK_DAY_WEDNESDAY = 0x8;
    public static final int BOOK_WEEK_DAY_THURSDAY = 0x10;
    public static final int BOOK_WEEK_DAY_FRIDAY = 0x20;
    public static final int BOOK_WEEK_DAY_SATURDAY = 0x40;

    public static final int BOOK_TYPE_RECORD_NOW = -1;
    public static final int BOOK_TYPE_RECORD = 0;
    public static final int BOOK_TYPE_POWER_ON = 1;
    public static final int BOOK_TYPE_CHANGE_CHANNEL = 2;

    public static final String BOOK_ID = "BookId";
    public static final String CHANNEL_ID = "ChannelId";
    public static final String CHANNEL_NUM = "ChannelNum";
    public static final String GROUP_TYPE = "GroupType";
    public static final String EVENT_NAME = "EventName";
    public static final String BOOK_TYPE = "BookType";
    public static final String BOOK_CYCLE = "BookCycle";
    public static final String YEAR = "Year";
    public static final String MONTH = "Month";
    public static final String DATE = "Date";
    public static final String WEEK = "Week";
    public static final String START_TIME = "StartTime";
    public static final String START_TIME_MS = "StartTimeMs";
    public static final String DURATION = "Duration";
    public static final String DURATION_MS = "DurationMs";
    public static final String ENABLE = "Enable";
    public static final String IS_SERIES = "IsSeries";
    public static final String EPISODE = "Episode";
    public static final String SERIES_REC_KEY = "SeriesRecKey";
    public static final String EPG_EVENT_ID = "EpgEventId";
    public static final String ALLOW_AUTO_SELECT = "AllowAutoSelect";
    public static final String IS_4K = "Is4K";

    public static final int CHECK_CONFLICT_NUM = 60;
    public static final int CHECK_WEEKLY_CONFLICT_NUM = 26;
    public static final int CHECK_WEEKEND_CONFLICT_NUM = 52;
    public static final int CHECK_WEEKDAYS_CONFLICT_NUM = 130;

    public static final String ACTION_TIMER_RECORD          = "com.prime.action.timer_record";
    public static final String ACTION_TIMER_POWER_ON        = "com.prime.action.timer_power_on";
    public static final String ACTION_TIMER_CHANGE_CHANNEL  = "com.prime.action.timer_change_channel";

    private int BookId;
    private long ChannelId;
    private String ChannelNum;
    private int GroupType;
    private String EventName;
    private int BookType;
    private int BookCycle;
    private int Year;
    private int Month; // 1~12
    private int Date;
    private int Week;
    private int StartTime; // HHmm, 2345 = 23:45, 0000~2359
    private long StartTimeMs;
    private int Duration; // HHmm, 2345 = 23:45, 0000~2359
    private int DurationMs;
    private int Enable;
    private boolean IsSeries; // series/not series
    private int Episode; // value is which episode of series
    private byte[] SeriesRecKey; // series's key (unique)
    private int EpgEventId; // epg event id
    private int Status;
    private boolean AllowAutoSelect;
    private boolean Is4K;

    public BookInfo() {
        EpgEventId = -1;
    }

    public BookInfo(Bundle bundle) {
        BookId = bundle.getInt(BookInfo.BOOK_ID);
        ChannelId = bundle.getLong(BookInfo.CHANNEL_ID);
        ChannelNum = bundle.getString(BookInfo.CHANNEL_NUM);
        GroupType = bundle.getInt(BookInfo.GROUP_TYPE);
        EventName = bundle.getString(BookInfo.EVENT_NAME);
        BookType = bundle.getInt(BookInfo.BOOK_TYPE);
        BookCycle = bundle.getInt(BookInfo.BOOK_CYCLE);
        Year = bundle.getInt(BookInfo.YEAR);
        Month = bundle.getInt(BookInfo.MONTH);
        Date = bundle.getInt(BookInfo.DATE);
        Week = bundle.getInt(BookInfo.WEEK);
        StartTime = bundle.getInt(BookInfo.START_TIME);
        StartTimeMs = bundle.getLong(BookInfo.START_TIME_MS);
        Duration = bundle.getInt(BookInfo.DURATION);
        DurationMs = bundle.getInt(BookInfo.DURATION_MS);
        Enable = bundle.getInt(BookInfo.ENABLE);
        IsSeries = bundle.getBoolean(BookInfo.IS_SERIES);
        Episode = bundle.getInt(BookInfo.EPISODE);
        SeriesRecKey = bundle.getByteArray(BookInfo.SERIES_REC_KEY);
        EpgEventId = bundle.getInt(BookInfo.EPG_EVENT_ID);
        AllowAutoSelect = bundle.getBoolean(BookInfo.ALLOW_AUTO_SELECT);
        Is4K = bundle.getBoolean(BookInfo.IS_4K);
    }

    protected BookInfo(Parcel in) {
        BookId = in.readInt();
        ChannelId = in.readLong();
        ChannelNum = in.readString();
        GroupType = in.readInt();
        EventName = in.readString();
        BookType = in.readInt();
        BookCycle = in.readInt();
        Year = in.readInt();
        Month = in.readInt();
        Date = in.readInt();
        Week = in.readInt();
        StartTime = in.readInt();
        StartTimeMs = in.readLong();
        Duration = in.readInt();
        DurationMs = in.readInt();
        Enable = in.readInt();
        IsSeries = in.readByte() != 0;
        Episode = in.readInt();
        SeriesRecKey = in.createByteArray();
        EpgEventId = in.readInt();
        Status = in.readInt();
        AllowAutoSelect = in.readByte() != 0;
        Is4K = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(BookId);
        dest.writeLong(ChannelId);
        dest.writeString(ChannelNum);
        dest.writeInt(GroupType);
        dest.writeString(EventName);
        dest.writeInt(BookType);
        dest.writeInt(BookCycle);
        dest.writeInt(Year);
        dest.writeInt(Month);
        dest.writeInt(Date);
        dest.writeInt(Week);
        dest.writeInt(StartTime);
        dest.writeLong(StartTimeMs);
        dest.writeInt(Duration);
        dest.writeInt(DurationMs);
        dest.writeInt(Enable);
        dest.writeByte((byte) (IsSeries ? 1 : 0));
        dest.writeInt(Episode);
        dest.writeByteArray(SeriesRecKey);
        dest.writeInt(EpgEventId);
        dest.writeInt(Status);
        dest.writeByte((byte) (AllowAutoSelect ? 1 : 0));
        dest.writeByte((byte) (Is4K ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BookInfo> CREATOR = new Creator<BookInfo>() {
        @Override
        public BookInfo createFromParcel(Parcel in) {
            return new BookInfo(in);
        }

        @Override
        public BookInfo[] newArray(int size) {
            return new BookInfo[size];
        }
    };

    public String ToString() {
        return "[BookId  " + BookId + " ChannelId : " + ChannelId + " GroupType : " + GroupType + " EventName : " + EventName
                + " BookType : "+ BookType + " BookCycle : " + BookCycle + " Year : " + Year + " Month : " + Month
                + " Date : " + Date + " Week : " + Week + " StartTime : " + StartTime + " Duration : " + Duration
                + " Enable : " + Enable + " IsSeries : " + IsSeries + " Episode: " + Episode
                + " SeriesRecKey: " + Arrays.toString(SeriesRecKey) + "EPGEventId: " + EpgEventId + "]";
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

    public String getChannelNum() {
        return ChannelNum;
    }

    public void setChannelNum(String channelNum) {
        ChannelNum = channelNum;
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

    public long getStartTimeMs() {
        return StartTimeMs;
    }

    public void setStartTimeMs(long startTimeMs) {
        StartTimeMs = startTimeMs;
    }

    public boolean isAutoSelect() {
        return AllowAutoSelect;
    }

    public void setAutoSelect(boolean allowAutoSelect) {
        AllowAutoSelect = allowAutoSelect;
    }

    public boolean getAutoSelect() {
        return AllowAutoSelect;
    }

    public boolean is4K() {
        return Is4K;
    }

    public void set4K(boolean is4K) {
        Is4K = is4K;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int duration) {
        Duration = duration;
    }

    public int getDurationMs() {
        return DurationMs;
    }

    public void setDurationMs(int durationMs) {
        DurationMs = durationMs;
    }

    public int getEnable() {
        return Enable;
    }

    public void setEnable(int enable) {
        Enable = enable;
    }

    public boolean isSeries() {
        return IsSeries;
    }

    public void setSeries(boolean isSeries) {
        IsSeries = isSeries;
    }

    public int getEpisode() {
        return Episode;
    }

    public void setEpisode(int episode) {
        Episode = episode;
    }

    public byte[] getSeriesRecKey() {
        return SeriesRecKey;
    }

    public void setSeriesRecKey(byte[] seriesRecKey) {
        SeriesRecKey = seriesRecKey;
    }

    public int getEpgEventId() {
        return EpgEventId;
    }

    public void setEpgEventId(int epgEventId) {
        EpgEventId = epgEventId;
    }

    public void update(BookInfo bookInfo) {
        if(this.BookId == bookInfo.getBookId()) {
            this.ChannelId = bookInfo.getChannelId();
            this.ChannelNum = bookInfo.getChannelNum();
            this.GroupType = bookInfo.getGroupType();
            this.EventName = bookInfo.getEventName();
            this.BookType = bookInfo.getBookType();
            this.BookCycle = bookInfo.getBookCycle();
            this.Year = bookInfo.getYear();
            this.Month = bookInfo.getMonth();
            this.Date = bookInfo.getDate();
            this.Week = bookInfo.getWeek();
            this.StartTime = bookInfo.getStartTime();
            this.StartTimeMs = bookInfo.getStartTimeMs();
            this.Duration = bookInfo.getDuration();
            this.DurationMs = bookInfo.getDurationMs();
            this.Enable = bookInfo.getEnable();
            this.IsSeries = bookInfo.isSeries();
            this.Episode = bookInfo.getEpisode();
            this.SeriesRecKey = bookInfo.getSeriesRecKey();
            this.EpgEventId = bookInfo.getEpgEventId();
            this.AllowAutoSelect = bookInfo.isAutoSelect();
            this.Is4K = bookInfo.is4K();
        }
    }

    public Intent getIntent() {
        Bundle bundle = new Bundle();
        bundle.putInt(BookInfo.BOOK_ID, BookId);
        bundle.putLong(BookInfo.CHANNEL_ID, ChannelId);
        bundle.putString(BookInfo.CHANNEL_NUM, ChannelNum);
        bundle.putInt(BookInfo.GROUP_TYPE, GroupType);
        bundle.putString(BookInfo.EVENT_NAME, EventName);
        bundle.putInt(BookInfo.BOOK_TYPE, BookType);
        bundle.putInt(BookInfo.BOOK_CYCLE, BookCycle);
        bundle.putInt(BookInfo.YEAR, Year);
        bundle.putInt(BookInfo.MONTH, Month);
        bundle.putInt(BookInfo.DATE, Date);
        bundle.putInt(BookInfo.WEEK, Week);
        bundle.putInt(BookInfo.START_TIME, StartTime);
        bundle.putLong(BookInfo.START_TIME_MS, StartTimeMs);
        bundle.putInt(BookInfo.DURATION, Duration);
        bundle.putInt(BookInfo.DURATION_MS, DurationMs);
        bundle.putInt(BookInfo.ENABLE, Enable);
        bundle.putBoolean(BookInfo.IS_SERIES, IsSeries);
        bundle.putInt(BookInfo.EPISODE, Episode);
        bundle.putByteArray(BookInfo.SERIES_REC_KEY, SeriesRecKey);
        bundle.putInt(BookInfo.EPG_EVENT_ID, EpgEventId);
        bundle.putBoolean(BookInfo.ALLOW_AUTO_SELECT, AllowAutoSelect);
        bundle.putBoolean(BookInfo.IS_4K, Is4K);

        Intent intent = new Intent();

        if (BookType == BOOK_TYPE_RECORD) {
            intent.setAction(ACTION_TIMER_RECORD);
        } else if (BookType == BOOK_TYPE_POWER_ON) {
            intent.setAction(ACTION_TIMER_POWER_ON);
        } else if (BookType == BOOK_TYPE_CHANGE_CHANNEL) {
            intent.setAction(ACTION_TIMER_CHANGE_CHANNEL);
        }

        intent.putExtras(bundle);
        return intent;
    }

    public long get_start_time_stamp() {
        LocalDateTime dateTime = LocalDateTime.of(Year, Month, Date, StartTime/100, StartTime%100); // 設定 2025/3/24 00:00:00
        long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        //Log.d(TAG, "get_start_time_stamp: " + dateTime.format(formatter));
        return timestamp;
    }

    public long get_end_time_stamp() {
        LocalDateTime dateTime = LocalDateTime.of(Year, Month, Date, StartTime/100, StartTime%100); // 設定 2025/3/24 00:00:00
        LocalDateTime endDateTime = dateTime.plusHours(Duration/100).plusMinutes(Duration%100);
        long timestamp = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        //Log.d(TAG, "get_end_time_stamp: " + endDateTime.format(formatter));
        return timestamp;
    }

    public Long get_start_time_stamp(LocalDateTime localDateTime) {
        long timestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        //Log.d(TAG, "get_start_time_stamp: " + localDateTime.format(formatter));
        return timestamp;
    }

    public Long get_end_time_stamp(LocalDateTime localDateTime, int duration) {
        LocalDateTime endDateTime = localDateTime.plusMinutes(duration/60);
        long timestamp = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        //Log.d(TAG, "get_end_time_stamp: " + endDateTime.format(formatter));
        return timestamp;
    }

    public int get_week_value() {
        switch (Week) {
            case BookInfo.BOOK_WEEK_DAY_SUNDAY:
                return 7;
            case BookInfo.BOOK_WEEK_DAY_MONDAY:
                return 1;
            case BookInfo.BOOK_WEEK_DAY_TUESDAY:
                return 2;
            case BookInfo.BOOK_WEEK_DAY_WEDNESDAY:
                return 3;
            case BookInfo.BOOK_WEEK_DAY_THURSDAY:
                return 4;
            case BookInfo.BOOK_WEEK_DAY_FRIDAY:
                return 5;
            case BookInfo.BOOK_WEEK_DAY_SATURDAY:
                return 6;
            default:
                return 1;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public String get_time_to_sting_format() {
        int year = getYear();
        int month = getMonth();
        int date = getDate();
        int hour = getStartTime()/100;
        int min = getStartTime()%100;

        long endHour = (long) (getDuration() / 100) *60*60*1000;
        long endMin = (long) (getDuration()%100) *60*1000;

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(year, month-1, date, hour, min);
        Date startDate = startCalendar.getTime();
        Date endDate = new Date(startDate.getTime() + endHour + endMin);

        String startTime = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(startDate);
        String endTime = new SimpleDateFormat("HH:mm").format(endDate);

        return startTime + "~" + endTime;
    }

    public String get_book_cycle_string(AppCompatActivity activity) {
        Log.e(TAG,"this function not porting!! please check");
        switch (getBookCycle()) {
//            case BookInfo.BOOK_CYCLE_ONETIME -> activity.getString(R.string.book_cycle_one_time);
//            case BookInfo.BOOK_CYCLE_DAILY -> activity.getString(R.string.book_cycle_daily);
//            case BookInfo.BOOK_CYCLE_WEEKLY -> get_week_day(activity);
//            case BookInfo.BOOK_CYCLE_WEEKEND -> activity.getString(R.string.book_cycle_weekend);
//            case BookInfo.BOOK_CYCLE_WEEKDAYS -> activity.getString(R.string.book_cycle_weekdays);
//            case BookInfo.BOOK_CYCLE_SERIES, BOOK_CYCLE_SERIES_EMPTY -> "";
            default:
                return "null";
        }
    }

    private String get_week_day(AppCompatActivity activity) {
        Log.e(TAG,"this function not porting!! please check");
        switch (getWeek()) {
//            case BookInfo.BOOK_WEEK_DAY_SUNDAY -> activity.getString(R.string.book_week_day_sunday);
//            case BookInfo.BOOK_WEEK_DAY_MONDAY -> activity.getString(R.string.book_week_day_monday);
//            case BookInfo.BOOK_WEEK_DAY_TUESDAY -> activity.getString(R.string.book_week_day_tuesday);
//            case BookInfo.BOOK_WEEK_DAY_WEDNESDAY -> activity.getString(R.string.book_week_day_wednesday);
//            case BookInfo.BOOK_WEEK_DAY_THURSDAY -> activity.getString(R.string.book_week_day_thursday);
//            case BookInfo.BOOK_WEEK_DAY_FRIDAY -> activity.getString(R.string.book_week_day_friday);
//            case BookInfo.BOOK_WEEK_DAY_SATURDAY -> activity.getString(R.string.book_week_day_saturday);
            default:
                return "null";
        }
    }

    public String get_formatted_start_time(String format) {
        LocalDateTime localDateTime = LocalDateTime.of(
                Year,
                Month,
                Date,
                StartTime / 100,
                StartTime % 100);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(formatter);
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public String getName() {
        return getChannelNum() + " " + getEventName();
    }

    public String getKey() {
        return getChannelId() + " " + getEpgEventId();
    }

    @NonNull
    @Override
    public String toString() {
        return "BookInfo{" +
                "AllowAutoSelect=" + AllowAutoSelect +
                ", BookId=" + BookId +
                ", ChannelId=" + ChannelId +
                ", ChannelNum='" + ChannelNum + '\'' +
                ", GroupType=" + GroupType +
                ", EventName='" + EventName + '\'' +
                ", BookType=" + BookType +
                ", BookCycle=" + BookCycle +
                ", Year=" + Year +
                ", Month=" + Month +
                ", Date=" + Date +
                ", Week=" + Week +
                ", StartTime=" + StartTime +
                ", StartTimeMs=" + StartTimeMs +
                ", Duration=" + Duration +
                ", DurationMs=" + DurationMs +
                ", Enable=" + Enable +
                ", IsSeries=" + IsSeries +
                ", Episode=" + Episode +
                ", SeriesRecKey=" + Arrays.toString(SeriesRecKey) +
                ", EpgEventId=" + EpgEventId +
                ", Status=" + Status +
                ", Is4K=" + Is4K +
                '}';
    }
}
