package com.prime.sysdata;

/**
 * Created by gary_hsu on 2018/1/8.
 */

public class SimpleChannel {
    private int ChannelId;
    private int ChannelNum;
    private String ChannelName;
    private int UserLock;
    private int CA;

    public int getChannelId() {
        return ChannelId;
    }

    public void setChannelId(int channelId) {
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
}
