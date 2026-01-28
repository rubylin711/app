package com.prime.dtv.service.Tuner;

import android.media.tv.tuner.Lnb;
import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.frontend.DvbsCodeRate;
import android.media.tv.tuner.frontend.DvbsFrontendSettings;
import android.util.Log;

import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.TVTunerParams;

public class TunerDVBS extends TunerBase {
    private static final String TAG = "TunerDVBS";

    public TunerDVBS(Tuner tuner) {
        super(tuner);
    }

    public TunerDVBS() {
        super();
    }

    @Override
    protected boolean tune(TpInfo tpInfo) {
//        ExecutorService mExecutor;
        if (tpInfo == null) {
            Log.e(TAG, "tune: null tpinfo");
            return false;
        }

        DvbsFrontendSettings frontendSettings;
        int result;
        //tpInfo.SatTp.setFec(3);//FEC_5_6
        //tpInfo.SatTp.setFreq(1756);
        //tpInfo.SatTp.setSymbol(34000);
        //tpInfo.SatTp.setMod(4);//8PSK
        setTpInfo(tpInfo);
        DvbsCodeRate dvbsCodeRate = DvbsCodeRate.builder()
                .setInnerFec(getDvbSFec(tpInfo.SatTp.getFec()))
                .setLinear(false)
                .setShortFrameEnabled(false)
                .setBitsPer1000Symbol(0)
                .build();
        Log.d(TAG, "tune(TpInfo tpInfo) tpInfo.SatTp.getFreq() = " + tpInfo.SatTp.getFreq());
        frontendSettings = DvbsFrontendSettings.builder()
                .setFrequencyLong(tpInfo.SatTp.getFreq() * 1000L)
                .setSymbolRate(tpInfo.SatTp.getSymbol() * 1000)
                .setModulation(getFrameworkModulation(tpInfo.SatTp.getMod()))
                .setCodeRate(dvbsCodeRate)
                .setStandard(DvbsFrontendSettings.STANDARD_S)
                .build();

        result = getTuner().tune(frontendSettings);

        Log.d(TAG, "FrontendInfo: " + getTuner().getFrontendInfo());
        Log.d(TAG, "tune: freq = " + frontendSettings.getFrequencyLong());
        Log.d(TAG, "tune: sym = " + frontendSettings.getSymbolRate());
        Log.d(TAG, "tune: modulation = " + getModulationName(frontendSettings.getModulation()));
        Log.d(TAG, "tune: fec = " + getDvbSFecName(tpInfo.SatTp.getFec()));
        Log.d(TAG, "tune: result error = " + result);
        Log.d(TAG, "tune: result = " + (result == Tuner.RESULT_SUCCESS ? "Success" : "Fail"));
        return result == Tuner.RESULT_SUCCESS;
    }

    @Override
    protected boolean tune(TVTunerParams tunerParams) {
        if (tunerParams == null) {
            Log.e(TAG, "tune: null tunerParams");
            return false;
        }

        DvbsFrontendSettings frontendSettings;
        int result;
        DvbsCodeRate dvbsCodeRate = DvbsCodeRate.builder()
                .setInnerFec(getDvbSFec(tunerParams.getFec()))
                .setLinear(false)
                .setShortFrameEnabled(false)
                .setBitsPer1000Symbol(0)
                .build();
        frontendSettings = DvbsFrontendSettings.builder()
                .setFrequencyLong(tunerParams.getFrequency() * 1000L)
                .setSymbolRate(tunerParams.getSymbolRate() * 1000)
                .setModulation(getFrameworkModulation(tunerParams.getMod()))
                .setCodeRate(dvbsCodeRate)
                .setStandard(DvbsFrontendSettings.STANDARD_S)//ruby need to check
                .build();

        result = getTuner().tune(frontendSettings);
        Log.d(TAG, "FrontendInfo: " + getTuner().getFrontendInfo());
        Log.d(TAG, "tune: freq = " + frontendSettings.getFrequencyLong());
        Log.d(TAG, "tune: sym = " + frontendSettings.getSymbolRate());
        Log.d(TAG, "tune: modulation = " + getModulationName(frontendSettings.getModulation()));
        Log.d(TAG, "tune: fec = " + getDvbSFecName(tunerParams.getFec()));
        Log.d(TAG, "tune: result error = " + result);
        Log.d(TAG, "tune: result = " + (result == Tuner.RESULT_SUCCESS ? "Success" : "Fail"));
        return (result == Tuner.RESULT_SUCCESS);
    }

    @Override
    protected boolean tune(TpInfo tpInfo, SatInfo satInfo) {
//        ExecutorService mExecutor;
        boolean use22khz = false;
//        int THREAD_COUNT = 4;
        if (tpInfo == null) {
            Log.e(TAG, "tune: null tpinfo");
            return false;
        }

        DvbsFrontendSettings frontendSettings;
        int result;
        //tpInfo.SatTp.setFec(3);//FEC_5_6
        //tpInfo.SatTp.setFreq(1756);
        //tpInfo.SatTp.setSymbol(34000);
        //tpInfo.SatTp.setMod(4);//8PSK
        setTpInfo(tpInfo);
        DvbsCodeRate dvbsCodeRate = DvbsCodeRate.builder()
                .setInnerFec(getDvbSFec(tpInfo.SatTp.getFec()))
                .setLinear(false)
                .setShortFrameEnabled(false)
                .setBitsPer1000Symbol(0)
                .build();
        Log.d(TAG, "tune(TpInfo tpInfo, SatInfo satInfo) tpInfo.SatTp.getFreq() = " + tpInfo.SatTp.getFreq());
        int tunerInputFreq = 0;
        int lnb = -1;
        int tone_22k = 0;
        int lofrq_val = 0;
        int pol_val = 0;

        if((satInfo.Antenna.getLnbType() == SatInfo.LNB_TYPE_UNIVERSAL) ||(satInfo.Antenna.getLnbType() == SatInfo.LNB_TYPE_LNBF)) {
            lnb = satInfo.get_lnb_select(tpInfo);
            use22khz = true;
        }else if(satInfo.Antenna.getLnbType() == 0){
            lnb = 0;
        }else if(satInfo.Antenna.getLnbType() == 3){
            lnb = 0;
        }else if(satInfo.Antenna.getLnbType() == 4){
            use22khz = false;
            if (tpInfo.SatTp.getFreq() < 4000) {
                lnb = 0;
            } else {
                lnb = 1;
            }
        }else if(satInfo.Antenna.getLnbType() == 5){
            use22khz = false;
            if (tpInfo.SatTp.getFreq() < 4000) {
                lnb = 1;
            } else {
                lnb = 0;
            }
        }else if(satInfo.Antenna.getLnbType() == 6){
            use22khz = true;
            if (tpInfo.SatTp.getFreq() < 11700) {
                lnb = 0;
            } else {
                lnb = 1;
            }
        }

        switch (lnb)
        {
            case 0:
                lofrq_val = satInfo.Antenna.getLnb1();
                break;
            case 1:
                lofrq_val = satInfo.Antenna.getLnb2();
                break;
            case 2:
                lofrq_val = 5150;
                break;
            default:
                return false;
        }

        tunerInputFreq= Math.abs(lofrq_val-tpInfo.SatTp.getFreq());

        if(satInfo.Antenna.getTone22k() == 2) {//22khz auto
            if (use22khz)//(satInfo.Antenna.getLnbType() == SatInfo.LNB_TYPE_UNIVERSAL)
                tone_22k = lnb;
            else
                tone_22k = 0;
        }else{
            tone_22k = satInfo.Antenna.getTone22k();
        }

        Lnb mLnb = getLNB() ;
        if (mLnb != null) {
            if(satInfo.Antenna.getV1418Use() == 0){
                Log.d(TAG, "@@@@@@ mLnb.setVoltage(Lnb.VOLTAGE_NONE)");
                mLnb.setVoltage(Lnb.VOLTAGE_NONE);
            }else{
                if(tpInfo.SatTp.getPolar() == TpInfo.Sat.POLAR_V) {
                    Log.d(TAG, "@@@@@@ mLnb.setVoltage(Lnb.VOLTAGE_13V)");
                    mLnb.setVoltage(Lnb.VOLTAGE_13V);
                    pol_val = 0;
                }
                else {
                    Log.d(TAG, "@@@@@@ mLnb.setVoltage(Lnb.VOLTAGE_18V)");
                    mLnb.setVoltage(Lnb.VOLTAGE_18V);
                    pol_val = 1;
                }
            }
            if (tone_22k == 0) {
                Log.d(TAG, "@@@@@@ mLnb.setTone(Lnb.TONE_NONE)");
                mLnb.setTone(Lnb.TONE_NONE);
            }
            else {
                Log.d(TAG, "@@@@@@ mLnb.setTone(Lnb.TONE_CONTINUOUS)");
                mLnb.setTone(Lnb.TONE_CONTINUOUS);
            }
            //casper20241231 diseqc--s
            if(satInfo.Antenna.getDiseqcType() == 2){
                int port = satInfo.Antenna.getDiseqc();
                byte[] diseqcCommand = new byte[]{(byte) 0xE0,
                        (byte) 0x10,
                        (byte) 0x38,
                        (byte) (0xF0+ port * 4 + pol_val * 2 +tone_22k)};
                int ret_diseqc = mLnb.sendDiseqcMessage(diseqcCommand);
                if (ret_diseqc == 0) {
                    Log.d(TAG, "DiSEqC command sent successfully.");
                } else {
                    Log.e(TAG, "Failed to send DiSEqC command.");
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                mLnb.sendDiseqcMessage(diseqcCommand);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (tone_22k == 0) {
                    Log.d(TAG, "@@@@@@ mLnb.setTone(Lnb.TONE_NONE)");
                    mLnb.setTone(Lnb.TONE_NONE);
                }
                else {
                    Log.d(TAG, "@@@@@@ mLnb.setTone(Lnb.TONE_CONTINUOUS)");
                    mLnb.setTone(Lnb.TONE_CONTINUOUS);
                }
            }
            //casper20241221 diseqc--e
        }
        else{
            Log.e(TAG, "@@@@@@ openLnb fail");
        }

        frontendSettings = DvbsFrontendSettings.builder()
                .setFrequencyLong(tunerInputFreq * 1000 * 1000L)
                .setSymbolRate(tpInfo.SatTp.getSymbol() * 1000)
                .setModulation(getFrameworkModulation(tpInfo.SatTp.getMod()))
                .setCodeRate(dvbsCodeRate)
                .setStandard(DvbsFrontendSettings.STANDARD_S)
                .build();
        setCancelTuning(false);
        result = getTuner().tune(frontendSettings);

        Log.d(TAG, "FrontendInfo: " + getTuner().getFrontendInfo());
        Log.d(TAG, "tune: freq = " + frontendSettings.getFrequencyLong());
        Log.d(TAG, "tune: sym = " + frontendSettings.getSymbolRate());
        Log.d(TAG, "tune: modulation = " + getModulationName(frontendSettings.getModulation()));
        Log.d(TAG, "tune: fec = " + getDvbSFecName(tpInfo.SatTp.getFec()));
        Log.d(TAG, "tune: result error = " + result);
        Log.d(TAG, "tune: result = " + (result == Tuner.RESULT_SUCCESS ? "Success" : "Fail"));
        return result == Tuner.RESULT_SUCCESS;
    }

    private long getDvbSFec(int position) {
        switch (position) {
            case 0:
                return DvbsFrontendSettings.FEC_3_5;
            case 1:
                return DvbsFrontendSettings.FEC_2_3;
            case 2:
                return DvbsFrontendSettings.FEC_3_4;
            case 3:
                return DvbsFrontendSettings.FEC_5_6;
            case 4:
                return DvbsFrontendSettings.FEC_8_9;
            case 8:
                return DvbsFrontendSettings.FEC_9_10;
        }
        return DvbsFrontendSettings.FEC_3_5;
    }

    private String getDvbSFecName(int position) {
        switch (position) {
            case TpInfo.Sat.FEC_1_2:
                return "FEC_1_2";
            case TpInfo.Sat.FEC_2_3:
                return "FEC_2_3";
            case TpInfo.Sat.FEC_3_4:
                return "FEC_3_4";
            case TpInfo.Sat.FEC_5_6:
                return "FEC_5_6";
            case TpInfo.Sat.FEC_7_8:
                return "FEC_7_8";
            case TpInfo.Sat.FEC_8_9:
                return "FEC_8_9";
            case TpInfo.Sat.FEC_3_5:
                return "FEC_3_5";
            case TpInfo.Sat.FEC_4_5:
                return "FEC_4_5";
            case TpInfo.Sat.FEC_9_10:
                return "FEC_9_10";
            default:
                Log.d(TAG, "getDvbSFecName: undefined framework FEC");
                return "FEC_UNDEFINED";
        }
    }

    private int getFrameworkModulation(int pesiModulation) {
        switch (pesiModulation) {
            case TpInfo.Sat.MOD_AUTO:
                return DvbsFrontendSettings.MODULATION_AUTO;
            case TpInfo.Sat.MOD_QPSK:
                return DvbsFrontendSettings.MODULATION_MOD_QPSK;
            case TpInfo.Sat.MOD_8PSK:
                return DvbsFrontendSettings.MODULATION_MOD_8PSK;
            case TpInfo.Sat.MOD_16QAM:
                return DvbsFrontendSettings.MODULATION_MOD_16QAM;
            case TpInfo.Sat.MOD_16PSK:
                return DvbsFrontendSettings.MODULATION_MOD_16PSK;
            case TpInfo.Sat.MOD_32PSK:
                return DvbsFrontendSettings.MODULATION_MOD_32PSK;
            case TpInfo.Sat.MOD_ACM:
                return DvbsFrontendSettings.MODULATION_MOD_ACM;
            case TpInfo.Sat.MOD_8APSK:
                return DvbsFrontendSettings.MODULATION_MOD_8APSK;
            case TpInfo.Sat.MOD_16APSK:
                return DvbsFrontendSettings.MODULATION_MOD_16APSK;
            case TpInfo.Sat.MOD_32APSK:
                return DvbsFrontendSettings.MODULATION_MOD_32APSK;
            case TpInfo.Sat.MOD_64APSK:
                return DvbsFrontendSettings.MODULATION_MOD_64APSK;
            case TpInfo.Sat.MOD_128APSK:
                return DvbsFrontendSettings.MODULATION_MOD_128APSK;
            case TpInfo.Sat.MOD_256APSK:
                return DvbsFrontendSettings.MODULATION_MOD_256APSK;
            default:
                Log.d(TAG, "getFrameworkModulation: undefined pesi qam");
                return DvbsFrontendSettings.MODULATION_UNDEFINED;
        }
    }

    private String getModulationName(int frameworkModulation) {
        switch (frameworkModulation) {
            case DvbsFrontendSettings.MODULATION_AUTO:
                return "MODULATION_AUTO";
            case DvbsFrontendSettings.MODULATION_MOD_QPSK:
                return "MODULATION_MOD_QPSK";
            case DvbsFrontendSettings.MODULATION_MOD_8PSK:
                return "MODULATION_MOD_8PSK";
            case DvbsFrontendSettings.MODULATION_MOD_16QAM:
                return "MODULATION_MOD_16QAM";
            case DvbsFrontendSettings.MODULATION_MOD_16PSK:
                return "MODULATION_MOD_16PSK";
            case DvbsFrontendSettings.MODULATION_MOD_32PSK:
                return "MODULATION_MOD_32PSK";
            case DvbsFrontendSettings.MODULATION_MOD_ACM:
                return "MODULATION_MOD_ACM";
            case DvbsFrontendSettings.MODULATION_MOD_8APSK:
                return "MODULATION_MOD_8APSK";
            case DvbsFrontendSettings.MODULATION_MOD_16APSK:
                return "MODULATION_MOD_16APSK";
            case DvbsFrontendSettings.MODULATION_MOD_32APSK:
                return "MODULATION_MOD_32APSK";
            case DvbsFrontendSettings.MODULATION_MOD_64APSK:
                return "MODULATION_MOD_64APSK";
            case DvbsFrontendSettings.MODULATION_MOD_128APSK:
                return "MODULATION_MOD_128APSK";
            case DvbsFrontendSettings.MODULATION_MOD_256APSK:
                return "MODULATION_MOD_256APSK";
            default:
                Log.d(TAG, "getModulationName: undefined framework modulation");
                return "MODULATION_UNDEFINED";
        }
    }
//    private LnbCallback mLnbCallback = new LnbCallback() {
//        @Override
//        public void onEvent(int i) {
//            Log.d(TAG, "LnbCallback : onEvent " + i );
//        }
//
//        @Override
//        public void onDiseqcMessage(byte[] bytes) {
//
//        }
//    };
}

