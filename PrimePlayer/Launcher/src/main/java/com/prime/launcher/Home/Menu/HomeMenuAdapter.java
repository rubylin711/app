package com.prime.launcher.Home.Menu;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.BaseActivity;
import com.prime.launcher.CustomView.Snakebar;
import com.prime.launcher.EPG.EpgActivity;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.Mail.MailActivity;
import com.prime.launcher.PVR.Management.RecordManagementDialog;
import com.prime.launcher.R;
import com.prime.launcher.Rank.RankActivity;
import com.prime.launcher.Weather.WeatherActivity;
import com.prime.launcher.member.MemberActivity;
import com.prime.launcher.ChannelChangeManager;
import com.prime.datastructure.sysdata.ProgramInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class HomeMenuAdapter extends RecyclerView.Adapter<HomeMenuAdapter.MyViewHolder> {
    private final static String TAG = "HomeMenuAdapter";
    private final static Boolean DEBUG = true;
    private static final String EXTRA_SEARCH_TYPE = "search_type";
    private static final int SEARCH_TYPE_VOICE = 1;
    private static final int SEARCH_TYPE_KEYBOARD = 2;

    public static int LAST_FOCUS_POSITION = 0;

    private static final float SCALE_ZOOM = 1.15f;
    private static final float SCALE_ORIGIN = 1.0f;

    public static final int SCALE_DURATION = 100;
    public static final int TRANSLATION_Z_INCREASE = 10;
    public static final int TRANSLATION_Z_ORIGIN = 0;

    //元件參數
    private static int ICON_ALPHA_FOCUS = 255;
    private static int ICON_ALPHA_NO_FOCUS = 120;
    private static int g_int_icon_alpha = ICON_ALPHA_NO_FOCUS;

    //元件
    private List<HomeMenuItem> g_list_item = null;
    private boolean g_bool_curr_icon_change_status = false;
    private final WeakReference<HomeMenuView> g_weakreference;
    private int g_notification_count;
    private boolean g_notification_unread_flag = false;

    public HomeMenuAdapter(List<HomeMenuItem> itemList, HomeMenuView menuView) {
        this.g_list_item = itemList;
        this.g_weakreference = new WeakReference<>(menuView);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lo_menu_sub_layer_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder: position = " + position );
        HomeMenuItem item = g_list_item.get(position);

        holder.g_textv_count.setText(get_count(holder, item));
        holder.g_textv_label.setText(item.get_label());
        holder.g_draw_icon.setImageDrawable(item.get_icon());
        holder.itemView.setTag(item.get_label());
        holder.set_alpha(g_int_icon_alpha);
        holder.itemView.setOnFocusChangeListener(on_focus_label(holder, position, item));
        holder.itemView.setOnClickListener(on_click_label(position, item));
        holder.itemView.setOnKeyListener((v, keyCode, event) -> {

            if (KeyEvent.ACTION_DOWN != event.getAction())
                return false;
            if (KeyEvent.KEYCODE_DPAD_UP != keyCode &&
                KeyEvent.KEYCODE_DPAD_DOWN != keyCode &&
                KeyEvent.KEYCODE_DPAD_LEFT != keyCode &&
                KeyEvent.KEYCODE_DPAD_CENTER != keyCode &&
                KeyEvent.KEYCODE_BUTTON_A != keyCode) {
                // close Home Menu
                HomeMenuView menuView = getHomeMenu();
                menuView.close_menu();
                // focus Live TV
                menuView.get().get_live_tv_manager().focus_small_screen();
                return false;
            }
            if (KeyEvent.ACTION_DOWN == event.getAction() &&
                KeyEvent.KEYCODE_DPAD_DOWN == keyCode  &&
                position == getItemCount()-1)
                return true;
            return false;
        });
    }

    private View.OnFocusChangeListener on_focus_label(MyViewHolder holder, final int position, HomeMenuItem item) {
        return (view, hasFocus) -> {
            if (DEBUG && hasFocus)
                Log.d(TAG, "on_focus_label: Label = " + item.get_label() + " position =" + position + " hasFocus = " + hasFocus);

            setup_highlight(holder, position, hasFocus);
        };
    }

    private View.OnClickListener on_click_label(final int position, HomeMenuItem item) {
        return view -> {
            if (DEBUG)
                Log.d(TAG, "on_click_label: Label = " + item.get_label() + " position =" + position);
            launch_activity((String)view.getTag());
        };
    }

    public void setup_highlight(MyViewHolder holder, int position, boolean hasFocus) {
        View view = holder.itemView;
        Context context = view.getContext();

        if (hasFocus) {
            scale_up(view);
            holder.set_alpha(ICON_ALPHA_FOCUS);
            holder.g_textv_label.setTextColor(context.getResources().getColor(R.color.pvr_red_color));
            set_icon(hasFocus, holder);
            LAST_FOCUS_POSITION = position;
        }
        else {
            scale_reset(view);
            holder.set_alpha(ICON_ALPHA_NO_FOCUS);
            set_icon(hasFocus, holder);
            holder.g_textv_label.setTextColor(context.getResources().getColor(R.color.lo_layer_item_sub_border_textv_label_unfocus));
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void set_icon(boolean hasFocus, MyViewHolder holder) {
        String label = (String)holder.itemView.getTag();
        HomeMenuView menuView = getHomeMenu();
        Drawable drawable = null;

        if (label.equals(menuView.SEARCH))
            drawable = get_context().getResources().getDrawable(hasFocus?R.drawable.icon_home_menu_voice_focus:R.drawable.icon_home_menu_voice);
        else if (label.equals(menuView.EPG))
            drawable = get_context().getResources().getDrawable(hasFocus?R.drawable.icon_home_menu_epg_focus:R.drawable.icon_home_menu_epg);
        else if (label.equals(menuView.RECORD))
            drawable = get_context().getResources().getDrawable(hasFocus?R.drawable.icon_home_menu_rec_focus:R.drawable.icon_home_menu_rec);
        else if (label.equals(menuView.WEATHER))
            drawable = get_context().getResources().getDrawable(hasFocus?R.drawable.icon_home_menu_weather_focus:R.drawable.icon_home_menu_weather);
        else if (label.equals(menuView.RANKING))
            drawable = get_context().getResources().getDrawable(hasFocus?R.drawable.icon_home_menu_ranking_focus:R.drawable.icon_home_menu_ranking);
        else if (label.equals(menuView.MESSAGE))
            drawable = get_context().getResources().getDrawable(hasFocus?R.drawable.icon_home_menu_mail_focus:R.drawable.icon_home_menu_mail);
        else if (label.equals(menuView.MEMBER))
            drawable = get_context().getResources().getDrawable(hasFocus?R.drawable.icon_home_menu_member_focus:R.drawable.icon_home_menu_member);
        else if (label.equals(menuView.MUSIC))
            drawable = get_context().getResources().getDrawable(hasFocus?R.drawable.icon_home_menu_music_focus:R.drawable.icon_home_menu_music);
        else if (label.equals(menuView.NOTIFICATIONS))
            drawable = get_context().getResources().getDrawable(hasFocus?R.drawable.icon_home_menu_notification_focus:R.drawable.icon_home_menu_notification);
        else if (label.equals(menuView.SETTINGS))
            drawable = get_context().getResources().getDrawable(hasFocus?R.drawable.icon_home_menu_settings_focus:R.drawable.icon_home_menu_settings);

        holder.g_draw_icon.setImageDrawable(drawable);
    }

    @Override
    public int getItemCount() {
        return g_list_item.size();
    }

    HomeMenuView getHomeMenu() {
        return g_weakreference.get();
    }

    private static void scale_up(View view) {
        view.setScaleX(SCALE_ZOOM);
        view.setScaleY(SCALE_ZOOM);
        view.setTranslationZ(TRANSLATION_Z_INCREASE);
    }

    private static void scale_reset(View view) {
        view.setScaleX(SCALE_ORIGIN);
        view.setScaleY(SCALE_ORIGIN);
        view.setTranslationZ(TRANSLATION_Z_ORIGIN);
    }

    private Context get_context() {
        return getHomeMenu().getContext();
    }

    private void launch_activity(String tagId) {
        HomeMenuView menuView = getHomeMenu();

        if (tagId != getHomeMenu().MUSIC) {
            Log.d(TAG, "launch_activity: ");
            ((HomeActivity)get_context()).g_homeMenu.close_menu();
            ((HomeActivity)get_context()).g_liveTvMgr.g_liveTvFrame.requestFocus();
        }

        BaseActivity.set_global_key(KeyEvent.KEYCODE_DPAD_CENTER);

        if (tagId.equals(menuView.SEARCH))
            launch_google_assist();
        else if (tagId.equals(menuView.EPG))
            launch_epg_activity();
        else if (tagId.equals(menuView.RECORD)) {
            Log.d(TAG, "launch_activity: Launch RECORD");
            open_pvr_dialog();
        }
        else if (tagId.equals(menuView.WEATHER)) {
            launch_weather_activity();
            Log.d(TAG, "launch_activity: Launch WEATHER");
        }
        else if (tagId.equals(menuView.RANKING)) {
            Log.d(TAG, "launch_activity: Launch RANKING");
            launch_rank_activity();
        }
        else if (tagId.equals(menuView.MESSAGE)) {
            Log.d(TAG, "launch_activity: Launch MESSAGE");
            launch_mail_activity();
        }
        else if (tagId.equals(menuView.MEMBER)) {
            Log.d(TAG, "launch_activity: Launch MEMBER");
            launch_member_activity();
        }
        else if (tagId.equals(menuView.MUSIC)) {
            Log.d(TAG, "launch_activity: Launch MUSIC");
            menuView.close_menu();
            show_music_view();
        }
        else if (tagId.equals(menuView.NOTIFICATIONS)) {
            Log.d(TAG, "launch_activity: Launch NOTIFICATIONS");
            launch_google_notification();
        }
        else if (tagId.equals(menuView.SETTINGS)) {
            Log.d(TAG, "launch_activity: Launch SETTINGS");
            launch_default_settings();
        }
    }

    public void update_all_icon(boolean status) {
        if (g_bool_curr_icon_change_status == status) {
            return;
        }
        this.g_bool_curr_icon_change_status = status;
        if (status) {
            g_int_icon_alpha = ICON_ALPHA_NO_FOCUS;
        } else {
            g_int_icon_alpha = ICON_ALPHA_FOCUS;
        }
        notifyDataSetChanged();
    }

    private String get_count(@NonNull MyViewHolder holder, HomeMenuItem homeMenuItem) {
        //Log.d(TAG, "get_count: position = " + position);
        if (!homeMenuItem.get_label().equals(getHomeMenu().NOTIFICATIONS)) {
            holder.g_textv_count.setBackgroundResource(0);
            holder.g_textv_count.setVisibility(View.GONE);
            return "";
        }
        else
            if (g_notification_count == 0) {
                holder.g_textv_count.setBackgroundResource(0);
                holder.g_textv_count.setVisibility(View.GONE);
                return "";
            }
            else {
                holder.g_textv_count.setVisibility(View.VISIBLE);
                if (g_notification_unread_flag) {
                    holder.g_textv_count.setBackgroundResource(R.drawable.circular_notification_count_highlight);
                    holder.g_textv_count.setTextColor(get_context().getColor(R.color.black));
                }
                else {
                    holder.g_textv_count.setBackgroundResource(R.drawable.circular_notification_count);
                    holder.g_textv_count.setTextColor(get_context().getColor(R.color.white));
                }
                return Integer.toString(g_notification_count);
            }
    }

    public void update_notification_count(int count, boolean unreadFlag) {
        g_notification_count = count;
        g_notification_unread_flag = unreadFlag;
        notifyItemChanged(getItemCount() - 2);
    }

    public void update_notification_count(int count) {
        g_notification_count = count;
        notifyItemChanged(getItemCount() - 2);
    }

    public void update_menu(List<HomeMenuItem> menuItems) {
        g_list_item = menuItems;
        notifyDataSetChanged();
    }

    private void launch_google_assist() {
        Log.d(TAG,"Launch Google Assistant");
        Intent intent = new Intent(Intent.ACTION_ASSIST);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.putExtra(EXTRA_SEARCH_TYPE, SEARCH_TYPE_VOICE );
        try {
            get_context().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // 處理語音助理不可用的情況
            Toast.makeText(get_context(), "Google Assistant is not available on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void launch_epg_activity() {
        Log.d(TAG,"Launch_Epg_Activity");
        Intent intent = new Intent(get_context(), EpgActivity.class);
        //intent.putExtra("EXTRA_MESSAGE", "Hello from MainActivity");
        get_context().startActivity(intent);
    }

    private void open_pvr_dialog() {
        Log.d(TAG,"open_pvr_dialog");

        RecordManagementDialog recordManagementDialog = new RecordManagementDialog(get_context());
        recordManagementDialog.show();
    }

    private void launch_weather_activity() {
        Log.d(TAG,"Launch_weather_Activity");
        Intent intent = new Intent(get_context(), WeatherActivity.class);
        //intent.putExtra("EXTRA_MESSAGE", "Hello from MainActivity");
        get_context().startActivity(intent);
    }

    private void launch_member_activity() {
        Log.d(TAG,"launch_member_Activity");
        Intent intent = new Intent(get_context(), MemberActivity.class);
        //intent.putExtra("EXTRA_MESSAGE", "Hello from MainActivity");
        get_context().startActivity(intent);
    }

    private void launch_rank_activity() {
        Log.d(TAG,"Launch_Rank_Activity");
        Intent intent = new Intent(get_context(), RankActivity.class);
        //intent.putExtra("EXTRA_MESSAGE", "Hello from MainActivity");
        get_context().startActivity(intent);
    }

    private void launch_mail_activity() {
        Log.d(TAG,"Launch_Mail_Activity");
        Intent intent = new Intent(get_context(), MailActivity.class);
        //intent.putExtra("EXTRA_MESSAGE", "Hello from MainActivity");
        get_context().startActivity(intent);
    }

    private void show_music_view() {
        ChannelChangeManager channelChangeManager = ChannelChangeManager.get_instance(get_context());
        HomeActivity homeActivity = (HomeActivity) get_context();
        ProgramInfo channelInfo = homeActivity.g_liveTvMgr.get_first_music_channel();

        if (null == channelInfo) {
            View rootView = homeActivity.findViewById(R.id.lo_root_layout);
            homeActivity.g_liveTvMgr.focus_small_screen();
            Snakebar.show(rootView, "No music channel", Snakebar.LENGTH_SHORT);
        }
        else {
            long channelId = channelInfo.getChannelId();
            homeActivity.g_liveTvMgr.check_blocked_channel(channelInfo);
            channelChangeManager.change_channel_by_id(channelId);
            channelChangeManager.reset_mini_epg_index();
            homeActivity.g_liveTvMgr.g_miniEPG.set_channel_info(homeActivity.g_liveTvMgr.g_miniEPG.get_genre());
            homeActivity.g_liveTvMgr.show_music_fullscreen();
        }
    }

    private void launch_google_notification() {
        Log.d(TAG,"launch_google_notification");
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, get_context().getPackageName());
            intent.setAction("com.android.tv.action.OPEN_NOTIFICATIONS_PANEL");
            g_notification_unread_flag = false;
            notifyItemChanged(getItemCount() - 2);
        }

        try {
            get_context().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // 處理語音助理不可用的情況
            Log.d(TAG, "launch_google_notification: e = " + e);
            Toast.makeText(get_context(), "Google notification is not available on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void launch_default_settings() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings"));
        get_context().startActivity(intent);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView g_textv_label;
        public TextView g_textv_count;
        public ImageView g_draw_icon;

        public MyViewHolder(View itemView) {
            super(itemView);
            g_textv_label = itemView.findViewById(R.id.lo_layer_item_sub_border_textv_label);
            g_textv_count = itemView.findViewById(R.id.lo_layer_item_sub_border_textv_count);
            g_draw_icon = itemView.findViewById(R.id.lo_layer_item_sub_border_imgv_icon);
        }

        public void set_alpha(int alpha) {
            g_draw_icon.setImageAlpha(alpha);
        }
    }

    public static class ItemDecoration extends RecyclerView.ItemDecoration {
        private int g_total_Item;

        public ItemDecoration(Context context) {
            if (check_480p_resolution(context))
                g_total_Item = 14;
            else
                g_total_Item = 12;

        }

        @Override
        public void getItemOffsets(Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
            int childAdapterPosition = parent.getChildAdapterPosition(view);
            int itemCount = null == parent.getAdapter() ? 0 : parent.getAdapter().getItemCount();

            int bottom = view.getContext().getResources().getDimensionPixelSize(R.dimen.home_menu_adapter_item_decoration_bottom);
            int topPos0 = view.getContext().getResources().getDimensionPixelSize(R.dimen.home_menu_adapter_item_decoration_top);
            int topPvr = view.getContext().getResources().getDimensionPixelSize(R.dimen.home_menu_adapter_item_decoration_top_with_pvr_pj);
            int topNonPvr = view.getContext().getResources().getDimensionPixelSize(R.dimen.home_menu_adapter_item_decoration_top_without_pvr_pj);

            outRect.bottom = bottom;
            if (childAdapterPosition == 0)
                outRect.top = topPos0;
            if (childAdapterPosition == itemCount - 2) {
                outRect.top = ((g_total_Item-1)-(childAdapterPosition+1))* view.getContext().getResources().getDimensionPixelSize(R.dimen.home_menu_adapter_item_decoration_offset);
            }
        }

        private boolean check_480p_resolution(Context context) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Point realSize = new Point();
            windowManager.getDefaultDisplay().getRealSize(realSize);
            int realHeight = realSize.y;
            //Log.d(TAG, "check_480p_resolution: Real Height:"  + realHeight + "px");

            return realHeight == 480;
        }
    }
}
