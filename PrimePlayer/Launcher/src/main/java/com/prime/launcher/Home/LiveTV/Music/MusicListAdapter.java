package com.prime.launcher.Home.LiveTV.Music;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.launcher.R;
import com.prime.datastructure.sysdata.ProgramInfo;

import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MyViewHolder> {
    private static final String TAG = "MusicListAdapter";

    private View.OnClickListener g_item_click_listener;
    private List<ProgramInfo> g_channel_info_list;
    private View.OnFocusChangeListener g_item_focus_listener;
    private View.OnKeyListener g_item_key_listener;

    public MusicListAdapter(List<ProgramInfo> channelInfoList, View.OnClickListener onClickListener, View.OnFocusChangeListener onFocusListener, View.OnKeyListener onKeyListener) {
        g_channel_info_list = channelInfoList;
        g_item_click_listener = onClickListener;
        g_item_focus_listener = onFocusListener;
        g_item_key_listener = onKeyListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_music_list_item, parent, false);
        return new MyViewHolder(inflate, g_item_click_listener);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        ProgramInfo channelInfo = g_channel_info_list.get(position);
        holder.itemView.setTag(position + "," + channelInfo.getDisplayNum());
        holder.g_textv_number.setText(get_leading_zero_number_at(channelInfo.getDisplayNum()));
        holder.g_textv_name.setText(channelInfo.getDisplayName());
        holder.itemView.setNextFocusDownId(position == getItemCount() + (-1) ? holder.itemView.getId() : View.NO_ID);
        holder.itemView.setOnKeyListener((v, keyCode, event)-> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (position == 0 && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    g_item_key_listener.onKey(v, keyCode, event);
                    return true;
                }
                else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    g_item_key_listener.onKey(v, keyCode, event);
                    return true;
                }
                else
                    return false;
            }
            return false;
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ((MiddleFocusRecyclerView) v.getParent()).focus_middle_vertical(v);
            }
           g_item_focus_listener.onFocusChange(v, hasFocus);
        });
    }

    public void update_data(List<ProgramInfo> channelInfoList) {
        g_channel_info_list = channelInfoList;
        notifyDataSetChanged();
    }

    @SuppressLint("DefaultLocale")
    public String get_leading_zero_number_at(int number) {
        return String.format("%03d", number);
    }

    @Override
    public int getItemCount() {
        if (g_channel_info_list != null) {
            return g_channel_info_list.size();
        }
        return 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView g_textv_name;
        public TextView g_textv_number;

        public MyViewHolder(final View item, View.OnClickListener click) {
            super(item);
            g_textv_number = item.findViewById(R.id.lo_music_list_item_number);
            g_textv_name = item.findViewById(R.id.lo_music_list_item_name);
            item.setOnClickListener(click);
        }
    }

    public static class MyItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable g_divider;
        private final int DIVIDER_BOUNDS_OFFSET = 29;

        public MyItemDecoration(Context context) {
            g_divider = ContextCompat.getDrawable(context, R.drawable.rank_divider);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int paddingLeft = parent.getPaddingLeft();
            int width = parent.getWidth() - parent.getPaddingRight();
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = parent.getChildAt(i);
                int bottom = childAt.getBottom() + ((RecyclerView.LayoutParams) childAt.getLayoutParams()).bottomMargin;
                g_divider.setBounds(paddingLeft + DIVIDER_BOUNDS_OFFSET , bottom, width - DIVIDER_BOUNDS_OFFSET, g_divider.getIntrinsicHeight() + bottom);
                g_divider.draw(c);
            }
        }
    }
}
