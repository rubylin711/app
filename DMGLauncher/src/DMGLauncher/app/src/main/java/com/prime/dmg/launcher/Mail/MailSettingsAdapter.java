package com.prime.dmg.launcher.Mail;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.dmg.launcher.R;

import java.lang.ref.WeakReference;

public class MailSettingsAdapter extends RecyclerView.Adapter<MailSettingsAdapter.MyViewHolder> {
    private static final String TAG = "MailSettingsAdapter";

    private final WeakReference<Context> g_weakreference;
    private SparseArray<Boolean> g_mail_settings_status_list;
    private View.OnClickListener g_item_click_listener;
    private String[] g_setting_label_list;

    public MailSettingsAdapter(Context context, SparseArray<Boolean> data, View.OnClickListener click) {
        g_weakreference = new WeakReference<>(context);
        g_mail_settings_status_list = data;
        g_item_click_listener = click;
        g_setting_label_list = context.getResources().getStringArray(R.array.mail_settings);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_mail_settings_list_item, parent, false);
        return new MyViewHolder(inflate, get_context(), g_item_click_listener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder g_textv_title = "+ g_setting_label_list[position]);
        holder.g_textv_title.setText(g_setting_label_list[position]);
        holder.itemView.setTag(position + "," + (position + 1001));
        holder.itemView.setNextFocusUpId(position == 0 ? holder.itemView.getId() : View.NO_ID);
        if (position == getItemCount() - 1) {
            holder.itemView.setNextFocusDownId(holder.itemView.getId());
        }
        holder.itemView.setNextFocusDownId(View.NO_ID);
        holder.g_switch.setChecked(g_mail_settings_status_list.get(position));
    }

    private Context get_context() {
        return g_weakreference.get();
    }

    @Override
    public int getItemCount() {
        if (g_mail_settings_status_list == null) {
            return 0;
        }
        return g_mail_settings_status_list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private Switch g_switch;
        private TextView g_textv_title;

        public MyViewHolder(View itemView, Context context, View.OnClickListener mItemClick) {
            super(itemView);
            this.g_textv_title = itemView.findViewById(R.id.lo_mail_settings_list_item_textv_label);
            this.g_switch = itemView.findViewById(R.id.lo_mail_settings_list_item_switch);;
            itemView.setOnClickListener(mItemClick);
            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        g_textv_title.setTextColor(ContextCompat.getColor(context, R.color.pvr_red_color));
                    } else {
                        g_textv_title.setTextColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            });
        }
    }

    public static class MyItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable g_drawable_divider;

        public MyItemDecoration(Context context) {
            this.g_drawable_divider = ContextCompat.getDrawable(context, R.drawable.rank_divider);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int paddingLeft = parent.getPaddingLeft();
            int width = parent.getWidth() - parent.getPaddingRight();
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = parent.getChildAt(i);
                int bottom = childAt.getBottom() + ((RecyclerView.LayoutParams) childAt.getLayoutParams()).bottomMargin;
                this.g_drawable_divider.setBounds(paddingLeft + 29, bottom, width - 29, this.g_drawable_divider.getIntrinsicHeight() + bottom);
                this.g_drawable_divider.draw(c);
            }
        }
    }
}
