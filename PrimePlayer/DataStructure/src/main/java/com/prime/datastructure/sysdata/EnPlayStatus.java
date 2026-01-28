package com.prime.datastructure.sysdata;
public enum EnPlayStatus
{
    STOP(0),
    LIVEPLAY(1),
    TIMESHIFTPLAY(2),
    PAUSE(3),
    IDLE(4),
    RELEASEPLAYRESOURCE(5),
    PIPPLAY(6),
    EWSPLAY(7),
    INVALID(8);

    private int mIndex = 0;

    EnPlayStatus(int nIndex)
    {
        mIndex = nIndex;
    }

    public int getValue()
    {
        return mIndex;
    }

    public static EnPlayStatus valueOf(int ordinal)
    {
        if (ordinal == STOP.getValue())
        {
            return STOP;
        }
        else if (ordinal == LIVEPLAY.getValue())
        {
            return LIVEPLAY;
        }
        else if (ordinal == TIMESHIFTPLAY.getValue())
        {
            return TIMESHIFTPLAY;
        }
        else if (ordinal == PAUSE.getValue())
        {
            return PAUSE;
        }
        else if (ordinal == IDLE.getValue())
        {
            return IDLE;
        }
        else if (ordinal == RELEASEPLAYRESOURCE.getValue())
        {
            return RELEASEPLAYRESOURCE;
        }
        else if (ordinal == PIPPLAY.getValue())
        {
            return PIPPLAY;
        }
        else if (ordinal == EWSPLAY.getValue())
        {
            return EWSPLAY;
        }
        else
        {
            return INVALID;
        }
    }
}
