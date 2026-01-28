package com.prime.launcher.Home.Recommend.Installer;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

public class InstallData implements Serializable {
    public String g_Title = "";
    public String g_Description = "";
    public String g_FullText = "";
    public String g_PkgName = "";
    public String g_IconUrl = "";
    public String g_AppPath = "";
    public ArrayList<String> g_ScreenCaptures = new ArrayList<>();
    public long g_VersionCode = 0;
    public boolean g_ForceUpdate = false;
    public boolean g_isInstalled = false;
    public boolean g_isUpdate = false;

    public String getFullText() {
        return this.g_FullText;
    }

    public String getDescription() {
        return this.g_Description;
    }

    public String getAppName() {
        return this.g_Title;
    }

    public String getPkgName() {
        return this.g_PkgName;
    }

    public String getIconUrl() {
        return this.g_IconUrl;
    }

    public String getAppPath() {
        return this.g_AppPath;
    }

    public ArrayList<String> getScreens() {
        if (this.g_ScreenCaptures == null) {
            this.g_ScreenCaptures = new ArrayList<>();
        }
        return this.g_ScreenCaptures;
    }

    public long getVersionCode() {
        return this.g_VersionCode;
    }

    public boolean getInstalled() {
        return isInstalled();
    }

    public boolean getUpdate() {
        return isUpdate();
    }

    public boolean isForceUpdate() {
        return this.g_ForceUpdate;
    }

    public boolean isInstalled() {
        return g_isInstalled;
    }

    public boolean isUpdate() {
        return g_isUpdate;
    }

    @NonNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("App Name:");
        sb.append(TextUtils.isEmpty(getAppName()));
        sb.append("\n");
        sb.append("Description:");
        sb.append(TextUtils.isEmpty(getDescription()));
        sb.append("\n");
        sb.append("Full Text:");
        sb.append(TextUtils.isEmpty(getFullText()));
        sb.append("\n");
        sb.append("Package Name:");
        sb.append(TextUtils.isEmpty(getPkgName()));
        sb.append("\n");
        sb.append("Icon Url:");
        sb.append(TextUtils.isEmpty(getIconUrl()));
        sb.append("\n");
        sb.append("App Path:");
        sb.append(TextUtils.isEmpty(getAppPath()));
        sb.append("\n");
        sb.append("Version Code:");
        sb.append(getVersionCode());
        sb.append("\n");
        sb.append("Force Update:");
        sb.append(isForceUpdate());
        sb.append("\n");
        if (getScreens() != null && getScreens().size() > 0) {
            sb.append("Screen Captures:");
            sb.append("\n");
            for (int i = 0; i < getScreens().size(); i++) {
                sb.append(getScreens().get(i));
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
