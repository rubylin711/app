package com.prime.launcher.ServiceInterface;

import android.content.Context;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.prime.datastructure.ServiceDefine.PrimeDtvInterface;
import com.prime.datastructure.sysdata.CasData;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.MailData;
import com.prime.datastructure.sysdata.MusicInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.FavInfo;
import com.prime.datastructure.sysdata.PvrInfo;
import com.prime.datastructure.sysdata.PvrRecFileInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.datastructure.sysdata.PvrRecStartParam;
import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.SeriesInfo;
import com.prime.datastructure.sysdata.SubtitleInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.sysdata.AudioInfo;
import com.prime.datastructure.utils.TVScanParams;
import com.prime.datastructure.utils.TVTunerParams;
import com.prime.launcher.PrimeDtv;
import com.prime.dtv.ServiceInterface;

import java.util.Date;
import java.util.List;

public class PesiDtvFrameworkInterface {
    public final static String TAG = "PesiDtvFrameworkInterface";

    private Context mContext = null;
    private static PesiDtvFrameworkInterface mPesiDtvFramework = null;
    private ServiceInterface mServiceInterface = null;

    public final static PesiDtvFrameworkInterface getInstance(PrimeDtvInterface.DTVCallback callback, Context context)
    {
        Log.e(TAG, "getInstance");
        if ( mPesiDtvFramework == null ) {
            mPesiDtvFramework = new PesiDtvFrameworkInterface(callback,context);
        }

        return mPesiDtvFramework ;
    }

    public static PesiDtvFrameworkInterface getPesiDtvFrameworkInterface() {
        return mPesiDtvFramework;
    }

    public PesiDtvFrameworkInterface(PrimeDtvInterface.DTVCallback callback, Context context) {
        mContext = context;
        mServiceInterface = ServiceInterface.getServiceInterfaceInstance(callback,context);
    }

    public void registerCallback(PrimeDtv.LauncherCallback callback) {

    }

    public int getPlay_index() {
        return mServiceInterface.get_prime_dtv().getPlay_index();
    }

    public void WaitAVPlayReady() {
        mServiceInterface.get_prime_dtv().WaitAVPlayReady();
    }

    public void hdd_monitor_start(long alert_sizeMB) {
        mServiceInterface.get_prime_dtv().hdd_monitor_start(alert_sizeMB);
    }

    public void hdd_monitor_stop() {
        mServiceInterface.get_prime_dtv().hdd_monitor_stop();
    }

    public int set_module_type(String type) {
        return mServiceInterface.get_prime_dtv().set_module_type(type);
    }

    public void UpdatePMT(long channel_id) {
        mServiceInterface.get_prime_dtv().UpdatePMT(channel_id);
    }

    public void StopDVBSubtitle(){
        mServiceInterface.get_prime_dtv().StopDVBSubtitle();
    }

    public void stopMonitorTable(long channel_id, int tuner_id) {
        mServiceInterface.get_prime_dtv().stopMonitorTable(channel_id,tuner_id);
    }

    public void startMonitorTable(long channel_id, int isFcc, int tuner_id){
        mServiceInterface.get_prime_dtv().startMonitorTable(channel_id,isFcc,tuner_id);
    }

    public void stopAllEitTableOnCurrentTp(int tuner_id){
        mServiceInterface.get_prime_dtv().stopAllEitTableOnCurrentTp(tuner_id);
    }

    public void ResetCheckAD() {
        mServiceInterface.get_prime_dtv().ResetCheckAD();
    }

    public void backupDatabase(boolean force) {
        mServiceInterface.get_prime_dtv().backupDatabase(force);
    }

    public GposInfo gpos_info_get() {
        return mServiceInterface.get_prime_dtv().gpos_info_get();
    }

    public void gpos_info_update(GposInfo gPos) {
        mServiceInterface.get_prime_dtv().gpos_info_update(gPos);
    }

    public void gpos_info_update_by_key_string(String key, String value) {
        mServiceInterface.get_prime_dtv().gpos_info_update_by_key_string(key,value);
    }

    public void gpos_info_update_by_key_string(String key, int value) {
        mServiceInterface.get_prime_dtv().gpos_info_update_by_key_string(key,value);
    }

    public void saveGposKeyValue(String key, String value) {
        mServiceInterface.get_prime_dtv().saveGposKeyValue(key,value);
    }

    public void saveGposKeyValue(String key,int value) {
        mServiceInterface.get_prime_dtv().saveGposKeyValue(key,value);
    }

    public void saveGposKeyValue(String key,long value) {
        mServiceInterface.get_prime_dtv().saveGposKeyValue(key,value);
    }


    public List<ProgramInfo> get_program_info_list(int type, int pos, int num) {
        return mServiceInterface.get_prime_dtv().get_program_info_list(type,pos,num);
    }

    public ProgramInfo get_program_by_channel_id(long channelId) {
        return mServiceInterface.get_prime_dtv().get_program_by_channel_id(channelId);
    }

    public ProgramInfo get_program_by_ch_num(int chnum, int type) {
        return mServiceInterface.get_prime_dtv().get_program_by_ch_num(chnum,type);
    }

    public ProgramInfo get_program_by_lcn(int lcn, int type) {
        return mServiceInterface.get_prime_dtv().get_program_by_lcn(lcn,type);
    }

    public ProgramInfo get_program_by_service_id(int service_id) {
        return mServiceInterface.get_prime_dtv().get_program_by_service_id(service_id);
    }

    public ProgramInfo get_program_by_service_id_transport_stream_id(int service_id, int ts_id) {
        return mServiceInterface.get_prime_dtv().get_program_by_service_id_transport_stream_id(service_id,ts_id);
    }

    public int update_program_info(ProgramInfo pProgram) {
        return mServiceInterface.get_prime_dtv().update_program_info(pProgram);
    }

    public int save_table(EnTableType option) {
        return mServiceInterface.get_prime_dtv().save_table(option);
    }

    public int set_default_open_channel(long channelId, int groupType) {
        return mServiceInterface.get_prime_dtv().set_default_open_channel(channelId,groupType);
    }

    public void update_pvr_skip_list(int groupType, int IncludePVRSkipFlag, int tpId, List<Integer> pvrTpList) {
        mServiceInterface.get_prime_dtv().update_pvr_skip_list(groupType,IncludePVRSkipFlag,tpId,pvrTpList);
    }


    public List<FavInfo> fav_info_get_list(int favMode) {
        return mServiceInterface.get_prime_dtv().fav_info_get_list(favMode);
    }

    public FavInfo fav_info_get(int favMode, int index) {
        return mServiceInterface.get_prime_dtv().fav_info_get(favMode,index);
    }

    public int fav_info_update_list(int favMode, List<FavInfo> favInfoList) {
        return mServiceInterface.get_prime_dtv().fav_info_update_list(favMode,favInfoList);
    }

    public int fav_info_delete(int favMode, long channelId) {
        return mServiceInterface.get_prime_dtv().fav_info_delete(favMode,channelId);
    }

    public int fav_info_delete_all(int favMode) {
        return mServiceInterface.get_prime_dtv().fav_info_delete_all(favMode);
    }

    public int fav_info_save_db(FavInfo favInfo) {
        return mServiceInterface.get_prime_dtv().fav_info_save_db(favInfo);
    }

    public String fav_group_name_get(int favMode) {
        return mServiceInterface.get_prime_dtv().fav_group_name_get(favMode);
    }

    public int fav_group_name_update(int favMode, String name) {
        return mServiceInterface.get_prime_dtv().fav_group_name_update(favMode,name);
    }


    public List<BookInfo> book_info_get_list() {
        return mServiceInterface.get_prime_dtv().book_info_get_list();
    }

    public BookInfo book_info_get(int bookId) {
        return mServiceInterface.get_prime_dtv().book_info_get(bookId);
    }

    public int book_info_add(BookInfo bookInfo) {
        return mServiceInterface.get_prime_dtv().book_info_add(bookInfo);
    }

    public int book_info_update(BookInfo bookInfo) {
        return mServiceInterface.get_prime_dtv().book_info_update(bookInfo);
    }

    public int book_info_update_list(List<BookInfo> bookList) {
        return mServiceInterface.get_prime_dtv().book_info_update_list(bookList);
    }

    public int book_info_delete(int bookId) {
        return mServiceInterface.get_prime_dtv().book_info_delete(bookId);
    }

    public int book_info_delete_all() {
        return mServiceInterface.get_prime_dtv().book_info_delete_all();
    }

    public BookInfo book_info_get_coming_book() {
        return mServiceInterface.get_prime_dtv().book_info_get_coming_book();
    }

    public List<BookInfo> book_info_find_conflict_records(BookInfo bookInfo) {
        return mServiceInterface.get_prime_dtv().book_info_find_conflict_records(bookInfo);
    }

    public void book_info_save(BookInfo bookInfo) {
        mServiceInterface.get_prime_dtv().book_info_save(bookInfo);
    }

    public int start_epg(long channelID) {
        return mServiceInterface.get_prime_dtv().start_epg(channelID);
    }

    public EPGEvent get_present_event(long channelId) {
        return mServiceInterface.get_prime_dtv().get_present_event(channelId);
    }

    public EPGEvent get_follow_event(long channelId) {
        return mServiceInterface.get_prime_dtv().get_follow_event(channelId);
    }

    public EPGEvent get_epg_by_event_id(long channelId, int eventId) {
        return mServiceInterface.get_prime_dtv().get_epg_by_event_id(channelId,eventId);
    }

    public List<EPGEvent> get_epg_events(long channelID, Date startTime, Date endTime, int pos, int reqNum, int addEmpty) {
        return mServiceInterface.get_prime_dtv().get_epg_events(channelID,startTime,endTime,pos,reqNum,addEmpty);
    }

    public void setup_epg_channel() {
        mServiceInterface.get_prime_dtv().setup_epg_channel();
    }

    public String get_short_description(long channelId, int eventId) {
        return mServiceInterface.get_prime_dtv().get_short_description(channelId,eventId);
    }

    public String get_detail_description(long channelId, int eventId) {
        return mServiceInterface.get_prime_dtv().get_detail_description(channelId,eventId);
    }

    public int set_event_lang(String firstLangCode, String secLangCode) {
        return mServiceInterface.get_prime_dtv().set_event_lang(firstLangCode,secLangCode);
    }

    public void start_schedule_eit(int tp_id,int tuner_id) {
        mServiceInterface.get_prime_dtv().start_schedule_eit(tp_id, tuner_id);
    }

    public void stop_schedule_eit() {
        mServiceInterface.get_prime_dtv().stop_schedule_eit();
    }

    public int add_series(long channelId, byte[] key) {
        return mServiceInterface.get_prime_dtv().add_series(channelId,key);
    }

    public int delete_series(long channelId, byte[] key) {
        return mServiceInterface.get_prime_dtv().delete_series(channelId,key);
    }

    public SeriesInfo.Series get_series(long channelId, byte[] key){
        return mServiceInterface.get_prime_dtv().get_series(channelId,key);
    }

    public int save_series() {
        return mServiceInterface.get_prime_dtv().save_series();
    }

    public void start_scan(TVScanParams sp)  {
        mServiceInterface.get_prime_dtv().start_scan(sp);
    }

    public void stop_scan(boolean store)  {
        mServiceInterface.get_prime_dtv().stop_scan(store);
    }


    public int tuner_lock(TVTunerParams tvTunerParams)  {
        return mServiceInterface.get_prime_dtv().tuner_lock(tvTunerParams);
    }

    public int get_tuner_num() {
        return mServiceInterface.get_prime_dtv().get_tuner_num();
    }

    public int get_tuner_type()  {
        return mServiceInterface.get_prime_dtv().get_tuner_type();
    }

    public int get_signal_strength(int nTunerID) {
        return mServiceInterface.get_prime_dtv().get_signal_strength(nTunerID);
    }

    public int get_signal_quality(int nTunerID) {
        return mServiceInterface.get_prime_dtv().get_signal_quality(nTunerID);
    }

    public int get_signal_snr(int nTunerID) {
        return mServiceInterface.get_prime_dtv().get_signal_snr(nTunerID);
    }

    public int get_signal_ber(int nTunerID) {
        return mServiceInterface.get_prime_dtv().get_signal_ber(nTunerID);
    }

    public boolean get_tuner_status(int tuner_id)  {
        return mServiceInterface.get_prime_dtv().get_tuner_status(tuner_id);
    }

    public int tuner_set_antenna_5v(int tuner_id, int onOff) {
        return mServiceInterface.get_prime_dtv().tuner_set_antenna_5v(tuner_id,onOff);
    }

    public int set_diseqc10_port_info(int nTuerID, int nPort, int n22KSwitch, int nPolarity) {
        return mServiceInterface.get_prime_dtv().set_diseqc10_port_info(nTuerID,nPort,n22KSwitch,nPolarity);
    }

    public int set_diseqc12_move_motor(int nTunerId, int direct, int step) {
        return mServiceInterface.get_prime_dtv().set_diseqc12_move_motor(nTunerId,direct,step);
    }

    public int set_diseqc12_move_motor_stop(int nTunerId) {
        return mServiceInterface.get_prime_dtv().set_diseqc12_move_motor_stop(nTunerId);
    }

    public int reset_diseqc12_position(int nTunerId) {
        return mServiceInterface.get_prime_dtv().reset_diseqc12_position(nTunerId);
    }

    public int set_diseqc_limit_pos(int nTunerId, int limitType) {
        return mServiceInterface.get_prime_dtv().set_diseqc_limit_pos(nTunerId,limitType);
    }


    public List<SatInfo> sat_info_get_list(int tunerType, int pos, int num) {
        return mServiceInterface.get_prime_dtv().sat_info_get_list(tunerType,pos,num);
    }

    public SatInfo sat_info_get(int satId) {
        return mServiceInterface.get_prime_dtv().sat_info_get(satId);
    }

    public int sat_info_add(SatInfo pSat) {
        return mServiceInterface.get_prime_dtv().sat_info_add(pSat);
    }

    public int sat_info_update(SatInfo pSat) {
        return mServiceInterface.get_prime_dtv().sat_info_update(pSat);
    }

    public int sat_info_update_list(List<SatInfo> pSats) {
        return mServiceInterface.get_prime_dtv().sat_info_update_list(pSats);
    }

    public int sat_info_delete(int satId) {
        return mServiceInterface.get_prime_dtv().sat_info_delete(satId);
    }

    public List<TpInfo> tp_info_get_list_by_satId(int tunerType, int satId, int pos, int num) {
        return mServiceInterface.get_prime_dtv().tp_info_get_list_by_satId(tunerType,satId,pos,num);
    }

    public TpInfo tp_info_get(int tp_id) {
        return mServiceInterface.get_prime_dtv().tp_info_get(tp_id);
    }

    public int tp_info_add(TpInfo pTp) {
        return mServiceInterface.get_prime_dtv().tp_info_add(pTp);
    }

    public int tp_info_update(TpInfo pTp) {
        return mServiceInterface.get_prime_dtv().tp_info_update(pTp);
    }

    public int tp_info_update_list(List<TpInfo> pTps) {
        return mServiceInterface.get_prime_dtv().tp_info_update_list(pTps);
    }

    public int tp_info_delete(int tpId) {
        return mServiceInterface.get_prime_dtv().tp_info_delete(tpId);
    }


    public int AvControlPlayByChannelIdFCC(int mode, List<Long> ch_id_list, List<Integer> tuner_id_list, boolean channelBlocked) {
        return mServiceInterface.get_prime_dtv().AvControlPlayByChannelIdFCC(mode, ch_id_list, tuner_id_list, channelBlocked);
    }

    public int av_control_play_by_channel_id(int playId, long channelId, int groupType, int show) {
        return mServiceInterface.get_prime_dtv().av_control_play_by_channel_id(playId,channelId,groupType,show);
    }

    public int av_control_set_fast_change_channel(int tunerId, long chId) {
        return mServiceInterface.get_prime_dtv().av_control_set_fast_change_channel(tunerId, chId);
    }

    public int av_control_clear_fast_change_channel(int tunerId, long chId) {
        return mServiceInterface.get_prime_dtv().av_control_clear_fast_change_channel(tunerId, chId);
    }

    public int av_control_change_audio(int playId, AudioInfo.AudioComponent component) {
        return mServiceInterface.get_prime_dtv().av_control_change_audio(playId, component);
    }

    public AudioInfo av_control_get_audio_list_info(int playId) {
        return mServiceInterface.get_prime_dtv().av_control_get_audio_list_info(playId);
    }

    public int av_control_get_play_status(int playId) {
        return mServiceInterface.get_prime_dtv().av_control_get_play_status(playId);
    }

    public int av_control_set_play_status(int status) {
        return mServiceInterface.get_prime_dtv().av_control_set_play_status(status);
    }

    public SubtitleInfo av_control_get_subtitle_list(int playId) {
        return mServiceInterface.get_prime_dtv().av_control_get_subtitle_list(playId);
    }

    public int av_control_select_subtitle(int playId, SubtitleInfo.SubtitleComponent subtitleComponent) {
        return mServiceInterface.get_prime_dtv().av_control_select_subtitle(playId, subtitleComponent);
    }

    public int av_control_show_subtitle(int playId, boolean enable) {
        return mServiceInterface.get_prime_dtv().av_control_show_subtitle(playId,enable);
    }

    public int av_control_play_stop_all() {
        return mServiceInterface.get_prime_dtv().av_control_play_stop_all();
    }

    public int av_control_play_stop(int tunerId, int mode, int stop_monitor_table) {
        return mServiceInterface.get_prime_dtv().av_control_play_stop(tunerId, mode, stop_monitor_table);
    }

    public void set_surface_view(Context context, SurfaceView surfaceView, int index) {
        mServiceInterface.get_prime_dtv().set_surface_view(context, surfaceView, index);
    }

    public void set_surface_view(Context context, SurfaceView surface ) {
        mServiceInterface.get_prime_dtv().set_surface_view(context, surface);
    }

    public void setSurface(Context context, Surface surface, int index) {
        mServiceInterface.get_prime_dtv().set_surface(context,surface,index);
    }


    public int pvr_init(String usbMountPath) {
        return mServiceInterface.get_prime_dtv().pvr_init(usbMountPath);
    }

    public int pvr_deinit() {
        return mServiceInterface.get_prime_dtv().pvr_deinit();
    }

    public int pvr_RecordStart(PvrRecStartParam startParam, int tunerId) {
        return mServiceInterface.get_prime_dtv().pvr_RecordStart(startParam, tunerId);
    }

    public int pvr_RecordStop(int recId) {
        return mServiceInterface.get_prime_dtv().pvr_RecordStop(recId);
    }

    public void pvr_RecordStopAll() {
        mServiceInterface.get_prime_dtv().pvr_RecordStopAll();
    }

    public int pvr_RecordGetRecTimeByRecId(int recId) {
        return mServiceInterface.get_prime_dtv().pvr_RecordGetRecTimeByRecId(recId);
    }

    public boolean pvr_PlayCheckLastPositionPoint(PvrRecIdx recIdx) {
        return mServiceInterface.get_prime_dtv().pvr_PlayCheckLastPositionPoint(recIdx);
    }

    public int pvr_PlayFileStart(PvrRecIdx recIdx, boolean lastPositionFlag, int tunerId) {
        return mServiceInterface.get_prime_dtv().pvr_PlayFileStart(recIdx, lastPositionFlag, tunerId);
    }

    public int pvr_PlayFileStop(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_PlayFileStop(playId);
    }

    public int pvr_PlayPlay(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_PlayPlay(playId);
    }

    public int pvr_PlayPause(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_PlayPause(playId);
    }

    public int pvr_PlayFastForward(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_PlayFastForward(playId);
    }

    public int pvr_PlayRewind(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_PlayRewind(playId);
    }

    public int pvr_PlaySeek(int playId, PvrInfo.EnSeekMode seekMode, int offsetSec) {
        return mServiceInterface.get_prime_dtv().pvr_PlaySeek(playId, seekMode, offsetSec);
    }

    public int pvr_PlaySetSpeed(int playId, PvrInfo.EnPlaySpeed playSpeed) {
        return mServiceInterface.get_prime_dtv().pvr_PlaySetSpeed(playId, playSpeed);
    }

    public PvrInfo.EnPlaySpeed pvr_PlayGetSpeed(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_PlayGetSpeed(playId);
    }

    public int pvr_PlayChangeAudioTrack(int playId, AudioInfo.AudioComponent audioComponent) {
        return mServiceInterface.get_prime_dtv().pvr_PlayChangeAudioTrack(playId, audioComponent);
    }

    public int pvr_PlayGetCurrentAudioIndex(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_PlayGetCurrentAudioIndex(playId);
    }

    public int pvr_PlayGetPlayTime(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_PlayGetPlayTime(playId);
    }

    public PvrInfo.EnPlayStatus pvr_PlayGetPlayStatus(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_PlayGetPlayStatus(playId);
    }

    public PvrInfo.PlayTimeInfo pvr_PlayGetPlayTimeInfo(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_PlayGetPlayTimeInfo(playId);
    }

    public int pvr_TimeShiftStart(ProgramInfo programInfo, int recordTunerId, int playTunerId) {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftStart(programInfo, recordTunerId, playTunerId);
    }

    public int pvr_TimeShiftStop() {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftStop();
    }

    public int pvr_TimeShiftPlayStart(int tunerId) {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftPlayStart(tunerId);
    }

    public int pvr_TimeShiftPlayPause(int tunerId) {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftPlayPause(tunerId);
    }

    public int pvr_TimeShiftPlayResume(int tunerId) {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftPlayResume(tunerId);
    }

    public int pvr_TimeShiftPlayFastForward(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftPlayFastForward(playId);
    }

    public int pvr_TimeShiftPlayRewind(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftPlayRewind(playId);
    }

    public int pvr_TimeShiftPlaySeek(int playId, PvrInfo.EnSeekMode seekMode, int startPosition) {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftPlaySeek(playId, seekMode, startPosition);
    }

    public int pvr_TimeShiftPlaySetSpeed(int playId, PvrInfo.EnPlaySpeed playSpeed) {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftPlaySetSpeed(playId, playSpeed);
    }

    public PvrInfo.EnPlaySpeed pvr_TimeShiftPlayGetSpeed(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftPlayGetSpeed(playId);
    }

    public int pvr_TimeShiftPlayChangeAudioTrack(int playId, AudioInfo.AudioComponent audioComponent) {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftPlayChangeAudioTrack(playId, audioComponent);
    }

    public int pvr_TimeShiftPlayGetCurrentAudioIndex(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftPlayGetCurrentAudioIndex(playId);
    }

    public PvrInfo.EnPlayStatus pvr_TimeShiftPlayGetStatus(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftPlayGetStatus(playId);
    }

    public PvrInfo.PlayTimeInfo pvr_TimeShiftGetTimeInfo(int playId) {
        return mServiceInterface.get_prime_dtv().pvr_TimeShiftGetTimeInfo(playId);
    }

    public PvrRecFileInfo pvr_GetFileInfoByIndex(PvrRecIdx tableKeyInfo) {
        return mServiceInterface.get_prime_dtv().pvr_GetFileInfoByIndex(tableKeyInfo);
    }

    public int pvr_GetRecCount() {
        return mServiceInterface.get_prime_dtv().pvr_GetRecCount();
    }

    public int pvr_GetSeriesRecCount(int masterIndex) {
        return mServiceInterface.get_prime_dtv().pvr_GetSeriesRecCount(masterIndex);
    }

    public List<PvrRecFileInfo> pvr_GetRecLink(int startIndex, int count) {
        return mServiceInterface.get_prime_dtv().pvr_GetRecLink(startIndex, count);
    }

    public List<PvrRecFileInfo> pvr_GetPlaybackLink(int startIndex, int count) {
        return pvr_GetPlaybackLink(startIndex, count);
    }

    public List<PvrRecFileInfo> pvr_GetSeriesRecLink(PvrRecIdx tableKeyInfo, int count) {
        return mServiceInterface.get_prime_dtv().pvr_GetSeriesRecLink(tableKeyInfo, count);
    }

    public int pvr_DelAllRecs() {
        return mServiceInterface.get_prime_dtv().pvr_DelAllRecs();
    }

    public int pvr_DelSeriesRecs(int masterIndex) {
        return mServiceInterface.get_prime_dtv().pvr_DelSeriesRecs(masterIndex);
    }

    public int pvr_DelRecsByChId(int channelId) {
        return mServiceInterface.get_prime_dtv().pvr_DelRecsByChId(channelId);
    }

    public int pvr_DelOneRec(PvrRecIdx tableKeyInfo) {
        return mServiceInterface.get_prime_dtv().pvr_DelOneRec(tableKeyInfo);
    }

    public int pvr_DelOnePlayback(PvrRecIdx tableKeyInfo) {
        return mServiceInterface.get_prime_dtv().pvr_DelOnePlayback(tableKeyInfo);
    }

    public int pvr_CheckSeriesEpisode(byte[] seriesKey, int episode) {
        return mServiceInterface.get_prime_dtv().pvr_CheckSeriesEpisode(seriesKey, episode);
    }

    public boolean pvr_CheckSeriesComplete(byte[]  seriesKey) {
        return mServiceInterface.get_prime_dtv().pvr_CheckSeriesComplete(seriesKey);
    }

    public boolean pvr_IsIdxInUse(PvrRecIdx pvrRecIdx) {
        return mServiceInterface.get_prime_dtv().pvr_IsIdxInUse(pvrRecIdx);
    }

    public void handleCasRefresh(final int deleteFlag, final int delay, long channel_id) {
        mServiceInterface.get_prime_dtv().handleCasRefresh(deleteFlag, delay, channel_id);
    }

    public void category_update_to_fav(List<ProgramInfo> ProgramInfoList, List<MusicInfo> musicInfoList) {
        mServiceInterface.get_prime_dtv().category_update_to_fav(ProgramInfoList, musicInfoList);
    }

    public void clear_cas_data() {
        mServiceInterface.get_prime_dtv().clear_cas_data();
    }

    public CasData get_cas_data() {
        return mServiceInterface.get_prime_dtv().get_cas_data();
    }

    public void build_epg_event_map(Context context) {
        mServiceInterface.get_prime_dtv().build_epg_event_map(context);
    }

    public void build_network_time() {
        mServiceInterface.get_prime_dtv().build_network_time();
    }

    public void delete_maildata_of_db(int mail_id) {
        mServiceInterface.get_prime_dtv().delete_maildata_of_db(mail_id);
    }

    public void save_maildata_to_db(MailData mailData) {
        mServiceInterface.get_prime_dtv().save_maildata_to_db(mailData);
    }

    public MailData get_mail_from_db(int id) {
        return mServiceInterface.get_prime_dtv().get_mail_from_db(id);
    }

    public List<MailData> get_mail_list_from_db() {
        return mServiceInterface.get_prime_dtv().get_mail_list_from_db();
    }

    public List<FavGroup> fav_group_get_list() {
        return mServiceInterface.get_prime_dtv().fav_group_get_list();
    }

    public void setACSMusicList(String musicList) {
        mServiceInterface.get_prime_dtv().setACSMusicList(musicList);
    }

    public void setSetVisibleCompleted(boolean isVisibleCompleted) {
        mServiceInterface.get_prime_dtv().setSetVisibleCompleted(isVisibleCompleted);
    }

    public int init_tuner() {
        return mServiceInterface.get_prime_dtv().init_tuner();
    }
}
