package com.prime.dtvplayer.Service.Database;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.prime.dtvplayer.Service.Database.DVBDatabaseTable.FavGroupNameDatabaseTable;
import com.prime.dtvplayer.Service.Database.DVBDatabaseTable.FavInfoDatabaseTable;
import com.prime.dtvplayer.Service.Database.DVBDatabaseTable.GposDatabaseTable;
import com.prime.dtvplayer.Service.Database.DVBDatabaseTable.ProgramDatabaseTable;
import com.prime.dtvplayer.Service.Database.DVBDatabaseTable.SatInfoDatabaseTable;
import com.prime.dtvplayer.Service.Database.DVBDatabaseTable.TpInfoDatabaseTable;
import com.prime.dtvplayer.Service.Database.NetStreamDatabaseTable.NetProgramDatabaseTable;

import java.io.File;

public class DVBDatabase {
    private static final String TAG = "DVBDatabase";
    public static final int DATABASE_VERSION = 1;
    // == database name ==
    public static final String DB_NAME = "DVB_DB.db" ;
//    public static final String DB_PATH = "/data/vendor/dtvdata";
    public static final String DB_PATH = "/data/data/com.prime.dtvplayer/dtvdata";
    public static final String AUTHORITY = "com.prime.dtvplayer.Service.Database";

    public DBOpenHelper mDbOpenHelper = null;

    public DVBDatabase(final Context context, int dbVersion) {
        mDbOpenHelper = new DBOpenHelper(context,DB_NAME,dbVersion);

        Log.d(TAG,"DVBDatabase mDbOpenHelper = "+mDbOpenHelper);
        //mDbPath = context.getApplicationInfo().dataDir + "/databases/";
    }

    public class DBOpenHelper extends SQLiteOpenHelper {

        DBOpenHelper(final Context context, String databaseName,int dbVersion) {
            super(new DatabaseContext(context,DB_PATH), databaseName, null, dbVersion);
            Log.d(TAG,"DBOpenHelper context = "+context);
            Log.d(TAG,"DBOpenHelper name = "+databaseName);

        }
//        public DBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
//            super(context, name, factory, version);
//            Log.d(TAG,"DBOpenHelper context = "+context);
//            Log.d(TAG,"DBOpenHelper name = "+name);
//        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG,"DBOpenHelper SQLiteDatabase = "+db+ " db.getVersion() = "+db.getVersion());

            db.execSQL(GposDatabaseTable.TableCreate());
            db.execSQL(ProgramDatabaseTable.TableCreate());
            db.execSQL(SatInfoDatabaseTable.TableCreate());
            db.execSQL(TpInfoDatabaseTable.TableCreate());
            db.execSQL(FavInfoDatabaseTable.TableCreate());
            db.execSQL(FavGroupNameDatabaseTable.TableCreate());
            db.execSQL(NetProgramDatabaseTable.TableCreate());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG,"onUpgrade oldVersion = "+oldVersion +" newVersion = "+newVersion);
            if(newVersion != oldVersion) {
                db.execSQL(GposDatabaseTable.TableDrop());
                db.execSQL(GposDatabaseTable.TableCreate());
                db.execSQL(ProgramDatabaseTable.TableDrop());
                db.execSQL(ProgramDatabaseTable.TableCreate());
                db.execSQL(SatInfoDatabaseTable.TableDrop());
                db.execSQL(SatInfoDatabaseTable.TableCreate());
                db.execSQL(TpInfoDatabaseTable.TableDrop());
                db.execSQL(TpInfoDatabaseTable.TableCreate());
                db.execSQL(FavInfoDatabaseTable.TableDrop());
                db.execSQL(FavInfoDatabaseTable.TableCreate());
                db.execSQL(FavGroupNameDatabaseTable.TableDrop());
                db.execSQL(FavGroupNameDatabaseTable.TableCreate());
                db.execSQL(NetProgramDatabaseTable.TableDrop());
                db.execSQL(NetProgramDatabaseTable.TableCreate());
            }
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            Log.d(TAG,"DBOpenHelper onOpen = "+db);
            super.onOpen(db);
        }
    }

    public class DatabaseContext extends ContextWrapper {

        private static final String TAG = "DatabaseContext";
        private String mPath;

        public DatabaseContext(Context base,String path) {
            super(base);
            mPath = path;
        }

        @Override
        public File getDatabasePath(String name)
        {
            File result = new File(mPath + File.separator + name);
            Log.d(TAG,"getDatabasePath result = "+result);
            if (!result.getParentFile().exists())
            {
                result.getParentFile().mkdirs();
            }

            return result;
        }
        /* this version is called for android devices >= api-11. thank to @damccull for fixing this. */
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
            return openOrCreateDatabase(name,mode, factory);
        }

        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
            return openOrCreateDatabase(name,mode, factory,errorHandler);
        }
    }
}
