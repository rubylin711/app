package com.prime.dtv.service.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.prime.dtv.service.database.dvbdatabasetable.PvrInfoDatabaseTable;

import java.io.File;

public class PvrContentProvider extends ContentProvider {
    private static final String TAG = "PvrContentProvider";

    public static final String AUTHORITY = PvrDatabase.AUTHORITY + ".PvrContentProvider";

    public static final String CONTENT_STRING = "content://" + AUTHORITY + "/" + PvrDatabase.TABLE_NAME;

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PvrDatabase.TABLE_NAME);
    public static final Uri PLAYBACK_URI = Uri.parse("content://" + AUTHORITY + "/" + PvrDatabase.TABLE_NAME_PLAYBACK);
    private static final int MASTER = 1;
    private static final int SERIES = 2;
    private static final int SERIES_TABLE_DROP = 3;
    private static final int MASTER_TABLE_DROP = 4;
    private static final int PLAYBACK = 5;
    private static final int PLAYBACK_TABLE_DROP = 6;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, PvrDatabase.TABLE_NAME, MASTER);
        uriMatcher.addURI(AUTHORITY, PvrDatabase.TABLE_NAME + "/#", SERIES);
        uriMatcher.addURI(AUTHORITY, PvrDatabase.DROP_SERIES_TABLE + "/#", SERIES_TABLE_DROP);
        uriMatcher.addURI(AUTHORITY, PvrDatabase.DROP_MASTER_TABLE, MASTER_TABLE_DROP);
        uriMatcher.addURI(AUTHORITY, PvrDatabase.TABLE_NAME_PLAYBACK, PLAYBACK);
        uriMatcher.addURI(AUTHORITY, PvrDatabase.DROP_PLAYBACK_TABLE, PLAYBACK_TABLE_DROP);
    }

    public static String usbPath;
    private static SQLiteDatabase database;
    //private static PvrContentProvider instance;
    private static boolean isInitialized = false;
    private static String usbDatabasePath;


    //public static void initialize(Context context, String usbPath) {
    //    if (!isInitialized) {
    //        usbDatabasePath = Uri.parse(usbPath).getPath();
    //        instance = new PvrContentProvider();
    //        instance.initDatabase(context);
    //        isInitialized = true;
    //        Log.d(TAG, "PvrContentProvider initialized with USB path: " + usbDatabasePath);
    //    }
    //}
    public static void setUsbPathAndInit(Context context, String usbPath) {
        if (!isInitialized) {
            usbDatabasePath = Uri.parse(usbPath).getPath();
            initDatabase(context);
            isInitialized = true;
            Log.d(TAG, "PvrContentProvider initialized with USB path: " + usbDatabasePath);
        }
    }

    public static void closeDatabase() {
        isInitialized = false;
        if(database != null) {
            database.close();
            database = null;
        }
        Log.d(TAG, "PvrContentProvider shut down");
    }

    private static void initDatabase(Context context) {
        database = new PvrDatabase(context, usbDatabasePath).getWritableDatabase();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    private String getTableName(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MASTER:
                return PvrDatabase.TABLE_NAME;
            case SERIES:
                return PvrDatabase.TABLE_NAME + "_" + uri.getLastPathSegment();
            case PLAYBACK:
                return PvrDatabase.TABLE_NAME_PLAYBACK;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case MASTER:
                cursor = database.query(PvrDatabase.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SERIES:
                String lastPathSegment = uri.getLastPathSegment();
                int seriesId = Integer.parseInt(lastPathSegment);
                cursor = database.query(PvrDatabase.TABLE_NAME + "_" + seriesId, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PLAYBACK:
                cursor = database.query(PvrDatabase.TABLE_NAME_PLAYBACK, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        long rowId;
        Uri returnUri;
        switch (uriMatcher.match(uri)) {
            case MASTER:
                if (!PvrInfoDatabaseTable.isMasterTableExists(database)) {
                    database.execSQL(PvrInfoDatabaseTable.TableCreateMaster());
                    Log.d(TAG, "Master table created.");
                }
                rowId = database.insert(PvrDatabase.TABLE_NAME, null, values);
                returnUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
                break;
            case SERIES:
                String lastPathSegment = uri.getLastPathSegment();
                int seriesId = Integer.parseInt(lastPathSegment);
                if (!PvrInfoDatabaseTable.isSeriesTableExists(database, seriesId)) {
                    database.execSQL(PvrInfoDatabaseTable.TableCreateSeries(seriesId));
                    Log.d(TAG, "Series table " + seriesId + " created.");
                }
                rowId = database.insert(PvrDatabase.TABLE_NAME + "_" + seriesId, null, values);
                returnUri = Uri.parse(CONTENT_STRING + "_" + seriesId + "/" + rowId);
                break;
            case PLAYBACK:
                if (!PvrInfoDatabaseTable.isPlaybackTableExists(database)) {
                    database.execSQL(PvrInfoDatabaseTable.TableCreatePlayback());
                    Log.d(TAG, "Playback table created.");
                }
                rowId = database.insert(PvrDatabase.TABLE_NAME_PLAYBACK, null, values);
                returnUri = ContentUris.withAppendedId(PLAYBACK_URI, rowId);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int rowsUpdated;
        switch (uriMatcher.match(uri)) {
            case MASTER:
                rowsUpdated = database.update(PvrDatabase.TABLE_NAME, values, selection, selectionArgs);
                break;
            case SERIES:
                String seriesId = uri.getLastPathSegment();
                rowsUpdated = database.update(PvrDatabase.TABLE_NAME + "_" + seriesId, values, selection, selectionArgs);
                break;
            case PLAYBACK:
                rowsUpdated = database.update(PvrDatabase.TABLE_NAME_PLAYBACK, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int rowsDeleted = 0;
        switch (uriMatcher.match(uri)) {
            case MASTER_TABLE_DROP:
                if (PvrInfoDatabaseTable.isMasterTableExists(database)) {
                    database.execSQL(PvrInfoDatabaseTable.TableDropMaster());
                    rowsDeleted = 1;
                }
                break;
            case SERIES_TABLE_DROP:
                String lastPathSegment = uri.getLastPathSegment();
                if (lastPathSegment == null || !lastPathSegment.matches("\\d+")) {
                    throw new IllegalArgumentException("Invalid seriesId: " + lastPathSegment);
                }
                int seriesId = Integer.parseInt(lastPathSegment);
                if (PvrInfoDatabaseTable.isSeriesTableExists(database, seriesId)) {
                    database.execSQL(PvrInfoDatabaseTable.TableDropSeries(seriesId));
                    rowsDeleted = 1;
                }
                break;
            case MASTER:
                if (PvrInfoDatabaseTable.isMasterTableExists(database)) {
                    rowsDeleted = database.delete(PvrDatabase.TABLE_NAME, selection, selectionArgs);
                }
                break;
            case SERIES:
                String lastPathSegment2 = uri.getLastPathSegment();
                int seriesId2 = Integer.parseInt(lastPathSegment2);
                if (PvrInfoDatabaseTable.isSeriesTableExists(database, seriesId2)) {
                    rowsDeleted = database.delete(PvrDatabase.TABLE_NAME + "_" + seriesId2, selection, selectionArgs);
                }
                break;
            case PLAYBACK:
                if (PvrInfoDatabaseTable.isPlaybackTableExists(database)) {
                    rowsDeleted = database.delete(PvrDatabase.TABLE_NAME_PLAYBACK, selection, selectionArgs);
                }
                break;
            case PLAYBACK_TABLE_DROP:
                if (PvrInfoDatabaseTable.isPlaybackTableExists(database)) {
                    database.execSQL(PvrInfoDatabaseTable.TableDropPlayback());
                    rowsDeleted = 1;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MASTER:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PvrDatabase.TABLE_NAME;
            case SERIES:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PvrDatabase.TABLE_NAME + "_series";
            case PLAYBACK:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PvrDatabase.TABLE_NAME_PLAYBACK;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}
