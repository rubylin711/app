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
import com.prime.dtv.sysdata.SatInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
                + SatInfo.SAT_ID + " integer UNIQUE,"
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

    public static final DVBDatabase.SQLiteBinder<SatInfo> BINDER = (stmt, sat) -> {
        int i = 1;
        stmt.bindLong(i++, sat.getSatId());
        stmt.bindString(i++, sat.getSatName());
        stmt.bindLong(i++, sat.getTunerType());
        stmt.bindLong(i++, sat.getTpNum());
        stmt.bindDouble(i++, sat.getAngle());
        stmt.bindLong(i++, sat.getLocation());
        stmt.bindLong(i++, sat.getPostionIndex());
        stmt.bindLong(i++, sat.getAngleEW());

        // serialize antenna
        stmt.bindString(i++, sat.getJsonStringFromAntenna());

        // serialize tps
        stmt.bindString(i++, sat.getJsonStringFromTps());

    };

    public void save(List<SatInfo> satInfoList) {
        String SQL = "INSERT OR REPLACE INTO "+TABLE_NAME+" (" +
                ""+SatInfo.SAT_ID+", "+SatInfo.SAT_NAME+", "+SatInfo.TUNER_TYPE+", "+SatInfo.SAT_TP_NUM+", "+SatInfo.ANGLE+
                ", "+SatInfo.LOCATION+", "+SatInfo.POSTION_INDEX+", "+SatInfo.ANGLE_EW+","+SatInfo.ANTENNA+", "+SatInfo.TPS +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        DVBContentProvider.getDvbDb().saveListWithUpsert(
                TABLE_NAME,
                new String[]{SatInfo.SAT_ID},
                satInfoList,
                SQL,
                BINDER,
                s -> new DVBDatabase.KeyTuple(s.getSatId())
        );
    }

    public List<SatInfo> load() {
        List<SatInfo> satInfoList = queryList();
        return satInfoList;
    }

    private ContentValues getSatInfoContentValues(SatInfo satInfo) {
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
        //Log.d(TAG, "getSatInfoContentValues "+ satInfo.ToString());
        return Value;
    }

    private void add(SatInfo satInfo) {
        ContentValues Value = getSatInfoContentValues(satInfo);
        mContext.getContentResolver().insert(SatInfoDatabaseTable.CONTENT_URI, Value);
    }

    private void update(SatInfo satInfo) {
        int count;
        String where = SatInfo.SAT_ID + " = " + satInfo.getSatId();
        ContentValues Value = getSatInfoContentValues(satInfo);

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

        }

        // return empty list instead of null
        return satInfoList;
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
        satInfo.setSatId(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, SatInfo.SAT_ID));
        satInfo.setSatName(mDbUtils.GetStringFromTable(TABLE_NAME,cursor, SatInfo.SAT_NAME));
        satInfo.setTunerType(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, SatInfo.TUNER_TYPE));
        satInfo.setTpNum(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, SatInfo.SAT_TP_NUM));
        satInfo.setAngle(mDbUtils.GetFloatFromTable(TABLE_NAME,cursor, SatInfo.ANGLE));
        satInfo.setLocation(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, SatInfo.LOCATION));
        satInfo.setPostionIndex(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, SatInfo.POSTION_INDEX));
        satInfo.setAngleEW(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, SatInfo.ANGLE_EW));
        satInfo.Antenna= satInfo.getAntennaFromJsonString(mDbUtils.GetStringFromTable(TABLE_NAME,cursor, SatInfo.ANTENNA));
        satInfo.setTps(satInfo.getTpsFromJsonString(mDbUtils.GetStringFromTable(TABLE_NAME,cursor, SatInfo.TPS)));
        Log.i(TAG, "mDbUtils.GetIntFromTable(cursor, _ID) = "+mDbUtils.GetIntFromTable(TABLE_NAME,cursor, "_ID") +
                " sat id = "+satInfo.getSatId());
        return satInfo;
    }
}
