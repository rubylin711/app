package com.prime.dtv.service.subtitle;

import com.prime.datastructure.utils.LogUtils;

import java.util.ArrayDeque;

public class PesQueue implements InputBufferQueue {
    private static final int DEFAULT_DISCONTINUITY_THRESHOLD_US = 500000; // 500 ms
    private static final int DEFAULT_PES_DELTA_US = 40000; // 40 ms
    private static final int MAX_QUEUE_SIZE_IN_US = 5000000;
    private static final int MAX_DELTA_REF_TIMESTAMP = 30000000; // 30s
    public static final long MICROS_PER_SECOND = 1000000L;


    class PesInputBuffer {
        long timestampUs;
        Pes pes;
    }

    // max delta for pts before discontinuity
    private long mDiscontinuityThresholdUs;
    // average expected delta of pts between two consecutive pes (in ms)
    private long mPesExpectedDeltaUs;

    // input pes
    private ArrayDeque<PesInputBuffer> mPesInputBufferQueue;

    // tell if info must be recomputed
    private boolean mAccurateInfos;
    // size is the delta between the first pes and last one
    private long mSizeUs;
    // number of discontinuity
    private int mDiscontinuityCount;
    private boolean mFirstBufferPopped;
    private boolean mInconsistentTimestampNotified;

    public PesQueue() {
        mPesInputBufferQueue = new ArrayDeque<>();
        mAccurateInfos = true;
        mSizeUs = 0;
        mDiscontinuityThresholdUs = DEFAULT_DISCONTINUITY_THRESHOLD_US;
        mPesExpectedDeltaUs = DEFAULT_PES_DELTA_US;
    }

    synchronized public void setDiscontinuityThreshold(long thresholdUs) {
        mDiscontinuityThresholdUs = thresholdUs;
    }

    @Override
    synchronized public void clear() {
        mAccurateInfos = true;
        mSizeUs = 0;
        if (!mPesInputBufferQueue.isEmpty())
            LogUtils.w("pop subtitle pes num:" + mPesInputBufferQueue.size());

        while (!mPesInputBufferQueue.isEmpty()) {
            PesInputBuffer pesInputBuffer = mPesInputBufferQueue.removeFirst();
            pesInputBuffer.pes.release();
        }
        mPesInputBufferQueue.clear();
        mFirstBufferPopped = false;
        mInconsistentTimestampNotified = false;
    }

    @Override
    synchronized public boolean isFull() {
        return (getSizeInUs() >= MAX_QUEUE_SIZE_IN_US);
    }

    @Override
    synchronized public boolean isEmpty() {
        return mPesInputBufferQueue.isEmpty();
    }

    @Override
    synchronized public long getSizeInUs() {
        ensureInfosAccuracy();
        return mSizeUs;
    }

    @Override
    synchronized public int getSize() {
        return mPesInputBufferQueue.size();
    }

    @Override
    synchronized public boolean pop(InputBuffer inputBuffer) {
        PesInputBuffer pesInputBuffer = popPesInputBuffer();
        pesInputBuffer.pes.getPayload(inputBuffer.buffer);
        pesInputBuffer.pes.release();
        inputBuffer.timestampUs = pesInputBuffer.timestampUs;

        return false;
    }


    synchronized public void pushPes(Pes pes) {
        long adjustedTimestampUs = ptsToUs(pes.getPts());
        // check discontinuity if needed
        if (mDiscontinuityCount == 0) {
            if (!mPesInputBufferQueue.isEmpty()) {
                long firstPts = mPesInputBufferQueue.peekFirst().timestampUs;
                long lastPts = mPesInputBufferQueue.peekLast().timestampUs;
                long deltaUs = deltaPts(adjustedTimestampUs, lastPts);
                if (mustCheckDiscontinuity() &&
                        (Math.abs(deltaUs) > mDiscontinuityThresholdUs)) {
                    mAccurateInfos = false;
                    clear();
                }
                mSizeUs = deltaPts(lastPts, firstPts);
            }
        }


        PesInputBuffer inputBuffer = new PesInputBuffer();
        inputBuffer.pes = pes;
        inputBuffer.timestampUs = adjustedTimestampUs;
        mPesInputBufferQueue.addLast(inputBuffer);
    }

    synchronized public long getFirstTimestampUs() {
        PesInputBuffer pesInputBuffer = mPesInputBufferQueue.peekFirst();
        if (pesInputBuffer == null)
            return -1;
        else
            return pesInputBuffer.timestampUs;
    }

    public synchronized void flushBefore(long timestampUs) {
        ensureInfosAccuracy();
        if (mDiscontinuityCount > 0) {
            clear();
        } else {
            while (!mPesInputBufferQueue.isEmpty()) {
                PesInputBuffer pesInputBuffer = mPesInputBufferQueue.peekFirst();
                if (pesInputBuffer.timestampUs >= timestampUs)
                    break;
                pesInputBuffer = popPesInputBuffer();
                pesInputBuffer.pes.release();
            }
        }
    }

    private PesInputBuffer popPesInputBuffer() {
        PesInputBuffer pesInputBuffer = mPesInputBufferQueue.removeFirst();
        if (mAccurateInfos && mDiscontinuityCount == 0) {
            if (mPesInputBufferQueue.size() > 1) {
                long firstTimestampUs = mPesInputBufferQueue.peekFirst().timestampUs;
                long lastTimestampUs = mPesInputBufferQueue.peekLast().timestampUs;
                mSizeUs = lastTimestampUs - firstTimestampUs;
            } else {
                mSizeUs = 0;
            }
        } else {
            mAccurateInfos = false;
        }
        return pesInputBuffer;
    }

    private void ensureInfosAccuracy() {
        if (mAccurateInfos)
            return;

        // reset infos
        mSizeUs = 0;
        mDiscontinuityCount = 0;

        // compute discontinuities and size
        if (!mPesInputBufferQueue.isEmpty()) {
            long firstTimestampUs = mPesInputBufferQueue.peekFirst().timestampUs;
            long lastTimestampUs = mPesInputBufferQueue.peekLast().timestampUs;
            long timestampUs = firstTimestampUs;
            if (mustCheckDiscontinuity()) {
                for (PesInputBuffer pesInputBuffer : mPesInputBufferQueue) {
                    long deltaUs = pesInputBuffer.timestampUs - timestampUs;
                    if (Math.abs(deltaUs) > mDiscontinuityThresholdUs) {
                        mDiscontinuityCount++;
                    }
                    timestampUs = pesInputBuffer.timestampUs;
                }
            }
            if (mDiscontinuityCount > 0) {
                mSizeUs = mPesInputBufferQueue.size() * mPesExpectedDeltaUs;
            } else {
                mSizeUs = lastTimestampUs - firstTimestampUs;
            }
        }

        mAccurateInfos = true;
    }

    private boolean mustCheckDiscontinuity() {
        return mDiscontinuityThresholdUs > 0;
    }

    private  long ptsToUs(long pts) {
        return (pts * MICROS_PER_SECOND) / 90000;
    }

    private  long deltaPts(long pts1, long pts2) {
        long delta1 = pts1 - pts2;
        long delta2 = pts1 + 0x1FFFFFFFFL - pts2;
        if (Math.abs(delta1) < Math.abs(delta2))
            return delta1;
        else
            return delta2;
    }
}
