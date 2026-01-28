package com.prime.launcher.Utils.JsonParser;

import java.util.List;

public class RecommendPackage {
    private String g_PackageName;
    private String g_PackageId;
    private List<RecommendContent> g_ChannelList;

    // Getters and Setters
    public String get_package_name() {
        return g_PackageName;
    }

    public void set_package_name(String packageName) {
        this.g_PackageName = packageName;
    }

    public String get_package_id() {
        return g_PackageId;
    }

    public void set_package_id(String packageId) {
        this.g_PackageId = packageId;
    }

    public List<RecommendContent> get_channel_list() {
        return g_ChannelList;
    }

    public void set_channel_list(List<RecommendContent> channelList) {
        this.g_ChannelList = channelList;
    }
}
