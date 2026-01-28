package com.prime.dtv.service.Test;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.datamanager.DefaultValue;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.service.database.dvbdatabasetable.BookInfoDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.FavGroupNameDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.FavInfoDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.GposDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.ProgramDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.SatInfoDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.TpInfoDatabaseTable;
import com.prime.dtv.service.Scan.Scan;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.FavGroupName;
import com.prime.dtv.sysdata.FavInfo;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.SatInfo;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.utils.TVScanParams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class DataManagerTest {
    private static final String TAG = "DataManagerTest";
    private final Semaphore mSemaphore;
    private DataManager mDataManager;
    private Context mContext;
    GposInfo mGposInfo = new GposInfo();
    private List<ProgramInfo> mProgramInfoList = new ArrayList<>();
    private List<BookInfo> mBookInfoList = new ArrayList<>();
    public List<FavGroup> mFavGroupList = new ArrayList<>();
    public List<SatInfo> mSatInfoList = new ArrayList<>();
    public List<TpInfo> mTpInfoList = new ArrayList<>();
    Thread mDataManagerTestThread;

    public class DataManagerTestRunnable implements Runnable {
        @Override
        public void run() {
            try {
                mSemaphore.acquire();
                //Log.d(TAG, "Database path = "+mContext.getFilesDir().getAbsolutePath());
                Log.d(TAG, "@@@@@@@@@@@@ DataManagerTest Start @@@@@@@@@@@@");
                AllTest();
                mDataManager.DataManagerSaveData(mDataManager.SV_RESET_DEFAULT);
                Log.d(TAG, "@@@@@@@@@@@@ DataManagerTest End @@@@@@@@@@@@");

            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                mSemaphore.release();
            }
        }
    }

    public DataManagerTest(Context context) {
        mDataManager = DataManager.getDataManager(context);
        mContext=context;
        mSemaphore = new Semaphore(1);

    }

    public void DataManagerTestStart() {
        //mSaveDataFlag=falg;
        mDataManagerTestThread = new Thread(new DataManagerTestRunnable(),"111");
        mDataManagerTestThread.start();
    }

    public void AllTest() {
        int TUNR_TYPE = TpInfo.DVBC;
        int i,j;
        GposInfo gposInfo_tmp;
        List<SatInfo> SatInfoList;
        List<TpInfo> TpInfoList;
        List<ProgramInfo> programInfoList_tmp;
        List<BookInfo> bookInfoList_tmp;
        List<FavGroup> favGroupList_tmp;
        List<SatInfo> satInfoList_tmp;
        List<TpInfo> tpInfoList_tmp;
        Gson gson;
        mDataManager.DataManagerSaveData(mDataManager.SV_RESET_DEFAULT);
        try {
            sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "========== Gpos Test Start ==========");
        copyGpos(mGposInfo);
        changeGpos(TUNR_TYPE);
        mDataManager.DataManagerSaveData(mDataManager.SV_GPOS);

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        gposInfo_tmp = new GposDatabaseTable(mContext).load();
        gson = new Gson();
        String mDataManager_mGposInfo_String = gson.toJson(mDataManager.mGposInfo);
        gson = new Gson();
        String gposInfo_tmp_String = gson.toJson(gposInfo_tmp);
        gson = new Gson();
        String mGposInfo_string = gson.toJson(mGposInfo);
        if((mGposInfo_string.equals(mDataManager_mGposInfo_String) == false) && (gposInfo_tmp_String.equals(mDataManager_mGposInfo_String) == true))
            Log.d(TAG, "GposInfo Test is Success");
        else
            Log.d(TAG, "GposInfo Test is Failure");
        Log.d(TAG, "========== Gpos Test End ==========");

        Log.d(TAG, "========== ProgramInfoList Test Start ==========");
        TpInfo tpInfo = new TpInfo(TpInfo.DVBC);
        tpInfo.setTpId(0);
        tpInfo.CableTp.setFreq(351000);
        tpInfo.CableTp.setSymbol(5217);
        tpInfo.CableTp.setQam(TpInfo.Cable.QAM_256);
        TVScanParams scanData = new TVScanParams(0, tpInfo, 0, 1, 0, 0, 0, 0);
        Scan scan = new Scan(mContext, scanData,null);
        scan.startScan();

        try {
            sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scan.AddToDataManager(true);
        copyProgramInfoList(mProgramInfoList);
        changeProgramInfoList(mDataManager.mProgramInfoList);
        mDataManager.DataManagerSaveData(mDataManager.SV_PROGRAM_INFO);

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        programInfoList_tmp=new ArrayList<>();
        programInfoList_tmp=new ProgramDatabaseTable(mContext).load();
        String mDataManager_mProgramInfoList_String = gson.toJson(mDataManager.mProgramInfoList);
        gson = new Gson();
        String programInfoList_tmp_String = gson.toJson(programInfoList_tmp);
        gson = new Gson();
        String mProgramInfoList_string = gson.toJson(mProgramInfoList);
        if((mProgramInfoList_string.equals(mDataManager_mProgramInfoList_String) == false) && (programInfoList_tmp_String.equals(mDataManager_mProgramInfoList_String) == true))
            Log.d(TAG, "ProgramInfoList Test is Success");
        else
            Log.d(TAG, "ProgramInfoList Test is Failure");
        Log.d(TAG, "========== ProgramInfoList Test End ==========");

        Log.d(TAG, "========== BookInfoList Test Start ==========");
        initBookInfoList(mProgramInfoList,mBookInfoList);
        initBookInfoList(mProgramInfoList,mDataManager.mBookInfoList);
        gson = new Gson();
        String mDataManager_mBookInfoList_String = gson.toJson(mDataManager.mBookInfoList);
        gson = new Gson();
        String mBookInfoList_String = gson.toJson(mBookInfoList);
        if(mBookInfoList_String.equals(mDataManager_mBookInfoList_String) == true) {
            changeBookInfoList(mDataManager.mBookInfoList);
            mDataManager.DataManagerSaveData(mDataManager.SV_BOOK_INFO);

            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            bookInfoList_tmp = new ArrayList<>();
            bookInfoList_tmp = new BookInfoDatabaseTable(mContext).load();
            gson = new Gson();
            mDataManager_mBookInfoList_String = gson.toJson(mDataManager.mBookInfoList);
            gson = new Gson();
            String bookInfoList_tmp_String = gson.toJson(bookInfoList_tmp);
            if((bookInfoList_tmp_String.equals(mBookInfoList_String) == false) && (bookInfoList_tmp_String.equals(mDataManager_mBookInfoList_String) == true))
                Log.d(TAG, "BookInfoList Test is Success");
            else
                Log.d(TAG, "BookInfoList Test is Failure");
        }
        else {
            Log.d(TAG, "BookInfoList Test is Failure");
        }
        Log.d(TAG, "========== BookInfoList Test End ==========");

        Log.d(TAG, "========== FavGroupList Test Start ==========");
        initFavGroupList(mProgramInfoList,mDataManager.mFavGroupList);
        initFavGroupList(mProgramInfoList,mFavGroupList);
        gson = new Gson();
        String mDataManager_mFavGroupList_String = gson.toJson(mDataManager.mFavGroupList);
        gson = new Gson();
        String mFavGroupList_String = gson.toJson(mFavGroupList);
        if(mFavGroupList_String.equals(mDataManager_mFavGroupList_String) == true) {
            changeFavGroupList(mDataManager.mFavGroupList);
            mDataManager.DataManagerSaveData(mDataManager.SV_FAV_INFO);

            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            favGroupList_tmp = new ArrayList<>();
            List<FavGroupName> favGroupNameList=new FavGroupNameDatabaseTable(mContext).load();
            if(favGroupNameList == null){
                Log.d(TAG, "mFavGroupList Test is Failure");
            }
            else {
                for(FavGroupName favGroupName : favGroupNameList) {
                    favGroupList_tmp.add(new FavGroup(favGroupName));
                }
                for(FavGroup tmp : favGroupList_tmp) {
                    List<FavInfo> favInfoList = new FavInfoDatabaseTable(mContext).loadByGroupType(tmp.getFavGroupName().getGroupType());
                    if(favInfoList != null)
                        favGroupList_tmp.get(tmp.getFavGroupName().getGroupType()).addFavInfoList(favInfoList);
                }
                gson = new Gson();
                mDataManager_mFavGroupList_String = gson.toJson(mDataManager.mFavGroupList);
                gson = new Gson();
                mFavGroupList_String = gson.toJson(mFavGroupList);
                gson = new Gson();
                String favGroupList_tmp_String = gson.toJson(favGroupList_tmp);
                if((favGroupList_tmp_String.equals(mDataManager_mFavGroupList_String) == true) && (favGroupList_tmp_String.equals(mFavGroupList_String) == false)) {
                    Log.d(TAG, "FavGroupList Test is Success");
                }
                else {
                    Log.d(TAG, "FavGroupList Test is Failure");
                }
            }
        }
        else {
            Log.d(TAG, "mFavGroupList Test is Failure");
        }
        Log.d(TAG, "========== FavGroupList Test End ==========");

        Log.d(TAG, "========== SatInfoList Test Start==========");
        copySatInfoList(mSatInfoList,TUNR_TYPE);
        changeSatInfoList(TUNR_TYPE);
        mDataManager.DataManagerSaveData(mDataManager.SV_SAT_INFO);

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        satInfoList_tmp=new SatInfoDatabaseTable(mContext).load();
        gson = new Gson();
        String mDataManager_mSatInfoList_String = gson.toJson(mDataManager.mSatInfoList);
        gson = new Gson();
        String mSatInfoList_String = gson.toJson(mSatInfoList);
        gson = new Gson();
        String satInfoList_tmp_String = gson.toJson(satInfoList_tmp);
        if((satInfoList_tmp_String.equals(mDataManager_mSatInfoList_String) == true) && (satInfoList_tmp_String.equals(mSatInfoList_String) == false)) {
            Log.d(TAG, "SatInfoList Test is Success");
        }
        else {
            Log.d(TAG, "SatInfoList Test is Failure");
        }
        Log.d(TAG, "========== SatInfoList Test End==========");

        Log.d(TAG, "========== TpInfoList Test Start==========");
        copyTpInfoList(mTpInfoList,TUNR_TYPE);
        changeTpInfoList(TUNR_TYPE);
        mDataManager.DataManagerSaveData(mDataManager.SV_TP_INFO);

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        tpInfoList_tmp=new TpInfoDatabaseTable(mContext).load();
        gson = new Gson();
        String mDataManager_mTpInfoList_String = gson.toJson(mDataManager.mTpInfoList);
        gson = new Gson();
        String mTpInfoList_String = gson.toJson(mTpInfoList);
        gson = new Gson();
        String tpInfoList_tmp_String = gson.toJson(tpInfoList_tmp);
        if((tpInfoList_tmp_String.equals(mDataManager_mTpInfoList_String) == true) && (tpInfoList_tmp_String.equals(mTpInfoList_String) == false)) {
            Log.d(TAG, "TpInfoList Test is Success");
        }
        else {
            Log.d(TAG, "TpInfoList Test is Failure");
        }
        Log.d(TAG, "========== TpInfoList Test End==========");

        mDataManager.DataManagerSaveData(mDataManager.SV_RESET_DEFAULT);

        try {
            sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void copyGpos(GposInfo gposInfo) {
        gposInfo.setDBVersion(mDataManager.mGposInfo.getDBVersion());
        gposInfo.setCurChannelId(mDataManager.mGposInfo.getCurChannelId());
        gposInfo.setCurGroupType(mDataManager.mGposInfo.getCurGroupType());
        gposInfo.setPasswordValue(mDataManager.mGposInfo.getPasswordValue());
        gposInfo.setParentalRate(mDataManager.mGposInfo.getParentalRate());
        gposInfo.setParentalLockOnOff(mDataManager.mGposInfo.getParentalLockOnOff());
        gposInfo.setInstallLockOnOff(mDataManager.mGposInfo.getInstallLockOnOff());
        gposInfo.setBoxPowerStatus(mDataManager.mGposInfo.getBoxPowerStatus());
        gposInfo.setStartOnChannelId(mDataManager.mGposInfo.getStartOnChannelId());
        gposInfo.setStartOnChType(mDataManager.mGposInfo.getStartOnChType());
        gposInfo.setVolume(mDataManager.mGposInfo.getVolume());
        gposInfo.setAudioTrackMode(mDataManager.mGposInfo.getAudioTrackMode());
        gposInfo.setAutoRegionTimeOffset(mDataManager.mGposInfo.getAutoRegionTimeOffset());
        gposInfo.setRegionTimeOffset(mDataManager.mGposInfo.getRegionTimeOffset());
        gposInfo.setRegionSummerTime(mDataManager.mGposInfo.getRegionSummerTime());
        gposInfo.setLnbPower(mDataManager.mGposInfo.getLnbPower());
        gposInfo.setScreen16x9(mDataManager.mGposInfo.getScreen16x9());
        gposInfo.setConversion(mDataManager.mGposInfo.getConversion());
        gposInfo.setOSDLanguage(mDataManager.mGposInfo.getOSDLanguage());
        gposInfo.setSearchProgramType(mDataManager.mGposInfo.getSearchProgramType());
        gposInfo.setSearchMode(mDataManager.mGposInfo.getSearchMode());
        gposInfo.setAudioLanguageSelection(0,mDataManager.mGposInfo.getAudioLanguageSelection(0));
        gposInfo.setAudioLanguageSelection(1,mDataManager.mGposInfo.getAudioLanguageSelection(1));
        gposInfo.setSubtitleLanguageSelection(0,mDataManager.mGposInfo.getSubtitleLanguageSelection(0));
        gposInfo.setSubtitleLanguageSelection(1,mDataManager.mGposInfo.getSubtitleLanguageSelection(1));
        gposInfo.setSortByLcn(mDataManager.mGposInfo.getSortByLcn());
        gposInfo.setOSDTransparency(mDataManager.mGposInfo.getOSDTransparency());
        gposInfo.setBannerTimeout(mDataManager.mGposInfo.getBannerTimeout());
        gposInfo.setHardHearing(mDataManager.mGposInfo.getHardHearing());
        gposInfo.setAutoStandbyTime(mDataManager.mGposInfo.getAutoStandbyTime());
        gposInfo.setDolbyMode(mDataManager.mGposInfo.getDolbyMode());
        gposInfo.setHDCPOnOff(mDataManager.mGposInfo.getHDCPOnOff());
        gposInfo.setDeepSleepMode(mDataManager.mGposInfo.getDeepSleepMode());
        gposInfo.setSubtitleOnOff(mDataManager.mGposInfo.getSubtitleOnOff());
        gposInfo.setAvStopMode(mDataManager.mGposInfo.getAvStopMode());
        gposInfo.setTimeshiftDuration(mDataManager.mGposInfo.getTimeshiftDuration());
        gposInfo.setRecordIconOnOff(mDataManager.mGposInfo.getRecordIconOnOff());
    }

    private void changeGpos(int tunerType) {
        String dbver = "";
        if(tunerType == TpInfo.DVBC)
            dbver = "C";
        else if(tunerType == TpInfo.DVBT)
            dbver = "T";
        else if(tunerType == TpInfo.ISDBT)
            dbver = "I";
        else if(tunerType == TpInfo.DVBS)
            dbver = "S";
        mDataManager.mGposInfo.setDBVersion(dbver+"1.0");
        mDataManager.mGposInfo.setCurChannelId(mDataManager.mGposInfo.getCurChannelId()+1);
        mDataManager.mGposInfo.setCurGroupType(mDataManager.mGposInfo.getCurGroupType()+1);
        mDataManager.mGposInfo.setPasswordValue(mDataManager.mGposInfo.getPasswordValue()+1);
        mDataManager.mGposInfo.setParentalRate(mDataManager.mGposInfo.getParentalRate()+1);
        mDataManager.mGposInfo.setParentalLockOnOff(~(mDataManager.mGposInfo.getParentalLockOnOff()) & 1);
        mDataManager.mGposInfo.setInstallLockOnOff(~(mDataManager.mGposInfo.getInstallLockOnOff()) & 1);
        mDataManager.mGposInfo.setBoxPowerStatus(~(mDataManager.mGposInfo.getBoxPowerStatus()) & 1);
        mDataManager.mGposInfo.setStartOnChannelId(mDataManager.mGposInfo.getStartOnChannelId()+1);
        mDataManager.mGposInfo.setStartOnChType(~(mDataManager.mGposInfo.getStartOnChType()) & 1);
        mDataManager.mGposInfo.setVolume(mDataManager.mGposInfo.getVolume()+1);
        mDataManager.mGposInfo.setAudioTrackMode(~(mDataManager.mGposInfo.getAudioTrackMode()) & 1);
        mDataManager.mGposInfo.setAutoRegionTimeOffset(mDataManager.mGposInfo.getAutoRegionTimeOffset()+1);
        mDataManager.mGposInfo.setRegionTimeOffset(mDataManager.mGposInfo.getRegionTimeOffset()+1);
        mDataManager.mGposInfo.setRegionSummerTime(mDataManager.mGposInfo.getRegionSummerTime()+1);
        mDataManager.mGposInfo.setLnbPower(~(mDataManager.mGposInfo.getLnbPower()) & 1);
        mDataManager.mGposInfo.setScreen16x9(~(mDataManager.mGposInfo.getScreen16x9()) & 1);
        mDataManager.mGposInfo.setConversion(~(mDataManager.mGposInfo.getConversion()) & 1);
        mDataManager.mGposInfo.setOSDLanguage("cht");
        mDataManager.mGposInfo.setSearchProgramType(TVScanParams.SEARCH_OPTION_TV_ONLY);
        mDataManager.mGposInfo.setSearchMode(TVScanParams.SEARCH_OPTION_TV_ONLY);
        mDataManager.mGposInfo.setAudioLanguageSelection(0,"cht");
        mDataManager.mGposInfo.setAudioLanguageSelection(1,"cht");
        mDataManager.mGposInfo.setSubtitleLanguageSelection(0,"cht");
        mDataManager.mGposInfo.setSubtitleLanguageSelection(1,"cht");
        mDataManager.mGposInfo.setSortByLcn(~(mDataManager.mGposInfo.getSortByLcn() & 1));
        mDataManager.mGposInfo.setOSDTransparency(~(mDataManager.mGposInfo.getOSDTransparency()) & 1);
        mDataManager.mGposInfo.setBannerTimeout(mDataManager.mGposInfo.getBannerTimeout()+1);
        mDataManager.mGposInfo.setHardHearing(~(mDataManager.mGposInfo.getHardHearing()) & 1);
        mDataManager.mGposInfo.setAutoStandbyTime(mDataManager.mGposInfo.getAutoStandbyTime()+10);
        mDataManager.mGposInfo.setDolbyMode(~(mDataManager.mGposInfo.getDolbyMode()) & 1);
        mDataManager.mGposInfo.setHDCPOnOff(~(mDataManager.mGposInfo.getHDCPOnOff()) & 1);
        mDataManager.mGposInfo.setDeepSleepMode(~(mDataManager.mGposInfo.getDeepSleepMode()) & 1);
        mDataManager.mGposInfo.setSubtitleOnOff(~(mDataManager.mGposInfo.getSubtitleOnOff()) & 1);
        mDataManager.mGposInfo.setAvStopMode(~(mDataManager.mGposInfo.getAvStopMode()) & 1);
        mDataManager.mGposInfo.setTimeshiftDuration(mDataManager.mGposInfo.getTimeshiftDuration()+10);
        mDataManager.mGposInfo.setRecordIconOnOff(~(mDataManager.mGposInfo.getRecordIconOnOff()) & 1);
    }

    private void changeSatInfoList(int tunerType) {
        int i, j, satId;
        List<Integer> tps = new ArrayList<>();
            if (tunerType == TpInfo.DVBC){
                for (i = (mDataManager.mSatInfoList.size() - 1); i > 0; i--) {
                    mDataManager.mSatInfoList.remove(i);
                }
            satId = 0;
            SatInfo satInfo = new SatInfo(satId, "DVBC_SAT", TpInfo.DVBC, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
            mDataManager.mSatInfoList.get(i).update(satInfo);
            for (j = 1; j <= 30; j++) {
                tps.add(j);
            }
            mDataManager.mSatInfoList.get(satId).setTps(tps);
            mDataManager.mSatInfoList.get(satId).setTpNum(j - 1);
        }
    }

    private void changeTpInfoList(int tunerType) {
        int i,satId,freq;
        for(i=(mDataManager.mTpInfoList.size()-1);i>=30;i--) {
            mDataManager.mTpInfoList.remove(i);
        }
        freq=285000;
        for(i = 0 ; i < mDataManager.mTpInfoList.size() ; i++) {
            TpInfo tpInfo;
            satId=(~(mDataManager.mTpInfoList.get(i).getSatId()) & 1);
            tpInfo= new TpInfo(tunerType, i, satId, i+1, freq, 5200, TpInfo.Cable.QAM_AUTO);
            tpInfo.setTuner_id(~(mDataManager.mTpInfoList.get(i).getTuner_id()) & 1);
            tpInfo.setNetwork_id(~(mDataManager.mTpInfoList.get(i).getNetwork_id()) & 1);
            tpInfo.setTransport_id(~(mDataManager.mTpInfoList.get(i).getTransport_id()) & 1);
            tpInfo.setOrignal_network_id(~(mDataManager.mTpInfoList.get(i).getOrignal_network_id()) & 1);
            mDataManager.mTpInfoList.get(i).update(tpInfo);
            freq=freq+6000;
        }

    }

    private void copyProgramInfoList(List<ProgramInfo> programInfoList)
    {
        ProgramInfo programInfo;
        if(programInfoList.size() > 0) {
            for(int i = programInfoList.size(); i >=0 ;i--) {
                programInfoList.remove(i);
            }
        }
        for(int i = 0; i < mDataManager.mProgramInfoList.size(); i++) {
            programInfo = new ProgramInfo();
            programInfo.setChannelId(mDataManager.mProgramInfoList.get(i).getChannelId());
            programInfo.setServiceId(mDataManager.mProgramInfoList.get(i).getServiceId());
            programInfo.setType(mDataManager.mProgramInfoList.get(i).getType());
            programInfo.setDisplayNum(mDataManager.mProgramInfoList.get(i).getDisplayNum());
            programInfo.setDisplayName(mDataManager.mProgramInfoList.get(i).getDisplayName());
            programInfo.setLock(mDataManager.mProgramInfoList.get(i).getLock());
            programInfo.setSkip(mDataManager.mProgramInfoList.get(i).getSkip());
            programInfo.setPvrSkip(mDataManager.mProgramInfoList.get(i).getPvrSkip());
            programInfo.setCA(mDataManager.mProgramInfoList.get(i).getCA());
            programInfo.setTpId(mDataManager.mProgramInfoList.get(i).getTpId());
            programInfo.setSatId(mDataManager.mProgramInfoList.get(i).getSatId());
            programInfo.setPcr(mDataManager.mProgramInfoList.get(i).getPcr());
            programInfo.setTransportStreamId(mDataManager.mProgramInfoList.get(i).getTransportStreamId());
            programInfo.setOriginalNetworkId(mDataManager.mProgramInfoList.get(i).getOriginalNetworkId());
            programInfo.setAudioLRSelected(mDataManager.mProgramInfoList.get(i).getAudioLRSelected());
            programInfo.setAudioSelected(mDataManager.mProgramInfoList.get(i).getAudioSelected());
            programInfo.setTunerId(mDataManager.mProgramInfoList.get(i).getTunerId());
            programInfo.setParentalRating(mDataManager.mProgramInfoList.get(i).getParentalRating());
            programInfo.setLCN(mDataManager.mProgramInfoList.get(i).getLCN());
            programInfo.setQuality(mDataManager.mProgramInfoList.get(i).getQuality());
            programInfo.setParentCountryCode(mDataManager.mProgramInfoList.get(i).getParentCountryCode());
            programInfo.pVideo.setPID(mDataManager.mProgramInfoList.get(i).pVideo.getPID());
            programInfo.pVideo.setCodec(mDataManager.mProgramInfoList.get(i).pVideo.getCodec());
            for(int j = 0; j < mDataManager.mProgramInfoList.get(i).pVideo.CaInfoList.size(); j++){
                ProgramInfo.CaInfo caInfo = new ProgramInfo.CaInfo();
                caInfo.setEcmPid(mDataManager.mProgramInfoList.get(i).pVideo.CaInfoList.get(j).getEcmPid());
                caInfo.setCaSystemId(mDataManager.mProgramInfoList.get(i).pVideo.CaInfoList.get(j).getCaSystemId());
                programInfo.pVideo.CaInfoList.add(caInfo);
            }
            for(int j = 0; j < mDataManager.mProgramInfoList.get(i).pAudios.size(); j++){
                ProgramInfo.AudioInfo audioInfo = new ProgramInfo.AudioInfo(0,0,null,null);
                audioInfo.setPid(mDataManager.mProgramInfoList.get(i).pAudios.get(j).getPid());
                audioInfo.setCodec(mDataManager.mProgramInfoList.get(i).pAudios.get(j).getCodec());
                audioInfo.setRightIsoLang(mDataManager.mProgramInfoList.get(i).pAudios.get(j).getRightIsoLang());
                audioInfo.setLeftIsoLang(mDataManager.mProgramInfoList.get(i).pAudios.get(j).getLeftIsoLang());
                for(int k = 0; k < mDataManager.mProgramInfoList.get(i).pAudios.get(j).CaInfoList.size(); k++)
                {
                    ProgramInfo.CaInfo caInfo = new ProgramInfo.CaInfo();
                    caInfo.setEcmPid(mDataManager.mProgramInfoList.get(i).pAudios.get(j).CaInfoList.get(k).getEcmPid());
                    caInfo.setCaSystemId(mDataManager.mProgramInfoList.get(i).pAudios.get(j).CaInfoList.get(k).getCaSystemId());
                    audioInfo.CaInfoList.add(caInfo);
                }
                programInfo.pAudios.add(audioInfo);
            }
            for(int j = 0; j < mDataManager.mProgramInfoList.get(i).pSubtitle.size(); j++){
                ProgramInfo.SubtitleInfo subtitleInfo = new ProgramInfo.SubtitleInfo(0,0,null,0,0);
                subtitleInfo.setType(mDataManager.mProgramInfoList.get(i).pSubtitle.get(j).getType());
                subtitleInfo.setPid(mDataManager.mProgramInfoList.get(i).pSubtitle.get(j).getPid());
                subtitleInfo.setComPageId(mDataManager.mProgramInfoList.get(i).pSubtitle.get(j).getComPageId());
                subtitleInfo.setAncPageId(mDataManager.mProgramInfoList.get(i).pSubtitle.get(j).getAncPageId());
                subtitleInfo.setLang(mDataManager.mProgramInfoList.get(i).pSubtitle.get(j).getLang());
                programInfo.pSubtitle.add(subtitleInfo);
            }
            for(int j = 0; j < mDataManager.mProgramInfoList.get(i).pTeletext.size(); j++){
                ProgramInfo.TeletextInfo teletextInfo = new ProgramInfo.TeletextInfo(0,0,null,0,0);
                teletextInfo.setPid(mDataManager.mProgramInfoList.get(i).pTeletext.get(j).getPid());
                teletextInfo.setType(mDataManager.mProgramInfoList.get(i).pTeletext.get(j).getType());
                teletextInfo.setMagazineNum(mDataManager.mProgramInfoList.get(i).pTeletext.get(j).getMagazineNum());
                teletextInfo.setPageNum(mDataManager.mProgramInfoList.get(i).pTeletext.get(j).getPageNum());
                teletextInfo.setLang(mDataManager.mProgramInfoList.get(i).pTeletext.get(j).getLang());
                programInfo.pTeletext.add(teletextInfo);
            }
            programInfoList.add(programInfo);
        }
    }

    private void changeProgramInfoList(List<ProgramInfo> programInfolist) {
        for (int i = 0; i < programInfolist.size(); i++){
            programInfolist.get(i).setChannelId(programInfolist.get(i).getChannelId()+100);
            programInfolist.get(i).setServiceId(programInfolist.get(i).getServiceId()+100);
            programInfolist.get(i).setTransportStreamId(programInfolist.get(i).getTransportStreamId()+100);
            programInfolist.get(i).setOriginalNetworkId(programInfolist.get(i).getOriginalNetworkId()+100);
            programInfolist.get(i).setLock(programInfolist.get(i).getLock()+1);
            programInfolist.get(i).setSkip(programInfolist.get(i).getSkip()+1);
            //programInfolist.get(i).setPvrSkip(programInfolist.get(i).getPvrSkip()+1); //Because the data is not stored in the database
            programInfolist.get(i).setCA(programInfolist.get(i).getCA()+1);
            programInfolist.get(i).setTpId(programInfolist.get(i).getTpId()+1);
            programInfolist.get(i).setSatId(programInfolist.get(i).getSatId()+1);
            programInfolist.get(i).setAudioLRSelected(programInfolist.get(i).getAudioLRSelected()+1);
            programInfolist.get(i).setAudioSelected(programInfolist.get(i).getAudioSelected()+1);
            programInfolist.get(i).setTunerId(programInfolist.get(i).getTunerId()+1);
            programInfolist.get(i).setParentalRating(programInfolist.get(i).getParentalRating()+1);
            //programInfolist.get(i).setLCN(programInfolist.get(i).getLCN()+1); //Because the data is not stored in the database
            //programInfolist.get(i).setQuality(programInfolist.get(i).getQuality()+1); //Because the data is not stored in the database
            programInfolist.get(i).setDisplayName("DisplayName test");
            programInfolist.get(i).setParentCountryCode("cht");
            programInfolist.get(i).pVideo.setPID(0x1fff);
            programInfolist.get(i).pVideo.setCodec(0xff);
            for (int j = 0; j < programInfolist.get(i).pAudios.size();j++){
                programInfolist.get(i).pAudios.get(j).setPid(0x1fff);
                programInfolist.get(i).pAudios.get(j).setCodec(0xff);
            }
            for (int j = 0; j < programInfolist.get(i).pTeletext.size();j++){
                programInfolist.get(i).pTeletext.get(j).setPid(0x1fff);
                programInfolist.get(i).pTeletext.get(j).setLang("cht");
            }
            for (int j = 0; j < programInfolist.get(i).pSubtitle.size();j++){
                programInfolist.get(i).pSubtitle.get(j).setPid(0x1fff);
                programInfolist.get(i).pSubtitle.get(j).setLang("cht");
            }
        }
    }

    private void initBookInfoList(List<ProgramInfo> programInfoList,List<BookInfo> bookInfoList){
        BookInfo bookInfo;
        for(int i=0;i<programInfoList.size();i++) {
            bookInfo = new BookInfo();
            bookInfo.setBookId(i+1);
            bookInfo.setEnable(1);
            bookInfo.setChannelId(mProgramInfoList.get(i).getChannelId());
            bookInfo.setGroupType(0);
            bookInfo.setEventName(mProgramInfoList.get(i).getDisplayName());
            bookInfo.setBookType(0);
            bookInfo.setBookCycle(0);
            bookInfo.setYear(2021);
            bookInfo.setMonth(1);
            bookInfo.setDate(1+i);
            bookInfo.setWeek(1); //BOOK_WEEK_DAY_SUNDAY
            bookInfo.setStartTime(123456789+i);
            bookInfo.setDuration(30);
            bookInfoList.add(bookInfo);
        }
    }

    private void changeBookInfoList(List<BookInfo> bookInfoList){
        for(int i=0;i<bookInfoList.size();i++) {
            bookInfoList.get(i).setBookId(bookInfoList.size()+1);
            bookInfoList.get(i).setEnable(bookInfoList.get(i).getEnable());
            bookInfoList.get(i).setChannelId(bookInfoList.get(i).getChannelId()+100);
            bookInfoList.get(i).setGroupType(bookInfoList.get(i).getGroupType()+1);
            bookInfoList.get(i).setEventName("This is test");
            bookInfoList.get(i).setBookType(bookInfoList.get(i).getBookType()+1);
            bookInfoList.get(i).setBookCycle(bookInfoList.get(i).getBookCycle()+1);
            bookInfoList.get(i).setYear(bookInfoList.get(i).getYear()+1);
            bookInfoList.get(i).setMonth(bookInfoList.get(i).getMonth()+1);
            bookInfoList.get(i).setDate(bookInfoList.get(i).getDate()+1);
            bookInfoList.get(i).setWeek(bookInfoList.get(i).getWeek()+1); //BOOK_WEEK_DAY_SUNDAY
            bookInfoList.get(i).setStartTime(bookInfoList.get(i).getStartTime()+1000);
            bookInfoList.get(i).setDuration(bookInfoList.get(i).getDuration()*2);
        }
    }

    private void initFavGroupList(List<ProgramInfo> programInfoList,List<FavGroup> favGroupList){
        FavInfo favInfo;
        List<FavInfo> favInfoList;
        int TUNR_TYPE = TpInfo.DVBC;
        if(favGroupList.size() == 0) {
            DefaultValue defaultValue = new DefaultValue(TUNR_TYPE);
            for (FavGroupName favGroupName : defaultValue.favGroupNameList) {
                favGroupList.add(new FavGroup(favGroupName));
            }
        }
        for(int i=0;i<favGroupList.size();i++) {
            favInfoList=new ArrayList<>();
            for(int j=0;j<programInfoList.size();j++) {
                favInfo = new FavInfo();
                favInfo.setFavNum(j+1);
                favInfo.setChannelId(programInfoList.get(j).getChannelId());
                favInfo.setFavMode(favGroupList.get(i).getFavGroupName().getGroupType());
                favInfoList.add(favInfo);
            }
            favGroupList.get(i).addFavInfoList(favInfoList);
        }
    }

    private void changeFavGroupList(List<FavGroup> favGroupList){
        for(int i=0;i<favGroupList.size();i++) {
            favGroupList.get(i).getFavGroupName().setGroupName("Group Test");
            for(int j=0;j<favGroupList.get(i).getFavInfoList().size();j++) {
                favGroupList.get(i).getFavInfoList().get(j).setChannelId(favGroupList.get(i).getFavInfoList().get(j).getChannelId()+100);
            }
        }
    }

    private void copySatInfoList(List<SatInfo> satInfoList,int tunerType) {
        if(tunerType == TpInfo.DVBC)
        {
            for(int i=0;i<mDataManager.mSatInfoList.size();i++) {
                SatInfo satInfo = new SatInfo(mDataManager.mSatInfoList.get(i).getSatId(),mDataManager.mSatInfoList.get(i).getSatName(),tunerType,
                        mDataManager.mTpInfoList.size(),mDataManager.mSatInfoList.get(i).getAngle(),mDataManager.mSatInfoList.get(i).getLocation(),
                        mDataManager.mSatInfoList.get(i).getPostionIndex(),mDataManager.mSatInfoList.get(i).getAngleEW(),
                        mDataManager.mSatInfoList.get(i).Antenna.getLnb1(),mDataManager.mSatInfoList.get(i).Antenna.getLnb2(),
                        mDataManager.mSatInfoList.get(i).Antenna.getLnbType(),mDataManager.mSatInfoList.get(i).Antenna.getDiseqcType(),
                        mDataManager.mSatInfoList.get(i).Antenna.getDiseqc(),mDataManager.mSatInfoList.get(i).Antenna.getTone22kUse(),
                        mDataManager.mSatInfoList.get(i).Antenna.getTone22k(),mDataManager.mSatInfoList.get(i).Antenna.getV1418Use(),
                        mDataManager.mSatInfoList.get(i).Antenna.getV1418(),mDataManager.mSatInfoList.get(i).Antenna.getCku());
                List<Integer> tps = new ArrayList<>();
                for(int j = 0 ; j < mDataManager.mTpInfoList.size() ; j++) {
                    tps.add(j);
                }
                satInfo.setTps(tps);
                satInfoList.add(satInfo);
             }
        }
    }

    private void copyTpInfoList(List<TpInfo> tpInfoList,int tunerType) {

        if(tunerType == TpInfo.DVBC)
        {
            for(int i=0;i<mDataManager.mTpInfoList.size();i++) {
                TpInfo tpInfo = new TpInfo(tunerType,mDataManager.mTpInfoList.get(i).getTpId(),mDataManager.mTpInfoList.get(i).getSatId(),
                        mDataManager.mTpInfoList.get(i).CableTp.getChannel(), mDataManager.mTpInfoList.get(i).CableTp.getFreq(),
                        mDataManager.mTpInfoList.get(i).CableTp.getSymbol(),mDataManager.mTpInfoList.get(i).CableTp.getQam());
                tpInfo.CableTp.setNetWork(mDataManager.mTpInfoList.get(i).CableTp.getNetWork());
                tpInfo.CableTp.setNitSearchIndex(mDataManager.mTpInfoList.get(i).CableTp.getNitSearchIndex());
                tpInfo.setTunerType(mDataManager.mTpInfoList.get(i).getTunerType());
                tpInfo.setNetwork_id(mDataManager.mTpInfoList.get(i).getNetwork_id());
                tpInfo.setTransport_id(mDataManager.mTpInfoList.get(i).getTransport_id());
                tpInfo.setOrignal_network_id(mDataManager.mTpInfoList.get(i).getOrignal_network_id());
                tpInfo.setTuner_id(mDataManager.mTpInfoList.get(i).getTuner_id());
                tpInfoList.add(tpInfo);
            }
        }
    }
}
