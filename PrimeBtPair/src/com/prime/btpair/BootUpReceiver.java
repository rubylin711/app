package com.prime.btpair;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.provider.Settings;
import android.util.Log;
import java.util.Set;

public class BootUpReceiver extends BroadcastReceiver{
    private static final String TAG = "BootUpReceiver";

	private static final boolean DEBUG = true;
    private static final String PRIME_BTPAIR_PACKAGE = "com.prime.btpair";
    private static final String PRIME_BTPAIR_HOOKBEGINACTIVITY = "com.prime.btpair.HookBeginActivity";	
    public static final int MINOR_DEVICE_CLASS_KEYBOARD =
            Integer.parseInt("0000001000000", 2);
    public static final int MINOR_DEVICE_CLASS_REMOTE =
            Integer.parseInt("0000000001100", 2);	


    @Override
    public void onReceive(Context context, Intent intent) {
		   Log.d(TAG, "Received action = [ "+ intent.getAction() +"]");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			handleKeycode_2(context);

        }
    }

    private static boolean isTvSetupComplete(Context context) {
        return Settings.Secure.getInt(
                context.getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 0) != 0;
    }

    public static void handleKeycode_2(Context context) {
        if ( ! isTvSetupComplete(context) ) // before setup process finish, auto pair apk will trigger by Android TV setup wraith
            return;
        if (hasRemoteControl())
            return;
        //PackageManager packageManager = context.getPackageManager();
        //Intent intent = new Intent("com.google.android.tvsetup.app.REPAIR_REMOTE");
        //List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        //boolean isIntentSafe = activities.size() > 0;
        //if (!isIntentSafe)
        //{
        //    intent = new Intent("com.rtk.partnerinterface.action.RCU_WARNNING");
        //}
		Intent newIntent = new Intent();
		newIntent.setComponent(new ComponentName(PRIME_BTPAIR_PACKAGE, PRIME_BTPAIR_HOOKBEGINACTIVITY));
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(newIntent);
    }
    public static boolean isRemoteControl (BluetoothDevice device) {
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        if (DEBUG) Log.d(TAG,"isRemoteControl device="+device+" name="+device.getName()
            +" getDeviceClass="+bluetoothClass.getDeviceClass()
            +" devicetype="+device.getType());
        if (bluetoothClass.getMajorDeviceClass() == BluetoothClass.Device.Major.PERIPHERAL &&
            BluetoothDevice.DEVICE_TYPE_LE == device.getType() &&
            (((bluetoothClass.getDeviceClass() & MINOR_DEVICE_CLASS_REMOTE)!= 0)
                || ((bluetoothClass.getDeviceClass() & MINOR_DEVICE_CLASS_KEYBOARD)!= 0))) {
            Log.d(TAG,"isRemoteControl device="+device+" is remote control");
            return true;
        }
        return false;
    }
    public static boolean hasRemoteControl () {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            final Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();
            if (bondedDevices != null) {
                for (BluetoothDevice device : bondedDevices) {
                    if (isRemoteControl(device)) {
                        Log.d(TAG, "Still has remote control device="+device);
                        return true;
                    }
                }
            }
        }
        return false;
    }	
}
