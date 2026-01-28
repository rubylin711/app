package com.mtest.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mtest.config.MtestConfig;
import com.mtest.module.EthernetModule;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;

import java.io.IOException;
import java.util.Locale;

import static android.net.ConnectivityManager.TYPE_ETHERNET;

public class Ethernet_Activity extends DTVActivity {
    private static final String TAG = "Ethernet_Activity";

    private TextView mTvFailCount;
    private int mFailCount;

    private Global_Variables mGlobalVars;

    private Handler mHandler = new Handler();
    private Runnable meth_runnable;
    private Runnable mStableTestRunnable = new Runnable() {
        @Override
        public void run() {
            checkEth();

            mTvFailCount.setText(String.format(Locale.getDefault(), "Fail : %d", mFailCount));
            mHandler.postDelayed(mStableTestRunnable, 3000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ethernet);

        mGlobalVars = (Global_Variables) getApplicationContext();
        mTvFailCount = (TextView) findViewById(R.id.tv_fail_count);
    }

    @Override
    protected void onStart() {
        super.onStart();
        meth_thread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacks(meth_runnable);
        mHandler.removeCallbacks(mStableTestRunnable);
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
                if (mGlobalVars.isStableTestEnabled()) {
                    Toast.makeText(this, "Stable Test Start!", Toast.LENGTH_SHORT).show();
                    mFailCount = 0;
                    mTvFailCount.setVisibility(View.VISIBLE);

                    mHandler.removeCallbacks(meth_runnable);    // remove normal check eth runnable
                    mHandler.removeCallbacks(mStableTestRunnable);
                    mHandler.post(mStableTestRunnable);
                }
                break;
            default:
                break;

        }
        return super.onKeyDown(keyCode, event);
    }

    private void meth_thread() {
        meth_runnable = new Runnable() {
            public void run() {
                if ( !checkEth() )
                    mHandler.postDelayed(meth_runnable, 3000);
            }
        };
        mHandler.postDelayed(meth_runnable, 100);
    }

    private boolean checkEth() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

//        Network[] networks = cm.getAllNetworks();
//        NetworkInfo netInfo;
//        boolean connected = false;
        int result;
        EthernetModule ethernetModule = new EthernetModule(this);
        TextView tvIP = (TextView) findViewById(R.id.tv_item18_eth);
        result = ethernetModule.checkEthConnect();
//        for (Network network : networks) {
//            netInfo = cm.getNetworkInfo(network);
//            boolean isConnected = ((netInfo != null) && netInfo.isConnected() && (netInfo.getType() == TYPE_ETHERNET));
//            if (isConnected) {
//                Runtime runtime = Runtime.getRuntime();
//                try {
//                    Process mIpAddrProcess = runtime.exec("/system/bin/ping -c1 -n -W3 " + MainActivity.PING_IP); // Edwin 20200511 fix ping fail
//                    int mExitValue = mIpAddrProcess.waitFor();
//                    if (mExitValue == 0) {
//                        Toast.makeText(Ethernet_Activity.this, "ping ok", Toast.LENGTH_SHORT).show();
//                        connected = true;
//                    } else {
//                        Toast.makeText(Ethernet_Activity.this, "ping fail", Toast.LENGTH_SHORT).show();
//                        connected = false;
//                    }
//                } catch (InterruptedException | IOException ignore) {
//                    connected = false;
//                    ignore.printStackTrace();
//                }
//            }
//        }

        if (result == MtestConfig.TEST_RESULT_PASS) {
            Log.d(TAG, "checkEth: pass");
            tvIP.setBackgroundResource(R.drawable.shape_rectangle_pass);
            return true;
        }
        else {
            Log.d(TAG, "checkEth: fail");
            tvIP.setBackgroundResource(R.drawable.shape_rectangle_fail);
            mFailCount++;
            return false;
        }
    }
}



