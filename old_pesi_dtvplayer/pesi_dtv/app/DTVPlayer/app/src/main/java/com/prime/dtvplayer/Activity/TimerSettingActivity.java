package com.prime.dtvplayer.Activity;
/*
  Created by scoty_kuo on 2017/11/23.
 */

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.View.ActivityHelpViewWoColorIcon;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.BookConflictDialogView;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.SelectBoxView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;

public class TimerSettingActivity extends DTVActivity {
    private static final String TAG = "TimerSettingActivity";
    private TextView TempTexView
            , timernum_text
            , date_text
            , duration_text
            , starttime_text
            , enable_text;
    private Spinner Spinnertimercycle,Spinnertimertype,Spinnertimerenable,Spinnertimerweekly;
    private Button selectChannelbtn,datebtn;
    //private Button startTimeEdit,durationEdit;
    private EditText startTimeEdit, durationEdit;
    private SelectBoxView timercycle,timertype,timerenable,timerweekly;
    private RecyclerAdapter chAdapter=null;
    private RecyclerView chlistview;
    private Dialog mDialog;
    private Handler  currenttimer_handler;
    private Runnable currenttimer_runnable;
    private List<BookInfo> conflictBooklist = null;
    private int book_chnum, book_type;
    private long book_channelId;
    private String book_chname;
    private int timernum = 0;
    private final int[] BookStartTime = {02, 00}; // hour, min
    private final int[] BookDuration = {02, 00}; // hour, min
    private int CurType = 0;
    private List<SimpleChannel> programList = new ArrayList<>();//Scoty 20181109 modify for skip channel
    BookConflictDialogView mConflictDialog = null;
    private BookManager bookManager = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_setting);

        TitleHelp_Init();

        if(ViewHistory.getCurChannel() == null) {
            String str = getString(R.string.STR_NO_TV_CHANNEL);
            new MessageDialogView(this, str, 3000) {
                public void dialogEnd() {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }.show();
        }
        else {
            //Scoty 20181109 modify for skip channel -s
            if (ViewHistory.getCurGroupType() >= ProgramInfo.ALL_TV_TYPE &&
                    ViewHistory.getCurGroupType() < ProgramInfo.ALL_RADIO_TYPE) {
                CurType = ProgramInfo.ALL_TV_TYPE;
            } else{
                CurType = ProgramInfo.ALL_RADIO_TYPE;
            }
            programList = ProgramInfoGetPlaySimpleChannelList(CurType,1);
            //Scoty 20181109 modify for skip channel -e
            TimerSetting_Init();
        }

    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        setup_timeupdatethread();
    }

    @Override
    protected void onStop(){
        Log.d(TAG, "onStop");
        super.onStop();
        currenttimer_handler.removeCallbacks(currenttimer_runnable);
    }

    private void TitleHelp_Init()
    {
        ActivityTitleView setActivityTitle;
        setActivityTitle = (ActivityTitleView) findViewById(R.id.activityTitleViewLayoutTimerSetting);
        setActivityTitle.setTitleView(getString(R.string.STR_TIMER_SETTING_TITLE));
       
        ActivityHelpViewWoColorIcon helpViewWoColorIcon;
        helpViewWoColorIcon = (ActivityHelpViewWoColorIcon) findViewById(R.id.HelpViewWoColorIconLayout);
        helpViewWoColorIcon.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));
    }

    private void TimerSetting_Init()
    {
        Log.d(TAG,"TimerSetting_Init");
        //BookManagerInit();
        bookManager = GetBookManager();
        timernum = getIntent().getIntExtra(TimerListActivity.TIMER_KEY_POS,0);

        timernum_text = (TextView) findViewById(R.id.timernumTXV);
        timernum_text.setText(String.valueOf(timernum+1));//current timer number

        date_text = (TextView) findViewById(R.id.timerdateTXV);
        starttime_text = (TextView) findViewById(R.id.timertimeTXV);
        duration_text = (TextView) findViewById(R.id.timerdurationTXV);
        enable_text = (TextView)findViewById(R.id.timerenableTXV);
        //durationEdit = (Button) findViewById(R.id.timerdurationBTN);
        durationEdit = (EditText)findViewById(R.id.timerdurationEDV);// connie 20180731 for use EditText to modify start time & duration
        //startTimeEdit = (Button) findViewById(R.id.timertimeBTN);
        startTimeEdit =  (EditText)findViewById(R.id.timerstartEDV);// connie 20180731 for use EditText to modify start time & duration
        datebtn = (Button) findViewById(R.id.timerdateBTN);
        Spinnertimertype= (Spinner) findViewById(R.id.eventtypeSPINNER);
        Spinnertimercycle= (Spinner) findViewById(R.id.timercycleSPINNER);
        Spinnertimerenable= (Spinner) findViewById(R.id.timerenableSPINNER);
        Spinnertimerweekly = (Spinner) findViewById(R.id.timerweeklySPINNER);

        startTimeEdit.setOnFocusChangeListener(startTime_change_focus);// connie 20180731 for use EditText to modify start time & duration-s
        durationEdit.setOnFocusChangeListener(duration_change_focus);
        startTimeEdit.setOnKeyListener(startTimeKeyListener);
        durationEdit.setOnKeyListener(DurationKeyListener);
        startTimeEdit.setShowSoftInputOnFocus(false); // disable keyboard
        durationEdit.setShowSoftInputOnFocus(false);// connie 20180731 for use EditText to modify start time & duration-e

        //timer cycle : once,daily,weekly,weekend,weekdays
        String[] str_timer_cycle_list = getResources().getStringArray(R.array.STR_ARRAY_TIMER_CYCLE);
        timercycle = new SelectBoxView(this, Spinnertimercycle, str_timer_cycle_list);
        Spinnertimercycle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    resetItemPosition(position);//reset Item postiion
                }

                public void onNothingSelected(AdapterView<?> parent) {
                    Log.d(TAG,"<<onNothingSelected>> ");
                }
            }
        );

        //timer type
        String[] str_timer_type_list = getResources().getStringArray(R.array.STR_ARRAY_TIMER_TYPE);
        timertype = new SelectBoxView(this, Spinnertimertype, str_timer_type_list);

        //timer weekly
        String[] str_timer_weekly_list = getResources().getStringArray(R.array.STR_ARRAY_TIMER_WEEKLY);
        timerweekly = new SelectBoxView(this, Spinnertimerweekly, str_timer_weekly_list);

        //timer enable
        String[] str_timer_enable_list = getResources().getStringArray(R.array.STR_ARRAY_TIMER_ENABLE);
        timerenable = new SelectBoxView(this, Spinnertimerenable, str_timer_enable_list);

        //channel select
        selectChannelbtn = (Button) findViewById(R.id.channelselectBTN);
        selectChannelbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDialog = new Dialog(TimerSettingActivity.this,R.style.MyDialog);
                mDialog.setOnKeyListener(new Dialog.OnKeyListener(){
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if(event.getAction() == KeyEvent.ACTION_DOWN)
                        {
                            View curFocusChild = chlistview.getFocusedChild();
                            View firstChild = chlistview.getChildAt(0);
                            View lastChild = chlistview.getChildAt(chlistview.getChildCount() - 1);
                            int curFocusPos = chlistview.getChildAdapterPosition(curFocusChild);
                            int firstChildPos = chlistview.getChildAdapterPosition(firstChild);
                            int lastChildPos = chlistview.getChildAdapterPosition(lastChild);
                            int lastAdapterItemPos = chlistview.getAdapter().getItemCount() - 1;
                            int itemHeight = (int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT);

                            switch (keyCode) {
                                case KeyEvent.KEYCODE_DPAD_DOWN:
                                    if (chlistview.getChildCount() <= 0)
                                    {
                                        return false;
                                    }

                                    if (curFocusPos >= lastAdapterItemPos) {
                                        // scroll to top
                                        chlistview.scrollToPosition(0);
                                        chlistview.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                chlistview.getChildAt(0).requestFocus();
                                            }
                                        });

                                        return true;
                                    } else if (curFocusPos >= lastChildPos) {
                                        // scroll down one item
                                        chlistview.scrollBy(0, itemHeight);
                                        chlistview.getChildAt(chlistview.getChildCount() - 1).requestFocus();

                                        return true;
                                    }
                                    break;
                                case KeyEvent.KEYCODE_DPAD_UP:
                                    if (chlistview.getChildCount() <= 0)
                                    {
                                        return false;
                                    }

                                    if (curFocusPos <= 0) {
                                        // scroll to bottom
                                        chlistview.scrollToPosition(lastAdapterItemPos);
                                        chlistview.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                chlistview.getChildAt(chlistview.getChildCount() - 1).requestFocus();
                                            }
                                        });

                                        return true;
                                    } else if (curFocusPos <= firstChildPos) {
                                        // scroll up one item
                                        chlistview.scrollBy(0, -itemHeight);
                                        chlistview.getChildAt(0).requestFocus();

                                        return true;
                                    }
                                    break;

                                // edwin 20180709 add page up & page down -s

                                case KeyEvent.KEYCODE_BACK:
                                case KeyEvent.KEYCODE_DPAD_CENTER:
                                    TitleHelp_Init();
                                    break;

                                case KeyEvent.KEYCODE_PAGE_DOWN:
                                    PagePrev(chlistview);
                                    break;

                                case KeyEvent.KEYCODE_PAGE_UP:
                                    PageNext(chlistview);
                                    break;

                                // edwin 20180709 add page up & page down -e
                            }
                        }
                        return false;
                    }

                });

                mDialog.show();
                mDialog.setContentView(R.layout.timer_channel_select_dialog);

                Window window = mDialog.getWindow();
                WindowManager.LayoutParams lp;
                if (window == null) {
                    Log.d(TAG, "onClick: window = null");
                    return;
                }

                lp = window.getAttributes();
                if (lp == null) {
                    Log.d(TAG, "onClick: lp = null");
                    return;
                }

                lp.dimAmount=0.0f;
                mDialog.getWindow().setAttributes(lp);
                mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                channel_select_dialog_init(lp.width);//init dialog

                ActivityHelpViewWoColorIcon helpViewWoColorIcon; // edwin 20180709 add page up & page down
                helpViewWoColorIcon = (ActivityHelpViewWoColorIcon) findViewById(R.id.HelpViewWoColorIconLayout);
                helpViewWoColorIcon.setHelpInfoTextBySplit(getString(R.string.STR_TIMER_SELECT_CH_HELP_INFO));
            }
        });

        //date calendar
        datebtn.setOnClickListener(
            new View.OnClickListener(){
                public void onClick(View v) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd",Locale.getDefault());
                    String dateString = datebtn.getText().toString();
                    Date select_date = GetLocalTime();
                    try {
                        select_date = sdf.parse(dateString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    final Calendar calendar = Calendar.getInstance();
                    calendar.setTime(select_date);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);


                    DatePickerDialog datePickerDialog = new DatePickerDialog(TimerSettingActivity.this, R.style.DatePickerDialogStyle, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            //Log.d(TAG," year " + year + " month " + month + " dayOfMonth " + dayOfMonth);
                            datebtn.setText(String
                                    .valueOf(year)
                                    .concat("/")
                                    .concat(String.format(Locale.getDefault(), "%02d", month+1))
                                    .concat("/")
                                    .concat(String.format(Locale.getDefault(), "%02d", dayOfMonth))
                            );
                        }

                    }, year, month, day);

                    datePickerDialog.show();

                    /*new CalendarDialogView(TimerSettingActivity.this,select_date){
                        public void onSetMessage(View v){
                        }
                        public void onSetNegativeButton(){
                        }
                        public void onSetPositiveButton(int year, int month, int dayOfMonth){
                            Log.d(TAG," year " + year + " month " + month + " dayOfMonth " + dayOfMonth);
                            datebtn.setText(String
                                    .valueOf(year)
                                    .concat("/")
                                    .concat(String.valueOf(month+1))
                                    .concat("/")
                                    .concat(String.valueOf(dayOfMonth))
                            );
                        }
                    };*/
                }
            }
        );

        /*
        // startTime timePicker
        startTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Log.d(TAG,"TxvCurrentTime.setOnClickListener: hour="+hour+", minute="+minute );

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(TimerSettingActivity.this, R.style.TimePickerDialogStyle, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //Log.d(TAG,"new TimePickerDialog: selectedHour="+selectedHour+", selectedMinute="+selectedMinute );
                        BookStartTime[0] = selectedHour;
                        BookStartTime[1] = selectedMinute;
                        String startStr = String.format(Locale.getDefault(), "%02d:%02d",BookStartTime[0],BookStartTime[1]);
                        startTimeEdit.setText( startStr);
                    }
                }, BookStartTime[0], BookStartTime[1], true);
                mTimePicker.show();
            }
        });
*/
        // duration timePicker
        /*
        durationEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(TimerSettingActivity.this, R.style.TimePickerDialogStyle, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        BookDuration[0] = selectedHour;
                        BookDuration[1] = selectedMinute;
                        String durStr = String.format(Locale.getDefault(), "%02d:%02d",BookDuration[0],BookDuration[1]);
                        durationEdit.setText( durStr);
                    }
                }, BookDuration[0], BookDuration[1], true);
                mTimePicker.show();
            }
        });
*/
        // set display value
        if(timernum < bookManager.BookList.size())//Edit timer
            setEditTimerParam();
        else {
            //Add new timer
            Date date = GetLocalTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd",Locale.getDefault());
            String today = sdf.format(date);
            datebtn.setText(today);
            setSelectChannel(getCurChPos());    //set default channel to cur channel
        }
    }

    public void setEditTimerParam()
    {
        String timer_date;
        String startTime, duration, curBookName;
        ProgramInfo bookProgram;

        book_chname = bookManager.BookList.get(timernum).getEventName();
        book_channelId = bookManager.BookList.get(timernum).getChannelId();
        book_type = bookManager.BookList.get(timernum).getGroupType();

        BookStartTime[0] = bookManager.BookList.get(timernum).getStartTime()/100; // Start Hour
        BookStartTime[1] = bookManager.BookList.get(timernum).getStartTime()%100; // Start Min
        BookDuration[0] = bookManager.BookList.get(timernum).getDuration()/100; // Duration Hour
        BookDuration[1] = bookManager.BookList.get(timernum).getDuration()%100; // Duration Min

        startTime = String.format(Locale.ENGLISH, "%02d:%02d",BookStartTime[0],BookStartTime[1]);
        duration = String.format(Locale.ENGLISH, "%02d:%02d",BookDuration[0],BookDuration[1]);

        // selectChannelbtn
        bookProgram = ProgramInfoGetByChannelId(book_channelId);
        if(book_chname.equals(""))
        {
            curBookName = Integer.toString(bookProgram.getDisplayNum()) + " " + bookProgram.getDisplayName();
        }
        else {
            curBookName = Integer.toString(bookProgram.getDisplayNum()) + " " + book_chname;
        }
        selectChannelbtn.setText(curBookName);//set edit first channel

        //timer cycle
        timercycle.SetSelectedItemIndex(bookManager.BookList.get(timernum).getBookCycle());

        if(bookManager.BookList.get(timernum).getBookCycle() == BookInfo.BOOK_CYCLE_ONETIME)//date calendar
        {
            timer_date = String.format(Locale.ENGLISH
                    , "%4d/%02d/%02d"
                    ,bookManager.BookList.get(timernum)
                            .getYear(),bookManager.BookList.get(timernum)
                            .getMonth()
                    ,bookManager.BookList.get(timernum)
                            .getDate()
            );
            datebtn.setText(timer_date);
        }
        else if(bookManager.BookList.get(timernum).getBookCycle() == BookInfo.BOOK_CYCLE_WEEKLY)//week
            timerweekly.SetSelectedItemIndex(bookManager.BookList.get(timernum).getWeek());


        //start time hour
        startTimeEdit.setText(startTime);
        //duration hour
        durationEdit.setText(duration);

        //enable
        timerenable.SetSelectedItemIndex(bookManager.BookList.get(timernum).getEnable());
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        private List<SimpleChannel> listItems;//Scoty 20181109 modify for skip channel

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView chnum;
            TextView chname;
            ImageView scramble;
            ImageView fav;
            ImageView chlock;

            ViewHolder(View itemView) {
                super(itemView);
                chnum = (TextView) itemView.findViewById(R.id.chnumTXV);//LCN
                chname = (TextView) itemView.findViewById(R.id.chnameTXV);//Channel name
                scramble = (ImageView) itemView.findViewById(R.id.scrambleIGV);//Scramble icon
                fav = (ImageView) itemView.findViewById(R.id.favIGV);//group icon
                chlock = (ImageView) itemView.findViewById(R.id.lockIGV);//Lock icon

            }
        }

        RecyclerAdapter(List<SimpleChannel> list) {//Scoty 20181109 modify for skip channel
            listItems = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater
                    .from( parent.getContext() )
                    .inflate(R.layout.tvmanager_list_item, parent, false);

            convertView.setOnClickListener(new chlistOnClick());
            convertView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
//                            Log.d(TAG, "onFocusChange: channel = "+ ((TextView) v.findViewById(R.id.chnameTXV)).getText());
                        v.setBackgroundResource(R.drawable.focus_list);
                    }
                }
            });

            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            String string = Integer.toString(listItems.get(position).getChannelNum());
            holder.chnum.setText(string);
            holder.chname.setText(listItems.get(position).getChannelName());

            if(listItems.get(position).getCA() == 1)
                holder.scramble.setBackgroundResource(R.drawable.scramble);
            else
                holder.scramble.setBackgroundResource(android.R.color.transparent);

            if(listItems.get(position).getUserLock() == 1)
                holder.chlock.setBackgroundResource(R.drawable.lock);
            else
                holder.chlock.setBackgroundResource(android.R.color.transparent);


            holder.itemView.getLayoutParams().height = ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));

            //for Marquee,--start
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        holder.chname.setSelected(true);
                        holder.chnum.setTextColor(BLACK);
                    }
                    else {
                        holder.chname.setSelected(false);
                        holder.chnum.setTextColor(WHITE);
                    }
                }
            });
            //for Marquee,--end
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            if (listItems == null) {
                return 0;
            }
            else
            {
                return listItems.size();//list size
            }
        }
    }

    class chlistOnClick implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            final int position = chlistview.getChildLayoutPosition(v);
            Log.d(TAG,"chlistOnClick curChannelIndex" + position);
            setSelectChannel(position);
            dismissDialog();
        }
    }

    public void setSelectChannel(int position)
    {
        //Scoty 20181109 modify for skip channel -s
        String string = Integer.toString(programList.get(position).getChannelNum())
                + " " + programList.get(position).getChannelName();
        selectChannelbtn.setText(string);
        selectChannelbtn.requestFocus();
        book_chnum = programList.get(position).getChannelNum();
        book_chname = programList.get(position).getChannelName();
        book_channelId = programList.get(position).getChannelId();
        book_type = CurType;
        //Scoty 20181109 modify for skip channel -e
    }

    public void channel_select_dialog_init(int width)
    {
        Log.d(TAG,"channel_select_dialog_init ");
        int listCount;
        int itemHeight = ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));

        Window window = mDialog.getWindow();
        if (window == null) {
            Log.d(TAG, "channel_select_dialog_init: window = null");
            return;
        }
        chlistview = (RecyclerView) window.findViewById(R.id.chlisLIV);
        chAdapter = new RecyclerAdapter(programList);//Scoty 20181109 modify for skip channel
        //for largest display size,--start
        int mheight = getResources().getDisplayMetrics().heightPixels;
        Guideline top = (Guideline) mDialog.findViewById(R.id.title_guideline2);
        Guideline bottom = (Guideline) mDialog.findViewById(R.id.bottom_guideline);
        float topPercent = ((ConstraintLayout.LayoutParams)top.getLayoutParams()).guidePercent;
        float bottomPercent = ((ConstraintLayout.LayoutParams)bottom.getLayoutParams()).guidePercent;
        float guideLineRange = bottomPercent - topPercent;
        listCount = (int)(mheight*guideLineRange)/itemHeight; //0.77-0.22
        //for largest display size,--end

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                width, itemHeight * listCount);
        chlistview.setLayoutParams(layoutParams);

        chlistview.setAdapter(chAdapter);
        chAdapter.notifyDataSetChanged();
    }

    public void resetItemPosition(int position)
    {
        Log.d(TAG, "resetItemPosition ==>> " + position);
        if(position == BookInfo.BOOK_CYCLE_DAILY
                || position == BookInfo.BOOK_CYCLE_WEEKEND
                || position == BookInfo.BOOK_CYCLE_WEEKDAYS) { //daily,weekend,weekdays

            if(datebtn.getVisibility() == View.VISIBLE || Spinnertimerweekly.getVisibility() == View.VISIBLE) {
                datebtn.setVisibility(GONE);
                date_text.setVisibility(GONE);
                datebtn.setFocusable(false);
                Spinnertimerweekly.setVisibility(GONE);
                date_text.setVisibility(GONE);
                Spinnertimerweekly.setFocusable(false);
            }
        }
        else if (position == BookInfo.BOOK_CYCLE_ONETIME) { //once
            String timer_date;

            Spinnertimerweekly.setVisibility(INVISIBLE);
            Spinnertimerweekly.setFocusable(false);
            datebtn.setVisibility(View.VISIBLE);
            date_text.setVisibility(View.VISIBLE);
            date_text.setText(R.string.STR_TIMER_DATE);
            datebtn.setFocusable(true);
            if(timernum < bookManager.BookList.size()) { //edit timer
                timer_date = String.format(Locale.ENGLISH
                                ,"%4d/%02d/%02d"
                                , bookManager.BookList.get(timernum).getYear()
                                , bookManager.BookList.get(timernum).getMonth()
                                , bookManager.BookList.get(timernum).getDate()
                );
                //date calendar
                datebtn.setText(timer_date);
            }
        }
        else { //weekly
            datebtn.setVisibility(INVISIBLE);
            datebtn.setFocusable(false);
            date_text.setVisibility(View.VISIBLE);
            date_text.setText(R.string.STR_DAY);
            Spinnertimerweekly.setVisibility(View.VISIBLE);
            Spinnertimerweekly.setFocusable(true);
            Spinnertimerweekly.setFocusableInTouchMode(true);   // Johnny 20181219 for mouse control
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//key event
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:{
                if (timerenable.GetSelectedItemIndex() == 1) {
                    Log.d(TAG, "onKeyDown: save timer");
                    UpdateEditTextStartTime(); // Johnny 20180515 add to update starttime/duration in edittext if press back key
                    UpdateEditTextDuration();  // Johnny 20180515 add to update starttime/duration in edittext if press back key
                    saveTimer();
                }
                else {
                    Log.d(TAG, "onKeyDown: cancel timer");
                    cancelTimer();
                }
            }break;

//            case KeyEvent.KEYCODE_DPAD_UP:{
//                Log.d(TAG, "KEYCODE_DPAD_UP");
//                if(datebtn.getVisibility() == View.VISIBLE) {//date visible
//                    if (startTimeEdit.hasFocus()) {
//                        datebtn.requestFocus();
//                        return true;
//                    }
//                }
//                else{//date invisible
//                    if (startTimeHourEdit.hasFocus()) {
//                        if(Spinnertimerweekly.getVisibility() == View.VISIBLE)
//                        {
//                            Spinnertimerweekly.requestFocus();
//                        }
//                        else {
//                            Spinnertimertype.requestFocus();
//                        }
//                        return true;
//                    }
//                    else if(Spinnertimerweekly.hasFocus()) {
//                        Spinnertimertype.requestFocus();
//                        return  true;
//                    }
//                }
//                if (Spinnertimerenable.hasFocus()) {
//                    durationHourEdit.requestFocus();
//                    return true;
//                }
//                else if(selectChannelbtn.hasFocus()){
//                    Spinnertimerenable.requestFocus();
//                    return true;
//                }
//            }break;
//
//            case KeyEvent.KEYCODE_DPAD_DOWN:{
//                Log.d(TAG, "KEYCODE_DPAD_DOWN");
//                if(datebtn.getVisibility() == View.VISIBLE) {//date visible
//                    if (datebtn.hasFocus()) {
//                        startTimeEdit.requestFocus();
//                        return true;
//                    }
//                }
//                else{//date invisible
//                    if (Spinnertimertype.hasFocus()) {
//                        if(Spinnertimerweekly.getVisibility() == View.VISIBLE)
//                        {
//                            Spinnertimerweekly.requestFocus();
//                        }
//                        else {
//                            startTimeHourEdit.requestFocus();
//                        }
//                        return true;
//                    }
//                    else if(Spinnertimerweekly.hasFocus())
//                    {
//                        startTimeHourEdit.requestFocus();
//                        return true;
//                    }
//                }
//                if(Spinnertimerenable.hasFocus()){
//                    selectChannelbtn.requestFocus();
//                    return true;
//                }
//            }break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void cancelTimer()
    {
        if(timernum < bookManager.BookList.size()) {
//            BookInfoDelete(bookManager.BookList.get(timernum).getBookId()); // remove when use pesi service
            bookManager.DelBookInfo(bookManager.BookList.get(timernum));
            bookManager.Save();

            Intent intent = new Intent();
            intent.putExtra(TimerListActivity.TIMER_KEY_POS, timernum);
            setResult(RESULT_OK, intent);
            finish();
        }
        else
        {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    public void saveTimer(){
        Log.d(TAG, "saveTimer");
        BookInfo bookInfo = new BookInfo();
        int startTime,duration;
        long startTimeMillisecs;
        String year, month, week, day, dateString;

        //calculate startTime
        startTime = BookStartTime[0] * 100 + BookStartTime[1] ;
        startTimeMillisecs = (long) ((BookStartTime[0]*3600 + BookStartTime[1]*60) * 1000);
        //calculate duration
        duration = BookDuration[0] * 100 + BookDuration[1];

        //calculate book date (include hour, min)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        if(timercycle.GetSelectedItemIndex() == BookInfo.BOOK_CYCLE_ONETIME)//timer cycle is once, get calendar time
        {
            dateString = datebtn.getText().toString();
        }
        else//timer cycle is not once, get current time
            dateString = TempTexView.getText().toString();
        Date sDate = GetLocalTime();
        try {
            sDate = sdf.parse(dateString);
            sDate.setTime(sDate.getTime() + startTimeMillisecs);   // add startTime millisecs
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG, "saveTimer: error " + e.toString());
        }

        // adjust date by bookcycle
        switch (timercycle.GetSelectedItemIndex())
        {
            case BookInfo.BOOK_CYCLE_DAILY:
            {
                sDate = GetDateOfNextDaily(sDate);
            } break;
            case BookInfo.BOOK_CYCLE_WEEKLY:
            {
                sDate = GetDateOfNextWeekly(sDate, timerweekly.GetSelectedItemIndex());
            } break;
            case BookInfo.BOOK_CYCLE_WEEKEND:
            {
                sDate = GetDateOfNextWeekEnd(sDate);
            } break;
            case BookInfo.BOOK_CYCLE_WEEKDAYS:
            {
                sDate = GetDateOfNextWeekDays(sDate);
            } break;
        }

        SimpleDateFormat syear = new SimpleDateFormat("yyyy", Locale.getDefault());
        year = syear.format(sDate);//get year

        SimpleDateFormat smonth = new SimpleDateFormat("MM", Locale.getDefault());
        month = smonth.format(sDate);//get month

        SimpleDateFormat sday = new SimpleDateFormat("dd", Locale.getDefault());
        day = sday.format(sDate);//get day

        if(timercycle.GetSelectedItemIndex() == BookInfo.BOOK_CYCLE_ONETIME)//timercycle is weekly, directly get week
            week = Integer.toString(timerweekly.GetSelectedItemIndex()+1);  // index = 0~6, trans to 1~7
        else{//timer cycle is not weekly, get calendar week
            SimpleDateFormat sweek = new SimpleDateFormat("u", Locale.getDefault());
            week = sweek.format(sDate);//get weekday => 1~7 Monday~Sunday
        }

        if(timernum >= bookManager.BookList.size()) {
            //Add new timer
            bookInfo.setBookId(timernum/*BookInfo.MAX_NUM_OF_BOOKINFO*/);// use MAX_NUM_OF_BOOKINFO, do not change
            bookInfo.setChannelId(book_channelId);//Booking Channel Number
            bookInfo.setGroupType(book_type);//Group type
            bookInfo.setEventName("");//Booking Channel Name
            bookInfo.setStartTime(startTime);//Booking starTime
            bookInfo.setDuration(duration);//Booking duration
            bookInfo.setYear(Integer.valueOf(year));//Booking Timer year
            bookInfo.setMonth(Integer.valueOf(month));//Booking Timer month
            bookInfo.setWeek(Integer.valueOf(week)-1);//Booking Timer week, tran 1~7 to 0~6
            bookInfo.setDate(Integer.valueOf(day));//Booking Timer day
            bookInfo.setBookType(timertype.GetSelectedItemIndex());//Booking Timer Type
            bookInfo.setBookCycle(timercycle.GetSelectedItemIndex());//Booking Timer Cycle
            bookInfo.setEnable(timerenable.GetSelectedItemIndex());
            if(timercycle.GetSelectedItemIndex() == BookInfo.BOOK_CYCLE_ONETIME
                    && !bookManager.CheckBookAfterNow(bookInfo)) {  // check time wrong if cycle = once

                new MessageDialogView(TimerSettingActivity.this, getString(R.string.STR_TIMER_ENTER_TIME_AFTER_NOW), 3000)
                {
                    public void dialogEnd() {
                    }
                }.show();
            }
            else if((conflictBooklist = BookInfoFindConflictBooks(bookInfo)) != null)   // find conflict
            {
                final BookInfo bookInfoForAdd = bookInfo;
                mConflictDialog = new BookConflictDialogView(TimerSettingActivity.this, conflictBooklist)
                {
                    @Override
                    public void onDialogPositiveClick()
                    {
                        // del conflict books
                        bookManager.DelBookInfoList(conflictBooklist);

                        /*// remove when use pesi service
                        for (int i = 0 ; i < conflictBooklist.size() ; i++)
                        {
                            BookInfoDelete(conflictBooklist.get(i).getBookId());
                        }*/

//                        BookInfoAdd(bookInfoForAdd);  // remove when use pesi service
                        bookManager.AddBookInfo(bookInfoForAdd);
                        bookManager.Save();

                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onDialogNegativeClick()
                    {
                    }
                };//.show();

                // Edwin 20190508 fix dialog not focus -s
                runOnUiThread(new Runnable() {
                    @Override
                    public void run () {
                        mConflictDialog.show();
                    }
                });
                // Edwin 20190508 fix dialog not focus -e
            }
            else {  // no conflict
//                BookInfoAdd(bookInfo);  // remove when use pesi service
                bookManager.AddBookInfo(bookInfo);
                bookManager.Save();

                setResult(RESULT_OK);
                finish();
            }
        }
        else
        {
            //Update timer parameter
            //bookManager.BookList.get(timernum).setBookId(timernum);//Booking Timer Id
            bookManager.BookList.get(timernum).setChannelId(book_channelId/*programInfo.getChannelId()*/);
            bookManager.BookList.get(timernum).setGroupType(book_type);//Group type
//            bookManager.BookList.get(timernum).setEventName("");//Booking Event Name, cant change eventName when update
            bookManager.BookList.get(timernum).setStartTime(startTime);//Booking starTime
            bookManager.BookList.get(timernum).setDuration(duration);//Booking duration
            bookManager.BookList.get(timernum).setYear(Integer.valueOf(year));//Booking Timer year
            bookManager.BookList.get(timernum).setMonth(Integer.valueOf(month));//Booking Timer month
            bookManager.BookList.get(timernum).setWeek(Integer.valueOf(week)-1);//Booking Timer week, trans 1~7 to 0~6
            bookManager.BookList.get(timernum).setDate(Integer.valueOf(day));//Booking Timer day
            bookManager.BookList.get(timernum).setBookType(timertype.GetSelectedItemIndex());//Booking Timer Type
            bookManager.BookList.get(timernum).setBookCycle(timercycle.GetSelectedItemIndex());//Booking Timer Cycle
            bookManager.BookList.get(timernum).setEnable(timerenable.GetSelectedItemIndex());
            if(bookManager.BookList.get(timernum).getBookCycle() == BookInfo.BOOK_CYCLE_ONETIME
                    && !bookManager.CheckBookAfterNow(bookManager.BookList.get(timernum))) {    // check time wrong if cycle = once

                new MessageDialogView(TimerSettingActivity.this, getString(R.string.STR_TIMER_ENTER_TIME_AFTER_NOW), 3000)
                {
                    public void dialogEnd() {
                    }
                }.show();
            }
            else if((conflictBooklist = BookInfoFindConflictBooks(bookManager.BookList.get(timernum))) != null)   // find conflict
            {
                final BookInfo bookInfoForUpdate = bookManager.BookList.get(timernum);
//                final int bookIdHisi = bookInfoForUpdate.getBookId();   // remove when use pesi service

                /*// remove when use pesi service, the only conflict is self
                if (conflictBooklist.size() == 1 && conflictBooklist.get(0).getBookId() == bookInfoForUpdate.getBookId())
                {
                    BookInfoUpdate(bookManager.BookList.get(timernum));
                    bookManager.Save();

                    Intent intent = new Intent();
                    intent.putExtra(TimerListActivity.TIMER_KEY_POS, timernum);
                    setResult(RESULT_OK, intent);
                    finish();
                    return;
                }*/

                /*// remove when use pesi service
                for (int i = 0 ; i < conflictBooklist.size() ; i++)
                {
                    if (bookInfoForUpdate.getBookId() == conflictBooklist.get(i).getBookId())  //remove self
                    {
                        conflictBooklist.remove(i);
                        break;
                    }
                }*/

                mConflictDialog = new BookConflictDialogView(TimerSettingActivity.this, conflictBooklist)
                {
                    @Override
                    public void onDialogPositiveClick()
                    {
                        int minusFocus = calcConflictCountAboveUpdateBook(bookInfoForUpdate);

                        // del conflict books
                        bookManager.DelBookInfoList(conflictBooklist);

                        /*// remove when use pesi service
                        for (int i = 0 ; i < conflictBooklist.size() ; i++) {
                            BookInfoDelete(conflictBooklist.get(i).getBookId());
                        }*/

//                        bookInfoForUpdate.setBookId(bookIdHisi);    // remove when use pesi service
//                        BookInfoUpdate(bookInfoForUpdate);  // remove when use pesi service
                        bookManager.Save();

                        Intent intent = new Intent();
                        intent.putExtra(TimerListActivity.TIMER_KEY_POS, timernum - minusFocus);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onDialogNegativeClick()
                    {
                    }
                };//.show();

                // Edwin 20190508 fix dialog not focus -s
                runOnUiThread(new Runnable() {
                    @Override
                    public void run () {
                        mConflictDialog.show();
                    }
                });
                // Edwin 20190508 fix dialog not focus -e
            }
            else {
//                BookInfoUpdate(bookManager.BookList.get(timernum));  // remove when use pesi service
                bookManager.Save();

                Intent intent = new Intent();
                intent.putExtra(TimerListActivity.TIMER_KEY_POS, timernum);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
    public void dismissDialog(){
        Log.d(TAG, "dismissDialog");
        if(mDialog!=null&& mDialog.isShowing()){
            mDialog.dismiss();//close dialog
        }
    }

    public boolean isShowing() {
        Log.d(TAG, "isShowing");
        //check dialog is exist
        return mDialog != null && mDialog.isShowing();
    }

    private void setup_timeupdatethread(){
        Log.d(TAG, "setup_timeupdatethread");
        currenttimer_handler = new Handler();
        currenttimer_runnable = new Runnable() {
            public void run() {
                refresh_currenttime();
                currenttimer_handler.postDelayed(currenttimer_runnable, 1000);
            }
        };

        currenttimer_handler.post(currenttimer_runnable);
    }

    private void refresh_currenttime(){
        Log.d(TAG, "refresh_currenttime");
        Date date = GetLocalTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String today = sdf.format(date);

        TempTexView = (TextView)findViewById(R.id.currenttimeTXV);
        TempTexView.setText((""+today));
    }

    private int getCurChPos()
    {
        long curChId = ViewHistory.getCurChannel().getChannelId();
        return ViewHistory.getCurListPos(curChId);
    }

    private int calcConflictCountAboveUpdateBook(BookInfo updateBook)
    {
        int count = 0;
        for (int i = 0 ; i < conflictBooklist.size() ; i++)
        {
            if (conflictBooklist.get(i).getBookId() < updateBook.getBookId())
            {
                count++;
            }
            else
            {
                break;
            }
        }

        return count;
    }

    // edwin 20180709 add page up & page down -s
    private void PagePrev(RecyclerView list)
    {
        Log.d(TAG, "PagePrev: ");

        int topPos  = list.getChildAdapterPosition(list.getChildAt(0));
        int curPos  = list.getChildAdapterPosition(list.getFocusedChild());
        int samePos = curPos - topPos;
        float totalHeight;

        if ( list.getChildAt(0) != null )
        {
            totalHeight = list
                    .getChildAt(0)
                    .getMeasuredHeight() * list.getAdapter().getItemCount();
        }
        else
        {
            totalHeight = getResources()
                    .getDimension(R.dimen.LIST_VIEW_HEIGHT) * list.getAdapter().getItemCount();
        }

        if ( list.getChildAdapterPosition(list.getChildAt(0)) == 0 )
        {
            list.scrollBy(0, (int) totalHeight);
        }
        else
        {
            list.scrollBy(0, -list.getLayoutParams().height);
        }

        list.getChildAt(samePos).requestFocus();
    }

    private void PageNext(RecyclerView list)
    {
        Log.d(TAG, "PageNext: ");

        int topPos  = list.getChildAdapterPosition(list.getChildAt(0));
        int curPos  = list.getChildAdapterPosition(list.getFocusedChild());
        int samePos = curPos - topPos;
        float totalHeight;

        if ( list.getChildAt(0) != null )
        {
            totalHeight = list.getChildAt(0).getMeasuredHeight() * list.getAdapter().getItemCount();
        }
        else
        {
            totalHeight = getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT) * list.getAdapter().getItemCount();
        }

        if ( list.getChildAdapterPosition(list
                .getChildAt(list.getChildCount()-1)) == (list.getAdapter().getItemCount()-1) )
        {
            list.scrollBy(0, (int) -totalHeight);
        }
        else
        {
            list.scrollBy(0, list.getLayoutParams().height);
        }

        list.getChildAt(samePos).requestFocus();
    }
    // edwin 20180709 add page up & page down -e

    // connie 20180731 for use EditText to modify start time & duration -s
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
                durationEdit.selectAll();
            }
            else {
                UpdateEditTextDuration();   // Johnny 20180515 use function instead
            }
        }
    };

    private View.OnFocusChangeListener startTime_change_focus = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                startTimeEdit.selectAll();
            }
            else {
                UpdateEditTextStartTime();  // Johnny 20180515 use function instead
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
                        if(startTimeEdit.getText().length()==5)
                            durationEdit.requestFocus();
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        durationEdit.requestFocus();
                        return true;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        //Scoty 20180801 fixed Timer Settings press up key not work -s
                        if(datebtn.getVisibility() == View.VISIBLE)
                            datebtn.requestFocus();
                        else if(Spinnertimerweekly.getVisibility() == View.VISIBLE)
                            Spinnertimerweekly.requestFocus();
                        else
                            Spinnertimertype.requestFocus();
                        //Scoty 20180801 fixed Timer Settings press up key not work -e
                        return true;
                    case KeyEvent.KEYCODE_DEL:
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        deleteEditText(startTimeEdit);
                        return true;

                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if(startTimeEdit.getText().length() < 3)
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
                        Log.d(TAG, "startTimeKeyListener:  " + startTimeEdit.getSelectionStart() + "     " + startTimeEdit.getSelectionEnd());
                        if(startTimeEdit.getSelectionStart() == 0 && startTimeEdit.getSelectionEnd() == 5) // select all
                            startTimeEdit.setText(":");
                        String input = Integer.toString(keyCode-KeyEvent.KEYCODE_0);
                        insertEditText(startTimeEdit, input);

                        if (startTimeEdit.length() == 5)    // Johnny 20180515 add to update starttime in edittext if input 5(':' included) char
                        {
                            UpdateEditTextStartTime();
                        }

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
                        if(durationEdit.getText().length()==5)
                            Spinnertimerenable.requestFocus();
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        Spinnertimerenable.requestFocus();
                        return true;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        startTimeEdit.requestFocus();
                        return true;
                    case KeyEvent.KEYCODE_DEL:
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        deleteEditText(durationEdit);
                        return true;

                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if(durationEdit.getText().length() < 3)
                            return true;
                        break;

                    case KeyEvent.KEYCODE_0:
                        String illegalStr00_0 = getString( R.string.STR_ILLEGAL_TIME ); // illegal string "00:0"
                        if ( durationEdit.getText().toString().equals( illegalStr00_0 ) )
                        {
                            Toast.makeText( getApplicationContext(), getString( R.string.STR_TIMER_MORE_THAN_1_MINUTE ), Toast.LENGTH_LONG ).show();
                            return true;
                        }
                    case KeyEvent.KEYCODE_1:
                    case KeyEvent.KEYCODE_2:
                    case KeyEvent.KEYCODE_3:
                    case KeyEvent.KEYCODE_4:
                    case KeyEvent.KEYCODE_5:
                    case KeyEvent.KEYCODE_6:
                    case KeyEvent.KEYCODE_7:
                    case KeyEvent.KEYCODE_8:
                    case KeyEvent.KEYCODE_9:
                        Log.d(TAG, "DurationKeyListener:  " + durationEdit.getSelectionStart() + "     " + durationEdit.getSelectionEnd());
                        if(durationEdit.getSelectionStart() == 0 && durationEdit.getSelectionEnd() == 5) // select all
                            durationEdit.setText(":");
                        String input = Integer.toString(keyCode-7);
                        insertEditText(durationEdit, input);

                        if (durationEdit.length() == 5)    // Johnny 20180515 add to update duration in edittext if input 5(':' included) char
                        {
                            UpdateEditTextDuration();
                        }

                        return true;
                    default:
                        break;
                }
            }
            return false;
        }
    };
    // connie 20180731 for use EditText to modify start time & duration -e

    // Johnny 20180815 add -s
    private void UpdateEditTextStartTime()
    {
        String NewStr = startTimeEdit.getText().toString();
        Log.d(TAG, "SetStartTime:  NewStr = " + NewStr);
        int hour, min;
        if(NewStr.length() == 5 && NewStr.indexOf(":") == 2) {  // Johnny 20181219 for mouse control, ":" must be the 3rd char
            hour = Integer.valueOf(NewStr.substring(0,2));
            min = Integer.valueOf(NewStr.substring(3,5));
            if(hour <= 23 && min <= 59) {
                BookStartTime[0] = hour;//Integer.valueOf(hour);
                BookStartTime[1] = min;//Integer.valueOf(min);
                Log.d(TAG, "SetStartTime: BookStartTime[0]"+BookStartTime[0] + "    BookStartTime[1]"+BookStartTime[1]);
                return;
            }
        }

        NewStr = String.format(Locale.getDefault(), "%02d:%02d",BookStartTime[0],BookStartTime[1]);
        Log.d(TAG, "SetStartTime: BookStartTime[0]"+BookStartTime[0] + "    BookStartTime[1]"+BookStartTime[1]);
        startTimeEdit.setText(NewStr);
        startTimeEdit.selectAll();
    }

    private void UpdateEditTextDuration()
    {
        String NewStr = durationEdit.getText().toString();
        Log.d(TAG, "SetDuration:  NewStr = " + NewStr);
        int hour, min;
        if(NewStr.length() == 5) {
            hour = Integer.valueOf(NewStr.substring(0,2));
            min = Integer.valueOf(NewStr.substring(3,5));
            if(hour <= 23 && min <= 59) {
                BookDuration[0] = hour;//Integer.valueOf(hour);
                BookDuration[1] = min;//Integer.valueOf(min);
                Log.d(TAG, "SetDuration: BookDuration[0]"+BookDuration[0] + "    BookDuration[1]"+BookDuration[1]);
                return;
            }
        }

        NewStr = String.format(Locale.getDefault(), "%02d:%02d",BookDuration[0],BookDuration[1]);
        Log.d(TAG, "SetDuration: BookDuration[0]"+BookDuration[0] + "    BookDuration[1]"+BookDuration[1]);
        durationEdit.setText(NewStr);
        durationEdit.selectAll();
    }
    // Johnny 20180815 add -e
}
