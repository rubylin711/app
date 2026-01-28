package com.prime.dtv.utils;


/**
 * Created by ethan_lin on 2017/10/30.
 */

public class TVTunerParams /*implements Parcelable */{
    private static final String TAG = "TVTunerParams";

    private int tuner_id;
    private int sat_id;
    private int TpId;
    private int fe_type;
    private int Frequency;
    private int SymbolRate;
    private int Polar;
    private int Bandwith;
    private int Qam;

    public static final int FE_TYPE_DVBT = 1;
    public static final int FE_TYPE_DVBS = 2;
    public static final int FE_TYPE_DVBC = 3;
    public static final int FE_TYPE_ISDBT = 4;

    public TVTunerParams (int tuner_id,int sat_id, int tpId, int fe_type){
        this.tuner_id = tuner_id;
        this.sat_id = sat_id;
        this.TpId = tpId;
        this.fe_type = fe_type;
    }
/*
    protected TVTunerParams(Parcel in) {
        readFromParcel(in);
    }
    public static final Creator<TVTunerParams> CREATOR = new Creator<TVTunerParams>() {
        @Override
        public TVTunerParams createFromParcel(Parcel in) {
            return new TVTunerParams(in);
        }

        @Override
        public TVTunerParams[] newArray(int size) {
            return new TVTunerParams[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(tuner_id);
        dest.writeInt(TpId);
        dest.writeInt(fe_type);
        dest.writeInt(Frequency);
        switch (fe_type) {
            case FE_TYPE_DVBT:
                dest.writeInt(Bandwith);
                break;
            case FE_TYPE_DVBS:
                dest.writeInt(SymbolRate);
                dest.writeInt(Polar);
                break;
            case FE_TYPE_DVBC:
                dest.writeInt(SymbolRate);
                dest.writeInt(Qam);
                break;
        }
    }

    public void readFromParcel(Parcel in){
        tuner_id = in.readInt();
        TpId = in.readInt();
        fe_type = in.readInt();
        Frequency = in.readInt();
        switch (fe_type){
            case FE_TYPE_DVBT:
                Bandwith = in.readInt();
            break;
            case FE_TYPE_DVBS:
                SymbolRate = in.readInt();
                Polar = in.readInt();
                break;
            case FE_TYPE_DVBC:
                SymbolRate = in.readInt();
                Qam = in.readInt();
                break;
        }
    }
*/
    public static TVTunerParams CreateTunerParamDVBT(int tuner_id, int sat_id, int tpId, int frequency, int bandwith){
        TVTunerParams tp = new TVTunerParams(tuner_id,sat_id,tpId, FE_TYPE_DVBT);
        tp.setFrequency(frequency);
        tp.setBandwith(bandwith);
        return tp;
    }
    public static TVTunerParams CreateTunerParamISDBT(int tuner_id, int sat_id, int tpId, int frequency, int bandwith){
        TVTunerParams tp = new TVTunerParams(tuner_id,sat_id,tpId, FE_TYPE_ISDBT);
        tp.setFrequency(frequency);
        tp.setBandwith(bandwith);
        return tp;
    }
    public static TVTunerParams CreateTunerParamDVBS(int tuner_id, int sat_id, int tpId, int frequency, int symbolRate, int polar){
        TVTunerParams tp = new TVTunerParams(tuner_id,sat_id,tpId, FE_TYPE_DVBS);
        tp.setFrequency(frequency);
        tp.setSymbolRate(symbolRate);
        tp.setPolar(polar);
        return tp;
    }
    public static TVTunerParams CreateTunerParamDVBC(int tuner_id, int sat_id, int tpId, int frequency, int symbolRate, int Qam){
        TVTunerParams tp = new TVTunerParams(tuner_id,sat_id,tpId, FE_TYPE_DVBC);
        tp.setFrequency(frequency);
        tp.setSymbolRate(symbolRate);
        tp.setQam(Qam);
        return tp;
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
}
