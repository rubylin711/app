package com.dolphin.dtv;


public enum DiSEqCLimitType
{
    SAT_MOTOR_LIMIT_OFF(0),          /**<Disable Limits*/
    SAT_MOTOR_LIMIT_EAST(1),         /**<Set East Limit*/
    SAT_MOTOR_LIMIT_WEST(2),         /**<Set West Limit*/
    SAT_MOTOR_LIMIT_BUTT(3);         /**<Invalid value*/

    private int mIndex = 0;

    DiSEqCLimitType(int nIndex)
    {
        mIndex = nIndex;
    }

    public int getValue()
    {
        return mIndex;
    }
}
