package com.prime.dtvplayer.Service.Database.NetStreamDatabaseTable;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.content.BroadcastReceiver;

import androidx.annotation.Nullable;

import com.dolphin.dtv.CallbackService;

import static com.prime.dtvplayer.Activity.DTVActivity.WV_TEST_SET_SESSION_ID;

public class NetProgramContentObserver extends ContentObserver {
    private static final String TAG = "TestDataBase_Observer";
    Handler mHandler = new Handler();

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public NetProgramContentObserver(Handler handler) {
        super(handler);
        Log.d(TAG, "NetProgramContentObserver: IN");
    }

    @Override
    public void onChange(boolean selfChange) {
        // TODO Auto-generated method stub
        super.onChange(selfChange);
        Log.d(TAG, "onChange: IN");

//        Message msg = Message.obtain();
//        msg.what = 123456;
//        msg.obj = "OK";
//        mHandler.sendMessage(msg);

//        Intent SearchIntent = new Intent();
//        SearchIntent.setAction("com.prime.netprogram.database.update");
//        this.mHandler.sendBroadcast(SearchIntent, "android.permission.NETPROGRAM_BROADCAST");

//        Message msg = new Message();
//        msg.what = 12345;
//        msg.obj = "obj";
//        mHandler.sendMessage(msg);

    }


}
