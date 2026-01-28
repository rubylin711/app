package com.prime.dtv.sysdata;

/**
 * Created by gary_hsu on 2018/1/8.
 */

public class MiscDefine {
    public class AvControl {
        public static final int PLAY_ID = 0;
        public static final int AV_STOP_ALL = 0xff;
    }
    public class ProgramInfo {
        public static final int POS_ALL = -1;
        public static final int NUM_ALL = -1;
    }
    public class SatInfo {
        public static final int POS_ALL = -1;
        public static final int NUM_ALL = -1;
    }
    public class TpInfo {
        public static final int NONE_SAT_ID = -1;
        public static final int NONE_TUNER_TYPE = -1;
        public static final int POS_ALL = -1;
        public static final int NUM_ALL = -1;
    }
    public class EpgEventInfo {
        public static final int POS_ALL = -1;
        public static final int NUM_ALL = -1;
    }

    public class OKListFilter {
        public static final int TAG_CHANNEL_NUM = 0;
        public static final int TAG_CHANNEL_NAME = 1;
    }

    public class SaveStatus {
        public static final int STATUS_NONE = 0;
        public static final int STATUS_ADD = 1;
        public static final int STATUS_UPDATE = 2;
        public static final int STATUS_DELETE = 3;
        public static final int STATUS_NOT_DELETE = 4;
    }
}
