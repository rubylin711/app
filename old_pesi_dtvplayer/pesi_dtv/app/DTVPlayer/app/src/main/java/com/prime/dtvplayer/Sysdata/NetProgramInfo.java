package com.prime.dtvplayer.Sysdata;

import android.util.Log;

public class NetProgramInfo {
    private static final String TAG = "NetProgramInfo";

    public static final String CHANNEL_ID = "ChannelId";
    public static final String PLAY_STREAM_TYPE = "PlayStreamType";
    public static final String GROUP_TYPE = "GroupType";
    public static final String DISPLAY_NUM = "DisplayNum";
    public static final String DISPLAY_NAME = "DisplayName";
    public static final String LOCK = "Lock";
    public static final String SKIP = "Skip";
    public static final String VIDEO_URL = "VideoUrl";

    private long ChannelId;
    private int PlayStreamType; //VOD Video Type
    private int GroupType;
    private String videoUrl;
    private int ChannelNum;
    private String ChannelName;
    private int UserLock;
    private int Skip;
    private EPGEvent PresentepgEvent;
    private EPGEvent FollowepgEvent;
    private String ShortEvent;
    private String DetailInfo;

    public long getChannelId() {
        return ChannelId;
    }

    public void setChannelId(long channelId) {
        ChannelId = channelId;
    }

    public int getChannelNum() {
        return ChannelNum;
    }

    public void setChannelNum(int channelNum) {
        ChannelNum = channelNum;
    }

    public String getChannelName() {
        return ChannelName;
    }

    public void setChannelName(String channelName) {
        ChannelName = channelName;
    }

    public int getUserLock() {
        return UserLock;
    }

    public void setUserLock(int userLock) {
        UserLock = userLock;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getPlayStreamType() {
        return PlayStreamType;
    }

    public void setPlayStreamType(int playStreamType) {
        PlayStreamType = playStreamType;
    }

    public EPGEvent getPresentepgEvent() {
        return PresentepgEvent;
    }

    public void setPresentepgEvent(EPGEvent presentepgEvent) {
        PresentepgEvent = presentepgEvent;
    }

    public EPGEvent getFollowepgEvent() {
        return FollowepgEvent;
    }

    public void setFollowepgEvent(EPGEvent followepgEvent) {
        FollowepgEvent = followepgEvent;
    }

    public String getShortEvent() {
        return ShortEvent;
    }

    public void setShortEvent(String shortEvent) {
        ShortEvent = shortEvent;
    }

    public String getDetailInfo() {
        return DetailInfo;
    }

    public void setDetailInfo(String detailInfo) {
        DetailInfo = detailInfo;
    }

    public int getGroupType() {
        return GroupType;
    }

    public void setGroupType(int groupType) {
        GroupType = groupType;
    }

    public int getSkip() {
        return Skip;
    }

    public void setSkip(int skip) {
        Skip = skip;
    }

    public String ToString(){

        String info = "ChannelId = "+ChannelId+" GroupType = "+GroupType+" DisplayNum = "+ ChannelNum +" DisplayName = "+ ChannelName
                +" Lock = "+UserLock+" Skip = "+Skip+" PlayStreamType = "+PlayStreamType +" videoUrl = " + videoUrl;

        return info;
    }
}
