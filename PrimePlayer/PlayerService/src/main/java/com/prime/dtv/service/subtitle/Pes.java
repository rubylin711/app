package com.prime.dtv.service.subtitle;

import android.util.Log;

import java.nio.ByteBuffer;

public class Pes {

    public static final int PES_MIN_HEADER_SIZE = 6;
    public static final int PES_MIN_HEADER_WITH_EXTENSION_SIZE = 9;

    public static final int PES_STREAM_ID_PROGRAM_STREAM_MAP = 0xBC;
    public static final int PES_STREAM_ID_PADING_STREAM = 0xBE;
    public static final int PES_STREAM_ID_PRIVATE_STREAM_2 = 0xBF;
    public static final int PES_STREAM_ID_ECM_STREAM = 0xF0;
    public static final int PES_STREAM_ID_EMM_STREAM = 0xF1;
    public static final int PES_STREAM_ID_DSMCC = 0xF2;
    public static final int PES_STREAM_ID_H222_1_TYPE_E = 0xF8;
    public static final int PES_STREAM_ID_PROGRAM_STREAM_DIRECTORY = 0xFF;

    private ByteBuffer mData;
    private int mStreamId;
    private int mHeaderLength;
    private int mPayloadLength;
    private boolean mHasPts;
    private long mPts;
    private boolean mHasDts;
    private long mDts;
    private boolean mRandomAccessPoint;
    private boolean mHeaderParsed;

    // building attributs
    private boolean mAccumulating;
    private int mLastContinuityCounter;

    private static final String TAG = "Pes";

    public Pes() {
    }

    public boolean hasRandomAccessPoint() {
        return mRandomAccessPoint;
    }

    public void release() {
        reset();
    }

    public byte[] getBuffer() {
        return mData.array();
    }

    public ByteBuffer getData() {
        return mData;
    }

    public void setByteBuffer(ByteBuffer buffer) {
        if (!buffer.hasArray())
            throw new IllegalArgumentException("backing array is not accessible");
        mData = buffer;
    }

    public void reset() {
        mLastContinuityCounter = -1;
        mAccumulating = false;
        mData.clear();

        mStreamId = 0;
        mHeaderLength = 0;
        mPayloadLength = 0;
        mHasPts = false;
        mPts = 0;
        mHasDts = false;
        mDts = 0;
        mHeaderParsed = false;

        mRandomAccessPoint = false;
    }

    public boolean isEmpty() {
        return mData.position() == 0;
    }

    public void finish() {
        mData.flip();
    }

    public boolean isComplete() {
        int position = mData.position();
        if (position < PES_MIN_HEADER_SIZE)
            return false;
        int pesLength = ((mData.array()[4] << 8) & 0xff00) | (mData.array()[5] & 0xff);
        if (pesLength == 0)
            return false;
        // 6 first bytes are for : start_code_prefix, stream_id, PES_packet_length
        // doc iso13818-1, table 2-36
        return (position >= pesLength + 6);
    }

    public void pushExtraData(byte[] data, int offset, int length) {
        int oldPosition = mData.position();
        mData.position(mData.limit());
        int newLimit = mData.limit() + length;
        ensureCapacity(newLimit);
        mData.limit(newLimit);
        mData.put(data, offset, length);
        mData.position(oldPosition);
    }

    public void parseHeader() {
        if (mData.limit() < PES_MIN_HEADER_SIZE)
            return;

        byte[] bytes = mData.array();
        int pesHeaderDataLength = bytes[8];
        if (mData.limit() < PES_MIN_HEADER_SIZE + pesHeaderDataLength)
            return;

        BufferParser parser = new BufferParser();
        parser.setBytes(bytes, 0);
        int startCodePrefix = parser.readInt(24, "Packet start code prefix");
        if (startCodePrefix != 1) {
            Log.w(TAG, String.format("Pes : bad start code prefix %#x", startCodePrefix));
            return;
        }
        mStreamId = parser.readInt(8, "Stream id");
        mPayloadLength = parser.readInt(16, "PES packet length");

        switch (mStreamId) {
            case PES_STREAM_ID_PROGRAM_STREAM_MAP:
            case PES_STREAM_ID_PRIVATE_STREAM_2:
            case PES_STREAM_ID_ECM_STREAM:
            case PES_STREAM_ID_EMM_STREAM:
            case PES_STREAM_ID_PROGRAM_STREAM_DIRECTORY:
            case PES_STREAM_ID_DSMCC:
            case PES_STREAM_ID_H222_1_TYPE_E:
            case PES_STREAM_ID_PADING_STREAM:
                mHeaderLength = parser.getPosInBytes();
                mHeaderParsed = true;
                break;
            default:
                break;
        }

        if (mHeaderParsed)
            return;

        if (mData.limit() < PES_MIN_HEADER_WITH_EXTENSION_SIZE + pesHeaderDataLength)
            return;

        parser.skip(2, "10");
        parser.skip(2, "PES scrambling control");
        parser.skip(1, "PES priority");
        parser.skip(1, "Data alignment indicator");
        parser.skip(1, "Copyright");
        parser.skip(1, "Original or copy");
        int ptsDtsFlag = parser.readInt(2, "PTS DTS flag");
        parser.skip(1, "ESCR flag");
        parser.skip(1, "ES rate flag");
        parser.skip(1, "DSM trick mode flag");
        parser.skip(1,
                "Additional copy info flag");
        boolean crcFlag = parser.readBool("CRC flag");
        parser.skip(1, "Extension flag");
        int headerDataLength = parser.readInt(8, "Header data length");
        mHeaderLength = parser.getPosInBytes() + headerDataLength;

        if ((ptsDtsFlag & 0x02) == 2) {
            mHasPts = true;
            parser.skip(4, "001x");
            long PTS30_32 = parser.readInt(3, "PTS[32..30]");
            parser.skip(1, "marker_bit");
            long PTS29_15 = parser.readInt(15, "PTS[29..15]");
            parser.skip(1, "marker_bit");
            long PTS14_0 = parser.readInt(15, "PTS[14..0]");
            parser.skip(1, "marker_bit");

            mPts = (PTS30_32 << 30) | (PTS29_15) << 15 | PTS14_0;
        }
        if ((ptsDtsFlag & 0x01) == 0x01) {
            mHasDts = true;
            parser.skip(4, "0001");
            int DTS30_32 = parser.readInt(3, "DTS[32..30]");
            parser.skip(1, "marker_bit");
            int DTS29_15 = parser.readInt(15, "DTS[29..15]");
            parser.skip(1, "marker_bit");
            int DTS14_0 = parser.readInt(15, "DTS[14..0]");
            parser.skip(1, "marker_bit");

            mDts = ((long) DTS30_32 << 30) | (long) (DTS29_15) << 15 | DTS14_0;
        }
        mHeaderParsed = true;
    }

    public boolean isHeaderParsed() {
        return mHeaderParsed;
    }

    public int getHeaderLength() {
        return mHeaderLength;
    }

    public int getLength() {
        if (mHeaderParsed)
            return mData.limit();
        else
            return 0;
    }

    public boolean hasPts() {
        return mHasPts;
    }

    public long getPts() {
        return mPts;
    }

    public boolean hasDts() {
        return mHasDts;
    }

    public long getDts() {
        return mDts;
    }

    public int getStreamId() {
        return mStreamId;
    }

    public void getPayload(ByteBuffer sample) {
        getPayload(sample, 0, getLength() - getHeaderLength());
    }

    public void getPayload(ByteBuffer sample, int offset, int length) {
        sample.clear();

        if (mHeaderParsed) {
            length = Math.min(length, mData.limit() - mHeaderLength - offset);
            sample.put(mData.array(), mHeaderLength + offset, length);
        }

        sample.flip();
    }

    private boolean isNextContinuityCounter(int current, int next) {
        return ((current + 1) % 16) == next;
    }

    private void ensureCapacity(int capacity) {
        if (capacity <= mData.capacity())
            return;

        ByteBuffer buffer = ByteBuffer.allocate(mData.capacity() + 1024 * 32);
        mData.flip();
        buffer.put(mData);
        mData = buffer;
        Log.d(TAG, String.format("Resize pes buffer (new size:%d)", mData.capacity()));
    }
}
