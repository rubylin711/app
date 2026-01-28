package com.prime.acsclient.acsdata;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.hardware.hdmi.HdmiControlManager;
import android.os.PowerManager;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import com.prime.acsclient.ACSService;
import com.prime.acsclient.common.BroadcastDefine;
import com.prime.acsclient.common.CommonDefine;
import com.prime.acsclient.common.CommonFunctionUnit;
import com.prime.acsclient.common.DeviceControlUnit;
import com.prime.acsclient.common.DeviceInfoUtil;
import com.prime.acsclient.common.MqttCommand;
import com.prime.acsclient.communicate.MqttHandler;
import com.prime.acsclient.gpos.GposDatabaseTable;
import com.prime.acsclient.gpos.GposDefine;
import com.prime.acsclient.prodiver.ACSDataContentProvider;
import com.prime.acsclient.prodiver.ACSDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProcessACSData {
    public static final int HTTP_RECEIVE_DATA = 0x100 ;
    public static final int HTTP_PROCESS_PROVISION_DATA = 0x101 ;
    public static final int HTTP_PROCESS_FWINFO_DATA = 0x102 ;
    public static final int HTTP_PROCESS_APPINFO_DATA = 0x103 ;
    public static final int HTTP_PROCESS_LAUNCHERINFO_DATA = 0x104 ;
    public static final int HTTP_PROCESS_AD_LIST_DATA = 0x105 ;
    public static final int HTTP_PROCESS_WEEK_HIGHLIGHT_DATA = 0x106 ;
    public static final int HTTP_PROCESS_RANK_LIST_DATA = 0x107 ;
    public static final int HTTP_PROCESS_HOT_VIDEO_DATA = 0x108 ;
    public static final int HTTP_PROCESS_RECOMMEND_PACKAGES = 0x109 ;
    public static final int HTTP_PROCESS_RECOMMEND_PACKAGES_JSON_DATA = 0x110 ;
    public static final int MQTT_RECEIVE_DATA = 0x200 ;
    public static final int MQTT_RECEIVE_COMMAND = 0x201 ;
    public static final int MQTT_DATA_OTA_GROUP_CHANGE = 0x202 ;
    public static final int MQTT_DATA_APP_GROUP_CHANGE = 0x203 ;
    public static final int MQTT_DATA_TV_MAIL = 0x204 ;
    public static final int MQTT_PUSLISH_BOX_STATUS = 0x205 ;
    public static final int MQTT_PUSLISH_SIGNAL_DETECT = 0x206 ;
    public static final int MQTT_PUSLISH_INSTANT_MONITORING = 0x207 ;
    public static final int MQTT_PUSLISH_HDMI_INFO = 0x208 ;
    public static final int MQTT_DATA_COMMON_DEVICE_INFO = 0x209 ;

    private static void process_device_operation()
    {
        if ( HttpContentValue.ProvisionResponse.device_settings_rawdata != null )
        {
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.TVMAIL_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_show_tvmail, false ) ;

            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.FORCE_SCREEN_PARAMS, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.force_screen_params, false );
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.SYSTEM_ALERT_PARAMS, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.system_alert_params, false );

            CommonFunctionUnit.update_data_without_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.RMS_STATUS,
                    HttpContentValue.ProvisionResponse.DeviceSettings.is_rms_upload, ()->DeviceControlUnit.rms_upload_data() ); ;

            CommonFunctionUnit.update_data_without_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.HDMI_OUT_STATUS,
                    HttpContentValue.ProvisionResponse.DeviceSettings.is_hdmi_output,
                    ()->DeviceControlUnit.hdmi_output_control(HttpContentValue.ProvisionResponse.DeviceSettings.is_hdmi_output));
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DVR_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_dvr_enable, false ) ;
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DTV_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.dtv_status, false ) ;
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.FORCE_SCREEN_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_force_lock_screen, false ) ;
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.IP_WHITELIST_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_ip_whitelist_enable, false ) ;
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.LAUNCHER_LOCK_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_launcher_lock, false ) ;
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.TICKER_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_marquee_display, true ) ;
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.STANDBY_REDIRECT_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_standby_redirect, false ) ;
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.SYSTEM_ALERT_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_system_alert, true ) ;

//            "deviceParams": {
//                    "stb_id": "2236765628",
//        },
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.ZIPCODE, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.region_code, false ) ;
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.SO, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.operator_code, false ) ;
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.HOME_ID, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.home_id, false ) ;
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.PIN_CODE, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.pin_code, false);
            CommonFunctionUnit.update_data_without_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.MEMBER_ID, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.member_id ) ;
            CommonFunctionUnit.update_data_without_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.MOBILE, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.mobile );
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.NIT_ID, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.nit_id, false ) ;

            CommonFunctionUnit.update_data_without_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.PA_DAY, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.pa_day);
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.BAT_ID, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.bat_id, false );
            CommonFunctionUnit.update_data_and_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.BOX_SI_NIT, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.nit_params, false );
            CommonFunctionUnit.update_data_without_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.NETWORK_SYNC_TIME, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.network_sync_time);

            CommonFunctionUnit.send_broadcast(ACSService.g_context, BroadcastDefine.SETUPWIZARD_PACKAGE_NAME, new Intent(BroadcastDefine.ACS_GET_PROVISION_DATA) );
        }
    }

    private static void process_provision_data()
    {
        if ( HttpContentValue.ProvisionResponse.provision_response_rawdata != null ) {
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.PROVISION_DATA, HttpContentValue.ProvisionResponse.provision_response_rawdata.toString());

            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.MQTT_APP_TOPIC_GROUP_NAME, HttpContentValue.ProvisionResponse.mqtt_app_topic_group_name);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.MQTT_OTA_TOPIC_GROUP_NAME, HttpContentValue.ProvisionResponse.mqtt_ota_topic_group_name);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.MQTT_SUBSCRIBE_TOPICS, HttpContentValue.ProvisionResponse.mqtt_subscribe_topics);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.PROJECT_NAME, HttpContentValue.ProvisionResponse.project_name);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.MQTT_TV_MAIL_TOPIC, HttpContentValue.ProvisionResponse.mqtt_tv_mail_topic);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.TOPIC_INFO, HttpContentValue.ProvisionResponse.mqtt_topic_info);

            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.IS_ILLEGAL_NETWORK, HttpContentValue.ProvisionResponse.is_illegal_network, true);
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.EULA, HttpContentValue.ProvisionResponse.eula_rawdata, false);
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.MUSIC_CATEGORY, HttpContentValue.ProvisionResponse.music_category_rawdata, false);
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.LAUNCHER_PROFILE, HttpContentValue.ProvisionResponse.launcher_profile_rawdata, false);
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.PROJECT_ID, HttpContentValue.ProvisionResponse.project_id, true);

            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.IS_NETWORK_ETH_WIFI_AUTO_SWITCH,
                    HttpContentValue.ProvisionResponse.is_network_eth_wifi_auto_switch_enable, ()->DeviceControlUnit.eth_wifi_auto_switch(HttpContentValue.ProvisionResponse.is_network_eth_wifi_auto_switch_enable));
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.NETWORK_QUALITY_INFO, HttpContentValue.ProvisionResponse.network_quality_info_rawdata, false);

            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.IS_SAVE_LOG_ENABLE, HttpContentValue.ProvisionResponse.is_save_log_enable);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.SAVE_LOG_SIZE_MB, HttpContentValue.ProvisionResponse.save_log_size_mb);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.LOG_UPLOAD_URL, HttpContentValue.ProvisionResponse.log_upload_url);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.AUTO_LOG, HttpContentValue.ProvisionResponse.auto_log);
            DeviceControlUnit.update_logcat_param() ;

            process_device_operation();
        }
    }
    private static void process_fwinfo_data()
    {
        if ( HttpContentValue.FwInfo.fwinfo_response_rawdata != null )
        {
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.MQTT_OTA_TOPIC_GROUP_NAME,
                    HttpContentValue.ProvisionResponse.mqtt_ota_topic_group_name,
                    ()->DeviceControlUnit.resubscribe_mqtt(new int[]{MqttHandler.MqttManager.MQTT_TOPIC_TYPE_OTA_GROUP, MqttHandler.MqttManager.MQTT_TOPIC_TYPE_OTA_GROUP_FWVER}));

            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.FILE_SIZE, HttpContentValue.FwInfo.file_size);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.FW_VERSION, HttpContentValue.FwInfo.fw_version);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.HIGH_BW_URL, HttpContentValue.FwInfo.FW_DOWNLOAD_PATH.high_bw_url);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.LOW_BW_URL, HttpContentValue.FwInfo.FW_DOWNLOAD_PATH.low_bw_url);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.MD5_CHECKSUM, HttpContentValue.FwInfo.md5_checksum);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.MODEL_CHECKER, HttpContentValue.FwInfo.model_checker);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.RELEASE_NOTES, HttpContentValue.FwInfo.release_notes);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.DRIVEN_BY, HttpContentValue.FwInfo.update_mode);

            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.OTA_AFTER_BOOT, HttpContentValue.FwInfo.is_ota_after_boot);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.OTA_AFTER_IMMEDIATELY, HttpContentValue.FwInfo.is_ota_immediately);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.OTA_AFTER_STANDBY, HttpContentValue.FwInfo.is_ota_in_standby);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.OTA_AFTER_TIME, HttpContentValue.FwInfo.is_ota_in_time_window);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.STATUS, HttpContentValue.FwInfo.http_status);

            ACSService.notify_acsservice(CommonDefine.ACS_OTA_UPDATE_PARAM, null, 0, 0);
        }
    }

    private static void process_appinfo_data()
    {
        if ( HttpContentValue.AppInfo.apps_info_rawdata != null )
        {
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.MQTT_APP_TOPIC_GROUP_NAME,
                    HttpContentValue.ProvisionResponse.mqtt_app_topic_group_name,
                    ()->DeviceControlUnit.resubscribe_mqtt(new int[]{MqttHandler.MqttManager.MQTT_TOPIC_TYPE_APP_GROUP, MqttHandler.MqttManager.MQTT_TOPIC_TYPE_APP_GROUP_FWVER}));

            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.AppInfo.APP_PATH, HttpContentValue.AppInfo.app_download_url_prefix);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.AppInfo.APPS_INFO, HttpContentValue.AppInfo.apps_info_rawdata);

            ArrayList<String> already_installed_apk_list = null;
            try {
                already_installed_apk_list = CommonFunctionUnit.json_to_arraylist(CommonFunctionUnit.get_acs_provider_data(ACSService.g_context, ACSServerJsonFenceName.AppInfo.ALREADY_INSTALLED_APK_LIST));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            ArrayList<String> install_package_list = new ArrayList<>();
            ArrayList<String> download_file_list = new ArrayList<>();
            ArrayList<String> uninstall_package_list = new ArrayList<>();
            for( HttpContentValue.AppInfo.Apps_info app_info : HttpContentValue.AppInfo.apps_info_list )
            {
                if ( app_info.is_install == CommonDefine.UNINSTALL ) {
                    uninstall_package_list.add(app_info.package_name);
                }
                else if ( app_info.is_install == CommonDefine.INSTALL_ALWAYS )
                {
                    install_package_list.add(app_info.package_name);
                    download_file_list.add(HttpContentValue.AppInfo.app_download_url_prefix + "/" + app_info.apk_file_name);
                }
                else if ( app_info.is_install == CommonDefine.INSTALL_ONCE )
                {
                    if ( already_installed_apk_list.size() <= 0 || (! already_installed_apk_list.contains(app_info.package_name)) )
                    {
                        install_package_list.add(app_info.package_name);
                        download_file_list.add(HttpContentValue.AppInfo.app_download_url_prefix + "/" + app_info.apk_file_name);
                    }
                }
            }

            DeviceControlUnit.initial_app_download_manager();
            if ( install_package_list.size() > 0 ) {
                DeviceControlUnit.install_apk(install_package_list, download_file_list);
                CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.AppInfo.ALREADY_INSTALLED_APK_LIST, CommonFunctionUnit.arraylist_to_json(install_package_list));
            }
            if ( uninstall_package_list.size() > 0 )
                DeviceControlUnit.uninstall_apk( uninstall_package_list );
        }
    }

    private static void process_launcherinfo_data()
    {
        if ( HttpContentValue.LauncherInfo.launcher_info_rawdata != null )
        {
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.LauncherInfo.APP_PROFILE_ID, HttpContentValue.LauncherInfo.app_profile_id);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.LauncherInfo.RECOMMEND_PROFILE_ID, HttpContentValue.LauncherInfo.recommendation_profile_id);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.LauncherInfo.HOTVIDEO_PROFILE_ID, HttpContentValue.LauncherInfo.hot_video_profile_id);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.LauncherInfo.LED_STYLE, HttpContentValue.LauncherInfo.led_style);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.LauncherInfo.MODEL_NAME, HttpContentValue.LauncherInfo.model_name);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.LauncherInfo.ANDROID_VERSION, HttpContentValue.LauncherInfo.android_version);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.LauncherInfo.OTA_STARTED_TIME, HttpContentValue.LauncherInfo.ota_started_time);

            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.OTA_AFTER_BOOT, HttpContentValue.FwInfo.is_ota_after_boot);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.OTA_AFTER_IMMEDIATELY, HttpContentValue.FwInfo.is_ota_immediately);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.OTA_AFTER_STANDBY, HttpContentValue.FwInfo.is_ota_in_standby);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.OTA_AFTER_TIME, HttpContentValue.FwInfo.is_ota_in_time_window);
            ACSService.notify_acsservice(CommonDefine.ACS_OTA_UPDATE_PARAM, null, 0, 0);


            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.IS_NETWORK_ETH_WIFI_AUTO_SWITCH,
                    HttpContentValue.ProvisionResponse.is_network_eth_wifi_auto_switch_enable, ()->DeviceControlUnit.eth_wifi_auto_switch(HttpContentValue.ProvisionResponse.is_network_eth_wifi_auto_switch_enable));

            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.IS_SAVE_LOG_ENABLE, HttpContentValue.ProvisionResponse.is_save_log_enable);
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.SAVE_LOG_SIZE_MB, HttpContentValue.ProvisionResponse.save_log_size_mb);
            DeviceControlUnit.update_logcat_param() ;


            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.PROJECT_NAME,
                    HttpContentValue.ProvisionResponse.project_name,
                    ()->DeviceControlUnit.resubscribe_mqtt(new int[]{MqttHandler.MqttManager.MQTT_TOPIC_TYPE_TBC_DEVICE}));
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.MQTT_OTA_TOPIC_GROUP_NAME,
                    HttpContentValue.ProvisionResponse.mqtt_ota_topic_group_name,
                    ()->DeviceControlUnit.resubscribe_mqtt(new int[]{MqttHandler.MqttManager.MQTT_TOPIC_TYPE_OTA_GROUP, MqttHandler.MqttManager.MQTT_TOPIC_TYPE_OTA_GROUP_FWVER}));
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.MQTT_APP_TOPIC_GROUP_NAME,
                    HttpContentValue.ProvisionResponse.mqtt_app_topic_group_name,
                    ()->DeviceControlUnit.resubscribe_mqtt(new int[]{MqttHandler.MqttManager.MQTT_TOPIC_TYPE_APP_GROUP, MqttHandler.MqttManager.MQTT_TOPIC_TYPE_APP_GROUP_FWVER}));


            CommonFunctionUnit.update_data_without_notify_launcher( ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.HDMI_OUT_STATUS,
                    HttpContentValue.ProvisionResponse.DeviceSettings.is_hdmi_output,
                    ()->DeviceControlUnit.hdmi_output_control(HttpContentValue.ProvisionResponse.DeviceSettings.is_hdmi_output));
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.LAUNCHER_PROFILE, HttpContentValue.ProvisionResponse.launcher_profile_rawdata, false);
        }
    }

    private static void mqtt_publish_box_status()
    {
        JSONObject box_status = CollectBoxStatus(ACSService.g_context) ;
        ACSService.notify_acsservice( CommonDefine.ACS_MQTT_SERVER_PUBLISH_CASEID, box_status, MQTT_PUSLISH_BOX_STATUS, 0 ) ;
    }

    private static void process_ad_list_data()
    {
        if ( HttpContentValue.AdList.ad_list_rawdata != null )
        {
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.AdList.AD_LIST, HttpContentValue.AdList.ad_list_rawdata, true);
        }
    }

    private static void process_music_ad_data()
    {
        if ( HttpContentValue.MusicAD.music_ad_rawdata != null )
        {
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.MusicAD.MusicAD, HttpContentValue.MusicAD.music_ad_rawdata, true);
        }
    }

    private static void process_week_highlight_data()
    {
        if ( HttpContentValue.WeekHightLight.week_highlight_rawdata != null )
        {
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.WeekHightLight.WEEK_HIGHLIGHT, HttpContentValue.WeekHightLight.week_highlight_rawdata, true);
        }
    }

    private static void process_rank_list_data()
    {
        if ( HttpContentValue.RankList.rank_list_rawdata != null )
        {
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.RankList.RANK_LIST, HttpContentValue.RankList.rank_list_rawdata, true);
        }
    }

    private static void process_hot_video_data()
    {
        if ( HttpContentValue.HotVideo.hot_video_rawdata != null )
        {
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.HotVideo.HOT_VIDEO, HttpContentValue.HotVideo.hot_video_rawdata, true);
        }
    }

    private static void process_recommend_packages()
    {
        if ( HttpContentValue.ProvisionResponse.storage_server != null )
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.STORAGE_SERVER, HttpContentValue.ProvisionResponse.storage_server, true);
    }

    private static void process_recommend_packages_json_data()
    {
        if ( HttpContentValue.RecommendPackages.recommend_packages_json_data != null )
        {
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.RecommendPackages.RECOMMEND_PACKAGES, HttpContentValue.RecommendPackages.recommend_packages_json_data, true);
        }
    }

    private static void process_mqtt_data_ota_group_change()
    {
        if ( MqttContentValue.ota_group_change_rawdata != null )
        {
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttData.OTA_GROUP_CHANGE, MqttContentValue.ota_group_change_rawdata.toString());
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.IS_SAVE_LOG_ENABLE, HttpContentValue.ProvisionResponse.is_save_log_enable);
            DeviceControlUnit.update_logcat_param() ;
            process_device_operation() ;
            process_fwinfo_data() ;
        }
    }

    private static void process_mqtt_data_app_group_change()
    {
        if ( MqttContentValue.app_group_change_rawdata != null )
        {
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttData.APP_GROUP_CHANGE, MqttContentValue.app_group_change_rawdata.toString());
            process_appinfo_data() ;
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.LAUNCHER_PROFILE, HttpContentValue.ProvisionResponse.launcher_profile_rawdata, false);
            process_week_highlight_data() ;
            process_hot_video_data();
            process_ad_list_data();
            process_music_ad_data();
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.MQTT_TV_MAIL_TOPIC,
                    HttpContentValue.ProvisionResponse.mqtt_tv_mail_topic, ()->DeviceControlUnit.resubscribe_mqtt(new int[]{MqttHandler.MqttManager.MQTT_TOPIC_TYPE_TVMAIL}));
        }
    }

    public static void returnReceivedTvMail(JSONArray tv_mail_rawdata)
    {
        JSONObject receivedTvMail = new JSONObject();
        try {
            JSONArray receivedTvMailIds = new JSONArray();
            for( int i = 0 ; i < tv_mail_rawdata.length() ; i++ )
            {
                try {
                    JSONObject jsonObject = tv_mail_rawdata.getJSONObject(i);
                    int mail_id = CommonFunctionUnit.get_json_int(jsonObject, "id", CommonDefine.DEFAULT_INT_VALUE) ;
                    if ( ! receivedTvMailIds.toString().contains(String.valueOf(mail_id)) )
                        receivedTvMailIds.put(mail_id) ;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            receivedTvMail.put(ACSServerJsonFenceName.ProvisionDeviceInfo.ETH_MAC_ADDR, HttpContentValue.ProvisionDeviceInfo.eth_mac_address);
            receivedTvMail.put("tvmailAck", receivedTvMailIds);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        ACSService.notify_acsservice( CommonDefine.ACS_MQTT_SERVER_PUBLISH_CASEID, receivedTvMail, 0, 0 ) ;
    }

    private static void process_mqtt_tv_mail()
    {
        if ( MqttContentValue.tv_mail_rawdata != null )
        {
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.MqttData.TV_MAIL, MqttContentValue.tv_mail_rawdata, true);
            returnReceivedTvMail(MqttContentValue.tv_mail_rawdata);
        }
    }
    public static void process_all_data(int data_type) {
        switch (data_type)
        {
            case HTTP_PROCESS_PROVISION_DATA:
            {
                process_provision_data();
            }break;
            case HTTP_PROCESS_FWINFO_DATA:
            {
                process_fwinfo_data();
            }break;
            case HTTP_PROCESS_APPINFO_DATA:
            {
                process_appinfo_data();
            }break;
            case HTTP_PROCESS_LAUNCHERINFO_DATA:
            {
                process_launcherinfo_data();
            }break;
            case HTTP_PROCESS_AD_LIST_DATA:
            {
                process_ad_list_data();
            }break;
            case HTTP_PROCESS_WEEK_HIGHLIGHT_DATA:
            {
                process_week_highlight_data();
            }break;
            case HTTP_PROCESS_RANK_LIST_DATA:
            {
                process_rank_list_data();
            }break;
            case HTTP_PROCESS_HOT_VIDEO_DATA:
            {
                process_hot_video_data();
            }break;
            case HTTP_PROCESS_RECOMMEND_PACKAGES:
            {
                process_recommend_packages() ;
            }break;
            case HTTP_PROCESS_RECOMMEND_PACKAGES_JSON_DATA:
            {
                process_recommend_packages_json_data() ;
            }break;
            case MQTT_DATA_OTA_GROUP_CHANGE:
            {
                process_mqtt_data_ota_group_change() ;
            }break;
            case MQTT_DATA_APP_GROUP_CHANGE:
            {
                process_mqtt_data_app_group_change() ;
            }break;
            case MQTT_DATA_TV_MAIL:
            {
                process_mqtt_tv_mail();
            }break;
            case MQTT_PUSLISH_BOX_STATUS:
            {
                mqtt_publish_box_status();
            }break;
            case MQTT_PUSLISH_SIGNAL_DETECT:
            {
                JSONObject signal_detect = process_signal_detect(ACSService.g_context) ;
                ACSService.notify_acsservice( CommonDefine.ACS_MQTT_SERVER_PUBLISH_CASEID, signal_detect, 0, 0 ) ;
            }break;
            case MQTT_PUSLISH_INSTANT_MONITORING:
            {
                JSONObject instant_monitoring = process_instant_monitoring(ACSService.g_context) ;
                ACSService.notify_acsservice( CommonDefine.ACS_MQTT_SERVER_PUBLISH_CASEID, instant_monitoring, 0, 0 ) ;
            }break;
            case MQTT_PUSLISH_HDMI_INFO:
            {
                JSONObject hdmi_info = process_hdmi_info(ACSService.g_context) ;
                ACSService.notify_acsservice( CommonDefine.ACS_MQTT_SERVER_PUBLISH_CASEID, hdmi_info, 0, 0 ) ;
            }break;
            case MQTT_DATA_COMMON_DEVICE_INFO:
            {
                process_device_operation() ;
            }break;
        }
    }

    private static JSONObject process_signal_detect(Context context) {
        JSONObject signal_detect = new JSONObject();
        try {
            JSONObject signal_config_json = new JSONObject();
            String signal_result_str = CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.MqttCommand.SIGNAL_DETECT_RESULT);
            signal_config_json.put("SignalDetect", signal_result_str);

            signal_detect.put(ACSServerJsonFenceName.ProvisionDeviceInfo.ETH_MAC_ADDR, HttpContentValue.ProvisionDeviceInfo.eth_mac_address);
            signal_detect.put("config", signal_config_json);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return signal_detect ;
    }

    private static JSONObject process_instant_monitoring(Context context)
    {
        JSONObject instant_monitoring = new JSONObject();
        try {
            JSONObject instant_monitoring_config_json = new JSONObject();
            instant_monitoring_config_json.put(ACSServerJsonFenceName.MqttCommand.CPU_TEMP, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.MqttCommand.CPU_TEMP));
            instant_monitoring_config_json.put(ACSServerJsonFenceName.MqttCommand.CPU_UTILITY, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.MqttCommand.CPU_UTILITY));
            instant_monitoring_config_json.put(ACSServerJsonFenceName.MqttCommand.MEM_TOTAL, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.MqttCommand.MEM_TOTAL));
            instant_monitoring_config_json.put(ACSServerJsonFenceName.MqttCommand.MEM_USED, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.MqttCommand.MEM_USED));
            instant_monitoring_config_json.put(ACSServerJsonFenceName.MqttCommand.MEM_FREE, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.MqttCommand.MEM_FREE));

            instant_monitoring.put(ACSServerJsonFenceName.ProvisionDeviceInfo.ETH_MAC_ADDR, HttpContentValue.ProvisionDeviceInfo.eth_mac_address);
            instant_monitoring.put("config", instant_monitoring_config_json);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return instant_monitoring ;
    }

    private static JSONObject process_hdmi_info(Context context)
    {
        JSONObject hdmi_info = new JSONObject();
        try {
            JSONObject hdmi_info_status_json = new JSONObject();
            {
                JSONObject hdmi_info_out_json = new JSONObject();
                hdmi_info_out_json.put(ACSServerJsonFenceName.MqttCommand.HDCP_INFO, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.MqttCommand.HDCP_INFO));
                hdmi_info_out_json.put(ACSServerJsonFenceName.MqttCommand.DISP_CAP, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.MqttCommand.DISP_CAP));
                hdmi_info_out_json.put(ACSServerJsonFenceName.MqttCommand.DISP_MODE, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.MqttCommand.DISP_MODE));
                hdmi_info_out_json.put(ACSServerJsonFenceName.MqttCommand.AUD_CAP, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.MqttCommand.AUD_CAP));
                hdmi_info_out_json.put(ACSServerJsonFenceName.MqttCommand.EDID, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.MqttCommand.EDID));
                hdmi_info_out_json.put(ACSServerJsonFenceName.MqttCommand.EDID_RAW_DATA, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.MqttCommand.EDID_RAW_DATA));
                hdmi_info_out_json.put(ACSServerJsonFenceName.MqttCommand.HDCP_USER_SETTING, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.MqttCommand.HDCP_USER_SETTING));
                hdmi_info_out_json.put(ACSServerJsonFenceName.BoxStatus.SOUND_TYPE, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.SOUND_TYPE));
                hdmi_info_status_json.put("hdmi_out", hdmi_info_out_json);
            }

            hdmi_info.put(ACSServerJsonFenceName.ProvisionDeviceInfo.ETH_MAC_ADDR, HttpContentValue.ProvisionDeviceInfo.eth_mac_address);
            hdmi_info.put("status", hdmi_info_status_json);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return hdmi_info ;
    }

    private static JSONObject CollectBoxStatus(Context context) {

        DeviceControlUnit.update_stb_hwinfo();
        JSONObject box_status = new JSONObject();
        try {
            box_status.put(ACSServerJsonFenceName.ProvisionDeviceInfo.ETH_MAC_ADDR, HttpContentValue.ProvisionDeviceInfo.eth_mac_address);
            JSONObject box_config = new JSONObject();
            box_config.put(ACSServerJsonFenceName.BoxStatus.NETWORK_INTERFACE, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.NETWORK_INTERFACE));
            box_config.put(ACSServerJsonFenceName.BoxStatus.STORAGE_SPACE, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.STORAGE_SPACE));
            box_config.put(ACSServerJsonFenceName.BoxStatus.LANG, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.LANG));
            box_config.put(ACSServerJsonFenceName.BoxStatus.SOUND_TYPE, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.SOUND_TYPE));
            box_config.put(ACSServerJsonFenceName.BoxStatus.SCREEN_RESOLUTION, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.SCREEN_RESOLUTION));
            box_config.put(ACSServerJsonFenceName.BoxStatus.TV_MODEL, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.TV_MODEL));
            HdmiControlManager hdmiControlManager = context.getSystemService(HdmiControlManager.class) ;
            int hdmi_cec_enable = hdmiControlManager.getHdmiCecEnabled() ;
            String hdmi_control_enabled = hdmi_cec_enable == 1 ? "ON" : "OFF";
            box_config.put(ACSServerJsonFenceName.BoxStatus.HDMI_CEC, hdmi_control_enabled) ;

            box_config.put(ACSServerJsonFenceName.ProvisionResponse.IS_SAVE_LOG_ENABLE, HttpContentValue.ProvisionResponse.is_save_log_enable) ;
            String setupwizard_finish_time = CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.SetupWizard.FINISH_TIME);
            if ( setupwizard_finish_time != null )
            {
                box_config.put(ACSServerJsonFenceName.SetupWizard.BOX_STATUS_UPLOAD_SETUPWIZARD_FINISH, "true");
            }
            else
            {
                box_config.put(ACSServerJsonFenceName.SetupWizard.BOX_STATUS_UPLOAD_SETUPWIZARD_FINISH, "false");
            }
            box_config.put(ACSServerJsonFenceName.SetupWizard.BOX_STATUS_UPLOAD_SETUPWIZARD_FINISH_TIME, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.SetupWizard.FINISH_TIME));

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            box_config.put(ACSServerJsonFenceName.BoxStatus.INTERACTIVE_MODE, String.valueOf(powerManager.isInteractive()));
            box_config.put(ACSServerJsonFenceName.BoxStatus.INTERACTIVE_CHANGE_TIME, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.INTERACTIVE_CHANGE_TIME)) ;

            box_config.put(ACSServerJsonFenceName.BoxStatus.WIFI_BSSID, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.WIFI_BSSID)) ;
            box_config.put(ACSServerJsonFenceName.BoxStatus.WIFI_SSID, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.WIFI_SSID)) ;
            box_config.put(ACSServerJsonFenceName.BoxStatus.WIFI_FREQ, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.WIFI_FREQ)) ;
            box_config.put(ACSServerJsonFenceName.BoxStatus.WIFI_RSSI, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.WIFI_RSSI)) ;

            box_config.put(ACSServerJsonFenceName.LauncherInfo.LED_STYLE, HttpContentValue.LauncherInfo.led_style);
            String server_type = SystemProperties.get(CommonDefine.ACS_SERVER_TYPE_PROPERTY) ;
            if ( server_type != null && server_type.equalsIgnoreCase(CommonDefine.ACS_SERVER_TYPE_Lab) )
            {
                server_type = CommonDefine.ACS_SERVER_TYPE_Lab ;
            }
            else
            {
                server_type = CommonDefine.ACS_SERVER_TYPE_PRODUCTION ;
            }
            box_config.put(ACSServerJsonFenceName.BoxStatus.SERVER_TYPE, server_type) ;

            box_config.put(ACSServerJsonFenceName.ProvisionResponse.HDMI_OUTPUT_CONTROL, "Deprecated") ;

            String adb_enabled = Settings.Global.getInt(context.getContentResolver(), "adb_enabled", 0) == 0 ? "false" : "true";
            box_config.put(ACSServerJsonFenceName.BoxStatus.ADB_ENABLED, adb_enabled) ;

            {
                String btarray_str = CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.BT_DEVICES);
                JSONArray bt_devices;
                if (btarray_str != null)
                    bt_devices = new JSONArray(btarray_str);
                else
                    bt_devices = new JSONArray();
                box_config.put(ACSServerJsonFenceName.BoxStatus.BT_DEVICES, bt_devices);
            }

            box_config.put(ACSServerJsonFenceName.BoxStatus.BT_LOW_POWER_CONFIRM, "Deprecated") ;

            {
                String usbarray_str = CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.USB_DEVICES);
                JSONArray usb_devices;
                if (usbarray_str != null)
                    usb_devices = new JSONArray(usbarray_str);
                else
                    usb_devices = new JSONArray();
                box_config.put(ACSServerJsonFenceName.BoxStatus.USB_STATUS, (usb_devices.length() > 0?"true":"false")) ;
                box_config.put(ACSServerJsonFenceName.BoxStatus.USB_DEVICES, usb_devices.toString());
            }

            box_config.put(ACSServerJsonFenceName.BoxStatus.PUBLIC_IP, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.PUBLIC_IP));
            box_config.put(ACSServerJsonFenceName.BoxStatus.RMSCLIENT, "Deprecated");


//            JSONArray jSONArray32 = new JSONArray();
//            Log.i("ACSService", "[DVRRecord] DVRRecord : " + providerData36);
//            if (providerData36 != null) {
//            }
//            Log.i("ACSService", "[MQTT] DVR Record 1 :" + jSONArray32);
//            jSONObject3.put("DVRRecord", jSONArray32);
			GposDatabaseTable g_pos_info = new GposDatabaseTable();
            String current_user_pin_code = g_pos_info.getKeyValue(context, GposDefine.GPOS_WORKER_PASSWORD_VALUE) ;
            box_config.put(ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.PIN_CODE,
                    (current_user_pin_code==null?HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.pin_code:current_user_pin_code) );


            box_config.put(ACSServerJsonFenceName.BoxStatus.SERIAL_NUMBER, HttpContentValue.ProvisionDeviceInfo.serial_number);
            box_config.put(ACSServerJsonFenceName.BoxStatus.ANDROID_VERSION, HttpContentValue.ProvisionDeviceInfo.android_version);
            box_config.put(ACSServerJsonFenceName.ProvisionDeviceInfo.FW_VERSION, HttpContentValue.ProvisionDeviceInfo.fw_version);
            box_config.put(ACSServerJsonFenceName.ProvisionDeviceInfo.WIFI_MAC_ADDR, HttpContentValue.ProvisionDeviceInfo.wifi_mac);


            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(BroadcastDefine.LAUNCHER_PACKAGE_NAME, 0);
            String versionName = packageInfo.versionName;
            box_config.put(ACSServerJsonFenceName.BoxStatus.LAUNCHER_VERSION_NAME, versionName);


            box_config.put(ACSServerJsonFenceName.BoxStatus.NETWORK_DNS, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.NETWORK_DNS));
            box_config.put(ACSServerJsonFenceName.BoxStatus.NETWORK_BOOTPROTO, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.NETWORK_BOOTPROTO));
            box_config.put(ACSServerJsonFenceName.BoxStatus.NETWORK_SPEED_RESULT, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.NETWORK_SPEED_RESULT));

            String agree_eula_content = CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.SetupWizard.AGREE_EULA_CONTENT) ;
            if ( agree_eula_content != null )
            {
                box_config.put(ACSServerJsonFenceName.SetupWizard.AGREE_EULA_VERSION, agree_eula_content);
            }

			String box_si_nit = g_pos_info.getKeyValue(context, GposDefine.GPOS_SI_NIT_ID) ;
            box_config.put(ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.BOX_SI_NIT, box_si_nit);
            box_config.put(ACSServerJsonFenceName.BoxStatus.BT_MAC, CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.BT_MAC));

            if (box_config.length() > 0) {
                box_status.put("config", box_config);
            }
        } catch (Exception e5) {
            e5.printStackTrace();
            return null;
        }
        return box_status ;
    }
}
