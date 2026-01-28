package com.prime.dtvplayer.Service.DataManager;

import android.util.Log;

import com.prime.dtvplayer.Sysdata.AntInfo;
import com.prime.dtvplayer.Sysdata.FavGroupName;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.MiscDefine;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.utils.TVScanParams;
import com.prime.sysdata.FavInfo;

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
            for(int i = 111000 ; i < 1005000 ; i+=6000) {
                tpInfoList.add(new TpInfo(tunerType, j, satId, j+1, i, 5217, TpInfo.Cable.QAM_256));
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
        GposInfo gposInfo = new GposInfo();
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
        gposInfo.setOSDLanguage("eng");
        gposInfo.setSearchProgramType(TVScanParams.SEARCH_OPTION_ALL);
        gposInfo.setSearchMode(TVScanParams.SEARCH_OPTION_ALL);
        gposInfo.setAudioLanguageSelection(0,"English");
        gposInfo.setAudioLanguageSelection(1,"English");
        gposInfo.setSubtitleLanguageSelection(0,"English");
        gposInfo.setSubtitleLanguageSelection(1,"English");
        gposInfo.setSortByLcn(1);
        gposInfo.setOSDTransparency(0);
        gposInfo.setBannerTimeout(5);
        gposInfo.setHardHearing(1);
        gposInfo.setAutoStandbyTime(0);
        gposInfo.setDolbyMode(0);
        gposInfo.setHDCPOnOff(1);
        gposInfo.setDeepSleepMode(0);
        gposInfo.setSubtitleOnOff(0);
        gposInfo.setAvStopMode(1);
        gposInfo.setTimeshiftDuration(90*60);
        gposInfo.setRecordIconOnOff(1);
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
    }
}
