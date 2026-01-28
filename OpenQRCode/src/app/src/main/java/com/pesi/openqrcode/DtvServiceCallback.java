package com.pesi.openqrcode;

import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import vendor.prime.hardware.dtvservice.V1_0.IDtvServiceCallback;

public class DtvServiceCallback extends IDtvServiceCallback.Stub {
    private static final String TAG = "OpenQRCodeDtvServiceCallback";

    @Override
    public void hwNotify(int i, int i1, int i2, int i3, ArrayList<Integer> arrayList) throws RemoteException {
        Log.d(TAG, "DtvServiceCallback coming !!!!!!!!!");
            int j;
            Parcel obj = Parcel.obtain();
            byte[] reply_data = new byte[arrayList.size()];
            for(j = 0; j < arrayList.size(); j++) {
//                    Log.d(TAG, "reply_arr[" + j + "] = " + String.format("0x%08X",reply_arr.get(j)));
                reply_data[j] = (byte) arrayList.get(j).intValue();
//                    Log.d(TAG, "reply_data[" + j + "] = " + String.format("0x%08X",reply_data[j]));
            }
            obj.unmarshall(reply_data,0,arrayList.size());
            obj.setDataPosition(0);
    }
}
