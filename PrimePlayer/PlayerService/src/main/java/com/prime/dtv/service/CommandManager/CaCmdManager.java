package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.prime.dtv.Interface.BaseManager;

public class CaCmdManager extends BaseManager {
    private static final String TAG = "CaCmdManager" ;
    public CaCmdManager(Context context, Handler handler) {
        super(context, TAG, handler, CaCmdManager.class);
    }

    /*
    widevine cas
     */
    public void WidevineCasSessionId(int sessionIndex, int sessionId) {

    }

    @Override
    public void BaseHandleMessage(Message msg) {

    }
}
