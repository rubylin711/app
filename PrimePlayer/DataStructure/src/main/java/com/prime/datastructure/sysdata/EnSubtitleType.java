package com.prime.datastructure.sysdata;

public enum EnSubtitleType {
    SUBTITLE("SUBTITLE"),
    TELETEXT("TELETEXT"),
    CC("CC");

    private final String m_szType;
    EnSubtitleType(String szType)
    {
        this.m_szType = szType;
    }
    public String toString()
    {
        return m_szType;
    }
}
