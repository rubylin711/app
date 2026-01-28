package com.mtest.module;

import android.content.Context;
import android.util.Log;

import com.dolphin.dtv.HiDtvMediaPlayer;
import com.mtest.config.MtestConfig;
import com.mtest.utils.TunerView;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.utils.TVScanParams;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TunerModule
{
    private static final String TAG = TunerModule.class.getSimpleName();
    private static final int CMD_SUCCESS = HiDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS;
    private static final int CMD_FAIL = HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    private static final int RESULT_PASS = MtestConfig.TEST_RESULT_PASS;
    private static final int RESULT_FAIL = MtestConfig.TEST_RESULT_FAIL;
    private static final int STATUS_LOCK = 1;
    private final WeakReference<Context> mContRef;

    public TunerModule(Context context)
    {
        mContRef = new WeakReference<>(context);
    }

    private static int getReturn(int ret)
    {
        return (ret == CMD_SUCCESS) ?
                RESULT_PASS :
                RESULT_FAIL ;
    }

    private static int getReturn(boolean success)
    {
        return success ?
                RESULT_PASS :
                RESULT_FAIL ;
    }

    // mtest av play must use this
    // because AvControlPlayByChannelID may have problem after split play
    public int AvSinglePlay(int tunerID, long channelID)
    {
        Log.d(TAG, "AvSinglePlay: ");
        int ret = 0;

        if (tunerID >= 0 && channelID >= 0)
        {
            List<Integer> tunerIDs = new ArrayList<>();
            List<Long> channelIDs = new ArrayList<>();

            tunerIDs.add(tunerID);
            channelIDs.add(channelID);

            ret = AvStopAll(0);
            if (ret != CMD_SUCCESS)
            {
                Log.d(TAG, "AvSinglePlay: AvStopAll fail , result = " + ret);
                return ret;
            }

            ret = AvMultiPlay(1, tunerIDs, channelIDs);
            if (ret != CMD_SUCCESS)
            {
                Log.d(TAG, "AvSinglePlay: AvMultiPlay fail , result = " + ret);
                return ret;
            }
        }
        Log.d(TAG, "AvSinglePlay: success");
        return CMD_SUCCESS;
    }

    public int AvMultiPlay(int tunerNum, List<Integer> tunerIDs, List<Long> channelIDs)
    {
        Log.d(TAG, "AvMultiPlay: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.MtestTestAvMultiPlay(tunerNum, tunerIDs, channelIDs);
    }

    public int AvSplitPlay(List<TunerView> tunerList)
    {
        Log.d(TAG, "AvSplitPlay: ");
        List<Integer> tunerIDs = new ArrayList<>();
        List<Long> channelIDs = new ArrayList<>();
        boolean canPlay = false;
        int ret = 0;
        int tunerNum = getTunerNum();

        for (int i = 0 ; i < tunerNum && i < 4 ; i++) // only support 4 tuners now
        {
            long channelID = getPlayableChannelIDByTpID(tunerList.get(i).getTpID());
            if (!canPlay && channelID >= 0) // can play if one of the tuners has a playable channelID
            {
                canPlay = true;
            }

            tunerIDs.add(i);
            channelIDs.add(channelID);
        }

        if (canPlay)
        {
            ret = AvStopAll(0);
            if (ret != CMD_SUCCESS)
            {
                Log.d(TAG, "AvSplitPlay: AvStopAll fail , ret = " + ret);
                return ret;
            }

            ret = AvMultiPlay(tunerNum, tunerIDs, channelIDs);
            if (ret != CMD_SUCCESS)
            {
                Log.d(TAG, "AvSplitPlay: AvMultiPlay fail , ret = " + ret);
                return ret;
            }
        }
        Log.d(TAG, "AvSplitPlay: success");
        return CMD_SUCCESS;
    }

    public int AvStopAll(int playID)
    {
        Log.d(TAG, "AvStopAll: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        int ret = CMD_FAIL;

        ret = activity.MtestTestAvStopByTunerID(0);
        if (ret != CMD_SUCCESS)
            return ret;

        ret = activity.MtestTestAvStopByTunerID(1);
        if (ret != CMD_SUCCESS)
            return ret;

        ret = activity.MtestTestAvStopByTunerID(2);
        if (ret != CMD_SUCCESS)
            return ret;

        ret = activity.MtestTestAvStopByTunerID(3);
        if (ret != CMD_SUCCESS)
            return ret;

        return ret;
    }

    public int AvStopByTunerID(int tunerID)
    {
        Log.d(TAG, "AvStopByTunerID: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.MtestTestAvStopByTunerID(tunerID);
    }

    public TpInfo TpInfoGet(int tpId)
    {
        Log.d(TAG, "getTpInfo: tpId = " + tpId);
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.TpInfoGet(tpId);
    }

    public int TpInfoUpdate(TpInfo tpInfo)
    {
        Log.d(TAG, "TpInfoUpdate: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.TpInfoUpdate(tpInfo);
    }

    public void ScanParamsStartScan(int tunerId, int tpId, int satId, int scanMode, int searchOptionTVRadio, int searchOptionCaFta, int nitSearch, int oneSegment)
    {
        Log.d(TAG, "ScanParamsStartScan: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        TpInfo tpInfo = activity.TpInfoGet(tpId);
        //Log.d(TAG,"ScanParamsStartScan: freq["+tpInfo.CableTp.getFreq()+"] sym["+tpInfo.CableTp.getSymbol()+"] qam["+tpInfo.CableTp.getQam()+"]");
        TVScanParams scanParams = new TVScanParams(tunerId, tpInfo, satId, scanMode, searchOptionTVRadio, searchOptionCaFta, nitSearch, oneSegment);
        activity.startScan(scanParams); // void
    }

    public void ScanParamsStopScan(boolean store)
    {
        Log.d(TAG, "ScanParamsStopScan: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        activity.stopScan(store); // void
    }

    public void ScanResultSetChannel()
    {
        Log.d(TAG, "ScanResultSetChannel: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        activity.ScanResultSetChannel(); // success(1) , fail(0)
    }

    public int checkTuner(int tunerID, int playID)
    {
        Log.d(TAG, "checkTuner: ");
        boolean success = isLock(tunerID) && isLivePlay(playID);
        Log.d(TAG, "checkTuner: success = " + success);
        return getReturn(success);
    }

    public int getTunerNum()
    {
        Log.d(TAG, "getTunerNum: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.GetTunerNum();
    }

    public int getPlayStatus(int playID)
    {
        Log.d(TAG, "getPlayStatus: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.AvControlGetPlayStatus(playID);
    }

    public int getLockStatus(int tunerID)
    {
        Log.d(TAG, "getLockStatus: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.TunerGetLockStatus(tunerID);
    }

    public int getQuality(int tunerID)
    {
        Log.d(TAG, "getQuality: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.TunerGetQuality(tunerID);
    }

    public int getStrength(int tunerID)
    {
        Log.d(TAG, "getStrength: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.TunerGetStrength(tunerID);
    }

    public int getErrorFrameCount(int tunerID)
    {
        Log.d(TAG, "getErrorFrameCount: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.MtestGetErrorFrameCount(tunerID);
    }

    public int getFrameDropCount(int tunerID)
    {
        Log.d(TAG, "getFrameDropCount: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.MtestGetFrameDropCount(tunerID);
    }

    public String getBER(int tunerID)
    {
        Log.d(TAG, "getBER: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.TunerGetBER(tunerID);
    }

    public boolean isLock(int tunerID)
    {
        Log.d(TAG, "isLock: ");
        return getLockStatus(tunerID) == STATUS_LOCK;
    }

    public boolean isLivePlay(int playID)
    {
        Log.d(TAG, "isLivePlay: ");
        int LIVEPLAY = HiDtvMediaPlayer.EnPlayStatus.LIVEPLAY.getValue();
        return getPlayStatus(playID) == LIVEPLAY;
    }

    // find playable TV channel
    // for now it means first TV channel of a tp
    private long getPlayableChannelIDByTpID(int tpID)
    {
        Log.d(TAG, "getPlayableChannelIDByTpID: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        long channelID = 0;

        List<SimpleChannel> simpleChannelList = activity.MtestGetSimpleChannelListByTpID(tpID);
        if (simpleChannelList.isEmpty())
            Log.d(TAG, "getPlayableChannelIDByTpID: simpleChannelList isEmpty");
        else
            channelID = simpleChannelList.get(0).getChannelId(); // first channel

        return channelID;
    }
}
