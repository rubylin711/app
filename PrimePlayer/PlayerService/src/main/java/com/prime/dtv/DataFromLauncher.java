package com.prime.dtv;

public class DataFromLauncher {
    public final static String TAG = "DataFromLauncher";

    private static DataFromLauncher mDataFromLauncher = null;
    private String mMusicList = "";

    public static DataFromLauncher getInstance() {
        if(mDataFromLauncher == null)
            mDataFromLauncher = new DataFromLauncher();
        return mDataFromLauncher;
    }

    public DataFromLauncher() {

    }

    public void setACSMusicList(String musicList) {
        mMusicList = musicList;
    }

    public String getACSMusicList() {
        return mMusicList;
    }
}
