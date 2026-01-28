package com.prime.launcher.CustomView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.prime.launcher.Home.Hotkey.HotkeyFunction;
import com.prime.launcher.Home.LiveTV.LiveTvManager;
import com.prime.launcher.Home.LiveTV.MiniEPG;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.R;
import com.prime.launcher.ChannelChangeManager;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.PvrInfo;

import java.lang.ref.WeakReference;

/** @noinspection UnusedReturnValue*/
public class TimeshiftBanner extends ConstraintLayout {

    private static final String TAG = TimeshiftBanner.class.getSimpleName();

    WeakReference <AppCompatActivity> g_ref;
    Handler gHandler;
    Runnable gSetupTimeProgress;
    LiveTvManager gLiveTvMgr;
    ChannelChangeManager gChangeMgr;
    HintBanner gHintBanner;
    ProgressBar gPlayProgressBar;
    ProgressBar gBufferProgressBar;
    TextView gTxvSpeed;
    TextView gTxvPlayTime;
    View gBtnPlayPause;
    View gBtnRewind;
    View gBtnForward;
    View gIconPlay;
    View gIconPause;
    View gIconStatus;
    View gLayerStatus;

    public TimeshiftBanner(@NonNull Context context) {
        super(context);
        init_layout(context);
    }

    public TimeshiftBanner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init_layout(context);
    }

    public TimeshiftBanner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init_layout(context);
    }

    public TimeshiftBanner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init_layout(context);
    }

    private void init_layout(Context context) {
        inflate(getContext(), R.layout.timeshift_banner, this);
        g_ref = new WeakReference<>((AppCompatActivity) context);
        gIconPlay = findViewById(R.id.play_timeshift);
        gIconPause = findViewById(R.id.pause_timeshift);
        gLayerStatus = findViewById(R.id.layer_status);
        gIconStatus = findViewById(R.id.icon_rewind_forward);
        gTxvSpeed = findViewById(R.id.txv_speed);
        init_hint_banner();
        init_progress_bar();
        init_listener();
    }

    private void init_hint_banner() {
        gHintBanner = findViewById(R.id.hint_timeshift);
        gHintBanner.hide_red();
        gHintBanner.hide_yellow();
        gHintBanner.set_green_text(R.string.hint_rcu_menu);
        gHintBanner.set_blue_text(R.string.hint_rcu_stop);
        gHintBanner.set_ok_text(R.string.hint_rcu_ok_confirm);
    }

    @SuppressLint("SetTextI18n")
    private void init_progress_bar() {
        gPlayProgressBar = findViewById(R.id.progress_play_timeshift);
        gPlayProgressBar.setProgress(0);
        gBufferProgressBar = findViewById(R.id.progress_buffer_timeshift);
        gBufferProgressBar.setProgress(0);
        gTxvPlayTime = findViewById(R.id.start_time_timeshift);
        gTxvPlayTime.setText("00:00:00");
    }

    private void init_listener() {
        gBtnPlayPause = findViewById(R.id.focus_play_pause_timeshift);
        gBtnRewind = findViewById(R.id.focus_rewind_timeshift);
        gBtnForward = findViewById(R.id.focus_forward_timeshift);

        // focus listener
        gBtnPlayPause.setOnFocusChangeListener((v, hasFocus) -> v.setBackgroundResource(hasFocus ? R.drawable.btn_control_focus : 0));
        gBtnRewind.setOnFocusChangeListener((v, hasFocus) -> v.setBackgroundResource(hasFocus ? R.drawable.btn_control_focus : 0));
        gBtnForward.setOnFocusChangeListener((v, hasFocus) -> v.setBackgroundResource(hasFocus ? R.drawable.btn_control_focus : 0));

        // key listener
        gBtnPlayPause.setOnKeyListener((v, keyCode, event) -> on_key_down(v, event, gBtnRewind, gBtnForward));
        gBtnRewind.setOnKeyListener((v, keyCode, event) -> on_key_down(v, event, gBtnRewind, gBtnPlayPause));
        gBtnForward.setOnKeyListener((v, keyCode, event) -> on_key_down(v, event, gBtnPlayPause, gBtnForward));
    }

    /** @noinspection SameReturnValue*/
    private boolean on_key_down(View view, KeyEvent event, View leftView, View rightView) {
        if (KeyEvent.ACTION_UP == event.getAction())
            return true;
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT -> leftView.requestFocus();
            case KeyEvent.KEYCODE_DPAD_RIGHT -> rightView.requestFocus();
            case KeyEvent.KEYCODE_DPAD_CENTER -> on_key_ok(view);
            case KeyEvent.KEYCODE_BACK -> hide();
            case KeyEvent.KEYCODE_PROG_BLUE -> timeshift_stop(false);
            case KeyEvent.KEYCODE_PROG_GREEN -> open_menu();
            case KeyEvent.KEYCODE_CHANNEL_UP,
                 KeyEvent.KEYCODE_CHANNEL_DOWN,
                 KeyEvent.KEYCODE_LAST_CHANNEL -> {
                Log.d(TAG, "on_key_down: channel key is pressed");
                hide();
                return false;
            }
            case KeyEvent.KEYCODE_0,
                 KeyEvent.KEYCODE_1,
                 KeyEvent.KEYCODE_2,
                 KeyEvent.KEYCODE_3,
                 KeyEvent.KEYCODE_4,
                 KeyEvent.KEYCODE_5,
                 KeyEvent.KEYCODE_6,
                 KeyEvent.KEYCODE_7,
                 KeyEvent.KEYCODE_8,
                 KeyEvent.KEYCODE_9 -> {
                Log.d(TAG, "on_key_down: number key is pressed");
                hide();
                return false;
            }
            default -> Log.w(TAG, "on_key_down: do nothing");
        }
        return true;
    }

    private void on_key_ok(View view) {
        if (view == gBtnPlayPause)
            on_click_play_pause();
        else if (view == gBtnRewind)
            on_click_rewind();
        else if (view == gBtnForward)
            on_click_forward();
    }

    private void on_click_play_pause() {
        gChangeMgr = get_channel_change_manager();

        if (is_timeshift_pause()) {
            gChangeMgr.pvr_TimeShiftPlayResume();
            gLayerStatus.setVisibility(INVISIBLE);
            gIconPause.setVisibility(VISIBLE);
            gIconPlay.setVisibility(INVISIBLE);
        }
        else {
            gChangeMgr.pvr_TimeShiftPlayPause();
            gLayerStatus.setVisibility(INVISIBLE);
            gIconPause.setVisibility(INVISIBLE);
            gIconPlay.setVisibility(VISIBLE);
        }
    }

    private void on_click_rewind() {
        if (!Pvcfg.supportTimeshiftRewind()) {
            Log.w(TAG, "on_click_rewind: rewind is not supported");
            return;
        }

        // rewind
        gChangeMgr = get_channel_change_manager();
        gChangeMgr.pvr_timeshift_rewind();

        // set speed
        PvrInfo.EnPlaySpeed speed = gChangeMgr.get_timeshift_speed();
        // TODO: set rewind speed
        Log.e(TAG, "on_click_rewind: speed = " + speed);
        gTxvSpeed.setText(switch (speed) {
            case PLAY_SPEED_ID_REV02 -> "2";
            case PLAY_SPEED_ID_REV04 -> "4";
            case PLAY_SPEED_ID_REV08 -> "8";
            case PLAY_SPEED_ID_REV16 -> "16";
            case PLAY_SPEED_ID_REV32 -> "32";
            default -> null;
        });

        // show layer
        gLayerStatus.setVisibility(VISIBLE);
        gIconPause.setVisibility(VISIBLE);
        gIconPlay.setVisibility(INVISIBLE);
        gIconStatus.setBackgroundResource(R.drawable.icon_timeshift_backward);
    }

    private void on_click_forward() {
        if (!Pvcfg.supportTimeshiftForward()) {
            Log.w(TAG, "on_click_forward: forward is not supported");
            return;
        }

        // forward
        gChangeMgr = get_channel_change_manager();
        gChangeMgr.pvr_timeshift_forward();

        // set speed
        PvrInfo.EnPlaySpeed speed = gChangeMgr.get_timeshift_speed();
        // TODO: set forward speed
        Log.e(TAG, "on_click_forward: speed = " + speed);
        gTxvSpeed.setText(switch (speed) {
            case PLAY_SPEED_ID_FWD02 -> "2";
            case PLAY_SPEED_ID_FWD04 -> "4";
            case PLAY_SPEED_ID_FWD08 -> "8";
            case PLAY_SPEED_ID_FWD16 -> "16";
            case PLAY_SPEED_ID_FWD32 -> "32";
            default -> null;
        });

        // show layer
        gLayerStatus.setVisibility(VISIBLE);
        gIconPause.setVisibility(VISIBLE);
        gIconPlay.setVisibility(INVISIBLE);
        gIconStatus.setBackgroundResource(R.drawable.icon_timeshift_forward);
    }

    private Runnable setup_time_progress() {
        gChangeMgr = get_channel_change_manager();
        gSetupTimeProgress = new Runnable() {
            @Override
            public void run() {
                PvrInfo.PlayTimeInfo timeInfo = gChangeMgr.get_timeshift_time_info();
                int playTime = timeInfo.mCurrentTime - timeInfo.mStartTime;
                int bufferTime = timeInfo.mEndTime - timeInfo.mStartTime;
                Log.d(TAG, "setup_time_progress: start time = " + timeInfo.mStartTime + ", current time = " + timeInfo.mCurrentTime + ", end time = " + timeInfo.mEndTime);
                gTxvPlayTime.setText(MiniEPG.sec_to_duration(playTime));
                gPlayProgressBar.setMax(3 * 60 * 60);
                gPlayProgressBar.setProgress(playTime);
                gBufferProgressBar.setMax(3 * 60 * 60);
                gBufferProgressBar.setProgress(bufferTime);
                gHandler.postDelayed(this, 1000);
            }
        };
        return gSetupTimeProgress;
    }

    private void set_banner_visible(boolean visible) {
        int visibility = visible ? VISIBLE : INVISIBLE;
        View timeshiftLayer = findViewById(R.id.timeshift_layer);
        timeshiftLayer.setVisibility(visibility);
        timeshiftLayer.requestFocus();
    }

    private HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    private Handler get_handler() {
        if (null == gHandler)
            gHandler = get().get_main_handler();
        return gHandler;
    }

    private LiveTvManager get_live_tv_manager() {
        if (null == gLiveTvMgr)
            gLiveTvMgr = get().get_live_tv_manager();
        return gLiveTvMgr;
    }

    private ChannelChangeManager get_channel_change_manager() {
        gLiveTvMgr = get_live_tv_manager();
        if (null == gChangeMgr)
            gChangeMgr = gLiveTvMgr.g_chChangeMgr;
        return gChangeMgr;
    }

    public boolean is_timeshift_start() {
        gChangeMgr = get_channel_change_manager();
        return gChangeMgr.is_timeshift_start();
    }

    private boolean is_timeshift_play() {
        gChangeMgr = get_channel_change_manager();
        return gChangeMgr.is_timeshift_play();
    }

    private boolean is_timeshift_pause() {
        gChangeMgr = get_channel_change_manager();
        return gChangeMgr.is_timeshift_pause();
    }

    private boolean is_timeshift_forward() {
        gChangeMgr = get_channel_change_manager();
        return gChangeMgr.is_timeshift_forward();
    }

    private boolean is_timeshift_rewind() {
        gChangeMgr = get_channel_change_manager();
        return gChangeMgr.is_timeshift_rewind();
    }

    public boolean timeshift_start() {
        gLiveTvMgr = get_live_tv_manager();
        gChangeMgr = get_channel_change_manager();
        MiniEPG miniEPG = gLiveTvMgr.g_miniEPG;

        if (miniEPG.is_show()) {
            Log.w(TAG, "timeshift_start: miniEPG is visible");
            return false;
        }
        if (!gLiveTvMgr.is_tuner_lock()) {
            Log.w(TAG, "timeshift_start: tuner is unlock");
            return false;
        }
        if (gChangeMgr.is_timeshift_start()) {
            Log.w(TAG, "timeshift_start: timeshift is already started");
            show();
            return false;
        }

        gChangeMgr.pvr_timeshift_start();
        gChangeMgr.set_timeshift_callback_started(true);
        return true;
    }

    public boolean timeshift_stop(boolean force) {
        gChangeMgr = get_channel_change_manager();

        if (!is_timeshift_start()) {
            Log.w(TAG, "timeshift_stop: there is no timeshift");
            return false;
        }

        Log.d(TAG, "timeshift_stop: force = " + force);
        if (force) {
            gChangeMgr.pvr_timeshift_stop();
            gChangeMgr.set_timeshift_callback_started(false);
            gChangeMgr.change_default_channel();
            hide();
            return true;
        }

        if (is_timeshift_start()) {
            EventDialog dialog = new EventDialog(get());
            dialog.set_title_text(R.string.pvr_exit_pause_mode_title);
            dialog.set_confirm_text(R.string.pvr_exit_pause_mode_title_ok);
            dialog.set_cancel_text(R.string.pvr_exit_pause_mode_title_no);
            dialog.set_confirm_action(() -> {
                gChangeMgr.pvr_timeshift_stop();
                gChangeMgr.set_timeshift_callback_started(false);
                gChangeMgr.change_default_channel();
                hide();
            });
            dialog.show();
            return true;
        }
        return false;
    }

    private void open_menu() {
        // open menu
        HotkeyFunction menuDialog = new HotkeyFunction(get());
        menuDialog.set_intro_time(10000);
        menuDialog.set_destroy_action(() -> set_banner_visible(true));
        menuDialog.enable_timeshift_mode().show();
        set_banner_visible(false);
    }

    public void show() {
        setVisibility(VISIBLE);
        gBtnPlayPause.requestFocus();
        gHandler = get_handler();
        gHandler.postDelayed(setup_time_progress(), 1000);
    }

    public void hide() {
        setVisibility(GONE);
        gHandler = get_handler();
        gHandler.removeCallbacks(gSetupTimeProgress);
    }
}
