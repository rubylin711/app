package com.pesi.openqrcode;

import static android.content.Intent.ACTION_BOOT_COMPLETED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import vendor.prime.hardware.dtvservice.V1_0.IDtvService;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "OpenQRCodeBootBroadcastReceiver";
    public static final String DTV_INTERFACE_NAME = "prime.dtv.IDtvService";
    public static final int CMD_TEST_OpenQRCode = 4073;
    public static final int CMD_TEST_CloseQRCode = 4074;
    public static final int CMD_TEST_INITQRCode = 4075;
    public static IDtvService server = null;
    public static DtvServiceCallback mCallback = null;
    /**
     * these define are used to determine success or fail by App
     * @see #CMD_RETURN_VALUE_SUCCESS
     * @see #CMD_RETURN_VALUE_FAIL
     */
    public static final int CMD_RETURN_VALUE_SUCCESS = 0;
    public static final int CMD_RETURN_VALUE_FAIL = -1;

    /**
     * the value is changed by PrepareDTV()
     * used to determine success or fail of command execution
     * @see # prepareDTV()
     * @see # prepareSuccess(int)
     */
    public static int PREPARE_SUCCESS = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d( TAG, "Broadcast Action : " + action );
        if (action.equals(ACTION_BOOT_COMPLETED))
        {
            Log.d(TAG, "onReceive: enter ACTION_BOOT_COMPLETED\n");
            if ( server == null )
            {
                try {
                    int flag = 0;
                    server = IDtvService.getService(true);
                    if(server != null)
                    {
                        mCallback = new DtvServiceCallback();
                        server.setCallback(mCallback);
                    }
                    Parcel request = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    request.writeString(DTV_INTERFACE_NAME);
                    request.writeInt(CMD_TEST_OpenQRCode);
                    invokeex(request, reply);
                    int nRet = reply.readInt();
                    Log.d(TAG, "onReceive: reply.readInt = " + nRet);//success -> 0, failure -> -1
                    nRet = getRetValue(nRet);

                    if (CMD_RETURN_VALUE_SUCCESS == nRet) {
                        Log.d(TAG, "get trigger QRCode flag: success pm0203");

                        flag = reply.readInt();//flag
                        Log.d(TAG, "pm0203onReceive: reply.readInt = " + flag);//1
                        Log.d(TAG, "onReceive: reply.readInt = " + reply.readInt());//2

                    }

                    request.recycle();
                    reply.recycle();

                    if (flag == 1)
                    {
                        // 創建一個 Intent 物件，指定要啟動的 Activity 類
                        Intent intentMain = new Intent(context, MainActivity.class);
                        intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        // 啟動 Activity
                        context.startActivity(intentMain);
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void invokeex(Parcel request, Parcel reply)
    {
        Log.d(TAG, "invokeex");
        try {
            if(server == null) {
                Log.e( TAG, "error !! , server = null" ) ;
            }
            else {
                int i = 0 ;
//gary20200507 fix client invoke int value to service not correct-s
                request.setDataPosition(0);

                Parcel tmp = Parcel.obtain();
                tmp.appendFrom(request, request.dataPosition(), request.dataAvail());
                tmp.setDataPosition(0);
                String name = tmp.readString();
                int cmd = tmp.readInt();
//                Log.d(TAG, "name = " + name);
//                Log.d(TAG, "cmd = " + cmd);
                request.setDataPosition(0);
                byte[] parcel_rawdata = request.marshall() ;
//                for (i = 0; i < parcel_rawdata.length; i++) {
//                    Log.d(TAG, "parcel_rawdata[" + i + "] = " + String.format("0x%08X",parcel_rawdata[i]));
//                }
                ArrayList<Integer> arr = new ArrayList<>();
                for(i = 0; i < parcel_rawdata.length; i++) {
                    arr.add((int) parcel_rawdata[i]);
//                    Log.d(TAG, "ArrayList<Integer>[" + i + "] = " + String.format("0x%08X",arr.get(i)));
                }
//                String rawString = new String(parcel_rawdata);
//                String returnData = server.hwInvoke(rawString) ;

                ArrayList<Integer> reply_arr;
                reply_arr = server.hwInvoke(arr) ;
                byte[] reply_data = new byte[reply_arr.size()];
                for(i = 0; i < reply_arr.size(); i++) {
//                    Log.d(TAG, "reply_arr[" + i + "] = " + String.format("0x%08X",reply_arr.get(i)));
                    reply_data[i] = (byte) reply_arr.get(i).intValue();
//                    Log.d(TAG, "reply_data[" + i + "] = " + String.format("0x%08X",reply_data[i]));
                }
                reply.unmarshall(reply_data,0,reply_arr.size());
//gary20200507 fix client invoke int value to service not correct-e
                reply.setDataPosition(0);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //Log.e(TAG, "invokeex");
        //int nRet = _invoke(request, reply);

        //if (0 != nRet && NOT_IMPLEMENT != nRet)
//        {
        //    request.recycle();
        //    reply.recycle();
        //    Log.e(TAG, "Dtvserver has crashed!");
//        throw new CInvokeRuntimeException("Dtvserver has crashed!");
//            try
//            {
//                _disconnect();
//                _setup(new WeakReference<HiDtvMediaPlayer>(this));
//                _setPlugins(mUri);
//            }
//            catch (IOException e)
//            {
//                Log.e(TAG, "invokeex exception");
//            }
//        }
    }

    public static int getRetValue(int ret)
    {
        if (ret == PREPARE_SUCCESS)
        {
            return CMD_RETURN_VALUE_SUCCESS;
        }
        else
        {
            Log.e(TAG, "getRetValue: FAILURE, ret = " + ret);
            return ret;
        }
    }

}

