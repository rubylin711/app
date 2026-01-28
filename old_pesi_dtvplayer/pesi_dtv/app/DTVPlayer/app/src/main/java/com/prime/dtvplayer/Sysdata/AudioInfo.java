package com.prime.dtvplayer.Sysdata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gary_hsu on 2018/1/4.
 */

public class AudioInfo {
    private static final String TAG="AudioInfo";
    private int CurPos;
    public List<AudioComponent> ComponentList = new ArrayList<>();  // Johnny modify 20180212

    public int getCurPos() {
        return CurPos;
    }

    public void setCurPos(int curIndex) {
        CurPos = curIndex;
    }

    public int getComponentCount() {
        return ComponentList.size();
    }

    public AudioComponent getComponent(int pos) {
        if(pos >= ComponentList.size())
            return null;
        return ComponentList.get(pos);
    }

    public static class AudioComponent {    // Johnny modify 20180212
        private int Pid;
        private int AudioType;
        private int AdType;
        private int TrackMode;
        private String LangCode;
        private int Pos;

        public int getPid() {
            return Pid;
        }

        public void setPid(int pid) {
            Pid = pid;
        }

        public int getAudioType() {
            return AudioType;
        }

        public void setAudioType(int audioType) {
            AudioType = audioType;
        }

        public int getAdType() {
            return AdType;
        }

        public void setAdType(int adType) {
            AdType = adType;
        }

        public int getTrackMode() {
            return TrackMode;
        }

        public void setTrackMode(int trackMode) {
            TrackMode = trackMode;
        }

        public String getLangCode() {
            return LangCode;
        }

        public void setLangCode(String langCode) {
            LangCode = langCode;
        }

        public int getPos() {
            return Pos;
        }

        public void setPos(int pos) {
            Pos = pos;
        }
    }
}
