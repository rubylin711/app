package com.prime.datastructure.sysdata;


import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.prime.datastructure.config.Pvcfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CasData {
    private final String mRawJsonString;

    // subscriber id
    private String mSubscriberId;

    // global info
    private String mCasProvider;
    private int mTryLicenseAfterRefresh;
    private int mTryLicenseIfEntitledChannel;

    // device info
    private int mSuspended;
    private String mDivisionCode;
    private String mGrpBits;
    private String mAttrExt;
    private String ac1;
    private String ac2;
    private String ac3;
    private String ac4;
    private String ac5;
    private String ac6;
    private String ac7;
    private String ac8;

    // pvr
    private int mPvr;

    // entitled channel ids
    private List<String> mEntitledChannelIds;

    private List<String> mPromotionChannelIds;

    public CasData() {
        mRawJsonString = "";
        init();
    }

    public CasData(String rawJsonString) {
        mRawJsonString = rawJsonString;
        parseFromJsonString(rawJsonString);
    }

    private void init() {
        mSubscriberId = "";
        mCasProvider = "";
        mTryLicenseAfterRefresh = 0;
        mTryLicenseIfEntitledChannel = 0;
        mSuspended = 0;
        mDivisionCode = "";
        mGrpBits = "";
        mAttrExt = "";
        mPvr = Pvcfg.getPVR_PJ()?1:0;
        ac1 = "0";
        ac2 = "25149";
        ac3 = "0000";
        mEntitledChannelIds = new ArrayList<>();

        mPromotionChannelIds = new ArrayList<>();
    }

    public void parseFromJsonString(String rawJsonString) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(rawJsonString, JsonObject.class);

        if (jsonObject == null) {
            init(); // init with default value if empty json
            return;
        }

        mSubscriberId = jsonObject.get("subscriber_id").getAsString();

        // global info
        JsonObject globalInfo = jsonObject.getAsJsonObject("global_info");
        mCasProvider = globalInfo.get("cas_provider").getAsString();
        mTryLicenseAfterRefresh = globalInfo.get("try_licenses_after_refresh").getAsInt();
        mTryLicenseIfEntitledChannel = globalInfo.get("try_license_if_entitled_channel").getAsInt();

        // pvr
        mPvr = jsonObject.get("pvr").getAsInt();

        // device info
        JsonObject deviceInfo = jsonObject.getAsJsonObject("device_info");
        mSuspended = deviceInfo.get("suspended").getAsInt();
        mDivisionCode = deviceInfo.get("divisioncode").getAsString();
        mGrpBits = deviceInfo.get("grpbits").getAsString();
        mAttrExt = deviceInfo.get("attr_ext").getAsString();
        ac1 = deviceInfo.get("ac1").getAsString();
        ac2 = deviceInfo.get("ac2").getAsString();
        ac3 = deviceInfo.get("ac3").getAsString();
        ac4 = deviceInfo.get("ac4").getAsString();
        ac5 = deviceInfo.get("ac5").getAsString();
        ac6 = deviceInfo.get("ac6").getAsString();
        ac7 = deviceInfo.get("ac7").getAsString();
        ac8 = deviceInfo.get("ac8").getAsString();


        // entitled channel ids
        JsonArray jsonArray = jsonObject.getAsJsonArray("entitled_channel_ids");
        mEntitledChannelIds = Arrays.asList(gson.fromJson(jsonArray, String[].class));

        JsonArray jsonArray2 = jsonObject.getAsJsonArray("promotion_channel_ids");
        mPromotionChannelIds = Arrays.asList(gson.fromJson(jsonArray2, String[].class));
    }

    public String getRawJsonString() {
        return mRawJsonString;
    }

    public String getSubscriberId() {
        return mSubscriberId;
    }

    public String getCasProvider() {
        return mCasProvider;
    }

    public int getTryLicenseAfterRefresh() {
        return mTryLicenseAfterRefresh;
    }

    public int getTryLicenseIfEntitledChannel() {
        return mTryLicenseIfEntitledChannel;
    }

    public int getSuspended() {
        return mSuspended;
    }

    public String getDivisionCode() {
        return mDivisionCode;
    }

    public String getGrpBits() {
        return mGrpBits;
    }

    public String getAttrExt() {
        return mAttrExt;
    }

    public int getPvr() {
        return mPvr;
    }

    public String getAreaCode(){
        return ac1;
    }

    public String getBouquetId(){
        return ac2;
    }

    public String getZipCode(){
        return ac3;
    }

    public List<String> getEntitledChannelIds() {
        return mEntitledChannelIds;
    }

    public List<String> getPromotionChannelIds(){
        return mPromotionChannelIds;
    }

    @NonNull
    @Override
    public String toString() {
        return "CasData{" +
                "mSubscriberId='" + mSubscriberId + '\'' +
                ", mCasProvider='" + mCasProvider + '\'' +
                ", mTryLicenseAfterRefresh=" + mTryLicenseAfterRefresh +
                ", mTryLicenseIfEntitledChannel=" + mTryLicenseIfEntitledChannel +
                ", mSuspended=" + mSuspended +
                ", mDivisionCode='" + mDivisionCode + '\'' +
                ", mGrpBits='" + mGrpBits + '\'' +
                ", mAttrExt='" + mAttrExt + '\'' +
                ", ac1='" + ac1 + '\'' +
                ", ac2='" + ac2 + '\'' +
                ", ac3='" + ac3 + '\'' +
                ", ac4='" + ac4 + '\'' +
                ", ac5='" + ac5 + '\'' +
                ", ac6='" + ac6 + '\'' +
                ", ac7='" + ac7 + '\'' +
                ", ac8='" + ac8 + '\'' +
                ", mAttrExt='" + mAttrExt + '\'' +
                ", mAttrExt='" + mAttrExt + '\'' +
                ", mAttrExt='" + mAttrExt + '\'' +
                ", mAttrExt='" + mAttrExt + '\'' +
                ", mAttrExt='" + mAttrExt + '\'' +
                ", mAttrExt='" + mAttrExt + '\'' +
                ", mPvr=" + mPvr +
                ", mEntitledChannelIds=" + mEntitledChannelIds +
                ", mPromotionChannelIds=" + mPromotionChannelIds +
                '}';
    }


    public String getAc4() {
        return ac4;
    }

    public void setAc4(String ac4) {
        this.ac4 = ac4;
    }

    public String getAc5() {
        return ac5;
    }

    public void setAc5(String ac5) {
        this.ac5 = ac5;
    }

    public String getAc6() {
        return ac6;
    }

    public void setAc6(String ac6) {
        this.ac6 = ac6;
    }

    public String getAc7() {
        return ac7;
    }

    public void setAc7(String ac7) {
        this.ac7 = ac7;
    }

    public String getAc8() {
        return ac8;
    }

    public void setAc8(String ac8) {
        this.ac8 = ac8;
    }
}
