package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.BookInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.prime.dtvplayer.Sysdata.ProgramInfo;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * Created by johnny_shih on 2018/3/1.
 */

public class BookConflictDialogView extends Dialog {
    private final String TAG = getClass().getSimpleName();

    private Context mContext;

    private TextView mWarningTextView;
    private RecyclerView mConflictItemRecycleView;
    private Button mBtnOk;
    private Button mBtnCancel;

    private List<BookInfo> mConflictBookList;
    private DTVActivity mDtvActivity;
    private int mDialogHeight;

    protected BookConflictDialogView(@NonNull Context context,
                                     final List<BookInfo> conflictBookList)
    {
        super(context);

        mContext    = context;
        mConflictBookList = conflictBookList;
        mDtvActivity = (DTVActivity) mContext;

        if (mConflictBookList == null)
        {
            mConflictBookList = new ArrayList<>();
        }
        Log.d(TAG, "BookConflictDialogView: Conflict Count = " + mConflictBookList.size());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.book_conflict_dialog);

        InitLayoutParams();
        InitWarningText();
        InitRecycleView();
        InitButton();
    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus) {
        if (hasFocus)
        {
            // Set recycleview height
            //for largest display size,--start
            int itemHeight = (int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT);
            int availableHeight = findViewById(R.id.bookconflictLIV_ll).getHeight();
            int listCount = availableHeight / itemHeight;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    (int)mContext.getResources().getDimension(R.dimen.BOOKCONFLICTDIALOG_WIDTH) , itemHeight * listCount);

            mConflictItemRecycleView.setLayoutParams(layoutParams);
            //for largest display size,--end
        }
    }

    private void InitLayoutParams()
    {
        // Set dialog height
        //for largest display size,--start
        int tmpHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        mDialogHeight = (int) (tmpHeight*0.6);//dialog height

        ConstraintLayout view = (ConstraintLayout) findViewById(R.id.book_conflict_dialog_layout);
        if(view != null) {
            view.setMinHeight(mDialogHeight);
        }
        //for largest display size,--end

        WindowManager.LayoutParams windowParams;
        if (getWindow() != null) {
            windowParams = getWindow().getAttributes();
            windowParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        }
    }

    private void InitWarningText()
    {
        mWarningTextView = (TextView) findViewById(R.id.warningTXV);

        // Set warning text color
        SpannableStringBuilder style = new SpannableStringBuilder(mContext.getString(R.string.STR_THE_FOLLOW_CONFLICTS_WILL_BE_REMOVED));
        style.setSpan(new ForegroundColorSpan(Color.RED), 32, 39, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mWarningTextView.setText(style);
    }

    private void InitRecycleView()
    {
        mConflictItemRecycleView = (RecyclerView) findViewById(R.id.conflictItemsRecycleView);

        // Set recycleview
        mConflictItemRecycleView.setHasFixedSize(true);
        mConflictItemRecycleView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mConflictItemRecycleView.setAdapter(new BookAdapter(mConflictBookList));
    }

    private void InitButton()
    {
        mBtnOk = (Button) findViewById(R.id.okBTN);
        mBtnCancel = (Button) findViewById(R.id.cancelBTN);

        // Set button
        mBtnCancel.setOnClickListener(CancelClickListener);
        mBtnOk.setOnClickListener(OkClickListener);
        mBtnCancel.requestFocus();
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
                no = (TextView) itemView.findViewById(R.id.bookingConflictIdTXV);
                type = (TextView) itemView.findViewById(R.id.bookingConflictTypeTXV);
                channel = (TextView) itemView.findViewById(R.id.bookingConflictChnameTXV);
                date = (TextView) itemView.findViewById(R.id.bookingConflictDateTXV);
                time = (TextView) itemView.findViewById(R.id.bookingConflictTimeTXV);
            }
        }

        String bookCycleList[];
        String weekList[];
        ProgramInfo programInfo;
        private List<BookInfo> listItem;

        BookAdapter(List<BookInfo> bookInfoList) {
            listItem = bookInfoList;
        }

        @Override
        public BookAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.timer_conflict_list_item, parent, false);
            return new BookAdapter.ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final BookAdapter.ViewHolder holder, int position) {
            holder.itemView.setFocusable(true);
            holder.itemView.setBackgroundResource(R.drawable.focus_list);
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View itemView, boolean hasFocus) {
                    if (hasFocus) {
                        holder.type.setSelected(true);//eric lin 20180801 adjust bookConflict ui
                        holder.channel.setSelected(true);
                        holder.no.setTextColor(BLACK);
                        holder.type.setTextColor(BLACK);
                        holder.date.setTextColor(BLACK);
                        holder.time.setTextColor(BLACK);
                    }
                    else {
                        holder.type.setSelected(false);//eric lin 20180801 adjust bookConflict ui
                        holder.channel.setSelected(false);
                        holder.no.setTextColor(WHITE);
                        holder.type.setTextColor(WHITE);
                        holder.date.setTextColor(WHITE);
                        holder.time.setTextColor(WHITE);
                    }
                }
            });

            // ******** No. ********
            holder.no.setText(String.valueOf(position+1));  // use below instead when use pesi service
//            holder.no.setText(String.valueOf(listItem.get(position).getBookId()+1));

            // ******** Timer Cycle ********
            bookCycleList = mContext.getResources().getStringArray(R.array.STR_ARRAY_TIMER_CYCLE);

            String cycle = bookCycleList[listItem.get(position).getBookCycle()];
            if(listItem.get(position).getBookType() == BookInfo.BOOK_TYPE_RECORD) {
                holder.type.setText(cycle
                        .concat(" / ")
                        .concat(mContext.getString(R.string.STR_TIMER_RECORD)));
            }
            else {
                holder.type.setText(cycle
                        .concat(" / ")
                        .concat(mContext.getString(R.string.STR_TIMER_TURNON)));
            }

            // ******** Channel Name / Program ********
            programInfo = mDtvActivity.ProgramInfoGetByChannelId(listItem.get(position).getChannelId());
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
            weekList = mContext.getResources().getStringArray(R.array.STR_ARRAY_WEEK_CYCLE);
            String timerDay = null;
            //String year = String.valueOf(listItem.get(position).getYear());
            String month = String.valueOf(listItem.get(position).getMonth());
            String daily = String.valueOf(listItem.get(position).getDate());
            String saturday = mContext.getString(R.string.STR_TIMER_SAT);
            String sunday   = mContext.getString(R.string.STR_TIMER_SUN);
            String monday   = mContext.getString(R.string.STR_TIMER_MON);
            String friday   = mContext.getString(R.string.STR_TIMER_FRI);
            int week = listItem.get(position).getWeek();
            int bookCycle = listItem.get(position).getBookCycle();

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
            DTVActivity.BookManager bookManager = mDtvActivity.GetBookManager();
            Date endTimeDate = bookManager.GetEndTime(
                    listItem.get(position)
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

    private View.OnClickListener OkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onDialogPositiveClick();
            dismiss();
        }
    };

    private View.OnClickListener CancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onDialogNegativeClick();
            dismiss();
        }
    };

    public void onDialogNegativeClick()
    {
        //TODO
    }

    public void onDialogPositiveClick()
    {
        //TODO
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        View curFocusChild = mConflictItemRecycleView.getFocusedChild();
        View firstChild = mConflictItemRecycleView.getChildAt(0);
        View lastChild = mConflictItemRecycleView.getChildAt(mConflictItemRecycleView.getChildCount() - 1);
        int curFocusPos = mConflictItemRecycleView.getChildAdapterPosition(curFocusChild);
        int firstChildPos = mConflictItemRecycleView.getChildAdapterPosition(firstChild);
        int lastChildPos = mConflictItemRecycleView.getChildAdapterPosition(lastChild);
        int lastAdapterItemPos = mConflictItemRecycleView.getAdapter().getItemCount() - 1;
        int itemHeight = (int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT);

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (curFocusPos < lastAdapterItemPos && curFocusPos == lastChildPos) {
                    mConflictItemRecycleView.scrollBy(0, itemHeight);
                    mConflictItemRecycleView.getChildAt(mConflictItemRecycleView.getChildCount() - 1).requestFocus();
                    return true;
                } break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (curFocusPos > 0 && curFocusPos == firstChildPos) {
                    mConflictItemRecycleView.scrollBy(0, -itemHeight);
                    mConflictItemRecycleView.getChildAt(0).requestFocus();
                    return true;
                } break;
        }

        return super.onKeyDown(keyCode, event);
    }
}
