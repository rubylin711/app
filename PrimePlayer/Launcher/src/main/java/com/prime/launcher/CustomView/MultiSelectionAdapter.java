package com.prime.launcher.CustomView;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.R;

import java.util.List;

public class MultiSelectionAdapter extends RecyclerView.Adapter<MultiSelectionAdapter.ViewHolder> {
    private final List<String> dataList;
    private OnCheckedChangeListener onCheckedChangeListener;

    public MultiSelectionAdapter(List<String> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_multi_selection, parent, false);

        ViewHolder viewHolder = new MultiSelectionAdapter.ViewHolder(itemView);
        itemView.setOnFocusChangeListener((v, hasFocus) -> {
            TextView text = viewHolder.checkboxText;
            if (hasFocus) {
                text.setTextColor(Color.WHITE);
                text.setSelected(true);
            } else {
                text.setTextColor(Color.GRAY);
                text.setSelected(false);
            }
        });

        itemView.setOnClickListener(v -> {
            viewHolder.setChecked(!viewHolder.checked);
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MultiSelectionAdapter.ViewHolder holder, int position) {
        holder.checkboxText.setText(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView checkboxText;
        private final ImageView checkboxImage;
        private boolean checked = false;

        public ViewHolder(View view) {
            super(view);

            checkboxText = view.findViewById(R.id.checkbox_text);
            checkboxImage = view.findViewById(R.id.checkbox_image);
        }

        public boolean isChecked() {
            return this.checked;
        }
        public void setChecked(boolean checked) {
            if (this.checked == checked) {
                return;
            }

            this.checked = checked;
            Drawable drawable;
            if (checked) {
                drawable = AppCompatResources.getDrawable(
                        itemView.getContext(),
                        R.drawable.icon_checkbox_on_red);
            } else {
                drawable = AppCompatResources.getDrawable(
                        itemView.getContext(),
                        R.drawable.icon_checkbox_off);
            }

            checkboxImage.setImageDrawable(drawable);
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChange(itemView, checked);
            }
        }
    }

    public interface OnCheckedChangeListener {
        void onCheckedChange(View itemView, boolean checked);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        onCheckedChangeListener = listener;
    }
}
