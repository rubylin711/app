package com.prime.datastructure.utils;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

public class LogUtils {
    public static final int LOG_LEVEL_E = 5;
    public static final int LOG_LEVEL_W = 4;
    public static final int LOG_LEVEL_D = 3;
    public static final int LOG_LEVEL_I = 2;
    public static final int LOG_LEVEL_V = 1;
    public static final int LOG_LEVEL_O = 0;

    public static String customTagPrefix = "PrimeDTV";
    private static int sLogLevel = LOG_LEVEL_O;
    private static volatile boolean fcc_debug =
            SystemProperties.getBoolean("persist.sys.prime.dtv.fcc_debug", false);

    private LogUtils() {
    }

    public static void setLogLevel(int logLevel) {
        sLogLevel = logLevel;
    }

    public static void refreshTifTuneDebug() {
        fcc_debug =
                SystemProperties.getBoolean("persist.sys.prime.dtv.fcc_debug", false);
    }

    private static boolean isfcc_debug() {
        return fcc_debug;
    }

    private static boolean shouldLog(String content) {
        if (content == null) {
            return true;
        }
        if (content.startsWith("FCC_LOG") && !isfcc_debug()) {
            return false;
        }
        return true;
    }

    private static String generateTag() {
        StackTraceElement caller = new Throwable().getStackTrace()[2];
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        tag = TextUtils.isEmpty(customTagPrefix) ? tag : customTagPrefix + ":" + tag;
        return tag;
    }

    public static void d(String content) {
        if (sLogLevel > LOG_LEVEL_D)
            return;
        if (!shouldLog(content))
            return;
        log_out(content, LOG_LEVEL_D);
    }

    public static void d(String content, Throwable tr) {
        if (sLogLevel > LOG_LEVEL_D)
            return;
        if (!shouldLog(content))
            return;
        String tag = generateTag();

        Log.d(tag, content, tr);
    }

    public static void e(String content) {
        if (sLogLevel > LOG_LEVEL_E)
            return;
        log_out(content, LOG_LEVEL_E);
    }

    public static void e(String content, Throwable tr) {
        if (sLogLevel > LOG_LEVEL_E)
            return;
        String tag = generateTag();

        Log.e(tag, content, tr);
    }

    public static void i(String content) {
        if (sLogLevel > LOG_LEVEL_I)
            return;
        log_out(content, LOG_LEVEL_I);
    }

    public static void i(String content, Throwable tr) {
        if (sLogLevel > LOG_LEVEL_I)
            return;
        String tag = generateTag();

        Log.i(tag, content, tr);
    }

    public static void v(String content) {
        if (sLogLevel > LOG_LEVEL_V)
            return;
        log_out(content, LOG_LEVEL_V);
    }

    public static void v(String content, Throwable tr) {
        if (sLogLevel > LOG_LEVEL_V)
            return;
        String tag = generateTag();

        Log.v(tag, content, tr);
    }

    public static void w(String content) {
        if (sLogLevel > LOG_LEVEL_W)
            return;
        log_out(content, LOG_LEVEL_W);
    }

    public static void w(String content, Throwable tr) {
        if (sLogLevel > LOG_LEVEL_W)
            return;
        String tag = generateTag();

        Log.w(tag, content, tr);
    }

    public static void w(Throwable tr) {
        if (sLogLevel > LOG_LEVEL_W)
            return;
        String tag = generateTag();

        Log.w(tag, tr);
    }

    public static void wtf(String content) {
        if (sLogLevel > LOG_LEVEL_E)
            return;
        String tag = generateTag();

        Log.wtf(tag, content);
    }

    public static void wtf(String content, Throwable tr) {
        if (sLogLevel > LOG_LEVEL_E)
            return;
        String tag = generateTag();

        Log.wtf(tag, content, tr);
    }

    public static void wtf(Throwable tr) {
        if (sLogLevel > LOG_LEVEL_E)
            return;
        String tag = generateTag();

        Log.wtf(tag, tr);
    }

    private static void log_out(String content, int log_level){
        if (!shouldLog(content)) {
            return;
        }
        String tag, content_full;
        if(new Throwable().getStackTrace().length >3) {
            StackTraceElement current = new Throwable().getStackTrace()[2];
            StackTraceElement caller = new Throwable().getStackTrace()[3];
            tag = current.getClassName();
            tag = tag.substring(tag.lastIndexOf(".") + 1);
            content_full = "%s(L:%d) => %s call by %s(L:%d) ";
            content_full = String.format(content_full, current.getMethodName(), current.getLineNumber(), content, caller.getMethodName(), caller.getLineNumber());
            //Log.d(tag, content_full);
        }else{
            StackTraceElement current = new Throwable().getStackTrace()[2];
            tag = current.getClassName();
            tag = tag.substring(tag.lastIndexOf(".") + 1);
            content_full = "%s(L:%d) => %s";
            content_full = String.format(content_full, current.getMethodName(), current.getLineNumber(), content);
            //Log.d(tag, content_full);
        }
        switch(log_level){
            case LOG_LEVEL_V: Log.v(tag, content_full); break;
            case LOG_LEVEL_I: Log.i(tag, content_full); break;
            case LOG_LEVEL_D: Log.d(tag, content_full); break;
            case LOG_LEVEL_W: Log.w(tag, content_full); break;
            case LOG_LEVEL_E: Log.e(tag, content_full); break;
            default:
                Log.d(tag, content_full); break;
        }
    }
}
