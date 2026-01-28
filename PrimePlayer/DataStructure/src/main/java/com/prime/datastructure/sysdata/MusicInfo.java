package com.prime.datastructure.sysdata;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.util.ArrayList;
import java.util.List;
@ParcelablePlease
public class MusicInfo implements Parcelable {
    private static final String TAG = "MusicInfo";

    @SerializedName("name")
    protected String g_category_name;
    @SerializedName("servicelists")
    protected ArrayList<Integer> g_service_id_list;
    @SerializedName("icon")
    protected String g_url;

    public void set_url(String url) {
        g_url = url;
    }

    public String get_url() {
        return g_url;
    }

    public void set_category_name(String categoryName) {
        g_category_name = categoryName;
    }

    public String get_category_name() {
        return g_category_name;
    }

    public void set_service_id_list(ArrayList<Integer> serviceIdList) {
        g_service_id_list = serviceIdList;
    }

    public ArrayList<Integer> get_service_id_list() {
        return g_service_id_list;
    }

    public void to_string() {
        Log.d(TAG, "to_string: Category Name: = " + g_category_name + " Icon Url: = " + g_url + " Service List: = " + g_service_id_list);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        MusicInfoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {
        public MusicInfo createFromParcel(Parcel source) {
            MusicInfo target = new MusicInfo();
            MusicInfoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };
}
