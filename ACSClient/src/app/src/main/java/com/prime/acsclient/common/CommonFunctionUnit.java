package com.prime.acsclient.common;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;

import com.prime.acsclient.ACSService;
import com.prime.acsclient.prodiver.ACSDataContentProvider;
import com.prime.acsclient.prodiver.ACSDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Source;

public class CommonFunctionUnit {
    private static String TAG = "ACS_CommonFunctionUnit" ;
    private static final boolean DBG_LOG = false ;

    public static boolean is_platform_rcu_name(String rcu_name) {
        if (rcu_name != null) {
            return rcu_name.equalsIgnoreCase("TATV Remote")
                    || rcu_name.equalsIgnoreCase("RemoteG10")
                    || rcu_name.equalsIgnoreCase("RemoteG20");
        }
        return false;
    }

    public static void send_broadcast(Context context, String package_name, Intent intent) {
        if ( package_name != null )
            intent.setPackage(package_name);
        context.sendBroadcastAsUser(intent, Process.myUserHandle());
    }

    public static void print_all_acs_provider_data(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;

        try {
            cursor = contentResolver.query(ACSDataContentProvider.ACS_PROVIDER_CONTENT_URI, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex1 = cursor.getColumnIndex(ACSDatabase.ACS_DATA_COL_ID);
                int columnIndex2 = cursor.getColumnIndex(ACSDatabase.ACS_DATA_COL_NAME);
                int columnIndex3 = cursor.getColumnIndex(ACSDatabase.ACS_DATA_COL_VALUE);

                do {
                    int data1 = cursor.getInt(columnIndex1);
                    String data2 = cursor.getString(columnIndex2);
                    String data3 = cursor.getString(columnIndex3);

                    Log.d(TAG,
                            ACSDatabase.ACS_DATA_COL_ID + ": " + data1 + ", " +
                                    ACSDatabase.ACS_DATA_COL_NAME + ": " + data2 + ", " +
                                    ACSDatabase.ACS_DATA_COL_VALUE + ": " + data3 );
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

    private static boolean set_acs_provider_data(Context context, String key_name, String json_value) {
        boolean set_data_success = false ;
        String[] keyArr = {key_name};
        Cursor query = context.getContentResolver().query(ACSDataContentProvider.ACS_PROVIDER_CONTENT_URI,
                new String[]{ACSDatabase.ACS_DATA_COL_NAME, ACSDatabase.ACS_DATA_COL_VALUE}, ACSDatabase.ACS_DATA_COL_NAME + "=?", keyArr, null);
        if (query != null) {
            if (query.getCount() > 0) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ACSDatabase.ACS_DATA_COL_VALUE, json_value);
                set_data_success = context.getContentResolver().update(ACSDataContentProvider.ACS_PROVIDER_CONTENT_URI, contentValues, ACSDatabase.ACS_DATA_COL_NAME + "=?", keyArr) > 0;
            } else {
                ContentValues contentValues2 = new ContentValues();
                contentValues2.put(ACSDatabase.ACS_DATA_COL_NAME, key_name);
                contentValues2.put(ACSDatabase.ACS_DATA_COL_VALUE, json_value);
                set_data_success = context.getContentResolver().insert(ACSDataContentProvider.ACS_PROVIDER_CONTENT_URI, contentValues2) != null;
            }
            query.close();
            return set_data_success;
        }
        return set_data_success;
    }

    public static String get_acs_provider_data(Context context, String key_name) {
        String query_value = null ;
        Cursor query = context.getContentResolver().query(ACSDataContentProvider.ACS_PROVIDER_CONTENT_URI,
                new String[]{ACSDatabase.ACS_DATA_COL_NAME, ACSDatabase.ACS_DATA_COL_VALUE}, ACSDatabase.ACS_DATA_COL_NAME + "=?", new String[]{key_name}, null);
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

    public static void del_acs_provider_data(Context context, String key_name) {
        context.getContentResolver().delete(ACSDataContentProvider.ACS_PROVIDER_CONTENT_URI, ACSDatabase.ACS_DATA_COL_NAME + "=?", new String[]{key_name});
    }
    public static void update_data_without_notify_launcher( String data_key_name, JSONArray receive_data_value, FunctionExecutor functionExecutor )
    {
        update_data_without_notify_launcher( data_key_name, receive_data_value!=null?receive_data_value.toString():null, functionExecutor ) ;
    }
    public static void update_data_without_notify_launcher( String data_key_name, String receive_data_value, FunctionExecutor functionExecutor )
    {
        if ( receive_data_value != null ) {
            String provider_data_value = CommonFunctionUnit.get_acs_provider_data(ACSService.g_context, data_key_name);
            if ( DBG_LOG ) {
                Log.d( TAG, "dbg " + CommonFunctionUnit.getMethodName() + " data_key_name : " + data_key_name ) ;
                Log.d( TAG, "dbg " + CommonFunctionUnit.getMethodName() + " receive_data_value : " + receive_data_value ) ;
                Log.d( TAG, "dbg " + CommonFunctionUnit.getMethodName() + " provider_data_value : " + provider_data_value ) ;
            }
            if ( provider_data_value == null || ( ! receive_data_value.contentEquals(provider_data_value) )  ) {
                CommonFunctionUnit.set_acs_provider_data(ACSService.g_context, data_key_name, receive_data_value);
                if ( functionExecutor != null )
                    functionExecutor.execute();
            }
        }
        else
        {
            CommonFunctionUnit.del_acs_provider_data(ACSService.g_context, data_key_name);
        }
    }
    public static void update_data_without_notify_launcher( String data_key_name, int receive_data_value, FunctionExecutor functionExecutor )
    {
        String provider_data_value = CommonFunctionUnit.get_acs_provider_data(ACSService.g_context, data_key_name);
        int provider_data_value_int =
                ( provider_data_value == null ? CommonDefine.DEFAULT_INT_VALUE : Integer.parseInt(provider_data_value) );
        if ( receive_data_value != provider_data_value_int )
        {
            CommonFunctionUnit.set_acs_provider_data(ACSService.g_context, data_key_name, String.valueOf(receive_data_value));
            if ( functionExecutor != null )
                functionExecutor.execute();
        }
    }
    public static void update_data_without_notify_launcher( String data_key_name, JSONArray receive_data_value )
    {
        update_data_without_notify_launcher( data_key_name, receive_data_value!=null?receive_data_value.toString():null, null ) ;
    }
    public static void update_data_without_notify_launcher( String data_key_name, String receive_data_value )
    {
        update_data_without_notify_launcher( data_key_name, receive_data_value, null ) ;
    }
    public static void update_data_without_notify_launcher( String data_key_name, int receive_data_value)
    {
        update_data_without_notify_launcher( data_key_name, receive_data_value, null ) ;
    }
    public static void update_data_without_notify_launcher( String data_key_name, boolean receive_data_value)
    {
        update_data_without_notify_launcher( data_key_name, String.valueOf(receive_data_value), null ) ;
    }

    public static void update_data_and_notify_launcher( String data_key_name, JSONArray receive_data_value, boolean force_notify )
    {
        update_data_and_notify_launcher( data_key_name, receive_data_value!=null?receive_data_value.toString():null, force_notify ) ;
    }
    public static void update_data_and_notify_launcher( String data_key_name, String receive_data_value, boolean force_notify )
    {
        if ( receive_data_value != null ) {
            boolean data_diff = false;
            String provider_data_value = CommonFunctionUnit.get_acs_provider_data(ACSService.g_context, data_key_name);
            if ( DBG_LOG ) {
                Log.d( TAG, "dbg " + CommonFunctionUnit.getMethodName() + " data_key_name : " + data_key_name ) ;
                Log.d( TAG, "dbg " + CommonFunctionUnit.getMethodName() + " receive_data_value : " + receive_data_value ) ;
                Log.d( TAG, "dbg " + CommonFunctionUnit.getMethodName() + " provider_data_value : " + provider_data_value ) ;
            }
            if ( provider_data_value == null || ( ! receive_data_value.contentEquals(provider_data_value) ) ) {
                CommonFunctionUnit.set_acs_provider_data(ACSService.g_context, data_key_name, receive_data_value);
                data_diff = true;
            }

            if (data_diff || force_notify)
                CommonFunctionUnit.send_broadcast(ACSService.g_context, BroadcastDefine.LAUNCHER_PACKAGE_NAME, new Intent(BroadcastDefine.ACS_DATA_UPDATE_ACTION_BASE + data_key_name));
        }
    }

    public static void update_data_and_notify_launcher( String data_key_name, int receive_data_value, boolean force_notify )
    {
        boolean data_diff = false ;
        String provider_data_value = CommonFunctionUnit.get_acs_provider_data(ACSService.g_context, data_key_name);
        int provider_data_value_int =
                ( provider_data_value == null ? CommonDefine.DEFAULT_INT_VALUE : Integer.parseInt(provider_data_value) );
        if ( receive_data_value != provider_data_value_int )
        {
            CommonFunctionUnit.set_acs_provider_data(ACSService.g_context, data_key_name, String.valueOf(receive_data_value));
            data_diff = true ;
        }

        if ( data_diff || force_notify )
            CommonFunctionUnit.send_broadcast(ACSService.g_context, BroadcastDefine.LAUNCHER_PACKAGE_NAME, new Intent(BroadcastDefine.ACS_DATA_UPDATE_ACTION_BASE + data_key_name));
    }

    public static String list_app_packages(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        String system_app_list = null;
        try {
            for (ApplicationInfo applicationInfo : installedApplications) {
                if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    PackageInfo packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, 0);
                    system_app_list = (system_app_list==null?"":system_app_list) + "  "
                            + packageManager.getApplicationLabel(applicationInfo) + ":" + applicationInfo.packageName
                            + " (versionName=" + packageInfo.versionName + ", versionCode=" + packageInfo.versionCode + ")\n";
                }
            }
            String system_app_final = (system_app_list != null)?("  --- System App ---\n" + system_app_list + "\n"):("  --- System App ---\n\n\n");

            String updated_system_app_list = null;
            for (ApplicationInfo applicationInfo : installedApplications) {
                if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    PackageInfo packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, 0);
                    updated_system_app_list = (updated_system_app_list==null?"":updated_system_app_list) + "  "
                            + packageManager.getApplicationLabel(applicationInfo) + ":" + applicationInfo.packageName
                            + " (versionName=" + packageInfo.versionName + ", versionCode=" + packageInfo.versionCode + ")\n";
                }
            }
            String updated_system_app_final = (updated_system_app_list != null)?("  --- Updated System App ---\n" + updated_system_app_list + "\n"):("  --- Updated System App ---\n\n\n");

            String user_installed_app_list = null;
            for (ApplicationInfo applicationInfo : installedApplications) {
                boolean is_system_app = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                boolean is_updated_system_app = (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
                if (!is_system_app && !is_updated_system_app) {
                    PackageInfo packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, 0);
                    user_installed_app_list = (user_installed_app_list==null?"":user_installed_app_list) + "  "
                            + packageManager.getApplicationLabel(applicationInfo) + ":" + applicationInfo.packageName
                            + " (versionName=" + packageInfo.versionName + ", versionCode=" + packageInfo.versionCode + ")\n";
                }
            }
            String user_installed_app_final = (user_installed_app_list != null)?("  --- Installed App ---\n" + user_installed_app_list + "\n"):("  --- Installed App ---\n\n\n");
//            Log.d( TAG, "system_app_final:" + system_app_final);
//            Log.d( TAG, "updated_system_app_final:" + updated_system_app_final);
//            Log.d( TAG, "user_installed_app_final:" + user_installed_app_final);
            return system_app_final + updated_system_app_final + user_installed_app_final;
        } catch (Exception e) {
            Log.i(TAG, CommonFunctionUnit.getMethodName() + " exception: " + e.toString());
            return null;
        }
    }

    public static String get_json_string(JSONObject jsonObject, String key, String default_val)
    {
        try {
            if ( jsonObject.has(key) )
                return jsonObject.getString(key) ;
            else if ( DBG_LOG )
                Log.d( TAG, getCallerName() + " " + getMethodName() + " not found key:" + key) ;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return default_val ;
    }

    public static int get_json_int(JSONObject jsonObject, String key, int default_val)
    {
        try {
            if ( jsonObject.has(key) )
                return jsonObject.getInt(key) ;
            else if ( DBG_LOG )
                Log.d( TAG, getCallerName() + " " + getMethodName() + " not found key:" + key) ;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return default_val ;
    }

    public static boolean get_json_boolean(JSONObject jsonObject, String key, boolean default_val)
    {
        try {
            if ( jsonObject.has(key) )
                return jsonObject.getBoolean(key) ;
            else if ( DBG_LOG )
                Log.d( TAG, getCallerName() + " " + getMethodName() + " not found key:" + key) ;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return default_val ;
    }

    public static JSONObject get_json_jsonobj(JSONObject jsonObject, String key, JSONObject default_val)
    {
        try {
            if ( jsonObject.has(key) )
                return jsonObject.getJSONObject(key) ;
            else if ( DBG_LOG )
                Log.d( TAG, getCallerName() + " " + getMethodName() + " not found key:" + key) ;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return default_val ;
    }

    public static JSONArray get_json_jsonarray(JSONObject jsonObject, String key, JSONArray default_val)
    {
        try {
            if ( jsonObject.has(key) && jsonObject.getJSONArray(key).length() > 0 )
                return jsonObject.getJSONArray(key) ;
            else if ( DBG_LOG )
                Log.d( TAG, getCallerName() + " " + getMethodName() + " not found key:" + key) ;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return default_val ;
    }

    public static JSONArray removeJsonArrayContents(JSONArray source_data, JSONArray remove_data) {
        Set<String> removeSet = new HashSet<>();

        for (int i = 0; remove_data != null && i < remove_data.length(); i++) {
            try {
                removeSet.add(remove_data.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONArray resultArray = new JSONArray();
        for (int i = 0; source_data != null && i < source_data.length(); i++) {
            try {
                String item = source_data.getString(i);
                if (!removeSet.contains(item)) {
                    resultArray.put(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return resultArray;
    }

    public static JSONArray mergeJsonArrays(JSONArray arrayA, JSONArray arrayB) {
        Set<String> resultSet = new HashSet<>();

        for (int i = 0; arrayA != null && i < arrayA.length(); i++) {
            try {
                resultSet.add(arrayA.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; arrayB != null && i < arrayB.length(); i++) {
            try {
                resultSet.add(arrayB.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONArray resultArray = new JSONArray();
        for (String item : resultSet) {
            resultArray.put(item);
        }

        return resultArray;
    }

    public static String[] jsonArray_To_StringArray(JSONArray jsonArray) {
        if ( jsonArray == null )
            return new String[0] ;

        String[] stringArray = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                stringArray[i] = jsonArray.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return stringArray;
    }

    public static ArrayList<String> json_to_arraylist(String jsonString) throws JSONException {
        ArrayList<String> list = new ArrayList<>();
        if ( jsonString == null )
            return list ;

        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }

        return list;
    }

    public static String arraylist_to_json(ArrayList<String> list) {
        JSONArray jsonArray = new JSONArray();
        for (String item : list) {
            jsonArray.put(item);
        }

        return jsonArray.toString();
    }

    public static String combinServerUrl( String token ) {
        String prefix_url = CommonDefine.ACS_HTTP_POST_PRODUCTION_PREFIX_URL ;
        String server_type = SystemProperties.get(CommonDefine.ACS_SERVER_TYPE_PROPERTY) ;
		Log.d(TAG,"server_type = "+server_type);
        if ( server_type != null && server_type.equalsIgnoreCase(CommonDefine.ACS_SERVER_TYPE_Lab) )
        {
            prefix_url = CommonDefine.ACS_HTTP_POST_LAB_TEST_PREFIX_URL ;
        }

        return prefix_url + token ;
    }
    public static void printClassFields(Object obj) {
        if (obj == null) {
            return;
        }

        Class<?> clazz;
        Object instance = null;

        if (obj instanceof Class<?>) {
            clazz = (Class<?>) obj;
        } else {
            clazz = obj.getClass();
            instance = obj;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String name = field.getName();
                Object value = field.get(instance);
                Log.d(TAG, clazz.getSimpleName() + " " + name + " = " + value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void printClassFields(List<?> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        for (Object obj : list) {
            printClassFields(obj);
        }
    }

    public static String getMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // 2 = getMethodName self, 3 = caller, 4 = caller's caller
        // if direct use stackTrace in your method, index must minus 1
        return stackTrace[3].getMethodName();
    }

    public static String getCallerName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // 2 = getCallerName self, 3 = caller, 4 = caller's caller
        // if direct use stackTrace in your method, index must minus 1
        return stackTrace[4].getMethodName();
    }
}
