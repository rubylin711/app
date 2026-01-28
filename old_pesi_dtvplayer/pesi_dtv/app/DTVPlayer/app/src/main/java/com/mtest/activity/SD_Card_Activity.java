package com.mtest.activity;

import android.content.Context;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.mtest.config.HwTestConfig;
import com.mtest.config.MtestConfig;
import com.mtest.module.PesiSharedPreference;
import com.mtest.module.SDcardModule;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;

public class SD_Card_Activity extends DTVActivity {
    private static final String TAG = "SD_Card_Activity";
    boolean mSdCardPass = false;
    private Global_Variables mGlobalVars;
    private Handler mHandler = new Handler();
    SDcardModule mSdCardModule;

    private Runnable mCheckSDRunnable = new Runnable() {
        @Override
        public void run() {
            checkSDCard();
            mHandler.postDelayed(mCheckSDRunnable, 2000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sd_card);
        mGlobalVars = (Global_Variables) getApplicationContext();
        mSdCardModule = new SDcardModule(this); // edwin 20201216 fix wrong return value
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.post(mCheckSDRunnable);
    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacks(mCheckSDRunnable);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // edwin 20200515 implement SD_TEST flag function -s
        /* USB_TEST
        0:off
        1:R/W one time,Production mode
        2:Continuous R/W */
        boolean mountMode = HwTestConfig.getInstance(this, 0).getUSB_TEST() == 0;
        if (!mountMode)
            return;
        // edwin 20200515 implement SD_TEST flag function -e

        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);

        if (mSdCardPass) {
            pesiSharedPreference.putInt(getString(R.string.str_test_item_sdcard), MtestConfig.TEST_RESULT_PASS);
        }
        else {
            pesiSharedPreference.putInt(getString(R.string.str_test_item_sdcard), MtestConfig.TEST_RESULT_FAIL);
        }

        pesiSharedPreference.save();
    }

//    @Override
//    public void onMessage(TVMessage tvMessage) {
//        super.onMessage(tvMessage);
//
//        switch (tvMessage.getMsgType()) {
//            case TVMessage.TYPE_PIO_USB_OVERLOAD:
//                overloadUSB(tvMessage.GetPioUsbOverloadPort());
//                break;
//            default:
//                break;
//        }
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
                if (mGlobalVars.isStableTestEnabled()) {
                    if (MtestConfig.isHiddenFunctionEnable(getApplicationContext(), MtestConfig.KEY_USB_STABLE_TEST)) {
                        Toast.makeText(this, "USB Stable Test Stop!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(this, "USB Stable Test Start!", Toast.LENGTH_SHORT).show();
                    }

                    MtestConfig.switchHiddenFunctionEnable(getApplicationContext(), MtestConfig.KEY_USB_STABLE_TEST);
                }
                break;
            default:
                break;

        }
        return super.onKeyDown(keyCode, event);
    }

    private void checkSDCard () // edwin 20201216 fix wrong return value
    {
        TextView txvResult_1 = findViewById(R.id.tv_item29_sd_1_result);
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        int result;
        mSdCardPass = false;

        if (storageManager != null)
        {
            result = mSdCardModule.checkStatus();

            if (result == MtestConfig.TEST_RESULT_PASS)
            {
                Log.d(TAG, "checkSDCard: pass");
                mSdCardPass = true;
                txvResult_1.setText(R.string.str_pass);
                txvResult_1.setBackgroundResource(R.drawable.shape_rectangle_pass);
            }
            else if (result == MtestConfig.TEST_RESULT_WAIT_CARD_OUT)
            {
                Log.d(TAG, "checkSDCard: wait card out");
                txvResult_1.setText(R.string.str_wait_card_out);
                txvResult_1.setBackgroundResource(R.drawable.shape_rectangle_focus);
            }
            else
            {
                Log.d(TAG, "checkSDCard: fail");
                txvResult_1.setText(R.string.str_fail);
                txvResult_1.setBackgroundResource(R.drawable.shape_rectangle_fail);
            }
        }
    }

//    @SuppressLint("ShowToast")
//    private void overloadUSB(int port) {
//        List<Integer> pesiUsbPortList = GetUsbPortList();
//        if (pesiUsbPortList == null) {
//            pesiUsbPortList = new ArrayList<>();
//        }
//
//        int index = pesiUsbPortList.indexOf(port);
//        Toast tUSBError;
//        if (index >= 0) {
//            tUSBError = Toast.makeText(
//                    this,
//                    String.format(Locale.getDefault(), "Warning!\nUSB%d overload......", ++index),
//                    Toast.LENGTH_SHORT);
//        }
//        else {
//            tUSBError = Toast.makeText(this,
//                    "Warning!\nUSB overload......",
//                    Toast.LENGTH_SHORT);
//        }
//
//        tUSBError.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//        tUSBError.show();
//    }
}
