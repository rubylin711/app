package com.prime.primetvinputapp.Utils;

import android.util.Log;


public class Utils {
    private static final String TAG = "Utils";

    public static String number_code_to_string(int keyCode) {
        Log.d(TAG, "number_code_to_string: keyCode = " + keyCode);
        return String.valueOf(keyCode - 7);
    }
}
