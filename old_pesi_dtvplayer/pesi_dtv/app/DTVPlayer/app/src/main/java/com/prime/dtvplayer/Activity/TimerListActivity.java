package com.prime.dtvplayer.Activity;

/*
  Created by scoty_kuo on 2017/11/21.
 */

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.MessageDialogView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static androidx.recyclerview.widget.RecyclerView.OnFocusChangeListener;

public class TimerListActivity extends DTVActivity {
    private static final String TAG = "TimerListActivity";
    private static int visibleCount;

    /**
     * Key name & define. Used for Timer to pass bundle..
     */
    public static final String TIMER_KEY_POS = "position";
    public static final int TIMER_ADD_REQUEST = 0;
    public static final int TIMER_UPDATE_REQUEST = 1;

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private LinearLayoutManager bookLayoutManager = null;
    private TextView messageView;
    private BookManager bookManager = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_list);

        //BookManagerInit();
        bookManager = GetBookManager();

        ActivityTitleView setActivityTitle;
        setActivityTitle = (ActivityTitleView) findViewById(R.id.activityTitleViewLayoutTimerList);
        setActivityTitle.setTitleView(getString(R.string.STR_TIMER_TITLE));

        messageView = (TextView) findViewById(R.id.notimermsgTXV);
        if (bookManager.BookList.isEmpty())
        {
            messageView.setVisibility(View.VISIBLE);
            messageView.requestFocus();
        }

        ActivityHelpView TimerListHelpView;
        TimerListHelpView = (ActivityHelpView) findViewById(R.id.timerListHelpViewLayout);
        TimerListHelpView.resetHelp(1,R.drawable.help_red,getString(R.string.STR_ADD));
        TimerListHelpView.resetHelp(2,R.drawable.help_blue,getString(R.string.STR_DELETE));
        TimerListHelpView.resetHelp(3,0,null);
        TimerListHelpView.resetHelp(4,0,null);

        // Johnny 20181228 for mouse control -s
        TimerListHelpView.setHelpIconClickListener(1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProgRedClicked();
            }
        });
        TimerListHelpView.setHelpIconClickListener(2, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProgBlueClicked();
            }
        });
        // Johnny 20181228 for mouse control -e

        TimerListHelpView.setHelpInfoTextBySplit(getString(R.string.STR_TIMER_HELP_INFO)); // edwin 20180709 add page up & page down info

        InitRecyclerView();
    }

    private class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView no;
            TextView type;
            TextView channel;
            TextView date;
            TextView time;
            ViewHolder(View itemView){
                super(itemView);
                no = (TextView) itemView.findViewById(R.id.bookingidTXV);
                type = (TextView) itemView.findViewById(R.id.bookingtypeTXV);
                channel = (TextView) itemView.findViewById(R.id.bookingchnameTXV);
                date = (TextView) itemView.findViewById(R.id.bookingdateTXV);
                time = (TextView) itemView.findViewById(R.id.bookingtimeTXV);
            }
        }

        String typeList[];
        String weekList[];
        ProgramInfo programInfo;
        List<BookInfo> listItem;
        BookAdapter(List<BookInfo> bookInfoList) {
            listItem = bookInfoList;
        }

        @Override
        public BookAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.timer_list_item, parent, false);
            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.itemView.setFocusable(true);
            holder.itemView.setFocusableInTouchMode(true);  // Johnny 20181219 for mouse control
            holder.itemView.setBackgroundResource(R.drawable.focus_list);
            holder.itemView.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View itemView, boolean hasFocus) {
                    if (hasFocus) {
                        holder.channel.setSelected(true);
                        holder.type.setSelected(true);//for Marquee
                        holder.date.setSelected(true);//for Marquee
                        holder.time.setSelected(true);//for Marquee
                        holder.no.setTextColor(BLACK);
                    }
                    else {
                        holder.channel.setSelected(false);
                        holder.type.setSelected(false);//for Marquee
                        holder.date.setSelected(false);//for Marquee
                        holder.time.setSelected(false);//for Marquee
                        holder.no.setTextColor(WHITE);
                    }
                }
            });

            // Johnny 20181219 for mouse control -s
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int curFocusPos = recyclerView.getChildAdapterPosition(view);
                    StartSettingActivityForResult(curFocusPos, TIMER_UPDATE_REQUEST);
                }
            });
            // Johnny 20181219 for mouse control -e

            // ******** No. ********
//            holder.no.setText(String.valueOf(position+1));  // use below instead when use pesi service
            holder.no.setText(String.valueOf(listItem.get(position).getBookId()+1));

            // ******** Timer Cycle ********
            typeList = getResources().getStringArray(R.array.STR_ARRAY_TIMER_CYCLE);

            String type = typeList[listItem.get(position).getBookCycle()];
            if(listItem.get(position).getBookType() == 0) {
                holder.type.setText(type
                        .concat(" / ")
                        .concat(getString(R.string.STR_TIMER_RECORD)));
            }
            else {
                holder.type.setText(type
                        .concat(" / ")
                        .concat(getString(R.string.STR_TIMER_TURNON)));
            }

            // ******** Channel Name / Program ********
            programInfo = ProgramInfoGetByChannelId(listItem.get(position).getChannelId());
            if(listItem.get(position).getEventName().equals("")) {
                holder.channel.setText(programInfo.getDisplayName());
            }
            else {
                holder.channel.setText(programInfo
                        .getDisplayName()
                        .concat(" / ")
                        .concat(listItem.get(position).getEventName())
                );
            }

            // Date
            weekList = getResources().getStringArray(R.array.STR_ARRAY_WEEK_CYCLE);
            String timerDay = null;
            //String year = String.valueOf(listItem.get(position).getYear());
            String month = String.valueOf(listItem.get(position).getMonth());
            String daily = String.valueOf(listItem.get(position).getDate());
            String saturday = getString(R.string.STR_TIMER_SAT);
            String sunday   = getString(R.string.STR_TIMER_SUN);
            String monday   = getString(R.string.STR_TIMER_MON);
            String friday   = getString(R.string.STR_TIMER_FRI);
            int week = listItem.get(position).getWeek();
            int bookCycle = bookManager.BookList.get(position).getBookCycle();

            switch (bookCycle) {
                case BookInfo.BOOK_CYCLE_ONETIME:
                case BookInfo.BOOK_CYCLE_DAILY:
                    timerDay = daily + "." + month;
                    break;
                case BookInfo.BOOK_CYCLE_WEEKLY:
                    timerDay = daily + "." + month + " " + weekList[week]; //weekly 29.3 SAT
                    break;
                case BookInfo.BOOK_CYCLE_WEEKEND:
                    timerDay = saturday + "&" + sunday; // weekend SAT&SUN
                    break;
                case BookInfo.BOOK_CYCLE_WEEKDAYS:
                    timerDay = monday + "-" + friday; //weekday MON-FRI
            }
            holder.date.setText(timerDay);

            // ******** Time ********
            // start time ~ end time
            int startTime = listItem.get(position).getStartTime();
            int startTimeHour = startTime / 100;
            int startTimeMin = startTime % 100;
            Date endTimeDate = bookManager.GetEndTime(
                    bookManager.BookList.get(position)
            ); // get end time Date
            SimpleDateFormat sdfHour =
                    new SimpleDateFormat("HH", Locale.getDefault()); //get end time Hour
            int endTimeHour = Integer.valueOf(sdfHour.format(endTimeDate));
            SimpleDateFormat sdfMin =
                    new SimpleDateFormat("mm", Locale.getDefault()); //get end time Min
            int endTimeMin = Integer.valueOf(sdfMin.format(endTimeDate));

            String timerTime = String.format(Locale.getDefault(),
                    "%02d:%02d~%02d:%02d",
                    startTimeHour,
                    startTimeMin,
                    endTimeHour,
                    endTimeMin);
            holder.time.setText(timerTime);
        }

        @Override
        public int getItemCount() {
            return listItem.size();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        View curFocusChild = recyclerView.getFocusedChild();
        View firstChild = recyclerView.getChildAt(0);
        View lastChild = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
        int curFocusPos = recyclerView.getChildAdapterPosition(curFocusChild);
        int firstChildPos = recyclerView.getChildAdapterPosition(firstChild);
        int lastChildPos = recyclerView.getChildAdapterPosition(lastChild);
        int lastAdapterItemPos = recyclerView.getAdapter().getItemCount() - 1;
        int itemHeight = (int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT);

        switch (keyCode) {
            case KeyEvent.KEYCODE_PROG_RED: //add timer
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                Log.d(TAG," KEYCODE_PROG_RED ");
                onProgRedClicked();     // Johnny 20181228 for mouse control
            break;
            // Johnny 20181219 for mouse control, handled in on click listener
//            case KeyEvent.KEYCODE_DPAD_CENTER: //edit timer
//                if(bookAdapter.getItemCount() == 0)
//                    return true;
//
//                Log.d(TAG, "onKeyDown: curPos = "+ curFocusPos);
//                StartSettingActivityForResult(curFocusPos, TIMER_UPDATE_REQUEST);
//            break;
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                onProgBlueClicked();    // Johnny 20181228 for mouse control
            break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (recyclerView.getChildCount() <= 0)
                {
                    return false;
                }

                if (curFocusPos >= lastAdapterItemPos) {
                    // scroll to top
                    recyclerView.scrollToPosition(0);
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.getChildAt(0).requestFocus();
                        }
                    });

                    return true;
                } else if (curFocusPos >= lastChildPos) {
                    // scroll down one item
                    recyclerView.scrollBy(0, itemHeight);
                    recyclerView.getChildAt(recyclerView.getChildCount() - 1).requestFocus();

                    return true;
                }
            break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (recyclerView.getChildCount() <= 0)
                {
                    return false;
                }

                if (curFocusPos <= 0) {
                    // scroll to bottom
                    recyclerView.scrollToPosition(lastAdapterItemPos);
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.getChildAt(recyclerView.getChildCount() - 1).requestFocus();
                        }
                    });

                    return true;
                } else if (curFocusPos <= firstChildPos) {
                    // scroll up one item
                    recyclerView.scrollBy(0, -itemHeight);
                    recyclerView.getChildAt(0).requestFocus();

                    return true;
                }
            break;

            // edwin 20180709 add page up & page down -s

            case KeyEvent.KEYCODE_PAGE_DOWN:
                PagePrev(recyclerView);
                break;

            case KeyEvent.KEYCODE_PAGE_UP:
                PageNext(recyclerView);
                break;

            // edwin 20180709 add page up & page down -e
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
        {
            UpdateRecyclerView();

            if(bookManager.BookList.isEmpty())
            {
                messageView.setVisibility(View.VISIBLE);
                messageView.requestFocus();
            }
            else {
                messageView.setVisibility(View.INVISIBLE);

                if (requestCode == TIMER_ADD_REQUEST) {
                    FocusBookLast();
                }
                else if (requestCode == TIMER_UPDATE_REQUEST) {
                    FocusBookByItemPos(data.getIntExtra(TimerListActivity.TIMER_KEY_POS, 0));
                }
            }
        }
    }

    private void StartSettingActivityForResult(int curPos, int requestCode)
    {
        Intent intent = new Intent();
        intent.setClass(TimerListActivity.this, TimerSettingActivity.class);

        intent.putExtra(TIMER_KEY_POS, curPos);

        startActivityForResult(intent, requestCode);
    }

    private void FocusBookLast()
    {
        final int finalScrollPos = recyclerView.getLayoutManager().getItemCount()-1;

        recyclerView.scrollToPosition(finalScrollPos);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                int focusPos = recyclerView.getChildCount()-1;
                recyclerView.getChildAt(focusPos).requestFocus();
            }
        });
    }

    private void FocusBookByItemPos(int pos)
    {
        int itemCount = recyclerView.getLayoutManager().getItemCount();
        if (pos >= itemCount)
        {
            pos = itemCount - 1;
        }

        final int finalPos = pos;
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                View view = recyclerView.getLayoutManager().findViewByPosition(finalPos);

                if (view != null)
                {
                    view.requestFocus();
                }
                else
                {
                    recyclerView.scrollToPosition(finalPos);
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.getLayoutManager().findViewByPosition(finalPos).requestFocus();
                        }
                    });
                }
            }
        });
    }

    private void InitRecyclerView()
    {
        // set recyclerview
        recyclerView = (RecyclerView) this.findViewById(R.id.timerlistLIV);
        bookLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(bookLayoutManager);
        bookAdapter = new BookAdapter(bookManager.BookList);
        recyclerView.setAdapter(bookAdapter);

        // remove animator
        recyclerView.setItemAnimator(null);

        //for largest display size,--start
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        
        int itemHeight =  ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        Guideline top = (Guideline) findViewById(R.id.listview_top_guideline);
        Guideline bottom = (Guideline) findViewById(R.id.listview_bottom_guideline);
        float topPercent = ((ConstraintLayout.LayoutParams)top.getLayoutParams()).guidePercent;
        float bottomPercent = ((ConstraintLayout.LayoutParams)bottom.getLayoutParams()).guidePercent;
        float guideLineRange = bottomPercent - topPercent;
        visibleCount = (int)(height*guideLineRange)/itemHeight; //0.75-0.2=55;
        //Log.d(TAG, "TTT onCreate: displayedCount = " + displayedCount);
        //eric lin-end

        // set height
        int visibleHeight = (int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT) * visibleCount;
        ViewGroup.LayoutParams layoutParams = recyclerView.getLayoutParams();
        layoutParams.height = visibleHeight;
        recyclerView.setLayoutParams(layoutParams);
    }

    private void UpdateRecyclerView()
    {
        //BookManagerInit();
        bookManager = GetBookManager();

        BookAdapter bookAdapter = (BookAdapter)recyclerView.getAdapter();
        bookAdapter.listItem = bookManager.BookList;
        bookAdapter.notifyDataSetChanged();
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

    // Johnny 20181228 for mouse control -s
    private void onProgRedClicked() {
        if(bookManager.CheckFull())
        {
            new MessageDialogView(this, getString(R.string.STR_TIMER_FULL), 3000)
            {
                public void dialogEnd() {
                }
            }.show();
            return;
        }

        StartSettingActivityForResult(bookManager.GetEmptyBookId(), TIMER_ADD_REQUEST);
    }

    private void onProgBlueClicked() {
        View curFocusChild = recyclerView.getFocusedChild();
        int curFocusPos = recyclerView.getChildAdapterPosition(curFocusChild);

        if(recyclerView.getAdapter().getItemCount() > 0) {
            int total = bookManager.BookList.size();

            bookManager.DelBookInfo(bookManager.BookList.get(curFocusPos));
            bookManager.Save();
            bookAdapter.notifyItemRemoved(curFocusPos);
            if(curFocusPos == total-1)
                curFocusPos--;
            bookAdapter.notifyItemRangeChanged(curFocusPos, bookAdapter.getItemCount()-curFocusPos);

            if(bookManager.BookList.isEmpty())    // show no book msg when del last one
            {
                messageView.setVisibility(View.VISIBLE);
                messageView.requestFocus();
            }
        }
    }
    // Johnny 20181228 for mouse control -e
}
