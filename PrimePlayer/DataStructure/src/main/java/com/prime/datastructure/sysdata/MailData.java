package com.prime.datastructure.sysdata;

public class MailData {
    public static final String TAG = "MailData";
    public static final String ID = "id";
    public static final String DATA = "data";
    public static final String READ = "read";
    private int id;
    private String data;
    private int read;
    private int already_shown;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public String ToString() {
        String str;
        str = "id : "+ id + " data : " + data + " read : " + read + " already_shown : " + already_shown;
        return str;
    }

    public int getAlready_shown() {
        return already_shown;
    }

    public void setAlready_shown(int already_shown) {
        this.already_shown = already_shown;
    }
}
