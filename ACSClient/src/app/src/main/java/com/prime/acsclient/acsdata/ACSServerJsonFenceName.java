package com.prime.acsclient.acsdata;

import com.prime.acsclient.acsdata.HttpContentValue;
import com.prime.acsclient.common.CommonFunctionUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ACSServerJsonFenceName {
    public static class ProvisionDeviceInfo {
        public static final String ETH_MAC_ADDR = "mac_address";
        public static final String ANDROID_ID = "android_id";
        public static final String FW_VERSION = "current_fw_version";
        public static final String MODEL_NAME = "model_name";
        public static final String SUB_MODEL = "sub_model";
        public static final String ANDROID_VERSION = "android_version";
        public static final String WIFI_MAC_ADDR = "wifi_mac";
        public static final String SERIAL_NUMBER = "serial";
        public static final String CHECK_NETWORK_CONNECT_ETH_MAC = "mac";
    }

    public static class ProvisionResponse {
        public static final String PROVISION_DATA = "provision_data";
        public static final String MQTT_CLIENT_ID = "sub_client_id";
        public static final String MQTT_USER_NAME = "sub_username";
        public static final String MQTT_SERVER_IP = "sub_host";
        public static final String MQTT_SERVER_PORT = "sub_port";
        public static final String MQTT_TLS_SERVER_PORT = "sub_tls_port";
        public static final String MQTT_QOS = "sub_qos";
        public static final String MQTT_SUBSCRIBE_TOPICS = "sub_topics";
        public static final String MQTT_PUBLISH_TOPICS = "pub_topics";
        public static final String HTTP_FWINFO_URL = "ota_sync_api";
        public static final String HTTP_APPINFO_URL = "app_sync_api";
        public static final String HTTP_LAUNCHERINFO_URL = "launcher_sync_api";
        public static final String OTA_CHECK_INTERVAL = "ota_sync_interval";
        public static final String FWINFO_CHECK_INTERVAL = "sw_sync_interval";
        public static final String FWINFO_CHECK_DAILY_INTERVAL = "fw_daily_confirm";
        public static final String MQTT_OTA_TOPIC_GROUP_NAME = "sw_group_name";
        public static final String APPINFO_CHECK_INTERVAL = "app_sync_interval";
        public static final String APPINFO_CHECK_DAILY_INTERVAL = "app_daily_confirm";
        public static final String LAUNCHERINFO_CHECK_INVERVAL = "launcher_sync_interval";
        public static final String LAUNCHERINFO_CHECK_DAILY_INTERVAL = "launcher_daily_confirm";
        public static final String SAVE_LOG_SIZE_MB = "logSize";
        public static final String IS_SAVE_LOG_ENABLE = "enable_log";
        public static final String HDMI_OUTPUT_CONTROL = "HDMIOutControl";
        public static final String LAUNCHER_PROFILE = "launcher_profile";
        public static final String LOG_UPLOAD_URL = "report_api";
        public static final String AD_PROFILE = "ad";
        public static final String NOTIFY_CENTER_API = "notify_center_api";
        public static final String DEVICE_SWITCHS = "DeviceSwitchs";
        public static final String DEVICE_PARAMS = "deviceParams";
        public static final String DEVICE_SETTINGS = "DeviceOperation";
        public static final String PROJECT_NAME = "projectName";
        public static final String PROJECT_ID = "projectId";
        public static final String IS_NETWORK_ETH_WIFI_AUTO_SWITCH = "networkSwitch";
        public static final String TOPIC_INFO = "topicInfo";
        public static final String MQTT_TV_MAIL_TOPIC = "ConditionTopic";
        public static final String MQTT_APP_TOPIC_GROUP_NAME = "app_group_name";
        public static final String EULA = "Eula";
        public static final String MUSIC_CATEGORY = "music_category";
        public static final String IS_ILLEGAL_NETWORK = "IllegalNetwork";
        public static final String STORAGE_SERVER = "storage_server";
        public static final String NETWORK_QUALITY_INFO = "networkQualityDefined";
        public static final String DVR_PAIR_HDD_INFO = "DVRPairHDs";
        public static final String DLNA = "dlna";
        public static final String MIRACAST = "miracast";
        public static final String AUTO_LOG = "auto_log";
        public static final String BPLUS_TRIGGER = "BplusTrigger";
        public static final String REPORT_INTERVAL = "report_interval";
        public static final String MESSAGE_TYPE = "message_type";
        public static final String GROUP_CONFIG_VERSION = "group_config_version";
        public static final String UBA_WHITE_LIST = "uba_white_list";
        public static final String UBA_RECOMMENDATION = "UBARecommendation";
        public static final String UBA_REMOTE_BUTTON = "UBARemoteButton";
        public static final String UBA_LAUNCHER_POINT = "UBALaunchPoint";
        public static final String UBA_DIRECTRY_KEY = "UBADirectionKey";
        public static final String USB_COLLECT = "UBACollect";
        public static final String UBA_UPLOAD_MIN = "ubaUploadMin";
        public static final String UBA_FORCE_UPLOAD_HOUR = "ubaForceUploadHour";
        public static final String PROMOTS = "prompts";
        public static final String EULA_URL = "EulaUrl";
        public static final String EULA_VERSION = "EulaVersion";


        public static class DeviceSettings {
            public static final String RMS_STATUS = "RMSStatus";
            public static final String TVMAIL_STATUS = "TVMAILStatus";
            public static final String HDMI_OUT_STATUS = "HdmiOutStatus";
            public static final String DVR_STATUS = "DVRStatus";
            public static final String STANDBY_REDIRECT_STATUS = "StandbyRedirectStatus";
            public static final String IP_WHITELIST_STATUS = "IPWhitelistStatus";
            public static final String FORCE_SCREEN_STATUS = "ForceScreenStatus";
            public static final String TICKER_STATUS = "TickerStatus";
            public static final String SYSTEM_ALERT_STATUS = "SystemAlertStatus";
            public static final String DTV_STATUS = "DTVStatus";
            public static final String LAUNCHER_LOCK_STATUS = "LauncherLockStatus";

            public static class DeviceParams {
                public static final String BAT_ID = "bat_id";
                public static final String BOX_SI_NIT = "box_si_nit";
                public static final String NETWORK_SYNC_TIME = "network_sync_time";
                public static final String NIT_ID = "nit_id";
                public static final String MOBILE = "mobile";
                public static final String ZIPCODE = "zipcode";
                public static final String SO = "so";
                public static final String HOME_ID = "home_id";
                public static final String PIN_CODE = "pin_code";
                public static final String MEMBER_ID = "member_id";
                public static final String PA_DAY = "pa_day";
                public static final String FORCE_SCREEN_PARAMS = "force_screen_params";
                public static final String SYSTEM_ALERT_PARAMS = "system_alert_params";
            }
        }

        public static class Eula {
            public static final String NAME = "name";
            public static final String SORT = "sort";
            public static final String URL = "url";
            public static final String VERSION = "version";
        }

        public static class MusicCategory {
            public static final String NAME = "name";
            public static final String ICON = "icon";
            public static final String SORT = "sort";
            public static final String SERVICE_LISTS = "servicelists";
        }

        public static class LauncherProfile {
            public static final String APP_NAME = "appName";
            public static final String PACKAGE_NAME = "packageName";
            public static final String THUMBNAIL = "thumbnail";
            public static final String ICON_PRIORITY = "iconPriority";
            public static final String ACTIVITY_NAME = "activityName";
            public static final String PRODUCT_ID = "productId";
            public static final String PACKAGE_PARAMS = "packageParams";
            public static final String DESCRIPTION = "description";
            public static final String FULL_TEXT = "fullText";
            public static final String APP_PATH = "appPath";
            public static final String APP_VERSION_CODE = "appVersionCode";
            public static final String APK_LABEL = "apkLabel";
            public static final String SCREEN_CAPUTRES = "screenCaptures";
            public static final String DISPLAY_POSITION = "displayPosition";
            public static final String FORCE_UPDATE = "forceUpdate";
        }

        public static class NetworkQualityInfo {
            public static final String NAME = "name";
            public static final String DESCRIPTION = "description";
            public static final String HIGHEST = "highest";
            public static final String LOWEST = "lowest";
            public static final String ID = "id";
        }

        public static class AdProfile {
            public static final String URL = "url";
        }
    }

    public static class FwInfo {
        public static final String FWINFO_DATA = "fwinfo_data";
        public static final String DRIVEN_BY = "driven_by";
        public static final String FW_VERSION = "fw_version";
        public static final String RELEASE_NOTES = "release_notes";
        public static final String FW_PATH = "fw_path";
        public static final String LOW_BW_URL = "low_bw_url";
        public static final String HIGH_BW_URL = "high_bw_url";
        public static final String MD5_CHECKSUM = "md5_checksum";
        public static final String FILE_SIZE = "file_size";
        public static final String STATUS = "status";
        public static final String MODEL_CHECKER = "model_checker";
        public static final String OTA_AFTER_STANDBY = "otaAfterStandby";
        public static final String OTA_AFTER_BOOT = "otaAfterBoot";
        public static final String OTA_AFTER_IMMEDIATELY = "otaAfterImmediately";
        public static final String OTA_AFTER_TIME = "otaAfterTime";

//        public static final String SW_SYNC_INTERVAL = ProvisionResponse.FWINFO_CHECK_INTERVAL ;
//        public static final String FW_DAILY_CONFIRM = ProvisionResponse.FWINFO_CHECK_DAILY_INTERVAL;
//        public static final String SW_GROUP_NAME = ProvisionResponse.MQTT_OTA_TOPIC_GROUP_NAME ;
    }

    public static class AppInfo {
        public static final String APP_PATH = "app_path";
        //        public static final String APP_GROUP_NAME = ProvisionResponse.MQTT_APP_TOPIC_GROUP_NAME ;
//        public static final String SW_SYNC_INTERVAL = ProvisionResponse.FWINFO_CHECK_INTERVAL ;
//        public static final String APP_SYNC_INTERVAL = ProvisionResponse.APPINFO_CHECK_INTERVAL ;
        public static final String APPS_INFO = "apps_info";
        public static final String PACKAGE_NAME = "package_name";
        public static final String APK_NAME = "apk_name";
        public static final String MD5_CHECKSUM = "md5_checksum";
        public static final String VERSION_CODE = "version_code";
        public static final String VERSION_NAME = "version_name";
        public static final String SDK_VERSION = "sdk_version";
        public static final String TARGET_VERSION = "target_version";
        public static final String ENABLE = "enabled";
        public static final String MODE = "mode";
        public static final String ALREADY_INSTALLED_APK_LIST = "already_installed_apk";
    }

    public static class LauncherInfo {
        public static String LAUNCHER_INFO = "launcher_info";
        public static String MODEL_NAME = "modelName";
        public static String ANDROID_VERSION = "androidVersion";
        public static String OTA_STARTED_TIME = "otaStarted";
        public static String APP_PROFILE_ID = "appProfileId";
        public static String RECOMMEND_PROFILE_ID = "recommendationProfileId";
        public static String HOTVIDEO_PROFILE_ID = "hotVideoProfileId";
        public static String LED_STYLE = "ledStyle";
    }

    public static class AdList {
        public static String AD_LIST = "ad_list";
        public static String AD_KEY = "tvRecommendations";
    }

    public static class WeekHightLight {
        public static String WEEK_HIGHLIGHT = "recommends";
    }

    public static class RankList {
        public static String RANK_LIST = "rank_list";
    }

    public static class HotVideo {
        public static String HOT_VIDEO = "hot_video" ;
        public static String HOT_VIDEO_KEY = "hotVideo" ;
    }

    public static class RecommendPackages {
        public static String RECOMMEND_PACKAGES = "recommend_packages" ;
        public static String SERVER = "server" ;
    }

    public static class MusicAD {
        public static String MusicAD = "ad_profile" ;
    }
    public static class MqttData {
        public static String OTA_GROUP_CHANGE = "ota_group_change";
        public static String APP_GROUP_CHANGE = "app_group_change";
        public static String TV_MAIL = "tv_mail";
    }

    public static class MqttCommand {
        public static String TASK_ID = "task_id";
        public static String COMMAND = "command";
        public static String ARGS = "args";
        public static String SERVICE_ID = "service_id" ;
        public static String SYMBOL_RATE = "symbol_rate" ;
        public static String FREQ = "frequency" ;
        public static String QAM = "qam" ;
        public static String PARENTAL_LOCK_PASSWORD = "parental_lock_password" ;
        public static String SIGNAL_DETECT_RESULT = "signal_detect";
        public static String DO_SIGNAL_DETECT = "do_signal_detect";
        public static String DO_NETWORK_SPEED_TEST = "do_network_speed_test";
        public static String PA_ACS_SERVER_IS_CONNECTED = "pa_acs_server_is_connected" ;
        public static String PA_ACS_SERVER_NO_CONNECTED_HOURS = "pa_acs_server_no_connected_hours" ;
        public static String PA_LOCK = "pa_lock" ;
        public static String CPU_TEMP = "cpu_temperature" ;
        public static String CPU_UTILITY = "cpu_utility" ;
        public static String MEM_TOTAL = "mem_total" ;
        public static String MEM_USED = "mem_used" ;
        public static String MEM_FREE = "mem_free" ;
        public static String HDCP_INFO = "hdcp_info" ;
        public static String DISP_CAP = "disp_cap" ;
        public static String DISP_MODE = "disp_mode" ;
        public static String AUD_CAP = "aud_cap" ;
        public static String EDID = "edid" ;
        public static String EDID_RAW_DATA = "edid_raw_data" ;
        public static String HDCP_USER_SETTING = "hdcp_user_setting" ;
    }

    public static class SetupWizard {
        public static String BOX_STATUS_UPLOAD_SETUPWIZARD_FINISH = "setupwizard_finished" ;
        public static String BOX_STATUS_UPLOAD_SETUPWIZARD_FINISH_TIME = "setupwizard_finish_time" ;
        public static String FINISH_TIME = "setupwizard_finish" ;
        public static String AGREE_EULA_CONTENT = "agree_eula_content" ;
        public static String AGREE_EULA_VERSION = "TermsVersion" ;
    }

    public static class BoxStatus {
        public static String STORAGE_SPACE = "storage_space" ;
        public static String LANG = "lang" ;
        public static String SOUND_TYPE = "sound_type" ;
        public static String SCREEN_RESOLUTION = "screen_resolution" ;
        public static String TV_MODEL = "tv_model" ;
        public static String HDMI_CEC = "hdmi_cec" ;
        public static String INTERACTIVE_MODE = "interactive_mode" ;
        public static String INTERACTIVE_CHANGE_TIME = "interactive_change_time" ;
        public static String WIFI_BSSID = "wifi_bssid" ;
        public static String WIFI_SSID = "wifi_ssid" ;
        public static String WIFI_FREQ = "wifi_freq" ;
        public static String WIFI_RSSI = "wifi_rssi" ;
        public static String SERVER_TYPE = "ServerType" ;
        public static String ADB_ENABLED = "enable_adb" ;
        public static String BT_DEVICES = "BT_Devices" ;
        public static String BT_DEVICES_NAME = "name" ;
        public static String BT_DEVICES_MAC = "mac" ;
        public static String BT_DEVICES_ECECTRICITY = "electricity" ;
        public static String BT_LOW_POWER_CONFIRM = "BT_LowPowerConfirm" ;
        public static String USB_STATUS = "USB_status" ;
        public static String USB_DEVICES = "USB_Devices" ;
        public static String USB_DEVICES_PRODUCT_NAME = "product_name" ;
        public static String USB_DEVICES_SERIAL_NUMBER = "serial_number" ;
        public static String PUBLIC_IP = "Public_IP" ;
        public static String RMSCLIENT = "RMSClient" ;
        public static String ANDROID_VERSION = "Android_version";
        public static String SERIAL_NUMBER = "serialNum";
        public static String LAUNCHER_VERSION_NAME = "otl_version";
        public static String NETWORK_INTERFACE = "network_interface";
        public static String NETWORK_DNS = "network_dns";
        public static String NETWORK_BOOTPROTO = "network_bootproto";
        public static String NETWORK_SPEED_RESULT = "network_quality";
        public static String BT_MAC = "bt_mac";
    }
}
