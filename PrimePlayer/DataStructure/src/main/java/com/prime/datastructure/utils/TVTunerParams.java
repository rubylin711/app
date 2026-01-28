package com.prime.datastructure.utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.prime.datastructure.sysdata.TpInfo;
/**
 * Created by ethan_lin on 2017/10/30.
 */
@ParcelablePlease
public class TVTunerParams implements Parcelable /*implements Parcelable */{
    public static final String TAG = "TVTunerParams";

    public static final String IsLock = "isLock";

    protected int tuner_id;
    protected int sat_id;
    protected int TpId;
    protected int fe_type;
    protected int Frequency;
    protected int SymbolRate;
    protected int Polar;
    protected int Bandwith;
    protected int Qam;
    protected int Fec;
    protected int Mod;

    public static final int FE_TYPE_DVBT = 1;
    public static final int FE_TYPE_DVBS = 2;
    public static final int FE_TYPE_DVBC = 3;
    public static final int FE_TYPE_ISDBT = 4;

    public TVTunerParams (int tuner_id,int sat_id, int tpId, int fe_type, int fec, int mod){
        this.tuner_id = tuner_id;
        this.sat_id = sat_id;
        this.TpId = tpId;
        this.fe_type = fe_type;
        this.Fec = fec;
        this.Mod = mod;
    }

    public TVTunerParams (){

    }

    public static TVTunerParams CreateTunerParamDVBT(int tuner_id, int sat_id, int tpId, int frequency, int bandwith){
        TVTunerParams tp = new TVTunerParams(tuner_id,sat_id,tpId, FE_TYPE_DVBT, 0, 0);
        tp.setFrequency(frequency);
        tp.setBandwith(bandwith);
        return tp;
    }
    public static TVTunerParams CreateTunerParamISDBT(int tuner_id, int sat_id, int tpId, int frequency, int bandwith){
        TVTunerParams tp = new TVTunerParams(tuner_id,sat_id,tpId, FE_TYPE_ISDBT, 0, 0);
        tp.setFrequency(frequency);
        tp.setBandwith(bandwith);
        return tp;
    }
    public static TVTunerParams CreateTunerParamDVBS(int tuner_id, int sat_id, int tpId, int frequency, int symbolRate, int polar, int fec, int mod){
        TVTunerParams tp = new TVTunerParams(tuner_id,sat_id,tpId, FE_TYPE_DVBS, fec, mod);
        tp.setFrequency(frequency);
        tp.setSymbolRate(symbolRate);
        tp.setPolar(polar);
        tp.setFec(fec);
        tp.setMod(mod);
        return tp;
    }
    public static TVTunerParams CreateTunerParamDVBC(int tuner_id, int sat_id, int tpId, int frequency, int symbolRate, int Qam){
        TVTunerParams tp = new TVTunerParams(tuner_id,sat_id,tpId, FE_TYPE_DVBC, 0, 0);
        tp.setFrequency(frequency);
        tp.setSymbolRate(symbolRate);
        tp.setQam(Qam);
        return tp;
    }

    public static TVTunerParams CreateTunerParam(int tuner_id, TpInfo tpInfo) {
        TVTunerParams tvTunerParams = null;
        if(tpInfo.getTunerType() == TpInfo.DVBC) {
            tvTunerParams =
                    TVTunerParams.CreateTunerParamDVBC(
                            tuner_id, 0/*sat id not used*/, 0/*tp id not used*/,
                            tpInfo.CableTp.getFreq(), tpInfo.CableTp.getSymbol(), tpInfo.CableTp.getQam());
        }
        else if(tpInfo.getTunerType() == TpInfo.DVBS) {
            tvTunerParams =
                    TVTunerParams.CreateTunerParamDVBS(
                            tuner_id, tpInfo.getSatId(), tpInfo.getTpId(),
                            tpInfo.SatTp.getFreq(), tpInfo.SatTp.getSymbol(), tpInfo.SatTp.getPolar(),
                            tpInfo.SatTp.getFec(),tpInfo.SatTp.getMod());
        }
        else if(tpInfo.getTunerType() == TpInfo.ISDBT) {
            tvTunerParams =
                    TVTunerParams.CreateTunerParamISDBT(
                            tuner_id, tpInfo.getSatId(), tpInfo.getTpId(),
                            tpInfo.TerrTp.getFreq(), tpInfo.TerrTp.getBand());
        }

        return tvTunerParams;
    }

    public int getTunerId(){
        return tuner_id;
    }
    public int getSatId(){
        return sat_id;
    }
    public int getTpId(){
        return TpId;
    }

    public int getFe_type(){
        return fe_type;
    }

    public int getFrequency() {
        return Frequency;
    }

    public int getSymbolRate() {
        return SymbolRate;
    }

    public int getPolar() {
        return Polar;
    }

    public int getBandwith() {
        return Bandwith;
    }

    public int getQam() {
        return Qam;
    }
    public int getFec() {
        return Fec;
    }
    public int getMod() {
        return Mod;
    }

    public void setFrequency(int frequency) {
        Frequency = frequency;
    }

    public void setSymbolRate(int symbolRate) {
        SymbolRate = symbolRate;
    }

    public void setPolar(int polar) {
        Polar = polar;
    }

    public void setBandwith(int bandwith) {
        Bandwith = bandwith;
    }

    public void setQam(int qam) {
        Qam = qam;
    }
    public void setFec(int fec) { Fec = fec; }
    public void setMod(int mod) { Mod = mod; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TVTunerParamsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TVTunerParams> CREATOR = new Creator<TVTunerParams>() {
        public TVTunerParams createFromParcel(Parcel source) {
            TVTunerParams target = new TVTunerParams();
            TVTunerParamsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TVTunerParams[] newArray(int size) {
            return new TVTunerParams[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return "TVTunerParams{" +
                "Bandwith=" + Bandwith +
                ", tuner_id=" + tuner_id +
                ", sat_id=" + sat_id +
                ", TpId=" + TpId +
                ", fe_type=" + fe_type +
                ", Frequency=" + Frequency +
                ", SymbolRate=" + SymbolRate +
                ", Polar=" + Polar +
                ", Qam=" + Qam +
                ", Fec=" + Fec +
                ", Mod=" + Mod +
                '}';
    }
}
