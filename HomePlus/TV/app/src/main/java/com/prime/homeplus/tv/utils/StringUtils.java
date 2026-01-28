package com.prime.homeplus.tv.utils;

import android.text.TextUtils;

public class StringUtils {
    private static final String TAG = "StringUtils";

    public static String padToNDigits(String numberStr, int digits) {
        if (numberStr == null || numberStr.trim().isEmpty() || digits <= 0) {
            return "";
        }

        try {
            int number = Integer.parseInt(numberStr.trim());
            return String.format("%0" + digits + "d", number);
        } catch (NumberFormatException e) {
            return "";
        }
    }

    public static String padToNDigits(Integer number, int digits) {
        if (number == null || digits <= 0) {
            return "";
        }
        return String.format("%0" + digits + "d", number);
    }

    public static String padToNDigits(int number, int digits) {
        if (digits <= 0) {
            return "";
        }
        return padToNDigits(Integer.valueOf(number), digits);
    }

    public static boolean isOnlyZeros(String input) {
        if (TextUtils.isEmpty(input)) {
            return false;
        }

        return input.matches("^0+$");
    }

    public static String normalizeInputNumber(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }

        return input.replaceFirst("^0+(?!$)", "");
    }

    public static java.util.Locale getJavaLocal(String isoLangCode) {
//        String lang = isoLangCode.equalsIgnoreCase("chs") ? "chi" : isoLangCode;
        return new java.util.Locale(isoLangCode);
    }
}
