package com.prime.dtv.module;

import android.os.Parcel;

import com.prime.dtv.PrimeDtvMediaPlayer;

public class PioModule {
    private static final String TAG = "PioModule";

    private static final int CMD_PIO_BASE = PrimeDtvMediaPlayer.CMD_Base + 0x1400;

    //PIO
    private static final int CMD_PIO_SetAntennaPower = CMD_PIO_BASE + 0x01;
    private static final int CMD_PIO_SetBuzzer = CMD_PIO_BASE + 0x02;
    private static final int CMD_PIO_SetLedRed = CMD_PIO_BASE + 0x03;
    private static final int CMD_PIO_SetLedGreen = CMD_PIO_BASE + 0x04;
    private static final int CMD_PIO_SetLedOrange = CMD_PIO_BASE + 0x05;
    private static final int CMD_PIO_SetUsbPower = CMD_PIO_BASE + 0x06;
    private static final int CMD_PIO_SetLedWhite = CMD_PIO_BASE + 0x07;


    public int set_antenna_5v(int tunerID, int tunerType, int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetAntennaPower);

        request.writeInt(tunerID); // tuner id
        request.writeInt(tunerType); // tuner type
        request.writeInt(enable); // enable

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int set_buzzer(int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetBuzzer);

        request.writeInt(enable); // enable

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int set_led_red(int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetLedRed);

        request.writeInt(enable); // enable

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int set_led_green(int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetLedGreen);

        request.writeInt(enable); // enable

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int set_led_orange(int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetLedOrange);

        request.writeInt(enable); // enable

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int set_led_white(int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetLedWhite);

        request.writeInt(enable); // enable

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int set_usb_power(int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetUsbPower);

        request.writeInt(enable); // enable

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }
}
