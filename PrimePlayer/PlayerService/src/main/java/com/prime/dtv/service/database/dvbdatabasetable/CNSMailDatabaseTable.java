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
import com.prime.datastructure.sysdata.CNSMailData;
import com.prime.dtv.service.database.DVBContentProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CNSMailDatabaseTable {
    private static final String TAG = "CNSMailDatabaseTable";

    public static final String TABLE_NAME = "CNS_Mail";

    public Uri CONTENT_URI = null;

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cns_mail";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cns_mail";

    private Context mContext;
    DBUtils mDbUtils = new DBUtils();

    public CNSMailDatabaseTable(Context context) {
        mContext = context;
        CONTENT_URI = Uri.parse("content://" + DVBContentProvider.getDVBContentProvider(context).getAuthority() + "/" + TABLE_NAME);
    }

    // ★ 修改：新增 ID, READ, ALREADY_SHOWN 欄位到資料表結構
    public static String TableCreate() {
        return "create table " + TABLE_NAME + " ("
                + "_ID integer primary key autoincrement,"
                + CNSMailData.ID + " INTEGER,"           // 新增：邏輯 ID
                + CNSMailData.TITLE + " TEXT,"
                + CNSMailData.BODY + " TEXT,"
                + CNSMailData.IMPORTANCE + " INTEGER,"
                + CNSMailData.RECEIVE_TIME + " INTEGER,"
                + CNSMailData.READ + " INTEGER,"          // 新增：已讀狀態
                + CNSMailData.ALREADY_SHOWN + " INTEGER"  // 新增：已顯示狀態
                + ");";
    }

    public static String TableDrop() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    }

    // ==========================================
    //  新增的功能：仿照 MailDatabaseTable.java
    // ==========================================

    // 儲存單筆 (自動判斷新增或更新)
    public void save(CNSMailData mailData) {
        CNSMailData temp = query(mailData.getId());
        if (temp == null) {
            add(mailData);
        } else {
            update(mailData);
        }
    }

    // 儲存列表 (批次處理：新增、更新、刪除)
    public void save(List<CNSMailData> mailDataList) {
        List<CNSMailData> dbMailDataList = queryList(); // 取得目前 DB 內的所有資料
        Map<Integer, CNSMailData> dbMailDataMap = new HashMap<>();
        if (dbMailDataList != null) {
            for (CNSMailData mailData : dbMailDataList) {
                dbMailDataMap.put(mailData.getId(), mailData);
            }
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (CNSMailData newMailData : mailDataList) {
            CNSMailData existingMailData = dbMailDataMap.get(newMailData.getId());
            if (existingMailData != null) {
                // 資料已存在，進行更新
                ContentValues values = getCNSMailDataContentValues(newMailData); // 使用更新後的 getContentValues
                String where = "CAST(" + CNSMailData.ID + " AS TEXT) = ?";
                String[] selectionArgs = {String.valueOf(newMailData.getId())};
                operations.add(ContentProviderOperation.newUpdate(CONTENT_URI)
                        .withValues(values)
                        .withSelection(where, selectionArgs)
                        .build());
                dbMailDataMap.remove(newMailData.getId()); // 從 Map 移除，表示已處理
                Log.d(TAG, "update getId " + newMailData.getId());
            } else {
                // 新資料，進行新增
                ContentValues values = getCNSMailDataContentValues(newMailData);
                operations.add(ContentProviderOperation.newInsert(CONTENT_URI)
                        .withValues(values)
                        .build());
                Log.d(TAG, "add getId " + newMailData.getId());
            }
        }

        // 剩下的 Map 成員表示 Server 端已刪除，DB 也需同步刪除
        for (Integer mailDataIdToRemove : dbMailDataMap.keySet()) {
            String where = "CAST(" + CNSMailData.ID + " AS TEXT) = ?";
            String[] selectionArgs = {String.valueOf(mailDataIdToRemove)};
            operations.add(ContentProviderOperation.newDelete(CONTENT_URI)
                    .withSelection(where, selectionArgs)
                    .build());
            Log.d(TAG, "remove getId " + mailDataIdToRemove);
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
        Log.d(TAG, "save CNSMailData list end");
    }

    // 載入全部
    public List<CNSMailData> load() {
        return queryList();
    }

    // 查詢單筆
    public CNSMailData query(int id) {
        CNSMailData data = null;
        String selection = CNSMailData.ID + " = " + id;
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
    public void update(CNSMailData mailData) {
        String where = CNSMailData.ID + " = " + mailData.getId();
        ContentValues values = getCNSMailDataContentValues(mailData);

        Log.d(TAG, "update " + mailData.toString());
        mContext.getContentResolver().update(CONTENT_URI, values, where, null);
    }

    // 刪除全部
    public void removeAll() {
        mContext.getContentResolver().delete(CONTENT_URI, null, null);
    }

    // 刪除單筆 (By Object)
    public void remove(CNSMailData mailData) {
        String where = CNSMailData.ID + " = " + mailData.getId();
        Log.d(TAG, "remove CNSMailData.ID = " + mailData.getId());
        mContext.getContentResolver().delete(CONTENT_URI, where, null);
    }

    // 刪除單筆 (By ID)
    public void remove(int mailId) {
        String where = CNSMailData.ID + " = " + mailId;
        Log.d(TAG, "remove mailId = " + mailId);
        mContext.getContentResolver().delete(CONTENT_URI, where, null);
    }

    // ==========================================
    //  原有的功能 (已修正支援新欄位)
    // ==========================================

    public void add(CNSMailData mailData) {
        ContentValues values = getCNSMailDataContentValues(mailData);
        Log.d(TAG, "add " + mailData.toString());
        try {
            mContext.getContentResolver().insert(CONTENT_URI, values);
        } catch (Exception e) {
            Log.e(TAG, "Insert failed", e);
        }
    }

    private List<CNSMailData> queryList() {
        List<CNSMailData> list = new ArrayList<>();
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
    private ContentValues getCNSMailDataContentValues(CNSMailData data) {
        ContentValues values = new ContentValues();
        values.put(CNSMailData.ID, data.getId()); // 記得存 ID
        values.put(CNSMailData.TITLE, data.getTitle());
        values.put(CNSMailData.BODY, data.getBody());
        values.put(CNSMailData.IMPORTANCE, data.getImportance());
        values.put(CNSMailData.RECEIVE_TIME, data.getReceiveTime());
        values.put(CNSMailData.READ, data.getRead());
        values.put(CNSMailData.ALREADY_SHOWN, data.getAlreadyShown());
        return values;
    }

    // ★ 修改：從 Cursor 解析資料 (補上 ID, READ, ALREADY_SHOWN)
    private CNSMailData parseCursor(Cursor cursor) {
        CNSMailData data = new CNSMailData();
        // 這裡取出邏輯 ID (CNSMailData.ID)，而非 DB 的 _ID
        data.setId(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSMailData.ID));
        data.setTitle(mDbUtils.GetStringFromTable(TABLE_NAME, cursor, CNSMailData.TITLE));
        data.setBody(mDbUtils.GetStringFromTable(TABLE_NAME, cursor, CNSMailData.BODY));
        data.setImportance(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSMailData.IMPORTANCE));
        data.setReceiveTime(mDbUtils.GetLongFromTable(TABLE_NAME, cursor, CNSMailData.RECEIVE_TIME));
        data.setRead(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSMailData.READ));
        data.setAlreadyShown(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, CNSMailData.ALREADY_SHOWN));

        return data;
    }
}