package com.prime.dtv;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.HomeActivity;
import com.prime.dtv.utils.LogUtils;

import java.lang.ref.WeakReference;

public class NetworkChangeReceiver{
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    WeakReference<AppCompatActivity> g_ref;

    public NetworkChangeReceiver(Context context, AppCompatActivity activity){
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(Network network) {
                LogUtils.d("Network is available");
            }
            @Override
            public void onLost(Network network){
                LogUtils.d("Network is lost");
                // power, standby and wifi led will be handled by OTAService
//                get().g_dtv.set_wifi_led(0);
            }
            @Override
            public void onCapabilitiesChanged (Network network,
                                               NetworkCapabilities capabilities){
                super.onCapabilitiesChanged(network, capabilities);
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    LogUtils.d("Wi-Fi is connected");
                    // power, standby and wifi led will be handled by OTAService
//                    get().g_dtv.set_wifi_led(1);
                //} else if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                //    LogUtils.d("Ethernet is connected");
                //   get().g_dtv.set_wifi_led(1);
                }
                else {
                    LogUtils.d( "Other network type is connected");
                    //get().g_dtv.set_wifi_led(0);
                }
            }
        };

        g_ref = new WeakReference<>(activity);
    }

    public void registerNetworkCallback() {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                //.addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build();
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    public void unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }
}
