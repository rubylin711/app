package com.mtest.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.mtest.config.MtestConfig;
import com.mtest.module.LedModule;
import com.mtest.module.PesiSharedPreference;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;

public class LedOnOffActivity extends DTVActivity {
    private static final String TAG = LedOnOffActivity.class.getSimpleName();

    int mCurLEDStatus = LedModule.STATUS_ALL_OFF;

    private Global_Variables mGlobalVars;

    private final Handler mHandler = new Handler();
    private final Runnable mStableTestRunnable = new Runnable() {
        @Override
        public void run() {
            runStableTest();
            mHandler.postDelayed(mStableTestRunnable, 3000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_on_off);

        mGlobalVars = (Global_Variables) getApplicationContext();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Thread.sleep(500);  // Johnny 20181019 add delay to fix led is sometimes wrong after back from screen off
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setLEDByStatus(mCurLEDStatus);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHandler.removeCallbacks(mStableTestRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setLEDByStatus(LedModule.STATUS_ALL_ON);//STATUS_RED
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mCurLEDStatus < LedModule.STATUS_ALL_ON) {
                    mCurLEDStatus++;
                }
                else {
                    mCurLEDStatus = LedModule.STATUS_ALL_OFF;
                }

                setLEDByStatus(mCurLEDStatus);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (mCurLEDStatus > LedModule.STATUS_ALL_OFF) {
                    mCurLEDStatus--;
                }
                else {
                    mCurLEDStatus = LedModule.STATUS_ALL_ON;
                }

                setLEDByStatus(mCurLEDStatus);
                break;
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_led), MtestConfig.TEST_RESULT_PASS);
                pesiSharedPreference.save();
                //Toast.makeText(this, "BLUE", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_led), MtestConfig.TEST_RESULT_FAIL);
                pesiSharedPreference.save();
                //Toast.makeText(this, "RED", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case KeyEvent.KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
                if (mGlobalVars.isStableTestEnabled()) {
                    Toast.makeText(this, "Stable Test Start!", Toast.LENGTH_SHORT).show();
                    mHandler.removeCallbacks(mStableTestRunnable);
                    mHandler.post(mStableTestRunnable);
                }
                break;
            default:
                break;

        }
        return super.onKeyDown(keyCode, event);
    }

    private void setLEDByStatus(int status)
    {
        TextView tvLedStatus = (TextView) findViewById(R.id.tv_led_status);
        LedModule ledModule = new LedModule(this);

        ledModule.setOnOff(status); //MtestSetLedOnOff(status);
        switch (status) {
            case LedModule.STATUS_ALL_OFF:
                tvLedStatus.setText(getResources().getString(R.string.str_ledoff));//Scoty 20190417 modify led on/off string to green/red

                break;
            case LedModule.STATUS_GREEN:
                tvLedStatus.setText(getResources().getString(R.string.str_ledon_green));//Scoty 20190417 modify led on/off string to green/red

                break;
            case LedModule.STATUS_RED:
                tvLedStatus.setText(getResources().getString(R.string.str_ledon_red_or_ora));//Scoty 20190417 modify led on/off string to green/red

                break;
            case LedModule.STATUS_ALL_ON:
                tvLedStatus.setText(getResources().getString(R.string.str_ledon));
                break;
        }
    }

    private void runStableTest() {
        if (++mCurLEDStatus > LedModule.STATUS_ALL_ON) {
            mCurLEDStatus = LedModule.STATUS_ALL_OFF;
        }
        setLEDByStatus(mCurLEDStatus);
    }
}
