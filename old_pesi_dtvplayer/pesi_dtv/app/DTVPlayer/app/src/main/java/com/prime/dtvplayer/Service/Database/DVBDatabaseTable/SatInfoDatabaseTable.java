package com.prime.dtvplayer.Service.Database.DVBDatabaseTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prime.dtvplayer.Database.DBUtils;
import com.prime.dtvplayer.Service.Database.DVBContentProvider;
import com.prime.dtvplayer.Sysdata.SatInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SatInfoDatabaseTable {
    private static final String TAG = "SatInfoDatabaseTable";
    public static final String TABLE_NAME = "SatInfo";

    public static final Uri CONTENT_URI = Uri.parse("content://" + DVBContentProvider.AUTHORITY + "/" + TABLE_NAME);

    //Type
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.satinfo";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.satinfo";

    private Context mContext;
    DBUtils mDbUtils = new DBUtils();

    public SatInfoDatabaseTable(Context context) {
        mContext = context;
    }


    public static String TableCreate() {
        String cmd = "create table "
                + SatInfoDatabaseTable.TABLE_NAME + " ("
                + "_ID" + " integer primary key autoincrement,"
                + SatInfo.SAT_ID + ","
                + SatInfo.SAT_NAME + ","
                + SatInfo.TUNER_TYPE + ","
                + SatInfo.SAT_TP_NUM + ","
                + SatInfo.ANGLE + ","
                + SatInfo.LOCATION + ","
                + SatInfo.POSTION_INDEX + ","
                + SatInfo.ANGLE_EW + ","
                + SatInfo.ANTENNA + ","
                + SatInfo.TPS
                + ");";
        return cmd;
    }

    public static String TableDrop() {
        String cmd = "DROP TABLE IF EXISTS " + SatInfoDatabaseTable.TABLE_NAME + ";";
        return cmd;
    }

//    public void save(SatInfo satInfo) {
//        SatInfo DBSatInfo = query(satInfo.getSatId());
//        if(DBSatInfo == null) {
//            add(satInfo);
//        }
//        else {
//            update(satInfo);
//        }
//    }

    public void save(List<SatInfo> satInfoList) {
        removeAll();
        for (int i = 0; i < satInfoList.size(); i++) {
            add(satInfoList.get(i));
        }
    }

    public List<SatInfo> load() {
        List<SatInfo> satInfoList = queryList();
        return satInfoList;
    }

    private void add(SatInfo satInfo) {
        ContentValues Value = new ContentValues();
        Value.put(SatInfo.SAT_ID, satInfo.getSatId());
        Value.put(SatInfo.SAT_NAME, satInfo.getSatName());
        Value.put(SatInfo.TUNER_TYPE, satInfo.getTunerType());
        Value.put(SatInfo.SAT_TP_NUM, satInfo.getTpNum());
        Value.put(SatInfo.ANGLE, satInfo.getAngle());
        Value.put(SatInfo.LOCATION, satInfo.getLocation());
        Value.put(SatInfo.POSTION_INDEX, satInfo.getPostionIndex());
        Value.put(SatInfo.ANGLE_EW, satInfo.getAngleEW());
        Value.put(SatInfo.ANTENNA, satInfo.getJsonStringFromAntenna());
        Value.put(SatInfo.TPS, satInfo.getJsonStringFromTps());

        Log.d(TAG, "add "+ satInfo.ToString());
        mContext.getContentResolver().insert(SatInfoDatabaseTable.CONTENT_URI, Value);
    }

    private void update(SatInfo satInfo) {
        int count;
        String where = SatInfo.SAT_ID + " = " + satInfo.getSatId();
        ContentValues Value = new ContentValues();
        Value.put(SatInfo.SAT_NAME, satInfo.getSatName());
        Value.put(SatInfo.TUNER_TYPE, satInfo.getTunerType());
        Value.put(SatInfo.SAT_TP_NUM, satInfo.getTpNum());
        Value.put(SatInfo.ANGLE, satInfo.getAngle());
        Value.put(SatInfo.LOCATION, satInfo.getLocation());
        Value.put(SatInfo.POSTION_INDEX, satInfo.getPostionIndex());
        Value.put(SatInfo.ANGLE_EW, satInfo.getAngleEW());
        Value.put(SatInfo.ANTENNA, satInfo.getJsonStringFromAntenna());
        Value.put(SatInfo.TPS, satInfo.getJsonStringFromTps());

        Log.d(TAG, "update " + satInfo.ToString());
        count = mContext.getContentResolver().update(SatInfoDatabaseTable.CONTENT_URI, Value, where, null);
    }

    public void removeAll() {
        int count;
        count = mContext.getContentResolver().delete(SatInfoDatabaseTable.CONTENT_URI,null,null);
    }

    private List<SatInfo> queryList() {
        Cursor cursor = mContext.getContentResolver().query(SatInfoDatabaseTable.CONTENT_URI, null, null, null, null);
        List<SatInfo> satInfoList = new ArrayList<>();
        SatInfo satInfo = null;
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    satInfo = ParseCursor(cursor);
                    satInfoList.add(satInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return satInfoList;
        }
        return null;
    }

    private SatInfo query(int satId) {
        SatInfo satInfo = null;
        String selection = SatInfo.SAT_ID + " = " + satId;
        Cursor cursor = mContext.getContentResolver().query(SatInfoDatabaseTable.CONTENT_URI, null, selection, null, null);
        Log.d(TAG,"query cursor = "+cursor);
        if(cursor != null) {
            if (cursor.moveToFirst())
                satInfo = ParseCursor(cursor);
            cursor.close();
            return satInfo;
        }
        return null;
    }

    private SatInfo ParseCursor(Cursor cursor){
        SatInfo satInfo;
        satInfo = new SatInfo();
        satInfo.setSatId(mDbUtils.GetIntFromTable(cursor, SatInfo.SAT_ID));
        satInfo.setSatName(mDbUtils.GetStringFromTable(cursor, SatInfo.SAT_NAME));
        satInfo.setTunerType(mDbUtils.GetIntFromTable(cursor, SatInfo.TUNER_TYPE));
        satInfo.setTpNum(mDbUtils.GetIntFromTable(cursor, SatInfo.SAT_TP_NUM));
        satInfo.setAngle(mDbUtils.GetFloatFromTable(cursor, SatInfo.ANGLE));
        satInfo.setLocation(mDbUtils.GetIntFromTable(cursor, SatInfo.LOCATION));
        satInfo.setPostionIndex(mDbUtils.GetIntFromTable(cursor, SatInfo.POSTION_INDEX));
        satInfo.setAngleEW(mDbUtils.GetIntFromTable(cursor, SatInfo.ANGLE_EW));
        satInfo.Antenna= satInfo.getAntennaFromJsonString(mDbUtils.GetStringFromTable(cursor, SatInfo.ANTENNA));
        satInfo.setTps(satInfo.getTpsFromJsonString(mDbUtils.GetStringFromTable(cursor, SatInfo.TPS)));
        Log.i(TAG, "mDbUtils.GetIntFromTable(cursor, _ID) = "+mDbUtils.GetIntFromTable(cursor, "_ID") +
                " sat id = "+satInfo.getSatId());
        return satInfo;
    }

}
