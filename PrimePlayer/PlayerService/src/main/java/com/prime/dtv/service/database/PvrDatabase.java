package com.prime.dtv.service.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.prime.dtv.service.database.dvbdatabasetable.PvrInfoDatabaseTable;

public class PvrDatabase extends SQLiteOpenHelper {
    private static final String TAG = "PvrDatabase";

    public static final String AUTHORITY = "com.prime.dtv.service.database";
    public static final String LAUNCHER_AUTHORITY = "com.prime.launcher.database";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "PVR_REC_TABLE";

    public static final String TABLE_NAME_PLAYBACK= "PLAYBACK_TABLE";

    public static final String RECORD_FOLDER_NAME = "REC";

    public static final String DROP_SERIES_TABLE = "drop_series_table";
    public static final String DROP_MASTER_TABLE = "drop_master_table";
    public static final String DROP_PLAYBACK_TABLE = "drop_playback_table";
    public static String DATABASE_PATH = null;
    public static String USB_PATH;

    public PvrDatabase(Context context, String usbMountPath) {
        super(context, usbMountPath + "/" + PvrInfoDatabaseTable.DATABASE_NAME, null, DATABASE_VERSION);

        USB_PATH = usbMountPath;
        DATABASE_PATH = usbMountPath + "/" + PvrInfoDatabaseTable.DATABASE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (!PvrInfoDatabaseTable.isMasterTableExists(db)) {
            db.execSQL(PvrInfoDatabaseTable.TableCreateMaster());
            Log.d(TAG, "Master table created.");
        }
        if (!PvrInfoDatabaseTable.isPlaybackTableExists(db)) {
            db.execSQL(PvrInfoDatabaseTable.TableCreatePlayback());
            Log.d(TAG, "Playback table created.");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL(PvrInfoDatabaseTable.TableDropPlayback());
            Log.d(TAG, "Playback table dropped.");
        }
        onCreate(db);
    }

    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        SQLiteDatabase database = null;
        String dbFilePath = DATABASE_PATH;
        if (DATABASE_PATH == null || DATABASE_PATH.isEmpty()) {
            Log.e("PvrDatabase", "USB path is invalid");
            return null;
        }
        try {
            database = SQLiteDatabase.openOrCreateDatabase(dbFilePath, null);
            Log.i("PvrDatabase", "Database opened/created at: " + dbFilePath);
        } catch (Exception e) {
            Log.e("PvrDatabase", "Error opening/creating database", e);
        }
        return database;
    }
}
