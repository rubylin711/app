package com.prime.homeplus.settings;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsRecyclerViewAdapter extends RecyclerView.Adapter<SettingsRecyclerViewAdapter.ItemViewHolder> {
    private RecyclerView recyclerView;
    public ArrayList<SettingsItemData> settingsItems;

    public SettingsRecyclerViewAdapter(RecyclerView recyclerView, ArrayList<SettingsItemData> arrayList) {
        //Log.d("HomePlus-", "!!! " + arrayList.size());
        this.recyclerView = recyclerView;
        this.settingsItems = arrayList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Log.d("HomePlus-", "onCreateViewHolder !!! ");
        return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.settings_recycler_view_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        SettingsItemData settingsItem = settingsItems.get(position);

        //Log.d("HomePlus-", "onBindViewHolder !!! " + settingsItem.getTitle());
        holder.tvTitle.setText(settingsItem.getTitle());
        holder.tvSubtitle.setText(settingsItem.getSubtitle());
        holder.tvValue.setText(settingsItem.getValue());

        View view = settingsItem.getView();
        if (null != view) {
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(view);
                //Log.d(TAG, "onBindViewHolder: removeView");
            }
            if (holder.expandLayout.getChildCount() > 0) {
                holder.expandLayout.removeAllViews();
            }


            holder.expandLayout.addView(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

            if (settingsItem.isExpand()) {
                holder.expandLayout.setViewExpand(true);
            } else {
                holder.expandLayout.setViewExpand(false);
            }
        }

        holder.expandLayout.show();
    }

    @Override
    public int getItemCount() {
        return settingsItems.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout llLayout, llTitleLayout;
        private TextView tvTitle, tvSubtitle, tvValue, tvLineUp, tvLineDown;
        private ImageView ivArrow;
        private ExpandLayout expandLayout;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            this.llLayout = (LinearLayout) itemView.findViewById(R.id.item_layout);
            this.llTitleLayout = (LinearLayout) itemView.findViewById(R.id.item_title_layout);

            this.tvTitle = (TextView) itemView.findViewById(R.id.item_title);
            this.tvSubtitle = (TextView) itemView.findViewById(R.id.item_subtitle);
            this.tvValue = (TextView) itemView.findViewById(R.id.item_value);

            this.tvLineUp = (TextView) itemView.findViewById(R.id.item_line_up);
            this.tvLineDown = (TextView) itemView.findViewById(R.id.item_line_down);

            this.ivArrow = (ImageView) itemView.findViewById(R.id.item_arrow);

            this.expandLayout = (ExpandLayout) itemView.findViewById(R.id.item_expandable);

            onFocus(false);

            expandLayout.setExpandListener(new ExpandListener() {
                @Override
                public void onExpand(boolean hasExpand) {
                    onFocus(hasExpand);
                }
            });
        }

        private void onFocus(boolean focus) {
            //Log.d("HomePlus-", "ItemViewHolder onFocus " + focus);
            Context context = recyclerView.getContext();

            if (focus) {
                tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
                tvTitle.setTextColor(context.getResources().getColor(R.color.white));
                tvSubtitle.setVisibility(View.VISIBLE);
                //tvLineUp.setBackgroundColor(context.getResources().getColor(R.color.));
                tvLineUp.setVisibility(View.VISIBLE);
                tvLineDown.setVisibility(View.VISIBLE);
                tvLineUp.setBackgroundColor(context.getResources().getColor(R.color.colorWhiteOpacity40));
                tvLineDown.setBackgroundColor(context.getResources().getColor(R.color.colorWhiteOpacity40));

                ivArrow.setBackgroundResource(R.drawable.arrow_up);

                //llTitleLayout.setBackgroundColor(context.getResources().getColor(R.color.colorTextExit));
                this.itemView.setBackgroundColor(context.getResources().getColor(R.color.settings_layout_bg_f));
                this.llTitleLayout.setBackgroundColor(context.getResources().getColor(R.color.settings_title_layout_bg_f));
            } else {
                tvTitle.setTypeface(Typeface.DEFAULT);
                tvTitle.setTextColor(context.getResources().getColor(R.color.colorWhiteOpacity50));
                tvSubtitle.setVisibility(View.GONE);

                tvLineUp.setVisibility(View.INVISIBLE);
                tvLineDown.setVisibility(View.INVISIBLE);

                ivArrow.setBackgroundResource(R.drawable.arrow_down);

                //llTitleLayout.setBackgroundColor(Color.TRANSPARENT);
                this.itemView.setBackgroundColor(Color.TRANSPARENT);
                this.llTitleLayout.setBackgroundColor(context.getResources().getColor(R.color.settings_title_layout_bg_d));
            }

            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) this.llLayout.getLayoutParams();
            if (!focus) {
                layoutParams.height = context.getResources().getDimensionPixelSize(R.dimen.settings_title_height);
            } else if ((settingsItems.get(getAdapterPosition())).getTitle().equals(context.getString(R.string.parental_control))) {
                layoutParams.height = context.getResources().getDimensionPixelSize(R.dimen.settings_parental_control_unlock_expanded_height);
            } else if ((settingsItems.get(getAdapterPosition())).getTitle().equals(context.getString(R.string.system_information))) {
                layoutParams.height = context.getResources().getDimensionPixelSize(R.dimen.settings_info_expanded_height);
                tvLineUp.setBackgroundColor(context.getResources().getColor(R.color.colorTabSelect));
                tvLineDown.setBackgroundColor(context.getResources().getColor(R.color.colorTabSelect));
            } else {
                layoutParams.height = context.getResources().getDimensionPixelSize(R.dimen.settings_expanded_height);
            }
        }
    }
}
