package com.prime.dtvplayer.Database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBOpenHelper";
    private static final String DB_NAME = "dvb_db.dat";
    private static final int DB_VERSION = 1;

    public DBOpenHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);

    }

    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DBChannel.TableCreate());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int OldVersion, int NewVersion) {

    }

}