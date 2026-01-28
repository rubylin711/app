package com.prime.dtvservice.Interface;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_STATUS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;

import android.os.Bundle;
import android.os.RemoteException;

import com.prime.datastructure.CommuincateInterface.EpgModule;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVTunerParams;
import com.prime.dtv.PrimeDtv;

public class EpgModuleCommand {
    private PrimeDtv primeDtv = null;
    public Bundle executeCommand(Bundle requestBundle,Bundle replyBundle,PrimeDtv primeDtv) {
        this.primeDtv = primeDtv;
        int command_id = requestBundle.getInt(COMMAND_ID,0);
        LogUtils.d("command_id = "+command_id);
        switch(command_id) {
            case EpgModule.CMD_ServicePlayer_EPG_SetupEpgChannel:
                replyBundle = setup_epg_channel(requestBundle,replyBundle);
                break;
            case EpgModule.CMD_ServicePlayer_EPG_StartEpg:
                replyBundle = start_epg(requestBundle,replyBundle);
                break;
            default:
                LogUtils.e("Command not implement");
                replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    public Bundle setup_epg_channel(Bundle requestBundle,Bundle replyBundle) {
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        primeDtv.setup_epg_channel();
        return replyBundle;
    }

    public Bundle start_epg(Bundle requestBundle,Bundle replyBundle) {
        long channelID = requestBundle.getLong(ProgramInfo.CHANNEL_ID, 0);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        primeDtv.start_epg(channelID);
        return replyBundle;
    }

//    public void start_schedule_eit(int tp_id,int tuner_id) {
//        // use tuner framework to get epg raw data
//        LogUtils.d("[Ethan] start_schedule_eit");
//        try {
//            Bundle requestBundle = new Bundle(),replyBundle;
//            requestBundle.putInt(COMMAND_ID, EpgModule.CMD_ServicePlayer_EPG_StartScheduleEit);
//            requestBundle.putInt(TpInfo.TP_ID, tp_id);
//            requestBundle.putInt(TpInfo.TUNER_ID, tuner_id);
//            replyBundle = gPrimeDtvService.invokeBundle(requestBundle);
//            replyBundle.getInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
//        } catch (RemoteException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void stop_schedule_eit() {
//        LogUtils.d("[Ethan] stop_schedule_eit");
////        gDtvFramework.stopScheduleEit();
////        try {
////            Bundle requestBundle = new Bundle(),replyBundle;
////            requestBundle.putInt(COMMAND_ID, EpgModule.CMD_ServicePlayer_EPG_StopScheduleEit);
////            replyBundle = gPrimeDtvService.invokeBundle(requestBundle);
////            replyBundle.getInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
////        } catch (RemoteException e) {
////            throw new RuntimeException(e);
////        }
//    }
//
//    public void setup_epg_channel() {
//        try {
//            Bundle requestBundle = new Bundle(),replyBundle;
//            requestBundle.putInt(COMMAND_ID, EpgModule.CMD_ServicePlayer_EPG_SetupEpgChannel);
//            replyBundle = gPrimeDtvService.invokeBundle(requestBundle);
//            replyBundle.getInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
//        } catch (RemoteException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
