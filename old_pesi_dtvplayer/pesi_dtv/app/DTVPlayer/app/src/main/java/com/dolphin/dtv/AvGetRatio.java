package com.dolphin.dtv;

/**
 * Created by scoty on 2018/5/23.
 */

public enum AvGetRatio
{
    FORBIDDEN(0),
    ASP_4TO3(1),       //    4 : 3
    ASP_16TO9(2),      //   16 : 9
    MVD_ASP_MAXNUM(3);

    private int mIndex = 0;

    AvGetRatio(int nIndex)
    {
        mIndex = nIndex;
    }

    public int getValue()
    {
        return mIndex;
    }

    public static AvGetRatio valueOf(int ordinal)
    {
        if (ordinal == FORBIDDEN.getValue()) {
            return FORBIDDEN;
        } else if (ordinal == ASP_4TO3.getValue()) {
            return ASP_4TO3;
        } else if (ordinal == ASP_16TO9.getValue()) {
            return ASP_16TO9;
        }else{
            return MVD_ASP_MAXNUM;
        }
    }
}

