package com.mtest.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.dolphin.dtv.HiDtvMediaPlayer;
import com.mtest.config.HwTestConfig;
import com.mtest.config.MtestConfig;
import com.mtest.module.PesiSharedPreference;
import com.mtest.module.UsbModule;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.utils.TVMessage;

import java.util.List;
import java.util.Locale;

public class USB_Activity extends DTVActivity {
    private static final String TAG = "USB_Activity";
    private UsbModule mUsbModule;
    boolean mUsb1Pass = false;
    boolean mUsb2Pass = false;

    private Global_Variables mGlobalVars;


    private Handler mHandler = new Handler();
    private Runnable mCheckUSBRunnable = new Runnable() {
        @Override
        public void run() {
            checkUSB();
            mHandler.postDelayed(mCheckUSBRunnable, 2000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);

        mGlobalVars = (Global_Variables) getApplicationContext();
        mUsbModule = new UsbModule(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.post(mCheckUSBRunnable);
    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacks(mCheckUSBRunnable);
        mUsbModule = null;
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // edwin 20200514 implement USB_TEST flag function -s
        /* USB_TEST
        0:off
        1:R/W one time,Production mode
        2:Continuous R/W */
        boolean mountMode = HwTestConfig.getInstance(this, 0).getUSB_TEST() == 0;
        if (!mountMode)
            return;
        // edwin 20200514 implement USB_TEST flag function -e

        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);

        if (mUsb1Pass && mUsb2Pass) {
            pesiSharedPreference.putInt(getString(R.string.str_test_item_usb), MtestConfig.TEST_RESULT_PASS);
        }
        else {
            pesiSharedPreference.putInt(getString(R.string.str_test_item_usb), MtestConfig.TEST_RESULT_FAIL);
        }

        pesiSharedPreference.save();
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);

        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_PIO_USB_OVERLOAD:
                overloadUSB(tvMessage.GetPioUsbOverloadPort());
                break;
            default:
                break;
        }
    }

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

    private void checkUSB()
    {
        TextView tv17_usb1 = (TextView) findViewById(R.id.tv_item17_usb_1_result);
        TextView tv17_usb2 = (TextView) findViewById(R.id.tv_item17_usb_2_result);
        PesiSharedPreference pref = new PesiSharedPreference(this);
        int resultUsb1;
        int resultUsb2;

        mUsb1Pass = (pref.getInt(getString(R.string.str_test_item_usb1), MtestConfig.TEST_RESULT_NONE) == MtestConfig.TEST_RESULT_PASS);
        mUsb2Pass = (pref.getInt(getString(R.string.str_test_item_usb2), MtestConfig.TEST_RESULT_NONE) == MtestConfig.TEST_RESULT_PASS);
        resultUsb1 = mUsbModule.checkUsbMounted(UsbModule.PORT_1);
        resultUsb2 = mUsbModule.checkUsbMounted(UsbModule.PORT_2);

        if (resultUsb1 == MtestConfig.TEST_RESULT_PASS)
            mUsb1Pass = true;
        if (resultUsb2 == MtestConfig.TEST_RESULT_PASS)
            mUsb2Pass = true;

        if (mUsb1Pass) {
            tv17_usb1.setText(R.string.str_pass);
            tv17_usb1.setBackgroundResource(R.drawable.shape_rectangle_pass);
        }
        else {
            tv17_usb1.setText(R.string.str_fail);
            tv17_usb1.setBackgroundResource(R.drawable.shape_rectangle_fail);
        }

        if (mUsb2Pass) {
            tv17_usb2.setText(R.string.str_pass);
            tv17_usb2.setBackgroundResource(R.drawable.shape_rectangle_pass);
        }
        else {
            tv17_usb2.setText(R.string.str_fail);
            tv17_usb2.setBackgroundResource(R.drawable.shape_rectangle_fail);
        }
    }

    @SuppressLint("ShowToast")
    private void overloadUSB(int port) {
//        List<Integer> pesiUsbPortList = GetUsbPortList();
//        if (pesiUsbPortList == null) {
//            pesiUsbPortList = new ArrayList<>();
//        }
        List<Integer> pesiUsbPortList = mUsbModule.getPortList();

        int index = pesiUsbPortList.indexOf(port);
        Toast tUSBError;
        if (index >= 0) {
            tUSBError = Toast.makeText(
                    this,
                    String.format(Locale.getDefault(), "Warning!\nUSB%d overload......", ++index),
                    Toast.LENGTH_SHORT);
        }
        else {
            tUSBError = Toast.makeText(this,
                    "Warning!\nUSB overload......",
                    Toast.LENGTH_SHORT);
        }

        tUSBError.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        tUSBError.show();
    }
}
