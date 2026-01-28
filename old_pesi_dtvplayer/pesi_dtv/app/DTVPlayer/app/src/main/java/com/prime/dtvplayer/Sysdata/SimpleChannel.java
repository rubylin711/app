package com.prime.dtvplayer.Sysdata;

/**
 * Created by gary_hsu on 2018/1/8.
 */

public class SimpleChannel {
    private long ChannelId;
    private int groupType;
    private int ChannelNum;
    private String ChannelName;
    private int UserLock;
    private int CA;
    private int ChannelSkip;//Scoty 20181109 modify for skip channel
    private int PVRSkip;
    private int TpId;//Scoty 20180613 change get simplechannel list for PvrSkip rule
    private String Url; //for VOD and Youtube Stream
    private int PlayStreamType;//0:DVB;1:VOD;2:Youtube
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


    public int getCA() {
        return CA;
    }

    public void setCA(int Ca) {
        CA = Ca;
    }

    public int getPVRSkip() {
        return PVRSkip;
    }

    public void setPVRSkip(int PVRSkip) {
        this.PVRSkip = PVRSkip;
    }

    public void setTpId(int TpId)//Scoty 20180613 change get simplechannel list for PvrSkip rule
    {
        this.TpId = TpId;
    }

    public int getTpId() {//Scoty 20180613 change get simplechannel list for PvrSkip rule
        return TpId;
    }

    public int getChannelSkip() {//Scoty 20181109 modify for skip channel
        return ChannelSkip;
    }

    public void setChannelSkip(int channelSkip) {//Scoty 20181109 modify for skip channel
        ChannelSkip = channelSkip;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public int getType() {
        return groupType;
    }

    public void setType(int type) {
        this.groupType = type;
    }

    public int getPlayStreamType() {
        return PlayStreamType;
    }

    public void setPlayStreamType(int playStreamType) {
        this.PlayStreamType = playStreamType;
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
}
