package com.prime.dtv;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.prime.datastructure.ServiceDefine.PrimeDtvInterface;
import com.prime.datastructure.utils.TVMessage;
import com.prime.dtv.service.database.DVBDatabase;

public class ServiceInterface {
    private static String TAG = "ServiceInterface";

    private static PrimeDtv g_dtv;
    private Handler gHandler;
    private static Context gContext;
    private static ServiceInterface g_serviceInterface;

    public static ServiceInterface getServiceInterfaceInstance(Context context) {
        gContext = context;
        if(g_serviceInterface == null)
            g_serviceInterface = new ServiceInterface(context);
        return g_serviceInterface;
    }

    public static ServiceInterface getServiceInterfaceInstance(PrimeDtvInterface.DTVCallback callback, Context context) {
        gContext = context;
        if(g_serviceInterface == null)
            g_serviceInterface = new ServiceInterface(callback,context);
        return g_serviceInterface;
    }

    public static Context getContext() {
        return g_serviceInterface.gContext;
    }

    public ServiceInterface(Context context) {
        g_dtv = PrimeDtv.getInstance(new PrimeDtvInterface.DTVCallback() {
            @Override
            public void onMessage(TVMessage msg) {
                Log.d(TAG, "onMessage msg = " + msg.getMessage() + " type " + msg.getMsgType() + " flag " + msg.getMsgFlag() ) ;
                if ( gHandler != null )
                {
                    gHandler.sendMessage(gHandler.obtainMessage(0, msg));
                }
            }
        }, context);
        g_dtv.register_callbacks();
        g_dtv.backupDatabase(false);
        DVBDatabase.BackupWorker.scheduleDailyBackup(context);
    }

    public ServiceInterface(PrimeDtvInterface.DTVCallback callback, Context context) {
        g_dtv = PrimeDtv.getInstance(callback, context);
        g_dtv.register_callbacks();
        g_dtv.backupDatabase(false);
        DVBDatabase.BackupWorker.scheduleDailyBackup(context);
    }

    public void registerHandler(Handler handler) {
        Log.d(TAG, "registerHandler " + handler);
        gHandler = handler ;
    }

    public void unregisterHandler(Handler handler) {
        Log.d(TAG, "unregisterHandler " + handler);
        gHandler = null ;
    }

    public static PrimeDtv get_prime_dtv() {
        return g_dtv;
    }
}
