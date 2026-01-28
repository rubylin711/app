package com.prime.dtv.Interface;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

public class BaseManagerService extends Service {
    private final static String TAG = "BaseManagerService" ;
    public Handler mHandler = null ;
    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public BaseManagerService getService() {
            return BaseManagerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void registerHandler( Handler handler )
    {
        mHandler = handler ;
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
