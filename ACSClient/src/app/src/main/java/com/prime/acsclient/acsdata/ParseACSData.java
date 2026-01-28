package com.prime.acsclient.acsdata;

import android.util.Log;

import com.prime.acsclient.ACSService;
import com.prime.acsclient.common.CommonDefine;
import com.prime.acsclient.common.CommonFunctionUnit;
import com.prime.acsclient.common.MqttCommand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseACSData {
    private final static String TAG = "ACS_ParseACSData" ;
    private final static boolean DBG_LOG = false ;
    public static int parse_provision_response( JSONObject provision_response )
    {
        int result = CommonDefine.SUCCESS ;
        if ( provision_response == null ) {
            Log.d( TAG, "provision_response null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.ProvisionResponse.provision_response_rawdata = provision_response ;
        HttpContentValue.ProvisionResponse.mqtt_client_id = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_CLIENT_ID, HttpContentValue.ProvisionResponse.mqtt_client_id );
        HttpContentValue.ProvisionResponse.mqtt_user_name = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_USER_NAME, HttpContentValue.ProvisionResponse.mqtt_user_name);
        HttpContentValue.ProvisionResponse.mqtt_server_ip = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_SERVER_IP, HttpContentValue.ProvisionResponse.mqtt_server_ip);
        HttpContentValue.ProvisionResponse.mqtt_server_port = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_SERVER_PORT, HttpContentValue.ProvisionResponse.mqtt_server_port);
        HttpContentValue.ProvisionResponse.mqtt_tls_server_port = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_TLS_SERVER_PORT, HttpContentValue.ProvisionResponse.mqtt_tls_server_port);
        HttpContentValue.ProvisionResponse.mqtt_qos = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_QOS, HttpContentValue.ProvisionResponse.mqtt_qos);
        HttpContentValue.ProvisionResponse.mqtt_subscribe_topics = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_SUBSCRIBE_TOPICS, HttpContentValue.ProvisionResponse.mqtt_subscribe_topics);
        HttpContentValue.ProvisionResponse.mqtt_publish_topics = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_PUBLISH_TOPICS, HttpContentValue.ProvisionResponse.mqtt_publish_topics);
        HttpContentValue.ProvisionResponse.http_fwinfo_url = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.HTTP_FWINFO_URL, CommonFunctionUnit.combinServerUrl(CommonDefine.ACS_HTTP_POST_FWINFO_TOKEN));
        HttpContentValue.ProvisionResponse.http_appinfo_url = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.HTTP_APPINFO_URL, CommonFunctionUnit.combinServerUrl(CommonDefine.ACS_HTTP_POST_APPINFO_TOKEN));

        HttpContentValue.ProvisionResponse.http_launchernfo_url = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.HTTP_LAUNCHERINFO_URL, CommonFunctionUnit.combinServerUrl(CommonDefine.ACS_HTTP_POST_LAUNCHERINFO_TOKEN));
        HttpContentValue.ProvisionResponse.ota_check_interval = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.OTA_CHECK_INTERVAL, HttpContentValue.ProvisionResponse.ota_check_interval);
        HttpContentValue.ProvisionResponse.fwinfo_check_interval = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.FWINFO_CHECK_INTERVAL, HttpContentValue.ProvisionResponse.fwinfo_check_interval);
        HttpContentValue.ProvisionResponse.fwinfo_check_daily_interval = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.FWINFO_CHECK_DAILY_INTERVAL, HttpContentValue.ProvisionResponse.fwinfo_check_daily_interval);
        HttpContentValue.ProvisionResponse.mqtt_ota_topic_group_name = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_OTA_TOPIC_GROUP_NAME, HttpContentValue.ProvisionResponse.mqtt_ota_topic_group_name);
        HttpContentValue.ProvisionResponse.appinfo_check_interval = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.APPINFO_CHECK_INTERVAL, HttpContentValue.ProvisionResponse.appinfo_check_interval);
        HttpContentValue.ProvisionResponse.appinfo_check_daily_interval = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.APPINFO_CHECK_DAILY_INTERVAL, HttpContentValue.ProvisionResponse.appinfo_check_daily_interval);
        HttpContentValue.ProvisionResponse.launcherinfo_check_interval = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.LAUNCHERINFO_CHECK_INVERVAL, HttpContentValue.ProvisionResponse.launcherinfo_check_interval);
        HttpContentValue.ProvisionResponse.launcherinfo_check_daily_interval = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.LAUNCHERINFO_CHECK_DAILY_INTERVAL, HttpContentValue.ProvisionResponse.launcherinfo_check_daily_interval);
        HttpContentValue.ProvisionResponse.save_log_size_mb = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.SAVE_LOG_SIZE_MB, HttpContentValue.ProvisionResponse.save_log_size_mb);

        HttpContentValue.ProvisionResponse.is_save_log_enable = CommonFunctionUnit.get_json_boolean( provision_response, ACSServerJsonFenceName.ProvisionResponse.IS_SAVE_LOG_ENABLE, HttpContentValue.ProvisionResponse.is_save_log_enable);
        HttpContentValue.ProvisionResponse.hdmi_output_control = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.HDMI_OUTPUT_CONTROL, HttpContentValue.ProvisionResponse.hdmi_output_control);
        HttpContentValue.ProvisionResponse.launcher_profile_rawdata = CommonFunctionUnit.get_json_jsonarray( provision_response, ACSServerJsonFenceName.ProvisionResponse.LAUNCHER_PROFILE, HttpContentValue.ProvisionResponse.launcher_profile_rawdata);
        HttpContentValue.ProvisionResponse.log_upload_url = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.LOG_UPLOAD_URL, HttpContentValue.ProvisionResponse.log_upload_url);
        HttpContentValue.ProvisionResponse.ad_profile_rawdata = CommonFunctionUnit.get_json_jsonarray( provision_response, ACSServerJsonFenceName.ProvisionResponse.AD_PROFILE, HttpContentValue.ProvisionResponse.ad_profile_rawdata);
        HttpContentValue.ProvisionResponse.notify_center_api = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.NOTIFY_CENTER_API, HttpContentValue.ProvisionResponse.notify_center_api);
        HttpContentValue.ProvisionResponse.device_switchs_rawdata = CommonFunctionUnit.get_json_jsonobj( provision_response, ACSServerJsonFenceName.ProvisionResponse.DEVICE_SWITCHS, HttpContentValue.ProvisionResponse.device_switchs_rawdata);
        HttpContentValue.ProvisionResponse.project_name = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.PROJECT_NAME, HttpContentValue.ProvisionResponse.project_name);

        HttpContentValue.ProvisionResponse.project_id = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.PROJECT_ID, HttpContentValue.ProvisionResponse.project_id);
        HttpContentValue.ProvisionResponse.is_network_eth_wifi_auto_switch_enable = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.IS_NETWORK_ETH_WIFI_AUTO_SWITCH, HttpContentValue.ProvisionResponse.is_network_eth_wifi_auto_switch_enable);

        HttpContentValue.ProvisionResponse.mqtt_topic_info = CommonFunctionUnit.get_json_jsonarray( provision_response, ACSServerJsonFenceName.ProvisionResponse.TOPIC_INFO, HttpContentValue.ProvisionResponse.mqtt_topic_info );
        HttpContentValue.ProvisionResponse.mqtt_tv_mail_topic = CommonFunctionUnit.get_json_jsonarray( provision_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_TV_MAIL_TOPIC, HttpContentValue.ProvisionResponse.mqtt_tv_mail_topic );

        HttpContentValue.ProvisionResponse.mqtt_app_topic_group_name = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_APP_TOPIC_GROUP_NAME, HttpContentValue.ProvisionResponse.mqtt_app_topic_group_name);
        HttpContentValue.ProvisionResponse.eula_rawdata = CommonFunctionUnit.get_json_jsonarray( provision_response, ACSServerJsonFenceName.ProvisionResponse.EULA, HttpContentValue.ProvisionResponse.eula_rawdata);
        HttpContentValue.ProvisionResponse.music_category_rawdata = CommonFunctionUnit.get_json_jsonarray( provision_response, ACSServerJsonFenceName.ProvisionResponse.MUSIC_CATEGORY, HttpContentValue.ProvisionResponse.music_category_rawdata);
        HttpContentValue.ProvisionResponse.is_illegal_network = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.IS_ILLEGAL_NETWORK, HttpContentValue.ProvisionResponse.is_illegal_network);
        HttpContentValue.ProvisionResponse.storage_server = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.STORAGE_SERVER, HttpContentValue.ProvisionResponse.storage_server);
        HttpContentValue.ProvisionResponse.network_quality_info_rawdata = CommonFunctionUnit.get_json_jsonarray( provision_response, ACSServerJsonFenceName.ProvisionResponse.NETWORK_QUALITY_INFO, HttpContentValue.ProvisionResponse.network_quality_info_rawdata);
        HttpContentValue.ProvisionResponse.dvr_pair_hdd_info = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.DVR_PAIR_HDD_INFO, HttpContentValue.ProvisionResponse.dvr_pair_hdd_info);

        // new value in real server response
        HttpContentValue.ProvisionResponse.dlna = CommonFunctionUnit.get_json_boolean( provision_response, ACSServerJsonFenceName.ProvisionResponse.DLNA, HttpContentValue.ProvisionResponse.dlna);
        HttpContentValue.ProvisionResponse.miracast = CommonFunctionUnit.get_json_boolean( provision_response, ACSServerJsonFenceName.ProvisionResponse.MIRACAST, HttpContentValue.ProvisionResponse.miracast);
        HttpContentValue.ProvisionResponse.auto_log = CommonFunctionUnit.get_json_boolean( provision_response, ACSServerJsonFenceName.ProvisionResponse.AUTO_LOG, HttpContentValue.ProvisionResponse.auto_log);
        HttpContentValue.ProvisionResponse.bplus_trigger = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.BPLUS_TRIGGER, HttpContentValue.ProvisionResponse.bplus_trigger);
        HttpContentValue.ProvisionResponse.report_interval = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.REPORT_INTERVAL, HttpContentValue.ProvisionResponse.report_interval);
        HttpContentValue.ProvisionResponse.message_type = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.MESSAGE_TYPE, HttpContentValue.ProvisionResponse.message_type);
        HttpContentValue.ProvisionResponse.group_config_version = CommonFunctionUnit.get_json_int( provision_response, ACSServerJsonFenceName.ProvisionResponse.GROUP_CONFIG_VERSION, HttpContentValue.ProvisionResponse.group_config_version);
        HttpContentValue.ProvisionResponse.uba_white_list = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.UBA_WHITE_LIST, HttpContentValue.ProvisionResponse.uba_white_list);
        HttpContentValue.ProvisionResponse.uba_recommendation = CommonFunctionUnit.get_json_boolean( provision_response, ACSServerJsonFenceName.ProvisionResponse.UBA_RECOMMENDATION, HttpContentValue.ProvisionResponse.uba_recommendation);
        HttpContentValue.ProvisionResponse.uba_remote_button = CommonFunctionUnit.get_json_boolean( provision_response, ACSServerJsonFenceName.ProvisionResponse.UBA_REMOTE_BUTTON, HttpContentValue.ProvisionResponse.uba_remote_button);
        HttpContentValue.ProvisionResponse.uba_launch_point = CommonFunctionUnit.get_json_boolean( provision_response, ACSServerJsonFenceName.ProvisionResponse.UBA_LAUNCHER_POINT, HttpContentValue.ProvisionResponse.uba_launch_point);
        HttpContentValue.ProvisionResponse.uba_direction_key = CommonFunctionUnit.get_json_boolean( provision_response, ACSServerJsonFenceName.ProvisionResponse.UBA_DIRECTRY_KEY, HttpContentValue.ProvisionResponse.uba_direction_key);
        HttpContentValue.ProvisionResponse.uba_collect = CommonFunctionUnit.get_json_boolean( provision_response, ACSServerJsonFenceName.ProvisionResponse.USB_COLLECT, HttpContentValue.ProvisionResponse.uba_collect);
        HttpContentValue.ProvisionResponse.uba_upload_min = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.UBA_UPLOAD_MIN, HttpContentValue.ProvisionResponse.uba_upload_min);
        HttpContentValue.ProvisionResponse.uba_force_uploader_hour = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.UBA_FORCE_UPLOAD_HOUR, HttpContentValue.ProvisionResponse.uba_force_uploader_hour);
        HttpContentValue.ProvisionResponse.prompts = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.PROMOTS, HttpContentValue.ProvisionResponse.prompts);
        HttpContentValue.ProvisionResponse.eula_url = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.EULA_URL, HttpContentValue.ProvisionResponse.eula_url);
        HttpContentValue.ProvisionResponse.eula_version = CommonFunctionUnit.get_json_string( provision_response, ACSServerJsonFenceName.ProvisionResponse.EULA_VERSION, HttpContentValue.ProvisionResponse.eula_version);

//        CommonFunctionUnit.printClassFields(HttpContentValue.ProvisionResponse.class);
        result |= parse_eula(HttpContentValue.ProvisionResponse.eula_rawdata);
        result |= parse_common_device_info(provision_response);
        result |= parse_music_category(HttpContentValue.ProvisionResponse.music_category_rawdata) ;
        result |= parse_launcher_profile(HttpContentValue.ProvisionResponse.launcher_profile_rawdata) ;
        result |= parse_ad_profile(HttpContentValue.ProvisionResponse.ad_profile_rawdata) ;
        result |= parse_network_quality_info(HttpContentValue.ProvisionResponse.network_quality_info_rawdata) ;
        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    private static int parse_eula( JSONArray eula_array )
    {
        int result = CommonDefine.SUCCESS ;
        JSONObject eulaObject = null;
        if ( eula_array == null ) {
            Log.d( TAG, "eula_array null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.ProvisionResponse.eula_list = new ArrayList<>() ;
        for (int i = 0; i < eula_array.length(); i++) {
            try {
                eulaObject = eula_array.getJSONObject(i);
                HttpContentValue.ProvisionResponse.Eula eula_node = new HttpContentValue.ProvisionResponse.Eula() ;
                eula_node.name = CommonFunctionUnit.get_json_string(eulaObject, ACSServerJsonFenceName.ProvisionResponse.Eula.NAME, eula_node.name) ;
                eula_node.sort = CommonFunctionUnit.get_json_int(eulaObject, ACSServerJsonFenceName.ProvisionResponse.Eula.SORT,eula_node.sort ) ;
                eula_node.url = CommonFunctionUnit.get_json_string(eulaObject, ACSServerJsonFenceName.ProvisionResponse.Eula.URL,eula_node.url ) ;
                eula_node.version = CommonFunctionUnit.get_json_string(eulaObject, ACSServerJsonFenceName.ProvisionResponse.Eula.VERSION,eula_node.version ) ;
                HttpContentValue.ProvisionResponse.eula_list.add(eula_node);
            } catch (JSONException e) {
                e.printStackTrace();
                result = CommonDefine.FAIL ;
            }
        }

//        CommonFunctionUnit.printClassFields(HttpContentValue.ProvisionResponse.eula_list);
        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }
    private static int parse_music_category( JSONArray music_category_array )
    {
        int result = CommonDefine.SUCCESS ;
        JSONObject music_obj = null ;
        if ( music_category_array == null ) {
            Log.d( TAG, "music_category_array null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.ProvisionResponse.music_category_list = new ArrayList<>() ;
        for (int i = 0; i < music_category_array.length(); i++) {
            try {
                music_obj = music_category_array.getJSONObject(i);
                HttpContentValue.ProvisionResponse.MusicCategory music_category_node = new HttpContentValue.ProvisionResponse.MusicCategory() ;
                music_category_node.name = CommonFunctionUnit.get_json_string(music_obj, ACSServerJsonFenceName.ProvisionResponse.MusicCategory.NAME, music_category_node.name ) ;
                music_category_node.icon_url = CommonFunctionUnit.get_json_string(music_obj, ACSServerJsonFenceName.ProvisionResponse.MusicCategory.ICON, music_category_node.icon_url ) ;
                music_category_node.position_index = CommonFunctionUnit.get_json_int(music_obj, ACSServerJsonFenceName.ProvisionResponse.MusicCategory.SORT, music_category_node.position_index ) ;
                music_category_node.service_id_list = new ArrayList<>() ;
                JSONArray service_list_array = CommonFunctionUnit.get_json_jsonarray(music_obj, ACSServerJsonFenceName.ProvisionResponse.MusicCategory.SERVICE_LISTS, null ) ;
                for( int ii = 0 ; service_list_array != null && ii < service_list_array.length() ; ii++ )
                {
                    music_category_node.service_id_list.add(service_list_array.getString(ii));
                }
                HttpContentValue.ProvisionResponse.music_category_list.add(music_category_node);
            } catch (JSONException e) {
                e.printStackTrace();
                result = CommonDefine.FAIL ;
            }
        }

//        CommonFunctionUnit.printClassFields(HttpContentValue.ProvisionResponse.music_category_list);
        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }
    private static int parse_launcher_profile( JSONArray launcher_profile_array )
    {
        JSONObject launcheer_profile_obj = null ;
        int result = CommonDefine.SUCCESS ;
        if ( launcher_profile_array == null ) {
            Log.d( TAG, "launcher_profile_array null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.ProvisionResponse.launcher_profile_list = new ArrayList<>();
        for (int i = 0; i < launcher_profile_array.length(); i++) {
            try {
                launcheer_profile_obj = launcher_profile_array.getJSONObject(i);
                HttpContentValue.ProvisionResponse.LauncherProfile launcher_profile_node = new HttpContentValue.ProvisionResponse.LauncherProfile() ;
                launcher_profile_node.app_name = CommonFunctionUnit.get_json_string(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.APP_NAME, launcher_profile_node.app_name ) ;
                launcher_profile_node.package_name = CommonFunctionUnit.get_json_string(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.PACKAGE_NAME, launcher_profile_node.package_name ) ;
                launcher_profile_node.thumbnail_url = CommonFunctionUnit.get_json_string(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.THUMBNAIL, launcher_profile_node.thumbnail_url ) ;
                launcher_profile_node.icon_priority = CommonFunctionUnit.get_json_int(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.ICON_PRIORITY, launcher_profile_node.icon_priority ) ;
                launcher_profile_node.launch_activity_name = CommonFunctionUnit.get_json_string(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.ACTIVITY_NAME, launcher_profile_node.launch_activity_name ) ;
                launcher_profile_node.rcu_id = CommonFunctionUnit.get_json_string(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.PRODUCT_ID, launcher_profile_node.rcu_id ) ;
                launcher_profile_node.lunch_app_params = CommonFunctionUnit.get_json_string(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.PACKAGE_PARAMS, launcher_profile_node.lunch_app_params ) ;
                launcher_profile_node.app_short_description = CommonFunctionUnit.get_json_string(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.DESCRIPTION, launcher_profile_node.app_short_description ) ;
                launcher_profile_node.app_full_description = CommonFunctionUnit.get_json_string(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.FULL_TEXT, launcher_profile_node.app_full_description ) ;
                launcher_profile_node.app_download_url = CommonFunctionUnit.get_json_string(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.APP_PATH, launcher_profile_node.app_download_url ) ;
                launcher_profile_node.app_version_code = CommonFunctionUnit.get_json_string(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.APP_VERSION_CODE, launcher_profile_node.app_version_code ) ;
                launcher_profile_node.app_dispaly_label = CommonFunctionUnit.get_json_string(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.APK_LABEL, launcher_profile_node.app_dispaly_label ) ;
                launcher_profile_node.app_screen_captures = CommonFunctionUnit.get_json_string(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.SCREEN_CAPUTRES, launcher_profile_node.app_screen_captures ) ;
                launcher_profile_node.app_disaply_position = CommonFunctionUnit.get_json_int(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.DISPLAY_POSITION, launcher_profile_node.app_disaply_position ) ;
                launcher_profile_node.is_force_update = CommonFunctionUnit.get_json_boolean(launcheer_profile_obj, ACSServerJsonFenceName.ProvisionResponse.LauncherProfile.FORCE_UPDATE, launcher_profile_node.is_force_update ) ;
                HttpContentValue.ProvisionResponse.launcher_profile_list.add(launcher_profile_node);
            } catch (JSONException e) {
                e.printStackTrace();
                result = CommonDefine.FAIL ;
            }
        }

//        CommonFunctionUnit.printClassFields(HttpContentValue.ProvisionResponse.launcher_profile_list);
        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    private static int parse_ad_profile( JSONArray ad_profile_array )
    {
        JSONObject adObject = null;
        int result = CommonDefine.SUCCESS ;
        if ( ad_profile_array == null ) {
            Log.d( TAG, "ad_profile_array null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.ProvisionResponse.ad_profile_list = new ArrayList<>();
        for (int i = 0; i < ad_profile_array.length(); i++) {
            try {
                adObject = ad_profile_array.getJSONObject(i);
                HttpContentValue.ProvisionResponse.AdProfile ad_profile_node = new HttpContentValue.ProvisionResponse.AdProfile() ;
                ad_profile_node.url = CommonFunctionUnit.get_json_string(adObject, ACSServerJsonFenceName.ProvisionResponse.AdProfile.URL, ad_profile_node.url ) ;
                HttpContentValue.ProvisionResponse.ad_profile_list.add(ad_profile_node);
//                result |= process_ad( ad_url ) ;
            } catch (JSONException e) {
                e.printStackTrace();
                result = CommonDefine.FAIL ;
            }
        }

//        CommonFunctionUnit.printClassFields(HttpContentValue.ProvisionResponse.ad_profile_list);
        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    private static int parse_device_params( JSONObject device_params )
    {
        int result = CommonDefine.SUCCESS ;
        if ( device_params == null ) {
            Log.d( TAG, "device_params null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.home_id = CommonFunctionUnit.get_json_string( device_params, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.HOME_ID, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.home_id ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.pin_code = CommonFunctionUnit.get_json_string( device_params, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.PIN_CODE, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.pin_code ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.mobile = CommonFunctionUnit.get_json_string( device_params, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.MOBILE, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.mobile ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.member_id = CommonFunctionUnit.get_json_string( device_params, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.MEMBER_ID, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.member_id ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.pa_day = CommonFunctionUnit.get_json_int( device_params, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.PA_DAY, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.pa_day ) ;

        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.nit_id = CommonFunctionUnit.get_json_string( device_params, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.NIT_ID, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.nit_id ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.region_code = CommonFunctionUnit.get_json_string( device_params, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.ZIPCODE, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.region_code ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.operator_code = CommonFunctionUnit.get_json_string( device_params, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.SO, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.operator_code ) ;

        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.bat_id = CommonFunctionUnit.get_json_string( device_params, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.BAT_ID, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.bat_id ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.network_sync_time = CommonFunctionUnit.get_json_int( device_params, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.NETWORK_SYNC_TIME, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.network_sync_time ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.nit_params = CommonFunctionUnit.get_json_string( device_params, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.BOX_SI_NIT, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.nit_params ) ;

        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.force_screen_params = CommonFunctionUnit.get_json_string( device_params, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.FORCE_SCREEN_PARAMS, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.force_screen_params ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.system_alert_params = CommonFunctionUnit.get_json_string( device_params, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.SYSTEM_ALERT_PARAMS, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.system_alert_params ) ;

//        CommonFunctionUnit.printClassFields(HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.class);
        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    private static int parse_device_settings( JSONObject device_settings )
    {
        int result = CommonDefine.SUCCESS ;
        if ( device_settings == null ) {
            Log.d( TAG, "device_settings null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.ProvisionResponse.DeviceSettings.is_rms_upload = CommonFunctionUnit.get_json_int( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.RMS_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_rms_upload ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.is_show_tvmail = CommonFunctionUnit.get_json_int( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.TVMAIL_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_show_tvmail ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.is_hdmi_output = CommonFunctionUnit.get_json_int( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.HDMI_OUT_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_hdmi_output ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.is_dvr_enable = CommonFunctionUnit.get_json_int( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DVR_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_dvr_enable ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.is_standby_redirect = CommonFunctionUnit.get_json_int( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.STANDBY_REDIRECT_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_standby_redirect ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.dtv_status = CommonFunctionUnit.get_json_int( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DTV_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.dtv_status ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.is_ip_whitelist_enable = CommonFunctionUnit.get_json_int( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.IP_WHITELIST_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_ip_whitelist_enable ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.is_force_lock_screen = CommonFunctionUnit.get_json_int( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.FORCE_SCREEN_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_force_lock_screen ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.is_launcher_lock = CommonFunctionUnit.get_json_int( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.LAUNCHER_LOCK_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_launcher_lock ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.is_marquee_display = CommonFunctionUnit.get_json_int( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.TICKER_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_marquee_display ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.is_system_alert = CommonFunctionUnit.get_json_int( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.SYSTEM_ALERT_STATUS, HttpContentValue.ProvisionResponse.DeviceSettings.is_system_alert ) ;

        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.home_id = CommonFunctionUnit.get_json_string( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.HOME_ID, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.home_id ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.pin_code = CommonFunctionUnit.get_json_string( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.PIN_CODE, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.pin_code ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.mobile = CommonFunctionUnit.get_json_string( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.MOBILE, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.mobile ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.member_id = CommonFunctionUnit.get_json_string( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.MEMBER_ID, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.member_id ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.pa_day = CommonFunctionUnit.get_json_int( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.PA_DAY, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.pa_day ) ;

        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.nit_id = CommonFunctionUnit.get_json_string( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.NIT_ID, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.nit_id ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.region_code = CommonFunctionUnit.get_json_string( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.ZIPCODE, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.region_code ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.operator_code = CommonFunctionUnit.get_json_string( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.SO, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.operator_code ) ;

        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.bat_id = CommonFunctionUnit.get_json_string( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.BAT_ID, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.bat_id ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.network_sync_time = CommonFunctionUnit.get_json_int( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.NETWORK_SYNC_TIME, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.network_sync_time ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.nit_params = CommonFunctionUnit.get_json_string( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.BOX_SI_NIT, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.nit_params ) ;

        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.force_screen_params = CommonFunctionUnit.get_json_string( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.FORCE_SCREEN_PARAMS, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.force_screen_params ) ;
        HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.system_alert_params = CommonFunctionUnit.get_json_string( device_settings, ACSServerJsonFenceName.ProvisionResponse.DeviceSettings.DeviceParams.SYSTEM_ALERT_PARAMS, HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.system_alert_params ) ;

//        CommonFunctionUnit.printClassFields(HttpContentValue.ProvisionResponse.DeviceSettings.class);
        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    public static int parse_common_device_info( JSONObject device_info )
    {
        int result = CommonDefine.SUCCESS ;
        if ( device_info == null ) {
            Log.d( TAG, "device_info null" ) ;
            return CommonDefine.FAIL;
        }

        if ( device_info.has(ACSServerJsonFenceName.ProvisionResponse.DEVICE_SETTINGS) )
        {
            HttpContentValue.ProvisionResponse.device_settings_rawdata = CommonFunctionUnit.get_json_jsonobj( device_info, ACSServerJsonFenceName.ProvisionResponse.DEVICE_SETTINGS, HttpContentValue.ProvisionResponse.device_settings_rawdata);
            result |= parse_device_settings( HttpContentValue.ProvisionResponse.device_settings_rawdata ) ;
        }

        if ( device_info.has(ACSServerJsonFenceName.ProvisionResponse.DEVICE_PARAMS) )
        {
            HttpContentValue.ProvisionResponse.DeviceSettings.device_params_rawdata = CommonFunctionUnit.get_json_jsonobj(device_info, ACSServerJsonFenceName.ProvisionResponse.DEVICE_PARAMS, HttpContentValue.ProvisionResponse.DeviceSettings.device_params_rawdata);
            result |= parse_device_params(HttpContentValue.ProvisionResponse.DeviceSettings.device_params_rawdata);
        }

        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    private static int parse_network_quality_info( JSONArray network_quality_info_array )
    {
        int result = CommonDefine.SUCCESS ;
        JSONObject net_quality_obj = null ;
        if ( network_quality_info_array == null ) {
            Log.d( TAG, "network_quality_info_array null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.ProvisionResponse.network_quality_info_list = new ArrayList<>() ;
        for (int i = 0; i < network_quality_info_array.length(); i++) {
            try {
                net_quality_obj = network_quality_info_array.getJSONObject(i);
                HttpContentValue.ProvisionResponse.NetworkQualityInfo net_work_quality_node = new HttpContentValue.ProvisionResponse.NetworkQualityInfo() ;
                net_work_quality_node.eng_description = CommonFunctionUnit.get_json_string(net_quality_obj, ACSServerJsonFenceName.ProvisionResponse.NetworkQualityInfo.NAME, net_work_quality_node.eng_description ) ;
                net_work_quality_node.chinese_description = CommonFunctionUnit.get_json_string(net_quality_obj, ACSServerJsonFenceName.ProvisionResponse.NetworkQualityInfo.DESCRIPTION, net_work_quality_node.chinese_description ) ;
                net_work_quality_node.highest_speed = CommonFunctionUnit.get_json_int(net_quality_obj, ACSServerJsonFenceName.ProvisionResponse.NetworkQualityInfo.HIGHEST, net_work_quality_node.highest_speed ) ;
                net_work_quality_node.lowest_speed = CommonFunctionUnit.get_json_int(net_quality_obj, ACSServerJsonFenceName.ProvisionResponse.NetworkQualityInfo.LOWEST, net_work_quality_node.lowest_speed ) ;
                net_work_quality_node.index = CommonFunctionUnit.get_json_int(net_quality_obj, ACSServerJsonFenceName.ProvisionResponse.NetworkQualityInfo.ID, net_work_quality_node.index ) ;
                HttpContentValue.ProvisionResponse.network_quality_info_list.add(net_work_quality_node);
            } catch (JSONException e) {
                e.printStackTrace();
                result = CommonDefine.FAIL ;
            }
        }

//        CommonFunctionUnit.printClassFields(HttpContentValue.ProvisionResponse.network_quality_info_list);
        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

//    private static int process_ad( String ad_url )
//    {
//        if ( ad_url == null )
//            return CommonDefine.FAIL ;
//
//        Log.d( TAG, CommonFunctionUnit.getMethodName() ) ;
//        JSONObject responseJson = connect_to_http_server( ad_url ) ;
//        if ( responseJson != null )
//        {
//
//        }
//
//        return CommonDefine.FAIL ;
//    }
    public static int parse_fwinfo_response( JSONObject fwinfo_response )
    {
        int result = CommonDefine.SUCCESS ;
        if ( fwinfo_response == null ) {
            Log.d( TAG, "fwinfo_response null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.ProvisionResponse.fwinfo_check_interval = CommonFunctionUnit.get_json_int( fwinfo_response, ACSServerJsonFenceName.ProvisionResponse.FWINFO_CHECK_INTERVAL, HttpContentValue.ProvisionResponse.fwinfo_check_interval ) ;
        HttpContentValue.ProvisionResponse.fwinfo_check_daily_interval = CommonFunctionUnit.get_json_int( fwinfo_response, ACSServerJsonFenceName.ProvisionResponse.FWINFO_CHECK_DAILY_INTERVAL, HttpContentValue.ProvisionResponse.fwinfo_check_daily_interval ) ;
        HttpContentValue.ProvisionResponse.mqtt_ota_topic_group_name = CommonFunctionUnit.get_json_string( fwinfo_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_OTA_TOPIC_GROUP_NAME, HttpContentValue.ProvisionResponse.mqtt_ota_topic_group_name ) ;

        HttpContentValue.FwInfo.fwinfo_response_rawdata = fwinfo_response ;
        HttpContentValue.FwInfo.http_status = CommonFunctionUnit.get_json_int( fwinfo_response, ACSServerJsonFenceName.FwInfo.STATUS, HttpContentValue.FwInfo.http_status ) ;
        HttpContentValue.FwInfo.fw_version = CommonFunctionUnit.get_json_string( fwinfo_response, ACSServerJsonFenceName.FwInfo.FW_VERSION, HttpContentValue.FwInfo.fw_version ) ;
        HttpContentValue.FwInfo.release_notes = CommonFunctionUnit.get_json_string( fwinfo_response, ACSServerJsonFenceName.FwInfo.RELEASE_NOTES, HttpContentValue.FwInfo.release_notes ) ;
        HttpContentValue.FwInfo.fw_download_path_rawdata = CommonFunctionUnit.get_json_jsonobj( fwinfo_response, ACSServerJsonFenceName.FwInfo.FW_PATH, HttpContentValue.FwInfo.fw_download_path_rawdata ) ;
        HttpContentValue.FwInfo.md5_checksum = CommonFunctionUnit.get_json_string( fwinfo_response, ACSServerJsonFenceName.FwInfo.MD5_CHECKSUM, HttpContentValue.FwInfo.md5_checksum ) ;
        HttpContentValue.FwInfo.file_size = CommonFunctionUnit.get_json_int( fwinfo_response, ACSServerJsonFenceName.FwInfo.FILE_SIZE, HttpContentValue.FwInfo.file_size ) ;
        HttpContentValue.FwInfo.update_mode = CommonFunctionUnit.get_json_string( fwinfo_response, ACSServerJsonFenceName.FwInfo.DRIVEN_BY, HttpContentValue.FwInfo.update_mode ) ;
        HttpContentValue.FwInfo.model_checker = CommonFunctionUnit.get_json_string( fwinfo_response, ACSServerJsonFenceName.FwInfo.MODEL_CHECKER, HttpContentValue.FwInfo.model_checker ) ;
        HttpContentValue.FwInfo.is_ota_in_standby = CommonFunctionUnit.get_json_int( fwinfo_response, ACSServerJsonFenceName.FwInfo.OTA_AFTER_STANDBY, HttpContentValue.FwInfo.is_ota_in_standby ) ;
        HttpContentValue.FwInfo.is_ota_after_boot = CommonFunctionUnit.get_json_int( fwinfo_response, ACSServerJsonFenceName.FwInfo.OTA_AFTER_BOOT, HttpContentValue.FwInfo.is_ota_after_boot ) ;
        HttpContentValue.FwInfo.is_ota_immediately = CommonFunctionUnit.get_json_int( fwinfo_response, ACSServerJsonFenceName.FwInfo.OTA_AFTER_IMMEDIATELY, HttpContentValue.FwInfo.is_ota_immediately ) ;
        HttpContentValue.FwInfo.is_ota_in_time_window = CommonFunctionUnit.get_json_string( fwinfo_response, ACSServerJsonFenceName.FwInfo.OTA_AFTER_TIME, HttpContentValue.FwInfo.is_ota_in_time_window ) ;

//        HttpContentValue.FwInfo.http_status = CommonFunctionUnit.get_json_int( fwinfo_response, ACSServerJsonFenceName.FwInfo.STATUS, CommonDefine.DEFAULT_INT_VALUE ) ;
//        HttpContentValue.FwInfo.fw_version = CommonFunctionUnit.get_json_string( fwinfo_response, ACSServerJsonFenceName.FwInfo.FW_VERSION, null ) ;
//        HttpContentValue.FwInfo.release_notes = CommonFunctionUnit.get_json_string( fwinfo_response, ACSServerJsonFenceName.FwInfo.RELEASE_NOTES, null ) ;
//        HttpContentValue.FwInfo.fw_download_path_rawdata = CommonFunctionUnit.get_json_jsonobj( fwinfo_response, ACSServerJsonFenceName.FwInfo.FW_PATH, null ) ;
//        HttpContentValue.FwInfo.md5_checksum = CommonFunctionUnit.get_json_string( fwinfo_response, ACSServerJsonFenceName.FwInfo.MD5_CHECKSUM, null ) ;
//        HttpContentValue.FwInfo.file_size = CommonFunctionUnit.get_json_int( fwinfo_response, ACSServerJsonFenceName.FwInfo.FILE_SIZE, CommonDefine.DEFAULT_INT_VALUE ) ;
//        HttpContentValue.FwInfo.update_mode = CommonFunctionUnit.get_json_string( fwinfo_response, ACSServerJsonFenceName.FwInfo.DRIVEN_BY, null ) ;
//        HttpContentValue.FwInfo.model_checker = CommonFunctionUnit.get_json_string( fwinfo_response, ACSServerJsonFenceName.FwInfo.MODEL_CHECKER, null ) ;
//        HttpContentValue.FwInfo.is_ota_in_standby = CommonFunctionUnit.get_json_int( fwinfo_response, ACSServerJsonFenceName.FwInfo.OTA_AFTER_STANDBY, CommonDefine.DEFAULT_INT_VALUE ) ;
//        HttpContentValue.FwInfo.is_ota_after_boot = CommonFunctionUnit.get_json_int( fwinfo_response, ACSServerJsonFenceName.FwInfo.OTA_AFTER_BOOT, CommonDefine.DEFAULT_INT_VALUE ) ;
//        HttpContentValue.FwInfo.is_ota_immediately = CommonFunctionUnit.get_json_int( fwinfo_response, ACSServerJsonFenceName.FwInfo.OTA_AFTER_IMMEDIATELY, CommonDefine.DEFAULT_INT_VALUE ) ;
//        HttpContentValue.FwInfo.is_ota_in_time_window = CommonFunctionUnit.get_json_string( fwinfo_response, ACSServerJsonFenceName.FwInfo.OTA_AFTER_TIME, null ) ;
//        CommonFunctionUnit.printClassFields(HttpContentValue.FwInfo.class);

        result |= parse_fw_download_path( HttpContentValue.FwInfo.fw_download_path_rawdata ) ;

        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    private static int parse_fw_download_path( JSONObject fw_download_path )
    {
        int result = CommonDefine.SUCCESS ;
        if ( fw_download_path == null ) {
            Log.d( TAG, "fw_download_path null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.FwInfo.FW_DOWNLOAD_PATH.high_bw_url = CommonFunctionUnit.get_json_string( fw_download_path, ACSServerJsonFenceName.FwInfo.HIGH_BW_URL, HttpContentValue.FwInfo.FW_DOWNLOAD_PATH.high_bw_url ) ;
        HttpContentValue.FwInfo.FW_DOWNLOAD_PATH.low_bw_url = CommonFunctionUnit.get_json_string( fw_download_path, ACSServerJsonFenceName.FwInfo.LOW_BW_URL, HttpContentValue.FwInfo.FW_DOWNLOAD_PATH.low_bw_url ) ;
//        CommonFunctionUnit.printClassFields(HttpContentValue.FwInfo.FW_DOWNLOAD_PATH.class);
        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    public static int parse_appinfo_response( JSONObject appinfo_response )
    {
        int result = CommonDefine.SUCCESS ;
        if ( appinfo_response == null ) {
            Log.d( TAG, "appinfo_response null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.ProvisionResponse.mqtt_app_topic_group_name = CommonFunctionUnit.get_json_string( appinfo_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_APP_TOPIC_GROUP_NAME, HttpContentValue.ProvisionResponse.mqtt_app_topic_group_name ) ;
        HttpContentValue.ProvisionResponse.fwinfo_check_interval = CommonFunctionUnit.get_json_int( appinfo_response, ACSServerJsonFenceName.ProvisionResponse.FWINFO_CHECK_INTERVAL, HttpContentValue.ProvisionResponse.fwinfo_check_interval ) ;
        HttpContentValue.ProvisionResponse.appinfo_check_interval = CommonFunctionUnit.get_json_int( appinfo_response, ACSServerJsonFenceName.ProvisionResponse.APPINFO_CHECK_INTERVAL, HttpContentValue.ProvisionResponse.appinfo_check_interval ) ;

        HttpContentValue.AppInfo.appinfo_response_rawdata = appinfo_response ;
        HttpContentValue.AppInfo.apps_info_rawdata = CommonFunctionUnit.get_json_jsonarray( appinfo_response, ACSServerJsonFenceName.AppInfo.APPS_INFO, HttpContentValue.AppInfo.apps_info_rawdata ) ;
        HttpContentValue.AppInfo.app_download_url_prefix = CommonFunctionUnit.get_json_string( appinfo_response, ACSServerJsonFenceName.AppInfo.APP_PATH, HttpContentValue.AppInfo.app_download_url_prefix ) ;
//        CommonFunctionUnit.printClassFields(HttpContentValue.AppInfo.class);

        result |= parse_apps_info( HttpContentValue.AppInfo.apps_info_rawdata ) ;
        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    private static int parse_apps_info( JSONArray apps_info_array )
    {
        JSONObject app_info_object = null;
        int result = CommonDefine.SUCCESS ;
        if ( apps_info_array == null ) {
            Log.d( TAG, "apps_info_array null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.AppInfo.apps_info_list = new ArrayList<>();
        for (int i = 0; i < apps_info_array.length(); i++) {
            try {
                app_info_object = apps_info_array.getJSONObject(i);
                HttpContentValue.AppInfo.Apps_info app_info_node = new HttpContentValue.AppInfo.Apps_info() ;
                app_info_node.package_name = CommonFunctionUnit.get_json_string( app_info_object, ACSServerJsonFenceName.AppInfo.PACKAGE_NAME, app_info_node.package_name ) ;
                app_info_node.apk_file_name = CommonFunctionUnit.get_json_string( app_info_object, ACSServerJsonFenceName.AppInfo.APK_NAME, app_info_node.apk_file_name ) ;
                app_info_node.md5_checksum = CommonFunctionUnit.get_json_string( app_info_object, ACSServerJsonFenceName.AppInfo.MD5_CHECKSUM, app_info_node.md5_checksum ) ;
                app_info_node.apk_version_code = CommonFunctionUnit.get_json_string( app_info_object, ACSServerJsonFenceName.AppInfo.VERSION_CODE, app_info_node.apk_version_code ) ;
                app_info_node.apk_version_name = CommonFunctionUnit.get_json_string( app_info_object, ACSServerJsonFenceName.AppInfo.VERSION_NAME, app_info_node.apk_version_name ) ;
                app_info_node.apk_sdk_version = CommonFunctionUnit.get_json_string( app_info_object, ACSServerJsonFenceName.AppInfo.SDK_VERSION, app_info_node.apk_sdk_version ) ;
                app_info_node.apk_target_version = CommonFunctionUnit.get_json_string( app_info_object, ACSServerJsonFenceName.AppInfo.TARGET_VERSION, app_info_node.apk_target_version ) ;
                app_info_node.is_install = CommonFunctionUnit.get_json_int( app_info_object, ACSServerJsonFenceName.AppInfo.ENABLE, CommonDefine.DEFAULT_INT_VALUE ) ;
                app_info_node.install_mode = CommonFunctionUnit.get_json_string( app_info_object, ACSServerJsonFenceName.AppInfo.MODE, app_info_node.install_mode ) ;

                HttpContentValue.AppInfo.apps_info_list.add(app_info_node);
            } catch (JSONException e) {
                e.printStackTrace();
                result = CommonDefine.FAIL ;
            }
        }

//        CommonFunctionUnit.printClassFields(HttpContentValue.AppInfo.apps_info_list);
        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    public static int parse_ad_list_response( JSONArray ad_list_response )
    {
        int result = CommonDefine.SUCCESS ;
        if ( ad_list_response == null ) {
            Log.d( TAG, "ad_list_response null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.AdList.ad_list_rawdata = ad_list_response ;
//        CommonFunctionUnit.printClassFields(HttpContentValue.AdList.class);

        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    public static int parse_week_highlight_response( JSONArray week_highlight_response )
    {
        int result = CommonDefine.SUCCESS ;
        if ( week_highlight_response == null ) {
            Log.d( TAG, "week_highlight_response null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.WeekHightLight.week_highlight_rawdata = week_highlight_response ;
//        CommonFunctionUnit.printClassFields(HttpContentValue.WeekHightLight.class);

        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    public static int parse_rank_list_response( JSONArray rank_list_response )
    {
        int result = CommonDefine.SUCCESS ;
        if ( rank_list_response == null ) {
            Log.d( TAG, "rank_list_response null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.RankList.rank_list_rawdata = rank_list_response ;
//        CommonFunctionUnit.printClassFields(HttpContentValue.RankList.class);

        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    public static int parse_launcherinfo_response( JSONObject launcherinfo_response )
    {
        int result = CommonDefine.SUCCESS ;
        if ( launcherinfo_response == null ) {
            Log.d( TAG, "launcherinfo_response null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.LauncherInfo.launcher_info_rawdata = launcherinfo_response ;

        HttpContentValue.ProvisionResponse.ota_check_interval = CommonFunctionUnit.get_json_int( launcherinfo_response, ACSServerJsonFenceName.ProvisionResponse.OTA_CHECK_INTERVAL, HttpContentValue.ProvisionResponse.ota_check_interval);
        HttpContentValue.ProvisionResponse.project_name = CommonFunctionUnit.get_json_string( launcherinfo_response, ACSServerJsonFenceName.ProvisionResponse.PROJECT_NAME, HttpContentValue.ProvisionResponse.project_name);
        HttpContentValue.FwInfo.is_ota_in_standby = CommonFunctionUnit.get_json_int( launcherinfo_response, ACSServerJsonFenceName.FwInfo.OTA_AFTER_STANDBY, HttpContentValue.FwInfo.is_ota_in_standby ) ;
        HttpContentValue.FwInfo.is_ota_after_boot = CommonFunctionUnit.get_json_int( launcherinfo_response, ACSServerJsonFenceName.FwInfo.OTA_AFTER_BOOT, HttpContentValue.FwInfo.is_ota_after_boot ) ;
        HttpContentValue.FwInfo.is_ota_immediately = CommonFunctionUnit.get_json_int( launcherinfo_response, ACSServerJsonFenceName.FwInfo.OTA_AFTER_IMMEDIATELY, HttpContentValue.FwInfo.is_ota_immediately ) ;
        HttpContentValue.FwInfo.is_ota_in_time_window = CommonFunctionUnit.get_json_string( launcherinfo_response, ACSServerJsonFenceName.FwInfo.OTA_AFTER_TIME, HttpContentValue.FwInfo.is_ota_in_time_window ) ;
        HttpContentValue.ProvisionResponse.is_network_eth_wifi_auto_switch_enable = CommonFunctionUnit.get_json_int( launcherinfo_response, ACSServerJsonFenceName.ProvisionResponse.IS_NETWORK_ETH_WIFI_AUTO_SWITCH, HttpContentValue.ProvisionResponse.is_network_eth_wifi_auto_switch_enable);
        HttpContentValue.ProvisionResponse.save_log_size_mb = CommonFunctionUnit.get_json_int( launcherinfo_response, ACSServerJsonFenceName.ProvisionResponse.SAVE_LOG_SIZE_MB, HttpContentValue.ProvisionResponse.save_log_size_mb);
        HttpContentValue.ProvisionResponse.is_save_log_enable = CommonFunctionUnit.get_json_boolean( launcherinfo_response, ACSServerJsonFenceName.ProvisionResponse.IS_SAVE_LOG_ENABLE, HttpContentValue.ProvisionResponse.is_save_log_enable);
        HttpContentValue.ProvisionResponse.mqtt_ota_topic_group_name = CommonFunctionUnit.get_json_string( launcherinfo_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_OTA_TOPIC_GROUP_NAME, HttpContentValue.ProvisionResponse.mqtt_ota_topic_group_name);
        HttpContentValue.ProvisionResponse.fwinfo_check_interval = CommonFunctionUnit.get_json_int( launcherinfo_response, ACSServerJsonFenceName.ProvisionResponse.FWINFO_CHECK_INTERVAL, HttpContentValue.ProvisionResponse.fwinfo_check_interval);
        HttpContentValue.ProvisionResponse.mqtt_app_topic_group_name = CommonFunctionUnit.get_json_string( launcherinfo_response, ACSServerJsonFenceName.ProvisionResponse.MQTT_APP_TOPIC_GROUP_NAME, HttpContentValue.ProvisionResponse.mqtt_app_topic_group_name);
        HttpContentValue.ProvisionResponse.appinfo_check_interval = CommonFunctionUnit.get_json_int( launcherinfo_response, ACSServerJsonFenceName.ProvisionResponse.APPINFO_CHECK_INTERVAL, HttpContentValue.ProvisionResponse.appinfo_check_interval);
        HttpContentValue.ProvisionResponse.launcherinfo_check_interval = CommonFunctionUnit.get_json_int( launcherinfo_response, ACSServerJsonFenceName.ProvisionResponse.LAUNCHERINFO_CHECK_INVERVAL, HttpContentValue.ProvisionResponse.launcherinfo_check_interval);
        HttpContentValue.ProvisionResponse.hdmi_output_control = CommonFunctionUnit.get_json_string( launcherinfo_response, ACSServerJsonFenceName.ProvisionResponse.HDMI_OUTPUT_CONTROL, HttpContentValue.ProvisionResponse.hdmi_output_control);
        HttpContentValue.ProvisionResponse.launcher_profile_rawdata = CommonFunctionUnit.get_json_jsonarray( launcherinfo_response, ACSServerJsonFenceName.ProvisionResponse.LAUNCHER_PROFILE, HttpContentValue.ProvisionResponse.launcher_profile_rawdata);

        HttpContentValue.LauncherInfo.model_name = CommonFunctionUnit.get_json_string( launcherinfo_response, ACSServerJsonFenceName.LauncherInfo.MODEL_NAME, HttpContentValue.LauncherInfo.model_name ) ;
        HttpContentValue.LauncherInfo.android_version = CommonFunctionUnit.get_json_string( launcherinfo_response, ACSServerJsonFenceName.LauncherInfo.ANDROID_VERSION, HttpContentValue.LauncherInfo.android_version ) ;
        HttpContentValue.LauncherInfo.ota_started_time = CommonFunctionUnit.get_json_string( launcherinfo_response, ACSServerJsonFenceName.LauncherInfo.OTA_STARTED_TIME, HttpContentValue.LauncherInfo.ota_started_time ) ;
        HttpContentValue.LauncherInfo.app_profile_id = CommonFunctionUnit.get_json_int( launcherinfo_response, ACSServerJsonFenceName.LauncherInfo.APP_PROFILE_ID, HttpContentValue.LauncherInfo.app_profile_id ) ;
        HttpContentValue.LauncherInfo.recommendation_profile_id = CommonFunctionUnit.get_json_int( launcherinfo_response, ACSServerJsonFenceName.LauncherInfo.RECOMMEND_PROFILE_ID, HttpContentValue.LauncherInfo.recommendation_profile_id ) ;
        HttpContentValue.LauncherInfo.hot_video_profile_id = CommonFunctionUnit.get_json_int( launcherinfo_response, ACSServerJsonFenceName.LauncherInfo.HOTVIDEO_PROFILE_ID, HttpContentValue.LauncherInfo.hot_video_profile_id ) ;
        HttpContentValue.LauncherInfo.led_style = CommonFunctionUnit.get_json_string( launcherinfo_response, ACSServerJsonFenceName.LauncherInfo.LED_STYLE, HttpContentValue.LauncherInfo.led_style ) ;

        result |= parse_launcher_profile(HttpContentValue.ProvisionResponse.launcher_profile_rawdata) ;

        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    public static int parse_music_ad_response( JSONArray music_ad_response )
    {
        int result = CommonDefine.SUCCESS ;
        if ( music_ad_response == null ) {
            Log.d( TAG, "music_ad_response null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.MusicAD.music_ad_rawdata = music_ad_response ;
//        CommonFunctionUnit.printClassFields(HttpContentValue.MusicAD.class);

        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    public static int parse_hot_video_response( JSONArray hot_video_response )
    {
        int result = CommonDefine.SUCCESS ;
        if ( hot_video_response == null ) {
            Log.d( TAG, "hot_video_response null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.HotVideo.hot_video_rawdata = hot_video_response ;
//        CommonFunctionUnit.printClassFields(HttpContentValue.HotVideo.class);

        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    public static int parse_recommend_packages_response( JSONObject recommend_packages_response )
    {
        int result = CommonDefine.SUCCESS ;
        if ( recommend_packages_response == null ) {
            Log.d( TAG, "recommend_packages_response null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.RecommendPackages.recommend_packages_rawdata = recommend_packages_response ;
        HttpContentValue.ProvisionResponse.storage_server = CommonFunctionUnit.get_json_string(recommend_packages_response, ACSServerJsonFenceName.RecommendPackages.SERVER, HttpContentValue.ProvisionResponse.storage_server)  ;
//        CommonFunctionUnit.printClassFields(HttpContentValue.RecommendPackages.class);

        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    public static int parse_recommend_packages_json_data( JSONArray recommend_packages_json_data )
    {
        int result = CommonDefine.SUCCESS ;
        if ( recommend_packages_json_data == null ) {
            Log.d( TAG, "recommend_packages_json_data null" ) ;
            return CommonDefine.FAIL;
        }

        HttpContentValue.RecommendPackages.recommend_packages_json_data = recommend_packages_json_data ;
//        CommonFunctionUnit.printClassFields(HttpContentValue.RecommendPackages.class);

        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    public static int parse_mqtt_data_ota_group_change( JSONObject ota_group_change_data )
    {
        int result = CommonDefine.SUCCESS ;
        if ( ota_group_change_data == null ) {
            Log.d( TAG, "ota_group_change_data null" ) ;
            return CommonDefine.FAIL;
        }

        MqttContentValue.ota_group_change_rawdata = ota_group_change_data ;
        HttpContentValue.ProvisionResponse.fwinfo_check_daily_interval = CommonFunctionUnit.get_json_int(ota_group_change_data, ACSServerJsonFenceName.ProvisionResponse.FWINFO_CHECK_DAILY_INTERVAL, HttpContentValue.ProvisionResponse.fwinfo_check_daily_interval) ;
        HttpContentValue.ProvisionResponse.launcherinfo_check_daily_interval = CommonFunctionUnit.get_json_int(ota_group_change_data, ACSServerJsonFenceName.ProvisionResponse.LAUNCHERINFO_CHECK_DAILY_INTERVAL, HttpContentValue.ProvisionResponse.launcherinfo_check_daily_interval) ;
        HttpContentValue.ProvisionResponse.is_save_log_enable = CommonFunctionUnit.get_json_boolean(ota_group_change_data, ACSServerJsonFenceName.ProvisionResponse.IS_SAVE_LOG_ENABLE, HttpContentValue.ProvisionResponse.is_save_log_enable) ;

        result |= parse_common_device_info(ota_group_change_data) ;
        result |= parse_fwinfo_response(ota_group_change_data);
//        CommonFunctionUnit.printClassFields(HttpContentValue.ProvisionResponse.DeviceSettings.class);
//        CommonFunctionUnit.printClassFields(HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.class);
//        CommonFunctionUnit.printClassFields(HttpContentValue.ProvisionResponse.class);
        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    public static int parse_mqtt_data_app_group_change( JSONObject app_group_change_data )
    {
        int result = CommonDefine.SUCCESS ;
        if ( app_group_change_data == null ) {
            Log.d( TAG, "app_group_change_data null" ) ;
            return CommonDefine.FAIL;
        }

        /*
            "categoryList":[], old BANDOTT, currently not used
         */
        MqttContentValue.app_group_change_rawdata = app_group_change_data ;
        HttpContentValue.AppInfo.app_download_url_prefix = CommonFunctionUnit.get_json_string(app_group_change_data, ACSServerJsonFenceName.AppInfo.APP_PATH, HttpContentValue.AppInfo.app_download_url_prefix);
        HttpContentValue.AppInfo.apps_info_rawdata = CommonFunctionUnit.get_json_jsonarray(app_group_change_data, ACSServerJsonFenceName.AppInfo.APPS_INFO, HttpContentValue.AppInfo.apps_info_rawdata);
        result |= parse_apps_info( HttpContentValue.AppInfo.apps_info_rawdata) ;

        HttpContentValue.ProvisionResponse.launcher_profile_rawdata = CommonFunctionUnit.get_json_jsonarray( app_group_change_data, ACSServerJsonFenceName.ProvisionResponse.LAUNCHER_PROFILE, HttpContentValue.ProvisionResponse.launcher_profile_rawdata);
        result |= parse_launcher_profile(HttpContentValue.ProvisionResponse.launcher_profile_rawdata) ;

        JSONArray ad_list_data = CommonFunctionUnit.get_json_jsonarray(app_group_change_data, ACSServerJsonFenceName.AdList.AD_KEY, HttpContentValue.AdList.ad_list_rawdata) ;
        result |= ParseACSData.parse_ad_list_response(ad_list_data);

        JSONArray week_highlight_data = CommonFunctionUnit.get_json_jsonarray(app_group_change_data, ACSServerJsonFenceName.WeekHightLight.WEEK_HIGHLIGHT, HttpContentValue.WeekHightLight.week_highlight_rawdata) ;
        result |= parse_week_highlight_response(week_highlight_data) ;

        JSONArray hot_video_data = CommonFunctionUnit.get_json_jsonarray(app_group_change_data, ACSServerJsonFenceName.HotVideo.HOT_VIDEO_KEY, HttpContentValue.HotVideo.hot_video_rawdata) ;
        result |= parse_hot_video_response(hot_video_data);

        JSONArray music_ad_data = CommonFunctionUnit.get_json_jsonarray(app_group_change_data, ACSServerJsonFenceName.MusicAD.MusicAD, HttpContentValue.MusicAD.music_ad_rawdata);
        result |= parse_music_ad_response(music_ad_data);

        HttpContentValue.ProvisionResponse.mqtt_tv_mail_topic = CommonFunctionUnit.get_json_jsonarray(app_group_change_data, ACSServerJsonFenceName.ProvisionResponse.MQTT_TV_MAIL_TOPIC, HttpContentValue.ProvisionResponse.mqtt_tv_mail_topic);

        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }

    public static int parse_mqtt_tv_mail( boolean data_type_is_jsonarray, JSONObject tv_mail_data )
    {
        int result = CommonDefine.SUCCESS ;
        if ( tv_mail_data == null ) {
            Log.d( TAG, "tv_mail_data null" ) ;
            return CommonDefine.FAIL;
        }

        try {
            String db_data_string = CommonFunctionUnit.get_acs_provider_data(ACSService.g_context, ACSServerJsonFenceName.MqttData.TV_MAIL) ;
            JSONArray db_data = null ;
            if ( db_data_string == null )
                db_data = new JSONArray() ;
            else
                db_data = new JSONArray(db_data_string) ;

            if ( data_type_is_jsonarray ) {
                JSONArray tv_mail_data_array = CommonFunctionUnit.get_json_jsonarray(tv_mail_data, MqttCommand.DATA, null);
                for ( int i = 0 ; i < tv_mail_data_array.length() ; i++ )
                {
                    db_data.put(tv_mail_data_array.get(i));
                }
            }
            else {
                JSONObject tv_mail_data_obj = CommonFunctionUnit.get_json_jsonobj(tv_mail_data, MqttCommand.DATA, null);
                db_data.put(tv_mail_data_obj);
            }

            MqttContentValue.tv_mail_rawdata = db_data ;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if ( DBG_LOG || result != CommonDefine.SUCCESS )
            Log.d( TAG, CommonFunctionUnit.getMethodName() + " : " + result ) ;
        return result ;
    }
}
