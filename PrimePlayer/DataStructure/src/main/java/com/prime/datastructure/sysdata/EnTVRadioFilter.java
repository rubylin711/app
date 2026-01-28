package com.prime.datastructure.sysdata;

public enum EnTVRadioFilter {
    /**
     * All .<br>
     * */
    ALL(0),
    /**
     * TV include EnServiceType.TV,EnServiceType.MPEG_2_HD ,EnServiceType.ADVANCED_CODEC_SD,
     * EnServiceType.ADVANCED_CODEC_HD, EnServiceType.ADVANCED_CODEC_HD_FRAME_COMPATIBLE.<br>
     */
    TV(1),
    /**
     * Radio include EnServiceType.RADIO, EnServiceType.FM_RADIO,
     * EnServiceType.ADVANCED_CODEC_RADIO. <br>
     */
    RADIO(2);

    private int mIndex = 0;

    EnTVRadioFilter(int nIndex)
    {
        mIndex = nIndex;
    }

    public int getValue()
    {
        return mIndex;
    }

    public static EnTVRadioFilter valueOf(int ordinal)
    {
        if (ordinal == ALL.getValue())
        {
            return ALL;
        }
        else if (ordinal == TV.getValue())
        {
            return TV;
        }
        else
        {
            return RADIO;
        }
    }

    public String toString()
    {
        String str = " " + mIndex;
        return str;
    }
}
