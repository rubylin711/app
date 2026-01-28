package com.prime.dmg.launcher.Home.Hotkey;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.CustomView.HintBanner;
import com.prime.dmg.launcher.EPG.Epg;
import com.prime.dmg.launcher.EPG.EpgActivity;
import com.prime.dmg.launcher.Home.LiveTV.LiveTvManager;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.BaseDialog;
import com.prime.dmg.launcher.Utils.JsonParser.AdPageItem;
import com.prime.dmg.launcher.R;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.TVMessage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HotkeyInfo extends BaseDialog {

    String TAG = HotkeyInfo.class.getSimpleName();

    @IntDef({Detail.NONE, Detail.PRESENT, Detail.FOLLOW, Detail.RECOMMEND, Detail.EPG_PRESENT, Detail.EPG_FOLLOW, Detail.EPG_END, Detail.BOOK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Detail {
        int NONE        = 0;
        int PRESENT     = 1;
        int FOLLOW      = 2;
        int RECOMMEND   = 3;
        int EPG_PRESENT = 4;
        int EPG_FOLLOW  = 5;
        int EPG_END     = 6;
        int BOOK        = 7;
    }

    WeakReference<AppCompatActivity> g_ref;
    LiveTvManager gLiveTvMgr;
    MiniEPG g_miniEPG;
    HintBanner gHintBanner;

    String  g_chNum;
    String  g_chName;
    String  g_pgName;
    String  g_timeStart;
    String  g_timeEnd;
    String  g_description;
    int     g_duration;
    int     g_progress;
    int     g_detail;
    int     g_gradingResId;
    int     g_qualityResId;

    // program data of ViewPager2
    AdPageItem g_pageItem;

    // action
    Runnable dismissAction, startAction;

    // detail info's hint
    private int redHintId = 0;
    private int yellowHintId = 0;

    public HotkeyInfo(@NonNull MiniEPG miniEPG) {
        super(miniEPG.get(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(miniEPG.get());
        g_miniEPG = miniEPG;
        gLiveTvMgr = get().g_liveTvMgr;
        g_chName  = get().getString(R.string.epg_no_data);
        g_pgName  = get().getString(R.string.epg_no_data);
    }

    public HotkeyInfo(AppCompatActivity activity, AdPageItem pageItem) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(activity);
        g_chName = get().getString(R.string.epg_no_data);
        g_pgName = get().getString(R.string.epg_no_data);
        g_pageItem = pageItem;
        get().g_liveTvMgr.set_mail_is_running(true);
    }

    public HotkeyInfo(EpgActivity epgActivity) {
        super(epgActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(epgActivity);
        g_chName = epgActivity.getString(R.string.epg_no_data);
        g_pgName = epgActivity.getString(R.string.epg_no_data);
    }

    public HotkeyInfo(AppCompatActivity activity) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(activity);
        g_chName = activity.getString(R.string.epg_no_data);
        g_pgName = activity.getString(R.string.epg_no_data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.color.trans);
            getWindow().setWindowAnimations(R.style.Theme_DMGLauncher_DialogAnimation);
            getWindow().setDimAmount(0);
        }
        setContentView(R.layout.live_tv_hotkey_intro_dialog);
        gHintBanner = findViewById(R.id.hint_of_detail_info);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (startAction != null)
            startAction.run();

        init_hint_banner();

        switch (g_detail) {
            case Detail.PRESENT:
            case Detail.FOLLOW:
                init_detail_of_mini_epg();
                break;
            case Detail.RECOMMEND:
                init_detail_of_recommend();
                break;
            case Detail.BOOK:
                init_detail_of_book();
                break;
            case Detail.EPG_PRESENT:
            case Detail.EPG_FOLLOW:
            case Detail.EPG_END:
                init_detail_of_epg();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (record_program(keyCode))
            Log.d(TAG, "onKeyDown: record program SUCCESS");
        else if (remind_program(keyCode))
            Log.d(TAG, "onKeyDown: remind program SUCCESS");
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    public void init_detail_of_epg() {
        TextView chNum  = findViewById(R.id.lo_intro_ch_num);
        TextView chName = findViewById(R.id.lo_intro_ch_name);
        TextView pgName = findViewById(R.id.lo_intro_program_title);
        TextView hint   = findViewById(R.id.lo_intro_hint);
        TextView startTime = findViewById(R.id.lo_intro_start_time);
        TextView endTime   = findViewById(R.id.lo_intro_end_time);
        TextView description = findViewById(R.id.lo_intro_description);
        ImageView quality = findViewById(R.id.lo_intro_quality);
        ImageView grading = findViewById(R.id.lo_intro_grading);
        ProgressBar introBar = findViewById(R.id.lo_intro_bar);

        chNum.setText(g_chNum);
        chName.setText(g_chName);
        pgName.setText(g_pgName);
        pgName.setSelected(true);
        hint.setText(Detail.EPG_PRESENT == g_detail ? R.string.hint_current_mini :
                Detail.EPG_FOLLOW  == g_detail ? R.string.hint_next_mini :
                Detail.EPG_END == g_detail ? R.string.hint_over_time_mini: R.string.dialog_lang_none);
        startTime.setText(g_timeStart);
        endTime.setText(g_timeEnd);
        quality.setImageResource(g_qualityResId);
        grading.setImageResource(g_gradingResId);
        description.setText(g_description);
        introBar.setMax(g_duration);
        introBar.setProgress(g_progress);

        if (Detail.EPG_END == g_detail)
            introBar.setVisibility(View.GONE);
    }

    public void init_detail_of_mini_epg() {
        TextView chNum  = findViewById(R.id.lo_intro_ch_num);
        TextView chName = findViewById(R.id.lo_intro_ch_name);
        TextView pgName = findViewById(R.id.lo_intro_program_title);
        TextView hint   = findViewById(R.id.lo_intro_hint);
        TextView startTime = findViewById(R.id.lo_intro_start_time);
        TextView endTime   = findViewById(R.id.lo_intro_end_time);
        TextView description = findViewById(R.id.lo_intro_description);
        ProgressBar introBar = findViewById(R.id.lo_intro_bar);
        ImageView quality = findViewById(R.id.lo_intro_quality);
        ImageView grading = findViewById(R.id.lo_intro_grading);

        chNum.setText(g_chNum);
        chName.setText(g_chName);
        chName.setSelected(true);
        pgName.setText(g_pgName);
        pgName.setSelected(true);
        hint.setText(Detail.PRESENT == g_detail ? R.string.hint_current_mini :
                     Detail.FOLLOW  == g_detail ? R.string.hint_next_mini : R.string.dialog_lang_none);
        startTime.setText(g_timeStart);
        endTime.setText(g_timeEnd);
        description.setText(g_description);
        introBar.setMax(g_duration);
        introBar.setProgress(g_progress);
        quality.setImageResource(g_qualityResId);
        grading.setImageResource(g_gradingResId);
    }

    public void init_detail_of_recommend() {
        TextView chNum  = findViewById(R.id.lo_intro_ch_num);
        TextView chName = findViewById(R.id.lo_intro_ch_name);
        TextView pgName = findViewById(R.id.lo_intro_program_title);
        TextView hint   = findViewById(R.id.lo_intro_hint);
        TextView startTime = findViewById(R.id.lo_intro_start_time);
        TextView endTime   = findViewById(R.id.lo_intro_end_time);
        TextView description = findViewById(R.id.lo_intro_description);
        TextView introDash = findViewById(R.id.lo_intro_dash);
        ProgressBar introBar = findViewById(R.id.lo_intro_bar);

        introBar.setVisibility(View.GONE);
        introDash.setVisibility(View.VISIBLE);

        if (get_start_time_ms() > System.currentTimeMillis())
            hint.setText(R.string.hint_next_mini);
        else
            hint.setText(R.string.hint_over_time_mini);

        startTime.setText(get_start_time());
        endTime.setText(get_end_time());

        if (null == g_pageItem.get_title() || g_pageItem.get_title().equals("null")) {
            chNum.setText(R.string.no_data);
            chName.setText("");
            pgName.setText("");
            description.setText("");
        }
        else {
            chNum.setText(g_pageItem.get_channel_num());
            chName.setText(g_pageItem.get_channel_name());
            pgName.setText(g_pageItem.get_title());
            pgName.setSelected(true);
            description.setText(g_pageItem.get_description());
        }
    }

    private void init_detail_of_book() {
        TextView chNum  = findViewById(R.id.lo_intro_ch_num);
        TextView chName = findViewById(R.id.lo_intro_ch_name);
        TextView pgName = findViewById(R.id.lo_intro_program_title);
        TextView hint   = findViewById(R.id.lo_intro_hint);
        TextView startTime = findViewById(R.id.lo_intro_start_time);
        TextView endTime   = findViewById(R.id.lo_intro_end_time);
        TextView description = findViewById(R.id.lo_intro_description);
        ProgressBar introBar = findViewById(R.id.lo_intro_bar);

        chNum.setText(g_chNum);
        chName.setText(g_chName);
        pgName.setText(g_pgName);
        hint.setText(R.string.dvr_mgr_pre_record_time);
        startTime.setText(g_timeStart);
        endTime.setVisibility(View.GONE);
        description.setText(g_description);
        introBar.setVisibility(View.GONE);
    }

    public void init_hint_banner() {
        gHintBanner.hide_time();
        gHintBanner.hide_red();
        gHintBanner.hide_green();
        gHintBanner.hide_blue();
        gHintBanner.hide_yellow();
        gHintBanner.hide_ok();

        if (!Pvcfg.getPVR_PJ())
            return;

        switch (g_detail) {
            case Detail.PRESENT, Detail.EPG_PRESENT -> init_present_hint();
            case Detail.FOLLOW, Detail.EPG_FOLLOW -> init_follow_hint();
            case Detail.EPG_END -> {
                gHintBanner.hide_red();
                gHintBanner.hide_yellow();
            }
            case Detail.BOOK -> {
                gHintBanner.set_hint_prev_page();
            }
        }
    }

    public void init_present_hint() {
        gHintBanner.show_red();
        gHintBanner.set_red_text(R.string.hint_rcu_record);
        if (redHintId > 0) {
            gHintBanner.set_red_text(redHintId);
            redHintId = 0;
        }
    }

    public void init_follow_hint() {
        gHintBanner.show_red();
        gHintBanner.show_yellow();
        gHintBanner.set_red_text(R.string.hint_rcu_record);
        gHintBanner.set_yellow_text(R.string.hint_rcu_remind);
        if (redHintId > 0) {
            gHintBanner.set_red_text(redHintId);
            redHintId = 0;
        }
        if (yellowHintId > 0) {
            gHintBanner.set_yellow_text(yellowHintId);
            yellowHintId = 0;
        }
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public AppCompatActivity get_activity() {
        return g_ref.get();
    }

    public String get_start_time() {
        SimpleDateFormat sdf_date_time = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
        SimpleDateFormat sdf_time = new SimpleDateFormat("MM/dd HH:mm", Locale.ENGLISH);
        String formattedTime = null;

        try {
            Date dateStart = sdf_date_time.parse(g_pageItem.get_start_time());
            assert dateStart != null;
            formattedTime = sdf_time.format(dateStart);
        }
        catch (ParseException e) {
            Log.w(TAG, "get_start_time: " + e);
            formattedTime = "00/00 00:00";
        }
        return formattedTime;
    }

    public long get_start_time_ms() {
        SimpleDateFormat sdf_date_time = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);

        try {
            Date dateStart = sdf_date_time.parse(g_pageItem.get_start_time());
            if (null == dateStart)
                return 0;
            return dateStart.getTime();
        }
        catch (ParseException e) {
            Log.w(TAG, "get_start_time: " + e);
        }
        return 0;
    }

    public String get_end_time() {
        SimpleDateFormat sdf_date_time = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
        SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        String formattedTime = null;

        try {
            Date dateStart = sdf_date_time.parse(g_pageItem.get_end_time());
            assert dateStart != null;
            formattedTime = sdf_time.format(dateStart);
        }
        catch (ParseException e) {
            Log.w(TAG, "get_end_time: " + e);
            formattedTime = "00:00";
        }
        return formattedTime;
    }

    public String get_description(EPGEvent epgEvent) {
        if (null == epgEvent)
            return "";

        ChannelChangeManager changeManager = ChannelChangeManager.get_instance(get());
        long channelId = changeManager.get_cur_ch_id();
        int eventId = epgEvent.get_event_id();
        String description = get().g_dtv.get_detail_description(channelId, eventId);

        if (null == description || description.isEmpty())
            description = get().g_dtv.get_short_description(channelId, eventId);

        return description;
    }

    private int get_grading_resource_id(EPGEvent epgEvent, ProgramInfo channel) {
        return MiniEPG.get_grading_res_id(epgEvent, channel);
    }

    private int get_quality_resource_id(EPGEvent epgEvent, ProgramInfo channel) {
        if (null == epgEvent)
            return 0;
        return get_quality_resource_id(channel);
    }

    private int get_quality_resource_id(ProgramInfo channelInfo) {
        if (null == channelInfo)
            return 0;
        return get_quality_resource_id(channelInfo.getCategory_type());
    }

    private int get_quality_resource_id(long categoryType) {
        if (is_4k(categoryType))
            return R.drawable.icon_4k;
        else if (is_hd(categoryType))
            return R.drawable.icon_tv_hd;
        else
            return 0;
    }

    private boolean is_hd(long category) {
        return ((category & 0x0100)>>8) == 1;
    }

    private boolean is_4k(long category) {
        return ((category & 0x0800)>>11) == 1;
    }

    public void set_start_action(Runnable action) {
        startAction = action;
    }

    public void set_dismiss_action(Runnable action) {
        dismissAction = action;
    }

    public void set_red_hint(int resId) {
        redHintId = resId;
        if (gHintBanner != null)
            gHintBanner.set_red_text(resId);
    }

    public void set_yellow_hint(int resId) {
        yellowHintId = resId;
        if (gHintBanner != null)
            gHintBanner.set_yellow_text(resId);
    }

    public void show(@Detail int detail, EPGEvent epgEvent) {
        MiniEPG.update_timezone_offset();
        g_detail = detail;
        if (Detail.NONE      == g_detail) Log.w(TAG, "show: nothing");
        if (Detail.PRESENT   == g_detail) show_detail_present(epgEvent, false);
        if (Detail.FOLLOW    == g_detail) show_detail_follow(epgEvent);
        if (Detail.RECOMMEND == g_detail) show_detail_recommend();
    }

    public void show_detail_present(EPGEvent epgEvent, boolean isRadio) {
        ProgramInfo channel = get().get_live_tv_manager().get_current_channel();
        String NO_EPG_DATA = getContext().getString(R.string.epg_no_data);

        // set program number and name
        if (isRadio)
            g_detail = Detail.PRESENT;
        g_chNum     = channel == null ? "000"       : channel.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM);
        g_chName    = channel == null ? NO_EPG_DATA : channel.getDisplayName();

        // set program name, start time and end time
        g_pgName    = epgEvent == null ? NO_EPG_DATA : epgEvent.get_event_name();
        g_timeStart = epgEvent == null ? "00/00 00:00" : MiniEPG.ms_to_time(epgEvent.get_start_time(MiniEPG.TIME_ZONE_OFFSET), "MM/dd HH:mm");
        g_timeEnd   = epgEvent == null ? "00:00"       : MiniEPG.ms_to_time(epgEvent.get_end_time(MiniEPG.TIME_ZONE_OFFSET), "HH:mm");

        // set description, quality and grading
        g_description = get_description(epgEvent);
        g_qualityResId = get_quality_resource_id(epgEvent, channel);
        g_gradingResId = get_grading_resource_id(epgEvent, channel);

        // set progress
        int currentTime = (int) System.currentTimeMillis();
        int startTime   = epgEvent == null ? 0 : (int) epgEvent.get_start_time(MiniEPG.TIME_ZONE_OFFSET);
        int endTime     = epgEvent == null ? 0 : (int) epgEvent.get_end_time(MiniEPG.TIME_ZONE_OFFSET);
        g_duration      = endTime - startTime;
        g_progress      = currentTime - startTime;

        Log.d(TAG, "show_detail_present: [number] " + g_chNum + ", [channel] " + g_chName + ", [program] " + g_pgName);
        super.show();
    }

    public void show_detail_follow(EPGEvent epgEvent) {
        TextView followNum = g_miniEPG.findViewById(R.id.lo_mini_epg_ch_num);
        TextView followCh  = g_miniEPG.findViewById(R.id.lo_mini_epg_ch_title);
        TextView followPg  = g_miniEPG.findViewById(R.id.lo_mini_epg_next_pg_title);
        TextView followDate  = g_miniEPG.findViewById(R.id.lo_mini_epg_date);
        TextView followStart = g_miniEPG.findViewById(R.id.lo_mini_epg_next_pg_time_start);
        TextView followEnd   = g_miniEPG.findViewById(R.id.lo_mini_epg_next_pg_time_end);
        int currentTime, startTime, endTime;

        if (null == followNum || null == followCh || null == followPg ||
            null == followDate || null == followStart || null == followEnd)
            return;

        g_chNum     = followNum.getText().toString();
        g_chName    = followCh.getText().toString();
        g_pgName    = followPg.getText().toString();
        g_timeStart = followDate.getText().toString() + " " + followStart.getText().toString();
        g_timeEnd   = followEnd.getText().toString();
        g_description = (epgEvent != null) ? get_description(epgEvent) : "";

        ProgramInfo channel = get().get_live_tv_manager().get_current_channel();
        g_qualityResId = get_quality_resource_id(channel.getCategory_type());
        g_gradingResId = get_grading_resource_id(epgEvent, channel);

        if (epgEvent == null)
            g_duration = g_progress = 0;
        else {
            currentTime = (int) System.currentTimeMillis();
            startTime = (int) epgEvent.get_start_time(MiniEPG.TIME_ZONE_OFFSET);
            endTime = (int) epgEvent.get_end_time(MiniEPG.TIME_ZONE_OFFSET);
            g_duration = endTime - startTime;
            g_progress = currentTime - startTime;
        }

        Log.d(TAG, "show_detail_follow: [number] " + g_chNum + ", [channel] " + g_chName + ", [program] " + g_pgName);
        super.show();
    }

    public void show_detail_recommend() {
        Log.d(TAG, "show_detail_recommend: [number] " + g_pageItem.get_channel_num() + ", [channel] " + g_pageItem.get_channel_name() + ", [program] " + g_pageItem.get_title());
        super.show();
    }

    public void show_detail_book(String chNum, String chName, String programName, String startTime, String description) {
        g_chNum         = chNum;
        g_chName        = chName;
        g_pgName        = programName;
        g_timeStart     = startTime;
        g_description   = description;

        g_detail = Detail.BOOK;
        super.show();
    }

    public void show(Epg epg, ProgramInfo channelInfo, EPGEvent epgEvent) {
        String EPG_NO_DATA = getContext().getString(R.string.epg_no_data);

        MiniEPG.update_timezone_offset();
        g_chNum         = channelInfo == null ? "000"       : channelInfo.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM);
        g_chName        = channelInfo == null ? EPG_NO_DATA : channelInfo.getDisplayName();
        g_pgName        = epgEvent == null ? EPG_NO_DATA   : epgEvent.get_event_name();
        g_timeStart     = epgEvent == null ? "00/00 00:00" : MiniEPG.ms_to_time(epgEvent.get_start_time(MiniEPG.TIME_ZONE_OFFSET), "MM/dd HH:mm");
        g_timeEnd       = epgEvent == null ? "00:00"       : MiniEPG.ms_to_time(epgEvent.get_end_time(MiniEPG.TIME_ZONE_OFFSET));
        g_description   = epgEvent == null || channelInfo == null ? EPG_NO_DATA : epg.get_detail_description(channelInfo.getChannelId(), epgEvent.get_event_id());
        g_gradingResId = get_grading_resource_id(epgEvent, channelInfo);
        g_qualityResId = get_quality_resource_id(channelInfo);

        int currentTime, startTime, endTime;

        if (epgEvent == null)
            g_duration = g_progress = 0;
        else {
            currentTime = (int) System.currentTimeMillis();
            startTime = (int) epgEvent.get_start_time(MiniEPG.TIME_ZONE_OFFSET);
            endTime = (int) epgEvent.get_end_time(MiniEPG.TIME_ZONE_OFFSET);
            g_duration = endTime - startTime;
            g_progress = currentTime - startTime;
            g_detail = checkTime(epgEvent.get_start_time(MiniEPG.TIME_ZONE_OFFSET), epgEvent.get_end_time(MiniEPG.TIME_ZONE_OFFSET));
        }

        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (get_activity() instanceof HomeActivity homeActivity)
            homeActivity.g_liveTvMgr.set_mail_is_running(false);
        if (dismissAction != null)
            dismissAction.run();
    }

    private int checkTime(long startTime, long endTime) {
        Date date = new Date();
        long currentTime = date.getTime();
        if (currentTime < startTime)
            return Detail.EPG_FOLLOW;
        else if (currentTime > startTime && currentTime < endTime)
            return Detail.EPG_PRESENT;
        else
            return Detail.EPG_END;
    }

    private boolean record_program(int keyCode) {
        if (KeyEvent.KEYCODE_PROG_RED != keyCode)
            return false;

        if (get_activity() instanceof HomeActivity) {
            g_miniEPG.setup_record();
            return true;
        }
        else if (get_activity() instanceof EpgActivity epgActivity) {
            epgActivity.setup_record();
            return true;
        }

        return false;
    }

    private boolean remind_program(int keyCode) {
        if (KeyEvent.KEYCODE_PROG_YELLOW != keyCode)
            return false;

        if (get_activity() instanceof HomeActivity) {
            g_miniEPG.setup_remind();
            return true;
        }
        else if (get_activity() instanceof EpgActivity epgActivity) {
            epgActivity.setup_remind();
            return true;
        }

        return false;
    }
}
