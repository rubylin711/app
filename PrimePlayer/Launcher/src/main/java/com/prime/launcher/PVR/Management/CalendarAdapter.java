package com.prime.launcher.PVR.Management;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.R;

import java.lang.ref.WeakReference;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
    String TAG = CalendarAdapter.class.getSimpleName();

    private final WeakReference<Context> g_ref;
    private List<String> g_dayList;
    private int g_year, g_month;

    public CalendarAdapter(Context context, List<String> days, int year, int month) {
        g_ref = new WeakReference<>(context);
        g_dayList = days;
        g_year = year;
        g_month = month;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder: position = " + position);

        if (position < 7 || g_dayList.get(position).length() == 0)
            holder.dayText.setFocusable(false);

        holder.dayText.setText(g_dayList.get(position));
        holder.itemView.setTag(g_dayList.get(position));

        holder.itemView.setOnClickListener(v -> {
            ((TimerManagementActivity) get_context()).
                    on_click_day(g_year, g_month, Integer.parseInt((String) v.getTag()));
        });
    }

    private Context get_context() {
        return g_ref.get();
    }

    @Override
    public int getItemCount() {
        return g_dayList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<String> dayList, int year, int month) {
        g_year = year;
        g_month = month;
        g_dayList = dayList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayText;

        public ViewHolder(View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayText);
        }
    }
}