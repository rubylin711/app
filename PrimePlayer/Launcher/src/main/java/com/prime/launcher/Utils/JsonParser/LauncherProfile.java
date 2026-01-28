package com.prime.launcher.Utils.JsonParser;

import java.util.HashMap;
import java.util.List;

public class LauncherProfile {
    private String appName;
    private String packageName;
    private String thumbnail;
    private int iconPriority;
    private String activityName;
    private String productId;
    private HashMap<String, String> packageParams;
    private String description;
    private String fullText;
    private String appPath;
    private String appVersionCode;
    private String apkLabel;
    private List<String> screenCaptures;
    private int displayPosition;
    private boolean forceUpdate;

    public String get_app_name() {
        return appName;
    }

    public String get_package_name() {
        return packageName;
    }

    public String get_thumbnail() {
        return thumbnail;
    }

    public int get_icon_priority() {
        return iconPriority;
    }

    public String get_activity_name() {
        return activityName;
    }

    public String get_product_id() {
        return productId;
    }

    public HashMap<String, String> get_package_params() {
        return packageParams;
    }

    public String get_description() {
        return description;
    }

    public String get_full_text() {
        return fullText;
    }

    public String get_app_path() {
        return appPath;
    }

    public String get_app_version_code() {
        return appVersionCode;
    }

    public String get_apk_label() {
        return apkLabel;
    }

    public List<String> get_screen_captures() {
        return screenCaptures;
    }

    public int get_display_position() {
        return displayPosition;
    }

    public boolean is_force_update() {
        return forceUpdate;
    }

    public void set_app_name(String appName) {
        this.appName = appName;
    }

    public void set_package_name(String packageName) {
        this.packageName = packageName;
    }

    public void set_thumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void set_icon_priority(int iconPriority) {
        this.iconPriority = iconPriority;
    }

    public void set_activity_name(String activityName) {
        this.activityName = activityName;
    }

    public void set_product_id(String productId) {
        this.productId = productId;
    }

    public void set_package_params(HashMap<String, String> packageParams) {
        this.packageParams = packageParams;
    }

    public void set_description(String description) {
        this.description = description;
    }

    public void set_full_text(String fullText) {
        this.fullText = fullText;
    }

    public void set_app_path(String appPath) {
        this.appPath = appPath;
    }

    public void set_app_version_code(String appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public void set_apk_label(String apkLabel) {
        this.apkLabel = apkLabel;
    }

    public void set_screen_captures(List<String> screenCaptures) {
        this.screenCaptures = screenCaptures;
    }

    public void set_display_position(int displayPosition) {
        this.displayPosition = displayPosition;
    }

    public void set_force_update(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
}
