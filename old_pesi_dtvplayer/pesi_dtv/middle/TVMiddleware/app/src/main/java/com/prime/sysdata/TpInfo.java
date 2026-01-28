package com.prime.sysdata;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * Created by gary_hsu on 2017/11/8.
 */

public class TpInfo {
    private static final String TAG = "TpInfo";
    public static final int DVBT = 1;
    public static final int DVBS = 2;
    public static final int DVBC = 3;
    public static final int ISDBT = 4; // Johnny add for ISDBT channel search 20180103

    private int TpId;
    private int SatId;
    private int TunerType;
    private int network_id;
    private int transport_id;
    private int orignal_network_id;
    private int tuner_id;
    public Terr TerrTp = null;
    public Sat SatTp = null;
    public Cable CableTp = null;

    public String ToString(){
        String info = "TpId = "+TpId+" SatId = "+SatId+" TunerType = "+TunerType+" network_id = "+network_id+" transport_id = "+transport_id
                +" orignal_network_id = "+orignal_network_id+" tuner_id = "+tuner_id;
        if(TerrTp != null)
            info += " Channel = "+TerrTp.Channel+" Freq = "+TerrTp.Freq+" Band = "+TerrTp.Band;
        if(SatTp != null)
            info += " Freq = "+SatTp.Freq+" Symbol = "+SatTp.Symbol+" Polar = "+SatTp.Polar;
        if(CableTp != null)
            info += " Channel = "+CableTp.Channel+" Freq ="+CableTp.Freq+" Symbol = "+CableTp.Symbol+" Qam = "+CableTp.Qam;
        return info;
    }
    public TpInfo(int tunerType){
        switch (tunerType) {
            case DVBT:
            case ISDBT: // Johnny add for ISDBT channel search 20180103
                TerrTp = new Terr();
                break;
            case DVBS:
                SatTp = new Sat();
                break;
            case DVBC:
                CableTp = new Cable();
                break;
        }
        this.TunerType = tunerType;
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


    public class Terr {
        private static final String TAG = "TpInfo.Terr";

        public static final int BAND_6MHZ = 0;
        public static final int BAND_7MHZ = 1;
        public static final int BAND_8MHZ = 2;
        private static final int NUM_OF_OTHER_DATA = 4;

        private int Channel;
        private int Freq;
        private int Band;
        private int Fft;
        private int Guard;
        private int Const;
        private int Hierarchy;


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
    }

    public class Sat {
        private static final String TAG = "TpInfo.Sat";

        public static final int POLAR_V = 0;
        public static final int POLAR_H = 1;
        private static final int NUM_OF_OTHER_DATA = 3;
        private int Freq;
        private int Symbol;
        private int Fec;
        private int Polar;
        private int Drot;
        private int Spect;

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
    }

    public class Cable {
        private static final String TAG = "TpInfo.Cable";

        public static final int QAM_16 = 0;
        public static final int QAM_32 = 1;
        public static final int QAM_64 = 2;
        public static final int QAM_128 = 3;
        public static final int QAM_256 = 4;
        public static final int QAM_AUTO = 5;

        private int Channel;
        private int Freq;
        private int Symbol;
        private int Qam;


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

        public String getOtherData(){
            String otherData = " ";
            Log.d(TAG, "getOtherData: "+otherData);
            return otherData;
        }
        public void setOtherData(String data){

        }
    }

}
