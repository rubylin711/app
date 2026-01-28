package com.mtest.activity;

import android.app.Application;
import android.widget.MediaController;
import android.widget.VideoView;

import com.prime.dtvplayer.Sysdata.TpInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will_feng on 2017/8/24.
 */

public class Global_Variables extends Application {
    final String TAG = "MTEST Global variables";
    private boolean[] m_land_changed = new boolean[28];
    private boolean cur_VideoUrl_change_status = false;
    private String cur_lang;
    private VideoView videoView;
    private MediaController m_mediaController;
    private String cur_video_url = "udp://@:1234";
    private boolean m_IPTV_Selected = false;
    private boolean mStableTestEnabled = false; // Johnny 20181029 for stable test
    private boolean mIsRecAvailable = true;

    private int mCurTunerType = 0;

    //DVBC
    public int mFrequency_DVBC = 402;
    public int mSymbolrate_DVBC = 6875;
    public int mQam_DVBC = 4;//0:16QAM, 1:32QAM, 2:64QAM, 3:128QAM, 4:256QAM

    //DVBS
    public int mFrequency_DVBS = 1000;
    public int mSymbolrate_DVBS = 27500;

    //DVBT

    //ISDBT
    public int mFrequency_ISDBT = 485143;
    public int mBand_ISDBT = 0;// 0 : 6Mhz, 1 : 7Mhz, 2 : 8Mhz

    public int mtunerID = 0;
    public int mtpID = 0;
    public int msatID = 0;
    public int msearch_mode = 1;// 1 : manaul  0 : auto
    public int mScanMode = 0;// 0 : All  1 : FTA  2 : $
    public int mChannelType = 0;// 0 : All  1 : TV  2 : Radio
    public List<TpInfo> mtpList = new ArrayList<>();
    private int mIPStreamStatus = 0;

    private int mMicInputGain = 6;  // 6 = +30dB
    private int mMicLInputGain = 4; // 4 = 0dB
    private int mMicRInputGain = 4; // 4 = 0dB
    private int mMicAlcGain = 153;  // 153 = +3dB


    public void m_SetLangChanged(int idx, boolean changed) {
        this.m_land_changed[idx] = changed;
    }

    public void m_SetCurLang(String strLang) {
        this.cur_lang = strLang;
    }

    public void m_SetVideoView(VideoView vView) {
        this.videoView = vView;
    }

    public void m_SetMediaController(MediaController tmp_mediaController) {
        this.m_mediaController = tmp_mediaController;
    }

    public void m_SetCurVideoUrl(String strVideoUrl) {
        this.cur_video_url = strVideoUrl;
    }

    public void m_set_VideoUrl_change_status(boolean strVideoUrl_change_status) {
        this.cur_VideoUrl_change_status = strVideoUrl_change_status;
    }

    public void m_SetTunerType(int intTunerType) {
        //Log.d(TAG, "m_SetTunerType:" + Integer.toString(intTunerType));
        this.mCurTunerType = intTunerType;
    }

    public void m_SetIPTVActive(boolean tmpStatus) {
        //Log.d(TAG, "m_SetIPTVActive:" + Boolean.toString(tmpStatus));
        m_IPTV_Selected = tmpStatus;
    }

    public void m_SetIPStreamStatus(int tmpStatus) {
        //Log.d(TAG, "m_SetIPStreamStatus:" + Integer.toString(tmpStatus));
        mIPStreamStatus = tmpStatus;
    }


    public boolean m_GetLangChanged(int idx) {
        return m_land_changed[idx];
    }


    public String m_GetCurLang() {
        return cur_lang;
    }

    public VideoView m_GetVideoView() {
        return videoView;
    }

    public MediaController m_GetMediaController() {
        return m_mediaController;
    }

    public String m_GetCurVideoUrl() {
        return cur_video_url;
    }

    public boolean m_GetCurVideoChangeStatus() {
        return cur_VideoUrl_change_status;
    }

    public int m_GetTunerType() {
        //Log.d(TAG, "m_GetTunerType:" + Integer.toString(mCurTunerType));
        return mCurTunerType;
    }

    public boolean m_GetIPTVActive() {
        //Log.d(TAG, "m_GetIPTVActive:" + Boolean.toString(m_IPTV_Selected));
        return m_IPTV_Selected;
    }

    public int m_GetIPStreamStatus() {
        //Log.d(TAG, "m_GetIPStreamStatus:" + Integer.toString(mIPStreamStatus));
        return mIPStreamStatus;
    }

    public void setStableTestEnabled(boolean enabled) { // Johnny 20181029 for stable test
        mStableTestEnabled = enabled;
    }

    public boolean isStableTestEnabled() {  // Johnny 20181029 for stable test
        return mStableTestEnabled;
    }

    public void setMicInputGain(int progress) {
        mMicInputGain = progress;
    }

    public void setMicLInputGain(int progress) {
        mMicLInputGain = progress;
    }

    public void setMicRInputGain(int progress) {
        mMicRInputGain = progress;
    }

    public void setMicAlcGain(int progress) {
        mMicAlcGain = progress;
    }

    public int getMicInputGain() {
        return mMicInputGain;
    }

    public int getMicLInputGain() {
        return mMicLInputGain;
    }

    public int getMicRInputGain() {
        return mMicRInputGain;
    }

    public int getMicAlcGain() {
        return mMicAlcGain;
    }

    public void setRecAvailable(boolean available) {
        mIsRecAvailable = available;
    }

    public boolean isRecAvailable() {
        return mIsRecAvailable;
    }
}
