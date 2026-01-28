package com.prime.dmg.launcher.EPG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.FavInfo;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.SimpleChannel;
import com.prime.dtv.utils.LogUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Epg{
    private static final String TAG = "Epg";
    private static final int ONETIME_OBTAINED_NUMBER = 75;

    private PrimeDtv g_dtv;
    Epg(PrimeDtv primeDtv ) {
        g_dtv = primeDtv;
    }

    private static ArrayList<List<EPGEvent>> fakeEpgEventList;

    public  void fakeData () {
        fakeEpgEventList = new ArrayList<>();
        for (int i = 0; i < 10; i ++) {
            List<EPGEvent> tempList = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                EPGEvent programInfo = new EPGEvent();
                programInfo.set_event_id(j);
                programInfo.set_event_name("頻道:" + i + "節目:" + j);
                programInfo.set_start_time(1633024800000L+ ((long)i)*3600000L);
                tempList.add(programInfo);
            }
            fakeEpgEventList.add(tempList);
        }
        Log.d(TAG, "fakeEpgEventList = " + fakeEpgEventList);
    }

    public List<SimpleChannel> g_curr_simple_channel_list() {
        return g_dtv.get_cur_play_channel_list();
    }

    public List<ProgramInfo> get_program_info_list_by_genre(int genreId) {
        if (genreId == FavGroup.ALL_TV_TYPE)
            return get_all_tv_program_info_list();
        else
            return get_program_info_list(genreId, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
    }

    public List<ProgramInfo> get_program_info_list(int type, int pos, int num) {
        return g_dtv.get_program_info_list(type, pos, num);
    }

    public List<ProgramInfo> get_all_tv_program_info_list() {
        List<ProgramInfo> allChannels, TypeTvChannels, TypeRadioChannels;
        TypeTvChannels    = get_program_info_list(FavGroup.ALL_TV_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
        LogUtils.d("TV number: "+ TypeTvChannels.size());
        TypeRadioChannels = get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
        LogUtils.d("Radio number: "+ TypeRadioChannels.size());

        allChannels = new ArrayList<>();
        allChannels.addAll(TypeTvChannels);
        allChannels.addAll(TypeRadioChannels);
        allChannels.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));
        return allChannels;
    }

    public List<FavInfo> fav_info_get_list(int genreId) {
        return g_dtv.fav_info_get_list(genreId);
    }

    public boolean check_genre_group(ProgramInfo programInfo, int genreId) {
        List<FavInfo> favInfoList = fav_info_get_list(genreId);

        for (FavInfo favInfo: favInfoList) {
            if (favInfo.getChannelId() == programInfo.getChannelId())
                return true;
        }
        return false;
    }

    public ProgramInfo get_program_by_channel_id(long channelId) {
        return g_dtv.get_program_by_channel_id(channelId);
    }

    public Date get_dtv_date() {
        return g_dtv.get_dtv_date();
    }

    public int get_dtv_time_zone() {
        return g_dtv.get_dtv_timezone();
    }

    public List<EPGEvent> get_epg_events(long channelId, long startTime, long endTime, int addEmpty) {
        ArrayList<EPGEvent> epgEventList = new ArrayList<>();

        if (( channelId >= 0) && (startTime >= 0)  && (endTime-startTime > 0))
        {
            int i = 0;
            Date startDate = new Date(startTime);
            Date endDate = new Date(endTime);

            Log.d(TAG, "get_epg_events: start time = " + startTime + ", " + MiniEPG.ms_to_time(startTime, "yyyy/MM/dd HH:mm:ss"));
            Log.d(TAG, "get_epg_events: end time   = " + endTime   + ", " + MiniEPG.ms_to_time(endTime, "yyyy/MM/dd HH:mm:ss"));
            Log.d(TAG, "get_epg_events: duration   = " + (endTime - startTime) + ", " + MiniEPG.ms_to_duration(endTime - startTime));

            while (true)
            {
                // update start offset position
                int offset = ONETIME_OBTAINED_NUMBER * (i++);

                // obtain event list by filter with channel and time enabled
                List<EPGEvent> secondaryList = get_epg_events(channelId, startDate, endDate, offset, ONETIME_OBTAINED_NUMBER, addEmpty);
                if (null == secondaryList)
                {
                    // while no event is obtained
                    Log.d(TAG, "get_epg_events: while no event is obtained");
                    break;
                }

                epgEventList.addAll(secondaryList);
                int count = secondaryList.size();
                if (count < ONETIME_OBTAINED_NUMBER)
                {
                    // while events by filter are all obtained
                    Log.d(TAG, "get_epg_events: while events by filter are all obtained");
                    break;
                }

                EPGEvent lastEvent = secondaryList.get(count - 1);
                if (null == lastEvent)
                {
                    // while the last event is invalid
                    Log.d(TAG, "get_epg_events: while the last event is invalid");
                    break;
                }

                Date lastEndDate = new Date(lastEvent.get_end_time());
                if (lastEndDate.after(endDate))
                {
                    // while endTime of last event is after endTime of filter
                    Log.d(TAG, "get_epg_events: while endTime of last event is after endTime of filter");
                    break;
                }

                // update endTime of filter by endTime of last event as startTime of filter to next time
                startTime = lastEvent.get_end_time();
                startDate = new Date(startTime);
            }
        }
        else
        {
            Log.w(TAG, "get_epg_events: one or more parameter is invalid.");
        }

        ArrayList<EPGEvent> result = new ArrayList<>();
        for (EPGEvent element : epgEventList) {
            if (!result.contains(element)) {
                element.set_channel_id(channelId);
                result.add(element);
            }
        }

        Log.d(TAG, "get_epg_events: Program List size = " + result.size());
        return result;
    }

    public List<EPGEvent> get_epg_events(long channelID, Date startTime, Date endTime, int pos, int reqNum, int addEmpty) {
        return g_dtv.get_epg_events(channelID, startTime, endTime, pos, reqNum, addEmpty);
    }

    public String get_detail_description(long channelId, int eventId) {
        String desc = g_dtv.get_detail_description(channelId, eventId);
        //Log.d(TAG, "get_detail_description: detail = " + g_dtv.get_detail_description(channelId, eventId));
        if (desc == null || desc.equals(""))
            desc = g_dtv.get_short_description(channelId, eventId);
        //Log.d(TAG, "get_detail_description: short = " + g_dtv.get_short_description(channelId, eventId));
        return desc;
    }

    public void gpos_info_update_by_key_string(String key, int value) {
        g_dtv.gpos_info_update_by_key_string(key, value);
    }

    public void save_table(EnTableType tableType) {
        g_dtv.save_table(tableType);
    }

    public GposInfo gpos_info_get() {
        return g_dtv.gpos_info_get();
    }

    public boolean get_tuner_status(int tunerId) {
        return g_dtv.get_tuner_status(tunerId);
    }

    public void set_alarm(Context context, Intent intent) {
        g_dtv.set_alarms(context, intent);
    }

    public void cancel_alarms(Context context, Intent  intent) {
        g_dtv.cancel_alarms(context, intent);
    }

    public BookInfo new_timer(int type,  int cycle, EPGEvent event, ProgramInfo programInfo) {
        BookInfo bookInfo = get_new_bookinfo(type, cycle, event, programInfo);
        Log.d(TAG, "new_timer: " + bookInfo.ToString());

        g_dtv.book_info_add(bookInfo);
        return bookInfo;
    }

    public void new_timer(BookInfo bookInfo) {
        Log.d(TAG, "new_timer: " + bookInfo.ToString());
        g_dtv.book_info_add(bookInfo);
    }

    public void add_series(BookInfo bookInfo) {
        g_dtv.add_series(bookInfo.getChannelId(), bookInfo.getSeriesRecKey());
    }

    public void delete_series(BookInfo bookInfo) {
        g_dtv.delete_series(bookInfo.getChannelId(), bookInfo.getSeriesRecKey());
    }

    public void save_series() {
        g_dtv.save_series();
    }

    public BookInfo get_new_bookinfo(int type,  int cycle, EPGEvent event, ProgramInfo programInfo) {
        BookInfo bookInfo = new BookInfo();
        bookInfo.setBookId(get_new_book_id());
        bookInfo.setChannelId(programInfo.getChannelId());
        bookInfo.setGroupType(0);
        bookInfo.setEventName(event.get_event_name());
        bookInfo.setBookType(type);
        bookInfo.setBookCycle(cycle);
        Calendar startCalendar = get_calendar(event, true);
        bookInfo.setYear(startCalendar.get(Calendar.YEAR));
        bookInfo.setMonth(startCalendar.get(Calendar.MONTH) + 1);
        bookInfo.setDate(startCalendar.get(Calendar.DATE));
        bookInfo.setWeek(get_cycle_day_value(startCalendar.get(Calendar.DAY_OF_WEEK)));
        bookInfo.setSeriesRecKey(event.get_series_key());
        bookInfo.setEpisode(event.get_episode_key());
        bookInfo.setSeries(cycle==BookInfo.BOOK_CYCLE_SERIES?true:false);

        bookInfo.setStartTime(startCalendar.get(Calendar.HOUR_OF_DAY)*100+startCalendar.get(Calendar.MINUTE));
        bookInfo.setDuration(get_duration_int_format(event.get_duration()));
        bookInfo.setDurationMs((int) event.get_duration());
        bookInfo.setEnable(0);
        bookInfo.setEpgEventId(event.get_event_id());
        return bookInfo;
    }

    public BookInfo get_now_record_bookinfo(int type,  int cycle, EPGEvent event, ProgramInfo programInfo) {
        BookInfo bookInfo = new BookInfo();
        bookInfo.setBookId(get_new_book_id());
        bookInfo.setChannelId(programInfo.getChannelId());
        bookInfo.setGroupType(0);
        bookInfo.setEventName(event.get_event_name());
        bookInfo.setBookType(type);
        bookInfo.setBookCycle(cycle);
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(new Date());

        bookInfo.setYear(startCalendar.get(Calendar.YEAR));
        bookInfo.setMonth(startCalendar.get(Calendar.MONTH) + 1);
        bookInfo.setDate(startCalendar.get(Calendar.DATE));
        bookInfo.setWeek(get_cycle_day_value(startCalendar.get(Calendar.DAY_OF_WEEK)));
        bookInfo.setSeriesRecKey(event.get_series_key());
        bookInfo.setEpisode(event.get_episode_key());
        bookInfo.setSeries(cycle == BookInfo.BOOK_CYCLE_SERIES);

        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(new Date());
        Calendar endCalendar = get_calendar(event, false);


        bookInfo.setStartTime(startCalendar.get(Calendar.HOUR_OF_DAY)*100+startCalendar.get(Calendar.MINUTE));
        bookInfo.setDuration(get_duration_int_format(nowCalendar, endCalendar));
        bookInfo.setEnable(0);
        bookInfo.setEpgEventId(event.get_event_id());
        return bookInfo;
    }

    public void cancel_timer(Context context, BookInfo bookInfo) {
        g_dtv.book_info_delete(bookInfo.getBookId());
        if (bookInfo.isSeries()) {
            delete_series(bookInfo);
            new Thread(this::save_series);
        }

        cancel_alarms(context, bookInfo.get_Intent());
    }

    public int get_new_book_id() {
        List<BookInfo> allBookInfoList = new ArrayList<>();

        if (g_dtv != null) {
            allBookInfoList = g_dtv.book_info_get_list();

        }
        Log.d(TAG, "get_new_book_id: All Book Info size = " + allBookInfoList.size());

        if (allBookInfoList == null || allBookInfoList.size() == 0)
            return 0;

        return allBookInfoList.get(allBookInfoList.size()-1).getBookId() +1;
    }

    public Calendar get_calendar(EPGEvent event, boolean isStart) {
        Date date = new Date();
        if (isStart)
            date.setTime(event.get_start_time());
        else
            date.setTime(event.get_end_time());

        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        return ca;
    }

    private int get_cycle_day_value(int cycleDayIndex) {
        return switch (cycleDayIndex) {
            case 2 -> BookInfo.BOOK_WEEK_DAY_MONDAY;
            case 3 -> BookInfo.BOOK_WEEK_DAY_TUESDAY;
            case 4 -> BookInfo.BOOK_WEEK_DAY_WEDNESDAY;
            case 5 -> BookInfo.BOOK_WEEK_DAY_THURSDAY;
            case 6 -> BookInfo.BOOK_WEEK_DAY_FRIDAY;
            case 7 -> BookInfo.BOOK_WEEK_DAY_SATURDAY;
            case 1 -> BookInfo.BOOK_WEEK_DAY_SUNDAY;
            default -> BookInfo.BOOK_WEEK_DAY_MONDAY;
        };
    }

    public List<BookInfo> bookInfo_find_conflict_books(BookInfo bookInfo) {
        return g_dtv.book_info_find_conflict_records(bookInfo);
    }

    private int get_duration_int_format(long duration) {
        int diffSeconds = (int)(duration/1000);
        int returnDuration = diffSeconds/3600*100 + diffSeconds%3600/60;// HHmm, 2345 = 23:45, 0000~2359
        //Log.d(TAG, "get_duration_int_format: returnDuration = " + returnDuration);
        return returnDuration;
    }

    private int get_duration_int_format(Calendar nowCalendar, Calendar endCalendar) {
        long duration = endCalendar.getTime().getTime() - nowCalendar.getTime().getTime();
        //Log.d(TAG, "get_duration_int_format: endTime = " + endCalendar.getTime().getTime());
        //Log.d(TAG, "get_duration_int_format: nowTime = " + nowCalendar.getTime().getTime());

        int diffSeconds = (int)(duration/1000);
        //Log.d(TAG, "get_duration_int_format: diffSeconds = " + diffSeconds);

        long returnDuration = diffSeconds/3600*100 + diffSeconds%3600/60;// HHmm, 2345 = 23:45, 0000~2359
        //Log.d(TAG, "get_duration_int_format: returnDuration = " + returnDuration);
        return (int)returnDuration;
    }

    public static String get_channel_full_name(ProgramInfo channel) {
        if (channel == null)
            return "NULL";
        return channel.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM) + " " + channel.getDisplayName();
    }
}
