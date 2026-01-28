package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.prime.dtv.Interface.BaseManager;
import com.prime.datastructure.sysdata.CaStatus;
import com.prime.datastructure.sysdata.LoaderInfo;
import com.prime.datastructure.sysdata.VMXProtectData;

import java.util.ArrayList;

public class VMXCmdManager extends BaseManager {
    private static final String TAG = "VMXCmdManager" ;
    public VMXCmdManager(Context context, Handler handler) {
        super(context, TAG, handler, VMXCmdManager.class);
    }

    /*
    VMX
     */
    public LoaderInfo GetLoaderInfo() {
        return null;
    }

    public CaStatus GetCAStatusInfo() {
        return null;
    }

    public int GetECMcount() {
        return 0;
    }

    public int GetEMMcount() {
        return 0;
    }

    public String GetLibDate() {
        return null;
    }

    public String GetChipID() {
        return null;
    }

    public String GetSN() {
        return null;
    }

    public String GetCaVersion() {
        return null;
    }

    public String GetSCNumber() {
        return null;
    }

    public int GetPairingStatus() {
        return 0;
    }

    public String GetPurse() {
        return null;
    }

    public int GetGroupM() {
        return 0;
    }

    public int SetPinCode(String pinCode, int pinIndex, int textSelect) {
        return 0;
    }

    public int SetPPTV(String pinCode, int pinIndex) {
        return 0;
    }

    public void SetOMSMok() {

    }

    public void VMXTest(int mode) {

    }

    public void TestVMXOTA(int mode) {

    }

    public void VMXAutoOTA(int otaMode, int triggerID, int triggerNum, int tunerId, int satId, int dsmccPid, int freqNum, ArrayList<Integer> freqList, ArrayList<Integer> bandwidthList) {

    }

    public String VMXGetBoxID() {
        return null;
    }

    public String VMXGetVirtualNumber() {
        return null;
    }

    public void VMXStopEWBS(int mode) {

    }

    public void VMXStopEMM() {

    }

    public void VMXOsmFinish(int triggerID, int triggerNum) {

    }

    public VMXProtectData GetProtectData() {
        return null;
    }

    public int SetProtectData(int first, int second, int third) {
        return 0;
    }

    @Override
    public void BaseHandleMessage(Message msg) {

    }
}
