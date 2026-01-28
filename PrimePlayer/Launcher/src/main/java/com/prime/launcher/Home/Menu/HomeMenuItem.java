package com.prime.launcher.Home.Menu;

import android.graphics.drawable.Drawable;

public class HomeMenuItem {
    private final String TAG = "HomeMenuItem";

    private Drawable g_draw_icom = null;
    private String g_str_label = null;
    private String g_str_count = null;

    public HomeMenuItem(Drawable icon, String label, String count) {
        this.g_draw_icom = icon;
        this.g_str_label = label;
        this.g_str_count = count;
    }

    public Drawable get_icon() {
        return this.g_draw_icom;
    }

    public String get_label() {
        return this.g_str_label;
    }

    public String get_count() {
        return this.g_str_count;
    }
}
