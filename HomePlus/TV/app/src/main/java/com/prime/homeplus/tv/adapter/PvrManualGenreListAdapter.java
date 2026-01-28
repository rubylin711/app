package com.prime.homeplus.tv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.data.GenreData;

import java.util.List;

public class PvrManualGenreListAdapter extends RecyclerView.Adapter<PvrManualGenreListAdapter.ViewHolder> {
    private static final String TAG = "PvrManualGenreListAdapter";

    private RecyclerView recyclerView;
    private OnRecyclerViewInteractionListener interactionListener;

    private Context context;
    List<GenreData.GenreInfo> genreList;
    private int currentGenreIndex = 0;

    public PvrManualGenreListAdapter(RecyclerView recyclerView, List<GenreData.GenreInfo> genreList) {
        Log.d(TAG, "PvrManualGenreListAdapter");
        this.recyclerView = recyclerView;
        this.context = recyclerView.getContext();
        this.genreList = genreList;;
    }

    public interface OnRecyclerViewInteractionListener {
        void onFocus(GenreData.GenreInfo genreInfo);
        void onKeyEventReceived(KeyEvent event);
    }

    public void setOnRecyclerViewInteractionListener(OnRecyclerViewInteractionListener listener) {
        this.interactionListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llPvrManualGenreList;
        TextView tvPvrManualGenreListName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            llPvrManualGenreList = itemView.findViewById(R.id.llPvrManualGenreList);
            tvPvrManualGenreListName = itemView.findViewById(R.id.tvPvrManualGenreListName);
        }

        public void applyFocusStyle() {
            llPvrManualGenreList.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
            tvPvrManualGenreListName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvPvrManualGenreListName.setTypeface(null, Typeface.BOLD);
        }

        public void applyNormalStyle() {
            llPvrManualGenreList.setBackgroundColor(Color.TRANSPARENT);
            tvPvrManualGenreListName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvPvrManualGenreListName.setTypeface(null, Typeface.NORMAL);
        }
    }

    @NonNull
    @Override
    public PvrManualGenreListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pvr_manual_genre_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PvrManualGenreListAdapter.ViewHolder holder, int position) {
        GenreData.GenreInfo genreInfo = genreList.get(position);
        Log.d(TAG, "onBindViewHolder position:" + position);

        holder.tvPvrManualGenreListName.setText(genreInfo.getName(context));

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            Log.d(TAG, "onBindViewHolder setOnKeyListener position:" + position +
                    ", action:" + event.getAction() + ", keyCode:" + keyCode + "genreList.size()::" + genreList.size());
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && position != 0) {
                recyclerView.scrollToPosition(position - 1);
                View lastView = recyclerView.getLayoutManager().findViewByPosition(position - 1);
                if (lastView != null) lastView.requestFocus();
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (position < genreList.size() - 1){
                    recyclerView.scrollToPosition(position + 1);
                    View firstView = recyclerView.getLayoutManager().findViewByPosition(position + 1);
                    if (firstView != null) firstView.requestFocus();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                    keyCode == KeyEvent.KEYCODE_PROG_RED ||
                    keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
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
                currentGenreIndex = position;
                if (interactionListener != null) {
                    interactionListener.onFocus(genreList.get(position));
                }

                holder.applyFocusStyle();
                recyclerView.setBackgroundColor(context.getResources().getColor(R.color.colorButton2));
            } else {
                holder.applyNormalStyle();

            }
        });
    }

    @Override
    public int getItemCount() {
        return genreList != null ? genreList.size() : 0;
    }

    public int getCurrentGenreIndex() {
        return currentGenreIndex;
    }
}

