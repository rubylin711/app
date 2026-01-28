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
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.FavInfo;
import com.prime.dtv.sysdata.ProgramInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
                + FavInfo.FAV_NUM + " integer,"
                + FavInfo.CHANNEL_ID + " integer,"
                + FavInfo.FAV_MODE + " integer,"
                + "UNIQUE("+FavInfo.FAV_NUM+","+FavInfo.FAV_MODE+")"
                + ");";
        return cmd;
    }

    public static String TableDrop() {
        String cmd = "DROP TABLE IF EXISTS " + FavInfoDatabaseTable.TABLE_NAME + ";";
        return cmd;
    }

    public void save(FavInfo favInfo) {
        FavInfo DBfavInfo = query(favInfo.getFavNum());
        if(DBfavInfo == null) {
            add(favInfo);
        }
        else {
            update(favInfo);
        }
    }

    public static final DVBDatabase.SQLiteBinder<FavInfo> BINDER = (stmt, f) -> {
        int i = 1;
        stmt.bindLong(i++, f.getFavNum());
        stmt.bindLong(i++, f.getChannelId());
        stmt.bindLong(i++, f.getFavMode());
    };

    public void save(List<FavInfo> favInfoList) {
//        long start = System.currentTimeMillis();
        String SQL = "INSERT OR REPLACE INTO "+TABLE_NAME+" ("+FavInfo.FAV_NUM+", "+FavInfo.CHANNEL_ID+", "+FavInfo.FAV_MODE
                +") VALUES (?, ?, ?)";
        DVBContentProvider.getDvbDb().saveListWithUpsert(
                TABLE_NAME,
                new String[]{FavInfo.FAV_MODE,FavInfo.FAV_NUM},
                favInfoList,
                SQL,
                BINDER,
                f -> new DVBDatabase.KeyTuple(f.getFavMode(), f.getFavNum())
        );
//        long end = System.currentTimeMillis();
//        Log.d("qqqq", "saveFavInfoList done in " + (end - start) + " ms");
    }

    public List<FavInfo> load() {
        List<FavInfo> favInfoList = queryList();
        return favInfoList;
    }

    public List<FavInfo> loadByGroupType(int grouType) {
        List<FavInfo> favInfoList = queryList(grouType);
        return favInfoList;
    }

    private void add(FavInfo favInfo) {
        ContentValues Value = new ContentValues();
        Value.put(FavInfo.FAV_NUM, favInfo.getFavNum());
        Value.put(FavInfo.CHANNEL_ID, favInfo.getChannelId());
        Value.put(FavInfo.FAV_MODE, favInfo.getFavMode());
//        if(favInfo.getFavMode() == FavGroup.ALL_TV_TYPE) {
//            Log.d(TAG, "add " + favInfo.ToString());
//            ProgramInfo p = DataManager.getDataManager().getProgramInfo(favInfo.getChannelId());
//            if(p != null) {
//                Log.d(TAG, "FavGroup.ALL_TV_TYPE add " + p.getDisplayName());
//            }
//        }
        mContext.getContentResolver().insert(FavInfoDatabaseTable.CONTENT_URI, Value);
    }

    private void update(FavInfo favInfo) {
        int count;
        String where = FavInfo.FAV_NUM + " = ? AND " + FavInfo.FAV_MODE + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(favInfo.getFavNum()), String.valueOf(favInfo.getFavMode())};
        ContentValues Value = new ContentValues();
        Value.put(FavInfo.CHANNEL_ID, favInfo.getChannelId());
        Value.put(FavInfo.FAV_MODE, favInfo.getFavMode());

//        if(favInfo.getFavMode() == FavGroup.ALL_TV_TYPE) {
//            Log.d(TAG, "update " + favInfo.ToString());
//            ProgramInfo p = DataManager.getDataManager().getProgramInfo(favInfo.getChannelId());
//            if(p != null) {
//                Log.d(TAG, "FavGroup.ALL_TV_TYPE add " + p.getDisplayName());
//            }
//        }
        count = mContext.getContentResolver().update(FavInfoDatabaseTable.CONTENT_URI, Value, where, selectionArgs);
    }

    public void remove(FavInfo favInfo) {
        int count;
        String where = FavInfo.CHANNEL_ID + " = ? AND " + FavInfo.FAV_MODE + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(favInfo.getChannelId()), String.valueOf(favInfo.getFavMode())};
        if(favInfo.getFavMode() == FavGroup.ALL_TV_TYPE) {
            Log.d(TAG, "bbbb remove favInfo.getChannelId() " + favInfo.getChannelId());
        }
        count = mContext.getContentResolver().delete(FavInfoDatabaseTable.CONTENT_URI, where, selectionArgs);
    }

    public void removeAll() {
        int count;
        Log.d(TAG, "removeAll favInfo");
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

    private List<FavInfo> queryList(int groupType) {
        String selection = FavInfo.FAV_MODE + " = " + groupType;
        String sort = FavInfo.FAV_NUM + " ASC";
        Cursor cursor = mContext.getContentResolver().query(FavInfoDatabaseTable.CONTENT_URI, null, selection, null, sort);
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
        favInfo.setFavNum(mDbUtils.GetIntFromTable(TABLE_NAME,cursor,FavInfo.FAV_NUM));
        favInfo.setChannelId(mDbUtils.GetLongFromTable(TABLE_NAME,cursor,FavInfo.CHANNEL_ID));
        favInfo.setFavMode(mDbUtils.GetIntFromTable(TABLE_NAME,cursor,FavInfo.FAV_MODE));
        Log.i(TAG, "mDbUtils.GetIntFromTable(cursor, _ID) = "+mDbUtils.GetIntFromTable(TABLE_NAME, cursor, "_ID") +
                " fav num = "+favInfo.getFavNum());
        return favInfo;
    }

    public class CompositeKey {
        private final int FavNum;
        private final int FavMode;

        public CompositeKey(int field1, int field2) {
            this.FavNum = field1;
            this.FavMode = field2;
        }

        public int getFavNum() {
            return this.FavNum;
        }

        public int getFavMode() {
            return this.FavMode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CompositeKey)) return false;
            CompositeKey that = (CompositeKey) o;
            return (FavMode == that.FavMode) &&
                    (FavNum ==  that.FavNum);
        }

        @Override
        public int hashCode() {
            return Objects.hash(FavNum, FavMode);
        }
    }
}
