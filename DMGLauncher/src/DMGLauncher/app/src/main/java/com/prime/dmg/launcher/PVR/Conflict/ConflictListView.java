package com.prime.dmg.launcher.PVR.Conflict;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.dtv.PrimeDtv;
import com.prime.dtv.sysdata.BookInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class ConflictListView extends RecyclerView {

    private static final String TAG = ConflictListView.class.getSimpleName();

    private static final int SCROLL_DURATION         = 100;
    private static final int ITEM_VIEW_CACHE_SIZE    = 1000;

    private WeakReference<Context> gRef;
    private int g_previous;

    public ConflictListView(@NonNull Context context) {
        super(context);
    }

    public ConflictListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ConflictListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setup_view(ConflictDialog conflictDialog, List<BookInfo> conflictList) {
        Log.d(TAG, "setup_view: ");
        gRef = new WeakReference<>(conflictDialog.getContext());
        setLayoutManager(new LinearLayoutManager(get(), LinearLayoutManager.VERTICAL, false));
        setAdapter(new ConflictListAdapter(conflictDialog, conflictList));
        setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
    }

    private Context get() {
        return gRef.get();
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

    public int get_previous() {
        return g_previous;
    }

    public int get_count() {
        if (!hasFocus())
            return 0;
        if (getAdapter() == null)
            return 0;
        return getAdapter().getItemCount();
    }
}
