package com.prime.dmg.launcher.Home.Recommend.List;

import static com.prime.dmg.launcher.HomeActivity.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.prime.dmg.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.dmg.launcher.Home.Recommend.Activity.SideMenuActivity;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.Utils.JsonParser.JsonParser;
import com.prime.dmg.launcher.Utils.JsonParser.LauncherProfile;
import com.prime.dmg.launcher.Utils.JsonParser.RecommendProgram;
import com.prime.dmg.launcher.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListManager {

    String TAG = ListManager.class.getSimpleName();

    public static int BANNER_WIDTH;
    public static int BANNER_HEIGHT;

    WeakReference<AppCompatActivity> g_ref;
    ScrollView g_scrollView;
    ExecutorService g_executorService;

    public static List<RecommendItem> g_recommendApps;
    public static List<RecommendItem> g_localApps;

    public ListManager(HomeActivity activity) {
        g_ref = new WeakReference<>(activity);
        g_executorService = Executors.newSingleThreadExecutor();
        g_executorService.execute(() -> {
            g_scrollView = get().findViewById(R.id.lo_home_list_container);

            g_recommendApps = get_recommend_apps();
            g_localApps = get_local_apps();
            BANNER_WIDTH = get().getResources().getDimensionPixelSize(R.dimen.home_list_rcv_block_width);
            BANNER_HEIGHT = get().getResources().getDimensionPixelSize(R.dimen.home_list_rcv_block_height);

            init_list_view_popular();
            init_list_view_apps();
            init_list_view_apps_games();
            on_focus_container();
        });
    }

    public void init_list_view_popular() {
        get().g_rcvPopular = get().findViewById(R.id.lo_home_rcv_popular);
        get().g_rcvPopular.init_list_view(get(), get_popular_programs());
    }

    public void init_list_view_apps() {
        get().g_rcvApps = get().findViewById(R.id.lo_home_rcv_apps);
        get().g_rcvApps.init_list_view(get(), g_recommendApps);
    }

    public void init_list_view_apps_games() {
        get().g_rcvAppsGames = get().findViewById(R.id.lo_home_rcv_apps_games);
        get().g_rcvAppsGames.init_list_view(get(), g_localApps);
    }

    public void on_focus_container() {
        View liveTvFrame            = get().findViewById(R.id.lo_home_live_tv_frame);
        LinearLayout llPopular      = get().findViewById(R.id.lo_home_ll_popular);
        LinearLayout llApps         = get().findViewById(R.id.lo_home_ll_apps);
        LinearLayout llAppsGames    = get().findViewById(R.id.lo_home_ll_apps_games);
        RecommendListView rcvPopular   = get().g_rcvPopular;
        RecommendListView rcvApps      = get().g_rcvApps;
        RecommendListView rcvAppsGames = get().g_rcvAppsGames;

        llPopular.setOnFocusChangeListener((v,hasFocus)->{
            if (hasFocus) {
                Log.d(TAG, "on_focus_container: focus " + get().getString(R.string.home_list_recommend_title_program));
                g_scrollView.smoothScrollBy(0, -get().getResources().getDimensionPixelSize(R.dimen.home_list_rcv_apps_games_margin_bottom));
                liveTvFrame.setFocusable(true);
                rcvPopular.requestFocus();
            }
        });

        llApps.setOnFocusChangeListener((v,hasFocus)->{
            if (hasFocus) {
                Log.d(TAG, "on_focus_container: focus " + get().getString(R.string.home_list_recommend_title_app));
                rcvApps.requestFocus();
            }
        });

        llAppsGames.setOnFocusChangeListener((v,hasFocus)->{
            if (hasFocus) {
                Log.d(TAG, "on_focus_container: focus " + get().getString(R.string.home_list_recommend_title_app_and_game));
                g_scrollView.smoothScrollBy(0, get().getResources().getDimensionPixelSize(R.dimen.home_list_rcv_apps_games_margin_bottom));
                rcvAppsGames.requestFocus();
            }
        });
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    /** @noinspection ConstantValue, ExtractMethodRecommender */
    public List<RecommendItem> get_popular_programs() {
        String jsonString = ACSDataProviderHelper.get_acs_provider_data(get(), "recommends");

        if (false) {
            String jsonApp = "{\"title\":\"MyVideo熱門推薦自動\",\"start_time\":null,\"end_time\":null,\"video_id\":null,\"playlist_id\":\"0\"," +
                    "\"icon\":\"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586480650217.png\",\"app_name\":\"myVideo\"," +
                    "\"type\":\"1002\"," + // 1001 播放 YT, 1002 開啟 App, 1003 CatchPlay, 1004 串流, 1006 開啟 Web
                    "\"triggers\":[{" +
                    "\"activity_name\":\"CP\"," +
                    "\"package_params\":{\"EXTRA_VIDEO_ID\":\"\"}," + // CP使用 原則上只認"activity_name":"CP", 其餘棄用
                    //"\"trigger_uri\":\"intent:#Intent;action=2110;launchFlags=0x34000000;component=net.fetnet.fetvod.tv/.MainActivity;S.poster=%7B%22contentId%22%3A2110%2C%22contentType%22%3A4%2C%22isEST%22%3A0%2C%22streamingType%22%3A0%2C%22streamingId%22%3A0%2C%22chineseName%22%3A%22%E5%8A%89QUIZ%20ON%20THE%20BLOCK%22%2C%22englishName%22%3A%22%EC%9C%A0%ED%80%B4%EC%A6%88%EC%98%A8%EB%8D%94%EB%B8%94%EB%9F%AD%22%2C%22effectiveDate%22%3A%222021%5C%2F09%5C%2F23%22%2C%22introduction%22%3A%22%E3%80%8A%E5%85%8D%E8%B2%BB%E6%9C%83%E5%93%A1%E6%AF%8F%E5%91%A8%E5%9B%9B%E6%9B%B4%E6%96%B0%EF%BC%8C%E4%BB%98%E8%B2%BB%E6%9C%83%E5%93%A1%E5%85%8D%E5%BB%A3%E5%91%8A%E6%90%B6%E5%85%88%E7%9C%8B%E3%80%8B%5Cn%E7%82%BA%E6%8E%A2%E8%A8%AA%E4%BA%BA%E5%80%91%E7%9A%84%E6%97%A5%E5%B8%B8%E7%94%9F%E6%B4%BB%EF%BC%8C%E7%94%B1%E5%8A%89%E5%9C%A8%E9%8C%AB%E3%80%81%E6%9B%B9%E4%B8%96%E9%8E%AC%E8%A6%AA%E8%87%AA%E5%88%B0%E8%A1%97%E9%A0%AD%E8%88%87%E8%B7%AF%E4%BA%BA%E5%80%91%E9%80%B2%E8%A1%8C%E7%B0%A1%E5%96%AE%E8%AB%87%E8%A9%B1%E5%92%8C%E7%AA%81%E7%84%B6%E5%95%8F%E7%AD%94%E7%9A%84%E8%A1%97%E9%A0%AD%E5%95%8F%E7%AD%94%E7%A7%80%EF%BC%9B%E5%9C%A8%E5%B9%B3%E5%87%A1%E4%BA%BA%E7%9A%84%E4%BA%BA%E7%94%9F%E8%88%9E%E5%8F%B0%E8%A1%97%E9%A0%AD%EF%BC%8C%E7%82%BA%E4%BB%96%E5%80%91%E6%93%BA%E8%84%AB%E7%96%B2%E6%86%8A%E7%9A%84%E6%97%A5%E5%B8%B8%EF%BC%8C%E9%80%81%E4%B8%8A%E5%83%8F%E9%A9%9A%E5%96%9C%E7%8D%8E%E5%8B%B5%E4%B8%80%E6%A8%A3%E7%9A%84%E7%A6%AE%E7%89%A9%E8%88%AC%E7%9A%84%E7%89%B9%E5%88%A5%E7%9A%84%E4%B8%80%E5%A4%A9%EF%BC%81%E5%81%B6%E6%9C%89%E9%87%8D%E9%87%8F%E7%B4%9A%E5%98%89%E8%B3%93%E7%99%BB%E5%A0%B4%EF%BC%81%5Cn%E3%80%90%E5%9B%A0%E7%89%88%E6%AC%8A%E5%9B%A0%E7%B4%A0%EF%BC%8C%E8%88%8A%E6%9C%89%E9%9B%86%E6%95%B8%E5%B0%87%E9%80%90%E9%9B%86%E4%B8%8B%E6%9E%B6%E3%80%91%22%2C%22largeImageUrl%22%3A%22%5C%2FHOTGROUP%5C%2FWIDE%5C%2F2110%5C%2F2110_14850.jpg%22%2C%22themeImageUrl%22%3A%22%5C%2FHOTGROUP%5C%2F3TO2%5C%2F2110%5C%2F2110_10718.jpg%22%2C%22imageUrl%22%3A%22%5C%2FDRAMA%5C%2F2110%5C%2F2110_15038.jpg%22%2C%22lancherImageUrl%22%3A%22%5C%2FHOTGROUP%5C%2FLAUNCHER%5C%2F2110%5C%2F2110_19329.jpg%22%2C%22duration%22%3A%22100%22%2C%22year%22%3A%222021%22%2C%22area%22%3A%22%E9%9F%93%E5%9C%8B%22%2C%22expireDate%22%3A%222027%5C%2F04%5C%2F17%22%2C%22paymentTagList%22%3A%22%5B1%5D%22%2C%22propertyTagList%22%3A%22%5B3%5D%22%2C%22newestEpisode%22%3A%22270%22%2C%22totalEpisode%22%3A173%2C%22isEmpty%22%3Afalse%2C%22percent%22%3A-1%2C%22episode%22%3A%22%22%2C%22stopTime%22%3A0%2C%22templateId%22%3A-1%2C%22url%22%3A%22%22%2C%22position%22%3A0%2C%22rankTag%22%3A%22-1%22%2C%22rating%22%3A1%2C%22isShowAd%22%3A0%2C%22enableAd%22%3Afalse%2C%22relatedList%22%3A%5B%5D%2C%22sort%22%3A-1%2C%22more%22%3A0%2C%22statusTag%22%3A-1%2C%22program%22%3A%22%5C%22%5C%22%22%2C%22isLive%22%3Afalse%2C%22eventChannel%22%3Afalse%2C%22episodeName%22%3A%22%22%2C%22streamingUrl%22%3A%22%22%2C%22loginMin%22%3A%22-1%22%2C%22updateEpisode%22%3A%22%22%2C%22smallChannelImageUrl%22%3A%22%3F1732167062699%22%2C%22channelImageUrl%22%3A%22%3F1732167062699%22%2C%22autoPlay%22%3A-1%2C%22fridayScore%22%3A4.9%7D;end\"," +
                    "\"trigger_uri\":\"intent:#Intent;action=212613;component=com.taiwanmobile.myVideotv/com.taiwanmobile.activity.SearchDeepLink;S.VIDEOTITLE=%E5%AF%86%E5%BC%92%E9%81%8A%E6%88%B2;S.DEEPACTION=Content%3A212613%7C0;i.NOTIFICATION_ID=212613;S.VIDEOMAINCATORY=%E9%9B%BB%E5%BD%B1;end\"," +
                    "\"package_name\":\"com.taiwanmobile.myVideotv\"," + // 開啟 App 使用
                    "\"min_version_code\":0," + // 棄用, 過去有碰過3rd Party會因版本而開啟方式不同
                    "\"max_version_code\":0," + // 棄用, 過去有碰過3rd Party會因版本而開啟方式不同
                    "\"intent_flags\":[\"0\"]}]},"; // 棄用, 過去有碰過3rd Party會因版本而開啟方式不同, 所對應的開啟參數
            jsonString = new StringBuilder(jsonString).insert(1, jsonApp).toString();
        }
        if (false) {
            jsonString = "[{\"sequence\":1,\"title\":\"【大大寬頻】光纖就是快！愛情好光鮮_ 2.視訊篇\",\"start_time\":null,\"end_time\":null,\"video_id\":\"FhQ2L1tAIEw\",\"playlist_id\":null,\"icon\":\"https://i.ytimg.com/vi/FhQ2L1tAIEw/hqdefault.jpg\",\"app_name\":\"Youtube\",\"type\":\"1001\",\"videoId\":\"FhQ2L1tAIEw\",\"triggers\":[]}," +
                    "{\"sequence\":2,\"title\":\"CH18 BBC Earth 舞動西西里\",\"start_time\":null,\"end_time\":null,\"video_id\":\"aUOpnYxPmtM\",\"playlist_id\":null,\"icon\":\"https://i.ytimg.com/vi/aUOpnYxPmtM/hqdefault.jpg\",\"app_name\":\"Youtube\",\"type\":\"1001\",\"videoId\":\"aUOpnYxPmtM\",\"triggers\":[]}," +
                    "{\"sequence\":3,\"title\":\"CH23 小尼克頻道 最新熱門節目推薦 \",\"start_time\":null,\"end_time\":null,\"video_id\":\"2uDD8ZRHHaA\",\"playlist_id\":null,\"icon\":\"https://i.ytimg.com/vi/2uDD8ZRHHaA/hqdefault.jpg\",\"app_name\":\"Youtube\",\"type\":\"1001\",\"videoId\":\"2uDD8ZRHHaA\",\"triggers\":[]}," +
                    "{\"sequence\":4,\"title\":\"CH67 AXN 蜘蛛人：返校日\",\"start_time\":null,\"end_time\":null,\"video_id\":\"bkBgZbZ67jI\",\"playlist_id\":null,\"icon\":\"https://i.ytimg.com/vi/bkBgZbZ67jI/hqdefault.jpg\",\"app_name\":\"Youtube\",\"type\":\"1001\",\"videoId\":\"bkBgZbZ67jI\",\"triggers\":[]}," +
                    "{\"sequence\":5,\"title\":\"CH65 HBO 乘客\",\"start_time\":null,\"end_time\":null,\"video_id\":\"jCLYIGYbkrI\",\"playlist_id\":null,\"icon\":\"https://i.ytimg.com/vi/jCLYIGYbkrI/hqdefault.jpg\",\"app_name\":\"Youtube\",\"type\":\"1001\",\"videoId\":\"jCLYIGYbkrI\",\"triggers\":[]}," +
                    "{\"sequence\":6,\"title\":\"CH61 CATCHPLAY電影台 全境炸裂\",\"start_time\":null,\"end_time\":null,\"video_id\":\"-xDMReEA1Qc\",\"playlist_id\":null,\"icon\":\"https://i.ytimg.com/vi/-xDMReEA1Qc/hqdefault.jpg\",\"app_name\":\"Youtube\",\"type\":\"1001\",\"videoId\":\"-xDMReEA1Qc\",\"triggers\":[]}," +
                    "{\"sequence\":7,\"title\":\"CH253 CBeebies 蔬果達人秀\",\"start_time\":null,\"end_time\":null,\"video_id\":\"XU9bwhTh-x4\",\"playlist_id\":null,\"icon\":\"https://i.ytimg.com/vi/XU9bwhTh-x4/hqdefault.jpg\",\"app_name\":\"Youtube\",\"type\":\"1001\",\"videoId\":\"XU9bwhTh-x4\",\"triggers\":[]}]";
        }
        List<RecommendProgram> recommendPrograms = JsonParser.parse_popular_programs(jsonString);
        List<RecommendItem> recommendItems = new ArrayList<>();

        for (RecommendProgram recommendProgram : recommendPrograms) {
            RecommendItem item = new RecommendItem(
                    recommendProgram.get_sequence(),
                    recommendProgram.get_title(),
                    recommendProgram.get_start_time(),
                    recommendProgram.get_end_time(),
                    recommendProgram.get_video_id(),
                    recommendProgram.get_playlist_id(),
                    recommendProgram.get_icon(),
                    recommendProgram.get_app_name(),
                    recommendProgram.get_type(),
                    recommendProgram.get_videoId(),
                    recommendProgram.get_trigger(),
                    recommendProgram.get_triggers()
            );
            if (item.get_trigger() != null)
                item.set_package_name(item.get_trigger().get_PackageName());
            recommendItems.add(item);
        }

        return recommendItems;
    }

    /** @noinspection ConstantValue*/
    @SuppressLint("QueryPermissionsNeeded")
    public List<RecommendItem> get_recommend_apps() {
        String jsonString = ACSDataProviderHelper.get_acs_provider_data(get(), "launcher_profile");

        if (false) {
            jsonString = "{\"btMac\":null,\"dlna\":false,\"miracast\":false,\"otaSyncInterval\":14400,\"reportInterval\":3600,\"bPlusTrigger\":\"off\",\"hdmiOutControl\":\"on\",\"hdmiOutControlStatus\":\"on\",\"hdmiPatch\":false,\"projectName\":\"TBC\",\"modelName\":\"TATV-8000\"," +
                    "\"androidVersion\":\"11\",\"condition1\":\"320681\",\"condition2\":\"1\",\"condition3\":null,\"condition4\":null,\"condition5\":null,\"otaConfigTime\":\"1724205244801\",\"appConfigTime\":\"1724205244801\",\"otaStarted\":\"2024-04-23 10:00\",\"otaDrivenBy\":\"system\",\"otaAfterStandby\":1," +
                    "\"otaAfterBoot\":1,\"otaAfterImmediately\":0,\"otaAfterTime\":\"0000-0600\",\"deviceLastFwVersionId\":171,\"videoRecommendationId\":41,\"scheduleProfileId\":1,\"appProfileId\":39,\"recommendationProfileId\":761,\"hotVideoProfileId\":1,\"networkSwitch\":1,\"ubaUploadMin\":null," +
                    "\"logSize\":\"100\",\"ubaForceUploadHour\":null,\"ledStyle\":\"7Seg\",\"report_interval\":14400,\"enable_log\":false,\"auto_log\":false,\"group_config_version\":\"20191008203911\",\"UBARecommendation\":true,\"UBARemoteButton\":true,\"UBALaunchPoint\":true,\"UBADirectionKey\":true," +
                    "\"UBACollect\":true,\"sw_group_name\":\"Default\",\"sw_sync_interval\":14400,\"app_group_name\":\"Default\",\"app_sync_interval\":3600,\"launcher_sync_interval\":14400,\"HDMIOutControl\":\"on\",\"BplusTrigger\":\"off\"," +
                    "\"launcher_profile\":[{\"appName\":\"Prime Video(5.7.13+v14.0.0.397-armv7a)\",\"packageName\":\"com.amazon.amazonvideo.livingroom\",\"thumbnail\":\"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1659963445442.png\",\"iconPriority\":0,\"activityName\":null,\"productId\":\"\",\"packageParams\":{},\"description\":\"\",\"fullText\":\"\",\"appPath\":\"\",\"appVersionCode\":\"505007013\",\"apkLabel\":\"Prime Video\",\"screenCaptures\":[],\"displayPosition\":1,\"forceUpdate\":false}," +
                    "{\"appName\":\"HBO GO(1.0.60)\",\"packageName\":\"com.hbo.asia.androidtv\",\"thumbnail\":\"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1588849192037.png\",\"iconPriority\":0,\"activityName\":null,\"productId\":\"\",\"packageParams\":{},\"description\":\"\",\"fullText\":\"\",\"appPath\":\"\",\"appVersionCode\":\"1811261503\",\"apkLabel\":\"HBO GO\",\"screenCaptures\":[],\"displayPosition\":1,\"forceUpdate\":false}," +
                    "{\"appName\":\"myVideo(4.0.0.11)\",\"packageName\":\"com.taiwanmobile.myVideotv\",\"thumbnail\":\"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1648799080664.jpg\",\"iconPriority\":1,\"activityName\":null,\"productId\":\"\",\"packageParams\":{},\"description\":\"\",\"fullText\":\"\",\"appPath\":\"\",\"appVersionCode\":\"181\",\"apkLabel\":\"myVideo\",\"screenCaptures\":[],\"displayPosition\":1,\"forceUpdate\":false}," +
                    "{\"appName\":\"Friday影音\",\"packageName\":\"net.fetnet.fetvod.tv\",\"thumbnail\":\"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1582798646071.png\",\"iconPriority\":0,\"activityName\":\"\",\"productId\":\"\",\"packageParams\":{},\"description\":\"\",\"fullText\":\"\",\"appPath\":\"\",\"appVersionCode\":\"56\",\"apkLabel\":\"friDay影音\",\"screenCaptures\":[],\"displayPosition\":1,\"forceUpdate\":false}," +
                    "{\"appName\":\"com.litv.cable.home(3.7.29-Cable)\",\"packageName\":\"com.litv.cable.home\",\"thumbnail\":\"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1600681975745.png\",\"iconPriority\":0,\"activityName\":null,\"productId\":\"\",\"packageParams\":{},\"description\":\"\",\"fullText\":\"\",\"appPath\":\"\",\"appVersionCode\":\"30729\",\"apkLabel\":\"com.litv.cable.home\",\"screenCaptures\":[],\"displayPosition\":1,\"forceUpdate\":false}," +
                    "{\"appName\":\"MoveVTV(1.6)\",\"packageName\":\"com.hihealth.movevTv\",\"thumbnail\":\"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1636453497296.png\",\"iconPriority\":0,\"activityName\":null,\"productId\":\"\",\"packageParams\":{},\"description\":null,\"fullText\":null,\"appPath\":\"https://www.tbc.net.tw/media/MoveVTV1.6.apk\",\"appVersionCode\":\"8\",\"apkLabel\":\"MoveVTV\",\"screenCaptures\":[],\"displayPosition\":1,\"forceUpdate\":true}," +
                    "{\"appName\":\"國圖到你家(2.05)\",\"packageName\":\"tw.org.itri.nclibrary\",\"thumbnail\":\"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1645430183724.png\",\"iconPriority\":0,\"activityName\":null,\"productId\":\"\",\"packageParams\":{},\"description\":\"\",\"fullText\":\"\",\"appPath\":\"\",\"appVersionCode\":\"205\",\"apkLabel\":\"國圖到你家\",\"screenCaptures\":[],\"displayPosition\":1,\"forceUpdate\":false}," +
                    "{\"appName\":\"CatchPlay\",\"packageName\":\"com.catchplay.asiaplay.common.tv\",\"thumbnail\":\"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1582798799999.png\",\"iconPriority\":0,\"activityName\":\"\",\"productId\":\"\",\"packageParams\":{},\"description\":\"\",\"fullText\":\"\",\"appPath\":\"\",\"appVersionCode\":\"89\",\"apkLabel\":\"CATCHPLAY+\",\"screenCaptures\":[],\"displayPosition\":1,\"forceUpdate\":false}," +
                    "{\"appName\":\"KKTV\",\"packageName\":\"com.kktv.kktv.tv\",\"thumbnail\":\"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1590398011181.png\",\"iconPriority\":0,\"activityName\":\"\",\"productId\":\"\",\"packageParams\":{},\"description\":\"\",\"fullText\":\"\",\"appPath\":\"\",\"appVersionCode\":\"86\",\"apkLabel\":\"com.kktv.kktv.tv\",\"screenCaptures\":[],\"displayPosition\":1,\"forceUpdate\":false}," +
                    "{\"appName\":\"三竹股市(1.87)\",\"packageName\":\"com.mitake.tv\",\"thumbnail\":\"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1699427028278.jpg\",\"iconPriority\":0,\"activityName\":null,\"productId\":\"\",\"packageParams\":{},\"description\":\"\",\"fullText\":\"\",\"appPath\":\"\",\"appVersionCode\":\"14\",\"apkLabel\":\"三竹股市\",\"screenCaptures\":[],\"displayPosition\":1,\"forceUpdate\":false}," +
                    "{\"appName\":\"MABOW TV(v2.69.2)\",\"packageName\":\"tw.com.mabow.liveinteraction\",\"thumbnail\":\"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1599629663069.png\",\"iconPriority\":0,\"activityName\":null,\"productId\":\"\",\"packageParams\":{},\"description\":\"\",\"fullText\":\"\",\"appPath\":\"\",\"appVersionCode\":\"1422\",\"apkLabel\":\"MABOW TV\",\"screenCaptures\":[],\"displayPosition\":1,\"forceUpdate\":false}]}";
        }
        //LauncherInfo launcherInfo = JsonParser.parse_launcher_info(jsonString);
        List<LauncherProfile> launcherProfiles = JsonParser.parse_recommend_apps(jsonString); //launcherInfo.get_launcher_profiles();
        List<RecommendItem> recommendApps = new ArrayList<>();

        for (LauncherProfile profile : launcherProfiles) {
            RecommendItem recommendItem = new RecommendItem(
                    profile.get_app_name(),
                    profile.get_package_name(),
                    profile.get_thumbnail(),
                    profile.get_icon_priority(),
                    profile.get_activity_name(),
                    profile.get_product_id(),
                    profile.get_package_params(),
                    profile.get_description(),
                    profile.get_full_text(),
                    profile.get_app_path(),
                    profile.get_app_version_code(),
                    profile.get_apk_label(),
                    profile.get_screen_captures(),
                    profile.get_display_position(),
                    profile.is_force_update()
            );
            recommendApps.add(recommendItem);
        }

        // ALL APPs
        RecommendItem ALL_APPs = new RecommendItem();
        ALL_APPs.set_app_name(SideMenuActivity.APP_NAME);
        ALL_APPs.set_thumbnail_res_id(R.drawable.app_all_apps);
        ALL_APPs.set_package_name(SideMenuActivity.class.getName());
        recommendApps.add(0, ALL_APPs);

        // Netflix
        PackageManager pm = get().getPackageManager();
        Intent queryApps = new Intent(Intent.ACTION_MAIN, null);
        queryApps.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        for (ResolveInfo info : pm.queryIntentActivities(queryApps, 0)) {
            if (!PKG_NETFLIX.equals(info.activityInfo.packageName))
                continue;
            RecommendItem Netflix = new RecommendItem();
            Netflix.set_app_name((String) info.loadLabel(pm));
            Netflix.set_package_name(info.activityInfo.packageName);
            Netflix.set_banner(info.activityInfo.loadLogo(pm));
            recommendApps.add(0, Netflix);
        }

        return recommendApps;
    }

    @SuppressLint("QueryPermissionsNeeded")
    public List<RecommendItem> get_local_apps() {
        List<RecommendItem> itemList = new ArrayList<>();
        PackageManager pkgMgr = get().getPackageManager();
        String app_label, app_pkg;
        Intent queryApps, app_intent;
        Drawable app_banner;

        for (int i = 0; i < 2; i++) {
            queryApps = new Intent(Intent.ACTION_MAIN, null);
            if (i == 0) queryApps.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
            else        queryApps.addCategory(Intent.CATEGORY_LAUNCHER);

            for (ResolveInfo info : pkgMgr.queryIntentActivities(queryApps, 0)) {
                if (is_skip_app(info, itemList))
                    continue;
                app_label  = (String) info.loadLabel(pkgMgr);
                app_pkg    = info.activityInfo.packageName;
                app_banner = get_app_banner(info, pkgMgr);
                app_intent = get_app_intent(info, pkgMgr);
                //Log.d(TAG, "get_app: [app] " + app_label + ", [category] " + get_app_category(info));
                itemList.add(new RecommendItem(app_label, app_banner, app_pkg, app_intent));
            }
        }
        sort_app(itemList);
        return itemList;
    }

    public Drawable get_app_banner(ResolveInfo info, PackageManager pkgMgr) {
        Drawable app_banner = info.activityInfo.loadBanner(pkgMgr);
        if (app_banner == null)
            app_banner = info.activityInfo.loadIcon(pkgMgr);
        return app_banner;
    }

    public Intent get_app_intent(ResolveInfo info, PackageManager pkgMgr) {
        Intent app_intent = pkgMgr.getLeanbackLaunchIntentForPackage(info.activityInfo.packageName);
        if (app_intent == null)
            app_intent = pkgMgr.getLaunchIntentForPackage(info.activityInfo.packageName);
        return app_intent;
    }

    public Set<String> get_app_category(ResolveInfo info) {
        PackageManager pm = get().getPackageManager();
        Intent appIntent = pm.getLeanbackLaunchIntentForPackage(info.activityInfo.packageName);
        Set<String> categories = new HashSet<>();

        if (appIntent == null)
            appIntent = pm.getLaunchIntentForPackage(info.activityInfo.packageName);

        if (appIntent != null)
            categories = appIntent.getCategories();

        return categories;
    }

    public List<RecommendItem> get_app_google(List<RecommendItem> itemList) {
        List<RecommendItem> newList = new ArrayList<>();
        RecommendItem item;

        item = find_app(itemList, PKG_GOOGLE_PLAY);
        if (item != null)
            newList.add(item);

        item = find_app(itemList, PKG_GOOGLE_MOVIES);
        if (item != null)
            newList.add(item);

        item = find_app(itemList, PKG_GOOGLE_YOUTUBE);
        if (item != null)
            newList.add(item);

        item = find_app(itemList, PKG_GOOGLE_YOUTUBE_MUSIC);
        if (item != null)
            newList.add(item);

        item = find_app(itemList, PKG_GOOGLE_GAMES);
        if (item != null)
            newList.add(item);

        return newList;
    }

    private List<String> get_skip_list() {
        List<String> skipList = new ArrayList<>();
        skipList.add("com.prime.otaupdater");
        skipList.add("rtk.perfmon");
        skipList.add("com.realtek.tunerhaldemo");
        skipList.add("com.ddkp.ir");
        return skipList;
    }

    public void set_visible_list(boolean visible) {
        ScrollView container = get().findViewById(R.id.lo_home_list_container);
        container.setVisibility(
                visible ? View.VISIBLE
                        : View.GONE);
    }

    public boolean is_app_existed(String pkgName) {
        RecommendAdapter adapter = (RecommendAdapter) get().g_rcvAppsGames.getAdapter();
        if (adapter != null)
            return adapter.is_app_existed(pkgName);
        return false;
    }

    public void scroll_to_top() {
        if (!g_scrollView.hasFocus())
            return;
        Log.d(TAG, "scroll_to_top: goto first list");
        g_scrollView.scrollBy(0, -g_scrollView.getScrollY());
    }

    public void show_list() {
        set_visible_list(true);
    }

    public void hide_list() {
        set_visible_list(false);
    }

    /** @noinspection CollectionAddAllCanBeReplacedWithConstructor*/
    public void sort_app(List<RecommendItem> itemList) {
        List<RecommendItem> newList = new ArrayList<>();
        List<String> skipList = get_skip_list();

        // sort from A to Z
        itemList.sort((o1, o2) -> o1.get_app_name().compareToIgnoreCase(o2.get_app_name()));

        // add Google
        newList.addAll(get_app_google(itemList));

        // add other apps
        for (RecommendItem item : itemList) {
            if (is_google(item))
                continue;

            String pkgName = null;
            if (item.get_intent() != null)
                pkgName = item.get_intent().getPackage();

            if (skipList.contains(pkgName))
                continue;

            newList.add(item);
        }

        // save new list to item list
        itemList.clear();
        itemList.addAll(newList);

        // insert ALL APPs
        String   app_label  = SideMenuActivity.APP_NAME;
        Drawable app_banner = ContextCompat.getDrawable(get(), R.drawable.app_all_apps);
        String   app_pkg    = get().getPackageName();
        Intent   app_intent = new Intent(get(), SideMenuActivity.class);
        itemList.add(0, new RecommendItem(app_label, app_banner, app_pkg, app_intent));
    }

    public RecommendItem find_app(List<RecommendItem> itemList, String pkg_name) {
        for (RecommendItem item : itemList) {
            if (pkg_name.equals(item.g_pkg))
                return item;
        }
        return null;
    }

    public boolean is_same_label(List<RecommendItem> itemList, ResolveInfo newInfo) {
        PackageManager pm = get().getPackageManager();
        String newLabel = (String) newInfo.loadLabel(pm);

        for (RecommendItem item : itemList) {
            if (item.g_label != null &&
                item.g_label.equals(newLabel))
                return true;
        }
        return false;
    }

    /** @noinspection RedundantIfStatement*/
    public boolean is_skip_app(ResolveInfo info, List<RecommendItem> itemList) {
        String app_pkg = info.activityInfo.packageName;

        if (app_pkg.equals(get().getPackageName()))
            return true; // skip self app

        if (is_same_label(itemList, info))
            return true; // skip same app

        return false;
    }

    public boolean is_google(RecommendItem item) {
        return item.g_pkg.equals(HomeActivity.PKG_GOOGLE_PLAY) ||
               item.g_pkg.equals(HomeActivity.PKG_GOOGLE_MOVIES) ||
               item.g_pkg.equals(HomeActivity.PKG_GOOGLE_YOUTUBE) ||
               item.g_pkg.equals(HomeActivity.PKG_GOOGLE_YOUTUBE_MUSIC) ||
               item.g_pkg.equals(HomeActivity.PKG_GOOGLE_GAMES);
    }

    public void forbid_focus_list() {
        g_scrollView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    public void allow_focus_list() {
        g_scrollView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
    }

    public void update_popular(boolean fromACS) {
        Log.d(TAG, "update_popular: from ACS: " + fromACS);
        init_list_view_popular(); // update recommends
    }

    public void update_launcher_profile(boolean fromACS) {
        Log.d(TAG, "update_launcher_profile: from ACS: " + fromACS);
        RecommendListView appListView = get().g_rcvApps;		
        g_recommendApps = get_recommend_apps();
        init_list_view_apps(); // update launcher profile
        appListView.post(() -> {
            appListView.g_dxRuntime = 0;
            appListView.scrollBy(appListView.g_dxShouldBe, 0);
            appListView.focus_child();
        });
    }

    private void update_apps_and_games() {
        g_localApps = get_local_apps();
        get().g_rcvAppsGames.init_list_view(get(), g_localApps);
    }

    public void add_local_app(String pkgName) {
        RecommendAdapter adapter = (RecommendAdapter) get().g_rcvAppsGames.getAdapter();
        if (adapter != null) {
            g_localApps = get_local_apps();
            adapter.add_local_app(g_localApps, pkgName);
        }
    }

    public void remove_local_app(String pkgName) {
        RecommendAdapter adapter = (RecommendAdapter) get().g_rcvAppsGames.getAdapter();
        if (adapter != null) {
            g_localApps = get_local_apps();
            adapter.remove_local_app(g_localApps, pkgName);
        }
    }

    public void cancel_retry_draw() {
        RecommendAdapter adapter;
        adapter = (RecommendAdapter) get().g_rcvPopular.getAdapter();
        if (adapter != null)
            adapter.cancel_retry_draw();
        adapter = (RecommendAdapter) get().g_rcvApps.getAdapter();
        if (adapter != null)
            adapter.cancel_retry_draw();
        adapter = (RecommendAdapter) get().g_rcvAppsGames.getAdapter();
        if (adapter != null)
            adapter.cancel_retry_draw();
    }

}
