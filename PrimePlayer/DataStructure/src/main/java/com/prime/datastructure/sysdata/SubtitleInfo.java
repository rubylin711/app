package com.prime.datastructure.sysdata;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gary_hsu on 2018/1/4.
 */

public class SubtitleInfo implements Parcelable {
    private static final String TAG = "SubtitleInfo";

    private int CurPos;
    // ★ 注意：這個欄位名字原本就叫 Component，我幫你保留
    public List<SubtitleComponent> Component = new ArrayList<>();  // Johnny modify 20180212

    public int getCurPos() {
        return CurPos;
    }

    public void setCurPos(int curPos) {
        CurPos = curPos;
    }

    public int getComponentCount() {
        return Component.size();
    }

    public SubtitleComponent getComponent(int pos) {
        if (pos >= Component.size())
            return null;
        return Component.get(pos);
    }

    // ★★★ 跟 AudioInfo 一樣，加一個 getter 回傳整個 List 給 UI 用 ★★★
    public List<SubtitleComponent> getComponentList() {
        return Component;
    }

    // =====================
    // Parcelable: SubtitleInfo
    // =====================

    public SubtitleInfo() {
    }

    protected SubtitleInfo(Parcel in) {
        CurPos = in.readInt();
        Component = in.createTypedArrayList(SubtitleComponent.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(CurPos);
        dest.writeTypedList(Component);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubtitleInfo> CREATOR = new Creator<SubtitleInfo>() {
        @Override
        public SubtitleInfo createFromParcel(Parcel in) {
            return new SubtitleInfo(in);
        }

        @Override
        public SubtitleInfo[] newArray(int size) {
            return new SubtitleInfo[size];
        }
    };

    // =====================
    // SubtitleComponent
    // =====================

    public static class SubtitleComponent implements Parcelable { // Johnny modify 20180212
        private int Pid;
        private int Type;
        private int ComPageId;
        private int AncPageId;
        private String LangCode;
        private int Pos;

        public SubtitleComponent() {
        }

        protected SubtitleComponent(Parcel in) {
            Pid = in.readInt();
            Type = in.readInt();
            ComPageId = in.readInt();
            AncPageId = in.readInt();
            LangCode = in.readString();
            Pos = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(Pid);
            dest.writeInt(Type);
            dest.writeInt(ComPageId);
            dest.writeInt(AncPageId);
            dest.writeString(LangCode);
            dest.writeInt(Pos);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SubtitleComponent> CREATOR = new Creator<SubtitleComponent>() {
            @Override
            public SubtitleComponent createFromParcel(Parcel in) {
                return new SubtitleComponent(in);
            }

            @Override
            public SubtitleComponent[] newArray(int size) {
                return new SubtitleComponent[size];
            }
        };

        // ===== getter / setter =====

        public int getPid() {
            return Pid;
        }

        public void setPid(int pid) {
            Pid = pid;
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

        public int getType() {
            return Type;
        }

        public void setType(int type) {
            Type = type;
        }

        public int getComPageId() {
            return ComPageId;
        }

        public void setComPageId(int comPageId) {
            ComPageId = comPageId;
        }

        public int getAncPageId() {
            return AncPageId;
        }

        public void setAncPageId(int ancPageId) {
            AncPageId = ancPageId;
        }
    }
}