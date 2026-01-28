package com.pesi.openqrcode;

import static com.pesi.openqrcode.BootBroadcastReceiver.CMD_RETURN_VALUE_FAIL;
import static com.pesi.openqrcode.BootBroadcastReceiver.CMD_RETURN_VALUE_SUCCESS;
import static com.pesi.openqrcode.BootBroadcastReceiver.CMD_TEST_CloseQRCode;
import static com.pesi.openqrcode.BootBroadcastReceiver.CMD_TEST_INITQRCode;
import static com.pesi.openqrcode.BootBroadcastReceiver.DTV_INTERFACE_NAME;
import static com.pesi.openqrcode.BootBroadcastReceiver.getRetValue;
import static com.pesi.openqrcode.BootBroadcastReceiver.invokeex;
import static com.pesi.openqrcode.BootBroadcastReceiver.server;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;


import static java.net.NetworkInterface.getNetworkInterfaces;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import vendor.prime.hardware.dtvservice.V1_0.IDtvService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
//    private static IDtvService server = null;

    TextView tvMAC_Android_ID;
    ImageView ivMAC_Android_ID;
    boolean ignoreCloseQRCode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setView(this);

       /* try {
            server = IDtvService.getService(true);
            server.hwInvoke()
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }*/
    }

    private void initView()
    {
        tvMAC_Android_ID = findViewById(R.id.mac_android_id_textview);
        ivMAC_Android_ID = findViewById(R.id.mac_android_id_imageview);
    }

    private void setView(Context context)
    {
        String mac = getMAC();
        String androidID = getAndroidID(context);
        tvMAC_Android_ID.setText(String.format(mac + "\n" + androidID));
        ivMAC_Android_ID.setImageBitmap(getQRCode(String.format(mac + "\n" + androidID)));
    }

    private String getMAC()
    {
        StringBuilder macStringBuilder = new StringBuilder();
        try
        {
            //macStringBuilder.append("Show displayname and MAC addr\n");
            for (NetworkInterface nif : Collections.list(getNetworkInterfaces()))
            {
                if (nif.getDisplayName().equals("eth0"))
                {
                    //macStringBuilder.append(nif.getDisplayName());
                    byte[] mac = nif.getHardwareAddress();
                    if (mac != null)
                    {
                        for (int i = 0; i < mac.length; ++i)
                            macStringBuilder.append(String.format("%02x", mac[i]));
                    }
                }

            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        return macStringBuilder.toString();
    }

    private String getAndroidID(Context context)
    {
        StringBuilder androidIDStringBuilder = new StringBuilder();
        ContentResolver r = context.getContentResolver();
        String ssaid = Settings.Secure.getString(r, Settings.Secure.ANDROID_ID);
        //androidIDStringBuilder.append("ANDROID ID: ");
        androidIDStringBuilder.append(ssaid);
        return androidIDStringBuilder.toString();
    }

    private Bitmap getQRCode(String codeString)
    {
        Bitmap bit = null;
        //修改Bitmap容錯度，解決SCANNER無法辨認的問題
        Map hints = new EnumMap(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        BarcodeEncoder encoder = new BarcodeEncoder();
        try
        {
            bit = encoder.encodeBitmap(codeString, BarcodeFormat.QR_CODE,
                    400, 400, hints);
        }
        catch (WriterException e)
        {
            e.printStackTrace();
        }
        return bit;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ignoreCloseQRCode = " + ignoreCloseQRCode);
        if (!ignoreCloseQRCode)
        {
            try {
                int flag = 0;
                if (server == null)
                    server = IDtvService.getService(true);
                Parcel request = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                request.writeString(DTV_INTERFACE_NAME);
                request.writeInt(CMD_TEST_CloseQRCode);
                invokeex(request, reply);
                int nRet = reply.readInt();
                Log.d(TAG, "onPause: int nRet = " + nRet);//success -> 0, failure -> -1
                nRet = getRetValue(nRet);
                if (nRet == CMD_RETURN_VALUE_SUCCESS)
                    Log.d(TAG, "onPause: CMD_TEST_CloseQRCode CMD_RETURN_VALUE_SUCCESS\n");
                else if (nRet == CMD_RETURN_VALUE_FAIL)
                    Log.d(TAG, "onPause: CMD_TEST_CloseQRCode CMD_RETURN_VALUE_FAIL\n");
                request.recycle();
                reply.recycle();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /*if (keyCode == KeyEvent.KEYCODE_0)
        {
            try {
                int flag = 0;
                if (server == null)
                    server = IDtvService.getService(true);
                Parcel request = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                request.writeString(DTV_INTERFACE_NAME);
                request.writeInt(CMD_TEST_INITQRCode);
                invokeex(request, reply);
                int nRet = reply.readInt();
                Log.d(TAG, "onKeyDown: int nRet = " + nRet);//success -> 0, failure -> -1
                nRet = getRetValue(nRet);
                if (nRet == CMD_RETURN_VALUE_SUCCESS)
                {
                    Log.d(TAG, "onKeyDown: CMD_TEST_INITQRCode CMD_RETURN_VALUE_SUCCESS\n");
                    ignoreCloseQRCode = true;
                }
                else if (nRet == CMD_RETURN_VALUE_FAIL)
                    Log.d(TAG, "onKeyDown: CMD_TEST_INITQRCode CMD_RETURN_VALUE_FAIL\n");
                request.recycle();
                reply.recycle();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }*/

        return super.onKeyDown(keyCode, event);
    }
}