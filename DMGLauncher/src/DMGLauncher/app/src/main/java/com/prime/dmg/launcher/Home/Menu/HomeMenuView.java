package com.prime.dmg.launcher.Home.Menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.R;
import com.prime.dtv.config.Pvcfg;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class HomeMenuView extends RelativeLayout {
    private final String TAG = "HomeMenuView";

    public String SEARCH;
    public String EPG;
    public String RECORD;
    public String WEATHER;
    public String RANKING;
    public String MESSAGE;
    public String MEMBER;
    public String MUSIC;
    public String NOTIFICATIONS;
    public String SETTINGS;

    public RecyclerView g_rcv_home_menu_item;
    public HomeMenuAdapter g_adpt_home_menu;
    private List<HomeMenuItem> g_homeMenuItemList;

    private boolean g_bool_is_fade_in = false;
    private int g_int_last_focus_item = 0;
    private RelativeLayout.LayoutParams g_layoutparams;
    WeakReference<AppCompatActivity> g_ref;
    Handler g_handler;
    //private int HOME_MENU_WIDTH_EXPAND = 360;
    //private int HOME_MENU_WIDTH_EXPAND = 360;

    public HomeMenuView(Context context) {
        super(context);
        init_home_menu();
    }

    public HomeMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init_home_menu();
    }

    public HomeMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init_home_menu();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        View lastView;

        if (!gainFocus)
            return;
        if (FOCUS_LEFT != direction)
            return;
        if (null == g_rcv_home_menu_item)
            return;

        unlock_recycler_view();
        lastView = get_last_view();
        if (null == lastView)
            return;

        open_menu();
        g_handler.post(lastView::requestFocus);
    }
    
    @SuppressLint("ResourceType")
    private void init_home_menu() {
        Log.d(TAG, "init Home Menu");
        inflate(getContext(), R.layout.lo_home_menu_view, this);
        g_rcv_home_menu_item = findViewById(R.id.lo_menu_rcv_home_menu_item);
        g_rcv_home_menu_item.setLayoutManager(new LinearLayoutManager(getContext()));
        g_rcv_home_menu_item.addItemDecoration(new HomeMenuAdapter.ItemDecoration(getContext()));
        g_rcv_home_menu_item.setFocusable(false);
        g_rcv_home_menu_item.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        g_homeMenuItemList = init_home_menu_data();
        g_adpt_home_menu = new HomeMenuAdapter(g_homeMenuItemList, this);
        g_rcv_home_menu_item.setAdapter(g_adpt_home_menu);
        g_handler = new Handler(Looper.getMainLooper());
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public int get_last_index() {
        return g_int_last_focus_item;
    }

    public View get_last_view() {
        return g_rcv_home_menu_item.getChildAt(g_int_last_focus_item);
    }

    public void set_home_activity(HomeActivity activity) {
        g_ref = new WeakReference<>(activity);
    }

    public void set_last_index(int lastIndex) {
        g_int_last_focus_item = lastIndex;
    }

    public void lock_recycler_view() {
        g_rcv_home_menu_item.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
    }

    public void unlock_recycler_view() {
        g_rcv_home_menu_item.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private List<HomeMenuItem> init_home_menu_data() {
        List<HomeMenuItem> tmpList = new ArrayList<>();
        Resources res = this.getResources();
        init_tag(res);

        tmpList.add(new HomeMenuItem(res.getDrawable(R.drawable.icon_home_menu_voice), res.getString(R.string.home_menu_search), ""));
        tmpList.add(new HomeMenuItem(res.getDrawable(R.drawable.icon_home_menu_epg), res.getString(R.string.home_menu_epg), ""));
        if (Pvcfg.getPVR_PJ())
            tmpList.add(new HomeMenuItem(res.getDrawable(R.drawable.icon_home_menu_rec), res.getString(R.string.home_menu_record), ""));
        tmpList.add(new HomeMenuItem(res.getDrawable(R.drawable.icon_home_menu_weather), res.getString(R.string.home_menu_weather), ""));
        if (Pvcfg.RANKING)
            tmpList.add(new HomeMenuItem(res.getDrawable(R.drawable.icon_home_menu_ranking), res.getString(R.string.home_menu_ranking), ""));
        tmpList.add(new HomeMenuItem(res.getDrawable(R.drawable.icon_home_menu_mail), res.getString(R.string.home_menu_mail), ""));
        tmpList.add(new HomeMenuItem(res.getDrawable(R.drawable.icon_home_menu_member), res.getString(R.string.home_menu_account), ""));
        tmpList.add(new HomeMenuItem(res.getDrawable(R.drawable.icon_home_menu_music), res.getString(R.string.home_menu_music), ""));
        tmpList.add(new HomeMenuItem(res.getDrawable(R.drawable.icon_home_menu_notification), res.getString(R.string.home_menu_notification), ""));
        tmpList.add(new HomeMenuItem(res.getDrawable(R.drawable.icon_home_menu_settings), res.getString(R.string.home_menu_settings), ""));

        return tmpList;
    }

    private void init_tag(Resources res) {
        SEARCH = res.getString(R.string.home_menu_search);
        EPG = res.getString(R.string.home_menu_epg);
        RECORD = res.getString(R.string.home_menu_record);
        WEATHER = res.getString(R.string.home_menu_weather);
        RANKING =  res.getString(R.string.home_menu_ranking);
        MESSAGE = res.getString(R.string.home_menu_mail);
        MEMBER = res.getString(R.string.home_menu_account);
        MUSIC = res.getString(R.string.home_menu_music);
        NOTIFICATIONS = res.getString(R.string.home_menu_notification);
        SETTINGS = res.getString(R.string.home_menu_settings);
    }

    private void fade_view(final boolean animation) {
        if (g_bool_is_fade_in == animation) {
            return;
        }
        float f = 0.0f;
        float f2 = 1.0f;
        if (!animation) {
            f2 = 0.0f;
            f = 1.0f;
        }
        g_bool_is_fade_in = animation;
        AlphaAnimation alphaAnimation = new AlphaAnimation(f, f2);
        alphaAnimation.setDuration(0);
        alphaAnimation.setFillAfter(true);

        if (animation) {
            g_rcv_home_menu_item.setBackgroundColor(getContext().getColor(R.color.lo_menu));
            //this.g_rcv_home_menu_item.getChildAt(this.g_int_last_focus_item).requestFocus();
            g_layoutparams = new RelativeLayout.LayoutParams((int)getContext().getResources().getDimension(R.dimen.lo_menu_rltvl_home_menu_width), -1);
        } else {
            g_rcv_home_menu_item.setBackgroundColor(getContext().getColor(R.color.trans));
            g_layoutparams = new RelativeLayout.LayoutParams((int) getContext().getResources().getDimension(R.dimen.lo_menu_rltvl_home_menu_hide_width), -1);
        }
        setLayoutParams(g_layoutparams);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        ConstraintLayout root         = (ConstraintLayout) getParent();
        FrameLayout      menuCollapse = root.findViewById(R.id.lo_menu_frml);
        View             menuDivider  = root.findViewById(R.id.lo_menu_divider);

        menuCollapse.setVisibility(visibility);
        menuDivider.setVisibility(visibility);
    }

    public void update_notification_count(int count, boolean unreadFlag) {
        g_adpt_home_menu.update_notification_count(count, unreadFlag);
    }

    public void update_notification_count(int count) {
        g_adpt_home_menu.update_notification_count(count);
    }

    public void open_menu() {
        fade_view(true);
    }

    public void close_menu() {
        int position = HomeMenuAdapter.LAST_FOCUS_POSITION;
        set_last_index(position);
        lock_recycler_view();
        fade_view(false);
        cancel_highlight(position);
    }

    public void cancel_highlight(int position) {
        if (g_rcv_home_menu_item == null)
            return;

        HomeMenuAdapter.MyViewHolder holder = (HomeMenuAdapter.MyViewHolder) g_rcv_home_menu_item.findViewHolderForLayoutPosition(position);
        if (null == holder)
            return;

        // cancel highlight
        g_adpt_home_menu.setup_highlight(holder, position, false);
    }

    public void update_menu() {
        g_adpt_home_menu.update_menu(init_home_menu_data());
    }
}
