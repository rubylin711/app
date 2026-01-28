package com.mtest.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

public class PesiSharedPreference {
    private static final String TAG = "PesiSharedPreference";

    public static final String NAME_HARDWARE_CONFIG = "HwMtestConfig";

    //private Context mContext;
    private SharedPreferences mSharedPreference;
    private SharedPreferences.Editor mEditor;

    // get/create default preference if no name parameter
    public PesiSharedPreference(Context context) {
        //mContext = context;
        mSharedPreference =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        mEditor = mSharedPreference.edit(); // commit in save()
    }

    // get/create preference by name
    public PesiSharedPreference(Context context, String preferenceName) {
        //mContext = context;
        mSharedPreference =
                context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        mEditor = mSharedPreference.edit(); // commit in save()
    }

    public boolean save() {
        Log.d(TAG, "save: ");
        boolean success = mEditor.commit();

        if (success) {
            try {
                // Johnny 20190524 call adb shell sync to prevent SharedPreferences not written if unplug power
                Process process = Runtime.getRuntime().exec("/system/bin/sync");
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                success = false;
            }
        }

        return success;
    }

    public int getInt(String key, int defaultValue) {
        Log.d(TAG, "getInt: key = " + key);
        return mSharedPreference.getInt(key, defaultValue);
    }

    public String getString(String key, String defaultValue) {
        Log.d(TAG, "getString: key = " + key);
        return mSharedPreference.getString(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Log.d(TAG, "getBoolean: key = " + key);
        return mSharedPreference.getBoolean(key, defaultValue);
    }

    public void putInt(String key, int value) {
        Log.d(TAG, "putInt: key = " + key + " value = " + value);
        mEditor.putInt(key, value);
    }

    public void putString(String key, String value) {
        Log.d(TAG, "putString: key = " + key + " value = " + value);
        mEditor.putString(key, value);
    }

    public void putBoolean(String key, boolean value) {
        Log.d(TAG, "putBoolean: key = " + key + " value = " + value);
        mEditor.putBoolean(key, value);
    }

    public void remove(String key) {
        Log.d(TAG, "remove: key = " + key);
        mEditor.remove(key);
    }

    public void clear() {
        Log.d(TAG, "clear: ");
        mEditor.clear();
    }
}
