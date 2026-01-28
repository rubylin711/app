package com.prime.dtv.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;

import com.prime.datastructure.CommuincateInterface.OtaModule;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.DTVMessage;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

public class OtaUtils {
    private static final String TAG = "OtaUtils";

    private static PesiDtvFrameworkInterfaceCallback mCallback;

    private UpdateInfo mUpdateInfo;

    public static class UpdateInfo {
        public static final String NORMAL_UPDATE = "1";
        public static final String FORCE_UPDATE = "2";

        private String mLastVersion;
        private String mDownloadType;
        private String mFileSize;
        private String mMD5;
        private String mUpgradeFileUrl;
        private String mDescribe;

        public String ToString() {
            return "UpgradePolicy{" +
                    "lastVersion='" + mLastVersion +
                    ", downloadType=" + mDownloadType +
                    ", fileSize=" + mFileSize +
                    ", md5='" + mMD5 +
                    ", url='" + mUpgradeFileUrl +
                    ", describe='" + mDescribe +
                    '}';
        }

        public String get_last_version() {
            return mLastVersion;
        }

        public String get_download_type() {
            return mDownloadType;
        }
    }

    public static class Result {
        private String mCase;
        private String mID;
        private String mIP;
        private String mStatus;

        public String ToString() {
            return "Result{" +
                    "Case='" + mCase +
                    ", ID=" + mID +
                    ", IP=" + mIP +
                    ", Status='" + mStatus +
                    '}';
        }
    }

    public OtaUtils( PesiDtvFrameworkInterfaceCallback callback) {
        mCallback = callback;
    }

    public void setUpdateInfo(UpdateInfo updateInfo) {
        if (updateInfo != null)
            updateInfo.mDownloadType = UpdateInfo.NORMAL_UPDATE;
        mUpdateInfo = updateInfo;
    }

    public static String doGet(String urlStr, Context context){
        StringBuilder result = null;
        BufferedReader reader = null;
        HttpURLConnection connection = null;

        if (!NetworkUnit.is_ethernet_enable(context) && !NetworkUnit.is_wifi_enable(context)) {
            Log.e(TAG, "doGet: no network");
            return null;
        }

        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            connection.setRequestProperty("accept", "application/xml");
            connection.setRequestProperty("connection", "close");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8"));

                result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                    //Log.d(TAG, "doGet: tony " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader!=null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null)
                connection.disconnect();
        }

        return result.toString();
    }



    public static String doPost(String urlStr, String paramString, Context context){
        StringBuilder result = null;
        BufferedReader reader = null;
        HttpURLConnection connection = null;

        if (!NetworkUnit.is_ethernet_enable(context) && !NetworkUnit.is_wifi_enable(context)) {
            Log.e(TAG, "doPost: no network");
            return null;
        }

        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            connection.setDoOutput(true);
            connection.setDoInput(true);

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);

            connection.setRequestProperty("Accept", "application/xml");
            connection.setRequestProperty("Content-Type", "application/xml; charset=utf-8");
            connection.setRequestProperty("Connection", "close"); // 或 Keep-Alive，看你的需求

            byte[] payload = paramString.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(payload.length);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload);
                os.flush();
            }

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8"));

                result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                    //Log.d(TAG, "doPost: tony " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader!=null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null)
                connection.disconnect();
        }

        return result.toString();
    }

    public static UpdateInfo parseUpdateInfo(String xml) {
        UpdateInfo updateInfo = new UpdateInfo();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));
            int eventType = parser.getEventType();
            String currentTag = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG: {
                        currentTag = parser.getName();
                        if ("Upgrade".equals(currentTag))
                            updateInfo.mLastVersion = parser.getAttributeValue(null, "LastVersion");
                        else if ("UpgradeSoftware".equals(currentTag)) {
                            updateInfo.mFileSize = parser.getAttributeValue(null, "FileSize");
                            updateInfo.mMD5 = parser.getAttributeValue(null, "MD5");
                            updateInfo.mUpgradeFileUrl = parser.getAttributeValue(null, "UpgradeFileUrl");
                        }
                        break;
                    }

                    case XmlPullParser.TEXT: {
                        String text = parser.getText();
                        if (text != null) text = text.trim();
                        if (text == null || text.isEmpty()) break;

                        if ("DownloadType".equals(currentTag))
                            updateInfo.mDownloadType = text;
                        else if ("Describe".equals(currentTag))
                            updateInfo.mDescribe = text;
                    }
                    case XmlPullParser.END_TAG: {
                        currentTag = null;
                        break;
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "parseUpdateInfo: " + e);
        }

        Log.d(TAG, "parseUpdateInfo: " + updateInfo.ToString());

        return updateInfo;
    }

    public static Result parseResult(String xml) {
        Result result = new Result();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));
            int eventType = parser.getEventType();
            String currentTag = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("FirstBoot".equals(parser.getName())) {
                        result.mCase = "FirstBoot";
                        result.mID = parser.getAttributeValue(null, "id");
                        result.mIP = parser.getAttributeValue(null, "ip");
                        result.mStatus = parser.getAttributeValue(null, "status");
                        break; // 只有一個節點，直接結束
                    }
                    else if ("StartupInfo".equals(parser.getName())) {
                        result.mCase = "StartupInfo";
                        result.mID = parser.getAttributeValue(null, "id");
                        result.mIP = parser.getAttributeValue(null, "ip");
                        result.mStatus = parser.getAttributeValue(null, "status");
                        break; // 只有一個節點，直接結束
                    }
                    else if ("SendInfo".equals(parser.getName())) {
                        result.mCase = "SendInfo";
                        result.mID = parser.getAttributeValue(null, "id");
                        result.mIP = parser.getAttributeValue(null, "ip");
                        result.mStatus = parser.getAttributeValue(null, "status");
                        break; // 只有一個節點，直接結束
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "parseResult: " + e);
        }

        Log.d(TAG, "parseResult: " + result.ToString());
        return result;
    }

    public static String get_update_info_url_string(Context context) {
        boolean isWifi = NetworkUnit.is_wifi_enable(context);

        String ip = isWifi? NetworkUnit.get_wifi_ip(context):NetworkUnit.get_ethernet_ip(context);
        String macTmp = isWifi? NetworkUnit.get_Wifi_mac(): NetworkUnit.get_Ethernet_mac();
        String mac = "";
        if (macTmp != null && !macTmp.isEmpty())
            mac = macTmp.replaceAll("..(?!$)", "$0:");

        String urlString = "https://cnsatv.totaltv.com.tw:18076/getPolicyFile"
                + "?type=upgrade"
                + "&deviceType=HPA-13194_Homeplus_RTD1319D"
                + "&version=" + Pvcfg.get_firmware_version()
                + "&ip=" + ip
                + "&sn=" + Pvcfg.get_device_sn()
                + "&mac=" + mac
                + "&manufacture=Prime"
                + "&hardwareVersion=" + Pvcfg.get_hardware_version();

        //urlString = "https://cnsatv.totaltv.com.tw:18085/getPolicyFile?type=upgrade&deviceType=9642C1_CNS_BCM72604&version=1.16.7&ip=10.180.89.12&sn=015000831900001111&mac=BC:20:BA:E2:DD:40&manufacture=Inspur&hardwareVersion=01.56";
        Log.d(TAG, "get_update_info_url_string: " + urlString);

        return urlString;
    }

    public static String get_upload_first_boot_info_url_string() {
        String urlString = "https://cnsatv.totaltv.com.tw:18085/FirstBoot";
        return urlString;
    }

    public static String get_boot_login_url_string() {
        String urlString = "https://cnsatv.totaltv.com.tw:18085/StartupInfo";
        return urlString;
    }

    public static String  get_upload_update_status_url_string() {
        String urlString = "https://cnsatv.totaltv.com.tw:18085/UpgradeStatus";
        return urlString;
    }

    public static String get_upload_first_boot_info_xml_string(Context context) {
        boolean isWifi = NetworkUnit.is_wifi_enable(context);
        String ip = isWifi? NetworkUnit.get_wifi_ip(context):NetworkUnit.get_ethernet_ip(context);
        String macTmp = isWifi? NetworkUnit.get_Wifi_mac(): NetworkUnit.get_Ethernet_mac();
        String mac = "";
        if (macTmp != null && !macTmp.isEmpty())
            mac = macTmp.replaceAll("..(?!$)", "$0:");

        String xmlString ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<FirstBoot"
                + " id=\"" + safeXmlAttr(Pvcfg.get_device_sn()) + "\""
                + " ip1=\"" + safeXmlAttr(ip) + "\""
                + " ip2=\"" + safeXmlAttr("") + "\""
                + " mac1=\"" + safeXmlAttr(mac) + "\""
                + " mac2=\"" + safeXmlAttr("") + "\""
                + " version=\"" + safeXmlAttr(Pvcfg.get_firmware_version()) + "\""
                + " board=\"" + safeXmlAttr("") + "\""
                + "/>";

        /*xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<FirstBoot"
                + " id=\"" + safeXmlAttr("015000831900000070") + "\""
                + " ip1=\"" + safeXmlAttr("192.168.50.202") + "\""
                + " ip2=\"" + safeXmlAttr("") + "\""
                + " mac1=\"" + safeXmlAttr("5C:B4:E2:03:D0:FE") + "\""
                + " mac2=\"" + safeXmlAttr("") + "\""
                + " version=\"" + safeXmlAttr("1.1.9") + "\""
                + " board=\"" + safeXmlAttr("") + "\""
                + "/>";*/

        Log.d(TAG, "get_upload_first_boot_info_xml_string: " + xmlString);
        return xmlString;
    }

    public static String get_boot_login_xml_string(Context context) {
        boolean isWifi = NetworkUnit.is_wifi_enable(context);
        String ip = isWifi? NetworkUnit.get_wifi_ip(context):NetworkUnit.get_ethernet_ip(context);
        String macTmp = isWifi? NetworkUnit.get_Wifi_mac(): NetworkUnit.get_Ethernet_mac();
        String mac = "";
        if (macTmp != null && !macTmp.isEmpty())
            mac = macTmp.replaceAll("..(?!$)", "$0:");

        String xmlString ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<StartupInfo"
                + " id=\"" + safeXmlAttr(Pvcfg.get_device_sn()) + "\""
                + " ip1=\"" + safeXmlAttr(ip) + "\""
                + " ip2=\"" + safeXmlAttr("") + "\""
                + " mac1=\"" + safeXmlAttr(mac) + "\""
                + " mac2=\"" + safeXmlAttr("") + "\""
                + " version=\"" + safeXmlAttr(Pvcfg.get_firmware_version()) + "\""
                + " board=\"" + safeXmlAttr("") + "\""
                + "/>";

        /*xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<StartupInfo"
                + " id=\"" + safeXmlAttr("015000831900000070") + "\""
                + " ip1=\"" + safeXmlAttr("192.168.50.202") + "\""
                + " ip2=\"" + safeXmlAttr("") + "\""
                + " mac1=\"" + safeXmlAttr("5C:B4:E2:03:D0:FE") + "\""
                + " mac2=\"" + safeXmlAttr("") + "\""
                + " version=\"" + safeXmlAttr("1.1.9") + "\""
                + " board=\"" + safeXmlAttr("") + "\""
                + "/>";*/

        Log.d(TAG, "get_boot_login_xml_string: " + xmlString);
        return xmlString;
    }

    public static String get_upload_update_status_xml_string() {
        String xmlString ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<SendInfo"
                + " id=\"" + safeXmlAttr(Pvcfg.get_device_sn()) + "\""
                + " statuscode=\"" + safeXmlAttr("200") + "\""
                + " version=\"" + safeXmlAttr(Pvcfg.get_firmware_version()) + "\""
                + "/>";

        /*xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<SendInfo"
                + " id=\"" + safeXmlAttr("015000831900000070") + "\""
                + " statuscode=\"" + safeXmlAttr("200") + "\""
                + " version=\"" + safeXmlAttr("1.1.9") + "\""
                + "/>";*/

        Log.d(TAG, "get_upload_update_status_xml_string: " + xmlString);
        return xmlString;
    }

    private static String safeXmlAttr(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public static void process_ota_download_file(Context context, UpdateInfo updateInfo) {

        Intent intent = new Intent(OtaModule.ABUPDATE_BROADCAST_DOWNLOAD_ZIP_FILE);
        intent.putExtra(OtaModule.BROADCAST_DOWNLOAD_ZIP_URL, updateInfo.mUpgradeFileUrl);
        intent.putExtra(OtaModule.BROADCAST_DOWNLOAD_ZIP_MD5, updateInfo.mMD5);
        intent.setPackage(OtaModule.OTA_SERVICE_PACKAGE_NAME);
        context.sendBroadcast(intent);
    }

    public void start_receiver(Context context) {
        BroadcastReceiver systemReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case OtaModule.ABUPDATE_BROADCAST_COMPLETE:
                    {
                        Log.i( TAG, "ota service update complete!!!" ) ;
                        set_download_status(OtaModule.IDLE);
                        send_start_reboot(context);
                    }break;
                    case OtaModule.ABUPDATE_BROADCAST_ERROR:
                    {
                        set_download_status(OtaModule.UPDATE_FAIL);
                        Log.i( TAG, "ota service update error!!!" ) ;
                    }break;
                    case OtaModule.ABUPDATE_BROADCAST_DOWNLOAD_COMPLETE:
                    {
                        set_download_status(OtaModule.DOWNLOAD_COMPLETE);
                        Log.i( TAG, "ota service zip file download complete!!!" ) ;

                        if (is_force_update())
                            force_update_mode(context);
                        else
                            normal_update_mode(context);
                    }break;
                    case OtaModule.ABUPDATE_BROADCAST_DOWNLOAD_ERROR:
                    {
                        set_download_status(OtaModule.DOWNLOAD_FAIL);
                        Log.i( TAG, "ota service zip file download error!!!" ) ;
                    }break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OtaModule.ABUPDATE_BROADCAST_ERROR);
        intentFilter.addAction(OtaModule.ABUPDATE_BROADCAST_COMPLETE);
        intentFilter.addAction(OtaModule.ABUPDATE_BROADCAST_STOP);
        intentFilter.addAction(OtaModule.ABUPDATE_BROADCAST_DOWNLOAD_COMPLETE);
        intentFilter.addAction(OtaModule.ABUPDATE_BROADCAST_DOWNLOAD_ERROR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(systemReceiver, intentFilter, Context.RECEIVER_EXPORTED);
        }

    }

    public void set_download_status(int status) {
        Log.d(TAG, "set_download_status = " + status);
        SystemProperties.set(OtaModule.OTA_DOWNLOAD_STATUS, String.valueOf(status));
    }

    public void send_register_caller(Context context) {
        Log.d(TAG, "broadcastPackageName: package name = " + context.getPackageName());
        Intent intent = new Intent(OtaModule.ABUPDATE_BROADCAST_REGISTER_CALLER);
        intent.setPackage(OtaModule.OTA_SERVICE_PACKAGE_NAME);
        //intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        intent.putExtra(OtaModule.ABUPDATE_BROADCAST_UPDATE_CALLER, context.getPackageName());
        context.sendBroadcast(intent);
    }

    public boolean is_screen_off(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            isScreenOn = pm.isInteractive();
        } else {
            isScreenOn = pm.isScreenOn();
        }
        return !isScreenOn;
    }

    private boolean is_force_update() {
        if (mUpdateInfo == null) {
            Log.e(TAG, "is_force_update: mUpdateInfo == null");
            return false;
        }

        if (mUpdateInfo.get_download_type().equals(UpdateInfo.FORCE_UPDATE))
            return true;
        else
            return false;
    }

    private void screen_on_mode(Context context) {
        Log.d(TAG, "screen_on_mode: ");

        if (is_force_update()) {
            Log.d(TAG, "screen_on_mode: force update");
        }
        else Log.d(TAG, "screen_on_mode: normal update");
    }

    private void screen_off_mode(Context context) {
        Log.d(TAG, "screen_off_mode: ");

        if (is_force_update()) {
            Log.d(TAG, "screen_off_mode: force update");
            start_update(context);
        }
        else Log.d(TAG, "screen_off_mode: normal update");
    }

    private void force_update_mode(Context context) {

        if (is_screen_off(context))
            start_update(context);
        else
            showUpdateCountDownDialog();
    }

    private void normal_update_mode(Context context) {
        if (is_screen_off(context)) {
            start_normal_update(context);
        }
        else
            showUpdateDialog();
    }

    private void start_normal_update(Context context) {
        if (!check_time()) {
            Log.i(TAG, "start_normal_update: time not between 2:00 ~ 5:00 am");
            return;
        }

        start_update(context);
    }

    public void start_update(Context context) {
        send_register_caller(context);
        Intent otaIntent = get_ota_intent();
        Log.d(TAG, "start_update: " + otaIntent);
        context.sendBroadcast(otaIntent);
    }

    private Intent get_ota_intent() {
        Intent intent = new Intent(OtaModule.ABUPDATE_BROADCAST_START);
        intent.setPackage(OtaModule.OTA_SERVICE_PACKAGE_NAME);
        intent.putExtra(OtaModule.ABUPDATE_BROADCAST_UPDATE_ZIP_URL,      "file://"+ OtaModule.TARGET_PATH);
        intent.putExtra(OtaModule.ABUPDATE_BROADCAST_UPDATE_ZIP_OFFSET,   0);
        intent.putExtra(OtaModule.ABUPDATE_BROADCAST_UPDATE_ZIP_SIZE,     "mPayloadSize");
        intent.putExtra(OtaModule.ABUPDATE_BROADCAST_UPDATE_PROPERTIES,   "mProperty");
        intent.putExtra(OtaModule.ABUPDATE_BROADCAST_UPDATE_MODE,         OtaModule.UPDATE_MODE);
        return intent;
    }

    public void showUpdateCountDownDialog() {
        Log.d(TAG, "showUpdateCountDownDialog:");

        String lastVersion;
        if (mUpdateInfo == null || mUpdateInfo.get_last_version() == null)
            lastVersion = "";
        else
            lastVersion = mUpdateInfo.get_last_version();
        Log.d(TAG, "showUpdateCountDownDialog: lastVersion = " + lastVersion);
        mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_OTA_SHOW_UPDATE_COUNT_DOWN_DIALOG, 0, 0, lastVersion);
    }

    private void showUpdateDialog() {
        Log.d(TAG, "showUpdateDialog: ");

        String lastVersion;
        if (mUpdateInfo == null || mUpdateInfo.get_last_version() == null)
            lastVersion = "";
        else
            lastVersion = mUpdateInfo.get_last_version();

        Log.d(TAG, "showUpdateDialog: lastVersion = " + lastVersion);
        mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_OTA_SHOW_UPDATE_DIALOG, 0, 0, lastVersion);
    }

    public boolean check_time() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalTime now = LocalTime.now();     // 使用系統時區
            LocalTime start = LocalTime.of(2, 0);
            LocalTime end   = LocalTime.of(5, 0);
            return !now.isBefore(start) && now.isBefore(end);
        }
        else
            return false;
    }

    public void send_start_reboot(Context context)
    {
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        context.sendBroadcast(intent);
    }
}
