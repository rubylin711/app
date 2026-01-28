package com.prime.dmg.launcher.Home.LiveTV.QuickTune;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.RelativeLayout.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.prime.dmg.launcher.Home.LiveTV.LiveTvManager;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.BaseDialog;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.FavInfo;
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.TVMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** @noinspection CommentedOutCode*/
@SuppressLint("CommitPrefEdits")
public class QuickTuneDialog extends BaseDialog {

    String TAG = getClass().getSimpleName();

    public static final int MAX_LENGTH_OF_SERVICE_ID    = MiniEPG.MAX_LENGTH_OF_SERVICE_ID;
    public static final int MAX_LENGTH_OF_CHANNEL_NUM   = MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM;
    public static final int MAX_COUNT_OF_QUICK_ITEM     = 10;

    // View TAG
    public static final String TAG_HINT_ROOT = "hintRoot";
    public static final String TAG_CONTAINER = "container";
    public static final String TAG_MOVE_TEXT = "moveText";
    public static final String TAG_MOVE_ICON = "moveIcon";
    public static final String TAG_REMOVE_TEXT = "removeText";
    public static final String TAG_REMOVE_ICON = "removeIcon";
    public static final String TAG_QUICK_ITEM = "quickItem";
    public static final String TAG_SELECT_ICON = "selectIcon";

    // SharedPreferences KEY
    public static final String KEY_QUICK_CHANNEL_NUM = "QUICK_CHANNEL_NUM";
    public static final String KEY_QUICK_SERVICE_ID = "QUICK_SERVICE_ID";

    // introduction time
    public int DELAY_MS_TO_DISMISS = 5000;

    // Title
    public final String QUICK_TUNE;
    public final String FAVORITE;

    // flag
    boolean g_isClickQuickItem;

    // data
    int g_previousQuickIndex;
    int g_currentQuickIndex;
    String g_enterNum;

    // handle
    LiveTvManager g_LiveTvManager;
    Handler g_handler;

    public QuickTuneDialog(@NonNull LiveTvManager liveTvMgr) {
        super(liveTvMgr.get(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_LiveTvManager = liveTvMgr;
        g_handler = new Handler(Looper.getMainLooper());
        g_enterNum = "";
        QUICK_TUNE = get().getString(R.string.title_quick_tune);
        FAVORITE = get().getString(R.string.favorite_title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.color.trans);
            //getWindow().setWindowAnimations(R.style.Theme_DMGLauncher_DialogAnimation);
        }
        setContentView(R.layout.dialog_quick_tune);

        // add view
        add_hint_root();
        add_all_quick_view();

        // hide
        hide_add_window();
        hide_favorite_window();
        hide_arrow_left();

        // callback
        on_keydown_add_window();
        //on_keydown_favorite();

        // delay
        set_delay_dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        g_currentQuickIndex = 5;
        FavListView favListView = findViewById(R.id.lo_quick_tune_favorite_channels);
        favListView.setVisibility(GONE);
        show_quick_window(null);
        set_favorite_hint(R.string.hint_rcu_move);
    }

    @Override
    protected void onStop() {
        super.onStop();
        g_handler.removeCallbacksAndMessages(null);
        save_favorite_channel();
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        //Log.e(TAG, "onKeyDown: keyCode = " + keyCode + ", " + getCurrentFocus());

        if (enter_channel_num(keyCode))
            return true;

        if (move_quick_item(keyCode))
            return true;

        if (delete_quick_item(keyCode))
            return true;

        if (setup_favorite_window(keyCode))
            return true;

        if (need_block_keycode(keyCode))
            return true;
        Log.w(TAG, "onKeyDown: not match [keycode] " + keyCode);

        return super.onKeyDown(keyCode, event);
    }

    void on_focus_quick_item(RelativeLayout quickItem, int index) {
        quickItem.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (is_move_quick_item()) {
                    g_currentQuickIndex = index;
                    switch_quick_item();
                    return;
                }

                // fix index be 1 after open [add window]
                if (!g_isClickQuickItem) {
                    Log.d(TAG, "on_focus_quick_item: [index] " + index);
                    g_currentQuickIndex = index;
                }
                g_isClickQuickItem = false;

                if (has_quick_channel(quickItem)) {
                    add_hint_remove();
                    add_hint_move();
                }
                else {
                    hide_hint_remove();
                    hide_hint_move();
                }

                set_delay_dismiss();
            }
        });
    }

    void on_click_quick_item(RelativeLayout quickItem, int index) {

        quickItem.setOnClickListener(v -> {
            if (is_move_quick_item()) {
                Log.d(TAG, "on_click_quick_item: finish switch");
                switch_quick_item_finish();
                return;
            }
            g_isClickQuickItem = true;
            if (has_quick_channel(quickItem)) {
                Log.d(TAG, "on_click_quick_item: change to channel " + get_current_display_num());
                LiveTvManager.change_channel(g_LiveTvManager, get_current_quick_channel(), true);
                dismiss();
            }
            else {
                Log.d(TAG, "on_click_quick_item: show add window, [index] " + index);
                show_add_window(index);
            }
        });
    }

    void on_keydown_add_window() {
        AddListView addList = findViewById(R.id.lo_quick_tune_add_list);
        addList.setOnKeyListener((v, keyCode, event) -> {

            if (KeyEvent.ACTION_UP == event.getAction())
                return false;

            if (KeyEvent.KEYCODE_BACK == keyCode) {
                if (has_enter_channel_num()) {
                    Log.w(TAG, "on_keydown_add_window: has enter channel number");
                    return false;
                }
                show_previous_window(null);
                return true;
            }
            if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode ||
                KeyEvent.KEYCODE_DPAD_UP == keyCode) {
                return addList.get_item_count() == 0;
            }
            return false;
        });
    }

    /*void on_keydown_favorite() {
        FavListView favListView = findViewById(R.id.lo_quick_tune_favorite_channels);
        favListView.setOnKeyListener((v, keyCode, event) -> {

            if (KeyEvent.ACTION_UP == event.getAction())
                return false;

            if (KeyEvent.KEYCODE_PROG_YELLOW == keyCode||133==keyCode) {
                Log.d(TAG, "on_keydown_favorite: show add window");
                show_add_window();
            }

            return false;
        });
    }*/
    
    void add_all_quick_view() {
        Log.d(TAG, "add_all_quick_view: ");
        RelativeLayout quickRoot = findViewById(R.id.lo_quick_tune_quick_channels);
        for (int i = 1; i < MAX_COUNT_OF_QUICK_ITEM; i++) {
            add_one_quick_view(quickRoot, i);
        }
    }

    void add_one_quick_view(RelativeLayout quickRoot, int index) {
        RelativeLayout quickItem;
        RelativeLayout.LayoutParams layoutParams;
        int width, height;

        quickItem = new RelativeLayout(quickRoot.getContext());
        quickItem.setTag(TAG_QUICK_ITEM + index);
        quickItem.setFocusable(true);
        quickItem.setBackgroundResource(R.drawable.quick_tune_bg_selector);

        // layout size
        width = height = get().getResources().getDimensionPixelSize(R.dimen.quick_tune_item_size);
        layoutParams = new RelativeLayout.LayoutParams(width, height);

        // layout align
        if (index == 1 || index == 4 || index == 7) layoutParams.addRule(ALIGN_PARENT_LEFT, TRUE);
        if (index == 2 || index == 5 || index == 8) layoutParams.addRule(CENTER_HORIZONTAL, TRUE);
        if (index == 3 || index == 6 || index == 9) layoutParams.addRule(ALIGN_PARENT_RIGHT, TRUE);
        if (index == 1 || index == 2 || index == 3) layoutParams.addRule(ALIGN_PARENT_TOP, TRUE);
        if (index == 4 || index == 5 || index == 6) layoutParams.addRule(CENTER_VERTICAL, TRUE);
        if (index == 7 || index == 8 || index == 9) layoutParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        quickItem.setLayoutParams(layoutParams);

        // layout format
        View.inflate(getContext(), R.layout.item_quick_tune, quickItem);
        quickRoot.addView(quickItem);
        show_quick_item(index);

        // callback
        on_focus_quick_item(quickItem, index);
        on_click_quick_item(quickItem, index);

        // focus center item
        if (index == 5)
            quickItem.requestFocus();
    }

    void add_hint_root() {
        RelativeLayout quickWindow;
        RelativeLayout hintRoot;
        RelativeLayout.LayoutParams layoutParams;

        quickWindow = findViewById(R.id.lo_quick_tune_window);
        hintRoot = new RelativeLayout(quickWindow.getContext());
        hintRoot.setTag(TAG_HINT_ROOT);
        hintRoot.setFocusable(true);
        hintRoot.setBackgroundResource(R.drawable.hint_shadow);

        layoutParams = new RelativeLayout.LayoutParams(MATCH_PARENT, get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_root_height));
        layoutParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        hintRoot.setLayoutParams(layoutParams);
        quickWindow.addView(hintRoot);

        add_hint_container(hintRoot);
    }

    void add_hint_container(RelativeLayout hintRoot) {
        LinearLayout container;
        LinearLayout.LayoutParams params;
        RelativeLayout.LayoutParams relativeParams;
        int height, marginEnd, marginBottom;

        height = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_content_height);
        marginEnd = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_content_margin_end);
        marginBottom = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_content_margin_bottom);

        params = new LinearLayout.LayoutParams(WRAP_CONTENT, height);
        container = new LinearLayout(hintRoot.getContext());
        container.setTag(TAG_CONTAINER);
        container.setLayoutParams(params);
        container.setOrientation(HORIZONTAL);

        relativeParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, height);
        relativeParams.rightMargin = marginEnd;
        relativeParams.bottomMargin = marginBottom;
        relativeParams.addRule(ALIGN_PARENT_BOTTOM);
        relativeParams.addRule(ALIGN_PARENT_END);
        hintRoot.addView(container, relativeParams);

        add_hint_ok(container);
        add_hint_select(container);
    }

    void add_hint_ok(LinearLayout container) {
        LinearLayout.LayoutParams iconParams;
        int width = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_icon_width);
        int height = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_icon_height);
        int iconMargin = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_icon_select_margin_end);
        int textSize = (int) (get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_content_text_size) / get().getResources().getDisplayMetrics().density);

        TextView okText = new TextView(get());
        okText.setText(R.string.hint_rcu_ok_confirm);
        okText.setTextSize(textSize);
        okText.setTextColor(Color.WHITE);
        container.addView(okText, 0, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));


        ImageView okIcon = new ImageView(get());
        okIcon.setImageResource(R.drawable.hint_ok);
        iconParams = new LinearLayout.LayoutParams(width, height);
        iconParams.rightMargin = iconMargin;
        container.addView(okIcon, 0, iconParams);
    }

    void add_hint_select(LinearLayout container) {
        LinearLayout.LayoutParams txtParams, iconParams;
        int txtSize = (int) (get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_content_text_size) / get().getResources().getDisplayMetrics().density);
        int txtMargin = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_content_text_margin_end);
        int iconMargin = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_icon_select_margin_end);
        int iconWidth = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_icon_select_width);

        TextView selectText = new TextView(get());
        selectText.setText(R.string.hint_rcu_dpad_select);
        selectText.setTextSize(txtSize);
        txtParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        txtParams.rightMargin = txtMargin;
        container.addView(selectText,0, txtParams);

        ImageView selectIcon = new ImageView(get());
        selectIcon.setTag(TAG_SELECT_ICON);
        selectIcon.setImageResource(R.drawable.hint_4way);
        selectIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iconParams = new LinearLayout.LayoutParams(iconWidth, WRAP_CONTENT);
        iconParams.rightMargin = iconMargin;
        container.addView(selectIcon, 0, iconParams);
    }

    void add_hint_move() {
        RelativeLayout quickWindow = findViewById(R.id.lo_quick_tune_window);
        LinearLayout container = quickWindow.findViewWithTag(TAG_CONTAINER);
        TextView moveText = quickWindow.findViewWithTag(TAG_MOVE_TEXT);
        ImageView moveIcon = quickWindow.findViewWithTag(TAG_MOVE_ICON);

        if (null == container) {
            Log.e(TAG, "add_hint_move: null container");
            return;
        }

        if (moveText != null) {
            moveText.setVisibility(VISIBLE);
            moveIcon.setVisibility(VISIBLE);
            return;
        }

        LinearLayout.LayoutParams txtParams, iconParams;
        int txtSize = (int) (get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_content_text_size) / get().getResources().getDisplayMetrics().density);
        int txtMargin = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_content_text_margin_end);
        int iconMargin = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_icon_select_margin_end);
        int iconWidth = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_icon_width);
        int iconHeight = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_icon_height);

        // move text
        moveText = new TextView(get());
        moveText.setTag(TAG_MOVE_TEXT);
        moveText.setText(R.string.hint_rcu_move);
        moveText.setTextSize(txtSize);
        txtParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        txtParams.rightMargin = txtMargin;
        container.addView(moveText,2, txtParams);
        Log.d(TAG, "add_hint_move: [tag] " + TAG_MOVE_TEXT);

        // move icon
        moveIcon = new ImageView(get());
        moveIcon.setTag(TAG_MOVE_ICON);
        moveIcon.setImageResource(R.drawable.hint_yellow);
        moveIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iconParams = new LinearLayout.LayoutParams(iconWidth, iconHeight);
        iconParams.rightMargin = iconMargin;
        container.addView(moveIcon, 2, iconParams);
        Log.d(TAG, "add_hint_move: [tag] " + TAG_MOVE_ICON);
    }

    void add_hint_remove() {
        RelativeLayout quickWindow = findViewById(R.id.lo_quick_tune_window);
        LinearLayout container = quickWindow.findViewWithTag(TAG_CONTAINER);
        TextView removeText = quickWindow.findViewWithTag(TAG_REMOVE_TEXT);
        ImageView removeIcon = quickWindow.findViewWithTag(TAG_REMOVE_ICON);

        if (null == container) {
            Log.e(TAG, "add_hint_remove: null container");
            return;
        }

        if (removeText != null) {
            removeText.setVisibility(VISIBLE);
            removeIcon.setVisibility(VISIBLE);
            return;
        }

        LinearLayout.LayoutParams txtParams, iconParams;
        int txtSize = (int) (get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_content_text_size) / get().getResources().getDisplayMetrics().density);
        int txtMargin = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_content_text_margin_end);
        int iconMargin = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_icon_select_margin_end);
        int iconWidth = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_icon_width);
        int iconHeight = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_icon_height);

        // remove text
        removeText = new TextView(get());
        removeText.setTag(TAG_REMOVE_TEXT);
        removeText.setText(R.string.hint_rcu_delete);
        removeText.setTextSize(txtSize);
        txtParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        txtParams.rightMargin = txtMargin;
        container.addView(removeText,2, txtParams);
        Log.d(TAG, "add_hint_remove: [tag] " + TAG_REMOVE_TEXT);

        // remove icon
        removeIcon = new ImageView(get());
        removeIcon.setTag(TAG_REMOVE_ICON);
        removeIcon.setImageResource(R.drawable.hint_blue);
        removeIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iconParams = new LinearLayout.LayoutParams(iconWidth, iconHeight);
        iconParams.rightMargin = iconMargin;
        container.addView(removeIcon, 2, iconParams);
        Log.d(TAG, "add_hint_remove: [tag] " + TAG_REMOVE_ICON);
    }

    boolean has_quick_channel(View quickItem) {
        SharedPreferences preferences;
        String quickTag, channelNum, serviceId;
        int quickIndex;

        if (null == quickItem) {
            Log.w(TAG, "has_quick_channel: null quick channel");
            return false;
        }

        // get quick index
        quickTag = (String) quickItem.getTag();
        quickIndex = Integer.parseInt(quickTag.replace(TAG_QUICK_ITEM, ""));

        // get channel num, service id by [quick index]
        preferences = get().getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        channelNum = preferences.getString(KEY_QUICK_CHANNEL_NUM + quickIndex, null);
        serviceId = preferences.getString(KEY_QUICK_SERVICE_ID + quickIndex, null);
        //Log.d(TAG, "has_quick_channel: [channel num] " + channelNum + ", [service id] " + serviceId);

        return channelNum != null;
    }

    boolean has_enter_channel_num() {
        return g_enterNum.length() > 0;
    }

    boolean is_quick_window() {
        TextView title = findViewById(R.id.lo_quick_tune_title_text);
        return title.getText().equals(get().getString(R.string.title_quick_tune));
    }

    boolean is_add_window() {
        LinearLayout addWindow = findViewById(R.id.lo_quick_tune_add_layer);
        return VISIBLE == addWindow.getVisibility();
    }

    boolean is_favorite() {
        TextView title = findViewById(R.id.lo_quick_tune_title_text);
        String currentTitle = (String) title.getText();
        return is_favorite_1() ||
               is_favorite_2() ||
               is_favorite_3() ||
               is_favorite_4() ||
               is_favorite_5() ||
               currentTitle.contains(FAVORITE);
    }

    boolean is_favorite_1() {
        TextView title = findViewById(R.id.lo_quick_tune_title_text);
        return title.getText().equals(get().getString(R.string.favorite_title_1));
    }

    boolean is_favorite_2() {
        TextView title = findViewById(R.id.lo_quick_tune_title_text);
        return title.getText().equals(get().getString(R.string.favorite_title_2));
    }

    boolean is_favorite_3() {
        TextView title = findViewById(R.id.lo_quick_tune_title_text);
        return title.getText().equals(get().getString(R.string.favorite_title_3));
    }

    boolean is_favorite_4() {
        TextView title = findViewById(R.id.lo_quick_tune_title_text);
        return title.getText().equals(get().getString(R.string.favorite_title_4));
    }

    boolean is_favorite_5() {
        TextView title = findViewById(R.id.lo_quick_tune_title_text);
        return title.getText().equals(get().getString(R.string.favorite_title_5));
    }

    boolean is_keycode_num(int keyCode) {
        return KeyEvent.KEYCODE_0 == keyCode ||
               KeyEvent.KEYCODE_1 == keyCode ||
               KeyEvent.KEYCODE_2 == keyCode ||
               KeyEvent.KEYCODE_3 == keyCode ||
               KeyEvent.KEYCODE_4 == keyCode ||
               KeyEvent.KEYCODE_5 == keyCode ||
               KeyEvent.KEYCODE_6 == keyCode ||
               KeyEvent.KEYCODE_7 == keyCode ||
               KeyEvent.KEYCODE_8 == keyCode ||
               KeyEvent.KEYCODE_9 == keyCode;
    }

    boolean is_move_quick_item() {
        TextView title = findViewById(R.id.lo_quick_tune_title_text);
        return title.getText().equals(get().getString(R.string.title_move_quick_tune));
    }

    HomeActivity get() {
        return g_LiveTvManager.get();
    }

    List<ProgramInfo> get_filtered_channels(String filterNum) {
        List<ProgramInfo> allChannels = g_LiveTvManager.get_channels();
        List<ProgramInfo> newChannels = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();

        // remove 0 in filter number
        for (int i = 0; i < filterNum.length(); i++) {
            if (filterNum.charAt(i) == '0' && stringBuilder.length() == 0)
                continue;
            if (filterNum.charAt(i) != '0' || stringBuilder.length() != 0)
                stringBuilder.append(filterNum.charAt(i));
        }
        filterNum = stringBuilder.toString();

        // get new channels
        for (ProgramInfo channel : allChannels) {
            if (filterNum.length() == 0)
                break;
            if (channel.getDisplayNum(3).contains(filterNum))
                newChannels.add(channel);
        }
        return newChannels;
    }

    View get_current_quick_item() {
        RelativeLayout quickWindow = findViewById(R.id.lo_quick_tune_window);
        return quickWindow.findViewWithTag(TAG_QUICK_ITEM + g_currentQuickIndex);
    }

    int get_current_display_num() {
        SharedPreferences preferences;
        String currentChNum;
        int currentIndex;

        currentIndex = g_currentQuickIndex;
        preferences = get().getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        currentChNum = preferences.getString(KEY_QUICK_CHANNEL_NUM + currentIndex, null);

        if (null == currentChNum)
            return 0;

        return Integer.parseInt(currentChNum);
    }

    ProgramInfo get_current_quick_channel() {
        ProgramInfo quickChannel = null;

        for (ProgramInfo channel : get().g_liveTvMgr.get_channels()) {
            if (channel.getDisplayNum() == get_current_display_num()) {
                quickChannel = channel;
                break;
            }
        }
        return quickChannel;
    }

    int get_group_type() {
        if (is_favorite_1())
            return FavGroup.TV_FAV1_TYPE;
        if (is_favorite_2())
            return FavGroup.TV_FAV2_TYPE;
        if (is_favorite_3())
            return FavGroup.TV_FAV3_TYPE;
        if (is_favorite_4())
            return FavGroup.TV_FAV4_TYPE;
        if (is_favorite_5())
            return FavGroup.TV_FAV5_TYPE;
        return -1;
    }

    void set_quick_icon(ImageView chIconView, ProgramInfo chInfo) {
        String iconUrl, serviceId;
        int iconResId, iconWidth, iconHeight;

        serviceId   = chInfo.getServiceId(MAX_LENGTH_OF_SERVICE_ID);
        iconUrl     = LiveTvManager.get_channel_icon_url(get(), serviceId);
        iconResId   = LiveTvManager.get_channel_icon_res_id(get(), serviceId);
        iconWidth   = get().getResources().getDimensionPixelSize(R.dimen.mini_epg_ch_icon_width);
        iconHeight  = get().getResources().getDimensionPixelSize(R.dimen.mini_epg_ch_icon_height);

        if (!get().isFinishing() && !get().isDestroyed())
            Glide.with(get())
                    .load(iconUrl)
                    .error(iconResId)
                    .placeholder(iconResId)
                    .override(iconWidth, iconHeight)
                    //.skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(chIconView);
    }

    void set_quick_icon(ImageView chIconView, String serviceId) {
        String iconUrl;
        int iconResId, iconWidth, iconHeight;

        iconUrl     = LiveTvManager.get_channel_icon_url(get(), serviceId);
        iconResId   = LiveTvManager.get_channel_icon_res_id(get(), serviceId);
        iconWidth   = get().getResources().getDimensionPixelSize(R.dimen.mini_epg_ch_icon_width);
        iconHeight  = get().getResources().getDimensionPixelSize(R.dimen.mini_epg_ch_icon_height);

        if (!get().isFinishing() && !get().isDestroyed())
            Glide.with(get())
                    .load(iconUrl)
                    .error(iconResId)
                    .placeholder(iconResId)
                    .override(iconWidth, iconHeight)
                    //.skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(chIconView);
    }

    void set_favorite_title(int keyCode) {
        TextView title = findViewById(R.id.lo_quick_tune_title_text);
        if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode) {
            if (is_quick_window())      title.setText(R.string.favorite_title_1);
            else if (is_favorite_1())   title.setText(R.string.favorite_title_2);
            else if (is_favorite_2())   title.setText(R.string.favorite_title_3);
            else if (is_favorite_3())   title.setText(R.string.favorite_title_4);
            else if (is_favorite_4())   title.setText(R.string.favorite_title_5);
        }
        if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode) {
            if (is_favorite_1())        title.setText(R.string.title_quick_tune);
            else if (is_favorite_2())   title.setText(R.string.favorite_title_1);
            else if (is_favorite_3())   title.setText(R.string.favorite_title_2);
            else if (is_favorite_4())   title.setText(R.string.favorite_title_3);
            else if (is_favorite_5())   title.setText(R.string.favorite_title_4);
        }
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            String strAddChannel = get().getString(R.string.favorite_add_hint);
            String strFavoriteN = title.getText().toString();
            title.setText(strFavoriteN.replace(strAddChannel, ""));
        }
    }

    void set_favorite_arrow() {
        if (is_favorite()) {
            show_arrow_right();
            show_arrow_left();
        }
        if (is_quick_window())  hide_arrow_left();
        if (is_favorite_1())    show_arrow_left();
        if (is_favorite_4())    show_arrow_right();
        if (is_favorite_5())    hide_arrow_right();
    }

    void set_favorite_hint(int resId) {
        RelativeLayout quickWindow = findViewById(R.id.lo_quick_tune_window);
        TextView moveText = quickWindow.findViewWithTag(TAG_MOVE_TEXT);
        if (moveText != null)
            moveText.setText(resId);
    }

    public void set_delay_dismiss() {
        set_delay_dismiss(DELAY_MS_TO_DISMISS);
    }

    public void set_delay_dismiss(int delayMs) {
        //Log.d(TAG, "set_delay_dismiss: " + delayMs);
        g_handler.removeCallbacksAndMessages(null);
        g_handler.postDelayed(this::dismiss, delayMs);
    }

    void set_delay_dismiss_stop() {
        g_handler.removeCallbacksAndMessages(null);
    }

    public void set_intro_time(int introMs) {
        DELAY_MS_TO_DISMISS = introMs;
    }

    void show_quick_window(ProgramInfo channel) {
        int quickIndex = g_currentQuickIndex;
        Log.d(TAG, "show_quick_window: [index] " + quickIndex);
        RelativeLayout quickRoot = findViewById(R.id.lo_quick_tune_quick_channels);
        View quickItem = quickRoot.findViewWithTag(TAG_QUICK_ITEM + quickIndex);
        ImageView quickAddIcon = quickItem.findViewById(R.id.lo_quick_item_add_icon);
        ImageView quickIcon = quickItem.findViewById(R.id.lo_quick_item_channel_icon);
        TextView quickNum = quickItem.findViewById(R.id.lo_quick_item_channel_text);
        TextView title = findViewById(R.id.lo_quick_tune_title_text);

        title.setText(R.string.title_quick_tune);
        quickRoot.setVisibility(VISIBLE);
        quickItem.requestFocus();

        hide_add_window();
        hide_empty_favorite();
        show_arrow_right();
        show_hint_select_4way();

        if (channel != null) {
            // remove same quick item
            delete_same_quick_item(channel);
            // show hint
            show_hint_remove();
            show_hint_move();
            // change layout
            set_quick_icon(quickIcon, channel);
            quickNum.setText(channel.getDisplayNum(MAX_LENGTH_OF_CHANNEL_NUM));
            quickAddIcon.setVisibility(INVISIBLE);
            // save quick channel
            Log.d(TAG, "show_quick_window: save quick channel");
            save_quick_channel(channel, quickIndex);
        }
    }

    void show_quick_item(int index) {
        View quickRoot, quickItem;
        ImageView quickAddIcon, quickIcon;
        TextView quickNum;
        SharedPreferences preferences;
        String channelNum, serviceId;

        quickRoot = findViewById(R.id.lo_quick_tune_quick_channels);
        quickItem = quickRoot.findViewWithTag(TAG_QUICK_ITEM + index);
        quickAddIcon = quickItem.findViewById(R.id.lo_quick_item_add_icon);
        quickIcon = quickItem.findViewById(R.id.lo_quick_item_channel_icon);
        quickNum = quickItem.findViewById(R.id.lo_quick_item_channel_text);

        preferences = get().getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        channelNum = preferences.getString(KEY_QUICK_CHANNEL_NUM + index, null);
        serviceId = preferences.getString(KEY_QUICK_SERVICE_ID + index, null);

        if (channelNum != null) {
            set_quick_icon(quickIcon, serviceId);
            quickNum.setText(channelNum);
            quickAddIcon.setVisibility(INVISIBLE);
        }
        else {
            quickIcon.setImageDrawable(null);
            quickNum.setText(null);
            quickAddIcon.setVisibility(VISIBLE);
        }
    }

    void show_add_window() {
        show_add_window(g_currentQuickIndex);
    }

    void show_add_window(int index) {
        Log.d(TAG, "show_add_window: ");
        LinearLayout addWindow = findViewById(R.id.lo_quick_tune_add_layer);
        TextView addTitle = findViewById(R.id.lo_quick_tune_title_text);
        TextView addText = findViewById(R.id.lo_quick_tune_add_text);
        AddListView addList = findViewById(R.id.lo_quick_tune_add_list);

        // init title
        String stringTitle = is_favorite()
                ? addTitle.getText() + get().getString(R.string.favorite_add_hint)
                : get().getString(R.string.title_add_quick_tune);
        addTitle.setText(stringTitle);

        // init
        g_currentQuickIndex = index;
        addText.setText(R.string.hint_enter_number);
        addWindow.setVisibility(VISIBLE);
        addList.init_all(this);
        addList.setVisibility(VISIBLE);
        addList.requestFocus();

        // show / hide view
        hide_quick_window();
        hide_favorite_window();
        hide_empty_favorite();
        hide_arrow_left();
        hide_arrow_right();
        hide_hint_remove();
        hide_hint_move();
        show_hint_select_up_down();

        // stop timeout dismiss
        set_delay_dismiss_stop();
    }

    void show_previous_window(ProgramInfo channel) {
        TextView txvTitle = findViewById(R.id.lo_quick_tune_title_text);
        String title = (String) txvTitle.getText();

        if (channel != null)
            Log.d(TAG, "show_previous_window: [channel] " + channel.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM) + " / " + channel.getDisplayName());
        else
            Log.d(TAG, "show_previous_window: [channel] null");

        g_enterNum = "";
        set_delay_dismiss();

        if (title.contains(QUICK_TUNE))
            show_quick_window(channel);

        if (title.contains(FAVORITE))
            show_favorite_window(channel);
    }

    void show_favorite_window(ProgramInfo channel) {
        FavListView favListView;

        hide_add_window();
        set_favorite_title(KeyEvent.KEYCODE_BACK);
        set_favorite_arrow();
        hold_favorite_channel(channel);
        show_hint_move();
        show_hint_remove();
        show_favorite_visibility();

        favListView = findViewById(R.id.lo_quick_tune_favorite_channels);
        favListView.setFocusable(true);
        favListView.setVisibility(VISIBLE);

        if (null == channel)
            Log.w(TAG, "show_favorite_window: null channel");
        else
            favListView.init_all(this);

        favListView.reset_for_scroll();
        favListView.scrollToPosition(0);
        favListView.requestFocus();
    }

    boolean setup_favorite_window(int keyCode) {
        FavListView favListView = findViewById(R.id.lo_quick_tune_favorite_channels);
        boolean LeftKey_to_QuickTune = VISIBLE == favListView.getVisibility();
        int index = g_currentQuickIndex;

        if (is_move_quick_item()) {
            Log.e(TAG, "setup_favorite_window: is moving");
            return false;
        }
        if (KeyEvent.KEYCODE_DPAD_RIGHT != keyCode &&
            KeyEvent.KEYCODE_DPAD_LEFT != keyCode) {
            Log.e(TAG, "setup_favorite_window: is not RIGHT or LEFT");
            return false;
        }
        if (index != 3 && index != 6 && index != 9) {
            Log.e(TAG, "setup_favorite_window: is not right side");
            return false;
        }
        if (is_add_window()) {
            Log.e(TAG, "setup_favorite_window: is Add List");
            return false;
        }

        set_favorite_title(keyCode);
        set_favorite_arrow();

        if (is_quick_window()) {
            Log.d(TAG, "show_favorite_window: hide Favorite, [LeftKey_to_QuickTune] " + LeftKey_to_QuickTune);
            favListView.setVisibility(GONE);
            show_quick_window(null);
            set_favorite_hint(R.string.hint_rcu_move);
            set_delay_dismiss();
            return LeftKey_to_QuickTune;
        }
        else {
            Log.d(TAG, "show_favorite_window: show Favorite");
            favListView.setVisibility(VISIBLE);
            favListView.init_all(this);
            favListView.requestFocus();
            hide_quick_window();
            show_hint_remove();
            show_hint_move();
            set_favorite_hint(R.string.favorite_add);
            set_delay_dismiss();
            return true;
        }
    }

    void show_favorite_visibility() {
        View emptyFavorite = findViewById(R.id.lo_quick_tune_empty_favorite);
        List<ProgramInfo> favChannels = get().g_dtv.get_program_info_list(
                get_group_type(),
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.POS_ALL);
        if (favChannels.isEmpty())
            emptyFavorite.setVisibility(VISIBLE);
        else
            emptyFavorite.setVisibility(GONE);
    }

    void show_arrow_left() {
        ImageView arrowLeft = findViewById(R.id.lo_quick_tune_left_arrow);
        arrowLeft.setVisibility(VISIBLE);
    }

    void show_arrow_right() {
        ImageView arrowRight = findViewById(R.id.lo_quick_tune_right_arrow);
        arrowRight.setVisibility(VISIBLE);
    }

    void show_hint_select_up_down() {
        RelativeLayout quickWindow;
        ImageView selectIcon;
        ViewGroup.LayoutParams params;

        quickWindow = findViewById(R.id.lo_quick_tune_window);
        selectIcon = quickWindow.findViewWithTag(TAG_SELECT_ICON);
        params = selectIcon.getLayoutParams();
        params.width = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_icon_select_width) / 2;
        selectIcon.setImageResource(R.drawable.hint_updown);
    }

    void show_hint_select_4way() {
        RelativeLayout quickWindow;
        ImageView selectIcon;
        ViewGroup.LayoutParams params;

        quickWindow = findViewById(R.id.lo_quick_tune_window);
        selectIcon = quickWindow.findViewWithTag(TAG_SELECT_ICON);
        params = selectIcon.getLayoutParams();
        params.width = get().getResources().getDimensionPixelSize(R.dimen.hotkey_hint_icon_select_width);
        selectIcon.setImageResource(R.drawable.hint_4way);
    }

    void show_hint_move() {
        add_hint_move();
    }

    void show_hint_remove() {
        add_hint_remove();
    }

    void hide_quick_window() {
        RelativeLayout quickRoot = findViewById(R.id.lo_quick_tune_quick_channels);
        quickRoot.setVisibility(GONE);
    }

    void hide_quick_item(int index) {
        Log.d(TAG, "hide_quick_item: [quick index] " + index);
        View quickRoot, quickItem;
        ImageView quickAddIcon, quickIcon;
        TextView quickNum;

        quickRoot = findViewById(R.id.lo_quick_tune_quick_channels);
        quickItem = quickRoot.findViewWithTag(TAG_QUICK_ITEM + index);
        quickAddIcon = quickItem.findViewById(R.id.lo_quick_item_add_icon);
        quickIcon = quickItem.findViewById(R.id.lo_quick_item_channel_icon);
        quickNum = quickItem.findViewById(R.id.lo_quick_item_channel_text);

        quickAddIcon.setVisibility(VISIBLE);
        quickIcon.setImageDrawable(null);
        quickNum.setText(null);
    }

    void hide_add_window() {
        LinearLayout addRoot = findViewById(R.id.lo_quick_tune_add_layer);
        addRoot.setVisibility(GONE);
    }

    void hide_favorite_window() {
        FavListView favListView = findViewById(R.id.lo_quick_tune_favorite_channels);
        favListView.setVisibility(GONE);
    }

    void hide_empty_favorite() {
        View emptyFavorite = findViewById(R.id.lo_quick_tune_empty_favorite);
        emptyFavorite.setVisibility(GONE);
    }

    void hide_arrow_left() {
        ImageView arrowLeft = findViewById(R.id.lo_quick_tune_left_arrow);
        arrowLeft.setVisibility(GONE);
    }

    void hide_arrow_right() {
        ImageView arrowRight = findViewById(R.id.lo_quick_tune_right_arrow);
        arrowRight.setVisibility(GONE);
    }

    void hide_hint_move() {
        RelativeLayout quickWindow = findViewById(R.id.lo_quick_tune_window);
        TextView moveText = quickWindow.findViewWithTag(TAG_MOVE_TEXT);
        ImageView moveIcon = quickWindow.findViewWithTag(TAG_MOVE_ICON);

        if (null == moveText)
            return;

        moveText.setVisibility(GONE);
        moveIcon.setVisibility(GONE);
    }

    void hide_hint_remove() {
        RelativeLayout quickWindow = findViewById(R.id.lo_quick_tune_window);
        TextView removeText = quickWindow.findViewWithTag(TAG_REMOVE_TEXT);
        ImageView removeIcon = quickWindow.findViewWithTag(TAG_REMOVE_ICON);

        if (null == removeText)
            return;

        removeText.setVisibility(GONE);
        removeIcon.setVisibility(GONE);
    }

    @SuppressLint("SetTextI18n")
    boolean enter_channel_num(int keyCode) {
        FavListView favListView = findViewById(R.id.lo_quick_tune_favorite_channels);
        AddListView addList = findViewById(R.id.lo_quick_tune_add_list);
        TextView enterNumTxv = findViewById(R.id.lo_quick_tune_add_text);
        String enterNumHint = get().getString(R.string.hint_enter_number);
        List<ProgramInfo> newChannels = new ArrayList<>();

        if (!is_add_window())
            return false;

        if (KeyEvent.KEYCODE_BACK != keyCode &&
            !is_keycode_num(keyCode)) {
            Log.w(TAG, "enter_channel_num: not back / number");
            return false;
        }

        if (KeyEvent.KEYCODE_BACK == keyCode) {
            int end = g_enterNum.isEmpty()
                    ? 0
                    : g_enterNum.length() - 1;
            g_enterNum = g_enterNum.substring(0, end);
            enterNumTxv.setText(g_enterNum.isEmpty()
                    ? enterNumHint
                    : g_enterNum + "_");
        }
        if (is_keycode_num(keyCode)) {
            g_enterNum = g_enterNum.length() >= 3
                    ? String.valueOf(keyCode - KeyEvent.KEYCODE_0)
                    : g_enterNum + (keyCode - KeyEvent.KEYCODE_0);
            enterNumTxv.setText(g_enterNum + "_");
        }

        if (enterNumTxv.getText().equals(enterNumHint))
            newChannels = g_LiveTvManager.get_channels();
        else
            newChannels = get_filtered_channels(g_enterNum);

        Log.d(TAG, "enter_channel_num: [enter number] " + g_enterNum);
        Log.d(TAG, "enter_channel_num: [channels size] " + newChannels.size());

        favListView.setFocusable(false);
        addList.update_list_view(newChannels);
        //addList.setAdapter(new AddListAdapter(this, newChannels));
        return KeyEvent.KEYCODE_BACK == keyCode;
    }

    boolean move_quick_item(int keyCode) {
        if (is_move_quick_item()) {
            if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode ||
                KeyEvent.KEYCODE_DPAD_UP == keyCode ||
                KeyEvent.KEYCODE_DPAD_RIGHT == keyCode ||
                KeyEvent.KEYCODE_DPAD_DOWN == keyCode) {
                g_previousQuickIndex = g_currentQuickIndex;
                Log.d(TAG, "move_quick_item: invoke switch_quick_item()");
            }
        }
        else {
            if (KeyEvent.KEYCODE_PROG_YELLOW != keyCode)
                return false;
            if (is_add_window())
                return false;
            if (is_favorite())
                return false;
            if (!has_quick_channel(get_current_quick_item()))
                return false;

            TextView title = findViewById(R.id.lo_quick_tune_title_text);
            title.setText(R.string.title_move_quick_tune);
            hide_hint_move();
            hide_hint_remove();
            set_delay_dismiss_stop();
            Log.d(TAG, "move_quick_item: start moving");
        }
        return false;
    }

    void switch_quick_item() {
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        String prevChNum, prevSrvId, currChNum, currSrvId;

        int previous = g_previousQuickIndex;
        int current = g_currentQuickIndex;

        preferences = get().getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        prevChNum = preferences.getString(KEY_QUICK_CHANNEL_NUM + previous, null);
        prevSrvId = preferences.getString(KEY_QUICK_SERVICE_ID + previous, null);
        currChNum = preferences.getString(KEY_QUICK_CHANNEL_NUM + current, null);
        currSrvId = preferences.getString(KEY_QUICK_SERVICE_ID + current, null);

        editor = preferences.edit();
        editor.putString(KEY_QUICK_CHANNEL_NUM + previous, currChNum);
        editor.putString(KEY_QUICK_SERVICE_ID + previous, currSrvId);
        editor.putString(KEY_QUICK_CHANNEL_NUM + current, prevChNum);
        editor.putString(KEY_QUICK_SERVICE_ID + current, prevSrvId);
        editor.apply();

        show_quick_item(previous);
        show_quick_item(current);
    }

    void switch_quick_item_finish() {
        TextView title = findViewById(R.id.lo_quick_tune_title_text);
        title.setText(R.string.title_quick_tune);
        show_hint_move();
        show_hint_remove();
        set_delay_dismiss();
    }
    
    boolean delete_quick_item(int keyCode) {
        int quickIndex;

        if (KeyEvent.KEYCODE_PROG_BLUE != keyCode)
            return false;
        if (is_add_window())
            return false;
        if (!has_quick_channel(get_current_quick_item()))
            return false;

        Log.d(TAG, "delete_quick_item: delete a quick channel");
        quickIndex = g_currentQuickIndex;
        hide_quick_item(quickIndex);
        hide_hint_move();
        hide_hint_remove();
        delete_quick_channel(quickIndex);
        set_delay_dismiss();
        return false;
    }

    void delete_same_quick_item(ProgramInfo channel) {
        RelativeLayout quickRoot = findViewById(R.id.lo_quick_tune_quick_channels);
        ImageView quickAddIcon, quickIcon;
        TextView quickNum;
        View quickItem;
        String channelNum;

        for (int index = 1; index < MAX_COUNT_OF_QUICK_ITEM; index++) {
            quickItem = quickRoot.findViewWithTag(TAG_QUICK_ITEM + index);
            quickNum = quickItem.findViewById(R.id.lo_quick_item_channel_text);
            channelNum = (String) quickNum.getText();
            if (channelNum.equals(channel.getDisplayNum(MAX_LENGTH_OF_CHANNEL_NUM))) {
                quickAddIcon = quickItem.findViewById(R.id.lo_quick_item_add_icon);
                quickIcon = quickItem.findViewById(R.id.lo_quick_item_channel_icon);
                quickAddIcon.setVisibility(VISIBLE);
                quickIcon.setImageDrawable(null);
                quickNum.setText(null);
                delete_quick_channel(index);
                Log.w(TAG, "delete_same_quick_item: [channel num]" + channelNum);
            }
        }
    }

    void save_quick_channel(ProgramInfo channel, int quickIndex) {
        SharedPreferences preferences = get().getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String channelNum = channel.getDisplayNum(MAX_LENGTH_OF_CHANNEL_NUM);
        String serviceId = channel.getServiceId(MAX_LENGTH_OF_SERVICE_ID);

        Log.i(TAG, "save_quick_channel: [quick index] " + quickIndex);
        Log.i(TAG, "save_quick_channel: [channel num] " + channelNum);
        Log.i(TAG, "save_quick_channel: [service id] " + serviceId);

        editor.putString(KEY_QUICK_CHANNEL_NUM + quickIndex, channelNum);
        editor.putString(KEY_QUICK_SERVICE_ID + quickIndex, serviceId);
        editor.apply();
    }

    void delete_quick_channel(int quickIndex) {
        SharedPreferences preferences = get().getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Log.d(TAG, "delete_quick_channel: [quick index] " + quickIndex);
        editor.remove(KEY_QUICK_CHANNEL_NUM + quickIndex);
        editor.remove(KEY_QUICK_SERVICE_ID + quickIndex);
        editor.apply();
    }

    void delete_favorite_channel(ProgramInfo channel) {
        FavListView favListView = findViewById(R.id.lo_quick_tune_favorite_channels);
        String chNum = channel.getDisplayNum(MAX_LENGTH_OF_CHANNEL_NUM);
        String chName = channel.getDisplayName();
        int groupType = get_group_type();

        Log.d(TAG, "delete_favorite_channel: Favorite " + groupType + " delete " + chNum + " / " + chName);
        get().g_dtv.fav_info_delete(groupType, channel.getChannelId());
        favListView.init_all(this);
        set_delay_dismiss();
    }

    void hold_favorite_channel(ProgramInfo channel) {
        List<FavInfo> favInfoList;
        int groupType = -1;

        if (null == channel)
            return;

        groupType = get_group_type();
        if (groupType == -1)
            return;

        favInfoList = get().g_dtv.fav_info_get_list(groupType);
        for (FavInfo favInfo : favInfoList) {
            if (favInfo.getChannelId() == channel.getChannelId()) {
                String chNum = channel.getDisplayNum(MAX_LENGTH_OF_CHANNEL_NUM);
                Log.w(TAG, "save_favorite_channel: Favorite " + groupType + " has same channel " + chNum + ", do not save...");
                return;
            }
        }
        favInfoList.add(new FavInfo(channel.getLCN(), channel.getChannelId(), groupType));
        favInfoList.sort(Comparator.comparingInt(FavInfo::getFavNum));
        get().g_dtv.fav_info_update_list(groupType, favInfoList);
    }

    void save_favorite_channel() {
        Log.w(TAG, "save_favorite_channel: save favorite channels to database");
        get().g_dtv.save_table(EnTableType.GROUP);
    }

    boolean need_block_keycode(int keyCode) {
        boolean is_favorite = is_favorite();
        boolean is_quick_window = is_quick_window();
        boolean is_move_quick_item = is_move_quick_item();
        boolean bottomIndex = g_currentQuickIndex == 7 || g_currentQuickIndex == 8 || g_currentQuickIndex == 9;
        return (KeyEvent.KEYCODE_DPAD_DOWN == keyCode && bottomIndex && (is_quick_window || is_move_quick_item)) ||
               (KeyEvent.KEYCODE_PROG_YELLOW == keyCode && is_favorite);
    }
}
