package com.prime.dmg.launcher.Mail;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.prime.dmg.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.dmg.launcher.ACSDatabase.ACSHelper;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyInfo;
import com.prime.dmg.launcher.Home.Marquee.MarqueeManager;
import com.prime.dmg.launcher.Home.QRCode.QRCodeDialog;
import com.prime.dmg.launcher.Home.Recommend.Stream.StreamActivity;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.Ticker.Ticker;
import com.prime.dmg.launcher.Utils.ActivityUtils;
import com.prime.dmg.launcher.Utils.JsonParser.AdPageItem;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.service.database.dvbdatabasetable.MailDatabaseTable;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

public class MailManager {
    private static final String TAG = "MailManager";

    public static final int TYPE_MAIL = 0;
    public static final int TYPE_TICKER = 1;

    WeakReference<AppCompatActivity> g_ref;
    private static MailManager g_mail_manager = null;
    private Semaphore g_semaphore = new Semaphore(1);
    private ArrayList<WorkingMail> g_workingMailArrayList = null;
    private ArrayList<WorkingTicker> g_workingTickerArrayList = null;

    Handler g_handler_working_mail,g_handler_working_ticker;

    private Runnable working_mail_check_runnable = new Runnable() {
        @Override
        public void run() {
            check_working_mail_to_delete();
            check_working_mail_to_do();
            g_handler_working_mail.postDelayed(working_mail_check_runnable,5000);
        }
    };

    private Runnable working_ticker_check_runnable = new Runnable() {
        @Override
        public void run() {
            check_working_ticker_to_delete();
            check_working_ticker_to_do();
            g_handler_working_ticker.postDelayed(working_ticker_check_runnable,1000);

        }
    };

    public class WorkingMail {
        public MailData mailData;
        public Mail mail;
        public boolean current_display;
//        public boolean already_shown;
    }

    public class WorkingTicker {
        public MailData mailData;
        public Ticker ticker;
        public boolean current_display;
    }

    public static MailManager GetInstance(AppCompatActivity activity) {
        if(g_mail_manager == null ) {
            g_mail_manager = new MailManager(activity);
        }
        return g_mail_manager;
    }

    public MailManager(AppCompatActivity activity) {
        if(activity != null)
            this.g_ref = new WeakReference<>(activity);
//        g_mail_manager.g_dtv = get().g_dtv;
        this.g_workingMailArrayList = new ArrayList<>();
        this.g_workingTickerArrayList = new ArrayList<>();
//        g_ref = new WeakReference<>(activity);
        this.g_handler_working_mail = new Handler(Looper.getMainLooper());
        this.g_handler_working_ticker = new Handler(Looper.getMainLooper());

    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public void lock_task() {
        try {
//            Log.d("ticker","lock_task");
            g_semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void unlock_task() {
//        Log.d("ticker","unlock_task");
        g_semaphore.release();
    }
    public void build_working_mail_from_db(Context context) {
        List<MailData> mailDataList = new MailDatabaseTable(context).load();
        boolean is_show_ticker = false ,is_show_mail = false;
        Log.d(TAG,"build_working_mail_from_db mailDataList.size() = "+mailDataList.size());
        if(mailDataList.size() > 0) {
            for(int i = 0; i < mailDataList.size(); i++) {
                try {
                    MailData mailData = mailDataList.get(i);
                    JSONObject jsonObject = new JSONObject(mailData.getData());
                    String mail_display_type = jsonObject.optString("mail_display_type","null");
                    String start_time_str = jsonObject.optString("start_time","null");
                    String end_time_str = jsonObject.optString("end_time","null");
                    int mail_id = jsonObject.optInt("id");
                    int mail_category = jsonObject.optInt("mail_category",0);

                    Date now = Calendar.getInstance().getTime();
                    long now_time = now.getTime();
                    long start_time = get_timestamp_no_second(start_time_str);
                    long end_time = get_timestamp_no_second(end_time_str);
//                    Log.d(TAG,"end time : "+end_time_str+" long value : "+end_time);
//                    Log.d(TAG,"now time : "+now+" long value : "+now_time);
//                    Log.d(TAG,"mail type["+mail_display_type+"]" );
                    if (now_time > end_time) {
//                        Log.d(TAG,"mail date expired !!");
                        if(mail_category == Mail.MAIL_CATEGORY_COUPON)
                            delete_mail_of_db(context, mail_id);
                        //not build to working mail
                    } else {
                        if(mail_display_type.equals("mail")) {
                            WorkingMail workingMail = new WorkingMail();
                            workingMail.mailData = mailData;
                            workingMail.mail = Mail.parsing_mail(jsonObject);
                            workingMail.mail.setRead_status(mailData.getRead());
                            g_workingMailArrayList.add(workingMail);
                            is_show_mail = true;
                        }
                        else if(mail_display_type.equals("ticker")) {
                            Date date = new Date();
                            WorkingTicker workingTicker = new WorkingTicker();
                            workingTicker.mailData = mailData;
                            workingTicker.ticker = Ticker.parsing_ticker(jsonObject);
                            workingTicker.ticker.setNext_show_time(date.getTime());
                            g_workingTickerArrayList.add(workingTicker);
                            is_show_ticker = true;
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            if(is_show_ticker) {
                g_handler_working_ticker.postDelayed(working_ticker_check_runnable,1000);
            }
            if(is_show_mail){
                g_handler_working_mail.postDelayed(working_mail_check_runnable, 1000);
            }
        }
    }

    private int find_working_mail_array_index_with_same_mail_id(int mail_id) {
        if(g_workingMailArrayList != null) {
            for(int i = 0; i < g_workingMailArrayList.size(); i++) {
                if(g_workingMailArrayList.get(i).mail.getId() == mail_id)
                    return i;
            }
        }
        return  -1;
    }

    private int find_working_ticker_array_index_with_same_mail_id(int mail_id) {
        if(g_workingTickerArrayList != null) {
            for(int i = 0; i < g_workingTickerArrayList.size(); i++) {
                if(g_workingTickerArrayList.get(i).ticker.getId() == mail_id)
                    return i;
            }
        }
        return  0;
    }

    public int parser_acs_mail_data(Context context, /*Handler handler,*/ int call_by_service) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        JSONArray jsonArrayMail = null;

        boolean is_new_mail = false,is_new_ticker = false;

        try {
            lock_task();
            String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"tv_mail");
//            Log.d(TAG,"acs_data = "+acs_data);
            if(acs_data == null || acs_data.equals("null")) {
                unlock_task();
                return 1;
            }
            jsonArrayMail = new JSONArray(acs_data);
            for(int i = 0; i < jsonArrayMail.length(); i++) {
                JSONObject jsonObject = jsonArrayMail.getJSONObject(i);
                String str = jsonObject.toString();
                int mail_id = jsonObject.optInt("id");
//                Log.d(TAG,"acs_data["+i+"] = "+str);

                if(jsonObject != null && mail_id != 0) {
                    MailData mailDataOfDatabase = get_mail_from_db(context,mail_id);
//                    if(mailDataOfDatabase != null)
//                        Log.d(TAG,"mailDataOfDatabase = "+mailDataOfDatabase.getData());
                    if(mailDataOfDatabase != null && mailDataOfDatabase.getData().equals(str)) {
//                        Log.d(TAG,"same as db , continue");
                        continue;
                    }
                    else {
//                        Log.d(TAG,"new mail : "+str);
                        String mail_display_type = jsonObject.optString("mail_display_type","null");
                        Log.d(TAG,"new mail , mail type : "+mail_display_type);
                        int enable = jsonObject.optInt("enabled",0);
                        if(mail_display_type.equals("forcetuneonly")) {
                            String tune_str = jsonObject.optString("force_tune","null");
                            if(!tune_str.equals("null") && enable == 1) {
                                Mail tune_mail = new Mail();
                                tune_mail.setMail_force_tune(jsonObject.optString("force_tune","null"));
                                if(call_by_service == 1) {
                                    unlock_task();
                                    return 1;
                                }
                                force_tune(tune_mail);
                            }
                        }
                        else {
                            MailData mailData = new MailData();
                            mailData.setId(mail_id);
                            mailData.setData(jsonObject.toString());
//                            Log.d(TAG,"to db str = "+jsonObject.toString());
//                            Log.d(TAG,"mailData.getData = "+mailData.getData());
                            mailData.setRead(Mail.UNREAD);
                            mailData.setAlready_shown(0);

                            save_maildata_to_db(context, mailData);
                            if(mail_display_type.equals("mail")) {
                                WorkingMail workingMail = new WorkingMail();
                                workingMail.mailData = mailData;
                                workingMail.mail = Mail.parsing_mail(jsonObject);
                                //Log.d(TAG, "parser_acs_mail_data: workingMail.mail" + workingMail.mail.toString());
                                //Log.d(TAG,"mailDataOfDatabase : "+mailDataOfDatabase);
                                //Log.d(TAG,"mail_id : "+mail_id);
                                if (mailDataOfDatabase != null) {//same mail id
                                    int mail_array_index = find_working_mail_array_index_with_same_mail_id(mail_id);
                                    if(enable == 1) {
                                        //Log.d(TAG,"mail_array_index : "+mail_array_index);
                                        if(mail_array_index != -1) {
                                            g_workingMailArrayList.remove(mail_array_index);
                                            g_workingMailArrayList.add(mail_array_index, workingMail);
                                        }else{
                                            g_workingMailArrayList.add(workingMail);
                                        }
                                        is_new_mail = true;
                                    }
                                    else {
                                        if(mail_array_index != -1) {
                                            g_workingMailArrayList.remove(mail_array_index);
                                        }
                                        delete_maildata_of_db(context,mailData);
                                    }
                                }
                                else {
                                    g_workingMailArrayList.add(workingMail);
                                    is_new_mail = true;
                                }
                            }
                            else if(mail_display_type.equals("ticker")) {
                                Date date = new Date();
                                WorkingTicker workingTicker = new WorkingTicker();
                                workingTicker.mailData = mailData;
                                workingTicker.ticker = Ticker.parsing_ticker(jsonObject);
                                workingTicker.ticker.setNext_show_time(date.getTime());
                                if (mailDataOfDatabase != null) {//same mail id
                                    int mail_array_index = find_working_ticker_array_index_with_same_mail_id(mail_id);
                                    if(enable == 1) {
                                        if(mail_array_index != -1) {
                                            g_workingTickerArrayList.remove(mail_array_index);
                                            g_workingTickerArrayList.add(mail_array_index,workingTicker);
                                            if(get().g_marqueeMgr.marquee_isRunning() && get().g_marqueeMgr.marquee_get_ticker_id() == mail_id)
                                                stop_running_ticker();
                                            is_new_ticker = true;
                                        }
                                    }
                                    else {
                                        if(mail_array_index != -1) {
                                            g_workingTickerArrayList.remove(mail_array_index);
                                            if(get().g_marqueeMgr.marquee_isRunning() && get().g_marqueeMgr.marquee_get_ticker_id() == mail_id)
                                                stop_running_ticker();
                                        }
                                        delete_maildata_of_db(context,mailData);
                                    }
                                }
                                else {
                                    g_workingTickerArrayList.add(workingTicker);
                                    is_new_ticker = true;
                                }
                            }
                        }
                    }
                }
            }
            //LogUtils.d("call_by_service = "+call_by_service+" is_new_mail = "+is_new_mail);
            if(call_by_service == 0) {
                acsDataProviderHelper.delete_acs_provider_data(context, "tv_mail");
                unlock_task();
                if (is_new_mail) {
                    Intent intent = new Intent(MailActivity.MSG_NEW_MAIL);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    do_unread_mail_check();
                }
                if (is_new_ticker)
                    do_unread_ticker_check();
            }
            else {
                unlock_task();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            unlock_task();
            throw new RuntimeException(e);
        }
        return 0;
    }

    public void do_unread_mail_check() {
        g_handler_working_mail.removeCallbacks(working_mail_check_runnable);
        g_handler_working_mail.post(working_mail_check_runnable);
    }

    public void do_unread_ticker_check() {
        g_handler_working_ticker.removeCallbacks(working_ticker_check_runnable);
        g_handler_working_ticker.post(working_ticker_check_runnable);
    }

    public GposInfo gpos_info_get() {
        if (get().g_dtv == null) Log.d(TAG, "get().g_dtv == null");

        return get().g_dtv.gpos_info_get();
    }

    public void gpos_info_update_by_key_string(String key, int value) {
        get().g_dtv.gpos_info_update_by_key_string(key, value);
    }

    public void save_mail_read_flag_to_db(Context context,Mail mail) {
        MailDatabaseTable mailDatabaseTable = new MailDatabaseTable(context);
        MailData mailDataOfDatabase = get_mail_from_db(context,mail.getId());
        if(mailDataOfDatabase != null && mailDataOfDatabase.getId() != 0) {
            mailDataOfDatabase.setRead(mail.getRead_status());
            mailDatabaseTable.save(mailDataOfDatabase);
        }
    }

    public void delete_mail_of_db(Context context, int mailId) {
        MailDatabaseTable mailDatabaseTable = new MailDatabaseTable(context);
        mailDatabaseTable.remove(mailId);
        for (int i = 0; i < g_workingMailArrayList.size(); i++) {
            if (g_workingMailArrayList.get(i).mailData.getId() == mailId) {
                g_workingMailArrayList.remove(i);
                break;
            }
        }
    }

    public void delete_maildata_of_db(Context context,MailData mailData) {
        MailDatabaseTable mailDatabaseTable = new MailDatabaseTable(context);
        mailDatabaseTable.remove(mailData);
        Log.d(TAG,"delete mail : "+mailData.ToString());
    }

    public void save_maildata_to_db(Context context,MailData mailData) {
        MailDatabaseTable mailDatabaseTable = new MailDatabaseTable(context);
        mailDatabaseTable.save(mailData);
    }

    public MailData get_mail_from_db(Context context,int id) {
        MailDatabaseTable mailDatabaseTable = new MailDatabaseTable(context);
        MailData mailData = mailDatabaseTable.query(id);
        return mailData;
    }

    public List<Mail> get_mail_list_from_db(Context context){ // for ui mail page to show
        List<MailData> mailDataList = new MailDatabaseTable(context).load();
        List<Mail> mailList = new ArrayList<>();
        if(mailDataList.size() > 0) {
            for(int i = 0; i < mailDataList.size(); i++) {
                try {
                    MailData mailData = mailDataList.get(i);
                    JSONObject jsonObject = new JSONObject(mailData.getData());
                    Mail mail = Mail.parsing_mail(jsonObject);
                    mail.setRead_status(mailData.getRead());
                    //Log.d(TAG, "get_mail_list_from_db: " + mail.getMail_display_type());
                    if (mail.getMail_display_type().equals("mail"))
                        mailList.add(mail);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return mailList;
    }

    public long get_timestamp_no_second(String date_time) {
        long timestamp = 0;
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date date = null;
            date = dateParser.parse(date_time);
            timestamp = date.getTime();

        } catch (ParseException e) {
            //throw new RuntimeException(e);
        }
        return timestamp;
    }

    public void check_working_mail_to_delete() {
        lock_task();
        if(g_workingMailArrayList != null) {
            for(int i=0; i<g_workingMailArrayList.size(); i++) {
                Date now = Calendar.getInstance().getTime();
                long now_time = now.getTime();
                long start_time = get_timestamp_no_second(g_workingMailArrayList.get(i).mail.getStart_time());
                long end_time = get_timestamp_no_second(g_workingMailArrayList.get(i).mail.getEnd_time());
//                Log.d(TAG,"end time : "+g_workingMailArrayList.get(i).mail.getEnd_time()+" long value : "+end_time);
//                Log.d(TAG,"now time : "+now+" long value : "+now_time);
                if(now_time > end_time) {//over exist time, delete from working mail
                    LogUtils.e( "Delete mail index: "+i);
                    ACSHelper.do_acs_command(ACSHelper.MSG_CLOSE_MAIL, g_workingMailArrayList.get(i).mail, i, g_workingMailArrayList.get(i).mail.getFlag());
                    if(g_workingMailArrayList.get(i).mail.getMail_category() == Mail.MAIL_CATEGORY_COUPON)
                        delete_mail_of_db(get(), g_workingMailArrayList.get(i).mail.getId());
                    else
                        g_workingMailArrayList.remove(i);
                    i--;
                }
            }
        }
        unlock_task();

    }

    public void check_working_mail_to_do() {
        lock_task();
        if(g_workingMailArrayList != null) {
            for(int i=0; i<g_workingMailArrayList.size(); i++) {
                WorkingMail workingMail = g_workingMailArrayList.get(i);
                Date now = Calendar.getInstance().getTime();
                long now_time = now.getTime();
                long start_time = get_timestamp_no_second(workingMail.mail.getStart_time());
                long end_time = get_timestamp_no_second(workingMail.mail.getEnd_time());
                long service_id = ChannelChangeManager.get_instance(get()).get_cur_channel().getServiceId();
                //Log.d(TAG,"mail end time : "+workingMail.mail.getEnd_time()+" long value : "+end_time);
                //Log.d(TAG,"mail now time : "+now+" long value : "+now_time);
                if(now_time > end_time) {//not do envelope
                    Log.e(TAG, "Delete mail");
                    ACSHelper.do_acs_command(ACSHelper.MSG_CLOSE_MAIL, workingMail.mail, i, workingMail.mail.getFlag());
                    if(workingMail.mail.getMail_category() == Mail.MAIL_CATEGORY_COUPON)
                        delete_mail_of_db(get(), workingMail.mail.getId());
                    g_workingMailArrayList.remove(i);
                    i--;
                }else if(now_time < start_time){
                    LogUtils.d("Don't reach the start time , skip it");
                }else{
                    //Log.d(TAG, "getRead_status = "+workingMail.mail.getRead_status());
                    //Log.d(TAG, "getRepeat_type = "+workingMail.mail.getRepeat_type());
                    if (workingMail.mail.getRead_status() == Mail.UNREAD|| workingMail.mail.getRepeat_type() == Mail.TYPE_FORCE_REPEAT) {
                        if ((ACSHelper.showMailIcon(get()) && checkMailGenre(workingMail.mail)) && workingMail.mail.checkNeedDisplay()) {

                            String[] stringArray = workingMail.mail.getService_id().split(":");
                            List<Long> longList = new ArrayList<>();
                            for (String str : stringArray) {
                                longList.add(Long.parseLong(str));
                            }

                            if (longList.contains((long) 0))
                                ;
                            else if (!longList.contains(service_id))
                                continue;
                            //if (Integer.parseInt(workingMail.mail.getService_id())!=0 && Integer.parseInt(workingMail.mail.getService_id()) != service_id)
                            //    continue;

                            ACSHelper.do_acs_command(ACSHelper.MSG_NEW_MAIL, workingMail.mail, i, workingMail.mail.getFlag());
                            break;
                        }
                    }
                }
            }
        }
        unlock_task();
    }

    public void check_working_ticker_to_delete() {
        lock_task();
        if(g_workingTickerArrayList != null) {
            for(int i=0; i<g_workingTickerArrayList.size(); i++) {
                Date now = Calendar.getInstance().getTime();
                long now_time = now.getTime();
                long start_time = get_timestamp_no_second(g_workingTickerArrayList.get(i).ticker.getStart_time());
                long end_time = get_timestamp_no_second(g_workingTickerArrayList.get(i).ticker.getEnd_time());
//                Log.d(TAG,"ticker end time : "+g_workingTickerArrayList.get(i).ticker.getEnd_time()+" long value : "+end_time);
//                Log.d(TAG,"ticker now time : "+now+" long value : "+now_time);
                if(now_time > end_time) {//over exist time, delete from working mail
                    delete_maildata_of_db(get(),g_workingTickerArrayList.get(i).mailData);
                    g_workingTickerArrayList.remove(i);
                    i--;
                }
            }
        }
        unlock_task();
    }

    public void check_working_ticker_to_do() {
        lock_task();
//        Log.d(TAG,"check working ticker to do g_workingTickerArrayList.size() = "+g_workingTickerArrayList.size());
        if(g_workingTickerArrayList != null) {
            for(int i=0; i<g_workingTickerArrayList.size(); i++) {
                WorkingTicker workingTicker = g_workingTickerArrayList.get(i);
                Date now = Calendar.getInstance().getTime();
                long now_time = now.getTime();
                long start_time = get_timestamp_no_second(workingTicker.ticker.getStart_time());
                long end_time = get_timestamp_no_second(workingTicker.ticker.getEnd_time());
                ProgramInfo p = ChannelChangeManager.get_instance(get()).get_cur_channel();
                if(p == null){
                    LogUtils.e("Can't find Programeinfo");
                    break;
                }
                int service_id = p.getServiceId();
//                Log.d(TAG,"ticker end time : "+workingTicker.ticker.getEnd_time()+" long value : "+end_time);
//                Log.d(TAG,"ticker now time : "+now+" long value : "+now_time);
//                Log.d(TAG,"ticker now service id : "+service_id);
                if(now_time > end_time) {//not do envelope
                    g_workingTickerArrayList.remove(i);
                    i--;
                }else if(now_time < start_time){
                    LogUtils.d("Don't reach the start time , skip it");
                }else {
                    Date nextshowtime = new Date(workingTicker.ticker.getNext_show_time());
//                    Log.d(TAG,"ticker["+i+"] next_show_time : "+nextshowtime+" long value : "+workingTicker.ticker.getNext_show_time());
                    if (workingTicker.ticker.getEnabled() == 1 && workingTicker.ticker.checkNeedDisplay() && workingTicker.ticker.getNext_show_time() != 0
                        && now_time >= workingTicker.ticker.getNext_show_time()) {
                        if(!workingTicker.ticker.getMail_type().equals(Mail.MAIL_TYPE_EMERGENCY) && !ACSHelper.showTicker(get()))
                            continue;

//                        Log.d(TAG, "check_working_ticker_to_do: service id = " + workingTicker.ticker.getService_id());
                        String[] stringArray = workingTicker.ticker.getService_id().split(":");
                        List<Integer> longList = new ArrayList<>();
                        for (String str : stringArray) {
                            longList.add(Integer.parseInt(str));
                        }

                        if (longList.contains(0))
                            ;
                        else if(!longList.contains(service_id))
                            continue;
                        //if (Integer.parseInt(workingTicker.ticker.getService_id())!=0 && Integer.parseInt(workingTicker.ticker.getService_id()) != service_id)
                        //    continue;
                        ACSHelper.do_acs_command(ACSHelper.MSG_NEW_TICKER, workingTicker.ticker, MarqueeManager.MARQUEE_START, service_id);
                        break;
                    }
                }
            }
        }
        unlock_task();
    }

    public void list_sort_by_mail_weight(int mail_type) {
        if(mail_type == TYPE_MAIL) {
            Collections.sort(g_workingMailArrayList, new Comparator<WorkingMail>() {
                @Override
                public int compare(WorkingMail workingMail, WorkingMail t1) {
                    return (workingMail.mail.getWeight() - t1.mail.getWeight());
                }
            });
        }
        if(mail_type == TYPE_TICKER) {
            Collections.sort(g_workingTickerArrayList, new Comparator<WorkingTicker>() {
                @Override
                public int compare(WorkingTicker workingTicker, WorkingTicker t1) {
                    return (workingTicker.ticker.getWeight() - t1.ticker.getWeight());
                }
            });
        }
    }

    public void set_working_mail_already_shown(Context context,int arrayIndex) {
        if(g_workingMailArrayList.size() > arrayIndex) {
            g_workingMailArrayList.get(arrayIndex).mailData.setAlready_shown(1);
            save_maildata_to_db(context,g_workingMailArrayList.get(arrayIndex).mailData);
        }
    }

    public void set_mail_read_status(Context context, Mail mail) { //if user read mail, should save to db
        if(mail.getMail_display_type().equals("mail")) {
            for(int i = 0;i < g_workingMailArrayList.size(); i++) {
                Mail tmp = g_workingMailArrayList.get(i).mail;
                if(tmp.getId() == mail.getId()) {
                    g_workingMailArrayList.get(i).mail.setRead_status(Mail.READ);
                    save_mail_read_flag_to_db(context,g_workingMailArrayList.get(i).mail);
                    break;
                }
            }
        }
        if(mail.getMail_display_type().equals("ticker")) {

        }
    }

    public void set_mail_read_status_from_mail_activity(Context context, Mail mail) {
        if(mail.getMail_display_type().equals("mail")) {
            save_mail_read_flag_to_db(context, mail);

            for(int i = 0;i < g_workingMailArrayList.size(); i++) {
                Mail tmp = g_workingMailArrayList.get(i).mail;
                if(tmp.getId() == mail.getId()) {
                    g_workingMailArrayList.get(i).mail.setRead_status(Mail.READ);
                    break;
                }
            }
        }
    }

    public void open_mail(Mail mail,MailDetail mailDetail) {
        Log.d(TAG,"do EVENT_OPEN_MAIL");
        MailDialog mailDialog = new MailDialog(get());
        mailDialog.set_content(mail);
        mailDialog.show();
    }

    public void open_app(Mail mail,MailDetail mailDetail) {
        Log.d(TAG,"do EVENT_OPEN_APP, APP_CP, OR APP_MULTI");
        ActivityUtils.start_activity(get(), mailDetail.getEvent_value(), "null");
    }

    public void open_app_page(Mail mail,MailDetail mailDetail) {
        Log.d(TAG,"do EVENT_OPEN_APP_PAGE");
    }

    public void open_browser(Mail mail,MailDetail mailDetail) {
        Log.d(TAG,"do EVENT_OPEN_BROWSER");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.prime.webbrowser", "com.prime.webbrowser.MainActivity"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("WEB_URL_KEY", mailDetail.getUrl());
        intent.putExtra("PERMIT", true);
        get().startActivity(intent);
    }

    public void open_youtube(Mail mail,MailDetail mailDetail) {
        Log.d(TAG,"do EVENT_OPEN_YOUTUBE");
        String url = mailDetail.getEvent_value();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + url));
        get().startActivity(intent);
    }

    public void force_tune(Mail mail) {
        Log.d(TAG,"do EVENT_FORCE_TUNE");
        int service_id = mail.get_mail_force_tune().getSrid();
        int ts_id = mail.get_mail_force_tune().getTsid();
        ACSHelper.do_acs_command(ACSHelper.MSG_FORCE_TUNE, null, service_id, ts_id);
    }

    public void force_tune(MailDetail mailDetail) {
        Log.d(TAG,"do EVENT_FORCE_TUNE");
        int channelNum = Integer.parseInt(mailDetail.getEvent_value());
        LogUtils.d("channelNum "+channelNum);
        //ProgramInfo channelInfo = get().g_dtv.get_program_by_service_id(channelNum, 0);
        //LogUtils.d("channelInfo = "+channelInfo);

        ACSHelper.do_acs_command(ACSHelper.MSG_FORCE_TUNE, null, channelNum, 0);
    }

    public void open_stream(Mail mail,MailDetail mailDetail) {
        Log.d(TAG,"do EVENT_OPEN_Stream");
        Intent intent = new Intent(get(), StreamActivity.class);
        intent.putExtra(StreamActivity.EXTRA_STREAM_URL, mailDetail.getUrl());
        get().startActivity(intent);
    }

    public void open_qrcode(Mail mail,MailDetail mailDetail) {
        Log.d(TAG,"do EVENT_OPEN_QRCODE");
        QRCodeDialog qrCodeDialog = new QRCodeDialog(get(), mailDetail);
        qrCodeDialog.show();
    }

    public void open_poster(Mail mail,MailDetail mailDetail) {
        Log.d(TAG,"do EVENT_OPEN_MAIL_POSTER");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.prime.webbrowser", "com.prime.webbrowser.MainActivity"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("WEB_URL_KEY", mailDetail.getUrl());
        intent.putExtra("PERMIT", true);
        get().startActivity(intent);
    }

    public void open_program_detail(Mail mail,MailDetail mailDetail) {
        Log.d(TAG,"do EVENT_OPEN_PROGRAM_DETAIL");

        AdPageItem adPageItem = new AdPageItem();
        adPageItem.set_channel_num(mailDetail.getChannel_num());
        adPageItem.set_channel_name(mailDetail.getChannel_name());
        adPageItem.set_title(mailDetail.getTitle());
        adPageItem.set_start_time(mailDetail.getStart_time());
        adPageItem.set_end_time(mailDetail.getEnd_time());
        adPageItem.set_description(mailDetail.getDescription());

        HotkeyInfo hotkeyInfo = new HotkeyInfo(get(), adPageItem);
        hotkeyInfo.show(HotkeyInfo.Detail.RECOMMEND, null);
    }

    public void start_mail_dialog_event(Mail mail,MailDetail mailDetail) {

        switch(mailDetail.getEvent_type()) {
            case Mail.EVENT_OPEN_MAIL: {
                open_mail(mail,mailDetail);
            }break;
            case Mail.EVENT_OPEN_APP:
            case Mail.EVENT_OPEN_APP_CP:
            case Mail.EVENT_OPEN_APP_CP_MULTI: {
                open_app(mail,mailDetail);
            }break;
            case Mail.EVENT_OPEN_YOUTUBE: {
                open_youtube(mail,mailDetail);
            }break;
            case Mail.EVENT_FORCE_TUNE: {
                force_tune(mailDetail);
            }break;
            case Mail.EVENT_OPEN_POSTER: {
                open_poster(mail,mailDetail);
            }break;
            case Mail.EVENT_OPEN_BROWSER: {
                open_browser(mail,mailDetail);
            }break;
            case Mail.EVENT_OPEN_STREAM : {
                open_stream(mail,mailDetail);
            }break;
            case Mail.EVENT_OPEN_QRCODE: {
                open_qrcode(mail,mailDetail);
            }break;
            case Mail.EVENT_OPEN_PROGRAM_DETAIL: {
                open_program_detail(mail,mailDetail);
            }break;

        }
    }

    public void set_ticker_next_show_time(int id) {
        lock_task();
        Log.d(TAG,"ticker id["+id+"] set_ticker_next_show_time");
        for(int i = 0; i < g_workingTickerArrayList.size(); i++) {
            if(g_workingTickerArrayList.get(i).ticker.getId() == id) {
//                Log.d(TAG,"id["+id+"] find to update");
                g_workingTickerArrayList.get(i).ticker.setNext_show_time();
                break;
            }
        }
        unlock_task();
    }

    public void stop_handler() {
        g_handler_working_ticker.removeCallbacks(working_ticker_check_runnable);
        g_handler_working_mail.removeCallbacks(working_mail_check_runnable);
    }

    public void stop_running_ticker() {
        ACSHelper.do_acs_command(ACSHelper.MSG_NEW_TICKER, null, MarqueeManager.MARQUEE_STOP, 0);
    }

    public boolean checkMailGenre(Mail mail) {
        GposInfo gposInfo = gpos_info_get();
        boolean isGenre = false;
        if(mail.getMail_category() == Mail.MAIL_CATEGORY_SHOPPING && gposInfo.getMailSettingsShopping() == 1) {
            isGenre = true;
            Log.d(TAG,"mail category = MAIL_CATEGORY_SHOPPING gposInfo.getMailSettingsShopping() = 1");
        }
        else if(mail.getMail_category() == Mail.MAIL_CATEGORY_NEWS && gposInfo.getMailSettingsNews() == 1) {
            isGenre = true;
            Log.d(TAG,"mail category = MAIL_CATEGORY_NEWS gposInfo.getMailSettingsNews() = 1");
        }
        else if(mail.getMail_category() == Mail.MAIL_CATEGORY_POPULAR && gposInfo.getMailSettingsPopular() == 1) {
            isGenre = true;
            Log.d(TAG,"mail category = MAIL_CATEGORY_POPULAR gposInfo.getMailSettingsPopular() = 1");
        }
        else if(mail.getMail_category() == Mail.MAIL_CATEGORY_COUPON && gposInfo.getMailSettingsCoupon() == 1) {
            isGenre = true;
            Log.d(TAG,"mail category = MAIL_CATEGORY_COUPON gposInfo.getMailSettingsCoupon() = 1");
        }
        else if(mail.getMail_category() == Mail.MAIL_CATEGORY_SERVICE && gposInfo.getMailSettingsService() == 1) {
            isGenre = true;
            Log.d(TAG,"mail category = MAIL_CATEGORY_SERVICE gposInfo.getMailSettingsService() = 1");
        }

        return isGenre;
    }
}
