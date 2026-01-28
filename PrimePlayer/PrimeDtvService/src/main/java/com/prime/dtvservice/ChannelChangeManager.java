package com.prime.dtvservice;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.datastructure.ServiceDefine.AvCmdMiddle;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.PvrInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.datastructure.sysdata.PvrRecStartParam;
import com.prime.datastructure.sysdata.SeriesInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.RecChannelInfo;
import com.prime.datastructure.utils.TVMessage;
import com.prime.datastructure.utils.TVTunerParams;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.utils.UsbUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** @noinspection CommentedOutCode, PointlessBooleanExpression */
public class ChannelChangeManager {
    private static final String TAG = "ChannelChangeManager[PrimeDtvService]";

    private static ChannelChangeManager g_channel_manager;
    private long g_cur_ch_id = 0; // current play ch id
    private long g_pre_ch_id = 0;
    private long g_next_ch_id = 0;
    public long g_back_ch_id = 0; // previous play ch id
    private boolean g_force_update_back_ch_id = false;
    private long gLastRecChId = 0;
    private long gRecordStopChId = 0;
    private List<Long> g_all_ch_id_list; // list of all tv and radio ch id
    private Map<Long, Integer> g_fcc_tuner_mapping; // fcc, map ch id to tuner id
    private List<Long> g_fcc_ch_id_list = new ArrayList<>();;
    private List<Integer> g_fcc_tuner_id_list = new ArrayList<>();;
    //private Map<Long, Integer> g_fcc_av_info = new LinkedHashMap<>();
    private RecChannelInfo[] g_RecChannels = new RecChannelInfo[Pvcfg.NUM_OF_RECORDING];
    private HashMap <Long, Integer> gRecIdMap = new HashMap<>();
    private HashMap <Long, Integer> gRecIndexMap = new HashMap<>();
    private final HashMap <Long, Integer> gRecSingleMap = new HashMap<>();
    private final HashMap <Long, Integer> gRecSeriesMap = new HashMap<>();
    private int g_rec_num = 0;
    private int AVTunerID = -1;
    private int mPlaybackTunerId = -1;
    private int mTimeshiftRecordTunerId = -1;
    private boolean isRecording = false;
    private boolean isTimeshiftStart = false;
    private boolean isTimeshiftPlaybackPause = false;
    private boolean is_timeshift_callback_started = false;
    private boolean isFilePlayback = false;
    private boolean isTimeshfitPlaybackPause = false;
    private Integer g_miniEPG_channel_index = null;
    private PrimeDtv g_dtv;
    private static Context mContext;

    private boolean isFirtPlay = true;
    public static boolean isSetVisibleCompleted = false;

    private ChannelChangeManager(Context context) {
        g_dtv = PrimeDtvServiceApplication.get_prime_dtv();
        g_all_ch_id_list = new ArrayList<>();
        g_fcc_tuner_mapping = new HashMap<>();

        update_all_channel();
        mContext = context;
        g_dtv.setSetVisibleCompleted(false);
    }

    public static ChannelChangeManager get_instance(Context context) {
        if(g_channel_manager == null) {
            g_channel_manager = new ChannelChangeManager(context);
        }

        return g_channel_manager;
    }

    public void set_context(Context context){
        mContext = context;
    }
    public Context get_context() {
        return mContext;
    }

    public PrimeDtv get_prime_dtv() {
        return g_dtv;
    }

    public long get_cur_ch_id() {
        if (g_cur_ch_id == 0) {
            ProgramInfo p = get_default_channel();
            if( p != null)
                g_cur_ch_id = p.getChannelId();
        }
        return g_cur_ch_id;
    }

    public void set_cur_ch_id(long channelId) {
        LogUtils.d(" channelId "+ channelId);
        g_cur_ch_id = channelId;
    }

    public ProgramInfo get_cur_channel() {
        ProgramInfo programInfo = g_dtv.get_program_by_channel_id(g_cur_ch_id);
        if (null == programInfo)
            programInfo = get_default_channel();
        return programInfo;
    }

    public long get_pre_ch_id() {
        return g_pre_ch_id;
    }

    public long get_mini_epg_cur_id() {

        if (g_all_ch_id_list.isEmpty())
            return 0;

        if (g_miniEPG_channel_index == null ||
            g_miniEPG_channel_index < 0 ||
            g_miniEPG_channel_index >= g_all_ch_id_list.size())
            g_miniEPG_channel_index = g_all_ch_id_list.indexOf(get_cur_ch_id());

        return g_all_ch_id_list.get(g_miniEPG_channel_index);
    }

    public long get_mini_epg_up_id() {

        if (g_all_ch_id_list.isEmpty())
            return 0;

        if (g_miniEPG_channel_index == null ||
            g_miniEPG_channel_index < 0 ||
            g_miniEPG_channel_index >= g_all_ch_id_list.size())
            g_miniEPG_channel_index = g_all_ch_id_list.indexOf(get_cur_ch_id());

        g_force_update_back_ch_id = true;
        g_back_ch_id = g_all_ch_id_list.get(g_miniEPG_channel_index);
        g_miniEPG_channel_index++;

        if(g_miniEPG_channel_index == g_all_ch_id_list.size())
            g_miniEPG_channel_index = 0;
        //noinspection ConstantValue
        if (false) {
            ProgramInfo back = g_dtv.get_program_by_channel_id(g_back_ch_id);
            Log.e(TAG, "get_mini_epg_up_id: [channel] " + back.getDisplayNum(3) + " " + back.getDisplayName());
        }

        return g_all_ch_id_list.get(g_miniEPG_channel_index);
    }

    public long get_mini_epg_down_id() {
        if (g_all_ch_id_list.isEmpty())
            return 0;

        if (g_miniEPG_channel_index == null ||
            g_miniEPG_channel_index < 0 ||
            g_miniEPG_channel_index >= g_all_ch_id_list.size())
            g_miniEPG_channel_index = g_all_ch_id_list.indexOf(get_cur_ch_id());

        g_force_update_back_ch_id = true;
        g_back_ch_id = g_all_ch_id_list.get(g_miniEPG_channel_index);
        g_miniEPG_channel_index--;

        if(g_miniEPG_channel_index == -1){
            g_miniEPG_channel_index = g_all_ch_id_list.size()-1;
        }
        //noinspection ConstantValue
        if (false) {
            ProgramInfo back = g_dtv.get_program_by_channel_id(g_back_ch_id);
            Log.e(TAG, "get_mini_epg_down_id: [channel] " + back.getDisplayNum(3) + " " + back.getDisplayName());
        }

        return g_all_ch_id_list.get(g_miniEPG_channel_index);
    }

    public long get_next_ch_up_id() {
        if (g_all_ch_id_list.isEmpty())
            return 0;

        int index = g_all_ch_id_list.indexOf(get_cur_ch_id()) + 1;
        if (index == g_all_ch_id_list.size())
            index = 0;

        return g_all_ch_id_list.get(index);
    }

    public long get_next_ch_down_id() {
        if (g_all_ch_id_list.isEmpty())
            return 0;

        int index = g_all_ch_id_list.indexOf(get_cur_ch_id()) - 1;
        if (index == -1)
            index = g_all_ch_id_list.size() - 1;

        return g_all_ch_id_list.get(index);
    }

    public void reset_mini_epg_index() {
        g_miniEPG_channel_index = null;
    }

    public void clean_subtitle(){
        Log.e(TAG, "clean_subtitle: need porting !!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    static public void setFCCVisible(int index) {
        LogUtils.d("FCC_LOG setFCCVisible index=" + index +
                " isSetVisibleCompleted=" + isSetVisibleCompleted);
        isSetVisibleCompleted = false;
        g_channel_manager.g_dtv.setSetVisibleCompleted(false);

        TVMessage msg = TVMessage.SetFCCVisible(index);
        PrimeDtvService.notifyMessage(msg);

//                        surface_3.setVisibility(View.INVISIBLE);
//                }break;
//                case 1: {
//                    if(surface_2 != null) {
//                        surface_2.setVisibility(View.VISIBLE);
//                        isSetVisibleCompleted = true;
//                        g_channel_manager.g_dtv.setSetVisibleCompleted(true);
//                    }
//                    if(surface_1 != null)
//                        surface_1.setVisibility(View.INVISIBLE);
//                    if(surface_3 != null)
//                        surface_3.setVisibility(View.INVISIBLE);
//
//                }break;
//                case 2: {
//                    if(surface_3 != null) {
//                        surface_3.setVisibility(View.VISIBLE);
//                        isSetVisibleCompleted = true;
//                        g_channel_manager.g_dtv.setSetVisibleCompleted(true);
//                    }
//                    if(surface_1 != null)
//                        surface_1.setVisibility(View.INVISIBLE);
//                    if(surface_2 != null)
//                        surface_2.setVisibility(View.INVISIBLE);
//                }break;
//            }
//        });
        LogUtils.d("FCC_LOG setFCCVisible wait UI surface ready index=" + index);
    }

    public static void WaitSetVisibleCompleted(){
        int count = 0;
        if(Pvcfg.isWaitSurfaceReady() == false)
            return;
        //LogUtils.d("[DB_Surface] isSetVisibleCompled = "+isSetVisibleCompled);
        while(!isSetVisibleCompleted){
            try {
                //LogUtils.d("[DB_Surface] isSetVisibleCompled = "+isSetVisibleCompled);
                Thread.sleep(50);
                count++;
                if(count >= 30) {//wait 1000ms
                    LogUtils.e("[DB_Surface] wait timeout!!!!");
                    break;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void change_channel_by_id(long target_ch_id) {
        LogUtils.d(" IN ");
        change_channel_by_id(target_ch_id, false);
    }

    public synchronized int change_channel_by_id(long target_ch_id, boolean force) {
        LogUtils.d("[FCC2] change_channel_by_id: target_ch_id = " + target_ch_id+" "+g_cur_ch_id+" force = "+force);
        LogUtils.d("[FCC2] before g_fcc_tuner_mapping = "+g_fcc_tuner_mapping);
        Log.d(TAG,"change_channel_by_id start : target_ch_id = " + target_ch_id+" force = "+force);
        ProgramInfo programInfo = g_dtv.get_program_by_channel_id(target_ch_id);
        int playStatus = g_dtv.av_control_get_play_status(0);
        int target_tuner_id, mode = 0;
        boolean do_fcc = false, channel_blocked = false;
        AVTunerID = programInfo.getTunerId();
        g_dtv.WaitAVPlayReady();
//        g_dtv.AvCmdMiddle_WaitAVPlayReady();
        // do not change channel if already playing
        if (target_ch_id == g_cur_ch_id
                && !force
                && (playStatus == AvCmdMiddle.PESI_SVR_AV_LIVEPLAY_STATE)) {
            Log.w(TAG, "change_channel_by_id: target_ch_id is already playing");
            return AVTunerID;
        }

        // change channel if channel exist
        if (!g_all_ch_id_list.contains(target_ch_id)) {
            Log.w(TAG, "change_channel_by_id: target_ch_id not in ch list");
            return AVTunerID;
        }

        g_dtv.StopDVBSubtitle();
        clean_subtitle();

        if(Pvcfg.isFccV2Enable()){
            int tid = (g_fcc_tuner_mapping.containsKey(g_cur_ch_id))?g_fcc_tuner_mapping.get(g_cur_ch_id):g_dtv.get_program_by_channel_id(target_ch_id).getTunerId();
            
            Log.d(TAG,"g_rec_num="+g_rec_num+" tid(tunerId)="+tid+" AVTunerID = "+AVTunerID);
            if((g_rec_num > 0) && (tid != AVTunerID))
                tid = AVTunerID;

            Log.d(TAG,"g_cur_ch_id="+g_cur_ch_id+" tid(tunerId)="+tid);
            //g_fcc_av_info.clear();
            g_fcc_ch_id_list.clear();
            g_fcc_tuner_id_list.clear();
            //g_fcc_av_info.put(g_cur_ch_id, tid);
            g_fcc_ch_id_list.add(g_cur_ch_id);
            g_fcc_tuner_id_list.add(tid);
            LogUtils.d("[FCC2] g_fcc_ch_id_list "+g_fcc_ch_id_list);
            LogUtils.d("[FCC2] g_fcc_tuner_id_list "+g_fcc_tuner_id_list);
        }else {
            if (!Pvcfg.isFccEnable() || g_fcc_tuner_mapping.containsKey(target_ch_id) || force) {
                change_channel_stop(Pvcfg.isFccEnable() ? 1 : 0, 1); // fcc1. stop
            } else {
                change_channel_stop_clear_fcc();
            }
        }
        do_fcc = update_cur_pre_ch_id_if_needed(target_ch_id);
        pvr_timeshift_stop();
        //target_tuner_id = g_dtv.get_program_by_channel_id(target_ch_id).getTunerId();
        if(g_fcc_tuner_mapping.containsKey(target_ch_id))
            target_tuner_id = g_fcc_tuner_mapping.get(target_ch_id);
        else
            target_tuner_id = g_dtv.get_program_by_channel_id(target_ch_id).getTunerId();

        if(g_rec_num > 0){
            if(target_tuner_id != AVTunerID)
                target_tuner_id = AVTunerID;
        }else{
            AVTunerID = target_tuner_id;
        }

//        BlockedChannel blockedChannel = BlockedChannel.get_instance(null,null);
        if(true/*blockedChannel != null && !blockedChannel.isLock()*/) {
            LogUtils.d("change_channel_by_id: target_ch_id = " + target_ch_id);
            if(isRecording && AVTunerID != -1){
                LogUtils.d("PVR recording is running. Use AVTunerID("+AVTunerID+") as tunerID force");
                programInfo.setTunerId(AVTunerID);
                g_dtv.update_program_info(programInfo);
            }
            if(Pvcfg.isFccV2Enable()){
                //g_fcc_av_info.put(target_ch_id, target_tuner_id);
                g_fcc_ch_id_list.add(target_ch_id);
                g_fcc_tuner_id_list.add(target_tuner_id);
                LogUtils.d("[FCC2] g_fcc_ch_id_list "+g_fcc_ch_id_list);
                LogUtils.d("[FCC2] g_fcc_tuner_id_list "+g_fcc_tuner_id_list);
            }else {
                g_dtv.av_control_play_by_channel_id(AVTunerID, target_ch_id, 0, 1); // fcc2. play
            }
            //g_dtv.startMonitorTable(target_ch_id, 0);
        }
//        else {
//            channel_blocked = true;
//            if(Pvcfg.isFccV2Enable()) {
//                //g_fcc_av_info.put(target_ch_id, target_tuner_id);
//                g_fcc_ch_id_list.add(target_ch_id);
//                g_fcc_tuner_id_list.add(target_tuner_id);
//                LogUtils.d("[FCC2] g_fcc_ch_id_list "+g_fcc_ch_id_list);
//                LogUtils.d("[FCC2] g_fcc_tuner_id_list "+g_fcc_tuner_id_list);
//            }
//            Log.d(TAG, "change_channel_by_id: blockedChannel null or locked, target_ch_id = " + target_ch_id);
//        }
        if ( do_fcc || force) {
            mode = do_fcc();
            int groupType = programInfo.getType() ==
                    ProgramInfo.PROGRAM_TV ? FavGroup.ALL_TV_TYPE : FavGroup.ALL_RADIO_TYPE;
            g_dtv.set_default_open_channel(target_ch_id, groupType);
        }
        else{
            if(Pvcfg.isFccV3Enable() == false)
                setFCCVisible(target_tuner_id);
        }

        Log.d(TAG,"AVTunerID="+AVTunerID+" target_ch_id="+target_ch_id);
        Log.d(TAG,"g_cur_ch_id="+g_cur_ch_id+" g_back_ch_id="+g_back_ch_id);
        Log.d(TAG,"g_pre_ch_id="+g_pre_ch_id+" g_next_ch_id="+g_next_ch_id);
        Log.d(TAG,"gLastRecChId="+gLastRecChId+" gRecordStopChId="+gRecordStopChId);
        Log.d(TAG,"g_fcc_tuner_mapping="+g_fcc_tuner_mapping);
        Log.d(TAG,"g_fcc_ch_id_list="+g_fcc_ch_id_list);
        Log.d(TAG,"g_fcc_tuner_id_list="+g_fcc_tuner_id_list);
        if(Pvcfg.isFccV2Enable()) {
            LogUtils.d("FCC_LOG FCC2 dispatch: mode=" + mode +
                    " cur=" + g_cur_ch_id + " pre=" + g_pre_ch_id +
                    " list=" + g_fcc_ch_id_list + " tuners=" + g_fcc_tuner_id_list +
                    " blocked=" + channel_blocked);
            g_dtv.AvControlPlayByChannelIdFCC(mode, g_fcc_ch_id_list, g_fcc_tuner_id_list, channel_blocked);
        }
        set_cur_ch_id(target_ch_id);
        g_miniEPG_channel_index = g_all_ch_id_list.indexOf(target_ch_id);
        isFirtPlay = false;
        Log.d(TAG,"change_channel_by_id end");
        return AVTunerID;
    }

    public void change_channel_by_digit(int digit) {
        Log.d(TAG, "change_channel_by_digit: digit = " + digit);

        // find channel by ch number
        ProgramInfo programInfo = g_dtv.get_program_by_ch_num(digit, FavGroup.ALL_TV_TYPE);
        if (programInfo == null) {
            // get from radio if not exist in tv
            programInfo = g_dtv.get_program_by_ch_num(digit, FavGroup.ALL_RADIO_TYPE);
        }

        long target_ch_id = programInfo == null ? 0 : programInfo.getChannelId();
        Log.d(TAG, "change_channel_by_digit: target_ch_id = " + target_ch_id);
        change_channel_by_id(target_ch_id);
    }

    public void change_channel_up() {
        Log.d(TAG, "change_channel_up: ");
        if (g_all_ch_id_list.size() > 1) {
            long target_ch_id = find_up_ch_id();
            Log.d(TAG, "change_channel_up: target_ch_id = " + target_ch_id);
            change_channel_by_id(target_ch_id);
        }
    }

    public void change_channel_down() {
        Log.d(TAG, "change_channel_down: ");
        if (g_all_ch_id_list.size() > 1) {
            long target_ch_id = find_down_ch_id();
            Log.d(TAG, "change_channel_down: target_ch_id = " + target_ch_id);
            change_channel_by_id(target_ch_id);
        }
    }

    public void change_pre_channel() {
        if (g_back_ch_id <= 0 || g_cur_ch_id == g_back_ch_id) {
            Log.d(TAG, "change_pre_channel: g_pre_ch_id not valid");
            return;
        }

        long target_ch_id = g_back_ch_id;
        Log.d(TAG, "change_pre_channel: target_ch_id = " + target_ch_id);
        change_channel_by_id(target_ch_id);
    }

    public ProgramInfo get_pre_channel() {
        long target_ch_id = g_back_ch_id;
        ProgramInfo programInfo = g_dtv.get_program_by_channel_id(target_ch_id);
        return programInfo;
    }

    public void set_previous_channel(ProgramInfo lastChannel) {
        g_force_update_back_ch_id = true;
        if (lastChannel != null)
            g_back_ch_id = lastChannel.getChannelId();
        else
            g_back_ch_id = 0;
    }

    public void set_previous_channel(long lastChannelId) {
        g_force_update_back_ch_id = true;
        g_back_ch_id = lastChannelId;
    }

    public ProgramInfo get_default_channel() {
        ProgramInfo programInfo = null;
        Log.d(TAG, "get_default_channel: ");
        if (g_all_ch_id_list.isEmpty()) {
            Log.d(TAG, "get_default_channel: no ch list");
            return programInfo;
        }

        long target_ch_id;
        // 1. change to cur ch
        // 2. change to cur ch id in gpos
        // 3. change to first ch in ch list
        LogUtils.d("g_cur_ch_id "+g_cur_ch_id);
        if (g_all_ch_id_list.contains(g_cur_ch_id)) {
            target_ch_id = g_cur_ch_id;
        }
        else if (g_all_ch_id_list.contains(GposInfo.getCurChannelId(mContext))) {
            target_ch_id = GposInfo.getCurChannelId(mContext);
        }
        else {
            target_ch_id = g_all_ch_id_list.get(0);
        }

        Log.d(TAG, "get_default_channel: target_ch_id = " + target_ch_id);
        programInfo = g_dtv.get_program_by_channel_id(target_ch_id);
        return programInfo;
    }

    public void change_default_channel() {
        change_channel_by_id(get_default_channel().getChannelId(), true);
    }

    public void change_1st_channel(){
        long target_ch_id = 0;//g_all_ch_id_list.get(0);
        if(g_all_ch_id_list.isEmpty())
        {
            update_all_channel();
        }
        if(g_all_ch_id_list.isEmpty()){
            return;
        }
        target_ch_id = g_all_ch_id_list.get(0);
        ProgramInfo programInfo = g_dtv.get_program_by_channel_id(target_ch_id);
        g_miniEPG_channel_index = 0;
        change_channel_by_id(programInfo.getChannelId(), true);
    }

    public void change_channel_stop_clear_fcc(){
        for (Long ch_id : g_fcc_tuner_mapping.keySet()) {
            Integer tuner_id = g_fcc_tuner_mapping.get(ch_id);
            if (tuner_id == 0) {
                g_dtv.av_control_clear_fast_change_channel(tuner_id, ch_id);
                g_dtv.stopMonitorTable(-1, tuner_id);
            }
        }
    }
    public void change_channel_stop(int mode, int stop_monitor_table) {
        LogUtils.d("[db_stopav] IN");
        ProgramInfo p = get_cur_channel();
        int tunerId = (p==null)?0:p.getTunerId();
        g_dtv.av_control_play_stop(tunerId, mode, stop_monitor_table);
//        if (mContext instanceof AppCompatActivity activity)
//            Utils.keep_screen_on(activity, false);
    }

    public long find_up_ch_id() {
        if (g_all_ch_id_list.isEmpty()) {
            return g_cur_ch_id;
        }

        int index = g_all_ch_id_list.indexOf(g_cur_ch_id);
        if (index < 0 || index+1 == g_all_ch_id_list.size()) { // not found or last
            return g_all_ch_id_list.get(0);
        }
        else {
            return g_all_ch_id_list.get(index+1);
        }
    }

    public long find_down_ch_id() {
        if (g_all_ch_id_list.isEmpty()) {
            return g_cur_ch_id;
        }

        int index = g_all_ch_id_list.indexOf(g_cur_ch_id);
        if (index < 0 || index-1 < 0) { // not found or first
            return g_all_ch_id_list.get(g_all_ch_id_list.size()-1);
        }
        else {
            return g_all_ch_id_list.get(index-1);
        }
    }

    private boolean update_cur_pre_ch_id_if_needed(long target_ch_id) {
        ProgramInfo programInfo = g_dtv.get_program_by_channel_id(target_ch_id);
        if(programInfo != null)
            Log.d(TAG, "update_cur_pre_ch_id_if_needed: target_ch_id = " + target_ch_id + ", lcn = " +programInfo.getLCN());
        programInfo = g_dtv.get_program_by_channel_id(g_cur_ch_id);
        if(programInfo != null)
            Log.d(TAG, "update_cur_pre_ch_id_if_needed: g_cur_ch_id = " + g_cur_ch_id + ", lcn = " +programInfo.getLCN());
        if (target_ch_id > 0 && target_ch_id != g_cur_ch_id) {
            Log.d(TAG, "update_cur_pre_ch_id_if_needed: update pre and cur ch id");
            g_pre_ch_id = g_cur_ch_id;
            if (g_force_update_back_ch_id)
                g_force_update_back_ch_id = false; // Turn off flag, Ending for channel up / channel down
            else
                g_back_ch_id = g_cur_ch_id;
            //if(Pvcfg.isFccEnable())
            //    g_dtv.startMonitorTable(g_pre_ch_id, 1);
            g_cur_ch_id = target_ch_id;
            return true; // cur/pre ch id updated
        }

        return false; // cur/pre ch id not updated
    }
    public void update_all_channel(int group_type) {
        List<ProgramInfo> all_ch_list = new ArrayList<>();
        if (!g_all_ch_id_list.isEmpty()) {
            g_all_ch_id_list.clear();
        }
        Log.d(TAG,"update_all_channel group_type = "+group_type);
        if(group_type == FavGroup.ALL_TV_TYPE) {
            // combine tv and radio channel id into g_all_ch_id_list
            List<ProgramInfo> tv_ch_list = g_dtv.get_program_info_list(
                    FavGroup.ALL_TV_TYPE,
                    MiscDefine.ProgramInfo.POS_ALL,
                    MiscDefine.ProgramInfo.NUM_ALL);
            List<ProgramInfo> radio_ch_list = g_dtv.get_program_info_list(
                    FavGroup.ALL_RADIO_TYPE,
                    MiscDefine.ProgramInfo.POS_ALL,
                    MiscDefine.ProgramInfo.NUM_ALL);
            all_ch_list.addAll(tv_ch_list);
            all_ch_list.addAll(radio_ch_list);

        }
        else {
            List<ProgramInfo> tv_ch_list = g_dtv.get_program_info_list(
                    group_type,
                    MiscDefine.ProgramInfo.POS_ALL,
                    MiscDefine.ProgramInfo.NUM_ALL);
            all_ch_list.addAll(tv_ch_list);
        }
        all_ch_list.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));
        for (ProgramInfo channel : all_ch_list) {
            LogUtils.d(TAG+ " CH "+channel.getDisplayNum()+" "+channel.getDisplayName());
            g_all_ch_id_list.add(channel.getChannelId());
        }

        Log.d(TAG, "update_all_channel : total ch size = " + g_all_ch_id_list.size());
    }

    public void update_all_channel() {
        if (!g_all_ch_id_list.isEmpty()) {
            g_all_ch_id_list.clear();
        }

        // combine tv and radio channel id into g_all_ch_id_list
        List<ProgramInfo> tv_ch_list = g_dtv.get_program_info_list(
                FavGroup.ALL_TV_TYPE,
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);
        List<ProgramInfo> radio_ch_list = g_dtv.get_program_info_list(
                FavGroup.ALL_RADIO_TYPE,
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);
        Log.d(TAG, "update_all_channel: tv_ch_list ch size = " + tv_ch_list.size());
        Log.d(TAG, "update_all_channel: radio_ch_list ch size = " + radio_ch_list.size());
        List<ProgramInfo> all_ch_list = new ArrayList<>();
        all_ch_list.addAll(tv_ch_list);
        all_ch_list.addAll(radio_ch_list);
        all_ch_list.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));
        Log.d(TAG, "update_all_channel: all_ch_list ch size = " + radio_ch_list.size());
        for (ProgramInfo channel : all_ch_list) {
            LogUtils.d(TAG+ " CH "+channel.getDisplayNum()+" "+channel.getDisplayName());
            g_all_ch_id_list.add(channel.getChannelId());
        }


        /*
        for (ProgramInfo tv_channel : tv_ch_list) {
            g_all_ch_id_list.add(tv_channel.getChannelId());
        }

        for (ProgramInfo radio_channel : radio_ch_list) {
            g_all_ch_id_list.add(radio_channel.getChannelId());
        }
        */

        Log.d(TAG, "update_all_channel: total ch size = " + g_all_ch_id_list.size());
    }

    private void set_program_fcc(long ch_id, int tuner_id){
        ProgramInfo programInfo = g_dtv.get_program_by_channel_id(ch_id);
        if(programInfo != null) {
            programInfo.setTunerId(tuner_id);
        }
        g_dtv.update_program_info(programInfo);
        g_fcc_tuner_mapping.put(ch_id, tuner_id);
    }

    private void reset_program_fcc(long ch_id) {
        ProgramInfo programInfo = g_dtv.get_program_by_channel_id(ch_id);
        if(programInfo != null) {
            programInfo.setTunerId(0); // reset to tuner id 0
        }
        g_dtv.update_program_info(programInfo);
        g_fcc_tuner_mapping.remove(ch_id);
    }

    private int do_fcc() {
        int mode = 0;
        if (!Pvcfg.isFccEnable() || g_all_ch_id_list.size() < 3 ) {
            Log.w(TAG, "do_fcc: fcc is disabled, do nothing AVTunerID = "+AVTunerID);
            g_fcc_tuner_mapping.remove(g_pre_ch_id);
            g_fcc_tuner_mapping.put(g_cur_ch_id, AVTunerID);
            if(AVTunerID != -1)
                setFCCVisible(AVTunerID);
            else
                setFCCVisible(0);
            return mode;
        }

        if (g_fcc_tuner_mapping.isEmpty()
                || !g_fcc_tuner_mapping.containsKey(g_cur_ch_id)
                || !g_fcc_tuner_mapping.containsKey(g_pre_ch_id)) {
            // fcc not start or current/pre channel not in fcc mapping
            // reset fcc, set fcc1 & 2 according to cur tuner id
            int cur_tuner_id = g_dtv.get_program_by_channel_id(g_cur_ch_id).getTunerId();
            long fcc1_ch_id = find_down_ch_id();
            long fcc2_ch_id = find_up_ch_id();
            if(!g_fcc_tuner_mapping.isEmpty())
                mode = 2;
//            ProgramInfo p = g_dtv.get_program_by_channel_id(fcc1_ch_id);
//            Log.d(TAG,"fcc1_ch_id = "+fcc1_ch_id+" lcn: "+p.getDisplayNum()+" "+p.getDisplayName());
//            p = g_dtv.get_program_by_channel_id(fcc2_ch_id);
//            Log.d(TAG,"fcc2_ch_id = "+fcc2_ch_id+" lcn: "+p.getDisplayNum()+" "+p.getDisplayName());
            // clear all fcc except playing channel
            if(!Pvcfg.isFccV2Enable()) {
                for (Long ch_id : g_fcc_tuner_mapping.keySet()) {
                    Integer tuner_id = g_fcc_tuner_mapping.get(ch_id);
                    if (tuner_id != null && tuner_id != cur_tuner_id) {
                        g_dtv.av_control_clear_fast_change_channel(tuner_id, ch_id);
                        //g_dtv.stopMonitorTable(ch_id);
                    }
                }
            }

            g_fcc_tuner_mapping.clear();
            if (cur_tuner_id == 0) {
                set_program_fcc(g_cur_ch_id, 0); // cur ch use tuner 0
                set_program_fcc(fcc1_ch_id, 1); // fcc1 ch use tuner 1
                set_program_fcc(fcc2_ch_id, 2); // fcc2 ch use tuner 2
                if(Pvcfg.isFccV2Enable()){
                    //g_fcc_av_info.put(fcc1_ch_id, 1);
                    //g_fcc_av_info.put(fcc2_ch_id, 2);
                    g_fcc_ch_id_list.add(fcc1_ch_id);
                    g_fcc_tuner_id_list.add(1);
                    g_fcc_ch_id_list.add(fcc2_ch_id);
                    g_fcc_tuner_id_list.add(2);
                    LogUtils.d("[FCC2] g_fcc_ch_id_list "+g_fcc_ch_id_list);
                    LogUtils.d("[FCC2] g_fcc_tuner_id_list "+g_fcc_tuner_id_list);
                }else {
                    g_dtv.av_control_set_fast_change_channel(1, fcc1_ch_id);
                    g_dtv.av_control_set_fast_change_channel(2, fcc2_ch_id);
                }
            }
            else if (cur_tuner_id == 1) {
                set_program_fcc(g_cur_ch_id, 1); // cur ch use tuner 1
                set_program_fcc(fcc1_ch_id, 0); // fcc1 ch use tuner 0
                set_program_fcc(fcc2_ch_id, 2); // fcc2 ch use tuner 2
                if(Pvcfg.isFccV2Enable()){
                    //g_fcc_av_info.put(fcc1_ch_id, 0);
                    //g_fcc_av_info.put(fcc2_ch_id, 2);
                    g_fcc_ch_id_list.add(fcc1_ch_id);
                    g_fcc_tuner_id_list.add(0);
                    g_fcc_ch_id_list.add(fcc2_ch_id);
                    g_fcc_tuner_id_list.add(2);
                    LogUtils.d("[FCC2] g_fcc_ch_id_list "+g_fcc_ch_id_list);
                    LogUtils.d("[FCC2] g_fcc_tuner_id_list "+g_fcc_tuner_id_list);
                }else {
                    g_dtv.av_control_set_fast_change_channel(0, fcc1_ch_id);
                    g_dtv.av_control_set_fast_change_channel(2, fcc2_ch_id);
                }
            }
            else if (cur_tuner_id == 2) {
                set_program_fcc(g_cur_ch_id, 2); // cur ch use tuner 2
                set_program_fcc(fcc1_ch_id, 0); // fcc1 ch use tuner 0
                set_program_fcc(fcc2_ch_id, 1); // fcc2 ch use tuner 1
                if(Pvcfg.isFccV2Enable()){
                    //g_fcc_av_info.put(fcc1_ch_id, 0);
                    //g_fcc_av_info.put(fcc2_ch_id, 1);
                    g_fcc_ch_id_list.add(fcc1_ch_id);
                    g_fcc_tuner_id_list.add(0);
                    g_fcc_ch_id_list.add(fcc2_ch_id);
                    g_fcc_tuner_id_list.add(1);
                    LogUtils.d("[FCC2] g_fcc_ch_id_list "+g_fcc_ch_id_list);
                    LogUtils.d("[FCC2] g_fcc_tuner_id_list "+g_fcc_tuner_id_list);
                }else {
                    g_dtv.av_control_set_fast_change_channel(0, fcc1_ch_id);
                    g_dtv.av_control_set_fast_change_channel(1, fcc2_ch_id);
                }
            }
            else {
                Log.w(TAG, "do_fcc: cur_tuner_id = " + cur_tuner_id + " out of bound, check!");
            }
            //g_dtv.startMonitorTable(g_cur_ch_id, 0);
            //g_dtv.startMonitorTable(fcc1_ch_id, 1);
            //g_dtv.startMonitorTable(fcc2_ch_id, 1);
            g_next_ch_id = fcc1_ch_id;
            g_pre_ch_id = fcc2_ch_id;
            if(Pvcfg.isFccV3Enable() == false)
                setFCCVisible(cur_tuner_id);
        } else {
            long ch_to_clear = 0;
            long ch_to_set = 0;
            mode = 1;
            if (g_fcc_tuner_mapping.size() != 3) {
                Log.w(TAG, "do_fcc: g_fcc_tuner_mapping size wrong, check!");
            }

            // remove cur and pre ch id in the map
            Integer cur_tuner_id = g_fcc_tuner_mapping.remove(g_cur_ch_id);
            Integer pre_tuner_id = g_fcc_tuner_mapping.remove(g_pre_ch_id);
            if (cur_tuner_id == null)
                cur_tuner_id = 0;
            if (pre_tuner_id == null)
                pre_tuner_id = 0;

            // the remaining ch which is not cur nor pre is the ch to clear
            for (Long ch_id : g_fcc_tuner_mapping.keySet()) {
                ch_to_clear = ch_id;
            }

            if (find_down_ch_id() == g_pre_ch_id) { // channel up, set fcc for up ch
                ch_to_set = find_up_ch_id();
            } else if (find_up_ch_id() == g_pre_ch_id) { // channel down, set fcc for down ch
                ch_to_set = find_down_ch_id();
            }

            Log.d(TAG, "do_fcc: clear ch = " + ch_to_clear);
            Log.d(TAG, "do_fcc: set ch = " + ch_to_set);
            Integer tuner_id = g_fcc_tuner_mapping.get(ch_to_clear);
            if (tuner_id != null) {
                reset_program_fcc(ch_to_clear); // fcc3. clear fcc
                if(Pvcfg.isFccV2Enable()){
                    //g_fcc_av_info.put(ch_to_clear, tuner_id);
                    g_fcc_ch_id_list.add(ch_to_clear);
                    g_fcc_tuner_id_list.add(tuner_id);
                    LogUtils.d("[FCC2] g_fcc_ch_id_list "+g_fcc_ch_id_list);
                    LogUtils.d("[FCC2] g_fcc_tuner_id_list "+g_fcc_tuner_id_list);
                }else {
                    g_dtv.av_control_clear_fast_change_channel(tuner_id, ch_to_clear);
                }
                //g_dtv.stopMonitorTable(ch_to_clear);

                set_program_fcc(ch_to_set, tuner_id); // fcc4. set fcc

                if(Pvcfg.isFccV2Enable()) {
                    //g_fcc_av_info.put(ch_to_set, tuner_id);
                    g_fcc_ch_id_list.add(ch_to_set);
                    g_fcc_tuner_id_list.add(tuner_id);
                    LogUtils.d("[FCC2] g_fcc_ch_id_list "+g_fcc_ch_id_list);
                    LogUtils.d("[FCC2] g_fcc_tuner_id_list "+g_fcc_tuner_id_list);
                }else {
                    g_dtv.av_control_set_fast_change_channel(tuner_id, ch_to_set);
                }
                g_next_ch_id = ch_to_set;
                //g_dtv.startMonitorTable(ch_to_set, 1);
            }

            set_program_fcc(g_cur_ch_id, cur_tuner_id);
            set_program_fcc(g_pre_ch_id, pre_tuner_id);

            if (pre_tuner_id != null) {
                if(Pvcfg.isFccV2Enable()) {
                    //g_fcc_av_info.put(g_pre_ch_id, pre_tuner_id);
                    g_fcc_ch_id_list.add(g_pre_ch_id);
                    g_fcc_tuner_id_list.add(pre_tuner_id);
                    LogUtils.d("[FCC2] g_fcc_ch_id_list "+g_fcc_ch_id_list);
                    LogUtils.d("[FCC2] g_fcc_tuner_id_list "+g_fcc_tuner_id_list);
                }else {
                    g_dtv.av_control_set_fast_change_channel(pre_tuner_id, g_pre_ch_id);
                }
                //g_dtv.startMonitorTable(g_pre_ch_id, 1);
            }

            if (cur_tuner_id != null) {
                setFCCVisible(cur_tuner_id);
            }
        }
        Log.d(TAG, "do_fcc: mapping = " + g_fcc_tuner_mapping);
        return mode;
    }

    public void reset_fcc() {
//        for (Long ch_id : g_fcc_tuner_mapping.keySet()) {
//            Integer tuner_id = g_fcc_tuner_mapping.get(ch_id);
//            if (tuner_id != null) {
//                g_dtv.av_control_clear_fast_change_channel(tuner_id, ch_id);
//                //g_dtv.stopMonitorTable(ch_id);
//            }
//        }
        g_dtv.av_control_set_play_status(0);
        g_dtv.av_control_play_stop_all();
//        set_cur_ch_id(0);
        g_fcc_tuner_mapping.clear();
    }

    public ProgramInfo signal_detect() {
        ProgramInfo programInfo = null;
        if(g_cur_ch_id != 0) {
            programInfo = g_dtv.get_program_by_channel_id(g_cur_ch_id);
            TpInfo tpInfo = g_dtv.tp_info_get(programInfo.getTpId());
//            Log.d(TAG,"program = "+programInfo.ToString());
//            Log.d(TAG,"tpInfo = "+tpInfo.ToString());
            TVTunerParams tvTunerParams =
                    TVTunerParams.CreateTunerParamDVBC(
                            programInfo.getTunerId(), 0/*sat id not used*/, 0/*tp id not used*/,
                            tpInfo.CableTp.getFreq(), tpInfo.CableTp.getSymbol(), tpInfo.CableTp.getQam());
            g_dtv.tuner_lock(tvTunerParams);
        }
        return programInfo;
    }

    /*
     * if epgData = null, it should be period record
     * if duration = 0, it will keep recording until manual stop
     * */
    public boolean pvr_record_start(long channelID, int eventId, int duration, boolean isSeries) {
        Log.d(TAG, "pvr_record_start: channelID = " + channelID + ", eventId = " + eventId +
                ", duration = " + duration + ", isSeries = " + isSeries);
        int rec_tuner_id = 0;
        int recordIndex = -1;

        if(Pvcfg.getPVR_PJ() == false) {
            LogUtils.d("PVR is not Enable. ");
            return false;
        }
        if(g_rec_num == Pvcfg.NUM_OF_RECORDING){
            LogUtils.d("PVR recording number is full  ");
            LogUtils.d("g_RecChannels[0] = "+g_RecChannels[0].toString());
            LogUtils.d("g_RecChannels[1] = "+g_RecChannels[1].toString());
            return false;
        }

        if((isTimeshiftStart == true) && (g_rec_num == 1)) {
            LogUtils.d("There are already both a video and a timeshift");
            return false;
        }

        if(is_channel_recording(channelID) ){
            LogUtils.d("channelId "+channelID+" is now recording!!!!!!!");
            return false;
        }

        if (!UsbUtils.check_usb_size()) {
            Log.e(TAG, "pvr_record_start: check size fail");
            return false;
        }

        EPGEvent epgData = null;
        if (eventId > -1) {
            epgData = g_dtv.get_epg_by_event_id(channelID, eventId);
        }

        if (isSeries && (epgData == null || !epgData.is_series())) {
            Log.e(TAG, "pvr_record_start: trying to do series recording with invalid epg");
            return false;
        }

        if (epgData != null) {
            epgData.set_short_event(g_dtv.get_short_description(channelID, eventId));
            epgData.set_extended_event(g_dtv.get_detail_description(channelID, eventId));
        }

        if(isTimeshiftStart == false){
            if(AVTunerID == -1) {
                AVTunerID = Objects.requireNonNullElse(g_fcc_tuner_mapping.get(g_cur_ch_id), 0);
            }
        }

        // clean FCC first
        boolean success = pvr_clear_fcc(false);
        if (!success) {
            Log.e(TAG, "pvr_record_start: clear fcc fail");
            return false;
        }

        ProgramInfo programInfo = g_dtv.get_program_by_channel_id(channelID);
        String chNum = programInfo != null ? programInfo.getDisplayNum(com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_CHANNEL_NUM) : "000";
        String chName = programInfo != null ? programInfo.getDisplayName() : "";
        String eventName = (epgData != null) ? epgData.get_event_name() : chName;
        boolean is4K = programInfo != null && programInfo.is_4k(programInfo);

        //Find rec_tuner_id
        rec_tuner_id = get_rec_tuner_id();
        if(g_rec_num == 1 && g_RecChannels[0] != null) {
            g_RecChannels[1] = new RecChannelInfo(channelID, eventId, rec_tuner_id, duration, chNum, eventName, is4K, epgData);
            recordIndex = 1;
        }
        else {
            g_RecChannels[0] = new RecChannelInfo(channelID, eventId, rec_tuner_id, duration, chNum, eventName, is4K, epgData);
            recordIndex = 0;
        }
        g_rec_num = get_rec_num();
        LogUtils.d(" [g_rec_num] " + g_rec_num + ", [rec_tuner_id] " + rec_tuner_id);

        boolean series=false;
        boolean episodeLast=false;
        int episode=0;
        boolean recordFromTimer=false;
        byte[] mSeriesKey = new byte[SeriesInfo.Series.MAX_SERIES_KEY_LENGTH];
        //EPGEvent epgData = g_dtv.get_present_event(channelID);

        if (isSeries) {
            series      = epgData.get_series_key()[0] != 0;
            episodeLast = epgData.get_episode_last() != 0;
            episode     = epgData.get_episode_key();
            mSeriesKey  = epgData.get_series_key();
            gRecSeriesMap.put(channelID, epgData.get_event_id());
        }
        else{
            if(epgData != null)
                gRecSingleMap.put(channelID, epgData.get_event_id());
            else
                gRecSingleMap.put(channelID, -1);
        }

        PvrRecStartParam startParam = new PvrRecStartParam(series,episodeLast,episode,recordFromTimer,duration,mSeriesKey,epgData,programInfo);
        g_dtv.pvr_RecordStart(startParam, rec_tuner_id);
        gRecIdMap.put(channelID, rec_tuner_id);
        gRecIndexMap.put(channelID, recordIndex);
        gLastRecChId = channelID;
        isRecording = true;
        Log.d(TAG, "pvr_record_start: Last Recording [Channel ID] " + gLastRecChId + ", [Count] " + g_rec_num);
        return true;
    }

    public void pvr_record_stop() {
        long channelId = get_cur_ch_id();
        pvr_record_stop(channelId);
        pvr_delete_kept_book(channelId);
    }

    public void pvr_record_stop(long channelId){
        int recId = get_record_id(channelId);
        int recIndex = get_record_index(channelId);
        LogUtils.d("channelId = "+channelId);
        if(recIndex == -1)
            return;
        if (g_RecChannels[recIndex] != null) {
            // g_dtv.pvr_record_stop(g_RecChannels[recId].getTunerId(),recId);
            g_dtv.pvr_RecordStop(recId);
            g_RecChannels[recIndex] = null;
            gRecIdMap.put(channelId, -1);
            gRecIndexMap.put(channelId, -1);
            gRecSeriesMap.put(channelId, null);
            gRecSingleMap.put(channelId, null);

            int recordCount = get_rec_num();
            if (recordCount == 0)
                gLastRecChId = 0;
            if (recordCount == 1) {
                for (int index = 0; index < Pvcfg.NUM_OF_RECORDING; index++) {
                    if (g_RecChannels[index] != null)
                        gLastRecChId = g_RecChannels[index].getChannelId();
                }
            }
            //Log.e(TAG, "pvr_record_stop: gLastRecChId = " + gLastRecChId);
        }
        g_rec_num = get_rec_num();
        if(g_rec_num == 0) {
            isRecording = false;
            Pvcfg.setFccEnable(true);
        }
        gRecordStopChId = channelId;
    }

    public void pvr_delete_kept_book(long channelId) {
        // normal epg booking will not be deleted when recording start, delete it when stop
        List<BookInfo> bookInfoList = g_dtv.book_info_get_list();
        for (BookInfo bookInfo : bookInfoList) {
            if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD_NOW
                    && bookInfo.getChannelId() == channelId) {
                g_dtv.book_info_delete(bookInfo.getBookId());
                g_dtv.save_table(EnTableType.TIMER);
                break;
            }
        }
    }

    public void pvr_delete_kept_books() {
        // normal epg booking will not be deleted when recording start, delete it when stop
        List<BookInfo> bookInfoList = g_dtv.book_info_get_list();
        boolean deleted = false;
        for (BookInfo bookInfo : bookInfoList) {
            if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD_NOW) {
                g_dtv.book_info_delete(bookInfo.getBookId());
                deleted = true;
            }
        }

        if (deleted) {
            g_dtv.save_table(EnTableType.TIMER);
        }
    }

    public void pvr_record_stop_all() {
        // stop first recording
        if (g_RecChannels[0] != null)
            pvr_record_stop(g_RecChannels[0].getChannelId());

        // stop second recording after 5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (g_RecChannels[1] != null)
                pvr_record_stop(g_RecChannels[1].getChannelId());
        }, 5000);
    }

    public void pvr_record_stop_all_no_delay() {
        for (RecChannelInfo recChannelInfo : g_RecChannels) {
            if (recChannelInfo != null) {
                pvr_record_stop(recChannelInfo.getChannelId());
            }
        }
    }

    public boolean is_channel_recording(long ch_id){
        for(int i=0 ; i<Pvcfg.NUM_OF_RECORDING; i++){
            if((g_RecChannels[i] != null)&& (g_RecChannels[i].isSameChannel(ch_id))){
                return true;
            }
        }
        return false;
    }

    public boolean is_last_recording(long channelId){
        for (RecChannelInfo recChannelInfo : g_RecChannels) {
            if (recChannelInfo != null && recChannelInfo.isSameChannel(channelId) && channelId == gLastRecChId)
                return true;
        }
        return false;
    }

    public boolean is_full_recording() {
        return get_rec_num() == Pvcfg.NUM_OF_RECORDING;
    }

    public boolean is_single_recording(long channelId, EPGEvent epgEvent) {
        Integer eventId = gRecSingleMap.get(channelId);
        return eventId != null && epgEvent != null && eventId == epgEvent.get_event_id();
    }

    public boolean is_series_recording(long channelId, EPGEvent epgEvent) {
        Integer eventId = gRecSeriesMap.get(channelId);
        return eventId != null && epgEvent != null && eventId == epgEvent.get_event_id();
    }

    public boolean is_reserve_record(List<BookInfo> bookInfoList, EPGEvent epgEvent) {
        if (bookInfoList == null || epgEvent == null)
            return false;

        for (BookInfo bookInfo :bookInfoList)
            if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD &&
                    bookInfo.getEpgEventId() == epgEvent.get_event_id() &&
                    !bookInfo.isSeries() &&
                    bookInfo.getBookCycle() != BookInfo.BOOK_CYCLE_ONETIME_EMPTY)
                return true;

        return false;
    }

    public boolean is_reserve_series_record(List<BookInfo> bookInfoList, EPGEvent epgEvent) {
        if (bookInfoList == null
                || epgEvent == null
                || epgEvent.get_start_time() <= System.currentTimeMillis()) // program has already started
            return false;

        for (BookInfo bookInfo : bookInfoList)
            if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD &&
                    bookInfo.isSeries() &&
                    Arrays.equals(epgEvent.get_series_key(), bookInfo.getSeriesRecKey()) &&
                    is_series_epg_record(epgEvent))
                return true;
        return false;
    }

    public boolean is_record_locked() {
        int rec_tuner_id = -1;
        boolean rec_tuner_locked = false;

        for (RecChannelInfo recChannelInfo : get_rec_channel_info()) {
            rec_tuner_id = get_record_tuner_id(recChannelInfo.getChannelId());
            rec_tuner_locked = g_dtv.get_tuner_status(rec_tuner_id);
            if (rec_tuner_locked)
                return true;
        }
        return false;
    }

    public boolean has_recording() {
        for (RecChannelInfo recChannelInfo : g_RecChannels) {
            if (recChannelInfo != null)
                return true;
        }
        return false;
    }

    public void pvr_timeshift_start() {
        ProgramInfo programInfo = get_prime_dtv().get_program_by_channel_id(g_cur_ch_id);
        int tuner_id;
        Log.d(TAG,"pvr_timeshift_start start");
        if(Pvcfg.getPVR_PJ() == false) {
            Log.d(TAG,"PVR is not Enable. ");
            return;
        }
        if(is_channel_recording(g_cur_ch_id)){
            Log.d(TAG,"PVR recording running!!! ");
            return;
        }

        if(isTimeshiftStart == true){
            Log.d(TAG,"Timeshift already start");
            return;
        }

        if(g_rec_num >= 2){
            Log.d(TAG,"There are already two recordings!!! ");
            return;
        }


        if(g_fcc_tuner_mapping.get(g_cur_ch_id) == null){
            tuner_id = 0;
        }
        else {
            tuner_id = g_fcc_tuner_mapping.get(g_cur_ch_id);
        }
        //clean FCC first
        Log.d(TAG,"pvr_clear_fcc start");
        if(g_rec_num > 0){
            g_dtv.av_control_play_stop(AVTunerID, 0, 1);
            g_dtv.stopMonitorTable(g_cur_ch_id, AVTunerID);
        }
        else{
        boolean success = pvr_clear_fcc(true);
        if(success == false){
            Log.d(TAG,"pvr_clear_fcc fail");
            return;
        }
        }

        if(g_rec_num == 1){
            int tunerIdTmp = -1,i;
            mTimeshiftRecordTunerId = AVTunerID;
            if(g_RecChannels[0] != null)
                tunerIdTmp = g_RecChannels[0].getTunerId();
            if(g_RecChannels[1] != null)
                tunerIdTmp = g_RecChannels[1].getTunerId();
            for(i=0;i<3;i++)
                if((i != tunerIdTmp) && (i != mTimeshiftRecordTunerId))
                    break;
            mPlaybackTunerId = i;
        }
        else{
            mTimeshiftRecordTunerId = tuner_id;
            if(tuner_id == 0)
        mPlaybackTunerId = 1;
            else
                mPlaybackTunerId = 0;
        }
        AVTunerID = -1;
        Log.d(TAG,"programInfo.setTunerId="+mTimeshiftRecordTunerId);

        //Log.e(TAG,"[Warning] This function not porting!! Please check");
        Log.d(TAG,"mTimeshiftRecordTunerId = "+mTimeshiftRecordTunerId+" mPlaybackTunerId = "+mPlaybackTunerId);
        setFCCVisible(mPlaybackTunerId);
        isTimeshiftStart = true;
        isTimeshiftPlaybackPause = false;

        Log.d(TAG,"pvr_timeshift_start end");
    }

    public void pvr_timeshift_stop() {
        LogUtils.e("pvr_timeshift_stop mPlaybackTunerId="+mPlaybackTunerId+" mTimeshiftRecordTunerId="+mTimeshiftRecordTunerId);
        ProgramInfo programInfo = get_prime_dtv().get_program_by_channel_id(g_cur_ch_id);
        if(g_rec_num != 0){
            Log.e(TAG,"[Warning] pvr_timeshift_stop This function not porting!! Please check");
        }
        if(isTimeshiftStart == true && is_timeshift_callback_started) {
            isTimeshiftStart = false;
            if(g_rec_num > 0) {
                AVTunerID = mPlaybackTunerId;
            }
            else {
                AVTunerID = -1;
            }
            mPlaybackTunerId = -1;
            mTimeshiftRecordTunerId = -1;
            g_dtv.pvr_TimeShiftStop();
            if(g_rec_num == 0)
                Pvcfg.setFccEnable(true);
            programInfo.setPvrSkip(0);
            if(AVTunerID == -1)
                programInfo.setTunerId(0);
            else
                programInfo.setTunerId(mPlaybackTunerId);
            g_dtv.update_program_info(programInfo);
            //setFCCVisible(0); // set surface view back to surface 0
        }
    }

    public void pvr_TimeShiftPlayPause(){
        LogUtils.d("mPlaybackTunerId = "+mPlaybackTunerId);
        isTimeshiftPlaybackPause = true;
        g_dtv.pvr_TimeShiftPlayPause(mPlaybackTunerId);
    }

    public void pvr_TimeShiftPlayResume(){
        LogUtils.d("mPlaybackTunerId = "+mPlaybackTunerId);
        isTimeshiftPlaybackPause = false;
        g_dtv.pvr_TimeShiftPlayResume(mPlaybackTunerId);
    }

    public void pvr_timeshift_rewind() {
        g_dtv.pvr_TimeShiftPlayRewind(mPlaybackTunerId);
    }

    public void pvr_timeshift_forward() {
        g_dtv.pvr_TimeShiftPlayFastForward(mPlaybackTunerId);
    }

    public void pvr_timeshift_seek(int position) {
        if (g_dtv != null)
            g_dtv.pvr_TimeShiftPlaySeek(mPlaybackTunerId, PvrInfo.EnSeekMode.PLAY_SEEK_SET, position);
    }

    public void set_timeshift_started(boolean started) {
        isTimeshiftStart = started;
    }

    public void set_timeshift_callback_started(boolean started) {
        is_timeshift_callback_started = started;
    }

    public PvrInfo.PlayTimeInfo get_timeshift_time_info() {
        return g_dtv.pvr_TimeShiftGetTimeInfo(mPlaybackTunerId);
    }

    public PvrInfo.EnPlayStatus get_timeshift_status() {
        return g_dtv.pvr_TimeShiftPlayGetStatus(mPlaybackTunerId);
    }

    public PvrInfo.EnPlaySpeed get_timeshift_speed() {
        return g_dtv.pvr_TimeShiftPlayGetSpeed(mPlaybackTunerId);
    }

    public boolean is_timeshift_play() {
        PvrInfo.EnPlayStatus status = g_dtv.pvr_TimeShiftPlayGetStatus(mPlaybackTunerId);
        return status == PvrInfo.EnPlayStatus.PLAY_STATUS_PLAY;
    }

    public boolean is_timeshift_pause() {
        PvrInfo.EnPlayStatus status = g_dtv.pvr_TimeShiftPlayGetStatus(mPlaybackTunerId);
        return status == PvrInfo.EnPlayStatus.PLAY_STATUS_PAUSE;
    }

    public boolean is_timeshift_forward() {
        PvrInfo.EnPlaySpeed speed = g_dtv.pvr_TimeShiftPlayGetSpeed(mPlaybackTunerId);
        return  speed == PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD02 ||
                speed == PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD04 ||
                speed == PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD08 ||
                speed == PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD16 ||
                speed == PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD32;
    }

    public boolean is_timeshift_rewind() {
        PvrInfo.EnPlaySpeed speed = g_dtv.pvr_TimeShiftPlayGetSpeed(mPlaybackTunerId);
        return  speed == PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV02 ||
                speed == PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV04 ||
                speed == PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV08 ||
                speed == PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV16 ||
                speed == PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV32;
    }

    public boolean is_timeshift_playback_pause(){
        LogUtils.d(" "+ isTimeshiftPlaybackPause);
        return isTimeshiftPlaybackPause;
    }

    public boolean is_timeshift_start(){
        //LogUtils.d(" "+isTimeshiftStart);
        return isTimeshiftStart;
    }

    public boolean is_timeshift_stop() {
        return !isTimeshiftStart;
    }

    public boolean is_timeshift_callback_started() {
        return is_timeshift_callback_started;
    }

    public boolean is_timeshift_callback_stopped() {
        return !is_timeshift_callback_started;
    }

    public boolean isFilePlayback(){
        return isFilePlayback;
    }

    public int pvr_playback_stop(){
        int rel;
        isFilePlayback = false;
        rel = g_dtv.pvr_PlayFileStop(mPlaybackTunerId);
        mPlaybackTunerId = -1;
        return  rel;
    }
    public int pvr_playback_start(PvrRecIdx recIdx, boolean fromLastPosition, int tunerId) {
//        Log.e(TAG,"[Warning] This function not porting!! Please check");
        isFilePlayback = true;
        int playbackTunerId = tunerId;
        if (playbackTunerId < 0 || playbackTunerId >= g_dtv.get_tuner_num()) {
            playbackTunerId = find_playback_tuner_id();
        }

        if (playbackTunerId == -1) {
            Log.e(TAG, "pvr_playback_start: invalid tuner id for playback");
            return -1;
        }

        mPlaybackTunerId = g_dtv.pvr_PlayFileStart(recIdx, fromLastPosition, playbackTunerId);
        return mPlaybackTunerId;
    }

    public int get_rec_num(){
        //return g_rec_num;
        int dataCount = 0;
        for (int index = 0; index < Pvcfg.NUM_OF_RECORDING; index++) {
            //Log.e(TAG, "get_rec_num: index = " + index + ", rec info = " + g_RecChannels[index]);
            if (g_RecChannels[index] != null)
                dataCount++;
        }
        return dataCount;
    }

    public RecChannelInfo get_rec_channel_info(int index){
        if(g_RecChannels[index] != null){
            return g_RecChannels[index];
        }
        return null;
    }

    public RecChannelInfo get_rec_channel_info_by_tuner(int tunerId) {
        for (RecChannelInfo recChannelInfo : g_RecChannels) {
            if (recChannelInfo != null && recChannelInfo.getTunerId() == tunerId) {
                return recChannelInfo;
            }
        }
        return null;
    }

    public List<RecChannelInfo> get_rec_channel_info() {
        List<RecChannelInfo> recChannelInfoList = new ArrayList<>();
        for (RecChannelInfo recChannelInfo : g_RecChannels) {
            if (null == recChannelInfo)
                continue;
            recChannelInfoList.add(recChannelInfo);
        }
        return recChannelInfoList;
    }

    public int get_record_id(long channelId) {
        Integer recId = gRecIdMap.get(channelId);
        return recId != null ? recId : -1;
    }

    public int get_record_index(long channelId) {
        Integer recIndex = gRecIndexMap.get(channelId);
        return recIndex != null ? recIndex : -1;
    }

    public int get_record_tuner_id(long channelId) {
        for (RecChannelInfo recChannelInfo : get_rec_channel_info()) {
            if (recChannelInfo.isSameChannel(channelId))
                return recChannelInfo.getTunerId();
        }
        return -1;
    }

    public long get_record_channel_id(int recId) {
        for (RecChannelInfo recChannelInfo : get_rec_channel_info()) {
            int curRecId = get_record_id(recChannelInfo.getChannelId());
            if (curRecId == recId)
                return recChannelInfo.getChannelId();
        }
        return -1;
    }

    public boolean is_recording() {
        return isRecording;
    }

    public boolean is_pvr_recording() {
        return isRecording || isTimeshiftStart;
    }

    public void change_channel_to_1st() {
        if (g_all_ch_id_list.isEmpty())
            return;
        if (g_all_ch_id_list.size() == 0)
            return;
        g_cur_ch_id = g_all_ch_id_list.get(0);
    }

    /** @noinspection DataFlowIssue*/
    public int get_rec_tuner_id(){
        int rec_tuner_id = 0;
        Log.e(TAG, "get_rec_tuner_id: AVTunerID = " + AVTunerID);
        Log.e(TAG, "get_rec_tuner_id: rec num = " + g_rec_num);
        if(isTimeshiftStart == false) {
            switch(AVTunerID) {
                case 0: {
                    if (g_rec_num == 0)
                        rec_tuner_id = 1;
                    else {
                        if (is_tuner_recording(1)) {
                            rec_tuner_id = 2;
                        } else {
                            rec_tuner_id = 1;
                        }
                    }
                }
                break;
                case 1: {
                    if (g_rec_num == 0)
                        rec_tuner_id = 2;
                    else {
                        if (is_tuner_recording(2)) {
                            rec_tuner_id = 0;
                        } else {
                            rec_tuner_id = 2;
                        }
                    }
                }
                break;
                case 2: {
                    if (g_rec_num == 0)
                        rec_tuner_id = 0;
                    else {
                        if (is_tuner_recording(0)) {
                            rec_tuner_id = 1;
                        } else {
                            rec_tuner_id = 0;
                        }
                    }
                }
                break;
                default: {
                    rec_tuner_id = 1;
                }
                break;
            }
        } else {
            int i;
            for(i=0;i<3;i++)
                if((i != mTimeshiftRecordTunerId) && (i != mPlaybackTunerId))
                    break;
            rec_tuner_id = i;
        }

        return rec_tuner_id;
    }

    public long get_last_record_ch_id() {
        return gLastRecChId;
    }

    public long get_record_stop_ch_id() {
        return gRecordStopChId;
    }

    public void remove_record_channel() {
        long channelID = get_last_record_ch_id();
        remove_record_channel(channelID);
    }

    public void remove_record_channel(long channelID) {
        int recIndex = get_record_index(channelID);

        g_RecChannels[recIndex] = null;
        gRecIdMap.put(channelID, -1);
        gRecIndexMap.put(channelID, -1);

        int recordCount = get_rec_num();
        if (recordCount == 0) {
            gLastRecChId = 0;
        }
        if (recordCount == 1) {
            for (int index = 0; index < Pvcfg.NUM_OF_RECORDING; index++) {
                if (g_RecChannels[index] != null)
                    gLastRecChId = g_RecChannels[index].getChannelId();
            }
        }

        g_rec_num = recordCount;
    }

    private boolean pvr_clear_fcc(boolean isTimeshift) {
        if(Pvcfg.isFccEnable() == false){
            LogUtils.d("FCC already disable");
            return true;
        }
        LogUtils.d("g_fcc_tuner_mapping = "+g_fcc_tuner_mapping);
        LogUtils.d("g_pre_ch_id = "+g_pre_ch_id);
        if(g_pre_ch_id != 0){
            Integer tuner_id = g_fcc_tuner_mapping.get(g_pre_ch_id);
            if (tuner_id != null) {
                Log.d(TAG, "FCC clean "+g_pre_ch_id);
                g_dtv.av_control_clear_fast_change_channel(tuner_id, g_pre_ch_id);
                g_fcc_tuner_mapping.remove(g_pre_ch_id);
                g_dtv.stopMonitorTable(g_pre_ch_id, tuner_id);
                g_pre_ch_id = 0;
            }
            else
                Log.e(TAG, "pvr_clear_fcc: something wrong, pre_ch_id = " + g_pre_ch_id);
        }
        LogUtils.d("g_next_ch_id = "+g_next_ch_id);
        if(g_next_ch_id != 0){
            Integer tuner_id = g_fcc_tuner_mapping.get(g_next_ch_id);
            if (tuner_id != null) {
                Log.d(TAG, "FCC clean "+g_next_ch_id);
                g_dtv.av_control_clear_fast_change_channel(tuner_id, g_next_ch_id);
                g_fcc_tuner_mapping.remove(g_next_ch_id);
                g_dtv.stopMonitorTable(g_next_ch_id, tuner_id);
                g_next_ch_id = 0;
            }
            else
                Log.e(TAG, "pvr_clear_fcc: something wrong, next_ch_id = " + g_next_ch_id);
        }
        //LogUtils.d("g_cur_ch_id = "+g_cur_ch_id);

        if (g_fcc_tuner_mapping.isEmpty()) {
            int tuner_id = 0;
            set_program_fcc(g_cur_ch_id, tuner_id);
            Log.e(TAG, "pvr_clear_fcc: g_fcc_tuner_mapping is empty, set_program_fcc ( " + g_cur_ch_id + ", " + tuner_id + " )");
        }

        Log.d(TAG, "pvr_clear_fcc: g_fcc_tuner_mapping = " + g_fcc_tuner_mapping);
        Log.d(TAG, "pvr_clear_fcc: g_cur_ch_id = " + g_cur_ch_id);

        Integer tuner_id = g_fcc_tuner_mapping.get(g_cur_ch_id);
        if (tuner_id != null) {
            //AVTunerID = tuner_id; //g_fcc_tuner_mapping.get(g_cur_ch_id);
            if(isTimeshift == true){
                ProgramInfo programInfo = get_prime_dtv().get_program_by_channel_id(g_cur_ch_id);
                if (programInfo == null) {
                    Log.e(TAG, "pvr_clear_fcc: programInfo is null");
                    return false;
                }
                programInfo.setPvrSkip(1);
                g_dtv.update_program_info(programInfo);
                g_dtv.av_control_clear_fast_change_channel(tuner_id, g_cur_ch_id);
                g_fcc_tuner_mapping.remove(g_cur_ch_id);
                g_dtv.stopMonitorTable(g_cur_ch_id, tuner_id);
                Log.d(TAG, "pvr_clear_fcc av_control_clear_fast_change_channel g_cur_ch_id="+g_cur_ch_id);
            }
            //g_fcc_tuner_mapping.clear();
            Pvcfg.setFccEnable(false);
            //isRecording = true;
            Log.d(TAG, "pvr_clear_fcc...01 g_cur_ch_id="+g_cur_ch_id+" AVTunerID="+AVTunerID+" isRecording="+isRecording);
            return true;
        }
        else if (!Pvcfg.isFccEnable()) {
            //isRecording = true;
            Log.d(TAG, "pvr_clear_fcc...02 AVTunerID="+AVTunerID+" isRecording="+isRecording);
            return true;
        }
        else {
            Log.e(TAG, "pvr_clear_fcc...03 something wrong, cur_ch_id = " + g_cur_ch_id);
            return false;
        }
    }

    // return whether the input series epgEvent should be record(marked with red dot)
    private boolean is_series_epg_record(EPGEvent epgEvent) {
        if (epgEvent == null || !epgEvent.is_series()) {
            return false;
        }

        // get series of the epgEvent
        g_dtv.add_series(epgEvent.get_channel_id(), epgEvent.get_series_key());
        SeriesInfo.Series series
                = g_dtv.get_series(epgEvent.get_channel_id(), epgEvent.get_series_key());


        if (series != null) {
            long startTime;
            for (SeriesInfo.Episode episode : series.getEpisodeList()) {
                LocalDateTime startDateTime = episode.getStartLocalDateTime();
                startTime = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                // there is an to-be-recorded episode that match the epgEvent
                // the epgEvent should be marked with red dot
                if (episode.getEpisodeKey() == epgEvent.get_episode_key()
                        && startTime == epgEvent.get_start_time()) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean is_tuner_recording(int tunerId) {
        for (RecChannelInfo gRecChannel : g_RecChannels) {
            if (gRecChannel != null && gRecChannel.getTunerId() == tunerId) {
                return true;
            }
        }

        return false;
    }

    public int getAVTunerID() {
        return AVTunerID;
    }

    private int find_playback_tuner_id() {
        Set<Integer> usedTunerIds = new HashSet<>();
        for (RecChannelInfo recInfo : get_rec_channel_info()) {
            usedTunerIds.add(recInfo.getTunerId());
        }

        int maxPlaybackTuners = g_dtv.get_tuner_num() - 1; // if 4 tuner, playback use first 3

        for (int tunerId = 0; tunerId < maxPlaybackTuners; tunerId++) {
            if (!usedTunerIds.contains(tunerId)) {
                Log.d(TAG, "find_playback_tuner_id: Found idle Tuner ID: " + tunerId);
                return tunerId;
            }
        }

        Log.d(TAG, "find_playback_tuner_id: No resources available (TunerId)");
        return -1;
    }
}
