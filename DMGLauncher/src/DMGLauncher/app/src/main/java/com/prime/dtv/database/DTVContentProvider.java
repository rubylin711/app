package com.prime.dtv.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
//import com.prime.dtv.Database.DBChannel;
//import com.prime.dtv.Database.DBOpenHelper;

public class DTVContentProvider extends ContentProvider {
    private static final String TAG = "DTVContentProvider";
    private DBOpenHelper dbOpenHelper;
    private static final int ITEM_CHANNEL = 1;
    private static final int ITEM_MAX = 2
;
    private static final int ITEM_ID_CHANNEL = 11;
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(DBChannel.AUTHORITY, DBChannel.TABLE_NAME, DBChannel.ITEM);

        uriMatcher.addURI(DBChannel.AUTHORITY, DBChannel.TABLE_NAME+ "/#", DBChannel.ITEM_ID);
    }
    @Override
    public boolean onCreate() {
        dbOpenHelper = new DBOpenHelper(this.getContext());

        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch(uriMatcher.match(uri)){
            case ITEM_CHANNEL:
                return DBChannel.CONTENT_TYPE;
            case ITEM_ID_CHANNEL:
                return DBChannel.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }
    }

    private String GetTableName(Uri uri){
        switch(uriMatcher.match(uri)){
            case ITEM_CHANNEL:
                return DBChannel.TABLE_NAME;
            case ITEM_ID_CHANNEL:
                return DBChannel.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }
    }

    private String GetTableID(Uri uri){
        switch(uriMatcher.match(uri)){
            case ITEM_CHANNEL:
                return DBChannel.CHANNEL_ID;
            case ITEM_ID_CHANNEL:
                return DBChannel.CHANNEL_ID;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }
    }
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String table_name = GetTableName(uri);
        String table_id = GetTableID(uri);
        Cursor cursor;
        switch (uriMatcher.match(uri))
        {
            case ITEM_CHANNEL:
                cursor = db.query(table_name, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ITEM_ID_CHANNEL:
                String id = uri.getPathSegments().get(1);
                cursor =db.query(table_name,
                        projection,
                        table_id + "=" + id + (!TextUtils.isEmpty(selection) ? "AND(" + selection + ")" : ""),
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Failed to query to uri:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        long rowId;
        String table_name = GetTableName(uri);
        String table_id = GetTableID(uri);
        int uri_id = uriMatcher.match(uri);
        if (uri_id >= ITEM_CHANNEL && uri_id < ITEM_MAX)
        {
            rowId = db.insert(table_name, table_id, contentValues);
            if (rowId > 0)
            {
                    Uri noteUri = ContentUris.withAppendedId(uri, rowId);
                getContext().getContentResolver().notifyChange(noteUri, null);
                return noteUri;
            }
            throw new SQLException("Failed to insert row to uri:" + uri);
        }
        else{
            throw new IllegalArgumentException("UnKnown URI" + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String table_name = GetTableName(uri);
        String table_id = GetTableID(uri);
        int count;
        switch (uriMatcher.match(uri))
        {
            case ITEM_CHANNEL:
                count = db.delete(table_name, selection, selectionArgs);
                break;
            case ITEM_ID_CHANNEL:
                String id = uri.getPathSegments().get(1);
                count =db.delete(table_name,
                        table_id + "=" + id + (!TextUtils.isEmpty(selection) ? "AND(" + selection + ")" : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Fail to delete to uri :" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String table_name = GetTableName(uri);
        String table_id = GetTableID(uri);
        int count;
        switch (uriMatcher.match(uri))
        {
            case ITEM_CHANNEL:
                count = db.update(table_name, values, selection, selectionArgs);
                break;
            case ITEM_ID_CHANNEL:
                String id = uri.getPathSegments().get(1);
                count =db.update(table_name,
                        values,
                        table_id + "=" + id + (!TextUtils.isEmpty(selection) ? "AND(" + selection + ")" : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Failed to update to uri :" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
