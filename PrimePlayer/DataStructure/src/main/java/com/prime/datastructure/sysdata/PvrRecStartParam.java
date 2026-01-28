package com.prime.datastructure.sysdata;

public class PvrRecStartParam {
    private static final String TAG = "PvrRecStartParam";
    public static final int MAX_SERIES_KEY_LEN = SeriesInfo.Series.MAX_SERIES_KEY_LENGTH;
    private boolean mSeries;
    private boolean mEpisodeLast;
    private int mEpisode;
    private boolean mRecordFromTimer;
    private int mDuration;
    private byte[] mSeriesKey = new byte[MAX_SERIES_KEY_LEN];
    private EPGEvent mEpgData;
    private ProgramInfo mProgramInfo;

    public PvrRecStartParam(boolean series, boolean episodeLast, int episode, boolean recordFromTimer, int duration, byte[] seriesKey, EPGEvent epgData, ProgramInfo programInfo) {
        if (series == true) {
            if (seriesKey != null) {
                int copyLength = Math.min(seriesKey.length, MAX_SERIES_KEY_LEN);
                System.arraycopy(seriesKey, 0, mSeriesKey, 0, copyLength);
            }
        }
        mSeries = series;
        mEpisodeLast = episodeLast;
        mEpisode = episode;
        mRecordFromTimer = recordFromTimer;
        mDuration = duration;
        mEpgData = epgData;
        mProgramInfo = programInfo;
        if(mEpgData != null)
            mProgramInfo.setParentalRating(mEpgData.get_parental_rate());
    }

    public boolean getSeries() {
        return mSeries;
    }

    public boolean getEpisodeLast() {
        return mEpisodeLast;
    }

    public int getEpisode() {
        return mEpisode;
    }

    public boolean getRecordFromTimer() {
        return mRecordFromTimer;
    }

    public int getDuration() {
        return mDuration;
    }

    public byte[] getSeriesKey() {
        return mSeriesKey.clone();
    }

    public EPGEvent getEPGEvent() {
        return mEpgData;
    }
    public ProgramInfo getmProgramInfo() {
        return mProgramInfo;
    }

}
