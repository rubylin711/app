package com.prime.launcher.PVR.Management;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.launcher.R;
import com.prime.datastructure.sysdata.SeriesInfo;

import java.time.format.DateTimeFormatter;


public class SeriesAdapter extends RecyclerView.Adapter<SeriesAdapter.ViewHolder> {
    private static final String TAG = "SeriesAdapter";

    private final SeriesInfo.Series g_Series;
    private OnKeyListener g_OnKeyListener;
    private OnItemClickListener g_OnItemClickListener;
    private OnFocusChangeListener g_OnFocusChangeListener;
    private int g_FocusPosition;

    public SeriesAdapter(SeriesInfo.Series series) {
        g_Series = series;
        g_FocusPosition = 0;
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
            if (hasFocus) {
                ((MiddleFocusRecyclerView) view.getParent()).focus_middle_vertical(view);
                g_FocusPosition = position;
            }

            if (g_OnFocusChangeListener != null) {
                g_OnFocusChangeListener.onFocusChange(view, hasFocus, position);
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
        SeriesInfo.Episode episode = g_Series.getEpisodeList().get(position);
        holder.getEventName().setText(episode.getEventName());
        holder.getTimePrefix().setText(holder.itemView.getResources().getString(R.string.dvr_mgr_pre_record_time));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        holder.getTime().setText(episode.getStartLocalDateTime().format(formatter));
    }

    @Override
    public int getItemCount() {
        return g_Series == null ? 0 : g_Series.getNumberOfEpisode();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventName;
        private final TextView timePrefix;
        private final TextView time;

        public ViewHolder(View view) {
            super(view);

            eventName = view.findViewById(R.id.epg_book_event_name);
            timePrefix = view.findViewById(R.id.epg_book_time_prefix);
            time = view.findViewById(R.id.epg_book_time);
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
