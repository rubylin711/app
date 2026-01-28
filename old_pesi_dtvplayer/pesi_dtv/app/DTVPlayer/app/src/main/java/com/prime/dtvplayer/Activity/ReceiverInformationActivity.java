package com.prime.dtvplayer.Activity;

import android.app.ActivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.MessageDialogView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;


/**
 * Created by edwin on 2017/11/17.
 */

public class ReceiverInformationActivity extends DTVActivity {
    private static final String TAG = "ReceiverInformationActivity";
    private TextView stbModel;
    private TextView clientID;
    private TextView serviceVersion;
    private TextView swVer;
    private TextView databaseVer;
    private TextView hwVer;
    private TextView loaderVer;
    private TextView cpuInfo;
    private TextView storageInfo;
    private TextView memoryInfoFree;
    private TextView memoryInfoTotal;
    private ActivityTitleView title;

    final Double[] preCPUtime = {0.0,0.0}; // connie 20180731 for box information
    CountDownTimer timer = null;

    private static class receiverInfo {
        static String stbModelVal    = "null";
        static String clientIDVal    = "null";
        static String serviceVersion = "null";
        static String swVerVal       = "null";
        static String databaseVerVal = "null";
        static String hwVerVal       = "null";
        static String loaderVerVal   = "null";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receiver_information);

        stbModel = (TextView) findViewById(R.id.idStbModelValue);
//        clientID = (TextView) findViewById(R.id.idClientIdVal);
        serviceVersion = (TextView) findViewById(R.id.idServiceVersionVal);
        swVer = (TextView) findViewById(R.id.idSwVerVal);
        databaseVer = (TextView) findViewById(R.id.idDatabaseVerValue);
        cpuInfo = (TextView) findViewById(R.id.cpuinfoTXV);
        storageInfo = (TextView) findViewById(R.id.storageInfoTXV);
        memoryInfoFree = (TextView) findViewById(R.id.memoryInfoFreeTXV);
        memoryInfoTotal = (TextView) findViewById(R.id.memoryInfoTotalTXV);
//        hwVer = (TextView) findViewById(R.id.idHwVerValue);
//        loaderVer = (TextView) findViewById(R.id.idLoaderVersionValue);

        InitTitle();
        StartTick();
        UpdateReceiverInfo();
    }

    private void InitTitle() {
        Log.d(TAG, "InitTitleHelp: ");
        title = (ActivityTitleView) findViewById(R.id.receiver_Info_Layout);
        title.setTitleView(getString(R.string.STR_RECEIVER_INFORMATION_TITLE));
    }

    private void UpdateReceiverInfo() {
        Log.d(TAG, "UpdateReceiverInfo()");
        if (true/*RevInfo.hasNewReceiverInfo*/) {
        /*
            receiverInfo.stbModelVal    = RevInfo.stbModelVal;
            receiverInfo.clientIDVal    = RevInfo.clientIDVal;
            receiverInfo.swVerVal       = RevInfo.swVerVal;
            receiverInfo.databaseVerVal = RevInfo.databaseVerVal;
            receiverInfo.hwVerVal       = RevInfo.hwVerVal;
            receiverInfo.loaderVerVal   = RevInfo.loaderVerVal;
        */

            receiverInfo.stbModelVal    = getString(R.string.STR_STB_MODEL_VALUE);
//            receiverInfo.clientIDVal    = getString(R.string.STR_CLIENT_ID_VALUE);
            receiverInfo.swVerVal       = GetApkSwVersion();
            receiverInfo.serviceVersion = GetPesiServiceVersion();
            receiverInfo.databaseVerVal = GposInfoGet().getDBVersion();
//            receiverInfo.hwVerVal       = getString(R.string.STR_HW_VERSION_VALUE);
//            receiverInfo.loaderVerVal   = getString(R.string.STR_LOADER_VERSION_VALUE);

            stbModel.setText(
                    receiverInfo.stbModelVal);
//            clientID.setText(
//                    receiverInfo.clientIDVal);
            swVer.setText(
                    receiverInfo.swVerVal);
            serviceVersion.setText(receiverInfo.serviceVersion);
            databaseVer.setText(
                    receiverInfo.databaseVerVal);
//            hwVer.setText(
//                    receiverInfo.hwVerVal);
//            loaderVer.setText(
//                    receiverInfo.loaderVerVal);

        }
            /*
        */

        // connie 20180731 for box information -s

        showInternalMemoryInfo();
        showRamInfo();

        // connie 20180731 for box information -e
    }

    // connie 20180731 for box information -s
    /**
     * get CPU Info (console:/ # setenforce 0)
     * @param cpuTime cpu time is set after parse /proc/stat
     * @return true if /proc/stat is accessed
     */
    private static int  getProcessCpuAction(final Double[] cpuTime)
    {
        //String cpuPath = "/proc/" + pid + "/stat";
        Log.d(TAG, "getProcessCpuAction: In");
        String cpuPath = "/proc/" + "/stat";
        String cpu = "";
        int result = 0;
        File f = new File(cpuPath);
        if (!f.exists() || !f.canRead())
        {
            return result;
        }

        FileReader fr = null;
        BufferedReader localBufferedReader = null;

        try {
            fr = new FileReader(f);
            localBufferedReader = new BufferedReader(fr, 8192);
            cpu = localBufferedReader.readLine();
            String[] cpuInfos = null;

            if (null != cpu)
            {
                Log.d(TAG, "getProcessCpuAction: cpu = " + cpu);
                cpuInfos = cpu.split(" ");
                //for(int i = 0;i<cpuInfos.length; i++)
                //{
                //    Log.d("ViewActivity", "cpuInfos[ = "+i+"] =" + cpuInfos[i]);
                //}
                cpuTime[0] = Double.parseDouble(cpuInfos[2])
                        + Double.parseDouble(cpuInfos[3]) + Double.parseDouble(cpuInfos[4])
                        + Double.parseDouble(cpuInfos[6]) + Double.parseDouble(cpuInfos[5])
                        + Double.parseDouble(cpuInfos[7]) + Double.parseDouble(cpuInfos[8]);
                cpuTime[1]= Double.parseDouble(cpuInfos[5]);
                result = 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //FileUtil.closeReader(localBufferedReader);
        return result;
    }


    void showInternalMemoryInfo()
    {
        //long totalInternalValue = getTotalInternalMemorySize();
        long freeInternalValue = getAvailableInternalMemorySize();
        //long usedInternalValue = totalInternalValue - freeInternalValue;
        String Free = freeInternalValue/1024/1024+"  MB";
        Log.d(TAG, "showInternalMemoryInfo:  Free Size = " + freeInternalValue/1024/1024+"  MB");
        //Log.d(TAG, "showInternalMemoryInfo:  Total Size = " + totalInternalValue/1024/1024+"  MB");
        storageInfo.setText(Free);
    }

    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
    }
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    public void showRamInfo()
    {
        long totalRamValue = totalRamMemorySize();
        long freeRamValue = freeRamMemorySize();
        Log.d(TAG, "showRamInfo:    totalRamValue = " + totalRamValue + "    freeRamValue = " +freeRamValue);
        memoryInfoFree.setText(freeRamValue+" MB");
        memoryInfoTotal.setText(totalRamValue+" MB");
    }

    private long totalRamMemorySize() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.totalMem / 1048576L; //(1024*1024)
        return availableMegs;
    }
    private long freeRamMemorySize() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;

        return availableMegs;
    }
    // connie 20180731 for box information -e

    public void StartTick()
    {
        Log.d(TAG, "StartBannerTick: ");

        timer = new CountDownTimer(
                500, 250)
        {
            @Override
            public void onTick(long l)
            {
                final Double[] cpuTimeCur = {0.0,0.0};
                double cpuRate ;
                Log.d(TAG, "onTick: StartBannerTick In getProcessCpuAction");
                getProcessCpuAction(cpuTimeCur);
                if(preCPUtime[0] != 0)
                {
                    Log.d(TAG, "StartBannerTick: preCPUtime ===> total =  " + preCPUtime[0] + "    idle = " + preCPUtime[1]);
                    Log.d(TAG, "StartBannerTick: preCPUtime ===> total =  " + cpuTimeCur[0] + "    idle = " + cpuTimeCur[1]);
                    double totaldiff = cpuTimeCur[0]-preCPUtime[0];
                    double idlediff = cpuTimeCur[1]-preCPUtime[1];
                    cpuRate = ((totaldiff - idlediff) / totaldiff)*100;

                    DecimalFormat df = new DecimalFormat("##.00");
                    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
                    dfs.setDecimalSeparator('.');
                    df.setDecimalFormatSymbols(dfs);

                    Log.d(TAG, "StartBannerTick: cpuRate = " + df.format(cpuRate) + " %");
                    cpuInfo.setText(df.format(cpuRate)+"%");
                }

                preCPUtime[0] =cpuTimeCur[0];
                preCPUtime[1] =cpuTimeCur[1];
            }
            public void onFinish() {}
        }.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//Scoty 20180817 add Change Tuner Command

        switch(keyCode)
        {
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
            {
                if(GetCurTunerType() == TpInfo.DVBC)
                    TestChangeTuner(TpInfo.DVBS);
                if(GetCurTunerType() == TpInfo.DVBS)
                    TestChangeTuner(TpInfo.DVBC);
                String str = getString(R.string.STR_PLEASE_WAIT_TEN_SECS);
                new MessageDialogView(this, str, 10000) {
                    public void dialogEnd() {
                    }
                }.show();

            }break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
