package com.prime.launcher.PVR.Management;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

public class RecordListView extends RecyclerView {

    private String TAG = getClass().getSimpleName();

    private static final int SCROLL_DURATION         = 100;
    private static final int ITEM_VIEW_CACHE_SIZE    = 1000;

    private WeakReference<AppCompatActivity> g_ref;
    private int g_previous;

    public RecordListView(@NonNull Context context) {
        super(context);
    }

    public RecordListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setup_view(AppCompatActivity activity) {
        Log.d(TAG, "setup_view: ");
        g_ref = new WeakReference<>(activity);
        setLayoutManager(new LinearLayoutManager(get(), LinearLayoutManager.VERTICAL, false));
        setAdapter(new RecordListAdapter(get()));
        setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == keyCode && is_series_list()) {
            Log.d(TAG, "onKeyDown: press KEYCODE_BACK");
            RecordListAdapter adapter = (RecordListAdapter) getAdapter();
            assert adapter != null;
            adapter.on_press_back(-1);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public boolean is_series_list() {
        RecordListAdapter adapter = (RecordListAdapter) getAdapter();
        return adapter != null && adapter.is_series_list();
    }

    public void set_previous(int position) {
        g_previous = position;
    }

    private RecordProgramsActivity get() {
        return (RecordProgramsActivity) g_ref.get();
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

    public int get_master_index() {
        RecordListAdapter adapter = (RecordListAdapter) getAdapter();
        if (adapter != null && adapter.is_series_list())
            return adapter.get_focus_position();
        else
            return get_position();
    }

    public int get_series_index() {
        RecordListAdapter adapter = (RecordListAdapter) getAdapter();
        if (adapter != null && adapter.is_series_list())
            return get_position();
        else
            return 0xFFFF;
    }
}
