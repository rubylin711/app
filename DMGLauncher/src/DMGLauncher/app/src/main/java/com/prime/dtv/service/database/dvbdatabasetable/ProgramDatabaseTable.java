package com.prime.dtv.service.database.dvbdatabasetable;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.prime.dtv.database.DBUtils;
import com.prime.dtv.service.database.DVBContentProvider;
import com.prime.dtv.service.database.DVBDatabase;
import com.prime.dtv.sysdata.ProgramInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProgramDatabaseTable {
    private static final String TAG = "ProgramDatabaseTable";
    public static final String TABLE_NAME = "Program";

    public static final Uri CONTENT_URI = Uri.parse("content://" + DVBContentProvider.AUTHORITY + "/" + TABLE_NAME);

    //Type
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.program";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.program";

    private Context mContext;
    DBUtils mDbUtils = new DBUtils();

    public ProgramDatabaseTable(Context context) {
        mContext = context;
    }


    public static String TableCreate() {
        String cmd = "create table "
                + ProgramDatabaseTable.TABLE_NAME + " ("
                + "_ID" + " integer primary key autoincrement,"
                + ProgramInfo.CHANNEL_ID + " integer UNIQUE,"
                + ProgramInfo.SERVICE_ID + ","
                + ProgramInfo.TYPE + ","
                + ProgramInfo.DISPLAY_NUM + ","
                + ProgramInfo.LOGICAL_CHANNEL_NUMBER + ","
                + ProgramInfo.DISPLAY_NAME + ","
                + ProgramInfo.LOCK + ","
                + ProgramInfo.SKIP + ","
                + ProgramInfo.CA_FLAG + ","
                + ProgramInfo.TP_ID + ","
                + ProgramInfo.SAT_ID + ","
                + ProgramInfo.PCR + ","
                + ProgramInfo.TRANSPORT_STREAM_ID + ","
                + ProgramInfo.ORIGINAL_NETWORK_ID + ","
                + ProgramInfo.AUDIO_LR_SELECTED + ","
                + ProgramInfo.AUDIO_SELECTED + ","
                + ProgramInfo.TUNER_ID + ","
                + ProgramInfo.PARENTAL_RATING + ","
                + ProgramInfo.PARENTAL_COUNTRY_CODE + ","
                + ProgramInfo.VIDEO_INFO_JSON + ","
                + ProgramInfo.AUDIO_INFO_JSON + ","
                + ProgramInfo.SUBTITLE_INFO_JSON + ","
                + ProgramInfo.TELETEXT_INFO_JSON + ","
                + ProgramInfo.PMT_PID + ","
                + ProgramInfo.PMT_VERSION + ","
                + ProgramInfo.TIME_LOCK_FLAG + ","
                + ProgramInfo.CATEGORY_TYPE
                + ");";
        return cmd;
    }

    public static String TableDrop() {
        String cmd = "DROP TABLE IF EXISTS " + ProgramDatabaseTable.TABLE_NAME + ";";
        return cmd;
    }

    public void save(ProgramInfo programInfo) {
        if (programInfo != null) {
            update(programInfo);
        }
    }

    private void deleteNotInDB(List<ProgramInfo> programInfoList) {
        List<DVBDatabase.KeyTuple> keepKeys = programInfoList.stream()
                .map(p -> new DVBDatabase.KeyTuple(p.getChannelId()))
                .collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < programInfoList.size(); i++) {
            sb.append("?");
            if (i < programInfoList.size() - 1) sb.append(",");
        }
        String sql = "DELETE FROM "+TABLE_NAME+" WHERE "+ProgramInfo.CHANNEL_ID+" NOT IN (" + sb + ")";

        DVBContentProvider.getDvbDb().deleteNotInDb(TABLE_NAME,sql,keepKeys);
    }

    public static final DVBDatabase.SQLiteBinder<ProgramInfo> BINDER = (stmt, programInfo) -> {
        int i = 1;
        stmt.bindLong(i++, programInfo.getChannelId());
        stmt.bindLong(i++, programInfo.getServiceId());
        stmt.bindLong(i++, programInfo.getType());
        stmt.bindLong(i++, programInfo.getDisplayNum());
        stmt.bindLong(i++, programInfo.getLCN());
        stmt.bindString(i++, programInfo.getDisplayName());
        stmt.bindLong(i++, programInfo.getLock());
        stmt.bindLong(i++, programInfo.getSkip());
        stmt.bindLong(i++, programInfo.getCA());
        stmt.bindLong(i++, programInfo.getTpId());
        stmt.bindLong(i++, programInfo.getSatId());
        stmt.bindLong(i++, programInfo.getPcr());
        stmt.bindLong(i++, programInfo.getTransportStreamId());
        stmt.bindLong(i++, programInfo.getOriginalNetworkId());
        stmt.bindLong(i++, programInfo.getAudioLRSelected());
        stmt.bindLong(i++, programInfo.getAudioSelected());
        stmt.bindLong(i++, programInfo.getTunerId());
        stmt.bindLong(i++, programInfo.getParentalRating());
        if (programInfo.getParentCountryCode() != null)
            stmt.bindString(i++, programInfo.getParentCountryCode());
        else
            stmt.bindNull(i++);
        stmt.bindString(i++, programInfo.getJsonStringFromVideoInfo());
        stmt.bindString(i++, programInfo.getJsonStringFromAudioInfo());
        stmt.bindString(i++, programInfo.getJsonStringFromSubtitleInfo());
        stmt.bindString(i++, programInfo.getJsonStringFromTeletextInfo());
        stmt.bindLong(i++, programInfo.getPmtPid());
        stmt.bindLong(i++, programInfo.getPmtversion());
        stmt.bindLong(i++, programInfo.getTimeLockFlag(3));
        stmt.bindLong(i++, programInfo.getCategory_type());
    };

    public void save(List<ProgramInfo> programInfoList) {
//        long t0 = System.currentTimeMillis();
        String SQL = "INSERT OR REPLACE INTO " + TABLE_NAME + " (" +
                ProgramInfo.CHANNEL_ID + ", " + ProgramInfo.SERVICE_ID + ", " + ProgramInfo.TYPE + ", " + ProgramInfo.DISPLAY_NUM + ", " +
                ProgramInfo.LOGICAL_CHANNEL_NUMBER + ", " + ProgramInfo.DISPLAY_NAME + ", " + ProgramInfo.LOCK + ", " + ProgramInfo.SKIP + ", " +
                ProgramInfo.CA_FLAG + ", " + ProgramInfo.TP_ID + ", " + ProgramInfo.SAT_ID + ", " + ProgramInfo.PCR + ", " +
                ProgramInfo.TRANSPORT_STREAM_ID + ", " + ProgramInfo.ORIGINAL_NETWORK_ID + ", " + ProgramInfo.AUDIO_LR_SELECTED + ", " + ProgramInfo.AUDIO_SELECTED + ", " +
                ProgramInfo.TUNER_ID + ", " + ProgramInfo.PARENTAL_RATING + ", " + ProgramInfo.PARENTAL_COUNTRY_CODE + ", " + ProgramInfo.VIDEO_INFO_JSON + ", " +
                ProgramInfo.AUDIO_INFO_JSON + ", " + ProgramInfo.SUBTITLE_INFO_JSON + ", " + ProgramInfo.TELETEXT_INFO_JSON + ", " + ProgramInfo.PMT_PID + ", " +
                ProgramInfo.PMT_VERSION + ", " + ProgramInfo.TIME_LOCK_FLAG + ", " + ProgramInfo.CATEGORY_TYPE +
                ") VALUES (?,?,?,?, ?,?,?,?, ?,?,?,?, ?,?,?,?, ?,?,?,?, ?,?,?,?, ?,?,?);";
        deleteNotInDB(programInfoList);
//        long t1 = System.currentTimeMillis();
        DVBContentProvider.getDvbDb().saveListWithInsert(TABLE_NAME,programInfoList,SQL,BINDER);
//        long t2 = System.currentTimeMillis();
//        Log.d("qqqq", "program list delete time = " + (t1 - t0) + " ms, all = " + (t2 - t0) + " ms");
    }

    public List<ProgramInfo> load() {
        List<ProgramInfo> programInfoList = queryList();
        return programInfoList;
    }

    private ContentValues getProgramInfoContentValues(ProgramInfo programInfo) {
        ContentValues Value = new ContentValues();
        Value.put(ProgramInfo.CHANNEL_ID, programInfo.getChannelId());
        Value.put(ProgramInfo.SERVICE_ID, programInfo.getServiceId());
        Value.put(ProgramInfo.TYPE, programInfo.getType());
        Value.put(ProgramInfo.DISPLAY_NUM, programInfo.getDisplayNum());
        Value.put(ProgramInfo.LOGICAL_CHANNEL_NUMBER, programInfo.getLCN());
        Value.put(ProgramInfo.DISPLAY_NAME, programInfo.getDisplayName());
        Value.put(ProgramInfo.LOCK, programInfo.getLock());
        Value.put(ProgramInfo.SKIP, programInfo.getSkip());
        Value.put(ProgramInfo.CA_FLAG, programInfo.getCA());
        Value.put(ProgramInfo.TP_ID, programInfo.getTpId());
        Value.put(ProgramInfo.SAT_ID, programInfo.getSatId());
        Value.put(ProgramInfo.PCR, programInfo.getPcr());
        Value.put(ProgramInfo.TRANSPORT_STREAM_ID, programInfo.getTransportStreamId());
        Value.put(ProgramInfo.ORIGINAL_NETWORK_ID, programInfo.getOriginalNetworkId());
        Value.put(ProgramInfo.AUDIO_LR_SELECTED, programInfo.getAudioLRSelected());
        Value.put(ProgramInfo.AUDIO_SELECTED, programInfo.getAudioSelected());
        Value.put(ProgramInfo.TUNER_ID, programInfo.getTunerId());
        Value.put(ProgramInfo.PARENTAL_RATING, programInfo.getParentalRating());
        Value.put(ProgramInfo.PARENTAL_COUNTRY_CODE, programInfo.getParentCountryCode());
        Value.put(ProgramInfo.VIDEO_INFO_JSON, programInfo.getJsonStringFromVideoInfo());
        Value.put(ProgramInfo.AUDIO_INFO_JSON, programInfo.getJsonStringFromAudioInfo());
        Value.put(ProgramInfo.SUBTITLE_INFO_JSON, programInfo.getJsonStringFromSubtitleInfo());
        Value.put(ProgramInfo.TELETEXT_INFO_JSON, programInfo.getJsonStringFromTeletextInfo());
        Value.put(ProgramInfo.PMT_PID, programInfo.getPmtPid());
        Value.put(ProgramInfo.PMT_VERSION, programInfo.getPmtversion());
        Value.put(ProgramInfo.TIME_LOCK_FLAG, programInfo.getTimeLockFlag(3));
        Value.put(ProgramInfo.CATEGORY_TYPE, programInfo.getCategory_type());
        //Log.d(TAG, "getProgramInfoContentValues "+ programInfo.ToString());
        return Value;
    }

    private void add(ProgramInfo programInfo) {
        mContext.getContentResolver().insert(ProgramDatabaseTable.CONTENT_URI, getProgramInfoContentValues(programInfo));
    }

    private void update(ProgramInfo programInfo) {
        int count;
        String selection = ProgramInfo.CHANNEL_ID + " = " + programInfo.getChannelId();
        Cursor cursor = mContext.getContentResolver().query(ProgramDatabaseTable.CONTENT_URI, null, selection, null, null);
        Log.d(TAG,"query cursor = "+cursor);
        String where = ProgramInfo.CHANNEL_ID + " = " + programInfo.getChannelId();
        ContentValues Value = new ContentValues();
//        Value.put(ProgramInfo.CHANNEL_ID, programInfo.getChannelId());
        Value.put(ProgramInfo.SERVICE_ID, programInfo.getServiceId());
        Value.put(ProgramInfo.TYPE, programInfo.getType());
        Value.put(ProgramInfo.DISPLAY_NUM, programInfo.getDisplayNum());
        Value.put(ProgramInfo.LOGICAL_CHANNEL_NUMBER, programInfo.getLCN());
        Value.put(ProgramInfo.DISPLAY_NAME, programInfo.getDisplayName());
        Value.put(ProgramInfo.LOCK, programInfo.getLock());
        Value.put(ProgramInfo.SKIP, programInfo.getSkip());
        Value.put(ProgramInfo.CA_FLAG, programInfo.getCA());
        Value.put(ProgramInfo.TP_ID, programInfo.getTpId());
        Value.put(ProgramInfo.SAT_ID, programInfo.getSatId());
        Value.put(ProgramInfo.PCR, programInfo.getPcr());
        Value.put(ProgramInfo.TRANSPORT_STREAM_ID, programInfo.getTransportStreamId());
        Value.put(ProgramInfo.ORIGINAL_NETWORK_ID, programInfo.getOriginalNetworkId());
        Value.put(ProgramInfo.AUDIO_LR_SELECTED, programInfo.getAudioLRSelected());
        Value.put(ProgramInfo.AUDIO_SELECTED, programInfo.getAudioSelected());
        Value.put(ProgramInfo.TUNER_ID, programInfo.getTunerId());
        Value.put(ProgramInfo.PARENTAL_RATING, programInfo.getParentalRating());
        Value.put(ProgramInfo.PARENTAL_COUNTRY_CODE, programInfo.getParentCountryCode());
        Value.put(ProgramInfo.VIDEO_INFO_JSON, programInfo.getJsonStringFromVideoInfo());
        Value.put(ProgramInfo.AUDIO_INFO_JSON, programInfo.getJsonStringFromAudioInfo());
        Value.put(ProgramInfo.SUBTITLE_INFO_JSON, programInfo.getJsonStringFromSubtitleInfo());
        Value.put(ProgramInfo.TELETEXT_INFO_JSON, programInfo.getJsonStringFromTeletextInfo());
        Value.put(ProgramInfo.PMT_PID, programInfo.getPmtPid());
        Value.put(ProgramInfo.PMT_VERSION, programInfo.getPmtversion());
        Value.put(ProgramInfo.TIME_LOCK_FLAG, programInfo.getTimeLockFlag(3));
        Value.put(ProgramInfo.CATEGORY_TYPE, programInfo.getCategory_type());
        //Log.d(TAG, "update " + programInfo.ToString());
        if(cursor != null) {
            mContext.getContentResolver().update(ProgramDatabaseTable.CONTENT_URI, Value, where, null);
        }else{
            mContext.getContentResolver().insert(ProgramDatabaseTable.CONTENT_URI, Value);
        }
    }

    public int removeAll() {
        int count;
        Log.d(TAG, "removeAll programInfolist");
        count = mContext.getContentResolver().delete(ProgramDatabaseTable.CONTENT_URI,null,null);

        return count;
    }

    public void remove(ProgramInfo programInfo) {
        int count;
        String where = ProgramInfo.CHANNEL_ID + " = " + programInfo.getChannelId();
        Log.d(TAG, "remove programInfo.getChannelId() "+ programInfo.getChannelId());
        count = mContext.getContentResolver().delete(ProgramDatabaseTable.CONTENT_URI, where, null);
    }

    private List<ProgramInfo> queryList() {
        Cursor cursor = mContext.getContentResolver().query(ProgramDatabaseTable.CONTENT_URI, null, null, null, null);
        List<ProgramInfo> programInfoList = new ArrayList<>();
        ProgramInfo programInfo = null;
        Log.d(TAG, "ProgramInfo queryList ");
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    programInfo = ParseCursor(cursor);
                    programInfoList.add(programInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        Log.d(TAG, "programInfoList = "+programInfoList);
        int i;
        for(i = 0; i < programInfoList.size(); i++) {
            Log.d(TAG,"["+i+"] "+programInfoList.get(i).getDisplayName()+" channel id = "+programInfoList.get(i).getChannelId());
        }
        // return empty list instead of null
        return programInfoList;
    }

//    private ArrayList<ProgramInfo> queryList() {
//        String selection = DtvChannelTable.RECOMMEND_NAME + " = " + recommendTitle;
//        Cursor cursor = mContext.getContentResolver().query(DtvChannelTable.CONTENT_URI, null, selection, null, null);
//        ArrayList<Channel> ChannelList = new ArrayList<Channel>();
//        Channel channel = null;
//        if(cursor != null) {
//            if (cursor.moveToFirst()) {
//                do {
//                    channel = ParseCursor(cursor);
//                    ChannelList.add(channel);
//                } while (cursor.moveToNext());
//            }
//            cursor.close();
//            return ChannelList;
//        }
//        return null;
//    }

    private ProgramInfo query(long channelId) {
        ProgramInfo programInfo = null;
        String selection = ProgramInfo.CHANNEL_ID + " = " + channelId;
        Cursor cursor = mContext.getContentResolver().query(ProgramDatabaseTable.CONTENT_URI, null, selection, null, null);
        Log.d(TAG,"query cursor = "+cursor);
        if(cursor != null) {
            if (cursor.moveToFirst())
                programInfo = ParseCursor(cursor);
            cursor.close();
            return programInfo;
        }
        return null;
    }

    private ProgramInfo ParseCursor(Cursor cursor){
        ProgramInfo programInfo = new ProgramInfo();
        programInfo.setChannelId(mDbUtils.GetLongFromTable(TABLE_NAME,cursor, ProgramInfo.CHANNEL_ID));
        programInfo.setServiceId(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.SERVICE_ID));
        programInfo.setType(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.TYPE));
        programInfo.setDisplayNum(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.DISPLAY_NUM));
        programInfo.setLCN(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.LOGICAL_CHANNEL_NUMBER));
        programInfo.setDisplayName(mDbUtils.GetStringFromTable(TABLE_NAME,cursor, ProgramInfo.DISPLAY_NAME));
        programInfo.setLock(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.LOCK));
        programInfo.setSkip(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.SKIP));
        programInfo.setCA(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.CA_FLAG));
        programInfo.setTpId(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.TP_ID));
        programInfo.setSatId(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.SAT_ID));
        programInfo.setPcr(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.PCR));
        programInfo.setTransportStreamId(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.TRANSPORT_STREAM_ID));
        programInfo.setOriginalNetworkId(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.ORIGINAL_NETWORK_ID));
        programInfo.setAudioLRSelected(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.AUDIO_LR_SELECTED));
        programInfo.setAudioSelected(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.AUDIO_SELECTED));
        programInfo.setTunerId(/*mDbUtils.GetIntFromTable(TABLE_NAME, cursor, ProgramInfo.TUNER_ID)*/0);
        programInfo.setParentalRating(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.PARENTAL_RATING));
        programInfo.setParentCountryCode(mDbUtils.GetStringFromTable(TABLE_NAME,cursor, ProgramInfo.PARENTAL_COUNTRY_CODE));
        programInfo.pVideo = programInfo.getVideoInfoFromJsonString(mDbUtils.GetStringFromTable(TABLE_NAME,cursor, ProgramInfo.VIDEO_INFO_JSON));
        programInfo.pAudios = programInfo.getAudioInfoFromJsonString(mDbUtils.GetStringFromTable(TABLE_NAME,cursor, ProgramInfo.AUDIO_INFO_JSON));
        programInfo.pSubtitle = programInfo.getSubtitleInfoFromJsonString(mDbUtils.GetStringFromTable(TABLE_NAME,cursor, ProgramInfo.SUBTITLE_INFO_JSON));
        programInfo.pTeletext = programInfo.getTeletextInfoFromJsonString(mDbUtils.GetStringFromTable(TABLE_NAME,cursor, ProgramInfo.TELETEXT_INFO_JSON));
        programInfo.setPmtPid(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.PMT_PID));
        programInfo.setPmtVersion(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.PMT_VERSION));
        programInfo.setTimeLockFlag(3, mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.TIME_LOCK_FLAG));
        programInfo.setCategory_type(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, ProgramInfo.CATEGORY_TYPE));
        Log.i(TAG, "mDbUtils.GetIntFromTable(cursor, _ID) = "+mDbUtils.GetIntFromTable(TABLE_NAME, cursor, "_ID") +
                " channel id = "+programInfo.getChannelId());
        return programInfo;
    }
}