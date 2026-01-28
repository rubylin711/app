package com.prime.dtv.service.Scan;

import static com.prime.dtv.service.Table.Desciptor.Descriptor.LINKAGE_DESC;
import static com.prime.dtv.sysdata.ProgramInfo.NUMBER_OF_AUDIO_IN_SIL;
import static com.prime.dtv.sysdata.ProgramInfo.PROGRAM_TV;

import android.content.Context;

import com.prime.dmg.launcher.ACSDatabase.ACSHelper;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.service.Table.BatData;
import com.prime.dtv.service.Table.Desciptor.CableDeliverySystemDescriptor;
import com.prime.dtv.service.Table.Desciptor.ChannelCategoryDescriptor;
import com.prime.dtv.service.Table.Desciptor.ChannelDescriptor;
import com.prime.dtv.service.Table.Desciptor.ChannelProductListDescriptor;
import com.prime.dtv.service.Table.Desciptor.DescBase;
import com.prime.dtv.service.Table.Desciptor.Descriptor;
import com.prime.dtv.service.Table.Desciptor.LinkageDescriptor;
import com.prime.dtv.service.Table.Desciptor.MultilingualServiceNameDescriptor;
import com.prime.dtv.service.Table.Desciptor.ServiceDescriptor;
import com.prime.dtv.service.Table.Desciptor.ServiceListDescriptor;
import com.prime.dtv.service.Table.NitData;
import com.prime.dtv.service.Table.PmtData;
import com.prime.dtv.service.Table.SdtData;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.datamanager.DefaultValue;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.FavInfo;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.MusicInfo;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.SatInfo;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.utils.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Scan_utils {
    private static DataManager mDataManager = null;
    private static final int MAX_CHANNEL = 4000;
    private static final boolean DEBUG_NIT_UPDATE = true;
    private static final boolean DEBUG_AUDIO_LANGUAGE = true;
    private int total_channel;
    private Context mContext;

    private static boolean IsBatInWhiteList;
    public Scan_utils(Context context){
        mDataManager = DataManager.getDataManager(context);
        mContext = context;
        IsBatInWhiteList = false;
    }
    public void set_audio_language(ProgramInfo programInfo, PmtData pmt)
    {
        String lang;
        int alloc_count = 0;
        int [] audio_alloc=new int [PmtData.PMT_MAX_AUDIO_PID_NUM];
        int i,j;
        int audio_pid;
        int audio_type;
        String audio_Llanguage;
        String audio_Rlanguage;
        for(i=0;i<audio_alloc.length;i++)
        {
            audio_alloc[i]=0xff;
        }
        for(i = 0; i < GposInfo.NUMBER_OF_AUDIO_LANGUAGE_SELECTION; i++)
        {
            lang=mDataManager.mGposInfo.getAudioLanguageSelection(i);
            for(j=0; j< PmtData.PMT_MAX_AUDIO_PID_NUM; j++) {
                if(pmt.getProgramMap().getAudio_pid(j)!=0) {
                    if ((lang != null) && (pmt.getProgramMap().getIso639LanguageCode1(j) != null)) {
                        if (lang.compareTo(pmt.getProgramMap().getIso639LanguageCode1(j)) == 0) {
                            if ((audio_alloc[j] == 0xff) && (alloc_count < NUMBER_OF_AUDIO_IN_SIL)) {
                                audio_alloc[j] = 0;
                                ProgramInfo.AudioInfo andioInfo = new ProgramInfo.AudioInfo(pmt.getProgramMap().getAudio_pid(j), pmt.getProgramMap().getAudio_stream_type(j), pmt.getProgramMap().getIso639LanguageCode1(j), pmt.getProgramMap().getIso639LanguageCode2(j));
                                programInfo.pAudios.add(alloc_count, andioInfo);
                                if(DEBUG_AUDIO_LANGUAGE) {
                                    LogUtils.d("Audio_pid:" + pmt.getProgramMap().getAudio_pid(j));
                                    LogUtils.d("alloc_count:" + alloc_count);
                                }
                                alloc_count++;
                            }
                        }
                    }
                }
            }
        }
        for(i = 0; (i < PmtData.PMT_MAX_AUDIO_PID_NUM) && (alloc_count < NUMBER_OF_AUDIO_IN_SIL); i++)
        {
            if(pmt.getProgramMap().getAudio_pid(i)!=0) {
                if (audio_alloc[i] == 0xff) {
                    if ((audio_alloc[i] == 0xff) && (alloc_count < NUMBER_OF_AUDIO_IN_SIL)) {
                        audio_alloc[i] = 0;
                    }
                    ProgramInfo.AudioInfo andioInfo = new ProgramInfo.AudioInfo(pmt.getProgramMap().getAudio_pid(i), pmt.getProgramMap().getAudio_stream_type(i), pmt.getProgramMap().getIso639LanguageCode1(i), pmt.getProgramMap().getIso639LanguageCode2(i));
                    programInfo.pAudios.add(alloc_count, andioInfo);
                    if(DEBUG_AUDIO_LANGUAGE) {
                        LogUtils.d("Audio_pid:" + pmt.getProgramMap().getAudio_pid(i));
                        LogUtils.d("alloc_count:" + alloc_count);
                    }
                    alloc_count++;
                }
            }
        }
    }

    public void UpdateTpId(int preId, int newId){
        List<ProgramInfo> programInfoList = mDataManager.getProgramInfoList();
        if(DEBUG_NIT_UPDATE)
            LogUtils.d("preId = "+preId+" newId = "+newId);
        for(ProgramInfo programInfo : programInfoList){
            if(programInfo.getTpId() == preId){
                if(DEBUG_NIT_UPDATE)
                    LogUtils.d(programInfo.getLCN()+" "+programInfo.getDisplayNum()+ " Changed Freq");
                programInfo.setTpId(newId);
            }
        }
    }
    public void CheckNitFreqChanged(CableDeliverySystemDescriptor pcds, int TransportStreamID, int original_network_id){
        List<TpInfo> tpInfoList=mDataManager.getTpInfoList();
        int found_freq_changed=0, i, j;
        for (i=0 ; i<tpInfoList.size(); i++){
            TpInfo tpInfo = tpInfoList.get(i);
            found_freq_changed=0;
            if(tpInfo.getTransport_id() == TransportStreamID && tpInfo.getOrignal_network_id() == original_network_id){
                if(pcds.Frequency != tpInfo.CableTp.getFreq()){
                    if(DEBUG_NIT_UPDATE)
                        LogUtils.d("Frequency not match........");
                    tpInfo.setTransport_id(0);
                    tpInfo.setOrignal_network_id(0);
                    for (j=0; j<tpInfoList.size(); j++){
                        TpInfo tpInfo2 = tpInfoList.get(j);
                        if(pcds.Frequency == tpInfo2.CableTp.getFreq()){
                            found_freq_changed = 1;
                            tpInfo2.setTransport_id(TransportStreamID);
                            tpInfo2.setOrignal_network_id(original_network_id);
                            tpInfo2.CableTp.setFreq(pcds.Frequency);
                            tpInfo2.CableTp.setSymbol(pcds.SymbolRate);
                            tpInfo2.CableTp.setQam(pcds.Qam);
                            tpInfo2.setStatus(MiscDefine.SaveStatus.STATUS_UPDATE);
                            UpdateTpId(i, j);
                        }
                    }
                    if(found_freq_changed == 0){
                        int tp_id = add_cable_with_compare(0, pcds.Frequency, pcds.Qam, pcds.Frequency, original_network_id,TransportStreamID);
                        UpdateTpId(i, tp_id);
                    }
                }
                else{
                    if(DEBUG_NIT_UPDATE)
                        LogUtils.d("TP["+i+"] FREQ not changed ["+tpInfo.CableTp.getFreq()+"] ["+pcds.Frequency+"]");
                }
            }
            else{
                //AUTO_UPDATE_TRACE("ts or onid different \n");
                //AUTO_UPDATE_TRACE("ptp->transportStreamId [%x] ptp->OriginalNetworkId [%x]\n", ptp->transportStreamId, ptp->OriginalNetworkId);
                //LogUtils.d("ts or onid different ");
                //LogUtils.d("pInfo.getTransport_id() ["+tpInfo.getTransport_id()+"] tpInfo.getOrignal_network_id() ["+tpInfo.getOrignal_network_id()+"]");
            }
        }
    }
    public void UpdateCableTPinfo(CableDeliverySystemDescriptor pcds, int transport_stream_id, int original_network_id)
    {
        int i;
        List<TpInfo> tpInfoList = mDataManager.getTpInfoList();
        TpInfo ptp;
        for(i= 0 ; i<tpInfoList.size(); i++){
            ptp = tpInfoList.get(i);
            if(ptp.CableTp.getFreq() == pcds.Frequency){
                LogUtils.d("TP["+i+"] FREQ ["+ptp.CableTp.getFreq()+"]");
                ptp.setOrignal_network_id(original_network_id);
                ptp.setTransport_id(transport_stream_id);
                ptp.CableTp.setSymbol(pcds.SymbolRate);
                ptp.CableTp.setQam(pcds.Qam-1);
                ptp.setStatus(MiscDefine.SaveStatus.STATUS_UPDATE);
                LogUtils.d("transport_stream_id ["+transport_stream_id+"]");
                LogUtils.d("original_network_id ["+original_network_id+"]");
                LogUtils.d("symbol_rate ["+pcds.SymbolRate+"]");
                LogUtils.d("qam ["+(pcds.Qam-1)+"]");
                break;
            }
        }
    }

    public boolean modifyCableTpInfo(int freq, int qam,int symbol)
    {
        List<SatInfo> satInfoList;
        List<TpInfo> tpInfoList;
        TpInfo tpInfo;
        int nit_search_index=0;
        if(freq<50000)
            return false;
        tpInfoList=mDataManager.getTpInfoList();
        satInfoList=mDataManager.getSatInfoList();
        int tpID;
        List<Integer> tpIDList;
        for(int i=0; i< satInfoList.size();i++)
        {
            tpIDList=satInfoList.get(i).getTps();
            for(int j=0; j<tpIDList.size();j++) {
                tpID=tpIDList.get(j);
                tpInfo = mDataManager.getTpInfo(tpID);
                if ((Math.abs(tpInfo.CableTp.getFreq() - freq) < 4) &&
                        (Math.abs(tpInfo.CableTp.getQam() - qam) < 4) &&
                        (Math.abs(tpInfo.CableTp.getSymbol() - symbol) < 4)) {
                    tpInfo.CableTp.setNetWork(1);
                    tpInfo.CableTp.setNitSearchIndex(++nit_search_index);
                    return true;
                }
            }
        }
        TpInfo TempTpInfo=new TpInfo(TpInfo.DVBC);
        TempTpInfo.setSatId(0);
        TempTpInfo.CableTp.setFreq(freq);
        TempTpInfo.CableTp.setQam(qam);
        TempTpInfo.CableTp.setSymbol(symbol);
        TempTpInfo.CableTp.setNetWork(1);
        TempTpInfo.CableTp.setNitSearchIndex(++nit_search_index);
        tpInfoList.add(TempTpInfo);
        return true;
    }
    public boolean modifySatTpInfo(int satID,int freq,int symbol,int polar,int FEC_inner)
    {
        int fgap,sgap;
        int frequency;
        int nit_search_index=0;
        List<SatInfo> satInfoList;
        List<TpInfo> tpInfoList;
        TpInfo tpInfo;
        if(symbol >= 100)
        {
            if(symbol < 5000)
            {
                fgap = 3;
                sgap = 100;
            }
            else if(symbol <  12000)
            {
                fgap = 4;
                sgap = 200;
            }
            else
            {
                fgap = 5;
                sgap = 2000;
            }
        }
        else
            return false;
        tpInfoList=mDataManager.getTpInfoList();
        satInfoList=mDataManager.getSatInfoList();
        int tpID;
        List<Integer> tpIDList;
        for(int i=0; i< satInfoList.size();i++)
        {
            tpIDList=satInfoList.get(i).getTps();
            for(int j=0; j<tpIDList.size();j++) {
                tpID = tpIDList.get(j);
                tpInfo = mDataManager.getTpInfo(tpID);
                frequency = tpInfo.SatTp.getFreq();
                if (tpInfo.SatTp.getPolar() == polar) {
                    if (frequency >= (freq - fgap)) {
                        if (frequency <= (freq + fgap)) {
                            if (symbol >= (tpInfo.SatTp.getSymbol() - sgap)) {
                                if (symbol <= (tpInfo.SatTp.getSymbol() + sgap)) {
                                    tpInfo.SatTp.setNetWork(1);
                                    tpInfo.SatTp.setNitSearchIndex(++nit_search_index);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        tpInfoList=mDataManager.getTpInfoList();
        TpInfo TempTpInfo=new TpInfo(TpInfo.DVBS);
        TempTpInfo.setSatId(satID);
        TempTpInfo.SatTp.setFreq(freq);
        TempTpInfo.SatTp.setPolar(polar);
        TempTpInfo.SatTp.setSymbol(symbol);
        TempTpInfo.SatTp.setFec(FEC_inner);
        TempTpInfo.SatTp.setNetWork(1);
        TempTpInfo.SatTp.setNitSearchIndex(++nit_search_index);
        tpInfoList.add(TempTpInfo);
        return true;
    }
    public boolean modifyTerrTpInfo(int freq ,int FFT,int collsetellation,int hierarchy,int guard_interval,int code_rate_H)
    {
        List<SatInfo> satInfoList;
        List<TpInfo> tpInfoList;
        TpInfo tpInfo;
        int nit_search_index=0;
        int last_chnum = 0;
        if(freq < 51000)
            return false;

        tpInfoList=mDataManager.getTpInfoList();
        satInfoList=mDataManager.getSatInfoList();
        int tpID;
        List<Integer> tpIDList;
        for(int i=0; i< tpInfoList.size();i++)
        {
            tpIDList=satInfoList.get(i).getTps();
            for(int j=0; j<tpIDList.size();j++) {
                tpID = tpIDList.get(j);
                tpInfo = mDataManager.getTpInfo(tpID);
                if (freq == tpInfo.TerrTp.getFreq()) {
                    last_chnum = 0;
                    tpInfo.TerrTp.setNetWork(1);
                    tpInfo.TerrTp.setNitSearchIndex(++nit_search_index);
                    return true;
                }
                last_chnum = tpInfo.TerrTp.getChannel();
            }
        }
        TpInfo TempTpInfo=new TpInfo(TpInfo.DVBT);
        TempTpInfo.setSatId(0);
        TempTpInfo.TerrTp.setFreq(freq);
        TempTpInfo.TerrTp.setFft(FFT);
        TempTpInfo.TerrTp.setGuard(guard_interval);
        TempTpInfo.TerrTp.setConst(collsetellation);
        TempTpInfo.TerrTp.setHierarchy(hierarchy);
        TempTpInfo.TerrTp.setCodeRate(code_rate_H);
        TempTpInfo.TerrTp.setNetWork(1);
        TempTpInfo.TerrTp.setNitSearchIndex(++nit_search_index);
        tpInfoList.add(TempTpInfo);
        return true;
    }
    public boolean modifyIsdbtTpInfo(int freq)
    {
        List<SatInfo> satInfoList;
        List<TpInfo> tpInfoList;
        TpInfo tpInfo;
        int nit_search_index=0;
        int last_chnum = 0;
        if(freq < 51000)
            return false;

        tpInfoList=mDataManager.getTpInfoList();
        satInfoList=mDataManager.getSatInfoList();
        int tpID;
        List<Integer> tpIDList;
        for(int i=0; i< tpInfoList.size();i++)
        {
            tpIDList=satInfoList.get(i).getTps();
            for(int j=0; j<tpIDList.size();j++) {
                tpID = tpIDList.get(j);
                tpInfo = mDataManager.getTpInfo(tpID);
                if (freq == tpInfo.TerrTp.getFreq()) {
                    last_chnum = 0;
                    tpInfo.TerrTp.setNetWork(1);
                    tpInfo.TerrTp.setNitSearchIndex(++nit_search_index);
                    return true;
                }
                last_chnum = tpInfo.TerrTp.getChannel();
            }
        }
        TpInfo TempTpInfo=new TpInfo(TpInfo.ISDBT);
        TempTpInfo.setSatId(0);
        TempTpInfo.TerrTp.setFreq(freq);
        TempTpInfo.TerrTp.setNetWork(1);
        TempTpInfo.TerrTp.setNitSearchIndex(++nit_search_index);
        tpInfoList.add(TempTpInfo);
        return true;
    }
    public int add_cable_with_compare(int sat_id, int freq, int	qam,int	symbol,int onid,int	tsid){
        int	j,k,frequency,temQAM,ret_val = -1;
        List<SatInfo> SatInfoList = mDataManager.getSatInfoList();
        List<TpInfo> TpInfoList = mDataManager.getTpInfoList();
        TpInfo pTp;
        SatInfo sat = SatInfoList.get(sat_id);

        temQAM=qam-1;
        frequency = freq;
        if(frequency < 50000)
        {
            LogUtils.e("BBBBBBBBBBBBBBBBBB");
            return -1;
        }

        for(j = 0; j < sat.getTpNum(); j++)
        {
            pTp = TpInfoList.get(sat.getTps().get(j));
            if((Math.abs(freq - pTp.CableTp.getFreq()) <4) && (Math.abs(symbol-pTp.CableTp.getSymbol()) <4) )
            {
                //LogUtils.i("j=["+j+"] Freq("+pTp.CableTp.getFreq()+") Symbol("+pTp.CableTp.getSymbol());
                break;
            }
        }
        if(j < sat.getTpNum())
        {
            pTp = TpInfoList.get(sat.getTps().get(j));
            ret_val = j;
            pTp.CableTp.setQam(temQAM);
            pTp.setTransport_id(tsid);
            pTp.setOrignal_network_id(onid);
            pTp.setNetwork_id(1);
            LogUtils.d("ret_val = "+ ret_val);
            pTp.setStatus(MiscDefine.SaveStatus.STATUS_UPDATE);
            return ret_val;
        }
        k = TpInfoList.size();
        pTp = new TpInfo(TpInfo.DVBC);
        pTp.CableTp.setFreq(freq);
        pTp.CableTp.setQam(temQAM);
        pTp.CableTp.setSymbol(symbol);
        pTp.setTransport_id(tsid);
        pTp.setOrignal_network_id(onid);
        pTp.setNetwork_id(1);
        pTp.setTpId(k);
        pTp.setStatus(MiscDefine.SaveStatus.STATUS_ADD);
        TpInfoList.add(pTp);
        sat.getTps().add(k);
        sat.setTpNum(sat.getTpNum()+1);
        ret_val = k;
        LogUtils.d("ret_val = "+ret_val+" sat.getTpNum() = "+sat.getTpNum());
        //AUTO_UPDATE_TRACE("ret_val=%d sat->ntp[%d]",ret_val,sat->Ntp);
        return ret_val;
    }
    public void addCompareService(List<ProgramInfo> ProgramInfoList, ProgramInfo programInfo)
    {
        int number_of_all_channel = 0;
        int tv_channel_number = 1;
        int radio_channel_number = 1;
        boolean add_flag=false;
        for (int i = 0; i < ProgramInfoList.size(); i++)
        {
            if(ProgramInfoList.get(i).getType() == ProgramInfo.PROGRAM_TV)
            {
                if(tv_channel_number <= ProgramInfoList.get(i).getDisplayNum())
                    tv_channel_number = ProgramInfoList.get(i).getDisplayNum() + 1;
            }
            else if((ProgramInfoList.get(i).getType() == ProgramInfo.PROGRAM_RADIO))
            {
                if(radio_channel_number <= ProgramInfoList.get(i).getDisplayNum())
                    radio_channel_number = ProgramInfoList.get(i).getDisplayNum() + 1;
            }
            number_of_all_channel++;
        }
        int index=0;
        for (int i = 0; i < ProgramInfoList.size(); i++)
        {
            if(programInfo.getTpId()==ProgramInfoList.get(i).getTpId()&&
                    programInfo.getServiceId()==ProgramInfoList.get(i).getServiceId())
            {
                ProgramInfoList.get(i).setLCN(programInfo.getLCN());
                ProgramInfoList.get(i).setType(programInfo.getType());
                index=i;
                break;
            }
        }
        if(index==0 && programInfo.getDisplayName()!=null)
        {
            add_flag=true;
            if(programInfo.getType() == ProgramInfo.PROGRAM_TV)
            {
                programInfo.setDisplayNum(tv_channel_number);
                if(tv_channel_number > MAX_CHANNEL)
                    add_flag = false;
                else
                    tv_channel_number++;
            }
            else if(programInfo.getType() == ProgramInfo.PROGRAM_RADIO)
            {
                programInfo.setDisplayNum(radio_channel_number);
                if(radio_channel_number > MAX_CHANNEL)
                    add_flag = false;
                else
                    radio_channel_number++;
            }
            if(add_flag==true)
            {
                ProgramInfoList.add(programInfo);
                number_of_all_channel++;
            }
        }
    }

    public void setBatInWhiteList(boolean available){
        IsBatInWhiteList = available;
    }

    public boolean getBatInWhiteList(){
        return IsBatInWhiteList;
    }
    public void Scan_process_Nit(NitData nitData){
        int i, j,Nit_version,Nit_id;
        LogUtils.d("IN");
        if(nitData == null)
            LogUtils.d("nitData = null");
        for(i=0, j=0; nitData != null && i<nitData.getTransportStreamList().size();i++) {
            NitData.TransportStream transportStream = nitData.getTransportStreamList().get(i);
            for(j=0 ; j<transportStream.getDescriptorList().size() ; j++){
                DescBase descriptor = transportStream.getDescriptorList().get(j);
                LogUtils.d("TAG = "+descriptor.Tag);
                if(descriptor.Tag == Descriptor.CABLE_DELIVERY_SYSTEM_DESC){
                    CableDeliverySystemDescriptor pcds = (CableDeliverySystemDescriptor)descriptor;
                    CheckNitFreqChanged(pcds, transportStream.getTransportStreamID(), transportStream.getOriginalNetworkID());
                    UpdateCableTPinfo(pcds, transportStream.getTransportStreamID(), transportStream.getOriginalNetworkID());
                }
            }
        }
        if(nitData.getNetworkStreamList().size() > 0) {
            Nit_version = nitData.getNetworkStreamList().get(0).getVersion();
            LogUtils.d("SI_TBC NitVersionNew = "+Nit_version+" NitVersionOld = "+mDataManager.getGposInfo().getNitVersion());
            mDataManager.getGposInfo().setNitVersion(Nit_version);
            Nit_id = nitData.getNetworkStreamList().get(0).getNetworkID();
            LogUtils.d("SI_TBC NitIdNew = "+Nit_id+" NitIdOld = "+mDataManager.getGposInfo().getSINitNetworkId());
            mDataManager.getGposInfo().setSINitNetworkId(Nit_id);
            ACSHelper.set_BoxNITIdToASC(mContext,Nit_id);
        }

        for(i=0, j=0; nitData != null && i<nitData.getNetworkStreamList().size();i++){
            List<DescBase> descBaseListTmp;
            NitData.NetworktStream  networktStream = nitData.getNetworkStreamList().get(i);
            descBaseListTmp=networktStream.getDescriptor().getDescriptorList(LINKAGE_DESC);
            for (DescBase temp : descBaseListTmp) {
                LinkageDescriptor tempDescriptor = (LinkageDescriptor) temp;
                if (tempDescriptor.LinkageType == 0xD2) {
                    List<LinkageDescriptor.BatWhiteList> batWhiteList = tempDescriptor.mBatWhiteList;
                    for (LinkageDescriptor.BatWhiteList temp2 : batWhiteList) {
                        List<Integer> batIdList = temp2.BatIdList;
                        for (Integer temp3 : batIdList) {
                            if (temp3 == mDataManager.getGposInfo().getBatId()) {
                                setBatInWhiteList(true);
                                LogUtils.d("scanTBC setBatInWhiteList(true)");
                                break;
                            }
                        }
                    }
                }
            }
        }
        LogUtils.d("OUT");
    }
    public int GetTpidByTSid(int ts_id){
        int tp_id = -1;
        List<TpInfo> tpInfoList = mDataManager.getTpInfoList();
        for(int i=0 ; i<tpInfoList.size(); i++){
            TpInfo ptp = tpInfoList.get(i);
            if(ptp.getTransport_id() == ts_id){
                tp_id = i;
            }
        }
        return tp_id;
    }

    public void Scan_process_Bat(BatData batData, List<ProgramInfo> ProgramInfoList){
        int transportStreamId , originalNetworkId;
        LogUtils.d("IN");
        if(batData != null){
            mDataManager.getGposInfo().setBatVersion(batData.getVersion());
            List<BatData.TransportStream> transportStreamList=batData.getTransportStreamList();
            for(BatData.TransportStream temp : transportStreamList) {
                transportStreamId=temp.getTransportStreamId();
                originalNetworkId=temp.getOriginalNetworkId();
                List<ServiceListDescriptor.Service> ServiceList=temp.getServiceList();
                for(ServiceListDescriptor.Service serviceTemp : ServiceList) {
                    //LogUtils.d("ServiceId = "+serviceTemp.ServiceID );
                    //LogUtils.d("ServiceType = "+serviceTemp.ServiceType);
                    if((serviceTemp.ServiceType == Scan_cfg.DIGITAL_TELEVISION_SERVICE) ||
                            (serviceTemp.ServiceType == Scan_cfg.MPEG_2_HD_DIGITAL_TELEVISION_SERVICE) ||
                            (serviceTemp.ServiceType == Scan_cfg.ADVANCED_CODEC_SD_DIGITAL_TELEVISION_SERVICE) ||
                            (serviceTemp.ServiceType == Scan_cfg.ADVANCED_CODEC_HD_DIGITAL_TELEVISION_SERVICE) ||
                            (serviceTemp.ServiceType == Scan_cfg.DIGITAL_RADIO_SOUND_SERVICE)) {
                        int ProgramType;
                        if (serviceTemp.ServiceType == Scan_cfg.DIGITAL_RADIO_SOUND_SERVICE)
                            ProgramType=ProgramInfo.PROGRAM_RADIO;
                        else
                            ProgramType= PROGRAM_TV;

                        //ProgramInfo programInfo = mDataManager.getProgramInfo(ProgramInfoList, serviceTemp.ServiceID, transportStreamId, originalNetworkId);
                        ProgramInfo programInfo = null;
                        if (programInfo != null) {
                            programInfo.setType(ProgramType);
                        }
                        else {
                            programInfo = new ProgramInfo();
                            programInfo.setType(ProgramType);
                            programInfo.setServiceId(serviceTemp.ServiceID);
                            programInfo.setTransportStreamId(transportStreamId);
                            programInfo.setOriginalNetworkId(originalNetworkId);
                            ProgramInfoList.add(programInfo);
                        }
                    }
                }
            }
        }
        else {
            LogUtils.d("No Bat Data");
        }
        LogUtils.d("OUT");
    }

    public void Scan_process_Nit_TBC(NitData nitData, boolean IsWhiteList, List<ProgramInfo> ProgramInfoList) {
        int i, j;
        int tsid, onid;
        LogUtils.d("IN");
        for (i = 0, j = 0; nitData != null && i < nitData.getTransportStreamList().size(); i++) {
            NitData.TransportStream transportStream = nitData.getTransportStreamList().get(i);
            tsid = transportStream.getTransportStreamID();
            onid = transportStream.getOriginalNetworkID();
            for (j = 0; j < transportStream.getDescriptorList().size(); j++) {
                DescBase desc = transportStream.getDescriptorList().get(j);
                //LogUtils.d("TAG = " + desc.Tag);
                if (desc.Tag == Descriptor.CHANNEL_DESCRIPTOR) {
                    ChannelDescriptor ch_descriptor = (ChannelDescriptor) desc;
                    List<ChannelDescriptor.ChannelDesc> channelDescList = ch_descriptor.mChannelDesc;
                    for (ChannelDescriptor.ChannelDesc channelDesc : channelDescList) {
                        ProgramInfo programInfo = mDataManager.getProgramInfo(ProgramInfoList, channelDesc.ServiceId, tsid, onid);
                        if (programInfo != null) {
                            programInfo.setLCN(channelDesc.LogicalChannelNumber);
                            programInfo.setDisplayNum(channelDesc.LogicalChannelNumber);
                        } else {
                            if(IsWhiteList == false) {
                                programInfo = new ProgramInfo();
                                programInfo.setLCN(channelDesc.LogicalChannelNumber);
                                programInfo.setDisplayNum(channelDesc.LogicalChannelNumber);
                                //programInfo.setType(ProgramType);
                                programInfo.setServiceId(channelDesc.ServiceId);
                                programInfo.setTransportStreamId(tsid);
                                programInfo.setOriginalNetworkId(onid);
                                programInfo.setTpId(GetTpidByTSid(tsid));
                                ProgramInfoList.add(programInfo);
                            }
                        }
                    }
                }
                if ((desc.Tag == Descriptor.SERVICE_LIST_DESC) && (IsWhiteList == false)) {
                    ServiceListDescriptor serviceListDescriptor = (ServiceListDescriptor) desc;
                    List<ServiceListDescriptor.Service> serviceListDescList = serviceListDescriptor.mServiceList;
                    for (ServiceListDescriptor.Service serviceTmp : serviceListDescList) {
                        if ((serviceTmp.ServiceType == Scan_cfg.DIGITAL_TELEVISION_SERVICE) ||
                                (serviceTmp.ServiceType == Scan_cfg.MPEG_2_HD_DIGITAL_TELEVISION_SERVICE) ||
                                (serviceTmp.ServiceType == Scan_cfg.ADVANCED_CODEC_SD_DIGITAL_TELEVISION_SERVICE) ||
                                (serviceTmp.ServiceType == Scan_cfg.ADVANCED_CODEC_HD_DIGITAL_TELEVISION_SERVICE) ||
                                (serviceTmp.ServiceType == Scan_cfg.DIGITAL_RADIO_SOUND_SERVICE)) {
                            int ProgramType;
                            if (serviceTmp.ServiceType == Scan_cfg.DIGITAL_RADIO_SOUND_SERVICE) {
                                ProgramType = ProgramInfo.PROGRAM_RADIO;
                            }
                            else {
                                ProgramType = ProgramInfo.PROGRAM_TV;
                            }
                            ProgramInfo programInfo = mDataManager.getProgramInfo(ProgramInfoList, serviceTmp.ServiceID, tsid, onid);

                            if (programInfo != null) {
                                programInfo.setType(ProgramType);
                            }
                            else {
                                programInfo = new ProgramInfo();
                                programInfo.setType(ProgramType);
                                programInfo.setServiceId(serviceTmp.ServiceID);
                                programInfo.setTransportStreamId(tsid);
                                programInfo.setOriginalNetworkId(onid);
                                programInfo.setTpId(GetTpidByTSid(tsid));
                                ProgramInfoList.add(programInfo);
                            }
                        }
                    }
                }
            }
        }
        LogUtils.d("OUT");
    }

    public void Scan_process_Sdt_TBC(List<SdtData> sdtDataList, List<ProgramInfo> ProgramInfoList){
        LogUtils.d("IN");
        int i,j,desc_index, tp_id, add_new=0;
        int programCnt,programIdx;

        List<TpInfo> tpInfoList=mDataManager.getTpInfoList();
        programCnt=ProgramInfoList.size();
        LogUtils.d("scanTBC programCnt = "+programCnt);

        total_channel = 0;
        if(programCnt > 0) {
            if (sdtDataList != null && sdtDataList.size() > 0) {
                for(i=0;i<sdtDataList.size();i++) {
                    List<SdtData.ServiceData> serviceDataList = sdtDataList.get(i).geServiceData();
                    //LogUtils.d("scanTBC serviceDataList.size() = " + serviceDataList.size());
                for (j = 0; j < serviceDataList.size(); j++) {
                    SdtData.ServiceData serviceData = serviceDataList.get(j);
                    add_new = 0;
                    tp_id = GetTpidByTSid(serviceData.getTransportStreamId());
                    if (tp_id == -1) {
                        LogUtils.d("TSID [" + tp_id + "] => Can't Find TP ID ");
                        continue;
                    }
                    tpInfoList.get(tp_id).setSdt_version(serviceData.getVersionNumber());

                    ProgramInfo programInfo = mDataManager.getProgramInfo(ProgramInfoList, serviceData.getServiceId(), serviceData.getTransportStreamId(), serviceData.getOriginalNetworkId());//new ProgramInfo();

                    if (programInfo == null) {
                        continue;
                    }
                        programInfo.setTpId(tp_id);
                    programInfo.setChannelId((tp_id << 16) | (programInfo.getServiceId()));
                    programInfo.setCA(serviceData.getFreeCaMode());
                    //LogUtils.d("scanTBC serviceData.getDescriptor().getDescList().size() = " + serviceData.getDescriptor().getDescList().size());
                    for (desc_index = 0; desc_index < serviceData.getDescriptor().getDescList().size(); desc_index++) {
                        DescBase desbase = serviceData.getDescriptor().getDescList().get(desc_index);
                        switch (desbase.Tag) {
                            case Descriptor.SERVICE_DESC: {
                                ServiceDescriptor serviceDescriptor = (ServiceDescriptor) desbase;
                                    //programInfo.setDisplayName(serviceDescriptor.ServiceName);
                                programInfo.setChName_eng(serviceDescriptor.ServiceName);
                                    //LogUtils.d("scanTBC programInfo.getDisplayName() = "+programInfo.getDisplayName());
                                    //LogUtils.d("scanTBC programInfo.setChName_eng() = "+programInfo.getDisplayName());
                                    //LogUtils.d("scanTBC serviceDescriptor.ServiceName = "+serviceDescriptor.ServiceName);
                            }
                            break;
                            case Descriptor.CHANNEL_CATEGORY_DESCRIPTOR: {
                                ChannelCategoryDescriptor channelCategoryDescriptor = (ChannelCategoryDescriptor) desbase;
                                for (ChannelCategoryDescriptor.ChannelCategoryDesc ccds : channelCategoryDescriptor.mChannelCategoryDesc) {
                                    programInfo.setCategory_type(ccds.CategoryType);
                                }
                            }
                            break;
                            case Descriptor.CHANNEL_PRODUCT_LIST_DESCRIPTOR: {
                                ChannelProductListDescriptor channelProductListDescriptor = (ChannelProductListDescriptor) desbase;
                                for (ChannelProductListDescriptor.ChannelProductListDesc cpldes : channelProductListDescriptor.mChannelProductListDesc) {
                                    programInfo.setService_product(cpldes.ProductId);
                                }
                            }
                            break;
                            case Descriptor.MULTILINGUAL_SERVICE_NAME_DESC: {
                                MultilingualServiceNameDescriptor multilingualServiceNameDescriptor = (MultilingualServiceNameDescriptor) desbase;
                                for (MultilingualServiceNameDescriptor.MultilingualServiceName multilingualServiceName : multilingualServiceNameDescriptor.mMultilingualServiceNameList) {
                                    programInfo.setChName_chi(multilingualServiceName.ServiceName);
                                        programInfo.setDisplayName(multilingualServiceName.ServiceName);
                                        //LogUtils.d("scanTBC programInfo.getChName_chi() = "+programInfo.getChName_chi());
                                        //LogUtils.d("scanTBC multilingualServiceName.ServiceName = "+multilingualServiceName.ServiceName);
                                        break;
                                }
                            }
                            break;
                        }
                    }
                    total_channel++;
                    //LogUtils.d("scanTBC Service ID = " + programInfo.getServiceId() + " Name: " + programInfo.getChName_eng() + " " + programInfo.getChName_chi());
                    //LogUtils.d("scanTBC total_channel = " + total_channel);
                    }
                }
            }
        }
        LogUtils.d("scanTBC total_channel = "+total_channel);
        LogUtils.d("OUT");
    }

    public void Scan_process_Nit_DMG(NitData nitData, List<ProgramInfo> ProgramInfoList){
        int i, j;
        int tsid , onid;
        LogUtils.d("IN");
        for(i=0, j=0; nitData != null && i<nitData.getTransportStreamList().size();i++) {
            NitData.TransportStream transportStream = nitData.getTransportStreamList().get(i);
            tsid = transportStream.getTransportStreamID();
            onid = transportStream.getOriginalNetworkID();
            LogUtils.d("tsid = "+tsid );
            LogUtils.d("onid = "+onid);
            for(j=0 ; j<transportStream.getDescriptorList().size() ; j++){
                DescBase desc = transportStream.getDescriptorList().get(j);
                LogUtils.d("TAG = "+desc.Tag);
                if(desc.Tag == Descriptor.CHANNEL_DESCRIPTOR){
                    ChannelDescriptor ch_descriptor = (ChannelDescriptor)desc;
                    List<ChannelDescriptor.ChannelDesc> channelDescList = ch_descriptor.mChannelDesc;
                    for(ChannelDescriptor.ChannelDesc channelDesc : channelDescList){
                        LogUtils.d("ServiceId = "+channelDesc.ServiceId );
                        LogUtils.d("LogicalChannelNumber = "+channelDesc.LogicalChannelNumber);
                        ProgramInfo programInfo = mDataManager.getProgramInfo(ProgramInfoList, channelDesc.ServiceId, tsid,onid);
                        if(programInfo != null){
                            programInfo.setLCN(channelDesc.LogicalChannelNumber);
                            programInfo.setDisplayNum(channelDesc.LogicalChannelNumber);
                            if(channelDesc.LogicalChannelNumber == 260){//power on switch to this channel but skip it in channel list
                                programInfo.setSkip(1);
                            }
                        }
                        else{
                            LogUtils.e("Cant get ProgramInfo");
                        }
                    }
                }
            }
        }
        LogUtils.d("OUT");
    }

    public void Scan_process_Sdt_DMG(List<SdtData> sdtDataList, List<ProgramInfo> ProgramInfoList){
        LogUtils.d("IN");
        int i,j,desc_index, tp_id, add_new=0;
        total_channel = 0;
        //for(i=0 ; i< sdtDataList.size() ; i++)
        List<TpInfo> tpInfoList = mDataManager.getTpInfoList();
        LogUtils.d("SSSSSS Scan_process_Sdt_DMG in");
        if(sdtDataList != null && sdtDataList.size()>0)
        {
            //LogUtils.d("sdtDataList.size() "+ sdtDataList.size());
            for(i=0;i<sdtDataList.size();i++) {
                List<SdtData.ServiceData> serviceDataList = sdtDataList.get(i).geServiceData();
                for (j = 0; j < serviceDataList.size(); j++) {
                    SdtData.ServiceData serviceData = serviceDataList.get(j);
                    add_new = 0;
                    tp_id = GetTpidByTSid(serviceData.getTransportStreamId());
                    if (tp_id == -1) {
                        LogUtils.d("TSID [" + tp_id + "] => Can't Find TP ID ");
                        continue;
                    }
                    LogUtils.d("SSSSSS serviceData.getTransportStreamId() = " + serviceData.getTransportStreamId());
                    LogUtils.d("SSSSSS serviceData.getVersionNumber() = " + serviceData.getVersionNumber());
                    tpInfoList.get(tp_id).setSdt_version(serviceData.getVersionNumber());
                    LogUtils.d("SSSSSS tpInfoList.get(" + tp_id + ").getSdt_version() = " + tpInfoList.get(tp_id).getSdt_version());
                    ProgramInfo programInfo = mDataManager.getProgramInfo(ProgramInfoList, serviceData.getServiceId(), serviceData.getTransportStreamId(), serviceData.getOriginalNetworkId());//new ProgramInfo();
                    if (programInfo == null) {
                        add_new = 1;
                        programInfo = new ProgramInfo();
                    }
                    programInfo.setTpId(tp_id);
                    programInfo.setTransportStreamId(serviceData.getTransportStreamId());
                    programInfo.setOriginalNetworkId(serviceData.getOriginalNetworkId());
                    programInfo.setServiceId(serviceData.getServiceId());
                    programInfo.setChannelId((tp_id << 16) | (programInfo.getServiceId()));
                    programInfo.setCA(serviceData.getFreeCaMode());
                    for (desc_index = 0; desc_index < serviceData.getDescriptor().getDescList().size(); desc_index++) {
                        DescBase desbase = serviceData.getDescriptor().getDescList().get(desc_index);
                        switch (desbase.Tag) {
                            case Descriptor.SERVICE_DESC: {
                                ServiceDescriptor serviceDescriptor = (ServiceDescriptor) desbase;
                                programInfo.setDisplayName(serviceDescriptor.ServiceName);
                                programInfo.setChName_eng(serviceDescriptor.ServiceName);
                                if (serviceDescriptor.ServiceType == Scan_cfg.DIGITAL_RADIO_SOUND_SERVICE)
                                    programInfo.setType(ProgramInfo.PROGRAM_RADIO);
                                else
                                    programInfo.setType(ProgramInfo.PROGRAM_TV);
                            }
                            break;
                            case Descriptor.CHANNEL_CATEGORY_DESCRIPTOR: {
                                ChannelCategoryDescriptor channelCategoryDescriptor = (ChannelCategoryDescriptor) desbase;
                                //LogUtils.d("CHANNEL_CATEGORY_DESCRIPTOR");
                                for (ChannelCategoryDescriptor.ChannelCategoryDesc ccds : channelCategoryDescriptor.mChannelCategoryDesc) {
                                    //LogUtils.d("CategoryType = "+ccds.CategoryType);
                                    programInfo.setCategory_type(ccds.CategoryType);
                                }
                                //programInfo.setCategory_type(channelCategoryDescriptor.CategoryType);
                            }
                            break;
                            case Descriptor.CHANNEL_PRODUCT_LIST_DESCRIPTOR: {
                                ChannelProductListDescriptor channelProductListDescriptor = (ChannelProductListDescriptor) desbase;
                                //LogUtils.d("CHANNEL_PRODUCT_LIST_DESCRIPTOR");
                                for (ChannelProductListDescriptor.ChannelProductListDesc cpldes : channelProductListDescriptor.mChannelProductListDesc) {
                                    //LogUtils.d("ProductId = "+cpldes.ProductId);
                                    programInfo.setService_product(cpldes.ProductId);
                                }
                            }
                            break;
                            case Descriptor.MULTILINGUAL_SERVICE_NAME_DESC: {
                                MultilingualServiceNameDescriptor multilingualServiceNameDescriptor = (MultilingualServiceNameDescriptor) desbase;
                                //LogUtils.d("MULTILINGUAL_SERVICE_NAME_DESC");
                                for (MultilingualServiceNameDescriptor.MultilingualServiceName multilingualServiceName : multilingualServiceNameDescriptor.mMultilingualServiceNameList) {
                                    //LogUtils.d("ISO639LanguageCode = "+multilingualServiceName.ISO639LanguageCode);
                                    //LogUtils.d("ServiceName = "+multilingualServiceName.ServiceName);
                                    //LogUtils.d("ServiceProviderName = "+multilingualServiceName.ServiceProviderName);
                                    //if(multilingualServiceName.ISO639LanguageCode)
                                    if(multilingualServiceName.ISO639LanguageCode.equals("chi")) {
                                        programInfo.setDisplayName(multilingualServiceName.ServiceName);
                                        programInfo.setChName_chi(multilingualServiceName.ServiceName);
                                    }
                                    if(multilingualServiceName.ISO639LanguageCode.equals("eng"))
                                        programInfo.setChName_eng(multilingualServiceName.ServiceName);
                                    LogUtils.d("ISO Language ["+mDataManager.getGposInfo().getOSDLanguage()+"] ["+multilingualServiceName.ISO639LanguageCode+"]");
                                    //if(mDataManager.getGposInfo().getOSDLanguage().equals(multilingualServiceName.ISO639LanguageCode)){
                                    //    programInfo.setDisplayName(multilingualServiceName.ServiceName);
                                    //}

                                }
                            }
                            break;
                        }
                    }
                    if (add_new == 1) {
                        ProgramInfoList.add(programInfo);
                        total_channel++;
                        LogUtils.d("Service ID = " + programInfo.getServiceId() + " Name: " + programInfo.getChName_eng() + " " + programInfo.getChName_chi());
                        LogUtils.d("total_channel = " + total_channel);
                    }
                }
            }
        }
        LogUtils.d("OUT");
    }
    public void no_lcn_channel_set_delete(List<ProgramInfo> ProgramInfoList) {
        for(ProgramInfo programInfo : ProgramInfoList){
            if((programInfo.getLCN() == 0)){
                programInfo.setDeleteFlag(1);
            }
        }
    }
    public void remove_programe_in_group(ProgramInfo programInfo){
        List<FavGroup> favGroupList = mDataManager.getFavGroupList();
        List<FavInfo> favInfoList;
        for(FavGroup favgroup : favGroupList){
            favInfoList = favgroup.getFavInfoList();
            Iterator<FavInfo> iterator = favInfoList.iterator();
            while(iterator.hasNext()){
                FavInfo favInfo = iterator.next();
                if(favInfo.getChannelId() == programInfo.getChannelId()){
                    iterator.remove();
                }
            }
//            for(FavInfo favInfo: favInfoList){
//                if(favInfo.getChannelId() == programInfo.getChannelId()){
//                    favInfoList.remove(favInfo);
//                }
//            }
        }
    }
    public void remove_mark_delete_service(List<ProgramInfo> ProgramInfoList){
        LogUtils.d("IN");
        int num = ProgramInfoList.size(), i;
        LogUtils.d("num = "+num);
        for(i=0; i<num ; i++) {
            ProgramInfo programInfo = ProgramInfoList.get(i);
            if(programInfo.getDeleteFlag() == 1){
                LogUtils.d("Remove Program Service ID = "+programInfo.getServiceId()+" Name = "+programInfo.getChName_chi());
                remove_programe_in_group(programInfo);
                ProgramInfoList.remove(programInfo);
                i--;
                num--;
            }
        }
        LogUtils.d("OUT");
    }


    public void SortProgrameInfoByLCN(List<ProgramInfo> ProgramInfoList) {
        try {
            mDataManager.ProtectProgramList();
            Collections.sort(ProgramInfoList, new Comparator<ProgramInfo>() {
                @Override
                public int compare(ProgramInfo programInfo, ProgramInfo t1) {
                    return programInfo.getLCN() - t1.getLCN();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            mDataManager.unProtectProgrameList();
        }
    }
    public ProgramInfo FindServiceByTripleID(List<ProgramInfo> ProgramInfoLis, ProgramInfo programInfo){
        for(int i=0;i<ProgramInfoLis.size();i++){
            if(ProgramInfoLis.get(i).getServiceId() == programInfo.getServiceId() &&
                    ProgramInfoLis.get(i).getTransportStreamId() == programInfo.getTransportStreamId() &&
                    ProgramInfoLis.get(i).getOriginalNetworkId() == programInfo.getOriginalNetworkId()){
                return ProgramInfoLis.get(i);
            }
        }
        return null;
    }
    public int CheckUpdateProgram(ProgramInfo p1, ProgramInfo p2){
        int update_db = 0;
        if((p1.getChannelId() != p2.getChannelId()) && (p2.getChannelId() !=0)){
            LogUtils.d("channel ID update ["+p1.getChannelId()+"] to ["+p2.getChannelId()+"]");
            p1.setChannelId(p2.getChannelId());
            update_db = 1;
            LogUtils.d("SV_ 000000000000000000");
        }
        if(p1.getLCN()!=p2.getLCN()){
            LogUtils.d("LCN update ["+p1.getLCN()+"] to ["+p2.getLCN()+"]");
            p1.setCA(p2.getLCN());
            p1.setDisplayNum(p2.getDisplayNum());
            update_db = 1;
            LogUtils.d("SV_ 1111111111111111");
        }
        if(p1.getSkip() != p2.getSkip()){
            LogUtils.d("Skip update ["+p1.getSkip()+"] to ["+p2.getSkip()+"]");
            p1.setSkip(p2.getSkip());
            update_db = 1;
            LogUtils.d("SV_ 2222222222222222");
        }
        if(p1.getDeleteFlag() != p2.getDeleteFlag()){
            LogUtils.d("Delete update ["+p1.getDeleteFlag()+"] to ["+p2.getDeleteFlag()+"]");
            p1.setDeleteFlag(p2.getDeleteFlag());
            update_db = 1;
            LogUtils.d("SV_ 333333333333333333");
        }
        if(p1.getSatId() != p2.getSatId()){
            LogUtils.d("Sat update ["+p1.getSatId()+"] to ["+p2.getSatId()+"]");
            p1.setSatId(p2.getSatId());
            update_db = 1;
            LogUtils.d("SV_ 4444444444444444");
        }
        if(p1.getCA() != p2.getCA()){
            LogUtils.d("CA flag update ["+p1.getCA()+"] to ["+p2.getCA()+"]");
            p1.setCA(p2.getCA());
            update_db = 1;
            LogUtils.d("SV_ 5555555555555555");
        }
        if(p1.getType() != p2.getType()){
            LogUtils.d("Type update ["+p1.getType()+"] to ["+p2.getType()+"]");
            p1.setType(p2.getType());
            update_db = 1;
            LogUtils.d("SV_ 6666666666666666666");
        }
        if ((p1.getDisplayName()==null) || (!p1.getDisplayName().equals(p2.getDisplayName()))) {
            LogUtils.d("Name update ["+p1.getDisplayName()+"] to ["+p2.getDisplayName()+"]");
            if((p2.getDisplayName()!=null)) {
                p1.setDisplayName(p2.getDisplayName());
                p1.setChName_eng(p2.getChName_eng());
                p1.setChName_chi(p2.getChName_chi());
                update_db = 1;
                LogUtils.d("SV_ 7777777777777777");
            }
        }
        if(p1.getCategory_type() != p2.getCategory_type()){
            LogUtils.d("Category update ["+p1.getCategory_type()+"] to ["+p2.getCategory_type()+"]");
            p1.setCategory_type(p2.getCategory_type());
            update_db = 1;
            LogUtils.d("SV_ 88888888888888888");
        }
        if(p1.getTpId() != p2.getTpId()){
            LogUtils.d("TPID  update ["+p1.getTpId()+"] to ["+p2.getTpId()+"]");
            p1.setTpId(p2.getTpId());
            update_db = 1;
            LogUtils.d("SV_ 999999999999999999");
        }
        if(p1.getService_product() != p2.getService_product()){
            p1.getService_product().clear();
            for(int i=0; i < p2.getService_product().size();i++){
                p1.setService_product(p2.getService_product().get(i));
            }
            //update_db = 1;
            //LogUtils.d("SV_ 999999999999999999999");
        }
        if(update_db == 1){
            LogUtils.d("name ["+p1.getDisplayName()+"] ["+p2.getDisplayName()+"]");
            mDataManager.DataManagerSaveProgramData(p1);
        }
        return update_db;
    }
    public boolean AddServiceInfoListWithCompareFavoriteGetServiceInfo(List<ProgramInfo> oldProgramInfoList, List<ProgramInfo> newProgramInfoList){
        try {
            mDataManager.ProtectProgramList();
            int sizeOld = oldProgramInfoList.size();
            int sizeNew = newProgramInfoList.size();
            int i, j, add_flag;
            add_flag = 0;
            ProgramInfo programInfoNew, programInfoOld;
            for (i = 0; i < sizeNew; i++) {
                programInfoNew = newProgramInfoList.get(i);
                for (j = 0; j < sizeOld; j++) {
                    programInfoOld = oldProgramInfoList.get(j);
                    if ((programInfoOld.getServiceId() == programInfoNew.getServiceId()) &&
                            (programInfoOld.getOriginalNetworkId() == programInfoNew.getOriginalNetworkId()) &&
                            (programInfoOld.getTransportStreamId() == programInfoNew.getTransportStreamId()) &&
                            (programInfoOld.getTpId() == programInfoNew.getTpId())) {
                        add_flag += CheckUpdateProgram(programInfoOld, programInfoNew);
                        break;
                    }
                }
                if (j >= sizeOld) {
                    add_flag = add_flag + 1;
                    LogUtils.d("Add new programInfo, add_flag = " + add_flag + " CH " + programInfoNew.getDisplayNum() + " name = " + programInfoNew.getDisplayName());
                    oldProgramInfoList.add(programInfoNew);
                    //mDataManager.addProgramInfo(programInfoNew);
                    mDataManager.DataManagerSaveProgramData(programInfoNew);
                    PrimeDtv dtv = HomeApplication.get_prime_dtv();
                    if(dtv != null){
                        dtv.add_epg_data_id(programInfoNew.getChannelId(), programInfoNew.getServiceId(), programInfoNew.getTransportStreamId(), programInfoNew.getOriginalNetworkId());
                    }
                }
            }
            LogUtils.e("add_flag = " + add_flag);
        if (add_flag ==0)
            return false;
        else
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            mDataManager.unProtectProgrameList();
        }
    }

    public void DelTimerByProgram(ProgramInfo ProgramInfo){
        LogUtils.d(" DelTimerFromProgram IN");
    }

    public void DelGroupMemberByProgram(ProgramInfo ProgramInfo){
        LogUtils.d(" DelGroupMemberByProgram IN");
    }
    public void DelMarkedProgram(List<ProgramInfo> oldProgramInfoList){
        int i,size=oldProgramInfoList.size();
        for(i=0;i<size;){
            ProgramInfo p = oldProgramInfoList.get(i);
            if((p.getSkip() & 0x80) == 0x80){
                long del_channle_id = p.getChannelId();
                DelTimerByProgram(p);
                DelGroupMemberByProgram(p);
                LogUtils.d("DEL channel = CH "+p.getDisplayNum()+" "+p.getDisplayName());
                PrimeDtv dtv = HomeApplication.get_prime_dtv();
                if(dtv != null){
                    dtv.delete_epg_data_id(p.getChannelId(), p.getServiceId(), p.getTransportStreamId(), p.getOriginalNetworkId());
                }
                mDataManager.delProgramInfo(del_channle_id);
                mDataManager.DataManagerDeleteProgramData(del_channle_id);
                //oldProgramInfoList.remove(i);
                size--;
            }
            else {
                i++;
            }
        }
        SortProgrameInfoByLCN(oldProgramInfoList);
    }
    public boolean CheckMarkServiceDelete(List<ProgramInfo> oldProgramInfoList, List<ProgramInfo> newProgramInfoList){
        int sizeOld,sizeNew,i,j;
        boolean rel = false;
        sizeOld=oldProgramInfoList.size();
        sizeNew=newProgramInfoList.size();
        for(i=0;i<sizeOld;i++){
            for(j=0;j<sizeNew;j++){
                if((oldProgramInfoList.get(i).getServiceId() == newProgramInfoList.get(j).getServiceId()) &&
                        (oldProgramInfoList.get(i).getOriginalNetworkId() == newProgramInfoList.get(j).getOriginalNetworkId()) &&
                        (oldProgramInfoList.get(i).getTransportStreamId() == newProgramInfoList.get(j).getTransportStreamId()) &&
                        (oldProgramInfoList.get(i).getTpId() == newProgramInfoList.get(j).getTpId())){
                    break;
                }
            }
            if(j == sizeNew){
                int delFlag=oldProgramInfoList.get(i).getSkip();
                delFlag=delFlag | 0x80;
                oldProgramInfoList.get(i).setSkip(delFlag);
                rel = true;
            }
        }
        return rel;
    }

    public void category_add_to_fav(List<ProgramInfo> ProgramInfoList){
        mDataManager.category_add_to_fav(ProgramInfoList);
    }

    public void category_update_to_fav(List<ProgramInfo> ProgramInfoList, List<MusicInfo> musicInfoList){ //only use in SI update
        List<FavGroup> favGroupList = mDataManager.category_update_to_fav(ProgramInfoList, musicInfoList);
        List<FavGroup> tmpGroupList;
        if (musicInfoList.isEmpty())
            tmpGroupList = mDataManager.getFavGroupList();
        else {
            tmpGroupList = mDataManager.getMusicFavGroupList();
        }

        for(FavGroup favGroup : tmpGroupList) {
            List<FavInfo> favInfoList = new ArrayList<>(favGroup.getFavInfoList());
            for(FavInfo favInfo : favInfoList) {
                if(favInfo.getStatus() != MiscDefine.SaveStatus.STATUS_NOT_DELETE) {
                    mDataManager.delFavInfo(favInfo.getFavMode(),favInfo);
                    mDataManager.DataManagerDeleteFavInfo(favInfo);
                }
            }
        }
        for(FavGroup favGroup : favGroupList) {
            for(FavInfo favInfo : favGroup.getFavInfoList()) {
                if((favInfo.getStatus() == MiscDefine.SaveStatus.STATUS_ADD || favInfo.getStatus() == MiscDefine.SaveStatus.STATUS_UPDATE)) {
                    if(favInfo.getStatus() == MiscDefine.SaveStatus.STATUS_ADD)
                        mDataManager.addFavInfo(favInfo.getFavMode(),favInfo);
                    else
                        mDataManager.updateFavInfo(favInfo.getFavMode(),favInfo);
                    mDataManager.DataManagerSaveFavInfo(favInfo);
                    favInfo.setStatus(MiscDefine.SaveStatus.STATUS_NONE);
                }
            }
        }
    }
}

