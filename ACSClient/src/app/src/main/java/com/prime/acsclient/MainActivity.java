package com.prime.acsclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(this, ACSService.class);
        serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(serviceIntent);
    }
}
