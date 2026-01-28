package com.prime.dtvservice.Interface;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_OBJ;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_STATUS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.prime.datastructure.CommuincateInterface.FeModule;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVTunerParams;
import com.prime.dtv.PrimeDtv;

public class FeModuleCommand {
    private PrimeDtv primeDtv = null;
    public Bundle executeCommand(Bundle requestBundle,Bundle replyBundle,PrimeDtv primeDtv) {
        this.primeDtv = primeDtv;
        int command_id = requestBundle.getInt(COMMAND_ID,0);
        LogUtils.d("command_id = "+command_id);
        switch(command_id) {
            case FeModule.CMD_ServicePlayer_FE_GetTunerStatus:
                replyBundle = get_tuner_status(requestBundle,replyBundle);
                break;
            case FeModule.CMD_ServicePlayer_FE_TunerInit:
                replyBundle = init_tuner(requestBundle,replyBundle);
                break;
            case FeModule.CMD_ServicePlayer_FE_TunerTune:
                replyBundle = tuner_lock(requestBundle, replyBundle);
                break;
            case FeModule.CMD_ServicePlayer_FE_GetSignalStrength:
                replyBundle = get_signal_strength(requestBundle, replyBundle);
                break;
            case FeModule.CMD_ServicePlayer_FE_GetSignalQuality:
                replyBundle = get_signal_quality(requestBundle, replyBundle);
                break;
            case FeModule.CMD_ServicePlayer_FE_GetSignalSNR:
                replyBundle = get_signal_snr(requestBundle, replyBundle);
                break;
            case FeModule.CMD_ServicePlayer_FE_GetSignalBER:
                replyBundle = get_signal_ber(requestBundle, replyBundle);
                break;
            default:
                LogUtils.e("Command not implement");
                replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    public Bundle get_tuner_status(Bundle requestBundle,Bundle replyBundle) {
        int tunerId = requestBundle.getInt(TpInfo.TUNER_ID,0);
        boolean isLock = primeDtv.get_tuner_status(tunerId);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putBoolean(TVTunerParams.IsLock,isLock);
        return replyBundle;
    }

    public Bundle init_tuner(Bundle requestBundle,Bundle replyBundle) {
        primeDtv.init_tuner();
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle tuner_lock(Bundle requestBundle, Bundle replyBundle) {
        TVTunerParams tvTunerParams =
                requestBundle.getParcelable(TVTunerParams.TAG, TVTunerParams.class);
        Log.d("TAG", "tuner_lock: " + tvTunerParams);
        int result = primeDtv.tuner_lock(tvTunerParams);
        if (result == 0) { // 0 = tune success
            replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        } else {
            replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

//
//    public int get_tuner_type() {
//        try {
//            int tunerType = 0;
//            Bundle requestBundle = new Bundle(),replyBundle;
//            requestBundle.putInt(COMMAND_ID, FeModule.CMD_ServicePlayer_FE_GetTunerType);
//            replyBundle = gPrimeDtvService.invokeBundle(requestBundle);
//            if(replyBundle.getInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
//                tunerType = replyBundle.getInt(TpInfo.TUNER_TYPE,0);
//            return tunerType;
//        } catch (RemoteException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
    public Bundle get_signal_strength(Bundle requestBundle,Bundle replyBundle) {
        int tunerId = requestBundle.getInt(TpInfo.TUNER_ID,0);
        int strength = primeDtv.get_signal_strength(tunerId);
        LogUtils.d("[Ethan] strength = "+strength);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putInt(FeModule.Strength_string,strength);
        return replyBundle;
    }

    public Bundle get_signal_quality(Bundle requestBundle,Bundle replyBundle) {
        int tunerId = requestBundle.getInt(TpInfo.TUNER_ID,0);
        int quality = primeDtv.get_signal_quality(tunerId);
        LogUtils.d("[Ethan] quality = "+quality);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putInt(FeModule.Quality_string,quality);
        return replyBundle;
    }

    public Bundle get_signal_snr(Bundle requestBundle,Bundle replyBundle) {
        int tunerId = requestBundle.getInt(TpInfo.TUNER_ID,0);
        int snr = primeDtv.get_signal_snr(tunerId);
        LogUtils.d("[Ethan] snr = "+snr);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putInt(FeModule.SNR_string,snr);
        return replyBundle;
    }

    public Bundle get_signal_ber(Bundle requestBundle,Bundle replyBundle) {
        int tunerId = requestBundle.getInt(TpInfo.TUNER_ID,0);
        int ber = primeDtv.get_signal_ber(tunerId);
        LogUtils.d("[Ethan] ber = "+ber);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putInt(FeModule.BER_string,ber);
        return replyBundle;
    }
}
