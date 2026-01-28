package com.prime.dmg.launcher.EPG;

import static android.view.View.*;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.prime.dmg.launcher.Home.LiveTV.LiveTvManager;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.R;
import com.prime.dtv.sysdata.ProgramInfo;

import java.lang.ref.WeakReference;
import java.util.List;

/** @noinspection CommentedOutCode*/
public class EpgChannelAdapter extends RecyclerView.Adapter<EpgChannelAdapter.MyViewHolder> {

    private static final String TAG = EpgChannelAdapter.class.getSimpleName();

    public static final int MAX_LENGTH_OF_SERVICE_ID = MiniEPG.MAX_LENGTH_OF_SERVICE_ID;

    private static int FOCUSED_TEXT_COLOR;
    private static int UNFOCUSED_TEXT_COLOR;

    private final WeakReference<Context> g_weakreference;
    private final ListLayer gListLayer;
    private int g_curr_selected_index = 0;
    private List<ProgramInfo> g_channel_list = null;
    //private OnFocusChangeListener g_item_focus_listener = null;
    private OnKeyListener g_item_right_key_listener = null;

    public EpgChannelAdapter(Context context, List<ProgramInfo> list, OnFocusChangeListener focus, OnKeyListener rightKey, ListLayer listLayer) {
        g_weakreference = new WeakReference<>(context);
        g_channel_list = list;
        //g_item_focus_listener = focus;
        g_item_right_key_listener = rightKey;
        FOCUSED_TEXT_COLOR = context.getResources().getColor(R.color.pvr_red_color, null);
        UNFOCUSED_TEXT_COLOR = -1;
        gListLayer = listLayer;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_epg_list_item, parent, false);
        return new MyViewHolder(relativeLayout, g_item_right_key_listener);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        ProgramInfo programInfo = g_channel_list.get(position);
        holder.itemView.setTag(position + "," + programInfo.getChannelId() + "," + programInfo.getDisplayNum() + "," + programInfo.getTpId() + "," + programInfo.getType());
        holder.g_imgv_ch_icon.setVisibility(VISIBLE);
        holder.g_textv_text.setVisibility(VISIBLE);
        //holder.g_imgv_ch_icon.setImageDrawable(ResourcesCompat.getDrawable(get_context().getResources(),R.mipmap.ch006, null));
        holder.set_channel_icon(programInfo, holder.g_imgv_ch_icon);
        String chNum = programInfo.getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM);
        String chName = chNum.concat("  ").concat(programInfo.getDisplayName());
        holder.g_textv_text.setText(chName);
        holder.itemView.setNextFocusDownId(position == getItemCount() - 1 ? holder.itemView.getId() : NO_ID);
        holder.itemView.setNextFocusUpId(position == 0 ? holder.itemView.getId() : NO_ID);
        holder.itemView.setSelected(position == g_curr_selected_index);
        setTextColor(holder, position);
        on_focus_channel(holder, position);
    }

    private void on_focus_channel(final MyViewHolder holder, final int position) {
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            MiddleFocusRecyclerView recyclerView = (MiddleFocusRecyclerView) v.getParent();
            int holderPosition = holder.getLayoutPosition();

            if (hasFocus) {
                //recyclerView.focus_middle_vertical(v);
                recyclerView.scroll_to_center(v);
                set_current_selected(holderPosition);
                gListLayer.on_focus_channel(v);
                gListLayer.set_current_program_position(0);
            }
            else {
                ProgramInfo currChannel = EpgView.channel_of_change_genre;

                if (currChannel == null) {
                    if (holderPosition < g_channel_list.size())
                        currChannel = g_channel_list.get(holderPosition);
                    else
                        currChannel = g_channel_list.get(0);
                }

                gListLayer.set_previous_channel(currChannel);
                holder.g_textv_text.setTextColor(holder.itemView.isSelected() ? FOCUSED_TEXT_COLOR : UNFOCUSED_TEXT_COLOR);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (g_channel_list == null) {
            return 0;
        }
        return g_channel_list.size();
    }

    private void setTextColor(final MyViewHolder holder, final int position) {
        holder.g_textv_text.setTextColor(g_curr_selected_index == position && !holder.itemView.hasFocus() ? FOCUSED_TEXT_COLOR : UNFOCUSED_TEXT_COLOR);
        holder.g_textv_text.setSelected(g_curr_selected_index == position);
        holder.g_textv_text.setEllipsize(g_curr_selected_index == position ? TextUtils.TruncateAt.MARQUEE : TextUtils.TruncateAt.END);
    }

    public int get_current_selected() {
        return g_curr_selected_index;
    }

    public void set_current_selected(int index) {
        int i = g_curr_selected_index;
        g_curr_selected_index = index;
        notifyItemChanged(i);
        notifyItemChanged(g_curr_selected_index);
    }

    public void update_channel(List<ProgramInfo> list) {
        g_channel_list = list;
        g_curr_selected_index = 0;
    }

    /*@SuppressLint("DefaultLocale")
    public String get_leading_zero_number_at(int number) {
        return String.format("%03d", number);
    }*/

    private AppCompatActivity get() {
        return (AppCompatActivity) get_context();
    }

    private Context get_context() {
        return g_weakreference.get();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView g_imgv_ch_icon;
        public TextView g_textv_text;
        //public RelativeLayout g_rltvl_item_view;

        public MyViewHolder(RelativeLayout itemView, OnKeyListener rightKeyListener) {
            super(itemView);
            //g_rltvl_item_view = itemView;
            g_imgv_ch_icon = itemView.findViewById(R.id.lo_epg_sub_list_item_imgv_channel_icon);
            g_textv_text = itemView.findViewById(R.id.lo_epg_sub_list_item_textv_text);
            itemView.setOnKeyListener(rightKeyListener);
        }

        public void set_channel_icon(ProgramInfo chInfo, ImageView channelIcon) {
            String iconUrl, serviceId;
            int iconResId, iconWidth, iconHeight;

            serviceId   = chInfo.getServiceId(MAX_LENGTH_OF_SERVICE_ID);
            iconUrl     = LiveTvManager.get_channel_icon_url(get_context(), serviceId);
            iconResId   = LiveTvManager.get_channel_icon_res_id(get_context(), serviceId);
            iconWidth   = get_context().getResources().getDimensionPixelSize(R.dimen.lo_epg_sub_list_item_imgv_channel_icon_width);
            iconHeight  = get_context().getResources().getDimensionPixelSize(R.dimen.lo_epg_sub_list_item_imgv_channel_icon_height);

            if (chInfo.getType() == ProgramInfo.PROGRAM_RADIO)
                iconResId = R.mipmap.ch300;
            Log.d(TAG, "set_channel_icon: " + iconUrl);
            if (get() != null && !get().isFinishing() && !get().isDestroyed()) {
                Glide.with(get())
                        .load(iconUrl)
                        .error(iconResId)
                        .placeholder(iconResId)
                        .override(iconWidth, iconHeight)
                        .priority(Priority.HIGH)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(channelIcon);
            }
        }
    }
}
