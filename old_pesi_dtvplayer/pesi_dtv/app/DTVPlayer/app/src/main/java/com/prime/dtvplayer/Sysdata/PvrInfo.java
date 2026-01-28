package com.prime.dtvplayer.Sysdata;

public class PvrInfo {
    private static final String TAG="PvrInfo";
    int recId;
    long channelId;
    int pvrMode;

    public PvrInfo(int rec_id, int channel_id, int pvr_mode) {
        int i = 0;

        this.recId = rec_id;
        this.channelId = channel_id;
        this.pvrMode = pvr_mode;
    }

    public int getRecId() {
        return recId;
    }

    public void setRecId(int rec_id) {
        recId = rec_id;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channel_id) {
        channelId = channel_id;
    }

    public int getPvrMode() {
        return pvrMode;
    }

    public void setPvrMode(int pvr_mode) {
        pvrMode = pvr_mode;
    }


    public class EnPVRMode {
        public static final int NO_ACTION = 0;        /* No action */
        public static final int TIMESHIFT_LIVE = 1;                /* TimeShift live */
        public static final int TIMESHIFT_FILE = 2;          /* TimeShift file*/
        public static final int RECORD = 3;         /* Record  */
        public static final int PLAY_RECORD_FILE = 4;     /* Play record file */
        public static final int PVRMODE_TIMESHIFT_LIVE_PAUSE = 5;/* TimeShift Start */ //Scoty 20180827 add and modify TimeShift Live Mode
    }
}
