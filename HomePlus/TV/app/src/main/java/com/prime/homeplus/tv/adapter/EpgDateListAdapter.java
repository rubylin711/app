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
import com.prime.homeplus.tv.utils.TimeUtils;

import java.util.List;

public class EpgDateListAdapter extends RecyclerView.Adapter<EpgDateListAdapter.ViewHolder> {
    private static final String TAG = "EpgDateListAdapter";

    private RecyclerView recyclerView;
    private OnRecyclerViewInteractionListener interactionListener;

    private Context context;
    List<Long> dateList;
    private int lastFocusedPosition = -1;
    private int lastActionDownKeyCode = -1;

    public EpgDateListAdapter(RecyclerView recyclerView, List<Long> dateList) {
        Log.d(TAG, "EpgDateListAdapter");
        this.recyclerView = recyclerView;
        this.context = recyclerView.getContext();
        this.dateList = dateList;;
    }

    public interface OnRecyclerViewInteractionListener {
        void onPageUp();
        void onPageDown();
        void onKeyEventReceived(KeyEvent event);
        void onFocus(boolean hasFocus);
        void onFocusDateChanged(int index, long searchStartTime);
    }

    public void setOnRecyclerViewInteractionListener(OnRecyclerViewInteractionListener listener) {
        this.interactionListener = listener;
    }

    public void setLastFocusedPosition(int position) {
        if (lastFocusedPosition != position) {
            lastFocusedPosition = position;
        }
    }

    public void setLastActionDownKeyCode(int keyCode) {
        if (lastActionDownKeyCode != keyCode) {
            lastActionDownKeyCode = keyCode;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llEpgDateList;
        TextView tvEpgDateListDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            llEpgDateList = itemView.findViewById(R.id.llEpgDateList);
            tvEpgDateListDate = itemView.findViewById(R.id.tvEpgDateListDate);
        }

        public void applyFocusStyle() {
            llEpgDateList.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
            tvEpgDateListDate.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvEpgDateListDate.setTypeface(null, Typeface.BOLD);
        }

        public void applySelectedStyle() {
            llEpgDateList.setBackgroundColor(ContextCompat.getColor(context, R.color.colorEpgSelect));
            tvEpgDateListDate.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvEpgDateListDate.setTypeface(null, Typeface.BOLD);
        }

        public void applyNormalStyle() {
            llEpgDateList.setBackgroundColor(Color.TRANSPARENT);
            tvEpgDateListDate.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity30));
            tvEpgDateListDate.setTypeface(null, Typeface.NORMAL);
        }
    }

    @NonNull
    @Override
    public EpgDateListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_epg_date_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpgDateListAdapter.ViewHolder holder, int position) {
        Long searchStartTime = dateList.get(position);
        Log.d(TAG, "onBindViewHolder position:" + position);

        if (lastFocusedPosition == -1 && position == 0) {
            // draw default select item
            holder.applySelectedStyle();
        }

        holder.tvEpgDateListDate.setText(TimeUtils.formatTimestampWithWeekday(searchStartTime));

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder click position:" + position);
        });

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            Log.d(TAG, "onBindViewHolder setOnKeyListener position:" + position +
                    ", action:" + event.getAction() + ", keyCode:" + keyCode);
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            setLastActionDownKeyCode(keyCode);

            // Loop Navigation
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && position == 0) {
                if (interactionListener != null) {
                    interactionListener.onPageUp();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == dateList.size() - 1) {
                if (interactionListener != null) {
                    interactionListener.onPageDown();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                    keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                    keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (interactionListener != null) {
                    interactionListener.onKeyEventReceived(event);
                }
                return true;
            }

            return false;
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (interactionListener != null) {
                    interactionListener.onFocus(true);
                    if (position != lastFocusedPosition) {
                        interactionListener.onFocusDateChanged(position, searchStartTime);
                    }
                }

                setLastFocusedPosition(position);
                holder.applyFocusStyle();
            } else {
                holder.applyNormalStyle();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList != null ? dateList.size() : 0;
    }

    public void updateList(List<Long> newList, boolean isFocusBottom) {
        this.dateList.clear();
        this.dateList.addAll(newList);
        notifyDataSetChanged();

        if (!dateList.isEmpty()) {
            int focusIndex = 0;
            if (isFocusBottom) {
                focusIndex = getItemCount() - 1;
            }
            final int finalFocusIndex = focusIndex;
            recyclerView.scrollToPosition(finalFocusIndex);
            recyclerView.postDelayed(() -> {
                View firstItem = recyclerView.getLayoutManager().findViewByPosition(finalFocusIndex);
                if (firstItem != null) {
                    firstItem.requestFocus();
                }
            }, 100);
        }
    }

    public void updateListWithoutFocus(List<Long> newList) {
        Log.d(TAG, "updateListWithoutFocus newList size:" + newList.size());
        lastFocusedPosition = -1;
        this.dateList.clear();
        this.dateList.addAll(newList);
        notifyDataSetChanged();
    }

    public void drawSelectItem(int selectIndex) {
        lastFocusedPosition = selectIndex;
        for (int i = 0; i < recyclerView.getAdapter().getItemCount(); i++) {
            ViewHolder viewHolderAtPosition = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (viewHolderAtPosition != null) {
                if (i == selectIndex) {
                    viewHolderAtPosition.applySelectedStyle();
                } else {
                    viewHolderAtPosition.applyNormalStyle();
                }
            }
        }
    }
}

