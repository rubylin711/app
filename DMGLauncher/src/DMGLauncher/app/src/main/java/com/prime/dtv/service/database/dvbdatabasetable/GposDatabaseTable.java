package com.prime.dtv.service.database.dvbdatabasetable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.prime.dtv.database.DBUtils;
import com.prime.dtv.service.database.DVBContentProvider;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.utils.LogUtils;
import com.prime.dtv.utils.TVScanParams;

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
        //Log.d(TAG,cmd);
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
            //Log.d(TAG, "getValueByColumn cursor = " + cursor);
            if (cursor == null || cursor.getCount() == 0) {
                //Log.d(TAG, "getValueByColumn new column : " + key);
                insertKeyValue(key, defaultValue);
                value = defaultValue;
            } else {
                if (cursor.moveToFirst()) {
                    //Log.d(TAG, "cursor moveToFirst");
                    value = mDbUtils.GetStringFromTable(cursor, GposDatabaseTable.VALUE);
                    //Log.d(TAG, "cursor value = " + value);
                    if (value == null) {
                        //Log.d(TAG, "cursor set new value = " + defaultValue);
                        setKeyValue(key, defaultValue);
                        value = defaultValue;
                    }

                }else{
                    value = defaultValue;
                }
            }
            if(cursor != null)
                cursor.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
        return value;
    }

    private void insertKeyValue(String key,String value) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(GposDatabaseTable.KEY, key);
        contentValue.put(GposDatabaseTable.VALUE, value);
        //(TAG, "insertKeyValue " + key + " value = "+key);
        try {
            mContext.getContentResolver().insert(GposDatabaseTable.CONTENT_URI, contentValue);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setKeyValue(String key, long value) {
        String where = GposDatabaseTable.KEY + " = " + "\"" + key + "\"";
        ContentValues Value = new ContentValues();
        Value.put(GposDatabaseTable.VALUE, value);
        //Log.d(TAG, "update " + key + " value = "+value);
        try {
            mContext.getContentResolver().update(GposDatabaseTable.CONTENT_URI, Value, where, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setKeyValue(String key,String value) {
        String where = GposDatabaseTable.KEY + " = " + "\'" + key + "\'";
        ContentValues Value = new ContentValues();
        Value.put(GposDatabaseTable.VALUE, value);
        //Log.d(TAG, "update " + key + " value = "+value);
        try {
            mContext.getContentResolver().update(GposDatabaseTable.CONTENT_URI, Value, where, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteKeyValue(String key,String value) {
        String where = GposDatabaseTable.KEY + " = " + "\"" + key + "\"";
        //Log.d(TAG, "delete " + key);
        try {
            mContext.getContentResolver().delete(GposDatabaseTable.CONTENT_URI, where, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(String key,String value){
        LogUtils.d("save "+key+" to "+value);
        setKeyValue(key,value);
    }

    public void save(String key, long value){
        LogUtils.d("save "+key+" to "+value);
        setKeyValue(key, value);
    }

    public void save(GposInfo gposInfo) {
        setKeyValue(GposInfo.GPOS_DB_VSERSION,gposInfo.getDBVersion());
        setKeyValue(GposInfo.GPOS_CUR_CHANNELID,gposInfo.getCurChannelId()+"");
        setKeyValue(GposInfo.GPOS_CUR_GROUP_TYPE,gposInfo.getCurGroupType()+"");
        setKeyValue(GposInfo.GPOS_PASSWORD_VALUE,gposInfo.getPasswordValue()+"");
        setKeyValue(GposInfo.GPOS_WORKER_PASSWORD_VALUE,gposInfo.getWorkerPasswordValue()+"");
        setKeyValue(GposInfo.GPOS_PARENTAL_RATE,gposInfo.getParentalRate()+"");
        setKeyValue(GposInfo.GPOS_PARENTAL_LOCK_ONOFF,gposInfo.getParentalLockOnOff()+"");
        setKeyValue(GposInfo.GPOS_INSTALL_LOCK_ONOFF,gposInfo.getInstallLockOnOff()+"");
        setKeyValue(GposInfo.GPOS_BOX_POWER_STATUS,gposInfo.getBoxPowerStatus()+"");
        setKeyValue(GposInfo.GPOS_START_ON_CHANNELID,gposInfo.getStartOnChannelId()+"");
        setKeyValue(GposInfo.GPOS_START_ON_CHTYPE,gposInfo.getStartOnChType()+"");
        setKeyValue(GposInfo.GPOS_VOLUME,gposInfo.getVolume()+"");
        setKeyValue(GposInfo.GPOS_AUDIO_TRACK_MODE,gposInfo.getAudioTrackMode()+"");
        setKeyValue(GposInfo.GPOS_AUTO_REGION_TIME_OFFSET,gposInfo.getAutoRegionTimeOffset()+"");
        setKeyValue(GposInfo.GPOS_REGION_TIME_OFSSET,gposInfo.getRegionTimeOffset()+"");
        setKeyValue(GposInfo.GPOS_REGION_SUMMER_TIME,gposInfo.getRegionSummerTime()+"");
        setKeyValue(GposInfo.GPOS_LNB_POWER,gposInfo.getLnbPower()+"");
        setKeyValue(GposInfo.GPOS_SCREEN_16X9,gposInfo.getScreen16x9()+"");
        setKeyValue(GposInfo.GPOS_CONVERSION,gposInfo.getConversion()+"");
        setKeyValue(GposInfo.GPOS_OSD_LANGUAGE,gposInfo.getOSDLanguage()+"");
        setKeyValue(GposInfo.GPOS_SEARCH_PROGRAM_TYPE,gposInfo.getSearchProgramType()+"");
        setKeyValue(GposInfo.GPOS_SEARCH_MODE,gposInfo.getSearchMode()+"");
        setKeyValue(GposInfo.GPOS_AUDIO_LANG_SELECT_1,gposInfo.getAudioLanguageSelection(0)+"");
        setKeyValue(GposInfo.GPOS_AUDIO_LANG_SELECT_2,gposInfo.getAudioLanguageSelection(1)+"");
        setKeyValue(GposInfo.GPOS_SUBTITLE_LANG_SELECT_1,gposInfo.getSubtitleLanguageSelection(0)+"");
        setKeyValue(GposInfo.GPOS_SUBTITLE_LANG_SELECT_2,gposInfo.getSubtitleLanguageSelection(1)+"");
        setKeyValue(GposInfo.GPOS_SORT_BY_LCN,gposInfo.getSortByLcn()+"");
        setKeyValue(GposInfo.GPOS_OSD_TRANSPARENCY,gposInfo.getOSDTransparency()+"");
        setKeyValue(GposInfo.GPOS_BANNER_TIMEOUT,gposInfo.getBannerTimeout()+"");
        setKeyValue(GposInfo.GPOS_HARD_HEARING,gposInfo.getHardHearing()+"");
        setKeyValue(GposInfo.GPOS_AUTO_STANDBY_TIME,gposInfo.getAutoStandbyTime()+"");
        setKeyValue(GposInfo.GPOS_DOLBY_MODE,gposInfo.getDolbyMode()+"");
        setKeyValue(GposInfo.GPOS_HDCP_ONOFF,gposInfo.getHDCPOnOff()+"");
        setKeyValue(GposInfo.GPOS_DEEP_SLEEP_MODE,gposInfo.getDeepSleepMode()+"");
        setKeyValue(GposInfo.GPOS_SUBTITLE_ONOFF,gposInfo.getSubtitleOnOff()+"");
        setKeyValue(GposInfo.GPOS_AV_STOP_MODE,gposInfo.getAvStopMode()+"");
        setKeyValue(GposInfo.GPOS_TIMESHIFT_DURATION,gposInfo.getTimeshiftDuration()+"");
        setKeyValue(GposInfo.GPOS_RECORD_ICON_ONOFF,gposInfo.getRecordIconOnOff()+"");
        setKeyValue(GposInfo.GPOS_MAIL_SETTINGS_SHOPPING,gposInfo.getMailSettingsShopping()+"");
        setKeyValue(GposInfo.GPOS_MAIL_SETTINGS_NEWS,gposInfo.getMailSettingsNews()+"");
        setKeyValue(GposInfo.GPOS_MAIL_SETTINGS_POPULAR,gposInfo.getMailSettingsPopular()+"");
        setKeyValue(GposInfo.GPOS_MAIL_SETTINGS_COUPON,gposInfo.getMailSettingsCoupon()+"");
        setKeyValue(GposInfo.GPOS_MAIL_SETTINGS_SERVICE,gposInfo.getMailSettingsService()+"");
        setKeyValue(GposInfo.GPOS_CHANNEL_LOCK_COUNT,gposInfo.getChannelLockCount()+"");
        setKeyValue(GposInfo.GPOS_NIT_VERSION,gposInfo.getNitVersion()+"");
        setKeyValue(GposInfo.GPOS_NIT_ID,gposInfo.getNitId()+"");
        setKeyValue(GposInfo.GPOS_SI_NIT_ID,gposInfo.getSINitNetworkId()+"");
        setKeyValue(GposInfo.GPOS_BAT_VERSION,gposInfo.getBatVersion()+"");
        setKeyValue(GposInfo.GPOS_BAT_ID,gposInfo.getBatId()+"");
        setKeyValue(GposInfo.GPOS_TIME_LOCK_PERIOD_1_START,gposInfo.getTimeLockPeriodStart(0)+"");
        setKeyValue(GposInfo.GPOS_TIME_LOCK_PERIOD_2_START,gposInfo.getTimeLockPeriodStart(1)+"");
        setKeyValue(GposInfo.GPOS_TIME_LOCK_PERIOD_3_START,gposInfo.getTimeLockPeriodStart(2)+"");
        setKeyValue(GposInfo.GPOS_TIME_LOCK_PERIOD_1_END,gposInfo.getTimeLockPeriodEnd(0)+"");
        setKeyValue(GposInfo.GPOS_TIME_LOCK_PERIOD_2_END,gposInfo.getTimeLockPeriodEnd(1)+"");
        setKeyValue(GposInfo.GPOS_TIME_LOCK_PERIOD_3_END,gposInfo.getTimeLockPeriodEnd(2)+"");
        setKeyValue(GposInfo.GPOS_INTRODUCTION_TIME,gposInfo.getIntroductionTime()+"");
        setKeyValue(GposInfo.GPOS_ZIPCODE,gposInfo.getZipCode()+"");
        setKeyValue(GposInfo.GPOS_PVRENABLE,gposInfo.getPvrEnable()+"");
        setKeyValue(GposInfo.GPOS_HOME_ID,gposInfo.getHomeId()+"");
        setKeyValue(GposInfo.GPOS_SO,gposInfo.getSo()+"");
        setKeyValue(GposInfo.GPOS_MOBILE,gposInfo.getMobile()+"");
        setKeyValue(GposInfo.GPOS_STANDBY_REDIRECT,gposInfo.getStandbyRedirect()+"");

        setKeyValue(GposInfo.GPOS_MAIL_NOTIFY_STATUS,gposInfo.getMailNotifyStatus()+"");
        setKeyValue(GposInfo.GPOS_SEARCH_ONE_TP_FLAG, gposInfo.getSearchOneTPFlag()+"");
        setKeyValue(GposInfo.GPOS_ISMUTE, gposInfo.getIsMute()+"");
        setKeyValue(GposInfo.GPOS_WVCAS_LICENSE_URL, gposInfo.getWVCasLicenseURL()+"");
        setKeyValue(GposInfo.GPOS_OTA_MD5, gposInfo.getOta_md5()+"");
        setKeyValue(GposInfo.GPOS_WVCAS_REF_CASDATA_TIME, gposInfo.getSTBRefCasDataTime()+"");
        setKeyValue(GposInfo.GPOS_DVR_MANAGEMENT_LIMIT, gposInfo.getDVRManagementLimit()+"");

        for(int i =0 ; i<GposInfo.MAX_NUM_OF_IRD_COMMAND; i++){
            String key = GposInfo.GPOS_IrdCommand+i;
            setKeyValue(key, gposInfo.getIrdCommand(i)+"");
        }
    }

    public GposInfo load() {
        GposInfo gposInfo = new GposInfo();
//        String dbver = "";
//        if(DataManager.TUNR_TYPE == TpInfo.DVBC)
//            dbver = "C";
//        else if(DataManager.TUNR_TYPE == TpInfo.DVBT)
//            dbver = "T";
//        else if(DataManager.TUNR_TYPE == TpInfo.ISDBT)
//            dbver = "I";
//        else if(DataManager.TUNR_TYPE == TpInfo.DVBS)
//            dbver = "S";
        gposInfo.setDBVersion(getKeyValue(GposInfo.GPOS_DB_VSERSION,"null"));
        gposInfo.setCurChannelId(Long.parseLong(getKeyValue(GposInfo.GPOS_CUR_CHANNELID,"0")));
        gposInfo.setCurGroupType(Integer.parseInt(getKeyValue(GposInfo.GPOS_CUR_GROUP_TYPE,"0")));
        gposInfo.setPasswordValue(Integer.parseInt(getKeyValue(GposInfo.GPOS_PASSWORD_VALUE,"0")));
        gposInfo.setWorkerPasswordValue(Integer.parseInt(getKeyValue(GposInfo.GPOS_WORKER_PASSWORD_VALUE,"77767776")));
        gposInfo.setParentalRate(Integer.parseInt(getKeyValue(GposInfo.GPOS_PARENTAL_RATE,"0")));
        gposInfo.setParentalLockOnOff(Integer.parseInt(getKeyValue(GposInfo.GPOS_PARENTAL_LOCK_ONOFF,"1")));
        gposInfo.setInstallLockOnOff(Integer.parseInt(getKeyValue(GposInfo.GPOS_INSTALL_LOCK_ONOFF,"0")));
        gposInfo.setBoxPowerStatus(Integer.parseInt(getKeyValue(GposInfo.GPOS_BOX_POWER_STATUS,"1")));
        gposInfo.setStartOnChannelId(Long.parseLong(getKeyValue(GposInfo.GPOS_START_ON_CHANNELID,"0")));
        gposInfo.setStartOnChType(Integer.parseInt(getKeyValue(GposInfo.GPOS_START_ON_CHTYPE,"0")));
        gposInfo.setVolume(Integer.parseInt(getKeyValue(GposInfo.GPOS_VOLUME,"15")));
        gposInfo.setAudioTrackMode(Integer.parseInt(getKeyValue(GposInfo.GPOS_AUDIO_TRACK_MODE,"0")));
        gposInfo.setAutoRegionTimeOffset(Integer.parseInt(getKeyValue(GposInfo.GPOS_AUTO_REGION_TIME_OFFSET,"0")));
        gposInfo.setRegionTimeOffset(Float.parseFloat(getKeyValue(GposInfo.GPOS_REGION_TIME_OFSSET,"0")));
        gposInfo.setRegionSummerTime(Integer.parseInt(getKeyValue(GposInfo.GPOS_REGION_SUMMER_TIME,"0")));
        gposInfo.setLnbPower(Integer.parseInt(getKeyValue(GposInfo.GPOS_LNB_POWER,"1")));
        gposInfo.setScreen16x9(Integer.parseInt(getKeyValue(GposInfo.GPOS_SCREEN_16X9,"0")));
        gposInfo.setConversion(Integer.parseInt(getKeyValue(GposInfo.GPOS_CONVERSION,"1")));
        gposInfo.setOSDLanguage(getKeyValue(GposInfo.GPOS_OSD_LANGUAGE,"eng"));
        gposInfo.setSearchProgramType(Integer.parseInt(getKeyValue(GposInfo.GPOS_SEARCH_PROGRAM_TYPE,"0")));//TVScanParams.SEARCH_OPTION_ALL
        gposInfo.setSearchMode(Integer.parseInt(getKeyValue(GposInfo.GPOS_SEARCH_MODE,"0")));//TVScanParams.SEARCH_OPTION_ALL
        gposInfo.setAudioLanguageSelection(0,getKeyValue(GposInfo.GPOS_AUDIO_LANG_SELECT_1,"eng"));
        gposInfo.setAudioLanguageSelection(1,getKeyValue(GposInfo.GPOS_AUDIO_LANG_SELECT_2,"eng"));
        gposInfo.setSubtitleLanguageSelection(0,getKeyValue(GposInfo.GPOS_SUBTITLE_LANG_SELECT_1,"eng"));
        gposInfo.setSubtitleLanguageSelection(1,getKeyValue(GposInfo.GPOS_SUBTITLE_LANG_SELECT_2,"eng"));
        gposInfo.setSortByLcn(Integer.parseInt(getKeyValue(GposInfo.GPOS_SORT_BY_LCN,"1")));
        gposInfo.setOSDTransparency(Integer.parseInt(getKeyValue(GposInfo.GPOS_OSD_TRANSPARENCY,"0")));
        gposInfo.setBannerTimeout(Integer.parseInt(getKeyValue(GposInfo.GPOS_BANNER_TIMEOUT,"5")));
        gposInfo.setHardHearing(Integer.parseInt(getKeyValue(GposInfo.GPOS_HARD_HEARING,"1")));
        gposInfo.setAutoStandbyTime(Integer.parseInt(getKeyValue(GposInfo.GPOS_AUTO_STANDBY_TIME,"0")));
        gposInfo.setDolbyMode(Integer.parseInt(getKeyValue(GposInfo.GPOS_DOLBY_MODE,"0")));
        gposInfo.setHDCPOnOff(Integer.parseInt(getKeyValue(GposInfo.GPOS_HDCP_ONOFF,"1")));
        gposInfo.setDeepSleepMode(Integer.parseInt(getKeyValue(GposInfo.GPOS_DEEP_SLEEP_MODE,"0")));
        gposInfo.setSubtitleOnOff(Integer.parseInt(getKeyValue(GposInfo.GPOS_SUBTITLE_ONOFF,"0")));
        gposInfo.setAvStopMode(Integer.parseInt(getKeyValue(GposInfo.GPOS_AV_STOP_MODE,"1")));
        gposInfo.setTimeshiftDuration(Integer.parseInt(getKeyValue(GposInfo.GPOS_TIMESHIFT_DURATION,"5400")));//90*60
        gposInfo.setRecordIconOnOff(Integer.parseInt(getKeyValue(GposInfo.GPOS_RECORD_ICON_ONOFF,"1")));
        gposInfo.setMailSettingsShopping(Integer.parseInt(getKeyValue(GposInfo.GPOS_MAIL_SETTINGS_SHOPPING,"1")));
        gposInfo.setMailSettingsNews(Integer.parseInt(getKeyValue(GposInfo.GPOS_MAIL_SETTINGS_NEWS,"1")));
        gposInfo.setMailSettingsPopular(Integer.parseInt(getKeyValue(GposInfo.GPOS_MAIL_SETTINGS_POPULAR,"1")));
        gposInfo.setMailSettingsCoupon(Integer.parseInt(getKeyValue(GposInfo.GPOS_MAIL_SETTINGS_COUPON,"1")));
        gposInfo.setMailSettingsService(Integer.parseInt(getKeyValue(GposInfo.GPOS_MAIL_SETTINGS_SERVICE,"1")));
        gposInfo.setChannelLockCount(Integer.parseInt(getKeyValue(GposInfo.GPOS_CHANNEL_LOCK_COUNT,"0")));
        gposInfo.setNitVersion(Integer.parseInt(getKeyValue(GposInfo.GPOS_NIT_VERSION,"0")));
        gposInfo.setNitId(Integer.parseInt(getKeyValue(GposInfo.GPOS_NIT_ID,"0")));
        gposInfo.setSINitNetworkId(Integer.parseInt(getKeyValue(GposInfo.GPOS_SI_NIT_ID,"0")));
        gposInfo.setBatVersion(Integer.parseInt(getKeyValue(GposInfo.GPOS_BAT_VERSION,"0")));
        gposInfo.setBatId(Integer.parseInt(getKeyValue(GposInfo.GPOS_BAT_ID,"0")));
        gposInfo.setTimeLockPeriodStart(0,Integer.parseInt(getKeyValue(GposInfo.GPOS_TIME_LOCK_PERIOD_1_START,"-1")));
        gposInfo.setTimeLockPeriodStart(1,Integer.parseInt(getKeyValue(GposInfo.GPOS_TIME_LOCK_PERIOD_2_START,"-1")));
        gposInfo.setTimeLockPeriodStart(2,Integer.parseInt(getKeyValue(GposInfo.GPOS_TIME_LOCK_PERIOD_3_START,"-1")));
        gposInfo.setTimeLockPeriodEnd(0,Integer.parseInt(getKeyValue(GposInfo.GPOS_TIME_LOCK_PERIOD_1_END,"-1")));
        gposInfo.setTimeLockPeriodEnd(1,Integer.parseInt(getKeyValue(GposInfo.GPOS_TIME_LOCK_PERIOD_2_END,"-1")));
        gposInfo.setTimeLockPeriodEnd(2,Integer.parseInt(getKeyValue(GposInfo.GPOS_TIME_LOCK_PERIOD_3_END,"-1")));
        gposInfo.setIntroductionTime(Integer.parseInt(getKeyValue(GposInfo.GPOS_INTRODUCTION_TIME,"5000")));
        gposInfo.setZipCode(getKeyValue(GposInfo.GPOS_ZIPCODE,"0"));
        gposInfo.setPvrEnable(Integer.parseInt(getKeyValue(GposInfo.GPOS_PVRENABLE,"0")));
        gposInfo.setHomeId(getKeyValue(GposInfo.GPOS_HOME_ID,"0"));
        gposInfo.setSo(getKeyValue(GposInfo.GPOS_SO,""));
        gposInfo.setMobile(getKeyValue(GposInfo.GPOS_MOBILE,""));
        gposInfo.setStandbyRedirect(Integer.parseInt(getKeyValue(GposInfo.GPOS_STANDBY_REDIRECT,"0")));
        gposInfo.setPvrEnable(Integer.parseInt(getKeyValue(GposInfo.GPOS_PVRENABLE,"0")));
        gposInfo.setMailNotifyStatus(Integer.parseInt(getKeyValue(GposInfo.GPOS_MAIL_NOTIFY_STATUS, "1")));
        gposInfo.setSearchOneTPFlag(Integer.parseInt(getKeyValue(GposInfo.GPOS_SEARCH_ONE_TP_FLAG,"0")));
        gposInfo.setIsMute(Integer.parseInt(getKeyValue(GposInfo.GPOS_ISMUTE,"0")));
        gposInfo.setDVRManagementLimit(Integer.parseInt(getKeyValue(GposInfo.GPOS_DVR_MANAGEMENT_LIMIT,"0")));
        for(int i =0 ; i<GposInfo.MAX_NUM_OF_IRD_COMMAND; i++){
            String key = GposInfo.GPOS_IrdCommand+i, value;
            value = getKeyValue(key,"0");
            gposInfo.setIrdCommand(Long.parseLong(value));
        }
        gposInfo.setWVCasLicenseURL(getKeyValue(GposInfo.GPOS_WVCAS_LICENSE_URL,""));
        gposInfo.setOta_md5(getKeyValue(GposInfo.GPOS_OTA_MD5, "0"));
        gposInfo.setSTBRefCasDataTime(Integer.parseInt(getKeyValue(GposInfo.GPOS_WVCAS_REF_CASDATA_TIME,"28800")));
        LogUtils.d("STBRefCasDataTime = "+gposInfo.getSTBRefCasDataTime());
        return gposInfo;
    }
}
