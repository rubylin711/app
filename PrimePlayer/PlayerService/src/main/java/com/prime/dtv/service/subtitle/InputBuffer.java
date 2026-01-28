package com.prime.dtv.service.subtitle;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public class InputBuffer {

    public static final int FLAG_NONE = 0;
    public static final int FLAG_SCRAMBLED_EVEN = 1;
    public static final int FLAG_SCRAMBLED_ODD = 2;

    public static final int ERROR_NONE = 0;
    public static final int ERROR_SCRAMBLING = 1;

    // mandatory: data and timestamp
    public ByteBuffer buffer;
    public long timestampUs;

    // for scrambled content
    public MediaCodec.CryptoInfo cryptoInfo;

    // optional: position in bytes in the source
    public long position;

    // flag provides hints about the content of the buffer
    public int flag;
    // error that might have occurred while building the buffer
    public int error;

    // additional details for video
    public float pixelAspectRatio;
    public int width;
    public int height;
}
