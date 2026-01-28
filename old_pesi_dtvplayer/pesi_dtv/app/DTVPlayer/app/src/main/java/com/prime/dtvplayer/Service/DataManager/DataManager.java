package com.prime.dtvplayer.Service.DataManager;

//import com.prime.dtvplayer.Service.Table.PatData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prime.dtvplayer.Database.DBChannel;
import com.prime.dtvplayer.Service.Database.DVBContentProvider;
import com.prime.dtvplayer.Service.Database.DVBDatabaseTable.ProgramDatabaseTable;
import com.prime.dtvplayer.Service.Database.NetStreamDatabaseTable.NetProgramDatabaseTable;
import com.prime.dtvplayer.Sysdata.DefaultChannel;
import com.prime.dtvplayer.Sysdata.FavGroupName;
import com.prime.dtvplayer.Sysdata.FavInfo;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.NetProgramInfo;
import com.prime.dtvplayer.Sysdata.PROGRAM_PLAY_STREAM_TYPE;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.Sysdata.BookInfo;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String TAG = "DataManager";
    public static final int CaSystemId = 0;
    public static final int TUNR_TYPE = TpInfo.DVBC;
    public String mPvrRecPath;
    public List<SatInfo> mSatInfoList;
    public List<TpInfo> mTpInfoList;
    public GposInfo mGposInfo;
    public List<ProgramInfo> mProgramInfoList = new ArrayList();
    public List<FavGroup> mFavGroupList = new ArrayList();
    public List<BookInfo> mBookInfoList = new ArrayList();

    private static DataManager mDataManager;
    private ProgramDatabase mProgramDatabase = null;
    private NetProgramDatabase mNetProgramDatabase = null;

    public final static DataManager getDataManager() {
        if(mDataManager == null) {
            mDataManager = new DataManager();
        }
        return mDataManager;
    }

    public DataManager() {
//        if(load db fail) {
//
//        }
//        else{
            DefaultValue defaultValue = new DefaultValue(TUNR_TYPE);
            mSatInfoList = defaultValue.satInfoList;
            mTpInfoList = defaultValue.tpInfoList;
            mGposInfo = defaultValue.gposInfo;
            for(FavGroupName tmp : defaultValue.favGroupNameList) {
                mFavGroupList.add(new FavGroup(tmp));
            }
            if(mProgramDatabase == null)
                mProgramDatabase = new ProgramDatabase();
            if(mNetProgramDatabase == null)
                mNetProgramDatabase = new NetProgramDatabase();
//        }
    }



    ////// Scoty Add SimpleChannel of PesiCmd
//    public void SetNetProgramDatabase(NetProgramDatabase netProgramDatabase)
//    {
//        mNetProgramDatabase = netProgramDatabase;
//    }
    public NetProgramDatabase GetNetProgramDatabase()
    {
        return mNetProgramDatabase;
    }
    public class NetProgramDatabase {
        public DefaultChannel GetDefaultChannel(Context context)
        {
            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            List<ProgramInfo> tmpProgramInfoList = mProgramDatabaseTable.load();
            DefaultChannel defaultChannel = new DefaultChannel();
            if(tmpProgramInfoList != null) {
                if(tmpProgramInfoList.size() == 0)
                    return null;
                defaultChannel.setChanneId(tmpProgramInfoList.get(0).getChannelId());
                defaultChannel.setGroupType(tmpProgramInfoList.get(0).getType());
                return defaultChannel;
            }else
                return null;

        }

        public void SetDefaultChannel(Context context, int channelId)
        {

        }

        public void SaveSimpleChannelList(Context context, List<SimpleChannel> channelList, int type, List<ProgramInfo> programInfoList) {
            for (int i = 0; i < channelList.size(); i++) {
                for (int j = 0; j < programInfoList.size(); j++) {
                    SimpleChannel channel = channelList.get(i);
                    if (programInfoList.get(j).getChannelId() == channelList.get(i).getChannelId()) {
                        programInfoList.get(j).setChannelId(channel.getChannelId());
                        programInfoList.get(j).setDisplayName(channel.getChannelName());
                        programInfoList.get(j).setDisplayNum(channel.getChannelNum());
                        programInfoList.get(j).setLock(channel.getUserLock());
                        programInfoList.get(j).setCA(channel.getCA());
                        programInfoList.get(j).setSkip(channel.getChannelSkip());
                        Log.d(TAG, "exce SaveSimpleChannelList: tpId = [" + programInfoList.get(j).getTpId() + "]");
                    }
                }
            }

            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            mProgramDatabaseTable.save(programInfoList);

        }

        public List<SimpleChannel> GetSimpleChannelList(Context context) {
            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            List<SimpleChannel> simpleChannelList = new ArrayList<>();
            List<ProgramInfo> tmpProgramInfoList = mProgramDatabaseTable.load();
            for(int i = 0 ; i < tmpProgramInfoList.size() ; i++)
            {
                SimpleChannel channel = new SimpleChannel();
                channel.setChannelId(tmpProgramInfoList.get(i).getChannelId());
                channel.setType(tmpProgramInfoList.get(i).getType());
                channel.setChannelName(tmpProgramInfoList.get(i).getDisplayName());
                channel.setChannelNum(tmpProgramInfoList.get(i).getDisplayNum());
                channel.setUserLock(tmpProgramInfoList.get(i).getLock());
                channel.setCA(tmpProgramInfoList.get(i).getCA());
                channel.setChannelSkip(tmpProgramInfoList.get(i).getSkip());
                channel.setTpId(tmpProgramInfoList.get(i).getTpId());
                channel.setPlayStreamType(PROGRAM_PLAY_STREAM_TYPE.DVB_TYPE);

                simpleChannelList.add(channel);
            }

            return simpleChannelList;
        }

        public void SaveNetProgramList(Context context, List<SimpleChannel> NetChannelList) {
            List<NetProgramInfo> mNetProgramInfoList = new ArrayList<>();

            for (int i = 0; i < NetChannelList.size(); i++) {
                NetProgramInfo mNetProgramInfo = new NetProgramInfo();
                mNetProgramInfo.setChannelId(NetChannelList.get(i).getChannelId());
                mNetProgramInfo.setPlayStreamType(NetChannelList.get(i).getPlayStreamType());
                mNetProgramInfo.setGroupType(NetChannelList.get(i).getType());
                mNetProgramInfo.setChannelNum(NetChannelList.get(i).getChannelNum());
                mNetProgramInfo.setChannelName(NetChannelList.get(i).getChannelName());
                mNetProgramInfo.setUserLock(NetChannelList.get(i).getUserLock());
                mNetProgramInfo.setSkip(NetChannelList.get(i).getChannelSkip());
                mNetProgramInfo.setVideoUrl(NetChannelList.get(i).getUrl());

                mNetProgramInfoList.add(mNetProgramInfo);
            }

            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            netProgramDatabaseTable.save(mNetProgramInfoList);

            ///Scoty Test Broadcast
//            StartNotify(context);

        }

        public void AddNetProgramList(Context context, List<SimpleChannel> NetChannelList) {
            List<NetProgramInfo> mNetProgramInfoList = new ArrayList<>();

            for (int i = 0; i < NetChannelList.size(); i++) {
                NetProgramInfo mNetProgramInfo = new NetProgramInfo();
                mNetProgramInfo.setChannelId(NetChannelList.get(i).getChannelId());
                mNetProgramInfo.setPlayStreamType(NetChannelList.get(i).getPlayStreamType());
                mNetProgramInfo.setGroupType(NetChannelList.get(i).getType());
                mNetProgramInfo.setChannelNum(NetChannelList.get(i).getChannelNum());
                mNetProgramInfo.setChannelName(NetChannelList.get(i).getChannelName());
                mNetProgramInfo.setUserLock(NetChannelList.get(i).getUserLock());
                mNetProgramInfo.setSkip(NetChannelList.get(i).getChannelSkip());
                mNetProgramInfo.setVideoUrl(NetChannelList.get(i).getUrl());

                mNetProgramInfoList.add(mNetProgramInfo);
            }

            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            netProgramDatabaseTable.add(mNetProgramInfoList);

        }

        public void UpdateNetProgramList(Context context, List<SimpleChannel> NetChannelList) {
            List<NetProgramInfo> mNetProgramInfoList = new ArrayList<>();

            for (int i = 0; i < NetChannelList.size(); i++) {
                NetProgramInfo mNetProgramInfo = new NetProgramInfo();
                mNetProgramInfo.setChannelId(NetChannelList.get(i).getChannelId());
                mNetProgramInfo.setPlayStreamType(NetChannelList.get(i).getPlayStreamType());
                mNetProgramInfo.setGroupType(NetChannelList.get(i).getType());
                mNetProgramInfo.setChannelNum(NetChannelList.get(i).getChannelNum());
                mNetProgramInfo.setChannelName(NetChannelList.get(i).getChannelName());
                mNetProgramInfo.setUserLock(NetChannelList.get(i).getUserLock());
                mNetProgramInfo.setSkip(NetChannelList.get(i).getChannelSkip());
                mNetProgramInfo.setVideoUrl(NetChannelList.get(i).getUrl());

                mNetProgramInfoList.add(mNetProgramInfo);
            }

            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            netProgramDatabaseTable.update(mNetProgramInfoList);

        }

        public void UpdateNetProgramInfo(Context context, SimpleChannel NetChannel) {
            NetProgramInfo mNetProgramInfo = new NetProgramInfo();
            mNetProgramInfo.setChannelId(NetChannel.getChannelId());
            mNetProgramInfo.setPlayStreamType(NetChannel.getPlayStreamType());
            mNetProgramInfo.setGroupType(NetChannel.getType());
            mNetProgramInfo.setChannelNum(NetChannel.getChannelNum());
            mNetProgramInfo.setChannelName(NetChannel.getChannelName());
            mNetProgramInfo.setUserLock(NetChannel.getUserLock());
            mNetProgramInfo.setSkip(NetChannel.getChannelSkip());
            mNetProgramInfo.setVideoUrl(NetChannel.getUrl());

            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            netProgramDatabaseTable.updateNetProgramInfo(mNetProgramInfo);

            ///Scoty Test Broadcast
            StartNotify(context);

        }

        public boolean SaveNetProgramListDatabase(Context context, List<NetProgramInfo> NetChannelList) {
            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            boolean isSuccess = netProgramDatabaseTable.save(NetChannelList);
            Log.e(TAG, "SaveNetProgramListDatabase: save isSuccess = [" + isSuccess  +"]");
            return isSuccess;
        }

        public List<NetProgramInfo> GetNetProgramList(Context context) {
            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            return netProgramDatabaseTable.load();
        }

        public void StartNotify(Context context)
        {
            Intent SearchIntent = new Intent();
            SearchIntent.setAction("com.prime.netprogram.database.update");
            context.sendBroadcast(SearchIntent, "android.permission.NETPROGRAM_BROADCAST");
        }

        public List<SimpleChannel> GetNetSimpleChannelList(Context context) {
            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            List<NetProgramInfo> tmpNetProgramInfoList = netProgramDatabaseTable.load();
            List<SimpleChannel> simpleChannelList = new ArrayList<>();

            for(int i = 0 ; i < tmpNetProgramInfoList.size() ; i++)
            {
                SimpleChannel channel = new SimpleChannel();
                channel.setChannelId(tmpNetProgramInfoList.get(i).getChannelId());
                channel.setType(tmpNetProgramInfoList.get(i).getGroupType());
                channel.setChannelName(tmpNetProgramInfoList.get(i).getChannelName());
                channel.setChannelNum(tmpNetProgramInfoList.get(i).getChannelNum());
                channel.setUserLock(tmpNetProgramInfoList.get(i).getUserLock());
                channel.setChannelSkip(tmpNetProgramInfoList.get(i).getSkip());
                channel.setPlayStreamType(tmpNetProgramInfoList.get(i).getPlayStreamType());
                channel.setUrl(tmpNetProgramInfoList.get(i).getVideoUrl());

                simpleChannelList.add(channel);
            }

            return simpleChannelList;
        }

        public int ResetDatabase(Context context)
        {
            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            int count = netProgramDatabaseTable.removeAll();

            return count;
        }

    }
    //////
    public ProgramDatabase GetProgramDatabase()
    {
        return mProgramDatabase;
    }
    public class ProgramDatabase {
        public void SaveSimpleChannelList(Context context, List<SimpleChannel> channelList, int type, List<ProgramInfo> programInfoList) {
            for (int i = 0; i < channelList.size(); i++) {
                for (int j = 0; j < programInfoList.size(); j++) {
                    SimpleChannel channel = channelList.get(i);
                    if (programInfoList.get(j).getChannelId() == channelList.get(i).getChannelId()) {
                        programInfoList.get(j).setChannelId(channel.getChannelId());
                        programInfoList.get(j).setDisplayName(channel.getChannelName());
                        programInfoList.get(j).setDisplayNum(channel.getChannelNum());
                        programInfoList.get(j).setLock(channel.getUserLock());
                        programInfoList.get(j).setCA(channel.getCA());
                        programInfoList.get(j).setSkip(channel.getChannelSkip());
                        Log.d(TAG, "exce SaveSimpleChannelList: tpId = [" + programInfoList.get(j).getTpId() + "]");
                    }
                }
            }

            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            mProgramDatabaseTable.save(programInfoList);
        }

        public void SaveProgramInfolList(Context context,  int type, List<ProgramInfo> programInfoList) {
            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            mProgramDatabaseTable.save(programInfoList);
        }

        public List<ProgramInfo> GetProgramInfoList(Context context)
        {
            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            return mProgramDatabaseTable.load();
        }

        public List<SimpleChannel> GetSimpleChannelList(Context context) {
            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            List<SimpleChannel> simpleChannelList = new ArrayList<>();
            List<ProgramInfo> tmpProgramInfoList = mProgramDatabaseTable.load();
            for(int i = 0 ; i < tmpProgramInfoList.size() ; i++)
            {
                SimpleChannel channel = new SimpleChannel();
                channel.setChannelId(tmpProgramInfoList.get(i).getChannelId());
                channel.setType(tmpProgramInfoList.get(i).getType());
                channel.setChannelName(tmpProgramInfoList.get(i).getDisplayName());
                channel.setChannelNum(tmpProgramInfoList.get(i).getDisplayNum());
                channel.setUserLock(tmpProgramInfoList.get(i).getLock());
                channel.setCA(tmpProgramInfoList.get(i).getCA());
                channel.setChannelSkip(tmpProgramInfoList.get(i).getSkip());
                channel.setTpId(tmpProgramInfoList.get(i).getTpId());
                channel.setPlayStreamType(PROGRAM_PLAY_STREAM_TYPE.DVB_TYPE);

                simpleChannelList.add(channel);
            }

            return simpleChannelList;
        }
        public int ResetDatabase(Context context)
        {
            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            int count = mProgramDatabaseTable.removeAll();

            return count;
        }
    }

    public String getPvrRecPath() {
        return mPvrRecPath;
    }

    public void setPvrRecPath(String pvrRecPath) {
        mPvrRecPath = pvrRecPath;
    }

    public List<SatInfo> getSatInfoList() {
        return mSatInfoList;
    }

    public SatInfo getSatInfo(int satId) {
        for(SatInfo tmp : mSatInfoList) {
            if(tmp.getSatId() == satId)
                return tmp;
        }
        return null;
    }

    public void updateSatInfo(SatInfo satInfo) {
        for(int i = 0; i < mSatInfoList.size(); i++) {
            if(mSatInfoList.get(i).getSatId() == satInfo.getSatId()) {
                mSatInfoList.get(i).update(satInfo);
                break;
            }
        }
    }

    public List<TpInfo> getTpInfoList() {
        return mTpInfoList;
    }

    public TpInfo getTpInfo(int tpId) {
        for(TpInfo tmp : mTpInfoList) {
            if(tmp.getTpId() == tpId)
                return tmp;
        }
        return null;
    }

    public void updateTpInfo(TpInfo tpInfo) {
        for(int i = 0; i < mTpInfoList.size(); i++) {
            if(mTpInfoList.get(i).getTpId() == tpInfo.getTpId()) {
                mTpInfoList.get(i).update(tpInfo);
                break;
            }
        }
    }

    public List<ProgramInfo> getProgramInfoList() {
        return mProgramInfoList;
    }

    public ProgramInfo getProgramInfo(int channelId) {
        for(ProgramInfo tmp : mProgramInfoList) {
            if(tmp.getChannelId() == channelId)
                return tmp;
        }
        return null;
    }

    public void addProgramInfo(ProgramInfo programInfo) {
        int add = 1;
        for(int i = 0; i < mProgramInfoList.size(); i++) {
            if(programInfo.getChannelId() == mProgramInfoList.get(i).getChannelId()) {
                mProgramInfoList.get(i).update(programInfo);
                add = 0;
                break;
            }
        }
        if(add == 1)
            mProgramInfoList.add(programInfo);
    }

    public void addProgramInfoList(List<ProgramInfo> programInfoList) {
        List<ProgramInfo> addList = new ArrayList<>();
        for(int i = 0; i < programInfoList.size(); i++) {
            int add = 1;
            for(int j = 0; j < mProgramInfoList.size(); i++) {
                if(programInfoList.get(i).getChannelId() == mProgramInfoList.get(j).getChannelId()) {
                    mProgramInfoList.get(j).update(programInfoList.get(i));
                    add = 0;
                    break;
                }
            }
            if(add == 1) {
                addList.add(programInfoList.get(i));
            }
        }
        for(ProgramInfo add : addList) {
            mProgramInfoList.add(add);
        }
    }

    public void delProgramInfo(ProgramInfo programInfo) {
        for(ProgramInfo tmp : mProgramInfoList) {
            if(tmp.getChannelId() == programInfo.getChannelId()) {
                mProgramInfoList.remove(tmp);
                break;
            }
        }
    }

    public void delProgramInfoList(List<ProgramInfo> programInfoList) {
        for(int i = 0; i < programInfoList.size(); i++) {
            for(int j = 0; j < mProgramInfoList.size(); i++) {
                if(programInfoList.get(i).getChannelId() == mProgramInfoList.get(j).getChannelId()) {
                    mProgramInfoList.remove(j);
                    break;
                }
            }
        }
    }

    public void updateProgramInfo(ProgramInfo programInfo) {
        for(int i = 0 ; i < mProgramInfoList.size(); i++) {
            if(mProgramInfoList.get(i).getChannelId() == programInfo.getChannelId()) {
                mProgramInfoList.get(i).update(programInfo);
                break;
            }
        }
    }

    public void delAllProgramInfo() {
        mProgramInfoList.clear();
    }

    public void updateGposInfo(GposInfo gposInfo) { mGposInfo=gposInfo; }

    public FavGroupName getFavGroupName(int groupIndex) {
        return mFavGroupList.get(groupIndex).getFavGroupName();
    }

    public List<FavInfo> getFavInfoList(int groupIndex) {
        return mFavGroupList.get(groupIndex).getFavInfoList();
    }

    public void setFavGroupName(int groupIndex,String name) {
        mFavGroupList.get(groupIndex).setFavGroupName(name);
    }

    public void addFavInfo(int groupIndex,FavInfo favInfo) {
        mFavGroupList.get(groupIndex).getFavInfoList().add(favInfo);
    }

    public void delFavInfo(int groupIndex,int favIndex) {
        mFavGroupList.get(groupIndex).getFavInfoList().remove(favIndex);
    }

    public void delAllFavInfo(int groupIndex) {
        mFavGroupList.get(groupIndex).getFavInfoList().clear();
    }

    public List<BookInfo> getBookInfoList() {
        return mBookInfoList;
    }

    public void addBookInfo(BookInfo bookInfo) {
        int findBookInfo=0,i;
        if(mBookInfoList.size() < bookInfo.MAX_NUM_OF_BOOKINFO) {
            for(i=0;i<mBookInfoList.size();i++){
                if (mBookInfoList.get(i).getBookId() == bookInfo.getBookId()){
                    findBookInfo=1;
                    break;
                }
            }
            if(findBookInfo==1)
                mBookInfoList.get(i).update(bookInfo);
            else
                mBookInfoList.add(bookInfo);
        }
    }

    public void updateBookInfo(int bookId,BookInfo bookInfo) {
        for(int i=0;i<mBookInfoList.size();i++) {
            if (mBookInfoList.get(i).getBookId() == bookId) {
                mBookInfoList.get(i).update(bookInfo);
            }
        }
    }

    public void delBookInfo(int bookId) {
        for(int i=0;i<mBookInfoList.size();i++) {
            if (mBookInfoList.get(i).getBookId() == bookId) {
                mBookInfoList.remove(i);
            }
        }
    }

    public void delAllBookInfo() {
        mBookInfoList.clear();
    }
}
