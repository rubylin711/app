package com.prime.android.tv.otaservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import android.os.Message;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BootUpReceiver extends BroadcastReceiver {
    private final String TAG = "BootUpReceiver";
    private final String SaveSpaceFile = "SaveSpace";
    private final int File_KB = 1024;
    private final int Min_Save_Size = 200*1024;
//    private HandlerThread mThread;
//    private Handler mThreadHandler;
//    private Thread mPowerKeyThread;
//    private final String POWKER_MODE = "persist.prop.InitPowerMode";//0 : Normal Mode 1 : Standby Mode
    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        /* 同一個接收者可以收多個不同行為的廣播
           所以可以判斷收進來的行為為何，再做不同的動作 */
		   Log.d(TAG, "Received action = [ "+ intent.getAction() +"]");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            /* 收到廣播後開啟目的Service */
            Log.d(TAG, "Received ACTION_BOOT_COMPLETED ==> Start Download Service now");

            Intent startServiceIntent = new Intent(context, DownloadService.class);

//            PropertyUtils mProperty = new PropertyUtils();

//            Log.d(TAG, "power key thread:  send Power");
//            mThread = new HandlerThread("power");
//            mThread.start();
//            mThreadHandler=new Handler(mThread.getLooper());
//            mThreadHandler.post(PowerKeyRun);

//            if(mProperty.get(POWKER_MODE,"0").equals("1")) {
//                Log.d(TAG, "open Power Mode Thread");
//                mPowerKeyThread = new Thread(PowerKeyRun);
//                mPowerKeyThread.start();
//            }

            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                Log.d(TAG, "call startForegroundService");
                context.startForegroundService(startServiceIntent);
            } else {
                Log.d(TAG, "call startService");
                context.startService(startServiceIntent);
            }

            //Write 200K files for Android OTA Update rules
            WriteFiles(Min_Save_Size,Environment.getExternalStorageDirectory()+"/"+SaveSpaceFile);
        }
    }

    private boolean isOTASavedFileExist(File file)
    {
        if (!file.exists()) { 
            return false;
        }
        return true;
    }

    private void WriteFiles(int filesize, String path)
    {
        Log.d(TAG, "writeFile: ====>> Running path =  ["+path+"]");
        try {
            Log.d(TAG, "writeFile: ====>> Running 0 ");

            File file = new File(path);
            if(!isOTASavedFileExist(file))
            {
                file.createNewFile();
            }else{
                return;//if file exist, no need to create new
            }

            FileOutputStream fops = new FileOutputStream(file);
            FileChannel channel = fops.getChannel();
           /* int count = filesize/File_KB;
            for(int i = 0 ; i < count ; i++)
            {
                ByteBuffer buffer = ByteBuffer.allocate(File_KB);
                channel.write(buffer);

            }*/

            ByteBuffer buffer = ByteBuffer.allocate(filesize);
            channel.write(buffer);
            fops.close();

        } catch (IOException e) {
            throw new RuntimeException("Something went wrong : " + e.getMessage(), e);
        } 
        
    }

//    private final Runnable PowerKeyRun = new Runnable() {
//        @Override
//        public void run() {
//            Instrumentation inst=new Instrumentation();
//            Log.d(TAG, "send Power key to Standby");
//            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);
//        }
//    };

//    public class PropertyUtils {
//
//        private volatile Method set = null;
//        private volatile Method get = null;
//        /*
//                public void set(String prop, String value) {
//
//                    try {
//                        if (null == set) {
//                            synchronized (PropertyUtils.class) {
//                                if (null == set) {
//                                    Class<?> cls = Class.forName(getString(R.string.method_property));
//                                    set = cls.getDeclaredMethod(getString(R.string.set_property), new Class<?>[]{String.class, String.class});
//                                }
//                            }
//                        }
//                        set.invoke(null, new Object[]{prop, value});
//                    } catch (Throwable e) {
//                        e.printStackTrace();
//                    }
//                }
//        */
//        public String get(String prop, String defaultvalue) {
//            String value = defaultvalue;
//            try {
//                if (null == get) {
//                    synchronized (PropertyUtils.class) {
//                        if (null == get) {
//                            Class<?> cls = Class.forName("android.os.SystemProperties");
//                            get = cls.getDeclaredMethod("get", new Class<?>[]{String.class, String.class});
//                        }
//                    }
//                }
//                value = (String) (get.invoke(null, new Object[]{prop, defaultvalue}));
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//            return value;
//        }
//    }

}
