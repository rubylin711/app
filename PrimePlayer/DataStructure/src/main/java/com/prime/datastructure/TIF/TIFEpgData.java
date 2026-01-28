package com.prime.datastructure.TIF;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.media.tv.TvContract.Programs;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class TIFEpgData {
    public static final String TAG = "TIFEpgData";


    /*************
     * 舊的 insertEpgData / testInsertEpgData
     * 這兩個我只加一點 log，邏輯不動太多
     *************/
    public void insertEpgData(Context context, long channelId, List<EPGEvent> epgEventList) {
        if (context == null) {
            Log.e(TAG, "insertEpgData: context is null, channelId=" + channelId);
            return;
        }
        if (epgEventList == null || epgEventList.isEmpty()) {
            Log.d(TAG, "insertEpgData: no events, channelId=" + channelId);
            return;
        }

        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            int epgEventCount = epgEventList.size();
            Log.d(TAG, "insertEpgData: channelId=" + channelId + " events=" + epgEventCount);

            for (int i = 0; i < epgEventCount; i++) {
                EPGEvent event = epgEventList.get(i);
                ContentValues programValues = new ContentValues();
                String ownerPackage = "com.prime.tvinputframework";
                programValues.put(TvContract.Channels.COLUMN_PACKAGE_NAME, ownerPackage);
                programValues.put(Programs.COLUMN_CHANNEL_ID, channelId);
                programValues.put(Programs.COLUMN_TITLE, event.get_event_name());
                programValues.put(Programs.COLUMN_SHORT_DESCRIPTION, event.get_short_event());
                programValues.put(Programs.COLUMN_START_TIME_UTC_MILLIS, event.get_start_time());
                programValues.put(Programs.COLUMN_END_TIME_UTC_MILLIS, event.get_end_time());
                programValues.put(Programs.COLUMN_CONTENT_RATING, "TV-Y");

                ops.add(ContentProviderOperation.newInsert(Programs.CONTENT_URI)
                        .withValues(programValues)
                        .build());

                if (i % 100 == 99 || i == epgEventCount - 1) {
                    context.getContentResolver().applyBatch(TvContract.AUTHORITY, ops);
                    Log.d(TAG, "insertEpgData: applied batch, count=" + ops.size());
                    ops.clear();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert channel id["+channelId+"] epg events", e);
        }
    }

    public static void testInsertEpgData(Context context, long channelId) {
        if (context == null) {
            Log.e(TAG, "testInsertEpgData: context is null");
            return;
        }
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            Date date = new Date();
            ContentValues programValues = new ContentValues();
            programValues.put(Programs.COLUMN_CHANNEL_ID, channelId);
            programValues.put(Programs.COLUMN_TITLE, "test");
            programValues.put(Programs.COLUMN_SHORT_DESCRIPTION, "test short event");
            programValues.put(Programs.COLUMN_START_TIME_UTC_MILLIS, date.getTime());
            programValues.put(Programs.COLUMN_END_TIME_UTC_MILLIS, date.getTime()+60*5*1000);
            programValues.put(Programs.COLUMN_CONTENT_RATING, "TV-Y");

            ops.add(ContentProviderOperation.newInsert(Programs.CONTENT_URI)
                    .withValues(programValues)
                    .build());

            context.getContentResolver().applyBatch(TvContract.AUTHORITY, ops);
            Log.d(TAG, "testInsertEpgData done, channelId=" + channelId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert channel id["+channelId+"] epg events", e);
        }
    }

    /*************
     * 工具：時間 / 時區
     *************/
    public static Date get_date(long timeMs) {
        // 原本 "0000-00-00 HH:mm:ss" 是不合法 pattern，會一直 ParseException
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            return formatter.parse(formatter.format(new Date(timeMs)));
        } catch (ParseException e) {
            Log.e(TAG, "get_date: parse error, timeMs=" + timeMs, e);
            return null;
        }
    }

    public static long getTimezoneOffsetInMillis() {
        TimeZone tz = TimeZone.getDefault();
        long offset = tz.getOffset(System.currentTimeMillis());
        if (Pvcfg.getEpgTimezoneOffset() == 0) {
            offset = -offset;
        }
        return offset;
    }

    /*************
     * Debug：檢查 event_id + channel_id 是否重複
     *************/
    public static void test_same_event(Context context) {
        if (context == null) {
            Log.e(TAG, "test_same_event: context is null");
            return;
        }

        String[] projection = {Programs.COLUMN_EVENT_ID, Programs.COLUMN_CHANNEL_ID};

		// 用來儲存複合鍵（"event_id|channel_id"）及其出現次數
        Map<String, Integer> compositeCounts = new HashMap<>();

        try (Cursor cursor = context.getContentResolver().query(
                Programs.CONTENT_URI,
                projection,
                null,
                null,
                null
        )) {

            if (cursor != null) {
                int eventIdIndex = cursor.getColumnIndex(Programs.COLUMN_EVENT_ID);
                int channelIdIndex = cursor.getColumnIndex(Programs.COLUMN_CHANNEL_ID);

                while (cursor.moveToNext()) {
                    long currentEventId = cursor.getLong(eventIdIndex);
                    long currentChannelId = cursor.getLong(channelIdIndex);

                    // 2. 建立複合鍵：將兩個 ID 用分隔符號連接
                    String compositeKey = currentEventId + "|" + currentChannelId;

                    // 3. 統計次數
                    compositeCounts.put(
                            compositeKey,
                            compositeCounts.getOrDefault(compositeKey, 0) + 1
                    );
                }
            }
        }

// 4. 迭代結果，找出重複的組合
        for (Map.Entry<String, Integer> entry : compositeCounts.entrySet()) {
            String compositeKey = entry.getKey();
            int count = entry.getValue();

            if (count > 1) {
                // 解析複合鍵，分離出 event_id 和 channel_id
                String[] parts = compositeKey.split("\\|");
                String eventId = parts[0];
                String channelId = parts[1];

                Log.d(TAG,"DuplicateCheck Event ID: " + eventId +
                                ", Channel ID: " + channelId +
                        " -> 組合重複次數: " + count);
            }
        }
    }

    /*************
     * 這是主要你在用的：從 ProgramInfo + EPGEvent list 寫進 TvContract.Programs
     * ★ 已加完整防呆 & 詳細 log
     *************/
    public static void insertProgramData(Context context, ProgramInfo programInfo, List<EPGEvent> events) {
		if (context == null) {
            Log.e(TAG, "insertProgramData: context is null, skip");
            return;
        }
        if (programInfo == null) {
            Log.e(TAG, "insertProgramData: programInfo is null, skip");
            return;
        }
        if (events == null || events.isEmpty()) {
            Log.d(TAG, "insertProgramData: no events, chId=" + programInfo.getChannelId()
                    + " name=" + programInfo.getDisplayName());
            return;
        }        

        // ★★★ 這裡改成「只信 TIF channel Uri」★★★
        Uri tvInputChannelUri = programInfo.getTvInputChannelUri();
        if (tvInputChannelUri == null) {
            Log.e(TAG, "insertProgramData: tvInputChannelUri is null, name=" 
                + programInfo.getDisplayName());
            return;
        }

        // 如果你的 Uri 一定是 content://android.media.tv/channel/<id>
        // 可以直接 parse：
        long channel_id;
        try {
            channel_id = ContentUris.parseId(tvInputChannelUri);
        } catch (NumberFormatException e) {
            // 若 Uri 不標準，就走舊的 TIFChannelData 流程
            Log.w(TAG, "insertProgramData: parseId failed, fallback to getChannelIdFromUriWithQuery. uri=" 
                    + tvInputChannelUri, e);
                    try {
                channel_id = TIFChannelData.getChannelIdFromUriWithQuery(context, tvInputChannelUri);
            } catch (IllegalArgumentException ex) {
                Log.e(TAG, "insertProgramData: getChannelIdFromUriWithQuery failed. uri=" 
                        + tvInputChannelUri, ex);
                return;
            }
        }

        Log.d(TAG, "insertProgramData: START, TIF chId=" + channel_id +
            " displayName=" + programInfo.getDisplayName() +
                " events=" + events.size());

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        for (EPGEvent epgEvent : events) {
            if (epgEvent == null) continue;

            long start_time = epgEvent.get_start_time();
            long end_time = epgEvent.get_end_time();

            ContentValues programValues = new ContentValues();
            String ownerPackage = "com.prime.tvinputframework";
            programValues.put(TvContract.Channels.COLUMN_PACKAGE_NAME, ownerPackage);
            programValues.put(Programs.COLUMN_CHANNEL_ID, channel_id);
            programValues.put(Programs.COLUMN_EVENT_ID, epgEvent.get_event_id());
            programValues.put(Programs.COLUMN_TITLE, epgEvent.get_event_name());
            programValues.put(Programs.COLUMN_SHORT_DESCRIPTION, epgEvent.get_short_event());
            programValues.put(Programs.COLUMN_LONG_DESCRIPTION, epgEvent.get_extended_event());
            programValues.put(Programs.COLUMN_START_TIME_UTC_MILLIS, start_time);
            programValues.put(Programs.COLUMN_END_TIME_UTC_MILLIS, end_time);

            //series
            if (epgEvent.is_series()) {
                String[] episodeTitleStringArray = epgEvent.get_event_name().split(":");
                String episodeTitle = episodeTitleStringArray[0];

                programValues.put(Programs.COLUMN_EPISODE_TITLE, episodeTitle);
                programValues.put(Programs.COLUMN_EPISODE_DISPLAY_NUMBER, epgEvent.get_episode_key());

                String seriesId = new String(epgEvent.get_series_key(), StandardCharsets.UTF_8);
                programValues.put(Programs.COLUMN_SERIES_ID, seriesId);
            }
            

            //programValues.put(Programs.COLUMN_CONTENT_RATING, rating.flattenToString()); //chuck tmp remove
            // 1. 設成 framework 標準 domain
            String domain = "com.android.tv";   // 建議用這個，跟 AOSP 一致
            String ratingSystem = "DVB";
            int parentalRate = epgEvent.get_parental_rate();  // 例如 6
            String ratingValue = "DVB_" + parentalRate;

            // 4. 建立 TvContentRating
            TvContentRating rating = TvContentRating.createRating(
                    domain,
                    ratingSystem,
                    ratingValue
            );

            // 5. 存進 DB 時一定要用 flattenToString()
            programValues.put(Programs.COLUMN_CONTENT_RATING, rating.flattenToString());

            //programValues.put(Programs.COLUMN_CONTENT_RATING, epgEvent.get_parental_rate());
            programValues.put(Programs.COLUMN_INTERNAL_PROVIDER_ID, Pvcfg.getTvInputId());
            programValues.put(Programs.COLUMN_SEARCHABLE,1);

            Uri existUri = getProgramUri(context, channel_id, epgEvent.get_event_id());

            String str = "yyy-MM-dd HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(str);
            String starttime = sdf.format(new Date(start_time));
            String endtime = sdf.format(new Date(end_time));

            if (existUri != null) {
                // update
                String selection = Programs.COLUMN_EVENT_ID + " = ? AND " +
                        Programs.COLUMN_CHANNEL_ID + " = ?";
                String[] selectionArgs = {
                        String.valueOf(epgEvent.get_event_id()),
                        String.valueOf(channel_id)
                };
                    ops.add(ContentProviderOperation.newUpdate(Programs.CONTENT_URI)
                            .withSelection(selection, selectionArgs)
                            .withValues(programValues)
                            .build());

                Log.d(TAG, "insertProgramData: " +
                        programInfo.getDisplayName() +
                        " UPDATE event. chId=" + channel_id +
                        " eventId=" + epgEvent.get_event_id() +
                        " title=" + epgEvent.get_event_name() +
                        " start time = "+ starttime +
                        " end time = " + endtime);
            } else {
                // insert
                    ops.add(ContentProviderOperation.newInsert(Programs.CONTENT_URI)
                            .withValues(programValues)
                            .build());

                Log.d(TAG, "insertProgramData: " +
                        programInfo.getDisplayName() +
                        " INSERT event. chId=" + channel_id +
                        " eventId=" + epgEvent.get_event_id() +
                        " title=" + epgEvent.get_event_name() +
                        " start time = "+ starttime +
                        " end time = " + endtime);
            }
        }

        if (ops.isEmpty()) {
            Log.d(TAG, "insertProgramData: no ops to apply, chId=" + channel_id);
            return;
        }

        try {
            context.getContentResolver().applyBatch(TvContract.AUTHORITY, ops);
            Log.d(TAG, "insertProgramData: applyBatch done. chId=" + channel_id +
                    " ops=" + ops.size());
        } catch (OperationApplicationException | RemoteException e) {
            Log.e(TAG, "insertProgramData: applyBatch failed. chId=" + channel_id, e);
        }

        // Debug 檢查 event 重複，量很大時你可以考慮關掉這行
        test_same_event(context);

        Log.d(TAG, "insertProgramData: END, chId=" + channel_id +
                " name=" + programInfo.getDisplayName());
    }

    /*************
     * 測試用：手動 insert 一筆，再 getProgramUri()
     *************/
    public static void test_insert(Context context) {
        if (context == null) {
            Log.e(TAG, "test_insert: context is null");
            return;
        }

        ContentValues programValues = new ContentValues();
        programValues.put(Programs.COLUMN_CHANNEL_ID, 333L);
        programValues.put(Programs.COLUMN_EVENT_ID, 1111);
        programValues.put(Programs.COLUMN_TITLE, "測試中");
        programValues.put(Programs.COLUMN_INTERNAL_PROVIDER_ID, Pvcfg.getTvInputId());

        Uri programUri = ContentUris.withAppendedId(TvContract.Channels.CONTENT_URI, 1L);
        String channel_id = "channel_id = "+TIFChannelData.getChannelIdFromUri(context,programUri);
        Log.d(TAG, "test_insert: " + channel_id);

        context.getContentResolver().insert(TvContract.Programs.CONTENT_URI,programValues);
        getProgramUri(context,333L,1111);
    }

    /*************
     * 查是否已存在某個 (channelId, eventId) 的節目
     *************/
    public static Uri getProgramUri(Context context, long channelId, int eventId) {
        if (context == null) {
            Log.e(TAG, "getProgramUri: context is null");
            return null;
        }

        String[] projection = {
                Programs._ID,
                Programs.COLUMN_CHANNEL_ID,
                Programs.COLUMN_EVENT_ID,
                Programs.COLUMN_TITLE
        };
        String selection = Programs.COLUMN_EVENT_ID + " = ? AND " +
                Programs.COLUMN_CHANNEL_ID + " = ?";
        String[] selectionArgs = {
                String.valueOf(eventId),
                String.valueOf(channelId)
        };

        try (Cursor cursor = context.getContentResolver().query(
                Programs.CONTENT_URI, projection, selection, selectionArgs, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex(TvContract.Programs._ID);
                long id = cursor.getLong(idColumnIndex);
//                int channelIdColumnIndex = cursor.getColumnIndex(TvContract.Programs.COLUMN_CHANNEL_ID);
//                long channel_id = cursor.getLong(channelIdColumnIndex);
//                int eventIDColumnIndex = cursor.getColumnIndex(TvContract.Programs.COLUMN_EVENT_ID);
//                int event_id = cursor.getInt(eventIDColumnIndex);
//                int titleColumnIndex = cursor.getColumnIndex(TvContract.Programs.COLUMN_TITLE);
//                String title = cursor.getString(titleColumnIndex);
//                Log.d(TAG, "getProgramUri: found _id=" + id +
//                        " channel_id=" + channel_id +
//                        " event_id=" + event_id +
//                        " title=" + title);

                return ContentUris.withAppendedId(Programs.CONTENT_URI, id);
            } else {
                Log.d(TAG, "getProgramUri: not found. event_id=" + eventId +
                        " channel_id=" + channelId);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "getProgramUri: query error", e);
            return null;
        }
    }

    public static Uri getProgramUri(Context context, long channelId, long timeMillis) {
        if (context == null) {
            Log.e(TAG, "getProgramUri: context is null");
            return null;
        }

        String[] projection = {
                Programs._ID,
        };
        String selection = Programs.COLUMN_CHANNEL_ID + " = ? AND " +
                Programs.COLUMN_START_TIME_UTC_MILLIS + " <= ? AND " +
                Programs.COLUMN_END_TIME_UTC_MILLIS + " >= ?";
        String[] selectionArgs = {
                String.valueOf(channelId),
                String.valueOf(timeMillis),
                String.valueOf(timeMillis)
        };
        String sortOrder = Programs.COLUMN_START_TIME_UTC_MILLIS + " ASC";

        try (Cursor cursor = context.getContentResolver().query(
                Programs.CONTENT_URI, projection, selection, selectionArgs, sortOrder)) {

            if (cursor != null && cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex(TvContract.Programs._ID);
                long id = cursor.getLong(idColumnIndex);

                return ContentUris.withAppendedId(Programs.CONTENT_URI, id);
            } else {
                Log.d(TAG, "getProgramUri: not found. timeMillis = " + timeMillis +
                        " channel_id = " + channelId);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "getProgramUri: query error", e);
            return null;
        }
    }

    public static ContentValues getValuesFromUri(Context context, Uri programUri, String[] projection) {
        if (context == null || programUri == null) {
            Log.e(TAG, "getValuesFromUri: context or programUri is null");
            return new ContentValues();
        }

        ContentValues values = new ContentValues();
        ContentResolver contentResolver = context.getContentResolver();

        try (Cursor cursor = contentResolver.query(
                programUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String[] columns = (projection != null && projection.length > 0)
                        ? projection
                        : cursor.getColumnNames();

                for (String columnName : columns) {
                    int index = cursor.getColumnIndex(columnName);
                    if (index == -1) {
                        Log.w(TAG, "getValuesFromUri: columnName = " + columnName + " not exist");
                        continue;
                    }

                    switch (cursor.getType(index)) {
                        case Cursor.FIELD_TYPE_INTEGER:
                            values.put(columnName, cursor.getLong(index));
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            values.put(columnName, cursor.getDouble(index));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            values.put(columnName, cursor.getString(index));
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            values.put(columnName, cursor.getBlob(index));
                            break;
                        case Cursor.FIELD_TYPE_NULL:
                        default:
                            values.putNull(columnName);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getValuesFromUri: failed, uri = " + programUri, e);
        }
        return values;
    }

    public static String getStringFromUri(Context context, Uri programUri, String columnName) {
        return getStringFromUri(context, programUri, columnName, "");
    }

    public static String getStringFromUri(Context context, Uri programUri, String columnName, String defaultValue) {
        if (context == null || programUri == null) {
            Log.e(TAG, "getStringFromUri: null Context or ProgramUri.");
            return defaultValue;
        }

        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = { columnName };
        try (Cursor cursor = contentResolver.query(
                programUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(columnName);
                if (index >= 0 && !cursor.isNull(index)) {
                    return cursor.getString(index);
                }
            }
        }

        return defaultValue;
    }

    public static int getIntFromUri(Context context, Uri programUri, String columnName) {
        return getIntFromUri(context, programUri, columnName, 0);
    }

    public static int getIntFromUri(Context context, Uri programUri, String columnName, int defaultValue) {
        if (context == null || programUri == null) {
            Log.e(TAG, "getIntFromUri: null Context or ProgramUri.");
            return defaultValue;
        }

        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = { columnName };
        try (Cursor cursor = contentResolver.query(
                programUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(columnName);
                if (index >= 0 && !cursor.isNull(index)) {
                    return cursor.getInt(index);
                }
            }
        }

        return defaultValue;
    }

    public static long getLongFromUri(Context context, Uri programUri, String columnName) {
        return getLongFromUri(context, programUri, columnName, 0L);
    }

    public static long getLongFromUri(Context context, Uri programUri, String columnName, long defaultValue) {
        if (context == null || programUri == null) {
            Log.e(TAG, "getLongFromUri: null Context or ProgramUri.");
            return defaultValue;
        }

        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = { columnName };
        try (Cursor cursor = contentResolver.query(
                programUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(columnName);
                if (index >= 0 && !cursor.isNull(index)) {
                    return cursor.getLong(index);
                }
            }
        }

        return defaultValue;
    }
}
