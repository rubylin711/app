package com.prime.launcher.teletextservice.decoder;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class TeletextContentProvider extends ContentProvider {

    private static final String TAG = "TeletextProvider";
    private static final int CODE_PAGES = 100;
    private static final int CODE_PAGES_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(DtvContract.AUTHORITY, DtvContract.Pages.TABLE_NAME, CODE_PAGES);
        sUriMatcher.addURI(DtvContract.AUTHORITY, DtvContract.Pages.TABLE_NAME + "/#", CODE_PAGES_ID);
    }

    private SQLiteDatabase mDatabase;

    @Override
    public boolean onCreate() {
        mDatabase = new TeletextDatabaseHelper(getContext()).getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_PAGES:
                return mDatabase.query(DtvContract.Pages.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            case CODE_PAGES_ID:
                selection = DtvContract.Pages._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return mDatabase.query(DtvContract.Pages.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CODE_PAGES:
                return "vnd.android.cursor.dir/vnd." + DtvContract.AUTHORITY + ".pages";
            case CODE_PAGES_ID:
                return "vnd.android.cursor.item/vnd." + DtvContract.AUTHORITY + ".pages";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (sUriMatcher.match(uri) != CODE_PAGES) {
            throw new IllegalArgumentException("Insertion not supported for " + uri);
        }
        long id = mDatabase.insertWithOnConflict(
            DtvContract.Pages.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE // 🔁 取代舊資料
        );
        if (id == -1) {
            throw new SQLException("Failed to insert row into " + uri);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
        if (sUriMatcher.match(uri) != CODE_PAGES) return super.bulkInsert(uri, valuesArray);
        int count = 0;
        mDatabase.beginTransaction();
        try {
            for (ContentValues values : valuesArray) {
                long id = mDatabase.insertWithOnConflict(
                    DtvContract.Pages.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                );
                if (id != -1) count++;
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
        return count;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        if (match == CODE_PAGES_ID) {
            selection = DtvContract.Pages._ID + "=?";
            selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
        }
        return mDatabase.delete(DtvContract.Pages.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (sUriMatcher.match(uri) != CODE_PAGES_ID) {
            throw new IllegalArgumentException("Update not supported for " + uri);
        }
        selection = DtvContract.Pages._ID + "=?";
        selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
        return mDatabase.update(DtvContract.Pages.TABLE_NAME, values, selection, selectionArgs);
    }

    private static class TeletextDatabaseHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "teletext.db";
        private static final int DB_VERSION = 3;

        public TeletextDatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + DtvContract.Pages.TABLE_NAME + " ("
                    + DtvContract.Pages._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + DtvContract.Pages.COLUMN_PAGE_SUBPAGE + " TEXT UNIQUE,"  // 加上 UNIQUE
                    + DtvContract.Pages.COLUMN_PAGE + " TEXT,"
                    + DtvContract.Pages.COLUMN_DATA + " TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DtvContract.Pages.TABLE_NAME);
            onCreate(db);
        }
    }
} 