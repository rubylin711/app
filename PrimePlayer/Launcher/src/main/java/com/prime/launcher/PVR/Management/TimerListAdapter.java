package com.prime.launcher.PVR.Management;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.PrimeDtv;
import com.prime.launcher.R;
import com.prime.datastructure.sysdata.BookInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class TimerListAdapter extends RecyclerView.Adapter<TimerListAdapter.Holder>{
    private static final String TAG = TimerListAdapter.class.getSimpleName();

    private final WeakReference<AppCompatActivity> g_ref;
    private List<BookInfo> g_BookList;
    private int g_FocusPosition;
    private OnItemClickListener g_OnItemClickListener;

    public TimerListAdapter(AppCompatActivity activity, @NonNull List<BookInfo> bookInfoList) {
        g_ref = new WeakReference<>(activity);
        g_BookList = bookInfoList;
        //fill_list_size_to_8();
        g_FocusPosition = 0;
    }

    @NonNull
    @Override
    public TimerListAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_book_record_list, parent, false);
        Holder holder = new TimerListAdapter.Holder(itemView);
        itemView.setOnClickListener(v -> {
            if (g_OnItemClickListener != null) {
                int position = holder.getBindingAdapterPosition();
                g_OnItemClickListener.onItemClick(itemView, position);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TimerListAdapter.Holder holder, int position) {
        //on_key_book_record(holder, position);
        on_focus_change(holder, position);
        setup_book_record_item_view(holder, position);
    }

    @Override
    public int getItemCount() {
        return g_BookList.size();
    }


    public BookInfo get_focus_book_info() {
        if (g_BookList == null || g_BookList.isEmpty())
            return null;

        return g_BookList.get(g_FocusPosition);
    }
    public boolean delete_book_info() {
        if (g_BookList == null || g_BookList.isEmpty())
            return false;

        if (g_FocusPosition < 0 || g_FocusPosition >= g_BookList.size())
            return false;

        g_BookList.remove(g_FocusPosition);
        //fill_list_size_to_8();
        notifyItemRemoved(g_FocusPosition);
        notifyItemRangeChanged(g_FocusPosition, g_BookList.size());
        return true;
    }

    public void update_list(@NonNull List<BookInfo> bookInfoList) {
        g_BookList = bookInfoList;
        //fill_list_size_to_8();
        notifyDataSetChanged();
    }

    private AppCompatActivity get_activity() {
        return g_ref.get();
    }

    /*private void on_key_book_record(TimerListAdapter.Holder holder, int position) {
        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            if (KeyEvent.ACTION_DOWN != event.getAction())
                return false;

            return switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP,
                        KeyEvent.KEYCODE_DPAD_DOWN ->  on_press_dpad(position);
                case KeyEvent.KEYCODE_PROG_GREEN ->  on_press_epg(position);
                case KeyEvent.KEYCODE_PROG_RED ->  on_press_new(position);
                case KeyEvent.KEYCODE_PROG_BLUE ->   on_press_delete(position);
                default -> false;
            };
        });
    }

    boolean on_press_dpad(int position) {
        Log.d(TAG, "on_press_dpad: ");
        return false;
    }

    boolean on_press_epg(int position) {
        Log.d(TAG, "on_press_epg: ");
        return false;
    }

    boolean on_press_new(int position) {
        Log.d(TAG, "on_press_new: ");
        return false;
    }

    boolean on_press_delete(int position) {
        Log.d(TAG, "on_press_delete: ");
        return false;
    }*/

    void on_focus_change(TimerListAdapter.Holder holder, int position) {
        holder.itemView.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                //Log.d(TAG, "on_focus_change: position = " + position);
                ((MiddleFocusRecyclerView) view.getParent()).focus_middle_vertical(view);
                g_FocusPosition = position;
            }
        });
    }

    void setup_book_record_item_view(TimerListAdapter.Holder holder, int position) {
        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
        TextView channelName = holder.itemView.findViewById(R.id.book_channel_name);
        TextView recordTime = holder.itemView.findViewById(R.id.book_record_time);
        TextView bookCycle = holder.itemView.findViewById(R.id.book_cycle);
        TextView bookType = holder.itemView.findViewById(R.id.book_type);

        BookInfo bookInfo = g_BookList.get(position);

        boolean isEmpty = bookInfo.getEventName() == null || bookInfo.getEventName().isEmpty();
        if (isEmpty) {
            channelName.setText("");
            recordTime.setText("");
            bookCycle.setText("");
            bookType.setText("");
            holder.itemView.setFocusable(false);
            return;
        }

        holder.itemView.setFocusable(true);
        channelName.setText(bookInfo.getEventName());
        recordTime.setText(bookInfo.get_time_to_sting_format());
        bookCycle.setText(primeDtv.get_book_cycle_string(bookInfo,get_activity()));
        bookType.setText(get_book_type_string(bookInfo));
    }


    String get_book_type_string(BookInfo bookInfo) {
        //理論上Book type只會等於0
        return get_activity().getString(R.string.book_type_record);
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public Holder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void fill_list_size_to_8() {
        int recordSize = g_BookList.size();
        for (int i = 0; i < 8 - recordSize; i++)
            g_BookList.add(new BookInfo());
    }

    interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        g_OnItemClickListener = listener;
    }
}
