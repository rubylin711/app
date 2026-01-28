package com.prime.dtv.service.Player;

import static com.prime.datastructure.sysdata.DTVMessage.PESI_EVT_CA_WIDEVINE_REMOVE_LICENSE;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.AudioInfo;
import com.prime.datastructure.sysdata.CasData;
import com.prime.datastructure.sysdata.DTVMessage;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.PvrDbRecordInfo;
import com.prime.datastructure.sysdata.PvrInfo;
import com.prime.datastructure.sysdata.PvrRecFileInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.ErrorCode;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.CasRefreshHelper;
import com.prime.dtv.Interface.PesiDtvFrameworkInterface;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.PesiDtvFramework;
import com.prime.dtv.service.Dvr.DvrDevManager;
import com.prime.dtv.service.Dvr.DvrRecorderController;
import com.prime.dtv.service.Table.Eit;
import com.prime.dtv.service.Table.Table;
import com.prime.dtv.service.Tuner.TunerBase;
import com.prime.dtv.service.Tuner.TunerInterface;
import com.prime.dtv.service.database.PvrContentProvider;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.datamanager.PvrDataManager;
import com.prime.dtv.service.subtitle.SubtitleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AvCmdMiddle {
    private static final String TAG = "AvCmdMiddle" ;

    public static final int FAST_ZAPPING = 0;
    public static final int PESI_MAINWIN = 0;
    public static final int PESI_PIPWIN = 1;
    public static final int PESI_PREAV = 2;
    public static final int PESI_WIN_MODE_MAX = 3;

    public static final int MAX_AVHANDLE_NUM = 4;
    public static final int MAX_PRE_AV_HANDLE = 2;
    public static final int MAX_REC = 2;

    public static final int PESI_PLAY_AV = 1;
    public static final int PESI_STOP_AV = 2;
    public static final int PESI_PLAY_PIP = 3;
    public static final int PESI_STOP_PIP = 4;
    public static final int PESI_PRE_START = 5;
    public static final int PESI_VIDEO_COUNT_CHECK = 6;

    public static final int PESI_START_DVB_SUBTITLE = 7;

    public static final int PESI_STOP_DVB_SUBTITLE = 8;

    public static final int PESI_CHANGE_AUDIO = 9;
    public static final int PESI_SET_FCC = 10;
    public static final int PESI_CLEAR_FCC = 11;
    public static final int PESI_STOP_AV_ALL = 12;
    public static final int PESI_PLAY_AV_NEW = 13;

    public static final int PESI_STOP_All_EIT_ON_CURRENT_TP = 14;

    public static final int PESI_RESET_AUDIO_DEFAULT_LANGUAGE = 15;
    //PVR feature
    public static final int PVR_START_RECORD = 101;
    public static final int PVR_STOP_RECORD = 102;
    public static final int PVR_START_PLAYBACK = 103;
    public static final int PVR_STOP_PLAYBACK = 104;
    public static final int PVR_START_TIMESHIFT = 105;
    public static final int PVR_STOP_TIMESHIFT = 106;
    public static final int PVR_STOP_ALLRECORD = 107;

    public static final int PVR_PLAYBACK_PLAY = 110;
    public static final int PVR_PLAYBACK_PAUSE = 111;
    public static final int PVR_PLAYBACK_SEEK = 112;
    public static final int PVR_PLAYBACK_FF = 113;
    public static final int PVR_PLAYBACK_RW = 114;
    public static final int PVR_PLAYBACK_SET_SPEED = 115;
    public static final int PVR_PLAYBACK_CHANGE_AUDIO_TRACK = 116;
    public static final int PVR_PLAYBACK_CHANGE_SUB_TRACK = 117;

    public static final int PVR_TIMESHIFT_PLAY_RESUME = 118;
    public static final int PVR_TIMESHIFT_PLAY_PAUSE = 119;
    public static final int PVR_TIMESHIFT_PLAY_SEEK = 120;
    public static final int PVR_TIMESHIFT_PLAY_FF = 121;
    public static final int PVR_TIMESHIFT_PLAY_RW = 122;
    public static final int PVR_TIMESHIFT_PLAY_CHANGE_AUDIO_TRACK = 123;
    public static final int PVR_TIMESHIFT_PLAY_CHANGE_SUB_TRACK = 124;
    public static final int PVR_STOP_ALL = 125;

    public static final int VIDEO_COUNT_CHECK_OFF = 0;
    public static final int VIDEO_COUNT_CHECK_ON = 1;

    public static final int PESI_SVR_AV_STOP_STATE = 0;
    public static final int PESI_SVR_AV_LIVEPLAY_STATE = 1;
    public static final int PESI_SVR_AV_TIMESHIFTPLAY_STATE = 2;
    public static final int PESI_SVR_AV_PAUSE_STATE = 3;
    public static final int PESI_SVR_AV_IDLE_STATE = 4;
    public static final int PESI_SVR_AV_RELEASE_STATE = 5;
    public static final int PESI_SVR_AV_PIP_STATE = 6;
    public static final int PESI_SVR_AV_EWSPLAY_STATE = 7;
    public static final int PESI_SVR_AV_PVRPLAY_STATE = 8;

    private static AvCmdMiddle g_av_cmd_middle_manager;
    private Handler mPlayHandler;

    private PesiDtvFrameworkInterfaceCallback mPesiDtvCallback = null;
    private Context mContext = null;
    private int AcceptRating = 0;
    private int IsAVLock = 0;
    private static DataManager mDatamanager = null;
    private Player mPlayer = null;
    private Player[] mPlayers = new Player[3]; // fcc
    private boolean[] mSetVisibleCompleted = new boolean[3];
    private static PmtNotifyCa[] mPlayersPmtNotifyCa = new PmtNotifyCa[3];
    private Surface mSurface = null;
    private Surface mTvViewSurface = null;
    private static Surface[] gSurface = new Surface[3];
    private static SurfaceHolder[] mSurfaceHolder = new SurfaceHolder[3];
    private SurfaceHolder mTvViewSurfaceHolder = null;
    private static SurfaceHolder.Callback[] mSurfaceHolderCallback = new SurfaceHolder.Callback[3];
    private final long[] mPendingFccChannelId = new long[]{-1, -1, -1};
    private boolean mFccSurfaceActive = false;
    private TunerInterface mTunerInterface = null;
    private boolean mPassthrough = true;
    private int mPesiplayStatus = PESI_SVR_AV_RELEASE_STATE;
    private ProgramInfo mProgramInfo = null;
    private boolean mIsWaitingForSurface = false;
    private SubtitleManager mSubtitleManager = null;
    private Eit mPresentFollowEit = null;
    private Eit mScheduleEit = null;
    private FrameLayout mLiveTvLayout = null;
    private static boolean isAVPlayReady = true;

    private DvrDevManager[] mDvrDevManagerList = new DvrDevManager[3];
    private PvrDbRecordInfo mDbRecordInfo = null;
    protected ExecutorService mExecutor;
    private static final int THREAD_COUNT = 1;
    private static int play_index = -1;


    private Player.Callback mPlayerCallback = new Player.Callback() {
        @Override
        public void onHttpError(int responseCode, long channelId, String contentId) {
            LogUtils.d("[CheckErrorMsg] responseCode = "+responseCode);
            switch (responseCode) {
                    case 401:
                        CasRefreshHelper casRefreshHelper = CasRefreshHelper.get_instance();
                        CasData casData = casRefreshHelper.get_cas_data();

                        if (casData.getSuspended() == 1) {
                            sendWvCasError(ErrorCode.ERROR_E511, "", channelId);
                        }
                        else if (!casData.getEntitledChannelIds().contains(contentId)) {
                            sendWvCasError(ErrorCode.ERROR_E010, "", channelId);
                        }
                        else {
                            sendWvCasError(ErrorCode.ERROR_E510, "", channelId);
                        }

                        break;
                    case 403:
                        sendWvCasError(ErrorCode.ERROR_E507, "", channelId);
                        break;
                    case 404:
                        sendWvCasError(ErrorCode.ERROR_E512, "", channelId);
                        break;
                    case 400:
                    case 409:
                    case 500:
                    default:
                        sendWvCasError(ErrorCode.ERROR_E510, "", channelId);
                        break;
                }
        }

        @Override
        public void onCasError(String msg, long channelId) {
            LogUtils.d("[CheckErrorMsg] msg = "+msg+" channelId = "+channelId);
            sendWvCasError(ErrorCode.ERROR_E510, msg, channelId);
        }

        @Override
        public void onConnectTimeout(String msg, long channelId) {
            LogUtils.d("[CheckErrorMsg] msg = "+msg+" channelId = "+channelId);
            sendWvCasError(ErrorCode.ERROR_E510, msg, channelId);
        }

        @Override
        public void onRemoveLicense(long channelId, String contentId) {
            mPesiDtvCallback.sendCallbackMessage(PESI_EVT_CA_WIDEVINE_REMOVE_LICENSE, (int)channelId, 0, contentId);
        }

        @Override
        public void onDecoderError() {
            if (mPesiDtvCallback != null) {
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_DECODER_ERROR, 0, 0, null);
            }
        }
        @Override
        public void onFirstFrameRendered(int tunerId, long channelId)
        {
            if (mPesiDtvCallback != null) {
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_FIRST_FRAME,
                        tunerId, (int) channelId, null);
            }
        }
        // ★ 新增：告訴 TIF「影像不可用（原因由 TIF 常數對應）」
        @Override
        public void onVideoUnavailable(int tunerId, int reason)
        {
            if (mPesiDtvCallback != null) {
                LogUtils.d("[CheckErrorMsg] tunerId = "+tunerId+" reason = "+reason);
                if(mPlayer!=null && mPlayer.getTunerId() == tunerId)
                    mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_VideoUnavailable, reason, 0, null);
            }
        }
    };

    public class PmtNotifyCa {
        private long mChannelId;
        private boolean isNotifyPmt;


        public long getChannelId() {
            return mChannelId;
        }

        public void setChannelId(long ChannelId) {
            this.mChannelId = ChannelId;
        }

        public boolean isNotifyPmt() {
            return isNotifyPmt;
        }

        public void setNotifyPmt(boolean notifyPmt) {
            isNotifyPmt = notifyPmt;
        }
    }

    private AvCmdMiddle(Context context,PesiDtvFrameworkInterfaceCallback callback) {
        mContext = context;
        mPesiDtvCallback = callback;
        mDatamanager = DataManager.getDataManager(context);
        mSubtitleManager = SubtitleManager.getSubtitleManager(context, callback);
        mTunerInterface = TunerInterface.getInstance(mContext);
        mPlayersPmtNotifyCa[0] = new PmtNotifyCa();
        mPlayersPmtNotifyCa[1] = new PmtNotifyCa();
        mPlayersPmtNotifyCa[2] = new PmtNotifyCa();
        mExecutor = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    public static AvCmdMiddle get_instance(Context context,PesiDtvFrameworkInterfaceCallback callback) {
        if(g_av_cmd_middle_manager == null) {
            g_av_cmd_middle_manager = new AvCmdMiddle(context,callback);
        }
        return g_av_cmd_middle_manager;
    }

    private boolean isFirtPlay = true;

    public static int getPlay_index() {
        return play_index;
    }

    private static int surfaceId(Surface surface) {
        return surface == null ? 0 : System.identityHashCode(surface);
    }

    private static final java.util.concurrent.ConcurrentHashMap<Integer, Integer> sSurfaceIdToIndex =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static void mapSurface(Surface surface, int index, String reason) {
        int id = surfaceId(surface);
        if (surface == null) {
            sSurfaceIdToIndex.remove(id);
            return;
        }
        sSurfaceIdToIndex.put(id, index);
    }

    public static int getSurfaceIndex(Surface surface) {
        if (surface == null) {
            return -1;
        }
        Integer index = sSurfaceIdToIndex.get(surfaceId(surface));
        return index == null ? -1 : index;
    }

    public static String surfaceIndexDebug(Surface surface) {
        return "surfaceId=" + surfaceId(surface) + " surfaceIndex=" + getSurfaceIndex(surface);
    }

    private void refreshFccSurfaceActive() {
        boolean active = false;
        for (Surface surface : gSurface) {
            if (surface != null && surface.isValid()) {
                active = true;
                break;
            }
        }
        if (mFccSurfaceActive != active) {
            mFccSurfaceActive = active;
            LogUtils.d("FCC_LOG fccSurfaceActive=" + mFccSurfaceActive);
        }
    }


    public void setSurface(Surface surface) {
        LogUtils.d("FCC_LOG setTvViewSurface: surface=" + surface +
                " surfaceId=" + surfaceId(surface));
        mTvViewSurface = surface;
        mapSurface(surface, -100, "setTvViewSurface");
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        LogUtils.d("FCC_LOG setTvViewSurfaceHolder: holder=" + surfaceHolder +
                " surface=" + (surfaceHolder == null ? "null" : surfaceHolder.getSurface()) +
                " surfaceId=" + surfaceId(surfaceHolder == null ? null : surfaceHolder.getSurface()));
        mTvViewSurfaceHolder = surfaceHolder;
        mapSurface(surfaceHolder == null ? null : surfaceHolder.getSurface(), -100, "setTvViewSurfaceHolder");
    }

    public void setSurfaceToPlayer(int index,Surface surface) {
        if(mPlayers[index] != null)
            mPlayers[index].set_new_surface(surface);
    }

    public void setSurface(Surface surface,int index) {
        gSurface[index] = surface;
        refreshFccSurfaceActive();
        LogUtils.d("FCC_LOG setSurface[" + index + "]: surface=" + surface +
                " surfaceId=" + surfaceId(surface) +
                " valid=" + (surface != null && surface.isValid()) +
                " hasPlayer=" + (mPlayers[index] != null));
        mapSurface(surface, index, "setSurface");
        if(mPlayers[index] != null) {
//            Log.d(TAG,"1111 set_new_surface surface = " + surface + " index = " + index);
            mPlayers[index].set_new_surface(surface);
//            Log.d(TAG,"2222 set_new_surface surface = " + surface + " index = " + index);
        }
    }

    private Surface pickSurfaceForPlay(int tunerId) {
        Surface surface = gSurface[tunerId];
        if (surface != null) {
            return surface;
        }
        SurfaceHolder holder = mSurfaceHolder[tunerId];
        if (holder != null && holder.getSurface() != null) {
            return holder.getSurface();
        }
        if (mTvViewSurface != null) {
            return mTvViewSurface;
        }
        if (mTvViewSurfaceHolder != null && mTvViewSurfaceHolder.getSurface() != null) {
            return mTvViewSurfaceHolder.getSurface();
        }
        return null;
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder, int index) {
        LogUtils.d("FCC_LOG setSurfaceHolder[" + index + "]: holder=" + surfaceHolder +
                " surface=" + (surfaceHolder == null ? "null" : surfaceHolder.getSurface()) +
                " surfaceId=" + surfaceId(surfaceHolder == null ? null : surfaceHolder.getSurface()) +
                " hasPlayer=" + (mPlayers[index] != null));

        if(mSurfaceHolder[index] != null/* && mSurfaceHolder[index].equals(surfaceHolder)*/ && mSurfaceHolderCallback[index] != null) {
//            Log.d(TAG,"remove mSurfaceHolderCallback["+index+"] = "+mSurfaceHolderCallback[index]);
            mSurfaceHolder[index].removeCallback(mSurfaceHolderCallback[index]);
        }
        mSurfaceHolder[index] = surfaceHolder;
        mapSurface(surfaceHolder == null ? null : surfaceHolder.getSurface(), index, "setSurfaceHolder");
        mSurfaceHolderCallback[index] = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                LogUtils.d("FCC_LOG surfaceCreated[" + index + "]: holder=" + holder +
                        " surface=" + holder.getSurface() +
                        " surfaceId=" + surfaceId(holder.getSurface()));
                if(mPlayers[index] != null) {
                    //mPlayers[index].set_new_surface(holder.getSurface());
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                LogUtils.d("FCC_LOG surfaceChanged[" + index + "]: holder=" + holder +
                        " surface=" + holder.getSurface() + " format=" + format +
                        " size=" + width + "x" + height +
                        " surfaceId=" + surfaceId(holder.getSurface()));
                if(mPlayers[index] != null ){
                    mPlayers[index].set_new_surface(holder.getSurface());
                }
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                LogUtils.d("FCC_LOG surfaceDestroyed[" + index + "]: holder=" + holder +
                        " surface=" + holder.getSurface() +
                        " surfaceId=" + surfaceId(holder.getSurface()));
//                mSurface = null;
            }
        };
        mSurfaceHolder[index].addCallback(mSurfaceHolderCallback[index]);
    }

    public void setLiveTvLayout(FrameLayout liveTvLayout){
        mLiveTvLayout = liveTvLayout;
    }

    public static boolean isAVPlayReady() {
        return isAVPlayReady;
    }

    public class PesiPictureCountControl{
        public int VidFrameCount;
        public int PreVidFrameCount;
        public int SendMsgFlag;
    }

    public class pesi_av_control_info{
        public int AVHandle;
        public int hWin;
        public int hTrack;
        public int index;
        public long channel_id;
        public int tuner_id;
        public int demux_id;
        public int isrec;
        public int x;
        public int y;
        public int dx;
        public int dy;
        public PesiPictureCountControl PictureControl;
    }

    public class pesi_av_rec_info{
        public long channel_id;
        public int tuner_id;
        public int pre_av_index;
        public int isrec;
    }


    public class pesi_av_control{
        public pesi_av_control_info[] mainwin = new pesi_av_control_info[MAX_AVHANDLE_NUM];
        public pesi_av_control_info pipwin;
        public pesi_av_control_info[] pre_av = new pesi_av_control_info[MAX_PRE_AV_HANDLE];
        public pesi_av_rec_info[] rec = new pesi_av_rec_info[MAX_REC];
//        public int mpeg_control_get_AVhandle(pesi_win_mode mode, int handle_index)
//        {
//            switch(mode)
//            {
//                case PESI_MAINWIN:
//                {
//                    return av_control.mainwin[handle_index].AVHandle;
//                }break;
//
//                case PESI_PIPWIN:
//                {
//                    return av_control.pipwin.AVHandle;
//
//                }break;
//                default:
//                {
//                    MPEGDRV_PRINTD("[%s] mode[%d] error\n", __FUNCTION__,handle_index);
//                }
//            }
//            return 0;
//        }
    }

    public static class pesi_audio_command {
        public int mode;
        public int Audio_pid;
    }

    public static class pesi_avplay_new {
        public static final int MODE_INIT = 0;
        public static final int MODE_FCC = 1;
        public static final int MODE_RESET = 2;
        public static final int MODE_CHANNEL_BLOCKED = 3;
        public int mode;//0: Init   1:FCC Switch  2:Reset
        public boolean channelBlocked;
        //public Map<Long, Integer> avplay_info = new LinkedHashMap<>();
        public List<Long> channelIdList = new ArrayList<>();
        public List<Integer> tunerIdList = new ArrayList<>();
    }

    public static class pesi_avplay {
        public int mode;
        public int playid;
        public long ChannelID;
        public int TunerId;
        public int stop_mode;
        public int stop_monitor_table;
        public int subtitle_play_index;
        public long[] PreAVChannelID = new long[MAX_PRE_AV_HANDLE];
    }

    public static class pesi_subtitle_cmd {
        public int mode;
        public int subtitle_play_index;
        public int TunerId;
        public int subtitling_type;
        public int subtitle_pid;
        public int subtitle_com_page_id;
        public int subtitle_anc_page_id;
    }

    public static class Mpeg_ProgLock_Status{
        public static final int MPEG_NO_LOCK = 0;
        public static final int MPEG_USER_LOCK = 1;
        public static final int MPEG_PARENT_LOCK = 2;
        public int LockType;
        public long ChannelID;
        public int ParentRating;
        public int bShow;
    }

    public int mpeg_get_accept_rating(){
        return AcceptRating;
    }

    public void mpeg_set_accept_rating(int rating){
        if(AcceptRating < rating)
            AcceptRating = rating;
    }

    public void mpeg_reset_accept_rating(){
        AcceptRating = 0;
    }

    public int Pesi_AV_get_av_lock()
    {
        return IsAVLock;
    }
    public void Pesi_AV_set_av_lock(int lock)
    {
        IsAVLock = lock;
    }

    public ProgramInfo getProgramInfo() {
        return mProgramInfo;
    }

    public void playAv(int tunerId, long ChannelID) {
        LogUtils.d("FCC_LOG playAv start tunerId=" + tunerId + " channelId=" + ChannelID);
        LogUtils.d("playAv avplay.ChannelID = "+ChannelID);
        ProgramInfo programInfo = mDatamanager.get_service_by_channelId(ChannelID);
        LogUtils.d("programInfo = "+programInfo+" tuner id = "+programInfo.getTunerId());
        isAVPlayReady = false;
        if(programInfo != null)
            Log.d(TAG,"playAv avplay.ChannelID = "+ChannelID+" name["+programInfo.getDisplayName()+"]");
        if(ChannelID == 0)
        {
            LogUtils.d(" ChannelID = 0 error!!!!!");
            isAVPlayReady = true;
            return;
        }
        if(programInfo == null)
        {
            LogUtils.d("programInfo = NULL!!!!!");
            isAVPlayReady = true;
            return;
        }
        if(getProgramInfo() != null
                && getProgramInfo().getChannelId() == ChannelID
                // fix unable to play if we stop and replay same program
                && mPesiplayStatus == PESI_SVR_AV_LIVEPLAY_STATE) {
            Log.d(TAG,"programInfo same and already playing !!!!!");
            isAVPlayReady = true;
            return;
        }

//        mProgramInfo = programInfo;

//        int tunerId = programInfo.getTunerId();
//        Log.d(TAG, "playAv: tunerid = " + tunerId);

        TpInfo tp = mDatamanager.getTpInfo(programInfo.getTpId());
        SatInfo satInfo = mDatamanager.getSatInfo(tp.getSatId());

        if(tp == null){
            LogUtils.e("TPifo is NULL!!!! Shall check DB!!!!!!!!!!!");
            isAVPlayReady = true;
            return;
        }

        if(tp != mTunerInterface.getTpinfo(tunerId) && !Pvcfg.isFccEnable())
            mTunerInterface.cancelTune(tunerId);

        boolean lock = mTunerInterface.tune(tunerId,tp,satInfo);
        LogUtils.d(TAG+" tunerInterface.tune "+ programInfo.getDisplayName() +
                " tuner_id = " + tunerId + " tp = " + tp.ToString() + " lock = "+lock);

        boolean isLock = mTunerInterface.isLock(tunerId);
        if(isLock == false){
            LogUtils.d(TAG+" Tuner NO lock , return!!!!!");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_PLAY_FAILURE, 0, 0, programInfo);
            isAVPlayReady = true;
            return;
        }

        if(!Pvcfg.isFccEnable())
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_START_TIME_SYNC, 0, 0, null);

        LogUtils.d("Send PESI_EVT_SYSTEM_START_MONITOR_TABLE");
        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_START_MONITOR_TABLE, 0, tunerId, ChannelID);
        // for present following EPG
        LogUtils.d("primetif test 0001 start eit pf");
        startEit(programInfo.getTpId(),tunerId);

        if(programInfo.getPmtversion() == 0){
            LogUtils.e("getPmtversion 0 , return.....Maybe need check PMT");
            //return;
        }
        LogUtils.d("getType = "+programInfo.getType());
        if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG) ||
                Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC)) {
            if (programInfo.pVideo.getPID() == 0 && programInfo.getType() == ProgramInfo.PROGRAM_TV) {
                LogUtils.e("Video PID is 0 , return.....Maybe need check PMT");
                isAVPlayReady = true;
                return;
            }
        }
        else {
            if(programInfo.pVideo.getPID() == 0){
                LogUtils.e("Video PID is 0 , return.....Maybe need check PMT");
                isAVPlayReady = true;
                return;
            }
        }
        if(programInfo.getType() == ProgramInfo.PROGRAM_RADIO && programInfo.pAudios.size() == 0){
            LogUtils.e("Radio channel without Audio !! , return.....Maybe need check PMT");
            isAVPlayReady = true;
            return;
        }
        //ChannelChangeManager.setFCCVisible(tunerId);
        mProgramInfo = programInfo;
        Surface playSurface = pickSurfaceForPlay(tunerId);
        int playSurfaceIndex = getSurfaceIndex(playSurface);
        LogUtils.d("FCC_LOG playAv surfaceSelect: tunerId=" + tunerId +
                " channelId=" + ChannelID +
                " surface=" + playSurface +
                " valid=" + (playSurface != null && playSurface.isValid()) +
                " " + surfaceIndexDebug(playSurface));
        if (mFccSurfaceActive && playSurfaceIndex == -100) {
            if (tunerId >= 0 && tunerId < mPendingFccChannelId.length) {
                mPendingFccChannelId[tunerId] = ChannelID;
            }
            LogUtils.d("FCC_LOG defer playAv: tunerId=" + tunerId +
                    " channelId=" + ChannelID +
                    " reason=TvViewFallback " + surfaceIndexDebug(playSurface));
            isAVPlayReady = true;
            return;
        }
        if (tunerId >= 0 && tunerId < mPendingFccChannelId.length
                && mPendingFccChannelId[tunerId] == ChannelID
                && playSurfaceIndex >= 0) {
            mPendingFccChannelId[tunerId] = -1;
        }
        if (mPlayers[tunerId] == null) {
            mPlayers[tunerId] = new Player(mContext,this,playSurface,tunerId,mTunerInterface,programInfo,mPassthrough, mPlayerCallback);
   			// 或傳進來的那個
            TunerBase tb = TunerInterface.getInstance().getTunerBase(tunerId);
            if (tb!=null){
                mPlayers[tunerId].bindFrontendLock(tb);
            }
            mPlayers[tunerId].setSetVisibleCompleted(mSetVisibleCompleted[tunerId]);
//            mPlayers[tunerId] = new Player(mContext,this,mSurfaceHolder[tunerId],tunerId,mTunerInterface,programInfo,mPassthrough, mPlayerCallback);
        }
        // player of the tunerId exist but not the channel we want to play
        // or fcc has error
        // clear the player and recreate one
        else {
            if (mPlayers[tunerId].getProgramInfo().getChannelId() != programInfo.getChannelId()
                    || mPlayers[tunerId].hasCasSessionError()) {
                mPlayers[tunerId].stop_now();
                mPlayers[tunerId].close();
                mPlayers[tunerId] = null;
                mPlayers[tunerId] = new Player(mContext,this,playSurface,tunerId,mTunerInterface,programInfo,mPassthrough, mPlayerCallback);
                TunerBase tb = TunerInterface.getInstance().getTunerBase(tunerId);
                if (tb!=null){
                    mPlayers[tunerId].bindFrontendLock(tb);
                }
                mPlayers[tunerId].setSetVisibleCompleted(mSetVisibleCompleted[tunerId]);
//                mPlayers[tunerId] = new Player(mContext,this,mSurfaceHolder[tunerId],tunerId,mTunerInterface,programInfo,mPassthrough, mPlayerCallback);
            }
        }
        LogUtils.d("programInfo LCN = "+programInfo.getLCN()+" Name = "+programInfo.getChName_chi()+" getDisplayNum = "+programInfo.getDisplayNum());
        if(Pvcfg.isFccV3Enable() && mPlayers[tunerId].IsPrePlay()){
            mPlayers[tunerId].forcue_audio();
            mPlayers[tunerId].setPrePlay(false);
        }else {
            mPlayers[tunerId].play(0);
        }
//        mPlayers[tunerId].SetCallback(mCallback);
        mPlayer = mPlayers[tunerId];
        mpeg_set_play_status(PESI_SVR_AV_LIVEPLAY_STATE);
//        start_monitor_table();
//        if(qgpos_ModifyTDTStime())
//            start_run_table_tdt();
//        if(qs_type(pservice) == RADIOCH){
//            PESI_SVR_AV_ShowVideo(0);
//        }
        LogUtils.d("FCC_LOG playAv end tunerId=" + tunerId + " channelId=" + ChannelID);
        isAVPlayReady = true;
        play_index = tunerId;
        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_PLAY_SUCCESS, 0, 0, null);
//        Log.d(TAG, "playAv: players = " + Arrays.toString(mPlayers));
    }
    public static void WaitAVPlayReady(){
        while (!isAVPlayReady){
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stopAV(){
        LogUtils.d(" All ");
        for(int i=0 ; i<3 ; i++){
            if(mPlayers[i] != null) {
                mPlayers[i].stop_now();
                mPlayers[i].close();
                mPlayers[i] = null;
                mTunerInterface.setTpinfo(i, null);
            }
        }
        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_STOP_MONITOR_TABLE, 0, -1,(long)-1);
        mpeg_set_play_status(PESI_SVR_AV_STOP_STATE);
    }
    //mode 0=> really stop
    //mode 1=> fcc stop current (do not close)
    //
    public void stopAv(int tunerId, int mode, int stop_monitor_table) {
        try {
            LogUtils.d("FCC_LOG stopAv: tunerId=" + tunerId +
                    " mode=" + mode +
                    " stopMonitorTable=" + stop_monitor_table +
                    " hasPlayer=" + (mPlayers[tunerId] != null));
            if (mPlayers[tunerId] != null) {
                mPlayers[tunerId].stop_now();
                if ((Pvcfg.isFccEnable() == false) || (mode == 0) || Pvcfg.isFccV3Enable()) {
                    mPlayers[tunerId].close();
                    mPlayers[tunerId] = null;
                }
            }
            // re prepare stopped player for fcc according to rtk demo
            if (/*Pvcfg.isFccEnable() && */mProgramInfo != null && (stop_monitor_table == 1)) {
                //LogUtils.d("stopAv FCC: re-new Player");
                LogUtils.d("Send PESI_EVT_SYSTEM_STOP_MONITOR_TABLE");
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_STOP_MONITOR_TABLE, 0, tunerId, (long) -1);
                //mPlayers[tunerId] = new Player(mContext, mSurfaceHolder, tunerId, mTunerInterface, mProgramInfo, mPassthrough);
            }
            //Log.d(TAG, "stopAv: players " + Arrays.toString(mPlayers));
            mpeg_set_play_status(PESI_SVR_AV_STOP_STATE);
            LogUtils.d("FCC_LOG stopAv end tunerId=" + tunerId);
        }
        catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void set_fcc(int tuner_id, long ch_id, int preplay) {
        LogUtils.d("FCC_LOG set_fcc: tunerId=" + tuner_id +
                " channelId=" + ch_id +
                " preplay=" + preplay +
                " surfaceHolder=" + mSurfaceHolder[tuner_id] +
                " surface=" + gSurface[tuner_id]);
        if (tuner_id < 0 || tuner_id >= mPlayers.length) {
            LogUtils.d("FCC_LOG set_fcc invalid tunerId=" + tuner_id);
            return;
        }
        if (ch_id == 0) {
            LogUtils.d("FCC_LOG set_fcc skip channelId=0 tunerId=" + tuner_id +
                    " preplay=" + preplay);
            return;
        }

        ProgramInfo programInfo = mDatamanager.get_service_by_channelId(ch_id);
        if (programInfo == null) {
            LogUtils.d("FCC_LOG set_fcc programInfo null tunerId=" + tuner_id +
                    " channelId=" + ch_id);
            return;
        }

        programInfo.setTunerId(tuner_id); // update tuner id in channel for tune
        mDatamanager.updateProgramInfo(programInfo);
        // clear the player if exist
        // tuner id is the index of players
        if (mPlayers[tuner_id] != null) {
            if(mPlayers[tuner_id].getChannelId() != ch_id) {
                mPlayers[tuner_id].stop_now();
                mPlayers[tuner_id].close();
                mPlayers[tuner_id] = null;
            }
            else{
                mPlayers[tuner_id].createVideoCodec();
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_START_MONITOR_TABLE, 1, tuner_id, ch_id);
                return;
            }
        }

        TpInfo tp = mDatamanager.getTpInfo(programInfo.getTpId());
        if(tp == null){
            LogUtils.d("FCC_LOG set_fcc getTpInfo fail tunerId=" + tuner_id);
            return;
        }
        boolean lock = mTunerInterface.tune(tuner_id, tp);

        if (lock == false) {
            LogUtils.d("FCC_LOG set_fcc lock failed tunerId=" + tuner_id +
                    " channelId=" + ch_id);
            return;
        }

        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_START_MONITOR_TABLE, 1, tuner_id, ch_id);
        if(programInfo.pVideo.getPID() == 0 && programInfo.getType() == ProgramInfo.PROGRAM_TV){
            LogUtils.d("FCC_LOG set_fcc videoPid=0 tunerId=" + tuner_id);
            return;
        }
        if(programInfo.getType() == ProgramInfo.PROGRAM_RADIO && programInfo.pAudios.size() == 0){
            LogUtils.d("FCC_LOG set_fcc radio no audio tunerId=" + tuner_id);
            return;
        }
        mPlayers[tuner_id] = new Player(
                mContext,this, gSurface[tuner_id], tuner_id, mTunerInterface, programInfo, mPassthrough, mPlayerCallback);
        TunerBase tb = TunerInterface.getInstance().getTunerBase(tuner_id);
        if (tb!=null){
            mPlayers[tuner_id].bindFrontendLock(tb);
        }
//        mPlayers[tuner_id] = new Player(
//                mContext,this, mSurfaceHolder[tuner_id], tuner_id, mTunerInterface, programInfo, mPassthrough, mPlayerCallback);
        mPlayers[tuner_id].setSetVisibleCompleted(mSetVisibleCompleted[tuner_id]);
        if(Pvcfg.isFccV3Enable() && (preplay == 1)) {
            mPlayers[tuner_id].play(1);
        }
    }

    public void clear_fcc(int tuner_id, long ch_id) {
        LogUtils.d("FCC_LOG clear_fcc: tunerId=" + tuner_id +
                " channelId=" + ch_id +
                " surfaceHolder=" + mSurfaceHolder[tuner_id] +
                " surface=" + gSurface[tuner_id]);
        LogUtils.d("FCC_LOG clear_fcc start tunerId=" + tuner_id + " channelId=" + ch_id);

        if (tuner_id < 0 || tuner_id >= mPlayers.length) {
            Log.e(TAG, "clear_fcc: invalid tuner id");
            return;
        }
        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_STOP_MONITOR_TABLE, 0, tuner_id,ch_id);

        ProgramInfo programInfo = mDatamanager.get_service_by_channelId(ch_id);
        if (programInfo == null) {
            Log.w(TAG, "clear_fcc: programInfo is null");
            return;
        }

        // set to PESI_SVR_AV_STOP_STATE if we clear playing channel
        if (mProgramInfo != null
                && mProgramInfo.getChannelId() == programInfo.getChannelId()
                && mProgramInfo.getTunerId() == programInfo.getTunerId()) {
            mpeg_set_play_status(PESI_SVR_AV_STOP_STATE);
        }


        if(programInfo.getPvrSkip() != 1) {
            programInfo.setTunerId(0); // reset to tuner id 0
            mDatamanager.updateProgramInfo(programInfo);
        }

        LogUtils.d("FCC_LOG clear_fcc players[" + tuner_id + "]=" + mPlayers[tuner_id]);
        // tuner id is the index of players
        if (mPlayers[tuner_id] != null) {
            mPlayers[tuner_id].stop_now();
            mPlayers[tuner_id].close();
            mPlayers[tuner_id] = null;
        }
        LogUtils.d("FCC_LOG clear_fcc end tunerId=" + tuner_id);
//        Log.d(TAG, "clear_fcc: mPlayers = " + Arrays.toString(mPlayers));
    }
    private void startEit(int tp_id, int tunerId){
        startPresentFollowEit(tunerId);
        if(!Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG))
            startSchedulerEit(tp_id,tunerId);
    }
    private void stopPresentFollowEit(){
        if (mPresentFollowEit != null) {
            mPresentFollowEit.abort();
            mPresentFollowEit.cleanup();
            mPresentFollowEit = null;
        }
    }
    private void startPresentFollowEit(int tunerId) {
        stopPresentFollowEit();

        if(Pvcfg.isEnableEIT_PF()) {
            mPresentFollowEit = new Eit(
                    tunerId,
                    Table.EIT_PRESENT_FOLLOWING_TABLE_ID,
                    null, 0);
        }
    }
    private void startSchedulerEit(int tp_id, int tunerId){
        PesiDtvFrameworkInterface dtvFramework = PesiDtvFramework.getInstance(mContext);
        dtvFramework.startScheduleEit(tp_id,tunerId);
    }
    private void stopSchedulerEit(int tunerId){
        if(mScheduleEit != null){
            mScheduleEit.abort();
            mScheduleEit.cleanup();
            mScheduleEit = null;
        }
    }
    public void mpeg_set_play_status(int status) {
        LogUtils.d("status = "+status);
        mPesiplayStatus = status;
    }

    public int mpeg_get_play_status() {
        return mPesiplayStatus;
    }

    private synchronized void waitForSurface(pesi_avplay avplay) {
        if (mIsWaitingForSurface) {
            return;
        }

        mIsWaitingForSurface = true;
        new Thread(() -> {
            while (mTvViewSurface == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mIsWaitingForSurface = false;
            playAv(avplay.TunerId,avplay.ChannelID);
        },"waitForSurface").start();
    }

    public void BaseHandleMessage(Message msg, Handler handler) {
        if (handler != null) {
            mPlayHandler = handler;
        }
        switch(msg.what) {
            case PESI_PLAY_AV: {
                Log.d(TAG,"PESI_PLAY_AV obj="+msg.obj.toString());

                pesi_avplay avplay = (pesi_avplay)msg.obj;
                playAv(avplay.TunerId,avplay.ChannelID);
                // callback example
            }break;
            case PESI_STOP_AV: {
                Log.d(TAG,"PESI_STOP_AV obj="+msg.obj.toString());
                pesi_avplay avplay = (pesi_avplay)msg.obj;
                stopAv(mProgramInfo == null ? 0 : mProgramInfo.getTunerId(), avplay.stop_mode, avplay.stop_monitor_table );
            }break;
            case PESI_PLAY_PIP: {
                Log.d(TAG,"PESI_PLAY_PIP dose not impletement !!!!");
            }break;
            case PESI_STOP_PIP: {
                Log.d(TAG,"PESI_STOP_PIP dose not impletement !!!!");
            }break;
            case PESI_PRE_START: {
                Log.d(TAG,"PESI_PRE_START dose not impletement !!!!");
            }break;
            case PESI_VIDEO_COUNT_CHECK: {
//                Log.d(TAG,"PESI_VIDEO_COUNT_CHECK !!!!");
                // clear the handler for those messages with what = 0
                // 20210911 johnny, now remove messages in closeVideoCountCheck()
//                handler.removeMessages(PESI_VIDEO_COUNT_CHECK);
                if (msg.arg1 == VIDEO_COUNT_CHECK_ON && mProgramInfo != null) {
					if (mPlayer != null){
						boolean isLock = mTunerInterface.isLock(mPlayer.getTunerId());
						if(isLock) {
                            int video_status = mPlayer.isNoVideo();
							if(video_status == 0)
								mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_FRONTEND_STOP, (int) mProgramInfo.getChannelId(), 0, null);
							else if(video_status == 1 && !is_e213(mProgramInfo.getChannelId()))
								mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_FRONTEND_RESUME, (int) mProgramInfo.getChannelId(), 0, null);
						}
					}

                    Message message = new Message();
                    message.copyFrom(msg);
                    handler.sendMessageDelayed(message, 1000);
                }
            }break;
            case PESI_START_DVB_SUBTITLE:{
                pesi_subtitle_cmd subtitle_cmd = (pesi_subtitle_cmd)msg.obj;
                mSubtitleManager.stoptSubtitle();
                mSubtitleManager.setCCLayout(mLiveTvLayout);
                mSubtitleManager.startSubtitle(subtitle_cmd.subtitling_type,subtitle_cmd.TunerId, subtitle_cmd.subtitle_pid, subtitle_cmd.subtitle_com_page_id, subtitle_cmd.subtitle_anc_page_id);
            }break;
            case PESI_STOP_DVB_SUBTITLE:{
                mSubtitleManager.stoptSubtitle();
            }break;
            case PESI_CHANGE_AUDIO:{
                pesi_audio_command audio_command = (pesi_audio_command)msg.obj;
                mPlayer.SwitchAudio(audio_command.Audio_pid);
                mDatamanager.updateProgramInfo(mPlayer.getProgramInfo());
                mDatamanager.DataManagerSaveProgramData(mPlayer.getProgramInfo());
            }break;
            case PESI_SET_FCC: {
                set_fcc(msg.arg1, msg.arg2, 0);
            } break;
            case PESI_CLEAR_FCC: {
                clear_fcc(msg.arg1, msg.arg2);
            } break;
            case PESI_STOP_AV_ALL:{
                stopAV();
            }break;
            case PESI_PLAY_AV_NEW:{
                closeVideoCountCheck(handler);
                AvPlayNew((pesi_avplay_new)msg.obj);
                if(((pesi_avplay_new) msg.obj).channelBlocked == false){
                    openVideoCountCheck(handler);
                }
            }break;
            case PESI_STOP_All_EIT_ON_CURRENT_TP:{
                Log.d(TAG,"PESI_STOP_All_EIT_ON_CURRENT_TP");
                pesi_avplay avplay = (pesi_avplay)msg.obj;
                stopAllEitTableOnCurrentTp(avplay.TunerId);;
            }break;
            case PESI_RESET_AUDIO_DEFAULT_LANGUAGE: {
                String lang = (String)msg.obj;
                mDatamanager.resetAllProgramAudioDefaultLanguage(lang);
            }break;
            case PVR_START_RECORD:{
                int recordTunerId;
                PvrDbRecordInfo dbRecordInfo ;
                Log.d(TAG,"PVR_START_RECORD");
                dbRecordInfo = (PvrDbRecordInfo)msg.obj;
                recordTunerId = msg.arg1;

                // getAvailableIndex here to get different index if called twice at the same time
                int isSeries = dbRecordInfo.getIsSeries() ? 1 : 0;
                PvrRecIdx recIdx = new PvrDataManager(mContext).getAvailableIndex(isSeries, dbRecordInfo.getSeriesKey());
                String recPath = new PvrDataManager(mContext).preCreateDirectories(isSeries, recIdx);
                dbRecordInfo.setMasterIdx(recIdx.getMasterIdx());
                dbRecordInfo.setSeriesIdx(recIdx.getSeriesIdx());
                dbRecordInfo.setFullNamePath(recPath);
                recordStart(dbRecordInfo,recordTunerId);
            }break;
            case PVR_STOP_RECORD:{
                int recordTunerId;
                Log.d(TAG,"PVR_STOP_RECORD");
                recordTunerId = msg.arg1;
                recordStop(recordTunerId);
            }break;
            case PVR_STOP_ALLRECORD:{
                Log.d(TAG,"PVR_STOP_ALLRECORD");
                recordAllStop();
            }break;
            case PVR_START_TIMESHIFT:{
                int recordId,playId;
                PvrDbRecordInfo dbRecordInfo ;
                Log.d(TAG,"PVR_START_TIMESHIFT");
                dbRecordInfo = (PvrDbRecordInfo)msg.obj;
                recordId = msg.arg1;
                playId = msg.arg2;
                timeshiftStart(dbRecordInfo,recordId,playId);
            }break;
            case PVR_TIMESHIFT_PLAY_RESUME:{
                int playTunerId;
                Log.d(TAG,"PVR_TIMESHIFT_PLAY_RESUME");
                playTunerId = msg.arg1;
                timeshiftPlayerResume(playTunerId);
            }break;
            case PVR_TIMESHIFT_PLAY_PAUSE:{
                int playTunerId;
                Log.d(TAG,"PVR_TIMESHIFT_PLAY_PAUSE");
                playTunerId = msg.arg1;
                timeshiftPlayerPause(playTunerId);
            }break;
            case PVR_TIMESHIFT_PLAY_SEEK:{
                int playTunerId,seekTime;
                Log.d(TAG,"PVR_TIMESHIFT_PLAY_SEEK");
                playTunerId = msg.arg1;
                seekTime = msg.arg2;
                timeshiftPlaySeek(playTunerId,seekTime);
            }break;
            case PVR_TIMESHIFT_PLAY_FF:{
                int playTunerId;
                Log.d(TAG,"PVR_TIMESHIFT_PLAY_FF");
                playTunerId = msg.arg1;
                timeshiftFastForward(playTunerId);
            }break;
            case PVR_TIMESHIFT_PLAY_RW:{
                int playTunerId;
                Log.d(TAG,"PVR_TIMESHIFT_PLAY_RW");
                playTunerId = msg.arg1;
                timeshiftBackForward(playTunerId);
            }break;
            case PVR_STOP_TIMESHIFT:{
                Log.d(TAG,"PVR_STOP_TIMESHIFT");
                timeshiftStop();
            }break;
            case PVR_START_PLAYBACK:{
                int playTunerId,lastPositionFlag;
                PvrRecFileInfo pvrRecFileInfo ;
                Log.d(TAG,"PVR_START_PLAYBACK");
                pvrRecFileInfo = (PvrRecFileInfo)msg.obj;
                playTunerId = msg.arg1;
                lastPositionFlag = msg.arg2;
                playbackStart( pvrRecFileInfo, lastPositionFlag,  playTunerId);
            }break;
            case PVR_STOP_PLAYBACK:{
                int playTunerId;
                Log.d(TAG,"PVR_STOP_PLAYBACK");
                playTunerId = msg.arg1;
                playbackStop(playTunerId);
            }break;
            case PVR_PLAYBACK_PLAY:{
                int playTunerId;
                Log.d(TAG,"PVR_PLAYBACK_PLAY");
                playTunerId = msg.arg1;
                playerResume(playTunerId);
            }break;
            case PVR_PLAYBACK_PAUSE:{
                int playTunerId;
                Log.d(TAG,"PVR_PLAYBACK_PAUSE");
                playTunerId = msg.arg1;
                playerPause(playTunerId);
            }break;
            case PVR_PLAYBACK_SEEK:{
                int playTunerId,seekTime;
                Log.d(TAG,"PVR_PLAYBACK_SEEK");
                playTunerId = msg.arg1;
                seekTime = msg.arg2;
                playSeek(playTunerId,seekTime);
            }break;
            case PVR_PLAYBACK_FF:{
                int playTunerId;
                Log.d(TAG,"PVR_PLAYBACK_FF");
                playTunerId = msg.arg1;
                fastForward(playTunerId);
            }break;
            case PVR_PLAYBACK_RW:{
                int playTunerId;
                Log.d(TAG,"PVR_PLAYBACK_RW");
                playTunerId = msg.arg1;
                backForward(playTunerId);
            }break;
            case PVR_PLAYBACK_SET_SPEED:{
                int playTunerId,speed;
                Log.d(TAG,"PVR_PLAYBACK_SET_SPEED");
                playTunerId = msg.arg1;
                speed = msg.arg2;
                playSetSpeed(playTunerId, PvrInfo.EnPlaySpeed.toFloatSpeed(speed));
            }break;
            case PVR_PLAYBACK_CHANGE_AUDIO_TRACK:{
                int playTunerId;
                AudioInfo.AudioComponent audioComponent;
                Log.d(TAG,"PVR_PLAYBACK_CHANGE_AUDIO_TRACK");
                audioComponent = (AudioInfo.AudioComponent)msg.obj;
                playTunerId = msg.arg1;
                changeAuidoTrack(playTunerId,audioComponent,DvrDevManager.PLAYBACK);
            }break;
            case PVR_TIMESHIFT_PLAY_CHANGE_AUDIO_TRACK:{
                int playTunerId;
                AudioInfo.AudioComponent audioComponent;
                Log.d(TAG,"PVR_TIMESHIFT_PLAY_CHANGE_AUDIO_TRACK");
                audioComponent = (AudioInfo.AudioComponent)msg.obj;
                playTunerId = msg.arg1;
                changeAuidoTrack(playTunerId,audioComponent,DvrDevManager.TIMESHIFT);
            }break;
            case PVR_STOP_ALL:{
                Log.d(TAG,"PVR_STOP_ALL");
                PvrContentProvider.closeDatabase();
                pvrStopAll();
            }break;
            default: {
                Log.d(TAG,"unknow msg what = "+msg.what);
            }
        }
    }

    public int getCurrentSubtitlePid() {
        return mSubtitleManager.getCurrentSubtitlePid();
    }

    public void AvPlayNew(pesi_avplay_new avplay){
        if(avplay == null){
            LogUtils.e("NULL POINTER, return !!!!!!!!!!!!!!!!!!!!");
            return;
        }
        int mode = avplay.mode;
        LogUtils.d("[FCC2] mode "+mode+" BlockedChannel "+avplay.channelBlocked);
        LogUtils.d("[FCC2] avplay_info "+avplay.channelIdList+" "+avplay.tunerIdList);
        switch(mode){
            case pesi_avplay_new.MODE_INIT:{
                //stopAv
                //playav
                //set fcc1 down channel
                //set fcc2 up channel
                for(int i=0 ; i<avplay.channelIdList.size() ; i++){
                    long ch_id = avplay.channelIdList.get(i);
                    int tuner_id = avplay.tunerIdList.get(i);
                        if (i == 0) stopAv(tuner_id, Pvcfg.isFccEnable() ? 1 : 0, 1);
                        if (i == 1 && !avplay.channelBlocked) playAv(tuner_id, ch_id);
                        if (Pvcfg.isFccEnable()) {
                            if (i == 2) set_fcc(tuner_id, ch_id, 1);
                            if (i == 3) set_fcc(tuner_id, ch_id, 0);
                        }
                }
            }break;
            case pesi_avplay_new.MODE_FCC:{
                //stop av
                //avplay
                //clear fcc
                //set fcc1
                //ser fcc2
                if (Pvcfg.isFccEnable()) {
                    sendFccVisibleEvent(-1, "MODE_FCC pre-stop");
                }
                if(Pvcfg.isFccV3Enable()){
                    long ch_id = avplay.channelIdList.get(1);
                    int tuner_id = avplay.tunerIdList.get(1);
                    if(!avplay.channelBlocked) playAv(tuner_id, ch_id);
                }
                LogUtils.d("FCC_LOG MODE_FCC enter: blocked=" + avplay.channelBlocked +
                        " chList=" + avplay.channelIdList + " tunerList=" + avplay.tunerIdList);
                for(int i=0 ; i<avplay.channelIdList.size() ; i++){
                    long ch_id = avplay.channelIdList.get(i);
                    int tuner_id = avplay.tunerIdList.get(i);
                    LogUtils.d("FCC_LOG MODE_FCC step: index=" + i +
                            " chId=" + ch_id + " tunerId=" + tuner_id +
                            " role=" + (i == 0 ? "stopAv(old)" :
                            (i == 1 ? "playAv(new)" :
                            (i == 2 ? "clear_fcc(bg)" :
                            (i == 3 ? "set_fcc(next)" :
                            (i == 4 ? "set_fcc(prev)" : "other"))))));
                    if (i == 0) stopAv(tuner_id, Pvcfg.isFccEnable() ? 1 : 0, 1);
                    if (i == 1 && !avplay.channelBlocked){
                        if(Pvcfg.isFccV3Enable())
                            mpeg_set_play_status(PESI_SVR_AV_LIVEPLAY_STATE);
                        else
                            playAv(tuner_id, ch_id);
                        if (Pvcfg.isFccEnable()) {
                            sendFccVisibleEvent(tuner_id, "MODE_FCC post-play");
                        }
                    }
                    if (Pvcfg.isFccEnable()) {
                        if (i == 2) clear_fcc(tuner_id, ch_id);
                        if (i == 3) set_fcc(tuner_id, ch_id, 1);
                        if (i == 4) set_fcc(tuner_id, ch_id, 0);
                    }
                }
            }break;
            case pesi_avplay_new.MODE_RESET:{
                //stop All
                //playav
                //set fcc1
                //set fcc2
                stopAV(); // this will send PESI_EVT_SYSTEM_STOP_MONITOR_TABLE
//                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_EVT_SYSTEM_STOP_MONITOR_TABLE, 0, 0,(long) -1);
                for(int i=0 ; i<avplay.channelIdList.size() ; i++){
                    long ch_id = avplay.channelIdList.get(i);
                    int tuner_id = avplay.tunerIdList.get(i);
                    //if(i == 0) stopAv(tuner_id, Pvcfg.isFccEnable()?1:0, 1);
                    if (i == 1 && !avplay.channelBlocked) playAv(tuner_id, ch_id);
                    if (Pvcfg.isFccEnable()) {
                        if (i == 2) set_fcc(tuner_id, ch_id, 1);
                        if (i == 3) set_fcc(tuner_id, ch_id, 0);
                    }
                }
            }break;
        }
    }

    private void sendFccVisibleEvent(int tunerId, String reason) {
        if (mPesiDtvCallback == null) {
            LogUtils.d("FCC_LOG sendFccVisibleEvent skip tunerId=" + tunerId +
                    " reason=" + reason + " callback=null");
            return;
        }
        LogUtils.d("FCC_LOG sendFccVisibleEvent tunerId=" + tunerId + " reason=" + reason);
        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_FCC_VISIBLE, tunerId, 0, null);
    }

    public void openVideoCountCheck(Handler handler) {
        if(handler != null) {
            LogUtils.d("IN");
            // 20210911 johnny, remove messages before start new PESI_VIDEO_COUNT_CHECK
            // to ensure there is only one PESI_VIDEO_COUNT_CHECK
            handler.removeMessages(PESI_VIDEO_COUNT_CHECK);
            Message msg = Message.obtain();
            msg.what = PESI_VIDEO_COUNT_CHECK;
            msg.arg1 = VIDEO_COUNT_CHECK_ON;

            // first video count check after 6 secs
            // because player may need some time to play
            handler.sendMessageDelayed(msg,6000);
        }
    }

    public void closeVideoCountCheck(Handler handler) {
        if(handler != null){
            LogUtils.d("IN");
            // 20210911 johnny, just remove messages to stop PESI_VIDEO_COUNT_CHECK
//            Message msg = Message.obtain();
//            msg.what = PESI_VIDEO_COUNT_CHECK;
//            msg.arg1 = VIDEO_COUNT_CHECK_OFF;
//            handler.sendMessage(msg);
            handler.removeMessages(PESI_VIDEO_COUNT_CHECK);
        }
    }

    public void destroy() {
        Log.d(TAG, "NEED TO CHECK TUNERID!!!!!!!!!!!!!!!!!!");
        for (int i = 0 ; i < mPlayers.length ; i++) {
            stopAv(i, 0,1);
        }

    }

    private void sendWvCasError(int errorCode, String msg, long channelId) {
        LogUtils.d("[CheckErrorMsg] errorCode = "+errorCode+" channelId = "+channelId);
        if(mProgramInfo != null){
            LogUtils.d("[CheckErrorMsg] mProgramInfo.getChannelId() = "+mProgramInfo.getChannelId());
        }
        // only send error if channel id is current channel
        if (mPesiDtvCallback != null
                && mProgramInfo != null
                /*&& channelId == mProgramInfo.getChannelId()*/) {

            closeVideoCountCheck(null);
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_EVT_CA_WIDEVINE_ERROR, errorCode, 0, msg);
        }
    }

    public DvrDevManager[] getDvrDevManagerList() {
        return mDvrDevManagerList;
    }

    private void timeshiftStart(PvrDbRecordInfo mDbRecordInfo, int recordTunerId, int playTunerId) {
        Log.d(TAG,"timeshiftStart start, recordTunerId="+recordTunerId+" playTunerId="+playTunerId);
        int index=0xff,i,count;
        if(recordTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"recordTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_ERROR, 0, 0, null);
            return ;
        }

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_ERROR, 0, 0, null);
            return ;
        }

        if(playTunerId == recordTunerId) {
            Log.d(TAG,"playTunerId and recordTunerId cannot be the same");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_ERROR, 0, 0, null);
            return ;
        }

        if(mDvrDevManagerList[0] != null && mDvrDevManagerList[1] != null && mDvrDevManagerList[2] != null) {
            Log.d(TAG,"The mPvrDevInfoList[] has no resources available");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++) {
            if (mDvrDevManagerList[i] == null) {
                index = i;
                break;
            }
        }

        if(index >= 3){
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_ERROR, 0, 0, null);
            return ;
        }

        count=0;
        while(mPlayers[recordTunerId] != null) {
            Log.d(TAG,"mPlayers["+recordTunerId+"]!=null, delay 200 ms");
        try {
                Thread.sleep(200);
        } catch (InterruptedException e) {
                Log.d(TAG, "sleep interrupted");
            }
            count ++;
            if(count >= 10){
                Log.d(TAG, "It has been waiting for more than 2000 ms seconds. No more waiting.");
                break;
            }
        }

        mDvrDevManagerList[index] = new DvrDevManager(DvrDevManager.TIMESHIFT,mContext,mPesiDtvCallback,mExecutor);
        mDvrDevManagerList[index].setSurface(mSurfaceHolder[playTunerId].getSurface());

        if(mDvrDevManagerList[index].timeshiftStart(mDbRecordInfo,recordTunerId,playTunerId) == DvrDevManager.FAILURE){
            mDvrDevManagerList[index] = null;
        }
        Log.d(TAG,"timeshiftStart end");
    }

    private void timeshiftPlayerResume(int playTunerId) {
        int index=0xff,i;
        Log.d(TAG,"timeshiftPlayerResume start");

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_RESUME_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTimeshiftPlaybackTunerId() == playTunerId &&
                        mDvrDevManagerList[i].getDvrPlaybackController() != null &&
                        mDvrDevManagerList[i].getDvrRecorderController().getRecordMode() == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                    index = i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_RESUME_ERROR, 0, 0, null);
            return ;
        }

        mDvrDevManagerList[index].timeshiftPlayResume(playTunerId);

        Log.d(TAG,"timeshiftPlayerResume end");
    }

    private void timeshiftPlayerPause(int playTunerId) {
        int index=0xff,i;
        Log.d(TAG,"timeshiftPlayerPause start");

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_PAUSE_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTimeshiftPlaybackTunerId() == playTunerId &&
                        mDvrDevManagerList[i].getDvrPlaybackController() != null &&
                        mDvrDevManagerList[i].getDvrRecorderController().getRecordMode() == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                    index = i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_PAUSE_ERROR, 0, 0, null);
            return ;
        }
        mDvrDevManagerList[index].timeshiftPlayPause(playTunerId);

        Log.d(TAG,"timeshiftPlayerPause end");
    }

    private void timeshiftFastForward(int playTunerId) {
        int index=0xff,i;
        Log.d(TAG,"timeshiftFastForward start");

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_FF_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTimeshiftPlaybackTunerId() == playTunerId &&
                        mDvrDevManagerList[i].getDvrPlaybackController() != null &&
                        mDvrDevManagerList[i].getDvrPlaybackController().getRecordMode() == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                    index = i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_FF_ERROR, 0, 0, null);
            return ;
        }

        mDvrDevManagerList[index].timeshiftPlayFastForward(playTunerId);

        Log.d(TAG,"timeshiftFastForward end");
    }

    private void timeshiftBackForward(int playTunerId) {
        int index=0xff,i;
        Log.d(TAG,"timeshiftBackForward start");

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_RW_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTimeshiftPlaybackTunerId() == playTunerId &&
                        mDvrDevManagerList[i].getDvrPlaybackController() != null &&
                        mDvrDevManagerList[i].getDvrRecorderController().getRecordMode() == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                    index = i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_RW_ERROR, 0, 0, null);
            return ;
        }

        mDvrDevManagerList[index].timeshiftPlayRewind(playTunerId);

        Log.d(TAG,"timeshiftBackForward end");
    }

    private void timeshiftPlaySeek(int playTunerId,int seekTime) {
        int index=0xff,i;
        Log.d(TAG,"timeshiftPlaySeek start");

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_SEEK_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTimeshiftPlaybackTunerId() == playTunerId &&
                        mDvrDevManagerList[i].getDvrPlaybackController() != null &&
                        mDvrDevManagerList[i].getDvrRecorderController().getRecordMode() == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                    index = i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_SEEK_ERROR, 0, 0, null);
            return ;
        }

        mDvrDevManagerList[index].timeshiftPlaySeek(playTunerId,seekTime);

        Log.d(TAG,"timeshiftPlaySeek end");
    }

    private void timeshiftStop() {
        int index=0xff,i;

        Log.d(TAG,"timeshiftStop start");

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getRecMode() == DvrDevManager.TIMESHIFT) {
                    index=i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_STOP_ERROR, 0, 0, null);
            return ;
        }

        mDvrDevManagerList[index].timeshiftStop();
        mDvrDevManagerList[index] = null;

        Log.d(TAG,"timeshiftStop end");

        }

    private void recordStart(PvrDbRecordInfo mDbRecordInfo, int recordTunerId) {
        int index=0xff,i,count;

        Log.d(TAG,"recordStart start, recordTunerId="+recordTunerId);

        if(recordTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"recordTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_REC_ERROR, recordTunerId, (int)mDbRecordInfo.getChannelId(), null);
            return ;
        }
        if(mDvrDevManagerList[0] != null && mDvrDevManagerList[1] != null && mDvrDevManagerList[2] != null) {
            Log.d(TAG,"The mPvrDevInfoList[] has no resources available");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_REC_ERROR, recordTunerId, (int)mDbRecordInfo.getChannelId(), null);
            return ;
        }
        for(i=0;i<3;i++) {
            if (mDvrDevManagerList[i] == null) {
                index = i;
                break;
            }
        }

        if(index >= 3){
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_REC_ERROR, 0, 0, null);
            return ;
        }

        count=0;
        while(mPlayers[recordTunerId] != null) {
            Log.d(TAG,"mPlayers["+recordTunerId+"]!=null, delay 200 ms");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.d(TAG, "sleep interrupted");
            }
            count ++;
            if(count >= 10){
                Log.d(TAG, "It has been waiting for more than 2000 ms seconds. No more waiting.");
                break;
        }
        }

        mDvrDevManagerList[index] = new DvrDevManager(DvrDevManager.RECORD,mContext,mPesiDtvCallback,mExecutor);
        if(mDvrDevManagerList[index].recordStart(mDbRecordInfo,recordTunerId) == DvrDevManager.FAILURE){
            mDvrDevManagerList[index] = null;
        }

        Log.d(TAG,"recordStart end");
    }

    private void playbackStart(PvrRecFileInfo pvrRecFileInfo, int lastPositionFlag, int playTunerId){

        int index=0xff,i, recMode=DvrDevManager.PLAYBACK;
        DvrRecorderController dvrRecorderController=null;
        Log.d(TAG,"playbackStart start, playTunerId="+playTunerId);
        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_ERROR, 0, 0, null);
            return ;
        }

        if(mDvrDevManagerList[0] != null && mDvrDevManagerList[1] != null && mDvrDevManagerList[2] != null) {
            Log.d(TAG,"The mPvrDevInfoList[] has no resources available");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++) {
            if (mDvrDevManagerList[i] == null) {
                index = i;
                break;
            }
        }

        if(index >= 3){
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++) {
            if (mDvrDevManagerList[i] != null && i != index && mDvrDevManagerList[i].getRecMode() == DvrDevManager.RECORD) {
                if(mDvrDevManagerList[i].getPvrRecIdx().equals(pvrRecFileInfo.getPvrRecIdx())) {
                    recMode = DvrDevManager.PLAYRECORDING;
                    dvrRecorderController=mDvrDevManagerList[i].getDvrRecorderController();
                    Log.d(TAG,"mDvrDevManagerList["+i+"]..getDvrRecorderController()="+dvrRecorderController);
                    break;
                }
            }
        }

        mDvrDevManagerList[index] = new DvrDevManager(recMode,mContext,mPesiDtvCallback,mExecutor);
        mDvrDevManagerList[index].setSurface(gSurface[playTunerId]);
        mDvrDevManagerList[index].setDvrRecorderControllerTmp(dvrRecorderController);

        if(mDvrDevManagerList[index].playbackStart(pvrRecFileInfo,lastPositionFlag,playTunerId) == DvrDevManager.FAILURE){
            mDvrDevManagerList[index] = null;
        }

        Log.d(TAG,"playbackStart end");
    }

    private void playerResume(int playTunerId) {
        int index=0xff,i;
        Log.d(TAG,"playerResume start, playTunerId="+playTunerId);

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_RESUME_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTunerId() == playTunerId && mDvrDevManagerList[i].getDvrPlaybackController() != null) {
                    index=i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_RESUME_ERROR, 0, 0, null);
            return ;
        }

        mDvrDevManagerList[index].playbackResume(playTunerId);
        Log.d(TAG,"playerResume end");

    }

    private void playerPause(int playTunerId) {
        int index=0xff,i;
        Log.d(TAG,"playerPause start, playTunerId="+playTunerId);

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_PAUSE_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTunerId() == playTunerId && mDvrDevManagerList[i].getDvrPlaybackController() != null) {
                    index=i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_PAUSE_ERROR, 0, 0, null);
            return ;
        }

        mDvrDevManagerList[index].playbackPause(playTunerId);
        Log.d(TAG,"playerPause end");

    }

    private void fastForward(int playTunerId) {
        int index=0xff,i;
        Log.d(TAG,"fastForward start, playTunerId="+playTunerId);

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_FF_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTunerId() == playTunerId && mDvrDevManagerList[i].getDvrPlaybackController() != null) {
                    index=i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_FF_ERROR, 0, 0, null);
            return ;
        }

        mDvrDevManagerList[index].playbackFastForward(playTunerId);
        Log.d(TAG,"fastForward end");
    }

    private void backForward(int playTunerId) {
        int index=0xff,i;
        Log.d(TAG,"backForward start, playTunerId="+playTunerId);

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_RW_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTunerId() == playTunerId && mDvrDevManagerList[i].getDvrPlaybackController() != null) {
                    index=i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_RW_ERROR, 0, 0, null);
            return ;
        }

        mDvrDevManagerList[index].playbackRewind(playTunerId);
        Log.d(TAG,"backForward end");
    }

    private void playSeek(int playTunerId,int seekTime) {
        int index=0xff,i;
        Log.d(TAG,"playSeek start, playTunerId="+playTunerId);

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SEEK_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTunerId() == playTunerId && mDvrDevManagerList[i].getDvrPlaybackController() != null) {
                    index=i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SEEK_ERROR, 0, 0, null);
            return ;
        }

        mDvrDevManagerList[index].playbackSeek(playTunerId,seekTime);
        Log.d(TAG,"playSeek end");
    }

    private void playSetSpeed(int playTunerId,float speed) {
        int index=0xff,i;
        Log.d(TAG,"playSetSpeed start, playTunerId="+playTunerId);

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SET_SPEED_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTunerId() == playTunerId && mDvrDevManagerList[i].getDvrPlaybackController() != null) {
                    index=i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SET_SPEED_ERROR, 0, 0, null);
            return ;
        }

        mDvrDevManagerList[index].playbackSetSpeed(playTunerId,speed);
        Log.d(TAG,"playSetSpeed end");
    }

    private void changeAuidoTrack(int playTunerId,AudioInfo.AudioComponent audioComponent,int recMode ) {

        int index=0xff,i;
        Log.d(TAG,"changeAuidoTrack start, playTunerId="+playTunerId);

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            if(recMode == DvrDevManager.PLAYBACK)
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_CHANGE_AUDIO_TRACK_ERROR, 0, 0, null);
            else
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTunerId() == playTunerId && mDvrDevManagerList[i].getDvrPlaybackController() != null) {
                    index=i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            if(recMode == DvrDevManager.PLAYBACK)
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_CHANGE_AUDIO_TRACK_ERROR, 0, 0, null);
            else
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_ERROR, 0, 0, null);
            return ;
        }

        mDvrDevManagerList[index].changeAuidoTrack(playTunerId,audioComponent,recMode);
        Log.d(TAG,"changeAuidoTrack end");
        }

    private void recordStop(int recordTunerId) {

        int index=0xff,i;

        Log.d(TAG,"recordStop start, recordTunerId="+recordTunerId);

        if(recordTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"recordTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_RECORDING_STOP_ERROR, recordTunerId, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTunerId() == recordTunerId && mDvrDevManagerList[i].getDvrRecorderController() != null) {
                    index=i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mDvrDevManagerList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_RECORDING_STOP_ERROR, recordTunerId, 0, null);
            return ;
        }

        mDvrDevManagerList[index].recordStop(recordTunerId);
        mDvrDevManagerList[index] = null;

        Log.d(TAG,"recordStop end");
    }

    void pvrStopAll(){
        Log.d(TAG,"pvrStopAll start");
        for(int i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null ) {
                if((mDvrDevManagerList[i].getDvrRecorderController() != null)) {
                    if(mDvrDevManagerList[i].getRecMode() == DvrDevManager.RECORD) {
                        Log.d(TAG,"recordStop start");
                        mDvrDevManagerList[i].recordStop(mDvrDevManagerList[i].getTunerId());
                    }
                    else if(mDvrDevManagerList[i].getRecMode() == DvrDevManager.TIMESHIFT) {
                        Log.d(TAG,"timeshiftStop start");
                        mDvrDevManagerList[i].timeshiftStop();
                    }
                }
                if(mDvrDevManagerList[i].getDvrPlaybackController() != null){
                    Log.d(TAG,"playbackStop start");
                    mDvrDevManagerList[i].playbackStop(mDvrDevManagerList[i].getTunerId());
                }
            }
        }
        Log.d(TAG,"pvrStopAll End");
    }
    void recordAllStop(){

        Log.d(TAG,"recordAllStop start");
        int i;
        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null ) {
                if((mDvrDevManagerList[i].getDvrRecorderController() != null) && (mDvrDevManagerList[i].getRecMode() == DvrDevManager.RECORD)) {
                recordStop(mDvrDevManagerList[i].getTunerId());
                //wv2RecordStopCasPlay();
            }
        }
        }

        Log.d(TAG,"recordAllStop end");
    }

    void playbackStop(int playTunerId){
        int index=0xff,i;
        Log.d(TAG,"playbackStop start, playTunerId="+playTunerId);

        if(playTunerId >= TunerInterface.NUMBER_OF_TUNER) {
            Log.d(TAG,"playTunerId >= TunerInterface.NUMBER_OF_TUNER");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAYBACK_STOP_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<3;i++){
            if(mDvrDevManagerList[i] != null) {
                if (mDvrDevManagerList[i].getTunerId() == playTunerId && mDvrDevManagerList[i].getDvrPlaybackController() != null) {
                    index=i;
                    break;
                }
            }
        }

        if( index > 2 ) {
            Log.d(TAG,"Unable to find the correct devices from mPvrDevInfoList by playTunerId");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAYBACK_STOP_ERROR, 0, 0, null);
            return ;
        }

        mDvrDevManagerList[index].playbackStop(playTunerId);
        mDvrDevManagerList[index] = null;

        Log.d(TAG,"playbackStop end");
        }


    public static boolean is_e213(long ch_id) {
        ProgramInfo programInfo = mDatamanager.get_service_by_channelId(ch_id);
        if(programInfo == null){
            return false;
        }
//        Log.d("e213","is_e213 programInfo.pAudios.size() = "+programInfo.pAudios.size());
//        if(programInfo.pAudios.size() > 0)
//            Log.d("e213","is_e213 programInfo.pAudios.get(0).getPid() = "+programInfo.pAudios.get(0).getPid());
//        Log.d("e213","is_e213 programInfo.getType() = "+programInfo.getType());
//        Log.d("e213","is_e213 programInfo.pVideo.getPID() = "+programInfo.pVideo.getPID());
        if(programInfo.pAudios == null || programInfo.pAudios.size() == 0 || programInfo.pAudios.get(0).getPid() == 0 ||
                (programInfo.getType() == ProgramInfo.PROGRAM_TV && programInfo.pVideo.getPID() == 0)) {
//            Log.d(TAG,"is e213");
            return true;
        }
        else {
//            Log.d(TAG,"is not e213");
            return false;
        }
    }
    public void check_e213(ProgramInfo programInfo) {
        if(programInfo == null)
            return;
//        Log.d(TAG,"check_e213 programInfo = "+programInfo.ToString());
        if(is_e213(programInfo.getChannelId())) {
//            Log.d(TAG,"check_e213 PESI_SVR_EVT_AV_FRONTEND_STOP");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_FRONTEND_STOP, (int) programInfo.getChannelId(), 0, null);
        }
        else {
//            Log.d(TAG,"check_e213 PESI_SVR_EVT_AV_FRONTEND_RESUME");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_FRONTEND_RESUME, (int) programInfo.getChannelId(), 0, null);
        }
    }

    private void startSubtitle(pesi_subtitle_cmd subtitle_cmd) {
        mSubtitleManager.stoptSubtitle();
        mSubtitleManager.startSubtitle(subtitle_cmd.subtitling_type, subtitle_cmd.TunerId, subtitle_cmd.subtitle_pid, subtitle_cmd.subtitle_com_page_id, subtitle_cmd.subtitle_anc_page_id);
    }

    public void setSetVisibleCompleted(boolean isVisibleCompleted) {
        for(int i = 0;i < 3; i++) {
            mSetVisibleCompleted[i] = isVisibleCompleted;
            if(mPlayers[i] != null)
                mPlayers[i].setSetVisibleCompleted(isVisibleCompleted);
        }
    }

    public static PmtNotifyCa getPlayerPmtNotifyCa(int tuner_id) {
        if (tuner_id < 0 || tuner_id >= mPlayersPmtNotifyCa.length) {
            Log.e(TAG, "getPlayerPmtNotifyCa: invalid tuner id");
            return null;
        }
        return mPlayersPmtNotifyCa[tuner_id];
    }

    public static void SetPlayerPmtNotifyCa(int tuner_id,long channel_id,boolean isPmtNotifyCa) {
        if (tuner_id < 0 || tuner_id >= mPlayersPmtNotifyCa.length) {
            Log.e(TAG, "SetPlayerPmtNotifyCa: invalid tuner id");
            return;
        }
        mPlayersPmtNotifyCa[tuner_id].setChannelId(channel_id);
        mPlayersPmtNotifyCa[tuner_id].setNotifyPmt(isPmtNotifyCa);
    }

    public boolean isStartSubtitle() {
        return mSubtitleManager.isStartSubtitle();
    }

    public void stopAllEitTableOnCurrentTp(int tuner_id) {
        stopPresentFollowEit();
        if(!Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)){
            stopSchedulerEit(tuner_id);
        }
    }

}
