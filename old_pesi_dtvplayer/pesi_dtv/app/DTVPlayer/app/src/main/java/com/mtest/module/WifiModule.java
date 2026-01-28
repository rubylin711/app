package com.mtest.module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.storage.StorageManager;
import android.util.Log;

import com.dolphin.dtv.HiDtvMediaPlayer;
import com.mtest.config.MtestConfig;
import com.mtest.utils.PesiStorageHelper;
import com.prime.dtvplayer.Activity.DTVActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.WIFI_SERVICE;

public class WifiModule
{
    private static final String TAG = WifiModule.class.getSimpleName();
    private static final int CMD_SUCCESS = HiDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS;
    private static final int CMD_FAIL = HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    private static final int RESULT_PASS = MtestConfig.TEST_RESULT_PASS;
    private static final int RESULT_FAIL = MtestConfig.TEST_RESULT_FAIL;
    private final WeakReference<Context> mContRef;

    public static final String WIFI_FILE_NAME = "/default_wifi.dat";
    public static final String DEFAILT_WIFI_SSID = "wifitest";
    public static final int DEFAULT_WIFI_MIN_LEVEL = -90;
    public static final int DEFAULT_WIFI_MAX_LEVEL = -50;
    private static BroadcastReceiver mWifiScanReceiver = null;
    private static List<ScanResult> mWifiList = null;

    public interface OnWifiScanListener
    {
        void scanSuccess(WifiManager wifiManager);
        void scanFailure(WifiManager wifiManager);
    }

    public static class WifiItem
    {
        String SSID;
        int MAX_LEVEL;
        int MIN_LEVEL;

        private void SetSSID(String ssid)
        {
            this.SSID = ssid;
        }

        private String GetSSID()
        {
            return this.SSID;
        }

        private void SetMaxLevel(int maxLevel)
        {
            this.MAX_LEVEL = maxLevel;
        }

        private int GetMaxLevel()
        {
            return this.MAX_LEVEL;
        }

        private void SetMinLevel(int minLevel)
        {
            this.MIN_LEVEL = minLevel;
        }

        private int GetMinLevel()
        {
            return this.MIN_LEVEL;
        }
    }

    public WifiModule(Context context)
    {
        mContRef = new WeakReference<>(context);
    }

    public static void destroy(Context context)
    {
        if (mWifiScanReceiver != null)
        {
            context.unregisterReceiver(mWifiScanReceiver);
            mWifiScanReceiver = null;
        }
        mWifiList = null;
        Log.d(TAG, "destroy: mWifiScanReceiver = " + mWifiScanReceiver);
        Log.d(TAG, "destroy: mWifiList = " + mWifiList);
    }

    private static int getReturn(int ret)
    {
        return (ret == CMD_SUCCESS) ?
                RESULT_PASS :
                RESULT_FAIL ;
    }

    private static int getReturn(boolean success)
    {
        return success ?
                RESULT_PASS :
                RESULT_FAIL ;
    }

    public List<ScanResult> getWifiList()
    {
        Log.d(TAG, "getWifiList: mWifiList = " + mWifiList);
        return mWifiList;
    }

    public void startWifiScan() //Scoty 20190417 need to connect wifi and the get each antenna level //Scoty 20190410 add bt and wifi auto test
    {
        DTVActivity activity = (DTVActivity) mContRef.get();
        WifiManager wifiManager = (WifiManager) activity.getSystemService(WIFI_SERVICE);

        if (mWifiScanReceiver == null) // registerReceiver
        {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            mWifiScanReceiver = new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
                    boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                    if (success)
                        scanSuccess(wifiManager);
                    else // scan failure handling
                        scanFailure(wifiManager);
                }
            };
            activity.registerReceiver(mWifiScanReceiver, intentFilter);
        }

        Log.d(TAG, "startWifiScan: isScanAlwaysAvailable = " + wifiManager.isScanAlwaysAvailable());
        boolean success = wifiManager.startScan();
        if (!success) // scan failure handling
            scanFailure(wifiManager);
    }

    private static void scanSuccess(WifiManager wifiManager)
    {
        mWifiList = wifiManager.getScanResults(); // Edwin 20200602 for long term test
        for (ScanResult result : mWifiList)
        {
            Log.d(TAG,"scanSuccess: SSID = " + result.SSID);
        }
    }

    private static void scanFailure(WifiManager wifiManager)
    {
        Log.d(TAG, "scanFailure: ");
        // scan failure handling
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        //List<ScanResult> results = wifiManager.getScanResults();
    }

    public boolean isConnected()
    {
        DTVActivity activity = (DTVActivity) mContRef.get();
        WifiManager wifiMgr = (WifiManager) activity.getSystemService(WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) // Wi-Fi adapter is ON
        {
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            String ssid = wifiInfo.getSSID();

            if (wifiInfo.getNetworkId() == -1)
            {
                Log.d(TAG, "isConnected: Not connected to an access point");
                return false; // Not connected to an access point
            }

            Log.d(TAG, "isConnected: Connected to an access point");
            return true; // Connected to an access point
        }
        else
        {
            Log.d(TAG, "isConnected: Wi-Fi adapter is OFF");
            return false; // Wi-Fi adapter is OFF
        }
    }

    public void removeNetwork()
    {
        Log.d(TAG, "removeNetwork: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        WifiManager wifiManager = (WifiManager) activity.getSystemService(WIFI_SERVICE);
        // Johnny 20190528 remove all wifi when pass to prevent connect pctool fail
        List<WifiConfiguration> configList = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configList)
        {
            wifiManager.removeNetwork(config.networkId);
        }
    }

    public int checkWifi(StringBuilder wifiLevel) //Scoty 20190417 need to connect wifi and the get each antenna level //Scoty 20190410 add bt and wifi auto test
    {
        DTVActivity activity = (DTVActivity) mContRef.get();
        boolean wifiPass = false;
        int ConnectNetId = 0;
        WifiManager wifiManager = (WifiManager) activity.getSystemService(WIFI_SERVICE);

        if(wifiManager != null)
        {
            if (!wifiManager.isWifiEnabled())
                wifiManager.setWifiEnabled(true);

            List<WifiItem> wifiFileList = checkWifiFile();
            WifiConfiguration WifiConfig = new WifiConfiguration();
            String WifiSSID = DEFAILT_WIFI_SSID;
            List<Integer> list;
            int maxLevel, minLevel;//Scoty 20190419 modify should check file max and min level

            //Scoty 20190419 modify should check file max and min level -s
            if (wifiFileList.size() > 0)
            {
                WifiSSID = wifiFileList.get(0).GetSSID();
                maxLevel = wifiFileList.get(0).GetMaxLevel();
                minLevel = wifiFileList.get(0).GetMinLevel();
            }
            else
            {
                maxLevel = DEFAULT_WIFI_MAX_LEVEL;
                minLevel = DEFAULT_WIFI_MIN_LEVEL;
            }
            Log.d(TAG, "checkWifi: Default_Wifi_Max_Level = " + maxLevel  );
            //Scoty 20190419 modify should check file max and min level -e

            if(wifiManager.getConnectionInfo().getNetworkId() == -1) //not connect wifi
            {
                ConnectNetId = wifiManager.addNetwork(createWifiConfig(WifiSSID));
                Log.d(TAG, "checkWifi: netId = [" + WifiSSID + "] WifiSSID = [" + WifiSSID + "]" + " SSID = [" + WifiConfig.SSID + "]");
                wifiManager.enableNetwork(ConnectNetId, true);
            }

            //Set wifi level value
            list = getWiFiTxRxLevel();
            if (list.size() > 0)//centaur 20200706 fix bt scan too slow
            {
                Log.d(TAG, "checkWifi: wifi list= " + list);
                String level = "RF0: " + list.get(1) + " RF1: " + list.get(0);
                if (wifiLevel != null)
                    wifiLevel.append(level);

                //Scoty 20190419 modify should check file max and min level -s//Scoty 20190417 fixed check RF0 and RF1 level and then set pass/fail
                if (list.get(0) >= minLevel && list.get(0) <= maxLevel
                        && list.get(1) >= minLevel && list.get(1) <= maxLevel)
                {
                    wifiPass = true;
                }
            }
            //Scoty 20190419 modify should check file max and min level -e
        }

        Log.d(TAG, "checkWifi: wifiPass = " + wifiPass);
        if (wifiPass)
            return MtestConfig.TEST_RESULT_PASS;
        else
            return MtestConfig.TEST_RESULT_FAIL;
    }

    public int checkWifiByLongTerm() //centaur 20200619 fix mtest
    {
        DTVActivity activity = (DTVActivity) mContRef.get();
        WifiManager wifiManager = (WifiManager) activity.getSystemService(WIFI_SERVICE);
        boolean wifiPass = false;

        Log.d(TAG,"checkWifiByLongTerm: WifiManager size = "+ wifiManager.getScanResults().size());
        if(wifiManager.getScanResults().size() != 0)
            wifiPass = true;

        return getReturn(wifiPass);
    }

    public List<Integer> getWiFiTxRxLevel()
    {
        Log.d(TAG, "getWiFiTxRxLevel: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        return activity.MtestGetWiFiTxRxLevel();
    }

    private List<WifiItem> checkWifiFile() //Scoty 20190410 add bt and wifi auto test
    {
        Log.d(TAG, "checkWifiFile: ");
        DTVActivity activity = (DTVActivity) mContRef.get();
        List<WifiItem> WifiFileStr = new ArrayList<>();
        WifiFileStr.clear();
        File WifiFile;
        Pattern pattern = Pattern.compile("=\\s*(.*?)\\s*,"); // Use regular expression to get str between "=" and ",". Ignore spaces after "=" and before ","
        Matcher matcher;

        StorageManager storageManager = (StorageManager)mContRef.get().getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
        for (Object volumeInfo : activity.getVolumes())
        {
            String FilePath = pesiStorageHelper.getPath(volumeInfo) + WIFI_FILE_NAME;
            WifiFile = new File(FilePath);

            if(WifiFile.exists())
            {
                try {
                    FileReader fileRd = new FileReader(FilePath);
                    BufferedReader bufRd = new BufferedReader(fileRd);
                    String tmpBuf = bufRd.readLine(); //read one line

                    while (tmpBuf != null)
                    {
                        WifiItem wifiItem = new WifiItem();

                        //read SSID
                        /*String[] tmpString = tmpBuf.split("=");
                        String[] tmpString2 = tmpString[1].split(",");
                        tmpString2 = tmpString2[0].split(" ");*/

                        // Johnny 20190618 Fix wrong SSID if SSID has spaces
                        matcher = pattern.matcher(tmpBuf);
                        if (matcher.find())
                            wifiItem.SetSSID(matcher.group(1)); // get str of (.*?)
                        else
                            wifiItem.SetSSID(DEFAILT_WIFI_SSID);

                        //read max level
                        tmpBuf = bufRd.readLine();
                        matcher = pattern.matcher(tmpBuf);
                        if (matcher.find())
                            wifiItem.SetMaxLevel(Integer.parseInt(matcher.group(1))); // get str of (.*?)
                        else
                            wifiItem.SetMaxLevel(DEFAULT_WIFI_MAX_LEVEL);

                        //read min level
                        tmpBuf = bufRd.readLine();
                        matcher = pattern.matcher(tmpBuf);
                        if (matcher.find())
                            wifiItem.SetMinLevel(Integer.parseInt(matcher.group(1))); // get str of (.*?)
                        else
                            wifiItem.SetMinLevel(DEFAULT_WIFI_MIN_LEVEL);

                        WifiFileStr.add(wifiItem);//save in List

                        //read next Wifi data
                        tmpBuf=bufRd.readLine();
                        break;
                    }
                    fileRd.close();
                    bufRd.close();

                } catch(Exception e) {
                    Log.d(TAG, "checkWifiFile: Exception");
                    e.printStackTrace();
                }
                break;
            }
        }

        return WifiFileStr;
    }

    public WifiConfiguration createWifiConfig(String ssid) //Scoty 20190417 need to connect wifi and the get each antenna level //Scoty 20190410 add bt and wifi auto test
    {
        Log.d(TAG, "createWifiConfig: ");
        //WifiConfiguration
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        //SSID
        config.SSID = "\"" +  ssid  + "\"";
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        return config;
    }

    /*private WifiConfiguration createWifiInfo(String SSID, String Password, int Type)
    {
        DTVActivity activity = (DTVActivity) mContRef.get();
        WifiManager wifiManager = (WifiManager) activity.getSystemService(WIFI_SERVICE);
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.allowedAuthAlgorithms.clear();
        configuration.allowedGroupCiphers.clear();
        configuration.allowedKeyManagement.clear();
        configuration.allowedPairwiseCiphers.clear();
        configuration.allowedProtocols.clear();
        configuration.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = isExsits(SSID);
        if (tempConfig != null)
        {
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
    }*/

    /*private WifiConfiguration isExsits(String SSID)
    {
        DTVActivity activity = (DTVActivity) mContRef.get();
        WifiManager wifiManager = (WifiManager) activity.getSystemService(WIFI_SERVICE);
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }*/
}
