package com.prime.dtv.module;

import android.os.Parcel;
import android.util.ArrayMap;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestModule {
    private static final String TAG = "TestModule";

    private static final int CMD_TEST_BASE = PrimeDtvMediaPlayer.CMD_Base + 0xF00;

    //Test
    private static final int CMD_TEST_START_INJECT = CMD_TEST_BASE + 0x01;
    private static final int CMD_TEST_STOP_INJECT = CMD_TEST_BASE + 0x02;
    private static final int CMD_TEST_FE_SET_FAKE_MODE = CMD_TEST_BASE + 0x03;
    private static final int CMD_TEST_GetGPIOStatus = CMD_TEST_BASE + 201;
    private static final int CMD_TEST_SetGPIOStatus = CMD_TEST_BASE + 202;
    private static final int CMD_TEST_GetATRStatus = CMD_TEST_BASE + 203;
    private static final int CMD_TEST_GetHDCPStatus = CMD_TEST_BASE + 204;
    private static final int CMD_TEST_PowerSave = CMD_TEST_BASE + 205;
    private static final int CMD_TEST_SevenSegment = CMD_TEST_BASE + 206;
    private static final int CMD_TEST_SET_CH = CMD_TEST_BASE + 207;
    private static final int CMD_TEST_CHANGE_TUNER = CMD_TEST_BASE + 208;//Scoty 20180817 add Change Tuner Command
    private static final int CMD_TEST_USB_READ_WRITE = CMD_TEST_BASE + 209;
    private static final int CMD_TEST_AV_MultiPlay = CMD_TEST_BASE + 210; // Johnny 20181221 for mtest split screen
    private static final int CMD_TEST_AV_StopByTunerId = CMD_TEST_BASE + 211; // Johnny 20190221 for mtest stop multi
    private static final int CMD_TEST_START_MTEST = CMD_TEST_BASE + 212; // Johnny 20190320 for mtest
    private static final int CMD_TEST_CONNECT_PCTOOL = CMD_TEST_BASE + 213; // Johnny 20190320 for mtest connect pctool
    private static final int CMD_TEST_WIFI_TX_RX_LEVEL = CMD_TEST_BASE + 214;//Scoty 20190417 add wifi level command
    private static final int CMD_TEST_CHECK_KEY = CMD_TEST_BASE + 215; // Johnny 20190522 check key before OTA
    private static final int CMD_TEST_GetHDMIStatus = CMD_TEST_BASE + 216;
    private static final int CMD_TEST_SetLEDOnOff = CMD_TEST_BASE + 217;
    private static final int CMD_TEST_FrontKey = CMD_TEST_BASE + 218;
    private static final int CMD_MOD_SetPowerLedColor = CMD_TEST_BASE + 219;
    private static final int CMD_MOD_SetEthLinkLedColor = CMD_TEST_BASE + 220;
    private static final int CMD_MOD_SetPPPoEWifiLedColor = CMD_TEST_BASE + 221;
    private static final int CMD_MOD_GetHDMISupportList = CMD_TEST_BASE + 222;
    private static final int CMD_MOD_SetHDMIResolution = CMD_TEST_BASE + 223;


    public int mtest_get_gpio_status(int u32GpioNo) {
        int ret;
        int bHighVolt = 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_GetGPIOStatus);
        request.writeInt(u32GpioNo);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            bHighVolt = reply.readInt();
            Log.d(TAG, "mtest_get_gpio_status = " + bHighVolt);
        }
        request.recycle();
        reply.recycle();
        return bHighVolt;
    }

    public int mtest_set_gpio_status(int u32GpioNo, int bHighVolt) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_SetGPIOStatus);
        request.writeInt(u32GpioNo);
        request.writeInt(bHighVolt);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int mtest_get_atr_status(int smartCardStatus) {
        int ret;
        int status = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_GetATRStatus);
        request.writeInt(smartCardStatus);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            status = reply.readInt();
            Log.d(TAG, "mtest_get_atr_status = " + status);
        }
        request.recycle();
        reply.recycle();
        return status;
    }

    public int mtest_get_hdcp_status() {
        int ret;
        int status = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_GetHDCPStatus);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            status = reply.readInt();
            Log.d(TAG, "mtest_get_hdcp_status = " + status);
        }
        request.recycle();
        reply.recycle();
        return status;
    }

    public int mtest_get_hdmi_status() {
        int ret;
        int status = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_GetHDMIStatus);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            status = reply.readInt();
            Log.d(TAG, "mtest_get_hdmi_status = " + status);
        }
        request.recycle();
        reply.recycle();
        return status;
    }

    public int mtest_power_save() {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_PowerSave);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int mtest_seven_segment(int enable) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_SevenSegment);
        request.writeInt(enable);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int mtest_set_led_on_off(int status) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_SetLEDOnOff);
        Log.d(TAG, "mtest_set_led_on_off: " + status);
        request.writeInt(status); // 0:off 1:green 2:red/orange

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);

    }

    public int mtest_get_front_key(int key) {
        int key_value = 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_FrontKey);
        Log.d(TAG, "mtest_get_front_key: " + key);
        request.writeInt(key); // 0:off 1:green 2:red/orange
        PrimeDtvMediaPlayer.invokeex(request, reply);
        if (reply.readInt() == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            Log.d(TAG, "key_value: " + key_value);
            key_value = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return key_value;

    }

    public int mtest_test_usb_read_write(int portNum, String path) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_USB_READ_WRITE);

        request.writeInt(portNum); // portNum by Jim
        request.writeString(path); // path

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    // Johnny 20181221 for mtest split screen
    public int mtest_test_av_multi_play(int tunerNum, List<Integer> tunerIDs, List<Long> channelIDs) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_AV_MultiPlay);

        request.writeInt(tunerNum); // total multiplay num, 1~4

        for (int i = 0; i < tunerNum; i++) {
            request.writeInt(tunerIDs.get(i)); // tunerID
            request.writeInt((int) (long) channelIDs.get(i)); // channelID
        }

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int mtest_test_av_stop_by_tuner_id(int tunerID) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_AV_StopByTunerId);

        request.writeInt(tunerID);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int mtest_start_mtest(String version) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_START_MTEST);

        request.writeString(version); // Johnny 20190909 send mtest apk version to service

        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int mtest_connect_pctool() {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_CONNECT_PCTOOL);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public List<Integer> mtest_get_wifi_tx_rx_level() {//Scoty 20190417 add wifi level command
        int ret;
        List<Integer> list = new ArrayList<>();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_WIFI_TX_RX_LEVEL);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            list.add(reply.readInt());//RF1
            list.add(reply.readInt());//RF0
        }
        request.recycle();
        reply.recycle();
        return list;
    }

    public Map<String, Integer> mtest_get_key_status_map() {
        int ret;
        Map<String, Integer> keyStatusMap = new ArrayMap<>();

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_CHECK_KEY);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            keyStatusMap.put("HDCP1.4 Key", reply.readInt()); // hdcp1.4
            keyStatusMap.put("Widevine Key", reply.readInt()); // Widevine
            keyStatusMap.put("Attestation Key", reply.readInt()); // Attestation
            keyStatusMap.put("Playready Key", reply.readInt()); // Playready
        } else {
            keyStatusMap.put("HDCP1.4 Key", -1); // hdcp1.4
            keyStatusMap.put("Widevine Key", -1); // Widevine
            keyStatusMap.put("Attestation Key", -1); // Attestation
            keyStatusMap.put("Playready Key", -1); // Playready
        }

        request.recycle();
        reply.recycle();
        return keyStatusMap;
    }

    public int test_set_tv_radio_count(int tvCount, int radioCount) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        Log.d(TAG, "test_set_tv_radio_count: ==>>> IN");
        request.writeInt(CMD_TEST_SET_CH);
        request.writeInt(tvCount);
        request.writeInt(radioCount);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int test_change_tuner(int tunerTpe)//Scoty 20180817 add Change Tuner Command
    {
        Log.d(TAG, "test_change_tuner: tunerTpe = " + tunerTpe);
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_CHANGE_TUNER);
        request.writeInt(tunerTpe);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }
}
