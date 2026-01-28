package com.prime.sysdata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gary_hsu on 2018/1/4.
 */

public class TeletextInfo {
    private static final String TAG="TeletextInfo";
    private int MagazingNum;
    private int PageNum;
    private String LangCode;

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
}
