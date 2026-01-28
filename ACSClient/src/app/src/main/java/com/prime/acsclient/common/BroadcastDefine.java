package com.prime.acsclient.common;


import com.prime.acsclient.communicate.SystemStatusMonitor;

import java.security.PublicKey;

public class BroadcastDefine {
    // setupwizard
    public final static String SETUPWIZARD_PACKAGE_NAME = "com.prime.setupwizard" ;
    public final static String ACS_GET_PROVISION_DATA = "com.prime.acsclient.action.GET_PROVISION_DATA" ;
    public final static String ACS_USER_FINISH_SETUPWIZARD = "com.prime.acsclient.finish_setupwizard" ;
    public final static String ACS_USER_AGREE_EULA = "com.prime.acsclient.finish_eula" ;
    public final static String ACS_RENEW_DEVICE_COMMON_DATA = "com.prime.acsclient.renew_device_common_data" ;
    public final static String ACS_RENEW_FW_INFO = "com.prime.acsclient.renew_fw_info" ;
    public final static String ACS_NETWORK_SPEED_TEST_DONE = "com.prime.acsclient.network_quality" ;
    public final static String ACS_SIGNAL_DETECT_DONE = "com.prime.acsclient.signal_detect" ;

    /// launcher
    public final static String LAUNCHER_PACKAGE_NAME = "com.prime.dmg.launcher" ;
    public final static String ACS_DATA_UPDATE_ACTION_BASE = "com.prime.acsclient.update." ;
    public final static String OTA_SERVICE_PACKAGE_NAME = "com.prime.android.tv.otaservice" ;

    /// ota
    public final static String OTA_UPDATER_PACKAGE_NAME = "com.prime.otaupdater" ;
    public final static String ACS_OTA_PARAM_UPDATE = "com.prime.acsclient.update.ota_param" ;
    public final static String OTA_UPDATER_PARAM_KEY_IS_FORECE_UPDATE = "is_force_update" ;
    public final static String OTA_UPDATER_PARAM_KEY_DOWNLOAD_URL = "download_url" ;
    public final static String OTA_UPDATER_PARAM_KEY_MD5_CHECKSUM = "md5_checksum" ;
    public final static String OTA_UPDATER_PARAM_KEY_FILE_SIZE = "file_size" ;
    public final static String OTA_UPDATER_PARAM_KEY_OTA_VERSION = "ota_version" ;
    public final static String OTA_UPDATER_PARAM_KEY_RELEASE_NOTE = "release_note" ;
    public final static String OTA_UPDATER_PARAM_KEY_MODEL_CHECK_NAME = "model_check_name" ;
    public final static String OTA_ALARAM_START_ACTION = "com.prime.acsclient.otaalarm.start" ;
    public final static String OTA_ALARAM_END_ACTION = "com.prime.acsclient.otaalarm.end" ;
    public final static String OTA_LAUNCHER_STATED = "com.prime.acsclient.launcher_ota.started";
    public final static String OTA_LAUNCHER_VERSION = "com.prime.acsclient.launcher_ota.version";

    //ota service action
    public static final String ABUPDATE_BROADCAST_STOP = "com.prime.android.tv.otaservice.abupdate.stop";
    public final static String ABUPDATE_BROADCAST_DOWNLOAD_COMPLETE = "com.prime.android.tv.otaservice.abupdate.download.complete";
    public final static String ABUPDATE_BROADCAST_DOWNLOAD_ERROR = "com.prime.android.tv.otaservice.abupdate.download.error";
    public final static String ABUPDATE_BROADCAST_DOWNLOAD_ZIP_FILE = "com.prime.android.tv.otaservice.download.zip.file";
    public static final String ABUPDATE_BROADCAST_ERROR = "com.prime.android.tv.otaservice.abupdate.error";
    public static final String ABUPDATE_BROADCAST_COMPLETE = "com.prime.android.tv.otaservice.abupdate.complete";

    //ota service param
    public final static String BROADCAST_DOWNLOAD_ZIP_URL = "prime.download.zip.url";
    public final static String BROADCAST_DOWNLOAD_ZIP_MD5 = "prime.download.zip.md5";

    /// app install/uninstall
    public final static String APP_MANAGER_PACAKGE_NAME = "com.prime.appDownloadManager" ;
    public final static String APP_MANAGER_ACTION = "pesi.broadcast.action.device.manager" ;
    public final static String APP_MANAGER_INSTRUCTION_ID = "INSTRUCTION_ID";
    public final static int APP_MANAGER_INSTALL_CASE_ID = 201;
    public final static String APP_MANAGER_INSTALL_PKGNAME = "INSTALL_PKGNAME";
    public final static String APP_MANAGER_INSTALL_PKG_DOWNLOAD_PATH = "INSTALL_PKG_DOWNLOAD_PATH";
    public final static int APP_MANAGER_UNINSTALL_CASE_ID = 202;
    public final static String APP_MANAGER_UNINSTALL_PKGNAME = "UNINSTALL_PKGNAME";
    public final static String ACS_APK_MANAGER_LIST_UPDATE = "com.prime.acsclient.update.apklist" ;
    public final static int APP_MANAGER_REGIST_CALLER_CASE_ID = 208;
    public final static String APP_MANAGER_RETURN_TARGET_ACTION = "RETURN_TARGET_ACTION";
    public final static String APP_MANAGER_RETURN_TARGET_PKGNAME = "RETURN_TARGET_PKGNAME";

    /// mqtt command
    public final static String MQTT_COMMAND_BROADCAST_ACTION_BASE = "com.prime.acsclient.mqtt_command." ;
    public final static String MQTT_COMMAND_DEVELOP_TEST = "com.prime.acsclient.mqtt_dev_test" ;

}
