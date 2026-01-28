package com.prime.datastructure.sysdata;

/**
 * Created by johnny_shih on 2018/4/12.
 */

public class PVREncryption
{
    public static final int PVR_ENCRYPTION_TYPE_DES = 0;
    public static final int PVR_ENCRYPTION_TYPE_3DES = 1;
    public static final int PVR_ENCRYPTION_TYPE_AES = 2;
    public static final int PVR_ENCRYPTION_TYPE_NONE = 8;

    private int mPvrEncryptionType;
    private String mPvrKey;

    public PVREncryption(int PvrEncryptionType, String PvrKey)
    {
        mPvrEncryptionType = PvrEncryptionType;
        mPvrKey = PvrKey;
    }

    public int getPvrEncryptionType()
    {
        return mPvrEncryptionType;
    }

    public String getPvrKey()
    {
        return mPvrKey;
    }
}
