package com.prime.dtvplayer.Database;

import android.database.Cursor;

public class DBUtils {

    public int GetIntFromTable(Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        int value = cursor.getInt(columnIndex);
        return value;
    }

    public long GetLongFromTable(Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        long value = cursor.getLong(columnIndex);
        return value;
    }

    public String GetStringFromTable(Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        String value = cursor.getString(columnIndex);
        return value;
    }

    public float GetFloatFromTable(Cursor cursor, String ColumnName){
        int columnIndex = cursor.getColumnIndex(ColumnName);
        float value = cursor.getFloat(columnIndex);
        return value;
    }
}