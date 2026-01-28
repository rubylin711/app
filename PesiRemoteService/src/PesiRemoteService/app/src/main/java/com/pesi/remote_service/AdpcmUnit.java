package com.pesi.remote_service;

public class AdpcmUnit {
    private int mPrevStepSizeIndex ;
    private short mPred_value ;
    public void setDecoderSyncData( int stepIndex, short predValue )
    {
        mPrevStepSizeIndex = stepIndex ;
        mPred_value = predValue ;
    }

    public int getPrevStepSizeIndex()
    {
        return mPrevStepSizeIndex ;
    }

    public short getPred_value()
    {
        return mPred_value ;
    }
    byte ima_index_table[] = {
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 2, 4, 6, 8,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 2, 4, 6, 8,
    };
    static short stepsizeTable[] = {
            7, 8, 9, 10, 11, 12, 13, 14, 16, 17,
            19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
            50, 55, 60, 66, 73, 80, 88, 97, 107, 118,
            130, 143, 157, 173, 190, 209, 230, 253, 279, 307,
            337, 371, 408, 449, 494, 544, 598, 658, 724, 796,
            876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
            2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358,
            5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
            15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
    };
    short ADPCM_Decode(byte nibble)
    {
        short step=0;
        int diffq=0;
        step = stepsizeTable[mPrevStepSizeIndex];
        /* 2. inverse code into diff */
        diffq = (step>> 3);
        if ((nibble&4)!=0)
            diffq += step;

        if ((nibble&2)!=0)
            diffq += (step>>1);

        if ((nibble&1)!=0)
            diffq += (step>>2);

        /* 3. add diff to predicted sample*/
        if ((nibble&8)!=0)
            mPred_value -= diffq;
        else
            mPred_value += diffq;

        /* check for overflow*/
        if (mPred_value > 32767)
            mPred_value = 32767;
        else if (mPred_value < -32768)
            mPred_value = -32768;

        /* 4. find new quantizer step size */
        mPrevStepSizeIndex += ima_index_table[nibble];
        /* check for overflow*/
        if (mPrevStepSizeIndex < 0)
            mPrevStepSizeIndex = 0;
        if (mPrevStepSizeIndex > 88)
            mPrevStepSizeIndex = 88;

        return mPred_value;
    }
}
