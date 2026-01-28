package com.prime.datastructure.CommuincateInterface;

import static com.prime.datastructure.CommuincateInterface.CommandBase.CMD_ServicePlayer_AV_Base;

public class AvModule {
    public static final int CMD_ServicePlayer_AV_SetSurface = CMD_ServicePlayer_AV_Base + 0x01;
    public static final int CMD_ServicePlayer_AV_PlayByChannelId = CMD_ServicePlayer_AV_Base + 0x02;
    public static final int CMD_ServicePlayer_AV_PlayByChannelIdFCC = CMD_ServicePlayer_AV_Base + 0x03;
    public static final int CMD_ServicePlayer_AV_SetFastChangeChannel = CMD_ServicePlayer_AV_Base + 0x04;
    public static final int CMD_ServicePlayer_AV_ClearFastChangeChannel = CMD_ServicePlayer_AV_Base + 0x05;
    public static final int CMD_ServicePlayer_AV_ChangeAudio = CMD_ServicePlayer_AV_Base + 0x06;
    public static final int CMD_ServicePlayer_AV_GetAudioListInfo = CMD_ServicePlayer_AV_Base + 0x07;
    public static final int CMD_ServicePlayer_AV_GetPlayStatus = CMD_ServicePlayer_AV_Base + 0x08;
    public static final int CMD_ServicePlayer_AV_SetPlayStatus = CMD_ServicePlayer_AV_Base + 0x09;
    public static final int CMD_ServicePlayer_AV_PlayStopAll = CMD_ServicePlayer_AV_Base + 0x0A;
    public static final int CMD_ServicePlayer_AV_PlayStop = CMD_ServicePlayer_AV_Base + 0x0B;
    public static final int CMD_ServicePlayer_AV_SetVisibleCompiled = CMD_ServicePlayer_AV_Base + 0x0C;
    public static final int CMD_ServicePlayer_AV_WaitAVPlayReady = CMD_ServicePlayer_AV_Base + 0x0D;
    // ★★★ Subtitle Command ID（實際值自行排，不要跟既有重複）★★★
    public static final int CMD_ServicePlayer_SUBTITLE_GetList   = CMD_ServicePlayer_AV_Base + 0x0E;
    public static final int CMD_ServicePlayer_SUBTITLE_Select    = CMD_ServicePlayer_AV_Base + 0x0F;
    public static final int CMD_ServicePlayer_SUBTITLE_Stop      = CMD_ServicePlayer_AV_Base + 0x10;
    public static final int CMD_ServicePlayer_AV_ChangeChannelManager_TIF_PlayByChannelId = CMD_ServicePlayer_AV_Base + 0x11;
    public static final int CMD_ServicePlayer_AV_ChangeChannelManager_ListUpdate = CMD_ServicePlayer_AV_Base + 0x12;
    public static final int CMD_ServicePlayer_AV_SetAspectRatio = CMD_ServicePlayer_AV_Base + 0x13;
    public static final int CMD_ServicePlayer_AV_ResetAudioDefaultLanguage = CMD_ServicePlayer_AV_Base + 0x14;
    // Bundle key

    public static final String SetVisibleCompiled_string = "SetVisibleCompiled";
    public static final String PlayStatus_string = "PlayStatus";
    public static final String PlayId_string = "PlayId";
    public static final String Show_string = "Show";
    public static final String Mode_string = "Mode";
    public static final String StopMonitorTable_string = "StopMonitorTable";
    public static final String Surface_string = "Surface";
    public static final String AudioComponent_string = "AudioComponent";
    public static final String SubtitleComponent_string = "SubtitleComponent";
    public static final String Force_string = "force";
}
