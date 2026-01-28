package com.prime.dmg.launcher.Home.Hotkey;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.BaseDialog;
import com.prime.dmg.launcher.CustomView.EventDialog;
import com.prime.dmg.launcher.Home.LiveTV.LiveTvManager;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.UsbUtils;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.TVMessage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** @noinspection CommentedOutCode*/
public class HotkeyRecord extends BaseDialog {

    private static final String TAG = HotkeyRecord.class.getSimpleName();

    WeakReference<AppCompatActivity> g_ref;
    PrimeDtv gPrimeDtv;
    LiveTvManager gLiveTvMgr;
    ChannelChangeManager gChChangeMgr;
    List<BookInfo> gConflictBooks;
    static BookInfo gSeriesBookInfo;
    boolean g_showDialog = true;
    boolean g_ShowCancel = false;
    String g_message;

    // button action
    Runnable g_confirmAction;
    Runnable g_dismissAction;

    public HotkeyRecord(@NonNull Context context) {
        super(context, R.style.Theme_DMGLauncher_DialogFullScreen);
        g_ref = new WeakReference<>((AppCompatActivity) context);
        g_message = get().getString(R.string.dialog_lang_none);
    }

    public HotkeyRecord(@NonNull AppCompatActivity activity) {
        super(activity, R.style.Theme_DMGLauncher_DialogFullScreen);
        g_ref = new WeakReference<>(activity);
        g_message = get().getString(R.string.dialog_lang_none);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.semi_transparent);
            getWindow().setWindowAnimations(R.style.Theme_DMGLauncher_DialogAnimation);
        }
        setContentView(R.layout.live_tv_hotkey_record_message_dialog);
    }

    @Override
    protected void onStart() {
        super.onStart();

        View messageDialog = findViewById(R.id.lo_live_tv_message_dialog);
        View messagePanel  = findViewById(R.id.lo_live_tv_message_panel);
        messageDialog.setVisibility(is_show_dialog() ? View.VISIBLE : View.GONE);
        messagePanel.setVisibility(is_show_dialog() ? View.GONE : View.VISIBLE);

        if (is_show_dialog()) {
            TextView dialogMsg = findViewById(R.id.lo_live_tv_message_dialog_text);
            dialogMsg.setText(g_message);
        }
        if (is_show_panel()) {
            TextView txvPanelMsg = findViewById(R.id.lo_live_tv_message_panel_main_text);
            Button   btnConfirm  = findViewById(R.id.lo_live_tv_message_panel_button_confirm);
            Button   btnCancel   = findViewById(R.id.lo_live_tv_message_panel_button_cancel);
            txvPanelMsg.setText(g_message);
            btnCancel.setVisibility(g_ShowCancel ? View.VISIBLE : View.GONE);
            btnConfirm.setOnClickListener(this::on_click_confirm);
            btnCancel.setOnClickListener(this::on_click_cancel);
        }
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    @Override
    public void dismiss() {
        if (g_dismissAction != null) {
            g_dismissAction.run();
            g_dismissAction = null;
        }
        super.dismiss();
    }

    public void on_click_confirm(View v) {
        dismiss();
        if (g_confirmAction != null) {
            g_confirmAction.run();
            g_confirmAction = null;
        }
    }

    public void on_click_cancel(View v) {
        dismiss();
    }

    public void set_confirm_action(Runnable action) {
        g_confirmAction = action;
    }

    public void set_dismiss_action(Runnable action) {
        g_dismissAction = action;
    }

    public AppCompatActivity get() {
        return g_ref.get();
    }

    public BookInfo get_current_book_info(long channelId, EPGEvent event) {
        // current time
        Calendar calCurrent = Calendar.getInstance();
        calCurrent.setTime(new Date());

        int new_book_id = MiniEPG.get_new_book_id();
        int day_of_week = MiniEPG.get_week_day(calCurrent);
        int year = calCurrent.get(Calendar.YEAR);
        int month = calCurrent.get(Calendar.MONTH) + 1;
        int date = calCurrent.get(Calendar.DATE);
        int hour_of_day = calCurrent.get(Calendar.HOUR_OF_DAY);
        int minute = calCurrent.get(Calendar.MINUTE);
        int startTime = hour_of_day * 100 + minute;
        int durationMs = (int) (event.get_end_time() - System.currentTimeMillis());
        int durationHour = durationMs / (1000 * 60 * 60);
        int durationMinute = durationMs % (1000 * 60 * 60) / (1000 * 60);
        int duration = durationHour * 100 + durationMinute;
        //Log.e(TAG, "get_current_book_info: [name] " + event.get_event_name() + ", [duration] " + durationMs + ", [hour] " + durationHour + ", [minute] " + durationMinute + ", [duration] " + duration);

        BookInfo bookInfo = new BookInfo();
        bookInfo.setBookId(new_book_id);
        bookInfo.setChannelId(channelId);
        bookInfo.setGroupType(FavGroup.ALL_TV_TYPE);
        bookInfo.setEventName(event.get_event_name());
        bookInfo.setBookType(BookInfo.BOOK_TYPE_RECORD);
        bookInfo.setBookCycle(BookInfo.BOOK_CYCLE_SERIES);
        bookInfo.setYear(year);
        bookInfo.setMonth(month);
        bookInfo.setDate(date);
        bookInfo.setWeek(day_of_week);
        bookInfo.setSeriesRecKey(event.get_series_key());
        bookInfo.setEpisode(event.get_episode_key());
        bookInfo.setSeries(event.is_series());
        bookInfo.setStartTime(startTime);
        bookInfo.setDuration(duration);
        bookInfo.setEnable(0);
        bookInfo.setEpgEventId(event.get_event_id());
        return bookInfo;
    }

    private List<String> get_conflict_names(List<BookInfo> conflictBooks) {
        List<String> conflictNameList = new ArrayList<>();
        long channelId;
        ProgramInfo channel;
        String displayNum;
        String eventName;
        String conflictName;

        for (BookInfo conflictBook : conflictBooks) {
            if (conflictBooks.indexOf(conflictBook) >= Pvcfg.NUM_OF_RECORDING)
                return conflictNameList;
            channelId = conflictBook.getChannelId();
            channel = gLiveTvMgr.get_channel(channelId);
            displayNum = null == channel ? "000" : channel.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM);
            eventName = null == conflictBook.getEventName() ? "NULL" : conflictBook.getEventName();
            conflictName = "[" + displayNum + "] " + eventName;
            conflictNameList.add(conflictName);
        }
        return conflictNameList;
    }

    public boolean is_show_dialog() {
        return g_showDialog;
    }

    public boolean is_show_panel() {
        return !g_showDialog;
    }

    public void show_dialog(int resId) {
        show_dialog(getContext().getString(resId));
    }

    public void show_dialog(String msg) {
        g_message = msg;
        g_showDialog = true;
        get().runOnUiThread(this::show);
    }

    public void show_panel(int resId, boolean showCancel) {
        g_ShowCancel = showCancel;
        show_panel(resId);
    }

    public void show_panel(int resId) {
        show_panel(getContext().getString(resId));
    }

    public void show_panel(String msg, Runnable action) {
        g_confirmAction = action;
        show_panel(msg);
    }

    public void show_panel(String msg) {
        g_message = msg;
        g_showDialog = false;
        get().runOnUiThread(this::show);
    }

    public void record_start(MiniEPG miniEPG, long channelId, EPGEvent present, int duration, boolean isSeries) {

        new Thread(() -> {

            if (!pass_all_check()) {
                Log.e(TAG, "record_start: something wrong");
                return;
            }

            if (isSeries)
                gSeriesBookInfo = get_current_book_info(channelId, present);

            gChChangeMgr.pvr_record_start(channelId, present.get_event_id(), duration, isSeries);
            miniEPG.delay();
            Log.i(TAG, "record_start: PVR Recording - Start");

        }).start();
    }

    public static void record_stop(Context context, long channelId) {
        new Thread(() -> {
            ChannelChangeManager changeMgr = ChannelChangeManager.get_instance(context);
            changeMgr.pvr_record_stop(channelId);
            Log.i(TAG, "record_stop: PVR Recording - Stop");
        }, "record_stop").start();
    }

    public void book_one_record(MiniEPG miniEPG, ProgramInfo channel, EPGEvent epgEvent, BookInfo newBookInfo, String recordName) {
        if (!pass_all_check()) {
            Log.e(TAG, "book_single_record: something wrong");
            return;
        }

        List<BookInfo> bookList = new ArrayList<>();
        for (BookInfo book : miniEPG.get().build_conflict_books(newBookInfo))
            if (!book.isSeries())
                bookList.add(book);

        if (bookList.size() < Pvcfg.NUM_OF_RECORDING)
            replace_book(miniEPG, null, newBookInfo);
        else {
            BookInfo book1 = bookList.get(0);
            BookInfo book2 = bookList.get(1);
            EventDialog dialog = new EventDialog(get());
            dialog.set_title_text(R.string.pvr_reserve_conflict_title);
            dialog.set_confirm_text("[" + book1.getChannelNum() + "] " + book1.getEventName());
            dialog.set_cancel_text( "[" + book2.getChannelNum() + "] " + book2.getEventName());
            dialog.set_confirm_action(() -> replace_book(miniEPG, book1, newBookInfo));
            dialog.set_cancel_action(() -> replace_book(miniEPG, book2, newBookInfo));
            dialog.show();
        }
    }

    public void book_series_record(MiniEPG miniEPG, ProgramInfo channel, EPGEvent epgEvent, BookInfo bookInfo, String recordName) {
        if (!pass_all_check()) {
            Log.e(TAG, "book_series_record: something wrong");
            return;
        }

        // add series (for check conflict)
        Log.d(TAG, "book_series_record: add series (for check conflict)");
        gPrimeDtv.add_series(bookInfo.getChannelId(), bookInfo.getSeriesRecKey());

        // book series (for record series)
        Log.d(TAG, "book_series_record: book series (for record series)");
        gPrimeDtv.book_info_add(bookInfo);
        gPrimeDtv.set_alarms(get(), bookInfo.get_Intent());
        gPrimeDtv.save_table(EnTableType.TIMER);

        // show record icon
        miniEPG.show_record_icon();
        miniEPG.delay();
        miniEPG.set_detail_red_hint(R.string.hint_rcu_cancel_record); // book_series_record
        miniEPG.get().set_fake_book_list(gPrimeDtv.book_info_get_list());
        Log.d(TAG, "book_series_record: [channel] " + channel.getDisplayNameFull() + ", [record name] " + recordName);
    }

    public void cancel_book_record(MiniEPG miniEPG, ProgramInfo channel, EPGEvent follow, String recordName) {
        if (!pass_null_check()) {
            Log.e(TAG, "cancel_book_record: something wrong");
            return;
        }

        int bookId = find_book_id(follow);
        BookInfo bookInfo = gPrimeDtv.book_info_get(bookId);
        gPrimeDtv.book_info_delete(bookId);
        gPrimeDtv.cancel_alarms(getContext(), bookInfo.get_Intent());
        gPrimeDtv.save_table(EnTableType.TIMER);
        miniEPG.hide_record_icon();
        miniEPG.delay();
        miniEPG.set_detail_red_hint(R.string.hint_rcu_record); // cancel_book_record
        miniEPG.get().set_fake_book_list(gPrimeDtv.book_info_get_list());
        Log.d(TAG, "cancel_book_record: [channel] " + channel.getDisplayNameFull() + ", [record name] " + recordName);
    }

    public void replace_book(MiniEPG miniEPG, BookInfo deleteBook, BookInfo newBook) {
        if (deleteBook != null)
            gPrimeDtv.book_info_delete(deleteBook.getBookId());

        newBook.setSeries(false);
        gPrimeDtv.book_info_add(newBook);
        gPrimeDtv.set_alarms(get(), newBook.get_Intent());
        gPrimeDtv.save_table(EnTableType.TIMER);
        miniEPG.show_record_icon();
        miniEPG.delay();
        miniEPG.set_detail_red_hint(R.string.hint_rcu_cancel_record);
        miniEPG.get().set_fake_book_list(gPrimeDtv.book_info_get_list());

        for (BookInfo b : miniEPG.get().get_fake_book_list())
            Log.d(TAG, "replace_book: [book id] " + b.getBookId() + ", [book name] " + b.getName() + ", [series] " + b.isSeries());
    }

    private int find_book_id(EPGEvent follow) {
        if (null == follow) {
            Log.e(TAG, "find_book_id: follow is null");
            return -1;
        }
        for (BookInfo book : gPrimeDtv.book_info_get_list()) {
            if (book.getBookType() == BookInfo.BOOK_TYPE_RECORD &&
                book.getEpgEventId() == follow.get_event_id())
                return book.getBookId();
        }
        return -1;
    }

    public void handle_book_series() {
        new Thread(() -> {
            if (!pass_all_check()) {
                Log.e(TAG, "handle_book_series: something wrong");
                return;
            }
            if (gSeriesBookInfo == null) {
                Log.w(TAG, "handle_book_series: no need to book series record");
                return;
            }
            // add series (for check conflict)
            Log.i(TAG, "handle_book_series: add series (for check conflict)");
            gPrimeDtv.add_series(gSeriesBookInfo.getChannelId(), gSeriesBookInfo.getSeriesRecKey());
            gPrimeDtv.save_series();
            // book series (for record series)
            Log.i(TAG, "handle_book_series: book series (for record series)");
            gPrimeDtv.book_info_add(gSeriesBookInfo);
            gPrimeDtv.schedule_next_timer(gSeriesBookInfo);
            gPrimeDtv.save_table(EnTableType.TIMER);
            gSeriesBookInfo = null;
        }).start();
    }

    private boolean handle_conflict_books(BookInfo bookInfo, int duration) {
        final boolean HAS_CONFLICT = true;
        final boolean NO_CONFLICT = false;

        // get conflict books
        gConflictBooks = gPrimeDtv.book_info_find_conflict_records(bookInfo);

        // show conflict books
        if (gConflictBooks.size() >= Pvcfg.NUM_OF_RECORDING) {
            // has conflict books
            Log.e(TAG, "handle_conflict_books: has conflict books, select one to delete book");
            List<String> conflictNames = get_conflict_names(gConflictBooks);
            EventDialog dialog = new EventDialog(get());
            dialog.set_title_text(R.string.pvr_reserve_conflict_title);
            dialog.set_confirm_text(conflictNames.get(0));
            dialog.set_cancel_text(conflictNames.get(1));
            dialog.set_confirm_action(() -> {
                Log.d(TAG, "handle_conflict_books: delete book 1st");
                // delete series
                gPrimeDtv.delete_series(gConflictBooks.get(0).getChannelId(), gConflictBooks.get(0).getSeriesRecKey());
                gPrimeDtv.save_series();
                // delete book
                gPrimeDtv.book_info_delete(gConflictBooks.get(0).getBookId());
                gPrimeDtv.save_table(EnTableType.TIMER);
            });
            dialog.set_cancel_action(() -> {
                Log.d(TAG, "handle_conflict_books: delete book 2nd");
                // delete series
                gPrimeDtv.delete_series(gConflictBooks.get(1).getChannelId(), gConflictBooks.get(1).getSeriesRecKey());
                gPrimeDtv.save_series();
                // delete book
                gPrimeDtv.book_info_delete(gConflictBooks.get(1).getBookId());
                gPrimeDtv.save_table(EnTableType.TIMER);
            });
            dialog.set_back_action(() -> {
                Log.d(TAG, "handle_conflict_books: delete series");
                gPrimeDtv.delete_series(bookInfo.getChannelId(), bookInfo.getSeriesRecKey());
                gPrimeDtv.save_series();
            });
            dialog.set_dismiss_action(() -> {
                boolean has_conflict = handle_conflict_books(bookInfo, duration);
                Log.d(TAG, "handle_conflict_books: [has conflict] " + has_conflict);
            });
            dialog.show();
            return HAS_CONFLICT;
        }
        else {
            Log.d(TAG, "handle_conflict_books: no conflict, add book and start recording");
            gPrimeDtv.book_info_add(bookInfo);
            gPrimeDtv.schedule_next_timer(bookInfo);
            gPrimeDtv.save_table(EnTableType.TIMER);
            gChChangeMgr.pvr_record_start(bookInfo.getChannelId(), bookInfo.getEpgEventId(), duration, true);
            Log.i(TAG, "handle_conflict_books: PVR Recording - Start");
            return NO_CONFLICT;
        }
    }

    /** @noinspection BooleanMethodIsAlwaysInverted*/
    private boolean pass_all_check() {
        AppCompatActivity activity = get();

        if (activity instanceof HomeActivity homeActivity) {

            if (!pass_null_check()) {
                Log.e(TAG, "pass_all_check: null handle");
                return false;
            }

            /*if (homeActivity.get_live_tv_manager().is_block()) {
                Log.w(TAG, "pass_all_check: blocked channel, not record");
                return false;
            }*/

            if (!UsbUtils.has_usb_disk(activity)) {
                show_dialog(R.string.error_e601);
                return false;
            }

            if (!UsbUtils.has_usb_enough_space()) {
                show_dialog(R.string.error_e621);
                return false;
            }

            return true;
        }

        return false;
    }

    /** @noinspection BooleanMethodIsAlwaysInverted*/
    private boolean pass_null_check() {
        AppCompatActivity activity = get();

        if (activity instanceof HomeActivity homeActivity) {
            gPrimeDtv = homeActivity.g_dtv;
            gLiveTvMgr = homeActivity.get_live_tv_manager();
            if (gPrimeDtv == null || gLiveTvMgr == null)
                return false;

            gChChangeMgr = gLiveTvMgr.g_chChangeMgr;
            return gChChangeMgr != null;
        }

        return false;
    }
}
