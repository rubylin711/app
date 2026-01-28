package com.prime.dtv.sysdata;

import java.util.ArrayList;
import java.util.List;

public enum EnServiceType
{
    /** Reserved */
    RESERVED(0x00),
    /** digital television service */
    TV(0x01),
    /** digital radio sound service */
    RADIO(0x02),
    /** Teletext service */
    TELETEXT(0x03),
    /** NVOD reference service */
    NVOD_REFERENCE(0x04),
    /** NVOD time-shifted service */
    NVOD_TIMESHIFT(0x05),
    /** mosaic service */
    MOSAIC(0x06),
    /** FM radio service */
    FM_RADIO(0x07),
    /** DVB SRM service */
    DVB_SRM(0x08),
    /** advanced codec digital radio sound service */
    ADVANCED_CODEC_RADIO(0x0A),
    /** advanced codec mosaic service */
    ADVANCED_CODEC_MOSAIC(0x0B),
    /** data broadcast service */
    DATABROADCAST(0x0C),
    /** RCS Map */
    RSC_MAP(0x0E),
    /** RCS FLS */
    RCS_FLS(0x0F),
    /** DVB MHP service */
    DVB_MHP(0x10),
    /** MPEG-2 HD digital television service */
    MPEG_2_HD(0x11),
    /** advanced codec SD digital television service */
    ADVANCED_CODEC_SD(0x16),
    /** advanced codec SD NVOD time-shifted service */
    ADVANCED_CODEC_SD_NVOD_TIMESHIFT(0x17),
    /**  advanced codec SD NVOD reference service */
    ADVANCED_CODEC_SD_NVOD_REFERENCE(0x18),
    /**  advanced codec HD digital television service */
    ADVANCED_CODEC_HD(0x19),
    /**  advanced codec HD NVOD time-shifted service */
    ADVANCED_CODEC_HD_NVOD_TIMESHIFT(0x1A),
    /** advanced codec HD NVOD reference service */
    ADVANCED_CODEC_HD_NVOD_REFERENCE(0x1B),
    /** advanced codec frame compatible plano-stereoscopic HD digital television service */
    ADVANCED_CODEC_HD_3D(0x1C),
    /** advanced codec frame compatible plano-stereoscopic HD NVOD time-shifted service */
    ADVANCED_CODEC_HD_NVOD_TIMESHIFT_3D(0x1D),
    /** advanced codec frame compatible plano-stereoscopic HD NVOD reference service */
    ADVANCED_CODEC_HD_NVOD_REFERENCE_FRAME_COMPATIBLE(0x1E),

    USER_DEFINE_TYPE(0x98),

    RESERVED_FOR_FUTURE(0xFF);

    private int mIndex = 0;

    EnServiceType(int nIndex)
    {
        mIndex = nIndex;
    }

    public int getValue()
    {
        return mIndex;
    }

    /**
     * Get the EnServiceType of television service.<br>
     *
     * @return EnServiceType list of television service.<br>
     *
     */
    public static List<EnServiceType> getTVServiceTypes()
    {
        List<EnServiceType> mTVTypes = new ArrayList<>();
        mTVTypes.add(TV);
        mTVTypes.add(MPEG_2_HD);
        mTVTypes.add(ADVANCED_CODEC_SD);
        mTVTypes.add(ADVANCED_CODEC_HD);
        mTVTypes.add(ADVANCED_CODEC_HD_3D);
        return mTVTypes;
    }

    /**
     * Get the EnServiceType of radio service.<br>
     *
     * @return EnServiceType list of radio service.<br>
     *
     */
    public static List<EnServiceType> getRadioServiceTypes()
    {
        List<EnServiceType> mRadioTypes = new ArrayList<EnServiceType>();
        mRadioTypes.add(RADIO);
        mRadioTypes.add(FM_RADIO);
        mRadioTypes.add(ADVANCED_CODEC_RADIO);
        return mRadioTypes;
    }

    public static EnServiceType valueOf(int ordinal)
    {
        if (ordinal == RESERVED.getValue())
        {
            return RESERVED;
        }
        else if (ordinal == TV.getValue())
        {
            return TV;
        }
        else if (ordinal == RADIO.getValue())
        {
            return RADIO;
        }
        else if (ordinal == TELETEXT.getValue())
        {
            return TELETEXT;
        }
        else if (ordinal == NVOD_REFERENCE.getValue())
        {
            return NVOD_REFERENCE;
        }
        else if (ordinal == NVOD_TIMESHIFT.getValue())
        {
            return NVOD_TIMESHIFT;
        }
        else if (ordinal == MOSAIC.getValue())
        {
            return MOSAIC;
        }
        else if (ordinal == FM_RADIO.getValue())
        {
            return FM_RADIO;
        }
        else if (ordinal == DVB_SRM.getValue())
        {
            return DVB_SRM;
        }
        else if (ordinal == ADVANCED_CODEC_RADIO.getValue())
        {
            return ADVANCED_CODEC_RADIO;
        }
        else if (ordinal == ADVANCED_CODEC_MOSAIC.getValue())
        {
            return ADVANCED_CODEC_MOSAIC;
        }
        else if (ordinal == DATABROADCAST.getValue())
        {
            return DATABROADCAST;
        }
        else if (ordinal == RSC_MAP.getValue())
        {
            return RSC_MAP;
        }
        else if (ordinal == RCS_FLS.getValue())
        {
            return RCS_FLS;
        }
        else if (ordinal == DVB_MHP.getValue())
        {
            return DVB_MHP;
        }
        else if (ordinal == MPEG_2_HD.getValue())
        {
            return MPEG_2_HD;
        }
        else if (ordinal == ADVANCED_CODEC_SD.getValue())
        {
            return ADVANCED_CODEC_SD;
        }
        else if (ordinal == ADVANCED_CODEC_SD_NVOD_REFERENCE.getValue())
        {
            return ADVANCED_CODEC_SD_NVOD_REFERENCE;
        }
        else if (ordinal == ADVANCED_CODEC_SD_NVOD_TIMESHIFT.getValue())
        {
            return ADVANCED_CODEC_SD_NVOD_TIMESHIFT;
        }
        else if (ordinal == ADVANCED_CODEC_HD.getValue())
        {
            return ADVANCED_CODEC_HD;
        }
        else if (ordinal == ADVANCED_CODEC_HD_NVOD_REFERENCE.getValue())
        {
            return ADVANCED_CODEC_HD_NVOD_REFERENCE;
        }
        else if (ordinal == ADVANCED_CODEC_HD_NVOD_TIMESHIFT.getValue())
        {
            return ADVANCED_CODEC_HD_NVOD_TIMESHIFT;
        }
        else if (ordinal == ADVANCED_CODEC_HD_3D.getValue())
        {
            return ADVANCED_CODEC_HD_3D;
        }
        else if (ordinal == ADVANCED_CODEC_HD_NVOD_TIMESHIFT_3D.getValue())
        {
            return ADVANCED_CODEC_HD_NVOD_TIMESHIFT_3D;
        }
        else if (ordinal == ADVANCED_CODEC_HD_NVOD_REFERENCE_FRAME_COMPATIBLE.getValue())
        {
            return ADVANCED_CODEC_HD_NVOD_REFERENCE_FRAME_COMPATIBLE;
        }
        else if ((ordinal >= 0x12 && ordinal <= 0x15) || (ordinal >= 0x1F && ordinal <= 0x7F)
                || (ordinal == 0x09) || (ordinal == 0xFF))
        {
            return RESERVED_FOR_FUTURE;
        }
        else if (ordinal >= 0x80 && ordinal <= 0xFE)
        {
            return USER_DEFINE_TYPE;
        }
        else
        {
            throw new IndexOutOfBoundsException("EnServiceType Invalid ordinal=" + ordinal);
        }
    }
}
