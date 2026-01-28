package com.prime.homeplus.tv.manager;

import android.text.TextUtils;

import androidx.tvprovider.media.tv.Channel;

import com.prime.datastructure.TIF.TIFChannelData;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.homeplus.tv.utils.ChannelUtils;

import java.util.ArrayList;
import java.util.List;

public class CurrentChannelListManager {
    private static final String TAG = "CurrentChannelListManager";

    private List<Channel> currentChannelList;
    private List<ProgramInfo> programInfoList;
    private String currentGenre = "";
    private int currentChannelIndex = 0;

    public CurrentChannelListManager(String currentGenre, List<Channel> currentChannelList) {
        this.currentGenre = currentGenre;
        this.currentChannelList = currentChannelList;
    }

    public void setCurrentGenre(String genre) {
        this.currentGenre = genre;
    }

    public String getCurrentGenre() {
        return currentGenre;
    }

    public void setCurrentChannelList(List<Channel> currentChannelList) {
        this.currentChannelList = currentChannelList;
    }

    public List<Channel> getCurrentChannelList() {
        return new ArrayList<>(currentChannelList);
    }

    public boolean setCurrentChannelIndexByDisplayNumber(String targetDisplayNumber) {
        if (!TextUtils.isEmpty(targetDisplayNumber)) {
            for (int i = 0; i < currentChannelList.size(); i++) {
                Channel ch = currentChannelList.get(i);
                if (ch.getDisplayNumber().equals(targetDisplayNumber)) {
                    currentChannelIndex = i;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean setCurrentChannelIndex(int currentChannelIndex) {
        if (currentChannelIndex < currentChannelList.size()) {
            this.currentChannelIndex = currentChannelIndex;
            return true;
        }
        return false;
    }

    public Channel getCurrentChannel() {
        if (currentChannelIndex < currentChannelList.size()) {
            return currentChannelList.get(currentChannelIndex);
        }
        return null;
    }

    public List<ProgramInfo> getProgramInfoList() {
        return programInfoList;
    }

    public void setProgramInfoList(List<ProgramInfo> newProgramInfoList) {
        programInfoList = newProgramInfoList;
    }

    public void channelIndexUp() {
        if (currentChannelList.isEmpty()) {
            return;
        }
        currentChannelIndex = (currentChannelIndex + 1) % currentChannelList.size();
    }

    public void channelIndexDown() {
        if (currentChannelList.isEmpty()) {
            return;
        }
        currentChannelIndex = (currentChannelIndex - 1 + currentChannelList.size()) % currentChannelList.size();
    }

    public boolean setCurrentChannel(ProgramInfo programInfo) {
        if(programInfo == null)
            return false;
        for (int i = 0; i < currentChannelList.size(); i++) {
            Channel ch = currentChannelList.get(i);
            if(ch.getServiceId() == programInfo.getChannelId() && ch.getTransportStreamId() == programInfo.getTransportStreamId()
                    && ch.getOriginalNetworkId() == programInfo.getOriginalNetworkId()) {
                currentChannelIndex = i;
                return true;
            }
        }
        return false;
    }
}
