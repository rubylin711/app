package com.prime.dtv.sysdata;


import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.prime.dtv.config.Pvcfg;

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

    // pvr
    private int mPvr;

    // entitled channel ids
    private List<String> mEntitledChannelIds;

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
        mEntitledChannelIds = new ArrayList<>();
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

        // entitled channel ids
        JsonArray jsonArray = jsonObject.getAsJsonArray("entitled_channel_ids");
        mEntitledChannelIds = Arrays.asList(gson.fromJson(jsonArray, String[].class));
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

    public List<String> getEntitledChannelIds() {
        return mEntitledChannelIds;
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
                ", mPvr=" + mPvr +
                ", mEntitledChannelIds=" + mEntitledChannelIds +
                '}';
    }


}
