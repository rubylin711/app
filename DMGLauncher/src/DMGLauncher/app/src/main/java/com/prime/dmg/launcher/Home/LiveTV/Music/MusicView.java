package com.prime.dmg.launcher.Home.LiveTV.Music;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.prime.dmg.launcher.CustomView.Snakebar;
import com.prime.dmg.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.dmg.launcher.EPG.MyLinearLayoutManager;
import com.prime.dmg.launcher.Utils.Utils;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyInfo;
import com.prime.dmg.launcher.Home.LiveTV.LiveTvManager;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.Home.LiveTV.Music.Vumeterlibrary.VuMeterView;
import com.prime.dmg.launcher.Home.QRCode.QRCodeDialog;
import com.prime.dmg.launcher.Home.Recommend.Stream.StreamActivity;
import com.prime.dmg.launcher.HomeActivity;

import com.prime.dmg.launcher.R;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Scan.Scan_utils;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.datamanager.DefaultValue;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.FavInfo;
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.MusicAdInfo;
import com.prime.dtv.sysdata.MusicAdScheduleInfo;
import com.prime.dtv.sysdata.MusicInfo;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.utils.LogUtils;
import com.prime.dtv.utils.RecChannelInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/** @noinspection CommentedOutCode*/
public class MusicView extends RelativeLayout {
    private static final String TAG = "MusicView";
    private static final long CHECK_INTERVAL_MS = 5000;

    public static final String TYPE_APP = "1009";
    public static final String TYPE_APP_CP = "1010";
    public static final String TYPE_APP_CP_1 = "1002";
    public static final String TYPE_APP_CP_2 = "1003";
    public static final String TYPE_APP_CP_MULTI = "1011";
    public static final String TYPE_BROWSER = "1005";
    public static final String TYPE_FORCE_TUNE = "2000";
    public static final String TYPE_OPEN_MAIL = "2001";
    public static final String TYPE_POSTER = "1006";
    public static final String TYPE_PROGRAM = "1008";
    public static final String TYPE_QRCODE = "1007";
    public static final String TYPE_STREAM = "1004";

    private WeakReference<AppCompatActivity> g_ref;
    private LiveTvManager g_live_tv_manager = null;
    private ChannelChangeManager g_chChangeMgr = null;
    private boolean g_is_lock = false;

    private RelativeLayout g_rltvl_category_layer = null;
    private MiddleFocusRecyclerView g_rcv_category_list = null;
    private MusicCategoryAdapter g_adpt_category = null;
    private List<MusicInfo> g_category_info_list = null;
    private View g_view_last_category;
    private int g_category_index;

    private RelativeLayout g_rltvl_music_layer = null;
    private MiddleFocusRecyclerView g_rcv_music_list = null;
    private MusicListAdapter g_adpt_list = null;
    private View g_view_last_list_view;

    private ImageView g_imgv_music_ad;
    private List<MusicAdInfo> g_music_ad_info_list;
    private int g_current_ad_index = 0;

    private TextView g_textv_mini_epg_channel_num, g_textv_mini_epg_channel_name;
    private TextView g_textv_mini_epg_present_status, g_textv_mini_epg_present_time, g_textv_mini_epg_present_program_name;
    private ImageView g_imgv_mini_epg_present_grading, g_imgv_mini_epg_present_record, g_imgv_mini_epg_present_reminder;

    private TextView g_textv_mini_epg_follow_status, g_textv_mini_epg_follow_time, g_textv_mini_epg_follow_program_name;
    private ImageView g_imgv_mini_epg_follow_grading, g_imgv_mini_epg_follow_record, g_imgv_mini_epg_follow_reminder;
    private VuMeterView g_vul_animation;
    private ProgressBar g_progress_bar;

    //hotkey
    private ImageView g_imgv_hot_key_red , g_imgv_hot_key_green, g_imgv_hot_key_yellow, g_imgv_hot_key_blue, g_imgv_hot_key_ok;
    private TextView g_textv_hot_key_red, g_textv_hot_key_green, g_textv_hot_key_yellow, g_textv_hot_key_blue, g_textv_hot_key_ok;
    private ConstraintLayout g_layout_hotkey;

    private RelativeLayout g_rltvl_error_code_layer;
    private TextView g_textv_error;
    private ImageView g_imgv_error_qr_code;

    private HotkeyInfo gDetailInfo;

    private final Runnable g_ad_runnable = new Runnable() {
        @Override
        public void run() {
            if (g_music_ad_info_list == null || g_music_ad_info_list.isEmpty()) {
                set_hint_yellow(View.GONE);
                return;
            }

            g_current_ad_index++;
            if (g_current_ad_index >= g_music_ad_info_list.size())
                g_current_ad_index = 0;

            update_open_ad_hint();
            if (getContext() instanceof HomeActivity homeActivity) {
                if (homeActivity.isFinishing() || homeActivity.isDestroyed()) {
                    Log.e(TAG, "g_ad_runnable: homeActivity is finishing or destroyed");
                    return;
                }
                if (!homeActivity.isFinishing() && !homeActivity.isDestroyed())
                    Glide.with(homeActivity)
                            .load((g_music_ad_info_list.get(g_current_ad_index)).get_media_url())
                            .error(R.drawable.internet_error)
                            .priority(Priority.IMMEDIATE)
                            .apply(new RequestOptions()
                                    .override(g_imgv_music_ad.getWidth(), g_imgv_music_ad.getHeight())
                                    .fitCenter())
                            .into(g_imgv_music_ad);
            }
            postDelayed(this, 15000);
        }
    };

    private final Runnable gUpdatePresentFollow = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            long endTime = g_epg_present != null ? g_epg_present.get_end_time(MiniEPG.TIME_ZONE_OFFSET) : Long.MAX_VALUE;

            // set follow to present if present ends
            // and wait TYPE_EPG_PF_VERSION_CHANGED to update latest present and follow
            if (g_epg_present != null && currentTime > endTime) {
                g_epg_present = g_epg_follow;
                g_epg_follow = null;
            }

            update_present_program(g_epg_present);
            update_follow_program(g_epg_follow);
            postDelayed(this, CHECK_INTERVAL_MS);
        }
    };

    private RelativeLayout g_rltvl_mini_epg_layer = null;
    private EPGEvent g_epg_present;
    private EPGEvent g_epg_follow;

    public MusicView(Context context) {
        super(context);
    }

    public MusicView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MusicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(Context context) {
        inflate(getContext(), R.layout.view_music, this);
        g_ref     = new WeakReference<>((AppCompatActivity) context);
        g_rcv_category_list = findViewById(R.id.lo_music_rcv_category_item_list);
        g_rcv_music_list = findViewById(R.id.lo_music_rcv_music_list);
        g_rltvl_error_code_layer = findViewById(R.id.lo_music_rltv_error_code_layer);
        g_textv_error = findViewById(R.id.lo_music_textv_error);
        g_imgv_error_qr_code = findViewById(R.id.lo_music_imgv_error_qr_code);

        g_rltvl_category_layer = findViewById(R.id.lo_music_rltv_category_item_layer);
        g_rltvl_music_layer = findViewById(R.id.lo_music_rltv_music_item_layer);
        g_rltvl_mini_epg_layer = findViewById(R.id.lo_music_rltvl_mini_epg_layer);

        g_imgv_music_ad = findViewById(R.id.lo_music_imgv_music_ad);

        init_mini_epg_ui();
        init_hot_key_ui();
        init_error_message();
        g_live_tv_manager =  get_context().g_liveTvMgr;
        g_chChangeMgr = ChannelChangeManager.get_instance(get());

        init_music_category();
        init_music_list();
        update_music_ad();
    }

    private HomeActivity get_context() {
        return (HomeActivity) g_ref.get();
    }

    private void init_mini_epg_ui() {
        g_textv_mini_epg_channel_num = findViewById(R.id.lo_music_textv_mini_epg_channel_num);
        g_textv_mini_epg_channel_name = findViewById(R.id.lo_music_textv_mini_epg_channel_name);

        g_textv_mini_epg_present_status = findViewById(R.id.lo_music_textv_mini_epg_present_status);
        g_textv_mini_epg_present_time = findViewById(R.id.lo_music_textv_mini_epg_present_time);
        g_textv_mini_epg_present_program_name = findViewById(R.id.lo_music_textv_mini_epg_present_program_name);

        g_imgv_mini_epg_present_grading = findViewById(R.id.lo_music_imgv_mini_epg_present_grading);
        g_imgv_mini_epg_present_record = findViewById(R.id.lo_music_imgv_mini_epg_present_record);
        g_imgv_mini_epg_present_reminder = findViewById(R.id.lo_music_imgv_mini_epg_present_reminder);

        g_textv_mini_epg_follow_status = findViewById(R.id.lo_music_textv_mini_epg_follow_status);
        g_textv_mini_epg_follow_time = findViewById(R.id.lo_music_textv_mini_epg_follow_time);
        g_textv_mini_epg_follow_program_name = findViewById(R.id.lo_music_textv_mini_epg_follow_program_name);

        g_imgv_mini_epg_follow_grading = findViewById(R.id.lo_music_imgv_mini_epg_follow_grading);
        g_imgv_mini_epg_follow_record = findViewById(R.id.lo_music_imgv_mini_epg_follow_record);
        g_imgv_mini_epg_follow_reminder = findViewById(R.id.lo_music_imgv_mini_epg_follow_reminder);

        g_vul_animation = findViewById(R.id.lo_music_mini_epg_animation);
        g_progress_bar = findViewById(R.id.lo_music_mini_epg_progress_bar);
    }

    private void init_hot_key_ui() {
        g_layout_hotkey = findViewById(R.id.lo_music_hot_key_hint);
        g_imgv_hot_key_red = findViewById(R.id.lo_music_hot_key_hint_imgv_record);
        g_textv_hot_key_red = findViewById(R.id.lo_music_hot_key_hint_textv_record);
        //g_imgv_hot_key_green = findViewById(R.id.lo_music_hot_key_hint_imgv_mode);
        //g_textv_hot_key_green = findViewById(R.id.lo_music_hot_key_hint_textv_mode);
        g_imgv_hot_key_yellow = findViewById(R.id.lo_music_hot_key_hint_imgv_remind);
        g_textv_hot_key_yellow = findViewById(R.id.lo_music_hot_key_hint_textv_remind);
        g_imgv_hot_key_blue = findViewById(R.id.lo_music_hot_key_hint_imgv_genre);
        g_textv_hot_key_blue = findViewById(R.id.lo_music_hot_key_hint_textv_genre);
        g_imgv_hot_key_ok = findViewById(R.id.lo_music_hot_key_hint_imgv_watch);
        g_textv_hot_key_ok = findViewById(R.id.lo_music_hot_key_hint_textv_watch);
        if (Pvcfg.getPVR_PJ()) {
            g_imgv_hot_key_red.setVisibility(VISIBLE);
            g_textv_hot_key_red.setVisibility(VISIBLE);
        }
        else {
            g_imgv_hot_key_red.setVisibility(GONE);
            g_textv_hot_key_red.setVisibility(GONE);
        }
    }

    private void init_music_category() {
        g_category_index = 0;
        g_rcv_category_list.setHasFixedSize(true);
        ((SimpleItemAnimator) Objects.requireNonNull(g_rcv_category_list.getItemAnimator())).setSupportsChangeAnimations(false);
        g_rcv_category_list.setLayoutManager(new MyLinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        g_rcv_category_list.addItemDecoration(new MusicCategoryAdapter.MyItemDecoration());
        g_adpt_category = new MusicCategoryAdapter(get_context(), get_music_category_list()
         , new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_lock())
                    get_context().g_liveTvMgr.open_password_dialog();
            }
        }, new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    g_view_last_category = v;
                    g_category_index = (Integer) v.getTag();
                    update_music_list();
                }
            }
        }, new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return false;
            }
        });
        g_rcv_category_list.setAdapter(g_adpt_category);
    }

    public List<MusicInfo> get_music_category_list() {
        g_category_info_list = MusicInfo.get_current_category(get_context());
        return g_category_info_list;
    }

    private void addFavInfo(List<FavGroup> favGroupList, int group_index,int fav_index,long channel_id) {
        List<FavInfo> favInfoList = favGroupList.get(group_index).getFavInfoList();
        FavInfo favInfo = new FavInfo(fav_index, channel_id, group_index);
        favInfoList.add(favInfo);
        get_context().g_dtv.fav_info_save_db(favInfo);
    }

    public void category_update_to_fav(Context context) {
        Log.d(TAG, "category_update_to_fav: ");
        List<ProgramInfo> musicInfoList = get_context().g_dtv.get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
        Scan_utils mScanUtils = new Scan_utils(context);
        mScanUtils.category_update_to_fav(musicInfoList, g_category_info_list);
    }

    public void category_add_to_fav() {
        Log.d(TAG, "category_add_to_fav: ");
        int mandarin=0, western=0, jpop=0, lounge=0, classical=0, musicelse=0;
        int serviceId;
        List<ProgramInfo> musicInfoList = get_context().g_dtv.get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
        List<FavGroup> favGroupList = DataManager.getDataManager(get_context()).getFavGroupList();

        if(favGroupList.size() != FavGroup.ALL_TV_RADIO_TYPE_MAX){
            LogUtils.d("FAV Group number is not Match !!!!!!!! favGroupList.size() = "+favGroupList.size());
            LogUtils.d("Rebuild FAV Group");
            DefaultValue defaultValue = new DefaultValue(TpInfo.DVBC);
            favGroupList.clear();
            DataManager.getDataManager(get_context()).rebuildFavGroup(defaultValue);
        }

        for (ProgramInfo programInfo : musicInfoList) {
            Log.d(TAG, "category_add_to_fav: name = " + programInfo.getDisplayName() + " service id = " + programInfo.getServiceId());
            serviceId = programInfo.getServiceId()/* + 1013*/;

            for (int i = 0 ; i < g_category_info_list.size() ; i++) {
                MusicInfo musicInfo = g_category_info_list.get(i);
                if (musicInfo.get_service_id_list().contains(serviceId)) {
                    if (i == 0) {
                        addFavInfo(favGroupList,FavGroup.GROUP_MANDARIN,mandarin,programInfo.getChannelId());
                        mandarin++;
                    } else if (i == 1) {
                        addFavInfo(favGroupList,FavGroup.GROUP_WESTERN,western,programInfo.getChannelId());
                        western++;
                    } else if (i == 2) {
                        addFavInfo(favGroupList,FavGroup.GROUP_JPOP,jpop,programInfo.getChannelId());
                        jpop++;
                    } else if (i == 3) {
                        addFavInfo(favGroupList,FavGroup.GROUP_LOUNGE,lounge,programInfo.getChannelId());
                        lounge++;
                    } else if (i == 4) {
                        addFavInfo(favGroupList,FavGroup.GROUP_CLASSICAL,classical,programInfo.getChannelId());
                        classical++;
                    } else if (i == 5) {
                        addFavInfo(favGroupList,FavGroup.GROUP_ELSE,musicelse,programInfo.getChannelId());
                        musicelse++;
                    }
                }
            }
        }
        Log.d(TAG, "MANDARIN = "+mandarin);
        Log.d(TAG, "WESTERN = "+western);
        Log.d(TAG, "JPOP = "+jpop);
        Log.d(TAG, "LOUNGE = "+lounge);
        Log.d(TAG, "CLASSICAL = "+classical);
        Log.d(TAG, "ELSE = "+musicelse);
    }

    public void clear_category_to_fav() {
        Log.d(TAG, "clear_category_to_fav: ");
        List<FavGroup> favGroupList = DataManager.getDataManager(get_context()).getFavGroupList();
        favGroupList.get(FavGroup.GROUP_MANDARIN).delAllFavInfo();
        favGroupList.get(FavGroup.GROUP_WESTERN).delAllFavInfo();
        favGroupList.get(FavGroup.GROUP_JPOP).delAllFavInfo();
        favGroupList.get(FavGroup.GROUP_LOUNGE).delAllFavInfo();
        favGroupList.get(FavGroup.GROUP_CLASSICAL).delAllFavInfo();
        favGroupList.get(FavGroup.GROUP_ELSE).delAllFavInfo();
    }

    private void init_music_list() {
        g_rcv_music_list.setHasFixedSize(true);
        g_rcv_music_list.setLayoutManager(new MyLinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        g_rcv_music_list.addItemDecoration(new MusicListAdapter.MyItemDecoration(get_context()));
        g_adpt_list = new MusicListAdapter(get_music_list()
        , new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int displayNumber = Utils.get_tag_id((String) v.getTag());
                if (displayNumber  <= 0) {
                    return;
                }

                if (is_lock()) {
                    get_context().g_liveTvMgr.open_password_dialog();
                    return;
                }
                change_channel(displayNumber);
            }
        }, new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    g_view_last_list_view = v;
                }
            }
        }, new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (g_view_last_list_view == null || !g_view_last_category.isShown()) {
                    return false;
                }
                g_view_last_category.requestFocus();
                return true;
            }
        });

        g_rcv_music_list.setAdapter(g_adpt_list);
    }

    public List<ProgramInfo> get_music_list() {
        Log.d(TAG, "get_music_list: category index = " + g_category_index);
        int favGroupType = get_music_category_type_in_fav_group();

        if (get_context() == null) {
            Log.e(TAG, "get_music_list: context == null");
            return new ArrayList<>();
        }

        if (get_context().g_dtv == null) {
            Log.e(TAG, "get_music_list: g_dtv == nul");
            return new ArrayList<>();
        }

        List<ProgramInfo> music_list = get_context().g_dtv.get_program_info_list(favGroupType, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
        if (music_list == null) {
            Log.e(TAG, "get_music_list: music list is NULL");
            return new ArrayList<>();
        }
        Log.d(TAG, "get_music_list: music_list size = " + music_list.size());
        return  music_list;
    }

    public int get_music_category_type_in_fav_group() {
        return g_category_index + FavGroup.GROUP_MANDARIN;
    }

    public void update_music_category_ui() {
        g_adpt_category.update_data(g_category_info_list);
    }

    public void update_music_list() {
        if (g_category_info_list.isEmpty()) {
            return;
        }

        if (g_category_index < 0 || g_category_index >= g_adpt_category.getItemCount())
            g_category_index = 0;

        if (g_adpt_list != null)
            g_adpt_list.update_data(get_music_list());
    }

    private boolean is_lock() {
        return get_context().g_liveTvMgr.g_blocked_channel.isLock();
    }

    public void update_music_ad() {
        g_music_ad_info_list = MusicAdScheduleInfo.get_music_ad_info_list_from_schedule(MusicAdScheduleInfo.get_music_ad_schedule_info_list(get_context()));
    }

    private void update_open_ad_hint() {
        if (g_music_ad_info_list.isEmpty())
            return;

        String adUrl = g_music_ad_info_list.get(g_current_ad_index).get_ad_url();
        String sourceType = g_music_ad_info_list.get(g_current_ad_index).get_source_type();
        if (adUrl.isEmpty()) {
            set_hint_yellow(View.GONE);
            return;
        }
        set_hint_yellow(View.VISIBLE);
    }

    public void hide_internal_music_ui() {
        Log.d(TAG, "hide_internal_music_ui: ");
        g_rltvl_category_layer.setVisibility(GONE);
        g_rltvl_music_layer.setVisibility(GONE);
        g_rltvl_mini_epg_layer.setVisibility(GONE);
        g_layout_hotkey.setVisibility(GONE);
        g_rltvl_error_code_layer.setVisibility(GONE);
        removeCallbacks(g_ad_runnable);
        removeCallbacks(gUpdatePresentFollow);
    }

    public void show_internal_music_ui() {
        Log.d(TAG, "show_internal_music_ui: ");
        g_rltvl_category_layer.setVisibility(VISIBLE);
        g_rltvl_music_layer.setVisibility(VISIBLE);
        g_rltvl_mini_epg_layer.setVisibility(VISIBLE);
        g_layout_hotkey.setVisibility(VISIBLE);
        if (g_rcv_category_list != null)
            g_rcv_category_list.requestFocus();
        else Log.e(TAG, "show_ui: g_rcv_category_list == null");
        removeCallbacks(g_ad_runnable);
        post(g_ad_runnable);
    }

    public void request_focus_on_category() {
        if (g_view_last_category != null) {
            g_view_last_category.requestFocus();
            return;
        }

        if (g_rcv_category_list != null)
            g_rcv_category_list.requestFocus();
    }

    public boolean is_show() {
        return getVisibility() == VISIBLE;
    }

    /** @noinspection BooleanMethodIsAlwaysInverted*/
    public boolean is_music_ui_show() {
        return g_rltvl_category_layer.getVisibility() == VISIBLE
                && g_rltvl_music_layer.getVisibility() == VISIBLE
                && g_rltvl_mini_epg_layer.getVisibility() == VISIBLE;
    }

    @SuppressLint("SetTextI18n")
    public void update_present_program(EPGEvent epgEvent) {
        Log.d(TAG, "update_present_program: " + (epgEvent != null ? epgEvent.get_event_name() : "NULL"));
        g_epg_present = epgEvent;

        if (is_present_valid(epgEvent)) {
            g_textv_mini_epg_present_time.setText(
                    MiniEPG.ms_to_time(epgEvent.get_start_time(MiniEPG.TIME_ZONE_OFFSET)) + " - " +
                    MiniEPG.ms_to_time(epgEvent.get_end_time(MiniEPG.TIME_ZONE_OFFSET))
            );
            String programName = g_textv_mini_epg_present_program_name.getText().toString();
            if (!epgEvent.get_event_name().equals(programName))
                g_textv_mini_epg_present_program_name.setText(epgEvent.get_event_name());
            g_textv_mini_epg_present_status.setText(R.string.hint_current_mini);
            set_grading(epgEvent);
            set_event_progress(epgEvent);

            // check present periodically
            removeCallbacks(gUpdatePresentFollow);
            postDelayed(gUpdatePresentFollow, CHECK_INTERVAL_MS);
        }
        else if(epgEvent == null) {
            g_textv_mini_epg_present_time.setText("00:00 - 00:00");
            g_textv_mini_epg_present_status.setText(R.string.epg_no_data);
            g_textv_mini_epg_present_program_name.setText(R.string.epg_no_data);
            g_imgv_mini_epg_present_grading.setVisibility(GONE);
            //g_imgv_mini_epg_present_record.setVisibility(GONE);
            g_imgv_mini_epg_present_reminder.setVisibility(GONE);
            set_event_progress(null);
        }
        g_textv_mini_epg_present_program_name.setSelected(true);
    }

    @SuppressLint("SetTextI18n")
    public void update_follow_program(EPGEvent epgEvent) {
        Log.d(TAG, "update_follow_program: " + (epgEvent != null ? epgEvent.get_event_name() : "NULL"));

        if (!is_follow_valid(epgEvent))
            epgEvent = null;

        if (epgEvent != null) {
            g_textv_mini_epg_follow_time.setText(
                    MiniEPG.ms_to_time(epgEvent.get_start_time(MiniEPG.TIME_ZONE_OFFSET)) + " - " +
                    MiniEPG.ms_to_time(epgEvent.get_end_time(MiniEPG.TIME_ZONE_OFFSET))
            );
            g_textv_mini_epg_follow_program_name.setText(epgEvent.get_event_name());
            g_textv_mini_epg_follow_status.setText(R.string.hint_next_mini);

            g_epg_follow = epgEvent;
        }
        else {
            g_textv_mini_epg_follow_time.setText("00:00 - 00:00");
            g_textv_mini_epg_follow_status.setText(R.string.epg_no_data);
            g_textv_mini_epg_follow_program_name.setText(R.string.epg_no_data);
            g_imgv_mini_epg_follow_grading.setVisibility(GONE);
            g_imgv_mini_epg_follow_record.setVisibility(GONE);
            g_imgv_mini_epg_follow_reminder.setVisibility(GONE);

        }
    }

    public void update_current_channel(ProgramInfo programInfo) {
        if (programInfo != null) {
            Log.d(TAG, "update_current_channel: " + programInfo.getDisplayNameFull());
            g_textv_mini_epg_channel_name.setText(programInfo.getDisplayName());
            g_textv_mini_epg_channel_name.setSelected(true);
            g_textv_mini_epg_channel_num.setText(Utils.get_leading_zero_number_at(programInfo.getDisplayNum()));
            g_epg_present = null;
            g_epg_follow = null;
            set_recording_status(programInfo);
        }
    }

    public void update_current_channel() {
        ProgramInfo programInfo = get().get_live_tv_manager().get_current_channel();
        if (!get().get_live_tv_manager().is_music_channel(programInfo))
            return;

        update_current_channel(programInfo);
    }

    private void set_grading(EPGEvent epgEvent) {
        int parental_rate_image = MiniEPG.get_grading_res_id(epgEvent);
        g_imgv_mini_epg_present_grading.setBackgroundResource(parental_rate_image);
        g_imgv_mini_epg_present_grading.setVisibility(parental_rate_image == 0 ? GONE : VISIBLE);
    }

    public void set_event_progress(EPGEvent epgEvent) {
        int duration = epgEvent != null ? (int) epgEvent.get_duration() : 100;
        int progress = epgEvent != null ? (int) (System.currentTimeMillis() - epgEvent.get_start_time(MiniEPG.TIME_ZONE_OFFSET)) : 0;
        g_progress_bar.setMax(duration);
        g_progress_bar.setProgress(progress);
    }

    public void set_recording_status(ProgramInfo channel) {
        if (!Pvcfg.getPVR_PJ() || null == get().get_live_tv_manager() || null == get().get_live_tv_manager().g_chChangeMgr)
            return;

        LiveTvManager liveTvManager = get().get_live_tv_manager();
        ChannelChangeManager changeManager = liveTvManager.g_chChangeMgr;

        // visibility
        boolean is_current_recording = MiniEPG.is_now_recording(get(), channel);
        set_recording_visibility(is_current_recording);
        Log.d(TAG, "set_recording_status: [visible] " + is_current_recording);

        // red hint: "Record" or "Stop record"
        TextView redHint = findViewById(R.id.lo_music_hot_key_hint_textv_record);
        redHint.setText(is_current_recording ? R.string.hint_rcu_stop_record : R.string.hint_rcu_record);

        // recording hint: setup --------------------------------------------------- BEGIN
        TextView recordText = findViewById(R.id.lo_music_textv_record_channel_hint);
        StringBuilder recordHint = new StringBuilder();
        List<RecChannelInfo> recInfoList = changeManager.get_rec_channel_info();

        // recording hint: "CH001"
        for (RecChannelInfo recInfo : recInfoList) {
            long channelId = recInfo.getChannelId();
            if (changeManager.is_last_recording(channelId))
                continue;
            String channelNum = liveTvManager.get_channel(channelId).getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM);
            recordHint.append(" ").append("CH").append(channelNum);
        }
        // recording hint: "CH001/CH002"
        for (RecChannelInfo recInfo : recInfoList) {
            long channelId = recInfo.getChannelId();
            if (!changeManager.is_last_recording(channelId))
                continue;
            String channelNum = liveTvManager.get_channel(channelId).getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM);
            if (recordHint.length() > 0)
                recordHint.append("/");
            recordHint.append("CH").append(channelNum);
        }
        // recording hint: "CH001/CH002 Recording"
        recordText.setText(recordHint
                .append(" ")
                .append(get().getString(R.string.hint_recording)));
        Log.d(TAG, "set_recording_status: [status] " + recordText.getText());
        // recording hint: setup --------------------------------------------------- END
    }

    public void set_recording_visibility(boolean is_current_channel_recording) {
        if (!Pvcfg.getPVR_PJ())
            return;

        boolean has_recording = MiniEPG.has_now_recording(get());
        View recLayer = findViewById(R.id.lo_music_lnrl_record_hint_layer);
        View recIconPresent = findViewById(R.id.lo_music_imgv_mini_epg_present_record);

        recLayer.setVisibility(has_recording ? VISIBLE : GONE);
        recIconPresent.setVisibility(is_current_channel_recording ? VISIBLE : GONE);
    }

    private HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    private ProgramInfo get_channel(int displayNumber) {
        return get_context().g_liveTvMgr.get_channel(displayNumber);
    }

    private void change_channel(int displayNumber) {
        ProgramInfo channelInfo = get_channel(displayNumber);

        if (null == channelInfo) {
            Log.w(TAG, "change_channel: null channel");
            return;
        }

        //check block channel
        get_context().g_liveTvMgr.g_blocked_channel.stop_blocked_channel_check();
        get_context().g_liveTvMgr.check_blocked_channel(channelInfo);

        ChannelChangeManager changeManager = ChannelChangeManager.get_instance(get_context());
        get_context().g_liveTvMgr.g_blocked_channel.set_check_tuner_lock_flag(false);
        changeManager.change_channel_by_id(channelInfo.getChannelId());
        changeManager.reset_mini_epg_index();

        get_context().g_liveTvMgr.g_blocked_channel.start_blocked_channel_check();
        get_context().g_liveTvMgr.g_handler.postDelayed(() -> get_context().g_liveTvMgr.g_blocked_channel.set_check_tuner_lock_flag(true), 3000);

        update_current_channel(channelInfo);
        update_present_program(get_context().g_dtv.get_present_event(channelInfo.getChannelId()));
        update_follow_program(get_context().g_dtv.get_follow_event(channelInfo.getChannelId()));

        if (g_rcv_music_list != null)
            g_rcv_music_list.scrollToPosition(0);

        if (g_view_last_category != null)
            g_view_last_category.requestFocus();
        else
            g_rcv_category_list.requestFocus();

        if (is_lock())
            set_hot_key_ok_to_unlock();
    }

    public void set_hot_key_ok_to_unlock() {
        g_textv_hot_key_ok.setText(R.string.hint_rcu_unlock);
    }

    public void check_lock() {
        if (is_lock())
            g_textv_hot_key_ok.setText(R.string.hint_rcu_unlock);
        else
            g_textv_hot_key_ok.setText(R.string.hint_rcu_ok_confirm);
    }

    public boolean on_key_down(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_PROG_YELLOW:
                show_ad(g_music_ad_info_list, g_current_ad_index);
                break;
            case KeyEvent.KEYCODE_INFO:
                show_info();
                break;
            case KeyEvent.KEYCODE_PROG_RED:
                setup_recording();
                break;
        }
        return false;
    }

    private void setup_recording() {
        if (Pvcfg.getPVR_PJ()) {

            if (get_context() == null || get_context().get_live_tv_manager() == null || get_context().get_live_tv_manager().g_miniEPG == null) {
                Log.e(TAG, "setup_recording: something is null");
                return;
            }

            HomeActivity homeActivity = get_context();
            homeActivity.get_live_tv_manager().g_miniEPG.setup_record();
        }
    }

    private void set_hint_yellow(int status) {
        g_imgv_hot_key_yellow.setVisibility(status);
        g_textv_hot_key_yellow.setVisibility(status);
    }

    private void show_ad(List<MusicAdInfo> AdList, int index) {
        if (null == AdList) {
            Log.w(TAG, "show_ad: AdList == null");
            return;
        }
        if (AdList.isEmpty()) {
            Log.w(TAG, "show_ad: no AD list");
            return;
        }
        if (index < 0 || index >= AdList.size()) {
            Log.w(TAG, "show_ad: index out of range");
            return;
        }
        MusicAdInfo musicAdInfo = AdList.get(index);
        show_ad(musicAdInfo);
    }

    private void show_ad(MusicAdInfo musicAdInfo) {
        Log.d(TAG, "show_ad: g_current_ad_index = " + g_current_ad_index);
        if (g_imgv_hot_key_yellow.getVisibility() == VISIBLE
            && g_textv_hot_key_yellow.getVisibility() == VISIBLE
            && getVisibility() == VISIBLE) {
            start_ad_event(musicAdInfo);
        }
    }

    private void show_info() {
        if (show_info_detail())
            return;

        show_info_no_epg();
    }

    public boolean show_info_detail() {
        if (gDetailInfo == null)
            gDetailInfo = new HotkeyInfo(get_context().g_liveTvMgr.g_miniEPG);

        if (gDetailInfo.isShowing())
            return true;

        if (get_context().g_liveTvMgr.is_block_adult()) {
            Log.w(TAG, "show_info_detail: adult lock, not show detail");
            return false;
        }

        EPGEvent epgEvent = get_context().g_liveTvMgr.get_current_present();
        if (epgEvent == null) {
            epgEvent = g_epg_present;
            Log.w(TAG, "show_info_detail: assign EPG Event (present)");
        }

        if (epgEvent == null) {
            Log.w(TAG, "show_info_detail: epgEvent == null");
            return false;
        }

        if (epgEvent.get_start_time() > System.currentTimeMillis() ||
            epgEvent.get_end_time()   < System.currentTimeMillis()) {
            Log.w(TAG, "show_info_detail: epgEvent incorrect time");
            return false;
        }

        String eventName = g_textv_mini_epg_present_program_name.getText().toString();
        if (eventName.equals(get().getString(R.string.epg_no_data))) {
            Log.w(TAG, "show_info_detail: no epg data");
            return false;
        }

        gDetailInfo.show_detail_present(epgEvent, true);
        return true;
    }

    /** @noinspection UnusedReturnValue*/
    public boolean show_info_no_epg() {
        Log.d(TAG, "show_info_message: has no EPG");
        Snakebar.show(this, R.string.epg_no_data, Snakebar.LENGTH_SHORT);
        //Toast.makeText(get_context(), get_context().getString(R.string.epg_no_data), Toast.LENGTH_SHORT).show();
        return true;
    }

    private void init_error_message() {
        g_rltvl_error_code_layer.setVisibility(VISIBLE);
        g_textv_error.setText(R.string.error_e200);
    }

    public void show_error_code(boolean show, String errorCode, Bitmap qrCode) {
        if ((is_error_code_show() && show) || !is_show()) {
            Log.w(TAG, "show_error_code: error code is showing or music view is not showing");
            return;
        }

        get().runOnUiThread(() -> {
            if (show) {
                g_rltvl_error_code_layer.startAnimation(AnimationUtils.makeInAnimation(getContext(), false));
                g_rltvl_error_code_layer.setVisibility(VISIBLE);
                if (qrCode != null)
                    g_imgv_error_qr_code.setVisibility(VISIBLE);
                else
                    g_imgv_error_qr_code.setVisibility(GONE);
                g_imgv_error_qr_code.setImageBitmap(qrCode);
            }
            else {
                g_imgv_error_qr_code.setVisibility(GONE);
                g_rltvl_error_code_layer.setVisibility(GONE);
            }
            g_textv_error.setText(errorCode);
        });
    }

    public void hide_error_code() {
        if (is_error_code_show() && is_show())
            show_error_code(false, null, null);
    }

    public boolean is_error_code_show() {
        return g_rltvl_error_code_layer.getVisibility() == VISIBLE;
    }

    public void hide_error_code_layer() {
        g_rltvl_error_code_layer.setVisibility(GONE);
    }

    private void start_ad_event(MusicAdInfo musicAdInfo) {
        String sourceType = musicAdInfo.get_source_type();
        String adUrl = musicAdInfo.get_ad_url();

        switch(sourceType) {
            case TYPE_BROWSER: {
                open_browser(adUrl);
            }break;
            case TYPE_STREAM: {
                open_stream(adUrl);
            }break;
            case TYPE_QRCODE: {
                open_qrcode(musicAdInfo);
            }break;
        }
    }

    private void open_qrcode(MusicAdInfo musicAdInfo) {
        QRCodeDialog qrCodeDialog = new QRCodeDialog(get_context(), musicAdInfo);
        qrCodeDialog.show();
    }

    private void open_stream(String url) {
        Intent intent = new Intent(get_context(), StreamActivity.class);
        intent.putExtra(StreamActivity.EXTRA_STREAM_URL, url);
        get_context().startActivity(intent);
    }

    private void open_browser(String url) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.prime.webbrowser", "com.prime.webbrowser.MainActivity"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("WEB_URL_KEY", url);
        intent.putExtra("PERMIT", true);
        get_context().startActivity(intent);
    }

    public void update_present_follow(ProgramInfo curChannel) {
        if (g_live_tv_manager == null)
            g_live_tv_manager = get().get_live_tv_manager();
        EPGEvent presentEvent = g_live_tv_manager.get_current_present();
        EPGEvent followEvent = g_live_tv_manager.get_current_follow();

        update_present_program(presentEvent);
        update_follow_program(followEvent);
    }

    private boolean is_present_valid(EPGEvent present) {
        long cur_time = System.currentTimeMillis();
        return present != null
                && cur_time >= present.get_start_time(MiniEPG.TIME_ZONE_OFFSET)
                && cur_time <= present.get_end_time(MiniEPG.TIME_ZONE_OFFSET);
    }

    private boolean is_follow_valid(EPGEvent follow) {
        long curTime = System.currentTimeMillis();
        long offset = MiniEPG.TIME_ZONE_OFFSET;

        if (g_epg_present == null)
            g_epg_present = get().get_live_tv_manager().get_current_present();

        if (follow != null
            && g_epg_present != null
            && follow.get_start_time(offset) <= g_epg_present.get_start_time(offset)
            && follow.get_end_time(offset)   <= g_epg_present.get_end_time(offset)) {
            Log.w(TAG, "is_follow_valid: follow event incorrect time");
            follow = null;
        }

        return follow != null && follow.get_start_time(offset) > curTime;
        /*
        long cur_time = System.currentTimeMillis();
        return follow != null
                && cur_time < follow.get_start_time(MiniEPG.TIME_ZONE_OFFSET);
        */
    }
}
