package com.prime.dtv.service.database.dvbdatabasetable;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.prime.dtv.database.DBUtils;
import com.prime.datastructure.sysdata.CNSEasData;
import com.prime.dtv.service.database.DVBContentProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class CNSEasDatabaseTable {
    private static final String TAG = "CNSEasDatabaseTable";

    public static final String TABLE_NAME = "CNS_Eas";

    public Uri CONTENT_URI = null;

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cns_eas";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cns_eas";

    private Context mContext;
    DBUtils mDbUtils = new DBUtils();

    public CNSEasDatabaseTable(Context context) {
        mContext = context;
        CONTENT_URI = Uri.parse("content://" + DVBContentProvider.getDVBContentProvider(context).getAuthority() + "/" + TABLE_NAME);
    }

    public static String TableCreate() {
        return "create table " + TABLE_NAME + " ("
                + "_ID integer primary key autoincrement,"
                + CNSEasData.ID + " INTEGER,"           // 新增：邏輯 ID
                + CNSEasData.REPEAT_COUNT + " INTEGER,"
                + CNSEasData.FOREGROUND_COLOR + " INTEGER,"
                + CNSEasData.BACKGROUND_COLOR + " INTEGER,"
                + CNSEasData.ALERT_LEVEL + " INTEGER,"
                + CNSEasData.ALERT_TYPE + " INTEGER,"
                + CNSEasData.ALERT_SOUND + " INTEGER,"
                + CNSEasData.START_TIME + " INTEGER,"
                + CNSEasData.END_TIME + " INTEGER,"
                + CNSEasData.ALERT_TITLE + " TEXT,"
                + CNSEasData.ALERT_MESSAGE + " TEXT,"
                + CNSEasData.FORCE_TUNE_ON_ID + " INTEGER,"
                + CNSEasData.FORCE_TUNE_TS_ID + " INTEGER,"
                + CNSEasData.FORCE_TUNE_SERVICE_ID + " INTEGER,"
                + CNSEasData.RECEIVE_TIME + " INTEGER,"
                + CNSEasData.READ + " INTEGER,"          // 新增：已讀狀態
                + CNSEasData.ALREADY_SHOWN + " INTEGER"  // 新增：已顯示狀態
                + ");";
    }

    public static String TableDrop() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    }

    // 儲存單筆 (自動判斷新增或更新)
    public void save(CNSEasData easData) {
        CNSEasData temp = query(easData.getId());
        if (temp == null) {
            add(easData);
        } else {
            update(easData);
        }
    }

    // 儲存列表 (批次處理：新增、更新、刪除)
    public void save(List<CNSEasData> easDataList) {
        List<CNSEasData> dbEasDataList = queryList(); // 取得目前 DB 內的所有資料
        Map<Integer, CNSEasData> dbEasDataMap = new HashMap<>();
        if (dbEasDataList != null) {
            for (CNSEasData easData : dbEasDataList) {
                dbEasDataMap.put(easData.getId(), easData);
            }
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (CNSEasData newEasData : easDataList) {
            CNSEasData existingEasData = dbEasDataMap.get(newEasData.getId());
            if (existingEasData != null) {
                // 資料已存在，進行更新
                ContentValues values = getCNSEasDataContentValues(newEasData); // 使用更新後的 getContentValues
                String where = "CAST(" + CNSEasData.ID + " AS TEXT) = ?";
                String[] selectionArgs = {String.valueOf(newEasData.getId())};
                operations.add(ContentProviderOperation.newUpdate(CONTENT_URI)
                        .withValues(values)
                        .withSelection(where, selectionArgs)
                        .build());
                dbEasDataMap.remove(newEasData.getId()); // 從 Map 移除，表示已處理
                Log.d(TAG, "update getId " + newEasData.getId());
            } else {
                // 新資料，進行新增
                ContentValues values = getCNSEasDataContentValues(newEasData);
                operations.add(ContentProviderOperation.newInsert(CONTENT_URI)
                        .withValues(values)
                        .build());
                Log.d(TAG, "add getId " + newEasData.getId());
            }
        }

        // 剩下的 Map 成員表示 Server 端已刪除，DB 也需同步刪除
        for (Integer easDataIdToRemove : dbEasDataMap.keySet()) {
            String where = "CAST(" + CNSEasData.ID + " AS TEXT) = ?";
            String[] selectionArgs = {String.valueOf(easDataIdToRemove)};
            operations.add(ContentProviderOperation.newDelete(CONTENT_URI)
                    .withSelection(where, selectionArgs)
                    .build());
            Log.d(TAG, "remove getId " + easDataIdToRemove);
        }

        if (!operations.isEmpty()) {
            try {
                final int BATCH_SIZE = 100;
                for (int i = 0; i < operations.size(); i += BATCH_SIZE) {
                    int end = Math.min(i + BATCH_SIZE, operations.size());
                    List<ContentProviderOperation> subBatch = operations.subList(i, end);
                    mContext.getContentResolver().applyBatch(DVBContentProvider.getDVBContentProvider(mContext).getAuthority(), new ArrayList<>(subBatch));
                }
                Log.d(TAG, "Batch operations applied successfully.");
            } catch (OperationApplicationException | RemoteException e) {
                Log.e(TAG, "Error applying batch operations", e);
            }
        }
        Log.d(TAG, "save CNSEasData list end");
    }

    // 載入全部
    public List<CNSEasData> load() {
        return queryList();
    }

    // 查詢單筆
    public CNSEasData query(int id) {
        CNSEasData data = null;
        String selection = CNSEasData.ID + " = " + id;
        Cursor cursor = mContext.getContentResolver().query(CONTENT_URI, null, selection, null, null);
        Log.d(TAG, "query cursor = " + cursor);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                data = parseCursor(cursor);
            }
            cursor.close();
        }
        return data;
    }

    // 更新單筆
    public void update(CNSEasData easData) {
        String where = CNSEasData.ID + " = " + easData.getId();
        ContentValues values = getCNSEasDataContentValues(easData);

        Log.d(TAG, "update " + easData.toString());
        mContext.getContentResolver().update(CONTENT_URI, values, where, null);
    }

    // 刪除全部
    public void removeAll() {
        mContext.getContentResolver().delete(CONTENT_URI, null, null);
    }

    // 刪除單筆 (By Object)
    public void remove(CNSEasData easData) {
        String where = CNSEasData.ID + " = " + easData.getId();
        Log.d(TAG, "remove CNSEasData.ID = " + easData.getId());
        mContext.getContentResolver().delete(CONTENT_URI, where, null);
    }

    // 刪除單筆 (By ID)
    public void remove(int easId) {
        String where = CNSEasData.ID + " = " + easId;
        Log.d(TAG, "remove easId = " + easId);
        mContext.getContentResolver().delete(CONTENT_URI, where, null);
    }

    // ==========================================
    //  原有的功能 (已修正支援新欄位)
    // ==========================================

    public void add(CNSEasData easData) {
        ContentValues values = getCNSEasDataContentValues(easData);
        Log.d(TAG, "add " + easData.toString());
        try {
            mContext.getContentResolver().insert(CONTENT_URI, values);
        } catch (Exception e) {
            Log.e(TAG, "Insert failed", e);
        }
    }

    private List<CNSEasData> queryList() {
        List<CNSEasData> list = new ArrayList<>();
        Cursor cursor = mContext.getContentResolver().query(CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(parseCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    // ★ 修改：轉成 ContentValues (補上 ID, READ, ALREADY_SHOWN)
    private ContentValues getCNSEasDataContentValues(CNSEasData data) {
        ContentValues values = new ContentValues();
        values.put(CNSEasData.ID, data.getId()); // 記得存 ID
        values.put(CNSEasData.REPEAT_COUNT, data.getRepeatCount());
        values.put(CNSEasData.FOREGROUND_COLOR, data.getForegroundColor());
        values.put(CNSEasData.BACKGROUND_COLOR, data.getBackgroundColor());
        values.put(CNSEasData.ALERT_LEVEL, data.getAlertLevel());
        values.put(CNSEasData.ALERT_TYPE, data.getAlertType());
        values.put(CNSEasData.ALERT_SOUND, data.getAlertSound());
        values.put(CNSEasData.START_TIME, data.getStartTime());
        values.put(CNSEasData.END_TIME, data.getEndTime());
        values.put(CNSEasData.ALERT_TITLE, data.getAlertTitle());
        values.put(CNSEasData.ALERT_MESSAGE, data.getAlertMessage());
        values.put(CNSEasData.FORCE_TUNE_ON_ID, data.getForceTune_on_id());
        values.put(CNSEasData.FORCE_TUNE_TS_ID, data.getForceTune_ts_id());
        values.put(CNSEasData.FORCE_TUNE_SERVICE_ID, data.getForceTune_service_id());
        values.put(CNSEasData.RECEIVE_TIME, data.getReceiveTime());
        values.put(CNSEasData.READ, data.getRead());
        values.put(CNSEasData.ALREADY_SHOWN, data.getAlreadyShown());
        return values;
    }

    // ★ 修改：從 Cursor 解析資料 (補上 ID, READ, ALREADY_SHOWN)
    private CNSEasData parseCursor(Cursor cursor) {
        CNSEasData data = new CNSEasData();
        // 這裡取出邏輯 ID (CNSEasData.ID)，而非 DB 的 _ID
        data.setId(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSEasData.ID));
        data.setRepeatCount(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSEasData.REPEAT_COUNT));
        data.setForegroundColor(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSEasData.FOREGROUND_COLOR));
        data.setBackgroundColor(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSEasData.BACKGROUND_COLOR));
        data.setAlertLevel(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSEasData.ALERT_LEVEL));
        data.setAlertType(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSEasData.ALERT_TYPE));
        data.setAlertSound(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSEasData.ALERT_SOUND));
        data.setStartTime(mDbUtils.GetLongFromTable(TABLE_NAME, cursor, CNSEasData.START_TIME));
        data.setEndTime(mDbUtils.GetLongFromTable(TABLE_NAME, cursor, CNSEasData.END_TIME));


        data.setAlertTitle(mDbUtils.GetStringFromTable(TABLE_NAME, cursor, CNSEasData.ALERT_TITLE));
        data.setAlertMessage(mDbUtils.GetStringFromTable(TABLE_NAME, cursor, CNSEasData.ALERT_MESSAGE));
        data.setForceTune_on_id(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSEasData.FORCE_TUNE_ON_ID));
        data.setForceTune_ts_id(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSEasData.FORCE_TUNE_TS_ID));
        data.setForceTune_service_id(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSEasData.FORCE_TUNE_SERVICE_ID));
        data.setReceiveTime(mDbUtils.GetLongFromTable(TABLE_NAME, cursor, CNSEasData.RECEIVE_TIME));
        data.setRead(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSEasData.READ));
        data.setAlreadyShown(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSEasData.ALREADY_SHOWN));

        return data;
    }
}
