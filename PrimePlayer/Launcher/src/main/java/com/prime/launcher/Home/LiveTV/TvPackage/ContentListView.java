package com.prime.launcher.Home.LiveTV.TvPackage;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.HomeActivity;
import com.prime.launcher.R;
import com.prime.launcher.Utils.JsonParser.RecommendContent;

import java.lang.ref.WeakReference;
import java.util.List;

/** @noinspection CommentedOutCode*/
public class ContentListView extends RecyclerView implements Animator.AnimatorListener {

    String TAG = getClass().getSimpleName();

    public static final int DURATION_SCROLL         = 200;
    public static final int DURATION_SCROLL_QUICK   = 10;
    public static final int DURATION_ANIMATE        = 100;
    public static final int ITEM_VIEW_CACHE_SIZE    = 1000;
    public static final int SCROLL_DISTANCE_X       = 114;

    WeakReference<AppCompatActivity> g_ref;
    int g_dxRuntime;
    int g_dxShouldBe;
    int g_contentIndex;
    boolean g_isFadeOut;
    boolean g_isFadeIn;
    //int g_durationFadeIn;

    public ContentListView(@NonNull Context context) {
        super(context);
    }

    public ContentListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ContentListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init_all(TvPackageDialog tvPackageDialog, List<RecommendContent> contentList) {
        g_ref = new WeakReference<>(tvPackageDialog.get());
        g_isFadeOut = false;
        g_isFadeIn = false;
        g_dxRuntime = 0;
        g_contentIndex = 0;

        fade_with_duration(
                TvPackageDialog.DURATION_CONTENT_LIST_FADE_OUT,
                TvPackageDialog.DURATION_CONTENT_LIST_FADE_IN);

        if (null == getAdapter()) {
            Log.d(TAG, "init_all: init view & data");
            init_view_data(tvPackageDialog, contentList);
        }
        else {
            Log.d(TAG, "init_all: update view & data");
            update_view_data(contentList);
        }
    }

    public void init_view_data(TvPackageDialog tvPackageDialog, List<RecommendContent> contentList) {
        setAdapter(new ContentListAdapter(tvPackageDialog, contentList));
        setLayoutManager(new LinearLayoutManager(get(), LinearLayoutManager.HORIZONTAL, false));
        setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
        setHasFixedSize(true);
        addItemDecoration(new ContentListAdapter.ItemDecoration());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update_view_data(List<RecommendContent> contentList) {
        Log.d(TAG, "update_view_data: content list size " + contentList.size());
        // scroll to first
        scrollToPosition(0);
        // update list view
        ContentListAdapter adapter = (ContentListAdapter) getAdapter();
        if (adapter != null)
            adapter.update_content_list(contentList);
    }

    /*@Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        int contentWidth = 130;
        int position = get_position();
        // dx runtime
        g_dxRuntime += dx;
        // dx should scroll
        g_dxShouldBe = SCROLL_DISTANCE_X * position;
        if (position > 5)
            g_dxShouldBe = SCROLL_DISTANCE_X * position + (contentWidth * (position - 5));
        if (is_last_item())
            g_dxShouldBe = Integer.MAX_VALUE;
        // re-scroll
        smoothScrollBy(g_dxShouldBe - g_dxRuntime, 0, null, DURATION_SCROLL);
        //Log.e(TAG, "onScrolled: [dy] " + g_dxRuntime + " / " + g_dxShouldBe + ", [position] " + position + ", [count] " + get_count());
    }*/

    @Override
    public void onAnimationStart(@NonNull Animator animation) {
        // onAnimationStart
    }

    @Override
    public void onAnimationEnd(@NonNull Animator animation) {
        if (g_isFadeOut) {
            Log.d(TAG, "onAnimationEnd: fade in duration " + TvPackageDialog.DURATION_CONTENT_LIST_FADE_IN);
            g_isFadeOut = false;
            fade_in(TvPackageDialog.DURATION_CONTENT_LIST_FADE_IN);
        }
        else {
            Log.d(TAG, "onAnimationEnd: fade in end");
            g_isFadeIn = false;
        }
    }

    @Override
    public void onAnimationCancel(@NonNull Animator animation) {
        // onAnimationCancel
    }

    @Override
    public void onAnimationRepeat(@NonNull Animator animation) {
       // onAnimationRepeat
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public int get_item_width() {
        if (!hasFocus())
            return 0;
        View focusedView = getFocusedChild();
        return focusedView.getWidth();
    }

    public int get_position() {
        //noinspection ConstantValue
        if (true) {
            return g_contentIndex;
        }
        if (!hasFocus())
            return 0;
        View focusedView = getFocusedChild();
        return getChildAdapterPosition(focusedView);
    }

    public View get_position_view() {
        return findViewWithTag(get_position());
    }

    public int get_count() {
        ContentListAdapter adapter = (ContentListAdapter) getAdapter();
        if (adapter == null)
            return 0;
        return adapter.getItemCount();
    }

    public View get_next_item(int keyCode) {
        if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode) {
            if (++g_contentIndex >= get_count()) {
                g_contentIndex = get_count() - 1;
            }
        }
        if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode) {
            if (--g_contentIndex < 0)
                g_contentIndex = 0;
        }
        return findViewWithTag(g_contentIndex);//getChildAt(g_contentIndex);
    }

    public boolean is_first_item() {
        return get_position() == 0;
    }

    public boolean is_last_item() {
        return get_position() == get_count() - 1;
    }

    public boolean is_fade_in_finished() {
        return !g_isFadeIn;
    }

    public void set_content_selected(View itemView) {
        if (null == itemView) {
            Log.w(TAG, "set_content_selected: null content view");
            return;
        }
        scale_2x(itemView);
        View focusView = itemView.findViewById(R.id.lo_item_layer_focus);
        ImageView posterView = itemView.findViewById(R.id.lo_item_layer_poster);
        TextView programView = itemView.findViewById(R.id.lo_content_layer_program);

        focusView.setVisibility(View.VISIBLE);
        posterView.setVisibility(View.VISIBLE);
        programView.setVisibility(View.VISIBLE);
    }

    public void set_content_unselected(View itemView) {
        if (null == itemView) {
            Log.w(TAG, "set_content_unselected: null content view");
            return;
        }
        View focusView = itemView.findViewById(R.id.lo_item_layer_focus);
        ImageView posterView = itemView.findViewById(R.id.lo_item_layer_poster);
        //TextView programView = itemView.findViewById(R.id.lo_content_layer_program);

        focusView.setVisibility(View.GONE);
        posterView.setVisibility(View.GONE);
        //programView.setVisibility(View.GONE);
        scale_1x(itemView);
    }

    public void scale_2x(View itemView) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        params.width = get().getResources().getDimensionPixelSize(R.dimen.content_item_layer_width_scale_up);
        params.height = get().getResources().getDimensionPixelSize(R.dimen.content_item_layer_height_scale_up);
        params.topMargin = 0;
        itemView.setLayoutParams(params);
    }

    public void scale_1x(View itemView) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        params.width = get().getResources().getDimensionPixelSize(R.dimen.content_item_layer_width);
        params.height = get().getResources().getDimensionPixelSize(R.dimen.content_item_layer_height);
        params.topMargin = get().getResources().getDimensionPixelSize(R.dimen.content_item_layer_marginTop);
        itemView.setLayoutParams(params);
    }

    public void scroll_left_right(int keyCode) {

        if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode)
            smoothScrollBy(SCROLL_DISTANCE_X, 0, null, DURATION_SCROLL);

        if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode)
            smoothScrollBy(-SCROLL_DISTANCE_X, 0, null, DURATION_SCROLL);
    }

    public void press_key(int keyCode) {
        // LEFT or RIGHT
        if (press_left_right(keyCode))
            return;
        // OK
        if (press_ok(keyCode))
            return;
    }

    public boolean press_left_right(int keyCode) {
        if (KeyEvent.KEYCODE_DPAD_LEFT != keyCode && KeyEvent.KEYCODE_DPAD_RIGHT != keyCode)
            return false;
        if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode && is_first_item())
            return false;
        if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode && is_last_item())
            return false;

        int backup = g_contentIndex;
        View contentCurrent = findViewWithTag(g_contentIndex);
        View contentNext = get_next_item(keyCode);

        if (contentCurrent == null || contentNext == null) {
            g_contentIndex = backup;
            return true;
        }

        Log.d(TAG, "press_left_right: [content index] " + get_position() + ", current " + contentCurrent.getTag() + ", next " + contentNext.getTag());
        set_content_unselected(contentCurrent);
        set_content_selected(contentNext);
        scroll_to_center(contentNext, true); //scroll_left_right(keyCode);
        return true;
    }

    public boolean press_ok(int keyCode) {
        if (KeyEvent.KEYCODE_DPAD_CENTER != keyCode)
            return false;
        View contentCurrent = findViewWithTag(g_contentIndex);
        contentCurrent.performClick();
        return true;
    }

    public void fade_with_duration(int durationFadeOut, int durationFadeIn) {
        Log.d(TAG, "fade_with_duration: fade out " + durationFadeOut + ", fade in " + durationFadeIn);
        float INVISIBLE = 0;
        animate().setDuration(durationFadeOut)
                .alpha(INVISIBLE)
                .setListener(this)
                .start();
        g_isFadeOut = true;
        //g_durationFadeIn = durationFadeIn;
    }

    public void fade_in(int duration) {
        //Log.d(TAG, "fade_in: duration " + duration);
        float VISIBLE = 1;
        g_isFadeIn = true;
        animate().setDuration(duration)
                .alpha(VISIBLE)
                .start();
    }

    public void scroll_to_center(View itemView, boolean hasFocus) { // scroll to middle, scroll to center, move to center, move to middle
        if (!hasFocus)
            return;
        ContentListView contentListView = (ContentListView) itemView.getParent();

        int[] location = new int[2];
        itemView.getLocationOnScreen(location);

        int[] locationParent = new int[2];
        contentListView.getLocationOnScreen(locationParent);

        int viewCenterX = location[0] + itemView.getWidth() / 2;
        int screenCenterX = locationParent[0] + contentListView.getWidth() / 2;
        int offsetX = viewCenterX - screenCenterX;

        contentListView.smoothScrollBy(offsetX, 0);
        /*if (offsetX < 0)
            Log.d(TAG, "scroll_to_center: move left, [offset] " + offsetX);
        else
            Log.d(TAG, "scroll_to_center: move right, [offset] " + offsetX);*/
    }
}
