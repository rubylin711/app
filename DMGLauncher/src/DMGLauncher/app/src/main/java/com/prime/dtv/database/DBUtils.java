package com.prime.dtv.database;

import android.database.Cursor;

import com.prime.dtv.service.database.DVBContentProvider;
import com.prime.dtv.utils.LogUtils;

public class DBUtils {

    public int GetIntFromTable(Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        if(columnIndex == -1)
            return -1;
        int value = cursor.getInt(columnIndex);
        return value;
    }

    public int GetIntFromTable(String tableName, Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        if(columnIndex == -1) {
            LogUtils.e("Cant find "+ColumnName+", need add it !!!!!!!!!!");
            DVBContentProvider.getDvbDb().insert_new_colume(tableName, ColumnName, "INTEGER");
            return -1;
        }
        int value = cursor.getInt(columnIndex);
        return value;
    }

    public long GetLongFromTable(Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        if(columnIndex == -1)
            return 0;
        long value = cursor.getLong(columnIndex);
        return value;
    }

    public long GetLongFromTable(String tableName, Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        if(columnIndex == -1) {
            LogUtils.e("Cant find "+ColumnName+", need add it !!!!!!!!!!");
            DVBContentProvider.getDvbDb().insert_new_colume(tableName, ColumnName, "INTEGER");
            return 0;
        }
        long value = cursor.getLong(columnIndex);
        return value;
    }

    public String GetStringFromTable(Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        LogUtils.d("columnIndex = "+columnIndex);
        if(columnIndex == -1)
            return null;
        String value = cursor.getString(columnIndex);
        return value;
    }

    public String GetStringFromTable(String tableName, Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        LogUtils.d("columnIndex = "+columnIndex);
        if(columnIndex == -1) {
            LogUtils.e("Cant find "+ColumnName+", need add it !!!!!!!!!!");
            DVBContentProvider.getDvbDb().insert_new_colume(tableName, ColumnName, "TEXT");
            return "";
        }
        String value = cursor.getString(columnIndex);
        return value;
    }

    public float GetFloatFromTable(String tableName, Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        if(columnIndex == -1) {
            DVBContentProvider.getDvbDb().insert_new_colume(tableName, ColumnName, "REAL");
            return 0;
        }
        float value = cursor.getFloat(columnIndex);
        return value;
    }

    public float GetFloatFromTable(Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        if(columnIndex == -1)
            return 0;
        float value = cursor.getFloat(columnIndex);
        return value;
    }

    public byte[] GetByteArrayFromTable(String tableName,Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        if(columnIndex == -1) {
            DVBContentProvider.getDvbDb().insert_new_colume(tableName, ColumnName, "BLOB");
            return null;
        }
        byte[] value = cursor.getBlob(columnIndex);
        return value;
    }

    public byte[] GetByteArrayFromTable(Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        if(columnIndex == -1)
            return null;
        byte[] value = cursor.getBlob(columnIndex);
        return value;
    }
}