package com.prime.dtv.service.Tuner;

import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.frontend.DvbcFrontendSettings;
import android.media.tv.tuner.frontend.FrontendSettings;
import android.media.tv.tuner.frontend.IsdbtFrontendSettings;
import android.util.Log;

import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.TVTunerParams;

public class TunerISDBT extends TunerBase {
    private static final String TAG = "TunerISDBT";

    // change by actual environment
    // TODO: try not to use fixed values
    private static final int SETTING_ANNEX = DvbcFrontendSettings.ANNEX_A;
    private static final int SETTING_SPECTRAL_INVERSION = FrontendSettings.FRONTEND_SPECTRAL_INVERSION_NORMAL;

    public TunerISDBT(Tuner tuner) {
        super(tuner);
    }

    public TunerISDBT() {
        super();
    }
//    @Override
//    protected boolean checkTpInfo(TpInfo tpInfo) {
//        TpInfo storedTpInfo = getTpInfo();
//
//        if (storedTpInfo == null) {
//            return false;
//        }
//
//        return tpInfo.CableTp.getFreq() == storedTpInfo.CableTp.getFreq()
//                && tpInfo.CableTp.getSymbol() == storedTpInfo.CableTp.getSymbol()
//                && tpInfo.CableTp.getQam() == storedTpInfo.CableTp.getQam()
//                && tpInfo.getTunerType() == TpInfo.DVBC;
//    }

    @Override
    protected boolean tune(TpInfo tpInfo) {

        if (tpInfo == null) {
            Log.e(TAG, "tune: null tpinfo");
            return false;
        }

        boolean result = false;
        result = tune_ISDBT(tpInfo.TerrTp.getFreq(), tpInfo.TerrTp.getBand());
        return result;
/*
        IsdbtFrontendSettings frontendSettings;
        int result=0;

        setTpInfo(tpInfo);
        Log.d("Scan","tpInfo.TerrTp.getFreq() = "+tpInfo.TerrTp.getFreq());
        frontendSettings = IsdbtFrontendSettings.builder()
                // e.g. freq = 482000000
                .setFrequencyLong(tpInfo.TerrTp.getFreq()*1000L)
                // e.g. we use 5200 previously but framework need 5200000
                //.setSymbolRate(tunerParams.getSymbolRate()*1000)
                // from our qam define to framework modulation define
                //.setModulation(getFrameworkModulation(tunerParams.getQam()))
                // Annex change by actual environment
                // our stream player and kbro live are annex A/C
                //.setAnnex(SETTING_ANNEX)
                // SpectralInversion change by actual environment
                // our stiream player is FRONTEND_SPECTRAL_INVERSION_NORMAL
                // kbro lve is FRONTEND_SPECTRAL_INVERSION_INVERTED in A1B
                //.setSpectralInversion(SETTING_SPECTRAL_INVERSION)

//                .setInnerFec(DvbcFrontendSettings.FEC_AUTO)
//                .setOuterFec(DvbcFrontendSettings.OUTER_FEC_OUTER_FEC_NONE)
                .setModulation(IsdbtFrontendSettings.MODULATION_AUTO)
                .setBandwidth(IsdbtFrontendSettings.BANDWIDTH_6MHZ)
                .setMode(IsdbtFrontendSettings.MODE_AUTO)
                .build();
        setCancelTuning(false);
        result = getTuner().tune(frontendSettings);

        Log.d(TAG, "tune: freq = " + frontendSettings.getFrequencyLong());
        //Log.d(TAG, "tune: sym = " + frontendSettings.getSymbolRate());
        Log.d(TAG, "tune: band = " + frontendSettings.getBandwidth());
       // Log.d(TAG, "tune: qam = " + getModulationName(frontendSettings.getModulation()));
        Log.d(TAG, "tune: result error = " + result);
        Log.d(TAG, "tune: result = " + (result == Tuner.RESULT_SUCCESS ? "Success" : "Fail"));


        return result == Tuner.RESULT_SUCCESS;
*/
    }

    @Override
    protected boolean tune(TVTunerParams tunerParams) {
        if (tunerParams == null) {
            Log.e(TAG, "tune: null tunerParams");
            return false;
        }

        boolean result = false;
        result = tune_ISDBT(tunerParams.getFrequency(), tunerParams.getBandwith());
        return result;
/*
        IsdbtFrontendSettings frontendSettings;
        int result=0;

        frontendSettings = IsdbtFrontendSettings.builder()
                // e.g. freq = 482000000
                .setFrequencyLong(tunerParams.getFrequency()*1000L)
                // e.g. we use 5200 previously but framework need 5200000
                //.setSymbolRate(tunerParams.getSymbolRate()*1000)
                // from our qam define to framework modulation define
                //.setModulation(getFrameworkModulation(tunerParams.getQam()))
                // Annex change by actual environment
                // our stream player and kbro live are annex A/C
                //.setAnnex(SETTING_ANNEX)
                // SpectralInversion change by actual environment
                // our stiream player is FRONTEND_SPECTRAL_INVERSION_NORMAL
                // kbro lve is FRONTEND_SPECTRAL_INVERSION_INVERTED in A1B
                //.setSpectralInversion(SETTING_SPECTRAL_INVERSION)

//                .setInnerFec(DvbcFrontendSettings.FEC_AUTO)
//                .setOuterFec(DvbcFrontendSettings.OUTER_FEC_OUTER_FEC_NONE)
                .setModulation(IsdbtFrontendSettings.MODULATION_AUTO)
                .setBandwidth(IsdbtFrontendSettings.BANDWIDTH_6MHZ)
                .setMode(IsdbtFrontendSettings.MODE_AUTO)
                .build();

        result = getTuner().tune(frontendSettings);

        Log.d(TAG, "tune: freq = " + frontendSettings.getFrequencyLong());
        //Log.d(TAG, "tune: sym = " + frontendSettings.getSymbolRate());
        Log.d(TAG, "tune: band = " + frontendSettings.getBandwidth());
        Log.d(TAG, "tune: qam = " + getModulationName(frontendSettings.getModulation()));
        Log.d(TAG, "tune: result error = " + result);
        Log.d(TAG, "tune: result = " + (result == Tuner.RESULT_SUCCESS ? "Success" : "Fail"));


        return result == Tuner.RESULT_SUCCESS;
*/
    }

    @Override
    protected boolean tune(TpInfo tpInfo, SatInfo satInfo) {
        if (tpInfo == null) {
            Log.e(TAG, "tune: null tpinfo");
            return false;
        }

        boolean result = false;
        result = tune_ISDBT(tpInfo.TerrTp.getFreq(), tpInfo.TerrTp.getBand());
        return result;
/*
        IsdbtFrontendSettings frontendSettings;
        int result=0;

        setTpInfo(tpInfo);
        Log.d(TAG,"tpInfo.TerrTp.getFreq() = "+tpInfo.TerrTp.getFreq());
        frontendSettings = IsdbtFrontendSettings.builder()
                // e.g. freq = 482000000
                .setFrequencyLong(tpInfo.TerrTp.getFreq()*1000L)
                // e.g. we use 5200 previously but framework need 5200000
                //.setSymbolRate(tunerParams.getSymbolRate()*1000)
                // from our qam define to framework modulation define
                //.setModulation(getFrameworkModulation(tunerParams.getQam()))
                // Annex change by actual environment
                // our stream player and kbro live are annex A/C
                //.setAnnex(SETTING_ANNEX)
                // SpectralInversion change by actual environment
                // our stiream player is FRONTEND_SPECTRAL_INVERSION_NORMAL
                // kbro lve is FRONTEND_SPECTRAL_INVERSION_INVERTED in A1B
                //.setSpectralInversion(SETTING_SPECTRAL_INVERSION)

//                .setInnerFec(DvbcFrontendSettings.FEC_AUTO)
//                .setOuterFec(DvbcFrontendSettings.OUTER_FEC_OUTER_FEC_NONE)
                .setModulation(IsdbtFrontendSettings.MODULATION_AUTO)
                .setBandwidth(IsdbtFrontendSettings.BANDWIDTH_6MHZ)
                .setMode(IsdbtFrontendSettings.MODE_AUTO)
                .build();

        result = getTuner().tune(frontendSettings);

        Log.d(TAG, "tune: freq = " + frontendSettings.getFrequencyLong());
        //Log.d(TAG, "tune: sym = " + frontendSettings.getSymbolRate());
        Log.d(TAG, "tune: band = " + frontendSettings.getBandwidth());
        //Log.d(TAG, "tune: qam = " + getModulationName(frontendSettings.getModulation()));
        Log.d(TAG, "tune: result error = " + result);
        Log.d(TAG, "tune: result = " + (result == Tuner.RESULT_SUCCESS ? "Success" : "Fail"));


        return result == Tuner.RESULT_SUCCESS;
*/

    }

    protected boolean tune_ISDBT(int freq, int band) {
        IsdbtFrontendSettings frontendSettings;
        int result=0;

        Log.d(TAG,"freq = "+freq);
        frontendSettings = IsdbtFrontendSettings.builder()
                // e.g. freq = 482000000
                .setFrequencyLong(freq*1000L)
                // e.g. we use 5200 previously but framework need 5200000
                //.setSymbolRate(tunerParams.getSymbolRate()*1000)
                // from our qam define to framework modulation define
                //.setModulation(getFrameworkModulation(tunerParams.getQam()))
                // Annex change by actual environment
                // our stream player and kbro live are annex A/C
                //.setAnnex(SETTING_ANNEX)
                // SpectralInversion change by actual environment
                // our stiream player is FRONTEND_SPECTRAL_INVERSION_NORMAL
                // kbro lve is FRONTEND_SPECTRAL_INVERSION_INVERTED in A1B
                //.setSpectralInversion(SETTING_SPECTRAL_INVERSION)

//                .setInnerFec(DvbcFrontendSettings.FEC_AUTO)
//                .setOuterFec(DvbcFrontendSettings.OUTER_FEC_OUTER_FEC_NONE)
                .setModulation(IsdbtFrontendSettings.MODULATION_AUTO)
                .setBandwidth(IsdbtFrontendSettings.BANDWIDTH_6MHZ)
                .setMode(IsdbtFrontendSettings.MODE_AUTO)
                .build();
        setCancelTuning(false);
        result = getTuner().tune(frontendSettings);

        Log.d(TAG, "tune: freq = " + frontendSettings.getFrequencyLong());
        //Log.d(TAG, "tune: sym = " + frontendSettings.getSymbolRate());
        Log.d(TAG, "tune: band = " + frontendSettings.getBandwidth());
        //Log.d(TAG, "tune: qam = " + getModulationName(frontendSettings.getModulation()));
        Log.d(TAG, "tune: result error = " + result);
        Log.d(TAG, "tune: result = " + (result == Tuner.RESULT_SUCCESS ? "Success" : "Fail"));

        return result == Tuner.RESULT_SUCCESS;
    }


    private int getFrameworkModulation(int pesiQam) {
        switch (pesiQam) {
            case TpInfo.Cable.QAM_AUTO:
                return DvbcFrontendSettings.MODULATION_AUTO; // broadcom no AUTO : mantis 0004981
            case TpInfo.Cable.QAM_16:
                return DvbcFrontendSettings.MODULATION_MOD_16QAM;
            case TpInfo.Cable.QAM_32:
                return DvbcFrontendSettings.MODULATION_MOD_32QAM;
            case TpInfo.Cable.QAM_64:
                return DvbcFrontendSettings.MODULATION_MOD_64QAM;
            case TpInfo.Cable.QAM_128:
                return DvbcFrontendSettings.MODULATION_MOD_128QAM;
            case TpInfo.Cable.QAM_256:
                return DvbcFrontendSettings.MODULATION_MOD_256QAM;
            default:
                Log.d(TAG, "getFrameworkModulation: undefined pesi qam");
                return DvbcFrontendSettings.MODULATION_UNDEFINED;
        }
    }

    private String getModulationName(int frameworkModulation) {
        switch (frameworkModulation) {
            case DvbcFrontendSettings.MODULATION_AUTO:
                return "MODULATION_AUTO";
            case DvbcFrontendSettings.MODULATION_MOD_16QAM:
                return "MODULATION_MOD_16QAM";
            case DvbcFrontendSettings.MODULATION_MOD_32QAM:
                return "MODULATION_MOD_32QAM";
            case DvbcFrontendSettings.MODULATION_MOD_64QAM:
                return "MODULATION_MOD_64QAM";
            case DvbcFrontendSettings.MODULATION_MOD_128QAM:
                return "MODULATION_MOD_128QAM";
            case DvbcFrontendSettings.MODULATION_MOD_256QAM:
                return "MODULATION_MOD_256QAM";
            default:
                Log.d(TAG, "getModulationName: undefined framework modulation");
                return "MODULATION_UNDEFINED";
        }
    }
}
