package com.prime.dtv.module;

import com.prime.dtv.PrimeDtvMediaPlayer;

public class CCModule {
    private static final String TAG = "CCModule";

    private static final int CMD_CC_BASE = PrimeDtvMediaPlayer.CMD_Base + 0xE00;

    // JAVA CMD
    // CC
    private static final int CMD_JAVA_Base = PrimeDtvMediaPlayer.CMD_JAVA_Base;
    private static final int CMD_CC_GetCurrentCC = CMD_JAVA_Base + CMD_CC_BASE + 0x01;
    private static final int CMD_CC_GetCCType = CMD_JAVA_Base + CMD_CC_BASE + 0x02;
    private static final int CMD_CC_SelectCC = CMD_JAVA_Base + CMD_CC_BASE + 0x03;
    private static final int CMD_CC_ShowCC = CMD_JAVA_Base + CMD_CC_BASE + 0x04;
    private static final int CMD_CC_IsCCVisible = CMD_JAVA_Base + CMD_CC_BASE + 0x05;
}
