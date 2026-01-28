package com.prime.datastructure.sysdata;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SystemInfo{
    public String device_sn;
    public String Ethernet_mac;
    public String Wifi_mac;
    public String HW_version;
    public String SW_version;
    public String Loader_version;
    public String HDD_sn;
    public String AreaCode;
    public int CmMode;
    public int BouquetId;
    public String CA_sn;
    public String CA_version;
    public String Android_version;
    public String Pvr_status;
    public String WV_id;
    public String Zip_code;

    public SystemInfo(){
        device_sn = "0320001319xxxxxxxx";
        Ethernet_mac = "001122334455";
        Wifi_mac = "554433221100";
        HW_version = "1";
        SW_version = "0.0.1";
        Loader_version = "1.0.0";
        HDD_sn = "";
        AreaCode = "0";
        CmMode = 0;
        BouquetId = 25149;
        CA_sn = "0320001319xxxxxxxx";
        CA_version = "Widevine18";
        Android_version = "14";
        Pvr_status = "";
        WV_id = "39805";
        Zip_code = "0";
    }
}
