package com.prime.launcher.PVR.Conflict;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.launcher.BaseActivity;
import com.prime.launcher.BaseDialog;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.launcher.ChannelChangeManager;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.utils.TVMessage;

import java.lang.ref.WeakReference;
import java.util.List;

/** @noinspection FieldCanBeLocal*/
public class AboutToStartDialog extends BaseDialog {
    private static final String TAG = AboutToStartDialog.class.getSimpleName();

    private final WeakReference<AppCompatActivity> Ref;
    private final PrimeDtv PrimeDtv;
    private final ChannelChangeManager ChangeMgr;
    private final BookInfo TimerBook;
    private final long DelayMs;
    private final List<BookInfo> ConflictBooks;
    private CountDownTimer Timer;
    
    public AboutToStartDialog(@NonNull AppCompatActivity activity, BookInfo bookInfo, long delayMs, List<BookInfo> conflictBooks) {
        super(activity, R.style.Theme_Launcher_DialogFullScreen);
        Ref = new WeakReference<>(activity);
        PrimeDtv = HomeApplication.get_prime_dtv();
        TimerBook = bookInfo;
        DelayMs = delayMs;
        ConflictBooks = conflictBooks;
        ChangeMgr = ChannelChangeManager.get_instance(activity.getApplicationContext());
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.semi_transparent);
            getWindow().setWindowAnimations(R.style.Theme_Launcher_DialogAnimation);
        }
        setContentView(R.layout.dialog_recording_is_about_to_start);
        Timer = new CountDownTimer(DelayMs, 1000) {
            @Override public void onTick(long millisUntilFinished) {}
            @Override public void onFinish() {
                Log.d(TAG, "onFinish: Auto close dialog");
                TextView btnConfirm = findViewById(R.id.YES_BUTTON);
                btnConfirm.performClick();
            }
        }.start();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        TextView btnConfirm = findViewById(R.id.YES_BUTTON);
        TextView btnCancel1 = findViewById(R.id.NO_BUTTON);
        TextView btnCancel2 = findViewById(R.id.Third_BUTTON);

        if (ConflictBooks.size() == 1) {
            btnConfirm.setText(getContext().getString(R.string.pvr_jump_conflict_first_item) + "CH " + TimerBook.getChannelNum());
            btnConfirm.setOnClickListener(view -> on_click_change_channel(TimerBook));
            btnCancel1.setOnClickListener(view -> on_click_cancel_record(TimerBook));
            btnCancel2.setVisibility(View.GONE);
        }
        else if (ConflictBooks.size() == 2) {
            BookInfo book1 = ConflictBooks.get(0);
            BookInfo book2 = ConflictBooks.get(1);
            BookInfo targetBook = book1.getChannelId() == TimerBook.getChannelId() ? book2 : book1;

            btnConfirm.setText(getContext().getString(R.string.pvr_jump_conflict_first_item) + "CH " + targetBook.getChannelNum());
            btnCancel1.setText(String.format(getContext().getString(R.string.pvr_jump_conflict_cancel_item), " CH " + book1.getChannelNum() + " "));
            btnCancel2.setText(String.format(getContext().getString(R.string.pvr_jump_conflict_cancel_item), " CH " + book2.getChannelNum() + " "));

            btnConfirm.setOnClickListener(view -> on_click_change_channel(targetBook));
            btnCancel1.setOnClickListener(view -> on_click_cancel_record(book1));
            btnCancel2.setOnClickListener(view -> on_click_cancel_record(book2));
            btnCancel2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Timer != null) {
            Timer.cancel();
            Timer = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            return true;
        return super.onKeyDown(keyCode, event);
    }

    private void on_click_change_channel(BookInfo timerBook) {
        if (get_activity() instanceof BaseActivity baseActivity) {
            Log.d(TAG, "on_click_change_channel: [book name] " + timerBook.getName());

            // change channel
            ChangeMgr.change_channel_by_id(timerBook.getChannelId());
            if (get_activity() instanceof HomeActivity homeActivity)
                homeActivity.get_live_tv_manager().g_miniEPG.show_info_only();

            // delete book
            for (BookInfo book : ConflictBooks)
                PrimeDtv.book_info_delete(book.getBookId());
            PrimeDtv.save_table(EnTableType.TIMER);
            baseActivity.set_fake_book_list(PrimeDtv.book_info_get_list());
        }
        dismiss();
    }

    private void on_click_cancel_record(BookInfo bookInfo) {
        if (get_activity() instanceof BaseActivity baseActivity) {
            Log.d(TAG, "on_click_cancel_record: [book name] " + bookInfo.getName());

            // delete book
            for (BookInfo book : ConflictBooks)
                PrimeDtv.book_info_delete(book.getBookId());
            PrimeDtv.save_table(EnTableType.TIMER);
            baseActivity.set_fake_book_list(PrimeDtv.book_info_get_list());

            // stop timer record
            baseActivity.stop_timer_record(bookInfo);
        }
        dismiss();
    }

    private AppCompatActivity get_activity() {
        return Ref.get();
    }
}
