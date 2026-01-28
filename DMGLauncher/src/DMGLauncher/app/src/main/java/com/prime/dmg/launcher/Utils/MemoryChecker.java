package com.prime.dmg.launcher.Utils;

import android.util.Log;

import java.text.DecimalFormat;

public class MemoryChecker {

    private static final String TAG = MemoryChecker.class.getSimpleName();
    private static final long MEGABYTE = 1024L * 1024L;

    public static void print_memory_usage() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();  // JVM 可用的總記憶體
        long freeMemory = runtime.freeMemory();    // JVM 當前可用的空閒記憶體
        long usedMemory = totalMemory - freeMemory; // JVM 當前已使用的記憶體
        long maxMemory = runtime.maxMemory();      // JVM 可分配的最大記憶體

        DecimalFormat formatter = new DecimalFormat("#,###"); // 格式化輸出，增加可讀性

        Log.i(TAG, "--- Current JVM Memory Usage ---");
        Log.i(TAG, "Total Memory (allocated to JVM): " + formatter.format(totalMemory) + " bytes (" + formatter.format(totalMemory / MEGABYTE) + " MB)");
        Log.i(TAG, "Free Memory (available in JVM):  " + formatter.format(freeMemory) + " bytes (" + formatter.format(freeMemory / MEGABYTE) + " MB)");
        Log.i(TAG, "Used Memory (by application):    " + formatter.format(usedMemory) + " bytes (" + formatter.format(usedMemory / MEGABYTE) + " MB)");
        Log.i(TAG, "Max Memory (JVM can use):        " + formatter.format(maxMemory) + " bytes (" + formatter.format(maxMemory / MEGABYTE) + " MB)");
        Log.i(TAG, "--------------------------------");
    }
}
