package com.prime.launcher.Utils.JsonParser;

import java.util.List;

public class AdPage {
    String g_page;
    String g_type;
    List<AdPageItem> g_items;

    public void setPage(String page) {
        g_page = page;
    }

    public void setType(String type) {
        g_type = type;
    }

    public void setItems(List<AdPageItem> items) {
        g_items = items;
    }

    public String get_page() {
        return g_page;
    }

    public String get_type() {
        return g_type;
    }

    public List<AdPageItem> get_items() {
        return g_items;
    }
}
