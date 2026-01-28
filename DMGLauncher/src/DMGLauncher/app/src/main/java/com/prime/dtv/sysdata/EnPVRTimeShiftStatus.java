package com.prime.dtv.sysdata;

/**
 * Created by johnny_shih on 2018/5/4.
 */

public class EnPVRTimeShiftStatus {
    public static final int START = 0;               /* After timeshift start but not play. */
    public static final int PLAY = 1;                /* Normal playing */
    public static final int PAUSE = 2;               /* Pause */
    public static final int FAST_FORWARD = 3;      /* Fast forward */
    public static final int FAST_BACKWARD = 4;    /* Fast backward */
    public static final int STOP = 5;                /* Stop */
    public static final int INVALID = 6;             /* Invalid */
}
