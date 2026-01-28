package com.prime.dmg.launcher.Mail;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;

public class MailDetail {
    public static final String TAG = "MailDetail";
    @SerializedName("hot_key")
    private String hot_key;
    @SerializedName("title")
    private String title;
    @SerializedName("event_type")
    private int event_type;
    @SerializedName("event_value")
    private String event_value;
    @SerializedName("event_name")
    private String event_name;
    @SerializedName("order")
    private int order;
    @SerializedName("poster")
    private String poster;
    @SerializedName("sourceType")
    private String sourceType;
    @SerializedName("typeName")
    private String typeName;
    @SerializedName("itemId")
    private String itemId;
    @SerializedName("packageName")
    private String packageName;
    @SerializedName("url")
    private String url;
    @SerializedName("channel_num")
    private String channel_num;
    @SerializedName("channel_name")
    private String channel_name;
    @SerializedName("start_time")
    private String start_time;
    @SerializedName("end_time")
    private String end_time;
    @SerializedName("description")
    private String description;


    public String getHot_key() {
        return hot_key;
    }

    public void setHot_key(String hot_key) {
        this.hot_key = hot_key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getEvent_type() {
        return event_type;
    }

    public void setEvent_type(int event_type) {
        this.event_type = event_type;
    }

    public String getEvent_value() {
        return event_value;
    }

    public void setEvent_value(String event_value) {
        this.event_value = event_value;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String toString() {
        String str = "MailDetail {" +
                " title = " + title + " hot_key = " + hot_key + " event_type = " + event_type + " event_value = " + event_value + "event_name = " + event_name + " order = " + order +
                " poster = " + poster + " sourceType = " + sourceType + " typeName = " + typeName + " itemId = " + itemId + " packageName = " + packageName +
                " url = " + url + " channelNum = " + channel_num + " channelName = " + channel_name + " startTime = " + start_time +
                " endTime = " + end_time + " description = " + description;
        return str;
    }

    public static ArrayList<MailDetail> getMailDetailData(String content) {
        ArrayList<MailDetail> arrayList;
        Log.d(TAG,"content = "+content);
        try {
            MailDetail[] mailDetails = (MailDetail[]) new Gson().fromJson(content, MailDetail[].class);
            if (mailDetails == null) {
                arrayList = new ArrayList<>();
            } else {
                arrayList = new ArrayList<>(Arrays.asList(mailDetails));
            }
            return arrayList;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChannel_num() {
        return channel_num;
    }

    public void setChannel_num(String channelNum) {
        this.channel_num = channelNum;
    }

    public String getChannel_name() {
        return channel_name;
    }

    public void setChannel_name(String channelName) {
        this.channel_name = channelName;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
