package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.datastructure.utils.TVTunerParams;
import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.datastructure.sysdata.EnNetworkType;

public class FrontEndModule {
    private static final String TAG = "FrontEndModule";

    private static final int CMD_FE_Base = PrimeDtvMediaPlayer.CMD_Base + 0x500;
    // FrontEnd command
    private static final int CMD_FE_SetAntennaType = CMD_FE_Base + 0x01;
    private static final int CMD_FE_GetAntennaType = CMD_FE_Base + 0x02;
    private static final int CMD_FE_SetAntennaPower = CMD_FE_Base + 0x03; // for atsc & dvb-s
    private static final int CMD_FE_GetAntennaPower = CMD_FE_Base + 0x04; // for atsc & dvb-s
    private static final int CMD_FE_SetConnectParam = CMD_FE_Base + 0x05;
    private static final int CMD_FE_Disconnect = CMD_FE_Base + 0x06;
    private static final int CMD_FE_SetFrequency = CMD_FE_Base + 0x07;
    private static final int CMD_FE_GetFrequency = CMD_FE_Base + 0x08;
    private static final int CMD_FE_SetBandwidth = CMD_FE_Base + 0x09;
    private static final int CMD_FE_GetBandwidth = CMD_FE_Base + 0x0A;
    private static final int CMD_FE_SetSymbolRate = CMD_FE_Base + 0x0B;
    private static final int CMD_FE_GetSymbolRate = CMD_FE_Base + 0x0C;
    private static final int CMD_FE_SetModulation = CMD_FE_Base + 0x0D;
    private static final int CMD_FE_GetModulation = CMD_FE_Base + 0x0E;
    private static final int CMD_FE_GetLockStatus = CMD_FE_Base + 0x0F;
    private static final int CMD_FE_GetSignalQuality = CMD_FE_Base + 0x10;
    private static final int CMD_FE_GetSignalStrength = CMD_FE_Base + 0x11;
    private static final int CMD_FE_SetFakeTuner = CMD_FE_Base + 0x12;
    private static final int CMD_FE_GetBer = CMD_FE_Base + 0x13; // Johnny 20190221 add for mtest ber

    private static final int CMD_FE_SetSat = CMD_FE_Base + 0x20;
    private static final int CMD_FE_GetSat = CMD_FE_Base + 0x21;
    private static final int CMD_FE_SetSatAntenna = CMD_FE_Base + 0x22;
    private static final int CMD_FE_SetLocalCoordinate = CMD_FE_Base + 0x23;
    private static final int CMD_FE_SetSatLNBParameter = CMD_FE_Base + 0x24;
    private static final int CMD_FE_SetSat22kSwitch = CMD_FE_Base + 0x25;
    private static final int CMD_FE_SetDiSEqC10 = CMD_FE_Base + 0x26;
    private static final int CMD_FE_SetDiSEqC11 = CMD_FE_Base + 0x27;
    private static final int CMD_FE_SetMotorLimitPosition = CMD_FE_Base + 0x28;
    private static final int CMD_FE_MoveMotor = CMD_FE_Base + 0x29;
    private static final int CMD_FE_StopMoveMotor = CMD_FE_Base + 0x2A;
    private static final int CMD_FE_ResetMotor = CMD_FE_Base + 0x2B;
    private static final int CMD_FE_SetMotorAutoRotation = CMD_FE_Base + 0x2C;
    private static final int CMD_FE_StoreSatPosition = CMD_FE_Base + 0x2D;
    private static final int CMD_FE_GotoSatPosition = CMD_FE_Base + 0x2E;
    private static final int CMD_FE_CalUSALSAngle = CMD_FE_Base + 0x2F;
    private static final int CMD_FE_GotoUSALSAngle = CMD_FE_Base + 0x30;

    private static final int CMD_FE_SetFrequencyTable = CMD_FE_Base + 0x40; // for atsc
    private static final int CMD_FE_GetFrequencyTable = CMD_FE_Base + 0x41; // for atsc
    private static final int CMD_FE_SetScanType = CMD_FE_Base + 0x42; // for atsc
    private static final int CMD_FE_GetScanType = CMD_FE_Base + 0x43;
    private static final int CMD_FE_AutoScan = CMD_FE_Base + 0x44;
    private static final int CMD_FE_StepScan = CMD_FE_Base + 0x45;
    private static final int CMD_FE_ManualScan = CMD_FE_Base + 0x46;
    private static final int CMD_FE_NitScan = CMD_FE_Base + 0x47;
    private static final int CMD_FE_SatelliteManualScan = CMD_FE_Base + 0x48;
    private static final int CMD_FE_SatelliteBlindScan = CMD_FE_Base + 0x49;
    private static final int CMD_FE_AbortScan = CMD_FE_Base + 0x4A;
    private static final int CMD_FE_AtvFineTune = CMD_FE_Base + 0x4B;

    // JAVA CMD
    // JAVA FE
    private static final int CMD_JAVA_Base = PrimeDtvMediaPlayer.CMD_JAVA_Base;
    private static final int CMD_FE_GetConnectParam = CMD_JAVA_Base + CMD_FE_Base + 0x01;
    private static final int CMD_FE_PauseSearch = CMD_JAVA_Base + CMD_FE_Base + 0x02;
    private static final int CMD_FE_ResumeSearch = CMD_JAVA_Base + CMD_FE_Base + 0x03;
    private static final int CMD_FE_GetScanProgress = CMD_JAVA_Base + CMD_FE_Base + 0x04;
    private static final int CMD_FE_GetScanInfo = CMD_JAVA_Base + CMD_FE_Base + 0x05;


    public int tuner_lock(TVTunerParams tunerParams) {
        //Log.d(TAG, "tuner_lock:tunerID(" + nTunerID + ")signalType(" + multiplexe.getNetworkType()
        //  + ")freq(" + multiplexe.getFrequency() + ")version(" + multiplexe.getVersion()+ ")");

        Log.d(TAG, "tuner_lock: start");
        int antennaType = 0;

        //int synConnect = (true == bsynConnect)? 1 : 0;
        int nTunerID = tunerParams.getTunerId(); //0; //mLocalTunerID;
        int nConnectTimeout = 0;
        boolean bMotorUsed = false;
        int nSaltelliteID = tunerParams.getSatId();
        //int fe =0;
        int qam = 0;
        int bandwidth = 0;
        int version = 0;
        int polar = 0;

        if (tunerParams.getFe_type() == TVTunerParams.FE_TYPE_DVBC) {
            Log.d(TAG, "tunerLock: DVBC");
            antennaType = EnNetworkType.CABLE.getValue();
            version = 0; //EnVersionType (Version_1)
            Log.d(TAG, "tunerLock: getQam()=" + tunerParams.getQam());
            qam = tunerParams.getQam();
        } else if (tunerParams.getFe_type() == TVTunerParams.FE_TYPE_DVBT) { //DVBT
            Log.d(TAG, "tunerLock: DVBT");
            antennaType = EnNetworkType.TERRESTRIAL.getValue();
            version = 0; //EnVersionType (Version_1)
            Log.d(TAG, "tunerLock: getBandwith()=" + tunerParams.getBandwith());
            bandwidth = tunerParams.getBandwith();
        } else if (tunerParams.getFe_type() == TVTunerParams.FE_TYPE_ISDBT) { //ISDBT
            Log.d(TAG, "tunerLock: ISDBT");
            antennaType = EnNetworkType.ISDB_TER.getValue();
            version = 0; //EnVersionType (Version_1)
            Log.d(TAG, "tunerLock: getBandwith()=" + tunerParams.getBandwith());
            bandwidth = tunerParams.getBandwith();
        } else if (tunerParams.getFe_type() == TVTunerParams.FE_TYPE_DVBS) { //DVBS
            Log.d(TAG, "tunerLock: DVBS");
            antennaType = EnNetworkType.SATELLITE.getValue();
            version = 0; //EnVersionType (Version_1)
            Log.d(TAG, "tunerLock: getPolar()=" + tunerParams.getPolar());
            polar = tunerParams.getPolar();
        }
        PrimeDtvMediaPlayer.excute_command(CMD_FE_SetAntennaType, antennaType);

        int motorParamUsed = (bMotorUsed) ? 1 : 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_SetConnectParam);
        request.writeInt(nTunerID);
        request.writeInt(antennaType);

        if (EnNetworkType.CABLE.getValue() == antennaType) // DVBC
        {
            //DVBCChannelDot multC = (DVBCChannelDot) multiplexe;
            Log.d(TAG, "tuner_lock: frequency:" + tunerParams.getFrequency() + ", symbolrate:" + tunerParams.getSymbolRate() + ", qam:" + qam + ", version:" + version);
            request.writeInt(tunerParams.getFrequency());
            request.writeInt(tunerParams.getSymbolRate());
            request.writeInt(qam);
            request.writeInt(version);
        } else if (EnNetworkType.TERRESTRIAL.getValue() == antennaType //DVBT
                || EnNetworkType.DTMB.getValue() == antennaType // DTMB
                || EnNetworkType.ISDB_TER.getValue() == antennaType)//ISDBT
        {
            //DVBTChannelDot multT = (DVBTChannelDot) multiplexe;
            Log.d(TAG, "tuner_lock: frequency:" + tunerParams.getFrequency() + ", bandwidth:" + bandwidth + ", qam:" + qam + ", version:" + version);
            request.writeInt(/*multT.getFrequency()*/tunerParams.getFrequency());
            request.writeInt(/*multT.getBandWidth()*/bandwidth);
            request.writeInt(/*multT.getModulation().getValue()*/qam);
            request.writeInt(/*multT.getVersion().ordinal()*/version);
        } else if (EnNetworkType.SATELLITE.getValue() == antennaType) // DVBS
        {
            //DVBSTransponder tp = (DVBSTransponder) multiplexe;
            Log.d(TAG, "tuner_lock: frequency:" + tunerParams.getFrequency() + ", SymbolRate:" + tunerParams.getSymbolRate() + ", qam:" + qam + ", version:" + version + ", polar:" + (polar == 1 ? "H" : "V"));
            request.writeInt(/*tp.getFrequency()*/tunerParams.getFrequency());
            request.writeInt(/*tp.getSymbolRate()*/tunerParams.getSymbolRate());
            request.writeInt(/*tp.getPolarity().ordinal()*/polar);
            request.writeInt(/*tp.getVersion().ordinal()*/version);
        }

        Log.d(TAG, "tuner_lock: SatID = " + tunerParams.getSatId() + " TpID = " + tunerParams.getTpId());
        request.writeInt(tunerParams.getSatId());   // Johnny 20180814 send satID to service in tunerLock
        request.writeInt(tunerParams.getTpId());    // Johnny 20180814 send tpID to service in tunerLock

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        Log.d(TAG, "tuner_lock: ret=" + ret);

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public boolean is_tuner_lock(int tuner_id) {
//        Log.d(TAG, "get_tuner_status: tunerID (" + tuner_id + ")");
        int  lockStatus = PrimeDtvMediaPlayer.excute_command_getII(CMD_FE_GetLockStatus, tuner_id);
        //Log.d(TAG, "get_tuner_status: lockStatus="+ lockStatus);
        return lockStatus == 1;
    }

    public int get_signal_strength(int nTunerID) {
        //Log.v(TAG, "get_signal_strength: tunerID (" + nTunerID + ")");
        int signalStrength = 0;
        signalStrength = PrimeDtvMediaPlayer.excute_command_getII(CMD_FE_GetSignalStrength, nTunerID);
        //Log.d(TAG, "get_signal_strength: signalStrength="+ signalStrength);
        return signalStrength;
    }

    public int get_signal_quality(int nTunerID) {
        //Log.v(TAG, "get_signal_quality: tunerID (" + nTunerID + ")");
        int signalQuality = 0;
        signalQuality = PrimeDtvMediaPlayer.excute_command_getII(CMD_FE_GetSignalQuality, nTunerID);
        //Log.d(TAG, "get_signal_quality: signalQuality="+ signalQuality);
        return signalQuality;
    }

    public int get_signal_snr(int nTunerID) {
        Log.d(TAG, "get_signal_snr: tunerID (" + nTunerID + ")");
        int signalSNR = 0;
        return PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        //signalSNR = PrimeDtvMediaPlayer.excuteCommandGetII(CMD_FE_GetSignalSNR, nTunerID);
        //Log.d(TAG, "get_signal_snr: signalSNR="+ signalSNR);
        //return signalSNR;
    }

    public String get_signal_ber(int nTunerID) {
//        Log.d(TAG, "get_signal_ber: tunerID (" + nTunerID + ")");
        String signalBER = "";

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_GetBer);
        request.writeInt(nTunerID);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == reply.readInt()) {
            signalBER = reply.readString();
        }
        request.recycle();
        reply.recycle();
        //Log.d(TAG, "get_signal_ber: signalBER="+ signalBER);
        return signalBER;
    }

    public int set_fake_tuner(int openFlag)//Scoty 20180809 add fake tuner command
    {
        return PrimeDtvMediaPlayer.excute_command(CMD_FE_SetFakeTuner, openFlag);
    }

    public int set_antenna_5v(int tuner_id, int onOff) {
        Log.d(TAG, "set_antenna_5v: onOff = " + onOff);
        return PrimeDtvMediaPlayer.excute_command(CMD_FE_SetAntennaPower, tuner_id, onOff);
    }

    // Johnny 20180814 add setDiseqc1.0 port -s
    public int set_diseqc10_port_info(int nTuerID, int nPort, int n22KSwitch, int nPolarity) {
        Log.d(TAG, "set_diseqc10_port_info:tunerID(" + nTuerID + ")cmdType(" + 0 + ")port(" + nPort
                + ")polar" + nPolarity + ")switch(" + n22KSwitch + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_SetDiSEqC10);
        request.writeInt(nTuerID);
        request.writeInt(nPort);
        request.writeInt(nPolarity);
        request.writeInt(n22KSwitch);
        request.writeInt(0); // diseqc level

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    //Scoty add DiSeqC Motor rule -s
    public int set_diseqc12_move_motor(int nTunerId, int Direct, int Step) {
        Log.d(TAG, "set_diseqc12_move_motor: ===>>> Direct = " + Direct + " Step = " + Step);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_MoveMotor);
        request.writeInt(nTunerId);
        request.writeInt(Direct);// 0: Move east, 1: Move west, 2: Invalid value
        request.writeInt(Step);//0 mean running continus; 1~128 mean running steps every time
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);

    }

    public int set_diseqc12_move_motor_stop(int nTunerId) {
        Log.d(TAG, "set_diseqc12_move_motor_stop: ===>> IN");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_StopMoveMotor);
        request.writeInt(nTunerId);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int reset_diseqc12_position(int nTunerId) {
        Log.d(TAG, "reset_diseqc12_position: ===>>> IN");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_ResetMotor);
        request.writeInt(nTunerId);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int set_diseqc_limit_pos(int nTunerId, int limitType) {
        Log.d(TAG, "set_diseqc_limit_pos: limitType = " + limitType);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_SetMotorLimitPosition);
        request.writeInt(nTunerId);
        request.writeInt(limitType);//0:Disable Limits , 1:Set East Limit, 2:Set West Limit, 3:Invalid value
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }
    //Scoty add DiSeqC Motor rule -e
    // Johnny 20180814 add setDiseqc1.0 port -e
}
