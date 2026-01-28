package com.prime.dmg.launcher.EPG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.dmg.launcher.R;


public class EpgDateAdapter extends RecyclerView.Adapter<EpgDateAdapter.MyViewHolder> {
    private static final String TAG = EpgDateAdapter.class.getSimpleName();
    private final ListLayer gListLayer;
    private int g_curr_selected_index = 0;
    private SparseArray<String> g_data_list = null;
    private View.OnKeyListener g_item_right_key_listener = null;
    private Context g_context;

    public EpgDateAdapter(Context context, SparseArray<String> mDate, View.OnFocusChangeListener focus, View.OnKeyListener rightKey, ListLayer listLayer) {
        g_context = context;
        gListLayer = listLayer;
        g_data_list = mDate;
        g_item_right_key_listener = rightKey;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_epg_list_item, parent, false);
        return new MyViewHolder(relativeLayout, g_item_right_key_listener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.g_textv_text.setVisibility(View.VISIBLE);
        holder.g_textv_text.setText(g_data_list.get(position));
        holder.itemView.setNextFocusDownId(position == getItemCount() - 1 ? holder.itemView.getId() : View.NO_ID);
        holder.itemView.setNextFocusUpId(position == 0 ? holder.itemView.getId() : View.NO_ID);
        holder.itemView.setSelected(g_curr_selected_index == position);
        set_text_color(holder, position);
        on_focus_date(holder, position);
    }

    private void set_text_color(final MyViewHolder holder, final int position) {
        if (this.g_curr_selected_index == position && !holder.itemView.hasFocus()) {
            holder.g_textv_text.setTextColor(holder.itemView.getResources().getColor(R.color.pvr_red_color, null));
        } else {
            holder.g_textv_text.setTextColor(-1);
        }
        holder.g_textv_text.setSelected(g_curr_selected_index == position);
        holder.g_textv_text.setEllipsize(g_curr_selected_index == position ? TextUtils.TruncateAt.MARQUEE : TextUtils.TruncateAt.END);
    }

    private void on_focus_date(final MyViewHolder holder, final int position) {
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (gListLayer != null && gListLayer.g_rcv_date != null) {
                    gListLayer.g_rcv_date.focus_middle_vertical(v);
                    set_current_select(position);
                    gListLayer.on_focus_date(v);
                }
            }
            else {
                int focusColor = holder.itemView.getResources().getColor(R.color.pvr_red_color, null);
                holder.g_textv_text.setTextColor(holder.itemView.isSelected() ? focusColor : -1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return g_data_list.size();
    }

    public int move_select(boolean up) {
        int i;
        int i2 = g_curr_selected_index;
        if (up) {
            i = i2 - 1;
            if (i < 0) {
                i = getItemCount() - 1;
            }
        } else {
            i = i2 + 1;
            if (i >= getItemCount()) {
                i = 0;
            }
        }
        set_current_select(i);
        return i;
    }

    public int get_current_selected() {
        return g_curr_selected_index;
    }

    public void set_current_select(int index) {
        int i = g_curr_selected_index;
        g_curr_selected_index = index;
        notifyItemChanged(i);
        notifyItemChanged(g_curr_selected_index);
    }

    public boolean update_date(SparseArray<String> date) {
        if (compare_date_array(date)) {
            return false;
        }
        g_data_list = date;
        g_curr_selected_index = 0;
        ((AppCompatActivity) g_context).runOnUiThread(() -> {
            notifyDataSetChanged();
        });
        return true;
    }

    private boolean compare_date_array(SparseArray<String> date) {
        if (date == null || g_data_list == null || date.size() != g_data_list.size()) {
            return false;
        }
        for (int i = 0; i < g_data_list.size(); i++) {
            if (!g_data_list.valueAt(i).equals(date.valueAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView g_textv_text;

        public MyViewHolder(RelativeLayout itemView, View.OnKeyListener rightKeyListener) {
            super(itemView);
            this.g_textv_text = itemView.findViewById(R.id.lo_epg_sub_list_item_textv_text);
            itemView.setOnKeyListener(rightKeyListener);
        }
    }

    public static class MyItemDecoration extends RecyclerView.ItemDecoration {
        private final Drawable g_draw_divider;

        public MyItemDecoration(Context context) {
            this.g_draw_divider = ContextCompat.getDrawable(context, R.drawable.rank_divider);
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = 35;
            } else {
                outRect.bottom = 0;
            }
        }

        @Override
        public void onDraw(@NonNull Canvas c, RecyclerView parent, @NonNull RecyclerView.State state) {
            int g_int_offset_padding_left = 20;
            int paddingLeft = parent.getPaddingLeft() + g_int_offset_padding_left;
            int width = (parent.getWidth() - parent.getPaddingRight()) - g_int_offset_padding_left;
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = parent.getChildAt(i);
                int bottom = childAt.getBottom() + ((RecyclerView.LayoutParams) childAt.getLayoutParams()).bottomMargin;
                this.g_draw_divider.setBounds(paddingLeft, bottom, width, this.g_draw_divider.getIntrinsicHeight() + bottom);
                this.g_draw_divider.draw(c);
            }
        }
    }
}
