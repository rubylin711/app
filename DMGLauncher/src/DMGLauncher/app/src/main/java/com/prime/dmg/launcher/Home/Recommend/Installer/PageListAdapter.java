package com.prime.dmg.launcher.Home.Recommend.Installer;

import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.ActivityUtils;
import com.prime.dmg.launcher.Utils.PackageUtils;
import com.prime.dmg.launcher.Utils.ThreadUtils;
import com.prime.dmg.launcher.Utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class PageListAdapter extends RecyclerView.Adapter<PageListAdapter.ViewHolder> {

    static String TAG;

    public static final int PAGE_SIZE = 3;
    public static final int PROGRESS_ERROR = -1;
    public static final int PROGRESS_START = -2;
    public static final int PROGRESS_STOP = -3;
    public static final int PROGRESS_INSTALLING = -4;
    public static final int PROGRESS_INSTALLED = -5;

    WeakReference<AppCompatActivity> g_ref;
    Thread g_installThread;
    InstallData g_installData;
    String g_firstButtonText;
    int g_position;

    public PageListAdapter(AppCompatActivity activity, InstallData installData) {
        TAG = getClass().getSimpleName();
        g_ref = new WeakReference<>(activity);
        g_installData = installData;
        g_firstButtonText = get().getString(R.string.detail_install);
        g_position = 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_installer_page_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        View itemView = holder.itemView;

        if (position == 0)
            init_install_page_1(itemView);
        if (position == 1)
            init_install_page_2(itemView);
        if (position == 2)
            init_install_page_3(itemView);

        on_focus_page(holder, position);
        on_key_page_1(holder, position);
        on_key_page_2(holder, position);
        on_key_page_3(holder, position);
        on_click_first_button(holder, position);
        on_click_open(holder, position);
        on_click_screenshots(holder, position);
    }

    public void on_focus_page(ViewHolder holder, int position) {
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d(TAG, "on_focus_page: [position] " + position);
                g_position = position;
                show_banner(v, position);
                focus_screenshots(v, position);
                Button.select_first_button(v, position);
            }
            else
                hide_banner(v, position);
        });
    }

    public void on_key_page_1(ViewHolder holder, int position) {
        if (position != 0)
            return;
        holder.itemView.setOnKeyListener((firstPageView, keyCode, event) -> {
            if (ACTION_UP == event.getAction())
                return false;
            switch (keyCode) {
                case KEYCODE_DPAD_RIGHT:
                case KEYCODE_DPAD_LEFT:
                    Button.select(firstPageView, keyCode);
                    break;
                case KEYCODE_DPAD_DOWN:
                    Button.unselect_all(firstPageView, position);
                    break;
                case KEYCODE_DPAD_CENTER:
                    Button.perform_click(firstPageView, position);
                    break;
            }
            return false;
        });
    }

    public void on_key_page_2(ViewHolder holder, int position) {
        if (position != 1)
            return;
        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            if (ACTION_UP == event.getAction())
                return false;
            //Log.d(TAG, "on_key_page_2: ");
            return false;
        });
    }

    public void on_key_page_3(ViewHolder holder, int position) {
        if (position != 2)
            return;
        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            if (ACTION_UP == event.getAction())
                return false;
            //Log.d(TAG, "on_key_page_3: ");
            return false;
        });
    }

    public void on_click_first_button(ViewHolder holder, int position) {
        TextView firstBtn = holder.itemView.findViewById(R.id.lo_first_button);
        firstBtn.setTag(position);
        firstBtn.setOnClickListener(v -> {
            Log.d(TAG, "on_click_first_button: [state] " + g_firstButtonText);
            if (is_button_install())
                start_install();
            else
            if (is_button_cancel())
                stop_install();
            else
            if (is_button_update())
                start_update();
            else
            if (is_button_open())
                open_app();
        });
    }

    public void on_click_open(ViewHolder holder, int position) {
        View openBtn = holder.itemView.findViewById(R.id.lo_open_btn);
        openBtn.setTag(position);
        openBtn.setOnClickListener(v -> {
            Log.d(TAG, "on_click_open: open app ...");
            ActivityUtils.start_activity(get(), g_installData.getPkgName(), g_installData.getAppName());
        });
    }

    public void on_click_screenshots(ViewHolder holder, int position) {
        View screenshotBtn = holder.itemView.findViewById(R.id.lo_screenshots_btn);
        screenshotBtn.setTag(position);
        screenshotBtn.setOnClickListener(v -> {
            int dyStart = get().getResources().getDimensionPixelSize(R.dimen.install_app_list_scroll_size_first);
            int dyNormal = get().getResources().getDimensionPixelSize(R.dimen.install_app_screenshots_total_height);
            int dyShouldBe = dyStart + dyNormal;

            g_position = 2;
            screenshotBtn.setSelected(false);
            PageListView pageListView = (PageListView) holder.itemView.getParent();
            pageListView.smoothScrollBy(0, dyShouldBe, null, PageListView.DURATION_SCROLL);
            focus_screenshots(pageListView, 100);
            Log.d(TAG, "on_click_screenshots: focus screenshots");
        });
    }

    public void init_install_page_1(View firstPage) {
        View installPage1 = firstPage.findViewById(R.id.lo_install_page_1);
        TextView title = firstPage.findViewById(R.id.lo_title);
        ImageView banner = firstPage.findViewById(R.id.lo_banner);

        String appName = g_installData.getAppName();
        boolean appInstalled = g_installData.isInstalled();
        boolean updateApp = g_installData.isUpdate();
        boolean forceUpdate = g_installData.isForceUpdate();

        installPage1.setVisibility(VISIBLE);
        title.setText(appName);
        draw_banner(banner);
        set_first_button(firstPage, R.string.detail_install);
        set_screenshots_button(firstPage);

        if (appInstalled) {
            if (updateApp) {
                set_first_button(firstPage, R.string.detail_update);
                if (forceUpdate) hide_open_button(firstPage);
                else             show_open_button(firstPage);
            }
            else {
                set_first_button(firstPage, R.string.detail_open);
                hide_open_button(firstPage);
            }
        }
    }

    public void init_install_page_2(View itemView) {
        View installPage2 = itemView.findViewById(R.id.lo_install_page_2);
        TextView appName = itemView.findViewById(R.id.lo_app_name);
        TextView description = itemView.findViewById(R.id.lo_app_description);
        TextView fulltext = itemView.findViewById(R.id.lo_app_fulltext);
        ImageView poster = itemView.findViewById(R.id.lo_app_poster);

        installPage2.setVisibility(VISIBLE);
        appName.setText(g_installData.getAppName());
        description.setText(g_installData.getDescription());
        fulltext.setText(g_installData.getFullText());
        poster.setVisibility(INVISIBLE);
        draw_banner(poster);
    }

    public void init_install_page_3(View itemView) {
        View installPage3 = itemView.findViewById(R.id.lo_install_page_3);
        ScreenshotsView screenshotsView = itemView.findViewById(R.id.lo_screenshots_view);
        ArrayList<String> screens = g_installData.getScreens();

        installPage3.setVisibility(VISIBLE);
        screenshotsView.init_list(get(), screens);
    }

    public InstallerActivity get() {
        return (InstallerActivity) g_ref.get();
    }

    @Override
    public int getItemCount() {
        return PAGE_SIZE;
    }

    public int get_position() {
        return g_position;
    }

    public void set_first_button(int resId) {
        g_firstButtonText = get().getString(resId);
        PageListView pageListView = get().findViewById(R.id.lo_page_list_view);
        View firstPage = pageListView.findViewWithTag(0);

        if (firstPage == null) {
            Log.e(TAG, "set_first_button: null view");
            pageListView.postDelayed(() -> set_first_button(resId), 1000);
            return;
        }

        TextView firstBtn = firstPage.findViewById(R.id.lo_first_button);
        firstBtn.setText(g_firstButtonText.toUpperCase());
    }

    @SuppressWarnings("unused")
    public void set_first_button(String text) {
        g_firstButtonText = text;
        PageListView pageListView = get().findViewById(R.id.lo_page_list_view);
        View firstPage = pageListView.findViewWithTag(0);

        if (firstPage == null) {
            Log.e(TAG, "set_first_button: null view");
            pageListView.postDelayed(() -> set_first_button(text), 1000);
            return;
        }

        TextView firstBtn = firstPage.findViewById(R.id.lo_first_button);
        firstBtn.setText(g_firstButtonText.toUpperCase());
    }

    public void set_first_button(View itemView, int resId) {
        g_firstButtonText = get().getString(resId);
        View installPage1 = itemView.findViewById(R.id.lo_install_page_1);

        if (installPage1 == null) {
            Log.e(TAG, "set_first_button: null view");
            itemView.postDelayed(() -> set_first_button(itemView, resId), 1000);
            return;
        }

        TextView firstBtn = installPage1.findViewById(R.id.lo_first_button);
        firstBtn.setText(g_firstButtonText.toUpperCase());
    }

    @SuppressWarnings("unused")
    public void set_first_button(View itemView, String text) {
        g_firstButtonText = text;
        View installPage1 = itemView.findViewById(R.id.lo_install_page_1);

        if (installPage1 == null) {
            Log.e(TAG, "set_first_button: null view");
            itemView.postDelayed(() -> set_first_button(itemView, text), 1000);
            return;
        }

        TextView firstBtn = installPage1.findViewById(R.id.lo_first_button);
        firstBtn.setText(text.toUpperCase());
    }

    public void set_screenshots_button(View firstPageView) {
        TextView screenshotsBtn = firstPageView.findViewById(R.id.lo_screenshots_btn);
        String screenshotsText = firstPageView.getContext().getString(R.string.detail_screen_capture);
        screenshotsBtn.setText(screenshotsText.toUpperCase());
        screenshotsBtn.setVisibility(VISIBLE);
    }

    public void set_progress(int progress) {
        show_progress(progress, null, null);
    }

    public boolean is_button_install() {
        return g_firstButtonText.equalsIgnoreCase(get().getString(R.string.detail_install));
    }

    public boolean is_button_cancel() {
        return g_firstButtonText.equalsIgnoreCase(get().getString(R.string.detail_cancel_downloading));
    }

    public boolean is_button_update() {
        return g_firstButtonText.equalsIgnoreCase(get().getString(R.string.detail_update));
    }

    public boolean is_button_open() {
        return g_firstButtonText.equalsIgnoreCase(get().getString(R.string.detail_open));
    }

    @SuppressWarnings("unused")
    public boolean is_select_install(ViewHolder holder) {
        View installBtn = holder.itemView.findViewById(R.id.lo_first_button);
        return installBtn.isSelected();
    }

    public void show_open_button() {
        PageListView pageListView = get().findViewById(R.id.lo_page_list_view);
        View firstPage = pageListView.findViewWithTag(0);

        if (firstPage == null) {
            Log.e(TAG, "show_open_button: null view");
            pageListView.postDelayed(this::show_open_button, 1000);
            return;
        }

        show_open_button(firstPage);
    }

    public void show_open_button(View firstPageView) {
        TextView openBtn = firstPageView.findViewById(R.id.lo_open_btn);
        String openText = firstPageView.getContext().getString(R.string.detail_open);
        openBtn.setText(openText.toUpperCase());
        openBtn.setVisibility(VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    public void show_progress(int progress, String downloadedSize, String totalSize) {
        PageListView pageListView = get().findViewById(R.id.lo_page_list_view);
        ProgressBar progressBar = pageListView.findViewById(R.id.lo_install_progress);
        TextView stateText = pageListView.findViewById(R.id.lo_install_progress_state);
        TextView percentText = pageListView.findViewById(R.id.lo_install_progress_percent);

        if (PROGRESS_ERROR == progress) {
            Log.d(TAG, "show_progress: ERROR");
            progressBar.setProgress(0);
            percentText.setText("(0/-1 MB) 0%");
        }
        else if (PROGRESS_START == progress) {
            Log.d(TAG, "show_progress: START");
            stateText.setText(R.string.detail_downloading);
            stateText.setVisibility(VISIBLE);
            percentText.setText("(0/-1 MB) 0%");
            percentText.setVisibility(VISIBLE);
            progressBar.setProgress(0);
            progressBar.setVisibility(VISIBLE);
        }
        else if (PROGRESS_STOP == progress) {
            Log.d(TAG, "show_progress: STOP");
            hide_progress();
        }
        else if (PROGRESS_INSTALLING == progress) {
            Log.d(TAG, "show_progress: INSTALLING");
            stateText.setText(R.string.detail_installing);
            progressBar.setIndeterminate(true);
            progressBar.setIndeterminateTintMode(PorterDuff.Mode.SRC_IN);
        }
        else if (PROGRESS_INSTALLED == progress) {
            Log.d(TAG, "show_progress: INSTALLED");
            hide_progress();
        }
        else {
            progressBar.setProgress(progress);
            percentText.setText("(" + downloadedSize + "/" + totalSize + " MB) " + progress + "%");
        }
    }

    public void show_banner(View itemView, int position) {
        if (position == 0) {
            ImageView banner = itemView.findViewById(R.id.lo_banner);
            banner.setVisibility(VISIBLE);
        }
        if (position == 1) {
            ImageView poster = itemView.findViewById(R.id.lo_app_poster);
            poster.setVisibility(VISIBLE);
        }
    }

    public void hide_open_button() {
        PageListView pageListView = get().findViewById(R.id.lo_page_list_view);
        View firstPage = pageListView.findViewWithTag(0);

        if (firstPage == null) {
            Log.e(TAG, "hide_open_button: null view");
            pageListView.postDelayed(this::hide_open_button, 1000);
            return;
        }

        hide_open_button(firstPage);
    }

    public void hide_open_button(View firstPage) {
        TextView openBtn = firstPage.findViewById(R.id.lo_open_btn);
        openBtn.setVisibility(GONE);
        Button.select_first_button(firstPage, 0);
    }

    public void hide_progress() {
        PageListView pageListView = get().findViewById(R.id.lo_page_list_view);
        ProgressBar progressBar = pageListView.findViewById(R.id.lo_install_progress);
        TextView stateText = pageListView.findViewById(R.id.lo_install_progress_state);
        TextView percentText = pageListView.findViewById(R.id.lo_install_progress_percent);
        View firstPage = pageListView.findViewWithTag(0);

        if (firstPage == null) {
            Log.e(TAG, "hide_progress: null view");
            pageListView.postDelayed(this::hide_progress, 1000);
            return;
        }

        stateText.setText("");
        stateText.setVisibility(INVISIBLE);
        percentText.setText("");
        percentText.setVisibility(INVISIBLE);
        progressBar.setProgress(0);
        progressBar.setVisibility(INVISIBLE);
    }

    public void hide_banner(View itemView, int position) {
        if (position == 0) {
            ImageView banner = itemView.findViewById(R.id.lo_banner);
            banner.setVisibility(INVISIBLE);
        }
        if (position == 1) {
            ImageView poster = itemView.findViewById(R.id.lo_app_poster);
            poster.setVisibility(INVISIBLE);
        }
    }

    public void draw_banner(ImageView posterView) {
        int posterWidth = get().getResources().getDimensionPixelSize(R.dimen.install_app_banner_width);
        int posterHeight = get().getResources().getDimensionPixelSize(R.dimen.install_app_banner_height);
        if (!get().isFinishing() && !get().isDestroyed())
            Glide.with(get())
                    .load(g_installData.getIconUrl())
                    .placeholder(R.drawable.default_photo)
                    .error(R.drawable.internet_error)
                    .override(posterWidth, posterHeight)
                    .skipMemoryCache(false)
                    .into(posterView);
    }

    public void focus_screenshots(PageListView pageListView, int delay) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!focus_screenshots(pageListView))
                    handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public boolean focus_screenshots(PageListView pageListView) {
        TextView noData = get().findViewById(R.id.lo_no_screens_hint_text);
        ArrayList<String> screens = g_installData.getScreens();

        if (get_position() != 2) {
            noData.setVisibility(GONE);
            return false;
        }

        LinearLayoutManager manager = (LinearLayoutManager) pageListView.getLayoutManager();
        if (null == manager) {
            Log.w(TAG, "focus_screenshots: null layout manager");
            return false;
        }

        View itemView = manager.findViewByPosition(get_position());
        if (null == itemView) {
            Log.w(TAG, "focus_screenshots: null item view");
            return false;
        }

        if (screens.size() <= 0)
            noData.setVisibility(VISIBLE);

        ScreenshotsView screenshotsView = itemView.findViewById(R.id.lo_screenshots_view);
        screenshotsView.requestFocus();
        return true;
    }

    public void focus_screenshots(View itemView, int position) {
        TextView noData = get().findViewById(R.id.lo_no_screens_hint_text);
        ArrayList<String> screens = g_installData.getScreens();

        if (position != 2) {
            noData.setVisibility(GONE);
            return;
        }

        if (screens.size() <= 0)
            noData.setVisibility(VISIBLE);

        ScreenshotsView screenshotsView = itemView.findViewById(R.id.lo_screenshots_view);
        screenshotsView.requestFocus();
        Log.d(TAG, "focus_screenshots: " + screenshotsView);
    }

    @SuppressLint("SetTextI18n")
    public File start_download() {
        File filePath;
        DecimalFormat decimalFormat = new DecimalFormat("0");
        String download_url = g_installData.getAppPath();
        String fileName = g_installData.getAppName() + "_versionCode_" + g_installData.getVersionCode() + ".apk";
        HttpURLConnection connection = null;

        if (InstallerActivity.FLAG_FOR_TEST) // download url
            download_url = "http://10.1.4.180/VLC_3.3.4.apk";

        try {
            // connection
            Log.d(TAG, "start_download: [file url] " + download_url);
            URL url = new URL(download_url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "start_download: connection fail");
                connection.disconnect();
                get().runOnUiThread(() -> set_progress(PROGRESS_ERROR));
                return null;
            }

            // file path
            filePath = new File(get().getExternalFilesDir(null/*Environment.DIRECTORY_DOWNLOADS*/), fileName);
            if (null == filePath.getParentFile()) {
                Log.w(TAG, "start_download: null download path");
                get().runOnUiThread(() -> set_progress(PROGRESS_ERROR));
                return null;
            }

            // download
            int fileLength = connection.getContentLength();
            InputStream input = connection.getInputStream();
            FileOutputStream output = new FileOutputStream(filePath);
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;

            while ((bytesRead = input.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                output.write(buffer, 0, bytesRead);
                // update progress & percent
                int progress = (int) (totalBytesRead * 100 / fileLength);
                String downloadedSize = decimalFormat.format(totalBytesRead / 1024 / 1024);
                String totalSize = fileLength <= 0 ? "-1" : decimalFormat.format(fileLength / 1024 / 1024);
                get().runOnUiThread(() -> show_progress(progress, downloadedSize, totalSize));
            }
            output.close();
            input.close();
        }
        catch (Exception e) {
            //e.printStackTrace();
            Log.w(TAG, "start_download: " + e);
            if (connection != null)
                connection.disconnect();
            if (e instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
                get().runOnUiThread(() -> set_progress(PROGRESS_STOP));
            }
            return null;
        }
        return filePath;
    }

    public void start_install() {
        g_installThread = new Thread(() -> {
            File tmpFile;

            while (null == (tmpFile = start_download())) {
                if (Thread.currentThread().isInterrupted() || null == g_installThread) {
                    Log.w(TAG, "start_install: interrupt download");
                    return;
                }
                else {
                    Log.w(TAG, "start_install: null file, download again after " + InstallerActivity.DOWNLOAD_DELAY_MS + " millis ...");
                    ThreadUtils.sleep(InstallerActivity.DOWNLOAD_DELAY_MS);
                }
            }

            File apkFile = tmpFile;
            Log.d(TAG, "start_install: [file path] " + apkFile.getAbsolutePath());
            get().runOnUiThread(() -> {
                set_progress(PROGRESS_INSTALLING);
                PackageUtils.install_apk(get(), apkFile);
            });
        });
        g_installThread.start();
        g_firstButtonText = get().getString(R.string.detail_cancel_downloading);
        set_first_button(R.string.detail_cancel_downloading);
        set_progress(PROGRESS_START);
    }

    public void start_update() {
        Log.d(TAG, "start_update: [package] " + g_installData.getPkgName() + ", [app name] " + g_installData.getAppName());
        start_install();
    }

    public void stop_install() {
        if (null == g_installThread)
            return;

        Log.d(TAG, "stop_install: [package] " + g_installData.getPkgName() + ", [app name] " + g_installData.getAppName());
        g_installThread.interrupt();
        g_installThread = null;

        if (!g_installData.isInstalled()) {
            Log.d(TAG, "stop_install: show INSTALL");
            set_first_button(R.string.detail_install);
            hide_open_button();
            hide_progress();
        }
        else if (g_installData.isForceUpdate()) {
            Log.d(TAG, "stop_install: show FORCE UPDATE");
            set_first_button(R.string.detail_update);
            hide_open_button();
            hide_progress();
        }
        else if (g_installData.isUpdate()) {
            Log.d(TAG, "stop_install: show UPDATE");
            set_first_button(R.string.detail_update);
            show_open_button();
            hide_progress();
        }
        else
            Log.e(TAG, "stop_install: incorrect case");

    }

    public void open_app() {
        Log.d(TAG, "open_app: [package] " + g_installData.getPkgName() + ", [app name] " + g_installData.getAppName());
        ActivityUtils.start_activity(get(), g_installData.getPkgName(), g_installData.getAppName());
    }

    public static class Button {
        public static int selectPosition = 0;

        public static boolean is_show_open_button(View firstPageView) {
            TextView openBtn = firstPageView.findViewById(R.id.lo_open_btn);
            return VISIBLE == openBtn.getVisibility();
        }

        public static int button_count(View firstPageView) {
            return is_show_open_button(firstPageView) ? 3 : 2;
        }

        public static void select(View firstPageView, int keyCode) {
            int buttonCount = button_count(firstPageView);

            if (KEYCODE_DPAD_RIGHT == keyCode)
                selectPosition++;
            if (KEYCODE_DPAD_LEFT == keyCode)
                selectPosition--;

            if (selectPosition >= buttonCount)
                selectPosition = buttonCount - 1;
            if (selectPosition < 0)
                selectPosition = 0;

            if (is_show_open_button(firstPageView)) {
                if (selectPosition == 0)    select_first_button(firstPageView, 0);
                if (selectPosition == 1)    select_open(firstPageView, 0);
                if (selectPosition == 2)    select_screenshot(firstPageView, 0);
            }
            else {
                if (selectPosition == 0)    select_first_button(firstPageView, 0);
                if (selectPosition == 1)    select_screenshot(firstPageView, 0);
            }
        }

        public static void select_first_button(View v, int position) {
            if (position != 0)
                return;

            View firstBtn = v.findViewById(R.id.lo_first_button);
            View openBtn = v.findViewById(R.id.lo_open_btn);
            View screenshotBtn = v.findViewById(R.id.lo_screenshots_btn);

            firstBtn.setSelected(true);
            openBtn.setSelected(false);
            screenshotBtn.setSelected(false);
            selectPosition = 0;
            Log.d(TAG, "select_first_button: " + firstBtn.isSelected());
        }

        public static void select_open(View v, int position) {
            if (position != 0)
                return;

            View firstBtn = v.findViewById(R.id.lo_first_button);
            View openBtn = v.findViewById(R.id.lo_open_btn);
            View screenshotBtn = v.findViewById(R.id.lo_screenshots_btn);

            firstBtn.setSelected(false);
            openBtn.setSelected(true);
            screenshotBtn.setSelected(false);
            Log.d(TAG, "select_update: " + openBtn.isSelected());
        }

        public static void select_screenshot(View v, int position) {
            if (position != 0)
                return;

            View firstBtn = v.findViewById(R.id.lo_first_button);
            View openBtn = v.findViewById(R.id.lo_open_btn);
            View screenshotBtn = v.findViewById(R.id.lo_screenshots_btn);

            firstBtn.setSelected(false);
            openBtn.setSelected(false);
            screenshotBtn.setSelected(true);
            Log.d(TAG, "select_screenshot: " + screenshotBtn.isSelected());
        }

        public static void unselect_all(View v, int position) {
            if (position != 0)
                return;

            View firstBtn = v.findViewById(R.id.lo_first_button);
            View openBtn = v.findViewById(R.id.lo_open_btn);
            View screenshotBtn = v.findViewById(R.id.lo_screenshots_btn);

            firstBtn.setSelected(false);
            openBtn.setSelected(false);
            screenshotBtn.setSelected(false);
            Log.d(TAG, "unselect_all: ");
        }

        public static void perform_click(View v, int position) {
            View firstBtn = v.findViewById(R.id.lo_first_button);
            View openBtn = v.findViewById(R.id.lo_open_btn);
            View screenshotBtn = v.findViewById(R.id.lo_screenshots_btn);
            v.setTag(position);

            if (firstBtn.isSelected())
                firstBtn.performClick();

            if (openBtn.isSelected())
                openBtn.performClick();

            if (screenshotBtn.isSelected())
                screenshotBtn.performClick();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
