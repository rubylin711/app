package com.prime.dtv.service.Table;

public abstract class TableData {
    private static final String TAG = "TableData";

    public abstract void parsing(byte[] data, int lens);
}
