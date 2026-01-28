package com.prime.acsclient.prodiver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ACSDatabase extends SQLiteOpenHelper {
    private static final String TAG = "ACS_ACSDatabase" ;
    private static final int DATABASE_VERSION = 1;
    public static final String ACS_DATA_COL_ID = "_ID";
    public static final String ACS_DATA_COL_NAME = "name";
    public static final String ACS_DATA_COL_VALUE = "value";
    public static final String PROVIDER_DB_TABLE_NAME = "PESI_ACS_DATA_TABLE" ;
    private static final String CREATE_MESSAGE_TABLE_TUTORIALS =
            "create table " + PROVIDER_DB_TABLE_NAME + " (_id integer primary key autoincrement, app_type text not null, time long not null, data text not null);";
    private static final String CREATE_ACS_DATA_TABLE =
            "create table " + PROVIDER_DB_TABLE_NAME + " ("
                    + ACS_DATA_COL_ID + " integer primary key autoincrement, "
                    + ACS_DATA_COL_NAME + " text not null unique, "
                    + ACS_DATA_COL_VALUE + " text);";

    private static final String DB_NAME = "acs_provider_db.db";
    public ACSDatabase(Context context) {
        super(context, DB_NAME, (SQLiteDatabase.CursorFactory) null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        db.execSQL(CREATE_ACS_DATA_TABLE);
//        db.execSQL(CREATE_MESSAGE_TABLE_TUTORIALS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        Log.d(TAG, "Upgrading database. Existing contents will be lost. [" + oldVersion + "]->[" + newVersion + "]");
        db.execSQL("DROP TABLE IF EXISTS " + PROVIDER_DB_TABLE_NAME );
//        db.execSQL("DROP TABLE IF EXISTS " + PROVIDER_DB_TABLE_NAME );
        onCreate(db);
    }
}
