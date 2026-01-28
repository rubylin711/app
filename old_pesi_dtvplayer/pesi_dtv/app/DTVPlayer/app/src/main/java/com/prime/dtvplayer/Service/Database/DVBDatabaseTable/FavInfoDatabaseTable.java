package com.prime.dtvplayer.Service.Database.DVBDatabaseTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.prime.dtvplayer.Database.DBUtils;
import com.prime.dtvplayer.Service.Database.DVBContentProvider;
import com.prime.dtvplayer.Sysdata.FavGroupName;
import com.prime.dtvplayer.Sysdata.FavInfo;

import java.util.ArrayList;
import java.util.List;

public class FavInfoDatabaseTable {
    private static final String TAG = "FavInfoDatabaseTable";
    public static final String TABLE_NAME = "FavInfo";

    public static final Uri CONTENT_URI = Uri.parse("content://" + DVBContentProvider.AUTHORITY + "/" + TABLE_NAME);

    //Type
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.favinfo";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.favinfo";

    private Context mContext;
    DBUtils mDbUtils = new DBUtils();

    public FavInfoDatabaseTable(Context context) {
        mContext = context;
    }


    public static String TableCreate() {
        String cmd = "create table "
                + FavInfoDatabaseTable.TABLE_NAME + " ("
                + "_ID" + " integer primary key autoincrement,"
                + FavInfo.FAV_NUM + ","
                + FavInfo.CHANNEL_ID + ","
                + FavInfo.FAV_MODE
                + ");";
        return cmd;
    }

    public static String TableDrop() {
        String cmd = "DROP TABLE IF EXISTS " + FavInfoDatabaseTable.TABLE_NAME + ";";
        return cmd;
    }

//    public void save(FavInfo favInfo) {
//        FavInfo DBfavInfo = query(favInfo.getFavNum());
//        if(DBfavInfo == null) {
//            add(favInfo);
//        }
//        else {
//            update(favInfo);
//        }
//    }

    public void save(List<FavInfo> favInfoList) {
        removeAll();
        for (int i = 0; i < favInfoList.size(); i++) {
            add(favInfoList.get(i));
        }
    }

    public List<FavInfo> load() {
        List<FavInfo> favInfoList = queryList();
        return favInfoList;
    }

    private void add(FavInfo favInfo) {
        ContentValues Value = new ContentValues();
        Value.put(FavInfo.FAV_NUM, favInfo.getFavNum());
        Value.put(FavInfo.CHANNEL_ID, favInfo.getChannelId());
        Value.put(FavInfo.FAV_MODE, favInfo.getFavMode());

        Log.d(TAG, "add "+ favInfo.ToString());
        mContext.getContentResolver().insert(FavInfoDatabaseTable.CONTENT_URI, Value);
    }

    private void update(FavInfo favInfo) {
        int count;
        String where = FavInfo.FAV_NUM + " = " + favInfo.getFavNum();
        ContentValues Value = new ContentValues();
        Value.put(FavInfo.CHANNEL_ID, favInfo.getChannelId());
        Value.put(FavInfo.FAV_MODE, favInfo.getFavMode());

        Log.d(TAG, "update " + favInfo.ToString());
        count = mContext.getContentResolver().update(FavInfoDatabaseTable.CONTENT_URI, Value, where, null);
    }

    public void removeAll() {
        int count;
        count = mContext.getContentResolver().delete(FavInfoDatabaseTable.CONTENT_URI,null,null);
    }

    private List<FavInfo> queryList() {
        Cursor cursor = mContext.getContentResolver().query(FavInfoDatabaseTable.CONTENT_URI, null, null, null, null);
        List<FavInfo> favInfoList = new ArrayList<>();
        FavInfo favInfo = null;
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    favInfo = ParseCursor(cursor);
                    favInfoList.add(favInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return favInfoList;
        }
        return null;
    }

    private FavInfo query(int favNum) {
        FavInfo favInfo = null;
        String selection = FavInfo.FAV_NUM + " = " + favNum;
        Cursor cursor = mContext.getContentResolver().query(FavInfoDatabaseTable.CONTENT_URI, null, selection, null, null);
        Log.d(TAG,"query cursor = "+cursor);
        if(cursor != null) {
            if (cursor.moveToFirst())
                favInfo = ParseCursor(cursor);
            cursor.close();
            return favInfo;
        }
        return null;
    }

    private FavInfo ParseCursor(Cursor cursor){
        FavInfo favInfo = new FavInfo();
        favInfo.setFavNum(mDbUtils.GetIntFromTable(cursor,FavInfo.FAV_NUM));
        favInfo.setChannelId(mDbUtils.GetLongFromTable(cursor,FavInfo.CHANNEL_ID));
        favInfo.setFavMode(mDbUtils.GetIntFromTable(cursor,FavInfo.FAV_MODE));
        Log.i(TAG, "mDbUtils.GetIntFromTable(cursor, _ID) = "+mDbUtils.GetIntFromTable(cursor, "_ID") +
                " fav num = "+favInfo.getFavNum());
        return favInfo;
    }
}
