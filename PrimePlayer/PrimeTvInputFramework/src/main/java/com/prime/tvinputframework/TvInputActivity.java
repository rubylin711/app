package com.prime.tvinputframework;

import static com.prime.datastructure.sysdata.TpInfo.Cable.QAM_256;
import static com.prime.datastructure.sysdata.TpInfo.Cable.QAM_64;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.media.tv.TvInputInfo;
import android.os.Bundle;
import android.util.Log;

import com.prime.datastructure.CommuincateInterface.MiscDefine;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.TpInfo;

public class TvInputActivity extends Activity {
    public static final String TAG = "SetupActivity";
    public static final String PACKAGE_LAUNCHER = "com.prime.tvinputframework";
    public static final String ACTIVITY_CHANNEL_SCAN = "com.prime.tvinputframework.Scan.Cable.ChannelPreScanningActivity";
    public static final String SCANNING_BW              = "scanning_bw";
    public static final String SCANNING_FREQ            = "scanning_freq";
    public static final String SCANNING_MODULATION      = "scanning_modulation";
    public static final String SCANNING_MODULATION_TEXT = "scanning_modulation_text";
    public static final String SCANNING_PAGE            = "scanning_page";
    public static final String SCANNING_SR              = "scanning_sr";
    public static final String SCANNING_TUNERID         = "scanning_tunerid";
    public static final String SCAN_FROM_ACS            = "scan_from_acs";
    public static final String SINGLE_FREQ              = "single_freq";

    private static String TvInputServiceID;
    private static boolean TV_INPUT_OPENED = false;

    public static String getTvInputServiceID() {
        return TvInputServiceID;
    }

    public static void setTvInputServiceID(String tvInputServiceID) {
        TvInputServiceID = tvInputServiceID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        TvInputServiceID = getIntent().getStringExtra(TvInputInfo.EXTRA_INPUT_ID);
        TV_INPUT_OPENED = true;
//        setContentView(R.layout.activity_setup);
//
//        // 新增範例頻道
//        ContentValues values = new ContentValues();
//        values.put(TvContract.Channels.COLUMN_INPUT_ID, getPackageName() + "/.DvbTvInputService");
//        values.put(TvContract.Channels.COLUMN_DISPLAY_NUMBER, "1");
//        values.put(TvContract.Channels.COLUMN_DISPLAY_NAME, "DVB Channel 1");
//        values.put(TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID, 1);
//        values.put(TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID, 1);
//        values.put(TvContract.Channels.COLUMN_SERVICE_ID, 1);
//        values.put(TvContract.Channels.COLUMN_VIDEO_FORMAT, "VIDEO_FORMAT_1080P");
//
//        getContentResolver().insert(TvContract.Channels.CONTENT_URI, values);
//
//        // 啟動 EPG 同步（可選）
//        scheduleEpgSync();
//        finish();
        scan_page();
    }

    private void scheduleEpgSync() {
        // 使用 JobScheduler 定時執行 EpgSyncJobService（範例簡化）
    }
    public void scan_page() {
        Log.i(TAG, "ChannelScan on click");
        final Intent src = getIntent();

        // 讀取外部傳入（若沒帶就用 -1）
        int freqIn   = src != null ? src.getIntExtra(SCANNING_FREQ, -1) : -1;      // MHz（405 之類）
        int srIn     = src != null ? src.getIntExtra(SCANNING_SR,   -1) : -1;      // kSym/s（5217 之類）
        int qamReal  = src != null ? src.getIntExtra(SCANNING_MODULATION, -1) : -1; // 16/32/64/128/256（real value）
        String qamTextIn = src != null ? src.getStringExtra(SCANNING_MODULATION_TEXT) : null;
        boolean singleIn = src != null && src.getBooleanExtra(SINGLE_FREQ, false);

        // real QAM → enum
        int qamEnum = qamRealToEnum(qamReal);

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PACKAGE_LAUNCHER, ACTIVITY_CHANNEL_SCAN));

        Bundle b = new Bundle();
        b.putBoolean(SCAN_FROM_ACS, false);
        b.putBoolean(SCANNING_PAGE, false);
        b.putBoolean(SINGLE_FREQ, singleIn);
        b.putInt(SCANNING_BW, 6);
        b.putInt(SCANNING_TUNERID, 0);

        if (freqIn > 0) b.putInt(SCANNING_FREQ, freqIn);
        if (srIn   > 0) b.putInt(SCANNING_SR,   srIn);
        if (qamEnum != TpInfo.Cable.QAM_AUTO)   b.putInt(SCANNING_MODULATION, qamEnum);
        if (qamTextIn != null) b.putString(SCANNING_MODULATION_TEXT, qamTextIn);

        // 若外部沒帶齊，就補預設
        if (!b.containsKey(SCANNING_FREQ) || !b.containsKey(SCANNING_SR) ||
                !b.containsKey(SCANNING_MODULATION) || !b.containsKey(SCANNING_MODULATION_TEXT)) {

            if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)) {
                b.putInt(SCANNING_FREQ, 405);
                b.putInt(SCANNING_SR,   5217);
                b.putInt(SCANNING_BW,   6);
                b.putInt(SCANNING_MODULATION, QAM_256);
                b.putString(SCANNING_MODULATION_TEXT, "256");
            } else if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC)) {
                b.putInt(SCANNING_FREQ, 405);
                b.putInt(SCANNING_SR,   5217);
                b.putInt(SCANNING_BW,   6);
                b.putInt(SCANNING_MODULATION, QAM_256);
                b.putString(SCANNING_MODULATION_TEXT, "256");
            } else {
                b.putInt(SCANNING_FREQ, 405);
                b.putInt(SCANNING_SR,   5217);
                b.putInt(SCANNING_BW,   6);
                b.putInt(SCANNING_MODULATION, QAM_256);
                b.putString(SCANNING_MODULATION_TEXT, "256");
            }
        }

        intent.putExtras(b);
        startActivity(intent);
        finish();
    }
    private static int qamRealToEnum(int real) {
        switch (real) {
            case 256: return TpInfo.Cable.QAM_256;
            case 128: return TpInfo.Cable.QAM_128;
            case 64:  return TpInfo.Cable.QAM_64;
            case 32:  return TpInfo.Cable.QAM_32;
            case 16:  return TpInfo.Cable.QAM_16;
            default:  return TpInfo.Cable.QAM_AUTO; // -1 或不識別
        }
    }

    public static boolean isTvInputOpened ()
    {
        return TV_INPUT_OPENED;
    }

    public static void disableTvInput()
    {
        TV_INPUT_OPENED = false;
    }
}
