package com.pesi.remote_service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RcuVoiceBluetoothGATT {
    String TAG = "RcuVoiceBluetoothGATT";
    UUID ATVV_SERVICE_UUID = UUID.fromString("AB5E0001-5A21-4F05-BC7D-AF01F617B664");
    UUID ATVV_CHAR_TX = UUID.fromString("AB5E0002-5A21-4F05-BC7D-AF01F617B664");
    UUID ATVV_CHAR_RX = UUID.fromString("AB5E0003-5A21-4F05-BC7D-AF01F617B664");
    UUID ATVV_CHAR_CTL = UUID.fromString("AB5E0004-5A21-4F05-BC7D-AF01F617B664");
    UUID GATT_CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
    UUID REMOTE_G20_UUID = UUID.fromString("00001812-0000-1000-8000-00805f9b34fb");

    private ProcessAtvvAudio mProcessAtvvAudio;
    private BluetoothGattThread mBluetoothGattThread;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mGattService;
    private Context mContext = null;
    private Handler mUiHandler = null ;
    private boolean mReceivingAudioData = false;

    BluetoothGattCharacteristic mAtvv_char_Rx_characteristic;
    BluetoothGattCharacteristic mAtvv_char_Ctrl_characteristic;
    BluetoothGattCharacteristic mAtvv_char_Tx_characteristic;

    public static final int ATVV_TX_COMMAND_SEND_GET_CAPS_0_4e = 0x100;
    public static final int ATVV_TX_COMMAND_SEND_GET_CAPS_1_0 = 0x101;
    public static final int ATVV_TX_COMMAND_SEND_MIC_OPEN = 0x102;
    public static final int ATVV_TX_COMMAND_SEND_MIC_CLOSE = 0x103;
    public static final int ATVV_NOTIFY_DATA = 0x104;

    private boolean mRcuInitFinish = false ;
    private boolean mVoiceAssistantStartFlag = false;

    private String mRcuMaxSupportAtvvVersion = "1.0";
//    private String mRcuMaxSupportAtvvVersion = "0.4e";
    byte TX_ATV_GET_CAPS_DATA_v_0_4e[] = {0x0A, 0x00, 0x04, 0x00, 0x07};
    byte TX_ATV_GET_CAPS_DATA_v_1_0[] = {0x0A, 0x01, 0x00, 0x00, 0x03, 0x00};
    byte TX_ATV_MIC_OPEN_DATA[] = {0x0C, 0x01};
    byte TX_ATV_MIC_CLOSE_DATA[] = {0x0D, 0x00};
    final byte CTL_GET_CAPS_RESP = 0x0B;
    final byte CTL_START_SEARCH_DATA = 0x08;
    final byte CTL_AUDIO_START_DATA = 0x04;
    final byte CTL_AUDIO_STOP_DATA = 0x00;
    final byte CTL_AUDIO_SYNC_DATA = 0x0A;
    private int mRcuAudioDataByteFrameSize = 0;


    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    private final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    private final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    private final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    private final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final List<String> ACCETABLE_RCU = Collections.unmodifiableList(new ArrayList<String>() {{
        add("RemoteG10");
        add("RemoteG20");
        add("RemoteB009");
    }});

    public RcuVoiceBluetoothGATT(Context context, Handler uiHandler) {
        mContext = context;
        mUiHandler = uiHandler ;
        mBluetoothGattThread = new BluetoothGattThread();
        mBluetoothGattThread.start();
        if ( mRcuMaxSupportAtvvVersion == "0.4e" )
            mProcessAtvvAudio = new ProcessAtvvAudio_V_0_4(context, mUiHandler);
        else
            mProcessAtvvAudio = new ProcessAtvvAudio_V_1_0(context, mUiHandler);
        mProcessAtvvAudio.start();
    }

    public void connectBluetoothGatt() {
        Log.d(TAG, "connectBluetoothGatt");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "BluetoothAdapter is Null");
            return;
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "BluetoothAdapter is NOT enabled, enable now");
//                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    return;
//                }
                bluetoothAdapter.enable();
                if (!bluetoothAdapter.isEnabled()) {
                    Log.e(TAG, "Can't enable Bluetooth");
                    return;
                }
            }
        }

        for (BluetoothDevice bluetoothDevice : bluetoothAdapter.getBondedDevices()) {
            Log.d(TAG, "bluetoothDevice = " + bluetoothDevice.getName());
            if ( ACCETABLE_RCU.contains(bluetoothDevice.getName()) ) {
                bluetoothDevice.connectGatt(mContext, false, mGattCallback);
            }
        }

    }

    public void disconnectBluetoothGatt() {
        if (mBluetoothGattThread != null) {
            mBluetoothGattThread.interrupt();
            mBluetoothGattThread = null ;
        }
        if (mBluetoothGatt != null) {
//            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null ;
        }
    }

    private void setClientCharConfigNotifyEnable( BluetoothGattDescriptor desc )
    {
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(desc);
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            mBluetoothGatt = gatt;
//            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                Log.i(TAG, "BluetoothGattCallback Connected to GATT server.");
                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "BluetoothGattCallback Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "BluetoothGattCallback onServicesDiscovered " + status);
                mGattService = mBluetoothGatt.getService(ATVV_SERVICE_UUID);

                mAtvv_char_Tx_characteristic = mGattService.getCharacteristic(ATVV_CHAR_TX);
                mAtvv_char_Tx_characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

                mAtvv_char_Rx_characteristic = mGattService.getCharacteristic(ATVV_CHAR_RX);
                mBluetoothGatt.setCharacteristicNotification(mAtvv_char_Rx_characteristic, true);
                // initial 1.set rx client char config enable notify
                setClientCharConfigNotifyEnable(mAtvv_char_Rx_characteristic.getDescriptor(GATT_CLIENT_CHARACTERISTIC_CONFIGURATION));

                mAtvv_char_Ctrl_characteristic = mGattService.getCharacteristic(ATVV_CHAR_CTL);
                mBluetoothGatt.setCharacteristicNotification(mAtvv_char_Ctrl_characteristic, true);

                mRcuInitFinish = false;
            } else {
                Log.w(TAG, "BluetoothGattCallback onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            sendMsg(ATVV_NOTIFY_DATA, String.valueOf(characteristic.getUuid()), "onCharacteristicRead", characteristic.getValue());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         byte[] value, int status) {
            sendMsg(ATVV_NOTIFY_DATA, String.valueOf(characteristic.getUuid()), "onCharacteristicRead 1", value);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            sendMsg(ATVV_NOTIFY_DATA, String.valueOf(characteristic.getUuid()), "onCharacteristicWrite", characteristic.getValue());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            sendMsg(ATVV_NOTIFY_DATA, String.valueOf(characteristic.getUuid()), "onCharacteristicChanged", characteristic.getValue());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic, byte[] value) {
            sendMsg(ATVV_NOTIFY_DATA, String.valueOf(characteristic.getUuid()), "onCharacteristicChanged 1", value);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            sendMsg(ATVV_NOTIFY_DATA, String.valueOf(descriptor.getUuid()), "onDescriptorRead", descriptor.getValue());
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if ( descriptor.getUuid().equals(GATT_CLIENT_CHARACTERISTIC_CONFIGURATION) && descriptor.getCharacteristic().getUuid().equals(ATVV_CHAR_RX) )
            {
                // initial 2. while rx client char config enable notify success
                // initial 3. set ctrl client char config enable notify
                setClientCharConfigNotifyEnable(mAtvv_char_Ctrl_characteristic.getDescriptor(GATT_CLIENT_CHARACTERISTIC_CONFIGURATION));
            }
            else if ( descriptor.getUuid().equals(GATT_CLIENT_CHARACTERISTIC_CONFIGURATION) && descriptor.getCharacteristic().getUuid().equals(ATVV_CHAR_CTL) )
            {
                // initial 4. while ctrl client char config enable notify success
                // initial 5. send GET_CAPS to tx
                if (mRcuMaxSupportAtvvVersion.equals("0.4e"))
                    sendMsg(ATVV_TX_COMMAND_SEND_GET_CAPS_0_4e, 1);
                else {
                    sendMsg(ATVV_TX_COMMAND_SEND_GET_CAPS_1_0, 0);
                }
            }
            else
            {
                sendMsg(ATVV_NOTIFY_DATA, String.valueOf(descriptor.getUuid()), "onDescriptorWrite", descriptor.getValue());
            }
        }
    };

    public String UUIDtoReadableString(UUID uuid) {
        if (uuid.equals(ATVV_SERVICE_UUID)) {
            return "ATVV_SERVICE_UUID";
        } else if (uuid.equals(ATVV_CHAR_TX)) {
            return "ATVV_CHAR_TX";
        } else if (uuid.equals(ATVV_CHAR_RX)) {
            return "ATVV_CHAR_RX";
        } else if (uuid.equals(ATVV_CHAR_CTL)) {
            return "ATVV_CHAR_CTL";
        }

        return "Unknow UUID " + uuid;
    }

    public void PrintBytesData(UUID uuid, String callerName, byte data[]) {
        Log.d(TAG, "[" + UUIDtoReadableString(uuid) + "] [" + callerName + "] PrintBytesData data length = " + data.length + " data[0] = " + String.format("%02X", data[0]));
        if (uuid.equals(ATVV_CHAR_RX)) {
            return;
        }

        for (Byte databyte : data) {
            Log.d(TAG, "[" + UUIDtoReadableString(uuid) + "] [" + callerName + "] PrintBytesData databyte = " + String.format("%02X", databyte));
        }
    }

    private void sendTxDataByteToRCU(byte data[]) {
//        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
        mAtvv_char_Tx_characteristic.setValue(data);
        mBluetoothGatt.writeCharacteristic(mAtvv_char_Tx_characteristic);
    }

    public void sendMsg(int caseId, String uuid, String str, byte data[]) {
        mBluetoothGattThread.sendMessage(caseId, uuid, str, data);
    }

    public void sendMsg(int caseId, int delayMs) {
        mBluetoothGattThread.sendMessage(caseId, delayMs);
    }

    public void sendMsg(int caseId) {
        mBluetoothGattThread.sendMessage(caseId, 0);
    }

    public void parsingAndProcessData(UUID uuid, byte data[]) {
        if (uuid.equals(ATVV_CHAR_CTL)) {
            int data_header = data[0];
            switch (data_header) {
                case CTL_GET_CAPS_RESP:
                    PrintBytesData(uuid, "CTL_GET_CAPS_RESP", data ) ;
                    mRcuInitFinish = true ;
                    mRcuAudioDataByteFrameSize = (Byte.toUnsignedInt(data[5]) << 8) + Byte.toUnsignedInt(data[6]);
//                    if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                        return;
//                    }
//                    mBluetoothGatt.requestMtu(123);
                break;
                case CTL_START_SEARCH_DATA:
                    if (!mVoiceAssistantStartFlag) {
                        startVoiceAssistApk();
                    }

                    PrintBytesData(uuid, "CTL_START_SEARCH_DATA", data ) ;
                    if ( mRcuInitFinish && mVoiceAssistantStartFlag) {
                        sendMessageToService(RcuVoiceBluetoothService.SEND_MIC_OPEN);
                        sendMsg(ATVV_TX_COMMAND_SEND_MIC_OPEN);
                    }
                break;
                case CTL_AUDIO_START_DATA:
                    PrintBytesData(uuid, "CTL_AUDIO_START_DATA", data ) ;
                    if ( mRcuInitFinish ) {
                        mProcessAtvvAudio.sendMessage(ProcessAtvvAudio.ATVV_AUDIO_START, data, mRcuAudioDataByteFrameSize);
                    }
                break;
                case CTL_AUDIO_STOP_DATA:
                    PrintBytesData(uuid, "CTL_AUDIO_STOP_DATA", data ) ;
                    if ( mRcuInitFinish ) {
                        mReceivingAudioData = false;
                        sendMessageToService(RcuVoiceBluetoothService.SEND_MIC_CLOSE);
                        mProcessAtvvAudio.sendMessage(ProcessAtvvAudio.ATVV_AUDIO_STOP, data);
                    }
                break;
                case CTL_AUDIO_SYNC_DATA:
                    if ( mRcuInitFinish )
                        mProcessAtvvAudio.sendMessage(ProcessAtvvAudio.ATVV_AUDIO_SYNC, data);
                break;
            }
        }
        else if ( uuid.equals(ATVV_CHAR_TX) )
        {
            PrintBytesData(uuid, "ATVV_CHAR_TX", data ) ;
        }
        else if ( uuid.equals(ATVV_CHAR_RX) )
        {
            if ( mRcuInitFinish ) {
                if (!mReceivingAudioData) {
                    Log.d(TAG, "SEND_MIC_RECEIVE_AUDIO_DATA");
                    sendMessageToService(RcuVoiceBluetoothService.SEND_MIC_RECEIVE_AUDIO_DATA);
                    mReceivingAudioData = true;
                }
                mProcessAtvvAudio.sendMessage(ProcessAtvvAudio.ATVV_AUDIO_DATA, data);
            }
        }
    }

    public void setVoiceAssistantStartFlag(boolean flag) {
        mVoiceAssistantStartFlag = flag;
    }

    private void startVoiceAssistApk() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.pesi.voice_assist", "com.pesi.voice_assist.MainActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void sendMessageToService(int what) {
        Message message = new Message() ;
        message.what = what;
        mUiHandler.sendMessage(message);
    }

    private class BluetoothGattThread extends Thread {
        private Handler mHandler;

        @SuppressLint("HandlerLeak")
        public BluetoothGattThread() {
            Log.d( TAG, "create thread BluetoothGattThread" ) ;
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    int caseId = msg.what;
                    switch (caseId) {
                        case ATVV_TX_COMMAND_SEND_GET_CAPS_0_4e:
                        {
                            sendTxDataByteToRCU( TX_ATV_GET_CAPS_DATA_v_0_4e ) ;
                        }break;
                        case ATVV_TX_COMMAND_SEND_GET_CAPS_1_0:
                        {
                            sendTxDataByteToRCU( TX_ATV_GET_CAPS_DATA_v_1_0 ) ;
                        }break;
                        case ATVV_TX_COMMAND_SEND_MIC_OPEN:
                        {
                            sendTxDataByteToRCU(TX_ATV_MIC_OPEN_DATA);
                        }break;
                        case ATVV_TX_COMMAND_SEND_MIC_CLOSE:
                        {
                            sendTxDataByteToRCU(TX_ATV_MIC_CLOSE_DATA);
                        }break;
                        case ATVV_NOTIFY_DATA:
                        {
                            Bundle bundle = msg.getData() ;
                            String uuid = bundle.getString("uuid") ;
                            String callerName = bundle.getString("caller");
                            byte data[] = bundle.getByteArray("data");
                            parsingAndProcessData(UUID.fromString(uuid), data ) ;
                        }break;
                        default:
                        {
                            Log.d( TAG, "Unknow command " + caseId ) ;
                        }break;
                    }
                }
            };
        }

        public void sendMessage(int caseId, String uuid, String caller, byte data[]) {
            Message message = mHandler.obtainMessage(caseId);
            if ( data != null ) {
                Bundle bundle = new Bundle();
                bundle.putString("uuid", uuid) ;
                bundle.putString("caller", caller) ;
                bundle.putByteArray("data", data) ;
                message.setData(bundle);
            }
            message.sendToTarget();
        }

        public void sendMessage(int caseId, int delayMS)
        {
            Message message = mHandler.obtainMessage(caseId);
            mHandler.sendMessageDelayed(message, delayMS);
        }
    }
}
