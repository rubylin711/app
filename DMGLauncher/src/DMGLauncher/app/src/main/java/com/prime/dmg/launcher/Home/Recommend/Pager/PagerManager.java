package com.prime.dmg.launcher.Home.Recommend.Pager;

import android.animation.AnimatorInflater;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.prime.dmg.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.dmg.launcher.ACSDatabase.ACSHelper;
import com.prime.dmg.launcher.Home.Recommend.List.RecommendItem;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.Utils.ActivityUtils;
import com.prime.dmg.launcher.Utils.JsonParser.AdPageItem;
import com.prime.dmg.launcher.Utils.JsonParser.JsonParser;
import com.prime.dmg.launcher.Utils.JsonParser.AdPage;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Home.Menu.HomeMenuView;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/** @noinspection CommentedOutCode*/
@SuppressWarnings("ConstantConditions")
public class PagerManager {

    String TAG = PagerManager.class.getSimpleName();

    public static final int PAGE_SWITCH_TIMEOUT     = 5000;
    public static final String CLS_TV_SETTINGS      = HomeActivity.CLS_TV_SETTINGS;
    public static final String CLS_HOME_ACTIVITY    = HomeActivity.CLS_HOME_ACTIVITY;

    public static final String PAGE_TYPE_FULL       = "Full";
    public static final String PAGE_TYPE_HORIZONTAL = "Horizontal";
    public static final String PAGE_TYPE_VERTICAL   = "Vertical";

    public static final String SOURCE_TYPE_YOUTUBE  = "Youtube";
    public static final String SOURCE_TYPE_MYVIDEO  = "MyVideo";
    public static final String SOURCE_TYPE_CATCHPLAY= "CatchPlay";
    public static final String SOURCE_TYPE_QRCODE   = "QRcode";
    public static final String SOURCE_TYPE_PROGRAM  = "Program";
    public static final String SOURCE_TYPE_STREAM   = "Stream";
    public static final String SOURCE_TYPE_BROWSER  = "Browser";
    public static final String SOURCE_TYPE_POSTER   = "Poster";
    public static final String SOURCE_TYPE_CP       = "CP";
    public static final String SOURCE_TYPE_CP_MULTI = "CP(multi)";
    public static final String SOURCE_TYPE_APP      = "APP";

    WeakReference<HomeActivity> g_ref;
    static ComponentName    g_componentName;
    ViewPager2              g_pager;
    OnPageChangeCallback    g_pager_callback;
    Timer                   g_AutoSwitchTimer;

    List<AdPage> g_pageList;
    int g_pagePos;
    int g_keyCode;

    public PagerManager(HomeActivity activity) {
        g_ref = new WeakReference<>(activity);
        init_pager_all(); // constructor
    }

    public void init_pager_all() {
        get().runOnUiThread(() -> {
            //if (is_first_init() || is_resume_by(CLS_TV_SETTINGS) || is_resume_by(CLS_HOME_ACTIVITY))
            if (is_first_init()) Log.d(TAG, "init_pager_all: first time, component name = " + g_componentName);
            else                 Log.d(TAG, "init_pager_all: init again, component name = " + g_componentName.getClassName());

            PagerAdapter adapter = new PagerAdapter(this, get_ad_pages(), new TransparentTransform());

            if (adapter!= null  && adapter.getItemCount() > 0)
                hide_pager_hint_no_program();

            g_pager = get().findViewById(R.id.lo_home_recommend);
            g_pager.setAdapter(adapter);
            g_pager.setOffscreenPageLimit(2);
            g_pager.setCurrentItem(0);

            init_pager_indicator();
            //start_auto_switch(); // on init
            //add_callback();
            //on_focus_page(); // on init
            //on_change_page(); // on init
        });
    }

    public void init_pager_indicator() {
        PagerAdapter adapter = (PagerAdapter) g_pager.getAdapter();

        if (adapter == null) {
            Log.e(TAG, "init_page_indicator: null adapter");
            return;
        }

        LinearLayout indicator = get().findViewById(R.id.lo_home_indicator);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp_to_px(5), dp_to_px(5));
        params.setMargins(dp_to_px(5), dp_to_px(5), dp_to_px(5), dp_to_px(5));
        indicator.removeAllViews();

        for (int i = indicator.getChildCount() ; i < adapter.getItemCount() ; i++) {
            View v = new View(g_ref.get());
            v.setBackgroundResource(R.drawable.circle_selector);
            v.setLayoutParams(params);
            indicator.addView(v);
        }
    }

    public void on_focus_page() {
        g_pager = get().findViewById(R.id.lo_home_recommend);
        g_pager.setOnFocusChangeListener(null);
        g_pager.setOnFocusChangeListener((v, hasFocus) -> {
            PagerAdapter adapter  = (PagerAdapter) g_pager.getAdapter();
            PagerFragment fragment = adapter != null
                                       ? adapter.get_fragment(g_pagePos)
                                       : null;
            if (!hasFocus)
                return;
            if (null == adapter ||
                null == fragment) {
                Log.e(TAG, "on_focus_page: null adapter/fragment");
                return;
            }
            Log.d(TAG, "on_focus_page: focus page " + g_pagePos);
            fragment.request_focus(g_keyCode);
        });
    }

    public void on_change_page() {
        /*if (null != g_pager_callback) {
            Log.d(TAG, "on_change_page: already register OnPageChangeCallback");
            return;
        }*/
        g_pager_callback = new OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                PagerAdapter adapter  = (PagerAdapter) g_pager.getAdapter();
                PagerFragment fragment = adapter != null
                                           ? adapter.get_fragment(g_pagePos)
                                           : null;
                if (ViewPager2.SCROLL_STATE_IDLE != state ||
                    !g_pager.hasFocus() ||
                    fragment == null)
                    return;

                Log.d(TAG, "on_change_page: page " + g_pagePos + " request focus");
                fragment.request_focus(g_keyCode);

                super.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageSelected(int position) {
                //Log.d(TAG, "on_change_page: draw dot/poster, [position] " + position);
                g_pagePos = position;
                draw_dot(position);
                draw_poster(g_pager, position);
            }
        };

        g_pager = get().findViewById(R.id.lo_home_recommend);
        g_pager.registerOnPageChangeCallback(g_pager_callback);
    }

    public boolean on_key_down(int keyCode) {
        boolean fullScreen;
        int pagePos;

        g_keyCode  = keyCode;
        fullScreen = get().g_liveTvMgr.is_full_screen();

        if (fullScreen)
            return false;

        if (is_outside(keyCode)) {
            Log.w(TAG, "on_key_down: " + (fullScreen ? "is full screen" : "focus pager view"));
            return true;
        }

        if (!g_pager.hasFocus())
            return false;

        if (!is_ok_adapter())
            return false;

        if (!is_scroll_ready(keyCode))
            return false;

        pagePos = get_page_pos(keyCode);
        set_page_pos(pagePos);

        return false;
    }

    public void on_create() {
        //init_pager_all(); // create
    }

    public void on_start() {
        start_auto_switch(); // on start
        add_callback();
    }

    public void on_pause() {
        //remove_callback();
        //stop_auto_switch(); // on pause
        g_componentName = ActivityUtils.get_running_task(get());
        //Log.d(TAG, "on_pause: component name = " + g_componentName.getClassName());
    }

    public void on_stop() {
        remove_callback();
        stop_auto_switch(); // on stop
        g_componentName = ActivityUtils.get_running_task(get());
    }

    public void on_destroy() {
        //remove_callback();
        g_componentName = ActivityUtils.get_running_task(get());
        //Log.d(TAG, "on_destroy: component name = " + g_componentName.getClassName());
    }

    public void block_view() {
        Log.d(TAG, "block_view: ");
        View liveTVFrame;
        HomeMenuView menu;

        // block Live TV
        liveTVFrame = get().findViewById(R.id.lo_home_live_tv_frame);
        liveTVFrame.setFocusable(false);

        // block Menu
        menu = get().findViewById(R.id.lo_menu_rltvl_home_menu);
        menu.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    public void unblock_view() {
        View liveTvFrame;
        HomeMenuView menu;

        // unblock Live TV
        liveTvFrame = get().findViewById(R.id.lo_home_live_tv_frame);
        liveTvFrame.setFocusable(true);
        Log.d(TAG, "unblock_view: " + liveTvFrame);

        // unblock Menu
        menu = get().findViewById(R.id.lo_menu_rltvl_home_menu);
        menu.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        Log.d(TAG, "unblock_view: " + menu);
    }

    public void add_callback() {
        //if (true)
        //    return;
        Log.d(TAG, "add_callback: ");
        LinearLayout indicator = get().findViewById(R.id.lo_home_indicator);
        for (int i = 0; i < indicator.getChildCount(); i++) {
            indicator.getChildAt(i).setSelected(false);
        }
        View dotView = indicator.getChildAt(get_page_pos());
        if (dotView != null) {
            dotView.setStateListAnimator(AnimatorInflater.loadStateListAnimator(get(), R.animator.circle_animator));
            dotView.setSelected(true);
        }

        on_focus_page();
        on_change_page();
    }

    public void remove_callback() {
        //if (true)
        //    return;
        Log.d(TAG, "remove_callback: ");
        g_pager.setOnFocusChangeListener(null);
        g_pager.unregisterOnPageChangeCallback(g_pager_callback);
    }

    public void remove_all_page() {
        Log.d(TAG, "remove_all_page: ");
        g_pager = get().findViewById(R.id.lo_home_recommend);
        //g_pager.setAdapter(null);
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean is_scroll_ready(int keyCode) {
        ConstraintLayout v;

        if (!is_key_left_right(keyCode)) {
            Log.w(TAG, "is_scroll_ready: not LEFT or RIGHT");
            return false;
        }

        v = get_poster_view();
        if (v == null) {
            Log.e(TAG, "is_scroll_ready: null poster view");
            return false;
        }

        if (is_poster_left(v) &&
            is_key_right(keyCode))
            return false;

        if (is_poster_right(v) &&
            is_key_left(keyCode))
            return false;

        return true;
    }

    public boolean is_poster_left(ConstraintLayout v) {
        View view = v.findFocus();
        if (view == null)
            return false;
        return view.getId() == R.id.lo_2_grid_poster_1_frame;
    }

    public boolean is_poster_right(ConstraintLayout v) {
        View view = v.findFocus();
        if (view == null)
            return false;
        return view.getId() == R.id.lo_2_grid_poster_2_frame;
    }

    public boolean is_key_left_right(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
               keyCode == KeyEvent.KEYCODE_DPAD_RIGHT;
    }

    public boolean is_key_left(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_DPAD_LEFT;
    }

    public boolean is_key_right(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_DPAD_RIGHT;
    }

    public boolean is_key_allow(int keyCode) {
        return KeyEvent.KEYCODE_DPAD_RIGHT == keyCode ||
               KeyEvent.KEYCODE_DPAD_LEFT  == keyCode ||
               KeyEvent.KEYCODE_DPAD_DOWN  == keyCode;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_ok_adapter() {
        PagerAdapter adapter = (PagerAdapter) g_pager.getAdapter();
        if (adapter == null) {
            Log.e(TAG, "is_ok_adapter: null adapter");
            return false;
        }
        if (adapter.getItemCount() <= 0) {
            Log.e(TAG, "is_ok_adapter: there is no fragment");
            return false;
        }
        return true;
    }

    public boolean is_outside(int keyCode) {
        View liveTvFrame = get().findViewById(R.id.lo_home_live_tv_frame);

        if (!liveTvFrame.hasFocus())
            return false;

        if (KeyEvent.KEYCODE_DPAD_RIGHT != keyCode)
            return false;

        if (has_recommend())
            g_pager.requestFocus();

        return true;
    }

    public boolean is_first_init() {
        return g_componentName == null;
    }

    public boolean is_resume_by(String clsName) {
        return g_componentName != null &&
               g_componentName.getClassName().equals(clsName);
    }

    public boolean is_last_page() {
        return get_page_pos() == get_page_count() - 1;
    }

    public boolean has_recommend() {
        View hint_no_program = get().findViewById(R.id.lo_home_recommend_hint);
        return View.VISIBLE != hint_no_program.getVisibility();
    }

    public HomeActivity get() {
        return g_ref.get();
    }

    /** @noinspection ConstantValue*/
    public List<AdPage> get_ad_pages() {
        String jsonString = ACSDataProviderHelper.get_acs_provider_data(get(), "ad_list");

        if (false) {
            jsonString = "[{\"page\":\"1\",\"type\":\"Full\",\"items\":[{\"pagination\":1,\"sequence\":0,\"title\":\"【大大寬頻】光纖就是快！愛情好光鮮_ 2.視訊篇\",\"type\":\"Full\",\"sourceType\":\"1001\",\"source_type\":\"Youtube\",\"typeName\":\"Youtube\",\"poster_art_url\":\"https://i.ytimg.com/vi/FhQ2L1tAIEw/hqdefault.jpg\",\"description\":\"【大大寬頻】光纖就是快！愛情好光鮮_ 2.視訊篇\n\",\"videoId\":\"FhQ2L1tAIEw\",\"playListId\":null,\"url\":\"FhQ2L1tAIEw\"}]},"
                    + "{\"page\":\"2\",\"type\":\"Full\",\"items\":[{\"pagination\":2,\"sequence\":0,\"title\":\"HBO GO\",\"type\":\"Full\",\"sourceType\":\"1007\",\"source_type\":\"QRcode\",\"typeName\":\"QRcode\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1723769886888.jpg\",\"description\":\"HBO GO最新強檔！\n8/16起瘋狂麥斯電影系列之《芙莉歐莎：瘋狂麥斯傳奇篇章》立即線上觀賞！\",\"url\":\"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\"}]},"
                    + "{\"page\":\"3\",\"type\":\"Full\",\"items\":[{\"pagination\":3,\"sequence\":0,\"title\":\"LiTV隨選影片\",\"type\":\"Full\",\"sourceType\":\"1007\",\"source_type\":\"QRcode\",\"typeName\":\"QRcode\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1723167782395.png\",\"description\":\"LiTV隨選影片年約超值優惠只要$490\",\"url\":\"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/litv\"}]},"
                    + "{\"page\":\"4\",\"type\":\"Horizontal\",\"items\":[{\"pagination\":4,\"sequence\":0,\"title\":\"大法師-信徒\",\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//program/ch2005/pt%E5%A4%A7%E6%B3%95%E5%B8%AB-%E4%BF%A1%E5%BE%92.png\",\"description\":\"當一個12歲的女孩被一個神秘的惡魔附身時，她的父親拼命求助，最終找到了一個人幫忙，此人在1970年代也幫助了自己的女兒從類似的附身事件中倖存\",\"start_time\":\"2024/08/17 21:00\",\"service_id\":\"2005\",\"end_time\":\"2024/08/17 22:50\",\"channel_num\":\"65\",\"channel_name\":\"HBO HD\"},{\"pagination\":4,\"sequence\":1,\"title\":\"尚氣與十環傳奇\",\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//program/ch3006/pt%E5%B0%9A%E6%B0%A3%E8%88%87%E5%8D%81%E7%92%B0%E5%82%B3%E5%A5%87.png\",\"description\":\"敘述尚氣（劉思慕飾演）自幼接受父親文武（梁朝偉飾演）的鐵血戰鬥培訓，精通各種武術招式。長大後為了逃離家鄉，他改名換姓展開新生活，認識了摯友凱蒂（奧卡菲娜飾演），平靜的日子就這樣過了十年，直到過去再度找上門…\",\"start_time\":\"2024/08/16 21:00\",\"service_id\":\"3006\",\"end_time\":\"2024/08/16 23:50\",\"channel_num\":\"66\",\"channel_name\":\"東森洋片台 HD\"}]},"
                    + "{\"page\":\"5\",\"type\":\"Horizontal\",\"items\":[{\"pagination\":5,\"sequence\":0,\"title\":\"惡女\",\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//program/ch2087/pt%E6%83%A1%E5%A5%B3.png\",\"description\":\"主播黃立美愛情事業兩得意，工作表現優異獲得晉升，同步籌辦與牙醫大為的婚禮，即將踏入禮堂迎來美好人生。某日，立美的父親忽然表示希望再婚，對象秀蘭其貌不揚、學歷不高卻以擅長烹飪及溫柔的照護緊抓住父親的心。立美的母親在她幼時早逝，多年來與父親相依關係緊密，面對父親突然的婚訊，她以女人的直覺判斷秀蘭肯定不單純。\",\"start_time\":\"2024/08/17 21:00\",\"service_id\":\"2087\",\"end_time\":\"2024/08/17 23:25\",\"channel_num\":\"61\",\"channel_name\":\"CATCHPLAY電影台 HD\"},{\"pagination\":5,\"sequence\":1,\"title\":\"雉岳山怪談\",\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//program/ch3005/pt%E9%9B%89%E5%B2%B3%E5%B1%B1%E6%80%AA%E8%AB%87.png\",\"description\":\"登山自行車社團「登山吧」的隊長民俊（尹筠相）和隊員們前往他的表妹賢智（金叡園）父親的森林小屋，計畫在山中練車和拍攝影片。深夜時，突然一陣紅光朝他們來襲來，賢智竟被一股奇怪的引力帶走，失去蹤影。民俊急忙到森林中尋人，其他隊員也陸續碰到奇怪的事件…\",\"start_time\":\"2024/08/18 21:00\",\"service_id\":\"3005\",\"end_time\":\"2024/08/18 22:50\",\"channel_num\":\"63\",\"channel_name\":\"緯來電影台 HD\"}]},"
                    + "{\"page\":\"6\",\"type\":\"Horizontal\",\"items\":[{\"pagination\":6,\"sequence\":0,\"title\":\"金牌間諜\",\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//program/ch2088/pt%E9%87%91%E7%89%8C%E9%96%93%E8%AB%9C.png\",\"description\":\"艾力克斯（歐文威爾森 飾）是位笨手笨腳的中央情報局探員，雖然熱愛間諜的工作，卻常常把任務搞砸。上司交給他的最新任務，是偵破一樁國際軍火走私案，由於這名軍火商熱愛拳擊運動，CIA便藉助金牌拳王凱利（艾迪墨菲 飾）的幫助接近軍火商。而在辦案的過程，由於兩人個性和辦事風格截然不同，引發許多笑料。\",\"start_time\":\"2024/08/17 21:00\",\"service_id\":\"2088\",\"end_time\":\"2024/08/17 23:05\",\"channel_num\":\"69\",\"channel_name\":\"amc電影台 HD\"},{\"pagination\":6,\"sequence\":1,\"title\":\"歡樂好聲音2\",\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//program/ch3006/pt%E6%AD%A1%E6%A8%82%E5%A5%BD%E8%81%B2%E9%9F%B32.png\",\"description\":\"野心勃勃的無尾熊巴斯特（金獎影帝馬修麥康納 配音）以及他旗下的藝人將他的新穆恩劇院，轉變成當地最火紅的表演場地，但是他現在有一個更大的野心，那就是在光芒萬丈的紅灘市的克里斯托塔劇院，舉辦一場全新歌舞大秀的首演會。\",\"start_time\":\"2024/08/22 19:00\",\"service_id\":\"3006\",\"end_time\":\"2024/08/22 21:20\",\"channel_num\":\"66\",\"channel_name\":\"東森洋片台 HD\"}]},"
                    + "{\"page\":\"7\",\"type\":\"Horizontal\",\"items\":[{\"pagination\":7,\"sequence\":0,\"title\":\"驚弒新娘\",\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//program/ch2088/pt%E9%A9%9A%E5%BC%92%E6%96%B0%E5%A8%98.png\",\"description\":\"葛瑞絲和卡洛斯一見鍾情，並隨即決定共結連理，幾乎所有卡洛斯的家人都因這項喜訊欣喜若狂，除了相當保護卡洛斯的姊姊潔絲敏，她認為僅僅認識一個月的他們操之過急，並對葛瑞絲心生疑慮。即使葛瑞絲想盡辦法試圖取得潔絲敏的信任，潔絲敏仍察覺葛瑞絲隱藏不可告人的祕密。\",\"start_time\":\"2024/08/18 21:00\",\"service_id\":\"2088\",\"end_time\":\"2024/08/18 22:50\",\"channel_num\":\"69\",\"channel_name\":\"amc電影台 HD\"},{\"pagination\":7,\"sequence\":1,\"title\":\"Q18量子預言:1\",\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//program/ch2501/ptQ18%E9%87%8F%E5%AD%90%E9%A0%90%E8%A8%80%3A1.png\",\"description\":\"公司為了提供人類最美好的生活製造各種量子產品,而當​科技和人性的相互拉扯，究竟會發生什麼意想不到的事情？\",\"start_time\":\"2024/08/16 22:00\",\"service_id\":\"2501\",\"end_time\":\"2024/08/16 23:00\",\"channel_num\":\"30\",\"channel_name\":\"三立都會台HD\"}]},"
                    + "{\"page\":\"8\",\"type\":\"Vertical\",\"items\":[{\"pagination\":8,\"sequence\":0,\"title\":\"嗨！營業中第4季:1\",\"type\":\"Vertical\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//program/ch3503/pt%E5%97%A8%EF%BC%81%E7%87%9F%E6%A5%AD%E4%B8%AD%E7%AC%AC4%E5%AD%A3%3A1.png\",\"description\":\"《嗨！營業中》第4季由姚元浩、郭泓志、吳映潔、莎莎與乱彈阿翔，挑戰移動營業升級版，上山下海到處烹調美味餐點給各式各樣的客人享用，他們如何同心協力度過難關是一大看點。\",\"start_time\":\"2024/08/18 20:00\",\"service_id\":\"3503\",\"end_time\":\"2024/08/18 22:00\",\"channel_num\":\"28\",\"channel_name\":\"八大綜合台 HD\"},{\"pagination\":8,\"sequence\":1,\"title\":\"完美的結婚公式:1\",\"type\":\"Vertical\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//program/ch2508/pt%E5%AE%8C%E7%BE%8E%E7%9A%84%E7%B5%90%E5%A9%9A%E5%85%AC%E5%BC%8F%3A1.png\",\"description\":\"為了報復丈夫和家人而選擇契約婚姻的女人，和為了迎娶她作為妻子而假裝契約婚姻的男子之間，驚險又隱秘的「回歸復仇羅曼史」...\",\"start_time\":\"2024/08/19 21:00\",\"service_id\":\"2508\",\"end_time\":\"2024/08/19 22:00\",\"channel_num\":\"40\",\"channel_name\":\"東森戲劇台 HD\"},{\"pagination\":8,\"sequence\":2,\"title\":\"KM Special 韓國文化探究竟:1\",\"type\":\"Vertical\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//program/ch2034/ptKM%20Special%20%E9%9F%93%E5%9C%8B%E6%96%87%E5%8C%96%E6%8E%A2%E7%A9%B6%E7%AB%9F%3A1.png\",\"description\":\"2024年8月18日 每週日 晚間6點（首播）、每週二 晚間6點（重播）博大精深的韓國，不僅有大家熟悉的韓劇、K-pop和美妝……等，淵遠流長的傳統文化也同樣精彩有趣。一起透過《KM Special 韓國文化探究竟》認識韓國的傳統節日、遊戲、歌舞和美食，享受韓國的千年之美吧！\",\"start_time\":\"2024/08/18 18:00\",\"service_id\":\"2034\",\"end_time\":\"2024/08/18 18:30\",\"channel_num\":\"217\",\"channel_name\":\"韓國娛樂台KMTV HD\"}]},"
                    + "{\"page\":\"9\",\"type\":\"Full\",\"items\":[{\"pagination\":9,\"sequence\":0,\"title\":\"離開美國結婚去第5季:1\",\"type\":\"Full\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//program/ch2006/pt%E9%9B%A2%E9%96%8B%E7%BE%8E%E5%9C%8B%E7%B5%90%E5%A9%9A%E5%8E%BB%E7%AC%AC5%E5%AD%A3%3A1.png\",\"description\":\"離開美國結婚去第5季:遠道而來-對那些以愛之名移居海外的美國人，搬離美國的生活舒適圈值得嗎？還是他們會捧著心碎搬回美國？\",\"start_time\":\"2024/08/16 21:00\",\"service_id\":\"2006\",\"end_time\":\"2024/08/16 23:00\",\"channel_num\":\"21\",\"channel_name\":\"TLC 旅遊生活 HD\"}]},"
                    + "{\"page\":\"10\",\"type\":\"Full\",\"items\":[{\"pagination\":10,\"sequence\":0,\"title\":\"金礦的賭注急流篇第7季:1\",\"type\":\"Full\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https://epgstore.tbc.net.tw/acs-api//program/ch4501/pt%E9%87%91%E7%A4%A6%E7%9A%84%E8%B3%AD%E6%B3%A8%E6%80%A5%E6%B5%81%E7%AF%87%E7%AC%AC7%E5%AD%A3%3A1.png\",\"description\":\"金礦的賭注急流篇第7季:悲劇的開始   經歷了上一季的挫敗之後，達斯汀赫特帶著一組人馬重回金塊溪。\",\"start_time\":\"2024/08/17 21:00\",\"service_id\":\"4501\",\"end_time\":\"2024/08/17 22:00\",\"channel_num\":\"19\",\"channel_name\":\"Discovery HD\"}]}]";
            String layoutType = "Full";
            jsonString = "[" +
                    "{\"page\":\"1\",\"type\":\"Full\",\"items\":[{\"pagination\":1,\"sequence\":0,\"title\":\"【大大寬頻】光纖就是快！愛情好光鮮_3.追劇打怪篇\",\"type\":\"Full\",\"sourceType\":\"1001\",\"source_type\":\"Youtube\",\"typeName\":\"Youtube\",\"poster_art_url\":\"https:\\/\\/i.ytimg.com\\/vi\\/ca2FAsUvwJM\\/hqdefault.jpg\",\"description\":\"【大大寬頻】光纖就是快！愛情好光鮮_3.追劇打怪篇\\n\",\"videoId\":\"ca2FAsUvwJM\",\"playListId\":null,\"url\":\"ca2FAsUvwJM\"}]}," +
                    "{\"items\": [{\"description\": \"P2_stream_desc\",\"pagination\": 2,\"poster_art_url\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1627238218059.png\",\"sequence\": 0,\"sourceType\": \"1004\",\"source_type\": \"Stream\",\"title\": \"Phase2_liveStream_test\",\"type\": \"" + layoutType + "\",\"typeName\": \"Livestream\",\"url\": \"https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8\"}],\"page\": \"2\",\"type\": \"" + layoutType + "\"}," +
                    "{\"items\": [{\"description\": \"數位門市新登場\n多種優惠輕鬆選擇\n快速又方便\",\"pagination\": 3,\"poster_art_url\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1627632835463.jpg\",\"sequence\": 0,\"sourceType\": \"1005\",\"source_type\": \"Browser\",\"title\": \"數位門市\",\"type\": \"" + layoutType + "\",\"typeName\": \"Browser\",\"url\": \"https://shop.tbc.net.tw/Store/tv\"}],\"page\": \"3\",\"type\": \"" + layoutType + "\"}," +
                    "{\"items\": [{\"description\": \"你知道嗎~\n大大寬頻光纖到府，從機房直接送訊號到你家，中間不再經過其他設備，速度更快、更穩、更安全！\",\"pagination\": 4,\"poster_art_url\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1693558246263.jpg\",\"sequence\": 0,\"sourceType\": \"1006\",\"source_type\": \"Poster\",\"title\": \"【大大寬頻】光纖到府\",\"type\": \"" + layoutType + "\",\"typeName\": \"Poster\",\"url\": \"https://youtu.be/ofR6EixhoJY\"}],\"page\": \"4\",\"type\": \"" + layoutType + "\"}," +
                    "{\"items\": [{\"description\": \"MoveV宅家不廢柴而是好身材\",\"pagination\": 5,\"poster_art_url\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1685080652445.png\",\"sequence\": 0,\"sourceType\": \"1007\",\"source_type\": \"QRcode\",\"title\": \"MoveV\",\"type\": \"" + layoutType + "\",\"typeName\": \"QRcode\",\"url\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/movev\"}],\"page\": \"5\",\"type\": \"" + layoutType + "\"}," +
                    "{\"items\": [{\"channel_name\": \"TVBS 歡樂台HD\",\"channel_num\": \"42\",\"description\": \"全新互動提款機，將益智與生活完美融合!來賓通過三大回合的智力考驗，即可從提款機中領獎金，把智慧變現金!\",\"end_time\": \"2024/10/18 21:00\",\"pagination\": 6,\"poster_art_url\": \"https://epgstore.tbc.net.tw/acs-api//program/ch2510/pt%E6%8B%9C%E8%A8%97ATM%3A165.png\",\"sequence\": 0,\"service_id\": \"2510\",\"sourceType\": \"1008\",\"source_type\": \"Program\",\"start_time\": \"2024/10/18 20:00\",\"title\": \"拜託ATM:165\",\"type\": \"" + layoutType + "\",\"typeName\": \"Program\"}],\"page\": \"6\",\"type\": \"" + layoutType + "\"}," +
                    "{\"items\": [{\"activityName\": \"CP\",\"description\": null,\"intent_flags\": [\"0\"],\"packageName\": \"net.fetnet.fetvod.tv\",\"package_params\": {\"EXTRA_VIDEO_ID\": \"\"},\"pagination\": 7,\"poster_art_url\": \"https://123.241.238.34/app_bd201/ico/1585291136394.jpg\",\"sequence\": 0,\"sourceType\": \"1010\",\"source_type\": \"CP\",\"title\": \"friDay影音\",\"type\": \"" + layoutType + "\",\"typeName\": \"CP\",\"uri\": null}],\"page\": \"7\",\"type\": \"" + layoutType + "\"}," +
                    "{\"items\": [{\"activityName\": \"CP\",\"description\": null,\"intent_flags\": [\"0\"],\"packageName\": \"net.fetnet.fetvod.tv\",\"package_params\": {\"EXTRA_VIDEO_ID\": \"\"},\"pagination\": 7,\"poster_art_url\": \"https://123.241.238.34/app_bd201/ico/1585291136394.jpg\",\"sequence\": 0,\"sourceType\": \"1010\",\"source_type\": \"CP\",\"title\": \"friDay影音\",\"type\": \"" + layoutType + "\",\"typeName\": \"CP\",\"uri\": null}],\"page\": \"7\",\"type\": \"" + layoutType + "\"}," +
                    "{\"items\": [{\"description\": null,\"packageName\": \"com.skysoft.kkbox.android\",\"pagination\": 8,\"poster_art_url\": \"https://123.241.238.34/app_bd201/ico/1582796015407.png\",\"sequence\": 0,\"sourceType\": \"1009\",\"source_type\": \"APP\",\"title\": \"KKBOX\",\"type\": \"" + layoutType + "\",\"typeName\": \"APP\"}],\"page\": \"8\",\"type\": \"" + layoutType + "\"}," +
                    "{\"items\": [{\"description\": null,\"packageName\": \"com.mitake.tv\",\"pagination\": 9,\"poster_art_url\": \"https://123.241.238.34/app_bd201/ico/1699427028278.jpg\",\"sequence\": 0,\"sourceType\": \"1011\",\"source_type\": \"CP(multi)\",\"title\": \"三竹股市\",\"type\": \"" + layoutType + "\",\"typeName\": \"CP(multi)\"}],\"page\": \"9\",\"type\": \"" + layoutType + "\"}," +
                    "{\"items\": [{\"description\": null,\"packageName\": \"com.ebc.news\",\"pagination\": 10,\"poster_art_url\": \"https://123.241.238.34/app_bd201/ico/1583721848200.png\",\"sequence\": 0,\"sourceType\": \"1011\",\"source_type\": \"CP(multi)\",\"title\": \"東森新聞\",\"type\": \"" + layoutType + "\",\"typeName\": \"CP(multi)\"}],\"page\": \"10\",\"type\": \"" + layoutType + "\"}," +
                    "{\"items\": [{\"activityName\": \"MyVideo\",\"description\": null,\"intent_flags\": [\"0\"],\"packageName\": \"com.taiwanmobile.myVideotv\",\"package_params\": {\"EXTRA_VIDEO_ID\": \"\"},\"pagination\": 7,\"poster_art_url\": \"http://acs-portal.bandott.com/app_bd201/ico/1524572798236.png\",\"sequence\": 0,\"sourceType\": \"1002\",\"source_type\": \"MyVideo\",\"title\": \"MyVideo\",\"type\": \"" + layoutType + "\",\"typeName\": \"MyVideo\",\"uri\": \"intent:#Intent;action=407063;component=com.taiwanmobile.myVideotv/com.taiwanmobile.activity.SearchDeepLink;S.VIDEOTITLE=%E6%95%B8%E7%A2%BC%E5%AF%B6%E8%B2%9D02%20THE%20BEGINNING;S.DEEPACTION=Content%3A407063%7C0;i.NOTIFICATION_ID=407063;S.VIDEOMAINCATORY=%E9%9B%BB%E5%BD%B1;end\"}],\"page\": \"7\",\"type\": \"" + layoutType + "\"}," +
                    "{\"items\": [{\"activityName\": \"CatchPlay\",\"description\": null,\"intent_flags\": [\"0\"],\"packageName\": \"com.catchplay.asiaplay.common.tv\",\"package_params\": {\"EXTRA_VIDEO_ID\": \"\"},\"pagination\": 7,\"poster_art_url\": \"https://172.19.76.17/app_bd201/ico/1584093496183.png\",\"sequence\": 0,\"sourceType\": \"1003\",\"source_type\": \"CatchPlay\",\"title\": \"CatchPlay\",\"type\": \"" + layoutType + "\",\"typeName\": \"CatchPlay\",\"uri\": \"intent:#Intent;action=407063;component=com.taiwanmobile.myVideotv/com.taiwanmobile.activity.SearchDeepLink;S.VIDEOTITLE=%E6%95%B8%E7%A2%BC%E5%AF%B6%E8%B2%9D02%20THE%20BEGINNING;S.DEEPACTION=Content%3A407063%7C0;i.NOTIFICATION_ID=407063;S.VIDEOMAINCATORY=%E9%9B%BB%E5%BD%B1;end\"}],\"page\": \"7\",\"type\": \"" + layoutType + "\"}," +
                    "{\"page\":\"2\",\"type\":\"Full\",\"items\":[{\"pagination\":2,\"sequence\":0,\"title\":\"【歡慶開幕！$50觀影盛典！】\",\"type\":\"Full\",\"sourceType\":\"1006\",\"source_type\":\"Poster\",\"typeName\":\"Poster\",\"poster_art_url\":\"https:\\/\\/epgstore.tbc.net.tw\\/acs-api\\/\\/uploads\\/icon\\/1726740391412.jpg\",\"description\":\"9\\/28(六)歡慶開幕！\\n前500名民眾於活動日領取「觀影盛典兌換券」\\n可享$50觀賞當日2D版本電影！\",\"url\":\"https:\\/\\/www.vscinemas.com.tw\\/\"}]}," +
                    "{\"page\":\"3\",\"type\":\"Full\",\"items\":[{\"pagination\":3,\"sequence\":0,\"title\":\"LiTV隨選影片\",\"type\":\"Full\",\"sourceType\":\"1007\",\"source_type\":\"QRcode\",\"typeName\":\"QRcode\",\"poster_art_url\":\"https:\\/\\/epgstore.tbc.net.tw\\/acs-api\\/\\/uploads\\/icon\\/1724981260997.png\",\"description\":\"LiTV隨選影片年約超值優惠只要$490\",\"url\":\"https:\\/\\/mwps.tbc.net.tw\\/product\\/ott\\/{STB_SC_ID}\\/{STB_CA_SN}\\/{BAT_ID}\\/litv\"}]}," +
                    "{\"page\":\"4\",\"type\":\"Full\",\"items\":[{\"pagination\":4,\"sequence\":0,\"title\":\"HBO GO服務中止公告\",\"type\":\"Full\",\"sourceType\":\"1005\",\"source_type\":\"Browser\",\"typeName\":\"Browser\",\"poster_art_url\":\"https:\\/\\/epgstore.tbc.net.tw\\/acs-api\\/\\/uploads\\/icon\\/1726196771055.jpg\",\"description\":\"2024\\/10\\/31終止合作，原訂戶可收視至方案到期日，權益不受影響\",\"url\":\"https:\\/\\/www.tbc.net.tw\\/Program\\/Introduct\\/HBOGO\"}]}," +
                    "{\"page\":\"5\",\"type\":\"Horizontal\",\"items\":[{\"pagination\":5,\"sequence\":0,\"title\":\"企鵝人第1季:2\",\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https:\\/\\/epgstore.tbc.net.tw\\/acs-api\\/\\/program\\/ch2005\\/pt%E4%BC%81%E9%B5%9D%E4%BA%BA%E7%AC%AC1%E5%AD%A3%3A2.png\",\"description\":\"這部DC工作室製作的影集，由柯林法洛飾演奧斯科波（別名企鵝人），共八集。故事接續導演麥特李維斯與華納兄弟打造的全球賣座電影“蝙蝠俠”，講述著史詩級犯罪傳奇故事，而主角為電影中法洛所飾演的角色\",\"start_time\":\"2024\\/09\\/30 21:00\",\"service_id\":\"2005\",\"end_time\":\"2024\\/09\\/30 22:00\",\"channel_num\":\"65\",\"channel_name\":\"HBO HD\"},{\"pagination\":5,\"sequence\":1,\"title\":\"雷鬼之父-音樂無國界\",\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https:\\/\\/epgstore.tbc.net.tw\\/acs-api\\/\\/program\\/ch2005\\/pt%E9%9B%B7%E9%AC%BC%E4%B9%8B%E7%88%B6-%E9%9F%B3%E6%A8%82%E7%84%A1%E5%9C%8B%E7%95%8C.png\",\"description\":\"本片記錄了音樂傳奇巴布馬利的人生和音樂創作歷程，以及他是如何藉由音樂傳遞愛、和平和團結的精神，啟發了一代又一代的人們。且看巴布馬利如何克服困境，製作出改變世界的音樂\",\"start_time\":\"2024\\/10\\/05 21:00\",\"service_id\":\"2005\",\"end_time\":\"2024\\/10\\/05 22:50\",\"channel_num\":\"65\",\"channel_name\":\"HBO HD\"}]}," +
                    "{\"page\":\"6\",\"type\":\"Horizontal\",\"items\":[{\"pagination\":6,\"sequence\":0,\"title\":\"辣妹過招\",\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https:\\/\\/epgstore.tbc.net.tw\\/acs-api\\/\\/program\\/ch2005\\/pt%E8%BE%A3%E5%A6%B9%E9%81%8E%E6%8B%9B.png\",\"description\":\"新入學的凱蒂海倫加入了學校社交食物鏈中最頂端，以陰險的人氣女王蕾吉娜喬治為首的「塑料姊妹」。在與學校的邊緣人聯手策畫要讓蕾吉娜下台後，凱蒂得學會如何不忘初衷，並設法在高中校園這個最殘酷的叢林中生存。\",\"start_time\":\"2024\\/09\\/25 21:00\",\"service_id\":\"2005\",\"end_time\":\"2024\\/09\\/25 22:50\",\"channel_num\":\"65\",\"channel_name\":\"HBO HD\"},{\"pagination\":6,\"sequence\":1,\"title\":\"脫稿玩家\",\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https:\\/\\/epgstore.tbc.net.tw\\/acs-api\\/\\/program\\/ch3006\\/pt%E8%84%AB%E7%A8%BF%E7%8E%A9%E5%AE%B6.png\",\"description\":\"描述一個銀行小職員 (萊恩雷諾斯飾) 日復一日年復一年做著無聊的工作，過著無聊的生活，直到有一天他突然發現自己是一個暴力電玩遊戲中可有可無的配角，他想要突破現狀，但這個自覺讓整個遊戲變調，還要挑戰層層關卡，最後他是否會從C咖變A咖呢?\",\"start_time\":\"2024\\/09\\/25 21:00\",\"service_id\":\"3006\",\"end_time\":\"2024\\/09\\/25 23:25\",\"channel_num\":\"66\",\"channel_name\":\"東森洋片台 HD\"}]}," +
                    "{\"page\":\"7\",\"type\":\"Horizontal\",\"items\":[{\"pagination\":7,\"sequence\":0,\"title\":null,\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":null,\"description\":null,\"start_time\":null,\"service_id\":null,\"end_time\":null,\"channel_num\":null,\"channel_name\":null},{\"pagination\":7,\"sequence\":1,\"title\":\"復仇行動\",\"type\":\"Horizontal\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https:\\/\\/epgstore.tbc.net.tw\\/acs-api\\/\\/program\\/ch2088\\/pt%E5%BE%A9%E4%BB%87%E8%A1%8C%E5%8B%95.png\",\"description\":\"西班牙影帝安東尼奧班德拉斯主演。律師法蘭克剛收到妻子傳來女兒的表演歌唱的影片，沒多久便接到妻女慘遭不明人士殺害的噩耗。悲痛欲絕的法蘭克，在發現警方無法查明真相之後，開始了刻苦的鍛鍊，他誓言在復仇成功之前，保持緘默…。\",\"start_time\":\"2024\\/09\\/24 23:00\",\"service_id\":\"2088\",\"end_time\":\"2024\\/09\\/25 00:50\",\"channel_num\":\"69\",\"channel_name\":\"amc電影台 HD\"}]}," +
                    "{\"page\":\"8\",\"type\":\"Vertical\",\"items\":[{\"pagination\":8,\"sequence\":0,\"title\":null,\"type\":\"Vertical\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":null,\"description\":null,\"start_time\":null,\"service_id\":null,\"end_time\":null,\"channel_num\":null,\"channel_name\":null},{\"pagination\":8,\"sequence\":1,\"title\":\"史提夫勇闖鱷魚王國:1\",\"type\":\"Vertical\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https:\\/\\/epgstore.tbc.net.tw\\/acs-api\\/\\/program\\/ch4528\\/pt%E5%8F%B2%E6%8F%90%E5%A4%AB%E5%8B%87%E9%97%96%E9%B1%B7%E9%AD%9A%E7%8E%8B%E5%9C%8B%3A1.png\",\"description\":\"史提夫引領我們進入危險的鱷魚世界。鱷魚是自然界最令人聞風喪膽的掠食者，而他將在一名武裝狩獵旅行嚮導的保護下，進入鱷魚環伺的地區。\",\"start_time\":\"2024\\/09\\/27 13:30\",\"service_id\":\"4528\",\"end_time\":\"2024\\/09\\/27 14:25\",\"channel_num\":\"18\",\"channel_name\":\"BBC Earth HD\"},{\"pagination\":8,\"sequence\":2,\"title\":\"雲襄傳:3\",\"type\":\"Vertical\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https:\\/\\/epgstore.tbc.net.tw\\/acs-api\\/\\/program\\/ch2507\\/pt%E9%9B%B2%E8%A5%84%E5%82%B3%3A3.png\",\"description\":\"百年前，一場皇室內亂導致生靈塗炭，進而皇位更迭。一方奉先皇遺命，他們放棄權力紛爭，超然世外，於暗處守護天下蒼生， 自 命“雲台”；另一方則自命“淩淵” ，他們不遺餘力對抗新朝，以圖輔佐先帝血脈奪回皇 權。兩大組織各有抱負，時而聯合、時而抗衡，他們的明爭暗鬥已綿延百餘年。\",\"start_time\":\"2024\\/09\\/30 16:00\",\"service_id\":\"2507\",\"end_time\":\"2024\\/09\\/30 17:00\",\"channel_num\":\"39\",\"channel_name\":\"中天娛樂台 HD\"}]}," +
                    "{\"page\":\"9\",\"type\":\"Full\",\"items\":[{\"pagination\":9,\"sequence\":0,\"title\":null,\"type\":\"Full\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":null,\"description\":null,\"start_time\":null,\"service_id\":null,\"end_time\":null,\"channel_num\":null,\"channel_name\":null}]}," +
                    "{\"page\":\"10\",\"type\":\"Full\",\"items\":[{\"pagination\":10,\"sequence\":0,\"title\":\"黑人女孩失蹤事件\",\"type\":\"Full\",\"sourceType\":\"1008\",\"source_type\":\"Program\",\"typeName\":\"Program\",\"poster_art_url\":\"https:\\/\\/epgstore.tbc.net.tw\\/acs-api\\/\\/program\\/ch2097\\/pt%E9%BB%91%E4%BA%BA%E5%A5%B3%E5%AD%A9%E5%A4%B1%E8%B9%A4%E4%BA%8B%E4%BB%B6.png\",\"description\":\"雪莉發現她的女兒蘿倫失蹤了。儘管雪莉努力尋求警方和媒體的幫助，她的請求卻被一名白人女孩的失蹤事件完全蓋過。\",\"start_time\":\"2024\\/09\\/25 22:00\",\"service_id\":\"2097\",\"end_time\":\"2024\\/09\\/25 23:35\",\"channel_num\":\"219\",\"channel_name\":\"Lifetime HD\"}]}" +
                    "]";
        }

        List<AdPage> pageList = JsonParser.parse_ad_pages(jsonString);
        g_pageList = bind_from_tv_contract(pageList); //return JsonParser.parse_ad_pages(jsonString);
        return g_pageList;
    }

    public int get_page_pos() {
        return g_pagePos;
    }

    public int get_page_pos(int keyCode) {
        PagerAdapter adapter = (PagerAdapter) g_pager.getAdapter();

        if (!is_ok_adapter())
            return 0;

        //noinspection DataFlowIssue
        int pageCount = adapter.getItemCount();
        int pagePos   = g_pager.getCurrentItem();

        pagePos = keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ? pagePos + 1 :
                  keyCode == KeyEvent.KEYCODE_DPAD_LEFT  ? pagePos - 1 : pagePos;

        pagePos = pagePos >= pageCount
                ? pageCount - 1
                : pagePos;

        return pagePos;
    }

    public int get_page_count() {
        PagerAdapter adapter = (PagerAdapter) g_pager.getAdapter();
        if (null == adapter) {
            Log.e(TAG, "get_page_count: null");
            return 0;
        }
        return adapter.getItemCount();
    }

    public ConstraintLayout get_poster_view() {
        PagerAdapter adapter;

        adapter = (PagerAdapter) g_pager.getAdapter();

        if (null == adapter)
            return null;
        if (null == adapter.get_fragment(g_pagePos))
            return null;
        if (null == adapter.get_fragment(g_pagePos).getView())
            return null;
        return (ConstraintLayout) adapter.get_fragment(g_pagePos).getView();
    }

    public void set_page_pos(int pagePos) {
        block_view();
        g_pagePos = Math.max(pagePos, 0);
        g_pager.setCurrentItem(pagePos, true);
        //Log.e(TAG, "set_page_pos: pagePos = " + pagePos);
        if (pagePos < 0)
            unblock_view();
    }

    public void set_visible_page(boolean visible) {
        LinearLayout indicator = get().findViewById(R.id.lo_home_indicator);

        indicator.setVisibility(visible ? View.VISIBLE : View.GONE);
        g_pager.setVisibility(visible ? View.VISIBLE : View.GONE);
        g_pager.setDescendantFocusability(visible ? ViewGroup.FOCUS_BEFORE_DESCENDANTS
                                                  : ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    /** @noinspection UnusedReturnValue*/
    public int reset_page_position() {
        if (null == g_pager)
            return Log.e(TAG, "reset_page_position: null pager");
        if (!g_pager.hasFocus())
            return Log.d(TAG, "reset_page_position: [pager hasFocus] " + g_pager.hasFocus());
        g_pagePos = 0;
        g_pager.setCurrentItem(g_pagePos, true);
        start_auto_switch(); // on back
        //unblock_view();
        return Log.d(TAG, "reset_page_position: to page " + g_pagePos);
    }

    public void reload_pager() {
        Log.w(TAG, "reload_pager: reload all ( ACSHelper.MSG_AD_LIST )");
        Handler handler = get().get_main_handler();
        Message message = handler.obtainMessage(ACSHelper.MSG_AD_LIST);
        handler.sendMessage(message);
    }

    public void show_pager() {
        set_visible_page(true);
    }

    public void hide_pager() {
        set_visible_page(false);
    }

    public void hide_pager_hint_no_program() {
        View hint_no_program = get().findViewById(R.id.lo_home_recommend_hint);
        hint_no_program.setVisibility(View.INVISIBLE);
    }

    public void draw_dot(int position) {
        LinearLayout indicator = get().findViewById(R.id.lo_home_indicator);

        for (int i = 0; i < indicator.getChildCount(); i++) {
            indicator.getChildAt(i).setSelected(false);
        }

        View dotView = indicator.getChildAt(position);
        if (dotView != null) {
            dotView.setStateListAnimator(AnimatorInflater.loadStateListAnimator(get(), R.animator.circle_animator));
            dotView.setSelected(true);
        }
    }

    public void draw_poster(ViewPager2 pager, int position) {
        PagerAdapter adapter  = (PagerAdapter) pager.getAdapter();
        PagerFragment fragment = adapter != null ? adapter.get_fragment(position) : null;
        if (fragment == null)
            return;
        fragment.draw_poster();
    }

    public void update_ad_list(boolean fromACS) {
        Log.d(TAG, "update_ad_list: from ACS: " + fromACS);
        init_pager_all(); // update
    }

    public List<AdPage> bind_from_tv_contract(List<AdPage> pageList) {
        List<RecommendItem> programList = ActivityUtils.get_tv_programs(get(), null);

        for (AdPage adPage : pageList) {
        for (AdPageItem adPageItem : adPage.get_items()) {
                String adPkgName = adPageItem.get_package_name();
                String adSourceType = adPageItem.get_source_type();

                if (null == adPkgName || null == adSourceType)
                    continue;
                if (!adSourceType.equals(PagerManager.SOURCE_TYPE_CP) &&
                    !adSourceType.equals(PagerManager.SOURCE_TYPE_CP_MULTI))
                    continue;

                for (RecommendItem tvProgram : programList) {
                    if (adPageItem.is_bind_from_tv_contract() ||
                        tvProgram.is_bind_from_tv_contract())
                        continue;
                    if (adPkgName.equals(tvProgram.get_package_name())) {
                        adPageItem.set_label(tvProgram.get_label());
                        adPageItem.set_description(tvProgram.get_description());
                        adPageItem.set_poster_art_url(tvProgram.get_thumbnail_url());
                        adPageItem.set_intent_uri(tvProgram.get_intent_uri());
                        adPageItem.set_bind_from_tv_contract(true);
                        tvProgram.set_bind_from_tv_contract(true);
                    }
                }
        } // for AdPageItem
        } // for AdPage
        return pageList;
    }

    public void start_auto_switch() {
        g_pager = get().findViewById(R.id.lo_home_recommend);

        if (g_AutoSwitchTimer != null)
            g_AutoSwitchTimer.cancel(); // Cancel any existing timer

        g_AutoSwitchTimer = new Timer();
        g_AutoSwitchTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                PagerAdapter adapter = (PagerAdapter) g_pager.getAdapter();
                if (null == adapter) {
                    Log.e(TAG, "start_auto_switch: null adapter");
                    return;
                }
                int currPos = g_pager.getCurrentItem();
                int nextPos = adapter.getItemCount() == 0 ? 0 : (currPos + 1) % adapter.getItemCount();

                // Ensure UI updates happen on the main thread
                g_pager.post(() -> {
                    g_pager.setCurrentItem(g_pager.hasFocus() ? currPos : nextPos);
                    //Log.e(TAG, "start_auto_switch: [position] " + g_pager.getCurrentItem());
                });
            }
        }, PAGE_SWITCH_TIMEOUT, PAGE_SWITCH_TIMEOUT); // Schedule at fixed delay

        Log.d(TAG, "start_auto_switch: ");
    }

    public void stop_auto_switch() {
        if (g_AutoSwitchTimer != null) {
            g_AutoSwitchTimer.cancel();
            g_AutoSwitchTimer = null;
            Log.d(TAG, "stop_auto_switch: ");
        }
    }

    /** @noinspection SameParameterValue*/
    private int dp_to_px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, get().getResources().getDisplayMetrics());
    }

    public static class TransparentTransform extends BitmapTransformation {

        @Override
        protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap source, int outWidth, int outHeight) {

            Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);

            for (int x = 0; x < source.getWidth(); x++) {
                for (int y = 0; y < source.getHeight(); y++) {
                    int pixel = source.getPixel(x, y);
                    if (pixel == Color.WHITE) // 替换白色背景为透明
                        result.setPixel(x, y, Color.TRANSPARENT);
                    else
                        result.setPixel(x, y, pixel);
                }
            }

            return result;
        }

        @Override
        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
            messageDigest.update("TransparentBackgroundTransformation".getBytes());
        }
    }
}
