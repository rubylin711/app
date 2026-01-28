package com.prime.dmg.launcher.Home.Hotkey;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.CustomView.EventDialog;
import com.prime.dmg.launcher.CustomView.Snakebar;
import com.prime.dmg.launcher.Home.LiveTV.LiveTvManager;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.ProgramInfo;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** @noinspection UnusedReturnValue, FieldCanBeLocal */
public class HotkeyRemind extends Dialog {

    private static final String TAG = HotkeyRemind.class.getSimpleName();

    // handle
    private final WeakReference<AppCompatActivity> gRef;
    private PrimeDtv gPrimeDtv;
    private LiveTvManager gLiveTvMgr;
    private ChannelChangeManager gChangeMgr;
    private MiniEPG gMiniEPG;

    // data
    private int gMessageId;

    // action
    private Runnable gYesAction;
    private Runnable gNoAction;
    private Runnable gDismissAction;

    public HotkeyRemind(@NonNull Context context) {
        super(context, R.style.Theme_DMGLauncher_DialogFullScreen);
        gRef = new WeakReference<>((AppCompatActivity) context);
        init_view();
    }

    public HotkeyRemind(@NonNull AppCompatActivity activity) {
        super(activity, R.style.Theme_DMGLauncher_DialogFullScreen);
        gRef = new WeakReference<>(activity);
    }

    private void init_view() {
        gMessageId = R.string.dialog_lang_none;
    }

    private void init_handle() {
        gPrimeDtv = HomeApplication.get_prime_dtv();
        gLiveTvMgr = get().get_live_tv_manager();
        gChangeMgr = get().get_channel_change_manager();
        gMiniEPG = gLiveTvMgr.g_miniEPG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.semi_transparent);
            getWindow().setWindowAnimations(R.style.Theme_DMGLauncher_DialogAnimation);
        }

        if (R.string.remind_hint_no_epg == gMessageId) {
            Log.d(TAG, "onCreate: No program can remind");
            setContentView(R.layout.live_tv_hotkey_remind_message_dialog);
            TextView msg = findViewById(R.id.lo_live_tv_hotkey_remind_dialog_msg_text);
            msg.setText(gMessageId);
        }
        if (R.string.remind_hint_cancel == gMessageId) {
            Log.d(TAG, "onCreate: Cancel remind ?");
            setContentView(R.layout.live_tv_hotkey_remind_cancel_dialog);
            on_click();
        }
    }

    public void on_click() {
        TextView yes = findViewById(R.id.lo_remind_cancel_dialog_yes);
        TextView no  = findViewById(R.id.lo_remind_cancel_dialog_no);

        if (null == yes || null == no)
            return;

        yes.setOnClickListener(v -> on_click_yes());
        no.setOnClickListener(v -> on_click_no());
    }

    public void on_click_yes() {
        if (gYesAction != null) {
            gYesAction.run();
            gYesAction = null;
        }
        dismiss();
    }

    public void on_click_no() {
        if (gNoAction != null) {
            gNoAction.run();
            gNoAction = null;
        }
        dismiss();
    };

    public void set_yes_action(Runnable action) {
        gYesAction = action;
    }

    public void set_no_action(Runnable action) {
        gNoAction = action;
    }

    public void set_dismiss_action(Runnable action) {
        gDismissAction = action;
    }

    private HomeActivity get() {
        return (HomeActivity) gRef.get();
    }

    public static boolean has_conflict_reminds(List<BookInfo> conflicts) {
        if (null == conflicts)
            return false;

        for (BookInfo book : conflicts) {
            Log.d(TAG, "has_conflict_reminds: [name] " + book.getEventName() + ", [id] " + book.getBookId() + ", [type] " + book.getBookType());

        }

        return !conflicts.isEmpty();
    }

    public void show_message(int resId) {
        gMessageId = resId;
        get().runOnUiThread(this::show);
    }

    public void show_message(AppCompatActivity activity, int resId) {
        gMessageId = resId;
        activity.runOnUiThread(this::show);
    }

    public void start_remind() {
        if (gPrimeDtv == null || gMiniEPG == null)
            init_handle();
        if (gPrimeDtv == null || gMiniEPG == null || gMiniEPG.get_follow() == null) {
            Log.e(TAG, "start_remind: something is null");
            return;
        }

        long diffTime = gMiniEPG.get_follow().get_start_time() - gPrimeDtv.get_current_time();
        if (diffTime < 2 * 60 * 1000) {
            Log.e(TAG, "start_remind: diffTime = " + MiniEPG.ms_to_duration(diffTime) + ", diffTimeMs = " + diffTime);
            Snakebar.show(get(), R.string.remind_program_too_close, Snakebar.LENGTH_LONG);
            return;
        }

        new Thread(() -> {

            if (null == gMiniEPG)
                init_handle();

            ProgramInfo channel = gMiniEPG.get_current_channel();
            EPGEvent follow = gMiniEPG.get_follow();

            // handle conflict reminds
            Log.i(TAG, "start_remind: handle conflict reminds");
            BookInfo bookInfo = build_book_info(channel, follow);
            handle_conflict_reminds(bookInfo, channel);

        }, "start_remind").start();
    }

    public void stop_remind() {
        new Thread(() -> {
            if (null == gPrimeDtv || null == gMiniEPG)
                init_handle();

            Log.i(TAG, "stop_remind: show remind cancel dialog");
            set_yes_action(() -> {
                stop_remind_confirm(gMiniEPG.get_current_channel(), gMiniEPG.get_follow());
                gMiniEPG.set_detail_yellow_hint(gMiniEPG.is_mark_record());
            });
            set_no_action(null);
            set_dismiss_action(null);
            show_message(R.string.remind_hint_cancel);
        }, "stop_remind").start();
    }

    private void stop_remind_confirm(ProgramInfo channel, EPGEvent follow) {
        new Thread(() -> {
            String chName = channel.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM) + " " + channel.getDisplayName();
            Log.d(TAG, "stop_remind_confirm: [channel] " + chName + ", [follow] " + follow.get_event_name());

            // delete book
            Log.d(TAG, "stop_remind_confirm: delete book");
            gPrimeDtv.book_info_delete(find_book_id(follow));
            gPrimeDtv.save_table(EnTableType.TIMER);

            // hide remind hint & icon
            Log.d(TAG, "stop_remind_confirm: hide remind hint & icon");
            gMiniEPG.hide_remind_icon();

        }, "stop_remind_confirm").start();
    }

    private void handle_conflict_reminds(BookInfo bookInfo, ProgramInfo channel) {
        List<BookInfo> conflicts = gPrimeDtv.book_info_find_conflict_reminds(bookInfo);

        if (has_conflict_reminds(conflicts)) {
            Log.d(TAG, "handle_conflict_reminds: has conflict reminds, replace remind ?");
            get().runOnUiThread(() -> {
                String programName = "CH" + channel.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM) + " " + bookInfo.getEventName();
                EventDialog dialog = new EventDialog(get());
                dialog.set_title_text(String.format(get().getString(R.string.remind_hint_conflict), programName));
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
        new Thread(() -> {
            Log.d(TAG, "replace_remind: delete conflict book");
            for (BookInfo conflict : conflictReminds)
                gPrimeDtv.book_info_delete(conflict.getBookId());

            Log.d(TAG, "replace_remind: schedule book");
            schedule_remind(bookInfo);
        }).start();
    }

    private void schedule_remind(BookInfo bookInfo) {
        gPrimeDtv.book_info_add(bookInfo);
        gPrimeDtv.set_alarms(get(), bookInfo.get_Intent());
        gPrimeDtv.save_table(EnTableType.TIMER);
        gMiniEPG.show_remind_icon();
        gMiniEPG.set_detail_yellow_hint(gMiniEPG.is_mark_remind());
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

    private int find_book_id(EPGEvent follow) {
        if (null == gPrimeDtv)
            init_handle();

        if (null == follow) {
            Log.e(TAG, "find_book_id: follow is null");
            return -1;
        }

        for (BookInfo book : gPrimeDtv.book_info_get_list()) {
            if (book.getBookType() == BookInfo.BOOK_TYPE_CHANGE_CHANNEL &&
                book.getEpgEventId() == follow.get_event_id())
                return book.getBookId();
        }

        return -1;
    }

    @Override
    public void dismiss() {
        TextView yes = findViewById(R.id.lo_remind_cancel_dialog_yes);
        if (yes != null)
            yes.requestFocus();

        if (gDismissAction != null) {
            gDismissAction.run();
            gDismissAction = null;
        }

        super.dismiss();
    }
}
