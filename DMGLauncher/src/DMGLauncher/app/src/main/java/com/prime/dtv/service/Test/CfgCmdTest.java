package com.prime.dtv.service.Test;

import android.util.Log;

import com.prime.dtv.Interface.PesiDtvFrameworkInterface;
import com.prime.dtv.sysdata.GposInfo;

public class CfgCmdTest {
    private static final String TAG = "CfgCmdTest";

    private final PesiDtvFrameworkInterface mPesiDtvFrameworkInterface;

    public CfgCmdTest(PesiDtvFrameworkInterface pesiDtvFrameworkInterface) {
        mPesiDtvFrameworkInterface = pesiDtvFrameworkInterface;
    }

    public boolean startTest() {
        Log.d(TAG, "startTest: ==========cfg cmd test start==========");

        // gpos update & get test -s
        GposInfo gposInfo = mPesiDtvFrameworkInterface.GposInfoGet();
        if (gposInfo == null) {
            Log.d(TAG, "startTest: GposInfoGet() get null gpos info");
            return false;
        }

        long testCurChannelID = 123456;
        long testStartOnChannelID = 654321;
        int testSubtitleOnOff = 1;
        String testLangSelect = "zh";

        gposInfo.setCurChannelId(testCurChannelID);
        gposInfo.setStartOnChannelId(testStartOnChannelID);
        gposInfo.setSubtitleOnOff(testSubtitleOnOff);
        gposInfo.setAudioLanguageSelection(0, testLangSelect);
        Log.d(TAG, "startTest: ==========GposInfoUpdate()==========");
        mPesiDtvFrameworkInterface.GposInfoUpdate(gposInfo);

        Log.d(TAG, "startTest: ==========GposInfoGet()==========");
        gposInfo = mPesiDtvFrameworkInterface.GposInfoGet();
        Log.d(TAG, "startTest: CurChannelId = " + gposInfo.getCurChannelId() + ", expect = " + testCurChannelID);
        Log.d(TAG, "startTest: StartOnChannelId = " + gposInfo.getStartOnChannelId() + ", expect = " + testStartOnChannelID);
        Log.d(TAG, "startTest: SubtitleOnOff = " + gposInfo.getSubtitleOnOff() + ", expect = " + testSubtitleOnOff);
        Log.d(TAG, "startTest: AudioLanguageSelection1 = " + gposInfo.getAudioLanguageSelection(0) + ", expect = " + testLangSelect);
        if (gposInfo.getCurChannelId() != testCurChannelID
                || gposInfo.getStartOnChannelId() != testStartOnChannelID
                || gposInfo.getSubtitleOnOff() != testSubtitleOnOff
                || !gposInfo.getAudioLanguageSelection(0).equals(testLangSelect)) {
            Log.d(TAG, "startTest: get wrong gpos value after GposInfoUpdate()");
            return false;
        }

        testSubtitleOnOff = 0;
        testLangSelect = "eng";
        Log.d(TAG, "startTest: ==========GposInfoUpdateByKeyString()==========");
        mPesiDtvFrameworkInterface.GposInfoUpdateByKeyString(GposInfo.GPOS_AUDIO_LANG_SELECT_1, testLangSelect);
        mPesiDtvFrameworkInterface.GposInfoUpdateByKeyString(GposInfo.GPOS_SUBTITLE_ONOFF, testSubtitleOnOff);

        Log.d(TAG, "startTest: ==========GposInfoGet()==========");
        gposInfo = mPesiDtvFrameworkInterface.GposInfoGet();
        Log.d(TAG, "startTest: CurChannelId = " + gposInfo.getCurChannelId() + ", expect = " + testCurChannelID);
        Log.d(TAG, "startTest: StartOnChannelId = " + gposInfo.getStartOnChannelId() + ", expect = " + testStartOnChannelID);
        Log.d(TAG, "startTest: SubtitleOnOff = " + gposInfo.getSubtitleOnOff() + ", expect = " + testSubtitleOnOff);
        Log.d(TAG, "startTest: AudioLanguageSelection1 = " + gposInfo.getAudioLanguageSelection(0) + ", expect = " + testLangSelect);
        if (gposInfo.getCurChannelId() != testCurChannelID
                || gposInfo.getStartOnChannelId() != testStartOnChannelID
                || gposInfo.getSubtitleOnOff() != testSubtitleOnOff
                || !gposInfo.getAudioLanguageSelection(0).equals(testLangSelect)) {
            Log.d(TAG, "startTest: get wrong gpos value after GposInfoUpdateByKeyString()");
            return false;
        }
        // gpos update & get test -e

        // other test -s
        // version
        Log.d(TAG, "startTest: ==========GetPesiServiceVersion()==========");
        String version = mPesiDtvFrameworkInterface.GetPesiServiceVersion();
        Log.d(TAG, "startTest: version = " + version);
        if (version == null || version.isEmpty()) {
            Log.d(TAG, "startTest: GetPesiServiceVersion() get null or empty version");
            return false;
        }

        // reset default
        Log.d(TAG, "startTest: ==========ResetFactoryDefault()==========");
        mPesiDtvFrameworkInterface.ResetFactoryDefault();
        try {
            // sleep for reset
            Log.d(TAG, "startTest: ==========sleep 2 secs==========");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // after reset, CurChannelId and StartOnChannelID should be 0
        Log.d(TAG, "startTest: ==========GposInfoGet()==========");
        gposInfo = mPesiDtvFrameworkInterface.GposInfoGet();
        Log.d(TAG, "startTest: CurChannelId = " + gposInfo.getCurChannelId() + ", expect = " + 0);
        Log.d(TAG, "startTest: StartOnChannelId = " + gposInfo.getStartOnChannelId() + ", expect = " + 0);
        if (gposInfo.getCurChannelId() != 0 || gposInfo.getStartOnChannelId() != 0) {
            Log.d(TAG, "startTest: gpos CurChannelId or StartOnChannelID not reset to 0 after ResetFactoryDefault()");
            return false;
        }
        // other test -e

        return true;
    }
}
