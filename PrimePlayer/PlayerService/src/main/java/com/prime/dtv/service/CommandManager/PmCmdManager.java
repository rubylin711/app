package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.prime.datastructure.sysdata.MailData;
import com.prime.dtv.Interface.PesiDtvFrameworkInterface;
import com.prime.dtv.PesiDtvFramework;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.ChannelNode;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.Interface.BaseManager;
import com.prime.dtv.service.database.dvbdatabasetable.MailDatabaseTable;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.datastructure.sysdata.DefaultChannel;
import com.prime.datastructure.sysdata.FavGroupName;
import com.prime.datastructure.sysdata.FavInfo;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.NetProgramInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.ProgramPlayStreamType;
import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.SimpleChannel;
import com.prime.datastructure.sysdata.TpInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PmCmdManager extends BaseManager {
    private static final String TAG = "PmCmdManager";
    private final ArrayList<List<SimpleChannel>> mTotalChannelList = new ArrayList<>();
    private final ArrayList<FavGroupName> mAllProgramGroup = new ArrayList<>();
    private final DataManager mDataManager;
    private Context mContext;

    private static final String RecordFileTest = "/data/vendor/dtvdata/RecordTest.ts";

    public PmCmdManager(Context context, Handler handler) {
        super(context, TAG, handler, PmCmdManager.class);
        mContext = context;
        mDataManager = DataManager.getDataManager(context);

        updateCurPlayChannelList(context, 1);
    }

    /*
     * channel & program
     */
    public ArrayList<List<SimpleChannel>> getProgramManagerTotalChannelList() {
        return mTotalChannelList;
    }

    public ArrayList<FavGroupName> getAllProgramGroup() {
        return mAllProgramGroup;
    }

    public List<ProgramInfo> getProgramInfoList(int type, int pos, int num) {
        return mDataManager.getProgramInfoList(type, pos, num);
        // LogUtils.d("Type = "+ type);
        // if (isGroupTypeInvalid(type)
        // || (pos < 0 && pos != MiscDefine.ProgramInfo.POS_ALL)
        // || (num < 0 && num != MiscDefine.ProgramInfo.NUM_ALL)
        // || (pos == MiscDefine.ProgramInfo.POS_ALL && num !=
        // MiscDefine.ProgramInfo.NUM_ALL)
        // || (num == MiscDefine.ProgramInfo.NUM_ALL && pos !=
        // MiscDefine.ProgramInfo.POS_ALL)) {
        // Log.e(TAG, "getProgramInfoList: invalid input param");
        // return new ArrayList<>();
        // }
        //
        // //List<ProgramInfo> allProgramInfoList = mDataManager.getProgramInfoList();
        // List<ProgramInfo> foundListByType = new ArrayList<>();
        // FavGroup favGroup = mDataManager.getFavGroupList().get(type);
        // LogUtils.d("fav info num = "+ favGroup.getFavInfoList().size());
        // for(FavInfo favInfo : favGroup.getFavInfoList()){
        // ProgramInfo programInfo = getProgramByChannelId(favInfo.getChannelId());
        // if(programInfo != null){
        // foundListByType.add(programInfo);
        // }
        // }
        // /*
        // // filter by type
        // for (ProgramInfo programInfo : allProgramInfoList) {
        // if (programInfo.getType() == type) {
        // foundListByType.add(programInfo);
        // }
        // }*/
        //
        // List<ProgramInfo> result;
        // int size = foundListByType.size();
        // // return all found program if pos = POS_ALL and num = NUM_ALL
        // // In fact, if pos == MiscDefine.ProgramInfo.POS_ALL,
        // // condition 'num == MiscDefine.ProgramInfo.NUM_ALL' is always 'true' here
        // if (pos == MiscDefine.ProgramInfo.POS_ALL && num ==
        // MiscDefine.ProgramInfo.NUM_ALL) {
        // result = new ArrayList<>(foundListByType);
        // }
        // // return empty list if no program found or pos is out of bond
        // else if (foundListByType.isEmpty() || pos >= size) {
        // result = new ArrayList<>();
        // }
        // // return #num program from pos
        // else {
        // int toIndex = Math.min(pos + num, size); // avoid toIndex out of bond
        // result = new ArrayList<>(foundListByType.subList(pos, toIndex));
        // }
        //
        // return result;
    }

    public ProgramInfo getProgramByChannelId(long channelId) {
        ProgramInfo programInfo = mDataManager.getProgramInfo(channelId);
        return programInfo == null ? null : new ProgramInfo(programInfo);
    }

    public ProgramInfo getProgramBySIdOnIDTsId(int sid, int onid, int tsid) {
        ProgramInfo programInfo = mDataManager.getProgramInfo(sid, tsid, onid);
        return programInfo == null ? null : new ProgramInfo(programInfo);
    }

    public ProgramInfo getProgramByChnum(int chnum, int type) {
        List<ProgramInfo> programInfoList = getProgramInfoList(type, MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);

        for (ProgramInfo programInfo : programInfoList) {
            if (programInfo.getDisplayNum() == chnum) {
                return new ProgramInfo(programInfo);
            }
        }

        return null;
    }

    public ProgramInfo getProgramByLcn(int lcn, int type) {
        List<ProgramInfo> programInfoList = getProgramInfoList(type, MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);

        for (ProgramInfo programInfo : programInfoList) {
            if (programInfo.getLCN() == lcn) {
                return new ProgramInfo(programInfo);
            }
        }

        return null;
    }

    public ProgramInfo getProgramByServiceIdTransportStreamId(int service_id, int ts_id) {
        ProgramInfo programInfo = mDataManager.getProgramInfo(service_id, ts_id);
        return programInfo == null ? null : new ProgramInfo(programInfo);
    }

    public ProgramInfo getProgramByServiceId(int service_id) {
        ProgramInfo programInfo = mDataManager.getProgramInfo(service_id);
        return programInfo == null ? null : new ProgramInfo(programInfo);
    }

    public int updateProgramInfo(ProgramInfo programInfo) {
        if (programInfo == null) {
            Log.e(TAG, "updateProgramInfo: update null programInfo");
            return -1;
        }

        mDataManager.updateProgramInfo(programInfo);
        return 0;
    }

    public int updateSimpleChannelList(List<SimpleChannel> simpleChannelList, int type) {
        if (simpleChannelList == null || isGroupTypeInvalid(type)) {
            Log.e(TAG, "updateSimpleChannelList: invalid param");
            return -1;
        }

        if (type >= mTotalChannelList.size()) {
            Log.e(TAG, "updateSimpleChannelList: mTotalChannelList size wrong");
            return -1;
        }

        // update ProgramInfo or Fav in DataManager
        if (type == FavGroup.ALL_TV_TYPE || type == FavGroup.ALL_RADIO_TYPE) {
            // update ProgramInfo by simpleChannelList
            // those are not exist in simpleChannelList should be deleted
            mDataManager.updateProgramInfoList(simpleChannelList, type);
        } else { // fav
            // clear all fav in group and re add
            mDataManager.delAllFavInfo(type);
            for (SimpleChannel simpleChannel : simpleChannelList) {
                FavInfo favInfo = new FavInfo(simpleChannel.get_channel_num(), simpleChannel.get_channel_id(), type);
                mDataManager.addFavInfo(type, favInfo);
            }
        }

        updateCurPlayChannelList(mContext, 1);
        return 0;
    }

    public void deleteProgram(long channelId) {
        mDataManager.delProgramInfo(channelId);
    }

    public int saveTable(EnTableType option) {
        Log.d(TAG, "saveTable: option table type = " + option);
        switch (option) {
            case ALL:
                mDataManager.DataManagerSaveData(DataManager.SV_ALL);
                break;
            case TP:
                mDataManager.DataManagerSaveData(DataManager.SV_TP_INFO);
                break;
            case GPOS:
                mDataManager.DataManagerSaveData(DataManager.SV_GPOS);
                break;
            case GROUP:
                mDataManager.DataManagerSaveData(DataManager.SV_FAV_INFO);
                break;
            case TIMER:
                mDataManager.DataManagerSaveData(DataManager.SV_BOOK_INFO);
                break;
            case PROGRAME:
                mDataManager.DataManagerSaveData(DataManager.SV_PROGRAM_INFO);
                break;
            case SATELLITE:
                mDataManager.DataManagerSaveData(DataManager.SV_SAT_INFO);
                break;
            default:
                Log.e(TAG, "saveTable: unsupported table type");
                break;
        }

        return 0;
    }

    public int saveGposKeyValue(String key, int value) {
        mDataManager.DataManagerSaveGposData(key, value);
        return 0;
    }

    public int saveGposKeyValue(String key, String value) {
        mDataManager.DataManagerSaveGposData(key, value);
        return 0;
    }

    public int saveGposKeyValue(String key, long value) {
        mDataManager.DataManagerSaveGposData(key, value);
        return 0;
    }

    public int setDefaultOpenChannel(long channelId, int groupType) {
        GposInfo gposInfo = mDataManager.getGposInfo();
        GposInfo.setStartOnChannelId(mContext, channelId);
        GposInfo.setStartOnChType(mContext, groupType);
        GposInfo.setCurChannelId(mContext, channelId);
        GposInfo.setCurGroupType(mContext, groupType);
        mDataManager.updateGposInfo(gposInfo);

        // --- Sync with CNSLauncher ---
        try {
            ProgramInfo programInfo = mDataManager.get_service_by_channelId(channelId);
            if (programInfo != null) {
                String lastChannelId = programInfo.getServiceId() + " # " + programInfo.getTransportStreamId() + " # "
                        + programInfo.getOriginalNetworkId();

                Log.d(TAG, "Sync to CNSLauncher: " + lastChannelId);
                GposInfo.setLastPlayChannelId(mContext, lastChannelId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to sync last channel to Settings.System", e);
        }
        // -----------------------------

        return 0;
    }

    public DefaultChannel getDefaultChannel() {
        GposInfo gposInfo = mDataManager.getGposInfo();
        long defaultChannelId = GposInfo.getStartOnChannelId(mContext);
        int defaultChannelType = GposInfo.getStartOnChType(mContext);
        if (isChannelIDInvalid(defaultChannelId)) {
            // use CurChannelId if no StartOnChannelId
            defaultChannelId = GposInfo.getCurChannelId(mContext);
            defaultChannelType = GposInfo.getCurGroupType(mContext);
        }

        if (isChannelIDInvalid(defaultChannelId)) {
            return null;
        } else {
            DefaultChannel defaultChannel = new DefaultChannel();
            defaultChannel.setChanneId(defaultChannelId);
            defaultChannel.setGroupType(defaultChannelType);
            return defaultChannel;
        }
    }

    public ChannelNode getChannelByID(long channelId) {
        ProgramInfo programInfo = mDataManager.getProgramInfo(channelId);
        return getChannelNodeFromProgramInfo(programInfo);
    }

    private ChannelNode getChannelNodeFromProgramInfo(ProgramInfo programInfo) {
        if (programInfo == null) {
            return null;
        }

        ChannelNode channelNode = new ChannelNode();
        channelNode.channelID = programInfo.getChannelId();
        channelNode.OrignalServiceName = programInfo.getDisplayName();
        channelNode.TSID = programInfo.getTransportStreamId();
        channelNode.bCAMode = programInfo.getCA();
        channelNode.LCN = programInfo.getLCN();
        int lock = programInfo.getLock();
        int skip = programInfo.getSkip();
        int delete = 0; // no delete in ProgramInfo
        int move = 0; // no move in ProgramInfo
        channelNode.editTag = lock | (skip << 1) | (delete << 2) | (move << 3);
        channelNode.tempFlag = 0; // no temp flag in ProgramInfo
        channelNode.favorTag = 0; // no favor tag in ProgramInfo
        channelNode.origNetworkID = programInfo.getOriginalNetworkId();
        channelNode.serviceID = programInfo.getServiceId();
        channelNode.serviceType = programInfo.getType();
        channelNode.HasScheduleEPG = 0; // TODO: complete this when EPG is ready
        channelNode.HasPFEPG = 0; // TODO: complete this when EPG is ready
        channelNode.AudPid = programInfo.pAudios.isEmpty() ? 0 : programInfo.pAudios.get(0).getPid();
        channelNode.VidPid = programInfo.pVideo.getPID();
        channelNode.PmtPid = 0; // no pmt pid in ProgramInfo
        channelNode.PcrPID = programInfo.getPcr();
        channelNode.VidType = programInfo.pVideo.getCodec();
        channelNode.AudType = programInfo.pAudios.isEmpty() ? 0 : programInfo.pAudios.get(0).getCodec();

        channelNode.deliveryID = programInfo.getSatId();
        channelNode.TPID = programInfo.getTpId();
        channelNode.volTrack.Volume = GposInfo.getVolume(mContext);
        channelNode.volTrack.AudioChannel = programInfo.getAudioLRSelected();
        channelNode.volTrack.AudioIndex = programInfo.getAudioSelected();
        channelNode.AudioNum = programInfo.pAudios.size();

        for (int i = 0; i < channelNode.AudioNum; i++) {
            channelNode.esidAudioStream[i].Type = programInfo.pAudios.get(i).getCodec();
            channelNode.esidAudioStream[i].Pid = programInfo.pAudios.get(i).getPid();
            channelNode.esidAudioStream[i].AudioType = 0;
            channelNode.esidAudioStream[i].TrackMode = 0;
            channelNode.esidAudioStream[i].szLangCode = programInfo.pAudios.get(i).getLeftIsoLang();
        }

        channelNode.SubtNum = programInfo.pSubtitle.size();

        for (int i = 0; i < channelNode.SubtNum; i++) {
            // channelNode.esidSubtitleInfo[i].Type =
            // programInfo.pSubtitle.get(i).getType();
            channelNode.esidSubtitleInfo[i].Pid = programInfo.pSubtitle.get(i).getPid();
            channelNode.esidSubtitleInfo[i].szLangCode = programInfo.pSubtitle.get(i).getLang();
        }

        channelNode.TTX_SubtNum = programInfo.pTeletext.size();

        for (int i = 0; i < channelNode.TTX_SubtNum; i++) {
            channelNode.esidTeletext[i].Type = programInfo.pTeletext.get(i).getType();
            channelNode.esidTeletext[i].Pid = programInfo.pTeletext.get(i).getPid();
            channelNode.esidTeletext[i].szLangCode = programInfo.pTeletext.get(i).getLang();
        }

        return channelNode;
    }

    public void updateCurPlayChannelList(Context context, int includePVRSkipFlag) {
        // clear and rebuild
        // mTotalChannelList.clear();
        // for (int i = ProgramInfo.ALL_TV_TYPE; i < ProgramInfo.ALL_TV_RADIO_TYPE_MAX;
        // i++) {
        // List<SimpleChannel> simpleChannelList = getSimpleProgramList(i, 0,
        // includePVRSkipFlag);
        // mTotalChannelList.add(simpleChannelList);
        // }

        Log.e(TAG, "exce update_cur_play_channel_list Net first");
        if (mTotalChannelList.size() != 0)
            mTotalChannelList.clear();

        DataManager mDataManager = DataManager.getDataManager(context);
        // DataManager.ProgramDatabase mProgramDatabase =
        // mDataManager.GetProgramDatabase();
        DataManager.NetProgramDatabase mNetProgramDatabase = mDataManager.GetNetProgramDatabase();

        // for (int i = 0; i < FavGroup.ALL_TV_RADIO_TYPE_MAX; i++) {
        // ////need change to get simple channel list form service to compatible with
        // now version
        // List<SimpleChannel> simpleChannelList = getSimpleProgramList(i, 0,
        // includePVRSkipFlag);//Get ProgramInfo List from Server
        //// List<SimpleChannel> simpleChannelList =
        // mProgramDatabase.GetSimpleChannelList(context); //Get ProgramInfo List from
        // ProgramInfo Database
        // List<SimpleChannel> netSimpleChannelList =
        // mNetProgramDatabase.GetNetSimpleChannelList(context);
        // if (simpleChannelList.size() > 0 && i < FavGroup.ALL_RADIO_TYPE) {//No
        // Considering Radio Channel
        // Log.d(TAG, "exce update_cur_play_channel_list: netprogram.Size = [" +
        // netSimpleChannelList.size() + "]");
        // for (int j = 0; j < netSimpleChannelList.size(); j++) {
        // simpleChannelList.add(update_net_program_epg_data(j,
        // netSimpleChannelList.get(j)));//add vod/youtube netprogram after program
        // }
        // }
        // mTotalChannelList.add(simpleChannelList);
        // }
        //
        // for (int ii = 0; ii < mTotalChannelList.size(); ii++)
        // for (int j = 0; j < mTotalChannelList.get(ii).size(); j++)
        // Log.d(TAG, "exce update_cur_play_channel_list: i = [" + ii + "] " + "= [" +
        // mTotalChannelList.get(ii).get(j).get_channel_name() + "] = [" +
        // mTotalChannelList.get(ii).get(j).get_channel_id() + "] = [" +
        // mTotalChannelList.get(ii).get(j).get_play_stream_type() + "] = [" +
        // mTotalChannelList.get(ii).get(j).get_channel_num() + "]");

    }

    public void updateCurPlayChannelList(int includePVRSkipFlag) {
        // clear and rebuild
        mTotalChannelList.clear();
        for (int i = FavGroup.ALL_TV_TYPE; i < FavGroup.ALL_TV_RADIO_TYPE_MAX; i++) {
            List<SimpleChannel> simpleChannelList = getSimpleProgramList(i, 0, includePVRSkipFlag);
            mTotalChannelList.add(simpleChannelList);
        }
    }

    public void resetTotalChannelList() {
        for (List<SimpleChannel> simpleChannelList : mTotalChannelList) {
            simpleChannelList.clear();
        }
    }

    public List<SimpleChannel> getCurPlayChannelList(int type, int includePVRSkipFlag) {
        if (isGroupTypeInvalid(type)) {
            return new ArrayList<>();
        }

        return mTotalChannelList.get(type);
    }

    public int getCurPlayChannelListCnt(int type) {
        if (isGroupTypeInvalid(type)) {
            return 0;
        }

        return mTotalChannelList.get(type).size();
    }

    public List<SimpleChannel> getSimpleProgramList(int type, int includeSkipFlag, int includePVRSkipFlag) {
        if (isGroupTypeInvalid(type)) {
            Log.d(TAG, "getSimpleProgramList: invalid type");
            return new ArrayList<>();
        }

        List<SimpleChannel> simpleChannelList = new ArrayList<>();
        if (type == FavGroup.ALL_TV_TYPE || type == FavGroup.ALL_RADIO_TYPE) {
            Log.d(TAG, "getSimpleProgramList GroupType = " + type);
            for (ProgramInfo programInfo : mDataManager.getProgramInfoList()) {
                if (programInfo.getType() == type
                        && mDataManager.skip_service_by_SkipPvrSkip(programInfo, includeSkipFlag,
                                includePVRSkipFlag) == 0) {
                    // Scoty 20180613 change get simplechannel list for PvrSkip rule -s
                    SimpleChannel channel = getSimpleChannelFromProgramInfo(programInfo);
                    simpleChannelList.add(channel);
                }
            }
            // Scoty 20180613 change get simplechannel list for PvrSkip rule -e
        } else { // fav
            for (FavInfo favInfo : mDataManager.getFavInfoList(type)) {
                ProgramInfo programInfo = mDataManager.getProgramInfo(favInfo.getChannelId());
                if (programInfo != null
                        && mDataManager.skip_service_by_SkipPvrSkip(programInfo, includeSkipFlag,
                                includePVRSkipFlag) == 0) {
                    SimpleChannel channel = getSimpleChannelFromProgramInfo(programInfo);
                    simpleChannelList.add(channel);
                }
            }
        }
        return simpleChannelList;
    }

    private SimpleChannel getSimpleChannelFromProgramInfo(ProgramInfo programInfo) {
        if (programInfo == null) {
            return null;
        }

        SimpleChannel channel = new SimpleChannel();
        channel.set_channel_id(programInfo.getChannelId());
        if (Pvcfg.channelNumberUseLcn()) { // displayNum or LCN by config
            channel.set_channel_num(programInfo.getLCN());
        } else {
            channel.set_channel_num(programInfo.getDisplayNum());
        }
        channel.set_channel_name(programInfo.getDisplayName());
        channel.set_user_lock(programInfo.getLock());
        channel.set_ca(programInfo.getCA());
        channel.set_tp_id(programInfo.getTpId());
        channel.set_channel_skip(programInfo.getSkip());// Scoty 20181109 modify for skip channel
        channel.set_pvr_skip(programInfo.getPvrSkip());

        return channel;
    }

    public List<SimpleChannel> getSimpleProgramListFromTotalChannelList(int type, int includeSkipFlag,
            int includePVRSkipFlag) {
        if (isGroupTypeInvalid(type)) {
            Log.d(TAG, "getSimpleProgramListFromTotalChannelList: invalid type");
            return new ArrayList<>();
        }

        List<SimpleChannel> simpleChannelList = new ArrayList<>();
        for (SimpleChannel simpleChannel : mTotalChannelList.get(type)) {
            if (!isSimpleChannelSkip(simpleChannel, includeSkipFlag, includePVRSkipFlag)) {
                simpleChannelList.add(simpleChannel);
            }
        }

        return simpleChannelList;
    }

    /**
     * check if given simpleChannel is skipped or not
     *
     * @param simpleChannel      channel to check
     * @param includeSkipFlag    if = 0, check user skip
     * @param includePVRSkipFlag if = 0, check pvr skip
     * @return whether {@code simpleChannel} need to be skipped or not
     */
    private boolean isSimpleChannelSkip(SimpleChannel simpleChannel, int includeSkipFlag, int includePVRSkipFlag) {
        int userSkipFlag = simpleChannel.get_channel_skip();
        int pvrSkipFlag = simpleChannel.get_pvr_skip();

        boolean isUserSkip = includeSkipFlag == 0 && userSkipFlag == 1;
        boolean isPVRSkip = includePVRSkipFlag == 0 && pvrSkipFlag == 1;

        return isUserSkip || isPVRSkip;
    }

    public SimpleChannel getSimpleProgramByChannelId(long channelId) {
        ProgramInfo programInfo = mDataManager.getProgramInfo(channelId);
        return getSimpleChannelFromProgramInfo(programInfo);
    }

    public SimpleChannel getSimpleProgramByChannelIdFromTotalChannelList(long channelId) {
        // find in ALL_TV_TYPE and ALL_RADIO_TYPE
        // because they should include all channels
        SimpleChannel simpleChannel = getSimpleProgramByChannelIdFromTotalChannelListByGroup(
                FavGroup.ALL_TV_TYPE,
                channelId);
        if (simpleChannel == null) {
            simpleChannel = getSimpleProgramByChannelIdFromTotalChannelListByGroup(
                    FavGroup.ALL_RADIO_TYPE,
                    channelId);
        }

        return simpleChannel;
    }

    public SimpleChannel getSimpleProgramByChannelIdFromTotalChannelListByGroup(int groupType, long channelId) {
        if (isGroupTypeInvalid(groupType) || isChannelIDInvalid(channelId)) {
            Log.e(TAG, "getSimpleProgramByChannelIdfromTotalChannelListByGroup: invalid input param");
            return null;
        }

        for (SimpleChannel simpleChannel : mTotalChannelList.get(groupType)) {
            if (simpleChannel.get_channel_id() == channelId) {
                return simpleChannel;
            }
        }

        return null;
    }

    public List<SimpleChannel> getChannelListByFilter(int filterTag, int groupType, String keyword, int includeSkip,
            int includePvrSkip) {
        List<SimpleChannel> simpleChannelList = getSimpleProgramList(groupType, includeSkip, includePvrSkip);
        List<SimpleChannel> filteredChannelList = new ArrayList<>();

        if (filterTag == MiscDefine.OKListFilter.TAG_CHANNEL_NUM) {
            for (SimpleChannel simpleChannel : simpleChannelList) {
                String strChannelNum = Integer.toString(simpleChannel.get_channel_num());
                // if keyword = '4', chNum '4', '4x', '4xx', ... is ok
                if (strChannelNum.startsWith(keyword)) {
                    filteredChannelList.add(simpleChannel);
                }
            }
        } else if (filterTag == MiscDefine.OKListFilter.TAG_CHANNEL_NAME) {
            for (SimpleChannel simpleChannel : simpleChannelList) {
                String channelName = simpleChannel.get_channel_name();
                // if keyword = 'news', name 'xxx news', 'xxx NeWs', 'xxx news xxx' ... is ok
                if (containsIgnoreCase(channelName, keyword)) {
                    filteredChannelList.add(simpleChannel);
                }
            }
        }

        // TODO: channel weight not implemented

        return filteredChannelList;
    }

    private boolean isGroupTypeInvalid(int groupType) {
        return groupType < FavGroup.ALL_TV_TYPE || groupType >= FavGroup.ALL_TV_RADIO_TYPE_MAX;
    }

    private boolean isChannelIDInvalid(long channelID) {
        return channelID <= 0;
    }

    /**
     * Check if given src String contains search String(ignore case).
     *
     * @param src    the string to be searched
     * @param search the string to search for
     * @return true if {@code src} contains {@code search} ignoring case, false
     *         otherwise
     */
    private boolean containsIgnoreCase(String src, String search) {
        if (src == null || search == null) {
            return false;
        }

        // return true if 'search' is empty or two strings are same(ignore case)
        if (search.isEmpty() || src.equalsIgnoreCase(search)) {
            return true;
        }

        // not using toLowerCase/UpperCase and String.contains
        // because of some special unicode chars

        // check if 'search' is equal(ignore case) to any substring of 'src'
        // the substring of 'src' should have the same length with 'search'
        int searchLength = search.length();
        for (int i = src.length() - searchLength; i >= 0; i--) {
            if (search.equalsIgnoreCase(src.substring(i, i + searchLength))) {
                return true;
            }
        }

        return false;
    }

    /*
     * fav
     */
    public List<FavInfo> favInfoGetList(int favMode) {
        if (isNotFavGroup(favMode)) {
            Log.e(TAG, "favInfoGetList: invalid favMode");
            return new ArrayList<>();
        }

        return mDataManager.getFavInfoList(favMode);
    }

    public FavInfo favInfoGet(int favMode, int index) {
        List<FavInfo> favInfoList = favInfoGetList(favMode);

        if (index < 0 || index >= favInfoList.size()) {
            Log.e(TAG, "favInfoGet: index out of bond");
            return null;
        }

        return favInfoList.get(index);
    }

    public int favInfoUpdateList(int favMode, List<FavInfo> favInfoList) {
        if (isNotFavGroup(favMode)) {
            Log.e(TAG, "favInfoUpdateList: invalid favMode");
            return -1;
        }

        // clear and rebuild
        mDataManager.delAllFavInfo(favMode);
        mDataManager.addFavInfoList(favMode, favInfoList);

        return 0;
    }

    public int favInfoDelete(int favMode, long channelId) {
        if (isNotFavGroup(favMode) || isChannelIDInvalid(channelId)) {
            Log.e(TAG, "favInfoDelete: invalid input param");
            return -1;
        }

        List<FavInfo> favInfoList = favInfoGetList(favMode);
        int favNum = -1;

        for (int i = 0; i < favInfoList.size(); i++) {
            if (favInfoList.get(i).getChannelId() == channelId) {
                favNum = favInfoList.get(i).getFavNum();
                break;
            }
        }

        if (favNum != -1) {
            mDataManager.delFavInfo(favMode, favNum);
        }

        return 0;
    }

    public int favInfoDeleteAll(int favMode) {
        if (isNotFavGroup(favMode)) {
            Log.e(TAG, "favInfoDeleteAll: invalid favMode");
            return -1;
        }

        mDataManager.delAllFavInfo(favMode);
        return 0;
    }

    public int favInfoSaveDb(FavInfo favInfo) {
        mDataManager.updateFavInfo(favInfo.getFavMode(), favInfo);
        return 0;
    }

    public String favGroupNameGet(int favMode) {
        // use isGroupTypeInvalid() instead of isNotFavGroup()
        // because we need to get name of ALL_TV and ALL_RADIO
        if (/* isNotFavGroup(favMode) */isGroupTypeInvalid(favMode)) {
            Log.e(TAG, "favGroupNameGet: invalid favMode = " + favMode);
            return "";
        }

        return mDataManager.getFavGroupName(favMode).getGroupName();
    }

    public int favGroupNameUpdate(int favMode, String name) {
        // use isGroupTypeInvalid() instead of isNotFavGroup()
        // because we need to update name of ALL_TV and ALL_RADIO
        if (/* isNotFavGroup(favMode) */isGroupTypeInvalid(favMode) || name == null) {
            Log.e(TAG, "favGroupNameUpdate: invalid input param");
            return -1;
        }

        mDataManager.setFavGroupName(favMode, name);
        return 0;
    }

    private boolean isNotFavGroup(int groupType) {
        return isGroupTypeInvalid(groupType)
                || groupType == FavGroup.ALL_TV_TYPE
                || groupType == FavGroup.ALL_RADIO_TYPE;
    }

    /*
     * tp info
     */
    public List<SatInfo> satInfoGetList(int tunerType, int pos, int num) {
        if ((pos < 0 && pos != MiscDefine.SatInfo.POS_ALL)
                || (num < 0 && num != MiscDefine.SatInfo.NUM_ALL)
                || (pos == MiscDefine.SatInfo.POS_ALL && num != MiscDefine.SatInfo.NUM_ALL)
                || (num == MiscDefine.SatInfo.NUM_ALL && pos != MiscDefine.SatInfo.POS_ALL)) {
            Log.e(TAG, "satInfoGetList: invalid input param");
            return new ArrayList<>();
        }

        List<SatInfo> satInfoList = mDataManager.getSatInfoList();
        List<SatInfo> result;
        int size = satInfoList.size();
        // return all found SatInfo if pos = POS_ALL and num = NUM_ALL
        // In fact, if pos == MiscDefine.SatInfo.POS_ALL,
        // condition 'num == MiscDefine.SatInfo.NUM_ALL' is always 'true' here
        if (pos == MiscDefine.SatInfo.POS_ALL && num == MiscDefine.SatInfo.NUM_ALL) {
            result = new ArrayList<>(satInfoList);
        }
        // return empty list if no SatInfo found or pos is out of bond
        else if (satInfoList.isEmpty() || pos >= size) {
            result = new ArrayList<>();
        }
        // return #num SatInfo from pos
        else {
            int toIndex = Math.min(pos + num, size); // avoid toIndex out of bond
            result = new ArrayList<>(satInfoList.subList(pos, toIndex));
        }

        return result;
    }

    public SatInfo satInfoGet(int satId) {
        SatInfo satInfo = mDataManager.getSatInfo(satId);
        return satInfo == null ? null : new SatInfo(satInfo);
    }

    public int satInfoAdd(SatInfo satInfo) {
        if (satInfo == null) {
            Log.e(TAG, "satInfoAdd: add null satInfo");
            return -1;
        }

        mDataManager.addSatInfo(satInfo);
        return 0;
    }

    public int satInfoUpdate(SatInfo satInfo) {
        if (satInfo == null) {
            Log.e(TAG, "satInfoUpdate: update null satInfo");
            return -1;
        }

        mDataManager.updateSatInfo(satInfo);
        return 0;
    }

    public int satInfoUpdateList(List<SatInfo> satInfoList) {
        mDataManager.updateSatInfoList(satInfoList);
        return 0;
    }

    public int satInfoDelete(int satId) {
        mDataManager.delSatInfo(satId);
        return 0;
    }

    public List<TpInfo> tpInfoGetListBySatId(int tunerType, int satId, int pos, int num) {
        if ((pos < 0 && pos != MiscDefine.TpInfo.POS_ALL)
                || (num < 0 && num != MiscDefine.TpInfo.NUM_ALL)
                || (pos == MiscDefine.TpInfo.POS_ALL && num != MiscDefine.TpInfo.NUM_ALL)
                || (num == MiscDefine.TpInfo.NUM_ALL && pos != MiscDefine.TpInfo.POS_ALL)) {
            Log.e(TAG, "tpInfoGetListBySatId: invalid input param");
            return new ArrayList<>();
        }

        List<TpInfo> tpInfoListBySatID;
        if (satId == MiscDefine.TpInfo.NONE_SAT_ID) { // sat id = 0 if NONE_SAT_ID
            tpInfoListBySatID = mDataManager.getTpInfoListBySatID(0);
        } else {
            tpInfoListBySatID = mDataManager.getTpInfoListBySatID(satId);
        }

        List<TpInfo> result;
        int size = tpInfoListBySatID.size();
        // return all found TpInfo if pos = POS_ALL and num = NUM_ALL
        // In fact, if pos == MiscDefine.TpInfo.POS_ALL,
        // condition 'num == MiscDefine.TpInfo.NUM_ALL' is always 'true' here
        if (pos == MiscDefine.TpInfo.POS_ALL && num == MiscDefine.TpInfo.NUM_ALL) {
            result = new ArrayList<>(tpInfoListBySatID);
        }
        // return empty list if no TpInfo found or pos is out of bond
        else if (tpInfoListBySatID.isEmpty() || pos >= size) {
            result = new ArrayList<>();
        }
        // return #num TpInfo from pos
        else {
            int toIndex = Math.min(pos + num, size); // avoid toIndex out of bond
            result = new ArrayList<>(tpInfoListBySatID.subList(pos, toIndex));
        }

        return result;
    }

    public TpInfo tpInfoGet(int tpID) {
        TpInfo tpInfo = mDataManager.getTpInfo(tpID);
        return tpInfo == null ? null : new TpInfo(tpInfo);
    }

    public int tpInfoAdd(TpInfo tpInfo) {
        if (tpInfo == null) {
            Log.e(TAG, "tpInfoAdd: add null tpInfo");
            return -1;
        }

        return mDataManager.addTpInfo(tpInfo);
    }

    public int tpInfoUpdate(TpInfo tpInfo) {
        if (tpInfo == null) {
            Log.e(TAG, "tpInfoUpdate: update null tpInfo");
            return -1;
        }

        mDataManager.updateTpInfo(tpInfo);
        return 0;
    }

    public int tpInfoUpdateList(List<TpInfo> tpInfoList) {
        mDataManager.updateTpInfoList(tpInfoList);
        return 0;
    }

    public int tpInfoDelete(int tpId) {
        mDataManager.delTpInfo(tpId);
        return 0;
    }

    @Override
    public void BaseHandleMessage(Message msg) {

    }

    private SimpleChannel update_net_program_epg_data(int index, SimpleChannel netProgramInfo) {
        PesiDtvFrameworkInterface DtvFramework = PesiDtvFramework.getInstance(mContext);
        EPGEvent presentepgEvent = new EPGEvent();
        if (netProgramInfo.get_play_stream_type() == ProgramPlayStreamType.VOD_TYPE) {// VOD
            presentepgEvent.set_event_id(5597 + index);
            presentepgEvent.set_event_name("Vod - " + index);
            presentepgEvent.set_event_type(EPGEvent.EPG_TYPE_PRESENT);

            // Set Current Time
            Date date = new Date(); // 取时间
            Calendar calendar = Calendar.getInstance();

            long tmpCurTime = DtvFramework.getDtvDate().getTime() - 60000;// date.getTime();
            presentepgEvent.set_start_time(tmpCurTime);
            calendar.add(Calendar.DATE, 7); // 把日期往后增加一天,整数 往后推,负数往前移动
            date = calendar.getTime(); // 这个时间就是日期往后推一天的结果
            presentepgEvent.set_end_time(date.getTime());
            presentepgEvent.set_duration(date.getTime() - tmpCurTime);

            netProgramInfo.set_present_epg_event(presentepgEvent);
            netProgramInfo.set_short_event("Vod - " + index);
            netProgramInfo.set_detail_info("Vod - " + index);

            EPGEvent followepgEvent = new EPGEvent();
            followepgEvent.set_event_id(5598 + index);
            followepgEvent.set_event_name("Vod - " + index);
            followepgEvent.set_event_type(EPGEvent.EPG_TYPE_FOLLOW);

            netProgramInfo.set_follow_epg_event(followepgEvent);
            netProgramInfo.set_short_event("Vod - " + index);
            netProgramInfo.set_detail_info("Vod - " + index);

        } else// YOUTUBE
        {
            presentepgEvent.set_event_id(5599 + index);
            presentepgEvent.set_event_name("Youtube - " + index);
            presentepgEvent.set_event_type(EPGEvent.EPG_TYPE_PRESENT);

            // Set Current Time
            Date date = new Date(); // 取时间
            Calendar calendar = Calendar.getInstance();
            long tmpCurTime = DtvFramework.getDtvDate().getTime();// date.getTime();
            presentepgEvent.set_start_time(tmpCurTime);
            calendar.add(Calendar.DATE, 7); // 把日期往后增加一天,整数 往后推,负数往前移动
            date = calendar.getTime(); // 这个时间就是日期往后推一天的结果
            presentepgEvent.set_end_time(date.getTime());
            presentepgEvent.set_duration(date.getTime() - tmpCurTime);

            netProgramInfo.set_present_epg_event(presentepgEvent);
            netProgramInfo.set_short_event("Youtube" + index);
            netProgramInfo.set_detail_info("Youtube" + index);

            EPGEvent followepgEvent = new EPGEvent();
            followepgEvent.set_event_id(5600 + index);
            followepgEvent.set_event_name("Youtube - " + index);
            followepgEvent.set_event_type(EPGEvent.EPG_TYPE_FOLLOW);
            netProgramInfo.set_follow_epg_event(followepgEvent);
            netProgramInfo.set_short_event("Youtube - " + index);
            netProgramInfo.set_detail_info("Youtube - " + index);
        }

        return netProgramInfo;
    }

    private boolean check_same_tp(int groupType, int tpId, List<Integer> pvrTpList)// Scoty 20181113 add for dual tuner
                                                                                   // pvrList
    {
        if (pvrTpList == null || pvrTpList.size() == 0)
            return false;

        for (int i = 0; i < pvrTpList.size(); i++) {
            if (tpId == pvrTpList.get(i))
                return true;
        }

        return false;
    }

    public void update_pvr_skip_list(int groupType, int IncludePVRSkipFlag, int tpId, List<Integer> pvrTpList)// Scoty
                                                                                                              // 20181113
                                                                                                              // add for
                                                                                                              // dual
                                                                                                              // tuner
                                                                                                              // pvrList
    {
        for (int i = 0; i < mTotalChannelList.get(groupType).size(); i++) {
            if ((IncludePVRSkipFlag == 0) || (pvrTpList != null
                    && check_same_tp(groupType, mTotalChannelList.get(groupType).get(i).get_tp_id(), pvrTpList)))// Scoty
                                                                                                                 // 20181113
                                                                                                                 // add
                                                                                                                 // for
                                                                                                                 // dual
                                                                                                                 // tuner
                                                                                                                 // pvrList
            {
                mTotalChannelList.get(groupType).get(i).set_pvr_skip(0);
            } else {
                mTotalChannelList.get(groupType).get(i).set_pvr_skip(1);
            }
        }
    }

    public int set_net_stream_info(int GroupType, NetProgramInfo netStreamInfo) {
        for (int i = 0; i < mTotalChannelList.get(GroupType).size(); i++) {
            if (mTotalChannelList.get(GroupType).get(i).get_channel_id() == netStreamInfo.getChannelId()) {
                return 0;
            }
        }

        SimpleChannel tmpNetSimpleChannel = new SimpleChannel();
        tmpNetSimpleChannel.set_channel_id(netStreamInfo.getChannelId());
        tmpNetSimpleChannel.set_channel_name(netStreamInfo.getChannelName());
        tmpNetSimpleChannel.set_channel_num(netStreamInfo.getChannelNum());
        tmpNetSimpleChannel.set_url(netStreamInfo.getVideoUrl());
        tmpNetSimpleChannel.set_user_lock(netStreamInfo.getUserLock());
        tmpNetSimpleChannel.set_pvr_skip(0);
        tmpNetSimpleChannel.set_ca(0);
        tmpNetSimpleChannel.set_play_stream_type(netStreamInfo.getPlayStreamType());// 0 : DVB; 1 : VOD; 2: Youtube
        tmpNetSimpleChannel.set_present_epg_event(netStreamInfo.getPresentepgEvent());
        tmpNetSimpleChannel.set_follow_epg_event(netStreamInfo.getFollowepgEvent());
        tmpNetSimpleChannel.set_short_event(netStreamInfo.getShortEvent());
        tmpNetSimpleChannel.set_detail_info(netStreamInfo.getDetailInfo());
        mTotalChannelList.get(GroupType).add(tmpNetSimpleChannel);
        return 1;
    }

    public int reset_program_database() {
        DataManager mDataManager = DataManager.getDataManager(mContext);
        DataManager.ProgramDatabase mProgramDatabase = mDataManager.GetProgramDatabase();
        mProgramDatabase.ResetDatabase(mContext);
        return 0;
    }

    /**
     * First Time Get netprogram.ini File and Add to DataBase
     * After Save Complete Rename to netprogram_already_set.ini
     * In order to not to save database again
     *
     * @return isSuccess : Save DataBase Results
     */
    public boolean init_net_program_database(int tunerType) {
        Log.d(TAG, "exce init_net_program_database: IN");
        DataManager.NetProgramDatabase mNetProgramDatabase = DataManager.getDataManager(mContext)
                .GetNetProgramDatabase();
        if (mNetProgramDatabase == null)
            Log.e(TAG, "init_net_program_database: mNetProgramDatabase is NULL");

        String VOD_INI_TITLE = "[VOD]", YOUTUBE_INI_TITLE = "[YOUTUBE]", EANABLE_NETPROGRAMS = "[ENABLE_NETPROGRAMS]";
        String VOD_ENABLE = "ENABLE", VOD_TRUE = "TRUE", VOD_PARAMS_FILTER = "==";
        String INI_READ_PATH = "/vendor/etc/dtv_settings/netprogram.ini";
        File readFile = new File(INI_READ_PATH);
        if (!readFile.exists()) {
            Log.d(TAG, "init_net_program_database: netprogram.ini Not Exist!");
            Pvcfg.SetEnableNetworkPrograms(false);
            return false;
        }

        String line;
        List<NetProgramInfo> netProgramInfoList = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(readFile);
            BufferedReader breader = new BufferedReader(fileReader);
            while ((line = breader.readLine()) != null) {
                NetProgramInfo tmpNetProgramInfo = new NetProgramInfo();
                Log.d(TAG, "init_net_program_database: get NetProgram Title [" + line + "]");
                if (line.equals(VOD_INI_TITLE) || line.equals(YOUTUBE_INI_TITLE)) {
                    tmpNetProgramInfo = set_net_program_from_ini_file(breader, tunerType);
                    netProgramInfoList.add(tmpNetProgramInfo);
                } else if (line.equals(EANABLE_NETPROGRAMS))// Check Add YOUTUBE & VOD or not
                {
                    line = breader.readLine();
                    String[] EnableNetProgramText = line.split(VOD_PARAMS_FILTER);
                    if (EnableNetProgramText[0].toUpperCase().equals(VOD_ENABLE)) {
                        // Enable or Disable Netprograms
                        Pvcfg.SetEnableNetworkPrograms(EnableNetProgramText[1].toUpperCase().equals(VOD_TRUE));
                    }

                    if (!Pvcfg.IsEnableNetworkPrograms())// No Need to Save Database if false
                        return false;

                }
            }
            breader.close();

        } catch (IOException e) {
            return false;
        }

        // Check NetPrograms are already saved in dB or not, if exist no need to save
        // again (Reboot to open DTVPlayer)
        if (mNetProgramDatabase != null) {
            if (mNetProgramDatabase.GetNetProgramList(mContext).size() > 0) {
                Log.e(TAG, "init_net_program_database: NetPrograms are already saved, no need to save again");
                return false;
            }
        }

        boolean isSuccess = save_net_program_list(netProgramInfoList, mContext);
        Log.d(TAG, "init_net_program_database: Save Database isSuccess = [" + isSuccess + "]");

        return isSuccess;

    }

    /**
     * Read netprogram.ini File and Save Params to NetProgramInfo Database
     *
     * @param reader Read netprogram.ini
     * @return NetprogramInfo
     */
    private NetProgramInfo set_net_program_from_ini_file(BufferedReader reader, int tunerType) {
        Log.d(TAG, "exce set_net_program_from_ini_file: ");
        String iniFileInfo;
        String PARAMS_END = "[END]";
        String NET_SERVICE_ID_TAG = "ServiceId";
        String NET_PLAY_STREAM_TYPE_TAG = "PlayStreamType";
        String NET_VIDEO_URL_TAG = "videoUrl";
        String NET_CHANNEL_NAME_TAG = "ChannelName";
        String NET_CHANNEL_NUM_TAG = "ChannelNum";
        NetProgramInfo netStreamInfo = new NetProgramInfo();
        try {
            while (!(iniFileInfo = reader.readLine()).equals(PARAMS_END)) {
                Log.d(TAG, "exce setVodProgramDatabase: get = [" + iniFileInfo + "]");
                String[] textsplit = iniFileInfo.split("==");
                if (textsplit[0].equals(NET_SERVICE_ID_TAG)) {
                    int serviceId = Integer.parseInt(textsplit[1]);
                    // int tpSize = TpInfoGetList(getTunerType()).size();
                    int tpSize = tpInfoGetListBySatId(tunerType, MiscDefine.TpInfo.NONE_SAT_ID,
                            MiscDefine.TpInfo.POS_ALL, MiscDefine.TpInfo.NUM_ALL).size();
                    int channelId = (serviceId << 16) + (tpSize + 1);
                    netStreamInfo.setChannelId(channelId);
                } else if (textsplit[0].equals(NET_PLAY_STREAM_TYPE_TAG)) {
                    netStreamInfo.setPlayStreamType(Integer.parseInt(textsplit[1]));
                } else if (textsplit[0].equals(NET_VIDEO_URL_TAG)) {
                    netStreamInfo.setVideoUrl(textsplit[1]);
                } else if (textsplit[0].equals(NET_CHANNEL_NAME_TAG)) {
                    netStreamInfo.setChannelName(textsplit[1]);
                } else if (textsplit[0].equals(NET_CHANNEL_NUM_TAG)) {
                    netStreamInfo.setChannelNum(Integer.parseInt(textsplit[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return netStreamInfo;
    }

    /**
     * netProgramlist from netprogram.ini
     *
     * @param netProgramlist Saved NetPrograms List
     * @return isSuccess Save DataBase Results
     */
    private boolean save_net_program_list(List<NetProgramInfo> netProgramlist, Context context) {
        // for(int i = 0 ; i < netProgramlist.size() ; i++)
        // Log.d(TAG, "exce SaveNetProgramList: " + netProgramlist.get(i).ToString());

        // Save NetProgramInfo Database
        DataManager.NetProgramDatabase mNetProgramDatabase = DataManager.getDataManager(context)
                .GetNetProgramDatabase();
        boolean isSuccess = mNetProgramDatabase.SaveNetProgramListDatabase(context, netProgramlist); // Clear and Add
        Log.d(TAG, "save_net_program_list: isSuccess = [" + isSuccess + "]");

        return isSuccess;
    }

    public void delete_maildata_of_db(int mail_id) {
        mDataManager.delete_maildata_of_db(mail_id);
        Log.d(TAG, "delete mail id : " + mail_id);
    }

    public void save_maildata_to_db(MailData mailData) {
        mDataManager.save_maildata_to_db(mailData);
    }

    public MailData get_mail_from_db(int id) {
        return mDataManager.get_mail_from_db(id);
    }

    public List<MailData> get_mail_list_from_db() { // for ui mail page to show
        return mDataManager.get_mail_list_from_db();
    }

    public List<FavGroup> fav_group_get_list() {
        return mDataManager.getFavGroupList();
    }

    public int SetTvInputId(String id) {
        return mDataManager.SetTvInputId(id);
    }

    public String GetTvInputId() {
        return mDataManager.GetTvInputId();
    }
}