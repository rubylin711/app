package com.prime.dtvplayer.Sysdata;

import android.content.Context;
import android.util.Log;

/**
 * Created by gary_hsu on 2017/11/13.
 */

public class FavInfo {
    public static final String TAG = "FavInfo";

    public static final String FAV_NUM = "FavNum";
    public static final String CHANNEL_ID = "ChannelId";
    public static final String FAV_MODE = "FavMode";

    private int FavNum;
    private long ChannelId;
    private int FavMode;

    public FavInfo(){//eric lin add

    }

    public FavInfo(int favNum, long channelId, int favMode){//eric lin add
        FavNum = favNum;
        ChannelId = channelId;
        FavMode = favMode;
    }

    public FavInfo(FavInfo favInfo){//eric lin add
        this.FavNum = favInfo.getFavNum();
        this.ChannelId = favInfo.getChannelId();
        this.FavMode = favInfo.getFavMode();
    }

    public String ToString(){
        return "[FavNum  " + FavNum + "ChannelId : " + ChannelId + "FavMode : "+ FavMode + "]";
    }

    public int getFavNum() {
        Log.i(TAG, "GetIndex: FavNum=" + FavNum);
        return FavNum;
    }

    public long getChannelId() {
        Log.i(TAG, "GetServiceId: ChannelId=" + ChannelId);
        return ChannelId;
    }

    public int getFavMode() {
        Log.i(TAG, "GetFavMode: FavMode=" + FavMode);
        return FavMode;
    }

    public void setFavNum(int value) {
        Log.i(TAG, "SetIndex: value=" + value);
        FavNum = value;
    }

    public void setChannelId(long value) {
        Log.i(TAG, "SetServiceId: value=" + value);
        ChannelId = value;
    }

    public void setFavMode(int value) {
        Log.i(TAG, "SetFavMode: value=" + value);
        FavMode = value;
    }

    public static FavInfo[] LoadFavListFromDB(Context context, int favMode) {
        FavInfo[] favInfo = null;

        Log.i(TAG, "LoadFavListFromDB: favMode="+favMode);
        return favInfo;
    }

    public static void StoreFavListToDB(Context context, FavInfo[] list) {

        Log.i(TAG, "StoreFavListToDB: ");
    }

    public static int LoadFavListNum(Context context, int favMode) {
        int num=0;

        Log.i(TAG, "GetFavListNum: favMode="+favMode+", num="+num);
        return num;
    }
}
