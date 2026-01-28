package com.prime.launcher.PVR.Conflict;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.R;
import com.prime.datastructure.sysdata.BookInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class SelectedListAdapter extends RecyclerView.Adapter<SelectedListAdapter.ViewHolder> {
    private static final String TAG = SelectedListAdapter.class.getSimpleName();

    private final ReplaceConflictDialog Dialog;
    private final WeakReference<Context> Ref;
    private final BookInfo ThirdBook;
    private final List<BookInfo> SelectedList;
    private int RadioBoxPosition;

    public SelectedListAdapter(ReplaceConflictDialog dialog, BookInfo thirdBook, List<BookInfo> selectedList) {
        Dialog = dialog;
        Ref = new WeakReference<>(dialog.getContext());
        ThirdBook = thirdBook;
        SelectedList = selectedList;
        RadioBoxPosition = 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_pvr_replace_radio_box, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookInfo book = SelectedList.get(position);
        holder.CheckRadio.setChecked(position == 0);
        holder.Title.setText(book.getChannelNum() + " " + book.getEventName());
        holder.itemView.setOnClickListener(v -> on_click_radio_box(holder, position));
        holder.itemView.setOnFocusChangeListener(this::on_focus_radio_box);

        if (position == SelectedList.size() - 1)
            holder.itemView.setNextFocusDownId(R.id.GoBackButton);
    }

    public void on_click_radio_box(ViewHolder holder, int position) {
        BookInfo book = SelectedList.get(position);
        RadioBoxPosition = position;
        Log.d(TAG, "on_click_radio_box: [position] " + position + " to be replaced ? [old book] " + book.getName());

        // update all radio button
        for (BookInfo bookInfo : SelectedList) {
            int index = SelectedList.indexOf(bookInfo);
            RadioButton radioButton = get_radio_button(index);
            if (radioButton != null)
                radioButton.setChecked(index == position);
        }
    }

    public void on_focus_radio_box(View view, boolean hasFocus) {
        TextView title = view.findViewById(R.id.Title);
        if (title == null) {
            Log.e(TAG, "on_focus_radio_box: title is null");
            return;
        }
        title.setSelected(hasFocus);
    }

    public Context getContext() {
        return Ref.get();
    }

    public int getRadioBoxPosition() {
        return RadioBoxPosition;
    }

    public RadioButton get_radio_button(int index) {
        SelectedListView selectedListView = Dialog.findViewById(R.id.SelectedListView);
        if (selectedListView == null) {
            Log.e(TAG, "get_radio_box: selectedListView is null");
            return null;
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) selectedListView.getLayoutManager();
        if (layoutManager == null) {
            Log.e(TAG, "get_radio_box: layoutManager is null");
            return null;
        }
        View itemView = layoutManager.findViewByPosition(index);
        if (itemView == null) {
            Log.e(TAG, "get_radio_box: itemView is null");
            return null;
        }
        RadioButton radioButton = itemView.findViewById(R.id.CheckRadio);
        if (radioButton == null) {
            Log.e(TAG, "get_radio_box: radioButton is null");
            return null;
        }
        return radioButton;
    }

    @Override
    public int getItemCount() {
        return SelectedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RadioButton CheckRadio;
        TextView Title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            CheckRadio = itemView.findViewById(R.id.CheckRadio);
            Title = itemView.findViewById(R.id.Title);
        }
    }
}
