package com.mtest.module;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.storage.StorageManager;
import android.util.Log;

import com.mtest.config.MtestConfig;
import com.mtest.utils.PesiStorageHelper;
import com.prime.dtvplayer.Activity.DTVActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsbModule
{
    private static final String TAG = "USBModule";
    private final WeakReference<Context> mContRef;

    public static final int PORT_1 = 1;
    public static final int PORT_2 = 3;
    private static final String PORT_1_STR = "usb1";
    private static final String PORT_2_STR = "usb3";

    public UsbModule(Context context)
    {
        mContRef = new WeakReference<>(context);
    }

    public List<Integer> getPortList()
    {
        DTVActivity activity = (DTVActivity) mContRef.get();
        List<Integer> pesiUsbPortList = activity.GetUsbPortList();
        if (pesiUsbPortList == null)
            pesiUsbPortList = new ArrayList<>();
        return pesiUsbPortList;
    }

    public void readWriteUSB(int portNumber, String path)
    {
        DTVActivity activity = (DTVActivity) mContRef.get();
        activity.MtestTestUsbReadWrite(portNumber, path);
    }

    public int checkUsbReadWrite(int port)
    {
        UsbManager usbManager = (UsbManager) mContRef.get().getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        int deviceSize = deviceList.size();

        Log.d(TAG, "checkUsbReadWrite: " + deviceSize + " USB device(s) found");
        if (deviceSize <= 0)
            return MtestConfig.TEST_RESULT_FAIL;

        for (UsbDevice usbDevice : deviceList.values())
        {
            String deviceName = usbDevice.getDeviceName();
            int busNumber = Integer.parseInt(deviceName.substring(13, 13+3));

            Log.d(TAG, "checkUsbReadWrite: busNumber = " + busNumber + " , port = " + port);
            if (busNumber == port)
                return testReadWrite(getInternalPath(port));
        }

        return MtestConfig.TEST_RESULT_FAIL;
    }

    public int checkUsbMounted(int port)
    {
        UsbManager usbManager = (UsbManager) mContRef.get().getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        int deviceSize = deviceList.size();

        Log.d(TAG, "checkUsbMounted: " + deviceSize + " USB device(s) found");
        if (deviceSize <= 0)
            return MtestConfig.TEST_RESULT_FAIL;

        for (UsbDevice device : deviceList.values())
        {
            String deviceName = device.getDeviceName();
            int busNumber = Integer.parseInt(deviceName.substring(13, 13+3));

            Log.d(TAG, "checkUsbMounted: busNumber = " + busNumber + " , port = " + port);
            if (busNumber == port)
            {
                return MtestConfig.TEST_RESULT_PASS;
            }
        }
        return MtestConfig.TEST_RESULT_FAIL;
    }

    private int testReadWrite(String internalPath)
    {
        String testStr = "test read / test write",
                readStr = null;
        try
        {
            File file = new File(internalPath, "test_read_write.txt");
            testWrite(file, testStr);
            readStr = testRead(file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (readStr == null)
            return MtestConfig.TEST_RESULT_FAIL;

        if (readStr.equals(testStr))
            return MtestConfig.TEST_RESULT_PASS;
        else
            return MtestConfig.TEST_RESULT_FAIL;
    }

    private void testWrite(File file, String str)
    {
        OutputStream os;
        try
        {
            os = new FileOutputStream(file, false);
            os.write(str.getBytes());
            os.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String testRead(File file)
    {
        FileReader fr;
        String str = null;
        try
        {
            if (file.exists())
            {
                fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                str = br.readLine();
                br.close();
                file.delete();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return str;
    }

    public boolean checkIsMounted(int usbIndex)
    {
        DTVActivity activity = (DTVActivity) mContRef.get();
        StorageManager storageMgr = (StorageManager) activity.getSystemService(Context.STORAGE_SERVICE);
        boolean mounted = false;

        if (storageMgr == null)
            return false;

        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageMgr);

        List<Object> volumeInfoList = pesiStorageHelper.getVolumes();
        List<Integer> pesiUsbPortList = getPortList();

        for (Object volumeInfo : volumeInfoList)
        {
            int portNo = pesiStorageHelper.getUsbPortNum(volumeInfo);
            int index = pesiUsbPortList.indexOf(portNo);
            if (index == usbIndex)
            {
                mounted = true;
                break;
            }
        }
        return mounted;
    }

    private String getInternalPath(int port)
    {
        StorageManager storageManager = (StorageManager)mContRef.get().getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper storageHelper = new PesiStorageHelper(storageManager);
        String portStr = null;

        if (port == PORT_1)
            portStr = PORT_1_STR;
        else if (port == PORT_2)
            portStr = PORT_2_STR;

        if (portStr != null)
        {
            for (Object volumeInfo : storageHelper.getVolumes())
            {
                Object diskObj = storageHelper.getDisk(volumeInfo);
                if (diskObj == null)
                    continue;

                String diskInfo = diskObj.toString();
                if (diskInfo.contains(portStr))
                    return storageHelper.getInternalPath(volumeInfo);
            }
        }

        return null;
    }
}
