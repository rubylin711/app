package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.datastructure.sysdata.CaStatus;
import com.prime.datastructure.sysdata.LoaderInfo;

import java.util.ArrayList;

public class VmxModule {
    private static final String TAG = "VmxModule";
    private static final int CMX_VMX_BASE = PrimeDtvMediaPlayer.CMD_Base + 0x1300;

    //VMX
    private static final int CMD_VMX_GET_EMM_CNT = CMX_VMX_BASE + 0x01;
    private static final int CMD_VMX_GET_ECM_CNT = CMX_VMX_BASE + 0x02;
    private static final int CMD_VMX_GET_PAIR = CMX_VMX_BASE + 0x03;
    private static final int CMD_VMX_GET_PURSE = CMX_VMX_BASE + 0x04;
    private static final int CMD_VMX_SET_PINCODE = CMX_VMX_BASE + 0x05;
    private static final int CMD_VMX_SET_PPTV = CMX_VMX_BASE + 0x06;
    private static final int CMD_VMX_SET_OSM_OK = CMX_VMX_BASE + 0x07;
    private static final int CMD_VMX_GET_CHIPID = CMX_VMX_BASE + 0x08;
    private static final int CMD_VMX_GET_LIBDATE = CMX_VMX_BASE + 0x09;
    private static final int CMD_VMX_GET_SN = CMX_VMX_BASE + 0x0A;
    private static final int CMD_VMX_GET_CAVER = CMX_VMX_BASE + 0x0B;
    private static final int CMD_VMX_GET_SCNUM = CMX_VMX_BASE + 0x0C;
    private static final int CMD_VMX_GET_LOADER_INFO = CMX_VMX_BASE + 0x0D;
    private static final int CMD_VMX_GET_STATUS = CMX_VMX_BASE + 0x0E;
    private static final int CMX_VMX_TEST = CMX_VMX_BASE + 0x0F;
    private static final int CMX_VMX_STOP_EMM = CMX_VMX_BASE + 0x10;
    private static final int CMX_VMX_OSM_FINISH = CMX_VMX_BASE + 0x11;
    private static final int CMD_VMX_CAT_EMM_ENABLE = CMX_VMX_BASE + 0x12;
    private static final int CMD_VMX_AUTO_OTA = CMX_VMX_BASE + 0x13;
    private static final int CMD_VMX_GET_BOXID = CMX_VMX_BASE + 0x14;
    private static final int CMD_VMX_GET_VIRTUAL_NUMBER = CMX_VMX_BASE + 0x15;
    private static final int CMD_VMX_STOP_EWBS = CMX_VMX_BASE + 0x16;//Scoty 20181218 add stop EWBS


    public LoaderInfo get_loader_info() // connie 20180903 for VMX -s
    {
        Log.d(TAG, "get_loader_info: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_LOADER_INFO);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        LoaderInfo loaderInfo = new LoaderInfo();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_loader_info ok");
            loaderInfo.Software = reply.readString();
            loaderInfo.Hardware = reply.readString();
            loaderInfo.SequenceNumber = reply.readString();
            loaderInfo.BuildDate = reply.readString();
        } else
            Log.d(TAG, "get_loader_info failed, ret: " + ret);
        request.recycle();
        reply.recycle();
        return loaderInfo;
    }

    public CaStatus get_ca_status_info() {
        Log.d(TAG, "get_ca_status_info: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_STATUS);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        CaStatus caInfo = new CaStatus();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_ca_status_info ok");
            caInfo.CA_status = reply.readString();
            caInfo.Auth = reply.readString();
            caInfo.Deauth = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return caInfo;
    }

    public int get_ecm_count() {
        Log.d(TAG, "get_ecm_count: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_ECM_CNT);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        int count = 0;
        CaStatus caInfo = new CaStatus();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_ecm_count ok");
            count = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return count;
    }

    public int get_emm_count() {
        Log.d(TAG, "get_emm_count: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_EMM_CNT);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        int count = 0;
        CaStatus caInfo = new CaStatus();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_emm_count ok");
            count = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return count;
    }

    public String get_lib_date() {
        Log.d(TAG, "get_lib_date: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_LIBDATE);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        String date = "";
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_lib_date ok");
            date = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return date;
    }

    public String get_chip_id() {
        Log.d(TAG, "get_chip_id: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_CHIPID);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        String chipID = "";
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_chip_id ok");
            chipID = reply.readString();
        } else
            Log.d(TAG, "get_chip_id failed, ret = " + ret);
        request.recycle();
        reply.recycle();
        return chipID;
    }

    public String get_sn() {
        Log.d(TAG, "get_sn: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_SN);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        String sn = "";
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_sn ok");
            sn = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return sn;
    }

    public String get_ca_version() {
        Log.d(TAG, "get_ca_version: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_CAVER);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        String caVer = "";
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_ca_version ok");
            caVer = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return caVer;
    }

    public String get_sc_number() {
        Log.d(TAG, "get_sc_number: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_SCNUM);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        String scNum = "";
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_sc_number ok");
            scNum = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return scNum;
    }

    public int get_pairing_status() {
        Log.d(TAG, "get_pairing_status: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_PAIR);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        int pair = 0;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_pairing_status ok");
            pair = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return pair;
    }

    public String get_purse() {
        Log.d(TAG, "get_purse: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_PURSE);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        String purse = "";
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_purse ok");
            purse = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return purse;
    }

    public int get_group_m() {
        Log.d(TAG, "get_group_m: ");
        int groupM = 0;
        return groupM;
    }

    public int vmx_set_pin_code(String pincode) {
        Log.d(TAG, "vmx_set_pin_code: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_SET_PINCODE);
        request.writeString(pincode);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        int err = 0;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "vmx_set_pin_code ok");
            err = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return err;
    }


    public String get_location() {
        Log.d(TAG, "get_location: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_PURSE);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        String purse = "";
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_location ok");
            purse = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return purse;
    }

    public int set_pin_code(String pinCode, int PinIndex, int TextSelect) {
        Log.d(TAG, "set_pin_code: pinCode = " + pinCode);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_SET_PINCODE);
        request.writeString(pinCode);
        request.writeInt(PinIndex);
        request.writeInt(TextSelect);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int err = 0;
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "set_pin_code ok");
            err = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return err;
    }

    public int set_pptv(String pinCode, int pinIndex) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_SET_PPTV);
        request.writeString(pinCode);
        request.writeInt(pinIndex);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int err = 0;
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "set_pptv ok");
            err = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return err;
    }

    public void set_osm_ok() {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_SET_OSM_OK);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "set_osm_ok ok");
        }

        request.recycle();
        reply.recycle();
    }

    public void vmx_test(int mode) {
        Log.d(TAG, "vmx_test:  mode=" + mode);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMX_VMX_TEST);
        //====test data====
        if (mode == 7) // Edwin 20181127 Scramble is same as e16
            request.writeInt(9);
        else
            request.writeInt(mode); // cmd

        if (mode == 0) { // message
            Log.d(TAG, "vmx_test:  Show Mesage TEST !!!!");
            request.writeInt(0); // mode    0:user  1: always
            request.writeInt(5); // duration // 1s
            //request.writeInt(0); // Trigger ID
            //request.writeInt(0); // Trigger Num
            request.writeString("Test Msg : abcdefg hijklm npqrs tuvwxyz 123 456 789 0"); // msg
        } else if (mode == 1) { // OTA
            Log.d(TAG, "vmx_test:  OTA TEST !!!!");
            request.writeInt(1); // mode    0: normal   1:force   2: Err
            request.writeInt(0); // Trigger ID
            request.writeInt(0); // Trigger Num
            request.writeInt(0); // Freq Num//Scoty 20181207 modify VMX OTA rule
        } else if (mode == 2) { // WaterPrint msg Test
            Log.d(TAG, "vmx_test:  WATER TEST !!!!");
            request.writeInt(0); // mode   0:alwayse  1:flash    2: close
            request.writeInt(100); // duration // 0.1s
            request.writeString("Test Msg : abcdefg hijklm npqrs tuvwxyz 123 456 789 0"); // msg
            request.writeInt(100); // frame X
            request.writeInt(100); // frame Y
            request.writeInt(0); // Trigger
            request.writeInt(0);// Trigger Num;
        } else if (mode == 3) // Pin Test
        {
            Log.d(TAG, "vmx_test:  Pin Test");
            request.writeInt(0); // 0:open    1:close
            request.writeInt(-2118123484); // channel ID
            request.writeInt(0); // Pin Index
            request.writeInt(0); // Text Selector
        } else if (mode == 4) // IPPV Pin Test
        {
            Log.d(TAG, "vmx_test:  IPPV PIN Test");
            request.writeInt(0);  // 0:open    1:close
            request.writeInt(-2118123484); // channel ID
            request.writeInt(0); // Pin Index
            request.writeString("80"); //cur token
            request.writeString("100"); // cost
        } else if (mode == 5) // Card Detect
        {
            request.writeInt(0); // card status
            Log.d(TAG, "vmx_test:  Card Status input = ");
        } else if (mode == 6) // Search
        {
            Log.d(TAG, "vmx_test: Block Search Test");
            request.writeInt(0); // search mode   0: all   1: tp
            request.writeInt(659143); // start freq
            request.writeInt(689143); // end freq
            request.writeInt(0); // Trigger
            request.writeInt(0);// Trigger Num
        }

        // Edwin 20181127 Scramble is same as e16
        //else if(mode == 7) // Scramble
        //{
        //    request.writeInt(0); // 0 :close 1: open
        //}

        else if (mode == 8) // ewbs
        {
            request.writeInt(1); // 0: close 1:open
            request.writeInt(0); // signal level
        } else if (mode == 9 || mode == 7) // e16
        {
            request.writeInt(0); // 0: open 1:close
        } else if (mode == 10) // mail
        {
            request.writeInt(1); // 0: force, 1:normal
            request.writeString("Test Msg : abcdefg hijklm npqrs tuvwxyz 123 456 789 0"); // msg
            request.writeInt(0); // Trigger
            request.writeInt(0); // Trigger Num
        } else if (mode == 11) // factory & rescan
        {
            request.writeInt(0); // Trigger
            request.writeInt(0);// Trigger Num
        } else if (mode == 12) // block
        {
            request.writeInt(1); // 1:enable, 0:disable
            request.writeInt(0); // Trigger
            request.writeInt(0);// Trigger Num
        }

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "vmx_test ok");
        }
        request.recycle();
        reply.recycle();
    }

    //Scoty 20181207 modify VMX OTA rule -s
    public void test_vmx_ota(int mode) {
        Log.d(TAG, "test_vmx_ota:  mode=" + mode);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMX_VMX_TEST);
        request.writeInt(1);

        if (mode == 1) { // OTA
            Log.d(TAG, "test_vmx_ota:  OTA TEST 0 !!!!");
            request.writeInt(0); // mode    0: normal   1:force   2: Err

            request.writeInt(0); // Trigger ID
            request.writeInt(0); // Trigger Num
            request.writeInt(0); // Freq Num
        } else if (mode == 2) //Ota Test freqNum == 1
        {
            Log.d(TAG, "test_vmx_ota:  OTA TEST 1 !!!!");
            request.writeInt(0); // mode    0: normal   1:force   2: Err

            request.writeInt(0); // Trigger ID
            request.writeInt(0); // Trigger Num

            request.writeInt(1); // Freq Num
            request.writeInt(659143);
            request.writeInt(0);
        } else if (mode == 3) //Ota Test freqNum == 4
        {
            Log.d(TAG, "test_vmx_ota:  OTA TEST 4 !!!!");
            request.writeInt(0); // mode    0: normal   1:force   2: Err

            request.writeInt(0); // Trigger ID
            request.writeInt(0); // Trigger Num
            request.writeInt(4); // Freq Num

            for (int i = 0; i < 4; i++) {
                //653143,677143,659143,701143
                if (i == 0)
                    request.writeInt(653143);
                else if (i == 1)
                    request.writeInt(677143);
                else if (i == 2)
                    request.writeInt(659143);
                else if (i == 3)
                    request.writeInt(701143);
                request.writeInt(0);
            }
        }

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "test_vmx_ota ok");
        }
        request.recycle();
        reply.recycle();
    }

    public int vmx_auto_ota(int OTAMode, int TriggerID, int TriggerNum, int TunerId, int SatId, int DsmccPid, int FreqNum, ArrayList<Integer> FreqList, ArrayList<Integer> BandwidthList, int tunerType) {
        Log.d(TAG, "vmx_auto_ota: ===>> IN OTAMode = " + OTAMode + " SatId = " + SatId + " TunerId = " + TunerId + " TUNER_TYPE = " + tunerType
                + " TriggerID = " + TriggerID + " TriggerNum = " + TriggerNum + " DsmccPid = " + DsmccPid + " FreqNum = " + FreqNum);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_AUTO_OTA);
        request.writeInt(OTAMode);
        request.writeInt(SatId);
        request.writeInt(TunerId);
        request.writeInt(tunerType);
        request.writeInt(TriggerID);
        request.writeInt(TriggerNum);
        request.writeInt(DsmccPid);
        request.writeInt(FreqNum);
        for (int i = 0; i < FreqNum; i++) {
            request.writeInt(FreqList.get(i));
            request.writeInt(BandwidthList.get(i));
        }
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int err = 0;
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "vmx_auto_ota ok");
            err = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return err;
    }

    //Scoty 20181207 modify VMX OTA rule -e
    public String vmx_get_box_id() {
        String boxID = "";
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_BOXID);

        PrimeDtvMediaPlayer.invokeex(request, reply);

        boxID = reply.readString();
        Log.d(TAG, "vmx_get_box_id:  boxID = " + boxID);
        request.recycle();
        reply.recycle();

        return boxID;
    }

    public String vmx_get_virtual_number() {
        String virtualNumber = "";
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_VIRTUAL_NUMBER);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        virtualNumber = reply.readString();
        Log.d(TAG, "vmx_get_virtual_number: virtualNumber =" + virtualNumber);
        request.recycle();
        reply.recycle();

        return virtualNumber;
    }

    public void vmx_stop_ewbs(int mode)//Scoty 20181225 modify VMX EWBS rule//Scoty 20181218 add stop EWBS
    {
        Log.d(TAG, "vmx_stop_ewbs: stop mode = " + mode);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_STOP_EWBS);
        request.writeInt(mode);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "vmx_stop_ewbs ok");
        }
        request.recycle();
        reply.recycle();
    }

    public void vmx_stop_emm() {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMX_VMX_STOP_EMM);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "vmx_stop_emm ok");
        }
        request.recycle();
        reply.recycle();
    } // connie 20180903 for VMX -e

    public void vmx_osm_finish(int triggerID, int triggerNum) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMX_VMX_OSM_FINISH);
        request.writeInt(triggerID);
        request.writeInt(triggerNum);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "vmx_osm_finish ok");
        }
        request.recycle();
        reply.recycle();
    }
}
