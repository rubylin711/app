package com.prime.dtvplayer.Config;

public class Pvcfg {
    /************ Define **************/
    //define ca type
    public static final int CA_NONE = 0x0;
    public static final int CA_CONAX =0x1;
    public static final int CA_SC = 0x2;
    public static final int CA_IRDETO = 0x04;
    public static final int CA_CLOAK = 0x08;
    public static final int CA_VMX = 0x10;
    public static final int CA_WIDEVINE_CAS = 0x20;//eric lin 20210107 widevine cas
    /************ Config **************/
    public static final int UIMODEL_3796 = 3796;
    public static final int UIMODEL_3798 = 3798;
    public static final int UI_TYPE = 3;//Scoty 20180912 add UI type cfg
    public static final int UI_MODEL = UIMODEL_3796;//Scoty 20181024 add UI Model cfg
    public static final int PESI_CA_TYPE = CA_NONE; //eric lin 20210107 widevine cas
    public static final boolean PVR_PJ = true;//PVR_PJ = 1
    public static final boolean PIP = true;
    public static boolean ENABLE_NETWORK_PROGRAM = false;//Enable Youtube & Vod Streams to Programs

    public static boolean getPVR_PJ(){
        return PVR_PJ;
    }
    public static boolean getPIP(){
        return PIP;
    }
    public static int getUIType(){//Scoty 20180912 add UI type cfg
        return UI_TYPE;
    }
    public static int getUIModel(){//Scoty 20181024 add UI Model cfg
        return UI_MODEL;
    }
    public static int getCAType() // connie 20181107 for get CA Type
    {
        return PESI_CA_TYPE;
    }
    public static boolean IsEnableNetworkPrograms()//Enable Youtube & Vod Streams to Programs
    {
        return ENABLE_NETWORK_PROGRAM;
    }
    public static void SetEnableNetworkPrograms(boolean enable)//Enable Youtube & Vod Streams to Programs
    {
        ENABLE_NETWORK_PROGRAM = enable;
    }

}
