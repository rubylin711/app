package com.prime.datastructure.CommuincateInterface;

import static com.prime.datastructure.CommuincateInterface.CommandBase.CMD_ServicePlayer_FE_Base;

public class FeModule {
    public static final int CMD_ServicePlayer_FE_GetTunerNum = CMD_ServicePlayer_FE_Base+0x01;
    public static final int CMD_ServicePlayer_FE_GetTunerType = CMD_ServicePlayer_FE_Base+0x02;
    public static final int CMD_ServicePlayer_FE_GetSignalStrength = CMD_ServicePlayer_FE_Base+0x03;
    public static final int CMD_ServicePlayer_FE_GetSignalQuality = CMD_ServicePlayer_FE_Base+0x04;
    public static final int CMD_ServicePlayer_FE_GetSignalSNR = CMD_ServicePlayer_FE_Base+0x05;
    public static final int CMD_ServicePlayer_FE_GetSignalBER = CMD_ServicePlayer_FE_Base+0x06;
    public static final int CMD_ServicePlayer_FE_GetTunerStatus = CMD_ServicePlayer_FE_Base+0x07;
    public static final int CMD_ServicePlayer_FE_TunerSetAntenna5V = CMD_ServicePlayer_FE_Base+0x08;
    public static final int CMD_ServicePlayer_FE_SetDiSEqC10PortInfo = CMD_ServicePlayer_FE_Base+0x09;
    public static final int CMD_ServicePlayer_FE_SetDiSEqC12MoveMotor = CMD_ServicePlayer_FE_Base+0x0A;
    public static final int CMD_ServicePlayer_FE_SetDiSEqC12MoveMotorStop = CMD_ServicePlayer_FE_Base+0x0B;
    public static final int CMD_ServicePlayer_FE_ResetDiSEqC12Position = CMD_ServicePlayer_FE_Base+0x0C;
    public static final int CMD_ServicePlayer_FE_SetDiSEqCLimitPos = CMD_ServicePlayer_FE_Base+0x0D;
    public static final int CMD_ServicePlayer_FE_TunerTune = CMD_ServicePlayer_FE_Base+0x0E;
    public static final int CMD_ServicePlayer_FE_TunerInit = CMD_ServicePlayer_FE_Base+0x0F;

    public static final String Strength_string = "Strength";
    public static final String Quality_string = "Quality";
    public static final String SNR_string = "SNR";
    public static final String BER_string = "BER";
}
