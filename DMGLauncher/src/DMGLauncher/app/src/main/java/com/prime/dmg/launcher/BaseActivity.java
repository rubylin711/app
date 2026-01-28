package com.prime.dmg.launcher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.CustomView.EventDialog;
import com.prime.dmg.launcher.CustomView.ProgressDialog;
import com.prime.dmg.launcher.CustomView.Snakebar;
import com.prime.dmg.launcher.EPG.EpgActivity;
import com.prime.dmg.launcher.Home.BlockChannel.BlockedChannel;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyRecord;
import com.prime.dmg.launcher.Home.LiveTV.LiveTvManager;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.Home.Recommend.Activity.SideMenuActivity;
import com.prime.dmg.launcher.Home.Recommend.List.RecommendAdapter;
import com.prime.dmg.launcher.PVR.Conflict.AboutToStartDialog;
import com.prime.dmg.launcher.PVR.Conflict.ConflictDialog;
import com.prime.dmg.launcher.PVR.Management.RecordProgramsActivity;
import com.prime.dmg.launcher.Utils.ActivityUtils;
import com.prime.dmg.launcher.Utils.UsbUtils;
import com.prime.dmg.launcher.Utils.Utils;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.PrimeBroadcastReceiver;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.PrimeTimerReceiver;
import com.prime.dtv.PrimeVolumeReceiver;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.PvrRecFileInfo;
import com.prime.dtv.sysdata.PvrRecIdx;
import com.prime.dtv.utils.RecChannelInfo;
import com.prime.dtv.utils.TVMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseActivity extends AppCompatActivity implements PrimeBroadcastReceiver.Callback {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final String EXTRA_TEMP_DISABLE_REC_SPACE_LIMIT_DIALOG
            = "com.prime.dmg.launcher.TEMP_DISABLE_REC_SPACE_LIMIT_DIALOG";
    private static Handler gThreadHandler;
    private static Handler g_Handler;
    private PrimeDtv gPrimeDtv;
    private LiveTvManager gLiveTvMgr;
    private HotkeyRecord gMsgDialog;
    private Thread gDeleteRecThread;
    private EventDialog gEventDialog;
    private ChannelChangeManager gChChangeManager;
    private boolean gShowRecSpaceLimitDialog = true;
    private CountDownTimer gCountDownTimer;

    // for conflict dialog
    public static final boolean FOR_TEST_BOOK_CONFLICT = false;
    private ConflictDialog gConflictDialog;
    private AboutToStartDialog gAboutToStartDialog;
    private static Map<String, CountDownTimer> gActiveTimes;
    private static List<BookInfo> gFakeBookList;

    public static int gGlobalKey;
    public static final String SYSTEM_PROPERTY_GLOBAL_KEY   = "persist.sys.prime.global_key";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gPrimeDtv = HomeApplication.get_prime_dtv();
        gChChangeManager = ChannelChangeManager.get_instance(this);
        gActiveTimes = new HashMap<>();

        Intent intent = getIntent();
        if (intent.getBooleanExtra(EXTRA_TEMP_DISABLE_REC_SPACE_LIMIT_DIALOG, false)) {
            temp_disable_rec_space_limit_dialog();
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        PrimeBroadcastReceiver.set_callback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Utils.keep_screen_on(this, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        String topClassName = HomeApplication.get_top_class_name();

        Utils.keep_screen_on(this, false);

        gGlobalKey = HomeActivity.get_global_key();
        Log.d(TAG, "onPause: [global key] " + gGlobalKey);
        Log.d(TAG, "topClassName "+topClassName);
        if (topClassName.equals(HomeActivity.class.getName()) ||
            topClassName.equals(EpgActivity.class.getName()) ||
            topClassName.equals(SideMenuActivity.class.getName())) {
            // check global key
            on_press_global_key(gGlobalKey);

            // check if is TV, HOME or Side Menu
            if (gGlobalKey == KeyEvent.KEYCODE_TV ||
                gGlobalKey == KeyEvent.KEYCODE_HOME ||
                gGlobalKey == KeyEvent.KEYCODE_BACK ||
                gGlobalKey == KeyEvent.KEYCODE_GUIDE ||
                RecommendAdapter.is_clicked_all_app())
                Log.i(TAG, "onPause: KEYCODE_TV / KEYCODE_HOME / Side Menu / KEYCODE_GUIDE");
            else {
                if (gGlobalKey != KeyEvent.KEYCODE_POWER)
                    gGlobalKey = KeyEvent.KEYCODE_UNKNOWN;
                Log.d(TAG, "onPause: Stop play AV, reset [global key] " + gGlobalKey);
                send_message(gThreadHandler, HomeActivity.TYPE_HANDLE_DTV_PAUSE);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        set_global_key(keyCode);
        return super.onKeyDown(keyCode, event);
    }

    // trigger when press assist
    @Override
    public void onProvideAssistData(Bundle data) {
        super.onProvideAssistData(data);
        set_global_key(KeyEvent.KEYCODE_ASSIST);
    }

    @Override
    public void on_power_off() {
        BaseActivity.gGlobalKey = KeyEvent.KEYCODE_POWER;
        set_global_key(BaseActivity.gGlobalKey);
        Log.d(TAG, "on_power_off: [global key] " + BaseActivity.gGlobalKey);
        send_message(gThreadHandler, HomeActivity.TYPE_HANDLE_DTV_PAUSE);
    }

    public static void set_global_key(int value) {
        //LogUtils.d("IN");
        //Log.i(TAG, "set_global_key: value = " + value);
        SystemProperties.set(SYSTEM_PROPERTY_GLOBAL_KEY, String.valueOf(value));
    }

    /** @noinspection UnusedReturnValue*/
    public boolean on_press_global_key(int keyCode) {
        if (null == gLiveTvMgr)
            return false;

        switch (keyCode) {
            case KeyEvent.KEYCODE_POWER:
                Log.d(TAG, "on_press_global_key: keyCode is POWER");
                return false;
            case KeyEvent.KEYCODE_TV:
                Log.d(TAG, "on_press_global_key: keyCode is LIVE TV");
                gLiveTvMgr.g_timeshiftBanner.timeshift_stop(true);
                return true;
            case KeyEvent.KEYCODE_HOME:
                Log.d(TAG, "on_press_global_key: keyCode is HOME");
                gLiveTvMgr.g_timeshiftBanner.timeshift_stop(true);
                return true;
            case KeyEvent.KEYCODE_GUIDE:
                Log.d(TAG, "on_press_global_key: keyCode is EPG");
                gLiveTvMgr.g_timeshiftBanner.timeshift_stop(true);
                return false;
            case KeyEvent.KEYCODE_BUTTON_3:
                Log.d(TAG, "on_press_global_key: keyCode is YouTube");
                gLiveTvMgr.g_timeshiftBanner.timeshift_stop(true);
                return false;
            case KeyEvent.KEYCODE_BUTTON_4:
                Log.d(TAG, "on_press_global_key: keyCode is Netflix");
                gLiveTvMgr.g_timeshiftBanner.timeshift_stop(true);
                return false;
            case KeyEvent.KEYCODE_BUTTON_5:
                Log.d(TAG, "on_press_global_key: keyCode is HOTTEST");
                gLiveTvMgr.g_timeshiftBanner.timeshift_stop(true);
                return false;
            case KeyEvent.KEYCODE_BUTTON_6:
                Log.d(TAG, "on_press_global_key: keyCode is Prime Video");
                gLiveTvMgr.g_timeshiftBanner.timeshift_stop(true);
                return false;
            case KeyEvent.KEYCODE_BUTTON_7:
                Log.d(TAG, "on_press_global_key: keyCode is LiTV");
                gLiveTvMgr.g_timeshiftBanner.timeshift_stop(true);
                return false;
            default:
                Log.d(TAG, "on_press_global_key: keyCode = " + keyCode);
                break;
        }
        return false;
    }

    public void onBroadcastMessage(Context context, Intent intent) {
        String action = intent.getAction();
        if (null == action)
            return;

        switch (action) {
            case Intent.ACTION_MEDIA_MOUNTED
                    -> on_usb_mount(intent);
            case Intent.ACTION_MEDIA_UNMOUNTED
                    -> on_usb_unmount(intent);
            case PrimeVolumeReceiver.ACTION_VOLUME_CHANGED,
                 PrimeVolumeReceiver.ACTION_STREAM_MUTE_CHANGED
                    -> Log.d(TAG, "onBroadcastMessage: ACTION_VOLUME_CHANGED / ACTION_STREAM_MUTE_CHANGED");
            case PrimeVolumeReceiver.ACTION_KEYCODE_VOLUME_DOWN,
                 PrimeVolumeReceiver.ACTION_KEYCODE_VOLUME_MUTE
                    -> on_volume_changed(intent);
            case PrimeTimerReceiver.ACTION_TIMER_RECORD -> {
                on_timer_record(context, intent);
                gPrimeDtv.schedule_next_timer(intent);
                gPrimeDtv.save_table(EnTableType.TIMER);
            }
            case PrimeTimerReceiver.ACTION_TIMER_POWER_ON -> {
                on_timer_power_on(context, intent);
                gPrimeDtv.schedule_next_timer(intent);
                gPrimeDtv.save_table(EnTableType.TIMER);
            }
            case PrimeTimerReceiver.ACTION_TIMER_CHANGE_CHANNEL -> {
                on_timer_change_channel(intent);
                gPrimeDtv.schedule_next_timer(intent);
                gPrimeDtv.save_table(EnTableType.TIMER);
            }
        }
    }

    public void onMessage(TVMessage msg) {
        switch (msg.getMsgType()) {
            case TVMessage.TYPE_AV_FRAME_PLAY_STATUS -> {
                boolean keepOn = msg.getAvFrameStatus() == 0 && !BlockedChannel.is_blocked();
                Utils.keep_screen_on(this, keepOn);
            }
            case TVMessage.TYPE_HDD_NO_SPACE -> {
                if (gChChangeManager.is_pvr_recording()) {
                    GposInfo gposInfo = gPrimeDtv.gpos_info_get();
                    if (gposInfo.getDVRManagementLimit() == GposInfo.MANAGEMENT_AUTO_CLEAR) {
                        del_oldest_rec();
                    } else {
                        if (!ActivityUtils.is_launcher_running_top(this)
                                || !(HomeApplication.get_current_activity() instanceof BaseActivity)) {
                            // just stop all pvr when we are in other app
                            // or in our activity which does not extends BaseActivity
                            stop_all_pvr();
                        } else if (gShowRecSpaceLimitDialog) {
                            show_record_space_limit_dialog();
                        }
                    }
                }
            }
            case TVMessage.TYPE_PVR_REC_DISK_FULL -> {
                if (gChChangeManager.is_pvr_recording()) {
//                    Utils.show_notification(
//                            getApplicationContext(),
//                            getString(R.string.pvr_message_storage_not_enough_title));
                    stop_all_pvr();
                }
            }
        }
    }

    public void set_live_tv_manager(LiveTvManager liveTvMgr) {
        gLiveTvMgr = liveTvMgr;
    }

    public static void set_thread_handler(Handler handler) {
        gThreadHandler = handler;
    }

    public static void set_handler(Handler handler) {
        g_Handler = handler;
    }

    public void set_fake_book_list(List<BookInfo> fakeBookList) {
        gFakeBookList = fakeBookList;
    }

    public PrimeDtv get_prime_dtv() {
        if (null == gPrimeDtv)
            gPrimeDtv = HomeApplication.get_prime_dtv();
        return gPrimeDtv;
    }

    public List<BookInfo> get_fake_book_list() {
        if (null == gFakeBookList)
            gFakeBookList = gPrimeDtv.book_info_get_list();
        return gFakeBookList;
    }

    private int get_booking_count(List<BookInfo> conflictBooks, BookInfo bookInfo) {
        int bookingCount = 0;
        
        for (BookInfo book : conflictBooks)
            if (book.getStartTime() == bookInfo.getStartTime())
                bookingCount++;
        
        return bookingCount;
    }

    public static void send_message(int what) {
        if (g_Handler != null)
            g_Handler.sendMessage(g_Handler.obtainMessage(what));
    }

    private void send_message(Handler handler, int what) {
        if (handler == null)
            return;
        Message msg = handler.obtainMessage();
        msg.what = what;
        //msg.obj = callback;
        handler.sendMessage(msg);
    }

    public void on_volume_changed(Intent intent) {
        if (PrimeVolumeReceiver.g_volume != 0)
            return;
        Log.d(TAG, "on_volume_changed: show unmute message");
        Snakebar.show(this, R.string.unmute_by_volume_up, Snakebar.LENGTH_SHORT);
    }

    public void on_usb_mount(Intent intent) {
        Log.d(TAG, "on_usb_mount: " + intent.getDataString());
        Context context = this;
        String[] usb_label = {""};
        long totalSize = UsbUtils.get_usb_total_size(context, intent, usb_label);

        if (totalSize < 0) {
            Log.w(TAG, "on_usb_mount: no usb disk");
            return;
        }

        if (totalSize < LiveTvManager.DEFAULT_LIMIT_OF_USB_TOTAL_SIZE) {
            if (!Pvcfg.getPVR_PJ())
                return;
            if (null == gMsgDialog)
                gMsgDialog = new HotkeyRecord(this);
            gMsgDialog.set_dismiss_action(() -> show_ejecting(usb_label[0]));
            // TODO: integrate Error Message 2
            gMsgDialog.show_panel("E621 " + context.getString(
                    R.string.pvr_message_low_space,
                    LiveTvManager.DEFAULT_LIMIT_OF_USB_TOTAL_SIZE / 1000));
        }
    }

    public void on_usb_unmount(Intent intent) {
        Log.d(TAG, "on_usb_unmount: " + intent.getDataString());
        if (gMsgDialog != null) {
            gMsgDialog.set_dismiss_action(null);
            gMsgDialog.dismiss();
        }
    }

    public void on_timer_record(Context context, Intent intent) {
        Bundle bookInfoBundle = intent.getExtras();
        if (bookInfoBundle == null) {
            Log.e(TAG, "on_timer_record: bookInfoBundle == null");
            return;
        }

        BookInfo bookInfo = new BookInfo(bookInfoBundle);
        // skip if book cycle == BOOK_CYCLE_SERIES_EMPTY
        if (bookInfo.getBookCycle() == BookInfo.BOOK_CYCLE_SERIES_EMPTY) {
            Log.w(TAG, "on_timer_record: book cycle = BOOK_CYCLE_SERIES_EMPTY, skip!");
            return;
        }

        if (gConflictDialog != null && gConflictDialog.isShowing())
            Log.w(TAG, "on_timer_record: already showing [Dialog] Conflict !!!");
        else {
            // backup book for MiniEPG's follow's Rec Icon
            gFakeBookList = gPrimeDtv.book_info_get_list();

            // build conflict books & records
            List<BookInfo> conflictRecords = build_conflict_records();
            List<BookInfo> conflictBooks = build_conflict_books(bookInfo);
            for (BookInfo book : conflictRecords) Log.i(TAG, "on_timer_record: recording now... [book id] " + book.getBookId() + ", [book name] " + book.getName() + ", [start time] " + MiniEPG.ms_to_time(book.getStartTimeMs(), "HH:mm:ss"));
            for (BookInfo book : conflictBooks)   Log.i(TAG, "on_timer_record: conflict book... [book id] " + book.getBookId() + ", [book name] " + book.getName() + ", [start time] " + MiniEPG.ms_to_time(book.getStartTimeMs(), "HH:mm:ss"));

            // count conflict books
            int recordCount = conflictRecords.size() + gActiveTimes.size();
            int bookCount = get_booking_count(conflictBooks, bookInfo);
            int conflictCount = recordCount + bookCount;
            Log.d(TAG, "on_timer_record: [record count] " + recordCount + " (timer: " + gActiveTimes.size() + ")" + ", [book count] " + bookCount + ", [conflict count] " + conflictCount);

            if (conflictCount > Pvcfg.NUM_OF_RECORDING) {
                cancel_all_timer(context, conflictBooks);
                gConflictDialog = new ConflictDialog(this, bookInfo, conflictRecords, conflictBooks);
                gConflictDialog.show();
            }
            else {
                long delayMs = bookInfo.getStartTimeMs() - System.currentTimeMillis();
                if (delayMs < 0) delayMs = 0;
                if (FOR_TEST_BOOK_CONFLICT) delayMs = 30000;

                if (gAboutToStartDialog != null && gAboutToStartDialog.isShowing())
                    Log.w(TAG, "on_timer_record: already showing [Dialog] About To Start !!!");
                else
                if (conflictCount == Pvcfg.NUM_OF_RECORDING && need_change_channel(conflictRecords, conflictBooks)) {
                    Log.d(TAG, "on_timer_record: need to change channel");
                    gAboutToStartDialog = new AboutToStartDialog(this, bookInfo, delayMs, conflictBooks);
                    gAboutToStartDialog.show();
                }

                Log.d(TAG, "on_timer_record: start record with [delay] " + MiniEPG.ms_to_duration(delayMs) + ", [book name] " + bookInfo.getName() + ", [start time] " + MiniEPG.ms_to_time(bookInfo.getStartTimeMs(), "HH:mm:ss"));
                start_timer_record(bookInfo, delayMs);
            }
        }
    }

    public void on_timer_power_on(Context context, Intent intent) {
        Log.d(TAG, "on_timer_power_on: ");

        if (PrimeBroadcastReceiver.get_screen_on_status()) {
            Log.i(TAG, "on_timer_power_on: already screen on");
            return;
        }
        Utils.input_keycode(KeyEvent.KEYCODE_POWER);
    }

    public void on_timer_change_channel(Intent intent) {
        Bundle bookInfoBundle = intent.getExtras();
        if (bookInfoBundle == null) {
            Log.e(TAG, "on_timer_change_channel: bookInfoBundle == null");
            return;
        }

        // if is same channel, skip
        ChannelChangeManager changeMgr = ChannelChangeManager.get_instance(this);
        BookInfo bookInfo = new BookInfo(bookInfoBundle);
        if (changeMgr.get_cur_ch_id() == bookInfo.getChannelId()) {
            Log.e(TAG, "on_timer_change_channel: current channel is same as bookInfo channel, skip!");
            return;
        }

        // get channel name
        ProgramInfo channel = get_prime_dtv().get_program_by_channel_id(bookInfo.getChannelId());
        String channelName = channel != null ? channel.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM) + " " + channel.getDisplayName() : "NULL";

        // show remind dialog
        EventDialog dialog = new EventDialog(this);
        dialog.set_title_text(String.format(getString(R.string.remind_dialog_channel_change_title), channelName));
        dialog.set_confirm_text(R.string.remind_dialog_channel_change_yes);
        dialog.set_cancel_text(R.string.remind_dialog_channel_change_no);
        dialog.set_confirm_action(change_channel(bookInfo));
        dialog.set_timeout_action(change_channel(bookInfo), 60 * 1000);
        dialog.show();
    }

    private List<BookInfo> build_conflict_records() {
        List<BookInfo> conflictRecords = new ArrayList<>();

        for (RecChannelInfo recChannelInfo : gChChangeManager.get_rec_channel_info()) {
            EPGEvent epgEvent = gPrimeDtv.get_epg_by_event_id(recChannelInfo.getChannelId(), recChannelInfo.getEventId());
            //noinspection ExtractMethodRecommender
            BookInfo book = new BookInfo();
            book.setBookId(-1);
            book.setBookType(BookInfo.BOOK_TYPE_RECORD_NOW);
            book.setChannelId(recChannelInfo.getChannelId());
            book.setChannelNum(recChannelInfo.getChannelNum());
            book.setEpgEventId(recChannelInfo.getEventId());
            book.setEventName(recChannelInfo.getEventName());
            book.setAutoSelect(true);
            book.set4K(recChannelInfo.is4K());
            if (epgEvent != null)
                book.setStartTimeMs(epgEvent.get_start_time());
            conflictRecords.add(book);
        }

        return conflictRecords;
    }

    public List<BookInfo> build_conflict_books(BookInfo bookInfo) {
        List<BookInfo> conflictBooks = new ArrayList<>();

        for (BookInfo book : gPrimeDtv.book_info_find_conflict_records(bookInfo)) {
            if (book.getStartTime() == bookInfo.getStartTime())
                conflictBooks.add(book);
        }

        return conflictBooks;
    }

    private void cancel_all_timer(Context context, List<BookInfo> conflictBooks) {
        for (BookInfo book : conflictBooks)
            gPrimeDtv.cancel_alarms(context, book.get_Intent());
    }

    private void start_timer_record(BookInfo bookInfo, long delayMs) {
        CountDownTimer timer = new CountDownTimer(delayMs, 1000) {
            @Override public void onTick(long millisUntilFinished) {
                if (millisUntilFinished / 1000 % 5 == 0)
                    Log.d(TAG, "start_timer_record: [delay] " + MiniEPG.ms_to_duration(millisUntilFinished) + ", [book name] " + bookInfo.getName());
            }
            @Override public void onFinish() {
                gActiveTimes.remove(bookInfo.getName());
                gChChangeManager.pvr_record_start(bookInfo.getChannelId(), bookInfo.getEpgEventId(), bookInfo.getDurationMs() / 1000, false);
            }
        }.start();
        gActiveTimes.put(bookInfo.getName(), timer);
        Log.d(TAG, "start_timer_record: [book name] " + bookInfo.getName());
    }

    public void stop_timer_record(BookInfo bookInfo) {
        CountDownTimer timer = gActiveTimes.get(bookInfo.getName());
        if (timer != null) {
            timer.cancel();
            gActiveTimes.remove(bookInfo.getName());
            Log.d(TAG, "stop_timer_record: [book name] " + bookInfo.getName());
        }
    }

    private boolean need_change_channel(List<BookInfo> conflictRecords, List<BookInfo> conflictBooks) {
        long currentChannelId = gChChangeManager.get_cur_ch_id();
        boolean isCurrentChannel = false;

        for (BookInfo book : conflictRecords) {
            if (book.getChannelId() == currentChannelId) {
                isCurrentChannel = true;
                break;
            }
        }

        for (BookInfo book : conflictBooks) {
            if (book.getChannelId() == currentChannelId) {
                isCurrentChannel = true;
                break;
            }
        }

        return !isCurrentChannel;
    }

    private Runnable change_channel(BookInfo bookInfo) {
        return () -> {
            Intent intentChangeChannel = new Intent(this, HomeActivity.class);
            intentChangeChannel.putExtra(HomeActivity.EXTRA_SCREEN_TYPE, HomeActivity.SCREEN_TYPE_CHANGE_CHANNEL);
            intentChangeChannel.putExtra(HomeActivity.EXTRA_CHANNEL_ID, bookInfo.getChannelId());
            startActivity(intentChangeChannel);
        };
    }

    public void show_ejecting(String usb_label) {
        Context context = this;
        ProgressDialog ejecting = new ProgressDialog(context);
        String message = String.format(context.getString(R.string.pvr_ejecting_usb_device), usb_label);
        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();

        Runnable ejectUsbDisk = () -> {
            // close database
            primeDtv.pvr_deinit();

            // eject usb device
            boolean success = Utils.eject_usb_device(context);
            Log.d(TAG, "show_ejecting: [ejecting success] " + success);

            // dismiss & show message
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                ejecting.dismiss();
                Snakebar.show(context, message, Snakebar.LENGTH_LONG);
            }, 3000);
        };

        ejecting.set_title(message);
        ejecting.set_start_action(ejectUsbDisk);
        ejecting.show();
    }

    private PvrRecFileInfo find_oldest_record() {
        int recCount = gPrimeDtv.pvr_GetRecCount();
        List<PvrRecFileInfo> pvrRecFileInfos
                = gPrimeDtv.pvr_GetRecLink(0, recCount);

        PvrRecFileInfo oldest = null;
        for (PvrRecFileInfo pvrRecFileInfo : pvrRecFileInfos) {
            PvrRecFileInfo tmpOldest;
            boolean isSeries = pvrRecFileInfo.isSeries();
            if (isSeries) {
                tmpOldest = find_oldest_record_in_series(pvrRecFileInfo);
            } else {
                tmpOldest = pvrRecFileInfo;
            }

            // find rec which is not recording/playing and has an earlier start time
            if (tmpOldest != null
                    && !gPrimeDtv.pvr_IsIdxInUse(tmpOldest.getPvrRecIdx())
                    && (oldest == null || tmpOldest.getStartTime() < oldest.getStartTime())) {
                oldest = tmpOldest;
            }

            if (oldest != null && !isSeries) {
                break;
            }
        }

        return oldest;
    }

    private PvrRecFileInfo find_oldest_record_in_series(PvrRecFileInfo pvrRecFileInfo) {
        PvrRecFileInfo oldest = null;
        if (pvrRecFileInfo != null && pvrRecFileInfo.isSeries()) {
            PvrRecIdx pvrRecIdx = pvrRecFileInfo.getPvrRecIdx();
            // here SeriesIdx = query start index, set to 0 to get first series rec
            pvrRecIdx.setSeriesIdx(0);
            // first rec in series should be the oldest
            List<PvrRecFileInfo> pvrRecFileInfos
                    = gPrimeDtv.pvr_GetSeriesRecLink(pvrRecIdx, 1);
            if (!pvrRecFileInfos.isEmpty()) {
                oldest = pvrRecFileInfos.get(0);
            }
        }

        return oldest;
    }

    private void del_oldest_rec() {
        Log.d(TAG, "del_oldest_rec: in");
        if (gDeleteRecThread != null && gDeleteRecThread.isAlive()) {
            Log.d(TAG, "del_oldest_rec: pre oldest rec is still deleting, skip");
            return;
        }

        gDeleteRecThread = new Thread(() -> {
            PvrRecFileInfo oldest = find_oldest_record();
            Log.d(TAG, "del_oldest_rec: " + oldest);
            if (oldest != null) {
                Log.d(TAG, "del_oldest_rec: oldest index = ["
                        + oldest.getMasterIdx()
                        + ", "
                        + oldest.getSeriesIdx() + "]");
                gPrimeDtv.pvr_DelOneRec(oldest.getPvrRecIdx());
            }
        });

        gDeleteRecThread.start();
    }

    private void stop_all_pvr() {
        if (gChChangeManager.is_recording()) {
            gChChangeManager.pvr_record_stop_all_no_delay();
        }

        if (gChChangeManager.isTimeshiftStart()) {
            gChChangeManager.pvr_timeshift_stop();
        }
    }

    private void show_record_space_limit_dialog() {
        if (gEventDialog == null) {
            gEventDialog = new EventDialog(this);
        }

        if (!gEventDialog.isShowing()) {
            gEventDialog.set_confirm_text(getString(R.string.pvr_event_dialog_record_manager_space));
            gEventDialog.set_cancel_text(getString(R.string.pvr_event_dialog_record_stop));
            gEventDialog.set_title_text("E602 " + getString(R.string.pvr_event_dialog_record_space_limit_title));
            gEventDialog.set_confirm_action(() -> {
                if (getClass().getName().equals(RecordProgramsActivity.class.getName())) { // already in RecordProgramsActivity
                    temp_disable_rec_space_limit_dialog();
                } else { // open RecordProgramsActivity
                    String usb_path = Utils.get_mount_usb_path();
                    if (usb_path != null && !usb_path.isEmpty()) {
                        gPrimeDtv.pvr_init(usb_path); // make sure pvr is initialized
                        Intent intent = new Intent(getApplicationContext(), RecordProgramsActivity.class);
                        intent.putExtra(EXTRA_TEMP_DISABLE_REC_SPACE_LIMIT_DIALOG, true);
                        startActivity(intent);
                    }
                }
            });
            gEventDialog.set_cancel_action(() -> {
//                Utils.show_notification(
//                        getApplicationContext(),
//                        getString(R.string.pvr_message_storage_not_enough_title));
                stop_all_pvr();
            });

            gEventDialog.show();
        }
    }

    private void temp_disable_rec_space_limit_dialog() {
        gShowRecSpaceLimitDialog = false;

        // start a timer to set gShowRecSpaceLimitDialog back to true
        if (gCountDownTimer != null) {
            gCountDownTimer.cancel();
        }

        gCountDownTimer = new CountDownTimer(60*1000, 3000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long availableSize = Utils.get_usb_space_info().get(1);
                if (!gChChangeManager.is_pvr_recording()
                        || availableSize >= Pvcfg.getPvrHddLimitSize()) {
                    gShowRecSpaceLimitDialog = true;
                    this.cancel();
                }
            }

            @Override
            public void onFinish() {
                gShowRecSpaceLimitDialog = true;
            }
        };
        gCountDownTimer.start();
    }
}
