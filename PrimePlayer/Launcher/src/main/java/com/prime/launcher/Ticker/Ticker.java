package com.prime.launcher.Ticker;

import android.util.Log;

import com.prime.datastructure.config.Pvcfg;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Ticker {
    private static final String TAG = "Ticker";

    private int id;
    //    private String title; //標題
    private String content; //內容
    private String start_time; //有效時間
    private String end_time; //有效時間
    private String created_time; //建立時間
    private String updated_time; //更新時間
    private String mail_type;// 0:noraml 2: emergency
    private int enabled;
    private String service_id; //作用頻道
    private int weight; //權重
    private String display_hour; //顯示時間, "0000-2359"
    private String scroll_speed; //"slow", "middle", "fast"
    private String font_color; //文字顏色
    private String bg_color; //文字背景顏色
    private String font_size; //字體大小
    private int repeat_time; //棄用, 沒有使用
    private int left_time; //顯示間隔頻率
    private int period; //顯示間隔時間(分)
    private int finger_print; //1 or 0 是否要在跑馬燈加入MAC字串浮水印
    private int finger_print_append_start; //1 or 0 是否浮水印在跑馬一開始
//    private int count; //跑過幾次
    private long next_show_time;

    public String ToString() {
        String str = "ticker :" + " content[" + content + "]" + " start_time[" + start_time + "]" + " end_time[" + end_time + "]"
                + " created_time[" + created_time + "]" + " updated_time[" + updated_time + "]" + " mail_type[" + mail_type + "]" + " enabled[" + enabled + "]"
                + " service_id[" + service_id + "]" + " weight[" + weight + "]" + " display_hour[" + display_hour + "]"
                + " scroll_speed[" + scroll_speed + "]" + " font_color[" + font_color + "]" + " bg_color[" + bg_color + "]"
                + " repeat_time[" + repeat_time + "]" + " left_time[" + left_time + "]" + " period[" + period + "]"
                + " finger_print[" + finger_print + "]" + " finger_print_append_start[" + finger_print_append_start
                + "]";
        return str;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public String getShowContent() {
        String Mac = Pvcfg.getEthernetMacAddress();
        if(Mac != null)
            Log.d("ticker","mac = "+Mac);
        else
            Mac = "";
        if(finger_print == 1) {
            if(finger_print_append_start == 1) {
                return Mac + " " + content;
            }
            else
                return content + " " + Mac;
        }
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getCreated_time() {
        return created_time;
    }

    public void setCreated_time(String created_time) {
        this.created_time = created_time;
    }

    public String getUpdated_time() {
        return updated_time;
    }

    public void setUpdated_time(String updated_time) {
        this.updated_time = updated_time;
    }

    public String getMail_type() {
        return mail_type;
    }

    public void setMail_type(String mail_type) {
        this.mail_type = mail_type;
    }

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getDisplay_hour() {
        return display_hour;
    }

    public void setDisplay_hour(String display_hour) {
        this.display_hour = display_hour;
    }

    public String getScroll_speed() {
        return scroll_speed;
    }

    public void setScroll_speed(String scroll_speed) {
        this.scroll_speed = scroll_speed;
    }

    public String getFont_color() {
        return font_color;
    }

    public void setFont_color(String font_color) {
        this.font_color = font_color;
    }

    public String getBg_color() {
        return bg_color;
    }

    public void setBg_color(String bg_color) {
        this.bg_color = bg_color;
    }

    public String getFont_size() {
        return font_size;
    }

    public void setFont_size(String font_size) {
        this.font_size = font_size;
    }

    public int getRepeat_time() {
        return repeat_time;
    }

    public void setRepeat_time(int repeat_time) {
        this.repeat_time = repeat_time;
    }

    public int getLeft_time() {
        return left_time;
    }

    public void setLeft_time(int left_time) {
        this.left_time = left_time;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getFinger_print() {
        return finger_print;
    }

    public void setFinger_print(int finger_print) {
        this.finger_print = finger_print;
    }

    public int getFinger_print_append_start() {
        return finger_print_append_start;
    }

    public void setFinger_print_append_start(int finger_print_append_start) {
        this.finger_print_append_start = finger_print_append_start;
    }

    public static Ticker parsing_ticker(JSONObject json) {
        Ticker ticker = new Ticker();
//        Log.d(TAG,"parsing ticker obj = "+ json.toString());
        ticker.id = json.optInt("id",0);
        ticker.content = json.optString("content","null");
        ticker.start_time = json.optString("start_time","null");
        ticker.end_time = json.optString("end_time","null");
        ticker.created_time = json.optString("created_time","null");
        ticker.updated_time = json.optString("updated_time","null");
        ticker.mail_type = json.optString("mail_type","null");
        ticker.enabled = json.optInt("enabled",0);
        ticker.service_id = json.optString("service_id","null");
        ticker.display_hour = json.optString("display_hour","null");
        ticker.weight = json.optInt("weight",0);
        String mail_ticker = json.optString("ticker");
        try {
            JSONObject ticker_json = new JSONObject(mail_ticker);
            ticker.scroll_speed = ticker_json.optString("scroll_speed","null");
            ticker.font_size = ticker_json.optString("font_size","null");
            ticker.font_color = ticker_json.optString("font_color","null");
            ticker.bg_color = ticker_json.optString("bg_color","null");
            ticker.repeat_time = ticker_json.optInt("repeat_time",0);
            ticker.left_time = ticker_json.optInt("left_time",0);
            ticker.period = ticker_json.optInt("period",0);
            ticker.finger_print = ticker_json.optInt("finger_print",0);
            ticker.finger_print_append_start = ticker_json.optInt("finger_print_append_start",0);
//            ticker.count = 0;
            ticker.next_show_time = 0;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Log.d(TAG,"ticker : "+ticker.ToString());
        return ticker;
    }

//    public int getCount() {
//        return count;
//    }
//
//    public void setCount(int count) {
//        this.count = count;
//    }

    public boolean checkNeedDisplay() {
        String time = display_hour;
        Date date = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmm");
        String result = timeFormat.format(date.getTime());
//        Log.d(TAG,"result = "+result+ " timeFormat = "+timeFormat);
        int now_time = Integer.parseInt(result);
        for (String str : time.split(":")) {
//            Log.d(TAG,"str = "+str);
            String[] split = str.split("-");
//            Log.d(TAG,"split = "+split);
            int start_time = Integer.valueOf(split[0]);
            int end_time = Integer.valueOf(split[1]);
//            Log.d(TAG,"checkNeedDisplay start_time = "+start_time+" end_time = "+end_time+" now time = " + now_time);
            if (now_time >= start_time && now_time < end_time) {
                return true;
            }
        }
        return false;
    }

    public long getNext_show_time() {
        return next_show_time;
    }

    public void setNext_show_time(long next_show_time) {
        this.next_show_time = next_show_time;
    }

    public void setNext_show_time() {
        Date date = new Date();
        this.next_show_time = date.getTime() + period*60000;
        date.setTime(next_show_time);
//        Log.d(TAG,"set next_show_time = "+date);
    }
}


