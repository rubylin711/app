package com.prime.dmg.launcher.PVR.Conflict;

import static android.view.View.*;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.ProgramInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class ConflictListAdapter extends RecyclerView.Adapter<ConflictListAdapter.Holder> {

    private static final String TAG = ConflictListAdapter.class.getSimpleName();

    private final WeakReference<Context> gRef;
    private final PrimeDtv gPrimeDtv;
    private final ChannelChangeManager gChangeMgr;
    private final ConflictDialog gConflictDialog;
    private final ConflictListView gConflictListView;
    private final List<BookInfo> gConflictList;
    private long g_previousMs;

    public ConflictListAdapter(ConflictDialog conflictDialog, List<BookInfo> conflictList) {
        gRef = new WeakReference<>(conflictDialog.getContext());
        gPrimeDtv = HomeApplication.get_prime_dtv();
        gChangeMgr = ChannelChangeManager.get_instance(get());
        gConflictDialog = conflictDialog;
        gConflictList = conflictList;
        gConflictListView = conflictDialog.findViewById(R.id.ConflictListView);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_conflict_list, parent, false);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        BookInfo bookInfo = gConflictList.get(position);
        String title = "000 NULL";
        boolean is4K = false;
        boolean isRecording = false;// gChangeMgr.is_channel_recording(bookInfo.getChannelId());

        if (bookInfo != null) {
            title = bookInfo.getChannelNum() + " " + bookInfo.getEventName();
            is4K = bookInfo.is4K();
            isRecording = bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD_NOW;
        }

        if (isRecording)
            gConflictDialog.selected_list_add(gConflictList.get(position));

        holder.title.setText(title);
        holder.icon4k.setVisibility(is4K ? VISIBLE : GONE);
        holder.iconRec.setVisibility(isRecording ? VISIBLE : GONE);
        holder.checkboxOn.setVisibility(isRecording ? VISIBLE : GONE);

        on_click_item(holder, position);
        on_focus_item(holder, position);
    }

    void on_click_item(Holder holder, int position) {
        holder.itemView.setOnClickListener(v -> {
            ImageView checkboxOn = holder.checkboxOn;

            if (VISIBLE == checkboxOn.getVisibility())
                gConflictDialog.on_unselect_book(checkboxOn, gConflictList.get(position));
            else
                gConflictDialog.on_select_book(checkboxOn, gConflictList.get(position));
        });
    }

    void on_focus_item(Holder holder, int position) {
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            holder.title.setSelected(hasFocus);
            holder.title.setTextColor(get().getResources().getColor(hasFocus ? R.color.white : R.color.gray, null));
        });
    }

    private Context get() {
        return gRef.get();
    }

    @Override
    public int getItemCount() {
        return gConflictList == null ? 0 : gConflictList.size();
    }

    public long get_time_diff() {
        long timeDiff = System.currentTimeMillis() - g_previousMs;
        g_previousMs = System.currentTimeMillis();
        return timeDiff;
    }

    /** @noinspection ConstantValue*/
    public void scroll_to_middle(View itemView) { // move_to_middle
        final boolean DEBUG_LOG = false;
        int position = gConflictListView.get_position();
        int previous = gConflictListView.get_previous();

        if (DEBUG_LOG)
            Log.e(TAG, "scroll_to_middle: [position] " + position + ", [previous] " + previous);

        if (position == previous)
            return;

        int[] location = new int[2];
        itemView.getLocationOnScreen(location);
        int itemCenterY = location[1] + itemView.getHeight() / 2;
        int screenCenterY = gConflictListView.getHeight() / 2 + dp_to_px(235);
        //int screenCenterX = itemView.getParent().getResources().getDisplayMetrics().widthPixels / 2;
        int offsetY = itemCenterY - screenCenterY;
        long timeDiff = get_time_diff();

        if (DEBUG_LOG)
            Log.e(TAG, "scroll_to_middle: itemCenterY = " + itemCenterY + ", screenCenterY = " + screenCenterY + ", timeDiff = " + timeDiff);

        if (timeDiff < 100)
            gConflictListView.scrollBy(0, offsetY);
        else
            gConflictListView.smoothScrollBy(0, offsetY);

        if (DEBUG_LOG && offsetY < 0)   Log.d(TAG, "scroll_to_middle: move up,   [offset] " + offsetY);
        if (DEBUG_LOG && offsetY > 0)   Log.d(TAG, "scroll_to_middle: move down, [offset] " + offsetY);
    }

    /** @noinspection SameParameterValue*/
    private int dp_to_px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, get().getResources().getDisplayMetrics());
    }

    public static class Holder extends RecyclerView.ViewHolder {
        TextView title;
        TextView confirmBtn;
        ImageView icon4k;
        ImageView iconRec;
        ImageView checkboxOn;

        public Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.Title);
            confirmBtn = itemView.findViewById(R.id.ConfirmButton);
            icon4k = itemView.findViewById(R.id.Show4KIcon);
            iconRec = itemView.findViewById(R.id.RecordIcon);
            checkboxOn = itemView.findViewById(R.id.CheckboxOn);
        }
    }
}
