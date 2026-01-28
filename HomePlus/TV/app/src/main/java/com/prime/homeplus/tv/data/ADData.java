package com.prime.homeplus.tv.data;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ADData {
    public String entryTime;
    public String pathPrefix;
    public List<String> playMode = new ArrayList<>();
    public List<String> playModeSubType = new ArrayList<>();
    public List<Integer> playModeValue = new ArrayList<>();
    public List<Integer> durationValue = new ArrayList<>();
    public List<List<ADImage>> adImageList = new ArrayList<>();

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public List<String> getPlayMode() {
        return playMode;
    }

    public void setPlayMode(List<String> playMode) {
        this.playMode = playMode;
    }

    public List<Integer> getPlayModeValue() {
        return playModeValue;
    }

    public void setPlayModeValue(List<Integer> playModeValue) {
        this.playModeValue = playModeValue;
    }

    public List<String> getPlayModeSubType() {
        return playModeSubType;
    }

    public void setPlayModeSubType(List<String> playModeSubType) {
        this.playModeSubType = playModeSubType;
    }

    public List<Integer> getDurationValue() {
        return durationValue;
    }

    public void setDurationValue(List<Integer> durationValue) {
        this.durationValue = durationValue;
    }

    public List<List<ADImage>> getAdImageList() {
        return adImageList;
    }

    public void setAdImageList(List<List<ADImage>> adImageList) {
        this.adImageList = adImageList;
    }

    @NonNull
    @Override
    public String toString() {
        return "ADData{" +
                "entryTime='" + entryTime + '\'' +
                ", pathPrefix='" + pathPrefix + '\'' +
                ", playMode='" + playMode + '\'' +
                ", playModeSubType='" + playModeSubType + '\'' +
                ", playModeValue=" + playModeValue +
                ", durationValue=" + durationValue +
                ", adImageList=" + adImageList +
                '}';
    }
}
