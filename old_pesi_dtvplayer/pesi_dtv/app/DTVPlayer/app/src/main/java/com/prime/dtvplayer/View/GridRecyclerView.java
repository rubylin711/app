package com.prime.dtvplayer.View;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.dolphin.dtv.PvrFileInfo;

import java.util.List;

public class GridRecyclerView extends RecyclerView
{
    private static final String TAG = "GridRecyclerView";
    private static final int ROW = 3;
    private static final int SPAN_COUNT = 5;
    private Context mCont;
    private float itemHeight;
    private float totalHeight;
    private int curPos = 0;
    private int samePos = 0;
    private boolean isMovePage = false;

    public GridRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mCont = context;

        Log.d(TAG, "GridRecyclerView: ");
    }

    @Override
    public void setAdapter(Adapter adapter) {

        Log.d(TAG, "setAdapter: curPos = "+curPos);

        GridLayoutManager layoutManager = new GridLayoutManager( mCont, SPAN_COUNT );

        super.setAdapter(adapter);
        //adapter.setHasStableIds(true);
        //totalCount = adapter.getItemCount();
        setLayoutManager( layoutManager );
        //setItemAnimator(null);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setRow(ROW);
        requestFocus();
        //setSelection(curPos);
    }

    @Override
    public void onChildAttachedToWindow ( View child ) // edwin: focus child after long scrolling
    {
        super.onChildAttachedToWindow( child );

        Log.d( TAG, "onChildAttachedToWindow: " );

        if ( getCurPos() ==  getChildAdapterPosition( child ))
        {
            setDescendantFocusability( FOCUS_AFTER_DESCENDANTS );
            child.requestFocus();
        }
    }

    @Override
    public void onScrollStateChanged ( int state )
    {
        super.onScrollStateChanged( state );

        if ( isMovePage )
        {
            Log.d( TAG, "onScrollStateChanged: isMovePage: state = "+state );
            switch ( state )
            {
                case SCROLL_STATE_IDLE:
                    if ( getChildAt( samePos ) == null )
                    {
                        samePos = getChildCount() - 1;
                    }
                    this.curPos = getChildAdapterPosition( getChildAt( samePos ) );
                    getChildAt( samePos ).requestFocus();
                    isMovePage = false;
                    break;

                case SCROLL_STATE_DRAGGING:
                    break;

                case SCROLL_STATE_SETTLING:
                    samePos = getSamePos();
                    Log.d( TAG, "onScrollStateChanged: samePos = "+samePos );
                    break;
            }
        }

    }

    public void remove( List<PvrFileInfo> list, int pos )
    {
        Log.d( TAG, "remove: " );

        list.remove( pos );
        getAdapter().notifyItemRemoved( pos );
    }

    public int getCurPos ()
    {
        if ( curPos < 0 )
        {
            curPos = 0;
        }
        else if ( curPos >= getItemCount() )
        {
            curPos = getItemCount() - 1;
        }

        return curPos;
    }

    private int getSamePos()
    {
        return getCurPos() - getChildAdapterPosition(getChildAt(0));
    }

    private int getFocusedPosition ()
    {
        return getChildAdapterPosition( getFocusedChild() );
    }

    private int getItemCount ()
    {
        return getAdapter().getItemCount();
    }

    private int getSpanCount ()
    {
        return ( (GridLayoutManager) getLayoutManager() ).getSpanCount();
    }

    private View getViewByPosition(int pos)
    {
        return getLayoutManager().findViewByPosition( pos );
    }

    private void setRow ( final int row )
    {
        Log.d( TAG, "setRow: " );

        post( new Runnable()
        {
            @Override
            public void run ()
            {
                if ( getChildAt( 0 ) == null )
                {
                    return;
                }

                View view = getChildAt( 0 );
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                int margin = lp.topMargin + lp.bottomMargin;
                int totalRow = ( getItemCount() + getSpanCount() - 1 ) / getSpanCount();

                itemHeight = view.getMeasuredHeight() + margin;
                totalHeight = itemHeight * totalRow;
                getLayoutParams().height = (int) ( row * itemHeight );
                setLayoutParams( getLayoutParams() );
                //setSelection(getCurPos());
            }
        } );
    }

    // Don't Use setSelection() in OnCreate()
    public void setSelection ( final int selectedPos )
    {
        Log.d( TAG, "setSelection: " );

        final View selectedView;

        if ( getLayoutManager() != null )
        {
            selectedView = getViewByPosition( selectedPos );
            this.curPos = selectedPos;
        }
        else
        {
            throw new RuntimeException( "RecyclerView.getLayoutManager() == null" );
        }

        setDescendantFocusability( FOCUS_BLOCK_DESCENDANTS );
        scrollToPosition( selectedPos );

        if ( selectedView != null )
        {
            setDescendantFocusability( FOCUS_AFTER_DESCENDANTS );
            selectedView.requestFocus();
            //postDelayed(new Runnable() {
            //    @Override
            //    public void run() {
            //        selectedView.requestFocus();
            //    }
            //}, 0);
        }
    }

    public boolean moveUp () // edwin: return true to block wrong focus because smooth scroll
    {
        Log.d( TAG, "moveUp: " );

        int spanCount = getSpanCount();
        int curPos = getFocusedPosition() - spanCount;
        int row = ( getItemCount() + spanCount - 1 ) / spanCount;
        boolean atTopRow = curPos < 0;

        if ( atTopRow )
        {
            curPos = curPos + row * spanCount;

            if ( curPos > (getItemCount() - 1) )
            {
                this.curPos = getItemCount() - 1;
            }
            else
            {
                this.curPos = curPos;
            }

            setSelection( this.curPos );
            return true;
        }
        //if ( is_at_top_row )
        //{
        //    Log.d( TAG, "move: curPos = "+this.curPos );
        //    return false;
        //}

        this.curPos = curPos;
        setSelection( this.curPos );
        return true;
    }

    public boolean moveDown () // edwin: return true to block wrong focus because smooth scroll
    {
        Log.d( TAG, "moveDown: " );

        int curPos = getFocusedPosition();
        int spanCount = ( (GridLayoutManager) getLayoutManager() ).getSpanCount();
        boolean atBottomRow = curPos >= ( getAdapter().getItemCount() - spanCount );

        if ( atBottomRow )
        {
            this.curPos = curPos % spanCount;
            setSelection( this.curPos );
            return true;
        }
        //if ( is_at_bottom_row )
        //{
        //    Log.d( TAG, "move: curPos = "+this.curPos );
        //    return false;
        //}

        this.curPos = curPos + spanCount;
        setSelection( this.curPos );
        return true;
    }

    public boolean moveRight ()
    {
        Log.d( TAG, "moveRight: " );

        int curPos = getFocusedPosition();
        int spanCount = getSpanCount();
        final boolean condition1 = ( ( ( curPos + 1 ) % spanCount ) == 0 );
        final boolean condition2 = ( curPos == ( getItemCount() - 1 ) );

        // is at right side
        if ( condition1 )
        {
            this.curPos = curPos - spanCount + 1;
            setSelection( this.curPos );
            return true;
        }
        if ( condition2 )
        {
            this.curPos = curPos - (curPos % spanCount);
            setSelection( this.curPos );
            return true;
        }
        //if ( is_at_right_side )
        //{
        //    return false;
        //}

        this.curPos = curPos + 1;
        return false;
    }

    public boolean moveLeft ()
    {
        Log.d( TAG, "moveLeft:" );

        int curPos = getFocusedPosition();
        int spanCount = getSpanCount();
        boolean condition1 = ( ( curPos + spanCount - 1 ) > ( getItemCount() - 1 ) );
        boolean condition2 = ( ( curPos % spanCount ) == 0 );
        condition1 = condition2 && condition1;

        // is at left side
        if ( condition1 )
        {
            this.curPos = getItemCount() - 1;
            setSelection( this.curPos );
            return true;
        }
        if ( condition2 )
        {
            this.curPos = curPos + spanCount - 1;
            setSelection( this.curPos );
            return true;
        }
        //if ( is_at_left_side )
        //{
        //    return false;
        //}
        this.curPos = curPos - 1;
        return false;
    }

    public void moveNextPage()
    {
        int lastPos = getChildAdapterPosition( getChildAt( getChildCount() - 1 ) );
        boolean atBottomPage = lastPos + 1 >= getItemCount();
        isMovePage = true;

        if ( atBottomPage )
        {
            smoothScrollBy( 0, (int) -totalHeight );
            return;
        }
        smoothScrollBy( 0, getLayoutParams().height );
    }

    public void movePrevPage()
    {
        int curPos = getChildAdapterPosition( getChildAt( 0 ) ) - getSpanCount();
        boolean atTopPage = curPos < 0;
        isMovePage = true;

        if ( atTopPage )
        {
            smoothScrollBy( 0, (int) totalHeight );
            return;
        }
        smoothScrollBy( 0, -getLayoutParams().height );
    }

    //private void pageUp_v1()
    //{
    //    Log.d(TAG, "pageUp: ");
    //
    //    if ( isTopPage() )
    //    {
    //        setSelection(getItemCount()-1);
    //    }
    //    else
    //    {
    //        int samePos = getSamePos(); // this line must do before scrollBy()
    //
    //        scrollBy(0, -getLayoutParams().height);
    //
    //        if ( getCurPos() < getChildCount() )
    //        {
    //            this.curPos = getChildAdapterPosition(getChildAt(0));
    //            getChildAt(0).requestFocus();
    //        }
    //        else
    //        {
    //            this.curPos = getChildAdapterPosition(getChildAt(samePos));
    //            getChildAt(samePos).requestFocus();
    //        }
    //    }
    //}
    //
    //private void pageDown_v2()
    //{
    //    Log.d(TAG, "pageDown: ");
    //
    //    if ( isBottomPage() )
    //    {
    //        setSelection(0);
    //    }
    //    else
    //    {
    //        int samePos = getSamePos(); // this line must do before scrollBy()
    //
    //        scrollBy(0, getLayoutParams().height);
    //
    //        int lastPos = getItemCount() - getChildCount() - 1;
    //        if ( getCurPos() > lastPos )
    //        {
    //            this.curPos = getChildAdapterPosition(getChildAt(getChildCount()-1));
    //            getChildAt(getChildCount()-1).requestFocus();
    //        }
    //        else
    //        {
    //            this.curPos = getChildAdapterPosition(getChildAt(samePos));
    //            getChildAt(samePos).requestFocus();
    //        }
    //    }
    //}

    public boolean isTopPage()
    {
        Log.d(TAG, "isTopPage: ");
        return getChildAdapterPosition(getChildAt(0)) == 0;
    }

    public boolean isBottomPage()
    {
        Log.d( TAG, "isBottomPage: "+getFocusedPosition() );

        return getChildAdapterPosition(getChildAt(getChildCount()-1)) == (getItemCount()-1);
    }

    // edwin 20180626
    // RecyclerView do smoothScroll by default
    // so we must override smoothScrollBy() by scrollBy()
    //@Override
    //public void smoothScrollBy(int dx, int dy) {
    //    super.smoothScrollBy(dx, dy);
    //}
}