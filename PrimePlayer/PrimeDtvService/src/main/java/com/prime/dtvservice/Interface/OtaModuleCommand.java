package com.prime.dtvservice.Interface;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_STATUS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;

import android.os.Bundle;
import android.util.Log;

import com.prime.datastructure.CommuincateInterface.OtaModule;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.utils.OtaUtils;

public class OtaModuleCommand {
    private static final String TAG = "OtaModuleCommand";
    private PrimeDtv primeDtv = null;

    public Bundle executeCommand(Bundle requestBundle, Bundle replyBundle, PrimeDtv primeDtv){
        this.primeDtv = primeDtv;
        int command_id = requestBundle.getInt(COMMAND_ID, 0);
        LogUtils.d("command_id = "+command_id);
        switch (command_id) {
            case OtaModule.CMD_ServicePlayer_OTA_GetUpdateInfo:
                replyBundle = ota_get_update_info(requestBundle, replyBundle);
                break;
            case OtaModule.CMD_ServicePlayer_OTA_UploadFirstBootInfo:
                replyBundle = upload_first_boot_info(requestBundle, replyBundle);
                break;
            case OtaModule.CMD_ServicePlayer_OTA_BootLogin:
                replyBundle = boot_login(requestBundle, replyBundle);
                break;
            case OtaModule.CMD_ServicePlayer_OTA_UploadUpdateStatus:
                replyBundle = upload_update_status(requestBundle, replyBundle);
                break;
            case OtaModule.CMD_ServicePlayer_OTA_StartUpdate:
                replyBundle = start_update(requestBundle, replyBundle);
                break;

            default:
                LogUtils.e("Command not implement");
                replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    public Bundle ota_get_update_info(Bundle requestBundle, Bundle replyBundle){
        String updateInfo = primeDtv.ota_get_update_info();
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putString(OtaModule.KEY_OTA_UPDATE_REPLY, updateInfo);
        Log.d(TAG, "ota_get_update_info: " + updateInfo);
        return replyBundle;
    }

    public Bundle upload_first_boot_info(Bundle requestBundle, Bundle replyBundle){
        String firstBootInfo = primeDtv.ota_upload_first_boot_info();
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putString(OtaModule.KEY_OTA_UPLOAD_FIRST_BOOT_REPLY, firstBootInfo);
        Log.d(TAG, "upload_first_boot_info: " + firstBootInfo);
        return replyBundle;
    }

    public Bundle boot_login(Bundle requestBundle, Bundle replyBundle){
        String bootLoginInfo = primeDtv.ota_boot_login();
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putString(OtaModule.KEY_OTA_BOOT_LOGIN_REPLY, bootLoginInfo);
        Log.d(TAG, "boot_login: " + bootLoginInfo);
        return replyBundle;
    }

    public Bundle upload_update_status(Bundle requestBundle, Bundle replyBundle){
        String updateStatusnInfo = primeDtv.ota_upload_update_status();
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putString(OtaModule.KEY_OTA_UPLOAD_UPDATE_STATUS_REPLY, updateStatusnInfo);
        Log.d(TAG, "upload_update_status: " + updateStatusnInfo);
        return replyBundle;
    }

    public Bundle start_update(Bundle requestBundle, Bundle replyBundle) {
        primeDtv.start_ota_update();
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }
}
