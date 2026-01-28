package com.prime.dtvservice.Interface;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_OBJ;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_STATUS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;

import android.os.Bundle;
import android.os.Binder;
import android.util.Log;
import android.view.Surface;

import com.prime.datastructure.CommuincateInterface.AvModule;
import com.prime.datastructure.CommuincateInterface.MiscDefine;
import com.prime.datastructure.sysdata.AudioInfo;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.SubtitleInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.PrimeDtv;
import com.prime.dtvservice.PrimeDtvServiceApplication;

public class AvModuleCommand {
    public static final String TAG = "AvModule";
    private PrimeDtv primeDtv = null;
    public Bundle executeCommand(Bundle requestBundle,Bundle replyBundle,PrimeDtv primeDtv) {
        this.primeDtv = primeDtv;
        int command_id = requestBundle.getInt(COMMAND_ID,0);
        LogUtils.d("command_id = "+command_id);
        switch(command_id) {
            case AvModule.CMD_ServicePlayer_AV_SetVisibleCompiled:
                replyBundle = setSetVisibleCompleted(requestBundle,replyBundle);
                break;
            case AvModule.CMD_ServicePlayer_AV_PlayByChannelId:
                Log.d(TAG,"executeCommand CMD_ServicePlayer_AV_PlayByChannelId");
                replyBundle = av_control_play_by_channel_id(requestBundle,replyBundle);
                break;
            case AvModule.CMD_ServicePlayer_AV_ChangeChannelManager_TIF_PlayByChannelId:
                Log.d(TAG,"executeCommand CMD_ServicePlayer_AV_ChangeChannelManager_TIF_PlayByChannelId");
                replyBundle = av_control_change_channel_manager_tif_play_by_channel_id(requestBundle,replyBundle);
                break;
            case AvModule.CMD_ServicePlayer_AV_ChangeChannelManager_ListUpdate:
                Log.d(TAG,"executeCommand CMD_ServicePlayer_AV_ChangeChannelManager_ListUpdate");
                replyBundle = av_control_change_channel_manager_list_update(requestBundle,replyBundle);
                break;
            case AvModule.CMD_ServicePlayer_AV_SetAspectRatio:
                replyBundle = av_control_set_aspect_ratio(requestBundle,replyBundle);
                break;
            case AvModule.CMD_ServicePlayer_AV_GetPlayStatus:
                replyBundle = av_control_get_play_status(requestBundle,replyBundle);
                break;
            // ★★★ 新增：取得音軌列表
            case AvModule.CMD_ServicePlayer_AV_GetAudioListInfo:
                replyBundle = av_control_get_audio_list_info(requestBundle, replyBundle);
                break;

            // ★★★ 新增：切換音軌
            case AvModule.CMD_ServicePlayer_AV_ChangeAudio:
                replyBundle = av_control_change_audio(requestBundle, replyBundle);
                break;
            // ★★★ Subtitle ★★★
            case AvModule.CMD_ServicePlayer_SUBTITLE_GetList:
                replyBundle = subt_control_get_subtitle_list(requestBundle, replyBundle);
                break;
            case AvModule.CMD_ServicePlayer_SUBTITLE_Select:
                replyBundle = subt_control_select_subtitle(requestBundle, replyBundle);
                break;
            case AvModule.CMD_ServicePlayer_SUBTITLE_Stop:
                replyBundle = subt_control_stop_subtitle(requestBundle, replyBundle);
                break;    
            case AvModule.CMD_ServicePlayer_AV_PlayStop:
                replyBundle = av_control_play_stop(requestBundle,replyBundle);
                break;
            case AvModule.CMD_ServicePlayer_AV_SetSurface:
                replyBundle = set_surface(requestBundle,replyBundle);
                break;
            case AvModule.CMD_ServicePlayer_AV_WaitAVPlayReady:
                replyBundle = AvCmdMiddle_WaitAVPlayReady(requestBundle,replyBundle);
                break;
            case AvModule.CMD_ServicePlayer_AV_ResetAudioDefaultLanguage:
                replyBundle = av_control_reset_audio_default_language(requestBundle,replyBundle);
                break;
            default:
                LogUtils.e("Command not implement");
                replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    private Bundle av_control_set_aspect_ratio(Bundle requestBundle, Bundle replyBundle) {
        int aspectRatio = requestBundle.getInt("AspectRatio",0);
        primeDtv.av_control_set_aspect_ratio(aspectRatio);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle setSetVisibleCompleted(Bundle inBundle,Bundle replyBundle) {
        boolean visibleCompiled = inBundle.getBoolean(AvModule.SetVisibleCompiled_string,false);
        primeDtv.setSetVisibleCompleted(visibleCompiled);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle av_control_get_play_status(Bundle inBundle,Bundle replyBundle) {
        int playId = inBundle.getInt(AvModule.PlayId_string,0);
        int status = primeDtv.av_control_get_play_status(playId);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putInt(AvModule.PlayStatus_string,status);
        return replyBundle;
    }
//
//    public int av_control_set_play_status(int status) {
//        try {
//            Bundle requestBundle = new Bundle(),replyBundle;
//            requestBundle.putInt(COMMAND_ID,AvModule.CMD_ServicePlayer_AV_SetPlayStatus);
//            requestBundle.putInt(AvModule.PlayStatus_string,status);
//            replyBundle = gPrimeDtvService.invokeBundle(requestBundle);
//            return replyBundle.getInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
//        } catch (RemoteException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public int av_control_play_stop_all() {
//        try {
//            Bundle requestBundle = new Bundle(),replyBundle;
//            requestBundle.putInt(COMMAND_ID,AvModule.CMD_ServicePlayer_AV_PlayStopAll);
//            replyBundle = gPrimeDtvService.invokeBundle(requestBundle);
//            return replyBundle.getInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
//        } catch (RemoteException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//

    public Bundle AvCmdMiddle_WaitAVPlayReady(Bundle inBundle,Bundle replyBundle) {
        Log.d("playercontrol","AvCmdMiddle_WaitAVPlayReady in");
        primeDtv.WaitAVPlayReady();
        Log.d("playercontrol","AvCmdMiddle_WaitAVPlayReady out");
        replyBundle.getInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
        return replyBundle;
    }
//
//    public void StopDVBStubtitle(){
//        Log.d(TAG,"StopDVBStubtitle() not porting");
//    }
//
    public Bundle av_control_play_stop(Bundle inBundle,Bundle replyBundle) {
        int tunerId = inBundle.getInt(ProgramInfo.TUNER_ID,0);
        int mode = inBundle.getInt(AvModule.Mode_string,0);
        int stop_monitor_table = inBundle.getInt(AvModule.StopMonitorTable_string,0);
        primeDtv.av_control_play_stop(tunerId,mode,stop_monitor_table);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle av_control_play_by_channel_id(Bundle inBundle,Bundle replyBundle) {
        int playId = inBundle.getInt(AvModule.PlayId_string,0);
        long channelId = inBundle.getLong(ProgramInfo.CHANNEL_ID,0);
        int groupType = inBundle.getInt(ProgramInfo.TYPE,0);
        int show = inBundle.getInt(AvModule.Show_string,0);
        primeDtv.av_control_play_by_channel_id(playId,channelId,groupType,show);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle av_control_change_channel_manager_tif_play_by_channel_id(Bundle inBundle, Bundle replyBundle) {
        int playId = inBundle.getInt(AvModule.PlayId_string,0);
        long channelId = inBundle.getLong(ProgramInfo.CHANNEL_ID,0);
        int groupType = inBundle.getInt(ProgramInfo.TYPE,0);
        boolean force = inBundle.getBoolean(AvModule.Force_string,false);
        PrimeDtvServiceApplication.getChannelChangeManager().change_channel_by_id(channelId,force);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putInt(AvModule.PlayId_string,PrimeDtvServiceApplication.getChannelChangeManager().getAVTunerID());
        return replyBundle;
    }

    public Bundle av_control_change_channel_manager_list_update(Bundle inBundle, Bundle replyBundle) {
        int groupType = inBundle.getInt(ProgramInfo.TYPE, FavGroup.ALL_TV_TYPE);
        PrimeDtvServiceApplication.getChannelChangeManager().update_all_channel(groupType);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }
//
//    public int AvControlPlayByChannelIdFCC(int mode, List<Long> ch_id_list, List<Integer> tuner_id_list, boolean channelBlocked){
//        Log.d(TAG,"AvControlPlayByChannelIdFCC() not porting");
//        return 0;
//    }
//
//    public int av_control_clear_fast_change_channel(int tunerId, long chId)
//    {
//        LogUtils.d("[FCC2] ");
//        Log.d(TAG,"AvControlPlayByChannelIdFCC() not porting");
//        return 0;
//    }
//
//    public void stopMonitorTable(long channel_id, int tuner_id){
//        LogUtils.d("AutoUpdateManager channel_id = "+channel_id+" tuner id = "+tuner_id);
//        Log.d(TAG,"AvControlPlayByChannelIdFCC() not porting");
//    }
//
//    public int av_control_set_fast_change_channel(int tunerId, long chId) {
//        Log.d(TAG,"av_control_set_fast_change_channel() not porting");
//        return 0;
//    }
    public Bundle av_control_get_audio_list_info(Bundle inBundle, Bundle replyBundle) {
        int playId = inBundle.getInt(AvModule.PlayId_string, 0);

        // 呼叫 PrimeDtv 實作
        AudioInfo audioInfo = primeDtv.av_control_get_audio_list_info(playId);

        if (audioInfo != null) {
            replyBundle.setClassLoader(AudioInfo.class.getClassLoader());
            replyBundle.putParcelable(COMMAND_REPLY_OBJ, audioInfo);
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        } else {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        }
        return replyBundle;
    }
    
    public Bundle av_control_change_audio(Bundle inBundle, Bundle replyBundle) {
        int playId = inBundle.getInt(AvModule.PlayId_string, 0);
    
        inBundle.setClassLoader(AudioInfo.class.getClassLoader());
        AudioInfo.AudioComponent component =
                inBundle.getParcelable(AvModule.AudioComponent_string, AudioInfo.AudioComponent.class);
    
        if (component == null) {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
            return replyBundle;
        }
    
        int result = primeDtv.av_control_change_audio(playId, component);
        replyBundle.putInt(COMMAND_REPLY_STATUS,
                (result == 0) ? COMMAND_REPLY_SUCCESS : COMMAND_REPLY_FAIL);
        return replyBundle;
    }

    public Bundle av_control_reset_audio_default_language(Bundle inBundle, Bundle replyBundle) {
        inBundle.setClassLoader(AudioInfo.class.getClassLoader());
        String lang = inBundle.getString(GposInfo.GPOS_AUDIO_LANG_SELECT_1, "");
        int result = primeDtv.av_control_reset_audio_default_language(lang);
        replyBundle.putInt(COMMAND_REPLY_STATUS,
                (result == 0) ? COMMAND_REPLY_SUCCESS : COMMAND_REPLY_FAIL);
        return replyBundle;
    }

    public Bundle subt_control_get_subtitle_list(Bundle inBundle, Bundle replyBundle) {
        int playId = inBundle.getInt(AvModule.PlayId_string, 0);
    
        SubtitleInfo subtitleInfo = primeDtv.av_control_get_subtitle_list(playId);
    
        replyBundle.setClassLoader(SubtitleInfo.class.getClassLoader());
        if (subtitleInfo != null) {
            replyBundle.putParcelable(COMMAND_REPLY_OBJ, subtitleInfo);
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        } else {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        }
        return replyBundle;
    }
    
    public Bundle subt_control_select_subtitle(Bundle inBundle, Bundle replyBundle) {
        int playId = inBundle.getInt(AvModule.PlayId_string, 0);
    
        inBundle.setClassLoader(SubtitleInfo.class.getClassLoader());
        SubtitleInfo.SubtitleComponent component =
                inBundle.getParcelable(AvModule.SubtitleComponent_string,
                        SubtitleInfo.SubtitleComponent.class);
    
        if (component == null) {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
            return replyBundle;
        }
    
        int result = primeDtv.av_control_select_subtitle(playId, component);
        replyBundle.putInt(COMMAND_REPLY_STATUS,
                (result == 0) ? COMMAND_REPLY_SUCCESS : COMMAND_REPLY_FAIL);
        return replyBundle;
    }
    
    public Bundle subt_control_stop_subtitle(Bundle inBundle, Bundle replyBundle) {
        int playId = inBundle.getInt(AvModule.PlayId_string, 0);
        int result = primeDtv.StopDVBSubtitle();
        replyBundle.putInt(COMMAND_REPLY_STATUS,
                (result == 0) ? COMMAND_REPLY_SUCCESS : COMMAND_REPLY_FAIL);
        return replyBundle;
    }
    
    public Bundle set_surface(Bundle inBundle,Bundle replyBundle) {
        LogUtils.d("FCC_LOG AvModuleCommand set_surface: enter pid=" + Binder.getCallingPid()
                + " uid=" + Binder.getCallingUid());
        int index = inBundle.getInt(MiscDefine.Index,0);
        Surface surface = inBundle.getParcelable(AvModule.Surface_string, Surface.class);
        LogUtils.d("FCC_LOG AvModuleCommand set_surface: index=" + index
                + " surface=" + surface
                + " surfaceId=" + (surface == null ? 0 : System.identityHashCode(surface)));
        primeDtv.set_surface(null,surface,index);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }
}
