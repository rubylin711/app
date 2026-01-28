package com.prime.homeplus.settings.system;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
//import android.net.EthernetManager;
//import android.net.IpConfiguration;
//import android.net.IpConfiguration.IpAssignment;
//import android.net.IpConfiguration.ProxySettings;
//import android.net.NetworkUtils;
//import android.net.StaticIpConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;

import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NetworkView extends ThirdLevelView {
    private String TAG = "HomePlus-NetworkView";

    private String mSelectWifiSSID;


    public NetworkView(int i, Context context, SettingsRecyclerView secondDepthView) {
        super(i, context, secondDepthView);
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_network;
    }

    private Button btn1, btn2;

    public void onFocus() {
        checkEnabale();
        btn1.requestFocus();
    }

    @Override
    public void onViewCreated() {
        initWifi();
        initPopWindow();

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);

        btn1.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        settingsRecyclerView.backToList();
                    }
                }
                return false;
            }
        });

        btn1.setOnFocusChangeListener(OnFocusChangeListener);

        btn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showEthernet();

            }
        });

        btn2.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

                    }
                }
                return false;
            }
        });

        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEthernetEnable()) {
                    Toast.makeText(getContext(), getContext().getString(R.string.connect_without_plugin)
                            , Toast.LENGTH_LONG).show();
                    return;
                } else {
                    showWifi();
                }
            }
        });

        btn2.setOnFocusChangeListener(OnFocusChangeListener);

        IntentFilter networkIntentFilter = new IntentFilter();
        networkIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    }

    private boolean isEthernetEnable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            // 獲取所有的網絡信息
            NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
            for (NetworkInfo networkInfo : networkInfos) {
                // 檢查以太網的連接狀態
                if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                    LogUtils.d(" Ethernet Connect "+networkInfo.isConnected());
                    return networkInfo.isConnected(); // 如果以太網已連接，返回 true
                }
            }
        }
        return false;
    }

    private boolean isWifiEnable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            // 獲取當前的網絡信息
            NetworkInfo wifiNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetwork != null) {
                LogUtils.d(" WIFI Connect "+wifiNetwork.isConnected());
                return wifiNetwork.isConnected(); // 如果 Wi-Fi 已連接，返回 true
            }
        }
        return false;
    }

    private void checkEnabale() {
        if (isEthernetEnable()) {
            btn1.setText("  ✓  " + getContext().getString(R.string.cable_network));
            btn2.setText(getContext().getString(R.string.wifi));
        } else if (isWifiEnable()) {
            btn1.setText(getContext().getString(R.string.cable_network));
            btn2.setText("  ✓  " + getContext().getString(R.string.wifi));
        } else {
            btn1.setText(getContext().getString(R.string.cable_network));
            btn2.setText(getContext().getString(R.string.wifi));
        }
    }

    private View popupConfirmView;
    private static PopupWindow popupConfirmWindow;

    private View popupEthernetView;
    private static PopupWindow popupEthernetWindow;

    private ImageView ivArrowLeft, ivArrowRight;
    private Button btnEthernetMode;
    private TextView tvEthernetSelect, tvEthernetMode, etIP, etNetMask, etGateway, etDNS;
    private Button btnSave, btnCancel;

    private void initPopWindow() {
        popupConfirmView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_notice, null);
        popupConfirmWindow = new PopupWindow(popupConfirmView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        popupEthernetView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_network, null);
        popupEthernetWindow = new PopupWindow(popupEthernetView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        ivArrowLeft = (ImageView) popupEthernetView.findViewById(R.id.ivArrowLeft);
        ivArrowRight = (ImageView) popupEthernetView.findViewById(R.id.ivArrowRight);
        btnEthernetMode = popupEthernetView.findViewById(R.id.btnEthernetMode);
        tvEthernetSelect = popupEthernetView.findViewById(R.id.tvEthernetSelect);
        tvEthernetMode = popupEthernetView.findViewById(R.id.tvEthernetMode);

        btnSave = popupEthernetView.findViewById(R.id.btnSave);
        btnCancel = popupEthernetView.findViewById(R.id.btnCancel);

        etIP = popupEthernetView.findViewById(R.id.etIP);
        etNetMask = popupEthernetView.findViewById(R.id.etNetMask);
        etGateway = popupEthernetView.findViewById(R.id.etGateway);
        etDNS = popupEthernetView.findViewById(R.id.etDNS);

        btnSave.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        etIP.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        etNetMask.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        etGateway.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        etDNS.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        DeleteKeyListener deleteKeyListener = new DeleteKeyListener();

        etIP.setOnKeyListener(deleteKeyListener);
        etNetMask.setOnKeyListener(deleteKeyListener);
        etGateway.setOnKeyListener(deleteKeyListener);
        etDNS.setOnKeyListener(deleteKeyListener);

        btnEthernetMode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    tvEthernetSelect.setTypeface(null, Typeface.BOLD);
                    tvEthernetSelect.setTextSize(17);
                    tvEthernetSelect.setTextColor(getResources().getColor(R.color.white));
                    tvEthernetMode.setTypeface(null, Typeface.BOLD);
                    tvEthernetMode.setTextSize(17);
                    tvEthernetMode.setTextColor(getResources().getColor(R.color.white));
                    ivArrowLeft.setImageResource(R.drawable.arrow_setting_spin_l_f);
                    ivArrowRight.setImageResource(R.drawable.arrow_setting_spin_r_f);
                } else {
                    tvEthernetSelect.setTypeface(null, Typeface.NORMAL);
                    tvEthernetSelect.setTextSize(16);
                    tvEthernetSelect.setTextColor(getResources().getColor(R.color.colorWhiteOpacity70));
                    tvEthernetMode.setTypeface(null, Typeface.NORMAL);
                    tvEthernetMode.setTextSize(16);
                    tvEthernetMode.setTextColor(getResources().getColor(R.color.colorWhiteOpacity70));
                    ivArrowLeft.setImageResource(R.drawable.arrow_setting_spin_l_d);
                    ivArrowRight.setImageResource(R.drawable.arrow_setting_spin_r_d);
                }
            }
        });

        btnEthernetMode.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        nextNetMode(keyCode);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        nextNetMode(keyCode);
                        return true;
                    }
                }
                return false;
            }
        });

        btnEthernetMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEthernetSelect.setVisibility(View.VISIBLE);
            }
        });

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (netMode[netModeIndex].equals(getContext().getString(R.string.net_static))) {
                    saveStaticCheck();
                } else {
                    saveDhcp();

                    popupEthernetWindow.dismiss();

                    showNotice();
                }
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupEthernetWindow.dismiss();
            }
        });
    }

    private class DeleteKeyListener implements OnKeyListener {
        public boolean onKey(View view, int keyCode, KeyEvent event) {
            if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (!((EditText) view).getText().toString().equals("")) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Instrumentation inst = new Instrumentation();
                                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                                } catch (Exception e) {
                                    Log.d(TAG, "" + e);
                                }
                            }
                        }.start();
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private void showNotice() {
        popupConfirmWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);

        handler.removeCallbacks(autoCloseNotice);
        handler.postDelayed(autoCloseNotice, 3000);
    }

    private Handler handler = new Handler();
    private Runnable autoCloseNotice = new Runnable() {
        public void run() {
            popupConfirmWindow.dismiss();
        }
    };

    private void saveStaticCheck() {
        String ip = etIP.getText().toString();
        String netMask = etNetMask.getText().toString();
        String gateway = etGateway.getText().toString();
        String dns = etDNS.getText().toString();

        Log.d(TAG, "??? " + (Patterns.IP_ADDRESS.matcher(ip).matches()));
        if (!Patterns.IP_ADDRESS.matcher(ip).matches()) {
            Toast.makeText(getContext(), getContext().getString(R.string.local_ip) +
                    "錯誤 ！！！", Toast.LENGTH_LONG).show();
        } else if (!Patterns.IP_ADDRESS.matcher(netMask).matches()) {
            Toast.makeText(getContext(), getContext().getString(R.string.subnet_mask) +
                    "錯誤 ！！！", Toast.LENGTH_LONG).show();
        } else if (!Patterns.IP_ADDRESS.matcher(gateway).matches()) {
            Toast.makeText(getContext(), getContext().getString(R.string.gateway) +
                    "錯誤 ！！！", Toast.LENGTH_LONG).show();
        } else if (!Patterns.IP_ADDRESS.matcher(dns).matches()) {
            Toast.makeText(getContext(), getContext().getString(R.string.dns) +
                    "錯誤 ！！！", Toast.LENGTH_LONG).show();
        } else {
            saveStatic();

            popupEthernetWindow.dismiss();

            showNotice();
        }
    }

    private void saveDhcp() {
        PrimeUtils.set_Ethernet_dhcp();
    }

    private void saveStatic() {
// 1. 從 EditText 獲取使用者輸入的資訊
        String ipStr = etIP.getText().toString();
        String netMaskStr = etNetMask.getText().toString();
        String gatewayStr = etGateway.getText().toString();
        String dnsStr = etDNS.getText().toString();

        // 2. 轉換子網路遮罩為 Prefix Length
        int prefixLength = maskToPrefixLength(netMaskStr);

        // 如果轉換失敗 (例如輸入了無效遮罩)
        if (prefixLength == 0) {
            Toast.makeText(getContext(), getContext().getString(R.string.subnet_mask) + "格式錯誤或為 0 ！！！", Toast.LENGTH_LONG).show();
            return;
        }

        LogUtils.d("ip "+ip+" "+ipStr);
        LogUtils.d("ip "+netMask+" "+netMaskStr);
        LogUtils.d("gateway "+gateway+" "+gatewayStr);
        LogUtils.d("dns "+dns+" "+dnsStr);
        // 3. 準備網路配置物件
        PrimeUtils.set_Ethernet_static_ip(ipStr, prefixLength, gatewayStr, dnsStr);
    }


    private void nextNetMode(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            netModeIndex = netModeIndex - 1;
            if (netModeIndex == -1) {
                netModeIndex = netMode.length - 1;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            netModeIndex = netModeIndex + 1;
            if (netModeIndex == netMode.length) {
                netModeIndex = 0;
            }
        } else {

        }

        String mode = netMode[netModeIndex];

        if (netModeNow == netModeIndex) {
            tvEthernetSelect.setVisibility(View.VISIBLE);
        } else {
            tvEthernetSelect.setVisibility(View.GONE);
        }

        tvEthernetMode.setText(mode);

        if (!netMode[netModeIndex].equals(getContext().getString(R.string.net_static))) {
            etIP.setEnabled(false);
            etNetMask.setEnabled(false);
            etGateway.setEnabled(false);
            etDNS.setEnabled(false);
        } else {
            etIP.setEnabled(true);
            etNetMask.setEnabled(true);
            etGateway.setEnabled(true);
            etDNS.setEnabled(true);
        }
    }

    private String ip, gateway, netMask, dns;
    private String[] netMode;
    private int netModeNow = 0;
    private int netModeIndex = 0;

    private String prefixLengthToSubnetMask(int prefixLength) {
        int mask = 0xffffffff << (32 - prefixLength);
        return String.format("%d.%d.%d.%d",
                (mask >> 24) & 0xff,
                (mask >> 16) & 0xff,
                (mask >> 8) & 0xff,
                mask & 0xff);
    }

    // 將子網路遮罩字串 (如 "255.255.255.0") 轉換為 Prefix Length (如 24)
    private int maskToPrefixLength(String subnetMask) {
        try {
            InetAddress maskAddr = InetAddress.getByName(subnetMask);
            byte[] maskBytes = maskAddr.getAddress();
            int prefixLength = 0;
            for (byte b : maskBytes) {
                for (int i = 7; i >= 0; i--) {
                    if (((b >> i) & 1) == 1) {
                        prefixLength++;
                    } else {
                        // 假設是連續的遮罩 (如 255.255.255.0)
                        return prefixLength;
                    }
                }
            }
            return prefixLength;
        } catch (Exception e) {
            Log.e(TAG, "Error converting subnet mask to prefix length", e);
            return 0; // 錯誤處理
        }
    }

    private void showEthernet() {
        String str = "0.0.0.0";
        ip = str;
        gateway = str;
        netMask = str;
        dns = str;

        netMode = new String[2];
        netMode[0] = getContext().getString(R.string.net_dhcp);
        netMode[1] = getContext().getString(R.string.net_static);

        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            // 獲取所有的網絡信息
            Network[] networks = connectivityManager.getAllNetworks();
            for (Network network : networks) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    // 獲取鏈接屬性
                    LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
                    if (linkProperties != null) {
                        // 獲取所有的鏈接地址
                        for (LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
                            InetAddress inetAddress = linkAddress.getAddress();
                            if (inetAddress != null) {
                                ip = inetAddress.getHostAddress(); // 獲取 IP 地址
                                netMask = prefixLengthToSubnetMask(linkAddress.getPrefixLength()); // 獲取前綴長度
                            }
                        }

                        // 獲取 DNS
                        List<InetAddress> dnsServers = linkProperties.getDnsServers();
                        if (!dnsServers.isEmpty()) {
                            dns = dnsServers.get(0).getHostAddress(); // 獲取第一個 DNS
                        }

                        // 獲取網關（如果有）
                        List<InetAddress> gateways = linkProperties.getRoutes()
                                .stream()
                                .filter(route -> route.getDestination().toString().equals("0.0.0.0/0"))
                                .map(route -> route.getGateway())
                                .collect(Collectors.toList());
                        if (!gateways.isEmpty()) {
                            gateway = gateways.get(0).getHostAddress(); // 獲取網關
                        }

                        // 這裡可以添加代碼獲取子網掩碼，根據具體需求來獲取
                    }
                    break; // 找到以太網後可以退出循環
                }
            }

        }

        String mode = netMode[netModeIndex];

        tvEthernetSelect.setVisibility(View.VISIBLE);

        tvEthernetMode.setText(mode);

        if (!netMode[netModeIndex].equals(getContext().getString(R.string.net_static))) {
            etIP.setEnabled(false);
            etNetMask.setEnabled(false);
            etGateway.setEnabled(false);
            etDNS.setEnabled(false);
        } else {
            etIP.setEnabled(true);
            etNetMask.setEnabled(true);
            etGateway.setEnabled(true);
            etDNS.setEnabled(true);
        }

        updateInfo();

        popupEthernetWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
    }

    private void updateInfo() {
        etIP.setText(ip);
        etNetMask.setText(netMask);
        etGateway.setText(gateway);
        etDNS.setText(dns);
    }

    private View popupWifiView;
    private static PopupWindow popupWifiWindow;

    private SimpleAdapter wifiAdapter;
    private List<Map<String, Object>> wifiList = new ArrayList<>();

    private ListView lvWifi;

    private void initWifiPopWindow() {
        popupWifiView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_wifi, null);
        popupWifiWindow = new PopupWindow(popupWifiView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        popupWifiWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                LogUtils.d(" ");
                wifiAdapter = null;
                //getContext().unregisterReceiver(wifiReceiver);
            }
        });
        lvWifi = (ListView) popupWifiView.findViewById(R.id.lvWifi);

        lvWifi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String scanResult = wirelessScanResult.get(position);
                Log.d(TAG, "SSID:" + scanResult);
                mSelectWifiSSID = scanResult;

                String lock = getWifiEncryption(scanResult);
                if (lock.toLowerCase().indexOf("wpa") != -1 || lock.toLowerCase().indexOf("wep") != -1){
                    showWifiPassword();
                    popupWifiWindow.dismiss();
                    return;
                }

                connectToWifi(scanResult, null);

                //popupWifiWindow.dismiss();

            }
        });
    }

    // 連接到指定的 Wi-Fi 網絡
    private void connectToWifi(String ssid, String password) {
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        // 這裡需要提供密碼，假設你有一個方法來獲取密碼
        if(password != null)
            wifiConfig.preSharedKey = String.format("\"%s\"", password);

        String connecting = getContext().getString(R.string.connect)+" "+ssid;
        LogUtils.d(connecting);
        Toast.makeText(getContext(), connecting, Toast.LENGTH_SHORT).show();

        // 添加網絡配置並連接
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        updateAdapterAfterConnection();
    }

    private void updateAdapterAfterConnection() {
        // 更新適配器的數據源
        // 這裡可以根據需要更新 ListView 的數據
        LogUtils.d(" ");
        showWifi();
        //searchWireless();
        //updateWifiInfo();
        //wifiList.clear();
        //wifiList = getWifiList();
        //LogUtils.d("wifiList = "+wifiList);
        //wifiAdapter.notifyDataSetChanged(); // 通知適配器更新

    }

    private SimpleAdapter getWifiListAdapter() {
        if(wifiAdapter == null){
            wifiAdapter = new SimpleAdapter(getContext(), wifiList, R.layout.dialog_wifi_list_item,
                    new String[]{"item_select", "item_name", "item_icon"}
                    , new int[]{R.id.item_select, R.id.item_name, R.id.item_icon});
        }
        return wifiAdapter;
    }

    private void showWifi() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
//        getContext().registerReceiver(wifiReceiver, filter);
        LogUtils.d(" ");
        searchWireless();
        updateWifiInfo();
        popupWifiWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
    }

    private void showWifiPassword(){
        popupWifiPwasswordWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
    }

    private void searchWireless() {
        wirelessScanResult = new ArrayList<>();
        mSelectWifiSSID = getCurrentBSsid();
        LogUtils.d(" mSelectWifiSSID "+mSelectWifiSSID);
        if(mSelectWifiSSID != null && mSelectWifiSSID.length() !=0)
        {
            wirelessScanResult.add(mSelectWifiSSID);
        }
        // 獲取 WifiManager 實例
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        // 確保 Wi-Fi 開啟
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true); // 開啟 Wi-Fi
        }
        // 開始掃描
        wifiManager.startScan();
        // 獲取掃描結果
        @SuppressLint("MissingPermission") List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {
            // 將 SSID 添加到結果列表中
            //LogUtils.d("Add to scan result: "+scanResult.SSID);
            if(scanResult.SSID.length() != 0 && !scanResult.SSID.equals(mSelectWifiSSID))
                wirelessScanResult.add(scanResult.SSID);
        }
    }

    private void dismissWifi() {
        popupWifiWindow.dismiss();
    }

    private static final String UI_TEST_SSID = "ui-test-ssid";

    private void initWifi() {
        initWifiPopWindow();
        initWifiPasswordPopWindow();
    }

    private List<String> wirelessScanResult;

    private void updateWifiInfo() {
        wifiList.clear();
        wifiList = getWifiList();
        LogUtils.d("wifiList ="+wifiList);
        lvWifi.setAdapter(getWifiListAdapter());
    }

    private List<Map<String, Object>> getWifiList() {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();

        HashMap<String, Object> wireless;
        String lock;
        for (String sanResult : wirelessScanResult) {
            wireless = new HashMap<String, Object>();
            wireless.clear();
            wireless.put("item_name", sanResult);

            lock = getWifiEncryption(sanResult);

            if (lock.toLowerCase().indexOf("wpa") != -1 || lock.toLowerCase().indexOf("wep") != -1) {
                wireless.put("item_icon", R.drawable.wifi_lock);
            } else {
                wireless.put("item_icon", R.drawable.wifi);
            }

            if(mSelectWifiSSID!=null  && mSelectWifiSSID.equals(sanResult)){
                wireless.put("item_select", " ✓ ");
            } else {
                wireless.put("item_select", "");
            }

            mList.add(wireless);
        }

        return mList;
    }

    // 假設這是一個獲取 Wi-Fi 加密算法的方法
    private String getWifiEncryption(String ssid) {
        // 這裡需要使用 WifiManager 獲取掃描結果以檢查加密類型
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResults = wifiManager.getScanResults();

        for (ScanResult result : scanResults) {
            if (result.SSID.equals(ssid)) {
                return result.capabilities; // 返回加密算法
            }
        }
        return ""; // 如果找不到，返回空字符串
    }

    public String getCurrentBSsid() {
        if(isWifiEnable() == false)
            return null;

        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        // 獲取 SSID，去掉引號
        String ssid = wifiInfo.getSSID();
        if (ssid != null) {
            return ssid.replace("\"", ""); // 去掉引號;
        }
        return null; // 如果沒有連接，則返回 null
    }


    private View popupWifiPasswordView;
    private static PopupWindow popupWifiPwasswordWindow;

    private EditText etWifiPassword;
    private Button btnWifiSave;

    private void updateWifiStatus(){
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo != null && wifiInfo.getNetworkId() != -1) {
            getWifiList();
            //String ssid = wifiInfo.getSSID().replace("\"", ""); // 去掉引號
            // 更新 UI
           // updateUIWithConnectedSSID(ssid);
        } else {
            // 沒有連接，更新 UI 以顯示未連接狀態
           // updateUIWithDisconnectedStatus();
        }
    }

    private void initWifiPasswordPopWindow() {
        popupWifiPasswordView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_wifi_password, null);
        popupWifiPwasswordWindow = new PopupWindow(popupWifiPasswordView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        popupWifiPwasswordWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //searchWireless();
                //updateWifiInfo();
            }
        });

        etWifiPassword = popupWifiPasswordView.findViewById(R.id.etWifiPassword);

        btnWifiSave = popupWifiPasswordView.findViewById(R.id.btnWifiSave);
        Button btnCancel = popupWifiPasswordView.findViewById(R.id.btnCancel);

        etWifiPassword.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnWifiSave.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        etWifiPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etWifiPassword.getText().toString().equals("")) {
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Instrumentation inst = new Instrumentation();
                                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                                    } catch (Exception e) {
                                        Log.d(TAG, "" + e);
                                    }
                                }
                            }.start();
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        etWifiPassword.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    showKeyboard(etWifiPassword);
            }
        });

        btnWifiSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);

                connectToWifi(mSelectWifiSSID, etWifiPassword.getText().toString());

                popupWifiPwasswordWindow.dismiss();
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWifiPwasswordWindow.dismiss();
            }
        });
    }

    public void showKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}