package com.prime.dtv.service.Scan;

import static com.prime.dtv.service.Table.MpsMessage.bytesToHex;
import static com.prime.dtv.service.Table.MpsMessage.bytesToHexWithSpaces;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.text.AutoGrowArray;

import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Table.Cat;
import com.prime.dtv.service.Table.CatData;
import com.prime.dtv.service.Table.Emm;
import com.prime.dtv.service.Table.MpsMessage;
import com.prime.dtv.service.Table.PrivateSectionData;
import com.prime.dtv.sysdata.DTVMessage;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.LogUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class EmmUpdater extends Updater{
    private static final String TAG = "EmmUpdater";
    private Emm mEMMTable = null;
    private boolean DEBUG = true;

    private PesiDtvFrameworkInterfaceCallback mCallback;

    public EmmUpdater(Context context, PesiDtvFrameworkInterfaceCallback callback, long channel_id) {
        super(context, channel_id);
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
        while(true){
            if(IsStop() == true){
                LogUtils.d("Stop process EMM");
                stop_emm();
                break;
            }
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

                                        LogUtils.d("mCommandType = " + mpsDataUnit.mMpsData.mCommandType);
                                        LogUtils.d("mCommandLength = " + mpsDataUnit.mMpsData.mCommandLength);
                                        LogUtils.d("mZipCode = " + bytesToHexWithSpaces(mpsDataUnit.mMpsData.mZipCode));
                                        LogUtils.d("mStbModel = " + bytesToHexWithSpaces(mpsDataUnit.mMpsData.mStbModel));
                                        LogUtils.d("mDelete = " + mpsDataUnit.mMpsData.mDelete);
                                        LogUtils.d("mMaxDelaySec = " + mpsDataUnit.mMpsData.mMaxDelaySec);
                                        LogUtils.d(" Process EMM get mpsData end");
                                    }
                                    if(IsOurMPSdata(mpsDataUnit)){
                                        int deleteflag = mpsDataUnit.mMpsData.mDelete;
                                        int delay = mpsDataUnit.mMpsData.mMaxDelaySec;
                                        LogUtils.d("sendCallbackMessage ==> PESI_EVT_CA_WIDEVINE_REFRESH_CAS_DATA "+getChannelId());
                                        mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_CA_WIDEVINE_REFRESH_CAS_DATA,deleteflag, delay,getChannelId());
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
            String device_id = SystemProperties.get("ro.boot.cstmsnno");//Build.getSerial();
            String tmp_id = new String(mpsDataUnit.mDeviceId.deviceId, StandardCharsets.UTF_8);
            LogUtils.d("device_id ="+device_id+ " " +tmp_id);
            if(tmp_id.equalsIgnoreCase(device_id)){
                result = true;
            }
        }
        else if(mpsDataUnit.mTargeType == MpsMessage.TARGET_TYPE_ZIPCODE){
            String zip_code = new String(mpsDataUnit.mDivisionCode.divisionCode, StandardCharsets.UTF_8);
            if(zip_code.equals(mDataManager.getGposInfo().getZipCode())){
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
            if(gpos.getIrdCommand(i) == sequenceNumber){
                LogUtils.d("The Command "+sequenceNumber+" is executed !");
                break;
            }
            if(gpos.getIrdCommand(i) == 0){
                LogUtils.d(sequenceNumber + " is new command");
                gpos.setIrdCommand(i, sequenceNumber);
                result = true;
                break;
            }
        }
        if(i == GposInfo.MAX_NUM_OF_IRD_COMMAND){
            LogUtils.d("There is "+GposInfo.MAX_NUM_OF_IRD_COMMAND+" commands in the list, but not found "+sequenceNumber);
            gpos.setIrdCommand(GposInfo.MAX_NUM_OF_IRD_COMMAND, sequenceNumber);
            result = true;
        }
        LogUtils.d("CheckComandSent => "+result);
        return result;
    }
}
