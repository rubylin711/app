package com.prime.logger;

import android.app.ActivityManager;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PrimeLogger.MainActivity";

    public static final String MSG_SERVICE =
        "PrimeLogger 狀態 ... ";
    public static final String MSG_README_DIR =
        "PrimeLogger 根據 USB 隨身碟內的目錄來決定存放的 STB 資訊，使用者可依個人需求建立下列目錄 \n" +
        "   * 建立 Pesi_Logcat 會擷取 logcat \n" +
        "   * 建立 Pesi_UserSystem 會擷取 dropbox、tombstone \n" +
        "   * 建立 Pesi_DeviceInfo 會擷取 property、recovery log、... \n" +
        "   * setprop persist.sys.prime.log.grep \"AAA|BBB|CCC\" 會根據 property 過濾 Logcat \n\n";
    public static final String MSG_README_STEP =
        "上述目錄建立完成後，請依照下列步驟抓取 STB 資料 \n" +
        "   1. 啟動 PrimeLogger 開啟背景 Service，再關閉 PrimeLogger (只需要啟動一次)\n" +
        "   2. 根據需求重現問題 \n" +
        "   3. 插入 USB 隨身碟並等待 1 分鐘 \n" +
        "   4. 1 分鐘後移除 USB 隨身碟，之後便可在目錄內可根據時間找到 STB 資料 \n" ;

    // public static final String MSG_TERMINATE = "terminated\n\n";
    // public static final String MSG_SERVICE = "This app start a service for coping STB data. Service status ... ";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start service
        Intent i = new Intent(this, PrimeLogger.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startService(i);

        // set status
        String status = "未啟動\n\n";
        if (isServiceRunning(PrimeLogger.class))
            status = "已啟動\n\n";

        // readme
        TextView viewReadme = findViewById(R.id.readme);
        viewReadme.setText(MSG_SERVICE + status + MSG_README_DIR + MSG_README_STEP);
    }

    private boolean isServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }
}