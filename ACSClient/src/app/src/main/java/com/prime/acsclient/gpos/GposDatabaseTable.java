package com.prime.acsclient.gpos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class GposDatabaseTable {
    private static final String TAG = "ACS_GposDatabaseTable";
    public static final String KEY = "KEY";
    public static final String VALUE = "VALUE";
    public static final Uri CONTENT_URI = Uri.parse("content://com.prime.dtv.service.database.DVBContentProvider/Gpos");
    public String getKeyValue(Context context, String key) {
        String value = null;
        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(GposDatabaseTable.CONTENT_URI, new String[]{GposDatabaseTable.KEY, GposDatabaseTable.VALUE}, GposDatabaseTable.KEY + " = ?", new String[]{key}, null);
//            Log.d(TAG, "getValueByColumn cursor = " + cursor);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
//                    Log.d(TAG, "cursor moveToFirst");
                    value = new DBUtils().GetStringFromTable(cursor, GposDatabaseTable.VALUE);
//                    Log.d(TAG, "cursor value = " + value);
                }
            }
            if(cursor != null)
                cursor.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
