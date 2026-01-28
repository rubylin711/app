package com.prime.dtvplayer.Sysdata;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by gary_hsu on 2017/11/8.
 */

public class SatInfo {
    private static final String TAG="SatInfo";
    public static final int MAX_NUM_OF_SAT = 128;
    public static final int MAX_TP_NUM_IN_ONE_SAT = 256;
    public static final int TONE_22K_AUTO = 0;
    public static final int TONE_22K_0K = 1;
    public static final int TONE_22K_22K = 2;
    public static final int LNB_TYPE_NORMAL = 0;
    public static final int LNB_TYPE_UNIVERSAL = 1;
    public static final int LNB_TYPE_LNBF = 2;
    public static final int LNB_POWER_OFF = 0;
    public static final int LNB_POWER_ON = 1;
    public static final int DISEQC_TYPE_NONE = 0;
    public static final int DISEQC_TYPE_OFF = 1;
    public static final int DISEQC_TYPE_1_0 = 2;
    public static final int DISEQC_TYPE_1_1 = 3;
    public static final int DISEQC_TYPE_1_2 = 4;
    public static final int DISEQC_PORT_A = 0;
    public static final int DISEQC_PORT_B = 1;
    public static final int DISEQC_PORT_C = 2;
    public static final int DISEQC_PORT_D = 3;
    public static final int ANGLE_E = 0;
    public static final int ANGLE_W = 1;

    public static final int LONGITUDE_VALUE_RATE = 10;
    public static final int LONGITUDE_VALUE_MAX = 3600;

    public static final String SAT_ID = "SatId";
    public static final String SAT_NAME = "SatName";
    public static final String TUNER_TYPE = "TunerType";
    public static final String SAT_TP_NUM = "SatTpNum";
    public static final String ANGLE = "Angle";
    public static final String LOCATION = "Location";
    public static final String POSTION_INDEX = "PostionIndex";
    public static final String ANGLE_EW = "AngleEW";
    public static final String ANTENNA = "Antenna";
    public static final String TPS = "Tps";

    private int SatId;
    private String SatName;
    private int TunerType;
    private int SatTpNum;
    private float Angle;
    private int Location;
    private int PostionIndex;
    private int AngleEW;
    public AntInfo Antenna = new AntInfo();
    private List<Integer> Tps;

    public SatInfo(int SatId,String SatName,int TunerType,int SatTpNum,float Angle,int Location,int PostionIndex,int AngleEW,
                   int Lnb1,int Lnb2,int LnbType,int DiseqcType,int Diseqc,int Tone22kUse,
                   int Tone22k,int V1418Use,int V1418,int Cku) {
        this.SatId = SatId;
        this.SatName = SatName;
        this.TunerType = TunerType;
        this.SatTpNum = SatTpNum;
        this.Angle = Angle;
        this.Location = Location;
        this.PostionIndex = PostionIndex;
        this.AngleEW = AngleEW;;
        AntInfo antInfo = new AntInfo(Lnb1,Lnb2,LnbType,DiseqcType,Diseqc,Tone22kUse,Tone22k,V1418Use,V1418,Cku);
        this.Antenna = antInfo;
    }

    public SatInfo() {

    }


    public class AntInfo {
        private int Lnb1;
        private int Lnb2;
        private int LnbType;
        private int DiseqcType;
//        private int DiseqcUse;
        private int Diseqc;
        private int Tone22kUse;
        private int Tone22k;
        private int V1418Use;
        private int V1418;
        private int Cku;

        public AntInfo(int Lnb1,int Lnb2,int LnbType,int DiseqcType,int Diseqc,int Tone22kUse,
                       int Tone22k,int V1418Use,int V1418,int Cku) {
            this.Lnb1 = Lnb1;
            this.Lnb2 = Lnb2;
            this.LnbType = LnbType;
            this.DiseqcType = DiseqcType;
            this.Diseqc = Diseqc;
            this.Tone22kUse = Tone22kUse;
            this.Tone22k = Tone22k;
            this.V1418Use = V1418Use;
            this.V1418 = V1418;
            this.Cku = Cku;
        }

        public AntInfo() {

        }


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

//        public int getDiseqcUse() {
//            return DiseqcUse;
//        }

//        public void setDiseqcUse(int diseqcUse) {
//            DiseqcUse = diseqcUse;
//        }

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
                + "Lnb2 : "+ Antenna.getLnb2() + "DiseqcType : " + Antenna.getDiseqcType() + /*"DiseqcUse : " + Antenna.getDiseqcUse() +*/ "Diseqc : " + Antenna.getDiseqc()
                + "Tone22kUse : " + Antenna.getTone22kUse() + "Tone22k : " + Antenna.getTone22k() + "V012Use : "
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

    public int getTunerType() {
        return TunerType;
    }

    public void setTunerType(int tunerType) {
        TunerType = tunerType;
    }

    public int getAngleEW()
    {
        return AngleEW;
    }

    public void setAngleEW(int angleEW)
    {
        AngleEW = angleEW;
    }

    public void update(SatInfo satInfo) {
        SatId = satInfo.getSatId();
        SatName = satInfo.getSatName();
        TunerType = satInfo.getTunerType();
        SatTpNum = satInfo.getTpNum();
        Angle = satInfo.getAngle();
        Location = satInfo.getLocation();
        PostionIndex = satInfo.getPostionIndex();
        AngleEW = satInfo.getAngleEW();
        Antenna = satInfo.Antenna;
        Tps = satInfo.Tps;
    }

    public String getJsonStringFromAntenna() {
        Gson gson = new Gson();
        String jsonString = null;
        if(Antenna != null)
            jsonString = gson.toJson(Antenna);
        return jsonString;
    }

    public String getJsonStringFromTps() {
        Gson gson = new Gson();
        String jsonString = null;
        if(Antenna != null)
            jsonString = gson.toJson(Antenna);
        return jsonString;
    }

    public AntInfo getAntennaFromJsonString(String jsonString) {
        Gson gson = new Gson();
        AntInfo antenna = gson.fromJson(jsonString, AntInfo.class);
        return antenna;
    }

    public List<Integer> getTpsFromJsonString(String jsonString) {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<Integer>>() {}.getType();
        List<Integer> tps = gson.fromJson(jsonString, collectionType);
        return tps;
    }

}
