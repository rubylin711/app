package com.prime.dtvplayer.Service.Database.DVBDatabaseTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.prime.dtvplayer.Database.DBUtils;
import com.prime.dtvplayer.Service.Database.DVBContentProvider;
import com.prime.dtvplayer.Sysdata.ProgramInfo;

import java.util.ArrayList;
import java.util.List;

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
                + ProgramInfo.CHANNEL_ID + ","
                + ProgramInfo.SERVICE_ID + ","
                + ProgramInfo.TYPE + ","
                + ProgramInfo.DISPLAY_NUM + ","
                + ProgramInfo.DISPLAY_NAME + ","
                + ProgramInfo.LOCK + ","
                + ProgramInfo.SKIP + ","
                + ProgramInfo.CA_FLAG + ","
                + ProgramInfo.TP_ID + ","
                + ProgramInfo.SAT_ID + ","
                + ProgramInfo.PCR + ","
                + ProgramInfo.TRANSPORT_STREAM_ID + ","
                + ProgramInfo.ORIGINAL_NETWORK_ID + ","
                + ProgramInfo.AUDIO_LR_SELELCTED + ","
                + ProgramInfo.AUDIO_SELECTED + ","
                + ProgramInfo.TUNER_ID + ","
                + ProgramInfo.PARENTAL_RATING + ","
                + ProgramInfo.PARENTAL_COUNTRY_CODE + ","
                + ProgramInfo.VIDEO_INFO_JSON + ","
                + ProgramInfo.AUDIO_INFO_JSON + ","
                + ProgramInfo.SUBTITLE_INFO_JSON + ","
                + ProgramInfo.TELETEXT_INFO_JSON
                + ");";
        return cmd;
    }

    public static String TableDrop() {
        String cmd = "DROP TABLE IF EXISTS " + ProgramDatabaseTable.TABLE_NAME + ";";
        return cmd;
    }

//    public void save(ProgramInfo programInfo) {
//        ProgramInfo DBProgramInfo = query(programInfo.getChannelId());
//        if(DBProgramInfo == null) {
//            add(programInfo);
//        }
//        else {
//            update(programInfo);
//        }
//    }

    public void save(List<ProgramInfo> programInfoList) {
        removeAll();
        for (int i = 0; i < programInfoList.size(); i++) {
            add(programInfoList.get(i));
        }
    }

    public List<ProgramInfo> load() {
        List<ProgramInfo> programInfoList = queryList();
        return programInfoList;
    }

    private void add(ProgramInfo programInfo) {
        ContentValues Value = new ContentValues();
        Value.put(ProgramInfo.CHANNEL_ID, programInfo.getChannelId());
        Value.put(ProgramInfo.SERVICE_ID, programInfo.getServiceId());
        Value.put(ProgramInfo.TYPE, programInfo.getType());
        Value.put(ProgramInfo.DISPLAY_NUM, programInfo.getDisplayNum());
        Value.put(ProgramInfo.DISPLAY_NAME, programInfo.getDisplayName());
        Value.put(ProgramInfo.LOCK, programInfo.getLock());
        Value.put(ProgramInfo.SKIP, programInfo.getSkip());
        Value.put(ProgramInfo.CA_FLAG, programInfo.getCA());
        Value.put(ProgramInfo.TP_ID, programInfo.getTpId());
        Value.put(ProgramInfo.SAT_ID, programInfo.getSatId());
        Value.put(ProgramInfo.PCR, programInfo.getPcr());
        Value.put(ProgramInfo.TRANSPORT_STREAM_ID, programInfo.getTransportStreamId());
        Value.put(ProgramInfo.ORIGINAL_NETWORK_ID, programInfo.getOriginalNetworkId());
        Value.put(ProgramInfo.AUDIO_LR_SELELCTED, programInfo.getAudioLRSelected());
        Value.put(ProgramInfo.AUDIO_SELECTED, programInfo.getAudioSelected());
        Value.put(ProgramInfo.TUNER_ID, programInfo.getTunerId());
        Value.put(ProgramInfo.PARENTAL_RATING, programInfo.getParentalRating());
        Value.put(ProgramInfo.PARENTAL_COUNTRY_CODE, programInfo.getParentCountryCode());
        Value.put(ProgramInfo.VIDEO_INFO_JSON, programInfo.getJsonStringFromVideoInfo());
        Value.put(ProgramInfo.AUDIO_INFO_JSON, programInfo.getJsonStringFromAudioInfo());
        Value.put(ProgramInfo.SUBTITLE_INFO_JSON, programInfo.getJsonStringFromSubtitleInfo());
        Value.put(ProgramInfo.TELETEXT_INFO_JSON, programInfo.getJsonStringFromTeletextInfo());

        Log.d(TAG, "add "+ programInfo.ToString());
        mContext.getContentResolver().insert(ProgramDatabaseTable.CONTENT_URI, Value);
    }

    private void update(ProgramInfo programInfo) {
        int count;
        String where = ProgramInfo.CHANNEL_ID + " = " + programInfo.getChannelId();
        ContentValues Value = new ContentValues();
//        Value.put(ProgramInfo.CHANNEL_ID, programInfo.getChannelId());
        Value.put(ProgramInfo.SERVICE_ID, programInfo.getServiceId());
        Value.put(ProgramInfo.TYPE, programInfo.getType());
        Value.put(ProgramInfo.DISPLAY_NUM, programInfo.getDisplayNum());
        Value.put(ProgramInfo.DISPLAY_NAME, programInfo.getDisplayName());
        Value.put(ProgramInfo.LOCK, programInfo.getLock());
        Value.put(ProgramInfo.SKIP, programInfo.getSkip());
        Value.put(ProgramInfo.CA_FLAG, programInfo.getCA());
        Value.put(ProgramInfo.TP_ID, programInfo.getTpId());
        Value.put(ProgramInfo.SAT_ID, programInfo.getSatId());
        Value.put(ProgramInfo.PCR, programInfo.getPcr());
        Value.put(ProgramInfo.TRANSPORT_STREAM_ID, programInfo.getTransportStreamId());
        Value.put(ProgramInfo.ORIGINAL_NETWORK_ID, programInfo.getOriginalNetworkId());
        Value.put(ProgramInfo.AUDIO_LR_SELELCTED, programInfo.getAudioLRSelected());
        Value.put(ProgramInfo.AUDIO_SELECTED, programInfo.getAudioSelected());
        Value.put(ProgramInfo.TUNER_ID, programInfo.getTunerId());
        Value.put(ProgramInfo.PARENTAL_RATING, programInfo.getParentalRating());
        Value.put(ProgramInfo.PARENTAL_COUNTRY_CODE, programInfo.getParentCountryCode());
        Value.put(ProgramInfo.VIDEO_INFO_JSON, programInfo.getJsonStringFromVideoInfo());
        Value.put(ProgramInfo.AUDIO_INFO_JSON, programInfo.getJsonStringFromAudioInfo());
        Value.put(ProgramInfo.SUBTITLE_INFO_JSON, programInfo.getJsonStringFromSubtitleInfo());
        Value.put(ProgramInfo.TELETEXT_INFO_JSON, programInfo.getJsonStringFromTeletextInfo());

        Log.d(TAG, "update " + programInfo.ToString());
        count = mContext.getContentResolver().update(ProgramDatabaseTable.CONTENT_URI, Value, where, null);
    }

    public int removeAll() {
        int count;
        count = mContext.getContentResolver().delete(ProgramDatabaseTable.CONTENT_URI,null,null);

        return count;
    }

//    public void remove(ProgramInfo programInfo) {
//        int count;
//        String where = ProgramInfo.CHANNEL_ID + " = " + programInfo.getChannelId();
//        Log.d(TAG, "remove programInfo.getChannelId() "+ programInfo.getChannelId());
//        count = mContext.getContentResolver().delete(ProgramDatabaseTable.CONTENT_URI, where, null);
//    }

    private List<ProgramInfo> queryList() {
        Cursor cursor = mContext.getContentResolver().query(ProgramDatabaseTable.CONTENT_URI, null, null, null, null);
        List<ProgramInfo> programInfoList = new ArrayList<>();
        ProgramInfo programInfo = null;
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    programInfo = ParseCursor(cursor);
                    programInfoList.add(programInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return programInfoList;
        }
        return null;
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
        programInfo.setChannelId(mDbUtils.GetLongFromTable(cursor, ProgramInfo.CHANNEL_ID));
        programInfo.setServiceId(mDbUtils.GetIntFromTable(cursor, ProgramInfo.SERVICE_ID));
        programInfo.setType(mDbUtils.GetIntFromTable(cursor, ProgramInfo.TYPE));
        programInfo.setDisplayNum(mDbUtils.GetIntFromTable(cursor, ProgramInfo.DISPLAY_NUM));
        programInfo.setDisplayName(mDbUtils.GetStringFromTable(cursor, ProgramInfo.DISPLAY_NAME));
        programInfo.setLock(mDbUtils.GetIntFromTable(cursor, ProgramInfo.LOCK));
        programInfo.setSkip(mDbUtils.GetIntFromTable(cursor, ProgramInfo.SKIP));
        programInfo.setCA(mDbUtils.GetIntFromTable(cursor, ProgramInfo.CA_FLAG));
        programInfo.setTpId(mDbUtils.GetIntFromTable(cursor, ProgramInfo.TP_ID));
        programInfo.setSatId(mDbUtils.GetIntFromTable(cursor, ProgramInfo.SAT_ID));
        programInfo.setPcr(mDbUtils.GetIntFromTable(cursor, ProgramInfo.PCR));
        programInfo.setTransportStreamId(mDbUtils.GetIntFromTable(cursor, ProgramInfo.TRANSPORT_STREAM_ID));
        programInfo.setOriginalNetworkId(mDbUtils.GetIntFromTable(cursor, ProgramInfo.ORIGINAL_NETWORK_ID));
        programInfo.setAudioLRSelected(mDbUtils.GetIntFromTable(cursor, ProgramInfo.AUDIO_LR_SELELCTED));
        programInfo.setAudioSelected(mDbUtils.GetIntFromTable(cursor, ProgramInfo.AUDIO_SELECTED));
        programInfo.setTunerId(mDbUtils.GetIntFromTable(cursor, ProgramInfo.TUNER_ID));
        programInfo.setParentalRating(mDbUtils.GetIntFromTable(cursor, ProgramInfo.PARENTAL_RATING));
        programInfo.setParentCountryCode(mDbUtils.GetStringFromTable(cursor, ProgramInfo.PARENTAL_COUNTRY_CODE));
        programInfo.pVideo = programInfo.getVideoInfoFromJsonString(mDbUtils.GetStringFromTable(cursor, ProgramInfo.VIDEO_INFO_JSON));
        programInfo.pAudios = programInfo.getAudioInfoFromJsonString(mDbUtils.GetStringFromTable(cursor, ProgramInfo.AUDIO_INFO_JSON));
        programInfo.pSubtitle = programInfo.getSubtitleInfoFromJsonString(mDbUtils.GetStringFromTable(cursor, ProgramInfo.SUBTITLE_INFO_JSON));
        programInfo.pTeletext = programInfo.getTeletextInfoFromJsonString(mDbUtils.GetStringFromTable(cursor, ProgramInfo.TELETEXT_INFO_JSON));
        Log.i(TAG, "mDbUtils.GetIntFromTable(cursor, _ID) = "+mDbUtils.GetIntFromTable(cursor, "_ID") +
                " channel id = "+programInfo.getChannelId());
        return programInfo;
    }
}