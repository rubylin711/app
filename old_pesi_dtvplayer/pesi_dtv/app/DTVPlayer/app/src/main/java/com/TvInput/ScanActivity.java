package com.TvInput;

import android.os.Bundle;
import android.util.Log;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

public class ScanActivity extends DTVActivity
{
    private final String TAG = "ScanActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.tv_input_setup_fragment);
    }
}
