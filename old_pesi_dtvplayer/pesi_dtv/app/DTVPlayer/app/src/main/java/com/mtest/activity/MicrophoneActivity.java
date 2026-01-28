package com.mtest.activity;


import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mtest.config.MtestConfig;
import com.mtest.module.PesiSharedPreference;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;

import java.text.DecimalFormat;

public class MicrophoneActivity extends DTVActivity {
    private static final int MAX_MIC = 7;
    private static final int MAX_MIC_LR = 8;
    private static final int MAX_ALC = 241; //0 ~ 0xF1

    private String[] mStrArrayMICValue = {
            "0",
            "+8",
            "+12",
            "+15",
            "+18",
            "+24",
            "+30",
            "+36"
    };

    private Global_Variables mGlobalVars;

    SeekBar mSeekBarMic;
    SeekBar mSeekBarMicL;
    SeekBar mSeekBarMicR;
    SeekBar mSeekBarAlc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microphone);
        mGlobalVars = (Global_Variables) getApplicationContext();
    }

    @Override
    protected void onStart() {
        super.onStart();

        initSeekBars();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mGlobalVars.setMicInputGain(mSeekBarMic.getProgress());
        mGlobalVars.setMicLInputGain(mSeekBarMicL.getProgress());
        mGlobalVars.setMicRInputGain(mSeekBarMicR.getProgress());
        mGlobalVars.setMicAlcGain(mSeekBarAlc.getProgress());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);

        switch (keyCode) {
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_microphone), MtestConfig.TEST_RESULT_PASS);
                pesiSharedPreference.save();
                finish();
                break;
            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_microphone), MtestConfig.TEST_RESULT_FAIL);
                pesiSharedPreference.save();
                finish();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initSeekBars() {
        mSeekBarMic = (SeekBar) findViewById(R.id.sb_mic);
        mSeekBarMicL = (SeekBar) findViewById(R.id.sb_mic_l);
        mSeekBarMicR = (SeekBar) findViewById(R.id.sb_mic_r);
        mSeekBarAlc = (SeekBar) findViewById(R.id.sb_alc);

        mSeekBarMic.setMax(MAX_MIC);
        mSeekBarMicL.setMax(MAX_MIC_LR);
        mSeekBarMicR.setMax(MAX_MIC_LR);
        mSeekBarAlc.setMax(MAX_ALC);

        mSeekBarMic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView tvMicValue = (TextView) findViewById(R.id.tv_mic_value);
                tvMicValue.setText(mStrArrayMICValue[progress]);

                // set mic
                MtestMicSetInputGain(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mSeekBarMicL.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView tvMicValueL = (TextView) findViewById(R.id.tv_mic_value_l);
                double valueDB = -3 + progress*0.75;    // -3 ~ +3, interval = +0.75
                DecimalFormat fmt = new DecimalFormat("+0.000;-0.000");
                tvMicValueL.setText(fmt.format(valueDB));

                // set mic l
                MtestMicSetLRInputGain(0, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mSeekBarMicR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView tvMicValueR = (TextView) findViewById(R.id.tv_mic_value_r);
                double valueDB = -3 + progress*0.75;    // -3 ~ +3, interval = +0.75
                DecimalFormat fmt = new DecimalFormat("+0.000;-0.000");
                tvMicValueR.setText(fmt.format(valueDB));

                // set mic r
                MtestMicSetLRInputGain(1, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mSeekBarAlc.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView tvAlcValue = (TextView) findViewById(R.id.tv_alc_value);

                if (progress <= 4) { // 0~4 = mute
                    tvAlcValue.setText(R.string.str_mute);
                }
                else {  // 5 = -52.5, 145 = 0, 241 = +36, interval = +0.375
                    int gap = progress - 5;
                    double valueDB = -52.5 + gap*0.375;
                    DecimalFormat fmt = new DecimalFormat("+0.000;-0.000");
                    tvAlcValue.setText(fmt.format(valueDB));
                }

                // set alc
                MtestMicSetAlcGain(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mSeekBarMic.setKeyProgressIncrement(1);  // Sets the amount of progress changed via the arrow keys.
        mSeekBarMicL.setKeyProgressIncrement(1);  // Sets the amount of progress changed via the arrow keys.
        mSeekBarMicR.setKeyProgressIncrement(1);  // Sets the amount of progress changed via the arrow keys.
        mSeekBarAlc.setKeyProgressIncrement(1);  // Sets the amount of progress changed via the arrow keys.

        mSeekBarMic.setProgress(mGlobalVars.getMicInputGain());
        mSeekBarMicL.setProgress(mGlobalVars.getMicLInputGain());
        mSeekBarMicR.setProgress(mGlobalVars.getMicRInputGain());
        mSeekBarAlc.setProgress(mGlobalVars.getMicAlcGain());
    }
}
