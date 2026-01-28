package com.prime.tvinputframework;

import android.content.Context;
import android.media.tv.TvTrackInfo;
import android.util.Log;

import androidx.annotation.Nullable;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.sysdata.AudioInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.PvrDbRecordInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.datastructure.sysdata.SubtitleInfo;
import com.prime.dtv.service.datamanager.PvrDataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 負責管理 Audio / Subtitle tracks：
 * - 從 PrimeDtvServiceInterface 拉 audio/subtitle list
 * - 建立 TvTrackInfo
 * - 記錄目前選中的 trackId
 * - 統一呼叫 TvInputService.Session 的 notifyTracksChanged / notifyTrackSelected
 *
 * Session 只需要：
 * - 傳入 PrimeDtvServiceInterface
 * - 透過 Callback 提供：
 *   - notifyTracksChanged()
 *   - notifyTrackSelected()
 *   - ensurePlayId()：確保 playId（AVTunerId）
 *   - isCaptionEnabled()：系統 CC 是否開啟
 */
public class PrimeTrackController {

    private static final String TAG = "PrimeTrackController";

    public interface Callback {
        void notifyTracksChanged(List<TvTrackInfo> tracks);

        void notifyTrackSelected(int type, @Nullable String trackId);

        /**
         * 由 Session 保證回傳正確的 playId（AVTunerId）
         * 回傳 < 0 代表現在沒有有效的 playId。
         */
        int ensurePlayId();

        /**
         * 系統層 CC 是否啟用（對應 Session.onSetCaptionEnabled）
         */
        boolean isCaptionEnabled();
    }

    private final PrimeDtvServiceInterface mDtv;
    private final Callback mCallback;

    // 音軌
    private final List<TvTrackInfo> mAudioTracks = new ArrayList<>();
    private final Map<String, AudioInfo.AudioComponent> mAudioMap = new HashMap<>();
    private @Nullable String mSelectedAudioId;

    // 字幕
    private final List<TvTrackInfo> mSubtitleTracks = new ArrayList<>();
    private final Map<String, SubtitleInfo.SubtitleComponent> mSubtitleMap = new HashMap<>();
    private @Nullable String mSelectedSubtitleId;

    public PrimeTrackController(PrimeDtvServiceInterface dtv, Callback callback) {
        this.mDtv = dtv;
        this.mCallback = callback;
    }

    // ===========================
    //      Audio Track 更新
    // ===========================

    public void updateAudioTracks() {
        if (mDtv == null) {
            Log.e(TAG, "updateAudioTracks: mDtv is null");
            clearAudioTracks();
            rebuildAndNotify();
            return;
        }

        int playId = mCallback.ensurePlayId();
        if (playId < 0) {
            Log.w(TAG, "updateAudioTracks: invalid playId, skip");
            clearAudioTracks();
            rebuildAndNotify();
            return;
        }

        AudioInfo audioInfo;
        try {
            audioInfo = mDtv.av_control_get_audio_list_info(playId);
        } catch (Throwable t) {
            Log.e(TAG, "av_control_get_audio_list_info error", t);
            clearAudioTracks();
            rebuildAndNotify();
            return;
        }

        if (audioInfo == null) {
            Log.w(TAG, "updateAudioTracks: audioInfo is null");
            clearAudioTracks();
            rebuildAndNotify();
            return;
        }

        int curPos = audioInfo.getCurPos();
        List<AudioInfo.AudioComponent> list = audioInfo.getComponentList();

        Log.d(TAG, "updateAudioTracks: playId=" + playId +
                " curPos=" + curPos + " count=" + list.size());

        mAudioTracks.clear();
        mAudioMap.clear();

        for (int i = 0; i < list.size(); i++) {
            AudioInfo.AudioComponent c = list.get(i);
            String trackId = buildAudioTrackId(i, c);

            TvTrackInfo.Builder builder =
                    new TvTrackInfo.Builder(TvTrackInfo.TYPE_AUDIO, trackId);

            builder.setLanguage(c.getLangCode());

            android.os.Bundle extra = new android.os.Bundle();
            extra.putInt("pid", c.getPid());
            extra.putInt("codec", c.getAudioType());
            extra.putInt("trackMode", c.getTrackMode());
            builder.setExtra(extra);

            TvTrackInfo track = builder.build();
            mAudioTracks.add(track);
            mAudioMap.put(trackId, c);

            Log.d(TAG, "audio track[" + i + "]: id=" + trackId +
                    " pid=" + c.getPid() + " lang=" + c.getLangCode());
        }

        if (curPos >= 0 && curPos < mAudioTracks.size()) {
            mSelectedAudioId = mAudioTracks.get(curPos).getId();
        } else {
            mSelectedAudioId = null;
        }

        rebuildAndNotify();
    }

    public void updateRecordedProgramAudioTracks(Context context, PvrRecIdx pvrRecIdx) {
        if (mDtv == null) {
            Log.e(TAG, "updateRecordedProgramAudioTracks: mDtv is null");
            clearAudioTracks();
            return;
        }

        PvrDbRecordInfo pvrDbRecordInfo = new PvrDataManager(context).queryDataFromTable(pvrRecIdx);
        List<ProgramInfo.AudioInfo> audioInfoList = pvrDbRecordInfo.getAudiosInfoList();
        if (audioInfoList == null) {
            Log.e(TAG, "updateRecordedProgramAudioTracks: audioInfoList is null");
            clearAudioTracks();
            return;
        }

        AudioInfo audioInfo = new AudioInfo();
        for (ProgramInfo.AudioInfo info : audioInfoList) {
            AudioInfo.AudioComponent audioComponent = new AudioInfo.AudioComponent();
            audioComponent.setPid(info.getPid());
            audioComponent.setAudioType(info.getCodec());
            audioComponent.setAdType(0);
            audioComponent.setTrackMode(-1);
            audioComponent.setLangCode(info.getLeftIsoLang());
            audioComponent.setPos(audioInfoList.indexOf(info));
            audioInfo.ComponentList.add(audioComponent);
        }

        int curPos = audioInfo.getCurPos();
        List<AudioInfo.AudioComponent> list = audioInfo.getComponentList();

        Log.d(TAG, "updateRecordedProgramAudioTracks: curPos=" + curPos +
                " count=" + list.size());

        mAudioTracks.clear();
        mAudioMap.clear();

        for (int i = 0; i < list.size(); i++) {
            AudioInfo.AudioComponent c = list.get(i);
            String trackId = buildAudioTrackId(i, c);

            TvTrackInfo.Builder builder =
                    new TvTrackInfo.Builder(TvTrackInfo.TYPE_AUDIO, trackId);

            builder.setLanguage(c.getLangCode());

            android.os.Bundle extra = new android.os.Bundle();
            extra.putInt("pid", c.getPid());
            extra.putInt("codec", c.getAudioType());
            extra.putInt("trackMode", c.getTrackMode());
            builder.setExtra(extra);

            TvTrackInfo track = builder.build();
            mAudioTracks.add(track);
            mAudioMap.put(trackId, c);

            Log.d(TAG, "updateRecordedProgramAudioTracks audio track[" + i + "]: id=" + trackId +
                    " pid=" + c.getPid() + " lang=" + c.getLangCode());
        }

        if (curPos >= 0 && curPos < mAudioTracks.size()) {
            mSelectedAudioId = mAudioTracks.get(curPos).getId();
        } else {
            mSelectedAudioId = null;
        }
    }

    private void clearAudioTracks() {
        mAudioTracks.clear();
        mAudioMap.clear();
        mSelectedAudioId = null;
    }

    private String buildAudioTrackId(int index, AudioInfo.AudioComponent c) {
        return "audio_" + index + "_" + c.getPid();
    }

    // ===========================
    //      Subtitle Track 更新
    // ===========================

    public void updateSubtitleTracks() {
        if (mDtv == null) {
            Log.e(TAG, "updateSubtitleTracks: mDtv is null");
            clearSubtitleTracks();
            rebuildAndNotify();
            return;
        }

        int playId = mCallback.ensurePlayId();
        if (playId < 0) {
            Log.w(TAG, "updateSubtitleTracks: invalid playId, skip");
            clearSubtitleTracks();
            rebuildAndNotify();
            return;
        }

        SubtitleInfo subInfo;
        try {
            subInfo = mDtv.av_control_get_subtitle_list_info(playId);
        } catch (Throwable t) {
            Log.e(TAG, "av_control_get_subtitle_list_info error", t);
            clearSubtitleTracks();
            rebuildAndNotify();
            return;
        }

        if (subInfo == null) {
            Log.d(TAG, "updateSubtitleTracks: subInfo is null, clear tracks");
            clearSubtitleTracks();
            rebuildAndNotify();
            return;
        }

        int curPos = subInfo.getCurPos();
        List<SubtitleInfo.SubtitleComponent> list = subInfo.getComponentList();

        Log.d(TAG, "updateSubtitleTracks: playId=" + playId +
                " curPos=" + curPos + " count=" + list.size());

        mSubtitleTracks.clear();
        mSubtitleMap.clear();

        for (int i = 0; i < list.size(); i++) {
            SubtitleInfo.SubtitleComponent c = list.get(i);
            String trackId = buildSubtitleTrackId(i, c);

            TvTrackInfo.Builder builder =
                    new TvTrackInfo.Builder(TvTrackInfo.TYPE_SUBTITLE, trackId);

            builder.setLanguage(c.getLangCode());

            android.os.Bundle extra = new android.os.Bundle();
            extra.putInt("pid", c.getPid());
            extra.putInt("type", c.getType());
            extra.putInt("comp_page_id", c.getComPageId());
            extra.putInt("anc_page_id", c.getAncPageId());
            extra.putInt("pos", c.getPos());
            builder.setExtra(extra);

            TvTrackInfo track = builder.build();
            mSubtitleTracks.add(track);
            mSubtitleMap.put(trackId, c);

            Log.d(TAG, "subtitle track[" + i + "]: id=" + trackId +
                    " pid=" + c.getPid() + " lang=" + c.getLangCode() +
                    " type=" + c.getType());
        }

        if (curPos >= 0 && curPos < mSubtitleTracks.size()) {
            mSelectedSubtitleId = mSubtitleTracks.get(curPos).getId();
        } else if (!mSubtitleTracks.isEmpty()) {
            // 如果 service 沒有 CurPos，可以預設第一個
            mSelectedSubtitleId = mSubtitleTracks.get(0).getId();
        } else {
            mSelectedSubtitleId = null;
        }

        rebuildAndNotify();
    }

    public void updateRecordedProgramSubtitleTracks(Context context, PvrRecIdx pvrRecIdx) {
        if (mDtv == null) {
            Log.e(TAG, "updateRecordedProgramSubtitleTracks: mDtv is null");
            clearSubtitleTracks();
            return;
        }

        PvrDbRecordInfo pvrDbRecordInfo = new PvrDataManager(context).queryDataFromTable(pvrRecIdx);
        List<ProgramInfo.SubtitleInfo> subtitleInfoList = pvrDbRecordInfo.getSubtitleInfo();

        if (subtitleInfoList == null) {
            Log.e(TAG, "updateRecordedProgramSubtitleTracks: subtitleInfoList is null");
            clearSubtitleTracks();
            return;
        }

        SubtitleInfo subInfo = new SubtitleInfo();
        for (ProgramInfo.SubtitleInfo info : subtitleInfoList) {
            SubtitleInfo.SubtitleComponent subtitleComponent = new SubtitleInfo.SubtitleComponent();
            subtitleComponent.setPid(info.getPid());
            subtitleComponent.setLangCode(info.getLang());
            subtitleComponent.setType(info.getType());
            subtitleComponent.setComPageId(info.getComPageId());
            subtitleComponent.setAncPageId(info.getAncPageId());
            subtitleComponent.setPos(subtitleInfoList.indexOf(info));
            subInfo.Component.add(subtitleComponent);
        }

        int curPos = subInfo.getCurPos();
        List<SubtitleInfo.SubtitleComponent> list = subInfo.getComponentList();

        Log.d(TAG, "updateRecordedProgramSubtitleTracks: " +
                "curPos=" + curPos + " count=" + list.size());

        mSubtitleTracks.clear();
        mSubtitleMap.clear();

        for (int i = 0; i < list.size(); i++) {
            SubtitleInfo.SubtitleComponent c = list.get(i);
            String trackId = buildSubtitleTrackId(i, c);

            TvTrackInfo.Builder builder =
                    new TvTrackInfo.Builder(TvTrackInfo.TYPE_SUBTITLE, trackId);

            builder.setLanguage(c.getLangCode());

            android.os.Bundle extra = new android.os.Bundle();
            extra.putInt("pid", c.getPid());
            extra.putInt("type", c.getType());
            extra.putInt("comp_page_id", c.getComPageId());
            extra.putInt("anc_page_id", c.getAncPageId());
            extra.putInt("pos", c.getPos());
            builder.setExtra(extra);

            TvTrackInfo track = builder.build();
            mSubtitleTracks.add(track);
            mSubtitleMap.put(trackId, c);

            Log.d(TAG, "updateRecordedProgramSubtitleTracks subtitle track[" + i + "]: " +
                    "id=" + trackId + " pid=" + c.getPid() +
                    " lang=" + c.getLangCode() + " type=" + c.getType());
        }

        if (curPos >= 0 && curPos < mSubtitleTracks.size()) {
            mSelectedSubtitleId = mSubtitleTracks.get(curPos).getId();
        } else if (!mSubtitleTracks.isEmpty()) {
            mSelectedSubtitleId = mSubtitleTracks.get(0).getId();
        } else {
            mSelectedSubtitleId = null;
        }
    }

    private void clearSubtitleTracks() {
        mSubtitleTracks.clear();
        mSubtitleMap.clear();
        mSelectedSubtitleId = null;
    }

    private String buildSubtitleTrackId(int index, SubtitleInfo.SubtitleComponent c) {
        return "subtitle_" + index + "_" + c.getPid();
    }

    // ===========================
    //      統一通知 TIF
    // ===========================

    public void rebuildAndNotify() {
        List<TvTrackInfo> all = new ArrayList<>(mAudioTracks.size() + mSubtitleTracks.size());
        all.addAll(mAudioTracks);
        all.addAll(mSubtitleTracks);

        mCallback.notifyTracksChanged(all);

        // audio selected
        mCallback.notifyTrackSelected(TvTrackInfo.TYPE_AUDIO, mSelectedAudioId);

        // subtitle：若 CC 關閉就回報 null
        String subtitleToReport =
                (mCallback.isCaptionEnabled() ? mSelectedSubtitleId : null);
        mCallback.notifyTrackSelected(TvTrackInfo.TYPE_SUBTITLE, subtitleToReport);
    }

    // ===========================
    //      Session 用的 Getter / Setter
    // ===========================

    @Nullable
    public AudioInfo.AudioComponent getAudioComponent(String trackId) {
        return mAudioMap.get(trackId);
    }

    @Nullable
    public SubtitleInfo.SubtitleComponent getSubtitleComponent(String trackId) {
        return mSubtitleMap.get(trackId);
    }

    public void setSelectedAudioId(@Nullable String trackId) {
        mSelectedAudioId = trackId;
    }

    public void setSelectedSubtitleId(@Nullable String trackId) {
        mSelectedSubtitleId = trackId;
    }

    public int getAudioTrackCount() {
        return mAudioTracks.size();
    }

    @Nullable
    public String getAudioTrackIdAt(int index) {
        if (index < 0 || index >= mAudioTracks.size()) return null;
        return mAudioTracks.get(index).getId();
    }

    @Nullable
    public String getSelectedAudioId() {
        return mSelectedAudioId;
    }

    public int getSubtitleTrackCount() {
        return mSubtitleTracks.size();
    }

    @Nullable
    public String getSubtitleTrackIdAt(int index) {
        if (index < 0 || index >= mSubtitleTracks.size()) return null;
        return mSubtitleTracks.get(index).getId();
    }

    @Nullable
    public String getSelectedSubtitleId() {
        return mSelectedSubtitleId;
    }

    public List<TvTrackInfo> getTracks() {
        List<TvTrackInfo> all = new ArrayList<>(mAudioTracks.size() + mSubtitleTracks.size());
        all.addAll(mAudioTracks);
        all.addAll(mSubtitleTracks);
        return all;
    }
    
}
