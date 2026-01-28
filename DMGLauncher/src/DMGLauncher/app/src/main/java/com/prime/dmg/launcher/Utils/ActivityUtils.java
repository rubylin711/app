package com.prime.dmg.launcher.Utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.tvprovider.media.tv.TvContractCompat;

import com.prime.dmg.launcher.Home.Hotkey.HotkeyInfo;
import com.prime.dmg.launcher.Home.QRCode.QRCodeDialog;
import com.prime.dmg.launcher.Home.Recommend.Installer.InstallerActivity;
import com.prime.dmg.launcher.Home.Recommend.Activity.SideMenuActivity;
import com.prime.dmg.launcher.Home.Recommend.List.RecommendItem;
import com.prime.dmg.launcher.Home.Recommend.Stream.StreamActivity;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.JsonParser.AdPageItem;
import com.prime.dmg.launcher.Utils.JsonParser.AppPackage;
import com.prime.dmg.launcher.Utils.JsonParser.RecommendProgram;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/** @noinspection CallToPrintStackTrace, CommentedOutCode */
public class ActivityUtils {

    public static final String TAG = "ActivityUtils";

    public static final String SOURCE_TYPE_YOUTUBE  = "1001";
    public static final String SOURCE_TYPE_MYVIDEO  = "1002";
    public static final String SOURCE_TYPE_CATCHPLAY= "1003";
    public static final String SOURCE_TYPE_STREAM   = "1004";
    public static final String SOURCE_TYPE_BROWSER  = "1005";
    public static final String SOURCE_TYPE_POSTER   = "1006";
    public static final String SOURCE_TYPE_QRCODE   = "1007";
    public static final String SOURCE_TYPE_PROGRAM  = "1008";
    public static final String SOURCE_TYPE_APP      = "1009";
    public static final String SOURCE_TYPE_CP       = "1010";
    public static final String SOURCE_TYPE_CP_MULTI = "1011";

    public static final String YOUTUBE_DEFAULT_HOST = "https://youtu.be/";
    public static final String YOUTUBE_EMBED_HOST = "https://www.youtube.com/embed/";

    public static void play_youtube_video(AppCompatActivity activity, AdPageItem pageItem) {
        String videoId = pageItem.get_url();
        String appName = pageItem.get_label();
        start_youtube(activity, videoId, appName);
    }

    public static void start_youtube(Context context, String videoId) {
        start_youtube(context, videoId, "Youtube");
    }

    public static void start_youtube(Context context, String videoId, String appName) {
        Log.w(TAG, "start_youtube: [appName] " + appName);
        Settings.Global.putInt(context.getContentResolver(), Settings.Global.AUTO_TIME, 1); // 自動從網路獲取時間
        Settings.Global.putInt(context.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 1); // 自動同步時區
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + videoId));
        context.startActivity(intent);
    }

    public static void start_activity(AppCompatActivity activity, RecommendItem item) {
        String appName, appPath, pkgName, triggerUri;
        RecommendProgram.Trigger trigger;

        appName = item.get_app_name();
        appPath = item.get_app_path();
        pkgName = item.get_package_name();
        trigger = item.get_trigger();
        triggerUri = trigger != null ? trigger.get_TriggerUri() : null;

        if (is_dmg_package(activity, pkgName)) {
            Intent intent = new Intent();
            intent.setClassName(activity.getPackageName(), pkgName);

            Log.i(TAG, "start_activity: [DMG appName] = " + appName);
            activity.startActivity(intent);

            if (pkgName.equals(SideMenuActivity.class.getName()))
                activity.overridePendingTransition(R.anim.slide_in_to_left, R.anim.slide_out_to_right);
            return;
        }
        if (!TextUtils.isEmpty(appPath) && !is_app_installed(activity, pkgName)) {
            Log.i(TAG, "start_activity: with path " + appPath);
            activity.runOnUiThread(start_by_install(activity, item));
            return;
        }
        if (!TextUtils.isEmpty(triggerUri)) {
            Log.i(TAG, "start_activity: [triggerUri] " + triggerUri);
            start_by_uri(activity, triggerUri, pkgName);
            return;
        }

        Log.i(TAG, "start_activity: with package " + pkgName);
        activity.runOnUiThread(start_by_package(activity, pkgName, appName));
    }

    public static void start_activity(AppCompatActivity activity, String pkgName, String appName) {

        if ( pkgName == null )
            return ;

        if (is_dmg_package(activity, pkgName)) {
            Intent intent = new Intent();
            intent.setClassName(activity.getPackageName(), pkgName);

            Log.w(TAG, "start_activity: [DMG appName] = " + appName);
            activity.startActivity(intent);

            if (pkgName.equals(SideMenuActivity.class.getName()))
                activity.overridePendingTransition(R.anim.slide_in_to_left, R.anim.slide_out_to_right);
        }
        else
            activity.runOnUiThread(start_by_package(activity, pkgName, appName));
    }

    /** @noinspection CommentedOutCode*/
    private static void start_with_check(AppCompatActivity activity, String pkgName, String appName) {
        /*
        new Thread(() -> {

            try {
                URL url = new URL("https://play.google.com/store/apps/details?id=" + pkgName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == 200)
                    activity.runOnUiThread(start_by_package(activity, pkgName, appName));
                else
                    activity.runOnUiThread(start_by_install(activity, pkgName, appName));
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "start_with_check: [Exception] " + e);
            }
        }).start();
        */
    }

    private static Runnable start_by_package(AppCompatActivity activity, String pkgName, String appName) {
        return () -> {
            PackageManager pm = activity.getPackageManager();
            Intent intent = pm.getLeanbackLaunchIntentForPackage(pkgName);

            if (null == intent)
                intent = pm.getLaunchIntentForPackage(pkgName);

            if (null == intent)
                intent = get_play_store(pkgName);

            if (pkgName.equals("com.netflix.ninja")) {
                if (activity instanceof HomeActivity) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.netflix.com/browse?iid=f3c8f8b9"));
                    //Log.e(TAG, "start_by_package: open netflix from HomeActivity");
                }
                if (activity instanceof SideMenuActivity) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.netflix.com/browse?iid=7c4ba6a8"));
                    //Log.e(TAG, "start_by_package: open netflix from SideMenuActivity");
                }
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                intent.setPackage("com.netflix.ninja");
            }

            Log.i(TAG, "start_by_package: [appName] " + appName);
            Log.i(TAG, "start_by_package: [Uri] " + intent.toUri(Intent.URI_ANDROID_APP_SCHEME));
            activity.startActivity(intent);
        };
    }

    private static Runnable start_by_install(AppCompatActivity activity, RecommendItem item) {
        return () -> {
            Log.i(TAG, "start_by_install: with path " + item.get_app_path());
            Intent intent = new Intent(activity, InstallerActivity.class);
            intent.putExtra(InstallerActivity.KEY_APP_NAME, item.get_app_name());
            intent.putExtra(InstallerActivity.KEY_APP_PATH, item.get_app_path());
            intent.putExtra(InstallerActivity.KEY_PKG_NAME, item.get_package_name());
            intent.putExtra(InstallerActivity.KEY_ICON_URL, item.get_poster());
            intent.putExtra(InstallerActivity.KEY_FULL_TEXT, item.get_full_text());
            intent.putExtra(InstallerActivity.KEY_VERSIONCODE, item.get_app_version_code());
            intent.putExtra(InstallerActivity.KEY_DESCRIPTION, item.get_description());
            intent.putExtra(InstallerActivity.KEY_FORCE_UPDATE, item.is_force_update());
            intent.putStringArrayListExtra(InstallerActivity.KEY_SCREEN_SHOTS, (ArrayList<String>) item.get_screen_captures());
            activity.startActivity(intent);
        };
    }

    public static void start_by_uri(AppCompatActivity activity, String uriString, String pkgName) {
        Intent intent;

        if (null == uriString || uriString.equalsIgnoreCase("null")) {
            Log.w(TAG, "start_by_uri: [uri string] null");
            activity.runOnUiThread(start_by_package(activity, pkgName, pkgName));
            return;
        }

        try {
            intent = Intent.parseUri(uriString, Intent.URI_INTENT_SCHEME);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            ComponentName componentName = intent.getComponent();
            String cpName = componentName != null ? componentName.getPackageName() : "";

            Log.i(TAG, "start_by_uri: [component package] " + cpName + ", [pkgName] " + pkgName);
            if (cpName.equals(pkgName))
                activity.startActivity(intent);
            else
                activity.runOnUiThread(start_by_package(activity, pkgName, pkgName));
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void start_by_type(AppCompatActivity activity, RecommendItem item) {
        RecommendProgram.Trigger trigger = item.get_trigger();
        String url = trigger != null ? item.get_trigger().get_TriggerUri() : "null";

        Log.i(TAG, "start_by_type: [source type] " + item.get_type());
        switch (item.get_type()) {
            case SOURCE_TYPE_YOUTUBE:
                start_youtube(activity, item.get_video_id());
                break;
            case SOURCE_TYPE_MYVIDEO:
            case SOURCE_TYPE_CATCHPLAY:
            case SOURCE_TYPE_APP:
            case SOURCE_TYPE_CP:
            case SOURCE_TYPE_CP_MULTI:
                start_activity(activity, item);
                break;
            case SOURCE_TYPE_STREAM:
                start_stream(activity, url);
                break;
            case SOURCE_TYPE_BROWSER:
            case SOURCE_TYPE_POSTER:
                show_browser(activity, url);
                break;
            case SOURCE_TYPE_QRCODE:
                show_qrcode(activity, url);
                break;
            case SOURCE_TYPE_PROGRAM:
                show_program_detail(activity, item);
                break;
        }
    }

    public static void start_stream(AppCompatActivity activity, AdPageItem pageItem) {
        Log.d(TAG, "start_stream: " + pageItem.get_url());
        Intent intent = new Intent(activity, StreamActivity.class);
        intent.putExtra(StreamActivity.EXTRA_STREAM_URL, pageItem.get_url());
        activity.startActivity(intent);
    }

    public static void start_stream(AppCompatActivity activity, String streamUrl) {
        show_stream(activity, streamUrl);
    }

    public static void show_stream(AppCompatActivity activity, String streamUrl) {
        Log.d(TAG, "show_stream: " + streamUrl);
        Intent intent = new Intent(activity, StreamActivity.class);
        intent.putExtra(StreamActivity.EXTRA_STREAM_URL, streamUrl);
        activity.startActivity(intent);
    }

    public static void show_browser(AppCompatActivity activity, String webUrl) {
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

    public static void show_qrcode(AppCompatActivity activity, String url) {
        AdPageItem pageItem = new AdPageItem();
        pageItem.set_url(url);
        QRCodeDialog dialog = new QRCodeDialog(activity, pageItem);
        dialog.show();
    }

    public static void show_program_detail(AppCompatActivity activity, RecommendItem item) {
        AdPageItem pageItem = new AdPageItem();
        pageItem.set_channel_num(item.get_app_name());
        pageItem.set_channel_name(item.get_app_version_code());
        pageItem.set_title(item.get_title());
        pageItem.set_start_time(item.get_start_time());
        pageItem.set_end_time(item.get_end_time());
        pageItem.set_description(item.get_description());
        show_program_detail(activity, pageItem);
    }

    public static void show_program_detail(AppCompatActivity activity, AppPackage.Content content) {
        AdPageItem adPageItem = new AdPageItem();
        adPageItem.set_channel_num(String.valueOf(content.get_channel_num()));
        adPageItem.set_channel_name(content.get_channel_name());
        adPageItem.set_title(content.get_title());
        adPageItem.set_start_time(content.get_start_time());
        adPageItem.set_end_time(content.get_end_time());
        adPageItem.set_description(content.get_description());
        show_program_detail(activity, adPageItem);
    }

    public static void show_program_detail(AppCompatActivity activity, AdPageItem pageItem) {
        HotkeyInfo hotkeyInfo = new HotkeyInfo(activity, pageItem);
        hotkeyInfo.show(HotkeyInfo.Detail.RECOMMEND, null);
    }

    public static void show_app(AppCompatActivity activity, String uriString, String pkgName) {
        Log.d(TAG, "show_app: package name " + pkgName);
        if (null == uriString) {
            start_activity(activity, pkgName, pkgName);
            return;
        }
        start_by_uri(activity, uriString, pkgName);
    }

    public static List<ResolveInfo> get_all_app(Context context, String pkgName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent;
        List<ResolveInfo> queryIntentActivities;

        intent = new Intent(Intent.ACTION_MAIN, (Uri) null);
        if (pkgName != null && !pkgName.isEmpty())
            intent.setPackage(pkgName);
        intent.addCategory(IntentCompat.CATEGORY_LEANBACK_LAUNCHER);
        queryIntentActivities = packageManager.queryIntentActivities(intent, 0);
        queryIntentActivities.sort(new ResolveInfo.DisplayNameComparator(packageManager));

        return queryIntentActivities;
    }

    public static Drawable get_app_icon(Context context, String pkgName, boolean banner) {
        PackageManager packageManager = context.getPackageManager();
        Drawable drawable = null;

        // get app icon
        for (ResolveInfo resolveInfo : get_all_app(context, pkgName)) {
            if (resolveInfo.activityInfo.packageName.equals(pkgName)) {
                if (banner)
                    drawable = resolveInfo.activityInfo.loadBanner(packageManager);
                if (null == drawable)
                    drawable = resolveInfo.activityInfo.loadIcon(packageManager);
                if (null == drawable)
                    drawable = resolveInfo.activityInfo.loadLogo(packageManager);
                return drawable;
            }
        }
        return null;
    }

    public static List<RecommendItem> get_tv_programs(Context context, String pkgName) {
        List<RecommendItem> itemList = new ArrayList<>();
        Cursor CHANNEL = get_ch_cursor(context, pkgName);

        while (CHANNEL.moveToNext()) {
            long   ch_id     = CHANNEL.getLong(0);
            String ch_name   = CHANNEL.getString(1);
            String ch_pkg    = CHANNEL.getString(2);
            String ch_intent = CHANNEL.getString(3);

            //Log.d(TAG, "get_tv_programs: ch_id     = " + ch_id);
            //Log.d(TAG, "get_tv_programs: ch_name   = " + ch_name);
            //Log.d(TAG, "get_tv_programs: ch_pkg    = " + ch_pkg);
            //Log.d(TAG, "get_tv_programs: ch_intent = " + ch_intent);

            Cursor PROGRAM = get_pg_cursor(context, ch_id);

            while (PROGRAM.moveToNext()) {
                long   pg_id        = PROGRAM.getLong(0);
                String pg_title     = PROGRAM.getString(1);
                String pg_desc      = PROGRAM.getString(2);
                String pg_intentUri = PROGRAM.getString(3);
                String pg_poster    = PROGRAM.getString(4);

                //Log.d(TAG, "get_tv_programs: pg_id     = " + pg_id);
                //Log.d(TAG, "get_tv_programs: pg_title  = " + pg_title);
                //Log.d(TAG, "get_tv_programs: pg_desc   = " + pg_desc);
                //Log.d(TAG, "get_tv_programs: pg_intent = " + pg_intent);
                //Log.d(TAG, "get_tv_programs: pg_poster = " + pg_poster);

                itemList.add(new RecommendItem(ch_pkg, pg_id, pg_title, pg_desc, pg_poster, pg_intentUri));
            }
            PROGRAM.close();
        }
        CHANNEL.close();

        return itemList;
    }

    private static Cursor get_ch_cursor(Context context, String pkgName) {
        String CHANNEL_SELECTION;

        if (null == pkgName)
            CHANNEL_SELECTION = null;
        else {
            CHANNEL_SELECTION = TvContractCompat.Channels.COLUMN_TYPE + "='" +
                                TvContractCompat.Channels.TYPE_PREVIEW + "'";
            CHANNEL_SELECTION = TvContractCompat.Channels.COLUMN_PACKAGE_NAME + "='" + pkgName + "'";
        }

        String[] CHANNEL_PROJECTION = { TvContractCompat.Channels._ID,
                TvContractCompat.Channels.COLUMN_DISPLAY_NAME,
                TvContractCompat.Channels.COLUMN_PACKAGE_NAME,
                TvContractCompat.Channels.COLUMN_APP_LINK_INTENT_URI, };

        return context.getContentResolver().query(
                TvContractCompat.Channels.CONTENT_URI,
                CHANNEL_PROJECTION,
                CHANNEL_SELECTION,
                null, null);
    }

    @SuppressLint("RestrictedApi")
    private static Cursor get_pg_cursor(Context context, long ch_id) {
        String PROGRAMS_SELECTION = TvContractCompat.PreviewPrograms.COLUMN_CHANNEL_ID + "=? AND " +
                TvContractCompat.PreviewPrograms.COLUMN_BROWSABLE + "=1";

        String[] PROGRAMS_PROJECTION = { TvContractCompat.PreviewPrograms._ID,
                TvContractCompat.PreviewPrograms.COLUMN_TITLE,
                TvContractCompat.PreviewPrograms.COLUMN_SHORT_DESCRIPTION,
                TvContractCompat.PreviewPrograms.COLUMN_INTENT_URI,
                TvContractCompat.PreviewPrograms.COLUMN_POSTER_ART_URI };

        String[] SELECTION_ARGS = { String.valueOf(ch_id) };

        return context.getContentResolver().query(
                TvContractCompat.PreviewPrograms.CONTENT_URI,
                PROGRAMS_PROJECTION,
                PROGRAMS_SELECTION,
                SELECTION_ARGS,
                null);
    }

    private static Intent get_play_store(String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + packageName));
        intent.setPackage("com.android.vending");
        return intent;
    }

    /** @noinspection deprecation*/
    public static ComponentName get_running_task(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        return taskInfo.get(0).topActivity;
    }

    public static boolean is_running(Context context, Class<?> cls) {
        ComponentName runningTask = get_running_task(context);
        return cls.getName().equals(runningTask.getClassName());
    }

    public static boolean is_process_on_top(Context context) {
        String str;
        long currentTimeMillis = System.currentTimeMillis();
        List<UsageStats> queryUsageStats = ((UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE)).queryUsageStats(0, currentTimeMillis - 10000, currentTimeMillis);
        if (queryUsageStats != null) {
            TreeMap treeMap = new TreeMap();
            for (UsageStats usageStats : queryUsageStats) {
                treeMap.put(Long.valueOf(usageStats.getLastTimeUsed()), usageStats);
            }
            if (!treeMap.isEmpty()) {
                str = ((UsageStats) treeMap.get(treeMap.lastKey())).getPackageName();
                return str.equals(context.getPackageName());
            }
        }
        str = "";
        return str.equals(context.getPackageName());
    }

    public static boolean is_launcher_running_top(Context context) {
        boolean on_top = false ;
        ActivityManager activityManager = (ActivityManager) context.getSystemService( Context.ACTIVITY_SERVICE );
        List<ActivityManager.RunningTaskInfo> infos = activityManager.getRunningTasks(100);
//        Log.d( TAG, "top Activity PackageName = " + infos.get(0).topActivity.getPackageName() ) ;
        on_top = infos.get(0).topActivity.getPackageName().equals(HomeActivity.HOME_PACKAGE_NAME) ;
        return on_top ;
    }

    public static String get_top_activity(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService( Context.ACTIVITY_SERVICE );
        List<ActivityManager.RunningTaskInfo> infos = activityManager.getRunningTasks(100);
        //        Log.d( TAG, "top Activity PackageName = " + infos.get(0).baseActivity.getPackageName() ) ;
        return infos.get(0).topActivity.getClassName();
    }

    public static boolean is_app_installed(Context context, String pkgName) {
        for (ResolveInfo resolveInfo : get_all_app(context, pkgName)) {
            if (resolveInfo.activityInfo.packageName.equals(pkgName))
                return true;
        }
        return false;
    }

    public static boolean is_dmg_package(AppCompatActivity activity, String packageName) {
        return packageName.startsWith(activity.getPackageName());
    }

    public static boolean isRunningCtsTest(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) return false;

        for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
            Log.d(TAG, "[EN0502] processInfo.processName " + processInfo.processName);
            if (processInfo.processName.contains("com.android.cts") ||
                    processInfo.processName.contains("android.server.wm.cts")) {
                return true; // 目前正在執行 CTS 測試
            }
        }
        return false;
    }
}
