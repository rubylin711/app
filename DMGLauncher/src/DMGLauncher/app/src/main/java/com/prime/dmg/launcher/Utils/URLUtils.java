package com.prime.dmg.launcher.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.prime.dmg.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.dtv.config.Pvcfg;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class URLUtils {

    static final String TAG = "URLUtils";

    private static final String PATH_CHANNEL = "channel";
    private static final String PATH_PREVIEW = "preview";
    private static final String PATH_PROGRAM = "program";
    private static final String PATH_TYPE_OTT = "ott";
    private static final String PATH_TYPE_PACKAGE = "package";
    private static final String PATH_TYPE_SERVICE = "service";

    private static String get_ca_sn() {
        return SystemProperties.get("ro.boot.cstmsnno");
    }

    private static String get_bat_id(Context context) {
        String BAT_ID = ACSDataProviderHelper.get_acs_provider_data(context, "bat_id");
        return BAT_ID == null ? "null" : BAT_ID;
    }

    private static String get_so(Context context) {
        String SO = ACSDataProviderHelper.get_acs_provider_data(context, "so");
        return SO == null ? "null" : SO;
    }

    private static String get_zip_code(Context context) {
        String ZIP_CODE = ACSDataProviderHelper.get_acs_provider_data(context, "zipcode");
        return ZIP_CODE == null ? "null" : ZIP_CODE;
    }

    private static String get_active_product_url(Context context) {
        return "https://acsapi.tbc.net.tw/api/AcsApi/AtvActiveProduct/" + get_bat_id(context) + "/" + get_ca_sn();
    }

    public static String generate_package_url(Context context, String packageId) {
        if (TextUtils.isEmpty(packageId))
            return generate_url(context, PATH_TYPE_PACKAGE, "p_null");
        return generate_url(context, PATH_TYPE_PACKAGE, "p_" + packageId);
    }

    public static String generate_payment_url(Context context, String serviceName) {
        String CA_SN = get_ca_sn();
        String BAT_ID = get_bat_id(context);
        String ZIP_CODE = get_zip_code(context);

        Uri.Builder builder = new Uri.Builder();
        //https://mwps.tbc.net.tw/payment/NA/NA/NA/tb02a-r36-20240927/223677608592/223677608592/330617526677/202/320023/null
        builder.scheme("https")
                .authority("mwps.tbc.net.tw")
                .appendPath(serviceName)
                .appendPath("NA"/*TunerInfoHelper.getInstance().getSnr()*/)
                .appendPath("NA"/*TunerInfoHelper.getInstance().getBer()*/)
                .appendPath("NA"/*TunerInfoHelper.getInstance().getPower()*/)
                .appendPath(Build.VERSION.INCREMENTAL)
                .appendPath(CA_SN)
                .appendPath(CA_SN)
                .appendPath("null"/*SystemProperties.get("ro.vendor.nagra.nuid_label")*/)
                .appendPath(BAT_ID)
                .appendPath(ZIP_CODE)
                .appendPath("null");

        return builder.build().toString();
    }

    public static String generate_poster_url(Context context, int serviceId, String eventName) throws UnsupportedEncodingException {
        String storageServer = ACSDataProviderHelper.get_acs_provider_data(context, "storage_server");
        return storageServer + "/program/ch" + serviceId + "/" + URLEncoder.encode("pt" + eventName + ".png", "UTF-8");
    }

    public static String generate_payment_url(Context context) {
        String CA_SN = get_ca_sn();
        String BAT_ID = get_bat_id(context);
        String SO = get_so(context);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("pay.dmg.tv")
                .appendPath("bill")
                .appendPath(SO)
                .appendPath(CA_SN)
                .appendPath(CA_SN)
                .appendPath(BAT_ID);
        return builder.build().toString();
    }

    private static String generate_url(Context context, String type, String type_id) {
        String CA_SN = get_ca_sn();
        String BAT_ID = get_bat_id(context);
        Uri.Builder builder = new Uri.Builder();

        if (Pvcfg.MODULE_DMG == Pvcfg.getModuleType()) {
            String SO = get_so(context);
            builder.scheme("https")
                    .authority("pay.dmg.tv")
                    .appendPath("product")
                    .appendPath(type)
                    .appendPath(TextUtils.isEmpty(SO) ? "null" : SO)
                    .appendPath(CA_SN)
                    .appendPath(CA_SN)
                    .appendPath(TextUtils.isEmpty(BAT_ID) ? "null" : BAT_ID)
                    .appendPath(type_id);
        }
        else if (Pvcfg.MODULE_TBC == Pvcfg.getModuleType()) {
            builder.scheme("https")
                    .authority("mwps.tbc.net.tw")
                    .appendPath("product")
                    .appendPath(type)
                    .appendPath(CA_SN)
                    .appendPath(CA_SN)
                    .appendPath(TextUtils.isEmpty(BAT_ID) ? "null" : BAT_ID)
                    .appendPath(type_id);
        }
        else Log.e(TAG, "generate_url: incorrect module type");

        return builder.build().toString();
    }

    public static Bitmap generate_qr_code(String contentUrl, int contentSize) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap QR_Code_Bitmap = Bitmap.createBitmap(contentSize, contentSize, Bitmap.Config.RGB_565);

        try {
            // method 1
            if (false) {
                BitMatrix bitMatrix = barcodeEncoder.encode(contentUrl, BarcodeFormat.QR_CODE, contentSize, contentSize);
                for (int x = 0; x < contentSize; x++) {
                    for (int y = 0; y < contentSize; y++) {
                        QR_Code_Bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                    }
                }
            }
            // method 2
            else {
                QR_Code_Bitmap = barcodeEncoder.encodeBitmap(contentUrl, BarcodeFormat.QR_CODE, contentSize, contentSize);
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return QR_Code_Bitmap;
    }

    public static String generate_url(Context context, String uri) {
        String STB_SC_ID = Build.getSerial(); //SystemProperties.get("ro.serialno");
        String STB_CA_SN = SystemProperties.get("ro.boot.cstmsnno");
        String BAT_ID = ACSDataProviderHelper.get_acs_provider_data(context, "bat_id");
        Log.d(TAG, "get_qrcode_url: STB_SC_ID = " + STB_SC_ID);
        Log.d(TAG, "get_qrcode_url: STB_CA_SN = " + STB_CA_SN);
        Log.d(TAG, "get_qrcode_url: BAT_ID = " + BAT_ID);

        if (BAT_ID == null)
            BAT_ID = "0";

        return uri.replace("{STB_SC_ID}", STB_CA_SN)
                .replace("{STB_CA_SN}", STB_CA_SN)
                .replace("{BAT_ID}", BAT_ID);
    }

    public static String generate_ott_url(Context context, String cp_code) {
        String CA_SN = get_ca_sn();
        String BAT_ID = get_bat_id(context);
        String SO = get_so(context);
        Uri.Builder builder = new Uri.Builder();

        if (Pvcfg.MODULE_DMG == Pvcfg.getModuleType()) {
            builder.scheme("https")
                    .authority("pay.dmg.tv")
                    .appendPath("product")
                    .appendPath(PATH_TYPE_OTT)
                    .appendPath(TextUtils.isEmpty(SO) ? "null" : SO)
                    .appendPath(CA_SN)
                    .appendPath(CA_SN)
                    .appendPath(TextUtils.isEmpty(BAT_ID) ? "null" : BAT_ID)
                    .appendPath(TextUtils.isEmpty(cp_code) ? "null" : cp_code);
            return builder.build().toString();
        }
        else {
            builder.scheme("https")
                    .authority("mwps.tbc.net.tw")
                    .appendPath("product")
                    .appendPath(PATH_TYPE_OTT)
                    .appendPath(CA_SN)
                    .appendPath(CA_SN)
                    .appendPath(TextUtils.isEmpty(BAT_ID) ? "null" : BAT_ID)
                    .appendPath(TextUtils.isEmpty(cp_code) ? "null" : cp_code);
            return builder.build().toString();
        }
    }
}
