package com.prime.dmg.launcher.ACSDatabase;

import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public abstract class ACSContentObserver extends ContentObserver {
    String TAG = getClass().getSimpleName();
    WeakReference<AppCompatActivity> g_ref;
    public ACSContentObserver(Handler handler, AppCompatActivity activity) {
        super(handler);
        g_ref = new WeakReference<>(activity);
    }

    public abstract void onChange(boolean selfChange);
//    @Override
//    public void onChange(boolean selfChange) {
//        super.onChange(selfChange);
//        Log.d("gary","ACS database change !!");
//
//        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
//        String acs_data = acsDataProviderHelper.get_acs_provider_data(g_ref.get(),"pa_lock") ;
//        Log.d("gary","onChange pa_lock = "+acs_data);
//    }
}
