package com.prime.dtvplayer.View.guide;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.Activity.DimensionEPG;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProgramRow extends TimelineGridView {
    private final String TAG = getClass().getSimpleName();

    private DimensionEPG mdimensionEpg=null;
    private DTVActivity.EpgUiDisplay mepgUiDisplay;
    private int channelIndex = 0;
    private int displayNum = 0;
    private static final long ONE_HOUR_MILLIS = TimeUnit.HOURS.toMillis(1);
    private static final long HALF_HOUR_MILLIS = ONE_HOUR_MILLIS / 2;

    public void resetScroll(int scrollOffset, List<EPGEvent> eventList )
    {
        int position = -1 ;
        long TimeRangeStart = mdimensionEpg.getTimeRangeStart();

        int low = 0, mid;
        int high = eventList.size()-1;

        while( low <= high ) {
            mid = (low + high) / 2;
            if (eventList.get(mid).getStartTime() <= TimeRangeStart && eventList.get(mid).getEndTime() >= TimeRangeStart)
            {
                position = mid;
                break;
            }
            else if(eventList.get(mid).getStartTime() > TimeRangeStart)
                high = mid - 1 ;
            else if (eventList.get(mid).getStartTime() <  TimeRangeStart)
                low = mid + 1 ;
        }

        if (position < 0 || position >= eventList.size()) {
            Log.d(TAG, "resetScroll: Not Find Event !!!");
            getLayoutManager().scrollToPosition(0);
        }
        else
        {
            int offset = GuideUtils.convertMillisToPixel(
                    mdimensionEpg.getStartTime(), eventList.get(position).getStartTime()) - scrollOffset;
            ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(position, offset);
        }
    }

    public ProgramRow(Context context) {
        this(context, null);
    }

    public ProgramRow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgramRow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean isDirectionStart(int direction) {
        return getLayoutDirection() == LAYOUT_DIRECTION_LTR
                ? direction == View.FOCUS_LEFT : direction == View.FOCUS_RIGHT;
    }

    private boolean isDirectionEnd(int direction) {
        return getLayoutDirection() == LAYOUT_DIRECTION_LTR
                ? direction == View.FOCUS_RIGHT : direction == View.FOCUS_LEFT;
    }

    @Override
    public View focusSearch(View focused, int direction)
    {
        if(focused == null )
            return null;
        View target = super.focusSearch(focused, direction); // add protect , avoid crash
        if(target == null || !(target instanceof ProgramItemView)) {
            target = focused;
        }
        EPGEvent event = ((ProgramItemView)focused).getItemEvent();
        EPGEvent TartgetEvent = ((ProgramItemView)target).getItemEvent();
        long timeRangeStart = mdimensionEpg.getTimeRangeStart();
        long timeRangeEnd = mdimensionEpg.getTimeRangeEnd();

        if( event == null || TartgetEvent == null )// add protect , avoid crash
            return focused;

        if (isDirectionStart(direction) || direction == View.FOCUS_BACKWARD)
        {
            //Log.d(TAG, "focusSearch:  Left Key !");
            if(event.getStartTime() <=timeRangeStart) //  Cur  event  not Start in Time Range ! ----> scroll Time Range
            {
                Log.d(TAG, "focusSearch:  Time Range Minus 30 minute!!!!");
                scrollByTime(HALF_HOUR_MILLIS * -1);
                if(event.getStartTime() <= mdimensionEpg.getStartTime())
                {
                    mdimensionEpg.setCurTime(mdimensionEpg.getTimeRangeStart());
                    return focused;
                }
                //formatter= new SimpleDateFormat("yyyy  MMM dd  ... HH : mm");
                //newDateS = new Date(mdimensionEpg.getTimeRangeStart());
                //newDateE = new Date(mdimensionEpg.getTimeRangeEnd());
                //test1 = formatter.format(newDateS);
                //test2 = formatter.format(newDateE);
                //Log.d(TAG, "focusSearch:  New Range = " + test1 + "  ~  " + test2);

                if(TartgetEvent.getEndTime() > mdimensionEpg.getTimeRangeStart()) {
                    if(TartgetEvent.getStartTime() >= mdimensionEpg.getTimeRangeStart()) // pre event start in new range
                        mdimensionEpg.setCurTime(TartgetEvent.getStartTime());
                    else // pre event start before new range!!
                        mdimensionEpg.setCurTime(mdimensionEpg.getTimeRangeStart());
                    return target;
                }
                else { // pre event  start before range !!!
                    mdimensionEpg.setCurTime(mdimensionEpg.getTimeRangeStart());
                    return focused;
                }
            }
            else // Cur  event  start  in  Time Range ---> not need scroll  ---> focus at pre event !!!
            {
                //Log.d(TAG, "focusSearch:  Time Range No Scroll !!!!!!!!!");
                if(TartgetEvent.getStartTime() < mdimensionEpg.getTimeRangeStart())
                    mdimensionEpg.setCurTime(mdimensionEpg.getTimeRangeStart());
                else
                    mdimensionEpg.setCurTime(TartgetEvent.getStartTime());
            }

        }
        else if (isDirectionEnd(direction) || direction == View.FOCUS_FORWARD)
        {
            //Log.d(TAG, "focusSearch:  Right Key !   event Name = " + event.getEventName());
            if(event.getEndTime() >= timeRangeEnd) // Cur event not finish in cur time range ----> Scroll Time range !
            {
                if(timeRangeEnd != mdimensionEpg.getEndTime()) // Time range Not 7 day End   ----> scroll 30 min
                {
                    //Log.d(TAG, "focusSearch:  Time Range Plus 30 minute!!!!");
                    scrollByTime(HALF_HOUR_MILLIS);

                    //formatter= new SimpleDateFormat("yyyy  MMM dd  ... HH : mm");
                    //newDateS = new Date(mdimensionEpg.getTimeRangeStart());
                    //newDateE = new Date(mdimensionEpg.getTimeRangeEnd());
                    //test1 = formatter.format(newDateS);
                    //test2 = formatter.format(newDateE);
                    //Log.d(TAG, "focusSearch:  New Range = " + test1 + "  ~  " + test2);

                    if(event.getEndTime() < mdimensionEpg.getTimeRangeEnd()) { // Next Event Show In New Range
                        mdimensionEpg.setCurTime(TartgetEvent.getStartTime());
                        return target;
                    }
                    else { // Next Event Not Show In New Range
                        if(mdimensionEpg.getCurTime() < mdimensionEpg.getTimeRangeStart())
                            mdimensionEpg.setCurTime(mdimensionEpg.getTimeRangeStart());
                        return focused;
                    }
                }
                else // Time range is 7 Day end !! ---> Back To  Head
                {
                    mdimensionEpg.ResetTimeRange();
                    return focused;
                }
            }
            else// Cur Event Finish In Cur Time Range ---> Focus Next Event
            {
                Log.d(TAG, "focusSearch:  Time Range No Scroll !!!!!!!!!");
                mdimensionEpg.setCurTime(TartgetEvent.getStartTime());
            }
        }

        return target;
    }

    public void setProgramGuide(DimensionEPG dimensionEpg, DTVActivity.EpgUiDisplay epgUiDisplay, int position, int displaynum) {
        mdimensionEpg = dimensionEpg;
        mepgUiDisplay = epgUiDisplay;
        channelIndex = position;
        displayNum = displaynum;
    }

    public int getChannelIndex()
    {
        return channelIndex;
    }
    public int getDisplayNum()
    {
        return displayNum;
    }

    private void scrollByTime(long timeToScroll) {
        mdimensionEpg.shiftTime(timeToScroll);
    }

    private void updateChildVisibleArea() {
        for (int i = 0; i < getChildCount(); ++i) {
            ProgramItemView child = (ProgramItemView) getChildAt(i);
            if (getLeft() < child.getRight() && child.getLeft() < getRight()) {
                child.updateVisibleArea();
            }
        }
    }
    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        ProgramItemView itemView = (ProgramItemView) child;
        if (getLeft() <= itemView.getRight() && itemView.getLeft() <= getRight()) {
            itemView.updateVisibleArea();
        }
    }
    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        updateChildVisibleArea();
    }

    // Johnny 20181219 for mouse control -s
    // block scroll/fling in programRow by mouse
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        switch(ev.getAction())
        {
            case MotionEvent.ACTION_MOVE:
                return true;    // block scroll by mouse
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        return false;   // block fling
    }
    // Johnny 20181219 for mouse control -e
}
