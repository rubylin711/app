package com.prime.dtv.sysdata;

import java.util.ArrayList;
import java.util.List;

public class PvrRecFileInfo {
    private static final String TAG="PvrRecFileInfo";
    private int mMasterIdx;             //Master table index //PVR_REC
    private int mSeriesIdx;             //Series table index //PVR_REC_MasterId
    private boolean mIsSeries = false;              //Save in Master table; 1 : series, 0: not series
    private int mEpisode=0;
    private int mChannelNo;
    private int mChannelLock;           //parent_lock;
    private int mRating;                //parent_level;
    private int mRecordStatus;          //1 means success, 0 means failure
    private int mPlayStopPos;
    private int mTotalEpisode=0;          //save in Master table (only series reocrds need it)
    private int mTotalRecordTime;       //save in Master table or Series table
    private long mChannelId;             //save in Master table & Series table for series records
    private long mStartTime;
    private int mDurationSec;
    private int mServiceType;
    private int mFileSize;
    private String mChName = "";
    private String mEventName = "";
    private String mFullNamePath = "";
    private ProgramInfo.VideoInfo mVideo = null;
    private List<ProgramInfo.AudioInfo> mAudiosList = null;//new ArrayList<ProgramInfo.AudioInfo>();
    private List<ProgramInfo.SubtitleInfo> mSubtitleList = null;//new ArrayList<ProgramInfo.SubtitleInfo>();
    private List<ProgramInfo.TeletextInfo> mTeletextList = null;//new ArrayList<ProgramInfo.TeletextInfo>();
    private EPGEvent mEpgEvent = null;

    private long mPlaybackTime = 0;

    public PvrRecFileInfo(){
        mAudiosList = new ArrayList<ProgramInfo.AudioInfo>();
        mSubtitleList = new ArrayList<ProgramInfo.SubtitleInfo>();
        mTeletextList = new ArrayList<ProgramInfo.TeletextInfo>();
    }

    public PvrRecFileInfo(PvrDbRecordInfo pvrDbRecordInfo){
        mMasterIdx = pvrDbRecordInfo.getMasterIdx();
        mSeriesIdx = pvrDbRecordInfo.getSeriesIdx();
        mIsSeries = pvrDbRecordInfo.getIsSeries();
        if(mIsSeries == true ){
            mEpisode = pvrDbRecordInfo.getEpisode();
            mTotalEpisode = pvrDbRecordInfo.getTotalEpisode();
        }

        mChannelNo = pvrDbRecordInfo.getChannelNo();
        mChannelLock = pvrDbRecordInfo.getChannelLock();
        mRating = pvrDbRecordInfo.getRating();
        mRecordStatus = pvrDbRecordInfo.getRecordStatus();
        mPlayStopPos = pvrDbRecordInfo.getPlayStopPos();
        mTotalRecordTime = pvrDbRecordInfo.getTotalRecordTime();
        mChannelId = pvrDbRecordInfo.getChannelId();
        mStartTime = pvrDbRecordInfo.getStartTime();
        mDurationSec = pvrDbRecordInfo.getDurationSec();
        mServiceType = pvrDbRecordInfo.getServiceType();
        mFileSize = pvrDbRecordInfo.getFileSize();
        mChName = pvrDbRecordInfo.getChName();
        mEventName = pvrDbRecordInfo.getEventName();
        mFullNamePath = pvrDbRecordInfo.getFullNamePath();
        mVideo = pvrDbRecordInfo.getVideoInfo();
        mAudiosList = pvrDbRecordInfo.getAudiosInfoList();
        mSubtitleList = pvrDbRecordInfo.getSubtitleInfo();
        mTeletextList = pvrDbRecordInfo.getTeletextList();
        mEpgEvent = pvrDbRecordInfo.getEpgInfo();
        mPlaybackTime = pvrDbRecordInfo.getPlaybackTime();
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

    public boolean isSeries(){
        return mIsSeries;
    }
    public void setIsSeries(boolean IsSeries){
        mIsSeries = IsSeries;
    }
    public long getPlaybackTime(){
        return mPlaybackTime;
    }
    public void setPlaybackTime(long PlaybackTime){
        PlaybackTime = mPlaybackTime;
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
    public void getFileSize(int FileSize){
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

    public EPGEvent getEpgEvent(){
        return mEpgEvent;
    }
    public void setEpgEvent(EPGEvent EpgEvent){
        mEpgEvent = EpgEvent;
    }

    public ProgramInfo.VideoInfo getVideoInfo(){
        return mVideo;
    }
    public void setVideoInfo(ProgramInfo.VideoInfo Video){
        mVideo = Video;
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

    public PvrRecIdx getPvrRecIdx() {
        return new PvrRecIdx(mMasterIdx, mSeriesIdx);
    }
}
