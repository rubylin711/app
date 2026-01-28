package com.prime.datastructure.Ca;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class IrdetoErrorCode {
    public static final String TAG = "IrdetoErrorCode";

    public static final int SET_E52	= 52;
    public static final int SET_E48	= 48;
    public static final int SET_E38	= 38;
    public static final int SET_E42	= 42;
    public static final int SET_E44	= 44;
    public static final int SET_PESI_DEFINE	= 0xaa00;

    public static final int CLEAN_E48_52 = 0xff48;
    public static final int CLEAN_E38 = 0xff38;
    public static final int CLEAN_E42 = 0xff42;
    public static final int CLEAN_E44 = 0xff44;
    public static final int CLEAN_PESI_DEFINE = 0xaaff;

    public enum ErrorCodePriority {
        ERR_E48_52(0),
        ERR_E38(1),
        //ERR_PROG_LOCK,
        ERR_CA(2),
        ERR_E44(3),
        ERR_PESI_DEFINE(4),//gary20120228 add no video signal and bad signal message
        ERR_E42(5),
        ERR_MAX(6);

        private int value;

        private ErrorCodePriority(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    };
}