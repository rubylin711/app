package com.prime.dtvplayer.Service.DataManager;

import com.prime.dtvplayer.Sysdata.FavGroupName;
import com.prime.dtvplayer.Sysdata.FavInfo;

import java.util.ArrayList;
import java.util.List;

public class FavGroup {
    private static final String TAG = "FavGroup";
    private FavGroupName mFavGroupName;
    private List<FavInfo> mFavInfoList;

    public FavGroup(FavGroupName favGroupName) {
        mFavGroupName = favGroupName;
        mFavInfoList = new ArrayList<>();
    }

    public FavGroup(FavGroupName favGroupName , List<FavInfo> favInfoList) {
        mFavGroupName = favGroupName;
        mFavInfoList = favInfoList;
    }

    public List<FavInfo> getFavInfoList() {
        return mFavInfoList;
    }

    public FavGroupName getFavGroupName() {
        return mFavGroupName;
    }

    public void setFavGroupName(String name) {
        mFavGroupName.setGroupName(name);
    }

    public void addFavInfo(FavInfo favInfo) {
        mFavInfoList.add(favInfo);
    }

    public void delFavInfo(int index) {
        mFavInfoList.remove(index);
    }

    public void delAllFavInfo() {
        mFavInfoList.clear();
    }
}
