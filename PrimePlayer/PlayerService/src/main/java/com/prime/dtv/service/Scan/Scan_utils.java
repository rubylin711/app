package com.prime.dtv.service.Scan;

import static com.prime.dtv.service.Table.Desciptor.Descriptor.LINKAGE_DESC;
import static com.prime.datastructure.sysdata.ProgramInfo.NUMBER_OF_AUDIO_IN_SIL;
import static com.prime.datastructure.sysdata.ProgramInfo.PROGRAM_TV;

import android.content.Context;
import android.util.Log;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.DTVMessage;
import com.prime.datastructure.sysdata.FavGroupName;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.ServiceApplication;
import com.prime.dtv.ServiceInterface;
import com.prime.dtv.service.Table.BatData;
import com.prime.dtv.service.Table.Desciptor.CableDeliverySystemDescriptor;
import com.prime.dtv.service.Table.Desciptor.ChannelCategoryDescriptor;
import com.prime.dtv.service.Table.Desciptor.ChannelDescriptor;
import com.prime.dtv.service.Table.Desciptor.ChannelProductListDescriptor;
import com.prime.dtv.service.Table.Desciptor.DescBase;
import com.prime.dtv.service.Table.Desciptor.Descriptor;
import com.prime.dtv.service.Table.Desciptor.DigiturkLogicalChannelNumberDescriptor;
import com.prime.dtv.service.Table.Desciptor.LinkageDescriptor;
import com.prime.dtv.service.Table.Desciptor.LogicalChannelNumberDescriptor;
import com.prime.dtv.service.Table.Desciptor.CNSLogicalChannelNumberDescriptor;
import com.prime.dtv.service.Table.Desciptor.CNSExtLogicalChannelNumberDescriptor;
import com.prime.dtv.service.Table.Desciptor.MultilingualServiceNameDescriptor;
import com.prime.dtv.service.Table.Desciptor.SatelliteDeliverySystemDescriptor;
import com.prime.dtv.service.Table.Desciptor.ServiceDescriptor;
import com.prime.dtv.service.Table.Desciptor.ServiceListDescriptor;
import com.prime.dtv.service.Table.NitData;
import com.prime.dtv.service.Table.PmtData;
import com.prime.dtv.service.Table.SdtData;
import com.prime.dtv.service.datamanager.CNSSITableData;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.FavInfo;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.MusicInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.TpInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Scan_utils {
    private static final String TAG = "Scan_utils";

    private static DataManager mDataManager = null;
    private static final int MAX_CHANNEL = 4000;
    private static final boolean DEBUG_NIT_UPDATE = true;
    private static final boolean DEBUG_AUDIO_LANGUAGE = true;
    private int total_channel;
    private Context mContext;

    private static boolean IsBatInWhiteList;

    private class Bouquet {
        int id;
        int version;
        String name;

        Bouquet(int id, int version, String name) {
            this.id = id;
            this.version = version;
            this.name = name;
        }
    }

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
            lang=GposInfo.getAudioLanguageSelection(mContext, i);
            if(DEBUG_AUDIO_LANGUAGE)
                LogUtils.d("DEBUG_AUDIO_LANGUAGE default select lang1:" + lang);
            for(j=0; j< PmtData.PMT_MAX_AUDIO_PID_NUM; j++) {
                if(pmt.getProgramMap().getAudio_pid(j)!=0) {
                    if ((lang != null) && (pmt.getProgramMap().getIso639LanguageCode1(j) != null)) {
                        String pmt_lang = pmt.getProgramMap().getIso639LanguageCode1(j);
                        if(pmt_lang != null && pmt_lang.equalsIgnoreCase("zho"))
                            pmt_lang = "chi";
                        if (lang.compareTo(pmt_lang) == 0) {
                            if ((audio_alloc[j] == 0xff) && (alloc_count < NUMBER_OF_AUDIO_IN_SIL)) {
                                audio_alloc[j] = 0;
                                ProgramInfo.AudioInfo andioInfo = new ProgramInfo.AudioInfo(pmt.getProgramMap().getAudio_pid(j), pmt.getProgramMap().getAudio_stream_type(j), pmt.getProgramMap().getIso639LanguageCode1(j), pmt.getProgramMap().getIso639LanguageCode2(j));
                                programInfo.pAudios.add(alloc_count, andioInfo);
                                if(DEBUG_AUDIO_LANGUAGE) {
                                    LogUtils.d("DEBUG_AUDIO_LANGUAGE 111 Audio_pid:" + pmt.getProgramMap().getAudio_pid(j));
                                    LogUtils.d("DEBUG_AUDIO_LANGUAGE 111 alloc_count:" + alloc_count);
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
                        LogUtils.d("DEBUG_AUDIO_LANGUAGE 222 Audio_pid:" + pmt.getProgramMap().getAudio_pid(i));
                        LogUtils.d("DEBUG_AUDIO_LANGUAGE 222 alloc_count:" + alloc_count);
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

    public void add_satellite_with_compare(SatelliteDeliverySystemDescriptor pcds, int SatelliteId, int onid, int	tsid){
        List<TpInfo> tpInfoList = mDataManager.getTpInfoList();
        SatInfo satInfo = mDataManager.getSatInfo(SatelliteId);
        TpInfo temp=null;
        int j,k, frequency, ret_val = -1, fgap, sgap;
        int symbol = pcds.SymbolRate;
        int freq = pcds.Frequency;
        int polar =pcds.Polarization & 1;
        int modulationSystem,modulationType;

        if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DIGITURK)) {
            if(tsid == 20101 && freq == 11385) {
                freq = 11675;
                symbol = 24444;
                polar = TpInfo.Sat.POLAR_V;
            }
        }

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
                sgap = 1000;//ruby modify 20220705 for SunTV SI
            }
        }
        else
            return;

        for(j = 0; j < satInfo.getTpNum(); j++)
        {
            temp = mDataManager.getTpInfo(satInfo.getTps().get(j));//tpInfoList.get(satInfo.getTps().get(j));
            //frequency = qtp_freq(temp);
            frequency = temp.SatTp.getFreq();//qtp_freq(temp);
            //if(qtp_polar(temp) == polar)
            if(temp.SatTp.getPolar() == polar)
            {
                if(frequency >= (freq - fgap))
                {
                    if(frequency <= (freq + fgap))
                    {
                        //if(symbol >= (qtp_symbol(temp) - sgap))
                        if(symbol >= (temp.SatTp.getSymbol() - sgap))
                        {
                            //if(symbol <= (qtp_symbol(temp) + sgap))
                            if(symbol <= (temp.SatTp.getSymbol() + sgap))
                            {
                                ret_val = satInfo.getTps().get(j);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if(j < satInfo.getTpNum())
        {
            temp = mDataManager.getTpInfo(satInfo.getTps().get(j));//tpInfoList.get(satInfo.getTps().get(j));
            temp.SatTp.setPolar(polar); //ruby add 20220304 for SunTV
            temp.SatTp.setNotDelete(1);
            temp.setTransport_id(tsid);
            temp.setOrignal_network_id(onid);
            temp.setNetwork_id(1);
            return;
        }
        if((tpInfoList.size() >= TpInfo.MAX_NUM_OF_TP) || (satInfo.getTpNum() >= SatInfo.MAX_TP_NUM_IN_ONE_SAT)){
            return;
        }

        temp = new TpInfo(TpInfo.DVBS);
        temp.SatTp.setFreq(freq);
        modulationSystem=(pcds.Modulation & 0x4) >> 2;
        modulationType= pcds.Modulation & 0x3;
        if(modulationType == 1){
            temp.SatTp.setMod(TpInfo.Sat.MOD_QPSK);
        }
        else if(modulationType == 2){
            temp.SatTp.setMod(TpInfo.Sat.MOD_8PSK);
        }
        else if(modulationType == 3){
            temp.SatTp.setMod(TpInfo.Sat.MOD_16QAM);
        }
        else{
            temp.SatTp.setMod(TpInfo.Sat.MOD_AUTO);
        }
        temp.SatTp.setModSys(modulationSystem);
        temp.SatTp.setSymbol(symbol);
        temp.SatTp.setPolar(polar);
        temp.setTransport_id(tsid);
        temp.setOrignal_network_id(onid);
        temp.setNetwork_id(1);
        temp.SatTp.setNotDelete(1);
        temp.setSatId(SatelliteId);
        mDataManager.addTpInfo(temp);
        LogUtils.d("scan add new tp : "+temp.ToString() + " in sat : "+satInfo.ToString());
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
        int index=-1;
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
        if(index==-1 && programInfo.getDisplayName()!=null)
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
    public void Scan_process_Nit(NitData nitData, PesiDtvFrameworkInterfaceCallback callback){
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
            LogUtils.d("SI_TBC NitVersionNew = "+Nit_version+" NitVersionOld = "+mDataManager.getGposInfo().getNitVersion(mContext));
            GposInfo.setNitVersion(mContext, Nit_version);
            Nit_id = nitData.getNetworkStreamList().get(0).getNetworkID();
            LogUtils.d("SI_TBC NitIdNew = "+Nit_id+" NitIdOld = "+GposInfo.getSINitNetworkId(mContext));
            GposInfo.setSINitNetworkId(mContext, Nit_id);
            callback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_SET_NIT_ID_TO_ACS, Nit_id, 0, null);
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
                            if (temp3 == GposInfo.getBatId(mContext)) {
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

    public void Scan_process_Nit_Sat(NitData nitData, int SatelliteId, PesiDtvFrameworkInterfaceCallback callback){
        int i, j,Nit_version,Nit_id;

        List<TpInfo> tpInfoList = mDataManager.getTpInfoList();
        SatInfo satInfo = mDataManager.getSatInfo(SatelliteId);
        TpInfo temp;
        LogUtils.d("IN");
        //LogUtils.d("#@#@#@ SatelliteId="+SatelliteId+",tpInfoList.size()="+tpInfoList.size());
        List<Integer> SatInfoTps;
        SatInfoTps=satInfo.getTps();
        //for(i=0;i<SatInfoTps.size();i++)
        //LogUtils.d("#@#@#@ SatInfoTps.get("+i+")="+SatInfoTps.get(i));
        if(nitData == null)
            LogUtils.d("nitData = null");
        for(i=0, j=0; nitData != null && i<nitData.getTransportStreamList().size();i++) {
            if(i == 0){
                //LogUtils.d("#@#@#@ satInfo.getTpNum()="+satInfo.getTpNum()+"temp.SatTp.setNotDelete(0)");
                for(j = 0; j < satInfo.getTpNum(); j++)
                {
                    temp = mDataManager.getTpInfo(satInfo.getTps().get(j));//tpInfoList.get(satInfo.getTps().get(j));
                    temp.SatTp.setNotDelete(0);
                }
            }
            NitData.TransportStream transportStream = nitData.getTransportStreamList().get(i);
            for(j=0 ; j<transportStream.getDescriptorList().size() ; j++){
                DescBase descriptor = transportStream.getDescriptorList().get(j);
                LogUtils.d("TAG = "+descriptor.Tag);
                if(descriptor.Tag == Descriptor.SATELLITE_DELIVERY_SYSTEM_DESC){
                    SatelliteDeliverySystemDescriptor pcds = (SatelliteDeliverySystemDescriptor)descriptor;
                    add_satellite_with_compare(pcds, SatelliteId, transportStream.getOriginalNetworkID(),transportStream.getTransportStreamID());
                    //UpdateCableTPinfo(pcds, transportStream.getTransportStreamID(), transportStream.getOriginalNetworkID());
                }
            }
        }
        remove_mark_delete_tp_and_reset_sat(SatelliteId);
        if(nitData.getNetworkStreamList().size() > 0) {
            Nit_version = nitData.getNetworkStreamList().get(0).getVersion();
            LogUtils.d("SI_TBC NitVersionNew = "+Nit_version+" NitVersionOld = "+GposInfo.getNitVersion(mContext));
            GposInfo.setNitVersion(mContext, Nit_version);
            Nit_id = nitData.getNetworkStreamList().get(0).getNetworkID();
            LogUtils.d("SI_TBC NitIdNew = "+Nit_id+" NitIdOld = "+GposInfo.getNitId(mContext));
            GposInfo.setNitId(mContext, Nit_id);
            callback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_SET_NIT_ID_TO_ACS, Nit_id, 0, null);
        }
        //for(i=0;i<SatInfoTps.size();i++)
        //LogUtils.d("#@#@#@ SatInfoTps.get("+i+")="+SatInfoTps.get(i));
        //LogUtils.d("#@#@#@ tpInfoList.size()="+tpInfoList.size());
        LogUtils.d("OUT");
    }
    
    public int GetTpIdByTSid(int ts_id){
        int tp_id = -1;
        List<TpInfo> tpInfoList = mDataManager.getTpInfoList();
        for(int i=0 ; i<tpInfoList.size(); i++){
            TpInfo ptp = tpInfoList.get(i);
            if(ptp.getTransport_id() == ts_id){
                tp_id = ptp.getTpId();
            }
        }
        return tp_id;
    }

    public int GetTpIndexByTSid(int ts_id){
        int tp_index = -1;
        List<TpInfo> tpInfoList = mDataManager.getTpInfoList();
        for(int i=0 ; i<tpInfoList.size(); i++){
            TpInfo ptp = tpInfoList.get(i);
            if(ptp.getTransport_id() == ts_id){
                tp_index = i;
            }
        }
        return tp_index;
    }

    public void Scan_process_Bat(BatData batData, List<ProgramInfo> ProgramInfoList){
        int transportStreamId , originalNetworkId;
        LogUtils.d("IN");
        if(batData != null){
            GposInfo.setBatVersion(mContext, batData.getVersion());
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

    public void scan_process_Bat_CNS_setLcn(BatData batData, List<ProgramInfo> ProgramInfoList) {
        applyExtLcnLinear(ProgramInfoList, batData.getExtLCNStreamList());
    }

    public void Scan_process_Bat_CNS(BatData batData, List<ProgramInfo> ProgramInfoList){
        int transportStreamId , originalNetworkId, LogicalChannelNumber, Server_id;
        int ProgramType;
        //ProgramInfo programInfoTmp=null;
        LogUtils.d("IN");
        if (batData == null) {
            LogUtils.d("No Bat Data");
            LogUtils.d("OUT");
            return;
        }
        int totalServiceCount = 0;
        GposInfo.setBatVersion(mContext, batData.getVersion());
        GposInfo.setBatId(mContext, batData.getBouquetId());
        List<BatData.TransportStream> transportStreamList=batData.getTransportStreamList();
        if (transportStreamList != null) {
            for (BatData.TransportStream temp : transportStreamList) {
                if (temp == null)
                    continue;
                transportStreamId = temp.getTransportStreamId();
                originalNetworkId = temp.getOriginalNetworkId();

                List<ServiceListDescriptor.Service> ServiceList = temp.getServiceList();
                if (ServiceList == null || ServiceList.isEmpty())
                    continue;
                totalServiceCount += ServiceList.size();

                LogUtils.d("CNS transportStreamId = " + transportStreamId + " getServiceListSize = " + temp.getServiceList().size() + " total size = " + totalServiceCount);
                for (ServiceListDescriptor.Service serviceTemp : ServiceList) {
                    if (serviceTemp == null) continue;
//                    LogUtils.d(TAG+" cns bat ServiceId = "+serviceTemp.ServiceID );
//                    LogUtils.d(TAG+" cns bat ServiceType = "+serviceTemp.ServiceType);
                    if ((serviceTemp.ServiceType == Scan_cfg.DIGITAL_TELEVISION_SERVICE) ||
                            (serviceTemp.ServiceType == Scan_cfg.MPEG_2_HD_DIGITAL_TELEVISION_SERVICE) ||
                            (serviceTemp.ServiceType == Scan_cfg.ADVANCED_CODEC_SD_DIGITAL_TELEVISION_SERVICE) ||
                            (serviceTemp.ServiceType == Scan_cfg.ADVANCED_CODEC_HD_DIGITAL_TELEVISION_SERVICE) ||
                            (serviceTemp.ServiceType == Scan_cfg.DIGITAL_RADIO_SOUND_SERVICE)) {

                        if (serviceTemp.ServiceType == Scan_cfg.DIGITAL_RADIO_SOUND_SERVICE)
                            ProgramType = ProgramInfo.PROGRAM_RADIO;
                        else
                            ProgramType = PROGRAM_TV;

                        //ProgramInfo programInfo = mDataManager.getProgramInfo(ProgramInfoList, serviceTemp.ServiceID, transportStreamId, originalNetworkId);
                        ProgramInfo programInfo = null;

                        programInfo = new ProgramInfo();
                        programInfo.setType(ProgramType);
                        programInfo.setServiceId(serviceTemp.ServiceID);
                        programInfo.setTransportStreamId(transportStreamId);
                        programInfo.setOriginalNetworkId(originalNetworkId);

                        int lcn = findLcnFromDescriptors(temp.getDescriptorList(), serviceTemp.ServiceID);
                        if (lcn == -1) {
                            lcn = serviceTemp.ServiceID;
                            LogUtils.d("[CNS] TS=" + transportStreamId + " serviceId=" + serviceTemp.ServiceID + " no LCN -> fallback=" + lcn);
                        } else {
                            LogUtils.d("[CNS] TS=" + transportStreamId + " serviceId=" + serviceTemp.ServiceID + " LCN=" + lcn);
                        }
                        programInfo.setLCN(lcn);
                        programInfo.setDisplayNum(lcn);
                        ProgramInfoList.add(programInfo);
                    }
                }
            }
            //process Ext LCN
            applyExtLcnLinear(ProgramInfoList, batData.getExtLCNStreamList());
        }
        LogUtils.d("OUT");
    }
    private int findLcnFromDescriptors(List<DescBase> descriptorList, int targetServiceId) {
        if (descriptorList == null || descriptorList.isEmpty())
            return -1;

        for (int i = 0; i < descriptorList.size(); i++) {
            DescBase d = descriptorList.get(i);
            if (d == null) continue;

            if (d.Tag == Descriptor.CNS_LOGICAL_CHANNEL_DESC) {
                CNSLogicalChannelNumberDescriptor cns = (CNSLogicalChannelNumberDescriptor) d;
                List<CNSLogicalChannelNumberDescriptor.LogicalChannelNumber> lst = cns.mLogicalChannelNumberList;
                if (lst == null || lst.isEmpty()) continue;

                for (int k = 0; k < lst.size(); k++) {
                    CNSLogicalChannelNumberDescriptor.LogicalChannelNumber item = lst.get(k);
                    if (item == null) continue;
                    if (item.ServiceId == targetServiceId) {
                        return item.LogicalChannelNumber;
                    }
                }
            }
        }
        return -1;
    }
    private void applyExtLcnLinear(List<ProgramInfo> programInfoList, List<BatData.ExtLCNStream> extStreams) {
        if (programInfoList == null || programInfoList.isEmpty()
                || extStreams == null || extStreams.isEmpty()) {
            return;
        }

        for (int i = 0; i < extStreams.size(); i++) {
            BatData.ExtLCNStream stream = extStreams.get(i);
            if (stream == null) continue;

            List<CNSExtLogicalChannelNumberDescriptor.ExtLogicalChannelNumber> extLCNList = stream.getExtLCNList();
            if (extLCNList == null || extLCNList.isEmpty()) continue;

            for (int j = 0; j < extLCNList.size(); j++) {
                CNSExtLogicalChannelNumberDescriptor.ExtLogicalChannelNumber e = extLCNList.get(j);
                if (e == null) continue;

                int serviceId = e.ServiceId;
                int lcn = e.LogicalChannelNumber;

                ProgramInfo target = findProgramInfoByServiceId(programInfoList, serviceId);
                if (target != null) {
                    target.setLCN(lcn);
                    target.setDisplayNum(lcn);
                    LogUtils.d("[ExtLCN] update name= "+target.getDisplayName()+" serviceId= " + serviceId + " LCN= " + lcn);
                } else {
                    LogUtils.d("[ExtLCN] cannot find serviceId= " + serviceId);
                }
            }
        }
    }
    private ProgramInfo findProgramInfoByServiceId(List<ProgramInfo> list, int serviceId) {
        if (list == null || list.isEmpty()) return null;
        for (int i = 0; i < list.size(); i++) {
            ProgramInfo p = list.get(i);
            if (p != null && p.getServiceId() == serviceId) {
                return p;
            }
        }
        return null;
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
                                programInfo.setTpId(GetTpIdByTSid(tsid));
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
                                programInfo.setTpId(GetTpIdByTSid(tsid));
                                ProgramInfoList.add(programInfo);
                            }
                        }
                    }
                }
            }
        }
        LogUtils.d("OUT");
    }
    public void Scan_process_Nit_CNS(NitData nitData, boolean IsWhiteList, List<ProgramInfo> ProgramInfoList) {
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
                                programInfo.setTpId(GetTpIdByTSid(tsid));
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
                    tp_id = GetTpIdByTSid(serviceData.getTransportStreamId());
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

    public void find_ticker_ad_serviceId_CNS(ProgramInfo programInfo,List<SdtData> sdtDataList) {
        if (programInfo == null) {
            Log.w(TAG, "findCNS: programInfo=null");
            return;
        }

        final int targetTpId = programInfo.getTpId();
        final int oldTickerSid = mDataManager.getCNSSITableData().getTickerServiceId();
        final int oldAdSid     = mDataManager.getCNSSITableData().getADServiceId();

        if (sdtDataList == null || sdtDataList.isEmpty()) {
            Log.d(TAG,"findCNS: SDT list empty. tpId="+ targetTpId);
            return;
        }

        Log.d(TAG,"findCNS: start tpId="+targetTpId
                +" sdt size="+sdtDataList.size()
                +" oldTickerSid="+oldTickerSid
                +" oldAdSid="+oldAdSid);
        for(int i=0;i<sdtDataList.size();i++) {
            List<SdtData.ServiceData> serviceDataList = sdtDataList.get(i).geServiceData();
            if (serviceDataList == null || serviceDataList.isEmpty()) continue;
            //LogUtils.d("scanCNS serviceDataList.size() = " + serviceDataList.size());
            for (int j = 0; j < serviceDataList.size(); j++) {
                SdtData.ServiceData serviceData = serviceDataList.get(j);
                if (serviceData == null) continue;

                int tp_id = GetTpIdByTSid(serviceData.getTransportStreamId());
                if (tp_id != targetTpId) continue;

                if (serviceData.getDescriptor() == null ||
                        serviceData.getDescriptor().getDescList() == null) {
                    continue;
                }

                Log.d(TAG,"findCNS find same tp["+ tp_id+"]");
                List<DescBase> descList = serviceData.getDescriptor().getDescList();
                for (int desc_index = 0; desc_index < descList.size(); desc_index++) {
                    DescBase desbase = descList.get(desc_index);
                    if (desbase == null || desbase.Tag != Descriptor.SERVICE_DESC) continue;

                    ServiceDescriptor sd = (ServiceDescriptor) desbase;
                    if (sd.ServiceType != Scan_cfg.DATA_BROADCAST_SERVICE) continue;

                    String name = sd.ServiceName;
                    if (name == null) continue;
                    int sid = serviceData.getServiceId();
                    // LogUtils.d(TAG+" serviceData.getServiceId() = " + serviceData.getServiceId() + " serviceDescriptor.ServiceName = " + serviceDescriptor.ServiceName);

                    if (CNSSITableData.CNS_TICKER_SERVICE_NAME.equalsIgnoreCase(name)
                            && mDataManager.getCNSSITableData().getTickerServiceId() != sid) {
                        Log.i(TAG, "findCNS: TICKER sid=" + sid + " (old=" + oldTickerSid + ")");
                        mDataManager.getCNSSITableData().setTickerServiceId(sid);
                    }

                    if (CNSSITableData.CNS_AD_SERVICE_NAME.equalsIgnoreCase(name)
                            && mDataManager.getCNSSITableData().getADServiceId() != sid) { // ★ 修正
                        Log.i(TAG, "findCNS: AD sid=" + sid + " (old=" + oldAdSid + ")");
                        mDataManager.getCNSSITableData().setADServiceId(sid);
                    }
                }
            }
        }

    }

    public void Scan_process_Sdt_CNS(List<SdtData> sdtDataList, List<ProgramInfo> ProgramInfoList){
        LogUtils.d("IN");
        int i,j,desc_index, tp_id, add_new=0;
        int programCnt,programIdx;

        List<TpInfo> tpInfoList=mDataManager.getTpInfoList();
        programCnt=ProgramInfoList.size();
        LogUtils.d("scanCNS programCnt = "+programCnt);

        total_channel = 0;
        if(programCnt > 0) {
            if (sdtDataList != null && sdtDataList.size() > 0) {
                for(i=0;i<sdtDataList.size();i++) {
                    List<SdtData.ServiceData> serviceDataList = sdtDataList.get(i).geServiceData();
                    //LogUtils.d("scanCNS serviceDataList.size() = " + serviceDataList.size());
                    for (j = 0; j < serviceDataList.size(); j++) {
                        SdtData.ServiceData serviceData = serviceDataList.get(j);
                        add_new = 0;
                        tp_id = GetTpIdByTSid(serviceData.getTransportStreamId());
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
                        //LogUtils.d("scanCNS serviceData.getDescriptor().getDescList().size() = " + serviceData.getDescriptor().getDescList().size());
                        for (desc_index = 0; desc_index < serviceData.getDescriptor().getDescList().size(); desc_index++) {
                            DescBase desbase = serviceData.getDescriptor().getDescList().get(desc_index);
                            switch (desbase.Tag) {
                                case Descriptor.SERVICE_DESC: {
                                    ServiceDescriptor serviceDescriptor = (ServiceDescriptor) desbase;
                                    //programInfo.setDisplayName(serviceDescriptor.ServiceName);
                                    programInfo.setChName_eng(serviceDescriptor.ServiceName);
                                    //LogUtils.d("scanCNS programInfo.getDisplayName() = "+programInfo.getDisplayName());
                                    //LogUtils.d("scanCNS programInfo.setChName_eng() = "+programInfo.getDisplayName());
                                    //LogUtils.d("scanCNS serviceDescriptor.ServiceName = "+serviceDescriptor.ServiceName);
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
                                        //LogUtils.d("scanCNS programInfo.getChName_chi() = "+programInfo.getChName_chi());
                                        //LogUtils.d("scanCNS multilingualServiceName.ServiceName = "+multilingualServiceName.ServiceName);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        total_channel++;
                        //LogUtils.d("scanCNS Service ID = " + programInfo.getServiceId() + " Name: " + programInfo.getChName_eng() + " " + programInfo.getChName_chi());
                        //LogUtils.d("scanCNS total_channel = " + total_channel);
                    }
                }
            }
        }
        LogUtils.d("scanCNS total_channel = "+total_channel);
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
                    tp_id = GetTpIdByTSid(serviceData.getTransportStreamId());
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
                                    LogUtils.d("ISO Language ["+GposInfo.getOSDLanguage(mContext)+"] ["+multilingualServiceName.ISO639LanguageCode+"]");
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

    public void Scan_process_Sdt_Digiturk(List<SdtData> sdtDataList, List<ProgramInfo> ProgramInfoList){
        LogUtils.d("IN");
        int i,j,desc_index, tp_id, add_new=0;
        total_channel = 0;
        //for(i=0 ; i< sdtDataList.size() ; i++)
        List<TpInfo> tpInfoList = mDataManager.getTpInfoList();
        LogUtils.d("SSSSSS Scan_process_Sdt_Digiturk in");
        if(sdtDataList != null && sdtDataList.size()>0)
        {
            //LogUtils.d("sdtDataList.size() "+ sdtDataList.size());
            for(i=0;i<sdtDataList.size();i++) {
                SdtData sdtData = sdtDataList.get(i);
                List<SdtData.ServiceData> serviceDataList = sdtData.geServiceData();
                for (j = 0; j < serviceDataList.size(); j++) {
                    SdtData.ServiceData serviceData = serviceDataList.get(j);
                    add_new = 0;
                    tp_id = GetTpIdByTSid(serviceData.getTransportStreamId());
                    if (tp_id == -1) {
                        LogUtils.d("TSID [" + tp_id + "] => Can't Find TP ID ");
                        continue;
                    }
                    LogUtils.d("SSSSSS serviceData.getTransportStreamId() = " + serviceData.getTransportStreamId());
                    LogUtils.d("SSSSSS serviceData.getVersionNumber() = " + serviceData.getVersionNumber());
                    LogUtils.d("SSSSSS tp_id = " + tp_id);
                    TpInfo tpInfo = mDataManager.getTpInfo(tp_id);
                    if(tpInfo != null) {
                        tpInfo.setSdt_version(serviceData.getVersionNumber());
//                    tpInfoList.get(tp_id).setSdt_version(serviceData.getVersionNumber());
                        LogUtils.d("SSSSSS tpInfoList.get(" + tp_id + ").getSdt_version() = " + mDataManager.getTpInfo(tp_id).getSdt_version());
                        LogUtils.d("SSSSSS tpInfo = " + tpInfo.ToString());
                    }
                    ProgramInfo programInfo = mDataManager.getProgramInfo(ProgramInfoList, serviceData.getServiceId(), serviceData.getTransportStreamId(), serviceData.getOriginalNetworkId());//new ProgramInfo();
                    int serviceType = serviceData.getServiceType();
                    LogUtils.d("SSSSSS Service ID = "+serviceData.getServiceId()+" ServiceName = "+serviceData.getServiceName()
                            +" serviceType = "+serviceType);
                    int programType;
                    if (serviceType == Scan_cfg.DIGITAL_TELEVISION_SERVICE ||
                            serviceType == Scan_cfg.MPEG_2_HD_DIGITAL_TELEVISION_SERVICE ||
                            serviceType == Scan_cfg.ADVANCED_CODEC_SD_DIGITAL_TELEVISION_SERVICE ||
                            serviceType == Scan_cfg.ADVANCED_CODEC_HD_DIGITAL_TELEVISION_SERVICE ||
                            serviceType == Scan_cfg.ISDBT_ONESEG_SERVICE ||
                            serviceType == Scan_cfg.HEVC ||
                            serviceType == Scan_cfg.HEVC_UHD)
                        programType = ProgramInfo.PROGRAM_TV;
                    else if(serviceType == Scan_cfg.DIGITAL_RADIO_SOUND_SERVICE ||
                            serviceType == Scan_cfg.FM_RADIO_SERVICE ||
                            serviceType == Scan_cfg.E_AC3_HE_AAC)
                        programType = ProgramInfo.PROGRAM_RADIO;
                    else if(serviceType > 0x20 && serviceType <= 0x7F)
                        continue; //reserved_for_future_use;
                    else if (serviceType >= 0x80 && serviceType <= 0xFE)
                        continue; //user_defined
                    else
                        continue;

                    if (programInfo == null) {
                        add_new = 1;
                        programInfo = new ProgramInfo();
                    }
                    programInfo.setType(programType);
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
                        }
                    }
                    if (add_new == 1) {
                        ProgramInfoList.add(programInfo);
                        total_channel++;
                        LogUtils.d("SSSSSS Service ID = " + programInfo.getServiceId() + " Name: " + programInfo.getChName_eng() + " " + programInfo.getChName_chi());
                        LogUtils.d("SSSSSS total_channel = " + total_channel);
                    }
                }
            }
        }
        LogUtils.d("OUT");
    }

    public void Scan_process_Bat_Digiturk(BatData batData, List<ProgramInfo> ProgramInfoList){
        int i, j,k,TransportStream_id,OriginalNetwork_id,Server_id,LogicalChannelNumber;
        ProgramInfo programInfoTmp=null;
        ProgramInfo programInfo= new ProgramInfo();
        LogUtils.d("IN");
        if(batData == null)
            LogUtils.d("batData = null");
        for(i=0, j=0; batData != null && i<batData.getTransportStreamList().size();i++) {
            BatData.TransportStream transportStream = batData.getTransportStreamList().get(i);
            for(j=0 ; j<transportStream.getDescriptorList().size() ; j++){
                DescBase descriptor = transportStream.getDescriptorList().get(j);
                LogUtils.d("TAG = "+descriptor.Tag);
                if(descriptor.Tag == Descriptor.LOGICAL_CHANNEL_DESC){
                    DigiturkLogicalChannelNumberDescriptor pcds = (DigiturkLogicalChannelNumberDescriptor)descriptor;
                    TransportStream_id=transportStream.getTransportStreamId();
                    OriginalNetwork_id=transportStream.getOriginalNetworkId();
                    for(k=0 ; k< pcds.mLogicalChannelNumberList.size() ; k++) {
                        int desc_TransportStream_id = pcds.mLogicalChannelNumberList.get(k).TransportStreamID;
                        Server_id=pcds.mLogicalChannelNumberList.get(k).ServiceId;
                        LogicalChannelNumber=pcds.mLogicalChannelNumberList.get(k).LogicalChannelNumber;
                        programInfo.setServiceId(Server_id);
                        programInfo.setTransportStreamId(desc_TransportStream_id);
                        programInfo.setOriginalNetworkId(OriginalNetwork_id);
                        programInfoTmp = FindServiceByServiceID_TransportID(ProgramInfoList, programInfo);
//                        Log.d("scan","not find service id = "+programInfoTmp.getServiceId()+" name = "+programInfoTmp.getDisplayName()+
//                                " Lcn = "+programInfoTmp.getLCN());
                        if(programInfoTmp != null){
                            programInfoTmp.setLCN(LogicalChannelNumber);
                            programInfoTmp.setDisplayNum(LogicalChannelNumber);
                            LogUtils.d("find service id = "+programInfoTmp.getServiceId()+" name = "+programInfoTmp.getDisplayName()+
                                    " Lcn = "+programInfoTmp.getLCN()+ " ts id = "+desc_TransportStream_id);
                        }
                    }
                }
            }
        }
        LogUtils.d("OUT");
    }

    public void Scan_process_Nit_SUND(NitData nitData, List<ProgramInfo> ProgramInfoList){
        int i, j,k,TransportStream_id,OriginalNetwork_id,Server_id,LogicalChannelNumber;
        ProgramInfo programInfoTmp=null;
        ProgramInfo programInfo= new ProgramInfo();
        LogUtils.d("IN");
        if(nitData == null)
            LogUtils.d("nitData = null");
        for(i=0, j=0; nitData != null && i<nitData.getTransportStreamList().size();i++) {
            NitData.TransportStream transportStream = nitData.getTransportStreamList().get(i);
            for(j=0 ; j<transportStream.getDescriptorList().size() ; j++){
                DescBase descriptor = transportStream.getDescriptorList().get(j);
                LogUtils.d("TAG = "+descriptor.Tag);
                if(descriptor.Tag == Descriptor.LOGICAL_CHANNEL_DESC){
                    LogicalChannelNumberDescriptor pcds = (LogicalChannelNumberDescriptor)descriptor;
                    TransportStream_id=transportStream.getTransportStreamID();
                    OriginalNetwork_id=transportStream.getOriginalNetworkID();
                    for(k=0 ; k< pcds.mLogicalChannelNumberList.size() ; k++) {
                        Server_id=pcds.mLogicalChannelNumberList.get(k).ServiceId;
                        LogicalChannelNumber=pcds.mLogicalChannelNumberList.get(k).LogicalChannelMumber;
                        programInfo.setServiceId(Server_id);
                        programInfo.setTransportStreamId(TransportStream_id);
                        programInfo.setOriginalNetworkId(OriginalNetwork_id);
                        programInfoTmp = FindServiceByTripleID(ProgramInfoList, programInfo);
                        if(programInfoTmp != null){
                            programInfoTmp.setLCN(LogicalChannelNumber);
                            programInfoTmp.setDisplayNum(LogicalChannelNumber);
                        }
                    }
                }
            }
        }
        LogUtils.d("OUT");
    }

    public void Scan_process_Bat_SUND(List<BatData> batDataList, List<ProgramInfo> ProgramInfoList){
        LogUtils.d("IN");
        int i,j,k,m,n,size;
        int MAX_GROUP = Pvcfg.GROUP_NO;
        int TransportStreamId;
        int OriginalNetworkId;
        int ServiceId;
        int pro=0;
        List<Bouquet> bouquets = new ArrayList<>();
        List<BatData.TransportStream> BatTsTmp=null;
        ProgramInfo programInfo = new ProgramInfo();
        size=batDataList.size();
        j=0;
        for(i=0;i<size;i++) {
            if (batDataList.get(i).getBouquetId() != Scan_cfg.SUNTV_CA_BOUQUET_ID) {
                bouquets.add(new Bouquet(batDataList.get(i).getBouquetId(),batDataList.get(i).getVersion(),batDataList.get(i).getBouquetNametreamList().get(0).getBouquetName()));
                //LogUtils.d("@@@@@@ bouquetsId="+bouquets.get(j).id+", bouquetsVersion="+bouquets.get(j).version+", bouquetsName="+bouquets.get(j).name);
                j++;
                if(MAX_GROUP == j)
                    break;
            }
        }
        size=bouquets.size();
        if(size > 0) {
            if(size > 1) {
                Collections.sort(bouquets, Comparator.comparingInt(b -> b.id));
            }
            GposInfo.initBouquetIds(mContext);
            GposInfo.initBouquetVers(mContext);
            GposInfo.initBouquetNames(mContext);
            //return;
        }

        size=batDataList.size();

        for(j=0;j<bouquets.size();j++) {
            for (i = 0; i < size; i++) {
                if ((batDataList.get(i).getBouquetId() != Scan_cfg.SUNTV_CA_BOUQUET_ID) && (batDataList.get(i).getBouquetId() == bouquets.get(j).id)) {
                    m = batDataList.get(i).getTransportStreamListSize();
                    BatTsTmp = batDataList.get(i).getTransportStreamList();
                    if (BatTsTmp != null) {
                        m = batDataList.get(i).getTransportStreamListSize();
                        for (k = 0; k < m; k++) {
                            int sCnt;
                            TransportStreamId = BatTsTmp.get(k).getTransportStreamId();
                            OriginalNetworkId = BatTsTmp.get(k).getOriginalNetworkId();
                            sCnt = BatTsTmp.get(k).getServiceList().size();
                            for (n = 0; n < sCnt; n++) {
                                ProgramInfo programInfoTmp;
                                long categoryTmp;
                                ServiceId = BatTsTmp.get(k).getServiceList().get(n).ServiceID;
                                programInfo.setOriginalNetworkId(OriginalNetworkId);
                                programInfo.setTransportStreamId(TransportStreamId);
                                programInfo.setServiceId(ServiceId);
                                programInfoTmp = FindServiceByTripleID(ProgramInfoList, programInfo);
                                categoryTmp=programInfoTmp.getCategory_type();
                                categoryTmp=categoryTmp | (1L << j);
                                programInfoTmp.setCategory_type(categoryTmp);
                                //category_add_to_fav_SUND(j,bouquets.get(j).id,bouquets.get(j).name,programInfoTmp);
                                pro++;
                            }
                        }
                    }
                }
            }
        }
        category_add_to_fav_SUND(bouquets,ProgramInfoList);
        for(i=0;i<mDataManager.getGposInfo().MAX_NUM_OF_GROUP;i++){
            if(i < size){
                GposInfo.setBouquetIds(mContext, i,bouquets.get(i).id);
                GposInfo.setBouquetVers(mContext, i,bouquets.get(i).version);
                GposInfo.setBouquetNames(mContext, i,bouquets.get(i).name);
            }
        }

        bouquets.removeAll(Collections.emptyList());
        LogUtils.d("OUT");
    }

    public void Scan_process_Sdt_SUND(List<SdtData> sdtDataList, List<ProgramInfo> ProgramInfoList){
        LogUtils.d("IN");

        int i,j,desc_index, tp_id, add_new=0,tp_index;
        total_channel = 0;
        //for(i=0 ; i< sdtDataList.size() ; i++)
        List<TpInfo> tpInfoList = mDataManager.getTpInfoList();
        LogUtils.d("SSSSSS Scan_process_Sdt_SUND in");
        if(sdtDataList != null && sdtDataList.size()>0)
        {
            //LogUtils.d("sdtDataList.size() "+ sdtDataList.size());
            for(i=0;i<sdtDataList.size();i++) {
                List<SdtData.ServiceData> serviceDataList = sdtDataList.get(i).geServiceData();
                for (j = 0; j < serviceDataList.size(); j++) {
                    SdtData.ServiceData serviceData = serviceDataList.get(j);
                    add_new = 0;
                    tp_index = GetTpIndexByTSid(serviceData.getTransportStreamId());
                    if (tp_index == -1) {
                        continue;
                    }
                    tpInfoList.get(tp_index).setSdt_version(serviceData.getVersionNumber());
                    ProgramInfo programInfo = mDataManager.getProgramInfo(ProgramInfoList, serviceData.getServiceId(), serviceData.getTransportStreamId(), serviceData.getOriginalNetworkId());//new ProgramInfo();
                    if (programInfo == null) {
                        add_new = 1;
                        programInfo = new ProgramInfo();
                    }
                    tp_id = tpInfoList.get(tp_index).getTpId() ;
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
                        }
                    }
                    if (add_new == 1) {
                        ProgramInfoList.add(programInfo);
                        total_channel++;
                    }
                }
            }
        }
        LogUtils.d("OUT");
    }
    
    public void no_lcn_channel_set_delete(List<ProgramInfo> ProgramInfoList) {
        int i=0;
        for(ProgramInfo programInfo : ProgramInfoList){
            if((programInfo.getLCN() == 0)){
                i++;
                programInfo.setDeleteFlag(1);
            }
        }
        LogUtils.d("no_lcn_channel_set_delete = "+i);
    }

    public void no_channelId_channel_set_delete(List<ProgramInfo> ProgramInfoList) {
        int i=0;
        for(ProgramInfo programInfo : ProgramInfoList){
            if((programInfo.getChannelId() == 0)){
                i++;
                programInfo.setDeleteFlag(1);
            }
        }
        LogUtils.d("no_channelId_channel_set_delete = "+i);
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

    public void remove_mark_delete_tp_and_reset_sat(int SatelliteId){
        LogUtils.d("IN");
        int i,j;
        List<TpInfo> tpInfoList = mDataManager.getTpInfoList();
        SatInfo satInfo = mDataManager.getSatInfo(SatelliteId);
        List<Integer> deleteIndex = new ArrayList<>();
        List<SatInfo> satInfoList = mDataManager.getSatInfoList();
        TpInfo  temp;
        for(i = 0 ; i < satInfo.getTps().size(); i++) {
            temp = mDataManager.getTpInfo(satInfo.getTps().get(i));
            if(temp.SatTp.getNotDelete() == 0) {
                if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DIGITURK) &&
                        temp.SatTp.getFreq() == 11675) {
                    temp.SatTp.setNotDelete(0);
                    break;
                }
                deleteIndex.add(temp.getTpId());
            }
            else
                temp.SatTp.setNotDelete(0);
        }
        for(j=0;j < deleteIndex.size() ;j++){
            LogUtils.d("#@#@#@ deleteIndex.get("+j+")="+deleteIndex.get(j));
            mDataManager.delTpInfo(deleteIndex.get(j));
        }
        //LogUtils.d("#@#@#@ tpInfoList.size()="+tpInfoList.size());
        //LogUtils.d("#@#@#@ satInfo.getTpNum()="+satInfo.getTpNum());
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

    public ProgramInfo FindServiceByServiceID_TransportID(List<ProgramInfo> ProgramInfoLis, ProgramInfo programInfo){
        for(int i=0;i<ProgramInfoLis.size();i++){
            if(ProgramInfoLis.get(i).getServiceId() == programInfo.getServiceId() &&
                    ProgramInfoLis.get(i).getTransportStreamId() == programInfo.getTransportStreamId()){
                return ProgramInfoLis.get(i);
            }
        }
        return null;
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
                    PrimeDtv dtv = ServiceInterface.get_prime_dtv();
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
                PrimeDtv dtv = ServiceInterface.get_prime_dtv();
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

    public void category_add_to_fav(List<ProgramInfo> ProgramInfoList,boolean del_all_fav){
        mDataManager.category_add_to_fav(ProgramInfoList,del_all_fav);
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

    public void category_add_to_fav_SUND(List<Bouquet> bouquets, List<ProgramInfo> ProgramInfoList) {
        List<FavGroup> favGroupList_SUDN_Old = mDataManager.getFavGroupList();
        List<FavGroup> favGroupList_SUDN_new = new ArrayList<>();
        List<FavGroupName> favGroupNameList_SUND = new ArrayList<>();
        List<ProgramInfo> ProgramInfoList_old = mDataManager.getProgramInfoList();
        int i,j,k,pCnt,pIdx,tvCnt=0,radioCnt=0,pCntOld=0,categoryCnt;
        int fav_type,tv_type;
        long category_type,channelId;
        ProgramInfo programInfo_tmp,programInfo_old;

        categoryCnt=bouquets.size();

        i = 0;
        //LogUtils.d("@@@@@@ favGroupNameList_SUND.size() = "+favGroupNameList_SUND.size());
        favGroupNameList_SUND.add(new FavGroupName(FavGroup.ALL_TV_TYPE, "All TV"));
        //LogUtils.d("@@@@@@ favGroupNameList_SUND.size() = "+favGroupNameList_SUND.size());
        for (i = 0; i < FavGroupName.TV_FAV_NUM; i++) {
            favGroupNameList_SUND.add(new FavGroupName(FavGroup.TV_FAV_BASE_TYPE + i, "TV Favorite " + i));
        }
        //LogUtils.d("@@@@@@ favGroupNameList_SUND.size() = "+favGroupNameList_SUND.size());
        favGroupNameList_SUND.add(new FavGroupName(FavGroup.ALL_RADIO_TYPE, "All Radio"));
        //LogUtils.d("@@@@@@ favGroupNameList_SUND.size() = "+favGroupNameList_SUND.size());
        for (i = 0; i < FavGroupName.RADIO_FAV_NUM; i++) {
            favGroupNameList_SUND.add(new FavGroupName(FavGroup.RADIO_FAV_BASE_TYPE + i, "Radio Favorite " + i));
        }
        //LogUtils.d("@@@@@@ favGroupNameList_SUND.size() = "+favGroupNameList_SUND.size());
        for (i = 0; i < FavGroupName.GENRE_NUM; i++) {
            favGroupNameList_SUND.add(new FavGroupName(FavGroup.GENRE_BASE_TYPE + i, "" ));
        }
        //LogUtils.d("@@@@@@ favGroupNameList_SUND.size() = "+favGroupNameList_SUND.size());

        for(FavGroupName favGroupName : favGroupNameList_SUND) {
            favGroupList_SUDN_new.add(new FavGroup(favGroupName));
        }

        //LogUtils.d("@@@@@@ favGroupList_SUDN_Old.size() = "+favGroupList_SUDN_Old.size());

        List<FavGroup> favGroupList_Old = mDataManager.getFavGroupList();
        //LogUtils.d("@@@@@@ favGroupList_Old.size() = "+favGroupList_Old.size());

        for (i = 0; i < FavGroupName.TV_FAV_NUM; i++) {
            pIdx = FavGroup.TV_FAV_BASE_TYPE + i;
            //LogUtils.d("@@@@@@ TV_FAV_BASE_TYPE,i = "+i+" pIdx = "+pIdx);
            favGroupList_SUDN_new.set(pIdx,favGroupList_SUDN_Old.get(pIdx));
        }
        for (i = 0; i < FavGroupName.RADIO_FAV_NUM; i++) {
            pIdx= FavGroup.RADIO_FAV_BASE_TYPE + i;
            //LogUtils.d("@@@@@@ RADIO_FAV_BASE_TYPE,i = "+i+" pIdx = "+pIdx);
            favGroupList_SUDN_new.set(pIdx,favGroupList_SUDN_Old.get(pIdx));
        }

        pCntOld=ProgramInfoList_old.size();
        LogUtils.d("@@@@@@ pCntOld = "+pCntOld);
        pCnt=ProgramInfoList.size();
        //LogUtils.d("@@@@@@ pCntOld = "+pCntOld+", pCnt = "+pCnt);
        for(i=0;i<pCntOld;i++){
            programInfo_old=ProgramInfoList_old.get(i);
            //LogUtils.d("@@@@@@ i = "+i);
            //fav_type=programInfo_old.getFav_type();
            //if(fav_type != 0)
            {
                for(j=0;j<pCnt;j++){
                    programInfo_tmp=ProgramInfoList.get(j);
                    if(programInfo_old.getChannelId() == programInfo_tmp.getChannelId()){
                        break;
                    }
                }
                if(j >= pCnt){

                    //LogUtils.d("@@@@@@ Program has been removed");
                    for(k= FavGroup.TV_FAV_BASE_TYPE; k<= FavGroup.TV_FAV_MAX_TYPE; k++){
                        List<FavInfo> favInfoList=favGroupList_SUDN_new.get(k).getFavInfoList();
                        for(FavInfo favInfo : favInfoList) {
                            if(favInfo.getChannelId()==programInfo_old.getChannelId()){
                                favInfoList.remove(favInfo);
                                break;
                            }
                        }
                    }
                    for(k= FavGroup.RADIO_FAV_BASE_TYPE; k<= FavGroup.RADIO_FAV_MAX_TYPE; k++){
                        List<FavInfo> favInfoList=favGroupList_SUDN_new.get(k).getFavInfoList();
                        for(FavInfo favInfo : favInfoList) {
                            if(favInfo.getChannelId()==programInfo_old.getChannelId()){
                                favInfoList.remove(favInfo);
                                break;
                            }
                        }
                    }
                }
                else{
                    //LogUtils.d("@@@@@@ programInfo_old == programInfo_tmp");
                }
            }
        }

        for(pIdx=0;pIdx<pCnt;pIdx++){
            programInfo_tmp=ProgramInfoList.get(pIdx);
            if(programInfo_tmp == null)
                LogUtils.d("@@@@@@ programInfo_tmp == null ");
            //fav_type=programInfo_tmp.getFav_type();
            category_type=programInfo_tmp.getCategory_type();
            channelId=programInfo_tmp.getChannelId();
            tv_type=programInfo_tmp.getType();
            //LogUtils.d("@@@@@@ pIdx = "+pIdx+", category_type = "+category_type+", channelId = "+channelId+", tv_type = "+tv_type);
            if(tv_type == ProgramInfo.PROGRAM_TV){
                List<FavInfo> favInfoList = favGroupList_SUDN_new.get(FavGroup.ALL_TV_TYPE).getFavInfoList();
                favInfoList.add(new FavInfo(programInfo_tmp.getLCN(), channelId, FavGroup.ALL_TV_TYPE));
                tvCnt++;
            }
            else if(tv_type == ProgramInfo.PROGRAM_RADIO){
                List<FavInfo> favInfoList = favGroupList_SUDN_new.get(FavGroup.ALL_RADIO_TYPE).getFavInfoList();
                favInfoList.add(new FavInfo(programInfo_tmp.getLCN(), channelId, FavGroup.ALL_RADIO_TYPE));
                radioCnt++;
            }
            //LogUtils.d("@@@@@@ tvCnt = "+tvCnt+", radioCnt = "+radioCnt+", categoryCnt = "+categoryCnt);
            if(category_type != 0){
                for(i=0;i<categoryCnt;i++){
                    if(((category_type & (0x1L << i)) >> i) == 1) {
                        //LogUtils.d("@@@@@@ ((category_type & (0x1L << i)) >> i) == 1, i = "+i);
                        int categoryIdx=i+ FavGroup.GENRE_BASE_TYPE;
                        int favNum=programInfo_tmp.getLCN();
                        FavInfo favInfo = new FavInfo(favNum,channelId,categoryIdx);
                        favGroupList_SUDN_new.get(categoryIdx).getFavGroupName().setGroupName(bouquets.get(i).name);
                        //favGroupList_SUDN_new.get(categoryIdx).getFavGroupName().setGroupType(bouquets.get(i).id);
                        favGroupList_SUDN_new.get(categoryIdx).getFavGroupName().setGroupType(categoryIdx);
                        favGroupList_SUDN_new.get(categoryIdx).addFavInfo(favInfo);
                    }
                    else{
                        //LogUtils.d("@@@@@@ ((category_type & (0x1L << i)) >> i) != 1, i = "+i);
                    }
                }
                favGroupList_SUDN_new.get(0).setFavGroupUsed(categoryCnt);
            }
        }
        mDataManager.setFavGroupList(favGroupList_SUDN_new);
    }
}

