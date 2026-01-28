package com.prime.dtv.service.Test;

import android.content.Context;
import android.util.Log;

import com.prime.dtv.Interface.PesiDtvFrameworkInterface;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.SimpleChannel;

import java.util.ArrayList;
import java.util.List;

public class PmCmdTest {
    private static final String TAG = "PmCmdTest";
    private static final int TEST_CHANNEL_COUNT = 5;
    private static final int TEST_CHANNEL_ID_START = 100100;
    private static final int TEST_CHANNEL_NUM_START = 100;
    private static final String TEST_CHANNEL_NAME_PREFIX = "PmCmdTestChannel";

    private final PesiDtvFrameworkInterface mPesiDtvFrameworkInterface;
    private final DataManager mDataManager;
    private Context mContext;

    public PmCmdTest(Context context, PesiDtvFrameworkInterface pesiDtvFrameworkInterface) {
        mPesiDtvFrameworkInterface = pesiDtvFrameworkInterface;
        mContext = context;
        mDataManager = DataManager.getDataManager(context);
    }

    private void setupTestPrograms() {
        mDataManager.delAllProgramInfo();

        for (int i = 0 ; i < TEST_CHANNEL_COUNT ; i++) {
            ProgramInfo programInfo = new ProgramInfo();
            programInfo.setChannelId(TEST_CHANNEL_ID_START + i);
            programInfo.setDisplayNum(TEST_CHANNEL_NUM_START + i);
            programInfo.setDisplayName(TEST_CHANNEL_NAME_PREFIX + i);
            mDataManager.addProgramInfo(programInfo);
        }
    }

    /*
    channel & program
     */
    public boolean startProgramInfoTest() {
        Log.d(TAG, "startProgramInfoTest: ==========program test start==========");
        setupTestPrograms();

        // get program list test -s
        List<ProgramInfo> programInfoList = mPesiDtvFrameworkInterface.getProgramInfoList(
                FavGroup.ALL_TV_TYPE,
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);

        Log.d(TAG, "startProgramInfoTest: ==========getProgramInfoList()==========");
        Log.d(TAG, "startProgramInfoTest: getProgramInfoList size = " + programInfoList.size() + ", expect = " + TEST_CHANNEL_COUNT);
        if (programInfoList.size() != TEST_CHANNEL_COUNT) {
            Log.e(TAG, "startProgramInfoTest: getProgramInfoList() size wrong");
            return false;
        }

        for (int i = 0 ; i < TEST_CHANNEL_COUNT ; i++) {
            long channelID = programInfoList.get(i).getChannelId();
            int channelNumber = programInfoList.get(i).getDisplayNum();
            String channelName = programInfoList.get(i).getDisplayName();

            Log.d(TAG, "startProgramInfoTest: ==========program[" + i + "]==========");
            Log.d(TAG, "startProgramInfoTest: channelID = " + channelID + ", expect = " + (TEST_CHANNEL_ID_START + i));
            Log.d(TAG, "startProgramInfoTest: channelNumber = " + channelNumber + ", expect = " + (TEST_CHANNEL_NUM_START + i));
            Log.d(TAG, "startProgramInfoTest: channelName = " + channelName + ", expect = " + (TEST_CHANNEL_NAME_PREFIX + i));
            if (channelID != TEST_CHANNEL_ID_START + i
                    || channelNumber != TEST_CHANNEL_NUM_START + i
                    || !channelName.equals(TEST_CHANNEL_NAME_PREFIX + i)) {
                Log.e(TAG, "startProgramInfoTest: wrong program data");
                return false;
            }
        }
        // get program list test -e

        // update first program test -s
        String testChannelName = "test program 123";
        int testChannelNumber = 123;
        ProgramInfo testProgramInfo = programInfoList.get(0);
        testProgramInfo.setDisplayNum(testChannelNumber);
        testProgramInfo.setDisplayName(testChannelName);
        mPesiDtvFrameworkInterface.updateProgramInfo(testProgramInfo);

        ProgramInfo programInfoByChannelID = mPesiDtvFrameworkInterface.getProgramByChannelId(testProgramInfo.getChannelId());
        ProgramInfo programInfoByChNumber = mPesiDtvFrameworkInterface.getProgramByChnum(testProgramInfo.getDisplayNum(), FavGroup.ALL_TV_TYPE);

        Log.d(TAG, "startProgramInfoTest: ==========update program[0]==========");
        Log.d(TAG, "startProgramInfoTest: ==========getProgramByChannelId==========");
        Log.d(TAG, "startProgramInfoTest: channelNumber = " + programInfoByChannelID.getDisplayNum() + ", expect = " + testChannelNumber);
        Log.d(TAG, "startProgramInfoTest: channelName = " + programInfoByChannelID.getDisplayName() + ", expect = " + testChannelName);
        Log.d(TAG, "startProgramInfoTest: ==========getProgramByChnum==========");
        Log.d(TAG, "startProgramInfoTest: channelNumber = " + programInfoByChNumber.getDisplayNum() + ", expect = " + testChannelNumber);
        Log.d(TAG, "startProgramInfoTest: channelName = " + programInfoByChNumber.getDisplayName() + ", expect = " + testChannelName);
        if (programInfoByChannelID.getDisplayNum() != testChannelNumber
                || !programInfoByChannelID.getDisplayName().equals(testChannelName)
                || programInfoByChNumber.getDisplayNum() != testChannelNumber
                || !programInfoByChNumber.getDisplayName().equals(testChannelName)) {

            Log.e(TAG, "startProgramInfoTest: get wrong value after update");
            return false;
        }
        // update first program test -e

        // delete last program test -s
        int preDelSize = programInfoList.size();
        testProgramInfo = programInfoList.get(preDelSize - 1);
        mPesiDtvFrameworkInterface.deleteProgram(testProgramInfo.getChannelId());

        programInfoList = mPesiDtvFrameworkInterface.getProgramInfoList(
                FavGroup.ALL_TV_TYPE,
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);

        Log.d(TAG, "startProgramInfoTest: ==========deleteProgram()==========");
        Log.d(TAG, "startProgramInfoTest: deleteProgram() size = " + programInfoList.size() + ", expect = " + (preDelSize - 1));
        if (programInfoList.size() != preDelSize - 1) {
            Log.e(TAG, "startProgramInfoTest: size wrong after delete");
            return false;
        }

        programInfoByChannelID = mPesiDtvFrameworkInterface.getProgramByChannelId(testProgramInfo.getChannelId());
        if (programInfoByChannelID != null) {
            Log.e(TAG, "startProgramInfoTest: program still exist after delete");
            return false;
        }
        // delete last program test -e

        return true;
    }

    public boolean startSimpleChannelTest() {
        Log.d(TAG, "startSimpleChannelTest: ==========simple channel test start==========");

        setupTestPrograms();
        String testChannelName = "test simple 123123";
        int testChannelNumber = 123123;

        // make sure total channel list(in pm cmd manager) is sync to program info list(in data manager)
        mPesiDtvFrameworkInterface.updateCurPlayChannelList(mContext, 1);

        // get simple channel list test -s
        List<SimpleChannel> simpleChannelList = mPesiDtvFrameworkInterface.getSimpleProgramList(
                FavGroup.ALL_TV_TYPE,
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);
        List<SimpleChannel> simpleChannelListFromTotalChannels = mPesiDtvFrameworkInterface.getSimpleProgramListfromTotalChannelList(
                FavGroup.ALL_TV_TYPE,
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);
        List<SimpleChannel> curPlayChannelList = mPesiDtvFrameworkInterface.getCurPlayChannelList(
                FavGroup.ALL_TV_TYPE,
                1);

        Log.d(TAG, "startSimpleChannelTest: ==========getSimpleProgramList()==========");
        Log.d(TAG, "startSimpleChannelTest: size = " + simpleChannelList.size() + ", expect = " + TEST_CHANNEL_COUNT);
        Log.d(TAG, "startSimpleChannelTest: ==========getSimpleProgramListfromTotalChannelList()==========");
        Log.d(TAG, "startSimpleChannelTest: size = " + simpleChannelListFromTotalChannels.size() + ", expect = " + TEST_CHANNEL_COUNT);
        Log.d(TAG, "startSimpleChannelTest: ==========getCurPlayChannelList()==========");
        Log.d(TAG, "startSimpleChannelTest: size = " + curPlayChannelList.size() + ", expect = " + TEST_CHANNEL_COUNT);
        if (simpleChannelList.size() != TEST_CHANNEL_COUNT
                || simpleChannelListFromTotalChannels.size() != TEST_CHANNEL_COUNT
                || curPlayChannelList.size() != TEST_CHANNEL_COUNT) {
            Log.e(TAG, "startSimpleChannelTest: size wrong");
            return false;
        }

        if (!checkSimpleChannel(simpleChannelList, "getSimpleProgramList()")
                || !checkSimpleChannel(simpleChannelListFromTotalChannels, "getSimpleProgramListfromTotalChannelList()")
                || !checkSimpleChannel(curPlayChannelList, "getCurPlayChannelList()")) {
            Log.e(TAG, "startSimpleChannelTest: wrong simple channel data");
            return false;
        }
        // get simple channel list test -e

        // update first simple channel -s
        List<SimpleChannel> testSimpleChannelList = new ArrayList<>(simpleChannelList);
        testSimpleChannelList.get(0).set_channel_num(testChannelNumber);
        testSimpleChannelList.get(0).set_channel_name(testChannelName);
        mPesiDtvFrameworkInterface.updateSimpleChannelList(testSimpleChannelList, FavGroup.ALL_TV_TYPE);

        long channelID = testSimpleChannelList.get(0).get_channel_id();
        SimpleChannel simpleChannelByChannelId
                = mPesiDtvFrameworkInterface.getSimpleProgramByChannelId(channelID);
        SimpleChannel simpleChannelFromTotalChannel
                = mPesiDtvFrameworkInterface.getSimpleProgramByChannelIdfromTotalChannelList(channelID);
        SimpleChannel simpleChannelFromTotalChannelByGroup
                = mPesiDtvFrameworkInterface.getSimpleProgramByChannelIdfromTotalChannelListByGroup(FavGroup.ALL_TV_TYPE, channelID);

        Log.d(TAG, "startSimpleChannelTest: ==========updateSimpleChannelList(), update[0]==========");
        Log.d(TAG, "startSimpleChannelTest: ==========getSimpleProgramByChannelId()==========");
        Log.d(TAG, "startSimpleChannelTest: channelNumber = " + simpleChannelByChannelId.get_channel_num() + ", expect = " + testChannelNumber);
        Log.d(TAG, "startSimpleChannelTest: channelName = " + simpleChannelByChannelId.get_channel_name() + ", expect = " + testChannelName);
        Log.d(TAG, "startSimpleChannelTest: ==========getSimpleProgramByChannelIdfromTotalChannelList()==========");
        Log.d(TAG, "startSimpleChannelTest: channelNumber = " + simpleChannelFromTotalChannel.get_channel_num() + ", expect = " + testChannelNumber);
        Log.d(TAG, "startSimpleChannelTest: channelName = " + simpleChannelFromTotalChannel.get_channel_name() + ", expect = " + testChannelName);
        Log.d(TAG, "startSimpleChannelTest: ==========getSimpleProgramByChannelIdfromTotalChannelListByGroup()==========");
        Log.d(TAG, "startSimpleChannelTest: channelNumber = " + simpleChannelFromTotalChannelByGroup.get_channel_num() + ", expect = " + testChannelNumber);
        Log.d(TAG, "startSimpleChannelTest: channelName = " + simpleChannelFromTotalChannelByGroup.get_channel_name() + ", expect = " + testChannelName);
        if (simpleChannelByChannelId.get_channel_num() != testChannelNumber
                || simpleChannelFromTotalChannel.get_channel_num() != testChannelNumber
                || simpleChannelFromTotalChannelByGroup.get_channel_num() != testChannelNumber) {
            Log.e(TAG, "startSimpleChannelTest: channel number wrong after update");
            return false;
        }

        if (!simpleChannelByChannelId.get_channel_name().equals(testChannelName)
                || !simpleChannelFromTotalChannel.get_channel_name().equals(testChannelName)
                || !simpleChannelFromTotalChannelByGroup.get_channel_name().equals(testChannelName)) {
            Log.e(TAG, "startSimpleChannelTest: channel name wrong after update");
            return false;
        }
        // update first simple channel -e

        // update(delete) last test -s
        int preDelSize = testSimpleChannelList.size();
        channelID = testSimpleChannelList.get(preDelSize - 1).get_channel_id();
        testSimpleChannelList.remove(preDelSize - 1);
        mPesiDtvFrameworkInterface.updateSimpleChannelList(testSimpleChannelList, FavGroup.ALL_TV_TYPE);

        simpleChannelList = mPesiDtvFrameworkInterface.getSimpleProgramList(
                FavGroup.ALL_TV_TYPE,
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);
        simpleChannelListFromTotalChannels = mPesiDtvFrameworkInterface.getSimpleProgramListfromTotalChannelList(
                FavGroup.ALL_TV_TYPE,
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);
        curPlayChannelList = mPesiDtvFrameworkInterface.getCurPlayChannelList(
                FavGroup.ALL_TV_TYPE,
                1);

        Log.d(TAG, "startSimpleChannelTest: ==========updateSimpleChannelList(), delete[last]==========");
        Log.d(TAG, "startSimpleChannelTest: size = " + simpleChannelList.size() + ", expect = " + (preDelSize - 1));
        Log.d(TAG, "startSimpleChannelTest: ==========getSimpleProgramListfromTotalChannelList()==========");
        Log.d(TAG, "startSimpleChannelTest: size = " + simpleChannelListFromTotalChannels.size() + ", expect = " + (preDelSize - 1));
        Log.d(TAG, "startSimpleChannelTest: ==========getCurPlayChannelList()==========");
        Log.d(TAG, "startSimpleChannelTest: size = " + curPlayChannelList.size() + ", expect = " + (preDelSize - 1));
        if (simpleChannelList.size() != preDelSize - 1
                || simpleChannelListFromTotalChannels.size() != preDelSize - 1
                || curPlayChannelList.size() != preDelSize - 1
                || mPesiDtvFrameworkInterface.getCurPlayChannelListCnt(FavGroup.ALL_TV_TYPE) != preDelSize - 1) {
            Log.e(TAG, "startSimpleChannelTest: size wrong after update(delete)");
            return false;
        }

        simpleChannelByChannelId
                = mPesiDtvFrameworkInterface.getSimpleProgramByChannelId(channelID);
        simpleChannelFromTotalChannel
                = mPesiDtvFrameworkInterface.getSimpleProgramByChannelIdfromTotalChannelList(channelID);
        simpleChannelFromTotalChannelByGroup
                = mPesiDtvFrameworkInterface.getSimpleProgramByChannelIdfromTotalChannelListByGroup(FavGroup.ALL_TV_TYPE, channelID);
        if (simpleChannelByChannelId != null
                || simpleChannelFromTotalChannel != null
                || simpleChannelFromTotalChannelByGroup != null) {
            Log.e(TAG, "startSimpleChannelTest: simple channel still exist after delete");
            return false;
        }
        // update(delete) last test -e

        return true;
    }

    private boolean checkSimpleChannel(List<SimpleChannel> simpleChannelList, String listName) {
        Log.d(TAG, "startSimpleChannelTest: ==========" + listName + "==========");
        for (int i = 0 ; i < TEST_CHANNEL_COUNT ; i++) {
            long channelID = simpleChannelList.get(i).get_channel_id();
            int channelNumber = simpleChannelList.get(i).get_channel_num();
            String channelName = simpleChannelList.get(i).get_channel_name();

            Log.d(TAG, "checkSimpleChannel: ==========simpleChannel[" + i + "]==========");
            Log.d(TAG, "checkSimpleChannel: channelID = " + channelID + ", expect = " + (TEST_CHANNEL_ID_START + i));
            Log.d(TAG, "checkSimpleChannel: channelNumber = " + channelNumber + ", expect = " + (TEST_CHANNEL_NUM_START + i));
            Log.d(TAG, "checkSimpleChannel: channelName = " + channelName + ", expect = " + (TEST_CHANNEL_NAME_PREFIX + i));
            if (channelID != TEST_CHANNEL_ID_START + i
                    || channelNumber != TEST_CHANNEL_NUM_START + i
                    || !channelName.equals(TEST_CHANNEL_NAME_PREFIX + i)) {
                return false;
            }
        }

        return true;
    }

    // tp
    public boolean startTpInfoTest() {
        Log.d(TAG, "startTpInfoTest: tpinfo can be tested in TestTpActivity");
        return true;
    }

    // sat
    public boolean startSatInfoTest() {
        Log.d(TAG, "startSatInfoTest: satinfo can be tested in TestSatActivity");
        return true;
    }
}
