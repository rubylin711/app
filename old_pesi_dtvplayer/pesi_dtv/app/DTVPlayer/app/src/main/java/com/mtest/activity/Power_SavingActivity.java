package com.mtest.activity;


import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.mtest.config.MtestConfig;
import com.mtest.module.PesiSharedPreference;
import com.mtest.module.PowerSavingModule;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;

import java.io.IOException;

public class Power_SavingActivity extends DTVActivity {
    public static final String POWER_SAVING_MENU_STRING = "key_power_saving";//jackie mod
    public static final String TAG = "power saving";

    private PesiSharedPreference mPesiSharedPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_saving);

        mPesiSharedPreference = new PesiSharedPreference(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_PROG_GREEN: // for kbro remote control
                // Decide how to go power save in module
                PowerSavingModule powerModule = new PowerSavingModule(this);
                powerModule.powerSave();
                break;
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                mPesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_power_saving), MtestConfig.TEST_RESULT_PASS);
                mPesiSharedPreference.save();
                //Toast.makeText(this, "BLUE", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                mPesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_power_saving), MtestConfig.TEST_RESULT_FAIL);
                mPesiSharedPreference.save();
                //Toast.makeText(this, "RED", Toast.LENGTH_SHORT).show();
                finish();
                break;
            default:
                break;

        }

        return super.onKeyDown(keyCode, event);
    }

//    // Edwin 20200525 for power saving -s
//    public static final String ACTION_REQUEST_SHUTDOWN = "com.android.internal.intent.action.REQUEST_SHUTDOWN";
//    public static final String EXTRA_KEY_CONFIRM = "android.intent.extra.KEY_CONFIRM";
//    public static final String EXTRA_USER_REQUESTED_SHUTDOWN = "android.intent.extra.USER_REQUESTED_SHUTDOWN";
//    public static final String str_enable ="enable";
//    private void shutdown () throws IOException
//    {
//        mPesiSharedPreference.putString(POWER_SAVING_MENU_STRING, str_enable);
//        mPesiSharedPreference.save();
//
//        String flag =  mPesiSharedPreference.getString(POWER_SAVING_MENU_STRING,"456");
//        Log.d(TAG, "xxx shutdown: POWER_SAVING_MENU_STRING = " + flag);
//
//        //deep standby
//        PowerSavingModule powerModule = PowerSavingModule.getInstance(this);
//        powerModule.shutdown();
//        /*Intent shutdown = new Intent(ACTION_REQUEST_SHUTDOWN);
//        shutdown.putExtra(EXTRA_KEY_CONFIRM, false);
//        shutdown.putExtra(EXTRA_USER_REQUESTED_SHUTDOWN, true);
//        shutdown.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(shutdown);*/
//
//        //STR
//        //String keyCommand = "input keyevent " + KeyEvent.KEYCODE_POWER;
//        //Runtime runtime = Runtime.getRuntime();
//        //Process proc = runtime.exec(keyCommand);
//    }
//    // Edwin 20200525 for power saving -e
}
