package com.prime.dtvplayer.TestData.sysglob.TestDataImpl;

import android.content.Context;
import android.util.Log;

import com.prime.dtvplayer.TestData.TestData.TestData;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.TestData.sysglob.GposInfoFunc;
import com.prime.dtvplayer.TestData.tvclient.TestDataTVClient;

/**
 * Created by johnny_shih on 2017/11/27.
 */

public class TestDataGposInfoFuncImpl implements GposInfoFunc
{
    private static final String TAG = "TestDataGposInfoFuncImpl";
    private Context context;
    private TestData testData = null;

    public TestDataGposInfoFuncImpl(Context context)
    {
        this.context = context;
        //this.testData = new TestData( TpInfo.DVBC );
        this.testData = TestDataTVClient.TestData;
    }

    @Override
    public GposInfo GetGposInfo() {
        GposInfo Gpos = new GposInfo();
        GposInfo curGPos = testData.GetTestDatGposInfo();

        Gpos.setDBVersion(curGPos.getDBVersion());
        Gpos.setCurChannelId(curGPos.getCurChannelId());
        Gpos.setCurGroupType(curGPos.getCurGroupType());
        Gpos.setPasswordValue(curGPos.getPasswordValue());
        Gpos.setParentalRate(curGPos.getParentalRate());
        Gpos.setParentalLockOnOff(curGPos.getParentalLockOnOff());
        Gpos.setInstallLockOnOff(curGPos.getInstallLockOnOff());
        Gpos.setBoxPowerStatus(curGPos.getBoxPowerStatus());
        Gpos.setStartOnChannelId(curGPos.getStartOnChannelId());
        Gpos.setStartOnChType(curGPos.getStartOnChType());
        Gpos.setVolume(curGPos.getVolume());
        Gpos.setAudioTrackMode(curGPos.getAudioTrackMode());
        Gpos.setAutoRegionTimeOffset(curGPos.getAutoRegionTimeOffset());
        Gpos.setRegionTimeOffset(curGPos.getRegionTimeOffset());
        Gpos.setRegionSummerTime(curGPos.getRegionSummerTime());
        Gpos.setLnbPower(curGPos.getLnbPower());
        Gpos.setScreen16x9(curGPos.getScreen16x9());
        Gpos.setResolution(curGPos.getResolution());
        Gpos.setConversion(curGPos.getConversion());
        Gpos.setOSDLanguage(curGPos.getOSDLanguage());
        Gpos.setSearchProgramType(curGPos.getSearchProgramType());
        Gpos.setSearchMode(curGPos.getSearchMode());
        Gpos.setAudioLanguageSelection(0, curGPos.getAudioLanguageSelection(0));
        Gpos.setAudioLanguageSelection(1, curGPos.getAudioLanguageSelection(1));
        Gpos.setSubtitleLanguageSelection(0, curGPos.getSubtitleLanguageSelection(0));
        Gpos.setSubtitleLanguageSelection(1, curGPos.getSubtitleLanguageSelection(1));
        Gpos.setSortByLcn(curGPos.getSortByLcn());
        Gpos.setOSDTransparency(curGPos.getOSDTransparency());
        Gpos.setBannerTimeout(curGPos.getBannerTimeout());
        Gpos.setHardHearing(curGPos.getHardHearing());
        Gpos.setAutoStandbyTime(curGPos.getAutoStandbyTime());
        Gpos.setDolbyMode(curGPos.getDolbyMode());
        Gpos.setHDCPOnOff(curGPos.getHDCPOnOff());
        Gpos.setDeepSleepMode(curGPos.getDeepSleepMode());
        Gpos.setSubtitleOnOff(curGPos.getSubtitleOnOff());
        Log.d(TAG, "add "+ Gpos.ToString());

        return Gpos;
    }

    @Override
    public void Save(GposInfo Gpos) {
        GposInfo curGPos = testData.GetTestDatGposInfo();

        curGPos.setDBVersion(Gpos.getDBVersion());
        curGPos.setCurChannelId(Gpos.getCurChannelId());
        curGPos.setCurGroupType(Gpos.getCurGroupType());
        curGPos.setPasswordValue(Gpos.getPasswordValue());
        curGPos.setParentalRate(Gpos.getParentalRate());
        curGPos.setParentalLockOnOff(Gpos.getParentalLockOnOff());
        curGPos.setInstallLockOnOff(Gpos.getInstallLockOnOff());
        curGPos.setBoxPowerStatus(Gpos.getBoxPowerStatus());
        curGPos.setStartOnChannelId(Gpos.getStartOnChannelId());
        curGPos.setStartOnChType(Gpos.getStartOnChType());
        curGPos.setVolume(Gpos.getVolume());
        curGPos.setAudioTrackMode(Gpos.getAudioTrackMode());
        curGPos.setAutoRegionTimeOffset(Gpos.getAutoRegionTimeOffset());
        curGPos.setRegionTimeOffset(Gpos.getRegionTimeOffset());
        curGPos.setRegionSummerTime(Gpos.getRegionSummerTime());
        curGPos.setLnbPower(Gpos.getLnbPower());
        curGPos.setScreen16x9(Gpos.getScreen16x9());
        curGPos.setConversion(Gpos.getConversion());
        curGPos.setResolution(Gpos.getResolution());
        curGPos.setOSDLanguage(Gpos.getOSDLanguage());
        curGPos.setSearchProgramType(Gpos.getSearchProgramType());
        curGPos.setSearchMode(Gpos.getSearchMode());
        curGPos.setAudioLanguageSelection(0, Gpos.getAudioLanguageSelection(0));
        curGPos.setAudioLanguageSelection(1, Gpos.getAudioLanguageSelection(1));
        curGPos.setSubtitleLanguageSelection(0, Gpos.getSubtitleLanguageSelection(0));
        curGPos.setSubtitleLanguageSelection(1, Gpos.getSubtitleLanguageSelection(1));
        curGPos.setSortByLcn(Gpos.getSortByLcn());
        curGPos.setOSDTransparency(Gpos.getOSDTransparency());
        curGPos.setBannerTimeout(Gpos.getBannerTimeout());
        curGPos.setHardHearing(Gpos.getHardHearing());
        curGPos.setAutoStandbyTime(Gpos.getAutoStandbyTime());
        curGPos.setDolbyMode(Gpos.getDolbyMode());
        curGPos.setHDCPOnOff(Gpos.getHDCPOnOff());
        curGPos.setDeepSleepMode(Gpos.getDeepSleepMode());
        curGPos.setSubtitleOnOff(Gpos.getSubtitleOnOff());
    }
}
