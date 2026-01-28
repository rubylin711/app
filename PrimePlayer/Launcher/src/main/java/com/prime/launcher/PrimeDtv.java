package com.prime.launcher;

//import static com.prime.dtv.Interface.BaseManager.getPesiDtvFrameworkInterfaceCallback;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.datastructure.sysdata.CasData;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.MailData;
import com.prime.datastructure.sysdata.MailInfo;
import com.prime.datastructure.sysdata.MusicAdScheduleInfo;
import com.prime.datastructure.sysdata.MusicInfo;
import com.prime.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.launcher.Receiver.PrimeTimerReceiver;
import com.prime.launcher.ServiceInterface.PesiDtvFrameworkInterface;
import com.prime.datastructure.config.Pvcfg;
//import com.prime.dtv.service.Player.CasSession;
import com.prime.datastructure.sysdata.AudioInfo;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.PvrRecFileInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.datastructure.sysdata.PvrRecStartParam;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.FavInfo;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.PvrInfo;
import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.SeriesInfo;
import com.prime.datastructure.sysdata.SubtitleInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVMessage;
import com.prime.datastructure.utils.TVScanParams;
import com.prime.datastructure.utils.TVTunerParams;
import com.prime.datastructure.utils.JsonParser;
import com.prime.datastructure.ServiceDefine.PrimeDtvInterface;
import com.prime.launcher.Utils.Utils;

import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** @noinspection ConstantValue*/
public class PrimeDtv {
    private static final String TAG = "PrimeDtv";
    private final static String RequestDTVServiceVersion = "V1.0.3 - 20241010";
    private final static int CALLBACK_EVENT_EMM = 0x01;
    private final static int CALLBACK_EVENT_ECM = 0x02;
    private final static int CALLBACK_EVENT_PRIVATE_DATA = 0x03;
    private final static int CALLBACK_EVENT_OPEN_SSESION = 0x04;
    private final static int WV_TEST = 0xF0;//0x04;
    private final static int WV_TEST_CAS_READY = 0xF1;//0x05;
    private final static int WV_TEST_SET_SESSION_ID = 0xF2;//0x06;
    private static final int ONETIME_OBTAINED_NUMBER = 1000;
    private static final boolean DEBUG_EPG_EVENT_MAP = false;

    private PesiDtvFrameworkInterface gDtvFramework = null;
    private final LauncherCallback gDtvCallback;
    private Context mContext = null;

//    private IMedia mPrimeMediaService;
    private static final String PRIME_BTPAIR_PACKAGE = "com.prime.btpair";
    private static final String PRIME_BTPAIR_HOOKBEGINACTIVITY = "com.prime.btpair.HookBeginActivity";
    public static final int MINOR_DEVICE_CLASS_KEYBOARD =
            Integer.parseInt("0000001000000", 2);
    public static final int MINOR_DEVICE_CLASS_REMOTE =
            Integer.parseInt("0000000001100", 2);
    public void stop_schedule_eit() {
        LogUtils.d("[Ethan] stop_schedule_eit");
//        gDtvFramework.stopScheduleEit();
    }

    public interface LauncherCallback {
        void onMessage(TVMessage msg);
    }

    public PrimeDtv(LauncherCallback callback,Context context) {
        Log.d(TAG,"context = "+context);
        mContext = context;
        //gPrimeDtvMediaPlayer = PrimeDtvMediaPlayer.get_instance(context);
        gDtvFramework = PesiDtvFrameworkInterface.getInstance(new PrimeDtvInterface.DTVCallback() {
            @Override
            public void onMessage(TVMessage msg) {
                gDtvCallback.onMessage(msg);
            }
        }, context);
//        gDtvFramework.registerCallback(callback);
        LogUtils.d(" ");
//        gPrimeDtvMediaPlayer = PrimeDtvMediaPlayer.get_instance(context);
        LogUtils.d(" ");
        gDtvCallback = callback;
//        try {
//            mPrimeMediaService = IMedia.getService(true);
//            Log.d(TAG," Get mPrimeMediaService => [" + mPrimeMediaService + "]");
//        } catch (RemoteException | NoSuchElementException e) {
//            Log.e(TAG, "Exception : getService fail", e);
//        }

    }

    public void set_surface_view(Context context, SurfaceView surfaceView, int index)//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
        LogUtils.d("set_surface_view "+ surfaceView);
        gDtvFramework.set_surface_view(context,surfaceView, index);
    }
    public void set_surface_view(Context context, SurfaceView surfaceView)//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
        LogUtils.d("set_surface_view "+ surfaceView);
        gDtvFramework.set_surface_view(context,surfaceView);
    }

    public void setSurface(Context context, Surface surface, int index) {
        gDtvFramework.setSurface(context,surface,index);
    }


    /*
      video playback -e
      */

    public void start_scan(TVScanParams sp) {
        gDtvFramework.start_scan(sp);
    }

    public void stop_scan(boolean store) {
        gDtvFramework.stop_scan(store);
    }

    public void stopMonitorTable(long channel_id, int tuner_id){
        LogUtils.d("stopMonitorTable channel_id = "+channel_id+" tuner id = "+tuner_id);
        gDtvFramework.stopMonitorTable(channel_id, tuner_id);
    }
    public void startMonitorTable(long channel_id, int isFcc, int tuner_id){
        LogUtils.d("startMonitorTable channel_id = "+channel_id+" isFcc = "+isFcc+" tuner_id = "+tuner_id);
        gDtvFramework.startMonitorTable(channel_id, isFcc,tuner_id);
    }

    public void stopAllEitTableOnCurrentTp(int tuner_id){
        LogUtils.d("stopAllEitTableOnCurrentTp tuner id = "+tuner_id);
        gDtvFramework.stopAllEitTableOnCurrentTp(tuner_id);
    }

    public void UpdatePMT(long channel_id){

        gDtvFramework.UpdatePMT(channel_id);
    }

    public void StopDVBStubtitle(){
        gDtvFramework.StopDVBSubtitle();
    }

    public List<SatInfo> sat_info_get_list(int tunerType, int pos, int num) {
        return gDtvFramework.sat_info_get_list(tunerType, pos, num);
    }

    public SatInfo sat_info_get(int satId) {
        return gDtvFramework.sat_info_get(satId);
    }

    public int sat_info_add(SatInfo pSat) {
        return gDtvFramework.sat_info_add(pSat);
    }

    public int sat_info_update(SatInfo pSat) {
        return gDtvFramework.sat_info_update(pSat);
    }

    public int sat_info_update_list(List<SatInfo> pSats) {
        return gDtvFramework.sat_info_update_list(pSats);
    }

    public int sat_info_delete(int satId) {
        return gDtvFramework.sat_info_delete(satId);
    }

    public List<TpInfo> tp_info_get_list_by_satId(int tunerType, int satId, int pos, int num) {
        return gDtvFramework.tp_info_get_list_by_satId(tunerType, satId, pos, num);
    }

    public TpInfo tp_info_get(int tp_id) {
        return gDtvFramework.tp_info_get(tp_id);
    }

    public int tp_info_add(TpInfo pTp) {
        return gDtvFramework.tp_info_add(pTp);
    }

    public int tp_info_update(TpInfo pTp) {
        return gDtvFramework.tp_info_update(pTp);
    }

    public int tp_info_update_list(List<TpInfo> pTps) {
        return gDtvFramework.tp_info_update_list(pTps);
    }

    public int tp_info_delete(int tpId) {
        return gDtvFramework.tp_info_delete(tpId);
    }

    public int AvControlPlayByChannelIdFCC(int mode, List<Long> ch_id_list, List<Integer> tuner_id_list, boolean channelBlocked){
        return gDtvFramework.AvControlPlayByChannelIdFCC(mode, ch_id_list, tuner_id_list ,channelBlocked);
    }

    public int av_control_play_by_channel_id(int playId, long channelId, int groupType, int show) {
        return gDtvFramework.av_control_play_by_channel_id(playId, channelId, groupType, show);
    }

    public int av_control_play_stop(int tuenrId, int mode, int stop_monitor_table) {
        LogUtils.d("[db_stopav]AvControlPlayStop playId = "+tuenrId);
        return gDtvFramework.av_control_play_stop(tuenrId, mode, stop_monitor_table);
    }

    public int av_control_play_stop_all() {
//        LogUtils.d("AvControlPlayStop playId = "+playId);
        return gDtvFramework.av_control_play_stop_all();
    }

    public int av_control_set_fast_change_channel(int tunerId, long chId)
    {
        LogUtils.d(" IN ");
        return gDtvFramework.av_control_set_fast_change_channel(tunerId, chId);
    }

    public int av_control_clear_fast_change_channel(int tunerId, long chId)
    {
        LogUtils.d("[FCC2] ");
        return gDtvFramework.av_control_clear_fast_change_channel(tunerId, chId);
    }

    public int av_control_change_audio(int playId, AudioInfo.AudioComponent component) {
        return gDtvFramework.av_control_change_audio(playId, component);
    }

    //return value should be check
    public AudioInfo av_control_get_audio_list_info(int playId) {
        return gDtvFramework.av_control_get_audio_list_info(playId);
    }

    /* return status =  LIVEPLAY,TIMESHIFTPLAY.....etc  */
    public int av_control_get_play_status(int playId) {
        return gDtvFramework.av_control_get_play_status(playId);
    }

    public int av_control_set_play_status(int status) {
        return gDtvFramework.av_control_set_play_status(status);
    }

    public SubtitleInfo av_control_get_subtitle_list(int playId) {
        return gDtvFramework.av_control_get_subtitle_list(playId);
    }

    public int av_control_select_subtitle(int playId, SubtitleInfo.SubtitleComponent subtitleComponent) {
        return gDtvFramework.av_control_select_subtitle(playId, subtitleComponent);
    }

    public int av_control_show_subtitle(int playId, boolean enable) {
        return gDtvFramework.av_control_show_subtitle(playId, enable);
    }

    public int get_dtv_timezone() {
        return 0;
    }

    public List<BookInfo> book_info_get_list() {
        return gDtvFramework.book_info_get_list();
    }

    public BookInfo book_info_get(int bookId) {
        return gDtvFramework.book_info_get(bookId);
    }

    public int book_info_add(BookInfo bookInfo) {
        return gDtvFramework.book_info_add(bookInfo);
    }

    public int book_info_update(BookInfo bookInfo) {
        return gDtvFramework.book_info_update(bookInfo);
    }

    public int book_info_update_list(List<BookInfo> bookList) {
        return gDtvFramework.book_info_update_list(bookList);
    }

    public int book_info_delete(int bookId) {
        return gDtvFramework.book_info_delete(bookId);
    }

    public void set_alarms(Context context, Calendar alarmCal, Intent  intent) {
        PendingIntent tempPendingIntent = null ;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        BookInfo bookInfo = new BookInfo(intent.getExtras());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startCalFormatted = sdf.format(alarmCal.getTime());
        Log.d(TAG, "Start time: " + startCalFormatted + " intent " + intent );

        tempPendingIntent = PendingIntent.getBroadcast(context, bookInfo.getBookId(), intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCal.getTimeInMillis(), tempPendingIntent);
    }

    public void set_alarms(Context context, Intent  intent) {
        PendingIntent tempPendingIntent = null ;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        BookInfo bookInfo = new BookInfo(intent.getExtras());
        Calendar alarmCal = Calendar.getInstance();
        alarmCal.set(bookInfo.getYear(),
                bookInfo.getMonth() - 1, // bookinfo month = 1 ~ 12, -1 for Calendar
                bookInfo.getDate(),
                bookInfo.getStartTime() / 100,
                bookInfo.getStartTime() % 100,
                0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startCalFormatted = sdf.format(alarmCal.getTime());
        Log.d(TAG, "Start time: " + startCalFormatted + " intent " + intent );

        tempPendingIntent = PendingIntent.getBroadcast(context, bookInfo.getBookId(), intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCal.getTimeInMillis(), tempPendingIntent);
    }

    public void cancel_alarms(Context context, Intent  intent) {
        BookInfo bookInfo = new BookInfo(intent.getExtras());
        PendingIntent temp_pending_intent = PendingIntent.getBroadcast(context, bookInfo.getBookId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(temp_pending_intent);
    }

    public int book_info_delete_all() {
        return gDtvFramework.book_info_delete_all();
    }

    public BookInfo book_info_get_coming_book() {
        return gDtvFramework.book_info_get_coming_book();
    }

    public List<BookInfo> book_info_find_conflict_records(BookInfo bookInfo) {
        return gDtvFramework.book_info_find_conflict_records(bookInfo);
    }

    public List<BookInfo> book_info_find_conflict_reminds(BookInfo bookInfo) {
        return new ArrayList<>();
    }

    public void book_info_save(BookInfo bookInfo) {
        gDtvFramework.book_info_save(bookInfo);
    }

    public void schedule_next_timer(BookInfo bookInfo) {
        if (bookInfo == null) {
            Log.e(TAG, "schedule_next_timer: bookInfo == null");
            return;
        }

        // update date in bookinfo
        int bookInfoId = bookInfo.getBookId();
        set_timer_next_date_by_cycle(bookInfoId, bookInfo);

        // set next alarm
        bookInfo = book_info_get(bookInfoId); // get updated bookinfo
        if (bookInfo != null) {
            Intent intent = get_book_info_intent(bookInfo);
            if (bookInfo.getBookCycle() == BookInfo.BOOK_CYCLE_SERIES_EMPTY) {
                // cycle = BOOK_CYCLE_SERIES_EMPTY
                // set next alarm to start later to check again
                Log.d(TAG, "schedule_next_timer: BOOK_CYCLE_SERIES_EMPTY! adjust alarm time...");
                Calendar calendar = Calendar.getInstance(); // now
                calendar.add(Calendar.MINUTE, Pvcfg.SERIES_CHECK_MINUTE);
                set_alarms(mContext, calendar, intent);
            }
            else {
                set_alarms(mContext, intent);
            }
        }
    }

    public void schedule_next_timer(Intent bookIntent) {
        Bundle bookBundle = bookIntent.getExtras();
        if (bookBundle == null) {
            Log.e(TAG, "schedule_next_timer: bundle == null");
            return;
        }

        BookInfo bookInfo = new BookInfo(bookBundle);
        schedule_next_timer(bookInfo);
    }

    public void schedule_all_timers() {
        List<BookInfo> bookInfoList = book_info_get_list();
        for (BookInfo bookInfo : bookInfoList) {
            schedule_next_timer(bookInfo);
        }
    }

    public int tuner_lock(TVTunerParams tunerParams) {
        return gDtvFramework.tuner_lock(tunerParams);
    }

    public boolean get_tuner_status(int tuner_id) {
        if (tuner_id < 0)
            return false;
        return gDtvFramework.get_tuner_status(tuner_id);
    }

    public int get_signal_strength(int nTunerID) {
        return gDtvFramework.get_signal_strength(nTunerID);
    }

    public int get_signal_quality(int nTunerID) {
        return gDtvFramework.get_signal_quality(nTunerID);
    }

    public int get_signal_snr(int nTunerID) {
        return gDtvFramework.get_signal_snr(nTunerID);
    }

    public int get_signal_ber(int nTunerID) {
        return gDtvFramework.get_signal_ber(nTunerID);
    }

    public int tuner_set_antenna_5v(int tuner_id, int onOff) {
        return gDtvFramework.tuner_set_antenna_5v(tuner_id, onOff);
    }

    public int get_tuner_type() {
        return gDtvFramework.get_tuner_type();
    }

    public int save_table(EnTableType tableType) {
//        LogUtils.d("save_table tableType = "+tableType);
        return gDtvFramework.save_table(tableType);
    }

    public List<ProgramInfo> get_program_info_list(int type, int pos, int num) {
        LogUtils.d("DataManager");
        return gDtvFramework.get_program_info_list(type, pos, num);
    }

    public ProgramInfo get_program_by_service_id(int service_id) {
        return gDtvFramework.get_program_by_service_id(service_id);
    }

    public ProgramInfo get_program_by_service_id_transport_stream_id(int service_id, int ts_id) {
        return gDtvFramework.get_program_by_service_id_transport_stream_id(service_id, ts_id);
    }

    public ProgramInfo get_program_by_lcn(int lcn, int type) {
        return gDtvFramework.get_program_by_lcn(lcn, type);
    }

    public ProgramInfo get_program_by_ch_num(int chnum, int type) {
        return gDtvFramework.get_program_by_ch_num(chnum, type);
    }

    public ProgramInfo get_program_by_channel_id(long channelId) {
        return gDtvFramework.get_program_by_channel_id(channelId);
    }

    public int set_default_open_channel(long channelId, int groupType) {
        return gDtvFramework.set_default_open_channel(channelId, groupType);
    }

    public int update_sat_list(List<SatInfo> satInfoList) {
        return gDtvFramework.sat_info_update_list(satInfoList);
    }

    public int update_tp_list(List<TpInfo> tpInfoList) {
        return gDtvFramework.tp_info_update_list(tpInfoList);
    }

    public int update_program_info(ProgramInfo pProgram) {
        return gDtvFramework.update_program_info(pProgram);
    }

    public int fav_info_update_list(int favMode, List<FavInfo> favInfo) {
        return gDtvFramework.fav_info_update_list(favMode, favInfo);
    }

    public int update_book_list(List<BookInfo> bookInfoList) // Need command and implement
    {
        return gDtvFramework.book_info_update_list(bookInfoList);
    }

    public int start_epg(long channelID) {
        // epg use pesi c service, not java
        return gDtvFramework.start_epg(channelID);
//        Log.e(TAG,"this function not porting!! please check");
//        return 0;
    }

    public EPGEvent get_present_event(long channelID) {
        return gDtvFramework.get_present_event(channelID);
    }

    public EPGEvent get_follow_event(long channelID) {
        return gDtvFramework.get_follow_event(channelID);
    }

    public EPGEvent get_epg_by_event_id(long channelID, int eventId) {
        // epg use pesi c service, not java
        return gDtvFramework.get_epg_by_event_id(channelID, eventId);
//        return gDtvFramework.getEpgByEventID(channelID, eventId);
    }

    public List<EPGEvent> get_epg_events(long channelID, Date startTime, Date endTime, int pos, int reqNum, int addEmpty) {
        // epg use pesi c service, not java
        return gDtvFramework.get_epg_events(channelID, startTime, endTime, pos, reqNum, addEmpty);
//        return gDtvFramework.getEPGEvents(channelID, startTime, endTime, pos, reqNum, addEmpty);
    }

    public String get_short_description(long channelId, int eventId) {
        // epg use pesi c service, not java
        return gDtvFramework.get_short_description(channelId, eventId);
//        return gDtvFramework.getShortDescription(channelId, eventId);
    }

    public String get_detail_description(long channelId, int eventId) {
        // epg use pesi c service, not java
        return gDtvFramework.get_detail_description(channelId, eventId);
//        return gDtvFramework.getDetailDescription(channelId, eventId);
    }

    public long get_current_time() {
        //return gNetworkTime != null ? gNetworkTime.getTime() : System.currentTimeMillis();
        return System.currentTimeMillis();
    }

    public Date get_current_date() {
        return new Date(get_current_time());
    }

    public void setup_epg_channel() {
        gDtvFramework.setup_epg_channel();
    }

    public void start_schedule_eit(int tp_id,int tuner_id) {
        // use tuner framework to get epg raw data
        LogUtils.d("[Ethan] start_schedule_eit");
        gDtvFramework.start_schedule_eit(tp_id, tuner_id);
    }

    public Date get_dtv_date() {
//        return gPrimeDtvMediaPlayer.get_dtv_date();
        return new Date();
    }

    public FavInfo fav_info_get(int favMode, int index) {
//        return gPrimeDtvMediaPlayer.fav_info_get(favMode, index);
        return gDtvFramework.fav_info_get(favMode, index);
    }

    public List<FavInfo> fav_info_get_list(int favMode) {
//        return gPrimeDtvMediaPlayer.fav_info_get_list(favMode);
        return gDtvFramework.fav_info_get_list(favMode);
    }

    public int fav_info_delete(int favMode, long channelId) {
//        return gPrimeDtvMediaPlayer.fav_info_delete(favMode, channelId);
        return gDtvFramework.fav_info_delete(favMode, channelId);
    }

    public int fav_info_delete_all(int favMode) {
//        return gPrimeDtvMediaPlayer.fav_info_delete_all(favMode);
        return gDtvFramework.fav_info_delete_all(favMode);
    }

    public int fav_info_save_db(FavInfo favInfo) {
//        return gPrimeDtvMediaPlayer.fav_info_delete(favMode, channelId);
        return gDtvFramework.fav_info_save_db(favInfo);
    }

    public String fav_group_name_get(int favMode) {
//        return gPrimeDtvMediaPlayer.fav_group_name_get(favMode);
        return gDtvFramework.fav_group_name_get(favMode);
    }

    public int fav_group_name_update(int favMode, String name) {
//        return gPrimeDtvMediaPlayer.fav_group_name_update(favMode, name);
        return gDtvFramework.fav_group_name_update(favMode, name);
    }

    public List<FavGroup> fav_group_get_list() {
        return gDtvFramework.fav_group_get_list();
    }

    public GposInfo gpos_info_get() {
//        return gPrimeDtvMediaPlayer.gpos_info_get();
        return gDtvFramework.gpos_info_get();
    }

    public void gpos_info_update(GposInfo gPos) {
//        gPrimeDtvMediaPlayer.gpos_info_update(gPos);
        gDtvFramework.gpos_info_update(gPos);
    }


    public void gpos_info_update_by_key_string(String key, String value) {
//        gPrimeDtvMediaPlayer.gpos_info_update_by_key_string(key, value);
        gDtvFramework.gpos_info_update_by_key_string(key, value);
    }

    public void gpos_info_update_by_key_string(String key, int value) {
//        gPrimeDtvMediaPlayer.gpos_info_update_by_key_string(key, value);
        gDtvFramework.gpos_info_update_by_key_string(key, value);
    }

    public int get_tuner_num()//Scoty 20181113 add GetTunerNum function
    {
//        return gPrimeDtvMediaPlayer.get_tuner_num();
        return gDtvFramework.get_tuner_num();
    }

    // pvr -start
    // Start - Series Record
    public int pvr_init(String usbMountPath){
        LogUtils.d("usbMountPath = "+usbMountPath);
        return gDtvFramework.pvr_init(usbMountPath);
    }
    public int pvr_deinit(){
        return gDtvFramework.pvr_deinit();
    }
    public int pvr_RecordStart(PvrRecStartParam startParam,int tunerId)
    {
        LogUtils.d("channel ID = "+startParam.getmProgramInfo().getChannelId()+", tunerId = "+tunerId);
        return gDtvFramework.pvr_RecordStart(startParam, tunerId);
    }
    public int pvr_RecordStop(int recId){
        LogUtils.d("recId = "+recId);
        return gDtvFramework.pvr_RecordStop(recId);
    }
    public void pvr_RecordStopAll(){
        gDtvFramework.pvr_RecordStopAll();
    }
    public int pvr_RecordGetRecTimeByRecId(int recId){
        LogUtils.d("recId = "+recId);
        return gDtvFramework.pvr_RecordGetRecTimeByRecId(recId);
    }
    public boolean pvr_PlayCheckLastPositionPoint(PvrRecIdx recIdx){
        LogUtils.d("MasterId = "+recIdx.getMasterIdx()+", SeriesId = "+recIdx.getSeriesIdx());
        return gDtvFramework.pvr_PlayCheckLastPositionPoint(recIdx);
    }
    public int pvr_PlayFileStart(PvrRecIdx recIdx, boolean fromLastPosition, int tunerId){
        LogUtils.d("MasterId = "+recIdx.getMasterIdx()+", SeriesId = "+recIdx.getSeriesIdx()+", tunerId = "+tunerId + ", lastPositionFlag = "+fromLastPosition);
        return gDtvFramework.pvr_PlayFileStart(recIdx,fromLastPosition,tunerId);
    }
    public int pvr_PlayFileStop(int tunerId) {
        return gDtvFramework.pvr_PlayFileStop(tunerId);
    }
    public int pvr_PlayPlay(int playId)
    {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.pvr_PlayPlay(playId);
    }
    public int pvr_PlayPause(int playId){
        LogUtils.d("playId = "+playId);
        return gDtvFramework.pvr_PlayPause(playId);
    }
    public int pvr_PlayFastForward(int playId) {
        LogUtils.d("playId = " + playId);
        return gDtvFramework.pvr_PlayFastForward(playId);
    }
    public int pvr_PlayRewind(int playId) {
        LogUtils.d("playId = " + playId);
        return gDtvFramework.pvr_PlayRewind(playId);
    }
    public int pvr_PlaySeek(int playId, PvrInfo.EnSeekMode seekMode, int offsetSec)
    {
        LogUtils.d("playId = "+playId+", seekMode = "+seekMode+", offsetSec = "+offsetSec);
        return gDtvFramework.pvr_PlaySeek(playId,seekMode,offsetSec);
    }
    public int pvr_PlaySetSpeed(int playId, PvrInfo.EnPlaySpeed playSpeed)
    {
        LogUtils.d("playId = "+playId+", playSpeed = "+playSpeed);
        return gDtvFramework.pvr_PlaySetSpeed(playId,playSpeed);
    }
    public PvrInfo.EnPlaySpeed pvr_PlayGetSpeed(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.pvr_PlayGetSpeed(playId);
    }
    public int pvr_PlayChangeAudioTrack(int playId, AudioInfo.AudioComponent audioComponent) {
        LogUtils.d("playId = "+playId+", audioComponent = "+audioComponent);
        return gDtvFramework.pvr_PlayChangeAudioTrack(playId, audioComponent);
    }
    public int pvr_PlayGetCurrentAudioIndex(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.pvr_PlayGetCurrentAudioIndex(playId);
    }
    public int pvr_PlayGetPlayTime(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.pvr_PlayGetPlayTime(playId);
    }
    public PvrInfo.EnPlayStatus pvr_PlayGetPlayStatus(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.pvr_PlayGetPlayStatus(playId);
    }
    public PvrInfo.PlayTimeInfo pvr_PlayGetPlayTimeInfo(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.pvr_PlayGetPlayTimeInfo(playId);
    }
    public int pvr_TimeShiftStart(ProgramInfo programInfo,int recordTunerId,int playTunerId) {
        LogUtils.d("channel ID = "+programInfo.getChannelId()+", recordTunerId = "+recordTunerId+", playTunerId = "+playTunerId);
        return gDtvFramework.pvr_TimeShiftStart(programInfo, recordTunerId,playTunerId);
    }
    public int pvr_TimeShiftStop() {
        LogUtils.d(" ");
        return gDtvFramework.pvr_TimeShiftStop();
    }
    public int pvr_TimeShiftPlayStart(int tunerId) {
        LogUtils.d("tunerId = "+tunerId);
        return gDtvFramework.pvr_TimeShiftPlayStart(tunerId);
    }
    public int pvr_TimeShiftPlayPause(int tunerId) {
        LogUtils.d("tunerId = "+tunerId);
        return gDtvFramework.pvr_TimeShiftPlayPause(tunerId);
    }
    public int pvr_TimeShiftPlayResume(int tunerId) {
        LogUtils.d("tunerId = "+tunerId);
        return gDtvFramework.pvr_TimeShiftPlayResume(tunerId);
    }
    public int pvr_TimeShiftPlayFastForward(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.pvr_TimeShiftPlayFastForward(playId);
    }
    public int pvr_TimeShiftPlayRewind(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.pvr_TimeShiftPlayRewind(playId);
    }
    public int pvr_TimeShiftPlaySeek(int playId, PvrInfo.EnSeekMode seekMode, int startPosition) {
        LogUtils.d("playId = "+playId+" ,seekMode = "+seekMode+", startPosition = "+startPosition);
        return gDtvFramework.pvr_TimeShiftPlaySeek(playId,seekMode,startPosition);
    }
    public int pvr_TimeShiftPlaySetSpeed(int playId, PvrInfo.EnPlaySpeed playSpeed) {
        LogUtils.d("playId = "+playId+", playSpeed = "+playSpeed);
        return gDtvFramework.pvr_TimeShiftPlaySetSpeed(playId,playSpeed);
    }
    public PvrInfo.EnPlaySpeed pvr_TimeShiftPlayGetSpeed(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.pvr_TimeShiftPlayGetSpeed(playId);
    }
    public int pvr_TimeShiftPlayChangeAudioTrack(int playId, AudioInfo.AudioComponent audioComponent) {
        LogUtils.d("playId = "+playId+", audioComponent = "+audioComponent);
        return gDtvFramework.pvr_TimeShiftPlayChangeAudioTrack(playId, audioComponent);
    }
    public int pvr_TimeShiftPlayGetCurrentAudioIndex(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.pvr_TimeShiftPlayGetCurrentAudioIndex(playId);
    }
    public PvrInfo.EnPlayStatus pvr_TimeShiftPlayGetStatus(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.pvr_TimeShiftPlayGetStatus(playId);
    }
    public PvrInfo.PlayTimeInfo pvr_TimeShiftGetTimeInfo(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.pvr_TimeShiftGetTimeInfo(playId);
    }
    public PvrRecFileInfo pvr_GetFileInfoByIndex(PvrRecIdx tableKeyInfo) {
        LogUtils.d("MasterID = "+tableKeyInfo.getMasterIdx()+", SeriesID = "+tableKeyInfo.getSeriesIdx() );
        return gDtvFramework.pvr_GetFileInfoByIndex(tableKeyInfo);
    }
    public int pvr_GetRecCount() {
        return gDtvFramework.pvr_GetRecCount();
    }
    public int pvr_GetSeriesRecCount(int masterIndex) {
        LogUtils.d("masterIndex = "+masterIndex);
        return gDtvFramework.pvr_GetSeriesRecCount(masterIndex);
    }
    public List<PvrRecFileInfo> pvr_GetRecLink(int startIndex, int count) {
        LogUtils.d("startIndex = "+startIndex+", count = "+count );
        return gDtvFramework.pvr_GetRecLink(startIndex, count);
    }

    public List<PvrRecFileInfo> pvr_GetPlaybackLink(int startIndex, int count) {
        LogUtils.d("startIndex = "+startIndex+", count = "+count );
        return gDtvFramework.pvr_GetPlaybackLink(startIndex, count);
    }

    public List<PvrRecFileInfo> pvr_GetSeriesRecLink(PvrRecIdx tableKeyInfo, int count) {
        LogUtils.d("MasterID = "+tableKeyInfo.getMasterIdx()+", SeriesID = "+tableKeyInfo.getSeriesIdx()+", count = "+count );
        return gDtvFramework.pvr_GetSeriesRecLink(tableKeyInfo, count);
    }
    public int pvr_DelAllRecs() {
        return gDtvFramework.pvr_DelAllRecs();
    }
    public int pvr_DelSeriesRecs(int masterIndex) {
        LogUtils.d("masterIndex = "+masterIndex);
        return gDtvFramework.pvr_DelSeriesRecs(masterIndex);
    }
    public int pvr_DelRecsByChId(int channelId) {
        LogUtils.d("channelId = "+channelId);
        return gDtvFramework.pvr_DelRecsByChId(channelId);
    }
    public int pvr_DelOneRec(PvrRecIdx tableKeyInfo){
        LogUtils.d("MasterID = "+tableKeyInfo.getMasterIdx()+", SeriesID = "+tableKeyInfo.getSeriesIdx() );
        return gDtvFramework.pvr_DelOneRec(tableKeyInfo);
    }
    public int pvr_DelOnePlayback(PvrRecIdx tableKeyInfo){
        LogUtils.d("MasterID = "+tableKeyInfo.getMasterIdx()+", SeriesID = "+tableKeyInfo.getSeriesIdx() );
        return gDtvFramework.pvr_DelOnePlayback(tableKeyInfo);
    }
    public int pvr_CheckSeriesEpisode(byte[] seriesKey, int episode){
        LogUtils.d("episode = "+episode);
        return gDtvFramework.pvr_CheckSeriesEpisode(seriesKey,episode);
    }
    public boolean pvr_CheckSeriesComplete(byte[]  seriesKey){
        return gDtvFramework.pvr_CheckSeriesComplete(seriesKey);
    }
    public boolean pvr_IsIdxInUse(PvrRecIdx pvrRecIdx) {
        return gDtvFramework.pvr_IsIdxInUse(pvrRecIdx);
    }

    // End - Series Record
    public int pvr_record_check(long channelID)
    {
        Log.e(TAG,"[Warning] This function not porting!! Please check");
        return 0;
    }

    /* === HDD Manger Start === */
    public void hdd_monitor_start(long alert_sizeMB) {
        gDtvFramework.hdd_monitor_start(alert_sizeMB);
    }
    public void hdd_monitor_stop() {
        gDtvFramework.hdd_monitor_stop();
    }
    /* === HDD Manger End === */

    // Johnny 20180814 add setDiseqc1.0 port -s
    public int set_diseqc10_port_info(int nTunerId, int nPort, int n22KSwitch, int nPolarity) {
//        return gPrimeDtvMediaPlayer.set_diseqc10_port_info(nTunerId, nPort, n22KSwitch, nPolarity);
        return gDtvFramework.set_diseqc10_port_info(nTunerId, nPort, n22KSwitch, nPolarity);
    }

    //Scoty add DiSeqC Motor rule -s
    public int set_diseqc12_move_motor(int nTunerId, int Direct, int Step) {
//        return gPrimeDtvMediaPlayer.set_diseqc12_move_motor(nTunerId, Direct, Step);
        return gDtvFramework.set_diseqc12_move_motor(nTunerId, Direct, Step);
    }

    public int set_diseqc12_move_motor_stop(int nTunerId) {
//        return gPrimeDtvMediaPlayer.set_diseqc12_move_motor_stop(nTunerId);
        return gDtvFramework.set_diseqc12_move_motor_stop(nTunerId);
    }

    public int reset_diseqc12_position(int nTunerId) {
//        return gPrimeDtvMediaPlayer.reset_diseqc12_position(nTunerId);
        return gDtvFramework.reset_diseqc12_position(nTunerId);
    }

    public int set_diseqc_limit_pos(int nTunerId, int limitType) {
//        return gPrimeDtvMediaPlayer.set_diseqc_limit_pos(nTunerId, limitType);
        return gDtvFramework.set_diseqc_limit_pos(nTunerId, limitType);
    }

    //Scoty add DiSeqC Motor rule -e
    // Johnny 20180814 add setDiseqc1.0 port -e

    public int set_module_type(String type){
        return gDtvFramework.set_module_type(type);
    }

    public List<EPGEvent> EpgEventGetEPGEventList(long channelId, long startTime, long endTime, int addEmpty) {
        ArrayList<EPGEvent> list = null;

        Log.d(TAG, "EpgEventGetEPGEventList:  start = " + startTime + "         endTime = " + endTime);
        if (( channelId >= 0) && (startTime >= 0)  && (endTime-startTime > 0))
        {
            Log.d(TAG, "getEPGEvents(ChannelID = " + channelId + ",startTime = " + startTime
                    + ",duration = " + (startTime - endTime) + ")");

            int i = 0;
            list = new ArrayList<EPGEvent>();
            List<EPGEvent> secondaryList = null;
            Date startDate = new Date(startTime);
            Date endDate = new Date(endTime);
            while (true)
            {
                // update start offset position
                int offset = ONETIME_OBTAINED_NUMBER * (i++);

                // obtain event list by filter with channel and time enabled

                secondaryList = gDtvFramework.get_epg_events(channelId, startDate, endDate, offset, ONETIME_OBTAINED_NUMBER, addEmpty);
                if (null == secondaryList)
                {
                    // while no event is obtained
                    break;
                }

                list.addAll(secondaryList);
                int count = secondaryList.size();
                if ((count >= 0) && (count < ONETIME_OBTAINED_NUMBER))
                {
                    // while events by filter are all obtained
                    break;
                }

                EPGEvent lastEvent = secondaryList.get(count - 1);
                if (null == lastEvent)
                {
                    // while the last event is invalid
                    break;
                }

                // Date lastEndDate = lastEvent.getEndTime();
                Date lastEndDate = new Date(lastEvent.get_end_time());

                if (lastEndDate.after(endDate))
                {
                    // while endTime of last event is after endTime of filter
                    break;
                }

                // update endTime of filter by endTime of last event as startTime of filter to next
                // time
                startTime = lastEvent.get_end_time();
                startDate = new Date(startTime);
            }
        }
        else
        {
            Log.d(TAG, "getEPGEvents:one or more parameter is invalid.");
        } // if(((null != channel) && (channel.getChannelID() >= 0)) && (startTime >= 0) &&
        // (duration > 0) )
        return list;
    }

    /**
     * First Time Get netprogram.ini File and Add to DataBase
     * After Save Complete Rename to netprogram_already_set.ini
     * In order to not to save database again
     *
     * @return isSuccess : Save DataBase Results
     */

    public void remove_wvcas_license(String licenseId) {
//        try {
//            LogUtils.d("remove licenseId = " + licenseId);
//            mPrimeMediaService.removeWvcasLicense(licenseId);
//        } catch (RemoteException e) {
//            throw new RuntimeException(e);
//        }
    }

    public static boolean isRemoteControl (BluetoothDevice device) {
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        LogUtils.d("isRemoteControl device="+device+" name="+device.getName()
                +" getDeviceClass="+bluetoothClass.getDeviceClass()
                +" devicetype="+device.getType());
        if (bluetoothClass.getMajorDeviceClass() == BluetoothClass.Device.Major.PERIPHERAL &&
                BluetoothDevice.DEVICE_TYPE_LE == device.getType() &&
                (((bluetoothClass.getDeviceClass() & MINOR_DEVICE_CLASS_REMOTE)!= 0)
                        || ((bluetoothClass.getDeviceClass() & MINOR_DEVICE_CLASS_KEYBOARD)!= 0))) {
            Log.d(TAG,"isRemoteControl device="+device+" is remote control");
            return true;
        }
        return false;
    }
    public static boolean hasRemoteControl () {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            final Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();
            if (bondedDevices != null) {
                for (BluetoothDevice device : bondedDevices) {
                    if (isRemoteControl(device)) {
                        Log.d(TAG, "Still has remote control device="+device);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void check_bt_remote_connect(Context context) {
        if (hasRemoteControl())
            return;
        Intent newIntent = new Intent();
        newIntent.setComponent(new ComponentName(PRIME_BTPAIR_PACKAGE, PRIME_BTPAIR_HOOKBEGINACTIVITY));
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(newIntent);
    }

    public void handleCasRefresh(final int deleteFlag, final int delay, long channel_id) {
        Log.d(TAG, "handleCasRefresh: deleteFlag = " + deleteFlag + " delay = " + delay+" channel_id = "+channel_id);
        gDtvFramework.handleCasRefresh(deleteFlag,delay,channel_id);
//        Thread newThread = new Thread(() -> {
//            boolean need_set_channel = false;
//            ProgramInfo programInfo = get_program_by_channel_id(channel_id);
//            String content_id = null;
//            if(programInfo != null) {
//                if(programInfo.getType() == ProgramInfo.PROGRAM_TV) {
//                    content_id = CasRefreshHelper.parse_content_id(programInfo.pVideo.getPrivateData(19156));
//                } else {
//                    content_id = CasRefreshHelper.parse_content_id(programInfo.pAudios.get(0).getPrivateData(19156));
//                }
//                LogUtils.d("handleCasRefresh Check content_id "+content_id);
//            }
//            if (delay > 0) {
//                try {
//                    Thread.sleep(delay * 1000L);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            CasRefreshHelper casRefreshHelper = CasRefreshHelper.get_instance();
//            CasData old_casData = casRefreshHelper.get_cas_data();
//            List<String> old_EntitledContentIds = old_casData.getEntitledChannelIds();
//            LogUtils.d("old_EntitledContentIds = "+old_EntitledContentIds);
//            // get latest cas data from server
//            casRefreshHelper.request_new_cas_data();
//            CasData casData = casRefreshHelper.get_cas_data();
//            List<String> new_EntitledContentIds = casData.getEntitledChannelIds();
//            LogUtils.d("new_EntitledContentIds = "+new_EntitledContentIds);
//            // update configs
//            Pvcfg.setTryLicenseEntitledOnly(casData.getTryLicenseIfEntitledChannel() == 1);
//            //Pvcfg.setPVR_PJ(casData.getPvr() == 1);
//
//            // send suspended
//            if (casData.getSuspended() == 1) {
//                sendCasError(ErrorCodeUtil.ERROR_E511, "");
//            }
//            List<String> unentitledContentIds = casRefreshHelper.get_unentitled_content_ids();
//            LogUtils.d("unentitledContentIds = "+unentitledContentIds);
//            // remove offline licenses
//            if (deleteFlag == 1) {
//                // remove all offline license file
//                remove_wvcas_license(""); // empty string will remove all
//                // clear lic record in lic map
//                casRefreshHelper.clear_license_mapping();
//
//                //need_set_channel = true;
//            }
//            else {
//                for (String contentId : unentitledContentIds) {
//                    String licenseId = casRefreshHelper.get_license_id(contentId);
//                    if (licenseId != null) {
//                        // remove unentitled offline license file
//                        Log.d(TAG, "handleCasRefresh: licenseId = " + licenseId);
//                        remove_wvcas_license(licenseId);
//                        // remove unentitled lic record in lic map
//                        casRefreshHelper.remove_license_mapping(contentId, licenseId);
//                    }
//                }
//            }
//            // try licenses
//            if (casData.getTryLicenseAfterRefresh() == 1) {
//                List<String> entitledChannelIds = casData.getEntitledChannelIds();
//                for (String contentId : entitledChannelIds) {
//                    //if(need_set_channel == true && contentId.equals(content_id))
//                    //    continue;
//                    try {
//                        byte[] privateData = CasRefreshHelper.generate_private_data(casData.getCasProvider(), contentId);
//
//                        CasSession casSession = new CasSession(Pvcfg.WVCAS_CA_SYSTEM_ID);
//                        casSession.setPrivateData(privateData);
//                        casSession.openForRefresh();
//                        Thread.sleep(300);
//                    } catch (Exception e) {
////                        throw new RuntimeException(e);
//                    }
//                }
//            }
//            LogUtils.d("programInfo = "+programInfo);
//            if(programInfo != null){
//                if(unentitledContentIds.contains(content_id))
//                    need_set_channel = true;
//                else{
//                    if(new_EntitledContentIds.contains(content_id) && !old_EntitledContentIds.contains(content_id))
//                        need_set_channel = true;
//                }
//                if(old_casData.getSuspended() != casData.getSuspended())
//                    need_set_channel = true;
//                LogUtils.d("getSuspended = "+old_casData.getSuspended()+" "+casData.getSuspended());
//                LogUtils.d("unentitledContentIds.contains(content_id) = "+unentitledContentIds.contains(content_id));
//                LogUtils.d("old_unentitledContentIds.contains(content_id) = "+old_EntitledContentIds.contains(content_id));
//                LogUtils.d("new_unentitledContentIds.contains(content_id) = "+unentitledContentIds.contains(content_id));
//                LogUtils.d("need_set_channel = "+need_set_channel);
//                if(need_set_channel) {
//                    TVMessage tvMessage = TVMessage.SetChannel(programInfo.getChannelId());
//                    gDtvCallback.onMessage(tvMessage);
//                    av_control_play_stop(0, 0, 0);
//                    av_control_play_by_channel_id(programInfo.getTunerId(), programInfo.getChannelId(), ViewHistory.getCurGroupType(), 1);
//                }
//            }
//            LogUtils.d("End");
//        },"handleCasRefresh");
//
//        newThread.start();
    }

    private void sendCasError(final int errorCode, final String msg) {
        Log.d(TAG, "sendCasError: errorCode = " + errorCode + " msg = " + msg);
        TVMessage tvMessage = TVMessage.SetShowErrorMsg(errorCode, msg, Pvcfg.CA_TYPE.CA_WIDEVINE_CAS);
        gDtvCallback.onMessage(tvMessage);
    }

    public void ResetCheckAD(){
        gDtvFramework.ResetCheckAD();
    }

    public int add_series(long channelId, byte[] key){
        return gDtvFramework.add_series(channelId,key);
    }

    public int delete_series(long channelId, byte[] key){
        return gDtvFramework.delete_series(channelId,key);
    }

    public SeriesInfo.Series get_series(long channelId, byte[] key){
        return gDtvFramework.get_series(channelId, key);
    }

    public int save_series() {
        return gDtvFramework.save_series();
    }

    private LocalDateTime get_next_date_onetime(LocalDateTime localDateTime, LocalDateTime bookDateTime) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            nextDateTime = null; // no next date if onetime
        }

        return nextDateTime;
    }

    private LocalDateTime get_next_date_daily(LocalDateTime localDateTime, LocalDateTime bookDateTime) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            nextDateTime = bookDateTime.with(localDateTime.toLocalDate()); // set next book date to today
            if (!nextDateTime.isAfter(localDateTime)) { // if next book datetime still  <= local datetime
                nextDateTime = nextDateTime.plusDays(1); // next day
            }
        }

        return nextDateTime;
    }

    private LocalDateTime get_next_date_weekly(LocalDateTime localDateTime, LocalDateTime bookDateTime) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            DayOfWeek bookDayOfWeek = bookDateTime.getDayOfWeek();
            nextDateTime = localDateTime
                    .with(TemporalAdjusters.nextOrSame(bookDayOfWeek)) // next or same dayOfWeek
                    .with(bookDateTime.toLocalTime());
            if (!nextDateTime.isAfter(localDateTime)) { // if next book datetime still <= local datetime
                nextDateTime = nextDateTime.plusWeeks(1); // next week
            }
        }

        return nextDateTime;
    }

    private LocalDateTime get_next_date_weekend(LocalDateTime localDateTime, LocalDateTime bookDateTime) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            nextDateTime = bookDateTime.with(localDateTime.toLocalDate()); // set next book datetime to today
            if (nextDateTime.getDayOfWeek() == DayOfWeek.SATURDAY
                    && !nextDateTime.isAfter(localDateTime)) { // Saturday && next book datetime still <= local datetime
                nextDateTime = nextDateTime.plusDays(1); // now(Saturday) + 1 day = Sunday
            }
            else if (nextDateTime.getDayOfWeek() == DayOfWeek.SUNDAY
                    && !nextDateTime.isAfter(localDateTime)) { // Sunday && next book datetime still <= local datetime
                nextDateTime = nextDateTime.plusDays(6); // now(Sunday) + 6 day = Saturday
            }
            else { // now = Monday, Tuesday, ..., Friday
                nextDateTime = nextDateTime
                        .with(TemporalAdjusters.next(DayOfWeek.SATURDAY)); // next Saturday
            }
        }

        return nextDateTime;
    }

    private LocalDateTime get_next_date_weekdays(LocalDateTime localDateTime, LocalDateTime bookDateTime) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            nextDateTime = bookDateTime.with(localDateTime.toLocalDate()); // set next book datetime to today
            if (nextDateTime.getDayOfWeek() == DayOfWeek.SATURDAY
                    || nextDateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
                nextDateTime = nextDateTime.with(TemporalAdjusters.next(DayOfWeek.MONDAY)); // next Monday
            }
            else if (nextDateTime.getDayOfWeek() == DayOfWeek.FRIDAY) {
                if (!nextDateTime.isAfter(localDateTime)) { // next book datetime still <= local datetime
                    nextDateTime = nextDateTime.with(TemporalAdjusters.next(DayOfWeek.MONDAY)); // next Monday
                }
            }
            else { // Monday, Tuesday, ..., Thursday
                if (!nextDateTime.isAfter(localDateTime)) { // next book datetime still <= local datetime
                    nextDateTime = nextDateTime.plusDays(1); // next day
                }
            }
        }

        return nextDateTime;
    }

    private LocalDateTime get_next_date_monthly(LocalDateTime localDateTime, LocalDateTime bookDateTime) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            // set to this year to reduce searching time
            nextDateTime = nextDateTime.withYear(localDateTime.getYear());
        }

        // find next valid month with book's dayOfMonth
        // e.g. 01/28 -> 02/28, 01/31 -> 03/31
        while (!nextDateTime.isAfter(localDateTime) // next book datetime <= local datetime
                || nextDateTime.getDayOfMonth() != bookDateTime.getDayOfMonth()) {
            try {
                nextDateTime = nextDateTime.plusMonths(1); // next month
                nextDateTime = nextDateTime.withDayOfMonth(bookDateTime.getDayOfMonth()); // set date to book date
            } catch (DateTimeException ignored) {

            }
        }

        return nextDateTime;
    }

    // bookInfo may be modified if needed
    private LocalDateTime get_next_date_series(LocalDateTime localDateTime, LocalDateTime bookDateTime, @NonNull BookInfo bookInfo) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            add_series(bookInfo.getChannelId(), bookInfo.getSeriesRecKey());
            SeriesInfo.Series series = get_series(bookInfo.getChannelId(), bookInfo.getSeriesRecKey());

            boolean found = false;
            if (series != null) {
                // try to find valid episode in series
                for (SeriesInfo.Episode episode : series.getEpisodeList()) {
                    LocalDateTime episodeStartTime = episode.getStartLocalDateTime();
                    if (!episodeStartTime.isBefore(bookDateTime) // episodeStartTime >= bookDateTime
                            && episodeStartTime.isAfter(localDateTime) // episodeStartTime > localDateTime
                            // pvr_CheckSeriesEpisode != 0 means series not recorded
                            && pvr_CheckSeriesEpisode(bookInfo.getSeriesRecKey(), episode.getEpisodeKey()) != 0) {
                        nextDateTime = episodeStartTime;
                        bookInfo.setEpisode(episode.getEpisodeKey());
                        bookInfo.setEventName(episode.getEventName());

                        // minute to HHmm // may change later
                        int bookDuration = episode.getDuration() / 60 * 100 + episode.getDuration() % 60;
                        bookInfo.setDuration(bookDuration);
                        found = true;
                        break;
                    }
                }
            }

            if (found) { // valid episode found
                // book cycle may be BOOK_CYCLE_SERIES_EMPTY so set back to BOOK_CYCLE_SERIES
                bookInfo.setBookCycle(BookInfo.BOOK_CYCLE_SERIES);
            } else  { // no valid episode found
                LocalDateTime expireTime = bookDateTime.plusWeeks(Pvcfg.SERIES_EXPIRE_WEEKS);
                if (localDateTime.isAfter(expireTime)) { // expired
                    nextDateTime = null;
                } else { // not expired
                    // not expired but no valid episode
                    // set book cycle to BOOK_CYCLE_SERIES_EMPTY
                    bookInfo.setBookCycle(BookInfo.BOOK_CYCLE_SERIES_EMPTY);
                }
            }
        }

        return nextDateTime;
    }

    private int get_pesi_day_of_week(DayOfWeek dayOfWeek) {
        int pesiDayOfWeek = 0;
        switch (dayOfWeek) {
            case MONDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_MONDAY;
            }
            case TUESDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_TUESDAY;
            }
            case WEDNESDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_WEDNESDAY;
            }
            case THURSDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_THURSDAY;
            }
            case FRIDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_FRIDAY;
            }
            case SATURDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_SATURDAY;
            }
            case SUNDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_SUNDAY;
            }
        }

        return pesiDayOfWeek;
    }

    /*
    mode = 1 force
    mode = 0 normal
     */
    public void set_timer_next_date_by_cycle(/*int mode,*/ int bookId, BookInfo pbookinfo) {
        BookInfo bookinfo;
        if(pbookinfo != null) {
            bookinfo = pbookinfo;
        }
        else {
            bookinfo = book_info_get(bookId);
        }

        if(bookinfo == null) {
            LogUtils.e("Can't find bookinfo !!!!!! bookId = "+ bookId +" pbookinfo = " + pbookinfo);
            return;
        }

        LogUtils.d("bookinfo in = " + bookinfo.ToString());

        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime bookDateTime;
        try {
            bookDateTime = LocalDateTime.of(
                    bookinfo.getYear(),
                    bookinfo.getMonth(),
                    bookinfo.getDate(),
                    bookinfo.getStartTime() / 100,
                    bookinfo.getStartTime() % 100);
        } catch (DateTimeException exception) {
            LogUtils.e(exception.getMessage());
            book_info_delete(bookinfo.getBookId()); // delete invalid book
            return;
        }

//        // according to pesidtv preserv.c
//        // return if 1. normal mode 2. recording time has not arrived yet
//        if (mode == 0 && !localDateTime.isAfter(bookDateTime)) {
//            LogUtils.d("Normal mode and recording time has not arrived yet, skip schedule next timer date");
//            return;
//        }

        int cycle = bookinfo.getBookCycle();
        LocalDateTime nextBookDateTime = null;
        switch (cycle) {
            case BookInfo.BOOK_CYCLE_ONETIME -> {
                nextBookDateTime = get_next_date_onetime(localDateTime, bookDateTime);
            }
            case BookInfo.BOOK_CYCLE_DAILY -> {
                nextBookDateTime = get_next_date_daily(localDateTime, bookDateTime);
            }
            case BookInfo.BOOK_CYCLE_WEEKLY -> {
                nextBookDateTime = get_next_date_weekly(localDateTime, bookDateTime);
            }
            case BookInfo.BOOK_CYCLE_WEEKEND -> {
                nextBookDateTime = get_next_date_weekend(localDateTime, bookDateTime);
            }
            case BookInfo.BOOK_CYCLE_WEEKDAYS -> {
                nextBookDateTime = get_next_date_weekdays(localDateTime, bookDateTime);
            }
            case BookInfo.BOOK_CYCLE_MONTHLY -> {
                nextBookDateTime = get_next_date_monthly(localDateTime, bookDateTime);
            }
            case BookInfo.BOOK_CYCLE_SERIES, BookInfo.BOOK_CYCLE_SERIES_EMPTY -> {
                nextBookDateTime = get_next_date_series(localDateTime, bookDateTime, bookinfo);
            }
            default -> {
                LogUtils.w("Unknown book cycle...");
            }
        }

        LogUtils.d("now = " + localDateTime
                + " preBookDateTime = " + bookDateTime
                + " nextBookDateTime = " + nextBookDateTime);
        if (nextBookDateTime == null) {
            book_info_delete(bookinfo.getBookId()); // del bookinfo if no next date
        }
        else {
            bookinfo.setDate(nextBookDateTime.getDayOfMonth());
            bookinfo.setMonth(nextBookDateTime.getMonthValue());
            bookinfo.setYear(nextBookDateTime.getYear());
            bookinfo.setWeek(get_pesi_day_of_week(nextBookDateTime.getDayOfWeek()));
            bookinfo.setStartTime(nextBookDateTime.getHour()*100 + nextBookDateTime.getMinute());

            book_info_update(bookinfo); // update bookinfo
            LogUtils.d("bookinfo out = " + bookinfo.ToString());
        }
    }

    public void saveGposKeyValue(String key, String value){
        gDtvFramework.saveGposKeyValue(key, value);
    }
    public void saveGposKeyValue(String key, int value){
        gDtvFramework.saveGposKeyValue(key, value);
    }
    public void saveGposKeyValue(String key, long value){
        gDtvFramework.saveGposKeyValue(key, value);
    }

    public void backupDatabase(boolean force) {
        gDtvFramework.backupDatabase(force);
    }

    public void AvCmdMiddle_WaitAVPlayReady() {
        gDtvFramework.WaitAVPlayReady();
    }

    public int AvCmdMiddle_getPlay_index() {
        return gDtvFramework.getPlay_index();
    }

    public void category_update_to_fav(List<ProgramInfo> ProgramInfoList, List<MusicInfo> musicInfoList) {
        gDtvFramework.category_update_to_fav(ProgramInfoList,musicInfoList);
    }

    public void clear_cas_data() {
        gDtvFramework.clear_cas_data();
    }

    public CasData get_cas_data() {
        return gDtvFramework.get_cas_data();
    }

    public void build_epg_event_map(Context context) {
        gDtvFramework.build_epg_event_map(context);
    }

    public void build_network_time() {
        gDtvFramework.build_network_time();
    }

    public void delete_maildata_of_db(int mail_id) {
        gDtvFramework.delete_maildata_of_db(mail_id);
    }

    public void save_maildata_to_db(MailData mailData) {
        gDtvFramework.save_maildata_to_db(mailData);
    }

    public MailData get_mail_from_db(int id) {
        return gDtvFramework.get_mail_from_db(id);
    }

    public List<MailData> get_mail_list_from_db() {
        return gDtvFramework.get_mail_list_from_db();
    }

    public Intent get_book_info_intent(BookInfo bookInfo) {
        Bundle bundle = new Bundle();
        bundle.putInt(BookInfo.BOOK_ID, bookInfo.getBookId());
        bundle.putLong(BookInfo.CHANNEL_ID, bookInfo.getChannelId());
        bundle.putString(BookInfo.CHANNEL_NUM, bookInfo.getChannelNum());
        bundle.putInt(BookInfo.GROUP_TYPE, bookInfo.getGroupType());
        bundle.putString(BookInfo.EVENT_NAME, bookInfo.getEventName());
        bundle.putInt(BookInfo.BOOK_TYPE, bookInfo.getBookType());
        bundle.putInt(BookInfo.BOOK_CYCLE, bookInfo.getBookCycle());
        bundle.putInt(BookInfo.YEAR, bookInfo.getYear());
        bundle.putInt(BookInfo.MONTH, bookInfo.getMonth());
        bundle.putInt(BookInfo.DATE, bookInfo.getDate());
        bundle.putInt(BookInfo.WEEK, bookInfo.getWeek());
        bundle.putInt(BookInfo.START_TIME, bookInfo.getStartTime());
        bundle.putLong(BookInfo.START_TIME_MS, bookInfo.getStartTimeMs());
        bundle.putInt(BookInfo.DURATION, bookInfo.getDuration());
        bundle.putInt(BookInfo.DURATION_MS, bookInfo.getDurationMs());
        bundle.putInt(BookInfo.ENABLE, bookInfo.getEnable());
        bundle.putBoolean(BookInfo.IS_SERIES, bookInfo.isSeries());
        bundle.putInt(BookInfo.EPISODE, bookInfo.getEpisode());
        bundle.putByteArray(BookInfo.SERIES_REC_KEY, bookInfo.getSeriesRecKey());
        bundle.putInt(BookInfo.EPG_EVENT_ID, bookInfo.getEpgEventId());
        bundle.putBoolean(BookInfo.ALLOW_AUTO_SELECT, bookInfo.getAutoSelect());
        bundle.putBoolean(BookInfo.IS_4K, bookInfo.is4K());

        Intent intent = new Intent();

        if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD)
            intent.setAction(PrimeTimerReceiver.ACTION_TIMER_RECORD);
        else if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_POWER_ON)
            intent.setAction(PrimeTimerReceiver.ACTION_TIMER_POWER_ON);
        else if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_CHANGE_CHANNEL)
            intent.setAction(PrimeTimerReceiver.ACTION_TIMER_CHANGE_CHANNEL);

        intent.putExtras(bundle);
        return intent;
    }

    public String get_book_cycle_string(BookInfo bookInfo, AppCompatActivity activity) {
        return switch (bookInfo.getBookCycle()) {
            case BookInfo.BOOK_CYCLE_ONETIME -> activity.getString(R.string.book_cycle_one_time);
            case BookInfo.BOOK_CYCLE_DAILY -> activity.getString(R.string.book_cycle_daily);
            case BookInfo.BOOK_CYCLE_WEEKLY -> get_week_day(bookInfo,activity);
            case BookInfo.BOOK_CYCLE_WEEKEND -> activity.getString(R.string.book_cycle_weekend);
            case BookInfo.BOOK_CYCLE_WEEKDAYS -> activity.getString(R.string.book_cycle_weekdays);
            case BookInfo.BOOK_CYCLE_SERIES, BookInfo.BOOK_CYCLE_SERIES_EMPTY -> "";
            default -> "null";
        };
    }

    public String get_week_day(BookInfo bookInfo, AppCompatActivity activity) {
        return switch (bookInfo.getWeek()) {
            case BookInfo.BOOK_WEEK_DAY_SUNDAY -> activity.getString(R.string.book_week_day_sunday);
            case BookInfo.BOOK_WEEK_DAY_MONDAY -> activity.getString(R.string.book_week_day_monday);
            case BookInfo.BOOK_WEEK_DAY_TUESDAY -> activity.getString(R.string.book_week_day_tuesday);
            case BookInfo.BOOK_WEEK_DAY_WEDNESDAY -> activity.getString(R.string.book_week_day_wednesday);
            case BookInfo.BOOK_WEEK_DAY_THURSDAY -> activity.getString(R.string.book_week_day_thursday);
            case BookInfo.BOOK_WEEK_DAY_FRIDAY -> activity.getString(R.string.book_week_day_friday);
            case BookInfo.BOOK_WEEK_DAY_SATURDAY -> activity.getString(R.string.book_week_day_saturday);
            default -> "null";
        };
    }

    public String get_mail_qrcode_type(Context context, String type) {
        int intValue = Integer.valueOf(type);
        String str = MailInfo.PATH_MEMBER;
        if (intValue == 1) {
            str = MailInfo.PATH_SERVICE;
        } else if (intValue == 2) {
            str = MailInfo.PATH_PAYMENT;
        } else if (intValue != 3 && intValue == 4) {
            str = MailInfo.PATH_WPS;
        }
        return Utils.generate_url(context, str);
    }

    public List<MusicAdScheduleInfo> get_music_ad_schedule_info_list(Context context) {
        String musicAdScheduleInfoList = ACSDataProviderHelper.get_acs_provider_data(context, "ad_profile");
        //String musicAdScheduleInfoList = "[\n" + "    {\n" + "        \"EndDate\": \"2022-01-30T16:00:00.000Z\",\n" + "        \"PlayList\": [\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\",\n" + "                \"endDate\": \"2022-01-30T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676876666059.jpg\",\n" + "                \"sequence\": 0,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2021-11-30T16:00:00.000Z\",\n" + "                \"title\": \"數位影音立即購QR Code\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\"\n" + "            },\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\",\n" + "                \"endDate\": \"2022-01-30T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676883894708.jpg\",\n" + "                \"sequence\": 1,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2021-11-30T16:00:00.000Z\",\n" + "                \"title\": \"線上影音加購超優惠\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\"\n" + "            }\n" + "        ],\n" + "        \"StartDate\": \"2021-11-30T16:00:00.000Z\"\n" + "    },\n" + "    {\n" + "        \"EndDate\": \"2022-03-30T16:00:00.000Z\",\n" + "        \"PlayList\": [\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\",\n" + "                \"endDate\": \"2022-03-30T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676876666059.jpg\",\n" + "                \"sequence\": 0,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2022-01-31T16:00:00.000Z\",\n" + "                \"title\": \"數位影音立即購QR Code\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\"\n" + "            },\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\",\n" + "                \"endDate\": \"2022-03-30T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676883894708.jpg\",\n" + "                \"sequence\": 1,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2022-01-31T16:00:00.000Z\",\n" + "                \"title\": \"線上影音加購超優惠\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\"\n" + "            }\n" + "        ],\n" + "        \"StartDate\": \"2022-01-31T16:00:00.000Z\"\n" + "    },\n" + "    {\n" + "        \"EndDate\": \"2022-06-29T16:00:00.000Z\",\n" + "        \"PlayList\": [\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\",\n" + "                \"endDate\": \"2022-06-29T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676876666059.jpg\",\n" + "                \"sequence\": 0,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2022-04-30T16:00:00.000Z\",\n" + "                \"title\": \"數位影音立即購QR Code\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\"\n" + "            },\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\",\n" + "                \"endDate\": \"2022-06-29T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676883894708.jpg\",\n" + "                \"sequence\": 1,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2022-04-30T16:00:00.000Z\",\n" + "                \"title\": \"線上影音加購超優惠\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\"\n" + "            }\n" + "        ],\n" + "        \"StartDate\": \"2022-04-30T16:00:00.000Z\"\n" + "    },\n" + "    {\n" + "        \"EndDate\": \"2022-09-29T16:00:00.000Z\",\n" + "        \"PlayList\": [\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\",\n" + "                \"endDate\": \"2022-09-29T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676876666059.jpg\",\n" + "                \"sequence\": 0,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2022-06-30T16:00:00.000Z\",\n" + "                \"title\": \"數位影音立即購QR Code\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\"\n" + "            },\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\",\n" + "                \"endDate\": \"2022-09-29T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676883894708.jpg\",\n" + "                \"sequence\": 1,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2022-06-30T16:00:00.000Z\",\n" + "                \"title\": \"線上影音加購超優惠\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\"\n" + "            }\n" + "        ],\n" + "        \"StartDate\": \"2022-06-30T16:00:00.000Z\"\n" + "    },\n" + "    {\n" + "        \"EndDate\": \"2024-10-30T16:00:00.000Z\",\n" + "        \"PlayList\": [\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\",\n" + "                \"endDate\": \"2024-10-30T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676876666059.jpg\",\n" + "                \"sequence\": 0,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2024-02-14T16:00:00.000Z\",\n" + "                \"title\": \"數位影音立即購QR Code\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/package/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/p_3842\"\n" + "            },\n" + "            {\n" + "                \"adUrl\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\",\n" + "                \"endDate\": \"2024-10-30T16:00:00.000Z\",\n" + "                \"md5_checksum\": \"\",\n" + "                \"mediaUrl\": \"https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676883894708.jpg\",\n" + "                \"sequence\": 1,\n" + "                \"sourceType\": \"1007\",\n" + "                \"startDate\": \"2024-02-14T16:00:00.000Z\",\n" + "                \"title\": \"線上影音加購超優惠\",\n" + "                \"type\": \"1007\",\n" + "                \"typeName\": \"QRcode\",\n" + "                \"url\": \"https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo\"\n" + "            }\n" + "        ],\n" + "        \"StartDate\": \"2024-02-14T16:00:00.000Z\"\n" + "    }\n" + "]";
        //Log.d(TAG, "get_current_category: musicAdScheduleInfoList = " + musicAdScheduleInfoList);

        if (musicAdScheduleInfoList == null)
            return new ArrayList<>();

        return JsonParser.parse_music_ad_schedule_info(musicAdScheduleInfoList);
    }

    public List<MusicInfo> get_current_category(Context context) {
        String musicList = ACSDataProviderHelper.get_acs_provider_data(context, "music_category");
        //String musicList = "[\n" + "        {\n" + "            \"name\": \"華語音樂\",\n" + "            \"icon\": \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330858497.png\",\n" + "            \"sort\": 1,\n" + "            \"servicelists\": [\n" + "                \"1534\",\n" + "                \"1535\",\n" + "                \"1536\",\n" + "                \"1543\"\n" + "            ]\n" + "        },\n" + "        {\n" + "            \"name\": \"西洋音樂\",\n" + "            \"icon\": \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330890167.png\",\n" + "            \"sort\": 2,\n" + "            \"servicelists\": [\n" + "                \"1514\",\n" + "                \"1515\",\n" + "                \"1516\",\n" + "                \"1517\",\n" + "                \"1518\",\n" + "                \"1519\",\n" + "                \"1521\"\n" + "            ]\n" + "        },\n" + "        {\n" + "            \"name\": \"東洋音樂\",\n" + "            \"icon\": \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330824653.png\",\n" + "            \"sort\": 3,\n" + "            \"servicelists\": [\n" + "                \"1525\",\n" + "                \"1526\",\n" + "                \"1527\"\n" + "            ]\n" + "        },\n" + "        {\n" + "            \"name\": \"沙發音樂\",\n" + "            \"icon\": \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330938131.png\",\n" + "            \"sort\": 4,\n" + "            \"servicelists\": [\n" + "                \"1522\",\n" + "                \"1523\",\n" + "                \"1524\",\n" + "                \"1528\",\n" + "                \"1531\",\n" + "                \"1532\"\n" + "            ]\n" + "        },\n" + "        {\n" + "            \"name\": \"古典音樂\",\n" + "            \"icon\": \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330777070.png\",\n" + "            \"sort\": 5,\n" + "            \"servicelists\": [\n" + "                \"1529\",\n" + "                \"1530\",\n" + "                \"1537\"\n" + "            ]\n" + "        },\n" + "        {\n" + "            \"name\": \"其他音樂\",\n" + "            \"icon\": \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586331013427.png\",\n" + "            \"sort\": 6,\n" + "            \"servicelists\": [\n" + "                \"1520\",\n" + "                \"1533\",\n" + "                \"1538\",\n" + "                \"1539\",\n" + "                \"1540\",\n" + "                \"1541\",\n" + "                \"1542\"\n" + "            ]\n" + "        }\n" + "    ]";
        Log.d(TAG, "get_current_category: musicList = " + musicList);

        if (musicList == null)
            return new ArrayList<>();

        return JsonParser.parse_music_info(musicList);
    }

    public void setACSMusicList(Context context) {
        String musicList = ACSDataProviderHelper.get_acs_provider_data(context, "music_category");
        gDtvFramework.setACSMusicList(musicList);
    }

    public String getLangString(Context context,String langCode) {
        String str;
        if(langCode.equalsIgnoreCase("chi"))
            return context.getString(R.string.dialog_lang_ch);
        else if(langCode.equalsIgnoreCase("eng"))
            return context.getString(R.string.dialog_lang_en);
        else
            return langCode;
    }

    public void setSetVisibleCompleted(boolean isVisibleCompleted) {
        gDtvFramework.setSetVisibleCompleted(isVisibleCompleted);
    }

    public int tuner_init() {
        return gDtvFramework.init_tuner();
    }
}
