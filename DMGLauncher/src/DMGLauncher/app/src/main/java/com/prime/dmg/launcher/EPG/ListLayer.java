package com.prime.dmg.launcher.EPG;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.prime.dmg.launcher.EPG.EpgDetailView.EPG_LOCK_DATA;
import static com.prime.dmg.launcher.EPG.EpgDetailView.EPG_NO_DATA;
import static com.prime.dtv.service.datamanager.FavGroup.ALL_TV_TYPE;
import static com.prime.dtv.service.datamanager.FavGroup.RADIO_FAV2_TYPE;
import static com.prime.dtv.sysdata.GposInfo.GPOS_CUR_GROUP_TYPE;
import static com.prime.dtv.sysdata.ProgramInfo.PROGRAM_RADIO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.CustomView.EventDialog;
import com.prime.dmg.launcher.CustomView.MessageDialog;
import com.prime.dmg.launcher.CustomView.Snakebar;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyRecord;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyRemind;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.Utils;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** @noinspection CommentedOutCode*/
public class ListLayer extends EpgContentView {
    private static final String TAG = ListLayer.class.getSimpleName();
    public static final int FOCUS_VIEW_CHANNEL = 101;
    public static final int FOCUS_VIEW_DATE = 100;
    public static final int FOCUS_VIEW_PROGRAM = 102;
    public static final int REMIND_EVENT = 0;
    public static final int RECORD_EVENT = 1;
    public static final int RESERVE_EVENT = 2;

    private static final long ONE_DAY_IN_MILLS = 24 * 60 *60 *1000;

    private PrimeDtv gPrimeDtv;
    private HotkeyRemind gRemindDialog;
    private Handler gHandler;
    private TextView g_textv_genre = null;
    private TextView g_textv_no_data = null;
    private long g_latest_dispatch_key_time;
    private boolean g_reset_position_to_first;
    private boolean g_reset_position_to_last;
    private Epg g_epg;
    private EpgView g_epgView;
    private EpgDetailView gDetailView;
    private ThreadPoolExecutor g_task_executor;
    //private ArrayBlockingQueue<Runnable> g_task_queue;
    private ChannelChangeManager gChangeMgr;

    private boolean g_blocked_channel;
    //private boolean g_last_channel_adult;

    private int g_current_focus_view_type;
    private boolean g_have_change_mode;
    private boolean g_can_click;

    //date
    public MiddleFocusRecyclerView g_rcv_date = null;
    private EpgDateAdapter g_adpt_epg_date = null;
    private View g_view_last_date;
    private int g_prev_date_position;

    //channel
    private String g_current_genre_name;
    private int g_current_genre_id;

    private MiddleFocusRecyclerView g_rcv_channel = null;
    private EpgChannelAdapter g_adpt_epg_channel = null;
    private View g_current_channel_view;
    private int g_current_channel_position;
    private int g_curr_channel_num;
    private View.OnClickListener g_channel_click_listener;
    private List<ProgramInfo> g_channel_list = null;
    private long g_current_channel_id;
    private int g_current_service_id;
    private long g_prev_channel_id = 0;

    //program
    private MiddleFocusRecyclerView g_rcv_program = null;
    private EpgProgramAdapter gProgramAdapter = null;
    private View g_current_program_view;
    private boolean g_update_program_by_genre;
    private boolean g_program_loop;
    private int g_current_program_position;
    private int g_current_event_id;
    private View.OnClickListener g_program_click_listener;
    private List<EPGEvent> g_program_list = null;

    //hotkey
    private ImageView g_imgv_hot_key_red , g_imgv_hot_key_green, g_imgv_hot_key_yellow, g_imgv_hot_key_blue, g_imgv_hot_key_ok;
    private TextView g_textv_hot_key_red, g_textv_hot_key_green, g_textv_hot_key_yellow, g_textv_hot_key_blue, g_textv_hot_key_ok;

    //pvr
    //private EventDialog g_event_dialog = null;
    //private List<BookInfo> g_conflict_bookInfoList;
    //private int g_conflict_index;
    //private boolean g_event_dialog_back_action = false;

    private final Runnable g_reset_loop_flag = new Runnable() { // from class: com.vasott.tbc.hybrid.view.epg.EpgListView.1
        @Override
        public void run() {
            g_program_loop = false;
        }
    };

    /*private OnFocusChangeListener on_focus_listener() {
        return (view, hasFocus) -> {
            if (hasFocus) {
                g_update_program_by_genre = false;
                //if ( is_date_list(view) )           on_focus_date(view);
                //else if ( is_channel_list(view) )   on_focus_channel(view);
                //else if ( is_program_list(view) )   on_focus_program(view);
                //else Log.e(TAG, "on_focus_listener: there is no match id");
                //update_epg_hot_Keys_hint(false);
            }
        };
    }*/

    public void on_focus_date(View view) {
        Log.d(TAG, "on_focus_date: focus date");
        g_update_program_by_genre = false;
        g_current_focus_view_type = FOCUS_VIEW_DATE;
        g_view_last_date = view;
        int current_date_pos = (Integer) view.getTag();
        if (g_prev_date_position != current_date_pos) {
            g_prev_date_position = current_date_pos;
            gHandler.removeCallbacksAndMessages(null);
            gHandler.postDelayed(UPDATE_PROGRAM, 500);
        }
        update_epg_hot_Keys_hint(false);
    }

    public void on_focus_channel(View view) {
        g_update_program_by_genre = false;
        g_current_focus_view_type = FOCUS_VIEW_CHANNEL;
        g_current_channel_view = view;
        g_current_channel_position = Utils.get_tag_position((String) view.getTag());
        long channelId = Utils.get_tag_id((String) view.getTag());
        g_curr_channel_num = Utils.get_sec_tag_id((String) view.getTag());
        int tagType = Utils.get_fourth_tag_id((String) view.getTag());
        boolean isRadioChannel = tagType == PROGRAM_RADIO;
        Log.d(TAG, "on_focus_channel: [position] " + g_current_channel_position + ", [channel Id] " + channelId + ", [channel num] " + g_curr_channel_num + ", [isRadio] " + isRadioChannel);

        if (g_current_channel_id != channelId || g_have_change_mode) {
            g_current_channel_id = channelId;
            g_have_change_mode = false;
            g_can_click = false;
        }
        update_epg_hot_Keys_hint(false);
    }

    public void on_focus_program(View view, int position) {
        Log.d(TAG, "on_focus_program: focus program");
        g_update_program_by_genre = false;
        g_current_focus_view_type = FOCUS_VIEW_PROGRAM;
        g_current_program_view = view;
        g_current_event_id = Utils.get_tag_id((String) view.getTag());
        g_current_program_position = position; // Utils.get_tag_position((String) view.getTag());
        g_can_click = true;
        update_epg_hot_Keys_hint(false);
        gHandler.removeCallbacksAndMessages(null);
        gHandler.postDelayed(() -> {
            EPGEvent epgEvent = get_current_event();
            long category = get_current_category();
            gDetailView.update_program(g_current_event_id, g_current_channel_id, epgEvent, category);
        }, 500);
    }

    private EpgActivity get_activity() {
        return (EpgActivity) getContext();
    }

    public EpgProgramAdapter get_program_adapter() {
        return gProgramAdapter;
    }

    private boolean is_date_list(View view) {
        if (view != null &&
            view.getParent() != null &&
            view.getParent() instanceof MiddleFocusRecyclerView middleFocusRecyclerView) {
            return middleFocusRecyclerView.getId() == R.id.lo_epg_sub_list_rcv_date;
        }
        return false;
    }

    private boolean is_channel_list(View view) {
        if (view != null &&
            view.getParent() != null &&
            view.getParent() instanceof MiddleFocusRecyclerView middleFocusRecyclerView) {
            return middleFocusRecyclerView.getId() == R.id.lo_epg_sub_list_rcv_channel;
        }
        return false;
    }

    private boolean is_program_list(View view) {
        if (view != null &&
            view.getParent() != null &&
            view.getParent() instanceof MiddleFocusRecyclerView middleFocusRecyclerView) {
            return middleFocusRecyclerView.getId() == R.id.lo_epg_sub_list_rcv_program;
        }
        return false;
    }

    private OnKeyListener on_date_key_listener() {
        return (v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN || keyCode != KeyEvent.KEYCODE_DPAD_RIGHT || g_current_channel_view == null || !g_current_channel_view.isShown()) {
                return false;
            }
            Log.d(TAG,"onKey Right Date " );
            g_current_channel_view.requestFocus();
            return true;
        };
    }

    private OnKeyListener on_channel_key_listener() {
        return (v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (g_current_program_view == null || !g_current_program_view.isShown()) {
                    if (gProgramAdapter.getItemCount() != 0) {
                        View childAt = g_rcv_program.getChildAt(0);
                        if (childAt != null) {
                            childAt.requestFocus();
                        }
                    }
                    return true;
                }
                g_current_program_view.requestFocus();
                return true;
            }
            if (keyCode != KeyEvent.KEYCODE_DPAD_LEFT || g_view_last_date == null || !g_view_last_date.isShown()) {
                if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) && g_curr_channel_num > 0 && g_channel_click_listener != null) {
                    LogUtils.d("Press OK on channel, need to do av play");
                    EpgActivity epgActivity = (EpgActivity) getContext();
                    gChangeMgr.set_cur_ch_id(g_current_channel_id);
                    Intent goLiveTvIntent = new Intent(getContext(), HomeActivity.class);
                    goLiveTvIntent.putExtra(GposInfo.GPOS_STANDBY_REDIRECT, 2);
                    BaseActivity.set_global_key(KeyEvent.KEYCODE_GUIDE);
                    epgActivity.startActivity(goLiveTvIntent);
                    epgActivity.finish();
                    return true;
                }
                else if (keyCode != KeyEvent.KEYCODE_DPAD_UP || Utils.get_tag_position((String) v.getTag()) != 0) {
                    if (keyCode != KeyEvent.KEYCODE_DPAD_DOWN || Utils.get_tag_position((String) v.getTag()) != g_channel_list.size() - 1) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && g_view_last_date == null) {
                            g_rcv_date.getChildAt(0).requestFocus();
                            return true;
                        }
                        return false;
                    }
                    g_rcv_channel.move_to_position(0);
                    return true;
                }
                else { // KeyEvent.KEYCODE_DPAD_UP
                    g_rcv_channel.move_to_position(g_channel_list.size() - 1);
                    return true;
                }
            }
            g_view_last_date.requestFocus();
            return true;
        };
    }

    private OnKeyListener on_program_key_listener() {
        return (view, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    Intent goLiveTvIntent = new Intent(getContext(), HomeActivity.class);
                    goLiveTvIntent.putExtra(GposInfo.GPOS_STANDBY_REDIRECT, 2);
                    getContext().startActivity(goLiveTvIntent);
                    ((EpgActivity)getContext()).finish();
                    return true;
                }
                return false;
            }
            if (keyCode != KeyEvent.KEYCODE_DPAD_LEFT || g_current_channel_view == null || !g_current_channel_view.isShown()) {
                if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) && g_current_event_id > 0 && g_program_click_listener != null) {
                    g_current_program_view.setTag(g_current_program_position + "," + g_current_event_id + "," + g_curr_channel_num + "," + get_service_id());
                    //if (!TbcResourceManager.isTbcOtl()) {
                    //    SharedPrefHelper.setCurrentChannelTIFId(EpgListView.this.getContext(), EpgListView.this.mLastChannelId);
                    //}
                    g_program_click_listener.onClick(g_current_program_view);
                    return true;
                }
                else if (keyCode != KeyEvent.KEYCODE_DPAD_UP || Utils.get_tag_position((String) view.getTag()) != 0) {
                    if (keyCode != KeyEvent.KEYCODE_DPAD_DOWN || Utils.get_tag_position((String) view.getTag()) != g_program_list.size() - 1) {
                        return false;
                    }
                    g_program_loop = true;
                    postDelayed(g_reset_loop_flag, 500L);
                    int moveSelect = g_adpt_epg_date.move_select(false);
                    g_view_last_date = g_rcv_date.getChildAt(moveSelect);

                    g_prev_date_position = moveSelect;
                    g_reset_position_to_first = true;
                    g_task_executor.execute(UPDATE_PROGRAM);
                    /*postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            g_rcv_program.move_to_position(0, g_rcv_program.getLayoutManager().getHeight() / 2);
                        }
                    }, 50L);*/
                    enable_date_and_channel_list(false);
                    postDelayed(g_enable_list_runnable, 500L);
                    return true;
                }
                else {
                    g_program_loop = true;
                    postDelayed(g_reset_loop_flag, 500L);
                    int moveSelect = g_adpt_epg_date.move_select(true);
                    g_view_last_date = g_rcv_date.getChildAt(moveSelect);
                    g_prev_date_position = moveSelect;
                    g_reset_position_to_last = true;
                    g_task_executor.execute(UPDATE_PROGRAM);
                    /*postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            g_rcv_program.move_to_position(g_program_list.size() - 1, g_rcv_program.getLayoutManager().getHeight() / 2);
                        }
                    }, 50L);*/
                    return true;
                }
            }
            g_current_channel_view.requestFocus();
            return true;
        };
    }

    public void update_program(long channelId) {
        g_current_channel_id = channelId;
        update_program();
    }

    public void update_program() {
        if (is_adult_blocked()) {
            Log.d(TAG, "update_program: for blocked channel");
            gProgramAdapter.update_program(null, 0);
            get_activity().runOnUiThread(() -> {
                g_textv_no_data.setText(R.string.epg_lock_data);
                g_textv_no_data.setVisibility(VISIBLE);
            });
            g_can_click = true;
            gDetailView.update_program(EPG_LOCK_DATA, g_current_channel_id, null, get_current_category());
        }
        else {
            Log.d(TAG, "update_program: for unblocked channel, channel Id = " + g_current_channel_id);
            update_program_at_channel_in_time(g_current_channel_id, get_time_stamp(true), get_time_stamp(false));
        }
    }

    private final Runnable UPDATE_PROGRAM = new Runnable() {
        @Override
        public void run() {
            //int serviceId = 0;
            ProgramInfo channel = g_epg.get_program_by_channel_id(g_current_channel_id);
            if (channel == null || g_channel_list == null || g_channel_list.isEmpty() || get_current_channel() == null) {
                Log.e(TAG, "UPDATE_PROGRAM: something is wrong");
                return;
            }
            g_current_service_id = channel.getServiceId();
            if (g_current_channel_id > 0 && g_current_service_id > 0) {
                Log.d(TAG, "UPDATE_PROGRAM: update channel's program >> " + channel.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM) + " " + channel.getDisplayName() + ", [adult blocked] " + is_adult_blocked());
                update_program(g_current_channel_id); // UPDATE_PROGRAM
            }
        }
    };

    private ProgramInfo get_current_channel() {
        if (g_channel_list == null || g_channel_list.isEmpty() || g_current_channel_position < 0 || g_current_channel_position >= g_channel_list.size())
            return null;
        return g_channel_list.get(g_current_channel_position);
    }

    private long get_current_category() {
        if (get_current_channel() == null)
            return -1;
        return get_current_channel().getCategory_type();
    }

    private EPGEvent get_current_event() {
        if (g_program_list == null || g_program_list.isEmpty() || g_current_program_position < 0 || g_current_program_position >= g_program_list.size())
            return null;
        return g_program_list.get(g_current_program_position);
    }

    public ListLayer(Context context) {
        super(context);
    }

    public ListLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void set_epg(Epg epg) {
        g_epg = epg;
    }

    public void init(Epg epg,EpgView epgView) {
        gChangeMgr = ChannelChangeManager.get_instance(getContext());
        g_epg = epg;
        g_epgView = epgView;
        gDetailView = epgView.get_detail_view();
        gHandler = epgView.get().get_thread_handler();
        init_view();
        init_task_queue();

        this.g_current_channel_id = -1;
        this.g_current_event_id = -1;
        g_current_focus_view_type = FOCUS_VIEW_DATE;

        init_date_list();
        init_channel_list();
        init_program_list();
    }

    private void init_view() {
        inflate(getContext(), R.layout.view_epg_list, this);
        g_textv_genre = findViewById(R.id.lo_epg_sub_list_textv_genre);
        g_textv_no_data = findViewById(R.id.lo_epg_sub_list_textv_no_data);
        g_rcv_date = findViewById(R.id.lo_epg_sub_list_rcv_date);
        g_rcv_channel = findViewById(R.id.lo_epg_sub_list_rcv_channel);
        g_rcv_program = findViewById(R.id.lo_epg_sub_list_rcv_program);

        //hot key view
        g_imgv_hot_key_red = findViewById(R.id.lo_epg_hot_key_hint_imgv_record);
        g_textv_hot_key_red = findViewById(R.id.lo_epg_hot_key_hint_textv_record);
        //g_imgv_hot_key_green = findViewById(R.id.lo_epg_hot_key_hint_imgv_mode);
        //g_textv_hot_key_green = findViewById(R.id.lo_epg_hot_key_hint_textv_mode);
        g_imgv_hot_key_yellow = findViewById(R.id.lo_epg_hot_key_hint_imgv_remind);
        g_textv_hot_key_yellow = findViewById(R.id.lo_epg_hot_key_hint_textv_remind);
        g_imgv_hot_key_blue = findViewById(R.id.lo_epg_hot_key_hint_imgv_genre);
        g_textv_hot_key_blue = findViewById(R.id.lo_epg_hot_key_hint_textv_genre);
        g_imgv_hot_key_ok = findViewById(R.id.lo_epg_hot_key_hint_imgv_watch);
        g_textv_hot_key_ok = findViewById(R.id.lo_epg_hot_key_hint_textv_watch);

    }

    private void init_task_queue() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 3000L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        g_task_executor = threadPoolExecutor;
        threadPoolExecutor.allowCoreThreadTimeOut(true);
    }

    public void init_date_list() {
        g_rcv_date.setHasFixedSize(true);
        SimpleItemAnimator animator = (SimpleItemAnimator) g_rcv_date.getItemAnimator();
        if (animator != null)
            animator.setSupportsChangeAnimations(false);
        g_rcv_date.setLayoutManager(new MyLinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        g_rcv_date.addItemDecoration(new EpgDateAdapter.MyItemDecoration(getContext()));

        g_adpt_epg_date = new EpgDateAdapter(getContext(), get_date_list(), null, on_date_key_listener(), this);
        g_rcv_date.setAdapter(g_adpt_epg_date);
    }

    public void init_channel_list() {
        g_rcv_channel.setHasFixedSize(true);
        SimpleItemAnimator animator = (SimpleItemAnimator) g_rcv_channel.getItemAnimator();
        if (animator != null)
            animator.setSupportsChangeAnimations(false);
        g_rcv_channel.setLayoutManager(new MyLinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        g_rcv_channel.addItemDecoration(new EpgDateAdapter.MyItemDecoration(getContext()));
        g_adpt_epg_channel = new EpgChannelAdapter(getContext(), get_channel_list(), null, on_channel_key_listener(), this);
        g_rcv_channel.setAdapter(g_adpt_epg_channel);
    }

    public void init_program_list() {
        g_rcv_program.setHasFixedSize(true);
        SimpleItemAnimator animator = (SimpleItemAnimator) g_rcv_program.getItemAnimator();
        if (animator != null)
            animator.setSupportsChangeAnimations(false);
        g_rcv_program.setLayoutManager(new MyLinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        g_rcv_program.addItemDecoration(new EpgDateAdapter.MyItemDecoration(getContext()));
        gProgramAdapter = new EpgProgramAdapter(getContext(), null, null, on_program_key_listener(), this);
        g_rcv_program.setAdapter(gProgramAdapter);
        g_textv_no_data.setVisibility(VISIBLE);
    }

    private SparseArray<String> get_date_list() {
        SparseArray<String> sparseArray = new SparseArray<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd  EEEE", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            if (i != 0) {
                calendar.add(5, 1);
            }
            sparseArray.put(i, simpleDateFormat.format(calendar.getTime()));
        }
        return sparseArray;
    }

    private List<ProgramInfo> get_channel_list() {
        if (g_current_genre_id == ALL_TV_TYPE)
            g_channel_list = g_epg.get_all_tv_program_info_list();
        else
            g_channel_list = g_epg.get_program_info_list(g_current_genre_id, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);

        if (!g_channel_list.isEmpty()) {
            g_current_channel_id = g_channel_list.get(0).getChannelId();
        }
        return g_channel_list;
    }

    /*private List<EPGEvent> get_program_list_from_db() {
        g_program_list = g_epg.epg_event_get_epg_event_list(g_current_channel_id, get_time_stamp(true), get_time_stamp(false), 0);

        if (g_program_list == null)
            g_textv_no_data.setVisibility(VISIBLE);
        else if (g_program_list.isEmpty())
            g_textv_no_data.setVisibility(VISIBLE);
        else
            ((EpgActivity)getContext()).get_epg_view().set_epg_detail_view(g_program_list.get(0), g_channel_list.get(0).getChannelId() , g_channel_list.get(0).getCategory_type());
        return g_program_list;
    }*/

    public void update_epg_data_by_genre_id(int genreId, String name, boolean changeByZapping, boolean channelChangeFlag) {
        Log.d(TAG, "update_epg_data_by_genre_id name = " + name + " id = " + genreId);
        g_update_program_by_genre = true;
        g_current_genre_name = name;
        g_current_genre_id = genreId;

        post(new Runnable() {
            @Override
            public void run() {
                g_textv_genre.setText(g_current_genre_name);
                if (channelChangeFlag) {
                    gProgramAdapter.update_program(null, 0);
                    g_textv_no_data.setText(getContext().getText(R.string.epg_loading));
                    g_textv_no_data.setVisibility(VISIBLE);
                }
                g_channel_list = g_epg.get_program_info_list_by_genre(genreId);
                g_have_change_mode = true;
                g_adpt_epg_channel.update_channel(g_channel_list);
                g_adpt_epg_channel.notifyDataSetChanged();
                focus_channel();
                g_epg.gpos_info_update_by_key_string(GPOS_CUR_GROUP_TYPE, g_current_genre_id);
            }
        });

        if (g_adpt_epg_date.update_date(get_date_list()) || changeByZapping) {
            g_rcv_date.move_to_position(0);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update_channel_list() {
        post(() -> {
            gProgramAdapter.update_program(null, 0);
            g_textv_no_data.setText(getContext().getText(R.string.epg_loading));
            g_textv_no_data.setVisibility(VISIBLE);
            if (g_current_genre_id == ALL_TV_TYPE)
                g_channel_list = g_epg.get_all_tv_program_info_list();
            else
                g_channel_list = g_epg.get_program_info_list(g_current_genre_id, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);

            g_adpt_epg_channel.update_channel(g_channel_list);
            g_adpt_epg_channel.notifyDataSetChanged();
            focus_channel();
        });
    }

    public void update_program_data(long channelId) {
        update_program_at_channel_in_time(channelId, get_time_stamp(true), get_time_stamp(false));
        if (g_current_focus_view_type == FOCUS_VIEW_PROGRAM)
            g_rcv_program.move_to_position(0);
    }

    private synchronized void update_program_at_channel_in_time(final long channelId, long startTimestamp, long endTimestamp) {
        Log.d(TAG, "update_program_at_channel_in_time: [time] " + MiniEPG.ms_to_time(startTimestamp, "MM/dd HH:mm") + " - " + MiniEPG.ms_to_time(endTimestamp, "MM/dd HH:mm"));
        boolean updateByGenre = g_update_program_by_genre;
        List<EPGEvent> programByTime = g_epg.get_epg_events(channelId, startTimestamp, endTimestamp, 0);

        g_adpt_epg_date.update_date(get_date_list());
        if (programByTime == null) {
            Log.w(TAG, "update_program_at_channel_in_time: programByTime is null");
            return;
        }
        if (gProgramAdapter != null && gProgramAdapter.same_programs(programByTime)) {
            Log.w(TAG, "update_program_at_channel_in_time: same programs, do not update again");
            return;
        }

        g_program_list = programByTime;
        g_current_program_view = null;

        if (programByTime.isEmpty()) {
            g_can_click = true;
            gDetailView.update_program(EPG_NO_DATA, g_current_channel_id, null, -1);
            if (g_current_focus_view_type == FOCUS_VIEW_PROGRAM && g_program_loop) {
                ListLayer.this.removeCallbacks(g_enable_list_runnable);
                enable_date_and_channel_list(true);
                g_view_last_date.requestFocus();
            }
            get_activity().runOnUiThread(() -> {
                g_textv_no_data.setText(ListLayer.this.getContext().getText(R.string.epg_no_data));
                g_textv_no_data.setVisibility(VISIBLE);
            });
        }
        else {
            EPGEvent firstEvent = programByTime.get(0);
            g_current_event_id = firstEvent.get_event_id();
            long chId = updateByGenre ? -1 : g_current_channel_id;
            long category = get_current_category();
            gDetailView.update_program(g_current_event_id, chId, firstEvent, category);
            get_activity().runOnUiThread(() -> g_textv_no_data.setVisibility(GONE));
        }

        if (is_adult_blocked()) {
            Log.w(TAG, "update_program_at_channel_in_time: is adult blocked");
            return;
        }

        gProgramAdapter.update_program(g_program_list, g_current_channel_id);
        if (g_program_list == null || g_program_list.isEmpty()) {
            Log.e(TAG, "update_program_at_channel_in_time: Program List is null or empty");
            return;
        }

        get_activity().runOnUiThread(() -> {
            Log.d(TAG, "update_program_at_channel_in_time: Program list size = " + g_program_list.size());
            if (g_rcv_program == null || g_rcv_program.getLayoutManager() == null) {
                Log.e(TAG, "update_program_at_channel_in_time: something is null");
                return;
            }

            g_rcv_program.scrollToPosition(0);

            if (g_reset_position_to_first) {
                g_reset_position_to_first = false;
                postDelayed(() -> g_rcv_program.move_to_position(0), 50L);
            }
            if (!g_reset_position_to_last) {
                return;
            }
            g_reset_position_to_last = false;
            postDelayed(() -> g_rcv_program.move_to_position(g_program_list.size() - 1), 50L);
        });
    }

    private long get_time_stamp(boolean start) {
        Calendar tmpCalendar = get_Date_by_offset(g_prev_date_position);
        Date startDate;
        if (start) {
            if (g_prev_date_position != 0) {
                tmpCalendar.set(Calendar.HOUR_OF_DAY, 0);
                tmpCalendar.set(Calendar.MINUTE, 0);
                tmpCalendar.set(Calendar.SECOND, 0);
                tmpCalendar.set(Calendar.MILLISECOND, 0);
            }
            startDate = tmpCalendar.getTime();
            return startDate.getTime();
        }

        tmpCalendar.add(Calendar.DAY_OF_MONTH, +1);
        tmpCalendar.set(Calendar.HOUR_OF_DAY, 0);
        tmpCalendar.set(Calendar.MINUTE, 0);
        tmpCalendar.set(Calendar.SECOND, 0);
        tmpCalendar.set(Calendar.MILLISECOND, 0);

        Date endDate = tmpCalendar.getTime();
        return endDate.getTime();
    }

    private Calendar get_Date_by_offset(int dayOffSet) {
        Date date = get_local_time();
        if (date == null)
        {
            Log.d(TAG, "getDateByOffset:  Date is NULL !!!!!!!");
            date = new Date();
        }

        long dateOffsetMills = dayOffSet * (ONE_DAY_IN_MILLS);
        long dateMills = date.getTime() + dateOffsetMills;
        date.setTime(dateMills);
        Calendar ca = Calendar.getInstance();
        if (TmGetCurrentTimeZone() != 0)
        {
            ca.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        ca.setTime(date);

        return ca;
    }

    public Date get_local_time() {
        Date Time= g_epg.get_dtv_date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(Time == null)
        {
            Log.d(TAG, "GetLocalTime:  TIME = NULL !!!!!!!!!");
            Time = new Date();
        }
        String str = format.format(Time.getTime());
        return Time;
    }

    public int TmGetCurrentTimeZone(){
        int TimeZone = g_epg.get_dtv_time_zone();
        return TimeZone;
    }

    public void update_date_list() {
        if(g_adpt_epg_date.update_date(get_date_list()))
            focus_channel();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN)) {
            if ((event.getRepeatCount() > 0 && System.currentTimeMillis() - g_latest_dispatch_key_time < 120) || g_program_loop) {
                return true;
            }
            g_latest_dispatch_key_time = System.currentTimeMillis();
        }
        return super.dispatchKeyEvent(event);
    }

    public void set_channel_click(View.OnClickListener listener) {
        g_channel_click_listener = listener;
    }

    public int get_service_id() {
        if (g_program_list == null || g_current_channel_position < 0 ||  g_channel_list.isEmpty()) {
            return -1;
        }

        if (g_program_list.isEmpty())
            return -1;

        return g_program_list.get(0).get_s_id();
    }

    public void enable_date_and_channel_list(boolean enable) {
        get_activity().runOnUiThread(()-> {
            set_list_item_enable(g_rcv_date, enable);
            set_list_item_enable(g_rcv_channel, enable);
        });
    }

    private void set_list_item_enable(RecyclerView target, boolean enable) {
        for (int i = 0; i < target.getChildCount(); i++) {
            View childAt = target.getChildAt(i);
            if (childAt != null) {
                childAt.setFocusable(enable);
                childAt.setEnabled(enable);
            }
        }
    }

    private Runnable g_enable_list_runnable = new Runnable() {
        @Override
        public void run() {
            enable_date_and_channel_list(true);
        }
    };

    public long get_current_channel_id() {
        return g_current_channel_id;
    }

    public long get_current_service_id() {
        return g_current_service_id;
    }

    public void set_current_channel_id(long channelId) {
        g_current_channel_id = channelId;
    }

    public void update_epg_hot_Keys_hint(boolean forceReset) {
        //get_activity().runOnUiThread(() -> {
        if(forceReset) {
            g_imgv_hot_key_red.setVisibility(View.GONE);
            g_textv_hot_key_red.setVisibility(View.GONE);
            //g_imgv_hot_key_green.setVisibility(View.VISIBLE);
            //g_textv_hot_key_green.setVisibility(View.VISIBLE);
            g_imgv_hot_key_yellow.setVisibility(View.GONE);
            g_textv_hot_key_yellow.setVisibility(View.GONE);
            g_imgv_hot_key_blue.setVisibility(View.VISIBLE);
            g_textv_hot_key_blue.setVisibility(View.VISIBLE);
            g_imgv_hot_key_ok.setVisibility(View.GONE);
            g_textv_hot_key_ok.setVisibility(View.GONE);
        }

        if (g_current_focus_view_type == FOCUS_VIEW_PROGRAM) {
            //g_imgv_hot_key_green.setVisibility(View.VISIBLE);
            //g_textv_hot_key_green.setVisibility(View.VISIBLE);
            if (Pvcfg.getPVR_PJ()) {
                g_imgv_hot_key_red.setVisibility(View.VISIBLE);
                g_textv_hot_key_red.setVisibility(View.VISIBLE);
                if (check_time_expired(REMIND_EVENT)) {
                    g_imgv_hot_key_yellow.setVisibility(View.GONE);
                    g_textv_hot_key_yellow.setVisibility(View.GONE);
                }
                else {
                    g_imgv_hot_key_yellow.setVisibility(View.VISIBLE);
                    g_textv_hot_key_yellow.setVisibility(View.VISIBLE);

                    if (is_reminder() || get_activity().is_mark_remind())
                        g_textv_hot_key_yellow.setText(getContext().getText(R.string.hint_rcu_cancel_remind));
                    else
                        g_textv_hot_key_yellow.setText(getContext().getText(R.string.hint_rcu_remind));
                }


                if (is_reserve_record())
                    g_textv_hot_key_red.setText(getContext().getText(R.string.hint_rcu_cancel_record));
                else
                    g_textv_hot_key_red.setText(getContext().getText(R.string.hint_rcu_record));
            }
            g_imgv_hot_key_blue.setVisibility(View.VISIBLE);
            g_textv_hot_key_blue.setVisibility(View.VISIBLE);
            g_imgv_hot_key_ok.setVisibility(View.VISIBLE);
            g_textv_hot_key_ok.setVisibility(View.VISIBLE);

        }
        else if ( g_current_focus_view_type == FOCUS_VIEW_CHANNEL) {
            g_imgv_hot_key_red.setVisibility(View.GONE);
            g_textv_hot_key_red.setVisibility(View.GONE);
            //g_imgv_hot_key_green.setVisibility(View.VISIBLE);
            //g_textv_hot_key_green.setVisibility(View.VISIBLE);
            g_imgv_hot_key_yellow.setVisibility(View.GONE);
            g_textv_hot_key_yellow.setVisibility(View.GONE);
            g_imgv_hot_key_blue.setVisibility(View.VISIBLE);
            g_textv_hot_key_blue.setVisibility(View.VISIBLE);
            g_imgv_hot_key_ok.setVisibility(View.VISIBLE);
            g_textv_hot_key_ok.setVisibility(View.VISIBLE);

        }
        else if (g_current_focus_view_type == FOCUS_VIEW_DATE) {
            g_imgv_hot_key_red.setVisibility(View.GONE);
            g_textv_hot_key_red.setVisibility(View.GONE);
            //g_imgv_hot_key_green.setVisibility(View.VISIBLE);
            //g_textv_hot_key_green.setVisibility(View.VISIBLE);
            g_imgv_hot_key_yellow.setVisibility(View.GONE);
            g_textv_hot_key_yellow.setVisibility(View.GONE);
            g_imgv_hot_key_blue.setVisibility(View.VISIBLE);
            g_textv_hot_key_blue.setVisibility(View.VISIBLE);
            g_imgv_hot_key_ok.setVisibility(View.GONE);
            g_textv_hot_key_ok.setVisibility(View.GONE);
        }
        //});
    }

    public int get_last_focus_view_type() {
        return g_current_focus_view_type;
    }

    public void focus_channel() {
        int focus_channel_position = 0;
        Log.d(TAG, "focus_channel: g_curr_channel_num = " + g_curr_channel_num);
        for (int i = 0; i < g_channel_list.size(); i++) {
            //Log.d(TAG, "focus_channel: DisplayNum = " + g_channel_list.get(i).getDisplayNum());
            if (g_channel_list.get(i).getDisplayNum() == g_curr_channel_num)
                focus_channel_position = i;
        }

        g_rcv_channel.move_to_position(focus_channel_position);
    }

    public void focus_1st_channel() {
        g_rcv_channel.move_to_position(0);
    }

    private void set_textv_genre() {
        //Log.d(TAG, "set_textv_genre: g_current_genre_id = " + g_current_genre_id);
        if (g_current_genre_id == ALL_TV_TYPE)
            g_textv_genre.setText(getResources().getStringArray(R.array.channel_category)[ALL_TV_TYPE]);
        else
            g_textv_genre.setText(getResources().getStringArray(R.array.channel_category)[g_current_genre_id-RADIO_FAV2_TYPE]);
    }

    public void set_previous_channel(ProgramInfo latest) {
        gChangeMgr.set_previous_channel(latest);
    }

    public boolean is_adult_blocked() {
        return g_blocked_channel;
    }

    public void set_adult_blocked(boolean blocked) {
        g_blocked_channel = blocked;
        Log.d(TAG,"set_adult_blocked: [adult blocked] " + blocked);
    }

    public List<EPGEvent> get_program_list() {
        return g_program_list;
    }

    public int get_last_focus_program_position() {
        return g_current_program_position;
    }

    public ProgramInfo get_current_channel_info() {
        return g_channel_list.get(g_current_channel_position);
    }

    public EPGEvent get_current_epg_event() {
        int position = get_last_focus_program_position();

        if (g_program_list != null && position < g_program_list.size())
            return g_program_list.get(position);

        return null;
    }

    public void set_current_channel_num(int channelNum) {
        g_curr_channel_num = channelNum;
    }

    private boolean check_time_expired(int type) {
        Date localTime = get_local_time();
        long programTime;
        if (type == REMIND_EVENT)
            programTime = g_program_list.get(g_current_program_position).get_start_time();
        else //RECORD_EVENT
            programTime = g_program_list.get(g_current_program_position).get_end_time();

        /*Log.d(TAG, "check_time_expired: g_last_program_position = " + g_last_program_position);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = format.format(localTime.getTime());
        Log.d(TAG, "check_time_expired: local time = " + str);
        str = format.format(programTime);
        Log.d(TAG, "check_time_expired: program time = " + str);*/
        return localTime.getTime() >= programTime;

    }

    public void remind_program() {
        if (check_time_expired(REMIND_EVENT)) {
            Log.i(TAG, "remind_program: time expired");
            return;
        }

        Log.d(TAG, "remind_program: ");

        if (is_reminder()) {
            ;
        }
        else {
            BookInfo bookInfo = g_epg.new_timer(BookInfo.BOOK_TYPE_CHANGE_CHANNEL, BookInfo.BOOK_CYCLE_ONETIME, g_program_list.get(g_current_program_position), g_channel_list.get(g_current_channel_position));
            set_alarm(bookInfo, bookInfo.getBookCycle());
            gProgramAdapter.update_program(g_current_program_position, bookInfo, true);
            g_textv_hot_key_yellow.setText(getContext().getText(R.string.hint_rcu_cancel_remind));
        }
    }

    public void remind_program_v2() {
        if (gPrimeDtv == null)
            gPrimeDtv = HomeApplication.get_prime_dtv();
        EPGEvent epgEvent = get_current_epg_event();
        long diffTime = epgEvent.get_start_time() - gPrimeDtv.get_current_time();

        if (diffTime < 2 * 60 * 1000) {
            Log.e(TAG, "remind_program_v2: diffTime = " + MiniEPG.ms_to_duration(diffTime) + ", diffTimeMs = " + diffTime);
            Snakebar.show(this, R.string.remind_program_too_close, Snakebar.LENGTH_LONG);
            return;
        }

        new Thread(() -> {
            ProgramInfo channel = get_current_channel_info();

            // handle conflict reminds
            Log.d(TAG, "remind_program_v2: handle conflict reminds");
            BookInfo bookInfo = build_book_info(channel, epgEvent);
            handle_conflict_reminds(bookInfo, channel);

        }, "remind_program_v2").start();
    }

    private BookInfo build_book_info(ProgramInfo channel, EPGEvent event) {
        // start time
        Calendar calCurrent = Calendar.getInstance();
        Date tmpDate = new Date();
        tmpDate.setTime(event.get_start_time());
        calCurrent.setTime(tmpDate);
        calCurrent.add(Calendar.MINUTE, -1);

        int new_book_id = MiniEPG.get_new_book_id();
        int day_of_week = MiniEPG.get_week_day(calCurrent);
        int year = calCurrent.get(Calendar.YEAR);
        int month = calCurrent.get(Calendar.MONTH) + 1;
        int date = calCurrent.get(Calendar.DATE);
        int hour_of_day = calCurrent.get(Calendar.HOUR_OF_DAY);
        int minute = calCurrent.get(Calendar.MINUTE);
        int startTime = hour_of_day * 100 + minute;
        int durationMs = (int) (event.get_end_time() - event.get_start_time());
        int durationHour = durationMs / (1000 * 60 * 60);
        int durationMinute = durationMs % (1000 * 60 * 60) / (1000 * 60);
        int duration = durationHour * 100 + durationMinute;
        //Log.e(TAG, "build_book_info: [name] " + event.get_event_name() + ", [duration] " + durationMs + ", [hour] " + durationHour + ", [minute] " + durationMinute + ", [duration] " + duration);

        BookInfo bookInfo = new BookInfo();
        bookInfo.setBookId(new_book_id);
        bookInfo.setChannelId(channel.getChannelId());
        bookInfo.setGroupType(FavGroup.ALL_TV_TYPE);
        bookInfo.setEventName(event.get_event_name());
        bookInfo.setBookType(BookInfo.BOOK_TYPE_CHANGE_CHANNEL);
        bookInfo.setBookCycle(BookInfo.BOOK_CYCLE_ONETIME);
        bookInfo.setYear(year);
        bookInfo.setMonth(month);
        bookInfo.setDate(date);
        bookInfo.setWeek(day_of_week);
        bookInfo.setSeriesRecKey(event.get_series_key());
        bookInfo.setEpisode(event.get_episode_key());
        bookInfo.setSeries(event.is_series());
        bookInfo.setStartTime(startTime);
        bookInfo.setDuration(duration);
        bookInfo.setDurationMs(durationMs);
        bookInfo.setEnable(0);
        bookInfo.setEpgEventId(event.get_event_id());
        return bookInfo;
    }

    private void handle_conflict_reminds(BookInfo bookInfo, ProgramInfo channel) {
        if (gPrimeDtv == null)
            gPrimeDtv = HomeApplication.get_prime_dtv();
        List<BookInfo> conflicts = gPrimeDtv.book_info_find_conflict_reminds(bookInfo);

        if (HotkeyRemind.has_conflict_reminds(conflicts)) {
            Log.d(TAG, "handle_conflict_reminds: has conflict reminds, replace remind ?");
            EpgActivity activity = get_activity();
            activity.runOnUiThread(() -> {
                String programName = "CH" + channel.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM) + " " + bookInfo.getEventName();
                EventDialog dialog = new EventDialog(activity);
                dialog.set_title_text(String.format(getContext().getString(R.string.remind_hint_conflict), programName));
                dialog.set_confirm_text(R.string.remind_hint_conflict_yes);
                dialog.set_cancel_text(R.string.remind_hint_conflict_no);
                dialog.set_confirm_action(() -> replace_remind(conflicts, bookInfo));
                dialog.show();
            });
            return;
        }

        Log.d(TAG, "handle_conflict_reminds: no conflict reminds, schedule remind");
        schedule_remind(bookInfo);
    }

    private void replace_remind(List<BookInfo> conflictReminds, BookInfo bookInfo) {
        if (gPrimeDtv == null)
            gPrimeDtv = HomeApplication.get_prime_dtv();
        new Thread(() -> {
            Log.d(TAG, "replace_remind: delete conflict book");
            for (BookInfo conflict : conflictReminds)
                gPrimeDtv.book_info_delete(conflict.getBookId());

            Log.d(TAG, "replace_remind: schedule book");
            schedule_remind(bookInfo);
        }).start();
    }

    private void schedule_remind(BookInfo bookInfo) {
        EpgActivity activity = get_activity();

        if (gPrimeDtv == null)
            gPrimeDtv = HomeApplication.get_prime_dtv();

        gPrimeDtv.book_info_add(bookInfo);
        gPrimeDtv.set_alarms(activity, bookInfo.get_Intent());
        gPrimeDtv.save_table(EnTableType.TIMER);

        activity.runOnUiThread(() -> {
            gProgramAdapter.update_program(g_current_program_position, bookInfo, true);
            g_textv_hot_key_yellow.setText(R.string.hint_rcu_cancel_remind);
            activity.on_schedule_remind();
        });
    }

    public void stop_remind_v2() {
        EpgActivity activity = get_activity();

        if (gPrimeDtv == null)
            gPrimeDtv = HomeApplication.get_prime_dtv();

        if (gRemindDialog == null)
            gRemindDialog = new HotkeyRemind(activity);

        new Thread(() -> {
            Log.d(TAG, "stop_remind_v2: show remind cancel dialog");
            gRemindDialog.set_yes_action(() -> stop_remind_confirm(get_current_channel(), get_current_epg_event()));
            gRemindDialog.set_no_action(null);
            gRemindDialog.set_dismiss_action(null);
            gRemindDialog.show_message(activity, R.string.remind_hint_cancel);
        }, "stop_remind_v2").start();
    }

    private void stop_remind_confirm(ProgramInfo channel, EPGEvent epgEvent) {
        EpgActivity activity = get_activity();

        if (gPrimeDtv == null)
            gPrimeDtv = HomeApplication.get_prime_dtv();

        new Thread(() -> {
            String chName = channel.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM) + " " + channel.getDisplayName();
            int bookId = find_book_id(epgEvent, BookInfo.BOOK_TYPE_CHANGE_CHANNEL);
            BookInfo bookInfo = gPrimeDtv.book_info_get(bookId);

            Log.d(TAG, "stop_remind_confirm: [channel] " + chName + ", [epgEvent] " + epgEvent.get_event_name());

            // delete book
            Log.d(TAG, "stop_remind_confirm: delete book");
            gPrimeDtv.book_info_delete(bookId);
            gPrimeDtv.save_table(EnTableType.TIMER);

            // callback: cancel remind
            activity.runOnUiThread(() -> {
                gProgramAdapter.update_program(g_current_program_position, bookInfo, true);
                g_textv_hot_key_yellow.setText(R.string.hint_rcu_remind);
                activity.on_cancel_remind();
            });
        }, "stop_remind_confirm").start();
    }

    private int find_book_id(EPGEvent epgEvent, int bookType) {
        if (gPrimeDtv == null)
            gPrimeDtv = HomeApplication.get_prime_dtv();

        if (null == epgEvent) {
            Log.e(TAG, "find_book_id: epgEvent is null");
            return -1;
        }

        for (BookInfo book : gPrimeDtv.book_info_get_list()) {
            if (book.getBookType() == bookType &&
                book.getEpgEventId() == epgEvent.get_event_id())
                return book.getBookId();
        }

        return -1;
    }

    private boolean is_reminder() {
        ImageView imageView = g_current_program_view.findViewById(R.id.lo_epg_sub_list_item_imgv_remind_icon);
        return imageView.getLayoutParams().width == WRAP_CONTENT;
    }

    public void setup_record() {
        if (check_time_expired(RECORD_EVENT)) {
            Log.i(TAG, "record_program: time expired");
            return;
        }

        if (check_event_is_playing()) {
            if (is_current_channel_recording()) {
                Log.i(TAG, "setup_record: stop record program");
                show_cancel_record_dialog(RECORD_EVENT);
            }
            else {
                Log.i(TAG, "setup_record: start record program");
                start_record_program();
            }
        }
        else {
            if (is_reserve_record()) {
                Log.i(TAG, "setup_record: prepare to cancel book record");
                show_cancel_record_dialog(RESERVE_EVENT);
            }
            else {
                Log.i(TAG, "setup_record: prepare to book record");
                reserve_record_program();
            }
        }
    }

    private void reserve_record_program() {
        if (is_series_event()) {
            Log.d(TAG, "reserve_record_program: book series record");
            show_series_record_dialog(RESERVE_EVENT);
        }
        else {
            Log.d(TAG, "reserve_record_program: book one record");
            book_one_record();
        }
    }

    private void book_one_record() {
        EpgActivity activity = get_activity();
        BookInfo bookInfo = g_epg.get_new_bookinfo(
                BookInfo.BOOK_TYPE_RECORD,
                BookInfo.BOOK_CYCLE_ONETIME,
                g_program_list.get(g_current_program_position),
                g_channel_list.get(g_current_channel_position));

        // book record
        if (gPrimeDtv == null)
            gPrimeDtv = HomeApplication.get_prime_dtv();
        gPrimeDtv.book_info_add(bookInfo);
        gPrimeDtv.set_alarms(getContext(), bookInfo.get_Intent());
        gPrimeDtv.save_table(EnTableType.TIMER);

        // update UI
        update_program(bookInfo, bookInfo.getBookCycle());
        activity.runOnUiThread(activity::on_book_record);
    }

    private void book_series_record() {
        EpgActivity activity = get_activity();
        BookInfo bookInfo = g_epg.get_new_bookinfo(
                BookInfo.BOOK_TYPE_RECORD,
                BookInfo.BOOK_CYCLE_SERIES,
                g_program_list.get(g_current_program_position),
                g_channel_list.get(g_current_channel_position));

        // event name
        String eventName = bookInfo.getEventName();
        String[] stringArray = eventName.split(":");
        if (stringArray.length > 1)
            bookInfo.setEventName(stringArray[0]);

        // book record
        if (gPrimeDtv == null)
            gPrimeDtv = HomeApplication.get_prime_dtv();
        gPrimeDtv.add_series(bookInfo.getChannelId(), bookInfo.getSeriesRecKey());
        gPrimeDtv.book_info_add(bookInfo);
        gPrimeDtv.set_alarms(getContext(), bookInfo.get_Intent());
        gPrimeDtv.save_table(EnTableType.TIMER);

        // update UI
        update_program(bookInfo, bookInfo.getBookCycle());
        activity.runOnUiThread(activity::on_book_record);
    }

    /*private boolean check_reserve_conflict(BookInfo bookInfo) {
        Log.d(TAG, "check_reserve_conflict: ");
        List<BookInfo> conflictBookInfoList = g_epg.bookInfo_find_conflict_books(bookInfo);
        Log.d(TAG, "check_reserve_conflict: conflict bookInfoList size = " + conflictBookInfoList.size());
        if (conflictBookInfoList.size() >= 1) { //Pvcfg.NUM_OF_RECORDING
            //conflictBookInfoList.sort((o1, o2)->
            //        (int) (o1.get_start_time_stamp() - o2.get_start_time_stamp()));

            //for (BookInfo conflictBookInfo:conflictBookInfoList)
            //    Log.d(TAG, "reserve_record_program: conflict " + conflictBookInfo.ToString());
            //g_conflict_index = 0;
            show_event_dialog(bookInfo, conflictBookInfoList);
            return true;
        }
        else {
            Log.d(TAG, "check_reserve_conflict: no conflict");
            if (g_event_dialog_back_action)
                return false;

            start_reserve_record(bookInfo, bookInfo.getBookType());
            g_epg.save_table(EnTableType.TIMER);
            g_event_dialog_back_action = false;
            return false;
        }
    }

    private void start_reserve_record(BookInfo bookInfo, int cycleType) {
        g_epg.new_timer(bookInfo);
        set_alarm(bookInfo, cycleType);
        update_program(bookInfo, bookInfo.getBookCycle());
    }

    private void start_reserve_record(int cycleType) {
        BookInfo bookInfo = g_epg.get_new_bookinfo(BookInfo.BOOK_TYPE_RECORD, cycleType, g_program_list.get(g_current_program_position), g_channel_list.get(g_current_channel_position));

        if (cycleType == BookInfo.BOOK_CYCLE_SERIES) {
            String eventName = bookInfo.getEventName();
            String[] stringArray = eventName.split(":");
            if (stringArray.length > 1)
                bookInfo.setEventName(stringArray[0]);

            g_epg.add_series(bookInfo);
        }

        check_reserve_conflict(bookInfo);
    }

    private void start_record(int cycleType) {

        if (cycleType == BookInfo.BOOK_CYCLE_SERIES) {
            start_series_record();
        }
        else
            start_single_record();
    }*/

    private void set_alarm(BookInfo bookInfo, int cycleType) {
        g_epg.set_alarm(getContext(), bookInfo.get_Intent());
        //if (cycleType == BookInfo.BOOK_CYCLE_ONETIME)
        //    gProgramAdapter.update_program(g_current_program_position, bookInfo, true);
        //else {
        //    gProgramAdapter.update_program(bookInfo, true);
        //    g_rcv_program.move_to_position(g_current_program_position);
        //}
        //g_textv_hot_key_red.setText(getContext().getText(R.string.hint_rcu_cancel_record));
    }

    private void update_program(BookInfo bookInfo, int cycleType) {
        if (cycleType == BookInfo.BOOK_CYCLE_ONETIME) {
            gProgramAdapter.update_program(g_current_program_position, bookInfo, true);
            g_textv_hot_key_red.setText(getContext().getText(R.string.hint_rcu_cancel_record));
        }
        else {
            gProgramAdapter.update_program(bookInfo, true);
            g_rcv_program.move_to_position(g_current_program_position);
        }
    }

    private boolean is_reserve_record() {
        ImageView imageView = g_current_program_view.findViewById(R.id.lo_epg_sub_list_item_imgv_record_icon);
        return imageView.getVisibility() == VISIBLE;
    }

    private boolean is_current_channel_recording() {
        return gChangeMgr.is_channel_recording(g_current_channel_id);
    }

    private boolean check_event_is_playing() {
        Date localTime = get_local_time();
        long programStartTime = g_program_list.get(g_current_program_position).get_start_time();
        long programEndTime = g_program_list.get(g_current_program_position).get_end_time();
        return localTime.getTime() >= programStartTime && localTime.getTime() < programEndTime;
    }

    private void start_record_program() {
        if (gChangeMgr.is_full_recording()) {
            HotkeyRecord hotkeyRecord = new HotkeyRecord(getContext());
            hotkeyRecord.show_dialog(R.string.dvr_other_channel_recording);
            return;
        }

        if (is_series_event()) {
            Log.d(TAG, "start_record_program: show series dialog");
            show_series_record_dialog(RECORD_EVENT);
        }
        else {
            Log.d(TAG, "start_record_program: start one record");
            start_single_record();
        }
    }

    private void start_single_record() {
        EPGEvent currentEvent = g_program_list.get(g_current_program_position);
        int duration = (int) (currentEvent.get_end_time() - get_local_time().getTime());
        Log.d(TAG, "start_single_record: [event] " + currentEvent.get_event_name() + ", [duration] " + MiniEPG.ms_to_duration(duration));

        if (gChangeMgr.pvr_record_start(g_current_channel_id, currentEvent.get_event_id(), duration / 1000, false)) {
            g_textv_hot_key_red.setText(R.string.hint_rcu_cancel_record);
            gProgramAdapter.update_program(g_current_program_position);
        }
        else { // display 'Recording fail'
            HotkeyRecord hotkeyRecord = new HotkeyRecord(getContext());
            hotkeyRecord.show_dialog(R.string.dvr_recording_fail);
        }
    }

    private void start_series_record() {
        Log.d(TAG, "start_series_record:");
        BookInfo bookInfo = g_epg.get_now_record_bookinfo(
                BookInfo.BOOK_TYPE_RECORD,
                BookInfo.BOOK_CYCLE_SERIES,
                g_program_list.get(g_current_program_position),
                g_channel_list.get(g_current_channel_position));
        g_epg.add_series(bookInfo);
        g_epg.new_timer(bookInfo);
        set_alarm(bookInfo, BookInfo.BOOK_CYCLE_SERIES);
        update_program(bookInfo, BookInfo.BOOK_CYCLE_SERIES);
    }

    /*private void cancel_record() {
        Log.d(TAG, "cancel_record:");
        show_cancel_record_dialog(RECORD_EVENT);
    }

    private void cancel_reserve_record() {
        Log.d(TAG, "cancel_reserve_record:");
        show_cancel_record_dialog(RESERVE_EVENT);
    }*/

    private void stop_recording() {
        Log.d(TAG, "stop_recording:");

        if (is_reserved_series_event()) {
            gChangeMgr.pvr_record_stop(g_current_channel_id);
            delete_reserve_series_record();
        }
        else {
            gChangeMgr.pvr_record_stop(g_current_channel_id);
            g_textv_hot_key_red.setText(getContext().getText(R.string.hint_rcu_record));
            gProgramAdapter.update_program(g_current_program_position);
        }
    }

    private void delete_reserve_record() {
        Log.d(TAG, "delete_reserve_record:");
        EpgActivity activity = get_activity();

        if (is_reserved_series_event())
            delete_reserve_series_record();
        else
            delete_reserve_single_record();

        g_textv_hot_key_red.setText(getContext().getText(R.string.hint_rcu_record));
        activity.runOnUiThread(activity::on_cancel_book_record);
    }

    private boolean is_series_event() {
        EPGEvent epgEvent = g_program_list.get(g_current_program_position);
        return epgEvent.get_series_key()[0] != 0;
    }

    private boolean is_reserved_series_event() {
        EPGEvent epgEvent = g_program_list.get(g_current_program_position);
        return gProgramAdapter.is_series(epgEvent);
    }

    private void delete_reserve_single_record() {
        BookInfo deletebookInfo = gProgramAdapter.get_bookInfo_by_event(g_program_list.get(g_current_program_position), BookInfo.BOOK_TYPE_RECORD);
        Log.d(TAG, "delete_reserve_record: " + deletebookInfo.ToString());
        g_epg.cancel_timer(getContext(), deletebookInfo);
        gProgramAdapter.update_program(g_current_program_position, deletebookInfo, false);
    }

    private void delete_reserve_series_record() {
        BookInfo deletebookInfo = gProgramAdapter.get_bookInfo_by_series_key(g_program_list.get(g_current_program_position).get_series_key(), BookInfo.BOOK_TYPE_RECORD);
        Log.d(TAG, "delete_reserve_series_record: " + deletebookInfo.ToString());
        g_epg.delete_series(deletebookInfo);
        g_epg.cancel_timer(getContext(), deletebookInfo);
        gProgramAdapter.update_program(deletebookInfo, false);
        g_rcv_program.move_to_position(g_current_program_position);
    }

    /*private void delete_reserve_record(BookInfo bookInfo) {
        Log.d(TAG, "delete_reserve_record: delete");
    }*/

    private String get_recording_name() {
        return g_program_list.get(g_current_program_position).get_event_name();
    }

    private void show_cancel_record_dialog(int type) {
        Log.d(TAG, "show_cancel_record_dialog: ");
        EventDialog dialog = new EventDialog((AppCompatActivity)getContext());
        dialog.set_title_text(String.format(getContext().getString(R.string.pvr_event_dialog_record_cancel_title), get_recording_name()));
        dialog.set_confirm_text(getContext().getString(R.string.pvr_event_dialog_record_cancel_ok));
        dialog.set_cancel_text(getContext().getString(R.string.pvr_event_dialog_record_cancel_no));

        if (type == RECORD_EVENT)
            dialog.set_confirm_action(this::stop_recording);
        else if (type == RESERVE_EVENT)
            dialog.set_confirm_action(this::delete_reserve_record);

        dialog.show();
    }

    private void show_series_record_dialog(int Type) {
        Log.d(TAG, "show_series_record_dialog: [Type] " + Type);
        EventDialog dialog = new EventDialog(get_activity());
        dialog.set_title_text(getContext().getString(R.string.pvr_event_dialog_series_video_title));
        dialog.set_confirm_text(getContext().getString(R.string.dvr_type_series_record));
        dialog.set_cancel_text(getContext().getString(R.string.dvr_type_single_record));

        if (Type == RESERVE_EVENT) {
            dialog.set_confirm_action(this::book_series_record);
            dialog.set_cancel_action(this::book_one_record);
        }
        else if (Type == RECORD_EVENT) {
            dialog.set_confirm_action(this::start_series_record);
            dialog.set_cancel_action(this::start_single_record);
        }
        dialog.show();
    }

    /*public void show_record_start_banner() {
        if (null == gChangeMgr)
            return;

        ProgramInfo channelInfo = get_channel(gChangeMgr.get_last_record_ch_id());
        if (null == channelInfo)
            return;

        String channelNum = channelInfo.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM);
        Log.d(TAG, "show_record_start_banner: [channel] " + channelNum + " " + channelInfo.getDisplayName());
        Utils.show_notification(getContext(), "CH" + channelNum + " " + getContext().getString(R.string.pvr_message_start_recording_hint));
    }

    public void show_record_stop_banner() {
        if (null == gChangeMgr)
            return;

        ProgramInfo channelInfo = get_channel(gChangeMgr.get_record_stop_ch_id());
        if (null == channelInfo)
            return;

        String channelNum = channelInfo.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM);
        Log.d(TAG, "show_record_stop_banner: [channel] " + channelNum + " " + channelInfo.getDisplayName());
        Utils.show_notification(getContext(), "CH" + channelNum + " " + getContext().getString(R.string.pvr_message_stop_recording_hint));

        g_textv_hot_key_red.setText(R.string.hint_rcu_record);
        gProgramAdapter.update_program(g_current_program_position);
    }

    public void show_record_start_error() {
        Log.d(TAG, "show_record_start_error: ");
        if (null == gChangeMgr)
            return;

        ProgramInfo channel = get_channel(gChangeMgr.get_last_record_ch_id());
        if (null == channel)
            return;

        String channelNum = channel.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM);
        MessageDialog messageDialog = g_epgView.get_pvrMsgDialog();

        if (messageDialog == null)
            messageDialog = new MessageDialog(getContext());

        if (!messageDialog.isShowing()) {
            messageDialog.set_content_message(R.string.error_e606);
            messageDialog.show_dialog();
        }

        Utils.show_notification(getContext(), "CH" + channelNum + " " + getContext().getString(R.string.pvr_message_stop_recording_hint));

        gChangeMgr.remove_record_channel();
    }

    private ProgramInfo get_channel(long channelId) {
        for (ProgramInfo channel : g_channel_list) {
            if (channelId == channel.getChannelId())
                return channel;
        }
        Log.w(TAG, "get_channel: not found channel Id");
        return null;
    }

    private String get_title_from_bookinfo(BookInfo bookInfo) {
        ProgramInfo programInfo = get_channel(bookInfo.getChannelId());
        if (programInfo == null)
            return "NULL";
        return "[" + programInfo.getDisplayNum(3) + "] " + bookInfo.getEventName();
    }

    public void block_date_list() {
        get_activity().runOnUiThread(() -> {
            g_rcv_date.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
            g_rcv_date.setFocusable(false);
        });
    }
    
    public void block_channel_list() {
        get_activity().runOnUiThread(() -> {
            g_rcv_channel.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
            g_rcv_channel.setFocusable(false);
        });
    }*/
    
    public void block_program_list() {
        get_activity().runOnUiThread(() -> {
            g_rcv_program.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
            g_rcv_program.setFocusable(false);
        });
    }

    /*public void unblock_date_list() {
        get_activity().runOnUiThread(() -> {
            g_rcv_date.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
            g_rcv_date.setFocusable(true);
        });
    }
    
    public void unblock_channel_list() {
        get_activity().runOnUiThread(() -> {
            g_rcv_channel.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
            g_rcv_channel.setFocusable(true);
        });
    }*/
    
    public void unblock_program_list() {
        get_activity().runOnUiThread(() -> {
            g_rcv_program.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
            g_rcv_program.setFocusable(true);
        });
    }

    /*private void new_timer_and_cancel_timer(BookInfo newBookInfo ,BookInfo deleteBookInfo) {
        g_epg.new_timer(newBookInfo);
        g_epg.cancel_timer(getContext(), deleteBookInfo);
        set_alarm(newBookInfo, newBookInfo.getBookCycle());
        update_program(newBookInfo, newBookInfo.getBookCycle());

        if (newBookInfo.isSeries()) {
            new Thread(() -> g_epg.save_series());
        }
    }

    private void cancel_timer(BookInfo deleteBookInfo) {
        Log.d(TAG, "cancel_timer: " + deleteBookInfo.ToString());

        g_epg.cancel_timer(getContext(), deleteBookInfo);
        if (deleteBookInfo.isSeries()) {
            g_epg.delete_series(deleteBookInfo);
        }
    }*/

    /*private void show_event_dialog(BookInfo bookInfo, List<BookInfo>conflictBookInfoList) {
        Log.d(TAG, "show_event_dialog: ");
        //if (g_event_dialog == null)
        //    g_event_dialog = new EventDialog((AppCompatActivity)getContext());
        EventDialog eventDialog = new EventDialog((AppCompatActivity)getContext());

        //Log.d(TAG, "show_event_dialog: g_conflict_index = " + g_conflict_index);
        //Log.d(TAG, "show_event_dialog: " + get_title_from_bookinfo(conflictBookInfoList.get(0)));
        //Log.d(TAG, "show_event_dialog: " + get_title_from_bookinfo(g_conflict_bookInfoList.get(g_conflict_index+1)));
        eventDialog.set_title_text(String.format(getContext().getString(R.string.reserve_timer_conflict)));
        eventDialog.set_confirm_text(get_title_from_bookinfo(bookInfo));
        eventDialog.set_cancel_text(get_title_from_bookinfo(conflictBookInfoList.get(0)));
        eventDialog.set_confirm_action(() -> cancel_timer(conflictBookInfoList.get(0)));
        eventDialog.set_cancel_action(() -> cancel_timer(bookInfo));
        eventDialog.set_back_action(()-> {
            if (bookInfo.isSeries()) {
                g_epg.delete_series(bookInfo);
                new Thread(() -> g_epg.save_series());
            }
            g_event_dialog_back_action = true;
        });
        eventDialog.set_dismiss_action(() -> {
            boolean has_conflict = check_reserve_conflict(bookInfo);
            Log.d(TAG, "show_event_dialog: [has conflict] " + has_conflict);
        });

        eventDialog.show();
        //Log.d(TAG, "show_event_dialog: show");
    }*/

    public void set_default_channel() {
        ProgramInfo programInfo = gChangeMgr.get_cur_channel();
        if(programInfo != null) {
            if ( g_current_genre_id == ALL_TV_TYPE
                    || g_epg.check_genre_group(programInfo, g_current_genre_id)) {
                set_textv_genre();
                g_curr_channel_num = programInfo.getDisplayNum();
                focus_channel();
                g_current_channel_id = programInfo.getChannelId();
                g_prev_channel_id = g_current_channel_id;
            }
            else {
                g_current_genre_id = ALL_TV_TYPE;
                g_epg.gpos_info_update_by_key_string(GPOS_CUR_GROUP_TYPE, g_current_genre_id);
                set_textv_genre();
                g_curr_channel_num = programInfo.getDisplayNum();
                g_current_channel_id = programInfo.getChannelId();
                g_prev_channel_id = g_current_channel_id;
                update_channel_list();
            }
        }
    }

    public void set_current_program_position(int position) {
        g_current_program_position = position;
    }
}
