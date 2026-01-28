package com.prime.datastructure.CommuincateInterface;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableClass implements Parcelable {
    private Parcelable data;

    public ParcelableClass(Parcelable data) {
        this.data = data;
    }

    protected ParcelableClass(Parcel in) {
        data = in.readParcelable(getClass().getClassLoader());
    }

    @SuppressWarnings("unchecked")
    public <T extends Parcelable> T getData(Class<T> clazz) {
        return clazz.cast(data);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(data, flags);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<ParcelableClass> CREATOR = new Creator<ParcelableClass>() {
        @Override
        public ParcelableClass createFromParcel(Parcel in) { return new ParcelableClass(in); }
        @Override
        public ParcelableClass[] newArray(int size) { return new ParcelableClass[size]; }
    };
}
