package com.prime.acsclient.common;


import com.prime.acsclient.ACSService;
import com.prime.acsclient.acsdata.HttpContentValue;

import java.util.UUID;

public class CommonDefine {
    public static int SUCCESS = 0 ;
    public static int FAIL = 1 ;
    public static int ONE_SECOND = 1000 ;
    public static int DEFAULT_INT_VALUE = -1 ;
    public static String HTTP_REQUEST_METHOD_POST = "POST" ;
    public static String HTTP_REQUEST_METHOD_GET = "GET" ;
    public static String HTTP_UPLOAD_URL_KEY = "UPLOAD_URL" ;
    public static String HTTP_UPLOAD_FILE_PATH_KEY = "FILE_PATH" ;
    public static int HTTP_FILE_SIZE_1MB = 1024*1024 ;

    // for MQTT SCREEN_CAPTURE
    public static String SCREEN_CAPTURE_FILE_NAME = "/screencap.png" ;
    // for MQTT GET_INFO
    public static String GET_INFO_BOX_INFO = "/box.info" ;
    public static String GET_INFO_TOMBSTONES = "/box.tombstones" ;
    public static String GET_INFO_ANR = "/box.traces" ;
    public static String ANDROID_DROPBOX_TOMBSTONES_PATH = "/data/system/dropbox/" ;
    public static String ANDROID_ANR_PATH = "/data/anr/" ;

    // for MQTT STATUS_UPDATE
    public static final int MINOR_DEVICE_CLASS_REMOTE = Integer.parseInt("0000000001100", 2);
    public static final int MINOR_DEVICE_CLASS_KEYBOARD = Integer.parseInt("0000001000000", 2);
    public static final UUID GATT_BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static final UUID GATT_BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");

    // HTTP
    public static final String ACS_SERVER_TYPE_PROPERTY = "persist.sys.prime.acs.server_type" ;
    public static final String ACS_SERVER_TYPE_PRODUCTION = "Production" ;
    public static final String ACS_SERVER_TYPE_Lab = "Lab" ;
    public static final String ACS_HTTP_POST_PRODUCTION_PREFIX_URL = "https://acs-portal.tbcnet.net.tw/" ;
    public static final String ACS_HTTP_POST_LAB_TEST_PREFIX_URL = "https://acs-sit.tbcnet.net.tw/" ;
    public static final String ACS_HTTP_POST_PROVISION_TOKEN = "provision" ;
    public static final String ACS_HTTP_POST_FWINFO_TOKEN = "fwinfo" ;
    public static final String ACS_HTTP_POST_APPINFO_TOKEN = "appinfo" ;
    public static final String ACS_HTTP_POST_LAUNCHERINFO_TOKEN = "launcherinfo" ;
    public static final String ACS_HTTP_POST_CHECK_NETWORK_CONNECT_TOKEN = "ottboxs/networkCheck" ;
    public static final String ACS_HTTP_POST_AD_TOKEN = "ottboxs/TVRecommendation" ;
    public static final String ACS_HTTP_POST_WEEK_HIGHLIGHT_TOKEN = "bandott/recommendation" ;
    public static final String ACS_HTTP_GET_RANKLIST_TOKEN = "ottboxs/getRankingList" ;
    public static final String ACS_HTTP_POST_HOT_VIDEO_TOKEN = "ottboxs/GetHotVideoInfos" ;
    public static final String ACS_HTTP_POST_RECOMMEND_PACKAGES_TOKEN = "ottboxs/storage/host" ;
    /*
    ACS_HTTP_GET_RECOMMEND_PACKAGES_SERVER_URL will get
    {
        "status": 200,
        "message": "success",
        "server": "https://epgstore.tbc.net.tw/acs-api/"
    }
    use "server" to combine url : https://epgstore.tbc.net.tw/acs-api/package/excel/package_list.json
    direct get file from https://epgstore.tbc.net.tw/acs-api/package/excel/package_list.json
     */
    public static final String ACS_HTTP_POST_RECOMMEND_PACKAGES_RES_DATA_SERVER_URL = "https://epgstore.tbc.net.tw/acs-api/" ;
    public static final String ACS_HTTP_GET_RECOMMEND_PACKAGES_FILE_PATH = "/package/excel/package_list.json" ;

    public static final int IS_DATILY_CHECK = 0x12345678 ;
    public static final int ACS_HTTP_CONNECT_TIMEOUT_DURATION = 60*CommonDefine.ONE_SECOND ;
    public static final int ACS_HTTP_CONNECT_FAIL_RETRY_DURATION = 60*CommonDefine.ONE_SECOND ;
    public static final int ACS_HTTP_BASE_CASEID = 0x1000 ;
    public static final int ACS_HTTP_PROVISION_CASEID = ACS_HTTP_BASE_CASEID + 1 ;
    public static final int ACS_HTTP_APPINFO_CASEID = ACS_HTTP_BASE_CASEID + 2 ;
    public static final int ACS_HTTP_FWINFO_CASEID = ACS_HTTP_BASE_CASEID + 3 ;
    public static final int ACS_HTTP_LAUNCHERINFO_CASEID = ACS_HTTP_BASE_CASEID + 4 ;
    public static final int ACS_HTTP_CHECK_NETWORK_CONNECT_CASEID = ACS_HTTP_BASE_CASEID + 5 ;
    public static final int ACS_HTTP_POST_AD_LIST_CASEID = ACS_HTTP_BASE_CASEID + 6 ;
    public static final int ACS_HTTP_POST_WEEK_HIGHLIGHT_CASEID = ACS_HTTP_BASE_CASEID + 7 ;
    public static final int ACS_HTTP_GET_RANK_LIST_CASEID = ACS_HTTP_BASE_CASEID + 8 ;
    public static final int ACS_HTTP_POST_HOT_VIDEO_CASEID = ACS_HTTP_BASE_CASEID + 9 ;
    public static final int ACS_HTTP_POST_RECOMMEND_PACKAGES_CASEID = ACS_HTTP_BASE_CASEID + 10 ;
    public static final int ACS_HTTP_UPLOAD_FILE_CASEID = ACS_HTTP_BASE_CASEID + 11 ;
    public static final int ACS_HTTP_CHECK_PA_ACS_SERVER_STATUS = ACS_HTTP_BASE_CASEID + 12 ;
    public static final int ACS_HTTP_RENEW_DEVICE_COMMON_CASEID = ACS_HTTP_BASE_CASEID + 13 ;

    // MQTT
    public static final String ACS_MQTT_DEBUG_FLAG = "persist.sys.prime.acs_mqtt_debug_flag";
    public static final int ACS_MQTT_BASE_CASEID = 0x2000 ;
    public static final int ACS_MQTT_SERVER_CONNECT_CASEID = ACS_MQTT_BASE_CASEID + 1 ;
    public static final int ACS_MQTT_SERVER_DISCONNECT_CASEID = ACS_MQTT_BASE_CASEID + 2 ;
    public static final int ACS_MQTT_SERVER_PUBLISH_CASEID = ACS_MQTT_BASE_CASEID + 3 ;
    public static final int ACS_MQTT_RESUBSCRIBE_CASEID = ACS_MQTT_BASE_CASEID + 4 ;
    public static final int ACS_MQTT_INNER_TRIGGER_BOX_STATUS_CASEID = ACS_MQTT_BASE_CASEID + 5 ;
    public static final int ACS_MQTT_EVERY_DAY_CHECKING_CASEID = ACS_MQTT_BASE_CASEID + 6 ;
    public static final int ACS_MQTT_INSTANT_MONITORING_CHECKING_CASEID = ACS_MQTT_BASE_CASEID + 7 ;
    public static final int ACS_MQTT_DEVELOP_TEST_CASEID = ACS_MQTT_BASE_CASEID + 100 ;

    // logcat
    public static int LOG_SEPARATE_PART = 10 ;
    public static final int ACS_LOGCAT_BASE_CASEID = 0x4000 ;
    public static final int ACS_LOGCAT_INIT_CASEID = ACS_LOGCAT_BASE_CASEID + 1 ;
    public static final int ACS_LOGCAT_DELETE_CASEID = ACS_LOGCAT_BASE_CASEID + 2 ;
    public static final int ACS_LOGCAT_CAPTURE_CASEID = ACS_LOGCAT_BASE_CASEID + 3 ;
    public static final int ACS_LOGCAT_UPDATE_PARAM_CASEID = ACS_LOGCAT_BASE_CASEID + 4 ;
    public static final int ACS_LOGCAT_UPLOAD_CASEID = ACS_LOGCAT_BASE_CASEID + 5 ;

    // OTA
    public static String IS_BOOT_COMPLETE = "isBootComplete" ;
    public static final String OTA_FORCE_UPDATE = "system" ;
    public static final String OTA_USER_UPDATE = "user" ;
    public static final int ACS_OTA_BASE_CASEID = 0x8000 ;
    public static final int ACS_OTA_UPDATE_PARAM = ACS_OTA_BASE_CASEID + 1 ;

    public static final String OTA_MODE = "persist.sys.prime.ota_mode" ;
    public static final String OTA_DOWNLOAD_STATUS = "persist.sys.prime.ota_download_status";
    public static final String OTA_LAST_DOWNLOAD_VERSION = "persist.sys.prime.ota_last_download_version";


    // APP install/uninstall
    public static int UNINSTALL = 0 ;
    public static int INSTALL_ALWAYS = 1 ;
    public static int INSTALL_ONCE = 2 ;

    public static int IDLE = 0 ;
    public static int DOWNLOADING = 1 ;
    public static int DOWNLOAD_COMPLETE = 2 ;
	public static int DOWNLOAD_FAIL = 3 ;
}
