package com.prime.homeplus.tv.utils;

public class FileUtils {
    public static String formatBytes(long bytes) {
        if (bytes <= 0) {
            return "-";
        }

        long kiloBytes = bytes / 1024;
        long megaBytes = kiloBytes / 1024;

        if (megaBytes >= 1) {
            return megaBytes + " MB";
        } else {
            return kiloBytes + " KB";
        }
    }
}

