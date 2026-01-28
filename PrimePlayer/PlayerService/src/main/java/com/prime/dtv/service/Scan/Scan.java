package com.prime.dtv.service.Scan;

import static com.prime.dtv.service.Table.PmtData.PMT_MAX_TELETEXT_PID_NUM;

import android.content.Context;
import android.util.Log;

import com.prime.datastructure.TIF.TIFChannelData;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.FavInfo;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVScanParams;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.utils.TVTunerParams;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.ServiceInterface;
import com.prime.dtv.service.Table.Bat;
import com.prime.dtv.service.Table.BatData;
import com.prime.datastructure.sysdata.DTVMessage;
import com.prime.datastructure.sysdata.EnNetworkType;
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
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.TpInfo;

import java.util.ArrayList;
import java.util.Iterator;
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
    private Context mContext;
    public Scan(Context context, TVScanParams scanData, PesiDtvFrameworkInterfaceCallback callback){
        mTuneInterface = TunerInterface.getInstance(context);
        mScanData=scanData;
        mDataManager = DataManager.getDataManager(context);
        mCallback = callback;
        mScanUtils = new Scan_utils(context);
        mContext = context;
        //run(scanData);
    }
    private static int one_seg_flag = 1;
    public void startScan()
    {
        //run(mScanData);
        process(mScanData);
    }
    
    private void scan_callback_progress_freq(int freq, int tp_id, int value)
    {
        if(mCallback!=null) {
            mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_SRCH_LOCK_START, freq, tp_id, null);
            mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_SRCH_CUR_SCHEDULE, value, 0, null);
        }
    }

    private void scan_callback_progress(int sat_tp_num, int tp_id, int value)
    {
        if(mCallback!=null) {
            mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_SRCH_LOCK_START, sat_tp_num, tp_id, null);
            mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_SRCH_CUR_SCHEDULE, value, 0, null);
        }
    }

    private void scan_callback_start()
    {
        if(mCallback!=null) {
//            Log.d(TAG,"111 scan_callback_start send");
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
            if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_SUND))
                if(pmtData.getProgram_number() == 550 || pmtData.getProgram_number() == 551)
                    continue;
            //set programInfo
            programInfo.setPmtPid(pmtData.getPmtPid());
            programInfo.setPmtVersion(pmtData.getPMTVersion());
            programInfo.pVideo.setPID(pmtData.getVideo_pid(0));
            programInfo.setServiceId(pmtData.getProgram_number());
            programInfo.setChannelId((scanData.getTpId()<<16)|(pmtData.getProgram_number()));
            programInfo.pVideo.setCodec(pmtData.getVideo_stream_type(0));
            programInfo.setPcr(pmtData.getPcr_pid());
            mScanUtils.set_audio_language(programInfo,pmts.get(i));
            Log.d(TAG, "Teletext Ttxt_num:"+pmtData.getTtxt_desc_number());

            if(pmtData.getTtxt_desc_number()>PMT_MAX_TELETEXT_PID_NUM)
                teletext_num=PMT_MAX_TELETEXT_PID_NUM;
            else
                teletext_num=pmtData.getTtxt_desc_number();
            int newTeletextPid=  pmtData.getTeletext_pid(0);    
            if (programInfo.getTeletextPid() != newTeletextPid) {
                programInfo.setTeletextPid(newTeletextPid);
            }
    
            int newInitPage = pmtData.getTtxt_init_page();
            if (programInfo.getTeletextInitPage()!=newInitPage ) {
                programInfo.setTeletextInitPage(newInitPage);
            }
            //int pid = pmtData.getTeletext_pid(0);
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
                    other_type = false;
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
                                serviceType == Scan_cfg.ADVANCED_CODEC_HD_DIGITAL_TELEVISION_SERVICE ||
                                serviceType == Scan_cfg.ISDBT_ONESEG_SERVICE)
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
                        else if(serviceType == Scan_cfg.DATA_BROADCAST_SERVICE)
                            other_type =true;
                        else
                            ProgramInfoList.get(i).setType(ProgramInfo.PROGRAM_RADIO);

                        service_not_in_sdt = false;
                        break;
                    }
                }
                if(Scan_cfg.ONLY_SDT_SERVICE) {
                    if (other_type == true || service_not_in_sdt == true)
                    {
                        ProgramInfoList.get(i).setDeleteFlag(1);
                        other_type = false;
                    } else {
                        service_not_in_sdt = true;
                    }
                }
                else {
                    if (other_type == true) {
                        ProgramInfoList.get(i).setDeleteFlag(1);
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
                programInfo.setDisplayName(sdtData.getServiceName(tsID, serviceID));
                //programInfo.setCharacterCode();
                if (serviceType == Scan_cfg.DIGITAL_TELEVISION_SERVICE ||
                        serviceType == Scan_cfg.MPEG_2_HD_DIGITAL_TELEVISION_SERVICE ||
                        serviceType == Scan_cfg.ADVANCED_CODEC_SD_DIGITAL_TELEVISION_SERVICE ||
                        serviceType == Scan_cfg.ADVANCED_CODEC_HD_DIGITAL_TELEVISION_SERVICE) {
                    programInfo.setType(ProgramInfo.PROGRAM_TV);
                }
                else if(serviceType == Scan_cfg.DATA_BROADCAST_SERVICE) {
                    Log.d(TAG,"serviceType DATA_BROADCAST_SERVICE");
                }
                else {
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

        int tunerType = Pvcfg.getTunerType();
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
                    break;
                    case Scan_cfg.ISDBT: {
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

    private List<ProgramInfo> scanDigiturk(TVScanParams scanData){
        PrimeDtv primeDtv = PrimeDtv.getInstance();
        List<ProgramInfo> programInfoList=new ArrayList<>();
        TpInfo tpInfo=scanData.getTpInfo();
        int SatId = tpInfo.getSatId();
        SatInfo satInfo = mDataManager.getSatInfo(SatId);
        int tuner_id = scanData.getTunerId();
        Nit nit;
        Sdt sdt;
        Bat bat;
        NitData nitDataTmp=null;
        List<SdtData> sdtDataList = null;
        BatData batData=null;
        LogUtils.d("scanDigiturk IN");
        LogUtils.d("scanDigiturk Sat Name : "+satInfo.getSatName());
        LogUtils.d("scanDigiturk freq = "+tpInfo.SatTp.getFreq());
        LogUtils.d("scanDigiturk sym = "+tpInfo.SatTp.getSymbol());
        LogUtils.d("scanDigiturk pol = "+tpInfo.SatTp.getPolar());
        if (mTuneInterface.tune(tuner_id, tpInfo, satInfo)) {
            nit = new Nit(tuner_id, true);
            nit.processWait();
            nitDataTmp = nit.getNitData();
            if (nitDataTmp != null) {
//                mScanUtils.Scan_process_Nit(nitDataTmp);
                mScanUtils.Scan_process_Nit_Sat(nitDataTmp,SatId,mCallback);
                LogUtils.d("scanDigiturk Start sdt");
                sdt = new Sdt(tuner_id, true, true, nitDataTmp);
                sdt.processWait();
                sdtDataList = sdt.getSdtData();
                mScanUtils.Scan_process_Sdt_Digiturk(sdtDataList, programInfoList);
                LogUtils.d("scanDigiturk programInfoList.size(Scan_process_Sdt_Digiturk) = "+programInfoList.size());
                {
                    LogUtils.d("scanDigiturk mDataManager.getGposInfo().getBatId() = " + GposInfo.getBatId(primeDtv.getContext()));
                    bat = new Bat(tuner_id, GposInfo.getBatId(primeDtv.getContext()), false);
                    bat.processWait();
                    batData = bat.getBatData();
                    if (batData != null) {
                        LogUtils.d("scanDigiturk Got Bat");
                        mScanUtils.Scan_process_Bat_Digiturk(batData, programInfoList);
                        LogUtils.d("scanDigiturk programInfoList.size(Scan_process_Bat_Digiturk) = "+programInfoList.size());
                    }

//                    mScanUtils.no_lcn_channel_set_delete(programInfoList);
//                    LogUtils.d("scanDigiturk programInfoList.size(no_lcn_channel_set_delete) = "+programInfoList.size());
                    mScanUtils.remove_mark_delete_service(programInfoList);
                    LogUtils.d("scanDigiturk programInfoList.size(remove_mark_delete_service) = "+programInfoList.size());
                    mScanUtils.SortProgrameInfoByLCN(programInfoList);
                    LogUtils.d("scanDigiturk programInfoList.size(SortProgrameInfoByLCN) = "+programInfoList.size());
//                    mScanUtils.category_add_to_fav(programInfoList,true);
//                    LogUtils.d("scanDigiturk programInfoList.size(category_add_to_fav) = "+programInfoList.size());
                    if (abort_scan == 0) {
                        AddProgramInfoList(programInfoList);
                        scan_callback_service(programInfoList);
                        Pvcfg.setScanOneTP(0);
                        GposInfo.setSearchOneTPFlag(mContext, 0);
                    }
                }
            }
        }
        else{
            LogUtils.d("scanDigiturk tuner not lock ");
        }

        return programInfoList;
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
                mScanUtils.Scan_process_Nit(nitDataTmp, mCallback);
            
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
                    mScanUtils.category_add_to_fav(programInfoList,true);
                    LogUtils.d("scanTBC programInfoList.size(category_add_to_fav) = "+programInfoList.size());
                    if (abort_scan == 0) {
                        AddProgramInfoList(programInfoList);
                        scan_callback_service(programInfoList);
                        Pvcfg.setScanOneTP(0);
                        GposInfo.setSearchOneTPFlag(mContext, 0);
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
    private List<ProgramInfo> scanCNS(TVScanParams scanData){
        List<ProgramInfo> programInfoList=new ArrayList<>();
        TpInfo tpInfo=scanData.getTpInfo();
        int tuner_id = scanData.getTunerId();
        Nit nit;
        Sdt sdt;
        Bat bat;
        NitData nitDataTmp=null;
        List<SdtData> sdtDataList = null;
        BatData batData=null;
        LogUtils.d("scanCNS IN");
        LogUtils.d("scanCNS freq = "+tpInfo.CableTp.getFreq());
        LogUtils.d("scanCNS sym = "+tpInfo.CableTp.getSymbol());
        LogUtils.d("scanCNS qam = "+tpInfo.CableTp.getQam());
        if (mTuneInterface.tune(tuner_id, tpInfo)) {
            nit = new Nit(tuner_id, true);
            nit.processWait();
            scan_callback_progress(0 , 0 ,10);
            nitDataTmp = nit.getNitData();
            if (nitDataTmp != null) {
                LogUtils.d("scanCNS Start sdt");
                sdt = new Sdt(tuner_id, true, true, nitDataTmp);
                sdt.processWait();
				scan_callback_progress(0 , 0 ,20);
                sdtDataList = sdt.getSdtData();
                LogUtils.d("scanCNS Stop sdt, sdtDataList.size() = " + sdtDataList.size());

                LogUtils.d("scanCNS Bat id ["+Pvcfg.getBatId());
                bat = new Bat(tuner_id, Pvcfg.getBatId(), false);
                bat.processWait();
                scan_callback_progress(0 , 0 ,30);
                batData = bat.getBatData();
                if (batData != null) {
                    LogUtils.d("scanCNS Got Bat");
                    mScanUtils.Scan_process_Bat_CNS(batData, programInfoList);
                    scan_callback_progress(0 , 0 ,40);
                    LogUtils.d("scanCNS programInfoList.size(Scan_process_Bat) = "+programInfoList.size());
                    mScanUtils.Scan_process_Nit(nitDataTmp, mCallback);
                    scan_callback_progress(0 , 0 ,50);
                    LogUtils.d("scanCNS programInfoList.size(Scan_process_Nit_TBC) = "+programInfoList.size());
                    mScanUtils.Scan_process_Sdt_CNS(sdtDataList, programInfoList);
                    scan_callback_progress(0 , 0 ,60);
                    LogUtils.d("scanCNS programInfoList.size(Scan_process_Sdt_TBC) = "+programInfoList.size());
                    mScanUtils.no_lcn_channel_set_delete(programInfoList);
                    scan_callback_progress(0 , 0 ,70);
                    LogUtils.d("scanCNS programInfoList.size(no_lcn_channel_set_delete) = "+programInfoList.size());
                    mScanUtils.no_channelId_channel_set_delete(programInfoList);
                    scan_callback_progress(0 , 0 ,80);
                    LogUtils.d("scanCNS programInfoList.size(no_channelId_channel_set_delete) = "+programInfoList.size());
                    mScanUtils.remove_mark_delete_service(programInfoList);
                    scan_callback_progress(0 , 0 ,90);
                    LogUtils.d("scanCNS programInfoList.size(remove_mark_delete_service) = "+programInfoList.size());
                    mScanUtils.SortProgrameInfoByLCN(programInfoList);
                    LogUtils.d("scanCNS programInfoList.size(SortProgrameInfoByLCN) = "+programInfoList.size());
//                    {
//                        for(ProgramInfo p : programInfoList) {
//                            TpInfo tp = mDataManager.getTpInfo(p.getTpId());
//                            Log.d("kkkk","scan p["+p.getDisplayNum()+"]"+
//                                    " Name = "+p.getDisplayName() + " freq = "+tp.CableTp.getFreq()+
//                                    " SID = "+p.getServiceId());
//                        }
//                    }
                    mScanUtils.category_add_to_fav(programInfoList,true);
                    LogUtils.d("scanCNS programInfoList.size(category_add_to_fav) = "+programInfoList.size());
                }
                else {
                    LogUtils.d("scanCNS No Bat");
                    //mScanUtils.Scan_process_Nit_CNS(nitDataTmp, false, programInfoList);
                }
                if (abort_scan == 0) {
                    AddProgramInfoList(programInfoList);
                    scan_callback_service(programInfoList);
                    Pvcfg.setScanOneTP(0);
                    GposInfo.setSearchOneTPFlag(mContext, 0);
                }
            }
        }
        else{
            LogUtils.d("scanCNS tuner not lock ");
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
                mScanUtils.Scan_process_Nit(nitDataTmp,mCallback);
                mScanUtils.Scan_process_Sdt_DMG(sdtDataList, programInfoList);
                mScanUtils.Scan_process_Nit_DMG(nitDataTmp, programInfoList);

                mScanUtils.no_lcn_channel_set_delete(programInfoList);
                mScanUtils.remove_mark_delete_service(programInfoList);

                mScanUtils.SortProgrameInfoByLCN(programInfoList);

                mScanUtils.category_add_to_fav(programInfoList,true);
                if (abort_scan == 0) {
                    AddProgramInfoList(programInfoList);
                    scan_callback_service(programInfoList);
                    Pvcfg.setScanOneTP(0);
                    GposInfo.setSearchOneTPFlag(mContext, 0);
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

    private List<ProgramInfo> scanSUND(TVScanParams scanData){
        List<ProgramInfo> programInfoList=new ArrayList<>();

        boolean result=false;
        int SatId=scanData.getSatId();
        SatInfo satInfo = mDataManager.getSatInfo(SatId);
        TpInfo tpInfo;

        int tuner_id = scanData.getTunerId();
        int BatId=0x3E8;
        /*
        u16 BouquetIDAry[BAT_GROUP_NUM]={0x3E8,0x3E9,0x3EA,0x3EB,0x3EC,0x3ED,0x3EE,0x3EF,0x3F0,0x3F1,0x3F2,0x3F3,0x3F4,0x3F5,0x3F6,0x3F7,0x3F8,0x3F9,0x3FA,0x3FB,0x3FC};
         */
        Nit nit;
        Sdt sdt;
        Bat bat;
        NitData nitDataTmp=null;
        List<SdtData> sdtDataList = null;
        List<BatData> batDataList=null;
        boolean tuner_lock=false;
        tpInfo=scanData.getTpInfo(); //mDataManager.getTpInfo(satInfo.getTps().get(scanData.getTpId()));
        Log.d(TAG, "@@@@@@ freq" + tpInfo.SatTp.getFreq());
        Log.d(TAG, "@@@@@@ sym" + tpInfo.SatTp.getSymbol());
        Log.d(TAG, "@@@@@@ polar " + tpInfo.SatTp.getPolar());
        TVTunerParams tunerParams = TVTunerParams.CreateTunerParamDVBS(scanData.getTunerId(),satInfo.getSatId(),
                scanData.getTpId(),tpInfo.SatTp.getFreq(),tpInfo.SatTp.getSymbol(),tpInfo.SatTp.getPolar(),
                tpInfo.SatTp.getFec(),tpInfo.SatTp.getMod());
        tuner_lock = mTuneInterface.tune(tuner_id, tpInfo, satInfo);

        if (tuner_lock){
            LogUtils.d("@@@@@@ tuner_lock is true");
            LogUtils.d("@@@@@@ new Nit");
            nit = new Nit(tuner_id, true);
            nit.processWait();
            LogUtils.d("@@@@@@ getNitData");
            nitDataTmp = nit.getNitData();
            if((nitDataTmp != null) && (nitDataTmp.getTransportStreamList().get(0).getOriginalNetworkID() == Scan_cfg.SUNTV_NEtWORK_ID)){
                LogUtils.d("@@@@@@ Scan_process_Nit_Sat");
                mScanUtils.Scan_process_Nit_Sat(nitDataTmp,SatId,mCallback);
                LogUtils.d("@@@@@@ new Sdt");
                sdt = new Sdt(tuner_id, true, true, nitDataTmp);
                sdt.processWait();
                LogUtils.d("@@@@@@ getSdtData");
                sdtDataList = sdt.getSdtData();
                if((sdtDataList != null) && (sdtDataList.size() > 0)) {
                    LogUtils.d("@@@@@@ Scan_process_Sdt_SUND,sdtDataList.size() = " + sdtDataList.size());
                    mScanUtils.Scan_process_Sdt_SUND(sdtDataList, programInfoList);
                    LogUtils.d("@@@@@@ new Bat");
                    bat = new Bat(tuner_id, Scan_cfg.SUNTV_BOUQUET_ID, true);
                    bat.processWait();
                    LogUtils.d("@@@@@@ Scan_process_Nit_SUND, programInfoList.size()" + programInfoList.size());
                    mScanUtils.Scan_process_Nit_SUND(nitDataTmp,programInfoList);
                    //bat.processWait();
                    LogUtils.d("@@@@@@ getBatDataList");
                    batDataList = bat.getBatDataList();

                    if((batDataList != null) && (batDataList.size() > 0)){
                        LogUtils.d("@@@@@@ no_lcn_channel_set_delete");
                        mScanUtils.no_lcn_channel_set_delete(programInfoList);
                        LogUtils.d("@@@@@@ remove_mark_delete_service");
                        mScanUtils.remove_mark_delete_service(programInfoList);
                        LogUtils.d("@@@@@@ SortProgrameInfoByLCN");
                        mScanUtils.SortProgrameInfoByLCN(programInfoList);
                        LogUtils.d("@@@@@@ Scan_process_Bat_SUND,batDataList.size() = " + batDataList.size());
                        mScanUtils.Scan_process_Bat_SUND(batDataList, programInfoList);
                        //mScanUtils.category_add_to_fav_SUND(programInfoList);
                        LogUtils.d("@@@@@@ programInfoList.size = "+programInfoList.size());
                        if (abort_scan == 0) {
                            AddProgramInfoList(programInfoList);
                            //Pvcfg.setScanOneTP(0);
                            //mDataManager.getGposInfo().setSearchOneTPFlag(0);

                            Pvcfg.setScanOneTP(1);
                            GposInfo.setSearchOneTPFlag(mContext, 1);

                            scan_callback_service(programInfoList);
                        }
                        }
                    }
                }
            }
        else{
            LogUtils.d("@@@@@@ tuner_lock is false");
        }
        LogUtils.d("@@@@@@ programInfoList.size()" + programInfoList.size());
        return programInfoList;
    }

    private List<ProgramInfo> scanOneTp(TVScanParams scanData){
        List<ProgramInfo> programInfoList=new ArrayList<>();
        List<PmtData> pmts;
        PatData patData;
        int quality=0;
        boolean tuner_lock = false;
        //run table PAT/NIT/SDT/PMT
        int originalScanMode = scanData.getScanMode();
        if (originalScanMode != TVScanParams.SCAN_MODE_MANUAL) {
            Log.d(TAG, "scanOneTp: force SCAN_MODE_MANUAL (was " + originalScanMode + ")");
            scanData.setScanMode(TVScanParams.SCAN_MODE_MANUAL);
        }

        //TpInfo tpInfo=mDataManager.getTpInfo(scanData.getTpId());
        TpInfo tpInfo=scanData.getTpInfo();
        SatInfo satInfo= mDataManager.getSatInfo(scanData.getSatId());
        int tuner_id = scanData.getTunerId();
        Pat pat;
        Pmt pmt;
        Nit nit=null;
        Sdt sdt;
        mTuneInterface.cancelTune(tuner_id);
        if(satInfo.getTunerType() == TpInfo.DVBC) {
        Log.d(TAG,"freq"+tpInfo.CableTp.getFreq());
        Log.d(TAG,"sym"+tpInfo.CableTp.getSymbol());
        Log.d(TAG,"qam"+tpInfo.CableTp.getQam());
            tuner_lock = mTuneInterface.tune(tuner_id, tpInfo);
        }
        else if(satInfo.getTunerType() == TpInfo.DVBS) {
            Log.d(TAG, "freq" + tpInfo.SatTp.getFreq());
            Log.d(TAG, "sym" + tpInfo.SatTp.getSymbol());
            Log.d(TAG, "polar " + tpInfo.SatTp.getPolar());
//            TVTunerParams tunerParams = TVTunerParams.CreateTunerParamDVBS(scanData.getTunerId(),satInfo.getSatId(),
//                    scanData.getTpId(),tpInfo.SatTp.getFreq(),tpInfo.SatTp.getSymbol(),tpInfo.SatTp.getPolar(),
//                    tpInfo.SatTp.getFec(),tpInfo.SatTp.getMod());
            tuner_lock = mTuneInterface.tune(tuner_id, tpInfo, satInfo);
        }
        else if(satInfo.getTunerType() == TpInfo.ISDBT) {
            Log.d(TAG, "freq" + tpInfo.TerrTp.getFreq());
            Log.d(TAG, "BW" + tpInfo.TerrTp.getBand());
            one_seg_flag = scanData.getOneSegment();
//            TVTunerParams tunerParams = TVTunerParams.CreateTunerParamISDBT(scanData.getTunerId(),satInfo.getSatId(),
//                    scanData.getTpId(),tpInfo.TerrTp.getFreq(),tpInfo.TerrTp.getBand());
            tuner_lock = mTuneInterface.tune(tuner_id, tpInfo, satInfo);
        }
        else if(satInfo.getTunerType() == TpInfo.DVBT) {
            Log.d(TAG, "freq" + tpInfo.TerrTp.getFreq());
            Log.d(TAG, "BW" + tpInfo.TerrTp.getBand());
//            TVTunerParams tunerParams = TVTunerParams.CreateTunerParamISDBT(scanData.getTunerId(),satInfo.getSatId(),
//                    scanData.getTpId(),tpInfo.TerrTp.getFreq(),tpInfo.TerrTp.getBand());
            tuner_lock = mTuneInterface.tune(tuner_id, tpInfo, satInfo);
        }
        Log.d(TAG, "@@@@@@ scanOneTp lock = " + tuner_lock);
        if (tuner_lock) {
            scan_callback_progress(0 , 0 ,10);
            pat = new Pat(tuner_id);
            pat.processWait();
            scan_callback_progress(0 , 0 ,20);
            if (abort_scan == 1)
                return programInfoList;
            patData = pat.getPatData();
            pmt = new Pmt(tuner_id, patData, satInfo.getTunerType());
            pmt.processWait();
            scan_callback_progress(0 , 0 ,30);
            if (abort_scan == 1)
                return programInfoList;
            pmts = pmt.getPmtDataList();
            if (pmts.size() != 0) {
                buildProgramInfoListByPMT(pmts, scanData, programInfoList);
                //sdt = new Sdt(tuner_id, true);
                sdt = new Sdt(tuner_id, true,false,null);
                sdt.processWait();
                scan_callback_progress(0 , 0 ,40);
                if (abort_scan == 1)
                    return programInfoList;
                if(sdt.getSdtData() != null)
                    addProgramInfoBySDT(sdt.getSdtData().get(0), programInfoList);
                if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG) || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC)) {
                    if (scanData.getNitSearch() == 1) {
                        nit = new Nit(tuner_id, true);
                        nit.processWait();
                        scan_callback_progress(0 , 0 ,50);
                        if (abort_scan == 1)
                            return programInfoList;
                        addProgramInfoByNIT(nit.getNitData(), scanData, programInfoList);
                    }
                }
                else {
                    int remote_id;
                    nit = new Nit(tuner_id, true);
                    nit.processWait();
                    scan_callback_progress(0 , 0 ,50);
                    if (abort_scan == 1)
                        return programInfoList;
                    NitData nitData = nit.getNitData();
                    Log.d(TAG,"scan nitData = "+nitData);
                    if(nitData != null){
                        if(satInfo.getTunerType() == TpInfo.ISDBT) {
                            if (one_seg_flag == 0) {
                                Iterator<ProgramInfo> iterator = programInfoList.iterator();
                                while (iterator.hasNext()) {
                                    ProgramInfo info = iterator.next();
                                    if (nitData.check_one_seg_service(info.getServiceId(), info.getOriginalNetworkId())) {
                                        iterator.remove();
                                    }
                                }
                            }
                            remote_id = nitData.getISDBT_remote_id();
                            for (int i = 0; i < programInfoList.size(); i++) {
                                if (remote_id != -1) {
                                    int serviceID = programInfoList.get(i).getServiceId() & 0x1f;
                                    int sub_remote_id;
                                    int lcn;
                                    String octalStr = Integer.toOctalString(serviceID);
                                    // 解析八进制字符串为整数
                                    sub_remote_id = Integer.parseInt(octalStr) + 1;
                                    lcn = remote_id * 100 + sub_remote_id;
                                    programInfoList.get(i).setLCN(lcn);
                                    programInfoList.get(i).setDisplayNum(lcn);
                                } else {
                                    int serviceID = programInfoList.get(i).getServiceId();
                                    int onID = programInfoList.get(i).getOriginalNetworkId();
                                    int lcn = nitData == null ? 0 : nitData.getLCN(serviceID, onID);
                                    if (lcn != 0) {
                                        programInfoList.get(i).setLCN(lcn);
                                        programInfoList.get(i).setDisplayNum(lcn);
                                    }
                                }
                            }
                        }else if(satInfo.getTunerType() == TpInfo.DVBC || satInfo.getTunerType() == TpInfo.DVBT) {
                            for (int i = 0; i < programInfoList.size(); i++) {
                                int serviceID = programInfoList.get(i).getServiceId();
                                int onID = programInfoList.get(i).getOriginalNetworkId();
                                int lcn = nitData == null ? 0 : nitData.getLCN(serviceID, onID);
                                if(lcn == 0) {
                                    if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_MNBC))
                                        lcn = serviceID;
                                }
                                if (lcn != 0) {
                                    programInfoList.get(i).setLCN(lcn);
                                    programInfoList.get(i).setDisplayNum(lcn);
                                }
                            }
                            if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS)) {
                                Bat bat = new Bat(tuner_id, Pvcfg.getBatId(), false);
                                bat.processWait();
                                BatData batData = bat.getBatData();
                                if(batData != null)
                                    mScanUtils.scan_process_Bat_CNS_setLcn(batData, programInfoList);
                            }
                            mScanUtils.SortProgrameInfoByLCN(programInfoList);
                        }
                    }
                }
                scan_callback_progress(0 , 0 ,60);
                mScanUtils.remove_mark_delete_service(programInfoList);
                scan_callback_progress(0 , 0 ,70);
                mScanUtils.category_add_to_fav(programInfoList,false);
                scan_callback_progress(0 , 0 ,80);
                Pvcfg.setScanOneTP(1);
                GposInfo.setSearchOneTPFlag(mContext, 1);
            }

            if(abort_scan == 0) { // if not abort, send callback of found program
                scan_callback_service(programInfoList);
            }
        }
        else
        {
            Log.d(TAG,"tuner not lock ");
        }
        scan_callback_progress(0 , 0 ,100);
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
            if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG) || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC))
                scan_callback_progress_freq(tpInfo.CableTp.getFreq(), tpIDList.get(i),100 * (i + 1)/tp_num);
            else
                scan_callback_progress(i, tpIDList.get(i),100 * (i + 1)/tp_num);
            programInfoList=scanOneTp(scanData);
            FreeLowQualitySameService(programInfoList);
            AddProgramInfoList(programInfoList);
            if(abort_scan==1)
                return false;
        }
        return true;
    }

    private boolean scanOneSat(TVScanParams scanData) {
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
        if(satInfo.getTunerType() == TpInfo.ISDBT)
            one_seg_flag = scanData.getOneSegment();
        for(int i=0; i<tp_num;i++)
        {
            tpInfo=mDataManager.getTpInfo(tpIDList.get(i));
            scanData.setTpInfo(tpInfo);
            //Log.d(TAG, "Freq =" + tpInfo.SatTp.getFreq());//centaur 20250313 fix crash when SatTp NULL
            scanData.setTpId(tpIDList.get(i));
            scan_callback_progress(i, tpIDList.get(i),100 * (i + 1)/tp_num);
            programInfoList=scanOneTp(scanData);
            FreeLowQualitySameService(programInfoList);
            AddProgramInfoList(programInfoList);
            if(abort_scan==1)
                return false;
        }
        return true;
    }

    private boolean scanAllSat(TVScanParams scanData) {
        Log.d(TAG, "scanAuto ");
        int tp_num,sat_num,i,j;
        List<Integer> tpIDList;
        List<ProgramInfo> programInfoList=null;
        boolean result=false;
        SatInfo satInfo;
        sat_num=mDataManager.getSatInfoList().size();
        for(j=0;j<sat_num;j++) {
            satInfo = mDataManager.getSatInfo(mDataManager.getSatInfoList().get(j).getSatId());
            TpInfo tpInfo;
            tp_num = satInfo.getTpNum();
            tpIDList = satInfo.getTps();
            for (i = 0; i < tp_num; i++) {
                tpInfo = mDataManager.getTpInfo(tpIDList.get(i));
                scanData.setTpInfo(tpInfo);
                Log.d(TAG, "Freq =" + tpInfo.SatTp.getFreq());
                scanData.setTpId(tpIDList.get(i));
                scan_callback_progress(i, tpIDList.get(i), (100 * (i + 1) * (j + 1))/ (tp_num*sat_num));
                programInfoList = scanOneTp(scanData);
                FreeLowQualitySameService(programInfoList);
                AddProgramInfoList(programInfoList);
                if (abort_scan == 1)
                    return false;
            }
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
        int tunerType = Pvcfg.getTunerType();
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
            LogUtils.d("insertChannelList run process: scan mode =" + mScanData.getScanMode());
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
                case TVScanParams.SCAN_CNS_SEARCH:{
                    scan_callback_start();
                    mProgramInfoList=scanCNS(mScanData);
                    Log.d(TAG, "mProgramInfoList NUM:" + mProgramInfoList.size());
                    printScanResult(mProgramInfoList);
                    //setDisplayNumbers(mProgramInfoList);
                    printScanGroupResult(mDataManager.getFavGroupList());
                    scan_callback_finsh();
                }break;
                case TVScanParams.SCAN_SUND_SEARCH:
                    Log.d(TAG,"@@@@@@ TVScanParams.SCAN_SUND_SEARCH");
                    scan_callback_start();
                    mProgramInfoList=scanSUND(mScanData);
                    Log.d(TAG, "@@@@@@ mProgramInfoList NUM:" + mProgramInfoList.size());
                    //if(abort_scan==1)
                    //    AddToDataManager(false);
                    //setDisplayNumbers(mProgramInfoList);
                    printScanResult(mProgramInfoList);
                    printScanGroupResult(mDataManager.getFavGroupList());
                    scan_callback_finsh();
                    break;
                case TVScanParams.SCAN_MODE_MANUAL:
                    Log.d(TAG,"@@@@@@ TVScanParams.SCAN_MODE_MANUAL");
                    scan_callback_start();
                    mProgramInfoList=scanOneTp(mScanData);
                    Log.d(TAG, "@@@@@@ mProgramInfoList NUM:" + mProgramInfoList.size());
                    printScanResult(mProgramInfoList);
                    printScanGroupResult(mDataManager.getFavGroupList());
                    //if(abort_scan==1)
                    //    AddToDataManager(false);
//                    setDisplayNumbers(mProgramInfoList);
                    scan_callback_finsh();
                    break;
                case TVScanParams.SCAN_MODE_FAST_SCAN:
                    Log.d(TAG,"@@@@@@ TVScanParams.SCAN_MODE_FAST_SCAN");
                    scan_callback_start();
                    if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_SUND))
                        mProgramInfoList = scanSUND(mScanData);
                    else if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DIGITURK))
                        mProgramInfoList = scanDigiturk(mScanData);
                    else {
                        Log.e(TAG, "SCAN_MODE_FAST_SCAN not porting in this project");
                    }
                    Log.d(TAG, "@@@@@@ mProgramInfoList NUM:" + mProgramInfoList.size());
                    //printScanResult(mProgramInfoList);
                    //printScanGroupResult(mDataManager.getFavGroupList());
                    //if(abort_scan==1)
                    //    AddToDataManager(false);
                    //setDisplayNumbers(mProgramInfoList);
                    scan_callback_finsh();
                    break;
                case TVScanParams.SCAN_MODE_AUTO:
                    Log.d(TAG,"@@@@@@ TVScanParams.SCAN_MODE_AUTO");
                    scan_callback_start();
                    scanAuto(mScanData);
                    Log.d(TAG, "@@@@@@ mProgramInfoList NUM:" + mProgramInfoList.size());
                    //printScanResult();
                    //if(abort_scan==1)
                    //    AddToDataManager(false);
                    //setDisplayNumbers(mProgramInfoList);
                    printScanResult(mProgramInfoList);
                    scan_callback_finsh();
                    break;
                case TVScanParams.SCAN_MODE_NETWORK:
                    Log.d(TAG,"@@@@@@ TVScanParams.SCAN_MODE_NETWORK");
                    scan_callback_start();
                    Log.d(TAG, "NIT_SCAN");
                    scanNIT(mScanData);
                    Log.d(TAG, "@@@@@@ mProgramInfoList NUM:" + mProgramInfoList.size());
                    //printScanResult();
                    //setDisplayNumbers(mProgramInfoList);
                    printScanResult(mProgramInfoList);
                    scan_callback_finsh();
                    break;
                case TVScanParams.SCAN_MODE_ONE_SAT:
                    Log.d(TAG,"@@@@@@ TVScanParams.SCAN_MODE_ONE_SAT");
                    scan_callback_start();
                    scanOneSat(mScanData);
                    Log.d(TAG, "@@@@@@ mProgramInfoList NUM:" + mProgramInfoList.size());
                    //printScanResult();
                    //if(abort_scan==1)
                    //    AddToDataManager(false);
                    //setDisplayNumbers(mProgramInfoList);
                    printScanResult(mProgramInfoList);
                    scan_callback_finsh();
                    break;
                case TVScanParams.SCAN_MODE_ALL_SAT:
                    Log.d(TAG,"@@@@@@ TVScanParams.SCAN_MODE_ALL_SAT");
                    scan_callback_start();
                    scanAllSat(mScanData);
                    Log.d(TAG, "@@@@@@ mProgramInfoList NUM:" + mProgramInfoList.size());
                    //if(abort_scan==1)
                    //    AddToDataManager(false);
//                    setDisplayNumbers(mProgramInfoList);
                    printScanResult(mProgramInfoList);
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

    void printScanGroupResult(List<FavGroup> favGroupList){
        int i,j,size,favCnt;
        FavGroup favGroupSund;
        List<FavInfo> favInfo;
        size=favGroupList.size();
        Log.d(TAG, "@@@@@@## FavGroup List size = " + size);
        Log.d(TAG, "@@@@@@## FavGroupUsed = " + favGroupList.get(0).getFavGroupUsed());
        for(i=0;i<size;i++){
            favGroupSund=favGroupList.get(i);
            favInfo=favGroupSund.getFavInfoList();
            favCnt=favInfo.size();
            if(favCnt > 0) {
                Log.d(TAG, "@@@@@@ GroupIndex = " + i);
                Log.d(TAG, "@@@@@@## GroupName = " + favGroupSund.getFavGroupName().getGroupName());
                Log.d(TAG, "@@@@@@ getGroupType = " + favGroupSund.getFavGroupName().getGroupType());
                Log.d(TAG, "@@@@@@ favInfo.size() = " + favInfo.size());
                for (j = 0; j < favCnt; j++) {
                    Log.d(TAG, "@@@@@@## FavNum = " + favInfo.get(j).getFavNum());
                    Log.d(TAG, "@@@@@@ ChannelId = " + favInfo.get(j).getChannelId());
                    Log.d(TAG, "@@@@@@ FavMode = " + favInfo.get(j).getFavMode());
                }
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
        if(add==true) {
            boolean del_all_fav;
            if(/*mScanData.getTpInfo().getTunerType() == TpInfo.DVBC ||*/ (mScanData.getScanMode() != TVScanParams.SCAN_MODE_MANUAL
                    /*&& mScanData.getScanMode() != TVScanParams.SCAN_MODE_ONE_SAT*/) ) {
                if(mProgramInfoList != null && !mProgramInfoList.isEmpty()) {
                    mDataManager.addProgramInfoList(mProgramInfoList);
                    del_all_fav = true;
                }
                else {
                    Log.d(TAG, "mProgramInfoList is empty, not clear DataManager programInfoList !");
                    return;
                }
            }
            else {
                for(int i = 0; i < mProgramInfoList.size(); i++) {
                    mDataManager.addProgramInfo(mProgramInfoList.get(i));
//                    Log.d(TAG,"111 lcn "+mProgramInfoList.get(i).getDisplayNum()+" name = "+mProgramInfoList.get(i).getDisplayName());
                }
                del_all_fav = false;

            }
            mDataManager.resetLast_LCN(null);
            mDataManager.SetNewLcnIfNoLcn(null);
            mScanUtils.SortProgrameInfoByLCN(mDataManager.getProgramInfoList());
            mScanUtils.category_add_to_fav(mDataManager.getProgramInfoList(), del_all_fav);
            if(Pvcfg.isPrimeTvInputEnable())
                mDataManager.updateProgramInfoListToTIF(mScanData.getTpInfo().getTunerType());
//            if(Pvcfg.isPrimeTvInputEnable())
//                TIFChannelData.insertChannelList(ServiceInterface.getContext(),mProgramInfoList,mDataManager.GetTvInputId(),mScanData.getTpInfo().getTunerType());
//            for(int i = 0; i < mDataManager.getProgramInfoList().size(); i++) {
//                Log.d(TAG,"222 lcn "+mDataManager.getProgramInfoList().get(i).getDisplayNum()+" name = "+mDataManager.getProgramInfoList().get(i).getDisplayName());
//            }
        }
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
