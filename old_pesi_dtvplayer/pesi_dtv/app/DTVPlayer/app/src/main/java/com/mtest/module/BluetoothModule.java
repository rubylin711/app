package com.mtest.module;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.storage.StorageManager;
import android.util.Log;

import com.mtest.config.MtestConfig;
import com.mtest.utils.PesiStorageHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BluetoothModule {
    private static final String TAG = "BluetoothModule";
    private static final String DEFAULT_BT_NAME = "PTS-HOGP-1471";//"RemoteB016"; //"Mi Smart Band 4";
    private static final String DEFAULT_BT_ADDRESS = "00:1A:7D:DA:71:14";//"CD:70:89:4E:A1:9C";
    private static final String BT_FILE_NAME = "/default_bt.dat";
    private static final int DEFAULT_BT_MIN_LEVEL = -90;
    private static final int DEFAULT_BT_MAX_LEVEL = -50;

    private final WeakReference<Context> mContRef;

    private short mBtRssi = 0;
    private int mBtTestResult = MtestConfig.TEST_RESULT_FAIL;
    private int mBtDiscoveringCount = 0;
    private BluetoothAdapter mBtAdapter;
    private List<BtItem> mBtItemList = new ArrayList<>();

    public BluetoothModule(Context context){
        mContRef = new WeakReference<>(context);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void registerReceiver() {
        IntentFilter btChangeFilter = new IntentFilter();
        btChangeFilter.addAction(BluetoothDevice.ACTION_FOUND);
        btChangeFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        btChangeFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        btChangeFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        btChangeFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContRef.get().registerReceiver(mBTReceiver, btChangeFilter);
    }

    public void unregisterReceiver() {
        mContRef.get().unregisterReceiver(mBTReceiver);
    }

    private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // edwin 20200518 implement BT_TEST -s
            String action = intent.getAction();//centaur 20200706 fix bt scan too slow
            Log.d(TAG,"onReceive: intent.getAction() = "+ action);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,  Short.MIN_VALUE);
//                Log.d(TAG, "onReceive: name = " + bluetoothDevice.getName());
//                Log.d(TAG, "onReceive: address = " + bluetoothDevice.getAddress());
//                Log.d(TAG, "onReceive: RSSI = " + rssi);

                btListAdd(bluetoothDevice);
                // update result and rssi if not pass yet
                if (mBtTestResult != MtestConfig.TEST_RESULT_PASS) {
                    mBtRssi = rssi;
                    mBtTestResult = isBtPass(bluetoothDevice, rssi) ?
                            MtestConfig.TEST_RESULT_PASS : MtestConfig.TEST_RESULT_FAIL;
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG,"onReceive: BluetoothAdapter.ACTION_DISCOVERY_FINISHED");
            }
            // edwin 20200518 implement BT_TEST -e
        }
    };

    private boolean isBtPass(BluetoothDevice bluetoothDevice, short rssi) { // edwin 20200518 implement BT_TEST
        Log.d(TAG, "isBtPass: ");

        List<BtItem> defaultBtlist = getBtConfigsFromFile();
        int maxLevel, minLevel;//Scoty 20190419 modify should check file max and min level
        String name, address;
        boolean btPass = false;

        // check by config in file or default config
        if (defaultBtlist.size() > 0) {
            maxLevel = defaultBtlist.get(0).getMaxLevel();
            minLevel = defaultBtlist.get(0).getMinLevel();
            name = defaultBtlist.get(0).getName();
            address = defaultBtlist.get(0).getAddress();
        }
        else {
            maxLevel = DEFAULT_BT_MAX_LEVEL;
            minLevel = DEFAULT_BT_MIN_LEVEL;
            name = DEFAULT_BT_NAME;
            address = DEFAULT_BT_ADDRESS;
        }

        Log.d(TAG, "bluetoothDevice.getAddress() = " + bluetoothDevice.getAddress());
        Log.d(TAG, "target address = " + address);
        // check name, address, and rssi
        if (name.equals(bluetoothDevice.getName())
                && address.equalsIgnoreCase(bluetoothDevice.getAddress())) { // Johnny 20190506 check bt mac ignore case
            Log.d(TAG, "rssi = " + rssi + " max = " + maxLevel + " min = " + minLevel);
            btPass = rssi >= minLevel && rssi <= maxLevel;
            mBtAdapter.cancelDiscovery(); // target bt found, cancel discovery so module will re-discovery next time
        }

        Log.d(TAG, "isBtPass: " + (btPass ? "pass" : "fail"));
        return btPass;
    }

    private List<BtItem> getBtConfigsFromFile() {
        Log.d(TAG, "getBtConfigsFromFile: ");

        List<BtItem> btFileStr = new ArrayList<>();
        StorageManager storageManager = (StorageManager) mContRef.get().getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
        File btFile;
        // Use regular expression to get str between "=" and ",". Ignore spaces after "=" and before ","
        Pattern pattern = Pattern.compile("=\\s*(.*?)\\s*,");
        Matcher matcher;
        if (storageManager != null) {
            List<Object> volumeInfoList = pesiStorageHelper.getVolumes();
            for (Object volumeInfo : volumeInfoList) {
                String FilePath = pesiStorageHelper.getPath(volumeInfo) + BT_FILE_NAME;
                btFile = new File(FilePath);
                if(btFile.exists()) {
                    try {
                        FileReader fileRd = new FileReader(FilePath);
                        BufferedReader bufRd = new BufferedReader(fileRd);
                        String tmpBuf = bufRd.readLine(); //read one line

                        while (tmpBuf != null){
                            BtItem btItem = new BtItem();

                            //read name
                            // Johnny 20190618 Fix wrong name if name has spaces
                            matcher = pattern.matcher(tmpBuf);
                            if (matcher.find()) {
                                btItem.setName(matcher.group(1)); // get str of (.*?)
                            }
                            else {
                                btItem.setName(DEFAULT_BT_NAME);
                            }

                            //read address
                            tmpBuf = bufRd.readLine();
                            matcher = pattern.matcher(tmpBuf);
                            if (matcher.find()) {
                                btItem.setAddress(matcher.group(1)); // get str of (.*?)
                            }
                            else {
                                btItem.setAddress(DEFAULT_BT_ADDRESS);
                            }

                            //read max level
                            tmpBuf = bufRd.readLine();
                            matcher = pattern.matcher(tmpBuf);
                            if (matcher.find()) {
                                btItem.setMaxLevel(Integer.parseInt(matcher.group(1))); // get str of (.*?)
                            }
                            else {
                                btItem.setMaxLevel(DEFAULT_BT_MAX_LEVEL);
                            }

                            //read min level
                            tmpBuf = bufRd.readLine();
                            matcher = pattern.matcher(tmpBuf);
                            if (matcher.find()) {
                                btItem.setMinLevel(Integer.parseInt(matcher.group(1))); // get str of (.*?)
                            }
                            else {
                                btItem.setMinLevel(DEFAULT_BT_MIN_LEVEL);
                            }

                            btFileStr.add(btItem);//save in List

                            //read next Bt data
                            tmpBuf = bufRd.readLine();
                            break; // only read first in file for now

                        }
                        fileRd.close();
                        bufRd.close();

                    }catch(Exception e) {
                        Log.d(TAG, "getBtConfigsFromFile: Exception");
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        return btFileStr;
    }

    private void resetTest() {
        mBtTestResult = MtestConfig.TEST_RESULT_FAIL;
        mBtDiscoveringCount = 0;
    }

    public void btListAdd (BluetoothDevice bluetoothDevice) { // Edwin 20200602 for long term test
        Log.d(TAG, "btListAdd: ");
        String name = bluetoothDevice.getName();
        String address = bluetoothDevice.getAddress();
        boolean exist = false;

        for (BtItem item : mBtItemList) {
            if (name != null && name.equals(item.getName())
                    && address != null && address.equalsIgnoreCase(item.getAddress())) { //centaur 20200619 fix mtest
                exist = true;
                break;
            }
        }

        if (!exist) {
            BtItem item = new BtItem(name, address);
            mBtItemList.add(item);
        }
    }

    public int getBtRssi() {
        return mBtRssi;
    }

    public List<BtItem> getBtItemList() {
        return mBtItemList;
    }

    public String getAddress() {
        String address = "";
        if (mBtAdapter != null) {
            address = mBtAdapter.getAddress();
        }
        return address;
    }

    public void setBtItemListEmpty() {
        if (mBtItemList == null) {
            mBtItemList = new ArrayList<>();
        }
        else {
            mBtItemList.clear();
        }
    }

    public boolean isBtEnabled() {
        return mBtAdapter != null && mBtAdapter.isEnabled();
    }

    public boolean isBtDiscovering() {
        return mBtAdapter != null && mBtAdapter.isDiscovering();
    }

    public boolean startDiscovery() {
        return mBtAdapter != null && mBtAdapter.startDiscovery();
    }

    public boolean cancelDiscovery() {
        return mBtAdapter != null && mBtAdapter.cancelDiscovery();
    }

    public int checkBt() {
        if (mBtAdapter == null) {
            Log.d(TAG, "checkBt: null BT adapter");
            return MtestConfig.TEST_RESULT_FAIL;
        }

        int result = mBtTestResult;
        Log.d(TAG, "checkBt: test result = " + result);

        // reset test or restart discovery if needed
        if (result == MtestConfig.TEST_RESULT_PASS) {
            Log.d(TAG, "checkBt: test pass, reset test");
            resetTest();
        }
        else {
            if (!mBtAdapter.isDiscovering()) {
                Log.d(TAG, "checkBt: test fail and not discovering, start discovery");
                resetTest();
                mBtAdapter.startDiscovery();
            }
            else if (mBtDiscoveringCount <= 0 || mBtDiscoveringCount >= 5) {
                Log.d(TAG, "checkBt: test fail and reach discovery count limit, restart discovery");
                mBtAdapter.cancelDiscovery();
                resetTest();
                mBtAdapter.startDiscovery();
            }
        }

        mBtDiscoveringCount++;
        return result;
    }

    public static class BtItem {
        String name;
        String address;
        short rssi;
        int maxLevel;
        int minLevel;

        BtItem () {
            name = "";
            address = "";
            maxLevel = DEFAULT_BT_MAX_LEVEL;
            minLevel = DEFAULT_BT_MIN_LEVEL;
            rssi = 0;
        }

        BtItem(String name, String address) {
            this.name = name;
            this.address = address;
            maxLevel = DEFAULT_BT_MAX_LEVEL;
            minLevel = DEFAULT_BT_MIN_LEVEL;
        }

        private void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        private void setAddress(String address) {
            this.address = address;
        }

        public String getAddress() {
            return address;
        }
        private void setMaxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        private void setMinLevel(int minLevel) {
            this.minLevel = minLevel;
        }

        public int getMinLevel() {
            return minLevel;
        }
    }
}
