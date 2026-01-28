package com.prime.dtv.sysdata;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.PrimeTimerReceiver;
import com.prime.dtv.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    public static final int BOOK_CYCLE_MONTHLY = 5;
    public static final int BOOK_CYCLE_SERIES = 6;
    public static final int BOOK_CYCLE_SERIES_EMPTY = 7;
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

    private final int CHECK_CONFLICT_NUM = 60;
    private final int CHECK_WEEKLY_CONFLICT_NUM = 26;
    private final int CHECK_WEEKEND_CONFLICT_NUM = 52;
    private final int CHECK_WEEKDAYS_CONFLICT_NUM = 130;

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

    public Intent get_Intent() {
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

        if (BookType == BOOK_TYPE_RECORD)
            intent.setAction(PrimeTimerReceiver.ACTION_TIMER_RECORD);
        else if (BookType == BOOK_TYPE_POWER_ON)
            intent.setAction(PrimeTimerReceiver.ACTION_TIMER_POWER_ON);
        else if (BookType == BOOK_TYPE_CHANGE_CHANNEL)
            intent.setAction(PrimeTimerReceiver.ACTION_TIMER_CHANGE_CHANNEL);

        intent.putExtras(bundle);
        return intent;
    }

    @SuppressLint("SimpleDateFormat")
    public boolean check_conflict(BookInfo oldBookInfo) {

        List<Long> startTimeList = new ArrayList<>();
        List<Long> endTimeList = new ArrayList<>();
        get_time_list(startTimeList, endTimeList);
        /*for (Long startTime:startTimeList)
            Log.d(TAG, "check_conflict: startTime = " + startTime);
        for (Long endTime:endTimeList)
            Log.d(TAG, "check_conflict: endTime = " + endTime);*/

        List<Long> oldStartTimeList = new ArrayList<>();
        List<Long> oldEndTimeList = new ArrayList<>();
        oldBookInfo.get_time_list(oldStartTimeList, oldEndTimeList);

        /*for (Long startTime:oldStartTimeList)
            Log.d(TAG, "check_conflict: startTime = " + startTime);
        for (Long endTime:oldEndTimeList)
            Log.d(TAG, "check_conflict: endTime = " + endTime);*/

        for (int i = 0; i < startTimeList.size(); i++) {
            //Log.i(TAG, "check_conflict: new " + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(startTimeList.get(i))) + " ~ " + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(endTimeList.get(i))));

            for (int j = 0; j < oldStartTimeList.size(); j++) {
                //Log.i(TAG, "check_conflict: old start " + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(oldStartTimeList.get(j))) + " ~ " + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(oldEndTimeList.get(j))));
                if (startTimeList.get(i) >= oldStartTimeList.get(j) && startTimeList.get(i) < oldEndTimeList.get(j))
                    return true;
                if (endTimeList.get(i) > oldStartTimeList.get(j) && endTimeList.get(i) <= oldEndTimeList.get(j))
                    return true;
                if (oldStartTimeList.get(j) >= startTimeList.get(i) && oldStartTimeList.get(j) < endTimeList.get(i))
                    return true;
                if (oldEndTimeList.get(j) > startTimeList.get(i) && oldEndTimeList.get(j) <= endTimeList.get(i))
                    return true;
            }
        }

        Log.i(TAG, "check_conflict: false");
        return false;
        /*if ( getBookCycle() == BOOK_CYCLE_ONETIME)
            return check_conflict_onetime_and_other(oldBookInfo);
        else if ( getBookCycle() == BOOK_CYCLE_DAILY)
            return check_conflict_daily_and_other(oldBookInfo);
        else if ( getBookCycle() == BOOK_CYCLE_WEEKLY)
            return check_conflict_weekly_and_other(oldBookInfo);
        else if ( getBookCycle() == BOOK_CYCLE_WEEKEND)
            return check_conflict_weekend_and_other(oldBookInfo);
        else if ( getBookCycle() == BOOK_CYCLE_WEEKDAYS)
            return check_conflict_weekdays_and_other(oldBookInfo);
        else
            return false;*/
    }

    public void get_time_list(List<Long> startTimeList, List<Long> endTimeList) {
        Long startTimestamp = get_start_time_stamp();
        Long endTimestamp = get_end_time_stamp();

        switch (getBookCycle()) {
            case BOOK_CYCLE_ONETIME -> {
                startTimeList.add(startTimestamp);
                endTimeList.add(endTimestamp);
            }
            case BOOK_CYCLE_DAILY -> {
                for (int i = 0; i < CHECK_CONFLICT_NUM; i++) {
                    startTimeList.add(startTimestamp + (long)86400000 * i);
                    endTimeList.add(endTimestamp + (long)86400000 * i);
                }
            }
            case BOOK_CYCLE_WEEKLY -> {
                for (int i = 0; i < CHECK_WEEKLY_CONFLICT_NUM; i++) {
                    startTimeList.add(startTimestamp + (long)86400000 * i * 7);
                    endTimeList.add(endTimestamp + (long)86400000 * i * 7);
                }
            }
            case BOOK_CYCLE_WEEKEND -> {
                for (int i = 0; i < CHECK_CONFLICT_NUM; i++) {
                    if ((get_week_value() + i) % 7 == 0 || (get_week_value() + i) % 6 == 0) {
                        startTimeList.add(startTimestamp + (long)86400000 * i);
                        endTimeList.add(endTimestamp + (long)86400000 * i);
                    }
                }
            }
            case BOOK_CYCLE_WEEKDAYS -> {
                for (int i = 0; i < CHECK_CONFLICT_NUM; i++) {
                    if ((get_week_value() + i) % 7 != 0 || (get_week_value() + i) % 6 != 0) {
                        startTimeList.add(startTimestamp + (long)86400000 * i);
                        endTimeList.add(endTimestamp + (long)86400000 * i);
                    }
                }
            }
            case BOOK_CYCLE_SERIES -> {
                PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
                SeriesInfo.Series series = primeDtv.get_series(getChannelId(), getSeriesRecKey());

                if (series == null) {
                    Log.e(TAG, "get_time_list: series == null");
                    return;
                }

                for (SeriesInfo.Episode episode : series.getEpisodeList()) {
                    //Log.d(TAG, "get_time_list: " + episode.toString());
                    if(primeDtv.pvr_CheckSeriesEpisode(getSeriesRecKey(), episode.getEpisodeKey()) == 0)
                        continue;
                    startTimeList.add(get_start_time_stamp(episode.getStartLocalDateTime()));
                    endTimeList.add(get_end_time_stamp(episode.getStartLocalDateTime(), episode.getDuration()));
                }
            }
            default -> Log.e(TAG, "get_time_list: book type not match");
        }
    }

    public Long get_start_time_stamp() {
        LocalDateTime dateTime = LocalDateTime.of(Year, Month, Date, StartTime/100, StartTime%100); // 設定 2025/3/24 00:00:00
        long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        //Log.d(TAG, "get_start_time_stamp: " + dateTime.format(formatter));
        return timestamp;
    }

    public Long get_end_time_stamp() {
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
        return switch (Week) {
            case BookInfo.BOOK_WEEK_DAY_SUNDAY -> 7;
            case BookInfo.BOOK_WEEK_DAY_MONDAY -> 1;
            case BookInfo.BOOK_WEEK_DAY_TUESDAY -> 2;
            case BookInfo.BOOK_WEEK_DAY_WEDNESDAY -> 3;
            case BookInfo.BOOK_WEEK_DAY_THURSDAY -> 4;
            case BookInfo.BOOK_WEEK_DAY_FRIDAY -> 5;
            case BookInfo.BOOK_WEEK_DAY_SATURDAY -> 6;
            default -> 1;
        };
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
        return switch (getBookCycle()) {
            case BookInfo.BOOK_CYCLE_ONETIME -> activity.getString(R.string.book_cycle_one_time);
            case BookInfo.BOOK_CYCLE_DAILY -> activity.getString(R.string.book_cycle_daily);
            case BookInfo.BOOK_CYCLE_WEEKLY -> get_week_day(activity);
            case BookInfo.BOOK_CYCLE_WEEKEND -> activity.getString(R.string.book_cycle_weekend);
            case BookInfo.BOOK_CYCLE_WEEKDAYS -> activity.getString(R.string.book_cycle_weekdays);
            case BookInfo.BOOK_CYCLE_SERIES, BOOK_CYCLE_SERIES_EMPTY -> "";
            default -> "null";
        };
    }

    private String get_week_day(AppCompatActivity activity) {
        return switch (getWeek()) {
            case BookInfo.BOOK_WEEK_DAY_SUNDAY -> activity.getString(R.string.book_week_day_sunday);
            case BookInfo.BOOK_WEEK_DAY_MONDAY -> activity.getString(R.string.book_week_day_monday);
            case BookInfo.BOOK_WEEK_DAY_TUESDAY -> activity.getString(R.string.book_week_day_tuesday);
            case BookInfo.BOOK_WEEK_DAY_WEDNESDAY -> activity.getString(R.string.book_week_day_wednesday);
            case BookInfo.BOOK_WEEK_DAY_THURSDAY -> activity.getString(R.string.book_week_day_thursday);
            case BookInfo.BOOK_WEEK_DAY_FRIDAY -> activity.getString(R.string.book_week_day_friday);
            case BookInfo.BOOK_WEEK_DAY_SATURDAY -> activity.getString(R.string.book_week_day_saturday);
            default -> "null";
        };
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
}
