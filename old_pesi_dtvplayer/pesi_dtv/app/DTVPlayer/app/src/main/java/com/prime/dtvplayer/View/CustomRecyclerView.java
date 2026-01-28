package com.prime.dtvplayer.View;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.prime.dtvplayer.R;

/*
  Created by edwin_weng on 2018/1/30.
 */

public class CustomRecyclerView extends RecyclerView {
    private final String TAG = "CustomRecyclerView";

    // initialize
    private Context context;
    private int visibleCount = 5;
    private int itemHeight = 66;
    private boolean isInit = false;
    private boolean isSmoothScroll = false;
    private boolean isWindowFocusChanged = false;
    private ItemAnimator itemAnimator;

    // switch recycler view
    private static int rightId = -1;
    private static int leftId = -1;
    private static int revCount = 0;
    private static int blockCount = 0;
    private static boolean atLeft = true;

    // remove item
    private int orgItemCount = -1;

    // scroll to bottom
    private int oldItemCount = 0;
    private int setLayoutCount = 0;
    private boolean isShowNew = false;
    private boolean setLayoutFinished = false;

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //private static int selection;
    @Override
    public View focusSearch(View focused, int direction) {
        final int lastPos = getItemCount()-1;
        View lastView = findViewByPosition(lastPos);
        View firstView = findViewByPosition(0);
        final RecyclerView rvRight = (RecyclerView) getRootView().findViewById(rightId);
        RecyclerView rvLeft = (RecyclerView) getRootView().findViewById(leftId);
        boolean isLastView = lastView == focused;
        boolean isFirstView = firstView == focused;
        boolean onlyOneRecyclerView = rightId == -1;
        atLeft = getId() == leftId;
        boolean rvRightNotNull
                = (atLeft
                && (rvRight != null)
                && (rvRight.getAdapter() != null)
                && (rvRight.getAdapter().getItemCount() != 0));
        boolean rvLeftNotNull
                = (!atLeft
                && (rvLeft != null)
                && (rvLeft.getAdapter() != null)
                && (rvLeft.getAdapter().getItemCount() != 0));

        switch (direction) {
            case FOCUS_RIGHT:
                Log.d(TAG, "focusSearch: ==== ==== RIGHT ==== ====");
                if (onlyOneRecyclerView)
                {
                    // only 1 recycler view, should not block
                    Log.d(TAG, "focusSearch: only 1 recycler view, should not block");
                    return super.focusSearch(focused, direction);
                }

                Log.d(TAG, atLeft ? "focusSearch: from left" : "focusSearch: from right");

                if (rvRightNotNull)
                {
                    rvRight.setFocusable(true);
                    rvRight.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                    Log.d(TAG, "focusSearch: unblock id = " + rightId);

                    if (rvLeft != null) {
                        rvLeft.setFocusable(true);
                        rvLeft.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                        Log.d(TAG, "focusSearch: block id = " + leftId);
                    }
                    atLeft = false;
                }
                break;
            case FOCUS_LEFT:
                Log.d(TAG, "focusSearch:  ==== ==== LEFT ==== ====");
                if (onlyOneRecyclerView)
                {
                    // only 1 recycler view, should not block
                    Log.d(TAG, "focusSearch: only 1 recycler view, should not block");
                    return super.focusSearch(focused, direction);
                }

                Log.d(TAG, atLeft ? "focusSearch: from left" : "focusSearch: from right");

                if (rvLeftNotNull)
                {
                    rvLeft.setFocusable(true);
                    rvLeft.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                    Log.d(TAG, "focusSearch: unblock id = " + leftId);

                    if (rvRight != null) {
                        rvRight.setFocusable(false);
                        rvRight.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                        Log.d(TAG, "focusSearch: block id = " + rightId);
                    }
                    atLeft = true;
                }
                break;
            case FOCUS_UP:
                Log.d(TAG, "focusSearch: ==== ==== UP ==== ====");
                if (isFirstView) {
                    Log.d(TAG, "focusSearch: go to bottom item");
                    if (visibleCount == 1) {
                        // this is a spacial case that prevent blink when switch item
                        setBackgroundResource(R.drawable.focus);
                    }
                    //smoothScrollBy(0, (itemHeight*5));
                    scrollToPosition(lastPos);
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (visibleCount < getItemCount()) {
                                // the recycler view's height is small & need scroll
                                // , so use "visibleCount"
                                getChildAt(visibleCount - 1).requestFocus();
                            }
                            else {
                                // the recycler view's height is larger than all item
                                // , so use "getItemCount()"
                                getChildAt(getItemCount() - 1).requestFocus();
                            }
                            setBackgroundResource(android.R.color.transparent);
                        }
                    }, 0);
                }
                else {
                    Log.d(TAG, "focusSearch: getTopPos = "+getTopPos());
                }
                break;
            case FOCUS_DOWN:
                Log.d(TAG, "focusSearch: ==== ==== DOWN ==== ====");
                if (isLastView) {
                    Log.d(TAG, "focusSearch: go to top item");
                    if (visibleCount == 1)
                        setBackgroundResource(R.drawable.focus);

                    //scrollBy(0, -(itemHeight*getItemCount()));
                    scrollToPosition(0);
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getChildAt(0).requestFocus();
                            setBackgroundResource(android.R.color.transparent);
                        }
                    }, 0);
                }
                break;
        }
        return super.focusSearch(focused, direction);
    }

    @Override
    public void smoothScrollBy(int dx, int dy) {
        //Log.d(TAG, "smoothScrollBy: ");
        if (isSmoothScroll)
        {
            super.scrollBy(dx, dy);
            Log.d(TAG, "smoothScrollBy: ");
        }
        else
        {
            super.scrollBy(dx, dy);
            Log.d(TAG, "scrollBy: ");
        }
    }

    @Override
    public void smoothScrollToPosition(int position) {
        if (isSmoothScroll) {
            super.smoothScrollToPosition(position);
            Log.d(TAG, "smoothScrollToPosition: ");
        }
        else {
            super.scrollToPosition(position);
            Log.d(TAG, "scrollToPosition: ");
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        isWindowFocusChanged = true;
        ((LinearLayout) getParent()).setFocusable(false);

        if (hasWindowFocus)
        {
            Log.d(TAG, "onWindowFocusChanged: ==== ==== BLOCK EMPTY ==== ====");
            if (getItemCount() == 0) {
                Log.d(TAG, "onWindowFocusChanged: recycler view's item = " + getItemCount() + ", block");
                setFocusable(true);
                setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                blockCount++;
            } else {
                Log.d(TAG, "onWindowFocusChanged: recycler view's item = " + getItemCount() + ", unblock");
                setFocusable(true);
                setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
            }

            if (!isInit) {
                Log.d(TAG, "onWindowFocusChanged: Initialize");
                //throw new RuntimeException("PLEASE INIT by CustomRecyclerView.Initialize()");
                Initialize(getContext(), itemHeight, visibleCount, isSmoothScroll, isShowNew);
            }
        }
        else
        {
            Log.d(TAG, "onWindowFocusChanged: ==== ==== RESET ==== ====");
            blockCount = 0;
            revCount = 0;
            rightId = -1;
            leftId = -1;
            atLeft = true;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow: ");
    }

    @Override
    public void onChildDetachedFromWindow(View child)
    {
        //Log.d(TAG, "onChildDetachedFromWindow: child = "+getChildLayoutPosition(child));
        super.onChildDetachedFromWindow(child);

        RecyclerView rvLeft = (RecyclerView) getRootView().findViewById(leftId);
        RecyclerView rvRight = (RecyclerView) getRootView().findViewById(rightId);
        boolean hasRemoveItem = (orgItemCount > getItemCount());
        boolean hasTwoRecyclerView = revCount == 2;
        boolean isMoveToLeft = (getId() == rightId)
                && (rvLeft != null)
                && (rvLeft.getAdapter() != null)
                && (rvLeft.getAdapter().getItemCount() != 0)
                && (getItemCount() == 0);
        boolean isMoveToRight = (getId() == leftId)
                && (rvRight != null)
                && (rvRight.getAdapter() != null)
                && (rvRight.getAdapter().getItemCount() != 0)
                && (getItemCount() == 0);
        int deletePos = getChildLayoutPosition(child) - getTopPos();

        if (hasRemoveItem && isWindowFocusChanged)
        {
            // remove 1 item
            Log.d(TAG, "onChildDetachedFromWindow: REMOVE");
            oldItemCount--;
            Log.d(TAG, "onChildDetachedFromWindow:" +
                    "\n deletePos = "+deletePos+
                    "\n getChildLayoutPosition(child) = "+getChildLayoutPosition(child)+
                    "\n getTopPos = "+getTopPos()+
                    "\n getBottomPos = "+getBottomPos()+
                    "\n getItemCount-1 = "+(getItemCount()-1)
            );
            if (getItemCount() != 0) {
                Log.d(TAG, "onChildDetachedFromWindow: getItemCount() = " + getItemCount());

                if (findViewByPosition(getChildLayoutPosition(child)) == null) {
                    Log.d(TAG, "onChildDetachedFromWindow: null");
                    getChildAt(0).requestFocus();
                }
                else if (getBottomPos() == (getItemCount() - 1)) {
                    Log.d(TAG, "onChildDetachedFromWindow: last");
                    findViewByPosition(getChildLayoutPosition(child)).requestFocus();
                }
                else if (getTopPos() == 0 && getChildLayoutPosition(child) == -1) {
                    Log.d(TAG, "onChildDetachedFromWindow: first");
                    findViewByPosition(getChildLayoutPosition(child)).requestFocus();
                }
                else {
                    Log.d(TAG, "onChildDetachedFromWindow: else");
                }
            }
        }

        orgItemCount = getItemCount();

        if (getItemCount() == 0 && hasTwoRecyclerView)
        {
            Log.d(TAG, "onChildDetachedFromWindow:"
                    +" move to another RecyclerView ? "+ (isMoveToLeft || isMoveToRight)
                    +"\n orgItemCount = "+ orgItemCount
                    +"\n getItemCount = "+ getItemCount()
            );

            if (isMoveToLeft)
            {
                Log.d(TAG, "onChildDetachedFromWindow: go to left");

                rvLeft.setFocusable(true);
                rvLeft.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                setFocusable(false);
                setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                atLeft = true;
            }
            else if (isMoveToRight)
            {
                Log.d(TAG, "onChildDetachedFromWindow: go to right");

                rvRight.setFocusable(true);
                rvRight.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                setFocusable(false);
                setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                atLeft = false;
            }
        }
    }

    // AUTO SCROLL WHEN ADD ITEM
    @Override
    public boolean hasFocus() {
        //Log.d(TAG, "hasFocus: ");
        final boolean hasFocus = super.hasFocus();
        boolean isAddItem = (oldItemCount < getItemCount())
                && isWindowFocusChanged
                && setLayoutFinished
                && isInit
                && isShowNew;

        /*Log.d(TAG, "hasFocus: "+hasFocus
                +"\n id = "+(getId())
                //+"\n addItem = "+addItem
                +"\n isWindowFocusChanged = "+isWindowFocusChanged
                +"\n setLayoutFinished = "+setLayoutFinished
                +"\n oldItemCount = "+oldItemCount
                +"\n getItemCount() = "+getItemCount()
                +"\n setLayoutCount = "+setLayoutCount
                +"\n isInit = "+isInit
        );*/

        // ================ Add item ================
        if (isAddItem)
        {
            // ================ scroll to bottom ================
            Log.d(TAG, "hasFocus: ==== ==== ADD ITEM ==== ====");
            Log.d(TAG, "hasFocus: oldItemCount = "+oldItemCount);
            oldItemCount = getItemCount();

            post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: scroll to bottom, hasFocus = "+hasFocus);
                    scrollBy(0, (itemHeight * oldItemCount));
                    boolean noFocusItem = getFocusedChild() == null;
                    RecyclerView rvLeft = (RecyclerView) getRootView().findViewById(leftId);
                    RecyclerView rvRight = (RecyclerView) getRootView().findViewById(rightId);

                    if (noFocusItem && hasFocus)
                    {
                        Log.d(TAG, "run: focus child item");
                        getChildAt(0).requestFocus();
                    }
                    if (blockCount == 1 && revCount == 1)
                    {
                        Log.d(TAG, "run: unblock 1 of 1 & focus item");
                        setFocusable(true);
                        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                        requestFocus();
                        getChildAt(0).requestFocus();
                        blockCount = 0;
                    }
                    if (blockCount == 2 && revCount == 2)
                    {
                        Log.d(TAG, "run: unblock 1 of 2 & focus item");
                        setFocusable(true);
                        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                        requestFocus();
                        getChildAt(0).requestFocus();

                        if (getId() != leftId && rvLeft != null) {
                            rvLeft.setFocusable(false);
                            rvLeft.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                        }
                        else if (getId() != rightId && rvRight != null) {
                            rvRight.setFocusable(false);
                            rvRight.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                        }
                        blockCount = 0;
                    }
                }
            });
        }

        if (setLayoutCount != 2)
        {
            setLayoutCount++;
            setLayoutFinished = (setLayoutCount == 2);
            //Log.d(TAG, "hasFocus: setLayoutFinished = "+setLayoutFinished);
        }

        return hasFocus;
    }

    private void Initialize(Context context
            , int itemHeight            // one item's height is "itemHeight"
            , int visibleCount          // how many items are visible in screen
            , boolean smoothScroll      // true(ON) false(OFF)
            , boolean isShowNew         // add & show new item
    )
    {
        Log.d(TAG, "Initialize: ");
        int listViewHeight = getDimension(R.dimen.LIST_VIEW_HEIGHT);

        if (revCount == 0)
        {
            leftId = getId();
            rightId = -1;
            revCount++;
        }
        else if (revCount == 1)
        {
            if (getId() == leftId) {
                rightId = -1;
            }
            else {
                rightId = getId();
            }
            revCount++;
        }

        isInit = true;
        this.context = context;
        this.itemHeight = itemHeight;
        this.visibleCount = (visibleCount < 1)
                ? 1             // min visible count
                : visibleCount; // new visible count

        itemAnimator = getItemAnimator();
        setSpacialEffects(false, false, false, false, smoothScroll);
        setItemAnimator(null);

        oldItemCount = getItemCount();
        orgItemCount = getItemCount();
        post( new SetHeight(
                (this.itemHeight < listViewHeight)
                ? listViewHeight    // min item height
                : itemHeight        // new item height
        ));

        setShowNew(isShowNew);
    }

    public void setShowNew(boolean enable) {
        isShowNew = enable;
    }

    public void setSelection(int selectedPos) {
        Log.d(TAG, "setSelection: find item view to request");

        View selectedView;
        if (getLayoutManager() != null)
            selectedView = getLayoutManager().findViewByPosition(selectedPos);
        else
            throw new RuntimeException("\n" +
                    "\n ==== Custom RecyclerView ====" +
                    "\n 1. please setSelection() in other function " +
                    "\n 2. holder.itemView.requestFocus() with same position in onBindViewHolder()");

        if (selectedView == null)
        {
            scrollToPosition(selectedPos);
            if (selectedPos > getChildLayoutPosition(getFocusedChild())) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: getChildAt(getChildCount() - 1) = "+getChildAt(getChildCount() - 1));
                        getChildAt(getChildCount() - 1).requestFocus();
                    }
                });
            }
            else {
                post(new Runnable() {
                    @Override
                    public void run() {
                        getChildAt(0).requestFocus();
                    }
                });
            }
        }
        else
            selectedView.requestFocus();
    }

    private void setSpacialEffects(boolean change, boolean add, boolean move, boolean remove, boolean scroll) {
        Log.d(TAG, "setSpacialEffects: ");
        boolean hasNoAnimator = !change && !add && !move && !remove;

        this.isSmoothScroll = scroll;

        /*itemAnimator.setChangeDuration(
                change ? itemAnimator.getChangeDuration() : 0);
        itemAnimator.setAddDuration(
                add ? itemAnimator.getAddDuration() : 0);
        itemAnimator.setMoveDuration(
                move ? itemAnimator.getMoveDuration() : 0);
        itemAnimator.setRemoveDuration(
                remove ? itemAnimator.getRemoveDuration() : 0);*/

        setItemAnimator(hasNoAnimator ? null : itemAnimator);
    }

    /*private View searchView(int selectedPos) {
        Log.d(TAG, "findView: "
                +" selectedPos = "+selectedPos
                +"\n selected pos < 0 ? "+(selectedPos < 0)
                +"\n selected pos out of bound ? "+(selectedPos > (getLayoutManager().getItemCount()-1))
        );
        View selectedView = null;
        boolean indexOutOfBound
                = (selectedPos > (getItemCount()-1)) || (selectedPos < 0);

        if (indexOutOfBound) {
            Log.e(TAG, "setSelection: index out of bound");
            throw new RuntimeException("index out of bound");
        }
        if (selectedPos < getTopPos()) { // find the view before current one
            while (selectedView == null) {
                //scrollBy(0, -itemHeight);
                scrollToPosition(selectedPos);
                selectedView = getLayoutManager().findViewByPosition(selectedPos);
            }
            return selectedView;
        }
        if (selectedPos > getBottomPos()) { // find the view after current one
            while (selectedView == null) {
                //scrollBy(0, itemHeight);
                scrollToPosition(selectedPos);
                selectedView = getLayoutManager().findViewByPosition(selectedPos);
            }
            return selectedView;
        }
        return null;
    }*/

    private View findViewByPosition(int pos) {
        //Log.d(TAG, "findViewByPosition: ");
        return getLayoutManager().findViewByPosition(pos);
    }

    /*private void setContext(Context context) {
        //Log.d(TAG, "setContext: ");
        this.context = context;
    }*/

    public void setItemHeight(int itemHeight) {
        //Log.d(TAG, "setItemHeight: ");
        this.itemHeight = itemHeight;
    }

    public void setVisibleCount(int visibleCount) {
        //Log.d(TAG, "setVisibleCount: ");
        this.visibleCount = visibleCount;
    }

    private void setSmoothScroll(boolean enable) {
        //Log.d(TAG, "setSmoothScroll: ");
        this.isSmoothScroll = enable;
    }

    private int getItemCount() {
        //Log.d(TAG, "getItemCount: ");
        if (getAdapter() == null)
            return -1;
        return getAdapter().getItemCount();
    }

    /*private int getItemHeight() {
        //Log.d(TAG, "getItemHeight: ");
        return itemHeight;
    }*/

    public int getSelection() {
        //Log.d(TAG, "getFocusedPos: ");
        View view = getFocusedChild();
        return getChildLayoutPosition(view);
    }

    private int getTopPos() {
        //Log.d(TAG, "getTopPos: ");
        View view = getChildAt(0);
        return getChildLayoutPosition(view);
    }

    private int getBottomPos() {
        //Log.d(TAG, "getBottomPos: ");
        int itemCount = getChildCount();
        View view = getChildAt(itemCount - 1);
        return getChildLayoutPosition(view);
    }

    private int getDimension(int ResId) {
        return (int) getResources().getDimension(ResId);
    }

    private class SetHeight implements Runnable {
        int newItemHeight;

        SetHeight(int newItemHeight) {
            this.newItemHeight = newItemHeight;
            itemHeight = newItemHeight;
        }

        @Override
        public void run() {
            Log.d(TAG, "run: set layout's new height");
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    getWidth()
                    , (visibleCount * newItemHeight)
            );
            setLayoutParams(layoutParams);
            setLayoutManager(new LinearLayoutManager(context, VERTICAL, false));
            setLayoutFinished = false;
            setLayoutCount = 0;
        }
    }

}