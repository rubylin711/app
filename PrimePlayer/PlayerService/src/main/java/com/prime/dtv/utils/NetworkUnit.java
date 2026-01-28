package com.prime.dtv.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class NetworkUnit {
    private static final String TAG = "NetworkUnit";

    public static String get_Ethernet_mac(){
        return get_mac("eth0");
    }
    public static String get_Wifi_mac(){
        return get_mac("wlan0");
    }

    private static String get_mac( String interface_name )
    {
        String rsp = "001122334455";
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getDisplayName().equalsIgnoreCase(interface_name)) {
                    byte[] macBytes = nif.getHardwareAddress();
                    nif.getInetAddresses();
                    if (macBytes != null) {
                        StringBuilder res1 = new StringBuilder();
                        for (byte b : macBytes) {
                            res1.append(String.format("%02X", Byte.valueOf(b)));
                        }

                        rsp = res1.toString();
                    }
                    Log.d(TAG, "NET INTERFACE INFO -  " + nif.getName() + " MAC: " + rsp);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(TAG, "SOME ERROR ON READING NETWORK INTERFACE: " + ex.toString());
        }
        return rsp ;
    }

    public static boolean is_wifi_enable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            // 獲取當前的網絡信息
            NetworkInfo wifiNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetwork != null) {
                Log.d(TAG, "is_wifi_enable: WIFI Connect = " +wifiNetwork.isConnected());
                return wifiNetwork.isConnected(); // 如果 Wi-Fi 已連接，返回 true
            }
        }
        return false;
    }

    public static boolean is_ethernet_enable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            // 獲取所有的網絡信息
            NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
            for (NetworkInfo networkInfo : networkInfos) {
                // 檢查以太網的連接狀態
                if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                    Log.d(TAG, "is_ethernet_enable: Ethernet Connect = " +networkInfo.isConnected());
                    return networkInfo.isConnected(); // 如果以太網已連接，返回 true
                }
            }
        }
        return false;
    }

    public static String get_wifi_ip(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Network network = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            network = cm.getActiveNetwork();
        }
        if (network == null) return null;

        LinkProperties lp = cm.getLinkProperties(network);
        if (lp == null) return null;

        for (LinkAddress la : lp.getLinkAddresses()) {
            InetAddress addr = la.getAddress();
            if (addr instanceof Inet4Address) {
                return addr.getHostAddress(); // e.g. 192.168.1.100
            }
        }
        return null;
    }

    public static String get_ethernet_ip(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return null;

        Network[] networks = cm.getAllNetworks();
        for (Network network : networks) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {

                LinkProperties lp = cm.getLinkProperties(network);
                if (lp == null) continue;

                for (LinkAddress la : lp.getLinkAddresses()) {
                    InetAddress addr = la.getAddress();
                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress(); // e.g. 10.180.89.12
                    }
                }
            }
        }
        return null;
    }
}
