package com.prime.dtvplayer.TestData.sysglob.TestDataImpl;

import android.content.Context;
import android.util.Log;

import com.prime.dtvplayer.Sysdata.FavInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.TestData.TestData.TestData;
import com.prime.dtvplayer.TestData.sysglob.ProgramInfoFunc;
import com.prime.dtvplayer.TestData.tvclient.TestDataTVClient;

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
    private ArrayList<List<SimpleChannel>> TotalChannelList = new ArrayList<List<SimpleChannel>>();

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
    public ProgramInfo GetProgramByChannelId(long channelId) {
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
    public SimpleChannel GetSimpleProgramByChannelId(long channelId) {
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
        Log.d(TAG,"DDD Save(List<SimpleChannel> pPrograms, int type): type="+type);
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

    public void DeleteFavAll(int favMode) {
        List<FavInfo> allFavInfo = testData.GetTestDataFavInfoArray(favMode);
        if(allFavInfo != null)//eric lin 20171206 add condition
            allFavInfo.clear();
    }
    public int SaveFavList(List<SimpleChannel> pPrograms, int type){
        Log.d(TAG, "SaveFavList(List<SimpleChannel> pPrograms, int type):?�type="+type+", pPrograms.size()="+pPrograms.size());
        //delete all
        DeleteFavAll(type);

        //save list
        List<FavInfo> allFavInfo = testData.GetTestDataFavInfoArray(type);
        FavInfo resultFavInfo = new FavInfo();

        if(allFavInfo != null) {
            Log.d(TAG, "SaveFavList(List<SimpleChannel> pPrograms, int type):?�allFavInfo is not null");
            for(int i=0; i<pPrograms.size(); i++){
                FavInfo favInfo = new FavInfo(pPrograms.get(i).getChannelNum(), pPrograms.get(i).getChannelId(), type);
                allFavInfo.add(favInfo);
            }
        }else {
            Log.d(TAG, "SaveFavList(List<SimpleChannel> pPrograms, int type):?�allFavInfo is null");
            allFavInfo = testData.GetTestDataFavInfoArrayForSave(type);
            for(int i=0; i<pPrograms.size(); i++){
                FavInfo favInfo = new FavInfo(pPrograms.get(i).getChannelNum(), pPrograms.get(i).getChannelId(), type);
                allFavInfo.add(favInfo);
            }
        }
        Log.d(TAG, "SaveFavList(List<SimpleChannel> pPrograms, int type):?�type="+type+", after save size="+allFavInfo.size());
        return 0;
    }
    public int saveSimpleChannelList(List<SimpleChannel> pPrograms, int type) {
        List<ProgramInfo> temp=new ArrayList<>();
        List<ProgramInfo> allProgramInfo=null;
        Log.d(TAG,"DDD saveSimpleChannelList(List<SimpleChannel> pPrograms, int type): type="+type);
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

        if(type == ProgramInfo.ALL_TV_TYPE || type == ProgramInfo.ALL_RADIO_TYPE) {
            for (int i = 0; i < pPrograms.size(); i++) {
                for (int j = 0; j < allProgramInfo.size(); j++) {
                    if (allProgramInfo.get(j).getChannelId() == pPrograms.get(i).getChannelId()) {
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
        }else {
            SaveFavList(pPrograms, type);
        }

        return 0;
//        allProgramInfo = testData.GetTestDataProgramInfoArray(type);
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
    public void Delete(long channelId) {
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

    //
    public List<FavInfo> GetFavInfoList(int favMode) {
        List<FavInfo> allFavInfo = testData.GetTestDataFavInfoArray(favMode);
        List<FavInfo> resultFavInfoList = new ArrayList<>();

        if(allFavInfo != null) {
            for (int i = 0; i < allFavInfo.size(); i++) {
                if (allFavInfo.get(i).getFavMode() == favMode)
                    resultFavInfoList.add(allFavInfo.get(i));
            }
            return resultFavInfoList;
        }
        else{
            Log.d(TAG, "allFavInfo is null");
            return null;
        }
    }

    public List<SimpleChannel> GetSimpleProgramListForTestData(int type,int IncludeSkipFlag,int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        List<SimpleChannel> ChnannelList = new ArrayList<>();
        List<ProgramInfo> allProgramInfo=null;

        if(type == ProgramInfo.ALL_TV_TYPE || type == ProgramInfo.ALL_RADIO_TYPE) {
            allProgramInfo = GetProgramInfoList(type);
            if (allProgramInfo == null) {
                Log.d(TAG, "DDD BK1 GetSimpleProgramList: ===>>> IS NULL" + ", type=" + type);
                return null;
            } else
                Log.d(TAG, "DDD BK1 GetSimpleProgramList: ===>>> NOT NULL" + ", type=" + type);
            if (allProgramInfo != null) {
                for (int i = 0; i < allProgramInfo.size(); i++) {
                    if (allProgramInfo.get(i).getType() == type) {
                        SimpleChannel channel = new SimpleChannel();
                        channel.setChannelId(allProgramInfo.get(i).getChannelId());
                        channel.setChannelName(allProgramInfo.get(i).getDisplayName());
                        channel.setChannelNum(allProgramInfo.get(i).getDisplayNum());
                        channel.setUserLock(allProgramInfo.get(i).getLock());
                        channel.setCA(allProgramInfo.get(i).getCA());
                        channel.setPVRSkip(0);
                        ChnannelList.add(channel);
                    }
                }
                return ChnannelList;
            } else {
                Log.d(TAG, "allProgramInfo is null");
                return null;
            }
        }else{
            if(type >= ProgramInfo.TV_FAV1_TYPE &&  type <= ProgramInfo.TV_FAV6_TYPE)
                allProgramInfo = GetProgramInfoList(ProgramInfo.ALL_TV_TYPE);
            else if(type >= ProgramInfo.RADIO_FAV1_TYPE &&  type <= ProgramInfo.RADIO_FAV2_TYPE)
                allProgramInfo = GetProgramInfoList(ProgramInfo.ALL_RADIO_TYPE);
                if (allProgramInfo == null) {
                    Log.d(TAG, "DDD BK2 GetSimpleProgramList: ===>>> IS NULL" + ", type=" + type);
                    return null;
                } else
                    Log.d(TAG, "DDD BK2 GetSimpleProgramList: ===>>> NOT NULL" + ", type=" + type);
                if (allProgramInfo != null) {
                    List<FavInfo> favInfoList = GetFavInfoList(type);
                    if(favInfoList != null) {
                        for (int i = 0; i < favInfoList.size(); i++) {
                            SimpleChannel channel = new SimpleChannel();
                            if (favInfoList.get(i).getFavMode() == type) {
                                channel.setChannelId(favInfoList.get(i).getChannelId());
                                channel.setChannelNum(favInfoList.get(i).getFavNum());
                                for (int j = 0; j < allProgramInfo.size(); j++) {
                                    if(allProgramInfo.get(j).getChannelId() == favInfoList.get(i).getChannelId()) {
                                        channel.setChannelName(allProgramInfo.get(j).getDisplayName());
                                        channel.setUserLock(allProgramInfo.get(j).getLock());
                                        channel.setCA(allProgramInfo.get(j).getCA());
                                        channel.setPVRSkip(0);
                                        ChnannelList.add(channel);
                                        break;
                                    }
                                }
                            }
                        }
                        return ChnannelList;
                    }else
                        return null;
                }else
                    return null;
        }
    }



    public List<SimpleChannel> GetSimpleProgramList(int type,int IncludeSkipFlag,int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function
//        if(PLATFORM == PLATFORM_PESI)
//            return GetSimpleProgramListForPesi(type, IncludeSkipFlag, IncludePVRSkipFlag);
//        else
//            return GetSimpleProgramListForHisi(type, IncludeSkipFlag, IncludePVRSkipFlag);
        return GetSimpleProgramListForTestData(type, IncludeSkipFlag, IncludePVRSkipFlag);//Scoty 20180615 recover get simple channel list function
    }
    public void UpdateCurPlayChannelList(int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        Log.e(TAG, "DDD UpdateCurPlayChannelList first");
        if(TotalChannelList.size() !=0)
            TotalChannelList.clear();
        for( int i = 0; i < ProgramInfo.ALL_TV_RADIO_TYPE_MAX;i++)
        {
            TotalChannelList.add(GetSimpleProgramList(i,0,IncludePVRSkipFlag));//Scoty 20180615 recover get simple channel list function
            if(TotalChannelList.get(i)!= null)
                Log.d(TAG, "DDD UpdateCurPlayChannelList:    TotalChannelList  group " + i + "    size = " + TotalChannelList.get(i).size());
            else
                Log.d(TAG, "DDD UpdateCurPlayChannelList:    TotalChannelList  group " + i + " is NULL !!!!!!!!!!");

        }
    }
//    public List<SimpleChannel> GetCurPlayChannelList(int type, int IncludePVRSkipFlag) {
//        List<SimpleChannel> simpleChannelList = null;
//
//        simpleChannelList = GetSimpleProgramList(type);
//
//        if(type < ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {
//            if(simpleChannelList!=null)
//                Log.d(TAG, "GetCurPlayChannelList:  totalList   " + type + "   size = " + simpleChannelList.size());
//            if(simpleChannelList.size() > 0)
//                simpleChannelList = new ArrayList<>();
//            for(int i = 0; i < simpleChannelList.size(); i++) {
//                if(IncludePVRSkipFlag == 1)
//                    simpleChannelList.add(simpleChannelList.get(i));
//                else if(TotalChannelList.get(type).get(i).getPVRSkip() == 0)
//                    simpleChannelList.add(TotalChannelList.get(type).get(i));
//            }
//        }
//        return simpleChannelList;
//    }
    public List<SimpleChannel> GetCurPlayChannelList(int type, int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        List<SimpleChannel> simpleChannelList = null;

        if(type < ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {
            if(TotalChannelList.get(type)!=null) {
                Log.d(TAG, "DDD GetCurPlayChannelList: TotalChannelList.get(type)!=null, type=" + type + ", size=" + TotalChannelList.get(type).size());
                if(TotalChannelList.get(type).size() > 0)
                    simpleChannelList = new ArrayList<>();
                for(int i = 0; i < TotalChannelList.get(type).size(); i++) {
                    if(IncludePVRSkipFlag == 1)
                        simpleChannelList.add(TotalChannelList.get(type).get(i));
                    else if(TotalChannelList.get(type).get(i).getPVRSkip() == 0)
                        simpleChannelList.add(TotalChannelList.get(type).get(i));
                }
            }
            else {
                Log.d(TAG, "DDD GetCurPlayChannelList: TotalChannelList.get(type) =null, type=" + type);
                return null;
            }
        }
        return simpleChannelList;
    }

}
