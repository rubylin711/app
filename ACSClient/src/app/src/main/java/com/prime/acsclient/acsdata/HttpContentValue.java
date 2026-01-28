package com.prime.acsclient.acsdata;

import android.util.Log;

import com.prime.acsclient.common.CommonDefine;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class HttpContentValue {
    private static String TAG = "ACS_HttpContentValue" ;
    public static class ProvisionDeviceInfo{
        public static String eth_mac_address = null ;
        public static String android_id = null ;
        public static String fw_version = null ;
        public static String model_name = null ;
        public static String sub_model = null ;
        public static String android_version = null;
        public static String wifi_mac = null ;
        public static String serial_number = null ;
        public static void set_provision_device_info(
                String new_eth_mac,
                String new_android_id,
                String new_fw_version,
                String new_model_name,
                String new_sub_model,
                String new_android_version,
                String new_wifi_mac,
                String new_serial_number )
        {
            eth_mac_address = new_eth_mac ;
            android_id = new_android_id ;
            fw_version = new_fw_version ;
            model_name = new_model_name ;
            sub_model = new_sub_model ;
            android_version = new_android_version ;
            wifi_mac = new_wifi_mac ;
            serial_number = new_serial_number ;
        }
    }

    public static class ProvisionResponse{
        public static JSONObject provision_response_rawdata = null ;
        public static JSONArray launcher_profile_rawdata = null ;
        public static JSONArray ad_profile_rawdata = null ;
        public static JSONArray network_quality_info_rawdata = null ;
        public static JSONArray music_category_rawdata = null ;
        public static JSONObject device_settings_rawdata = null ;
        public static JSONObject device_switchs_rawdata = null ;
        public static JSONArray eula_rawdata = null ;
        public static String mqtt_client_id = null ;
        public static String mqtt_user_name = null ;
        public static String mqtt_server_ip = null ;
        public static int mqtt_server_port = CommonDefine.DEFAULT_INT_VALUE ;
        public static int mqtt_tls_server_port = CommonDefine.DEFAULT_INT_VALUE ;
        public static int mqtt_qos = CommonDefine.DEFAULT_INT_VALUE ;
        public static String mqtt_subscribe_topics = null ;
        public static String mqtt_publish_topics = null ;
        public static String http_fwinfo_url = CommonDefine.ACS_HTTP_POST_PRODUCTION_PREFIX_URL + CommonDefine.ACS_HTTP_POST_FWINFO_TOKEN ;
        public static String http_appinfo_url = CommonDefine.ACS_HTTP_POST_PRODUCTION_PREFIX_URL + CommonDefine.ACS_HTTP_POST_APPINFO_TOKEN ;
        public static String http_launchernfo_url = CommonDefine.ACS_HTTP_POST_PRODUCTION_PREFIX_URL + CommonDefine.ACS_HTTP_POST_LAUNCHERINFO_TOKEN ;
        public static int ota_check_interval = CommonDefine.DEFAULT_INT_VALUE ;
        public static int fwinfo_check_interval = CommonDefine.DEFAULT_INT_VALUE ;
        public static int fwinfo_check_daily_interval = CommonDefine.DEFAULT_INT_VALUE ;
        public static String mqtt_ota_topic_group_name = null ;
        public static int appinfo_check_interval = CommonDefine.DEFAULT_INT_VALUE ;
        public static int appinfo_check_daily_interval = CommonDefine.DEFAULT_INT_VALUE ;
        public static int launcherinfo_check_interval = CommonDefine.DEFAULT_INT_VALUE ;
        public static int launcherinfo_check_daily_interval = CommonDefine.DEFAULT_INT_VALUE ;
        public static int save_log_size_mb = CommonDefine.DEFAULT_INT_VALUE ;
        public static boolean is_save_log_enable = false ;
        public static String hdmi_output_control = null ;
        public static String log_upload_url = null ;
        public static String notify_center_api = null ;
        public static String project_name = null ;
        public static int project_id = CommonDefine.DEFAULT_INT_VALUE ;
        public static int is_network_eth_wifi_auto_switch_enable = CommonDefine.DEFAULT_INT_VALUE ;
        public static JSONArray mqtt_topic_info = null ;
        public static JSONArray mqtt_tv_mail_topic = null ;
        public static String mqtt_app_topic_group_name = null ;
        public static int is_illegal_network = CommonDefine.DEFAULT_INT_VALUE ;
        public static String storage_server = CommonDefine.ACS_HTTP_POST_RECOMMEND_PACKAGES_RES_DATA_SERVER_URL ;
        public static String dvr_pair_hdd_info = null ;
        public static boolean dlna = false ;
        public static boolean miracast = false ;
        public static boolean auto_log = false ;
        public static String bplus_trigger = null ;
        public static int report_interval = CommonDefine.DEFAULT_INT_VALUE ;
        public static String message_type = null ;
        public static int group_config_version = CommonDefine.DEFAULT_INT_VALUE ;
        public static String uba_white_list = null ;
        public static boolean uba_recommendation = false ;
        public static boolean uba_remote_button = false ;
        public static boolean uba_launch_point = false ;
        public static boolean uba_direction_key = false ;
        public static boolean uba_collect = false ;
        public static String uba_upload_min = null ;
        public static String uba_force_uploader_hour = null ;
        public static String prompts = null ;
        public static String eula_url = null ;
        public static String eula_version = null ;

        public static ArrayList<LauncherProfile> launcher_profile_list = null ;
        public static ArrayList<AdProfile> ad_profile_list = null ;
        public static ArrayList<NetworkQualityInfo> network_quality_info_list = null ;
        public static ArrayList<MusicCategory> music_category_list = null ;
        public static ArrayList<Eula> eula_list = null ;
        /*
        public static class DeviceSwitchs
        {
            public static int is_rms_upload = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_show_tvmail = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_hdmi_output = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_dvr_enable = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_standby_redirect = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_ip_whitelist_enable = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_force_lock_screen = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_launcher_lock = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_marquee_display = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_system_alert = CommonDefine.DEFAULT_INT_VALUE ;
            public static int home_id = CommonDefine.DEFAULT_INT_VALUE ;
            public static String pin_code = null ;
            public static int member_id = CommonDefine.DEFAULT_INT_VALUE ;
            public static int pa_day = CommonDefine.DEFAULT_INT_VALUE ;
            public static int nit_id = CommonDefine.DEFAULT_INT_VALUE ;
            public static int region_code = CommonDefine.DEFAULT_INT_VALUE ;
            public static int operator_code = CommonDefine.DEFAULT_INT_VALUE ;
            public static int dtv_status = CommonDefine.DEFAULT_INT_VALUE ;
        }
        */
        public static class DeviceSettings
        {
            public static int is_rms_upload = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_show_tvmail = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_hdmi_output = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_dvr_enable = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_standby_redirect = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_ip_whitelist_enable = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_force_lock_screen = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_launcher_lock = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_marquee_display = CommonDefine.DEFAULT_INT_VALUE ;
            public static int is_system_alert = CommonDefine.DEFAULT_INT_VALUE ;

            public static int dtv_status = CommonDefine.DEFAULT_INT_VALUE ;
            public static JSONObject device_params_rawdata = null ;
            public static class DeviceParams
            {
                public static int pa_day = 60 ;
                public static String nit_id = null ;
                public static String home_id = null ;
                public static String pin_code = null ;
                public static String mobile = null ;
                public static String member_id = null ;
                public static String bat_id = null ;
                public static String nit_params = null ;
                public static int network_sync_time = 360 ;
                public static String region_code = null ;
                public static String operator_code = null ;
                public static String force_screen_params = null;
                public static String system_alert_params = null;
            }
        }
        public static class Eula
        {
            public String name = null ;
            public int sort = CommonDefine.DEFAULT_INT_VALUE ;
            public String url = null ;
            public String version = null ;
            public Eula()
            {

            }
        }
        public static class MusicCategory
        {
            public String name = null ;
            public String icon_url = null ;
            public int position_index = CommonDefine.DEFAULT_INT_VALUE ;
            public List<String> service_id_list = null ;
        }
        public static class LauncherProfile
        {
            public String app_name = null ;
            public String package_name = null ;
            public String thumbnail_url = null ;
            public int icon_priority = CommonDefine.DEFAULT_INT_VALUE ;
            public String launch_activity_name = null ;
            public String rcu_id = null ;
            public String lunch_app_params = null ;
            public String app_short_description = null ;
            public String app_full_description = null ;
            public String app_download_url = null ;
            public String app_version_code = null ;
            public String app_dispaly_label = null ;
            public String app_screen_captures = null ;
            public int app_disaply_position = CommonDefine.DEFAULT_INT_VALUE ;
            public boolean is_force_update = false ;
        }
        public static class NetworkQualityInfo
        {
            public String eng_description = null ;
            public String chinese_description = null ;
            public int highest_speed = CommonDefine.DEFAULT_INT_VALUE ;
            public int lowest_speed = CommonDefine.DEFAULT_INT_VALUE ;
            public int index = CommonDefine.DEFAULT_INT_VALUE ;
        }
        public static class AdProfile
        {
            public String url = null;
        }
    }

    public static class FwInfo
    {
        public static JSONObject fwinfo_response_rawdata = null ;
        public static String update_mode = null ;
        public static String fw_version = null ;
        public static String release_notes = null ;
        public static JSONObject fw_download_path_rawdata = null ;
        public static class FW_DOWNLOAD_PATH
        {
            public static String low_bw_url = null ;
            public static String high_bw_url = null ;
        }
        public static String md5_checksum = null ;
        public static int file_size = CommonDefine.DEFAULT_INT_VALUE ;
        public static int http_status = CommonDefine.DEFAULT_INT_VALUE ;
        public static String model_checker = null ;
        public static int is_ota_in_standby = CommonDefine.DEFAULT_INT_VALUE ;
        public static int is_ota_after_boot = CommonDefine.DEFAULT_INT_VALUE ;
        public static int is_ota_immediately = CommonDefine.DEFAULT_INT_VALUE ;
        public static String is_ota_in_time_window = null ;
    }

    public static class AppInfo
    {
        public static JSONObject appinfo_response_rawdata = null ;

        public static String app_download_url_prefix = null ;
        public static JSONArray apps_info_rawdata = null ;
        public static ArrayList<Apps_info> apps_info_list = null ;
        public static class Apps_info {
            public String package_name = null ;
            public String apk_file_name = null ;
            public String md5_checksum = null ;
            public String apk_version_code = null ;
            public String apk_version_name = null ;
            public String apk_sdk_version = null ;
            public String apk_target_version = null ;
            public int is_install = CommonDefine.DEFAULT_INT_VALUE;
            public String install_mode = null ;
        }
    }

    public static class AdList
    {
        public static JSONArray ad_list_rawdata = null ;
    }

    public static class WeekHightLight
    {
        public static JSONArray week_highlight_rawdata = null ;
    }

    public static class RankList
    {
        public static JSONArray rank_list_rawdata = null ;
    }

    public static class MusicAD
    {
        public static JSONArray music_ad_rawdata = null ;
    }

    public static class LauncherInfo
    {
        public static JSONObject launcher_info_rawdata = null ;
        public static String model_name = null ;
        public static String android_version = null ;
        public static String ota_started_time = null ;
        public static int app_profile_id = CommonDefine.DEFAULT_INT_VALUE ;
        public static int recommendation_profile_id = CommonDefine.DEFAULT_INT_VALUE ;
        public static int hot_video_profile_id = CommonDefine.DEFAULT_INT_VALUE ;
        public static String led_style = null ;
    }

    public static class HotVideo
    {
        public static JSONArray hot_video_rawdata = null ;
    }
    public static class RecommendPackages
    {
        public static JSONObject recommend_packages_rawdata = null ;
        public static JSONArray recommend_packages_json_data = null ;
    }
}
