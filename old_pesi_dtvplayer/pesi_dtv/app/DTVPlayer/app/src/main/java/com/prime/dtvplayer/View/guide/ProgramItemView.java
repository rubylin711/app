package com.prime.dtvplayer.View.guide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DimensionEPG;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.EPGEvent;

@SuppressLint("AppCompatCustomView")
public class ProgramItemView  extends TextView{
    private final String TAG = getClass().getSimpleName();
    //public static int a = 0;
    private static int sVisibleThreshold;
    private static TextAppearanceSpan sProgramTitleStyle;
    private static DimensionEPG mDimensionEPG;

    private EPGEvent mItemEvent=null;
    private int mChannelIndex=0;
    private long mStartTime = 0;
    private long mEndTime = 0;
    private int mTextWidth=0;
    private static int sItemPadding;
    private boolean mPreventParentRelayout;

    public ProgramItemView(Context context) {
        super(context);
    }

    public ProgramItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        OnFocusChangeListener ON_FOCUS_CHANGED = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mDimensionEPG.setChannelIndex(mChannelIndex);
                    mDimensionEPG.updateDetail(mItemEvent);
                }
            }
        };

        // Johnny 20181219 for mouse control -s
        OnClickListener ON_CLICKED = new OnClickListener() {
            @Override
            public void onClick(View view) {
                mDimensionEPG.changeChannel();
            }
        };
        // Johnny 20181219 for mouse control -e

        setOnFocusChangeListener(ON_FOCUS_CHANGED);
        setOnClickListener(ON_CLICKED); // Johnny 20181219 for mouse control
    }

    public ProgramItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ProgramItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setValues(DimensionEPG context, int channelIndex,EPGEvent event, long startTime, long endTime) {
        mDimensionEPG = context;
        mChannelIndex = channelIndex;
        mItemEvent = event;
        mStartTime = startTime;
        mEndTime = endTime;

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = GuideUtils.convertMillisToPixel(mStartTime, mEndTime);
        setLayoutParams(layoutParams);

        //Log.d(TAG, "setValues: layoutParams.width = "+ layoutParams.width);
        //Log.d(TAG, "setValues:   mChannelIndex  =" + mChannelIndex);
        //Log.d(TAG, "setValues: Event Name = " + event.getEventName());

        TextAppearanceSpan titleStyle = sProgramTitleStyle;
        SpannableStringBuilder description = new SpannableStringBuilder();
        if(mItemEvent.getEventId()==0)
            description.append(mDimensionEPG.getString(R.string.STR_NO_INFO_AVAILABLE));
        else
            description.append(mItemEvent.getEventName());
        description.setSpan(titleStyle, 0, description.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        setText(description);
        mTextWidth = getMeasuredWidth();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //Log.d(TAG, "onFinishInflate: ");
        initIfNeeded();
    }


    private void initIfNeeded() {
        //Log.d(TAG, "initIfNeeded: ");
        if (sVisibleThreshold != 0) {
            return;
        }
        Resources res = getContext().getResources();
        sVisibleThreshold = res.getDimensionPixelOffset(
                R.dimen.PROGRAMITEMVIEW_TABLE_ITEM_VISIBLE_THRESHOLD);
        sItemPadding = res.getDimensionPixelOffset(R.dimen.PROGRAMITEMVIEW_TABLE_ITEM_PADDING);
    }

    public EPGEvent getItemEvent() {
        return mItemEvent;
    }
    public int getItemChannel() {
        return mChannelIndex;
    }

    public void updateVisibleArea() {
        View parentView = ((View) getParent());
        if (parentView == null) {
            return;
        }
        if (getLayoutDirection() == LAYOUT_DIRECTION_LTR) {
            layoutVisibleArea(parentView.getLeft() - getLeft(), getRight() - parentView.getRight());
        } else  {
            layoutVisibleArea(getRight() - parentView.getRight(), parentView.getLeft() - getLeft());
        }
    }

    private void layoutVisibleArea(int startOffset, int endOffset) {
        int width = GuideUtils.convertMillisToPixel(mStartTime, mEndTime);
        int startPadding = Math.max(0, startOffset);
        int endPadding = Math.max(0, endOffset);
        int minWidth = Math.min(width, mTextWidth + 2 * sItemPadding);
        if (startPadding > 0 && width - startPadding < minWidth) {
            startPadding = Math.max(0, width - minWidth);
        }
        if (endPadding > 0 && width - endPadding < minWidth) {
            endPadding = Math.max(0, width - minWidth);
        }

        if (startPadding + sItemPadding != getPaddingStart()
                || endPadding + sItemPadding != getPaddingEnd()) {
            setPaddingRelative(startPadding + sItemPadding, 0, endPadding + sItemPadding, 0);
        }
    }

    public void clearValues() {
        setTag(null);
        mItemEvent=null;
    }
}
