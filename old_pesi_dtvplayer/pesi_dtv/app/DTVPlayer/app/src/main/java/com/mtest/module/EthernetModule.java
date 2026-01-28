package com.mtest.module;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.mtest.activity.MainActivity;
import com.mtest.config.HwTestConfig;
import com.mtest.config.MtestConfig;
import com.prime.dtvplayer.Activity.DTVActivity;

import java.io.IOException;

import static android.net.ConnectivityManager.TYPE_ETHERNET;

public class EthernetModule {
    private static final String TAG = "EthernetModule";
    private Context mContext;

    public EthernetModule(Context context) {
        mContext = context;
    }

    /*public int checkEthPass(ConnectivityManager cm){

        Log.d(TAG, "checkEthPass: ");
        Network[] networks = new Network[0];
        boolean ethPass = false;

        if (cm != null){
            networks = cm.getAllNetworks();
        }

        for (Network network : networks){
//            NetworkInfo netInfo = cm.getNetworkInfo(network);
//            ethPass = netInfo != null && netInfo.getType() == TYPE_ETHERNET;
            NetworkCapabilities netCap = cm.getNetworkCapabilities(network);
            ethPass = netCap != null && netCap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
        }

        if (ethPass)
            return MtestConfig.TEST_RESULT_PASS;
        else
            return MtestConfig.TEST_RESULT_FAIL;
    }*/

    public int checkEthConnect() {

        Log.d(TAG, "checkEthConnect: ");
        DTVActivity dtvActivity = ((DTVActivity)mContext);
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = cm.getAllNetworks();
        NetworkInfo netInfo;
        HwTestConfig hwTestConfig = HwTestConfig.getInstance(mContext, 1);

        boolean connected = false;

        for (Network network : networks) {
            netInfo = cm.getNetworkInfo(network);
            boolean isConnected = ((netInfo != null) && netInfo.isConnected() && (netInfo.getType() == TYPE_ETHERNET));
            if (isConnected) {
                Runtime runtime = Runtime.getRuntime();
                try {

                    Process mIpAddrProcess = runtime.exec("/system/bin/ping -c1 -n -W3 " + hwTestConfig.getDEST_IP()); // Edwin 20200511 fix ping fail
                    int mExitValue = mIpAddrProcess.waitFor();
                    if (mExitValue == 0) {
                        connected = true;
                    } else {
                        connected = false;
                    }

                    /*final boolean finalConnect = connected;
                    dtvActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (finalConnect)
                                Toast.makeText(mContext, "Ping : " + MainActivity.PING_IP + ", success", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(mContext, "Ping : " + MainActivity.PING_IP + ", fail", Toast.LENGTH_SHORT).show();
                        }
                    });*/
                } catch (InterruptedException | IOException ignore) {
                    connected = false;
                    ignore.printStackTrace();
                }
            }
        }
        if (connected)
            return MtestConfig.TEST_RESULT_PASS;
        else
            return MtestConfig.TEST_RESULT_FAIL;
    }
}
