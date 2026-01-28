package com.prime.homeplus.vbm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "HOMEPLUS_VBM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
        String appVersion = "v1.0.4";
        tvVersion.setText("Version: " + appVersion);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (MainService.isRunning == false) {
            Intent serviceIntent = new Intent(MainActivity.this, MainService.class);
            startService(serviceIntent);
        } else {
            Log.d(TAG, "already running.");
        }
    }
}