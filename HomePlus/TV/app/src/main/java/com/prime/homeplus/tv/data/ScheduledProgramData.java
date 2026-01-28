package com.prime.homeplus.tv.data;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.Program;
import androidx.tvprovider.media.tv.TvContractCompat;
import androidx.tvprovider.media.tv.TvContractUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class ScheduledProgramData implements Serializable {

    private long id;
    private String packageName;
    private String inputId;
    private long channelId;
    private String channelNumber;
    private String channelName;
    private String title;
    private String episodeDisplayNumber;
    private String episodeTitle;
    private long startTimeUtcMillis;
    private long endTimeUtcMillis;
    private String longDescription;
    private String contentRatings;
    private String recordingDataUri;
    private long recordingDataBytes;
    private long recordingDurationMillis;
    private int eventId;
    private String seriesId;
    private byte[] internalProviderData;
    private long internalProviderFlag1;
    private long internalProviderFlag2;
    private long internalProviderFlag3;
    private long internalProviderFlag4;
    private int versionNumber;

    private Uri programUri;
    private int cycle;
    private int weekMask;

    public ScheduledProgramData() {
    }

    // for Program-Based recording
    public ScheduledProgramData(Program pg, Channel ch) {
        if (pg == null || ch == null) {
            return;
        }

        this.id = pg.getId();
        this.packageName = ch.getPackageName();
        this.inputId = ch.getInputId();
        this.channelId = pg.getChannelId();
        this.channelNumber = ch.getDisplayNumber();
        this.channelName = ch.getDisplayName();
        this.title = pg.getTitle();
        this.episodeDisplayNumber = pg.getEpisodeNumber();
        this.episodeTitle = pg.getEpisodeTitle();
        this.startTimeUtcMillis = pg.getStartTimeUtcMillis();
        this.endTimeUtcMillis = pg.getEndTimeUtcMillis();
        this.longDescription = pg.getLongDescription();
        this.contentRatings = TvContractUtils.contentRatingsToString(pg.getContentRatings());
        this.eventId = pg.getEventId();
        this.seriesId = pg.getSeriesId();

        // TODO: TBC
        this.recordingDataUri = "";
        this.recordingDataBytes = 0;
        this.recordingDurationMillis = 0;

        this.programUri = TvContractCompat.buildProgramUri(pg.getId());
        this.cycle = 0; // cycle should be set to ONE_TIME or SERIES later
        this.weekMask = 0; // no week for Program-Based recording
    }

    // for manual recording(Time-Based recording)
    public ScheduledProgramData(
            Channel ch,
            String contentRatings,
            long startTimeUtcMillis,
            long endTimeUtcMillis,
            int cycle,
            int weekMask) {
        if (ch == null) {
            return;
        }

        // ensure id is in Int range to match our bookinfo id define
        this.id = Math.abs(Objects.hash(ch.getId(), startTimeUtcMillis));

        this.packageName = ch.getPackageName();
        this.inputId = ch.getInputId();
        this.channelId = ch.getId();
        this.channelNumber = ch.getDisplayNumber();
        this.channelName = ch.getDisplayName();
        this.title = ch.getDisplayName();
        this.startTimeUtcMillis = startTimeUtcMillis;
        this.endTimeUtcMillis = endTimeUtcMillis;
        this.contentRatings = contentRatings;
        this.eventId = -1; // -1 for Time-Based recording

        // TODO: TBC
        this.recordingDataUri = "";
        this.recordingDataBytes = 0;
        this.recordingDurationMillis = 0;

        this.programUri = null; // no program uri for Time-Based recording
        this.cycle = cycle;
        this.weekMask = weekMask;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public String getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(String channelNumber) {
        this.channelNumber = channelNumber;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEpisodeDisplayNumber() {
        return episodeDisplayNumber;
    }

    public void setEpisodeDisplayNumber(String episodeDisplayNumber) {
        this.episodeDisplayNumber = episodeDisplayNumber;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    public long getStartTimeUtcMillis() {
        return startTimeUtcMillis;
    }

    public void setStartTimeUtcMillis(long startTimeUtcMillis) {
        this.startTimeUtcMillis = startTimeUtcMillis;
    }

    public long getEndTimeUtcMillis() {
        return endTimeUtcMillis;
    }

    public void setEndTimeUtcMillis(long endTimeUtcMillis) {
        this.endTimeUtcMillis = endTimeUtcMillis;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getContentRatings() {
        return contentRatings;
    }

    public void setContentRatings(String contentRatings) {
        this.contentRatings = contentRatings;
    }

    public String getRecordingDataUri() {
        return recordingDataUri;
    }

    public void setRecordingDataUri(String recordingDataUri) {
        this.recordingDataUri = recordingDataUri;
    }

    public long getRecordingDataBytes() {
        return recordingDataBytes;
    }

    public void setRecordingDataBytes(long recordingDataBytes) {
        this.recordingDataBytes = recordingDataBytes;
    }

    public long getRecordingDurationMillis() {
        return recordingDurationMillis;
    }

    public void setRecordingDurationMillis(long recordingDurationMillis) {
        this.recordingDurationMillis = recordingDurationMillis;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public byte[] getInternalProviderData() {
        return internalProviderData;
    }

    public void setInternalProviderData(byte[] internalProviderData) {
        this.internalProviderData = internalProviderData;
    }

    public long getInternalProviderFlag1() {
        return internalProviderFlag1;
    }

    public void setInternalProviderFlag1(long internalProviderFlag1) {
        this.internalProviderFlag1 = internalProviderFlag1;
    }

    public long getInternalProviderFlag2() {
        return internalProviderFlag2;
    }

    public void setInternalProviderFlag2(long internalProviderFlag2) {
        this.internalProviderFlag2 = internalProviderFlag2;
    }

    public long getInternalProviderFlag3() {
        return internalProviderFlag3;
    }

    public void setInternalProviderFlag3(long internalProviderFlag3) {
        this.internalProviderFlag3 = internalProviderFlag3;
    }

    public long getInternalProviderFlag4() {
        return internalProviderFlag4;
    }

    public void setInternalProviderFlag4(long internalProviderFlag4) {
        this.internalProviderFlag4 = internalProviderFlag4;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Uri getProgramUri() {
        return programUri;
    }

    public void setProgramUri(Uri programUri) {
        this.programUri = programUri;
    }

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    public int getWeekMask() {
        return weekMask;
    }

    public void setWeekMask(int weekMask) {
        this.weekMask = weekMask;
    }

    @NonNull
    @Override
    public String toString() {
        return "ScheduledProgramData{" +
                "id=" + id +
                ", channelId=" + channelId +
                ", channelName='" + channelName + '\'' +
                ", channelNumber='" + channelNumber + '\'' +
                ", contentRatings='" + contentRatings + '\'' +
                ", cycle=" + cycle +
                ", endTimeUtcMillis=" + endTimeUtcMillis +
                ", episodeDisplayNumber='" + episodeDisplayNumber + '\'' +
                ", episodeTitle='" + episodeTitle + '\'' +
                ", eventId=" + eventId +
                ", inputId='" + inputId + '\'' +
                ", programUri=" + programUri +
                ", seriesId='" + seriesId + '\'' +
                ", startTimeUtcMillis=" + startTimeUtcMillis +
                ", title='" + title + '\'' +
                ", weekMask=" + weekMask +
                '}';
    }
}