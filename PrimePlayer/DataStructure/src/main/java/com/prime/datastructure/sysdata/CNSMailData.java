package com.prime.datastructure.sysdata;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class CNSMailData {
    private static final String TAG = "CNSMailData";

    // 定義資料庫欄位名稱常數
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    public static final String IMPORTANCE = "importance";
    public static final String RECEIVE_TIME = "receive_time";
    public static final String READ = "read";
    public static final String ALREADY_SHOWN = "alreadyShown"; 

    // 成員變數
    private int id;
    private String title;
    private String body;
    private int importance;
    private long receiveTime;
    private int read;
    private int alreadyShown;

    // 建構子
    public CNSMailData() {
    }

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public int getImportance() { return importance; }
    public void setImportance(int importance) { this.importance = importance; }

    public long getReceiveTime() { return receiveTime; }
    public void setReceiveTime(long receiveTime) { this.receiveTime = receiveTime; }
    public String getFormattedReceiveTime() {
        if (receiveTime == 0) return ""; // 防呆
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(receiveTime));
    }

    public int getRead() { return read; }
    public void setRead(int read) { this.read = read; }

    public int getAlreadyShown() { return alreadyShown; }
    public void setAlreadyShown(int alreadyShown) { this.alreadyShown = alreadyShown; }

    @Override
public String toString() {
        return "CNSMailData{" +
                "id=" + id +                         
                ", title='" + title + '\'' +         
                ", body='" + body + '\'' +           
                ", importance=" + importance +
                ", receiveTime=" + receiveTime +
                ", read=" + read +
                ", alreadyShown=" + alreadyShown + 
                '}';
    }


}
