package com.prime.dtv.service.subtitle;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import com.prime.dtv.utils.LogUtils;

import java.nio.ByteBuffer;

/**
 * Base class for DvbSubtitleCodec and TeletextSubtitleCode and IsdbccSubtitleCodec
 * Manage
 * - input and output buffers
 * - callback and buffers notification
 */
public abstract class SubtitleCodec {

    public static abstract class Callback {
        public abstract void onError(SubtitleCodec codec, SubtitleException exception);

        public abstract void onInputBufferAvailable(SubtitleCodec codec, int index);

        public abstract void onOutputBufferAvailable(SubtitleCodec codec, Bitmap bitmap, BufferInfo info);
    }

    public static class SubtitleException extends IllegalStateException {
        SubtitleException(String message) {
            super(message);
        }
    }

    public static class BufferInfo {
        public long presentationTimeUs;
        public int timeoutSec;
    }

    protected class InputBuffer {
        InputBuffer(int index) {
            this.index = index;
            // max pes size
            this.buffer = ByteBuffer.allocate(1024 * 64);
        }

        public void reset() {
            buffer.clear();
            inUse = false;
            presentationTimeUs = 0;
            offset = 0;
            size = 0;
            flags = 0;
        }

        public ByteBuffer buffer;
        public boolean inUse;

        public int index;
        public int offset;
        public int size;
        public int flags;

        public long presentationTimeUs;
    }

    private static final String TAG = "SubtitleCodec";

    private static final int MESSAGE_PARSE_BUFFER = 1;

    private static final int NB_INPUT_BUFFERS = 4;

    private InputBuffer mInputBuffers[];

    private HandlerThread mParserThread;
    private Handler mParserHandler;
    private Callback mCallback;
    private Handler mCallbackHandler;
    private final String mTokenSubtitle;

    private String mName;

    public SubtitleCodec(String name) {
        mName = name;
        mInputBuffers = new InputBuffer[NB_INPUT_BUFFERS];
        for (int i = 0; i < NB_INPUT_BUFFERS; ++i)
            mInputBuffers[i] = new InputBuffer(i);
        for (InputBuffer buffer : mInputBuffers)
            buffer.reset();
        mTokenSubtitle = "TOKEN_SUBTITLE";
    }

    protected void reset() {
        for (InputBuffer buffer : mInputBuffers)
            buffer.reset();
    }

    public void start() {
        reset();
        if (mParserThread != null) {
            Log.w(TAG, String.format("%s: codec already started", mName));
            return;
        }
        mParserThread = new HandlerThread(mName,Process.THREAD_PRIORITY_AUDIO);
        mParserThread.start();
        mParserHandler = new Handler(mParserThread.getLooper(), msg -> {
            if (msg.what == MESSAGE_PARSE_BUFFER) {
                parse((InputBuffer) msg.obj);
            }
            return false;
        });
        for (InputBuffer inputBuffer : mInputBuffers) notifyInputBuffer(inputBuffer);
    }

    public void stop() {
        if (mParserThread != null) {
            mParserThread.quit();
            try {
                mParserThread.join();
            } catch (InterruptedException e) {
                Log.w(TAG, "interrupted while joining parser thread");
            }
            mParserHandler.removeMessages(MESSAGE_PARSE_BUFFER);

        }
        mParserThread = null;
        if (mCallbackHandler != null)
            mCallbackHandler.removeCallbacksAndMessages(mTokenSubtitle);
        reset();
    }

    public void setCallback(Callback callback, Handler handler) {
        mCallback = callback;
        mCallbackHandler = handler;
    }

    public ByteBuffer getInputBuffer(int index) {
        InputBuffer inputBuffer = mInputBuffers[index];
        if (inputBuffer.inUse)
            throw new IllegalStateException(String.format("buffer %d is already in use", index));
        inputBuffer.buffer.clear();
        return inputBuffer.buffer;
    }

    public void queueInputBuffer(int index,
                                 int offset,
                                 int size,
                                 long presentationTimeUs,
                                 int flags) {
        InputBuffer inputBuffer = mInputBuffers[index];
        if (inputBuffer.inUse)
            throw new IllegalStateException(String.format("buffer %d is already in use", index));
        if (size == 0) {
            notifyInputBuffer(inputBuffer);
            return;
        }
        inputBuffer.inUse = true;
        inputBuffer.offset = offset;
        inputBuffer.size = size;
        inputBuffer.presentationTimeUs = presentationTimeUs;
        inputBuffer.flags = flags;
        LogUtils.d("sendMessage => MESSAGE_PARSE_BUFFER");
        mParserHandler.sendMessage(mParserHandler.obtainMessage(MESSAGE_PARSE_BUFFER, inputBuffer));
    }

    public abstract void parse(InputBuffer inputBuffer);

    protected void notifyError(final String message) {
        mCallbackHandler.postAtTime(new Runnable() {
            @Override
            public void run() {
                mCallback.onError(SubtitleCodec.this, new SubtitleException(message));
            }
        }, mTokenSubtitle, SystemClock.uptimeMillis());
    }

    protected void notifyOutputBuffer(final Bitmap bitmap, final long timestampUs, final int timeoutSec) {
        final BufferInfo info = new BufferInfo();
        info.presentationTimeUs = timestampUs;
        info.timeoutSec = timeoutSec;
        mCallbackHandler.postAtTime(new Runnable() {
            @Override
            public void run() {
                mCallback.onOutputBufferAvailable(SubtitleCodec.this, bitmap, info);
            }
        }, mTokenSubtitle, SystemClock.uptimeMillis());
    }

    protected void notifyInputBuffer(final InputBuffer inputBuffer) {
        inputBuffer.inUse = false;
        //LogUtils.d("notifyInputBuffer IN");
        mCallbackHandler.postAtTime(new Runnable() {
            @Override
            public void run() {
                mCallback.onInputBufferAvailable(SubtitleCodec.this, inputBuffer.index);
            }
        }, mTokenSubtitle, SystemClock.uptimeMillis());
    }
}
