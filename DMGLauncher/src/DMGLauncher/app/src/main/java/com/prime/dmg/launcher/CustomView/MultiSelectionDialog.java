package com.prime.dmg.launcher.CustomView;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.leanback.widget.VerticalGridView;

import com.prime.dmg.launcher.BaseDialog;
import com.prime.dmg.launcher.R;
import com.prime.dtv.utils.TVMessage;

import java.util.ArrayList;
import java.util.List;

public class MultiSelectionDialog extends BaseDialog {

    private final MultiSelectionAdapter g_adapter;
    private VerticalGridView g_grid_view;
    private TextView g_checked_count_text;
    private int g_checked_count = 0;
    private final Context g_context;
    private OnDeleteConfirmListener onDeleteConfirmListener;
    public MultiSelectionDialog(@NonNull Context context, List<String> data) {
        super(context, R.style.Theme_DMGLauncher_DialogFullScreen);
        g_adapter = new MultiSelectionAdapter(data);
        g_context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.semi_transparent);
            getWindow().setWindowAnimations(R.style.Theme_DMGLauncher_DialogAnimation);
        }
        setContentView(R.layout.dialog_multi_selection);

        g_grid_view = findViewById(R.id.data_v_grid_view);
        g_grid_view.setNumColumns(2);
        g_grid_view.setAdapter(g_adapter);

        g_checked_count_text= findViewById(R.id.selected_num_text);
        g_adapter.setOnCheckedChangeListener((position, checked) -> {
            if (checked) {
                g_checked_count++;
            } else {
                g_checked_count--;
            }

            g_checked_count_text.setText(String.valueOf(g_checked_count));
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW
                && g_grid_view != null) {
            select_all();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            if (g_checked_count > 0) {
                EventDialog dialog = new EventDialog((AppCompatActivity) g_context);
                dialog.set_title_text(g_context.getString(R.string.pvr_event_dialog_record_mgmt_cancel_title, String.valueOf(g_checked_count)));
                dialog.set_confirm_text(g_context.getString(R.string.pvr_event_dialog_record_cancel_ok));
                dialog.set_cancel_text(g_context.getString(R.string.pvr_event_dialog_record_cancel_no));
                dialog.set_confirm_action(() -> {
                    if (onDeleteConfirmListener != null) {
                        onDeleteConfirmListener.onDeleteConfirm(get_checked_positions());
                    }
                    dismiss();
                });
                dialog.show();
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    private void select_all() {
        for (int i = 0 ; i < g_grid_view.getChildCount() ; i++) {
            View childView = g_grid_view.getChildAt(i);
            MultiSelectionAdapter.ViewHolder viewHolder
                    = (MultiSelectionAdapter.ViewHolder) g_grid_view.getChildViewHolder(childView);
            viewHolder.setChecked(true);
        }
    }

    private int get_checked_count() {
        int checked_count = 0;
        for (int i = 0 ; i < g_grid_view.getChildCount() ; i++) {
            View childView = g_grid_view.getChildAt(i);
            MultiSelectionAdapter.ViewHolder viewHolder
                    = (MultiSelectionAdapter.ViewHolder) g_grid_view.getChildViewHolder(childView);

            if (viewHolder.isChecked()) {
                checked_count++;
            }
        }

        return checked_count;
    }

    private List<Integer> get_checked_positions() {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0 ; i < g_grid_view.getChildCount() ; i++) {
            View childView = g_grid_view.getChildAt(i);
            MultiSelectionAdapter.ViewHolder viewHolder
                    = (MultiSelectionAdapter.ViewHolder) g_grid_view.getChildViewHolder(childView);

            if (viewHolder.isChecked()) {
                positions.add(i);
            }
        }

        return positions;
    }

    public void setOnDeleteConfirmListener(OnDeleteConfirmListener listener) {
        onDeleteConfirmListener = listener;
    }
    public interface OnDeleteConfirmListener {
        void onDeleteConfirm(List<Integer> deletePositions);
    }
}
