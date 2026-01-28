package com.prime.dtv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.prime.datastructure.CommuincateInterface.BookModule;
import com.prime.datastructure.sysdata.BookInfo;

public class PrimeTimerReceiver extends BroadcastReceiver {
    private static final String TAG = "PrimeTimerReceiver";
    private static final String SERVICE_PACKAGE = "com.prime.homeplus.tv";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle data = intent.getExtras();

        if (action != null && data != null) {
            Log.d(TAG, "onReceive: " + action);
            if (action.equals(BookInfo.ACTION_TIMER_RECORD)) {
                BookInfo bookInfo = new BookInfo(data);
                Log.d(TAG, "onReceive: " + bookInfo);

                Intent serviceIntent = new Intent(BookModule.ACTION_START_RECORD);
                serviceIntent.setPackage(SERVICE_PACKAGE); // other app handle recording

                long bookId = bookInfo.getBookId(); // from int to long
                serviceIntent.putExtra(BookInfo.BOOK_ID, bookId);

                ContextCompat.startForegroundService(context, serviceIntent);
            }
        }
    }
}
