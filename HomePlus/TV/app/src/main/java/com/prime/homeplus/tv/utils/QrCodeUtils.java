package com.prime.homeplus.tv.utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;

public class QrCodeUtils {
    private static final String TAG = "QrCodeUtils";

    public static Bitmap generateQRCode(String content, int size) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.QR_VERSION, "9");
            hints.put(EncodeHintType.MARGIN, "0");

            return encoder.encodeBitmap(content, BarcodeFormat.QR_CODE, size, size, hints);
        } catch (Exception e) {
            Log.d(TAG, "Error:" + e.toString());
            return null;
        }
    }
}
