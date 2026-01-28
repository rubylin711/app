package com.prime.launcher.Home.LiveTV.Music;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prime.launcher.R;
import com.prime.datastructure.sysdata.MusicInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class MusicCategoryAdapter extends RecyclerView.Adapter<MusicCategoryAdapter.MyViewHolder> {
    private static final String TAG = "MusicCategoryAdapter";

    private final WeakReference<Context> g_weakreference;
    private View.OnClickListener g_onclick_listener;
    private int g_current_selected_index = 0;
    private List<MusicInfo> g_music_info_list;
    private View.OnFocusChangeListener g_onfocus_listener;
    private View.OnKeyListener g_onkey_listener;

    public MusicCategoryAdapter(Context context, List<MusicInfo> data, View.OnClickListener click, View.OnFocusChangeListener focus, View.OnKeyListener keyListener) {
        g_weakreference = new WeakReference<>(context);
        g_music_info_list = data;
        g_onclick_listener = click;
        g_onfocus_listener = focus;
        g_onkey_listener = keyListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_music_category_item, parent, false);
        return new MyViewHolder(inflate, g_onclick_listener, g_onfocus_listener, g_onkey_listener);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.itemView.setTag(Integer.valueOf(position));
        if (!get().isFinishing() && !get().isDestroyed()) {
            Glide.with(get_context())
                    .load(g_music_info_list.get(position).get_url())
                    .into(holder.g_category_icon);
        }

        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //Log.d(TAG, "onFocusChange: index = " + (Integer) v.getTag() + " hasFocus = " + hasFocus);
                if (hasFocus) {
                    holder.g_category_focus_view.setVisibility(View.VISIBLE);
                    g_current_selected_index = (Integer) v.getTag();
                } else {
                    holder.g_category_focus_view.setVisibility(View.GONE);
                }
                g_onfocus_listener.onFocusChange(v, hasFocus);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (g_music_info_list == null) {
            return 0;
        }
        return g_music_info_list.size();
    }

    public void update_data(List<MusicInfo> musicInfoList) {
        g_music_info_list = musicInfoList;
        g_current_selected_index = 0;
        notifyDataSetChanged();
    }

    private AppCompatActivity get() {
        return (AppCompatActivity) get_context();
    }

    private Context get_context() {
        return g_weakreference.get();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public FrameLayout g_category_focus_view;
        public ImageView g_category_icon;

        public MyViewHolder(final View item, View.OnClickListener click, final View.OnFocusChangeListener focus, final View.OnKeyListener keyListener) {
            super(item);
            g_category_icon = item.findViewById(R.id.lo_music_imgv_category_item);
            g_category_focus_view = item.findViewById(R.id.lo_music_frml_category_item);
            item.setOnClickListener(click);
            item.setOnFocusChangeListener(focus);
            item.setOnKeyListener(keyListener);
        }
    }

    public static class MyItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) == 0) {
                //outRect.left = 40;
                outRect.left = view.getContext().getResources().getDimensionPixelSize(R.dimen.lo_music_decoration_marginstart);
            }
            outRect.top = view.getContext().getResources().getDimensionPixelSize(R.dimen.lo_music_decoration_margintop);
            outRect.right = view.getContext().getResources().getDimensionPixelSize(R.dimen.lo_music_decoration_marginstart);
        }
    }
}
