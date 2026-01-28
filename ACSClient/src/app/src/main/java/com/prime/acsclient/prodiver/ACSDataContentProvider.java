package com.prime.acsclient.prodiver;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ACSDataContentProvider extends ContentProvider {
    private ACSDatabase g_acs_db = null ;
    private static final String AUTHORITY = "com.prime.acsclient.prodiver";
    private static final String URI_PATH = "ACSData";
    public static final Uri ACS_PROVIDER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH);

    @Override
    public boolean onCreate() {
        g_acs_db = new ACSDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = g_acs_db.getReadableDatabase();
        return db.query(ACSDatabase.PROVIDER_DB_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = g_acs_db.getWritableDatabase();
        long id = db.insert(ACSDatabase.PROVIDER_DB_TABLE_NAME, null, values);
        if (id > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = g_acs_db.getWritableDatabase();
        int count = db.update(ACSDatabase.PROVIDER_DB_TABLE_NAME, values, selection, selectionArgs);
        if ( count > 0 )
            getContext().getContentResolver().notifyChange(uri, null);
        return count ;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = g_acs_db.getWritableDatabase();
        int count = db.delete(ACSDatabase.PROVIDER_DB_TABLE_NAME, selection, selectionArgs);
        if ( count > 0 )
            getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
