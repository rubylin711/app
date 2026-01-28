package com.prime.launcher.Home.Hotkey;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.prime.launcher.Home.LiveTV.MiniEPG;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.R;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class HotkeyGenre extends DialogFragment {

    String TAG = HotkeyGenre.class.getSimpleName();

    int[] GENRE_ITEMS = {R.string.genre_all, R.string.genre_kids, R.string.genre_education, R.string.genre_news, R.string.genre_movie, R.string.genre_variety, R.string.genre_music, R.string.genre_adult, R.string.genre_sports, R.string.genre_religion, R.string.genre_uhd, R.string.genre_shopping};

    WeakReference<AppCompatActivity> g_ref;
    MiniEPG g_miniEPG;
    long g_currTimeMs;

    public HotkeyGenre(MiniEPG miniEPG) {
        g_ref = new WeakReference<>(miniEPG.get());
        g_miniEPG = miniEPG;
        g_currTimeMs = 0;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // full screen
        setStyle(DialogFragment.STYLE_NORMAL,
                 android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

        // without the title
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // remove background shadow
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // custom layout
        View rootView = inflater.inflate(R.layout.live_tv_hotkey_genre_dialog,
                                         container, false);
        init_menu(rootView);
        return rootView;
    }

    public void init_menu(View root) {
        TextView     genreTitle = root.findViewById(R.id.lo_dialog_genre_title);
        LinearLayout genreList  = root.findViewById(R.id.lo_dialog_genre_list_root);

        genreTitle.setText(R.string.mini_epg_genre);
        genreList.removeAllViews();

        for (int genreId : GENRE_ITEMS) {
            TextView genreView = get_genre_view(genreId);
            on_click_genre(genreView, root);
            on_key_genre(genreView, root);
            genreList.addView(genreView);
        }
        genreList.requestFocus();

        init_menu_position(root);
    }

    public void init_menu_position(View root) {
        ScrollView   genreScroll = root.findViewById(R.id.lo_dialog_genre_scroll_list);
        LinearLayout genreRoot   = root.findViewById(R.id.lo_dialog_genre_list_root);
        int  index = get_genre_index();
        View child = genreRoot.getChildAt(index);
        int  dy    = index > 2 ? (index - 2) * get().getResources().getDimensionPixelSize(R.dimen.live_tv_dialog_item_height) : 0;

        new Handler().postDelayed(()->{
            genreScroll.smoothScrollBy(0, dy);
            child.requestFocus();
        }, 100);
    }

    public void on_click_genre(TextView genreView, View root) {
        genreView.setOnClickListener(v -> {
            String genre = (String) genreView.getText();
            g_miniEPG.on_click_genre(genre);
            dismiss();
        });
    }

    public void on_key_genre(TextView genreView, View root) {
        ScrollView   genreScroller = root.findViewById(R.id.lo_dialog_genre_scroll_list);
        LinearLayout genreList     = root.findViewById(R.id.lo_dialog_genre_list_root);

        genreView.setOnKeyListener((v, keyCode, event) -> {

            if (KeyEvent.ACTION_UP == event.getAction())
                return false;

            if (System.currentTimeMillis() - g_currTimeMs < 100)
                return true;
            g_currTimeMs = System.currentTimeMillis();

            int index = get_genre_index(genreView, root);
            int dy    = get().getResources().getDimensionPixelSize(R.dimen.live_tv_dialog_item_height);
            int bound = genreList.getChildCount() - 3;

            if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode) {
                Log.d(TAG, "on_key: DOWN");
                if (index >= 2)
                    genreScroller.smoothScrollBy(0, dy);
            }
            if (KeyEvent.KEYCODE_DPAD_UP == keyCode) {
                Log.d(TAG, "on_key: UP");
                if (index <= bound)
                    genreScroller.smoothScrollBy(0, -dy);
            }
            return false;
        });
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public TextView get_genre_view(int resId) {
        TextView genreView = new TextView(get());
        genreView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                get().getResources().getDimensionPixelSize(R.dimen.live_tv_dialog_item_height)
        ));
        genreView.setText(resId);
        genreView.setTextSize(get().getResources().getDimensionPixelSize(R.dimen.live_tv_dialog_item_text_size) / get().getResources().getDisplayMetrics().density);
        genreView.setFocusable(true);
        genreView.setGravity(Gravity.CENTER);
        genreView.setOnFocusChangeListener((v, hasFocus) -> {
            genreView.setTextColor(ContextCompat.getColor(get(),
                    hasFocus ? R.color.black : R.color.white));
            genreView.setBackgroundColor(ContextCompat.getColor(get(),
                    hasFocus ? R.color.pvr_red_color : R.color.trans));
            genreView.setTypeface(null,
                    hasFocus ? Typeface.BOLD : Typeface.NORMAL);
        });

        return genreView;
    }

    public int get_genre_index() {
        String miniEpgGenre = g_miniEPG.get_genre();
        Log.e(TAG, "get_genre_index: " + miniEpgGenre);
        for (int index = 0; index < GENRE_ITEMS.length; index++) {
            String currGenre = get().getString(GENRE_ITEMS[index]);

            if (miniEpgGenre.equals(currGenre))
                return index;
        }

        return 0;
    }

    public int get_genre_index(TextView genreView, View root) {
        LinearLayout genreList;
        View child;

        genreList = root.findViewById(R.id.lo_dialog_genre_list_root);
        if (genreList == null)
            return 0;

        for (int index = 0; index < genreList.getChildCount(); index++) {
            child = genreList.getChildAt(index);
            if (child == null)
                return 0;
            if (child.equals(genreView))
                return index;
        }
        return 0;
    }

    public void show() {
        super.show(get().getSupportFragmentManager(), TAG);
    }

}
