package com.prime.datastructure.CommuincateInterface;

import static com.prime.datastructure.CommuincateInterface.CommandBase.CMD_ServicePlayer_OTA_Base;

public class OtaModule {
    public static final int CMD_ServicePlayer_OTA_GetUpdateInfo = CMD_ServicePlayer_OTA_Base+0x01;
    public static final int CMD_ServicePlayer_OTA_UploadFirstBootInfo = CMD_ServicePlayer_OTA_Base+0x02;
    public static final int CMD_ServicePlayer_OTA_BootLogin = CMD_ServicePlayer_OTA_Base+0x03;
    public static final int CMD_ServicePlayer_OTA_UploadUpdateStatus = CMD_ServicePlayer_OTA_Base+0x04;
    public static final int CMD_ServicePlayer_OTA_StartUpdate = CMD_ServicePlayer_OTA_Base+0x05;


    public static final String KEY_OTA_UPDATE_REPLY = "OtaUpdateReply";
    public static final String KEY_OTA_UPLOAD_FIRST_BOOT_REPLY = "OtaUploadFirstBootReply";
    public static final String KEY_OTA_BOOT_LOGIN_REPLY = "OtaBootLoginReply";
    public static final String KEY_OTA_UPLOAD_UPDATE_STATUS_REPLY = "OtaUploadUpdateStatusReply";

    //from OTA SERVICE
    public final static String OTA_SERVICE_PACKAGE_NAME = "com.prime.android.tv.otaservice" ;

    public static final String ABUPDATE_BROADCAST_ERROR = "com.prime.android.tv.otaservice.abupdate.error";
    public static final String ABUPDATE_BROADCAST_COMPLETE = "com.prime.android.tv.otaservice.abupdate.complete";
    public static final String ABUPDATE_BROADCAST_STOP = "com.prime.android.tv.otaservice.abupdate.stop";
    public final static String ABUPDATE_BROADCAST_DOWNLOAD_COMPLETE = "com.prime.android.tv.otaservice.abupdate.download.complete";
    public final static String ABUPDATE_BROADCAST_DOWNLOAD_ERROR = "com.prime.android.tv.otaservice.abupdate.download.error";
    public final static String ABUPDATE_BROADCAST_DOWNLOAD_ZIP_FILE = "com.prime.android.tv.otaservice.download.zip.file";
    public static final String ABUPDATE_BROADCAST_REGISTER_CALLER   = "com.prime.android.tv.otaservice.abupdate.register.caller";
    public static final String ABUPDATE_BROADCAST_UPDATE_CALLER         = "prime.abupdate.caller";
    public static final String ABUPDATE_BROADCAST_START = "com.prime.android.tv.otaservice.abupdate.start";

    //ota service param
    public final static String BROADCAST_DOWNLOAD_ZIP_URL = "prime.download.zip.url";
    public final static String BROADCAST_DOWNLOAD_ZIP_MD5 = "prime.download.zip.md5";

    public static final String OTA_MODE = "persist.sys.prime.ota_mode" ;
    public static final String OTA_DOWNLOAD_STATUS = "persist.sys.prime.ota_download_status";

    //param of ABUPDATE_BROADCAST_START
    public static final String ABUPDATE_BROADCAST_UPDATE_ZIP_URL       = "prime.abupdate.url.zip";
    public static final String ABUPDATE_BROADCAST_UPDATE_ZIP_OFFSET    = "prime.abupdate.offset.zip";
    public static final String ABUPDATE_BROADCAST_UPDATE_ZIP_SIZE      = "prime.abupdate.size.zip";
    public static final String ABUPDATE_BROADCAST_UPDATE_PROPERTIES    = "prime.abupdate.properties.zip";
    public static final String ABUPDATE_BROADCAST_UPDATE_MODE          = "prime.abupdate.MODE";
    public static final String TARGET_PATH = "/data/ota_package/update.zip";
    public static final int UPDATE_MODE = 3;// 0:stream payload, 1:stream zip, 2:usb, 3:download

    public static int IDLE = 0 ;
    public static int DOWNLOADING = 1 ;
    public static int DOWNLOAD_COMPLETE = 2 ;
    public static int DOWNLOAD_FAIL = 3 ;
    public static int UPDATE_COMPLETE = 4;
    public static int UPDATE_FAIL = 5;

    public static int CNS_DEVICE_LOGIN = 200;
    public static int CNS_DOWNLOADING = 100;
    public static int CNS_DOWNLOAD_COMPLETE = 101;
    public static int CNS_DOWNLOAD_FAIL = 102;
    public static int CNS_UPDATE_COMPLETE = 103;
    public static int CNS_UPDATE_FAIL = 104;
}
