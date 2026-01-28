package com.prime.dtv.service.datamanager;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.TpInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


// json data like this
//String jsonString = "{\n" +
//        "    \"Hotbird 13F\": [\n" +
//        "        {\n" +
//        "            \"frequency\": \"10727\",\n" +
//        "            \"polarization\": \"H\",\n" +
//        "            \"modulation_type\": \"DVB-S2\",\n" +
//        "            \"modulation_scheme\": \"8PSK\",\n" +
//        "            \"symbol_rate\": \"30000\",\n" +
//        "            \"fec\": \"3/4\"\n" +
//        "        }\n" +
//        "        // More items...\n" +
//        "    ]\n" +
//        "}";

public class SatelliteDefaultData {
    public static final String TAG = "SatelliteDefaultData";

    public class TpData {
        public String frequency;
        public String polarization;
        public String modulation_type;
        public String modulation_scheme;
        public String symbol_rate;
        public String fec;
    }

    public SatInfo mSatInfo;
    public List<TpInfo> mTpInfoList;

    public int mEndTpId;
    public SatelliteDefaultData(Context context,int which_sat) {
        mSatInfo = new SatInfo();
        mTpInfoList = new ArrayList<>();
        if(which_sat == DefaultValue.SAT_SUND_DIRECT) {
            parser_data(context,"sun_direct.json");
        }
        else if(which_sat == DefaultValue.SAT_ASTRA) {
            parser_data(context,"astra.json");
        }
        else if(which_sat == DefaultValue.SAT_HOTBIRD) {
            parser_data(context,"hotbird.json");
        }
        else if(which_sat == DefaultValue.SAT_TURK) {
            parser_data(context,"turksat.json");
        }
    }

    private String loadJSON(Context context, String fileName) {
        String json = null;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void parser_data(Context context,String fileName) {
        String jsonStr = loadJSON(context,fileName);
        if(jsonStr != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonStr);
                Iterator<String> satelliteKeys = jsonObject.keys();
                while (satelliteKeys.hasNext()) {
                    String satelliteName = satelliteKeys.next();

//                    Log.d(TAG,"Satellite: " + satelliteName);
                    mSatInfo.setSatName(satelliteName);

                    JSONArray frequenciesArray = jsonObject.getJSONArray(satelliteName);
                    for (int i = 0; i < frequenciesArray.length(); i++) {
                        TpInfo tpInfo = new TpInfo(TpInfo.DVBS);
                        JSONObject frequencyData = frequenciesArray.getJSONObject(i);
//                        Log.d(TAG,"  Frequency: " + frequencyData.getString("frequency"));
//                        Log.d(TAG,"  Polarization: " + frequencyData.getString("polarization"));
//                        Log.d(TAG,"  Modulation Type: " + frequencyData.getString("modulation_type"));
//                        Log.d(TAG,"  Modulation Scheme: " + frequencyData.getString("modulation_scheme"));
//                        Log.d(TAG,"  Symbol Rate: " + frequencyData.getString("symbol_rate"));
//                        Log.d(TAG,"  FEC: " + frequencyData.getString("fec"));
//                        Log.d(TAG,"-------------------");

                        tpInfo.SatTp.setFreq(Integer.valueOf(frequencyData.getString("frequency")));
                        tpInfo.SatTp.setPolar(frequencyData.getString("polarization").equals("V") ? TpInfo.Sat.POLAR_V: TpInfo.Sat.POLAR_H);
                        tpInfo.SatTp.setFec(get_fec(frequencyData.getString("fec")));
                        tpInfo.SatTp.setMod(get_mod(frequencyData.getString("modulation_scheme")));
                        tpInfo.SatTp.setSymbol(Integer.valueOf(frequencyData.getString("symbol_rate")));

                        mTpInfoList.add(tpInfo);
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private int get_fec(String value) {
        int fec;
        if(value.equals("2/3")) {
            fec = TpInfo.Sat.FEC_2_3;
        }
        else if(value.equals("3/4")) {
            fec = TpInfo.Sat.FEC_3_4;
        }
        else if(value.equals("5/6")) {
            fec = TpInfo.Sat.FEC_5_6;
        }
        else if(value.equals("8/9")) {
            fec = TpInfo.Sat.FEC_8_9;
        }
        else if(value.equals("9/10")) {
            fec = TpInfo.Sat.FEC_9_10;
        }
        else if(value.equals("1/2")) {
            fec = TpInfo.Sat.FEC_1_2;
        }
        else if(value.equals("7/8")) {
            fec = TpInfo.Sat.FEC_7_8;
        }
        else if(value.equals("4/5")) {
            fec = TpInfo.Sat.FEC_4_5;
        }
        else{
            fec = TpInfo.Sat.FEC_3_5;
        }


        return fec;
    }
    private int get_mod(String value) {
        int mod = TpInfo.Sat.MOD_UNDEFINED;
        if(value.equals("QPSK")) {
            mod = TpInfo.Sat.MOD_QPSK;
        }
        else if(value.equals("8PSK")) {
            mod = TpInfo.Sat.MOD_8PSK;
        }
        else if(value.equals("16QAM")) {
            mod = TpInfo.Sat.MOD_16QAM;
        }
        else if(value.equals("16PSK")) {
            mod = TpInfo.Sat.MOD_16PSK;
        }
        else if(value.equals("32PSK")) {
            mod = TpInfo.Sat.MOD_32PSK;
        }
        else if(value.equals("ACM")) {
            mod = TpInfo.Sat.MOD_ACM;
        }
        else if(value.equals("8APSK")) {
            mod = TpInfo.Sat.MOD_8APSK;
        }
        else if(value.equals("16APSK")) {
            mod = TpInfo.Sat.MOD_16APSK;
        }
        else if(value.equals("32APSK")) {
            mod = TpInfo.Sat.MOD_32APSK;
        }
        else if(value.equals("64APSK")) {
            mod = TpInfo.Sat.MOD_64APSK;
        }
        else if(value.equals("128APSK")) {
            mod = TpInfo.Sat.MOD_128APSK;
        }
        else if(value.equals("256APSK")) {
            mod = TpInfo.Sat.MOD_256APSK;
        }
        return mod;
    }
}
