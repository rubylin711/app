package com.prime.homeplus.tv.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.tvprovider.media.tv.Channel;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.TpInfo;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ReportExporter {
    private static final String TAG = "ReportExporter";

    public static final Set<String> AERM_REPORT_CODES = new HashSet<>(Arrays.asList(
            "E002", "E015", "E202", "SignalCheck"
    ));

    public static String getQrErrorReport(Context context, String errorCode, Channel ch) {
        String url = Pvcfg.get_error_code_report_url();
        String qrErrorReport = ReportExporter.exportQrErrorReport(context, errorCode,
                ch);
        return url+qrErrorReport;
    }

    public static String exportQrErrorReport(Context applicationContext, String errorCode, Channel ch) {
        long tsid = -1, sid = -1;
        int autoStandby = 1;
        String chFreq = "0", chStrength  = "0", chSnr = "0", chBer = "0",
                areaLimitation = "0", hdcpMode = "0";
        String model = Build.MODEL;
        String swVersion = Build.ID + " " + Build.VERSION.INCREMENTAL;
        String stbSn = Pvcfg.get_device_sn();
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        String cardSn = Pvcfg.get_ca_sn(); // smart card sn
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        String soid = PrimeUtils.getSoId(applicationContext);; //default value
        String currentTime = "" + System.currentTimeMillis() / 1000;
        String csv = "", aes = "", report = "";
        GposInfo gposInfo = PrimeUtils.get_gpos_info();

        try {
            stbSn = Build.getSerial();
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.toString());
        }

        if (ch != null) {
            tsid = ch.getTransportStreamId();
            sid = ch.getServiceId();
        }

        ProgramInfo programInfo = PrimeUtils.getProgramInfo(ch);
        if(programInfo != null) {
            TpInfo tpInfo = PrimeUtils.getTpInfo(programInfo.getTpId());
            chFreq = tpInfo.CableTp.getFreq()*1000+"";
            chStrength = PrimeUtils.tuner_get_strength(programInfo.getTunerId())+"";
            chSnr = PrimeUtils.tuner_get_snr(programInfo.getTunerId())+"";
            chBer = PrimeUtils.tuner_get_ber(programInfo.getTunerId())+"";
        }
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // chFreq, chBER, chSNR, chStrength, areaLimitation, hdcpMode
        areaLimitation = Pvcfg.get_area_limitation();
        hdcpMode = gposInfo != null ? GposInfo.getHDCPOnOff(applicationContext)+"" : "1";

        try {
            int autoStandbyValue = GposInfo.getSleepTimeout(applicationContext);//Settings.Secure.getInt(applicationContext.getContentResolver(), "sleep_timeout");
            if (autoStandbyValue == -1) {
                autoStandby = 0;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.toString());
        }


        csv = model + "," + swVersion  + "," +  cardSn  + "," + stbSn  + "," + chFreq +
                "," + chStrength  + "," + chSnr + "," + chBer  + "," + errorCode  + "," + tsid +
                "," + sid  + "," + soid  + "," + areaLimitation  + "," + hdcpMode  + "," + autoStandby +
                "," + currentTime;
        Log.d(TAG, "exportQrErrorReport csv ==== "+ csv);

        aes = aesEncrypt("HomeplusQRReport", csv);
        Log.d(TAG, "exportQrErrorReport aes ==== "+ aes);

        report = aes.replace('+', '-').replace('/', '_');
        Log.d(TAG, "exportQrErrorReport report = " + report);

        return report;
    }

    public static String exportAermReport(String aermErrorCode, Channel ch) {
        long sid = -1;
        String chFreqMhz = "0", chStrength  = "0", chSnr = "0", chBer = "0";
        String cardSn = Pvcfg.get_ca_sn(); // TODO: smart card sn
        String stbSn = Pvcfg.get_device_sn();
        String report = "";
        ProgramInfo programInfo = PrimeUtils.getProgramInfo(ch);
        if(programInfo != null) {
            TpInfo tpInfo = PrimeUtils.getTpInfo(programInfo.getTpId());
            chFreqMhz = tpInfo.CableTp.getFreq()+"";
            chStrength = PrimeUtils.tuner_get_strength(programInfo.getTunerId())+"";
            chSnr = PrimeUtils.tuner_get_snr(programInfo.getTunerId())+"";
            chBer = PrimeUtils.tuner_get_ber(programInfo.getTunerId())+"";
        }
        try {
            stbSn = Build.getSerial();
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.toString());
        }

        if (ch != null) {
            sid = ch.getServiceId();
        }

        try {
            JSONObject reportJson = new JSONObject();
            reportJson.put("sc", cardSn);
            reportJson.put("sn", stbSn);
            reportJson.put("sid", sid);
            reportJson.put("error", aermErrorCode);
            reportJson.put("freq1", parseIntSafe(chFreqMhz));
            reportJson.put("lv1", parseIntSafe(chStrength));
            reportJson.put("snr1", parseIntSafe(chSnr));
            reportJson.put("ber1", chBer);

            report = reportJson.toString();
            Log.d(TAG, "exportAermReport report = " + report);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create AERM report JSON, Error: " + e.toString());
        }

        return report;
    }

    private static String aesEncrypt(String rootKey, String s) {
        String CIPER_KEY = "AES";
        String CIPER_METHOD = "AES/ECB/PKCS5Padding";
        String en_s = "";
        try {
            SecretKeySpec secretkeyspec = new SecretKeySpec(rootKey.getBytes(), CIPER_KEY);
            Cipher cipher = Cipher.getInstance(CIPER_METHOD);
            cipher.init(Cipher.ENCRYPT_MODE, secretkeyspec);
            byte[] encrypted = cipher.doFinal(s.getBytes("utf-8"));
            Base64.Encoder encoder = Base64.getEncoder();
            en_s = new String(encoder.encode(encrypted));
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.toString());
        }
        return en_s;
    }

    private static int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
