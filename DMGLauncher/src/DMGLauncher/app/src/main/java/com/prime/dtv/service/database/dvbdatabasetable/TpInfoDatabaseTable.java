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
import com.prime.dtv.sysdata.TpInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TpInfoDatabaseTable {
    private static final String TAG = "TpInfoDatabaseTable";
    public static final String TABLE_NAME = "TpInfo";

    public static final Uri CONTENT_URI = Uri.parse("content://" + DVBContentProvider.AUTHORITY + "/" + TABLE_NAME);

    //Type
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tpinfo";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.tpinfo";

    private Context mContext;
    DBUtils mDbUtils = new DBUtils();

    public TpInfoDatabaseTable(Context context) {
        mContext = context;
    }


    public static String TableCreate() {
        String cmd = "create table "
                + TpInfoDatabaseTable.TABLE_NAME + " ("
                + "_ID" + " integer primary key autoincrement,"
                + TpInfo.TP_ID + " integer UNIQUE,"
                + TpInfo.SAT_ID + ","
                + TpInfo.TUNER_TYPE + ","
                + TpInfo.NETWORK_ID + ","
                + TpInfo.TRANSPORT_ID + ","
                + TpInfo.ORIGNAL_ID + ","
                + TpInfo.TUNER_ID + ","
                + TpInfo.TP_SDT_VERSION + ","
                + TpInfo.TERR_TP_JSON + ","
                + TpInfo.SAT_TP_JSON + ","
                + TpInfo.CABLE_TP_JSON
                + ");";
        return cmd;
    }

    public static String TableDrop() {
        String cmd = "DROP TABLE IF EXISTS " + TpInfoDatabaseTable.TABLE_NAME + ";";
        return cmd;
    }

    public void save(TpInfo tpInfo) {
        TpInfo DBtpInfo = query(tpInfo.getTpId());
        if(DBtpInfo == null) {
            add(tpInfo);
        }
        else {
            update(tpInfo);
        }
    }

    public static final DVBDatabase.SQLiteBinder<TpInfo> BINDER = (stmt, tp) -> {
        int i = 1;
        stmt.bindLong(i++, tp.getTpId());
        stmt.bindLong(i++, tp.getSatId());
        stmt.bindLong(i++, tp.getTunerType());
        stmt.bindLong(i++, tp.getNetwork_id());
        stmt.bindLong(i++, tp.getTransport_id());
        stmt.bindLong(i++, tp.getOrignal_network_id());
        stmt.bindLong(i++, tp.getTuner_id());
        stmt.bindLong(i++, tp.getSdt_version());

        // 子結構序列化（若為 null 則存空字串）
        stmt.bindString(i++, tp.TerrTp != null ? tp.getJsonStringFromTerrTP() : "");
        stmt.bindString(i++, tp.SatTp != null ? tp.getJsonStringFromSatTP() : "");
        stmt.bindString(i++, tp.CableTp != null ? tp.getJsonStringFromCableTP() : "");
    };

    public void save(List<TpInfo> tpInfoList) {
        String SQL = "INSERT OR REPLACE INTO "+TABLE_NAME+" (" +
                ""+TpInfo.TP_ID+", "+TpInfo.SAT_ID+", "+TpInfo.TUNER_TYPE+", "+TpInfo.NETWORK_ID+", "+TpInfo.TRANSPORT_ID+
                ", "+TpInfo.ORIGNAL_ID+", "+TpInfo.TUNER_ID+", "+TpInfo.TP_SDT_VERSION+", " +
                ""+TpInfo.TERR_TP_JSON+", "+TpInfo.SAT_TP_JSON+", "+TpInfo.CABLE_TP_JSON+") " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        DVBContentProvider.getDvbDb().saveListWithUpsert(
                TABLE_NAME,
                new String[]{TpInfo.TP_ID},
                tpInfoList,
                SQL,
                BINDER,
                s -> new DVBDatabase.KeyTuple(s.getSatId())
        );
    }

    public List<TpInfo> load() {
        List<TpInfo> tpInfoList = queryList();
        return tpInfoList;
    }

    private ContentValues getTpInfoContentValues(TpInfo tpInfo) {
        ContentValues Value = new ContentValues();
        Value.put(TpInfo.TP_ID, tpInfo.getTpId());
        Value.put(TpInfo.SAT_ID, tpInfo.getSatId());
        Value.put(TpInfo.TUNER_TYPE, tpInfo.getTunerType());
        Value.put(TpInfo.NETWORK_ID, tpInfo.getNetwork_id());
        Value.put(TpInfo.TRANSPORT_ID, tpInfo.getTransport_id());
        Value.put(TpInfo.ORIGNAL_ID, tpInfo.getOrignal_network_id());
        Value.put(TpInfo.TUNER_ID, tpInfo.getTuner_id());
        Value.put(TpInfo.TERR_TP_JSON, tpInfo.getJsonStringFromTerrTP());
        Value.put(TpInfo.SAT_TP_JSON, tpInfo.getJsonStringFromSatTP());
        Value.put(TpInfo.CABLE_TP_JSON, tpInfo.getJsonStringFromCableTP());
        Value.put(TpInfo.TP_SDT_VERSION, tpInfo.getSdt_version());
        //Log.d(TAG, "getTpInfoContentValues "+ tpInfo.ToString());
        return Value;
    }

    private void add(TpInfo tpInfo) {
        ContentValues Value = getTpInfoContentValues(tpInfo);
//        Log.d(TAG, "gggg add "+ tpInfo.ToString());
        mContext.getContentResolver().insert(TpInfoDatabaseTable.CONTENT_URI, Value);
    }

    private void update(TpInfo tpInfo) {
        int count;
        String where = TpInfo.TP_ID + " = " + tpInfo.getTpId();
        ContentValues Value = getTpInfoContentValues(tpInfo);

//        Log.d(TAG, "gggg update " + tpInfo.ToString());
        count = mContext.getContentResolver().update(TpInfoDatabaseTable.CONTENT_URI, Value, where, null);
    }

    public void removeAll() {
        int count;
        count = mContext.getContentResolver().delete(TpInfoDatabaseTable.CONTENT_URI,null,null);
    }

    private List<TpInfo> queryList() {
        Cursor cursor = mContext.getContentResolver().query(TpInfoDatabaseTable.CONTENT_URI, null, null, null, null);
        List<TpInfo> tpInfoList = new ArrayList<>();
        TpInfo tpInfo = null;
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    tpInfo = ParseCursor(cursor);
                    tpInfoList.add(tpInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return tpInfoList;
        }

        // return empty list instead of null
        return tpInfoList;
    }

    private TpInfo query(int tpId) {
        TpInfo tpInfo = null;
        String selection = TpInfo.TP_ID + " = " + tpId;
        Cursor cursor = mContext.getContentResolver().query(TpInfoDatabaseTable.CONTENT_URI, null, selection, null, null);
        Log.d(TAG,"query cursor = "+cursor);
        if(cursor != null) {
            if (cursor.moveToFirst())
                tpInfo = ParseCursor(cursor);
            cursor.close();
            return tpInfo;
        }
        return null;
    }

    private TpInfo ParseCursor(Cursor cursor){
        TpInfo tpInfo;
        int tunerType = mDbUtils.GetIntFromTable(TABLE_NAME,cursor,TpInfo.TUNER_TYPE);
        tpInfo = new TpInfo(tunerType);
        tpInfo.setTpId(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, TpInfo.TP_ID));
        tpInfo.setSatId(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, TpInfo.SAT_ID));
        tpInfo.setTunerType(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, TpInfo.TUNER_TYPE));
        tpInfo.setNetwork_id(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, TpInfo.NETWORK_ID));
        tpInfo.setTransport_id(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, TpInfo.TRANSPORT_ID));
        tpInfo.setOrignal_network_id(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, TpInfo.ORIGNAL_ID));
        tpInfo.setTuner_id(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, TpInfo.TUNER_ID));
        if(tunerType == TpInfo.DVBT) {
            tpInfo.TerrTp = tpInfo.getTerrTPFromJsonString(mDbUtils.GetStringFromTable(TABLE_NAME,cursor,TpInfo.TERR_TP_JSON));
        }
        else if(tunerType == TpInfo.DVBC) {
            tpInfo.CableTp = tpInfo.getCableTPFromJsonString(mDbUtils.GetStringFromTable(TABLE_NAME,cursor,TpInfo.CABLE_TP_JSON));
        }
        else if(tunerType == TpInfo.DVBS) {
            tpInfo.SatTp = tpInfo.getSatTPFromJsonString(mDbUtils.GetStringFromTable(TABLE_NAME,cursor,TpInfo.SAT_TP_JSON));
        }
        //Log.i(TAG, "mDbUtils.GetIntFromTable(cursor, _ID) = "+mDbUtils.GetIntFromTable(TABLE_NAME, cursor, "_ID") +
        //        " tp id = "+tpInfo.getTpId());
        tpInfo.setSdt_version(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, TpInfo.TP_SDT_VERSION));
        return tpInfo;
    }
}
