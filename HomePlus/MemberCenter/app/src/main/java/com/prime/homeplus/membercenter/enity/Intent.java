package com.prime.homeplus.membercenter.enity;

import java.util.ArrayList;

public class Intent {
    private String action;
    private String contentUri;
    private ArrayList<Extra> extras;
    private String packageName;

    public Intent(String action, String contentUri, ArrayList<Extra> extras, String packageName) {
        this.action = action;
        this.contentUri = contentUri;
        this.extras = extras;
        this.packageName = packageName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getContentUri() {
        return contentUri;
    }

    public void setContentUri(String contentUri) {
        this.contentUri = contentUri;
    }

    public ArrayList<Extra> getExtras() {
        return extras;
    }

    public void setExtras(ArrayList<Extra> extras) {
        this.extras = extras;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}

