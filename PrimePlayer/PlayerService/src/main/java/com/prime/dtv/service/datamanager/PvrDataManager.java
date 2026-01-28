package com.prime.dtv.service.datamanager;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.prime.dtv.database.DBUtils;
import com.prime.dtv.service.database.PvrContentProvider;
import com.prime.dtv.service.database.PvrDatabase;
import com.prime.dtv.service.database.dvbdatabasetable.PvrInfoDatabaseTable;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.PvrDbRecordInfo;
import com.prime.datastructure.sysdata.PvrRecFileInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PvrDataManager {

    private static final String TAG = "PvrDataManager" ;
    public static final int FILE_SORT_SMALL_TO_LARGE_METHOD = 0;
    public static final int FILE_SORT_LARGE_TO_SMALL_METHOD = 1;

    public static final int FILE_SORT_BY_NAME_TYPE = 0;
    public static final int FILE_SORT_BY_CHNAME_TYPE = 1;
    public static final int FILE_SORT_BY_LCN_TYPE = 2;//channel_id
    public static final int FILE_SORT_BY_START_TIME_TYPE = 3;
    public static final int FILE_SORT_BY_SERIES_TYPE = 4;
    public static final int FILE_SORT_BY_PLAYTIME_TYPE = 5;
    private int mSortMethod = FILE_SORT_SMALL_TO_LARGE_METHOD;
    private int mSortType = FILE_SORT_BY_LCN_TYPE;
    private Context mContext;
    private ContentResolver resolver;
    DBUtils mDbUtils = new DBUtils();
    private String usbPath = PvrDatabase.USB_PATH;
    public PvrDataManager(Context context) {
        mContext=context;
        resolver = mContext.getContentResolver();
    }
    public int getNewMasterIdx(){
        int primaryId;
        primaryId=getMaxPrimaryId()+1;
        return primaryId;
        //return getMasterTotalRecordCount();//Idx from 0
    }

    public int getNewSeriesIdx(int masterIdx){
        int seriesPrimaryId;
        seriesPrimaryId=getSeriesMaxPrimaryId(masterIdx)+1;
        return seriesPrimaryId;
        //return getSeriesRecordTotalCount(masterIdx);//Idx from 0
    }

    public int getMaxPrimaryId() {
        int maxPrimaryId = -1;
        Cursor cursor = null;
        try {
            Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
            String[] projection = {PvrInfoDatabaseTable.PRIMARY_ID};

            // 關鍵：依據 PRIMARY_ID 降序排列，並只取第一筆 (最大的 PRIMARY_ID)
            String sortOrder = PvrInfoDatabaseTable.PRIMARY_ID + " DESC LIMIT 1";
            cursor = resolver.query(uri, projection, null, null, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                maxPrimaryId = mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.PRIMARY_ID);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting max PRIMARY_ID", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return maxPrimaryId;
    }

    public int getMasterIdxBasedOnSeriesKey(byte[] seriesKey){
        int masterIdx = -1;
        Cursor cursor = null;
        try {
            Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
            String[] projection
                    = { PvrInfoDatabaseTable.PRIMARY_ID, PvrInfoDatabaseTable.SERIES_KEY };
            String selection = PvrInfoDatabaseTable.SERIES + " = ?";
            String[] selectionArgs = new String[]{ String.valueOf(1) };

            cursor = resolver.query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    byte[] dbSeriesKey
                            = mDbUtils.GetByteArrayFromTable(cursor,PvrInfoDatabaseTable.SERIES_KEY);
                    if (Arrays.equals(seriesKey, dbSeriesKey)) {
                        masterIdx
                                = mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.PRIMARY_ID);
                        break;
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error finding Master Index", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return masterIdx;
    }

    public PvrRecIdx getAvailableIndex(int isSeries, byte[] seriesKey){
        PvrRecIdx pvrRecIdx = new PvrRecIdx(-1,-1);
        int masterIdx = -1;
        int seriesIdx = -1;
        if (isSeries == 1) {

            masterIdx = getMasterIdxBasedOnSeriesKey(seriesKey);
            if (masterIdx != -1) {

                seriesIdx = getNewSeriesIdx(masterIdx);
            } else {

                masterIdx = getNewMasterIdx();
                seriesIdx = 0;
            }
        } else {

            masterIdx = getNewMasterIdx();
            seriesIdx = 0xFFFF;
        }
        pvrRecIdx.setMasterIdx(masterIdx);
        pvrRecIdx.setSeriesIdx(seriesIdx);
        return pvrRecIdx;
    }
    public String preCreateDirectories(int isSeries, PvrRecIdx pvrRecIdx){
        int masterIdx = pvrRecIdx.getMasterIdx();
        int seriesIdx = pvrRecIdx.getSeriesIdx();
        String dirPath;
        String fullNamePath;
        String fullPath;

        if (isSeries == 1) {
            dirPath = "/"+PvrDatabase.RECORD_FOLDER_NAME+"_" + masterIdx + "_" + seriesIdx;
            fullNamePath = dirPath + "/"+PvrDatabase.RECORD_FOLDER_NAME+"_" + masterIdx + "_" + seriesIdx + ".ts";
        } else {
            dirPath = "/"+PvrDatabase.RECORD_FOLDER_NAME+"_" + masterIdx;
            fullNamePath = dirPath + "/"+PvrDatabase.RECORD_FOLDER_NAME+"_" + masterIdx + ".ts";
        }

        //File dbFile = new File(usbPath, PvrInfoDatabaseTable.DATABASE_NAME);
        //if (!dbFile.exists()) {
        //    PvrDatabase pvrDatabase = new PvrDatabase(mContext, usbPath);
        //   pvrDatabase.openOrCreateDatabase(PvrInfoDatabaseTable.DATABASE_NAME, Context.MODE_PRIVATE, null);
        //} else {
        //    deleteAllRecordFiles();
        //}

        String filePath = usbPath + dirPath;
        File fileDir = new File(filePath);

        if (!fileDir.exists()) {//check if the folder exists
            boolean isCreated = fileDir.mkdirs();
            if (!isCreated) {
                Log.e(TAG, "Failed to create directory: " + filePath);
            }
        }
        else //fileDir already exists,the files inside need to be cleared.
        {
            deleteFileOrDirectory(filePath);
        }
        fullPath=usbPath+fullNamePath;
        return fullPath;
    }
    private void updatePlaybackTable(String columnName, long newValue, int masterIdx, int seriesIdx) {
        ContentValues values = new ContentValues();
        values.put(columnName, newValue);

        String where = PvrInfoDatabaseTable.MASTER_ID + " = ? AND " + PvrInfoDatabaseTable.SERIES_ID + " = ?";
        String[] whereArgs = {String.valueOf(masterIdx), String.valueOf(seriesIdx)};

        try {
            int rowsUpdated = resolver.update(PvrContentProvider.get_CONTENT_PLAYBACK_URI(), values, where, whereArgs);
            if (rowsUpdated > 0) {
                // Log.d(TAG, "Successfully updated " + columnName + " for masterId " + masterId);
            } else {
                // Log.d(TAG, "Failed to update " + columnName + " for masterId " + masterId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void updateMasterRecord(String columnName, int newValue, int masterIdx) {
        ContentValues values = new ContentValues();
        values.put(columnName, newValue);

        String where = PvrInfoDatabaseTable.PRIMARY_ID + " = ?";
        String[] whereArgs = { String.valueOf(masterIdx) };

        try {
            int rowsUpdated = resolver.update(PvrContentProvider.get_CONTENT_MASTER_URI(), values, where, whereArgs);
            if (rowsUpdated > 0) {
                // Log.d(TAG, "Successfully updated " + columnName + " for masterId " + masterId);
            } else {
                // Log.d(TAG, "Failed to update " + columnName + " for masterId " + masterId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Uri insertPlaybackTable(PvrRecFileInfo pvrRecFileInfo) {
        int playbackCount = getPlaybackTotalRecordCount();
        if(playbackCount>=30) {//playback list more than 30, the oldest one will be deleted
            Cursor cursor = resolver.query(
                    PvrContentProvider.get_CONTENT_PLAYBACK_URI(),
                    new String[]{PvrInfoDatabaseTable.MASTER_ID, PvrInfoDatabaseTable.SERIES_ID},
                    null, null,
                    PvrInfoDatabaseTable.PLAYBACK_TIME + " ASC LIMIT 1"
            );

            if (cursor != null && cursor.moveToFirst()) {
                int masterIdToDel = mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.MASTER_ID);
                int seriesIdToDel = mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_ID);
                cursor.close();
                deleteOnePlaybackInList(new PvrRecIdx(masterIdToDel, seriesIdToDel));
            }
        }
        Gson gson = new Gson();
        ContentValues values = new ContentValues();
        values.put(PvrInfoDatabaseTable.MASTER_ID, pvrRecFileInfo.getMasterIdx());
        values.put(PvrInfoDatabaseTable.SERIES_ID, pvrRecFileInfo.getSeriesIdx());
        values.put(PvrInfoDatabaseTable.IS_SERIES, pvrRecFileInfo.isSeries() ? 1 : 0);//convert bool into int and and store it in SQLite
        values.put(PvrInfoDatabaseTable.PLAYBACK_TIME, System.currentTimeMillis());

        return resolver.insert(PvrContentProvider.get_CONTENT_PLAYBACK_URI(), values);
    }
    private Uri insertMasterRecord(PvrDbRecordInfo dbRecordInfo) {

        Gson gson = new Gson();
        ContentValues values = new ContentValues();
        int ChLock=0,Adult=0,tmp=0;

        values.put(PvrInfoDatabaseTable.PRIMARY_ID, dbRecordInfo.getMasterIdx());
        values.put(PvrInfoDatabaseTable.CH_NUM, dbRecordInfo.getChannelNo());
        //values.put(PvrInfoDatabaseTable.CHANNEL_LOCK, dbRecordInfo.getChannelLock());
        Adult = dbRecordInfo.getAdult();
        ChLock = dbRecordInfo.getChannelLock();
        tmp = ((Adult & 0xff) << 8) | (ChLock & 0xff);
        values.put(PvrInfoDatabaseTable.CHANNEL_LOCK, tmp);
        values.put(PvrInfoDatabaseTable.PARENT_RATING, dbRecordInfo.getRating());
        values.put(PvrInfoDatabaseTable.RECORD_STATUS, dbRecordInfo.getRecordStatus());
        values.put(PvrInfoDatabaseTable.CHANNEL_ID, dbRecordInfo.getChannelId());
        //values.put(PvrInfoDatabaseTable.START_TIME, System.currentTimeMillis());
        values.put(PvrInfoDatabaseTable.START_TIME, dbRecordInfo.getStartTime());
        values.put(PvrInfoDatabaseTable.DURATION, dbRecordInfo.getDurationSec());
        values.put(PvrInfoDatabaseTable.PLAY_STOP_POS, dbRecordInfo.getPlayStopPos());
        values.put(PvrInfoDatabaseTable.SERVICE_TYPE, dbRecordInfo.getServiceType());
        values.put(PvrInfoDatabaseTable.FILE_SIZE, dbRecordInfo.getFileSize());
        values.put(PvrInfoDatabaseTable.CHNAME, dbRecordInfo.getChName());

        // only keeps name before last ':' if insert master record of series
        String eventName = dbRecordInfo.getEventName();
        if (dbRecordInfo.getIsSeries()) {
            int lastIndexOfEpisode = eventName.lastIndexOf(":");
            if (lastIndexOfEpisode != -1) {
                eventName = eventName.substring(0, lastIndexOfEpisode);
            }
        }

        values.put(PvrInfoDatabaseTable.EVENT_NAME, eventName);
        values.put(PvrInfoDatabaseTable.FULLNAMEPATH, dbRecordInfo.getFullNamePath());
        values.put(PvrInfoDatabaseTable.SERIES_KEY, dbRecordInfo.getSeriesKey());
        values.put(PvrInfoDatabaseTable.PLAY_TIME, dbRecordInfo.getPlayTime());

        //String videoInfoJson = gson.toJson(dbRecordInfo.getVideoInfo());
        String videoInfoJson = ProgramInfo.videoInfoSerialize(dbRecordInfo.getVideoInfo());
        values.put(PvrInfoDatabaseTable.VIDEO_INFO_JSON, videoInfoJson);
        //String audioInfoJson = gson.toJson(dbRecordInfo.getAudiosInfoList());
        String audioInfoJson = ProgramInfo.audioInfoSerialize(dbRecordInfo.getAudiosInfoList());
        values.put(PvrInfoDatabaseTable.AUDIO_INFO_JSON, audioInfoJson);
        //String subtitleInfoJson = gson.toJson(dbRecordInfo.getSubtitleInfo());
        String subtitleInfoJson = ProgramInfo.subtitleListSerialize(dbRecordInfo.getSubtitleInfo());
        values.put(PvrInfoDatabaseTable.SUBTITLE_INFO_JSON, subtitleInfoJson);
        //String teletextInfoJson = gson.toJson(dbRecordInfo.getTeletextList());
        String teletextInfoJson = ProgramInfo.teletextListSerialize(dbRecordInfo.getTeletextList());
        values.put(PvrInfoDatabaseTable.TELETEXT_INFO_JSON, teletextInfoJson);
        String epgInfoJson = gson.toJson(dbRecordInfo.getEpgInfo());
        values.put(PvrInfoDatabaseTable.EPG_INFO_JSON, epgInfoJson);
        values.put(PvrInfoDatabaseTable.SERIES, dbRecordInfo.getIsSeries()? 1 : 0);//convert bool into int and and store it in SQLite
        values.put(PvrInfoDatabaseTable.TOTAL_RECORDS, dbRecordInfo.getTotalRecordTime());
        values.put(PvrInfoDatabaseTable.TOTAL_EPISODES, dbRecordInfo.getTotalEpisode());
        return resolver.insert(PvrContentProvider.get_CONTENT_MASTER_URI(), values);
    }

    private Uri insertSeriesRecord(PvrDbRecordInfo dbRecordInfo) {
        int masterId=dbRecordInfo.getMasterIdx();//series table id
        if (masterId < 0) {
            throw new IllegalArgumentException("Invalid seriesId");
        }
        Gson gson = new Gson();
        Uri seriesTableUri = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterId);
        ContentValues values = new ContentValues();
        int ChLock=0,Adult=0,tmp=0;

        values.put(PvrInfoDatabaseTable.SERIES_PRIMARY_ID, dbRecordInfo.getSeriesIdx());
        values.put(PvrInfoDatabaseTable.SERIES_CH_NUM, dbRecordInfo.getChannelNo());
        //values.put(PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK, dbRecordInfo.getChannelLock());

        Adult = dbRecordInfo.getAdult();
        ChLock = dbRecordInfo.getChannelLock();
        tmp = ((Adult & 0xff) << 8) | (ChLock & 0xff);
        values.put(PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK, tmp);

        values.put(PvrInfoDatabaseTable.SERIES_PARENT_RATING, dbRecordInfo.getRating());
        values.put(PvrInfoDatabaseTable.SERIES_RECORD_STATUS, dbRecordInfo.getRecordStatus());
        values.put(PvrInfoDatabaseTable.SERIES_CHANNEL_ID, dbRecordInfo.getChannelId());
        values.put(PvrInfoDatabaseTable.SERIES_START_TIME, dbRecordInfo.getStartTime());
        values.put(PvrInfoDatabaseTable.SERIES_DURATION, dbRecordInfo.getDurationSec());
        values.put(PvrInfoDatabaseTable.SERIES_PLAY_STOP_POS, dbRecordInfo.getPlayStopPos());
        values.put(PvrInfoDatabaseTable.SERIES_SERVICE_TYPE, dbRecordInfo.getServiceType());
        values.put(PvrInfoDatabaseTable.SERIES_FILE_SIZE, dbRecordInfo.getFileSize());
        values.put(PvrInfoDatabaseTable.SERIES_CHNAME, dbRecordInfo.getChName());
        values.put(PvrInfoDatabaseTable.SERIES_EVENT_NAME, dbRecordInfo.getEventName());
        values.put(PvrInfoDatabaseTable.SERIES_FULLNAMEPATH, dbRecordInfo.getFullNamePath());
        values.put(PvrInfoDatabaseTable.SERIES_SERIES_KEY, dbRecordInfo.getSeriesKey());
        values.put(PvrInfoDatabaseTable.SERIES_PLAY_TIME, dbRecordInfo.getPlayTime());

        //String videoInfoJson = gson.toJson(dbRecordInfo.getVideoInfo());
        String videoInfoJson = ProgramInfo.videoInfoSerialize(dbRecordInfo.getVideoInfo());
        values.put(PvrInfoDatabaseTable.VIDEO_INFO_JSON, videoInfoJson);
        //String audioInfoJson = gson.toJson(dbRecordInfo.getAudiosInfoList());
        String audioInfoJson = ProgramInfo.audioInfoSerialize(dbRecordInfo.getAudiosInfoList());
        values.put(PvrInfoDatabaseTable.AUDIO_INFO_JSON, audioInfoJson);
        //String subtitleInfoJson = gson.toJson(dbRecordInfo.getSubtitleInfo());
        String subtitleInfoJson = ProgramInfo.subtitleListSerialize(dbRecordInfo.getSubtitleInfo());
        values.put(PvrInfoDatabaseTable.SUBTITLE_INFO_JSON, subtitleInfoJson);
        //String teletextInfoJson = gson.toJson(dbRecordInfo.getTeletextList());
        String teletextInfoJson = ProgramInfo.teletextListSerialize(dbRecordInfo.getTeletextList());
        values.put(PvrInfoDatabaseTable.TELETEXT_INFO_JSON, teletextInfoJson);
        String epgInfoJson = gson.toJson(dbRecordInfo.getEpgInfo());
        values.put(PvrInfoDatabaseTable.EPG_INFO_JSON, epgInfoJson);
        values.put(PvrInfoDatabaseTable.SERIES_EPISODE, dbRecordInfo.getEpisode());
        return resolver.insert(seriesTableUri, values);
    }
    private void updateSeriesRecord(String columnName, int newValue, PvrRecIdx pvrRecIdx) {
        ContentValues values = new ContentValues();
        values.put(columnName, newValue);
        Uri seriesTableUri = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + pvrRecIdx.getMasterIdx());

        String where = PvrInfoDatabaseTable.SERIES_PRIMARY_ID + " = ?";
        String[] whereArgs = { String.valueOf(pvrRecIdx.getSeriesIdx()) };

        try {
            int rowsUpdated = resolver.update(seriesTableUri, values, where, whereArgs);
            if (rowsUpdated > 0) {
                // Log.d(TAG, "Successfully updated " + columnName + " for masterId " + masterId);
            } else {
                // Log.d(TAG, "Failed to update " + columnName + " for masterId " + masterId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private PvrDbRecordInfo queryMaster(Context context, int masterIdx) {
        ProgramInfo programInfo = new ProgramInfo();
        PvrDbRecordInfo dbRecordInfo = new PvrDbRecordInfo();
        EPGEvent epgEvent = new EPGEvent();
        Cursor cursor = null;
        int ChLock=0,Adult=0,tmp=0;
        try {
            Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
            String[] projection = {
                    PvrInfoDatabaseTable.PRIMARY_ID, PvrInfoDatabaseTable.CH_NUM, PvrInfoDatabaseTable.CHANNEL_LOCK, PvrInfoDatabaseTable.PARENT_RATING, PvrInfoDatabaseTable.RECORD_STATUS,
                    PvrInfoDatabaseTable.CHANNEL_ID, PvrInfoDatabaseTable.START_TIME, PvrInfoDatabaseTable.DURATION, PvrInfoDatabaseTable.PLAY_STOP_POS, PvrInfoDatabaseTable.SERVICE_TYPE,
                    PvrInfoDatabaseTable.FILE_SIZE, PvrInfoDatabaseTable.CHNAME, PvrInfoDatabaseTable.EVENT_NAME, PvrInfoDatabaseTable.FULLNAMEPATH, PvrInfoDatabaseTable.SERIES_KEY,
                    PvrInfoDatabaseTable.PLAY_TIME, PvrInfoDatabaseTable.VIDEO_INFO_JSON, PvrInfoDatabaseTable.AUDIO_INFO_JSON, PvrInfoDatabaseTable.SUBTITLE_INFO_JSON,
                    PvrInfoDatabaseTable.TELETEXT_INFO_JSON, PvrInfoDatabaseTable.EPG_INFO_JSON, PvrInfoDatabaseTable.SERIES, PvrInfoDatabaseTable.TOTAL_RECORDS, PvrInfoDatabaseTable.TOTAL_EPISODES
            };
            String selection = PvrInfoDatabaseTable.PRIMARY_ID + " = ?";
            String[] selectionArgs = {String.valueOf(masterIdx)};
            cursor = resolver.query(uri, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                dbRecordInfo.setMasterIdx(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.PRIMARY_ID));
                dbRecordInfo.setChannelNo(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CH_NUM));
                //dbRecordInfo.setChannelLock(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CHANNEL_LOCK));
                tmp = mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CHANNEL_LOCK);
                ChLock = tmp&0xFF;
                Adult = (tmp>>8) & 0xFF;
                dbRecordInfo.setChannelLock(ChLock);
                dbRecordInfo.setAdult(Adult);
                dbRecordInfo.setRating(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.PARENT_RATING));
                dbRecordInfo.setRecordStatus(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.RECORD_STATUS));
                dbRecordInfo.setChannelId(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CHANNEL_ID));
                //dbRecordInfo.setStartTime(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.START_TIME));
                dbRecordInfo.setStartTime(mDbUtils.GetLongFromTable(cursor,PvrInfoDatabaseTable.START_TIME));
                dbRecordInfo.setDurationSec(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.DURATION));
                dbRecordInfo.setPlayStopPos(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.PLAY_STOP_POS));
                dbRecordInfo.setServiceType(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.SERVICE_TYPE));
                dbRecordInfo.setFileSize(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.FILE_SIZE));
                dbRecordInfo.setChName(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.CHNAME));
                dbRecordInfo.setEventName(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.EVENT_NAME));
                dbRecordInfo.setFullNamePath(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.FULLNAMEPATH));
                dbRecordInfo.setSeriesKey(mDbUtils.GetByteArrayFromTable(cursor,PvrInfoDatabaseTable.SERIES_KEY));
                dbRecordInfo.setPlayTime(mDbUtils.GetLongFromTable(cursor,PvrInfoDatabaseTable.PLAY_TIME));
                dbRecordInfo.setVideoInfo(programInfo.getVideoInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.VIDEO_INFO_JSON)));
                dbRecordInfo.setAudiosInfoList(programInfo.getAudioInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.AUDIO_INFO_JSON)));
                dbRecordInfo.setSubtitleInfo(programInfo.getSubtitleInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SUBTITLE_INFO_JSON)));
                dbRecordInfo.setTeletextList(programInfo.getTeletextInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.TELETEXT_INFO_JSON)));
                dbRecordInfo.setEpgInfo(epgEvent.getEPGEventFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.EPG_INFO_JSON)));
                dbRecordInfo.setIsSeries(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.SERIES) == 1);//convert int to bool: 1 -> true, 0 -> false
                dbRecordInfo.setTotalRecordTime(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.TOTAL_RECORDS));
                dbRecordInfo.setTotalEpisode(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.TOTAL_EPISODES));
            }

        } catch(Exception e){
            Log.e(TAG, "Error querying Master Table", e);
        } finally{
            if (cursor != null) {
                cursor.close();
            }
        }
        return dbRecordInfo;
    }

    private PvrDbRecordInfo querySeries(Context context, int masterIdx, int seriesIdx) {
        ProgramInfo programInfo = new ProgramInfo();
        EPGEvent epgEvent = new EPGEvent();
        PvrDbRecordInfo dbRecordInfo = new PvrDbRecordInfo();
        Cursor cursor = null;
        int tmp=0,ChLock=0,Adult=0;
        try {
            Uri CONTENT_SERIES_URI = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterIdx);
            String[] projection = {
                    PvrInfoDatabaseTable.SERIES_PRIMARY_ID, PvrInfoDatabaseTable.SERIES_CH_NUM, PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK, PvrInfoDatabaseTable.SERIES_PARENT_RATING, PvrInfoDatabaseTable.SERIES_RECORD_STATUS,
                    PvrInfoDatabaseTable.SERIES_CHANNEL_ID, PvrInfoDatabaseTable.SERIES_START_TIME, PvrInfoDatabaseTable.SERIES_DURATION, PvrInfoDatabaseTable.SERIES_PLAY_STOP_POS, PvrInfoDatabaseTable.SERIES_SERVICE_TYPE,
                    PvrInfoDatabaseTable.SERIES_FILE_SIZE, PvrInfoDatabaseTable.SERIES_CHNAME, PvrInfoDatabaseTable.SERIES_EVENT_NAME, PvrInfoDatabaseTable.SERIES_FULLNAMEPATH, PvrInfoDatabaseTable.SERIES_SERIES_KEY,
                    PvrInfoDatabaseTable.SERIES_PLAY_TIME, PvrInfoDatabaseTable.SERIES_VIDEO_INFO_JSON, PvrInfoDatabaseTable.SERIES_AUDIO_INFO_JSON, PvrInfoDatabaseTable.SERIES_SUBTITLE_INFO_JSON,
                    PvrInfoDatabaseTable.SERIES_TELETEXT_INFO_JSON, PvrInfoDatabaseTable.SERIES_EPG_INFO_JSON, PvrInfoDatabaseTable.SERIES_EPISODE
            };
            String selection = PvrInfoDatabaseTable.SERIES_PRIMARY_ID + " = ?";

            String[] selectionArgs = {String.valueOf(seriesIdx)};

            cursor = resolver.query(CONTENT_SERIES_URI, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                dbRecordInfo.setMasterIdx(masterIdx);
                dbRecordInfo.setSeriesIdx(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_PRIMARY_ID));
                dbRecordInfo.setChannelNo(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_CH_NUM));
                //dbRecordInfo.setChannelLock(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK));

                tmp = mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK);
                ChLock = tmp&0xFF;
                Adult = (tmp>>8) & 0xFF;
                dbRecordInfo.setChannelLock(ChLock);
                dbRecordInfo.setAdult(Adult);

                dbRecordInfo.setRating(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_PARENT_RATING));
                dbRecordInfo.setRecordStatus(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_RECORD_STATUS));
                dbRecordInfo.setChannelId(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_CHANNEL_ID));
                //dbRecordInfo.setStartTime(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_START_TIME));
                dbRecordInfo.setStartTime(mDbUtils.GetLongFromTable(cursor, PvrInfoDatabaseTable.SERIES_START_TIME));
                dbRecordInfo.setDurationSec(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_DURATION));
                dbRecordInfo.setPlayStopPos(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_PLAY_STOP_POS));
                dbRecordInfo.setServiceType(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_SERVICE_TYPE));
                dbRecordInfo.setFileSize(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_FILE_SIZE));
                dbRecordInfo.setChName(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_CHNAME));
                dbRecordInfo.setEventName(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_EVENT_NAME));
                dbRecordInfo.setFullNamePath(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_FULLNAMEPATH));
                dbRecordInfo.setSeriesKey(mDbUtils.GetByteArrayFromTable(cursor,PvrInfoDatabaseTable.SERIES_SERIES_KEY));
                dbRecordInfo.setPlayTime(mDbUtils.GetLongFromTable(cursor, PvrInfoDatabaseTable.SERIES_PLAY_TIME));
                dbRecordInfo.setVideoInfo(programInfo.getVideoInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_VIDEO_INFO_JSON)));
                dbRecordInfo.setAudiosInfoList(programInfo.getAudioInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_AUDIO_INFO_JSON)));
                dbRecordInfo.setSubtitleInfo(programInfo.getSubtitleInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_SUBTITLE_INFO_JSON)));
                dbRecordInfo.setTeletextList(programInfo.getTeletextInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_TELETEXT_INFO_JSON)));
                dbRecordInfo.setEpgInfo(epgEvent.getEPGEventFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_EPG_INFO_JSON)));
                dbRecordInfo.setEpisode(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_EPISODE));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying Series Table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return dbRecordInfo;
    }
    private PvrRecFileInfo queryPlayback(Context context, int masterIdx, int seriesIdx) {

        PvrRecFileInfo pvrRecFileInfo = null;
        Cursor cursor = null;

        try {
            String[] projection = {
                    PvrInfoDatabaseTable.MASTER_ID, PvrInfoDatabaseTable.SERIES_ID,
                    PvrInfoDatabaseTable.IS_SERIES,
                    PvrInfoDatabaseTable.PLAYBACK_TIME
            };
            String selection = PvrInfoDatabaseTable.MASTER_ID + " = ? AND " + PvrInfoDatabaseTable.SERIES_ID + " = ?";

            String[] selectionArgs = {String.valueOf(masterIdx), String.valueOf(seriesIdx)};

            cursor = resolver.query(PvrContentProvider.get_CONTENT_PLAYBACK_URI(), projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                pvrRecFileInfo = new PvrRecFileInfo();
                pvrRecFileInfo.setMasterIdx(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.MASTER_ID));
                pvrRecFileInfo.setSeriesIdx(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_ID));
                pvrRecFileInfo.setIsSeries(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.IS_SERIES) == 1);//convert int to bool: 1 -> true, 0 -> false
                pvrRecFileInfo.setPlaybackTime(mDbUtils.GetLongFromTable(cursor, PvrInfoDatabaseTable.PLAYBACK_TIME));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying Series Table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return pvrRecFileInfo;
    }
    private void deleteFileOrDirectory(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            Log.e(TAG, "Invalid file path");
            return;
        }

        if (filePath.startsWith("content://")) {

            deleteFileByContentResolver(filePath);
        } else {

            deleteFileByFileAPI(new File(filePath));
        }
    }

    private void deleteFileByContentResolver(String filePath) {
        Uri fileUri = Uri.parse(filePath);

        try {
            int deletedRows = resolver.delete(fileUri, null, null);
            if (deletedRows > 0) {
                Log.d(TAG, "Deleted file via ContentProvider: " + filePath);
            } else {
                Log.w(TAG, "No file deleted via ContentProvider: " + filePath);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete file via ContentProvider: " + filePath, e);
        }
    }

    private void deleteFileByFileAPI(File fileOrDir) {
        if (!fileOrDir.exists()) {
            Log.w(TAG, "File/Directory does not exist: " + fileOrDir.getAbsolutePath());
            return;
        }

        if (fileOrDir.isDirectory()) {
            for (File child : fileOrDir.listFiles()) {
                deleteFileByFileAPI(child);
            }
        }

        if (fileOrDir.delete()) {
            Log.d(TAG, "Deleted file or directory: " + fileOrDir.getAbsolutePath());
        } else {
            Log.e(TAG, "Failed to delete file or directory: " + fileOrDir.getAbsolutePath());
        }
    }

    public ArrayList<PvrDbRecordInfo> getMasterFilesInfoList(int startIndex, int count)
    {
        ArrayList<PvrDbRecordInfo> dvrDbRecordInfoList = new ArrayList<>();
        Cursor cursor = null;
        int ChLock=0,Adult=0,tmp=0;
        try {
            Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
            String[] projection = {
                    PvrInfoDatabaseTable.PRIMARY_ID, PvrInfoDatabaseTable.CH_NUM, PvrInfoDatabaseTable.CHANNEL_LOCK, PvrInfoDatabaseTable.PARENT_RATING, PvrInfoDatabaseTable.RECORD_STATUS,
                    PvrInfoDatabaseTable.CHANNEL_ID, PvrInfoDatabaseTable.START_TIME, PvrInfoDatabaseTable.DURATION, PvrInfoDatabaseTable.PLAY_STOP_POS, PvrInfoDatabaseTable.SERVICE_TYPE,
                    PvrInfoDatabaseTable.FILE_SIZE, PvrInfoDatabaseTable.CHNAME, PvrInfoDatabaseTable.EVENT_NAME, PvrInfoDatabaseTable.FULLNAMEPATH, PvrInfoDatabaseTable.SERIES_KEY,
                    PvrInfoDatabaseTable.PLAY_TIME, PvrInfoDatabaseTable.VIDEO_INFO_JSON, PvrInfoDatabaseTable.AUDIO_INFO_JSON, PvrInfoDatabaseTable.SUBTITLE_INFO_JSON,
                    PvrInfoDatabaseTable.TELETEXT_INFO_JSON, PvrInfoDatabaseTable.EPG_INFO_JSON, PvrInfoDatabaseTable.SERIES, PvrInfoDatabaseTable.TOTAL_RECORDS, PvrInfoDatabaseTable.TOTAL_EPISODES
            };
            String selection = null;
            String[] selectionArgs = null;
            String orderBy = PvrInfoDatabaseTable.PRIMARY_ID + " ASC" + " LIMIT " + count + " OFFSET " + startIndex;

            cursor = resolver.query(uri, projection, selection, selectionArgs, orderBy);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ProgramInfo programInfo = new ProgramInfo();
                    PvrDbRecordInfo dbRecordInfo = new PvrDbRecordInfo();
                    EPGEvent epgEvent = new EPGEvent();

                    dbRecordInfo.setMasterIdx(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.PRIMARY_ID));
                    dbRecordInfo.setChannelNo(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CH_NUM));
                    //dbRecordInfo.setChannelLock(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CHANNEL_LOCK));
                    tmp = mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CHANNEL_LOCK);
                    ChLock = tmp&0xFF;
                    Adult = (tmp>>8) & 0xFF;
                    dbRecordInfo.setChannelLock(ChLock);
                    dbRecordInfo.setAdult(Adult);

                    dbRecordInfo.setRating(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.PARENT_RATING));
                    dbRecordInfo.setRecordStatus(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.RECORD_STATUS));
                    dbRecordInfo.setChannelId(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CHANNEL_ID));
                    //dbRecordInfo.setStartTime(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.START_TIME));
                    dbRecordInfo.setStartTime(mDbUtils.GetLongFromTable(cursor,PvrInfoDatabaseTable.START_TIME));
                    dbRecordInfo.setDurationSec(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.DURATION));
                    dbRecordInfo.setPlayStopPos(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.PLAY_STOP_POS));
                    dbRecordInfo.setServiceType(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.SERVICE_TYPE));
                    dbRecordInfo.setFileSize(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.FILE_SIZE));
                    dbRecordInfo.setChName(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.CHNAME));
                    dbRecordInfo.setEventName(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.EVENT_NAME));
                    dbRecordInfo.setFullNamePath(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.FULLNAMEPATH));
                    dbRecordInfo.setSeriesKey(mDbUtils.GetByteArrayFromTable(cursor,PvrInfoDatabaseTable.SERIES_KEY));
                    dbRecordInfo.setPlayTime(mDbUtils.GetLongFromTable(cursor,PvrInfoDatabaseTable.PLAY_TIME));
                    dbRecordInfo.setVideoInfo(programInfo.getVideoInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.VIDEO_INFO_JSON)));
                    dbRecordInfo.setAudiosInfoList(programInfo.getAudioInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.AUDIO_INFO_JSON)));
                    dbRecordInfo.setSubtitleInfo(programInfo.getSubtitleInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SUBTITLE_INFO_JSON)));
                    dbRecordInfo.setTeletextList(programInfo.getTeletextInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.TELETEXT_INFO_JSON)));
                    dbRecordInfo.setEpgInfo(epgEvent.getEPGEventFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.EPG_INFO_JSON)));
                    dbRecordInfo.setIsSeries(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.SERIES) == 1);//convert int to bool: 1 -> true, 0 -> false
                    dbRecordInfo.setTotalRecordTime(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.TOTAL_RECORDS));
                    dbRecordInfo.setTotalEpisode(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.TOTAL_EPISODES));
                    dvrDbRecordInfoList.add(dbRecordInfo);
                }
                while (cursor.moveToNext());
            }

        } catch(Exception e){
            Log.e(TAG, "Error querying Master Table", e);
        } finally{
            if (cursor != null) {
                cursor.close();
            }
        }
        return dvrDbRecordInfoList;
    }
    public ArrayList<PvrDbRecordInfo> getPlaybackFilesInfoList(int startIndex, int count)
    {
        ArrayList<PvrDbRecordInfo> dvrDbRecordInfoList = new ArrayList<>();
        Cursor cursor = null;
        try {
            Uri uri = PvrContentProvider.get_CONTENT_PLAYBACK_URI();
            String[] projection = {
                    PvrInfoDatabaseTable.MASTER_ID, PvrInfoDatabaseTable.SERIES_ID,
                    PvrInfoDatabaseTable.IS_SERIES, PvrInfoDatabaseTable.PLAYBACK_TIME
            };
            String selection = null;
            String[] selectionArgs = null;
            String orderBy = PvrInfoDatabaseTable.PLAYBACK_TIME + " DESC" + " LIMIT " + count + " OFFSET " + startIndex;

            cursor = resolver.query(uri, projection, selection, selectionArgs, orderBy);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    PvrDbRecordInfo dbRecordInfo = new PvrDbRecordInfo();
                    int masterIdx = mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.MASTER_ID);
                    int seriesIdx = mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_ID);
                    long playbackTime = mDbUtils.GetLongFromTable(cursor, PvrInfoDatabaseTable.PLAYBACK_TIME);
                    PvrRecIdx pvrRecIdx = new PvrRecIdx(masterIdx,seriesIdx);
                    dbRecordInfo = new PvrDataManager(mContext).queryDataFromTable(pvrRecIdx);
                    dbRecordInfo.setPlaybackTime(playbackTime);
                    dvrDbRecordInfoList.add(dbRecordInfo);
                }
                while (cursor.moveToNext());
            }

        } catch(Exception e){
            Log.e(TAG, "Error querying Master Table", e);
        } finally{
            if (cursor != null) {
                cursor.close();
            }
        }
        return dvrDbRecordInfoList;
    }
    public int addDataToPlaybackTable(PvrRecFileInfo pvrRecFileInfo)
    {
        Uri result = null;
        if (pvrRecFileInfo == null) {
            return -1;
        }
        PvrRecFileInfo playbackRecFileInfo;
        //check record exists in the table
        playbackRecFileInfo = queryPlayback(mContext, pvrRecFileInfo.getMasterIdx(), pvrRecFileInfo.getSeriesIdx());
        pvrRecFileInfo.setPlaybackTime(System.currentTimeMillis());
        if (playbackRecFileInfo == null)
        {
            //If it does not exist, add it to the table
            result = insertPlaybackTable(pvrRecFileInfo);
            if (result == null) {
                return -1;
            }
        } else {
            //If it exists, reorder it based on the playback time
            updatePlaybackTable(PvrInfoDatabaseTable.PLAYBACK_TIME, pvrRecFileInfo.getPlaybackTime(), pvrRecFileInfo.getMasterIdx(), pvrRecFileInfo.getSeriesIdx());
        }
        return 0;
    }
    public int addDataToTable(PvrDbRecordInfo dbRecordInfo) {

        Uri result = null;
        if (dbRecordInfo == null) {
            return -1;
        }
        if (dbRecordInfo.getIsSeries()) {
            if (dbRecordInfo.getSeriesIdx() == 0)// first record to series, need to add data to master table
            {
                //add master column to Master Table
                result = insertMasterRecord(dbRecordInfo);
                if (result == null) {
                    return -1;
                }
            } else {
                //not first record series, need to update total_episode in master table
                int totalEpisode = dbRecordInfo.getTotalEpisode();
                if (totalEpisode > 0) {
                updateMasterRecord(PvrInfoDatabaseTable.TOTAL_EPISODES, dbRecordInfo.getTotalEpisode(), dbRecordInfo.getMasterIdx());
                }
            }
            //add series column to series Table
            result = insertSeriesRecord(dbRecordInfo);
            if (result == null) {
                return -1;
            }

            return 0;
        }
        else {
            result = insertMasterRecord(dbRecordInfo);
            if (result == null) {
                return -1;
            }
            return 0;
        }
    }
    public PvrDbRecordInfo queryDataFromTable (PvrRecIdx pvrRecIdx){
        if (pvrRecIdx == null) {
            Log.e(TAG, "Invalid PvrRecIdx: null");
            return null;
        }
        PvrDbRecordInfo dbRecordInfo = new PvrDbRecordInfo();
        if (pvrRecIdx.getSeriesIdx() == 0xffff) {
            dbRecordInfo = queryMaster(mContext, pvrRecIdx.getMasterIdx());
        } else {
            dbRecordInfo = querySeries(mContext, pvrRecIdx.getMasterIdx(), pvrRecIdx.getSeriesIdx());
        }
        return dbRecordInfo;
    }

    public int modifyRecordStatus(PvrRecIdx pvrRecIdx, int recordStatus){
        if (pvrRecIdx == null) {
            Log.e(TAG, "Invalid PvrRecIdx: null");
            return -1;
        }
        if (pvrRecIdx.getSeriesIdx() == 0xffff) {
            // update recordStatus in master
            updateMasterRecord(PvrInfoDatabaseTable.RECORD_STATUS, recordStatus, pvrRecIdx.getMasterIdx());
        } else {
            if(recordStatus == 1 || recordStatus == 2) { // if series record success, update RECORD_STATUS in master
                updateMasterRecord(PvrInfoDatabaseTable.RECORD_STATUS, recordStatus, pvrRecIdx.getMasterIdx());
            }
            updateSeriesRecord(PvrInfoDatabaseTable.SERIES_RECORD_STATUS, recordStatus, pvrRecIdx);
        }
        return 0;
    }

    public int modifyDurationSec(PvrRecIdx pvrRecIdx, int DurationSec){
        if (pvrRecIdx == null) {
            Log.e(TAG, "Invalid PvrRecIdx: null");
            return -1;
        }
        if (pvrRecIdx.getSeriesIdx() == 0xffff) {
            // update recordStatus in master
            updateMasterRecord(PvrInfoDatabaseTable.DURATION, DurationSec, pvrRecIdx.getMasterIdx());
        } else {
            updateSeriesRecord(PvrInfoDatabaseTable.SERIES_DURATION, DurationSec, pvrRecIdx);
        }
        return 0;
    }

    public int modifyFileSizeKbyte(PvrRecIdx pvrRecIdx, int fileSizeKbyte){
        if (pvrRecIdx == null) {
            Log.e(TAG, "Invalid PvrRecIdx: null");
            return -1;
        }
        if (pvrRecIdx.getSeriesIdx() == 0xffff) {
            // update recordStatus in master
            updateMasterRecord(PvrInfoDatabaseTable.FILE_SIZE, fileSizeKbyte, pvrRecIdx.getMasterIdx());
        } else {
            updateSeriesRecord(PvrInfoDatabaseTable.FILE_SIZE, fileSizeKbyte, pvrRecIdx);
        }
        return 0;
    }
    public int modifyPlayStopPos(PvrRecIdx pvrRecIdx, int playStopPos) {
        if (pvrRecIdx == null) {
            Log.e(TAG, "Invalid PvrRecIdx: null");
            return -1;
        }
        if (pvrRecIdx.getSeriesIdx() == 0xffff) {
            updateMasterRecord(PvrInfoDatabaseTable.PLAY_STOP_POS, playStopPos, pvrRecIdx.getMasterIdx());
        } else {
            updateSeriesRecord(PvrInfoDatabaseTable.SERIES_PLAY_STOP_POS, playStopPos, pvrRecIdx);
        }
        return 0;
    }

    public void setSortMethod(int sortMethod) {
        mSortMethod=sortMethod;
    }

    public int getSortMethod() {
        return mSortMethod;
    }


    public void setSortMethodByType(int sortType) {
        mSortType=sortType;
    }

    public int getSortMethodByType()
    {
        return mSortType;
    }

    public int getMasterTotalRecordCount(){
        int count = 0;
        Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
        Cursor cursor = resolver.query(
                uri,
                new String[]{"COUNT(*) AS count"},
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            count = mDbUtils.GetIntFromTable(cursor, "count");
            cursor.close();
        }
        return count;
    }
    public int getPlaybackTotalRecordCount(){
        int count = 0;
        Uri uri = PvrContentProvider.get_CONTENT_PLAYBACK_URI();
        Cursor cursor = resolver.query(
                uri,
                new String[]{"COUNT(*) AS count"},
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            count = mDbUtils.GetIntFromTable(cursor, "count");
            cursor.close();
        }
        return count;
    }

    public int getMasterTotalSuccessRecordCount(){
        int count = 0;
        Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
        String selection = PvrInfoDatabaseTable.RECORD_STATUS + " != ?";
        String[] selectionArgs = {"0"}; // RECORD_STATUS != 0 means success recording
        Cursor cursor = resolver.query(
                uri,
                new String[]{"COUNT(*) AS count"},
                selection,
                selectionArgs,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            count = mDbUtils.GetIntFromTable(cursor, "count");
            cursor.close();
        }
        return count;
    }

    public int getMasterTotalErrorRecordCount(){
        int count = 0;
        Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();

        String selection = PvrInfoDatabaseTable.RECORD_STATUS + " = ?";
        String[] selectionArgs = { "0" };
        Cursor cursor = resolver.query(
                uri,
                new String[]{"COUNT(*) AS count"},
                selection,
                selectionArgs,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            count = mDbUtils.GetIntFromTable(cursor, "count");
            cursor.close();
        }
        return count;
    }

    public ArrayList<PvrDbRecordInfo> getMasterSuccessFilesInfoList(int startIndex, int count) {
        ArrayList<PvrDbRecordInfo> dvrDbRecordInfoList = new ArrayList<>();
        Cursor cursor = null;
        int ChLock=0,Adult=0,tmp=0;
        try {
            Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
            String[] projection = {
                    PvrInfoDatabaseTable.PRIMARY_ID, PvrInfoDatabaseTable.CH_NUM, PvrInfoDatabaseTable.CHANNEL_LOCK, PvrInfoDatabaseTable.PARENT_RATING, PvrInfoDatabaseTable.RECORD_STATUS,
                    PvrInfoDatabaseTable.CHANNEL_ID, PvrInfoDatabaseTable.START_TIME, PvrInfoDatabaseTable.DURATION, PvrInfoDatabaseTable.PLAY_STOP_POS, PvrInfoDatabaseTable.SERVICE_TYPE,
                    PvrInfoDatabaseTable.FILE_SIZE, PvrInfoDatabaseTable.CHNAME, PvrInfoDatabaseTable.EVENT_NAME, PvrInfoDatabaseTable.FULLNAMEPATH, PvrInfoDatabaseTable.SERIES_KEY,
                    PvrInfoDatabaseTable.PLAY_TIME, PvrInfoDatabaseTable.VIDEO_INFO_JSON, PvrInfoDatabaseTable.AUDIO_INFO_JSON, PvrInfoDatabaseTable.SUBTITLE_INFO_JSON,
                    PvrInfoDatabaseTable.TELETEXT_INFO_JSON, PvrInfoDatabaseTable.EPG_INFO_JSON, PvrInfoDatabaseTable.SERIES, PvrInfoDatabaseTable.TOTAL_RECORDS, PvrInfoDatabaseTable.TOTAL_EPISODES
            };
            String selection = PvrInfoDatabaseTable.RECORD_STATUS + " != ?";
            String[] selectionArgs = {"0"}; // RECORD_STATUS != 0 means success record
            String orderBy = PvrInfoDatabaseTable.PRIMARY_ID + " ASC" + " LIMIT " + count + " OFFSET " + startIndex;

            cursor = resolver.query(uri, projection, selection, selectionArgs, orderBy);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ProgramInfo programInfo = new ProgramInfo();
                    PvrDbRecordInfo dbRecordInfo = new PvrDbRecordInfo();
                    EPGEvent epgEvent = new EPGEvent();

                    dbRecordInfo.setMasterIdx(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.PRIMARY_ID));
                    dbRecordInfo.setChannelNo(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CH_NUM));
                    //dbRecordInfo.setChannelLock(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CHANNEL_LOCK));
                    tmp = mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CHANNEL_LOCK);
                    ChLock = tmp&0xFF;
                    Adult = (tmp>>8) & 0xFF;
                    dbRecordInfo.setChannelLock(ChLock);
                    dbRecordInfo.setAdult(Adult);

                    dbRecordInfo.setRating(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.PARENT_RATING));
                    dbRecordInfo.setRecordStatus(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.RECORD_STATUS));
                    dbRecordInfo.setChannelId(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CHANNEL_ID));
                    //dbRecordInfo.setStartTime(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.START_TIME));
                    dbRecordInfo.setStartTime(mDbUtils.GetLongFromTable(cursor,PvrInfoDatabaseTable.START_TIME));
                    dbRecordInfo.setDurationSec(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.DURATION));
                    dbRecordInfo.setPlayStopPos(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.PLAY_STOP_POS));
                    dbRecordInfo.setServiceType(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.SERVICE_TYPE));
                    dbRecordInfo.setFileSize(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.FILE_SIZE));
                    dbRecordInfo.setChName(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.CHNAME));
                    dbRecordInfo.setEventName(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.EVENT_NAME));
                    dbRecordInfo.setFullNamePath(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.FULLNAMEPATH));
                    dbRecordInfo.setSeriesKey(mDbUtils.GetByteArrayFromTable(cursor,PvrInfoDatabaseTable.SERIES_KEY));
                    dbRecordInfo.setPlayTime(mDbUtils.GetLongFromTable(cursor,PvrInfoDatabaseTable.PLAY_TIME));
                    dbRecordInfo.setVideoInfo(programInfo.getVideoInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.VIDEO_INFO_JSON)));
                    dbRecordInfo.setAudiosInfoList(programInfo.getAudioInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.AUDIO_INFO_JSON)));
                    dbRecordInfo.setSubtitleInfo(programInfo.getSubtitleInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SUBTITLE_INFO_JSON)));
                    dbRecordInfo.setTeletextList(programInfo.getTeletextInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.TELETEXT_INFO_JSON)));
                    dbRecordInfo.setEpgInfo(epgEvent.getEPGEventFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.EPG_INFO_JSON)));
                    dbRecordInfo.setIsSeries(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.SERIES) == 1);//convert int to bool: 1 -> true, 0 -> false
                    dbRecordInfo.setTotalRecordTime(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.TOTAL_RECORDS));
                    dbRecordInfo.setTotalEpisode(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.TOTAL_EPISODES));
                    dvrDbRecordInfoList.add(dbRecordInfo);
                }
                while (cursor.moveToNext());
            }

        } catch(Exception e){
            Log.e(TAG, "Error querying Master Table", e);
        } finally{
            if (cursor != null) {
                cursor.close();
            }
        }
        return dvrDbRecordInfoList;
    }

    public ArrayList<PvrDbRecordInfo> getMasterErrorFilesInfoList(int startIndex, int count){
        ArrayList<PvrDbRecordInfo> dvrDbRecordInfoList = new ArrayList<>();
        Cursor cursor = null;
        int ChLock=0,Adult=0,tmp=0;
        try {
            Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
            String[] projection = {
                    PvrInfoDatabaseTable.PRIMARY_ID, PvrInfoDatabaseTable.CH_NUM, PvrInfoDatabaseTable.CHANNEL_LOCK, PvrInfoDatabaseTable.PARENT_RATING, PvrInfoDatabaseTable.RECORD_STATUS,
                    PvrInfoDatabaseTable.CHANNEL_ID, PvrInfoDatabaseTable.START_TIME, PvrInfoDatabaseTable.DURATION, PvrInfoDatabaseTable.PLAY_STOP_POS, PvrInfoDatabaseTable.SERVICE_TYPE,
                    PvrInfoDatabaseTable.FILE_SIZE, PvrInfoDatabaseTable.CHNAME, PvrInfoDatabaseTable.EVENT_NAME, PvrInfoDatabaseTable.FULLNAMEPATH, PvrInfoDatabaseTable.SERIES_KEY,
                    PvrInfoDatabaseTable.PLAY_TIME, PvrInfoDatabaseTable.VIDEO_INFO_JSON, PvrInfoDatabaseTable.AUDIO_INFO_JSON, PvrInfoDatabaseTable.SUBTITLE_INFO_JSON,
                    PvrInfoDatabaseTable.TELETEXT_INFO_JSON, PvrInfoDatabaseTable.EPG_INFO_JSON, PvrInfoDatabaseTable.SERIES, PvrInfoDatabaseTable.TOTAL_RECORDS, PvrInfoDatabaseTable.TOTAL_EPISODES
            };
            String selection = PvrInfoDatabaseTable.RECORD_STATUS + " = ?";
            String[] selectionArgs = {"0"}; // RECORD_STATUS = 0 means error record
            String orderBy = PvrInfoDatabaseTable.PRIMARY_ID + " ASC" + " LIMIT " + count + " OFFSET " + startIndex;

            cursor = resolver.query(uri, projection, selection, selectionArgs, orderBy);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ProgramInfo programInfo = new ProgramInfo();
                    PvrDbRecordInfo dbRecordInfo = new PvrDbRecordInfo();
                    EPGEvent epgEvent = new EPGEvent();

                    dbRecordInfo.setMasterIdx(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.PRIMARY_ID));
                    dbRecordInfo.setChannelNo(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CH_NUM));
                    //dbRecordInfo.setChannelLock(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CHANNEL_LOCK));
                    tmp = mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CHANNEL_LOCK);
                    ChLock = tmp&0xFF;
                    Adult = (tmp>>8) & 0xFF;
                    dbRecordInfo.setChannelLock(ChLock);
                    dbRecordInfo.setAdult(Adult);

                    dbRecordInfo.setRating(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.PARENT_RATING));
                    dbRecordInfo.setRecordStatus(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.RECORD_STATUS));
                    dbRecordInfo.setChannelId(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.CHANNEL_ID));
                    //dbRecordInfo.setStartTime(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.START_TIME));
                    dbRecordInfo.setStartTime(mDbUtils.GetLongFromTable(cursor,PvrInfoDatabaseTable.START_TIME));
                    dbRecordInfo.setDurationSec(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.DURATION));
                    dbRecordInfo.setPlayStopPos(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.PLAY_STOP_POS));
                    dbRecordInfo.setServiceType(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.SERVICE_TYPE));
                    dbRecordInfo.setFileSize(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.FILE_SIZE));
                    dbRecordInfo.setChName(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.CHNAME));
                    dbRecordInfo.setEventName(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.EVENT_NAME));
                    dbRecordInfo.setFullNamePath(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.FULLNAMEPATH));
                    dbRecordInfo.setSeriesKey(mDbUtils.GetByteArrayFromTable(cursor,PvrInfoDatabaseTable.SERIES_KEY));
                    dbRecordInfo.setPlayTime(mDbUtils.GetLongFromTable(cursor,PvrInfoDatabaseTable.PLAY_TIME));
                    dbRecordInfo.setVideoInfo(programInfo.getVideoInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.VIDEO_INFO_JSON)));
                    dbRecordInfo.setAudiosInfoList(programInfo.getAudioInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.AUDIO_INFO_JSON)));
                    dbRecordInfo.setSubtitleInfo(programInfo.getSubtitleInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SUBTITLE_INFO_JSON)));
                    dbRecordInfo.setTeletextList(programInfo.getTeletextInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.TELETEXT_INFO_JSON)));
                    dbRecordInfo.setEpgInfo(epgEvent.getEPGEventFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.EPG_INFO_JSON)));
                    dbRecordInfo.setIsSeries(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.SERIES) == 1);//convert int to bool: 1 -> true, 0 -> false
                    dbRecordInfo.setTotalRecordTime(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.TOTAL_RECORDS));
                    dbRecordInfo.setTotalEpisode(mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.TOTAL_EPISODES));
                    dvrDbRecordInfoList.add(dbRecordInfo);
                }
                while (cursor.moveToNext());
            }

        } catch(Exception e){
            Log.e(TAG, "Error querying Master Table", e);
        } finally{
            if (cursor != null) {
                cursor.close();
            }
        }
        return dvrDbRecordInfoList;
    }

    public int getSeriesRecordTotalCount(int masterIdx){
        int count = 0;
        Uri seriesTableUri = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterIdx);
        Cursor cursor = resolver.query(
                seriesTableUri,
                new String[]{"COUNT(*) AS count"},
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            count = mDbUtils.GetIntFromTable(cursor,"count");
            cursor.close();
        }
        return count;
    }

    public int getSeriesMaxPrimaryId(int masterIdx) {
        int maxPrimaryId = -1;
        Cursor cursor = null;
        try {
            //Uri uri = PvrInfoDatabaseTable.CONTENT_MASTER_URI;
            Uri uri = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterIdx);
            String[] projection = {PvrInfoDatabaseTable.SERIES_PRIMARY_ID};

            // 關鍵：依據 PRIMARY_ID 降序排列，並只取第一筆 (最大的 PRIMARY_ID)
            String sortOrder = PvrInfoDatabaseTable.SERIES_PRIMARY_ID + " DESC LIMIT 1";
            cursor = resolver.query(uri, projection, null, null, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                maxPrimaryId = mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_PRIMARY_ID);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting max PRIMARY_ID", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return maxPrimaryId;
    }

    public int getSeriesSuccessRecordTotalCount(int masterIdx){
        int count = 0;
        Uri seriesTableUri = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterIdx);
        String selection = PvrInfoDatabaseTable.SERIES_RECORD_STATUS + " != ?";
        String[] selectionArgs = {"0"}; // SERIES_RECORD_STATUS != 0 means success recording
        Cursor cursor = resolver.query(
                seriesTableUri,
                new String[]{"COUNT(*) AS count"},
                selection,
                selectionArgs,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            count = mDbUtils.GetIntFromTable(cursor,"count");
            cursor.close();
        }
        return count;
    }

    public int getSeriesErrorRecordTotalCount(int masterIdx){
        int count = 0;
        Uri seriesTableUri = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterIdx);


        String selection = PvrInfoDatabaseTable.SERIES_RECORD_STATUS + " = ?";
        String[] selectionArgs = { "0" };

        Cursor cursor = resolver.query(
                seriesTableUri,
                new String[]{"COUNT(*) AS count"},
                selection,
                selectionArgs,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            count = mDbUtils.GetIntFromTable(cursor, "count");
            cursor.close();
        }
        return count;
    }

    public ArrayList<PvrDbRecordInfo> getSeriesFilesInfoList(int masterIdx, int startIndex, int count){
        ArrayList<PvrDbRecordInfo> dvrDbRecordInfoList = new ArrayList<>();
        Cursor cursor = null;
        int ChLock=0,Adult=0,tmp=0;
        try {
            Uri CONTENT_SERIES_URI = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterIdx);
            String[] projection = {
                    PvrInfoDatabaseTable.SERIES_PRIMARY_ID, PvrInfoDatabaseTable.SERIES_CH_NUM, PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK, PvrInfoDatabaseTable.SERIES_PARENT_RATING, PvrInfoDatabaseTable.SERIES_RECORD_STATUS,
                    PvrInfoDatabaseTable.SERIES_CHANNEL_ID, PvrInfoDatabaseTable.SERIES_START_TIME, PvrInfoDatabaseTable.SERIES_DURATION, PvrInfoDatabaseTable.SERIES_PLAY_STOP_POS, PvrInfoDatabaseTable.SERIES_SERVICE_TYPE,
                    PvrInfoDatabaseTable.SERIES_FILE_SIZE, PvrInfoDatabaseTable.SERIES_CHNAME, PvrInfoDatabaseTable.SERIES_EVENT_NAME, PvrInfoDatabaseTable.SERIES_FULLNAMEPATH, PvrInfoDatabaseTable.SERIES_SERIES_KEY,
                    PvrInfoDatabaseTable.SERIES_PLAY_TIME, PvrInfoDatabaseTable.SERIES_VIDEO_INFO_JSON, PvrInfoDatabaseTable.SERIES_AUDIO_INFO_JSON, PvrInfoDatabaseTable.SERIES_SUBTITLE_INFO_JSON,
                    PvrInfoDatabaseTable.SERIES_TELETEXT_INFO_JSON, PvrInfoDatabaseTable.SERIES_EPG_INFO_JSON, PvrInfoDatabaseTable.SERIES_EPISODE
            };
            String orderBy = PvrInfoDatabaseTable.SERIES_PRIMARY_ID + " ASC";
            String limit = startIndex + ", " + count;
            cursor = resolver.query(CONTENT_SERIES_URI, projection, null, null, orderBy + " LIMIT " + limit);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ProgramInfo programInfo = new ProgramInfo();
                    EPGEvent epgEvent = new EPGEvent();
                    PvrDbRecordInfo dbRecordInfo = new PvrDbRecordInfo();

                    dbRecordInfo.setMasterIdx(masterIdx);
                    dbRecordInfo.setSeriesIdx(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_PRIMARY_ID));
                    dbRecordInfo.setChannelNo(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_CH_NUM));
                    //dbRecordInfo.setChannelLock(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK));
                    tmp = mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK);
                    ChLock = tmp&0xFF;
                    Adult = (tmp>>8) & 0xFF;
                    dbRecordInfo.setChannelLock(ChLock);
                    dbRecordInfo.setAdult(Adult);

                    dbRecordInfo.setRating(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_PARENT_RATING));
                    dbRecordInfo.setRecordStatus(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_RECORD_STATUS));
                    dbRecordInfo.setChannelId(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_CHANNEL_ID));
                    //dbRecordInfo.setStartTime(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_START_TIME));
                    dbRecordInfo.setStartTime(mDbUtils.GetLongFromTable(cursor, PvrInfoDatabaseTable.SERIES_START_TIME));
                    dbRecordInfo.setDurationSec(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_DURATION));
                    dbRecordInfo.setPlayStopPos(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_PLAY_STOP_POS));
                    dbRecordInfo.setServiceType(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_SERVICE_TYPE));
                    dbRecordInfo.setFileSize(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_FILE_SIZE));
                    dbRecordInfo.setChName(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_CHNAME));
                    dbRecordInfo.setEventName(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_EVENT_NAME));
                    dbRecordInfo.setFullNamePath(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_FULLNAMEPATH));
                    dbRecordInfo.setSeriesKey(mDbUtils.GetByteArrayFromTable(cursor,PvrInfoDatabaseTable.SERIES_SERIES_KEY));
                    dbRecordInfo.setPlayTime(mDbUtils.GetLongFromTable(cursor, PvrInfoDatabaseTable.SERIES_PLAY_TIME));
                    dbRecordInfo.setVideoInfo(programInfo.getVideoInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_VIDEO_INFO_JSON)));
                    dbRecordInfo.setAudiosInfoList(programInfo.getAudioInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_AUDIO_INFO_JSON)));
                    dbRecordInfo.setSubtitleInfo(programInfo.getSubtitleInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_SUBTITLE_INFO_JSON)));
                    dbRecordInfo.setTeletextList(programInfo.getTeletextInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_TELETEXT_INFO_JSON)));
                    dbRecordInfo.setEpgInfo(epgEvent.getEPGEventFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_EPG_INFO_JSON)));
                    dbRecordInfo.setEpisode(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_EPISODE));
                    dvrDbRecordInfoList.add(dbRecordInfo);
                }
                while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying Series Table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return dvrDbRecordInfoList;
    }

    public ArrayList<PvrDbRecordInfo> getSeriesSuccessFilesInfoList(int masterIdx, int startIndex, int count){
        ArrayList<PvrDbRecordInfo> dvrDbRecordInfoList = new ArrayList<>();
        Cursor cursor = null;
        int ChLock=0,Adult=0,tmp=0;
        try {
            Uri CONTENT_SERIES_URI = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterIdx);
            String[] projection = {
                    PvrInfoDatabaseTable.SERIES_PRIMARY_ID, PvrInfoDatabaseTable.SERIES_CH_NUM, PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK, PvrInfoDatabaseTable.SERIES_PARENT_RATING, PvrInfoDatabaseTable.SERIES_RECORD_STATUS,
                    PvrInfoDatabaseTable.SERIES_CHANNEL_ID, PvrInfoDatabaseTable.SERIES_START_TIME, PvrInfoDatabaseTable.SERIES_DURATION, PvrInfoDatabaseTable.SERIES_PLAY_STOP_POS, PvrInfoDatabaseTable.SERIES_SERVICE_TYPE,
                    PvrInfoDatabaseTable.SERIES_FILE_SIZE, PvrInfoDatabaseTable.SERIES_CHNAME, PvrInfoDatabaseTable.SERIES_EVENT_NAME, PvrInfoDatabaseTable.SERIES_FULLNAMEPATH, PvrInfoDatabaseTable.SERIES_SERIES_KEY,
                    PvrInfoDatabaseTable.SERIES_PLAY_TIME, PvrInfoDatabaseTable.SERIES_VIDEO_INFO_JSON, PvrInfoDatabaseTable.SERIES_AUDIO_INFO_JSON, PvrInfoDatabaseTable.SERIES_SUBTITLE_INFO_JSON,
                    PvrInfoDatabaseTable.SERIES_TELETEXT_INFO_JSON, PvrInfoDatabaseTable.SERIES_EPG_INFO_JSON, PvrInfoDatabaseTable.SERIES_EPISODE
            };

            String selection = PvrInfoDatabaseTable.SERIES_RECORD_STATUS + " != ?";
            String[] selectionArgs = {"0"}; // SERIES_RECORD_STATUS != 0 means success record
            String orderBy = PvrInfoDatabaseTable.SERIES_PRIMARY_ID + " ASC";
            String limit = startIndex + ", " + count;
            cursor = resolver.query(CONTENT_SERIES_URI, projection, selection, selectionArgs, orderBy + " LIMIT " + limit);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ProgramInfo programInfo = new ProgramInfo();
                    EPGEvent epgEvent = new EPGEvent();
                    PvrDbRecordInfo dbRecordInfo = new PvrDbRecordInfo();

                    dbRecordInfo.setMasterIdx(masterIdx);
                    dbRecordInfo.setSeriesIdx(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_PRIMARY_ID));
                    dbRecordInfo.setChannelNo(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_CH_NUM));
                    //dbRecordInfo.setChannelLock(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK));
                    tmp = mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK);
                    ChLock = tmp&0xFF;
                    Adult = (tmp>>8) & 0xFF;
                    dbRecordInfo.setChannelLock(ChLock);
                    dbRecordInfo.setAdult(Adult);

                    dbRecordInfo.setRating(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_PARENT_RATING));
                    dbRecordInfo.setRecordStatus(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_RECORD_STATUS));
                    dbRecordInfo.setChannelId(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_CHANNEL_ID));
                    //dbRecordInfo.setStartTime(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_START_TIME));
                    dbRecordInfo.setStartTime(mDbUtils.GetLongFromTable(cursor, PvrInfoDatabaseTable.SERIES_START_TIME));
                    dbRecordInfo.setDurationSec(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_DURATION));
                    dbRecordInfo.setPlayStopPos(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_PLAY_STOP_POS));
                    dbRecordInfo.setServiceType(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_SERVICE_TYPE));
                    dbRecordInfo.setFileSize(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_FILE_SIZE));
                    dbRecordInfo.setChName(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_CHNAME));
                    dbRecordInfo.setEventName(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_EVENT_NAME));
                    dbRecordInfo.setFullNamePath(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_FULLNAMEPATH));
                    dbRecordInfo.setSeriesKey(mDbUtils.GetByteArrayFromTable(cursor,PvrInfoDatabaseTable.SERIES_SERIES_KEY));
                    dbRecordInfo.setPlayTime(mDbUtils.GetLongFromTable(cursor, PvrInfoDatabaseTable.SERIES_PLAY_TIME));
                    dbRecordInfo.setVideoInfo(programInfo.getVideoInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_VIDEO_INFO_JSON)));
                    dbRecordInfo.setAudiosInfoList(programInfo.getAudioInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_AUDIO_INFO_JSON)));
                    dbRecordInfo.setSubtitleInfo(programInfo.getSubtitleInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_SUBTITLE_INFO_JSON)));
                    dbRecordInfo.setTeletextList(programInfo.getTeletextInfoFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_TELETEXT_INFO_JSON)));
                    dbRecordInfo.setEpgInfo(epgEvent.getEPGEventFromJsonString(mDbUtils.GetStringFromTable(cursor,PvrInfoDatabaseTable.SERIES_EPG_INFO_JSON)));
                    dbRecordInfo.setEpisode(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_EPISODE));
                    dvrDbRecordInfoList.add(dbRecordInfo);
                }
                while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying Series Table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return dvrDbRecordInfoList;
    }

    public ArrayList<PvrDbRecordInfo> getSeriesErrorFilesInfoList(int masterIdx, int startIndex, int count){
        ArrayList<PvrDbRecordInfo> dvrDbRecordInfoList = new ArrayList<>();
        Cursor cursor = null;
        int ChLock=0,Adult=0,tmp=0;
        try {
            Uri CONTENT_SERIES_URI = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterIdx);
            String[] projection = {
                    PvrInfoDatabaseTable.SERIES_PRIMARY_ID, PvrInfoDatabaseTable.SERIES_CH_NUM, PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK, PvrInfoDatabaseTable.SERIES_PARENT_RATING, PvrInfoDatabaseTable.SERIES_RECORD_STATUS,
                    PvrInfoDatabaseTable.SERIES_CHANNEL_ID, PvrInfoDatabaseTable.SERIES_START_TIME, PvrInfoDatabaseTable.SERIES_DURATION, PvrInfoDatabaseTable.SERIES_PLAY_STOP_POS, PvrInfoDatabaseTable.SERIES_SERVICE_TYPE,
                    PvrInfoDatabaseTable.SERIES_FILE_SIZE, PvrInfoDatabaseTable.SERIES_CHNAME, PvrInfoDatabaseTable.SERIES_EVENT_NAME, PvrInfoDatabaseTable.SERIES_FULLNAMEPATH, PvrInfoDatabaseTable.SERIES_SERIES_KEY,
                    PvrInfoDatabaseTable.SERIES_PLAY_TIME, PvrInfoDatabaseTable.SERIES_VIDEO_INFO_JSON, PvrInfoDatabaseTable.SERIES_AUDIO_INFO_JSON, PvrInfoDatabaseTable.SERIES_SUBTITLE_INFO_JSON,
                    PvrInfoDatabaseTable.SERIES_TELETEXT_INFO_JSON, PvrInfoDatabaseTable.SERIES_EPG_INFO_JSON, PvrInfoDatabaseTable.SERIES_EPISODE
            };
            String selection = PvrInfoDatabaseTable.SERIES_RECORD_STATUS + " = ?";
            String[] selectionArgs = new String[]{"0"};
            String orderBy = PvrInfoDatabaseTable.SERIES_PRIMARY_ID + " ASC";
            String limit = startIndex + ", " + count;
            cursor = resolver.query(CONTENT_SERIES_URI, projection, selection, selectionArgs, orderBy + " LIMIT " + limit);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ProgramInfo programInfo = new ProgramInfo();
                    EPGEvent epgEvent = new EPGEvent();
                    PvrDbRecordInfo dbRecordInfo = new PvrDbRecordInfo();
                    dbRecordInfo.setMasterIdx(masterIdx);
                    dbRecordInfo.setSeriesIdx(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_PRIMARY_ID));
                    dbRecordInfo.setChannelNo(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_CH_NUM));
                    //dbRecordInfo.setChannelLock(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK));
                    tmp = mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.SERIES_CHANNEL_LOCK);
                    ChLock = tmp&0xFF;
                    Adult = (tmp>>8) & 0xFF;
                    dbRecordInfo.setChannelLock(ChLock);
                    dbRecordInfo.setAdult(Adult);

                    dbRecordInfo.setRating(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_PARENT_RATING));
                    dbRecordInfo.setRecordStatus(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_RECORD_STATUS));
                    dbRecordInfo.setChannelId(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_CHANNEL_ID));
                    //dbRecordInfo.setStartTime(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_START_TIME));
                    dbRecordInfo.setStartTime(mDbUtils.GetLongFromTable(cursor, PvrInfoDatabaseTable.SERIES_START_TIME));
                    dbRecordInfo.setDurationSec(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_DURATION));
                    dbRecordInfo.setPlayStopPos(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_PLAY_STOP_POS));
                    dbRecordInfo.setServiceType(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_SERVICE_TYPE));
                    dbRecordInfo.setFileSize(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_FILE_SIZE));
                    dbRecordInfo.setChName(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_CHNAME));
                    dbRecordInfo.setEventName(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_EVENT_NAME));
                    dbRecordInfo.setFullNamePath(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_FULLNAMEPATH));
                    dbRecordInfo.setSeriesKey(mDbUtils.GetByteArrayFromTable(cursor,PvrInfoDatabaseTable.SERIES_SERIES_KEY));
                    dbRecordInfo.setPlayTime(mDbUtils.GetLongFromTable(cursor, PvrInfoDatabaseTable.SERIES_PLAY_TIME));
                    dbRecordInfo.setVideoInfo(programInfo.getVideoInfoFromJsonString(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_VIDEO_INFO_JSON)));
                    dbRecordInfo.setAudiosInfoList(programInfo.getAudioInfoFromJsonString(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_AUDIO_INFO_JSON)));
                    dbRecordInfo.setSubtitleInfo(programInfo.getSubtitleInfoFromJsonString(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_SUBTITLE_INFO_JSON)));
                    dbRecordInfo.setTeletextList(programInfo.getTeletextInfoFromJsonString(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_TELETEXT_INFO_JSON)));
                    dbRecordInfo.setEpgInfo(epgEvent.getEPGEventFromJsonString(mDbUtils.GetStringFromTable(cursor, PvrInfoDatabaseTable.SERIES_EPG_INFO_JSON)));
                    dbRecordInfo.setEpisode(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_EPISODE));
                    dvrDbRecordInfoList.add(dbRecordInfo);
                }
                while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying Series Table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return dvrDbRecordInfoList;
    }

    //Get total number of Master table due to condition
    public int getTotalCountCondition(int dbIdx, int value){
        int count = 0;
        String masterType = PvrInfoDatabaseTable.ColumnsMasterType[dbIdx];
        Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();

        String selection = masterType + " = ?";
        String[] selectionArgs = { "value" };
        Cursor cursor = resolver.query(
                uri,
                new String[]{"COUNT(*) AS count"},
                selection,
                selectionArgs,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            count = mDbUtils.GetIntFromTable(cursor, "count");
            cursor.close();
        }
        return count;
    }

    public int deleteAllRecordFiles(){
        int deleteCount = 0;
        Cursor cursor = null;

        try {
            Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
            String[] projection = {
                    PvrInfoDatabaseTable.PRIMARY_ID,
                    PvrInfoDatabaseTable.SERIES
            };
            cursor = resolver.query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int masterIdx = mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.PRIMARY_ID);
                    int isSeries = mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.SERIES);

                    if (isSeries == 1) {
                        deleteCount += deleteSeriesRecordFolder(masterIdx);
                    } else {
                        deleteCount += deleteRecordFile(new PvrRecIdx(masterIdx, 0xFFFF));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting all records", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return deleteCount;
    }

    //delete Series id from Master table && delete Series Table
    public int deleteSeriesRecordFolder(int masterIdx){
        int deleteCount = 0;
        Cursor seriesCursor = null;
        PvrRecIdx pvrRecIdx = new PvrRecIdx(-1,-1);
        try {
            Uri series_uri = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterIdx);
            String[] projection = {
                    PvrInfoDatabaseTable.SERIES_PRIMARY_ID
            };
            seriesCursor = resolver.query(series_uri, projection, null, null, null);
            if (seriesCursor != null && seriesCursor.moveToFirst()) {
                do {
                    int seriesIdx = mDbUtils.GetIntFromTable(seriesCursor, PvrInfoDatabaseTable.SERIES_PRIMARY_ID);
                    String dirPath_series = "/"+PvrDatabase.RECORD_FOLDER_NAME + "_" + masterIdx + "_" + seriesIdx;
                    String filePath = usbPath+dirPath_series;
                    deleteFileOrDirectory(filePath);
                    Log.d(TAG, "Deleted Series Folder: " + filePath);
                    //delete playback list
                    pvrRecIdx.setMasterIdx(masterIdx);
                    pvrRecIdx.setSeriesIdx(seriesIdx);
                    deleteOnePlaybackInList(pvrRecIdx);
                    deleteCount++;
                } while (seriesCursor.moveToNext());
                seriesCursor.close();
            }

            Uri dropSeriesUri = Uri.parse(PvrContentProvider.get_CONTENT_STRING() + PvrDatabase.DROP_SERIES_TABLE + "/" +masterIdx);
            resolver.delete(dropSeriesUri, null, null);

            // also remove correspond series item in master table
            Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
            String selection = PvrInfoDatabaseTable.PRIMARY_ID + " = ?";
            String[] selectionArgs = { String.valueOf(masterIdx) };
            resolver.delete(uri, selection, selectionArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting series record folder for masterIdx: " + masterIdx, e);
        } finally {
            if (seriesCursor != null) {
                seriesCursor.close();
            }
        }
        return deleteCount;
    }

    private int deleteOneSeriesRecord(PvrRecIdx pvrRecIdx) {
        if (pvrRecIdx == null) {
            return 0;
        }

        int deleteCount = 0;
        int masterIdx = pvrRecIdx.getMasterIdx();
        int seriesIdx = pvrRecIdx.getSeriesIdx();
        Uri uriSeries = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterIdx);
        String selectionSeries = PvrInfoDatabaseTable.SERIES_PRIMARY_ID + " = ?";
        String[] selectionArgsSeries = { String.valueOf(seriesIdx) };
        int deleted = resolver.delete(uriSeries, selectionSeries, selectionArgsSeries);
        deleteCount += deleted;
        Log.d(TAG, "Deleted Record Master ID: " + masterIdx);
        Log.d(TAG, "Deleted Record Series ID: " + seriesIdx);
        String dirPath = "/" + PvrDatabase.RECORD_FOLDER_NAME + "_" + masterIdx + "_" + seriesIdx;
        String filePath = usbPath + dirPath;
        deleteFileOrDirectory(filePath);
        Log.d(TAG, "Deleted File: " + filePath);
        //delete playback list
        deleteOnePlaybackInList(pvrRecIdx);

        // also drop series table and remove correspond series item in master table
        // if all series records of masterIdx are removed
        if (getSeriesRecordTotalCount(masterIdx) == 0) {
            Uri dropSeriesUri = Uri.parse(
                    PvrContentProvider.get_CONTENT_STRING()
                            + PvrDatabase.DROP_SERIES_TABLE
                            + "/"
                            + masterIdx);
            resolver.delete(dropSeriesUri, null, null);

            Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
            String selection = PvrInfoDatabaseTable.PRIMARY_ID + " = ?";
            String[] selectionArgs = { String.valueOf(masterIdx) };
            resolver.delete(uri, selection, selectionArgs);
        }
        return deleteCount;
    }

    public int deleteRecordFile(PvrRecIdx pvrRecIdx){
        int deleteCount = 0;
        if (pvrRecIdx == null) {
            Log.e(TAG, "Invalid PvrRecIdx: null");
            return deleteCount;
        }

        int masterIdx = pvrRecIdx.getMasterIdx();
        int seriesIdx = pvrRecIdx.getSeriesIdx();

        if (seriesIdx == 0xffff || seriesIdx == -1) {

            Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
            String selection = PvrInfoDatabaseTable.PRIMARY_ID + " = ?";
            String[] selectionArgs = { String.valueOf(masterIdx) };
            int deleted = resolver.delete(uri, selection, selectionArgs);
            deleteCount += deleted;
            Log.d(TAG, "Deleted Master Record ID: " + masterIdx);
            String dirPath =  "/"+PvrDatabase.RECORD_FOLDER_NAME + "_" + masterIdx;
            String filePath = usbPath + dirPath;
            deleteFileOrDirectory(filePath);
            Log.d(TAG, "Deleted File: " + filePath);
            //delete playback list
            deleteOnePlaybackInList(pvrRecIdx);
        } else {
            deleteCount = deleteOneSeriesRecord(pvrRecIdx);
        }

        return deleteCount;
    }
    public int deleteOnePlaybackInList(PvrRecIdx pvrRecIdx){
        int deleteCount = 0;
        if (pvrRecIdx == null) {
            Log.e(TAG, "Invalid PvrRecIdx: null");
            return deleteCount;
        }
        int masterIdx = pvrRecIdx.getMasterIdx();
        int seriesIdx = pvrRecIdx.getSeriesIdx();
        Uri playback_uri = PvrContentProvider.get_CONTENT_PLAYBACK_URI();
        String playback_selection = PvrInfoDatabaseTable.MASTER_ID + " = ? AND " + PvrInfoDatabaseTable.SERIES_ID + " = ?";
        String[] playback_selectionArgs = {
                String.valueOf(masterIdx),
                String.valueOf(seriesIdx)
        };
        int deleted = resolver.delete(playback_uri, playback_selection, playback_selectionArgs);
        deleteCount += deleted;
        return deleteCount;
    }
    //delete program (channel_id) form db
    public int deleteRecordFilesByChannelId(int channelId){
        int deleteCount = 0;
        PvrRecIdx pvrRecIdx = new PvrRecIdx(-1,-1);
        Cursor masterCursor = null;
        try {

            Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
            String[] projection = { PvrInfoDatabaseTable.PRIMARY_ID, PvrInfoDatabaseTable.SERIES };
            String selection = PvrInfoDatabaseTable.CHANNEL_ID + " = ?";
            String[] selectionArgs = { String.valueOf(channelId) };
            masterCursor = resolver.query(uri, projection, selection, selectionArgs, null);

            if (masterCursor != null) {
                while (masterCursor.moveToNext()) {
                    int masterIdx = mDbUtils.GetIntFromTable(masterCursor, PvrInfoDatabaseTable.PRIMARY_ID);
                    int isSeries = mDbUtils.GetIntFromTable(masterCursor, PvrInfoDatabaseTable.SERIES);

                    if (isSeries == 1) {
                        Cursor seriesCursor = null;
                        Uri seriesUri = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterIdx);
                        String seriesSelection = PvrInfoDatabaseTable.CHANNEL_ID + " = ?";
                        int seriesDeleteCount = resolver.delete(seriesUri, seriesSelection, selectionArgs);
                        deleteCount += seriesDeleteCount;

                        String[] projection_series = {
                                PvrInfoDatabaseTable.SERIES_PRIMARY_ID
                        };
                        seriesCursor = resolver.query(seriesUri, projection_series, null, null, null);
                        if (seriesCursor != null && seriesCursor.moveToFirst()) {
                            do {
                                int seriesIdx = mDbUtils.GetIntFromTable(seriesCursor, PvrInfoDatabaseTable.SERIES_PRIMARY_ID);
                                //delete playback list
                                pvrRecIdx.setMasterIdx(masterIdx);
                                pvrRecIdx.setSeriesIdx(seriesIdx);
                                deleteOnePlaybackInList(pvrRecIdx);
                            } while (seriesCursor.moveToNext());
                            seriesCursor.close();
                    }
                        Uri dropSeriesUri = Uri.parse(PvrContentProvider.get_CONTENT_STRING() + PvrDatabase.DROP_SERIES_TABLE +"/" +masterIdx);
                        resolver.delete(dropSeriesUri, null, null);

                    }
                    Uri masterDeleteUri = Uri.withAppendedPath(uri, String.valueOf(masterIdx));
                    int masterDeleteCount = resolver.delete(masterDeleteUri, null, null);
                    deleteCount += masterDeleteCount;
                    //delete playback list
                    pvrRecIdx.setMasterIdx(masterIdx);
                    pvrRecIdx.setSeriesIdx(0xffff);
                    deleteOnePlaybackInList(pvrRecIdx);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting records by channelId", e);
        } finally {
            if (masterCursor != null) {
                masterCursor.close();
            }
        }

        return deleteCount;
    }
    public int modifyPlaybacktime(PvrRecIdx pvrRecIdx, int playbackTime){
        if (pvrRecIdx == null) {
            Log.e(TAG, "Invalid PvrRecIdx: null");
            return -1;
        }
        updatePlaybackTable(PvrInfoDatabaseTable.PLAYBACK_TIME, playbackTime, pvrRecIdx.getMasterIdx(), pvrRecIdx.getSeriesIdx());
        return 0;
    }
    public int modifyRecordPlaytime(PvrRecIdx pvrRecIdx, int playTime){
        if (pvrRecIdx == null) {
            Log.e(TAG, "Invalid PvrRecIdx: null");
            return -1;
        }
        if (pvrRecIdx.getSeriesIdx() == 0xffff) {
            // update recordStatus in master
            updateMasterRecord(PvrInfoDatabaseTable.PLAY_TIME, playTime, pvrRecIdx.getMasterIdx());
        } else {
            updateSeriesRecord(PvrInfoDatabaseTable.PLAY_TIME, playTime, pvrRecIdx);
        }
        return 0;
    }
    //Find out if a certain episode of a TV series has been recorded and is complete
    public int querySeriesEpisode(byte[] seriesKey, int episode){
        int find_episode = -1;
        int masterIdx = getMasterIdxBasedOnSeriesKey(seriesKey);
        if(masterIdx!=-1)
        {
            Cursor cursor = null;
            try {
                Uri CONTENT_SERIES_URI = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterIdx);
                String[] projection = {
                        PvrInfoDatabaseTable.SERIES_PRIMARY_ID
                };
                String selection = PvrInfoDatabaseTable.SERIES_EPISODE
                        + " = ? AND "
                        + PvrInfoDatabaseTable.SERIES_RECORD_STATUS
                        + " = ?";
                String[] selectionArgs = {
                        String.valueOf(episode),
                        "1" // SERIES_RECORD_STATUS = 1 means success record and complete
                };
                cursor = resolver.query(CONTENT_SERIES_URI, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    find_episode = 0;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error querying Series Table", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return find_episode;
    }

    //Find out if this series has been recorded
    public boolean checkSeriesComplete(byte[] seriesKey) {
        boolean seriesComplete = false;
        int totalEpisode = 0;
        int masterIdx = getMasterIdxBasedOnSeriesKey(seriesKey);
        if(masterIdx!=-1) {
            Cursor cursor = null;
            try {
                Uri uri = PvrContentProvider.get_CONTENT_MASTER_URI();
                String[] projection = {
                        PvrInfoDatabaseTable.TOTAL_EPISODES
                };
                String selection = PvrInfoDatabaseTable.PRIMARY_ID + " = ?";
                String[] selectionArgs = {String.valueOf(masterIdx)};
                cursor = resolver.query(uri, projection, selection, selectionArgs, null);

                if (cursor != null && cursor.moveToFirst()) {
                    totalEpisode = mDbUtils.GetIntFromTable(cursor,PvrInfoDatabaseTable.TOTAL_EPISODES);
                }

            } catch(Exception e){
                Log.e(TAG, "Error querying Master Table", e);
            } finally{
                if (cursor != null) {
                    cursor.close();
                }
            }

            Log.d(TAG, "checkSeriesComplete: totalEpisode = " + totalEpisode);
            if (totalEpisode != 0) {
                int completedEpisodeCount = getSeriesSuccessCompleteEpisodeCount(masterIdx);
                Log.d(TAG, "checkSeriesComplete: completedEpisodeCount = " + completedEpisodeCount);
                // completedEpisodeCount should == totalEpisode but use >= for safety
                if (completedEpisodeCount >= totalEpisode) {
                    seriesComplete = true;
                }
            }
        }

        return seriesComplete;
    }

    private int getSeriesSuccessCompleteEpisodeCount(int masterIdx) {
        Set<Integer> episodeSet = new HashSet<>();
        Cursor cursor = null;

        try {
            Uri CONTENT_SERIES_URI = Uri.parse(PvrContentProvider.get_CONTENT_SERIES_STRING() + masterIdx);
            String[] projection = {
                    PvrInfoDatabaseTable.SERIES_EPISODE
            };

            String selection = PvrInfoDatabaseTable.SERIES_RECORD_STATUS + " = ?";
            String[] selectionArgs = {"1"}; // SERIES_RECORD_STATUS = 1 means success and complete record
            cursor = resolver.query(CONTENT_SERIES_URI, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // episode may duplicate so use Set<>
                    episodeSet.add(mDbUtils.GetIntFromTable(cursor, PvrInfoDatabaseTable.SERIES_EPISODE));
                }
                while (cursor.moveToNext());
                }
        } catch (Exception e) {
            Log.e(TAG, "Error querying Series Table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return episodeSet.size();
    }
}
