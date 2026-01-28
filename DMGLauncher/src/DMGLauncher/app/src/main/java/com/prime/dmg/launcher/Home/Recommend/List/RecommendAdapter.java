package com.prime.dmg.launcher.Home.Recommend.List;

import static android.view.KeyEvent.*;
import static androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.Home.Recommend.Activity.SideMenuActivity;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.ActivityUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** @noinspection CommentedOutCode*/
public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.ViewHolder> {

    String TAG = getClass().getSimpleName();

    // handle
    WeakReference<AppCompatActivity> g_ref;
    Handler gRetryHandler;
    Runnable gRetryDraw;
    ViewHolder.NetworkChangeReceiver gNetworkChangeReceiver;

    // data
    public static String gAppName;
    List<RecommendItem> g_itemList;
    int g_listViewID;
    long g_previousMs;

    public RecommendAdapter(AppCompatActivity activity, int listViewID, List<RecommendItem> itemList) {
        g_ref        = new WeakReference<>(activity);
        g_listViewID = listViewID;
        g_itemList   = new ArrayList<>();
        g_previousMs = 0;
        gRetryHandler = new Handler(Looper.getMainLooper());
        init_list(itemList);
    }

    /** @noinspection ClassEscapesDefinedScope*/
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resID = is_in_home()
                ? R.layout.list_view_item_of_home
                : R.layout.list_view_item_of_side_menu;

        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(resID, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.set_thumbnail();
        holder.set_label();
        holder.set_layout_params();
        holder.set_next_focus();
        holder.on_focus_item();
        holder.on_click_item();
        holder.on_key_item();
    }

    @Override
    public int getItemCount() {
        return g_itemList.size();
    }

    public AppCompatActivity get() {
        return g_ref.get();
    }

    public static boolean is_clicked_all_app() {
        boolean isClicked = gAppName != null && gAppName.equals(SideMenuActivity.APP_NAME);
        gAppName = "";
        return isClicked;
    }

    public void init_list(List<RecommendItem> itemList) {

        if (init_list_of_populars(itemList))
            Log.d(TAG, "init_list: POPULARS");

        if (init_list_of_applications(itemList))
            Log.d(TAG, "init_list: APPLICATIONS");

        if (init_list_of_apps_games(itemList))
            Log.d(TAG, "init_list: APPs & GAMEs");

    }

    public boolean init_list_of_populars(List<RecommendItem> programs) {
        if (!is_popular_programs())
            return false;

        g_itemList.addAll(programs);

        return true;
    }

    public boolean init_list_of_applications(List<RecommendItem> applications) {

        if (!is_home_applications() &&
            !is_side_menu_applications())
            return false;

        for (RecommendItem item : applications) {
            //if (is_google(item))
            //    continue;
            if (is_side_menu_applications() &&
                is_side_menu_activity(item))
                continue;
            g_itemList.add(item);
        }

        // move Netflix to first
        //RecommendItem app_Netflix = find_app(g_itemList, HomeActivity.PKG_NETFLIX);
        //g_itemList.remove(app_Netflix);
        //g_itemList.add(0, app_Netflix);

        return true;
    }

    public boolean init_list_of_apps_games(List<RecommendItem> apps_and_games) {
        if (!is_home_apps_and_games() &&
            !is_side_menu_apps_and_games())
            return false;

        g_itemList.clear();
        for (RecommendItem item : apps_and_games) {
            if (is_side_menu_activity(item))
                continue;
            g_itemList.add(item);
        }

        return true;
    }

    public boolean is_popular_programs() {
        return g_listViewID == R.id.lo_home_rcv_popular;
    }

    public boolean is_home_applications() {
        return g_listViewID == R.id.lo_home_rcv_apps;
    }

    public boolean is_home_apps_and_games() {
        return g_listViewID == R.id.lo_home_rcv_apps_games;
    }

    public boolean is_side_menu_applications() {
        return g_listViewID == R.id.lo_side_menu_app_list;
    }

    public boolean is_side_menu_apps_and_games() {
        return g_listViewID == R.id.lo_side_menu_app_game_grid;
    }

    public boolean is_in_home() {
        return g_listViewID == R.id.lo_home_rcv_popular ||
               g_listViewID == R.id.lo_home_rcv_apps    ||
               g_listViewID == R.id.lo_home_rcv_apps_games;
    }

    public boolean is_in_side_menu() {
        return g_listViewID == R.id.lo_side_menu_app_game_grid ||
               g_listViewID == R.id.lo_side_menu_app_list;
    }

    public boolean is_grid() {
        return g_listViewID == R.id.lo_side_menu_app_game_grid;
    }

    public boolean is_google(RecommendItem item) {
        return item.g_pkg.equals(HomeActivity.PKG_GOOGLE_PLAY) ||
               item.g_pkg.equals(HomeActivity.PKG_GOOGLE_MOVIES) ||
               item.g_pkg.equals(HomeActivity.PKG_GOOGLE_YOUTUBE) ||
               item.g_pkg.equals(HomeActivity.PKG_GOOGLE_YOUTUBE_MUSIC) ||
               item.g_pkg.equals(HomeActivity.PKG_GOOGLE_GAMES);
    }

    public boolean is_side_menu_activity(RecommendItem item) {
        return item.get_app_name().equals(SideMenuActivity.APP_NAME);
    }

    public boolean is_app_existed(String pkgName) {
        for (RecommendItem item : g_itemList) {
            if (pkgName.equals(item.get_package_name()))
                return true;
        }
        return false;
    }

    public RecommendItem find_app(List<RecommendItem> itemList, String pkg_name) {
        for (RecommendItem item : itemList) {
            if (pkg_name.equals(item.g_pkg))
                return item;
        }
        return null;
    }

    public void cancel_retry_draw() {
        if (gRetryHandler != null) {
            gRetryHandler.removeCallbacksAndMessages(null);
            gRetryDraw = null;
        }

        if (gNetworkChangeReceiver != null) {
            try {
                if (get() != null && !get().isFinishing() && !get().isDestroyed())
                    get().unregisterReceiver(gNetworkChangeReceiver);
            }
            catch (Exception e) {
                Log.e(TAG, "cancel_retry_draw: " + e.getMessage());
            }
            gNetworkChangeReceiver = null;
        }
    }

    public void add_local_app(List<RecommendItem> newAppList, String pkgName) {
        List<RecommendItem> recommendList = new ArrayList<>();
        RecommendItem newApp = null;
        int pos_insert = -1;

        if (newAppList == null || newAppList.isEmpty() || pkgName == null) {
            Log.e(TAG, "add_local_app: null list or pkg name");
            return;
        }
        for (RecommendItem item : g_itemList) {
            if (pkgName.equals(item.get_package_name())) {
                Log.e(TAG, "add_local_app: already have app");
                return;
            }
        }
        for (RecommendItem item : newAppList) {
            if (is_side_menu_activity(item))
                continue;
            recommendList.add(item);
        }
        for (RecommendItem item : recommendList) {
            if (pkgName.equals(item.get_package_name())) {
                pos_insert = recommendList.indexOf(item);
                newApp = recommendList.get(pos_insert);
                break;
            }
        }

        if (pos_insert != -1 && newApp != null) {
            RecommendListView listView = get().findViewById(g_listViewID);
            if (listView.g_focused_pos >= pos_insert)
                listView.g_focused_pos++;
            g_itemList.add(pos_insert, newApp);
            notifyItemInserted(pos_insert);
            bindViewHolder(0);
            bindViewHolder(1);
            bindViewHolder(getItemCount() - 1);
            bindViewHolder(getItemCount() - 2);
        }

    }

    public void remove_local_app(List<RecommendItem> newAppList, String pkgName) {
        int pos_remove = -1;

        if (newAppList == null || newAppList.isEmpty() || pkgName == null) {
            Log.e(TAG, "remove_local_app: null list or pkg name");
            return;
        }

        for (RecommendItem item : g_itemList) {
            if (pkgName.equals(item.get_package_name())) {
                pos_remove = g_itemList.indexOf(item);
                break;
            }
        }

        if (pos_remove != -1) {
            RecommendListView listView = get().findViewById(g_listViewID);
            if (listView.g_focused_pos >= pos_remove)
                listView.g_focused_pos--;
            g_itemList.remove(pos_remove);
            notifyItemRemoved(pos_remove);
            bindViewHolder(0);
            bindViewHolder(1);
            bindViewHolder(getItemCount() - 1);
            bindViewHolder(getItemCount() - 2);
        }
    }

    public void bindViewHolder(int position) {
        RecommendListView listView = get().findViewById(g_listViewID);
        ViewHolder holder = (ViewHolder) listView.findViewHolderForAdapterPosition(position);
        if (holder != null)
            bindViewHolder(holder, position);
    }

    /** @noinspection ConstantValue*/
    class ViewHolder extends RecyclerView.ViewHolder {

        //String TAG = getClass().getSimpleName();

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void set_thumbnail() {
            RecommendItem item = g_itemList.get(getAbsoluteAdapterPosition());

            if (false) {
                Log.e(TAG, "set_thumbnail: [popular] " + is_popular_list() + ", [application] " + is_application_list() + ", [apps & games] " + is_apps_games_list());
                Log.e(TAG, "set_thumbnail: position = " + getAbsoluteAdapterPosition());
                Log.e(TAG, "set_thumbnail: drawable = " + item.get_thumbnail_drawable());
                Log.e(TAG, "set_thumbnail: url      = " + item.get_thumbnail_url());
                Log.e(TAG, "set_thumbnail: res id   = " + item.get_thumbnail_res_id());
            }

            // thumbnail
            ImageView thumbnail = itemView.findViewById(R.id.lo_rcv_item_thumbnail);
            draw_thumbnail(thumbnail, item.get_thumbnail_drawable());
            draw_thumbnail(thumbnail, item.get_thumbnail_url());
            draw_thumbnail(thumbnail, item.get_thumbnail_res_id());
        }

        public void set_label() {
            //Log.d(TAG, "set_label: " + getAdapterPosition());
            RecommendItem item = g_itemList.get(getAbsoluteAdapterPosition());

            // label
            TextView label = itemView.findViewById(R.id.lo_rcv_item_label);
            label.setText(item.get_label());
            //Log.d(TAG, "set_label: label = "+ item.g_label);

            if (g_listViewID == R.id.lo_home_rcv_apps ||
                g_listViewID == R.id.lo_home_rcv_apps_games) {
                //Log.d(TAG, "set_label: hide label");
                label.setVisibility(View.GONE);
            }
        }

        public void set_layout_params() {

            int position = getAbsoluteAdapterPosition();
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();

            if (is_in_side_menu()) {
                if (is_grid()) {
                    params.bottomMargin = (position >= getItemCount() - RecommendListView.GRID_COLUMN_NUM)
                            ? get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_margin_y)
                            : 0;
                    itemView.setLayoutParams(params);
                    return;
                }
                params.rightMargin = (position == getItemCount() - 1)
                    ? get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_margin_x_2)
                    : get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_margin_x);
                itemView.setLayoutParams(params);
                return;
            }

            params.leftMargin = (position == 0)
                    ? get().getResources().getDimensionPixelSize(R.dimen.home_list_title_margin_start)
                    : 0;
            params.width = (position == getItemCount() - 1)
                    ? get().getResources().getDimensionPixelSize(R.dimen.home_list_rcv_block_out_width)
                    : WRAP_CONTENT;
            params.width = (getItemCount() == 1) ? WRAP_CONTENT : params.width;
            itemView.setLayoutParams(params);
        }

        public void set_next_focus() {

            if (is_in_side_menu())
                return;

            itemView.setNextFocusUpId(R.id.lo_home_live_tv_frame);
        }

        public void on_focus_item() {
            itemView.setOnFocusChangeListener((view, isFocus) -> {
                int position = getAbsoluteAdapterPosition();
                if (position == -1) {
                    RecommendListView listView = (RecommendListView) view.getParent();
                    position = listView.get_position();
                }

                if (isFocus) {
                    RecommendItem item = g_itemList.get(position);
                    Log.d(TAG, "on_focus_item: [position] " + position + ", [label] " + item.get_label()/* + ", [apk label] " + item.get_apk_label() + ", [app name] " + item.get_app_name()*/);
                }

                item_save_focus(view, isFocus);
                item_scroll_to_middle(view, isFocus);
                item_scale_up(view, isFocus);
                item_highlight(view, isFocus);
            });
        }

        public void on_click_item() {
            itemView.setOnClickListener(view -> {
                BaseActivity.set_global_key(KEYCODE_DPAD_CENTER);
                String videoId, pkgName, appName;
                Intent intent = null;
                RecommendItem recommendItem;

                recommendItem = g_itemList.get(getAbsoluteAdapterPosition());
                videoId = recommendItem.get_videoId();
                pkgName = recommendItem.get_package_name();
                intent = recommendItem.get_intent();
                appName = recommendItem.get_app_name();

                Log.d(TAG, "on_click_item: [appName] " + appName);
                gAppName = appName;

                if (get() instanceof SideMenuActivity sideMenuActivity)
                    sideMenuActivity.finish();

                if (intent != null) {
                    ActivityUtils.start_activity(get(), intent.getPackage(), appName);
                    return;
                }

                if (is_popular_programs())
                    ActivityUtils.start_by_type(get(), recommendItem); //ActivityUtils.start_youtube(get(), videoId, appName);
                else
                    ActivityUtils.start_activity(get(), recommendItem);

                Log.d(TAG, "on_click_item: [video id] " + videoId + ", [pkgName] " + pkgName);
            });
        }

        public void on_key_item() {
            final boolean OK_TO_SCROLL      = false;
            final boolean NOT_OK_TO_SCROLL  = !OK_TO_SCROLL;
            final boolean OK_TO_FOCUS_APP   = false;
            final boolean FORBID_INPUT_KEY  = true;

            itemView.setOnKeyListener((v, keyCode, event) -> {
                RecommendListView listView = (RecommendListView) v.getParent();
                boolean scrollIdle = listView.is_scroll_idle();

                // key left/right : before scroll finish, not allow to input key
                if (keyCode == KEYCODE_DPAD_LEFT ||
                    keyCode == KEYCODE_DPAD_RIGHT) {
                    if (is_allow_key(event)) {
                        return OK_TO_SCROLL;
                        /*
                        if (!scrollIdle) Log.w(TAG, "on_key: list scroll NOT IDLE");
                        if (scrollIdle)  return OK_TO_SCROLL;
                        else             return NOT_OK_TO_SCROLL;
                        */
                    }
                    else
                        return FORBID_INPUT_KEY;
                }

                // key up/down : to focus recycler view, block focus child
                if (keyCode == KEYCODE_DPAD_UP ||
                    keyCode == KEYCODE_DPAD_DOWN) {
                    if (is_allow_key(event)) {
                        if (is_in_home())       item_block_focus(listView, keyCode);
                        if (is_in_side_menu())  return OK_TO_FOCUS_APP;
                    }
                    else
                        return FORBID_INPUT_KEY;
                }
                return false;
            });
        }

        public boolean is_open_install() {
            RecommendItem recommendItem = g_itemList.get(getAbsoluteAdapterPosition());
            String appPath = recommendItem.get_app_path();
            return appPath != null;
        }

        public boolean is_allow_key(KeyEvent event) {
            int keyCode  = event.getKeyCode();
            int action   = event.getAction();
            int position = getAbsoluteAdapterPosition();
            boolean isLeftSide  = is_grid() && position % RecommendListView.GRID_COLUMN_NUM == 0;
            boolean isRightSide = is_grid() && position % RecommendListView.GRID_COLUMN_NUM == 4;

            if (keyCode == KEYCODE_DPAD_UP   ||
                keyCode == KEYCODE_DPAD_DOWN ||
                keyCode == KEYCODE_DPAD_LEFT ||
                keyCode == KEYCODE_DPAD_RIGHT) {
                if (ACTION_UP == action) {
                    //Log.w(TAG, "is_pass_check: not pass, forbid ACTION UP");
                    return false;
                }
            }
            if (keyCode == KEYCODE_DPAD_RIGHT &&
                position == (getItemCount() - 1)) {
                Log.w(TAG, "is_pass_check: not pass, forbid list KEY RIGHT");
                return false;
            }
            if (keyCode == KEYCODE_DPAD_RIGHT &&
                isRightSide) {
                Log.w(TAG, "is_pass_check: not pass, forbid grid KEY RIGHT");
                return false;
            }
            if (keyCode == KEYCODE_DPAD_LEFT &&
                isLeftSide) {
                Log.w(TAG, "is_pass_check: not pass, forbid grid KEY LEFT");
                return false;
            }
            return true;
        }

        public boolean is_popular_list() {
            return g_listViewID == R.id.lo_home_rcv_popular;
        }

        public boolean is_application_list() {
            return g_listViewID == R.id.lo_home_rcv_apps;
        }

        public boolean is_apps_games_list() {
            return g_listViewID == R.id.lo_home_rcv_apps_games;
        }

        public boolean is_our_package() {
            RecommendItem recommendItem = g_itemList.get(getAbsoluteAdapterPosition());
            String pkgName = recommendItem.get_package_name();
            return pkgName.startsWith(get().getPackageName());
        }

        public boolean is_network_available() {
            ConnectivityManager connectivityManager = (ConnectivityManager) get().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        /** @noinspection ConstantValue*/
        public void item_save_focus(View view, boolean isFocus) {
            RecommendListView listView = get_list_view(itemView);

            if (!isFocus) {
                listView.g_prev_pos = getAbsoluteAdapterPosition();
                return;
            }

            int itemPos = getAbsoluteAdapterPosition();
            listView.g_focused_pos = itemPos;
            listView.g_prev_row = itemPos / RecommendListView.GRID_COLUMN_NUM + 1;

            if (false) {
                set_middle_position(listView, itemPos);
                Log.d(TAG, "item_save_focus: itemPos   = " + itemPos);
                Log.d(TAG, "item_save_focus: middlePos = " + listView.g_middle_pos);
            }
        }

        public void item_block_focus(RecommendListView listView, int keyCode) {
            int viewID = listView.getId();

            // key down : do nothing
            if (keyCode == KEYCODE_DPAD_DOWN &&
                viewID  == R.id.lo_home_rcv_apps_games) {
                Log.d(TAG, "item_block_focus: key down , do nothing");
                return;
            }

            listView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

            Log.d(TAG, "item_block_focus: " + (
                    viewID == R.id.lo_home_rcv_popular    ? "exit Popular" :
                    viewID == R.id.lo_home_rcv_apps       ? "exit Apps" :
                    viewID == R.id.lo_home_rcv_apps_games ? "exit Apps & Games" :
                    viewID == R.id.lo_side_menu_app_game_grid ? "exit Apps & Games" :
                    viewID == R.id.lo_side_menu_app_list      ? "exit Applications" : "exit ???") );
        }

        public void item_scale_up(View view, boolean isFocus) {
            float zoomIn  = 1.225f;
            float zoomOut = 1.0f;
            int duration  = 40;
            view.animate()
                .setDuration(duration)
                .scaleX(isFocus ? zoomIn : zoomOut)
                .scaleY(isFocus ? zoomIn : zoomOut)
                .translationZ(isFocus ? 10 : 0);
            //view.setStateListAnimator(AnimatorInflater.loadStateListAnimator(g_ref.get(), R.animator.circle_animator));
            //view.setSelected(isFocus);
        }

        public void item_highlight(View view, boolean isFocus) {
            View frame = view.findViewById(R.id.lo_rcv_block_focus);
            View label = view.findViewById(R.id.lo_rcv_item_label);

            // highlight frame
            if (frame != null)
                frame.setSelected(isFocus);

            // highlight label
            if (label != null) {
                label.setSelected(isFocus);
                if (is_in_side_menu())
                    label.setVisibility(isFocus ? View.VISIBLE
                                                : View.INVISIBLE);
            }

        }

        public void item_scroll_to_middle(View view, boolean isFocus) {
            if (!isFocus)
                return;

            if (is_in_home())
                move_to_middle(view); //item_scroll_of_home(view);

            if (is_in_side_menu())
                item_scroll_of_side_menu(view);
        }

        public void item_scroll_of_home(View view) {
            RecommendListView listView = (RecommendListView) view.getParent();
            int itemPos   = getAbsoluteAdapterPosition();
            int focusPos  = listView.g_focused_pos;
            int middlePos = listView.g_middle_pos;

            if (itemPos > focusPos && itemPos >= middlePos) {
                listView.smoothScrollBy(get_dx_right(itemPos, middlePos), 0, null, RecommendListView.SCROLL_DURATION);
                listView.g_scroll_state = listView.getScrollState();
            }
            if (itemPos < focusPos && itemPos <= middlePos) {
                listView.smoothScrollBy(get_dx_left(itemPos, middlePos), 0, null, RecommendListView.SCROLL_DURATION);
                listView.g_scroll_state = listView.getScrollState();
            }
        }

        public void item_scroll_of_side_menu(View view) {
            RecommendListView listView = (RecommendListView) view.getParent();
            int itemPos = getAbsoluteAdapterPosition();
            int prevPos = listView.g_prev_pos;
            int middlePos = listView.g_middle_pos;
            int prevRow = listView.get_previous() / RecommendListView.GRID_COLUMN_NUM + 1;
            int currRow = itemPos / RecommendListView.GRID_COLUMN_NUM + 1;

            if (is_grid()) { // Apps & Games
                //Log.e(TAG, "item_scroll_of_side_menu: [itemPos] " + itemPos + ", [prevPos] " + prevPos + ", [prevRow] " + prevRow + ", [currRow] " + currRow);
                if (itemPos == prevPos ||
                    prevRow == currRow) {
                    Log.d(TAG, "item_scroll_of_side_menu: do not scroll");
                    return;
                }
                if (currRow > prevRow) { // scroll down
                    Log.d(TAG, "item_scroll_of_side_menu: scroll down");
                    listView.smoothScrollBy(0, get_dy_down(currRow), null, RecommendListView.SCROLL_DURATION);
                    listView.g_scroll_state = listView.getScrollState();
                }
                if (currRow < prevRow) { // scroll up
                    Log.d(TAG, "item_scroll_of_side_menu: scroll up");
                    listView.smoothScrollBy(0, get_dy_up(currRow), null, RecommendListView.SCROLL_DURATION);
                    listView.g_scroll_state = listView.getScrollState();
                }
            }
            else { // Recommendations
                //Log.e(TAG, "item_scroll_of_side_menu: [itemPos] " + itemPos + ", [middlePos] " + middlePos);
                if (itemPos > middlePos) { // scroll right
                    Log.d(TAG, "item_scroll_of_side_menu: scroll right");
                    listView.smoothScrollBy(get_dx_right(itemPos, middlePos), 0, null, RecommendListView.SCROLL_DURATION);
                    listView.g_scroll_state = listView.getScrollState();
                }
                if (itemPos < middlePos) { // scroll left
                    Log.d(TAG, "item_scroll_of_side_menu: scroll left");
                    listView.smoothScrollBy(get_dx_left(itemPos, middlePos), 0, null, RecommendListView.SCROLL_DURATION);
                    listView.g_scroll_state = listView.getScrollState();
                }
                set_middle_position(listView, itemPos);
            }
        }

        public int get_dx_right(int itemPos, int middlePos) {
            int itemWidth, itemMargin, delta;
            int moveCount = itemPos - middlePos;

            //Log.d(TAG, "get_dx_right: itemPos   = " + itemPos);
            //Log.d(TAG, "get_dx_right: middlePos = " + middlePos);

            if (is_in_home()) {
                itemMargin = get().getResources().getDimensionPixelSize(R.dimen.home_list_title_margin_start);
                itemWidth  = get().getResources().getDimensionPixelSize(R.dimen.home_list_rcv_block_width);
                delta      = get().getResources().getDimensionPixelSize(R.dimen.home_list_rcv_block_middle_ref);
                return itemPos == 3 ? 4 * itemWidth + itemMargin - delta
                                    : itemWidth;
            }
            if (is_in_side_menu()) {
                itemMargin = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_margin_x);
                itemWidth  = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_width) + itemMargin * 2;
                delta      = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_middle_delta);
                if (moveCount == 1) {
                    return itemPos == 3 ? itemWidth * 4 - delta - itemMargin
                                        : itemWidth;
                }
                if (moveCount == 2) {
                    return itemPos == 4 ? itemWidth * 5 - delta - itemMargin
                                        : itemWidth * moveCount;
                }
            }
            Log.e(TAG, "get_dx_right: not focus Home/Side Menu, itemPos = " + itemPos);
            return 0;
        }

        public int get_dx_left(int itemPos, int middlePos) {
            int itemWidth, itemWidthLast, itemMargin, itemMarginLast, delta, scrollPos, moveCount;
            scrollPos = getItemCount() - 3;
            moveCount = itemPos - middlePos;

            if (moveCount < 0)
                moveCount = -moveCount;

            //Log.d(TAG, "get_dx_left: itemPos   = " + itemPos);
            //Log.d(TAG, "get_dx_left: middlePos = " + middlePos);

            if (is_in_home()) {
                itemWidth     = get().getResources().getDimensionPixelSize(R.dimen.home_list_rcv_block_width);
                itemWidthLast = get().getResources().getDimensionPixelSize(R.dimen.home_list_rcv_block_out_width);
                delta         = get().getResources().getDimensionPixelSize(R.dimen.home_list_rcv_block_middle_ref);
                return itemPos == middlePos ? delta - (3 * itemWidth + itemWidthLast)
                                            : -itemWidth;
            }
            if (is_in_side_menu()) {
                itemMarginLast = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_margin_x_2);
                itemMargin     = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_margin_x);
                itemWidth      = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_width) + (itemMargin * 2);
                delta          = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_middle_delta);
                //Log.e(TAG, "get_dx_left: delta = " + (delta - (4 * itemWidth + itemMarginLast - itemMargin - itemMargin)));
                if (moveCount == 1) return -itemWidth;
                if (moveCount == 2) return -itemWidth * 2;
            }
            Log.e(TAG, "get_dx_left: not focus Home/Side Menu, itemPos = " + itemPos);
            return 0;
        }

        public int get_dy_up(int currRow) {
            int delta        = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_game_grid_middle_delta);
            int bannerHeight = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_height);
            int labelHeight  = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_text_height);
            int itemMargin   = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_margin_y);
            int itemHeight   = bannerHeight + labelHeight + itemMargin;
            int lastRow      = (getItemCount() - 1) / RecommendListView.GRID_COLUMN_NUM + 1;
            int startRow     = lastRow - 2;
            //Log.d(TAG, "get_dy_up: currRow  = " + currRow);
            //Log.d(TAG, "get_dy_up: startRow = " + startRow);
            if (currRow == startRow)
                return delta - currRow * itemHeight;
            if (currRow < startRow)
                return -itemHeight;
            return 0;
        }

        public int get_dy_down(int currRow) {
            int delta        = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_game_grid_middle_delta);
            int bannerHeight = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_height);
            int labelHeight  = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_text_height);
            int itemMargin   = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_margin_y);
            int itemHeight   = bannerHeight + labelHeight + itemMargin;
            int startRow     = 3;
            if (currRow == startRow)
                return currRow * itemHeight - delta - labelHeight;
            if (currRow > startRow)
                return itemHeight;
            return 0;
        }

        public long get_time_diff() {
            long timeDiff = System.currentTimeMillis() - g_previousMs;
            g_previousMs = System.currentTimeMillis();
            return timeDiff;
        }

        public RecommendListView get_list_view(View itemView) {
            RecommendListView listView = null;

            if (is_popular_list())
                listView = get().findViewById(R.id.lo_home_rcv_popular);
            if (is_application_list())
                listView = get().findViewById(R.id.lo_home_rcv_apps);
            if (is_apps_games_list())
                listView = get().findViewById(R.id.lo_home_rcv_apps_games);
            if (null == listView)
                listView = (RecommendListView) itemView.getParent();

            return listView;
        }

        /** @noinspection ManualMinMaxCalculation*/
        public void set_middle_position(RecommendListView listView, int itemPos) {
            int firstPos = is_in_home() ? 3 :
                           is_in_side_menu() ? 2 : 0;
            int lastPos = is_in_home() ? getItemCount() - 4 :
                          is_in_side_menu() ? getItemCount() - 3 : 0;

            listView.g_middle_pos = itemPos;

            if (itemPos <= firstPos)
                listView.g_middle_pos = firstPos;

            if (itemPos >= lastPos)
                listView.g_middle_pos = lastPos;
        }

        public void draw_thumbnail(ImageView thumbnail, Drawable drawable) {
            if (null == drawable)
                return;
            if (get().isFinishing() || get().isDestroyed()) {
                Log.e(TAG, "draw_thumbnail: activity is finishing or destroyed");
                return;
            }

            Glide.with(get())
                    .load(drawable)
                    .error(R.drawable.internet_error)
                    .placeholder(R.drawable.default_photo)
                    .override(ListManager.BANNER_WIDTH, ListManager.BANNER_HEIGHT)
                    .centerCrop()
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(thumbnail);
        }

        public void draw_thumbnail(ImageView thumbnail, String url) {
            if (null == url || null == thumbnail || url.isEmpty())
                return;
            if (null == get() || get().isFinishing() || get().isDestroyed()) {
                Log.e(TAG, "draw_thumbnail: activity is finishing or destroyed");
                return;
            }

            Glide.with(get())
                    .load(url)
                    .error(R.drawable.internet_error)
                    .placeholder(R.drawable.internet_error)
                    .override(ListManager.BANNER_WIDTH, ListManager.BANNER_HEIGHT)
                    .centerCrop()
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(request_listener(thumbnail, url))
                    .into(thumbnail);
        }

        public void draw_thumbnail(ImageView thumbnail, int resId) {
            if (0 == resId)
                return;
            if (get().isFinishing() || get().isDestroyed()) {
                Log.e(TAG, "draw_thumbnail: activity is finishing or destroyed");
                return;
            }

            Glide.with(get())
                    .load(resId)
                    .error(R.drawable.internet_error)
                    .placeholder(R.drawable.default_photo)
                    .override(ListManager.BANNER_WIDTH, ListManager.BANNER_HEIGHT)
                    .centerCrop()
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(thumbnail);
        }

        public RequestListener<Drawable> request_listener(ImageView thumbnail, String url) {
            final WeakReference<AppCompatActivity> activityContextRef = new WeakReference<>(get());
            final WeakReference<ImageView> thumbnailRef = new WeakReference<>(thumbnail);

            return new RequestListener<>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    AppCompatActivity activity = activityContextRef.get();
                    ImageView thumbnail = thumbnailRef.get();

                    if (null == activity || activity.isFinishing() || activity.isDestroyed()) {
                        Log.e(TAG, "onLoadFailed: activity is finishing or destroyed");
                        return false;
                    }
                    if (null == url || null == thumbnail/* || !thumbnail.isAttachedToWindow()*/) {
                        Log.e(TAG, "onLoadFailed: something is null");
                        return false;
                    }

                    retry_load_image(thumbnail, url);
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            };
        }

        public void retry_load_image(ImageView thumbnail, String url) {
            AppCompatActivity activity = get();
            if (null == activity || activity.isFinishing() || activity.isDestroyed()) {
                Log.e(TAG, "retry_load_image: activity is finishing or destroyed");
                return;
            }
            if (null == url || null == thumbnail/* || !thumbnail.isAttachedToWindow()*/) {
                Log.e(TAG, "retry_load_image: something is null");
                return;
            }

            if (is_network_available()) {
                // remove previous callback
                if (gRetryDraw != null)
                    gRetryHandler.removeCallbacks(gRetryDraw);

                // set new callback
                gRetryDraw = () -> {
                    if (activity.isFinishing() || activity.isDestroyed()) {
                        Log.e(TAG, "retry_load_image: activity is finishing or destroyed");
                        return;
                    }
                    /*if (!thumbnail.isAttachedToWindow()) {
                        Log.e(TAG, "retry_load_image: something is null or not attached to window");
                        return;
                    }*/
                    draw_thumbnail(thumbnail, url);
                };

                // schedule new callback
                gRetryHandler.postDelayed(gRetryDraw, 3000);
            }
            else if (null == gNetworkChangeReceiver) {
                // register network change receiver
                gNetworkChangeReceiver = new NetworkChangeReceiver(thumbnail, url);
                IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                activity.registerReceiver(gNetworkChangeReceiver, intentFilter);
            }
        }

        public void move_to_middle(View itemView) {
            RecommendListView recommendListView = (RecommendListView) itemView.getParent();
            //int position = recommendListView.get_position();
            //int previous = recommendListView.get_previous();

            //Log.e(TAG, "move_to_middle: [position] " + position + ", [previous] " + previous);
            //if (position == previous)
            //    return;

            int[] location = new int[2];
            itemView.getLocationOnScreen(location);
            int viewCenterX = location[0] + itemView.getWidth() / 2;
            recommendListView.getLocationOnScreen(location);
            int listCenterX = location[0] + recommendListView.getWidth() / 2;
            int offsetX = viewCenterX - listCenterX;

            if (get_time_diff() < 100)
                recommendListView.scrollBy(offsetX, 0);
            else
                recommendListView.smoothScrollBy(offsetX, 0, null, RecommendListView.QUICK_SCROLL_DURATION);
            /*if (offsetX < 0)
                Log.d(TAG, "move_to_middle: move left, [offset] " + offsetX);
            else
                Log.d(TAG, "move_to_middle: move right, [offset] " + offsetX);*/
        }
        
        public int dp_to_px(int dp) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, get().getResources().getDisplayMetrics());
        }

        class NetworkChangeReceiver extends BroadcastReceiver {
            private final WeakReference<AppCompatActivity> activityContextRef;
            private final WeakReference<ImageView> thumbnailRef;
            private final String url;

            public NetworkChangeReceiver(ImageView thumbnail, String url) {
                this.activityContextRef = new WeakReference<>(get());
                this.thumbnailRef = new WeakReference<>(thumbnail);
                this.url = url;
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                AppCompatActivity activity = activityContextRef.get();
                ImageView thumbnail = thumbnailRef.get();

                if (null == activity || activity.isFinishing() || activity.isDestroyed()) {
                    Log.e(TAG, "onReceive: activity is finishing or destroyed");
                    try {
                        context.unregisterReceiver(this);
                    }
                    catch (Exception e) {
                        Log.e(TAG, "onReceive: " + e.getMessage());
                    }
                    return;
                }
                if (null == url || null == thumbnail/* || !thumbnail.isAttachedToWindow()*/) {
                    Log.e(TAG, "onReceive: something is null");
                    try {
                        context.unregisterReceiver(this);
                    }
                    catch (Exception e) {
                        Log.e(TAG, "onReceive: " + e.getMessage());
                    }
                    return;
                }

                if (is_network_available()) {
                    activity.unregisterReceiver(this);
                    RecommendAdapter.ViewHolder.this.draw_thumbnail(thumbnail, url);
                }
            }
        }
    }
}
