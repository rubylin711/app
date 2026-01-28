package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.Sysdata.EPGEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SetReserveInfoDialog extends Dialog{
    private final String TAG = getClass().getSimpleName();
    private final long ONEDAY_MILLISEC = 86400000;
    private Context mContext = null;
    private DTVActivity.BookManager mbookManager = null;
    private DTVActivity.EpgUiDisplay mepgUiDisplay = null;
    private DTVActivity mDTVActivity = null;
    private int BookID=0;
    private final int[] startTime = {0, 0}; // hour, min
    private final int[] duration = {0, 0}; // hour, min
    private EPGEvent BookEvent = null ;
    private int chindex = 0;
    private int groupType = 0 ;
    private Runnable resetCurTime;
    private TextView txvCurTime;
    private List<BookInfo> conflictBooklist = null;
    private EditText bookStartTime,bookDuration;
    private Spinner spinnerCircle;
    private Button okButton;

    public SetReserveInfoDialog(Context context
            , DTVActivity DtvActivity
            , DTVActivity.EpgUiDisplay epgUiDisplay
            , DTVActivity.BookManager bookManager
            , final int bookID
            , EPGEvent bookEvent
            , int bookCHInex
            , int type
    )
    {
        super(context);
        mContext = context;//caller activity (DTVBookingManager)
        mDTVActivity = DtvActivity ;
        mepgUiDisplay = epgUiDisplay;
        mbookManager = bookManager;
        BookID = bookID;
        BookEvent = bookEvent;
        chindex = bookCHInex;
        groupType = type ;


        Window window = getWindow();
        if(window == null){
            Log.d(TAG, "SetReserveInfoDialog: window = null");
            return;
        }
//        setCancelable(false);// disable click back button // Johnny 20181210 for keyboard control
        setCanceledOnTouchOutside(false);// disable click home button and other area

        //show(); // Edwin 20190508 fix dialog not focus
        setContentView(R.layout.event_reserve);
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.dimAmount=0.0f;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        txvCurTime = (TextView) window.findViewById(R.id.curTimeTXV);

        final int newBookID = BookID;
        final int year, month, date, week;

        final Spinner spinnerType =(Spinner)window.findViewById(R.id.typeSPINNER);
        spinnerCircle =(Spinner)window.findViewById(R.id.cycleSPINNER);

        //final Button bookStartTime = (Button)window.findViewById(R.id.startTimeBTN);
        //final Button bookDuration = (Button)window.findViewById(R.id.durationBTN);
        bookStartTime = (EditText)window.findViewById(R.id.startTimeEDV);
        bookDuration = (EditText)window.findViewById(R.id.durationEDV);

        TextView txvStartTime = (TextView) window.findViewById(R.id.startTimeTXV);
        TextView txvEventName = (TextView) window.findViewById(R.id.eventNameTXV);
        okButton = (Button) window.findViewById(R.id.okBTN);
        final Button cancelButton = (Button) window.findViewById(R.id.cancelBTN);
        final SelectBoxView selectType ;
        final SelectBoxView selectCycle;

        // ==== Event  Type  =====
        String[] ItemType = mContext.getResources().getStringArray(R.array.STR_EPG_RESERVE_TYPE);
        String[] ItemCycle = mContext.getResources().getStringArray(R.array.STR_EPG_RESERVE_CYCLE);
        selectType = new SelectBoxView(mContext, spinnerType, ItemType);
        //  ===== Cycle  =======
        selectCycle = new SelectBoxView(mContext, spinnerCircle, ItemCycle);
        //  ====   Show  Event   Name ====
        txvEventName.setText(BookEvent.getEventName());

        Date Eventdate = new Date(BookEvent.getStartTime());
        Date Eventduration = new Date(BookEvent.getDuration());
        String strTime, strDur;

        Calendar calendarTime = Calendar.getInstance();
        calendarTime.setTime(Eventdate);

        year = calendarTime.get(Calendar.YEAR);
        month = calendarTime.get(Calendar.MONTH);
        date = calendarTime.get(Calendar.DATE);
        int tmpWeek = calendarTime.get(Calendar.DAY_OF_WEEK);
        if(calendarTime.getFirstDayOfWeek() == Calendar.SUNDAY){
            tmpWeek = tmpWeek - 1;
            if(tmpWeek == 0){
                tmpWeek = 7;
            }
        }

        week = tmpWeek - 1;   // Booking Timer week, tran 1~7 to 0~6

        Calendar TimeCal = Calendar.getInstance();
        TimeCal.setTime(new Date(BookEvent.getStartTime()));
        startTime[0] = TimeCal.get(Calendar.HOUR_OF_DAY);
        startTime[1] = TimeCal.get(Calendar.MINUTE);

        if(BookEvent.getDuration() < ONEDAY_MILLISEC)
        {
            TimeCal.setTime(new Date(BookEvent.getDuration()));
            duration[0] = TimeCal.get(Calendar.HOUR_OF_DAY);
            duration[1] = TimeCal.get(Calendar.MINUTE);
        }
        else    // duration limit = 2359
        {
            duration[0] = 23;
            duration[1] = 59;
        }

        // Show  Local  Time
        resetCurTime = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat formatter= new SimpleDateFormat("dd  MMM  HH : mm", Locale.ENGLISH);
                boolean notSameTime = !txvCurTime.getText().equals(formatter.format(mDTVActivity.GetLocalTime()));
                if (notSameTime) {
                    txvCurTime.setText(formatter.format(mDTVActivity.GetLocalTime()));
                }
                txvCurTime.postDelayed(resetCurTime, 60000);
            }
        };
        txvCurTime.post(resetCurTime);

        //  Show  Event  Start  Time : Month  /  Date
        SimpleDateFormat formatter= new SimpleDateFormat("MMM dd", Locale.ENGLISH);
        strTime = formatter.format(Eventdate);
        txvStartTime.setText(strTime);

        // Show  Event  Start  Time : Hour : Minute
        strTime = String.format(Locale.ENGLISH, "%02d:%02d",startTime[0],startTime[1]);
        bookStartTime.setText(strTime);
        // Show  Event  Duration  : Hour : Minute
        strDur = String.format(Locale.ENGLISH, "%02d:%02d",duration[0],duration[1]);
        bookDuration.setText(strDur);

        bookStartTime.setOnFocusChangeListener(startTime_change_focus);// connie 20180731 for use EditText to modify start time & duration-s
        bookDuration.setOnFocusChangeListener(duration_change_focus);
        bookStartTime.setOnKeyListener(startTimeKeyListener);
        bookDuration.setOnKeyListener(DurationKeyListener);
        bookStartTime.setShowSoftInputOnFocus(false);
        bookDuration.setShowSoftInputOnFocus(false);// connie 20180731 for use EditText to modify start time & duration-e

        okButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if(isShowing()){
                    final BookInfo bookInfo = new BookInfo();
                    if (selectCycle.GetSelectedItemIndex() == BookInfo.BOOK_CYCLE_ONETIME)
                    {
//                        bookInfo.setBookId(newBookID);
                        bookInfo.setBookId(mbookManager.BookList.size()/*BookInfo.MAX_NUM_OF_BOOKINFO*/);   // use MAX_NUM_OF_BOOKINFO, do not change
                        bookInfo.setYear(year);
                        bookInfo.setMonth(month+1);
                        bookInfo.setDate(date);
                        bookInfo.setWeek(week);
                        bookInfo.setStartTime((startTime[0] * 100) + startTime[1]);
                        bookInfo.setDuration((duration[0] * 100) + duration[1]);
                        bookInfo.setBookType(selectType.GetSelectedItemIndex());
                        bookInfo.setBookCycle(selectCycle.GetSelectedItemIndex());
                        bookInfo.setEventName(BookEvent.getEventName());
                        bookInfo.setChannelId(mepgUiDisplay.programInfoList.get(chindex).getChannelId());
                        bookInfo.setGroupType(groupType);
                        bookInfo.setEnable(1);
                    }
                    else
                    {
                        Date eventDate = new Date(BookEvent.getStartTime());

                        // adjust date by bookcycle
                        switch (selectCycle.GetSelectedItemIndex())
                        {
                            case BookInfo.BOOK_CYCLE_DAILY:
                            {
                                eventDate = mDTVActivity.GetDateOfNextDaily(eventDate);
                            } break;
                            case BookInfo.BOOK_CYCLE_WEEKLY:
                            {
                                eventDate = mDTVActivity.GetDateOfNextWeekly(eventDate, week);
                            } break;
                            case BookInfo.BOOK_CYCLE_WEEKEND:
                            {
                                eventDate = mDTVActivity.GetDateOfNextWeekEnd(eventDate);
                            } break;
                            case BookInfo.BOOK_CYCLE_WEEKDAYS:
                            {
                                eventDate = mDTVActivity.GetDateOfNextWeekDays(eventDate);
                            } break;
                        }

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(eventDate);

                        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
                        if(calendar.getFirstDayOfWeek() == Calendar.SUNDAY){
                            weekDay = weekDay - 1;
                            if(weekDay == 0){
                                weekDay = 7;
                            }
                        }

//                        bookInfo.setBookId(newBookID);
                        bookInfo.setBookId(mbookManager.BookList.size()/*BookInfo.MAX_NUM_OF_BOOKINFO*/); // use MAX_NUM_OF_BOOKINFO, do not change
                        bookInfo.setYear(calendar.get(Calendar.YEAR));
                        bookInfo.setMonth(calendar.get(Calendar.MONTH)+1);
                        bookInfo.setDate(calendar.get(Calendar.DATE));
                        bookInfo.setWeek(weekDay-1);    // Booking Timer week, tran 1~7 to 0~6
                        bookInfo.setStartTime((startTime[0] * 100) + startTime[1]);
                        bookInfo.setDuration((duration[0] * 100) + duration[1]);
                        bookInfo.setBookType(selectType.GetSelectedItemIndex());
                        bookInfo.setBookCycle(selectCycle.GetSelectedItemIndex());
                        bookInfo.setEventName(BookEvent.getEventName());
                        bookInfo.setChannelId(mepgUiDisplay.programInfoList.get(chindex).getChannelId());
                        bookInfo.setGroupType(groupType);
                        bookInfo.setEnable(1);
                    }

                    boolean hasConflict = (conflictBooklist = mDTVActivity.BookInfoFindConflictBooks(bookInfo)) != null;

                    if (!mbookManager.CheckBookAfterNow(bookInfo) && bookInfo.getBookCycle() == BookInfo.BOOK_CYCLE_ONETIME)
                    {
                        new MessageDialogView(mContext,mContext.getResources().getString(R.string.STR_TIMER_ENTER_TIME_AFTER_NOW),3000)
                        {
                            public void dialogEnd() {
                            }
                        }.show();
                    }
                    else if (hasConflict)
                    {
                        dismiss();
                        new BookConflictDialogView(mContext, conflictBooklist)
                        {
                            @Override
                            public void onDialogPositiveClick()
                            {
                                // press OK button to delete conflict books
                                mbookManager.DelBookInfoList(conflictBooklist);

                                // remove when use pesi service
                                /*for (int i = 0 ; i < conflictBooklist.size() ; i++) {
                                    mDTVActivity.BookInfoDelete(conflictBooklist.get(i).getBookId());
                                }*/

//                                mDTVActivity.BookInfoAdd(bookInfo); // remove when use pesi service
                                mbookManager.AddBookInfo(bookInfo);
                                mbookManager.Save();
                            }

                            @Override
                            public void onDialogNegativeClick()
                            {
                            }
                        }.show();
                    }
                    else {
//                        mDTVActivity.BookInfoAdd(bookInfo); // remove when use pesi service
                        mbookManager.AddBookInfo(bookInfo);
                        mbookManager.Save();
                        dismiss();
                        //Log.d(TAG, "onClick:   ==========================");
                        //Log.d(TAG, "onClick:  Year = " + bookInfo.getYear());
                        //Log.d(TAG, "onClick:  Month = " + bookInfo.getMonth());
                        //Log.d(TAG, "onClick:  Date = " + bookInfo.getDate());
                        //Log.d(TAG, "onClick:  Week = " + bookInfo.getWeek());
                        //Log.d(TAG, "onClick:   Start Time = " + bookInfo.getStartTime());
                        //Log.d(TAG, "onClick:   Duration = " + bookInfo.getDuration());

                    }
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if(isShowing()){
                    dismiss();
                }
            }
        });

        /*
        bookStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(mContext, R.style.TimePickerDialogStyle, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //Log.d(TAG,"new TimePickerDialog: selectedHour="+selectedHour+", selectedMinute="+selectedMinute );
                        startTime[0] = selectedHour;
                        startTime[1] = selectedMinute;
                        String strTime = String.format(Locale.ENGLISH, "%02d:%02d",startTime[0],startTime[1]);
                        bookStartTime.setText(strTime);
                    }
                }, startTime[0], startTime[1], true);
                mTimePicker.show();

            }
        });
        */

        /*
        bookDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(mContext, R.style.TimePickerDialogStyle, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //Log.d(TAG,"new TimePickerDialog: selectedHour="+selectedHour+", selectedMinute="+selectedMinute );
                        duration[0] = selectedHour;
                        duration[1] = selectedMinute;
                        String durTime = String.format(Locale.ENGLISH, "%02d:%02d",duration[0],duration[1]);
                        bookDuration.setText(durTime);
                    }
                }, duration[0], duration[1], true);
                mTimePicker.show();
            }
        });
        */
    }

    /*private boolean Conflict(BookInfo bookSetting, List<BookInfo> allBook) {
        int startTime = bookSetting.getStartTime();
        int endTime = startTime + bookSetting.getDuration();

        for (BookInfo curBook : allBook) {
            if (startTime <= curBook.getStartTime()) {
                show keep dialog;
                startTime = curBook.getStartTime();
            }
            if (endTime > curBook.getStartTime()+curBook.getDuration()) {
                show keep dialog;
                endTime = curBook.getStartTime()+curBook.getDuration();
            }
        }

        return false;
    }*/

    // connie 20180731 for use EditText to modify start time & duration-s
    private boolean deleteEditText(EditText et) {
        int position = et.getSelectionStart();
        String curStr = String.valueOf(et.getText());
        Log.d(TAG, "deleteEditText: curStr =" + curStr + "    position="+position);
        Editable editable = et.getText();

        if (position > 0 && curStr.length() > 1 ) {
            Log.d(TAG, "deleteEditText:  curStr.charAt( " + (position-1)+") =" + curStr.charAt(position-1));
            if( curStr.charAt(position-1)== ':') {
                et.setSelection(position - 1);
                position = et.getSelectionStart();
            }
            editable.delete(position - 1, position);
            return true;
        }
        else {
            return false; //delete fail
        }
    }
    private boolean insertEditText(EditText et, String input) {
        int position = et.getSelectionStart();
        String curStr = String.valueOf(et.getText());
        Log.d(TAG, "insertEditText: curStr =" + curStr + "    position="+position);
        Editable editable = et.getText();

        if (position >= 0  && curStr.length() < 6 ) {
            if( curStr.length() == 2 ) {
                editable.insert(position, input);
                et.setSelection(3);
                Log.d(TAG, "insertEditText:  getSelection = " + et.getSelectionStart());
            }
            else
                editable.insert(position, input);

            return true;
        }
        else {
            return false; //delete fail
        }
    }

    private View.OnFocusChangeListener duration_change_focus = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                bookDuration.selectAll();
            }
            else {
                String NewStr = bookDuration.getText().toString();
                Log.d(TAG, "onFocusChange:  NewStr = " + NewStr);
                int hour, min;
                if(NewStr.length() == 5) {
                    hour = Integer.valueOf(NewStr.substring(0,2));
                    min = Integer.valueOf(NewStr.substring(3,5));
                    if(hour <= 23 && min <= 59) {
                        duration[0] = hour;//Integer.valueOf(hour);
                        duration[1] = min;//Integer.valueOf(min);
                        Log.d(TAG, "onFocusChange: duration[0]"+duration[0] + "    duration[1]"+duration[1]);
                        return;
                    }
                }

                NewStr = String.format(Locale.getDefault(), "%02d:%02d",duration[0],duration[1]);
                Log.d(TAG, "onFocusChange: duration[0]"+duration[0] + "    duration[1]"+duration[1]);
                bookDuration.setText(NewStr);
            }
        }
    };

    private View.OnFocusChangeListener startTime_change_focus = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                bookStartTime.selectAll();
            }
            else {
                String NewStr = bookStartTime.getText().toString();
                Log.d(TAG, "startTime_change_focus:  NewStr = " + NewStr);
                int hour, min;
                if(NewStr.length() == 5) {
                    hour = Integer.valueOf(NewStr.substring(0,2));
                    min = Integer.valueOf(NewStr.substring(3,5));
                    if(hour <= 23 && min <= 59) {
                        startTime[0] = hour;//Integer.valueOf(hour);
                        startTime[1] = min;//Integer.valueOf(min);
                        Log.d(TAG, "startTime_change_focus: startTime[0]"+startTime[0] + "    startTime[1]"+startTime[1]);
                        return;
                    }
                }

                NewStr = String.format(Locale.getDefault(), "%02d:%02d",startTime[0],startTime[1]);
                Log.d(TAG, "startTime_change_focus: startTime[0]"+startTime[0] + "    startTime[1]"+startTime[1]);
                bookStartTime.setText(NewStr);
            }
        }
    };

    private View.OnKeyListener startTimeKeyListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            boolean isActionDown = (event.getAction() == KeyEvent.ACTION_DOWN);
            if (isActionDown)
            {
                Log.d(TAG, "startTimeKeyListener:  keyCode = " + keyCode);
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if(bookStartTime.getText().length()==5)
                            bookDuration.requestFocus();
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        bookDuration.requestFocus();
                        return true;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        spinnerCircle.requestFocus();
                        return true;
                    case KeyEvent.KEYCODE_DEL:
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        deleteEditText(bookStartTime);
                        return true;

                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if(bookStartTime.getText().length() < 3)
                            return true;
                        break;
                    case KeyEvent.KEYCODE_0:
                    case KeyEvent.KEYCODE_1:
                    case KeyEvent.KEYCODE_2:
                    case KeyEvent.KEYCODE_3:
                    case KeyEvent.KEYCODE_4:
                    case KeyEvent.KEYCODE_5:
                    case KeyEvent.KEYCODE_6:
                    case KeyEvent.KEYCODE_7:
                    case KeyEvent.KEYCODE_8:
                    case KeyEvent.KEYCODE_9:
                        Log.d(TAG, "startTimeKeyListener:  " + bookStartTime.getSelectionStart() + "     " + bookStartTime.getSelectionEnd());
                        if(bookStartTime.getSelectionStart() == 0 && bookStartTime.getSelectionEnd() == 5) // select all
                            bookStartTime.setText(":");
                        String input = Integer.toString(keyCode-7);
                        insertEditText(bookStartTime, input);

                        return true;
                    default:
                        break;
                }
            }
            return false;
        }
    };

    private View.OnKeyListener DurationKeyListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            boolean isActionDown = (event.getAction() == KeyEvent.ACTION_DOWN);
            if (isActionDown)
            {
                Log.d(TAG, "DurationKeyListener:  keyCode = " + keyCode);
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if(bookDuration.getText().length()==5)
                            okButton.requestFocus();
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        okButton.requestFocus();
                        return true;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        bookStartTime.requestFocus();
                        return true;
                    case KeyEvent.KEYCODE_DEL:
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        deleteEditText(bookDuration);
                        return true;

                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if(bookDuration.getText().length() < 3)
                            return true;
                        break;

                    case KeyEvent.KEYCODE_0:
                    case KeyEvent.KEYCODE_1:
                    case KeyEvent.KEYCODE_2:
                    case KeyEvent.KEYCODE_3:
                    case KeyEvent.KEYCODE_4:
                    case KeyEvent.KEYCODE_5:
                    case KeyEvent.KEYCODE_6:
                    case KeyEvent.KEYCODE_7:
                    case KeyEvent.KEYCODE_8:
                    case KeyEvent.KEYCODE_9:
                        Log.d(TAG, "DurationKeyListener:  " + bookDuration.getSelectionStart() + "     " + bookDuration.getSelectionEnd());
                        if(bookDuration.getSelectionStart() == 0 && bookDuration.getSelectionEnd() == 5) // select all
                            bookDuration.setText(":");
                        String input = Integer.toString(keyCode-7);
                        insertEditText(bookDuration, input);

                        return true;
                    default:
                        break;
                }
            }
            return false;
        }
    };// connie 20180731 for use EditText to modify start time & duration-e
}
