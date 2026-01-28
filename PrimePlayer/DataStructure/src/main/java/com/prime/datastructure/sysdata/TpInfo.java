package com.prime.datastructure.sysdata;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.prime.datastructure.config.Pvcfg;

/**
 * Created by gary_hsu on 2017/11/8.
 */

@ParcelablePlease
public class TpInfo implements Parcelable {
    public static final String TAG = "TpInfo";
    public static final int DVBT = EnNetworkType.TERRESTRIAL.getValue();
    public static final int DVBS = EnNetworkType.SATELLITE.getValue();
    public static final int DVBC = EnNetworkType.CABLE.getValue();
    public static final int ISDBT = EnNetworkType.ISDB_TER.getValue(); // Johnny add for ISDBT channel search 20180103
    public static final int NONE = EnNetworkType.NONE.getValue();

    public static final int MAX_NUM_OF_TP = 1024;

    public static final String TP_ID = "TpId";
    public static final String SAT_ID = "SatId";
    public static final String TUNER_TYPE = "TunerType";
    public static final String NETWORK_ID = "network_id";
    public static final String TRANSPORT_ID = "transport_id";
    public static final String ORIGNAL_ID = "orignal_network_id";
    public static final String TUNER_ID = "tuner_id";
    public static final String TERR_TP_JSON = "TerrTp";
    public static final String SAT_TP_JSON = "SatTp";
    public static final String CABLE_TP_JSON = "CableTp";
    public static final String TP_SDT_VERSION = "SdtTableVersionNumber";


    protected int TpId;
    protected int SatId;
    protected int TunerType;
    protected int network_id;
    protected int transport_id;
    protected int orignal_network_id;
    protected int tuner_id;
    protected int sdt_version=0xff;
    public Terr TerrTp = null;
    public Sat SatTp = null;
    public Cable CableTp = null;
    protected int Status;

    public String ToString(){
        String info = "TpId = "+TpId+" SatId = "+SatId+" TunerType = "+TunerType+" network_id = "+network_id+" transport_id = "+transport_id
                +" orignal_network_id = "+orignal_network_id+" tuner_id = "+tuner_id + " sdt_version = "+sdt_version;
        if(TerrTp != null)
            info += " Channel = "+TerrTp.Channel+" Freq = "+TerrTp.Freq+" Band = "+TerrTp.Band;
        if(SatTp != null)
            info += " Freq = "+SatTp.Freq+" Symbol = "+SatTp.Symbol+" Polar = "+SatTp.Polar;
        if(CableTp != null)
            info += " Channel = "+CableTp.Channel+" Freq ="+CableTp.Freq+" Symbol = "+CableTp.Symbol+" Qam = "+CableTp.Qam;
        return info;
    }

    public TpInfo(int tunerType,int TpId,int SatId,int Channel,int Freq,int Symbol,int Qam,int Band){
        if(tunerType == DVBC) {
            CableTp = new Cable();
            this.TunerType = tunerType;
            this.TpId = TpId;
            this.SatId = SatId;
            CableTp.setChannel(Channel);
            CableTp.setFreq(Freq);
            CableTp.setSymbol(Symbol);
            CableTp.setQam(Qam);
            CableTp.setBand(Band);
        }
        else if(tunerType == ISDBT || tunerType == DVBT){
            TerrTp = new Terr();
            this.TunerType = tunerType;
            this.TpId = TpId;
            this.SatId = SatId;
            TerrTp.setChannel(Channel);
            TerrTp.setFreq(Freq);
            TerrTp.setBand(Band);
            TerrTp.setDvbtType(Terr.DVBT_TYPE_AUTO);
        }
        else {
            Log.d(TAG,"TpInfo init fail !!");
        }
    }

    public TpInfo(int tunerType){
        if(tunerType == DVBT || tunerType == ISDBT) {
            TerrTp = new Terr();
        }
        else if(tunerType == DVBS) {
            SatTp = new Sat();
        }
        else if(tunerType == DVBC) {
            CableTp = new Cable();
        }
        this.TunerType = tunerType;
    }

    public TpInfo(){
        TerrTp = new Terr();
        SatTp = new Sat();
        CableTp = new Cable();
    }

    public TpInfo(TpInfo tpInfo) {
        this.TunerType = tpInfo.getTunerType();
        this.CableTp = tpInfo.CableTp;
        this.SatTp = new Sat(tpInfo.SatTp);
        this.TerrTp = tpInfo.TerrTp;
        this.TpId = tpInfo.getTpId();
        this.SatId = tpInfo.getSatId();
        this.network_id = tpInfo.getNetwork_id();
        this.transport_id = tpInfo.getTransport_id();
        this.orignal_network_id = tpInfo.getOrignal_network_id();
        this.tuner_id = tpInfo.getTuner_id();
        this.sdt_version = tpInfo.getSdt_version();
    }

    public int getTpId() {
        return TpId;
    }

    public void setTpId(int tpId) {
        TpId = tpId;
    }

    public int getSatId() {
        return SatId;
    }

    public void setSatId(int satId) {
        SatId = satId;
    }

    public int getTunerType() {
        return TunerType;
    }

    public void setTunerType(int tunerType) {
        TunerType = tunerType;
    }

    public int getTransport_id() {
        return transport_id;
    }

    public void setTransport_id(int transport_id) {
        this.transport_id = transport_id;
    }

    public int getSdt_version() {
        return sdt_version;
    }

    public void setSdt_version(int sdtVersion) {
        this.sdt_version = sdtVersion;
    }

    public int getOrignal_network_id() {
        return orignal_network_id;
    }

    public void setOrignal_network_id(int orignal_network_id) {
        this.orignal_network_id = orignal_network_id;
    }

    public int getNetwork_id() {
        return network_id;
    }

    public void setNetwork_id(int network_id) {
        this.network_id = network_id;
    }

    public int getTuner_id() {
        return tuner_id;
    }

    public void setTuner_id(int tuner_id) {
        this.tuner_id = tuner_id;
    }

    public void update(TpInfo tpInfo) {
        TpId = tpInfo.getTpId();
        SatId = tpInfo.getSatId();
        TunerType = tpInfo.getTunerType();
        network_id = tpInfo.getNetwork_id();
        transport_id = tpInfo.getTransport_id();
        orignal_network_id = tpInfo.getOrignal_network_id();
        tuner_id = tpInfo.getTuner_id();
        if(tpInfo.TerrTp != null)
            TerrTp = tpInfo.TerrTp;
        if(tpInfo.SatTp != null)
            SatTp = tpInfo.SatTp;
        if(tpInfo.CableTp != null)
            CableTp = tpInfo.CableTp;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    @ParcelablePlease
    public static class Terr implements Parcelable {
        protected static final String TAG = "TpInfo.Terr";

        public static final int BAND_6MHZ = 0;
        public static final int BAND_7MHZ = 1;
        public static final int BAND_8MHZ = 2;

        public static final int DVBT_TYPE_AUTO = 0;
        public static final int DVBT_TYPE_T = 1;
        public static final int DVBT_TYPE_T2 = 2;
        protected static final int NUM_OF_OTHER_DATA = 4;

        protected int Channel;
        protected int Freq;
        protected int Band;
        protected int Fft;
        protected int Guard;
        protected int Const;
        protected int Hierarchy;
        protected int NetWork;
        protected int NitSearchIndex;
        protected int CodeRate;
        protected int SearchOrNot;
        protected int DvbtType;

        public Terr()
        {

        }
        public int getCodeRate() {
            return CodeRate;
        }

        public void setCodeRate(int codeRate) {
            CodeRate = codeRate;
        }

        public int getNetWork() {
            return NetWork;
        }

        public void setNetWork(int netWork) {
            NetWork = netWork;
        }

        public int getNitSearchIndex() {
            return NitSearchIndex;
        }

        public void setNitSearchIndex(int nitSearchIndex) {
            NitSearchIndex = nitSearchIndex;
        }

        public int getChannel() {
            return Channel;
        }

        public void setChannel(int channel) {
            Channel = channel;
        }

        public int getFreq() {
            return Freq;
        }

        public void setFreq(int freq) {
            Freq = freq;
        }

        public int getBand() {
            return Band;
        }

        public void setBand(int band) {
            Band = band;
        }

        public int getFft() {
            return Fft;
        }

        public void setFft(int fft) {
            Fft = fft;
        }

        public int getGuard() {
            return Guard;
        }

        public void setGuard(int guard) {
            Guard = guard;
        }

        public int getConst() {
            return Const;
        }

        public void setConst(int aConst) {
            Const = aConst;
        }

        public int getHierarchy() {
            return Hierarchy;
        }

        public void setHierarchy(int hierarchy) {
            Hierarchy = hierarchy;
        }

        public String getOtherData(){
            String otherData = Fft+","+Guard+","+Const+","+Hierarchy;
            Log.d(TAG, "getOtherData: "+otherData);
            return otherData;
        }

        public void setOtherData(String data){
            if ( data == null ) // johnny 20171207 add
            {
                return;
            }

            String otherData[] = null;
            if(data.length() != 0)
                otherData = data.split(",");
            if(otherData.length <= NUM_OF_OTHER_DATA){
                Fft = Integer.parseInt(otherData[0]);
                Guard = Integer.parseInt(otherData[1]);
                Const = Integer.parseInt(otherData[2]);
                Hierarchy = Integer.parseInt(otherData[3]);
            }
        }

        public int getSearchOrNot() {
            return SearchOrNot;
        }

        public void setSearchOrNot(int searchOrNot) {
            SearchOrNot = searchOrNot;
        }

        public int getDvbtType() {
            return DvbtType;
        }

        public void setDvbtType(int dvbtType) {
            DvbtType = dvbtType;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            TpInfo$TerrParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Terr> CREATOR = new Creator<Terr>() {
            public Terr createFromParcel(Parcel source) {
                Terr target = new Terr();
                TpInfo$TerrParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public Terr[] newArray(int size) {
                return new Terr[size];
            }
        };
    }

    @ParcelablePlease
    public static class Sat implements Parcelable {
        protected static final String TAG = "TpInfo.Sat";

        public static final int DVBS = 0;
        public static final int DVBS2 = 1;
        public static final int POLAR_H = 0;
        public static final int POLAR_V = 1;
        private static final int NUM_OF_OTHER_DATA = 3;

        public static final int FEC_1_2 = 1;
        public static final int FEC_2_3 = 2;
        public static final int FEC_3_4 = 3;
        public static final int FEC_5_6 = 4;
        public static final int FEC_7_8 = 5;
        public static final int FEC_8_9 = 6;
        public static final int FEC_3_5 = 7;
        public static final int FEC_4_5 = 8;
        public static final int FEC_9_10 = 9;

        public static final int MOD_UNDEFINED = 0;
        public static final int MOD_AUTO = 1;
        public static final int MOD_QPSK = 2;
        public static final int MOD_8PSK = 4;
        public static final int MOD_16QAM = 8;
        public static final int MOD_16PSK = 16;
        public static final int MOD_32PSK = 32;
        public static final int MOD_ACM = 64;
        public static final int MOD_8APSK = 128;
        public static final int MOD_16APSK = 256;
        public static final int MOD_32APSK = 512;
        public static final int MOD_64APSK = 1024;
        public static final int MOD_128APSK = 2048;
        public static final int MOD_256APSK = 4096;
        public static final int MOD_RESERVED = 8192;

        protected int Freq;
        protected int Symbol;
        protected int Fec;

        protected int ModSys;
        protected int Mod;
        protected int Polar;
        protected int Drot;
        protected int Spect;
        protected int NetWork;
        protected int NitSearchIndex;
        protected int SearchOrNot;
        protected int NotDelete;

        public Sat() {

        }

        public Sat(Sat sat) {
            if(sat == null) {
                Log.w(TAG, "[Sat] sat is null! Freq, Symbol, etc. not be set !!!");
            }
            else {
                Freq = sat.getFreq();
                Symbol = sat.getSymbol();
                Fec = sat.getFec();
                ModSys = sat.getModSys();
                Mod = sat.getMod();
                Polar = sat.getPolar();
                Drot = sat.getDrot();
                Spect = sat.getSpect();
                NetWork = sat.getNetWork();
                NitSearchIndex = sat.getNitSearchIndex();
                SearchOrNot = sat.getSearchOrNot();
                NotDelete = sat.getNotDelete();
            }
        }

        public int getNetWork() {
            return NetWork;
        }

        public void setNetWork(int netWork) {
            NetWork = netWork;
        }

        public int getModSys() {
            return ModSys;
        }

        public void setModSys(int modSys) {
            ModSys = modSys;
        }

        public int getNitSearchIndex() {
            return NitSearchIndex;
        }

        public void setNitSearchIndex(int nitSearchIndex) {
            NitSearchIndex = nitSearchIndex;
        }

        public int getFreq() {
            return Freq;
        }

        public void setFreq(int freq) {
            Freq = freq;
        }

        public int getSymbol() {
            return Symbol;
        }

        public void setSymbol(int symbol) {
            Symbol = symbol;
        }

        public int getFec() {
            return Fec;
        }

        public void setFec(int fec) {
            Fec = fec;
        }
        public int getMod() {
            return Mod;
        }

        public void setMod(int mod) { Mod = mod; }

        public int getPolar() {
            return Polar;
        }

        public void setPolar(int polar) {
            this.Polar = polar;
        }

        public int getDrot() {
            return Drot;
        }

        public void setDrot(int drot) {
            Drot = drot;
        }

        public int getSpect() {
            return Spect;
        }

        public void setSpect(int spect) {
            Spect = spect;
        }

        public String getOtherData(){
            String otherData = Fec+","+Drot+","+Spect;
            Log.d(TAG, "getOtherData: "+otherData);
            return otherData;
        }
        public void setOtherData(String data){
            if ( data == null ) // johnny 20171207 add
            {
                return;
            }

            String otherData[] = null;
            if(data.length() != 0)
                otherData = data.split(",");
            if(otherData.length <= NUM_OF_OTHER_DATA){
                Fec = Integer.parseInt(otherData[0]);
                Drot = Integer.parseInt(otherData[1]);
                Spect = Integer.parseInt(otherData[2]);
            }
        }

        public int getSearchOrNot() {
            return SearchOrNot;
        }

        public void setSearchOrNot(int searchOrNot) {
            SearchOrNot = searchOrNot;
        }

        public int getNotDelete() {
            return NotDelete;
        }

        public void setNotDelete(int notDelete) {
            NotDelete = notDelete;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            TpInfo$SatParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Sat> CREATOR = new Creator<Sat>() {
            public Sat createFromParcel(Parcel source) {
                Sat target = new Sat();
                TpInfo$SatParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public Sat[] newArray(int size) {
                return new Sat[size];
            }
        };
    }

    @ParcelablePlease
    public static class Cable implements Parcelable {
        protected static final String TAG = "TpInfo.Cable";

        public static final int QAM_16 = 0;
        public static final int QAM_32 = 1;
        public static final int QAM_64 = 2;
        public static final int QAM_128 = 3;
        public static final int QAM_256 = 4;
        public static final int QAM_AUTO = 5; // broadcom no AUTO : mantis 0004981

        public static final int BAND_6MHZ = 0;
        public static final int BAND_7MHZ = 1;
        public static final int BAND_8MHZ = 2;

        protected int Channel;
        protected int Freq;
        protected int Symbol;
        protected int Qam;
        protected int NetWork;
        protected int NitSearchIndex;
        protected int SearchOrNot;
        protected int Band = BAND_8MHZ;

        public int getNetWork() {
            return NetWork;
        }

        public void setNetWork(int netWork) {
            NetWork = netWork;
        }

        public int getNitSearchIndex() {
            return NitSearchIndex;
        }

        public void setNitSearchIndex(int nitSearchIndex) {
            NitSearchIndex = nitSearchIndex;
        }

        public int getChannel() {
            return Channel;
        }

        public void setChannel(int channel) {
            Channel = channel;
        }

        public int getFreq() {
            return Freq;
        }

        public void setFreq(int freq) {
            Freq = freq;
        }

        public int getSymbol() {
            return Symbol;
        }

        public void setSymbol(int symbol) {
            Symbol = symbol;
        }

        public int getQam() {
            return Qam;
        }

        public void setQam(int qam) {
            Qam = qam;
        }

        public int getBand() {
            switch (Pvcfg.getCableBandwidth()) {
                case 8:
                    Band = BAND_8MHZ;
                    break;
                case 7:
                    Band = BAND_7MHZ;
                    break;
                case 6:
                    Band = BAND_6MHZ;
                    break;
                default:
                    Band = BAND_8MHZ;
                    break;
            }
            return Band;
        }
        public void setBand(int band) {
            Band = band;
        }

        public Cable()
        {

        }
        public String getOtherData(){
            String otherData = " ";
            Log.d(TAG, "getOtherData: "+otherData);
            return otherData;
        }
        public void setOtherData(String data){

        }

        public int getSearchOrNot() {
            return SearchOrNot;
        }

        public void setSearchOrNot(int searchOrNot) {
            SearchOrNot = searchOrNot;
        }

        public int getQamRealValue() {
            int value = 0;
            switch(Qam) {
                case QAM_16: {
                    value = 16;
                }break;
                case QAM_32:{
                    value = 32;
                }break;
                case QAM_64:{
                    value = 64;
                }break;
                case QAM_128:{
                    value = 128;
                }break;
                case QAM_256:
                ddefault:
                {
                    value = 256;
                }break;
            }
            return value;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            TpInfo$CableParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Cable> CREATOR = new Creator<Cable>() {
            public Cable createFromParcel(Parcel source) {
                Cable target = new Cable();
                TpInfo$CableParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public Cable[] newArray(int size) {
                return new Cable[size];
            }
        };
    }

    public String getJsonStringFromTerrTP() {
//        Gson gson = new Gson();
        String jsonString = null;
        if(TerrTp != null)
            jsonString = serializeTerr(TerrTp);//gson.toJson(TerrTp);
        return jsonString;
    }

    public String getJsonStringFromSatTP() {
//        Gson gson = new Gson();
        String jsonString = null;
        if(SatTp != null)
            jsonString = serializeSat(SatTp);//gson.toJson(SatTp);
        return jsonString;
    }

    public String getJsonStringFromCableTP() {
//        Gson gson = new Gson();
        String jsonString = null;
        if(CableTp != null)
            jsonString = serializeCable(CableTp);//gson.toJson(CableTp);
        return jsonString;
    }

    public Terr getTerrTPFromJsonString(String jsonString) {
//        Gson gson = new Gson();
        Terr terrtp = deserializeTerr(jsonString);//gson.fromJson(jsonString, Terr.class);
        return terrtp;
    }

    public Sat getSatTPFromJsonString(String jsonString) {
//        Gson gson = new Gson();
        Sat sattp = deserializeSat(jsonString);//gson.fromJson(jsonString, Sat.class);
        return sattp;
    }

    public Cable getCableTPFromJsonString(String jsonString) {
//        Gson gson = new Gson();
        Cable cabletp = deserializeCable(jsonString);//gson.fromJson(jsonString, Cable.class);
        return cabletp;
    }

    public String serializeTerr(Terr t) {
        return t.getChannel() + "," +
                t.getFreq() + "," +
                t.getBand() + "," +
                t.getFft() + "," +
                t.getGuard() + "," +
                t.getConst() + "," +
                t.getHierarchy() + "," +
                t.getNetWork() + "," +
                t.getNitSearchIndex() + "," +
                t.getCodeRate() + "," +
                t.getSearchOrNot() + "," +
                t.getDvbtType();
    }

    public Terr deserializeTerr(String s) {
        if (s == null || s.isEmpty()) return new Terr();
        String[] parts = s.split(",", -1);
        if (parts.length < 11) return new Terr();
        Terr t = new Terr();
        t.setChannel(Integer.parseInt(parts[0]));
        t.setFreq(Integer.parseInt(parts[1]));
        t.setBand(Integer.parseInt(parts[2]));
        t.setFft(Integer.parseInt(parts[3]));
        t.setGuard(Integer.parseInt(parts[4]));
        t.setConst(Integer.parseInt(parts[5]));
        t.setHierarchy(Integer.parseInt(parts[6]));
        t.setNetWork(Integer.parseInt(parts[7]));
        t.setNitSearchIndex(Integer.parseInt(parts[8]));
        t.setCodeRate(Integer.parseInt(parts[9]));
        t.setSearchOrNot(Integer.parseInt(parts[10]));
        t.setDvbtType(Integer.parseInt(parts[11]));
        return t;
    }

    public String serializeSat(Sat s) {
        return s.getFreq() + "," +
                s.getSymbol() + "," +
                s.getFec() + "," +
                s.getPolar() + "," +
                s.getDrot() + "," +
                s.getSpect() + "," +
                s.getNetWork() + "," +
                s.getNitSearchIndex() + "," +
                s.getSearchOrNot();
    }

    public Sat deserializeSat(String s) {
        if (s == null || s.isEmpty()) return new Sat();
        String[] parts = s.split(",", -1);
        if (parts.length < 9) return new Sat();
        Sat sat = new Sat();
        sat.setFreq(Integer.parseInt(parts[0]));
        sat.setSymbol(Integer.parseInt(parts[1]));
        sat.setFec(Integer.parseInt(parts[2]));
        sat.setPolar(Integer.parseInt(parts[3]));
        sat.setDrot(Integer.parseInt(parts[4]));
        sat.setSpect(Integer.parseInt(parts[5]));
        sat.setNetWork(Integer.parseInt(parts[6]));
        sat.setNitSearchIndex(Integer.parseInt(parts[7]));
        sat.setSearchOrNot(Integer.parseInt(parts[8]));
        return sat;
    }

    public String serializeCable(Cable c) {
        return c.getChannel() + "," +
                c.getFreq() + "," +
                c.getSymbol() + "," +
                c.getQam() + "," +
                c.getSearchOrNot();
    }

    public Cable deserializeCable(String s) {
        if (s == null || s.isEmpty()) return new Cable();
        String[] parts = s.split(",", -1);
        if (parts.length < 5) return new Cable();
        Cable c = new Cable();
        c.setChannel(Integer.parseInt(parts[0]));
        c.setFreq(Integer.parseInt(parts[1]));
        c.setSymbol(Integer.parseInt(parts[2]));
        c.setQam(Integer.parseInt(parts[3]));
        c.setSearchOrNot(Integer.parseInt(parts[4]));
        return c;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TpInfoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TpInfo> CREATOR = new Creator<TpInfo>() {
        public TpInfo createFromParcel(Parcel source) {
            TpInfo target = new TpInfo();
            TpInfoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TpInfo[] newArray(int size) {
            return new TpInfo[size];
        }
    };
}
