package com.prime.launcher.PVR.Management;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.prime.launcher.BaseDialog;
import com.prime.launcher.Home.Hotkey.HotkeyRecord;
import com.prime.launcher.Home.LiveTV.Zapping.ZappingDialog;
import com.prime.launcher.R;
import com.prime.launcher.ChannelChangeManager;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.utils.TVMessage;

import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;

public class NewTimerDialog extends BaseDialog implements TimerManagementActivity.Callback{
    String TAG = NewTimerDialog.class.getSimpleName();
    WeakReference<AppCompatActivity> g_ref;
    private int g_year, g_month, g_day, g_time_hour, g_time_minute, g_duration_hour, g_duration_minute, g_cycle_type, g_cycle_day;
    private int g_input_time_length = 0, g_input_duration_length = 0;

    private ProgramInfo g_ProgramInfo;

    public NewTimerDialog(@NonNull Context context) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>((AppCompatActivity) context);
        register_callback();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.color.trans);
            //getWindow().setWindowAnimations(R.style.Theme_Launcher_DialogAnimation);
        }
        setContentView(R.layout.dialog_new_timer);
        init_ui();
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(hide_calendar()) {
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void register_callback() {
        get_activity().register_callback(this);
    }

    private void init_ui() {
        set_timer_channel(null);
        set_timer_date(0, 0, 0);
        set_timer_time("", "", true);
        set_timer_duration("", "", true);
        set_timer_cycle_type(true);

        set_on_click_listener();
    }

    private void set_on_click_listener() {
        ConstraintLayout selectChannelView = findViewById(R.id.new_timer_channel_select_item);
        selectChannelView.setOnClickListener(view ->{
            ZappingDialog zappingDialog = new ZappingDialog(get_activity(), "", get_all_tv_program_info_list());
            zappingDialog.show();
        });

        ConstraintLayout reserveDateView = findViewById(R.id.new_timer_reserve_date_item);
        reserveDateView.setOnClickListener(view ->{
            init_calendar();
            set_focus_container_layer(false);
        });

        ConstraintLayout reserveTimeView = findViewById(R.id.new_timer_reserve_time_item);
        reserveTimeView.setOnKeyListener((view, keyCode, keyEvent) ->{
            if (keyEvent.getAction() == KeyEvent.ACTION_UP)
                return false;

            //Log.d(TAG, "reserve Time key listener: start keyCode = " + keyCode);

            if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                input_timer_time(String.valueOf(keyCode - 7));
                g_input_time_length++;
                check_time();
                return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                remove_time_character();
                g_input_time_length--;
                return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (g_input_time_length < 4) {
                    set_timer_time("", "", true);
                    g_input_time_length = 4;
                }
            }
            return false;
        });

        ConstraintLayout reserveDurationView = findViewById(R.id.new_timer_reserve_duration_item);
        reserveDurationView.setOnKeyListener((view, keyCode, keyEvent) ->{
            if (keyEvent.getAction() == KeyEvent.ACTION_UP)
                return false;
            //Log.d(TAG, "reserve Duration key listener: start keyCode = " + keyCode);

            if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                input_duration_time(String.valueOf(keyCode - 7));
                g_input_duration_length++;
                check_duration();
                return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                remove_duration_character();
                g_input_duration_length--;
                return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (g_input_duration_length < 4) {
                    set_timer_duration("", "", true);
                    g_input_duration_length = 4;
                }
            }
            return false;
        });

        ConstraintLayout reserveCycleTypeView = findViewById(R.id.new_timer_reserve_cycle_type_item);
        reserveCycleTypeView.setOnKeyListener((view, keyCode, keyEvent) ->{
            if (keyEvent.getAction() == KeyEvent.ACTION_UP)
                return false;
            //Log.d(TAG, "reserve Cycle Type key listener: start keyCode = " + keyCode);

            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                g_cycle_type--;
                if (g_cycle_type < BookInfo.BOOK_CYCLE_ONETIME)
                    g_cycle_type = BookInfo.BOOK_CYCLE_WEEKDAYS;
                set_timer_cycle_type(false);
                return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                g_cycle_type++;
                if (g_cycle_type > BookInfo.BOOK_CYCLE_WEEKDAYS)
                    g_cycle_type = BookInfo.BOOK_CYCLE_ONETIME;
                set_timer_cycle_type(false);
                return true;
            }
            return false;
        });

        ConstraintLayout reserveCycleDayView = findViewById(R.id.new_timer_reserve_cycle_day_item);
        reserveCycleDayView.setOnKeyListener((view, keyCode, keyEvent) ->{
            if (keyEvent.getAction() == KeyEvent.ACTION_UP)
                return false;
            Log.d(TAG, "reserve Cycle Day key listener: start keyCode = " + keyCode);

            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                g_cycle_day--;
                g_day--;
                if (g_cycle_day < 1)
                    g_cycle_day = 7;
                set_timer_cycle_day();
                return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                g_cycle_day++;
                g_day++;
                if (g_cycle_day > 7)
                    g_cycle_day = 1;
                set_timer_cycle_day();
                return true;
            }
            return false;
        });

        TextView addTimer = findViewById(R.id.new_timer_new_button);
        addTimer.setOnClickListener(view ->{
            new_timer();
        });
    }

    private List<ProgramInfo> get_all_tv_program_info_list() {
        return get_activity().get_all_tv_program_info_list();
    }

    private void init_calendar() {
        PrimeCalendarView calendarView = findViewById(R.id.new_timer_calendar_view);
        calendarView.set_data(get_activity());
        calendarView.setVisibility(View.VISIBLE);
        calendarView.request_focus_on_year();
    }

    private void set_focus_container_layer(boolean flag) {
        findViewById(R.id.new_timer_channel_select_item).setFocusable(flag);
        findViewById(R.id.new_timer_reserve_date_item).setFocusable(flag);
        findViewById(R.id.new_timer_reserve_time_item).setFocusable(flag);
        findViewById(R.id.new_timer_reserve_duration_item).setFocusable(flag);
        findViewById(R.id.new_timer_reserve_cycle_type_item).setFocusable(flag);
        findViewById(R.id.new_timer_reserve_cycle_day_item).setFocusable(flag);
        findViewById(R.id.new_timer_reserve_type_item).setFocusable(flag);
        findViewById(R.id.new_timer_new_button).setFocusable(flag);
    }

    private TimerManagementActivity get_activity() {
        return (TimerManagementActivity)g_ref.get();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void set_timer_channel(ProgramInfo programInfo) {
        if (programInfo == null) {
            programInfo = get_cur_channel();
        }

        if (programInfo == null)
            return;

        TextView selectChannel = findViewById(R.id.new_timer_channel);
        selectChannel.setText(programInfo.getDisplayNum() + " " + programInfo.getDisplayName());
        g_ProgramInfo = programInfo;
    }

    private ProgramInfo get_cur_channel() {
        ChannelChangeManager channelChangeManager = ChannelChangeManager.get_instance(get_activity());
        return  channelChangeManager.get_cur_channel();
    }

    private boolean hide_calendar() {
        PrimeCalendarView calendarView = findViewById(R.id.new_timer_calendar_view);
        boolean returnValue = calendarView.is_show();
        if (returnValue) {
            calendarView.setVisibility(View.GONE);
            set_focus_container_layer(true);
            findViewById(R.id.new_timer_reserve_date_item).requestFocus();
        }
        return returnValue;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void set_timer_date(int year, int month, int day) {
        TextView timerDate = findViewById(R.id.new_timer_date);
        String dateString;
        Calendar calendar = Calendar.getInstance();
        if (year == 0) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH)+1;
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }
        else {
            hide_calendar();
        }
        dateString = year + "/" +String.format("%02d", month) + "/" + String.format("%02d", day);
        timerDate.setText(dateString);

        g_year = year;
        g_month = month;
        g_day = day;
        calendar.set(g_year, g_month-1, g_day);
        g_cycle_day = calendar.get(Calendar.DAY_OF_WEEK);
    }

    @SuppressLint("DefaultLocale")
    private void set_timer_time(String hour, String minute, boolean initFlag) {
        String timeString;

        if (initFlag) {
            LocalTime localTime = LocalTime.now();
            g_time_hour = localTime.getHour();
            g_time_minute = localTime.getMinute();
            hour = String.format("%02d", g_time_hour) ;   // 24小時制
            minute = String.format("%02d", g_time_minute);
            g_input_time_length = 4;
        }

        timeString = hour + ":" + minute;
        TextView timerTime = findViewById(R.id.new_timer_time);
        timerTime.setText(timeString);
    }

    @SuppressLint("DefaultLocale")
    private void input_timer_time(String number) {
        if (g_input_time_length == 4)
            g_input_time_length = 0;

        String hour, minute;
        if (g_input_time_length == 0) {
            hour = number + "_";
            minute = "  ";
            g_time_hour = Integer.parseInt(number)*10;
            set_timer_time(hour, minute, false);
        }
        else if (g_input_time_length == 1) {
            hour = g_time_hour/10 + number;
            minute = "_ ";
            g_time_hour += Integer.parseInt(number);
            set_timer_time(hour, minute, false);
        }
        else if (g_input_time_length == 2) {
            hour = String.format("%02d", g_time_hour);;
            g_time_minute = Integer.parseInt(number)*10;
            minute = number + "_";
            set_timer_time(hour, minute, false);
        }
        else if (g_input_time_length == 3) {
            hour = String.format("%02d", g_time_hour);;
            minute = g_time_minute/10 + number;
            g_time_minute += Integer.parseInt(number);
            set_timer_time(hour, minute, false);
        }

        Log.d(TAG, "input_timer_time: hour = " + g_time_hour + " minute:" + g_time_minute);
    }

    @SuppressLint("DefaultLocale")
    private void remove_time_character() {
        if (g_input_time_length < 0) {
            g_input_time_length = 1;
            return;
        }

        String hour, minute;
        if (g_input_time_length == 1) {
            hour = "_ ";
            minute = "  ";
            g_time_hour = 0;
            set_timer_time(hour, minute, false);
        }
        else if (g_input_time_length == 2) {
            g_time_hour -= (g_time_hour%10);
            hour = g_time_hour/10 + "_";
            minute = "  ";
            set_timer_time(hour, minute, false);
        }
        else if (g_input_time_length == 3) {
            hour = String.format("%02d", g_time_hour);
            g_time_minute = 0;
            minute = "_ ";
            set_timer_time(hour, minute, false);
        }
        else if (g_input_time_length == 4) {
            hour = String.format("%02d", g_time_hour);
            g_time_minute -= (g_time_minute%10);
            minute = g_time_minute/10 + "_";
            set_timer_time(hour, minute, false);
        }

        Log.d(TAG, "remove_time_character: hour = " + g_time_hour + " minute:" + g_time_minute);
    }

    private void check_time() {
        if (g_input_time_length != 4)
            return ;

        if (g_time_hour > 24 || g_time_minute > 59)
            set_timer_time("", "", true);
    }

    private void set_timer_duration(String hour, String minute, boolean initFlag) {
        String durationString;
        if (initFlag) {
            g_duration_hour = 0;
            g_duration_minute = 30;
            hour = String.format("%02d", g_duration_hour) ;   // 24小時制
            minute = String.format("%02d", g_duration_minute);
        }

        durationString = hour + get_string(R.string.hrs) + minute + get_string(R.string.mins);
        TextView durationTime = findViewById(R.id.new_timer_duration);
        durationTime.setText(durationString);
    }

    private void input_duration_time(String number) {
        if (g_input_duration_length == 4)
            g_input_duration_length = 0;

        String hour = "", minute = "";
        if (g_input_duration_length == 0) {
            hour = number + "_";
            minute = "  ";
            g_duration_hour = Integer.parseInt(number)*10;
        }
        else if (g_input_duration_length == 1) {
            hour = g_duration_hour/10 + number;
            minute = "_ ";
            g_duration_hour += Integer.parseInt(number);
        }
        else if (g_input_duration_length == 2) {
            hour = String.valueOf(g_duration_hour);
            g_duration_minute = Integer.parseInt(number)*10;
            minute = number + "_";
        }
        else if (g_input_duration_length == 3) {
            hour = String.valueOf(g_duration_hour);
            minute = g_duration_minute/10 + number;
            g_duration_minute += Integer.parseInt(number);
        }
        set_timer_duration(hour, minute, false);

        Log.d(TAG, "input_duration_time: hour = " + g_duration_hour + " minute:" + g_duration_minute);
    }

    private void check_duration() {
        if (g_input_duration_length != 4)
            return ;

        if (g_duration_hour > 24 || g_duration_minute > 59)
            set_timer_duration("", "", true);
    }

    @SuppressLint("DefaultLocale")
    private void remove_duration_character() {
        if (g_input_duration_length < 0) {
            g_input_duration_length = 1;
            return;
        }

        String hour = "", minute = "";
        if (g_input_duration_length == 1) {
            hour = "_ ";
            minute = "  ";
            g_duration_hour = 0;
        }
        else if (g_input_duration_length == 2) {
            g_duration_hour -= (g_duration_hour%10);
            hour = g_duration_hour/10 + "_";
            minute = "  ";
        }
        else if (g_input_duration_length == 3) {
            hour = String.format("%02d", g_duration_hour);
            g_duration_minute = 0;
            minute = "_ ";
        }
        else if (g_input_duration_length == 4) {
            hour = String.format("%02d", g_duration_hour);
            g_duration_minute -= (g_duration_minute%10);
            minute = g_duration_minute/10 + "_";
        }
        set_timer_duration(hour, minute, false);

        Log.d(TAG, "remove_duration_character: hour = " + g_duration_hour + " minute:" + g_duration_minute);
    }

    private void set_timer_cycle_type( boolean initFlag) {
        String cycleTypeString, cycleDayString;
        if (initFlag) {
            g_cycle_type = BookInfo.BOOK_CYCLE_ONETIME;
        }

        ConstraintLayout reserveDateView = findViewById(R.id.new_timer_reserve_date_item);
        if (g_cycle_type != BookInfo.BOOK_CYCLE_ONETIME) {
            Calendar matchDateCalendar = get_match_cycle_type_date();
            set_timer_date(matchDateCalendar.get(Calendar.YEAR), matchDateCalendar.get(Calendar.MONTH) +1, matchDateCalendar.get(Calendar.DAY_OF_MONTH));
            reserveDateView.setFocusable(false);
        }
        else {
            set_timer_date(0, 0, 0);
            reserveDateView.setFocusable(true);
        }

        cycleTypeString = get_cycle_type_string(g_cycle_type);
        TextView cycleType = findViewById(R.id.new_timer_cycle);
        cycleType.setText(cycleTypeString);

        cycleDayString = get_cycle_day_string(g_cycle_day);
        TextView cycleDay = findViewById(R.id.new_timer_cycle_day);
        cycleDay.setText(cycleDayString);

        ConstraintLayout timerReserveCycleDay =  findViewById(R.id.new_timer_reserve_cycle_day_item);
        if (g_cycle_type == BookInfo.BOOK_CYCLE_WEEKLY)
            timerReserveCycleDay.setVisibility(View.VISIBLE);
        else
            timerReserveCycleDay.setVisibility(View.GONE);
    }

    private void set_timer_cycle_day() {
        String cycleDayString;
        cycleDayString = get_cycle_day_string(g_cycle_day);
        TextView cycleDay = findViewById(R.id.new_timer_cycle_day);
        cycleDay.setText(cycleDayString);

        //Log.d(TAG, "set_timer_cycle_day: year = " + g_year + " month = " + g_month + " day = " + g_day);

        Calendar currentCalendar = Calendar.getInstance();
        //test change month event
        //currentCalendar.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), 30);

        //Log.d(TAG, "set_timer_cycle_day: current year = " + currentCalendar.get(Calendar.YEAR)
        //                                            + " month = " + currentCalendar.get(Calendar.MONTH)
        //                                            + " day = " + currentCalendar.get(Calendar.DATE));

        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(g_year, g_month-1, g_day);

        if (currentCalendar.after(newCalendar))
            newCalendar.add(Calendar.DATE, 7);
        else if (check_two_days_diff_7(currentCalendar, newCalendar)) {
            newCalendar.add(Calendar.DATE, -7);
        }

        set_timer_date(newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH)+1, newCalendar.get(Calendar.DATE));

    }

    private String get_string(int stingId) {
        return get_activity().getString(stingId);
    }

    private String get_cycle_type_string(int cycleType) {
        return switch (cycleType) {
            case BookInfo.BOOK_CYCLE_ONETIME -> get_string(R.string.reserve_timer_cycle_once);
            case BookInfo.BOOK_CYCLE_DAILY -> get_string(R.string.reserve_timer_cycle_everyday);
            case BookInfo.BOOK_CYCLE_WEEKLY -> get_string(R.string.reserve_timer_cycle_weekly);
            case BookInfo.BOOK_CYCLE_WEEKEND -> get_string(R.string.reserve_timer_cycle_weekend);
            case BookInfo.BOOK_CYCLE_WEEKDAYS -> get_string(R.string.reserve_timer_cycle_weekdays);
            default -> "null";
        };
    }

    private String get_cycle_day_string(int cycleDayIndex) {
        return switch (cycleDayIndex) {
            case 2 -> get_string(R.string.monday);
            case 3 -> get_string(R.string.tuesday);
            case 4 -> get_string(R.string.wednesday);
            case 5 -> get_string(R.string.thursday);
            case 6 -> get_string(R.string.friday);
            case 7 -> get_string(R.string.saturday);
            case 1 -> get_string(R.string.sunday);
            default -> get_string(R.string.monday);
        };
    }

    private int get_cycle_day_value(int cycleDayIndex) {
        return switch (cycleDayIndex) {
            case 2 -> BookInfo.BOOK_WEEK_DAY_MONDAY;
            case 3 -> BookInfo.BOOK_WEEK_DAY_TUESDAY;
            case 4 -> BookInfo.BOOK_WEEK_DAY_WEDNESDAY;
            case 5 -> BookInfo.BOOK_WEEK_DAY_THURSDAY;
            case 6 -> BookInfo.BOOK_WEEK_DAY_FRIDAY;
            case 7 -> BookInfo.BOOK_WEEK_DAY_SATURDAY;
            case 1 -> BookInfo.BOOK_WEEK_DAY_SUNDAY;
            default -> BookInfo.BOOK_WEEK_DAY_MONDAY;
        };
    }

    private void new_timer() {
        BookInfo bookInfo = get_new_book_info();
        Log.d(TAG, "new_timer: " + bookInfo.ToString());

        if (check_timer_expired(get_calendar_by_book_info(bookInfo))) {
            Log.d(TAG, "new_timer: time expired");
            if (bookInfo.getBookCycle() == BookInfo.BOOK_CYCLE_ONETIME) {
                //Log.d(TAG, "new_timer: one time, time expired");
                show_time_expired();
                return;
            }
            else {
                Log.d(TAG, "new_timer: else, time expired");
                bookInfo = book_info_get_next_date(bookInfo);
            }
        }

        if (check_conflict_book_info(bookInfo)) {
            Log.i(TAG, "new_timer: conflict");
            dismiss();
            return;
        }

        get_activity().new_timer(bookInfo);
        dismiss();
    }

    private BookInfo get_new_book_info() {
        BookInfo bookInfo = new BookInfo();
        bookInfo.setBookId(get_new_book_id());
        bookInfo.setChannelId(g_ProgramInfo.getChannelId());
        bookInfo.setGroupType(0);
        bookInfo.setEventName(g_ProgramInfo.getDisplayName());
        bookInfo.setBookType(BookInfo.BOOK_TYPE_RECORD);
        bookInfo.setBookCycle(g_cycle_type);
        bookInfo.setYear(g_year);
        bookInfo.setMonth(g_month);
        bookInfo.setDate(g_day);
        bookInfo.setWeek(get_cycle_day_value(g_cycle_day));
        bookInfo.setStartTime(g_time_hour*100+g_time_minute);
        bookInfo.setDuration(g_duration_hour*100+g_duration_minute);
        bookInfo.setEnable(0);

        return bookInfo;
    }

    private int get_new_book_id() {
        return get_activity().get_new_book_id();
    }

    private boolean check_conflict_book_info(BookInfo bookInfo) {
        List<BookInfo> conflictBookInfoList = book_info_find_conflict_books(bookInfo);
        Log.i(TAG, "check_conflict_book_info: conflictBookInfoList size = " + conflictBookInfoList.size());
        if (conflictBookInfoList.size() == 0)
            return false;
        else {
            show_conflict(conflictBookInfoList, bookInfo);
            return true;
        }
    }

    private List<BookInfo> book_info_find_conflict_books(BookInfo bookInfo) {
        return get_activity().book_info_find_conflict_books(bookInfo);
    }

    private boolean check_two_days_diff_7(Calendar currentCalendar, Calendar newCalendar) {
        LocalDate currentDate = LocalDate.of(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH)+1, currentCalendar.get(Calendar.DATE));
        LocalDate newDate = LocalDate.of(newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH)+1, newCalendar.get(Calendar.DATE));
        long diffDays = ChronoUnit.DAYS.between(currentDate, newDate);
        Log.d(TAG, "check_two_days_diff_7: diffDays = " + diffDays);

        return diffDays == 7;
    }

    private Calendar get_match_cycle_type_date() {
        Calendar calendar = Calendar.getInstance();
        if (g_cycle_type == BookInfo.BOOK_CYCLE_WEEKEND ) {
            //find next saturday or sunday
            while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
                    && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        else if (g_cycle_type == BookInfo.BOOK_CYCLE_WEEKDAYS) {
            while (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                    || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        g_cycle_day = calendar.get(Calendar.DAY_OF_WEEK);
        return calendar;
    }

    private boolean check_timer_expired(Calendar newCalendar) {
        Calendar currentCalendar = Calendar.getInstance();

        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(currentCalendar.getTime().getTime());
        String newCalFormatted = sdf.format(newCalendar.getTime().getTime());
        Log.d(TAG, "check_timer_expired: newCalendar = " + newCalFormatted);
        Log.d(TAG, "check_timer_expired: currentCalendar = " + currentDate);*/

        return currentCalendar.after(newCalendar);
    }

    private BookInfo book_info_get_next_date(BookInfo bookInfo) {
        Calendar calendar = get_calendar_by_book_info(bookInfo);

        if (bookInfo.getBookCycle() == BookInfo.BOOK_CYCLE_WEEKEND ) {
            //find next saturday or sunday
            do {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            } while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
                    && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY);
        }
        else if (bookInfo.getBookCycle() == BookInfo.BOOK_CYCLE_WEEKDAYS) {
            //find next neither saturday or sunday
            do {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            } while (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                    || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
        }
        else if (bookInfo.getBookCycle() == BookInfo.BOOK_CYCLE_WEEKLY) {
            calendar.add(Calendar.DATE, 7);
        }
        else if (bookInfo.getBookCycle() == BookInfo.BOOK_CYCLE_DAILY) {
            calendar.add(Calendar.DATE, 1);
        }

        g_year = calendar.get(Calendar.YEAR);
        g_month = calendar.get(Calendar.MONTH) +1;
        g_day = calendar.get(Calendar.DAY_OF_MONTH);
        g_time_hour = calendar.get(Calendar.HOUR_OF_DAY);
        g_time_minute = calendar.get(Calendar.MINUTE);
        g_cycle_day = calendar.get(Calendar.DAY_OF_WEEK);

        bookInfo.setYear(g_year);
        bookInfo.setMonth(g_month);
        bookInfo.setDate(g_day);
        bookInfo.setStartTime(g_time_hour*100+g_time_minute);
        bookInfo.setWeek(get_cycle_day_value(g_cycle_day));

        return bookInfo;
    }

    private Calendar get_calendar_by_book_info(BookInfo bookInfo) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(bookInfo.getYear(),
                (bookInfo.getMonth()-1),
                bookInfo.getDate(),
                (bookInfo.getStartTime()/100),
                (bookInfo.getStartTime()%100),
                0);

        return calendar;
    }

    private void show_conflict(List<BookInfo> conflictBookInfoList, BookInfo newBookInfo) {
        get_activity().show_conflict(conflictBookInfoList, newBookInfo);
    }

    void show_time_expired() {
        HotkeyRecord hotkeyRecord = new HotkeyRecord(get_activity());
        hotkeyRecord.show_panel(R.string.reserve_timer_expired);
    }
}