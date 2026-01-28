package com.prime.dtv.service.database.dvbdatabasetable;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.prime.dmg.launcher.Mail.Mail;
import com.prime.dmg.launcher.Mail.MailData;
import com.prime.dmg.launcher.Mail.MailManager;
import com.prime.dtv.database.DBUtils;
import com.prime.dtv.service.database.DVBContentProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailDatabaseTable {
    private static final String TAG = "MailDatabaseTable";
    public static final String TABLE_NAME = "Mail";

    public static final Uri CONTENT_URI = Uri.parse("content://" + DVBContentProvider.AUTHORITY + "/" + TABLE_NAME);

    //Type
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mail";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mail";

    private Context mContext;
    DBUtils mDbUtils = new DBUtils();

    public MailDatabaseTable(Context context) {
        mContext = context;
    }


    public static String TableCreate() {
        String cmd = "create table "
                + MailDatabaseTable.TABLE_NAME + " ("
                + "_ID" + " integer primary key autoincrement,"
                + MailData.ID + ","
                + MailData.DATA + ","
                + MailData.READ
                + ");";
        return cmd;
    }

    public static String TableDrop() {
        String cmd = "DROP TABLE IF EXISTS " + MailDatabaseTable.TABLE_NAME + ";";
        return cmd;
    }

    public void save(MailData mailData) {
        MailData mailDataTemp = query(mailData.getId());
        if(mailDataTemp == null) {
            add(mailData);
        }
        else {
            update(mailData);
        }
    }

    public void save(List<MailData> mailDataList) {
        List<MailData> dbMailDataList = queryList(); // Get existing channels
        Map<Integer, MailData> dbMailDataMap = new HashMap<>();
        if (dbMailDataList != null) {
            for (MailData mailData : dbMailDataList) {
                dbMailDataMap.put(mailData.getId(), mailData);
            }
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (MailData newMailData : mailDataList) {
            MailData existingMailData = dbMailDataMap.get(newMailData.getId());
            if (existingMailData != null) {
                // Channel exists, potential update
                // Compare if actual update is needed to avoid unnecessary operations
                // Create update operation
                ContentValues values = getMailDataContentValues(newMailData);
                String where =  "CAST("+ MailData.ID+" AS TEXT) = ?";
                String[] selectionArgs = {String.valueOf(newMailData.getId())};
                operations.add(ContentProviderOperation.newUpdate(CONTENT_URI)
                        .withValues(values)
                        .withSelection(where, selectionArgs)
                        .build());
                dbMailDataMap.remove(newMailData.getId()); // Remove from map as it's processed
                Log.d(TAG, "update getId " + newMailData.getId());
            } else {
                // New channel, create insert operation
                ContentValues values = getMailDataContentValues(newMailData);
                operations.add(ContentProviderOperation.newInsert(CONTENT_URI)
                        .withValues(values)
                        .build());
                Log.d(TAG, "add getId " + newMailData.getId());
            }
        }

        // Any channels left in dbChannelMap are to be deleted
        for (Integer MailDataIdToRemove : dbMailDataMap.keySet()) {
            String where = "CAST("+ MailData.ID+" AS TEXT) = ?";
            String[] selectionArgs = {String.valueOf(MailDataIdToRemove)};
            operations.add(ContentProviderOperation.newDelete(CONTENT_URI)
                    .withSelection(where, selectionArgs)
                    .build());
            Log.d(TAG, "remove getId " + MailDataIdToRemove);
        }

        if (!operations.isEmpty()) {
            try {
                final int BATCH_SIZE = 100;
                for (int i = 0; i < operations.size(); i += BATCH_SIZE) {
                    int end = Math.min(i + BATCH_SIZE, operations.size());
                    List<ContentProviderOperation> subBatch = operations.subList(i, end);
                    mContext.getContentResolver().applyBatch(DVBContentProvider.AUTHORITY, new ArrayList<>(subBatch));
                }

//                mContext.getContentResolver().applyBatch(DVBContentProvider.AUTHORITY, operations);
                Log.d(TAG, "Batch operations applied successfully.");
            } catch (OperationApplicationException | RemoteException e) {
                Log.e(TAG, "Error applying batch operations", e);
                // Handle error appropriately, e.g., revert changes or notify user
            }
        }
        Log.d(TAG, "save MailData list end");
    }

    public List<MailData> load() {
        List<MailData> mailDataList = queryList();
        return mailDataList;
    }

    private ContentValues getMailDataContentValues(MailData mailData) {
        ContentValues Value = new ContentValues();
        Value.put(MailData.ID, mailData.getId());
        Value.put(MailData.DATA, mailData.getData());
        Value.put(MailData.READ, mailData.getRead());
        //Log.d(TAG, "getMailDataContentValues "+ mailData.ToString());
        return Value;
    }

    private void add(MailData mailData) {
        ContentValues Value = getMailDataContentValues(mailData);
        Log.d(TAG, "add "+ mailData.ToString());
        mContext.getContentResolver().insert(MailDatabaseTable.CONTENT_URI, Value);
    }

    public void update(MailData mailData) {
        int count;
        String where = MailData.ID + " = " + mailData.getId();
        ContentValues Value = new ContentValues();
        Value.put(MailData.DATA, mailData.getData());
        Value.put(MailData.READ, mailData.getRead());

        Log.d(TAG, "update " + mailData.ToString());
        count = mContext.getContentResolver().update(MailDatabaseTable.CONTENT_URI, Value, where, null);
    }

    public void removeAll() {
        int count;
        count = mContext.getContentResolver().delete(MailDatabaseTable.CONTENT_URI,null,null);
    }

    public void remove(MailData mailData) {
        int count;
        String where = MailData.ID + " = " + mailData.getId();
        Log.d(TAG, "remove MailData.ID = "+ mailData.getId());
        count = mContext.getContentResolver().delete(MailDatabaseTable.CONTENT_URI, where, null);
    }

    public void remove(int mailId) {
        int count;
        String where = MailData.ID + " = " + mailId;
        Log.d(TAG, "remove mailId = "+ mailId);
        count = mContext.getContentResolver().delete(MailDatabaseTable.CONTENT_URI, where, null);
    }

    private List<MailData> queryList() {
        Cursor cursor = mContext.getContentResolver().query(MailDatabaseTable.CONTENT_URI, null, null, null, null);
        List<MailData> mailDataList = new ArrayList<>();
        MailData mailData = null;
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    mailData = ParseCursor(cursor);
                    mailDataList.add(mailData);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return mailDataList;
        }

        // return empty list instead of null
        return mailDataList;
    }

    public MailData query(int id) {
        MailData mailData = null;
        String selection = MailData.ID + " = " + id;
        Cursor cursor = mContext.getContentResolver().query(MailDatabaseTable.CONTENT_URI, null, selection, null, null);
        Log.d(TAG,"query cursor = "+cursor);
        if(cursor != null) {
            if (cursor.moveToFirst())
                mailData = ParseCursor(cursor);
            cursor.close();
        }
        return mailData;
    }

    private MailData ParseCursor(Cursor cursor){
        MailData mailData = new MailData();
        mailData.setId(mDbUtils.GetIntFromTable(TABLE_NAME, cursor,MailData.ID));
        mailData.setData(mDbUtils.GetStringFromTable(TABLE_NAME,cursor,MailData.DATA));
        mailData.setRead(mDbUtils.GetIntFromTable(TABLE_NAME,cursor,MailData.READ));
        Log.i(TAG, "mDbUtils.GetIntFromTable(cursor, _ID) = "+mDbUtils.GetIntFromTable(TABLE_NAME, cursor, "_ID") +
                " id = "+mailData.getId());
        return mailData;
    }
}
