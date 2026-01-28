package com.prime.dmg.launcher.Utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class LiAdDayFrequencyQueryJavaUtils {
    private static final String TAG = "QueryDayFrequencyUtils";

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public void queryDayFrequency(Context context, OnQueryDayFrequencyListener listener){
        new Thread(){
            public void run(){
                queryDayFrequencyCore(context, listener);
            }
        }.start();
    }

    private void queryDayFrequencyCore(Context context, OnQueryDayFrequencyListener listener){
        try {
            //【Query ContentProvider】step 3 : query by authorities -->
            String queryUri = "content://com.litv.liad.sdk.authorities/GetDayFrequency";
            Log.d(TAG, "queryUri : "+queryUri);
            Cursor cursor = context.getContentResolver().query(Uri.parse(queryUri), null, null, null, null);

            if(cursor == null){
                Log.e(TAG, "query day frequency fail, cursor is null");
                callbackFail(new Throwable("cursor is null"), listener);
                return;
            }

            if(cursor.getCount() <= 0){
                Log.e(TAG, "query day frequency fail, cursor.count <= 0, cursor.count = "+cursor.getCount());
                callbackFail(new Throwable("cursor is null"), listener);
                cursor.close();
                return;
            }


            Log.d(TAG, "query day frequency, cursor.count = "+cursor.getCount()+", cursor = "+cursor+", context = "+context+", contentResolver = "+context.getContentResolver());
            if( cursor.moveToFirst() ){
                try {
                    int appDayFrequencyCountColumnIndex = cursor.getColumnIndex("app_day_frequency_count");
                    int serverDayFrequencyLimitColumnIndex = cursor.getColumnIndex("server_day_frequency_limit");

                    Log.i(TAG, "query day frequency appDayFrequencyCountColumnIndex = "+appDayFrequencyCountColumnIndex+", serverDayFrequencyLimitColumnIndex = "+serverDayFrequencyLimitColumnIndex);

                    int appDayFrequencyCount = cursor.getInt(appDayFrequencyCountColumnIndex);
                    int serverDayFrequencyLimit = cursor.getInt(serverDayFrequencyLimitColumnIndex);
                    Log.w(TAG, "query day frequency result : appDayFrequencyCount = "+appDayFrequencyCount+", serverDayFrequencyLimit = "+serverDayFrequencyLimit);

                    callbackSuccess(appDayFrequencyCount, serverDayFrequencyLimit, listener);

                }catch (Exception e){
                    Log.e(TAG, "query day frequency read column exception, "+e.getMessage());
                    callbackFail(new Throwable("query day frequency read column exception : "+e.getMessage()), listener);
                }
            }else{
                Log.e(TAG, "cursor moveToFirst return false");
                callbackFail(new Throwable("cursor moveToFirst return false"), listener);
            }
            cursor.close();
        }catch (Exception e){
            Log.e(TAG, "query day frequency exception "+e.getMessage());
            callbackFail(new Throwable("query day frequency exception : "+e.getMessage()), listener);
        }
    }

    private void callbackFail(Throwable throwable, OnQueryDayFrequencyListener listener){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onFail(throwable);
            }
        });
    }

    private void callbackSuccess(int appDayFrequencyCount, int serverDayFrequencyLimit, OnQueryDayFrequencyListener listener){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onSuccess(appDayFrequencyCount, serverDayFrequencyLimit);
            }
        });
    }
}
