package com.prime.dtv.Interface;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public abstract class BaseManager {
    private final static String TAG = "BaseManager" ;
    private final int ANR_TIME_MS = 3*1000 ;
    private String mCallerTag = "BaseManager" ;
    private Context mApplicationContext = null ;
    private Handler mCallbackHandler = null ;
    private static PesiDtvFrameworkInterfaceCallback mCallback = null;
    private HandlerThread mHandlerThread = null;
    private Handler mHandlerThreadHandler = null;

    public BaseManager(Context context, String callerTag, Handler handler, Class<?> serviceClass) {
        mApplicationContext = context ;
        mCallerTag = callerTag ;
        mCallbackHandler = handler ;
        mHandlerThread = new HandlerThread(serviceClass.getName());
        mHandlerThread.start();
        mHandlerThreadHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                //
                BaseHandleMessage(msg);
            }
        };
        mCallback = new PesiDtvFrameworkInterfaceCallback(mCallbackHandler);
        Log.d( TAG, "init " + mCallerTag ) ;
    }

    public BaseManager(Context context, String callerTag, Handler handler, Class<?> serviceClass, Handler shareHandler) {
        mApplicationContext = context ;
        mCallerTag = callerTag ;
        mCallbackHandler = handler ;
        mHandlerThreadHandler = shareHandler;

        mCallback = new PesiDtvFrameworkInterfaceCallback(mCallbackHandler);
       Log.d( TAG, "init " + mCallerTag ) ;
    }

    public static PesiDtvFrameworkInterfaceCallback getPesiDtvFrameworkInterfaceCallback() {
        return mCallback;
    }
    public void sendCallbackMessage(int what, int arg1, int arg2, Object obj) {
        mCallback.sendCallbackMessage(what, arg1, arg2, obj);
    }

    public abstract void BaseHandleMessage(Message msg);

    public void CleanCommand(Message msg){
        mHandlerThreadHandler.removeMessages(msg.what);
    }

    public void CleanCommand(int what){
        mHandlerThreadHandler.removeMessages(what);
    }

    public void CleanAllCommand() {
        mHandlerThreadHandler.removeCallbacksAndMessages(null);
    }

    public void DoCommand(Message msg){
        Log.d(TAG,"DoCommand msg = "+msg.toString()+" what = "+msg.what);
        if(mHandlerThreadHandler.hasMessages(msg.what))
            mHandlerThreadHandler.removeMessages(msg.what);
        Message newMessage = mHandlerThreadHandler.obtainMessage(msg.what,msg.arg1,msg.arg2,msg.obj);
        mHandlerThreadHandler.sendMessage(newMessage);
    }

    public void DoCommandDelayed(Message msg, long msec){
        Log.d(TAG,"DoCommand msg = "+msg.toString()+" what = "+msg.what);
        mHandlerThreadHandler.sendMessageDelayed(msg, msec);
    }

    public Context getApplicationContext() {
        return mApplicationContext;
    }

    public Handler getHandlerThreadHandler() {
        return mHandlerThreadHandler;
    }

    public void destroy() {
        if(mHandlerThread != null)
            mHandlerThread.quit();
    }
}
