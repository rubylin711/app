package com.prime.dtvplayer.Service.Database.DVBDatabaseTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.prime.dtvplayer.Database.DBUtils;
import com.prime.dtvplayer.Service.DataManager.DataManager;
import com.prime.dtvplayer.Service.Database.DVBContentProvider;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.utils.TVScanParams;

import java.util.ArrayList;
import java.util.List;

public class GposDatabaseTable {
    private static final String TAG = "GposDatabaseTable";
    public static final String TABLE_NAME = "Gpos";
    public static final String KEY = "KEY";
    public static final String VALUE = "VALUE";

    public static final Uri CONTENT_URI = Uri.parse("content://" + DVBContentProvider.AUTHORITY + "/" + TABLE_NAME);

    //Type
    public static final String CONTENT_TYPE ="vnd.android.cursor.dir/vnd.gpos";
    public static final String CONTENT_ITEM_TYPE="vnd.android.cursor.item/vnd.gpos";

    private Context mContext;
    DBUtils mDbUtils = new DBUtils();

    public GposDatabaseTable(Context context) {
        mContext = context;
    }

    public static String TableCreate(){
        String cmd = "create table "
                + TABLE_NAME + " ("
                + "_ID" + " integer primary key autoincrement,"
                + GposDatabaseTable.KEY + " text, "
                + GposDatabaseTable.VALUE + " text"
                + ");";
        Log.d(TAG,cmd);
        return cmd;
    }

//    public static String TableCreate(){
//        String cmd = "create table "
//                + GposDatabaseTable.TABLE_NAME + " ("
//                + "_ID" + " integer primary key autoincrement,"
//                + GposInfo.GPOS_DB_VSERSION+ ","
//                + GposInfo.GPOS_CUR_CHANNELID+ ","
//                + GposInfo.GPOS_CUR_GROUP_TYPE+ ","
//                + GposInfo.GPOS_PASSWORD_VALUE+ ","
//                + GposInfo.GPOS_PARENTAL_RATE+ ","
//                + GposInfo.GPOS_PARENTAL_LOCK_ONOFF+ ","
//                + GposInfo.GPOS_INSTALL_LOCK_ONOFF+ ","
//                + GposInfo.GPOS_BOX_POWER_STATUS+ ","
//                + GposInfo.GPOS_START_ON_CHANNELID+ ","
//                + GposInfo.GPOS_START_ON_CHTYPE+ ","
//                + GposInfo.GPOS_VOLUME+ ","
//                + GposInfo.GPOS_AUDIO_TRACK_MODE+ ","
//                + GposInfo.GPOS_AUTO_REGION_TIME_OFFSET+ ","
//                + GposInfo.GPOS_REGION_TIME_OFSSET+ ","
//                + GposInfo.GPOS_REGION_SUMMER_TIME+ ","
//                + GposInfo.GPOS_LNB_POWER+ ","
//                + GposInfo.GPOS_SCREEN_16X9+ ","
//                + GposInfo.GPOS_CONVERSION+ ","
//                + GposInfo.GPOS_RESOLUTION+ ","
//                + GposInfo.GPOS_OSD_LANGUAGE+ ","
//                + GposInfo.GPOS_SEARCH_PROGRAM_TYPE+ ","
//                + GposInfo.GPOS_SEARCH_MODE+ ","
//                + GposInfo.GPOS_AUDIO_LANG_SELECT_1+ ","
//                + GposInfo.GPOS_AUDIO_LANG_SELECT_2+ ","
//                + GposInfo.GPOS_SUBTITLE_LANG_SELECT_1+ ","
//                + GposInfo.GPOS_SUBTITLE_LANG_SELECT_2+ ","
//                + GposInfo.GPOS_SORT_BY_LCN+ ","
//                + GposInfo.GPOS_OSD_TRANSPARENCY+ ","
//                + GposInfo.GPOS_BANNER_TIMEOUT+ ","
//                + GposInfo.GPOS_HARD_HEARING+ ","
//                + GposInfo.GPOS_AUTO_STANDBY_TIME+ ","
//                + GposInfo.GPOS_DOLBY_MODE+ ","
//                + GposInfo.GPOS_HDCP_ONOFF+ ","
//                + GposInfo.GPOS_DEEP_SLEEP_MODE+ ","
//                + GposInfo.GPOS_SUBTITLE_ONOFF+ ","
//                + GposInfo.GPOS_AV_STOP_MODE
//                + ");";
//        return cmd;
//    }

    public static String TableDrop() {
        String cmd = "DROP TABLE IF EXISTS "+ GposDatabaseTable.TABLE_NAME + ";";
        return cmd;
    }

    private String getKeyValue(String key,String defaultValue) {
        String value = null;
        Cursor cursor;
        try {
            cursor = mContext.getContentResolver().query(GposDatabaseTable.CONTENT_URI, new String[]{GposDatabaseTable.KEY, GposDatabaseTable.VALUE}, GposDatabaseTable.KEY + " = ?", new String[]{key}, null);
            Log.d(TAG, "getValueByColumn cursor = " + cursor);
            if (cursor == null || cursor.getCount() == 0) {
                Log.d(TAG, "getValueByColumn new column : " + key);
                insertKeyValue(key, defaultValue);
                value = defaultValue;
            } else {
                if (cursor.moveToFirst()) {
                    Log.d(TAG, "cursor moveToFirst");
                    value = mDbUtils.GetStringFromTable(cursor, GposDatabaseTable.VALUE);
                    Log.d(TAG, "cursor value = " + value);
                    if (value == null) {
                        Log.d(TAG, "cursor set new value = " + defaultValue);
                        setKeyValue(key, defaultValue);
                        value = defaultValue;
                    }

                }
            }
            if(cursor != null)
                cursor.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private void insertKeyValue(String key,String value) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(GposDatabaseTable.KEY, key);
        contentValue.put(GposDatabaseTable.VALUE, value);
        Log.d(TAG, "insertKeyValue " + key + " value = "+key);
        try {
            mContext.getContentResolver().insert(GposDatabaseTable.CONTENT_URI, contentValue);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setKeyValue(String key,String value) {
        String where = GposDatabaseTable.KEY + " = " + "\"" + key + "\"";
        ContentValues Value = new ContentValues();
        Value.put(GposDatabaseTable.VALUE, value);
        Log.d(TAG, "update " + key + " value = "+value);
        try {
            mContext.getContentResolver().update(GposDatabaseTable.CONTENT_URI, Value, where, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteKeyValue(String key,String value) {
        String where = GposDatabaseTable.KEY + " = " + "\"" + key + "\"";
        Log.d(TAG, "delete " + key);
        try {
            mContext.getContentResolver().delete(GposDatabaseTable.CONTENT_URI, where, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(GposInfo gposInfo) {
        setKeyValue(GposInfo.GPOS_DB_VSERSION,gposInfo.getDBVersion());
        setKeyValue(GposInfo.GPOS_CUR_CHANNELID,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_CUR_GROUP_TYPE,gposInfo.getCurGroupType()+"");
        setKeyValue(GposInfo.GPOS_PASSWORD_VALUE,gposInfo.getPasswordValue()+"");
        setKeyValue(GposInfo.GPOS_PARENTAL_RATE,gposInfo.getParentalRate()+"");
        setKeyValue(GposInfo.GPOS_PARENTAL_LOCK_ONOFF,gposInfo.getParentalLockOnOff()+"");
        setKeyValue(GposInfo.GPOS_INSTALL_LOCK_ONOFF,gposInfo.getInstallLockOnOff()+"");
        setKeyValue(GposInfo.GPOS_BOX_POWER_STATUS,gposInfo.getBoxPowerStatus()+"");
        setKeyValue(GposInfo.GPOS_START_ON_CHANNELID,gposInfo.getStartOnChannelId()+"");
        setKeyValue(GposInfo.GPOS_START_ON_CHTYPE,gposInfo.getStartOnChType()+"");
        setKeyValue(GposInfo.GPOS_VOLUME,gposInfo.getVolume()+"");
        setKeyValue(GposInfo.GPOS_AUDIO_TRACK_MODE,gposInfo.getAudioTrackMode()+"");
        setKeyValue(GposInfo.GPOS_AUTO_REGION_TIME_OFFSET,gposInfo.getAutoRegionTimeOffset()+"");
        setKeyValue(GposInfo.GPOS_REGION_TIME_OFSSET,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_REGION_SUMMER_TIME,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_LNB_POWER,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_SCREEN_16X9,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_CONVERSION,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_OSD_LANGUAGE,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_SEARCH_PROGRAM_TYPE,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_SEARCH_MODE,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_AUDIO_LANG_SELECT_1,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_AUDIO_LANG_SELECT_2,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_SUBTITLE_LANG_SELECT_1,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_SUBTITLE_LANG_SELECT_2,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_SORT_BY_LCN,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_OSD_TRANSPARENCY,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_BANNER_TIMEOUT,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_HARD_HEARING,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_AUTO_STANDBY_TIME,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_DOLBY_MODE,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_HDCP_ONOFF,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_DEEP_SLEEP_MODE,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_SUBTITLE_ONOFF,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_AV_STOP_MODE,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_TIMESHIFT_DURATION,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_RECORD_ICON_ONOFF,gposInfo.getCurChannelId()+"");
    }

    public GposInfo load() {
        GposInfo gposInfo = new GposInfo();
        String dbver = "";
        if(DataManager.TUNR_TYPE == TpInfo.DVBC)
            dbver = "C";
        else if(DataManager.TUNR_TYPE == TpInfo.DVBT)
            dbver = "T";
        else if(DataManager.TUNR_TYPE == TpInfo.ISDBT)
            dbver = "I";
        else if(DataManager.TUNR_TYPE == TpInfo.DVBS)
            dbver = "S";
        gposInfo.setDBVersion(getKeyValue(GposInfo.GPOS_DB_VSERSION,dbver+"1.0"));
        gposInfo.setCurChannelId(Long.parseLong(getKeyValue(GposInfo.GPOS_CUR_CHANNELID,0+"")));
        gposInfo.setCurGroupType(Integer.parseInt(getKeyValue(GposInfo.GPOS_CUR_GROUP_TYPE,0+"")));
        gposInfo.setPasswordValue(Integer.parseInt(getKeyValue(GposInfo.GPOS_PASSWORD_VALUE,0+"")));
        gposInfo.setParentalRate(Integer.parseInt(getKeyValue(GposInfo.GPOS_PARENTAL_RATE,0+"")));
        gposInfo.setParentalLockOnOff(Integer.parseInt(getKeyValue(GposInfo.GPOS_PARENTAL_LOCK_ONOFF,1+"")));
        gposInfo.setInstallLockOnOff(Integer.parseInt(getKeyValue(GposInfo.GPOS_INSTALL_LOCK_ONOFF,0+"")));
        gposInfo.setBoxPowerStatus(Integer.parseInt(getKeyValue(GposInfo.GPOS_BOX_POWER_STATUS,1+"")));
        gposInfo.setStartOnChannelId(Long.parseLong(getKeyValue(GposInfo.GPOS_START_ON_CHANNELID,0+"")));
        gposInfo.setStartOnChType(Integer.parseInt(getKeyValue(GposInfo.GPOS_START_ON_CHTYPE,0+"")));
        gposInfo.setVolume(Integer.parseInt(getKeyValue(GposInfo.GPOS_VOLUME,15+"")));
        gposInfo.setAudioTrackMode(Integer.parseInt(getKeyValue(GposInfo.GPOS_AUDIO_TRACK_MODE,0+"")));
        gposInfo.setAutoRegionTimeOffset(Integer.parseInt(getKeyValue(GposInfo.GPOS_AUTO_REGION_TIME_OFFSET,0+"")));
        gposInfo.setRegionTimeOffset(Float.parseFloat(getKeyValue(GposInfo.GPOS_REGION_TIME_OFSSET,0+"")));
        gposInfo.setRegionSummerTime(Integer.parseInt(getKeyValue(GposInfo.GPOS_REGION_SUMMER_TIME,0+"")));
        gposInfo.setLnbPower(Integer.parseInt(getKeyValue(GposInfo.GPOS_LNB_POWER,1+"")));
        gposInfo.setScreen16x9(Integer.parseInt(getKeyValue(GposInfo.GPOS_SCREEN_16X9,0+"")));
        gposInfo.setConversion(Integer.parseInt(getKeyValue(GposInfo.GPOS_CONVERSION,1+"")));
        gposInfo.setOSDLanguage(getKeyValue(GposInfo.GPOS_OSD_LANGUAGE,"eng"));
        gposInfo.setSearchProgramType(Integer.parseInt(getKeyValue(GposInfo.GPOS_SEARCH_PROGRAM_TYPE,TVScanParams.SEARCH_OPTION_ALL+"")));
        gposInfo.setSearchMode(Integer.parseInt(getKeyValue(GposInfo.GPOS_SEARCH_MODE,TVScanParams.SEARCH_OPTION_ALL+"")));
        gposInfo.setAudioLanguageSelection(0,getKeyValue(GposInfo.GPOS_AUDIO_LANG_SELECT_1,"English"));
        gposInfo.setAudioLanguageSelection(1,getKeyValue(GposInfo.GPOS_AUDIO_LANG_SELECT_2,"English"));
        gposInfo.setSubtitleLanguageSelection(0,getKeyValue(GposInfo.GPOS_SUBTITLE_LANG_SELECT_1,"English"));
        gposInfo.setSubtitleLanguageSelection(1,getKeyValue(GposInfo.GPOS_SUBTITLE_LANG_SELECT_2,"English"));
        gposInfo.setSortByLcn(Integer.parseInt(getKeyValue(GposInfo.GPOS_SORT_BY_LCN,1+"")));
        gposInfo.setOSDTransparency(Integer.parseInt(getKeyValue(GposInfo.GPOS_OSD_TRANSPARENCY,0+"")));
        gposInfo.setBannerTimeout(Integer.parseInt(getKeyValue(GposInfo.GPOS_BANNER_TIMEOUT,5+"")));
        gposInfo.setHardHearing(Integer.parseInt(getKeyValue(GposInfo.GPOS_HARD_HEARING,1+"")));
        gposInfo.setAutoStandbyTime(Integer.parseInt(getKeyValue(GposInfo.GPOS_AUTO_STANDBY_TIME,0+"")));
        gposInfo.setDolbyMode(Integer.parseInt(getKeyValue(GposInfo.GPOS_DOLBY_MODE,0+"")));
        gposInfo.setHDCPOnOff(Integer.parseInt(getKeyValue(GposInfo.GPOS_HDCP_ONOFF,1+"")));
        gposInfo.setDeepSleepMode(Integer.parseInt(getKeyValue(GposInfo.GPOS_DEEP_SLEEP_MODE,0+"")));
        gposInfo.setSubtitleOnOff(Integer.parseInt(getKeyValue(GposInfo.GPOS_SUBTITLE_ONOFF,0+"")));
        gposInfo.setAvStopMode(Integer.parseInt(getKeyValue(GposInfo.GPOS_AV_STOP_MODE,1+"")));
        gposInfo.setTimeshiftDuration(Integer.parseInt(getKeyValue(GposInfo.GPOS_TIMESHIFT_DURATION,90*60+"")));
        gposInfo.setRecordIconOnOff(Integer.parseInt(getKeyValue(GposInfo.GPOS_RECORD_ICON_ONOFF,1+"")));
        return gposInfo;
    }
}
