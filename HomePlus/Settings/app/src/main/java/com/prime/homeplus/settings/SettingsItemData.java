package com.prime.homeplus.settings;

import android.view.View;

public class SettingsItemData {
    private String title, subtitle, value;
    private View view;
    private boolean isExpand = false;

    public SettingsItemData(String title, String subtitle, String value, View view){
        this.title = title;
        this.subtitle = subtitle;
        this.value = value;
        this.view = view;
    }

    public String getTitle(){
        return this.title;
    }

    public String getSubtitle(){
        return this.subtitle;
    }

    public String getValue(){
        return this.value;
    }

    public void setValue(String value){
        this.value = value;
    }

    public View getView(){
        return this.view;
    }

    public void setExpand(boolean expand){
        this.isExpand = expand;
    }

    public boolean isExpand(){
        return isExpand;
    }
}
