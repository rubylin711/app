package com.mtest.activity;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.mtest.module.PesiSharedPreference;
import com.mtest.module.WifiModule;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

import java.net.Inet4Address;

import static com.mtest.config.MtestConfig.TEST_RESULT_FAIL;
import static com.mtest.config.MtestConfig.TEST_RESULT_PASS;

public class WiFi_Activity extends DTVActivity {
    WifiManager wifiManager;
    private boolean mWifiAvailable = false;
    private WifiModule mWifiModule = new WifiModule(this);

    private Handler mHandler = new Handler();
    private Runnable mCheckWifiRunnable = new Runnable() {
        @Override
        public void run() {
            m_WiFiChk();
            mHandler.postDelayed(mCheckWifiRunnable, 2000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        try {
            wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            System.out.println("wifiManager.getWifiState()");
            System.out.println(wifiManager.getWifiState());
            if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                openWifi();
                while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "get wifi state failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "open wifi failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        mHandler.post(mCheckWifiRunnable);
    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacks(mCheckWifiRunnable);
        mWifiModule.removeNetwork(); // edwin 20201203 add Wifi module
        mWifiModule = null;
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);

        if (mWifiAvailable) // edwin 20201203 add Wifi module
            pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_wifi), TEST_RESULT_PASS);
        else
            pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_wifi), TEST_RESULT_FAIL);

        pesiSharedPreference.save();
        super.onBackPressed();
    }

    // edwin 20201203 add Wifi module -s
    public void m_WiFiChk()
    {
        TextView tv26_wifi_result = (TextView) findViewById(R.id.tv_item20_wifi);

        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
        {
            boolean wifiPass = mWifiModule.isConnected();
            if (!wifiPass)
            {
                WifiConfiguration wifiConfig = null;
                //wifiConfig = createWifiInfo("obama1111", "H034557717", 3);
                wifiConfig = mWifiModule.createWifiConfig(WifiModule.DEFAILT_WIFI_SSID);//createWifiInfo(WifiModule.DEFAILT_WIFI_SSID, "", 1);
                if (wifiConfig != null)
                {
                    int networkId = wifiManager.addNetwork(wifiConfig);
                    boolean enabled = wifiManager.enableNetwork(networkId, true);
                }
            }

            wifiPass = mWifiModule.isConnected();
            if (wifiPass)
            {
                //int networkId = wifiManager.addNetwork(wifiConfig);
                //boolean enabled = wifiManager.enableNetwork(networkId, true);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                String ip = ipIntToString(ipAddress);
                tv26_wifi_result.setText(getWifiIPAddress());
                tv26_wifi_result.setBackgroundResource(R.drawable.shape_rectangle_pass);
                mWifiAvailable = true;
                System.out.println(getWifiIPAddress().toString());
            }
            else
            {
                tv26_wifi_result.setText(R.string.str_fail);
                tv26_wifi_result.setBackgroundResource(R.drawable.shape_rectangle_fail);
                mWifiAvailable = false;
            }
        }
        else
        {
            tv26_wifi_result.setText(R.string.str_fail);
            tv26_wifi_result.setBackgroundResource(R.drawable.shape_rectangle_fail);
            mWifiAvailable = false;
        }
    }
    // edwin 20201203 add Wifi module -e

    public String getWifiIPAddress() {
        WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return ipIntToString(ip);
    }

    private String ipIntToString(int ip) {
        try {
            byte[] bytes = new byte[4];
            bytes[0] = (byte) (0xff & ip);
            bytes[1] = (byte) ((0xff00 & ip) >> 8);
            bytes[2] = (byte) ((0xff0000 & ip) >> 16);
            bytes[3] = (byte) ((0xff000000 & ip) >> 24);
            return Inet4Address.getByAddress(bytes).getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean openWifi() {
        boolean bRet = true;
        if (!wifiManager.isWifiEnabled()) {
            bRet = wifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    /* // edwin 20201203 add Wifi module
    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    public WifiConfiguration createWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.allowedAuthAlgorithms.clear();
        configuration.allowedGroupCiphers.clear();
        configuration.allowedKeyManagement.clear();
        configuration.allowedPairwiseCiphers.clear();
        configuration.allowedProtocols.clear();
        configuration.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = isExsits(SSID);
        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }

        switch (Type) {
            case 1:
                //configuration.wepKeys[0] = "";
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                //configuration.wepTxKeyIndex = 0;
                //configuration.priority = 20000;
                break;
            case 2://wep?��?
                configuration.hiddenSSID = true;
                configuration.wepKeys[0] = "\"" + Password + "\"";
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                break;
            case 3: //wpa?��?

                configuration.preSharedKey = "\"" + Password + "\"";
                configuration.hiddenSSID = true;
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                configuration.status = WifiConfiguration.Status.ENABLED;
                break;
        }
        return configuration;
    }

    private boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            String ssid = wifiInfo.getSSID();

            if (wifiInfo.getNetworkId() == -1) {
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        } else {
            return false; // Wi-Fi adapter is OFF
        }
    }*/
}
