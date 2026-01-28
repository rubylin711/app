package com.prime.datastructure.sysdata;

import android.text.TextUtils;
import android.util.Log;

public class PvrRecIdx {
    private static final String TAG="PvrRecIdx";
    private int mMasterIdx;
    private int mSeriesIdx = 0xffff;

    public PvrRecIdx(int masterIdx, int seriesIdx) {
        mMasterIdx = masterIdx;
        mSeriesIdx = seriesIdx;
    }

    public int getMasterIdx() {
        return mMasterIdx;
    }

    public void setMasterIdx(int masterIdx) {
        mMasterIdx = masterIdx;
    }

    public int getSeriesIdx() {
        return mSeriesIdx;
    }

    public void setSeriesIdx(int seriesIdx) {
        mSeriesIdx = seriesIdx;
    }

    public boolean equals(PvrRecIdx recIdx) {
        return recIdx != null
                && mMasterIdx == recIdx.getMasterIdx()
                && mSeriesIdx == recIdx.getSeriesIdx();
    }

    public String toCombinedString() {
        return mMasterIdx + ":" + mSeriesIdx;
    }

    public static PvrRecIdx fromCombinedString(String combinedString) {
        if (combinedString == null || combinedString.isEmpty()) {
            return null;
        }

        String[] splits = combinedString.split(":");

        if (splits.length != 2) {
            return null;
        }

        String masterIdxStr = splits[0];
        String seriesIdxStr = splits[1];

        if (masterIdxStr.isEmpty() || seriesIdxStr.isEmpty() ||
                !TextUtils.isDigitsOnly(masterIdxStr) ||
                !TextUtils.isDigitsOnly(seriesIdxStr)) {
            return null;
        }

        try {
            int masterIdx = Integer.parseInt(masterIdxStr);
            int seriesIdx = Integer.parseInt(seriesIdxStr);
            return new PvrRecIdx(masterIdx, seriesIdx);
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException: combinedString = " + combinedString, e);
            return null;
        }
    }

}
