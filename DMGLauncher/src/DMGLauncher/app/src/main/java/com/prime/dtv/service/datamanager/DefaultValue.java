package com.prime.dtv.service.datamanager;


import android.util.Log;

import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.sysdata.FavGroupName;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.SatInfo;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.utils.TVScanParams;

import java.util.ArrayList;
import java.util.List;

public class DefaultValue {
    private static final String TAG = "DefaultValue";
    public List<SatInfo> satInfoList = new ArrayList<>();
    public List<TpInfo> tpInfoList = new ArrayList<>();
    public GposInfo gposInfo = new GposInfo();
    public List<FavGroupName> favGroupNameList = new ArrayList<>();

    public DefaultValue(int tunerType) {
        buildSatInfoList(TpInfo.DVBC);
        buildTpInfoList(TpInfo.DVBC);
        buildGpos(TpInfo.DVBC);
        buildGroupList();
    }

    private void buildSatInfoList(int tunerType) {
        if(tunerType == TpInfo.DVBC) {
            int satId = 0;
            SatInfo satInfo = new SatInfo(satId,"DVBC_SAT",TpInfo.DVBC,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
            satInfoList.add(satInfo);
        }
        else if(tunerType == TpInfo.DVBS) {
            Log.d(TAG,"buildSatInfoList fail , TpInfo.DVBS not porting");
        }
        else if(tunerType == TpInfo.DVBT) {
            Log.d(TAG,"buildSatInfoList fail , TpInfo.DVBT not porting");
        }
        else if(tunerType == TpInfo.ISDBT) {
            Log.d(TAG,"buildSatInfoList fail , TpInfo.ISDBT not porting");
        }
        else {
            Log.d(TAG,"buildSatInfoList fail , unknown tuner type["+tunerType+"]");
        }
    }

    public void buildTpInfoList(int tunerType) {
        if(tunerType == TpInfo.DVBC) {
            int j = 0;
            int satId = 0;
            List<Integer> tps = new ArrayList<>();
            for(int i = 105000 ; i < 1005000 ; i+=6000) {
                int qam = TpInfo.Cable.QAM_256;
                int symbol = 5200;
                if(i == 615000) {
                    qam = TpInfo.Cable.QAM_64;
                    symbol = 5057;
                }
                tpInfoList.add(new TpInfo(tunerType, j, satId, j+1, i, symbol, qam));
                tps.add(j);
                j++;
            }
            satInfoList.get(satId).setTps(tps);
            satInfoList.get(satId).setTpNum(j);
        }
        else if(tunerType == TpInfo.DVBS) {
            Log.d(TAG,"buildTpInfoList fail , TpInfo.DVBS not porting");
        }
        else if(tunerType == TpInfo.DVBT) {
            Log.d(TAG,"buildTpInfoList fail , TpInfo.DVBT not porting");
        }
        else if(tunerType == TpInfo.ISDBT) {
            Log.d(TAG,"buildTpInfoList fail , TpInfo.ISDBT not porting");
        }
        else {
            Log.d(TAG,"buildTpInfoList fail , unknown tuner type["+tunerType+"]");
        }
    }

    private void buildGpos(int tunerType) {
        //GposInfo gposInfo = new GposInfo();//gary remove
        String dbver = "";
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
        gposInfo.setWorkerPasswordValue(77767776);
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
        gposInfo.setOSDLanguage("chi");
        gposInfo.setSearchProgramType(TVScanParams.SEARCH_OPTION_ALL);
        gposInfo.setSearchMode(TVScanParams.SEARCH_OPTION_ALL);
        gposInfo.setAudioLanguageSelection(0,"eng");
        gposInfo.setAudioLanguageSelection(1,"eng");
        gposInfo.setSubtitleLanguageSelection(0,"eng");
        gposInfo.setSubtitleLanguageSelection(1,"eng");
        gposInfo.setSortByLcn(1);
        gposInfo.setOSDTransparency(0);
        gposInfo.setBannerTimeout(5);
        gposInfo.setHardHearing(1);
        gposInfo.setAutoStandbyTime(0);
        gposInfo.setDolbyMode(0);
        gposInfo.setHDCPOnOff(1);
        gposInfo.setDeepSleepMode(0);
        gposInfo.setSubtitleOnOff(1);
        gposInfo.setAvStopMode(1);
        gposInfo.setTimeshiftDuration(90*60);
        gposInfo.setRecordIconOnOff(1);
        gposInfo.setTimeLockPeriodStart(0, -1);
        gposInfo.setTimeLockPeriodStart(1, -1);
        gposInfo.setTimeLockPeriodStart(2, -1);
        gposInfo.setTimeLockPeriodEnd(0, -1);
        gposInfo.setTimeLockPeriodEnd(1, -1);
        gposInfo.setTimeLockPeriodEnd(2, -1);
        gposInfo.setBatId(0);
        gposInfo.setBatVersion(0);
        gposInfo.setNitId(0);
        gposInfo.setSINitNetworkId(0);
        gposInfo.setNitVersion(0);
        gposInfo.setIntroductionTime(5000);
        gposInfo.setPvrEnable(0);
        gposInfo.setHomeId("0");
        gposInfo.setSo("");
        gposInfo.setMobile("");
        gposInfo.setStandbyRedirect(0);
        gposInfo.setMailSettingsShopping(1);
        gposInfo.setMailSettingsNews(1);
        gposInfo.setMailSettingsPopular(1);
        gposInfo.setMailSettingsCoupon(1);
        gposInfo.setMailSettingsService(1);
        gposInfo.setMailNotifyStatus(1);
        gposInfo.setWVCasLicenseURL(Pvcfg.DMG_DR_SERVER);
        gposInfo.setWVCasLicenseURL("");
        gposInfo.setSTBRefCasDataTime(28800);//20 days  20*24*60
        gposInfo.setDVRManagementLimit(0);
        for(int i =0 ; i<GposInfo.MAX_NUM_OF_IRD_COMMAND; i++){
            gposInfo.setIrdCommand(0);
        }
    }

    private void buildGroupList() {
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
