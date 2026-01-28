package com.prime.dtv.sysdata;

public class PvrInfo {
    private static final String TAG="PvrInfo";
    int recId;
    long channelId;
    int pvrMode;
    PlayTimeInfo mPlayTimeInfo = new PlayTimeInfo();

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

    public enum EnSeekMode {
        PLAY_SEEK_SET(0),
        PLAY_SEEK_CUR(1),
        PLAY_SEEK_END(2);

        private final int value;

        EnSeekMode(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    public enum EnPlaySpeed {

        PLAY_SPEED_ID_REV128(0),
        PLAY_SPEED_ID_REV64(1),
        PLAY_SPEED_ID_REV32(2),
        PLAY_SPEED_ID_REV16(3),
        PLAY_SPEED_ID_REV08(4),
        PLAY_SPEED_ID_REV04(5),
        PLAY_SPEED_ID_REV02(6),
        PLAY_SPEED_ID_REV01(7),
        PLAY_SPEED_ID_ZERO(8),
        PLAY_SPEED_ID_FWD01(9),
        PLAY_SPEED_ID_FWD02(10),
        PLAY_SPEED_ID_FWD04(11),
        PLAY_SPEED_ID_FWD08(12),
        PLAY_SPEED_ID_FWD16(13),
        PLAY_SPEED_ID_FWD32(14),
        PLAY_SPEED_ID_FWD64(15),
        PLAY_SPEED_ID_FWD128(16),
        PLAY_SPEED_ID_SLOW_REV4(17),
        PLAY_SPEED_ID_SLOW_REV2(18),
        PLAY_SPEED_ID_SLOW_REV1(19),
        PLAY_SPEED_ID_SLOW_FWD1(20),
        PLAY_SPEED_ID_SLOW_FWD2(21),
        PLAY_SPEED_ID_SLOW_FWD4(22),
        PLAY_SPEED_ID_FAILD(23);

        private final int value;

        EnPlaySpeed(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }

        public EnPlaySpeed convertSpeed(int speed){
            if (speed == PLAY_SPEED_ID_REV128.getValue()) {
                return PLAY_SPEED_ID_REV128;
            }
            else if (speed == PLAY_SPEED_ID_REV64.getValue()){
                return PLAY_SPEED_ID_REV64;
            }
            else if (speed == PLAY_SPEED_ID_REV32.getValue()){
                return PLAY_SPEED_ID_REV32;
            }
            else if (speed == PLAY_SPEED_ID_REV16.getValue()){
                return PLAY_SPEED_ID_REV16;
            }
            else if (speed == PLAY_SPEED_ID_REV08.getValue()){
                return PLAY_SPEED_ID_REV08;
            }
            else if (speed == PLAY_SPEED_ID_REV04.getValue()){
                return PLAY_SPEED_ID_REV04;
            }
            else if (speed == PLAY_SPEED_ID_REV02.getValue()){
                return PLAY_SPEED_ID_REV02;
            }
            else if (speed == PLAY_SPEED_ID_REV01.getValue()){
                return PLAY_SPEED_ID_REV01;
            }
            else if (speed == PLAY_SPEED_ID_ZERO.getValue()){
                return PLAY_SPEED_ID_ZERO;
            }
            else if (speed == PLAY_SPEED_ID_FWD01.getValue()){
                return PLAY_SPEED_ID_FWD01;
            }
            else if (speed == PLAY_SPEED_ID_FWD02.getValue()){
                return PLAY_SPEED_ID_FWD02;
            }
            else if (speed == PLAY_SPEED_ID_FWD04.getValue()){
                return PLAY_SPEED_ID_FWD04;
            }
            else if (speed == PLAY_SPEED_ID_FWD08.getValue()){
                return PLAY_SPEED_ID_FWD08;
            }
            else if (speed == PLAY_SPEED_ID_FWD16.getValue()){
                return PLAY_SPEED_ID_FWD16;
            }
            else if (speed == PLAY_SPEED_ID_FWD32.getValue()){
                return PLAY_SPEED_ID_FWD32;
            }
            else if (speed == PLAY_SPEED_ID_FWD64.getValue()){
                return PLAY_SPEED_ID_FWD64;
            }
            else if (speed == PLAY_SPEED_ID_FWD128.getValue()){
                return PLAY_SPEED_ID_FWD128;
            }
            else if (speed == PLAY_SPEED_ID_SLOW_REV4.getValue()){
                return PLAY_SPEED_ID_SLOW_REV4;
            }
            else if (speed == PLAY_SPEED_ID_SLOW_REV2.getValue()){
                return PLAY_SPEED_ID_SLOW_REV2;
            }
            else if (speed == PLAY_SPEED_ID_SLOW_REV1.getValue()){
                return PLAY_SPEED_ID_SLOW_REV1;
            }
            else if (speed == PLAY_SPEED_ID_SLOW_FWD1.getValue()){
                return PLAY_SPEED_ID_SLOW_FWD1;
            }
            else if (speed == PLAY_SPEED_ID_SLOW_FWD2.getValue()){
                return PLAY_SPEED_ID_SLOW_FWD2;
            }
            else if (speed == PLAY_SPEED_ID_SLOW_FWD4.getValue()){
                return PLAY_SPEED_ID_SLOW_FWD4;
            }
            else{
                return PLAY_SPEED_ID_FAILD;
            }
        }
    }

    public enum EnAuidoTrackMode {

        TRACK_MODE_STEREO(0),
        TRACK_MODE_DOUBLE_MONO(1),
        TRACK_MODE_DOUBLE_LEFT(2),
        TRACK_MODE_DOUBLE_RIGHT(3),
        TRACK_MODE_EXCHANGE(4),
        TRACK_MODE_ONLY_RIGHT(5),
        TRACK_MODE_ONLY_LEFT(6),
        TRACK_MODE_MUTED(7);

        private final int value;

        EnAuidoTrackMode(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }

    }

    public enum EnPlayStatus {
        PLAY_STATUS_STOP(0),
        PLAY_STATUS_PLAY(1),
        PLAY_STATUS_PAUSE(2),
        PLAY_STATUS_SCAN(3),
        PLAY_STATUS_FAILD(4);

        private final int value;

        EnPlayStatus(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    public static class PlayTimeInfo {
        public int mStartTime;
        public int mCurrentTime;
        public int mEndTime;
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
