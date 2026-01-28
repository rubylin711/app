package com.prime.dtvplayer.Service.Database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.prime.dtvplayer.Service.Database.DVBDatabaseTable.FavGroupNameDatabaseTable;
import com.prime.dtvplayer.Service.Database.DVBDatabaseTable.FavInfoDatabaseTable;
import com.prime.dtvplayer.Service.Database.DVBDatabaseTable.GposDatabaseTable;
import com.prime.dtvplayer.Service.Database.DVBDatabaseTable.ProgramDatabaseTable;
import com.prime.dtvplayer.Service.Database.DVBDatabaseTable.SatInfoDatabaseTable;
import com.prime.dtvplayer.Service.Database.DVBDatabaseTable.TpInfoDatabaseTable;
import com.prime.dtvplayer.Service.Database.NetStreamDatabaseTable.NetProgramDatabaseTable;

public class DVBContentProvider extends ContentProvider {
    private static final String TAG = "DVBContentProvider";
    public static final String AUTHORITY = DVBDatabase.AUTHORITY + ".DVBContentProvider";
    public static final int ITEM_GPOS = 1;
    public static final int ITEM_PROGRAM_INFO = 2;
    public static final int ITEM_FAV_GROUP_NAME = 3;
    public static final int ITEM_FAV_INFO = 4;
    public static final int ITEM_ANT_INFO = 5;
    public static final int ITEM_SAT_INFO = 6;
    public static final int ITEM_TP_INFO = 7;
    public static final int ITEM_NET_PROGRAM_INFO = 8;

    private static final UriMatcher uriMatcher;
    private DVBDatabase mDvbDb;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, GposDatabaseTable.TABLE_NAME, ITEM_GPOS);
        uriMatcher.addURI(AUTHORITY, ProgramDatabaseTable.TABLE_NAME, ITEM_PROGRAM_INFO);
        uriMatcher.addURI(AUTHORITY, FavGroupNameDatabaseTable.TABLE_NAME, ITEM_FAV_GROUP_NAME);
        uriMatcher.addURI(AUTHORITY, FavInfoDatabaseTable.TABLE_NAME, ITEM_FAV_INFO);
        uriMatcher.addURI(AUTHORITY, SatInfoDatabaseTable.TABLE_NAME, ITEM_SAT_INFO);
        uriMatcher.addURI(AUTHORITY, TpInfoDatabaseTable.TABLE_NAME, ITEM_TP_INFO);
        uriMatcher.addURI(AUTHORITY, NetProgramDatabaseTable.TABLE_NAME, ITEM_NET_PROGRAM_INFO);
    }

    private String GetTableName(Uri uri){
        switch(uriMatcher.match(uri)){
            case ITEM_GPOS:
                return GposDatabaseTable.TABLE_NAME;
            case ITEM_PROGRAM_INFO:
                return ProgramDatabaseTable.TABLE_NAME;
            case ITEM_FAV_GROUP_NAME:
                return FavGroupNameDatabaseTable.TABLE_NAME;
            case ITEM_FAV_INFO:
                return FavInfoDatabaseTable.TABLE_NAME;
            case ITEM_SAT_INFO:
                return SatInfoDatabaseTable.TABLE_NAME;
            case ITEM_TP_INFO:
                return TpInfoDatabaseTable.TABLE_NAME;
            case ITEM_NET_PROGRAM_INFO:
                return NetProgramDatabaseTable.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mDvbDb = new DVBDatabase(this.getContext(),DVBDatabase.DATABASE_VERSION);
        Log.d(TAG,"exce mDvbDb = "+mDvbDb);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDvbDb.mDbOpenHelper.getWritableDatabase();
        Log.d(TAG,"query db = "+db);
        String table_name = GetTableName(uri);
//        String table_id = GetTableID(uri);
        Cursor cursor;
        switch (uriMatcher.match(uri))
        {
            case ITEM_GPOS:
            case ITEM_PROGRAM_INFO:
            case ITEM_FAV_GROUP_NAME:
            case ITEM_FAV_INFO:
            case ITEM_SAT_INFO:
            case ITEM_TP_INFO:
            case ITEM_NET_PROGRAM_INFO:
                cursor = db.query(table_name, projection, selection, selectionArgs, null, null, sortOrder);
                break;
//            case ITEM_ID_CHANNEL:
//                String id = uri.getPathSegments().get(1);
//                cursor =db.query(table_name,
//                        projection,
//                        table_id + "=" + id + (!TextUtils.isEmpty(selection) ? "AND(" + selection + ")" : ""),
//                        selectionArgs,
//                        null,
//                        null,
//                        sortOrder);
//                break;
            default:
                throw new IllegalArgumentException("Failed to query to uri:" + uri);
        }
//        getContext().getContentResolver().notifyChange(uri, null);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch(uriMatcher.match(uri)){
            case ITEM_GPOS:
                return GposDatabaseTable.CONTENT_TYPE;
            case ITEM_PROGRAM_INFO:
                return ProgramDatabaseTable.CONTENT_TYPE;
            case ITEM_FAV_GROUP_NAME:
                return FavGroupNameDatabaseTable.CONTENT_TYPE;
            case ITEM_FAV_INFO:
                return FavInfoDatabaseTable.CONTENT_TYPE;
            case ITEM_SAT_INFO:
                return SatInfoDatabaseTable.CONTENT_TYPE;
            case ITEM_TP_INFO:
                return TpInfoDatabaseTable.CONTENT_TYPE;
            case ITEM_NET_PROGRAM_INFO:
                return NetProgramDatabaseTable.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mDvbDb.mDbOpenHelper.getWritableDatabase();
        long rowId;
        String table_name = GetTableName(uri);
//        String table_id = GetTableID(uri);
        int uri_id = uriMatcher.match(uri);
        switch(uri_id){
            case ITEM_GPOS:
            case ITEM_PROGRAM_INFO:
            case ITEM_FAV_GROUP_NAME:
            case ITEM_FAV_INFO:
            case ITEM_SAT_INFO:
            case ITEM_TP_INFO:
            case ITEM_NET_PROGRAM_INFO:
                try {
                    rowId = db.insert(table_name, null, values);
                    if (rowId > 0)
                    {
                        Uri noteUri = ContentUris.withAppendedId(uri, rowId);
                        getContext().getContentResolver().notifyChange(noteUri, null);
                        return noteUri;
                    }
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
//            case ITEM_ID_CHANNEL:
//                return DtvChannelTable.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Failed to insert row to uri:" + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDvbDb.mDbOpenHelper.getWritableDatabase();
        String table_name = GetTableName(uri);
//        String table_id = GetTableID(uri);
        int count = 0;
        switch(uriMatcher.match(uri)){
            case ITEM_GPOS:
            case ITEM_PROGRAM_INFO:
            case ITEM_FAV_GROUP_NAME:
            case ITEM_FAV_INFO:
            case ITEM_SAT_INFO:
            case ITEM_TP_INFO:
            case ITEM_NET_PROGRAM_INFO:
                try {
                    count = db.delete(table_name, selection, selectionArgs);
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
//            case ITEM_ID_CHANNEL:
//                String id = uri.getPathSegments().get(1);
//                count =db.update(table_name,
//                        values,
//                        table_id + "=" + id + (!TextUtils.isEmpty(selection) ? "AND(" + selection + ")" : ""),
//                        selectionArgs);
//                break;
            default:
                throw new IllegalArgumentException("Failed to update to uri :" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDvbDb.mDbOpenHelper.getWritableDatabase();
        String table_name = GetTableName(uri);
//        String table_id = GetTableID(uri);
        int count;
        switch (uriMatcher.match(uri))
        {
            case ITEM_GPOS:
            case ITEM_PROGRAM_INFO:
            case ITEM_FAV_GROUP_NAME:
            case ITEM_FAV_INFO:
            case ITEM_SAT_INFO:
            case ITEM_TP_INFO:
            case ITEM_NET_PROGRAM_INFO:
                count = db.update(table_name, values, selection, selectionArgs);
                break;
//            case ITEM_ID_CHANNEL:
//                String id = uri.getPathSegments().get(1);
//                count =db.update(table_name,
//                        values,
//                        table_id + "=" + id + (!TextUtils.isEmpty(selection) ? "AND(" + selection + ")" : ""),
//                        selectionArgs);
//                break;
            default:
                throw new IllegalArgumentException("Failed to update to uri :" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
