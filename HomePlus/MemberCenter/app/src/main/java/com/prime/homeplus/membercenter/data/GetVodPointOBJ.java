package com.prime.homeplus.membercenter.data;

public class GetVodPointOBJ {
    String mSoId, mDevicesno;
    public GetVodPointOBJ(String soId, String devicesno) {
        mSoId = soId;
        mDevicesno = devicesno;
    }

    public String getSoId() {
	return mSoId;
    }

    public String getDevicesno() {
	return mDevicesno;
    }
}

