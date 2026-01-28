package com.prime.datastructure.utils;

import android.util.Log;

public class ThreadUtils extends Thread {

    public static String TAG = "ThreadUtils";

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        }
        catch (Exception e) {
            Log.i(TAG, "sleep: [exception] " + e);
        }
    }
}
