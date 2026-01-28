package com.prime.datastructure.sysdata;

import android.content.Context;
import android.util.Log;

public class MailInfo {
    private static final String TAG = "MailInfo";

    public static final int MAILUNREAD = 0;
    public static final int MAILREAD = 1;
    public static final int FORCE = 0;
    public static final int NORMAL = 1;

    public static final int EVENT_FORCE_TUNE = 5;
    public static final int EVENT_IDLE = 8;
    public static final int EVENT_OPEN_APP = 1;
    public static final int EVENT_OPEN_APP_PAGE = 2;
    public static final int EVENT_OPEN_BROWSER = 3;
    public static final int EVENT_OPEN_MAIL = 0;
    public static final int EVENT_OPEN_MAIL_PAGE = 7;
    public static final int EVENT_OPEN_VOD = 6;
    public static final int EVENT_OPEN_YOUTUBE = 4;
    public static final int LEVEL_EMERGENCY = 2001;
    public static final int LEVEL_NORMAL = 2000;
    public static final int LOCATION_CENTER_BOTTOM = 2;
    public static final int LOCATION_CENTER_TOP = 6;
    public static final int LOCATION_LEFT_BOTTOM = 3;
    public static final int LOCATION_LEFT_CENTER = 4;
    public static final int LOCATION_LEFT_TOP = 5;
    public static final int LOCATION_RIGHT_BOTTOM = 1;
    public static final int LOCATION_RIGHT_CENTER = 8;
    public static final int LOCATION_RIGHT_TOP = 7;
    public static final int MAIL_ALL = 1000;
    public static final int MAIL_COUPON = 1004;
    public static final int MAIL_EMERGENCY = 1007;
    public static final int MAIL_NEWS = 1002;
    public static final int MAIL_POPULAR_REC = 1003;
    public static final int MAIL_RESERVED = 1006;
    public static final int MAIL_SERVICE = 1005;
    public static final int MAIL_SHOPPING = 1001;
    private static final String MSG_GENRE_COUPON = "4";
    private static final String MSG_GENRE_NEWS = "2";
    private static final String MSG_GENRE_POPULAR_REC = "3";
    private static final String MSG_GENRE_RESERVED = "99";
    private static final String MSG_GENRE_SERVICE = "5";
    private static final String MSG_GENRE_SHOPPING = "1";
    public static final String PATH_MEMBER = "member";
    public static final String PATH_PAYMENT = "payment";
    public static final String PATH_SERVICE = "service";
    public static final String PATH_WPS = "wps";
    public static final int QRCODE_MEMBER = 3;
    public static final int QRCODE_PAYMENT = 2;
    public static final int QRCODE_SERVICE = 1;
    public static final int QRCODE_WPS = 4;
    public static final int REPEAT_TYPE_ONE_TIMES = 0;
    public static final int REPEAT_TYPE_UNLIMITED = 2;
    public static final int TYPE_CLOSE_REPEAT = 0;
    public static final int TYPE_CLOSE_REPEAT_IF_READ = 1;
    public static final int TYPE_FORCE_REPEAT = 2;
    public static final int TYPE_FORCE_TUNE = 3002;
    public static final int TYPE_MAIL = 3000;
    public static final int TYPE_TICKER = 3001;

    private int g_back_key;
    private long g_create_timestamp;
    private String g_dialog_button_content;
    private String g_dialog_photo_uri;
    private String g_display_time;
    private int g_enabled;
    private long g_end_timestamp;
    private int g_envelope_location;
    private int g_flag;
    private int g_id;
    private String g_internal_flag_1;
    private long g_last_show_timestamp;
    private int g_level;
    private int g_mail_id;
    private String g_mail_photo_uri;
    private String g_message;
    private int g_msg_genre;
    private int g_notify;
    private String g_promotion_button_content;
    private int g_read_status;
    private int g_repeat_type;
    private String g_service_id;
    private long g_start_timestamp;
    private String g_title;
    private int g_type;
    private long g_update_timestamp;
    private int g_weight;

    private String MailID = "";
    private String MailMsg = "";
    private int MailRead = MAILUNREAD;

    public void setMailID(String id) {
        MailID = id;
    }
    public String getMailID() {
        return MailID;
    }

    public void setMailMsg(String msg) {
        MailMsg = msg;
    }
    public String getMailMsg() {
        return MailMsg;
    }

    public void setMailRead(int read) {
        MailRead = read;
    }
    public int getMailRead() {
        return MailRead;
    }

    public static int get_repeat_type(int repeatType) {
        if (repeatType == 0) {
            return 0;
        }
        return repeatType == 1 ? 1 : 2;
    }

    public void set_id(int id) {
        this.g_id = id;
    }

    public int get_id() {
        return this.g_id;
    }

    public void set_mail_id(int id) {
        this.g_mail_id = id;
    }

    public int get_mail_id() {
        return this.g_mail_id;
    }

    public void set_enabled(int enabled) {
        this.g_enabled = enabled;
    }

    public int get_enabled() {
        return this.g_enabled;
    }

    public void set_level(int level) {
        this.g_level = level;
    }

    public int get_level() {
        return this.g_level;
    }

    public void set_type(int type) {
        this.g_type = type;
    }

    public int get_type() {
        return this.g_type;
    }

    public void set_msg_genre(int msgType) {
        this.g_msg_genre = msgType;
    }

    public int get_msg_genre() {
        return this.g_msg_genre;
    }

    public void set_envelope_location(int location) {
        this.g_envelope_location = location;
    }

    public int get_envelope_location() {
        return this.g_envelope_location;
    }

    public void set_start_timestamp(long time) {
        this.g_start_timestamp = time;
    }

    public long get_start_timestamp() {
        return this.g_start_timestamp;
    }

    public void set_end_timestamp(long time) {
        this.g_end_timestamp = time;
    }

    public long get_end_timestamp() {
        return this.g_end_timestamp;
    }

    public void set_create_timestamp(long time) {
        this.g_create_timestamp = time;
    }

    public long get_create_timestamp() {
        return this.g_create_timestamp;
    }

    public void set_update_timestamp(long time) {
        this.g_update_timestamp = time;
    }

    public long get_update_timestamp() {
        return this.g_update_timestamp;
    }

    public void set_dialog_photo_uri(String uri) {
        this.g_dialog_photo_uri = uri;
    }

    public String get_dialog_photo_uri() {
        return this.g_dialog_photo_uri;
    }

    public void set_mail_photo_uri(String uri) {
        this.g_mail_photo_uri = uri;
    }

    public String get_mail_photo_uri() {
        return this.g_mail_photo_uri;
    }

    public void set_title(String title) {
        this.g_title = title;
    }

    public String get_title() {
        return this.g_title;
    }

    public void set_message(String message) {
        this.g_message = message;
    }

    public String get_message() {
        return this.g_message;
    }

    public void set_read_status(int status) {
        this.g_read_status = status;
    }

    public int get_read_status() {
        return this.g_read_status;
    }

    public void set_service_id(String id) {
        this.g_service_id = id;
    }

    public String get_service_id() {
        return this.g_service_id;
    }

    public void set_display_time(String time) {
        this.g_display_time = time;
    }

    public String get_display_time() {
        return this.g_display_time;
    }

    public void set_dialog_button_content(String content) {
        this.g_dialog_button_content = content;
    }

    public String get_dialog_button_content() {
        return this.g_dialog_button_content;
    }

    public void set_promotion_button_content(String content) {
        this.g_promotion_button_content = content;
    }

    public String get_promotion_button_content() {
        return this.g_promotion_button_content;
    }

    public void set_back_key(int back) {
        this.g_back_key = back;
    }

    public int get_back_key() {
        return this.g_back_key;
    }

    public void set_repeat_type(int repeat) {
        this.g_repeat_type = repeat;
    }

    public int get_repeat_type() {
        return this.g_repeat_type;
    }

    public void set_notify(int notify) {
        this.g_notify = notify;
    }

    public int get_notify() {
        return this.g_notify;
    }

    public void set_flag(int flag) {
        this.g_flag = flag;
    }

    public int get_flag() {
        return this.g_flag;
    }

    public void set_last_show_timestamp(long time) {
        this.g_last_show_timestamp = time;
    }

    public long get_last_show_timestamp() {
        return this.g_last_show_timestamp;
    }

    public void set_weight(int weight) {
        this.g_weight = weight;
    }

    public int get_weight() {
        return this.g_weight;
    }

    public void set_internal_flag_1(String flag) {
        this.g_internal_flag_1 = flag;
    }

    public String get_internal_flag_1() {
        return this.g_internal_flag_1;
    }

    public void set_time(long startTs, long endTs) {
        this.g_start_timestamp = startTs;
        this.g_end_timestamp = endTs;
    }

    public String to_string() {
        return "MailBean{mId=" + this.g_id + ", mMailId=" + this.g_mail_id + ", mEnabled=" + this.g_enabled + ", mLevel=" + this.g_level + ", mType=" + this.g_type + ", mMsgGenre=" + this.g_msg_genre + ", mEnvelopeLocation=" + this.g_envelope_location + ", mStartTimeStamp=" + this.g_start_timestamp + ", mEndTimeStamp=" + this.g_end_timestamp + ", mCreateTimeStamp=" + this.g_create_timestamp + ", mUpdateTimeStamp=" + this.g_update_timestamp + ", mDialogPhotoUri='" + this.g_dialog_photo_uri + "', mMailPhotoUri='" + this.g_mail_photo_uri + "', mTitle='" + this.g_title + "', mMessage='" + this.g_message + "', mReadStatus=" + this.g_read_status + ", mServiceId='" + this.g_service_id + "', mDisplayTime='" + this.g_display_time + "', mBackKey=" + this.g_back_key + ", mRepeatType=" + this.g_repeat_type + ", mFlag=" + this.g_flag + ", mNotify=" + this.g_notify + ", mDialogButtonContent='" + this.g_dialog_button_content + "', mPromotionButtonContent='" + this.g_promotion_button_content + "', mLastShowTimeStamp=" + this.g_last_show_timestamp + ", mWeight=" + this.g_weight + ", mInternalFlag1=" + this.g_internal_flag_1 + '}';
    }

    public static int get_genre(String genre) {
        if (genre.equals(MSG_GENRE_SHOPPING)) {
            return MAIL_SHOPPING;
        }
        if (genre.equals("2")) {
            return MAIL_NEWS;
        }
        if (genre.equals("3")) {
            return MAIL_POPULAR_REC;
        }
        if (genre.equals(MSG_GENRE_COUPON)) {
            return MAIL_COUPON;
        }
        return genre.equals(MSG_GENRE_SERVICE) ? MAIL_SERVICE : MAIL_RESERVED;
    }
}
