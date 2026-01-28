package com.prime.dtv;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.prime.datastructure.ServiceDefine.PrimeDtvInterface;
import com.prime.datastructure.utils.TVMessage;
import com.prime.dtv.service.database.DVBDatabase;

public class ServiceApplication extends Application {
    private static String TAG = "ServiceApplication";
    private static ServiceApplication gInstance;
    private static PrimeDtv g_dtv;
    private Handler gHandler;
    private static ServiceInterface gServiceInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        gInstance = this;
        gServiceInterface = ServiceInterface.getServiceInterfaceInstance(new PrimeDtvInterface.DTVCallback() {
            @Override
            public void onMessage(TVMessage msg) {
                Log.d(TAG, "onMessage msg = " + msg.getMessage() + " type " + msg.getMsgType() + " flag " + msg.getMsgFlag() ) ;
                if ( gHandler != null )
                {
                    gHandler.sendMessage(gHandler.obtainMessage(0, msg));
                }
            }
        }, this);
        g_dtv = gServiceInterface.get_prime_dtv();
    }

    public static ServiceApplication getInstance() {
        Log.d(TAG, "ServiceApplication getInstance " + gInstance);
        return gInstance;
    }

    public PrimeDtv get_prime_dtv() {
        return g_dtv;
    }

    public void registerHandler(Handler handler) {
        Log.d(TAG, "registerHandler " + handler);
        gServiceInterface.registerHandler(handler);
        gHandler = handler ;
    }

    public void unregisterHandler(Handler handler) {
        Log.d(TAG, "unregisterHandler " + handler);
        gServiceInterface.unregisterHandler(handler);
        gHandler = null ;
    }
}