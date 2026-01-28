package com.prime.dmg.launcher.Home.LiveTV.TvPackage;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.JsonParser.RecommendPackage;

import java.lang.ref.WeakReference;
import java.util.List;

/** @noinspection CommentedOutCode*/
public class TypeListAdapter extends RecyclerView.Adapter<TypeListAdapter.ViewHolder> implements Animator.AnimatorListener {

    String TAG = getClass().getSimpleName();

    WeakReference<AppCompatActivity> g_ref;
    //Handler g_handler;
    TvPackageDialog g_tvPackageDialog;
    List<RecommendPackage> g_recommendPackages;
    int g_keyCode;
    int g_itemHeight;
    boolean g_animationEnd;

    public TypeListAdapter(TvPackageDialog tvPackageDialog, List<RecommendPackage> recommendPackages) {
        g_ref = new WeakReference<>(tvPackageDialog.get());
        //g_handler = new Handler(Looper.getMainLooper());
        g_tvPackageDialog = tvPackageDialog;
        g_recommendPackages = recommendPackages;
        g_animationEnd = true;
        g_itemHeight = 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_recommend_package_type, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TypeListAdapter.ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        init_item_view(holder, position);
        init_item_layout(holder, position);
        on_focus_item(holder, position);
        on_key_item(holder, position);
    }

    @Override
    public void onAnimationStart(@NonNull Animator animation) {
        // animation start
    }

    @Override
    public void onAnimationEnd(@NonNull Animator animation) {
        g_animationEnd = true;

        /*boolean isKeyDown = g_tvPackageDialog.is_key_down();
        if (isKeyDown) {
            g_handler.removeCallbacksAndMessages(null);
            g_handler.postDelayed(() -> {
                Log.e(TAG, "onAnimationEnd: update content list");
                g_tvPackageDialog.update_content_list();
            }, 10);
        }*/
    }

    @Override
    public void onAnimationCancel(@NonNull Animator animation) {
        // animation cancel
    }

    @Override
    public void onAnimationRepeat(@NonNull Animator animation) {
        // animation repeat
    }

    public void on_focus_item(ViewHolder holder, int position) {
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (g_itemHeight <= 0) {
                g_itemHeight = holder.itemView.getHeight() + get().getResources().getDimensionPixelSize(R.dimen.recommend_type_item_margin);
            }
            scale_animation(hasFocus, holder);
            scroll_up_down(hasFocus, position);
            move_to_middle(holder.itemView, hasFocus);
        });
    }

    public void on_key_item(ViewHolder holder, int position) {
        holder.itemView.setOnKeyListener((v, keyCode, event) -> {

            if (KeyEvent.ACTION_UP == event.getAction())
                return false;

            if (!is_animation_end()) {
                Log.w(TAG, "on_key_item: Animation not End");
                return true;
            }
            
            g_keyCode = 0;
            if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode ||
                KeyEvent.KEYCODE_DPAD_UP == keyCode) {
                g_keyCode = keyCode;
            }

            return false;
        });
    }

    @SuppressLint("SetTextI18n")
    public void init_item_view(ViewHolder holder, int position) {
        TextView typeName = holder.itemView.findViewById(R.id.lo_type_name);
        typeName.setText(g_recommendPackages.get(position).get_package_name());

        View purchaseView = holder.itemView.findViewById(R.id.lo_type_purchase_layer);
        purchaseView.setVisibility(is_purchased() ? View.VISIBLE : View.INVISIBLE);
    }

    public void init_item_layout(ViewHolder holder, int position) {
        if (position == 0) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.topMargin = 20;
            holder.itemView.setLayoutParams(params);
        }
        if (position == getItemCount() - 1) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.bottomMargin = 28;
            holder.itemView.setLayoutParams(params);
        }
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    @Override
    public int getItemCount() {
        return g_recommendPackages.size();
    }

    public int get_item_height() {
        return g_itemHeight;
    }

    public boolean is_purchased() {
        return false;
    }

    public boolean is_animation_end() {
        return g_animationEnd;
    }

    public void scale_animation(boolean hasFocus, ViewHolder holder) {
        // start animation
        if (!hasFocus && is_animation_end())
            scale_down(holder);
        else
            scale_up(holder);

        if (is_animation_end())
            g_animationEnd = false;
    }
    
    public void scale_up(ViewHolder holder) {
        TextView typeName = holder.itemView.findViewById(R.id.lo_type_name);

        float scaleFactor = typeName.getText().length() > 4 ? 1.3f : 1.5f;
        int parentWidth = ((View) holder.itemView.getParent()).getWidth();
        int scaledWidth = (int) (holder.itemView.getWidth() * scaleFactor);
        float translationX = Math.max(0, (scaledWidth - parentWidth) / 2);

        holder.itemView.animate().setListener(this);
        holder.itemView.animate().scaleX(scaleFactor).scaleY(scaleFactor).translationXBy(translationX).setDuration(TypeListView.DURATION_ANIMATE).start();
        typeName.setTextColor(get().getResources().getColor(R.color.pvr_red_color, null));
    }

    public void scale_down(ViewHolder holder) {
        TextView typeName = holder.itemView.findViewById(R.id.lo_type_name);

        float scaleFactor = typeName.getText().length() > 4 ? 1.3f : 1.5f;
        int parentWidth = ((View) holder.itemView.getParent()).getWidth();
        int scaledWidth = (int) (holder.itemView.getWidth() * scaleFactor);
        float translationX = Math.max(0, (scaledWidth - parentWidth) / 2);

        holder.itemView.animate().setListener(null);
        holder.itemView.animate().scaleX(1.0f).scaleY(1.0f).translationXBy(-translationX).setDuration(TypeListView.DURATION_ANIMATE).start();
        typeName.setTextColor(Color.WHITE);
    }

    public void scroll_up_down(boolean hasFocus, int position) {
        TypeListView typeListView = g_tvPackageDialog.findViewById(R.id.lo_tv_package_type_list);
        typeListView.scroll_up_down(hasFocus, position, g_keyCode);
    }

    public void move_to_middle(View itemView, boolean hasFocus) { // scroll to middle, scroll to center, move to center, move to middle
        if (!hasFocus)
            return;
        TypeListView typeListView = (TypeListView) itemView.getParent();

        int[] location = new int[2];
        itemView.getLocationOnScreen(location);

        int[] locationParent = new int[2];
        typeListView.getLocationOnScreen(locationParent);

        int viewCenterY = location[1] + itemView.getHeight() / 2;
        int screenCenterY = locationParent[1] + typeListView.getHeight() / 2;
        int offsetY = viewCenterY - screenCenterY;

        typeListView.smoothScrollBy(0, offsetY);
        /*if (offsetX < 0)
            Log.d(TAG, "move_to_middle: move left, [offset] " + offsetX);
        else
            Log.d(TAG, "move_to_middle: move right, [offset] " + offsetX);*/
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
