package com.prime.dtvplayer.Config;

public class PcToolcfg {
    //Check PcTool Data Ready
    public static boolean WritePcToolDataPass = false;
    public static void SetPcToolWriteDataPass(boolean pass)
    {
        WritePcToolDataPass = pass;
    }
    public static boolean GetPcToolWriteDataPass()
    {
        return WritePcToolDataPass;
    }

//---Connect Testing--------------------
    public static final int CMD_CONNECT_TEST = 0x1001;
//---Write Command ID-----------------
    public static final int CMD_WRITE_MAC_ETH = 0x2001;
    public static final int CMD_WRITE_MAC_BT = 0x2002;
    public static final int CMD_WRITE_MAC_WIFI = 0x2003;
    public static final int CMD_WRITE_HDCP_1_4_TX = 0x2004;
    public static final int CMD_WRITE_HDCP_1_4_RX = 0x2005;
    public static final int CMD_WRITE_HDCP_2_2_TX = 0x2006;
    public static final int CMD_WRITE_HDCP_2_2_RX = 0x2007;
    public static final int CMD_WRITE_PRIMEID = 0x2008;
    public static final int CMD_WRITE_CUSTOMER_SN = 0x2009;
    public static final int CMD_WRITE_CHIPID = 0x200A;
    public static final int CMD_WRITE_DRMKEY = 0x200B;
    public static final int CMD_WRITE_WIDEVINE_KEY = 0x200C;
    public static final int CMD_WRITE_PLAYREADY_KEY_BGROUPCERT_ENC = 0x200D;
    public static final int CMD_WRITE_PLAYREADY_KEY_ZGPRIV_ENC = 0x200E;
    public static final int CMD_WRITE_PLAYREADY_KEY_ENC = 0x200F;
    public static final int CMD_WRITE_ATTESTION_KEY = 0x2010;
    public static final int CMD_WRITE_HDCP_1_4_KEY = 0x2011;
    public static final int CMD_WRITE_HDCP_1_4_KEY_TOOLKEY = 0x2012;
    public static final int CMD_WRITE_NAGRA_PK = 0x2013;
    public static final int CMD_WRITE_NAGRA_CSC = 0x2014;
    public static final int CMD_WRITE_ANDROID_SN = 0x2015;
    public static final int CMD_WRITE_FINISH = 0x2016;
    public static final int CMD_WRITE_STATION_FINISH = 0x2017;

    public static final int CMD_READ_MAC_ETH = 0x4001;
    public static final int CMD_READ_MAC_BT = 0x4002;
    public static final int CMD_READ_MAC_WIFI = 0x4003;
    public static final int CMD_READ_HDCP_1_4_TX = 0x4004;
    public static final int CMD_READ_HDCP_1_4_RX = 0x4005;
    public static final int CMD_READ_HDCP_2_2_TX = 0x4006;
    public static final int CMD_READ_HDCP_2_2_RX = 0x4007;
    public static final int CMD_READ_PRIMEID = 0x4008;
    public static final int CMD_READ_CUSTOMER_SN = 0x4009;
    public static final int CMD_READ_CHIPID = 0x400A;
    public static final int CMD_READ_DRMKEY = 0x400B;
    public static final int CMD_READ_CSC_CHECKNUM = 0x400C;
    public static final int CMD_READ_NUID_CHECKNUM = 0x400D;
    public static final int CMD_READ_DPT_CRC = 0x400E;
    public static final int CMD_READ_CERT_REPORT_CHECKNUM = 0x400F;
    public static final int CMD_READ_CA_SN = 0x4010;
    public static final int CMD_READ_CSC_DATA_CONFIG = 0x4011;
    public static final int CMD_READ_ANDROID_SN = 0x4012;
    public static final int CMD_READ_MTEST_VERSION = 0x4013;

    public class PcToolErr{
        public static final int PCTOOL_NO_ERROR = 0xF000;
        public static final int PCTOOL_INIT_FAIL = 0xF001;
        public static final int PCTOOL_WRITE_ERROR = 0xF002;
        public static final int PCTOOL_READ_ERROR = 0xF003;
        public static final int PCTOOL_CRC_CHECK_FAIL = 0xF004;
        public static final int PCTOOL_TIMEOUT = 0xF005;
        public static final int PCTOOL_CMD_NOT_SUPPORT = 0xF006;
        public static final int PCTOOL_UNKNOW_ERROR = 0xF007;
    }
}
