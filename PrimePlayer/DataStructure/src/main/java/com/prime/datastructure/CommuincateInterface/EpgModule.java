package com.prime.datastructure.CommuincateInterface;

import static com.prime.datastructure.CommuincateInterface.CommandBase.CMD_ServicePlayer_EPG_Base;

public class EpgModule {
    public static final int CMD_ServicePlayer_EPG_GetPresentEvent = CMD_ServicePlayer_EPG_Base+0x01;
    public static final int CMD_ServicePlayer_EPG_GetFollowEvent = CMD_ServicePlayer_EPG_Base+0x02;
    public static final int CMD_ServicePlayer_EPG_GetEpgByEventID = CMD_ServicePlayer_EPG_Base+0x03;
    public static final int CMD_ServicePlayer_EPG_GetEPGEvents = CMD_ServicePlayer_EPG_Base+0x04;
    public static final int CMD_ServicePlayer_EPG_GetShortDescription = CMD_ServicePlayer_EPG_Base+0x05;
    public static final int CMD_ServicePlayer_EPG_GetDetailDescription = CMD_ServicePlayer_EPG_Base+0x06;
    public static final int CMD_ServicePlayer_EPG_StartScheduleEit = CMD_ServicePlayer_EPG_Base+0x07;
    public static final int CMD_ServicePlayer_EPG_StopScheduleEit = CMD_ServicePlayer_EPG_Base+0x08;
    public static final int CMD_ServicePlayer_EPG_StartEpg = CMD_ServicePlayer_EPG_Base+0x09;
    public static final int CMD_ServicePlayer_EPG_SendEpgDataId = CMD_ServicePlayer_EPG_Base+0x0A;
    public static final int CMD_ServicePlayer_EPG_AddEpgDataId = CMD_ServicePlayer_EPG_Base+0x0B;
    public static final int CMD_ServicePlayer_EPG_DeleteEpgDataId = CMD_ServicePlayer_EPG_Base+0x0C;
    public static final int CMD_ServicePlayer_EPG_SetupEpgChannel = CMD_ServicePlayer_EPG_Base+0x0D;
}
