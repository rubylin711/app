package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfoModule {
    private static final String TAG = "DeviceInfoModule";

    private static final int CMD_DEVICE_INFO_Base = PrimeDtvMediaPlayer.CMD_Base + 0x1600;

    //Device
    private static final int CMD_DEVICE_INFO_USB_PORT = CMD_DEVICE_INFO_Base + 0x01;
    private static final int CMD_DEVICE_INFO_WAKEUP_MODE = CMD_DEVICE_INFO_Base + 0x02;

    private static List<Integer> g_usb_port_list = null;

    public List<Integer> get_usb_port_list() {
        return g_usb_port_list;
    }

    //Device -start
    public void set_usb_port() {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_DEVICE_INFO_USB_PORT);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            g_usb_port_list = new ArrayList<>();
            int usbNum = reply.readInt();
            Log.d(TAG, "set_usb_port: " + usbNum);
            for (int i = 0; i < usbNum; i++) {
                g_usb_port_list.add(reply.readInt());
                Log.d(TAG, "set_usb_port: " + g_usb_port_list.get(i));
            }

        }

        request.recycle();
        reply.recycle();
    }

    //Device -end

    public int get_wakeup_mode() {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_DEVICE_INFO_WAKEUP_MODE);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            ret = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }
}
