package com.prime.dmg.launcher.Mail;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Comparator;

public class Mail {
    private static final String TAG = "Mail";

    public static final int UNREAD = 0;
    public static final int READ = 1;

    /*舊的 event type
    public static final int EVENT_OPEN_MAIL = 0;
    public static final int EVENT_OPEN_APP = 1;
    public static final int EVENT_OPEN_APP_PAGE = 2;
    public static final int EVENT_OPEN_BROWSER = 3;
    public static final int EVENT_OPEN_YOUTUBE = 4;
    public static final int EVENT_FORCE_TUNE = 5;
    public static final int EVENT_OPEN_VOD = 6;
    public static final int EVENT_OPEN_MAIL_PAGE = 7;
    public static final int EVENT_IDLE = 8;*/

    //新的 event type
    public static final int EVENT_OPEN_MAIL             = 0;
    public static final int EVENT_OPEN_APP              = 1;
    public static final int EVENT_OPEN_YOUTUBE          = 4;
    public static final int EVENT_FORCE_TUNE            = 5;
    public static final int EVENT_OPEN_APP_CP           = 7;
    public static final int EVENT_OPEN_APP_CP_MULTI     = 8;
    public static final int EVENT_OPEN_POSTER           = 9;
    public static final int EVENT_OPEN_BROWSER          = 10;
    public static final int EVENT_OPEN_STREAM           = 11;
    public static final int EVENT_OPEN_QRCODE           = 12;
    public static final int EVENT_OPEN_PROGRAM_DETAIL   = 13;

    public static final int MAIL_CATEGORY_SHOPPING = 1;
    public static final int MAIL_CATEGORY_NEWS = 2;
    public static final int MAIL_CATEGORY_POPULAR = 3;
    public static final int MAIL_CATEGORY_COUPON = 4;
    public static final int MAIL_CATEGORY_SERVICE = 5;

    public static final int TYPE_CLOSE_REPEAT = 0;
    public static final int TYPE_CLOSE_REPEAT_IF_READ = 1;
    public static final int TYPE_FORCE_REPEAT = 2;

    public static final String MAIL_TYPE_NORMAL =   "0";
    public static final String MAIL_TYPE_EMERGENCY =   "2";

    private int id;
    private String title;
    private String content;
    private String start_time;
    private String end_time;
    private String created_time;
    private String updated_time;
    private int mail_category;
    private String mail_type;
    private String mail_display_type;
    private int enabled;
    private int page_type;
    private String service_id;
    private String display_hour;
    private int weight;
    private int envelope_location;
    private int flag;
    private String envelope_icon; //信封圖示
    private int is_back; //是否支援返回鍵關閉
    private String content_icon; //url //信封內容圖示
    private String qrcode; //url //信封內容QRCode
    private int repeat_type;
    private String qrItem;
    private String mail_detail_envelope;
    private String mail_detial_dialog;
    private String mail_force_tune;
    private String mail_ticker;
    private int read_status;

    public class ForceTune {
        private int srid;
        private int tsid;

        public ForceTune() {
            this.srid = 0;
            this.tsid = 0;
        }
        public String ToString() {
            String str = "force tune : srid["+srid+"] tsid["+tsid+"]";
            return str;
        }

        public int getSrid() {
            return srid;
        }

        public void setSrid(int srid) {
            this.srid = srid;
        }

        public int getTsid() {
            return tsid;
        }

        public void setTsid(int tsid) {
            this.tsid = tsid;
        }
    }

    public Mail() {

    }

    public String toString() {
        String str = "mail : ["+
                     " id = " + id + " title = " + title + " content = " + content + " start_time = " + start_time + " end_time = " + end_time +
                     " created_time = " + created_time + " updated_time = " + updated_time + " mail_category = " + mail_category +
                     " mail_type = " + mail_type + " mail_display_type = " + mail_display_type +
                     " enabled = " + enabled + " page_type = " + page_type + " service_id = " + service_id + " display_hour = " + display_hour +
                     " weight = " + weight + " envelope_location = " + envelope_location + " flag = " + flag + " envelope_icon = " + envelope_icon +
                     " is_back = " + is_back + " content_icon = " + content_icon + " qrcode = " + qrcode + " repeat_type = " + repeat_type +
                     " qrItem = " + qrItem + " mail_detail_envelope = " + mail_detail_envelope + " mail_ticker = " + mail_ticker +
                     " mail_detial_dialog = " + mail_detial_dialog + " mail_force_tune = " + mail_force_tune + " read = " + read_status +
                     "]";
        return str;
    }

    public ArrayList<MailDetail> get_mail_detail_envelope() {
        ArrayList<MailDetail> list = MailDetail.getMailDetailData(this.mail_detail_envelope);

        /*
        MailDetail mailDetail = new MailDetail();
        mailDetail.setTitle("開啟信件內容");
        mailDetail.setHot_key("ok");
        mailDetail.setOrder(1);
        mailDetail.setEvent_type(0);

        MailDetail mailDetail1 = new MailDetail();
        mailDetail1.setTitle("開啟 App");
        mailDetail1.setHot_key("green");
        mailDetail1.setOrder(2);
        mailDetail1.setEvent_type(1);

        MailDetail mailDetail2 = new MailDetail();
        mailDetail2.setTitle("開啟 App");
        mailDetail2.setHot_key("blue");
        mailDetail2.setOrder(3);
        mailDetail2.setEvent_type(1);

        list.add(mailDetail1);
        list.add(mailDetail2);
        list.add(mailDetail);*/

        list.sort(Comparator.comparingInt(MailDetail::getOrder));

//        Log.d(TAG,"envelope list.size() = " + list.size());
//        for(int i = 0; i < list.size(); i++) {
//            Log.d(TAG,"envelope : "+list.get(i).toString());
//        }
        return list;
    }

    public ArrayList<MailDetail> get_mail_detail_dialog() {
        ArrayList<MailDetail> list = MailDetail.getMailDetailData(this.mail_detial_dialog);
        /*
        MailDetail mailDetail = new MailDetail();
        mailDetail.setEvent_type(1);
        mailDetail.setEvent_value("com.litv.cable.home");
        mailDetail.setSourceType("1009");
        mailDetail.setTypeName("APP");
        mailDetail.setItemId("83");
        mailDetail.setOrder(2);
        mailDetail.setPoster("");
        mailDetail.setTitle("開啟應用程式");
        mailDetail.setPackageName("com.litv.cable.home");
        list.add(mailDetail);

        MailDetail mailDetail1 = new MailDetail();
        mailDetail1.setEvent_type(1);
        mailDetail1.setEvent_value("com.litv.cable.home");
        mailDetail1.setSourceType("1009");
        mailDetail1.setTypeName("APP");
        mailDetail1.setItemId("84");
        mailDetail1.setOrder(1);
        mailDetail1.setPoster("");
        mailDetail1.setTitle("開啟app");
        mailDetail1.setPackageName("com.litv.cable.home");
        list.add(mailDetail1);*/

        list.sort(Comparator.comparingInt(MailDetail::getOrder));
//        Log.d(TAG,"dialog list.size() = " + list.size());
//        for(int i = 0; i < list.size(); i++) {
//            Log.d(TAG,"dialog : "+list.get(i).toString());
//        }
        return list;
    }

    public void setMail_force_tune(String str) {
        this.mail_force_tune = str;
    }

    public Mail.ForceTune get_mail_force_tune() {
        Mail.ForceTune forceTune = null;
        if(!mail_force_tune.equals("null")) {
            try {
                JSONObject jsonObject = new JSONObject(mail_force_tune);
                String srid_str = jsonObject.optString("srid","null");
                String tsid_str = jsonObject.optString("tsid","null");
                forceTune = new ForceTune();
                if(!srid_str.equals("null")&&srid_str.length()>0)
                    forceTune.setSrid(Integer.valueOf(srid_str));
                if(!tsid_str.equals("null")&&tsid_str.length()>0)
                    forceTune.setTsid(Integer.valueOf(tsid_str));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            Log.d(TAG,"force tune : srid["+forceTune.getSrid()+"] tsid["+forceTune.getTsid()+"]");
        }
        return forceTune;
    }


    public static Mail parsing_mail(JSONObject json) {
        Mail mail = new Mail();

        mail.id = json.optInt("id",0);
        mail.title = json.optString("title","null");
        mail.content = json.optString("content","null");
        mail.start_time = json.optString("start_time","null");
        mail.end_time = json.optString("end_time","null");
        mail.created_time = json.optString("created_time","null");
        mail.updated_time = json.optString("updated_time","null");
        mail.mail_category = json.optInt("mail_category",0);
        mail.mail_type = json.optString("mail_type","null");
        mail.mail_display_type = json.optString("mail_display_type","null");
        mail.enabled = json.optInt("enabled",0);
        mail.page_type = json.optInt("page_type",0);
        mail.service_id = json.optString("service_id","null");
        mail.display_hour = json.optString("display_hour","null");
        mail.weight = json.optInt("weight",0);
        mail.envelope_location = json.optInt("envelope_location");
        {
            String mail_detail = json.optString("mail_detail","null");
//            Log.d(TAG,"mail_detail = "+mail_detail);
            if(!mail_detail.equals("null")) {
                try {
                    JSONObject jsonObject = new JSONObject(mail_detail);
                    mail.mail_detail_envelope = jsonObject.getString("envelope");
//                    Log.d(TAG,"envelope = "+mail.mail_detail_envelope);
                    mail.mail_detial_dialog = jsonObject.optString("dialog");
//                    Log.d(TAG,"dialog = "+mail.mail_detial_dialog);
                    mail.flag = jsonObject.optInt("flag",0);
                    mail.envelope_icon = jsonObject.optString("envelope_icon","null");
                    mail.is_back = jsonObject.optInt("is_back",0);
                    mail.content_icon = jsonObject.optString("content_icon","null");
                    mail.qrcode = jsonObject.optString("qrcode","null");
                    mail.repeat_type = jsonObject.optInt("repeat_type",0);
                    mail.qrItem = jsonObject.optString("qrItem","null");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        mail.mail_force_tune = json.optString("force_tune","null");
        mail.mail_ticker = json.optString("ticker");
//        Log.d(TAG,"mail : "+mail.toString());
        mail.get_mail_detail_envelope();
        mail.get_mail_detail_dialog();
        return mail;
    }

    public void parser_force_tune(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String tsid = jsonObject.getString("tsid");
            String srid = jsonObject.getString("srid");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
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

    public int getMail_category() {
        return mail_category;
    }

    public void setMail_category(int mail_category) {
        this.mail_category = mail_category;
    }

    public String getMail_type() {
        return mail_type;
    }

    public void setMail_type(String mail_type) {
        this.mail_type = mail_type;
    }

    public String getMail_display_type() {
        return mail_display_type;
    }

    public void setMail_display_type(String mail_display_type) {
        this.mail_display_type = mail_display_type;
    }

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public int getPage_type() {
        return page_type;
    }

    public void setPage_type(int page_type) {
        this.page_type = page_type;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getDisplay_hour() {
        return display_hour;
    }

    public void setDisplay_hour(String display_hour) {
        this.display_hour = display_hour;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getEnvelope_location() {
        return envelope_location;
    }

    public void setEnvelope_location(int envelope_location) {
        this.envelope_location = envelope_location;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getEnvelope_icon() {
        return envelope_icon;
    }

    public void setEnvelope_icon(String envelope_icon) {
        this.envelope_icon = envelope_icon;
    }

    public int getIs_back() {
        return is_back;
    }

    public void setIs_back(int is_back) {
        this.is_back = is_back;
    }

    public String getContent_icon() {
        return content_icon;
    }

    public void setContent_icon(String content_icon) {
        this.content_icon = content_icon;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public int getRepeat_type() {
        return repeat_type;
    }

    public void setRepeat_type(int repeat_type) {
        this.repeat_type = repeat_type;
    }

    public String getQrItem() {
        return qrItem;
    }

    public void setQrItem(String qrItem) {
        this.qrItem = qrItem;
    }

    public String getMail_detail_envelope() {
        return mail_detail_envelope;
    }

    public void setMail_detail_envelope(String mail_detail_envelope) {
        this.mail_detail_envelope = mail_detail_envelope;
    }

    public String getMail_detial_dialog() {
        return mail_detial_dialog;
    }

    public void setMail_detial_dialog(String mail_detial_dialog) {
        this.mail_detial_dialog = mail_detial_dialog;
    }

    public int getRead_status() {
        return read_status;
    }

    public void setRead_status(int read_status) {
        this.read_status = read_status;
    }

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
}
