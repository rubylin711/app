package com.prime.appDownloadManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArraySet;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;

import static android.content.Context.MODE_PRIVATE;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.SET_RETURN_TARGET.KEY_NAME_RETURN_TARGET_ACTION;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.SET_RETURN_TARGET.KEY_NAME_RETURN_TARGET_PKGNAME;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.SET_WHITELIST_ONOFF.KEY_NAME_WHITELIST_ONOFF;

public class WhitelistSharedPreferences
{
    private static final String TAG = "WhitelistSharedPreferences";
    private static final boolean DEBUG = false;
    private static final String KEY_NAME_WHITELIST = "WHITELIST";
    private WeakReference<Context> mContRef;
    private SharedPreferences mPref = null;

    public WhitelistSharedPreferences(Context context)
    {
        mContRef = new WeakReference<>(context);
        initPref();
    }

    private void initPref()
    {
        mPref = mContRef.get().getSharedPreferences(KEY_NAME_WHITELIST, MODE_PRIVATE);
        if (mPref.getAll().isEmpty())
        {
            boolean isInitSuccess = mPref.edit()
                    .putBoolean(KEY_NAME_WHITELIST_ONOFF, false)
                    .putStringSet(KEY_NAME_WHITELIST, new ArraySet<String>())
                    .putString(KEY_NAME_RETURN_TARGET_ACTION, "")
                    .putString(KEY_NAME_RETURN_TARGET_PKGNAME, "")
                    .commit();
            if (DEBUG)
                Log.d(TAG, "initPref: is Init Success = " + isInitSuccess);
        }
    }

    public ArrayList<String> getWhitelist()
    {
        return new ArrayList<>(mPref.getStringSet(KEY_NAME_WHITELIST, null));
    }

    public boolean getWhitelistOnOff()
    {
        return mPref.getBoolean(KEY_NAME_WHITELIST_ONOFF, false);
    }

    public String getTargetAction()
    {
        return mPref.getString(KEY_NAME_RETURN_TARGET_ACTION, null);
    }

    public String getTargetPkgName()
    {
        return mPref.getString(KEY_NAME_RETURN_TARGET_PKGNAME, null);
    }

    public boolean setTarget(String targetAction, String targetPkgName)
    {
        return mPref.edit()
                .putString(KEY_NAME_RETURN_TARGET_ACTION, targetAction)
                .putString(KEY_NAME_RETURN_TARGET_PKGNAME, targetPkgName)
                .commit();
    }

    public boolean setWhitelist(ArrayList<String> whitelist)
    {
        return mPref.edit()
                .putStringSet(KEY_NAME_WHITELIST, new HashSet<>(whitelist)).commit();
    }

    public boolean setWhitelistOnOff(boolean whitelistOnOff)
    {
        return mPref.edit()
                .putBoolean(KEY_NAME_WHITELIST_ONOFF, whitelistOnOff).commit();
    }
}
