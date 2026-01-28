package com.pesi.remote_service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RcuReceiver extends BroadcastReceiver {
    private static final String TAG ="RcuReceiver";

    public final List<String> ACCETABLE_RCU = Collections.unmodifiableList(new ArrayList<String>() {{
        add("RemoteG10");
        add("RemoteG20");
        add("RemoteB009");
    }});

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "get action " + intent.getAction());
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())) {
            Log.d(TAG, "Bluetooth Connected");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            for (BluetoothDevice bluetoothDevice : bluetoothAdapter.getBondedDevices()) {
                Log.d(TAG, "bluetoothDevice = " + bluetoothDevice.getName());
                if ( ACCETABLE_RCU.contains(bluetoothDevice.getName()) ) {
                    Log.d(TAG, "start RcuVoiceBluetoothService");
                    Intent intentRun = new Intent(Intent.ACTION_RUN);
                    intentRun.setClass(context, RcuVoiceBluetoothService.class);
                    intentRun.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startService(intentRun);
                }
            }
        } /*else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
            Log.d(TAG, "stop RcuVoiceBluetoothService");
            Intent intentRun = new Intent(Intent.ACTION_RUN);
            intentRun.setClass(context, RcuVoiceBluetoothService.class);
            intentRun.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.stopService(intentRun);
        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
            Log.d(TAG, "ACTION_BOND_STATE_CHANGED");
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(intent.getAction())) {
            Log.d(TAG, "ACTION_BOND_STATE_CHANGED");
        }*/
    }
}
