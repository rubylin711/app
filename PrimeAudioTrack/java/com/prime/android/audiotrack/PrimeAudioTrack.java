package com.prime.android.audiotrack;

import android.media.AudioTrack;
import android.annotation.NonNull;

public class PrimeAudioTrack extends AudioTrack {
    public PrimeAudioTrack(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes, int mode, int sessionId) throws IllegalArgumentException {
        super(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode, sessionId);
    }
    public static class TunerConfiguration extends AudioTrack.TunerConfiguration
    {
        public TunerConfiguration(int contentId, int syncId) {
            super(contentId, syncId);
        }
    }
    public static class Builder extends AudioTrack.Builder
    {
        public @NonNull Builder setTunerConfiguration(
                @NonNull TunerConfiguration tunerConfiguration) {
            super.setTunerConfiguration(tunerConfiguration);
            return this ;
        }
    }
}
