package com.prime.homeplus.tv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.data.PvrMenuData;

import java.util.ArrayList;
import java.util.List;

public class PvrMenuAdapter extends RecyclerView.Adapter<PvrMenuAdapter.ViewHolder> {
    private static final String TAG = "PvrMenuAdapter";
    private RecyclerView recyclerView;
    private List<PvrMenuData> pvrMenuList;
    private OnItemFocusedListener focusedListener;
    private OnItemSelectedListener selectedListener;
    private Context context;
    private boolean isMenuNameGone = false;
    private int lastFocusedIndex = -1;

    public PvrMenuAdapter(RecyclerView recyclerView, List<PvrMenuData> pvrMenuList) {
        this.recyclerView = recyclerView;
        this.context = recyclerView.getContext();
        this.pvrMenuList = new ArrayList<>();
        if (pvrMenuList != null) {
            this.pvrMenuList.addAll(pvrMenuList);
        }
    }

    public interface OnItemFocusedListener {
        void onItemFocused(PvrMenuData.PvrMenuState pvrMenuState);
    }

    public void setOnItemFocusedListener(OnItemFocusedListener listener) {
        this.focusedListener = listener;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(PvrMenuData.PvrMenuState pvrMenuState);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.selectedListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llPvrMenu;
        ImageView ivPvrMenuIcon;
        TextView tvPvrMenuName;

        public ViewHolder(View itemView) {
            super(itemView);
            llPvrMenu = itemView.findViewById(R.id.llPvrMenu);
            ivPvrMenuIcon = itemView.findViewById(R.id.ivPvrMenuIcon);
            tvPvrMenuName = itemView.findViewById(R.id.tvPvrMenuName);
        }
    }

    @NonNull
    @Override
    public PvrMenuAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pvr_menu_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PvrMenuAdapter.ViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder position:" + position);
        PvrMenuData pvrMenu = pvrMenuList.get(position);
        holder.ivPvrMenuIcon.setImageResource(pvrMenu.getIconResId());
        holder.tvPvrMenuName.setText(pvrMenu.getMenuName());
        holder.tvPvrMenuName.setVisibility(isMenuNameGone ? View.GONE : View.VISIBLE);

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if ( focusedListener != null && lastFocusedIndex != position) {
                    focusedListener.onItemFocused(pvrMenu.getPvrMenuState());
                }
                lastFocusedIndex = position;

                holder.llPvrMenu.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
                holder.tvPvrMenuName.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.tvPvrMenuName.setTypeface(null, Typeface.BOLD);
            } else {
                holder.llPvrMenu.setBackgroundColor(Color.TRANSPARENT);
                holder.tvPvrMenuName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity75));
                holder.tvPvrMenuName.setTypeface(null, Typeface.NORMAL);
            }
        });

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            Log.d(TAG, "onBindViewHolder setOnKeyListener position:" + position +
                    ", action:" + event.getAction() + ", keyCode:" + keyCode);
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (selectedListener != null) {
                    selectedListener.onItemSelected(pvrMenu.getPvrMenuState());
                }
                return true;
            }

            if ((keyCode == KeyEvent.KEYCODE_DPAD_UP && position == 0) ||
                    (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) ||
                    (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == pvrMenuList.size() - 1)) {
                return true;
            }

            return false;
        });
    }

    @Override
    public int getItemCount() {
        return pvrMenuList != null ? pvrMenuList.size() : 0;
    }

    public void focusTo(PvrMenuData.PvrMenuState state) {
        if (!pvrMenuList.isEmpty()) {
            int focusIndex = 0;
            for (int i = 0; i < pvrMenuList.size(); i++) {
                if (pvrMenuList.get(i).getPvrMenuState() == state) {
                    focusIndex = i;
                    break;
                }
            }

            final int finalFocusIndex = focusIndex;
            recyclerView.scrollToPosition(finalFocusIndex);
            recyclerView.postDelayed(() -> {
                View firstItem = recyclerView.getLayoutManager().findViewByPosition(finalFocusIndex);
                if (firstItem != null) {
                    firstItem.requestFocus();
                }
            }, 0);
        }
    }

    public void setMenuNameGone() {
        isMenuNameGone = true;
        notifyDataSetChanged();
    }

    public void setMenuNameVisible() {
        isMenuNameGone = false;
        notifyDataSetChanged();
    }
}

