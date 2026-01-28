package com.prime.dtv.service.Scan;

public class Scan_cfg {
    static final boolean ONLY_SDT_SERVICE = true;
    public static final int MAX_CHANNEL = 4000;
    public static final int ONE_TP_SCAN = 0;
    public static final int AUTO_SCAN = 1;
    public static final int NIT_SCAN = 2;
    public static final int RESERVED_FOR_FUTURE_USE = 0;
    public static final int DIGITAL_TELEVISION_SERVICE	= 0x01;
    public static final int DIGITAL_RADIO_SOUND_SERVICE = 0x02;
    public static final int TELETEXT_SERVICE = 0x03;
    public static final int NVOD_REFERENCE_SERVICE = 0x04;
    public static final int NVOD_TIME_SHIFTED_SERVICE = 0x05;
    public static final int MOSAIC_SERVICE = 0x06;
    public static final int PAL_CODED_SIGNAL = 0x07;
    public static final int SECAM_CODED_SIGNAL = 0x08;
    public static final int DD2_MAC = 0x09;
    public static final int FM_RADIO = 0x0a;
    public static final int NTSC_CODED_SIGNAL = 0x0b;
    public static final int MPEG_2_HD_DIGITAL_TELEVISION_SERVICE = 0x11;
    public static final int DATA_BROADCAST_SERVICE = 0x12;
    public static final int RESERVED_FOR_FUTURE_USE1 = 0x15;
    public static final int ADVANCED_CODEC_SD_DIGITAL_TELEVISION_SERVICE = 0x16;
    public static final int ADVANCED_CODEC_HD_DIGITAL_TELEVISION_SERVICE = 0x19;
    public static final int RESERVED_FOR_FUTURE_USE2 = 0x1c;
    public static final int HEVC = 0x1f;

    public static final int NUMBER_OF_AUDIO_IN_SIL = 20;
    //stream type
    public static final int STREAM_MPEG1_VIDEO = 0x1;
    public static final int STREAM_MPEG2_VIDEO = 0x2;
    public static final int STREAM_MPEG1_AUDIO = 0x3;
    public static final int STREAM_MPEG2_AUDIO = 0x4;
    public static final int STREAM_TELETEXT = 0x6;
    public static final int STREAM_MPEG4_VIDEO = 0x10;
    public static final int STREAM_MPEG4_H264_VIDEO = 0x1b;
    public static final int STREAM_HEVC_VIDEO = 0x24;
    public static final int STREAM_AVS_VIDEO = 0x42;
    public static final int STREAM_WM9_VIDEO = 0xEA;
    public static final int STREAM_AC3_AUDIO = 0x81;
    public static final int STREAM_DTS_AUDIO = 0x85;
    public static final int STREAM_AAC_AUDIO = 0x0f;
    public static final int STREAM_HEAAC_AUDIO = 0x11;
    public static final int STREAM_DDPLUS_AUDIO = 0x91;
    public static final int STREAM_WM9_AUDIO = 0xE6;

    //tuner type
    public static final int DVBS = 1;
    public static final int DVBC = 2;
    public static final int DVBT = 3;
    public static final int ISDBT = 5;
}
