package com.prime.dtv.service.Scan;

import static com.prime.dtv.service.Table.PmtData.PMT_MAX_AUDIO_PID_NUM;

import android.content.Context;
import android.os.SystemProperties;

import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Table.Pat;
import com.prime.dtv.service.Table.PatData;
import com.prime.dtv.service.Table.Pmt;
import com.prime.dtv.service.Table.PmtData;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.sysdata.DTVMessage;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PmtUpdater extends Updater {
    private static final String TAG = "PmtUpdater";
    private static final int PMT_NO_CHANGED = 0;
    private static final int PMT_SUB_LANG_CHANGED = 1;
    private static final int PMT_AUD_LANG_CHANGED = 2;
    private static final int PMT_AVPID_CHANGED = 4;
    private static final int PMT_VERSION_CHANGED = 8;
    private static final boolean DEBUG_PMT = SystemProperties.getBoolean("persist.sys.prime.debug.pmt_update",false);
    private static final boolean DEBUG_SUB_LANG_CHANGED = SystemProperties.getBoolean("persist.sys.prime.debug.pmt.sublang",false);
    private static final boolean DEBUG_AUD_LANG_CHANGED = SystemProperties.getBoolean("persist.sys.prime.debug.pmt.audlang",false);
    private static final boolean DEBUG_AV_PID_CHANGED = SystemProperties.getBoolean("persist.sys.prime.debug.pmt.avpid",false);

    private static final int PMT_CA_INFO_CHANGED = 8;
    private static final int NAGRA_NETWORK_ID = 0x31;
    private static final int CTI_NETWORK_ID = 0x457;
    private int IsPMTVersionChange;
    private int IsAVPidChanged;
    private int IsSubtitleChanged;
    private int IsAudioLanguageChanged;
    private int mIsFcc = 0;
    private PesiDtvFrameworkInterfaceCallback mCallback;
    private int not_found_service_count = 0;
//    private SIUpdater mSIUpdater;
//    private EmmUpdater mEmmUpdater;

    public PmtUpdater(Context context, PesiDtvFrameworkInterfaceCallback callback, long channel_id, int isFcc) {
        super(context, channel_id);

        mCallback = callback;
        IsPMTVersionChange = 0;
        IsAVPidChanged = 0;
        IsSubtitleChanged = 0;
        IsAudioLanguageChanged = 0;
        //mSIUpdater = null;
        mIsFcc = isFcc;
//        mEmmUpdater = new EmmUpdater(context, mCallback, channel_id);
//        mSIUpdater = new SIUpdater(context, mCallback, channel_id);
//        if(mIsFcc == 0) {
//            if(Pvcfg.isProcessEMM()) {
//                mEmmUpdater.start();
//            }
//            if(Pvcfg.isSiupdateEnable()) {
//                mSIUpdater.start();
//            }
//        }
    }
    public void check_si_emm_stop(){

//        if((mIsFcc == 1) && (mSIUpdater != null)){
//            mSIUpdater.stop();
//            //mSIUpdater = null;
//        }
//        if((mIsFcc == 1) && (mEmmUpdater != null)){
//            mEmmUpdater.stop();
//            //mEmmUpdater = null;
//        }
    }
    public void setIsFcc(int isfcc){
        mIsFcc = isfcc;
    }
    public int getIsFcc(){
        return mIsFcc;
    }
    void start(){
        super.start();
    }
    void stop(){
        super.stop();
    }
    @Override
    protected void proecee(long channel_id) {
        Pesi_Monitor_Table(channel_id);

    }

    @Override
    public void setChannelId(long channelId) {
        super.setChannelId(channelId);
        not_found_service_count = 0;
    }

    private void Pesi_Monitor_Table(long channelId){
        ProgramInfo programInfo = mDataManager.getProgramInfo(channelId);
        //int update_flag;
        int count=0;
        if(programInfo == null){
            LogUtils.d("Invaild ProgramInfo channelId="+ channelId);
            return;
        }
        //update_flag=0;
        //start_eit();
        while(true) {
            try {
                count++;
                if(IsStop()) {
                    LogUtils.d("Pesi_Monitor_Table Stop");
                    stop();
                   // mHandlerThreadHandler.removeMessages(SI_UPDATE_DMG);
                    break;
                }

                if(!mTuneInterface.isLock(programInfo.getTunerId())){
                    LogUtils.d("Tuner is no lock, TunerId = "+programInfo.getTunerId()+" channelid = "+programInfo.getChannelId()+ " name["+programInfo.getDisplayName()+"]");
                    Thread.sleep(300);
                    continue;
                }

                Pesi_Monitor_PMT();

                if(IsStop())
                {
                    LogUtils.d("Pesi_Monitor_Table Stop");
                    stop();
                    break;
                }

                if((count % 3) == 0) {
                    if(IsStop() == true) {
                        LogUtils.d("Pesi_Monitor_Table Stop");
                        stop();
                        break;
                    }
//                    else {
//                        if(mSIUpdater == null && mIsFcc == 0 && Pvcfg.getScanOneTP() == 0) {
//                            if(Pvcfg.isSiupdateEnable()) {
//                                mSIUpdater.start();
//                            }
//                        }
//                    }
                }
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void Pesi_Monitor_PMT() {
        try{
            ProgramInfo programInfo = mDataManager.getProgramInfo(getChannelId());
            if(programInfo == null)
                return;
            if(DEBUG_PMT)
                LogUtils.d(" Pesi_Monitor_PMT IN TunerId = "+programInfo.getTunerId()+" programInfo = "+programInfo);
            //if(programInfo.getPmtPid() == 0)
            {
                Pat pat;
                Pmt pmt;
                List<PmtData> pmts;
                PatData patData, tmp=null;
                IsPMTVersionChange = 0;
                IsAVPidChanged = 0;
                IsSubtitleChanged = 0;
                IsAudioLanguageChanged = 0;
                int tuner_id = getTunerId();//mTunerId;//programInfo.getTunerId();
                if(DEBUG_PMT) {
                    LogUtils.d("[DEBUG_PMT] tuner_id " + tuner_id + " " + programInfo.getTunerId());
                    LogUtils.d("[DEBUG_PMT] CH " + programInfo.getDisplayNum() + " [" + programInfo.getDisplayName() + "]");
                    LogUtils.d("[DEBUG_PMT] channelID " + getChannelId() + " [" + programInfo.getChannelId() + "]");
                }
                pat = new Pat(tuner_id);
                pat.processWait();
                patData = pat.getPatData();
                int patProgramSize = patData == null ? 0 : patData.getProgramTotalNum();
                if(DEBUG_PMT) {
                    LogUtils.d("programInfo Service ID = " + programInfo.getServiceId() + " Name:" + programInfo.getChName_chi());
                    LogUtils.d("patProgramSize = " + patProgramSize);
                }
                for (int i = 0 ; i < patProgramSize ; i++) {
                    int programMapPid = patData.getProgramMapPid(i);
                    int programNumber = patData.getProgramNumber(i);
//                    if(DEBUG_PMT) {
//                        LogUtils.d("programMapPid = " + programMapPid);
//                        LogUtils.d("programNumber = " + programNumber);
//                    }
                    if(programNumber == programInfo.getServiceId()){
                        if(DEBUG_PMT)
                            LogUtils.d("Pesi_Monitor_PMT run PAT to find PMT programNumber="+programNumber+" PID="+programMapPid);
                        tmp = new PatData(programNumber, programMapPid);
                        break;
                    }
                }
                if(tmp != null){
                    pmt = new Pmt(tuner_id, tmp);
                    pmt.processWait();
                    pmts = pmt.getPmtDataList();

                    if(pmts.size() >0){
                        CompareDataWithPMT(programInfo, pmts);
                        if(IsSubtitleChanged == 1){
                            LogUtils.d("Pesi_Monitor_PMT ==>PMT_SUB_LANG_CHANGED");
                            LogUtils.d("Need change subtitle");
                            mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_PMT_UPDATE_SUBTITLE,0,0 ,programInfo);
                        }
                        if(IsAVPidChanged == 1){
                            LogUtils.d("Pesi_Monitor_PMT ==>PMT_AVPID_CHANGED ["+programInfo.getDisplayName()+"]");
                            LogUtils.d("Tuner id = "+programInfo.getTunerId());
                            LogUtils.d("Need change channel");
                            mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_PMT_UPDATE_AV,mIsFcc,0 ,programInfo);
                        }else {
                            if (IsPMTVersionChange == 1) {
                                LogUtils.d("Pesi_Monitor_PMT ==>PMT_VERSION_CHANGED");
                                LogUtils.d("Need call change_ca_info");
                                mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_PMT_UPDATE_VERSION, mIsFcc, 0, programInfo);
                            }
                        }
                    }
                    pmt.abort();
                }
                else{
                    not_found_service_count++;

                    if(not_found_service_count > 5 && (mIsFcc == 0)){
                        mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_FRONTEND_STOP,(int)getChannelId(),0,null);
                    }
                }
                pat.abort();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void CompareDataWithPMT(ProgramInfo programInfo, List<PmtData> pmts){
        PmtData.Program_map pmtData = pmts.get(0).getProgramMap();
        int diff = PMT_NO_CHANGED;
        if(programInfo.getPmtversion() != pmtData.getPMTVersion()){
            diff |= PMT_VERSION_CHANGED;
            LogUtils.d("PMTVersion "+programInfo.getPmtversion()+" "+pmtData.getPMTVersion());
            programInfo.setPmtVersion(pmtData.getPMTVersion());
            IsPMTVersionChange = 1;
        }
        //Subtitle
        if(CompareSubtitle(programInfo, pmts) == 1){
            diff |= PMT_SUB_LANG_CHANGED;
            IsSubtitleChanged = 1;
        }
        //Audio PID
        if(CompareAudioLanguage(programInfo, pmts) == 1) {
            programInfo.pAudios.clear();
            mScanUtils.set_audio_language(programInfo, pmts.get(0));
            programInfo.setAudioSelected(0);
            diff |= PMT_AVPID_CHANGED;
            IsAVPidChanged = 1;
        }
        //Video PID
        if(CompareVideoPID(programInfo, pmts) == 1){
            diff |= PMT_AVPID_CHANGED;
            IsAVPidChanged = 1;
        }

        // CA info
        if(CompareCaInfo(programInfo, pmts) == 1) {
            diff |= PMT_CA_INFO_CHANGED;
            IsAVPidChanged = 1;
        }

        if(diff > PMT_NO_CHANGED) {
            mDataManager.updateProgramInfo(programInfo);
            mDataManager.DataManagerSaveProgramData(programInfo);
            //mDataManager.DataManagerSaveData(DataManager.SV_PROGRAM_INFO);
        }
    }
    private int CompareVideoPID(ProgramInfo programInfo, List<PmtData> pmts){
        PmtData.Program_map pmtData = pmts.get(0).getProgramMap();
        if(programInfo.getPcr() != pmtData.getPcr_pid()){
            programInfo.setPcr(pmtData.getPcr_pid());
        }
        if(programInfo.pVideo.getPID() != pmtData.getVideo_pid(0) ||
                programInfo.pVideo.getCodec() != pmtData.getVideo_stream_type(0))
        {
            if(DEBUG_AV_PID_CHANGED)
                LogUtils.d("CompareVideoPID Video PID changed, need reset audio info");
            programInfo.pVideo.setPID(pmtData.getVideo_pid(0));
            programInfo.pVideo.setCodec(pmtData.getVideo_stream_type(0));
            return 1;
        }
        return 0;
    }

    private int CompareAudioLanguage(ProgramInfo programInfo, List<PmtData> pmts){
        PmtData.Program_map pmtData = pmts.get(0).getProgramMap();
        int ps_naudio = programInfo.pAudios.size(),pmt_audio = 0,found_pid = 0, changed = 0;
        for(int i=0 ; i<PMT_MAX_AUDIO_PID_NUM ; i++){
            if(pmtData.getAudio_pid(i) !=0){
                pmt_audio++;
            }
        }
        if(ps_naudio == pmt_audio){
            found_pid = 0;
            for(int j=0 ; j<ps_naudio ; j++){
                if(programInfo.pAudios.get(j).getPid() == pmtData.getAudio_pid(j)){
                    found_pid = 1;
                    break;
                }
            }
            if(found_pid == 0){
                if(DEBUG_AUD_LANG_CHANGED)
                    LogUtils.d("CompareAudioLanguage found new Audio PID, need reset audio info");
                changed = 1;
            }
        }
        else{
            if(DEBUG_AUD_LANG_CHANGED)
                LogUtils.d("CompareAudioLanguage Audio number different, need reset audio info");
            changed = 1;
        }

        return changed;
    }

    private int CompareSubtitle(ProgramInfo programInfo, List<PmtData> pmts){
        PmtData.Program_map pmtData = pmts.get(0).getProgramMap();
        int change = 0;
        if(programInfo.pSubtitle.size() != pmtData.getSubt_lang_num()){
            if(DEBUG_SUB_LANG_CHANGED) {
                LogUtils.d("Subtitle number not match , clear all and rebuild");
                LogUtils.d("pSubtitle size="+programInfo.pSubtitle.size()+" PMT size="+pmtData.getSubt_lang_num());
            }
            programInfo.pSubtitle.clear();
            for(int j=0;j<pmtData.getSubt_lang_num();j++)
            {
                ProgramInfo.SubtitleInfo subtitleInfo = new ProgramInfo.SubtitleInfo(
                        pmtData.getSubtitling_type(j),
                        pmtData.getSubt_stream_id(j),
                        pmtData.getsubt_lang(j),
                        pmtData.getSubt_com_page_id(j),
                        pmtData.getSubt_anc_page_id(j));
                programInfo.pSubtitle.add(j,subtitleInfo);
                if(DEBUG_SUB_LANG_CHANGED) {
                    LogUtils.d("intdex: " + j);
                    subtitleInfo.toString();
                }
            }
            change = 1;
        }
        else{
            for(int j=0;j<pmtData.getSubt_lang_num();j++){
                if(programInfo.pSubtitle.get(j).getType() != pmtData.getSubtitling_type(j)) {
                    programInfo.pSubtitle.get(j).setType(pmtData.getSubtitling_type(j));
                    change = 1;
                    if(DEBUG_SUB_LANG_CHANGED)
                        LogUtils.d("Subtitle Type not match");
                }
                if(programInfo.pSubtitle.get(j).getPid() != pmtData.getSubt_stream_id(j)){
                    programInfo.pSubtitle.get(j).setPid(pmtData.getSubt_stream_id(j));
                    change = 1;
                    if(DEBUG_SUB_LANG_CHANGED)
                        LogUtils.d("Subtitle Pid not match");
                }
                if(programInfo.pSubtitle.get(j).getLang().compareTo(pmtData.getsubt_lang(j)) != 0){
                    programInfo.pSubtitle.get(j).setLang(pmtData.getsubt_lang(j));
                    change = 1;
                    if(DEBUG_SUB_LANG_CHANGED)
                        LogUtils.d("Subtitle Lang not match");
                }
                if(programInfo.pSubtitle.get(j).getComPageId() != pmtData.getSubt_com_page_id(j)){
                    programInfo.pSubtitle.get(j).setComPageId(pmtData.getSubt_com_page_id(j));
                    change = 1;
                    if(DEBUG_SUB_LANG_CHANGED)
                        LogUtils.d("Subtitle ComPageID not match");
                }
                if(programInfo.pSubtitle.get(j).getAncPageId() != pmtData.getSubt_anc_page_id(j)){
                    programInfo.pSubtitle.get(j).setAncPageId(pmtData.getSubt_anc_page_id(j));
                    change = 1;
                    if(DEBUG_SUB_LANG_CHANGED)
                        LogUtils.d("Subtitle AncPageId not match");
                }
            }
        }
        return change;
    }

    private int CompareCaInfo(ProgramInfo programInfo, List<PmtData> pmts) {
        boolean changed = false;

        PmtData.Program_map pmtData = pmts.get(0).getProgramMap();
        int caCount = pmtData.getCaSystemIdList().size();
        List<ProgramInfo.CaInfo> caInfoList = new ArrayList<>();
        for (int i = 0 ; i < caCount ; i++) {
            ProgramInfo.CaInfo caInfo = new ProgramInfo.CaInfo();
            caInfo.setCaSystemId(pmtData.getCaSystemIdList().get(i));
            caInfo.setEcmPid(pmtData.getCaPidList().get(i)); // ca id in pmt is ecm pid
            caInfo.setPrivateData(pmtData.getPrivateDataByteList().get(i));
            //LogUtils.d("[cas] CA system id = "+pmtData.getCaSystemIdList().get(i));
            //LogUtils.d("[cas] ECM PID = "+pmtData.getCaPidList().get(i));
            //LogUtils.d("[cas] private data = "+ Arrays.toString(pmtData.getPrivateDataByteList().get(i)));
            caInfoList.add(caInfo);
        }

        // assume video and audio use same ca
        // so we check video only
        if(programInfo.getType() == ProgramInfo.PROGRAM_RADIO){
            if (caCount != programInfo.pAudios.get(0).CaInfoList.size()) {
                changed = true;
            } else {
                for (int i = 0; i < caCount; i++) {
                    int caSystemId = programInfo.pAudios.get(0).getCaSystemId(i);
                    int ecmPid = programInfo.pAudios.get(0).getEcmPid(caSystemId);
                    byte[] privateData = programInfo.pAudios.get(0).getPrivateData(caSystemId);

                    if (caSystemId != caInfoList.get(i).getCaSystemId()
                            || ecmPid != caInfoList.get(i).getEcmPid()
                            || !Arrays.equals(privateData, caInfoList.get(i).getPrivateData())) {
                        changed = true;
                        break;
                    }
                }
            }
        }else {
            if (caCount != programInfo.pVideo.CaInfoList.size()) {
                changed = true;
            } else {
                for (int i = 0; i < caCount; i++) {
                    int caSystemId = programInfo.pVideo.getCaSystemId(i);
                    int ecmPid = programInfo.pVideo.getEcmPid(caSystemId);
                    byte[] privateData = programInfo.pVideo.getPrivateData(caSystemId);

                    if (caSystemId != caInfoList.get(i).getCaSystemId()
                            || ecmPid != caInfoList.get(i).getEcmPid()
                            || !Arrays.equals(privateData, caInfoList.get(i).getPrivateData())) {
                        changed = true;
                        break;
                    }
                }
            }
        }

        if (changed) {
            // clear and re add all ca
            // video
            if(programInfo.getType() == ProgramInfo.PROGRAM_TV) {
                programInfo.pVideo.CaInfoList.clear();
                programInfo.pVideo.CaInfoList.addAll(caInfoList);
            }

            // audios
            for (ProgramInfo.AudioInfo audioInfo : programInfo.pAudios) {
                audioInfo.CaInfoList.clear();
                audioInfo.CaInfoList.addAll(caInfoList);
            }
        }

        return changed ? 1 : 0;
    }
}
