package com.mtest.activity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.mtest.config.MtestConfig;
import com.mtest.module.PesiSharedPreference;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.utils.TVMessage;

public class Ant_5vActivity extends DTVActivity {
    boolean mEnableAntenna5V = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ant_5v);
        MtestSetAntenna5V(0, GetCurTunerType(), 1);//ant 5v on
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);

        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_PIO_ANTENNA_OVERLOAD:
                overloadTuner();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        TextView tvAntStatus = (TextView) findViewById(R.id.tv_item07_ANT);
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mEnableAntenna5V = !mEnableAntenna5V;
                if (mEnableAntenna5V) {
                    MtestSetAntenna5V(0, GetCurTunerType(), 1);//ant 5v on
                    tvAntStatus.setText(getResources().getString(R.string.str_antenna_on));

                } else {
                    MtestSetAntenna5V(0, GetCurTunerType(), 0);//ant 5v off
                    tvAntStatus.setText(getResources().getString(R.string.str_antenna_off));
                }
                break;
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_antenna5v), MtestConfig.TEST_RESULT_PASS);
                pesiSharedPreference.save();
                finish();
                break;

            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_antenna5v), MtestConfig.TEST_RESULT_FAIL);
                pesiSharedPreference.save();
                finish();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void overloadTuner() {
        Toast tTunerError = Toast.makeText(this, "Warning!\nTuner overload......", Toast.LENGTH_SHORT);
        tTunerError.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        tTunerError.show();
    }
}
