package com.prime.homeplus.tv.utils;

import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContentRating;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.Program;
import androidx.tvprovider.media.tv.TvContractCompat;
import androidx.tvprovider.media.tv.TvContractUtils;

import com.prime.datastructure.CommuincateInterface.BookModule;
import com.prime.datastructure.CommuincateInterface.MiscDefine;
import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.homeplus.tv.PrimeHomeplusTvApplication;
import com.prime.homeplus.tv.data.ScheduledProgramData;
import com.prime.homeplus.tv.service.RecordingService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ScheduledProgramUtils {
    private static final String TAG = "ScheduledProgramUtils";
//    private static List<ScheduledProgramData> debugScheduledPrograms = new ArrayList<>();
    private static boolean isInit = false;

    private static final List<ScheduledProgramData> cachedScheduledPrograms = new CopyOnWriteArrayList<>();

    private static List<ScheduledProgramData> getTestScheduledData() {
        List<ScheduledProgramData> list = new ArrayList<>();
        for (int i = 0; i <= 4; i++) {
            ScheduledProgramData data = new ScheduledProgramData();
            data.setId(i);
            data.setChannelId(i);

            String contentRatings = "com.android.tv/DVB/DVB_6";
            if (i == 2) {
                contentRatings = "com.android.tv/DVB/DVB_12";
            } else if (i == 3) {
                contentRatings = "com.android.tv/DVB/DVB_18";
            }
            data.setContentRatings(contentRatings);

            data.setChannelNumber("" + i);
            data.setChannelName("TEST_SCH_CHANNEL_" + i);
            data.setTitle("TEST_SCH_TITLE_" + i);

            if (i == 0) {
                data.setEpisodeDisplayNumber("1");
                data.setEpisodeTitle("TEST_SCH_EPISODE");
            }

            if (i == 4) {
                long now = System.currentTimeMillis();
                data.setStartTimeUtcMillis(now - 60000L);
                data.setEndTimeUtcMillis(now + 60000L);
            } else {
                data.setStartTimeUtcMillis(1557385200000L);
                data.setEndTimeUtcMillis(1557388800000L);
            }

            list.add(data);
        }
        return list;
    }

    public static synchronized List<ScheduledProgramData> getScheduledPrograms(Context context) {
//        // TODO: if debugScheduledPrograms is empty, create dummy data for UI testing
//        if (debugScheduledPrograms != null && debugScheduledPrograms.isEmpty() && !isInit) {
//            isInit = true;
//            debugScheduledPrograms = getTestScheduledData();
//        }
//        return debugScheduledPrograms;

        if (!isInit) {
            refreshCacheFromPesi(context);
        }

        return new ArrayList<>(cachedScheduledPrograms);
    }

    public static synchronized Uri insertScheduledProgram(Context context, ScheduledProgramData data) {
        Log.d(TAG, "insertScheduledProgram: data = " + data);
        if (data == null) {
            Log.e(TAG, "Cannot insert null ScheduledProgramData");
            return null;
        }

//        if (debugScheduledPrograms != null && debugScheduledPrograms.isEmpty() && !isInit) {
//            isInit = true;
//            debugScheduledPrograms = getTestScheduledData();
//        }
//
//        if (debugScheduledPrograms != null) {
//            debugScheduledPrograms.add(data);
//        }

        if (!isInit) {
            refreshCacheFromPesi(context);
        }

        BookInfo bookInfo = toPesiBookInfo(context, data);
        if (bookInfo == null) {
            Log.e(TAG, "insertScheduledProgram: fail to build BookInfo from ScheduledProgramData");
            return null;
        }

        PrimeDtvServiceInterface primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
        if (primeDtv == null) {
            Log.e(TAG, "insertScheduledProgram: null primeDtv");
            return null;
        }

        int result = primeDtv.book_info_add(bookInfo);
        if (result == MiscDefine.COMMAND_REPLY_FAIL) {
            Log.e(TAG, "insertScheduledProgram: book_info_add failed");
            return null;
        }

        cachedScheduledPrograms.add(data); // add to cache
        if (isNowRecordingProgram(data)) {
            // start recording
            Intent serviceIntent = new Intent(context, RecordingService.class);
            serviceIntent.setAction(BookModule.ACTION_START_RECORD);
            serviceIntent.putExtra(BookInfo.BOOK_ID, data.getId());
            ContextCompat.startForegroundService(context, serviceIntent);
        } else {
            // set alarm to start recording
            primeDtv.set_alarm(bookInfo);
        }

        return Uri.EMPTY; // no uri for our bookinfo, just return a non null uri
    }

    public static synchronized boolean deleteScheduledProgram(Context context, long scheduledProgramId) {
        Log.d(TAG, "deleteScheduledProgram: id = " + scheduledProgramId);
//        if (debugScheduledPrograms != null && !debugScheduledPrograms.isEmpty()) {
//            for (int i = 0; i <= debugScheduledPrograms.size(); i++) {
//                if (scheduledProgramId == debugScheduledPrograms.get(i).getId()) {
//                    debugScheduledPrograms.remove(i);
//                    return true;
//                }
//            }
//        }

        PrimeDtvServiceInterface primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
        if (primeDtv == null) {
            Log.e(TAG, "deleteScheduledProgram: null primeDtv");
            return false;
        }

        int bookId = (int) scheduledProgramId;
        BookInfo deleteBookInfo = primeDtv.book_info_get(bookId);
        if (deleteBookInfo == null) {
            Log.w(TAG, "deleteScheduledProgram: bookInfo to delete not exist, id = " + bookId);
            // continue deleting for this case
        }

        int result = primeDtv.book_info_delete(bookId);
        if (result == MiscDefine.COMMAND_REPLY_FAIL) {
            Log.e(TAG, "deleteScheduledProgram: book_info_delete failed");
            return false;
        }

        // remove from cache
        cachedScheduledPrograms.removeIf(data -> data.getId() == scheduledProgramId);

        // stop recording
        Intent intent = new Intent(context, RecordingService.class);
        intent.setAction(BookModule.ACTION_STOP_RECORD);
        intent.putExtra(BookInfo.BOOK_ID, scheduledProgramId);
        context.startService(intent);

        // cancel alarm
        if (deleteBookInfo != null) {
            primeDtv.cancel_alarm(deleteBookInfo);
        }

        return true;
    }

    public static synchronized void deleteAllScheduledProgram(Context context) {
//        if (debugScheduledPrograms != null && !debugScheduledPrograms.isEmpty()) {
//            debugScheduledPrograms.clear();
//        }

        PrimeDtvServiceInterface primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
        if (primeDtv == null) {
            Log.e(TAG, "deleteAllScheduledProgram: null primeDtv");
            return;
        }

        List<BookInfo> deleteBookInfoList = primeDtv.book_info_get_list();

        int result = primeDtv.book_info_delete_all();
        if (result == MiscDefine.COMMAND_REPLY_FAIL) {
            Log.e(TAG, "deleteAllScheduledProgram: book_info_delete_all failed");
            return;
        }

        // remove from cache
        cachedScheduledPrograms.clear();

        // stop all recording
        Intent intent = new Intent(context, RecordingService.class);
        intent.setAction(BookModule.ACTION_STOP_ALL_RECORD);
        context.startService(intent);

        // stop all alarm
        for (BookInfo bookInfo : deleteBookInfoList) {
            primeDtv.cancel_alarm(bookInfo);
        }
    }

    public static ScheduledProgramData getScheduledProgram(Context context, long scheduledProgramId) {
//        ScheduledProgramData data = new ScheduledProgramData();
//        if (debugScheduledPrograms != null && !debugScheduledPrograms.isEmpty()) {
//            for (int i = 0; i < debugScheduledPrograms.size(); i++) {
//                if (scheduledProgramId == debugScheduledPrograms.get(i).getId()) {
//                    data = debugScheduledPrograms.get(i);
//                    return data;
//                }
//            }
//        }
//        return null;

        if (!isInit) {
            refreshCacheFromPesi(context);
        }

        for (ScheduledProgramData data : cachedScheduledPrograms) {
            if (data.getId() == scheduledProgramId) {
                return data;
            }
        }

        return null;
    }

    public static List<ScheduledProgramData> getSeriesScheduledPrograms(Context context, String episodeName) {
        List<ScheduledProgramData> list = new ArrayList<>();
//        if (debugScheduledPrograms != null) {
//            for (ScheduledProgramData oldScheduledProgram : debugScheduledPrograms) {
//                if (episodeName.equals(oldScheduledProgram.getEpisodeTitle())) {
//                    list.add(oldScheduledProgram);
//                }
//            }
//        }

        if (!isInit) {
            refreshCacheFromPesi(context);
        }

        for (ScheduledProgramData oldScheduledProgram : cachedScheduledPrograms) {
            if (episodeName.equals(oldScheduledProgram.getEpisodeTitle())) {
                list.add(oldScheduledProgram);
            }
        }

        return list;
    }

    public static List<ScheduledProgramData> getConflictScheduledPrograms(Context context, ScheduledProgramData scheduledProgramData) {
        ScheduledProgramData newScheduledProgram = scheduledProgramData;
        List<ScheduledProgramData> list = new ArrayList<>();

//        if (newScheduledProgram != null && debugScheduledPrograms != null) {
//            for (ScheduledProgramData oldScheduledProgram : debugScheduledPrograms) {
//                boolean isOverlap = newScheduledProgram.getStartTimeUtcMillis() < oldScheduledProgram.getEndTimeUtcMillis() &&
//                        oldScheduledProgram.getStartTimeUtcMillis() < newScheduledProgram.getEndTimeUtcMillis();
//                if (isOverlap) {
//                    list.add(oldScheduledProgram);
//                }
//            }
//        }

        if (!isInit) {
            refreshCacheFromPesi(context);
        }

        if (newScheduledProgram != null) {
            for (ScheduledProgramData oldScheduledProgram : cachedScheduledPrograms) {
                boolean isOverlap = newScheduledProgram.getStartTimeUtcMillis() < oldScheduledProgram.getEndTimeUtcMillis() &&
                        oldScheduledProgram.getStartTimeUtcMillis() < newScheduledProgram.getEndTimeUtcMillis();
                if (isOverlap) {
                    list.add(oldScheduledProgram);
                }
            }
        }

        return list;
    }

    public static List<ScheduledProgramData> getNowRecordingPrograms(Context context) {
        List<ScheduledProgramData> list = new ArrayList<>();
//        if (debugScheduledPrograms != null) {
//            for (ScheduledProgramData scheduledProgramData : debugScheduledPrograms) {
//                boolean isRecording = TimeUtils.isInTimeRange(scheduledProgramData.getStartTimeUtcMillis(), scheduledProgramData.getEndTimeUtcMillis());
//                if (isRecording) {
//                    list.add(scheduledProgramData);
//                }
//            }
//        }

        if (!isInit) {
            refreshCacheFromPesi(context);
        }

        for (ScheduledProgramData data : cachedScheduledPrograms) {
            long statTimeMs = data.getStartTimeUtcMillis();
            long endTimeMs = data.getEndTimeUtcMillis();
            boolean isRecording = TimeUtils.isInTimeRange(statTimeMs, endTimeMs);
            if (isRecording) {
                list.add(data);
            }
        }

        return list;
    }

    public static boolean isNowRecordingProgram(ScheduledProgramData scheduledProgramData) {
        return TimeUtils.isInTimeRange(
                scheduledProgramData.getStartTimeUtcMillis(),
                scheduledProgramData.getEndTimeUtcMillis());
    }

    public static synchronized boolean scheduleNextScheduledProgram(Context context, long scheduledProgramId) {
        Log.d(TAG, "scheduleNextScheduledProgram: id = " + scheduledProgramId);
        PrimeDtvServiceInterface primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
        if (primeDtv == null) {
            Log.e(TAG, "scheduleNextScheduledProgram: null primeDtv");
            return false;
        }

        int bookId = (int) scheduledProgramId;
        BookInfo bookInfo = primeDtv.book_info_get(bookId);
        if (bookInfo == null) {
            Log.w(TAG, "scheduleNextScheduledProgram: book not found, id = " + bookId);
            return false;
        }

        int result = primeDtv.schedule_next_timer(bookInfo);
        if (result == MiscDefine.COMMAND_REPLY_FAIL) {
            Log.e(TAG, "scheduleNextScheduledProgram: schedule_next_timer failed");
            return false;
        }

        BookInfo updatedBookInfo = primeDtv.book_info_get(bookId);
        if (updatedBookInfo == null) {
            Log.i(TAG, "scheduleNextScheduledProgram: updated book not found, delete from cache");
            // book may be deleted after schedule_next_timer, delete from cache
            cachedScheduledPrograms.removeIf(data -> data.getId() == scheduledProgramId);
            return true;
        }

        ScheduledProgramData newData = fromPesiBookInfo(context, updatedBookInfo, null);
        if (newData != null) {
            // remove old and add new
            cachedScheduledPrograms.removeIf(data -> data.getId() == scheduledProgramId);
            cachedScheduledPrograms.add(newData);
            Log.d(TAG, "scheduleNextScheduledProgram: Cache updated for next schedule");
            return true;
        }

        return false;
    }

    private static synchronized void refreshCacheFromPesi(Context context) {
        if (isInit) return;

        List<ScheduledProgramData> latestData = getScheduledProgramsFromPesi(context);

        cachedScheduledPrograms.clear();
        cachedScheduledPrograms.addAll(latestData);

        isInit = true;
    }

    private static List<ScheduledProgramData> getScheduledProgramsFromPesi(Context context) {
        List<ScheduledProgramData> scheduledProgramDataList = new ArrayList<>();
        PrimeDtvServiceInterface primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
        if (primeDtv != null) {
            List<BookInfo> bookInfoList = primeDtv.book_info_get_list();
            Map<Long, Channel> channelCache = new HashMap<>();
            for (BookInfo bookInfo : bookInfoList) {
                ScheduledProgramData data = fromPesiBookInfo(context, bookInfo, channelCache);
                if (data != null) {
                    scheduledProgramDataList.add(data);
                }
            }

            channelCache.clear();
        }

        return scheduledProgramDataList;
    }

    private static ScheduledProgramData fromPesiBookInfo(
            Context context, BookInfo bookInfo, Map<Long, Channel> channelCache) {
        if (bookInfo == null) {
            return null;
        }

        PrimeDtvServiceInterface primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
        if (primeDtv == null) {
            Log.e(TAG, "fromPesiBookInfo: null primeDtv");
            return null;
        }

        long pesiChannelId = bookInfo.getChannelId();
        Channel tifChannel = null;
        if (channelCache != null && channelCache.containsKey(pesiChannelId)) {
            tifChannel = channelCache.get(pesiChannelId);
        }

        if (tifChannel == null) {
            ProgramInfo pesiProgramInfo = primeDtv.get_program_by_channel_id(pesiChannelId);
            if (pesiProgramInfo != null && pesiProgramInfo.getTvInputChannelUri() != null) {
                tifChannel = ChannelUtils.getChannelFullData(
                        context, pesiProgramInfo.getTvInputChannelUri());

                // save to channel cache for next time
                if (tifChannel != null && channelCache != null) {
                    channelCache.put(pesiChannelId, tifChannel);
                }
            }
        }

        if (tifChannel == null) {
            Log.e(TAG, "fromPesiBookInfo: unable to get tif channel");
            return null;
        }

        ScheduledProgramData data;
        long tifChannelId = tifChannel.getId();
        int eventId = bookInfo.getEpgEventId();
        long bookStartMs = bookInfo.get_start_time_stamp();
        long bookEndMs = bookInfo.get_end_time_stamp();

        if (eventId >= 0) {
            // Program-based
            Program tifProgram = ProgramUtils.getProgramByEventId(context, tifChannelId, eventId);

            // try to get by time if can not find by eventId
            if (tifProgram == null) {
                tifProgram = ProgramUtils.getProgramForDate(context, tifChannelId, bookStartMs);
            }

            if (tifProgram != null) {
                data = new ScheduledProgramData(tifProgram, tifChannel);
                if (bookInfo.isSeries()) {
                    data.setCycle(BookInfo.BOOK_CYCLE_SERIES);
                    //TODO: check episode title, episode number, and series id exist?
                } else {
                    data.setCycle(BookInfo.BOOK_CYCLE_ONETIME);
                    // remove series related info for single record
                    data.setEpisodeTitle(null);
                    data.setEpisodeDisplayNumber(null);
                    data.setSeriesId(null);
                }
            } else {
                Log.e(TAG, "fromPesiBookInfo: Program-based recording but no program found");
                return null;
            }
        } else {
            // Time-based / Manual

            // try to get rating by program at that time
            Program tifProgram = ProgramUtils.getProgramForDate(context, tifChannelId, bookStartMs);
            String rating = (tifProgram != null)
                    ? TvContractUtils.contentRatingsToString(tifProgram.getContentRatings())
                    : TvContentRating.UNRATED.flattenToString();
            int cycle = bookInfo.getBookCycle();
            int weekMask = bookInfo.getWeek();

            data = new ScheduledProgramData(tifChannel, rating, bookStartMs, bookEndMs, cycle, weekMask);
        }

        data.setId(bookInfo.getBookId()); // ensure id match
        return data;
    }

    private static BookInfo toPesiBookInfo(Context context, ScheduledProgramData data) {
        if (data == null) {
            return null;
        }

        PrimeDtvServiceInterface primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
        if (primeDtv == null) {
            Log.e(TAG, "toPesiBookInfo: null primeDtv");
            return null;
        }

        // try to get pesi ProgramInfo by SId, ONId, TSId for pesi channel id
        Uri channelUri = TvContractCompat.buildChannelUri(data.getChannelId());
        Channel tifChannel = ChannelUtils.getChannelFullData(context, channelUri);
        if (tifChannel == null) {
            Log.e(TAG, "toPesiBookInfo: unable to get tif channel");
            return null;
        }

        ProgramInfo pesiProgramInfo = primeDtv.get_program_by_SId_OnId_TsId(
                tifChannel.getServiceId(),
                tifChannel.getOriginalNetworkId(),
                tifChannel.getTransportStreamId());

        if (pesiProgramInfo == null) {
            Log.e(TAG, "toPesiBookInfo: pesi program info not found, " +
                    "sid = " + tifChannel.getServiceId() +
                    " onid = " + tifChannel.getOriginalNetworkId() +
                    " tsid = " + tifChannel.getTransportStreamId());
            return null;
        }

        // basic data
        BookInfo bookInfo = new BookInfo();
        bookInfo.setBookId((int) data.getId()); // make sure ScheduledProgramData.id is in int range
        bookInfo.setChannelId(pesiProgramInfo.getChannelId()); // pesi channel id
        bookInfo.setChannelNum(data.getChannelNumber());
        bookInfo.setEventName(data.getTitle());
        bookInfo.setBookType(BookInfo.BOOK_TYPE_RECORD);

        // cycle, weekMask, series
        int cycle = data.getCycle();
        boolean isSeries = cycle == BookInfo.BOOK_CYCLE_SERIES;
        bookInfo.setBookCycle(cycle);
        bookInfo.setWeek(data.getWeekMask());
        bookInfo.setSeries(isSeries);
        bookInfo.setEpgEventId(data.getEventId());

        if (isSeries) {
            // episdoe title
            String episodeTitle = data.getEpisodeTitle();
            bookInfo.setEventName(episodeTitle);

            // episode key
            String episodeStr = data.getEpisodeDisplayNumber();
            bookInfo.setEpisode(!TextUtils.isEmpty(episodeStr) && TextUtils.isDigitsOnly(episodeStr)
                    ? Integer.parseInt(episodeStr) : 0);

            // series rec key
            if (data.getSeriesId() != null) {
                bookInfo.setSeriesRecKey(data.getSeriesId().getBytes(StandardCharsets.UTF_8));
                Log.d(TAG, "toPesiBookInfo: seriesid = " + data.getSeriesId());
//                Log.d(TAG, "toPesiBookInfo: seriesRecKey = " + Arrays.toString(bookInfo.getSeriesRecKey()));
            }
        }

        // year, month, date
        long startMs = data.getStartTimeUtcMillis();
        long endMs = data.getEndTimeUtcMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startMs);

        bookInfo.setYear(calendar.get(Calendar.YEAR));
        bookInfo.setMonth(calendar.get(Calendar.MONTH) + 1);
        bookInfo.setDate(calendar.get(Calendar.DATE));

        // HHmm (14:30 = 1430)
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        bookInfo.setStartTime(hour * 100 + minute);

        // startTimeMs, durationMs
        bookInfo.setStartTimeMs(startMs);
        long durationTotalMs = endMs - startMs;
        bookInfo.setDurationMs((int) durationTotalMs);

        // duration HHmm
        int durationSecs = (int) (durationTotalMs / 1000L);
        int dHour = durationSecs / 3600;
        int dMinute = (durationSecs % 3600) / 60;
        bookInfo.setDuration(dHour * 100 + dMinute);

        return bookInfo;
    }
}