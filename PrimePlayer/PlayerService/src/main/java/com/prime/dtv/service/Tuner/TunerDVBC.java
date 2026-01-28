package com.prime.dtv.service.Tuner;

import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.frontend.DvbcFrontendSettings;
import android.media.tv.tuner.frontend.FrontendSettings;
import android.util.Log;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVTunerParams;
import com.prime.dtv.service.Tuner.TunerBase;
public class TunerDVBC extends TunerBase {
    private static final String TAG = "TunerDVBC";

    // change by actual environment
    // TODO: try not to use fixed values
    private static final int SETTING_ANNEX = DvbcFrontendSettings.ANNEX_A;
    private static final int SETTING_SPECTRAL_INVERSION = FrontendSettings.FRONTEND_SPECTRAL_INVERSION_NORMAL;

    public TunerDVBC(Tuner tuner) {
        super(tuner);
    }

    public TunerDVBC() {
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

    public boolean IsSameTp(TpInfo ptp){
        boolean rel = false;

        if(getTpInfo() != null){
            if(ptp.CableTp.getFreq() == getTpInfo().CableTp.getFreq() &&
                ptp.CableTp.getSymbol() == getTpInfo().CableTp.getSymbol() &&
                ptp.CableTp.getQam() == getTpInfo().CableTp.getQam()){
                LogUtils.d("tuner ["+get_Id()+"] It is the same TP, skip tuning");
                rel = true;
            }
        }else{
            LogUtils.i("tuner ["+get_Id()+"] mTpInfo is null !!!!!!!!!!!!");
        }
        return rel;
    }

    @Override
    protected boolean tune(TpInfo tpInfo) {
        if (tpInfo == null) {
            Log.e(TAG, "tuner ["+get_Id()+"] tune: null tpinfo");
            return false;
        }

        DvbcFrontendSettings frontendSettings;
        int result;

        if(IsSameTp(tpInfo))
            return true;
        cancelTune();

        setTpInfo(tpInfo);
        Log.d(TAG,"111 tuner ["+get_Id()+"] tpInfo.CableTp.getFreq() = "+tpInfo.CableTp.getFreq());
        frontendSettings = DvbcFrontendSettings.builder()
                // e.g. freq = 482000000
                .setFrequencyLong(tpInfo.CableTp.getFreq()*1000L)
                // e.g. we use 5200 previously but framework need 5200000
                .setSymbolRate(tpInfo.CableTp.getSymbol()*1000)
                // from our qam define to framework modulation define
                .setModulation(getFrameworkModulation(tpInfo.CableTp.getQam()))
                // Annex change by actual environment
                // our stream player and kbro live are annex A/C
                .setAnnex(SETTING_ANNEX)
                // SpectralInversion change by actual environment
                // our stream player is FRONTEND_SPECTRAL_INVERSION_NORMAL
                // kbro live is FRONTEND_SPECTRAL_INVERSION_INVERTED in A1B
                .setSpectralInversion(SETTING_SPECTRAL_INVERSION)

//                .setInnerFec(DvbcFrontendSettings.FEC_AUTO)
//                .setOuterFec(DvbcFrontendSettings.OUTER_FEC_OUTER_FEC_NONE)
                .setBandwidth(DvbcFrontendSettings.BANDWIDTH_6MHZ)
                .build();
        setCancelTuning(false);
        if (null == getTuner()) {
            result = Tuner.RESULT_UNAVAILABLE;
        }
        else
            result = getTuner().tune(frontendSettings);

        Log.d(TAG, "tuner ["+get_Id()+"] tune: freq = " + frontendSettings.getFrequencyLong());
        Log.d(TAG, "tuner ["+get_Id()+"] tune: sym = " + frontendSettings.getSymbolRate());
        Log.d(TAG, "tuner ["+get_Id()+"] tune: band = " + frontendSettings.getBandwidth());
        Log.d(TAG, "tuner ["+get_Id()+"] tune: qam = " + getModulationName(frontendSettings.getModulation()));
        Log.d(TAG, "tuner ["+get_Id()+"] tune: result error = " + result);
        Log.d(TAG, "tuner ["+get_Id()+"] tune: result = " + (result == Tuner.RESULT_SUCCESS ? "Success" : "Fail"));
        return result == Tuner.RESULT_SUCCESS;
    }

    @Override
    protected boolean tune(TVTunerParams tunerParams) {
        if (tunerParams == null) {
            Log.e(TAG, "tune: null tunerParams");
            return false;
        }

        DvbcFrontendSettings frontendSettings;
        int result;

        frontendSettings = DvbcFrontendSettings.builder()
                // e.g. freq = 482000000
                .setFrequencyLong(tunerParams.getFrequency()*1000L)
                // e.g. we use 5200 previously but framework need 5200000
                .setSymbolRate(tunerParams.getSymbolRate()*1000)
                // from our qam define to framework modulation define
                .setModulation(getFrameworkModulation(tunerParams.getQam()))
                // Annex change by actual environment
                // our stream player and kbro live are annex A/C
                .setAnnex(SETTING_ANNEX)
                // SpectralInversion change by actual environment
                // our stream player is FRONTEND_SPECTRAL_INVERSION_NORMAL
                // kbro live is FRONTEND_SPECTRAL_INVERSION_INVERTED in A1B
                .setSpectralInversion(SETTING_SPECTRAL_INVERSION)

//                .setInnerFec(DvbcFrontendSettings.FEC_AUTO)
//                .setOuterFec(DvbcFrontendSettings.OUTER_FEC_OUTER_FEC_NONE)
                .setBandwidth(DvbcFrontendSettings.BANDWIDTH_6MHZ)
                .build();

        result = getTuner().tune(frontendSettings);

        LogUtils.d( "tune: freq = " + frontendSettings.getFrequencyLong());
        Log.d(TAG, "tune: sym = " + frontendSettings.getSymbolRate());
        Log.d(TAG, "tune: band = " + frontendSettings.getBandwidth());
        Log.d(TAG, "tune: qam = " + getModulationName(frontendSettings.getModulation()));
        Log.d(TAG, "tune: result error = " + result);
        Log.d(TAG, "tune: result = " + (result == Tuner.RESULT_SUCCESS ? "Success" : "Fail"));
        return result == Tuner.RESULT_SUCCESS;
    }

    @Override
    protected boolean tune(TpInfo tpInfo, SatInfo satInfo) {
        if (tpInfo == null) {
            Log.e(TAG, "tune: null tpinfo");
            return false;
        }

        DvbcFrontendSettings frontendSettings;
        int result;
        int cable_bandwidth;
        switch (Pvcfg.getCableBandwidth()) {
            case 8:
                cable_bandwidth = DvbcFrontendSettings.BANDWIDTH_8MHZ;
                break;
            case 7:
                cable_bandwidth = DvbcFrontendSettings.BANDWIDTH_7MHZ;
                break;
            case 6:
                cable_bandwidth = DvbcFrontendSettings.BANDWIDTH_6MHZ;
                break;
            default:
                cable_bandwidth = DvbcFrontendSettings.BANDWIDTH_8MHZ;
                break;
        }
        setTpInfo(tpInfo);
        Log.d(TAG,"222 tuner ["+get_Id()+"] tpInfo.CableTp.getFreq() = "+tpInfo.CableTp.getFreq());
        frontendSettings = DvbcFrontendSettings.builder()
                // e.g. freq = 482000000
                .setFrequencyLong(tpInfo.CableTp.getFreq()*1000L)
                // e.g. we use 5200 previously but framework need 5200000
                .setSymbolRate(tpInfo.CableTp.getSymbol()*1000)
                // from our qam define to framework modulation define
                .setModulation(getFrameworkModulation(tpInfo.CableTp.getQam()))
                // Annex change by actual environment
                // our stream player and kbro live are annex A/C
                .setAnnex(SETTING_ANNEX)
                // SpectralInversion change by actual environment
                // our stream player is FRONTEND_SPECTRAL_INVERSION_NORMAL
                // kbro live is FRONTEND_SPECTRAL_INVERSION_INVERTED in A1B
                .setSpectralInversion(SETTING_SPECTRAL_INVERSION)

//                .setInnerFec(DvbcFrontendSettings.FEC_AUTO)
//                .setOuterFec(DvbcFrontendSettings.OUTER_FEC_OUTER_FEC_NONE)
                .setBandwidth(cable_bandwidth)
                .build();
        setCancelTuning(false);
        result = getTuner().tune(frontendSettings);

        Log.d(TAG, "tune: freq = " + frontendSettings.getFrequencyLong());
        Log.d(TAG, "tune: sym = " + frontendSettings.getSymbolRate());
        Log.d(TAG, "tune: band = " + frontendSettings.getBandwidth());
        Log.d(TAG, "tune: qam = " + getModulationName(frontendSettings.getModulation()));
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
