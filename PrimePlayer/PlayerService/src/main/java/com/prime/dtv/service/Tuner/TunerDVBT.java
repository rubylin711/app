package com.prime.dtv.service.Tuner;

import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.frontend.DvbcFrontendSettings;
import android.media.tv.tuner.frontend.DvbtFrontendSettings;
import android.media.tv.tuner.frontend.FrontendSettings;
import android.util.Log;

import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.TVTunerParams;

public class TunerDVBT extends TunerBase {
    private static final String TAG = "TunerDVBT";

    // change by actual environment
    // TODO: try not to use fixed values
    private static final int SETTING_ANNEX = DvbcFrontendSettings.ANNEX_A;
    private static final int SETTING_SPECTRAL_INVERSION = FrontendSettings.FRONTEND_SPECTRAL_INVERSION_NORMAL;

    public TunerDVBT(Tuner tuner) {
        super(tuner);
    }

    public TunerDVBT() {
        super();
    }

    @Override
    protected boolean tune(TpInfo tpInfo) {

        if (tpInfo == null) {
            Log.e(TAG, "tune: null tpinfo");
            return false;
        }

        boolean result = false;
        result = tune_DVBT(tpInfo);
        return result;
    }

    @Override
    protected boolean tune(TVTunerParams tunerParams) {
        if (tunerParams == null) {
            Log.e(TAG, "tune: null tunerParams");
            return false;
        }
        Log.e(TAG, "tune(TVTunerParams tunerParams) : not support");
        boolean result = false;
//        result = tune_DVBT(tunerParams.getFrequency(), tunerParams.getBandwith());
        return result;
    }

    @Override
    protected boolean tune(TpInfo tpInfo, SatInfo satInfo) {
        if (tpInfo == null) {
            Log.e(TAG, "tune: null tpinfo");
            return false;
        }

        boolean result = false;
        result = tune_DVBT(tpInfo);
        return result;
    }

    protected boolean tune_DVBT(/*int freq, int band*/TpInfo tpInfo) { // Only dvb-t2 work in rtk's module
        DvbtFrontendSettings frontendSettings;
        int result=0;
        int freq = tpInfo.TerrTp.getFreq();
        int band = tpInfo.TerrTp.getBand();
        int dvbt_type = tpInfo.TerrTp.getDvbtType();
        int bandwidth = DvbtFrontendSettings.BANDWIDTH_8MHZ;
        int standard_type = DvbtFrontendSettings.STANDARD_AUTO;
        int plpId = 0;
        switch(band) {
            case TpInfo.Terr.BAND_6MHZ:
                bandwidth = DvbtFrontendSettings.BANDWIDTH_6MHZ;
                break;
            case TpInfo.Terr.BAND_7MHZ:
                bandwidth = DvbtFrontendSettings.BANDWIDTH_7MHZ;
                break;
            case TpInfo.Terr.BAND_8MHZ:
                bandwidth = DvbtFrontendSettings.BANDWIDTH_8MHZ;
                break;
            default:
                bandwidth = DvbtFrontendSettings.BANDWIDTH_AUTO;
                break;
        }

        switch(dvbt_type) {
            case TpInfo.Terr.DVBT_TYPE_T: {
                standard_type = DvbtFrontendSettings.STANDARD_T;
                plpId = 0xff;
            }break;
            case TpInfo.Terr.DVBT_TYPE_T2: {
                standard_type = DvbtFrontendSettings.STANDARD_T2;
                plpId = 0x0;
            }break;
            default:
                standard_type = DvbtFrontendSettings.STANDARD_AUTO;
                break;
        }
        setTpInfo(tpInfo);
        String[] type_str = {"DVBT_AUTO","DVBT_T","DVBT_T2"};
        Log.d(TAG,"freq = "+freq+" band = "+band+ " type = "+type_str[dvbt_type] + " plpId = "+plpId);

        frontendSettings = DvbtFrontendSettings.builder()
                .setFrequencyLong(freq*1000L)
                .setStandard(DvbtFrontendSettings.STANDARD_T2)
                .setBandwidth(bandwidth)
                .setPlpId(plpId) // use plpid to switch t/t2 in tuner driver
                .build();
        setCancelTuning(false);
        result = getTuner().tune(frontendSettings);

        Log.d(TAG, "tune: freq = " + frontendSettings.getFrequencyLong());
        //Log.d(TAG, "tune: sym = " + frontendSettings.getSymbolRate());
        Log.d(TAG, "tune: band = " + frontendSettings.getBandwidth());
        //Log.d(TAG, "tune: qam = " + getModulationName(frontendSettings.getModulation()));
        Log.d(TAG, "tune: Standard = " + frontendSettings.getStandard());
        Log.d(TAG, "tune: result error = " + result);
        Log.d(TAG, "tune: result = " + (result == Tuner.RESULT_SUCCESS ? "Success" : "Fail"));

        return result == Tuner.RESULT_SUCCESS;
    }
}
