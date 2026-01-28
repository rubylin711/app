package com.prime.homeplus.tv.data;

import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.Program;

import com.prime.homeplus.tv.utils.ProgramRatingUtils;

public class ProgramReminderData {

    private long programId;
    private String programName;
    private long channelId;
    private String channelNumber;
    private String channelName;
    private long startTimeUtcMillis;
    private long endTimeUtcMillis;
    private int contentRating;

    public ProgramReminderData(Program pg, Channel ch) {
        this.programId = pg.getId();
        this.programName = pg.getTitle();
        this.channelId = ch.getId();
        this.channelNumber = ch.getDisplayNumber();
        this.channelName = ch.getDisplayName();
        this.startTimeUtcMillis = pg.getStartTimeUtcMillis();
        this.endTimeUtcMillis = pg.getEndTimeUtcMillis();
        this.contentRating = ProgramRatingUtils.getNowRating(pg.getContentRatings());
    }

    public ProgramReminderData(long programId, String programName,
                               long channelId, String channelNumber, String channelName,
                               long startTimeUtcMillis, long endTimeUtcMillis,
                               int contentRating) {
        this.programId = programId;
        this.programName = programName;
        this.channelId = channelId;
        this.channelNumber = channelNumber;
        this.channelName = channelName;
        this.startTimeUtcMillis = startTimeUtcMillis;
        this.endTimeUtcMillis = endTimeUtcMillis;
        this.contentRating = contentRating;
    }

    public long getProgramId() {
        return programId;
    }

    public void setProgramId(long programId) {
        this.programId = programId;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
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

    public int getContentRating() {
        return contentRating;
    }

    public void setContentRating(int contentRating) {
        this.contentRating = contentRating;
    }

    @Override
    public String toString() {
        return "ProgramReminderData{" +
                "programId='" + programId + '\'' +
                ", programName='" + programName + '\'' +
                ", channelId='" + channelId + '\'' +
                ", channelNumber='" + channelNumber + '\'' +
                ", channelName='" + channelName + '\'' +
                ", startTimeUtcMillis=" + startTimeUtcMillis +
                ", endTimeUtcMillis=" + endTimeUtcMillis +
                ", contentRating='" + contentRating + '\'' +
                '}';
    }

    public String toStorageString() {
        return programId + "|" + programName + "|" + channelId + "|" + channelNumber + "|" + channelName + "|" +
                startTimeUtcMillis + "|" + endTimeUtcMillis + "|" + contentRating;
    }

    public static ProgramReminderData fromStorageString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 8) return null;
        return new ProgramReminderData(
                Long.parseLong(parts[0]), parts[1], Long.parseLong(parts[2]),
                parts[3], parts[4], Long.parseLong(parts[5]),
                Long.parseLong(parts[6]), Integer.parseInt(parts[7])
        );
    }
}


