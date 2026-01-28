package com.prime.dtv.service.Scan;

import static com.prime.dtv.service.Table.MpsMessage.bytesToHexWithSpaces;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;

import com.prime.datastructure.ServiceDefine.AvCmdMiddle;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.ServiceInterface;
import com.prime.dtv.service.CNS.IrdCommand;
import com.prime.dtv.service.Table.Cat;
import com.prime.dtv.service.Table.CatData;
import com.prime.dtv.service.Table.Emm;
import com.prime.dtv.service.Table.MpsMessage;
import com.prime.dtv.service.Table.PrivateSectionData;
import com.prime.datastructure.sysdata.DTVMessage;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.ProgramInfo;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class EmmUpdater extends Updater{
    private static final String TAG = "EmmUpdater";
    private Emm mEMMTable = null;
    private boolean DEBUG = true;
    private Context mContext;

    private PesiDtvFrameworkInterfaceCallback mCallback;

    public EmmUpdater(Context context, PesiDtvFrameworkInterfaceCallback callback, long channel_id) {
        super(context, channel_id);
        this.mContext = context;
        mCallback = callback;
        mEMMTable = null;
    }
    private void stop_emm(){
        try{
            if(mEMMTable != null){
                LogUtils.d("Stop EMM");
                mEMMTable.abort();
                mEMMTable = null;
            }
        }catch (Exception e){
			mEMMTable = null;
            //LogUtils.d("Exception "+e.toString());
        }
    }
    void stop(){
        super.stop();
        stop_emm();
    }
    @Override
    protected void proecee(long channel_id) {
        ProgramInfo programInfo = mDataManager.getProgramInfo(channel_id);
        PrimeDtv primeDtv = ServiceInterface.get_prime_dtv();
        while(true){
            if(Pvcfg.getCAType() == Pvcfg.CA_TYPE.CA_IRDETO) {
                if (primeDtv.av_control_get_play_status(0) == AvCmdMiddle.PESI_SVR_AV_RELEASE_STATE ||
                        primeDtv.av_control_get_play_status(0) == AvCmdMiddle.PESI_SVR_AV_IDLE_STATE ||
                        primeDtv.av_control_get_play_status(0) == AvCmdMiddle.PESI_SVR_AV_STOP_STATE ||
                        primeDtv.av_control_get_play_status(0) == AvCmdMiddle.PESI_SVR_AV_PAUSE_STATE) {
                    Log.d(TAG, "mTuner.openDescrambler() not ready");
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }
            }
            if(IsStop() == true){
                LogUtils.d("Stop process EMM");
                stop_emm();
                break;
            }
            if(Pvcfg.getCAType() == Pvcfg.CA_TYPE.CA_IRDETO)
                 process_cat(programInfo);
            process_emm(programInfo);
            if(IsStop() == true){
                LogUtils.d("Stop process EMM");
                stop_emm();
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void process_cat(ProgramInfo programInfo){
        Log.d(TAG,"process_cat !");
        Cat pcat = new Cat(programInfo.getTunerId());
        pcat.processWait();
        CatData pcatdata = pcat.getCatData();
        if(DEBUG)
            LogUtils.d("pcatdata = "+pcatdata);
        if(pcatdata != null ){
            if(pcatdata.getRawData() != null && pcatdata.getRawDataLen() != 0){
                //LogUtils.d("@@@### PESI_EVT_SYSTEM_CAT_UPDATE_VERSION");
                mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_CAT_UPDATE_VERSION,0,0 ,pcatdata.getRawData());
            }
            else {
                //LogUtils.d("@@@### No cat data");
            }
        }
        else {
            //LogUtils.d("@@@### pcatdata == null");
        }
    }

    private void process_emm(ProgramInfo programInfo){
        if(mEMMTable == null && programInfo != null){
            Cat pcat = new Cat(getTunerId());
            pcat.processWait();
            CatData pcatdata = pcat.getCatData();
            if(DEBUG)
                LogUtils.d("pcatdata = "+pcatdata);
            if(pcatdata != null) {
                List<CatData.CaEmmData> caEmmDataList = pcatdata.getCaEmmDataList();
                for (CatData.CaEmmData emmdata : caEmmDataList) {
                    if(DEBUG)
                        LogUtils.d("CaSystemId = "+emmdata.getCaSystemId());
                    if (emmdata.getCaSystemId() == Pvcfg.ALTIMEDIA_CA_SYSTEM_ID) {
                        LogUtils.d("New EMM channel ID "+programInfo.getChannelId()+" name = "+programInfo.getDisplayName());
                        LogUtils.d("EMM PID = "+emmdata.getEmmPid());
                        mEMMTable = new Emm(getTunerId(), emmdata.getEmmPid());
                    }
                }
            }
            pcat.abort();
            pcat = null;
        }
        if(mEMMTable != null){
            // handle MPS data
            PrivateSectionData PSData = mEMMTable.getEmmData();
            //if(DEBUG)
            //    LogUtils.d("PrivateSectionData = "+PSData);
            if(PSData != null){
                int Cnt = PSData.getNumOfPrivateData();
                if(DEBUG)
                    LogUtils.d("NumOfPrivateData = "+Cnt);
                for (int i = 0; i < Cnt; i++) {
                    if(IsStop() == true){
                        break;
                    }
                    if(PSData.getPrivateDataByIndex(i).mSectionLength > 0){
                        MpsMessage mpsMessage=new MpsMessage(PSData.getPrivateDataByIndex(i).mPrivateDataByte,PSData.getPrivateDataByIndex(i).mSectionLength);
                        int num = mpsMessage.getNumOfMpsDataUnit();
                        LogUtils.d("NumOfMpsDataUnit = "+num);
                        for(int j =0 ; j< num ; j++){
                            MpsMessage.MpsDataUnit mpsDataUnit = mpsMessage.getMpsDataUnitByIndex(j);
                            if(IsStop() == true){
                                break;
                            }
                            if(mpsDataUnit != null) {
                                if(mpsDataUnit.mMpsData != null) {
                                    if(DEBUG) {
                                        LogUtils.d(" Process EMM get mpsData start");
                                        LogUtils.d("mTargeType = " + mpsDataUnit.mTargeType);
                                        if(mpsDataUnit.mTargeType == MpsMessage.TARGET_TYPE_DEVICE_ID){
                                            LogUtils.d("mDeviceId.deviceIdLen = " + mpsDataUnit.mDeviceId.deviceIdLen);
                                            LogUtils.d("mDeviceId.deviceId = " + bytesToHexWithSpaces(mpsDataUnit.mDeviceId.deviceId));
                                        }
//                                        LogUtils.d("commandData = " + bytesToHexWithSpaces(mpsDataUnit.mMpsData.commandData));
                                        LogUtils.d(" Process EMM get mpsData end");
                                    }
                                    if(IsOurMPSdata(mpsDataUnit)){
                                        long ird_code = mpsDataUnit.mIrdCode;
                                        LogUtils.d("ird_code ="+ird_code);
                                        if(ird_code == MpsMessage.MpsDataUnit.CNS_IRD_CODE_CA_MAIL) {
//                                            LogUtils.d("LLLLLL sendCallbackMessage ==> CNS_IRD_CODE_CA_MAIL" +
//                                                    " Data = " + bytesToHexWithSpaces(mpsDataUnit.mMpsData.commandData));
                                            mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_CA_WIDEVINE_CA_MAIL,0, 0,mpsDataUnit.mMpsData.commandData);
                                        }
                                        else if(ird_code == MpsMessage.MpsDataUnit.CNS_IRD_CODE_EMERGENCY_ALARM) {
//                                            LogUtils.d("LLLLLL sendCallbackMessage ==> CNS_IRD_CODE_EMERGENCY_ALARM" +
//                                                    " Data = " + bytesToHexWithSpaces(mpsDataUnit.mMpsData.commandData));
                                            mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_CA_WIDEVINE_EMERGENCY_ALARM,0, 0,mpsDataUnit.mMpsData.commandData);
                                        }
                                        else if(ird_code == MpsMessage.MpsDataUnit.CNS_IRD_CODE_IRC_COMMAND) {
//                                            LogUtils.d("LLLLLL sendCallbackMessage ==> CNS_IRD_CODE_IRC_COMMAND" +
//                                                " Data = " + bytesToHexWithSpaces(mpsDataUnit.mMpsData.commandData));
                                            LogUtils.d("commandData "+bytesToHexWithSpaces(mpsDataUnit.mMpsData.commandData));
                                            IrdCommand irdCommand = new IrdCommand(mContext, mpsDataUnit.mMpsData.commandData, getChannelId());
                                            mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_CA_WIDEVINE_IRD_COMMAND,0, 0,irdCommand);
                                        }
                                    }
                                }else{
                                    if(DEBUG)
                                        LogUtils.e("mMpsData is NULL");
                                }
                            }else{
                                if(DEBUG)
                                    LogUtils.e("mpsDataUnit is NULL");
                            }
                        }
                    }
                    else{
                        //LogUtils.d("LLLLLL  mSectionLength <= 0");
                    }
                }
                if(mEMMTable != null)
                    mEMMTable.cleanEmmData();
            }
        }
    }

    private boolean IsOurMPSdata(MpsMessage.MpsDataUnit mpsDataUnit){
        boolean result = false;
        if(mpsDataUnit.mTargeType == MpsMessage.TARGET_TYPE_ALL) {
            result = true;
        }
        else if(mpsDataUnit.mTargeType == MpsMessage.TARGET_TYPE_DEVICE_ID){
            String device_id = Build.getSerial();
            String tmp_id = new String(mpsDataUnit.mDeviceId.deviceId, StandardCharsets.UTF_8);
            LogUtils.d("stb device_id ="+device_id+ " mpsDataUnit device_id = " +tmp_id);
            //if(tmp_id.equalsIgnoreCase(device_id))//ruby test remove 20260126
            {
                result = true;
            }
        }
        else if(mpsDataUnit.mTargeType == MpsMessage.TARGET_TYPE_ZIPCODE){
            String zip_code = new String(mpsDataUnit.mDivisionCode.divisionCode, StandardCharsets.UTF_8);
            if(zip_code.equals(GposInfo.getZipCode(mContext))){
                result = true;
            }
        }

        if(result){
            LogUtils.d("The command is sent to us.");
            result = CheckComandSent(mpsDataUnit);
        }
        return result;
    }

    private boolean CheckComandSent(MpsMessage.MpsDataUnit mpsDataUnit){
        boolean result = false;
        int i;
        long sequenceNumber =  mpsDataUnit.mSequenceNumber;
        GposInfo gpos = mDataManager.getGposInfo();
        for(i=0 ; i<GposInfo.MAX_NUM_OF_IRD_COMMAND ; i++){
            if(GposInfo.getIrdCommand(mContext, i) == sequenceNumber){
                LogUtils.d("The Command "+sequenceNumber+" is executed !");
                result = true;//ruby test 20260127
                break;
            }
            if(GposInfo.getIrdCommand(mContext, i) == 0){
                LogUtils.d(sequenceNumber + " is new command");
                GposInfo.setIrdCommand(mContext, i, sequenceNumber);
                result = true;
                break;
            }
        }
        if(i == GposInfo.MAX_NUM_OF_IRD_COMMAND){
            LogUtils.d("There is "+GposInfo.MAX_NUM_OF_IRD_COMMAND+" commands in the list, but not found "+sequenceNumber);
            GposInfo.setIrdCommand(mContext, GposInfo.MAX_NUM_OF_IRD_COMMAND, sequenceNumber);
            result = true;
        }
        LogUtils.d("CheckComandSent => "+result);
        return result;
    }
}
