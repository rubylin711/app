package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.prime.dmg.launcher.Home.BlockChannel.BlockedChannel;
import com.prime.dtv.Interface.BaseManager;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.Player.AvCmdMiddle;
import com.prime.dtv.sysdata.AudioInfo;
import com.prime.dtv.sysdata.EnAudioTrackMode;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.SubtitleInfo;
import com.prime.dtv.sysdata.TeletextInfo;
import com.prime.dtv.utils.LogUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvCmdManager extends BaseManager {
    private static final String TAG = "AvCmdManager" ;
    private Surface mSurface = null;
    private AvCmdMiddle mAvCmdMiddle = null;
    private DataManager mDataManager = null;
    public AvCmdManager(Context context, Handler handler) {
        super(context, TAG, handler, AvCmdManager.class);
        mDataManager = DataManager.getDataManager(context);
        //mAvCmdMiddle = new AvCmdMiddle(getApplicationContext(),getPesiDtvFrameworkInterfaceCallback());
        mAvCmdMiddle = AvCmdMiddle.get_instance(getApplicationContext(), getPesiDtvFrameworkInterfaceCallback());
    }

    public void setSurface(Surface surface) {
        mSurface = surface;
        mAvCmdMiddle.setSurface(surface);
    }

    public void setSurfaceToPlayer(int index, Surface surface) {
        mAvCmdMiddle.setSurfaceToPlayer(index,surface);
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        LogUtils.d("setSurfaceHolder "+ surfaceHolder);
        mAvCmdMiddle.setSurfaceHolder(surfaceHolder);
    }
    public void setSurfaceHolder(SurfaceHolder surfaceHolder, int index) {
        LogUtils.d("AvCmd setSurfaceHolder["+index+"] "+ surfaceHolder);
        mAvCmdMiddle.setSurfaceHolder(surfaceHolder, index);
    }
/*
    public AvCmdMiddle.Mpeg_ProgLock_Status Mpegapi_Check_AVLock(ProgramInfo programInfo, int CheckUserLock){
        int Parental_rating = 0;
        int bShow = 1;
        AvCmdMiddle.Mpeg_ProgLock_Status status = new AvCmdMiddle.Mpeg_ProgLock_Status();
        if(programInfo != null){
            Log.d(TAG,"should implement Pesi_EPG_GetParentRate function");
            //Pesi_EPG_GetParentRate(qs_channel_id(pservice), &Parental_rating);
            if(CheckUserLock == 1&& programInfo.getLock() == 1){
                bShow = 0;
                status.LockType = AvCmdMiddle.Mpeg_ProgLock_Status.MPEG_USER_LOCK;
                status.ChannelID = programInfo.getChannelId();
                status.ParentRating = Parental_rating;
            }
            else
            {
                //MPEGAPI_PRINTD("[%d] [%d] [%d]\n", mpeg_get_accept_rating(), qgpos_ParentalRating(), Parental_rating);
                if((mAvCmdMiddle.mpeg_get_accept_rating() > 0 && mAvCmdMiddle.mpeg_get_accept_rating()>= Parental_rating)){
                    status.LockType = AvCmdMiddle.Mpeg_ProgLock_Status.MPEG_NO_LOCK;
                    status.ChannelID = programInfo.getChannelId();
                    status.ParentRating = Parental_rating;
                }
                else{
                    //No EIT DATA or UI Set Disable
                    if(Parental_rating == 0 || mDataManager.mGposInfo.getParentalRate() == 0){
                        status.LockType = AvCmdMiddle.Mpeg_ProgLock_Status.MPEG_NO_LOCK;
                        status.ChannelID = programInfo.getChannelId();
                        status.ParentRating = Parental_rating;
                    }
                    else if(mDataManager.mGposInfo.getParentalRate() == 99 ||
                            Parental_rating >= mDataManager.mGposInfo.getParentalRate())
                    {
                        bShow = 0;
                        status.LockType = AvCmdMiddle.Mpeg_ProgLock_Status.MPEG_PARENT_LOCK;
                        status.ChannelID = programInfo.getChannelId();
                        status.ParentRating = Parental_rating;
                    }
                    else
                    {
                        status.LockType = AvCmdMiddle.Mpeg_ProgLock_Status.MPEG_NO_LOCK;
                        status.ChannelID = programInfo.getChannelId();
                        status.ParentRating = Parental_rating;

                    }
                }
            }
        }
        //MPEGAPI_PRINTD("LockType [%d]\n", pProglock->LockType);
        //MPEGAPI_PRINTD("ChannelID [%x]\n", pProglock->ChannelID);
        //MPEGAPI_PRINTD("Parental_rating [%d]\n", pProglock->ParentRating);
        status.bShow = bShow;
        return status;
    }

 */
    /*
    AvControl
     */

    public int AvControlPlayByChannelId(int playId, long channelId, int groupType, int show) {
        Log.d(TAG,"[AvControlPlayByChannelId]");
        Message msg = Message.obtain();
        AvCmdMiddle.pesi_avplay avplay = new AvCmdMiddle.pesi_avplay();
        ProgramInfo programInfo = mDataManager.get_service_by_channelId(channelId);
//        AvCmdMiddle.Mpeg_ProgLock_Status pProglockStatus;
        int tmpShow=0;
        if(playId != -1) {
            programInfo.setTunerId(playId);
            mDataManager.updateProgramInfo(programInfo);
        }
        Log.d(TAG,"ChannelID ["+channelId+"]"+" TunerId = ["+programInfo.getTunerId()+"]");
        if(programInfo != null){
            Log.d(TAG,"pservice ["+programInfo.getDisplayName()+"]");

            tmpShow = BlockedChannel.get_instance(null,null).isLock()  ? 0 : 1;

            if(show == 1){
                show = tmpShow;

            }

            Log.d(TAG,"tmpShow ["+tmpShow+"]" + "show ["+show+"]");
            if(show > 0)
                avplay.mode  = AvCmdMiddle.PESI_PLAY_AV;
            else
                avplay.mode  = AvCmdMiddle.PESI_STOP_AV;

            avplay.ChannelID = channelId;
            avplay.TunerId = programInfo.getTunerId();//qs_tuner_id(pservice);
            msg.what = avplay.mode;
            msg.obj = avplay;

            if(Pvcfg.isAVPlayRunOnUIThread()){
                mAvCmdMiddle.playAv(avplay.TunerId ,avplay.ChannelID);
            }else {
                DoCommand(msg);
            }
            mDataManager.set_cur_channel(programInfo.getType(),programInfo.getChannelId());

            mDataManager.DataManagerSaveGposData(GposInfo.GPOS_CUR_CHANNELID, programInfo.getChannelId());

            mAvCmdMiddle.openVideoCountCheck(getHandlerThreadHandler());
        }
        return 0;
    }

    public int AvControlPrePlayStop() {
        Log.d(TAG,"AvControlPrePlayStop dose not implement !!!!");
        return 0;
    }

    public int AvControlPlayStop(int tunerId,int mode, int stop_monitor_table) {
        Log.d(TAG,"[AvControlPlayStop]");
        Message msg = Message.obtain();
        AvCmdMiddle.pesi_avplay avplay = new AvCmdMiddle.pesi_avplay();
        avplay.mode = AvCmdMiddle.PESI_STOP_AV;
        avplay.stop_mode = mode;
        if(stop_monitor_table == 1)
            avplay.stop_monitor_table = 1;
        else
            avplay.stop_monitor_table = 0;
        avplay.playid = 0;
        avplay.ChannelID =0;
        avplay.TunerId = tunerId;
        msg.what = avplay.mode;
        msg.obj = avplay;
        if(Pvcfg.isAVPlayRunOnUIThread()){
            mAvCmdMiddle.stopAv(tunerId,mode,stop_monitor_table);
        }else {
            DoCommand(msg);
        }

        mAvCmdMiddle.closeVideoCountCheck(getHandlerThreadHandler());
        return 0;
    }
    public int AvControlPlayStopAll() {
        Log.d(TAG,"[AvControlPlayStopAll]");
        Message msg = Message.obtain();
        AvCmdMiddle.pesi_avplay avplay = new AvCmdMiddle.pesi_avplay();
        avplay.mode  = AvCmdMiddle.PESI_STOP_AV_ALL;
        avplay.playid = 0;
        avplay.ChannelID =0;
        avplay.TunerId = 0;
        msg.what = avplay.mode;
        msg.obj = avplay;
        if(Pvcfg.isAVPlayRunOnUIThread()){
            mAvCmdMiddle.stopAV();
        }else {
            DoCommand(msg);
        }

        mAvCmdMiddle.closeVideoCountCheck(getHandlerThreadHandler());
        return 0;
    }
    public int AvControlPlayByChannelIdFCC(int mode, List<Long> ch_id_list, List<Integer> tuner_id_list, boolean channelBlocked){
        Message msg = Message.obtain();
        msg.what = AvCmdMiddle.PESI_PLAY_AV_NEW;
        AvCmdMiddle.pesi_avplay_new avplay = new AvCmdMiddle.pesi_avplay_new();
        avplay.mode = mode;
        avplay.channelBlocked = channelBlocked;
        //avplay.avplay_info = avplay_info;
        avplay.channelIdList.addAll(ch_id_list);
        avplay.tunerIdList.addAll(tuner_id_list);
        msg.obj = avplay;

        if(Pvcfg.isAVPlayRunOnUIThread()){
            mAvCmdMiddle.AvPlayNew(avplay);
        }else {
            CleanCommand(msg);
            DoCommand(msg);
        }
        return 0;
    }

    public int closeVideoCountCheck(){
        mAvCmdMiddle.closeVideoCountCheck(getHandlerThreadHandler());
        return 0;
    }
    public int AvControlChangeRatioConversion(int playId, int ratio, int conversion) {
        Log.d(TAG,"AvControlChangeRatioConversion dose not implement !!!!");
        return 0;
    }

    public int AvControlSetFastChangeChannel(int tunerId, long channelId) {
        Log.d(TAG,"[AvControlSetFastChangeChannel] tunerId = "+tunerId+" channelId = "+channelId);
        Message msg = Message.obtain();
        msg.what = AvCmdMiddle.PESI_SET_FCC;
        msg.arg1 = tunerId;
        msg.arg2 = (int)channelId;
        if(Pvcfg.isAVPlayRunOnUIThread()){
            mAvCmdMiddle.set_fcc(tunerId,channelId, 0);
        }else {
            DoCommand(msg);
        }

        return 0;
    }

    public int AvControlClearFastChangeChannel(int tunerId, long channelId) {
        Log.d(TAG,"[AvControlClearFastChangeChannel]");
        Message msg = Message.obtain();
        msg.what = AvCmdMiddle.PESI_CLEAR_FCC;
        msg.arg1 = tunerId;
        msg.arg2 = (int)channelId;
        if(Pvcfg.isAVPlayRunOnUIThread()){
            mAvCmdMiddle.clear_fcc(tunerId,channelId);
        }else {
            DoCommand(msg);
        }

        return 0;
    }

    public int AvControlChangeResolution(int playId, int resolution) {
        Log.d(TAG,"AvControlSetFastChangeChannel dose not implement !!!!");
        return 0;
    }

    public int AvControlChangeAudio(int playId, AudioInfo.AudioComponent component) {
        Message msg = Message.obtain();
        AvCmdMiddle.pesi_audio_command audio_cmd = new AvCmdMiddle.pesi_audio_command();
        audio_cmd.mode  = AvCmdMiddle.PESI_CHANGE_AUDIO;
        audio_cmd.Audio_pid = component.getPid();
        msg.what = AvCmdMiddle.PESI_CHANGE_AUDIO;
        msg.obj = audio_cmd;
        DoCommand(msg);
        return 0;
    }

    public int AvControlSetVolume(int volume) {
        Log.d(TAG,"AvControlChangeAudio dose not implement !!!!");
        return 0;
    }

    public int AvControlGetVolume() {
        Log.d(TAG,"AvControlGetVolume dose not implement !!!!");
        return 0;
    }

    public int AvControlSetMute(int playId, boolean mute) {
        Log.d(TAG,"AvControlSetMute dose not implement !!!!");
        return 0;
    }

    public int AvControlSetTrackMode(int playId, EnAudioTrackMode stereo) {
        Log.d(TAG,"AvControlSetMute dose not implement !!!!");
        return 0;
    }

    public int AvControlAudioOutput(int playId, int byPass) {
        Log.d(TAG,"AvControlSetMute dose not implement !!!!");
        return 0;
    }

    public int AvControlClose(int playId) {
        Log.d(TAG,"AvControlClose dose not implement !!!!");
        return 0;
    }

    public int AvControlOpen(int playId) {
        Log.d(TAG,"AvControlOpen dose not implement !!!!");
        return 0;
    }

    public int AvControlShowVideo(int playId, boolean show) {
        Log.d(TAG,"AvControlShowVideo dose not implement !!!!");
        return 0;
    }

    public int AvControlFreezeVideo(int playId, boolean freeze) {
        Log.d(TAG,"AvControlFreezeVideo dose not implement !!!!");
        return 0;
    }

    public AudioInfo AvControlGetAudioListInfo(int playId) {
        if(mAvCmdMiddle == null || mAvCmdMiddle.getProgramInfo()==null)
            return null;
        ProgramInfo programInfo = mDataManager.getProgramInfo(mAvCmdMiddle.getProgramInfo().getChannelId());
        AudioInfo audioInfo = null;
        if(programInfo != null) {
            audioInfo = new AudioInfo();
            audioInfo.setCurPos(programInfo.getAudioSelected());
            for (int i = 0; i < programInfo.pAudios.size(); i++) {
                AudioInfo.AudioComponent audioComponent = new AudioInfo.AudioComponent();
                audioComponent.setPid(programInfo.pAudios.get(i).getPid());
                audioComponent.setAudioType(programInfo.pAudios.get(i).getCodec());
                audioComponent.setAdType(0);
                audioComponent.setTrackMode(programInfo.getAudioLRSelected());
                audioComponent.setLangCode(programInfo.pAudios.get(i).getLeftIsoLang());
                audioComponent.setPos(i);
                audioInfo.ComponentList.add(audioComponent);
            }
        }
        return audioInfo;
    }

    public int AvControlGetPlayStatus(int playId) {
        return mAvCmdMiddle.mpeg_get_play_status();
    }

    public int AvControlSetPlayStatus(int status) {
        mAvCmdMiddle.mpeg_set_play_status(status);
        return 1;
    }

    public boolean AvControlGetMute(int playId) {
        Log.d(TAG,"AvControlGetMute dose not implement !!!!");
        return false;
    }

    public EnAudioTrackMode AvControlGetTrackMode(int playId) {
        if(mAvCmdMiddle.getProgramInfo() != null)
            return EnAudioTrackMode.valueOf(mAvCmdMiddle.getProgramInfo().getAudioLRSelected());
        else
            return EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO;
    }

    public int AvControlGetRatio(int playId) {
        Log.d(TAG,"AvControlGetRatio dose not implement !!!!");
        return 0;
    }

    public int AvControlSetStopScreen(int playId, int stopType) {
        Log.d(TAG,"AvControlSetStopScreen dose not implement !!!!");
        return 0;
    }

    public int AvControlGetStopScreen(int playId) {
        Log.d(TAG,"AvControlGetStopScreen dose not implement !!!!");
        return 0;
    }

    public int AvControlGetFPS(int playId) {
        //try player.java -> VideoDecoderThread() -> onOutputFormatChanged()
        Log.d(TAG,"AvControlGetFPS dose not implement !!!!");
        return 0;
    }

    public int AvControlEwsActionControl(int playId, boolean enable) {
        Log.d(TAG,"AvControlEwsActionControl dose not implement !!!!");
        return 0;
    }

    public int AvControlSetWindowSize(int playId, Rect rect) {
        //maybe try in player.java
        Log.d(TAG,"AvControlSetWindowSize dose not implement !!!!");
        return 0;
    }

    public Rect AvControlGetWindowSize(int playId) {
        //maybe try in player.java
        Log.d(TAG,"AvControlGetWindowSize dose not implement !!!!");
        return null;
    }

    public int AvControlGetVideoResolutionHeight(int playId) {
        //maybe try in player.java
        Log.d(TAG,"AvControlGetVideoResolutionHeight dose not implement !!!!");
        return 0;
    }

    public int AvControlGetVideoResolutionWidth(int playId) {
        //maybe try in player.java
        Log.d(TAG,"AvControlGetVideoResolutionWidth dose not implement !!!!");
        return 0;
    }

    public int AvControlGetDolbyInfoStreamType(int playId) {
        Log.d(TAG,"AvControlGetDolbyInfoStreamType dose not implement !!!!");
        return 0;
    }

    public int AvControlGetDolbyInfoAcmod(int playId) {
        Log.d(TAG,"AvControlGetDolbyInfoAcmod dose not implement !!!!");
        return 0;
    }

    public SubtitleInfo.SubtitleComponent AvControlGetCurrentSubtitle(int playId) {
//        ProgramInfo programInfo = mAvCmdMiddle.getProgramInfo();
//        SubtitleInfo.SubtitleComponent subtitleComponent = new SubtitleInfo.SubtitleComponent();
        Log.d(TAG,"AvControlGetCurrentSubtitle dose not implement !!!!");
        return null;
    }

    public SubtitleInfo AvControlGetSubtitleList(int playId) {
        Log.d(TAG,"AvControlGetSubtitleList dose not implement !!!!");
        ProgramInfo programInfo = mAvCmdMiddle.getProgramInfo();
        SubtitleInfo subtitle_info = new SubtitleInfo();

        if (programInfo != null && programInfo.pSubtitle != null) {
            subtitle_info.setCurPos(0);
            for (int i = 0; i < programInfo.pSubtitle.size(); i++) {
                SubtitleInfo.SubtitleComponent subtitleComponent = new SubtitleInfo.SubtitleComponent();
                subtitleComponent.setPid(programInfo.pSubtitle.get(i).getPid());
                subtitleComponent.setLangCode(programInfo.pSubtitle.get(i).getLang());
                subtitleComponent.setComPageId(programInfo.pSubtitle.get(i).getComPageId());
                subtitleComponent.setAncPageId(programInfo.pSubtitle.get(i).getAncPageId());
                subtitle_info.Component.add(subtitleComponent);
            }
        }

        return subtitle_info;
    }

    public int AvControlSelectSubtitle(int playId, SubtitleInfo.SubtitleComponent subtitleComponent) {
        //Log.d(TAG,"AvControlSelectSubtitle dose not implement !!!!");
        Message msg = new Message();
        ProgramInfo programInfo = mAvCmdMiddle.getProgramInfo();
        AvCmdMiddle.pesi_subtitle_cmd subtitle_cmd = new AvCmdMiddle.pesi_subtitle_cmd();
        subtitle_cmd.mode = AvCmdMiddle.PESI_START_DVB_SUBTITLE;
        subtitle_cmd.TunerId = programInfo.getTunerId();
        subtitle_cmd.subtitle_pid = subtitleComponent.getPid();
        subtitle_cmd.subtitle_com_page_id = subtitleComponent.getComPageId();
        subtitle_cmd.subtitle_anc_page_id = subtitleComponent.getAncPageId();
        msg.what = subtitle_cmd.mode;
        msg.obj = subtitle_cmd;
        DoCommand(msg);
        return 0;
    }

    public int AvControlShowSubtitle(int playId, boolean enable) {
        Log.d(TAG,"AvControlShowSubtitle dose not implement !!!!");
        return 0;
    }

    public boolean AvControlIsSubtitleVisible(int playId) {
        Log.d(TAG,"AvControlIsSubtitleVisible dose not implement !!!!");
        return false;
    }

    public int AvControlSetSubtHiStatus(int playId, boolean hiOn) {
        Log.d(TAG,"AvControlSetSubtHiStatus dose not implement !!!!");
        return 0;
    }

    public int AvControlSetSubtitleLanguage(int playId, int index, String lang) {
        Log.d(TAG,"AvControlSetSubtitleLanguage dose not implement !!!!");
        return 0;
    }

    public TeletextInfo AvControlGetCurrentTeletext(int playId) {
        Log.d(TAG,"AvControlGetCurrentTeletext dose not implement !!!!");
        return null;
    }

    public List<TeletextInfo> AvControlGetTeletextList(int playId) {
        Log.d(TAG,"AvControlGetTeletextList dose not implement !!!!");
        return null;
    }

    public int AvControlShowTeletext(int playId, boolean enable) {
        Log.d(TAG,"AvControlShowTeletext dose not implement !!!!");
        return 0;
    }

    public boolean AvControlIsTeletextVisible(int playId) {
        Log.d(TAG,"AvControlIsTeletextVisible dose not implement !!!!");
        return false;
    }

    public boolean AvControlIsTeletextAvailable(int playId) {
        Log.d(TAG,"AvControlIsTeletextAvailable dose not implement !!!!");
        return false;
    }

    public int AvControlSetTeletextLanguage(int playId, String primeLang) {
        Log.d(TAG,"AvControlSetTeletextLanguage dose not implement !!!!");
        return 0;
    }

    public String AvControlGetTeletextLanguage(int playId) {
        Log.d(TAG,"AvControlGetTeletextLanguage dose not implement !!!!");
        return null;
    }

    public int AvControlSetCommand(int playId, int keyCode) {
        Log.d(TAG,"AvControlSetCommand dose not implement !!!!");
        return 0;
    }

    public Date AvControlGetTimeShiftBeginTime(int playId) {
        Log.d(TAG,"AvControlGetTimeShiftBeginTime dose not implement !!!!");
        return null;
    }

    public Date AvControlGetTimeShiftPlayTime(int playId) {
        Log.d(TAG,"AvControlGetTimeShiftPlayTime dose not implement !!!!");
        return null;
    }

    public int AvControlGetTimeShiftRecordTime(int playId) {
        Log.d(TAG,"AvControlGetTimeShiftRecordTime dose not implement !!!!");
        return 0;
    }

    public int AvControlGetTrickMode(int playId) {
        Log.d(TAG,"AvControlGetTrickMode dose not implement !!!!");
        return 0;
    }

    public int AvControlTimeshiftTrickPlay(int playId, int trickMode) {
        Log.d(TAG,"AvControlTimeshiftTrickPlay dose not implement !!!!");
        return 0;
    }

    public int AvControlTimeshiftPausePlay(int playId) {
        Log.d(TAG,"AvControlTimeshiftPausePlay dose not implement !!!!");
        return 0;
    }

    public int AvControlTimeshiftPlay(int playId) {
        Log.d(TAG,"AvControlTimeshiftPlay dose not implement !!!!");
        return 0;
    }

    public int AvControlTimeshiftSeekPlay(int playId, long seekTime) {
        Log.d(TAG,"AvControlTimeshiftSeekPlay dose not implement !!!!");
        return 0;
    }

    public int AvControlStopTimeShift(int playId) {
        Log.d(TAG,"AvControlStopTimeShift dose not implement !!!!");
        return 0;
    }

    public int AvControlStartDVBSubtitle(int index, long channelId){

        return 0;
    }

    public int AvControlStopDVBSubtitle(){
        Message msg = new Message();
        AvCmdMiddle.pesi_avplay avplay = new AvCmdMiddle.pesi_avplay();
        avplay.mode = AvCmdMiddle.PESI_STOP_DVB_SUBTITLE;
        msg.what = avplay.mode;
        msg.obj = avplay;
        DoCommand(msg);
        return 0;
    }
    /*
    PIP
     */
    public int PipOpen(int x, int y, int width, int height) {
        Log.d(TAG,"PipOpen dose not implement !!!!");
        return 0;
    }

    public int PipClose() {
        Log.d(TAG,"PipClose dose not implement !!!!");
        return 0;
    }

    public int PipStart(long channelId, int show) {
        Log.d(TAG,"PipStart dose not implement !!!!");
        return 0;
    }

    public int PipStop() {
        Log.d(TAG,"PipStop dose not implement !!!!");
        return 0;
    }

    public int PipSetWindow(int x, int y, int width, int height) {
        Log.d(TAG,"PipSetWindow dose not implement !!!!");
        return 0;
    }

    public int PipExChange() {
        Log.d(TAG,"PipExChange dose not implement !!!!");
        return 0;
    }

    @Override
    public void BaseHandleMessage(Message msg) {
//        Log.d(TAG,"BaseHandleMessage msg = "+msg.toString()+" what = "+msg.what+" start !!");
        mAvCmdMiddle.BaseHandleMessage(msg,getHandlerThreadHandler());
//        Log.d(TAG,"BaseHandleMessage msg = "+msg.toString()+" what = "+msg.what+" end !!");
    }

    @Override
    public void destroy() {
        mAvCmdMiddle.destroy();
        super.destroy();
    }
}
