package com.prime.datastructure.ServiceDefine;

public class AvCmdMiddle {
    public static final int FAST_ZAPPING = 0;
    public static final int PESI_MAINWIN = 0;
    public static final int PESI_PIPWIN = 1;
    public static final int PESI_PREAV = 2;
    public static final int PESI_WIN_MODE_MAX = 3;

    public static final int MAX_AVHANDLE_NUM = 4;
    public static final int MAX_PRE_AV_HANDLE = 2;
    public static final int MAX_REC = 2;

    public static final int PESI_PLAY_AV = 1;
    public static final int PESI_STOP_AV = 2;
    public static final int PESI_PLAY_PIP = 3;
    public static final int PESI_STOP_PIP = 4;
    public static final int PESI_PRE_START = 5;
    public static final int PESI_VIDEO_COUNT_CHECK = 6;

    public static final int PESI_START_DVB_SUBTITLE = 7;

    public static final int PESI_STOP_DVB_SUBTITLE = 8;

    public static final int PESI_CHANGE_AUDIO = 9;
    public static final int PESI_SET_FCC = 10;
    public static final int PESI_CLEAR_FCC = 11;
    public static final int PESI_STOP_AV_ALL = 12;
    public static final int PESI_PLAY_AV_NEW = 13;

    //PVR feature
    public static final int PVR_START_RECORD = 101;
    public static final int PVR_STOP_RECORD = 102;
    public static final int PVR_START_PLAYBACK = 103;
    public static final int PVR_STOP_PLAYBACK = 104;
    public static final int PVR_START_TIMESHIFT = 105;
    public static final int PVR_STOP_TIMESHIFT = 106;
    public static final int PVR_STOP_ALLRECORD = 107;

    public static final int PVR_PLAYBACK_PLAY = 110;
    public static final int PVR_PLAYBACK_PAUSE = 111;
    public static final int PVR_PLAYBACK_SEEK = 112;
    public static final int PVR_PLAYBACK_FF = 113;
    public static final int PVR_PLAYBACK_RW = 114;
    public static final int PVR_PLAYBACK_SET_SPEED = 115;
    public static final int PVR_PLAYBACK_CHANGE_AUDIO_TRACK = 116;
    public static final int PVR_PLAYBACK_CHANGE_SUB_TRACK = 117;

    public static final int PVR_TIMESHIFT_PLAY_RESUME = 118;
    public static final int PVR_TIMESHIFT_PLAY_PAUSE = 119;
    public static final int PVR_TIMESHIFT_PLAY_SEEK = 120;
    public static final int PVR_TIMESHIFT_PLAY_FF = 121;
    public static final int PVR_TIMESHIFT_PLAY_RW = 122;
    public static final int PVR_TIMESHIFT_PLAY_CHANGE_AUDIO_TRACK = 123;
    public static final int PVR_TIMESHIFT_PLAY_CHANGE_SUB_TRACK = 124;

    public static final int VIDEO_COUNT_CHECK_OFF = 0;
    public static final int VIDEO_COUNT_CHECK_ON = 1;

    public static final int PESI_SVR_AV_STOP_STATE = 0;
    public static final int PESI_SVR_AV_LIVEPLAY_STATE = 1;
    public static final int PESI_SVR_AV_TIMESHIFTPLAY_STATE = 2;
    public static final int PESI_SVR_AV_PAUSE_STATE = 3;
    public static final int PESI_SVR_AV_IDLE_STATE = 4;
    public static final int PESI_SVR_AV_RELEASE_STATE = 5;
    public static final int PESI_SVR_AV_PIP_STATE = 6;
    public static final int PESI_SVR_AV_EWSPLAY_STATE = 7;
    public static final int PESI_SVR_AV_PVRPLAY_STATE = 8;
}
