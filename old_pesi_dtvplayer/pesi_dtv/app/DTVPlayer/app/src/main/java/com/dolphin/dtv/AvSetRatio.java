package com.dolphin.dtv;

/**
 * Created by scoty on 2018/5/23.
 */

public enum AvSetRatio {

    ASPECT_RATIO_AUTO(0),     /**< auto*/
    ASPECT_RATIO_16TO9(1),        /**< 16:9*/
    ASPECT_RATIO_4TO3(2),         /**< 4:3*/
    ASPECT_RATIO_UNKNOWN(3),
    NB_OF_ASPECT_RATIO(4);

    private int mIndex = 0;

    AvSetRatio(int nIndex)
    {
        mIndex = nIndex;
    }

    public int getValue()
    {
        return mIndex;
    }

    public static AvSetRatio valueOf(int ordinal) {
        if (ordinal == ASPECT_RATIO_AUTO.getValue()) {
            return ASPECT_RATIO_AUTO;
        } else if (ordinal == ASPECT_RATIO_16TO9.getValue()) {
            return ASPECT_RATIO_16TO9;
        } else if (ordinal == ASPECT_RATIO_4TO3.getValue()) {
            return ASPECT_RATIO_4TO3;
        } else if (ordinal == ASPECT_RATIO_UNKNOWN.getValue()) {
            return ASPECT_RATIO_UNKNOWN;
        } else
        {
            return NB_OF_ASPECT_RATIO;
        }
    }

}
