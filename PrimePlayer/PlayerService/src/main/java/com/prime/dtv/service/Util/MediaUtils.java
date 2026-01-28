package com.prime.dtv.service.Util;

import android.media.MediaFormat;

import com.prime.dtv.service.Table.StreamType;

public class MediaUtils {
    private static final String TAG = "Prime_MediaUtils";
    //}

    public static final int RTK_CODEC_VIDEO_MPEG1 = 100;  //= 100
    public static final int RTK_CODEC_VIDEO_MPEG2 = 101;
    public static final int RTK_CODEC_VIDEO_MPEG4_PART2 = 102;
    public static final int RTK_CODEC_VIDEO_H264 = 103;
    public static final int RTK_CODEC_VIDEO_H265 = 104;
    public static final int RTK_CODEC_VIDEO_H263 = 105;
    public static final int RTK_CODEC_VIDEO_VC1 = 106;
    public static final int RTK_CODEC_VIDEO_VC1_SM = 107;
    public static final int RTK_CODEC_VIDEO_VP6 = 108;
    public static final int RTK_CODEC_VIDEO_H266 = 110;

    public static final String OMX_AUDIO_DEC = "OMX.realtek.audio.dec";
    public static final String OMX_REALTEK_VIDEO_DEC_AV1 = "c2.realtek.video.av1.decoder";
    public static final String OMX_REALTEK_VIDEO_DEC_AVC = "c2.realtek.video.avc.decoder";
    public static final String OMX_REALTEK_VIDEO_DEC_3GPP = "c2.realtek.video.3gpp.decoder";
    public static final String OMX_REALTEK_VIDEO_DEC_HEVC = "c2.realtek.video.hevc.main10.decoder";
    public static final String OMX_REALTEK_VIDEO_DEC_VVC = "c2.realtek.video.vvc.decoder";
    public static final String OMX_REALTEK_VIDEO_DEC_MPEG2 = "c2.realtek.video.mpeg2.decoder";
    // public static final String OMX_REALTEK_VIDEO_DEC_MPEG2 = "c2.realtek.video.dec.mpeg2.secure";

    public static final String OMX_REALTEK_VIDEO_DEC_MPEG4 = "c2.realtek.video.mpeg4.decoder";
    public static final String OMX_REALTEK_VIDEO_DEC_VP8 = "c2.realtek.video.vp8.decoder";
    public static final String OMX_REALTEK_VIDEO_DEC_VP9 = "c2.realtek.video.vp9.decoder";

   // public static final String OMX_AUDIO_DEC_SECURE = "c2.realtek.audio.dec.secure";
    public static final String OMX_REALTEK_VIDEO_DEC_AV1_SECURE = "c2.realtek.video.av1.decoder.secure";
    public static final String OMX_REALTEK_VIDEO_DEC_AVC_SECURE = "c2.realtek.video.avc.decoder.secure";
    public static final String OMX_REALTEK_VIDEO_DEC_3GPP_SECURE = "c2.realtek.video.3gpp.decoder.secure";
    public static final String OMX_REALTEK_VIDEO_DEC_HEVC_SECURE = "c2.realtek.video.hevc.main10.decoder.secure";
    public static final String OMX_REALTEK_VIDEO_DEC_MPEG2_SECURE = "c2.realtek.video.mpeg2.decoder.secure";
    // public static final String OMX_REALTEK_VIDEO_DEC_MPEG2 = "OMX.realtek.video.dec.mpeg2.decoder.secure";

    public static final String OMX_REALTEK_VIDEO_DEC_MPEG4_SECURE = "c2.realtek.video.mpeg4.decoder.secure";
    public static final String OMX_REALTEK_VIDEO_DEC_VP8_SECURE = "c2.realtek.video.vp8.decoder.secure";
    public static final String OMX_REALTEK_VIDEO_DEC_VP9_SECURE = "c2.realtek.video.vp9.decoder.secure";
    public static final String OMX_REALTEK_VIDEO_DEC_VVC_SECURE = "c2.realtek.video.vvc.decoder.secure";

    ///	audio.
    public static final int RTK_CODEC_AUDIO_MPEG1 = 1;
    public static final int RTK_CODEC_AUDIO_MPEG2 = 2;
    public static final int RTK_CODEC_AUDIO_AAC = 3;
    public static final int RTK_CODEC_AUDIO_AACPLUS = 4;
    public static final int RTK_CODEC_AUDIO_AC3 = 5;
    public static final int RTK_CODEC_AUDIO_AC3_TRUE_HD = 6;
    public static final int RTK_CODEC_AUDIO_AC3_PLUS = 7;
    public static final int RTK_CODEC_AUDIO_EAC3 = 8;

    //N/T
    public static final int RTK_CODEC_AUDIO_DTS = 9;
    public static final int RTK_CODEC_AUDIO_DTS_HD = 10;
    public static final int RTK_CODEC_AUDIO_DTS_HD_MA = 11;
    public static final int RTK_CODEC_AUDIO_LPCM = 12;
    public static final int RTK_CODEC_AUDIO_MP3 = 13;
    public static final int RTK_CODEC_AUDIO_MP4 = 14;  // LATM AAC
    public static final int RTK_CODEC_AUDIO_AC4 = 15;
    public static final int RTK_CODEC_AUDIO_HE_AAC_V1 = 16;
    public static final int RTK_CODEC_AUDIO_HE_AAC_V2 = 17;

    public static String getVideoCodeName(int codec) {
        switch (codec) {
            case StreamType.STREAM_MPEG1_VIDEO: // 0x01
                return OMX_REALTEK_VIDEO_DEC_AV1;
            case StreamType.STREAM_MPEG2_VIDEO: //0x02
                return OMX_REALTEK_VIDEO_DEC_MPEG2;
            case StreamType.STREAM_MPEG4_VIDEO: // 0x10
                return OMX_REALTEK_VIDEO_DEC_MPEG4;
            case StreamType.STREAM_MPEG4_H264_VIDEO: // 0x1b ok
                return OMX_REALTEK_VIDEO_DEC_AVC;
            case StreamType.STREAM_HEVC_VIDEO:
                return OMX_REALTEK_VIDEO_DEC_HEVC;
//            case RTK_CODEC_VIDEO_H266:
//                return OMX_REALTEK_VIDEO_DEC_VVC;
        }
        return OMX_REALTEK_VIDEO_DEC_AV1;
    }

    public static String getVideoCodeNameSecure(int codec) {
        switch (codec) {
            case StreamType.STREAM_MPEG1_VIDEO: // 0x01
                return OMX_REALTEK_VIDEO_DEC_AV1_SECURE;
            case StreamType.STREAM_MPEG2_VIDEO: //0x02
                return OMX_REALTEK_VIDEO_DEC_MPEG2_SECURE;
            case StreamType.STREAM_MPEG4_VIDEO: // 0x10
                return OMX_REALTEK_VIDEO_DEC_MPEG4_SECURE;
            case StreamType.STREAM_MPEG4_H264_VIDEO: // 0x1b ok
                return OMX_REALTEK_VIDEO_DEC_AVC_SECURE;
            case StreamType.STREAM_HEVC_VIDEO:
                return OMX_REALTEK_VIDEO_DEC_HEVC_SECURE;
//            case RTK_CODEC_VIDEO_H266:
//                return OMX_REALTEK_VIDEO_DEC_VVC_SECURE;
        }
        return OMX_REALTEK_VIDEO_DEC_AV1;
    }

    public static String getVideoMediaFormatStr(int videoCodec) {
        switch (videoCodec) {
            case StreamType.STREAM_MPEG1_VIDEO:
                return MediaFormat.MIMETYPE_VIDEO_AV1;
            case StreamType.STREAM_MPEG2_VIDEO:
                return MediaFormat.MIMETYPE_VIDEO_MPEG2;
            case StreamType.STREAM_MPEG4_VIDEO:
                return MediaFormat.MIMETYPE_VIDEO_MPEG4;
            case StreamType.STREAM_MPEG4_H264_VIDEO:
                return MediaFormat.MIMETYPE_VIDEO_AVC;
            case StreamType.STREAM_HEVC_VIDEO:
                return MediaFormat.MIMETYPE_VIDEO_HEVC;
//            case RTK_CODEC_VIDEO_H266:
//                return "video/vvc";
        }
        return MediaFormat.MIMETYPE_VIDEO_AVC;

    }
}
