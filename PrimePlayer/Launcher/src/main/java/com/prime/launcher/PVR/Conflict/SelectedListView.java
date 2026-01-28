package com.prime.launcher.PVR.Conflict;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.datastructure.sysdata.BookInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class SelectedListView extends RecyclerView {
    private static final String TAG = SelectedListView.class.getSimpleName();

    private static final int SCROLL_DURATION         = 100;
    private static final int ITEM_VIEW_CACHE_SIZE    = 1000;

    private WeakReference<Context> Ref;

    public SelectedListView(@NonNull Context context) {
        super(context);
    }

    public SelectedListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectedListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setup_view(Object context, BookInfo thirdBook, List<BookInfo> selectedList) {
        if (context instanceof ReplaceConflictDialog dialog) {
            Log.d(TAG, "setup_view: ");
            Ref = new WeakReference<>(dialog.getContext());
            setLayoutManager(new LinearLayoutManager(get(), LinearLayoutManager.VERTICAL, false));
            setAdapter(new SelectedListAdapter(dialog, thirdBook, selectedList));
            setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
        }
    }

    private Context get() {
        return Ref.get();
    }

    public SelectedListAdapter getAdapter() {
        return (SelectedListAdapter) super.getAdapter();
    }

    public int get_item_height() {
        if (!hasFocus())
            return 0;
        View focusedView = getFocusedChild();
        if (null == focusedView)
            return 0;
        return focusedView.getHeight();
    }

    public int get_position() {
        if (!hasFocus())
            return 0;
        View focusedView = getFocusedChild();
        if (null == focusedView)
            return 0;
        return getChildAdapterPosition(focusedView);
    }

    public int get_count() {
        if (!hasFocus())
            return 0;
        if (getAdapter() == null)
            return 0;
        return getAdapter().getItemCount();
    }
}
