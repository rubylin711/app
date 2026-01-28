package com.prime.launcher.teletextservice.decoder;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * 基底 TeletextCodec 類別，用於管理 input buffer 的生命周期與解析工作。
 */
public abstract class TeletextCodec {

    public static abstract class Callback {
        public abstract void onError(TeletextCodec codec, TeletextException exception);
        public abstract void onInputBufferAvailable(TeletextCodec codec, int index);
    }

    public static class TeletextException extends IllegalStateException {
        public TeletextException(String message) {
            super(message);
        }
    }

    protected class InputBuffer {
        public InputBuffer(int index) {
            this.index = index;
            this.buffer = ByteBuffer.allocate(64 * 1024); // PES 最大 buffer
        }

        public void reset() {
            buffer.clear();
            inUse = false;
        }

        public final int index;
        public ByteBuffer buffer;
        public boolean inUse = false;
        public int offset = 0;
        public int size = 0;
        public int flags = 0;
    }

    private static final int NB_INPUT_BUFFERS = 4;
    private static final int MSG_PARSE = 1;
    private static final String TOKEN = "TELETEXT_CODEC";

    private final InputBuffer[] mInputBuffers = new InputBuffer[NB_INPUT_BUFFERS];
    private Callback mCallback;
    private Handler mCallbackHandler;
    private HandlerThread mParserThread;
    private Handler mParserHandler;
    private final String mName;

    public TeletextCodec(String name) {
        mName = name;
        for (int i = 0; i < NB_INPUT_BUFFERS; ++i) {
            mInputBuffers[i] = new InputBuffer(i);
        }
    }

    public void start() {
        if (mParserThread != null) return;
        for (InputBuffer buffer : mInputBuffers) {
            buffer.reset();
        }
        mParserThread = new HandlerThread(mName);
        mParserThread.start();
        mParserHandler = new Handler(mParserThread.getLooper(), msg -> {
            if (msg.what == MSG_PARSE) {
                parse((InputBuffer) msg.obj);
            }
            return true;
        });
        for (InputBuffer buffer : mInputBuffers) {
            notifyInputBuffer(buffer);
        }
    }

    public void stop() {
        if (mParserThread != null) {
            mParserThread.quitSafely();
            try {
                mParserThread.join(); // 加這行
            } catch (InterruptedException ignored) {}
            mParserThread = null;
            mParserHandler = null;
        }
        if (mCallbackHandler != null) {
            mCallbackHandler.removeCallbacksAndMessages(TOKEN);
        }
    }

    public void setCallback(Callback callback, Handler handler) {
        mCallback = callback;
        mCallbackHandler = handler;
    }

    public ByteBuffer getInputBuffer(int index) {
        InputBuffer buffer = mInputBuffers[index];
        if (buffer.inUse) {
            throw new IllegalStateException("Input buffer already in use: " + index);
        }
        buffer.reset();
        return buffer.buffer;
    }

    public void queueInputBuffer(int index, int offset, int size, int flags) {
        InputBuffer buffer = mInputBuffers[index];
        if (buffer.inUse) {
            throw new IllegalStateException("Input buffer already in use: " + index);
        }
        if (size == 0) {
            notifyInputBuffer(buffer);
            return;
        }
        buffer.inUse = true;
        buffer.offset = offset;
        buffer.size = size;
        buffer.flags = flags;

        if (mParserHandler != null) {
            mParserHandler.sendMessage(mParserHandler.obtainMessage(MSG_PARSE, buffer));
        }
    }

    protected abstract void parse(InputBuffer inputBuffer);

    protected void notifyError(String message) {
        if (mCallbackHandler != null) {
            mCallbackHandler.postAtTime(() -> {
                if (mCallback != null) {
                    mCallback.onError(this, new TeletextException(message));
                }
            }, TOKEN, SystemClock.uptimeMillis());
        }
    }


    protected void notifyInputBuffer(InputBuffer inputBuffer) {
        inputBuffer.inUse = false;
    
        final Callback callbackSnapshot = mCallback;
        final Handler handlerSnapshot = mCallbackHandler;
    
        if (handlerSnapshot != null && callbackSnapshot != null) {
            handlerSnapshot.postAtTime(() -> {
                try {
                    callbackSnapshot.onInputBufferAvailable(this, inputBuffer.index);
                } catch (Exception e) {
                    Log.e(TOKEN, "⚠ Exception during callback.onInputBufferAvailable", e);
                }
            }, TOKEN, SystemClock.uptimeMillis());
        } else {
            Log.w(TOKEN, "⚠ Skipped notifyInputBuffer: callback or handler is null");
        }
    }
    
}
