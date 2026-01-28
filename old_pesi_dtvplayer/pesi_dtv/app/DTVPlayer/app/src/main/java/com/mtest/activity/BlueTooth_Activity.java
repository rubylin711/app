package com.mtest.activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
//import android.bluetooth.IBluetoothGatt;//jackie mod bt rssi
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;//jackie mod bt rssi
import android.util.Log;//jackie mod bt rssi
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.mtest.config.MtestConfig;
import com.mtest.module.PesiSharedPreference;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.prime.dtvplayer.R.id.tv_item26_message;

public class BlueTooth_Activity extends DTVActivity {
    String strListDevices;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice mBondDevice = null;
    private HidConncetUtil mHidConncetUtil = null;
    boolean reg_service = false;
    private BluetoothDevice mDevice;//jackie mod bt rssi
//    private IBluetoothGatt mService;//jackie mod bt rssi // @hide, find other method
    private int mClientIf;//jackie mod bt rssi
    private static final String TAG = "BlueTooth_Activity";//jackie mod bt rssi

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        m_bt_init();

        TextView tv26_msg = (TextView) findViewById(tv_item26_message);
        tv26_msg.setText("1.Push BT's any keys to pair\n2.Push IR yellow key to start");
        tv26_msg.setTextColor(Color.RED);
    }

    public boolean m_bt_init() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "找不到藍牙裝置", Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(this, "找到藍牙裝置", Toast.LENGTH_SHORT).show();
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }

            //4.0以上才支持HID模式
            if (Build.VERSION.SDK_INT >= 14) {
                this.mHidConncetUtil = new HidConncetUtil(this);
            }
        }

        return false;
    }

    // use @hide values, find other method
//    public boolean readRemoteRssi() {//jackie mod bt rssi
//        Log.d(TAG, "jackie-bt readRssi() - device: " + mDevice.getAddress());
//        if (mService == null ) return false;//|| mClientIf == 0
//        try {
//            mService.readRemoteRssi(mClientIf, mDevice.getAddress());
//        } catch (RemoteException e) {
//            Log.e(TAG,"jackie-bt e = ",e);
//            return false;
//        }
//        return true;
//    }

    public void startScanBT() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(scanBTReceiver, intentFilter);
        reg_service = true;
        if (bluetoothAdapter.startDiscovery()) {
            //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            //registerReceiver(bondedBTReceiver, filter);
        } else {
            System.out.println("搜尋失敗");
        }
    }

    private BroadcastReceiver scanBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                strListDevices = strListDevices + bluetoothDevice.toString() + "\\";
                System.out.println("ACTION_FOUND");
                System.out.println(bluetoothDevice.toString());
                if (bluetoothDevice.toString().equals("00:02:5B:A1:00:04")) {
                    mBondDevice = bluetoothDevice;
                    System.out.println("mtest BT bond!!");
                    connect(mBondDevice);
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                System.out.println("start scan");
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                System.out.println("scan finish");
                System.out.println(strListDevices);
            }

            Set<BluetoothDevice> bondDevices = bluetoothAdapter.getBondedDevices();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg_service == true)
            this.unregisterReceiver(scanBTReceiver);
        if ((mHidConncetUtil != null) && (mBondDevice != null)) {
            //if (mBondDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            mHidConncetUtil.disConnect(mBondDevice);
            mHidConncetUtil.unPair(mBondDevice);
            //}
        }
    }

    private BroadcastReceiver bondedBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            switch (bluetoothDevice.getBondState()) {
                case BluetoothDevice.BOND_BONDING:
                    System.out.println("配對中");
                    break;
                case BluetoothDevice.BOND_NONE:
                    System.out.println("沒有設備進行配對");
                    break;
                case BluetoothDevice.BOND_BONDED:
                    System.out.println("配對完成");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);

        System.out.println(keyCode);
        Global_Variables gv = (Global_Variables) getApplicationContext();
        //Toast toast = Toast.makeText(this, Integer.toString(keyCode), Toast.LENGTH_SHORT);
        //toast.show();
        if (mBondDevice != null) {
            //if (mBondDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            //Toast toast = Toast.makeText(this, Integer.toString(mBondDevice.getBondState()), Toast.LENGTH_SHORT);
            //toast.show();
            //}
        }


        TextView tv_item26_key = (TextView) findViewById(R.id.tv_item26_bluetooth);
        tv_item26_key.setText(String.format("KEY:%02X", keyCode));
        switch (keyCode) {
            case KeyEvent.KEYCODE_PROG_YELLOW:
            case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
                if (!bluetoothAdapter.isDiscovering()) {
                    startScanBT();
                }
                break;

            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_bluetooth), MtestConfig.TEST_RESULT_PASS);
                pesiSharedPreference.save();
                //Toast.makeText(this, "BLUE", Toast.LENGTH_SHORT).show();
                finish();
                break;

            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_bluetooth), MtestConfig.TEST_RESULT_FAIL);
                pesiSharedPreference.save();
                //Toast.makeText(this, "BLUE", Toast.LENGTH_SHORT).show();
                finish();
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                //mHidConncetUtil.disConnect(mBondDevice);
                //mHidConncetUtil.unPair(mBondDevice);
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void createBond(BluetoothDevice device) {
        Boolean returnValue = false;
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            //需要API >= 19
            //device.createBond();
            Method createBondMethod = null;
            try {
                createBondMethod = BluetoothDevice.class.getMethod("createBond");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            try {
                returnValue = (Boolean) createBondMethod.invoke(device);
                connect(device);
                //Toast toast = Toast.makeText(this, "createBond" + returnValue.toString(), Toast.LENGTH_SHORT);
                //toast.show();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void connect(BluetoothDevice btDev) {
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();

        if (mHidConncetUtil != null) {
            //先配对再连接
            mHidConncetUtil.pair(btDev);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mHidConncetUtil.connect(btDev);
            if (mBondDevice != null) {
                //if (mBondDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                //Toast toast = Toast.makeText(this, Integer.toString(mBondDevice.getBondState()), Toast.LENGTH_LONG);
                //toast.show();
                //}
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (btDev.getBondState() == BluetoothDevice.BOND_BONDED) {
                TextView tv26_msg = (TextView) findViewById(tv_item26_message);
                tv26_msg.setText("Push BT's key to show key");
                tv26_msg.setTextColor(Color.GREEN);
                TextView tv26_bt = (TextView) findViewById(R.id.tv_item26_bluetooth);
                tv26_bt.setBackgroundResource(R.drawable.shape_rectangle_pass);
            }
        }

    }


    @SuppressLint("NewApi")
    public static class HidConncetUtil {
        private BluetoothDevice device;
        Context context;
        ArrayList<BluetoothDevice> hidConncetList = new ArrayList<BluetoothDevice>();
        GetHidConncetListListener getHidConncetListListener;

        public HidConncetUtil(Context context) {
            this.context = context;
        }

        /**
         * 获取BluetoothProfile中hid的profile，"INPUT_DEVICE"类型隐藏，需反射获取
         *
         * @return
         */
        @SuppressLint("NewApi")
        public static int getInputDeviceHiddenConstant() {
            Class<BluetoothProfile> clazz = BluetoothProfile.class;
            for (Field f : clazz.getFields()) {
                int mod = f.getModifiers();
                if (Modifier.isStatic(mod) && Modifier.isPublic(mod)
                        && Modifier.isFinal(mod)) {
                    try {
                        if (f.getName().equals("INPUT_DEVICE")) {
                            return f.getInt(null);
                        }
                    } catch (Exception e) {
                    }
                }
            }
            return -1;
        }

        /**
         * 通过getHidConncetListListener.getSuccess(hidConncetList);回调
         */
        private BluetoothProfile.ServiceListener getList = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                try {
                    if (profile == getInputDeviceHiddenConstant()) {
                        hidConncetList.clear();
                        List<BluetoothDevice> connectedDevices = proxy
                                .getConnectedDevices();
                        for (BluetoothDevice bluetoothDevice : connectedDevices) {
                            hidConncetList.add(bluetoothDevice);
                        }
                    }
                    getHidConncetListListener.getSuccess(hidConncetList);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }

            }

            @Override
            public void onServiceDisconnected(int profile) {

            }
        };
        /**
         * 查看BluetoothInputDevice源码，connect(BluetoothDevice device)该方法可以连接HID设备，但是查看BluetoothInputDevice这个类
         * 是隐藏类，无法直接使用，必须先通过BluetoothProfile.ServiceListener回调得到BluetoothInputDevice，然后再反射connect方法连接
         */
        private BluetoothProfile.ServiceListener connect = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                //BluetoothProfile proxy这个已经是BluetoothInputDevice类型了
                try {
                    if (profile == getInputDeviceHiddenConstant()) {
                        if (device != null) {
                            //得到BluetoothInputDevice然后反射connect连接设备
                            Method method = proxy.getClass().getMethod("connect",
                                    new Class[]{BluetoothDevice.class});
                            method.invoke(proxy, device);
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {

            }
        };
        private BluetoothProfile.ServiceListener disConnect = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                try {
                    if (profile == getInputDeviceHiddenConstant()) {
                        List<BluetoothDevice> connectedDevices = proxy
                                .getConnectedDevices();
                        for (BluetoothDevice bluetoothDevice : connectedDevices) {
                            hidConncetList.add(bluetoothDevice);
                        }

                        if (device != null) {
                            Method method = proxy.getClass().getMethod("disconnect",
                                    new Class[]{BluetoothDevice.class});
                            method.invoke(proxy, device);
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {

            }
        };

        /**
         * 连接设备
         *
         * @param bluetoothDevice
         */
        public void connect(final BluetoothDevice bluetoothDevice) {
            device = bluetoothDevice;
            try {
                BluetoothAdapter.getDefaultAdapter().getProfileProxy(context,
                        connect, getInputDeviceHiddenConstant());
            } catch (Exception e) {

            }
        }

        /**
         * 断开连接
         *
         * @param bluetoothDevice
         */
        public void disConnect(BluetoothDevice bluetoothDevice) {
            device = bluetoothDevice;
            try {
                BluetoothAdapter.getDefaultAdapter().getProfileProxy(context,
                        disConnect, getInputDeviceHiddenConstant());
            } catch (Exception e) {

            }
        }

        /**
         * 配对
         *
         * @param bluetoothDevice
         */
        public void pair(BluetoothDevice bluetoothDevice) {
            device = bluetoothDevice;
            Method createBondMethod;
            try {
                createBondMethod = BluetoothDevice.class.getMethod("createBond");
                createBondMethod.invoke(device);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        /**
         * 取消配对
         *
         * @param bluetoothDevice
         */
        public void unPair(BluetoothDevice bluetoothDevice) {
            device = bluetoothDevice;
            Method createBondMethod;
            try {
                createBondMethod = BluetoothDevice.class.getMethod("removeBond");
                createBondMethod.invoke(device);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        /**
         * 返回true代表修改成功 返回false代表修改失败
         */
        public Boolean rename(BluetoothDevice bluetoothDevice, String name) {
            device = bluetoothDevice;
            Method createBondMethod;
            try {
                createBondMethod = BluetoothDevice.class.getMethod("setAlias",
                        String.class);
                Boolean Issuccess = (Boolean) createBondMethod.invoke(device, name);
                return Issuccess;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }

        }

        /**
         * 得到所有HID连接的设备列表
         *
         * @param getHidConncetListListener
         */
        public void getHidConncetList(
                GetHidConncetListListener getHidConncetListListener) {
            this.getHidConncetListListener = getHidConncetListListener;
            try {
                BluetoothAdapter.getDefaultAdapter().getProfileProxy(context,
                        getList, getInputDeviceHiddenConstant());
            } catch (Exception e) {

            }

        }

        public interface GetHidConncetListListener {
            public void getSuccess(ArrayList<BluetoothDevice> list);
        }
    }

}
