package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.prime.dtv.sysdata.OTACableParameters;
import com.prime.dtv.sysdata.OTATerrParameters;
import com.prime.dtv.Interface.BaseManager;

import java.util.List;
import java.util.Map;

public class MtestCmdManager extends BaseManager {
    private static final String TAG = "MtestCmdManager" ;
    public MtestCmdManager(Context context, Handler handler) {
        super(context, TAG, handler, MtestCmdManager.class);
    }

    /*
    MTest
     */
    public int UpdateMtestOTASoftWare() {
        return 0;
    }

    public OTACableParameters DVBGetOTACableParas() {
        return null;
    }

    public OTATerrParameters DVBGetOTAIsdbtParas() {
        return null;
    }

    public OTATerrParameters DVBGetOTATerrestrialParas() {
        return null;
    }

    public OTATerrParameters DVBGetOTADVBT2Paras() {
        return null;
    }

    public void PESI_CMD_CallBackTest(int hiSvrEvtAvPlaySuccess) {

    }

    public int getTempPesiDefaultChannelFlag() {
        return 0;
    }

    public void setTempPesiDefaultChannelFlag(int flag) {

    }

    public int MtestGetGPIOStatus(int u32GpioNo) {
        return 0;
    }

    public int MtestSetGPIOStatus(int u32GpioNo, int bHighVolt) {
        return 0;
    }

    public int MtestGetATRStatus(int smartCardStatus) {
        return 0;
    }

    public int MtestGetHDCPStatus() {
        return 0;
    }

    public int MtestGetHDMIStatus() {
        return 0;
    }

    public int MtestPowerSave() {
        return 0;
    }

    public int MtestSevenSegment(int enable) {
        return 0;
    }

    public int MtestSetAntenna5V(int tunerID, int tunerType, int enable) {
        return 0;
    }

    public int MtestSetBuzzer(int enable) {
        return 0;
    }

    public int MtestSetLedRed(int enable) {
        return 0;
    }

    public int MtestSetLedGreen(int enable) {
        return 0;
    }

    public int MtestSetLedOrange(int enable) {
        return 0;
    }

    public int MtestSetLedWhite(int enable) {
        return 0;
    }

    public int MtestSetLedOnOff(int status) {
        return 0;
    }

    public int MtestGetFrontKey(int key) {
        return 0;
    }

    public int MtestSetUsbPower(int enable) {
        return 0;
    }

    public int MtestTestUsbReadWrite(int portNum, String path) {
        return 0;
    }

    public int MtestTestAvMultiPlay(int tunerNum, List<Integer> tunerIDs, List<Long> channelIDs) {
        return 0;
    }

    public int MtestTestAvStopByTunerID(int tunerID) {
        return 0;
    }

    public int MtestMicSetInputGain(int value) {
        return 0;
    }

    public int MtestMicSetLRInputGain(int l_r, int value) {
        return 0;
    }

    public int MtestMicSetAlcGain(int value) {
        return 0;
    }

    public int MtestGetErrorFrameCount(int tunerID) {
        return 0;
    }

    public int MtestGetFrameDropCount(int tunerID) {
        return 0;
    }

    public String MtestGetChipID() {
        return null;
    }

    public int MtestStartMtest(String version) {
        return 0;
    }

    public int MtestConnectPctool() {
        return 0;
    }

    public List<Integer> MtestGetWiFiTxRxLevel() {
        return null;
    }

    public int MtestGetWakeUpMode() {
        return 0;
    }

    public Map<String, Integer> MtestGetKeyStatusMap() {
        return null;
    }

    public int MtestEnableOpt(boolean enable) {
        return 0;
    }

    public void TestSetTvRadioCount(int tvCount, int radioCount) {

    }

    public int TestChangeTuner(int tunerType) {
        return 0;
    }

    @Override
    public void BaseHandleMessage(Message msg) {

    }
}
