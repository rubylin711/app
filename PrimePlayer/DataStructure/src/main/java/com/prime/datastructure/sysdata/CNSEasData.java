package com.prime.datastructure.sysdata;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CNSEasData {
    private static final String TAG = "CNSEasData";

    // 定義資料庫欄位名稱常數 (依照慣例，通常對應成員變數名稱)
    public static final String ID = "id";
    public static final String REPEAT_COUNT = "repeatCount";
    public static final String FOREGROUND_COLOR = "foregroundColor";
    public static final String BACKGROUND_COLOR = "backgroundColor";
    public static final String ALERT_LEVEL = "alertLevel";
    public static final String ALERT_TYPE = "alertType";
    public static final String ALERT_SOUND = "alertSound";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
    public static final String ALERT_TITLE = "alertTitle";
    public static final String ALERT_MESSAGE = "alertMessage";
    // Force Tune 相關欄位
    public static final String FORCE_TUNE_ON_ID = "forceTune_on_id";
    public static final String FORCE_TUNE_TS_ID = "forceTune_ts_id";
    public static final String FORCE_TUNE_SERVICE_ID = "forceTune_service_id";
    public static final String RECEIVE_TIME = "receive_time";
    public static final String READ = "read";
    public static final String ALREADY_SHOWN = "alreadyShown";

    // 成員變數
    private int id;
    private int repeatCount;
    private int foregroundColor; // 建議儲存為 Android Color Int
    private int backgroundColor; // 建議儲存為 Android Color Int
    private int alertLevel;      // 0: low, 1: normal, 2: High
    private int alertType;       // 0: marquee, 1: mail, 2: popup
    private int alertSound;      // 0: none, 1: alert1, 2: alert2
    private long startTime;      // Unix Timestamp
    private long endTime;        // Unix Timestamp
    private String alertTitle;
    private String alertMessage;

    // forceTune: [original_network_id]/[transport_id]/[service_id]
    // 建議若無強制切台需求，這些值預設為 -1
    private int forceTune_on_id;
    private int forceTune_ts_id;
    private int forceTune_service_id;

    private long receiveTime;
    private int read;            // 0: unread, 1: read
    private int alreadyShown;    // 0: not shown, 1: shown

    // 建構子
    public CNSEasData() {
    }

    // Getter & Setter

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRepeatCount() { return repeatCount; }
    public void setRepeatCount(int repeatCount) { this.repeatCount = repeatCount; }

    public int getForegroundColor() { return foregroundColor; }
    public void setForegroundColor(int foregroundColor) { this.foregroundColor = foregroundColor; }

    public int getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(int backgroundColor) { this.backgroundColor = backgroundColor; }

    public int getAlertLevel() { return alertLevel; }
    public void setAlertLevel(int alertLevel) { this.alertLevel = alertLevel; }

    public int getAlertType() { return alertType; }
    public void setAlertType(int alertType) { this.alertType = alertType; }

    public int getAlertSound() { return alertSound; }
    public void setAlertSound(int alertSound) { this.alertSound = alertSound; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    // 輔助方法：格式化開始時間
    public String getFormattedStartTime() {
        if (startTime == 0) return "";
        // 注意：Unix Timestamp 有時是秒，Java Date 需要毫秒，若輸入是秒請 * 1000
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(startTime * 1000L));
    }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    // 輔助方法：格式化結束時間
    public String getFormattedEndTime() {
        if (endTime == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(endTime * 1000L));
    }

    public String getAlertTitle() { return alertTitle; }
    public void setAlertTitle(String alertTitle) { this.alertTitle = alertTitle; }

    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }

    public int getForceTune_on_id() { return forceTune_on_id; }
    public void setForceTune_on_id(int forceTune_on_id) { this.forceTune_on_id = forceTune_on_id; }

    public int getForceTune_ts_id() { return forceTune_ts_id; }
    public void setForceTune_ts_id(int forceTune_ts_id) { this.forceTune_ts_id = forceTune_ts_id; }

    public int getForceTune_service_id() { return forceTune_service_id; }
    public void setForceTune_service_id(int forceTune_service_id) { this.forceTune_service_id = forceTune_service_id; }

    public long getReceiveTime() { return receiveTime; }
    public void setReceiveTime(long receiveTime) { this.receiveTime = receiveTime; }
    public String getFormattedReceiveTime() {
        if (receiveTime == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(receiveTime));
    }

    public int getRead() { return read; }
    public void setRead(int read) { this.read = read; }

    public int getAlreadyShown() { return alreadyShown; }
    public void setAlreadyShown(int alreadyShown) { this.alreadyShown = alreadyShown; }

    @Override
    public String toString() {
        return "CNSEasData{" +
                "id=" + id +
                ", repeatCount=" + repeatCount +
                ", foregroundColor=" + foregroundColor +
                ", backgroundColor=" + backgroundColor +
                ", level=" + alertLevel +
                ", type=" + alertType +
                ", sound=" + alertSound +
                ", start=" + getFormattedStartTime() +
                ", end=" + getFormattedEndTime() +
                ", title='" + alertTitle + '\'' + // 為了方便辨識，印出 title
                ", message='" + alertMessage + '\'' +
                ", forceTune=" + forceTune_on_id + "/" + forceTune_ts_id + "/" + forceTune_service_id +
                ", receiveTime=" + getFormattedReceiveTime() +
                ", read=" + read +
                ", shown=" + alreadyShown +
                '}';
    }
}