package com.prime.datastructure.sysdata;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SeriesInfo { // old series_info_list

    private long channelId; // old uniqueNo
    private final List<Series> seriesList = new ArrayList<>();


    public SeriesInfo() {
        channelId = 0;
    }

    public SeriesInfo(long channelId, List<Series> seriesList) {
        this.channelId = channelId;
        this.seriesList.addAll(seriesList);
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public int getNumberOfSeries() {
        return seriesList.size();
    }

    public List<Series> getSeriesList() {
        return seriesList;
    }

    public void addSeries(@NonNull Series series) {
        int index = indexOfSeries(series.seriesKey);
        if (index < 0) { // not exist
            seriesList.add(series);
        } else { // exist
            updateSeries(series);
        }
    }

    public void addSeries(byte[] seriesKey, List<Episode> episodeList) {
        Series series = new Series(seriesKey, episodeList);
        addSeries(series);
    }

    public int indexOfSeries(byte[] seriesKey) {
        int index = -1;
        for (int i = 0; i < seriesList.size(); i++) {
            if (Arrays.equals(seriesKey, seriesList.get(i).seriesKey)) {
                index = i;
                break;
            }
        }

        return index;
    }

    public int indexOfSeries(@NonNull Series series) {
        return indexOfSeries(series.seriesKey);
    }

    public Series getSeriesByKey(byte[] seriesKey) {
        Series series = null;
        int index = indexOfSeries(seriesKey);
        if (index >= 0) {
            series = seriesList.get(index);
        }

        return series;
    }

    public void updateSeries(@NonNull Series series) {
        int index = indexOfSeries(series.seriesKey);
        if (index >= 0) {
            seriesList.set(index, series);
        }
    }

    public void removeSeries(@NonNull Series series) {
        int index = indexOfSeries(series.seriesKey);
        if (index >= 0) {
            seriesList.remove(index);
        }
    }

    public Episode getEpisode(byte[] seriesKey, int episodeKey) {
        Series series = getSeriesByKey(seriesKey);
        Episode episode = null;
        if (series != null) {
            episode = series.getEpisodeByKey(episodeKey);
        }

        return episode;
    }

    public void addEpisode(byte[] seriesKey, Episode episode) {
        Series series = getSeriesByKey(seriesKey);
        if (series != null) {
            series.addEpisode(episode);
        }
    }

    public void updateEpisode(byte[] seriesKey, Episode episode) {
        Series series = getSeriesByKey(seriesKey);
        if (series != null) {
            series.updateEpisode(episode);
        }
    }

    public void removeEpisode(byte[] seriesKey, Episode episode) {
        Series series = getSeriesByKey(seriesKey);
        if (series != null) {
            series.removeEpisode(episode);
        }
    }

    public void sortSeries(byte[] seriesKey) {
        Series series = getSeriesByKey(seriesKey);
        series.sort();
    }

    public void sortAllSeries() {
        for (Series series : seriesList) {
            series.sort();
        }
    }

    @NonNull
    @Override
    public String toString() {

        return "SeriesInfo{" +
                "channelId=" + channelId +
                ", seriesList=" + seriesList +
                '}';
    }

    public static class Series { // old SeriesInfo
        public static final int MAX_SERIES_KEY_LENGTH = 256; // this must be the same as dtvservice
        private byte[] seriesKey; // identify a unique series
        private final List<Episode> episodeList = new ArrayList<>();

        public Series() {
            seriesKey = new byte[0];
        }

        public Series(byte[] seriesKey, List<Episode> episodeList) {
            this.seriesKey = seriesKey;
            this.episodeList.addAll(episodeList);
        }

        public byte[] getSeriesKey() {
            return seriesKey;
        }

        public void setSeriesKey(byte[] seriesKey) {
            this.seriesKey = seriesKey;
        }

        public int getNumberOfEpisode() {
            return episodeList.size();
        }

        public List<Episode> getEpisodeList() {
            return episodeList;
        }

        public void addEpisode(@NonNull Episode episode) {
            int index = indexOfEpisode(episode.episodeKey);
            if (index < 0) { // not exist
                episodeList.add(episode);
            } else { // exist
                episodeList.set(index, episode);
            }
        }

        public int indexOfEpisode(int episodeKey) {
            int index = -1;
            for (int i = 0; i < episodeList.size(); i++) {
                if (episodeKey == episodeList.get(i).episodeKey) {
                    index = i;
                    break;
                }
            }

            return index;
        }

        public int indexOfEpisode(@NonNull Episode episode) {
            return indexOfEpisode(episode.episodeKey);
        }

        public Episode getEpisodeByKey(int episodeKey) {
            Episode episode = null;
            int index = indexOfEpisode(episodeKey);
            if (index >= 0) {
                episode = episodeList.get(index);
            }

            return episode;
        }

        public void updateEpisode(@NonNull Episode episode) {
            int index = indexOfEpisode(episode.episodeKey);
            if (index >= 0) {
                episodeList.set(index, episode);
            }
        }

        public void removeEpisode(@NonNull Episode episode) {
            int index = indexOfEpisode(episode.episodeKey);
            if (index >= 0) {
                episodeList.remove(index);
            }
        }

        // sort by start time
        public void sort() {
            episodeList.sort(Comparator.comparing(episode -> episode.startLocalDateTime));
        }

        @NonNull
        @Override
        public String toString() {
            return "Series{" +
                    "seriesKey=" + Arrays.toString(seriesKey) +
                    ", episodeList=" + episodeList +
                    '}';
        }
    }

    public static class Episode { // old SeriesInfo_sub
        public static final int STATUS_NORMAL = 0;
        public static final int STATUS_PREMIER = 1;
        public static final int STATUS_FINALE = 2;

        private int episodeKey;   // identify a unique episode
        private int episodeStatus; // 0=normal, 1=premier, 2=finale
        private boolean lastEpisode;  // indicate the last episode
        private LocalDateTime startLocalDateTime;
        private int duration; // seconds
        private String eventName;

        public Episode() {
            this.episodeKey = 0;
            this.episodeStatus = STATUS_NORMAL;
            this.lastEpisode = false;
            this.startLocalDateTime = LocalDateTime.now();
            this.duration = 0;
            this.eventName = "";
        }

        public Episode(int episodeKey, int episodeStatus, boolean lastEpisode,
                       LocalDateTime startLocalDateTime, int duration, String eventName) {
            this.episodeKey = episodeKey;
            this.episodeStatus = episodeStatus;
            this.lastEpisode = lastEpisode;
            this.startLocalDateTime = startLocalDateTime;
            this.duration = duration;
            this.eventName = eventName;
        }


        public int getEpisodeKey() {
            return episodeKey;
        }

        public void setEpisodeKey(int episodeKey) {
            this.episodeKey = episodeKey;
        }

        public int getEpisodeStatus() {
            return episodeStatus;
        }

        public void setEpisodeStatus(int episodeStatus) {
            this.episodeStatus = episodeStatus;
        }

        public boolean isLastEpisode() {
            return lastEpisode;
        }

        public void setLastEpisode(boolean lastEpisode) {
            this.lastEpisode = lastEpisode;
        }

        public LocalDateTime getStartLocalDateTime() {
            return startLocalDateTime;
        }

        public void setStartLocalDateTime(LocalDateTime startLocalDateTime) {
            this.startLocalDateTime = startLocalDateTime;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        @NonNull
        @Override
        public String toString() {
            return "Episode{" +
                    "episodeKey=" + episodeKey +
                    ", episodeStatus=" + episodeStatus +
                    ", lastEpisode=" + lastEpisode +
                    ", startLocalDateTime=" + startLocalDateTime +
                    ", duration=" + duration +
                    ", eventName='" + eventName + '\'' +
                    '}';
        }
    }

}
