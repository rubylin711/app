package com.prime.launcher.EPG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.launcher.ChannelChangeManager;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.EPGEvent;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/** @noinspection CommentedOutCode*/
public class EpgProgramAdapter extends RecyclerView.Adapter<EpgProgramAdapter.MyViewHolder> {
    private static final String TAG = "EpgProgramAdapter";

    private final WeakReference<Context> g_weakreference;
    private final ListLayer gListLayer;
    private int g_curr_selected_index = 0;
    private List<EPGEvent> g_program_list = null;
    //private View.OnFocusChangeListener g_item_focus_listener = null;
    private View.OnKeyListener g_item_right_key_listener = null;
    private List<BookInfo> g_bookInfo_list = null;
    private long g_channel_id;

    public EpgProgramAdapter(Context context, List<EPGEvent> programs, View.OnFocusChangeListener focus, View.OnKeyListener rightKey, ListLayer listLayer) {
        g_weakreference = new WeakReference<>(context);
        gListLayer = listLayer;
        g_program_list = programs;
        //g_item_focus_listener = focus;
        g_item_right_key_listener = rightKey;
        g_bookInfo_list = get_book_info_list();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_epg_list_item, parent, false);
        return new MyViewHolder(relativeLayout, g_item_right_key_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        /**/if (EpgView.isChangeChannel) {
            Log.w(TAG, "onBindViewHolder: interrupt bind view");
            //return;
        }

        if (!g_program_list.isEmpty()) {
            EPGEvent epgEvent = g_program_list.get(position);
            long channelId = g_channel_id;

            holder.itemView.setTag(position + "," + epgEvent.get_event_id());
            //UBAManager.setUbaTag(holder.itemView, "EPG-List");
            String format = new SimpleDateFormat("HH:mm").format(new Date(epgEvent.get_start_time()));
            //holder.g_imgv_ch_icon.setVisibility(View.VISIBLE);
            //holder.g_imgv_ch_icon.setImageDrawable(ResourcesCompat.getDrawable(get_context().getResources(),R.mipmap.ch006, null));
            holder.g_textv_text.setVisibility(View.VISIBLE);
            holder.g_textv_text.setText(format + "     " +epgEvent.get_event_name());
            holder.itemView.setNextFocusDownId(position == getItemCount() - 1 ? holder.itemView.getId() : View.NO_ID);
            holder.itemView.setNextFocusUpId(position == 0 ? holder.itemView.getId() : View.NO_ID);
            holder.itemView.setSelected(position == g_curr_selected_index);
            holder.g_imgv_record_icon.setVisibility(View.INVISIBLE);

            if (is_reminded(epgEvent))
                holder.g_imgv_remind_icon.setVisibility(View.VISIBLE);
            else
                holder.g_imgv_remind_icon.setVisibility(View.GONE);

            //Log.d(TAG, "onBindViewHolder: position = " + position);

            if (is_single_record(channelId, epgEvent) || is_reserve_record(g_bookInfo_list, epgEvent)) {
                holder.g_imgv_record_icon.setVisibility(View.VISIBLE);
                holder.g_imgv_record_icon.setImageResource(R.drawable.icon_rec_single);
            }
            else if (is_series_record(channelId, epgEvent) || is_reserve_series_record(g_bookInfo_list, epgEvent)) {
                holder.g_imgv_record_icon.setVisibility(View.VISIBLE);
                holder.g_imgv_record_icon.setImageResource(R.drawable.icon_rec_series);
            }
            else {
                holder.g_imgv_record_icon.setVisibility(View.GONE);
            }
            /*ViewGroup.LayoutParams layoutParams = holder.g_imgv_remind_icon.getLayoutParams();
            if (is_reminder(epgEvent)) {
                layoutParams.width = WRAP_CONTENT;
                layoutParams.height = WRAP_CONTENT;
                holder.g_imgv_remind_icon.setLayoutParams(layoutParams);
                //holder.g_imgv_remind_icon.setImageResource(R.drawable.icon_calendar_yellow);
            } else {
                layoutParams.width = 1;
                layoutParams.height = 1;
                holder.g_imgv_remind_icon.setLayoutParams(layoutParams);
            }*/
            on_focus_program(holder, position);
            set_text_color(holder, position);
            return;
        }

        holder.g_textv_text.setText(get_context().getResources().getText(R.string.epg_no_data));
    }

    private Context get_context() {
        return g_weakreference.get();
    }

    private EpgActivity get_activity() {
        return (EpgActivity) get_context();
    }

    @Override
    public int getItemCount() {
        if (g_program_list == null) {
            return 0;
        }
        return g_program_list.size();
    }

    public void set_current_select(int index) {
        int i = g_curr_selected_index;
        g_curr_selected_index = index;
        notifyItemChanged(i);
        notifyItemChanged(g_curr_selected_index);
    }

    private void set_text_color(final MyViewHolder holder, final int position) {
        if (g_curr_selected_index == position && !holder.itemView.hasFocus()) {
            holder.g_textv_text.setTextColor(holder.itemView.getResources().getColor(R.color.pvr_red_color, null));
        } else {
            holder.g_textv_text.setTextColor(-1);
        }
        //holder.g_textv_text.setSelected(g_curr_selected_index == position);
        //holder.g_textv_text.setEllipsize(g_curr_selected_index == position ? TextUtils.TruncateAt.MARQUEE : TextUtils.TruncateAt.END);
    }

    private void on_focus_program(final MyViewHolder holder, final int position) {
        holder.itemView.setOnFocusChangeListener((view, hasFocus) -> {
            MiddleFocusRecyclerView recyclerView = (MiddleFocusRecyclerView) view.getParent();

            if (hasFocus) {
                recyclerView.focus_middle_vertical(view);
                set_current_select(position);
                gListLayer.on_focus_program(view, position);
                holder.g_textv_text.setSelected(true);
                holder.g_textv_text.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            }
            else {
                int focusColor = holder.itemView.getResources().getColor(R.color.pvr_red_color, null);
                holder.g_textv_text.setTextColor(holder.itemView.isSelected() ? focusColor : -1);
                holder.g_textv_text.setSelected(false);
                holder.g_textv_text.setEllipsize(TextUtils.TruncateAt.END);
            }
        });
    }

    private List<BookInfo> get_book_info_list() {
        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
        List<BookInfo> bookInfoList = new ArrayList<>();
        List<BookInfo> bookInfoListFromRam =  primeDtv.book_info_get_list();
        Log.d(TAG, "get_book_info_list: bookInfoListFromRam size = " + bookInfoListFromRam.size());
        for (BookInfo bookInfo:bookInfoListFromRam) {
            if (bookInfo.getChannelId() == g_channel_id)
                bookInfoList.add(bookInfo);
        }
        return bookInfoList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update_program(List<EPGEvent> list, long channelId) {
        get_activity().runOnUiThread(() -> {
            g_program_list = list;
            g_curr_selected_index = 0;
            g_channel_id = channelId;
            g_bookInfo_list = get_book_info_list();
            //Log.d(TAG, "update_program: channelId = " + channelId);
            notifyDataSetChanged();
        });
    }

    public void update_program(int position, BookInfo bookInfo, boolean newFlag) {
        get_activity().runOnUiThread(() -> {
            g_bookInfo_list = get_book_info_list();
            notifyItemChanged(position);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update_program(BookInfo bookInfo, boolean newFlag) {
        get_activity().runOnUiThread(() -> {
            g_bookInfo_list = get_book_info_list();
            notifyDataSetChanged();
        });
    }

    public void update_program(int position) {
        get_activity().runOnUiThread(() -> notifyItemChanged(position));
    }

    public static boolean is_reminded(EPGEvent currentEvent) {
        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
        for (BookInfo bookInfo : primeDtv.book_info_get_list()) {
            if (bookInfo.getEpgEventId() == currentEvent.get_event_id() &&
                bookInfo.getBookType() == BookInfo.BOOK_TYPE_CHANGE_CHANNEL) {
                return true;
            }
        }
        return false;
    }

    public boolean is_single_record(long channelId, EPGEvent epgEvent) {
        ChannelChangeManager changeManager = ChannelChangeManager.get_instance(get_context());
        return changeManager.is_single_recording(channelId, epgEvent);
    }

    public boolean is_series_record(long channelId, EPGEvent epgEvent) {
        ChannelChangeManager changeManager = ChannelChangeManager.get_instance(get_context());
        return changeManager.is_series_recording(channelId, epgEvent);
    }

    public boolean is_reminder(EPGEvent epgEvent) {
        //Log.d(TAG, "is_reminder: epgEvent id = " + epgEvent.get_event_id());
        return get_bookInfo_by_event(epgEvent, BookInfo.BOOK_TYPE_CHANGE_CHANNEL) != null;
    }

    public boolean is_reserve_record(List<BookInfo> bookInfoList, EPGEvent epgEvent){
        //Log.e(TAG, "is_reserve_record: event name = " + epgEvent.get_event_name());
        ChannelChangeManager changeManager = ChannelChangeManager.get_instance(get_context());
        return changeManager.is_reserve_record(bookInfoList, epgEvent);
    }

    public boolean is_reserve_series_record(List<BookInfo> bookInfoList, EPGEvent epgEvent){
        //Log.e(TAG, "is_reserve_series_record: event name = " + epgEvent.get_event_name());
        ChannelChangeManager changeManager = ChannelChangeManager.get_instance(get_context());
        return changeManager.is_reserve_series_record(bookInfoList, epgEvent);
    }

    public boolean is_record(EPGEvent epgEvent, int position) {
        //Log.d(TAG, "is_record: epgEvent id = " + epgEvent.get_event_id());

        if (position == 0) {
            ChannelChangeManager channelChangeManager = ChannelChangeManager.get_instance(get_context());
            if (channelChangeManager.is_channel_recording(g_channel_id)) {
                //Log.d(TAG, "is_record: recording");
                return true;
            }
            else
                return false;
        }

        return check_is_record(epgEvent, BookInfo.BOOK_TYPE_RECORD);
    }

    public boolean is_series(EPGEvent epgEvent) {
        Log.d(TAG, "is_series:");
        return check_is_series(epgEvent, BookInfo.BOOK_TYPE_RECORD);
    }

    public BookInfo get_bookInfo_by_event(EPGEvent epgEvent, int Type) {
        Log.d(TAG, "get_bookInfo_by_event: ");
        for (BookInfo bookInfo : g_bookInfo_list) {
            //Log.d(TAG, "get_bookInfo_by_event: " + bookInfo.ToString());
            if (bookInfo.getEpgEventId() == epgEvent.get_event_id()
                    && bookInfo.getBookType() == Type) {
                return bookInfo;
            }
        }
        return null;
    }

    public BookInfo get_bookInfo_by_series_key(byte[] key, int Type) {
        //Log.d(TAG, "get_bookInfo_by_series_key:");
        for (BookInfo bookInfo : g_bookInfo_list) {
            if (Arrays.equals(key, bookInfo.getSeriesRecKey())
                    && bookInfo.getBookType() == Type)
                return bookInfo;
        }
        return null;
    }
    
    private boolean check_is_record(EPGEvent epgEvent, int Type) {
        //Log.d(TAG, "check_is_record:");
        for (BookInfo bookInfo : g_bookInfo_list) {
            if (Arrays.equals(epgEvent.get_series_key(), bookInfo.getSeriesRecKey())
                    && bookInfo.isSeries()
                    && bookInfo.getBookType() == Type)
                return true;

            if (bookInfo.getEpgEventId() == epgEvent.get_event_id()
                    && bookInfo.getBookType() == Type)
                return true;
        }
        //Log.d(TAG, "check_is_record: false");
        return false;
    }

    private boolean check_is_series(EPGEvent epgEvent, int Type) {
        //Log.d(TAG, "check_is_series: " + epgEvent.get_series_key().toString());
        for (BookInfo bookInfo : g_bookInfo_list) {
            if (Type != bookInfo.getBookType())
                continue;

            //Log.d(TAG, "check_is_series: " + bookInfo.getSeriesRecKey().toString());
            if (Arrays.equals(epgEvent.get_series_key(), bookInfo.getSeriesRecKey()) && bookInfo.isSeries()) {
                return true;
            }
        }
        return false;
    }

    public boolean same_programs(List<EPGEvent> programList) {
        if (g_program_list == null || programList == null)
            return false;

        if (programList.size() != g_program_list.size())
            return false;

        for (int i = 0; i < g_program_list.size(); i++) {
            EPGEvent epgEvent = g_program_list.get(i);
            EPGEvent program = programList.get(i);
            if (epgEvent.get_event_id() != program.get_event_id())
                return false;
            if (epgEvent.get_start_time() != program.get_start_time())
                return false;
            if (epgEvent.get_end_time() != program.get_end_time())
                return false;
            if (!epgEvent.get_event_name().equals(program.get_event_name()))
                return false;
        }
        return true;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        //public RelativeLayout g_rltvl_item_view;
        public TextView g_textv_text;
        public ImageView g_imgv_record_icon;
        public ImageView g_imgv_remind_icon;
        public ImageView g_imgv_ch_icon;

        public MyViewHolder(final RelativeLayout itemView, View.OnKeyListener rightKeyListener) {
            super(itemView);
            //g_rltvl_item_view = itemView;
            g_textv_text = itemView.findViewById(R.id.lo_epg_sub_list_item_textv_text);
            g_imgv_record_icon = itemView.findViewById(R.id.lo_epg_sub_list_item_imgv_record_icon);
            g_imgv_remind_icon = itemView.findViewById(R.id.lo_epg_sub_list_item_imgv_remind_icon);
            g_imgv_ch_icon  = itemView.findViewById(R.id.lo_epg_sub_list_item_imgv_channel_icon);
            itemView.setOnKeyListener(rightKeyListener);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }
}
