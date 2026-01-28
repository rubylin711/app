package com.prime.dmg.launcher.PVR.Management;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyFunction;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.CustomView.HintBanner;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.PrimeUsbReceiver;
import com.prime.dtv.PrimeVolumeReceiver;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.sysdata.PvrInfo;
import com.prime.dtv.sysdata.PvrRecFileInfo;
import com.prime.dtv.sysdata.PvrRecIdx;
import com.prime.dtv.utils.LogUtils;
import com.prime.dtv.utils.RecChannelInfo;
import com.prime.dtv.utils.TVMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** @noinspection CommentedOutCode, UnusedReturnValue */
public class RecordProgramsActivity extends BaseActivity implements PrimeDtv.DTVCallback {
    private static final String TAG = RecordProgramsActivity.class.getSimpleName();

    private static int PLAY_ID = -1;
    private static int BANNER_TIMEOUT = 5000;

    private PrimeDtv g_PrimeDtv;
    private SurfaceView g_SurfaceView;
    private RecordListView g_RecordListView;
    private Handler g_Handler;
    private PvrInfo.EnPlayStatus g_PlayStatus;
    private Runnable g_TimeoutHideProgressBanner;
    private int g_currentPlaybackMasterKey, g_currentPlaybackSeriesKey;
    private ChannelChangeManager g_chChangeMgr;
    private int g_tunerId=0;
    private boolean g_progress_bar_start = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_programs);
        setup_all();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            g_PrimeDtv = HomeApplication.get_prime_dtv();
            g_PrimeDtv.set_surface_view(getApplicationContext(), g_SurfaceView);
            g_chChangeMgr = ChannelChangeManager.get_instance(getApplicationContext());
            runOnUiThread(this::on_construct_PrimeDtv);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(PLAY_ID < 0) {
            Log.e(TAG, "need stop playback");
            stop_playback();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        g_Handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
        int type = msg.getMsgType();
        int flag = msg.getMsgFlag();
        //FLAG_PVR, TYPE_PVR_PLAY_ERROR
        if(flag == TVMessage.FLAG_PVR){
            switch(type){
                case TVMessage.TYPE_PVR_PLAY_ERROR:
                    stop_playback();
                    break;
                case TVMessage.TYPE_PVR_PLAY_EOF:
                    Log.d(TAG, "onMessage : TVMessage.FLAG_PVR, TVMessage.TYPE_PVR_PLAY_EOF");
                    stop_playback();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
        String action = intent.getAction();
        if (action == null) {
            Log.e(TAG, "onBroadcastMessage: action == null");
            return;
        }

        switch (action) {
            case PrimeUsbReceiver.ACTION_MEDIA_MOUNTED:
                break;
            case PrimeUsbReceiver.ACTION_MEDIA_UNMOUNTED:
                finish();
                break;
            case PrimeVolumeReceiver.ACTION_VOLUME_CHANGED:
            case PrimeVolumeReceiver.ACTION_STREAM_MUTE_CHANGED:
                on_volume_changed();
                break;
        }
    }

    void on_construct_PrimeDtv() {
        Log.d(TAG, "on_construct_PrimeDtv: ");
        int i,j,stopAv;
        RecChannelInfo[] g_RecChannels = new RecChannelInfo[Pvcfg.NUM_OF_RECORDING];
        for(i=0;i<Pvcfg.NUM_OF_RECORDING;i++){
            g_RecChannels[i]=g_chChangeMgr.get_rec_channel_info(i);
        }
        BANNER_TIMEOUT = g_PrimeDtv.gpos_info_get().getIntroductionTime();
        PLAY_ID = -1;
        setup_space_info();
        setup_record_list();
        for(i=0;i<3;i++){
            stopAv=1;
            for(j=0;j<Pvcfg.NUM_OF_RECORDING;j++){
                if((g_RecChannels[j] != null) && (g_RecChannels[j].getTunerId() == i)){
                    stopAv=0;
                    Log.d(TAG, "g_RecChannels["+j+"].getTunerId()="+g_RecChannels[j].getTunerId());
                    break;
                }
            }
            if(stopAv == 1) {
                g_PrimeDtv.av_control_play_stop(i, 0, 1);
                Log.d(TAG, "av_control_play_stop, tuner id="+i);
            }
        }
    }

    void on_input_keycode() {
        g_SurfaceView.setOnKeyListener((v, keyCode, event) -> {
            if (KeyEvent.ACTION_DOWN != event.getAction())
                return false;

            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    Log.d(TAG, "on_input_keycode: KEYCODE_BACK");
                    return stop_playback();
                case KeyEvent.KEYCODE_INFO:
                    Log.d(TAG, "on_input_keycode: KEYCODE_INFO");
                    return show_progress_banner();
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    Log.d(TAG, "on_input_keycode: KEYCODE_DPAD_CENTER");
                    return handle_playback();
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    Log.d(TAG, "on_input_keycode: KEYCODE_DPAD_RIGHT");
                    return forward_playback();
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    Log.d(TAG, "on_input_keycode: KEYCODE_DPAD_LEFT");
                    return rewind_playback();
                case KeyEvent.KEYCODE_PROG_GREEN:
                    Log.d(TAG, "on_input_keycode: KEYCODE_PROG_GREEN");
                    return show_function();
                default:
                    Log.i(TAG, "on_input_keycode: keyCode = " + keyCode);
            }

            return false;
        });
    }

    void on_volume_changed() {
        AudioManager audioManager = getSystemService(AudioManager.class);
        int          audioVolume  = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        //Log.e(TAG, "on_volume_changed: " + audioVolume);
        if (audioVolume == 0)
            show_mute();
        else
            hide_mute();
    }

    void setup_all() {
        Log.d(TAG, "setup_all: ");
        g_Handler = new Handler(getMainLooper());
        g_SurfaceView = findViewById(R.id.record_player);
        g_RecordListView = findViewById(R.id.record_program_list);
        g_currentPlaybackMasterKey = -1;
        g_currentPlaybackSeriesKey = -1;
        on_volume_changed();
        on_input_keycode();
        setup_space_info();
        setup_record_list();
        setup_normal_screen();
    }

    void setup_space_info() {
        List<Long> usbSize = get_space_info();
        long totalSize = usbSize.get(0);
        long availableSize = usbSize.get(1);
        long usedSize = totalSize - availableSize;
        int percent = (int) (usedSize * 100 / totalSize);

        Log.d(TAG, "setup_space_information: Total Size     : " + totalSize + " MB");
        Log.d(TAG, "setup_space_information: Available Size : " + availableSize + " MB");

        ProgressBar progressBar = findViewById(R.id.hdd_space_progress);
        progressBar.setMax(100);
        progressBar.setProgress(percent);

        int recordCount = g_PrimeDtv != null ? g_PrimeDtv.pvr_GetRecCount() : 0;
        TextView spaceMessage = findViewById(R.id.hdd_space_message);
        spaceMessage.setText(getString(R.string.pvr_record_programs_message, percent + "%", recordCount));
    }

    void setup_record_list() {
        Log.d(TAG, "setup_record_list: ");
        RecordListView recordListView = findViewById(R.id.record_program_list);
        recordListView.setup_view(this);
    }

    boolean setup_full_screen() {
        Log.d(TAG, "setup_full_screen: hide Record List, hide Bottom Banner");
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) g_SurfaceView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.topMargin = 0;
        params.leftMargin = 0;
        g_SurfaceView.setLayoutParams(params);
        g_SurfaceView.setFocusable(true);

        RecordListView recordListView = findViewById(R.id.record_program_list);
        recordListView.setVisibility(View.GONE);

        HintBanner hintBanner = findViewById(R.id.record_program_list_hint_banner);
        hintBanner.setVisibility(View.GONE);

        View progressBanner = findViewById(R.id.progress_banner);
        progressBanner.setVisibility(View.VISIBLE);

        return true;
    }

    boolean setup_normal_screen() {
        Log.d(TAG, "setup_normal_screen: show Record List, show Bottom Banner");
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) g_SurfaceView.getLayoutParams();
        params.width = dp_to_px(349);
        params.height = dp_to_px(196);
        params.topMargin = dp_to_px(30);
        params.leftMargin = dp_to_px(30);
        g_SurfaceView.setLayoutParams(params);
        g_SurfaceView.setFocusable(false);

        RecordListView recordListView = findViewById(R.id.record_program_list);
        recordListView.setVisibility(View.VISIBLE);

        HintBanner hintBanner = findViewById(R.id.record_program_list_hint_banner);
        hintBanner.setVisibility(View.VISIBLE);
        hintBanner.hide_green();
        hintBanner.hide_yellow();

        View progressBanner = findViewById(R.id.progress_banner);
        progressBanner.setVisibility(View.INVISIBLE);

        return true;
    }

    void setup_status_speed() {
        PvrInfo.EnPlaySpeed playSpeed = g_PrimeDtv.pvr_PlayGetSpeed(PLAY_ID);

        Log.d(TAG, "setup_status_speed: play speed = " + playSpeed);
        String speed = switch (playSpeed) {
            //case PLAY_SPEED_ID_FWD01, PLAY_SPEED_ID_REV01 -> "1";
            case PLAY_SPEED_ID_FWD02, PLAY_SPEED_ID_REV02 -> "2";
            case PLAY_SPEED_ID_FWD04, PLAY_SPEED_ID_REV04 -> "4";
            case PLAY_SPEED_ID_FWD08, PLAY_SPEED_ID_REV08 -> "8";
            case PLAY_SPEED_ID_FWD16, PLAY_SPEED_ID_REV16 -> "16";
            case PLAY_SPEED_ID_FWD32, PLAY_SPEED_ID_REV32 -> "32";
            case PLAY_SPEED_ID_FWD64, PLAY_SPEED_ID_REV64 -> "64";
            case PLAY_SPEED_ID_FWD128, PLAY_SPEED_ID_REV128 -> "128";
            default -> "";
        };

        TextView txvSpeed = findViewById(R.id.banner_speed);
        txvSpeed.setText(speed);
    }

    void setup_status_icon() {
        ImageView playIcon = findViewById(R.id.banner_play_icon);
        ImageView pauseIcon = findViewById(R.id.banner_pause_icon);
        ImageView forwardIcon = findViewById(R.id.banner_forward_icon);
        ImageView rewindIcon = findViewById(R.id.banner_rewind_icon);

        playIcon.setVisibility(View.INVISIBLE);
        pauseIcon.setVisibility(View.INVISIBLE);
        forwardIcon.setVisibility(View.INVISIBLE);
        rewindIcon.setVisibility(View.INVISIBLE);

        boolean is_playing  = is_playing();
        boolean is_pause    = is_pause();
        boolean is_forward  = is_forward();
        boolean is_rewind   = is_rewind();

        if (is_playing) Log.d(TAG, "setup_status_icon: show play icon");
        else if (is_pause)   Log.d(TAG, "setup_status_icon: show pause icon");
        else if (is_forward) Log.d(TAG, "setup_status_icon: show forward icon");
        else if (is_rewind)  Log.d(TAG, "setup_status_icon: show rewind icon");
        else                 Log.d(TAG, "setup_status_icon: Unknown status");

        if (is_playing)      playIcon.setVisibility(View.VISIBLE);
        else if (is_pause)   pauseIcon.setVisibility(View.VISIBLE);
        else if (is_forward) forwardIcon.setVisibility(View.VISIBLE);
        else if (is_rewind)  rewindIcon.setVisibility(View.VISIBLE);
        else                 Toast.makeText(this, "Unknown status", Toast.LENGTH_SHORT).show();

    }

    @Override
    public PrimeDtv get_prime_dtv() {
        return g_PrimeDtv;
    }

    public Handler get_handler() {
        return g_Handler;
    }

    List<PvrRecFileInfo> get_record_list() {
        List<PvrRecFileInfo> allRecList = new ArrayList<>();

        if (g_PrimeDtv != null) {
            allRecList = g_PrimeDtv.pvr_GetRecLink(0, g_PrimeDtv.pvr_GetRecCount());

            // fill list with dummy data
            /*PvrRecFileInfo recordInfo = new PvrRecFileInfo();
            recordInfo.setEventName("AAABBBCCC");
            recordInfo.setStartTime(1231241241245L);
            recordInfo.setTotalRecordTime(12345);
            allRecList.add(recordInfo);
            PvrRecFileInfo recordInfo2 = new PvrRecFileInfo();
            recordInfo2.setEventName("DDDDEEEFFF");
            recordInfo2.setStartTime(32142333L);
            recordInfo2.setTotalRecordTime(34);
            allRecList.add(recordInfo2);
            PvrRecFileInfo recordInfo3 = new PvrRecFileInfo();
            recordInfo3.setEventName("HHHIIJJJ");
            recordInfo3.setStartTime(56341212342345L);
            recordInfo3.setTotalRecordTime(145342);
            recordInfo3.setIsSeries(true);
            allRecList.add(recordInfo3);
            PvrRecFileInfo recordInfo4 = new PvrRecFileInfo();
            recordInfo4.setEventName("KKKLLLMM");
            recordInfo4.setStartTime(56341212342345L);
            recordInfo4.setTotalRecordTime(145342);
            recordInfo4.setIsSeries(true);
            allRecList.add(recordInfo4);*/
        }
        Log.i(TAG, "get_record_list: record size = " + allRecList.size());

        // fill list size to 6
        fill_list_size_to_6(allRecList);

        return allRecList;
    }

    List<PvrRecFileInfo> get_series_record_list(PvrRecFileInfo recFileInfo) {
        List<PvrRecFileInfo> allRecList = new ArrayList<>();

        if (g_PrimeDtv != null) {
            int masterIndex = recFileInfo.getMasterIdx();
            int seriesIndex = 0;
            PvrRecIdx tableKeyInfo = new PvrRecIdx(masterIndex, seriesIndex);
            allRecList = g_PrimeDtv.pvr_GetSeriesRecLink(tableKeyInfo, g_PrimeDtv.pvr_GetSeriesRecCount(masterIndex));

            // fill list with dummy data
            /*PvrRecFileInfo recordInfo = new PvrRecFileInfo();
            recordInfo.setEventName("111222333");
            recordInfo.setStartTime(1231241241245L);
            recordInfo.setTotalRecordTime(12345);
            allRecList.add(recordInfo);
            PvrRecFileInfo recordInfo2 = new PvrRecFileInfo();
            recordInfo2.setEventName("444555666");
            recordInfo2.setStartTime(32142333L);
            recordInfo2.setTotalRecordTime(34);
            allRecList.add(recordInfo2);
            PvrRecFileInfo recordInfo3 = new PvrRecFileInfo();
            recordInfo3.setEventName("777888999");
            recordInfo3.setStartTime(56341212342345L);
            recordInfo3.setTotalRecordTime(145342);
            allRecList.add(recordInfo3);
            PvrRecFileInfo recordInfo4 = new PvrRecFileInfo();
            recordInfo4.setEventName("0000000000");
            recordInfo4.setStartTime(59L);
            recordInfo4.setTotalRecordTime(59);
            allRecList.add(recordInfo4);*/
        }
        Log.i(TAG, "get_series_record_list: record size = " + allRecList.size());

        // fill list size to 6
        fill_list_size_to_6(allRecList);

        return allRecList;
    }

    public PvrRecFileInfo get_current_record() {
        RecordListAdapter adapter = (RecordListAdapter) g_RecordListView.getAdapter();
        if (adapter != null)
            return adapter.get_current_record();
        else
            return null;
    }

    List<Long> get_space_info() {
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        long totalSize = -1, availableSize = -1;
        Uri currentUri;
        String currentPath;

        for (StorageVolume currentVol : storageManager.getStorageVolumes()) {
            currentUri = Uri.fromFile(currentVol.getDirectory());
            currentPath = null == currentVol.getDirectory() ? null : currentVol.getDirectory().getPath();
            Log.d(TAG, "get_space_info: [currentUri] " + currentUri + ", [currentPath] " + currentPath);

            if (null == currentPath || currentPath.startsWith("/storage/emulated"))
                continue;

            File usbFile    = new File(currentVol.getDirectory().getPath());
            StatFs statFs   = new StatFs(usbFile.getPath());
            totalSize       = statFs.getBlockCountLong() * statFs.getBlockSizeLong() / (1024 * 1024);
            availableSize   = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong() / (1024 * 1024);
            break;
        }

        List<Long> usbSize = new ArrayList<>();
        usbSize.add(totalSize);
        usbSize.add(availableSize);
        return usbSize;
    }

    String get_description(long channelId, int eventId) {
        String description = "";

        if (g_PrimeDtv != null) {
            description = g_PrimeDtv.get_detail_description(channelId, eventId);
            if (null == description || description.isEmpty())
                description = g_PrimeDtv.get_short_description(channelId, eventId);
        }

        return description;
    }

    boolean is_playing() {
        g_PlayStatus = g_PrimeDtv.pvr_PlayGetPlayStatus(PLAY_ID);
        return PvrInfo.EnPlayStatus.PLAY_STATUS_PLAY == g_PlayStatus;
    }

    boolean is_pause() {
        g_PlayStatus = g_PrimeDtv.pvr_PlayGetPlayStatus(PLAY_ID);
        return PvrInfo.EnPlayStatus.PLAY_STATUS_PAUSE == g_PlayStatus;
    }

    boolean is_forward() {
        g_PlayStatus = g_PrimeDtv.pvr_PlayGetPlayStatus(PLAY_ID);
        PvrInfo.EnPlaySpeed playSpeed = g_PrimeDtv.pvr_PlayGetSpeed(PLAY_ID);
        return PvrInfo.EnPlayStatus.PLAY_STATUS_SCAN == g_PlayStatus && (
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD01 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD02 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD04 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD08 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD16 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD32 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD64 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD128 == playSpeed);
    }

    boolean is_rewind() {
        g_PlayStatus = g_PrimeDtv.pvr_PlayGetPlayStatus(PLAY_ID);
        PvrInfo.EnPlaySpeed playSpeed = g_PrimeDtv.pvr_PlayGetSpeed(PLAY_ID);
        return PvrInfo.EnPlayStatus.PLAY_STATUS_SCAN == g_PlayStatus && (
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV01 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV02 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV04 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV08 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV16 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV32 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV64 == playSpeed ||
                PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV128 == playSpeed);
    }

    boolean is_stop() {
        g_PlayStatus = g_PrimeDtv.pvr_PlayGetPlayStatus(PLAY_ID);
        return PvrInfo.EnPlayStatus.PLAY_STATUS_STOP == g_PlayStatus;
    }

    boolean handle_progress_start() {
        LogUtils.d( "handle_progress_start: "+g_progress_bar_start);
        if(g_progress_bar_start == true){
            return false;
        }
        g_progress_bar_start = true;
        int max = get_current_record().getDurationSec();// / 1000;

        // set end time
        TextView txvEndTime = findViewById(R.id.banner_end_time);
        txvEndTime.setText(MiniEPG.sec_to_duration(max));

        // set max
        ProgressBar progressBar = findViewById(R.id.banner_time_progress);
        progressBar.setMax(max);

        // set progress & start time
        g_Handler.post(new Runnable() {
        @Override public void run() {
            if (is_playing() || is_forward() || is_rewind()) {

                // set progress
                int progress = g_PrimeDtv.pvr_PlayGetPlayTime(PLAY_ID);
                progressBar.setProgress(progress);

                // set start time
                TextView txvStartTime = findViewById(R.id.banner_start_time);
                txvStartTime.setText(MiniEPG.sec_to_duration(progress));

                // stop progress banner
                Log.d(TAG, "handle_progress_start: progress = " + progress + " / " + progressBar.getMax() + ", time = " + txvStartTime.getText() + " / " + txvEndTime.getText());
                int max = progressBar.getMax();
                if (progress >= max && max > 0) {
                    Runnable stopProgressBanner = () -> {
                        Log.d(TAG, "handle_progress_start: send KEYCODE_BACK");
                        g_SurfaceView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                    };
                    g_Handler.postDelayed(stopProgressBanner, 1000);
                    return;
                }
            }
            else if (is_stop()) {
                Log.d(TAG, "handle_progress_start: do not start progress banner, status is stop");
                g_progress_bar_start = false;
                return;
            }
            else if (is_pause()){
                show_progress_banner(false); // start_progress_banner - pause
                setup_status_speed(); // start_progress_banner - pause
                setup_status_icon(); // start_progress_banner - pause
            }

            // redo
            g_Handler.postDelayed(this, 1000);
        }});

        return true;
    }

    boolean handle_progress_stop() {
        Log.d(TAG, "handle_progress_stop: ");
        g_progress_bar_start = false;
        g_Handler.removeCallbacksAndMessages(null);
        return true;
    }

    boolean show_progress_banner() {
        return show_progress_banner(true); // show_progress_banner
    }

    boolean show_progress_banner(boolean autoClose) {
        View bannerView = findViewById(R.id.progress_banner);
        bannerView.setVisibility(View.VISIBLE);

        HintBanner fullscreenHintBanner = findViewById(R.id.hint_banner_of_progress);
        fullscreenHintBanner.show_back();
        fullscreenHintBanner.hide_red();
        fullscreenHintBanner.hide_blue();
        fullscreenHintBanner.hide_yellow();
        fullscreenHintBanner.hide_ok();
        fullscreenHintBanner.hide_time();

        if (g_TimeoutHideProgressBanner == null)
            g_TimeoutHideProgressBanner = () -> bannerView.setVisibility(View.INVISIBLE);

        g_Handler.removeCallbacks(g_TimeoutHideProgressBanner);
        if (autoClose) {
            Log.d(TAG, "show_progress_banner: auto close after " + BANNER_TIMEOUT + " ms");
            g_Handler.postDelayed(g_TimeoutHideProgressBanner, BANNER_TIMEOUT);
        }

        handle_progress_start();

        return true;
    }

    boolean hide_progress_banner() {
        Log.d(TAG, "hide_progress_banner: ");
        handle_progress_stop();
        View bannerView = findViewById(R.id.progress_banner);
        bannerView.setVisibility(View.INVISIBLE);
        return true;
    }

    boolean handle_playback() {
        g_PlayStatus = g_PrimeDtv.pvr_PlayGetPlayStatus(PLAY_ID);
        Log.d(TAG, "handle_playback: play status = " + g_PlayStatus);

        if (is_playing() || is_forward() || is_rewind())
            return pause_playback();
        else if (is_pause())
            return resume_playback();
        else
            Toast.makeText(this, "Unknown status", Toast.LENGTH_SHORT).show();
        return false;
    }

    boolean start_playback(PvrRecFileInfo recordInfo, boolean fullscreen, boolean fromLastPosition) {
        if (null == g_PrimeDtv || null == g_RecordListView)
            return false;

        boolean success = false;
        RecChannelInfo[] g_RecChannels = new RecChannelInfo[Pvcfg.NUM_OF_RECORDING];
        int i,j;
        //int masterIndex = g_RecordListView.get_master_index();
        int masterIndex = get_current_record().getMasterIdx();
        //int seriesIndex = g_RecordListView.get_series_index();
        int seriesIndex = get_current_record().getSeriesIdx();
        Log.d(TAG, "start_playback: playback MasterKey = " + g_currentPlaybackMasterKey + " / " + masterIndex);
        Log.d(TAG, "start_playback: playback SeriesKey = " + g_currentPlaybackSeriesKey + " / " + seriesIndex);

        if (g_currentPlaybackMasterKey == masterIndex &&
            g_currentPlaybackSeriesKey == seriesIndex) {
            if (fullscreen)
                success = setup_full_screen() && show_progress_banner();
            Log.i(TAG, "start_playback: do not start playback");
            return success;
        }

        for(i=0;i<Pvcfg.NUM_OF_RECORDING;i++){
            g_RecChannels[i]=g_chChangeMgr.get_rec_channel_info(i);
        }

        for(i=0;i<3;i++){
            g_tunerId=i;
            for(j=0;j<Pvcfg.NUM_OF_RECORDING;j++){
                if((g_RecChannels[j] != null) && (g_RecChannels[j].getTunerId() == i)) {
                    Log.d(TAG, "tuner id="+i+", This tuner id has been used by record");
                    g_tunerId = -1;
                }
            }
            if(g_tunerId != -1)
                break;
        }

        if(g_tunerId == -1){
            Log.i(TAG, "start_playback: No resources available(TunerId)");
            return success;
        }
        if(!is_stop())
            stop_playback();
        Log.d(TAG, "tuner id="+g_tunerId+", This tuner id can be used by playback");
        PLAY_ID = g_chChangeMgr.pvr_playback_start(new PvrRecIdx(masterIndex, seriesIndex), fromLastPosition, g_tunerId);

        Log.d(TAG, "start_playback: PLAY_ID = " + PLAY_ID + ", masterIndex = " + masterIndex + ", seriesIndex = " + seriesIndex + ", channel name = " + recordInfo.getChName());
        g_currentPlaybackMasterKey = masterIndex;
        g_currentPlaybackSeriesKey = seriesIndex;
        if (fullscreen)
            success = setup_full_screen() && show_progress_banner();
        handle_progress_start();
        setup_status_speed(); // start_playback
        setup_status_icon(); // start_playback
        Log.d(TAG, "start_playback: success = " + success);
        return success;
    }

    boolean stop_playback() {
        if (null == g_PrimeDtv)
            return true;

        boolean success = g_PrimeDtv.pvr_PlayFileStop(PLAY_ID) > 0;
        PLAY_ID = -1;
        Log.d(TAG, "stop_playback: stop playback, setup normal screen, success = " + success);
        g_currentPlaybackMasterKey = -1;
        g_currentPlaybackSeriesKey = -1;
        setup_normal_screen();
        handle_progress_stop();
        hide_progress_banner();
        return true;
    }

    boolean pause_playback() {
        int result = g_PrimeDtv.pvr_PlayPause(PLAY_ID);
        boolean success = result > 0;

        Log.d(TAG, "pause_playback: success = " + success);
        setup_status_speed(); // pause_playback
        setup_status_icon(); // pause_playback
        show_progress_banner(false); // pause_playback
        return success;
    }

    boolean resume_playback() {
        int result = g_PrimeDtv.pvr_PlayPlay(PLAY_ID);
        boolean success = result > 0;

        Log.d(TAG, "resume_playback: success = " + success);
        setup_status_speed(); // resume_playback
        setup_status_icon(); // resume_playback
        show_progress_banner(true); // resume_playback
        return success;
    }

    boolean forward_playback() {
        int result = g_PrimeDtv.pvr_PlayFastForward(PLAY_ID);
        boolean success = result > 0;

        Log.d(TAG, "forward_playback: success = " + success);
        setup_status_speed(); // forward_playback
        setup_status_icon(); // forward_playback
        show_progress_banner(false); // forward_playback
        return success;
    }

    boolean rewind_playback() {
        int result = g_PrimeDtv.pvr_PlayRewind(PLAY_ID);
        boolean success = result > 0;

        Log.d(TAG, "rewind_playback: success = " + success);
        setup_status_speed(); // rewind_playback
        setup_status_icon(); // rewind_playback
        show_progress_banner(false); // rewind_playback
        return success;
    }

    boolean delete_record(PvrRecFileInfo recordInfo) {
        if (null == g_PrimeDtv || null == g_RecordListView)
            return false;

        int master_index = recordInfo.getMasterIdx();//g_RecordListView.get_master_index();
        int series_index = recordInfo.getSeriesIdx();//g_RecordListView.get_series_index();
        boolean isSeries = recordInfo.isSeries();
        int result;

        Log.d(TAG, "delete_record: [master index] " + master_index + ", [series index] " + series_index
                + ", [series] " + isSeries + ", [channel] " + recordInfo.getChannelNo() + " " + recordInfo.getChName());

        if (isSeries)
            result = g_PrimeDtv.pvr_DelSeriesRecs(master_index);
        else
            result = g_PrimeDtv.pvr_DelOneRec(new PvrRecIdx(master_index, series_index));

        Log.d(TAG, "delete_record: result = " + result);
        return result > 0;
    }

    void fill_list_size_to_6(List<PvrRecFileInfo> allRecList) {
        int recordSize = allRecList.size();
        for (int i = 0; i < 6 - recordSize; i++)
            allRecList.add(new PvrRecFileInfo());
    }

    void show_mute() {
        ImageView muteIcon = findViewById(R.id.mute_of_record_program);
        muteIcon.setVisibility(View.VISIBLE);
    }

    void hide_mute() {
        ImageView muteIcon = findViewById(R.id.mute_of_record_program);
        muteIcon.setVisibility(View.INVISIBLE);
    }

    boolean show_function() {
        Log.d(TAG, "show_function: " + HotkeyFunction.class.getSimpleName());
        HotkeyFunction function = new HotkeyFunction(this);
        function.set_intro_time(10000);
        function.enable_playback_mode().show();
        return true;
    }

    int dp_to_px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

}
