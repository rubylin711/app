package com.prime.dtv.service.Table.Desciptor;

import android.util.Log;

public abstract class DescBase {
    public static final int MAX_STRING_LENGTH_TABLES = 50;

    // descriptor_tag + descriptor_length
    // these 2 bytes apply to all descriptors
    protected static final int MIN_DESCRIPTOR_LENGTH = 2;

    public int Tag;
    public int Length;
    public boolean DataExist;
    public boolean DebugMsgEnable;
    public abstract void Parsing(byte[] data,int lens);
    public void LogPrint(String tag, String msg) {
        if(DebugMsgEnable) {
            Log.d(tag,msg);
        }
    }

    public int bcd_to_bin(int bcd)
    {
        return (((bcd & 0xf0000000) >> 28) * 10000000 +
                ((bcd & 0xf000000) >> 24) * 1000000 +
                ((bcd & 0xf00000) >> 20) * 100000 +
                ((bcd & 0xf0000) >> 16) * 10000 +
                ((bcd & 0xf000) >> 12) * 1000 +
                ((bcd & 0xf00) >> 8) * 100 +
                ((bcd & 0xf0) >> 4) * 10 +
                (bcd & 0xf));
    }
}
