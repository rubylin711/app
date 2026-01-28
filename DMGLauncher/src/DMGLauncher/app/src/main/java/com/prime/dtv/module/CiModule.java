package com.prime.dtv.module;

import com.prime.dtv.PrimeDtvMediaPlayer;

public class CiModule {
    private static final String TAG = "CiModule";
    private static final int CMD_CI_BASE = PrimeDtvMediaPlayer.CMD_Base + 0xA00;
    //CI Command
    private static final int CMD_CI_GetCardStatus = CMD_CI_BASE + 0x01;
    private static final int CMD_CI_EnterMainMenu = CMD_CI_BASE + 0x02;
    private static final int CMD_CI_EnterSubMenu = CMD_CI_BASE + 0x03;
    private static final int CMD_CI_IsMenuShow = CMD_CI_BASE + 0x04;
    private static final int CMD_CI_GetMenuTitle = CMD_CI_BASE + 0x05;
    private static final int CMD_CI_GetMenuSubTitle = CMD_CI_BASE + 0x06;
    private static final int CMD_CI_GetMenuBottomTitle = CMD_CI_BASE + 0x07;
    private static final int CMD_CI_GetMenuChoiceNum = CMD_CI_BASE + 0x08;
    private static final int CMD_CI_GetMenuChoiceInfo = CMD_CI_BASE + 0x09;
    private static final int CMD_CI_GetCurMMIType = CMD_CI_BASE + 0x0A;
    private static final int CMD_CI_BackToPreMenu = CMD_CI_BASE + 0x0B;
    private static final int CMD_CI_ColseMainMenu = CMD_CI_BASE + 0x0C;
    private static final int CMD_CI_GetEnqContent = CMD_CI_BASE + 0x0D;
    private static final int CMD_CI_GetEnqLength = CMD_CI_BASE + 0x0E;
    private static final int CMD_CI_GetEnqBlindMode = CMD_CI_BASE + 0x0F;
    private static final int CMD_CI_SetEnqAnswer = CMD_CI_BASE + 0x10;
    private static final int CMD_CI_IsMenuHaveDate = CMD_CI_BASE + 0x11;
    private static final int CMD_CI_SetPassMode = CMD_CI_BASE + 0x12;
}
