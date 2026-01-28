package com.prime.dtvservice.Interface;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_OBJ;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_STATUS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_DeleteProgram;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_GetProgramByChannelId;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_GetProgramInfoList;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_TpInfoGetListBySatId;

import com.prime.datastructure.CommuincateInterface.MiscDefine;
import com.prime.datastructure.CommuincateInterface.PmModule;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.FavInfo;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.MusicInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.JsonParser;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.PrimeDtv;
import com.prime.dtvservice.PrimeDtvService;
import com.prime.dtvservice.PrimeDtvServiceApplication;

import java.util.ArrayList;
import java.util.List;

public class PmModuleCommand {
    public final static String TAG = "PmModuleCommand";
    private PrimeDtv primeDtv = null;
    public Bundle executeCommand(Bundle requestBundle,Bundle replyBundle,PrimeDtv primeDtv) {
        this.primeDtv = primeDtv;
        int command_id = requestBundle.getInt(COMMAND_ID,0);
        LogUtils.d("command_id = "+command_id);
        switch(command_id) {
            case PmModule.CMD_ServicePlayer_PM_GetProgramBySIdOnIdTsId:
                LogUtils.d("CMD_ServicePlayer_PM_GetProgramBySIdOnIdTsId");
                replyBundle = GetProgramBySIdOnIdTsId(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_GetProgramByChannelId:
                replyBundle = GetProgramByChannelId(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_GetProgramByChnum:
                replyBundle = get_program_by_ch_num(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_GetProgramInfoList:
                replyBundle = get_program_info_list(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_TpInfoGet:
                replyBundle = TpInfoGet(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_GposInfoUpdate:
                replyBundle = GposInfoUpdate(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_GposSaveKeyValueInteger:
                replyBundle = gpos_info_update_by_key_string_int(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_GposSaveKeyValueString:
                replyBundle = gpos_info_update_by_key_string_string(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_GposInfoGet:
                replyBundle = GposInfoGet(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_TpInfoGetListBySatId:
                replyBundle = tp_info_get_list_by_satId(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_TpInfoAdd:
                replyBundle = tp_info_add(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_TpInfoUpdate:
                replyBundle = tp_info_update(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_UpdateProgramInfo:
                replyBundle = update_program_info(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_SetDefaultOpenChannel:
                replyBundle = set_default_open_channel(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_CategoryUpdateToFav:
                replyBundle = category_update_to_fav(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_SaveTable:
                replyBundle = save_table(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_BackupDatabase:
                replyBundle = backupDatabase(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_SetTvInputId:
                replyBundle = set_tv_input_id(requestBundle,replyBundle);
                break;
            case PmModule.CMD_ServicePlayer_PM_GetTvInputId:
                replyBundle = get_tv_input_id(requestBundle,replyBundle);
                break;
            default:
                LogUtils.e("Command not implement");
                replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    public Bundle GetProgramBySIdOnIdTsId(Bundle requestBundle,Bundle replyBundle) {
        int sid = requestBundle.getInt(ProgramInfo.SERVICE_ID,0);
        int onid = requestBundle.getInt(ProgramInfo.ORIGINAL_NETWORK_ID,0);
        int tsid = requestBundle.getInt(ProgramInfo.TRANSPORT_STREAM_ID,0);
        ProgramInfo programInfo = primeDtv.get_program_by_sid_onid_tsid(sid,onid,tsid);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putParcelable(COMMAND_REPLY_OBJ,programInfo);
        Log.d(TAG," CMD_ServicePlayer_PM_GetProgramBySIdOnIdTsId programInfo = "+programInfo);
        return replyBundle;
    }

    public Bundle GetProgramByChannelId(Bundle requestBundle,Bundle replyBundle) {
        long channel_id = requestBundle.getLong(ProgramInfo.CHANNEL_ID,0);
        ProgramInfo programInfo = primeDtv.get_program_by_channel_id(channel_id);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putParcelable(COMMAND_REPLY_OBJ,programInfo);
        return replyBundle;
    }

    public Bundle get_program_by_ch_num(Bundle requestBundle,Bundle replyBundle) {
        int num = requestBundle.getInt(ProgramInfo.DISPLAY_NUM,0);
        int type = requestBundle.getInt(ProgramInfo.TYPE,0);
        ProgramInfo programInfo = primeDtv.get_program_by_ch_num(num,type);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putParcelable(COMMAND_REPLY_OBJ,programInfo);
        return replyBundle;
    }

    public Bundle get_program_info_list(Bundle requestBundle,Bundle replyBundle) {
        int type = requestBundle.getInt(FavInfo.FAV_MODE,0);
        int pos = requestBundle.getInt(MiscDefine.POS,0);
        int num = requestBundle.getInt(MiscDefine.Num,0);
        ArrayList<ProgramInfo> programInfoArrayList = new ArrayList<>(primeDtv.get_program_info_list(type,pos,num));
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putParcelableArrayList(COMMAND_REPLY_OBJ,programInfoArrayList);
        return replyBundle;
    }

    public Bundle TpInfoGet(Bundle requestBundle,Bundle replyBundle) {
        int tp_id = requestBundle.getInt(TpInfo.TP_ID,-1);
        if(tp_id == -1) {
            replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
            return replyBundle;
        }
//        replyBundle.setClassLoader(TpInfo.class.getClassLoader());
        TpInfo tpInfo = primeDtv.tp_info_get(tp_id);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putParcelable(COMMAND_REPLY_OBJ,tpInfo);
        return replyBundle;
    }

    public Bundle tp_info_get_list_by_satId(Bundle requestBundle,Bundle replyBundle) {
        ArrayList<TpInfo> tpInfoList;
//        replyBundle.setClassLoader(TpInfo.class.getClassLoader());
        int tunerType = requestBundle.getInt(TpInfo.TUNER_TYPE,0);
        int satId = requestBundle.getInt(TpInfo.SAT_ID,0);
        int pos = requestBundle.getInt(MiscDefine.POS,0);
        int num = requestBundle.getInt(MiscDefine.POS,0);
        LogUtils.d("[Ethan]");
        tpInfoList = new ArrayList<>(primeDtv.tp_info_get_list_by_satId(tunerType,satId,pos,num));
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        LogUtils.d("[Ethan] tpInfoList size = "+tpInfoList.size());
        replyBundle.putParcelableArrayList(COMMAND_REPLY_OBJ,tpInfoList);
        return replyBundle;
    }

    public Bundle tp_info_add(Bundle requestBundle,Bundle replyBundle) {
        TpInfo tpInfo = requestBundle.getParcelable(TpInfo.TAG,TpInfo.class);
        int tp_id = primeDtv.tp_info_add(tpInfo);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putInt(TpInfo.TP_ID,tp_id);
        return replyBundle;
    }

    public Bundle tp_info_update(Bundle requestBundle,Bundle replyBundle) {
        TpInfo tpInfo = requestBundle.getParcelable(TpInfo.TAG,TpInfo.class);
        primeDtv.tp_info_update(tpInfo);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle GposInfoUpdate(Bundle requestBundle,Bundle replyBundle) {
        GposInfo gposInfo = requestBundle.getParcelable(GposInfo.TAG,GposInfo.class);
        primeDtv.gpos_info_update(gposInfo);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle gpos_info_update_by_key_string_int(Bundle requestBundle,Bundle replyBundle) {
        String keyName = requestBundle.getString(PmModule.KeyName_String," ");
        int value = requestBundle.getInt(keyName,0);
        primeDtv.gpos_info_update_by_key_string(keyName,value);
        primeDtv.saveGposKeyValue(keyName,value);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle gpos_info_update_by_key_string_string(Bundle requestBundle,Bundle replyBundle) {
        String keyName = requestBundle.getString(PmModule.KeyName_String," ");
        String value = requestBundle.getString(keyName,"");
        primeDtv.gpos_info_update_by_key_string(keyName,value);
        primeDtv.saveGposKeyValue(keyName,value);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle GposInfoGet(Bundle requestBundle,Bundle replyBundle) {
        GposInfo gposInfo = primeDtv.gpos_info_get();
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putParcelable(COMMAND_REPLY_OBJ,gposInfo);
        return replyBundle;
    }

    public Bundle update_program_info(Bundle requestBundle,Bundle replyBundle) {
        ProgramInfo programInfo = requestBundle.getParcelable(ProgramInfo.TAG,ProgramInfo.class);
        primeDtv.update_program_info(programInfo);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle set_default_open_channel(Bundle requestBundle,Bundle replyBundle) {
        long channelId = requestBundle.getLong(ProgramInfo.CHANNEL_ID,0);
        int groupType = requestBundle.getInt(ProgramInfo.TYPE,0);
        primeDtv.set_default_open_channel(channelId,groupType);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle category_update_to_fav(Bundle requestBundle,Bundle replyBundle) {
        List<ProgramInfo> ProgramInfoList = requestBundle.getParcelableArrayList(MiscDefine.ProgramInfoList_string,ProgramInfo.class);
        List<MusicInfo> musicInfoList = requestBundle.getParcelableArrayList(MiscDefine.MusicInfoList_string,MusicInfo.class);
        primeDtv.category_update_to_fav(ProgramInfoList,musicInfoList);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle get_current_category(Bundle requestBundle,Bundle replyBundle) {
        ArrayList<MusicInfo> musicInfoList = new ArrayList<>();
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putParcelableArrayList(MiscDefine.MusicInfoList_string,musicInfoList);
        return replyBundle;
    }

    public Bundle save_table(Bundle requestBundle,Bundle replyBundle) {
        String tableName = requestBundle.getString(MiscDefine.EnTableType_string,EnTableType.ALL.name());
        EnTableType tableType = EnTableType.valueOf(tableName);
        primeDtv.save_table(tableType);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle backupDatabase(Bundle requestBundle,Bundle replyBundle) {
        boolean force = requestBundle.getBoolean(MiscDefine.Force_string,false);
        primeDtv.backupDatabase(force);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle set_tv_input_id(Bundle inBundle,Bundle replyBundle) {
        String tvInputId = inBundle.getString(PmModule.TvInputId_String,"");
        primeDtv.set_tv_input_id(tvInputId);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle get_tv_input_id(Bundle inBundle,Bundle replyBundle) {
        String tvInputId = primeDtv.av_control_get_tv_input_id();
        replyBundle.putString(PmModule.TvInputId_String,tvInputId);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }
}
