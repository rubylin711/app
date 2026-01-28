package com.prime.dtv.sysdata;

import java.util.ArrayList;
import java.util.List;

public class PvrDbRecordInfo {
    private static final String TAG="PvrDbRecordInfo";
    public static final int MAX_SERIES_KEY_LEN = 80;
    private int mMasterIdx;             //Master table index //PVR_REC
    private int mSeriesIdx;             //Series table index //PVR_REC_MasterId
    private boolean mIsSeries;              //Save in Master table; 1 : series, 0: not series
    private int mEpisode;
    private int mChannelNo;
    private int mChannelLock;           //parent_lock;
    private int mRating;                //parent_level;
    private int mRecordStatus;          //1 means success, 0 means failure
    private int mPlayStopPos;
    private int mTotalEpisode;          //save in Master table (only series reocrds need it)
    private int mTotalRecordTime;       //save in Master table or Series table
    private long mChannelId;             //save in Master table & Series table for series records
    private long mStartTime;
    private long mPlayTime;
    private int mDurationSec;
    private int mServiceType;
    private int mFileSize;
    private String mChName;
    private String mEventName;
    private String mFullNamePath;
    private byte[] mSeriesKey;
    private ProgramInfo.VideoInfo mVideo ;
    private List<ProgramInfo.AudioInfo> mAudiosList;// = new ArrayList<ProgramInfo.AudioInfo>();
    private List<ProgramInfo.SubtitleInfo> mSubtitleList;// = new ArrayList<ProgramInfo.SubtitleInfo>();
    private List<ProgramInfo.TeletextInfo> mTeletextList;// = new ArrayList<ProgramInfo.TeletextInfo>();
    private EPGEvent mEpgEvent;

    private long mPlaybackTime;

    public PvrDbRecordInfo(){
        mMasterIdx = -1;
        mSeriesIdx = 0xFFFF;
        mIsSeries = false;
        mEpisode = 0;
        mChannelNo = 0;
        mChannelLock = 0;
        mRating = 0;
        mRecordStatus = 0;
        mPlayStopPos = -1;
        mTotalEpisode = 0;
        mTotalRecordTime = 0;
        mChannelId = 0;
        mStartTime = 0;
        mPlayTime = 0;
        mDurationSec = 0;
        mServiceType = 0;
        mFileSize = 0;
        mChName = "";
        mEventName = "";
        mFullNamePath = "";
        mSeriesKey = new byte[MAX_SERIES_KEY_LEN];
        mVideo = null;
        mAudiosList = new ArrayList<ProgramInfo.AudioInfo>();
        mSubtitleList = new ArrayList<ProgramInfo.SubtitleInfo>();
        mTeletextList = new ArrayList<ProgramInfo.TeletextInfo>();
        mEpgEvent = null;
        mPlaybackTime = 0;
    }



    public int getMasterIdx(){
        return mMasterIdx;
    }
    public void setMasterIdx(int MasterIdx){
        mMasterIdx = MasterIdx;
    }

    public int getSeriesIdx(){
        return mSeriesIdx;
    }
    public void setSeriesIdx(int SeriesIdx){
        mSeriesIdx = SeriesIdx;
    }

    public boolean getIsSeries(){
        return mIsSeries;
    }
    public void setIsSeries(boolean IsSeries){
        mIsSeries = IsSeries;
    }

    public int getEpisode(){
        return mEpisode;
    }
    public void setEpisode(int Episode){
        mEpisode = Episode;
    }

    public int getChannelNo(){
        return mChannelNo;
    }
    public void setChannelNo(int ChannelNo){
        mChannelNo = ChannelNo;
    }

    public int getChannelLock(){
        return mChannelLock;
    }
    public void setChannelLock(int ChannelLock){
        mChannelLock = ChannelLock;
    }

    public int getRating(){
        return mRating;
    }
    public void setRating(int Rating){
        mRating = Rating;
    }

    public int getRecordStatus(){
        return mRecordStatus;
    }
    public void setRecordStatus(int RecordStatus){
        mRecordStatus = RecordStatus;
    }

    public int getPlayStopPos(){
        return mPlayStopPos;
    }
    public void setPlayStopPos(int PlayStopPos){
        mPlayStopPos = PlayStopPos;
    }

    public int getTotalEpisode(){
        return mTotalEpisode;
    }
    public void setTotalEpisode(int TotalEpisode){
        mTotalEpisode = TotalEpisode;
    }

    public int getTotalRecordTime(){
        return mTotalRecordTime;
    }
    public void setTotalRecordTime(int TotalRecordTime){
        mTotalRecordTime = TotalRecordTime;
    }

    public long getChannelId(){
        return mChannelId;
    }
    public void setChannelId(long ChannelId){
        mChannelId = ChannelId;
    }

    public long getStartTime(){
        return mStartTime;
    }
    public void setStartTime(long StartTime){
        mStartTime = StartTime;
    }

    public long getPlayTime(){
        return mPlayTime;
    }
    public void setPlayTime(long PlayTime){
        mPlayTime = PlayTime;
    }

    public int getDurationSec(){
        return mDurationSec;
    }
    public void setDurationSec(int DurationSec){
        mDurationSec = DurationSec;
    }

    public int getServiceType(){
        return mServiceType;
    }
    public void setServiceType(int ServiceType){
        mServiceType = ServiceType;
    }

    public int getFileSize(){
        return mFileSize;
    }
    public void setFileSize(int FileSize){
        mFileSize = FileSize;
    }

    public String getChName(){
        return mChName;
    }
    public void setChName(String ChName){
        if(ChName != null){
            mChName = ChName;
        }
    }

    public String getEventName(){
        return mEventName;
    }
    public void setEventName(String EventName){
        if(mEventName != null){
            mEventName = mEventName;
        }
    }

    public String getFullNamePath(){
        return mFullNamePath;
    }
    public void setFullNamePath(String FullNamePath){
        if(FullNamePath != null){
            mFullNamePath = FullNamePath;
        }
    }

    public byte[] getSeriesKey() {
        return mSeriesKey.clone();
    }
    public void setSeriesKey(byte[] SeriesKey) {
        if(SeriesKey != null) {
            int copyLength = Math.min(SeriesKey.length, MAX_SERIES_KEY_LEN);
            System.arraycopy(SeriesKey, 0, mSeriesKey, 0, copyLength);
        }
    }

    public ProgramInfo.VideoInfo getVideoInfo(){
        return mVideo;
    }
    public void setVideoInfo(ProgramInfo.VideoInfo Video){
        mVideo = Video;
    }

    public EPGEvent getEpgInfo(){
        return mEpgEvent;
    }
    public void setEpgInfo(EPGEvent EpgEvent){
        mEpgEvent = EpgEvent;
    }
    public long getPlaybackTime(){
        return mPlaybackTime;
    }

    public List<ProgramInfo.AudioInfo> getAudiosInfoList(){
        return mAudiosList;
    }
    public void setAudiosInfoList(List<ProgramInfo.AudioInfo> AudioInfo){
        //mAudiosList.clear();
        //mAudiosList.addAll(AudioInfo);
        mAudiosList = AudioInfo;
    }

    public List<ProgramInfo.SubtitleInfo> getSubtitleInfo(){
        return mSubtitleList;
    }
    public void setSubtitleInfo(List<ProgramInfo.SubtitleInfo> SubtitleList){
        //mSubtitleList.clear();
        //mSubtitleList.addAll(SubtitleList);
        mSubtitleList = SubtitleList;
    }

    public List<ProgramInfo.TeletextInfo> getTeletextList(){
        return mTeletextList;
    }
    public void setTeletextList(List<ProgramInfo.TeletextInfo> TeletextList){
        //mTeletextList.clear();
        //mTeletextList.addAll(TeletextList);
        mTeletextList = TeletextList;
    }
}
