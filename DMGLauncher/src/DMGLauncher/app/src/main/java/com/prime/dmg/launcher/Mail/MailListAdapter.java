package com.prime.dmg.launcher.Mail;

import static com.prime.dmg.launcher.Mail.Mail.UNREAD;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.dmg.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.dmg.launcher.R;
import com.prime.dtv.sysdata.MailInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class MailListAdapter extends RecyclerView.Adapter<MailListAdapter.MyViewHolder>{
    private static final String TAG = "MailListAdapter";

    private final WeakReference<Context> g_weakreference;
    private List<Mail> g_list_mail_info;
    private View.OnFocusChangeListener g_item_focus_listener;

    public MailListAdapter(Context context, List<Mail> mailInfoList, View.OnFocusChangeListener focusListener) {
        g_weakreference = new WeakReference<>(context);
        g_list_mail_info = mailInfoList;
        g_item_focus_listener = focusListener;
    }

    public void update_data(List<Mail> mailInfoList) {
        g_list_mail_info = mailInfoList;
        notifyDataSetChanged();
    }

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_mail_item_list, parent, false);
        return new MyViewHolder(inflate, get_context(), g_item_focus_listener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Mail mailInfo = g_list_mail_info.get(position);
        holder.g_textv_title.setText(mailInfo.getTitle());
        holder.g_textv_title.setTextColor(mailInfo.getRead_status()== UNREAD ? ContextCompat.getColor(get_context(), R.color.pvr_red_color) : ContextCompat.getColor(get_context(), R.color.taupe_gray));
        holder.itemView.setTag(position + "," + mailInfo.getId() + "," + mailInfo.getRead_status());
        holder.itemView.setNextFocusUpId(position == 0 ? holder.itemView.getId() : View.NO_ID);
        if (position == getItemCount() - 1)
            holder.itemView.setNextFocusDownId(holder.itemView.getId());
    }

    @Override
    public int getItemCount() {
        if (g_list_mail_info != null) {
            return g_list_mail_info.size();
        }
        return 0;
    }

    private Context get_context() {
        return g_weakreference.get();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView g_textv_title;

        public MyViewHolder(View itemView, Context context, final View.OnFocusChangeListener itemFocus) {
            super(itemView);
            this.g_textv_title = itemView.findViewById(R.id.lo_mail_inbox_sub_item_list_textv_title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    itemFocus.onFocusChange(v, hasFocus);
                    if (!hasFocus) {
                        g_textv_title.setEllipsize(TextUtils.TruncateAt.END);
                        g_textv_title.setSelected(false);
                        g_textv_title.setTextColor(ContextCompat.getColor(context, R.color.taupe_gray));
                        return;
                    }
                    ((MiddleFocusRecyclerView) v.getParent()).focus_middle_vertical(v);
                    g_textv_title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                    g_textv_title.setMarqueeRepeatLimit(-1);
                    g_textv_title.setSelected(true);
                    g_textv_title.setTextColor(Color.WHITE);
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
                this.g_drawable_divider.setBounds(paddingLeft + 29, bottom, width - 29, g_drawable_divider.getIntrinsicHeight() + bottom);
                this.g_drawable_divider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = 115;
            } else {
                outRect.bottom = 0;
            }
        }
    }
}
