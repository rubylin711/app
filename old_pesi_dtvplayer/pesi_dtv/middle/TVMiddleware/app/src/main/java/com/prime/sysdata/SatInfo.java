package com.prime.sysdata;

import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by gary_hsu on 2017/11/8.
 */

public class SatInfo {
    private static final String TAG="SatInfo";
    public static final int MAX_NUM_OF_SAT = 128;
    public static final int MAX_TP_NUM_IN_ONE_SAT = 128;
    public static final int TONE_22K_AUTO = 0;
    public static final int TONE_22K_0K = 1;
    public static final int TONE_22K_22K = 2;
    public static final int LNB_TYPE_NORMAL = 0;
    public static final int LNB_TYPE_UNIVERSAL = 0;
    public static final int LNB_TYPE_LNBF = 0;
    public static final int LNB_POWER_OFF = 0;
    public static final int LNB_POWER_ON = 1;
    public static final int DISEQC_TYPE_1 = 0;
    public static final int DISEQC_TYPE_1_1 = 1;
    public static final int DISEQC_PORT_A = 0;
    public static final int DISEQC_PORT_B = 1;
    public static final int DISEQC_PORT_C = 2;
    public static final int DISEQC_PORT_D = 3;

    private int SatId;
    private String SatName;
    private int SatTpNum;
    private float Angle;
    private int Location;
    private int PostionIndex;
    public AntInfo Antenna = new AntInfo();
    private List<Integer> Tps;

    public class AntInfo {
        private int Lnb1;
        private int Lnb2;
        private int LnbType;
        private int DiseqcType;
        private int DiseqcUse;
        private int Diseqc;
        private int Tone22kUse;
        private int Tone22k;
        private int V012Use;
        private int V012;
        private int V1418Use;
        private int V1418;
        private int Cku;

        public int getLnb1() {
            return Lnb1;
        }

        public void setLnb1(int lnb1) {
            Lnb1 = lnb1;
        }

        public int getLnb2() {
            return Lnb2;
        }

        public void setLnb2(int lnb2) {
            Lnb2 = lnb2;
        }

        public int getLnbType() {
            return LnbType;
        }

        public void setLnbType(int lnbType) {
            LnbType = lnbType;
        }

        public int getDiseqcType() {
            return DiseqcType;
        }

        public void setDiseqcType(int diseqcType) {
            DiseqcType = diseqcType;
        }

        public int getDiseqcUse() {
            return DiseqcUse;
        }

        public void setDiseqcUse(int diseqcUse) {
            DiseqcUse = diseqcUse;
        }

        public int getDiseqc() {
            return Diseqc;
        }

        public void setDiseqc(int diseqc) {
            Diseqc = diseqc;
        }

        public int getTone22kUse() {
            return Tone22kUse;
        }

        public void setTone22kUse(int tone22kUse) {
            Tone22kUse = tone22kUse;
        }

        public int getTone22k() {
            return Tone22k;
        }

        public void setTone22k(int tone22k) {
            Tone22k = tone22k;
        }

        public int getV012Use() {
            return V012Use;
        }

        public void setV012Use(int v012Use) {
            V012Use = v012Use;
        }

        public int getV012() {
            return V012;
        }

        public void setV012(int v012) {
            V012 = v012;
        }

        public int getV1418Use() {
            return V1418Use;
        }

        public void setV1418Use(int v1418Use) {
            V1418Use = v1418Use;
        }

        public int getV1418() {
            return V1418;
        }

        public void setV1418(int v1418) {
            V1418 = v1418;
        }

        public int getCku() {
            return Cku;
        }

        public void setCku(int cku) {
            Cku = cku;
        }
    }
    public String ToString(){
        return "[SatId : " + SatId + " SatName : " + SatName + " Angle : " + Angle + " location : " + Location
                + " PostionIndex : " + PostionIndex + " SatTpNum : " + SatTpNum + " [Antenna -- " + "LnbType : " + Antenna.getLnbType() + "Lnb1 : " + Antenna.getLnb1()
                + "Lnb2 : "+ Antenna.getLnb2() + "DiseqcType : " + Antenna.getDiseqcType() + "DiseqcUse : " + Antenna.getDiseqcUse() + "Diseqc : " + Antenna.getDiseqc()
                + "Tone22kUse : " + Antenna.getTone22kUse() + "Tone22k : " + Antenna.getTone22k() + "V012Use : " + Antenna.getV012Use() + "V012 : " + Antenna.getV012()
                + "V1418Use : " + Antenna.getV1418Use() + "V1418 : " + Antenna.getV1418() + "Cku : " + Antenna.getCku() + "] " + "]";
    }

    public int getSatId() {
        Log.i(TAG, "GetSatId: SatId="+SatId);
        return SatId;
    }

    public int getTpNum() {
        Log.i(TAG, "GetTpNum: SatTpNum="+SatTpNum);
        return SatTpNum;
    }


    public float getAngle() {
        Log.i(TAG, "GetAngle: Angle="+Angle);
        return Angle;
    }



    public void setSatId(int value) {
        Log.i(TAG, "SetSatId: value="+value);
        SatId = value;
    }

    public void setTpNum(int value) {
        Log.i(TAG, "SetTpNum: value="+value);
        SatTpNum = value;
    }

    public void setAngle(float value) {
        Log.i(TAG, "SetAngle: value="+value);
        Angle = value;
    }


    public String getSatName() {
        return SatName;
    }

    public void setSatName(String satName) {
        SatName = satName;
    }

    public int getLocation() {
        return Location;
    }

    public void setLocation(int location) {
        this.Location = location;
    }

    public int getPostionIndex() {
        return PostionIndex;
    }

    public void setPostionIndex(int postionIndex) {
        PostionIndex = postionIndex;
    }

    public List<Integer> getTps() {
        return Tps;
    }

    public void setTps(List<Integer> tps) {
        Tps = tps;
    }
}
