package com.mtest.activity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.mtest.config.MtestConfig;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.GposInfo;

import java.util.Locale;


public class HiddenActivity extends DTVActivity {
    final String TAG = "HiddenActivity";
    Global_Variables gv;
    //private HiDisplayManager displayManager = null;
    private static final int HDMI_MODE = 1;

    private static final int AUDIO_OUTPUT_PCM = 0;
    private static final int AUDIO_OUTPUT_BITSTREAM = 1;

    private static final int HIDDEN_INPUT_TIMEOUT = 3000; // ms, reset mHiddenInput // Johnny 20190613 add timeout to reset hidden input

    private ConstraintLayout  mMainLayout;
    private TextView mTvAudioOutput;
    private Handler mHandler = new Handler(); // Johnny 20190613 add timeout to reset hidden input

    private String mHiddenInput = "";
    private String mAudioOutputArray[];
    private int mCurAudioOutputIdx;

    private ViewUiDisplay viewUiDisplay = null;

    // Johnny 20190613 add timeout to reset hidden input
    private Runnable mResetHiddenInputRunnable = new Runnable() {
        @Override
        public void run() {
            mHiddenInput = "";
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden);

        gv = (Global_Variables) getApplicationContext();
        //getWindow().getDecorView().setBackgroundColor(Color.LTGRAY);

        //displayManager = new HiDisplayManager();
        mCurAudioOutputIdx = GposInfoGet().getDolbyMode();
        mAudioOutputArray = getResources().getStringArray(R.array.STR_ARRAY_AUDIO_OUTPUT);

        initViews();
        m_hidden_init();
        viewUiDisplay = GetViewUiDisplay();
        //ViewUiDisplayInit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        GposInfo gposInfo = GposInfoGet();
        gposInfo.setDolbyMode(mCurAudioOutputIdx);
        GposInfoUpdate(gposInfo);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initViews() {
        mTvAudioOutput = (TextView) findViewById(R.id.tv_hidden_red_text);
    }

    private void m_hidden_init() {
        TextView tvhidden_title = (TextView) findViewById(R.id.tv_hidden_title);
        //        tvhidden_title.setText("HDMI DD DDP" + );
        tvhidden_title.setText(String.format(Locale.getDefault(), "Service : %s / Apk : %s", GetPesiServiceVersion(), GetApkSwVersion())); // version

//        TextView tvhidden_osd = (TextView) findViewById(R.id.tv_hidden_osd);
//        tvhidden_osd.setText("OSD Hide/Display");
        mTvAudioOutput.setText(/*"TMDS Off"*/mAudioOutputArray[mCurAudioOutputIdx]);
        TextView tvhidden_yellow_text = (TextView) findViewById(R.id.tv_hidden_yellow_text);
        tvhidden_yellow_text.setText(/*"1080i"*/"");
        TextView tvhidden_blue = (TextView) findViewById(R.id.tv_hidden_blue_text);
        tvhidden_blue.setText("");
        TextView tvhidden_green_text = (TextView) findViewById(R.id.tv_hidden_green_text);
        tvhidden_green_text.setText(/*"ETH OFF"*/"");
        TextView tvhidden_chup = (TextView) findViewById(R.id.tv_hidden_chup);
        tvhidden_chup.setText("CH+/PR+");
        TextView tvhidden_chdown = (TextView) findViewById(R.id.tv_hidden_chdown);
        tvhidden_chdown.setText("CH-/PR-");
        TextView tvhidden_volup = (TextView) findViewById(R.id.tv_hidden_volup);
        tvhidden_volup.setText("Vol+");
        TextView tvhidden_voldown = (TextView) findViewById(R.id.tv_hidden_voldown);
        tvhidden_voldown.setText("Vol-");
        TextView tvhidden_vol_value = (TextView) findViewById(R.id.tv_hidden_vol_value);
        tvhidden_vol_value.setText("MAX");
        TextView tvhidden_dump = (TextView) findViewById(R.id.tv_hidden_dump);
        tvhidden_dump.setText("DUMP 16M");
        TextView tvhidden_EHCI_fun = (TextView) findViewById(R.id.tv_hidden_EHCI_FUN);
        tvhidden_EHCI_fun.setText("Menu");
        TextView tvhidden_EHCI_switch = (TextView) findViewById(R.id.tv_hidden_EHCI_SWITCH);
        tvhidden_EHCI_switch.setText("OFF");
        TextView tvhidden_EHCI_val = (TextView) findViewById(R.id.tv_hidden_EHCI_VAL);
        tvhidden_EHCI_val.setText("0");
        TextView tvhidden_exit = (TextView) findViewById(R.id.tv_hidden_exit);
        tvhidden_exit.setText("EXIT(Back)");
        TextView tvhidden_stabilily = (TextView) findViewById(R.id.tv_hidden_stability);
        tvhidden_stabilily.setText("STABILITY(6153)");
        TextView tvhidden_unicable_fun = (TextView) findViewById(R.id.tv_hidden_uni_cable_fun);
        tvhidden_unicable_fun.setText("Mute");
        TextView tvhidden_unicable_switch = (TextView) findViewById(R.id.tv_hidden_uni_cable_switch);
        tvhidden_unicable_switch.setText("Uni_Cable");
        TextView tvhidden_ch_name = (TextView) findViewById(R.id.tv_hidden_ch_name);

        if (ViewHistory.getCurChannel() != null && (gv.m_GetIPTVActive() == false))
            tvhidden_ch_name.setText(Integer.toString(ViewHistory.getCurChannel().getChannelNum()) + "-" + ViewHistory.getCurChannel().getChannelName());
        else
            tvhidden_ch_name.setText("");
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(/*AudioManager.STREAM_MUSIC*/AudioManager.STREAM_SYSTEM);
        int vol = audioManager.getStreamMaxVolume(/*AudioManager.STREAM_MUSIC*/AudioManager.STREAM_SYSTEM);
        if (vol == max)
            tvhidden_vol_value.setText("MAX");
        else
            tvhidden_vol_value.setText(Integer.toString(vol));

        mMainLayout = (ConstraintLayout) findViewById(R.id.layout_hidden_activity);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(/*AudioManager.STREAM_MUSIC*/AudioManager.STREAM_SYSTEM);
        int vol = audioManager.getStreamVolume(/*AudioManager.STREAM_MUSIC*/AudioManager.STREAM_SYSTEM);
        TextView tvhidden_vol_value = (TextView) findViewById(R.id.tv_hidden_vol_value);
        TextView tvhidden_ch_name = (TextView) findViewById(R.id.tv_hidden_ch_name);
        boolean osdVisible;

        switch (keyCode) {
            case KeyEvent.KEYCODE_CHANNEL_UP:
            case KeyEvent.KEYCODE_DPAD_UP:
                if (ViewHistory.getCurChannel() != null && (gv.m_GetIPTVActive() == false)) {
                    viewUiDisplay.ChangeBannerInfoUp();
                    viewUiDisplay.ChangeProgram();
                    if (ViewHistory.getCurChannel() != null)
                        tvhidden_ch_name.setText(Integer.toString(ViewHistory.getCurChannel().getChannelNum()) + "-" + ViewHistory.getCurChannel().getChannelName());
                }
                break;

            case KeyEvent.KEYCODE_CHANNEL_DOWN:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (ViewHistory.getCurChannel() != null && (gv.m_GetIPTVActive() == false)) {
                    viewUiDisplay.ChangeBannerInfoDown();
                    viewUiDisplay.ChangeProgram();
                    if (ViewHistory.getCurChannel() != null)
                        tvhidden_ch_name.setText(Integer.toString(ViewHistory.getCurChannel().getChannelNum()) + "-" + ViewHistory.getCurChannel().getChannelName());
                }
                break;

            /*case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                //audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
                //Toast.makeText(this, "KEYCODE_DPAD_LEFT", Toast.LENGTH_SHORT).show();
                if (vol <= 1)
                    vol = 0;
                else
                    vol--;
                audioManager.setStreamVolume(*//*AudioManager.STREAM_MUSIC*//*AudioManager.STREAM_SYSTEM, vol, *//*AudioManager.STREAM_MUSIC*//*AudioManager.STREAM_SYSTEM);
                //vol = audioManager.getStreamVolume(AudioManager.FLAG_PLAY_SOUND);
                //Toast.makeText(this, Integer.toString(vol), Toast.LENGTH_SHORT).show();
                tvhidden_vol_value.setText(Integer.toString(vol));
                break;

            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                //Toast.makeText(this, "KEYCODE_DPAD_LEFT", Toast.LENGTH_SHORT).show();
                if (vol >= max - 1)
                    vol = max;
                else
                    vol++;
                audioManager.setStreamVolume(*//*AudioManager.STREAM_MUSIC*//*AudioManager.STREAM_SYSTEM, vol, *//*AudioManager.STREAM_MUSIC*//*AudioManager.STREAM_SYSTEM);
                //vol = audioManager.getStreamVolume(AudioManager.FLAG_PLAY_SOUND);
                //Toast.makeText(this, Integer.toString(vol), Toast.LENGTH_SHORT).show();
                if (vol == max)
                    tvhidden_vol_value.setText("MAX");
                else
                    tvhidden_vol_value.setText(Integer.toString(vol));
                break;*/

            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                //audio pcm/dd
                if (mCurAudioOutputIdx == AUDIO_OUTPUT_BITSTREAM) {
                    mCurAudioOutputIdx = AUDIO_OUTPUT_PCM;
                }
                else {
                    mCurAudioOutputIdx = AUDIO_OUTPUT_BITSTREAM;
                }

                AvControlAudioOutput(ViewHistory.getPlayId(), mCurAudioOutputIdx);
                mTvAudioOutput.setText(mAudioOutputArray[mCurAudioOutputIdx]);
                break;
            case KeyEvent.KEYCODE_PROG_YELLOW:
            case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
                //OSD
                osdVisible = mMainLayout.getVisibility() == View.VISIBLE;
                setOSDVisibility(!osdVisible);
                break;

            //case KeyEvent.KEYCODE_PROG_YELLOW:
            //case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
            //    Log.d(TAG, "onKeyDown: displayManager.getOutputEnable(0) = "+displayManager.getOutputEnable(0));
            //    if ( displayManager.getOutputEnable(0) != HDMI_MODE )
            //        break;
            //
            //    int format = displayManager.getFmt();
            //    Log.d(TAG, "onKeyDown: format = "+format);
            //    switch ( format )
            //    {
            //        case HiDisplayManager.ENC_FMT_1080P_60:
            //            displayManager.setFmt(HiDisplayManager.ENC_FMT_1080i_60);
            //            break;
            //        case HiDisplayManager.ENC_FMT_1080i_60:
            //            displayManager.setFmt(HiDisplayManager.ENC_FMT_720P_60);
            //            break;
            //        case HiDisplayManager.ENC_FMT_720P_60:
            //            displayManager.setFmt(HiDisplayManager.ENC_FMT_576P_50);
            //            break;
            //        case HiDisplayManager.ENC_FMT_576P_50:
            //            displayManager.setFmt(HiDisplayManager.ENC_FMT_480P_60);
            //            break;
            //        case HiDisplayManager.ENC_FMT_480P_60:
            //            displayManager.setFmt(HiDisplayManager.ENC_FMT_1080P_60);
            //            break;
            //    }
            //    break;
        }

        if (isDigitKeyCode(keyCode)) {   // between 0~9
            checkHiddenInput(keyCode);
        }

        return super.onKeyDown(keyCode, event);
    }

    private void checkHiddenInput(int keyCode) {
        if (mHiddenInput.length() < 4 && isDigitKeyCode(keyCode)) {
            mHiddenInput += String.valueOf(keyCode - KeyEvent.KEYCODE_0);
        }

        switch (mHiddenInput) {
            case MtestConfig.HIDDEN_INPUT_EMI:
                Toast.makeText(this, mHiddenInput, Toast.LENGTH_SHORT).show();
                MtestConfig.switchHiddenFunctionEnable(getApplicationContext(), MtestConfig.KEY_EMI);

                mHiddenInput = "";
                MtestConfig.restart(getApplicationContext());
                break;
            case MtestConfig.HIDDEN_INPUT_STABLE_TEST_MODE: // Johnny 20190318 for stable test
                if (gv.isStableTestEnabled()) {
                    gv.setStableTestEnabled(false);
                    Toast.makeText(this, mHiddenInput + " Stable Test Mode Disabled!", Toast.LENGTH_SHORT).show();
                }
                else {
                    gv.setStableTestEnabled(true);
                    Toast.makeText(this, mHiddenInput + " Stable Test Mode Enabled!", Toast.LENGTH_SHORT).show();
                }

                mHiddenInput = "";
                break;
            default: // Johnny 20190613 add timeout to reset hidden input
                mHandler.removeCallbacks(mResetHiddenInputRunnable);
                mHandler.postDelayed(mResetHiddenInputRunnable, HIDDEN_INPUT_TIMEOUT);
                break;
        }

        if (mHiddenInput.length() >= 4)
        {
            Toast.makeText(this, "Error Hidden Input: " + mHiddenInput, Toast.LENGTH_SHORT).show();
            mHiddenInput = "";
        }
    }

    private void setOSDVisibility(boolean visible) {

        if (visible) {
            mMainLayout.setVisibility(View.VISIBLE);
        } else {
            mMainLayout.setVisibility(View.INVISIBLE);
        }
    }

    private boolean isDigitKeyCode(int keyCode) {
        return keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9;
    }
}
