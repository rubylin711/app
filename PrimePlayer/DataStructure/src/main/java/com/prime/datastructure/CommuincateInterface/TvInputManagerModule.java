package com.prime.datastructure.CommuincateInterface;

import static com.prime.datastructure.CommuincateInterface.CommandBase.CMD_ServicePlayer_TVINPUT_MANAGER_Base;

public class TvInputManagerModule {
    public static final String TAG = "TvInputManagerModule";
    public static final int CMD_ServicePlayer_TvInputManager_GetRatings                 = CMD_ServicePlayer_TVINPUT_MANAGER_Base + 0x01;
    public static final int CMD_ServicePlayer_TvInputManager_SetParentalRatingEnable    = CMD_ServicePlayer_TVINPUT_MANAGER_Base + 0x02;
    public static final int CMD_ServicePlayer_TvInputManager_RemoveAllRatings           = CMD_ServicePlayer_TVINPUT_MANAGER_Base + 0x03;
    public static final int CMD_ServicePlayer_TvInputManager_AddRatings                 = CMD_ServicePlayer_TVINPUT_MANAGER_Base + 0x04;
}
