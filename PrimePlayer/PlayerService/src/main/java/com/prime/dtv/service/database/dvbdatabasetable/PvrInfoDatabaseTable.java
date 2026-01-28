package com.prime.dtv.service.database.dvbdatabasetable;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.prime.dtv.database.DBUtils;
import com.prime.dtv.service.database.PvrDatabase;

public class PvrInfoDatabaseTable {

    private static final String TAG = "PvrRecordTable";

    public static final String TABLE_NAME = PvrDatabase.TABLE_NAME;

    public static final String TABLE_NAME_PLAYBACK = PvrDatabase.TABLE_NAME_PLAYBACK;
    public static final String DATABASE_NAME = "pvr_database.db";
    public static final String DATABASE_NAME_PLAYBACK = "playback_database.db";

//    public static final Uri CONTENT_MASTER_URI = Uri.parse("content://" + PvrContentProvider.AUTHORITY + "/" + TABLE_NAME);
//    public static final Uri CONTENT_PLAYBACK_URI = Uri.parse("content://" + PvrContentProvider.AUTHORITY + "/" + TABLE_NAME_PLAYBACK);
//    public static final String CONTENT_SERIES_STRING = "content://" + PvrContentProvider.AUTHORITY + "/" + TABLE_NAME+ "/";
//    public static final String CONTENT_STRING = "content://" + PvrContentProvider.AUTHORITY + "/";
    public static final String PRIMARY_ID = "ID";
    public static final String CH_NUM = "CH_NUM";
    public static final String CHANNEL_LOCK = "CHANNEL_LOCK";
    public static final String PARENT_RATING = "PARENT_RATING";
    public static final String RECORD_STATUS = "RECORD_STATUS";
    public static final String CHANNEL_ID = "CHANNEL_ID";
    public static final String START_TIME = "START_TIME";
    public static final String DURATION = "DURATION";
    public static final String PLAY_STOP_POS = "PLAY_STOP_POS";
    public static final String SERVICE_TYPE = "SERVICE_TYPE";
    public static final String FILE_SIZE = "FILE_SIZE";
    public static final String CHNAME = "CHNAME";
    public static final String EVENT_NAME = "EVENT_NAME";
    public static final String FULLNAMEPATH = "FULLNAMEPATH";
    public static final String SERIES_KEY = "SERIES_KEY";
    public static final String SERIES = "SERIES";
    public static final String TOTAL_RECORDS = "TOTAL_RECORDS";
    public static final String TOTAL_EPISODES = "TOTAL_EPISODES";
    public static final String PLAY_TIME = "PLAY_TIME";
    public static final String VIDEO_INFO_JSON = "VIDEO_INFO";
    public static final String AUDIO_INFO_JSON = "AUDIO_INFO";
    public static final String SUBTITLE_INFO_JSON = "SUBTITLE_INFO";
    public static final String TELETEXT_INFO_JSON = "TELETEXT_INFO";
    public static final String EPG_INFO_JSON = "EPG_INFO";

    public static final String SERIES_PRIMARY_ID = "ID";
    public static final String SERIES_CH_NUM = "CH_NUM";
    public static final String SERIES_CHANNEL_LOCK = "CHANNEL_LOCK";
    public static final String SERIES_PARENT_RATING = "PARENT_RATING";
    public static final String SERIES_RECORD_STATUS = "RECORD_STATUS";
    public static final String SERIES_CHANNEL_ID = "CHANNEL_ID";
    public static final String SERIES_START_TIME = "START_TIME";
    public static final String SERIES_DURATION = "DURATION";
    public static final String SERIES_PLAY_STOP_POS = "PLAY_STOP_POS";
    public static final String SERIES_SERVICE_TYPE = "SERVICE_TYPE";
    public static final String SERIES_FILE_SIZE = "FILE_SIZE";
    public static final String SERIES_CHNAME = "CHNAME";
    public static final String SERIES_EVENT_NAME = "EVENT_NAME";
    public static final String SERIES_FULLNAMEPATH = "FULLNAMEPATH";
    public static final String SERIES_SERIES_KEY = "SERIES_KEY";
    public static final String SERIES_EPISODE = "EPISODE";
    public static final String SERIES_PLAY_TIME = "PLAY_TIME";
    public static final String SERIES_VIDEO_INFO_JSON = "VIDEO_INFO";
    public static final String SERIES_AUDIO_INFO_JSON = "AUDIO_INFO";
    public static final String SERIES_SUBTITLE_INFO_JSON = "SUBTITLE_INFO";
    public static final String SERIES_TELETEXT_INFO_JSON = "TELETEXT_INFO";
    public static final String SERIES_EPG_INFO_JSON = "EPG_INFO";
    public static final String MASTER_ID = "MASTER_ID";
    public static final String SERIES_ID = "SERIES_ID";

    public static final String IS_SERIES = "IS_SERIES";
    public static final String PLAYBACK_TIME = "PLAYBACK_TIME";


    public static final String[] ColumnsMasterType = {
            "integer", "integer", "integer", "integer",
            "integer", "integer", "integer", "integer",
            "integer", "integer", "integer",
            "text","text","text",
            "blob","integer",
            "text","text","text","text","text",
            "integer","integer","integer",
    };

    public static final String[] ColumnsSeriesType ={
            "integer","integer","integer","integer",
            "integer","integer","integer","integer",
            "integer","integer","integer",
            "text","text","text",
            "blob","integer",
            "text","text","text","text","text",
            "integer"
    };
    public static final String[] ColumnsPlaybackType = {
            "integer", "integer", "integer", "integer", "integer"
    };
    public static final int Primary_Id = 0;
    public static final int Ch_Num = 1;
    public static final int Channel_Lock = 2;
    public static final int Parent_Rating = 3;
    public static final int Record_Status = 4;
    public static final int Channel_Id = 5;
    public static final int Start_Time = 6;
    public static final int Duration = 7;
    public static final int Play_Stop_Pos = 8;
    public static final int Service_Type = 9;
    public static final int File_Size =10;
    public static final int Chname = 11;
    public static final int Event_Name = 12;
    public static final int Fullnamepath = 13;
    public static final int Series_Key = 14;
    public static final int Play_Time = 15;
    public static final int Video_Info_Json = 16;
    public static final int Audio_Info_Json = 17;
    public static final int Subtitle_Info_Json = 18;
    public static final int Teletext_Info_Json = 19;
    public static final int Epg_Info_Json = 20;
    public static final int Series = 21;
    public static final int Total_Records = 22;
    public static final int Total_Episodes = 23;

    public static final int Series_Primary_Id = 0;
    public static final int Series_Ch_Num = 1;
    public static final int Series_Channel_Lock = 2;
    public static final int Series_Parent_Rating = 3;
    public static final int Series_Record_Status = 4;
    public static final int Series_Channel_Id = 5;
    public static final int Series_Start_Time = 6;
    public static final int Series_Duration = 7;
    public static final int Series_Play_Stop_Pos = 8;
    public static final int Series_Service_Type = 9;
    public static final int Series_File_Size = 10;
    public static final int Series_Chname = 11;
    public static final int Series_Event_Name = 12;
    public static final int Series_Fullnamepath = 13;
    public static final int Series_Series_Key = 14;
    public static final int Series_Play_Time = 15;
    public static final int Series_Video_Info_Json = 16;
    public static final int Series_Audio_Info_Json = 17;
    public static final int Series_Subtitle_Info_Json = 18;
    public static final int Series_Teletext_Info_Json = 19;
    public static final int Series_Epg_Info_Json = 20;
    public static final int Series_Episode = 21;

    public static final int Id = 0;
    public static final int MasterId = 1;
    public static final int SeriesId = 2;
    public static final int IsSeries = 3;
    public static final int PlaybackTime = 4;
    public static final int FILE_SORT_SAMLL_TO_LARGE_METHOD = 0;
    public static final int FILE_SORT_LARGE_TO_SMALL_METHOD = 1;

    public static final int FILE_SORT_BY_NAME_TYPE = 0;
    public static final int FILE_SORT_BY_CHNAME_TYPE = 1;
    public static final int FILE_SORT_BY_LCN_TYPE = 2;//channel_id
    public static final int FILE_SORT_BY_START_TIME_TYPE = 3;
    public static final int FILE_SORT_BY_SERIES_TYPE = 4;
    public static final int FILE_SORT_BY_PLAYTIME_TYPE = 5;

    private Context mContext;
    DBUtils mDbUtils = new DBUtils();
    private ContentResolver resolver;
    private int mSortMethod = FILE_SORT_SAMLL_TO_LARGE_METHOD;
    private int mSortType = FILE_SORT_BY_LCN_TYPE;

    public PvrInfoDatabaseTable(Context context) {
        mContext = context;
        resolver = mContext.getContentResolver();
    }

    public static String TableCreateMaster() {
        String cmd;
        cmd = "create table "
                + TABLE_NAME + " ("
                + PRIMARY_ID + " " + ColumnsMasterType[Primary_Id]+" primary key autoincrement,"
                + CH_NUM + " " + ColumnsMasterType[Ch_Num] + ", "
                + CHANNEL_LOCK + " " + ColumnsMasterType[Channel_Lock] + ", "
                + PARENT_RATING + " " + ColumnsMasterType[Parent_Rating] + ", "
                + RECORD_STATUS + " " + ColumnsMasterType[Record_Status] + ", "
                + CHANNEL_ID + " " + ColumnsMasterType[Channel_Id] + ", "
                + START_TIME + " " + ColumnsMasterType[Start_Time] + ", "
                + DURATION + " " + ColumnsMasterType[Duration] + ", "
                + PLAY_STOP_POS + " " + ColumnsMasterType[Play_Stop_Pos] + ", "
                + SERVICE_TYPE + " " + ColumnsMasterType[Service_Type] + ", "
                + FILE_SIZE + " " + ColumnsMasterType[File_Size] + ", "
                + CHNAME + " " + ColumnsMasterType[Chname] + ", "
                + EVENT_NAME + " " + ColumnsMasterType[Event_Name] + ", "
                + FULLNAMEPATH + " " + ColumnsMasterType[Fullnamepath] + ", "
                + SERIES_KEY + " " + ColumnsMasterType[Series_Key] + ", "
                + PLAY_TIME + " " + ColumnsMasterType[Play_Time] + ", "
                + VIDEO_INFO_JSON + " " + ColumnsMasterType[Video_Info_Json] + ", "
                + AUDIO_INFO_JSON + " " + ColumnsMasterType[Audio_Info_Json] + ", "
                + SUBTITLE_INFO_JSON + " " + ColumnsMasterType[Subtitle_Info_Json] + ", "
                + TELETEXT_INFO_JSON + " " + ColumnsMasterType[Teletext_Info_Json] + ", "
                + EPG_INFO_JSON + " " + ColumnsMasterType[Epg_Info_Json] + ", "
                + SERIES + " " + ColumnsMasterType[Series] + ", "
                + TOTAL_RECORDS + " " + ColumnsMasterType[Total_Records] + ", "
                + TOTAL_EPISODES + " " + ColumnsMasterType[Total_Episodes]
                + ");";
        return cmd;
    }

    public static String TableCreateSeries(int seriesId) {
        String cmd;
        if(seriesId < 0) {
            cmd = "";
        }
        else{
            cmd = "create table "
                    + TABLE_NAME + "_" + seriesId + " ("
                    + SERIES_PRIMARY_ID + " " + ColumnsSeriesType[Series_Primary_Id]+" primary key autoincrement,"
                    + SERIES_CH_NUM + " " + ColumnsSeriesType[Series_Ch_Num] + ", "
                    + SERIES_CHANNEL_LOCK + " " + ColumnsSeriesType[Series_Channel_Lock] + ", "
                    + SERIES_PARENT_RATING + " " + ColumnsSeriesType[Series_Parent_Rating] + ", "
                    + SERIES_RECORD_STATUS + " " + ColumnsSeriesType[Series_Record_Status] + ", "
                    + SERIES_CHANNEL_ID + " " + ColumnsSeriesType[Series_Channel_Id] + ", "
                    + SERIES_START_TIME + " " + ColumnsSeriesType[Series_Start_Time] + ", "
                    + SERIES_DURATION + " " + ColumnsSeriesType[Series_Duration] + ", "
                    + SERIES_PLAY_STOP_POS + " " + ColumnsSeriesType[Series_Play_Stop_Pos] + ", "
                    + SERIES_SERVICE_TYPE + " " + ColumnsSeriesType[Series_Service_Type] + ", "
                    + SERIES_FILE_SIZE + " " + ColumnsSeriesType[Series_File_Size] + ", "
                    + SERIES_CHNAME + " " + ColumnsSeriesType[Series_Chname] + ", "
                    + SERIES_EVENT_NAME + " " + ColumnsSeriesType[Series_Event_Name] + ", "
                    + SERIES_FULLNAMEPATH + " " + ColumnsSeriesType[Series_Fullnamepath] + ", "
                    + SERIES_SERIES_KEY + " " + ColumnsSeriesType[Series_Series_Key] + ", "
                    + SERIES_PLAY_TIME + " " + ColumnsSeriesType[Series_Play_Time] + ", "
                    + SERIES_VIDEO_INFO_JSON + " " + ColumnsSeriesType[Series_Video_Info_Json] + ", "
                    + SERIES_AUDIO_INFO_JSON + " " + ColumnsSeriesType[Series_Audio_Info_Json] + ", "
                    + SERIES_SUBTITLE_INFO_JSON + " " + ColumnsSeriesType[Series_Subtitle_Info_Json] + ", "
                    + SERIES_TELETEXT_INFO_JSON + " " + ColumnsSeriesType[Series_Teletext_Info_Json] + ", "
                    + SERIES_EPG_INFO_JSON + " " + ColumnsSeriesType[Series_Epg_Info_Json] + ", "
                    + SERIES_EPISODE + " " + ColumnsSeriesType[Series_Episode]
                    + ");";
        }
        return cmd;
    }
    public static String TableCreatePlayback() {
        String cmd;
        cmd = "create table "
                + TABLE_NAME_PLAYBACK + " ("
                + PRIMARY_ID + " " + ColumnsPlaybackType[Id]+ " primary key autoincrement, "
                + MASTER_ID + " " + ColumnsPlaybackType[MasterId]+", "
                + SERIES_ID + " " + ColumnsPlaybackType[SeriesId] + ", "
                + IS_SERIES + " " + ColumnsPlaybackType[IsSeries] + ", "
                + PLAYBACK_TIME + " " + ColumnsPlaybackType[PlaybackTime]
                + ");";
        return cmd;
    }
    public static String TableDropMaster() {
        String cmd;
        cmd = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
        return cmd;
    }

    public static String TableDropSeries(int seriesId) {
        String cmd;
        if(seriesId < 0) {
            cmd = "";
        }
        else{
            cmd = "DROP TABLE IF EXISTS " + TABLE_NAME + "_" + seriesId  + ";";
        }
        return cmd;
    }
    public static String TableDropPlayback() {
        String cmd;
        cmd = "DROP TABLE IF EXISTS " + TABLE_NAME_PLAYBACK + ";";
        return cmd;
    }
    public static boolean  isMasterTableExists(SQLiteDatabase db){
        return isTableExists(db, TABLE_NAME);
    }

    public static boolean  isSeriesTableExists(SQLiteDatabase db, int seriesId){
        return isTableExists(db, TABLE_NAME + "_" + seriesId);
    }
    public static boolean  isPlaybackTableExists(SQLiteDatabase db){
        return isTableExists(db, TABLE_NAME_PLAYBACK);
    }
    private static boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}

