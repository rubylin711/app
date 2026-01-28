package com.prime.launcher.PVR.Management;

import android.annotation.SuppressLint;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.launcher.R;
import com.prime.datastructure.sysdata.BookInfo;

import java.util.List;

public class BookRecordAdapter extends RecyclerView.Adapter<BookRecordAdapter.ViewHolder> {
    private static final String TAG = "BookRecordAdapter";
    private final List<BookInfo> g_BookInfoList;
    private OnKeyListener g_OnKeyListener;
    private OnItemClickListener g_OnItemClickListener;
    private OnFocusChangeListener g_OnFocusChangeListener;
    private int g_FocusPosition;

    public BookRecordAdapter(List<BookInfo> bookInfoList) {
        g_BookInfoList = bookInfoList;
        g_FocusPosition = 0;
    }

    public BookInfo getFocusedBookInfo() {
        BookInfo bookInfo = null;
        if (g_BookInfoList.size() > g_FocusPosition) {
            bookInfo = g_BookInfoList.get(g_FocusPosition);
        }

        return bookInfo;
    }

    public boolean deleteBookInfo(int position) {
        if (position < 0 || position >= g_BookInfoList.size()) {
            return false;
        }

        boolean success = g_BookInfoList.remove(position) != null;

        if (success) {
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, g_BookInfoList.size());
        }

        return success;
    }

    @SuppressLint("NotifyDataSetChanged")
    public int deleteBookInfos(@NonNull List<Integer> positions) {
        int deleteCount = 0;
        for (int position : positions) {
            if (position >= 0 && position < g_BookInfoList.size()) {
                if (g_BookInfoList.remove(position) != null) {
                    deleteCount++;
                }
            }
        }

        if (deleteCount > 0) {
            notifyDataSetChanged();
        }

        return deleteCount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_epg_book_record_list, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemView);

        itemView.setOnFocusChangeListener((view, hasFocus) -> {
            int position = viewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                if (hasFocus) {
                    ((MiddleFocusRecyclerView) view.getParent()).focus_middle_vertical(view);
                    g_FocusPosition = position;
                }

                if (g_OnFocusChangeListener != null) {
                    g_OnFocusChangeListener.onFocusChange(view, hasFocus, position);
                }
            }
        });

        itemView.setOnClickListener(view -> {
            if (g_OnItemClickListener != null) {
                int position = viewHolder.getBindingAdapterPosition();
                g_OnItemClickListener.onItemClick(view, position);
            }
        });

        itemView.setOnKeyListener((view, keyCode, event) -> {
            if (g_OnKeyListener != null) {
                int position = viewHolder.getBindingAdapterPosition();
                return g_OnKeyListener.onKey(view, keyCode, event, position);
            }

            return false;
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookInfo bookInfo = g_BookInfoList.get(position);
        String eventName = bookInfo.getEventName();
        if (bookInfo.getEpgEventId() == -1) {
            eventName += "(" + holder.itemView.getResources().getString(R.string.dvr_type_time_hint) + ")";
        }

        holder.getEventName().setText(eventName);
        holder.getTimePrefix().setText(holder.itemView.getResources().getString(R.string.dvr_mgr_pre_record_time));
        holder.getTime().setText(bookInfo.get_formatted_start_time("yyyy/MM/dd HH:mm"));

        if (bookInfo.isSeries()) {
            holder.getSeriesIcon().setVisibility(View.VISIBLE);
        } else {
            holder.getSeriesIcon().setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return g_BookInfoList == null ? 0 : g_BookInfoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventName;
        private final TextView timePrefix;
        private final TextView time;
        private final ImageView seriesIcon;

        public ViewHolder(View view) {
            super(view);

            eventName = view.findViewById(R.id.epg_book_event_name);
            timePrefix = view.findViewById(R.id.epg_book_time_prefix);
            time = view.findViewById(R.id.epg_book_time);
            seriesIcon = view.findViewById(R.id.epg_book_series_icon);
        }

        public TextView getEventName() {
            return eventName;
        }

        public TextView getTimePrefix() {
            return timePrefix;
        }

        public TextView getTime() {
            return time;
        }
        public ImageView getSeriesIcon() {
            return seriesIcon;
        }
    }

    public interface OnKeyListener {
        boolean onKey(View view, int keyCode, KeyEvent event, int position);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnFocusChangeListener {
        void onFocusChange(View view, boolean hasFocus, int position);
    }

    public void setOnKeyListener(OnKeyListener listener) {
        g_OnKeyListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        g_OnItemClickListener = listener;
    }

    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
        g_OnFocusChangeListener = listener;
    }
}
