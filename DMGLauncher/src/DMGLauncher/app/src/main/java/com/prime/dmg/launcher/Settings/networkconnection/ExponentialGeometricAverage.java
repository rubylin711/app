package com.prime.dmg.launcher.Settings.networkconnection;

class ExponentialGeometricAverage {
    private int g_count;
    private final int g_cutover;
    private final double g_decay_constant;
    private double g_value = -1.0d;

    public ExponentialGeometricAverage(double decayConstant) {
        this.g_decay_constant = decayConstant;
        this.g_cutover = decayConstant == 0.0d ? Integer.MAX_VALUE : (int) Math.ceil(1.0d / decayConstant);
    }

    public void add_measurement(double measurement) {
        double keepConstant = 1.0d - this.g_decay_constant;
        if (this.g_count > this.g_cutover) {
            this.g_value = Math.exp((Math.log(this.g_value) * keepConstant) + (this.g_decay_constant * Math.log(measurement)));
        } else if (this.g_count > 0) {
            double retained = (this.g_count * keepConstant) / (this.g_count + 1.0d);
            double newcomer = 1.0d - retained;
            this.g_value = Math.exp((Math.log(this.g_value) * retained) + (Math.log(measurement) * newcomer));
        } else {
            this.g_value = measurement;
        }
        this.g_count++;
    }

    public double get_average() {
        return this.g_value;
    }

    public void reset() {
        this.g_value = -1.0d;
        this.g_count = 0;
    }
}
