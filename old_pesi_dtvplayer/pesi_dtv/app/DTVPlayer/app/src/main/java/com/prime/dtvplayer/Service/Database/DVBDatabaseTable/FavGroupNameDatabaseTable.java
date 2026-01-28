package com.prime.dtvplayer.Service.Database.DVBDatabaseTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.prime.dtvplayer.Database.DBUtils;
import com.prime.dtvplayer.Service.Database.DVBContentProvider;
import com.prime.dtvplayer.Sysdata.FavGroupName;
import com.prime.dtvplayer.Sysdata.TpInfo;

import java.util.ArrayList;
import java.util.List;

public class FavGroupNameDatabaseTable {
    private static final String TAG = "FavGroupNameDatabaseTable";
    public static final String TABLE_NAME = "FavGroupName";

    public static final Uri CONTENT_URI = Uri.parse("content://" + DVBContentProvider.AUTHORITY + "/" + TABLE_NAME);

    //Type
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.favgroupname";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.favgroupname";

    private Context mContext;
    DBUtils mDbUtils = new DBUtils();

    public FavGroupNameDatabaseTable(Context context) {
        mContext = context;
    }


    public static String TableCreate() {
        String cmd = "create table "
                + FavGroupNameDatabaseTable.TABLE_NAME + " ("
                + "_ID" + " integer primary key autoincrement,"
                + FavGroupName.GROUP_TYPE + ","
                + FavGroupName.GROUP_NAME
                + ");";
        return cmd;
    }

    public static String TableDrop() {
        String cmd = "DROP TABLE IF EXISTS " + FavGroupNameDatabaseTable.TABLE_NAME + ";";
        return cmd;
    }

//    public void save(FavGroupName favGroupName) {
//        FavGroupName DBfavGroupName = query(favGroupName.getGroupType());
//        if(DBfavGroupName == null) {
//            add(favGroupName);
//        }
//        else {
//            update(favGroupName);
//        }
//    }

    public void save(List<FavGroupName> favGroupNameList) {
        removeAll();
        for (int i = 0; i < favGroupNameList.size(); i++) {
            add(favGroupNameList.get(i));
        }
    }

    public List<FavGroupName> load() {
        List<FavGroupName> favGroupNameList = queryList();
        return favGroupNameList;
    }

    private void add(FavGroupName favGroupName) {
        ContentValues Value = new ContentValues();
        Value.put(FavGroupName.GROUP_TYPE, favGroupName.getGroupType());
        Value.put(FavGroupName.GROUP_NAME, favGroupName.getGroupName());

        Log.d(TAG, "add "+ favGroupName.ToString());
        mContext.getContentResolver().insert(FavGroupNameDatabaseTable.CONTENT_URI, Value);
    }

    private void update(FavGroupName favGroupName) {
        int count;
        String where = FavGroupName.GROUP_TYPE + " = " + favGroupName.getGroupType();
        ContentValues Value = new ContentValues();
        Value.put(FavGroupName.GROUP_NAME, favGroupName.getGroupName());

        Log.d(TAG, "update " + favGroupName.ToString());
        count = mContext.getContentResolver().update(FavGroupNameDatabaseTable.CONTENT_URI, Value, where, null);
    }

    public void removeAll() {
        int count;
        count = mContext.getContentResolver().delete(FavGroupNameDatabaseTable.CONTENT_URI,null,null);
    }

    private List<FavGroupName> queryList() {
        Cursor cursor = mContext.getContentResolver().query(FavGroupNameDatabaseTable.CONTENT_URI, null, null, null, null);
        List<FavGroupName> favGroupNameList = new ArrayList<>();
        FavGroupName favGroupName = null;
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    favGroupName = ParseCursor(cursor);
                    favGroupNameList.add(favGroupName);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return favGroupNameList;
        }
        return null;
    }

    private FavGroupName query(int groupType) {
        FavGroupName favGroupName = null;
        String selection = FavGroupName.GROUP_TYPE + " = " + groupType;
        Cursor cursor = mContext.getContentResolver().query(FavGroupNameDatabaseTable.CONTENT_URI, null, selection, null, null);
        Log.d(TAG,"query cursor = "+cursor);
        if(cursor != null) {
            if (cursor.moveToFirst())
                favGroupName = ParseCursor(cursor);
            cursor.close();
            return favGroupName;
        }
        return null;
    }

    private FavGroupName ParseCursor(Cursor cursor){
        FavGroupName favGroupName = new FavGroupName();
        favGroupName.setGroupType(mDbUtils.GetIntFromTable(cursor,FavGroupName.GROUP_TYPE));
        favGroupName.setGroupName(mDbUtils.GetStringFromTable(cursor,FavGroupName.GROUP_NAME));
        Log.i(TAG, "mDbUtils.GetIntFromTable(cursor, _ID) = "+mDbUtils.GetIntFromTable(cursor, "_ID") +
                " group type = "+favGroupName.getGroupType());
        return favGroupName;
    }
}
