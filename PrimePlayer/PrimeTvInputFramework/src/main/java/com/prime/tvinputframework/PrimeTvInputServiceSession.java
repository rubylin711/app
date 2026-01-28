package com.prime.tvinputframework;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.PlaybackParams;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.media.tv.TvTrackInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.prime.datastructure.CommuincateInterface.AvModule;
import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.ServiceDefine.AvCmdMiddle;
import com.prime.datastructure.TIF.TIFChannelData;
import com.prime.datastructure.TIF.TIFRecordedProgramData;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.AudioInfo;
import com.prime.datastructure.sysdata.PvrInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.datastructure.sysdata.SubtitleInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVMessage;
import com.prime.tvinputframework.subtitle.DvbSubtitleView;

import java.util.concurrent.TimeUnit;

/**
 * 完整版 PrimeTvInputServiceSession
 * - Surface 去抖（null → 延遲停播）
 * - pending tune（Surface 未到先記住 uri/params）
 * - 兩種切台：
 * (A) 依 TIF channel id
 * (B) 依 (sid,onid,tsid) 反查 ProgramInfo → channelId
 * - 音軌：
 * - mPlayId = AVTunerID（從 ChannelChangeManager 取得）
 * - 第一幀出來後抓 audio list → notifyTracksChanged / notifyTrackSelected
 * - onSelectTrack() 用 mPlayId 切音軌
 * - 字幕：
 * - Subtitle bitmap 由 TVMessage 傳進來，丟給 DvbSubtitleView
 * - 走 TIF subtitle track (TYPE_SUBTITLE) 列表 + onSelectTrack 切換
 * - onSetCaptionEnabled() 控制是否顯示字幕
 */
public class PrimeTvInputServiceSession extends TvInputService.Session {
    private static final String TAG = "PrimeTvInputServiceSession";
    private static final long SURFACE_NULL_GRACE_MS = 1200L;

    // 同一進程內，唯一的「正在播 AV 的 Session」
    private static volatile PrimeTvInputServiceSession sActiveOwner;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Context mContext;
    private final PrimeDtvServiceInterface mDtv;

    // 播放相關
    private int mPlayId = -1; // AVTunerId
    private int mRecordedProgramPlayTunerId = -1;
    private @Nullable Surface mSurface;
    private @Nullable Uri mPendingChannelUri;
    private @Nullable Uri mPendingRecordedProgramUri;
    private @Nullable Bundle mPendingParams;
    private boolean mIsPlaying = false;
    private boolean mIsPlayingRecordedProgram = false;
    private boolean mSidebandMode = false;

    // Subtitle overlay
    private @Nullable DvbSubtitleView mSubtitleView;
    private boolean mCaptionEnabled = false;

    // Track 管理器
    private final PrimeTrackController mTrackController;

    // 延遲停播（Surface=null 去抖）
    private final Runnable mDelayedStop = new Runnable() {
        @Override
        public void run() {
            int playId = (mPlayId >= 0) ? mPlayId : 0;
            int playStatus = AvCmdMiddle.PESI_SVR_AV_STOP_STATE;
            try {
                playStatus = mDtv.av_control_get_play_status(playId);
            } catch (Throwable t) {
                Log.w(TAG, "delayed-stop get_play_status error: ", t);
            }
            boolean shouldStop = (playStatus != AvCmdMiddle.PESI_SVR_AV_STOP_STATE) &&
                    (playStatus != AvCmdMiddle.PESI_SVR_AV_RELEASE_STATE) &&
                    (playStatus != AvCmdMiddle.PESI_SVR_AV_IDLE_STATE);
            if (mSurface == null && shouldStop && iAmOwner()) {
                Log.d(TAG, "delayed-stop (owner) @" + System.identityHashCode(this));
                try {
                    mDtv.av_control_play_stop(playId, 0, 1);
                } catch (Throwable t) {
                    Log.w(TAG, "delayed-stop error: ", t);
                } finally {
                    mIsPlaying = false;
                    notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
                }
            } else {
                Log.d(TAG, "delayed-stop ignored (status=" + playStatus + ", surface=" + mSurface +
                        ", owner=" + iAmOwner() + ") @" + System.identityHashCode(this));
            }
        }
    };

    public PrimeTvInputServiceSession(Context context) {
        super(context);
        mContext = context;
        mDtv = PrimeTvInputFrameworkApplication.get_prime_dtv_service();

        mTrackController = new PrimeTrackController(mDtv, new PrimeTrackController.Callback() {
            @Override
            public void notifyTracksChanged(java.util.List<TvTrackInfo> tracks) {
                PrimeTvInputServiceSession.this.notifyTracksChanged(tracks);
            }

            @Override
            public void notifyTrackSelected(int type, @Nullable String trackId) {
                PrimeTvInputServiceSession.this.notifyTrackSelected(type, trackId);
            }

            @Override
            public int ensurePlayId() {
                return ensurePlayIdInternal();
            }

            @Override
            public boolean isCaptionEnabled() {
                return mCaptionEnabled;
            }
        });

        Log.d(TAG, "Session created @" + System.identityHashCode(this));
    }

    private boolean iAmOwner() {
        return sActiveOwner == this;
    }

    private void takeOwnership() {
        sActiveOwner = this;
    }

    // ===========================
    // Surface / Tune / Playback
    // ===========================

    @Override
    public boolean onSetSurface(@Nullable Surface surface) {
        LogUtils.d("FCC_LOG TvView onSetSurface: surface=" + surface +
                " surfaceId=" + (surface == null ? 0 : System.identityHashCode(surface)));

        if (surface == null) {
            mSurface = null;
            mHandler.removeCallbacks(mDelayedStop);
            mHandler.postDelayed(mDelayedStop, SURFACE_NULL_GRACE_MS);

            if (mIsPlayingRecordedProgram) {
                stopRecordedProgramInternal();
            }

            return true;
        }

        takeOwnership();
        mSurface = surface;
        mHandler.removeCallbacks(mDelayedStop);
        try {
            if (mSidebandMode) {
                Log.d(TAG, "onSetSurface: SIDEBAND_MODE active, skip TvView set_surface");
            } else if (mIsPlaying || mIsPlayingRecordedProgram) {
                LogUtils.d("FCC_LOG TvView set_surface: index=-1 surface=" + surface +
                        " surfaceId=" + System.identityHashCode(surface));
                mDtv.set_surface(null, surface, -1);
            } else {
                Log.d(TAG, "onSetSurface: defer set_surface until onTune");
            }
        } catch (Throwable t) {
            Log.w(TAG, "set_surface error: ", t);
        }

        if (mPendingChannelUri != null) {
            Log.d(TAG, "resume pending tune: " + mPendingChannelUri);
            startPlaybackInternal(mPendingChannelUri, mPendingParams);
        }

        if (mPendingRecordedProgramUri != null) {
            Log.d(TAG, "onSetSurface: play pending recorded program");
            playRecordedProgramInternal(mPendingRecordedProgramUri);
            mPendingRecordedProgramUri = null;
        }
        return true;
    }

    @Override
    public void onSetStreamVolume(float volume) {
        Log.d(TAG, "onSetStreamVolume: " + volume);
        // 若有需要，可轉給 HAL
    }

    @Override
    public boolean onTune(Uri channelUri) {
        return onTune(channelUri, null);
    }


    @Override
    public boolean onTune(Uri channelUri, @Nullable Bundle params) {
        Log.d(TAG, "primetif onTune: uri=" + channelUri + " params=" + (params == null ? "null" : params));

        if (params != null && params.containsKey("SIDEBAND_MODE")) {
            mSidebandMode = params.getBoolean("SIDEBAND_MODE");
            if (mSidebandMode) {
                Log.d(TAG, "onTune: SIDEBAND_MODE enabled");
            }
        } else {
            mSidebandMode = false;
        }
        if (mSurface != null) {
            try {
                if (!mSidebandMode) {
                    LogUtils.d("FCC_LOG onTune set_surface: index=-1 surface=" + mSurface +
                            " surfaceId=" + System.identityHashCode(mSurface));
                    mDtv.set_surface(null, mSurface, -1);
                } else {
                    Log.d(TAG, "onTune: SIDEBAND_MODE active, skip TvView set_surface");
                }
            } catch (Throwable t) {
                Log.w(TAG, "onTune set_surface error: ", t);
            }
        }

        notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);

        boolean defaultOverlayViewEnabled = true;
        boolean overlayViewEnabled = (params != null) ?
                params.getBoolean("overlay_view_enable", defaultOverlayViewEnabled) :
                defaultOverlayViewEnabled;
        Log.d(TAG, "onTune: overlayViewEnabled = " + overlayViewEnabled);
        setOverlayViewEnabled(overlayViewEnabled);

        mPendingChannelUri = channelUri;
        mPendingParams = params;

        if (mSurface != null || mSidebandMode) {
            startPlaybackInternal(channelUri, params);
        } else {
            Log.d(TAG, "defer playback until surface becomes available");
        }
        return true;
    }

    private void startPlaybackInternal(Uri channelUri, @Nullable Bundle params) {
        mHandler.removeCallbacks(mDelayedStop);
        takeOwnership();

        try {
            int playId = (mPlayId >= 0) ? mPlayId : 0;
            int playStatus = AvCmdMiddle.PESI_SVR_AV_STOP_STATE;
            boolean shouldStop = false;
            try {
                playStatus = mDtv.av_control_get_play_status(playId);
                shouldStop = (playStatus != AvCmdMiddle.PESI_SVR_AV_STOP_STATE) &&
                        (playStatus != AvCmdMiddle.PESI_SVR_AV_RELEASE_STATE) &&
                        (playStatus != AvCmdMiddle.PESI_SVR_AV_IDLE_STATE);
            } catch (Throwable t) {
                Log.w(TAG, "startPlaybackInternal get_play_status error: ", t);
            }
            Log.d(TAG, "startPlaybackInternal status=" + playStatus + " shouldStop=" + shouldStop);
            if (shouldStop) {
                stopPlaybackInternal(false);
            }

            if (params != null &&
                    params.getInt(COMMAND_ID, 0) == AvModule.CMD_ServicePlayer_AV_PlayByChannelId) {
                playBySidOnidTsid(channelUri, params);
            } else {
                playByTifChannelId(channelUri);
            }

            mIsPlaying = true;
            mPendingChannelUri = null;
            mPendingParams = null;

            // 真正可視等 HAL 回報 TYPE_AV_FirstFrame
            notifyChannelRetuned(channelUri);
        } catch (Throwable t) {
            Log.e(TAG, "startPlaybackInternal error: ", t);
            mIsPlaying = false;
            notifyVideoUnavailableUnknown("startPlaybackInternal error");
        }
    }

    /**
     * 主動停播（例如 onRelease）；若是 Surface-lost 的情境通常走延遲停播邏輯
     */
    private void stopPlaybackInternal(boolean dueToSurfaceLost) {
        int playId = (mPlayId >= 0) ? mPlayId : 0;
        int playStatus = AvCmdMiddle.PESI_SVR_AV_STOP_STATE;
        try {
            playStatus = mDtv.av_control_get_play_status(playId);
        } catch (Throwable t) {
            Log.w(TAG, "stopPlaybackInternal get_play_status error: ", t);
        }
        if (playStatus == AvCmdMiddle.PESI_SVR_AV_STOP_STATE ||
                playStatus == AvCmdMiddle.PESI_SVR_AV_RELEASE_STATE ||
                playStatus == AvCmdMiddle.PESI_SVR_AV_IDLE_STATE) {
            Log.d(TAG, "stopPlaybackInternal skip: status=" + playStatus);
            return;
        }
        Log.d(TAG, "stopPlaybackInternal status=" + playStatus);
        try {
            mDtv.av_control_play_stop(playId, 0, 1);
        } catch (Throwable t) {
            Log.w(TAG, "stopPlaybackInternal error: ", t);
        } finally {
            mIsPlaying = false;

            // ★ 清掉 subtitle bitmap，避免停播後殘影
            if (mSubtitleView != null) {
                mSubtitleView.clearSubtitle();
            }

            if (!dueToSurfaceLost) {
                // 主動停播時你可視需要清掉 pending
            }
        }
    }

    private void playByTifChannelId(Uri channelUri) {
        Log.d(TAG, "primetif1 playByTifChannelId: " + channelUri);

        if (mDtv == null) {
            Log.e(TAG, "playByTifChannelId: mDtv is null!");
            notifyVideoUnavailableUnknown("playByTifChannelId mDtv null");
            return;
        }

        int playId = Math.max(mPlayId, 0);
        Bundle bundle = TIFChannelData.getChannelSIdOnIdTsIdFromUri(mContext, channelUri);
        int sid = bundle.getInt(ProgramInfo.SERVICE_ID, 0);
        int onid = bundle.getInt(ProgramInfo.ORIGINAL_NETWORK_ID, 0);
        int tsid = bundle.getInt(ProgramInfo.TRANSPORT_STREAM_ID, 0);

        // Validation: If all are 0, it's likely an invalid URI or parsing failed
        if (sid == 0 && onid == 0 && tsid == 0) {
            Log.e(TAG, "playByTifChannelId: Invalid channel URI or parsing failed. sid=" + sid + ", onid=" + onid
                    + ", tsid=" + tsid);
            notifyVideoUnavailableUnknown("playByTifChannelId invalid sid/onid/tsid");
            return;
        }

        ProgramInfo programInfo = mDtv.get_program_by_SId_OnId_TsId(sid, onid, tsid);
        if (programInfo != null) {
            mPlayId = mDtv.av_control_tif_play_by_channel_id(playId, programInfo.getChannelId(), programInfo.getType(),
                    false);
        } else {
            Log.e(TAG,
                    "playByTifChannelId: ProgramInfo not found for sid=" + sid + ", onid=" + onid + ", tsid=" + tsid);
            notifyVideoUnavailableUnknown("playByTifChannelId programInfo null");
        }
        Log.d(TAG, "playByTifChannelId: mPlayId(AVTunerID)=" + mPlayId);
    }

    /**
     * 路徑 B：以 (sid,onid,tsid) 反查 ProgramInfo，再 av_control_play_by_channel_id() 播放
     */
    private void playBySidOnidTsid(Uri channelUri, Bundle params) {
        params.setClassLoader(PrimeTvInputFrameworkApplication.getInstance().getClassLoader());
        int playId = params.getInt(AvModule.PlayId_string, 0);
        int type = params.getInt(ProgramInfo.TYPE, FavGroup.ALL_TV_TYPE);
        int show = params.getInt(AvModule.Show_string, 0);

        // ★ 這條路徑 APP 已指定 playId（通常是 tunerId）
        mPlayId = playId;

        Bundle b = TIFChannelData.getChannelSIdOnIdTsIdFromUri(mContext, channelUri);
        int sid = b.getInt(ProgramInfo.SERVICE_ID, 0);
        int onid = b.getInt(ProgramInfo.ORIGINAL_NETWORK_ID, 0);
        int tsid = b.getInt(ProgramInfo.TRANSPORT_STREAM_ID, 0);

        ProgramInfo p = null;
        try {
            p = mDtv.get_program_by_SId_OnId_TsId(sid, onid, tsid);
        } catch (Throwable t) {
            Log.w(TAG, "get_program_by_SId_OnId_TsId error: " + t);
        }

        if (p != null) {
            long chId = p.getChannelId();
            Log.d(TAG, "primetif playBySidOnidTsid → channelId=" + chId +
                    " (sid=" + sid + ", onid=" + onid + ", tsid=" + tsid + "), playId=" + playId);
            try {
                mDtv.av_control_play_by_channel_id(playId, chId, type, show);
            } catch (Throwable t) {
                Log.e(TAG, "av_control_play_by_channel_id error: " + t);
            }
        } else {
            Log.w(TAG, "program not found, sid=" + sid + " onid=" + onid + " tsid=" + tsid);
            notifyVideoUnavailableUnknown("playBySidOnidTsid program null");
        }
    }

    private int ensurePlayIdInternal() {
        if (mPlayId < 0)
            mPlayId = 0;
        return mPlayId;
    }

    // ===========================
    // Caption / Overlay
    // ===========================

    @Override
    public void onSetCaptionEnabled(boolean enabled) {
        Log.d(TAG, "onSetCaptionEnabled: " + enabled);
        mCaptionEnabled = enabled;

        if (!enabled) {
            if (mSubtitleView != null) {
                mSubtitleView.clearSubtitle();
            }
            try {
                mDtv.StopDVBStubtitle();
            } catch (Throwable t) {
                Log.w(TAG, "StopDVBStubtitle error", t);
            }
        }

        mTrackController.rebuildAndNotify();
    }

    @Override
    public View onCreateOverlayView() {
        Log.d(TAG, "onCreateOverlayView");
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View root = inflater.inflate(R.layout.overlay_subtitle, null);
        mSubtitleView = root.findViewById(R.id.subtitle_view);
        return root;
    }

    // ===========================
    // HAL 回報 → Session
    // ===========================

    public void onPmtAvChangeFromHal() {
        if (!iAmOwner()) {
            Log.d(TAG, "ignore PmtAvChange (not owner) @" + System.identityHashCode(this));
            return;
        }
        mTrackController.updateAudioTracks();
    }

    public void onPmtDvbSubtitleChangeFromHal() {
        if (!iAmOwner()) {
            Log.d(TAG, "ignore PmtAvChange (not owner) @" + System.identityHashCode(this));
            return;
        }
        mTrackController.updateSubtitleTracks();
    }

    public void onFirstFrameRenderedFromHal() {
        if (!iAmOwner()) {
            Log.d(TAG, "ignore first-frame (not owner) @" + System.identityHashCode(this));
            return;
        }
        Log.d(TAG, "primetif notifyVideoAvailable @" + System.identityHashCode(this));
        mTrackController.updateAudioTracks();
        mTrackController.updateSubtitleTracks();
        notifyVideoAvailable();
    }

    // ===========================
    // Track Selection
    // ===========================

    @Override
    public boolean onSelectTrack(int type, @Nullable String trackId) {
        Log.d(TAG, "onSelectTrack: type=" + type + " trackId=" + trackId);

        if (mIsPlayingRecordedProgram) { // for playing recorded program
            return onSelectRecordedProgramTrackInternal(type, trackId);
        }

        if (type == TvTrackInfo.TYPE_AUDIO) {
            if (trackId == null) {
                Log.d(TAG, "onSelectTrack: audio trackId null, ignore");
                return false;
            }

            int playId = ensurePlayIdInternal();
            if (playId < 0) {
                Log.w(TAG, "onSelectTrack: invalid playId for audio, skip");
                return false;
            }

            AudioInfo.AudioComponent c = mTrackController.getAudioComponent(trackId);
            if (c == null) {
                Log.w(TAG, "onSelectTrack: no audio component for " + trackId);
                return false;
            }

            Log.d(TAG, "onSelectTrack: change audio, playId=" + playId +
                    " pid=" + c.getPid() + " lang=" + c.getLangCode());
            try {
                mDtv.av_control_change_audio(playId, c);
            } catch (Throwable t) {
                Log.e(TAG, "onSelectTrack: av_control_change_audio error", t);
            }

            mTrackController.setSelectedAudioId(trackId);
            mTrackController.rebuildAndNotify();
            return true;

        } else if (type == TvTrackInfo.TYPE_SUBTITLE) {
            int playId = ensurePlayIdInternal();
            if (playId < 0) {
                Log.w(TAG, "onSelectTrack: invalid playId for subtitle, skip");
                return false;
            }

            if (trackId == null) {
                // 關閉字幕
                Log.d(TAG, "onSelectTrack: subtitle trackId null → disable subtitle");
                mTrackController.setSelectedSubtitleId(null);

                if (mSubtitleView != null) {
                    mSubtitleView.clearSubtitle();
                }
                try {
                    mDtv.StopDVBStubtitle();
                } catch (Throwable t) {
                    Log.w(TAG, "StopDVBStubtitle error", t);
                }

                mTrackController.rebuildAndNotify();
                return true;
            }

            SubtitleInfo.SubtitleComponent c = mTrackController.getSubtitleComponent(trackId);
            if (c == null) {
                Log.w(TAG, "onSelectTrack: no subtitle component for " + trackId);
                return false;
            }

            Log.d(TAG, "onSelectTrack: change subtitle, playId=" + playId +
                    " pid=" + c.getPid() + " lang=" + c.getLangCode() +
                    " type=" + c.getType());

            try {
                mDtv.av_control_change_subtitle(playId, c);
            } catch (Throwable t) {
                Log.e(TAG, "onSelectTrack: av_control_change_subtitle error", t);
            }

            mTrackController.setSelectedSubtitleId(trackId);
            mTrackController.rebuildAndNotify();
            return true;
        }

        return super.onSelectTrack(type, trackId);
    }

    // ===========================
    // AppPrivateCommand (UI 控制)
    // ===========================

    @Override
    public void onAppPrivateCommand(@NonNull String action, @Nullable Bundle data) {
        if (data == null) {
            Log.w(TAG, "onAppPrivateCommand: data is null for action=" + action);
            return;
        }
        Log.d(TAG, "onAppPrivateCommand: action=" + action + " data=" + data);

        switch (action) {
            case "com.prime.action.SELECT_AUDIO": {
                int audioIndex = data.getInt("index", 0);
                if (audioIndex >= 0 && audioIndex < mTrackController.getAudioTrackCount()) {
                    String id = mTrackController.getAudioTrackIdAt(audioIndex);
                    if (id != null) {
                        onSelectTrack(TvTrackInfo.TYPE_AUDIO, id);
                    }
                }
                break;
            }

            case "com.prime.action.SELECT_SUBTITLE": {
                int subtitleIndex = data.getInt("index", -1);
                if (subtitleIndex >= 0 && subtitleIndex < mTrackController.getSubtitleTrackCount()) {
                    String id = mTrackController.getSubtitleTrackIdAt(subtitleIndex);
                    if (id != null) {
                        onSelectTrack(TvTrackInfo.TYPE_SUBTITLE, id);
                    }
                } else {
                    // index < 0 代表關閉字幕
                    onSelectTrack(TvTrackInfo.TYPE_SUBTITLE, null);
                }
                break;
            }

            case "com.prime.action.TELETEXT_ON":
                // TODO: 通知 TeletextService 啟動
                break;

            default:
                break;
        }
    }

    // ===========================
    // Release / TVMessage handler
    // ===========================

    @Override
    public void onRelease() {
        Log.d(TAG, "primetif onRelease @" + System.identityHashCode(this));
        mHandler.removeCallbacks(mDelayedStop);

        if (mSubtitleView != null) {
            mSubtitleView.clearSubtitle();
        }
        if (iAmOwner()) {
            if (mIsPlayingRecordedProgram) {
                stopRecordedProgramInternal();
            } else {
                try {
                    int playId = (mPlayId >= 0) ? mPlayId : 0;
                    mDtv.av_control_play_stop(playId, 0, 1);
                } catch (Throwable t) {
                    Log.w(TAG, "onRelease stop error: ", t);
                }
            }

            sActiveOwner = null;
        }

        mIsPlaying = false;
    }

    public static void handleTvMessageFromPrime(@NonNull TVMessage msg) {
        if (msg.getMsgFlag() != TVMessage.FLAG_AV)
            return;
        final PrimeTvInputServiceSession s = sActiveOwner;
        if (s == null) {
            Log.d(TAG, "AV msg dropped: no active owner");
            return;
        }
        s.mHandler.post(() -> {
            switch (msg.getMsgType()) {
                case TVMessage.TYPE_AV_FRAME_PLAY_STATUS:
                    //Log.d(TAG, "primetif TYPE_AV_FRAME_PLAY_STATUS");
//                    s.playByTifChannelId(s.mChannelUri);
                    break;
                case TVMessage.TYPE_AV_PmtAvChange:
                    Log.d(TAG, "primetif TYPE_AV_PmtAvChange");
                    s.onPmtAvChangeFromHal();
                    break;
                case TVMessage.TYPE_AV_DvbSubtileChange:
                    Log.d(TAG, "primetif TYPE_AV_PmtAvChange");
                    s.onPmtDvbSubtitleChangeFromHal();
                    break;

                case TVMessage.TYPE_AV_FirstFrame:
                    Log.d(TAG, "primetif TYPE_AV_FirstFrame");
                    s.onFirstFrameRenderedFromHal();
                    break;
                case TVMessage.TYPE_AV_VideoUnavailable: {
                    int avStatus = msg.getAvFrameStatus();
                    int tifReason = mapPrimeAvStatusToTif(avStatus);
                    Log.d(TAG, "primetif TYPE_AV_VideoUnavailable: " + tifReason);
                    s.onPrimeVideoUnavailable(tifReason);
                    break;
                }
                case TVMessage.TYPE_AV_DECODER_ERROR:
                    s.onPrimeDecoderError();
                    break;
                case TVMessage.TYPE_AV_SUBTITLE_BIMAP: {
                    Bitmap bmp = msg.subtitle_bitmap;
                    Log.d(TAG, "TYPE_AV_SUBTITLE_BIMAP: bmp=" + bmp);

                    s.handleSubtitleBitmapFromPrime(bmp);
                    break;
                }
                default:
                    break;
            }
        });
    }

    private void onPrimeVideoUnavailable(int tifReason) {
        if (!iAmOwner())
            return;
        mHandler.removeCallbacks(mDelayedStop);
        notifyVideoUnavailable(tifReason);
        mIsPlaying = false;
    }

    private void onPrimeDecoderError() {
        if (!iAmOwner())
            return;
        mHandler.removeCallbacks(mDelayedStop);
        notifyVideoUnavailableUnknown("onPrimeDecoderError");
        mIsPlaying = false;
    }

    private void notifyVideoUnavailableUnknown(String where) {
        notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN);
    }

    private void onPrimeLockFail() {
        if (!iAmOwner())
            return;
        mHandler.removeCallbacks(mDelayedStop);
        notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL);
        mIsPlaying = false;
    }

    private void handleSubtitleBitmapFromPrime(@Nullable Bitmap bitmap) {
        if (!iAmOwner()) {
            Log.d(TAG, "handleSubtitleBitmapFromPrime: not owner, ignore");
            return;
        }
        if (mSubtitleView == null) {
            Log.d(TAG, "handleSubtitleBitmapFromPrime: mSubtitleView is null");
            return;
        }

        // CC 關閉或沒有選中的 subtitle track → 不畫
        if (!mCaptionEnabled || mTrackController.getSelectedSubtitleId() == null) {
            mSubtitleView.clearSubtitle();
            return;
        }

        if (bitmap != null) {
            mSubtitleView.setSubtitleBitmap(bitmap);
        } else {
            mSubtitleView.clearSubtitle();
        }
    }

    private static int mapPrimeAvStatusToTif(int avStatus) {
        LogUtils.d("[CheckErrorMsg] avStatus = "+avStatus);
        switch (avStatus) {
            // 例：0=BUFFERING, 1=WEAK_SIGNAL, 2=TUNING, 其他=UNKNOWN
            case 0:
                return TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING;
            case TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING:
                return TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING;
            case 1:
                return TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL;
            case 2:
                return TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING;
            case 511:
                return 511;
            case 507:
                return 507;
            case 10:
                return 10;
            default:
                return TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN;
        }
    }


    @Override
    public long onTimeShiftGetCurrentPosition() {
//        Log.d(TAG, "onTimeShiftGetCurrentPosition: ");
        if (mIsPlayingRecordedProgram) {
//            Log.d(TAG, "onTimeShiftGetCurrentPosition: file playback get current pos");
            return getRecordedProgramCurrentPositionInternal(mRecordedProgramPlayTunerId);
        }

        // TODO: need implement get current pos for timeShift
        return super.onTimeShiftGetCurrentPosition();
    }

    @Override
    public long onTimeShiftGetStartPosition() {
//        Log.d(TAG, "onTimeShiftGetStartPosition: ");
        if (mIsPlayingRecordedProgram) {
//            Log.d(TAG, "onTimeShiftGetStartPosition: file playback get start pos");
            return 0; // start pos is always 0 for file playback
        }

        // TODO: need implement get start pos for timeShift
        return super.onTimeShiftGetStartPosition();
    }

    @Override
    public void onTimeShiftPause() {
        Log.d(TAG, "onTimeShiftPause: ");
        // pause for file playback
        if (mIsPlayingRecordedProgram) {
            Log.d(TAG, "onTimeShiftPause: file playback pause");
            pauseRecordedProgramInternal(mRecordedProgramPlayTunerId);
        }

        // TODO: need implement pause for timeShift
    }

    @Override
    public void onTimeShiftPlay(Uri recordedProgramUri) {
        Log.d(TAG, "onTimeShiftPlay: " + recordedProgramUri);
        if (mSurface == null) {
            Log.d(TAG, "onTimeShiftPlay: null surface, playback will start in onSetSurface()");
            mPendingRecordedProgramUri = recordedProgramUri;
            return;
        }

        playRecordedProgramInternal(recordedProgramUri);
    }

    @Override
    public void onTimeShiftResume() {
        Log.d(TAG, "onTimeShiftResume: ");
        // resume for file playback
        if (mIsPlayingRecordedProgram) {
            Log.d(TAG, "onTimeShiftResume: file playback resume");
            resumeRecordedProgramInternal(mRecordedProgramPlayTunerId);
        }

        // TODO: need implement resume for timeShift
    }

    @Override
    public void onTimeShiftSeekTo(long timeMs) {
        Log.d(TAG, "onTimeShiftSeekTo: " + timeMs);
        // seek for file playback
        if (mIsPlayingRecordedProgram) {
            Log.d(TAG, "onTimeShiftSeekTo: file playback seek to " + timeMs);
            seekRecordedProgramInternal(mRecordedProgramPlayTunerId, timeMs);
        }

        // TODO: need implement seek for timeShift
    }

    @Override
    public void onTimeShiftSetPlaybackParams(PlaybackParams params) {
        Log.d(TAG, "onTimeShiftSetPlaybackParams: " + params);
        // set play params for file playback
        if (mIsPlayingRecordedProgram) {
            try {
                float speed = params.getSpeed();
                Log.d(TAG, "onTimeShiftSetPlaybackParams: file playback set speed = " + speed);
                setRecordedProgramSpeedInternal(mRecordedProgramPlayTunerId, speed);
            } catch (IllegalStateException e) {
                Log.e(TAG, "onTimeShiftSetPlaybackParams: fail", e);
            }
        }

        // TODO: need implement set play param for timeShift
    }

    private void playRecordedProgramInternal(Uri recordedProgramUri) {
        TIFRecordedProgramData tifRecordedProgramData =
                TIFRecordedProgramData.fromUri(mContext, recordedProgramUri);
        if (tifRecordedProgramData != null) {
            PvrRecIdx pvrRecIdx = tifRecordedProgramData.getPesiPvrRecIdx();
            boolean fromLastPos = false;
            mRecordedProgramPlayTunerId =
                    mDtv.pvr_change_channel_manager_playback_start(pvrRecIdx, fromLastPos);
            if (mRecordedProgramPlayTunerId >= 0) {
                mIsPlayingRecordedProgram = true;
                notifyVideoAvailable();

                // update tracks and notify tv app
                mTrackController.updateRecordedProgramAudioTracks(mContext, pvrRecIdx);
                mTrackController.updateRecordedProgramSubtitleTracks(mContext, pvrRecIdx);
                notifyTracksChanged(mTrackController.getTracks());
                notifyTrackSelected(TvTrackInfo.TYPE_AUDIO, mTrackController.getSelectedAudioId());
                String subtitleId =
                        mCaptionEnabled ? mTrackController.getSelectedSubtitleId() : null;
                notifyTrackSelected(TvTrackInfo.TYPE_SUBTITLE, subtitleId);
            } else {
                Log.e(TAG, "playRecordedProgramInternal: failed, invalid play tuner id");
                notifyVideoUnavailableUnknown("playRecordedProgramInternal invalid play tuner id");
            }
        } else {
            Log.e(TAG, "playRecordedProgramInternal: failed, tifRecordedProgramData is null");
            notifyVideoUnavailableUnknown("playRecordedProgramInternal data null");
        }
    }

    private void stopRecordedProgramInternal() {
        mDtv.pvr_change_channel_manager_playback_stop();
        mIsPlayingRecordedProgram = false;
    }

    private void resumeRecordedProgramInternal(int playbackTunerId) {
        mDtv.pvr_playback_resume(playbackTunerId);
    }

    private void pauseRecordedProgramInternal(int playbackTunerId) {
        mDtv.pvr_playback_pause(playbackTunerId);
    }

    private long getRecordedProgramCurrentPositionInternal(int playbackTunerId) {
        int pesiCurPlayTimeSecs = mDtv.pvr_playback_get_play_time(playbackTunerId);
        if (pesiCurPlayTimeSecs < 0) {
            Log.w(TAG, "getRecordedProgramCurrentPositionInternal: invalid pesi play time = " +
                    pesiCurPlayTimeSecs);
            return TvInputManager.TIME_SHIFT_INVALID_TIME;
        }

        long curPositionMs = TimeUnit.SECONDS.toMillis(pesiCurPlayTimeSecs);
//        Log.d(TAG, "getRecordedProgramCurrentPositionInternal: curPositionMs = " + curPositionMs);
        return curPositionMs;
    }

    private void seekRecordedProgramInternal(int playbackTunerId, long timeMs) {
        PvrInfo.EnSeekMode seekMode = PvrInfo.EnSeekMode.PLAY_SEEK_SET; // from begin
        int offsetSecs = (int) TimeUnit.MILLISECONDS.toSeconds(timeMs);

        // our pvr seek does not handle seek to 0 for now
        if (offsetSecs <= 0) {
            Log.w(TAG, "seekRecordedProgramInternal: invalid offsetSecs = " + offsetSecs +
                    ", adjust to 1");
            offsetSecs = 1;
        }

        Log.d(TAG, "seekRecordedProgramInternal: seek to " + offsetSecs);
        mDtv.pvr_playback_seek(playbackTunerId, seekMode, offsetSecs);
    }

    private void setRecordedProgramSpeedInternal(int playbackTunerId, float speed) {
        PvrInfo.EnPlaySpeed playSpeed = PvrInfo.EnPlaySpeed.fromFloatSpeed(speed);
        Log.d(TAG, "setRecordedProgramSpeedInternal: EnPlaySpeed = " + playSpeed);
        if (playSpeed != PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FAILD) {
            mDtv.pvr_playback_set_speed(playbackTunerId, playSpeed);
        }
    }

    private boolean onSelectRecordedProgramTrackInternal(int type, String trackId) {
        if (type == TvTrackInfo.TYPE_AUDIO) {
            if (trackId == null) {
                Log.d(TAG, "onSelectRecordedProgramTrackInternal: audio trackId null, ignore");
                return false;
            }

            AudioInfo.AudioComponent audioComponent = mTrackController.getAudioComponent(trackId);
            if (audioComponent == null) {
                Log.w(TAG, "onSelectRecordedProgramTrackInternal: " +
                        "no audio component for " + trackId);
                return false;
            }

            Log.d(TAG, "onSelectRecordedProgramTrackInternal: " +
                    "change audio, pid=" + audioComponent.getPid() +
                    " lang=" + audioComponent.getLangCode());
            try {
                mDtv.pvr_playback_change_audio_track(mRecordedProgramPlayTunerId, audioComponent);
            } catch (Throwable t) {
                Log.e(TAG, "onSelectRecordedProgramTrackInternal: " +
                        "pvr_playback_change_audio_track error", t);
                return false;
            }

            mTrackController.setSelectedAudioId(trackId);
            notifyTrackSelected(TvTrackInfo.TYPE_AUDIO, trackId);
            return true;

        } else if (type == TvTrackInfo.TYPE_SUBTITLE) {
            if (trackId == null) {
                Log.d(TAG, "onSelectRecordedProgramTrackInternal: " +
                        "subtitle trackId null → disable subtitle");

                if (mSubtitleView != null) {
                    mSubtitleView.clearSubtitle();
                }
                try {
                    // TODO: implement primeDtv pvr stop subtitle?
                    Log.w(TAG, "onSelectRecordedProgramTrackInternal: " +
                            "primeDtv stop pvr subtitle not implemented");
                } catch (Throwable t) {
                    Log.e(TAG, "onSelectRecordedProgramTrackInternal: error", t);
                    return false;
                }

                mTrackController.setSelectedSubtitleId(null);
                notifyTrackSelected(TvTrackInfo.TYPE_SUBTITLE, null);
                return true;
            }

            SubtitleInfo.SubtitleComponent subtitleComponent = mTrackController.getSubtitleComponent(trackId);
            if (subtitleComponent == null) {
                Log.w(TAG, "onSelectRecordedProgramTrackInternal: " +
                        "no subtitle component for " + trackId);
                return false;
            }

            Log.d(TAG, "onSelectRecordedProgramTrackInternal: change subtitle, " +
                    " pid=" + subtitleComponent.getPid() + " lang=" + subtitleComponent.getLangCode() +
                    " type=" + subtitleComponent.getType());

            try {
                // TODO: implement primeDtv pvr change subtitle?
                Log.w(TAG, "onSelectRecordedProgramTrackInternal: " +
                        "primeDtv change pvr subtitle not implemented");
            } catch (Throwable t) {
                Log.e(TAG, "onSelectRecordedProgramTrackInternal: change subtitle error", t);
                return false;
            }

            mTrackController.setSelectedSubtitleId(trackId);
            notifyTrackSelected(TvTrackInfo.TYPE_SUBTITLE, trackId);
            return true;
        }

        return false;
    }
}
