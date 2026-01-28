package com.mtest.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.mtest.config.MtestConfig;
import com.mtest.module.PesiSharedPreference;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;

public class BuzzerOnOffActivity extends DTVActivity {
    boolean mEnableBuzzer = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buzzer_on_off);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setBuzzer(mEnableBuzzer);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setBuzzer(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        TextView buzzerStatus = (TextView) findViewById(R.id.tv_item27_buzzer);
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mEnableBuzzer = !mEnableBuzzer;
                setBuzzer(mEnableBuzzer);
                if (mEnableBuzzer) {
                    buzzerStatus.setText(getResources().getString(R.string.str_buzzer_on));
                } else {
                    buzzerStatus.setText(getResources().getString(R.string.str_buzzer_off));
                }
                break;
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_buzzer), MtestConfig.TEST_RESULT_PASS);
                pesiSharedPreference.save();
                finish();
                break;
            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_buzzer), MtestConfig.TEST_RESULT_FAIL);
                pesiSharedPreference.save();
                finish();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setBuzzer(boolean enable) {
        if (enable) {
            MtestSetBuzzer(1);//buzzer on
        }
        else {
            MtestSetBuzzer(0);//buzzer off
        }
    }
}
