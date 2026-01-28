package com.prime.launcher.Mail;

import static com.prime.launcher.Mail.MailActivity.MAIL_INBOX;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prime.launcher.R;

import java.util.ArrayList;

public class MailMenuView extends RelativeLayout {
    private static final String TAG = "MailMenuView";

    private RelativeLayout g_menuContent;
    private View g_menuBackground;
    private ArrayList<ImageView> g_iconViewList;
    private ArrayList<MenuItem> g_menuItemList;
    private ArrayList<TextView> g_labelViewList;
    private int g_last_click_item = 0;
    private boolean g_last_extend_status = false;
    private View g_last_focus_view;
    private ArrayList<LinearLayout> g_itemViewList;
    //private LinearLayout g_last_item_focus_view;

    private OnMenuItemListener g_menuItemListener;

    public void set_menu_item_listener(OnMenuItemListener listener) {
        g_menuItemListener = listener;
    }

    public interface OnMenuItemListener {
        void on_click_menu_item(int item);

        void on_key_menu_item(View view, int keyCode);
    }

    public MailMenuView(Context context) {
        super(context);
        init();
    }

    public MailMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MailMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_mail_menu, this);
        g_menuBackground = findViewById(R.id.lo_mail_menu_view_frame);
        g_menuContent = findViewById(R.id.lo_mail_menu_rltvl_content);
        g_menuItemList = new ArrayList<>();
    }

    public void add_menu_item(int unfocusIcon, int focusIcon, String label) {
        g_menuItemList.add(new MenuItem(unfocusIcon, focusIcon, label));
    }

    public void genMenuItemLayout() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout menuRootView = new LinearLayout(getContext());
        menuRootView.setOrientation(LinearLayout.VERTICAL);
        menuRootView.setGravity(Gravity.CENTER_VERTICAL);
        menuRootView.setLayoutParams(layoutParams);
        g_labelViewList = new ArrayList<>();
        g_iconViewList = new ArrayList<>();
        g_itemViewList = new ArrayList<>();

        for (MenuItem menuItem : g_menuItemList) {
            LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams(getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_menu_imgv_icon_width), getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_menu_imgv_icon_height));
            iconLayoutParams.gravity = Gravity.CENTER_VERTICAL;
            iconLayoutParams.leftMargin = getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_menu_imgv_icon_marginleft);
            iconLayoutParams.rightMargin = getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_menu_imgv_icon_marginright);
            ImageView iconView = new ImageView(getContext());
            iconView.setBackgroundResource(menuItem.get_unfocus_icon());
            iconView.setLayoutParams(iconLayoutParams);
            g_iconViewList.add(iconView);

            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            labelParams.gravity = Gravity.CENTER_VERTICAL;
            TextView labelView = new TextView(getContext());
            labelView.setText(menuItem.get_label());
            labelView.setLayoutParams(labelParams);
            labelView.setGravity(Gravity.CENTER);
            labelView.setTextColor(Color.WHITE);
            labelView.setTextSize(getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_menu_textv_lable_textsize) / getContext().getResources().getDisplayMetrics().density);
            labelView.setSingleLine();
            labelView.setVisibility(View.GONE);
            g_labelViewList.add(labelView);

            LinearLayout.LayoutParams itemViewParams = new LinearLayout.LayoutParams(getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_menu_lnrl_item_width), getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_menu_lnrl_item_height));
            LinearLayout itemView = new LinearLayout(getContext());
            itemViewParams.leftMargin = getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_menu_lnrl_item_marginleft);
            itemViewParams.bottomMargin = getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_menu_lnrl_item_marginbottom);
            int menu_position = g_menuItemList.indexOf(menuItem);
            itemView.setTag(menu_position);
            itemView.setId(View.generateViewId());
            itemView.setOrientation(LinearLayout.HORIZONTAL);
            itemView.setLayoutParams(itemViewParams);
            itemView.setClickable(true);
            itemView.setFocusable(true);
            itemView.setBackgroundResource(R.drawable.mail_item_bg_focus);
            itemView.setNextFocusUpId(menu_position == 0 ? itemView.getId() : View.NO_ID);
            itemView.setNextFocusDownId(menu_position == g_menuItemList.size() - 1 ? itemView.getId() : View.NO_ID);
            itemView.setOnClickListener(view -> {
                Log.d(TAG, "onClick: [menu_position] " + menu_position);
                if (g_menuItemListener != null)
                    g_menuItemListener.on_click_menu_item(menu_position);

                g_last_focus_view = view;
                set_label_visible(false);

                MenuItem prevItem = g_menuItemList.get(g_last_click_item);
                g_iconViewList.get(g_last_click_item).setBackgroundResource(prevItem.get_unfocus_icon());
                g_labelViewList.get(g_last_click_item).setTextColor(-1);

                MenuItem currentItem = g_menuItemList.get(menu_position);
                g_last_click_item = menu_position;
                g_iconViewList.get(g_last_click_item).setBackgroundResource(currentItem.get_focus_icon());
                g_labelViewList.get(g_last_click_item).setTextColor(getContext().getColor(R.color.pvr_red_color));
            });
            itemView.setOnKeyListener((v, keyCode, event) -> {
                Log.d(TAG, "onKey: [menu_position] " + menu_position);
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }
                g_last_focus_view = v;
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    set_label_visible(true);
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    set_label_visible(false);

                    if (g_menuItemListener != null && get_menu_position() == MAIL_INBOX) {
                        g_menuItemListener.on_key_menu_item(v, keyCode);
                        return false;
                    }
                }

                return false;
            });
            itemView.setOnFocusChangeListener((v, hasFocus) -> {
                Log.d(TAG, "onFocus: [menu_position] " + menu_position + ", [hasFocus] " + hasFocus);
                g_last_focus_view = v;
                if (!hasFocus || View.GONE != g_menuBackground.getVisibility()) {
                    return;
                }
                set_label_visible(true);
            });
            itemView.addView(iconView);
            itemView.addView(labelView);
            menuRootView.addView(itemView);
            g_itemViewList.add(itemView);
        }
        LayoutParams menuRootParams = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        menuRootParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        g_menuContent.addView(menuRootView, menuRootParams);
    }

    public void focus_first_item(int index) {
        g_itemViewList.get(index).requestFocus();
    }

    public void update_item_select(int itemIndex) {
        g_last_click_item = itemIndex;
        MenuItem menuItem = g_menuItemList.get(itemIndex);
        ImageView iconView = g_iconViewList.get(itemIndex);
        TextView labelView = g_labelViewList.get(itemIndex);
        iconView.setBackgroundResource(menuItem.get_focus_icon());
        labelView.setTextColor(getContext().getColor(R.color.pvr_red_color));
    }

    public void set_label_visible(boolean visible) {

        if (g_last_extend_status == visible)
            return;

        g_last_extend_status = visible;
        g_menuBackground.setVisibility(visible ? VISIBLE : GONE);
        if (visible) {
            if (g_last_click_item >= 0) {
                g_itemViewList.get(g_last_click_item).requestFocus();
                //g_last_item_focus_view = itemViewList.get(g_last_click_item);
            }
        }

        int childCount = ((LinearLayout) g_last_focus_view.getParent()).getChildCount();
        for (int i = 0; i < childCount; i++)
            g_labelViewList.get(i).setVisibility(visible ? VISIBLE : GONE);
    }

    public int get_menu_position() {
        MailActivity activity = (MailActivity) getContext();
        return activity.get_menu_position();
    }

    public static class MenuItem {
        private final int g_focus_icon;
        private final String g_label;
        private final int g_unfocus_icon;

        private MenuItem(int unf_icon, int f_icon, String label) {
            this.g_unfocus_icon = unf_icon;
            this.g_focus_icon = f_icon;
            this.g_label = label;
        }

        public int get_unfocus_icon() {
            return this.g_unfocus_icon;
        }

        public int get_focus_icon() {
            return this.g_focus_icon;
        }
        public String get_label() {
            return this.g_label;
        }
    }
}
