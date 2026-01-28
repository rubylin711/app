package com.prime.dtvservice.Interface;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_STATUS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.VALUE;

import android.os.Bundle;
import android.util.Log;

import com.prime.datastructure.CommuincateInterface.AvModule;
import com.prime.datastructure.CommuincateInterface.PvrModule;
import com.prime.datastructure.sysdata.AudioInfo;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.PvrInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.utils.UsbUtils;
import com.prime.dtvservice.PrimeDtvServiceApplication;

public class PvrModuleCommand {
    private static final String TAG = "PvrModuleCommand";
    private PrimeDtv primeDtv = null;

    public Bundle executeCommand(Bundle requestBundle, Bundle replyBundle, PrimeDtv primeDtv) {
        this.primeDtv = primeDtv;
        int command_id = requestBundle.getInt(COMMAND_ID, 0);
        LogUtils.d("command_id = "+command_id);
        switch (command_id) {
            case PvrModule.CMD_ServicePlayer_PVR_Init:
                replyBundle = pvr_init(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_DeInit:
                replyBundle = pvr_deinit(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_IsFullRecording:
                replyBundle = pvr_change_channel_manager_is_full_recording(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_IsTimeshiftStart:
                replyBundle = pvr_change_channel_manager_is_timeshift_start(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_IsChannelRecording:
                replyBundle = pvr_change_channel_manager_is_channel_recording(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_RecordStart:
                replyBundle = pvr_change_channel_manager_record_start(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_RecordStop:
                replyBundle = pvr_change_channel_manager_record_stop(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_GetRecNum:
                replyBundle = pvr_change_channel_manager_get_rec_num(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_GetRecTunerId:
                replyBundle = pvr_change_channel_manager_get_rec_tuner_id(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_PlaybackStart:
                replyBundle = pvr_change_channel_manager_playback_start(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_PlaybackStop:
                replyBundle = pvr_change_channel_manager_playback_stop(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_PlayPlay:
                replyBundle = pvr_playback_resume(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_PlayPause:
                replyBundle = pvr_playback_pause(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_PlayGetPlayTime:
                replyBundle = pvr_playback_get_play_time(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_PlaySeek:
                replyBundle = pvr_playback_seek(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_PlaySetSpeed:
                replyBundle = pvr_playback_set_speed(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_PlayChangeAudioTrack:
                replyBundle = pvr_playback_change_audio_track(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_GetMountUsbPath:
                replyBundle = pvr_get_mount_usb_path(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_DelOneRec:
                replyBundle = pvr_delete_one_rec(requestBundle, replyBundle);
                break;
            case PvrModule.CMD_ServicePlayer_PVR_DelAllRecs:
                replyBundle = pvr_delete_all_recs(requestBundle, replyBundle);
                break;
            default:
                LogUtils.e("Command not implement");
                replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    public Bundle pvr_init(Bundle requestBundle, Bundle replyBundle) {
        String usbMountPath = requestBundle.getString(PvrModule.KEY_USB_MOUNT_PATH, "");
        if (usbMountPath.isBlank()) {
            Log.e(TAG, "pvr_init: empty or blank usb path.");
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
            return replyBundle;
        }

        int result = primeDtv.pvr_init(usbMountPath);
        if (result == 0) {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        } else {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    public Bundle pvr_deinit(Bundle requestBundle, Bundle replyBundle) {
        int result = primeDtv.pvr_deinit();
        if (result == 0) {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        } else {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    public Bundle pvr_change_channel_manager_is_full_recording(Bundle requestBundle, Bundle replyBundle) {
        boolean isFullRecording = PrimeDtvServiceApplication.getChannelChangeManager().is_full_recording();
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putBoolean(VALUE, isFullRecording);

        return replyBundle;
    }

    public Bundle pvr_change_channel_manager_is_timeshift_start(Bundle requestBundle, Bundle replyBundle) {
        boolean isFullRecording = PrimeDtvServiceApplication.getChannelChangeManager().is_timeshift_start();
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putBoolean(VALUE, isFullRecording);

        return replyBundle;
    }

    public Bundle pvr_change_channel_manager_is_channel_recording(Bundle requestBundle, Bundle replyBundle) {
        long channelId = requestBundle.getLong(ProgramInfo.CHANNEL_ID,0);
        boolean isFullRecording = PrimeDtvServiceApplication.getChannelChangeManager().is_channel_recording(channelId);
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putBoolean(VALUE, isFullRecording);

        return replyBundle;
    }

    public Bundle pvr_change_channel_manager_record_start(Bundle requestBundle, Bundle replyBundle) {
        long channelId = requestBundle.getLong(ProgramInfo.CHANNEL_ID,0);
        int eventId = requestBundle.getInt(BookInfo.EPG_EVENT_ID,0);
        int duration = requestBundle.getInt(BookInfo.DURATION,0);
        boolean isSeries = requestBundle.getBoolean(BookInfo.IS_SERIES,false);
        boolean ret = PrimeDtvServiceApplication.getChannelChangeManager().pvr_record_start(channelId,eventId,duration,isSeries);
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putBoolean(VALUE, ret);

        return replyBundle;
    }

    public Bundle pvr_change_channel_manager_record_stop(Bundle requestBundle, Bundle replyBundle) {
        long channelId = requestBundle.getLong(ProgramInfo.CHANNEL_ID,0);
        PrimeDtvServiceApplication.getChannelChangeManager().pvr_record_stop(channelId);
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);

        return replyBundle;
    }

    public Bundle pvr_change_channel_manager_get_rec_num(Bundle requestBundle, Bundle replyBundle) {
        int rec_num = PrimeDtvServiceApplication.getChannelChangeManager().get_rec_num();
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putInt(VALUE, rec_num);

        return replyBundle;
    }

    public Bundle pvr_change_channel_manager_get_rec_tuner_id(Bundle requestBundle, Bundle replyBundle) {
        int rec_tunerId = PrimeDtvServiceApplication.getChannelChangeManager().get_rec_tuner_id();
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putInt(VALUE, rec_tunerId);

        return replyBundle;
    }

    public Bundle pvr_change_channel_manager_playback_start(Bundle requestBundle, Bundle replyBundle) {
        int masterIdx = requestBundle.getInt(PvrModule.KEY_MASTER_REC_INDEX, -1);
        int seriesIdx = requestBundle.getInt(PvrModule.KEY_SERIES_REC_INDEX, -1);
        if (masterIdx == -1 || seriesIdx == -1) {
            Log.e(TAG, "pvr_change_channel_manager_playback_start: invalid rec index");
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
            return replyBundle;
        }

        PvrRecIdx pvrRecIdx = new PvrRecIdx(masterIdx, seriesIdx);
        boolean fromLastPos = requestBundle.getBoolean(PvrModule.KEY_FROM_LAST_POS, false);

        int playbackTunerId = PrimeDtvServiceApplication.getChannelChangeManager().pvr_playback_start(
                pvrRecIdx,
                fromLastPos,
                -1); // will find a idle tuner id itself if we pass invalid id

        replyBundle.putInt(AvModule.PlayId_string, playbackTunerId);
        if (playbackTunerId >= 0) {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        } else {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    public Bundle pvr_change_channel_manager_playback_stop(Bundle requestBundle, Bundle replyBundle) {
        PrimeDtvServiceApplication.getChannelChangeManager().pvr_playback_stop();
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);

        return replyBundle;
    }

    public Bundle pvr_playback_resume(Bundle requestBundle, Bundle replyBundle) {
        int playbackTunerId = requestBundle.getInt(AvModule.PlayId_string, 0);
        primeDtv.pvr_PlayPlay(playbackTunerId);
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);

        return replyBundle;
    }

    public Bundle pvr_playback_pause(Bundle requestBundle, Bundle replyBundle) {
        int playbackTunerId = requestBundle.getInt(AvModule.PlayId_string, 0);
        primeDtv.pvr_PlayPause(playbackTunerId);
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);

        return replyBundle;
    }

    public Bundle pvr_playback_get_play_time(Bundle requestBundle, Bundle replyBundle) {
        int playbackTunerId = requestBundle.getInt(AvModule.PlayId_string, 0);
        int playTimeSecs = primeDtv.pvr_PlayGetPlayTime(playbackTunerId);
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putInt(PvrModule.KEY_PLAY_TIME, playTimeSecs);
        return replyBundle;
    }

    public Bundle pvr_playback_seek(Bundle requestBundle, Bundle replyBundle) {
        int playbackTunerId = requestBundle.getInt(AvModule.PlayId_string, 0);
        int seekModeValue = requestBundle.getInt(PvrModule.KEY_SEEK_MODE, 0);
        PvrInfo.EnSeekMode seekMode = PvrInfo.EnSeekMode.fromValue(seekModeValue);
        int offsetSecs = requestBundle.getInt(PvrModule.KEY_SEEK_OFFSET_SECS, 0);

        primeDtv.pvr_PlaySeek(playbackTunerId, seekMode, offsetSecs);

        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle pvr_playback_set_speed(Bundle requestBundle, Bundle replyBundle) {
        int playbackTunerId = requestBundle.getInt(AvModule.PlayId_string, 0);
        int speedValue = requestBundle.getInt(
                PvrModule.KEY_SPEED, PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD01.getValue());
        PvrInfo.EnPlaySpeed playSpeed = PvrInfo.EnPlaySpeed.convertSpeed(speedValue);

        primeDtv.pvr_PlaySetSpeed(playbackTunerId, playSpeed);

        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle pvr_playback_change_audio_track(Bundle requestBundle, Bundle replyBundle) {
        int playbackTunerId = requestBundle.getInt(AvModule.PlayId_string, 0);
        requestBundle.setClassLoader(AudioInfo.class.getClassLoader());
        AudioInfo.AudioComponent audioComponent = requestBundle.getParcelable(
                AvModule.AudioComponent_string, AudioInfo.AudioComponent.class);

        if (audioComponent == null) {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
            return replyBundle;
        }

        primeDtv.pvr_PlayChangeAudioTrack(playbackTunerId, audioComponent);

        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle pvr_get_mount_usb_path(Bundle requestBundle, Bundle replyBundle) {
        String mountPath = UsbUtils.get_mount_usb_path();

        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putString(PvrModule.KEY_USB_MOUNT_PATH, mountPath);

        return replyBundle;
    }

    public Bundle pvr_delete_one_rec(Bundle requestBundle, Bundle replyBundle) {
        int masterIdx = requestBundle.getInt(PvrModule.KEY_MASTER_REC_INDEX, -1);
        int seriesIdx = requestBundle.getInt(PvrModule.KEY_SERIES_REC_INDEX, -1);
        if (masterIdx == -1 || seriesIdx == -1) {
            Log.e(TAG, "pvr_delete_one_rec: invalid rec index");
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
            return replyBundle;
        }

        PvrRecIdx pvrRecIdx = new PvrRecIdx(masterIdx, seriesIdx);
        int deleteCount = primeDtv.pvr_DelOneRec(pvrRecIdx);
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putInt(PvrModule.KEY_COUNT, deleteCount);
        return replyBundle;
    }

    public Bundle pvr_delete_all_recs(Bundle requestBundle, Bundle replyBundle) {
        int deleteCount = primeDtv.pvr_DelAllRecs();
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putInt(PvrModule.KEY_COUNT, deleteCount);
        return replyBundle;
    }
}
