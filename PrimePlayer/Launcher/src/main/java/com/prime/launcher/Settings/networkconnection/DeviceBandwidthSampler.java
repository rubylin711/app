package com.prime.launcher.Settings.networkconnection;

import android.net.TrafficStats;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

public class DeviceBandwidthSampler {
    private static long g_previous_bytes = -1;
    private final ConnectionClassManager g_connection_class_manager;
    private SamplingHandler g_handler;
    private long g_last_time_reading;
    private AtomicInteger g_sampling_counter;
    private HandlerThread g_thread;

    private static class DeviceBandwidthSamplerHolder {
        public static final DeviceBandwidthSampler instance = new DeviceBandwidthSampler(ConnectionClassManager.getInstance());

        private DeviceBandwidthSamplerHolder() {
        }
    }

    @NonNull
    public static DeviceBandwidthSampler getInstance() {
        return DeviceBandwidthSamplerHolder.instance;
    }

    public DeviceBandwidthSampler(ConnectionClassManager connectionClassManager) {
        this.g_connection_class_manager = connectionClassManager;
        this.g_sampling_counter = new AtomicInteger();
        this.g_thread = new HandlerThread("ParseThread");
        this.g_thread.start();
        this.g_handler = new SamplingHandler(this.g_thread.getLooper());
    }

    public void start_sampling() {
        if (this.g_sampling_counter.getAndIncrement() == 0) {
            this.g_handler.start_sampling_thread();
            this.g_last_time_reading = SystemClock.elapsedRealtime();
        }
    }

    public void stop_sampling() {
        if (this.g_sampling_counter.decrementAndGet() == 0) {
            this.g_handler.stop_sampling_thread();
            add_final_sample();
        }
    }

    protected void add_sample() {
        long newBytes = TrafficStats.getTotalRxBytes();
        long byteDiff = newBytes - g_previous_bytes;
        if (g_previous_bytes >= 0) {
            synchronized (this) {
                long curTimeReading = SystemClock.elapsedRealtime();
                this.g_connection_class_manager.add_bandwidth(byteDiff, curTimeReading - this.g_last_time_reading);
                this.g_last_time_reading = curTimeReading;
            }
        }
        g_previous_bytes = newBytes;
    }

    protected void add_final_sample() {
        add_sample();
        g_previous_bytes = -1L;
    }

    public boolean is_sampling() {
        return this.g_sampling_counter.get() != 0;
    }

    private class SamplingHandler extends Handler {
        private static final int MSG_START = 1;
        static final long SAMPLE_TIME = 1000;

        public SamplingHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                DeviceBandwidthSampler.this.add_sample();
                sendEmptyMessageDelayed(1, SAMPLE_TIME);
                return;
            }
            throw new IllegalArgumentException("Unknown what=" + msg.what);
        }

        public void start_sampling_thread() {
            sendEmptyMessage(1);
        }

        public void stop_sampling_thread() {
            removeMessages(1);
        }
    }

    public void destory() {
        g_thread.quit();
    }
}
