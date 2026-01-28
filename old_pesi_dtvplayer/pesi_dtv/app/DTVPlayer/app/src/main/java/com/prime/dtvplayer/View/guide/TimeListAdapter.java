package com.prime.dtvplayer.View.guide;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prime.dtvplayer.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class TimeListAdapter extends RecyclerView.Adapter<TimeListAdapter.TimeViewHolder> {
    private final String TAG = getClass().getSimpleName();
    private static final long TIME_UNIT_MS = TimeUnit.MINUTES.toMillis(30);

    private long mStartTime;
    private SimpleDateFormat formatter;

    public TimeListAdapter(long startTime) {
        mStartTime = startTime;
        formatter= new SimpleDateFormat("HH:mm",Locale.getDefault());
    }

    public void update(long startTimeMs) {
        mStartTime = startTimeMs;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return 7*24*2;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.program_guide_table_header_row_item;
    }

    @Override
    public void onBindViewHolder(TimeViewHolder holder, int position) {
        long startTime = mStartTime + position * TIME_UNIT_MS;
        long endTime = startTime + TIME_UNIT_MS;

        View itemView = holder.itemView;
        Date timeDate = new Date(startTime);
        String timeString;
        timeString = formatter.format(timeDate);
        ((TextView) itemView.findViewById(R.id.time)).setText(timeString);
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        lp.width = GuideUtils.convertMillisToPixel(startTime, endTime);

        itemView.setLayoutParams(lp);
    }

    @Override
    public TimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new TimeViewHolder(itemView);
    }

    static class TimeViewHolder extends RecyclerView.ViewHolder {
        TimeViewHolder(View itemView) {
            super(itemView);
        }
    }
}
