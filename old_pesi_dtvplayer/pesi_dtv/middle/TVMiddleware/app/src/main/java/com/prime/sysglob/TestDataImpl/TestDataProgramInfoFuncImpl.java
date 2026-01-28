package com.prime.sysglob.TestDataImpl;

import android.content.Context;
import android.util.Log;

import com.prime.TestData.TestData;
import com.prime.sysdata.ProgramInfo;
import com.prime.sysdata.SimpleChannel;
import com.prime.sysglob.ProgramInfoFunc;
import com.prime.tvclient.TestDataTVClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 2017/11/22.
 */

public class TestDataProgramInfoFuncImpl implements ProgramInfoFunc {
    private static final String TAG="TestDataProgramInfoFuncImpl";
    private Context context;
    private final int DVBT = 1;
    private final int DVBS = 2;
    private final int DVBC = 3;

    TestData testData = null;

    public TestDataProgramInfoFuncImpl(Context context){
        this.context = context;
        this.testData = TestDataTVClient.TestData;
    }

    @Override
    public List<ProgramInfo> GetProgramInfoList(int type) {
        List<ProgramInfo> allProgramInfo=null;
        List<ProgramInfo> resultProgramInfoInfoList = new ArrayList<>();

        allProgramInfo = testData.GetTestDataProgramInfoArray(type);

        if(allProgramInfo != null) {
            for (int i = 0; i < allProgramInfo.size(); i++) {
                if (allProgramInfo.get(i).getType() == type)
                    resultProgramInfoInfoList.add(allProgramInfo.get(i));
            }
            return resultProgramInfoInfoList;
        }
        else{
            Log.d(TAG, "allProgramInfo is null");
            return null;
        }
    }

    @Override
    public List<ProgramInfo> GetProgramInfoList(int type, String sortBy) {

        return null;
    }

    @Override
    public ProgramInfo GetProgramByChnum(int chnum, int type) {
        List<ProgramInfo> allProgramInfo = null;

        allProgramInfo = testData.GetTestDataProgramInfoArray(type);

        if(allProgramInfo != null) {
            for (int i = 0; i < allProgramInfo.size(); i++) {
                if (allProgramInfo.get(i).getDisplayNum() == chnum && allProgramInfo.get(i).getType() == type)
                return allProgramInfo.get(i);
            }
        }
        return null;
    }

    @Override
    public ProgramInfo GetProgramByLcn(int lcn, int type) {
        List<ProgramInfo> allProgramInfo = null;

        allProgramInfo = testData.GetTestDataProgramInfoArray(type);

        if(allProgramInfo != null) {
            for (int i = 0; i < allProgramInfo.size(); i++) {
                if (allProgramInfo.get(i).getDisplayNum() == lcn && allProgramInfo.get(i).getType() == type)
                return allProgramInfo.get(i);
            }
        }
        return null;
    }

    @Override
    public ProgramInfo GetProgramByChannelId(int channelId) {
        List<ProgramInfo> allTvProgramInfo = null;
        List<ProgramInfo> allRadioProgramInfo = null;

        allTvProgramInfo = testData.GetTestDataProgramInfoArray(ProgramInfo.ALL_TV_TYPE);
        allRadioProgramInfo = testData.GetTestDataProgramInfoArray(ProgramInfo.ALL_RADIO_TYPE);

        //TV
        if(allTvProgramInfo != null) {
            for (int i = 0; i < allTvProgramInfo.size(); i++) {
                if ( allTvProgramInfo.get(i).getChannelId() == channelId)
                    return allTvProgramInfo.get(i);
            }
        }
        //Radio
        if(allRadioProgramInfo != null) {
            for (int i = 0; i < allRadioProgramInfo.size(); i++) {
                if ( allRadioProgramInfo.get(i).getChannelId() == channelId)
                    return allRadioProgramInfo.get(i);
            }
        }
        return null;
    }

    @Override
    public SimpleChannel GetSimpleProgramByChannelId(int channelId) {
        ProgramInfo programInfo = GetProgramByChannelId(channelId);
        if(programInfo != null) {
            SimpleChannel simpleChannel = new SimpleChannel();
            simpleChannel.setChannelId(programInfo.getChannelId());
            simpleChannel.setChannelNum(programInfo.getDisplayNum());
            simpleChannel.setChannelName(programInfo.getDisplayName());
            simpleChannel.setUserLock(programInfo.getLock());
            simpleChannel.setCA(programInfo.getCA());
            return simpleChannel;
        }
        return null;
    }

    @Override
    public List<SimpleChannel> GetSimpleProgramList(int type) {
        List<SimpleChannel> ChnannelList = new ArrayList<>();
        List<ProgramInfo> allProgramInfo=null;
        allProgramInfo = testData.GetTestDataProgramInfoArray(type);
        if(allProgramInfo == null)
            Log.d(TAG, "GetSimpleProgramList: ===>>> IS NULL");
        else
            Log.d(TAG, "GetSimpleProgramList: ===>>> NOT NULL");
        if(allProgramInfo != null) {
            for (int i = 0; i < allProgramInfo.size(); i++) {
                if (allProgramInfo.get(i).getType() == type) {
                    SimpleChannel channel= new SimpleChannel();
                    channel.setChannelId(allProgramInfo.get(i).getChannelId());
                    channel.setChannelName(allProgramInfo.get(i).getDisplayName());
                    channel.setChannelNum(allProgramInfo.get(i).getDisplayNum());
                    channel.setUserLock(allProgramInfo.get(i).getLock());
                    channel.setCA(allProgramInfo.get(i).getCA());
                    ChnannelList.add(channel);
                }
            }
            return ChnannelList;
        }
        else{
            Log.d(TAG, "allProgramInfo is null");
            return null;
        }
    }

    @Override
    public ProgramInfo GetProgramByTripletId(int s_id, int ts_id, int on_id) {
        List<ProgramInfo> allTvProgramInfo = null;
        List<ProgramInfo> allRadioProgramInfo = null;

        allTvProgramInfo = testData.GetTestDataProgramInfoArray(ProgramInfo.ALL_TV_TYPE);
        allRadioProgramInfo = testData.GetTestDataProgramInfoArray(ProgramInfo.ALL_RADIO_TYPE);

        //TV
        if(allTvProgramInfo != null) {
            for (int i = 0; i < allTvProgramInfo.size(); i++) {
                if ( allTvProgramInfo.get(i).getServiceId() == s_id && allTvProgramInfo.get(i).getTransportStreamId() == ts_id
                        && allTvProgramInfo.get(i).getOriginalNetworkId() == on_id)
                    return allTvProgramInfo.get(i);
            }
            }
        //Radio
        if(allRadioProgramInfo != null) {
            for (int i = 0; i < allRadioProgramInfo.size(); i++) {
                if ( allRadioProgramInfo.get(i).getServiceId() == s_id && allRadioProgramInfo.get(i).getTransportStreamId() == ts_id
                        && allRadioProgramInfo.get(i).getOriginalNetworkId() == on_id)
                    return allRadioProgramInfo.get(i);
        }
        }
        return null;
    }

    @Override
    public void Save(ProgramInfo pProgram) {
        if(pProgram != null) {//eric lin 20171225 check program save param
        int type = pProgram.getType();
        List<ProgramInfo> allProgramInfo=null;
        allProgramInfo = testData.GetTestDataProgramInfoArray(type);
        if(allProgramInfo == null)
            allProgramInfo = testData.GetTestDataProgramInforSave(type);
        allProgramInfo.add(pProgram);
        }
    }

    @Override
    public void Save(List<SimpleChannel> pPrograms, int type) {
        List<ProgramInfo> temp=new ArrayList<>();
        List<ProgramInfo> allProgramInfo=null;
        allProgramInfo = testData.GetTestDataProgramInfoArray(type);
//        if(allProgramInfo == null)
//            Log.d(TAG, "Save:AAA allProgramInfo null");
//        else
//            Log.d(TAG, "Save:AAA allProgramInfo not null");
//
//        if(pPrograms == null)
//            Log.d(TAG, "Save:AAA pPrograms null");
//        else
//            Log.d(TAG, "Save:AAA pPrograms not null");
//
//        Log.d(TAG, "Save: AAA pPrograms size == " + allProgramInfo.size()
//                + "pPrograms size == " + pPrograms);
        for(int i = 0; i <  pPrograms.size(); i++) {
            for(int j = 0; j < allProgramInfo.size(); j++) {
                if (allProgramInfo.get(j).getChannelId() == pPrograms.get(i).getChannelId())
                {
//                    Log.d(TAG, "Save: channel ID1 = " + allProgramInfo.get(j).getChannelId()
//                            + " channel ID2 = " +pPrograms.get(i).getChannelId());
                    allProgramInfo.get(j).setDisplayName(pPrograms.get(i).getChannelName());
                    allProgramInfo.get(j).setDisplayNum(pPrograms.get(i).getChannelNum());
                    allProgramInfo.get(j).setLock(pPrograms.get(i).getUserLock());
                    temp.add(allProgramInfo.get(j));
                }
            }
            //Log.d(TAG, "Save: temp AAAAAAAAA");
            //temp.add(allProgramInfo.get(i));
        }
        testData.TestDataProgramInfoListSave(type, temp);

        allProgramInfo = testData.GetTestDataProgramInfoArray(type);
//        if(allProgramInfo != null)
//            Log.d(TAG, "Save:BBB allProgramInfo not null, size="+ allProgramInfo.size());
//        else
//            Log.d(TAG, "Save:BBB allProgramInfo is null");
    }

    @Override
    public void Save(List<ProgramInfo> pPrograms) {
        if(pPrograms != null) {//eric lin 20171225 check program save param
        int type = pPrograms.get(0).getType();
            List<ProgramInfo> allProgramInfo = null;

        testData.TestDataProgramInfoListSave(type, pPrograms);
        allProgramInfo = testData.GetTestDataProgramInfoArray(type);

            if (allProgramInfo != null)
                Log.d(TAG, "Save: allProgramInfo not null, size=" + allProgramInfo.size());
        else
            Log.d(TAG, "Save: allProgramInfo is null");
    }
    }

    @Override
    public void Delete(int sid, int tsid, int onid) {
        List<ProgramInfo> allProgramInfo = null;


        for (int j = 0; j < 2; j++) {
            if (j == 0)//TV
                allProgramInfo = testData.GetTestDataProgramInfoArray(ProgramInfo.ALL_TV_TYPE);
            else if (j == 1)//Radio
                allProgramInfo = testData.GetTestDataProgramInfoArray(ProgramInfo.ALL_RADIO_TYPE);
            if (allProgramInfo != null) {
                for (int i = 0; i < allProgramInfo.size(); i++) {
                    if (allProgramInfo.get(i).getServiceId() == sid
                            && allProgramInfo.get(i).getTransportStreamId() == tsid
                            && allProgramInfo.get(i).getOriginalNetworkId() == onid) {
                        Log.d(TAG, "Delete: find ProgramInfo" + "sid=" + sid + "tsid=" + tsid + "onid=" + onid);
                        allProgramInfo.remove(i);
                    } else
                        Log.d(TAG, "Delete: Can't find ProgramInfo" + "sid=" + sid + "tsid=" + tsid + "onid=" + onid);
                }
            }
        }
    }

    @Override
    public void Delete(int channelId) {
        List<ProgramInfo> allProgramInfo = null;

        for (int j = 0; j < 2; j++) {
            if (j == 0)//TV
                allProgramInfo = testData.GetTestDataProgramInfoArray(ProgramInfo.ALL_TV_TYPE);
            else if (j == 1)//Radio
                allProgramInfo = testData.GetTestDataProgramInfoArray(ProgramInfo.ALL_RADIO_TYPE);
            if (allProgramInfo != null) {
                for (int i = 0; i < allProgramInfo.size(); i++) {
                    if (allProgramInfo.get(i).getChannelId() == channelId ) {
                        Log.d(TAG, "Delete: find ProgramInfo" + "channelId=" + channelId);
                        allProgramInfo.remove(i);
                    } else
                        Log.d(TAG, "Delete: Can't find ProgramInfo" + "channelId=" + channelId);
                }
            }
        }
    }

    @Override
    public void DeleteAll(int type) {
        List<ProgramInfo> allProgramInfo = testData.GetTestDataProgramInfoArray(type);
        if(allProgramInfo != null)//eric lin 20171206 add condition
        allProgramInfo.clear();
    }
}
