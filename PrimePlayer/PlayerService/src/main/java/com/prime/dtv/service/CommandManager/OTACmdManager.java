package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.print.PageRange;
import android.util.Log;

import com.prime.datastructure.CommuincateInterface.OtaModule;
import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.Interface.BaseManager;
import com.prime.dtv.utils.OtaUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OTACmdManager extends BaseManager {
    private static final String TAG = "OTACmdManager" ;

    private Context mContext;
    private OtaUtils mOtaUtils;

    public OTACmdManager(Context context, Handler handler) {
        super(context, TAG, handler, OTACmdManager.class);
        mContext = context;
        mOtaUtils = new OtaUtils(getPesiDtvFrameworkInterfaceCallback());
    }

    /*
    OTA
     */
    public int UpdateUsbSoftWare(String filename) {
        return 0;
    }

    public int UpdateFileSystemSoftWare(String pathAndFileName, String partitionName) {
        return 0;
    }

    public int UpdateOTADVBCSoftWare(int tpId, int freq, int symbol, int qam) {
        return 0;
    }

    public int UpdateOTADVBTSoftWare(int tpId, int freq, int bandwith, int qam, int priority) {
        return 0;
    }

    public int UpdateOTADVBT2SoftWare(int tpId, int freq, int symbol, int qam, int channelmode) {
        return 0;
    }

    public int UpdateOTAISDBTSoftWare(int tpId, int freq, int bandwith, int qam, int priority) {
        return 0;
    }

    @Override
    public void BaseHandleMessage(Message msg) {

    }

    public void set_cns_ota() {
        mOtaUtils.start_receiver(mContext);

        ScheduledExecutorService io = Executors.newSingleThreadScheduledExecutor();
        io.scheduleWithFixedDelay(
                () -> {
                    try {
                        if (SystemProperties.get(OtaModule.OTA_DOWNLOAD_STATUS).equals(String.valueOf(OtaModule.DOWNLOADING))) {
                            Log.i(TAG, "ota thread: ota file downloading");
                            return;
                        }

                        if (SystemProperties.get(OtaModule.OTA_DOWNLOAD_STATUS).equals(String.valueOf(OtaModule.DOWNLOAD_COMPLETE))) {
                            Log.i(TAG, "ota thread: ota file download complete");

                            if (!mOtaUtils.is_screen_off(mContext)) {
                                Log.i(TAG, "ota thread: screen on, unable to start ota update");
                                return;
                            }

                            if (mOtaUtils.check_time())
                                mOtaUtils.start_update(mContext);
                            else
                                Log.i(TAG, "ota thread: time not between 2:00 ~ 5:00 am");
                            return;
                        }

                        if (SystemProperties.get(OtaModule.OTA_DOWNLOAD_STATUS).equals(String.valueOf(OtaModule.DOWNLOAD_FAIL))) {
                            Log.i(TAG, "ota thread: ota file fail");
                            mOtaUtils.set_download_status(OtaModule.IDLE);
                        }

                        if (SystemProperties.get(OtaModule.OTA_DOWNLOAD_STATUS).equals(String.valueOf(OtaModule.UPDATE_FAIL))) {
                            Log.i(TAG, "ota thread: ota update fail");
                            mOtaUtils.set_download_status(OtaModule.IDLE);
                        }

                        mOtaUtils.setUpdateInfo(null);
                        String updateInfoString = ota_get_update_info();
                        if (updateInfoString == null) {
                            Log.i(TAG, "ota thread: updateInfoString is null");
                            return;
                        }

                        OtaUtils.UpdateInfo updateInfo = OtaUtils.parseUpdateInfo(updateInfoString);

                        String newVersion = updateInfo.get_last_version();
                        String currentVersion = Pvcfg.get_firmware_version();
                        Log.d(TAG, "ota thread: newVersion = " + newVersion + " currentVersion = " + currentVersion);
                        if (newVersion == null || newVersion.isEmpty()) {
                            Log.i(TAG, "ota thread: new version is empty or null");
                            return;
                        }

                        if (newVersion.equals(currentVersion)) {
                            Log.i(TAG, "ota thread: new version is equals current version");
                            return;
                        }

                        mOtaUtils.setUpdateInfo(updateInfo);

                        OtaUtils.process_ota_download_file(mContext, updateInfo);
                        Log.d(TAG, "ota thread: start download");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                0,                  // 立刻執行第一次
                2,
                TimeUnit.HOURS
        );
    }

    public String ota_get_update_info() {
        String urlString = OtaUtils.get_update_info_url_string(mContext);
        String updateInfo = OtaUtils.doGet(urlString, mContext);
        return updateInfo;
    }

    public String ota_upload_first_boot_info() {
        String urlString = OtaUtils.get_upload_first_boot_info_url_string();
        String xmlString = OtaUtils.get_upload_first_boot_info_xml_string(mContext);
        String firstBootInfo = OtaUtils.doPost(urlString, xmlString, mContext);
        return firstBootInfo;
    }

    public String ota_boot_login() {
        String urlString = OtaUtils.get_boot_login_url_string();
        String xmlString = OtaUtils.get_boot_login_xml_string(mContext);
        String bootLoginInfo = OtaUtils.doPost(urlString, xmlString, mContext);
        return bootLoginInfo;
    }

    public String ota_upload_update_status() {
        String urlString = OtaUtils.get_upload_update_status_url_string();
        String xmlString = OtaUtils.get_upload_update_status_xml_string();
        String updateStatusnInfo = OtaUtils.doPost(urlString, xmlString, mContext);
        return updateStatusnInfo;
    }

    public int start_ota_update() {
        mOtaUtils.start_update(mContext);
        return 0;
    }
}
