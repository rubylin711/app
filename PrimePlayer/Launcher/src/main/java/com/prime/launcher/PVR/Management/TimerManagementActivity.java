package com.prime.launcher.PVR.Management;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.prime.launcher.BaseActivity;
import com.prime.launcher.EPG.EpgActivity;
import com.prime.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.launcher.EPG.MyLinearLayoutManager;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.launcher.Utils.Utils;
import com.prime.launcher.PrimeDtv;
import com.prime.launcher.Receiver.PrimeTimerReceiver;
import com.prime.launcher.Receiver.PrimeUsbReceiver;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.utils.TVMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TimerManagementActivity extends BaseActivity implements PrimeDtv.LauncherCallback {
    private static final String TAG = TimerManagementActivity.class.getSimpleName();

    private PrimeDtv g_PrimeDtv;
    private MiddleFocusRecyclerView g_TimerListView;
    private List<BookInfo> g_conflictBookInfo;
    private int g_conflictBookInfoIndex;
    private BookInfo g_newConflictBookInfo;

    public interface Callback {
        void set_timer_channel(ProgramInfo programInfo);
        void set_timer_date(int year, int month, int day);
    } Callback g_callback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_management);
    }

    @Override
    protected void onStart() {
        super.onStart();
        g_PrimeDtv = HomeApplication.get_prime_dtv();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        init_ui();
    }

    @Override
    protected void onStop() {
        super.onStop();
        g_PrimeDtv.save_table(EnTableType.TIMER);
    }

    private void init_ui() {
        setup_space_info();
        setup_book_record_list();
        set_listener();
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
        String action = intent.getAction();
        if (action == null) {
            Log.e(TAG, "onBroadcastMessage: action == null");
            return;
        }

        switch (action) {
            case PrimeTimerReceiver.ACTION_TIMER_RECORD:
                BookInfo bookInfo = new BookInfo(intent.getExtras());
                Log.d(TAG, "onBroadcastMessage: ACTION_TIMER_RECORD " + bookInfo.getEventName());
                break;
            case PrimeUsbReceiver.ACTION_MEDIA_MOUNTED:
                break;
            case PrimeUsbReceiver.ACTION_MEDIA_UNMOUNTED:
                finish();
                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp: ");
        if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            open_epg();
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            open_book_dialog();
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            delete_book_info();
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_BACK) {
            ConstraintLayout conflictLayer = findViewById(R.id.conflict_timer_layer);
            if (conflictLayer.getVisibility() == View.VISIBLE) {
                conflictLayer.setVisibility(View.GONE);
                g_TimerListView.requestFocus();
                return true;
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    private void on_construct_PrimeDtv() {
        Log.d(TAG, "on_construct_PrimeDtv: ");
        setup_book_record_list();
    }

    private void setup_space_info() {
        List<Long> usbSize = Utils.get_usb_space_info();
        long totalSize = usbSize.get(0);
        long availableSize = usbSize.get(1);
        long usedSize = totalSize - availableSize;
        int percent = (int) (usedSize * 100 / totalSize);

        Log.d(TAG, "setup_space_info: Total Size     : " + totalSize + " MB");
        Log.d(TAG, "setup_space_info: Available Size : " + availableSize + " MB");

        ProgressBar progressBar = findViewById(R.id.timer_hdd_space_progress);
        progressBar.setMax(100);
        progressBar.setProgress(percent);

        int recordCount = g_PrimeDtv != null ? g_PrimeDtv.pvr_GetRecCount() : 0;
        TextView spaceMessage = findViewById(R.id.timer_hdd_space_message);
        spaceMessage.setText(getString(R.string.dvr_quota_space_hint, percent +"%", recordCount));
    }

    private void setup_book_record_list() {
        g_TimerListView = findViewById(R.id.timer_book_list);
        g_TimerListView.setHasFixedSize(true);
        ((SimpleItemAnimator) Objects.requireNonNull(g_TimerListView.getItemAnimator())).setSupportsChangeAnimations(false);
        g_TimerListView.setLayoutManager(new MyLinearLayoutManager(this, RecyclerView.VERTICAL, false));
        g_TimerListView.setAdapter(new TimerListAdapter(this, get_book_record_info_list()));
    }

    private void set_listener() {
        ConstraintLayout conflictLayer = findViewById(R.id.conflict_timer_layer);

        TextView newBookInfoText = findViewById(R.id.conflict_timer_new_item);
        newBookInfoText.setOnClickListener(view -> {
            g_PrimeDtv.book_info_add(g_newConflictBookInfo);
            delete_conflict_book_info();
            conflictLayer.setVisibility(View.GONE);
            g_TimerListView.requestFocus();
        });

        TextView conflictBookInfoText = findViewById(R.id.conflict_timer_old_item);
        conflictBookInfoText.setOnClickListener(view -> {
            g_conflictBookInfoIndex++;
            if (g_conflictBookInfo.size()>g_conflictBookInfoIndex) {
                show_next_conflict(g_conflictBookInfo.get(g_conflictBookInfoIndex));
                newBookInfoText.requestFocus();
            }
            else {
                conflictLayer.setVisibility(View.GONE);
                g_TimerListView.requestFocus();
            }
        });

    }

    public List<BookInfo> get_book_record_info_list() {
        List<BookInfo> allBookInfoList = new ArrayList<>();

        if (g_PrimeDtv != null) {
            allBookInfoList = g_PrimeDtv.book_info_get_list();
            //allBookInfoList = get_fake_book_data();
        }

        Log.d(TAG, "get_book_info_list: All Book Info size = " + allBookInfoList.size());

        List<BookInfo> bookRecordInfoList = new ArrayList<>();
        if (allBookInfoList == null || allBookInfoList.isEmpty() || allBookInfoList.size() == 0) {
            return bookRecordInfoList;
        }

        for (BookInfo bookInfo: allBookInfoList) {
            if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD && bookInfo.getEpgEventId() < 0)
                bookRecordInfoList.add(bookInfo);
        }

        Log.d(TAG, "get_book_record_info_list: bookRecordInfoList size = " + bookRecordInfoList.size());
        return bookRecordInfoList;
    }

    public PrimeDtv get_dtv() {
        return g_PrimeDtv;
    }

    public void register_callback(Callback callback) {
        g_callback = callback;
    }

    public List<ProgramInfo> get_all_tv_program_info_list() {
          return Utils.get_all_tv_program_info_list(g_PrimeDtv);
    }

    public void change_channel_from_zapping(ProgramInfo programInfo) {
        g_callback.set_timer_channel(programInfo);
    }

    public void on_click_day(int year, int month, int day) {
        g_callback.set_timer_date(year, month, day);
    }

    public int get_new_book_id() {
        List<BookInfo> allBookInfoList = new ArrayList<>();

        if (g_PrimeDtv != null) {
            allBookInfoList = g_PrimeDtv.book_info_get_list();

        }
        Log.d(TAG, "get_new_book_id: All Book Info size = " + allBookInfoList.size());

        if (allBookInfoList == null || allBookInfoList.size() == 0)
            return 0;

        return allBookInfoList.get(allBookInfoList.size()-1).getBookId() +1;
    }

    public List<BookInfo> book_info_find_conflict_books(BookInfo bookInfo) {
        return g_PrimeDtv.book_info_find_conflict_records(bookInfo);
    }

    public void new_timer(BookInfo bookInfo) {
        TimerListAdapter timerListAdapter = (TimerListAdapter) g_TimerListView.getAdapter();
        List<BookInfo> bookRecordInfoList = get_book_record_info_list();
        bookRecordInfoList.add(bookInfo);
        Log.d(TAG, "new_timer: bookRecordInfoList size = " + bookRecordInfoList.size());
        timerListAdapter.update_list(bookRecordInfoList);

        g_PrimeDtv.book_info_add(bookInfo);
        g_PrimeDtv.set_alarms(this, g_PrimeDtv.get_book_info_intent(bookInfo));
    }

    public void show_conflict(List<BookInfo> conflictBookInfoList, BookInfo newBookInfo) {
        g_conflictBookInfo = conflictBookInfoList;
        g_conflictBookInfoIndex = 0;
        g_newConflictBookInfo = newBookInfo;
        runOnUiThread(() -> {
            ConstraintLayout conflictLayer = findViewById(R.id.conflict_timer_layer);
            conflictLayer.setVisibility(View.VISIBLE);
            TextView newBookInfoText = findViewById(R.id.conflict_timer_new_item);
            String newBookInfoString = get_record_info_string(newBookInfo);
            newBookInfoText.setText(newBookInfoString);
            newBookInfoText.requestFocus();

            show_next_conflict(conflictBookInfoList.get(g_conflictBookInfoIndex));
        });
    }

    void show_next_conflict(BookInfo conflictBookInfo) {
        TextView conflictBookInfoText = findViewById(R.id.conflict_timer_old_item);
        String conflictBookInfoString = get_record_info_string(conflictBookInfo);
        conflictBookInfoText.setText(conflictBookInfoString);
    }

    List<BookInfo> get_fake_book_data() {
        List<BookInfo> fakedataList = new ArrayList<>();
        BookInfo bookInfo = new BookInfo();
        bookInfo.setBookId(0);
        bookInfo.setChannelId(0);
        bookInfo.setGroupType(0);
        bookInfo.setEventName("節目(零)");
        bookInfo.setBookType(0);
        bookInfo.setBookCycle(0);
        bookInfo.setYear(2025);
        bookInfo.setMonth(3);
        bookInfo.setDate(13);
        bookInfo.setWeek(0);
        bookInfo.setStartTime(1430);
        bookInfo.setDuration(100);
        bookInfo.setEnable(0);

        BookInfo bookInfo1 = new BookInfo();
        bookInfo1.setBookId(1);
        bookInfo1.setChannelId(1);
        bookInfo1.setGroupType(0);
        bookInfo1.setEventName("節目(一)");
        bookInfo1.setBookType(0);
        bookInfo1.setBookCycle(1);
        bookInfo1.setYear(2025);
        bookInfo1.setMonth(3);
        bookInfo1.setDate(13);
        bookInfo1.setWeek(0);
        bookInfo1.setStartTime(30);
        bookInfo1.setDuration(30);
        bookInfo1.setEnable(0);

        BookInfo bookInfo2 = new BookInfo();
        bookInfo2.setBookId(1);
        bookInfo2.setChannelId(1);
        bookInfo2.setGroupType(0);
        bookInfo2.setEventName("節目(二)");
        bookInfo2.setBookType(0);
        bookInfo2.setBookCycle(1);
        bookInfo2.setYear(2025);
        bookInfo2.setMonth(3);
        bookInfo2.setDate(13);
        bookInfo2.setWeek(0);
        bookInfo2.setStartTime(30);
        bookInfo2.setDuration(30);
        bookInfo2.setEnable(0);

        BookInfo bookInfo3 = new BookInfo();
        bookInfo3.setBookId(0);
        bookInfo3.setChannelId(0);
        bookInfo3.setGroupType(0);
        bookInfo3.setEventName("節目(三)");
        bookInfo3.setBookType(0);
        bookInfo3.setBookCycle(2);
        bookInfo3.setYear(2025);
        bookInfo3.setMonth(3);
        bookInfo3.setDate(13);
        bookInfo3.setWeek(0x1);
        bookInfo3.setStartTime(1430);
        bookInfo3.setDuration(100);
        bookInfo3.setEnable(0);

        fakedataList.add(bookInfo);
        fakedataList.add(bookInfo1);
        fakedataList.add(bookInfo2);
        fakedataList.add(bookInfo3);
        /*fakedataList.add(bookInfo1);
        fakedataList.add(bookInfo2);
        fakedataList.add(bookInfo3);
        fakedataList.add(bookInfo1);
        fakedataList.add(bookInfo2);
        fakedataList.add(bookInfo3);
        fakedataList.add(bookInfo1);
        fakedataList.add(bookInfo2);
        fakedataList.add(bookInfo3);
        fakedataList.add(bookInfo1);
        fakedataList.add(bookInfo2);
        fakedataList.add(bookInfo3);*/

        return fakedataList;
    }

    void open_epg() {
        if (is_conflict_layer_show())
            return;

        Log.d(TAG, "open_epg: ");
        Intent intent = new Intent(this, EpgActivity.class);
        //intent.putExtra("EXTRA_MESSAGE", "Hello from MainActivity");
        this.startActivity(intent);
    }

    void open_book_dialog() {
        if (is_conflict_layer_show())
            return;

        Log.d(TAG, "open_book_dialog: ");

        NewTimerDialog newTimerDialog = new NewTimerDialog(this);
        newTimerDialog.show();
        /*SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date Time = new Date();
        String str = format.format(Time.getTime());

        Calendar ca = Calendar.getInstance();
        ca.setTime(Time);

        BookInfo bookInfo = new BookInfo();
        bookInfo.setBookId((int) ca.getTime().getTime());
        bookInfo.setChannelId(0);
        bookInfo.setGroupType(0);
        bookInfo.setEventName("節目(零)" + str);
        bookInfo.setBookType(0);
        bookInfo.setBookCycle(0);
        bookInfo.setYear(ca.get(Calendar.YEAR));
        bookInfo.setMonth(ca.get(Calendar.MONTH));
        bookInfo.setDate(ca.get(Calendar.DATE));
        bookInfo.setWeek(0);
        bookInfo.setStartTime(Time.getHours()*100+Time.getMinutes()+1);
        bookInfo.setDuration(100);
        bookInfo.setEnable(0);

        Date date = new Date();
        long dateOffsetMills = 20 *1000;
        long dateMills = date.getTime() + dateOffsetMills;
        date.setTime(dateMills);

        ca.setTime(date);
        if (g_PrimeDtv == null) Log.d(TAG, "open_book_dialog: g_PrimeDtv == null");

        g_PrimeDtv.book_info_add(bookInfo);
        g_PrimeDtv.set_alarms(this, bookInfo.get_Intent());

        Log.d(TAG, "open_book_dialog: bookinfolist size = " + g_PrimeDtv.book_info_get_list().size());

        TimerListAdapter timerListAdapter = (TimerListAdapter)g_TimerListView.getAdapter();
        timerListAdapter.update_list(get_book_record_info_list());*/
    }

    void delete_book_info() {
        if (is_conflict_layer_show())
            return;

        Log.d(TAG, "delete_book_info: ");

        if (null == g_TimerListView)
            return;

        TimerListAdapter timerListAdapter = (TimerListAdapter)g_TimerListView.getAdapter();
        if (timerListAdapter == null)
            return;

        BookInfo deleteBookInfo = timerListAdapter.get_focus_book_info();
        //Log.d(TAG, "delete_book_info: deleteBookInfo = " + deleteBookInfo.getEventName());

        if (deleteBookInfo == null) {
            Log.e(TAG, "delete_book_info: deleteBookInfo == null");
            return;
        }

        if (timerListAdapter.delete_book_info()) {
            g_PrimeDtv.book_info_delete(deleteBookInfo.getBookId());
            g_PrimeDtv.cancel_alarms(this, g_PrimeDtv.get_book_info_intent(deleteBookInfo));
        }
        else Log.e(TAG, "delete_book_info: fail");
    }

    String get_record_info_string(BookInfo bookInfo) {
        ProgramInfo programInfo = g_PrimeDtv.get_program_by_channel_id(bookInfo.getChannelId());
        return programInfo.getDisplayNum() + ". " + bookInfo.getEventName() + " " + g_PrimeDtv.get_book_cycle_string(bookInfo,this) + " "+ bookInfo.get_time_to_sting_format();
    }

    void delete_conflict_book_info() {
        Log.d(TAG, "delete_conflict_book_info: ");
        if (null == g_TimerListView)
            return;

        TimerListAdapter timerListAdapter = (TimerListAdapter)g_TimerListView.getAdapter();
        if (timerListAdapter == null)
            return;

        for (BookInfo bookInfo:g_conflictBookInfo) {
            Log.i(TAG, "delete_conflict_book_info: " + bookInfo.ToString());
            g_PrimeDtv.book_info_delete(bookInfo.getBookId());
        }

        List<BookInfo> newbookInfoList = get_book_record_info_list();

        timerListAdapter.update_list(newbookInfoList);
    }

    boolean is_conflict_layer_show() {
        ConstraintLayout conflictLayer = findViewById(R.id.conflict_timer_layer);
        return conflictLayer.getVisibility() == View.VISIBLE;
    }
}
