package com.prime.dtv.service.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.prime.dtv.service.database.dvbdatabasetable.BookInfoDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.CNSMailDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.CNSEasDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.FavGroupNameDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.FavInfoDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.GposDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.MailDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.ProgramDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.SatInfoDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.TpInfoDatabaseTable;
import com.prime.dtv.service.database.netstreamdatabasetable.NetProgramDatabaseTable;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class DVBDatabase {
    private static final String TAG = "DVBDatabase";
    public static final int DATABASE_VERSION = 18;//14;

    public static final int DATABASE_SAVE_START = 1;
    public static final int DATABASE_SAVE_END = 0;
    // == database name ==
    public static final String DB_NAME = "DVB_DB.db" ;
    public static final String DB_BACKUP_NAME = "DVB_BACKUP_DB.db" ;
    public static final String DB_PATH = "/data/vendor/dtvdata/Launcher";
    public static final String AUTHORITY = "com.prime.dtv.service.database";
    public static final String LAUNCHER_AUTHORITY = "com.prime.launcher.database";
    public static final String DB_CHECK_FLAT_PATH = "/data/vendor/dtvdata/Launcher/checkDB";
    public DBOpenHelper mDbOpenHelper = null;

    public DVBDatabase(final Context context, int dbVersion) {
        if(!isCorrectDB())
            restoreBackupDatabase();
        mDbOpenHelper = new DBOpenHelper(context,DB_NAME,dbVersion);

        Log.d(TAG,"DVBDatabase mDbOpenHelper = "+mDbOpenHelper);
        //mDbPath = context.getApplicationInfo().dataDir + "/databases/";
    }

    public class DBOpenHelper extends SQLiteOpenHelper {

        DBOpenHelper(final Context context, String databaseName,int dbVersion) {
            super(new DatabaseContext(context, DB_PATH), databaseName, null, dbVersion);
//            super(new DatabaseContext(context,context.getFilesDir().getAbsolutePath()), databaseName, null, dbVersion);///DB_PATH=context.getFilesDir().getAbsolutePath())
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
            db.execSQL(BookInfoDatabaseTable.TableCreate());
            db.execSQL(NetProgramDatabaseTable.TableCreate());
            db.execSQL(MailDatabaseTable.TableCreate());
            db.execSQL(CNSMailDatabaseTable.TableCreate());
            db.execSQL(CNSEasDatabaseTable.TableCreate());
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
                db.execSQL(BookInfoDatabaseTable.TableDrop());
                db.execSQL(BookInfoDatabaseTable.TableCreate());
                db.execSQL(NetProgramDatabaseTable.TableDrop());
                db.execSQL(NetProgramDatabaseTable.TableCreate());
                db.execSQL(MailDatabaseTable.TableDrop());
                db.execSQL(MailDatabaseTable.TableCreate());
                db.execSQL(CNSMailDatabaseTable.TableDrop());
                db.execSQL(CNSMailDatabaseTable.TableCreate());
                db.execSQL(CNSEasDatabaseTable.TableDrop());
                db.execSQL(CNSEasDatabaseTable.TableCreate());
            }
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            Log.d(TAG,"DBOpenHelper onOpen = "+db);
            super.onOpen(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG,"onDowngrade oldVersion = "+oldVersion +" newVersion = "+newVersion);
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
                db.execSQL(BookInfoDatabaseTable.TableDrop());
                db.execSQL(BookInfoDatabaseTable.TableCreate());
                db.execSQL(NetProgramDatabaseTable.TableDrop());
                db.execSQL(NetProgramDatabaseTable.TableCreate());
                db.execSQL(MailDatabaseTable.TableDrop());
                db.execSQL(MailDatabaseTable.TableCreate());
                db.execSQL(CNSMailDatabaseTable.TableDrop());
                db.execSQL(CNSMailDatabaseTable.TableCreate());
                db.execSQL(CNSEasDatabaseTable.TableDrop());
                db.execSQL(CNSEasDatabaseTable.TableCreate());
            }
            //super.onDowngrade(db, oldVersion, newVersion);
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

    public interface SQLiteBinder<T> {
        void bind(SQLiteStatement stmt, T item);
    }

    public static class KeyTuple {
        private final List<Object> keys;

        public KeyTuple(Object... keys) {
            this.keys = Arrays.asList(keys);
        }

        public List<Object> getKeys() {
            return keys;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KeyTuple)) return false;
            KeyTuple other = (KeyTuple) obj;
            return keys.equals(other.keys);
        }

        @Override
        public int hashCode() {
            return keys.hashCode();
        }

        @Override
        public String toString() {
            return keys.toString();
        }
    }

    public <T> void saveListWithInsert(String tableName,List<T> dataList,String insertSql,SQLiteBinder<T> binder) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        SaveDbStart(tableName);
        db.beginTransaction();
        try {

            SQLiteStatement stmt = db.compileStatement(insertSql);
            for (T item : dataList) {
                stmt.clearBindings();
                binder.bind(stmt, item);
                stmt.execute();
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            SaveDbEnd();
        }
    }

    public void deleteNotInDb(String tableName,String sql,List<KeyTuple> keepKeys) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        SaveDbStart(tableName);
        if (keepKeys == null || keepKeys.isEmpty()) {
            SaveDbEnd();
            return;
        }
        SQLiteStatement stmt = db.compileStatement(sql);

        // 綁定所有 key 值
        int bindIndex = 1;
        for (KeyTuple tuple : keepKeys) {
            for (Object key : tuple.getKeys()) {
                Log.d("qqqq","key = "+key);
                if (key instanceof Long) {
                    stmt.bindLong(bindIndex++, (Long) key);
                } else if (key instanceof Integer) {
                    stmt.bindLong(bindIndex++, ((Integer) key).longValue());
                } else if (key instanceof String) {
                    stmt.bindString(bindIndex++, (String) key);
                } else {
                    throw new IllegalArgumentException("Unsupported key type: " + key.getClass());
                }
            }
        }
        stmt.executeUpdateDelete();
        SaveDbEnd();
    }

    private static @NonNull String getDeleteString(String tableName, String[] keyColumns, List<KeyTuple> keepKeys) {
//        StringBuilder whereBuilder = new StringBuilder();
//
//        for (int i = 0; i < keepKeys.size(); i++) {
//            if (i > 0) whereBuilder.append(" OR ");
//            whereBuilder.append("(");
//            for (int j = 0; j < keyColumns.length; j++) {
//                if (j > 0) whereBuilder.append(" AND ");
//                whereBuilder.append(keyColumns[j]).append(" = ?");
//            }
//            whereBuilder.append(")");
//        }

        // 組成最終 DELETE SQL
//        String sql = "DELETE FROM " + tableName + " WHERE NOT (" + whereBuilder + ")";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keepKeys.size(); i++) {
            sb.append("?");
            if (i < keepKeys.size() - 1) sb.append(",");
        }
        String sql = "DELETE FROM "+tableName+" WHERE ChannelId NOT IN (" + sb + ")";
        return sql;
    }

    public <T> void saveListWithUpsert(String tableName,String[] keyColumns,List<T> dataList,String insertSql,SQLiteBinder<T> binder,
            Function<T, KeyTuple> keyExtractor) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        SaveDbStart(tableName);
        db.beginTransaction();
        try {
            HashSet<KeyTuple> keepKeys = new HashSet<>();

            SQLiteStatement stmt = db.compileStatement(insertSql);
            for (T item : dataList) {
                keepKeys.add(keyExtractor.apply(item));
            }

            // 查詢 DB 中的主鍵
            Cursor cursor = db.query(tableName, keyColumns, null, null, null, null, null);
            while (cursor.moveToNext()) {
                Object[] keyValues = new Object[keyColumns.length];
                for (int i = 0; i < keyColumns.length; i++) {
                    int type = cursor.getType(i);
                    switch (type) {
                        case Cursor.FIELD_TYPE_INTEGER:
                            keyValues[i] = cursor.getLong(i);
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            keyValues[i] = cursor.getString(i);
                            break;
                        default:
                            keyValues[i] = null;
                            break;
                    }
                }
                KeyTuple dbKey = new KeyTuple(keyValues);
                if (!keepKeys.contains(dbKey)) {
                    StringBuilder where = new StringBuilder();
                    String[] whereArgs = new String[keyColumns.length];
                    for (int i = 0; i < keyColumns.length; i++) {
                        if (i > 0) where.append(" AND ");
                        where.append(keyColumns[i]).append(" = ?");
                        whereArgs[i] = String.valueOf(keyValues[i]);
                    }
                    db.delete(tableName, where.toString(), whereArgs);
                }
            }
            cursor.close();

            for (T item : dataList) {
                stmt.clearBindings();
                binder.bind(stmt, item);
                stmt.execute();
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            SaveDbEnd();
        }
    }


    public void SaveDbStart(String tableName) {
        File file = new File(DB_CHECK_FLAT_PATH);
        String str = ""+DATABASE_SAVE_START;
        if(file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(str.getBytes(StandardCharsets.UTF_8));
//                Log.d(TAG,"SaveDbStart table name = "+tableName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void SaveDbEnd() {
        File file = new File(DB_CHECK_FLAT_PATH);
        String str = ""+DATABASE_SAVE_END;
        if(file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(str.getBytes(StandardCharsets.UTF_8));
//                Log.d(TAG,"SaveDbEnd");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getDBCheckFlag() {
        File file = new File(DB_CHECK_FLAT_PATH);
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    public boolean isCorrectDB() {
        String flagStr = getDBCheckFlag();
        boolean result = false;
        if(flagStr != null && flagStr.matches("\\d+")) {
            Integer flag = Integer.parseInt(flagStr);
//            Log.d(TAG,"flag = "+flag);
            if(Integer.parseInt(flagStr) != 0)
                result = false;
            else
                result = true;
        }
//        Log.d(TAG,"isCorrectDB() = "+result);
        return result;
    }

    public static boolean backupDatabase(boolean force) {
        String backupPath = DB_PATH+"/"+DB_BACKUP_NAME;
        DVBDatabase dvbDatabase = DVBContentProvider.getDvbDb();
        if(dvbDatabase == null)
            return false;
        boolean isCorrectDb = dvbDatabase.isCorrectDB();
        if(!isCorrectDb && !force)
            return false;
        try {
            SQLiteDatabase srcDb = DVBContentProvider.getDvbDb().mDbOpenHelper.getReadableDatabase();
            File backupFile = new File(backupPath);
            if (backupFile.exists()) {
                if (!backupFile.delete()) {
                    Log.e(TAG, "can not delete backup DB file: " + backupPath);
                    return false;
                }
            }
            srcDb.execSQL("VACUUM INTO '" + backupPath + "'");
            Log.d(TAG, "DB backup success : " + backupPath);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "DB backup failed", e);
            return false;
        }
    }

    public boolean restoreBackupDatabase() {
        String backupPath = DB_PATH+"/"+DB_BACKUP_NAME;
        String originalDbPath = DB_PATH+"/"+DB_NAME;
        try {
            File backupFile = new File(backupPath);
            File originalFile = new File(originalDbPath);

            if (!backupFile.exists()) {
                Log.e("DBBackup", "備份檔不存在，無法還原: " + backupPath);
                return false;
            }

            // 用 Java NIO 複製檔案（需 API 26+）
            Files.copy(
                    backupFile.toPath(),
                    originalFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            Log.i("DBBackup", "資料庫成功還原: " + originalDbPath);
            return true;
        } catch (Exception e) {
            Log.e("DBBackup", "還原資料庫失敗", e);
            return false;
        }
    }

    public static class BackupWorker extends Worker {

        public BackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            boolean result = backupDatabase(false);
            return result ? Result.success() : Result.failure();
        }

        public static void scheduleDailyBackup(Context context) {
            long currentTime = System.currentTimeMillis();
            Calendar now = Calendar.getInstance();
            Calendar next3am = Calendar.getInstance();
            next3am.set(Calendar.HOUR_OF_DAY, 3);
            next3am.set(Calendar.MINUTE, 0);
            next3am.set(Calendar.SECOND, 0);
            if (now.after(next3am)) {
                next3am.add(Calendar.DAY_OF_MONTH, 1);
            }

            long initialDelay = next3am.getTimeInMillis() - currentTime;

            PeriodicWorkRequest request =
                    new PeriodicWorkRequest.Builder(BackupWorker.class, 24, TimeUnit.HOURS)
                            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                            .build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "daily_db_backup",
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE ,
                    request
            );
        }
    }

    public void insert_new_colume(String tableName, String columeName, String columeType){
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        String addColumnQuery = "ALTER TABLE "+tableName+" ADD COLUMN "+columeName+" "+columeType;
        Log.d(TAG, "addColumnQuery = "+addColumnQuery);
        if(db != null){
            db.execSQL(addColumnQuery);
        }

    }

    public boolean isColumnExists(String tableName, String columnName) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
            if (name.equals(columnName)) {
                cursor.close();
                return true;
            }
        }
        cursor.close();
        return false;
    }
}
