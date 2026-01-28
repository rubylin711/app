package com.prime.launcher.Settings.Adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.launcher.Utils.Utils;
import com.prime.launcher.R;
import com.prime.datastructure.sysdata.ProgramInfo;

import java.util.List;

public class ChannelLockAdapter extends RecyclerView.Adapter<ChannelLockAdapter.MyViewHolder>{
    private static final String TAG = "ChannelLockAdapter";

    private List<ProgramInfo> g_program_info_list;
    private boolean g_lock_status;
    private final View.OnClickListener g_on_click_listener;
    private final View.OnKeyListener g_on_key_listener;

    public ChannelLockAdapter(List<ProgramInfo> programInfoList, boolean lockStatus, View.OnClickListener onClickListener, View.OnKeyListener onKeyListener) {
        g_program_info_list = programInfoList;
        g_lock_status = lockStatus;
        g_on_click_listener = onClickListener;
        g_on_key_listener = onKeyListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_channel_lock, parent, false);
        return new MyViewHolder(relativeLayout, g_on_click_listener, g_on_key_listener,g_lock_status);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: position = " + position);
        ProgramInfo programInfo = g_program_info_list.get(position);

        holder.g_textv_channel_name.setText(programInfo.getDisplayName());
        holder.g_textv_channel_num.setText(Utils.get_leading_zero_number_at(programInfo.getDisplayNum()));
        holder.g_rltvl_item_view.setTag(Integer.valueOf(programInfo.getDisplayNum()));
    }

    @Override
    public int getItemCount() {
        return g_program_info_list.size();
    }

    public void set_data(List<ProgramInfo> ProgramInfoList) {
        g_program_info_list = ProgramInfoList;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView g_textv_channel_name, g_textv_channel_num;
        public RelativeLayout g_rltvl_item_view;
        public ImageView g_imgv_status;

        public MyViewHolder(RelativeLayout itemView, View.OnClickListener onClickListener, View.OnKeyListener onKeyListener, boolean lockStatus) {
            super(itemView);
            g_rltvl_item_view = itemView;
            g_textv_channel_name = itemView.findViewById(R.id.lo_item_channel_lock_channel_name_text);
            g_textv_channel_num = itemView.findViewById(R.id.lo_item_channel_lock_channel_num_text);
            g_imgv_status = itemView.findViewById(R.id.lo_item_channel_lock_channel_status);
            itemView.setOnClickListener(onClickListener);
            itemView.setOnKeyListener(onKeyListener);
            itemView.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        ((MiddleFocusRecyclerView) v.getParent()).focus_middle_vertical(v);
                        if (lockStatus)
                            g_imgv_status.setBackgroundResource(R.drawable.icon_lock_delete);
                        else
                            g_imgv_status.setBackgroundResource(R.drawable.icon_lock_add);

                        g_textv_channel_name.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                        g_textv_channel_name.setSelected(true);
                        return;
                    }
                    g_imgv_status.setBackgroundResource(0);
                    g_textv_channel_name.setEllipsize(TextUtils.TruncateAt.END);
            });
        }
    }
}
