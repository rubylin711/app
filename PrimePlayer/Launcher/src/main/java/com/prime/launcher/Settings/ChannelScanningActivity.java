package com.prime.launcher.Settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;


import com.prime.datastructure.ServiceDefine.EpgCmdManager;
import com.prime.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.launcher.ACSDatabase.ACSHelper;
import com.prime.launcher.BaseActivity;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.launcher.ChannelChangeManager;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.MusicInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.TVMessage;
import com.prime.datastructure.utils.TVScanParams;
import com.prime.datastructure.utils.TVTunerParams;

import java.text.DecimalFormat;
import java.util.List;


public class ChannelScanningActivity extends BaseActivity implements PrimeDtv.LauncherCallback {
    private final static String TAG = "ChannelScanningActivity";
    public static final String AUTO_DETECT              = "signal_auto_detect";
    public static final String SCANNING_BW              = "scanning_bw";
    public static final String SCANNING_FREQ            = "scanning_freq";
    public static final String SCANNING_MODULATION      = "scanning_modulation";
    public static final String SCANNING_MODULATION_TEXT = "scanning_modulation_text";
    public static final String SCANNING_PAGE            = "scanning_page";
    public static final String SCANNING_SR              = "scanning_sr";
    public static final String SCANNING_TUNERID         = "scanning_tunerid";
    public static final String SCAN_FROM_ACS            = "scan_from_acs";
    public static final String SINGLE_FREQ              = "single_freq";
    private static final long SCANNING_TIME_OUT = 60000L;
    public static final int DBM_TO_DBMV = 107 - 60; // dBm + 107 - 60 ~= dBmV
    private PrimeDtv g_prime_dtv;
    private int g_band_width;
    private ChannelScanView g_channel_scan_view;
    //private MyVideoView g_dummy_video_view;
    private int g_modulation;
    private String g_modulation_text;
    private int g_param_freq;
    //private TVSPlayer g_player;
    private SignalDetectView g_signal_detect_view;
    private boolean g_single_freq;
    private int g_symbol_rate;
    private int g_total_freq;
    private int g_tuner_id;
    //private TunerInfo g_tuner_info;
    private boolean g_view_status = true;
    private boolean g_from_acs = false;
    private boolean g_auto_detect = false;
    private Handler g_handler = new Handler();
    private boolean g_scan_complete = false;
    private boolean g_signal_detect = false;
    private boolean g_restart_eit = false;
    private ChannelChangeManager g_ch_change_manager;

    //signal detect
    private boolean g_do_signal_detect_flag = false;

    private Runnable g_scan_timeout_runnable = new Runnable() {
        @Override
        public void run() {
            /*
                stop_channel_scan()/stop_scan()
            */
            ChannelScanningActivity.this.g_channel_scan_view.update_scan_timeout();
        }
    };
    private Runnable g_show_tuner_status = new Runnable() {
        @Override
        public void run() {
            /*
                get_tuner_status()
                set_tuner_info(tunerStatus, false)
                update_signal_value(ChannelScanningActivity.this.g_tuner_info)
            }*/

            update_signal_value(g_tuner_id);
            ChannelScanningActivity.this.g_handler.postDelayed(this, 1000L);
        }
    };
    private Runnable g_scan_signal_check = new Runnable() {
        @Override
        public void run() {
            /*
                get_tuner_status()
                if (tunerStatus != null && !tunerStatus.is_locked()) {
                    stop_channel_scan()/stop_scan()
                }
                ChannelScanningActivity.this.g_channel_scan_view.update_scan_no_signal()
            */
            ChannelScanningActivity.this.g_handler.postDelayed(this, 1000L);
        }
    };
    private Runnable g_scan_signal_check_begin = new Runnable() {
        @Override
        public void run() {
            /*
                get_tuner_status()
                if (tunerStatus != null) {
                    g_player.stop()
                    TVSHelper...get_tv_system().get_system().destroy_demux()
                    if (!tunerStatus.isLocked()) {
                        stop_channel_scan()/stop_scan()
                        g_channel_scan_view.update_scan_no_signal()
                        return
                    }
                    channelScanningActivity.start_scan(g_param_freq, g_symbol_rate, g_band_width, g_modulation, g_tuner_id)
                    return
                }
            */
//            ChannelScanningActivity.this.g_handler.postDelayed(this, 500L);

            ChannelScanningActivity.this.start_scan(g_param_freq, g_symbol_rate, g_band_width, g_modulation, g_tuner_id, g_single_freq);
        }
    };
    /*
        private DmgSiServiceListener g_dmg_si_service_listener ...
        private ISIScanCallback.Stub g_register_scan_callback ...
    */
    private Runnable g_scan_interval = new Runnable() {
        @Override
        public void run() {
            ChannelScanningActivity channelScanningActivity = ChannelScanningActivity.this;
            channelScanningActivity.ready_to_scan(g_param_freq, g_symbol_rate, g_band_width, g_modulation, g_tuner_id);
        }
    };

    private static int get_qam_from_modulation(int modulation) {
        int i = 8;
        for (int i2 = 0; i2 < modulation; i2++) {
            i *= 2;
        }
        return i;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_PROG_BLUE/*186*/) {
            /*
                g_player.get_tuner_status()
                tunerInfo.set_tuner_info(tunerStatus, true)
            */
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        int i;
        int i2;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_scanning);
        init();

        Bundle extras = getIntent().getExtras();
        int i3 = 0;
        if (extras != null) {
            this.g_from_acs        = extras.getBoolean(SCAN_FROM_ACS, false);
            this.g_view_status     = extras.getBoolean(SCANNING_PAGE, true);
            this.g_auto_detect     = extras.getBoolean(AUTO_DETECT, false);
            this.g_single_freq     = extras.getBoolean(SINGLE_FREQ, false);
            this.g_param_freq      = extras.getInt(SCANNING_FREQ, -1);
            this.g_symbol_rate     = extras.getInt(SCANNING_SR, -1);
            this.g_band_width      = extras.getInt(SCANNING_BW, -1);
            this.g_modulation      = extras.getInt(SCANNING_MODULATION, -1);
            this.g_tuner_id        = extras.getInt(SCANNING_TUNERID, -1);
            this.g_modulation_text = extras.getString(SCANNING_MODULATION_TEXT, "");
        }
        else {
            this.g_from_acs        = false;
            this.g_view_status     = false;
            this.g_auto_detect     = false;
            this.g_single_freq     = false;
            this.g_param_freq      = 591;
            this.g_symbol_rate     = 5057;
            this.g_band_width      = 6;
            this.g_modulation      = TpInfo.Cable.QAM_64;
            this.g_tuner_id        = 0;
            this.g_modulation_text = "64";
        }

        /*
            register_si_scan_callback(this.g_register_scan_callback)
            get_dmg_service().addListener(this.g_dmg_si_service_listener)
            if (this.g_auto_detect &&
                this.g_view_status &&
                !TextUtils.isEmpty(SharedPrefHelper.getCurrentChCate(getApplicationContext()))) {
                String currentChannelUri = SharedPrefHelper.getCurrentChannelUri(this);
                if (!TextUtils.isEmpty(currentChannelUri)) {
                    Uri parse = Uri.parse(currentChannelUri);
                    try {
                        i = Integer.valueOf(parse.getQueryParameter("sr")).intValue();
                        i2 = Integer.valueOf(parse.getPort()).intValue() / 1000;
                        i3 = Integer.valueOf(parse.getHost().replace("qam", "")).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        i = 0;
                        i2 = 0;
                    }
                    if (i3 > 0 && i2 > 0 && i > 0) {
                        this.g_param_freq = i2;
                        this.g_symbol_rate = i;
                        int modulationFromQam = get_modulation_from_qam(i3);
                        this.g_modulation = modulationFromQam;
                        this.g_modulation_text = String.valueOf(get_qam_from_modulation(modulationFromQam));
                    }
                }
            }
        */
        update_view_status(this.g_view_status);
    }

    private static int get_modulation_from_qam(int qam) {
        int i = qam / 8;
        if (i > 1) {
            return (int) (Math.log(i) / Math.log(2.0d));
        }
        return 0;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //g_prime_dtv.register_callbacks();
    }

    @Override
    public void onPause() {
        this.g_handler.removeCallbacks(this.g_show_tuner_status);
        this.g_handler.removeCallbacks(this.g_scan_timeout_runnable);
        this.g_handler.removeCallbacks(this.g_scan_signal_check);
        this.g_handler.removeCallbacks(this.g_scan_signal_check_begin);
        if (this.g_signal_detect) {
            /*
                this.g_player.stop()
                get_tv_system().get_system().destroy_demux()
            */

            if (g_do_signal_detect_flag) {
                String str = get_detect_info_string(g_tuner_id);
                ACSHelper.set_signal_detect(this,str);
                ACSHelper.delete_acs_provider_data(this, "do_signal_detect");
            }
        }
        if (!this.g_scan_complete) {
            /*
                stop_channel_scan()/stop_scan()
            */
            g_prime_dtv.stop_scan(false);
        }
        /*
            get_si_service().unregister_si_scan_callback(g_register_scan_callback);
            get_dmg_service().removeListener(g_dmg_si_service_listener);
        */

        if (this.g_restart_eit) {
            g_prime_dtv.start_schedule_eit(0,0);
        }

        //g_prime_dtv.unregister_callbacks();

        super.onPause();
    }

    private void backToHomeActivity()
    {
        finish();
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(HomeActivity.EXTRA_SCREEN_TYPE, HomeActivity.SCREEN_TYPE_LIVE_TV);
        getApplicationContext().startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        this.g_channel_scan_view.remove_update_progress_callback();
        this.g_signal_detect_view.remove_callback();
        if (this.g_from_acs) {
            backToHomeActivity() ;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
        if (msg.getMsgFlag() == TVMessage.FLAG_SCAN) {
            switch (msg.getMsgType()) {
                case TVMessage.TYPE_SCAN_BEGIN:
                    Log.d(TAG, "onMessage: TYPE_SCAN_BEGIN");
                    break;
                case TVMessage.TYPE_SCAN_SERCHTP:
                    Log.d(TAG, "onMessage: TYPE_SCAN_SERCHTP");
                    break;
                case TVMessage.TYPE_SCAN_PROCESS:
                    Log.d(TAG, "onMessage: TYPE_SCAN_PROCESS");
                    update_channel_data();
                    break;
                case TVMessage.TYPE_SCAN_SCHEDULE:
                    Log.d(TAG, "onMessage: TYPE_SCAN_SCHEDULE percent = " + msg.getPercent());
                    g_channel_scan_view.update_scan_progress_percent(msg.getPercent());
                    break;
                case TVMessage.TYPE_SCAN_END:
                    Log.d(TAG, "onMessage: TYPE_SCAN_END");
                    if (g_prime_dtv.get_tuner_status(g_tuner_id)) {

                        g_prime_dtv.stop_scan(true);
                        g_prime_dtv.save_table(EnTableType.PROGRAME); // save program to database
                        g_prime_dtv.save_table(EnTableType.TP);
                        g_prime_dtv.save_table(EnTableType.GROUP);

                        GposInfo gposInfo = g_prime_dtv.gpos_info_get();
                        gposInfo.setChannelLockCount(0); // reset locked channels to 0
                        gposInfo.resetTimeLockPeriods(); // reset all time lock periods to -1
                        g_prime_dtv.save_table(EnTableType.GPOS);

                        g_ch_change_manager.update_all_channel();
                        g_ch_change_manager.reset_fcc();
                        g_ch_change_manager.change_channel_to_1st();
                        g_prime_dtv.backupDatabase(true);
                        scan_complete(msg);
                    }
                    else {
                        g_channel_scan_view.update_scan_no_signal();
                    }

                    g_scan_complete = true;
                    break;
                default:
                    Log.d(TAG, "onMessage: unknown scan message");
                    break;
            }
        }
        else if (msg.getMsgType() == TVMessage.TYPE_ADD_MUSIC_CATEGORY_TO_FAV) {
            Log.d(TAG, "onMessage: TYPE_ADD_MUSIC_CATEGORY_TO_FAV");
            add_music_category_to_fav();
        }
    }

    private void scan_complete(TVMessage msg) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            g_channel_scan_view.update_scan_completed(msg.getTotalTVNumber(), msg.getTotalRadioNumber());
            g_prime_dtv.setup_epg_channel(); // send all channels to pesi c service
            g_prime_dtv.start_schedule_eit(0,0); // start schedule EIT filter and send data to pesi c service
        }, 6000);
    }

    private void init() {
        this.g_signal_detect_view = (SignalDetectView) findViewById(R.id.lo_cs_signal_scan_view);
        this.g_channel_scan_view = (ChannelScanView) findViewById(R.id.lo_cs_channel_scan_view);
//        this.g_dummy_video_view = (MyVideoView) findViewById(R.id.DummyVideoView);
        this.g_channel_scan_view.set_on_back_listener(new ChannelScanView.go_back_listener() {
            @Override
            public void go_back() {
                ChannelScanningActivity.this.g_channel_scan_view.remove_update_progress_callback();
                if (!ChannelScanningActivity.this.g_from_acs) {
                    ChannelScanningActivity.this.g_channel_scan_view.setVisibility(View.GONE);
                    ChannelScanningActivity.this.onBackPressed();
//                    ChannelScanningActivity.this.onPause(); // this will cause onPause() trigger twice
                    return;
                }
                backToHomeActivity() ;
            }
        });
        //this.g_tuner_info = new TunerInfo();

        g_prime_dtv = HomeApplication.get_prime_dtv();
        g_ch_change_manager = ChannelChangeManager.get_instance(getApplicationContext());
        g_prime_dtv.av_control_play_stop_all();
        g_ch_change_manager.reset_fcc();
    }

    private void update_view_status(boolean status) {
        Log.d(TAG, "update_view_status: status = " + status);
        if (status) {
            this.g_signal_detect_view.update_signal_params(this.g_param_freq, this.g_symbol_rate, this.g_modulation_text);
            this.g_signal_detect_view.setVisibility(View.VISIBLE);
            this.g_channel_scan_view.setVisibility(View.GONE);
            start_detect(this.g_param_freq, this.g_symbol_rate, this.g_modulation, 0);
            return;
        }
        this.g_channel_scan_view.update_scan_params(this.g_param_freq, this.g_symbol_rate, this.g_modulation_text);
        this.g_signal_detect_view.setVisibility(View.GONE);
        this.g_channel_scan_view.setVisibility(View.VISIBLE);
        this.g_handler.removeCallbacks(this.g_scan_interval);
        if (this.g_from_acs) {
            this.g_handler.postDelayed(this.g_scan_interval, 3000L);
        } else {
            this.g_handler.post(this.g_scan_interval);
        }
    }

    private void start_detect(int freq, int sr, int modulation, int mode) {
        this.g_signal_detect = true;
        final String str = "tuner://qam" + get_qam_from_modulation(modulation) + ":" + (freq * 1000) + "?&sr=" + sr;
        /*
            set g_player to do something
        */
        if (mode == 0) { // signal detect
            if (g_tuner_id == EpgCmdManager.EPG_TUNER_ID) {
                // stop schedule eit if we use same tuner id
                g_prime_dtv.stop_schedule_eit();
                this.g_restart_eit = true;
            }

            TVTunerParams tvTunerParams =
                    TVTunerParams.CreateTunerParamDVBC(
                            g_tuner_id, 0/*sat id not used*/, 0/*tp id not used*/,
                            freq * 1000, sr, modulation);
            String acs_data = ACSDataProviderHelper.get_acs_provider_data(this,"do_signal_detect");
            Log.d(TAG,"do_signal_detect = "+acs_data);
            g_do_signal_detect_flag = acs_data != null && acs_data.equals("1");
            g_prime_dtv.tuner_lock(tvTunerParams);

            this.g_handler.post(this.g_show_tuner_status);
        } else if (mode == 1) { // scan
            this.g_handler.post(this.g_scan_signal_check_begin);
        } else if (mode != 2) {
        } else {
            this.g_handler.post(this.g_scan_signal_check);
        }
    }

    public void ready_to_scan(final int freq, final int sr, int bw, final int modulation, int tunerId) {
        Log.d(TAG, "ready_to_scan: ");
        this.g_handler.postDelayed(this.g_scan_timeout_runnable, SCANNING_TIME_OUT);
        start_detect(freq, sr, modulation, 1);
    }

    public void start_scan(final int freq, final int sr, int bw, final int modulation, int tunerId, boolean isSingleFreq) {
        List<TpInfo> tpList =
                g_prime_dtv.tp_info_get_list_by_satId(
                        g_prime_dtv.get_tuner_type(),
                        MiscDefine.TpInfo.NONE_SAT_ID,
                        MiscDefine.TpInfo.POS_ALL,
                        MiscDefine.TpInfo.NUM_ALL);
        TpInfo tpInfo = null;
        for( TpInfo tmpTpinfo : tpList ) {
            if ( tmpTpinfo.CableTp.getFreq() == freq*1000 ) {
                tpInfo = tmpTpinfo;
                break;
            }
        }

        if (tpInfo != null) {
            tpInfo.CableTp.setFreq(freq * 1000);
            tpInfo.CableTp.setSymbol(sr);
            tpInfo.CableTp.setQam(modulation);

            g_prime_dtv.tp_info_update(tpInfo); // update tp
            g_prime_dtv.save_table(EnTableType.TP); // save tp to database

            int scanMode ;//= (isSingleFreq)?TVScanParams.SCAN_MODE_MANUAL:TVScanParams.SCAN_DMG_SEARCH;
            if(isSingleFreq){
                scanMode = TVScanParams.SCAN_MODE_MANUAL;
            }else{
                if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG))
                    scanMode = TVScanParams.SCAN_DMG_SEARCH;
                else if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS))
                    scanMode = TVScanParams.SCAN_CNS_SEARCH;
                else
                    scanMode = TVScanParams.SCAN_TBC_SEARCH;
            }
            TVScanParams params = new TVScanParams(
                    tunerId, tpInfo, 0,
                    scanMode, TVScanParams.SEARCH_OPTION_ALL, TVScanParams.SEARCH_OPTION_ALL,
                    0, 0);
            g_prime_dtv.stopMonitorTable(-1, -1);
            g_prime_dtv.start_scan(params);
        }
    }

    public void update_channel_data() {
       // new GetChannelUriTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Integer[0]);
    }

    private void update_signal_value(int tunerId) {

        boolean lock = g_prime_dtv.get_tuner_status(tunerId);
        // tuner framework strength return thousandths of a dBm (0.001dBm)
        // convert to dBmV
        int strength = g_prime_dtv.get_signal_strength(tunerId) / 1000 + DBM_TO_DBMV;
        // tuner framework quality return percent
        int quality = g_prime_dtv.get_signal_quality(tunerId);
        // tuner framework SNR return thousandths of a deciBel (0.001dB)
        int snr = g_prime_dtv.get_signal_snr(tunerId) / 1000;
        // tuner framework BER return the number of error bit per 1 billion bits.
        // divide by 1 million according to RTK
        double ber = g_prime_dtv.get_signal_ber(tunerId) / 1000000d;

//        Log.d(TAG, "update_signal_value: strength = " + strength);
//        Log.d(TAG, "update_signal_value: quality = " + quality);
//        Log.d(TAG, "update_signal_value: snr = " + snr);
//        Log.d(TAG, "update_signal_value: ber = " + ber);

        g_signal_detect_view.update_signal_value(lock, strength, quality, snr, ber);
    }

    public String get_detect_info_string(int tunerId) {
        int strength = g_prime_dtv.get_signal_strength(tunerId) / 1000 + DBM_TO_DBMV;
        // tuner framework quality return percent
        int quality = g_prime_dtv.get_signal_quality(tunerId);
        // tuner framework SNR return thousandths of a deciBel (0.001dB)
        int snr = g_prime_dtv.get_signal_snr(tunerId) / 1000;
        // tuner framework BER return the number of error bit per 1 billion bits.
        // divide by 1 million according to RTK
        double ber = g_prime_dtv.get_signal_ber(tunerId) / 1000000d;

        DecimalFormat ber_format = new DecimalFormat("0.0E+00");
        String str = "{\"qam_strength\":\""+strength+" dBmV\","
                + "\"qam_mer\":\""+snr+" dB\","
                + "\"qam\":\""+g_modulation_text+"\","
                + "\"symbol_rate\":\""+g_symbol_rate+"\","
                + "\"frequency\":\""+g_param_freq+"\","
                + "\"qam_ber\":\""+ber_format.format(ber)+"\"}";

        return str;
    }

    private void add_music_category_to_fav() {
        /*return;*/

        List<ProgramInfo> programInfoList = g_prime_dtv.get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
        List<MusicInfo> musicInfoList = g_prime_dtv.get_current_category(this);
        Log.d(TAG, "add_music_category_to_fav: musicInfoList size = " + musicInfoList.size());
        Log.d(TAG, "add_music_category_to_fav: programInfoList size = " + musicInfoList.size());
        if (musicInfoList.isEmpty())
            return;
        g_prime_dtv.category_update_to_fav(programInfoList, musicInfoList);
    }
}
