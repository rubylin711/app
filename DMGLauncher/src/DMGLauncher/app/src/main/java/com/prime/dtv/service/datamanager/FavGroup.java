package com.prime.dtv.service.datamanager;

import com.prime.dtv.sysdata.FavGroupName;
import com.prime.dtv.sysdata.FavInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FavGroup {
    private static final String TAG = "FavGroup";
    private FavGroupName mFavGroupName;
    private List<FavInfo> mFavInfoList;

    public static final int ALL_TV_TYPE = 0;
    public static final int TV_FAV1_TYPE = 1;
    public static final int TV_FAV2_TYPE = 2;
    public static final int TV_FAV3_TYPE = 3;
    public static final int TV_FAV4_TYPE = 4;
    public static final int TV_FAV5_TYPE = 5;
    public static final int TV_FAV6_TYPE = 6;
    public static final int ALL_RADIO_TYPE = 7;
    public static final int RADIO_FAV1_TYPE = 8;
    public static final int RADIO_FAV2_TYPE = 9;
    public static final int GROUP_KIDS = 10;
    public static final int GROUP_EDUCATION = 11;
    public static final int GROUP_NEWS = 12;
    public static final int GROUP_MOVIES = 13;
    public static final int GROUP_VARIETY = 14;
    public static final int GROUP_MUSIC = 15;
    public static final int GROUP_ADULT = 16;
    public static final int GROUP_SPORTS = 17;
    public static final int GROUP_HD = 18;
    public static final int GROUP_RELIGION = 19;
    public static final int GROUP_SHOPPING = 20;
    public static final int GROUP_UHD = 21;
    public static final int GROUP_MANDARIN = 22;
    public static final int GROUP_WESTERN = 23;
    public static final int GROUP_JPOP = 24;
    public static final int GROUP_LOUNGE = 25;
    public static final int GROUP_CLASSICAL = 26;
    public static final int GROUP_ELSE = 27;

    public static final int ALL_TV_RADIO_TYPE_MAX = 28;

    private static final Object mFavListLock = new Object();

    public FavGroup(FavGroupName favGroupName) {
        mFavGroupName = favGroupName;
        mFavInfoList = new ArrayList<>();
    }

    public FavGroup(FavGroupName favGroupName , List<FavInfo> favInfoList) {
        mFavGroupName = favGroupName;
        mFavInfoList = favInfoList;
    }

    public List<FavInfo> getFavInfoList() {
        synchronized(mFavListLock) {
            return mFavInfoList;
        }
    }

    public FavGroupName getFavGroupName() {
        return mFavGroupName;
    }

    public void setFavGroupName(String name) {
        mFavGroupName.setGroupName(name);
    }

    public void updateFavInfo(FavInfo favInfo) {
        synchronized(mFavListLock) {
            if(favInfo.getFavNum() < mFavInfoList.size()) {
                FavInfo oldfavInfo = mFavInfoList.get(favInfo.getFavNum());
                oldfavInfo.setChannelId(favInfo.getChannelId());
            }
            else {
                mFavInfoList.add(favInfo);
            }
        }
    }

    public void addFavInfo(FavInfo favInfo) {
        synchronized(mFavListLock) {
            mFavInfoList.add(favInfo);
        }
    }

    public void addFavInfoList(List<FavInfo> favInfoList) {
        synchronized(mFavListLock) {
            mFavInfoList.addAll(favInfoList);
        }
    }

    public void delFavInfo(int favNum) {
        synchronized(mFavListLock) {
            Iterator<FavInfo> iterator = mFavInfoList.iterator();
            while (iterator.hasNext()) {
                FavInfo favInfoTmp = iterator.next();
                if (favInfoTmp.getFavNum() == favNum) {
                    iterator.remove();  // ✅ 用 iterator 移除是安全的
                }
            }
        }
    }

    public void delFavInfo(FavInfo favInfo) {
        synchronized(mFavListLock) {
            Iterator<FavInfo> iterator = mFavInfoList.iterator();
            while (iterator.hasNext()) {
                FavInfo favInfoTmp = iterator.next();
                if (favInfoTmp.getFavNum() == favInfo.getFavNum() && favInfoTmp.getChannelId() == favInfo.getChannelId()) {
                    iterator.remove();  // ✅ 用 iterator 移除是安全的
                }
            }
        }
    }

    public void delAllFavInfo() {
        synchronized(mFavListLock) {
            mFavInfoList.clear();
        }
    }

    public FavInfo getFavInfo(int index) {
        synchronized(mFavListLock) {
            if(mFavInfoList.size() > index)
                return mFavInfoList.get(index);
            else
                return null;
        }
    }
}
