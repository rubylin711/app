package com.prime.dtv.service.Scan;

import static com.prime.dtv.service.Table.PmtData.PMT_MAX_TELETEXT_PID_NUM;

import android.content.Context;
import android.util.Log;

import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Table.Bat;
import com.prime.dtv.service.Table.BatData;
import com.prime.dtv.sysdata.DTVMessage;
import com.prime.dtv.sysdata.EnNetworkType;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.Table.Desciptor.Descriptor;
import com.prime.dtv.service.Table.Nit;
import com.prime.dtv.service.Table.NitData;
import com.prime.dtv.service.Table.Pat;
import com.prime.dtv.service.Table.PatData;
import com.prime.dtv.service.Table.Pmt;
import com.prime.dtv.service.Table.PmtData;
import com.prime.dtv.service.Table.Sdt;
import com.prime.dtv.service.Table.SdtData;
import com.prime.dtv.service.Tuner.TunerInterface;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.SatInfo;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.utils.LogUtils;
import com.prime.dtv.utils.TVScanParams;

import java.util.ArrayList;
import java.util.List;

public class Scan {
    private static final String TAG = "Scan";
    //private final Semaphore mSemaphore;
    private int abort_scan=0;
    private List<ProgramInfo> mProgramInfoList=new ArrayList<>();
    private List<TpInfo> mTpInfoList;
    private static DataManager mDataManager = null;
    private static int nit_search_index=0;
    private static PesiDtvFrameworkInterfaceCallback mCallback;
    private static TunerInterface mTuneInterface;
    private static TVScanParams mScanData;
    private Scan_utils mScanUtils;
    public Scan(Context context, TVScanParams scanData, PesiDtvFrameworkInterfaceCallback callback){
        mTuneInterface = TunerInterface.getInstance(context);
        mScanData=scanData;
        mDataManager = DataManager.getDataManager(context);
        mCallback = callback;
        mScanUtils = new Scan_utils(context);
        //run(scanData);
    }
    public void startScan()
    {
        //run(mScanData);
        process(mScanData);
    }
    private void scan_callback_progress(int freq, int tp_id, int value)
    {
        if(mCallback!=null) {
            mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_SRCH_LOCK_START, freq, tp_id, null);
            mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_SRCH_CUR_SCHEDULE, value, 0, null);
        }
    }
    private void scan_callback_start()
    {
        if(mCallback!=null) {
            mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_SRCH_BEGIN, 0, 0, null);
        }
    }
    private void scan_callback_finsh() {
        int value = 100;
        if (mCallback != null) {
            mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_SRCH_CUR_SCHEDULE, value, 0, null);
            mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_SRCH_FINISH, 0, 0, null);
        }
    }

    private void scan_callback_service(List<ProgramInfo> programInfoList) {
        if (mCallback != null) {
            for (ProgramInfo programInfo : programInfoList) {
                int tpID = programInfo.getTpId();
                long channelID = programInfo.getChannelId();

                // channel id should not > 2,147,483,647 (2^32-1)
                mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_SRCH_GET_PROG_PESI, tpID, (int) channelID, programInfo);
            }
        }
    }

    public void setScanAbortFlag(int flag)
    {
        abort_scan=flag;
    }
    private void buildProgramInfoListByPMT(List<PmtData> pmts, TVScanParams scanData, List<ProgramInfo> programInfoList)
    {
        Log.d(TAG, "pmts.size:"+ pmts.size());
        int teletext_num=0;

        for(int i = 0; i < pmts.size(); i++) {
            ProgramInfo programInfo = new ProgramInfo();
            PmtData.Program_map pmtData = pmts.get(i).getProgramMap();
            //set programInfo
            programInfo.setPmtPid(pmtData.getPmtPid());
            programInfo.setPmtVersion(pmtData.getPMTVersion());
            programInfo.pVideo.setPID(pmtData.getVideo_pid(0));
            programInfo.setServiceId(pmtData.getProgram_number());
            programInfo.setChannelId((scanData.getTpId()<<16)|(pmtData.getProgram_number()));
            programInfo.pVideo.setCodec(pmtData.getVideo_stream_type(0));
            programInfo.setPcr(pmtData.getPcr_pid());
            mScanUtils.set_audio_language(programInfo,pmts.get(i));
            Log.d(TAG, "Ttxt_num:"+pmtData.getTtxt_desc_number());

            if(pmtData.getTtxt_desc_number()>PMT_MAX_TELETEXT_PID_NUM)
                teletext_num=PMT_MAX_TELETEXT_PID_NUM;
            else
                teletext_num=pmtData.getTtxt_desc_number();
            for(int j=0;j<teletext_num;j++) {
                Log.d(TAG, "Teletext_pid:"+pmtData.getTeletext_pid(j));
                ProgramInfo.TeletextInfo pTeletext=new ProgramInfo.TeletextInfo(pmtData.getTeletext_pid(j),
                        pmtData.getTtxt_type(j),
                        pmtData.getTtxt_lang(j),
                        pmtData.getTtxt_magazine_number(j),
                        pmtData.getTtxt_page_number(j));
                programInfo.pTeletext.add(pTeletext);
            }
            programInfo.setSatId(scanData.getSatId());
            programInfo.setTpId(scanData.getTpId());
            //ss_lnbselect
            programInfo.setCA(pmtData.getCa_flag());

            // mediacas
            // add all ca from pmt data
            int caCount = pmtData.getCaSystemIdList().size();
            List<ProgramInfo.CaInfo> caInfoList = new ArrayList<>();
            for (int j = 0 ; j < caCount ; j++ ) {
                ProgramInfo.CaInfo caInfo = new ProgramInfo.CaInfo();
                caInfo.setCaSystemId(pmtData.getCaSystemIdList().get(j));
                caInfo.setEcmPid(pmtData.getCaPidList().get(j)); // ca id in pmt is ecm pid
                caInfo.setPrivateData(pmtData.getPrivateDataByteList().get(j));
                caInfoList.add(caInfo);
            }

            programInfo.pVideo.CaInfoList.addAll(caInfoList); // add ca for video
            // add ca for all audios?
            for (ProgramInfo.AudioInfo audioInfo : programInfo.pAudios) {
                audioInfo.CaInfoList.addAll(caInfoList);
            }

            //pmtData.getPrivateDataByte();
            if(programInfo.pAudios.size()>0) {
                //if (programInfo.pAudios.get(0).getPid() != 0)
                    programInfo.setType(ProgramInfo.PROGRAM_RADIO);
            }
            if(programInfo.pVideo.getPID()!=0)
                programInfo.setType(ProgramInfo.PROGRAM_TV);

            for(int j=0;j<pmtData.getSubt_lang_num();j++)
            {
                ProgramInfo.SubtitleInfo subtitleInfo = new ProgramInfo.SubtitleInfo(
                        pmtData.getSubtitling_type(j),
                        pmtData.getSubt_stream_id(j),
                        pmtData.getsubt_lang(j),
                        pmtData.getSubt_com_page_id(j),
                        pmtData.getSubt_anc_page_id(j));
                programInfo.pSubtitle.add(j,subtitleInfo);
            }
            if((programInfo.pAudios.size()>0)||(programInfo.pVideo.getPID()!=0)) {
                Log.d(TAG, "add programInfoList:"+i);
                programInfoList.add(programInfo);
            }
        }
    }
    private void addProgramInfoBySDT(SdtData sdtData, List<ProgramInfo> ProgramInfoList)
    {
        int serviceID;
        int tsID;
        int onID;
        int serviceType;
        boolean other_type=false;
        boolean service_not_in_sdt=true;
        List<TpInfo> tpInfoList;
        TpInfo tpInfo;
        if(sdtData != null && sdtData.getServiceDataTotalNum()>0) {
            int i=0;
            tpInfo = mDataManager.getTpInfo(mScanData.getTpId());
            tpInfo.setOrignal_network_id(sdtData.getOriginalNetworkIdByIndex(0));
            tpInfo.setTransport_id(sdtData.getTransportStreamIdByIndex(0));
            for (i = 0; i < ProgramInfoList.size(); i++) {
                serviceID = ProgramInfoList.get(i).getServiceId();
                LogUtils.d("serviceID = "+serviceID);
                for (int j = 0; j < sdtData.getServiceDataTotalNum(); j++) {
                    LogUtils.d("serviceID = "+sdtData.getServiceIdByIndex(j));
                    if (serviceID == sdtData.getServiceIdByIndex(j)) {
                        tsID = sdtData.getTransportStreamIdByIndex(j);
                        onID = sdtData.getOriginalNetworkIdByIndex(j);
                        ProgramInfoList.get(i).setTransportStreamId(tsID);
                        Log.d(TAG, "ServiceName =" + sdtData.getServiceName(tsID, serviceID));
                        ProgramInfoList.get(i).setDisplayName(sdtData.getServiceName(tsID, serviceID));
                        ProgramInfoList.get(i).setOriginalNetworkId(onID);
                        serviceType = sdtData.getServiceType(tsID, serviceID);
                        if (serviceType == Scan_cfg.DIGITAL_TELEVISION_SERVICE ||
                                serviceType == Scan_cfg.MPEG_2_HD_DIGITAL_TELEVISION_SERVICE ||
                                serviceType == Scan_cfg.ADVANCED_CODEC_SD_DIGITAL_TELEVISION_SERVICE ||
                                serviceType == Scan_cfg.ADVANCED_CODEC_HD_DIGITAL_TELEVISION_SERVICE)
                            ProgramInfoList.get(i).setType(ProgramInfo.PROGRAM_TV);
                        else if (serviceType == Scan_cfg.RESERVED_FOR_FUTURE_USE ||
                                serviceType == Scan_cfg.PAL_CODED_SIGNAL ||
                                serviceType == Scan_cfg.SECAM_CODED_SIGNAL ||
                                serviceType == Scan_cfg.DD2_MAC ||
                                serviceType == Scan_cfg.RESERVED_FOR_FUTURE_USE1 ||
                                serviceType == Scan_cfg.RESERVED_FOR_FUTURE_USE2) {
                            if(serviceType==Scan_cfg.HEVC)
                                other_type =false;
                            else
                                other_type =true;
                        }
                        else
                            ProgramInfoList.get(i).setType(ProgramInfo.PROGRAM_RADIO);

                        service_not_in_sdt = false;
                        break;
                    }
                }
                if(Scan_cfg.ONLY_SDT_SERVICE) {
                    if (other_type == true || service_not_in_sdt == true)
                    {
                        ProgramInfoList.remove(i);
                        other_type = false;
                    } else {
                        service_not_in_sdt = true;
                    }
                }
                else {
                    if (other_type == true) {
                        ProgramInfoList.remove(i);
                        other_type = false;
                    } else {
                        service_not_in_sdt = true;
                    }
                }
            }
        }
        for (int j = 0; sdtData != null && j < sdtData.getServiceDataTotalNum(); j++) {
            tsID = sdtData.getTransportStreamIdByIndex(j);
            serviceID = sdtData.getServiceIdByIndex(j);
            serviceType = sdtData.getServiceType(tsID, serviceID);
            if (serviceType == Scan_cfg.DIGITAL_TELEVISION_SERVICE ||
                    serviceType == Scan_cfg.DIGITAL_RADIO_SOUND_SERVICE ||
                    serviceType == Scan_cfg.MPEG_2_HD_DIGITAL_TELEVISION_SERVICE ||
                    serviceType == Scan_cfg.ADVANCED_CODEC_SD_DIGITAL_TELEVISION_SERVICE ||
                    serviceType == Scan_cfg.ADVANCED_CODEC_HD_DIGITAL_TELEVISION_SERVICE) {
                ProgramInfo programInfo = new ProgramInfo();
                programInfo.setServiceId(sdtData.getServiceIdByIndex(j));
                programInfo.setTransportStreamId(sdtData.getTransportStreamIdByIndex(j));
                programInfo.setOriginalNetworkId(sdtData.getOriginalNetworkIdByIndex(j));
                programInfo.setSatId(mScanData.getSatId());
                programInfo.setTpId(mScanData.getTpId());
                //programInfo.setLNBSelect(0);
                programInfo.setCA(sdtData.getFreeCaMode(sdtData.getTransportStreamIdByIndex(j),sdtData.getServiceIdByIndex(j)));
                //programInfo.setCharacterCode();
                if (serviceType == Scan_cfg.DIGITAL_TELEVISION_SERVICE ||
                        serviceType == Scan_cfg.MPEG_2_HD_DIGITAL_TELEVISION_SERVICE ||
                        serviceType == Scan_cfg.ADVANCED_CODEC_SD_DIGITAL_TELEVISION_SERVICE ||
                        serviceType == Scan_cfg.ADVANCED_CODEC_HD_DIGITAL_TELEVISION_SERVICE) {
                    programInfo.setType(ProgramInfo.PROGRAM_TV);
                } else {
                    programInfo.setType(ProgramInfo.PROGRAM_RADIO);
                }
                mScanUtils.addCompareService(ProgramInfoList,programInfo);
            }
        }
        Log.d(TAG,"ProgramInfoList.size() = " + ProgramInfoList.size());
        for (int i = 0; i < ProgramInfoList.size(); i++)
        {
            if (ProgramInfoList.get(i).getDisplayName()==null)
            {
                switch(ProgramInfoList.get(i).getType())
                {
                    case ProgramInfo.PROGRAM_TV:
                        ProgramInfoList.get(i).setDisplayName("TV Ch");
                        break;
                    case ProgramInfo.PROGRAM_RADIO:
                        ProgramInfoList.get(i).setDisplayName("Radio Ch");
                        break;
                }
            }

        }
    }
    private void addProgramInfoByNIT(NitData nitData, TVScanParams scanData, List<ProgramInfo> ProgramInfoList)
    {
        int serviceID;
        int onID;
        int lcn;

        int tunerType = DataManager.TUNR_TYPE;
        Log.d(TAG,"ProgramInfoList.size() = " + ProgramInfoList.size());
        for(int i=0;i<ProgramInfoList.size();i++) {
            serviceID = ProgramInfoList.get(i).getServiceId();
            onID = ProgramInfoList.get(i).getOriginalNetworkId();
            lcn = nitData == null ? 0 : nitData.getLCN(serviceID, onID);
            ProgramInfoList.get(i).setLCN(lcn);
        }
        if (scanData.getScanMode() == TVScanParams.SCAN_MODE_NETWORK) {
            for(int j=0; nitData != null && j<nitData.getNumOfTransportStream();j++) {
                switch (tunerType) {
                    case Scan_cfg.DVBC: {
                        if(nitData.getTransportStreamList().get(j).getdescTag()==Descriptor.CABLE_DELIVERY_SYSTEM_DESC) {
                            int freq = nitData.getTransportStreamList().get(j).getFreq(Descriptor.CABLE_DELIVERY_SYSTEM_DESC);
                            int qam = nitData.getTransportStreamList().get(j).getQam();
                            int symbolrate = nitData.getTransportStreamList().get(j).getSymbolrate(Descriptor.CABLE_DELIVERY_SYSTEM_DESC);
                            mScanUtils.modifyCableTpInfo(freq, qam, symbolrate);
                        }
                    }
                    break;
                    case Scan_cfg.DVBT: {
                        if(nitData.getTransportStreamList().get(j).getdescTag()==Descriptor.TERRESTRIAL_DELIVERY_SYSTEM_DESC) {
                            int freq = nitData.getTransportStreamList().get(j).getFreq(Descriptor.TERRESTRIAL_DELIVERY_SYSTEM_DESC);
                            int fft=nitData.getTransportStreamList().get(j).getFFT_mode();
                            int collsetellation=nitData.getTransportStreamList().get(j).getConstellation();
                            int hierarchy=nitData.getTransportStreamList().get(j).getHierarchy();
                            int guard_interval=nitData.getTransportStreamList().get(j).getGuardInterval();
                            int code_rate_H=nitData.getTransportStreamList().get(j).getCodeRateH();
                            mScanUtils.modifyTerrTpInfo(freq ,fft,collsetellation,hierarchy,guard_interval,code_rate_H);
                        }
                    }
                    break;
                    case Scan_cfg.DVBS: {
                        if(nitData.getTransportStreamList().get(j).getdescTag()==Descriptor.SATELLITE_DELIVERY_SYSTEM_DESC) {
                            int freq=nitData.getTransportStreamList().get(j).getFreq(Descriptor.SATELLITE_DELIVERY_SYSTEM_DESC);
                            int symbolrate = nitData.getTransportStreamList().get(j).getSymbolrate(Descriptor.SATELLITE_DELIVERY_SYSTEM_DESC);
                            int polar=nitData.getTransportStreamList().get(j).getPolarization();
                            int ffcInner=nitData.getTransportStreamList().get(j).getFECinner();
                            mScanUtils.modifySatTpInfo(scanData.getSatId(),freq,symbolrate,polar,ffcInner);
                        }
                    }
                    break;                    case Scan_cfg.ISDBT: {
                        if(nitData.getTransportStreamList().get(j).getdescTag()==Descriptor.ISDBT_DELIVERY_SYSTEM_DESC) {
                            for(int i=0;i<nitData.getTransportStreamList().get(j).getISDBT_freqnum();i++)
                            {
                                int freq=nitData.getTransportStreamList().get(j).getISDBT_freqency(i);
                                mScanUtils.modifyIsdbtTpInfo(freq);
                            }
                        }
                    }
                    break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + tunerType);
                }
            }
        }
    }

    private List<ProgramInfo> scanTBC(TVScanParams scanData){
        List<ProgramInfo> programInfoList=new ArrayList<>();
        TpInfo tpInfo=scanData.getTpInfo();
        int tuner_id = scanData.getTunerId();
        Nit nit;
        Sdt sdt;
        Bat bat;
        NitData nitDataTmp=null;
        List<SdtData> sdtDataList = null;
        BatData batData=null;
        LogUtils.d("scanTBC IN");
        LogUtils.d("scanTBC freq = "+tpInfo.CableTp.getFreq());
        LogUtils.d("scanTBC sym = "+tpInfo.CableTp.getSymbol());
        LogUtils.d("scanTBC qam = "+tpInfo.CableTp.getQam());
        if (mTuneInterface.tune(tuner_id, tpInfo)) {
            nit = new Nit(tuner_id, true);
            nit.processWait();
            nitDataTmp = nit.getNitData();
            if (nitDataTmp != null) {
                mScanUtils.Scan_process_Nit(nitDataTmp);
            
                LogUtils.d("scanTBC Start sdt");
                sdt = new Sdt(tuner_id, true, true, nitDataTmp);
                sdt.processWait();
                sdtDataList = sdt.getSdtData();
                LogUtils.d("scanTBC Stop sdt, sdtDataList.size() = " + sdtDataList.size());

                if (mScanUtils.getBatInWhiteList() == true) {
                    LogUtils.d("scanTBC Bat id ["+Pvcfg.getBatId()+" is in the whitelist ");
                    bat = new Bat(tuner_id, Pvcfg.getBatId(), true);
                    bat.processWait();
                    batData = bat.getBatData();
                    if (batData != null) {
                        LogUtils.d("scanTBC Got Bat");
                        mScanUtils.Scan_process_Bat(batData, programInfoList);
                        LogUtils.d("scanTBC programInfoList.size(Scan_process_Bat) = "+programInfoList.size());
                        mScanUtils.Scan_process_Nit_TBC(nitDataTmp, true, programInfoList);
                        LogUtils.d("scanTBC programInfoList.size(Scan_process_Nit_TBC) = "+programInfoList.size());
                    } 
                    else {
                        LogUtils.d("scanTBC No Bat");
                        mScanUtils.Scan_process_Nit_TBC(nitDataTmp, false, programInfoList);
                    }
                    mScanUtils.Scan_process_Sdt_TBC(sdtDataList, programInfoList);
                    LogUtils.d("scanTBC programInfoList.size(Scan_process_Sdt_TBC) = "+programInfoList.size());
                    mScanUtils.no_lcn_channel_set_delete(programInfoList);
                    LogUtils.d("scanTBC programInfoList.size(no_lcn_channel_set_delete) = "+programInfoList.size());
                    mScanUtils.remove_mark_delete_service(programInfoList);
                    LogUtils.d("scanTBC programInfoList.size(remove_mark_delete_service) = "+programInfoList.size());
                    mScanUtils.SortProgrameInfoByLCN(programInfoList);
                    LogUtils.d("scanTBC programInfoList.size(SortProgrameInfoByLCN) = "+programInfoList.size());
                    mScanUtils.category_add_to_fav(programInfoList);
                    LogUtils.d("scanTBC programInfoList.size(category_add_to_fav) = "+programInfoList.size());
                    if (abort_scan == 0) {
                        AddProgramInfoList(programInfoList);
                        scan_callback_service(programInfoList);
                        Pvcfg.setScanOneTP(0);
                        mDataManager.getGposInfo().setSearchOneTPFlag(0);
                    }
                } 
                else {
                    LogUtils.d("scanTBC not in the whitelist ");
                }
            }
        }
        else{
            LogUtils.d("scanTBC tuner not lock ");
        }

        return programInfoList;
    }

    private List<ProgramInfo> scanDMG(TVScanParams scanData){
        List<ProgramInfo> programInfoList=new ArrayList<>();
        TpInfo tpInfo=scanData.getTpInfo();
        int tuner_id = scanData.getTunerId();
        Nit nit;
        Sdt sdt;
        NitData nitDataTmp=null;
        List<SdtData> sdtDataList = null;
        LogUtils.d("IN");
        LogUtils.d("freq = "+tpInfo.CableTp.getFreq());
        LogUtils.d("sym = "+tpInfo.CableTp.getSymbol());
        LogUtils.d("qam = "+tpInfo.CableTp.getQam());
        if (mTuneInterface.tune(tuner_id, tpInfo)) {
            nit = new Nit(tuner_id, true);
            nit.processWait();
            nitDataTmp = nit.getNitData();
            if(nitDataTmp != null) {
                sdt = new Sdt(tuner_id, true, true, nitDataTmp);
                sdt.processWait();
                sdtDataList = sdt.getSdtData();
                mScanUtils.Scan_process_Nit(nitDataTmp);
                mScanUtils.Scan_process_Sdt_DMG(sdtDataList, programInfoList);
                mScanUtils.Scan_process_Nit_DMG(nitDataTmp, programInfoList);

                mScanUtils.no_lcn_channel_set_delete(programInfoList);
                mScanUtils.remove_mark_delete_service(programInfoList);

                mScanUtils.SortProgrameInfoByLCN(programInfoList);

                mScanUtils.category_add_to_fav(programInfoList);
                if (abort_scan == 0) {
                    AddProgramInfoList(programInfoList);
                    scan_callback_service(programInfoList);
                    Pvcfg.setScanOneTP(0);
                    mDataManager.getGposInfo().setSearchOneTPFlag(0);
                }
            }
        }
        else{
            LogUtils.d("tuner not lock ");
        }
        LogUtils.d("OUT");

/*  Allen test start*/
/*

        TdtData tdtData;
        Tot tot;
        TotData totData;
        tdt = new Tdt(mTuneInterface.getTuner(tuner_id));
        tdt.processWait();
        tdtData=tdt.getTdtData();
        tot = new Tot(mTuneInterface.getTuner(tuner_id));
        tot.processWait();
        totData=tot.getTotData();
        if(tdtData != null){
            LogUtils.d("getUtcFormattedTimeString = " + tdtData.getUtcFormattedTimeString());
            LogUtils.d("getTimeMillis = " + tdtData.getTimeMillis());
            LogUtils.d("getUTCLocalDateTime = " + tdtData.getUTCLocalDateTime());
            LogUtils.d("getUtcFormattedTimeString = " + tdtData.getDate());
        }

        PrimeDtvMediaPlayer mPrimeDtvMediaPlayer=PrimeDtvMediaPlayer.get_instance();
        LogUtils.d("@@@@@allen send_epg_data_id 1111111");
        mPrimeDtvMediaPlayer.send_epg_data_id(programInfoList);

        List<EPGEvent> epgevent_list;
        EPGEvent epgeventById;
        long channelID=5636106;
        int eventID=4239;
        Date startTime, endTime;
        int pos=0, reqNum=100, addEmpty=0;
        startTime = new Date();
        endTime = new Date();
        epgevent_list=mPrimeDtvMediaPlayer.get_epg_events(channelID, startTime, endTime, pos, reqNum, addEmpty);
        LogUtils.d("@@@@@allen epgevent_list count = " + epgevent_list.size());
       // Log.d(TAG,"allen_test present name = "+present.get_event_name());
        //Log.d(TAG,"allen_test follow name = "+follow.get_event_name());
        epgeventById=mPrimeDtvMediaPlayer.get_epg_by_event_id(channelID,eventID);
*/


        return programInfoList;
    }
    private List<ProgramInfo> scanOneTp(TVScanParams scanData){
        List<ProgramInfo> programInfoList=new ArrayList<>();
        List<PmtData> pmts;
        PatData patData;
        int quality=0;
        //run table PAT/NIT/SDT/PMT

        //TpInfo tpInfo=mDataManager.getTpInfo(scanData.getTpId());
        TpInfo tpInfo=scanData.getTpInfo();
        int tuner_id = scanData.getTunerId();
        Pat pat;
        Pmt pmt;
        Nit nit=null;
        Sdt sdt;
        Log.d(TAG,"freq"+tpInfo.CableTp.getFreq());
        Log.d(TAG,"sym"+tpInfo.CableTp.getSymbol());
        Log.d(TAG,"qam"+tpInfo.CableTp.getQam());
        if (mTuneInterface.tune(tuner_id, tpInfo)) {
            pat = new Pat(tuner_id);
            pat.processWait();
            if (abort_scan == 1)
                return programInfoList;
            patData = pat.getPatData();
            pmt = new Pmt(tuner_id, patData);
            pmt.processWait();
            if (abort_scan == 1)
                return programInfoList;
            pmts = pmt.getPmtDataList();
            if (pmts.size() != 0) {
                buildProgramInfoListByPMT(pmts, scanData, programInfoList);
                //sdt = new Sdt(mTuneInterface.getTuner(tuner_id), true);
                sdt = new Sdt(tuner_id, true,false,null);
                sdt.processWait();
                if (abort_scan == 1)
                    return programInfoList;
                if(sdt.getSdtData() != null)
                    addProgramInfoBySDT(sdt.getSdtData().get(0), programInfoList);
                if (scanData.getNitSearch() == 1) {
                    nit = new Nit(tuner_id, true);
                    nit.processWait();
                    if (abort_scan == 1)
                        return programInfoList;
                    addProgramInfoByNIT(nit.getNitData(), scanData, programInfoList);
                }
                mScanUtils.category_add_to_fav(programInfoList);
                Pvcfg.setScanOneTP(1);
                mDataManager.getGposInfo().setSearchOneTPFlag(1);
            }


/* Allen test start*/
/*

            //ProgramInfo programInfoTmp=new ProgramInfo();
            //programInfoTmp.setChannelId((scanData.getTpId()<<16)|(110));
            //programInfoTmp.setServiceId(110);
            //programInfoTmp.setTransportStreamId(49);
            //programInfoTmp.setOriginalNetworkId(1);
            //programInfoList.add(programInfoTmp);
            

            mPrimeDtvMediaPlayer.send_epg_data_id(programInfoList);
            Log.d(TAG,"allen_test mPrimeDtvMediaPlayer 1111111");
            for(ProgramInfo tmp : programInfoList) {
                long ChannelId = (int) tmp.getChannelId();
                int TransportStreamId = tmp.getTransportStreamId();
                int ServiceId = (int) tmp.getServiceId();
                int OriginalNetworkId = (int) tmp.getOriginalNetworkId();
                Log.d(TAG,"allen_test ChannelId = "+ChannelId+" ServiceId = "+ServiceId+" TransportStreamId = "+TransportStreamId+" OriginalNetworkId = "+OriginalNetworkId);
            }

            //eit = new Eit(mTuneInterface.getTuner(tuner_id),(byte)0x4F,null);
            eit2 = new Eit(mTuneInterface.getTuner(tuner_id),(byte)0x4e,null);

            for(int hhh=0;hhh<1;hhh++) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            //eit.abort();
            Log.d(TAG, "@@@@@allen eit2.abort");
            Log.d(TAG, "@@@@@allen eit2.abort");
            Log.d(TAG, "@@@@@allen eit2.abort");
            eit2.abort();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            EPGEvent present,follow;
            Log.d(TAG,"allen_test get event start");
            for(ProgramInfo tmp : programInfoList) {
                Log.d(TAG,"allen_test Channel Id = "+tmp.getChannelId());
                present = mPrimeDtvMediaPlayer.get_present_event(tmp.getChannelId());
                follow = mPrimeDtvMediaPlayer.get_follow_event(tmp.getChannelId());
                Log.d(TAG,"allen_test present name = "+present.get_event_name());
                Log.d(TAG,"allen_test follow name = "+follow.get_event_name());
            }
            Log.d(TAG,"allen_test get event end");

            int ttt;
            NitData nitDataTmp=null;
            if(nit == null){
                Log.d(TAG,"allen_test nit is null");
                nit = new Nit(mTuneInterface.getTuner(tuner_id), true);
                nit.processWait();
            }

            nitDataTmp=nit.getNitData();
            Log.d(TAG,"allen_test Nit 1111111");
            if(nitDataTmp != null) {
                Log.d(TAG, "allen_test Num Of Transport Stream = " + nitDataTmp.getNumOfTransportStream());
                List<NitData.TransportStream> transportStreamList=nitDataTmp.getTransportStreamList();
                for(NitData.TransportStream temp : transportStreamList) {
                    Log.d(TAG,"allen_test Transport Stream ID = " + temp.getTransportStreamID());
                }
            }
            else{
                Log.d(TAG, "allen_test No Nit Data Type");
                nit = new Nit(mTuneInterface.getTuner(tuner_id), true);
                nit.processWait();
                nitDataTmp=nit.getNitData();
            }

            if(nitDataTmp != null)
            {
                int i,Size=nitDataTmp.getNetworkStreamList().size();
                Log.d(TAG,"allen_test getNetworkStreamList().size() = " + Size);
                List<DescBase> descBaseListTmp;
                Log.d(TAG,"allen_test getNetworkStreamList==>LINKAGE_DESC");
                for(i=0;i<Size;i++){
                    Log.d(TAG,"allen_test i = " + i);
                    descBaseListTmp=nitDataTmp.getNetworkStreamList().get(i).getDescriptor().getDescriptorList(LINKAGE_DESC);
                    Log.d(TAG,"allen_test descBaseListTmp.size() = " + descBaseListTmp.size());
                    for (DescBase temp : descBaseListTmp){
                        LinkageDescriptor tempDescriptor = (LinkageDescriptor) temp;
                        Log.d(TAG,"allen_test tempDescriptor.LinkageType = " + tempDescriptor.LinkageType);
                        if (tempDescriptor.LinkageType == 0xD2){ //BAT whitelist for TB
                            Log.d(TAG,"allen_test mBatWhiteList (for TB)");
                            List<LinkageDescriptor.BatWhiteList> batWhiteList=tempDescriptor.mBatWhiteList;
                            for (LinkageDescriptor.BatWhiteList temp2 : batWhiteList){
                                Log.d(TAG,"allen_test mBatWhiteList BatNumber = " + temp2.BatNumber);
                                List<Integer> batIdList=temp2.BatIdList;
                                for (Integer temp3 : batIdList){
                                    Log.d(TAG,"allen_test mBatWhiteList BatId = " + temp3);
                                }
                            }
                        }
                        else if(tempDescriptor.LinkageType == 0xB0){ //miltiple scanning Freq for TBC // leo 20230427 for tbc ota
                            Log.d(TAG,"allen_test mMultiSacnFreq (for TB)");
                            List<LinkageDescriptor.MultiSacnFreq> multiSacnFreq=tempDescriptor.mMultiSacnFreq;
                            for (LinkageDescriptor.MultiSacnFreq temp2 : multiSacnFreq){
                                Log.d(TAG,"allen_test mBatWhiteList BatNumber = " + temp2.HomeFreq);
                                Log.d(TAG,"allen_test mBatWhiteList BatNumber = " + temp2.CandidateFreq);
                            }
                        }
                        else if(tempDescriptor.LinkageType == 0xB8) { // leo 20230427 for tbc ota
                            Log.d(TAG,"allen_test mOUITbc (for TB)");
                            List<LinkageDescriptor.OUITBC> oUITbc=tempDescriptor.mOUITbc;
                            for (LinkageDescriptor.OUITBC temp2 : oUITbc){
                                Log.d(TAG,"allen_test mOUITbc OUI = " + temp2.Oui);
                                List<LinkageDescriptor.OUIDataTBC> ouidatadmg=temp2.mOUIDataTBC;
                                for (LinkageDescriptor.OUIDataTBC temp3 : ouidatadmg){
                                    Log.d(TAG,"allen_test mOUITbc HwVersion = " + temp3.HwVersion);
                                    Log.d(TAG,"allen_test mOUITbc SwVersion = " + temp3.SwVersion);
                                    Log.d(TAG,"allen_test mOUITbc DownloadPid = " + temp3.DownloadPid);
                                    Log.d(TAG,"allen_test mOUITbc ZipCode = " + temp3.ZipCode);
                                    Log.d(TAG,"allen_test mOUITbc OtaType = " + temp3.OtaType);
                                }
                            }
                        }
                        else if(tempDescriptor.LinkageType == 0xB9) { //eric lin 20240215 dmg ota
                            Log.d(TAG,"allen_test mOUIDmg (for TB)");
                            List<LinkageDescriptor.OUIDMG> oUIDmg=tempDescriptor.mOUIDmg;
                            for (LinkageDescriptor.OUIDMG temp2 : oUIDmg){
                                Log.d(TAG,"allen_test mOUIDmg OUI = " + temp2.Oui);
                                List<LinkageDescriptor.OUIDataDMG> ouidatadmg=temp2.mOUIDataDMG;
                                for (LinkageDescriptor.OUIDataDMG temp3 : ouidatadmg){
                                    Log.d(TAG,"allen_test mOUIDmg HwVersion = " + temp3.HwVersion);
                                    Log.d(TAG,"allen_test mOUIDmg SwVersion = " + temp3.SwVersion);
                                    Log.d(TAG,"allen_test mOUIDmg DownloadPid = " + temp3.DownloadPid);
                                    Log.d(TAG,"allen_test mOUIDmg ZipCode = " + temp3.ZipCode);
                                    Log.d(TAG,"allen_test mOUIDmg OtaType = " + temp3.OtaType);
                                    Log.d(TAG,"allen_test mOUIDmg OtaType = " + temp3.StartSn);
                                    Log.d(TAG,"allen_test mOUIDmg OtaType = " + temp3.EndSn);
                                }
                            }
                        }
                    }
                }

                Size=nitDataTmp.getTransportStreamList().size();
                Log.d(TAG,"allen_test getTransportStreamList==>CHANNEL_DESCRIPTOR");
                for(i=0;i<Size;i++){
                    Log.d(TAG,"allen_test i = " + i);
                    descBaseListTmp=nitDataTmp.getTransportStreamList().get(i).getDescriptor().getDescriptorList(CHANNEL_DESCRIPTOR);
                    Log.d(TAG,"allen_test descBaseListTmp.size() = " + descBaseListTmp.size());
                    for (DescBase temp : descBaseListTmp) {
                        ChannelDescriptor tempDescriptor = (ChannelDescriptor) temp;
                        List<ChannelDescriptor.ChannelDesc> channelDesc=tempDescriptor.mChannelDesc;
                        for (ChannelDescriptor.ChannelDesc temp2 : channelDesc){
                            Log.d(TAG,"allen_test mChannelDesc ServiceId = " + temp2.ServiceId);
                            Log.d(TAG,"allen_test mChannelDesc LogicalChannelNumber = " + temp2.LogicalChannelNumber);
                        }
                    }
                }

                Size=nitDataTmp.getNetworkStreamList().size();
                Log.d(TAG,"allen_test getNetworkStreamList==>NETWORK_PRODUCT_LIST_DESCRIPTOR");
                for(i=0;i<Size;i++){
                    descBaseListTmp=nitDataTmp.getNetworkStreamList().get(i).getDescriptor().getDescriptorList(NETWORK_PRODUCT_LIST_DESCRIPTOR);
                    Log.d(TAG,"allen_test descBaseListTmp.size() = " + descBaseListTmp.size());
                    for (DescBase temp : descBaseListTmp) {
                        NetworkProductListDescriptor tempDescriptor = (NetworkProductListDescriptor) temp;
                        List<NetworkProductListDescriptor.NetworkProductListDesc> networkProductListDesc=tempDescriptor.mNetworkProductListDesc;
                        for (NetworkProductListDescriptor.NetworkProductListDesc temp2 : networkProductListDesc){
                            Log.d(TAG,"allen_test NetworkProductListDesc ProductId = " + temp2.ProductId);
                            Log.d(TAG,"allen_test NetworkProductListDesc DisplayOrder = " + temp2.DisplayOrder);
                            Log.d(TAG,"allen_test NetworkProductListDesc DisplayMode = " + temp2.DisplayMode);
                            Log.d(TAG,"allen_test NetworkProductListDesc ISO639LanguageCode1 = " + temp2.ISO639LanguageCode1);
                            Log.d(TAG,"allen_test NetworkProductListDesc ProductNameEngLength = " + temp2.ProductNameEngLength);
                            Log.d(TAG,"allen_test NetworkProductListDesc ProductNameEng = " + temp2.ProductNameEng);
                            Log.d(TAG,"allen_test NetworkProductListDesc ISO639LanguageCode2 = " + temp2.ISO639LanguageCode2);
                            Log.d(TAG,"allen_test NetworkProductListDesc ProductNameChiLength = " + temp2.ProductNameChiLength);
                            Log.d(TAG,"allen_test NetworkProductListDesc ProductNameChi = " + temp2.ProductNameChi);
                        }
                    }
                }
            }

            Log.d(TAG,"allen_test Bat 1111111");
            //Bat batTmp=new Bat(mTuneInterface.getTuner(tuner_id),19123,true);
            Bat batTmp=new Bat(mTuneInterface.getTuner(tuner_id),601,true);
            batTmp.processWait();;

            BatData batDataTmp=batTmp.getBatData();
            if(batDataTmp != null){
                Log.d(TAG,"allen_test Bouquet Id = " + batDataTmp.getBouquetId());
                Log.d(TAG,"allen_test Version = " + batDataTmp.getVersion());

                List<BatData.BouquetNameStream> bouquetNameStreamList=batDataTmp.getBouquetNametreamList();
                for(BatData.BouquetNameStream temp : bouquetNameStreamList) {
                    Log.d(TAG,"allen_test Bouquet Name = " + temp.getBouquetName());
                }

                List<BatData.TransportStream> transportStreamList=batDataTmp.getTransportStreamList();
                for(BatData.TransportStream temp : transportStreamList) {
                    List<ServiceListDescriptor.Service> ServiceList=temp.getServiceList();
                    Log.d(TAG, "allen_test Transport Stream Id = " + temp.getTransportStreamId());
                    Log.d(TAG, "allen_test Original Network Id = " + temp.getOriginalNetworkId());
                    for(ServiceListDescriptor.Service temp2 : ServiceList) {
                        Log.d(TAG, "allen_test Service Id = " + temp2.ServiceID);
                        Log.d(TAG, "allen_test Service Type = " + temp2.ServiceType);
                    }
                }
            }
            else {
                Log.d(TAG, "allen_test No Bat Data Type");
            }

            Log.d(TAG,"allen_test Sdt 1111111");
            Sdt sdt_other;
            if(nitDataTmp == null){
                Log.d(TAG, "allen_test nitDataTmp is null, new Nit");
            }

            sdt_other = new Sdt(mTuneInterface.getTuner(tuner_id), false,true,nitDataTmp);
            sdt_other.processWait();

            List<SdtData> SdtData_tmp=sdt_other.getSdtData();
            int indexTmp,dataNum=SdtData_tmp.size();
            for(indexTmp=0;indexTmp<dataNum;indexTmp++) {
                int indexTmp2,dataNum2;
                List<DescBase> descBaseListTmp,descBaseListTmp2;
                dataNum2=SdtData_tmp.get(indexTmp).getServiceDataTotalNum();
                Log.d(TAG,"allen_test @@@@@@@@Transport Stream Id = " + SdtData_tmp.get(indexTmp).getTransportStreamIdByIndex(0));
                for(indexTmp2=0;indexTmp2<dataNum2;indexTmp2++){
                    Log.d(TAG,"allen_test Service Id = " + SdtData_tmp.get(indexTmp).getServiceIdByIndex(indexTmp2));
                    Log.d(TAG,"allen_test Service Name = " + SdtData_tmp.get(indexTmp).getServiceNameByIndex(indexTmp2));
                    Log.d(TAG,"allen_test geServiceData==>CHANNEL_CATEGORY_DESCRIPTOR");
                    descBaseListTmp=SdtData_tmp.get(indexTmp).geServiceData().get(indexTmp2).getDescriptor().getDescriptorList(CHANNEL_CATEGORY_DESCRIPTOR);
                    descBaseListTmp2=SdtData_tmp.get(indexTmp).geServiceData().get(indexTmp2).getDescriptor().getDescriptorList(CHANNEL_PRODUCT_LIST_DESCRIPTOR);
                    for (DescBase temp : descBaseListTmp) {
                        ChannelCategoryDescriptor tempDescriptor = (ChannelCategoryDescriptor) temp;
                        List<ChannelCategoryDescriptor.ChannelCategoryDesc> channelCategoryDesc=tempDescriptor.mChannelCategoryDesc;
                        for (ChannelCategoryDescriptor.ChannelCategoryDesc temp2 : channelCategoryDesc){
                            Log.d(TAG,"allen_test ChannelCategoryDesc CategoryType = " + temp2.CategoryType);
                        }
                    }
                    Log.d(TAG,"allen_test geServiceData==>mChannelProductListDesc");
                    for (DescBase temp : descBaseListTmp2) {
                        ChannelProductListDescriptor tempDescriptor = (ChannelProductListDescriptor) temp;
                        List<ChannelProductListDescriptor.ChannelProductListDesc> channelProductListDesc=tempDescriptor.mChannelProductListDesc;
                        for (ChannelProductListDescriptor.ChannelProductListDesc temp2 : channelProductListDesc){
                            Log.d(TAG,"allen_test ChannelProductListDesc ProductId = " + temp2.ProductId);
                        }
                    }
                }
            }
*/
/* Allen test start*/
/*
            quality=mTuneInterface.getQuality(0, tpInfo);
            for(int i=0; i<programInfoList.size();i++)
            {
                programInfoList.get(i).setQuality(quality);
            }
 */
            if(abort_scan == 0) { // if not abort, send callback of found program
                scan_callback_service(programInfoList);
            }
        }
        else
        {
            Log.d(TAG,"tuner not lock ");
        }

        return programInfoList;
    }
    private boolean scanAuto(TVScanParams scanData) {
        Log.d(TAG, "scanAuto ");
        int tp_num;
        List<Integer> tpIDList;
        List<ProgramInfo> programInfoList=null;
        boolean result=false;
        SatInfo satInfo = mDataManager.getSatInfo(scanData.getSatId());
        TpInfo tpInfo;
        tp_num= satInfo.getTpNum();
        tpIDList=satInfo.getTps();
        Log.d(TAG, "tp_num =" + tp_num);

        for(int i=0; i<tp_num;i++)
        {
            tpInfo=mDataManager.getTpInfo(tpIDList.get(i));
            scanData.setTpInfo(tpInfo);
            Log.d(TAG, "Freq =" + tpInfo.CableTp.getFreq());
            scanData.setTpId(tpIDList.get(i));
            scan_callback_progress(tpInfo.CableTp.getFreq(), tpIDList.get(i),100 * (i + 1)/tp_num);
            programInfoList=scanOneTp(scanData);
            FreeLowQualitySameService(programInfoList);
            AddProgramInfoList(programInfoList);
            if(abort_scan==1)
                return false;
        }
        return true;
    }
    private void FreeLowQualitySameService(List<ProgramInfo> programInfoList)
    {
        for(int i=0; i<mProgramInfoList.size();i++)
        {
            for(int j=0; j<programInfoList.size();j++)
            {
                if((mProgramInfoList.get(i).getTransportStreamId()==programInfoList.get(j).getTransportStreamId())&&
                        (mProgramInfoList.get(i).getOriginalNetworkId()==programInfoList.get(j).getOriginalNetworkId())&&
                        (mProgramInfoList.get(i).getServiceId()==programInfoList.get(j).getServiceId()))
                {
                    if(mProgramInfoList.get(i).getQuality()<programInfoList.get(j).getQuality())
                    {
                        mProgramInfoList.remove(i);
                    }
                    break;
                }
            }
        }

    }
    private void AddProgramInfoList(List<ProgramInfo> programInfoList)
    {
        mProgramInfoList.addAll(programInfoList);
    }
    private void setNitSearchIndex_bytunertype(TpInfo tpInfo, int tunerType, int value)
    {
        if(tunerType == EnNetworkType.CABLE.getValue())
            tpInfo.CableTp.setNitSearchIndex(value);
        else if(tunerType == EnNetworkType.TERRESTRIAL.getValue())
            tpInfo.TerrTp.setNitSearchIndex(value);
        else if(tunerType == EnNetworkType.SATELLITE.getValue())
            tpInfo.SatTp.setNitSearchIndex(value);
        /*
        switch (tunerType) {
            case DVBC: {
                tpInfo.CableTp.setNitSearchIndex(value);
            }
            break;
            case DVBT: {
                tpInfo.TerrTp.setNitSearchIndex(value);
            }
            break;
            case DVBS: {
                tpInfo.SatTp.setNitSearchIndex(value);
            }
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + tunerType);
        }

         */
    }
    private int getNitSearchIndex_bytunertype(TpInfo tpInfo, int tunerType)
    {
        if(tunerType == EnNetworkType.CABLE.getValue())
            return tpInfo.CableTp.getNitSearchIndex();
        else if(tunerType == EnNetworkType.TERRESTRIAL.getValue())
            return tpInfo.TerrTp.getNitSearchIndex();
        else if(tunerType == EnNetworkType.SATELLITE.getValue())
            return tpInfo.SatTp.getNitSearchIndex();
        else
            return 0;
        /*
        switch (tunerType) {
            case DVBC: {
                return tpInfo.CableTp.getNitSearchIndex();
            }
            case DVBT: {
                return tpInfo.TerrTp.getNitSearchIndex();
            }
            case DVBS: {
                return tpInfo.SatTp.getNitSearchIndex();
            }
            default:
                throw new IllegalStateException("Unexpected value: " + tunerType);
        }

         */
    }
    private void setNetWork_bytunertype(TpInfo tpInfo, int tunerType, int value)
    {
        if(tunerType == EnNetworkType.CABLE.getValue())
            tpInfo.CableTp.setNetWork(value);
        else if(tunerType == EnNetworkType.TERRESTRIAL.getValue())
            tpInfo.TerrTp.setNetWork(value);
        else if(tunerType == EnNetworkType.SATELLITE.getValue())
            tpInfo.SatTp.setNetWork(value);
        /*
        switch (tunerType) {
            case DVBC: {
                tpInfo.CableTp.setNetWork(value);
            }
            break;
            case DVBT: {
                tpInfo.TerrTp.setNetWork(value);
            }
            break;
            case DVBS: {
                tpInfo.SatTp.setNetWork(value);
            }
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + tunerType);
        }

         */
    }
    private int getNetWork_bytunertype(TpInfo tpInfo, int tunerType)
    {
        if(tunerType == EnNetworkType.CABLE.getValue())
            return tpInfo.CableTp.getNetWork();
        else if(tunerType == EnNetworkType.TERRESTRIAL.getValue())
            return tpInfo.TerrTp.getNetWork();
        else if(tunerType == EnNetworkType.SATELLITE.getValue())
            return tpInfo.SatTp.getNetWork();
        else
            return 0;
        /*
        switch (tunerType) {
            case DVBC: {
                return tpInfo.CableTp.getNetWork();
            }
            case DVBT: {
                return tpInfo.TerrTp.getNetWork();
            }
            case DVBS: {
                return tpInfo.SatTp.getNetWork();
            }
            default:
                throw new IllegalStateException("Unexpected value: " + tunerType);
        }

         */
    }
    private void setSearchOrNot_bytunertype(TpInfo tpInfo, int tunerType, int value)
    {
        if(tunerType == EnNetworkType.CABLE.getValue())
            tpInfo.CableTp.setSearchOrNot(value);
        else if(tunerType == EnNetworkType.TERRESTRIAL.getValue())
            tpInfo.TerrTp.setSearchOrNot(value);
        else if(tunerType == EnNetworkType.SATELLITE.getValue())
            tpInfo.SatTp.setSearchOrNot(value);
        /*
        switch (tunerType) {
            case DVBC: {
                tpInfo.CableTp.setSearchOrNot(value);
            }
            break;
            case DVBT: {
                tpInfo.TerrTp.setSearchOrNot(value);
            }
            break;
            case DVBS: {
                tpInfo.SatTp.setSearchOrNot(value);
            }
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + tunerType);
        }

         */
    }
    private int getSearchOrNot_bytunertype(TpInfo tpInfo, int tunerType)
    {
        if(tunerType == EnNetworkType.CABLE.getValue())
            return tpInfo.CableTp.getSearchOrNot();
        else if(tunerType == EnNetworkType.TERRESTRIAL.getValue())
            return tpInfo.TerrTp.getSearchOrNot();
        else if(tunerType == EnNetworkType.SATELLITE.getValue())
            return tpInfo.SatTp.getSearchOrNot();
        else
            return 0;
        /*
        switch (tunerType) {
            case DVBC: {
                return tpInfo.CableTp.getSearchOrNot();
            }
            case DVBT: {
                return tpInfo.TerrTp.getSearchOrNot();
            }
            case DVBS: {
                return tpInfo.SatTp.getSearchOrNot();
            }
            default:
                throw new IllegalStateException("Unexpected value: " + tunerType);
        }

         */
    }
    private boolean scanNIT(TVScanParams scanData) {
        int i, j;
        List<Integer> tpIDList;
        List<ProgramInfo> programInfoList=null;
        SatInfo satInfo = mDataManager.getSatInfo(scanData.getSatId());
        TpInfo tpInfo;
        int tunerType = mDataManager.TUNR_TYPE;
        int nit_index = 0xffff, search_index = 0;

        Log.d(TAG, "tunerType:"+tunerType);

        tpIDList = satInfo.getTps();
        nit_search_index=0;
        for(i = 0; i < satInfo.getTpNum(); i++)
        {
            tpInfo = mDataManager.getTpInfo(tpIDList.get(i));
            setNitSearchIndex_bytunertype(tpInfo, tunerType, 0);
            setNetWork_bytunertype(tpInfo, tunerType, 0);
            setSearchOrNot_bytunertype(tpInfo, tunerType, 0);
        }

        programInfoList=scanOneTp(scanData);
        AddProgramInfoList(programInfoList);
        tpInfo = mDataManager.getTpInfo(tpIDList.get(scanData.getTpId()));
        setSearchOrNot_bytunertype(tpInfo, tunerType, 1);
        if(abort_scan==1) {
            mProgramInfoList.removeAll(programInfoList);
            return false;
        }
        for(i = 0; i < satInfo.getTpNum(); i++)
        {
            nit_index = 0xffff;
            for(j = 0; j < satInfo.getTpNum(); j++)
            {
                tpInfo = mDataManager.getTpInfo(tpIDList.get(j));
                if(getNetWork_bytunertype(tpInfo, tunerType)==1 && getSearchOrNot_bytunertype(tpInfo, tunerType)==0)
                {
                    if(getNitSearchIndex_bytunertype(tpInfo, tunerType)<nit_index && getNitSearchIndex_bytunertype(tpInfo, tunerType)>0)
                    {
                        nit_index = getNitSearchIndex_bytunertype(tpInfo, tunerType);
                        search_index = j;
                    }
                }

            }

            tpInfo = mDataManager.getTpInfo(tpIDList.get(search_index));
            if(getNetWork_bytunertype(tpInfo, tunerType)==1 && getSearchOrNot_bytunertype(tpInfo, tunerType)==0)
            {
                scanData.setTpInfo(tpInfo);
                scanData.setTpId(tpIDList.get(search_index));
                programInfoList = scanOneTp(scanData);
                AddProgramInfoList(programInfoList);
                setSearchOrNot_bytunertype(tpInfo, tunerType, 1);
                setNitSearchIndex_bytunertype(tpInfo, tunerType, 0);
                i = 0;
                if(abort_scan==1)
                    return false;
            }
        }
        nit_search_index=0;

        return true;
    }
    private void processWait() {

    }
    private void processNotify() {

    }
    private void run(TVScanParams mScanData) {
        ScanRunnable runnable = new ScanRunnable(mScanData);
        newThread(runnable);
    }
    private void process(TVScanParams mScanData) {
        try {
            LogUtils.d("run process: scan mode =" + mScanData.getScanMode());
            //sendCallbackMessage(DTVMessage.PESI_SVR_EVT_SRCH_BEGIN,1,2,null);
            switch (mScanData.getScanMode()) {
                case TVScanParams.SCAN_DMG_SEARCH:{
                    scan_callback_start();
                    mProgramInfoList=scanDMG(mScanData);
                    LogUtils.d("mProgramInfoList NUM = "+mProgramInfoList.size());
                    printScanResult(mProgramInfoList);
                    //setDisplayNumbers(mProgramInfoList);
                    scan_callback_finsh();
                }break;
                case TVScanParams.SCAN_TBC_SEARCH:{
                    scan_callback_start();
                    mProgramInfoList=scanTBC(mScanData);
                    Log.d(TAG, "mProgramInfoList NUM:" + mProgramInfoList.size());
                    printScanResult(mProgramInfoList);
                    //setDisplayNumbers(mProgramInfoList);
                    scan_callback_finsh();
                }break;
                case TVScanParams.SCAN_MODE_MANUAL:
                    scan_callback_start();
                    mProgramInfoList=scanOneTp(mScanData);
                    Log.d(TAG, "mProgramInfoList NUM:" + mProgramInfoList.size());
                    printScanResult(mProgramInfoList);
                    //if(abort_scan==1)
                    //    AddToDataManager(false);
                    setDisplayNumbers(mProgramInfoList);
                    scan_callback_finsh();
                    break;
                case TVScanParams.SCAN_MODE_AUTO:
                    scan_callback_start();
                    scanAuto(mScanData);
                    Log.d(TAG, "mProgramInfoList NUM:" + mProgramInfoList.size());
                    //printScanResult();
                    //if(abort_scan==1)
                    //    AddToDataManager(false);
                    setDisplayNumbers(mProgramInfoList);
                    scan_callback_finsh();
                    break;
                case TVScanParams.SCAN_MODE_NETWORK:
                    scan_callback_start();
                    Log.d(TAG, "NIT_SCAN");
                    scanNIT(mScanData);
                    //printScanResult();
                    setDisplayNumbers(mProgramInfoList);
                    scan_callback_finsh();
                    break;
                case TVScanParams.SCAN_MODE_ALL_SAT:
                    scan_callback_start();
                    //scanAllSat();
                    Log.d(TAG, "SCAN_MODE_ALL_SAT not implement");
                    //if(abort_scan==1)
                    //    AddToDataManager(false);
                    setDisplayNumbers(mProgramInfoList);
                    scan_callback_finsh();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + mScanData.getScanMode());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    void printScanResult(List<ProgramInfo> programInfoList){
        for(int i=0;i<programInfoList.size();i++) {
            Log.d(TAG, "Tp ID:" + programInfoList.get(i).getTpId());
            Log.d(TAG, "Sat ID:" + programInfoList.get(i).getSatId());
            Log.d(TAG, "TransportStreamId:" + programInfoList.get(i).getTransportStreamId());
            Log.d(TAG, "OriginalNetworkId:" + programInfoList.get(i).getOriginalNetworkId());
            Log.d(TAG, "ServiceId:" + programInfoList.get(i).getServiceId());
            Log.d(TAG, "Channel ID:" + programInfoList.get(i).getChannelId());
            Log.d(TAG, "DisplayName:" + programInfoList.get(i).getDisplayName());
            Log.d(TAG, "ChName_eng:" + programInfoList.get(i).getChName_eng());
            Log.d(TAG, "ChName_chi:" + programInfoList.get(i).getChName_chi());
            Log.d(TAG, "Category_type:" + programInfoList.get(i).getCategory_type());
            Log.d(TAG, "LCN:" + programInfoList.get(i).getLCN());
            Log.d(TAG, "Type:" + programInfoList.get(i).getType());
            Log.d(TAG, "ParentCountryCode:" + programInfoList.get(i).getParentCountryCode());
            Log.d(TAG, "CA:" + programInfoList.get(i).getCA());
            Log.d(TAG, "Video PID:" + programInfoList.get(i).pVideo.getPID());
            Log.d(TAG, "Video Codec:" + programInfoList.get(i).pVideo.getCodec());
            Log.d(TAG, "Audio NUM:" + programInfoList.get(i).pAudios.size());
            for (int j = 0; j < programInfoList.get(i).pAudios.size(); j++) {
                Log.d(TAG, "Audio PID:" + programInfoList.get(i).pAudios.get(j).getPid());
                Log.d(TAG, "Audio Codec:" + programInfoList.get(i).pAudios.get(j).getCodec());
                Log.d(TAG, "Audio Left Lang:" + programInfoList.get(i).pAudios.get(j).getLeftIsoLang());
                Log.d(TAG, "Audio Right Lang:" + programInfoList.get(i).pAudios.get(j).getRightIsoLang());
            }
            Log.d(TAG, "Subtitle NUM:" + programInfoList.get(i).pSubtitle.size());
            for (int j = 0; j < programInfoList.get(i).pSubtitle.size(); j++) {
                Log.d(TAG, "Subtitle PID:" + programInfoList.get(i).pSubtitle.get(j).getPid());
                Log.d(TAG, "Subtitle Lang:" + programInfoList.get(i).pSubtitle.get(j).getLang());
                Log.d(TAG, "Subtitle AncPageId:" + programInfoList.get(i).pSubtitle.get(j).getAncPageId());
                Log.d(TAG, "Subtitle ComPageId:" + programInfoList.get(i).pSubtitle.get(j).getComPageId());
            }
            Log.d(TAG, "Teletext NUM:" + programInfoList.get(i).pTeletext.size());
            for (int j = 0; j < programInfoList.get(i).pTeletext.size(); j++) {
                Log.d(TAG, "Teletext PID:" + programInfoList.get(i).pTeletext.get(j).getPid());
            }
        }
    }
    private class ScanRunnable implements Runnable {
        TVScanParams ScanData;
        public ScanRunnable(TVScanParams mScanData) {
            ScanData=mScanData;
        }
        @Override
        public void run() {
            process(ScanData);
        }
    }
    private void newThread(ScanRunnable runnable) {
        new Thread(runnable, "Scan").start();
    }
    public void abort() {
        //setAbortFlag
        setScanAbortFlag(1);
        //processNotify();
    }
    public void AddToDataManager(boolean add)
    {
        if(add==true)
            mDataManager.addProgramInfoList(mProgramInfoList);
        else
        {
            //mProgramInfoList remove
            Log.d(TAG, "mProgramInfoList don't save to mDataManager");
        }
    }

    // TODO: program in different tp should not have same display number
    private void setDisplayNumbers(List<ProgramInfo> programInfoList) {
        int tvDisplayNumber = 1;
        int radioDisplayNumber = 1;

        for (ProgramInfo programInfo : programInfoList) {
            if(programInfo.getType() == ProgramInfo.PROGRAM_TV) {
                programInfo.setDisplayNum(tvDisplayNumber);
                tvDisplayNumber++;
            }
            else {
                programInfo.setDisplayNum(radioDisplayNumber);
                radioDisplayNumber++;
            }
        }
    }
}
