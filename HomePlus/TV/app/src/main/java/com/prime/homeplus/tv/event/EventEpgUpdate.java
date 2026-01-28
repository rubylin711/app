package com.prime.homeplus.tv.event;

import com.prime.homeplus.tv.data.ADData;

public class EventEpgUpdate {
    private ADData mADData;

    public ADData getmADData() {
        return mADData;
    }

    public void setmADData(ADData mADData) {
        this.mADData = mADData;
    }

    @Override
    public String toString() {
        return "EventEpgUpdate{" +
                "mADData=" + mADData +
                '}';
    }
}
