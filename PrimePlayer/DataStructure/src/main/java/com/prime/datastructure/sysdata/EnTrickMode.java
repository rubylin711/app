package com.prime.datastructure.sysdata;

/**
 * Created by johnny_shih on 2018/4/12.
 */

/**
 * TimeShift trick play mode enumeration:<br>
 * FAST_FORWARD_NORMAL:normal forward play,FAST_FORWARD_TWO:2X forward play<br>
 * FAST_FORWARD_FOUR:4X forward play,FAST_FORWARD_EIGHT:8X forward play<br>
 * FAST_FORWARD_SIXTEEN:16X forward play,FAST_FORWARD_THIRTYTWO:32X forward play<br>
 * FAST_FORWARD_SIXTYFOUR:64X forward play,FAST_BACKWARD_NORMAL:normal rewind play<br>
 * FAST_BACKWARD_TWO:2X rewind play,FAST_BACKWARD_FOUR:4X rewind play<br>
 * FAST_BACKWARD_EIGHT:8X rewind play,FAST_BACKWARD_SIXTEEN:16X rewind play<br>
 * FAST_BACKWARD_THIRTYTWO:32X rewind play,FAST_BACKWARD_SIXTYFOUR:64X rewind play<br>
 * SLOW_FORWARD_TWO:1/2X forward play,SLOW_FORWARD_FOUR:1/4X forward play<br>
 * SLOW_FORWARD_EIGHT:1/8X forward play,SLOW_FORWARD_SIXTEEN:1/16X forward play<br>
 * SLOW_FORWARD_THIRTYTWO:1/32X forward play<br>
 *
 * current version not support slow rewind play<br>
 * SLOW_BACKWARD_TWO:1/2X rewind play,SLOW_BACKWARD_FOUR:1/4X rewind play<br>
 * SLOW_BACKWARD_EIGHT:1/8X rewind play,SLOW_BACKWARD_SIXTEEN:1/16X rewind play<br>
 * SLOW_BACKWARD_THIRTYTWO:1/32X rewind play<br>
 * INVALID_TRICK_MODE:invalid Trick mode.<br>
 *
 * FAST_FORWARD_NORMAL:单倍速播放,FAST_FORWARD_TWO:2 倍快进播放<br>
 * FAST_FORWARD_FOUR:4 倍快进播放,FAST_FORWARD_EIGHT:8 倍快进播放<br>
 * FAST_FORWARD_SIXTEEN:16 倍快进播放,FAST_FORWARD_THIRTYTWO:32 倍快进播放<br>
 * FAST_FORWARD_SIXTYFOUR:64 倍快进播放,FAST_BACKWARD_NORMAL:单倍速倒退播放<br>
 * FAST_BACKWARD_TWO:2 倍倒退播放,FAST_BACKWARD_FOUR:4 倍倒退播放<br>
 * FAST_BACKWARD_EIGHT:8 倍倒退播放,FAST_BACKWARD_SIXTEEN:16 倍倒退播放<br>
 * FAST_BACKWARD_THIRTYTWO:32 倍倒退播放,FAST_BACKWARD_SIXTYFOUR:64 倍倒退播放<br>
 * SLOW_FORWARD_TWO:1/2 倍速播放,SLOW_FORWARD_FOUR:1/4 倍速播放<br>
 * SLOW_FORWARD_EIGHT:1/8 倍速播放,SLOW_FORWARD_SIXTEEN:1/16 倍速播放<br>
 * SLOW_FORWARD_THIRTYTWO:1/32 倍速播放<br>
 *
 * 当前版本不支持慢速回放<br>
 * SLOW_BACKWARD_TWO:1/2 倍速倒退播放,SLOW_BACKWARD_FOUR:1/4 倍速倒退播放<br>
 * SLOW_BACKWARD_EIGHT:1/8 倍速倒退播放,SLOW_BACKWARD_SIXTEEN:1/16 倍速倒退播放<br>
 * SLOW_BACKWARD_THIRTYTWO:1/32 倍速倒退播放<br>
 * INVALID_TRICK_MODE:无效倍速值.<br>
 *
 */
public enum EnTrickMode
{
    FAST_FORWARD_NORMAL(1024),
    FAST_FORWARD_TWO(2*1024),
    FAST_FORWARD_FOUR(4*1024),
    FAST_FORWARD_EIGHT(8*1024),
    FAST_FORWARD_SIXTEEN(16*1024),
    FAST_FORWARD_THIRTYTWO(32*1024),
    FAST_FORWARD_SIXTYFOUR(64*1024),
    FAST_BACKWARD_NORMAL(-1*1024),
    FAST_BACKWARD_TWO(-2*1024),
    FAST_BACKWARD_FOUR(-4*1024),
    FAST_BACKWARD_EIGHT(-8*1024),
    FAST_BACKWARD_SIXTEEN(-16*1024),
    FAST_BACKWARD_THIRTYTWO(-32*1024),
    FAST_BACKWARD_SIXTYFOUR(-64*1024),
    SLOW_FORWARD_TWO(1024/2),
    SLOW_FORWARD_FOUR(1024/4),
    SLOW_FORWARD_EIGHT(1024/8),
    SLOW_FORWARD_SIXTEEN(1024/16),
    SLOW_FORWARD_THIRTYTWO(1024/32),
    SLOW_BACKWARD_TWO(1024/(-2)),
    SLOW_BACKWARD_FOUR(1024/(-4)),
    SLOW_BACKWARD_EIGHT(1024/(-8)),
    SLOW_BACKWARD_SIXTEEN(1024/(-16)),
    SLOW_BACKWARD_THIRTYTWO(1024/(-32)),
    INVALID_TRICK_MODE(0xFFFFFFFF);
    private int mIndex = 0;

    EnTrickMode(int nIndex)
    {
        mIndex = nIndex;
    }

    public int getValue()
    {
        return mIndex;
    }

    public static EnTrickMode valueOf(int ordinal)
    {
        if (ordinal == FAST_FORWARD_NORMAL.getValue())
        {
            return FAST_FORWARD_NORMAL;
        }
        else if (ordinal == FAST_FORWARD_TWO.getValue())
        {
            return FAST_FORWARD_TWO;
        }
        else if (ordinal == FAST_FORWARD_FOUR.getValue())
        {
            return FAST_FORWARD_FOUR;
        }
        else if (ordinal == FAST_FORWARD_EIGHT.getValue())
        {
            return FAST_FORWARD_EIGHT;
        }
        else if (ordinal == FAST_FORWARD_SIXTEEN.getValue())
        {
            return FAST_FORWARD_SIXTEEN;
        }
        else if (ordinal == FAST_FORWARD_THIRTYTWO.getValue())
        {
            return FAST_FORWARD_THIRTYTWO;
        }
        else if (ordinal == FAST_FORWARD_SIXTYFOUR.getValue())
        {
            return FAST_FORWARD_SIXTYFOUR;
        }
        else if (ordinal == FAST_FORWARD_SIXTYFOUR.getValue())
        {
            return FAST_FORWARD_SIXTYFOUR;
        }
        else if (ordinal == FAST_BACKWARD_NORMAL.getValue())
        {
            return FAST_BACKWARD_NORMAL;
        }
        else if (ordinal == FAST_BACKWARD_TWO.getValue())
        {
            return FAST_BACKWARD_TWO;
        }
        else if (ordinal == FAST_BACKWARD_FOUR.getValue())
        {
            return FAST_BACKWARD_FOUR;
        }
        else if (ordinal == FAST_BACKWARD_EIGHT.getValue())
        {
            return FAST_BACKWARD_EIGHT;
        }
        else if (ordinal == FAST_BACKWARD_SIXTEEN.getValue())
        {
            return FAST_BACKWARD_SIXTEEN;
        }
        else if (ordinal == FAST_BACKWARD_THIRTYTWO.getValue())
        {
            return FAST_BACKWARD_THIRTYTWO;
        }
        else if (ordinal == FAST_BACKWARD_SIXTYFOUR.getValue())
        {
            return FAST_BACKWARD_SIXTYFOUR;
        }
        else if (ordinal == SLOW_FORWARD_TWO.getValue())
        {
            return SLOW_FORWARD_TWO;
        }
        else if (ordinal == SLOW_FORWARD_FOUR.getValue())
        {
            return SLOW_FORWARD_FOUR;
        }
        else if (ordinal == SLOW_FORWARD_EIGHT.getValue())
        {
            return SLOW_FORWARD_EIGHT;
        }
        else if (ordinal == SLOW_FORWARD_SIXTEEN.getValue())
        {
            return SLOW_FORWARD_SIXTEEN;
        }
        else if (ordinal == SLOW_FORWARD_THIRTYTWO.getValue())
        {
            return SLOW_FORWARD_THIRTYTWO;
        }
        else if (ordinal == SLOW_BACKWARD_TWO.getValue())
        {
            return SLOW_BACKWARD_TWO;
        }
        else if (ordinal == SLOW_BACKWARD_FOUR.getValue())
        {
            return SLOW_BACKWARD_FOUR;
        }
        else if(ordinal == SLOW_BACKWARD_EIGHT.getValue())
        {
            return SLOW_BACKWARD_EIGHT;
        }
        else if(ordinal == SLOW_BACKWARD_SIXTEEN.getValue())
        {
            return SLOW_BACKWARD_SIXTEEN;
        }
        else if(ordinal == SLOW_BACKWARD_THIRTYTWO.getValue())
        {
            return SLOW_BACKWARD_THIRTYTWO;
        }
        else
        {
            return INVALID_TRICK_MODE;
        }
    }
}

