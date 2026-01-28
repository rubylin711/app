package com.prime.dtv.service.datamanager;


import android.content.Context;
import android.util.Log;

import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.utils.TVScanParams;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.FavGroupName;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.TpInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultValue {
    private static final String TAG = "DefaultValue";
    public List<SatInfo> satInfoList = new ArrayList<>();
    public List<TpInfo> tpInfoList = new ArrayList<>();
    public GposInfo gposInfo = new GposInfo();
    public List<FavGroupName> favGroupNameList = new ArrayList<>();

    public static int DIGITURK_BAT_ID = 0x0097;
    public static int CNS_BAT_ID = 0x623D;
    public static int SAT_SUND_DIRECT  =   1;
    public static int SAT_ASTRA        =   2;
    public static int SAT_HOTBIRD      =   3;
    public static int SAT_TURK         =   4;
    private Context mContext;
    private List<Integer>   mDefaultSatList;

    public DefaultValue(Context context, int tunerType) {
        mContext = context;
        //TpInfo tpInfo = new TpInfo(tunerType);
        if(tunerType == TpInfo.DVBS) {
            mDefaultSatList = new ArrayList<>();
            if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_SUND)) {
                mDefaultSatList.add(SAT_SUND_DIRECT);
                mDefaultSatList.add(SAT_ASTRA);
                mDefaultSatList.add(SAT_HOTBIRD);
            }
            else if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DIGITURK)) {
                mDefaultSatList.add(SAT_TURK);//centaur 20250324 add turk sat
            }
            Collections.sort(mDefaultSatList);
            if ( mDefaultSatList.size() > 0 )
                Log.d(TAG,"mDefaultSatList[0] = "+mDefaultSatList.get(0));
        }
        buildSatInfoList(tunerType);
//        buildTpInfoList(tunerType);
        buildGpos(tunerType);
        buildGroupList();
    }

//    public DefaultValue(int tunerType) {  // only use in DMG/TBC
//        if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)
//            || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC))
//        buildSatInfoList(TpInfo.DVBC);
//        buildTpInfoList(TpInfo.DVBC);
//        buildGpos(TpInfo.DVBC);
//        buildGroupList();
//    }

    private void buildSatInfoList(int tunerType) {
        if(tunerType == TpInfo.DVBC) {
            int satId = 0;
            SatInfo satInfo = new SatInfo(satId,"DVBC_SAT",TpInfo.DVBC,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
            satInfoList.add(satInfo);
            buildCableTpInfoList();
        }
        else if(tunerType == TpInfo.DVBS) {
            int startTpId = 0;
            for(int i = 0; i < mDefaultSatList.size(); i++) {
                SatelliteDefaultData satelliteDefaultData = new SatelliteDefaultData(mContext,mDefaultSatList.get(i));
                satelliteDefaultData = buildSatTpInfoList(satelliteDefaultData,i,startTpId);
                startTpId = satelliteDefaultData.mEndTpId;
                SatInfo satInfo = new SatInfo(i,satelliteDefaultData.mSatInfo.getSatName(),TpInfo.DVBS,satelliteDefaultData.mTpInfoList.size(),0,0,0,0,9750,10600,SatInfo.LNB_TYPE_UNIVERSAL,0,0,0,2,1,0,0);
                satInfo.setTps(satelliteDefaultData.mSatInfo.getTps());
                satInfo.setTpNum(satelliteDefaultData.mSatInfo.getTpNum());
                satInfoList.add(satInfo);
//                Log.d(TAG, "SatId " + satelliteDefaultData.mSatInfo.getSatId());
//                Log.d(TAG, "SatId Name " + satelliteDefaultData.mSatInfo.getSatName());
//                Log.d(TAG, "SatId Tp size " + satelliteDefaultData.mTpInfoList.size());
//                for(int j = 0; j < satelliteDefaultData.mTpInfoList.size(); j++) {
//                    Log.d(TAG, "SatId "+satelliteDefaultData.mTpInfoList.get(j).getSatId()+" TpId "+satelliteDefaultData.mTpInfoList.get(j).getTpId()+" Freq "+satelliteDefaultData.mTpInfoList.get(j).SatTp.getFreq());
//                }
            }
        }
        else if(tunerType == TpInfo.DVBT) {
            Log.d(TAG,"buildSatInfoList");
            int satId = 0;
            SatInfo satInfo = new SatInfo(satId,"DVBT_SAT",TpInfo.DVBT,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
            satInfoList.add(satInfo);
            buildTerrTpInfoList();
        }
        else if(tunerType == TpInfo.ISDBT) {
            int satId = 0;
            SatInfo satInfo = new SatInfo(satId,"ISDBT_SAT",TpInfo.ISDBT,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
            satInfoList.add(satInfo);
            buildIsdbtTpInfoList();
        }
        else {
            Log.d(TAG,"buildSatInfoList fail , unknown tuner type["+tunerType+"]");
        }
    }

    public void buildCableTpInfoList() {
        int j = 0;
        int satId = 0;
        List<Integer> tps = new ArrayList<>();

        if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_VECTRA)){
            int[][] vectraTable = {
                    {362, 6875}, {370, 6875}, {378, 6900}, {386, 6900},
                    {402, 6900}, {410, 6900}, {418, 6900}, {434, 6900},
                    {442, 6900}, {450, 6900}, {458, 6900}, {466, 6900},
                    {474, 6900}, {482, 6900}, {490, 6900}, {498, 6900},
                    {506, 6900}, {522, 6900}, {546, 6875}, {554, 6875},
                    {562, 6875}, {570, 6875}, {578, 6875}, {586, 6875},
                    {602, 6875}, {610, 6875}, {618, 6875}, {626, 6875},
                    {634, 6875}, {642, 6875}, {650, 6875}, {658, 6875},
                    {666, 6875}, {674, 6875}, {682, 6875}, {690, 6875},
                    {698, 6900}, {706, 6900}, {714, 6900}, {738, 6900},
                    {746, 6900}, {754, 6900}
            };

            for (int[] entry : vectraTable) {
                int freqKHz = entry[0] * 1000; // MHz → kHz
                int symbol = entry[1];
                int qam = TpInfo.Cable.QAM_256; // 固定為 QAM_256

                tpInfoList.add(new TpInfo(TpInfo.DVBC, j, satId, j + 1, freqKHz, symbol, qam, TpInfo.Cable.BAND_8MHZ));
                tps.add(j);
                j++;
            }
        }
        else if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_EXPRESS) ||
                Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TOP)){
            int[][] expressTable = {
                    {399, 5360}, {405, 5360}, {411, 5360}, {417, 5360},
                    {423, 5360}, {429, 5360}, {435, 5360}, {441, 5360},
                    {447, 5360}, {453, 5360}, {459, 5360}, {465, 5360},
                    {471, 5360}, {477, 5360}, {483, 5360}, {489, 5360},
                    {495, 5360}, {501, 5360}
            };

            for (int[] entry : expressTable) {
                int freqKHz = entry[0] * 1000; // MHz → kHz
                int symbol = entry[1];
                int qam = TpInfo.Cable.QAM_256; // 固定為 QAM_256

                tpInfoList.add(new TpInfo(TpInfo.DVBC, j, satId, j + 1, freqKHz, symbol, qam, TpInfo.Cable.BAND_6MHZ));
                tps.add(j);
                j++;
            }
        }
        else if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TURKSAT)){
            int[][] TurksatTable = {
                    {298, 6900}, {306, 6900}, {314, 6900}, {322, 6900}, {330, 6900}, {338, 6900},
                    {346, 6900}, {354, 6900}, {362, 6900}, {370, 6900}, {378, 6900}, {386, 6900},
                    {394, 6900}, {402, 6900}, {410, 6900}, {418, 6900}, {426, 6900}, {434, 6900},
                    {442, 6900}, {450, 6900}, {458, 6900}, {466, 6900}, {474, 6900}, {482, 6900},
                    {490, 6900}, {498, 6900}, {506, 6900}, {514, 6900}, {522, 6900}, {530, 6900}
            };

            for (int[] entry : TurksatTable) {
                int freqKHz = entry[0] * 1000; // MHz → kHz
                int symbol = entry[1];
                int qam = TpInfo.Cable.QAM_256; // 固定為 QAM_256
                tpInfoList.add(new TpInfo(TpInfo.DVBC, j, satId, j + 1, freqKHz, symbol, qam, TpInfo.Cable.BAND_8MHZ));
                tps.add(j);
                j++;
            }
        }
        else if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_BAHIAVISION)){
            int[][] bahiavisionTable = {
                    {591, 5360}, {597, 5360}, {603, 5360}, {609, 5360},
                    {615, 5360}, {621, 5360}, {627, 5360}, {633, 5360},
                    {639, 5360}, {645, 5360}, {651, 5360}, {657, 5360},
                    {663, 5360}, {669, 5360}, {675, 5360}, {681, 5360},
                    {687, 5360}, {693, 5360}, {699, 5360}, {705, 5360},
                    {711, 5360}, {717, 5360}, {723, 5360}
            };

            for (int[] entry : bahiavisionTable) {
                int freqKHz = entry[0] * 1000; // MHz → kHz
                int symbol = entry[1];
                int qam = TpInfo.Cable.QAM_256; // 固定為 QAM_256

                tpInfoList.add(new TpInfo(TpInfo.DVBC, j, satId, j + 1, freqKHz, symbol, qam, TpInfo.Cable.BAND_6MHZ));
                tps.add(j);
                j++;
            }
        }
        else if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)
            || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC)) {
            for(int i = 105000 ; i < 1005000 ; i+=6000) {
                int qam = TpInfo.Cable.QAM_256;
                int symbol = 5200;
                if(i == 615000) {
                    qam = TpInfo.Cable.QAM_64;
                    symbol = 5057;
                }
                tpInfoList.add(new TpInfo(TpInfo.DVBC, j, satId, j+1, i, symbol, qam, TpInfo.Cable.BAND_6MHZ));
                tps.add(j);
                j++;
            }
            satInfoList.get(satId).setTps(tps);
            satInfoList.get(satId).setTpNum(j);
        }
        else if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS)) {
            for(int i = 105000 ; i < 1005000 ; i+=6000) {
                int qam = TpInfo.Cable.QAM_256;
                int symbol = 5217;
                tpInfoList.add(new TpInfo(TpInfo.DVBC, j, satId, j+1, i, symbol, qam, TpInfo.Cable.BAND_6MHZ));
                tps.add(j);
                j++;
            }
            satInfoList.get(satId).setTps(tps);
            satInfoList.get(satId).setTpNum(j);
        }
        else{
            for (int i = 105000; i < 1005000; i += 6000) {
                int qam = TpInfo.Cable.QAM_256;
                int symbol = 5200;
                if (i == 615000) {
                    qam = TpInfo.Cable.QAM_64;
                    symbol = 5057;
                }
                tpInfoList.add(new TpInfo(TpInfo.DVBC, j, satId, j + 1, i, 5200, qam, 0));
                tps.add(j);
                j++;
            }
        }
        satInfoList.get(satId).setTps(tps);
        satInfoList.get(satId).setTpNum(j);
    }

    public SatelliteDefaultData buildSatTpInfoList(SatelliteDefaultData satelliteDefaultData,int satId, int startTpId) {
        Log.d(TAG,"buildSatTpInfoList satId = "+satId + " startTpId = "+startTpId);
        List<Integer> tps = new ArrayList<>();
        for(int i = 0; i < satelliteDefaultData.mTpInfoList.size(); i++) {
            TpInfo tpInfo = new TpInfo(satelliteDefaultData.mTpInfoList.get(i));
            tpInfo.setSatId(satId);
            tpInfo.setTpId(startTpId);
            tpInfoList.add(tpInfo);
            satelliteDefaultData.mTpInfoList.set(i, tpInfo);//ruby add
            tps.add(tpInfo.getTpId());
            startTpId++;
        }
        satelliteDefaultData.mSatInfo.setTpNum(satelliteDefaultData.mTpInfoList.size());
        satelliteDefaultData.mSatInfo.setTps(tps);
        satelliteDefaultData.mSatInfo.setSatId(satId);//ruby add
        satelliteDefaultData.mEndTpId = startTpId;
        return satelliteDefaultData;
    }

    public void buildMNBCTerrTpInfoList() {
        Log.d(TAG,"buildMNBCTerrTpInfoList");
        int j = 0;
        int satId = 0;
        List<Integer> tps = new ArrayList<>();
        for(int i = 650000 ; i < 690000 ; i+=8000) {

            tpInfoList.add(new TpInfo(TpInfo.DVBT, j, satId, j+43, i, 0, 0,TpInfo.Terr.BAND_8MHZ));
            tps.add(j);
            j++;
        }
        satInfoList.get(satId).setTps(tps);
        satInfoList.get(satId).setTpNum(j);
    }

    public void buildTerrTpInfoList() {
        if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_MNBC))
            buildMNBCTerrTpInfoList();
        else
            Log.d(TAG,"buildTerrTpInfoList fail , not porting");
    }

    public void buildIsdbtTpInfoList() {
        //Log.d(TAG,"buildTpInfoList fail , TpInfo.ISDBT not porting");
        int j = 0;
        int satId = 0;
        List<Integer> tps = new ArrayList<>();
        for(int i = 177143 ; i < 219143 ; i+=6000) {

            tpInfoList.add(new TpInfo(TpInfo.ISDBT, j, satId, j+7, i, 0, 0,TpInfo.Terr.BAND_6MHZ));
            tps.add(j);
            j++;
        }
        for(int i = 473143 ; i < 809143 ; i+=6000) {

            tpInfoList.add(new TpInfo(TpInfo.ISDBT, j, satId, j+7, i, 0, 0,TpInfo.Terr.BAND_6MHZ));
            tps.add(j);
            j++;
        }
        satInfoList.get(satId).setTps(tps);
        satInfoList.get(satId).setTpNum(j);
    }

    private void buildGpos(int tunerType) {
        // tony maybe delete, update by content resolver
        //GposInfo gposInfo = new GposInfo();//gary remove
        /*String dbver = "";
        if(tunerType == TpInfo.DVBC)
            dbver = "C";
        else if(tunerType == TpInfo.DVBT)
            dbver = "T";
        else if(tunerType == TpInfo.ISDBT)
            dbver = "I";
        else if(tunerType == TpInfo.DVBS)
            dbver = "S";
        gposInfo.setDBVersion(dbver+"1.0");
        gposInfo.setCurChannelId(0);
        gposInfo.setCurGroupType(0);
        gposInfo.setPasswordValue(0);
        gposInfo.setWorkerPasswordValue(13197776);
        gposInfo.setPurchasePasswordValue(0);
        gposInfo.setParentalRate(0);
        gposInfo.setParentalLockOnOff(1);
        gposInfo.setInstallLockOnOff(0);
        gposInfo.setBoxPowerStatus(1);
        gposInfo.setStartOnChannelId(0);
        gposInfo.setStartOnChType(0);
        gposInfo.setVolume(15);
        gposInfo.setAudioTrackMode(0);
        gposInfo.setAutoRegionTimeOffset(0);
        gposInfo.setRegionTimeOffset((float)0);
        gposInfo.setRegionSummerTime(0);
        gposInfo.setLnbPower(1);
        gposInfo.setScreen16x9(0);
        gposInfo.setConversion(1);
        if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)
                || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC)
                || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS))
            gposInfo.setOSDLanguage("chi");
        else
            gposInfo.setOSDLanguage("eng");
        gposInfo.setSearchProgramType(TVScanParams.SEARCH_OPTION_ALL);
        gposInfo.setSearchMode(TVScanParams.SEARCH_OPTION_ALL);
        gposInfo.setAudioLanguageSelection(0,"chi");
        gposInfo.setAudioLanguageSelection(1,"eng");
        gposInfo.setSubtitleLanguageSelection(0,"chi");
        gposInfo.setSubtitleLanguageSelection(1,"eng");
        gposInfo.setSortByLcn(1);
        gposInfo.setOSDTransparency(0);
        gposInfo.setBannerTimeout(5);
        gposInfo.setHardHearing(1);
        gposInfo.setAutoStandbyTime(0);
        gposInfo.setDolbyMode(0);
        gposInfo.setHDCPOnOff(1);
        gposInfo.setDeepSleepMode(0);
        if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)
                || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC)
                || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS))
            gposInfo.setSubtitleOnOff(1);
        else
            gposInfo.setSubtitleOnOff(0);
        gposInfo.setAvStopMode(1);
        gposInfo.setTimeshiftDuration(90*60);
        gposInfo.setRecordIconOnOff(1);
        gposInfo.setTimeLockPeriodStart(0, -1);
        gposInfo.setTimeLockPeriodStart(1, -1);
        gposInfo.setTimeLockPeriodStart(2, -1);
        gposInfo.setTimeLockPeriodEnd(0, -1);
        gposInfo.setTimeLockPeriodEnd(1, -1);
        gposInfo.setTimeLockPeriodEnd(2, -1);
        if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DIGITURK))
            gposInfo.setBatId(DIGITURK_BAT_ID);
        else if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS))
            gposInfo.setBatId(CNS_BAT_ID);
        else
            gposInfo.setBatId(0);
        gposInfo.setBatVersion(0);
        gposInfo.setNitId(0);
        gposInfo.setSINitNetworkId(0);
        gposInfo.setNitVersion(0);
        gposInfo.setIntroductionTime(5000);
        gposInfo.setPvrEnable(0);
        gposInfo.setBatId(25149);
        gposInfo.setHomeId("0");
        gposInfo.setSo("20");
        gposInfo.setAreaCode("0");
        gposInfo.setZipCode("0000");
        gposInfo.setMobile("");
        gposInfo.setStandbyRedirect(0);
        gposInfo.setMailSettingsShopping(1);
        gposInfo.setMailSettingsNews(1);
        gposInfo.setMailSettingsPopular(1);
        gposInfo.setMailSettingsCoupon(1);
        gposInfo.setMailSettingsService(1);
        gposInfo.setMailNotifyStatus(1);
        gposInfo.setWVCasLicenseURL(Pvcfg.CNS_DR_SERVER);
        gposInfo.setSTBRefCasDataTime(28800);//20 days  20*24*60
        gposInfo.setDVRManagementLimit(0);
        for(int i =0 ; i<GposInfo.MAX_NUM_OF_IRD_COMMAND; i++){
            gposInfo.setIrdCommand(0);
        }
        gposInfo.initBouquetIds();
        gposInfo.initBouquetVers();
        gposInfo.initBouquetNames();
        gposInfo.setTunerOutput5V(0);
        gposInfo.setTutorialSetting(0);
        gposInfo.setSTBDataReturn(1);
        gposInfo.setHdmiCecOnOff(1);
        gposInfo.setCasUpdateTime(0);
        GposInfo.setCNSMenuSettingLang(mContext,"zh");*/
    }

    private void buildGroupList() {
        int i = 0;
        if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)
                || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC)) {
            buildDMGGroupList();
        }
        else if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS)) {
            buildCNSGroupList();
        }
        else {
            favGroupNameList.add(new FavGroupName(FavGroup.ALL_TV_TYPE, "All TV"));
            for (i = 0; i < FavGroupName.TV_FAV_NUM; i++) {
                favGroupNameList.add(new FavGroupName(FavGroup.TV_FAV_BASE_TYPE + i, "TV Favorite " + i));
            }
            favGroupNameList.add(new FavGroupName(FavGroup.ALL_RADIO_TYPE, "All Radio"));
            for (i = 0; i < FavGroupName.RADIO_FAV_NUM; i++) {
                favGroupNameList.add(new FavGroupName(FavGroup.RADIO_FAV_BASE_TYPE + i, "Radio Favorite " + i));
            }

            for (i = 0; i < FavGroupName.GENRE_NUM; i++) {
                favGroupNameList.add(new FavGroupName(FavGroup.GENRE_BASE_TYPE + i, ""));
            }

            /*
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_KIDS,"Kids"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_EDUCATION,"Education"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_NEWS,"News"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_MOVIES,"Movies"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_VARIETY,"Variety"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_MUSIC,"Music"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_ADULT,"Adult"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_SPORTS,"Sports"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_HD,"HD"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_RELIGION,"Religion"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_SHOPPING,"Shopping"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_UHD,"UHD"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_MANDARIN,"MANDARIN"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_WESTERN,"WESTERN"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_JPOP,"JPOP"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_LOUNGE,"LOUNGE"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CLASSICAL,"CLASSICAL"));
            favGroupNameList.add(new FavGroupName(FavGroup.GROUP_ELSE,"ELSE"));
             */
        }
    }
    private void buildCNSGroupList() {
        int i = 0;
        favGroupNameList.add(new FavGroupName(0,"All TV"));
        for(i = 1; i <= FavGroupName.TV_FAV_NUM; i++) {
            favGroupNameList.add(new FavGroupName(i,"TV Favorite "+i));
        }
        favGroupNameList.add(new FavGroupName(FavGroupName.TV_FAV_NUM+1,"All Radio"));
        for(i = 1; i <= FavGroupName.RADIO_FAV_NUM; i++) {
            favGroupNameList.add(new FavGroupName(i+FavGroupName.TV_FAV_NUM+1,"Radio Favorite "+i));
        }
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CNS_PUBLIC_WELFARE_RRLIGION,"Public Welfare/Religion"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CNS_DRAMA_MUSIC,"Drama/Music"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CNS_NEWS_FINANCE,"News/Finance"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CNS_LEISURE_KNOWLEDGE,"Leisure/Knowledge"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CNS_CHILDREN_ANIMATION,"Children/Animation"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CNS_FILMS_SERIES,"Films/Series"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CNS_VARIETY,"Variety"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CNS_HOME_SHOPPING,"Home Shopping"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CNS_FOREIGN_LANGUAGE_LEARNING,"Foreign Language/Learning"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CNS_HD,"HD"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CNS_SPORTS_OTHERS,"Sports/Others"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CNS_ADULT,"Adult"));
    }
    private void buildDMGGroupList() {
        int i = 0;
        favGroupNameList.add(new FavGroupName(0,"All TV"));
        for(i = 1; i <= FavGroupName.TV_FAV_NUM; i++) {
            favGroupNameList.add(new FavGroupName(i,"TV Favorite "+i));
        }
        favGroupNameList.add(new FavGroupName(FavGroupName.TV_FAV_NUM+1,"All Radio"));
        for(i = 1; i <= FavGroupName.RADIO_FAV_NUM; i++) {
            favGroupNameList.add(new FavGroupName(i+FavGroupName.TV_FAV_NUM+1,"Radio Favorite "+i));
        }
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_KIDS,"Kids"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_EDUCATION,"Education"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_NEWS,"News"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_MOVIES,"Movies"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_VARIETY,"Variety"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_MUSIC,"Music"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_ADULT,"Adult"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_SPORTS,"Sports"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_HD,"HD"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_RELIGION,"Religion"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_SHOPPING,"Shopping"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_UHD,"UHD"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_MANDARIN,"MANDARIN"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_WESTERN,"WESTERN"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_JPOP,"JPOP"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_LOUNGE,"LOUNGE"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_CLASSICAL,"CLASSICAL"));
        favGroupNameList.add(new FavGroupName(FavGroup.GROUP_ELSE,"ELSE"));
    }

    public List<FavGroup> buildDefaultFavGroup(){
        List<FavGroup> newFavGroupList = new ArrayList<>();
        for (FavGroupName favGroupName : favGroupNameList) {
            newFavGroupList.add(new FavGroup(favGroupName));
        }
        return newFavGroupList;
    }
}
