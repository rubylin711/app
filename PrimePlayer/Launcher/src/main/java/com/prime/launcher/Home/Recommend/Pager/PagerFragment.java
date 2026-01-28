package com.prime.launcher.Home.Recommend.Pager;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.prime.launcher.Home.Hotkey.HotkeyInfo;
import com.prime.launcher.Home.LiveTV.LiveTvManager;
import com.prime.launcher.Home.QRCode.QRCodeDialog;
import com.prime.launcher.Home.Recommend.Pager.PagerManager.TransparentTransform;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.Utils.ActivityUtils;
import com.prime.launcher.Utils.JsonParser.AdPage;
import com.prime.launcher.Utils.JsonParser.AdPageItem;
import com.prime.launcher.R;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.RejectedExecutionException;

/** @noinspection CommentedOutCode*/
public class PagerFragment extends Fragment {

    static String TAG = "PagerFragment";

    public static final String LAYOUT_1_GRID_POSTER  = "Layout 1 Grid Poster";
    public static final String LAYOUT_2_GRID_POSTER  = "Layout 2 Grid Poster";
    public static final String LAYOUT_3_GRID_CHANNEL = "Layout 3 Grid Channel";

    public static final String PAGE_TYPE_FULL       = PagerManager.PAGE_TYPE_FULL;
    public static final String PAGE_TYPE_HORIZONTAL = PagerManager.PAGE_TYPE_HORIZONTAL;
    public static final String PAGE_TYPE_VERTICAL   = PagerManager.PAGE_TYPE_VERTICAL;

    public static final String SOURCE_TYPE_YOUTUBE  = PagerManager.SOURCE_TYPE_YOUTUBE;
    public static final String SOURCE_TYPE_MYVIDEO  = PagerManager.SOURCE_TYPE_MYVIDEO;
    public static final String SOURCE_TYPE_CATCHPLAY= PagerManager.SOURCE_TYPE_CATCHPLAY;
    public static final String SOURCE_TYPE_QRCODE   = PagerManager.SOURCE_TYPE_QRCODE;
    public static final String SOURCE_TYPE_PROGRAM  = PagerManager.SOURCE_TYPE_PROGRAM;
    public static final String SOURCE_TYPE_STREAM   = PagerManager.SOURCE_TYPE_STREAM;
    public static final String SOURCE_TYPE_BROWSER  = PagerManager.SOURCE_TYPE_BROWSER;
    public static final String SOURCE_TYPE_POSTER   = PagerManager.SOURCE_TYPE_POSTER;
    public static final String SOURCE_TYPE_CP       = PagerManager.SOURCE_TYPE_CP;
    public static final String SOURCE_TYPE_CP_MULTI = PagerManager.SOURCE_TYPE_CP_MULTI;
    public static final String SOURCE_TYPE_APP      = PagerManager.SOURCE_TYPE_APP;

    public static final String YOUTUBE_DEFAULT_HOST = "https://youtu.be/";
    public static final String YOUTUBE_EMBED_HOST = "https://www.youtube.com/embed/";

    WeakReference<AppCompatActivity> g_ref;
    Context g_context;
    PagerManager g_pagerMgr;
    String g_layoutType;
    View g_fragmentView;
    AdPage g_adPage;
    TransparentTransform g_transform;

    public PagerFragment() {
    }

    public PagerFragment(PagerManager pagerManager, AdPage adPage, TransparentTransform transform) {
        g_ref = new WeakReference<>(pagerManager.get());
        g_context = get().getApplicationContext();
        g_pagerMgr = pagerManager;
        g_adPage = adPage;
        g_transform = transform;

        String pageType = adPage.get_type();
        if (PAGE_TYPE_FULL.equals(pageType))        g_layoutType = LAYOUT_1_GRID_POSTER;
        if (PAGE_TYPE_HORIZONTAL.equals(pageType))  g_layoutType = LAYOUT_2_GRID_POSTER;
        if (PAGE_TYPE_VERTICAL.equals(pageType))    g_layoutType = LAYOUT_3_GRID_CHANNEL;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null == g_adPage) {
            Log.e(TAG, "onCreateView: cannot create view");
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        String pageType = g_adPage.get_type();
        //Log.d(TAG, "onCreateView: [type] " + pageType);

        if (PAGE_TYPE_FULL.equals(pageType))
            return init_1grid_poster(inflater, container);
        else
        if (PAGE_TYPE_HORIZONTAL.equals(pageType))
            return init_2grid_poster(inflater, container);
        else
        if (PAGE_TYPE_VERTICAL.equals(pageType))
            return init_3grid_poster(inflater, container);
        else
            Log.e(TAG, "onCreateView: error [type] " + pageType);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        /*ImageView fullPoster = g_fragmentView.findViewById(R.id.lo_1_grid_poster_img);
        ImageView leftPoster = g_fragmentView.findViewById(R.id.lo_2_grid_poster_1_img);
        ImageView rightPoster = g_fragmentView.findViewById(R.id.lo_2_grid_poster_2_img);
        ImageView topPoster    = g_fragmentView.findViewById(R.id.lo_3_grid_poster_1_img);
        ImageView middlePoster = g_fragmentView.findViewById(R.id.lo_3_grid_poster_2_img);
        ImageView bottomPoster = g_fragmentView.findViewById(R.id.lo_3_grid_poster_3_img);
        Glide.with(get()).clear(fullPoster);
        Glide.with(get()).clear(leftPoster);
        Glide.with(get()).clear(rightPoster);
        Glide.with(get()).clear(topPoster);
        Glide.with(get()).clear(middlePoster);
        Glide.with(get()).clear(bottomPoster);*/
        Glide.get(g_context).clearMemory();
        super.onDestroyView();
    }

    public View init_1grid_poster(LayoutInflater inflater, ViewGroup container) {
        g_fragmentView = inflater.inflate(R.layout.fragment_1_grid_poster, container, false);
        ImageView fullPoster = g_fragmentView.findViewById(R.id.lo_1_grid_poster_img);
        View fullFocus = g_fragmentView.findViewById(R.id.lo_1_grid_poster_frame);

        set_poster_info_full(0);
        set_poster_image(fullPoster, 0);
        on_focus_poster(fullFocus);
        on_key_poster(fullFocus);
        on_click_poster(fullFocus, 0);

        return g_fragmentView;
    }

    public View init_2grid_poster(LayoutInflater inflater, ViewGroup container) {
        g_fragmentView = inflater.inflate(R.layout.fragment_2_grid_poster, container, false);
        ImageView leftPoster = g_fragmentView.findViewById(R.id.lo_2_grid_poster_1_img);
        ImageView rightPoster = g_fragmentView.findViewById(R.id.lo_2_grid_poster_2_img);
        View leftFocus = g_fragmentView.findViewById(R.id.lo_2_grid_poster_1_frame);
        View rightFocus = g_fragmentView.findViewById(R.id.lo_2_grid_poster_2_frame);

        set_poster_info_horizontal(0);
        set_poster_info_horizontal(1);
        set_poster_image(leftPoster, 0);
        set_poster_image(rightPoster, 1);
        on_focus_poster(leftFocus);
        on_focus_poster(rightFocus);
        on_key_poster(leftFocus);
        on_key_poster(rightFocus);
        on_click_poster(leftFocus, 0);
        on_click_poster(rightFocus, 1);

        return g_fragmentView;
    }

    public View init_3grid_poster(LayoutInflater inflater, ViewGroup container) {
        g_fragmentView = inflater.inflate(R.layout.fragment_3_grid_poster, container, false);
        ImageView topPoster    = g_fragmentView.findViewById(R.id.lo_3_grid_poster_1_img);
        ImageView middlePoster = g_fragmentView.findViewById(R.id.lo_3_grid_poster_2_img);
        ImageView bottomPoster = g_fragmentView.findViewById(R.id.lo_3_grid_poster_3_img);
        View topFocus    = g_fragmentView.findViewById(R.id.lo_3_grid_poster_1_frame);
        View middleFocus = g_fragmentView.findViewById(R.id.lo_3_grid_poster_2_frame);
        View bottomFocus = g_fragmentView.findViewById(R.id.lo_3_grid_poster_3_frame);

        set_poster_info_vertical(0);
        set_poster_info_vertical(1);
        set_poster_info_vertical(2);
        set_poster_image(topPoster, 0);
        set_poster_image(middlePoster, 1);
        set_poster_image(bottomPoster, 2);
        on_focus_poster(topFocus);
        on_focus_poster(middleFocus);
        on_focus_poster(bottomFocus);
        on_key_poster(topFocus);
        on_key_poster(bottomFocus);
        on_click_poster(topFocus, 0);
        on_click_poster(middleFocus, 1);
        on_click_poster(bottomFocus, 2);

        return g_fragmentView;
    }

    public void request_focus(int keyCode) {
        View focusView;

        if (g_fragmentView == null) {
            Log.w(TAG, "request_focus: null g_view");
            return;
        }

        if (LAYOUT_1_GRID_POSTER.equals(g_layoutType))
            focusView = g_fragmentView.findViewById(R.id.lo_1_grid_poster_frame);
        else
        if (LAYOUT_2_GRID_POSTER.equals(g_layoutType))
            focusView = (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode)
                      ? g_fragmentView.findViewById(R.id.lo_2_grid_poster_1_frame)
                      : g_fragmentView.findViewById(R.id.lo_2_grid_poster_2_frame);
        else
            focusView = g_fragmentView.findViewById(R.id.lo_3_grid_poster_1_frame);

        if (focusView == null) {
            Log.w(TAG, "request_focus: null view");
            return;
        }
        //Log.d(TAG, "request_focus: ");
        focusView.requestFocus();
    }

    public void on_focus_poster(View view) {
        view.setOnFocusChangeListener((v, hasFocus) -> {
            int viewID = view.getId();

            if (hasFocus) {
                if (viewID == R.id.lo_1_grid_poster_frame)      Log.d(TAG, "on_focus_poster: focus 1 grid poster");
                if (viewID == R.id.lo_2_grid_poster_1_frame)    Log.d(TAG, "on_focus_poster: focus 2 grid poster at left");
                if (viewID == R.id.lo_2_grid_poster_2_frame)    Log.d(TAG, "on_focus_poster: focus 2 grid poster at right");
                if (viewID == R.id.lo_3_grid_poster_1_frame)    Log.d(TAG, "on_focus_poster: focus 3 grid channel at index 0");
                if (viewID == R.id.lo_3_grid_poster_2_frame)    Log.d(TAG, "on_focus_poster: focus 3 grid channel at index 1");
                if (viewID == R.id.lo_3_grid_poster_3_frame)    Log.d(TAG, "on_focus_poster: focus 3 grid channel at index 2");
            }
        });
    }

    public void on_key_poster(View focusView) {
        focusView.setOnKeyListener((v, keyCode, event) -> {
            int viewID = focusView.getId();

            if (KeyEvent.ACTION_UP == event.getAction())
                return false;

            //noinspection StatementWithEmptyBody
            if (HomeActivity.ENABLE_WEBVIEW) {
                /*BaseWebView webView = get().findViewById(R.id.lo_home_web_view);
                if (webView.control_web_view(event))
                    return true;*/
            }

            if (viewID == R.id.lo_1_grid_poster_frame   ||
                viewID == R.id.lo_2_grid_poster_1_frame ||
                viewID == R.id.lo_2_grid_poster_2_frame ||
                viewID == R.id.lo_3_grid_poster_1_frame) {
                if (KeyEvent.KEYCODE_DPAD_UP == keyCode) {
                    Log.w(TAG, "on_keydown_poster: DO NOT INPUT KEY, KEYCODE_DPAD_UP " + keyCode);
                    return true;
                }
            }

            if (viewID == R.id.lo_1_grid_poster_frame ||
                viewID == R.id.lo_2_grid_poster_2_frame ||
                viewID == R.id.lo_3_grid_poster_1_frame ||
                viewID == R.id.lo_3_grid_poster_2_frame ||
                viewID == R.id.lo_3_grid_poster_3_frame) {
                if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode &&
                    get().get_pager_manager().is_last_page()) {
                    Log.w(TAG, "on_keydown_poster: DO NOT INPUT KEY, KEYCODE_DPAD_RIGHT " + keyCode);
                    return true;
                }
            }

            if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode) {
                if (viewID == R.id.lo_1_grid_poster_frame ||
                    viewID == R.id.lo_2_grid_poster_1_frame ||
                    viewID == R.id.lo_2_grid_poster_2_frame ||
                    viewID == R.id.lo_3_grid_poster_3_frame) {
                    Log.w(TAG, "on_keydown_poster: UNBLOCK VIEW, key code = " + keyCode);
                    get().g_pagerMgr.unblock_view();
                    return false;
                }
            }
            return false;
        });
    }

    public void on_click_poster(View view, int index) {
        view.setOnClickListener(v -> {
            AdPageItem pageItem = null;
            String sourceType = null;

            if (index >= g_adPage.get_items().size()) {
                Toast.makeText(get(), "Null", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "on_click_poster: null pageItem");
                return;
            }
            pageItem = g_adPage.get_items().get(index);
            sourceType = pageItem.get_source_type();

            Log.d(TAG, "on_click_poster: sourceType = " + sourceType);
            open_ad(get(), pageItem);
        });
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    /** @noinspection CallToPrintStackTrace*/
    public String get_program_time(AdPageItem pageItem) {
        SimpleDateFormat sdf_date_time = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
        SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        String formattedTime = null;
        String startTime = pageItem.get_start_time();
        String endTime = pageItem.get_end_time();

        try {
            if (null == startTime || startTime.equalsIgnoreCase("null"))
                startTime = "N/A";

            if (null == endTime || endTime.equalsIgnoreCase("null"))
                endTime = "N/A";

            if (startTime.equals("N/A") || endTime.equals("N/A"))
                return "";

            Date dateStart = sdf_date_time.parse(startTime);
            Date dateEnd = sdf_date_time.parse(endTime);

            if (dateStart != null)
                startTime = sdf_date_time.format(dateStart);

            if (dateEnd != null)
                endTime = sdf_time.format(dateEnd);

            formattedTime = startTime + " - " + endTime;
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedTime;
    }

    public TransparentTransform get_transform() {
        if (null == g_transform)
            g_transform = new TransparentTransform();
        return g_transform;
    }

    @SuppressLint("SetTextI18n")
    public void set_poster_info_full(int index) {
        TextView progTime = g_fragmentView.findViewById(R.id.lo_1_grid_poster_prog_time);
        TextView progName = g_fragmentView.findViewById(R.id.lo_1_grid_poster_prog_name);
        TextView progChannel = g_fragmentView.findViewById(R.id.lo_1_grid_poster_prog_channel);
        AdPageItem pageItem = g_adPage.get_items().get(index);
        String sourceType = pageItem.get_source_type();

        if (SOURCE_TYPE_PROGRAM.equals(sourceType)) {
            if (null == pageItem.get_title() || pageItem.get_title().equals("null")) {
                progTime.setText("");
                progChannel.setText("");
                progName.setText(get().getString(R.string.no_data));
            }
            else {
                progChannel.setText(pageItem.get_channel_num() + " " + pageItem.get_channel_name());
                progTime.setText(get_program_time(pageItem));
                progName.setText(pageItem.get_title());
            }
        }
        else {
            progTime.setText("");
            progChannel.setText("");
            progName.setText(pageItem.get_label());
        }
    }

    @SuppressLint("SetTextI18n")
    public void set_poster_info_horizontal(int index) {
        TextView progTime = index == 0 ? g_fragmentView.findViewById(R.id.lo_2_grid_poster_1_prog_time) : g_fragmentView.findViewById(R.id.lo_2_grid_poster_2_prog_time);
        TextView progName = index == 0 ? g_fragmentView.findViewById(R.id.lo_2_grid_poster_1_prog_name) : g_fragmentView.findViewById(R.id.lo_2_grid_poster_2_prog_name);
        TextView chName = index == 0 ? g_fragmentView.findViewById(R.id.lo_2_grid_poster_1_prog_channel) : g_fragmentView.findViewById(R.id.lo_2_grid_poster_2_prog_channel);
        AdPageItem pageItem = null;

        if (index >= g_adPage.get_items().size()) {
            chName.setText("N/A");
            progName.setText("N/A");
            progTime.setText("N/A");
            return;
        }

        pageItem = g_adPage.get_items().get(index);
        String sourceType = pageItem.get_source_type();
        if (SOURCE_TYPE_PROGRAM.equals(sourceType)) {
            if (null == pageItem.get_title() || pageItem.get_title().equals("null")) {
                progTime.setText("");
                chName.setText("");
                progName.setText(get().getString(R.string.no_data));
            }
            else {
                progTime.setText(get_program_time(pageItem));
                chName.setText(pageItem.get_channel_num() + " " + pageItem.get_channel_name());
                progName.setText(pageItem.get_title());
            }
        }
        else {
            progTime.setText("");
            chName.setText("");
            progName.setText(pageItem.get_label());
        }
    }

    @SuppressLint("SetTextI18n")
    public void set_poster_info_vertical(int index) {
        TextView progTime = index == 0 ? g_fragmentView.findViewById(R.id.lo_3_grid_poster_1_prog_time) :
                            index == 1 ? g_fragmentView.findViewById(R.id.lo_3_grid_poster_2_prog_time) : g_fragmentView.findViewById(R.id.lo_3_grid_poster_3_prog_time);
        TextView progName = index == 0 ? g_fragmentView.findViewById(R.id.lo_3_grid_poster_1_prog_name) :
                            index == 1 ? g_fragmentView.findViewById(R.id.lo_3_grid_poster_2_prog_name) : g_fragmentView.findViewById(R.id.lo_3_grid_poster_3_prog_name);
        TextView chName = index == 0 ? g_fragmentView.findViewById(R.id.lo_3_grid_poster_1_ch_name) :
                          index == 1 ? g_fragmentView.findViewById(R.id.lo_3_grid_poster_2_ch_name) : g_fragmentView.findViewById(R.id.lo_3_grid_poster_3_ch_name);
        ImageView chIcon = index == 0 ? g_fragmentView.findViewById(R.id.lo_3_grid_poster_1_ch_icon) :
                           index == 1 ? g_fragmentView.findViewById(R.id.lo_3_grid_poster_2_ch_icon) : g_fragmentView.findViewById(R.id.lo_3_grid_poster_3_ch_icon);
        View    divider = index == 0 ? g_fragmentView.findViewById(R.id.lo_divider) :
                          index == 1 ? g_fragmentView.findViewById(R.id.divider2) : g_fragmentView.findViewById(R.id.divider3);
        AdPageItem pageItem = null;
        String sourceType;

        if (index >= g_adPage.get_items().size()) {
            chName.setText("N/A");
            progName.setText("N/A");
            progTime.setText("N/A");
            set_channel_icon(chIcon, "N/A");
            return;
        }

        pageItem = g_adPage.get_items().get(index);
        sourceType = pageItem.get_source_type();

        if (SOURCE_TYPE_PROGRAM.equals(sourceType)) {
            if (null == pageItem.get_title() || pageItem.get_title().equals("null")) {
                chName.setVisibility(View.GONE);
                chIcon.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
                progTime.setVisibility(View.GONE);
                progName.setText(get().getString(R.string.no_data));
            }
            else {
                progTime.setText(get_program_time(pageItem));
                chName.setText(pageItem.get_channel_num() + " " + pageItem.get_channel_name());
                progName.setText(pageItem.get_title());
                set_channel_icon(chIcon, pageItem.get_service_id());
            }
        }
        else {
            chName.setVisibility(View.GONE);
            chIcon.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            progTime.setVisibility(View.GONE);
            progName.setText(pageItem.get_label());
        }
    }

    public void set_poster_image(ImageView posterView, int index) {
        AdPageItem pageItem = null;
        String posterUrl = null;

        if (get().isFinishing() || get().isDestroyed()) {
            Log.e(TAG, "set_poster_image: activity is finishing or destroyed");
            return;
        }
        if (getContext() == null || !isAdded()/* || getView() == null*/) {
            //Log.e(TAG, "set_poster_image: Fragment is not added or view is null");
            return;
        }

        if (index < g_adPage.get_items().size()) {
            pageItem = g_adPage.get_items().get(index);
            posterUrl = pageItem.get_poster_art_url();
        }

        try {
            if (isAdded() && getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                Glide.with(g_context).load(posterUrl)
                        .error(R.drawable.internet_error)
                        .placeholder(R.drawable.default_photo)
                        .transform(get_transform())
                        .fitCenter()
                        .skipMemoryCache(false)
                        .into(posterView);
            }
        }
        catch (IllegalArgumentException | RejectedExecutionException e) {
            Log.e(TAG, "set_poster_image: " + e.getMessage());
            posterView.setImageResource(R.drawable.default_photo);
        }
    }

    public void set_channel_icon(ImageView channelIcon, String serviceId) {
        String iconUrl = LiveTvManager.get_channel_icon_url(get(), serviceId);
        int iconResId = LiveTvManager.get_channel_icon_res_id(get(), serviceId);
        int iconWidth = get().getResources().getDimensionPixelSize(R.dimen.home_poster_3_grid_ch_icon_width);
        int iconHeight = get().getResources().getDimensionPixelSize(R.dimen.home_poster_3_grid_ch_icon_height);
        Log.d(TAG, "set_channel_icon: " + iconUrl);
        if (isAdded() && getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            Glide.with(g_context)
                    .load(iconUrl)
                    .error(iconResId)
                    .placeholder(iconResId)
                    .override(iconWidth, iconHeight)
                    .priority(Priority.HIGH)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(channelIcon);
        }
    }

    public static void show_qr_code(AppCompatActivity activity, AdPageItem pageItem) {
        QRCodeDialog qrCodeDialog = new QRCodeDialog(activity, pageItem);
        qrCodeDialog.show();
    }

    public static void show_program_detail(AppCompatActivity activity, AdPageItem pageItem) {
        HotkeyInfo hotkeyInfo = new HotkeyInfo(activity, pageItem);
        hotkeyInfo.show(HotkeyInfo.Detail.RECOMMEND, null);
    }

    /*public static void show_stream(AppCompatActivity activity, AdPageItem pageItem) {
        Log.d(TAG, "show_stream: " + pageItem.get_url());
        Intent intent = new Intent(activity, StreamActivity.class);
        intent.putExtra(StreamActivity.EXTRA_STREAM_URL, pageItem.get_url());
        activity.startActivity(intent);
    }*/

    public static void show_poster(AppCompatActivity activity, AdPageItem pageItem) {
        Log.d(TAG, "show_poster: " + pageItem.get_url());
        show_browser(activity, pageItem);
    }

    /** @noinspection StatementWithEmptyBody*/
    public static void show_browser(AppCompatActivity activity, AdPageItem pageItem) {
        String webUrl = pageItem.get_url();

        if (HomeActivity.ENABLE_WEBVIEW) {
            /*Log.d(TAG, "show_browser: " + webUrl);
            BaseWebView webView = activity.findViewById(R.id.lo_home_web_view);
            webView.loadUrl(webUrl);
            webView.setVisibility(View.VISIBLE);
            webView.fade_in();
            HomeActivity homeActivity = (HomeActivity) activity;
            homeActivity.g_listMgr.forbid_focus_list();*/
        }
        else {
            boolean is_youtube_url = webUrl.contains(YOUTUBE_DEFAULT_HOST);
            if (is_youtube_url)
                webUrl = webUrl.replace(YOUTUBE_DEFAULT_HOST, YOUTUBE_EMBED_HOST) + "?autoplay=1";
            Log.d(TAG, "show_browser: " + webUrl);
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.prime.webbrowser", "com.prime.webbrowser.MainActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("WEB_URL_KEY", webUrl);
            intent.putExtra("PERMIT", true);
            activity.startActivity(intent);
        }
    }

    public static void show_app(AppCompatActivity activity, AdPageItem pageItem) {
        String uriString = pageItem.get_intent_uri();
        String pkgName = pageItem.get_package_name();

        Log.d(TAG, "show_app: package name " + pkgName);

        if (null == uriString) {
            ActivityUtils.start_activity(activity, pageItem.get_package_name(), pageItem.get_title());
            return;
        }
        ActivityUtils.start_by_uri(activity, uriString, pkgName);
    }

    public static void open_ad(AppCompatActivity activity, AdPageItem pageItem) {
        String sourceType = pageItem.get_source_type();
        if (SOURCE_TYPE_YOUTUBE.equals(sourceType)) {
            Log.d(TAG, "open_ad: play YouTube");
            ActivityUtils.play_youtube_video(activity, pageItem);
        }
        if (SOURCE_TYPE_QRCODE.equals(sourceType)) {
            Log.d(TAG, "open_ad: show QR code");
            show_qr_code(activity, pageItem);
        }
        if (SOURCE_TYPE_PROGRAM.equals(sourceType)) {
            Log.d(TAG, "open_ad: show program detail");
            show_program_detail(activity, pageItem);
        }
        if (SOURCE_TYPE_STREAM.equals(sourceType)) {
            Log.d(TAG, "open_ad: show stream");
            ActivityUtils.start_stream(activity, pageItem);
        }
        if (SOURCE_TYPE_BROWSER.equals(sourceType)) {
            Log.d(TAG, "open_ad: show browser");
            show_browser(activity, pageItem);
        }
        if (SOURCE_TYPE_POSTER.equals(sourceType)) {
            Log.d(TAG, "open_ad: show poster");
            show_poster(activity, pageItem);
        }
        if (SOURCE_TYPE_MYVIDEO.equalsIgnoreCase(sourceType) ||
            SOURCE_TYPE_CATCHPLAY.equalsIgnoreCase(sourceType) ||
            SOURCE_TYPE_CP.equals(sourceType) ||
            SOURCE_TYPE_CP_MULTI.equals(sourceType) ||
            SOURCE_TYPE_APP.equals(sourceType)) {
            Log.d(TAG, "open_ad: open " + pageItem.get_title());
            show_app(activity, pageItem);
        }
    }

    public void draw_poster() {
        if (null == g_fragmentView) {
            Log.e(TAG, "draw_poster: null fragment view");
            g_pagerMgr.reload_pager();
            return;
        }
        if (LAYOUT_1_GRID_POSTER.equals(g_layoutType)) {
            ImageView fullPoster = g_fragmentView.findViewById(R.id.lo_1_grid_poster_img);
            set_poster_image(fullPoster, 0);
        }
        else
        if (LAYOUT_2_GRID_POSTER.equals(g_layoutType)) {
            ImageView leftPoster = g_fragmentView.findViewById(R.id.lo_2_grid_poster_1_img);
            ImageView rightPoster = g_fragmentView.findViewById(R.id.lo_2_grid_poster_2_img);
            set_poster_image(leftPoster, 0);
            set_poster_image(rightPoster, 1);
        }
        else {
            ImageView topPoster    = g_fragmentView.findViewById(R.id.lo_3_grid_poster_1_img);
            ImageView middlePoster = g_fragmentView.findViewById(R.id.lo_3_grid_poster_2_img);
            ImageView bottomPoster = g_fragmentView.findViewById(R.id.lo_3_grid_poster_3_img);
            set_poster_image(topPoster, 0);
            set_poster_image(middlePoster, 1);
            set_poster_image(bottomPoster, 2);
        }
    }

}
