package com.prime.dtvplayer.View;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.dolphin.dtv.PvrFileInfo;
import com.prime.dtvplayer.R;

import java.util.List;

///**
// * Created by edwin_weng on 2018/4/27.
// */

public class PvrRecyclerView extends RecyclerView {

    private static final String TAG = "PvrRecyclerView";
    private Context cont;
    private float itemHeight;
    private float totalHeight;
    private int curPos = 0;

    public PvrRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        cont = context;

        Log.d(TAG, "PvrRecyclerView: ");
    }

    public void remove( List<PvrFileInfo> list, int pos) {

        Log.d(TAG, "remove: ");

        list.remove(pos);
        getAdapter().notifyItemRemoved(pos);
    }

    @Override
    public void setAdapter(Adapter adapter) {

        Log.d(TAG, "setAdapter: curPos = "+curPos);

        super.setAdapter(adapter);
        //adapter.setHasStableIds(true);
        //totalCount = adapter.getItemCount();
        setLayoutManager(new LinearLayoutManager(cont, VERTICAL, false));
        setItemAnimator(null);
        //setCount(5);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        requestFocus();
        //setSelection(curPos);
    }

    public int getCurPos() {

        Log.d(TAG, "getCurPos: ");

        if (curPos < 0)                 curPos = 0;
        else if (curPos >= getItemCount())   curPos = getItemCount() - 1;
        return curPos;
    }

    public int getSamePos()
    {
        Log.d(TAG, "getSamePos: ");

        return getCurPos() - getChildAdapterPosition(getChildAt(0));
    }

    public int getItemCount() {

        return getAdapter().getItemCount();
    }

    public void setListNum(final int count) {

        Log.d(TAG, "setListNum: count = "+count);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                itemHeight = (int) getResources().getDimension( R.dimen.PVR_LIST_ITEM_HEIGHT);
                totalHeight = itemHeight * count;
                getLayoutParams().height = (int) totalHeight;
                setLayoutParams(getLayoutParams());
                setVisibility(VISIBLE);
            }
        }, 100);
    }

    //    public boolean focusView() {
//
//        Log.d(TAG, "focusView: ");
//
//        if (getFocusedChild() != null)
//            getFocusedChild().requestFocus();
//        else {
//            setSelection(curPos);
//        }
//        return true;
//    }

    // Don't Use setSelection() in OnCreate()
    public void setSelection(final int selectedPos) {

        Log.d(TAG, "setSelection: Don't Use setSelection() in OnCreate()");
//        Log.d(TAG, "setSelection: selectedPos = "+selectedPos);
        final View selectedView;

        if (getLayoutManager() != null) {
            selectedView = getLayoutManager().findViewByPosition(selectedPos);
            this.curPos = selectedPos;
        }
        else {
            throw new RuntimeException("RecyclerView.getLayoutManager() == null");
        }

        if (selectedView == null)
        {
            scrollToPosition(selectedPos);

            post(new Runnable()
            {
                @Override
                public void run()
                {
                    View selectedView = getLayoutManager().findViewByPosition(selectedPos);

                    if (selectedView == null) {
                        Log.d(TAG, "setSelection: post(this)");
                        post(this);
                    } else {
                        Log.d(TAG, "setSelection: selectedView.requestFocus()");
                        selectedView.requestFocus();
                    }
                }
            });
        }
        else
        {
            selectedView.requestFocus();
//            postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    selectedView.requestFocus();
//                }
//            }, 0);
        }
    }

    public boolean moveUp() {

        Log.d(TAG, "moveUp: ");

        int curPos = getChildAdapterPosition(getFocusedChild());
        int lastPos = getAdapter().getItemCount() - 1;

        this.curPos = (curPos == 0)
                ? lastPos
                : curPos - 1;

        if (this.curPos == lastPos)
        {
            setSelection(this.curPos);
            return true;
        }

        return false;
    }

    public boolean moveDown() {

        Log.d(TAG, "moveDown: ");

        int curPos = getChildAdapterPosition(getFocusedChild());

        this.curPos = (curPos == getAdapter().getItemCount()-1)
                ? 0
                : curPos + 1;

        if (this.curPos == 0)
        {
            setSelection(this.curPos);
            return true;
        }

        return false;
    }

    public void pagePrev()
    {
        Log.d(TAG, "pagePrev: ");

        int samePos = getSamePos(); // this line must do before scrollBy()

        if ( isTopPage() )
        {
            scrollBy(0, (int) totalHeight);
        }
        else
        {
            scrollBy(0, -getLayoutParams().height);
        }

        this.curPos = getChildAdapterPosition(getChildAt(samePos));
        getChildAt(samePos).requestFocus();
    }

    public void pageNext()
    {
        Log.d(TAG, "pageNext: ");

        int samePos = getSamePos(); // this line must do before scrollBy()

        if ( isBottomPage() )
        {
            scrollBy(0, (int) -totalHeight);
        }
        else
        {
            scrollBy(0, getLayoutParams().height);
        }

        this.curPos = getChildAdapterPosition(getChildAt(samePos));
        getChildAt(samePos).requestFocus();
    }

    private void pageUp_v1()
    {
        Log.d(TAG, "pageUp: ");

        if ( isTopPage() )
        {
            setSelection(getItemCount()-1);
        }
        else
        {
            int samePos = getSamePos(); // this line must do before scrollBy()

            scrollBy(0, -getLayoutParams().height);

            if ( getCurPos() < getChildCount() )
            {
                this.curPos = getChildAdapterPosition(getChildAt(0));
                getChildAt(0).requestFocus();
            }
            else
            {
                this.curPos = getChildAdapterPosition(getChildAt(samePos));
                getChildAt(samePos).requestFocus();
            }
        }
    }

    private void pageDown_v2()
    {
        Log.d(TAG, "pageDown: ");

        if ( isBottomPage() )
        {
            setSelection(0);
        }
        else
        {
            int samePos = getSamePos(); // this line must do before scrollBy()

            scrollBy(0, getLayoutParams().height);

            int lastPos = getItemCount() - getChildCount() - 1;
            if ( getCurPos() > lastPos )
            {
                this.curPos = getChildAdapterPosition(getChildAt(getChildCount()-1));
                getChildAt(getChildCount()-1).requestFocus();
            }
            else
            {
                this.curPos = getChildAdapterPosition(getChildAt(samePos));
                getChildAt(samePos).requestFocus();
            }
        }
    }

    public boolean isTopPage()
    {
        Log.d(TAG, "isTopPage: ");
        return getChildAdapterPosition(getChildAt(0)) == 0;
    }

    public boolean isBottomPage()
    {
        Log.d(TAG, "isBottomPage: ");
        return getChildAdapterPosition(getChildAt(getChildCount()-1)) == (getItemCount()-1);
    }

    // edwin 20180626
    // RecyclerView do smoothScroll by default
    // so we must override smoothScrollBy() by scrollBy()
    @Override
    public void smoothScrollBy(int dx, int dy) {
        super.scrollBy(dx, dy);
    }
}
