package com.prime.datastructure.sysdata;

/**
 * Created by gary_hsu on 2018/3/29.
 */

public enum EnNetworkType
{
    NONE(0), CABLE(2), SATELLITE(1),TERRESTRIAL(3), ISDB_TER(5), ATSC_T(6), DTMB(4), J83B(8), RF(9), ATSC_CAB(7), ISDB_CAB(1024);

    private int mIndex = 0;

    EnNetworkType(int nIndex)
    {
        mIndex = nIndex;
    }

    public int getValue()
    {
        return mIndex;
    }

    public static EnNetworkType valueOf(int value)
    {
        if (value == CABLE.getValue())
        {
            return CABLE;
        }
        else if (value == SATELLITE.getValue())
        {
            return SATELLITE;
        }
        else if (value == TERRESTRIAL.getValue())
        {
            return TERRESTRIAL;
        }
        else if (value == ISDB_TER.getValue())
        {
            return ISDB_TER;
        }
        else if (value == ATSC_T.getValue())
        {
            return ATSC_T;
        }
        else if (value == DTMB.getValue())
        {
            return DTMB;
        }
        else if (value == J83B.getValue())
        {
            return J83B;
        }
        else if (value == ATSC_CAB.getValue())
        {
            return ATSC_CAB;
        }
        else if (value == ISDB_CAB.getValue())
        {
            return ISDB_CAB;
        }
        else if (value == RF.getValue())
        {
            return RF;
        }
        else if (value == NONE.getValue())
        {
            return NONE;
        }
        else
        {
            throw new IndexOutOfBoundsException("EnNetworkType Invalid value=" + value);
        }
    }
}
