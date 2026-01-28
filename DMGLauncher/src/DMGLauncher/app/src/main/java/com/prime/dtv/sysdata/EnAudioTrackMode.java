package com.prime.dtv.sysdata;

/**
 * Created by gary_hsu on 2018/2/7.
 */

public enum EnAudioTrackMode {
    MPEG_AUDIO_TRACK_STEREO(0),
    MPEG_AUDIO_TRACK_LEFT(1),
    MPEG_AUDIO_TRACK_RIGHT(2),
    MPEG_AUDIO_TRACK_BUTT(3);

    private int Index = 0;

    EnAudioTrackMode(int i) {
        Index = i;
    }

    public int getValue() {
        return Index;
    }

    public static EnAudioTrackMode valueOf(int ordinal)
    {
        if (ordinal == MPEG_AUDIO_TRACK_STEREO.getValue()) {
            return MPEG_AUDIO_TRACK_STEREO;
        }
        else if (ordinal == MPEG_AUDIO_TRACK_LEFT.getValue()) {
            return MPEG_AUDIO_TRACK_LEFT;
        }
        else if (ordinal == MPEG_AUDIO_TRACK_RIGHT.getValue()) {
            return MPEG_AUDIO_TRACK_RIGHT;
        }
        else {
            return MPEG_AUDIO_TRACK_BUTT;
        }
    }
}
