package com.prime.dtv.service.Scan;

import static com.prime.dtv.service.Table.Desciptor.Descriptor.LINKAGE_DESC;

import android.content.Context;
import android.os.SystemProperties;

import com.prime.dmg.launcher.HomeApplication;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Table.Desciptor.DescBase;
import com.prime.dtv.service.Table.Desciptor.LinkageDescriptor;
import com.prime.dtv.service.Table.Nit;
import com.prime.dtv.service.Table.NitData;
import com.prime.dtv.service.Table.Sdt;
import com.prime.dtv.service.Table.SdtData;
import com.prime.dtv.service.Table.Bat;
import com.prime.dtv.service.Table.BatData;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.sysdata.DTVMessage;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.MusicInfo;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class SIUpdater extends Updater{
    private static final String TAG = "SIUpdater";
    private static final Object mSiUpdateDmgLock = new Object();
    private static final int NAGRA_NETWORK_ID = 0x31;
    private static final int CTI_NETWORK_ID = 0x457;
    private static boolean isE301 = false;
    private boolean DEBUG = SystemProperties.getBoolean("persist.sys.prime.debug.si_update",false);

    private PesiDtvFrameworkInterfaceCallback mCallback;
    public SIUpdater(Context context, PesiDtvFrameworkInterfaceCallback callback, long channel_id) {
        super(context,channel_id);
        mCallback=callback;
        //LogUtils.d("SIUpdater channelId = "+ channel_id);
    }

    @Override
    protected void proecee(long channel_id) {
        run_si_update(channel_id);
    }

    private void run_si_update(long channelId){
        ProgramInfo programInfo = this.mDataManager.getProgramInfo(channelId);
        //int update_flag;
        int count=0;
        if(programInfo == null){
            LogUtils.d("Invaild ProgramInfo channelId="+ channelId);
            return;
        }
        //LogUtils.d("IN channelId = "+channelId+" Lcn = "+programInfo.getLCN());
        while(true) {
            try {
                programInfo = this.mDataManager.getProgramInfo(getChannelId());
                if(programInfo == null){
                    LogUtils.d("Invaild ProgramInfo channelId="+ getChannelId());
                    continue;
                }
                if(Pvcfg.getModuleType() == Pvcfg.MODULE_DMG) {
                    Pesi_Monitor_SI_DMG(programInfo);
                }
                else if(Pvcfg.getModuleType() == Pvcfg.MODULE_TBC) {
                    Pesi_Monitor_SI_TBC(programInfo);
                }
                else{
                    LogUtils.d("The results of Pvcfg.getModuleType() are unexpected");
                    stop();
                }
                if(IsStop() == true)
                {
                    LogUtils.d("stop run_si_update channelId "+channelId);
                    break;
                }

                Thread.sleep(1000);
            }catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private int Pesi_Check_SI_DMG(GposInfo gposInfo, ProgramInfo programInfo, NitData nitData, List<SdtData> sdtDataList, int check_allupdate_flag) {
        int i,j,size,count,NetVer, NetId, TunerId,nit_update_flag=0,sdt_update_flag=0,update_flag=0;
        Nit nit;
        Sdt sdt;
        List<TpInfo> tpInfoList=null;

        int TsId;
        update_flag=0;
        if((nitData == null) || (sdtDataList == null))
            return update_flag;

        NetVer = gposInfo.getNitVersion();
        NetId = gposInfo.getSINitNetworkId();
        TunerId = Pvcfg.PROJECT_HOME_TP_TUNER_ID;//programInfo.getTunerId();

        if (nitData != null) {
            if(nitData.getNetworkStreamList().size() > 0){
                NetVer=mDataManager.getGposInfo().getNitVersion();//mNitData.getNetworkStreamList().get(0).getVersion();
                NetId=nitData.getNetworkStreamList().get(0).getNetworkID();
                if(DEBUG) {
                    LogUtils.d("NetId = "+NetId);
                    LogUtils.d("NetVersionSTB = "+NetVer);
                }
                if(CTI_NETWORK_ID != NetId) {
                    if (NetVer != 0xff) {
                        int NetVerLive;
                        NetVerLive = nitData.getNetworkStreamList().get(0).getVersion();
                        if(DEBUG) {
                            LogUtils.d("NetVerisonLive = "+NetVerLive+" NetVersionSTB = "+NetVer);
                        }
                        if (NetVerLive != NetVer) {
                            nit_update_flag = 1;
                        }
                    }
                }
                for(i=0, j=0; nitData != null && i<nitData.getNetworkStreamList().size();i++){
                    List<DescBase> descBaseListTmp;
                    NitData.NetworktStream  networktStream = nitData.getNetworkStreamList().get(i);
                    descBaseListTmp=networktStream.getDescriptor().getDescriptorList(LINKAGE_DESC);
                    for (DescBase temp : descBaseListTmp) {
                        LinkageDescriptor tempDescriptor = (LinkageDescriptor) temp;
                        if (tempDescriptor.LinkageType == 0xCA){
                            List<LinkageDescriptor.WvCasUrlData> WvCasUrlDatas = tempDescriptor.mWvCasUrlDataList;
                            if(!WvCasUrlDatas.isEmpty()){
                                LogUtils.d("WVCas URL = "+WvCasUrlDatas.get(0).mUrl);
                                LogUtils.d("mStbUpdateTime = "+WvCasUrlDatas.get(0).mStbUpdateTime+" "+mDataManager.getGposInfo().getSTBRefCasDataTime());
                                if(!WvCasUrlDatas.get(0).mUrl.equalsIgnoreCase(mDataManager.getGposInfo().getWVCasLicenseURL()) ||
                                        WvCasUrlDatas.get(0).mStbUpdateTime != mDataManager.getGposInfo().getSTBRefCasDataTime()){
                                    mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_WVCAS_URL,WvCasUrlDatas.get(0).mStbUpdateTime,(int)getChannelId(),WvCasUrlDatas.get(0).mUrl);
                                }
                            }
                        }
                    }
                }
            }
        }

        if(sdtDataList != null) {
            tpInfoList=mDataManager.getTpInfoList();
            size=sdtDataList.size();
            if(DEBUG)
                LogUtils.d("mSdtDataList.size() = "+size);
            for (i = 0; i < size; i++) {
                count=sdtDataList.get(i).getServiceDataTotalNum();
                for(j = 0; j < count; j++) {
                    int find = 0, index, ServiceId;
                    TsId = sdtDataList.get(i).getTransportStreamIdByIndex(j);
                    ServiceId = sdtDataList.get(i).getServiceIdByIndex(j);
                    if(DEBUG) {
                        LogUtils.d("i = " + i);
                        LogUtils.d("TransportStreamId = " + TsId + " ServiceId = " + ServiceId);
                    }
                    for (index = 0; index < tpInfoList.size(); index++) {
//                        if(DEBUG) {
//                            LogUtils.d("index = "+ index);
//                            LogUtils.d("TransportStreamIdByTp = "+tpInfoList.get(index).getTransport_id());
//                        }
                        if (TsId == tpInfoList.get(index).getTransport_id()) {
                            if(DEBUG) {
                                LogUtils.d("Find tpInfoList[] index = "+index);
                                LogUtils.d("TransportStreamIdByTp = "+tpInfoList.get(index).getTransport_id());
                            }
                            break;
                        }
                    }
                    if (index < tpInfoList.size()) {
                        int SdtVer, SdtVer_Tp;
                        if(DEBUG) {
                            LogUtils.d("Find Tp");
                        }
                        SdtVer = sdtDataList.get(i).getVersionNumberByIndex(j);
                        SdtVer_Tp = tpInfoList.get(index).getSdt_version();
                        if(DEBUG) {
                            int TransportStreamIdByTp = tpInfoList.get(index).getTransport_id();
                            LogUtils.d("TransportStreamId = "+TsId+" SdtVersion = "+SdtVer);
                            LogUtils.d("TransportStreamIdByTp = "+TransportStreamIdByTp+" SdtVersionByTp = "+SdtVer_Tp);
                        }
                        if ((SdtVer != 0xff) && (SdtVer_Tp != SdtVer)) {
                            sdt_update_flag = 1;
                            if(check_allupdate_flag == 1) {
                                LogUtils.d("TransportStreamId ["+TsId+"] SDT Version change ["+SdtVer+"] SdtVer_Tp ["+SdtVer_Tp+"]");
                                tpInfoList.get(index).setSdt_version(SdtVer);
                                mDataManager.DataManagerSaveTpInfo(tpInfoList.get(index));
                            }
                            break;
                        }
                    }
                }
            }
        }


        if(sdt_update_flag ==1 && nit_update_flag == 1)
            update_flag=3;
        else if(sdt_update_flag == 1)
            update_flag=2;
        else if(nit_update_flag == 1)
            update_flag=1;
        else
            update_flag=0;

        //if(DEBUG)
        LogUtils.d("sdt_update_flag = "+sdt_update_flag+" nit_update_flag = "+nit_update_flag+" update_flag = "+update_flag);
        return update_flag;
    }

    private int Pesi_Monitor_SI_DMG(ProgramInfo programInfo) {
        int update_flag=0,check_allupdate_flag;
        List<ProgramInfo> programInfoList=new ArrayList<>();
        List<ProgramInfo> currProgramInfoList;
        ProgramInfo programInfoTmp;
        int TunerId = programInfo.getTunerId();
        Boolean need_to_setchannel,need_notify_ui = false;
        synchronized(mSiUpdateDmgLock) {
            currProgramInfoList=mDataManager.getProgramInfoList();
            Nit nit = null;
            Sdt sdt = null;
            NitData NitData = null;
            List<SdtData> SdtDataList = null;
            //ProgramInfo programInfoOld = new ProgramInfo(programInfo);
            //long goToChannelbyChId=-1;
            try {
                check_allupdate_flag = 0;
                while (check_allupdate_flag < 2) {
                    LogUtils.d("Pesi_Monitor_SI IN, check_allupdate_flag = "+check_allupdate_flag);
                    LogUtils.d("CH "+programInfo.getDisplayNum()+" "+programInfo.getDisplayName());
                    if (IsStop() == true) {
                        LogUtils.d("Pesi_Monitor_SI Stop");
                        reset_si_table(nit, NitData, sdt, SdtDataList, null, null);
                        break;
                    }
                    nit = new Nit(TunerId, true);
                    nit.processWait();
                    NitData = nit.getNitData();
                    if (NitData != null) {
                        if(DEBUG)
                            LogUtils.d("Got Nit data");
                        sdt = new Sdt(TunerId, true, true, NitData);
                        sdt.processWait();
                        SdtDataList = sdt.getSdtData();
                        if (SdtDataList != null) {
                            if(DEBUG)
                                LogUtils.d("Got Sdt data");
                        }
                    }

                    if (IsStop() == true || SdtDataList == null || NitData == null) {
                        LogUtils.d("Pesi_Monitor_SI Stop "+" IsStop() = "+ IsStop()+" SdtDataList = "+ SdtDataList+ " NitData = "+NitData);
                        reset_si_table(nit, NitData, sdt, SdtDataList, null, null);
                        break;
                    }

                    update_flag = Pesi_Check_SI_DMG(mDataManager.getGposInfo(), programInfo, NitData, SdtDataList, check_allupdate_flag);

                    if (IsStop() == true) {
                        LogUtils.d("Pesi_Monitor_SI Stop");
                        reset_si_table(nit, NitData, sdt, SdtDataList, null, null);
                        break;
                    }
/*
                if(sdt_update_flag ==1 || nit_update_flag == 1)
                    update_flag=3;
                else if(sdt_update_flag == 1)
                    update_flag=2;
                else if(nit_update_flag == 1)
                    update_flag=1;
                else
                    update_flag=0;
 */
                    if (update_flag != 0) {
                        if (!(check_allupdate_flag == 3) && check_allupdate_flag == 0) {
                            check_allupdate_flag = 1;
                            //LogUtils.d("adam 20230302 fix SI update not work");
                            reset_si_table(nit, NitData, sdt, SdtDataList, null, null);
                            Thread.sleep(5000);
                            continue;
                        }
                        check_allupdate_flag = 2;

                        mScanUtils.Scan_process_Nit(NitData);
                        mScanUtils.Scan_process_Sdt_DMG(SdtDataList, programInfoList);
                        mScanUtils.Scan_process_Nit_DMG(NitData, programInfoList);

                        mScanUtils.no_lcn_channel_set_delete(programInfoList);
                        mScanUtils.remove_mark_delete_service(programInfoList);
                        mScanUtils.SortProgrameInfoByLCN(programInfoList);

                        if (IsStop() == true) {
                            LogUtils.d("Pesi_Monitor_SI Stop");
                            programInfoList.clear();
                            reset_si_table(nit, NitData, sdt, SdtDataList, null, null);
                            break;
                        }
                        //LogUtils.d("currProgramInfoList.size() = "+currProgramInfoList.size());
                        //LogUtils.d("programInfoList.size() = "+programInfoList.size());
                    /*
                    for(ProgramInfo programTmp1:currProgramInfoList){
                        LogUtils.d("oLCN = "+programTmp1.getLCN()+" oServiceId = "+programTmp1.getServiceId()+" oChName = "+programTmp1.getChName_chi());
                    }
                    for(ProgramInfo programTmp2:programInfoList){
                        LogUtils.d("nLCN = "+programTmp2.getLCN()+" nServiceId = "+programTmp2.getServiceId()+" nChName = "+programTmp2.getChName_chi());
                    }
                    */

                        need_notify_ui = mScanUtils.CheckMarkServiceDelete(currProgramInfoList, programInfoList);
                        mScanUtils.DelMarkedProgram(currProgramInfoList);
                        need_notify_ui |= mScanUtils.AddServiceInfoListWithCompareFavoriteGetServiceInfo(currProgramInfoList, programInfoList);
                        mScanUtils.SortProgrameInfoByLCN(currProgramInfoList);
                        programInfoTmp = mScanUtils.FindServiceByTripleID(currProgramInfoList, programInfo);

                        if (update_flag > 1) {
                            List<MusicInfo> musicInfoList = MusicInfo.get_current_category(this.mContext);
                            mScanUtils.category_update_to_fav(currProgramInfoList, musicInfoList);
                        }

                        need_to_setchannel = false;

                        if (programInfoTmp != null) {
                            if (programInfoTmp.getTpId() != programInfo.getTpId())
                                need_to_setchannel = true;
                            if ((programInfoTmp.getSkip() == 0) || (programInfoTmp.getLCN() == 260/*DMG_START_UP_CHANNEL_LCN*/)) {
                                if (need_to_setchannel == true) {
                                    //goToChannelbyChId = programInfoTmp.getChannelId();
                                    need_to_setchannel = true;
                                }
                            }
                        }
                        else {
                            int i;
                            //goToChannelbyChId=currProgramInfoList.get(0).getChannelId();
                            need_to_setchannel = true;
                            //programInfoTmp = currProgramInfoList.get(0);
                            for(i=0;i<currProgramInfoList.size();i++) {
                                if (currProgramInfoList.get(i).getType() == ProgramInfo.PROGRAM_TV) {
                                    programInfoTmp = currProgramInfoList.get(i);
                                    break;
                                }
                            }
                        }
                        //LogUtils.d("PreCh LCN = "+programInfo.getLCN()+" Name = "+programInfo.getChName_chi());
                        //LogUtils.d("PostCh LCN = "+programInfoTmp.getLCN()+" Name = "+programInfoTmp.getChName_chi());

//                        mDataManager.DataManagerSaveData(DataManager.SV_GPOS);
//                        mDataManager.DataManagerSaveData(DataManager.SV_TP_INFO);
//                        mDataManager.DataManagerSaveData(DataManager.SV_PROGRAM_INFO);
//                        mDataManager.DataManagerSaveData(DataManager.SV_FAV_INFO);
                        //mScanUtils.SortProgrameInfoByLCN(currProgramInfoList);
                        LogUtils.e("need_notify_ui = "+need_notify_ui);
                        if(need_notify_ui)
                        {
                            //if(DEBUG)
                                LogUtils.d("Need Notify UI to get new channel list");
                            mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_CHANNEL, 1, 0, null);
                        }
                        if (need_to_setchannel == true) {
                            //if(DEBUG)
                                LogUtils.d("Need change channel");
                            mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_SET_1ST_CHANNEL, 0, 1, programInfoTmp);
                        }
                        else {
                            //if(DEBUG)
                                LogUtils.d("Don't need change channel");
                        }
                    }
                    check_allupdate_flag = 2;
                }
                reset_si_table(nit, NitData, sdt, SdtDataList, null, null);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return update_flag;
        }
    }

    private int Pesi_Check_White_List_TBC(NitData nitData){
        int i;

        boolean	batInWhiteList=true;

        if(nitData == null)
            return -1;

        for(i=0; nitData != null && i<nitData.getNetworkStreamList().size();i++){
            List<DescBase> descBaseListTmp;
            NitData.NetworktStream  networktStream = nitData.getNetworkStreamList().get(i);
            descBaseListTmp=networktStream.getDescriptor().getDescriptorList(LINKAGE_DESC);
            //LogUtils.d(" Linkage decscript size = "+descBaseListTmp.size());
            for (DescBase temp : descBaseListTmp) {
                LinkageDescriptor tempDescriptor = (LinkageDescriptor) temp;
                //LogUtils.d(" Linkage decscript LinkageType = "+tempDescriptor.LinkageType);
                if (tempDescriptor.LinkageType == 0xD2) {
                    List<LinkageDescriptor.BatWhiteList> batWhiteList = tempDescriptor.mBatWhiteList;
                    batInWhiteList = false;
                    for (LinkageDescriptor.BatWhiteList temp2 : batWhiteList) {
                        List<Integer> batIdList = temp2.BatIdList;
                        for (Integer temp3 : batIdList) {
                            LogUtils.d("Bat = "+temp3+" "+Pvcfg.getBatId());
                            if (temp3 == /*mDataManager.getGposInfo().getBatId()*/Pvcfg.getBatId()) {
                                batInWhiteList=true;
                                LogUtils.d("SI_TBC Bat ["+temp3+"] is in White List");
                                break;
                            }
                        }
                    }
                }
                if (tempDescriptor.LinkageType == 0xCA){
                    List<LinkageDescriptor.WvCasUrlData> WvCasUrlDatas = tempDescriptor.mWvCasUrlDataList;
                    if(!WvCasUrlDatas.isEmpty()){
                        //LogUtils.d("WVCas URL = "+WvCasUrlDatas.get(0).mUrl + " "+ mDataManager.getGposInfo().getWVCasLicenseURL());
                        //LogUtils.d("mStbUpdateTime = "+WvCasUrlDatas.get(0).mStbUpdateTime);
                        if(!WvCasUrlDatas.get(0).mUrl.equalsIgnoreCase(mDataManager.getGposInfo().getWVCasLicenseURL()) ||
                                WvCasUrlDatas.get(0).mStbUpdateTime != mDataManager.getGposInfo().getSTBRefCasDataTime()){
                            mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_WVCAS_URL,WvCasUrlDatas.get(0).mStbUpdateTime,(int)getChannelId(),WvCasUrlDatas.get(0).mUrl);
                        }
                    }
                }
            }
        }
        if(batInWhiteList == false){
            return 0;
        }
        else{
            return 1;
        }
    }

    private int Pesi_Check_SI_TBC(GposInfo gposInfo, ProgramInfo programInfo, NitData nitData, BatData batData,List<SdtData> sdtDataList){
        int i,j,size,count,NetVer, NetId, TunerId,nit_update_flag=0,sdt_update_flag=0,bat_update_flag=0,update_flag=0;
        Nit nit;
        Sdt sdt;
        List<TpInfo> tpInfoList=null;
        //boolean	batInWhiteList=false;
        int BatVer;
        int TsId;
        update_flag=0;
        if((nitData == null) /*|| (sdtDataList == null)*/)
            return update_flag;

        NetVer = gposInfo.getNitVersion();
        NetId = gposInfo.getSINitNetworkId();

        //LogUtils.d("SI_TBC gposInfo.getNitVersion() = "+ NetVer + " gposInfo.getSINitNetworkId()" + NetId);

        TunerId = Pvcfg.PROJECT_HOME_TP_TUNER_ID;//programInfo.getTunerId();

        if (nitData != null) {
            if(nitData.getNetworkStreamList().size() > 0){
                NetVer=mDataManager.getGposInfo().getNitVersion();//mNitData.getNetworkStreamList().get(0).getVersion();
                NetId=nitData.getNetworkStreamList().get(0).getNetworkID();
                //LogUtils.d("NetId = "+NetId);
                //LogUtils.d("NetVersionSTB = "+NetVer);
                if(NAGRA_NETWORK_ID != NetId) {
                    if (NetVer != 0xff) {
                        int NetVerLive;
                        NetVerLive = nitData.getNetworkStreamList().get(0).getVersion();
                        //LogUtils.d("NetVerisonLive = "+NetVerLive+" NetVersionSTB = "+NetVer);
                        if (NetVerLive != NetVer) {
                            nit_update_flag = 1;
                        }
                    }
                    else
                        nit_update_flag = 1;
                }
                else{
                    update_flag=0;
                    LogUtils.d("SI_TBC Network id is NAGRA_NETWORK_ID(0x31)");
                    return update_flag;
                }
            }
        }

        if(batData != null){
            int Verion;
            BatVer=mDataManager.getGposInfo().getBatVersion();
            Verion=batData.getVersion();
            if (Verion != 0xff) {
                if(Verion != BatVer)
                    bat_update_flag = 1;
            }
            else
                bat_update_flag = 1;
            if(bat_update_flag == 1){
                LogUtils.d("SI_TBC set BAT ID to Gpos "+batData.getBouquetId());
                mDataManager.getGposInfo().setBatId(batData.getBouquetId());
            }
        }

        if(sdtDataList != null) {
            tpInfoList=mDataManager.getTpInfoList();
            size=sdtDataList.size();
            //LogUtils.d("mSdtDataList.size() = "+size);
            for (i = 0; i < size; i++) {
                count=sdtDataList.get(i).getServiceDataTotalNum();
                for(j = 0; j < count; j++) {
                    int find = 0, index, ServiceId;
                    TsId = sdtDataList.get(i).getTransportStreamIdByIndex(j);
                    ServiceId = sdtDataList.get(i).getServiceIdByIndex(j);
                    //LogUtils.d("i = " + i);
                    //LogUtils.d("TransportStreamId = " + TsId + " ServiceId = " + ServiceId);
                    for (index = 0; index < tpInfoList.size(); index++) {
                        //LogUtils.d("index = "+ index);
                        //LogUtils.d("TransportStreamIdByTp = "+tpInfoList.get(index).getTransport_id());
                        if (TsId == tpInfoList.get(index).getTransport_id()) {
                            //LogUtils.d("Find tpInfoList[] index = "+index);
                            //LogUtils.d("TransportStreamIdByTp = "+tpInfoList.get(index).getTransport_id());
                            break;
                        }
                    }
                    if (index < tpInfoList.size()) {
                        int SdtVer, SdtVer_Tp;
                        //int TransportStreamIdByTp== tpInfoList.get(index).getTransport_id();
                        //LogUtils.d("Find Tp");
                        SdtVer = sdtDataList.get(i).getVersionNumberByIndex(j);
                        SdtVer_Tp = tpInfoList.get(index).getSdt_version();
                        //LogUtils.d("TransportStreamId = "+TsId+" SdtVersion = "+SdtVer);
                        //LogUtils.d("TransportStreamIdByTp = "+TransportStreamIdByTp+" SdtVersionByTp = "+SdtVer_Tp);
                        if ((SdtVer != 0xff) && (SdtVer_Tp != SdtVer)) {
                            sdt_update_flag = 1;
                            break;
                        }
                    }
                }
            }
        }



        if(nit_update_flag == 1)
            update_flag=(update_flag | 4);
        if(sdt_update_flag == 1)
            update_flag=(update_flag | 2);
        if(bat_update_flag == 1)
            update_flag=(update_flag | 1);

        LogUtils.d("SI_TBC sdt_update_flag = "+sdt_update_flag+" nit_update_flag = "+nit_update_flag+" update_flag = "+update_flag);
        return update_flag;
    }

    private int Pesi_Monitor_SI_TBC(ProgramInfo programInfo){
        int update_flag=0,check_allupdate_flag;
        List<ProgramInfo> programInfoList=new ArrayList<>();
        List<ProgramInfo> currProgramInfoList;
        ProgramInfo programInfoTmp;
        Boolean need_to_setchannel, need_notify_ui = false;
        int TunerId = programInfo.getTunerId();

        synchronized(mSiUpdateDmgLock) {
            currProgramInfoList=mDataManager.getProgramInfoList();
            int Pvcfg_BatId,GposInfo_BatId;
            Nit nit = null;
            Sdt sdt = null;
            Bat bat = null;
            NitData NitData = null;
            BatData batData = null;
            List<SdtData> SdtDataList = null;
            ProgramInfo programInfoOld = new ProgramInfo(programInfo);
            LogUtils.d("SI_TBC Lcn = "+programInfo.getLCN()+" Name = "+programInfo.getDisplayName());
            if(mTuneInterface.isLock(TunerId) == false){
                LogUtils.d("SI_TBC Tuner is no lock, TunerId = "+TunerId);
            }
            else{
                LogUtils.d("SI_TBC Tuner is lock, TunerId = "+TunerId);
            }
            //long goToChannelbyChId=-1;
            try {
                check_allupdate_flag = 0;

                while (check_allupdate_flag < 2) {
                    if (IsStop() == true) {
                        reset_si_table(nit, NitData, sdt, SdtDataList, bat, batData);
                        LogUtils.d("SI_TBC Pesi_Monitor_SI Stop");
                        break;
                    }
                    LogUtils.d("SI_TBC Pesi_Monitor_SI IN, check_allupdate_flag = "+check_allupdate_flag);
                    nit = new Nit(TunerId, true);
                    nit.processWait();
                    NitData = nit.getNitData();
                    if (NitData != null) {
                        LogUtils.d("Got Nit data");
                        if(Pesi_Check_White_List_TBC(NitData) == 0){
                            LogUtils.d("SI_TBC Bat is not in the whitelist");
                            LogUtils.d("SI_TBC Pesi_Monitor_SI Stop");
                            isE301 = true;
                            mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_TBC_BAT_WHITE_LIST_FAIL, 1, 0, null);
                            break;
                        }
                        sdt = new Sdt(TunerId, true, true, NitData);
                        sdt.processWait();
                        SdtDataList = sdt.getSdtData();
                        if (SdtDataList != null) {
                            LogUtils.d("Got Sdt data");
                        }
                    }


                    if (IsStop() == true /*|| SdtDataList == null*/ || NitData == null) {
                        LogUtils.e("SI_TBC Pesi_Monitor_SI Stop IsStop() = "+IsStop()+" SdtDataList = "+SdtDataList+" NitData = "+NitData);
                        reset_si_table(nit, NitData, sdt, SdtDataList, bat, batData);
                        break;
                    }
                    Pvcfg_BatId=Pvcfg.getBatId();
                    GposInfo_BatId=mDataManager.getGposInfo().getBatId();
                    if(Pvcfg_BatId != GposInfo_BatId){
                        LogUtils.d("SI_TBC Pvcfg_BatId != GposInfo_BatId, Pvcfg_BatId = "+Pvcfg_BatId+" GposInfo_BatId = "+GposInfo_BatId);
                        mDataManager.getGposInfo().setNitVersion(0xff);
                        mDataManager.getGposInfo().setBatVersion(0xff);
                    }
                    
                    if(Pvcfg_BatId != 0) {
                        LogUtils.d("SI_TBC Get Bat Table Pvcfg_BatId = "+Pvcfg_BatId);
                        bat = new Bat(TunerId, Pvcfg_BatId, true);
                        bat.processWait();
                        batData = bat.getBatData();
                    }

                    update_flag = Pesi_Check_SI_TBC(mDataManager.getGposInfo(), programInfo, NitData, batData, SdtDataList);

                    if (IsStop() == true) {
                        LogUtils.e("SI_TBC Pesi_Monitor_SI Stop");
                        reset_si_table(nit, NitData, sdt, SdtDataList, bat, batData);
                        break;
                    }
                    LogUtils.d("SI_TBC update_flag = "+update_flag);
                    if (update_flag != 0) {
                        if (!(check_allupdate_flag == 7) && check_allupdate_flag == 0) {
                            check_allupdate_flag = 1;
                            reset_si_table(nit, NitData, sdt, SdtDataList, bat, batData);
                            Thread.sleep(5000);
                            continue;
                        }
                        check_allupdate_flag = 2;
                        if(batData == null){
                            Pvcfg_BatId = 0;
                            LogUtils.e("BAT NULL!!!!!!!!!!!!!!");
                        }
                        if(SdtDataList == null){
                            LogUtils.e("SDT NULL!!!!!!!!!!!!!!");
                        }
                        if(NitData == null){
                            LogUtils.e("NIT NULL!!!!!!!!!!!!!!");
                        }
                        if(Pvcfg_BatId == 0){
                            //SearchProcessNit_1(&head, 0, tunerId);
                            //SearchProcessNit_2(&head, 1, tunerId);
                            //SearchProcessSdt(&head, 0, tunerId);
                            LogUtils.d("SI_TBC Need update, bat_id = "+Pvcfg_BatId);
                            mScanUtils.Scan_process_Nit(NitData);
                            mScanUtils.Scan_process_Nit_TBC(NitData, false, programInfoList);
                            mScanUtils.Scan_process_Sdt_TBC(SdtDataList, programInfoList);

                        }
                        else{
                            LogUtils.d("SI_TBC Need update, bat_id = "+Pvcfg_BatId);
                            mScanUtils.Scan_process_Nit(NitData);
                            mScanUtils.Scan_process_Bat(batData, programInfoList);
                            mScanUtils.Scan_process_Sdt_TBC(SdtDataList, programInfoList);
                            mScanUtils.Scan_process_Nit_TBC(NitData, true, programInfoList);
                        }

                        LogUtils.d("SI_TBC programInfoList number = "+programInfoList.size());
                        mScanUtils.no_lcn_channel_set_delete(programInfoList);
                        mScanUtils.remove_mark_delete_service(programInfoList);
                        mScanUtils.SortProgrameInfoByLCN(programInfoList);


                        need_notify_ui = mScanUtils.CheckMarkServiceDelete(currProgramInfoList, programInfoList);
                        mScanUtils.DelMarkedProgram(currProgramInfoList);
                        need_notify_ui |= mScanUtils.AddServiceInfoListWithCompareFavoriteGetServiceInfo(currProgramInfoList, programInfoList);
                        mScanUtils.SortProgrameInfoByLCN(currProgramInfoList);
                        if (update_flag > 1 && SdtDataList != null) {
                            List<MusicInfo> musicInfoList = MusicInfo.get_current_category(this.mContext);
                            mScanUtils.category_update_to_fav(currProgramInfoList, musicInfoList);
                        }
                        LogUtils.d("SI_TBC currProgramInfoList number = "+currProgramInfoList.size());

                        programInfoTmp = mScanUtils.FindServiceByTripleID(currProgramInfoList, programInfo);

                        need_to_setchannel = false;

                        if (programInfoTmp != null) {
                            LogUtils.d("programInfoTmp != NULL");
                            if (programInfoTmp.getTpId() != programInfo.getTpId())
                                need_to_setchannel = true;
                            if (programInfoTmp.getSkip() == 0) {
                                if (need_to_setchannel == true) {
                                    //goToChannelbyChId = programInfoTmp.getChannelId();
                                    need_to_setchannel = true;
                                }
                            }
                        }
                        else {
                            LogUtils.d("programInfoTmp == NULL");
                            int i;
                            need_to_setchannel = true;
                            for(i=0;i<currProgramInfoList.size();i++) {
                                if (currProgramInfoList.get(i).getType() == ProgramInfo.PROGRAM_TV) {
                                    programInfoTmp = currProgramInfoList.get(i);
                                    LogUtils.d("Get programInfoTmp, Lcn = "+programInfoTmp.getLCN());
                                    break;
                                }
                            }
                        }
                        //LogUtils.d("PreCh LCN = "+programInfo.getLCN()+" Name = "+programInfo.getChName_chi());
                        //LogUtils.d("PostCh LCN = "+programInfoTmp.getLCN()+" Name = "+programInfoTmp.getChName_chi());

//                        mDataManager.DataManagerSaveData(DataManager.SV_GPOS);
//                        mDataManager.DataManagerSaveData(DataManager.SV_TP_INFO);
//                        mDataManager.DataManagerSaveData(DataManager.SV_PROGRAM_INFO);
//                        mDataManager.DataManagerSaveData(DataManager.SV_FAV_INFO);

                        if(need_notify_ui){
                            LogUtils.e("SI_TBC Need Notify UI to get new channel list");
                            mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_CHANNEL, 1, 0, null);
                        }
                        if (need_to_setchannel == true) {
                            LogUtils.e("SI_TBC Need change channel");
                            mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_SET_1ST_CHANNEL, 0, 1, programInfoTmp);
                        }
                        else {
                            LogUtils.d(" isE301 "+isE301);
                            if(isE301){
                                LogUtils.e("SI_TBC, unblock E301, set Current channel");
                                isE301 = false;
                                mCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_SET_CHANNEL, 0, 1, programInfoTmp);
                            }else {
                                LogUtils.d("SI_TBC Don't need change channel");
                            }
                        }
                        mDataManager.getGposInfo().setBatId(Pvcfg.getBatId());
                    }
                    check_allupdate_flag = 2;
                }
                reset_si_table(nit, NitData, sdt, SdtDataList, bat, batData);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return update_flag;
        }
    }

    private void reset_si_table(Nit nit, NitData nitData, Sdt sdt, List<SdtData> sdtDataList, Bat bat, BatData batData){
        if (nitData != null) {
            if(nit != null) {
                nit.abort();
                nit = null;
            }
            nitData = null;
        }
        if (sdtDataList != null) {
            if(sdt != null){
                sdt.abort();
                sdt = null;
            }

            sdtDataList.clear();
            sdtDataList = null;
        }
        if (batData != null) {
            if(bat != null) {
                bat.abort();
                bat = null;
            }
            batData = null;
        }
    }
}
