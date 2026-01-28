package com.prime.dtvplayer.Sysdata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gary_hsu on 2018/1/4.
 */

public class SubtitleInfo {
    private static final String TAG="SubtitleInfo";
    private int CurPos;
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

    public SubtitleInfo.SubtitleComponent getComponent(int pos) {
        if(pos >= Component.size())
            return null;
        return Component.get(pos);
    }

    public static class SubtitleComponent { // Johnny modify 20180212
        private int Pid;
        private int Type;
        private int MagazingNum;
        private int PageNum;
        private String LangCode;
        private int Pos;

        public int getPid() {
            return Pid;
        }

        public void setPid(int pid) {
            Pid = pid;
        }

        public int getMagazingNum() {
            return MagazingNum;
        }

        public void setMagazingNum(int magazingNum) {
            MagazingNum = magazingNum;
        }

        public int getPageNum() {
            return PageNum;
        }

        public void setPageNum(int pageNum) {
            PageNum = pageNum;
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
    }
}
