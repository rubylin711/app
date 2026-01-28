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
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class BookInfoDatabaseTable {
    private static final String TAG = "BookInfoDatabaseTable";
    public static final String TABLE_NAME = "BookInfo";

    public static final Uri CONTENT_URI = Uri.parse("content://" + DVBContentProvider.AUTHORITY + "/" + TABLE_NAME);

    //Type
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.bookinfo";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.bookinfo";

    private Context mContext;
    DBUtils mDbUtils = new DBUtils();

    public BookInfoDatabaseTable(Context context) {
        mContext = context;
    }


    public static String TableCreate() {
        String cmd = "create table "
                + BookInfoDatabaseTable.TABLE_NAME + " ("
                + "_ID" + " integer primary key autoincrement,"
                + BookInfo.BOOK_ID + " integer UNIQUE,"
                + BookInfo.CHANNEL_ID + ","
                + BookInfo.CHANNEL_NUM + ","
                + BookInfo.GROUP_TYPE + ","
                + BookInfo.EVENT_NAME + ","
                + BookInfo.BOOK_TYPE + ","
                + BookInfo.BOOK_CYCLE + ","
                + BookInfo.YEAR + ","
                + BookInfo.MONTH + ","
                + BookInfo.DATE + ","
                + BookInfo.WEEK + ","
                + BookInfo.START_TIME + ","
                + BookInfo.START_TIME_MS + ","
                + BookInfo.DURATION + ","
                + BookInfo.DURATION_MS + ","
                + BookInfo.ENABLE + ","
                + BookInfo.IS_SERIES + ","
                + BookInfo.EPISODE + ","
                + BookInfo.SERIES_REC_KEY + ","
                + BookInfo.EPG_EVENT_ID + ","
                + BookInfo.ALLOW_AUTO_SELECT + ","
                + BookInfo.IS_4K
                + ");";
        return cmd;
    }

    public static String TableDrop() {
        String cmd = "DROP TABLE IF EXISTS " + BookInfoDatabaseTable.TABLE_NAME + ";";
        return cmd;
    }

    public void save(BookInfo bookInfo) {
        BookInfo dbBookInfo = query(bookInfo.getBookId());
        if(dbBookInfo == null) {
            add(bookInfo);
        }
        else {
            update(bookInfo);
        }
    }

    public static final DVBDatabase.SQLiteBinder<BookInfo> BINDER = (stmt, b) -> {
        int i = 1;
        stmt.bindLong(i++, b.getBookId());
        stmt.bindLong(i++, b.getChannelId());
        stmt.bindString(i++, b.getChannelNum() != null ? b.getChannelNum() : "");
        stmt.bindLong(i++, b.getGroupType());
        stmt.bindString(i++, b.getEventName() != null ? b.getEventName() : "");
        stmt.bindLong(i++, b.getBookType());
        stmt.bindLong(i++, b.getBookCycle());
        stmt.bindLong(i++, b.getYear());
        stmt.bindLong(i++, b.getMonth());
        stmt.bindLong(i++, b.getDate());
        stmt.bindLong(i++, b.getWeek());
        stmt.bindLong(i++, b.getStartTime());
        stmt.bindLong(i++, b.getStartTimeMs());
        stmt.bindLong(i++, b.getDuration());
        stmt.bindLong(i++, b.getDurationMs());
        stmt.bindLong(i++, b.getEnable());
        stmt.bindLong(i++, b.isSeries() ? 1 : 0);
        stmt.bindLong(i++, b.getEpisode());
        if (b.getSeriesRecKey() != null)
            stmt.bindBlob(i++, b.getSeriesRecKey());
        else
            stmt.bindNull(i++);
        stmt.bindLong(i++, b.getEpgEventId());
        stmt.bindLong(i++, b.isAutoSelect() ? 1 : 0);
        stmt.bindLong(i++, b.is4K() ? 1 : 0);
    };

    public void save(List<BookInfo> bookInfoList) {

        if(DVBContentProvider.getDvbDb().isColumnExists(TABLE_NAME, BookInfo.CHANNEL_NUM) == false){
            DVBContentProvider.getDvbDb().insert_new_colume(TABLE_NAME, BookInfo.CHANNEL_NUM, "TEXT");
        }
        if(DVBContentProvider.getDvbDb().isColumnExists(TABLE_NAME, BookInfo.START_TIME_MS) == false){
            DVBContentProvider.getDvbDb().insert_new_colume(TABLE_NAME, BookInfo.START_TIME_MS, "INTEGER");
        }
        if(DVBContentProvider.getDvbDb().isColumnExists(TABLE_NAME, BookInfo.DURATION_MS) == false){
            DVBContentProvider.getDvbDb().insert_new_colume(TABLE_NAME, BookInfo.DURATION_MS, "INTEGER");
        }
        if(DVBContentProvider.getDvbDb().isColumnExists(TABLE_NAME, BookInfo.ALLOW_AUTO_SELECT) == false){
            DVBContentProvider.getDvbDb().insert_new_colume(TABLE_NAME, BookInfo.ALLOW_AUTO_SELECT, "INTEGER");
        }
        if(DVBContentProvider.getDvbDb().isColumnExists(TABLE_NAME, BookInfo.IS_4K) == false){
            DVBContentProvider.getDvbDb().insert_new_colume(TABLE_NAME, BookInfo.IS_4K, "INTEGER");
        }

        String SQL = "INSERT OR REPLACE INTO "+TABLE_NAME+" (" +
                BookInfo.BOOK_ID + ", " + BookInfo.CHANNEL_ID + ", " + BookInfo.CHANNEL_NUM + ", " + BookInfo.GROUP_TYPE + ", " + BookInfo.EVENT_NAME + ", " +
                BookInfo.BOOK_TYPE + ", " + BookInfo.BOOK_CYCLE + ", " + BookInfo.YEAR + ", " + BookInfo.MONTH + ", " +
                BookInfo.DATE + ", " + BookInfo.WEEK + ", " + BookInfo.START_TIME + ", " + BookInfo.START_TIME_MS + ", " + BookInfo.DURATION + ", " + BookInfo.DURATION_MS + ", " +
                BookInfo.ENABLE + ", " + BookInfo.IS_SERIES + ", " + BookInfo.EPISODE + ", " +
                BookInfo.SERIES_REC_KEY + ", " + BookInfo.EPG_EVENT_ID + ", " + BookInfo.ALLOW_AUTO_SELECT + ", " + BookInfo.IS_4K +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        DVBContentProvider.getDvbDb().saveListWithUpsert(
                TABLE_NAME,
                new String[]{BookInfo.BOOK_ID},
                bookInfoList,
                SQL,
                BINDER,
                b -> new DVBDatabase.KeyTuple(b.getBookId())
        );
    }


    public List<BookInfo> load() {
        List<BookInfo> bookInfoList = queryList();
        return bookInfoList;
    }

    private ContentValues getBookInfoContentValues(BookInfo bookInfo) {
        ContentValues Value = new ContentValues();
        Value.put(BookInfo.BOOK_ID, bookInfo.getBookId());
        Value.put(BookInfo.CHANNEL_ID, bookInfo.getChannelId());
        Value.put(BookInfo.CHANNEL_NUM, bookInfo.getChannelNum());
        Value.put(BookInfo.GROUP_TYPE, bookInfo.getGroupType());
        Value.put(BookInfo.EVENT_NAME, bookInfo.getEventName());
        Value.put(BookInfo.BOOK_TYPE, bookInfo.getBookType());
        Value.put(BookInfo.BOOK_CYCLE, bookInfo.getBookCycle());
        Value.put(BookInfo.YEAR, bookInfo.getYear());
        Value.put(BookInfo.MONTH, bookInfo.getMonth());
        Value.put(BookInfo.DATE, bookInfo.getDate());
        Value.put(BookInfo.WEEK, bookInfo.getWeek());
        Value.put(BookInfo.START_TIME, bookInfo.getStartTime());
        Value.put(BookInfo.START_TIME_MS, bookInfo.getStartTimeMs());
        Value.put(BookInfo.DURATION, bookInfo.getDuration());
        Value.put(BookInfo.DURATION_MS, bookInfo.getDurationMs());
        Value.put(BookInfo.ENABLE, bookInfo.getEnable());
        Value.put(BookInfo.IS_SERIES, bookInfo.isSeries() ? 1 : 0);
        Value.put(BookInfo.EPISODE, bookInfo.getEpisode());
        Value.put(BookInfo.SERIES_REC_KEY, bookInfo.getSeriesRecKey());
        Value.put(BookInfo.EPG_EVENT_ID, bookInfo.getEpgEventId());
        Value.put(BookInfo.ALLOW_AUTO_SELECT, bookInfo.isAutoSelect() ? 1 : 0);
        Value.put(BookInfo.IS_4K, bookInfo.is4K() ? 1 : 0);

        //Log.d(TAG, "getBookInfoContentValues "+ bookInfo.ToString());
        return Value;
    }

    private void add(BookInfo bookInfo) {
        ContentValues Value = getBookInfoContentValues(bookInfo);

        Log.d(TAG, "add "+ bookInfo.ToString());
        mContext.getContentResolver().insert(BookInfoDatabaseTable.CONTENT_URI, Value);
    }

    private void update(BookInfo bookInfo) {
        int count;
        String where = BookInfo.BOOK_ID + " = " + bookInfo.getBookId();
        ContentValues Value = new ContentValues();
//        Value.put(BookInfo.BOOK_ID, bookInfo.getBookId());
        Value.put(BookInfo.CHANNEL_ID, bookInfo.getChannelId());
        Value.put(BookInfo.CHANNEL_NUM, bookInfo.getChannelNum());
        Value.put(BookInfo.GROUP_TYPE, bookInfo.getGroupType());
        Value.put(BookInfo.EVENT_NAME, bookInfo.getEventName());
        Value.put(BookInfo.BOOK_TYPE, bookInfo.getBookType());
        Value.put(BookInfo.BOOK_CYCLE, bookInfo.getBookCycle());
        Value.put(BookInfo.YEAR, bookInfo.getYear());
        Value.put(BookInfo.MONTH, bookInfo.getMonth());
        Value.put(BookInfo.DATE, bookInfo.getDate());
        Value.put(BookInfo.WEEK, bookInfo.getWeek());
        Value.put(BookInfo.START_TIME, bookInfo.getStartTime());
        Value.put(BookInfo.START_TIME_MS, bookInfo.getStartTimeMs());
        Value.put(BookInfo.DURATION, bookInfo.getDuration());
        Value.put(BookInfo.DURATION_MS, bookInfo.getDurationMs());
        Value.put(BookInfo.ENABLE, bookInfo.getEnable());
        Value.put(BookInfo.IS_SERIES, bookInfo.isSeries() ? 1 : 0);
        Value.put(BookInfo.EPISODE, bookInfo.getEpisode());
        Value.put(BookInfo.SERIES_REC_KEY, bookInfo.getSeriesRecKey());
        Value.put(BookInfo.EPG_EVENT_ID, bookInfo.getEpgEventId());
        Value.put(BookInfo.ALLOW_AUTO_SELECT, bookInfo.isAutoSelect() ? 1 : 0);
        Value.put(BookInfo.IS_4K, bookInfo.is4K() ? 1 : 0);

        Log.d(TAG, "update " + bookInfo.ToString());
        count = mContext.getContentResolver().update(BookInfoDatabaseTable.CONTENT_URI, Value, where, null);
    }

    public void removeAll() {
        int count;
        count = mContext.getContentResolver().delete(BookInfoDatabaseTable.CONTENT_URI,null,null);
    }

    public void remove(BookInfo bookInfo) {
        int count;
        String where = BookInfo.BOOK_ID + " = " + bookInfo.getBookId();
        Log.d(TAG, "remove BookInfo.getBookId() "+ bookInfo.getBookId());
        count = mContext.getContentResolver().delete(BookInfoDatabaseTable.CONTENT_URI, where, null);
    }

    private List<BookInfo> queryList() {
        Cursor cursor = mContext.getContentResolver().query(BookInfoDatabaseTable.CONTENT_URI, null, null, null, null);
        List<BookInfo> bookInfoList = new ArrayList<>();
        BookInfo bookInfo = null;
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    bookInfo = ParseCursor(cursor);
                    bookInfoList.add(bookInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        // return empty list if no bookInfo
        return bookInfoList;
    }

    private BookInfo query(long bookId) {
        BookInfo bookInfo = null;
        String selection = BookInfo.BOOK_ID + " = " + bookId;
        Cursor cursor = mContext.getContentResolver().query(BookInfoDatabaseTable.CONTENT_URI, null, selection, null, null);
        Log.d(TAG,"query cursor = "+cursor);
        if(cursor != null) {
            if (cursor.moveToFirst())
                bookInfo = ParseCursor(cursor);
            cursor.close();
            return bookInfo;
        }
        return null;
    }

    private BookInfo ParseCursor(Cursor cursor){
        BookInfo bookInfo = new BookInfo();
        bookInfo.setBookId(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.BOOK_ID));
        bookInfo.setChannelId(mDbUtils.GetLongFromTable(TABLE_NAME,cursor, BookInfo.CHANNEL_ID));
        bookInfo.setChannelNum(mDbUtils.GetStringFromTable(TABLE_NAME, cursor, BookInfo.CHANNEL_NUM));
        bookInfo.setGroupType(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.GROUP_TYPE));
        bookInfo.setEventName(mDbUtils.GetStringFromTable(TABLE_NAME,cursor, BookInfo.EVENT_NAME));
        bookInfo.setBookType(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.BOOK_TYPE));
        bookInfo.setBookCycle(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.BOOK_CYCLE));
        bookInfo.setYear(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.YEAR));
        bookInfo.setMonth(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.MONTH));
        bookInfo.setDate(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.DATE));
        bookInfo.setWeek(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.WEEK));
        bookInfo.setStartTime(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.START_TIME));
        bookInfo.setStartTimeMs(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, BookInfo.START_TIME_MS));
        bookInfo.setDuration(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.DURATION));
        bookInfo.setDurationMs(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.DURATION_MS));
        bookInfo.setEnable(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.ENABLE));
        bookInfo.setSeries(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.IS_SERIES) != 0);
        bookInfo.setEpisode(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.EPISODE));
        bookInfo.setSeriesRecKey(mDbUtils.GetByteArrayFromTable(TABLE_NAME,cursor, BookInfo.SERIES_REC_KEY));
        bookInfo.setEpgEventId(mDbUtils.GetIntFromTable(TABLE_NAME,cursor, BookInfo.EPG_EVENT_ID));
        bookInfo.setAutoSelect(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, BookInfo.ALLOW_AUTO_SELECT) != 0);
        bookInfo.set4K(mDbUtils.GetIntFromTable(TABLE_NAME, cursor, BookInfo.IS_4K) != 0);

        Log.i(TAG, "mDbUtils.GetIntFromTable(cursor, _ID) = "+mDbUtils.GetIntFromTable(cursor, "_ID") +
                " book id = "+bookInfo.getBookId());
        return bookInfo;
    }
}
