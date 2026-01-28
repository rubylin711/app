package com.prime.datastructure.sysdata;

import android.util.Log;

import com.prime.datastructure.config.Pvcfg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FavGroup {
    public static final String TAG = "FavGroup";
    private static int mFavGroupUsed;
    private FavGroupName mFavGroupName;
    private List<FavInfo> mFavInfoList;

    public static final int ALL_TV_TYPE = 0;
    public static final int TV_FAV1_TYPE = 1;
    public static final int TV_FAV2_TYPE = 2;
    public static final int TV_FAV3_TYPE = 3;
    public static final int TV_FAV4_TYPE = 4;
    public static final int TV_FAV5_TYPE = 5;
    public static final int TV_FAV6_TYPE = 6;
    public static final int TV_FAV7_TYPE = 7;
    public static final int TV_FAV8_TYPE = 8;
    public static final int TV_FAV9_TYPE = 9;
    public static final int TV_FAV10_TYPE = 10;
    public static final int TV_FAV11_TYPE = 11;
    public static final int TV_FAV12_TYPE = 12;
    public static final int TV_FAV13_TYPE = 13;
    public static final int TV_FAV14_TYPE = 14;
    public static final int TV_FAV15_TYPE = 15;
    public static final int TV_FAV16_TYPE = 16;
    public static final int ALL_RADIO_TYPE = 17;
    public static final int RADIO_FAV1_TYPE = 18;
    public static final int RADIO_FAV2_TYPE = 19;
    public static final int RADIO_FAV3_TYPE = 20;
    public static final int RADIO_FAV4_TYPE = 21;
    public static final int RADIO_FAV5_TYPE = 22;
    public static final int RADIO_FAV6_TYPE = 23;
    public static final int RADIO_FAV7_TYPE = 24;
    public static final int RADIO_FAV8_TYPE = 25;
    public static final int RADIO_FAV9_TYPE = 26;
    public static final int RADIO_FAV10_TYPE = 27;
    public static final int RADIO_FAV11_TYPE = 28;
    public static final int RADIO_FAV12_TYPE = 29;
    public static final int RADIO_FAV13_TYPE = 30;
    public static final int RADIO_FAV14_TYPE = 31;
    public static final int RADIO_FAV15_TYPE = 32;
    public static final int RADIO_FAV16_TYPE = 33;
    public static final int GENRE_BASE_TYPE = 34;
    public static final int GENRE_MAX_TYPE = GENRE_BASE_TYPE+ Pvcfg.GROUP_NO-1;

    public static final int GROUP_KIDS = GENRE_BASE_TYPE;
    public static final int GROUP_EDUCATION = GENRE_BASE_TYPE+1;
    public static final int GROUP_NEWS = GENRE_BASE_TYPE+2;
    public static final int GROUP_MOVIES = GENRE_BASE_TYPE+3;
    public static final int GROUP_VARIETY = GENRE_BASE_TYPE+4;
    public static final int GROUP_MUSIC = GENRE_BASE_TYPE+5;
    public static final int GROUP_ADULT = GENRE_BASE_TYPE+6;
    public static final int GROUP_SPORTS = GENRE_BASE_TYPE+7;
    public static final int GROUP_HD = GENRE_BASE_TYPE+8;
    public static final int GROUP_RELIGION = GENRE_BASE_TYPE+9;
    public static final int GROUP_SHOPPING = GENRE_BASE_TYPE+10;
    public static final int GROUP_UHD = GENRE_BASE_TYPE+11;
    public static final int GROUP_MANDARIN = GENRE_BASE_TYPE+12;
    public static final int GROUP_WESTERN = GENRE_BASE_TYPE+13;
    public static final int GROUP_JPOP = GENRE_BASE_TYPE+14;
    public static final int GROUP_LOUNGE = GENRE_BASE_TYPE+15;
    public static final int GROUP_CLASSICAL = GENRE_BASE_TYPE+16;
    public static final int GROUP_ELSE = GENRE_BASE_TYPE+17;
    public static final int GENRE_BASE_TYPE_CNS = GENRE_BASE_TYPE;
    public static final int GROUP_CNS_PUBLIC_WELFARE_RRLIGION = GENRE_BASE_TYPE_CNS;
    public static final int GROUP_CNS_DRAMA_MUSIC = GENRE_BASE_TYPE_CNS+1;
    public static final int GROUP_CNS_NEWS_FINANCE = GENRE_BASE_TYPE_CNS+2;
    public static final int GROUP_CNS_LEISURE_KNOWLEDGE = GENRE_BASE_TYPE_CNS+3;
    public static final int GROUP_CNS_CHILDREN_ANIMATION = GENRE_BASE_TYPE_CNS+4;
    public static final int GROUP_CNS_FILMS_SERIES = GENRE_BASE_TYPE_CNS+5;
    public static final int GROUP_CNS_VARIETY = GENRE_BASE_TYPE_CNS+6;
    public static final int GROUP_CNS_HOME_SHOPPING = GENRE_BASE_TYPE_CNS+7;
    public static final int GROUP_CNS_FOREIGN_LANGUAGE_LEARNING = GENRE_BASE_TYPE_CNS+8;
    public static final int GROUP_CNS_HD = GENRE_BASE_TYPE_CNS+9;
    public static final int GROUP_CNS_SPORTS_OTHERS = GENRE_BASE_TYPE_CNS+10;
    public static final int GROUP_CNS_ADULT = GENRE_BASE_TYPE_CNS+11;
    public static final int ALL_TV_RADIO_TYPE_MAX = GENRE_MAX_TYPE+1;//GENRE_BASE_TYPE+18;


    public static final int TV_FAV_BASE_TYPE = TV_FAV1_TYPE;
    public static final int TV_FAV_MAX_TYPE = TV_FAV16_TYPE;
    public static final int RADIO_FAV_BASE_TYPE = RADIO_FAV1_TYPE; ////RADIO_FAV1_TYPE=18;
    public static final int RADIO_FAV_MAX_TYPE = RADIO_FAV16_TYPE; //RADIO_FAV16_TYPE=33;

//    public static final int ALL_TV_TYPE = 0;
//    public static final int TV_FAV1_TYPE = 1;
//    public static final int TV_FAV2_TYPE = 2;
//    public static final int TV_FAV3_TYPE = 3;
//    public static final int TV_FAV4_TYPE = 4;
//    public static final int TV_FAV5_TYPE = 5;
//    public static final int TV_FAV6_TYPE = 6;
//    public static final int ALL_RADIO_TYPE = 7;
//    public static final int RADIO_FAV1_TYPE = 8;
//    public static final int RADIO_FAV2_TYPE = 9;
//    public static final int GROUP_KIDS = 10;
//    public static final int GROUP_EDUCATION = 11;
//    public static final int GROUP_NEWS = 12;
//    public static final int GROUP_MOVIES = 13;
//    public static final int GROUP_VARIETY = 14;
//    public static final int GROUP_MUSIC = 15;
//    public static final int GROUP_ADULT = 16;
//    public static final int GROUP_SPORTS = 17;
//    public static final int GROUP_HD = 18;
//    public static final int GROUP_RELIGION = 19;
//    public static final int GROUP_SHOPPING = 20;
//    public static final int GROUP_UHD = 21;
//    public static final int GROUP_MANDARIN = 22;
//    public static final int GROUP_WESTERN = 23;
//    public static final int GROUP_JPOP = 24;
//    public static final int GROUP_LOUNGE = 25;
//    public static final int GROUP_CLASSICAL = 26;
//    public static final int GROUP_ELSE = 27;
//
//    public static final int ALL_TV_RADIO_TYPE_MAX = 28;

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

    public int getFavGroupUsed() {
        return mFavGroupUsed;
    }

    public void setFavGroupUsed(int favGroupUsed) {
        mFavGroupUsed=favGroupUsed;
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

    public void insertFavInfo( int index,FavInfo favInfo) {
        synchronized(mFavListLock) {
            mFavInfoList.add(index, favInfo);
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
                    Log.d(TAG,"delFavInfo ["+favInfoTmp.getFavNum()+"] "+ "channelId["+favInfoTmp.getChannelId()+"]");
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

    public void printAll() {
        synchronized(mFavListLock) {
            Log.d(TAG, "groupName : "+getFavGroupName().getGroupName() + " "+ getFavGroupName().getGroupType());
            for (int i = 0; i < mFavInfoList.size(); i++) {
                FavInfo favInfo = mFavInfoList.get(i);
                if (favInfo != null) {
                    Log.d(TAG, "favInfo["+favInfo.getFavMode()+"] = ("+favInfo.getFavNum()+") "+favInfo.getChannelId());
                }
            }
        }
    }
}
