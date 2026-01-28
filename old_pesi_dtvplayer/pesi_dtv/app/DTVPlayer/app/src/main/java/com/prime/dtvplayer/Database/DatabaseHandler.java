package com.prime.dtvplayer.Database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class DatabaseHandler
{
    private final static String TAG = "DatabaseHandler";
    private final static String ID = "_ID";
	private final static int SUCCESS = 0;
	private final static int FAIL = -1;
    public final static String TYPE_STRING = "TEXT";
	public final static String TYPE_INTEGER = "INTEGER";
	public final static String TYPE_FLOAT = "REAL";
    private Context mCont;
    private String mDbName;
    private String mDbPath;
    private SQLiteDatabase mDB;
    private StringBuilder mCreateTable;
    private boolean mLock;

	public static class Column
	{
		public String name;
		public String value;

        public Column () {}
        public Column(String colName, String colValue)
		{
			name = colName;
			value = colValue;
		}
        public Column(String colName, long colValue)
		{
			name = colName;
			value = String.valueOf(colValue);
		}
        public Column(String colName, double colValue)
		{
			name = colName;
			value = String.valueOf(colValue);
		}
	}

	public DatabaseHandler ( Context context, String databaseName )
	{
		mCont = context;
		mDbName = databaseName;
		mDB = null;
        mLock = false;
	}

	public int openDatabase()
	{
	    try
        {
            int isExist;
            SQLiteOpenHelper helper = new SQLiteOpenHelper(mCont, mDbName, null, 1)
            {
                @Override
                public void onCreate ( SQLiteDatabase db )
                {

                }

                @Override
                public void onUpgrade ( SQLiteDatabase db, int oldVersion, int newVersion )
                {

                }
            };
            mDB = helper.getWritableDatabase();
            mDbPath = mDB.getPath();
            mCreateTable = null;
            isExist = mDB != null ? SUCCESS : FAIL;
            Log.d(TAG, "openDatabase: isExist = "+isExist+" getPath = "+ Objects.requireNonNull(mDB).getPath());
            return isExist;
        }
        catch ( Exception e )
        {
            Log.d(TAG, "openDatabase: "+e.getMessage());
            e.printStackTrace();
            return FAIL;
        }
	}

	public int openDatabase(String customPath)
	{
        String dbPath = "";
        int isExist;
	    try
        {
            if ( customPath.endsWith("/") )
            {
                dbPath = customPath + mDbName;
            }
            else
            {
                dbPath = customPath + "/" + mDbName;
            }
            mDB = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
            mDbPath = mDB.getPath();
            mCreateTable = null;
            isExist = mDB != null ? SUCCESS : FAIL;
            Log.d(TAG, "openDatabase: isExist = "+isExist+" getPath = "+ Objects.requireNonNull(mDB).getPath());
            return isExist;
        }
        catch ( Exception e )
        {
            if ( e instanceof SQLiteCantOpenDatabaseException )
            {
                Log.d(TAG, "openDatabase: database Path = "+dbPath);
            }
            Log.d(TAG, "openDatabase: "+e.getMessage());
            e.printStackTrace();
            return FAIL;
        }
	}

	public int closeDatabase()
	{
	    //http://blog.sina.com.cn/s/blog_5de73d0b0102w0g0.html
	    //https://stackoverflow.com/questions/4557154/android-sqlite-db-when-to-close
	    try
        {
            if ( mDB == null )
            {
                Log.d(TAG, "closeDatabase: null database");
                return FAIL;
            }
            mDB.close();
            return SUCCESS;
        }
        catch ( Exception e )
        {
            Log.d(TAG, "closeDatabase: FAIL: "+e.getMessage());
            e.printStackTrace();
            return FAIL;
        }
	}

	public int removeDatabase()
	{
	    try
        {
            if ( mDB == null )
            {
                Log.d(TAG, "removeDatabase: null database");
                return FAIL;
            }
            closeDatabase();
            boolean ret = mCont.deleteDatabase(mDbName);
            Log.d(TAG, "removeDatabase: ret = "+ret);
            return ret ? SUCCESS : FAIL;
        }
	    catch ( Exception e )
        {
            Log.d(TAG, "removeDatabase: "+e.getMessage());
            e.printStackTrace();
            return FAIL;
        }
	}

	public int checkDatabase()
	{
		try
        {
            //if ( mDB == null || ! mDB.isOpen() )
            //{
            //    Log.d(TAG, "checkDatabase: Database is closed");
            //    return FAIL;
            //}
			SQLiteDatabase checkDB = SQLiteDatabase.openDatabase(mDbPath,
                    null, SQLiteDatabase.OPEN_READONLY);
			checkDB.close();
            Log.d(TAG, "checkDatabase: SUCCESS");
            return SUCCESS;
		}
		catch ( Exception e )
        {
            Log.d(TAG, "checkDatabase: FAIL: "+e.getMessage());
			e.printStackTrace();
			return FAIL;
		}
	}

    // =================================== CREATE ===================================
	public int addColumn(String table, String column, String type, boolean unique)
    {
        try
        {
            if ( !databaseOK() )
            {
                return FAIL;
            }
            if ( table == null || table.isEmpty() || column == null || column.isEmpty() ||
                    type == null || type.isEmpty() )
            {
                Log.d(TAG, "addColumn: empty parameter");
                return FAIL;
            }
            if ( mCreateTable == null || mCreateTable.toString().isEmpty() )
            {
                mCreateTable = new StringBuilder("CREATE TABLE IF NOT EXISTS " +
                        table +
                        " (_ID INTEGER PRIMARY KEY AUTOINCREMENT");
            }
            String currentTable = mCreateTable.toString().split(" ")[5];
            if ( ! table.equals(currentTable) )
            {
                Log.d(TAG, "addColumn: FAIL: current table is "+currentTable+
                        ", incorrect table = "+table);
                return FAIL;
            }
            mCreateTable.append(", ");
            mCreateTable.append(column).append(" ");
            mCreateTable.append(type).append(" ");
            //mCreateTable.append("NOT NULL").append(" ");

            if ( unique )
            {
                mCreateTable.append("UNIQUE");
            }
        }
        catch ( Exception e )
        {
            Log.d(TAG, "addColumn: FAIL: "+e.getMessage());
            e.printStackTrace();
            return FAIL;
        }
        return SUCCESS;
    }

	public int createTable()
	{
		try
        {
            if ( ! databaseOK() )
            {
                return FAIL;
            }
            if ( mCreateTable == null )
            {
                Log.d(TAG, "createTable: please addColumn() before createTable()");
                return FAIL;
            }
            String createTable = mCreateTable.append(");").toString();
			mDB.execSQL(createTable);
            mCreateTable = null;
			Log.d(TAG, "createTable: "+createTable);
            return SUCCESS;
		}
		catch ( Exception e )
        {
            Log.d(TAG, "createTable: FAIL: "+e.getMessage());
			e.printStackTrace();
			return FAIL;
		}
	}

	public int removeTable(String table)
	{
		try
        {
            if ( table == null || table.isEmpty() )
            {
                Log.d(TAG, "removeTable: no table name");
                return FAIL;
            }
            if ( ! databaseOK() )
            {
                return FAIL;
            }
            String dropTable = "DROP TABLE IF EXISTS " + table;
			mDB.execSQL(dropTable);
            mCreateTable = null;
			Log.d(TAG, "removeTable: "+dropTable);
            return SUCCESS;
		}
		catch ( Exception e )
        {
            Log.d(TAG, "removeTable: FAIL: "+e.getMessage());
			e.printStackTrace();
			return FAIL;
		}
	}

    //public long addUnique(String table, String columnName)
    //{
    //    https://stackoverflow.com/questions/35156488/add-unique-column-to-already-exist-sqlite-table
    //    if ( table == null || columnName == null || table.isEmpty() || columnName.isEmpty() )
    //        return FAIL;
    //    // ALTER TABLE ____ ADD UNIQUE ( ____ );
    //    String ADD_UNIQUE = "ALTER TABLE "+table+" ADD UNIQUE ("+columnName+");";
    //    Log.d(TAG, "addUnique: "+ADD_UNIQUE);
    //    try {
    //        mDB.execSQL(ADD_UNIQUE);
    //    } catch ( SQLiteException e ) {
    //        Log.d(TAG, "addUnique: FAIL");
    //        e.printStackTrace();
    //        return FAIL;
    //    }
    //    Log.d(TAG, "addUnique: SUCCESS");
    //    return SUCCESS;
    //}

    // =================================== APPEND ===================================
    public long append( String table, List<Column> inputList )
    {
        long rowIndex = -1;
        try
        {
            if ( ! databaseOK() || table == null || inputList == null || table.isEmpty() ||
                    inputList.size() < 1 )
            {
                return FAIL;
            }
            rowIndex = getRowCount(table) + 1;
            ContentValues cv = new ContentValues();
            for ( Column input : inputList )
            {
                cv.put(input.name, input.value);
            }
            mDB.insertOrThrow(table, null, cv);
            return rowIndex;
        }
        catch ( Exception e )
        {
            if ( e instanceof SQLiteConstraintException )
            {
                Log.d(TAG, "append: FAIL: SQLiteConstraintException = "+e.getMessage());
                deleteFrom(table, rowIndex);
                return FAIL;
            }
            Log.d(TAG, "append: FAIL: "+e.getMessage());
            e.printStackTrace();
            return FAIL;
        }
    }

	public long appendNull( String table )
	{
        long rowIndex = getRowCount(table) + 1;
        try
        {
            if ( ! databaseOK() || table == null || table.isEmpty() || rowIndex < 1 )
            {
                Log.d(TAG, "appendNull: row index = "+rowIndex);
                return FAIL;
            }
            ContentValues cv = new ContentValues();
            for ( String col : getColumnNameAll(table) )
            {
                cv.putNull(col);
            }
            mDB.insert(table, null, cv);
            Log.d(TAG, "appendNull: row index = "+rowIndex);
            return rowIndex;
		}
		catch ( Exception e )
        {
            Log.d(TAG, "appendNull: FAIL: "+e.getMessage());
			e.printStackTrace();
			return FAIL;
		}
	}
    //boolean append( String table, String name, String value )
    //{
    //    if ( table == null || name == null || table.isEmpty())
    //        return false;
    //
    //    ContentValues cv = new ContentValues();
    //    cv.put(name, value);
    //    if ( type.equals(TYPE_INTEGER) )		cv.put(name, Long.valueOf(value));
    //    else if ( type.equals(TYPE_STRING) ) 	cv.put(name, String.valueOf(value));
    //    else if ( type.equals(TYPE_FLOAT) )		cv.put(name, Double.valueOf(value));
    //    else
    //        return false;
    //    try
    //    {
    //        mDB.insert(table, null, cv);
    //    }
    //    catch ( SQLiteException e )
    //    {
    //        e.printStackTrace();
    //        return false;
    //    }
    //    return true;
    //}

    // =================================== WRITE ===================================
    // String table:    table name
    // long rowIndex:   at row number
    // String colName:  at column name
    // long newValue:   write newValue
    public int writeTo( String table, long rowIndex, String colName, long newValue )
    {
        return writeTo(table, rowIndex, getColumnIndex(table, colName), String.valueOf(newValue));
    }

    public int writeTo( String table, long rowIndex, String colName, double newValue )
    {
        return writeTo(table, rowIndex, getColumnIndex(table, colName), String.valueOf(newValue));
    }

    public int writeTo( String table, long rowIndex, String colName, String newValue )
    {
        return writeTo(table, rowIndex, getColumnIndex(table, colName), newValue);
    }

    // String table:    table name
    // long rowIndex:   at row number
    // int colIndex:    at column number
    // long newValue:   write newValue
    public int writeTo( String table, long rowIndex, int colIndex, long newValue )
    {
        return writeTo(table, rowIndex, colIndex, String.valueOf(newValue));
    }

    public int writeTo( String table, long rowIndex, int colIndex, double newValue )
    {
        return writeTo(table, rowIndex, colIndex, String.valueOf(newValue));
    }

    public int writeTo( String table, long rowIndex, int colIndex, String newValue )
    {
        try
        {
            if ( ! databaseOK() )
            {
                return FAIL;
            }
            if ( colIndex < 1 || rowIndex < 1 )
            {
                Log.d(TAG, "writeTo: column index = "+colIndex+", row index = "+rowIndex+
                        ", cannot less than 1");
                return FAIL;
            }
            Long sequence_id = getID(table, rowIndex);
            Log.d(TAG, "writeTo: sequence_id = "+sequence_id);
            String columnName = getColumnName(table, colIndex);
            ContentValues cv = new ContentValues();
            cv.put(columnName, newValue);
            mDB.update(table, cv, ID+"="+sequence_id, null);
            return SUCCESS;
        }
        catch ( Exception e )
        {
            if ( e instanceof SQLiteConstraintException )
            {
                Log.d(TAG, "writeTo: FAIL: SQLiteConstraintException = "+e.getMessage());
                //deleteByRow(table, rowIndex);
                return FAIL;
            }
            e.printStackTrace();
            Log.d(TAG, "writeTo: FAIL: "+e.getMessage());
            return FAIL;
        }
    }

    // String table:    table name
    // String colName:  column name
    // long value:      current value
    // long newValue:   new value
    public int write( String table, String colName, long value, long newValue )
    {
        return write(table, getColumnIndex(table, colName), String.valueOf(value), String.valueOf(newValue));
    }

    public int write( String table, String colName, double value, double newValue )
    {
        return write(table, getColumnIndex(table, colName), String.valueOf(value), String.valueOf(newValue));
    }

    public int write( String table, String colName, String value, String newValue )
    {
        return write(table, getColumnIndex(table, colName), String.valueOf(value), String.valueOf(newValue));
    }

    // String table:    table name
    // int colIndex:    column number
    // long value:      current value
    // long newValue:   new value
    public int write( String table, int colIndex, long value, long newValue )
    {
        return write(table, colIndex, String.valueOf(value), String.valueOf(newValue));
    }

    public int write ( String table, int colIndex, double value, double newValue )
    {
        return write(table, colIndex, String.valueOf(value), String.valueOf(newValue));
    }

    public int write ( String table, int colIndex, String value, String newValue )
    {
        try
        {
            if ( ! databaseOK() || colIndex < 1 )
            {
                return FAIL;
            }
            String colName = getColumnName(table, colIndex);
            ContentValues cv = new ContentValues();
            cv.put(colName, newValue);
            mDB.update(table, cv, colName+" = ?", new String[]{value});
            return SUCCESS;
        }
        catch ( Exception e )
        {
            if ( e instanceof SQLiteConstraintException )
            {
                Log.d(TAG, "write: FAIL: "+e.getMessage());
                return FAIL;
            }
            e.printStackTrace();
            Log.d(TAG, "write: FAIL: "+e.getMessage());
            return FAIL;
        }
    }

    // =================================== READ ===================================
    // String table:    table name
    // long rowIndex:   read from row number
    public List<Column> readFrom( String table, long rowIndex )
    {
        try
        {
            if ( ! databaseOK() || rowIndex < 1 )
            {
                return new ArrayList<>();
            }
            Long sequence_id = getID(table, rowIndex);
            Cursor cursor = mDB.query(table, null,
                    ID+"="+sequence_id, //ID+" like ?",
                    null, //new String[]{sequence_id},
                    null, null, null );
            List<Column> row = new ArrayList<>();
            int colCount = cursor.getColumnCount();
            String[] colNames = cursor.getColumnNames();
            String t = ""; int j = 1; t+="\n"+(j);
            while ( cursor.moveToNext() )
            {
                for ( int i = 1; i < colCount; i++ )
                {
                    Column col = new Column(colNames[i], cursor.getString(i)); t += " "+col.name+"="+col.value;
                    row.add(col);
                }
                j++; t += "\n"+j;
            }
            Log.d(TAG, "readFrom: "+t);
            cursor.close();
            return row;
        }
        catch ( Exception e )
        {
            Log.d(TAG, "readFrom: FAIL: "+e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // String table:    read row list from table name
    public List<List<Column>> readAll( String table )
    {
        try
        {
            if ( ! databaseOK() )
            {
                return new ArrayList<>();
            }
            List<List<Column>> rowList = new ArrayList<>();
            //Cursor cursor = mDB.query(table, null, ID+" like ?", new String[]{"%%"}, null, null, null );
            Cursor cursor = mDB.query(table, null, null, null
                    , null, null, null );
            String t = "";
            int j = 1;

            while ( cursor.moveToNext() )
            {
                List<Column> row = new ArrayList<>();
                t += "\n"+j+" "+ID+"="+cursor.getLong(0)+" ";
                for ( int i = 1; i < cursor.getColumnCount(); i++ )
                {
                    Column col = new Column();
                    col.name = cursor.getColumnName(i);
                    col.value = cursor.getString(i);
                    row.add(col);
                    t += col.name + "=" + col.value+" ";
                }
                rowList.add(row);
                j++;
            }
            t += "\nEND --------------------------------------------------------------------------------------------------------------------------------\n";
            Log.d(TAG, "readAll: " + t);
            cursor.close();
            return rowList;
        }
        catch ( Exception e )
        {
            Log.d(TAG, "readAll: FAIL: "+e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // read rows according to current value
    // String table:    table name
    // int colIndex:    column number
    // long value:      current value
    public List<List<Column>> read( String table, int colIndex, long value )
    {
        return read(table, getColumnName(table, colIndex), String.valueOf(value));
    }

    public List<List<Column>> read( String table, int colIndex, double value )
    {
        return read(table, getColumnName(table, colIndex), String.valueOf(value));
    }

    public List<List<Column>> read( String table, int colIndex, String value )
    {
        return read(table, getColumnName(table, colIndex), (value));
    }

    // read rows according to current value
    // String table:    table name
    // String colName:  column name
    // long value:      current value
    public List<List<Column>> read( String table, String colName, long value )
    {
        return read(table, colName, String.valueOf(value));
    }

    public List<List<Column>> read( String table, String colName, double value )
    {
        return read(table, colName, String.valueOf(value));
    }

    public List<List<Column>> read( String table, String colName, String value )
    {
        try
        {
            if ( ! databaseOK() )
            {
                return new ArrayList<>();
            }
            List<List<Column>> rowList = new ArrayList<>();
            Cursor cursor = mDB.query(table, null, colName+"=?", new String[]{value}, null, null, null );
            //Cursor cursor = mDB.query(table, null, colName+" like ?", new String[]{value}, null, null, null );
            String t = "";
            int j = 1;

            while ( cursor.moveToNext() )
            {
                List<Column> row = new ArrayList<>();
                t += "\n"+j+" "+ID+"="+cursor.getLong(0)+" ";
                for ( int i = 1; i < cursor.getColumnCount(); i++ )
                {
                    Column col = new Column();
                    col.name = cursor.getColumnName(i);
                    col.value = cursor.getString(i);
                    row.add(col);
                    t += col.name + "=" + col.value+" ";
                }
                rowList.add(row);
                j++;
            }
            Log.d(TAG, "read: " + t);
            cursor.close();
            return rowList;
        }
        catch ( Exception e )
        {
            Log.d(TAG, "read: FAIL: "+e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // =================================== DELETE ===================================
    // delete from row number
    // String table:    table name
    // long rowIndex:     row number
    public int deleteFrom( String table, long rowIndex )
    {
        try
        {
            if ( ! databaseOK() || rowIndex < 1 )
            {
                return FAIL;
            }
            long sequence_id = getID(table, rowIndex);
            String where = ID+"="+sequence_id;
            Log.d(TAG, "deleteFrom: sequence_id = "+sequence_id);
            mDB.delete(table, where, null);
            return SUCCESS;
        }
        catch ( SQLiteException e )
        {
            e.printStackTrace();
            Log.d(TAG, "deleteFrom: FAIL: "+e.getMessage());
            return FAIL;
        }
    }

    // String table:    table name
    // int colIndex:    column number
    // long value:      current value
    public int delete( String table, int colIndex, long value )
    {
        return delete(table, getColumnName(table, colIndex), String.valueOf(value));
    }

    public int delete( String table, int colIndex, double value )
    {
        return delete(table, getColumnName(table, colIndex), String.valueOf(value));
    }

    public int delete( String table, int colIndex, String value )
    {
        return delete(table, getColumnName(table, colIndex), (value));
    }

    // String table:    table name
    // String colName:  column name
    // long value:      current value
    public int delete( String table, String colName, long value )
    {
        return delete(table, colName, String.valueOf(value));
    }

    public int delete( String table, String colName, double value )
    {
        return delete(table, colName, String.valueOf(value));
    }

    public int delete( String table, String colName, String value )
    {
        try
        {
            if ( ! databaseOK() )
            {
                return FAIL;
            }
            int deleteCount = mDB.delete(table, colName+" = ?", new String[]{value});
            Log.d(TAG, "delete: "+value+", delete count = "+deleteCount);
            if ( deleteCount == 0 )
            {
                Log.d(TAG, "delete: FAIL: data not exist");
                return FAIL;
            }
            return SUCCESS;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            Log.d(TAG, "delete: FAIL: "+e.getMessage());
            return FAIL;
        }
    }

    // =================================== SORT ===================================
    public List<List<Column>> sort(String table, int colIndex, boolean descending)
    {
        return sort(table, getColumnName(table, colIndex), descending);
    }

    public List<List<Column>> sort(String table, String colName, boolean descending)
    {
        try
        {
            if ( ! databaseOK() )
            {
                return new ArrayList<>();
            }
            List<List<Column>> rowList = new ArrayList<>();
            String orderBy = descending ? colName.concat(" DESC") : colName;
            Cursor cursor = mDB.query(table, null, null, null, null, null, orderBy);
            long colCount = cursor.getColumnCount();
            //String t = "";
            while ( cursor.moveToNext() )
            {
                //t += "\n";
                List<Column> row = new ArrayList<>();
                for ( int i = 1; i < colCount; i++ )
                {
                    Column col = new Column();
                    col.name = cursor.getColumnName(i);
                    col.value = cursor.getString(i);
                    row.add(col);
                }
                //for ( int i = 0; i < colCount; i++ )
                //{
                //    t += cursor.getColumnName(i)+"="+cursor.getString(i)+" ";
                //}
                rowList.add(row);
            }
            //Log.d(TAG, "sort: t = "+t);
            cursor.close();

            return rowList;
        }
        catch ( Exception e )
        {
            Log.d(TAG, "sort: FAIL: "+e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<List<Column>> sortReserve( final String table, int colIndex, boolean descending)
    {
        // ____________________ method will cause ANR ____________________

        return sortReserve(table, getColumnName(table, colIndex), descending);
    }

    public List<List<Column>> sortReserve( final String table, String colName, boolean descending)
    {
        // ____________________ method will cause ANR ____________________

        try
        {
            if ( !databaseOK() )
            {
                return new ArrayList<>();
            }
            List<List<Column>> rowList = sort(table, colName, descending);
            long rowTotalSize = rowList.size();
            final ContentValues cv = new ContentValues();
            for ( String col : getColumnNameAll(table) )
            {
                cv.putNull(col); // ID should not be update
            }
            for ( int rowIndex = 1; rowIndex <= rowTotalSize; rowIndex++ )
            {
                Long sequence_id = getID(table, rowIndex);
                mDB.update(table, cv, ID+"="+sequence_id, null);
            }
            for ( int rowIndex = 1; rowIndex <= rowTotalSize; rowIndex++ )
            {
                final Long sequence_id = getID(table, rowIndex);
                List<Column> row = rowList.get(rowIndex - 1);
                for ( Column col : row )
                {
                    cv.put(col.name, col.value); // ID should not be update
                }
                mDB.update(table, cv, ID+"="+sequence_id, null);
            }
//            Thread updateAll = new Thread(new Runnable()
//            {
//                @Override
//                public void run ()
//                {
//                    mLock = true;
//                    for ( int rowIndex = 1; rowIndex <= rowList.size(); rowIndex++ )
//                    {
//                        Long sequence_id = getID(table, rowIndex);
//                        List<Column> row = rowList.get(rowIndex - 1);
//                        for ( Column col : row )
//                        {
//                            cv.putNull(col.name); // ID should not be update
//                        }
//                        mDB.update(table, cv, ID+"="+sequence_id, null);
//                    }
//                    for ( int rowIndex = 1; rowIndex <= rowList.size(); rowIndex++ )
//                    {
//                        final Long sequence_id = getID(table, rowIndex);
//                        List<Column> row = rowList.get(rowIndex - 1);
//                        for ( Column col : row )
//                        {
//                            cv.put(col.name, col.value); // ID should not be update
//                        }
//                        mDB.update(table, cv, ID+"="+sequence_id, null);
//                    }
//                    mLock = false;
//                }
//            });
//            updateAll.start();
            return rowList;
        }
        catch ( Exception e )
        {
            Log.d(TAG, "sortReserve: FAIL: "+e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // =================================== Get Row Index ===================================
    public long getRowIndex(String table, int colIndex, long value)
    {
        return getRowIndex(table, getColumnName(table, colIndex), String.valueOf(value));
    }

    public long getRowIndex(String table, int colIndex, double value)
    {
        return getRowIndex(table, getColumnName(table, colIndex), String.valueOf(value));
    }

    public long getRowIndex(String table, int colIndex, String value)
    {
        return getRowIndex(table, getColumnName(table, colIndex), (value));
    }

    public long getRowIndex(String table, String colName, long value)
    {
        return getRowIndex(table, colName, String.valueOf(value));
    }

    public long getRowIndex(String table, String colName, double value)
    {
        return getRowIndex(table, colName, String.valueOf(value));
    }

    public long getRowIndex(String table, String colName, String value)
    {
        try
        {
            if ( ! databaseOK() )
            {
                return FAIL;
            }
            String colValue;
            boolean foundRow = false;
            int rowIndex = 1;
            Cursor cursor = mDB.query(table, new String[]{colName}, null, null, null, null, ID);
            while ( cursor.moveToNext() )
            {
                colValue = cursor.getString(0);
                Log.d(TAG, "getRowIndex: colValue = "+colValue+", value = "+value);
                if ( colValue != null && colValue.equals(value) )
                {
                    foundRow = true;
                    break;
                }
                rowIndex++;
            }
            cursor.close();
            rowIndex = foundRow ? rowIndex : FAIL;
            Log.d(TAG, "getRowIndex: rowIndex = "+rowIndex);
            return rowIndex;
        }
        catch ( Exception e )
        {
            Log.d(TAG, "getRowIndex: FAIL: "+e.getMessage());
            e.printStackTrace();
            return FAIL;
        }
    }

    public long getRowCount(String table)
    {
        try
        {
            if ( ! databaseOK() )
            {
                Log.d(TAG, "getRowCount: Database is closed");
                return FAIL;
            }
            return DatabaseUtils.queryNumEntries(mDB, table);
        }
        catch ( Exception e )
        {
            Log.d(TAG, "getRowCount: "+e.getMessage());
            e.printStackTrace();
            return FAIL;
        }
    }

    private int getColumnIndex(String table, String colName)
    {
        try
        {
            if ( ! databaseOK() )
            {
                Log.d(TAG, "getColumnIndex: Database is closed");
                return FAIL;
            }
            Cursor cursor = mDB.query(table, null, null, null
                    , null, null, null, null);
            int colIndex = cursor.getColumnIndex(colName);
            cursor.close();
            return colIndex;
        }
        catch ( Exception e )
        {
            Log.d(TAG, "getColumnIndex: "+e.getMessage());
            e.printStackTrace();
            return FAIL;
        }
    }

    public String getColumnName(String table, int colIndex)
    {
        String columnName = "";
        try
        {
            if ( ! databaseOK() )
            {
                return columnName;
            }
            if ( colIndex < 1 )
            {
                Log.d(TAG, "getColumnName: FAIL: index < 1");
                return columnName;
            }
            Cursor cursor = mDB.query(table, null, null, null
                    , null, null, null, null);
            columnName = cursor.getColumnName(colIndex);
            cursor.close();
            //Log.d(TAG, "getColumnName: "+columnName);
            return columnName;
        }
        catch ( Exception e )
        {
            Log.d(TAG, "getColumnName: FAIL: "+e.getMessage());
            e.printStackTrace();
            return columnName;
        }
    }

    public List<String> getColumnNameAll(String table)
    {
        List<String> columnNameAll = new ArrayList<>();
        try
        {
            if ( ! databaseOK() )
            {
                Log.d(TAG, "getColumnNameAll: Database is closed");
                return columnNameAll;
            }
            Cursor cursor = mDB.query(table, null, null, null
                    , null, null, null, null);
            columnNameAll = new LinkedList<>(Arrays.asList(cursor.getColumnNames()));
            columnNameAll.remove(0); // remove ID column
            cursor.close();
            return columnNameAll;
        }
        catch ( Exception e )
        {
            Log.d(TAG, "getColumnNameAll: FAIL: "+e.getMessage());
            e.printStackTrace();
            return columnNameAll;
        }
    }

    public List<String> getTableNameAll()
    {
        List<String> tableList = new ArrayList<>();
        try
        {
            if ( ! databaseOK() )
            {
                Log.d(TAG, "getTableNameAll: Database is closed");
                return tableList;
            }
            Cursor cursor = mDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            //if (cursor.moveToFirst()) {
            //    while ( !cursor.isAfterLast() ) {
            //        tableList.add(cursor.getString(0));
            //        cursor.moveToNext();
            //    }
            //}
            while ( cursor.moveToNext() )
            {
                tableList.add(cursor.getString(0));
            }
            tableList.remove("android_metadata");
            tableList.remove("sqlite_sequence");
            String t = "";
            for ( String s : tableList )
            {
                t += s+", ";
            }
            Log.d(TAG, "getTableNameAll: "+t);
            return tableList;
        }
        catch ( Exception e )
        {
            Log.d(TAG, "getTableNameAll: "+e.getMessage());
            e.printStackTrace();
            return tableList;
        }
    }

    private long getID( String table, long row)
    {
        try
        {
            if ( row > getRowCount(table) )
                return 0;

            Long id = (long) 0;
            Cursor cursor = mDB.query(table, null, null, null, null, null,
                    ID, String.valueOf(row));

            if ( cursor.moveToLast() )
                id = cursor.getLong(0);

            cursor.close();
            return id;
        }
        catch ( Exception e )
        {
            Log.d(TAG, "getID: FAIL: "+e.getMessage());
            e.printStackTrace();
        }
        return FAIL;
    }

    private boolean databaseOK()
    {
        if ( mDB == null || !mDB.isOpen() )
        {
            Log.d(TAG, "databaseOK: Database is closed");
            return false;
        }
        if ( mLock )
        {
            Log.d(TAG, "databaseOK: Database is locked");
            return false;
        }
        return true;
    }
}

//    private static class Helper extends SQLiteOpenHelper
//    {
//        private final static String TAG = "Database Test";
//        private static SQLiteDatabase db;
//        private final static String databaseName = "sql.db";
//        private final static int version = 1;
//
//        Helper ( Context context, String name, SQLiteDatabase.CursorFactory factory, int version )
//        {
//            super(context, name, factory, version);
//        }
//
//        public static SQLiteDatabase getDatabase(Context context )
//        {
//            Log.d(TAG, "getDatabase: ");
//            if ( db == null || !db.isOpen() )
//            {
//                Log.d(TAG, "getDatabase: IN");
//                Helper helper = new Helper(context, databaseName, null, version);
//                db = helper.getWritableDatabase();
//                Log.d(TAG, "getDatabase: db.getPath() = "+db.getPath());
//            }
//            return db;
//        }
//
//        @Override
//        public void onCreate ( SQLiteDatabase sqLiteDatabase )
//        {
//            Log.d(TAG, "onCreate: db null ? "+(sqLiteDatabase == null));
//            if ( sqLiteDatabase == null )
//                return;
//            sqLiteDatabase.execSQL(Table.CREATE_TABLE);
//        }
//
//        @Override
//        public void onUpgrade ( SQLiteDatabase sqLiteDatabase, int i, int i1 )
//        {
//            Log.d(TAG, "onUpgrade: ");
//            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.TABLE_NAME);
//            onCreate(sqLiteDatabase);
//        }
//    }