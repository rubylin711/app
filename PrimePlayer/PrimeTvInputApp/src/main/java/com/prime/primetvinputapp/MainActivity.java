package com.prime.primetvinputapp;

import static com.prime.datastructure.sysdata.TpInfo.Cable.QAM_256;
import static com.prime.datastructure.sysdata.TpInfo.Cable.QAM_64;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.datastructure.ServiceDefine.PrimeDtvInterface;
import com.prime.datastructure.TIF.TIFChannelData;
import com.prime.datastructure.TIF.TIFEpgData;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.TVMessage;

public class MainActivity extends BaseActivity implements PrimeDtvInterface.DTVCallback{
    public static final String TAG = "MainActivity";
    public static final String PACKAGE_LAUNCHER = "com.prime.primetvinputapp";
    public static final String ACTIVITY_CHANNEL_SCAN = "com.prime.primetvinputapp.Scan.Cable.ChannelPreScanningActivity";
    public static final String SCANNING_BW              = "scanning_bw";
    public static final String SCANNING_FREQ            = "scanning_freq";
    public static final String SCANNING_MODULATION      = "scanning_modulation";
    public static final String SCANNING_MODULATION_TEXT = "scanning_modulation_text";
    public static final String SCANNING_PAGE            = "scanning_page";
    public static final String SCANNING_SR              = "scanning_sr";
    public static final String SCANNING_TUNERID         = "scanning_tunerid";
    public static final String SCAN_FROM_ACS            = "scan_from_acs";
    public static final String SINGLE_FREQ              = "single_freq";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Button scan_btn = (Button) findViewById(R.id.Scan_btn);
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan_page();
            }
        });

        Button liveTv_btn = (Button) findViewById(R.id.LiveTv_btn);
        liveTv_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_liveTv();
            }
        });

        Button epg_btn = (Button) findViewById(R.id.Epg_btn);
        epg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testepg();
            }
        });

//        Button callback_test_btn = (Button) findViewById(R.id.callback_test_btn);
//        callback_test_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                runOnUiThread(()->{
//                    TIFChannelData.deleteTIFChannelDataByTvInputId(MainActivity.this,PrimeTvInputAppApplication.getTvInputId());
//                    for(int i = 0; i < 10; i++) {
//                        PrimeTvInputAppApplication.get_prime_dtv_service().testCallback(i+" test callback");
//                    }
//                });
//
//            }
//        });
    }

    public void testepg() {
//        TIFEpgData.test_insert(this);
    }

    public void start_liveTv() {
        Intent intent = new Intent(this, LiveTvActivity.class);
        startActivity(intent);
    }

    public void scan_page() {
        Log.i(TAG, "ChannelScan on click");
        runOnUiThread(()->{
            ProgramInfo programInfo = PrimeTvInputAppApplication.get_prime_dtv_service().get_program_by_channel_id(2497401);
            if(programInfo != null) {
                Log.d("pppp", "program = " + programInfo.ToString());
                TpInfo tpInfo = PrimeTvInputAppApplication.get_prime_dtv_service().tp_info_get(programInfo.getTpId());
                Log.d("pppp", "tpInfo = " + tpInfo.ToString());
            }
        });
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PACKAGE_LAUNCHER, ACTIVITY_CHANNEL_SCAN));
        Bundle bundle = new Bundle();
        bundle.putBoolean(SCAN_FROM_ACS, false);
        bundle.putBoolean(SCANNING_PAGE, false);
        bundle.putBoolean(SINGLE_FREQ, false);
        if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)) {
            bundle.putInt(SCANNING_FREQ, 615);
            bundle.putInt(SCANNING_SR, 5057);
            bundle.putInt(SCANNING_BW, 6);
            bundle.putInt(SCANNING_MODULATION, QAM_64);
            bundle.putString(SCANNING_MODULATION_TEXT, "64");
        }
        else {
            bundle.putInt(SCANNING_FREQ, 303);
            bundle.putInt(SCANNING_SR, 5200);
            bundle.putInt(SCANNING_BW, 6);
            bundle.putInt(SCANNING_MODULATION, QAM_256);
            bundle.putString(SCANNING_MODULATION_TEXT, "256");
        }
        bundle.putInt(SCANNING_TUNERID, 0);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
        if (msg.getMsgFlag() == TVMessage.FLAG_SCAN) {
            switch (msg.getMsgType()) {
                case TVMessage.TYPE_SCAN_BEGIN:
                    Log.d(TAG, "PrimeDtvServiceApplication onMessage: TYPE_SCAN_BEGIN");
                    break;
                case TVMessage.TYPE_SCAN_END:
                    Log.d(TAG, "PrimeDtvServiceApplication onMessage: TYPE_SCAN_END");
                    break;
                default:
                    Log.d(TAG, "PrimeDtvServiceApplication onMessage: unknown scan message");
                    break;
            }
        }
    }
}