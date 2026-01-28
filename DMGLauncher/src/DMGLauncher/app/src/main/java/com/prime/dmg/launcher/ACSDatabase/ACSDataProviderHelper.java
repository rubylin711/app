package com.prime.dmg.launcher.ACSDatabase;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class ACSDataProviderHelper {
    private static final String TAG = "ACSDataProviderHelper" ;
    private static final String AUTHORITY = "com.prime.acsclient.prodiver";
    private static final String URI_PATH = "ACSData";
    public static final Uri ACS_PROVIDER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH);
    public static final String ACS_DATA_COL_ID = "_ID";
    public static final String ACS_DATA_COL_NAME = "name";
    public static final String ACS_DATA_COL_VALUE = "value";
    public static boolean set_acs_provider_data(Context context, String key_name, String json_value) {
        boolean set_data_success = false ;
        String[] keyArr = {key_name};
        Cursor query = context.getContentResolver().query(ACS_PROVIDER_CONTENT_URI,
                new String[]{ACS_DATA_COL_NAME, ACS_DATA_COL_VALUE}, ACS_DATA_COL_NAME + "=?", keyArr, null);
        if (query != null) {
            if (query.getCount() > 0) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ACS_DATA_COL_VALUE, json_value);
                set_data_success = context.getContentResolver().update(ACS_PROVIDER_CONTENT_URI, contentValues, ACS_DATA_COL_NAME + "=?", keyArr) > 0;
            } else {
                ContentValues contentValues2 = new ContentValues();
                contentValues2.put(ACS_DATA_COL_NAME, key_name);
                contentValues2.put(ACS_DATA_COL_VALUE, json_value);
                set_data_success = context.getContentResolver().insert(ACS_PROVIDER_CONTENT_URI, contentValues2) != null;
            }
            query.close();
            return set_data_success;
        }
        return set_data_success;
    }

    public static String get_acs_provider_data(Context context, String key_name) {
        String query_value = null ;
        Cursor query = context.getContentResolver().query(ACS_PROVIDER_CONTENT_URI,
                new String[]{ACS_DATA_COL_NAME, ACS_DATA_COL_VALUE}, ACS_DATA_COL_NAME + "=?", new String[]{key_name}, null);
        if (query != null) {
            int count = query.getCount();
            if (count > 0) {
                query.moveToFirst();
                String query_key_name = query.getString(0);
                query_value = query.getString(1);
            }
            query.close();
        }

        return query_value;
    }
    public static void print_all_acs_provider_data(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;

        try {
            cursor = contentResolver.query(ACS_PROVIDER_CONTENT_URI, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                // 获取列索引
                int columnIndex1 = cursor.getColumnIndex(ACS_DATA_COL_ID);
                int columnIndex2 = cursor.getColumnIndex(ACS_DATA_COL_NAME);
                int columnIndex3 = cursor.getColumnIndex(ACS_DATA_COL_VALUE);

                do {
                    int data1 = cursor.getInt(columnIndex1);
                    String data2 = cursor.getString(columnIndex2);
                    String data3 = cursor.getString(columnIndex3);

                    Log.d(TAG,
                            ACS_DATA_COL_ID + ": " + data1 + ", " +
                                    ACS_DATA_COL_NAME + ": " + data2 + ", " +
                                    ACS_DATA_COL_VALUE + ": " + data3 );
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No data found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int delete_acs_provider_data(Context context, String key_name)
    {
        return context.getContentResolver().delete(ACS_PROVIDER_CONTENT_URI, ACS_DATA_COL_NAME + "=?", new String[]{key_name});
    }
}
