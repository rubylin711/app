package com.prime.homeplus.settings.sms;

import java.math.BigInteger;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

public class Utils {
    private static String TAG = "Utils";

    public static String calculateMD5(String input) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();

            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
