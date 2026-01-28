package com.prime.launcher.teletextservice.decoder;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;


public class TeletextDecoder {
    private static final String TAG = "TeletextDecoder";

    private final TeletextPageCollector mCollector;
    private final Handler mHandler;
    private final TeletextCodec mCodec;

    // ? 改用 Queue 儲存多筆 PES buffer
    private final Queue<byte[]> mBufferQueue = new LinkedList<>();

    public TeletextDecoder(Context context, Handler handler) {
        mHandler = handler;
        mCollector = new TeletextPageCollector();
        mCollector.setContext(context); 
        mCodec = mCollector;
    }

    public void start() {
        mCollector.setCallback(new TeletextCodec.Callback() {
            @Override
            public void onError(TeletextCodec codec, TeletextCodec.TeletextException exception) {
                Log.e(TAG, "Decoder error: " + exception.getMessage());
            }
    
            @Override
            public void onInputBufferAvailable(TeletextCodec codec, int index) {
                //Log.d(TAG, "?? InputBuffer[" + index + "] is available.");
                
                byte[] data;
                synchronized (mBufferQueue) {
                    data = mBufferQueue.poll();
                }

                if (data != null) {
                    //Log.d(TAG, "?? Dequeued PES buffer of length: " + data.length);
                    ByteBuffer inputBuffer = codec.getInputBuffer(index);
                    inputBuffer.put(data);
                    codec.queueInputBuffer(index, 0, data.length, 0);
                } else {
                    //Log.w(TAG, "? No PES data available to feed. Returning buffer.");
                    // Return empty buffer to keep codec loop alive
                    codec.queueInputBuffer(index, 0, 0, 0);
                }
            }
        }, mHandler);
        mCollector.start();
    }

    public void stop() {
        mCodec.setCallback(null, null);
        mCodec.stop();
        synchronized (mBufferQueue) {
            mBufferQueue.clear(); // 清空 buffer
        }
    }

   
    public void feedTeletextPes(byte[] pesData) {
        if (pesData == null || pesData.length < 10) {
            Log.w(TAG, "⚠ Ignored invalid PES buffer");
            return;
        }
    
        // 根據 PES 規範第 9 個 byte 是 PES header length
        int pesHeaderLength = pesData[8] & 0xFF;
        int headerOffset = 9 + pesHeaderLength;
    
        if (pesData.length > headerOffset) {
            byte[] payload = Arrays.copyOfRange(pesData, headerOffset, pesData.length);
            synchronized (mBufferQueue) {
                mBufferQueue.offer(payload);
            }
            //Log.d(TAG, "📥 PES payload queued, size=" + payload.length);
        } else {
            Log.w(TAG, "⚠ PES too short to skip header (size=" + pesData.length + ", header=" + headerOffset + ")");
        }
    }
    
}
