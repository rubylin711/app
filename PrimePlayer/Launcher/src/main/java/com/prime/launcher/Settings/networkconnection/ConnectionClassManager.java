package com.prime.launcher.Settings.networkconnection;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class ConnectionClassManager {
    static final long BANDWIDTH_LOWER_BOUND = 10;
    private static final int BYTES_TO_BITS = 8;
    private static final double DEFAULT_DECAY_CONSTANT = 0.05d;
    static final int DEFAULT_GOOD_BANDWIDTH = 20000;
    static final long DEFAULT_HYSTERESIS_PERCENT = 20;
    static final int DEFAULT_MODERATE_BANDWIDTH = 10000;
    static final int DEFAULT_POOR_BANDWIDTH = 2000;
    static final double DEFAULT_SAMPLES_TO_QUALITY_CHANGE = 5.0d;
    private static final double HYSTERESIS_BOTTOM_MULTIPLIER = 0.8d;
    private static final double HYSTERESIS_TOP_MULTIPLIER = 1.25d;
    private AtomicReference<ConnectionQuality> g_current_bandwidth_connection_quality;
    private ExponentialGeometricAverage g_download_bandwidth;
    private volatile boolean g_initiate_state_change;
    private ArrayList<connection_class_state_change_listener> g_listener_list;
    private AtomicReference<ConnectionQuality> g_next_bandwidth_connection_quality;
    private int g_sample_counter;

    public interface connection_class_state_change_listener {
        void on_bandwidth_state_change(ConnectionQuality connectionQuality);
    }

    private static class ConnectionClassManagerHolder {
        public static final ConnectionClassManager instance = new ConnectionClassManager();

        private ConnectionClassManagerHolder() {
        }
    }

    @NonNull
    public static ConnectionClassManager getInstance() {
        return ConnectionClassManagerHolder.instance;
    }

    public ConnectionClassManager() {
        this.g_download_bandwidth = new ExponentialGeometricAverage(DEFAULT_DECAY_CONSTANT);
        this.g_initiate_state_change = false;
        this.g_current_bandwidth_connection_quality = new AtomicReference<>(ConnectionQuality.UNKNOWN);
        this.g_listener_list = new ArrayList<>();
    }

    public synchronized void add_bandwidth(long bytes, long timeInMs) {
        if (timeInMs == 0 || ((bytes * 1.0d) / timeInMs) * 8.0d < 10.0d) {
            return;
        }
        double bandwidth = ((bytes * 1.0d) / timeInMs) * 8.0d;
        this.g_download_bandwidth.add_measurement(bandwidth);
        if (this.g_initiate_state_change) {
            this.g_sample_counter++;
            if (get_current_bandwidth_quality() != this.g_next_bandwidth_connection_quality.get()) {
                this.g_initiate_state_change = false;
                this.g_sample_counter = 1;
            }
            if (this.g_sample_counter >= DEFAULT_SAMPLES_TO_QUALITY_CHANGE && significantly_outside_currentBand()) {
                this.g_initiate_state_change = false;
                this.g_sample_counter = 1;
                this.g_current_bandwidth_connection_quality.set(this.g_next_bandwidth_connection_quality.get());
                notify_listeners();
            }
            return;
        }
        if (this.g_current_bandwidth_connection_quality.get() != get_current_bandwidth_quality()) {
            this.g_initiate_state_change = true;
            this.g_next_bandwidth_connection_quality = new AtomicReference<>(get_current_bandwidth_quality());
        }
    }

    private boolean significantly_outside_currentBand() {
        double bottomOfBand;
        double topOfBand;
        if (this.g_download_bandwidth == null) {
            return false;
        }
        ConnectionQuality currentQuality = this.g_current_bandwidth_connection_quality.get();
        switch (currentQuality) {
            case POOR:
                bottomOfBand = 0.0d;
                topOfBand = 2000.0d;
                break;
            case MODERATE:
                bottomOfBand = 2000.0d;
                topOfBand = 10000.0d;
                break;
            case GOOD:
                bottomOfBand = 10000.0d;
                topOfBand = 20000.0d;
                break;
            case EXCELLENT:
                bottomOfBand = 20000.0d;
                topOfBand = 3.4028234663852886E38d;
                break;
            default:
                return true;
        }
        double average = this.g_download_bandwidth.get_average();
        if (average > topOfBand) {
            if (average > HYSTERESIS_TOP_MULTIPLIER * topOfBand) {
                return true;
            }
        } else if (average < HYSTERESIS_BOTTOM_MULTIPLIER * bottomOfBand) {
            return true;
        }
        return false;
    }

    public void reset() {
        if (this.g_download_bandwidth != null) {
            this.g_download_bandwidth.reset();
        }
        this.g_current_bandwidth_connection_quality.set(ConnectionQuality.UNKNOWN);
    }

    public synchronized ConnectionQuality get_current_bandwidth_quality() {
        if (this.g_download_bandwidth == null) {
            return ConnectionQuality.UNKNOWN;
        }
        return map_bandwidth_quality(this.g_download_bandwidth.get_average());
    }

    private ConnectionQuality map_bandwidth_quality(double average) {
        if (average < 0.0d) {
            return ConnectionQuality.UNKNOWN;
        }
        if (average < 2000.0d) {
            return ConnectionQuality.POOR;
        }
        if (average < 10000.0d) {
            return ConnectionQuality.MODERATE;
        }
        if (average < 20000.0d) {
            return ConnectionQuality.GOOD;
        }
        return ConnectionQuality.EXCELLENT;
    }

    public synchronized double get_download_kbits_per_second() {
        return this.g_download_bandwidth == null ? -1.0d : this.g_download_bandwidth.get_average();
    }

    public ConnectionQuality register(connection_class_state_change_listener listener) {
        if (listener != null) {
            this.g_listener_list.add(listener);
        }
        return this.g_current_bandwidth_connection_quality.get();
    }

    public void remove(connection_class_state_change_listener listener) {
        if (listener != null) {
            this.g_listener_list.remove(listener);
        }
    }

    private void notify_listeners() {
        int size = this.g_listener_list.size();
        for (int i = 0; i < size; i++) {
            this.g_listener_list.get(i).on_bandwidth_state_change(this.g_current_bandwidth_connection_quality.get());
        }
    }
}
