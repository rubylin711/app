package com.prime.datastructure.sysdata;

import android.os.Parcel;
import android.os.Parcelable;

import com.prime.datastructure.table.StreamType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gary_hsu on 2018/1/4.
 */

public class AudioInfo  implements Parcelable  {
    private static final String TAG="AudioInfo";

    private int CurPos;
    // Johnny modify 20180212
    public List<AudioComponent> ComponentList = new ArrayList<>();

    public AudioInfo() {
    }

    /* ====== Getter / Setter ====== */

    public int getCurPos() {
        return CurPos;
    }

    public void setCurPos(int curIndex) {
        CurPos = curIndex;
    }

    public int getComponentCount() {
        return ComponentList.size();
    }

    // 給 TIF 用的方便方法
    public List<AudioComponent> getComponentList() {
        return ComponentList;
    }

    public AudioComponent getComponent(int pos) {
        if (pos < 0 || pos >= ComponentList.size()) {
            return null;
        }
        return ComponentList.get(pos);
    }

    /* ====== Parcelable: AudioInfo 本體 ====== */

    protected AudioInfo(Parcel in) {
        CurPos = in.readInt();
        ComponentList = in.createTypedArrayList(AudioComponent.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(CurPos);
        dest.writeTypedList(ComponentList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AudioInfo> CREATOR = new Creator<AudioInfo>() {
        @Override
        public AudioInfo createFromParcel(Parcel in) {
            return new AudioInfo(in);
        }

        @Override
        public AudioInfo[] newArray(int size) {
            return new AudioInfo[size];
        }
    };

    /* ====== 內部類別：AudioComponent ====== */

    public static class AudioComponent implements Parcelable { // Johnny modify 20180212
        private int Pid;
        private int AudioType;
        private int AdType;
        private int TrackMode;
        private String LangCode;
        private int Pos;

        public AudioComponent() {
        }

        /* --- Getter / Setter --- */

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

        public String getAudioTypeString() {
            if (AudioType == StreamType.STREAM_AAC_AUDIO
                    || AudioType == StreamType.STREAM_HEAAC_AUDIO) {
                return "(ACC)";
            }
            if (AudioType == StreamType.STREAM_MPEG1_AUDIO
                    || AudioType == StreamType.STREAM_MPEG2_AUDIO) {
                return "(MPEG)";
            }
            if(AudioType == StreamType.STREAM_AC3_AUDIO) {
                return "(DD)";
            }
            if(AudioType == StreamType.STREAM_DDPLUS_AUDIO) {
                return "(DDP)";
            }
            return "";
        }

        /* --- Parcelable: AudioComponent --- */

        protected AudioComponent(Parcel in) {
            Pid = in.readInt();
            AudioType = in.readInt();
            AdType = in.readInt();
            TrackMode = in.readInt();
            LangCode = in.readString();
            Pos = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(Pid);
            dest.writeInt(AudioType);
            dest.writeInt(AdType);
            dest.writeInt(TrackMode);
            dest.writeString(LangCode);
            dest.writeInt(Pos);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<AudioComponent> CREATOR = new Creator<AudioComponent>() {
            @Override
            public AudioComponent createFromParcel(Parcel in) {
                return new AudioComponent(in);
            }

            @Override
            public AudioComponent[] newArray(int size) {
                return new AudioComponent[size];
            }
        };
    }
}
