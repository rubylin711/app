package com.prime.dtv.sysdata;

import android.content.Context;

import com.prime.dmg.launcher.R;

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

    public SubtitleComponent getComponent(int pos) {
        if(pos >= Component.size())
            return null;
        return Component.get(pos);
    }

    public static class SubtitleComponent { // Johnny modify 20180212
        private int Pid;
        private int Type;
        private int ComPageId;
        private int AncPageId;
        private String LangCode;
        private int Pos;

        public int getPid() {
            return Pid;
        }

        public void setPid(int pid) {
            Pid = pid;
        }

        public static String getLangString(Context context,String langCode) {
            String str;
            if(langCode.equalsIgnoreCase("chi"))
                return context.getString(R.string.dialog_lang_ch);
            else if(langCode.equalsIgnoreCase("eng"))
                return context.getString(R.string.dialog_lang_en);
            else
                return langCode;
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
