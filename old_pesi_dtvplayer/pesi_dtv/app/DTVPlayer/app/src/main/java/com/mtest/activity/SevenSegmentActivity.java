package com.mtest.activity;


import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.mtest.config.MtestConfig;
import com.mtest.module.PesiSharedPreference;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;

public class SevenSegmentActivity extends DTVActivity {
    private static final String TAG = "SevenSegmentActivity";
    boolean mSeven_segment_Status = true;
    private Handler  seven_segment_handler;
    private Runnable seven_segment_runnable;
    int seven_segment_count = 0;
    boolean mtest_prevent_hang_flag=false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seven_segment);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        setup_seven_segment_thread();
    }

    @Override
    protected void onStop(){
        Log.d(TAG, "onStop");
        super.onStop();
        MtestSevenSegment(10);
        seven_segment_handler.removeCallbacks(seven_segment_runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        seven_segment_handler.removeCallbacksAndMessages(null);
    }

    private void setup_seven_segment_thread(){
       Log.d(TAG, "setup_seven_segment_thread");
        seven_segment_handler = new Handler();
        seven_segment_runnable = new Runnable() {
            public void run() {
                if(mtest_prevent_hang_flag) {
                    if (seven_segment_count == 12)
                        seven_segment_count = 0;
                    MtestSevenSegment(seven_segment_count);
                    seven_segment_count++;
                }
                mtest_prevent_hang_flag=true;
                seven_segment_handler.postDelayed(seven_segment_runnable, 500);
            }
        };
        seven_segment_handler.post(seven_segment_runnable);
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        TextView seven_segment_Status = (TextView) findViewById(R.id.tv_item03_seven_segment);
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mSeven_segment_Status = !mSeven_segment_Status;
                if (mSeven_segment_Status) {

                    setup_seven_segment_thread();
                    seven_segment_Status.setText(getString(R.string.str_seven_segment_on));

                } else {
                    seven_segment_handler.removeCallbacks(seven_segment_runnable);
                    mtest_prevent_hang_flag=false;
                    seven_segment_count = 0;
                    MtestSevenSegment(10);
                    seven_segment_Status.setText(getString(R.string.str_seven_segment_off));
                }
                break;

            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_seven_segment), MtestConfig.TEST_RESULT_PASS);
                pesiSharedPreference.save();
                finish();
                break;

            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_seven_segment), MtestConfig.TEST_RESULT_FAIL);
                pesiSharedPreference.save();
                finish();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
