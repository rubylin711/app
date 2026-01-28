package com.prime.homeplus.tv.data;

import com.prime.datastructure.sysdata.PvrRecIdx;

import java.io.Serializable;
import java.util.Arrays;

public class RecordedProgramData implements Serializable {

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
    private byte[] internalProviderData;
    private long internalProviderFlag1;
    private long internalProviderFlag2;
    private long internalProviderFlag3;
    private long internalProviderFlag4;
    private String internalProviderId;
    private int versionNumber;

    public RecordedProgramData() {}

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

    public String getInternalProviderId() {
        return internalProviderId;
    }

    public void setInternalProviderId(String internalProviderId) {
        this.internalProviderId = internalProviderId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public PvrRecIdx getPesiPvrRecIdx() {
        // we use COLUMN_INTERNAL_PROVIDER_ID for our pvr record index
        return PvrRecIdx.fromCombinedString(this.internalProviderId);
    }

    public long getPesiRecordStatus() {
        // we use COLUMN_INTERNAL_PROVIDER_FLAG1 for our pvr record status
        return internalProviderFlag1;
    }


    @Override
    public String toString() {
        return "RecordedProgramData{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", episodeTitle='" + episodeTitle + '\'' +
                ", recordingDataUri='" + recordingDataUri + '\'' +
                ", contentRatings='" + contentRatings + '\'' +
                ", internalProviderData=" + Arrays.toString(internalProviderData) +
                '}';
    }
}