package com.prime.launcher.Rank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.prime.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.launcher.Home.LiveTV.LiveTvManager;
import com.prime.launcher.R;
import com.prime.launcher.Utils.JsonParser.RankInfo;
import com.prime.datastructure.sysdata.ProgramInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.MyViewHolder>{
    private static final String TAG = "RankAdapter";

    private final WeakReference<Context> g_weakreference;
    private List<RankInfo> g_rank_program_item_list;
    private View.OnFocusChangeListener g_item_focus_listener = null;
    private View.OnClickListener g_item_click_listener = null;

    public RankingAdapter(Context context, List<RankInfo> itemList, View.OnClickListener click, View.OnFocusChangeListener focus) {
        g_weakreference = new WeakReference<>(context);
        g_rank_program_item_list = itemList;
        g_item_click_listener = click;
        g_item_focus_listener = focus;
    }

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_rank_list_item, parent, false);
        return new MyViewHolder(linearLayout, g_item_click_listener, g_item_focus_listener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder position = " + position);
        RankInfo rankProgramItem = g_rank_program_item_list.get(position);

        if (!get().isFinishing() && !get().isDestroyed())
            Glide.with(get())
                    .load(rankProgramItem.get_channel_poster())
                    .error(get_res_id(rankProgramItem.get_service_id()))
                    .placeholder(get_res_id(rankProgramItem.get_service_id()))
                    //.override(iconWidth, iconHeight)
                    //.skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.g_imgv_channel);

        holder.g_textv_rank.setText(Integer.toString(position+1));
        holder.g_textv_program_name.setText(rankProgramItem.get_tv_name());
        holder.g_textv_channel_name.setText(get_channel_num(rankProgramItem.get_service_id(), rankProgramItem.get_channel_id()) + "  " + rankProgramItem.get_channel_name());
        holder.g_textv_ratio.setText(rankProgramItem.get_rating());
        holder.g_lnrl_item_view.setTag(position + "," + rankProgramItem.get_id() + "," + get_channel_num(rankProgramItem.get_service_id(), rankProgramItem.get_channel_id()));
        if (position == g_rank_program_item_list.size() - 1) {
            holder.g_lnrl_item_view.setNextFocusDownId(holder.g_lnrl_item_view.getId());
        } else {
            holder.g_lnrl_item_view.setNextFocusDownId(View.NO_ID);
        }
    }
    public void set_data(List<RankInfo> itemList) {
        g_rank_program_item_list = itemList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (g_rank_program_item_list != null) {
            return g_rank_program_item_list.size();
        }
        return 0;
    }

    private AppCompatActivity get() {
        return (AppCompatActivity) get_context();
    }

    private Context get_context() {
        return g_weakreference.get();
    }

    private int get_res_id(int serviceId) {
        return LiveTvManager.get_channel_icon_res_id(get_context(), String.valueOf(serviceId));
    }

    private String get_channel_num(int serviceId, int channelId) {
        ProgramInfo channelInfo = ((RankActivity)get_context()).g_rank.get_program_by_service_id(serviceId);
        if (channelInfo == null)
            return Integer.toString(channelId);
        return channelInfo.getDisplayNum(3);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout g_lnrl_item_view;
        public ImageView g_imgv_channel;
        public TextView g_textv_channel_name;
        public TextView g_textv_program_name;
        public TextView g_textv_rank;
        public TextView g_textv_ratio;

        public MyViewHolder(LinearLayout itemView, View.OnClickListener listener, final View.OnFocusChangeListener focusListener) {
            super(itemView);
            this.g_lnrl_item_view = itemView;
            this.g_textv_rank = itemView.findViewById(R.id.lo_rank_list_textv_rank_number);
            this.g_imgv_channel = itemView.findViewById(R.id.lo_rank_list_imgv_channel_icon);
            this.g_textv_channel_name = itemView.findViewById(R.id.lo_rank_list_textv_channel_name);
            this.g_textv_program_name = itemView.findViewById(R.id.lo_rank_list_textv_program_name);
            this.g_textv_ratio = itemView.findViewById(R.id.lo_rank_list_textv_ratio);
            itemView.setOnClickListener(listener);
            this.g_lnrl_item_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        ((MiddleFocusRecyclerView) v.getParent()).focus_middle_vertical(v);
                        MyViewHolder.this.g_textv_program_name.setTextColor(v.getContext().getResources().getColor(R.color.black, null));
                        MyViewHolder.this.g_textv_channel_name.setTextColor(v.getContext().getResources().getColor(R.color.black, null));
                        MyViewHolder.this.g_textv_ratio.setTextColor(v.getContext().getResources().getColor(R.color.black, null));
                    } else {
                        MyViewHolder.this.g_textv_program_name.setTextColor(-1);
                        MyViewHolder.this.g_textv_channel_name.setTextColor(v.getContext().getResources().getColor(R.color.rank_item_focus, null));
                        MyViewHolder.this.g_textv_ratio.setTextColor(-1);
                    }

                    if (g_item_focus_listener != null) {
                        g_item_focus_listener.onFocusChange(v, hasFocus);
                    }
                }
            });
        }
    }

    public static class MyItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable g_divider;

        public MyItemDecoration(Context context) {
            g_divider = ContextCompat.getDrawable(context, R.drawable.rank_divider);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = 30;
            } else {
                outRect.bottom = 0;
            }
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int paddingLeft = parent.getPaddingLeft();
            int width = parent.getWidth() - parent.getPaddingRight();
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = parent.getChildAt(i);
                int bottom = childAt.getBottom() + ((RecyclerView.LayoutParams) childAt.getLayoutParams()).bottomMargin;
                g_divider.setBounds(paddingLeft, bottom, width, g_divider.getIntrinsicHeight() + bottom);
                g_divider.draw(c);
            }
        }
    }
}
