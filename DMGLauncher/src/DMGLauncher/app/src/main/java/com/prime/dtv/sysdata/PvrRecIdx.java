package com.prime.dtv.sysdata;

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

}
