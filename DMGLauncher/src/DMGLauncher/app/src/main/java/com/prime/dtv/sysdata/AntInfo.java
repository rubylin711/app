package com.prime.dtv.sysdata;

import android.util.Log;

/**
 * Created by gary_hsu on 2017/11/8.
 */

public class AntInfo {
    private static final String TAG="AntInfo";
    public static final int MAX_NUM_OF_ANT = 64;

    private int AntId;
    private int SatId;
    private int Lnb1;
    private int Lnb2;
    private int LnbType;
    private int DiseqcType;
//    private int DiseqcUse;
    private int Diseqc;
    private int Tone22kUse;
    private int Tone22k;
    private int V012Use;
    private int V012;
    private int V1418Use;
    private int V1418;
    private int Cku;

    public String ToString(){
        return "[Ant id  " + AntId + "SatId : " + SatId + "LnbType : " + LnbType + "Lnb1 : " + Lnb1
                + "Lnb2 : "+ Lnb2 + "DiseqcType : " + DiseqcType /*+ "DiseqcUse : " + DiseqcUse*/ + "Diseqc : " + Diseqc
                + "Tone22kUse : " + Tone22kUse + "Tone22k : " + Tone22k + "V012Use : " + V012Use + "V012 : " + V012
                + "V1418Use : " + V1418Use + "V1418 : " + V1418 + "Cku : " + Cku + "]";
    }


    public int getAntId() {
        Log.i(TAG, "GetAntId: AntId=" + AntId);
        return AntId;
    }

    public int getSatId() {
        Log.i(TAG, "GetSatId: SatId=" + SatId);
        return SatId;
    }

    public int getLnb1() {
        Log.i(TAG, "GetLnb1: Lnb1=" + Lnb1);
        return Lnb1;
    }

    public int getLnb2() {
        Log.i(TAG, "GetLnb2: Lnb2=" + Lnb2);
        return Lnb2;
    }

    public int getLnbType() {
        Log.i(TAG, "GetLnbType: LnbType=" + LnbType);
        return LnbType;
    }

//    public int getDiseqcUse() {
//        Log.i(TAG, "GetDiseqcUse: DiseqcUse=" + DiseqcUse);
//        return DiseqcUse;
//    }

    public int getDiseqc() {
        Log.i(TAG, "GetDiseqc: Diseqc=" + Diseqc);
        return Diseqc;
    }


    public int getTone22kUse() {
        Log.i(TAG, "GetTone22kUse: Tone22kUse=" + Tone22kUse);
        return Tone22kUse;
    }

    public int getTone22k() {
        Log.i(TAG, "GetTone22k: Tone22k=" + Tone22k);
        return Tone22k;
    }

    public int getV012Use() {
        Log.i(TAG, "GetV012Use: V012Use=" + V012Use);
        return V012Use;
    }

    public int getV012() {
        Log.i(TAG, "GetV012: V012=" + V012);
        return V012;
    }

    public int getV1418Use() {
        Log.i(TAG, "GetV1418Use: V1418Use=" + V1418Use);
        return V1418Use;
    }

    public int getV1418() {
        Log.i(TAG, "GetV1418: V1418=" + V1418);
        return V1418;
    }

    public int getCku() {
        Log.i(TAG, "GetCku: Cku=" + Cku);
        return Cku;
    }

    public void setAntId(int value) {
        Log.i(TAG, "SetAntId: value=" + value);
        AntId = value;
    }

    public void setSatId(int value) {
        Log.i(TAG, "SetSatId: value=" + value);
        SatId = value;
    }

    public void setLnb1(int value) {
        Log.i(TAG, "SetLnb1: value=" + value);
        Lnb1 = value;
    }

    public void setLnb2(int value) {
        Log.i(TAG, "SetLnb2: value=" + value);
        Lnb2 = value;
    }

    public void setLnbType(int value) {
        Log.i(TAG, "SetLnbType: value=" + value);
        LnbType = value;
    }

//    public void setDiseqcUse(int value) {
//        Log.i(TAG, "SetDiseqcUse: value=" + value);
//        DiseqcUse = value;
//    }

    public void setDiseqc(int value) {
        Log.i(TAG, "SetDiseqc: value=" + value);
        Diseqc = value;
    }


    public void setTone22kUse(int value) {
        Log.i(TAG, "SetTone22kUse: value=" + value);
        Tone22kUse = value;
    }

    public void setTone22k(int value) {
        Log.i(TAG, "SetTone22k: value=" + value);
        Tone22k = value;
    }

    public void setV012Use(int value) {
        Log.i(TAG, "SetV012Use: value=" + value);
        V012Use = value;
    }

    public void setV012(int value) {
        Log.i(TAG, "SetV012: value=" + value);
        V012 = value;
    }

    public void setV1418Use(int value) {
        Log.i(TAG, "SetV1418Use: value=" + value);
        V1418Use = value;
    }

    public void setV1418(int value) {
        Log.i(TAG, "SetV1418: value=" + value);
        V1418 = value;
    }


    public void setCku(int value) {
        Log.i(TAG, "SetCku: value=" + value);
        Cku = value;
    }

    public int getDiseqcType() {
        return DiseqcType;
    }

    public void setDiseqcType(int diseqcType) {
        DiseqcType = diseqcType;
    }
}
