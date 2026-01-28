package com.prime.dtv.Interface;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PesiDtvFrameworkInterfaceCallback {
    private final static String TAG = "PesiDtvFrameworkInterfaceCallback" ;
    private Handler mHandler;

    public PesiDtvFrameworkInterfaceCallback(Handler handler) {
        mHandler = handler;
    }

    public void sendCallbackMessage(int what, int arg1, int arg2, Object obj)
    {
        if (mHandler != null) {
            Message m = mHandler.obtainMessage(what, arg1, arg2, obj);
            mHandler.sendMessage(m);
        }
        else
        {
            Log.d( TAG, "mHandler is null!!!!!!" ) ;
        }
    }
}
