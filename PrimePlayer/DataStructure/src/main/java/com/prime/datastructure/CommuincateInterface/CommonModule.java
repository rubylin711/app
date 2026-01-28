package com.prime.datastructure.CommuincateInterface;

import static com.prime.datastructure.CommuincateInterface.CommandBase.CMD_ServicePlayer_COMMON_Base;

public class CommonModule {
    public static final String TAG = "CommonModule";
    public static final String DATASTRUCTURE_PACKAGE_NAME = "com.prime.datastructure";
    public static final String PLAYERSERVICE_PACKAGE_NAME = "com.prime.dtv";
    public static final int CMD_ServicePlayer_COMMON_SetModuleType          = CMD_ServicePlayer_COMMON_Base+0x01;
    public static final int CMD_ServicePlayer_COMMON_GetModuleType          = CMD_ServicePlayer_COMMON_Base+0x02;
    public static final int CMD_ServicePlayer_COMMON_GetLibVer              = CMD_ServicePlayer_COMMON_Base+0x03;
    public static final int CMD_ServicePlayer_COMMON_FactoryReset           = CMD_ServicePlayer_COMMON_Base+0x04;
    public static final int CMD_ServicePlayer_COMMON_SetEthernetStaticIP    = CMD_ServicePlayer_COMMON_Base+0x05;
    public static final int CMD_ServicePlayer_COMMON_SetEthernetDHCP        = CMD_ServicePlayer_COMMON_Base+0x06;
    public static final int CMD_ServicePlayer_COMMON_InitService            = CMD_ServicePlayer_COMMON_Base+0x07;
    public static final int CMD_ServicePlayer_COMMON_GetMac                 = CMD_ServicePlayer_COMMON_Base+0x08;
    public static final int CMD_ServicePlayer_COMMON_GetHddSeries           = CMD_ServicePlayer_COMMON_Base+0x09;
    public static final int CMD_ServicePlayer_COMMON_SetSystemLanguage      = CMD_ServicePlayer_COMMON_Base+0x0a;
    public static final int CMD_ServicePlayer_COMMON_SetHDCPLevel           = CMD_ServicePlayer_COMMON_Base+0x0b;
    public static final int CMD_ServicePlayer_COMMON_SetHdmiCec             = CMD_ServicePlayer_COMMON_Base+0x0c;
    public static final int CMD_ServicePlayer_COMMON_GetHdmiOutputFormat    = CMD_ServicePlayer_COMMON_Base+0x0d;
    public static final int CMD_ServicePlayer_COMMON_SetHdmiOutputFormat    = CMD_ServicePlayer_COMMON_Base+0x0e;
}
