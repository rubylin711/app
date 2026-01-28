package com.prime.launcher.Home.BlockChannel;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.launcher.HomeActivity;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.launcher.ChannelChangeManager;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/** @noinspection CommentedOutCode*/
public class BlockedChannel {
    static String TAG = BlockedChannel.class.getSimpleName();

    public static final boolean LOCK  =  true;
    public static final boolean UNLOCK  =  false;

//    public static final int LOCK_STATUS_CHANNEL  =  1;
//    public static final int LOCK_STATUS_TIME  =  2;
//    public static final int LOCK_STATUS_PARENTAL  =  3;
//    public static final int LOCK_STATUS_ADULT  =  4;
    public static final int PARENTAL_RATE_NONE  =  0xff;
    public static final int PARENTAL_RATE_0  =  0;
    public static final int PARENTAL_RATE_6  =  1;
    public static final int PARENTAL_RATE_12  =  2;
    public static final int PARENTAL_RATE_15  =  3;
    public static final int PARENTAL_RATE_18  =  4;
    private boolean pre_lock_status;
    private boolean lock_status_channel;
    private boolean lock_status_time;
    private boolean lock_status_parental;
    private boolean lock_status_adult;
    private ProgramInfo g_cur_program,g_pre_program;
    private int cur_rate;
    private int acc_rate;
    private int cur_gpos_rate; //save current gpos parental rate, for checking gpos parental rate change
    private PrimeDtv g_dtv;
    private boolean timelock_enter_pincode;
    private boolean adultlock_enter_pincode;
    private boolean channellock_enter_pincode;
    private boolean check_tuner_lock_flag = false;

    private static BlockedChannel g_blocked_channel;
    private Callback g_callback;
    private Handler g_Handler;
    private long pre_tuner_check_time = 0;
    private final Runnable BlockChannelCheckRunnable = new Runnable() {
        @Override
        public void run() {
//            Log.d(TAG,"BlockChannelCheckRunnable check g_blocked_channel = "+g_blocked_channel+" start");
            if(g_blocked_channel != null) {
                ProgramInfo channel = g_cur_program;
                if(channel != null) {
//                    Log.d(TAG,"adultlock_enter_pincode["+adultlock_enter_pincode+"]");
                    boolean isLock = g_blocked_channel.checkLockForRunnable(channel);
//                    Log.d(TAG,"isLock = "+isLock+" pre_lock_status = "+pre_lock_status);
                    if(pre_lock_status != isLock) {
                        pre_lock_status = isLock;
                        g_callback.on_isLock_changed(pre_lock_status);
                    }

                    if (check_tuner_lock_flag) {
                        long check_tuner_now = System.currentTimeMillis();
                        if(pre_tuner_check_time == 0 || (check_tuner_now - pre_tuner_check_time >= 3000)) { //3 sec check tuner
                            g_callback.on_tuner_lock(g_cur_program);
                            pre_tuner_check_time = check_tuner_now;
                        }
                    }
                }
            }
//            if(g_cur_program != null)
//                Log.d("on_isLock_changed","g_cur_program = "+g_cur_program.getDisplayName());
//            Log.d("on_isLock_changed","BlockChannelCheckRunnable check end");
            g_Handler.postDelayed(this, 1000);
        }
    };

    public interface Callback {
        void on_isLock_changed(boolean isLock);
        void on_tuner_lock(ProgramInfo channel);
    }

    public void register_callback(Callback callback) {
        g_callback = callback;
    }

    private BlockedChannel(ProgramInfo cur_program, Handler threadHandler) {
        g_dtv = HomeApplication.get_prime_dtv();
        lock_status_channel = lock_status_time = lock_status_parental = lock_status_adult = false;
        g_cur_program = null;
        timelock_enter_pincode = adultlock_enter_pincode = channellock_enter_pincode = false;
        if(cur_program != null)
            g_cur_program = cur_program;
        if (null == g_Handler && threadHandler != null)
            g_Handler = threadHandler;
    }

    public static BlockedChannel get_instance(ProgramInfo cur_program) {
        return get_instance(cur_program, null);
    }

    public static BlockedChannel get_instance(ProgramInfo cur_program, Handler threadHandler) {
        if(g_blocked_channel == null || threadHandler != null) {
            LogUtils.d("create BlockedChannel class !!!");
            g_blocked_channel = new BlockedChannel(cur_program, threadHandler);
        }
        return g_blocked_channel;
    }

    public static boolean is_blocked() {
        return g_blocked_channel != null && g_blocked_channel.isLock();
    }

    public boolean isLock() {
//        LogUtils.d("lock_status_channel["+lock_status_channel+"] "+
//                "lock_status_time["+lock_status_time+"] "+
//                "lock_status_parental["+lock_status_parental+"] "+
//                "lock_status_adult["+lock_status_adult+"] ");
        return (lock_status_channel | lock_status_time | lock_status_parental | lock_status_adult);
    }

    public boolean isChannelLock() {
        return lock_status_channel;
    }

    public boolean isTimeLock() {
        return lock_status_time;
    }

    public boolean isParentalLock() {
        return lock_status_parental;
    }

    public boolean isAdultLock() {
        return lock_status_adult;
    }

    public void setChannelLock(boolean value) {
        lock_status_channel = value;
    }

    public void setTimeLock(boolean value) {
        lock_status_time = value;
    }

    public void setParentalLock(boolean value) {
        lock_status_parental = value;
    }

    public void setAdultLock(boolean value) {
        lock_status_adult = value;
    }

    private void reset_enter_pincode_status_parental() {
        if (!lock_status_parental) {
            if (cur_rate == 0xff)
                acc_rate = 0;
            else
                acc_rate = cur_rate;
        }
    }

    private void reset_enter_pincode_status_runnable() {
        if(!checkTimeLockWithoutEnterPincode())
            timelock_enter_pincode = false;
    }

    private void reset_enter_pincode_status() {
        reset_enter_pincode_status_runnable();
        if(g_cur_program.getLock() == 0)
            channellock_enter_pincode = false;
        if(g_cur_program.getAdultFlag() == 0)
            adultlock_enter_pincode = false;
        reset_enter_pincode_status_parental();
    }

    public void reset_pin_code_status() {
        Log.d(TAG, "reset_pin_code_status: ");
        channellock_enter_pincode = false;
        timelock_enter_pincode = false;
        adultlock_enter_pincode = false;
        acc_rate = 0;
    }
    
    public boolean checkLock(ProgramInfo programInfo) {
        boolean first_check = false;
//        if(programInfo != null) {
//            LogUtils.d("change_channel_stop checkLock new program["+programInfo.getDisplayName()+"]");
//        }
//        if(g_cur_program != null)
//            LogUtils.d("change_channel_stop checkLock cur program[+"+g_cur_program.getDisplayName()+"]");
//        if(g_pre_program != null)
//            LogUtils.d("change_channel_stop checkLock pre program[+"+g_pre_program.getDisplayName()+"]");
        if(g_cur_program == null || (g_cur_program.getChannelId() != programInfo.getChannelId())) {
            g_pre_program = g_cur_program;
            first_check = true;
        }
        g_cur_program = programInfo;
        cur_rate = getParentalRate(g_cur_program);
        //Log.e(TAG,"cur_rate = "+cur_rate+" acc_rate = "+acc_rate);
        if(g_cur_program != null) {
            lock_status_channel = checkChannelLock();
            lock_status_time = checkTimeLock();
            lock_status_parental = checkParentalLock();
            lock_status_adult = checkAdultLock();
            //Log.d(TAG,"lock_status_channel["+lock_status_channel+"] "+"lock_status_time["+lock_status_time+"]");
            //Log.d(TAG,"lock_status_parental["+lock_status_parental+"] "+"lock_status_adult["+lock_status_adult+"]");
        }
        reset_enter_pincode_status();
        if(first_check)
            pre_lock_status = isLock();
        return isLock();
    }

    private boolean checkLockForRunnable(ProgramInfo programInfo) {
        boolean first_check = false;
        if(g_cur_program == null || (g_cur_program.getChannelId() != programInfo.getChannelId())) {
            g_pre_program = g_cur_program;
            first_check = true;
        }
        g_cur_program = programInfo;
        if(g_cur_program != null) {
            lock_status_time = checkTimeLock();
            //Log.d(TAG,"lock_status_channel["+lock_status_channel+"] "+"lock_status_time["+lock_status_time+"]");
            //Log.d(TAG,"lock_status_adult["+lock_status_adult+"]");
        }
        reset_enter_pincode_status_runnable();
        if(first_check)
            pre_lock_status = isLock();
        return isLock();
    }

    private boolean checkTunerLock(int tunerId) {
        return g_dtv.get_tuner_status(tunerId);
    }

    private boolean checkChannelLock() {
        //Log.d(TAG,"g_cur_program.getLock()["+g_cur_program.getLock()+"] " +"channellock_enter_pincode["+channellock_enter_pincode+"]");
        //Log.e(TAG, "checkChannelLock: [get lock] " + g_cur_program.getLock());
        //Log.e(TAG, "checkChannelLock: [enter pin code] " + channellock_enter_pincode);
        if(g_cur_program.getLock() == 1 && !channellock_enter_pincode)
            return true;
        else
            return false;
    }

    private boolean checkTimeLockWithoutEnterPincode() {
        boolean islock = false;
//        Log.d(TAG,"g_cur_program.getTimeLockFlag()["+g_cur_program.getTimeLockFlag(3)+"] " +"timelock_enter_pincode["+timelock_enter_pincode+"]");
        
        GposInfo gposInfo = g_dtv.gpos_info_get();
//            Log.d(TAG,"gposInfo.getTimeLockAllDay()["+gposInfo.getTimeLockAllDay()+"]");
        if(gposInfo.getTimeLockAllDay() == 1) {
            islock = true;
        }
        else {
            Date date = new Date();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HHmm");
            String result = timeFormat.format(date.getTime());
            int now_time = Integer.parseInt(result);
//                Log.d(TAG,"now_time = " + now_time);
            for(int i = 0; i < 3; i ++) {
                if(g_cur_program.getTimeLockFlag(i) == 1) {
                    int start_time = gposInfo.getTimeLockPeriodStart(i);
                    int end_time = gposInfo.getTimeLockPeriodEnd(i);
//                    Log.d(TAG,"timelock period time["+i+"] = " + now_time);
                    if(start_time <= now_time && now_time < end_time) {
                        islock = true;
                        break;
                    }
                }
            }
        }
//        Log.d(TAG,"checkTimeLockWithoutEnterPincode islock = " + islock);
        return islock;
    }

    private boolean checkTimeLock() {
//        Log.d(TAG,"timelock_enter_pincode = " + timelock_enter_pincode);
        return (checkTimeLockWithoutEnterPincode() & !timelock_enter_pincode);
    }

    private int get_correct_parental_rate(int parental_rate){
        int rate = PARENTAL_RATE_NONE;
        if(parental_rate != PARENTAL_RATE_NONE) {
            if ( parental_rate >= 18) // 18+
                rate = PARENTAL_RATE_18;
            else if ( parental_rate >= 15 ) // 15+ //12
                rate = PARENTAL_RATE_15;
            else if ( parental_rate >= 12 ) // 12+ //10-11
                rate = PARENTAL_RATE_12;
          else if ( parental_rate >= 7) // 6+ //7-9
                rate = PARENTAL_RATE_6;
            else if ( parental_rate >= 0) // 3+ //4-6
                rate = PARENTAL_RATE_0;
        }
//        Log.d(TAG,"get_correct_parental_rate parental_rate = "+parental_rate+", rate = " + rate);
        return rate;
    }

    private int get_correct_parental_rate_dmg(int parental_rate) {
        int rate = PARENTAL_RATE_NONE;
        if(parental_rate != PARENTAL_RATE_NONE) {
            if ( parental_rate >= 18) // 18+
                rate = PARENTAL_RATE_18;
            else if ( parental_rate >= 15 ) // 15+ //12
                rate = PARENTAL_RATE_15;
            else if ( parental_rate >= 12 ) // 12+ //10-11
                rate = PARENTAL_RATE_12;
            //eric lin 20240222 dmg parental rating 6
            else if ( parental_rate >= 6) // 6+ //6-9
                rate = PARENTAL_RATE_6;
            else if (parental_rate >= 0) // 3+ //4-6
                rate = PARENTAL_RATE_0;
        }
//        Log.d(TAG,"get_correct_parental_rate_dmg parental_rate = "+parental_rate+", rate = " + rate);
        return rate;
    }

    public boolean parental_lock_flow(int cur_rating,int parental_rating_of_setting) {
        boolean lock = UNLOCK;
        int rate = 0;
        GposInfo gposInfo = g_dtv.gpos_info_get();

        if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)) {
            rate = get_correct_parental_rate_dmg(cur_rating);
        } else {
            rate = get_correct_parental_rate(cur_rating);
        }

        if(gposInfo.getParentalLockOnOff() == 0 || parental_rating_of_setting == GposInfo.PARENTALRATE_DISABLE)
            return lock;
        if (rate == PARENTAL_RATE_18) {// 18+
            if(parental_rating_of_setting <= 18)
                lock = LOCK;
        }
        else if (rate == PARENTAL_RATE_15 ) {// 15+ //12
            if(parental_rating_of_setting <= 15)
                lock = LOCK;
        }
        else if (rate == PARENTAL_RATE_12) {// 12+ //10-11
            if(parental_rating_of_setting <= 12)
                lock = LOCK;
        }
        else if (rate == PARENTAL_RATE_6) {// 6+ //7-9
            if(parental_rating_of_setting <= 6)
                lock = LOCK;
        }
//        Log.d(TAG,"lock = "+lock+" rate = "+rate+" parental_rating_of_setting = "+parental_rating_of_setting);
        return lock;
    }

    private int getParentalRate(ProgramInfo programInfo) {
        // we should get latest present epg from service because parental rate may change
        EPGEvent epgEvent = g_dtv.get_present_event(programInfo.getChannelId());
        if (epgEvent == null) {
            if (programInfo.getAdultFlag() == 1)
                return 18;
            return 0xff;
        }
        else {
            if (programInfo.getAdultFlag() == 1)
                epgEvent.set_parental_rate(18);
            return epgEvent.get_parental_rate();
        }
    }

    private boolean checkParentalLock() {
        check_cur_gpos_rate_change(); // check gpos rate change to reset acc_rate(accept_rate) if necessary
        GposInfo gposInfo = g_dtv.gpos_info_get();
        //EPGEvent epgEvent = g_dtv.get_present_event(g_cur_program.getChannelId());
        int global_parental_rate = gposInfo.getParentalRate();
        //int program_parental_rate = getParentalRate(g_cur_program);
        //Log.d(TAG,"global_parental_rate["+global_parental_rate+"] "+"program_parental_rate["+program_parental_rate+"]");
        if(cur_rate != 0xff) {
            boolean lock = parental_lock_flow(cur_rate, global_parental_rate);
            //Log.d(TAG,"acc_rate["+acc_rate+"] "+"cur_rate["+cur_rate+"]");
            //Log.e(TAG, "checkParentalLock: lock = " + lock + ", acc_rate = " + acc_rate + ", result = " + (acc_rate < cur_rate));
            return lock && acc_rate < cur_rate;
        }
        return false;
    }

    private boolean checkAdultLock() {
        int adult = g_cur_program.getAdultFlag();
//        Log.d(TAG,"g_cur_program.getAdultFlag()["+adult+"] "+"adultlock_enter_pincode["+adultlock_enter_pincode+"]");
//        if(g_pre_program != null)
//            Log.d(TAG,"g_pre_program.getAdultFlag()["+g_pre_program.getAdultFlag()+"] ");
        if(adult == 1 && g_pre_program != null && g_pre_program.getAdultFlag() == 0 && !adultlock_enter_pincode)
            return true;
        if(adult == 1 && !adultlock_enter_pincode)
            return true;
        return false;
    }


    public void block_channel() {

    }

    public void unblock_channel() {
        if(lock_status_channel)
            channellock_enter_pincode = true;
        if(lock_status_time)
            timelock_enter_pincode = true;
        if(lock_status_adult)
            adultlock_enter_pincode = true;
        if(lock_status_parental) {
            acc_rate = cur_rate;
        }

        lock_status_channel = lock_status_time = lock_status_parental = lock_status_adult = false;
        pre_lock_status = false;
//        Log.d(TAG,"adultlock_enter_pincode["+adultlock_enter_pincode+"]");
    }

    public ProgramInfo get_cur_channel() {
        return g_cur_program;
    }

    public void stop_blocked_channel_check() {
        //Log.e(TAG,"stop_blocked_channel_check");
        if (g_Handler != null)
            g_Handler.removeCallbacks(BlockChannelCheckRunnable);
        pre_lock_status = false;
    }

    public void start_blocked_channel_check() {
        //Log.e(TAG,"start_blocked_channel_check");
        if (g_Handler != null)
            g_Handler.post(BlockChannelCheckRunnable);
    }

    public void set_cur_gpos_rate(int rate) {
        cur_gpos_rate = rate;
    }

    public void check_cur_gpos_rate_change() {
        GposInfo gposInfo = g_dtv.gpos_info_get();
        if(gposInfo.getParentalRate() != cur_gpos_rate) {
            Log.d(TAG,String.format("gpos parental rate change, old[%d] new[%d] , acc_rate should be reset",cur_gpos_rate,gposInfo.getParentalRate()));
            cur_gpos_rate = gposInfo.getParentalRate();
            acc_rate = 0;
            Log.d(TAG,"gpos parental rate change, acc_rate should be reset");
        }
    }

    public static void set_hint_visibility(AppCompatActivity activity, boolean showHint) {
        if (!(activity instanceof HomeActivity))
            return;

        HomeActivity homeActivity = (HomeActivity) activity;
        View bgBlockedChannel = homeActivity.findViewById(R.id.lo_home_live_tv_blocked_channel_bg);
        bgBlockedChannel.setBackgroundResource(showHint ? R.drawable.bg_blocked_channel_hint : R.drawable.bg_blocked_channel);
    }

    public void set_check_tuner_lock_flag(boolean flag) {
        LogUtils.d(" flag = "+flag);
        check_tuner_lock_flag = flag;
    }

    public void update_parental_lock() {
        cur_rate = getParentalRate(g_cur_program);
//        Log.d(TAG, "update_parental_lock: cur_rate = " + cur_rate);
        lock_status_parental = checkParentalLock();
//        Log.d(TAG, "update_parental_lock: lock_status_parental = " + lock_status_parental);
//        Log.d(TAG, "update_parental_lock: pre_lock_status = " + pre_lock_status);
        boolean blocked = isLock();
//        Log.d(TAG, "update_parental_lock: blocked = " + blocked);
        if(pre_lock_status != blocked) {
            pre_lock_status = blocked;
            g_callback.on_isLock_changed(pre_lock_status);
        }

        reset_enter_pincode_status_parental();
    }
}
