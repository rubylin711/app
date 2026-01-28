package com.mtest.config;

import android.content.Context;
import android.content.Intent;
import android.os.storage.StorageManager;
import android.util.Log;
import android.widget.TextView;

import com.mtest.activity.Global_Variables;
import com.mtest.activity.MainActivity;
import com.mtest.module.PesiSharedPreference;
import com.mtest.utils.PesiStorageHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HwTestConfig {
    public static final String TAG = "HwTestConfig";

    public static final String HW_TEST_CONFIG_ONOFF_STRING = "HW_TEST_CONFIG_ONOFF";
    public static final String HW_TEST_CONFIG_UDP_URL_STRING = "UDP_URL"; // Edwin 20200508 HwTestConfig parse url, ip
    public static final String HW_TEST_CONFIG_STB_IP_STRING = "STB_IP"; // Edwin 20200511 disable stb ip // Edwin 20200508 HwTestConfig parse url, ip
    public static final String HW_TEST_CONFIG_DEST_IP_STRING = "DEST_IP"; // Edwin 20200508 HwTestConfig parse url, ip
    public static final String HW_TEST_CONFIG_TUNER_TEST_STRING = "TUNER_TEST";
    public static final String HW_TEST_CONFIG_LED_TEST_STRING = "LED_TEST";
    public static final String HW_TEST_CONFIG_SC_TEST_STRING = "SC_TEST";
    public static final String HW_TEST_CONFIG_USB_TEST_STRING = "USB_TEST";
    public static final String HW_TEST_CONFIG_WIFI_TEST_STRING = "WIFI_TEST";
    public static final String HW_TEST_CONFIG_BT_TEST_STRING = "BT_TEST";
    public static final String HW_TEST_CONFIG_POWER_TEST_STRING = "POWER_TEST";
    public static final String HW_TEST_CONFIG_SATA_TEST_STRING = "SATA_TEST";
    public static final String HW_TEST_CONFIG_SD_TEST_STRING = "SD_TEST";
    public static final String HW_TEST_CONFIG_ETHERNET_TEST_STRING = "ETHERNET_TEST";
    public static final String HW_TEST_CONFIG_DISEQC_TEST_STRING = "DISEQC_TEST";
    public static final String HW_TEST_CONFIG_LNB_TEST_STRING = "LNB_TEST";
    public static final String HW_TEST_CONFIG_LONG_TERM_TEST_STRING = "LONG_TERM_TEST";
    public static final String HW_TEST_CONFIG_THERMAL_TEST_STRING = "THERMAL_TEST";
    public static final String HW_TEST_CONFIG_FLASH_TEST_STRING = "FLASH_TEST";
    public static final String HW_TEST_CONFIG_DDR_TEST_STRING = "DDR_TEST";
    public static final String HW_TEST_CONFIG_TOTAL_COUNT = "count";
    public static final String HW_TEST_CONFIG_PASS_COUNT = "passCount";
    public static final String HW_TEST_CONFIG_LONG_TERM_TOTAL_COUNT = "count_LongTerm";
    public static final String HW_TEST_CONFIG_LONG_TERM_PASS_COUNT = "passCount_LongTerm";
    public static final String HW_TEST_CONFIG_DDR_TOTAL_COUNT = "ddrCount";
    public static final String HW_TEST_CONFIG_DDR_PASS_COUNT = "ddrPass";
    public static final String HW_TEST_CONFIG_FLASH_TOTAL_COUNT = "flashCount";
    public static final String HW_TEST_CONFIG_FLASH_PASS_COUNT = "flashPass";
    public static final String HW_TEST_CONFIG_SD_TOTAL_COUNT = "sdCount";
    public static final String HW_TEST_CONFIG_SD_PASS_COUNT = "sdPass";
    public static final String HW_TEST_CONFIG_FROM_USB = "getHwTestConfigFromUSB";

    // Reset Default Test
    public static final int RUN_RESET_DEFAULT_TEST = 4;
    public static final String ACTION_FACTORY_RESET = "android.intent.action.FACTORY_RESET";
    public static final String EXTRA_REASON = "android.intent.extra.REASON";
    public static final String SHUTDOWN_INTENT_EXTRA = "shutdown";

    /* HW_TEST_CONFIG_ONOFF */
    private int HW_TEST_CONFIG_ONOFF=0;

    /* UDP_URL */
    private String UDP_URL=null; // Edwin 20200508 HwTestConfig parse url, ip

    /* STB_IP */
    //private String STB_IP=null; // Edwin 20200511 disable stb ip // Edwin 20200508 HwTestConfig parse url, ip

    /* DEST_IP */
    private String DEST_IP=null; // Edwin 20200508 HwTestConfig parse url, ip

    /* TUNER_TEST
    0:off
    1:1TP,Production mode
    2:2TP*/
    private int TUNER_TEST=0;

    /* LED_TEST
    0:off
    1:Production mode
    2:LED on/off*/
    private int LED_TEST=0;

    /* SC_TEST
    0:off
    1:ATR one time,Production mode
    2:Continuous ATR*/
    private int SC_TEST=0;

    /* USB_TEST
    0:off
    1:R/W one time,Production mode
    2:Continuous R/W */
    private int USB_TEST=0;

    /* WIFI_TEST
    0:off
    1:connect AP,Production mode
    2:Continuous scan */
    private int WIFI_TEST=0;

    /* BT_TEST
    0:off
    1:connect AP,Production mode
    2:Continuous scan */
    private int BT_TEST=0;

    /* POWER_TEST
    0:off
    1:Tool, Production mode
    2:SW reboot
    3:STR
    4:Reset Default */
    private int POWER_TEST=0;

    /* SATA_TEST
    0:off
    1:R/W one time,Production mode
    2:Continuous R/W */
    private int SATA_TEST=0;

    /* SD_TEST
    0:off
    1:R/W one time,Production mode
    2:Continuous R/W */
    private int SD_TEST=0;

    /* ETHERNET_TEST
    0:off
    1:ping one time,Production mode
    2:Continuous ping */
    private int ETHERNET_TEST=0;

    /* DISEQC_TEST
    0:off
    1:Production mode
    2:Continuous change */
    private int DISEQC_TEST=0;

    /* LNB_TEST
    0:off
    1:Production mode
    2:Continuous change */
    private int LNB_TEST=0;

    /* LONG_TERM_TEST
    0:off
    1:Wait 10 mins, show PASS/FAIL */
    private int LONG_TERM_TEST=0;

    /* THERMAL_TEST
    0:off
    1:add loading , Ex UI redraw */
    private int THERMAL_TEST=0;

    /* FLASH_TEST
    0:off
    1:Continuous R/W */
    private int FLASH_TEST=0;

    /* DDR_TEST
    0:off
    1:Continuous R/W */
    private int DDR_TEST=0;

    private boolean showLog=false;

    private static HwTestConfig mHwTestConfig = null;

    private Context mContext;
    private MountCallback mCallback;

    private TextView mModelTV;

    private boolean fileFound = false; // Edwin 20200508 HwTestConfig parse url, ip

    private PesiSharedPreference mHwConfigSharedPreference;

    public MountCallback getmCallback() {
        return mCallback;
    }

    private static class MtestIni {
        public String item;
        public int value;
        String addr; // Edwin 20200508 HwTestConfig parse url, ip
    }

    public interface Callback {
        public void MountCallback(Object vol);
    }

    public class MountCallback implements Callback {

        @Override
        public void MountCallback(Object vol) {
            try {
                getHwTestConfigFromUSB(vol);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
    public HwTestConfig(Context context,int bootComplete) {
        mContext = context;
        mHwConfigSharedPreference = new PesiSharedPreference(context, PesiSharedPreference.NAME_HARDWARE_CONFIG);
        HwtestConfigSetDefault(bootComplete);
        mCallback = new MountCallback();
    }

    public void setConfigFromUsbFlag(int value) {
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_FROM_USB,value);
        mHwConfigSharedPreference.save();
    }

    public int getConfigFromUsbFlag() {
        int flag = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_FROM_USB,0);
        Log.d(TAG,"usbFlag = "+flag);
        return flag;
    }
    public void HwtestConfigDefault() {
        HW_TEST_CONFIG_ONOFF = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_ONOFF_STRING,0);
        UDP_URL = mHwConfigSharedPreference.getString(HW_TEST_CONFIG_UDP_URL_STRING,MainActivity.DEFAULT_URL); // Edwin 20200508 HwTestConfig parse url, ip
        //STB_IP = mHwConfigSharedPreference.getString("STB_IP","10.1.4.183"); // Edwin 20200511 disable stb ip // Edwin 20200508 HwTestConfig parse url, ip
        DEST_IP = mHwConfigSharedPreference.getString(HW_TEST_CONFIG_DEST_IP_STRING,MainActivity.DEFAULT_PING_IP); // Edwin 20200508 HwTestConfig parse url, ip
        TUNER_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_TUNER_TEST_STRING,1);
        LED_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_LED_TEST_STRING,1);
        SC_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_SC_TEST_STRING,1);
        USB_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_USB_TEST_STRING,1);
        WIFI_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_WIFI_TEST_STRING,1);
        BT_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_BT_TEST_STRING,1);
        POWER_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_POWER_TEST_STRING,0);
        SATA_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_SATA_TEST_STRING,1);
        SD_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_SD_TEST_STRING,1);
        ETHERNET_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_ETHERNET_TEST_STRING,1);
        DISEQC_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_DISEQC_TEST_STRING,1);
        LNB_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_LNB_TEST_STRING,1);
        LONG_TERM_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_LONG_TERM_TEST_STRING,0);
        THERMAL_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_THERMAL_TEST_STRING,0);
        FLASH_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_FLASH_TEST_STRING,0);
        DDR_TEST = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_DDR_TEST_STRING,0);
    }

    public void HwtestConfigSetNotHWConfig() {
        mHwConfigSharedPreference.clear();
        mHwConfigSharedPreference.save();
//        setConfigFromUsbFlag(1);
        HwtestConfigDefault();
    }

    public void HwtestConfigSetDefault(int bootComplete) {
//        if(bootComplete == 1)
//            setConfigFromUsbFlag(0);
        HwtestConfigDefault();
    }

    public final static HwTestConfig getInstance(Context context,int bootComplete) {
        if(mHwTestConfig == null) {
            mHwTestConfig = new HwTestConfig(context,bootComplete);
        }
        return mHwTestConfig;
    }

    // edwin 20200527 implement Flash Test -s
    private List<Object> getVolumeList()
    {
        StorageManager storageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
        return pesiStorageHelper.getVolumes();
    }

    public void writeFlashTestLog (String log, boolean pass)
    {
        int count = 0;
        int passCount = 0;

        count = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_FLASH_TOTAL_COUNT, 0);
        passCount = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_FLASH_PASS_COUNT, 0);

        count++;
        passCount = pass ? (passCount + 1) : (passCount);
        log += ("Total Pass Count : " + passCount + "\n");
        log += ("Test Count : " + count + "\n");
        //preferences.edit().putInt(HW_TEST_CONFIG_FLASH_TOTAL_COUNT, count).apply();

        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_FLASH_TOTAL_COUNT, count);
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_FLASH_PASS_COUNT, passCount);
        mHwConfigSharedPreference.save();
        writeLogInAPK(log,pass,"hwReport_flashTest.txt");
    }
    // edwin 20200527 implement Flash Test -e
    public void writeSDTestLog (String log, boolean pass)//centaur 20200619 fix mtest
    {
        int count = 0;
        int passCount = 0;
        count = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_SD_TOTAL_COUNT, 0);
        passCount = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_SD_PASS_COUNT, 0);

        count++;
        passCount = pass ? (passCount + 1) : (passCount);
        log += ("Total Pass Count : " + passCount + "\n");
        log += ("Test Count : " + count + "\n");
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_SD_TOTAL_COUNT, count);
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_SD_PASS_COUNT, count);
        mHwConfigSharedPreference.save();
        Log.d(TAG, "===log= " + log );
        writeLogInAPK(log,pass,"hwReport_sdTest.txt");
    }
    public void writeDDRTestLog (String log, boolean pass) // edwin 20200527 implement DDR Test
    {
        int count = 0;
        int passCount = 0;
        count = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_DDR_TOTAL_COUNT, 0);
        passCount = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_DDR_PASS_COUNT, 0);

        count++;
        passCount = pass ? (passCount + 1) : (passCount);
        log += ("Total Pass Count : " + passCount + "\n");
        log += ("Test Count : " + count + "\n");
        //Log.d(TAG, "writeDDRTestLog: log = \n"+log);
        //preferences.edit().putInt(HW_TEST_CONFIG_DDR_TOTAL_COUNT, count).apply();
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_DDR_TOTAL_COUNT, count);
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_DDR_PASS_COUNT, passCount);
        mHwConfigSharedPreference.save();
        writeLogInAPK(log,pass,"hwReport_ddrTest.txt");
    }

    public void writeLogLongTerm (String log, boolean pass) // Edwin 20200602 for long term test
    {
        showLog = true;

        int count = 0;
        int passCount = 0;
        count = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_LONG_TERM_TOTAL_COUNT, 0);
        passCount = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_LONG_TERM_PASS_COUNT, 0);
        count++;
        passCount = pass ? (passCount + 1) : (passCount);
        log += ("Total Pass Count : " + passCount);
        log += ("\n");
        log += ("Test Count : " + count);
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_LONG_TERM_TOTAL_COUNT, count);
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_LONG_TERM_PASS_COUNT, passCount);
        mHwConfigSharedPreference.save();
        writeLogInAPK(log,pass,"hwReport_LongTermTest.txt");
    }

    public void writeLogBootStable(String log,boolean pass) {
        StorageManager storageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        showLog = true;

        int count = 0;
        int passCount = 0;

        count = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_TOTAL_COUNT, 0);
        passCount = mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_PASS_COUNT, 0);
        count++;
        passCount = pass ? (passCount + 1) : (passCount);
        log += ("Total Pass Count : " + passCount);
        log += ("\n");
        log += ("Test Count : " + count);

        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_TOTAL_COUNT, count);
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_PASS_COUNT, passCount);
        mHwConfigSharedPreference.save();
        writeLogInAPK(log,pass,"hwReport.txt");
    }

    public void writeLogInAPK(String log,boolean pass,String fileName) {
          try {
            //writeLog(vol,log,pass);

            File file = new File(mContext.getFilesDir() + "/" + fileName);
            Log.d(TAG, "writeLog: "+mContext.getFilesDir() + "/" + fileName);
            if (file.exists() == false) {
                Log.d(TAG, "writeLog: createNewFile");
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file, true);
            FileWriter fw = new FileWriter(fos.getFD());
            BufferedWriter bw = new BufferedWriter(fw);
            if (showLog) {
                Log.d(TAG, log);
                showLog = false;
            }
            bw.write(log);
            bw.newLine();
            bw.close();
            fw.close();
            fos.getFD().sync();
            fos.close();
                //writeLogDebug(vol.path, count);
                //writeLogcat(vol.path, count);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public boolean getHwTestConfigFromUSB(Object vol) throws Throwable {
        boolean update = false;
        StorageManager storageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
//        if(vol != null ) {//gary20200508 add update hw mtest config then should reboot stb
        if(vol != null/* && getConfigFromUsbFlag() == 0*/) {
            String id = pesiStorageHelper.getId(vol);
            String path = pesiStorageHelper.getPath(vol);
            Log.d(TAG, "getHwTestConfigFromUSB: id = " + id);
            if (id.startsWith("public")) {
                Log.d(TAG, "getHwTestConfigFromUSB: is public = " + path);
                if(path != null) {
                    // Edwin 20200508 HwTestConfig parse url, ip -s
                    File file = new File(path, "mtest_ini.dat");
                    List<MtestIni> listMtest = new ArrayList<>();
                    String line, strValue;
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("#") && line.length() != 0) {
                            MtestIni mtestIni = new MtestIni();
                            mtestIni.item = line.substring(0, line.indexOf("="));
                            strValue = line.substring(line.indexOf("=") + 1);

                            try {
                                if (mtestIni.item.equals(HW_TEST_CONFIG_UDP_URL_STRING)
                                || mtestIni.item.equals(HW_TEST_CONFIG_DEST_IP_STRING))
                                    mtestIni.addr = strValue;
                                else if (mtestIni.item.equals(HW_TEST_CONFIG_STB_IP_STRING))
                                    Log.d(TAG, "getHwTestConfigFromUSB: skip STB_IP");
                                else
                                    mtestIni.value = Integer.parseInt(strValue);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            listMtest.add(mtestIni);
                        }
                    }
                    reader.close();
                    for (int i = 0; i < listMtest.size(); i++) {
                        Log.d(TAG, "getHwTestConfigFromUSB: item = " + listMtest.get(i).item + " , value = " + listMtest.get(i).value + " , addr = " + listMtest.get(i).addr);
                        if (listMtest.get(i).item.equals(HW_TEST_CONFIG_ONOFF_STRING)) {
                            HW_TEST_CONFIG_ONOFF = listMtest.get(i).value;
                        }else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_UDP_URL_STRING)) {
                            UDP_URL = listMtest.get(i).addr;
                        //} else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_STB_IP_STRING)) { // Edwin 20200511 disable stb ip
                        //    STB_IP = listMtest.get(i).addr;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_DEST_IP_STRING)) {
                            DEST_IP = listMtest.get(i).addr;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_TUNER_TEST_STRING)) {
                            TUNER_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_LED_TEST_STRING)) {
                            LED_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_SC_TEST_STRING)) {
                            SC_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_USB_TEST_STRING)) {
                            USB_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_WIFI_TEST_STRING)) {
                            WIFI_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_BT_TEST_STRING)) {
                            BT_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_POWER_TEST_STRING)) {
                            POWER_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_SATA_TEST_STRING)) {
                            SATA_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_SD_TEST_STRING)) {
                            SD_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_ETHERNET_TEST_STRING)) {
                            ETHERNET_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_DISEQC_TEST_STRING)) {
                            DISEQC_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_LNB_TEST_STRING)) {
                            LNB_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_LONG_TERM_TEST_STRING)) {
                            LONG_TERM_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_THERMAL_TEST_STRING)) {
                            THERMAL_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_FLASH_TEST_STRING)) {
                            FLASH_TEST = listMtest.get(i).value;
                        } else if (listMtest.get(i).item.equals(HW_TEST_CONFIG_DDR_TEST_STRING)) {
                            DDR_TEST = listMtest.get(i).value;
                        }
                    }

                    update = checkConfigUpdate();//gary20200508 add update hw mtest config then should reboot stb
//                    setConfigFromUsbFlag(1);
                    // Edwin 20200508 HwTestConfig parse url, ip -e
                }
            }
        }
        return update;
    }

    public void setTvModelTextView(TextView view) {
        mModelTV = view;
        if(mModelTV != null && getHW_TEST_CONFIG_ONOFF() == 1)
            mModelTV.setText("HW test mode : "+
//                                        UDP_URL +
                    //STB_IP + // Edwin 20200511 disable stb ip
//                                        DEST_IP +
                    TUNER_TEST +
                    LED_TEST +
                    SC_TEST +
                    USB_TEST +
                    WIFI_TEST +
                    BT_TEST +
                    POWER_TEST +
                    SATA_TEST +
                    SD_TEST +
                    ETHERNET_TEST +
                    DISEQC_TEST +
                    LNB_TEST +
                    LONG_TERM_TEST +
                    THERMAL_TEST +
                    FLASH_TEST +
                    DDR_TEST);
    }
//gary20200508 add update hw mtest config then should reboot stb-s
    public boolean checkConfigItemUpdate(String key,int value,int defaultValue) {
        Log.d(TAG,"key = " + key );
        Log.d(TAG,"value = " + value );
        Log.d(TAG,"mHwConfigSharedPreference.getInt("+key+","+defaultValue+") = " + mHwConfigSharedPreference.getInt(key,defaultValue) );
        if(mHwConfigSharedPreference.getInt(key,defaultValue) != value) {
            mHwConfigSharedPreference.putInt(key, value);
            mHwConfigSharedPreference.save();
            return true;
        }
        return false;
    }

    public boolean checkConfigItemUpdate(String key,String addr,String defaultValue) { // Edwin 20200508 HwTestConfig parse url, ip
        String pref_url = null;
        Log.d(TAG,"key = " + key );
        Log.d(TAG,"addr = " + addr );
        Log.d(TAG,"mHwConfigSharedPreference.getString("+key+","+defaultValue+") = " + mHwConfigSharedPreference.getString(key,defaultValue) );
        if(!mHwConfigSharedPreference.getString(key,defaultValue).equals(addr)) {
            mHwConfigSharedPreference.putString(key, addr);
            mHwConfigSharedPreference.save();
            return true;
        }
        Log.d(TAG, "checkConfigItemUpdate: addr is same");
        return false;
    }

    public boolean checkConfigUpdate() {
        boolean update = false, Hwtest_onoff_update = false, resetDefaultTestOngoing = false;
        Hwtest_onoff_update = checkConfigItemUpdate(HW_TEST_CONFIG_ONOFF_STRING, HW_TEST_CONFIG_ONOFF,0);
        Log.d(TAG, "checkConfigUpdate: Hwtest_onoff_update = "+Hwtest_onoff_update);
        if(getHW_TEST_CONFIG_ONOFF() == 1) {
        // Edwin 20200511 reboot if update address -s // Edwin 20200508 HwTestConfig parse url, ip -s
            update = checkConfigItemUpdate(HW_TEST_CONFIG_UDP_URL_STRING, UDP_URL,MainActivity.DEFAULT_URL);
            //update |= checkConfigItemUpdate("STB_IP", STB_IP); // Edwin 20200511 disable stb ip
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_DEST_IP_STRING, DEST_IP,MainActivity.DEFAULT_PING_IP);
            // Edwin 20200511 reboot if update address -e // Edwin 20200508 HwTestConfig parse url, ip -e
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_TUNER_TEST_STRING,TUNER_TEST,1);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_LED_TEST_STRING,LED_TEST,1);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_SC_TEST_STRING,SC_TEST,1);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_USB_TEST_STRING,USB_TEST,1);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_WIFI_TEST_STRING,WIFI_TEST,1);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_BT_TEST_STRING,BT_TEST,1);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_POWER_TEST_STRING,POWER_TEST,0);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_SATA_TEST_STRING,SATA_TEST,1);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_SD_TEST_STRING,SD_TEST,1);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_ETHERNET_TEST_STRING,ETHERNET_TEST,1);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_DISEQC_TEST_STRING,DISEQC_TEST,1);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_LNB_TEST_STRING,LNB_TEST,1);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_LONG_TERM_TEST_STRING,LONG_TERM_TEST,0);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_THERMAL_TEST_STRING,THERMAL_TEST,0);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_FLASH_TEST_STRING,FLASH_TEST,0);
            update |= checkConfigItemUpdate(HW_TEST_CONFIG_DDR_TEST_STRING,DDR_TEST,0);
        }
        Log.d(TAG, "checkConfigUpdate: update = "+update);
        Log.d(TAG, "checkConfigUpdate: getHW_TEST_CONFIG_ONOFF() = "+getHW_TEST_CONFIG_ONOFF());
        if(getHW_TEST_CONFIG_ONOFF() == 0) {
            HwtestConfigSetNotHWConfig();
        }

        if (POWER_TEST == RUN_RESET_DEFAULT_TEST && isResetDefaultTestOnGoing()) {
            Log.d(TAG, "checkConfigUpdate: reset default test is ongoing, set not reboot");
            resetDefaultTestOngoing = true;
        }

        if(!resetDefaultTestOngoing && (Hwtest_onoff_update || update)) {
            reboot();
            return true;
        }
        return false;
    }

    /*
    public void apk_count_add(String ccc)
    {
        int count = mHwConfigSharedPreference.getInt(ccc,0);
        mHwConfigSharedPreference.putInt(ccc,(count+1));
        mHwConfigSharedPreference.save();
    }

    public int get_apk_count(String ccc) {
        int count = mHwConfigSharedPreference.getInt(ccc,0);
        return count;
    }

    public void apk_count_reset(String ccc) {
        mHwConfigSharedPreference.putInt(ccc,0);
        mHwConfigSharedPreference.save();
    }
    */

    public int writeLogByResetDefault()
    {
        Log.d(TAG, "writeLogByResetDefault: ");
        StorageManager storageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
        try {
            for (Object vol : getVolumeList())
            {
                if (pesiStorageHelper.isUsb(vol))
                {
                    String filePath = pesiStorageHelper.getPath(vol) + "/Reset_Default_Test.txt";
//                    String filePath = mContext.getExternalFilesDir("test").getAbsolutePath() + "/z001.txt";
                    Log.d(TAG, "writeLogByResetDefault: filePath = " + filePath);
                    File file = new File(filePath);
                    if (!file.exists())
                    {
                        Log.d(TAG, "writeLogByResetDefault: " +
                                "createNewFile success = " +
                                file.createNewFile());
                    }

                    // get current count
                    String line;
                    int currentCount = 0;
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    while ((line = reader.readLine()) != null)
                    {
                        currentCount++;
                        Log.d(TAG, "writeLogByResetDefault: line = "+line);
                    }
                    reader.close();
                    Log.d(TAG, "writeLogByResetDefault: current count = "+currentCount);

                    // write test success
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                    if (currentCount == 0)
                    {
                        // write new count
                        writer.write(String.valueOf(++currentCount));
                        writer.flush();
                    }
                    else
                    {
                        // write Reset Default success
                        writer.write(" , Reset Default success !");
                        writer.newLine();
                        writer.flush();
                        // write next count
                        writer.write(String.valueOf(++currentCount));
                        writer.flush();
                    }
                    writer.close();
                    return --currentCount;
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return -1; // error
    }

    public void resetDefault()
    {
        Log.d(TAG, "resetDefault: RUN_RESET_DEFAULT_TEST = " + (POWER_TEST == RUN_RESET_DEFAULT_TEST));
        Log.d(TAG, "resetDefault: ACTION_FACTORY_RESET = " + ACTION_FACTORY_RESET);
        Intent resetIntent = new Intent(ACTION_FACTORY_RESET);
        resetIntent.setPackage("android");
        mContext.sendBroadcast(resetIntent);
    }

    /**
     * check whether the reset default test is ongoing by report file in usb
     */
    private boolean isResetDefaultTestOnGoing() {
        StorageManager storageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
        for (Object vol : getVolumeList()) {
            if (pesiStorageHelper.isUsb(vol)) {
                String filePath = pesiStorageHelper.getPath(vol) + "/Reset_Default_Test.txt";
                File file = new File(filePath);
                if (file.exists()) {
                    return true;
                }
            }
        }

        return false;
    }

    public void reboot()
    {
        Intent intent2 = new Intent(Intent.ACTION_REBOOT);
        intent2.putExtra("nowait", 1);
        intent2.putExtra("interval", 1);
        intent2.putExtra("window", 0);
        mContext.sendBroadcast(intent2);
    }
//gary20200508 add update hw mtest config then should reboot stb-e

    // Edwin 20200508 HwTestConfig parse url, ip -s
    public void setFileFound (boolean found) {
        fileFound = found;
    }

    public boolean isFileFound () {
        // Edwin 20200511 fix invalid method isFileFound() -s
        StorageManager storageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
        if (storageManager == null) {
            setFileFound(false);
            return fileFound;
        }
        setFileFound(false);
        for (Object vol : pesiStorageHelper.getVolumes()) {
            String id = pesiStorageHelper.getId(vol);
            String path = pesiStorageHelper.getPath(vol);
            if (vol != null) {
                if (id.startsWith("public")) {
                    if (path != null) {
                        File file = new File(path, "mtest_ini.dat");
                        try {
                            BufferedReader reader;
                            if (file.exists()) // edwin 20200522 read file when file do exist
                            {
                                reader = new BufferedReader(new FileReader(file));
                                reader.close();
                                setFileFound(true);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        // Edwin 20200511 fix invalid method isFileFound() -e
        return fileFound;
    }

    public boolean DeleteReportFileInFlash() {
        boolean pass = false;
        pass |= new File(mContext.getFilesDir() + "/" + "hwReport.txt").delete();
        pass |= new File(mContext.getFilesDir() + "/" + "hwReport_LongTermTest.txt").delete();
        pass |= new File(mContext.getFilesDir() + "/" + "hwReport_ddrTest.txt").delete();
        pass |= new File(mContext.getFilesDir() + "/" + "hwReport_flashTest.txt").delete();
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_TOTAL_COUNT, 0);
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_PASS_COUNT, 0);
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_LONG_TERM_TOTAL_COUNT, 0);
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_LONG_TERM_PASS_COUNT, 0);
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_FLASH_TOTAL_COUNT, 0);
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_FLASH_PASS_COUNT, 0);
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_DDR_TOTAL_COUNT, 0);
        mHwConfigSharedPreference.putInt(HW_TEST_CONFIG_DDR_PASS_COUNT, 0);
        mHwConfigSharedPreference.save();
        return pass;
    }

    public void CopyReportToUsb() {
        String sourcePath = mContext.getFilesDir().toString();
        String DestDirPath = null;
        String DestFilePath = null;
        StorageManager storageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
        List<Object> volList = pesiStorageHelper.getVolumes();
        for (Object vol : volList)
        {
            try {
                if (pesiStorageHelper.isUsb(vol)) {
                    //writeLog(vol,log,pass);
                    DestDirPath = pesiStorageHelper.getPath(vol);
                    try {
                        DoCopyFile(sourcePath, DestDirPath,"hwReport.txt");
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    try {
                        DoCopyFile(sourcePath, DestDirPath,"hwReport_LongTermTest.txt");
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    try {
                        DoCopyFile(sourcePath, DestDirPath,"hwReport_ddrTest.txt");
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    try {
                        DoCopyFile(sourcePath, DestDirPath,"hwReport_flashTest.txt");
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    break;
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private void DoCopyFile(String sourceDir, String DestDir, String fileName) throws IOException {
        String comepath = sourceDir + "/" + fileName;
        String gopath = DestDir + "/" + fileName;
        Log.d(TAG, "DoCopyFile: comepath = "+comepath+" , gopath = "+gopath);
        try {
            /*File wantfile = new File(comepath);
            File newfile = new File(gopath);
            InputStream in = new FileInputStream(wantfile);
            OutputStream out = new FileOutputStream(newfile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();*/
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("cp " + comepath + " " + gopath);
            int mExitValue = process.waitFor();
            Log.d(TAG, "DoCopyFile: mExitValue = "+mExitValue);
        } catch (Exception e) {
            Log.e("copy file error", e.toString());
        }
    }

    public String getTestCount() {
        String str = "";
        str += "count : " + mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_TOTAL_COUNT, 0);
        str += "\n";
        str += "passCount : " + mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_PASS_COUNT, 0);
        str += "\n";
        str += "count_LongTerm : " + mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_LONG_TERM_TOTAL_COUNT, 0);
        str += "\n";
        str += "passCount_LongTerm : " + mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_LONG_TERM_PASS_COUNT, 0);
        str += "\n";
        str += "flashCount : " + mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_FLASH_TOTAL_COUNT, 0);
        str += "\n";
        str += "flashPass : " + mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_FLASH_PASS_COUNT, 0);
        str += "\n";
        str += "ddrCount : " + mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_DDR_TOTAL_COUNT, 0);
        str += "\n";
        str += "ddrPass : " + mHwConfigSharedPreference.getInt(HW_TEST_CONFIG_DDR_PASS_COUNT, 0);
        return str;
    }

    public int getHW_TEST_CONFIG_ONOFF() {
        Log.d(TAG,"HW_TEST_CONFIG_ONOFF = "+ HW_TEST_CONFIG_ONOFF);
        return HW_TEST_CONFIG_ONOFF;
    }

    public String getUDP_URL () {
        return UDP_URL;
    }

    //public String getSTB_IP () { // Edwin 20200511 disable stb ip
    //    return STB_IP;
    //}

    public String getDEST_IP () {
        return DEST_IP;
    }
    // Edwin 20200508 HwTestConfig parse url, ip -e

    public int getTUNER_TEST() {
        return TUNER_TEST;
    }

    /* LED_TEST
    0:off
    1:Production mode
    2:LED on/off*/
    public int getLED_TEST() {
        return LED_TEST;
    }

    /* SC_TEST
    0:off
    1:ATR one time,Production mode
    2:Continuous ATR*/
    public int getSC_TEST() {
        return SC_TEST;
    }

    /* USB_TEST
    0:off
    1:R/W one time,Production mode
    2:Continuous R/W */
    public int getUSB_TEST() {
        return USB_TEST;
    }

    /* WIFI_TEST
    0:off
    1:connect AP,Production mode
    2:Continuous scan */
    public int getWIFI_TEST() {
        return WIFI_TEST;
    }

    /* BT_TEST
    0:off
    1:connect AP,Production mode
    2:Continuous scan */
    public int getBT_TEST() {
        return BT_TEST;
    }

    /* POWER_TEST
    0:off
    1:Tool, Production mode
    2:SW reboot
    3:STR
    4:Reset Default */
    public int getPOWER_TEST() {
        return POWER_TEST;
    }

    /* SATA_TEST
    0:off
    1:R/W one time,Production mode
    2:Continuous R/W */
    public int getSATA_TEST() {
        return SATA_TEST;
    }

    /* SD_TEST
    0:off
    1:R/W one time,Production mode
    2:Continuous R/W */
    public int getSD_TEST() {
        return SD_TEST;
    }

    /* ETHERNET_TEST
    0:off
    1:ping one time,Production mode
    2:Continuous ping */
    public int getETHERNET_TEST() {
        return ETHERNET_TEST;
    }

    /* DISEQC_TEST
    0:off
    1:Production mode
    2:Continuous change */
    public int getDISEQC_TEST() {
        return DISEQC_TEST;
    }

    /* LNB_TEST
    0:off
    1:Production mode
    2:Continuous change */
    public int getLNB_TEST() {
        return LNB_TEST;
    }

    /* LONG_TERM_TEST
    0:off
    1:Wait 10 mins, show PASS/FAIL */
    public int getLONG_TERM_TEST() {
        return LONG_TERM_TEST;
    }

    /* THERMAL_TEST
    0:off
    1:add loading , Ex UI redraw */
    public int getTHERMAL_TEST() {
        return THERMAL_TEST;
    }

    /* FLASH_TEST
    0:off
    1:Continuous R/W */
    public int getFLASH_TEST() {
        return FLASH_TEST;
    }

    /* DDR_TEST
    0:off
    1:Continuous R/W */
    public int getDDR_TEST() {
        return DDR_TEST;
    }
}
