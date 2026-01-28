package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.SimpleChannel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_PROG_BLUE;
import static android.view.KeyEvent.KEYCODE_PROG_YELLOW;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;





public class SummaryInfoDialog extends Dialog {

    private final String TAG = getClass().getSimpleName();
    public Dialog mDialog = null;
    private Context mContext = null;
    public DTVActivity mDTVActivity = null;
    public DTVActivity.BookManager mbookManager = null;
    public DTVActivity.EpgUiDisplay mepgUiDisplay = null;
    int itemHeight = 0;
    private final TextView mNoBookInfo;
    private final ReserveInfoAdapter mReserveAdapter;

    public SummaryInfoDialog(Context context, DTVActivity DtvActivity, DTVActivity.EpgUiDisplay epgUiDisplay, final DTVActivity.BookManager bookManager) {
        super(context);
        mDTVActivity = DtvActivity;
        mContext = context;
        mepgUiDisplay = epgUiDisplay;
        mbookManager = bookManager;
        mDialog = new Dialog(mContext, R.style.MyDialog);

//        mDialog.setCancelable(false);// disable click back button // Johnny 20181210 for keyboard control
        mDialog.setCanceledOnTouchOutside(false);// disable click home button and other area
        //mDialog.show();

        mDialog.setContentView(R.layout.epg_summary);
        Window window = mDialog.getWindow();

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.dimAmount = 0.0f;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        int width =lp.width;

        TextView title = (TextView) window.findViewById(R.id.titleTXV);
        final RecyclerView ListviewReserve = (RecyclerView) window.findViewById(R.id.SummaryLIV);
        mNoBookInfo = (TextView) window.findViewById(R.id.msgTXV);
        int summaryItemCount = 7;
        final int[] childPosition = {0};
        itemHeight =(int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT);

        title.setText(mContext.getResources().getString(R.string.STR_SUMMARY));

        ViewGroup.LayoutParams layoutParams = ListviewReserve.getLayoutParams();

        //for largest display size,--start
        int height = (int) mContext.getResources().getDimension(R.dimen.SUMMARYINFODIALOG_HEIGHT);
        //Log.d(TAG, "TTT onCreate:    width = " + width + "    height = " + height + ", itemHeight="+itemHeight);

        int displayedCount = (int)(height*0.7)/itemHeight; //0.8-0.1=0.7
        summaryItemCount = displayedCount;
        //Log.d(TAG, "TTT onCreate: displayedCount = " + displayedCount);
        //for largest display size,--end

        layoutParams.height = (int) (summaryItemCount * itemHeight);
        layoutParams.width = width;
        ListviewReserve.setLayoutParams(layoutParams);

        mReserveAdapter = new ReserveInfoAdapter(mContext, mbookManager.BookList);
        ListviewReserve.setAdapter(mReserveAdapter);
        ListviewReserve.setItemAnimator(null);
        // === NO  Book  Info ===
        if (mReserveAdapter.getCount() == 0)
            mNoBookInfo.setVisibility(VISIBLE);
        else
            mNoBookInfo.setVisibility(INVISIBLE);


        mDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override

            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if ((keyCode == KEYCODE_PROG_YELLOW || keyCode == ExtKeyboardDefine.KEYCODE_PROG_YELLOW)
                        && event.getAction() == ACTION_DOWN)  // Yellow Key : Close  // Johnny 20181210 for keyboard control
                    dialog.dismiss();

                if(mReserveAdapter.getItemCount() == 0)
                    return false;

                int allItemCount = mReserveAdapter.getItemCount();
                int childPos = childPosition[0];
                int totalHeight = (itemHeight * allItemCount);
                int childCount = ListviewReserve.getChildCount();

                boolean atTop = (mReserveAdapter.getSelectedPos() == 0);
                boolean atBottom = (mReserveAdapter.getSelectedPos() == (allItemCount - 1));
                boolean firstChild = (childPos == 0);
                boolean lastChild = (childPos == (childCount - 1));

                if (keyCode == KEYCODE_DPAD_UP && event.getAction() == ACTION_DOWN) {
                    if (atTop) {
                        Log.d(TAG, "onKeyDown: scroll bottom -> childPos = " + childPos);
                        ListviewReserve.scrollBy(0, totalHeight);
                        ListviewReserve.getChildAt(childCount - 1).requestFocus();
                        childPosition[0] = childCount - 1;
                        return true;
                    }
                    if (firstChild) {
                        Log.d(TAG, "onKeyDown: scroll up -> childPos = " + childPos);
                        ListviewReserve.scrollBy(0, -itemHeight);
                        ListviewReserve.getChildAt(childPos).requestFocus();
                        return true;
                    }
                }
                if (keyCode == KEYCODE_DPAD_DOWN && event.getAction() == ACTION_DOWN)
                {
                    if (atBottom) {
                        Log.d(TAG, "onKeyDown: scroll top -> childPos = " + childPos);
                        ListviewReserve.scrollBy(0, -totalHeight);
                        ListviewReserve.getChildAt(0).requestFocus();
                        childPosition[0] = 0;
                        return true;
                    }
                    if (lastChild) {
                        Log.d(TAG, "onKeyDown: scroll down -> childPos = " + childPos);
                        ListviewReserve.scrollBy(0, itemHeight);
                        ListviewReserve.getChildAt(childPos).requestFocus();
                        return true;
                    }
                }
                if ((keyCode == KEYCODE_PROG_BLUE || keyCode == ExtKeyboardDefine.KEYCODE_PROG_BLUE) && event.getAction() == ACTION_DOWN) {
                    if (mbookManager.BookList.size() > 0) {
                        int position = mReserveAdapter.getSelectedPos();
                        int total = mbookManager.BookList.size();
//                        mDTVActivity.BookInfoDelete(mDTVActivity.BookInfoGetList().get(position).getBookId());  // Johnny modify 20180316, remove when use pesi service
                        mbookManager.DelBookInfo(mbookManager.BookList.get(position));
                        mbookManager.Save();
                        mReserveAdapter.notifyItemRemoved(position);
                        if(position == total-1)
                            position--;
                        Log.d(TAG, "onKey:  position = " + position + "     count = "+(mReserveAdapter.getItemCount()-position));
                        mReserveAdapter.notifyItemRangeChanged(position, mReserveAdapter.getItemCount()-position);

                        if (mReserveAdapter.getCount() == 0)
                            mNoBookInfo.setVisibility(VISIBLE);
                        else
                            mNoBookInfo.setVisibility(INVISIBLE);

                        return true;
                    }
                }

                // get new child pos
                if (keyCode == KEYCODE_DPAD_UP && event.getAction() == ACTION_DOWN)
                    childPos--;
                else if (keyCode == KEYCODE_DPAD_DOWN && event.getAction() == ACTION_DOWN)
                    childPos++;
                if (childPos < 0)
                    childPos = 0;
                else if (childPos >= childCount)
                    childPos = childCount - 1;
                childPosition[0] = childPos;

                return false;
            }
        });


        setImageIconClickListener();    // Johnny 20181228 for mouse control
    }

    private class ReserveInfoAdapter extends RecyclerView.Adapter<ReserveInfoAdapter.ViewHolder> {
        private List<BookInfo> bookList;
        private int selectedPos;
        private Context cont;

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView num;
            TextView TypeCycle;
            TextView bookName;
            TextView bookDate;
            TextView bookTime;

            ViewHolder(View itemView) {
                super(itemView);
                num = (TextView) itemView.findViewById(R.id.numTXV);
                TypeCycle = (TextView) itemView.findViewById(R.id.typeDateTXV);
                bookName = (TextView) itemView.findViewById(R.id.nameTXV);
                bookDate = (TextView) itemView.findViewById(R.id.dateTXV);
                bookTime = (TextView) itemView.findViewById(R.id.timeTXV);
            }
        }

        ReserveInfoAdapter(Context context, List<BookInfo> book) {
            super();
            this.bookList = book;
            cont = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.epg_summary_list, parent, false);
            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            BookInfo bookInfo = bookList.get(position);
            int index = bookInfo.getBookId()+1;
            String num = String.valueOf(position+1);
            String bookDate;
            String bookName;
            String bookTime;
            String typeCycle;
            String[] WeekList = cont.getResources().getStringArray(R.array.STR_ARRAY_WEEK_CYCLE);
            SimpleChannel simpleChannel;
            String bookCycle = String.valueOf(bookInfo.getBookCycle());
            int bookType = bookInfo.getBookType();
            int bookWeek = bookInfo.getWeek();
            long channelID = bookInfo.getChannelId();

            // === Get  Book Cycle  ===
            switch (bookCycle) {
                case "0":
                    bookCycle = cont.getResources().getString(R.string.STR_ONCE);
                    break;
                case "1":
                    bookCycle = cont.getResources().getString(R.string.STR_DAILY);
                    break;
                case "2":
                    bookCycle = cont.getResources().getString(R.string.STR_WEEKLY);
                    break;
                case "3":
                    bookCycle = cont.getResources().getString(R.string.STR_WEEKEND);
                    break;
                case "4":
                    bookCycle = cont.getResources().getString(R.string.STR_WEEKDAY);
                    break;
            }

            // === Get Book  Type ===
            typeCycle = null;
            if (bookType == 0)
                typeCycle = bookCycle + " / " + cont.getResources().getString(R.string.STR_RECORD);
            else if (bookType == 1)
                typeCycle = bookCycle + " / " + cont.getResources().getString(R.string.STR_TURN_ON);

            //  ===  Get  Channel  &  Event  Name ===
            simpleChannel = mDTVActivity.GetSimpleProgramByChannelIdfromTotalChannelList(channelID);
            if (simpleChannel != null)
                bookName = simpleChannel.getChannelName();
            else
                bookName = cont.getResources().getString(R.string.STR_NULL);
            bookName = bookName + " / " + bookInfo.getEventName();

            //  ===  Get  Start  Date ===
            bookDate = null;
            if (bookCycle.equals("Once") || bookCycle.equals("Daily") || bookCycle.equals("Weekly")) {
                // 0 : Once /  1: Daily  / 2: Weekly
                bookDate = String.format(
                        Locale.ENGLISH,
                        "%02d.%02d",
                        bookInfo.getDate(),
                        bookInfo.getMonth()
                );
                if (bookCycle.equals("Weekly"))
                    bookDate = bookDate + " " + WeekList[bookWeek];

            } else if (bookInfo.getBookCycle() == 3) // weekend
                bookDate = WeekList[6] + " & " + WeekList[0];
            else if (bookInfo.getBookCycle() == 4) // weekday
                bookDate = WeekList[1] + " - " + WeekList[5];
            Log.d(TAG, "book Circle = " + bookDate);

            /* ==== book time ==== */
            SimpleDateFormat format;
            String strEndTime;
            int bookStartTime;
            Date endtime;

            bookStartTime = bookInfo.getStartTime();
            endtime = mbookManager.GetEndTime(bookList.get(position));
            format = new SimpleDateFormat("HH : mm", Locale.ENGLISH);
            strEndTime = format.format(endtime);

            bookTime = String.format(Locale.ENGLISH,
                    "%02d : %02d ~ %s",
                    bookStartTime / 100,
                    bookStartTime % 100,
                    strEndTime
            );

            holder.num.setText(num);
            holder.TypeCycle.setText(typeCycle);
            holder.bookName.setText(bookName);
            holder.bookDate.setText(bookDate);
            holder.bookTime.setText(bookTime);
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        holder.bookName.setSelected(true);
                        selectedPos = holder.getAdapterPosition();
                        holder.num.setTextColor(BLACK);
                        holder.TypeCycle.setTextColor(BLACK);
                        holder.bookName.setTextColor(BLACK);
                        holder.bookDate.setTextColor(BLACK);
                        holder.bookTime.setTextColor(BLACK);
                    } else {
                        holder.bookName.setSelected(false);
                        holder.num.setTextColor(WHITE);
                        holder.TypeCycle.setTextColor(WHITE);
                        holder.bookName.setTextColor(WHITE);
                        holder.bookDate.setTextColor(WHITE);
                        holder.bookTime.setTextColor(WHITE);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return bookList.size();
        }

        public long getItemId(int position) {
            return position;
        }

        int getCount() {
            return getItemCount();
        }

        int getSelectedPos() {
            /*View view = recyclerView.getFocusedChild();
            LayoutManager layoutManager = recyclerView.getLayoutManager();
            return layoutManager.getPosition(view);*/
            return selectedPos;
        }
    };

    // Johnny 20181228 for mouse control -s
    private void setImageIconClickListener() {
        Window window = mDialog.getWindow();

        window.findViewById(R.id.yellowIGV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

        window.findViewById(R.id.blueIGV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mbookManager.BookList.size() > 0) {
                    int position = mReserveAdapter.getSelectedPos();
                    int total = mbookManager.BookList.size();

                    mbookManager.DelBookInfo(mbookManager.BookList.get(position));
                    mbookManager.Save();
                    mReserveAdapter.notifyItemRemoved(position);
                    if(position == total-1)
                        position--;
                    Log.d(TAG, "onKey:  position = " + position + "     count = "+(mReserveAdapter.getItemCount()-position));
                    mReserveAdapter.notifyItemRangeChanged(position, mReserveAdapter.getItemCount()-position);

                    if (mReserveAdapter.getCount() == 0)
                        mNoBookInfo.setVisibility(VISIBLE);
                    else
                        mNoBookInfo.setVisibility(INVISIBLE);

                }
            }
        });
    }
    // Johnny 20181228 for mouse control -e
}