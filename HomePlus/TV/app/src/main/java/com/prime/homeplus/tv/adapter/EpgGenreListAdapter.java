package com.prime.homeplus.tv.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.data.GenreData;

import java.util.List;

public class EpgGenreListAdapter extends RecyclerView.Adapter<EpgGenreListAdapter.ViewHolder> {
    private static final String TAG = "EpgGenreListAdapter";

    private RecyclerView recyclerView;
    private OnRecyclerViewInteractionListener interactionListener;

    private Context context;
    List<GenreData.GenreInfo> genreList;
    private int lastGenreIndex = -1;
    private int currentGenreIndex = 0;

    public EpgGenreListAdapter(RecyclerView recyclerView, List<GenreData.GenreInfo> genreList) {
        Log.d(TAG, "EpgGenreListAdapter");
        this.recyclerView = recyclerView;
        this.context = recyclerView.getContext();
        this.genreList = genreList;;
    }

    public interface OnRecyclerViewInteractionListener {
        void onClick(GenreData.GenreInfo genreInfo);
        void onKeyEventReceived(KeyEvent event);
    }

    public void setOnRecyclerViewInteractionListener(OnRecyclerViewInteractionListener listener) {
        this.interactionListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llEpgGenreList;
        RadioButton rbEpgGenreList;
        TextView tvEpgGenreListName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            llEpgGenreList = itemView.findViewById(R.id.llEpgGenreList);
            rbEpgGenreList = itemView.findViewById(R.id.rbEpgGenreList);
            tvEpgGenreListName = itemView.findViewById(R.id.tvEpgGenreListName);
        }

        public void applyFocusStyle() {
            llEpgGenreList.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
            rbEpgGenreList.setButtonTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
            tvEpgGenreListName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvEpgGenreListName.setTypeface(null, Typeface.BOLD);
        }

        public void applyNormalStyle() {
            llEpgGenreList.setBackgroundColor(Color.TRANSPARENT);
            rbEpgGenreList.setButtonTintList(ColorStateList.valueOf(context.getColor(R.color.colorWhiteOpacity50)));
            tvEpgGenreListName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvEpgGenreListName.setTypeface(null, Typeface.NORMAL);
        }
    }

    @NonNull
    @Override
    public EpgGenreListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_epg_genre_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpgGenreListAdapter.ViewHolder holder, int position) {
        GenreData.GenreInfo genreInfo = genreList.get(position);
        Log.d(TAG, "onBindViewHolder position:" + position);

        holder.tvEpgGenreListName.setText(genreInfo.getName(context));
        if (position == currentGenreIndex) {
            holder.rbEpgGenreList.setButtonDrawable(R.drawable.layer_epg_genre_radio_btn_checked);
        }

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder click position:" + position);
            if (interactionListener != null) {
                lastGenreIndex = currentGenreIndex;
                currentGenreIndex = position;

                if (lastGenreIndex != -1 && lastGenreIndex < getItemCount()) {
                    EpgGenreListAdapter.ViewHolder viewHolderAtPosition = (EpgGenreListAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(lastGenreIndex);
                    viewHolderAtPosition.rbEpgGenreList.setButtonDrawable(R.drawable.layer_epg_genre_radio_btn_unchecked);
                }
                interactionListener.onClick(genreList.get(currentGenreIndex));
            }
        });

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            Log.d(TAG, "onBindViewHolder setOnKeyListener position:" + position +
                    ", action:" + event.getAction() + ", keyCode:" + keyCode);
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            int itemCount = getItemCount();

            // Loop Navigation
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && position == 0) {
                recyclerView.scrollToPosition(itemCount - 1);
                recyclerView.postDelayed(() -> {
                    View lastView = recyclerView.getLayoutManager().findViewByPosition(itemCount - 1);
                    if (lastView != null) lastView.requestFocus();
                }, 100);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == genreList.size() - 1) {
                recyclerView.scrollToPosition(0);
                recyclerView.postDelayed(() -> {
                    View firstView = recyclerView.getLayoutManager().findViewByPosition(0);
                    if (firstView != null) firstView.requestFocus();
                }, 100);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                    keyCode == KeyEvent.KEYCODE_PROG_RED) {
                if (interactionListener != null) {
                    interactionListener.onKeyEventReceived(event);
                }
                return true;
            }

            return false;
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d(TAG, "onBindViewHolder setOnFocusChangeListener position:" + position + ", hasFocus:" + hasFocus);
            if (hasFocus) {
                holder.applyFocusStyle();
            } else {
                holder.applyNormalStyle();
            }
        });
    }

    @Override
    public int getItemCount() {
        return genreList != null ? genreList.size() : 0;
    }

    public GenreData.GenreInfo getCurrentGenreInfo() {
        if (currentGenreIndex < getItemCount()) {
            return genreList.get(currentGenreIndex);
        }
        return null;
    }

    public void resetIndex() {
        Log.d(TAG, "reset currentGenreIndex to 0");
        currentGenreIndex = 0;
    }

    public void resetRadioButtonStyle() {
        Log.d(TAG, "resetRadioButtonStyle");
        for (int i = 0; i < getItemCount(); i++) {
            EpgGenreListAdapter.ViewHolder viewHolderAtPosition = (EpgGenreListAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (viewHolderAtPosition != null) {
                viewHolderAtPosition.rbEpgGenreList.setButtonDrawable(R.drawable.layer_epg_genre_radio_btn_unchecked);
            }
        }
    }

    public void focusCurrentGenreIndex() {
        Log.d(TAG, "focusCurrentGenreIndex currentGenreIndex:" + currentGenreIndex + ", getItemCount:" + getItemCount());
        if (currentGenreIndex < getItemCount()) {
            final int finalFocusIndex = currentGenreIndex;
            recyclerView.scrollToPosition(finalFocusIndex);
            EpgGenreListAdapter.ViewHolder viewHolderAtPosition = (EpgGenreListAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(finalFocusIndex);
            if (viewHolderAtPosition != null) {
                viewHolderAtPosition.rbEpgGenreList.setButtonDrawable(R.drawable.layer_epg_genre_radio_btn_checked);
                viewHolderAtPosition.itemView.requestFocus();
            }
        }
    }
}

