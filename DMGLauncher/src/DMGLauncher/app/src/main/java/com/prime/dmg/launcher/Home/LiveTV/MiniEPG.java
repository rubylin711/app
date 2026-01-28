package com.prime.dmg.launcher.Home.LiveTV;

import static com.prime.dmg.launcher.Home.Hotkey.HotkeyInfo.Detail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.CustomView.EventDialog;
import com.prime.dmg.launcher.CustomView.Snakebar;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyFunction;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyGenre;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyInfo;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyRecord;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyRemind;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.RecChannelInfo;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** @noinspection CommentedOutCode*/
@SuppressLint("DiscouragedApi")
public class MiniEPG extends ConstraintLayout {

    private static final String TAG = MiniEPG.class.getSimpleName();

    public static final int MAX_LENGTH_OF_CHANNEL_NUM   = 3;
    public static final int MAX_LENGTH_OF_SERVICE_ID    = 3;
    public static int DELAY_MINI_EPG = 5000;
    public static long TIME_ZONE_OFFSET;

    WeakReference<AppCompatActivity> g_ref;
    Handler g_handler;
    PrimeDtv gPrimeDtv;
    LiveTvManager gLiveTvMgr;
    ChannelChangeManager gChangeMgr;
    String g_genre;

    TextView gPresentStatus, gPresentTitle, gPresentStart, gPresentEnd;
    ImageView gPresentQuality, gPresentGrading;
    TextView gFollowStatus, gFollowTitle, gFollowStart, gFollowEnd;
    ImageView gFollowQuality, gFollowGrading;
    View g_currPrg;
    View g_nextPrg;
    View g_chTitle;
    ProgramInfo gCurChannel;
    EPGEvent g_epgEventP;
    EPGEvent g_epgEventF;

    HotkeyInfo gDetailInfo;
    HotkeyRecord gHotkeyRecord;
    HotkeyRemind gHotkeyRemind;
    HotkeyFunction gHotkeyFunction;

    final ExecutorService iconExecutor = Executors.newSingleThreadExecutor();
    final ExecutorService presentExecutor = Executors.newSingleThreadExecutor();
    final ExecutorService followExecutor = Executors.newSingleThreadExecutor();


    public MiniEPG(Context context) {
        super(context);
        init(context);
    }

    public MiniEPG(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MiniEPG(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public MiniEPG(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        // layout
        inflate(getContext(), R.layout.live_tv_mini_epg, this);
        // handle
        g_ref     = new WeakReference<>((AppCompatActivity) context);
        g_handler = new Handler(Looper.getMainLooper());
        // view
        g_currPrg = findViewById(R.id.lo_mini_epg_curr_pg);
        g_nextPrg = findViewById(R.id.lo_mini_epg_next_pg);
        g_chTitle = findViewById(R.id.lo_mini_epg_ch_title);
        gPresentStatus = findViewById(R.id.lo_mini_epg_curr_pg_status);
        gPresentTitle = findViewById(R.id.lo_mini_epg_curr_pg_title);
        gPresentStart = findViewById(R.id.lo_mini_epg_curr_pg_time_start);
        gPresentEnd = findViewById(R.id.lo_mini_epg_curr_pg_time_end);
        gPresentQuality = findViewById(R.id.lo_mini_epg_curr_pg_quality);
        gPresentGrading = findViewById(R.id.lo_mini_epg_curr_pg_grading);
        gFollowStatus = findViewById(R.id.lo_mini_epg_next_pg_status);
        gFollowTitle = findViewById(R.id.lo_mini_epg_next_pg_title);
        gFollowStart = findViewById(R.id.lo_mini_epg_next_pg_time_start);
        gFollowEnd = findViewById(R.id.lo_mini_epg_next_pg_time_end);
        gFollowQuality = findViewById(R.id.lo_mini_epg_next_pg_quality);
        gFollowGrading = findViewById(R.id.lo_mini_epg_next_pg_grading);
        g_genre = get_string(R.string.genre_all);
        // setup view
        init_hotkey_dialog();
        // callback
        on_focus_present();
        on_focus_follow();
        on_key_program();
    }

    public void init_hotkey_dialog() {
        gHotkeyRecord = new HotkeyRecord(get()) {
            @Override
            public void on_click_confirm(View v) {
                super.on_click_confirm(v);
            }
        };
        gHotkeyRemind = new HotkeyRemind(get()) {
            @Override
            public void on_click_yes() {
                super.on_click_yes();
            }
        };
    }

    public void on_focus_present() {

        // current program
        g_currPrg.setOnFocusChangeListener((v, hasFocus) -> {
            boolean isMarkRecord = is_mark_record();

            if (hasFocus)
                Log.d(TAG, "on_focus_present: [is Mark Record] " + isMarkRecord);

            set_highlight(R.id.lo_mini_epg_curr_pg_status, hasFocus);
            set_highlight(R.id.lo_mini_epg_curr_pg_time_start, hasFocus);
            set_highlight(R.id.lo_mini_epg_curr_pg_time_dash, hasFocus);
            set_highlight(R.id.lo_mini_epg_curr_pg_time_end, hasFocus);
            set_highlight(R.id.lo_mini_epg_curr_pg_title, hasFocus);
            set_event_quality(R.id.lo_mini_epg_curr_pg_quality);
            set_event_grading(R.id.lo_mini_epg_curr_pg_grading, g_epgEventP);
            set_event_progress(g_epgEventP);
            set_event_record_icon(R.id.lo_mini_epg_curr_pg_record, hasFocus, isMarkRecord);
            set_event_record_hint(hasFocus, isMarkRecord);
            set_detail_red_hint(hasFocus && isMarkRecord); // on_focus_present
            set_delay(DELAY_MINI_EPG, hasFocus);
            show_record_hint(hasFocus ? VISIBLE : GONE);
            gPresentTitle.setSelected(hasFocus);
        });
    }

    public void on_focus_follow() {

        // next program
        g_nextPrg.setOnFocusChangeListener((v, hasFocus) -> {
            boolean isMarkRecord = is_mark_record();

            if (hasFocus)
                Log.d(TAG, "on_focus_follow: [is Mark Record] " + isMarkRecord);

            set_highlight(R.id.lo_mini_epg_next_pg_status, hasFocus);
            set_highlight(R.id.lo_mini_epg_next_pg_time_start, hasFocus);
            set_highlight(R.id.lo_mini_epg_next_pg_time_dash, hasFocus);
            set_highlight(R.id.lo_mini_epg_next_pg_time_end, hasFocus);
            set_highlight(R.id.lo_mini_epg_next_pg_title, hasFocus);
            set_event_quality(R.id.lo_mini_epg_next_pg_quality);
            set_event_grading(R.id.lo_mini_epg_next_pg_grading, g_epgEventF);
            set_event_progress(g_epgEventF);
            set_event_record_icon(R.id.lo_mini_epg_next_pg_record, hasFocus, isMarkRecord);
            set_event_record_hint(hasFocus, isMarkRecord);
            set_detail_red_hint(hasFocus && isMarkRecord); // on_focus_follow
            set_detail_yellow_hint(hasFocus && is_mark_remind());
            set_delay(DELAY_MINI_EPG, hasFocus);
            show_record_hint(hasFocus ? VISIBLE : GONE);
            show_remind_hint(hasFocus ? VISIBLE : GONE);
            gFollowTitle.setSelected(hasFocus);
        });
    }

    public void on_key_program() {

        // current program
        g_currPrg.setOnKeyListener((v, keyCode, event) -> {
            if (KeyEvent.KEYCODE_BACK == keyCode) {
                hide_info();
                return true;
            }
            return false;
        });

        // next program
        g_nextPrg.setOnKeyListener((v, keyCode, event) -> {
            if (KeyEvent.KEYCODE_BACK == keyCode) {
                hide_info();
                return true;
            }
            return false;
        });
    }

    public void on_click_genre(String genre) {
        Log.d(TAG, "on_click_genre: " + genre);
        g_genre = genre;
        show_info();
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public String get_string(int resId) {
        return get().getString(resId);
    }

    public String get_genre() {
        return g_genre;
    }

    public Date get_date(long timeMs) {
        SimpleDateFormat formatter = new SimpleDateFormat("0000-00-00 HH:mm:ss", Locale.getDefault());
        try {
            return formatter.parse(formatter.format(new Date(timeMs)));
        }
        catch (ParseException e) {
            return null;
        }
    }

    public int get_progress(EPGEvent epgEvent) {
        Date dateStart, dateCurrent;
        int progress = 0;

        if (null == epgEvent)
            return progress;

        dateStart = get_date(epgEvent.get_start_time(TIME_ZONE_OFFSET));
        dateCurrent = get_date(System.currentTimeMillis());
        progress = (int) (dateCurrent.getTime() - dateStart.getTime());
        /*Log.e(TAG, "set_event_progress: [start] " + dateStart);
        Log.e(TAG, "set_event_progress: [current] " + dateCurrent);
        Log.e(TAG, "set_event_progress: [duration] " + epgEvent.get_duration());
        Log.e(TAG, "set_event_progress: [progress] " + progress);*/
        return progress;
    }

    private String get_recording_name() {
        g_epgEventP = get().get_live_tv_manager().get_current_present();
        if (g_epgEventP != null)
            return g_epgEventP.get_event_name();

        ProgramInfo channel = get().get_live_tv_manager().get_current_channel();
        if (channel != null)
            return channel.getDisplayName();

        return get().getString(R.string.dialog_lang_none);
    }

    private String get_recording_name(ProgramInfo channel, EPGEvent present) {
        if (present != null)
            return present.get_event_name();

        if (channel != null)
            return channel.getDisplayName();

        return get().getString(R.string.dialog_lang_none);
    }

    public static int get_new_book_id() {
        List<BookInfo> allBookInfoList = new ArrayList<>();
        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
        int bookId = 0;

        if (primeDtv != null)
            allBookInfoList = primeDtv.book_info_get_list();

        if (allBookInfoList != null && !allBookInfoList.isEmpty()) {
            int last = Math.max(allBookInfoList.size() - 1, 0);
            bookId = allBookInfoList.get(last).getBookId() + 1;
        }

        return bookId;
    }

    public static int get_week_day(Calendar calendar) {
        return switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case 1 -> BookInfo.BOOK_WEEK_DAY_SUNDAY;
            case 2 -> BookInfo.BOOK_WEEK_DAY_MONDAY;
            case 3 -> BookInfo.BOOK_WEEK_DAY_TUESDAY;
            case 4 -> BookInfo.BOOK_WEEK_DAY_WEDNESDAY;
            case 5 -> BookInfo.BOOK_WEEK_DAY_THURSDAY;
            case 6 -> BookInfo.BOOK_WEEK_DAY_FRIDAY;
            case 7 -> BookInfo.BOOK_WEEK_DAY_SATURDAY;
            default -> 0x00;
        };
    }

    public ProgramInfo get_current_channel() {
        if (null == gLiveTvMgr)
            gLiveTvMgr = get().get_live_tv_manager();
        gCurChannel = gLiveTvMgr.get_current_channel();
        return gCurChannel;
    }

    public EPGEvent get_present() {
        if (null == gLiveTvMgr)
            gLiveTvMgr = get().get_live_tv_manager();
        g_epgEventP = gLiveTvMgr.get_current_present();
        return g_epgEventP;
    }

    public EPGEvent get_follow() {
        if (gLiveTvMgr != null)
            g_epgEventF = gLiveTvMgr.get_current_follow();
        return g_epgEventF;
    }

    public static int get_grading_res_id(EPGEvent epgEvent, ProgramInfo channel) {
        if (channel != null && channel.getAdultFlag() == 1 && epgEvent != null)
            epgEvent.set_parental_rate(18);
        return get_grading_res_id(epgEvent);
    }

    public static int get_grading_res_id(EPGEvent epgEvent) {
        int parental_rate = epgEvent != null ? epgEvent.get_parental_rate() : -1;
        int parental_rate_image = 0;

        if (Pvcfg.getModuleType() == Pvcfg.MODULE_DMG) {
            parental_rate_image
                    = parental_rate >= 18 ? R.mipmap.rating_level_r
                    : parental_rate >= 15 ? R.mipmap.rating_level_pg15
                    : parental_rate >= 12 ? R.mipmap.rating_level_pg12
                    : parental_rate >= 6 ? R.mipmap.rating_level_p
                    : parental_rate >= 0 ? R.mipmap.rating_level_g : 0;
        }
        else {
            parental_rate_image
                    = parental_rate >= 18 ? R.mipmap.rating_level_r
                    : parental_rate >= 15 ? R.mipmap.rating_level_pg15
                    : parental_rate >= 12 ? R.mipmap.rating_level_pg12
                    : parental_rate >= 7 ? R.mipmap.rating_level_p
                    : parental_rate >= 0 ? R.mipmap.rating_level_g : 0;
        }

        return parental_rate_image;
    }

    private String get_booking_name(BookInfo bookInfo) {
        ProgramInfo channel = gPrimeDtv.get_program_by_channel_id(bookInfo.getChannelId());
        return "[" + channel.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM) + "] " + bookInfo.getEventName();
    }

    public void set_channel_info(String genre) {
        if (null == gLiveTvMgr)
            gLiveTvMgr = get().get_live_tv_manager();
        if (null == gLiveTvMgr)
            Log.e(TAG, "set_channel_info: LiveTvManager is null");

        set_channel_info(genre, gLiveTvMgr.get_current_channel());
    }

    public void set_channel_info(ProgramInfo channel) {
        if (null == gLiveTvMgr)
            gLiveTvMgr = get().get_live_tv_manager();
        if (null == gLiveTvMgr)
            Log.e(TAG, "set_channel_info: LiveTvManager is null");
        set_channel_info(get_genre(), channel);
    }
    
    private void set_channel_info(String genre, ProgramInfo channel) {
        delay();
        set_event_status_size();

        if (null == channel) {
            Log.w(TAG, "set_channel_info: null channel info");
            return;
        }

        gCurChannel = channel;
        update_timezone_offset();
        set_channel_genre(genre);
        set_channel_num(channel);
        set_channel_name(channel);
        set_channel_icon(channel);
        set_channel_present(channel);
        set_channel_follow(channel);
        set_summary_record_status(channel);
        set_unlock_hint_visibility();
    }

    public void set_channel_genre(String genre) {
        TextView genreView = findViewById(R.id.lo_mini_epg_ch_genre);
        genreView.setText(genre);
    }

    public void set_channel_num(ProgramInfo chInfo) {
        TextView chNumView = findViewById(R.id.lo_mini_epg_ch_num);
        chNumView.setText(chInfo.getDisplayNum(MAX_LENGTH_OF_CHANNEL_NUM));
    }

    public void set_channel_name(ProgramInfo chInfo) {
        TextView chNameView;
        String chName;

        chName = chInfo.getDisplayName();
        chNameView = findViewById(R.id.lo_mini_epg_ch_title);
        chNameView.setText(chName);
    }

    public void set_channel_icon(ProgramInfo chInfo) {
        if (chInfo == null || gCurChannel == null || ProgramInfo.PROGRAM_RADIO == chInfo.getType())
            return;

        iconExecutor.execute(() -> {
            ImageView channelIcon = MiniEPG.this.findViewById(R.id.lo_mini_epg_ch_icon);
            String serviceId = chInfo.getServiceId(MAX_LENGTH_OF_SERVICE_ID);
            String iconUrl = LiveTvManager.get_channel_icon_url(get(), serviceId);
            int iconResId = LiveTvManager.get_channel_icon_res_id(get(), serviceId);
            int iconWidth = get().getResources().getDimensionPixelSize(R.dimen.mini_epg_ch_icon_width);
            int iconHeight = get().getResources().getDimensionPixelSize(R.dimen.mini_epg_ch_icon_height);
            if (chInfo.getChannelId() != gCurChannel.getChannelId())
                return;
            Log.d(TAG, "set_channel_icon: " + iconUrl);
            get().get_main_handler().post(() -> {
                if (!get().isFinishing() && !get().isDestroyed()) {
                    Glide.with(get())
                            .load(iconUrl)
                            .placeholder(iconResId)
                            .error(iconResId)
                            .override(iconWidth, iconHeight)
                            //.skipMemoryCache(false)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(channelIcon);
                }
            });
        });
    }

    public void set_channel_present(ProgramInfo channel) {
        g_epgEventP = get().g_dtv.get_present_event(channel.getChannelId());
        presentExecutor.execute(() -> {
            get().get_main_handler().post(() -> {
                gPresentStatus.setText(R.string.hint_current_mini);
                gPresentStart.setText(g_epgEventP != null ? ms_to_time(g_epgEventP.get_start_time(TIME_ZONE_OFFSET)) : "00:00");
                gPresentEnd.setText(g_epgEventP != null ? ms_to_time(g_epgEventP.get_end_time(TIME_ZONE_OFFSET)) : "00:00");
                set_event_title(gPresentTitle, g_epgEventP);
                set_event_quality(gPresentQuality, channel);
                set_event_grading(gPresentGrading, g_epgEventP);
                set_event_progress(g_epgEventP);
            });
        });
    }

    public void set_channel_follow(ProgramInfo channel) {
        g_epgEventF = get().g_dtv.get_follow_event(channel.getChannelId());
        followExecutor.execute(() -> {
            get().get_main_handler().post(() -> {
                gFollowStatus.setText(R.string.hint_next_mini);
                gFollowStart.setText(g_epgEventF != null ? ms_to_time(g_epgEventF.get_start_time(TIME_ZONE_OFFSET)) : "00:00");
                gFollowEnd.setText(g_epgEventF != null ? ms_to_time(g_epgEventF.get_end_time(TIME_ZONE_OFFSET)) : "00:00");
                set_event_title(gFollowTitle, g_epgEventF);
                set_event_quality(gFollowQuality, channel);
                set_event_grading(gFollowGrading, g_epgEventF);
            });
        });
    }

    public void do_channel_icon_preload() {
        ChannelChangeManager changeMgr;
        ProgramInfo currInfo, prevInfo, nextInfo;
        String currIconUrl, prevIconUrl, nextIconUrl;
        ImageView channelIcon;

        channelIcon = findViewById(R.id.lo_mini_epg_ch_icon);
        changeMgr = ChannelChangeManager.get_instance(get());
        prevInfo = get().g_liveTvMgr.get_channel(changeMgr.find_down_ch_id());
        currInfo = get().g_liveTvMgr.get_channel(changeMgr.get_cur_ch_id());
        nextInfo = get().g_liveTvMgr.get_channel(changeMgr.find_up_ch_id());

        if (null == currInfo || null == prevInfo || null == nextInfo) {
            Log.e(TAG, "preload_channel_icon: null channel info");
            return;
        }

        prevIconUrl = LiveTvManager.get_channel_icon_url(get(), prevInfo.getServiceId(MAX_LENGTH_OF_SERVICE_ID));
        currIconUrl = LiveTvManager.get_channel_icon_url(get(), currInfo.getServiceId(MAX_LENGTH_OF_SERVICE_ID));
        nextIconUrl = LiveTvManager.get_channel_icon_url(get(), nextInfo.getServiceId(MAX_LENGTH_OF_SERVICE_ID));

        if (!get().isFinishing() && !get().isDestroyed()) {
            Glide.with(get()).load(prevIconUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(channelIcon);
            Glide.with(get()).load(nextIconUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(channelIcon);
            Glide.with(get()).load(currIconUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(channelIcon);
        }
    }

    @SuppressLint("SetTextI18n")
    public void set_event_empty(TextView status, TextView title, TextView start, TextView end, ImageView quality, ImageView grading) {
        ProgressBar progressBar = findViewById(R.id.lo_mini_epg_curr_pg_progress);
        progressBar.setMax(0);
        progressBar.setProgress(0);

        if (start != null)
            status.setText(R.string.epg_no_data);
        if (title != null)
            title.setText(R.string.epg_no_data);
        if (start != null)
            start.setText("00:00");
        if (end != null)
            end.setText("00:00");
        if (quality != null)
            quality.setImageResource(0);
        if (grading != null)
            grading.setImageResource(0);
    }

    public void set_event_status_size() {
        if (Locale.getDefault().getLanguage().equals("zh") && gPresentStatus.getTextSize() != 32) {
            int size = (int) (get().getResources().getDimensionPixelSize(R.dimen.mini_epg_curr_pg_status_size_zh) / get().getResources().getDisplayMetrics().density);
            gPresentStatus.setTextSize(size);
            gFollowStatus.setTextSize(size);
        }
    }

    public void set_event_title(TextView title, EPGEvent epgEvent) {
        if (is_block_adult())
            title.setText(R.string.lock_program);
        else
            title.setText(epgEvent != null ? epgEvent.get_event_name() + "  " : get().getString(R.string.epg_no_data));
    }

    public void set_event_quality(int qualityId) {
        ProgramInfo channel = get().g_liveTvMgr.get_current_channel();
        ImageView quality = findViewById(qualityId);

        set_event_quality(quality, channel);
    }

    public void set_event_quality(ImageView quality, ProgramInfo channel) {
        boolean hasFocus = false;

        if (quality.getId() == R.id.lo_mini_epg_curr_pg_quality) hasFocus = is_focus_present();
        if (quality.getId() == R.id.lo_mini_epg_next_pg_quality) hasFocus = is_focus_follow();

        if (!hasFocus) {
            quality.setImageResource(0);
            quality.setVisibility(GONE);
            return;
        }

        if (is_hd(channel)) {
            quality.setImageResource(R.drawable.icon_tv_hd);
            quality.setVisibility(VISIBLE);
        }
        else if (is_4k(channel)) {
            quality.setImageResource(R.drawable.icon_4k);
            quality.setVisibility(VISIBLE);
        }
        else {
            quality.setImageResource(0);
            quality.setVisibility(GONE);
        }
    }

    public void set_event_grading(int gradingId, EPGEvent epgEvent) {
        ImageView grading = findViewById(gradingId);
        set_event_grading(grading, epgEvent);
    }

    public void set_event_grading(ImageView grading, EPGEvent epgEvent) {
        get().runOnUiThread(() -> {
            int parental_rate_image = get_grading_res_id(epgEvent, gCurChannel);
            boolean hasFocus = false;

            if (grading.getId() == R.id.lo_mini_epg_curr_pg_grading) hasFocus = is_focus_present();
            if (grading.getId() == R.id.lo_mini_epg_next_pg_grading) hasFocus = is_focus_follow();

            if (!hasFocus) {
                grading.setImageResource(0);
                grading.setVisibility(GONE);
                return;
            }

            grading.setImageResource(parental_rate_image);
            grading.setVisibility(parental_rate_image == 0 ? GONE : VISIBLE);
        });
    }

    public void set_event_record_icon(boolean isMarkRecord) {
        if (is_focus_present())
            set_event_record_icon(R.id.lo_mini_epg_curr_pg_record, true, isMarkRecord);
        else if (is_focus_follow())
            set_event_record_icon(R.id.lo_mini_epg_next_pg_record, true, isMarkRecord);
    }

    public void set_event_record_icon(int viewId, boolean hasFocus, boolean isMarkRecord) {
        View recIcon = findViewById(viewId);

        if (hasFocus && isMarkRecord)
            recIcon.setVisibility(VISIBLE);
        else
            recIcon.setVisibility(GONE);
    }

    public void set_event_record_hint(boolean hasFocus, boolean hasRecord) {
        if (hasFocus) {
            // red hint: "Record" or "Stop record"
            TextView redHint = findViewById(R.id.lo_mini_epg_func_record_text);
            redHint.setText(hasRecord ? R.string.hint_rcu_stop_record : R.string.hint_rcu_record);
        }
    }

    public void set_event_progress(EPGEvent epgEvent) {
        get().runOnUiThread(() -> {
            ProgressBar progressBar = findViewById(R.id.lo_mini_epg_curr_pg_progress);
            int duration = epgEvent != null ? (int) epgEvent.get_duration() : 0;
            int progress = epgEvent != null ? (int) (get().g_dtv.get_current_time() - epgEvent.get_start_time(TIME_ZONE_OFFSET)) : 0;

            /*if (epgEvent != null)
                progress = get_progress(epgEvent);*/

            progressBar.setMax(duration);
            progressBar.setProgress(progress);
        });
    }

    public void set_intro_time() {
        // introduction time
        DELAY_MINI_EPG = get().g_dtv.gpos_info_get().getIntroductionTime();
        //Log.d(TAG, "set_intro_time: DELAY_MINI_EPG = " + DELAY_MINI_EPG);
    }

    private void set_delay(int delay) {
        set_delay(delay, true);
    }

    private void set_delay(int delay, boolean hasFocus) {

        if (!hasFocus)
            return;

        //Log.d(TAG, "set_delay: " + delay);
        g_handler.removeCallbacksAndMessages(null);
        g_handler.postDelayed(this::hide_info, delay);
    }

    public void set_highlight(int resId, boolean hasFocus) {
        TextView view = findViewById(resId);

        if (null == view)
            return;

        int color = hasFocus
                ? ContextCompat.getColor(getContext(), R.color.white)
                : ContextCompat.getColor(getContext(), R.color.hint);

        view.setSelected(hasFocus);
        view.setTextColor(color);
    }

    public void set_summary_record_status(ProgramInfo channel) {
        if (!Pvcfg.getPVR_PJ() || null == get().get_live_tv_manager() || null == get().get_live_tv_manager().g_chChangeMgr)
            return;

        get().runOnUiThread(() -> {
            LiveTvManager liveTvManager = get().get_live_tv_manager();
            ChannelChangeManager changeManager = liveTvManager.g_chChangeMgr;
            boolean isMarkRecord = is_mark_record(channel);
            boolean isMarkRemind = is_mark_remind();

            // p/f record icon
            set_event_record_icon(isMarkRecord);

            // red hint: "Record" or "Stop record"
            TextView redHint = findViewById(R.id.lo_mini_epg_func_record_text);
            redHint.setText(isMarkRecord ? R.string.hint_rcu_stop_record : R.string.hint_rcu_record);

            // detail's hint: "Record" or "Stop record"
            set_detail_red_hint(isMarkRecord); // set_summary_record_status
            set_detail_yellow_hint(isMarkRemind);

            // recording hint: "CH001/CH002 Recording"
            set_summary_record_visibility();
            TextView recordText = findViewById(R.id.lo_mini_epg_record_hint_text);
            StringBuilder recordHint = new StringBuilder();
            List<RecChannelInfo> recInfoList = changeManager.get_rec_channel_info();

            for (RecChannelInfo recInfo : recInfoList) {
                long channelId = recInfo.getChannelId();
                if (changeManager.is_last_recording(channelId))
                    continue;
                String channelNum = liveTvManager.get_channel(channelId).getDisplayNum(MAX_LENGTH_OF_CHANNEL_NUM);
                recordHint.append("CH").append(channelNum);
            }
            for (RecChannelInfo recInfo : recInfoList) {
                long channelId = recInfo.getChannelId();
                if (!changeManager.is_last_recording(channelId))
                    continue;
                String channelNum = liveTvManager.get_channel(channelId).getDisplayNum(MAX_LENGTH_OF_CHANNEL_NUM);
                if (recordHint.length() > 0)
                    recordHint.append("/");
                recordHint.append("CH").append(channelNum);
            }
            recordText.setText(recordHint);
            delay();
        });
    }

    public void set_summary_record_visibility() {
        if (!Pvcfg.getPVR_PJ())
            return;

        boolean has_now_recording = has_now_recording(get());
        View recordIcon = findViewById(R.id.lo_mini_epg_record_hint_icon);
        View recordText = findViewById(R.id.lo_mini_epg_record_hint_text);
        View recordPostfix = findViewById(R.id.lo_mini_epg_record_hint_postfix);
        //View presentRecIcon = findViewById(R.id.lo_mini_epg_curr_pg_record);

        recordIcon.setVisibility(has_now_recording ? VISIBLE : GONE);
        recordText.setVisibility(has_now_recording ? VISIBLE : GONE);
        recordPostfix.setVisibility(has_now_recording ? VISIBLE : GONE);
        //presentRecIcon.setVisibility(is_current_channel_recording ? VISIBLE : GONE);
    }

    public void set_unlock_hint_visibility() {
        get().runOnUiThread(() -> {
            ImageView unlockIcon = findViewById(R.id.lo_hint_unlock_icon);
            TextView unlockText = findViewById(R.id.lo_hint_unlock_text);

            if (is_block()) {
                unlockIcon.setVisibility(VISIBLE);
                unlockText.setVisibility(VISIBLE);
            }
            else {
                unlockIcon.setVisibility(GONE);
                unlockText.setVisibility(GONE);
            }
        });
    }

    public void set_detail_red_hint(boolean isMarkRecord) {
        set_detail_red_hint(isMarkRecord ? R.string.hint_rcu_cancel_record : R.string.hint_rcu_record); // self
    }

    public void set_detail_red_hint(int resId) {
        get().runOnUiThread(() -> {
            if (gDetailInfo == null)
                build_detail_view();
            gDetailInfo.set_red_hint(resId);
        });
    }

    public void set_detail_yellow_hint(boolean isMarkRemind) {
        set_detail_yellow_hint(isMarkRemind ? R.string.hint_rcu_cancel_remind : R.string.hint_rcu_remind); // self
    }

    public void set_detail_yellow_hint(int resId) {
        get().runOnUiThread(() -> {
            if (gDetailInfo == null)
                build_detail_view();
            gDetailInfo.set_yellow_hint(resId);
        });
    }

    public boolean is_show() {
        return getVisibility() == VISIBLE;
    }

    public boolean is_record_booked() {
        if (null == gPrimeDtv)
            gPrimeDtv = get().g_dtv;

        if (null == g_epgEventF) {
            Log.e(TAG, "is_record_booked: null epgEvent");
            return false;
        }
        for (BookInfo bookInfo : gPrimeDtv.book_info_get_list()) {
            if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD &&
                bookInfo.getEpgEventId() == g_epgEventF.get_event_id())
                return true;
        }
        for (BookInfo bookInfo : get().get_fake_book_list()) {
            if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD &&
                bookInfo.getEpgEventId() == g_epgEventF.get_event_id())
                return true;
        }
        return false;
    }

    public boolean is_remind_booked() {
        if (null == gPrimeDtv)
            gPrimeDtv = get().g_dtv;

        if (null == g_epgEventF) {
            Log.e(TAG, "is_remind_booked: null epgEvent");
            return false;
        }
        for (BookInfo bookInfo : gPrimeDtv.book_info_get_list()) {
            if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_CHANGE_CHANNEL &&
                bookInfo.getEpgEventId() == g_epgEventF.get_event_id())
                return true;
        }
        return false;
    }

    public boolean is_hd(ProgramInfo programInfo) {
        if (null == programInfo)
            return false;
        return ((programInfo.getCategory_type()& 0x0100)>>8) == 1;
    }

    public static boolean is_4k(ProgramInfo programInfo) {
        if (null == programInfo)
            return false;
        return ((programInfo.getCategory_type()& 0x0800)>>11) == 1;
    }

    public boolean is_block() {
        return get().g_liveTvMgr.is_block();
    }

    public boolean is_block_adult() {
        return get().g_liveTvMgr.is_block_adult();
    }

    public boolean is_block_parental() {
        return get().g_liveTvMgr.is_block_parental();
    }

    public boolean is_block_channel() {
        return get().g_liveTvMgr.is_block_channel();
    }

    public boolean is_block_time() {
        return get().g_liveTvMgr.is_block_time();
    }

    public boolean is_focus_present() {
        return g_currPrg.hasFocus();
    }

    public boolean is_focus_follow() {
        return g_nextPrg.hasFocus();
    }

    public boolean is_music_channel() {
        LiveTvManager liveTvManager = get().get_live_tv_manager();
        return liveTvManager.is_music_channel();
    }

    public boolean is_mark_record() {
        boolean isMarkRecord = false;

        if (null == gChangeMgr)
            gChangeMgr = get().get_channel_change_manager();

        if (is_focus_present()) {
            //boolean nowRecording = gChangeMgr.is_channel_recording(gChangeMgr.get_cur_ch_id());
            boolean singleRecord = gChangeMgr.is_single_recording(gChangeMgr.get_cur_ch_id(), g_epgEventP);
            boolean seriesRecord = gChangeMgr.is_series_recording(gChangeMgr.get_cur_ch_id(), g_epgEventP);
            isMarkRecord = singleRecord || seriesRecord;
        }
        if (is_focus_follow()) {
            isMarkRecord = is_record_booked();
        }

        return isMarkRecord;
    }

    public boolean is_mark_record(ProgramInfo channel) {
        boolean isMarkRecord = false;

        if (null == gChangeMgr)
            gChangeMgr = get().get_channel_change_manager();

        if (is_focus_present()) {
            boolean singleRecord = gChangeMgr.is_single_recording(channel.getChannelId(), g_epgEventP);
            boolean seriesRecord = gChangeMgr.is_series_recording(channel.getChannelId(), g_epgEventP);
            isMarkRecord = singleRecord || seriesRecord;
        }
        if (is_focus_follow()) {
            isMarkRecord = is_record_booked();
        }

        return isMarkRecord;
    }

    public boolean is_mark_remind() {
        boolean isMarkRemind = false;

        if (null == gChangeMgr)
            gChangeMgr = get().get_channel_change_manager();

        if (is_focus_follow())
            isMarkRemind = is_remind_booked();

        return isMarkRemind;
    }

    public boolean is_now_recording() {
        return is_now_recording(get(), get().g_liveTvMgr.get_current_channel());
    }

    public static boolean is_now_recording(HomeActivity homeActivity, ProgramInfo channel) {
        if (!Pvcfg.getPVR_PJ() || null == homeActivity || null == homeActivity.g_dtv || null == homeActivity.get_live_tv_manager() || null == channel)
            return false;
        LiveTvManager liveTvManager = homeActivity.get_live_tv_manager();
        ChannelChangeManager changeManager = liveTvManager.g_chChangeMgr;
        return changeManager.is_channel_recording(channel.getChannelId());
    }

    public static boolean has_now_recording(HomeActivity homeActivity) {
        if (!Pvcfg.getPVR_PJ() || null == homeActivity || null == homeActivity.get_live_tv_manager() || null == homeActivity.get_live_tv_manager().g_chChangeMgr)
            return false;
        return homeActivity.get_live_tv_manager().g_chChangeMgr.has_recording();
    }

    public boolean has_curr_epg() {
        TextView currPrg     = g_currPrg.findViewById(R.id.lo_mini_epg_curr_pg_status);
        String   currEPG     = (String) currPrg.getText();
        String   EPG_NO_DATA = get_string(R.string.epg_no_data);
        return !EPG_NO_DATA.equals(currEPG) &&
                g_currPrg.hasFocus() &&
                get_present() != null;
    }

    public boolean has_next_epg() {
        TextView nextPrg     = g_nextPrg.findViewById(R.id.lo_mini_epg_next_pg_status);
        String   nextEPG     = (String) nextPrg.getText();
        String   EPG_NO_DATA = get_string(R.string.epg_no_data);
        return !EPG_NO_DATA.equals(nextEPG) &&
                g_nextPrg.hasFocus() &&
                get_follow() != null;
    }

    public void show_function() {
        Log.d(TAG, "show_function: " + HotkeyFunction.class.getSimpleName());
        hide_info();
        if (null == gHotkeyFunction)
            gHotkeyFunction = new HotkeyFunction(get());
        int INTRODUCTION_TIME = get().g_dtv.gpos_info_get().getIntroductionTime();
        gHotkeyFunction.set_intro_time(INTRODUCTION_TIME);
        gChangeMgr = get().get_channel_change_manager();
        gHotkeyFunction.set_channel_id(gChangeMgr.get_cur_ch_id());
        gHotkeyFunction.show();
    }

    /** @noinspection ConstantValue*/
    public void show_genre() {
        if (true)
            return;
        Log.d(TAG, "show_genre: " + HotkeyGenre.class.getSimpleName());
        hide_info();
        HotkeyGenre genre = new HotkeyGenre(this);
        genre.show();
    }

    public void show_record_icon() {
        get().runOnUiThread(() -> {
            if (!is_show()) {
                Log.e(TAG, "show_record_icon: miniEPG is not show");
                return;
            }
            ImageView recordIcon = findViewById(R.id.lo_mini_epg_next_pg_record);
            TextView recordText = findViewById(R.id.lo_mini_epg_func_record_text);
            recordIcon.setVisibility(VISIBLE);
            recordText.setText(R.string.hint_rcu_cancel_record);
        });
    }

    public void show_record_hint(int visibility) {
        if (!Pvcfg.getPVR_PJ())
            return;

        TextView txtRecord = findViewById(R.id.lo_mini_epg_func_record_text);
        ImageView imgRecord = findViewById(R.id.lo_mini_epg_func_record_img);

        if (null == txtRecord || null == imgRecord) {
            Log.e(TAG, "show_record_hint: imgRemind = " + imgRecord);
            Log.e(TAG, "show_record_hint: txtRemind = " + txtRecord);
            return;
        }

        txtRecord.setVisibility(visibility);
        imgRecord.setVisibility(visibility);

        if (g_currPrg != null && g_currPrg.hasFocus() && is_now_recording())
            txtRecord.setText(R.string.hint_rcu_stop_record);
        if (g_nextPrg != null && g_nextPrg.hasFocus() && is_record_booked())
            txtRecord.setText(R.string.hint_rcu_cancel_record);
    }

    public void show_remind_hint(int visibility) {
        if (!Pvcfg.getPVR_PJ())
            return;

        View txtRemind = findViewById(R.id.lo_mini_epg_func_remind_txt);
        View imgRemind = findViewById(R.id.lo_mini_epg_func_remind_img);

        if (null == txtRemind || null == imgRemind) {
            Log.e(TAG, "show_remind_hint: imgRemind = " + imgRemind);
            Log.e(TAG, "show_remind_hint: txtRemind = " + txtRemind);
            return;
        }

        txtRemind.setVisibility(visibility);
        imgRemind.setVisibility(visibility);

        if (is_remind_booked() && VISIBLE == visibility)
            show_remind_icon();
        else
            hide_remind_icon();
    }

    public void show_remind_icon() {
        get().runOnUiThread(() -> {
            if (!is_show()) {
                Log.e(TAG, "show_remind_icon: miniEPG is not show");
                return;
            }
            ImageView remindIcon = findViewById(R.id.lo_mini_epg_next_pg_reminder);
            TextView remindText = findViewById(R.id.lo_mini_epg_func_remind_txt);
            remindIcon.setVisibility(VISIBLE);
            remindText.setText(R.string.hint_rcu_cancel_remind);
        });
    }

    public void show_info() {
        if (is_music_channel()) {
            Log.d(TAG, "show_info: music channel, do not show miniEPG");
            return;
        }
        if (get().get_live_tv_manager() == null || get().get_live_tv_manager().g_chChangeMgr == null) {
            Log.e(TAG, "show_info: something is null");
            return;
        }
        if (get().get_channel_change_manager().isTimeshiftStart()) {
            Log.d(TAG, "show_info: timeshift start, do not show miniEPG");
            get().get_live_tv_manager().g_timeshiftBanner.show();
            return;
        }

        // show detail
        if (show_info_detail())
            return;

        // show message
        if (show_info_no_epg())
            return;

        // show info
        show_info_only();
    }

    public void show_info_only() {
        ProgramInfo currChannel = get_current_channel();

        if (currChannel == null) {
            Log.e(TAG, "show_info_only: current channel is null");
            return;
        }
        if (ProgramInfo.PROGRAM_RADIO == currChannel.getType()) {
            if (gCurChannel != null)
                Log.w(TAG, "show_info_only: do not show " + MiniEPG.class.getSimpleName() + " at PROGRAM_RADIO, " + gCurChannel.getDisplayNameFull());
            return;
        }

        Log.d(TAG, "show_info_only: [genre] " + g_genre + ", [channel] " + currChannel.getDisplayNameFull());
        set_intro_time();
        g_chTitle.setSelected(true);
        g_currPrg.requestFocus();
        setVisibility(VISIBLE);
        set_channel_info(g_genre);
        set_delay(DELAY_MINI_EPG);
    }

    public void show_info_only(ProgramInfo channel) {
        if (null == channel) {
            show_info_only();
            return;
        }
        if (ProgramInfo.PROGRAM_RADIO == channel.getType()) {
            gCurChannel = channel; // for music view, can not request focus
            Log.w(TAG, "show_info_only: do not show " + MiniEPG.class.getSimpleName() + " at PROGRAM_RADIO, " + gCurChannel.getDisplayNameFull());
            return;
        }
        Log.d(TAG, "show_info_only: [channel] " + channel.getDisplayNum() + " " + channel.getDisplayName());
        g_chTitle.setSelected(true);
        g_currPrg.requestFocus();
        setVisibility(VISIBLE);
        set_channel_info(g_genre, channel);
        set_delay(DELAY_MINI_EPG);
    }

    public boolean show_info_detail() {
        int detail = has_curr_epg() ? Detail.PRESENT :
                     has_next_epg() ? Detail.FOLLOW  : Detail.NONE;
        if (gDetailInfo == null)
            build_detail_view();

        EPGEvent epgEvent = null;

        if (!is_show())
            return false;

        if (is_block_adult()) {
            Log.w(TAG, "show_info_detail: adult lock, not show detail");
            return true;
        }

        if (Detail.NONE == detail)
            return false;

        if (Detail.PRESENT == detail)
            epgEvent = get().g_liveTvMgr.get_current_present();

        if (Detail.FOLLOW == detail)
            epgEvent = get().g_liveTvMgr.get_current_follow();

        set_detail_red_hint(is_mark_record());
        set_detail_yellow_hint(is_mark_remind());
        gDetailInfo.show(detail, epgEvent);
        return true;
    }

    public boolean show_info_no_epg() {
        if (!is_show())
            return false;
        Log.d(TAG, "show_info_message: has no EPG");
        Snakebar.show(this, R.string.epg_no_data, Toast.LENGTH_SHORT);
        //Toast.makeText(get(), get_string(R.string.epg_no_data), Toast.LENGTH_SHORT).show();
        g_chTitle.setSelected(false);
        hide_info();
        return true;
    }

    public void hide_info() {
        setVisibility(GONE);
    }

    public void hide_record_icon() {
        get().runOnUiThread(() -> {
            ImageView recordIcon = findViewById(R.id.lo_mini_epg_next_pg_record);
            TextView  recordText = findViewById(R.id.lo_mini_epg_func_record_text);
            recordIcon.setVisibility(GONE);
            recordText.setText(R.string.hint_rcu_record);
        });
    }

    public void hide_remind_icon() {
        get().runOnUiThread(() -> {
            ImageView remindIcon = findViewById(R.id.lo_mini_epg_next_pg_reminder);
            TextView  remindText = findViewById(R.id.lo_mini_epg_func_remind_txt);
            remindIcon.setVisibility(GONE);
            remindText.setText(R.string.hint_rcu_remind);
        });
    }

    public void setup_remind() {

        if (!Pvcfg.getPVR_PJ() || !is_show() || !g_nextPrg.hasFocus()) {
            Log.e(TAG, "setup_remind: something wrong");
            return;
        }

        if (has_next_epg()) {
            if (is_remind_booked()) {
                Log.d(TAG, "setup_remind: cancel remind ?");
                gHotkeyRemind.stop_remind();
            }
            else {
                Log.d(TAG, "setup_remind: remind program");
                gHotkeyRemind.start_remind();
            }
        }
        else {
            Log.d(TAG, "setup_remind: no epg");
            gHotkeyRemind.set_yes_action(null);
            gHotkeyRemind.set_no_action(null);
            gHotkeyRemind.set_dismiss_action(null);
            gHotkeyRemind.show_message(R.string.remind_hint_no_epg);
        }

        // delay mini epg
        set_delay(DELAY_MINI_EPG);
    }

    public void setup_record() {
        if (!Pvcfg.getPVR_PJ() || !pass_null_check())
            return;

        long channelId = gChangeMgr.get_cur_ch_id();

        if (is_show()) {
            if (is_focus_present()) setup_record_present(channelId, g_epgEventP);
            if (is_focus_follow())  setup_record_follow(channelId, g_epgEventF);
        }
        else
            setup_record_present(channelId, g_epgEventP);
    }

    private void setup_record_present(long channelId, EPGEvent present) {
        Context context = getContext().getApplicationContext();
        boolean isRecording = gChangeMgr.is_channel_recording(channelId);

        if (!isRecording) {
            ProgramInfo curChannel = gChangeMgr.get_cur_channel();
            if (curChannel == null || present == null) {
                Log.e(TAG, "setup_record_present: [channel] " + curChannel + ", [present] " + present);
                Snakebar.show(this, R.string.pvr_operation_prohibited_while_scheduling, Snakebar.LENGTH_SHORT);
                return;
            }
            String recordName = get_recording_name(curChannel, present);
            boolean singleRecord = !present.is_series();
            int duration = (int) ((present.get_end_time(TIME_ZONE_OFFSET) - System.currentTimeMillis()) / 1000);

            if (!support_record_music(curChannel))
                return;

            if (singleRecord) {
                Log.d(TAG, "setup_record_present: SINGLE RECORD, [channel] " + curChannel.getDisplayNameFull() + ", [record name] " + recordName);
                gHotkeyRecord.record_start(this, channelId, present, duration, false); // present normal record
            }
            else {
                Log.d(TAG, "setup_record_present: SERIES RECORD, [channel] " + curChannel.getDisplayNameFull() + ", [record name] " + recordName);
                EventDialog dialog = new EventDialog(get());
                dialog.set_title_text(R.string.pvr_event_dialog_series_video_title);
                dialog.set_confirm_text(R.string.pvr_event_dialog_episodes);
                dialog.set_cancel_text(R.string.pvr_event_dialog_single_episode);
                dialog.set_confirm_action(() -> gHotkeyRecord.record_start(this, channelId, present, duration, true)); // present series record
                dialog.set_cancel_action(() -> gHotkeyRecord.record_start(this, channelId, present, duration, false)); // present single record
                dialog.show();
            }
        }
        else {
            Log.d(TAG, "setup_record_present: STOP RECORD");
            EventDialog dialog = new EventDialog(get());
            dialog.set_title_text(String.format(get().getString(R.string.pvr_event_dialog_record_cancel_title), get_recording_name()));
            dialog.set_confirm_text(get().getString(R.string.pvr_event_dialog_record_cancel_ok));
            dialog.set_cancel_text(get().getString(R.string.pvr_event_dialog_record_cancel_no));
            dialog.set_start_action(() -> g_handler.removeCallbacksAndMessages(null));
            dialog.set_confirm_action(() -> {
                HotkeyRecord.record_stop(context, channelId);
                delay();
            });
            dialog.show();
        }
    }

    private void setup_record_follow(long channelId, EPGEvent follow) {
        ProgramInfo curChannel = gPrimeDtv.get_program_by_channel_id(channelId);
        if (curChannel == null || follow == null) {
            Log.e(TAG, "setup_record_follow: [channel] " + curChannel + ", [follow] " + follow);
            Snakebar.show(this, R.string.pvr_operation_prohibited_while_scheduling, Snakebar.LENGTH_SHORT);
            return;
        }
        BookInfo bookInfo = build_book_info(curChannel, follow);
        String recordName = get_recording_name(curChannel, follow);
        boolean book_one_record = !follow.is_series();
        delay();

        if (is_record_booked()) {
            Log.d(TAG, "setup_record_follow: Cancel book record ?");
            EventDialog dialog = new EventDialog(get());
            dialog.set_title_text(String.format(get().getString(R.string.pvr_event_dialog_record_cancel_title), recordName));
            dialog.set_confirm_text(get().getString(R.string.pvr_event_dialog_record_cancel_ok));
            dialog.set_cancel_text(get().getString(R.string.pvr_event_dialog_record_cancel_no));
            dialog.set_confirm_action(() -> gHotkeyRecord.cancel_book_record(this, curChannel, follow, recordName));
            dialog.show();
            return;
        }

        if (book_one_record) {
            Log.d(TAG, "setup_record_follow: Book one record");
            gHotkeyRecord.book_one_record(this, curChannel, follow, bookInfo, recordName);
        }
        else {
            EventDialog dialog = new EventDialog(get());
            dialog.set_title_text(R.string.pvr_event_dialog_series_video_title);
            dialog.set_confirm_text(R.string.pvr_event_dialog_episodes);
            dialog.set_cancel_text(R.string.pvr_event_dialog_single_episode);
            dialog.set_confirm_action(() -> {
                Log.d(TAG, "setup_record_follow: Book series record");
                gHotkeyRecord.book_series_record(this, curChannel, follow, bookInfo, recordName);
            }); // book follow series record
            dialog.set_cancel_action(() -> {
                Log.d(TAG, "setup_record_follow: Book one record");
                gHotkeyRecord.book_one_record(this, curChannel, follow, bookInfo, recordName);
            }); // book follow one record
            dialog.show();
        }
    }

    private BookInfo build_book_info(ProgramInfo channel, EPGEvent event) {
        // start time
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(new Date(event.get_start_time()));
        if (BaseActivity.FOR_TEST_BOOK_CONFLICT)
            calStart.setTime(new Date(System.currentTimeMillis() + 2 * 60000));
        calStart.add(Calendar.MINUTE, -1);

        int new_book_id = MiniEPG.get_new_book_id();
        int day_of_week = MiniEPG.get_week_day(calStart);
        int year = calStart.get(Calendar.YEAR);
        int month = calStart.get(Calendar.MONTH) + 1;
        int date = calStart.get(Calendar.DATE);
        int hour_of_day = calStart.get(Calendar.HOUR_OF_DAY);
        int minute = calStart.get(Calendar.MINUTE);
        int startTime = hour_of_day * 100 + minute;
        int durationMs = (int) (event.get_end_time() - event.get_start_time());
        int durationHour = durationMs / (1000 * 60 * 60);
        int durationMinute = durationMs % (1000 * 60 * 60) / (1000 * 60);
        int duration = durationHour * 100 + durationMinute;
        //Log.e(TAG, "build_book_info: [name] " + event.get_event_name() + ", [duration] " + durationMs + ", [hour] " + durationHour + ", [minute] " + durationMinute + ", [duration] " + duration);

        BookInfo bookInfo = new BookInfo();
        bookInfo.setBookId(new_book_id);
        bookInfo.setChannelId(channel.getChannelId());
        bookInfo.setChannelNum(channel.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM));
        bookInfo.setGroupType(FavGroup.ALL_TV_TYPE);
        bookInfo.setEventName(event.get_event_name());
        bookInfo.setBookType(BookInfo.BOOK_TYPE_RECORD);
        bookInfo.setBookCycle(BookInfo.BOOK_CYCLE_ONETIME);
        bookInfo.setYear(year);
        bookInfo.setMonth(month);
        bookInfo.setDate(date);
        bookInfo.setWeek(day_of_week);
        bookInfo.setSeriesRecKey(event.get_series_key());
        bookInfo.setEpisode(event.get_episode_key());
        bookInfo.setSeries(event.is_series());
        bookInfo.setStartTime(startTime);
        bookInfo.setStartTimeMs(event.get_start_time());
        if (BaseActivity.FOR_TEST_BOOK_CONFLICT)
            bookInfo.setStartTimeMs(calStart.getTimeInMillis());
        bookInfo.setDuration(duration);
        bookInfo.setDurationMs(durationMs);
        bookInfo.setEnable(0);
        bookInfo.setEpgEventId(event.get_event_id());
        bookInfo.setAutoSelect(true);
        bookInfo.set4K(is_4k(channel));
        return bookInfo;
    }

    private void build_detail_view() {
        gDetailInfo = new HotkeyInfo(this);
        gDetailInfo.set_start_action(() -> g_handler.removeCallbacksAndMessages(null));
        gDetailInfo.set_dismiss_action(this::delay);
    }

    public void handle_book_series() {
        if (gHotkeyRecord != null)
            gHotkeyRecord.handle_book_series();
    }

    private boolean pass_null_check() {
        if (gPrimeDtv == null)  gPrimeDtv = HomeApplication.get_prime_dtv();
        if (gLiveTvMgr == null) gLiveTvMgr = get().get_live_tv_manager();
        if (gChangeMgr == null) gChangeMgr = get().get_channel_change_manager();
        g_epgEventP = gLiveTvMgr.get_current_present();
        g_epgEventF = gLiveTvMgr.get_current_follow();

        if (gLiveTvMgr == null) {
            Log.e(TAG, "pass_null_check: liveTvManager is null");
            return false;
        }
        if (gChangeMgr == null) {
            Log.e(TAG, "pass_null_check: channelChangeManager is null");
            return false;
        }
        if (gPrimeDtv == null) {
            Log.e(TAG, "pass_null_check: primeDtv is null");
            return false;
        }

        ProgramInfo currChannel = gChangeMgr.get_cur_channel();
        if (null == currChannel) {
            Log.e(TAG, "pass_null_check: current channel is null");
            return false;
        }
        return true;
    }

    private boolean support_record_music(ProgramInfo programInfo) {
        if (gLiveTvMgr.is_music_channel(programInfo)) {
            if (Pvcfg.supportRecordMusic())
                return true;
            Log.w(TAG, "support_record_music: music recording is not supported");
            return false;
        }
        Log.d(TAG, "support_record_music: not music channel");
        return true;
    }

    @SuppressLint("SimpleDateFormat")
    public static String ms_to_time(long milliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(milliseconds));
    }

    @SuppressLint("SimpleDateFormat")
    public static String ms_to_time(long milliseconds, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date(milliseconds));
    }

    public static String ms_to_duration(long milliseconds) {
        return sec_to_duration(milliseconds / 1000);
    }

    @SuppressLint("SimpleDateFormat")
    public static String sec_to_duration(long seconds) {
        Duration duration = Duration.ofSeconds(seconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long second = duration.getSeconds() % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, second);
    }

    public static void update_timezone_offset() {
        TIME_ZONE_OFFSET = 0;//TimeZone.getDefault().getRawOffset();
    }

    public void delay() {
        set_delay(DELAY_MINI_EPG);
    }
}
