package com.prime.dtvplayer.Service.Database.DVBDatabaseTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.prime.dtvplayer.Database.DBUtils;
import com.prime.dtvplayer.Service.Database.DVBContentProvider;
import com.prime.dtvplayer.Sysdata.TpInfo;

import java.util.ArrayList;
import java.util.List;

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
                + TpInfo.TP_ID + ","
                + TpInfo.SAT_ID + ","
                + TpInfo.TUNER_TYPE + ","
                + TpInfo.NETWORK_ID + ","
                + TpInfo.TRANSPORT_ID + ","
                + TpInfo.ORIGNAL_ID + ","
                + TpInfo.TUNER_ID + ","
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

//    public void save(TpInfo tpInfo) {
//        TpInfo DBtpInfo = query(tpInfo.getTpId());
//        if(DBtpInfo == null) {
//            add(tpInfo);
//        }
//        else {
//            update(tpInfo);
//        }
//    }

    public void save(List<TpInfo> tpInfoList) {
        removeAll();
        for (int i = 0; i < tpInfoList.size(); i++) {
            add(tpInfoList.get(i));
        }
    }

    public List<TpInfo> load() {
        List<TpInfo> tpInfoList = queryList();
        return tpInfoList;
    }

    private void add(TpInfo tpInfo) {
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

        Log.d(TAG, "add "+ tpInfo.ToString());
        mContext.getContentResolver().insert(TpInfoDatabaseTable.CONTENT_URI, Value);
    }

    private void update(TpInfo tpInfo) {
        int count;
        String where = TpInfo.TP_ID + " = " + tpInfo.getTpId();
        ContentValues Value = new ContentValues();
        Value.put(TpInfo.SAT_ID, tpInfo.getSatId());
        Value.put(TpInfo.TUNER_TYPE, tpInfo.getTunerType());
        Value.put(TpInfo.NETWORK_ID, tpInfo.getNetwork_id());
        Value.put(TpInfo.TRANSPORT_ID, tpInfo.getTransport_id());
        Value.put(TpInfo.ORIGNAL_ID, tpInfo.getOrignal_network_id());
        Value.put(TpInfo.TUNER_ID, tpInfo.getTuner_id());
        Value.put(TpInfo.TERR_TP_JSON, tpInfo.getJsonStringFromTerrTP());
        Value.put(TpInfo.SAT_TP_JSON, tpInfo.getJsonStringFromSatTP());
        Value.put(TpInfo.CABLE_TP_JSON, tpInfo.getJsonStringFromCableTP());

        Log.d(TAG, "update " + tpInfo.ToString());
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
        return null;
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
        int tunerType = mDbUtils.GetIntFromTable(cursor,TpInfo.TUNER_TYPE);
        tpInfo = new TpInfo(tunerType);
        tpInfo.setTpId(mDbUtils.GetIntFromTable(cursor, TpInfo.TP_ID));
        tpInfo.setSatId(mDbUtils.GetIntFromTable(cursor, TpInfo.SAT_ID));
        tpInfo.setTunerType(mDbUtils.GetIntFromTable(cursor, TpInfo.TUNER_TYPE));
        tpInfo.setNetwork_id(mDbUtils.GetIntFromTable(cursor, TpInfo.NETWORK_ID));
        tpInfo.setTransport_id(mDbUtils.GetIntFromTable(cursor, TpInfo.TRANSPORT_ID));
        tpInfo.setOrignal_network_id(mDbUtils.GetIntFromTable(cursor, TpInfo.ORIGNAL_ID));
        tpInfo.setTuner_id(mDbUtils.GetIntFromTable(cursor, TpInfo.TUNER_ID));
        if(tunerType == TpInfo.DVBT) {
            tpInfo.TerrTp = tpInfo.getTerrTPFromJsonString(mDbUtils.GetStringFromTable(cursor,TpInfo.TERR_TP_JSON));
        }
        else if(tunerType == TpInfo.DVBC) {
            tpInfo.CableTp = tpInfo.getCableTPFromJsonString(mDbUtils.GetStringFromTable(cursor,TpInfo.CABLE_TP_JSON));
        }
        else if(tunerType == TpInfo.DVBS) {
            tpInfo.SatTp = tpInfo.getSatTPFromJsonString(mDbUtils.GetStringFromTable(cursor,TpInfo.SAT_TP_JSON));
        }
        Log.i(TAG, "mDbUtils.GetIntFromTable(cursor, _ID) = "+mDbUtils.GetIntFromTable(cursor, "_ID") +
                " tp id = "+tpInfo.getTpId());
        return tpInfo;
    }
}
