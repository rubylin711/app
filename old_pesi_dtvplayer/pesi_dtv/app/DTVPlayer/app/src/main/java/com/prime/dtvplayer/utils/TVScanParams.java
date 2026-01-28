package com.prime.dtvplayer.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.prime.dtvplayer.Sysdata.TpInfo;

/**
 * Created by ethan_lin on 2017/10/30.
 */

public class TVScanParams/* implements Parcelable */{
    private static final String TAG = "TVScanParmas";

    private int TunerId;
    private TpInfo TpInfo;
    private int TpId;
    private int SatId;
    private int ScanMode;
    private int SearchOptionTVRadio;
    private int SearchOptionCaFta;
    private int NitSearch;
    private int OneSegment;

    public static final int SCAN_MODE_AUTO = 0;
    public static final int SCAN_MODE_MANUAL = 1;
    public static final int SCAN_MODE_NETWORK = 2;
    public static final int SCAN_MODE_ALL_SAT = 3;
    public static final int SCAN_VMX_SEARCH = 4; // connie 20180919 add for vmx search

    public static final int SEARCH_OPTION_ALL = 0;
    public static final int SEARCH_OPTION_TV_ONLY = 1;
    public static final int SEARCH_OPTION_RADIO_ONLY = 2;
    public static final int SEARCH_OPTION_CA_ONLY = 1;
    public static final int SEARCH_OPTION_FTA_ONLY = 2;

    public TVScanParams(int tuner_id,TpInfo tpInfo,int satId,int scanMode, int searchOptionTVRadio, int searchOptionCaFta,int nitSearch,int oneSegment){
        TunerId = tuner_id;
        TpInfo = tpInfo;
        TpId = TpInfo.getTpId();
        SatId = satId;
        ScanMode = scanMode;
        SearchOptionTVRadio = searchOptionTVRadio;
        SearchOptionCaFta = searchOptionCaFta;
        NitSearch = nitSearch;
        OneSegment = oneSegment;
    }
/*
    protected TVScanParams(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<TVScanParams> CREATOR = new Creator<TVScanParams>() {
        @Override
        public TVScanParams createFromParcel(Parcel in) {
            return new TVScanParams(in);
        }

        @Override
        public TVScanParams[] newArray(int size) {
            return new TVScanParams[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(TunerId);
        dest.writeInt(TpId);
        dest.writeInt(SatId);
        dest.writeInt(ScanMode);
        dest.writeInt(SearchOptionTVRadio);
        dest.writeInt(SearchOptionCaFta);
        dest.writeInt(NitSearch);
        dest.writeInt(OneSegment);
    }

    public void readFromParcel(Parcel in){
        TunerId = in.readInt();
        TpId = in.readInt();
        SatId = in.readInt();
        ScanMode = in.readInt();
        SearchOptionTVRadio = in.readInt();
        SearchOptionCaFta = in.readInt();
        NitSearch = in.readInt();
        OneSegment = in.readInt();
    }
*/
    public int getTunerId(){
        return TunerId;
    }

    public TpInfo getTpInfo(){
        return TpInfo;
    }

    public int getTpId(){
        return TpId;
    }

    public int getScanMode(){
        return ScanMode;
    }

    public int getSearchOptionTVRadio(){
        return SearchOptionTVRadio;
    }

    public int getSearchOptionCaFta(){
        return SearchOptionCaFta;
    }

    public int getNitSearch() {
        return NitSearch;
    }

    public int getOneSegment() {
        return OneSegment;
    }

    public int getSatId() {
        return SatId;
    }
}
