package com.prime.dtv.service.cnsad.adparse;

import androidx.annotation.Nullable;

/** <playMode type="interval|random" subtype="bytime" value="10"/> */ 
public final class PlayMode {
    private final PlayModeType type;
    @Nullable private final String subtype;
    @Nullable private final Integer valueSec;

    public PlayMode(PlayModeType type, @Nullable String subtype, @Nullable Integer valueSec) {
        this.type = (type == null) ? PlayModeType.INTERVAL : type;
        this.subtype = subtype;
        this.valueSec = valueSec;
    }

    public PlayModeType getType() { return type; }
    @Nullable public String getSubtype() { return subtype; }
    @Nullable public Integer getValueSec() { return valueSec; }
}
