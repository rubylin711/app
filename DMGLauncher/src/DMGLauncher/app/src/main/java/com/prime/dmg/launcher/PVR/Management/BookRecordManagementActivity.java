package com.prime.dmg.launcher.PVR.Management;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.CustomView.EventDialog;
import com.prime.dmg.launcher.CustomView.HintBanner;
import com.prime.dmg.launcher.CustomView.MultiSelectionDialog;
import com.prime.dmg.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.dmg.launcher.EPG.MyLinearLayoutManager;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyInfo;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.URLUtils;
import com.prime.dmg.launcher.Utils.Utils;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.SeriesInfo;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class BookRecordManagementActivity extends BaseActivity {

    private static final String TAG = "BookRecordManagementActivity";

    private PrimeDtv g_prime_dtv;
    private List<BookInfo> g_book_record_list;
    private MiddleFocusRecyclerView g_recycle_view;
    private BookRecordAdapter g_book_record_adapter;
    private SeriesAdapter g_series_adapter;
    private HintBanner g_hint_banner;
    private TextView g_no_data_text;
    private ImageView g_poster;
    private ConstraintLayout g_sort_info;
    private TextView g_sort_type;
    private int g_last_selected_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_book_record_management);

        g_prime_dtv = HomeApplication.get_prime_dtv();
        g_hint_banner = findViewById(R.id.book_record_hint_banner);
        g_no_data_text = findViewById(R.id.book_record_no_data);
        g_recycle_view = findViewById(R.id.book_record_book_list);
        g_poster = findViewById(R.id.book_record_poster);
        g_sort_info = findViewById(R.id.book_record_sort_info);
        g_sort_type = findViewById(R.id.book_record_sort_type);
        g_recycle_view.setHasFixedSize(true);
        ((SimpleItemAnimator) Objects.requireNonNull(g_recycle_view.getItemAnimator())).setSupportsChangeAnimations(false);
        g_recycle_view.setLayoutManager(new MyLinearLayoutManager(this, RecyclerView.VERTICAL, false));

        // handle back key pressed
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // if showing series adapter: return to book adapter
                // if showing book adapter: exit
                if (g_recycle_view.getAdapter() instanceof SeriesAdapter) {
                    g_recycle_view.setAdapter(g_book_record_adapter);
                    setup_hint_banner();
                    show_sort_info();

                    focus_item(g_last_selected_position);
                } else {
                    finish();
                }
            }
        });
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
        Log.d(TAG, "onStop: ");
        g_prime_dtv.save_table(EnTableType.TIMER);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp: " + keyCode);

        return super.onKeyUp(keyCode, event);
    }

    private void init_ui() {
        setup_space_info();
        setup_book_record_list();
        setup_hint_banner();
        setup_no_data_text();
    }

    private void setup_space_info() {
        String usbPath = Utils.get_mount_usb_path();
        TextView spaceMessage = findViewById(R.id.book_record_hdd_space_message);
        if (usbPath == null || usbPath.isEmpty() ) {
            spaceMessage.setText(getString(R.string.dvr_mgr_hdd_not_ready));
        } else {
            int recordCount = 0;
            List<Long> usbSize = Utils.get_usb_space_info();
            long totalSize = usbSize.get(0);
            long availableSize = usbSize.get(1);
            long usedSize = totalSize - availableSize;
            int percent = (int) (usedSize * 100 / totalSize);

            Log.d(TAG, "setup_space_info: Total Size     : " + totalSize + " MB");
            Log.d(TAG, "setup_space_info: Available Size : " + availableSize + " MB");

            ProgressBar progressBar = findViewById(R.id.book_record_hdd_space_progress);
            progressBar.setMax(100);
            progressBar.setProgress(percent);

            if (g_prime_dtv != null) {
                g_prime_dtv.pvr_init(usbPath);
                recordCount = g_prime_dtv.pvr_GetRecCount();
            }

            spaceMessage.setText(getString(R.string.dvr_quota_space_hint, percent +"%", recordCount));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setup_book_record_list() {
        g_book_record_list = get_book_record_info_list();
        sort_book_record(g_sort_type.getText().toString());
        g_book_record_adapter = new BookRecordAdapter(g_book_record_list);
        g_recycle_view.setAdapter(g_book_record_adapter);
        g_book_record_adapter.setOnItemClickListener((viewClick, positionClick) -> {
            BookInfo bookInfo = g_book_record_list.get(positionClick);
            if (bookInfo.isSeries()) {
                g_prime_dtv.add_series(bookInfo.getChannelId(), bookInfo.getSeriesRecKey());
                SeriesInfo.Series series = g_prime_dtv.get_series(bookInfo.getChannelId(), bookInfo.getSeriesRecKey());
                g_series_adapter = new SeriesAdapter(series);
                g_series_adapter.setOnKeyListener((viewOnKey, keyCode, event, positionOnKey) -> {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        if (keyCode == KeyEvent.KEYCODE_INFO) {
                            SeriesInfo.Episode episode = series.getEpisodeList().get(positionOnKey);
                            show_info(episode, bookInfo);
                        }
                    }

                    return false;
                });

                g_series_adapter.setOnFocusChangeListener((viewFocusChange, hasFocus, positionFocusChange) -> {
                    if (hasFocus) {
                        SeriesInfo.Episode episode = series.getEpisodeList().get(positionFocusChange);
                        set_program_information(episode);

                        load_poster(bookInfo.getChannelId(), episode.getEventName());
                    }
                });

                g_recycle_view.setAdapter(g_series_adapter);
                set_program_information("", "", ""); // clear info
                clear_poster();
                setup_hint_banner();
                hide_sort_info();

                g_last_selected_position = positionClick;
            }
        });

        g_book_record_adapter.setOnFocusChangeListener((view, hasFocus, position) -> {
            if (hasFocus) {
                BookInfo bookInfo = g_book_record_list.get(position);
                if (bookInfo.isSeries()) {
                    g_hint_banner.hide_intro();
                    g_hint_banner.show_ok();
                } else {
                    g_hint_banner.hide_ok();
                    g_hint_banner.show_intro();
                }

                set_program_information(bookInfo);
                load_poster(bookInfo.getChannelId(), bookInfo.getEventName());
            }
        });

        g_book_record_adapter.setOnKeyListener((view, keyCode, event, position) -> {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_PROG_BLUE -> {
                        BookInfo bookInfo = g_book_record_list.get(position);
                        EventDialog dialog = new EventDialog(this);
                        dialog.set_title_text(getString(R.string.pvr_event_dialog_record_cancel_title, bookInfo.getEventName()));
                        dialog.set_confirm_text(getString(R.string.pvr_event_dialog_record_cancel_ok));
                        dialog.set_cancel_text(getString(R.string.pvr_event_dialog_record_cancel_no));
                        dialog.set_confirm_action(() -> delete_book_info(position));
                        dialog.show();
                        return true;
                    }
                    case KeyEvent.KEYCODE_PROG_GREEN -> {
                        String sort_type_newest = getString(R.string.dvr_record_sort_newest);
                        String sort_type_oldest = getString(R.string.dvr_record_sort_oldest);
                        if (g_sort_type.getText().equals(sort_type_newest)) {
                            g_sort_type.setText(sort_type_oldest);
                            sort_book_record(sort_type_oldest);
                        } else {
                            g_sort_type.setText(sort_type_newest);
                            sort_book_record(sort_type_newest);
                        }

                        g_book_record_adapter.notifyDataSetChanged();
                        return true;
                    }
                    case KeyEvent.KEYCODE_PROG_YELLOW -> {
                        MultiSelectionDialog multi_select_dialog = get_multi_select_dialog();
                        multi_select_dialog.show();
                        setup_hint_banner_multi_selection();
                        return true;
                    }
                    case KeyEvent.KEYCODE_INFO -> {
                        BookInfo bookInfo = g_book_record_list.get(position);
                        if (!bookInfo.isSeries()) {
                            show_info(bookInfo);
                        }
                        return true;
                    }
                }
            }

            return false;
        });
    }

    private MultiSelectionDialog get_multi_select_dialog() {
        List<String> list = new ArrayList<>();
        for (BookInfo book : g_book_record_list) {
            list.add(book.getEventName());
        }

        MultiSelectionDialog multi_select_dialog = new MultiSelectionDialog(this, list);
        multi_select_dialog.setOnDismissListener(dialog -> setup_hint_banner());
        multi_select_dialog.setOnDeleteConfirmListener(deletePositions -> {
            // remove from back to avoid index change
            deletePositions.sort(Comparator.reverseOrder());
            delete_book_infos(deletePositions);
        });

        return multi_select_dialog;
    }

    private void setup_hint_banner() {
        if (g_recycle_view.getAdapter() instanceof SeriesAdapter) {
            setup_hint_banner_series();
        } else {
            setup_hint_banner_book();
        }
    }

    private void setup_hint_banner_book() {
        g_hint_banner.set_hint_ok(R.string.hint_open);
        g_hint_banner.set_hint_intro(R.string.hint_program_intro);
        g_hint_banner.set_hint_green(R.string.hint_rcu_sort);
        g_hint_banner.set_hint_yellow(R.string.pvr_hint_rcu_more_recorded);

        g_hint_banner.hide_red();
        g_hint_banner.hide_back();

        if (g_book_record_list.isEmpty() || g_book_record_adapter.getFocusedBookInfo() == null) {
            g_hint_banner.hide_intro();
            g_hint_banner.hide_ok();
        } else if (g_book_record_adapter.getFocusedBookInfo().isSeries()) {
            g_hint_banner.hide_intro();
            g_hint_banner.show_ok();
        } else {
            g_hint_banner.hide_ok();
            g_hint_banner.show_intro();
        }

        g_hint_banner.show_green();
        g_hint_banner.show_yellow();
        g_hint_banner.show_blue();

        g_hint_banner.enable_shadow(true);
    }

    private void setup_hint_banner_series() {
        g_hint_banner.set_hint_back(R.string.hint_rcu_prev);
        g_hint_banner.set_hint_intro(R.string.hint_program_intro);

        g_hint_banner.hide_red();
        g_hint_banner.hide_ok();
        g_hint_banner.hide_blue();
        g_hint_banner.hide_green();
        g_hint_banner.hide_yellow();

        g_hint_banner.show_back();
        g_hint_banner.show_intro();

        g_hint_banner.enable_shadow(true);
    }

    private void setup_hint_banner_multi_selection() {
        g_hint_banner.set_hint_ok(R.string.settings_hint_rcu_dpad_select);
        g_hint_banner.set_hint_yellow(R.string.hint_rcu_select_all);

        g_hint_banner.hide_back();
        g_hint_banner.hide_green();
        g_hint_banner.hide_red();
        g_hint_banner.hide_intro();
        g_hint_banner.show_yellow();
        g_hint_banner.show_blue();
        g_hint_banner.show_ok();

        g_hint_banner.enable_shadow(true);
    }

    public List<BookInfo> get_book_record_info_list() {
        List<BookInfo> allBookInfoList = new ArrayList<>();

        if (g_prime_dtv != null) {
            allBookInfoList = g_prime_dtv.book_info_get_list();
        }

        Log.d(TAG, "get_book_info_list: All Book Info size = " + allBookInfoList.size());

        List<BookInfo> bookRecordInfoList = new ArrayList<>();
        for (BookInfo bookInfo: allBookInfoList) {
            if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD) {
                bookRecordInfoList.add(bookInfo);
            }
        }

        Log.d(TAG, "get_book_record_info_list: bookRecordInfoList size = " + bookRecordInfoList.size());
        return bookRecordInfoList;
    }

    private void delete_book_info(int position) {
        Log.d(TAG, "delete_book_info: ");

        BookInfo deleteBookInfo = g_book_record_list.get(position);
        if (deleteBookInfo != null && g_book_record_adapter.deleteBookInfo(position)) {
            Log.d(TAG, "delete_book_info: " + deleteBookInfo.ToString());
            g_prime_dtv.book_info_delete(deleteBookInfo.getBookId());
            g_prime_dtv.cancel_alarms(this, deleteBookInfo.get_Intent());
        } else {
            Log.e(TAG, "delete_book_info: fail");
        }

        if (g_book_record_adapter.getItemCount() == 0) {
            set_program_information("", "", ""); // clear info
            clear_poster();
            g_hint_banner.hide_intro();
            g_hint_banner.hide_ok();
            g_no_data_text.setVisibility(View.VISIBLE);
        }
    }

    private void delete_book_infos(List<Integer> positions) {
        Log.d(TAG, "delete_book_infos: positions = " + positions);

        for (int position : positions) {
            BookInfo deleteBookInfo = g_book_record_list.get(position);
            if (deleteBookInfo != null) {
                Log.d(TAG, "delete_book_infos: " + deleteBookInfo.ToString());
                g_prime_dtv.book_info_delete(deleteBookInfo.getBookId());
                g_prime_dtv.cancel_alarms(this, deleteBookInfo.get_Intent());
            } else {
                Log.e(TAG, "delete_book_infos: fail position = " + position);
            }
        }

        g_book_record_adapter.deleteBookInfos(positions);
        if (g_book_record_adapter.getItemCount() == 0) {
            set_program_information("", "", ""); // clear info
            clear_poster();
            g_hint_banner.hide_intro();
            g_hint_banner.hide_ok();
            g_no_data_text.setVisibility(View.VISIBLE);
        }
    }

    private String get_description_from_epg(long channelId, int epgEventId) {
        String description = g_prime_dtv.get_detail_description(channelId, epgEventId);

        if (description == null || description.isEmpty()) {
            description = g_prime_dtv.get_short_description(channelId, epgEventId);
        }

        return description == null ? "" : description;
    }

    private void set_program_information(BookInfo bookInfo) {
        String title = bookInfo.getEventName();
        String duration = "";
        String description = "";

        if (bookInfo.getEpgEventId() == -1 || !bookInfo.isSeries()) {
            // bookInfo.getDuration() = HHmm
            int hour = bookInfo.getDuration() / 100;
            int minute = bookInfo.getDuration() % 100;
            duration = String.format(Locale.getDefault(),
                    "%s %02d:%02d:00",
                    getString(R.string.dvr_video_time_length),
                    hour,
                    minute);
        }

        if (bookInfo.getEpgEventId() == -1) {
            // period record
            description = bookInfo.getEventName();
        } else {
            description = get_description_from_epg(bookInfo.getChannelId(), bookInfo.getEpgEventId());
        }

        set_program_information(title, duration, description);
    }

    private void set_program_information(SeriesInfo.Episode episode) {
        String title = episode.getEventName();

        // episode.getDuration() = seconds
        int hour = episode.getDuration() / 3600;
        int minute = episode.getDuration() % 3600 / 60;
        int second = episode.getDuration() % 60;
        String duration = String.format(Locale.getDefault(),
                "%s %02d:%02d:%02d",
                getString(R.string.dvr_video_time_length),
                hour,
                minute,
                second);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        String description = getString(R.string.dvr_type_next_record_time)
                + " "
                + episode.getStartLocalDateTime().format(formatter);

        set_program_information(title, duration, description);
    }

    private void set_program_information(String title, String duration, String description) {
        TextView textView = findViewById(R.id.book_record_program_title);
        textView.setText(title);

        textView = findViewById(R.id.book_record_program_duration);
        if (duration.isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(duration);
            textView.setVisibility(View.VISIBLE);
        }

        textView = findViewById(R.id.book_record_program_description);
        textView.setText(description);
    }

    private void show_info(BookInfo bookInfo) {
        if (bookInfo == null) {
            return;
        }

        HotkeyInfo hotkeyInfo = new HotkeyInfo(this);
        ProgramInfo channel = g_prime_dtv.get_program_by_channel_id(bookInfo.getChannelId());
        String description;
        if (bookInfo.getEpgEventId() == -1) {
            // period record
            description = bookInfo.getEventName();
        } else {
            description = get_description_from_epg(bookInfo.getChannelId(), bookInfo.getEpgEventId());
        }

        hotkeyInfo.show_detail_book(channel.getDisplayNum(3),
                channel.getDisplayName(),
                bookInfo.getEventName(),
                bookInfo.get_formatted_start_time("MM/dd HH:mm"),
                description);
    }

    private void show_info(SeriesInfo.Episode episode, BookInfo bookInfo) {
        if (episode == null || bookInfo == null) {
            return;
        }

        HotkeyInfo hotkeyInfo = new HotkeyInfo(this);
        ProgramInfo channel = g_prime_dtv.get_program_by_channel_id(bookInfo.getChannelId());
        String description = get_description_from_epg(bookInfo.getChannelId(), bookInfo.getEpgEventId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
        String formatStartTime = episode.getStartLocalDateTime().format(formatter);

        hotkeyInfo.show_detail_book(channel.getDisplayNum(3),
                channel.getDisplayName(),
                episode.getEventName(),
                formatStartTime,
                description);
    }

    private void setup_no_data_text() {
        if (g_book_record_adapter.getItemCount() == 0) {
            g_no_data_text.setVisibility(View.VISIBLE);
        } else {
            g_no_data_text.setVisibility(View.INVISIBLE);
        }
    }

    private void load_poster(long channel_id, String event_name) {
        ProgramInfo channel = g_prime_dtv.get_program_by_channel_id(channel_id);

        if (channel != null) {
            int service_id = channel.getServiceId();
            load_poster(service_id, event_name);
        }
    }

    private void load_poster(int service_id, String event_name) {
        try {
            String url = URLUtils.generate_poster_url(this, service_id, event_name);
            Log.d(TAG, "load_poster: url = " + url);
            Glide.with(this)
                    .load(url)
                    .into(g_poster);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "load_poster: ", e);
        }
    }

    private void clear_poster() {
        g_poster.setImageDrawable(null);
    }

    private void sort_book_record(@NonNull String sort_type) {
        if (sort_type.equals(getString(R.string.dvr_record_sort_oldest))) {
            g_book_record_list.sort((book1, book2)
                    -> Long.signum(book2.get_start_time_stamp() - book1.get_start_time_stamp()));
        } else {
            g_book_record_list.sort((book1, book2)
                    -> Long.signum(book1.get_start_time_stamp() - book2.get_start_time_stamp()));
        }
    }

    private void show_sort_info() {
        g_sort_info.setVisibility(View.VISIBLE);
    }

    private void hide_sort_info() {
        g_sort_info.setVisibility(View.INVISIBLE);
    }

    private void focus_item(int adapter_position) {
        RecyclerView.Adapter<?> adapter = g_recycle_view.getAdapter();
        if (adapter != null
                && (adapter_position < 0 || adapter_position >= adapter.getItemCount())) {
            return;
        }

        g_recycle_view.scrollToPosition(adapter_position);
        g_recycle_view.post(() -> {
            RecyclerView.ViewHolder view_holder
                    = g_recycle_view.findViewHolderForAdapterPosition(adapter_position);
            if (view_holder != null) {
                view_holder.itemView.requestFocus();
            }
        });
    }
}