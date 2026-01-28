package com.prime.dmg.launcher.PVR.Conflict;

import static android.view.View.*;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.BaseDialog;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyRecord;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.utils.TVMessage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ConflictDialog extends BaseDialog {

    private static final String TAG = ConflictDialog.class.getSimpleName();
    private final WeakReference<AppCompatActivity> Ref;
    private final PrimeDtv PrimeDtv;
    private final BookInfo TimerBook;
    private final List<BookInfo> ConflictList;
    private final List<BookInfo> SelectedList;

    // Replace conflict dialog
    ReplaceConflictDialog ReplaceConflictDialog;

    public ConflictDialog(@NonNull AppCompatActivity activity, BookInfo timerBook, List<BookInfo> conflictRecords, List<BookInfo> conflictBooks) {
        super(activity, R.style.Theme_DMGLauncher_DialogFullScreen);
        Ref = new WeakReference<>(activity);
        PrimeDtv = HomeApplication.get_prime_dtv();
        TimerBook = timerBook;
        SelectedList = new ArrayList<>(conflictRecords);
        ConflictList = new ArrayList<>();
        ConflictList.addAll(conflictRecords);
        ConflictList.addAll(conflictBooks);
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.semi_transparent);
            getWindow().setWindowAnimations(R.style.Theme_DMGLauncher_DialogAnimation);
        }
        setContentView(R.layout.dialog_conflict);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // display conflict list
        ConflictListView conflictListView = findViewById(R.id.ConflictListView);
        conflictListView.setup_view(this, ConflictList);

        // set confirm button
        TextView confirmBtn = findViewById(R.id.ConfirmButton);
        confirmBtn.setOnClickListener(this::on_click_confirm);

        // timeout to dismiss
        long delay = TimerBook.getStartTimeMs() - System.currentTimeMillis() - 5000;
        if (delay < 0) delay = 0;
        if (BaseActivity.FOR_TEST_BOOK_CONFLICT) delay = 60000;
        Log.d(TAG, "onStart: count down timer [delay] " + MiniEPG.ms_to_duration(delay) + " (" + delay + ")");
        new CountDownTimer(delay, 1000) {
            @Override public void onTick(long millisUntilFinished) {}
            @Override public void onFinish() {
                if (isShowing()) {
                    Log.d(TAG, "onFinish: timeout to stop conflict dialog");
                    dismiss();
                }
            }
        }.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            return true;
        return super.onKeyDown(keyCode, event);
    }

    public void on_select_book(ImageView checkboxOn, BookInfo selectedBook) {
        if (selected_list_add(selectedBook)) {
            Log.d(TAG, "on_select_book: selected [book name] " + selectedBook.getName());
            checkboxOn.setVisibility(VISIBLE);
        }
        else {
            shake_conflict_hint(5);
            Log.d(TAG, "on_select_book: show dialog, [new book] " + selectedBook.getName());
            ReplaceConflictDialog = new ReplaceConflictDialog(get_activity(), this, selectedBook, SelectedList);
            ReplaceConflictDialog.show();
        }
    }

    public void on_unselect_book(ImageView checkboxOn, BookInfo unselectedBook) {
        if (selected_list_delete(unselectedBook)) {
            Log.d(TAG, "on_unselect_book: unselected [event name] " + unselectedBook.getEventName());
            checkboxOn.setVisibility(INVISIBLE);
        }
    }

    public void on_replace_book(BookInfo oldBook, BookInfo newBook) {
        int oldPos = ConflictList.indexOf(oldBook);
        int newPos = ConflictList.indexOf(newBook);

        if (selected_list_replace(oldBook, newBook)) {
            Log.d(TAG, "on_replace_book: [old book] ("+ oldPos + ") " + oldBook.getName() + ", [new book] (" + newPos + ") " + newBook.getName());
            View oldCheckbox = get_checkbox_on(oldPos);
            View newCheckbox = get_checkbox_on(newPos);
            if (oldCheckbox != null) oldCheckbox.setVisibility(INVISIBLE);
            if (newCheckbox != null) newCheckbox.setVisibility(VISIBLE);
        }
    }

    public void on_click_confirm(View view) {
        Log.d(TAG, "on_click_confirm: click to close conflict dialog");
        dismiss();
    }

    public AppCompatActivity get_activity() {
        return Ref.get();
    }

    public View get_checkbox_on(int position) {
        ConflictListView conflictListView = findViewById(R.id.ConflictListView);
        if (conflictListView == null) {
            Log.e(TAG, "get_checkbox_on: conflictListView is null");
            return null;
        }
        ConflictListAdapter.Holder holder = (ConflictListAdapter.Holder) conflictListView.findViewHolderForAdapterPosition(position);
        if (holder == null) {
            Log.e(TAG, "get_checkbox_on: holder is null");
            return null;
        }
        return holder.checkboxOn;
    }

    @Override
    public void dismiss() {
        super.dismiss();

        // close dialog
        if (ReplaceConflictDialog != null && ReplaceConflictDialog.isShowing())
            ReplaceConflictDialog.dismiss();

        // schedule incoming book
        auto_select_books();
        schedule_incoming_book();
    }

    private void auto_select_books() {
        if (SelectedList.size() >= Pvcfg.NUM_OF_RECORDING)
            return;

        List<BookInfo> conflictList = new ArrayList<>(ConflictList);
        conflictList.sort(Comparator.comparingLong(BookInfo::getStartTimeMs));
        for (BookInfo book : conflictList) {
            String startTime = MiniEPG.ms_to_time(book.getStartTimeMs(), "HH:mm:ss");
            boolean isRecordNow = book.getBookType() == BookInfo.BOOK_TYPE_RECORD_NOW;
            if (isRecordNow) Log.d(TAG, "auto_select_books: recording.. [start time] " + startTime + ", [auto select] " + book.isAutoSelect() + ", [channel] " + book.getChannelNum() + ", [event] " + book.getEventName());
            else             Log.d(TAG, "auto_select_books: conflict... [start time] " + startTime + ", [auto select] " + book.isAutoSelect() + ", [channel] " + book.getChannelNum() + ", [event] " + book.getEventName());

            if (SelectedList.contains(book) || !book.isAutoSelect() || SelectedList.size() == Pvcfg.NUM_OF_RECORDING)
                continue;
            selected_list_add(book);
        }
        for (BookInfo book : SelectedList) {
            String startTime = MiniEPG.ms_to_time(book.getStartTimeMs(), "HH:mm:ss");
            Log.d(TAG, "auto_select_books: auto selected [start time] " + startTime + ", [auto select] " + book.isAutoSelect() + ", [channel] " + book.getChannelNum() + ", [event] " + book.getEventName());
        }
    }

    private void schedule_incoming_book() {
        Context context = getContext().getApplicationContext();

        for (BookInfo one_book : ConflictList) {
            int bookType = one_book.getBookType();
            String channelNum = one_book.getChannelNum();
            String eventName = one_book.getEventName();
            String startTime = MiniEPG.ms_to_time(one_book.getStartTimeMs(), "HH:mm:ss");

            if (SelectedList.contains(one_book)) {
                if (BookInfo.BOOK_TYPE_RECORD_NOW == bookType)
                    Log.d(TAG, "schedule_incoming_book: CONTINUE RECORD !!! [channel] " + channelNum + ", [event] " + eventName + ", [start time] " + startTime);
                else {
                    Log.d(TAG, "schedule_incoming_book: SETUP TIMER !!! [channel] " + channelNum + ", [event] " + eventName + ", [start time] " + startTime);
                    PrimeDtv.set_alarms(context, one_book.get_Intent());
                }
            }
            else {
                if (BookInfo.BOOK_TYPE_RECORD_NOW == bookType) {
                    Log.d(TAG, "schedule_incoming_book: STOP RECORD !!! [channel] " + channelNum + ", [event] " + eventName + ", [start time] " + startTime);
                    HotkeyRecord.record_stop(context, one_book.getChannelId());
                }
                else {
                    Log.d(TAG, "schedule_incoming_book: DELETE BOOK !!! [channel] " + channelNum + ", [event] " + eventName + ", [start time] " + startTime);
                    PrimeDtv.book_info_delete(one_book.getBookId());
                    if (get_activity() instanceof BaseActivity baseActivity)
                        baseActivity.set_fake_book_list(PrimeDtv.book_info_get_list());
                }
            }
        }
        PrimeDtv.save_table(EnTableType.TIMER);
    }

    public boolean selected_list_add(BookInfo selectedBook) {
        if (SelectedList.contains(selectedBook))
            return true;
        if (SelectedList.size() < Pvcfg.NUM_OF_RECORDING) {
            SelectedList.add(selectedBook);
            for (BookInfo book : ConflictList) {
                if (book.getBookType() == BookInfo.BOOK_TYPE_RECORD_NOW &&
                    book.getChannelId() == selectedBook.getChannelId() &&
                    book.getEpgEventId() == selectedBook.getEpgEventId()) {
                    book.setAutoSelect(true);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean selected_list_delete(BookInfo unselectedBook) {
        if (!SelectedList.contains(unselectedBook))
            return true;
        for (BookInfo selectedBook : SelectedList) {
            if (selectedBook.getBookId() == unselectedBook.getBookId() &&
                selectedBook.getChannelId() == unselectedBook.getChannelId() &&
                selectedBook.getEpgEventId() == unselectedBook.getEpgEventId()) {
                SelectedList.remove(unselectedBook);
                for (BookInfo book : ConflictList) {
                    if (book.getBookType() == BookInfo.BOOK_TYPE_RECORD_NOW &&
                        book.getChannelId() == unselectedBook.getChannelId() &&
                        book.getEpgEventId() == unselectedBook.getEpgEventId()) {
                        book.setAutoSelect(false);
                        break;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean selected_list_replace(BookInfo oldBook, BookInfo newBook) {
        if (!SelectedList.contains(oldBook))
            return false;
        if (SelectedList.contains(newBook))
            return false;
        SelectedList.remove(oldBook);
        SelectedList.add(newBook);
        return true;
    }

    /** @noinspection SameParameterValue*/
    private void shake_conflict_hint(int distance) {
        int colorRed = getContext().getResources().getColor(R.color.red, null);
        int colorGray = getContext().getResources().getColor(R.color.gray, null);

        TextView hint = findViewById(R.id.ConflictHint);
        hint.setTranslationX(distance);
        hint.setTextColor(colorRed);

        new CountDownTimer(500, 100) {
            @Override public void onTick(long millisUntilFinished) {
                if ((millisUntilFinished / 100) % 2 == 0)
                    hint.setTranslationX(distance);
                else
                    hint.setTranslationX(-distance);
            }
            @Override public void onFinish() {
                hint.setTranslationX(0);
                hint.setTextColor(colorGray);
            }
        }.start();
    }
}
